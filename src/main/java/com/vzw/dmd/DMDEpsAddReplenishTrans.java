package com.vzw.dmd;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;

import com.vzw.dmd.dao.DeviceLookupDAO;
import com.vzw.dmd.util.DBUtils;
import com.vzw.dmd.util.DMDConstants;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.XXEDisabler;
import com.vzw.dmd.util.XmlUtils;

public class DMDEpsAddReplenishTrans extends HttpServlet {

	private static Logger L = Logger.getLogger(DMDLogs.getLogName(DMDEpsAddReplenishTrans.class));
	

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

	/**
	* @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	*/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void defaultAction(HttpServletRequest req, HttpServletResponse res, String xmlReqt)
		throws ServletException, IOException {
		L.debug("DMDEpsAddReplenishTrans : xmlReqt: "+xmlReqt);
		
		StringBuffer statsLogBuf = new StringBuffer(DMDConstants.XML_NEW);
		statsLogBuf.append(DMDConstants.DMD_PIPE);
		Date entryTime = new Date();
		
		//res.setContentType("text/xml");
		ServletOutputStream out = res.getOutputStream();
		String xmlResp = "";
		String statusCode = "";
		String statusMessage = "";
		ArrayList transList = new ArrayList();
    	int count;
    	int recordsProcessed =0;
		String appType = "", requestId = "" ,clientId= "";
		DocumentBuilderFactory df = null;
		Document document = null;
		try {
			//Fortify Fix - XML External Entity Injection
			df = new XXEDisabler().disableDBF(DocumentBuilderFactory.newInstance());
			DocumentBuilder db = df.newDocumentBuilder();
			document = db.parse(new InputSource(new StringReader(xmlReqt)));
			L.debug("document-"+document);
			appType = XmlUtils.getValue(document, DMDConstants.UNIVERSAL_APP_TYPE_QUERY);
			requestId = XmlUtils.getValue(document, DMDConstants.UNIVERSAL_REQUEST_ID_QUERY);
			clientId = XmlUtils.getValue(document, DMDConstants.UNIVERSAL_CLIENT_ID_QUERY);
			NodeIterator nit = XPathAPI.selectNodeIterator(document, "dmd/requestBody/transList/transInfo");
			Element el;
			NodeList nodes;
			int nbrNode = 0;
			while ((el=(Element)nit.nextNode()) != null) {
				nbrNode++;
				String deviceId = null, transId = null , transDate = null , amount= null;
				if ((nodes=el.getElementsByTagName("transId")) != null) {
					if (nodes.getLength() > 0) {
						if (nodes.item(0).getFirstChild() != null) { 
							transId = nodes.item(0).getFirstChild().getNodeValue().trim();
							
						}
					}
				}
				if ((nodes=el.getElementsByTagName("deviceId")) != null) {
					if (nodes.getLength() > 0) {
						if (nodes.item(0).getFirstChild() != null) {
							deviceId = nodes.item(0).getFirstChild().getNodeValue();
							if (deviceId != null) {
								deviceId = deviceId.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
							}
						}
					}
				}
				if ((nodes=el.getElementsByTagName("transDate")) != null) {
					if (nodes.getLength() > 0) {
						if (nodes.item(0).getFirstChild() != null) { 
							transDate = nodes.item(0).getFirstChild().getNodeValue();
							
						}
					}
				}
				if ((nodes=el.getElementsByTagName("amount")) != null) {
					if (nodes.getLength() > 0) {
						if (nodes.item(0).getFirstChild() != null) { 
							amount = nodes.item(0).getFirstChild().getNodeValue();
							
						}
					}
				}
				transList.add(transId + "," + deviceId + "," + transDate + "," + amount +  ",EPS_UI");
			}
			L.debug("transList.size()"+transList.size());
			if (transList.size() > 50){
				xmlResp="<?xml version=\"1.0\"?><dmd><responseHeader><statusCode>02</statusCode><message>Number of Records should not more then 50.</message></responseHeader><responseBody></responseBody></dmd>";
				out.println(xmlResp);
				out.flush();
				out.close();
				
		    }
			else{
				L.debug("before calling getEpsAddReplenishTrans");
				count =	DeviceLookupDAO.getEpsAddReplenishTrans(transList);
				L.debug("after calling getEpsAddReplenishTrans: count-"+count);
				recordsProcessed = transList.size()- count;
				if (count ==0){
					xmlResp="<?xml version=\"1.0\"?><dmd><responseHeader><statusCode>00</statusCode><message>SUCCESS</message></responseHeader><responseBody></responseBody></dmd>";
					out.println(xmlResp);
					out.flush();
					out.close();
				}else{
					xmlResp="<?xml version=\"1.0\"?><dmd><responseHeader><statusCode>01</statusCode><message>Failed to Load "+count+" Records Out of "+transList.size()+" Records."+"</message></responseHeader><responseBody></responseBody></dmd>";
					out.println(xmlResp);
					out.flush();
					out.close();
				}
				
			}
			
				L.debug("xmlResp-"+xmlResp);
			//feb 2019 BQVT-965
			//send input request to new dmd write queue for cassandra processing
			//need to be handled via on-off toggle
			String serviceEnabled = DBUtils.getDBPropertyValue("DMDEPSADDREPLENISHCASSSERVICE");
			L.debug("serviceEnabled-"+serviceEnabled);
			L.info("DMDEpsAddReplenishTrans.defaultAction() cass flow serviceEnabled--"+serviceEnabled);
			//add service name to the xml
			if("Y".equalsIgnoreCase(serviceEnabled))
			{
				String messageText =  "<xmlReq><apiName>DMDEpsAddReplenishTrans</apiName><dmdXmlReq><![CDATA["+xmlReqt+"]]></dmdXmlReq></xmlReq>";
				L.info("DMDEpsAddReplenishTrans.defaultAction() messageText--"+messageText);
				DMDMqClient mqClient = new DMDMqClient();
				mqClient.sendMessageToQueue(messageText);		
				L.info("DMDEpsAddReplenishTrans.defaultAction() cass flow done");
			
			}
			
		} catch (Exception e) {
			statusCode = DMDConstants.STATUS_CODE__INVALID_INPUT;
			xmlResp="<?xml version=\"1.0\"?><dmd><responseHeader><statusCode>01</statusCode><message>FAILED</message></responseHeader><responseBody></responseBody></dmd>";

			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			statusMessage = sw.toString();
			L.error(statusMessage);
	   }finally{
           Date exitTime = new Date();
           DMDLogs.getStatsLogger().info(statsLogBuf.toString());
           long prcTime = exitTime.getTime() - entryTime.getTime();
           DMDLogs.getEStatsLogger().info(
                   statsLogBuf.toString() + DMDConstants.DMD_PIPE + "Totla Records : " +transList.size()+ DMDConstants.DMD_PIPE + "Number of Records Processed : "+recordsProcessed
                       + DMDConstants.DMD_PIPE + DMDProps.ldf.format(entryTime) + DMDConstants.DMD_PIPE
                       + DMDProps.ldf.format(exitTime) + DMDConstants.DMD_PIPE + prcTime);
         }
    }
}
