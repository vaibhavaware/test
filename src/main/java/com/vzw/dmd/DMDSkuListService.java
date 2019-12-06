package com.vzw.dmd;

import java.io.IOException;
import java.io.StringReader;
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
import org.xml.sax.InputSource;

import com.vzw.dmd.dao.BYODDAO;
import com.vzw.dmd.dao.SKUDAO;
import com.vzw.dmd.util.DMDConstants;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.ILteXmlCreator;
import com.vzw.dmd.util.XXEDisabler;
import com.vzw.dmd.util.XmlUtils;
import com.vzw.dmd.valueobject.BYODRequestVO;
import com.vzw.dmd.valueobject.BYODResponseVO;
import com.vzw.dmd.valueobject.SkuListRequestVO;
import com.vzw.dmd.valueobject.SkuListResponseVO;
import com.vzw.dmd.util.XSSEncoder;

/**
 * @author v736771
 *
 */
public class DMDSkuListService extends HttpServlet implements ILteXmlCreator {
	private static Logger L =
			Logger.getLogger(DMDLogs.getLogName(DMDByodService.class));
	XSSEncoder encoder = new XSSEncoder();
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
		SkuListRequestVO request = new SkuListRequestVO();
		SKUDAO skuDAO =  new SKUDAO();
		try{
			String xmlReq = req.getParameter("xmlReq");
			L.debug("The request parameter is"+req.getParameter("xmlReq"));

			if(xmlReq==null || xmlReq.trim().length()==0){
				xmlReq = req.getParameter("xmlreqdoc");
			}
			if(xmlReq != null) {
				xmlReq = xmlReq.trim();
			}
			L.info("DMDSkuListService request xml is :"+xmlReq);

			//Fortify Fix - XML External Entity Injection
			DocumentBuilderFactory docBuilderFactory = new XXEDisabler().disableDBF(DocumentBuilderFactory.newInstance());
			Document document = docBuilderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(xmlReq)));
			
			request.setAppType(XmlUtils.getValue(document, "/dmd_request/requestHeader/appType"));
			request.setRequestId(XmlUtils.getValue(document, "/dmd_request/requestHeader/requestId"));
			request.setDeviceMfgCode(XmlUtils.getValue(document, "/dmd_request/requestBody/deviceMfgCode"));
			request.setDeviceCategory(XmlUtils.getValue(document, "/dmd_request/requestBody/deviceCategory"));		
			
			statsLogBuf.append("APP_TYPE=" + XmlUtils.getValue(document, "/dmd_request/requestHeader/appType")).append(DMDConstants.DMD_PIPE);
			statsLogBuf.append("TRANSACTION_ID=" + XmlUtils.getValue(document, "/dmd_request/requestHeader/requestId")).append(DMDConstants.DMD_PIPE);
			statsLogBuf.append("Service_Name=" + "DMDSkuList").append(DMDConstants.DMD_PIPE);

			if( isEmpty(request.getDeviceMfgCode()) ) {
				throw new Exception("Invalid SkuList request");
			}
			else{
				SkuListResponseVO response = skuDAO.fetchSkuList(request);
				if (response != null) {
					L.debug("DB response Not Null :"+response);
					returnXML = getResponseXML(response);				
										
				}
				else {
					L.debug("DB response is Null :"+response);					
					returnXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><dmd_response><request><appType>"+request.getAppType()+"</appType><requestId>"+request.getRequestId()+"</requestId></request><status><status_code>1</status_code><status_message>Application Error</status_message></status></dmd_response>";
				}
			}						
		}catch (Exception e){
			L.error("Unable to process request.", e);
			statsLogBuf.append("STATUS=Failure");
			statsLogBuf.append(DMDConstants.DMD_PIPE);			
			returnXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><dmd_response><request><appType>"+request.getAppType()+"</appType><requestId>"+request.getRequestId()+"</requestId></request><status><status_code>2</status_code><status_message>Invalid XML</status_message></status></dmd_response>";
		}finally{
			Date exitTime = new Date();
			DMDLogs.getStatsLogger().info(statsLogBuf.toString());
			long prcTime = exitTime.getTime() - entryTime.getTime();
			DMDLogs.getEStatsLogger().info(
					statsLogBuf.toString() + DMDConstants.DMD_PIPE                    
					+ DMDProps.ldf.format(entryTime) + DMDConstants.DMD_PIPE
					+ DMDProps.ldf.format(exitTime) + DMDConstants.DMD_PIPE + prcTime);
			L.info("DMDByodService return xml is :"+returnXML);
		}		
		res.setContentType("text/xml");
		out.println(encoder.encodeXML(returnXML));
		out.flush();
		out.close();
	}
	
	private boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}
	
	private String getResponseXML(SkuListResponseVO response){
		
		String returnXML = "";
		List<String> skuList = response.getDeviceSkuList();
		returnXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><deviceSkuList>";
		for(String deviceSKu:skuList){
			
			returnXML += "<deviceSku>"+deviceSKu+"</deviceSku>";
		}		
		returnXML += "</deviceSkuList>";
		return returnXML;
	}
	
	
}
