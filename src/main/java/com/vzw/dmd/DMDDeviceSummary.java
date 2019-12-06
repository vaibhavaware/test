package com.vzw.dmd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.vzw.dmd.dao.OracleDAOFactory;
import com.vzw.dmd.dao.OracleEsnLookupDAO;
import com.vzw.dmd.exception.DaoException;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.DMDUtils;
import com.vzw.dmd.util.XSSEncoder;
import com.vzw.dmd.valueobject.EsnLookupRequestVO;

public class DMDDeviceSummary extends HttpServlet {
	private static Logger L = Logger.getLogger(DMDLogs.getLogName(DMDDeviceSummary.class));
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
		
		ArrayList esnMeidFound = new ArrayList();
		
		OracleDAOFactory oracleDAOFactory = OracleDAOFactory.getInstance();
		OracleEsnLookupDAO daoLookup = (OracleEsnLookupDAO)oracleDAOFactory.createEsnLookupDAO();
		ServletOutputStream out = res.getOutputStream();
		
		StringBuffer statsLogBuf = new StringBuffer("XML|");
		
		Date entryTime = new Date();
		
		String appType = req.getParameter("app_type");
		statsLogBuf.append(appType + "|");		
		statsLogBuf.append(DMDUtils.getClientIP(req) + "|");
		statsLogBuf.append("LOOKUP|");
		//statsLogBuf.append("ESN_MODEL|");
		
		String action = req.getParameter("action");
		
		if (req.getParameter("esn") != null) {
			statsLogBuf.append("ESN_MODEL|");
			
			String esn = req.getParameter("esn");
			esn = DMDUtils.addLeadingZeros(esn.trim());
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			out.println("	<DMD>");
			//out.println("		<ESN_MEID_INFOS>");
			

				
				EsnLookupRequestVO vo = new EsnLookupRequestVO();				
				
				if (esn.length() == 11) {
					vo.setIdType("ESN");
					vo.setId(esn);
				}
				
				vo.setAppType(appType);
				
				EsnLookupRequestVO voResponse = daoLookup.locateEsnMeid(vo);
				if (voResponse.isSearchResultStatus() == true) 
				{
					out.println("		<STATUS>");
					out.println("			<STATUS_STR>Normal</STATUS_STR>");
					out.println("			<MESSAGE />");
					out.println("		</STATUS>");			

					out.println("<EQUIPMENT_ESN>");
					out.println("<ESN>" + encoder.encodeXMLAttribute(esn) + "</ESN>");
					out.println("</EQUIPMENT_ESN>");
					
					out.println("<EQUIPMENT_MODEL>");
					//Fortify Fix - Cross-site scripting
					out.println("<MFG_CODE>" + encoder.encodeXMLAttribute(voResponse.getMfgCode()) + "</MFG_CODE>");
					out.println("<PROD_NAME>" + encoder.encodeXMLAttribute(voResponse.getProdName()) + "</PROD_NAME>");
					out.println("</EQUIPMENT_MODEL>");
					esnMeidFound.add(new Boolean(true));
					statsLogBuf.append(esn + "|");
				}else{
					out.println("		<STATUS>");
					out.println("			<STATUS_STR>ERROR</STATUS_STR>");
					out.println("			<MESSAGE>ESN Not Found</MESSAGE>");
					out.println("		</STATUS>");			
					
					out.println("<EQUIPMENT_ESN>");
					out.println("<ESN>" + encoder.encodeXMLAttribute(esn) + "</ESN>");
					out.println("</EQUIPMENT_ESN>");

				}
				out.println("</DMD>");
		}
		else if (req.getParameter("meid") != null) {
			statsLogBuf.append("MEID_MODEL|");
			String meid = req.getParameter("meid").toUpperCase();
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			out.println("	<DMD>");

			EsnLookupRequestVO vo = new EsnLookupRequestVO();				
			if (meid.length() == 14) {
					vo.setIdType("MEID");
					vo.setId(DMDUtils.convertMEIDFromHexToDecimal(meid.toUpperCase()));
					//vo.setId(meid);
					vo.setAppType(appType);
					
				EsnLookupRequestVO voResponse = daoLookup.locateEsnMeid(vo);
				L.debug(" esnMeidFound : "+voResponse.isSearchResultStatus());
				if (voResponse.isSearchResultStatus() == true) 
				{
					out.println("		<STATUS>");
					out.println("			<STATUS_STR>Normal</STATUS_STR>");
					out.println("			<MESSAGE />");
					out.println("		</STATUS>");			
					
					out.println("<EQUIPMENT_MEID>");
					out.println("<MEID>" + encoder.encodeXMLAttribute(meid) + "</MEID>");
					out.println("</EQUIPMENT_MEID>");

					out.println("<EQUIPMENT_MODEL>");
					//Fortify Fix - Cross-site scripting
					out.println("<MFG_CODE>" + encoder.encodeXMLAttribute(voResponse.getMfgCode()) + "</MFG_CODE>");
					out.println("<PROD_NAME>" + encoder.encodeXMLAttribute(voResponse.getProdName()) + "</PROD_NAME>");
					out.println("</EQUIPMENT_MODEL>");
					esnMeidFound.add(new Boolean(true));
					statsLogBuf.append(meid + "|");
				}else{
					out.println("		<STATUS>");
					out.println("			<STATUS_STR>ERROR</STATUS_STR>");
					out.println("			<MESSAGE>MEID Not Found</MESSAGE>");
					out.println("		</STATUS>");			
					
					out.println("<EQUIPMENT_MEID>");
					out.println("<MEID>" + encoder.encodeXMLAttribute(meid) + "</MEID>");
					out.println("</EQUIPMENT_MEID>");

				}

			}else{
				out.println("		<STATUS>");
				out.println("			<STATUS_STR>ERROR</STATUS_STR>");
				out.println("			<MESSAGE>Invalid MEID</MESSAGE>");
				out.println("		</STATUS>");			
				
				out.println("<EQUIPMENT_MEID>");
				out.println("<MEID>" + encoder.encodeXMLAttribute(meid) + "</MEID>");
				out.println("</EQUIPMENT_MEID>");
				
			}
				
			out.println("</DMD>");
			
		}else {
		  statsLogBuf.append("ESN_MODEL|");	
		  out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		  out.println("<DMD>");
		  out.println("			<STATUS>ERROR</STATUS>"); 
		  out.println("			<MESSAGE>Please input ESN or MEID to search the database</MESSAGE>"); 
		  out.println("</DMD>");
		}
				
				

		
		Iterator it = esnMeidFound.iterator();
		boolean found = false;
		
		while (it.hasNext()) {
			Boolean result = (Boolean) it.next();
			if (result.booleanValue() == true)
				found = true;
		}
		
		if (found)
			statsLogBuf.append("TRUE|");
		else
			statsLogBuf.append("FALSE|Invalid ESN/MEID");
		
		
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
