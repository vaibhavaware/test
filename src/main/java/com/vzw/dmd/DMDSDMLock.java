/*
 * Created on Jul 7, 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.vzw.dmd.dao.LteDAO;
import com.vzw.dmd.dao.OracleDAOFactory;
import com.vzw.dmd.dao.OracleLteDAO;
import com.vzw.dmd.util.DMDConstants;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.DMDUtils;
import com.vzw.dmd.util.XXEDisabler;
import com.vzw.dmd.util.XmlUtils;


/**
 * @author c0gaddv
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DMDSDMLock extends HttpServlet {
	private static Logger L =
		Logger.getLogger(DMDLogs.getLogName(DMDSDMLock.class));
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
		StringBuffer statsLogBuf = new StringBuffer("XMLNEW|");
		Date entryTime = new Date();			
		Document inpDoc = null;
		
		StringBuffer returnXML=new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?><dmd><status><status_code>");
		boolean errorFlag=false;

		try{
			//Fortify Fix - XML External Entity Injection
			DocumentBuilderFactory df =
					new XXEDisabler().disableDBF(DocumentBuilderFactory.newInstance());
			DocumentBuilder db =df.newDocumentBuilder();
    		try
    		{
    		    inpDoc = db.parse(new InputSource(new StringReader(req.getParameter("message"))));
    		}
    		catch (Exception e)
    		{
    		    L.error ("Error while parsing input xml.", e);
    		    statsLogBuf.append("NONE|")
    		    		.append(DMDUtils.getClientIP(req))
    		    		.append("NONE|SDM_LOCK|FALSE|Invalid XML. Failed to parse.");
				returnXML.append(DMDConstants.DMD_ERROR)
						.append("</status_code>")
						.append("<message>")
						.append("Invalid input XML")
						.append("</message>")
						.append("</status></dmd>");
				errorFlag = true;
    		}
    		if(!errorFlag){
	    		String client_id = XmlUtils.getValue(inpDoc, "dmd_request/client_id");
	    		String user_id = XmlUtils.getValue(inpDoc, "dmd_request/user_id");
	    		String password = XmlUtils.getValue(inpDoc, "dmd_request/password");
				String device_id = XmlUtils.getValue(inpDoc, "dmd_request/sdm_lock_request/device_id");			
				String sim_id = XmlUtils.getValue(inpDoc, "dmd_request/sdm_lock_request/sim_id");
				String request_id = XmlUtils.getValue(inpDoc, "dmd_request/sdm_lock_request/request_id");
				String lock_status = XmlUtils.getValue(inpDoc, "dmd_request/sdm_lock_request/lock_status");
				String load_date = XmlUtils.getValue(inpDoc, "dmd_request/sdm_lock_request/load_date");
				String statusCode = null;
				if(device_id ==null || device_id.trim().length()==0)
					device_id=sim_id;
				String deviceType=DMDUtils.getDeviceIDType(device_id);
				if(DMDUtils.authenticateUsers(client_id, user_id, password)){
					if(deviceType != null){
					//send to update
						OracleDAOFactory oracleDAOFactory = OracleDAOFactory.getInstance();
						LteDAO daoLte = (OracleLteDAO)oracleDAOFactory.getLteDAO();				
						statusCode= daoLte.updateSDMLockStatus(device_id, deviceType, request_id, lock_status, load_date, client_id, user_id, DMDUtils.getClientIP(req));
						if(statusCode == DMDConstants.DMD_SUCCESS){
							statsLogBuf.append(client_id)
							.append("|")					
							.append(DMDUtils.getClientIP(req))				
							.append("|UPDATE|")
							.append("SDM_LOCK|")
							.append(device_id)
							.append("|")			
							.append("TRUE");
							
							returnXML.append(DMDConstants.DMD_SUCCESS)
							.append("</status_code>")
							.append("<message/>")						
							.append("</status></dmd>");
						}else if(statusCode == DMDConstants.DMD_NOT_FOUND){
							statsLogBuf.append(client_id)
							.append("|")					
							.append(DMDUtils.getClientIP(req))
							.append("|UPDATE|")
							.append("SDM_LOCK|")
							.append(device_id)
							.append("|")			
							.append("FALSE|Device ID not found");
							
							returnXML.append(DMDConstants.DMD_NOT_FOUND)
							.append("</status_code>")
							.append("<message>")
							.append("Device ID not found in DMD")
							.append("</message>")
							.append("</status></dmd>");
						}else if(statusCode == DMDConstants.DMD_INVALID_DATE__FORMAT){
							statsLogBuf.append(client_id)
							.append("|")					
							.append(DMDUtils.getClientIP(req))				
							.append("|UPDATE|")
							.append("SDM_LOCK|")
							.append(device_id)
							.append("|")			
							.append("FALSE|Invalid Date Format");
							
							returnXML.append(DMDConstants.DMD_INVALID_DATE__FORMAT)
							.append("</status_code>")
							.append("<message>")
							.append("Invalid Date Format")
							.append("</message>")
							.append("</status></dmd>");
						}else{
							statsLogBuf.append(client_id)
							.append("|")					
							.append(DMDUtils.getClientIP(req))				
							.append("|UPDATE|")
							.append("SDM_LOCK|")
							.append(device_id)
							.append("|")			
							.append("FALSE|Application Error");
							
							returnXML.append(DMDConstants.DMD_ERROR)
							.append("</status_code>")
							.append("<message>")
							.append("Application Error")
							.append("</message>")
							.append("</status></dmd>");
						}
					}else{
						statsLogBuf.append(client_id)
						.append("|")					
						.append(DMDUtils.getClientIP(req))				
						.append("|UPDATE|")
						.append("SDM_LOCK|")
						.append(device_id)
						.append("|")			
						.append("FALSE|Invalid Device ID");
						
						returnXML.append(DMDConstants.DMD_INVALID_DEVICE_ID)
						.append("</status_code>")
						.append("<message>")
						.append("Invalid Device ID format.")
						.append("</message>")
						.append("</status></dmd>");
					}
				}else{
					statsLogBuf.append(client_id)
					.append("|")					
					.append(DMDUtils.getClientIP(req))				
					.append("|UPDATE|")
					.append("SDM_LOCK|")
					.append(device_id)
					.append("|")			
					.append("FALSE|Authentication failed.");
					
					returnXML.append(DMDConstants.DMD_AUTHENTICATION_ERROR)
					.append("</status_code>")
					.append("<message>")
					.append("Authentication failed.")
					.append("</message>")
					.append("</status></dmd>");
				}
    		}
		}catch (Exception e){
			statsLogBuf.append("NONE|NONE|UPDATE|SDM_LOCK|FALSE|Application ERROR");
			returnXML.append(DMDConstants.DMD_ERROR)
			.append("</status_code>")
			.append("<message>")
			.append("Application error.")
			.append("</message>")
			.append("</status></dmd>");			
		}finally{
			Date exitTime = new Date();
			DMDLogs.getStatsLogger().info(statsLogBuf.toString());
			long prcTime = exitTime.getTime() - entryTime.getTime();
			DMDLogs.getEStatsLogger().info(
                    statsLogBuf.toString() + "|"
                            + DMDProps.ldf.format(entryTime) + "|"
                            + DMDProps.ldf.format(exitTime) + "|" + prcTime);
		}
		/*OutputFormat resFmt = new OutputFormat("xml", "UTF-8", true);
		resFmt.setPreserveEmptyAttributes(true);
		//resFmt.setPreserveSpace(true);
		resFmt.setIndent(3);
		String resXmlStr = XmlUtils.prettyFormat(returnXML.toString());
		resXmlStr = resXmlStr.replaceAll("!-!-!", "     ");
		*/
		try{
			/*Document resDoc =
				db.parse(
					new InputSource(
						new StringReader(returnXML.toString())));
			StringWriter resWri = new StringWriter();
			
			// Output the document with indentation
			OutputFormat resFmt = new OutputFormat("xml", "UTF-8", true);
			resFmt.setPreserveEmptyAttributes(true);
			//resFmt.setPreserveSpace(true);
			resFmt.setIndent(3);
			// Create a new Serializer
			XMLSerializer ser =
				new XMLSerializer(
					resWri,
					resFmt);
					
			// Serialize the document
			ser.serialize(resDoc);
			
//			 Get pretty formatted XML String
			String resXmlStr = XmlUtils.prettyFormat(resWri.toString());
			resXmlStr = resXmlStr.replaceAll("!-!-!", "     ");
			*/
			ServletOutputStream out = res.getOutputStream();
			res.setContentType("text/xml");		
			out.println(returnXML.toString());
			out.flush();
			out.close();
		}catch(Exception e){
			
		}
	}	
}
