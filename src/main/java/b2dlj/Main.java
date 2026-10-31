package b2dlj;

import b2dlj.data.PathologyCase;
import b2dlj.data.Patient;
import b2dlj.data.StudyPatients;
import b2dlj.finder.PartFinder;
import b2dlj.finder.PathologyCaseFinder;
import b2dlj.finder.PatientFinder;
import jakarta.xml.bind.JAXBException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws ClassNotFoundException, SQLException, JAXBException, IOException {

        List<String> accessionNumbers = (new BufferedReader(new InputStreamReader(System.in))).lines().toList();

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        // integrated security requires mssql-jdbc_auth-....dll on -Djava.library.path
        Connection connClr = DriverManager.getConnection(args[0]);

        PatientFinder PatientFinder = new PatientFinder(connClr, args[1]);
        PathologyCaseFinder pathologyCaseFinder = new PathologyCaseFinder(connClr, args[1]);
        PartFinder partFinder = new PartFinder(connClr);

        StudyPatients studyPatients = new StudyPatients();
        studyPatients.studyName = "Larry";
        studyPatients.patients = new ArrayList<>();

        Map<String, Patient> patientMap = new HashMap<>();

        for(String accessionNumber : accessionNumbers) {

            Patient patient = PatientFinder.getByAccessionNumber(accessionNumber);
            if (patientMap.get(patient.patId) != null) {
                patient = patientMap.get(patient.patId);
            } else {
                patientMap.put(patient.patId, patient);
                studyPatients.patients.add(patient);
                patient.pathologyCases = new ArrayList<>();
            }

            patient.pathologyCases.add(pathologyCaseFinder.getByAccessionNumber(accessionNumber));

            for(PathologyCase pathologyCase : patient.pathologyCases) {

                pathologyCase.parts = partFinder.getByCaseId(pathologyCase.caseId);

            }

        }

        connClr.close();

        studyPatients.marshal("StudyPatients");

    }

}