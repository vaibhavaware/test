package com.vzw.dmd;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.vzw.dmd.dao.DeviceMacIdDAO;
import com.vzw.dmd.util.DMDConstants;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.ILteXmlCreator;
import com.vzw.dmd.util.XSSEncoder;
import com.vzw.dmd.util.XXEDisabler;
import com.vzw.dmd.util.XmlUtils;
import com.vzw.dmd.valueobject.DeviceMacAddressResponseVO;

/**
 * @author singhay
 *
 */
public class DMDDeviceMacIdService extends HttpServlet implements ILteXmlCreator {

	private static Logger L =
		Logger.getLogger(DMDLogs.getLogName(DMDDeviceMacIdService.class));
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
		String deviceId = null;
		try{
			String xmlReq = req.getParameter("xmlReq");					
			L.debug("The request parameter is"+req.getParameter("xmlReq"));
			
			if(xmlReq==null || xmlReq.trim().length()==0){
				xmlReq = req.getParameter("xmlreqdoc");
			}
			if(xmlReq != null) {
				xmlReq = xmlReq.trim();
			}
			L.debug("The request xml  is :"+xmlReq);
			
			//Fortify Fix - XML External Entity Injection
			DocumentBuilderFactory docBuilderFactory = new XXEDisabler().disableDBF(DocumentBuilderFactory.newInstance());
			Document document = docBuilderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(xmlReq)));
			
			statsLogBuf.append("CLIENT_ID=" + XmlUtils.getValue(document, "/dmd/requestHeader/clientId")).append(DMDConstants.DMD_PIPE);
			statsLogBuf.append("APP_TYPE=" + XmlUtils.getValue(document, "/dmd/requestHeader/appType")).append(DMDConstants.DMD_PIPE);
			statsLogBuf.append("TRANSACTION_ID=" + XmlUtils.getValue(document, "/dmd/requestHeader/requestId")).append(DMDConstants.DMD_PIPE);
			statsLogBuf.append("Service_Name=" + XmlUtils.getValue(document, "/dmd/requestHeader/serviceName")).append(DMDConstants.DMD_PIPE);
			statsLogBuf.append("Device_Id=" + XmlUtils.getValue(document, "/dmd/requestBody/deviceId")).append(DMDConstants.DMD_PIPE);
			
			deviceId = XmlUtils.getValue(document, "/dmd/requestBody/deviceId");
			
			if( isEmpty(deviceId)  ) {
				L.error("No Device Id sent in request XML Unable to process request.");
				statsLogBuf.append("STATUS=Failure");
				statsLogBuf.append(DMDConstants.DMD_PIPE);
				returnXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><DMD><STATUS>Normal</STATUS><RETURN_CODE>01</RETURN_CODE><RETURN_MESSAGE>Invalid data</RETURN_MESSAGE><DEVICE_ID></DEVICE_ID><MAC_ID></MAC_ID><SKU></SKU><MFG_CODE></MFG_CODE></DMD>";
			}
			else{
				DeviceMacAddressResponseVO	responseObj = DeviceMacIdDAO.fetchDeviceMacAddress(deviceId);
				returnXML = createDeviceMacAddressInfoXML(responseObj);
			}
						
		}catch (Exception e){
			L.error("Unable to process request.", e);
			statsLogBuf.append("STATUS=Failure");
			statsLogBuf.append(DMDConstants.DMD_PIPE);
			returnXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><DMD><STATUS>Normal</STATUS><RETURN_CODE>02</RETURN_CODE><RETURN_MESSAGE>DMD Application Error</RETURN_MESSAGE><DEVICE_ID>"+deviceId
			+"</DEVICE_ID><MAC_ID></MAC_ID><SKU></SKU><MFG_CODE></MFG_CODE></DMD>";
		}finally{
			Date exitTime = new Date();
			DMDLogs.getStatsLogger().info(statsLogBuf.toString());
			long prcTime = exitTime.getTime() - entryTime.getTime();
			DMDLogs.getEStatsLogger().info(
                    statsLogBuf.toString() + DMDConstants.DMD_PIPE                    
                            + DMDProps.ldf.format(entryTime) + DMDConstants.DMD_PIPE
                            + DMDProps.ldf.format(exitTime) + DMDConstants.DMD_PIPE + prcTime);
		}
		
		res.setContentType("text/xml");
		//Fortify Fix - Cross-site scripting
		out.println(new XSSEncoder().encodeXML(returnXML));
		out.flush();
		out.close();
	}
	
	private static String createDeviceMacAddressInfoXML(DeviceMacAddressResponseVO vo) {
		StringBuffer sbReturnXML=new StringBuffer(MACID_XML_RESPONSE_START);
		sbReturnXML.append(vo.getReturnCode())
			.append(RETURN_CODE_END)
			.append(RETURN_MESSAGE_START)
			.append(vo.getReturnMsg())
			.append(RETURN_MESSAGE_END)
			.append(MAC_DEVICE_ID_START)
			.append(vo.getDeviceId())
			.append(MAC_DEVICE_ID_END)
			.append(MAC_ID_START)
			.append(vo.getMacId())
			.append(MAC_ID_END)
			.append(SKU_START)
			.append(vo.getSku())
			.append(SKU_END)
			.append(MAC_MFG_CODE_START)
			.append(vo.getMfgCode())
			.append(MAC_MFG_CODE_END);
		sbReturnXML.append(MAC_DMD_END);
		return sbReturnXML.toString();
	 }
	
	 private static boolean isEmpty(String str) {
	        return str == null || str.length() == 0;
	    }
	
}
