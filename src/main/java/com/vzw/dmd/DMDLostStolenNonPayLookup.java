package com.vzw.dmd;

import java.io.IOException;
import java.util.*;

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
import com.vzw.dmd.valueobject.LostStolenNonPayLookupVO;
import com.vzw.dmd.valueobject.NegativityCheckVO;

public class DMDLostStolenNonPayLookup extends HttpServlet{
	private static Logger L =
		Logger.getLogger(DMDLogs.getLogName(DMDLostStolenNonPayLookup.class));
	XSSEncoder encoder = new XSSEncoder();
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
		LostStolenNonPayLookupVO lostStolenNonPayLookupVO= new LostStolenNonPayLookupVO();		
		ServletOutputStream out = res.getOutputStream();
		try{
			LTEProcessor processor = new LTEProcessor();
			
			String returnXML = null, statusCode = null;
			if(xmlReq != null && xmlReq.contains("<subServiceName>pairLookup</subServiceName>")) {
				List lstolenNonPayVOArr = processor.getLostStolenNonPayPairInfo(xmlReq);
				if(lstolenNonPayVOArr == null || lstolenNonPayVOArr.size() == 0 
						|| lstolenNonPayVOArr.get(0) == null 
						|| !((LostStolenNonPayLookupVO)lstolenNonPayVOArr.get(0)).getStatusCode().equals(DMDConstants.STATUS_CODE_SUCCESS)) {
					String errMsg = DMDConstants.STATUS_MESSAGE_ERROR;
					try { errMsg = ((LostStolenNonPayLookupVO)lstolenNonPayVOArr.get(0)).getStatusMessage().trim();} catch(Exception e) {}
					
					returnXML = LteXmlCreator.createLostStolenNonPayInfoPairXML( DMDConstants.STATUS_CODE_ERROR
							                                                   , errMsg
							                                                   , new ArrayList(), "");
					lostStolenNonPayLookupVO.setStatusCode(DMDConstants.STATUS_CODE_ERROR);
					statusCode = DMDConstants.STATUS_CODE_ERROR;
				} else {
					String compInd = "";
					if(lstolenNonPayVOArr.size() == 2) {
						try {
							LostStolenNonPayLookupVO vo1, vo2;
							vo1 = (LostStolenNonPayLookupVO)lstolenNonPayVOArr.get(0);
							vo2 = (LostStolenNonPayLookupVO)lstolenNonPayVOArr.get(1);
							if(vo1 == null || vo2 == null) {
								compInd = "";
							} else {
								compInd = "N";
								if(vo2.getDeviceId().equals(vo1.getPairId())
										&& vo1.getDeviceId().equals(vo2.getPairId())) {
									compInd = "Y";
								}
							}
						} catch (Exception e) {
							compInd = "N";
						}
					}
					returnXML = LteXmlCreator.createLostStolenNonPayInfoPairXML( DMDConstants.STATUS_CODE_SUCCESS
				                            , DMDConstants.STATUS_MESSAGE_SUCCESS
				                            , lstolenNonPayVOArr, compInd);
					lostStolenNonPayLookupVO.setStatusCode(DMDConstants.STATUS_CODE_SUCCESS);
					statusCode = DMDConstants.STATUS_CODE_SUCCESS;
				}
			} else {
				processor.getLostStolenNonPayInfo(xmlReq, lostStolenNonPayLookupVO);
				returnXML = LteXmlCreator.createLostStolenNonPayInfoXML(lostStolenNonPayLookupVO);
				statusCode = lostStolenNonPayLookupVO.getStatusCode();
			}
			
			if(statusCode.equals(DMDConstants.STATUS_CODE_ERROR)){//error
				statsLogBuf.append(lostStolenNonPayLookupVO.getRequestInfo().getAppType())
							.append(DMDConstants.DMD_PIPE)
							.append(DMDUtils.getClientIP(req))
							.append(DMDConstants.DMD_PIPE)
							.append(DMDConstants.LOOKUP)
							.append(DMDConstants.DMD_PIPE)
							.append(DMDConstants.LOSTSTOLEN_NONPAY_LOOKUP_API)
							.append(DMDConstants.DMD_PIPE)
							.append(DMDConstants.DMD_NONE)
							.append(DMDConstants.DMD_PIPE)
							.append(DMDConstants.DMD_FALSE)
							.append(DMDConstants.DMD_PIPE)
							.append(lostStolenNonPayLookupVO.getStatusMessage());	
				
				out.println(encoder.encodeXML(returnXML));
				out.flush();
				out.close();
			}else if(statusCode.equals(DMDConstants.STATUS_CODE__INVALID_INPUT)){//error
				statsLogBuf.append(lostStolenNonPayLookupVO.getRequestInfo().getAppType())
				.append(DMDConstants.DMD_PIPE)
				.append(DMDUtils.getClientIP(req))
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.LOOKUP)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.LOSTSTOLEN_NONPAY_LOOKUP_API)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.DMD_NONE)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.DMD_FALSE)
				.append(DMDConstants.DMD_PIPE)
				.append(lostStolenNonPayLookupVO.getStatusMessage());
				
				out.println(encoder.encodeXML(returnXML));
				out.flush();
				out.close();
			}else{
				statsLogBuf.append(lostStolenNonPayLookupVO.getRequestInfo().getAppType())
				.append(DMDConstants.DMD_PIPE)
				.append(DMDUtils.getClientIP(req))
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.LOOKUP)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.LOSTSTOLEN_NONPAY_LOOKUP_API)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.DMD_NONE)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.DMD_TRUE)
				.append(DMDConstants.DMD_PIPE)
				.append(lostStolenNonPayLookupVO.getStatusMessage());	
	
				out.print(encoder.encodeXML(returnXML));
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