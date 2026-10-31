package b2dlj.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement
public class Part {

    @XmlAttribute
    public String partDesig;
    @XmlAttribute
    public String partDesigLab;
    @XmlAttribute
    public String partType;
    @XmlAttribute
    public String source;
    @XmlAttribute
    public String protocol;
    @XmlAttribute
    public String descr;

    @XmlElementWrapper(name = "blocks")
    @XmlElement(name = "block")
    public List<Block> blocks;

}