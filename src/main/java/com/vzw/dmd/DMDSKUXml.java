/*
 * Created on Jul 11, 2008
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.vzw.dmd;

import java.io.IOException;
import java.util.Date;
import java.util.Vector;

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
import com.vzw.dmd.valueobject.DMDSKUDetail;
import com.vzw.dmd.valueobject.DMDSKUVO;
import com.vzw.dmd.valueobject.DeviceInfo;

/**
 * @author damodra
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DMDSKUXml extends HttpServlet {

	private static Logger L = Logger.getLogger(DMDLogs.getLogName(DMDSKUXml.class));
	XSSEncoder encoder = new XSSEncoder();

	public void doGet(HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException {
		try {
			L.debug("Inside dmdskuxml: ");
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
		L.debug("Inside dmdskuxml: ");
		
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
		String device_id= req.getParameter("deviceid");
		String action = req.getParameter("action");
		DeviceInfo deviceInfo = new DeviceInfo();
		
		if(req.getParameterValues("esn") != null){
			device_id = req.getParameter("esn");
		}
		else if (req.getParameter("meid") != null){
			device_id= req.getParameter("meid").toUpperCase();
		}
//		 check device
		if(device_id != null && !device_id.trim().equals(DMDConstants.EMPTY_STRING)){
			deviceInfo.setDeviceIdType(DMDUtils.getDeviceIDType(device_id));
			if(DMDConstants.DEVICE_TYPE_ESN.equals(deviceInfo.getDeviceIdType())){
				deviceInfo.setDeviceId(DMDUtils.addLeadingZeros(device_id));
		}else{
				deviceInfo.setDeviceId(device_id.toUpperCase());
			}
		}
		int deviceNotFound =DeviceLookupDAO.locateDevice(deviceInfo);
		L.debug("Device ID "+device_id + "Device Found " + deviceNotFound );
		if(deviceNotFound  == DMDConstants.DMD_DEVICE_NOT_FOUND && DMDConstants.DEVICE_TYPE_IMEI.equals(deviceInfo.getDeviceIdType()) && deviceInfo.getDeviceId().length()==14){
			deviceInfo.setDeviceIdType(DMDConstants.DEVICE_TYPE_MEID);
			deviceNotFound =DeviceLookupDAO.locateDevice(deviceInfo);
		}
		L.debug("Device ID "+device_id + "Device Found " + deviceNotFound );
		// Done device lookup
		if (DMDConstants.DEVICE_TYPE_ESN.equals(deviceInfo.getDeviceIdType())) {
			
			//String esn = req.getParameter("esn");
			//esn = DMDUtils.addLeadingZeros(esn.trim());
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			out.println("<DMD>");

			DMDEquipmentIdLookupRequestVO vo = new DMDEquipmentIdLookupRequestVO();				
					vo.setEsn(deviceInfo.getDeviceId());
					//DMDSKUVO dmdSKUVO = null;
				
				DMDSKUVO dmdSKUVO  = daoLookup.getDMDSKUEsn(vo);
				
				if (dmdSKUVO != null )  
				{
					String dymaxBam = dmdSKUVO.getDymaxBam();
					String sku = dmdSKUVO.getSku();
					if(dymaxBam.trim().length() == 0 && sku.trim().length() == 0){
						
						out.println("<STATUS>ERROR</STATUS>");
						if(req.getParameterValues("esn") != null){
							out.println("<ESN>" + encoder.encodeXMLAttribute(deviceInfo.getDeviceId()) + "</ESN>");
						}
						else{
							out.println("<DEVICE_ID>" + encoder.encodeXMLAttribute(deviceInfo.getDeviceId()) + "</DEVICE_ID>");
						}
						out.println("<DYMAX_BAM>Not Found</DYMAX_BAM>");
						out.println("<SKU>Not Found</SKU>");
						out.println("<SIM_CLASS_4G>Not Found</SIM_CLASS_4G>");
						out.println("<VIRTUAL_SIM_SKU>Not Found</VIRTUAL_SIM_SKU>");
						statsLogBuf.append(deviceInfo.getDeviceId() + "|");
						statsLogBuf.append("FALSE|Invalid ESN/MEID");
					}else{
						out.println("<STATUS>Normal</STATUS>");
						if(req.getParameterValues("esn") != null){
							out.println("<ESN>" + encoder.encodeXMLAttribute(deviceInfo.getDeviceId()) + "</ESN>");
						}
						else{
							out.println("<DEVICE_ID>" + encoder.encodeXMLAttribute(deviceInfo.getDeviceId()) + "</DEVICE_ID>");
						}
						//out.println("<ESN>" + deviceInfo.getDeviceId() + "</ESN>");
						out.println("<DYMAX_BAM>" + encoder.encodeXMLAttribute(dymaxBam) + "</DYMAX_BAM>");
						out.println("<SKU>" + encoder.encodeXMLAttribute(sku) + "</SKU>");
						out.println("<SIM_CLASS_4G></SIM_CLASS_4G>");
						out.println("<VIRTUAL_SIM_SKU></VIRTUAL_SIM_SKU>");
						statsLogBuf.append(deviceInfo.getDeviceId() + "|");
						statsLogBuf.append("TRUE|");
						
					}
					

				}
				else {
					out.println("<STATUS>ERROR</STATUS>");
					if(req.getParameterValues("esn") != null){
						out.println("<ESN>" + encoder.encodeXMLAttribute(deviceInfo.getDeviceId()) + "</ESN>");
					}
					else{
						out.println("<DEVICE_ID>" + encoder.encodeXMLAttribute(deviceInfo.getDeviceId()) + "</DEVICE_ID>");
					}
					//out.println("<ESN>" + deviceInfo.getDeviceId() + "</ESN>");					
					out.println("<DYMAX_BAM>Not Found</DYMAX_BAM>");
					out.println("<SKU>Not Found</SKU>");
					out.println("<SIM_CLASS_4G>Not Found</SIM_CLASS_4G>");
					out.println("<VIRTUAL_SIM_SKU>Not Found</VIRTUAL_SIM_SKU>");
					
					statsLogBuf.append(deviceInfo.getDeviceId() + "|");
					statsLogBuf.append("FALSE|Invalid ESN/MEID");
				}
				
			out.println("</DMD>");
		}
		else if (deviceInfo.getDeviceIdType().equals(DMDConstants.DEVICE_TYPE_MEID)){
			
			//String meid = req.getParameter("meid").toUpperCase();
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			out.println("<DMD>");
			

				
			DMDEquipmentIdLookupRequestVO vo = new DMDEquipmentIdLookupRequestVO();				
				

					//vo.setMeid(DMDUtils.convertMEIDFromHexToDecimal(meid));
			//vo.setMeid(DMDUtils.convertMEIDFromHexToDecimal(deviceInfo.getDeviceId()));
			vo.setMeid(deviceInfo.getDeviceId().trim());

			DMDSKUVO dmdSKUVO  = daoLookup.getDMDSKUMeid(vo);
					//String dymaxBam = daoLookup.getDMDSKUMeid(vo);
			if (dmdSKUVO != null ) 
				{				
					L.debug("dmdSKUVO is not null ");
					String dymaxBam = dmdSKUVO.getDymaxBam();
					String sku = dmdSKUVO.getSku();

					if(dymaxBam.trim().length() == 0 && sku.trim().length() == 0){
						out.println("<STATUS>ERROR</STATUS>");
						if(req.getParameter("meid") != null){ 
							out.println("<MEID>" + encoder.encodeXMLAttribute(deviceInfo.getDeviceId()) + "</MEID>");
						}else{
							out.println("<DEVICE_ID>" + encoder.encodeXMLAttribute(deviceInfo.getDeviceId()) + "</DEVICE_ID>");
						}
						out.println("<DYMAX_BAM>Not Found</DYMAX_BAM>");
						out.println("<SKU>Not Found</SKU>");
						out.println("<SIM_CLASS_4G>Not Found</SIM_CLASS_4G>");
						out.println("<VIRTUAL_SIM_SKU>Not Found</VIRTUAL_SIM_SKU>");
						statsLogBuf.append(deviceInfo.getDeviceId() + "|");
						statsLogBuf.append("FALSE|Invalid MEID");

					}else{
						out.println("<STATUS>Normal</STATUS>");
						if(req.getParameter("meid") != null){ 
							out.println("<MEID>" + encoder.encodeXMLAttribute(deviceInfo.getDeviceId()) + "</MEID>");
						}else{
							out.println("<DEVICE_ID>" + encoder.encodeXMLAttribute(deviceInfo.getDeviceId()) + "</DEVICE_ID>");
						}
						//out.println("<MEID>" + deviceInfo.getDeviceId() + "</MEID>");					
						out.println("<DYMAX_BAM>" + encoder.encodeXMLAttribute(dymaxBam) + "</DYMAX_BAM>");
						out.println("<SKU>" + encoder.encodeXMLAttribute(sku) + "</SKU>");
						out.println("<SIM_CLASS_4G></SIM_CLASS_4G>");
						out.println("<VIRTUAL_SIM_SKU></VIRTUAL_SIM_SKU>");
						statsLogBuf.append(deviceInfo.getDeviceId() + "|");
						statsLogBuf.append("TRUE|");
					}
				}
				else {
					out.println("<STATUS>ERROR</STATUS>");
					if(req.getParameter("meid") != null){ 
						out.println("<MEID>" + encoder.encodeXMLAttribute(deviceInfo.getDeviceId()) + "</MEID>");
					}else{
						out.println("<DEVICE_ID>" + encoder.encodeXMLAttribute(deviceInfo.getDeviceId()) + "</DEVICE_ID>");
					}
					//out.println("<MEID>" + deviceInfo.getDeviceId() + "</MEID>");					
					out.println("<DYMAX_BAM>Not Found</DYMAX_BAM>");
					out.println("<SKU>Not Found</SKU>");
					out.println("<SIM_CLASS_4G>Not Found</SIM_CLASS_4G>");
					out.println("<VIRTUAL_SIM_SKU>Not Found</VIRTUAL_SIM_SKU>");
					statsLogBuf.append(deviceInfo.getDeviceId() + "|");
					statsLogBuf.append("FALSE|Invalid MEID");

				}
				
			out.println("</DMD>");

		}
		else if (deviceInfo.getDeviceIdType().equals(DMDConstants.DEVICE_TYPE_IMEI)){
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			out.println("<DMD>");
			

				
			DMDEquipmentIdLookupRequestVO vo = new DMDEquipmentIdLookupRequestVO();				
				

					//vo.setMeid(DMDUtils.convertMEIDFromHexToDecimal(meid));
			vo.setImei(deviceInfo.getDeviceId().substring(0, 14));

			DMDSKUVO dmdSKUVO  = daoLookup.getDMDSKUImei(vo,deviceInfo);
					//String dymaxBam = daoLookup.getDMDSKUMeid(vo);
			if (dmdSKUVO != null ) 
				{				
					L.debug("dmdSKUVO is not null ");
					String dymaxBam = dmdSKUVO.getDymaxBam();
					String sku = dmdSKUVO.getSku();

					if(dymaxBam.trim().length() == 0 && sku.trim().length() == 0){
						out.println("<STATUS>ERROR</STATUS>");
						out.println("<DEVICE_ID>" + encoder.encodeXMLAttribute(deviceInfo.getDeviceId()) + "</DEVICE_ID>");					
						out.println("<DYMAX_BAM>Not Found</DYMAX_BAM>");
						out.println("<SKU>Not Found</SKU>");
						out.println("<SIM_CLASS_4G>Not Found</SIM_CLASS_4G>");
						out.println("<VIRTUAL_SIM_SKU>Not Found</VIRTUAL_SIM_SKU>");
						statsLogBuf.append(deviceInfo.getDeviceId() + "|");
						statsLogBuf.append("FALSE|Invalid DEVICE_ID");

					}else{
						String simClass4G = deviceInfo.getSimClass4g();
						String virtualSimSku = deviceInfo.getSimModelId();
						if(simClass4G == null) {
							simClass4G = "";
						}
						
						if(virtualSimSku == null) {
							virtualSimSku = "";
						}
						
						out.println("<STATUS>Normal</STATUS>");
						out.println("<DEVICE_ID>" + encoder.encodeXMLAttribute(deviceInfo.getDeviceId()) + "</DEVICE_ID>");					
						out.println("<DYMAX_BAM>" + encoder.encodeXMLAttribute(dymaxBam) + "</DYMAX_BAM>");
						out.println("<SKU>" + encoder.encodeXMLAttribute(sku) + "</SKU>");
						out.println("<SIM_CLASS_4G>" + encoder.encodeXMLAttribute(simClass4G) + "</SIM_CLASS_4G>");
						out.println("<VIRTUAL_SIM_SKU>" + encoder.encodeXMLAttribute(virtualSimSku) + "</VIRTUAL_SIM_SKU>");
						statsLogBuf.append(deviceInfo.getDeviceId() + "|");
						statsLogBuf.append("TRUE|");
					}
				}
				else {
					out.println("<STATUS>ERROR</STATUS>");
					out.println("<DEVICE_ID>" + encoder.encodeXMLAttribute(deviceInfo.getDeviceId()) + "</DEVICE_ID>");					
					out.println("<DYMAX_BAM>Not Found</DYMAX_BAM>");
					out.println("<SKU>Not Found</SKU>");
					out.println("<SIM_CLASS_4G>Not Found</SIM_CLASS_4G>");
					out.println("<VIRTUAL_SIM_SKU>Not Found</VIRTUAL_SIM_SKU>");
					statsLogBuf.append(deviceInfo.getDeviceId() + "|");
					statsLogBuf.append("FALSE|Invalid DEVICE_ID");

				}
				
			out.println("</DMD>");
		}
		else if (deviceInfo.getDeviceIdType().equals(DMDConstants.DEVICE_TYPE_ICCID)){
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			out.println("<DMD>");
			

				
			DMDEquipmentIdLookupRequestVO vo = new DMDEquipmentIdLookupRequestVO();				
				

					//vo.setMeid(DMDUtils.convertMEIDFromHexToDecimal(meid));
			vo.setIccid(deviceInfo.getDeviceId());

			DMDSKUVO dmdSKUVO  = daoLookup.getDMDSKUIccid(vo,deviceInfo);
					//String dymaxBam = daoLookup.getDMDSKUMeid(vo);
			if (dmdSKUVO != null ) 
				{				
					L.debug("dmdSKUVO is not null ");
					String dymaxBam = dmdSKUVO.getDymaxBam();
					String sku = dmdSKUVO.getSku();

					if(dymaxBam.trim().length() == 0 && sku.trim().length() == 0){
						out.println("<STATUS>ERROR</STATUS>");
						out.println("<DEVICE_ID>" + encoder.encodeXMLAttribute(deviceInfo.getDeviceId()) + "</DEVICE_ID>");					
						out.println("<DYMAX_BAM>Not Found</DYMAX_BAM>");
						out.println("<SKU>Not Found</SKU>");
						out.println("<SIM_CLASS_4G>Not Found</SIM_CLASS_4G>");
						out.println("<VIRTUAL_SIM_SKU>Not Found</VIRTUAL_SIM_SKU>");
						statsLogBuf.append(deviceInfo.getDeviceId() + "|");
						statsLogBuf.append("FALSE|Invalid MEID");

					}else{
						out.println("<STATUS>Normal</STATUS>");
						out.println("<DEVICE_ID>" + encoder.encodeXMLAttribute(deviceInfo.getDeviceId()) + "</DEVICE_ID>");					
						out.println("<DYMAX_BAM>" + encoder.encodeXMLAttribute(dymaxBam) + "</DYMAX_BAM>");
						out.println("<SKU>" + encoder.encodeXMLAttribute(sku) + "</SKU>");
						out.println("<SIM_CLASS_4G></SIM_CLASS_4G>");
						out.println("<VIRTUAL_SIM_SKU></VIRTUAL_SIM_SKU>");
						statsLogBuf.append(deviceInfo.getDeviceId() + "|");
						statsLogBuf.append("TRUE|");
					}
				}
				else {
					out.println("<STATUS>ERROR</STATUS>");
					out.println("<DEVICE_ID>" + encoder.encodeXMLAttribute(deviceInfo.getDeviceId()) + "</DEVICE_ID>");					
					out.println("<DYMAX_BAM>Not Found</DYMAX_BAM>");
					out.println("<SKU>Not Found</SKU>");
					out.println("<SIM_CLASS_4G>Not Found</SIM_CLASS_4G>");
					out.println("<VIRTUAL_SIM_SKU>Not Found</VIRTUAL_SIM_SKU>");
					statsLogBuf.append(deviceInfo.getDeviceId() + "|");
					statsLogBuf.append("FALSE|Invalid DEVICE_ID");

				}
				
			out.println("</DMD>");
		}
		else {
			  out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			  out.println("<DMD>");
			  out.println("	<STATUS>ERROR</STATUS>"); 
			  out.println("	<MESSAGE>Please input ESN or MEID to search the database</MESSAGE>"); 
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
