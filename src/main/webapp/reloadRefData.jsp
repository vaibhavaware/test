<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ page language="java" 
	import="java.util.*, com.vzw.dmd.util.*"
	contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<HTML>
<HEAD>
<META http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<META name="GENERATOR" content="IBM Software Development Platform">
<META http-equiv="Content-Style-Type" content="text/css">
<% 
	DMDRefData.reload();
%>
<LINK href="theme/Master.css" rel="stylesheet" type="text/css">
<TITLE>reload.jsp</TITLE>
</HEAD>
<BODY>
<%
	XSSEncoder encoder = new XSSEncoder();	
	ArrayList modes = DMDRefData.getModesList();
	ArrayList models = DMDRefData.getModelsList();
	ArrayList mfgs = DMDRefData.getMfgsList();
	Properties capabilities = DMDRefData.getCapProps();
	Properties xslProps = DMDRefData.getXmlProps();
%>
<B>Modes:</B>
<BR><%=encoder.encodeHTML(modes.toString()) %>
<BR><B>Models:</B>
<BR><%=encoder.encodeHTML(models.toString()) %>
<BR><B>Mfgs:</B>
<BR><%=encoder.encodeHTML(mfgs.toString()) %>
<BR><B>Capabilies:</B>
<BR><%=encoder.encodeHTML(capabilities.toString()) %>
<BR><B>xslProps:</B>
<BR><%=encoder.encodeHTML(xslProps.toString()) %>
</BODY>
</HTML>
