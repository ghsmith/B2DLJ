package b2dlj;

import b2dlj.data.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Iterator;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DownloadSlides {

    private final static Logger LOGGER = LoggerFactory.getLogger(DownloadSlides.class);

    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    public static void main(String[] args) throws Exception {

        StudyPatients studyPatients = StudyPatients.Unmarshal(args[0]);

        Properties prop = new Properties();
        InputStream stream = new FileInputStream(args[1]);
        prop.load(stream);

        String urlU = prop.getProperty("urlU");
        String urlP = prop.getProperty("urlP");
        String prodName = prop.getProperty("prodName");
        String u = prop.getProperty("u");
        String p = prop.getProperty("p");
        String domain = prop.getProperty("d") != null ? prop.getProperty("d") : "";

        CookieHandler.setDefault(new CookieManager());
        HttpClient client = HttpClient.newBuilder()
                .cookieHandler(CookieHandler.getDefault())
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        String startSlideId = args.length >= 3 ? args[2] : null;
        boolean startSlideIdFound = false;

        for (Patient patient : studyPatients.patients) {
        for (PathologyCase pathologyCase : patient.pathologyCases) {
        for (Part part : pathologyCase.parts) {
        for (Block block : part.blocks) {
        for (Slide slide : block.slides) {

            LOGGER.info(String.format("processing slide %s", slide.slideDesigLab));

            if (startSlideId != null && !startSlideIdFound) {
                startSlideIdFound = startSlideId.equals(slide.slideDesigLab);
                if (!startSlideIdFound) {
                    LOGGER.info("skipping (start slide not found yet)");
                    continue;
                }
            }

            if (slide.anonSlideFileName != null && slide.anonSlideFileName.length() > 0) {
                LOGGER.info("skipping (already downloaded)");
                continue;
            }

            // LOGIN
            {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(urlU + "/Logon.ashx"))
                        .setHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                        .POST(HttpRequest.BodyPublishers.ofString(String.format("{\"Domain\":\"%s\",\"Password\":\"%s\",\"UserName\":\"%s\",\"AutoselectLastAssignment\":true}", domain, p, u)))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response == null || response.statusCode() != 200) {
                    LOGGER.warn("login failed");
                    System.exit(1);
                }
                LOGGER.info("logged into server");
            }

            // FIND DESIRED CASE
            String patientIdentifier = null;
            String patientHistoryFetchToken = null;
            String historyItemIdentifier = null;
            String fetchToken = null;
            String studyDescription = null;
            {

                HttpRequest requestSearch = HttpRequest.newBuilder()
                        .uri(URI.create(urlU + "/Search.ashx"))
                        .setHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                        .POST(HttpRequest.BodyPublishers.ofString(String.format("searchServer=2&freeText=\"%s\"", pathologyCase.accessionNumber)))
                        .build();
                HttpResponse<String> responseSearch = client.send(requestSearch, HttpResponse.BodyHandlers.ofString());

                for (Iterator iterPatient = (new JSONObject(responseSearch.body())).getJSONArray("Patients").iterator(); iterPatient.hasNext(); ) {

                    JSONObject joPatient = (JSONObject) iterPatient.next();
                    patientIdentifier = joPatient.getString(prodName + "PatientIdentifier");
                    patientHistoryFetchToken = joPatient.getString("PatientHistoryFetchToken");

                    HttpRequest requestHistory = HttpRequest.newBuilder()
                            .uri(URI.create(urlU + "/GetPatientHistory.ashx"))
                            .setHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                            .POST(HttpRequest.BodyPublishers.ofString(String.format("patientHistoryFetchToken=%s", URLEncoder.encode(patientHistoryFetchToken))))
                            .build();
                    HttpResponse<String> responseHistory = client.send(requestHistory, HttpResponse.BodyHandlers.ofString());

                    for (Iterator iterHistory = new JSONObject(responseHistory.body()).getJSONArray("History").iterator(); iterHistory.hasNext(); ) {
                        JSONObject joHistory = (JSONObject) iterHistory.next();
                        String accessionNumber = joHistory.getString("AccessionNumber");
                        if (accessionNumber.equals(pathologyCase.accessionNumber)) {
                            historyItemIdentifier = joHistory.getString(prodName + "HistoryItemIdentifier");
                            fetchToken = joHistory.getJSONArray("RequestedProcedures").getJSONObject(0).getString("FetchToken");
                            studyDescription = joHistory.getJSONArray("RequestedProcedures").getJSONObject(0).getString("StudyDescription");
                            break;
                        }
                    }

                    if (historyItemIdentifier != null) {
                        break;
                    }

                }

            }

            if (historyItemIdentifier == null) {
                LOGGER.warn(String.format("examination \"%s\" not found", pathologyCase.accessionNumber));
                logout(urlU, client);
                continue;
            }

            // INITIALIZE PATHOLOGY SESSION
            String value = null;
            String hash = null;
            {
                HttpRequest requestPathSession = HttpRequest.newBuilder()
                        .uri(URI.create(urlU + "/InitializePathologySession.ashx"))
                        .setHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                        .POST(HttpRequest.BodyPublishers.ofString(String.format("fetchToken=%s", URLEncoder.encode(fetchToken))))
                        .build();
                HttpResponse<String> responsePathSession = client.send(requestPathSession, HttpResponse.BodyHandlers.ofString());
                JSONObject joPathSession = new JSONObject(responsePathSession.body());
                value = joPathSession.getString("Value");
                hash = joPathSession.getString("Hash");
            }

            // LOOP THROUGH SLIDES
            boolean slideFound = false;
            {
                HttpRequest requestSlides = HttpRequest.newBuilder()
                        .uri(URI.create(String.format(urlP + "/api/requestslides?requestId=%s&hash=%s", URLEncoder.encode(value), URLEncoder.encode(hash))))
                        .setHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                        .GET()
                        .build();
                HttpResponse<String> responseSlides = client.send(requestSlides, HttpResponse.BodyHandlers.ofString());
                JSONObject joSlides = new JSONObject(responseSlides.body());
                for (Iterator iterSlides = joSlides.getJSONArray("slides").iterator(); iterSlides.hasNext(); ) {
                    JSONObject slideJson = (JSONObject) iterSlides.next();
                    if (slideJson.get("labSlideIdString").equals(slide.slideDesigLab) && slideJson.getBoolean("hasImage")) {
                        LOGGER.info(String.format("%s\t%s\t%s\t%s\t%s\t%s",
                                slideJson.getString("requestIdString"),
                                slideJson.getString("labSlideIdString"),
                                slideJson.getString("staining"),
                                slideJson.getString("scanDateTime"),
                                slideJson.getBoolean("isViewable"),
                                slideJson.isNull("archived") ? null : slideJson.getString("archived")
                        ));
                        if (slideJson.getBoolean("isViewable")) {
                            HttpRequest requestDetails = HttpRequest.newBuilder()
                                    .uri(URI.create(String.format(urlP + "/api/slides/%s/details", slideJson.getString("id"))))
                                    .setHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                                    .GET()
                                    .build();
                            HttpResponse<String> responseDetails = client.send(requestDetails, HttpResponse.BodyHandlers.ofString());
                            JSONObject joDetails = new JSONObject(responseDetails.body());
                            LOGGER.info("slide is accessible");
                            slideFound = true;
                            LOGGER.info(String.format(urlP + "/slides/%s/files?requestId=%s&anonymize=true", slideJson.getString("id"), pathologyCase.accessionNumber));
                            System.out.print(String.format("%-25s", "downloading ZIP..."));
                            HttpRequest requestDownload = HttpRequest.newBuilder()
                                    .uri(URI.create(String.format(urlP + "/slides/%s/files?requestId=%s&anonymize=true", slideJson.getString("id"), pathologyCase.accessionNumber)))
                                    .build();
                            HttpResponse<Path> responseDownload = client.send(requestDownload, HttpResponse.BodyHandlers.ofFileDownload(
                                    Path.of("."),
                                    StandardOpenOption.WRITE,
                                    StandardOpenOption.CREATE
                            ));
                            LOGGER.info(String.format("downloaded to: %s", responseDownload.body().getFileName()));
                            ZipFile zipFile = new ZipFile(responseDownload.body().toFile());
                            for (Iterator iterZipEntries = zipFile.entries().asIterator(); iterZipEntries.hasNext(); ) {
                                ZipEntry zipEntry = (ZipEntry) iterZipEntries.next();
                                if (zipEntry.getName().endsWith(".svs")) {
                                    String anonId = responseDownload.body().getFileName().toString().replaceAll(".zip$", "").replaceAll("^ANON", "");
                                    String stainAbridged = slide.stain.replace(" ", "_").replace("&", "").replace("-", "").replace("/", "").replace("'", "");
                                    stainAbridged = stainAbridged.substring(0, Math.min(10, stainAbridged.length()));
                                    String svsFileName = String.format("%s-%s-%s%02d-%02d-%s-%s.svs", patient.patIdStudy.substring(0,8), pathologyCase.caseIdStudy.substring(0, 8), part.partDesig, block.blockNo, slide.slideNo, stainAbridged, anonId);
                                    LOGGER.info(String.format("%-25s", "unzipping..."));
                                    Files.copy(zipFile.getInputStream(zipEntry), new File(svsFileName).toPath(), StandardCopyOption.REPLACE_EXISTING);
                                    LOGGER.info(String.format("unzipped to: %s", svsFileName));
                                    slide.anonSlideId = anonId;
                                    slide.anonSlideFileName = svsFileName;
                                    studyPatients.marshal(args[0] + ".DownloadSlides");
                                }
                            }
                            zipFile.close();
                            LOGGER.info(String.format("%-25s", "deleting ZIP..."));
                            responseDownload.body().toFile().delete();
                            LOGGER.info("ZIP deleted");
                        } else {
                            LOGGER.warn("slide is not accessible - recall may be required");
                        }
                    }
                }
            }

            if (!slideFound) {
                LOGGER.warn(String.format("slide \"%s\" ERROR", slide.slideDesigLab));
                logout(urlU, client);
                continue;
            }

            logout(urlU, client);

        }
        }
        }
        }
        }

    }

    public static void logout(String urlU, HttpClient client) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlU + "/Logout.ashx"))
                .setHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(String.format("0")))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        LOGGER.info("logged out of server");
    }

}