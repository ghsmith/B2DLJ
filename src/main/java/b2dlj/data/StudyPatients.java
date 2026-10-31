package b2dlj.data;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.SchemaOutputResolver;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

}