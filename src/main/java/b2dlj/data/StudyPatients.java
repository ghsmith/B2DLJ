package b2dlj.data;

import jakarta.xml.bind.*;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.List;

@XmlRootElement
public class StudyPatients {

    @XmlAttribute
    public String studyName;

    @XmlElement(name = "patient")
    public List<Patient> patients;

    public void marshal(String fileName) throws JAXBException, IOException {

        JAXBContext jc = JAXBContext.newInstance(new Class[] { StudyPatients.class });
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(this, new FileOutputStream(new File(fileName + ".xml")));

        jc.generateSchema(new SchemaOutputResolver() {
            public Result createOutput(String namespaceURI, String suggestedFileName) throws IOException {
                File file = new File(fileName + ".xsd");
                StreamResult result = new StreamResult(file);
                result.setSystemId(file.toURI().toURL().toString());
                return result;
            }
        });

    }

    public static StudyPatients Unmarshal(String fileName) throws FileNotFoundException, JAXBException {

        JAXBContext jc = JAXBContext.newInstance(new Class[] { StudyPatients.class });
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return (StudyPatients) unmarshaller.unmarshal(new FileInputStream(fileName + ".xml"));

    }

}