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
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.vzw.dmd.dao.DeviceLookupDAO;
import com.vzw.dmd.util.DMDConstants;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.ILteXmlCreator;
import com.vzw.dmd.util.LteXmlCreator;
import com.vzw.dmd.util.XSSEncoder;
import com.vzw.dmd.util.XXEDisabler;
import com.vzw.dmd.util.XmlUtils;


public class DMDMultiDeviceFirmwareLookup extends HttpServlet implements ILteXmlCreator {
	private static Logger L =
		Logger.getLogger(DMDLogs.getLogName(DMDMacIDInfo.class));
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
		String deviceInfoXml = null;
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
			
			statsLogBuf.append("CLIENT_ID=" + XmlUtils.getValue(document, "/dmd/clientId")).append(DMDConstants.DMD_PIPE);
			statsLogBuf.append("APP_TYPE=" + XmlUtils.getValue(document, "/dmd/requestHeader/appType")).append(DMDConstants.DMD_PIPE);
			statsLogBuf.append("TRANSACTION_ID=" + XmlUtils.getValue(document, "/dmd/requestHeader/requestId")).append(DMDConstants.DMD_PIPE);
			statsLogBuf.append("Service_Name=" + XmlUtils.getValue(document, "/dmd/serviceName")).append(DMDConstants.DMD_PIPE);
			statsLogBuf.append(DMDConstants.DMD_DEVICE_BULK_FIRMWARE);
			statsLogBuf.append(DMDConstants.DMD_PIPE);
			StringBuffer deviceIdList = new StringBuffer();
			String serviceName = XmlUtils.getValue(document, "/dmd/serviceName");
			int cnt = getDeviceIdList(document, deviceIdList);
									
			if( cnt == 0  ) {
				throw new Exception("Input DeviceId list is empty ");
			}
			if(cnt > 50) {
				throw new Exception("Input DeviceId count is more than 50");
			}else{
				
				 deviceInfoXml = DeviceLookupDAO.fetchDeviceFirmwareInfo(deviceIdList.toString());
				 statsLogBuf.append("STATUS=Success").append(DMDConstants.DMD_PIPE);
				 returnXML = createConsolidatedXml("00", "SUCCESS", deviceInfoXml,serviceName,null);
				 
			}
						
		}catch (Exception e){
			L.error("Unable to process request.", e);	
			statsLogBuf.append("STATUS=Failure");
			statsLogBuf.append(DMDConstants.DMD_PIPE);
			returnXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><dmd><responseHeader><statusCode>02</statusCode><message>DMD Application Error.</message></responseHeader></dmd>";
		}finally{
			Date exitTime = new Date();
			DMDLogs.getStatsLogger().info(statsLogBuf.toString());
			long prcTime = exitTime.getTime() - entryTime.getTime();
			DMDLogs.getEStatsLogger().info(
                    statsLogBuf.toString() + DMDConstants.DMD_PIPE                    
                            + DMDProps.ldf.format(entryTime) + DMDConstants.DMD_PIPE
                            + DMDProps.ldf.format(exitTime) + DMDConstants.DMD_PIPE + prcTime);
		}
		
		res.setContentType("text/xml");
		//Fortify Fix - Cross-site scripting
		out.println(new XSSEncoder().encodeXML(returnXML));
		out.flush();
		out.close();
	}
	private int getDeviceIdList(Document document, StringBuffer deviceIdList) throws Exception {
		NodeList nl = XmlUtils.getMultipleNodes(document, "/dmd/requestBody/deviceIDList/deviceId");
		int size = nl.getLength();
		int cnt = 0;
		for (int i=0; i<size; i++) {
			String str = XmlUtils.nodeToString(nl.item(i));
			if(str != null) {
				str = str.trim();
				if(!str.equals("")) {
					cnt++;
					deviceIdList.append(str).append(",");
				}
			}
		}
	
		return cnt;
	}
	
	private String createConsolidatedXml(String statusCd, String msg, String body,String serviceName,String errorMsg) {
		StringBuffer sbReturnXML=new StringBuffer(XML_RESPONSE_START);
		//add header. status and message
		sbReturnXML.append(statusCd)
					.append(STATUS_CODE_END)
					.append(MESSAGE_START);
					if("firmwareInfo".equalsIgnoreCase(serviceName) && null != errorMsg){
						sbReturnXML.append(errorMsg);
					}
					else{
						sbReturnXML.append(msg);	
					}					
					sbReturnXML.append(MESSAGE_END)
					.append(RESPONSE_HEADER_END);
		//add body					
		sbReturnXML.append(RESPONSE_BODY_START);
		if(body != null) {
			sbReturnXML.append(body);
		}
		sbReturnXML.append(RESPONSE_BODY_END).append(DMD_END);
		
		return sbReturnXML.toString();
		
	}
}
