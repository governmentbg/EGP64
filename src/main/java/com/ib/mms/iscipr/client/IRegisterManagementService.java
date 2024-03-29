package com.ib.mms.iscipr.client;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.Action;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 3.4.5
 * 2022-10-19T16:06:20.237+03:00
 * Generated source version: 3.4.5
 *
 */
@WebService(targetNamespace = "http://iscipr.egov.bg/", name = "IRegisterManagementService")
@XmlSeeAlso({ObjectFactory.class})
public interface IRegisterManagementService {

    @WebMethod(operationName = "RegisterRecordChange", action = "http://iscipr.egov.bg/IRegisterManagementService/RegisterRecordChange")
    @Action(input = "http://iscipr.egov.bg/IRegisterManagementService/RegisterRecordChange", output = "http://iscipr.egov.bg/IRegisterManagementService/RegisterRecordChangeResponse")
    @RequestWrapper(localName = "RegisterRecordChange", targetNamespace = "http://iscipr.egov.bg/", className = "com.ib.mms.iscipr.client.RegisterRecordChange")
    @ResponseWrapper(localName = "RegisterRecordChangeResponse", targetNamespace = "http://iscipr.egov.bg/", className = "com.ib.mms.iscipr.client.RegisterRecordChangeResponse")
    @WebResult(name = "RegisterRecordChangeResult", targetNamespace = "http://iscipr.egov.bg/")
    public com.ib.mms.iscipr.client.ServiceResultISCIPR registerRecordChange(

        @WebParam(name = "requestData", targetNamespace = "http://iscipr.egov.bg/")
        com.ib.mms.iscipr.client.RequestDataISCIPR requestData
    );

    @WebMethod(operationName = "RegisterRecordRemove", action = "http://iscipr.egov.bg/IRegisterManagementService/RegisterRecordRemove")
    @Action(input = "http://iscipr.egov.bg/IRegisterManagementService/RegisterRecordRemove", output = "http://iscipr.egov.bg/IRegisterManagementService/RegisterRecordRemoveResponse")
    @RequestWrapper(localName = "RegisterRecordRemove", targetNamespace = "http://iscipr.egov.bg/", className = "com.ib.mms.iscipr.client.RegisterRecordRemove")
    @ResponseWrapper(localName = "RegisterRecordRemoveResponse", targetNamespace = "http://iscipr.egov.bg/", className = "com.ib.mms.iscipr.client.RegisterRecordRemoveResponse")
    @WebResult(name = "RegisterRecordRemoveResult", targetNamespace = "http://iscipr.egov.bg/")
    public com.ib.mms.iscipr.client.ServiceResultISCIPR registerRecordRemove(

        @WebParam(name = "requestData", targetNamespace = "http://iscipr.egov.bg/")
        com.ib.mms.iscipr.client.RequestDataISCIPR requestData
    );

    @WebMethod(operationName = "RegisterRecordEntry", action = "http://iscipr.egov.bg/IRegisterManagementService/RegisterRecordEntry")
    @Action(input = "http://iscipr.egov.bg/IRegisterManagementService/RegisterRecordEntry", output = "http://iscipr.egov.bg/IRegisterManagementService/RegisterRecordEntryResponse")
    @RequestWrapper(localName = "RegisterRecordEntry", targetNamespace = "http://iscipr.egov.bg/", className = "com.ib.mms.iscipr.client.RegisterRecordEntry")
    @ResponseWrapper(localName = "RegisterRecordEntryResponse", targetNamespace = "http://iscipr.egov.bg/", className = "com.ib.mms.iscipr.client.RegisterRecordEntryResponse")
    @WebResult(name = "RegisterRecordEntryResult", targetNamespace = "http://iscipr.egov.bg/")
    public com.ib.mms.iscipr.client.ServiceResultISCIPR registerRecordEntry(

        @WebParam(name = "requestData", targetNamespace = "http://iscipr.egov.bg/")
        com.ib.mms.iscipr.client.RequestDataISCIPR requestData
    );
}
