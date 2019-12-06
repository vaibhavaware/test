/*
 * Created on Apr 30, 2012
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.vzw.dmd;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.vzw.dmd.dao.DeviceLookupDAO;
import com.vzw.dmd.util.DMDConstants;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.DMDUtils;
import com.vzw.dmd.util.ILteXmlCreator;
import com.vzw.dmd.util.XSSEncoder;
import com.vzw.dmd.util.XXEDisabler;
import com.vzw.dmd.util.XmlUtils;

/**
 * @author c0palk1
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
@SuppressWarnings("serial")
public class DMDMultiDeviceLookupAPI extends HttpServlet implements ILteXmlCreator {
	private static Logger L = Logger.getLogger(DMDLogs.getLogName(DMDMultiDeviceLookupAPI.class));
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
	* @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	*/
	public void defaultAction(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		StringBuffer statsLogBuf = new StringBuffer("DMD_MULTI_DEVICE_LOOKUP").append(DMDConstants.DMD_PIPE);
		Date entryTime = new Date();
		
		  
		
		ServletOutputStream out = res.getOutputStream();
		String returnXML = null;
		String deviceInfoXml = null;
		String deviceIdTypeListStr = null;
		String serviceName ="";
		try{
			String xmlReq = req.getParameter("xmlReq");
			if(xmlReq==null || xmlReq.trim().length()==0){
				xmlReq = req.getParameter("xmlreqdoc");
			}
			if(xmlReq != null) {
				xmlReq = xmlReq.trim();
			}
			
			//Fortify Fix - XML External Entity Injection
			DocumentBuilderFactory docBuilderFactory = new XXEDisabler().disableDBF(DocumentBuilderFactory.newInstance());
			Document document = docBuilderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(xmlReq)));
			
			
			serviceName = XmlUtils.getValue(document, "/dmd/serviceName");
			if("multiDeviceIDLookup".equalsIgnoreCase(serviceName)){
				statsLogBuf.append("Service_Name=" + serviceName).append(DMDConstants.DMD_PIPE);
			
				statsLogBuf.append("CLIENT_ID=" + XmlUtils.getValue(document, "/dmd/requestHeader/clientId")).append(DMDConstants.DMD_PIPE);
				statsLogBuf.append("APP_TYPE=" + XmlUtils.getValue(document, "/dmd/requestHeader/appType")).append(DMDConstants.DMD_PIPE);
				statsLogBuf.append(getLoggerData(XmlUtils.getValue(document, "/dmd/requestHeader/appType"), "", req));
			
				String deviceIdArr[] = getDeviceIdList(document);
				//L.debug("The device id list size is: "+ deviceIdArr.length);
				if(deviceIdArr == null || deviceIdArr.length == 0 ) {
						throw new Exception("Input device Id list is empty ");
				}
				if(deviceIdArr.length > 100) {
					throw new Exception("Input device ID count is more than 100");
				}
				statsLogBuf.append("DEVICE_ID_LIST_COUNT=" + deviceIdArr.length).append(DMDConstants.DMD_PIPE);
							
					deviceInfoXml = DeviceLookupDAO.retriveMultiDeviceIdInfoXml(deviceIdArr);
			
			}
			else {
			statsLogBuf.append("CLIENT_ID=" + XmlUtils.getValue(document, "/dmd/clientId")).append(DMDConstants.DMD_PIPE);
			statsLogBuf.append("APP_TYPE=" + XmlUtils.getValue(document, "/dmd/requestHeader/appType")).append(DMDConstants.DMD_PIPE);
			statsLogBuf.append(getLoggerData(XmlUtils.getValue(document, "/dmd/requestHeader/appType"), "", req));

			String deviceSkuArr[] = getDeviceSkuList(document);
			if(deviceSkuArr == null || deviceSkuArr.length == 0) {
				throw new Exception("Input device sku list is empty.");
			}			
			statsLogBuf.append("DEVICE_SKU_LIST_COUNT=" + deviceSkuArr.length).append(DMDConstants.DMD_PIPE);
													
				deviceInfoXml = DeviceLookupDAO.retriveMultiDeviceSkuInfoXml(deviceSkuArr);
			}
			
			statsLogBuf.append("STATUS=Success").append(DMDConstants.DMD_PIPE);
			returnXML = createConsolidatedXml("00", "SUCCESS", deviceInfoXml,serviceName,null);
			
		}catch (Exception e){
			L.error("Unable to process request.", e);
			
			statsLogBuf.append("STATUS=Failure");
			statsLogBuf.append(DMDConstants.DMD_PIPE);
			statsLogBuf.append(getLoggerData("", "", req));
			
						
			returnXML = createConsolidatedXml("-1", "Invalid input or device info not found. ", null,serviceName,e.getMessage());			
		}finally{
			Date exitTime = new Date();
			DMDLogs.getStatsLogger().info(statsLogBuf.toString());
			long prcTime = exitTime.getTime() - entryTime.getTime();
			DMDLogs.getEStatsLogger().info(
                    statsLogBuf.toString() + DMDConstants.DMD_PIPE                    
                            + DMDProps.ldf.format(entryTime) + DMDConstants.DMD_PIPE
                            + DMDProps.ldf.format(exitTime) + DMDConstants.DMD_PIPE + prcTime);
		}
		//Fortify Fix - Cross-site scripting
		out.println(new XSSEncoder().encodeXML(returnXML));
		out.flush();
		out.close();
	}
	
	private String createConsolidatedXml(String statusCd, String msg, String body,String serviceName,String errorMsg) {
		StringBuffer sbReturnXML=new StringBuffer(XML_RESPONSE_START);
		//add header. status and message
		sbReturnXML.append(statusCd)
					.append(STATUS_CODE_END)
					.append(MESSAGE_START);
					if("multiDeviceIDLookup".equalsIgnoreCase(serviceName) && null != errorMsg){
						sbReturnXML.append(errorMsg);
					}
					else{
						sbReturnXML.append(msg);	
					}					
					sbReturnXML.append(MESSAGE_END)
					.append(RESPONSE_HEADER_END);
		//add body					
		sbReturnXML.append(RESPONSE_BODY_START);
		if(body != null) {
			sbReturnXML.append(body);
		}
		sbReturnXML.append(RESPONSE_BODY_END).append(DMD_END);
		
		return sbReturnXML.toString();
		
	}
	
	private String[] getDeviceSkuList(Document document) throws Exception {
		NodeList nl = XmlUtils.getMultipleNodes(document, "/dmd/requestBody/deviceSkuList/deviceSku");
		int size = nl.getLength();
		List<String> tmpList = new ArrayList<String>();
		
		for (int i=0; i<size; i++) {
			String str = XmlUtils.nodeToString(nl.item(i));
			if(str != null) {
				str = str.trim();
				if(!str.equals("")) {
					tmpList.add(str);
				}
			}
		}
		
		size = tmpList.size();
		String devSkuArr[] = new String[size];
		for (int i=0; i<size; i++) {
			devSkuArr[i] = tmpList.get(i);
		}
		
		
		return devSkuArr;
	}
	private String[] getDeviceIdList(Document document) throws Exception {
		NodeList nl = XmlUtils.getMultipleNodes(document, "/dmd/requestBody/deviceIdList/deviceId");
		int size = nl.getLength();
		List<String> tmpList = new ArrayList<String>();
		
		for (int i=0; i<size; i++) {
			String str = XmlUtils.nodeToString(nl.item(i));
			if(str != null) {
				str = str.trim();
				if(!str.equals("")) {
					tmpList.add(str);
				}
			}
		}
		
		size = tmpList.size();
		String devIdArr[] = new String[size];
		for (int i=0; i<size; i++) {
			devIdArr[i] = tmpList.get(i);
		}
		
		
		return devIdArr;
	}
	
	
private StringBuffer getLoggerData(String appType,String statusMessage,HttpServletRequest req){
		
		StringBuffer statsLogBuf = new StringBuffer();
		
		 statsLogBuf.append(appType)
		.append(DMDConstants.DMD_PIPE)
		.append(DMDUtils.getClientIP(req))
		.append(DMDConstants.DMD_PIPE)
		.append(DMDConstants.LOOKUP)
		.append(DMDConstants.DMD_PIPE)
		.append(DMDConstants.MULTI_DEVICE_API)
		.append(DMDConstants.DMD_PIPE)	 
		.append(DMDConstants.DMD_TRUE)
		.append(DMDConstants.DMD_PIPE)
		.append(statusMessage);	
		 
		 return statsLogBuf;
	}
	
}
