package com.vzw.dmd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import oracle.panama.core.xml.XML;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.vzw.dmd.dao.BulkDeviceSimLookupDAO;
//import com.ibm.jvm.Dump;
import com.vzw.dmd.dao.DeviceLookupDAO;
import com.vzw.dmd.dao.OracleDAOFactory;
import com.vzw.dmd.dao.OracleEsnLookupDAO;
import com.vzw.dmd.dao.OracleFullXmlDAO;
import com.vzw.dmd.dao.PhoneList;
import com.vzw.dmd.ejb.EsnMeidLockLocal;
import com.vzw.dmd.ejb.EsnMeidLockLocalHome;
import com.vzw.dmd.ejb.EsnMeidLookupLocal;
import com.vzw.dmd.ejb.EsnMeidLookupLocalHome;
import com.vzw.dmd.ejb.FullXmlLookupLocal;
import com.vzw.dmd.ejb.FullXmlLookupLocalHome;
import com.vzw.dmd.util.DBUtils;
import com.vzw.dmd.util.DMDConstants;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.DMDRefData;
import com.vzw.dmd.util.DMDUtils;
import com.vzw.dmd.util.XmlUtils;
import com.vzw.dmd.valueobject.DMDEquipmentIdLookupRequestVO;
import com.vzw.dmd.valueobject.DMDFULLXMLEQUIPMENTVO;
import com.vzw.dmd.valueobject.DMDHubMacIdLookupRequestVO;
import com.vzw.dmd.valueobject.DMDSKUVO;
import com.vzw.dmd.valueobject.EMSFeaturesVO;
import com.vzw.dmd.valueobject.EquipmentFeatureVO;
import com.vzw.dmd.valueobject.EquipmentModelVO;
import com.vzw.dmd.valueobject.EquipmentPhysicalAttributeVO;
import com.vzw.dmd.valueobject.EsnLockRequestVO;
import com.vzw.dmd.valueobject.EsnLookupRequestVO;
import com.vzw.dmd.valueobject.LaunchPackageMacidVO;
import com.vzw.dmd.valueobject.ModelDetailVO;
import com.vzw.dmd.valueobject.SoftwareMacidVO;
import com.vzw.dmd.valueobject.TechnologyVO;
import com.vzw.dmd.util.EsnMeidMultiLockBeanHelper;
import com.vzw.dmd.util.XSSEncoder;
import com.vzw.dmd.util.XXEDisabler;


/**
 * @version 	1.0
 * @author
 */
