
package com.ib.iscipr.testClient;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for RequestDataISCIPR complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="RequestDataISCIPR"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;element name="Argument" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="CallContext" type="{http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi}CallContext" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="CitizenEGN" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="EmployeeEGN" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="Operation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RequestDataISCIPR", namespace = "http://iscipr.egov.bg", propOrder = {
    "argument",
    "callContext",
    "citizenEGN",
    "employeeEGN",
    "operation"
})
public class RequestDataISCIPR {

    @XmlElementRef(name = "Argument", namespace = "http://iscipr.egov.bg", type = JAXBElement.class, required = false)
    protected JAXBElement<String> argument;
    @XmlElementRef(name = "CallContext", namespace = "http://iscipr.egov.bg", type = JAXBElement.class, required = false)
    protected JAXBElement<CallContext> callContext;
    @XmlElementRef(name = "CitizenEGN", namespace = "http://iscipr.egov.bg", type = JAXBElement.class, required = false)
    protected JAXBElement<String> citizenEGN;
    @XmlElementRef(name = "EmployeeEGN", namespace = "http://iscipr.egov.bg", type = JAXBElement.class, required = false)
    protected JAXBElement<String> employeeEGN;
    @XmlElementRef(name = "Operation", namespace = "http://iscipr.egov.bg", type = JAXBElement.class, required = false)
    protected JAXBElement<String> operation;

    /**
     * Gets the value of the argument property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getArgument() {
        return argument;
    }

    /**
     * Sets the value of the argument property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setArgument(JAXBElement<String> value) {
        this.argument = value;
    }

    /**
     * Gets the value of the callContext property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link CallContext }{@code >}
     *     
     */
    public JAXBElement<CallContext> getCallContext() {
        return callContext;
    }

    /**
     * Sets the value of the callContext property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link CallContext }{@code >}
     *     
     */
    public void setCallContext(JAXBElement<CallContext> value) {
        this.callContext = value;
    }

    /**
     * Gets the value of the citizenEGN property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getCitizenEGN() {
        return citizenEGN;
    }

    /**
     * Sets the value of the citizenEGN property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setCitizenEGN(JAXBElement<String> value) {
        this.citizenEGN = value;
    }

    /**
     * Gets the value of the employeeEGN property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getEmployeeEGN() {
        return employeeEGN;
    }

    /**
     * Sets the value of the employeeEGN property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setEmployeeEGN(JAXBElement<String> value) {
        this.employeeEGN = value;
    }

    /**
     * Gets the value of the operation property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getOperation() {
        return operation;
    }

    /**
     * Sets the value of the operation property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setOperation(JAXBElement<String> value) {
        this.operation = value;
    }

}
