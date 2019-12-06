

package com.vzw.dmd;

import java.io.IOException;
import java.util.Date;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.vzw.dmd.dao.DeviceLookupDAO;
import com.vzw.dmd.dao.OracleDAOFactory;
import com.vzw.dmd.dao.OracleEsnLookupDAO;
import com.vzw.dmd.exception.DaoException;
import com.vzw.dmd.util.DMDConstants;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.DMDUtils;
import com.vzw.dmd.util.XSSEncoder;
import com.vzw.dmd.valueobject.DMDEquipmentIdLookupRequestVO;
import com.vzw.dmd.valueobject.DeviceInfo;

public class DMDEquipmentIdXml extends HttpServlet {
	private static Logger L = Logger.getLogger(DMDLogs.getLogName(DMDEquipmentIdXml.class));
	XSSEncoder encoder = new XSSEncoder();

	public void doGet(HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException {
		try {
			defaultAction(req, res);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		try {
		defaultAction(req, res);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void defaultAction(HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException, DaoException, NamingException, CreateException {
		
		res.setStatus(200);
		res.setContentType("text/xml");
		
		
		OracleDAOFactory oracleDAOFactory = OracleDAOFactory.getInstance();
		OracleEsnLookupDAO daoLookup = (OracleEsnLookupDAO)oracleDAOFactory.createEsnLookupDAO();
		ServletOutputStream out = res.getOutputStream();
		
		StringBuffer statsLogBuf = new StringBuffer("XML|");
		
		Date entryTime = new Date();
		
		String appType = req.getParameter("app_type");
		statsLogBuf.append(appType + "|");		
		statsLogBuf.append(DMDUtils.getClientIP(req) + "|");
		statsLogBuf.append("LOOKUP|");
		statsLogBuf.append("ESN_FEATURES|");
		statsLogBuf.append(DMDConstants.DMD_EQUIPMENTID_API);
		statsLogBuf.append(DMDConstants.DMD_PIPE);
		String action = req.getParameter("action");
		//	4G Change
		String device_id = req.getParameter( "deviceID" );
		//
		DeviceInfo deviceInfo = new DeviceInfo();
		if(device_id != null && !device_id.trim().equals(DMDConstants.EMPTY_STRING)){
			deviceInfo.setDeviceIdType(DMDUtils.getDeviceIDType(device_id));
			if(DMDConstants.DEVICE_TYPE_ESN.equals(deviceInfo.getDeviceIdType())){
				deviceInfo.setDeviceId(DMDUtils.addLeadingZeros(device_id));
		}else{
				deviceInfo.setDeviceId(device_id);
			}
		}
		int deviceNotFound =DeviceLookupDAO.locateDevice(deviceInfo);
		L.debug("Device ID "+device_id + "Device Found " + deviceNotFound );
		if(deviceNotFound  == DMDConstants.DMD_DEVICE_NOT_FOUND && DMDConstants.DEVICE_TYPE_IMEI.equals(deviceInfo.getDeviceIdType()) && deviceInfo.getDeviceId().length()==14){
			deviceInfo.setDeviceIdType(DMDConstants.DEVICE_TYPE_MEID);
			deviceNotFound =DeviceLookupDAO.locateDevice(deviceInfo);
		}
		if (DMDConstants.DEVICE_TYPE_ESN.equals(deviceInfo.getDeviceIdType())) {
			
			String esn = device_id;
			esn = DMDUtils.addLeadingZeros(esn.trim());
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			out.println("	<DMD>");

			DMDEquipmentIdLookupRequestVO vo = new DMDEquipmentIdLookupRequestVO();				
					vo.setEsn(esn);
				
				
				String marketingEquipmentId = daoLookup.getDMDEquipmentIdEsn(vo);
				if (marketingEquipmentId != null && !marketingEquipmentId.equals("")) 
				{
					out.println("			<STATUS>Normal</STATUS>");
					//Fortify Fix - Cross-site scripting
					out.println("<DEVICEID>" + encoder.encodeXMLAttribute(esn) + "</DEVICEID>");
					out.println("<MARKETING_EQP_LINK>" + encoder.encodeXMLAttribute(marketingEquipmentId) + "</MARKETING_EQP_LINK>");
					
					statsLogBuf.append(esn + "|");
					statsLogBuf.append("TRUE|");

				}
				
				else {
					out.println("			<STATUS>Normal</STATUS>");
					out.println("<DEVICEID>" + encoder.encodeXMLAttribute(esn) + "</DEVICEID>");					
					out.println("<MARKETING_EQP_LINK>Not Found</MARKETING_EQP_LINK>");
					
					statsLogBuf.append(esn + "|");
					statsLogBuf.append("FALSE|Invalid DeviceIdD");
				}
				
			out.println("</DMD>");
		}
		else if (DMDConstants.DEVICE_TYPE_MEID.equals(deviceInfo.getDeviceIdType())){
			
			String meid = device_id.toUpperCase();
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			out.println("	<DMD>");
			

				
			DMDEquipmentIdLookupRequestVO vo = new DMDEquipmentIdLookupRequestVO();				
				

					//vo.setMeid(DMDUtils.convertMEIDFromHexToDecimal(meid));
			vo.setMeid(meid);

					String marketingEquipmentId = daoLookup.getDMDEquipmentIdMeid(vo);
				if (marketingEquipmentId != null && !marketingEquipmentId.equals("")) 
				{				
					out.println("			<STATUS>Normal</STATUS>");
					//Fortify Fix - Cross-site scripting
					out.println("<DEVICEID>" + encoder.encodeXMLAttribute(meid) + "</DEVICEID>");					
					out.println("<MARKETING_EQP_LINK>" + encoder.encodeXMLAttribute(marketingEquipmentId) + "</MARKETING_EQP_LINK>");
					
					statsLogBuf.append(meid + "|");
					statsLogBuf.append("TRUE|");
				}
				
				else {
					out.println("			<STATUS>Normal</STATUS>");
					out.println("<DEVICEID>" + encoder.encodeXMLAttribute(meid) + "</DEVICEID>");					
					out.println("<MARKETING_EQP_LINK>Not Found</MARKETING_EQP_LINK>");
					
					statsLogBuf.append(meid + "|");
					statsLogBuf.append("FALSE|Invalid DeviceId");

				}
				
			out.println("</DMD>");

		}
		else if (DMDConstants.DEVICE_TYPE_IMEI.equals(deviceInfo.getDeviceIdType()) || DMDConstants.DEVICE_TYPE_ICCID.equals(deviceInfo.getDeviceIdType())||
				DMDConstants.DEVICE_TYPE_MC4.equals(deviceInfo.getDeviceIdType())){
			
			// 4G code here
			String deviceId = device_id.toUpperCase();
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			out.println("	<DMD>");
			

				
			DMDEquipmentIdLookupRequestVO vo = new DMDEquipmentIdLookupRequestVO();				
				

			String marketingEquipmentId = daoLookup.get4GDMDEquipmentId(deviceInfo);
				if (marketingEquipmentId != null && !marketingEquipmentId.equals("")) 
				{				
					out.println("			<STATUS>Normal</STATUS>");
					//Fortify Fix - Cross-site scripting
					out.println("<DEVICEID>" + encoder.encodeXMLAttribute(device_id) + "</DEVICEID>");					
					out.println("<MARKETING_EQP_LINK>" + encoder.encodeXMLAttribute(marketingEquipmentId) + "</MARKETING_EQP_LINK>");
					
					statsLogBuf.append(device_id + "|");
					statsLogBuf.append("TRUE|");
				}
				
				else {
					out.println("			<STATUS>Normal</STATUS>");
					out.println("<DEVICEID>" + encoder.encodeXMLAttribute(device_id) + "</DEVICEID>");					
					out.println("<MARKETING_EQP_LINK>Not Found</MARKETING_EQP_LINK>");
					
					statsLogBuf.append(device_id + "|");
					statsLogBuf.append("FALSE|Invalid DeviceId");

				}
				
			out.println("</DMD>");
			// End 
		}
		else {
			  out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			  out.println("<DMD>");
			  out.println("			<STATUS>ERROR</STATUS>"); 
			  out.println("			<MESSAGE>Please input ESN or MEID to search the database</MESSAGE>"); 
			  out.println("</DMD>");
		}
		
		Date exitTime = new Date();
	    String transId = req.getParameter("transaction_id");
	    if (transId == null || transId.trim().equals(""))
	    {
	        transId = "NONE";
	    }
	    
	    long prcTime = exitTime.getTime() - entryTime.getTime();
		DMDLogs.getStatsLogger().info(statsLogBuf.toString());
		DMDLogs.getEStatsLogger().info(
                statsLogBuf.toString() + "|"
                		+ transId + "|"
                        + DMDProps.ldf.format(entryTime) + "|"
                        + DMDProps.ldf.format(exitTime) + "|" + prcTime);
				
		out.flush();
		out.close();
	}
}
