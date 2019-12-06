package com.vzw.dmd;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.vzw.dmd.dao.OracleDAOFactory;
import com.vzw.dmd.dao.OracleEsnLookupDAO;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.DMDUtils;
import com.vzw.dmd.util.XSSEncoder;

public class DMDEuimidImeiAssociation extends HttpServlet {
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
		StringBuffer statsLogBuf = new StringBuffer("XMLNEW|");
		Date entryTime = new Date();		
		try{
			String app_type = req.getParameter("NWK");
			String euimID = req.getParameter("Euimid");
			String retDoc = null;
			
			if(euimID != null) {
				euimID = euimID.trim();
				while(",".equals(euimID.substring(euimID.length()-1))) {
					euimID = euimID.substring(0, euimID.length()-1);
				}
				euimID = euimID.trim();
			}
			
			L.debug("euimID = (" + euimID + ")");
			if(euimID != null && !"".equals(euimID)) {
				try {
					OracleDAOFactory oracleDAOFactory = OracleDAOFactory.getInstance();
					OracleEsnLookupDAO daoLookup = (OracleEsnLookupDAO) oracleDAOFactory.createEsnLookupDAO();
					retDoc = daoLookup.getEuimidImeiAssociation(euimID);
				} catch(Exception e) {
					retDoc = "ERR:" + e.getMessage();
				}
			}else{
				statsLogBuf.append(app_type)
						.append("|")
						.append(DMDUtils.getClientIP(req))
						.append("|LOOKUP|EUIMID_IMEI_ASSOCIATION|NONE|FALSE|Invalid Euimid");
				L.debug("ERR: Please enter value for 'Euimid' parameter.");
				retDoc="Invalid input. Please put value for 'Euimid' parameter.";
			}			
						
			ServletOutputStream out = res.getOutputStream();
			//Fortify Fix - Cross-site scripting
			out.println(new XSSEncoder().encodeHTML(retDoc));
			out.flush();
			out.close();			
			
			statsLogBuf.append(app_type)
			.append("|")
			.append(DMDUtils.getClientIP(req))				
			.append("|LOOKUP|")
			.append("EUIMID_IMEI_ASSOCIATION|")			
			.append(euimID)
			.append("|");
			
			if(retDoc!=null)				
				statsLogBuf.append("TRUE|");
			else
				statsLogBuf.append("FALSE|Not found");

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
