/**
 * 
 */
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
import com.vzw.dmd.util.XmlUtils;
import com.vzw.dmd.util.ILteXmlCreator;
import com.vzw.dmd.util.LteXmlCreator;
import com.vzw.dmd.util.XXEDisabler;

/**
 * @author punugve
 *
 */
public class DMDEpsEligibilityStatus extends HttpServlet implements ILteXmlCreator {
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
			String deviceId = XmlUtils.getValue(document, "/dmd/requestBody/deviceId");
			L.debug("The device id  is :"+deviceId);
			statusCode = DeviceLookupDAO.getEpsEligiblityStatus(deviceId);
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
			else if(statusCode==-1){
				StringBuffer sbReturnXML=new StringBuffer(XML_RESPONSE_START);
				sbReturnXML.append("-1")
				.append(STATUS_CODE_END)
				.append(MESSAGE_START)
				.append("Device Id is not an valid IMEI")
				.append(MESSAGE_END)
				.append(RESPONSE_HEADER_END)
				.append(DMD_END);
				out.println(sbReturnXML.toString());
			}else if(statusCode==-2){
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
			out.flush();
			out.close();
			statsLogBuf.append(deviceId)
			.append(DMDConstants.DMD_PIPE);
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
