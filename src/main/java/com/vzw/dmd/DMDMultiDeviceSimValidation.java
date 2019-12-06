package com.vzw.dmd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.BufferedReader;
import java.net.URLDecoder;
import java.text.Normalizer;
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

import com.vzw.dmd.dao.DeviceSimValidationDAO;
import com.vzw.dmd.exception.DaoException;
import com.vzw.dmd.exception.SchemaValidationException;
import com.vzw.dmd.util.DMDConstants;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.DMDUtils;
import com.vzw.dmd.util.SchemaValidator;
import com.vzw.dmd.util.XSSEncoder;
import com.vzw.dmd.util.XXEDisabler;
import com.vzw.dmd.util.XmlUtils;

@SuppressWarnings("serial")
public class DMDMultiDeviceSimValidation extends HttpServlet {
	private static Logger L = Logger.getLogger(DMDLogs.getLogName(DMDMultiDeviceSimValidation.class));
	XSSEncoder encoder = new XSSEncoder();
	
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

		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;
		try {
			InputStream inputStream = req.getInputStream();
			if (inputStream != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				char[] charBuffer = new char[4096];
				int bytesRead = -1;
				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			} else {
				stringBuilder.append("");
			}
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException ex) {
					throw ex;
				}
			}
		}  
		String body = URLDecoder.decode(stringBuilder.toString(), "UTF-8"); 
		// L.debug("body=" + body);	
		// L.debug("subString: " + body.indexOf("<dmd>") + " - " + body.indexOf("</dmd>")+"</dmd>".length());
		String xmlReqt = body.substring(body.indexOf("<dmd>"), body.indexOf("</dmd>")+"</dmd>".length());

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
	public void defaultAction(HttpServletRequest req, HttpServletResponse res, String xmlReqt)
		throws ServletException, IOException {
		L.debug("DMDMultiDeviceSimValidation : xmlReqt: "+xmlReqt);
		
		StringBuffer statsLogBuf = new StringBuffer(DMDConstants.XML_NEW);
		statsLogBuf.append(DMDConstants.DMD_PIPE);
		Date entryTime = new Date();
		
		//res.setContentType("text/xml");
		ServletOutputStream out = res.getOutputStream();

		String xmlResp = "";
		String statusCode = "";
		String statusMessage = "";
		
		String appType = "", requestId = "";
		boolean featureInquired = false;
		boolean lockInquired = false;
		boolean lostStolenInquired = false;

		StringBuffer deviceIdList = new StringBuffer();
		StringBuffer simIdList = new StringBuffer();

		DocumentBuilderFactory df = null;
		Document document = null;
		try {
			if (DMDProps.isMultiDeviceSimSchemaValidation()) {
				//Date beforeValidateSchema = new Date();
				//validateSchema(xmlReqt);
				//Date afterValidateSchema = new Date();
				//long timeValidateSchema = afterValidateSchema.getTime() - beforeValidateSchema.getTime();
				//L.debug("timeValidateSchema=" + timeValidateSchema);
			}
			//Fortify Fix - XML External Entity Injection
			df = new XXEDisabler().disableDBF(DocumentBuilderFactory.newInstance());
			DocumentBuilder db = df.newDocumentBuilder();
			document = db.parse(new InputSource(new StringReader(xmlReqt)));

			appType = XmlUtils.getValue(document, DMDConstants.UNIVERSAL_APP_TYPE_QUERY);
			requestId = XmlUtils.getValue(document, DMDConstants.UNIVERSAL_REQUEST_ID_QUERY);
			featureInquired    = "Y".equalsIgnoreCase(XmlUtils.getValue(document, "dmd/requestBody/featureAndDaccAndCompatibility"));
			lockInquired       = "Y".equalsIgnoreCase(XmlUtils.getValue(document, "dmd/requestBody/deviceLock"));
			lostStolenInquired = "Y".equalsIgnoreCase(XmlUtils.getValue(document, "dmd/requestBody/lostStolenAndNonPay"));
			L.debug("appType=" + appType + ", requestId=" + requestId + ", featureInquired=" + featureInquired
					+ ", lockInquired=" + lockInquired + ", lostStolenInquired=" + lostStolenInquired);

			NodeIterator nit = XPathAPI.selectNodeIterator(document, "dmd/requestBody/devicePair");
			Element el;
			int nbrNode = 0;
			boolean firstPair = true;
			while ((el=(Element)nit.nextNode()) != null) {
				nbrNode++;
				String deviceId = null, simId = null;
				deviceId = getElementVal(el, "deviceId", "");
				if(deviceId == null) {
					deviceId = getElementVal(el, "deviceSku", "MSKU");
				} else {
					deviceId = deviceId.replaceAll("[^a-zA-Z0-9]", "").toUpperCase().trim();
				}
				
				simId = getElementVal(el, "simId", "");
				if(simId == null) {
					simId = getElementVal(el, "simSku", "SSKU");
				} else {
					simId = simId.replaceAll("[^a-zA-Z0-9]", "").toUpperCase().trim();
				}
				
				if( simId != null || deviceId != null) {
					if(deviceId == null) {
						deviceId = "x";
					}					
					
					if(simId == null) {
						simId = "x";
					}
					
					if(firstPair) {
						firstPair = false;
					} else {
						deviceIdList.append(",");
						simIdList.append(",");
					}
					
					deviceIdList.append(deviceId);
					simIdList.append(simId);
				}				
			}
			
			L.debug("nbrNode="+nbrNode+", deviceIdList="+deviceIdList.toString()+", simIdList="+simIdList.toString());
			
			if(firstPair) {
				statusCode = DMDConstants.STATUS_CODE__INVALID_INPUT;
				xmlResp="<?xml version=\"1.0\"?><dmd><responseHeader><statusCode>01</statusCode><message>No device id or sim id or sku in request XML.</message></responseHeader><responseBody></responseBody></dmd>";
				statusMessage = "No device/sim/sku in request XML";
				L.info("DVS API Request(" + requestId + "): No device/sim/sku in request XML");				
			}

		} catch (Exception e) {
			statusCode = DMDConstants.STATUS_CODE__INVALID_INPUT;
			xmlResp="<?xml version=\"1.0\"?><dmd><responseHeader><statusCode>01</statusCode><message>FAILED</message></responseHeader><responseBody></responseBody></dmd>";

			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			statusMessage = sw.toString();
			L.error(statusMessage);
		}
		
		try {
			if (! statusCode.equals(DMDConstants.STATUS_CODE__INVALID_INPUT)) {
				xmlResp = DeviceSimValidationDAO.validateMultiDeviceSim(featureInquired, lockInquired, 
						lostStolenInquired, deviceIdList.toString(), simIdList.toString());
				L.debug("DMDMultiDeviceSimValidation : xmlResp: "+xmlResp);
			}

			if (xmlResp == null || xmlResp.length()==0) {
				statusCode = DMDConstants.STATUS_CODE_ERROR;
			}
			
			if (statusCode.equals(DMDConstants.STATUS_CODE_ERROR)) { //error
				statsLogBuf.append(appType)
							.append(DMDConstants.DMD_PIPE)
							.append(DMDUtils.getClientIP(req))
							.append(DMDConstants.DMD_PIPE)
							.append(DMDConstants.LOOKUP)
							.append(DMDConstants.DMD_PIPE)
							.append(DMDConstants.MULTI_DEVICE_SIM_VALIDATION_API)
							.append(DMDConstants.DMD_PIPE)
			                .append(DMDConstants.DMD_DVS)
							.append(DMDConstants.DMD_PIPE)
							.append(DMDConstants.DMD_NONE)
							.append(DMDConstants.DMD_PIPE)
							.append(DMDConstants.DMD_FALSE)
							.append(DMDConstants.DMD_PIPE)
							.append(statusMessage);	
				
				out.println(encoder.encodeXML(xmlResp));
				out.flush();
				out.close();
			} else if (statusCode.equals(DMDConstants.STATUS_CODE__INVALID_INPUT)) { //error
				statsLogBuf.append(appType)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDUtils.getClientIP(req))
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.LOOKUP)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.MULTI_DEVICE_SIM_VALIDATION_API)
				.append(DMDConstants.DMD_PIPE)
			    .append(DMDConstants.DMD_DVS)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.DMD_NONE)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.DMD_FALSE)
				.append(DMDConstants.DMD_PIPE)
				.append(statusMessage);	
				
				out.println(encoder.encodeXML(xmlResp));
				out.flush();
				out.close();
			} else {
				statsLogBuf.append(appType)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDUtils.getClientIP(req))
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.LOOKUP)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.MULTI_DEVICE_SIM_VALIDATION_API)
				.append(DMDConstants.DMD_PIPE)
			    .append(DMDConstants.DMD_DVS)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.DMD_NONE)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.DMD_TRUE)
				.append(DMDConstants.DMD_PIPE)
				.append(statusMessage);	
	
				out.print(encoder.encodeXML(xmlResp));
				out.flush();
				out.close();
			}
		} catch (DaoException e){
			L.error(e.getMessage(), e);
			statsLogBuf.append(appType)
			.append(DMDConstants.DMD_PIPE)
			.append(DMDUtils.getClientIP(req))
			.append(DMDConstants.DMD_PIPE)
			.append(DMDConstants.LOOKUP)
			.append(DMDConstants.DMD_PIPE)
			.append(DMDConstants.MULTI_DEVICE_SIM_VALIDATION_API)
			.append(DMDConstants.DMD_PIPE)
			.append(DMDConstants.DMD_DVS)
			.append(DMDConstants.DMD_PIPE)
			.append(DMDConstants.DMD_NONE)
			.append(DMDConstants.DMD_PIPE)
			.append(DMDConstants.DMD_FALSE)
			.append(DMDConstants.DMD_PIPE)
			.append(DMDConstants.STATUS_MESSAGE_ERROR);	
		} catch (Exception e){
			L.error(e.getMessage(), e);
			statsLogBuf.append(appType)
			.append(DMDConstants.DMD_PIPE)
			.append(DMDUtils.getClientIP(req))
			.append(DMDConstants.DMD_PIPE)
			.append(DMDConstants.LOOKUP)
			.append(DMDConstants.DMD_PIPE)
			.append(DMDConstants.MULTI_DEVICE_SIM_VALIDATION_API)
			.append(DMDConstants.DMD_PIPE)
			.append(DMDConstants.DMD_DVS)
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
                    statsLogBuf.toString() + DMDConstants.DMD_PIPE                    
                            + DMDProps.ldf.format(entryTime) + DMDConstants.DMD_PIPE
                            + DMDProps.ldf.format(exitTime) + DMDConstants.DMD_PIPE + prcTime);
		}
	}
	
	private static String getElementVal(Element element, String tagName, String preFix) {
		NodeList nodes;
		String value = null;
		if ((nodes=element.getElementsByTagName(tagName)) != null) {
			if (nodes.getLength() > 0) {
				if (nodes.item(0).getFirstChild() != null) {
					value = nodes.item(0).getFirstChild().getNodeValue();
					if (value != null) {
						value = value.trim();
						if("".equals(value)) {
							value = null;
						} else {
							value = preFix + value;
						}
					}
				}
			}
		}
		return value;
	}

	private final static String SCHEMA_FILE_NAME = "universal_multi_device_sim_validation_request.xsd";

	public static void validateSchema(String xmlString)
			throws SchemaValidationException {
		/*String schemaURL = new File(System.getProperty("PROPPATH")
				+ File.separator + SCHEMA_FILE_NAME).getAbsolutePath();*/
		String schemaURL = new File(Normalizer.normalize(System.getProperty("PROPPATH")
				+ File.separator + SCHEMA_FILE_NAME, Normalizer.Form.NFD)).getAbsolutePath();

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

