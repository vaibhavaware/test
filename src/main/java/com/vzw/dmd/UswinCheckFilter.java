package com.vzw.dmd;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

import com.vzw.dmd.util.*;

/**
 * @version 	1.0
 * @author 		Kaladhar
 */
@SuppressWarnings("serial")
public class UswinCheckFilter implements Filter {
	private static Logger L = Logger.getLogger(DMDLogs.getLogName(UswinCheckFilter.class));
	private ServletContext application = null;
	
	public boolean defaultFilter(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String auth = request.getHeader("Authorization");
		if(auth == null) {
			auth = "";
		} else {
			auth = auth.trim();
		}
		
		if (!auth.startsWith("NTLM ")) {
			String uswin = (String)request.getParameter("uswin");
			if(uswin != null && !uswin.trim().equals("")) {
				String authMode = "USING_PARAMETER";
				if("VB_SCRIPT_JSP".equalsIgnoreCase(request.getParameter("srcPage"))) {
					authMode = "USING_VB_SCRIPT";
				}
				DBUtils.saveLoginInfo(uswin, request.getParameter("app_type"), authMode, getClientIP(request));
				request.setAttribute("uswin", uswin.trim());
				return true;
			}
			
			uswin = getCookieValue(request, "DMD_USER", null);
			if(uswin != null && !uswin.equals("")) {
				request.setAttribute("uswin", uswin.trim());
				return true;
			}			
		}		
		
		String authType = DBUtils.getDBPropertyValue("GUI_AUTH_CHECK_TYPE");
		boolean authCheckEnabled = "Y".equalsIgnoreCase(DBUtils.getDBPropertyValue("GUI_AUTH_CHECK_ENABLE"));
		
		if(authType != null) {
			authType = authType.trim();
			if(authType.equalsIgnoreCase("NTLM")) {
				if(doNtlmAction(request, response)) {
					DBUtils.saveLoginInfo((String)request.getAttribute("uswin"), request.getParameter("app_type"), "USING_NTLM", getClientIP(request));
					return true;
				}
				return false;
			} else if(authType.equalsIgnoreCase("VB_SCRIPT")) {
				String queryString = request.getQueryString();
				if(queryString != null) {
					queryString = queryString.trim(); 
				} else {
					queryString = "";
				}
				
				if(!queryString.equalsIgnoreCase("")) {
					queryString = "?" + queryString;
				}
				
				if(!"VB_SCRIPT_JSP".equalsIgnoreCase(request.getParameter("srcPage"))) {
					request.setAttribute("REQUEST_URL", request.getRequestURI() + queryString);
					application.getRequestDispatcher("uswinVBScript.jsp").forward(request, response);
					return false;
				}
			}
		}
		
		if(!authCheckEnabled) {
			request.setAttribute("uswin", "NO_AUTH_CHECK");
			return true;
		}
		
		throw new Exception("Unable to authenticate user");
	}
	
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) 
	throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req; 
		HttpServletResponse response = (HttpServletResponse) resp;
		
		try {
			if(defaultFilter(request, response)) {
				addCookie(request, response, "DMD_USER", (String)request.getAttribute("uswin"));
				chain.doFilter(req, resp);
			}
		} catch(Exception e) {
			L.error("Unable to autheticate user: ", e);
			application.getRequestDispatcher("authError.jsp").forward(request, response);
		}		
	}
	
	public void init(FilterConfig config) throws ServletException {
		application = config.getServletContext();
	}
	
	public void destroy() {
		
	}
	
	public boolean doNtlmAction(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String auth = request.getHeader("Authorization");
		
		L.debug("Auth token: " + auth);
		if (auth == null) { // STEP: 1
		   response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		   response.setHeader("WWW-Authenticate", "NTLM");		   
		   response.flushBuffer();
		   
		   L.debug("Step 1 response sent; servlet path " + request.getServletPath());
		   return false;
		}
		
		if (auth.startsWith("NTLM ")) {
		  try {
		      byte[] msg = new sun.misc.BASE64Decoder().decodeBuffer(auth.substring(5));
		      int off = 0, length, offset;
		      if (msg[8] == 1) { // STEP: 2
		        byte z = 0;
		        byte[] msg1 = {(byte)'N', (byte)'T', (byte)'L', (byte)'M', (byte)'S', (byte)'S', (byte)'P', z,(byte)2, z, z, z, z, z, z, z,(byte)40, z, z, z, (byte)1, (byte)130, z, z,z, (byte)2, (byte)2, (byte)2, z, z, z, z, z, z, z, z, z, z, z, z};
		        String encodeRes = "NTLM " + new sun.misc.BASE64Encoder().encode(msg1);
		         
		        
		        response.setHeader("WWW-Authenticate", encodeRes); 
		        response.setHeader("Content-Type", "text/html; charset=UTF-8"); 
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.flushBuffer();

				L.debug("Step 2 response sent; servlet path " + request.getServletPath());
		        return false;
		      } else if (msg[8] == 3) { // STEP: 3
		        off = 30;
		    
		        length = msg[off+9]*256 + msg[off+8];
		        offset = msg[off+11]*256 + msg[off+10];
		     
		        String uswin = cleanCTL(new String(msg, offset, length)); 
		        uswin = new String(uswin.getBytes(), "US-ASCII");
		        request.setAttribute("uswin", uswin);
		        
		        return true;
		      }
		    }
		    catch(Exception e) {
		    	L.error("Error in doNtlmAction: " + e.getMessage(), e);
		    	throw e;
		    }
		}
		
		throw new Exception("Unreachable code reached in doNtlmAction");
	}
	
    public static String cleanCTL(String value) { /* NEED TO STRIP OUT SPECIAL CHARS */
    	StringBuilder text = new StringBuilder();
	    if( value==null) { 
	    	return "";
	    }
	    
	    int len = value.length();
	    for (int i = 0; i < len; i++) {
	    	char c = value.charAt(i);
	    	if (c < 0x20 || c >= 0x7f) {
	    		if (c == 0x09) {
	    			text.append(c);
	    		}
	    	} else {
	    		text.append(c);
	    	}
	    }
	    return text.toString();
    }
    
    private static void addCookie(HttpServletRequest request, HttpServletResponse response, String cookieName, String value) {
    	Cookie cookie = getCookie(request, cookieName);
    	//Header Manipulation: Cookies Fix Starts
    	cookieName = ESAPIValidationUtils.getValidContent(cookieName);
    	value = ESAPIValidationUtils.getValidContent(value);
    	//Header Manipulation: Cookies Ends
    	if(cookie == null) {
	    	cookie = new Cookie(cookieName, value);
	    	cookie.setMaxAge(-1);
	    	response.addCookie(cookie);
    	} else {
    		cookie.setValue(value);
    	}
    }

	private static String getCookieValue(HttpServletRequest request, String cookieName, String defaultValue) {
		Cookie cookie = getCookie(request, cookieName);
		if(cookie == null) {
			return defaultValue;
		}
		
		if(cookie.getValue() != null)
			return cookie.getValue().trim();
		
		return defaultValue;
	}	

	private static Cookie getCookie(HttpServletRequest request, String cookieName) {
		Cookie[] cookies = request.getCookies();
		if(cookies == null) {
			return null;
		}
		
		for(int i=0; i<cookies.length; i++) {
			Cookie cookie = cookies[i];
			if (cookieName.equals(cookie.getName())) {
				return cookie;
			}
		}
		return null;
	}
	
	public static String getClientIP(HttpServletRequest req){
		String xff = null;
		String clientIP = null;

		String[] xffValues = null;

		try{
			xff=req.getHeader("X-Forwarded-For");

			if(xff==null || xff.trim().equals(""))
				clientIP = req.getRemoteAddr().trim();
			else{
				xffValues = xff.split(",");
				if(xffValues.length<1) {
					clientIP = req.getRemoteAddr().trim();
					//AceLogWriter.logDebug(cID, "X-Forward-For header contianed no values== returning from request.getRemoteAddr: " + clientIP);
				}else{
					// HEADER VALUES SHOULD BE IN THE FOLLOWING FORMAT
					// X-Forwarded-For: proxy1, proxy2, ...,  client1

					clientIP = xffValues[xffValues.length-1].trim(); // return the clientIP

					//AceLogWriter.logDebug(cID, "X-Forward-For header contianed: " + xff + " == returning from last value: " + clientIP);
				}
			}
		}catch(Exception e){
			clientIP = req.getRemoteAddr();
			L.error("Exception while attempting to return client IP from X-Forward-For header == returning from request: " + clientIP);
		}
		
		return clientIP;
	}	
}
