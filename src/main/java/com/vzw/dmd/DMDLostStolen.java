package com.vzw.dmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.SAXParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.vzw.dmd.util.DBUtils;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDLostStolenNonPayMq;
import com.vzw.dmd.util.DMDUtils;
import com.vzw.dmd.util.LostStolenNonPayBean;
import com.vzw.dmd.util.XXEDisabler;
import com.vzw.dmd.util.XmlUtils;
import com.vzw.dmd.valueobject.LostStolenNonPayResponseVO;
import com.vzw.dmd.valueobject.LostStolenNonPayVO;
import com.vzw.dmd.valueobject.PIBLockRequestVO;



/**
 * @version 	1.0
 * @author
 */
public class DMDLostStolen extends HttpServlet {

	//private static Logger L = Logger.getLogger(DMDLogs.getLogName(DMDXmlPost.class));
	private static Logger L = Logger.getLogger(DMDLogs.getPibLogName(DMDLostStolen.class));	
	
	public static final Logger statsLogger = DMDLogs.getStatsLogger();

	public void doGet(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
		
		L.info("HTTP GET: " + DMDUtils.getClientIP(req));
		
		defaultAction (req, res);
	}
	
	/**
	* @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	*/
	public void doPost(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
		defaultAction (req, res);
	}

