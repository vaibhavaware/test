package com.vzw.dmd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.BufferedReader;
import java.net.URLDecoder;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.SAXParser;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.vzw.dmd.dao.BulkDeviceSimLookupDAO;
import com.vzw.dmd.dao.DeviceSimValidationDAO;
import com.vzw.dmd.exception.DaoException;
import com.vzw.dmd.exception.SchemaValidationException;
import com.vzw.dmd.util.DMDConstants;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.DMDUtils;
import com.vzw.dmd.util.SchemaValidator;
import com.vzw.dmd.util.XXEDisabler;
import com.vzw.dmd.util.XmlUtils;

public class DMDBulkDeviceSimLookup extends HttpServlet {
	private static Logger L = Logger.getLogger(DMDLogs.getLogName(DMDBulkDeviceSimLookup.class));
	
	/**
	* @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	*/
	public void doGet(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException {
		//L.debug("doGet()");
		
		String xmlReqt = req.getParameter("xmlReq");
		if (xmlReqt == null || xmlReqt.length() == 0) {
			xmlReqt = req.getParameter("xmlreqdoc");
		}

		defaultAction(req, res, xmlReqt);
	}

	/**
	* @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	*/
	public void doPost(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException {
		//L.debug("doPost()");

		String xmlReqt = req.getParameter("xmlReq");
		if (xmlReqt == null || xmlReqt.length() == 0) {
			xmlReqt = req.getParameter("xmlreqdoc");
		}

		defaultAction(req, res, xmlReqt); 
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void defaultAction(HttpServletRequest req, HttpServletResponse res, String xmlReqt)
		throws ServletException, IOException {
		L.debug("DMDBulkDeviceSimLookup : xmlReqt: "+xmlReqt);
		
		StringBuffer statsLogBuf = new StringBuffer(DMDConstants.XML_NEW);
		statsLogBuf.append(DMDConstants.DMD_PIPE);
		Date entryTime = new Date();
		
		//res.setContentType("text/xml");
		ServletOutputStream out = res.getOutputStream();

		String appType = "", requestId = "";
    	ArrayList esnList = new ArrayList();
    	ArrayList meidList = new ArrayList();
    	ArrayList imeiList = new ArrayList();
    	ArrayList iccidList = new ArrayList();
    	ArrayList imeiIccidList = new ArrayList();
    	ArrayList imeiSkuIccidList = new ArrayList();
    	ArrayList invalidDeviceIdList = new ArrayList();
    	ArrayList invalidSimIdList = new ArrayList();
    	ArrayList invalidDeviceSimIdList = new ArrayList();
    	//Fortify Fix - XML External Entity Injection
		DocumentBuilderFactory df = null;
		Document document = null;
		try {
			df = new XXEDisabler().disableDBF(DocumentBuilderFactory.newInstance());
			DocumentBuilder db = df.newDocumentBuilder();
			document = db.parse(new InputSource(new StringReader(xmlReqt)));

			appType = XmlUtils.getValue(document, DMDConstants.UNIVERSAL_APP_TYPE_QUERY);
			requestId = XmlUtils.getValue(document, DMDConstants.UNIVERSAL_REQUEST_ID_QUERY);
			L.debug("appType=" + appType + ", requestId=" + requestId);

			NodeIterator nit = XPathAPI.selectNodeIterator(document, "dmd/requestBody/devicePair");
			Element el;
			NodeList nodes;
			int nbrNode = 0, validDeviceCnt = 0, validSimCnt = 0;
			while ((el=(Element)nit.nextNode()) != null) {
				nbrNode++;
				String deviceId = null, simId = null, deviceType = null, seqNum = null, deviceSku = null;
				if ((nodes=el.getElementsByTagName("seqNum")) != null) {
					if (nodes.getLength() > 0) {
						if (nodes.item(0).getFirstChild() != null) { 
							seqNum = nodes.item(0).getFirstChild().getNodeValue().trim();
						}
					}
				}

				if ((nodes=el.getElementsByTagName("deviceId")) != null) {
					if (nodes.getLength() > 0) {
						if (nodes.item(0).getFirstChild() != null) {
							deviceId = nodes.item(0).getFirstChild().getNodeValue();
							if (deviceId != null) {
								deviceId = deviceId.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
								if(deviceId.equalsIgnoreCase("")) {
									deviceId = null;
								}
							}
						}
					}
				}
				
				if (deviceId == null && (nodes=el.getElementsByTagName("deviceSku")) != null) {
					if (nodes.getLength() > 0) {
						if (nodes.item(0).getFirstChild() != null) {
							deviceSku = nodes.item(0).getFirstChild().getNodeValue();
							if (deviceSku != null) {
								if(deviceSku.trim().equals("")) {
									deviceSku = null;
								}
							}
						}
					}					
				}
				
				if ((nodes=el.getElementsByTagName("simId")) != null) {
					if (nodes.getLength() > 0) {
						if (nodes.item(0).getFirstChild() != null) { 
							simId = nodes.item(0).getFirstChild().getNodeValue();
							if (simId != null) {
								simId = simId.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
								if(simId.equalsIgnoreCase("")) {
									simId = null;
								}
							}
						}
					}
				}
				
				if(deviceId != null) {
					validDeviceCnt++;
					
					deviceType = DMDUtils.getDeviceIDType(deviceId);
					if(simId == null && DMDConstants.DEVICE_TYPE_ESN.equalsIgnoreCase(deviceType)) {
						esnList.add(seqNum + ":" + deviceId);
					} else if(simId == null && DMDConstants.DEVICE_TYPE_MEID.equalsIgnoreCase(deviceType)) {
						meidList.add(seqNum + ":" + deviceId);
					} else if(DMDConstants.DEVICE_TYPE_IMEI.equalsIgnoreCase(deviceType)) {
						if(simId != null && DMDConstants.DEVICE_TYPE_ICCID.equalsIgnoreCase(DMDUtils.getDeviceIDType(simId))) {
							imeiIccidList.add(seqNum + ":" + deviceId + "," + simId);
						} else {
							imeiList.add(seqNum + ":" + deviceId);
						}
					} else {
						validDeviceCnt--;
						
						if(simId != null) {
							//invalidDeviceSimIdList.add(deviceId + "," + simId);
							invalidDeviceSimIdList.add(deviceId + "," + simId + "," + seqNum);
						} else {
							//invalidDeviceIdList.add(deviceId);
							invalidDeviceIdList.add(deviceId + "," + seqNum);
						}
					}
					
					simId = null;
				} else if(deviceSku != null) {
					validDeviceCnt++;
					
					deviceType = DMDUtils.getDeviceIDType(deviceSku);
					if(simId != null && DMDConstants.DEVICE_TYPE_ICCID.equalsIgnoreCase(DMDUtils.getDeviceIDType(simId))) {
						imeiSkuIccidList.add(seqNum + ":" + deviceSku + "," + simId);
					} else {
						//invalidDeviceSimIdList.add(deviceSku + "," + simId);
						invalidDeviceSimIdList.add(deviceSku + "," + simId  + "," + seqNum);
					}
					
					simId = null;
				}
				
				if(simId != null) {
					if(DMDConstants.DEVICE_TYPE_ICCID.equalsIgnoreCase(DMDUtils.getDeviceIDType(simId))) {
						validSimCnt++;
						iccidList.add(seqNum + ":" + simId);
					} else {
						//invalidSimIdList.add(simId);
						invalidSimIdList.add(simId + "," + seqNum);
					}
				}
			}
			L.info("DMDBulkDeviceSimLookup.defaultAction: Device/Sim count: " + nbrNode);
			L.debug("validDeviceCnt="+validDeviceCnt);
			L.debug("validSimCnt="+validSimCnt);
			
			BulkDeviceSimLookupDAO.fetchAndPrintBulkDeviceSimInfo( new PrintStream(out, true)
														         , esnList, meidList, imeiList, iccidList
														         , imeiIccidList, imeiSkuIccidList, invalidDeviceIdList
														         , invalidSimIdList, invalidDeviceSimIdList);				
			out.flush();
			out.close();
			
			statsLogBuf.append(appType)
					.append(DMDConstants.DMD_PIPE)
					.append(DMDUtils.getClientIP(req))
					.append(DMDConstants.DMD_PIPE)
					.append(DMDConstants.LOOKUP)
					.append(DMDConstants.DMD_PIPE)
					.append(DMDConstants.BULK_DEVICE_SIM_LOOKUP)
					.append(DMDConstants.DMD_PIPE)
					.append(DMDConstants.DMD_NONE)
					.append(DMDConstants.DMD_PIPE)
					.append(DMDConstants.DMD_FALSE)
					.append(DMDConstants.DMD_PIPE)
					.append(DMDConstants.STATUS_MESSAGE_SUCCESS);
		} catch (Exception e) {
			L.error(e.getMessage(), e);
			out.println("<?xml version=\"1.0\"?><dmd><responseHeader><statusCode>02</statusCode><message>DMD Application Error.</message></responseHeader><responseBody/></dmd>");
			out.flush();
			out.close();

			statsLogBuf.append(appType)
						.append(DMDConstants.DMD_PIPE)
						.append(DMDUtils.getClientIP(req))
						.append(DMDConstants.DMD_PIPE)
						.append(DMDConstants.LOOKUP)
						.append(DMDConstants.DMD_PIPE)
						.append(DMDConstants.BULK_DEVICE_SIM_LOOKUP)
						.append(DMDConstants.DMD_PIPE)
						.append(DMDConstants.DMD_NONE)
						.append(DMDConstants.DMD_PIPE)
						.append(DMDConstants.DMD_FALSE)
						.append(DMDConstants.DMD_PIPE)
						.append(DMDConstants.STATUS_MESSAGE_ERROR);
		} finally{
			Date exitTime = new Date();
			DMDLogs.getStatsLogger().info(statsLogBuf.toString());
			long prcTime = exitTime.getTime() - entryTime.getTime();
			DMDLogs.getEStatsLogger().info(
                    statsLogBuf.toString() + DMDConstants.DMD_PIPE  + requestId  + DMDConstants.DMD_PIPE                
                            + DMDProps.ldf.format(entryTime) + DMDConstants.DMD_PIPE
                            + DMDProps.ldf.format(exitTime) + DMDConstants.DMD_PIPE + prcTime);
    		if(prcTime > 25000) {
    			L.error("DMDBulkDeviceSimLookup.defaultAction: SLOWNESS ALERT: " + prcTime);
    		}
		}
	}


	private final static String SCHEMA_FILE_NAME = "universal_multi_device_sim_validation_request.xsd";

	private static void validateSchema(String xmlString)
			throws SchemaValidationException {
		//Fortify Fix - XML External Entity Injection
		String dmdProps = System.getProperty("PROPPATH") + File.separator + SCHEMA_FILE_NAME;
		File file= new File(Normalizer.normalize(dmdProps, Normalizer.Form.NFD));
		
		String schemaURL = file.getAbsolutePath();
		
		/*String schemaURL = new File(System.getProperty("PROPPATH")
				+ File.separator + SCHEMA_FILE_NAME).getAbsolutePath();*/

		SAXParser parser = new SAXParser();
		try {
			parser.setFeature("http://xml.org/sax/features/validation", true);
			parser.setFeature("http://apache.org/xml/features/validation/schema", true);
			parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
			parser.setProperty( "http://java.sun.com/xml/jaxp/properties/schemaSource", new File(schemaURL));

			SchemaValidator handler = new SchemaValidator();
			parser.setErrorHandler(handler);
			parser.parse(new InputSource(new StringReader(xmlString)));

			L.debug("validation error: " + handler.validationError);

			if (handler.validationError) {
				L.debug("XML Document has error: "
						+ handler.validationError + "----"
						+ handler.saxParseException.getMessage());
				throw new SchemaValidationException(handler.saxParseException.getMessage());
			} else {
				L.debug("XML document is valid");
			}
		} catch (SchemaValidationException e) {
			throw e;
		} catch (IOException e) {
			throw new SchemaValidationException(e);
		} catch (SAXException e) {
			throw new SchemaValidationException(e);
		} catch (Exception e) {
			throw new SchemaValidationException(e);
		}
	}	

}