package b2dlj.data;

import jakarta.xml.bind.annotation.*;

import java.util.Date;
import java.util.List;

@XmlRootElement
public class PathologyCase {

    @XmlTransient
    public long caseId;
    @XmlAttribute
    public String caseIdStudy;
    @XmlAttribute
    public String accessionNumber;
    @XmlAttribute
    public Date collectionDate;

    @XmlElement
    public String addendum5Dx;
    @XmlElement
    public String addendum4Dx;
    @XmlElement
    public String addendum3Dx;
    @XmlElement
    public String addendum2Dx;
    @XmlElement
    public String addendum1Dx;
    @XmlElement
    public String finalDx;
    @XmlElement
    public String dxComment;
    @XmlElement
    public String specialStains;
    @XmlElement
    public String grossDescr;
    @XmlElement
    public String microDescr;
    @XmlElement
    public String clinicalHx;

    @XmlElementWrapper(name = "parts")
    @XmlElement(name = "part")
    public List<Part> parts;

}