public class DMDXml extends HttpServlet
{
	private static Logger L =
		Logger.getLogger(DMDLogs.getLogName(DMDXml.class));
	XSSEncoder encoder = new XSSEncoder();
	/**
	* @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	*/
	public void doGet(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
		if(req.getParameter("lteCategory") != null){
			defaultAction(req,res,req.getParameter("lteCategory"));
			
		}else {
		String macid = req.getParameter("macid");
		if (macid != null && macid.trim().length() > 0){
			try {
			defaultMacidAction(req, res);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			defaultAction(req, res);			
		}
	}
	}

	/**
	* @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	*/
	public void doPost(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
		String macid = req.getParameter("macid");
		if (macid != null && macid.trim().length() > 0){
			try {
				defaultMacidAction(req, res);
				} catch (Exception e) {
					e.printStackTrace();
				}

		}else{
			defaultAction(req, res);			
		}
	}

	/**
	* @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	*/
	public void defaultAction(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
//		String dumpReq = req.getParameter("dump");
//		if (dumpReq != null && !dumpReq.trim().equals(""))
//		{
//			Dump.HeapDump();
//			return;
//		}
				
		StringBuffer statsLogBuf = new StringBuffer("XML|");
		StringBuffer xslTypeKey = new StringBuffer("");
		res.setStatus(200);
		res.setContentType("text/xml");
		Date entryTime = new Date();
		String xslType = null;
		
		//#BQVT-966
		StringBuffer xmlReqForNewCassWriteQueue = new StringBuffer("");				
		String lteCategory = req.getParameter("lteCategory");		
		String macid = req.getParameter("macid");
		xmlReqForNewCassWriteQueue.append(lteCategory==null ? "" : "lteCategory="+lteCategory+"&");
		xmlReqForNewCassWriteQueue.append(macid==null ? "" : "macid="+macid+"&");
		
		try
		{
			EsnLockRequestVO lockVO = new EsnLockRequestVO();
			EsnLookupRequestVO lookupVO = new EsnLookupRequestVO();
			xslType = xslType(req, lookupVO, lockVO, xslTypeKey, statsLogBuf, xmlReqForNewCassWriteQueue);
			
			L.debug("-");
			L.debug("xslType-"+xslType);
			if (xslType == null || xslType.trim().startsWith("ERR:"))
			{
			    String errorMsg = null;
			    if (xslType == null)
			    {
			        errorMsg = "No valid xsl type found for request.";
			    }
			    else
			    {
			        errorMsg = xslType.substring(4);
			    }
				L.error(
						"Error: "
							+ errorMsg
							+ "; Can't Continue.;");
				ServletOutputStream out = res.getOutputStream();
				out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"); 
				out.println("<DMD>");
				out.println("   <STATUS>");
				out.println("      <STATUS_STR>Not Found</STATUS_STR>");
				out.println(
					"      <MESSAGE>"
						+ encoder.encodeXMLAttribute(errorMsg)
						+ "</MESSAGE>\n   </STATUS>\n</DMD>");
				out.flush();
				out.close();
				return;
			}
			// Check for SCM MEID fix
			if (lookupVO != null)
			{
				String aType = lookupVO.getAppType();
				String iType = lookupVO.getIdType();
				if (aType.trim().equalsIgnoreCase("SCM") && iType.trim().equalsIgnoreCase("MEID"))
				{
				    if (lookupVO.isSearchResultStatus())
				    {
				        statsLogBuf.append("FALSE|Invalid MEID");
				        // Invalid XML
				        StringBuffer retXml = new StringBuffer();
				        retXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"); 
				        retXml.append("<DMD xmlns=\"http://www.vzw.com/namespace/scm\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
				        retXml.append("<STATUS>");
				        retXml.append("<STATUS_STR>Normal</STATUS_STR>"); 
				        retXml.append("<MESSAGE /> ");
				        retXml.append("</STATUS>");
				        retXml.append("<EQUIPMENT_MEID>");
				        retXml.append("<MEID>" + lookupVO.getId() + "</MEID>"); 
				        retXml.append("</EQUIPMENT_MEID>");
				        retXml.append("<EQUIPMENT_MODEL>");
				        retXml.append("<PROD_NAME>Unknown</PROD_NAME>"); 
				        retXml.append("<MFG_CODE>UNK</MFG_CODE>"); 
				        retXml.append("<EFFECTIVE_DATE>2004/01/01</EFFECTIVE_DATE>"); 
				        retXml.append("</EQUIPMENT_MODEL>");
				        retXml.append("</DMD>");
						ServletOutputStream out = res.getOutputStream();
						out.println(encoder.encodeXML(retXml.toString()));
						out.flush();
						out.close();
						return;
				    }
				}
				else if (aType.trim().equalsIgnoreCase("SCM") && iType.trim().equalsIgnoreCase("ESN"))
				{
				    if (lookupVO.isSearchResultStatus())
				    {
				        statsLogBuf.append("FALSE|Invalid MEID");
				        // Invalid XML
				        StringBuffer retXml = new StringBuffer();
				        retXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"); 
				        retXml.append("<DMD xmlns=\"http://www.vzw.com/namespace/scm\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
				        retXml.append("<STATUS>");
				        retXml.append("<STATUS_STR>Normal</STATUS_STR>"); 
				        retXml.append("<MESSAGE /> ");
				        retXml.append("</STATUS>");
				        retXml.append("<EQUIPMENT_ESN>");
				        retXml.append("<ESN>" + encoder.encodeXMLAttribute(lookupVO.getId()) + "</ESN>"); 
				        retXml.append("</EQUIPMENT_ESN>");
				        retXml.append("<EQUIPMENT_MODEL>");
				        retXml.append("<PROD_NAME>Unknown</PROD_NAME>"); 
				        retXml.append("<MFG_CODE>UNK</MFG_CODE>"); 
				        retXml.append("<EFFECTIVE_DATE>2004/01/01</EFFECTIVE_DATE>"); 
				        retXml.append("</EQUIPMENT_MODEL>");
				        retXml.append("</DMD>");
						ServletOutputStream out = res.getOutputStream();
						out.println(encoder.encodeXML(retXml.toString()));
						out.flush();
						out.close();
						return;
				    }
				}
			}
			
			L.debug("-->xslType = " + xslType);
			L.debug("lookupVO = " + lookupVO);
			L.debug("lockVO = " + lockVO);
			InitialContext ic = new InitialContext();
			String searchStatus = null;
			String statusMsg = null;
	
			if (xslType.trim().equalsIgnoreCase("XML-FIRMWAREHIS")){
				EsnMeidLookupLocalHome ejbHome =
					(EsnMeidLookupLocalHome) ic.lookup(
						"java:comp/env/ejb/EsnMeidLookup");
				EsnMeidLookupLocal ejb = ejbHome.create();
				String retDoc=ejb.getFirmWareHistoryXML(lookupVO);
				ServletOutputStream out = res.getOutputStream();
				//Fortify Fix - Cross-site scripting
				out.println(encoder.encodeHTML(retDoc));
				out.flush();
				out.close();
				String esnMeidLog=null;
				if("ESN".equalsIgnoreCase(lookupVO.getIdType()))
					esnMeidLog="ESN_SOFTWARE";
				else
					esnMeidLog="MEID_SOFTWARE";
				
				statsLogBuf.append(req.getParameter("app_type"))
				.append("|")
				.append(DMDUtils.getClientIP(req))				
				.append("|LOOKUP|")
				.append(esnMeidLog)
				.append("|")				
				.append(lookupVO.getId())				
				.append("|TRUE|");				
				/*searchStatus = 	XmlUtils.getValue(
							retDoc,
							"/equipment/search_status/status/status_message/text()");;
				*/								
				return;
			}
			Document retDoc = null;
			ServletOutputStream out = res.getOutputStream();
			if (lockVO != null && lockVO.getId() != null)
			{
				EsnMeidLockLocalHome ejbHome =
					(EsnMeidLockLocalHome) ic.lookup(
						"java:comp/env/ejb/EsnMeidLock");
				EsnMeidLockLocal ejb = ejbHome.create();
				String lockReq = lockVO.getLockRequest();
				if (lockReq != null && !lockReq.trim().equals(""))
				{
					//if ( DMDProps.getProperty( "ESN_MEID_MULTI_UPDATE" ).equalsIgnoreCase( "true" ) )
					if ( "Y".equalsIgnoreCase( DMDProps.getEsnMeidMultiUpdate() ) )
					{
						EsnMeidMultiLockBeanHelper multiLockBeanHelper = new EsnMeidMultiLockBeanHelper();
						retDoc = multiLockBeanHelper.updateEsnMeidLockStatus( lockVO );
						multiLockBeanHelper = null;
						
					}
					else 
						retDoc = ejb.updateEsnMeidLockStatus( lockVO );
				}
				else
				{
					retDoc = ejb.getEsnMeidCurentLockStatus(lockVO);
				}
				searchStatus = "TRUE";
				statusMsg = "";
				
				//feb 2019 BQVT-966
				//send input request to new dmd write queue for cassandra processing
				//need to be handled via on-off toggle
				String serviceEnabled = DBUtils.getDBPropertyValue("DMDXMLCASSSERVICE");
				L.debug("DMDXml.defaultAction() cass flow serviceEnabled--"+serviceEnabled);
				//add service name to the xml
				if("Y".equalsIgnoreCase(serviceEnabled))
				{
					String cassXmlMsg= xmlReqForNewCassWriteQueue.toString();
					if(cassXmlMsg.endsWith("&"))
						cassXmlMsg=cassXmlMsg.substring(0, cassXmlMsg.length()-1);
					
					String messageText =  "<xmlReq><apiName>DMDXml</apiName><dmdXmlReq><![CDATA["+cassXmlMsg+"]]></dmdXmlReq></xmlReq>";
					L.debug("DMDXml.defaultAction() messageText--"+messageText);
					DMDMqClient mqClient = new DMDMqClient();
					mqClient.sendMessageToQueue(messageText);	
					L.info("DMDXml.defaultAction() cass flow done");
					L.debug("DMDXml.defaultAction() cass flow done");
				
				}
			}
			else
			{
				L.debug("-->LookupVO: " + lookupVO.toString());
				if (xslType.trim().equalsIgnoreCase("FULL"))
				{
				    if (lookupVO.getIdType().equals("MODEL"))
				    {
				        try
				        {
					        String retXml = ModelFullXmls.getModelXml(lookupVO.getId());
					        if (retXml != null && !retXml.equals(""))
					        {
					            L.info ("Found Model(" + lookupVO.getId() + ") in cache.");
					            statsLogBuf.append("TRUE|");
					            out.print(retXml);
					            out.flush();
					            out.close();
					            return;
					        }
				        }
				        catch (Exception e)
				        {
				            L.error("Exception duing loading model xml from cache, " + e);
				        }
				    }
					FullXmlLookupLocalHome ejbHome =
						(FullXmlLookupLocalHome) ic.lookup(
							"java:comp/env/ejb/FullXmlLookup");
					FullXmlLookupLocal ejb = ejbHome.create();
					retDoc = ejb.getFullXml(lookupVO);
					Element rootEle = retDoc.getDocumentElement();
					if (rootEle.getTagName().equalsIgnoreCase("equipment"))
					{
						xslType = DMDRefData.getXslProperty("web-error");
					}
					else
					{
						statsLogBuf.append("TRUE|");
						XML.printWithFormat(new PrintWriter(out), retDoc, "UTF-8");
					    out.flush();
					    out.close();
						return;
					}
				}
				else if (xslType.trim().equalsIgnoreCase("PHONES"))
				{
					if(!OracleFullXmlDAO.printPhonesAPIResponse(new PrintStream(out))) {
					    String retXml = getPhoneTypesString();
					    out.print(retXml);
					}
				    statsLogBuf.append("TRUE|");
				    out.flush();
				    out.close();
				    return;
				}
				//begin .. features Light 
				else if (xslType.trim().equalsIgnoreCase("featuresLight"))
				{
					String deviceType = DMDUtils.getDeviceIDType(lookupVO.getDeviceID());
					String retValue = "";
					if (deviceType.equalsIgnoreCase(DMDConstants.EMPTY_STRING)){
						L.debug("The device type is empty:"+deviceType);
						retValue="NOT_FOUND";
					}
					else 
						retValue = DeviceLookupDAO.fetchEMSForDevice(lookupVO.getDeviceID(),deviceType);				    
				    StringBuffer retXml = new StringBuffer();
				    if (retValue != null && !retValue.equalsIgnoreCase("NOT_FOUND") && retValue.length() == 1){
				    	statsLogBuf.append("TRUE|");
				        retXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
				        retXml.append("<DMD>");
				        retXml.append("<STATUS>");
				        retXml.append("<STATUS_STR>Normal</STATUS_STR>"); 
				        retXml.append("<MESSAGE /> ");
				        retXml.append("</STATUS>");
				        retXml.append("<DEVICE_ID_INFO>"); 
				        retXml.append("<DEVICE_ID> ");
				        retXml.append(lookupVO.getDeviceID().toUpperCase());
				        retXml.append("</DEVICE_ID>");
				        retXml.append("<EMS>");
				        retXml.append(retValue);
				        retXml.append("</EMS>");
				        retXml.append("</DEVICE_ID_INFO>");
				        retXml.append("</DMD>");
				    }
				    else if (retValue.equalsIgnoreCase("NOT_FOUND")){
				    	statsLogBuf.append("FALSE|");
				    	retXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
				        retXml.append("<DMD>");
				        retXml.append("<STATUS>");
				        retXml.append("<STATUS_STR>FAILURE</STATUS_STR>"); 
				        retXml.append("<MESSAGE > ");
				        retXml.append("DEVICE NOT FOUND IN DB");
				        retXml.append("</MESSAGE > ");
				        retXml.append("</STATUS>");
				        retXml.append("<DEVICE_ID_INFO>"); 
				        retXml.append("<DEVICE_ID> ");
				        retXml.append(lookupVO.getDeviceID().toUpperCase());
				        retXml.append("</DEVICE_ID>");
				        retXml.append("</DEVICE_ID_INFO>");
				        retXml.append("</DMD>");
				    }
				        else {
				        	statsLogBuf.append("FALSE|");
					    	retXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
					        retXml.append("<DMD>");
					        retXml.append("<STATUS>");
					        retXml.append("<STATUS_STR>FAILURE</STATUS_STR>"); 
					        retXml.append("<MESSAGE > ");
					        retXml.append(retValue);
					        retXml.append("</MESSAGE > ");
					        retXml.append("</STATUS>");
					        retXml.append("<DEVICE_ID_INFO>"); 
					        retXml.append("<DEVICE_ID> ");
					        retXml.append(lookupVO.getDeviceID().toUpperCase());
					        retXml.append("</DEVICE_ID>");
					        retXml.append("</DEVICE_ID_INFO>");
					        retXml.append("</DMD>");
				        }
				    //Fortify Fix - Cross-site scripting
				    out.print(encoder.encodeXML(retXml.toString()));
				    out.flush();
				    out.close();
				    return;
				}
				//end .. features Light
				else
				{
					String device_id = req.getParameter("deviceID");
					if(device_id != null && !("").equals(device_id)){
						lookupVO.setSearchResultStatus(true);
					}
					EsnMeidLookupLocalHome ejbHome =
						(EsnMeidLookupLocalHome) ic.lookup(
							"java:comp/env/ejb/EsnMeidLookup");
					EsnMeidLookupLocal ejb = ejbHome.create();
					retDoc = ejb.getCombinedXml(lookupVO);
					ejb.remove();
					ejb = null;
					ejbHome = null;
				}
				searchStatus =
					XmlUtils.getValue(
						retDoc,
						"/equipment/search_status/search_result_status/text()");
				statusMsg =
					XmlUtils.getValue(
						retDoc,
						"/equipment/search_status/status/status_message/text()");
			}
			statsLogBuf.append(searchStatus.trim().toUpperCase());
			statsLogBuf.append("|");
			statsLogBuf.append(statusMsg.trim());
			String action = req.getParameter("action");
			String logConstent =DMDConstants.DMD_XLM +"_"+action.toUpperCase();
			statsLogBuf.append("|");
			statsLogBuf.append(logConstent);
			L.debug(
				"Ret Document from EJB:\n"
					+ XmlUtils.convertToString(retDoc, true));
			String stylePath =
				getServletContext().getRealPath(
					"/WEB-INF/xslt/" + xslType.trim());

			try
			{
				// Get the transformer
				Transformer trans = StylesheetCache.newTransformer(stylePath);
				
				// Create a XML Format for input
				OutputFormat inpXmlFmt = new OutputFormat("xml", "UTF-8", true);
				// Create a string writer for input XML
				StringWriter inpWri = new StringWriter();
				XMLSerializer ser = new XMLSerializer(inpWri, inpXmlFmt);

				//Serialize the document to get the String representation
				ser.serialize(retDoc);
				ByteArrayInputStream origXMLIn =
					new ByteArrayInputStream(inpWri.toString().getBytes());
				Source styleSource = new StreamSource(origXMLIn);
				ByteArrayOutputStream resultBuf = new ByteArrayOutputStream();
				
				// Transform
				trans.transform(styleSource, new StreamResult(resultBuf));
				//Fortify Fix - XML External Entity Injection
				DocumentBuilderFactory df = new XXEDisabler().disableDBF(DocumentBuilderFactory.newInstance());
				DocumentBuilder db = df.newDocumentBuilder();
				L.debug(
					"Result Buf:\n"
						+ resultBuf.toString());
						
				// Create the Final Result Document
				Document resDoc =
					db.parse(
						new InputSource(
							new StringReader(resultBuf.toString())));
				
				// Create a StingWriter for result XML
				StringWriter resWri = new StringWriter();
				
				// Output the document with indentation
				OutputFormat resFmt = new OutputFormat("xml", "UTF-8", true);
				resFmt.setPreserveEmptyAttributes(true);
				//resFmt.setPreserveSpace(true);
				resFmt.setIndent(3);
				// Create a new Serializer
				ser =
					new XMLSerializer(
						resWri,
						resFmt);
						
				// Serialize the document
				ser.serialize(resDoc);
				
				// Get pretty formatted XML String
				String resXmlStr = XmlUtils.prettyFormat(resWri.toString());
				resXmlStr = resXmlStr.replaceAll("!-!-!", "     ");
				L.debug(
					"Final Returned Doc:\n"
						+ resXmlStr);
				out.print(resXmlStr);
				out.flush();
				out.close();
			}
			catch (Exception e)
			{
				L.error("Exception during Transformation", e);
			}
			
		}
		catch (Exception e)
		{
			L.error("Exception", e);
		}
		finally
		{
		    Date exitTime = new Date();
		    long prcTime = exitTime.getTime() - entryTime.getTime();
		    if(xslType.trim().equalsIgnoreCase("XML-FIRMWAREHIS")){
		    	DMDLogs.getStatsLogger().info(statsLogBuf.toString());
		    	DMDLogs.getEStatsLogger().info(
		    			statsLogBuf.toString() 
                            + DMDProps.ldf.format(entryTime) + "|"
                            + DMDProps.ldf.format(exitTime) + "|" + prcTime);
		    }else{
		    	String transId = req.getParameter("transaction_id");
		    	if (transId == null || transId.trim().equals(""))
		    	{
		    		transId = "NONE";
		    	}		    
		    	DMDLogs.getStatsLogger().info(statsLogBuf.toString());
		    	DMDLogs.getEStatsLogger().info(
		    			statsLogBuf.toString() + "|"
							+ transId + "|"
                            + DMDProps.ldf.format(entryTime) + "|"
                            + DMDProps.ldf.format(exitTime) + "|" + prcTime);
		    }
		}
	}
	

	/**
	* @see javax.servlet.GenericServlet#void ()
	*/
	public void init() throws ServletException
	{

		super.init();

	}

	private String xslType(
		HttpServletRequest req,
		EsnLookupRequestVO lookVO,
		EsnLockRequestVO lockVO,
		StringBuffer xslType,
		StringBuffer statsBuf,
		StringBuffer xmlReqForNewCassWriteQueue)
	{
		
		String deviceID = null;
		
		
		
		
		xslType.append("web-");
		String esn_dec = req.getParameter("esn");		
		String esn_hex = req.getParameter("esn_hex");
		String meid_hex = req.getParameter("meid");
		String app_type = req.getParameter("app_type");
		String action = req.getParameter("action");
		String prodtype = req.getParameter("prodtype");
		String prodname = req.getParameter("prodname");
		String esn_lock = req.getParameter("esn_lock");
		String meid_lock = req.getParameter("meid_lock");
		String trans_id = req.getParameter("transaction_id");
		String device_id = req.getParameter("deviceID");
		
		//#BQVT-966
		xmlReqForNewCassWriteQueue.append(esn_dec==null ? "" : "esn="+esn_dec+"&");
		xmlReqForNewCassWriteQueue.append(esn_hex==null ? "" : "esn_hex="+esn_hex+"&");
		xmlReqForNewCassWriteQueue.append(meid_hex==null ? "" : "meid="+meid_hex+"&");
		xmlReqForNewCassWriteQueue.append(app_type==null ? "" : "app_type="+app_type+"&");
		xmlReqForNewCassWriteQueue.append(action==null ? "" : "action="+action+"&");
		xmlReqForNewCassWriteQueue.append(prodtype==null ? "" : "prodtype="+prodtype+"&");
		xmlReqForNewCassWriteQueue.append(prodname==null ? "" : "prodname="+prodname+"&");
		xmlReqForNewCassWriteQueue.append(esn_lock==null ? "" : "esn_lock="+esn_lock+"&");
		xmlReqForNewCassWriteQueue.append(meid_lock==null ? "" : "meid_lock="+meid_lock+"&");
		xmlReqForNewCassWriteQueue.append(trans_id==null ? "" : "transaction_id="+trans_id+"&");
		xmlReqForNewCassWriteQueue.append(device_id==null ? "" : "deviceID="+device_id+"&");

		if(action!=null && action.trim().equalsIgnoreCase("FIRMWAREHIS")){
			deviceID = req.getParameter("deviceID");
			L.debug("deviceID = (" + deviceID + ")");
			if(deviceID != null && !deviceID.trim().equals("")){
			   lookVO.setDeviceID(deviceID);
				
			}else{
				statsBuf.append("FALSE|Invalid DeviceID");
				return "ERR: Please enter value for 'deviceID' parameter.";				
			}
			/*
			String esn_meid = req.getParameter("esn_meid");
			L.debug("esn_meid = (" + esn_meid + ")");
			if(esn_meid != null && !esn_meid.trim().equals("")){
				if(esn_meid.trim().length()<=11){
					lookVO.setIdType("ESN");
					lookVO.setId(DMDUtils.addLeadingZeros(esn_meid.trim()));
				}
				else{
					lookVO.setIdType("MEID");
					lookVO.setId(esn_meid.trim());
				}
			}
			*/
			lookVO.setAppType(app_type);
			return "XML-FIRMWAREHIS";
		}
		
		if(action!=null && action.trim().equalsIgnoreCase("features")){
			deviceID = req.getParameter("deviceID");
			L.debug("deviceID = (" + deviceID + ")");
			String msg="";
			if(deviceID != null && !deviceID.trim().equals("")){
			   lookVO.setDeviceID(deviceID);
			    lookVO.setAppType(app_type);
				return "Web4GDeviceFeatures.xsl";
				
			}else {
				if((esn_dec != null && !esn_dec.trim().equals("")) || (esn_hex !=null && !esn_hex.trim().equals(""))
						|| ( meid_hex != null && !meid_hex.equals(""))){
					L.debug("ESN or MEID is not null or empty");
				}
				else{
					statsBuf.append("FALSE|Invalid ESN or MEID or DeviceID");
					return "ERR: Please enter value for 'esn_meid' or 'deviceID' parameter.";
				}
								
			}
			
			
			
			//return "Web4GDeviceFeatures.xsl";
		}
		//begin .. features Light
		if(action!=null && action.trim().equalsIgnoreCase("featuresLight")){
			deviceID = req.getParameter("deviceID");
			L.debug("deviceID = (" + deviceID + ")");
			String msg="";
			if(deviceID != null && !deviceID.trim().equals("")){
			   lookVO.setDeviceID(deviceID.toUpperCase());
			    lookVO.setAppType(app_type);
				return "featuresLight";
				
			}else{
					statsBuf.append("FALSE|Invalid DeviceID");
					return "ERR: Please enter value for deviceID parameter.";
				}														
			
			
		}
		//end .. features Light
		
		
			
		
		L.debug("esn = (" + esn_dec + ")");
		L.debug("esn_hex = (" + esn_hex + ")");
		L.debug("meid = (" + meid_hex + ")");
		L.debug("app_type = (" + app_type + ")");
		L.debug("action = (" + action + ")");
		L.debug("prodtype = (" + prodtype + ")");
		L.debug("prodname = (" + prodname + ")");
		L.debug("esn_lock = (" + esn_lock + ")");
		L.debug("meid_lock = (" + meid_lock + ")");
		L.debug("Is MEID Enabled = (" + DMDProps.isMeidEnabled() + ")");
		L.debug("transaction_id = (" + trans_id + ")");

		if (trans_id != null && !trans_id.trim().equals(""))
		{
		    lockVO.setTransId(trans_id);
		    lookVO.setTransId(trans_id);
		}
		if (app_type == null || app_type.trim().equals(""))
		{
			// Error. App type can't be null. Reject the message.
			app_type = "UNKNOWN";
			//app_type = DMDUtils.getClientIP(req);
		}
		statsBuf.append(app_type.trim().toUpperCase());
		statsBuf.append("|");
		statsBuf.append(DMDUtils.getClientIP(req));
		statsBuf.append("|");
		lockVO.setAppType(app_type);
		lookVO.setAppType(app_type);
		if (action != null && action.trim().equalsIgnoreCase("esnlock"))
		{
			if (esn_lock != null && !esn_lock.trim().equals(""))
			{
				if (esn_lock.trim().equalsIgnoreCase("y"))
					statsBuf.append("LOCK|ESN|");
				else if (esn_lock.trim().equalsIgnoreCase("n"))
					statsBuf.append("UNLOCK|ESN|");
				else
					statsBuf.append("UNKLOCK|ESN|");
			}
			else
			{
				statsBuf.append("LOCKSTS|ESN|");
			}

			if (esn_dec == null || esn_dec.trim().equals(""))
			{
				statsBuf.append("N/A|FALSE|No ESN Entered");
				return "ERR:Please input esn or phone model to search the database";
			}

			String esn = DMDUtils.addLeadingZeros(esn_dec.trim());
			Pattern esnPat = Pattern.compile("\\d{11}");
			Matcher esnMatcher = esnPat.matcher(esn);
			if (!esnMatcher.matches())
			{
				statsBuf.append(esn_dec.trim());
				statsBuf.append("|");
				lookVO.setId(esn_dec.trim());
				statsBuf.append("FALSE|Invalid ESN");
				return "ERR:Invalid ESN " + esn_dec.trim();
			}

			statsBuf.append(esn_dec.trim());
			statsBuf.append("|");
			lockVO.setId(esn);
			lockVO.setIdType("ESN");
			if (esn_lock != null && !esn_lock.trim().equals(""))
				xslType.append(app_type.trim() + "-esnlock");
			else
				xslType.append("esnlock");
			if (esn_lock != null && !esn_lock.trim().equals(""))
			{
				xslType.append("_" + esn_lock.trim().toLowerCase());
				lockVO.setLockRequest(esn_lock.trim());
			}
			L.debug("DMDXml: xslTypeKey = " + xslType);
			String xslProp = DMDRefData.getXslProperty(xslType.toString());
			if (xslProp == null || xslProp.trim().equals(""))
			{
				statsBuf.append("FALSE|ESN LOCK/UNLOCK not allowed from this app_type");
				return "ERR:ESN LOCK/UNLOCK not allowed from app_type " + app_type;
			}
			return xslProp;
		}
		if (DMDProps.isMeidEnabled()
			&& action != null
			&& action.trim().equalsIgnoreCase("meidlock"))
		{
			if (meid_lock != null && !meid_lock.trim().equals(""))
			{
				if (meid_lock.trim().equalsIgnoreCase("y"))
					statsBuf.append("LOCK|MEID|");
				else if (meid_lock.trim().equalsIgnoreCase("n"))
					statsBuf.append("UNLOCK|MEID|");
				else
					statsBuf.append("UNKLOCK|MEID|");
			}
			else
			{
				statsBuf.append("LOCKSTS|MEID|");
			}
			Pattern meidHexPat = Pattern.compile("[A-Fa-f0-9]{14}");
			Matcher meidHexMatcher = meidHexPat.matcher(meid_hex.trim());
			if (!meidHexMatcher.matches())
			{
				statsBuf.append(meid_hex.trim());
				statsBuf.append("|");
				statsBuf.append("FALSE|Invalid MEID");
				return "ERR:The input meid is not a valid hex meid.";
			}

			String meid = DMDUtils.convertMEIDFromHexToDecimal(meid_hex.trim());
			if (meid == null || meid.trim().equals(""))
			{
				// Error. return null;
				statsBuf.append(meid_hex.trim());
				statsBuf.append("|");
				statsBuf.append("FALSE|No MEID provided");
				return "ERR:No MEID Provided for meidlock request";
			}

			statsBuf.append(meid_hex.trim());
			statsBuf.append("|");

			lockVO.setId(meid);
			lockVO.setIdType("MEID");
			if (meid_lock != null && !meid_lock.trim().equals(""))
				xslType.append(app_type.trim() + "-meidlock");
			else
				xslType.append("meidlock");
			if (meid_lock != null && !meid_lock.trim().equals(""))
			{
				xslType.append("_" + meid_lock.trim().toLowerCase());
				lockVO.setLockRequest(meid_lock.trim());
			}
			L.debug("DMDXml: xslTypeKey = " + xslType);
			String xslProp = DMDRefData.getXslProperty(xslType.toString());
			if (xslProp == null || xslProp.trim().equals(""))
			{
				return "ERR:MEID LOCK/UNLOCK not allowed from app_type " + app_type;
			}
			return xslProp;
		}

		if (esn_dec != null && !esn_dec.trim().equals(""))
		{
			statsBuf.append("LOOKUP|ESN");
			lookVO.setIdType("ESN");
			String esn = DMDUtils.addLeadingZeros(esn_dec.trim());
			Pattern esnPat = Pattern.compile("\\d{11}");
			Matcher esnMatcher = esnPat.matcher(esn);
			if (!esnMatcher.matches())
			{
			    if (app_type.trim().equalsIgnoreCase("scm"))
			    {
			        L.debug("APP Type is SCM and invalid MEID");
				    lookVO.setId(esn);
				    lookVO.setSearchResultStatus(true);
			    }
			    else
			    {
					// Invalid ESN.
					if (action != null && !action.trim().equals(""))
					{
						statsBuf.append("_" + action.trim().toUpperCase());
					}
					statsBuf.append("|");
					statsBuf.append(esn_dec.trim());
					statsBuf.append("|");
					lookVO.setId(esn_dec.trim());
					statsBuf.append("FALSE|Invalid ESN");
					return "ERR:No Results Found for " + esn_dec;
			    }
			}

			L.debug("ESN = " + esn);
			lookVO.setId(esn);
			xslType.append("esn");
			String xslTypePreAction = xslType.toString();
			if (action != null && !action.trim().equals(""))
			{
				xslType.append("_" + action.trim().toLowerCase());
			}
			L.debug("DMDXml: xslTypeKey = " + xslType);
			String xslProp = DMDRefData.getXslProperty(xslType.toString());
			if (xslProp == null)
			{
				xslProp = DMDRefData.getXslProperty(xslTypePreAction);
			}
			else
			{
				if (action != null && !action.trim().equals(""))
				{
					statsBuf.append("_" + action.trim().toUpperCase());
				}
			}
			statsBuf.append("|");
			statsBuf.append(esn_dec.trim());
			statsBuf.append("|");
			if (xslProp == null)
			{
				statsBuf.append("FALSE|Invalid Request Provided");
				return "ERR: Invalid Request. Check URL";
			}
			return (xslProp);
		}
		if (esn_hex != null && !esn_hex.trim().equals(""))
		{
			lookVO.setIdType("ESN");
			statsBuf.append("LOOKUP|ESN_HEX|");
			statsBuf.append(esn_hex.trim());
			statsBuf.append("|");
			Pattern esnHexPat = Pattern.compile("[A-Fa-f0-9]*");
			Matcher esnHexMatcher = esnHexPat.matcher(esn_hex.trim());
			if (!esnHexMatcher.matches())
			{
				statsBuf.append("FALSE|Invalid hex ESN");
				return "ERR:The input esn is not a valid hex esn.";
			}
			String esn = DMDUtils.convertESNFromHexToDecimal(esn_hex.trim());
			if (esn == null)
			{
				statsBuf.append("FALSE|Invalid hex ESN");
				return "ERR:The input esn is not a valid hex esn.";
			}
			L.debug("ESN = " + esn);
			lookVO.setId(esn);
			xslType.append("esn_hex");
			L.debug("DMDXml: xslTypeKey = " + xslType);
			return (DMDRefData.getXslProperty(xslType.toString()));
		}
		if (DMDProps.isMeidEnabled()
			&& (meid_hex != null && !meid_hex.trim().equals("")))
		{
			statsBuf.append("LOOKUP|MEID");
			lookVO.setIdType("MEID");
			Pattern meidHexPat = Pattern.compile("[A-Fa-f0-9]{14}");
			Matcher meidHexMatcher = meidHexPat.matcher(meid_hex.trim());
			if (!meidHexMatcher.matches())
			{
			    if (app_type.trim().equalsIgnoreCase("scm"))
			    {
			        L.debug("APP Type is SCM and invalid MEID");
				    lookVO.setId(meid_hex);
				    lookVO.setSearchResultStatus(true);
			    }
			    else
			    {
					if (action != null && !action.trim().equals(""))
					{
						statsBuf.append("_" + action.trim().toUpperCase());
					}
					statsBuf.append("|");
					statsBuf.append(meid_hex.trim());
					statsBuf.append("|");
					lookVO.setId(meid_hex.trim());
					statsBuf.append("FALSE|Invalid HEX MEID");
					return "ERR:The input meid is not a valid hex meid.";
			    }
			}
			else
			{
				String meid = DMDUtils.convertMEIDFromHexToDecimal(meid_hex.trim());
				if (meid != null && !meid.trim().equals(""))
				{
					lookVO.setId(meid);
				}
				else
				{
					if (action != null && !action.trim().equals(""))
					{
						statsBuf.append("_" + action.trim().toUpperCase());
					}
					statsBuf.append("|");
					statsBuf.append(meid_hex.trim());
					statsBuf.append("|");
					statsBuf.append("FALSE|Invalid MEID Provided");
					return null;
				}
			}
			xslType.append("meid");
			String xslTypePreAction = xslType.toString();
			if (action != null && !action.trim().equals(""))
			{
				xslType.append("_" + action.trim().toLowerCase());
			}
			L.debug("DMDXml: xslTypeKey = " + xslType);
			String xslProp = DMDRefData.getXslProperty(xslType.toString());

			if (xslProp == null)
			{
				xslProp = DMDRefData.getXslProperty(xslTypePreAction);
			}
			else
			{
				if (action != null && !action.trim().equals(""))
				{
					statsBuf.append("_" + action.trim().toUpperCase());
				}
			}
			statsBuf.append("|");
			statsBuf.append(meid_hex.trim());
			statsBuf.append("|");
			if (xslProp == null)
			{
				statsBuf.append("FALSE|Invalid Request Provided");
				return "ERR: Invalid Request. Check URL";
			}
			return (xslProp);
		}
		if (prodname != null && !prodname.trim().equals(""))
		{
			String logConstent =DMDConstants.DMD_XLM +"_"+"OTHERS";
			statsBuf.append("LOOKUP|MODEL|");
			statsBuf.append(prodname);
			statsBuf.append("|");
			statsBuf.append(logConstent);
			statsBuf.append("|");
			// Set full XML to true
			xslType.append("prodname");
			lookVO.setId(prodname);
			lookVO.setIdType("MODEL");
			lookVO.setProdName(prodname);
			L.debug("DMDXml: xslTypeKey = " + xslType);
			return (DMDRefData.getXslProperty(xslType.toString()));
		}
		if (prodtype != null && prodtype.trim().equalsIgnoreCase("phones"))
		{
			String logConstent =DMDConstants.DMD_XLM +"_"+prodtype.toUpperCase();
			statsBuf.append("LOOKUP|PRODTYPE|");
			statsBuf.append(prodtype);
			statsBuf.append("|");
			statsBuf.append(logConstent);
			statsBuf.append("|");
			xslType.append("prodtype_" + prodtype.toLowerCase());
			lookVO.setIdType("PRODTYPE");
			lookVO.setId(prodtype);
			lookVO = new EsnLookupRequestVO();
			L.debug("DMDXml: xslTypeKey = " + xslType);
			return (DMDRefData.getXslProperty(xslType.toString()));
		}
		statsBuf.append("UNKNOWN|UNKNOWN|NONE|FALSE|Unrecognized Input");
		return "ERR:Please input esn or phone model to search the database";
	}
	
//	private Document getPhoneTypesDocument()
//	{
//	    if (lastCacheDate == null)
//	        lastCacheDate = new Date();
//	    Date now = new Date();
//	    long diff = now.getTime() - lastCacheDate.getTime();
//	    if (diff > 86400000L)
//	        phonesDoc = null;
//
//	    if (phonesDoc == null)
//	    {
//	        L.info("DMDXml:getPhoneTypesDocument(): Getting the Phones from DB");
//	        phonesDoc = PhoneList.getPhoneModels();
//	        lastCacheDate = new Date();
//	    }
//	    else
//	    {
//	        L.info("DMDXml:getPhoneTypesDocument(): Returning the Cached XML");
//	    }
//	    return phonesDoc;
//	}

	private String getPhoneTypesString()
	{
	    if (lastCacheDate == null)
	        lastCacheDate = new Date();
	    Date now = new Date();
	    long diff = now.getTime() - lastCacheDate.getTime();
	    if (diff > DMDProps.phoneTypesCache)
	        phonesString = null;

	    if (phonesString == null)
	    {
	        L.info("DMDXml:getPhoneTypesDocument(): Getting the Phones from DB");
	        Document phonesDoc = PhoneList.getPhoneModels();
			StringWriter outWri = new StringWriter();
			try
			{
				XML.printWithFormat(outWri, phonesDoc, "UTF-8");
			}
			catch (Exception e)
			{
			    
			}
			phonesString = outWri.toString();
	        lastCacheDate = new Date();
	    }
	    else
	    {
	        L.info("DMDXml:getPhoneTypesDocument(): Returning the Cached XML");
	    }
	    return phonesString;
	}
	
	public void defaultMacidAction(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
	{
		
		res.setStatus(200);
		res.setContentType("text/xml");
		L.debug("Inside dmdskuxml: ");
		
		
		OracleDAOFactory oracleDAOFactory = OracleDAOFactory.getInstance();
		OracleEsnLookupDAO daoLookup = (OracleEsnLookupDAO)oracleDAOFactory.createEsnLookupDAO();
		ServletOutputStream out = res.getOutputStream();
		
		StringBuffer statsLogBuf = new StringBuffer("XML|");
		
		Date entryTime = new Date();
		
		String appType = req.getParameter("app_type");
		statsLogBuf.append(appType + "|");		
		statsLogBuf.append(DMDUtils.getClientIP(req) + "|");
		statsLogBuf.append("LOOKUP|");
		statsLogBuf.append("ESN_FEATURES|");
		
		String action = req.getParameter("action");
		
		String mfgCode = "";
		String prodName = "";
		Date effDate;
		String modelId = "";
		if (req.getParameterValues("macid") != null) {
			
			
			String macid = req.getParameter("macid").toUpperCase();
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			out.println("<DMD>");
			
			
			
			//DMDEquipmentIdLookupRequestVO vo = new DMDEquipmentIdLookupRequestVO();
			DMDHubMacIdLookupRequestVO vo = new DMDHubMacIdLookupRequestVO();				

			
			
			//vo.setMeid(DMDUtils.convertMEIDFromHexToDecimal(meid));
			vo.setMacid(macid);
			DMDFULLXMLEQUIPMENTVO dmdFULLXMLEQUIPMENTVO  = null;
			try{
				dmdFULLXMLEQUIPMENTVO  = daoLookup.getDMDFullXMLMacid(vo);
				//String dymaxBam = daoLookup.getDMDSKUMeid(vo);
				if (dmdFULLXMLEQUIPMENTVO != null ) 
				{				
					L.debug("dmdFULLXMLEQUIPMENTVO is not null ");
					//dmdFULLXMLEQUIPMENTVO.get
					if(dmdFULLXMLEQUIPMENTVO.getModelid().trim().length() == 0 ){
						out.println("<STATUS>ERROR</STATUS>");
						out.println("<EQUIPMENT_MACID>No Data Found");
						out.println("</EQUIPMENT_MACID>");
						
						statsLogBuf.append(macid + "|");
						statsLogBuf.append("FALSE|Invalid MACID");
						
					}else{
						modelId  = dmdFULLXMLEQUIPMENTVO.getModelid();
						
						out.println("<STATUS>");
						out.println("<STATUS_STR>Normal</STATUS_STR>");
						out.println("<MESSAGE></MESSAGE>");
						out.println("</STATUS>");
						out.println("<EQUIPMENT_MACID>");
						//Fortify Fix - Cross-site scripting starts
						out.println("<MACID>"+encoder.encodeXMLAttribute(macid)+"</MACID>");
						out.println("<MODEL_ID>"+encoder.encodeXMLAttribute(dmdFULLXMLEQUIPMENTVO.getModelid())+"</MODEL_ID>");
						out.println("<MFG_CODE>"+encoder.encodeXMLAttribute(dmdFULLXMLEQUIPMENTVO.getMfgcode())+"</MFG_CODE>"); 
						out.println("<SHIP_DATE>"+encoder.encodeXMLAttribute(dmdFULLXMLEQUIPMENTVO.getShipdate())+"</SHIP_DATE>"); 
						out.println("<BREW_VERSION>"+encoder.encodeXMLAttribute(dmdFULLXMLEQUIPMENTVO.getBrewversion())+"</BREW_VERSION>");
						out.println("<MESSAGING>"+encoder.encodeXMLAttribute(dmdFULLXMLEQUIPMENTVO.getMessaging())+"</MESSAGING>");
						out.println("<BR_TYPE_1>"+encoder.encodeXMLAttribute(dmdFULLXMLEQUIPMENTVO.getBrtype1())+"</BR_TYPE_1>");
						out.println("<BR_VERSION_1>"+encoder.encodeXMLAttribute(dmdFULLXMLEQUIPMENTVO.getBrversion1())+"</BR_VERSION_1>"); 
						out.println("<BR_USER_AGENT_ID_1>"+encoder.encodeXMLAttribute(dmdFULLXMLEQUIPMENTVO.getBrUserAgentId1())+"</BR_USER_AGENT_ID_1>"); 
						out.println("<BR_TYPE_2>"+encoder.encodeXMLAttribute(dmdFULLXMLEQUIPMENTVO.getBrType2())+"</BR_TYPE_2>");
						out.println("<BR_VERSION_2>"+encoder.encodeXMLAttribute(dmdFULLXMLEQUIPMENTVO.getBrVersion2())+"</BR_VERSION_2>");
						out.println("<BR_USER_AGENT_ID_2>"+encoder.encodeXMLAttribute(dmdFULLXMLEQUIPMENTVO.getBrUserAgentId2())+"</BR_USER_AGENT_ID_2>"); 
						out.println("<JAVA_VERSION>"+encoder.encodeXMLAttribute(dmdFULLXMLEQUIPMENTVO.getJavaVersion())+"</JAVA_VERSION>");
						out.println("<FIRMWARE_VERSION>"+encoder.encodeXMLAttribute(dmdFULLXMLEQUIPMENTVO.getFirmwareVersion())+"</FIRMWARE_VERSION>"); 
						out.println("<OS_TYPE>"+encoder.encodeXMLAttribute(dmdFULLXMLEQUIPMENTVO.getOsType())+"</OS_TYPE>");
						out.println("<OS_VERSION>"+encoder.encodeXMLAttribute(dmdFULLXMLEQUIPMENTVO.getOsVersion())+"</OS_VERSION>"); 
						out.println("<PRL_BASELINE>"+encoder.encodeXMLAttribute(dmdFULLXMLEQUIPMENTVO.getPrlBaseline())+"</PRL_BASELINE>");
						out.println("<PRL_INDEX>"+encoder.encodeXMLAttribute(dmdFULLXMLEQUIPMENTVO.getPrlIndex())+"</PRL_INDEX>");
						out.println("<PRL_NAME>"+encoder.encodeXMLAttribute(dmdFULLXMLEQUIPMENTVO.getPrlName())+"</PRL_NAME>");
						out.println("<PRL_START_DATE>"+encoder.encodeXMLAttribute(dmdFULLXMLEQUIPMENTVO.getPrlStartDate())+"</PRL_START_DATE>"); 
						out.println("<PRL_STATUS>"+encoder.encodeXMLAttribute(dmdFULLXMLEQUIPMENTVO.getPrlStatus())+"</PRL_STATUS>");
						out.println("<DE_ACTIVE_DATE>"+encoder.encodeXMLAttribute(dmdFULLXMLEQUIPMENTVO.getDeActiveDate())+"</DE_ACTIVE_DATE>"); 
						out.println("<MDN>"+encoder.encodeXMLAttribute(dmdFULLXMLEQUIPMENTVO.getMdn())+"</MDN>");
						out.println("<MTN>"+encoder.encodeXMLAttribute(dmdFULLXMLEQUIPMENTVO.getMtn())+"</MTN>");
						out.println("<BILLING_SYSTEM>"+encoder.encodeXMLAttribute(dmdFULLXMLEQUIPMENTVO.getBillingSystem())+"</BILLING_SYSTEM>"); 
						out.println("<LAST_UPDATE>"+encoder.encodeXMLAttribute(dmdFULLXMLEQUIPMENTVO.getLastUpdate())+"</LAST_UPDATE>");
						//Fortify Fix - Cross-site scripting ends
						out.println("</EQUIPMENT_MACID>");
						
						L.debug("Model Id "+modelId);
						ModelDetailVO modelDetailVO = daoLookup.getDMDFullXMLModelMacid(modelId);
						 prodName = modelDetailVO.getProdName();
						 mfgCode = modelDetailVO.getMfgCode();
						effDate = modelDetailVO.getEffDate();
						L.debug("Prod Name "+prodName);
						L.debug("Mfg Code "+mfgCode);
						L.debug("Eff Date "+effDate);
						
						L.debug("Model Id "+modelId);
						EquipmentModelVO equipmentModelVO = daoLookup.getDMDFullXMLEquipmentModelMacid(prodName, mfgCode, effDate);
						L.debug("Prod Name "+prodName);
						L.debug("Mfg Code "+mfgCode);
						L.debug("Eff Date "+effDate);
						out.println("<EQUIPMENT_MODEL>");
						//Fortify Fix - Cross-site scripting starts
						out.println("<PROD_NAME>"+encoder.encodeXMLAttribute(prodName)+"</PROD_NAME>");
						out.println("<MFG_CODE>"+encoder.encodeXMLAttribute(mfgCode)+"</MFG_CODE>");
						out.println("<EFFECTIVE_DATE>"+encoder.encodeXMLAttribute(effDate.toString())+"</EFFECTIVE_DATE>");
						out.println("<PROD_DESC>"+encoder.encodeXMLAttribute(equipmentModelVO.getProdDesc())+"</PROD_DESC>");
						out.println("<MODEL_ALIAS>"+encoder.encodeXMLAttribute(equipmentModelVO.getModelAlias())+"</MODEL_ALIAS>");
						out.println("<PROD_TIER>"+encoder.encodeXMLAttribute(equipmentModelVO.getProdTier())+"</PROD_TIER>");
						out.println("<EQP_MODE>"+encoder.encodeXMLAttribute(equipmentModelVO.getEqpMode())+"</EQP_MODE>");
						out.println("<PROD_TYPE>"+encoder.encodeXMLAttribute(equipmentModelVO.getProdType())+"</PROD_TYPE>");
						out.println("<PROD_TECHNOLOGY>"+encoder.encodeXMLAttribute(equipmentModelVO.getProdTechnology())+"</PROD_TECHNOLOGY>");
						out.println("<IMAGE>"+encoder.encodeXMLAttribute(equipmentModelVO.getImage())+"</IMAGE>");
						out.println("<MICC_OLR_LINK>"+encoder.encodeXMLAttribute(equipmentModelVO.getMiccOlrLink())+"</MICC_OLR_LINK>");
						out.println("<MARKETING_EQP_LINK>"+encoder.encodeXMLAttribute(equipmentModelVO.getMarketEquipLink())+"</MARKETING_EQP_LINK>");
						out.println("<EQP_MODE_CODE>"+encoder.encodeXMLAttribute(equipmentModelVO.getEquipModeCode())+"</EQP_MODE_CODE>");
						out.println("<USER_MANUAL_MIM_LINK>"+encoder.encodeXMLAttribute(equipmentModelVO.getUserManualMIMLink())+"</USER_MANUAL_MIM_LINK>");
						out.println("<USER_MANUAL_PDF_LINK>"+encoder.encodeXMLAttribute(equipmentModelVO.getUserManualPDFLink())+"</USER_MANUAL_PDF_LINK>");
						//Fortify Fix - Cross-site scripting ends
						out.println("</EQUIPMENT_MODEL>");				
						
						EquipmentFeatureVO equipmentFeatureVO = daoLookup.getDMDFullXMLFeatureMacid(prodName, mfgCode, effDate);

						out.println("<FEATURES>");
						//Fortify Fix - Cross-site scripting starts
						out.println("<PROD_NAME>"+encoder.encodeXMLAttribute(prodName)+"</PROD_NAME>");	 
						out.println("<MFG_CODE>"+encoder.encodeXMLAttribute(mfgCode)+"</MFG_CODE>");	
						out.println("<EFFECTIVE_DATE>"+encoder.encodeXMLAttribute(effDate.toString())+"</EFFECTIVE_DATE>");	 
						out.println("<E911>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getE911())+"</E911>");	
						out.println("<BATTERY_CHARGER_TIME>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getBattery_charger_time())+"</BATTERY_CHARGER_TIME>");	 
						out.println("<PICTURE_CALLER_ID>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getPicture_caller_id())+"</PICTURE_CALLER_ID>");	
						out.println("<MOBILE_MESSAGING>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getMobile_messaging())+"</MOBILE_MESSAGING>");	
						out.println("<VOICE_ACTIVATION>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getVoice_activation())+"</VOICE_ACTIVATION>");	
						out.println("<VOICE_MEMO>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getVoice_memo())+"</VOICE_MEMO>");	
						out.println("<STREAMING_VIDEO>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getStreaming_video())+"</STREAMING_VIDEO>");	 
						out.println("<MOBILE_IP>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getMobile_ip())+"</MOBILE_IP>");	
						out.println("<BLUETOOTH>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getBluetooth())+"</BLUETOOTH>");	
						out.println("<MOBILE_WEB>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getMobile_web())+"</MOBILE_WEB>");	
						out.println("<PUSH_TO_TALK>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getPush_to_talk())+"</PUSH_TO_TALK>");	
						out.println("<IOTA>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getIota())+"</IOTA>");	
						out.println("<OTA>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getOta())+"</OTA>");	
						out.println("<BREW>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getBrew())+"</BREW>");	
						out.println("<J2ME>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getJ2me())+"</J2ME>");	
						out.println("<QUICK2NET>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getQuick2net())+"</QUICK2NET>");	 
						out.println("<SPANISH>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getSpanish())+"</SPANISH>");	
						out.println("<OTAPA>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getOtapa())+"</OTAPA>");	
						out.println("<EVRC>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getEvrc())+"</EVRC>");	
						out.println("<GLOBAL_PHONE>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getGlobal_phone())+"</GLOBAL_PHONE>");	 
						out.println("<VCAST>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getVcast())+"</VCAST>");	
						out.println("<MOD>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getMod_macid())+"</MOD>");	
						out.println("<LBS_TRACK>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getLbs_track())+"</LBS_TRACK>");	 
						out.println("<MNAI>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getMnai())+"</MNAI>");	
						out.println("<FOTA>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getFota())+"</FOTA>");	
						out.println("<FLASHCAST>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getFlashcast())+"</FLASHCAST>");	 
						out.println("<MEDIAFLO>"+encoder.encodeXMLAttribute(equipmentFeatureVO.getMediaflo())+"</MEDIAFLO>");
						//Fortify Fix - Cross-site scripting ends
						
						
						Vector emsFeatureVector = daoLookup.getDMDFullXMLEMSFeature(prodName, mfgCode, effDate);
						EMSFeaturesVO emsFeaturesVO = null;
						int sizeEMSFeature = emsFeatureVector.size();
						out.println("<EMS_FEATURES>");	
					    for (int i = 0; i < sizeEMSFeature; i++){
					    	emsFeaturesVO = (EMSFeaturesVO)emsFeatureVector.get(i);
					    	//out.println("<FEATURE FEATURE_DESC="+emsFeaturesVO.getFeatureDesc()+" COMMENTS ="+emsFeaturesVO.getFeatureComment()+"/>");
					    	//out.println("<FEATURE>"+ "FEATURE_DESC="+emsFeaturesVO.getFeatureDesc()+" COMMENTS ="+emsFeaturesVO.getFeatureComment()+"</FEATURE>");
					    	//out.println("<FEATURE>"+ "FEATURE_DESC="+emsFeaturesVO.getFeatureDesc()+" COMMENTS ="+emsFeaturesVO.getFeatureComment()+"</FEATURE>");
					    	out.println("<FEATURE>");
						    out.println("<FEATURE_DESC>"+encoder.encodeXMLAttribute(emsFeaturesVO.getFeatureDesc())+"</FEATURE_DESC>");
						    out.println("<COMMENTS>"+encoder.encodeXMLAttribute(emsFeaturesVO.getFeatureComment())+"</COMMENTS>");
					    	out.println("</FEATURE>");
					    }
					    out.println("</EMS_FEATURES>");
					    	
						out.println("</FEATURES>");	
						
						
						EquipmentPhysicalAttributeVO equipmentPhysicalAttributeVO = daoLookup.getDMDFullXMLPhyAttibuteMacid(prodName, mfgCode, effDate);

						out.println("<PHYSICAL_ATTRIBUTES>");
						out.println("<PROD_NAME>"+encoder.encodeXMLAttribute(prodName)+"</PROD_NAME>"); 
						out.println("<MFG_CODE>"+encoder.encodeXMLAttribute(mfgCode)+"</MFG_CODE>");
						out.println("<EFFECTIVE_DATE>"+encoder.encodeXMLAttribute(effDate.toString())+"</EFFECTIVE_DATE>"); 
						out.println("<FORM_FACTOR_STYLE>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getForm_factor_style())+"</FORM_FACTOR_STYLE>");
						out.println("<DUAL_LCD>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getDual_lcd())+"</DUAL_LCD>");
						out.println("<CHIPSET>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getChipset())+"</CHIPSET>"); 
						out.println("<ANTENNA_TYPE>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getAntenna_type())+"</ANTENNA_TYPE>"); 
						out.println("<WEIGHT>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getWeight())+"</WEIGHT>");
						out.println("<PHONE_SIZE>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getPhone_size())+"</PHONE_SIZE>"); 
						out.println("<BATTERY_TYPE>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getBattery_type())+"</BATTERY_TYPE>"); 
						out.println("<TALK_TIME>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getTalk_time())+"</TALK_TIME>");
						out.println("<STANDBY_TIME_IS95>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getStandby_time_is95())+"</STANDBY_TIME_IS95>"); 
						out.println("<STANDBY_TIME_1X>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getStandby_time_1x())+"</STANDBY_TIME_1X>");
						out.println("<DISPLAY_TYPE>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getDisplay_type())+"</DISPLAY_TYPE>");
						out.println("<DISPLAY_TECHNOLOGY>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getDisplay_technology())+"</DISPLAY_TECHNOLOGY>"); 
						out.println("<DISPLAY_SIZE>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getDisplay_size())+"</DISPLAY_SIZE>");
						out.println("<NUM_DISPLAY_COLOR>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getNum_display_color())+"</NUM_DISPLAY_COLOR>"); 
						out.println("<SRAM_SIZE>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getSram_size())+"</SRAM_SIZE>");
						out.println("<FLASH_SIZE>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getFlash_size())+"</FLASH_SIZE>");
						out.println("<SPEAKER_TYPE>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getSpeaker_type())+"</SPEAKER_TYPE>"); 
						out.println("<VIBRA>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getVibra())+"</VIBRA>");
						out.println("<NAVIGATION_KEY>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getNavigation_key())+"</NAVIGATION_KEY>"); 
						out.println("<CONNECTER>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getConnector())+"</CONNECTER>");
						out.println("<PHONE_BOOK_ENTRIES>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getPhone_book_entries())+"</PHONE_BOOK_ENTRIES>"); 
						out.println("<R_UIM>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getR_uim())+"</R_UIM>");
						out.println("<BACKLIGHT>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getBacklight())+"</BACKLIGHT>"); 
						out.println("<CAMERA>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getCamera())+"</CAMERA>");
						out.println("<SEC255>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getSec255())+"</SEC255>");
						out.println("<MIL_STANDARD>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getMil_standard())+"</MIL_STANDARD>"); 
						out.println("<TTY>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getTty())+"</TTY>");
						out.println("<PRELOAD_RINGTONES>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getPreload_ringtones())+"</PRELOAD_RINGTONES>"); 
						out.println("<TOTAL_RINGTONES>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getTotal_ringtones())+"</TOTAL_RINGTONES>");
						out.println("<PRELOAD_GAMES>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getPreload_games())+"</PRELOAD_GAMES>");
						out.println("<TOTAL_GAMES>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getTotal_games())+"</TOTAL_GAMES>");
						out.println("<DISPLAY_RESOLUTION>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getDisplay_resolution())+"</DISPLAY_RESOLUTION>"); 
						out.println("<DISPLAY_DIMENSION>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getDisplay_dimension())+"</DISPLAY_DIMENSION>");
						out.println("<SD_CARD_SLOT>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getSd_card_slot())+"</SD_CARD_SLOT>");
						out.println("<INFRARED_PORT>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getInfrared_port())+"</INFRARED_PORT>");
						out.println("<MP3_PLAYER>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getMp3_player())+"</MP3_PLAYER>");
						out.println("<DATA_SPEED>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getData_speed())+"</DATA_SPEED>"); 
						out.println("<HEADSET_JACK>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getHeadset_jack())+"</HEADSET_JACK>");
						out.println("<PC_CARD>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getPc_card())+"</PC_CARD>");
						out.println("<FM_STEREO_RADIO>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getFm_stereo_radio())+"</FM_STEREO_RADIO>"); 
						out.println("<CONNECTIVITY_KIT>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getConnectivity_kit())+"</CONNECTIVITY_KIT>");
						out.println("<MODEM_1X>"+encoder.encodeXMLAttribute(equipmentPhysicalAttributeVO.getModem_1x())+"</MODEM_1X>");
						out.println("</PHYSICAL_ATTRIBUTES>");
						
						TechnologyVO technologyVO = daoLookup.getDMDFullXMLTechnologyMacid(prodName, mfgCode, effDate);
						
						out.println("<TECHNOLOGY>");
						out.println("<PROD_NAME>"+encoder.encodeXMLAttribute(prodName)+"</PROD_NAME>");
						out.println("<MFG_CODE>"+encoder.encodeXMLAttribute(mfgCode)+"</MFG_CODE>");
						out.println("<EFFECTIVE_DATE>"+encoder.encodeXMLAttribute(effDate.toString())+"</EFFECTIVE_DATE>"); 
						out.println("<EXPRESS_NETWORK>"+encoder.encodeXMLAttribute(technologyVO.getExpress_network())+"</EXPRESS_NETWORK>");
						out.println("<IS95>"+encoder.encodeXMLAttribute(technologyVO.getIs95())+"</IS95>");
						out.println("<CDMA800>"+encoder.encodeXMLAttribute(technologyVO.getCdma800())+"</CDMA800>"); 
						out.println("<CDMA1900>"+encoder.encodeXMLAttribute(technologyVO.getCdma1900())+"</CDMA1900>");
						out.println("<WCDMA>"+encoder.encodeXMLAttribute(technologyVO.getWcdma())+"</WCDMA>");
						out.println("<TDMA>"+encoder.encodeXMLAttribute(technologyVO.getTdma())+"</TDMA>");
						out.println("<GSM>"+encoder.encodeXMLAttribute(technologyVO.getGsm())+"</GSM>");
						out.println("<GPRS>"+encoder.encodeXMLAttribute(technologyVO.getGprs())+"</GPRS>");
						out.println("<CDMA_1XRTT>"+encoder.encodeXMLAttribute(technologyVO.getCdma_1xrtt())+"</CDMA_1XRTT>"); 
						out.println("<CDMA_1XEVDO>"+encoder.encodeXMLAttribute(technologyVO.getCdma_1xevdo())+"</CDMA_1XEVDO>");
						out.println("<MIP_802_11>"+encoder.encodeXMLAttribute(technologyVO.getMip_802_11())+"</MIP_802_11>");
						out.println("</TECHNOLOGY>");
						
						LaunchPackageMacidVO launchPackageMacidVO = daoLookup.getDMDFullXMLLaunchPackage(prodName, mfgCode, effDate);
						  out.println("<LAUNCH_PACKAGE>");
						  out.println("<PROD_NAME>"+encoder.encodeXMLAttribute(prodName)+"</PROD_NAME>");
						  out.println("<MFG_CODE>"+encoder.encodeXMLAttribute(mfgCode)+"</MFG_CODE>");
						  out.println("<EFFECTIVE_DATE>"+encoder.encodeXMLAttribute(effDate.toString())+"</EFFECTIVE_DATE>"); 
						  out.println("<AFFECTED_AREA>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getAffected_area())+"</AFFECTED_AREA>");
						  out.println("<NETWORK_TYPE>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getNetwork_type())+"</NETWORK_TYPE>");
						  out.println("<DYMAX_AIRTOUCH>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getDymax_airtouch())+"</DYMAX_AIRTOUCH>"); 
						  out.println("<DYMAX_BAM>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getDymax_bam())+"</DYMAX_BAM>");
						  out.println("<GERS_AIRTOUCH>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getGers_airtouch())+"</GERS_AIRTOUCH>");
						  out.println("<GERS_BAM>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getGers_bam())+"</GERS_BAM>");
						  out.println("<NW_CAMBAR_AIRTOUCH>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getNw_cambar_airtouch())+"</NW_CAMBAR_AIRTOUCH>"); 
						  out.println("<NW_CAMBAR_BAM>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getNw_cambar_bam())+"</NW_CAMBAR_BAM>");
						  out.println("<PR_MMS_AIRTOUCH>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getPr_mms_airtouch())+"</PR_MMS_AIRTOUCH>");
						  out.println("<PR_MMS_BAM>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getPr_mms_bam())+"</PR_MMS_BAM>"); 
						  out.println("<RT_RETAIL_AIRTOUCH>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getRt_retail_airtouch())+"</RT_RETAIL_AIRTOUCH>");
						  out.println("<RT_RETAIL_BAM>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getRt_retail_bam())+"</RT_RETAIL_BAM>");
						  out.println("<GT_ENDURA_AIRTOUCH>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getGt_endura_airtouch())+"</GT_ENDURA_AIRTOUCH>"); 
						  out.println("<GT_ENDURA_BAM>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getGt_endura_bam())+"</GT_ENDURA_BAM>"); 
						  out.println("<DYMAX_AIRTOUCH_FRU>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getDymax_airtouch_fru())+"</DYMAX_AIRTOUCH_FRU>"); 
						  out.println("<DYMAX_BAM_FRU>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getDymax_bam_fru())+"</DYMAX_BAM_FRU>"); 
						  out.println("<GERS_AIRTOUCH_FRU>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getGers_airtouch_fru())+"</GERS_AIRTOUCH_FRU>"); 
						  out.println("<GERS_BAM_FRU>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getGers_bam_fru())+"</GERS_BAM_FRU>"); 
						  out.println("<NW_CAMBAR_AIRTOUCH_FRU>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getNw_cambar_aritouch_fru())+"</NW_CAMBAR_AIRTOUCH_FRU>"); 
						  out.println("<NW_CAMBER_BAM_FRU>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getNw_camber_bam_fru())+"</NW_CAMBER_BAM_FRU>"); 
						  out.println("<PR_MMS_AIRTOUCH_FRU>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getPr_mms_airtouch_fru())+"</PR_MMS_AIRTOUCH_FRU>"); 
						  out.println("<PR_MMS_BAM_FRU>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getPr_mms_bam_fru())+"</PR_MMS_BAM_FRU>");
						  out.println("<RT_RETAIL_AIRTOUCH_FRU>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getRt_retail_airtouch_fru())+"</RT_RETAIL_AIRTOUCH_FRU>"); 
						  out.println("<RT_RETAIL_BAM_FRU>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getRt_retail_bam_fru())+"</RT_RETAIL_BAM_FRU>"); 
						  out.println("<GT_ENDURA_AIRTOUCH_FRU>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getGt_endura_airtouch_fru())+"</GT_ENDURA_AIRTOUCH_FRU>"); 
						  out.println("<GT_ENDURA_BAM_FRU>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getGt_endura_bam_fru())+"</GT_ENDURA_BAM_FRU>"); 
						  out.println("<VISION_PROD_ID>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getVision_prod_id())+"</VISION_PROD_ID>"); 
						  out.println("<LITERATURE_CODE>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getLiterature_code())+"</LITERATURE_CODE>"); 
						  out.println("<POSITIONING>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getPositioning())+"</POSITIONING>"); 
						  out.println("<WARRANTY>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getWarranty())+"</WARRANTY>"); 
						  out.println("<PROD_RETIREMENT_DATE>"+encoder.encodeXMLAttribute(launchPackageMacidVO.getProd_retirement_date())+"</PROD_RETIREMENT_DATE>"); 
						  out.println("</LAUNCH_PACKAGE>");
						  

						  SoftwareMacidVO softwareMacidVO = daoLookup.getDMDFullXMLSoftware(prodName, mfgCode, effDate);
						  
						  out.println("<SOFTWARE>");
						  
						  out.println("<PROD_NAME>"+encoder.encodeXMLAttribute(prodName)+"</PROD_NAME>"); 
						  out.println("<MFG_CODE>"+encoder.encodeXMLAttribute(mfgCode)+"</MFG_CODE>");
						  out.println("<EFFECTIVE_DATE>"+encoder.encodeXMLAttribute(effDate.toString())+"</EFFECTIVE_DATE>"); 
						  out.println("<TEXT_ENTRY_SOFTWARE>"+encoder.encodeXMLAttribute(softwareMacidVO.getText_entry_software())+"</TEXT_ENTRY_SOFTWARE>");
						  out.println("<BR_TYPE>"+encoder.encodeXMLAttribute(softwareMacidVO.getBr_type())+"</BR_TYPE>");
						  out.println("<BR_VERSION>"+encoder.encodeXMLAttribute(softwareMacidVO.getBr_version())+"</BR_VERSION>"); 
						  out.println("<BR_USER_AGENT_ID>"+encoder.encodeXMLAttribute(softwareMacidVO.getBr_user_agent_id())+"</BR_USER_AGENT_ID>");
						  out.println("<OS_TYPE>"+encoder.encodeXMLAttribute(softwareMacidVO.getOs_type())+"</OS_TYPE>");
						  out.println("<OS_VERSION>"+encoder.encodeXMLAttribute(softwareMacidVO.getOs_version())+"</OS_VERSION>"); 
						  out.println("<FIRMWARE_VERSION>"+encoder.encodeXMLAttribute(softwareMacidVO.getFirmware_version())+"</FIRMWARE_VERSION>"); 
						  out.println("<DSU_MAKE>"+encoder.encodeXMLAttribute(softwareMacidVO.getDsu_make())+"</DSU_MAKE>");
						  out.println("<DSU_MODEL>"+encoder.encodeXMLAttribute(softwareMacidVO.getDsu_model())+"</DSU_MODEL>");
						  out.println("<DSU_FIRMWARE_VERSION>"+encoder.encodeXMLAttribute(softwareMacidVO.getDsu_firmware_version())+"</DSU_FIRMWARE_VERSION>");
						   
						  out.println("</SOFTWARE>");
						  
						  
						statsLogBuf.append(macid + "|");
						statsLogBuf.append("TRUE|");
					}
				}
				else {
					out.println("<STATUS>ERROR</STATUS>");
					out.println("<MACID>" + encoder.encodeXMLAttribute(macid) + "</MACID>");					
					statsLogBuf.append(macid + "|");
					statsLogBuf.append("FALSE|Invalid MACID");
					
				}
				
				out.println("</DMD>");
				
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}else {
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			out.println("<DMD>");
			out.println("	<STATUS>ERROR</STATUS>"); 
			out.println("	<MESSAGE>Please input ESN or MEID to search the database</MESSAGE>"); 
			out.println("</DMD>");
		}
		
		Date exitTime = new Date();
		String transId = req.getParameter("transaction_id");
		if (transId == null || transId.trim().equals(""))
		{
			transId = "NONE";
		}
		
		long prcTime = exitTime.getTime() - entryTime.getTime();
		DMDLogs.getStatsLogger().info(statsLogBuf.toString());
		DMDLogs.getEStatsLogger().info(
				statsLogBuf.toString() + "|"
				+ transId + "|"
				+ DMDProps.ldf.format(entryTime) + "|"
				+ DMDProps.ldf.format(exitTime) + "|" + prcTime);
		
		out.flush();
		out.close();
		
		

		//#BQVT-966
		String macId =  req.getParameter("macid");
		String app_type =req.getParameter("app_type");
		String actionType = req.getParameter("action");
		String trans_id = req.getParameter("transaction_id");
		
		StringBuffer xmlReqForNewCassWriteQueue = new StringBuffer("");						
		xmlReqForNewCassWriteQueue.append(macId==null ? "" : "macid="+macId+"&");
		xmlReqForNewCassWriteQueue.append(app_type==null ? "" : "app_type="+app_type+"&");
		xmlReqForNewCassWriteQueue.append(actionType==null ? "" : "action="+actionType+"&");
		xmlReqForNewCassWriteQueue.append(trans_id==null ? "" : "transaction_id="+trans_id+"&");
				//feb 2019 BQVT-966
		//send input request to new dmd write queue for cassandra processing
		/*//need to be handled via on-off toggle
		String serviceEnabled = DBUtils.getDBPropertyValue("DMDXMLCASSSERVICE");
		L.debug("serviceEnabled-"+serviceEnabled);
		//add service name to the xml
		if("Y".equalsIgnoreCase(serviceEnabled))
		{
					String cassXmlMsg= xmlReqForNewCassWriteQueue.toString();
					if(cassXmlMsg.endsWith("&"))
						cassXmlMsg=cassXmlMsg.substring(0, cassXmlMsg.length()-1);
			
			String messageText =  "<xmlReq><apiName>DMDXml</apiName><dmdXmlReq><![CDATA["+cassXmlMsg+"]]></dmdXmlReq></xmlReq>";
			L.debug("messageText-"+messageText);
			DMDMqClient mqClient = new DMDMqClient();
			mqClient.sendMessageToQueue(messageText);
			L.debug("msg succeffully sent cassandra queue");
			
		}*/
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void defaultAction(HttpServletRequest req, HttpServletResponse res,String lteCategory)
		throws ServletException, IOException {
		res.setContentType("text/xml");
		ServletOutputStream out = res.getOutputStream();
		try {
			BulkDeviceSimLookupDAO.fetchDeivecNameAndDevceSKU( new PrintStream(out, true), lteCategory);
			out.flush();
			out.close();
		}
		catch(Exception e) {
			out.println("<?xml version=\"1.0\"?><dmd><responseHeader><statusCode>02</statusCode><message>DMD Application error</message></responseHeader><responseBody>");
	       	 out.println("</responseBody></dmd>");
	       	out.flush();
			out.close();
		}
	}

//	}

	private static String phonesString = null;
	//private static Document phonesDoc = null;
	private Date lastCacheDate = null;
	
}
