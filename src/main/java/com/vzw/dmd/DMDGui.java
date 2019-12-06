package com.vzw.dmd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.InitialContext;
import javax.servlet.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.vzw.dmd.dao.OracleDAOFactory;
import com.vzw.dmd.dao.OracleEsnLookupDAO;
import com.vzw.dmd.ejb.EsnMeidLookupLocal;
import com.vzw.dmd.ejb.EsnMeidLookupLocalHome;
import com.vzw.dmd.util.*;
import com.vzw.dmd.valueobject.DMDHubMacIdLookupRequestVO;
import com.vzw.dmd.valueobject.EsnLookupRequestVO;

/**
 * @version 	1.0
 * @author 		Bhaskar
 */
public class DMDGui extends HttpServlet
{
	private static Logger L = Logger.getLogger(DMDLogs.getLogName(DMDGui.class));
	XSSEncoder encoder = new XSSEncoder();
	
	/**
	* @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	*/
	public void doGet(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
		defaultAction(req, res);
	}

	/**
	* @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	*/
	public void doPost(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
		defaultAction(req, res);
	}

	/**
	* @see javax.servlet.GenericServlet#void ()
	*/
	public void init() throws ServletException
	{
		super.init();
	}

	/**
	* @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	*/
	public void defaultAction(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
		StringBuffer statsLogBuf = new StringBuffer("GUI|");
		StringBuffer xslTypeKey = new StringBuffer("");
		Date entryTime = new Date();
		
		String uswin = (String)req.getAttribute("uswin");
		res.setHeader("Cache-Control","no-cache"); //HTTP 1.1
		res.setHeader("Pragma","no-cache"); //HTTP 1.0
		res.setDateHeader ("Expires", 0); //prevent caching at the proxy server						
		res.setContentType("text/html");
		try
		{
			L.info("DMDGui "+"Inside Default Action");
			L.debug("DMDGui "+"Inside Default Action");
			
			L.debug("deviceIdType***-"+req.getParameter("deviceIdType"));
			L.debug("deviceId***-"+req.getParameter("deviceId"));
			//System.out.println("***-"+);
			/**
			 * BQVT-403
			 */
			if(null != req.getParameter("deviceIdType") && req.getParameter("deviceIdType").equalsIgnoreCase("IMEI") ){
				L.debug("deviceIdType is not null***-"+req.getParameter("deviceIdType"));
				L.debug("validation for alphanumeric device id ");
				if(null != req.getParameter("deviceId") && !isNan(req.getParameter("deviceId"))){
					L.debug("deviceId is not null***-"+req.getParameter("deviceId"));
					L.debug("input is not numeric");
					L.debug("handleError for alphanumeric device id ");
					handleError(req, res,"Please enter a valid numeric device id");
					return;
				}else {
					L.debug("deviceId is  null***-");
					L.debug("isNan validation***-"+isNan(req.getParameter("deviceId")));
				}
			}
			else {
				L.debug("deviceIdType is  null");
			}
			
			 /**  **/
			
			EsnLookupRequestVO reqVO = new EsnLookupRequestVO();
			String xslType = xslType(req, reqVO, xslTypeKey, statsLogBuf);
			L.debug("xsl Type : "+xslType);
			if (xslType == null || xslType.trim().equals(""))
			{
				L.error(
					"No XSL Mapping found for the request. xslKey = "
						+ xslTypeKey.toString()
						+ "; Can't Continue.;");
				StringBuffer errBuf = new StringBuffer();
				String idType = reqVO.getIdType();
				if (idType != null && idType.equalsIgnoreCase("MEID")
					&& reqVO.getId() != null
					&& !reqVO.getId().trim().equals(""))
				{
					errBuf.append("No results found for MEID " + reqVO.getId());
				}
				else
					if (idType != null
						&& idType.equalsIgnoreCase("ESN")
						&& reqVO.getId() != null
						&& !reqVO.getId().trim().equals(""))
					{
						errBuf.append("No results found for DeviceID " + reqVO.getId());
					}
					else
					{
						errBuf.append("Please enter ");
						if (DMDProps.isMeidEnabled())
						{
							errBuf.append("ESN/MEID");
						}
						else
						{
							errBuf.append("ESN");
						}
						errBuf.append(
							" or select Model to search Device Management Database.");
						errBuf.append(
							"Or select Manufacturers, Equipment Mode, or Capabilities ");
						errBuf.append("to search phones.");
					}
				handleError(req, res, errBuf.toString());
				return;
			}
			
			
			
			InitialContext ic = new InitialContext();
			EsnMeidLookupLocalHome ejbHome =
				(EsnMeidLookupLocalHome) ic.lookup(
					"java:comp/env/ejb/EsnMeidLookup");
			EsnMeidLookupLocal ejb = ejbHome.create();
			Document retDoc = null;
			String searchStatus = null;
			String statusMsg = null;
			if (reqVO.getIdType().equalsIgnoreCase("other"))
			{
				retDoc = ejb.getGuiXmlByOthers(reqVO);
				Element rootEle = retDoc.getDocumentElement();
				if (rootEle.getTagName().equalsIgnoreCase("equipment"))
				{
					xslType = DMDRefData.getXslProperty("gui-error");
					searchStatus =
						XmlUtils.getValue(
							retDoc,
							"/equipment/search_status/search_result_status/text()");
					statusMsg =
						XmlUtils.getValue(
							retDoc,
							"/equipment/search_status/status/status_message/text()");
				}
				else
				{
					searchStatus = "true";
					statusMsg = "";
				}
				
				try {
					retDoc.getFirstChild().appendChild(DmdXmlCreatorHelper.createElement(retDoc, "uswin", uswin));
					retDoc.getFirstChild().appendChild(DmdXmlCreatorHelper.createElement(retDoc, "uswin_image_url", DMDProps.getEmbededUsrIdImgURL() + uswin));
					
				} catch(Exception e) {
					// Ignore exception
					L.error("Unable to add uswun/uswin_image_url element: " + e.getMessage(), e);
				}
			}
			else if(reqVO.getIdType().equalsIgnoreCase("NEMACID")){
				L.debug("DMDGui "+"NEMACID");
				retDoc = EsnMeidLookupBeanHelper.prepareGuiDocByNEMACID(reqVO);
				ServletOutputStream out = res.getOutputStream();
				//DMDHtmlUtils.print_header(out);
				//DMDHtmlUtils.print_left_bar(req, out);
				String stylePath =
					getServletContext().getRealPath(
						"/WEB-INF/xslt/" + xslType.trim());				
				searchStatus = "true";
				statusMsg = "";
			}
			else
			{ 
				L.info("ejb.getGuiXml(reqVO)*");
				retDoc = ejb.getGuiXml(reqVO);
				searchStatus =
					XmlUtils.getValue(
						retDoc,
						"/equipment/search_status/search_result_status/text()");
				statusMsg =
					XmlUtils.getValue(
						retDoc,
						"/equipment/search_status/status/status_message/text()");
				
				String paramId = req.getParameter("deviceId");
				
				String tmpDevType = XmlUtils.getValue(
						retDoc,
				"/equipment/device_type/text()");
				
				if(tmpDevType != null && reqVO != null
						&& reqVO.getId() != null //&& reqVO.getId().trim().length() == 14
						&& !"false".equalsIgnoreCase(searchStatus)) {
					if(!tmpDevType.trim().startsWith("4G")) {
						if("IMEI".equals(reqVO.getIdType())) {
							reqVO.setIdType("MEID");
							req.setAttribute("DEVICE_ID_TYPE", "MEID");
						} else if("MEID".equals(reqVO.getIdType())) {
							reqVO.setIdType("MEID");
							req.setAttribute("DEVICE_ID_TYPE", "MEID");							
						}
					}
					else {
						reqVO.setIdType("IMEI");
						req.setAttribute("DEVICE_ID_TYPE", "IMEI");
					}
				}
				
				if(reqVO != null && "false".equalsIgnoreCase(searchStatus) && paramId != null) {
					String id = reqVO.getId();
					if(id != null && id.trim().length() == 14 && "IMEI".equalsIgnoreCase(reqVO.getIdType()) 
										&& paramId.trim().equalsIgnoreCase(id.trim())) {
						try {
							reqVO.setIdType("MEID");
							reqVO.setId(DMDUtils.convertMEIDFromHexToDecimal(id.trim()));
							Document tmpRetDoc = ejb.getGuiXml(reqVO); 
							String tmpSearchStatus = XmlUtils.getValue( tmpRetDoc
									                                  , "/equipment/search_status/search_result_status/text()");
							if(!"false".equalsIgnoreCase(tmpSearchStatus)) {
								retDoc = tmpRetDoc;
								searchStatus = tmpSearchStatus;
								statusMsg = XmlUtils.getValue( retDoc
										                     , "/equipment/search_status/status/status_message/text()");
								req.setAttribute("DEVICE_ID_TYPE", "MEID");
							} else {
								reqVO.setId(id);
								reqVO.setIdType("IMEI");
							}
						} catch(Exception e) {
							reqVO.setId(id);
							reqVO.setIdType("IMEI");
						}
					}
				}
				
				try {
					retDoc.getFirstChild().appendChild(DmdXmlCreatorHelper.createElement(retDoc, "uswin", uswin));
					retDoc.getFirstChild().appendChild(DmdXmlCreatorHelper.createElement(retDoc, "uswin_image_url", DMDProps.getEmbededUsrIdImgURL() + uswin));
					
				} catch(Exception e) {
					// Ignore exception
					L.error("Unable to add uswun/uswin_image_url element: " + e.getMessage(), e);
				}				
			}
			statsLogBuf.append(searchStatus.trim().toUpperCase());
			statsLogBuf.append("|");
			statsLogBuf.append(statusMsg.trim());
			L.debug(
				"DMDGui, return lookup for esn:\n"
					+ XmlUtils.convertToString(retDoc, true));
			String stylePath =
				getServletContext().getRealPath(
					"/WEB-INF/xslt/" + xslType.trim());
			res.setContentType("text/html");
			ServletOutputStream out = res.getOutputStream();
			DMDHtmlUtils.print_header(out);
			DMDHtmlUtils.print_left_bar(req, out);
			if (reqVO.getIdType().equalsIgnoreCase("other"))
			{

				out.println(
					"<td valign=\"top\" width=\"800\" height=\"1000\" ><br />");
				out.print(
					"<span class=\"verdanaBlue10\">Device Equipment Lookup by: ");
				StringBuffer qStr = new StringBuffer();
				String capQ = reqVO.getCapabilitiesGui();
				String mfgQ = reqVO.getManufacturerGui();
				String eqpQ = reqVO.getEquipmentModeGui();

				if (mfgQ != null && !mfgQ.trim().equals(""))
					qStr.append(mfgQ.trim());
				if (eqpQ != null && !eqpQ.trim().equals(""))
				{
					if (qStr.length() > 0)
						qStr.append(", ");
					qStr.append(eqpQ);
				}
				if (capQ != null && !capQ.trim().equals(""))
				{
					if (qStr.length() > 0)
						qStr.append(", ");
					qStr.append(DMDRefData.getCapabilityKey(capQ));
				}
				out.println(
						encoder.encodeHTML(qStr.toString())
						+ "</span><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");
			}
			try
			{
				//L.info("newTransformer");
				Transformer trans = StylesheetCache.newTransformer(stylePath);
				//L.info("done newTransformer");
				OutputFormat fmt = new OutputFormat("xml", "UTF-8", false);
				//L.info("1");
				StringWriter wri = new StringWriter();
				//L.info("2");
				XMLSerializer ser = new XMLSerializer(wri, fmt);
				//L.info("3");
				ser.serialize(retDoc);
				//L.info("4");
				ByteArrayInputStream origXMLIn =
					new ByteArrayInputStream(wri.toString().getBytes());
				//L.info("6");
				Source styleSource = new StreamSource(origXMLIn);
				//L.info("7");
				ByteArrayOutputStream resultBuf = new ByteArrayOutputStream();
				//L.info("8");
				trans.transform(styleSource, new StreamResult(resultBuf));
				//L.info("9");
				out.write(resultBuf.toByteArray());
				//L.info("10");
				out.flush();
				//L.info("11");
				res.flushBuffer();
				//L.info("12");
			}
			catch (Exception e)
			{
				L.error("Exception during Transformation", e);
				out.println("<b>Error:" + e.getMessage() + "</b>");
				res.flushBuffer();
			}
			if (reqVO.getIdType().equalsIgnoreCase("other"))
			{
				out.println("</td></tr>");
				/*
				out.print("<tr><td align=\"center\"><a href=\"");
				out.print(DMDProps.getProperty("MICC_LINK") + "\">");
				out.println(
					"<span class=\"verdanaBlack10\">Click here to link to MICC OLR phone page</span></a></td></tr>");
				*/
				out.print("<tr><td align=\"center\"><a href=\"");
				out.print(DMDProps.getProperty("MA_LINK") + "\">");
				out.println(
					"<span class=\"verdanaBlack10\">Click here to link to Marketing's equipment site</span></a></td></tr>");
				out.println("</table></td>");
			}
		}
		catch (Exception e)
		{
			L.error("Exception=", e);
		}
		finally
		{
		    Date exitTime = new Date();
		    long prcTime = exitTime.getTime() - entryTime.getTime();
			DMDLogs.getStatsLogger().info(statsLogBuf.toString());
			DMDLogs.getEStatsLogger().info(
                    statsLogBuf.toString() + "|"
                            + DMDProps.ldf.format(entryTime) + "|"
                            + DMDProps.ldf.format(exitTime) + "|" + prcTime);
		}
	}
	
