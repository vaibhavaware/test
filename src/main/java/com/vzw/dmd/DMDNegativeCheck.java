package com.vzw.dmd;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.vzw.dmd.util.DMDConstants;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.DMDUtils;
import com.vzw.dmd.util.LTEProcessor;
import com.vzw.dmd.util.LteXmlCreator;
import com.vzw.dmd.util.XSSEncoder;
import com.vzw.dmd.valueobject.NegativityCheckVO;

public class DMDNegativeCheck extends HttpServlet{
	private static Logger L =
		Logger.getLogger(DMDLogs.getLogName(DMDNegativeCheck.class));
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
		String xmlReq = null;
		Enumeration enu_p = req.getParameterNames();
		
		while(enu_p.hasMoreElements()){				
			String name = (String)enu_p.nextElement();
			xmlReq = req.getParameter(name);
			L.debug("name:" + name + " value: " + xmlReq);
		}
		L.info(" xmlReq: " + xmlReq);
		NegativityCheckVO multiDeviceVO= new NegativityCheckVO();		
		ServletOutputStream out = res.getOutputStream();
		try{
			LTEProcessor processor = new LTEProcessor();
			
			processor.getNegativityCheckData(xmlReq, multiDeviceVO);
			
			String returnXML = LteXmlCreator.createNegativityCheckXML(multiDeviceVO);
			String statusCode=multiDeviceVO.getStatusCode();
			if(statusCode.equals(DMDConstants.STATUS_CODE_ERROR)){//error
				statsLogBuf.append(multiDeviceVO.getRequestInfo().getAppType())
							.append(DMDConstants.DMD_PIPE)
							.append(DMDUtils.getClientIP(req))
							.append(DMDConstants.DMD_PIPE)
							.append(DMDConstants.LOOKUP)
							.append(DMDConstants.DMD_PIPE)
							.append(DMDConstants.NEGATIVITY_CHECK_API)
							.append(DMDConstants.DMD_PIPE)
							.append(DMDConstants.DMD_NONE)
							.append(DMDConstants.DMD_PIPE)
							.append(DMDConstants.DMD_FALSE)
							.append(DMDConstants.DMD_PIPE)
							.append(multiDeviceVO.getStatusMessage());	
				
				//Fortify Fix - Cross-site scripting
				out.println(new XSSEncoder().encodeXML(returnXML));
				out.flush();
				out.close();
			}else if(statusCode.equals(DMDConstants.STATUS_CODE__INVALID_INPUT)){//error
				statsLogBuf.append(multiDeviceVO.getRequestInfo().getAppType())
				.append(DMDConstants.DMD_PIPE)
				.append(DMDUtils.getClientIP(req))
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.LOOKUP)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.NEGATIVITY_CHECK_API)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.DMD_NONE)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.DMD_FALSE)
				.append(DMDConstants.DMD_PIPE)
				.append(multiDeviceVO.getStatusMessage());
				
				//Fortify Fix - Cross-site scripting
				out.println(new XSSEncoder().encodeXML(returnXML));
				out.flush();
				out.close();
			}else{
				statsLogBuf.append(multiDeviceVO.getRequestInfo().getAppType())
				.append(DMDConstants.DMD_PIPE)
				.append(DMDUtils.getClientIP(req))
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.LOOKUP)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.NEGATIVITY_CHECK_API)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.DMD_NONE)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.DMD_TRUE)
				.append(DMDConstants.DMD_PIPE)
				.append(multiDeviceVO.getStatusMessage());	
	
				//Fortify Fix - Cross-site scripting
				out.print(new XSSEncoder().encodeXML(returnXML));
				out.flush();
				out.close();
			}
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