package com.vzw.dmd;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.vzw.dmd.dao.DeviceLookupDAO;
import com.vzw.dmd.util.DBUtils;
import com.vzw.dmd.util.DMDConstants;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.XmlUtils;
import com.vzw.dmd.util.ILteXmlCreator;
import com.vzw.dmd.util.LteXmlCreator;
import com.vzw.dmd.util.XXEDisabler;
import com.vzw.dmd.util.DMDUtils;

public class DMDEUICCPairAddUpdate extends HttpServlet implements ILteXmlCreator {
private static Logger L = Logger.getLogger(DMDLogs.getLogName(DMDEUICCPairAddUpdate.class));
	

	/**
	* @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	*/
	public void doGet(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException {
		//L.debug("doGet()");
		
		String xmlReqt = req.getParameter("xmlReq");
		if (xmlReqt == null || xmlReqt.length() == 0) {
			xmlReqt = req.getParameter("xmlreqdoc");
		}

		defaultAction(req, res, xmlReqt);
	}

	/**
	* @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	*/
	public void doPost(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException {
		//L.debug("doPost()");

		String xmlReqt = req.getParameter("xmlReq");
		if (xmlReqt == null || xmlReqt.length() == 0) {
			xmlReqt = req.getParameter("xmlreqdoc");
		}

		defaultAction(req, res, xmlReqt); 
	}
	/**
	* @see javax.servlet.GenericServlet#void ()
	*/
	public void init() throws ServletException {
		super.init();
	}
	public void defaultAction(HttpServletRequest req, HttpServletResponse res, String xmlReqt)
	throws ServletException, IOException {
		StringBuffer statsLogBuf = new StringBuffer(DMDConstants.XML_NEW);
		statsLogBuf.append(DMDConstants.DMD_PIPE);
		Date entryTime = new Date();
		res.setContentType("text/xml");
		int statusCode ;
		ServletOutputStream out = res.getOutputStream();
		try{
			String xmlReq = req.getParameter("xmlReq");					
			L.debug("The request parameter is"+req.getParameter("xmlReq"));
			
			if(xmlReq==null || xmlReq.trim().length()==0){
				xmlReq = req.getParameter("xmlreqdoc");
			}
			if(xmlReq != null) {
				xmlReq = xmlReq.trim();
			}
			L.debug("The request xml  is :"+xmlReq);
			
			//Fortify Fix - XML External Entity Injection
			DocumentBuilderFactory docBuilderFactory = new XXEDisabler().disableDBF(DocumentBuilderFactory.newInstance());
			Document document = docBuilderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(xmlReq)));
			statsLogBuf.append("REQ_TYPE=" + XmlUtils.getValue(document, "/dmd/requestHeader/req_type")).append(DMDConstants.DMD_PIPE);
			statsLogBuf.append("TRANSACTION_ID=" + XmlUtils.getValue(document, "/dmd/requestHeader/requestId")).append(DMDConstants.DMD_PIPE);
			statsLogBuf.append("CLIENT_ID=" + XmlUtils.getValue(document, "/dmd/clientId")).append(DMDConstants.DMD_PIPE);
			statsLogBuf.append(DMDConstants.DMD_EPS_ELIGIBILITY_STATUS_API);
			statsLogBuf.append(DMDConstants.DMD_PIPE);
			statsLogBuf.append(getLoggerData(XmlUtils.getValue(document, "/dmd/clientId"), "", req));
		
		String deviceId = XmlUtils.getValue(document, "/dmd/requestBody/imeiId");
		L.debug("The device id  is :"+deviceId);
		String simId = 	XmlUtils.getValue(document, "/dmd/requestBody/iccId");
		L.debug("The sim id  is :"+simId);
		String eId = 	XmlUtils.getValue(document, "/dmd/requestBody/eId");
		L.debug("The sim id  is :"+eId);
		String personalizationStatus = 	XmlUtils.getValue(document, "/dmd/requestBody/personalizationStatus");
		L.debug("The sim id  is :"+personalizationStatus);
		String clientId = 	XmlUtils.getValue(document, "/dmd/requestBody/clientId");
		L.debug("The sim id  is :"+clientId);
		L.debug("*********");
		if (deviceId.trim().equals(""))
			deviceId=null;
		if (simId.trim().equals(""))
			simId =null;
		if (eId.trim().equals(""))
			eId=null;
		if(personalizationStatus.trim().equals(""))
			personalizationStatus= null;
		if (clientId.trim().equals(""))
			clientId = null;
		L.debug("");
		L.debug("device id -"+deviceId);
		if((deviceId == null ||deviceId.trim().equals("") ) && (simId ==null ||simId.trim().equals("")) && (eId ==null ||eId.trim().equals(""))&& (personalizationStatus ==null ||personalizationStatus.trim().equals("")) && (clientId ==null ||clientId.trim().equals("")) ) {
			L.debug(" IF CONDITION*********");
			StringBuffer returnXML=new StringBuffer(XML_RESPONSE_START);
			//add header. status and message
			returnXML.append("-1")
						.append(STATUS_CODE_END)
						.append(MESSAGE_START)
						.append("Input device,sim Id, eId, personalizationStatus, clientId should not empty.")
						.append(MESSAGE_END)
						.append(RESPONSE_HEADER_END);
			//add body					
			returnXML.append(RESPONSE_BODY_START);			
			returnXML.append(RESPONSE_BODY_END).append(DMD_END);
			out.println(returnXML.toString());
			out.flush();
			out.close();
			return;
		}
		L.debug("BEFORE CALLING addOrUpdateEidIccidPair*******");
		statusCode = DeviceLookupDAO.addOrUpdateEidIccidPair(deviceId,simId,eId,personalizationStatus,clientId);
		L.debug("after calling  addOrUpdateEidIccidPair*********");
		L.debug("statusCode-"+statusCode);
		
		if (statusCode==0){
			StringBuffer sbReturnXML=new StringBuffer(XML_RESPONSE_START);
			sbReturnXML.append("00")
			.append(STATUS_CODE_END)
			.append(MESSAGE_START)
			.append("SUCCESS")
			.append(MESSAGE_END)
			.append(RESPONSE_HEADER_END)
			.append(DMD_END);
			out.println(sbReturnXML.toString());
		}
		else {
			StringBuffer sbReturnXML=new StringBuffer(XML_RESPONSE_START);
			sbReturnXML.append("-2")
			.append(STATUS_CODE_END)
			.append(MESSAGE_START)
			.append("Application error .")
			.append(MESSAGE_END)
			.append(RESPONSE_HEADER_END)
			.append(DMD_END);
			out.println(sbReturnXML.toString());
		}
		
		//feb 2019 BQVT-964
		//send input request to new dmd write queue for cassandra processing
		//need to be handled via on-off toggle
		String serviceEnabled = DBUtils.getDBPropertyValue("DMDEUICCPAIRCASSSERVICE");
		L.debug("DMDEUICCPAIRCASSSERVICE serviceEnabled-"+serviceEnabled);
		L.info("DMDEUICCPairAddUpdate.defaultAction() cass flow serviceEnabled--"+serviceEnabled);
		//add service name to the xml
		if("Y".equalsIgnoreCase(serviceEnabled))
		{
			
			String messageText =  "<xmlReq><apiName>DMDEUICCPairAddUpdate</apiName><dmdXmlReq><![CDATA["+xmlReq+"]]></dmdXmlReq></xmlReq>";
			L.info("DMDEUICCPairAddUpdate.defaultAction() messageText--"+messageText);
			DMDMqClient mqClient = new DMDMqClient();
			mqClient.sendMessageToQueue(messageText);	
			L.info("DMDEUICCPairAddUpdate.defaultAction() cass flow done");
			L.debug("DMDEUICCPairAddUpdate.defaultAction() cass flow done");
		}
		
		
		out.flush();
		out.close();
		statsLogBuf.append(deviceId)
		.append(DMDConstants.DMD_PIPE);
}catch (Exception e){
	statsLogBuf.append(DMDConstants.STATUS_MESSAGE_ERROR);
	StringBuffer sbReturnXML=new StringBuffer(XML_RESPONSE_START);
	sbReturnXML.append("-1")
	.append(STATUS_CODE_END)
	.append(MESSAGE_START)
	.append("Application error .")
	.append(MESSAGE_END)
	.append(RESPONSE_HEADER_END)
	.append(DMD_END);
	out.println(sbReturnXML.toString());
}finally{
	Date exitTime = new Date();
	DMDLogs.getStatsLogger().info(statsLogBuf.toString());
	long prcTime = exitTime.getTime() - entryTime.getTime();
	DMDLogs.getEStatsLogger().info(
            statsLogBuf.toString() + DMDConstants.DMD_PIPE                    
                    + DMDProps.ldf.format(entryTime) + DMDConstants.DMD_PIPE
                    + DMDProps.ldf.format(exitTime) + DMDConstants.DMD_PIPE + prcTime);
}
	}
	
private StringBuffer getLoggerData(String appType,String statusMessage,HttpServletRequest req){
		
		StringBuffer statsLogBuf = new StringBuffer();
		
		 statsLogBuf.append(appType)
		.append(DMDConstants.DMD_PIPE)
		.append(DMDUtils.getClientIP(req))
		.append(DMDConstants.DMD_PIPE)
		.append(DMDConstants.LOOKUP)
		.append(DMDConstants.DMD_PIPE)
		.append(DMDConstants.DMD_EPS_ELIGIBILITY_STATUS_API)
		.append(DMDConstants.DMD_PIPE)	 
		.append(DMDConstants.DMD_TRUE)
		.append(DMDConstants.DMD_PIPE)
		.append(statusMessage);	
		 
		 return statsLogBuf;
	}
}