	/**
	 * Method to validate if a device id is numeric 
	 */
	private boolean isNan(String deviceIdString) {
		return deviceIdString.matches("^[-+]?\\d+(\\.\\d+)?$");
	}

	private String xslType(
		HttpServletRequest req,
		EsnLookupRequestVO reqVO,
		StringBuffer xslType,
		StringBuffer statsBuf)
	{
		boolean euimidSearch = false;
		
		L.debug("DMDGui "+"Inside xslType");
		xslType.append("gui-");
		String deviceIdType = req.getParameter("deviceIdType");
		L.debug("DMDGui "+"Inside xslType deviceIdType : "+deviceIdType);
		/*
		String esn_dec = req.getParameter("esn");
		String esn_hex = req.getParameter("esn_hex");
		String meid_hex = req.getParameter("meid");
		String macid = req.getParameter("macid");
		String pmacid = req.getParameter("pmacid");
		*/
		String esn_dec = "";
		String esn_hex = "";
		String meid_hex = "";
		String macid = "";
		String pmacid = "";
		String nemacid = "";
		String iccid = "";
		String imei = "";	
		String mac4 = "";
		String deviceId = req.getParameter("deviceId");
		L.info("deviceIdType*-"+deviceIdType);
		
		if(deviceId != null){
			deviceId = req.getParameter("deviceId").trim().toUpperCase();	
		}
		L.info("deviceId*-"+deviceId);
		if(deviceIdType == null || deviceIdType.trim().equals("")){
			L.debug("DMDGui "+"Inside xslType deviceIdType is null : ");
			String tdeviceId = req.getParameter("deviceId");
			L.info("tdeviceId*-"+tdeviceId);
			if(tdeviceId == null || tdeviceId.trim().equals("")){
				L.info("tdeviceId == null ");
				deviceIdType = "";
				deviceId = "";

				L.debug("DMDGui "+"Inside xslType deviceIdType tdeviceId is null  ");
				if(req.getParameter("esn") != null && !req.getParameter("esn").trim().equals("")){
					deviceIdType = "ESNDEC";
					deviceId = req.getParameter("esn").trim().toUpperCase();
				}
				if(req.getParameter("esn_hex") != null && !req.getParameter("esn_hex").trim().equals("")){
					deviceIdType = "ESNHEX";
					deviceId = req.getParameter("esn_hex").trim().toUpperCase();
				}				
				if(req.getParameter("meid") != null && !req.getParameter("meid").trim().equals("")){
					deviceIdType = "MEID";
					deviceId = req.getParameter("meid").trim().toUpperCase();
				}
				if(req.getParameter("macid") != null && !req.getParameter("macid").trim().equals("")){
					deviceIdType = "HUBMACID";
					deviceId = req.getParameter("macid").trim().toUpperCase();
				}
				if(req.getParameter("pmacid") != null && !req.getParameter("pmacid").trim().equals("")){
					deviceIdType = "HUBPESN";
					deviceId = req.getParameter("pmacid").trim().toUpperCase();
				}
				if(req.getParameter("imei") != null && !req.getParameter("imei").trim().equals("")){
					deviceIdType = "IMEI";
					deviceId = req.getParameter("imei").trim().toUpperCase();
				}	
				if(req.getParameter("iccid") != null && !req.getParameter("iccid").trim().equals("")){
					deviceIdType = "ICCID";
					deviceId = req.getParameter("iccid").trim().toUpperCase();
				} 			
				if(req.getParameter("EUIMID") != null && !req.getParameter("EUIMID").trim().equals("")){
					euimidSearch = true;
					deviceIdType = "ICCID";
					deviceId = req.getParameter("EUIMID").trim().toUpperCase();
				}
				if(req.getParameter("nemacid") != null && !req.getParameter("nemacid").trim().equals("")){
					deviceIdType = "NEMACID";
					deviceId = req.getParameter("NEMACID").trim().toUpperCase();
				}				
				L.debug("DMDGui "+"Inside xslType deviceIdType is NULL 2 : ");
			}else{
				L.debug("DMDGui "+"Inside xslType deviceIdType is not null : ");
				deviceIdType = DMDUtils.getGUIDeviceIDType(req.getParameter("deviceId").trim().toUpperCase());
				if(deviceIdType == null){
					deviceIdType = "";
					esn_dec = "";
				}				
			}
		}
		
		
		if(deviceIdType.trim().equalsIgnoreCase("ESNDEC")){
			esn_dec = deviceId;
		}else if(deviceIdType.trim().equalsIgnoreCase("ESNHEX")){
			esn_hex = deviceId;
		}else if(deviceIdType.trim().equalsIgnoreCase("MEID")){
			meid_hex = deviceId;
		}else if(deviceIdType.trim().equalsIgnoreCase("HUBMACID")){
			macid = deviceId;
		}else if(deviceIdType.trim().equalsIgnoreCase("HUBPESN")){
			pmacid = deviceId;
		}else if(deviceIdType.trim().equalsIgnoreCase("NEMACID")){
			nemacid = deviceId;
		}else if(deviceIdType.trim().equalsIgnoreCase("IMEI")){
			imei = deviceId;
		}else if(deviceIdType.trim().equalsIgnoreCase("ICCID")){
			iccid = deviceId;
		}else if(deviceIdType.trim().equalsIgnoreCase("MACID")){
			mac4 = deviceId;
		}else if(deviceIdType.trim().equalsIgnoreCase("IMSI1")){
			
		}else if(deviceIdType.trim().equalsIgnoreCase("IMSI2")){
			
		}else if(deviceIdType.trim().equalsIgnoreCase("EUIMID")){
			deviceIdType = "ICCID";
			iccid = deviceId;
			euimidSearch = true;
		}
		String model_name = req.getParameter("modelname");
		String report_type = req.getParameter("reporttype");
		String mfg_name = req.getParameter("mfg");
		String eqp_mode = req.getParameter("eqp_mode");
		String capabilities = req.getParameter("s_feature");
		L.debug("req:deviceIdType=" + deviceIdType);
		L.debug("req:esn=" + esn_dec);
		L.debug("req:esn_hex=" + esn_hex);
		L.debug("req:meid=" + meid_hex);
		L.debug("req:iccid=" + iccid);
		L.debug("req:modelname=" + model_name);
		L.debug("req:reporttype=" + report_type);
		L.debug("req:mfg=" + mfg_name);
		L.debug("req:eqp_mode=" + eqp_mode);
		L.debug("req:s_feature=" + capabilities);
		L.debug("req:macid=" + macid);
		L.debug("req:pmacid=" + pmacid);
		L.debug("req:nemacid=" + nemacid);
		L.debug("req:macid=" + mac4);
		if (report_type == null || report_type.trim().equals(""))
			report_type = "features";
		req.getSession().removeAttribute("reqmacid");
		req.getSession().removeAttribute("reqpmacid");
		if (macid != null && !macid.trim().equals("")){
			{
				
				reqVO.setIdType("ESN");
				statsBuf.append("Macid");
				statsBuf.append("_" + report_type.trim().toUpperCase());
				statsBuf.append("|");
				xslType.append("esn_" + report_type.trim().toLowerCase());
				OracleDAOFactory oracleDAOFactory = OracleDAOFactory.getInstance();
				OracleEsnLookupDAO daoLookup = (OracleEsnLookupDAO)oracleDAOFactory.createEsnLookupDAO();
				DMDHubMacIdLookupRequestVO vo = new DMDHubMacIdLookupRequestVO();	
				macid = macid.trim().toUpperCase();
				vo.setMacid(macid);
				String pmac = "";
				req.getSession().setAttribute("reqmacid",macid);
				
				try
				{
					pmac = daoLookup.getDMDHubPesn(vo);
				}catch (Exception e){
					e.printStackTrace();
				}
				L.debug("req:macid3 pesn =" + pmac);
				req.getSession().setAttribute("reqpmacid",pmac);
				//pmac = "02712853248";
				String esn = pmac;
				if (esn == null || esn.trim().equals(""))
				{
					esn = DMDUtils.convertESNFromHexToDecimal(esn_hex.trim());
				}
				else
				{
					esn = esn.trim();
				}

				if (esn == null || esn.trim().equals("") || esn.trim().length() > 11)
				{
					// Invalid ESN
					if (esn_dec != null && !esn_dec.trim().equals(""))
						reqVO.setId(esn_dec);
					else
						reqVO.setId(esn_hex);
					statsBuf.append("FALSE|Invalid ESN");
					return null;
				}
				String tEsn = DMDUtils.addLeadingZeros(esn);
				Pattern esnPat = Pattern.compile("\\d{11}");
				Matcher esnMatcher = esnPat.matcher(tEsn);
				if (!esnMatcher.matches())
				{
					// Invalid ESN.
					if (esn_dec != null && !esn_dec.trim().equals(""))
						reqVO.setId(esn_dec);
					else
						reqVO.setId(esn_hex);
					statsBuf.append("FALSE|Invalid ESN");
					return null;
				}

				reqVO.setId(tEsn);
				reqVO.setAttributeGui(report_type.trim().toUpperCase());
				L.debug("xslKey = " + xslType);
				return (DMDRefData.getXslProperty(xslType.toString()));
			}
			
		}
		if (pmacid != null && !pmacid.trim().equals("")){
			L.debug("req:pmacid=" + pmacid);
			
			{
				reqVO.setIdType("ESN");
				statsBuf.append("pESN(Macid)");
				statsBuf.append("_" + report_type.trim().toUpperCase());
				statsBuf.append("|");
				xslType.append("esn_" + report_type.trim().toLowerCase());
				
				OracleDAOFactory oracleDAOFactory = OracleDAOFactory.getInstance();
				OracleEsnLookupDAO daoLookup = (OracleEsnLookupDAO)oracleDAOFactory.createEsnLookupDAO();
				DMDHubMacIdLookupRequestVO vo = new DMDHubMacIdLookupRequestVO();	
				vo.setPesn(pmacid);
				String mac = "";

								
				try
				{
					mac = daoLookup.getDMDHubMacid(vo);
				}catch (Exception e){
					e.printStackTrace();
				}
				L.debug("req:macid3 pesn =" + mac);
				req.getSession().setAttribute("reqmacid",mac);
				req.getSession().setAttribute("reqpmacid",pmacid);

				
				String esn = pmacid;

				if (esn == null || esn.trim().equals(""))
				{
					esn = DMDUtils.convertESNFromHexToDecimal(esn_hex.trim());
				}
				else
				{
					esn = esn.trim();
				}

				if (esn == null || esn.trim().equals("") || esn.trim().length() > 11)
				{
					// Invalid ESN
					if (esn_dec != null && !esn_dec.trim().equals(""))
						reqVO.setId(esn_dec);
					else
						reqVO.setId(esn_hex);
					statsBuf.append("FALSE|Invalid ESN");
					return null;
				}
				String tEsn = DMDUtils.addLeadingZeros(esn);
				Pattern esnPat = Pattern.compile("\\d{11}");
				Matcher esnMatcher = esnPat.matcher(tEsn);
				if (!esnMatcher.matches())
				{
					// Invalid ESN.
					if (esn_dec != null && !esn_dec.trim().equals(""))
						reqVO.setId(esn_dec);
					else
						reqVO.setId(esn_hex);
					statsBuf.append("FALSE|Invalid ESN");
					return null;
				}

				reqVO.setId(tEsn);
				reqVO.setAttributeGui(report_type.trim().toUpperCase());
				L.debug("xslKey = " + xslType);
				return (DMDRefData.getXslProperty(xslType.toString()));
			}
			
		}
		String app_type = req.getParameter("app_type");
		if (app_type == null || app_type.trim().equals(""))
			app_type = "UNKNOWN";
		statsBuf.append(app_type + "|");
		statsBuf.append(DMDUtils.getClientIP(req));
		statsBuf.append("|LOOKUP|");
		if (report_type == null || report_type.trim().equals(""))
			report_type = "features";
		if ((esn_dec != null && !esn_dec.trim().equals(""))
			|| (esn_hex != null && !esn_hex.trim().equals("")))
		{
			reqVO.setIdType("ESN");
			statsBuf.append("ESN");
			statsBuf.append("_" + report_type.trim().toUpperCase());
			statsBuf.append("|");
			if (esn_dec != null && !esn_dec.trim().equals(""))
				statsBuf.append(esn_dec);
			else
				statsBuf.append(esn_hex);
			statsBuf.append("|");

			xslType.append("esn_" + report_type.trim().toLowerCase());
			String esn = esn_dec;
			if (esn == null || esn.trim().equals(""))
			{
				esn = DMDUtils.convertESNFromHexToDecimal(esn_hex.trim());
			}
			else
			{
				esn = esn.trim();
			}

			if (esn == null || esn.trim().equals("") || esn.trim().length() > 11)
			{
				// Invalid ESN
				if (esn_dec != null && !esn_dec.trim().equals(""))
					reqVO.setId(esn_dec);
				else
					reqVO.setId(esn_hex);
				statsBuf.append("FALSE|Invalid ESN");
				return null;
			}
			String tEsn = DMDUtils.addLeadingZeros(esn);
			Pattern esnPat = Pattern.compile("\\d{11}");
			Matcher esnMatcher = esnPat.matcher(tEsn);
			if (!esnMatcher.matches())
			{
				// Invalid ESN.
				if (esn_dec != null && !esn_dec.trim().equals(""))
					reqVO.setId(esn_dec);
				else
					reqVO.setId(esn_hex);
				statsBuf.append("FALSE|Invalid ESN");
				return null;
			}

			reqVO.setId(tEsn);
			reqVO.setAttributeGui(report_type.trim().toUpperCase());
			L.debug("xslKey = " + xslType);
			return (DMDRefData.getXslProperty(xslType.toString()));
		}
		if (DMDProps.isMeidEnabled()
			&& meid_hex != null
			&& !meid_hex.trim().equals(""))
		{
			reqVO.setIdType("MEID");
			statsBuf.append("MEID");
			statsBuf.append("_" + report_type.trim().toUpperCase());
			statsBuf.append("|");
			statsBuf.append(meid_hex.trim());
			statsBuf.append("|");

			Pattern meidPat = Pattern.compile("[A-Fa-f0-9]{14}");
			Matcher meidMatcher = meidPat.matcher(meid_hex);
			if (!meidMatcher.matches())
			{
				reqVO.setId(meid_hex);
				statsBuf.append("FALSE|Invalid MEID");
				return null;
			}
			String meid = DMDUtils.convertMEIDFromHexToDecimal(meid_hex.trim());
			if (meid != null && !meid.trim().equals(""))
			{
				reqVO.setId(meid.trim());
				reqVO.setAttributeGui(report_type.trim().toUpperCase());
				xslType.append("meid_" + report_type.trim().toLowerCase());
				L.debug("xslKey = " + xslType);
				return (DMDRefData.getXslProperty(xslType.toString()));
			}
			else
			{
				statsBuf.append("FALSE|Invalid MEID");
				return null;
			}
		}
		if(iccid != null && !iccid.trim().equals("")){
			reqVO.setIdType("ICCID");
			reqVO.setId(iccid);
			if(euimidSearch) {
				reqVO.setEuimidSearch(true);
			}
			
			
			app_type = req.getParameter("app_type");
			reqVO.setAppType(app_type);
			L.debug("iccid = " + iccid);
			L.debug("app_type = " + app_type);
			
			statsBuf.append("ICCID");
			statsBuf.append("_" + report_type.trim().toUpperCase());
			statsBuf.append("|");
				statsBuf.append(iccid);
			statsBuf.append("|");

			if(euimidSearch) {
				xslType.append("euimid_");
			} else {
				xslType.append("iccid_");
			}
			xslType.append(report_type.trim().toLowerCase());
			
			reqVO.setAttributeGui(report_type.trim().toUpperCase());
			L.debug("xslKey = " + xslType);
			return (DMDRefData.getXslProperty(xslType.toString()));

		}
		if(imei != null && !imei.trim().equals("")){
			reqVO.setIdType("IMEI");
			reqVO.setId(imei);			
			app_type = req.getParameter("app_type");
			reqVO.setAppType(app_type);
			L.debug("imei = " + imei);
			L.debug("app_type = " + app_type);
			
			statsBuf.append("IMEI");
			statsBuf.append("_" + report_type.trim().toUpperCase());
			statsBuf.append("|");
				statsBuf.append(imei);
			statsBuf.append("|");

			xslType.append("esn_" + report_type.trim().toLowerCase());
			reqVO.setAttributeGui(report_type.trim().toUpperCase());
			L.debug("xslKey = " + xslType);
			return (DMDRefData.getXslProperty(xslType.toString()));

		}		
		if(mac4 != null && !mac4.trim().equals("")){
			reqVO.setIdType("MACID");
			reqVO.setId(mac4);			
			app_type = req.getParameter("app_type");
			reqVO.setAppType(app_type);
			L.debug("macid = " + mac4);
			L.debug("app_type = " + app_type);
			
			statsBuf.append("MACID");
			statsBuf.append("_" + report_type.trim().toUpperCase());
			statsBuf.append("|");
				statsBuf.append(mac4);
			statsBuf.append("|");

			xslType.append("esn_" + report_type.trim().toLowerCase());
			reqVO.setAttributeGui(report_type.trim().toUpperCase());
			L.debug("xslKey = " + xslType);
			return (DMDRefData.getXslProperty(xslType.toString()));

		}
		if(nemacid != null && !nemacid.trim().equals("")){
			reqVO.setIdType("NEMACID");
			reqVO.setId(nemacid);			
			app_type = req.getParameter("app_type");
			reqVO.setAppType(app_type);
			L.debug("nemacid = " + nemacid);
			L.debug("app_type = " + app_type);
			
			statsBuf.append("NEMACID");
			statsBuf.append("_" + report_type.trim().toUpperCase());
			statsBuf.append("|");
				statsBuf.append(nemacid);
			statsBuf.append("|");

			xslType.append("nemacid_" + "features");
			reqVO.setAttributeGui(report_type.trim().toUpperCase());
			L.debug("xslKey = " + xslType);
			return (DMDRefData.getXslProperty(xslType.toString()));

		}
		
		if (model_name != null && !model_name.trim().equals(""))
		{
			statsBuf.append("MODEL");
			if (report_type != null)
				statsBuf.append("_" + report_type.trim().toUpperCase());
			statsBuf.append("|");
			statsBuf.append(model_name.trim());
			statsBuf.append("|");
			xslType.append("model_" + report_type.trim().toLowerCase());
			reqVO.setId(model_name.trim());
			reqVO.setIdType("MODEL");
			reqVO.setAttributeGui(report_type.trim().toUpperCase());
			L.debug("xslKey = " + xslType);
			return (DMDRefData.getXslProperty(xslType.toString()));
		}
		if ((mfg_name != null && !mfg_name.trim().equals(""))
			|| (eqp_mode != null && !eqp_mode.trim().equals(""))
			|| (capabilities != null && !capabilities.trim().equals("")))
		{
			statsBuf.append("OTHER|");
			reqVO.setIdType("OTHER");
			if (mfg_name != null && !mfg_name.trim().equals(""))
			{
				reqVO.setManufacturerGui(mfg_name.trim());
				statsBuf.append(mfg_name.trim());
			}
			else
			{
				statsBuf.append("NONE");
			}

			statsBuf.append("-");
			if (eqp_mode != null && !eqp_mode.trim().equals(""))
			{
				reqVO.setEquipmentModeGui(eqp_mode.trim());
				statsBuf.append(eqp_mode.trim());
			}
			else
			{
				statsBuf.append("NONE");
			}

			statsBuf.append("-");
			if (capabilities != null && !capabilities.trim().equals(""))
			{
			    StringTokenizer tok = new StringTokenizer(capabilities, ":");
//			    System.err.println("Tokens = " + tok.countTokens());
			    if (tok.countTokens() == 2)
			    {
			        String capStr = tok.nextToken();
			        String locStr = tok.nextToken();
			        reqVO.setCapabilitiesGui(capStr.trim());
			        reqVO.setCapabilitiesLocation(locStr.trim());
					statsBuf.append(capStr);
			    }
			    else
			    {
			        reqVO.setCapabilitiesGui(capabilities.trim());
			        reqVO.setCapabilitiesLocation("FEATURES");
					statsBuf.append(capabilities.trim());
			    }
			}
			else
			{
				statsBuf.append("NONE");
			}
			statsBuf.append("|");
			xslType.append("others");
			return (DMDRefData.getXslProperty(xslType.toString()));
		}
		statsBuf.append("NONE|NONE|FALSE|No Input");
		return null;
	}

	private void handleError(
		HttpServletRequest req,
		HttpServletResponse res,
		String msg)
		throws IOException
	{
		ServletOutputStream out = res.getOutputStream();
		DMDHtmlUtils.print_header(out);
		DMDHtmlUtils.print_left_bar(req, out);
		out.println("<td  valign='top'>");
		out.println("<table cellpadding='0' cellspacing'0' border='0'>");
		out.println("<tr><td valign='top' align='center'>");
		out.println("<img src='images/phone.gif' border='0'></td></tr>");
		out.println("<bug><big><big>" + encoder.encodeHTML(msg) + "</big></big></bug>");
		out.println("</table>");
		out.println("</td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</body>");
		out.println("</html>");
		out.flush();
		out.close();
	}
	
}
