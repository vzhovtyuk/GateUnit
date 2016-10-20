
package document;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}GateDocumentFeatures"/>
 *         &lt;element ref="{}TextWithNodes"/>
 *         &lt;choice>
 *           &lt;element ref="{}AnnotationSet" maxOccurs="unbounded"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "gateDocumentFeatures",
    "textWithNodes",
    "annotationSet"
})
@XmlRootElement(name = "GateDocument")
public class GateDocument {

    @XmlElement(name = "GateDocumentFeatures", required = true)
    protected GateDocumentFeatures gateDocumentFeatures;
    @XmlElement(name = "TextWithNodes", required = true)
    protected TextWithNodes textWithNodes;
    @XmlElement(name = "AnnotationSet")
    protected List<AnnotationSet> annotationSet;

    /**
     * Gets the value of the gateDocumentFeatures property.
     * 
     * @return
     *     possible object is
     *     {@link GateDocumentFeatures }
     *     
     */
    public GateDocumentFeatures getGateDocumentFeatures() {
        return gateDocumentFeatures;
    }

    /**
     * Sets the value of the gateDocumentFeatures property.
     * 
     * @param value
     *     allowed object is
     *     {@link GateDocumentFeatures }
     *     
     */
    public void setGateDocumentFeatures(GateDocumentFeatures value) {
        this.gateDocumentFeatures = value;
    }

    /**
     * Gets the value of the textWithNodes property.
     * 
     * @return
     *     possible object is
     *     {@link TextWithNodes }
     *     
     */
    public TextWithNodes getTextWithNodes() {
        return textWithNodes;
    }

    /**
     * Sets the value of the textWithNodes property.
     * 
     * @param value
     *     allowed object is
     *     {@link TextWithNodes }
     *     
     */
    public void setTextWithNodes(TextWithNodes value) {
        this.textWithNodes = value;
    }

    /**
     * Gets the value of the annotationSet property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the annotationSet property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAnnotationSet().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AnnotationSet }
     * 
     * 
     */
    public List<AnnotationSet> getAnnotationSet() {
        if (annotationSet == null) {
            annotationSet = new ArrayList<AnnotationSet>();
        }
        return this.annotationSet;
    }

}