	public void defaultAction(
			HttpServletRequest request,
			HttpServletResponse res)
			throws ServletException, IOException {
			PIBLockRequestVO pibLockRequestVO = new PIBLockRequestVO();
			String methodName = "deafultAction : ";
			String xmlReqString = "";



			PrintWriter out = res.getWriter();
			//ServletOutputStream out = null;
			//String schemaURL = "file:///D:/DMD-utilities/props/etc/dmd_request.xsd";
			L.debug(System.getProperty("PROPPATH"));
			/*String schemaURL =
				new File(
					System.getProperty("PROPPATH")
						+ File.separator
						+ "dmd_request.xsd")
					.getAbsolutePath();*/
			String schemaURL = new File(Normalizer.normalize(System.getProperty("PROPPATH")
					+ File.separator + "dmd_request.xsd", Normalizer.Form.NFD)).getAbsolutePath();

			//L.debug(methodName + "schemaURL : " + schemaURL);
			
			InputSource inputSourceForXmlRequest = null;
			String message = "SUCCESS";
			String retailerName = "";
			ServletInputStream xmlRequest = request.getInputStream();
			
			

			try {
				xmlReqString = request.getParameter("xmlReq");
				//xmlReqString = getStringFromInputStream(xmlRequest);
				/*
				xmlReqString = 
			        	"<Verizon>"+
			        	"<ReqId>414d512053444d4</ReqId>"+
			        	"<UsrId>ABCD1234</UsrId>" +
			        	"<SourceClass>ABCD1234</SourceClass>"+
			        	"<Source>MTAS</Source>"+
			         	"<TrnType>StolenAdd</TrnType>"+
			         	"<MDN>6097090567</MDN>" +
			         	"<ESN>00608603584</ESN>"+
			        	"<MEID></MEID>"+
			        	"<ICCID></ICCID>"+
			         	"<IMEI></IMEI>"+
			        	"<StolenDate>11/14/2009</StolenDate>"+
			        	"<ReportOnFile>Y</ReportOnFile>"+
			        	"<DateFiled>11/14/2009</DateFiled>"+
			        	"<LastUser></LastUser>"+
			        	"<LastUpdDate></LastUpdDate>"+
			        	"<Recovered>N</Recovered>"+
			        	"<RestoreUser></RestoreUser>"+
			        	"<RestoreDate></RestoreDate>"+
			        	"<CustomerId>0000775387</CustomerId>"+
			        	"<AccountNum>00001</AccountNum>"+
			        	"</Verizon>";
					*/
				L.info("INCOMM REQUEST: " + DMDUtils.getClientIP(request) + "|" + xmlReqString);
				L.info("INCOMM REQUEST test1 : " + DMDUtils.getClientIP(request) + "|" + xmlReqString);				


				DMDLostStolenNonPayMq.processMsgXMLString(xmlReqString);

				//feb 2019 BQVT-963
				//send input request to new dmd write queue for cassandra processing
				//need to be handled via on-off toggle
				String serviceEnabled = DBUtils.getDBPropertyValue("DMDLOSTSTOLENCASSSERVICE");
				//add service name to the xml
				if("Y".equalsIgnoreCase(serviceEnabled))
				{
					String messageText =  "<xmlReq><apiName>DMDLostStolen</apiName><dmdXmlReq><![CDATA["+xmlReqString+"]]></dmdXmlReq></xmlReq>";
					DMDMqClient mqClient = new DMDMqClient();
					mqClient.sendMessageToQueue(messageText);			
				
				}


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

			//		if(xmlRequest!=null)
			//			xmlRequest.close();
		}
	
	public void defaultAction_old(
		HttpServletRequest request,
		HttpServletResponse res)
		throws ServletException, IOException {
		PIBLockRequestVO pibLockRequestVO = new PIBLockRequestVO();
		String methodName = "deafultAction : ";
		String xmlReqString = "";

		ServletInputStream xmlRequest = request.getInputStream();

		PrintWriter out = res.getWriter();
		//ServletOutputStream out = null;

		//String schemaURL = "file:///D:/DMD-utilities/props/etc/dmd_request.xsd";
		L.debug(System.getProperty("PROPPATH"));
		/*String schemaURL =
			new File(
				System.getProperty("PROPPATH")
					+ File.separator
					+ "dmd_request.xsd")
				.getAbsolutePath();*/
		String schemaURL = new File(Normalizer.normalize(System.getProperty("PROPPATH")
				+ File.separator + "dmd_request.xsd", Normalizer.Form.NFD)).getAbsolutePath();

		//L.debug(methodName + "schemaURL : " + schemaURL);
		
		InputSource inputSourceForXmlRequest = null;
		String message = "SUCCESS";
		String retailerName = "";

		try {
			xmlReqString = getStringFromInputStream(xmlRequest);
			L.info("INCOMM REQUEST 111: " + DMDUtils.getClientIP(request) + "|" + xmlReqString);

			// Temperorly commented for schema validation
			/*
			inputSourceForXmlRequest =
				new InputSource(new StringReader(xmlReqString));
			L.debug(
				methodName
					+ "DMDProps.isSchemaValidation()--"
					+ DMDProps.isSchemaValidation());

			if (DMDProps.isSchemaValidation()) {
				message =
					validateSchema(
						schemaURL,
						inputSourceForXmlRequest,
						pibLockRequestVO,
						xmlReqString);
			} else {
				//manual validation
				message =
					validateParsedInputSource(
						inputSourceForXmlRequest,
						pibLockRequestVO);
			}
			*/
			message =
				validateSchema(
					schemaURL,
					inputSourceForXmlRequest,
					pibLockRequestVO,
					xmlReqString);
			
			boolean checkForPurchase = setRequestToValueObject(inputSourceForXmlRequest,pibLockRequestVO,xmlReqString);
			
			if (message == null || message.trim().startsWith("ERR:")) {
				String errorMsg = null;
				errorMsg = "Invalid XML Request";
				String code = "3";

				L.debug(
					"DMD Error Message Response: " + errorMsg);

				String reqType = "";
				String reqId = "";

				if (xmlReqString != null) {
					int reqTypeStartIndex = xmlReqString.indexOf("<req_type>");
					int reqTypeEndIndex = xmlReqString.indexOf("</req_type>");

					if (reqTypeStartIndex != -1 && reqTypeEndIndex != -1) {
						reqType =
							xmlReqString.substring(
								reqTypeStartIndex + 10,
								reqTypeEndIndex);
					}

					int reqIdStartIndex = xmlReqString.indexOf("<req_id>");
					int reqIdEndIndex = xmlReqString.indexOf("</req_id>");

					if (reqIdStartIndex != -1 && reqIdEndIndex != -1) {
						reqId =
							xmlReqString.substring(
								reqIdStartIndex + 8,
								reqIdEndIndex);
					}

				}
				String clientIPAddress = DMDUtils.getClientIP(request);
				createResponseXML(out, clientIPAddress, reqType, reqId, code, errorMsg);

			} else {

				LostStolenNonPayVO lostStolenNonPayVO = new LostStolenNonPayVO();
				LostStolenNonPayResponseVO lostStolenNonPayResponseVO =
					LostStolenNonPayBean.addLostStolenStatus(lostStolenNonPayVO);

				

				String code = lostStolenNonPayResponseVO.getResponseCode();
				
				L.debug(
					"DMD Status Message: " + message);

				String clientIPAddress = DMDUtils.getClientIP(request);
				createResponseXML(out, clientIPAddress, pibLockRequestVO.getRequestType(), pibLockRequestVO.getRequestId(), code, message);
			}

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

		//		if(xmlRequest!=null)
		//			xmlRequest.close();
	}

	/**
	* @see javax.servlet.GenericServlet#void ()
	*/
	public void init() throws ServletException {
		super.init();
		//L.setLevel(Level.DEBUG);
	}
	
	
	
	public String validateSchema(String SchemaUrl, InputSource xmlDocument,PIBLockRequestVO pibLockRequestVO,String xmlRequest)   {
		
		String methodName = "validatateSchema : ";
		
		SAXParser parser = new SAXParser();     
		try
		{
			L.debug(methodName);
			
			parser.setFeature("http://xml.org/sax/features/validation",true);
			parser.setFeature("http://apache.org/xml/features/validation/schema",true);
			parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
			parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource",new File(SchemaUrl));
			
			Validator handler=new Validator();
	   		
			parser.setErrorHandler(handler); 
			parser.parse(xmlDocument);
			
			L.debug(methodName + "validationError : "+handler.validationError);	
			
			if(handler.validationError==true){           
				L.error(methodName + "XML Document has Error: "+handler.validationError+"----"+handler.saxParseException.getMessage());
				return "ERR:Invalid XML" ;
			}
			else {                  
				L.debug(methodName + "XML Document is valid");
				boolean checkForPurchase = setRequestToValueObject(xmlDocument,pibLockRequestVO,xmlRequest);
				
				if(!checkForPurchase){
					return "ERR:Invalid XML " ;
				}else {
					return "SUCCESS";
				}
			}
			
		 }
		catch(java.io.IOException ioe){   
			L.error(methodName + "IOException "+ioe.getMessage());
			
			return "ERR:";    
		}catch (SAXException e) { 
			L.error(methodName + "SAXException "+e.getMessage());
			return "ERR:";    
		 
		}
		catch (Exception e) { 
			L.error(methodName + "Exception  "+e.getMessage());
			return "ERR:";    
		}     

	}
	
	private class Validator extends DefaultHandler {
		
		public boolean  validationError = false;  
		public SAXParseException saxParseException=null;
		
		public void error(SAXParseException exception) throws SAXException	       {
		validationError=true;
		saxParseException=exception;
		}     

	  public void fatalError(SAXParseException exception) throws SAXException {
		validationError = true;	    
		saxParseException=exception;	     

	  }		    
	  public void warning(SAXParseException exception) throws SAXException
	   {}
	   	
	  }   



	  
	  private String validateParsedInputSource(InputSource xmlDocument,PIBLockRequestVO pibLockRequestVO) 
	  {
		String methodName ="validateParsedInputSource : "; 
		Document document = null;
		boolean isValid = true;
		String sale_date = "";
		String pib_lock = "";
		String outlet_id = "";
		String retailerName ="";
		
		// Create an XML document of incoming message.
		try{
			DocumentBuilderFactory df = new XXEDisabler().disableDBF(DocumentBuilderFactory.newInstance());
			DocumentBuilder db = df.newDocumentBuilder();
			Document inpDoc = null;
				
			try
			{
				inpDoc = db.parse(xmlDocument);
			}
			catch (Exception e)
			{	L.error(methodName + "Exception  "+e.getMessage());
				return "ERR:Invalid XML request";
			}

			// Extract ESN
			
			String req_type = XmlUtils.getValue(inpDoc, "dmd_request/request/req_type");
			pibLockRequestVO.setRequestType(req_type);
				
			String req_id = XmlUtils.getValue(inpDoc, "dmd_request/request/req_id");
			pibLockRequestVO.setRequestId(req_id);


			String esnMeid = XmlUtils.getValue(inpDoc, "dmd_request/id/esn_meid");
			
			pibLockRequestVO.setId(esnMeid);
			//L.debug(methodName + "esnMeid  "+esnMeid);
			
			pibLockRequestVO.setAppType("INCOMM");
			
			if((!req_type.equalsIgnoreCase("PIB_PURCHASE"))&&
					(!req_type.equalsIgnoreCase("PIB_RETURN"))){
				L.debug(methodName + "Request type should have either PIB_PURCHASE or PIR_RETURN");		
				isValid=false;
			}
			if(isValid){
				if(req_id==null || req_id.trim().equals("")){
					L.debug(methodName + "Request Id is mandatory");
					isValid = false;
				}
			}
			if(isValid){
				if(esnMeid!=null){
					int esnMeidLength = esnMeid.length();
					//L.debug(methodName + "esnMeidLength "+esnMeidLength);
					if(esnMeidLength!=11 && esnMeidLength!=14){
						L.debug(methodName + "esn or meid not equal to 11");
						isValid = false;
					}else{
						if(esnMeidLength!=11){
							pibLockRequestVO.setIdType("MEID");
						
							Pattern meidHexPat = Pattern.compile("[A-Fa-f0-9]{14}");
							Matcher meidHexMatcher = meidHexPat.matcher(esnMeid.trim());
							if (!meidHexMatcher.matches())
							{
								L.debug(methodName +"Invalid meid");
								isValid = false;
							}
						
							String meid = DMDUtils.convertMEIDFromHexToDecimal(esnMeid.trim());
							if (meid == null || meid.trim().equals(""))
							{
								isValid = false;
							}
							pibLockRequestVO.setMeidHex(meid);
									
						}else{
							pibLockRequestVO.setIdType("ESN");
							Pattern esnPat = Pattern.compile("\\d{11}");
							Matcher esnMatcher = esnPat.matcher(esnMeid);
							if (!esnMatcher.matches())
							{
								L.debug(methodName + "Invalid esn");
								isValid = false;
							}
						}
					}
					//isValid = true;
				}else{
					L.debug(methodName + "esnMeid null. So error out");
					isValid = false;
				}
			
				if(isValid){	
					pib_lock = XmlUtils.getValue(inpDoc, "dmd_request/data/pib_lock");
					//L.debug(methodName + "pib_lock-"+pib_lock);
					pibLockRequestVO.setLockRequest(pib_lock);
				
					if(pib_lock!=null){
						pib_lock = pib_lock.trim().toUpperCase();
					}
				
					Pattern pibPat = Pattern.compile("[NY]");
					Matcher pibMatcher = pibPat.matcher(pib_lock);
					if (!pibMatcher.matches())
					{
						L.debug(methodName + "Invalid PIB value. It should be either Y or N");
						isValid = false;
					}else{
						isValid = true;
					}
				}
				
				if(isValid){
					retailerName = XmlUtils.getValue(inpDoc, "dmd_request/data/retailer_name");
					
					
					if(retailerName!=null){
						retailerName = retailerName.trim().toUpperCase();
					}else{
						retailerName="";
					}
					
					pibLockRequestVO.setRetailerName(retailerName);
				}
			
				if(isValid){
					if(req_type.equalsIgnoreCase("PIB_RETURN"))
					{
						L.debug("The Request type is PIB_RETURN. So set the "+
								"sales start date and out let to null");
							
						pibLockRequestVO.setSalesDate("");
						pibLockRequestVO.setOutletId(outlet_id);
						isValid = true;
				
					}else{
						sale_date = XmlUtils.getValue(inpDoc, "dmd_request/data/sale_date");
						L.debug(methodName + "sale_date-"+sale_date);
						if(sale_date==null || sale_date.equals("")){
							L.debug(methodName + "Sales Date is mandatory");
							isValid = false;
						}else{
							isValid = true;
						}
						if(isValid){
							SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							try{
								java.util.Date requestSalesDate = formatter.parse(sale_date);
								L.debug(methodName + "requestSalesDate---"+requestSalesDate);
								pibLockRequestVO.setSalesDate(sale_date);
								isValid = true;
							}catch(Exception e){
								isValid = false;	
								L.error(methodName + "invalid sale_date "+sale_date);
							}
							if(isValid){
								outlet_id = XmlUtils.getValue(inpDoc, "dmd_request/data/outlet_id");
								L.debug(methodName + "outlet_id "+outlet_id);
								if((outlet_id==null) || (outlet_id.trim().equals("")) || (outlet_id.trim().length()>18)){
									L.debug(methodName + "invalid Outlet ID");
									isValid = false;
								}else{
									pibLockRequestVO.setOutletId(outlet_id);
									isValid = true;
								}
							}

						}
					}
				}
			}
		}
		catch(Exception e){
			L.debug(methodName + "Exception occured "+ e.getMessage());
			isValid=false;
		}

		
		L.debug(methodName + "Parsed XML is valid : "+isValid);
		
		if(isValid){
			return "SUCCESS";
		}else{
			return "ERR:";
		}
	  	
	  }
	  
	public static String getStringFromInputStream(ServletInputStream inputStream){
			StringBuffer sb = new StringBuffer(); 
			String 	inputLine = null;	
			BufferedReader in = null;
			try{
				in =
					new BufferedReader(
						new InputStreamReader(inputStream));
				while ((inputLine = in.readLine()) != null) {
					sb.append(inputLine);
				}
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
		
		
		private boolean setRequestToValueObject(InputSource xmlDocument,PIBLockRequestVO pibLockRequestVO,String xmlRequest){
			
			Document document =null;
			String method = "setRequestToValueObject:   ";
			
			InputSource inputSourceForXmlRequest = new InputSource(new StringReader(xmlRequest));
			// Create an XML document of incoming message.
			try{
				//Fortify Fix - XML External Entity Injection
				DocumentBuilderFactory dbf = new XXEDisabler().disableDBF(DocumentBuilderFactory.newInstance());
				DocumentBuilder db = dbf.newDocumentBuilder();

				document = db.parse(inputSourceForXmlRequest);
				
				String req_type = XmlUtils.getValue(document, "dmd_request/request/req_type");
				pibLockRequestVO.setRequestType(req_type);
				//L.debug(method+"req_type-****-----------"+req_type);
				
				String req_id = XmlUtils.getValue(document, "dmd_request/request/req_id");
				pibLockRequestVO.setRequestId(req_id);
				
				// Extract ESN
				String esnMeid = XmlUtils.getValue(document, "dmd_request/id/esn_meid");
				pibLockRequestVO.setId(esnMeid);
				//L.debug(method+"esnMeid--****----------"+esnMeid);
				
				pibLockRequestVO.setAppType("INCOMM");
				
				if(esnMeid!=null){
					int esnMeidLength = esnMeid.length();
					//L.debug(method+"esnMeidLength "+esnMeidLength);
					if(esnMeidLength!=11){
						pibLockRequestVO.setIdType("MEID");
					}
					else{
						pibLockRequestVO.setIdType("ESN");
					}
				}
				
				String sale_date = XmlUtils.getValue(document, "dmd_request/data/sale_date");
				L.debug(method+"sale_date---***---------"+sale_date);
				
				String outlet_id = XmlUtils.getValue(document, "dmd_request/data/outlet_id");
				L.debug(method+"outlet_id "+outlet_id);
				
				String retailerName = XmlUtils.getValue(document, "dmd_request/data/retailer_name");
				L.debug(method+"retailerName "+retailerName);
				
				if(req_type.equalsIgnoreCase("PIB_PURCHASE")){
					if(sale_date==null || sale_date.equals("")){
						L.info("Request Error: sale_date is mandatory");
						return false;
					}else{
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						try{
							java.util.Date requestSalesDate = formatter.parse(sale_date);
							//L.debug(method+"requestSalesDate*****------------"+requestSalesDate);
							pibLockRequestVO.setSalesDate(sale_date);
						} catch(Exception e){
							L.info("Request Error: invalid sale_date: " + sale_date);
							return false;
						}
					}
					if(outlet_id==null || (outlet_id.compareTo("")==0)){
						L.info("Request Error: outlet_id is mandatory");
						return false;
					}else{
						outlet_id = outlet_id.trim();
						pibLockRequestVO.setOutletId(outlet_id);
					}

					if (retailerName==null || (retailerName.compareTo("")==0)){
						L.info("Request Error: retailer_name is mandatory");
						return false;
					} else{
						retailerName = retailerName.trim();
						pibLockRequestVO.setRetailerName(retailerName);
					}
					
				}else{
					pibLockRequestVO.setSalesDate("");
					pibLockRequestVO.setOutletId("");
				}
				
				
				if(retailerName!=null){
					retailerName = retailerName.trim();
					pibLockRequestVO.setRetailerName(retailerName);
				}else{
					pibLockRequestVO.setRetailerName("");
				}

				
				String pib_lock = XmlUtils.getValue(document, "dmd_request/data/pib_lock");
				//L.debug(method+"Sales Date at the end  : "+pibLockRequestVO.getSalesDate());
				//L.debug(method+"pib_lock : "+pib_lock);
				if(pib_lock!=null){
					pib_lock = pib_lock.trim().toUpperCase();
					pibLockRequestVO.setLockRequest(pib_lock);
				}
			}catch(Exception e){
				L.debug(method+"Exception "+ e.getMessage());
				return false;
			}
			
			return true;
		}

	private void createResponseXML(
		PrintWriter out,
		String clientIPAddress,
		String reqType,
		String reqId,
		String statusCode,
		String statusMessage) {
		try {
		
			StreamResult streamResult = new StreamResult(out);
			SAXTransformerFactory tf =
				(SAXTransformerFactory) SAXTransformerFactory.newInstance();
			// SAX2.0 ContentHandler.
			TransformerHandler hd = tf.newTransformerHandler();
			Transformer serializer = hd.getTransformer();
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			hd.setResult(streamResult);
			hd.startDocument();

			hd.startElement("", "", "dmd_response", null);

			hd.startElement("", "", "request", null);
			hd.startElement("", "", "req_type", null);
			hd.characters(reqType.toCharArray(), 0, reqType.length());
			hd.endElement("", "", "req_type");
			hd.startElement("", "", "req_id", null);
			hd.characters(reqId.toCharArray(), 0, reqId.length());
			hd.endElement("", "", "req_id");
			hd.endElement("", "", "request");

			hd.startElement("", "", "status", null);
			hd.startElement("", "", "status_code", null);
			hd.characters(statusCode.toCharArray(), 0, statusCode.length());
			hd.endElement("", "", "status_code");
			hd.startElement("", "", "status_message", null);
			hd.characters(statusMessage.toCharArray(), 0, statusMessage.length());
			hd.endElement("", "", "status_message");
			hd.endElement("", "", "status");

			hd.endElement("", "", "dmd_response");
			hd.endDocument();

			L.info("DMD RESPONSE: " + clientIPAddress + "|" + reqId + "|" + reqType + "|" + statusCode + "|" + statusMessage);
			
		} catch (Exception e) {
			L.error("createResponseXML: Exception "+ e.getMessage());
		}
	}
}
