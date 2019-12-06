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
public class DMDOmniFeed extends HttpServlet implements ILteXmlCreator {
	private static Logger L = Logger.getLogger(DMDLogs.getLogName(DMDOmniFeed.class));
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
		StringBuffer statsLogBuf = new StringBuffer("DMD_OMNI_FEED");
		statsLogBuf.append(DMDConstants.DMD_PIPE);
		Date entryTime = new Date();
		
		UniversalVO universalVO = new UniversalVO(); 
		universalVO.setFromMq(true);
		ServletOutputStream out = res.getOutputStream();
		String returnXML = null;
		try{
			String deviceSku = req.getParameter("deviceSku");
			if(deviceSku == null || deviceSku.trim().equalsIgnoreCase("")) {
				deviceSku = null;
			} else {
				deviceSku = deviceSku.trim();
			}
			
			String deltaOnly = req.getParameter("deltaOnly");
			if(deltaOnly == null || !"N".equalsIgnoreCase(deltaOnly.trim())) {
				deltaOnly = "Y";
			} else {
				deltaOnly = "N";
			}
			
			String effectiveBegTime = req.getParameter("effectiveBegTime");
			if(effectiveBegTime == null || effectiveBegTime.trim().equalsIgnoreCase("")) {
				effectiveBegTime = null;
			} else {
				effectiveBegTime = effectiveBegTime.trim();
			}
			
			String notificationEmails = req.getParameter("notificationEmails");
			if(notificationEmails == null || notificationEmails.trim().equalsIgnoreCase("")) {
				notificationEmails = null;
			} else {
				notificationEmails = notificationEmails.trim();
			}
			
			statsLogBuf.append("DEVICE_SKU="+deviceSku).append("DELTA_ONLY="+deltaOnly);
			statsLogBuf.append("EFFECTIVE_BEG_TIME="+effectiveBegTime).append("NOTIF_EMAILS="+notificationEmails);
			
			DeviceLookupDAO.generateOmniFeed(deviceSku, deltaOnly, effectiveBegTime, notificationEmails);
			
			returnXML = createResponseXml("00", "SUCCESS");
			statsLogBuf.append("|STATUS=Success").append(DMDConstants.DMD_PIPE);
		} catch (Exception e) {
			L.error("Unable to process request.", e);
			
			statsLogBuf.append("|STATUS=Failure");
			statsLogBuf.append(DMDConstants.DMD_PIPE);
			
			returnXML = createResponseXml("-1", "FAILED(" + e.getMessage() + ")");
		} finally {
			Date exitTime = new Date();
			DMDLogs.getStatsLogger().info(statsLogBuf.toString());
			long prcTime = exitTime.getTime() - entryTime.getTime();
			DMDLogs.getEStatsLogger().info(
                    statsLogBuf.toString() + DMDConstants.DMD_PIPE                    
                            + DMDProps.ldf.format(entryTime) + DMDConstants.DMD_PIPE
                            + DMDProps.ldf.format(exitTime) + DMDConstants.DMD_PIPE + prcTime);
		}
		out.println(returnXML);
		out.flush();
		out.close();
	}
	
	private String createResponseXml(String statusCd, String msg) {
		StringBuffer sbReturnXML=new StringBuffer(XML_RESPONSE_START);
		//add header. status and message
		sbReturnXML.append(statusCd)
					.append(STATUS_CODE_END)
					.append(MESSAGE_START)
					.append(msg)
					.append(MESSAGE_END)
					.append(RESPONSE_HEADER_END)
					.append(DMD_END);
		
		return sbReturnXML.toString();
		
	}
}
