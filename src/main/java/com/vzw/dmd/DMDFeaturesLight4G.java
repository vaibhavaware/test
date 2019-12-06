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
import com.vzw.dmd.util.DMDConstants;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.DMDUtils;
import com.vzw.dmd.util.ILteXmlCreator;
import com.vzw.dmd.util.LteXmlCreator;
import com.vzw.dmd.util.XSSEncoder;
import com.vzw.dmd.util.XXEDisabler;
import com.vzw.dmd.util.XmlUtils;
import com.vzw.dmd.valueobject.DeviceSimLightResponseVO;

public class DMDFeaturesLight4G extends HttpServlet implements ILteXmlCreator{
	private static Logger L =
		Logger.getLogger(DMDLogs.getLogName(DMDFeaturesLight4G.class));
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
		res.setContentType("text/xml");
		
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
			statsLogBuf.append(DMDConstants.DMD_4G_LIGHT_API);
			statsLogBuf.append(DMDConstants.DMD_PIPE);
			statsLogBuf.append(DMDUtils.getClientIP(req));
			String deviceId = XmlUtils.getValue(document, "/dmd/requestBody/deviceId");
			L.debug("The device id  is :"+deviceId);
			String simId = 	XmlUtils.getValue(document, "/dmd/requestBody/simId");
			L.debug("The sim id  is :"+simId);
			
			if((deviceId == null ||deviceId.trim().equals("") ) && (simId ==null ||simId.trim().equals("")) ) {
				StringBuffer returnXML=new StringBuffer(XML_RESPONSE_START);
				//add header. status and message
				returnXML.append("-1")
							.append(STATUS_CODE_END)
							.append(MESSAGE_START)
							.append("Input device/sim Id is empty.")
							.append(MESSAGE_END)
							.append(RESPONSE_HEADER_END);
				//add body					
				returnXML.append(RESPONSE_BODY_START);			
				returnXML.append(RESPONSE_BODY_END).append(DMD_END);
				out.println(returnXML.toString());
				out.flush();
				out.close();
			}
												
			DeviceSimLightResponseVO responseObj = DeviceLookupDAO.fetchDeviceSimInfo(deviceId, simId);
			
			String returnXML = LteXmlCreator.createDeviceSimInfoLightXML(responseObj);
			//Fortify Fix - Cross-site scripting
			out.println(new XSSEncoder().encodeXML(returnXML));
			out.flush();
			out.close();
						
		}catch (Exception e){
			statsLogBuf.append(DMDConstants.STATUS_MESSAGE_ERROR);
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
	

}
