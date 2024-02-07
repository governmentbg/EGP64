
package com.ib.mms.iscipr.client;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for CallContext complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="CallContext"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;element name="AdministrationName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="AdministrationOId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="EmployeeAdditionalIdentifier" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="EmployeeIdentifier" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="EmployeeNames" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="EmployeePosition" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="LawReason" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="Remark" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="ServiceType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="ServiceURI" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CallContext", namespace = "http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", propOrder = {
    "administrationName",
    "administrationOId",
    "employeeAdditionalIdentifier",
    "employeeIdentifier",
    "employeeNames",
    "employeePosition",
    "lawReason",
    "remark",
    "serviceType",
    "serviceURI"
})
public class CallContext {

    @XmlElementRef(name = "AdministrationName", namespace = "http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", type = JAXBElement.class, required = false)
    protected JAXBElement<String> administrationName;
    @XmlElementRef(name = "AdministrationOId", namespace = "http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", type = JAXBElement.class, required = false)
    protected JAXBElement<String> administrationOId;
    @XmlElementRef(name = "EmployeeAdditionalIdentifier", namespace = "http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", type = JAXBElement.class, required = false)
    protected JAXBElement<String> employeeAdditionalIdentifier;
    @XmlElementRef(name = "EmployeeIdentifier", namespace = "http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", type = JAXBElement.class, required = false)
    protected JAXBElement<String> employeeIdentifier;
    @XmlElementRef(name = "EmployeeNames", namespace = "http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", type = JAXBElement.class, required = false)
    protected JAXBElement<String> employeeNames;
    @XmlElementRef(name = "EmployeePosition", namespace = "http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", type = JAXBElement.class, required = false)
    protected JAXBElement<String> employeePosition;
    @XmlElementRef(name = "LawReason", namespace = "http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", type = JAXBElement.class, required = false)
    protected JAXBElement<String> lawReason;
    @XmlElementRef(name = "Remark", namespace = "http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", type = JAXBElement.class, required = false)
    protected JAXBElement<String> remark;
    @XmlElementRef(name = "ServiceType", namespace = "http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", type = JAXBElement.class, required = false)
    protected JAXBElement<String> serviceType;
    @XmlElementRef(name = "ServiceURI", namespace = "http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", type = JAXBElement.class, required = false)
    protected JAXBElement<String> serviceURI;

    /**
     * Gets the value of the administrationName property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getAdministrationName() {
        return administrationName;
    }

    /**
     * Sets the value of the administrationName property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setAdministrationName(JAXBElement<String> value) {
        this.administrationName = value;
    }

    /**
     * Gets the value of the administrationOId property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getAdministrationOId() {
        return administrationOId;
    }

    /**
     * Sets the value of the administrationOId property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setAdministrationOId(JAXBElement<String> value) {
        this.administrationOId = value;
    }

    /**
     * Gets the value of the employeeAdditionalIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getEmployeeAdditionalIdentifier() {
        return employeeAdditionalIdentifier;
    }

    /**
     * Sets the value of the employeeAdditionalIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setEmployeeAdditionalIdentifier(JAXBElement<String> value) {
        this.employeeAdditionalIdentifier = value;
    }

    /**
     * Gets the value of the employeeIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getEmployeeIdentifier() {
        return employeeIdentifier;
    }

    /**
     * Sets the value of the employeeIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setEmployeeIdentifier(JAXBElement<String> value) {
        this.employeeIdentifier = value;
    }

    /**
     * Gets the value of the employeeNames property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getEmployeeNames() {
        return employeeNames;
    }

    /**
     * Sets the value of the employeeNames property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setEmployeeNames(JAXBElement<String> value) {
        this.employeeNames = value;
    }

    /**
     * Gets the value of the employeePosition property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getEmployeePosition() {
        return employeePosition;
    }

    /**
     * Sets the value of the employeePosition property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setEmployeePosition(JAXBElement<String> value) {
        this.employeePosition = value;
    }

    /**
     * Gets the value of the lawReason property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getLawReason() {
        return lawReason;
    }

    /**
     * Sets the value of the lawReason property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setLawReason(JAXBElement<String> value) {
        this.lawReason = value;
    }

    /**
     * Gets the value of the remark property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getRemark() {
        return remark;
    }

    /**
     * Sets the value of the remark property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setRemark(JAXBElement<String> value) {
        this.remark = value;
    }

    /**
     * Gets the value of the serviceType property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getServiceType() {
        return serviceType;
    }

    /**
     * Sets the value of the serviceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setServiceType(JAXBElement<String> value) {
        this.serviceType = value;
    }

    /**
     * Gets the value of the serviceURI property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getServiceURI() {
        return serviceURI;
    }

    /**
     * Sets the value of the serviceURI property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setServiceURI(JAXBElement<String> value) {
        this.serviceURI = value;
    }

}
