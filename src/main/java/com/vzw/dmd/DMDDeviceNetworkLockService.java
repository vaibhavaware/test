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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.vzw.dmd.dao.DMDDeviceNetworkLockDAO;
import com.vzw.dmd.util.DMDConstants;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.ILteXmlCreator;
import com.vzw.dmd.util.XXEDisabler;
import com.vzw.dmd.util.XmlUtils;
import com.vzw.dmd.valueobject.DeviceLockInfo;
import com.vzw.dmd.valueobject.DeviceNetworkLockRequestVO;
import com.vzw.dmd.valueobject.DeviceNetworkLockResponseVO;

/**
 * @author vaidya
 *
 */
public class DMDDeviceNetworkLockService extends HttpServlet implements ILteXmlCreator {
	private static Logger L =
			Logger.getLogger(DMDLogs.getLogName(DMDDeviceNetworkLockService.class));
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
	 * @see javax.servlet.GenericServlet#void ()
	 */
	public void init() throws ServletException {
		super.init();
	}

	/**
	 * @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void defaultAction(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		StringBuffer statsLogBuf = new StringBuffer(DMDConstants.XML_NEW);
		statsLogBuf.append(DMDConstants.DMD_PIPE);
		Date entryTime = new Date();
		String returnXML = null;
		ServletOutputStream out = res.getOutputStream();
		try{
			String xmlReq = req.getParameter("xmlReq");
			L.debug("The request parameter is"+req.getParameter("xmlReq"));

			if(xmlReq==null || xmlReq.trim().length()==0){
				xmlReq = req.getParameter("xmlreqdoc");
			}
			if(xmlReq != null) {
				xmlReq = xmlReq.trim();
			}
			L.info("DMDDeviceNetworkLockService request xml is :"+xmlReq);

			//Fortify Fix - XML External Entity Injection
			DocumentBuilderFactory docBuilderFactory = new XXEDisabler().disableDBF(DocumentBuilderFactory.newInstance());
			Document document = docBuilderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(xmlReq)));

			DeviceNetworkLockRequestVO request = new DeviceNetworkLockRequestVO();
			request.setRequestId(XmlUtils.getValue(document, "/dmd/requestHeader/requestId"));
			request.setClientId(XmlUtils.getValue(document, "/dmd/requestHeader/clientId"));
			request.setRequestType(XmlUtils.getValue(document, "/dmd/requestHeader/requestType"));
			request.setRequestApplication(XmlUtils.getValue(document, "/dmd/requestHeader/requestApplication"));		
			request.setUserId(XmlUtils.getValue(document, "/dmd/requestHeader/userId"));
			request.setServiceName(XmlUtils.getValue(document, "/dmd/requestBody/serviceName"));

			List<DeviceLockInfo> deviceLockInfoList = new ArrayList<DeviceLockInfo>();			
			getDeviceLockInfoList(document, deviceLockInfoList);
			L.debug("Total number of deviceLockInfo :"+deviceLockInfoList.size());			
			request.setDeviceLockInfoList(deviceLockInfoList);

			statsLogBuf.append("CLIENT_ID=" + XmlUtils.getValue(document, "/dmd/requestHeader/clientId")).append(DMDConstants.DMD_PIPE);
			statsLogBuf.append("REQUEST_TYPE=" + XmlUtils.getValue(document, "/dmd/requestHeader/requestType")).append(DMDConstants.DMD_PIPE);
			statsLogBuf.append("TRANSACTION_ID=" + XmlUtils.getValue(document, "/dmd/requestHeader/requestId")).append(DMDConstants.DMD_PIPE);
			statsLogBuf.append("Service_Name=" + XmlUtils.getValue(document, "/dmd/requestBody/serviceName")).append(DMDConstants.DMD_PIPE);

			if( deviceLockInfoList == null || deviceLockInfoList.size() == 0 ) {
				throw new Exception("Input deviceLockInfo list is empty ");
			}
			else{
				DeviceNetworkLockResponseVO	response = DMDDeviceNetworkLockDAO.writeDeviceNetworkLockData(request);
				if (response != null) {
					L.debug("DB response Not Null :"+response);
					returnXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><DMD><requestId>" + response.getRequestId() + "</requestId><requestType>DEVICE_NETWORK_LOCK</requestType>";
					if (response.getErrorIds() == null || response.getErrorIds().length() == 0) {
						returnXML += "<statusCode>00</statusCode><statusMessage>SUCCESS</statusMessage>"; 
					}
					else {
						returnXML += "<statusCode>01</statusCode><statusMessage>FAILED [" +response.getErrorIds().toString() + "]</statusMessage>";
					}
					returnXML += "</DMD>";					
				}
				else {
					L.debug("DB response is Null :"+response);
					returnXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><DMD><requestId>" + request.getRequestId() + "</requestId><requestType>DEVICE_NETWORK_LOCK</requestType><statusCode>01</statusCode><statusMessage>FAILED</statusMessage></DMD>";
				}
			}						
		}catch (Exception e){
			L.error("Unable to process request.", e);
			statsLogBuf.append("STATUS=Failure");
			statsLogBuf.append(DMDConstants.DMD_PIPE);
			returnXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><DMD><requestId>-1</requestId><requestType>DEVICE_NETWORK_LOCK</requestType><statusCode>01</statusCode><statusMessage>DMD Application Error.</statusMessage></DMD>";
		}finally{
			Date exitTime = new Date();
			DMDLogs.getStatsLogger().info(statsLogBuf.toString());
			long prcTime = exitTime.getTime() - entryTime.getTime();
			DMDLogs.getEStatsLogger().info(
					statsLogBuf.toString() + DMDConstants.DMD_PIPE                    
					+ DMDProps.ldf.format(entryTime) + DMDConstants.DMD_PIPE
					+ DMDProps.ldf.format(exitTime) + DMDConstants.DMD_PIPE + prcTime);
			L.info("DMDDeviceNetworkLockService return xml is :"+returnXML);
		}		
		res.setContentType("text/xml");
		out.println(returnXML);
		out.flush();
		out.close();
	}

	private void getDeviceLockInfoList(Document document, List<DeviceLockInfo> deviceLockInfoList) throws Exception {
		NodeList nl = XmlUtils.getMultipleNodes(document, "/dmd/requestBody/deviceLockList/deviceLockInfo");
		int size = nl.getLength();		
		DeviceLockInfo deviceLockInfo = null;
		for (int i=0; i<size; i++) {
			deviceLockInfo = new DeviceLockInfo();
			Node currentItem = nl.item(i);			
			NodeList childNodes = currentItem.getChildNodes();
			int childSize = childNodes.getLength();
			for (int j=0; j<childSize; j++) {
				Node currItem = childNodes.item(j);
				if (!isEmpty(currItem.getNodeName()) && "deviceId".equalsIgnoreCase(currItem.getNodeName())) {
					deviceLockInfo.setDeviceId(currItem.getTextContent());	
				}else if (!isEmpty(currItem.getNodeName()) && "lockServer".equalsIgnoreCase(currItem.getNodeName())) {
					deviceLockInfo.setLockServer(currItem.getTextContent());	
				}else if (!isEmpty(currItem.getNodeName()) && "lockStatus".equalsIgnoreCase(currItem.getNodeName())) {
					deviceLockInfo.setLockStatus(currItem.getTextContent());	
				}else if (!isEmpty(currItem.getNodeName()) && "lockStatusCode".equalsIgnoreCase(currItem.getNodeName())) {
					deviceLockInfo.setLockStatusCode(currItem.getTextContent());	
				}else if (!isEmpty(currItem.getNodeName()) && "lockStatusCodeDesc".equalsIgnoreCase(currItem.getNodeName())) {
					deviceLockInfo.setLockStatusCodeDesc(currItem.getTextContent());	
				}else if (!isEmpty(currItem.getNodeName()) && "lockDate".equalsIgnoreCase(currItem.getNodeName())) {
					deviceLockInfo.setLockDate(currItem.getTextContent());	
				}				
			}			
			deviceLockInfoList.add(deviceLockInfo);
		}		
	}
	
	private boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}
}
