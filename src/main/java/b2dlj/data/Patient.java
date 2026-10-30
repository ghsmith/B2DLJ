package b2dlj.data;

import jakarta.xml.bind.annotation.*;

import java.util.Date;
import java.util.List;

@XmlRootElement
public class Patient {

    @XmlTransient
    public String patId;
    @XmlAttribute
    public String mrn;
    @XmlAttribute
    public Date dateOfBirth;
    @XmlAttribute
    public String sex;
    @XmlAttribute
    public String race;

    @XmlElementWrapper(name = "pathologyCases")
    @XmlElement(name = "pathologyCase")
    public List<PathologyCase> pathologyCases;

}