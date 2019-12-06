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
import com.vzw.dmd.valueobject.MultiDeviceVO;
import com.vzw.dmd.valueobject.SIMCompatibilityVO;

public class DMDSIMCompatibility extends HttpServlet{
	private static Logger L =
		Logger.getLogger(DMDLogs.getLogName(DMDSIMCompatibility.class));
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
		
		SIMCompatibilityVO compatibilityVO= new SIMCompatibilityVO();		
		ServletOutputStream out = res.getOutputStream();
		boolean proceedToGetData = false;
		try{
			String deviceId = req.getParameter(DMDConstants.MULTIDEVICE_INPUT_DEVICE_ID);
			if(deviceId == null || deviceId.trim().equals(DMDConstants.EMPTY_STRING)){
				deviceId = req.getParameter(DMDConstants.MULTIDEVICE_INPUT_DEVICE_SKU);
				if(deviceId == null && deviceId.trim().equals(DMDConstants.EMPTY_STRING)){
					compatibilityVO.setStatusCode(DMDConstants.STATUS_CODE__INVALID_INPUT);//error
					compatibilityVO.setStatusMessage(DMDConstants.STATUS_MESSAGE_INVALID_INPUT_SIM_COMPA);//error
				}else{
					compatibilityVO.setSkuSearch(true);
					compatibilityVO.setDeviceId(deviceId);
					proceedToGetData = true;
				}
			}else{
				compatibilityVO.setDeviceId(deviceId);
				String deviceIdType = DMDUtils.getDeviceIDType(deviceId);
				if(deviceIdType !=null){
					proceedToGetData = true;
					compatibilityVO.getDeviceInfo().setDeviceIdType(deviceIdType);
				}else{
					proceedToGetData = false;
					compatibilityVO.setStatusCode(DMDConstants.STATUS_CODE_DEVICE_NOT_FOUND);
					compatibilityVO.setStatusMessage(DMDConstants.STATUS_MESSAGE_DEVICE_NOT_FOUND);
				}				
			}
			if(proceedToGetData){
				LTEProcessor processor = new LTEProcessor();
				processor.getSIMCompatibilityData(compatibilityVO);
			}
			
			String returnXML = LteXmlCreator.createSIMCompatibilityXML(compatibilityVO);
			String statusCode=compatibilityVO.getStatusCode();
			if(statusCode.equals(DMDConstants.STATUS_CODE_ERROR)){//error
				statsLogBuf.append(compatibilityVO.getRequestInfo().getAppType())
							.append(DMDConstants.DMD_PIPE)
							.append(DMDUtils.getClientIP(req))
							.append(DMDConstants.DMD_PIPE)
							.append(DMDConstants.LOOKUP)
							.append(DMDConstants.DMD_PIPE)
							.append(DMDConstants.SIM_COMAPTIBILITY_API)
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
				.append(DMDConstants.SIM_COMAPTIBILITY_API)
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
				.append(DMDConstants.SIM_COMAPTIBILITY_API)
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
			e.printStackTrace();
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
