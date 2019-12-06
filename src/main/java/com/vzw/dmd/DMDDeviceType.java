

package com.vzw.dmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Enumeration;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.vzw.dmd.dao.DeviceLookupDAO;
import com.vzw.dmd.dao.OracleDAOFactory;
import com.vzw.dmd.dao.OracleEsnLookupDAO;
import com.vzw.dmd.dao.OraclePibLockDAO;
import com.vzw.dmd.exception.DaoException;
import com.vzw.dmd.util.DMDConstants;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.DMDUtils;
import com.vzw.dmd.util.DmdXmlCreator;
import com.vzw.dmd.util.XXEDisabler;
import com.vzw.dmd.util.XmlUtils;
import com.vzw.dmd.valueobject.DMDHubMacIdLookupRequestVO;
import com.vzw.dmd.valueobject.DeviceInfo;
import com.vzw.dmd.valueobject.EsnLookupRequestVO;
import com.vzw.dmd.valueobject.LaunchPackageVO;
import com.vzw.dmd.valueobject.RssPrepayVO;


public class DMDDeviceType extends HttpServlet {
	private static Logger L = Logger.getLogger(DMDLogs.getLogName(DMDDeviceType.class));

	public void doGet(HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException {
		try {
			L.debug("r:" + req.getParameterNames().toString());
			String requestMode="";
			Enumeration enu_p = req.getParameterNames();
			while(enu_p.hasMoreElements()){
					
				String name = (String)enu_p.nextElement();
				String value = req.getParameter(name);
				L.debug("name:" + name + " value: " + value);
			}
			
			postAction(req, res);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public void doPost1(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		try {
			
				defaultAction(req, res);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		try {
			L.debug("r:" + req.getParameterNames().toString());
			String requestMode="";
			Enumeration enu_p = req.getParameterNames();
			while(enu_p.hasMoreElements()){
					
				String name = (String)enu_p.nextElement();
				String value = req.getParameter(name);
				L.debug("name: " + name + " value: " + value);
			}
			
			postAction(req, res);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void defaultAction(HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException, DaoException, NamingException, CreateException {
	}
	
	public void postAction(
			HttpServletRequest request,
			HttpServletResponse res)
			throws ServletException, IOException {
			String methodName = "postAction : ";
			String xmlReqString = "";


			L.debug("c:" + request.hashCode());
			StringBuffer statsLogBuf = new StringBuffer();
			
			Date entryTime = new Date();
			
			ServletInputStream xmlRequest = request.getInputStream();
			L.debug("xmlRequest  postAction : "+xmlRequest.toString());
			PrintWriter out = res.getWriter();

			//InputSource inputSourceForXmlRequest = null;

			String reqClientName = "";
			String reqAplName = "";
			String reqEsnMeid = "";
			String deviceID = "";
			
			String resStatus = "ERROR";
			String resMeid = "Not Found";
			String resEsn = "Not Found";
			String returnCode = "99";
			String returnMessage = "ERROR WHILE RETRIEVING DATA";
			
			String prodName = "";
			String mfgCode = "" ;
			String effDate = "";
			String deviceType= "";
			String meid_hex = "";
			RssPrepayVO prepayVO = null;
			LaunchPackageVO launchVO = null;
			// Added for Jan 2010 Release
			String mfgName = "" ;
			//

			try {
				
				//xmlReqString = "<DMD><APP_TYPE>MTAS</APP_TYPE><MACID>0013E09D560F</MACID><pESN></pESN></DMD>";
				xmlReqString = getStringFromInputStream(xmlRequest);
				L.debug("xmlReqString INSIDE postAction : "+xmlReqString);
			
				//L.debug("inputSourceForXmlRequest : "+inputSourceForXmlRequest);
				L.debug("String Reader  : "+new StringReader(xmlReqString));

				//inputSourceForXmlRequest = new InputSource(new StringReader(xmlReqString));
				
				//
				/*
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				L.debug("TEST1 ");
				Document document =null;
				L.debug("TEST21"+xmlReqString);
				document = db.parse(xmlReqString);
				L.debug("TEST2");
				String req_type = XmlUtils.getValue(document, "DMD/APP_TYPE");
				L.debug("TEST3");
				L.debug("XML UTIL VALUE : "+req_type);
				//
				 * */
				 
				Enumeration enu_p = request.getParameterNames();
				while(enu_p.hasMoreElements()){
						
					String name = (String)enu_p.nextElement();
					String value = request.getParameter(name);
					L.debug("name: " + name + " value: " + value);
					xmlReqString = value;
				}
				
					if (xmlReqString != null) {
						
						L.debug("xmlReqString is not null : " + xmlReqString);
								
						int clientNameStartIndex = xmlReqString.indexOf("<CLIENT_NAME>");
						int clientNameEndIndex = xmlReqString.indexOf("</CLIENT_NAME>");
						L.debug("clientName StartIndex : "+clientNameStartIndex);
						L.debug("clientName EndIndex : "+clientNameEndIndex);
						if (clientNameStartIndex != -1 && clientNameEndIndex != -1) {
							reqClientName =
								xmlReqString.substring(
										clientNameStartIndex + 13,
										clientNameEndIndex);
							L.debug("clientName is not null : "+reqClientName);
						}
						//statsLogBuf.append("DEVICE_TYPE|");
						 
						int aplNameStartIndex = xmlReqString.indexOf("<APL_NAME>");
						int aplNameEndIndex = xmlReqString.indexOf("</APL_NAME>");

						if (aplNameStartIndex != -1 && aplNameEndIndex != -1) {
							reqAplName =
								xmlReqString.substring(
										aplNameStartIndex + 10,
										aplNameEndIndex);
							L.debug("Got Request for Application Name : "+reqAplName);
						}

						int esnMeidStartIndex = xmlReqString.indexOf("<ESN_MEID>");
						int esnMeidEndIndex = xmlReqString.indexOf("</ESN_MEID>");

						// DEVICE ID -- IMEI , ICCID , ..
						int deviceStartIndex = xmlReqString.indexOf("<DEVICE_ID>");
						int devicedEndIndex = xmlReqString.indexOf("</DEVICE_ID>");
						//

						if (esnMeidStartIndex != -1 && esnMeidEndIndex != -1) {
							reqEsnMeid =
								xmlReqString.substring(
										esnMeidStartIndex + 10,
										esnMeidEndIndex);
							reqEsnMeid = reqEsnMeid.trim().toUpperCase();
							L.debug("Got Request for ESN/MEID : "+reqEsnMeid);
						}
						
						if (deviceStartIndex != -1 && devicedEndIndex != -1) {
							String subPart=xmlReqString.substring(deviceStartIndex,devicedEndIndex);
					        int nextIndex = subPart.lastIndexOf('>');
					        L.debug("deviceStartIndex  " +deviceStartIndex);
					        L.debug("deviceStartIndex  " +devicedEndIndex);
					        deviceID = subPart.substring(nextIndex+1);
							deviceID = deviceID.trim().toUpperCase();
							L.debug("Got Request for Device ID : "+deviceID);
						}
						statsLogBuf.append(reqClientName + "|");
						statsLogBuf.append(reqAplName + "|");		
						statsLogBuf.append(DMDUtils.getClientIP(request) + "|");
						statsLogBuf.append("LOOKUP|");

					}

					if ((reqEsnMeid == null) &&  (reqEsnMeid == null)){
						returnCode = "01";
						returnMessage = "INVALID ESN / MEID";
						resStatus = "ERROR";
						resMeid = "Invalid ESN/MEID";
						statsLogBuf.append(reqEsnMeid + "|");
						statsLogBuf.append("FALSE|");
						createResponseXML(out,reqEsnMeid,resStatus,returnCode,returnMessage,prodName , mfgCode, mfgName, deviceType,prepayVO,launchVO);

					} 
					else {
						if (reqEsnMeid.trim().length() == 11){
							String esn = reqEsnMeid.toUpperCase();
							L.debug("Got Request for ESN : "+esn);
							
					        try
					        {
					            OracleDAOFactory oracleDAOFactory = OracleDAOFactory.getInstance();
					            OracleEsnLookupDAO daoLookup = (OracleEsnLookupDAO) oracleDAOFactory
					                    .createEsnLookupDAO();
								EsnLookupRequestVO lookupRequestVO = new EsnLookupRequestVO();
								lookupRequestVO.setIdType("ESN");
								lookupRequestVO.setId(esn);

					            //First check if ESN/MEID exist in our DataBase.
								// EsnLookupRequestVO lookupResultVO = 
								EsnLookupRequestVO lookupResultVO = daoLookup.locateEsnMeid(lookupRequestVO);
								L.debug("Got Response for ESN Search Result Status: "+lookupResultVO.isSearchResultStatus());
								lookupResultVO.isSearchResultStatus();
								prodName = chkNull(lookupResultVO.getProdName());
								mfgCode = chkNull(lookupResultVO.getMfgCode());
								mfgName = chkNull(lookupResultVO.getMfgName());
								effDate = chkNull(lookupResultVO.getEffDate());
								L.debug("Got Response for ESN prodName: "+prodName);
								L.debug("Got Response for ESN mfgCode: "+mfgCode);
								L.debug("Got Response for ESN mfgName: "+mfgName);
								L.debug("Got Response for ESN effDate: "+effDate);
								deviceType=chkNull(daoLookup.getDeviceMask(prodName, mfgCode, effDate));
								OraclePibLockDAO daoPibLock = (OraclePibLockDAO) oracleDAOFactory.getPibLockDAO();
								prepayVO = daoPibLock.getRssPrepayData(esn);
								// 
								// Get the VisionPROD ID and DeviceCategory FOR RIM PREPAY
								DeviceLookupDAO lookup = new DeviceLookupDAO();
								launchVO = lookup.getGuiLaunchPackage_tst(lookupResultVO.getProdName());
								// 
								L.debug("Got Response for ESN deviceType: "+deviceType);
								returnCode = "00";
								returnMessage = "SUCCESS";
								resStatus = "Normal";
								if(prodName.trim().length()==0 && mfgCode.trim().length()==0){
									returnCode = "02";
									returnMessage = "ESN NOT FOUND";
								}
								statsLogBuf.append("ESN_FEATURES|");
								statsLogBuf.append(esn + "|");
								statsLogBuf.append("TRUE|");

					        }
					        catch (DaoException ex)
					        {
								returnCode = "02";
								returnMessage = "ESN NOT FOUND";
								resStatus = "ERROR";
								resEsn = "Not Found";
					            L.error(": getDeviceType(): failed during locating esn: " + esn, ex);
								statsLogBuf.append(esn + "|");
								statsLogBuf.append("FALSE|");

					        }
					        createResponseXML(out,reqEsnMeid,resStatus,returnCode,returnMessage,prodName , mfgCode, mfgName,deviceType,prepayVO,launchVO);
					        
							/*
							DMDHubMacIdLookupRequestVO vo = new DMDHubMacIdLookupRequestVO();				
							vo.setMacid(esn);

							OracleDAOFactory oracleDAOFactory = OracleDAOFactory.getInstance();
							OracleEsnLookupDAO daoLookup = (OracleEsnLookupDAO)oracleDAOFactory.createEsnLookupDAO();
							resEsn = daoLookup.getDMDHubPesn(vo);				

							
							if (resEsn == null){
								resStatus = "ERROR";
								resEsn = "Not Found";
							}else{
								resStatus = "Normal";
							}
							L.debug("Got Response for pESN/Macid : "+resEsn);

							statsLogBuf.append(esn + "|");
							statsLogBuf.append("TRUE|");

							createResponseXML(out,resStatus , esn, resEsn);
							*/
						}else if(reqEsnMeid.trim().length() == 14){
							meid_hex = reqEsnMeid.toUpperCase();
							DMDHubMacIdLookupRequestVO vo = new DMDHubMacIdLookupRequestVO();				
							L.debug("Got Request for MEID : "+meid_hex);
							
							String meid = DMDUtils.convertMEIDFromHexToDecimal(meid_hex);
					        try
					        {
					            OracleDAOFactory oracleDAOFactory = OracleDAOFactory.getInstance();
					            OracleEsnLookupDAO daoLookup = (OracleEsnLookupDAO) oracleDAOFactory
					                    .createEsnLookupDAO();
								EsnLookupRequestVO lookupRequestVO = new EsnLookupRequestVO();
								lookupRequestVO.setIdType("MEID");
								lookupRequestVO.setId(meid);

					            //First check if ESN/MEID exist in our DataBase.
								// EsnLookupRequestVO lookupResultVO = 
								EsnLookupRequestVO lookupResultVO = daoLookup.locateEsnMeid(lookupRequestVO);
								L.debug("Got Response for ESN Search Result Status: "+lookupResultVO.isSearchResultStatus());
								lookupResultVO.isSearchResultStatus();
								prodName = chkNull(lookupResultVO.getProdName());
								mfgCode = chkNull(lookupResultVO.getMfgCode());
								mfgName = chkNull(lookupResultVO.getMfgName());
								effDate = chkNull(lookupResultVO.getEffDate());
								L.debug("Got Response for ESN prodName: "+prodName);
								L.debug("Got Response for ESN mfgCode: "+mfgCode);
								L.debug("Got Response for ESN mfgName: "+mfgName);
								L.debug("Got Response for ESN effDate: "+effDate);
								deviceType=chkNull(daoLookup.getDeviceMask(prodName, mfgCode, effDate));
								OraclePibLockDAO daoPibLock = (OraclePibLockDAO) oracleDAOFactory.getPibLockDAO();
								prepayVO = daoPibLock.getRssPrepayData(meid_hex);
								L.debug("Got Response for ESN deviceType: "+deviceType);
								//// Get the VisionPROD ID and DeviceCategory FOR RIM PREPAY
								DeviceLookupDAO lookup = new DeviceLookupDAO();
								launchVO = lookup.getGuiLaunchPackage_tst(lookupResultVO.getProdName());
								// 
								returnCode = "00";
								returnMessage = "SUCCESS";
								resStatus = "Normal";
								if(prodName.trim().length()==0 && mfgCode.trim().length()==0){
									returnCode = "02";
									returnMessage = "MEID NOT FOUND";
								}
								statsLogBuf.append("MEID_FEATURES|");
								statsLogBuf.append(meid_hex + "|");
								statsLogBuf.append("TRUE|");

					        }
					        catch (DaoException ex)
					        {
								returnCode = "02";
								returnMessage = "MEID NOT FOUND";
								resStatus = "ERROR";
								resEsn = "Not Found";
								statsLogBuf.append(meid_hex + "|");
								statsLogBuf.append("FALSE|");

					            L.error(": getDeviceType(): failed during locating meid: " + meid, ex);
					        }
					        createResponseXML(out,meid_hex,resStatus,returnCode,returnMessage,prodName , mfgCode, mfgName,deviceType,prepayVO,launchVO);
							/*
							OracleDAOFactory oracleDAOFactory = OracleDAOFactory.getInstance();
							OracleEsnLookupDAO daoLookup = (OracleEsnLookupDAO)oracleDAOFactory.createEsnLookupDAO();
							resMeid = daoLookup.getDMDHubMacid(vo);
							if (resMeid == null){
								L.debug("ERROR for pESN/Macid : ");
								resStatus = "ERROR";
								resMeid = "Not Found";
							}else{
								L.debug("Normal for pESN/Macid : ");
								resStatus = "Normal";
							}
							L.debug("Got Response for pESN/Macid : "+resMeid);
							
							statsLogBuf.append(meid + "|");
							statsLogBuf.append("TRUE|");

							createResponseXML(out,resStatus , resMeid, meid);
							*/
						}
						else if(! ("").equals(deviceID) ){
							DeviceInfo deviceInfo = new DeviceInfo();
							if(deviceID != null && !deviceID.trim().equals(DMDConstants.EMPTY_STRING)){
								deviceInfo.setDeviceIdType(DMDUtils.getDeviceIDType(deviceID));
								if(DMDConstants.DEVICE_TYPE_ESN.equals(deviceInfo.getDeviceIdType())){
									deviceInfo.setDeviceId(DMDUtils.addLeadingZeros(deviceID));
						}else{
									deviceInfo.setDeviceId(deviceID);
								}
							}
							int deviceNotFound =DeviceLookupDAO.locateDevice(deviceInfo);
							L.debug("Device ID "+deviceID + "Device Found " + deviceNotFound );
//							 //IMEI
					        if(deviceNotFound  == DMDConstants.DMD_DEVICE_NOT_FOUND && DMDConstants.DEVICE_TYPE_IMEI.equals(deviceInfo.getDeviceIdType()) && deviceInfo.getDeviceId().length()==14){
								deviceInfo.setDeviceIdType(DMDConstants.DEVICE_TYPE_MEID);
								deviceNotFound =DeviceLookupDAO.locateDevice(deviceInfo);
							}
							if(deviceNotFound  == DMDConstants.DMD_DEVICE_NOT_FOUND){
								returnCode = "01";
								returnMessage = "INVALID Device";
								resStatus = "ERROR";
								resMeid = "Invalid Device";
								L.debug("Invalid Device: "+deviceID);
								statsLogBuf.append(deviceID + "|");
								statsLogBuf.append("FALSE|Invalid Device");
								createResponseXML(out,deviceID,resStatus,returnCode,returnMessage,prodName , mfgCode, mfgName,deviceType,prepayVO,launchVO);
							}
							else{
								prodName = chkNull(deviceInfo.getProdName());
								mfgCode = chkNull(deviceInfo.getMfgCode());
								mfgName = chkNull(deviceInfo.getMfgName());
								effDate = chkNull(deviceInfo.getEffectiveDate());
								DeviceLookupDAO lookup = new DeviceLookupDAO();
								launchVO = lookup.getGuiLaunchPackage_tst(deviceInfo.getProdName());
								 OracleDAOFactory oracleDAOFactory = OracleDAOFactory.getInstance();
						            OracleEsnLookupDAO daoLookup = (OracleEsnLookupDAO) oracleDAOFactory
						                    .createEsnLookupDAO();
								if(deviceInfo.getDeviceIdType().equals(DMDConstants.DEVICE_TYPE_ESN) || deviceInfo.getDeviceIdType().equals(DMDConstants.DEVICE_TYPE_MEID))
								{
									String meid = "";
									deviceType=chkNull(daoLookup.getDeviceMask(prodName, mfgCode, effDate));
									OraclePibLockDAO daoPibLock = (OraclePibLockDAO) oracleDAOFactory.getPibLockDAO();
									if(deviceInfo.getDeviceIdType().equals(DMDConstants.DEVICE_TYPE_MEID))
									{
										 meid= DMDUtils.convertMEIDFromHexToDecimal(meid_hex);
										 prepayVO = daoPibLock.getRssPrepayData(deviceID);
										 L.debug("Got Response for ESN deviceType: "+deviceType);
									}
									else{
										prepayVO = daoPibLock.getRssPrepayData(deviceID);
										L.debug("Got Response for ESN deviceType: "+deviceType);
									}
									
								}
								returnCode = "00";
								returnMessage = "SUCCESS";
								resStatus = "Normal";
								createResponseXML(out,deviceID,resStatus,returnCode,returnMessage,prodName , mfgCode, mfgName,deviceType,prepayVO,launchVO);
							}
						}
						else{
							returnCode = "01";
							returnMessage = "INVALID ESN / MEID";
							resStatus = "ERROR";
							resMeid = "Invalid ESN/MEID";
							L.debug("Invalid ESN/MEID : "+reqEsnMeid);
							statsLogBuf.append(reqEsnMeid + "|");
							statsLogBuf.append("FALSE|Invalid ESN/MEID");
							createResponseXML(out,reqEsnMeid,resStatus,returnCode,returnMessage,prodName , mfgCode, mfgName,deviceType,prepayVO,launchVO);


						}

						
					}

					Date exitTime = new Date();
				    String transId = request.getParameter("transaction_id");
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
				out = null;


			} catch (Exception e) {
				L.error(e.getMessage());
			} finally {
				if (xmlRequest != null)
					xmlRequest.close();
				if (out != null) {
					out.flush();
					out.close();
				}
			}
		}
	
	public String getStringFromInputStream(ServletInputStream inputStream){
		StringBuffer sb = new StringBuffer(); 
		String 	inputLine = null;	
		BufferedReader in = null;
		 
		try{
			in =
				new BufferedReader(
					new InputStreamReader(inputStream));
			L.debug("outside while in stringfrom input: " );
			while ((inputLine = in.readLine()) != null) {
				L.debug("input line: " + inputLine);
				sb.append(inputLine);
			}
			L.debug("sb string : "+sb.toString());
			//in.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			if(in!=null) {
				try{
				in.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}
	
	public String chkNull(String inSting){
		String outString = "";
		if(inSting == null){
			outString = "";
		}else{
			outString = inSting.trim();
		}
		return outString;
	}
	
	private void createResponseXML(
			/*
			<DMD> 
			<STATUS>Normal</STATUS> 
			<ESN_MEID>05405349408<ESN_MEID> 
			<PROD_NAME>LG-VX5200</PROD_NAME> 
			<MFG_CODE>LGI</MFG_CODE> 
			<DEVICE_TYPE>ODI</DEVICE_TYPE>
			</DMD>
			*/ 

			PrintWriter out,
			String resEsnMeid,
			String resStatus,
			String returnCode,
			String returnMessage,
			String resProdName,
			String resMfgCode,
			String resMfgName,
			String resDeviceType,
			RssPrepayVO prepayVO,
			LaunchPackageVO vo) {
			try {
				L.debug("Got Response for ESN/MEID resStatus : "+resStatus);
				L.debug("Got Response for ESN/MEID resDeviceType : "+returnCode);
				L.debug("Got Response for ESN/MEID resDeviceType : "+returnMessage);
				L.debug("Got Response for ESN/MEID resProdName : "+resProdName);
				L.debug("Got Response for ESN/MEID resMfgCode : "+resMfgCode);
				L.debug("Got Response for ESN/MEID resMfgName : "+resMfgName);
				L.debug("Got Response for ESN/MEID resDeviceType : "+resDeviceType);

				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document xmlDoc = db.newDocument();
				Element root = xmlDoc.createElement("DMD");
				xmlDoc.appendChild(root);

				Element child1 = xmlDoc.createElement("STATUS");
				child1.appendChild(xmlDoc.createTextNode(resStatus));
				root.appendChild(child1);

				Element child2 = xmlDoc.createElement("RETURN_CODE");
				child2.appendChild(xmlDoc.createTextNode(returnCode));
				root.appendChild(child2);

				Element child3 = xmlDoc.createElement("RETURN_MESSAGE");
				child3.appendChild(xmlDoc.createTextNode(returnMessage));
				root.appendChild(child3);
				
				Element child4 = xmlDoc.createElement("ESN_MEID");
				child4.appendChild(xmlDoc.createTextNode(resEsnMeid));
				root.appendChild(child4);

				Element child5 = xmlDoc.createElement("PROD_NAME");
				child5.appendChild(xmlDoc.createTextNode(resProdName));
				root.appendChild(child5);

				Element child6 = xmlDoc.createElement("MFG_CODE");
				child6.appendChild(xmlDoc.createTextNode(resMfgCode));
				root.appendChild(child6);
				
				Element child7 = xmlDoc.createElement("MFG_NAME");
				child7.appendChild(xmlDoc.createTextNode(resMfgName));
				root.appendChild(child7);

				Element child8 = xmlDoc.createElement("DEVICE_TYPE");
				child8.appendChild(xmlDoc.createTextNode(resDeviceType));
				root.appendChild(child8);
				
				Element child9 = xmlDoc.createElement("PREPAY_ELIGIBILITY");
				child9.appendChild(xmlDoc.createTextNode(prepayVO!=null?prepayVO.getPrepayEligibility():""));
				root.appendChild(child9);
				
				Element child10 = xmlDoc.createElement("ELIGIBILITY_DATE");
				child10.appendChild(xmlDoc.createTextNode(prepayVO!=null?prepayVO.getEligibilityDateMMDDYYYY():""));
				root.appendChild(child10);
				
				Element child11 = xmlDoc.createElement("PROD_ID");
				child11.appendChild(xmlDoc.createTextNode(vo!=null?vo.getVisionProdId():""));
				root.appendChild(child11);
				
				Element child12 = xmlDoc.createElement("DEVICE_CATEGORY");
				child12.appendChild(xmlDoc.createTextNode(vo!=null?vo.getDeviceCategory():""));
				root.appendChild(child12);
				
				//Fortify Fix - XML External Entity Injection
				TransformerFactory transformerFactory = new XXEDisabler().disableTransformerFactory(TransformerFactory.newInstance());
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(xmlDoc);
				StreamResult result = new StreamResult(out);
//				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.transform(source,result);
				
				
			} catch (Exception e) {
				L.error("createResponseXML: Exception "+ e.getMessage());
			}
		}
	

}
