/*
 * Created on Jul 7, 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.vzw.dmd;

import java.io.IOException;
import java.util.Date;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.vzw.dmd.dao.DeviceLookupDAO;
import com.vzw.dmd.ejb.EsnMeidLookupLocal;
import com.vzw.dmd.ejb.EsnMeidLookupLocalHome;
import com.vzw.dmd.util.DMDConstants;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.DMDUtils;
import com.vzw.dmd.util.XSSEncoder;
import com.vzw.dmd.valueobject.DeviceInfo;
import com.vzw.dmd.valueobject.EsnLookupRequestVO;

/**
 * @author c0gaddv
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DMDPIDLookUP extends HttpServlet {
	private static Logger L =
		Logger.getLogger(DMDLogs.getLogName(DMDPIDLookUP.class));
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
		StringBuffer statsLogBuf = new StringBuffer("XML|");
		Date entryTime = new Date();
		String esnMeidLog=null;
		EsnLookupRequestVO lookVO = new EsnLookupRequestVO();
		InitialContext ic = null;
		EsnMeidLookupLocalHome ejbHome = null;
		EsnMeidLookupLocal ejb = null;
		try{
			ic = new InitialContext();
			ejbHome =(EsnMeidLookupLocalHome) ic.lookup(
				"java:comp/env/ejb/EsnMeidLookup");
			ejb = ejbHome.create();
			String app_type = req.getParameter("app_type");
			String esn_meid = req.getParameter("esn_meid");
			String device_id = req.getParameter("deviceID");
			if(esn_meid==null)
				esn_meid = req.getParameter("ESN_MEID");
			
				
			L.debug("esn_meid = (" + esn_meid + ")");
			if(esn_meid != null && !esn_meid.trim().equals("")){
				if(esn_meid.trim().length()<=11){
					lookVO.setIdType("ESN");
					lookVO.setId(DMDUtils.addLeadingZeros(esn_meid.trim()));
				}
				else{
					lookVO.setIdType("MEID");
					lookVO.setId(esn_meid.trim());
				}				
			}
			else{
				statsLogBuf.append("FALSE|Invalid DeviceID");
				//return "ERR: Please enter value for 'esn_meid' parameter.";				
			}
			lookVO.setAppType(app_type);
			String retDoc="";
			int deviceNotFound=1;
			if(device_id != null && !device_id.equals("")){
				DeviceInfo deviceInfo = new DeviceInfo();
				if(device_id != null && !device_id.trim().equals(DMDConstants.EMPTY_STRING)){
					deviceInfo.setDeviceIdType(DMDUtils.getDeviceIDType(device_id));
					if(DMDConstants.DEVICE_TYPE_ESN.equals(deviceInfo.getDeviceIdType())){
						deviceInfo.setDeviceId(DMDUtils.addLeadingZeros(device_id));
				}else{
						deviceInfo.setDeviceId(device_id.toUpperCase());
					}
				}
				deviceNotFound =DeviceLookupDAO.locateDevice(deviceInfo);
				L.debug("Device ID "+device_id + "Device Found " + deviceNotFound );
				if(deviceNotFound  == DMDConstants.DMD_DEVICE_NOT_FOUND && DMDConstants.DEVICE_TYPE_IMEI.equals(deviceInfo.getDeviceIdType()) && deviceInfo.getDeviceId().length()==14){
					deviceInfo.setDeviceIdType(DMDConstants.DEVICE_TYPE_MEID);
					deviceNotFound =DeviceLookupDAO.locateDevice(deviceInfo);
				}
				L.debug("Device ID "+device_id + "Device Found " + deviceNotFound );
				 retDoc = ejb.getPlatformIDXML4G(deviceInfo,deviceNotFound);
			}
			else{
				 retDoc=ejb.getPlatformIDXML(lookVO);
			}
			ServletOutputStream out = res.getOutputStream();
			//Fortify Fix - Cross-site scripting
			out.println(new XSSEncoder().encodeHTML(retDoc));
			out.flush();
			out.close();
			if(device_id != null && !device_id.equals("")){
				esnMeidLog="DEVICE_SOFTWARE";
				
			}else{
						if("ESN".equalsIgnoreCase(lookVO.getIdType()))
							esnMeidLog="ESN_SOFTWARE";
						else if ("MEID".equalsIgnoreCase(lookVO.getIdType())){
							esnMeidLog="MEID_SOFTWARE";
						}
			}
			
			
			
			statsLogBuf.append(app_type)
			.append("|")
			.append(DMDUtils.getClientIP(req))				
			.append("|LOOKUP|")
			.append(esnMeidLog)
			.append("|");				
			if(device_id != null && !device_id.equals("")){
						if(deviceNotFound == 0){
							statsLogBuf.append(lookVO.getId())
							.append("|TRUE|");
						}
						else{
							statsLogBuf.append("|FALSE|");
						}
			}
			else{
					if(lookVO.isSearchResultStatus())
						statsLogBuf.append(lookVO.getId())
						.append("|TRUE|");
					else
						statsLogBuf.append("|FALSE|");
			}

		}catch (Exception e){
			statsLogBuf.append("ERROR|Application ERROR");
		}finally{
			Date exitTime = new Date();
			DMDLogs.getStatsLogger().info(statsLogBuf.toString());
			long prcTime = exitTime.getTime() - entryTime.getTime();
			DMDLogs.getEStatsLogger().info(
                    statsLogBuf.toString() + "|"
                            + DMDProps.ldf.format(entryTime) + "|"
                            + DMDProps.ldf.format(exitTime) + "|" + prcTime);
		}
	}
}
