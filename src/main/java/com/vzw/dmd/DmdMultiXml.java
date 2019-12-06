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

public class DmdMultiXml extends HttpServlet {
	private static Logger L = Logger.getLogger(DMDLogs.getLogName(DmdMultiXml.class));
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
		statsLogBuf.append("ESN_FEATURES|");
		
		String action = req.getParameter("action");
		
		if (req.getParameterValues("esn_meid") != null) {
			
			String[] esnMeids = req.getParameterValues("esn_meid");
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			out.println("	<DMD>");
			out.println("		<STATUS>");
			out.println("			<STATUS_STR>Normal</STATUS_STR>");
			out.println("			<MESSAGE />");
			out.println("		</STATUS>");			
			out.println("		<ESN_MEID_INFOS>");
			
			for (int x = 0; x < esnMeids.length; x++) {
				out.println("<ESN_MEID_INFO>");
				
				EsnLookupRequestVO vo = new EsnLookupRequestVO();				
				
				if (esnMeids[x].length() == 11) {
					vo.setIdType("ESN");
					out.println("<ESN>" + encoder.encodeXMLAttribute(esnMeids[x]) + "</ESN>");
					vo.setId(esnMeids[x]);
				}
				else {
					vo.setIdType("MEID");
					out.println("<MEID>" + encoder.encodeXMLAttribute(esnMeids[x].toUpperCase()) + "</MEID>");
					vo.setId(DMDUtils.convertMEIDFromHexToDecimal(esnMeids[x].toUpperCase()));
				}
				
				vo.setAppType(appType);
				
				EsnLookupRequestVO voResponse = daoLookup.locateEsnMeid(vo);
				if (voResponse.isSearchResultStatus() == true) 
				{			
					//Fortify Fix - Cross-site scripting
					out.println("<MFG_CODE>" + encoder.encodeXMLAttribute(voResponse.getMfgCode()) + "</MFG_CODE>");
					out.println("<PROD_NAME>" + encoder.encodeXMLAttribute(voResponse.getProdName()) + "</PROD_NAME>");
					
					String dacc = daoLookup.getDaccForEsnMeid(voResponse);					
					out.println("<DACC>" + encoder.encodeXMLAttribute(dacc) + "</DACC>");
					
					try{
						String deviceType=daoLookup.getDeviceMask(voResponse.getProdName(), voResponse.getMfgCode(), voResponse.getEffDate());
						if(deviceType!=null)
							out.println("<DEVICE_TYPE>" + encoder.encodeXMLAttribute(deviceType) + "</DEVICE_TYPE>");
						else
							out.println("<DEVICE_TYPE/>");
					}catch(Exception e){
						out.println("<DEVICE_TYPE/>");
					}
					esnMeidFound.add(new Boolean(true));

				}
				
				else {
					out.println("<MFG_CODE>Not Found</MFG_CODE>");
					out.println("<PROD_NAME>Not Found</PROD_NAME>");
					out.println("<DACC>Not Found</DACC>");
					out.println("<DEVICE_TYPE>Not Found</DEVICE_TYPE>");
					esnMeidFound.add(new Boolean(false));
				}
				
				out.println("</ESN_MEID_INFO>");		
			}

			statsLogBuf.append(esnMeids[0] + "|");
			
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
			
			out.println("</ESN_MEID_INFOS>");
			out.println("</DMD>");
		}
		
		else {
			  out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			  out.println("<DMD>");
			  out.println("		<STATUS>");
			  out.println("			<STATUS_STR>Not Found</STATUS_STR>"); 
			  out.println("			<MESSAGE>Please input ESN or MEID to search the database</MESSAGE>"); 
			  out.println("		</STATUS>");
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
