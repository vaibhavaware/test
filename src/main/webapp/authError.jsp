<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="com.vzw.dmd.util.*, java.io.*"%>
<%
	PrintWriter pout = new PrintWriter(out);
	DMDHtmlUtils.print_header(pout);
	DMDHtmlUtils.print_left_bar(request, pout);
	pout.flush();
%>
	<td  valign='top'>
		<br>
		<h3 style="color: red">&nbsp;Unable to authenticate user. Please contact DMD support.</h3><br>
		<table cellpadding='0' cellspacing'0' border='0'>
			<tr><td valign='top' align='center'>&nbsp;<img src='images/phone.gif' border='0'></td></tr>
		</table>
	</td></tr></table>
	
	<script type="text/javascript">
		document.dmdForm.esn_dec.disabled=true;
		document.dmdForm.esn_hex.disabled=true;
		document.dmdForm.search.disabled=true;
		document.resetForm.subButton.disabled=true;
		document.fdbkForm.subButton.disabled=true;		
		document.dmdForm.deviceCategory.disabled=true;
		document.dmdForm.modelname.disabled=true;
		document.dmdForm.mfg.disabled=true;
		document.dmdForm.reporttype.disabled=true;		
		document.dmdForm.eqp_mode.disabled=true;
		document.dmdForm.s_feature.disabled=true;
		document.dmdForm.tmp_deviceId.disabled=true;	
		document.dmdForm.tmp_deviceIdType.disabled=true;
	</script>
</body></html>

