<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ page language="java" 
	import="java.util.*, com.vzw.dmd.util.*"
	contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%
	DMDProps.load();
	XSSEncoder encoder = new XSSEncoder();
	String imageURL = encoder.encodeHTML(DMDProps.getEmbededUsrIdImgURL());
	String esnMeidUpd = encoder.encodeHTML(DMDProps.getEsnMeidMultiUpdate());
	String esnMeidLock = encoder.encodeHTML(DMDProps.getEsnMeidLockXA());
%>
<HTML>

<HEAD>
<META http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<META name="GENERATOR" content="IBM Software Development Platform">
<META http-equiv="Content-Style-Type" content="text/css">
<LINK href="theme/Master.css" rel="stylesheet" type="text/css">
<TITLE>reloadProps.jsp</TITLE>
</HEAD>
<BODY>
<B>Properties:</B>
<%
	Properties props = DMDProps.getPropObject();
	String ignoreProps[] = {"DBUID", "ORACONNSTR", "DBPASSWD", "DBPASSWD1", "DBUID1", "POS_DB_URL", "POS_DB_USER", "POS_DB_PASSWD"};
	if (props != null)
	{
		Enumeration en = props.keys();
		while (en.hasMoreElements())
		{
			String key = (String)en.nextElement();
			String val = props.getProperty(key);
			if(key != null) {
				key = key.trim();
			}
			boolean found = false;
			for(int i=0; i<ignoreProps.length; i++) {
				if(ignoreProps[i].equalsIgnoreCase(key)) {
					found = true;
				}
			}
			
			if(found) {
				continue; // Do not display in result
			}
%>
	<BR><B><%=key%></B>  = <%=val%>
<%
		}
	}
%>
	<BR><B>EMBEDED_USRID_IMG_URL</B>  = <%=imageURL%>
	<BR><B>ESN_MEID_MULTI_UPDATE</B>  = <%=esnMeidUpd %>
	<BR><B>ESN_MEID_LOCK_XA</B>  = <%=esnMeidLock %>
</BODY>
</HTML>
