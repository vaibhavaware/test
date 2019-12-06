<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page import="com.vzw.dmd.util.*, org.apache.log4j.*, com.vzw.dmd.util.*" session="true" %>
<%
		response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
		response.setHeader("Pragma","no-cache"); //HTTP 1.0
		response.setDateHeader ("Expires", 0); //prevent caching at the proxy server		

		//String app_type = request.getParameter("app_type");
		String app_type = DMDRequestFilter.getRequestPareameter( request, "app_type" );
		if (app_type == null || app_type.trim().equals(""))
			app_type="UNKNOWN";
 %>			
 <%!
 	static Logger L = Logger.getLogger(DMDLogs.logBase + ".index_jsp");
 	java.io.PrintWriter tmpOut = null;
 %>
	<html xmlns="http://www.w3.org/1999/xhtml">

	<% 
		try {
			tmpOut = new java.io.PrintWriter(out);
			DMDHtmlUtils.print_javascript(tmpOut);
			tmpOut.flush();
		 } catch(Exception e) {
			L.error("Exception in printing javascript part", e);
		 }
	%>

	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
		<meta name="GENERATOR" content="IBM WebSphere Studio" />
		<meta http-equiv="Content-Style-Type" content="text/css" />
		<link href="theme/Master.css" rel="stylesheet" type="text/css" />
		<link href="theme/vzw.css" rel="stylesheet" type="text/css" />
		<title>Device Management Database</title>
	</head>
	<body bgcolor="#ffffff" leftmargin="0" topmargin="0">
		<table cellpadding="0" cellspacing="0" border="0">
			<tr>
				<td valign="top"  bgcolor="black"><img alt="Verizon" src="images/vzwLogo.png" border="0" /></td>
				<td><img src="images/3top_graphic.jpg" alt="Verizon" border="0" /></td>
			</tr>
			<tr>
			
				<% 
					try {
						DMDHtmlUtils.print_left_bar(request, tmpOut);
						tmpOut.flush();
					 } catch(Exception e) {
						L.error("Exception in printing javascript part", e);
					 }
				%>
				<td  valign="top">
					<table cellpadding="0" cellspacing="0" border="0">
						<tr><td valign="top" align="center">
							<img src="images/dmdLogo.jpg" border="0" />
						</td></tr>
					</table>
				</td>
			</tr>
		</table>
	</body>
</html>

