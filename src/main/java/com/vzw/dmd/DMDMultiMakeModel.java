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

import com.vzw.dmd.ejb.EsnMeidLookupLocal;
import com.vzw.dmd.ejb.EsnMeidLookupLocalHome;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.DMDUtils;
import com.vzw.dmd.util.XSSEncoder;
import com.vzw.dmd.valueobject.EsnLookupRequestVO;

public class DMDMultiMakeModel extends HttpServlet {
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
		InitialContext ic = null;
		EsnMeidLookupLocalHome ejbHome = null;
		EsnMeidLookupLocal ejb = null;
		try{
			ic = new InitialContext();
			ejbHome =(EsnMeidLookupLocalHome) ic.lookup(
				"java:comp/env/ejb/EsnMeidLookup");
			ejb = ejbHome.create();
			String app_type = req.getParameter("NWK");
			String esn_meid = req.getParameter("esn_meid");
			String device_id = req.getParameter("deviceID");
			String retDoc=null;
			if(device_id != null && !("").equals(device_id)){
				esn_meid = device_id.trim();
			}
			if(esn_meid==null || device_id == null)
				esn_meid = req.getParameter("ESN_MEID");
			
			L.debug("esn_meid = (" + esn_meid + ")");
			if(esn_meid != null && !esn_meid.trim().equals("")){
				retDoc=ejb.getMultiModelInfo(esn_meid);
			}else{
				statsLogBuf.append(app_type)
						.append("|")
						.append(DMDUtils.getClientIP(req))
						.append("|LOOKUP|MULTI_MODEL|NONE|FALSE|Invalid ESN or MEID");
				L.debug("ERR: Please enter value for 'esn_meid' parameter.");
				retDoc="Invalid input. Please put value for esn_meid parameter.";
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
			.append("MULTI_MODEL|")			
			.append(esn_meid)
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
