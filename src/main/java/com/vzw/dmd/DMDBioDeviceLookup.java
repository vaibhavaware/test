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

import com.vzw.dmd.dao.BioDeviceLoadDAO;
import com.vzw.dmd.dao.DeviceLookupDAO;
import com.vzw.dmd.exception.DaoException;
import com.vzw.dmd.util.DMDConstants;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.ILteXmlCreator;
import com.vzw.dmd.util.XSSEncoder;
import com.vzw.dmd.util.XXEDisabler;
import com.vzw.dmd.util.XmlUtils;
import com.vzw.dmd.valueobject.BioDeviceBean;



@SuppressWarnings("serial")
public class DMDBioDeviceLookup  extends HttpServlet implements ILteXmlCreator{

	
	int insertRec;
	private static Logger L = Logger.getLogger(DMDLogs.getLogName(DMDBioDeviceLookup.class));
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
		/*public void init() throws ServletException {
			super.init();
		}*/

		/**
		* @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
		*/
		public void defaultAction(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
			
			StringBuffer statsLogBuf = new StringBuffer("DMDBioDeviceLookup").append(DMDConstants.DMD_PIPE);
			Date entryTime = new Date();
			
			ServletOutputStream out = res.getOutputStream();
			String returnXML = null;			
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
				statsLogBuf.append("CLIENT_ID=" + XmlUtils.getValue(document, "/dmd/requestHeader/clientId")).append(DMDConstants.DMD_PIPE);
				statsLogBuf.append("APP_TYPE=" + XmlUtils.getValue(document, "/dmd/requestHeader/appType")).append(DMDConstants.DMD_PIPE);
				statsLogBuf.append("TRANSACTION_ID=" + XmlUtils.getValue(document, "/dmd/requestHeader/requestId")).append(DMDConstants.DMD_PIPE);
				statsLogBuf.append("Service_Name=" + XmlUtils.getValue(document, "/dmd/serviceName")).append(DMDConstants.DMD_PIPE);
				
				String deviceSkuArr[] = getDeviceSkuList(document);
				if(deviceSkuArr == null || deviceSkuArr.length == 0) {
					throw new Exception("Input device sku list is empty.");
				}			
				statsLogBuf.append("DEVICE_SKU_LIST_COUNT=" + deviceSkuArr.length).append(DMDConstants.DMD_PIPE);
				
				String deviceInfoXml = BioDeviceLoadDAO.fetchBioDeviceRec(deviceSkuArr);
				statsLogBuf.append("STATUS=Success").append(DMDConstants.DMD_PIPE);
				returnXML = createConsolidatedXml("00", "SUCCESS", deviceInfoXml);
				
			}catch (Exception e){
				L.error("Unable to process request.", e);
				
				statsLogBuf.append("STATUS=Failure");
				statsLogBuf.append(DMDConstants.DMD_PIPE);
				
				returnXML = createConsolidatedXml("-1", "Invalid input or device info not found.", null);
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
			out.println(new XSSEncoder().encodeHTML(returnXML));
			out.flush();
			out.close();
		}
		
		private String createConsolidatedXml(String statusCd, String msg, String body) {
			StringBuffer sbReturnXML=new StringBuffer(XML_RESPONSE_START);
			//add header. status and message
			sbReturnXML.append(statusCd)
						.append(STATUS_CODE_END)
						.append(MESSAGE_START)
						.append(msg)
						.append(MESSAGE_END)
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
}

