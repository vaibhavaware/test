/*
 * Created on Jan 30, 2012
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.vzw.dmd;

import java.io.IOException;
import java.util.Date;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

import com.vzw.dmd.util.*;
import com.vzw.dmd.valueobject.UniversalVO;

import com.vzw.dmd.dao.DeviceLookupDAO;

/**
 * @author c0palk1
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
@SuppressWarnings("serial")
public class DMDConsolidatedAPI extends HttpServlet implements ILteXmlCreator {
	private static Logger L = Logger.getLogger(DMDLogs.getLogName(DMDConsolidatedAPI.class));
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
	* @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	*/
	public void defaultAction(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		StringBuffer statsLogBuf = new StringBuffer("DMD_CONSOLIDATED_API");
		statsLogBuf.append(DMDConstants.DMD_PIPE);
		Date entryTime = new Date();
		
		UniversalVO universalVO = new UniversalVO(); 
		universalVO.setFromMq(true);
		ServletOutputStream out = res.getOutputStream();
		String returnXML = null;
		try{
			String deviceSku = req.getParameter("deviceSku");
			if(deviceSku == null) {deviceSku = "";}
			deviceSku = deviceSku.trim();
			
			String accessorySku = req.getParameter("accessorySku");
			if(accessorySku == null) {accessorySku = "";}
			accessorySku = accessorySku.trim();
			
			String deviceImagesUrls = req.getParameter("deviceImagesUrls");
			if(deviceImagesUrls == null) {deviceImagesUrls = "";}
			deviceImagesUrls = deviceImagesUrls.trim();
			
			String accImagesUrls = req.getParameter("accImagesUrls");
			if(accImagesUrls == null) {accImagesUrls = "";}
			accImagesUrls = accImagesUrls.trim();
			
			if(!deviceSku.equals("")) {
				statsLogBuf.append("DEVICE_SKU=" + deviceSku);
				returnXML = DeviceLookupDAO.retriveConsolidatedApiXml(deviceSku);
			} else if(!accessorySku.equals("")){
				statsLogBuf.append("ACCESSORY_SKU=" + accessorySku);
				returnXML = DeviceLookupDAO.retriveAccessoryInfoXml(accessorySku);
			} else if("Y".equalsIgnoreCase(deviceImagesUrls)){
				statsLogBuf.append("DEVICE_SKU=ALL_DEVICE_SKU_IMAGES");
				returnXML = DeviceLookupDAO.retriveConsolidatedApiXml("ALL_DEVICE_SKU_IMAGES");
			} else if("Y".equalsIgnoreCase(accImagesUrls)){
				statsLogBuf.append("ACCESSORY_SKU=ALL_ACCESSORY_SKU_IMAGES");
				returnXML = DeviceLookupDAO.retriveConsolidatedApiXml("ALL_ACCESSORY_SKU_IMAGES");
			} else {
				throw new Exception("Invalid Input.");
			}
			
			returnXML = createConsolidatedXml("00", "SUCCESS", returnXML);
			statsLogBuf.append("STATUS=Success").append(DMDConstants.DMD_PIPE);
		} catch (Exception e) {
			L.error("Unable to process request.", e);
			
			statsLogBuf.append("STATUS=Failure");
			statsLogBuf.append(DMDConstants.DMD_PIPE);
			
			returnXML = createConsolidatedXml("-1", "Invalid input or device/accessory info not found.", null);
		} finally {
			Date exitTime = new Date();
			DMDLogs.getStatsLogger().info(statsLogBuf.toString());
			long prcTime = exitTime.getTime() - entryTime.getTime();
			DMDLogs.getEStatsLogger().info(
                    statsLogBuf.toString() + DMDConstants.DMD_PIPE                    
                            + DMDProps.ldf.format(entryTime) + DMDConstants.DMD_PIPE
                            + DMDProps.ldf.format(exitTime) + DMDConstants.DMD_PIPE + prcTime);
		}
		//Fortify Fix - Cross-site scripting
		out.println(new XSSEncoder().encodeXML(returnXML));
		out.flush();
		out.close();
	}
	
	private String createConsolidatedXml(String statusCd, String msg, String body) {
		StringBuffer sbReturnXML=new StringBuffer(XML_RESPONSE_START);
		//add header. status and message
		sbReturnXML.append(statusCd)
					.append(STATUS_CODE_END)
					.append(MESSAGE_START)
					.append(msg)
					.append(MESSAGE_END)
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
