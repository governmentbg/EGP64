
package com.ib.mms.iscipr.client;

import java.math.BigDecimal;
import java.math.BigInteger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.ib.iscipr.testClient package. 
 * &lt;p&gt;An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _AnyType_QNAME = new QName("http://schemas.microsoft.com/2003/10/Serialization/", "anyType");
    private final static QName _AnyURI_QNAME = new QName("http://schemas.microsoft.com/2003/10/Serialization/", "anyURI");
    private final static QName _Base64Binary_QNAME = new QName("http://schemas.microsoft.com/2003/10/Serialization/", "base64Binary");
    private final static QName _Boolean_QNAME = new QName("http://schemas.microsoft.com/2003/10/Serialization/", "boolean");
    private final static QName _Byte_QNAME = new QName("http://schemas.microsoft.com/2003/10/Serialization/", "byte");
    private final static QName _DateTime_QNAME = new QName("http://schemas.microsoft.com/2003/10/Serialization/", "dateTime");
    private final static QName _Decimal_QNAME = new QName("http://schemas.microsoft.com/2003/10/Serialization/", "decimal");
    private final static QName _Double_QNAME = new QName("http://schemas.microsoft.com/2003/10/Serialization/", "double");
    private final static QName _Float_QNAME = new QName("http://schemas.microsoft.com/2003/10/Serialization/", "float");
    private final static QName _Int_QNAME = new QName("http://schemas.microsoft.com/2003/10/Serialization/", "int");
    private final static QName _Long_QNAME = new QName("http://schemas.microsoft.com/2003/10/Serialization/", "long");
    private final static QName _QName_QNAME = new QName("http://schemas.microsoft.com/2003/10/Serialization/", "QName");
    private final static QName _Short_QNAME = new QName("http://schemas.microsoft.com/2003/10/Serialization/", "short");
    private final static QName _String_QNAME = new QName("http://schemas.microsoft.com/2003/10/Serialization/", "string");
    private final static QName _UnsignedByte_QNAME = new QName("http://schemas.microsoft.com/2003/10/Serialization/", "unsignedByte");
    private final static QName _UnsignedInt_QNAME = new QName("http://schemas.microsoft.com/2003/10/Serialization/", "unsignedInt");
    private final static QName _UnsignedLong_QNAME = new QName("http://schemas.microsoft.com/2003/10/Serialization/", "unsignedLong");
    private final static QName _UnsignedShort_QNAME = new QName("http://schemas.microsoft.com/2003/10/Serialization/", "unsignedShort");
    private final static QName _Char_QNAME = new QName("http://schemas.microsoft.com/2003/10/Serialization/", "char");
    private final static QName _Duration_QNAME = new QName("http://schemas.microsoft.com/2003/10/Serialization/", "duration");
    private final static QName _Guid_QNAME = new QName("http://schemas.microsoft.com/2003/10/Serialization/", "guid");
    private final static QName _RequestDataISCIPR_QNAME = new QName("http://iscipr.egov.bg", "RequestDataISCIPR");
    private final static QName _CallContext_QNAME = new QName("http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", "CallContext");
    private final static QName _ServiceResultISCIPR_QNAME = new QName("http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", "ServiceResultISCIPR");
    private final static QName _RegisterRecordEntryRequestData_QNAME = new QName("http://iscipr.egov.bg/", "requestData");
    private final static QName _RegisterRecordEntryResponseRegisterRecordEntryResult_QNAME = new QName("http://iscipr.egov.bg/", "RegisterRecordEntryResult");
    private final static QName _RegisterRecordChangeResponseRegisterRecordChangeResult_QNAME = new QName("http://iscipr.egov.bg/", "RegisterRecordChangeResult");
    private final static QName _RegisterRecordRemoveResponseRegisterRecordRemoveResult_QNAME = new QName("http://iscipr.egov.bg/", "RegisterRecordRemoveResult");
    private final static QName _CallContextAdministrationName_QNAME = new QName("http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", "AdministrationName");
    private final static QName _CallContextAdministrationOId_QNAME = new QName("http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", "AdministrationOId");
    private final static QName _CallContextEmployeeAdditionalIdentifier_QNAME = new QName("http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", "EmployeeAdditionalIdentifier");
    private final static QName _CallContextEmployeeIdentifier_QNAME = new QName("http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", "EmployeeIdentifier");
    private final static QName _CallContextEmployeeNames_QNAME = new QName("http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", "EmployeeNames");
    private final static QName _CallContextEmployeePosition_QNAME = new QName("http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", "EmployeePosition");
    private final static QName _CallContextLawReason_QNAME = new QName("http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", "LawReason");
    private final static QName _CallContextRemark_QNAME = new QName("http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", "Remark");
    private final static QName _CallContextServiceType_QNAME = new QName("http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", "ServiceType");
    private final static QName _CallContextServiceURI_QNAME = new QName("http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", "ServiceURI");
    private final static QName _ServiceResultISCIPRData_QNAME = new QName("http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", "Data");
    private final static QName _ServiceResultISCIPRErrorCode_QNAME = new QName("http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", "ErrorCode");
    private final static QName _ServiceResultISCIPRErrorMessage_QNAME = new QName("http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", "ErrorMessage");
    private final static QName _RequestDataISCIPRArgument_QNAME = new QName("http://iscipr.egov.bg", "Argument");
    private final static QName _RequestDataISCIPRCallContext_QNAME = new QName("http://iscipr.egov.bg", "CallContext");
    private final static QName _RequestDataISCIPRCitizenEGN_QNAME = new QName("http://iscipr.egov.bg", "CitizenEGN");
    private final static QName _RequestDataISCIPREmployeeEGN_QNAME = new QName("http://iscipr.egov.bg", "EmployeeEGN");
    private final static QName _RequestDataISCIPROperation_QNAME = new QName("http://iscipr.egov.bg", "Operation");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.ib.iscipr.testClient
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RegisterRecordEntry }
     * 
     */
    public RegisterRecordEntry createRegisterRecordEntry() {
        return new RegisterRecordEntry();
    }

    /**
     * Create an instance of {@link RequestDataISCIPR }
     * 
     */
    public RequestDataISCIPR createRequestDataISCIPR() {
        return new RequestDataISCIPR();
    }

    /**
     * Create an instance of {@link RegisterRecordEntryResponse }
     * 
     */
    public RegisterRecordEntryResponse createRegisterRecordEntryResponse() {
        return new RegisterRecordEntryResponse();
    }

    /**
     * Create an instance of {@link ServiceResultISCIPR }
     * 
     */
    public ServiceResultISCIPR createServiceResultISCIPR() {
        return new ServiceResultISCIPR();
    }

    /**
     * Create an instance of {@link RegisterRecordChange }
     * 
     */
    public RegisterRecordChange createRegisterRecordChange() {
        return new RegisterRecordChange();
    }

    /**
     * Create an instance of {@link RegisterRecordChangeResponse }
     * 
     */
    public RegisterRecordChangeResponse createRegisterRecordChangeResponse() {
        return new RegisterRecordChangeResponse();
    }

    /**
     * Create an instance of {@link RegisterRecordRemove }
     * 
     */
    public RegisterRecordRemove createRegisterRecordRemove() {
        return new RegisterRecordRemove();
    }

    /**
     * Create an instance of {@link RegisterRecordRemoveResponse }
     * 
     */
    public RegisterRecordRemoveResponse createRegisterRecordRemoveResponse() {
        return new RegisterRecordRemoveResponse();
    }

    /**
     * Create an instance of {@link CallContext }
     * 
     */
    public CallContext createCallContext() {
        return new CallContext();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Object }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/2003/10/Serialization/", name = "anyType")
    public JAXBElement<Object> createAnyType(Object value) {
        return new JAXBElement<Object>(_AnyType_QNAME, Object.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/2003/10/Serialization/", name = "anyURI")
    public JAXBElement<String> createAnyURI(String value) {
        return new JAXBElement<String>(_AnyURI_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/2003/10/Serialization/", name = "base64Binary")
    public JAXBElement<byte[]> createBase64Binary(byte[] value) {
        return new JAXBElement<byte[]>(_Base64Binary_QNAME, byte[].class, null, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/2003/10/Serialization/", name = "boolean")
    public JAXBElement<Boolean> createBoolean(Boolean value) {
        return new JAXBElement<Boolean>(_Boolean_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Byte }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Byte }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/2003/10/Serialization/", name = "byte")
    public JAXBElement<Byte> createByte(Byte value) {
        return new JAXBElement<Byte>(_Byte_QNAME, Byte.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/2003/10/Serialization/", name = "dateTime")
    public JAXBElement<XMLGregorianCalendar> createDateTime(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_DateTime_QNAME, XMLGregorianCalendar.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/2003/10/Serialization/", name = "decimal")
    public JAXBElement<BigDecimal> createDecimal(BigDecimal value) {
        return new JAXBElement<BigDecimal>(_Decimal_QNAME, BigDecimal.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Double }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Double }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/2003/10/Serialization/", name = "double")
    public JAXBElement<Double> createDouble(Double value) {
        return new JAXBElement<Double>(_Double_QNAME, Double.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Float }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Float }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/2003/10/Serialization/", name = "float")
    public JAXBElement<Float> createFloat(Float value) {
        return new JAXBElement<Float>(_Float_QNAME, Float.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/2003/10/Serialization/", name = "int")
    public JAXBElement<Integer> createInt(Integer value) {
        return new JAXBElement<Integer>(_Int_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Long }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/2003/10/Serialization/", name = "long")
    public JAXBElement<Long> createLong(Long value) {
        return new JAXBElement<Long>(_Long_QNAME, Long.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QName }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link QName }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/2003/10/Serialization/", name = "QName")
    public JAXBElement<QName> createQName(QName value) {
        return new JAXBElement<QName>(_QName_QNAME, QName.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Short }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Short }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/2003/10/Serialization/", name = "short")
    public JAXBElement<Short> createShort(Short value) {
        return new JAXBElement<Short>(_Short_QNAME, Short.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/2003/10/Serialization/", name = "string")
    public JAXBElement<String> createString(String value) {
        return new JAXBElement<String>(_String_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Short }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Short }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/2003/10/Serialization/", name = "unsignedByte")
    public JAXBElement<Short> createUnsignedByte(Short value) {
        return new JAXBElement<Short>(_UnsignedByte_QNAME, Short.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Long }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/2003/10/Serialization/", name = "unsignedInt")
    public JAXBElement<Long> createUnsignedInt(Long value) {
        return new JAXBElement<Long>(_UnsignedInt_QNAME, Long.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/2003/10/Serialization/", name = "unsignedLong")
    public JAXBElement<BigInteger> createUnsignedLong(BigInteger value) {
        return new JAXBElement<BigInteger>(_UnsignedLong_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/2003/10/Serialization/", name = "unsignedShort")
    public JAXBElement<Integer> createUnsignedShort(Integer value) {
        return new JAXBElement<Integer>(_UnsignedShort_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/2003/10/Serialization/", name = "char")
    public JAXBElement<Integer> createChar(Integer value) {
        return new JAXBElement<Integer>(_Char_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Duration }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Duration }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/2003/10/Serialization/", name = "duration")
    public JAXBElement<Duration> createDuration(Duration value) {
        return new JAXBElement<Duration>(_Duration_QNAME, Duration.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.microsoft.com/2003/10/Serialization/", name = "guid")
    public JAXBElement<String> createGuid(String value) {
        return new JAXBElement<String>(_Guid_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RequestDataISCIPR }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link RequestDataISCIPR }{@code >}
     */
    @XmlElementDecl(namespace = "http://iscipr.egov.bg", name = "RequestDataISCIPR")
    public JAXBElement<RequestDataISCIPR> createRequestDataISCIPR(RequestDataISCIPR value) {
        return new JAXBElement<RequestDataISCIPR>(_RequestDataISCIPR_QNAME, RequestDataISCIPR.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CallContext }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link CallContext }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", name = "CallContext")
    public JAXBElement<CallContext> createCallContext(CallContext value) {
        return new JAXBElement<CallContext>(_CallContext_QNAME, CallContext.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServiceResultISCIPR }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ServiceResultISCIPR }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", name = "ServiceResultISCIPR")
    public JAXBElement<ServiceResultISCIPR> createServiceResultISCIPR(ServiceResultISCIPR value) {
        return new JAXBElement<ServiceResultISCIPR>(_ServiceResultISCIPR_QNAME, ServiceResultISCIPR.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RequestDataISCIPR }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link RequestDataISCIPR }{@code >}
     */
    @XmlElementDecl(namespace = "http://iscipr.egov.bg/", name = "requestData", scope = RegisterRecordEntry.class)
    public JAXBElement<RequestDataISCIPR> createRegisterRecordEntryRequestData(RequestDataISCIPR value) {
        return new JAXBElement<RequestDataISCIPR>(_RegisterRecordEntryRequestData_QNAME, RequestDataISCIPR.class, RegisterRecordEntry.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServiceResultISCIPR }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ServiceResultISCIPR }{@code >}
     */
    @XmlElementDecl(namespace = "http://iscipr.egov.bg/", name = "RegisterRecordEntryResult", scope = RegisterRecordEntryResponse.class)
    public JAXBElement<ServiceResultISCIPR> createRegisterRecordEntryResponseRegisterRecordEntryResult(ServiceResultISCIPR value) {
        return new JAXBElement<ServiceResultISCIPR>(_RegisterRecordEntryResponseRegisterRecordEntryResult_QNAME, ServiceResultISCIPR.class, RegisterRecordEntryResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RequestDataISCIPR }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link RequestDataISCIPR }{@code >}
     */
    @XmlElementDecl(namespace = "http://iscipr.egov.bg/", name = "requestData", scope = RegisterRecordChange.class)
    public JAXBElement<RequestDataISCIPR> createRegisterRecordChangeRequestData(RequestDataISCIPR value) {
        return new JAXBElement<RequestDataISCIPR>(_RegisterRecordEntryRequestData_QNAME, RequestDataISCIPR.class, RegisterRecordChange.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServiceResultISCIPR }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ServiceResultISCIPR }{@code >}
     */
    @XmlElementDecl(namespace = "http://iscipr.egov.bg/", name = "RegisterRecordChangeResult", scope = RegisterRecordChangeResponse.class)
    public JAXBElement<ServiceResultISCIPR> createRegisterRecordChangeResponseRegisterRecordChangeResult(ServiceResultISCIPR value) {
        return new JAXBElement<ServiceResultISCIPR>(_RegisterRecordChangeResponseRegisterRecordChangeResult_QNAME, ServiceResultISCIPR.class, RegisterRecordChangeResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RequestDataISCIPR }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link RequestDataISCIPR }{@code >}
     */
    @XmlElementDecl(namespace = "http://iscipr.egov.bg/", name = "requestData", scope = RegisterRecordRemove.class)
    public JAXBElement<RequestDataISCIPR> createRegisterRecordRemoveRequestData(RequestDataISCIPR value) {
        return new JAXBElement<RequestDataISCIPR>(_RegisterRecordEntryRequestData_QNAME, RequestDataISCIPR.class, RegisterRecordRemove.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServiceResultISCIPR }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ServiceResultISCIPR }{@code >}
     */
    @XmlElementDecl(namespace = "http://iscipr.egov.bg/", name = "RegisterRecordRemoveResult", scope = RegisterRecordRemoveResponse.class)
    public JAXBElement<ServiceResultISCIPR> createRegisterRecordRemoveResponseRegisterRecordRemoveResult(ServiceResultISCIPR value) {
        return new JAXBElement<ServiceResultISCIPR>(_RegisterRecordRemoveResponseRegisterRecordRemoveResult_QNAME, ServiceResultISCIPR.class, RegisterRecordRemoveResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", name = "AdministrationName", scope = CallContext.class)
    public JAXBElement<String> createCallContextAdministrationName(String value) {
        return new JAXBElement<String>(_CallContextAdministrationName_QNAME, String.class, CallContext.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", name = "AdministrationOId", scope = CallContext.class)
    public JAXBElement<String> createCallContextAdministrationOId(String value) {
        return new JAXBElement<String>(_CallContextAdministrationOId_QNAME, String.class, CallContext.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", name = "EmployeeAdditionalIdentifier", scope = CallContext.class)
    public JAXBElement<String> createCallContextEmployeeAdditionalIdentifier(String value) {
        return new JAXBElement<String>(_CallContextEmployeeAdditionalIdentifier_QNAME, String.class, CallContext.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", name = "EmployeeIdentifier", scope = CallContext.class)
    public JAXBElement<String> createCallContextEmployeeIdentifier(String value) {
        return new JAXBElement<String>(_CallContextEmployeeIdentifier_QNAME, String.class, CallContext.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", name = "EmployeeNames", scope = CallContext.class)
    public JAXBElement<String> createCallContextEmployeeNames(String value) {
        return new JAXBElement<String>(_CallContextEmployeeNames_QNAME, String.class, CallContext.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", name = "EmployeePosition", scope = CallContext.class)
    public JAXBElement<String> createCallContextEmployeePosition(String value) {
        return new JAXBElement<String>(_CallContextEmployeePosition_QNAME, String.class, CallContext.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", name = "LawReason", scope = CallContext.class)
    public JAXBElement<String> createCallContextLawReason(String value) {
        return new JAXBElement<String>(_CallContextLawReason_QNAME, String.class, CallContext.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", name = "Remark", scope = CallContext.class)
    public JAXBElement<String> createCallContextRemark(String value) {
        return new JAXBElement<String>(_CallContextRemark_QNAME, String.class, CallContext.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", name = "ServiceType", scope = CallContext.class)
    public JAXBElement<String> createCallContextServiceType(String value) {
        return new JAXBElement<String>(_CallContextServiceType_QNAME, String.class, CallContext.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", name = "ServiceURI", scope = CallContext.class)
    public JAXBElement<String> createCallContextServiceURI(String value) {
        return new JAXBElement<String>(_CallContextServiceURI_QNAME, String.class, CallContext.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", name = "Data", scope = ServiceResultISCIPR.class)
    public JAXBElement<String> createServiceResultISCIPRData(String value) {
        return new JAXBElement<String>(_ServiceResultISCIPRData_QNAME, String.class, ServiceResultISCIPR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", name = "ErrorCode", scope = ServiceResultISCIPR.class)
    public JAXBElement<String> createServiceResultISCIPRErrorCode(String value) {
        return new JAXBElement<String>(_ServiceResultISCIPRErrorCode_QNAME, String.class, ServiceResultISCIPR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/SmartRegistry.WebApi", name = "ErrorMessage", scope = ServiceResultISCIPR.class)
    public JAXBElement<String> createServiceResultISCIPRErrorMessage(String value) {
        return new JAXBElement<String>(_ServiceResultISCIPRErrorMessage_QNAME, String.class, ServiceResultISCIPR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://iscipr.egov.bg", name = "Argument", scope = RequestDataISCIPR.class)
    public JAXBElement<String> createRequestDataISCIPRArgument(String value) {
        return new JAXBElement<String>(_RequestDataISCIPRArgument_QNAME, String.class, RequestDataISCIPR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CallContext }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link CallContext }{@code >}
     */
    @XmlElementDecl(namespace = "http://iscipr.egov.bg", name = "CallContext", scope = RequestDataISCIPR.class)
    public JAXBElement<CallContext> createRequestDataISCIPRCallContext(CallContext value) {
        return new JAXBElement<CallContext>(_RequestDataISCIPRCallContext_QNAME, CallContext.class, RequestDataISCIPR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://iscipr.egov.bg", name = "CitizenEGN", scope = RequestDataISCIPR.class)
    public JAXBElement<String> createRequestDataISCIPRCitizenEGN(String value) {
        return new JAXBElement<String>(_RequestDataISCIPRCitizenEGN_QNAME, String.class, RequestDataISCIPR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://iscipr.egov.bg", name = "EmployeeEGN", scope = RequestDataISCIPR.class)
    public JAXBElement<String> createRequestDataISCIPREmployeeEGN(String value) {
        return new JAXBElement<String>(_RequestDataISCIPREmployeeEGN_QNAME, String.class, RequestDataISCIPR.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://iscipr.egov.bg", name = "Operation", scope = RequestDataISCIPR.class)
    public JAXBElement<String> createRequestDataISCIPROperation(String value) {
        return new JAXBElement<String>(_RequestDataISCIPROperation_QNAME, String.class, RequestDataISCIPR.class, value);
    }

}
