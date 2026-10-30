package b2dlj.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement
public class Block {

    @XmlAttribute
    public int blockNo;

    @XmlElementWrapper(name = "slides")
    @XmlElement(name = "slide")
    public List<Slide> slides;

}