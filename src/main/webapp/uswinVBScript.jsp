<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="com.vzw.dmd.util.XSSEncoder"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<%
		response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
		response.setHeader("Pragma","no-cache"); //HTTP 1.0
		response.setDateHeader ("Expires", 0); //prevent caching at the proxy server		
		
		if("VB_SCRIPT_JSP".equalsIgnoreCase(request.getParameter("srcPage"))) {
			return;// To stop recurssive call to this page
		}
%>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<title>Device Management Database</title>
<script language="VBScript">
<!--

sub fetch_uswin

	strComputer = "."   ' use "." for local computer 
	wbemImpersonationLevelImpersonate = 3
	wbemAuthenticationLevelPktPrivacy = 6

	' For error handling
	submitFlag = 0
	On Error Resume Next

	If Not strUser = "" Then

		Set objLocator = CreateObject("WbemScripting.SWbemLocator")
		Set objWMI = objLocator.ConnectServer _
			(strComputer, "root\cimv2", strUser, strPassword)
		objWMI.Security_.ImpersonationLevel = wbemImpersonationLevelImpersonate
		objWMI.Security_.AuthenticationLevel = wbemAuthenticationLevelPktPrivacy
		
	Else
		Set objWMI = GetObject("winmgmts:{impersonationLevel=impersonate}!\\" & strComputer & "\root\cimv2") 

	End If

	' Get OS name
	Set colOS = objWMI.InstancesOf ("Win32_OperatingSystem")

	For Each objOS in colOS
		 strName = objOS.Name
	Next

	If Instr(strName, "Windows 2000") > 0 Then
		Set colComputer = objWMI.ExecQuery("Select * from Win32_ComputerSystem")
		
		For Each objComputer in colComputer
			document.myForm.uswin.value = objItem.Name
			document.myForm.submit
			submitFlag = 1
			Exit Sub
		Next
	Else
		' ------------------------------------------------------------------
		' Code for Windows XP or later
		' ------------------------------------------------------------------
		
		' Get interactive session
		Set colSessions = objWMI.ExecQuery _ 
					  ("Select * from Win32_LogonSession Where LogonType = 2") 
		
		If colSessions.Count = 0 Then 
			' No interactive session found
			document.myForm.uswin.value = ""
		Else 
			'Interactive session found
			For Each objSession in colSessions 			
				Set colList = objWMI.ExecQuery("Associators of " _ 
				& "{Win32_LogonSession.LogonId=" & objSession.LogonId & "} " _ 
				& "Where AssocClass=Win32_LoggedOnUser Role=Dependent" ) 
										
				' Show user info
				For Each objItem in colList
					' MsgBox("No interactive user found: " & objItem.Name)
					If submitFlag = 0 Then
						document.myForm.uswin.value = objItem.Name
						document.myForm.submit
						submitFlag = 1
						Exit Sub
					End If
				Next
			Next 
		End If 
	End If

	If submitFlag = 0 Then
		document.myForm.uswin.value = ""
		document.myForm.submit
	End If

end sub

-->
</script> 
</head>
<body onload="fetch_uswin">
	<form name=myForm method="post" action="<%=new XSSEncoder().encodeHTML((String)request.getAttribute("REQUEST_URL")) %>">
		<input type="hidden" name="<csrf:tokenname/>" value="<csrf:tokenvalue uri="uswinVBScript"/>"/>
		<input type=hidden name="uswin" value=""/>
		<input type=hidden name="srcPage" value="VB_SCRIPT_JSP"/>
	</form>
</body>
</html>

