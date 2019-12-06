package com.vzw.dmd;

import java.io.IOException;
import java.util.Date;

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
import com.vzw.dmd.valueobject.DaccSaccCompatibilityVO;
import com.vzw.dmd.valueobject.MultiDeviceVO;
import com.vzw.dmd.valueobject.SaccDaccCompatibilityVO;

public class DMDSaccDaccCompatibility extends HttpServlet{
	private static Logger L =
		Logger.getLogger(DMDLogs.getLogName(DMDSaccDaccCompatibility.class));
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
		
		SaccDaccCompatibilityVO compatibilityVO= new SaccDaccCompatibilityVO();		
		ServletOutputStream out = res.getOutputStream();
		boolean proceedToGetData = false;
		try{
			String sacc = req.getParameter(DMDConstants.COMPA_INPUT_DEVICE_SACC);
			if(sacc == null || sacc.trim().equals(DMDConstants.EMPTY_STRING)){
				compatibilityVO.setStatusCode(DMDConstants.STATUS_CODE__INVALID_INPUT);
				compatibilityVO.setStatusMessage(DMDConstants.STATUS_MESSAGE_INVALID_INPUT_SACC_COMPA);
			}else{
				compatibilityVO.setSacc(sacc);
				proceedToGetData = true;
			}
			if(proceedToGetData){
				LTEProcessor processor = new LTEProcessor();
				processor.getSaccDaccCompatibilityData(compatibilityVO);
			}
			
			String returnXML = LteXmlCreator.createSaccDaccCompatibilityXML(compatibilityVO);
			String statusCode=compatibilityVO.getStatusCode();
			if(statusCode.equals(DMDConstants.STATUS_CODE_ERROR)){//error
				statsLogBuf.append(compatibilityVO.getRequestInfo().getAppType())
							.append(DMDConstants.DMD_PIPE)
							.append(DMDUtils.getClientIP(req))
							.append(DMDConstants.DMD_PIPE)
							.append(DMDConstants.LOOKUP)
							.append(DMDConstants.DMD_PIPE)
							.append(DMDConstants.SACC_DACC_COMPATIBILITY_API)
							.append(DMDConstants.DMD_PIPE)
							.append(DMDConstants.DMD_NONE)
							.append(DMDConstants.DMD_PIPE)
							.append(DMDConstants.DMD_FALSE)
							.append(DMDConstants.DMD_PIPE)
							.append(compatibilityVO.getStatusMessage());	
				
				//Fortify Fix - Cross-site scripting
				out.println(new XSSEncoder().encodeXML(returnXML));
				out.flush();
				out.close();
			}else if(statusCode.equals(DMDConstants.STATUS_CODE__INVALID_INPUT)){//error
				statsLogBuf.append(compatibilityVO.getRequestInfo().getAppType())
				.append(DMDConstants.DMD_PIPE)
				.append(DMDUtils.getClientIP(req))
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.LOOKUP)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.SACC_DACC_COMPATIBILITY_API)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.DMD_NONE)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.DMD_FALSE)
				.append(DMDConstants.DMD_PIPE)
				.append(compatibilityVO.getStatusMessage());
				
				//Fortify Fix - Cross-site scripting
				out.println(new XSSEncoder().encodeXML(returnXML));
				out.flush();
				out.close();
			}else{
				statsLogBuf.append(compatibilityVO.getRequestInfo().getAppType())
				.append(DMDConstants.DMD_PIPE)
				.append(DMDUtils.getClientIP(req))
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.LOOKUP)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.SACC_DACC_COMPATIBILITY_API)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.DMD_NONE)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.DMD_TRUE)
				.append(DMDConstants.DMD_PIPE)
				.append(compatibilityVO.getStatusMessage());	
	
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
