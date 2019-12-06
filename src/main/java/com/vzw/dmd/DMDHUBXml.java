

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

import com.vzw.dmd.dao.OracleDAOFactory;
import com.vzw.dmd.dao.OracleEsnLookupDAO;
import com.vzw.dmd.exception.DaoException;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.DMDUtils;
import com.vzw.dmd.util.XSSEncoder;
import com.vzw.dmd.util.XXEDisabler;
import com.vzw.dmd.util.XmlUtils;
import com.vzw.dmd.valueobject.DMDHubMacIdLookupRequestVO;


public class DMDHUBXml extends HttpServlet {
	private static Logger L = Logger.getLogger(DMDLogs.getLogName(DMDHUBXml.class));
	XSSEncoder encoder = new XSSEncoder();

	public void doGet(HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException {
		try {
			defaultAction(req, res);
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
				if(name.equalsIgnoreCase("app_type")){
					
					requestMode = "A";
				}
			}
			if(requestMode.equals("A")){
				L.debug("xmlReqString request type : ACSS");
				defaultAction(req, res);
			}else{
				L.debug("xmlReqString request type : MTAS");
				postAction(req, res);
				
			}

			//ServletInputStream xmlRequest = req.getInputStream();
			//InputSource inputSourceForXmlRequest = null;
			//String reqAppType = "";
			//String reqMacId = "";
			//String reqPesn = "";
			//String xmlReqString = "";
			
			/*
			String resStatus = "ERROR";
			String resMacid = "Not Found";
			String resPesn = "Not Found";
			L.debug("TEST1 TEST 	--doPost--- : ");

			
			String appType = req.getParameter("app_type") != null ? (String) req.getParameter("app_type") : "";
			L.debug("request parameter apptype 1: ");

			
			L.debug("request parameter apptype : "+appType);
			
			
			if(appType == null){
				appType = "";
			}
			
			
			if (appType.trim().toUpperCase().equals("ACSS")){
				//req.getParameter("MACID");
				L.debug("xmlReqString request type : ACSS");
				//defaultAction(req, res);
				//postActionString(req, res,xmlReqString);
				defaultAction(req, res);
			}else{
				L.debug("xmlReqString request type : MTAS");
				//L.debug("c:" + req.hashCode());
				postAction(req, res);
				

			}
			*/
			//postAction(req, res);
			/*
			String xmlReqString = getStringFromInputStream(xmlRequest);
			if (xmlReqString.indexOf("<DMD>") > 0){
				L.debug("xmlReqString request type : MTAS");	
				postAction(req, res);
			}else{
				//req.getParameter("MACID");
				L.debug("xmlReqString request type : ACSS");
				//defaultAction(req, res);
				//postActionString(req, res,xmlReqString);
				defaultAction(req, res);
			}
			*/

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void defaultAction(HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException, DaoException, NamingException, CreateException {
		L.debug("Test: Default Action ");
		//String treq = req.getParameter("xmlreqdoc");
		//L.debug("xml req: "+treq);
		res.setStatus(200);
		res.setContentType("text/xml");
		L.debug("Test1: ");
		OracleDAOFactory oracleDAOFactory = OracleDAOFactory.getInstance();
		OracleEsnLookupDAO daoLookup = (OracleEsnLookupDAO)oracleDAOFactory.createEsnLookupDAO();
		ServletOutputStream out = res.getOutputStream();
		
		StringBuffer statsLogBuf = new StringBuffer("XML|");
		
		Date entryTime = new Date();
		L.debug("Test2: ");
		String appType = req.getParameter("app_type");
		L.debug("Test3: "+appType);
		statsLogBuf.append(appType + "|");		
		statsLogBuf.append(DMDUtils.getClientIP(req) + "|");
		statsLogBuf.append("LOOKUP|");
		statsLogBuf.append("ESN_FEATURES|");
		//L.debug("appType DEFAULT ACTION: "+req.getParameterValues("MACID"));
		//String action = req.getParameter("action");
		
		if (req.getParameterValues("pESN") != null) {
			
			String pesn = req.getParameter("pESN");
			L.debug("Test3 pESN: "+pesn);
			pesn = DMDUtils.addLeadingZeros(pesn.trim());
			L.debug("After Leading zeros: "+pesn);
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			out.println("	<DMD>");

			DMDHubMacIdLookupRequestVO vo = new DMDHubMacIdLookupRequestVO();				
					vo.setPesn(pesn);
				
				
				String macid = daoLookup.getDMDHubMacid(vo);
				if (macid != null && !macid.equals("")) 
				{
					out.println("			<STATUS>Normal</STATUS>");
					//Fortify Fix - Cross-site scripting
					out.println("<pESN>" + encoder.encodeXMLAttribute(pesn) + "</pESN>");
					out.println("<MACID>" + encoder.encodeXMLAttribute(macid) + "</MACID>");
					
					statsLogBuf.append(macid + "|");
					statsLogBuf.append("TRUE|");

				}
				
				else {
					out.println("			<STATUS>ERROR</STATUS>");
					out.println("<pESN>" + encoder.encodeXMLAttribute(pesn) + "</pESN>");					
					out.println("<MACID>Not Found</MACID>");
					
					statsLogBuf.append(pesn + "|");
					statsLogBuf.append("FALSE|Invalid pESN");
				}
				
			out.println("</DMD>");
		}
		else if (req.getParameterValues("MACID") != null){
			
			String macid = req.getParameter("MACID").toUpperCase();
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			out.println("	<DMD>");
			
			L.debug("Test3 macid: "+macid);
				
			DMDHubMacIdLookupRequestVO vo = new DMDHubMacIdLookupRequestVO();				
				

					//vo.setMeid(DMDUtils.convertMEIDFromHexToDecimal(meid));
			vo.setMacid(macid);

					String pesn = daoLookup.getDMDHubPesn(vo);
				if (pesn != null && !pesn.equals("")) 
				{				
					out.println("			<STATUS>Normal</STATUS>");
					out.println("<MACID>" + encoder.encodeXMLAttribute(macid) + "</MACID>");					
					out.println("<pESN>" + encoder.encodeXMLAttribute(pesn) + "</pESN>");
					
					statsLogBuf.append(macid + "|");
					statsLogBuf.append("TRUE|");
				}
				
				else {
					out.println("			<STATUS>ERROR</STATUS>");
					out.println("<MACID>" + encoder.encodeXMLAttribute(macid) + "</MACID>");					
					out.println("<pESN>Not Found</pESN>");
					
					statsLogBuf.append(macid + "|");
					statsLogBuf.append("FALSE|Invalid MACID");

				}
				
			out.println("</DMD>");

		}else {
			L.debug("xmlReqString request type111 : ACSS "+req.getParameter("MACID"));
			 
			  out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			  out.println("<DMD>");
			  out.println("			<STATUS>ERROR</STATUS>"); 
			  out.println("			<MESSAGE>Please input pESN or MACID to search the database</MESSAGE>"); 
			  out.println("</DMD>");
		}
		
		Date exitTime = new Date();
	    String transId = req.getParameter("transaction_id");
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
	}
	
	public void postAction(
			HttpServletRequest request,
			HttpServletResponse res)
			throws ServletException, IOException {
			String methodName = "postAction : ";
			String xmlReqString = "";

/*			
	//		String xml = request.getParameter("xmlreqdoc");
//			L.debug("xml : "+xml);

			BufferedReader xml1 = request.getReader();
			StringBuffer env = new StringBuffer();
			String line = null;
			while ((line = xml1.readLine()) != null) {
				env.append(line);
			}
			L.debug("sb string : "+env.toString());
*/
			L.debug("c:" + request.hashCode());
			StringBuffer statsLogBuf = new StringBuffer("XML|");
			
			Date entryTime = new Date();
			
			ServletInputStream xmlRequest = request.getInputStream();
			L.debug("xmlRequest  postAction : "+xmlRequest.toString());
			PrintWriter out = res.getWriter();

			//InputSource inputSourceForXmlRequest = null;

			String reqAppType = "";
			String reqMacId = "";
			String reqPesn = "";
			
			String resStatus = "ERROR";
			String resMacid = "Not Found";
			String resPesn = "Not Found";

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
								
						int appTypeStartIndex = xmlReqString.indexOf("<APP_TYPE>");
						int appTypeEndIndex = xmlReqString.indexOf("</APP_TYPE>");
						L.debug("appTypeStartIndex : "+appTypeStartIndex);
						L.debug("appTypeEndIndex : "+appTypeEndIndex);
						if (appTypeStartIndex != -1 && appTypeEndIndex != -1) {
							reqAppType =
								xmlReqString.substring(
									appTypeStartIndex + 10,
									appTypeEndIndex);
							L.debug("reqAppType is not null : "+reqAppType);
						}

						statsLogBuf.append(reqAppType + "|");		
						statsLogBuf.append(DMDUtils.getClientIP(request) + "|");
						statsLogBuf.append("LOOKUP|");
						statsLogBuf.append("ESN_FEATURES|");
						
						int macIdStartIndex = xmlReqString.indexOf("<MACID>");
						int macIdEndIndex = xmlReqString.indexOf("</MACID>");

						if (macIdStartIndex != -1 && macIdEndIndex != -1) {
							reqMacId =
								xmlReqString.substring(
									macIdStartIndex + 7,
									macIdEndIndex);
							L.debug("Got Request for pESN/Macid : ");
						}

						int pEsnStartIndex = xmlReqString.indexOf("<pESN>");
						int pEsnEndIndex = xmlReqString.indexOf("</pESN>");

						if (pEsnStartIndex != -1 && pEsnEndIndex != -1) {
							reqPesn =
								xmlReqString.substring(
										pEsnStartIndex + 6,
										pEsnEndIndex);
							L.debug("Got Request for pESN/Macid : ");
						}
					}

					if ((reqPesn == null) &&  (reqMacId == null)){
						resStatus = "ERROR";
						resMacid = "Invalid MACID";
						resPesn = "Invalid Pesn";	
					} 
					else {
						if (reqMacId.trim().length() == 12){
							String macid = reqMacId.toUpperCase();
								
							DMDHubMacIdLookupRequestVO vo = new DMDHubMacIdLookupRequestVO();				
							vo.setMacid(macid);

							OracleDAOFactory oracleDAOFactory = OracleDAOFactory.getInstance();
							OracleEsnLookupDAO daoLookup = (OracleEsnLookupDAO)oracleDAOFactory.createEsnLookupDAO();
							resPesn = daoLookup.getDMDHubPesn(vo);				

							
							if (resPesn == null){
								resStatus = "ERROR";
								resPesn = "Not Found";
							}else{
								resStatus = "Normal";
							}
							L.debug("Got Response for pESN/Macid : "+resPesn);

							statsLogBuf.append(macid + "|");
							statsLogBuf.append("TRUE|");

							createResponseXML(out,resStatus , macid, resPesn);
						}else{
							reqPesn = DMDUtils.addLeadingZeros(reqPesn.trim());
							DMDHubMacIdLookupRequestVO vo = new DMDHubMacIdLookupRequestVO();				
							vo.setPesn(reqPesn);

							OracleDAOFactory oracleDAOFactory = OracleDAOFactory.getInstance();
							OracleEsnLookupDAO daoLookup = (OracleEsnLookupDAO)oracleDAOFactory.createEsnLookupDAO();
							resMacid = daoLookup.getDMDHubMacid(vo);
							if (resMacid == null){
								L.debug("ERROR for pESN/Macid : ");
								resStatus = "ERROR";
								resMacid = "Not Found";
							}else{
								L.debug("Normal for pESN/Macid : ");
								resStatus = "Normal";
							}
							L.debug("Got Response for pESN/Macid : "+resMacid);
							
							statsLogBuf.append(reqPesn + "|");
							statsLogBuf.append("TRUE|");

							createResponseXML(out,resStatus , resMacid, reqPesn);
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
	
	private void createResponseXML(
			PrintWriter out,
			String resStatus,
			String resMacid,
			String resPesn) {
			try {
				L.debug("Got Response for pESN : "+resMacid);
				L.debug("Got Response for Macid : "+resPesn);
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document xmlDoc = db.newDocument();
				Element root = xmlDoc.createElement("DMD");
				xmlDoc.appendChild(root);

				Element child1 = xmlDoc.createElement("STATUS");
				child1.appendChild(xmlDoc.createTextNode(resStatus));
				root.appendChild(child1);

				Element child2 = xmlDoc.createElement("MACID");
				child2.appendChild(xmlDoc.createTextNode(resMacid));
				root.appendChild(child2);

				Element child3 = xmlDoc.createElement("pESN");
				child3.appendChild(xmlDoc.createTextNode(resPesn));
				root.appendChild(child3);

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
