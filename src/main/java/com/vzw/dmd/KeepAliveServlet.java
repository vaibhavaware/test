/*
 * Created on Jan 30, 2012
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.vzw.dmd;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;

import com.vzw.dmd.util.*;

/**
 * @author c0palk1
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
@SuppressWarnings("serial")
public class KeepAliveServlet extends HttpServlet implements ILteXmlCreator {
	/**
	* @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	*/
	public void doGet(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException {
		defaultAction(req, res);
	}
	
	private static final String XML_START, XML_MID, XML_END;
	static {
		XML_START = XML_RESPONSE_START ;
		XML_MID = "</statusCode><message>";
		XML_END = "</message></responseHeader></dmd>";
	}

	/**
	* @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	*/
	public void doPost(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException {
		defaultAction(req, res);
	}

	/**
	* @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	*/
	public void defaultAction(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		String returnXML = null;
		ServletOutputStream out = res.getOutputStream();
		try{
			returnXML = XML_START + "00" + XML_MID + DBUtils.getDBTime() + ": Test" + XML_END;
			res.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			returnXML = XML_START + "01" + XML_MID + e.getMessage() + XML_END;
			res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		out.println(new XSSEncoder().encodeXML(returnXML));
		out.flush();
		out.close();
	}
	
}
