package b2dlj.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Slide {

    @XmlAttribute
    public int slideNo;
    @XmlAttribute
    public String stain;

}