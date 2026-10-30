package b2dlj.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement
public class StudyPatients {

    @XmlAttribute
    public String studyName;

    @XmlElement(name = "patient")
    public List<Patient> patients;

}