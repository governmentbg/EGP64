
package com.ib.iscipr.testClient;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for anonymous complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;element name="RegisterRecordEntryResult" type="{http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi}ServiceResultISCIPR" minOccurs="0"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "registerRecordEntryResult"
})
@XmlRootElement(name = "RegisterRecordEntryResponse")
public class RegisterRecordEntryResponse {

    @XmlElementRef(name = "RegisterRecordEntryResult", namespace = "http://iscipr.egov.bg/", type = JAXBElement.class, required = false)
    protected JAXBElement<ServiceResultISCIPR> registerRecordEntryResult;

    /**
     * Gets the value of the registerRecordEntryResult property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ServiceResultISCIPR }{@code >}
     *     
     */
    public JAXBElement<ServiceResultISCIPR> getRegisterRecordEntryResult() {
        return registerRecordEntryResult;
    }

    /**
     * Sets the value of the registerRecordEntryResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ServiceResultISCIPR }{@code >}
     *     
     */
    public void setRegisterRecordEntryResult(JAXBElement<ServiceResultISCIPR> value) {
        this.registerRecordEntryResult = value;
    }

}