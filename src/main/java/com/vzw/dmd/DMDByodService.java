package com.vzw.dmd;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.UUID;


import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.vzw.dmd.dao.BYODDAO;
import com.vzw.dmd.dao.DeviceLookupDAO;
import com.vzw.dmd.util.DBUtils;
import com.vzw.dmd.util.DMDConstants;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.ILteXmlCreator;
import com.vzw.dmd.util.XXEDisabler;
import com.vzw.dmd.util.XmlUtils;
import com.vzw.dmd.valueobject.BYODRequestVO;
import com.vzw.dmd.valueobject.BYODResponseVO;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import com.ibm.mq.jms.MQQueue;
import com.ibm.mq.jms.MQQueueConnection;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.mq.jms.MQQueueSender;
import com.ibm.mq.jms.MQQueueSession;
import com.ibm.msg.client.wmq.WMQConstants;

/*import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;*/


/**
 * @author vaidya
 *
 */
public class DMDByodService extends HttpServlet implements ILteXmlCreator {
	private static Logger L =
			Logger.getLogger(DMDLogs.getLogName(DMDByodService.class));
	/**
	 * @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		defaultAction(req, res);
	}

	/**
	 * @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		defaultAction(req, res);
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
	public void defaultAction(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		StringBuffer statsLogBuf = new StringBuffer(DMDConstants.XML_NEW);
		statsLogBuf.append(DMDConstants.DMD_PIPE);
		Date entryTime = new Date();
		String returnXML = null;
		ServletOutputStream out = res.getOutputStream();
		BYODRequestVO request = new BYODRequestVO();
		try{
			String xmlReq = req.getParameter("xmlReq");
			L.debug("The request parameter is"+req.getParameter("xmlReq"));

			if(xmlReq==null || xmlReq.trim().length()==0){
				xmlReq = req.getParameter("xmlreqdoc");
			}
			if(xmlReq != null) {
				xmlReq = xmlReq.trim();
			}
			L.info("DMDByodService request xml is :"+xmlReq);

			//Fortify Fix - XML External Entity Injection
			DocumentBuilderFactory docBuilderFactory = new XXEDisabler().disableDBF(DocumentBuilderFactory.newInstance());
			Document document = docBuilderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(xmlReq)));
			
			request.setReqType(XmlUtils.getValue(document, "/dmd_request/request/req_type"));
			request.setReqId(XmlUtils.getValue(document, "/dmd_request/request/req_id"));
			request.setDeviceId(XmlUtils.getValue(document, "/dmd_request/id/deviceID"));
			request.setDeviceDetail(XmlUtils.getValue(document, "/dmd_request/data/deviceDetail"));	
			//BQVT-406 byod enhancement for DSDS
			request.setImei1(XmlUtils.getValue(document, "/dmd_request/data/imei1"));
			request.setImei2(XmlUtils.getValue(document, "/dmd_request/data/imei2"));
			request.setSn(XmlUtils.getValue(document, "/dmd_request/data/sn"));
			request.setEid(XmlUtils.getValue(document, "/dmd_request/data/eid"));
			
			
			statsLogBuf.append("REQUEST_TYPE=" + XmlUtils.getValue(document, "/dmd_request/request/req_type")).append(DMDConstants.DMD_PIPE);
			statsLogBuf.append("TRANSACTION_ID=" + XmlUtils.getValue(document, "/dmd_request/request/req_id")).append(DMDConstants.DMD_PIPE);
			statsLogBuf.append("Service_Name=" + "DMDByod").append(DMDConstants.DMD_PIPE);

			if( isEmpty(request.getDeviceId()) || isEmpty(request.getDeviceDetail())) {
				throw new Exception("Invalid BYOD request");
			}
			else{
				BYODResponseVO response = BYODDAO.writeByodData(request);			
				/*
				0 Success 									(1 from back end)
				1 Application Error 						(except 0/1/2 from back end)
				2 Invalid XML request						(in case of any java exception)
				3 Device Name can not be found in DMD 		(0 from back end)
				3 IMEI2 or EID can not be null in request 	(2 from back end)
				 */
				if (response != null) {
					L.debug("DB response Not Null :"+response);
					returnXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><dmd_response><request><req_type>Open Device Detail</req_type><req_id>"+request.getReqId()+"</req_id></request><status>";					
					if ("1".equals(response.getDeviceFound()) && (response.getErrorIds() == null || response.getErrorIds().length() == 0)) {
						returnXML += "<status_code>0</status_code><status_message>Success</status_message>"; 
					}
					else if ("0".equals(response.getDeviceFound()) && (response.getErrorIds() == null || response.getErrorIds().length() == 0)) {
						returnXML += "<status_code>3</status_code><status_message>Device Name can not be found in DMD</status_message>"; 
					}
					else if ("2".equals(response.getDeviceFound()) && (response.getErrorIds() == null || response.getErrorIds().length() == 0)) {
						returnXML += "<status_code>3</status_code><status_message>IMEI2 or EID can not be null in request</status_message>"; 
					}
					else {
						returnXML += "<status_code>1</status_code><status_message>Application Error</status_message>";
					}
					returnXML += "</status></dmd_response>";					
				}
				else {
					L.debug("DB response is Null :"+response);					
					returnXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><dmd_response><request><req_type>Open Device Detail</req_type><req_id>"+request.getReqId()+"</req_id></request><status><status_code>1</status_code><status_message>Application Error</status_message></status></dmd_response>";
				}
			}	
			
			//feb 2019 BQVT-962
			//send input request to new dmd write queue for cassandra processing
			//need to be handled via on-off toggle
			String serviceEnabled = DBUtils.getDBPropertyValue("DMDBYODCASSSERVICE");
			L.info("DMDByodService.defaultAction() cass flow serviceEnabled--"+serviceEnabled);
			//add service name to the xml
			if("Y".equalsIgnoreCase(serviceEnabled))
			{
				String messageText =  "<xmlReq><apiName>DMDByodService</apiName><dmdXmlReq><![CDATA["+xmlReq+"]]></dmdXmlReq></xmlReq>";
				L.info("DMDByodService.defaultAction() messageText--"+messageText);
				DMDMqClient mqClient = new DMDMqClient();
				mqClient.sendMessageToQueue(messageText);	
				L.info("DMDByodService.defaultAction() done with cass flow");
			
			}
			
		}catch (Exception e){
			L.error("Unable to process request.", e);
			statsLogBuf.append("STATUS=Failure");
			statsLogBuf.append(DMDConstants.DMD_PIPE);			
			returnXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><dmd_response><request><req_type>Open Device Detail</req_type><req_id>"+request.getReqId()+"</req_id></request><status><status_code>2</status_code><status_message>Invalid XML request</status_message></status></dmd_response>";
		}finally{
			Date exitTime = new Date();
			DMDLogs.getStatsLogger().info(statsLogBuf.toString());
			long prcTime = exitTime.getTime() - entryTime.getTime();
			DMDLogs.getEStatsLogger().info(
					statsLogBuf.toString() + DMDConstants.DMD_PIPE                    
					+ DMDProps.ldf.format(entryTime) + DMDConstants.DMD_PIPE
					+ DMDProps.ldf.format(exitTime) + DMDConstants.DMD_PIPE + prcTime);
			L.info("DMDByodService return xml is :"+returnXML);
		}		
		res.setContentType("text/xml");
		out.println(returnXML);
		out.flush();
		out.close();
	}
	
	private boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}	
	
	
	
	/*public static void main(String[] args){
		
		
		//String messageText =  "<SimOtaProgrammingResult><RequestId>050220192</RequestId><Iccid>89148000002916050202</Iccid><Msisdn>15163181930</Msisdn><Status>Success</Status><DateProgrammed>2019-02-04T16:38:31</DateProgrammed><TemplateList><TemplateName>GTO_FP_PRL_TEST_052</TemplateName></TemplateList></SimOtaProgrammingResult>";
		String xmlReq ="<dmd><requestHeader><appType>MyVerizonMobile</appType><requestId></requestId></requestHeader><clientId></clientId><serviceName>DMDEUICCPairAddUpdate</serviceName><subserviceName></subserviceName><requestBody><clientId>VZW-MVM-RPS</clientId><iccId>89148000004325499559</iccId><personalizationStatus>P</personalizationStatus><imeiId></imeiId><eId>89049032003008882300002010958017</eId></requestBody></dmd>";
		String msgTxt = "<xmlReq><apiName>DMDEUICCPairAddUpdate</apiName><dmdXmlReq><![CDATA["+xmlReq+"]]></dmdXmlReq></xmlReq>";
		DMDMqClient mqClient = new DMDMqClient();
		mqClient.testSendMessageQueue(msgTxt);
		
	}*/
	
}
