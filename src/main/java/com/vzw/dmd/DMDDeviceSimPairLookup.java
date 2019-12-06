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

public class DMDDeviceSimPairLookup extends HttpServlet {
	private static Logger L = Logger.getLogger(DMDLogs.getLogName(DMDDeviceSimPairLookup.class));
	
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
		
		res.setContentType("text/xml");
		ServletOutputStream out = res.getOutputStream();

		String xmlResp = "";
		String statusCode = "";
		String statusMessage = "";
		
		String appType = "", requestId = "";

		StringBuffer deviceIdList = new StringBuffer();
		StringBuffer simIdList = new StringBuffer();

		DocumentBuilderFactory df = null;
		Document document = null;
		try {
			if (DMDProps.isMultiDeviceSimSchemaValidation()) {
				validateSchema(xmlReqt);
			}
			//Fortify Fix - XML External Entity Injection
			df = new XXEDisabler().disableDBF(DocumentBuilderFactory.newInstance());
			DocumentBuilder db = df.newDocumentBuilder();
			document = db.parse(new InputSource(new StringReader(xmlReqt)));

			appType = XmlUtils.getValue(document, "dmd/requestHeader/appType");
			requestId = XmlUtils.getValue(document, "dmd/requestHeader/requestId");

			NodeIterator nit = XPathAPI.selectNodeIterator(document, "dmd/requestBody/devicePairInfoList/devicePairInfo");
			Element el;
			NodeList nodes;
			int nbrNode = 0;
			while ((el=(Element)nit.nextNode()) != null) {
				nbrNode++;
				String deviceId = null, simId = null;
				if ((nodes=el.getElementsByTagName("deviceId")) != null) {
					if (nodes.getLength() > 0) {
						if (nodes.item(0).getFirstChild() != null) {
							deviceId = nodes.item(0).getFirstChild().getNodeValue();
							if (deviceId != null) {
								deviceId = deviceId.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
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
							}
						}
					}
				}
				deviceIdList.append( deviceIdList.length() == 0 ? "" : "," )
							.append( (deviceId==null || deviceId.length()==0) ? "x" : deviceId );
				simIdList.append( simIdList.length() == 0 ? "" : "," )
						 .append( (simId==null || simId.length()==0) ? "x" : simId );
			}
			L.debug("nbrNode="+nbrNode+", deviceIdList="+deviceIdList.toString()+", simIdList="+simIdList.toString());

		} catch (Exception e) {
			statusCode = DMDConstants.STATUS_CODE__INVALID_INPUT;
			xmlResp="<?xml version=\"1.0\"?><dmd><responseHeader><statusCode>01</statusCode><message>FAILED</message></responseHeader><responseBody/></dmd>";

			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			statusMessage = sw.toString();
			L.error(statusMessage);
		}
		
		try {
			if (! statusCode.equals(DMDConstants.STATUS_CODE__INVALID_INPUT)) {
				xmlResp = DeviceSimValidationDAO.retriveDeviceSimPairInfoList(deviceIdList.toString(), simIdList.toString()); 
				L.debug("DeviceSimPairInfo : xmlResp: "+xmlResp);
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
							.append("DMD_DEVICE_SIM_PAIR_LOOKUP")
							.append(DMDConstants.DMD_PIPE)
							.append(DMDConstants.DMD_NONE)
							.append(DMDConstants.DMD_PIPE)
							.append(DMDConstants.DMD_FALSE)
							.append(DMDConstants.DMD_PIPE)
							.append(statusMessage);	
				
				//Fortify Fix - Cross-site scripting
				out.println(new XSSEncoder().encodeXML(xmlResp));
				out.flush();
				out.close();
			} else if (statusCode.equals(DMDConstants.STATUS_CODE__INVALID_INPUT)) { //error
				statsLogBuf.append(appType)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDUtils.getClientIP(req))
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.LOOKUP)
				.append(DMDConstants.DMD_PIPE)
				.append("DMD_DEVICE_SIM_PAIR_LOOKUP")
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.DMD_NONE)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.DMD_FALSE)
				.append(DMDConstants.DMD_PIPE)
				.append(statusMessage);	
				
				//Fortify Fix - Cross-site scripting
				out.println(new XSSEncoder().encodeXML(xmlResp));
				out.flush();
				out.close();
			} else {
				statsLogBuf.append(appType)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDUtils.getClientIP(req))
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.LOOKUP)
				.append(DMDConstants.DMD_PIPE)
				.append("DMD_DEVICE_SIM_PAIR_LOOKUP")
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.DMD_NONE)
				.append(DMDConstants.DMD_PIPE)
				.append(DMDConstants.DMD_TRUE)
				.append(DMDConstants.DMD_PIPE)
				.append(statusMessage);	
	
				//Fortify Fix - Cross-site scripting
				out.print(new XSSEncoder().encodeXML(xmlResp));
				out.flush();
				out.close();
			}
		} catch (Exception e){
			L.error(e.getMessage(), e);
			statsLogBuf.append(appType)
			.append(DMDConstants.DMD_PIPE)
			.append(DMDUtils.getClientIP(req))
			.append(DMDConstants.DMD_PIPE)
			.append(DMDConstants.LOOKUP)
			.append(DMDConstants.DMD_PIPE)
			.append("DMD_DEVICE_SIM_PAIR_LOOKUP")
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


	private final static String SCHEMA_FILE_NAME = "dmd_device_sim_pair_lookup_api_request.xsd";

	private static void validateSchema(String xmlString)
			throws SchemaValidationException {
		/*String schemaURL = new File(System.getProperty("PROPPATH")
				+ File.separator + SCHEMA_FILE_NAME).getAbsolutePath();*/
		String schemaURL = new File(Normalizer.normalize(System.getProperty("PROPPATH")
				+ File.separator + SCHEMA_FILE_NAME, Normalizer.Form.NFD)).getAbsolutePath();

		SAXParser parser = null;
		try {
			//Fortify Fix - XML External Entity Injection
			parser = new XXEDisabler().disableSAXParser(new SAXParser());
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
