

package com.vzw.dmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.vzw.dmd.dao.OracleDAOFactory;
import com.vzw.dmd.dao.OracleEsnLookupDAO;
import com.vzw.dmd.exception.DaoException;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.DMDUtils;
import com.vzw.dmd.util.DMDXMLParser;
import com.vzw.dmd.util.XSSEncoder;
import com.vzw.dmd.util.XXEDisabler;
import com.vzw.dmd.util.XmlUtils;


public class DMDMacidLookup extends HttpServlet {
	private static Logger L = Logger.getLogger(DMDLogs.getLogName(DMDMacidLookup.class));
	XSSEncoder encoder = new XSSEncoder();

	public void doGet(HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException {
		try {
			defaultAction(req, res);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		try {
			
				defaultAction(req, res);

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

		if (req.getParameter("MACID") != null && req.getParameter("MACID").toUpperCase().trim().length() ==12){
			
			String macid = req.getParameter("MACID").toUpperCase().trim();			
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			out.println("	<DMD>");
			
			L.debug("Test3 macid: "+macid);
			Map<String, String> macidInfo = daoLookup.getDMDMacidAttributes(macid);
			String macidExists = (String)macidInfo.get("MACID_EXISTS");
			
			statsLogBuf.append(macid + "|");
			if (macidExists != null && !macidExists.trim().equals("")) {				
				out.println("			<STATUS>Normal</STATUS>");
				out.println("<MACID>" + encoder.encodeXMLAttribute(macid) + "</MACID>");
				statsLogBuf.append("TRUE|");
			} else {
					out.println("			<STATUS>ERROR</STATUS>");
					out.println("<MACID>" + encoder.encodeXMLAttribute(macid) + "</MACID>");
					statsLogBuf.append("FALSE|Invalid MACID");
			}
				
			out.println("<MACID_EXISTS>" + encoder.encodeXMLAttribute(macidExists) + "</MACID_EXISTS>");
			//Fortify Fix - Cross-site scripting starts
			out.println("<BOXID>" + encoder.encodeXMLAttribute(macidInfo.get("BOX_ID")) + "</BOXID>");
			out.println("<SKU>" + encoder.encodeXMLAttribute(macidInfo.get("SKU")) + "</SKU>");
			out.println("<NETWORK>" + encoder.encodeXMLAttribute(macidInfo.get("NETWORK_TYPE")) + "</NETWORK>");
			out.println("<DEVICE_TYPE>" + encoder.encodeXMLAttribute(macidInfo.get("DEVICE_TYPE")) + "</DEVICE_TYPE>");
			out.println("<MANUFACTURER>" + encoder.encodeXMLAttribute(macidInfo.get("MANUFACTURER")) + "</MANUFACTURER>");
			//Fortify Fix - Cross-site scripting Ends
			out.println("</DMD>");
		}else if (req.getParameter("BOXID") != null && req.getParameter("BOXID").toUpperCase().trim().length() !=0 && req.getParameter("BOXID").toUpperCase().trim().length() <=16){
			
			String boxId = req.getParameter("BOXID").toUpperCase().trim();			
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			out.println("	<DMD>");
			
			L.debug("Test3 boxId: "+boxId);
			Map macidInfo = daoLookup.getMacidsInBox(boxId);
			String macidExists = (String)macidInfo.get("MACID_EXISTS");
			
			statsLogBuf.append(boxId + "|");
			if (macidExists != null && !macidExists.trim().equals("")) {				
				out.println("			<STATUS>Normal</STATUS>");
				out.println("<BOXID>" + encoder.encodeXMLAttribute(boxId) + "</BOXID>");
				statsLogBuf.append("TRUE|");
			} else {
					out.println("			<STATUS>ERROR</STATUS>");
					out.println("<BOXID>" + encoder.encodeXMLAttribute(boxId) + "</BOXID>");
					statsLogBuf.append("FALSE|Invalid MACID");
			}
				
			out.println("<MACID_EXISTS>" + macidExists + "</MACID_EXISTS>");			
			String[] macidArr = ((String)macidInfo.get("MACIDLIST")).split(",");
			
			if(macidArr!=null && macidArr.length>0){
				//Fortify Fix - Cross-site scripting Starts
				Map<String, String> macidInfoForBoxId = daoLookup.getDMDMacidAttributes(macidArr[0]);
				out.println("<SKU>" + encoder.encodeXMLAttribute(macidInfoForBoxId.get("SKU")) + "</SKU>");
				out.println("<NETWORK>" + encoder.encodeXMLAttribute(macidInfoForBoxId.get("NETWORK_TYPE")) + "</NETWORK>");
				out.println("<DEVICE_TYPE>" + encoder.encodeXMLAttribute(macidInfoForBoxId.get("DEVICE_TYPE")) + "</DEVICE_TYPE>");
				out.println("<MANUFACTURER>" + encoder.encodeXMLAttribute(macidInfoForBoxId.get("MANUFACTURER")) + "</MANUFACTURER>");
				//Fortify Fix - Cross-site scripting ends
			}
			
			out.println("<MACIDLIST>");
			for(String macid:macidArr)
			{			
			out.println("<MACID>" + encoder.encodeXMLAttribute(macid) + "</MACID>");
			}
			out.println("</MACIDLIST>");				
			
			out.println("</DMD>");
			
			
		}	
		
		else {
			L.debug("xmlReqString request Error in Macid "+req.getParameter("MACID"));
			 
			  out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			  out.println("<DMD>");
			  out.println("			<STATUS>ERROR</STATUS>"); 
			  out.println("			<MESSAGE>Please input Valid MACID</MESSAGE>"); 
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
				DocumentBuilder db = DMDXMLParser.getParser();
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
	
	/*public static void main (String[] args){
		
		HttpServletResponse res;
		OracleDAOFactory oracleDAOFactory = OracleDAOFactory.getInstance();
		OracleEsnLookupDAO daoLookup = (OracleEsnLookupDAO)oracleDAOFactory.createEsnLookupDAO();
		ServletOutputStream out;
		try {
			StringBuilder outputxml = new StringBuilder();
		
		
		String boxId = "AMRYMIXG1000000";			
		
		outputxml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		
		outputxml.append("	<DMD>");
		
		L.debug("Test3 boxId: "+boxId);
		Map macidInfo = daoLookup.getMacidsInBox(boxId);
		String macidExists = (String)macidInfo.get("MACID_EXISTS");
		
		//statsLogBuf.append(boxId + "|");
		if (macidExists != null && !macidExists.trim().equals("")) {				
			outputxml.append("			<STATUS>Normal</STATUS>");
			outputxml.append("<BOXID>" + boxId + "</BOXID>");
			//statsLogBuf.append("TRUE|");
		} else {
				outputxml.append("			<STATUS>ERROR</STATUS>");
				outputxml.append("<BOXID>" + boxId + "</BOXID>");
				//statsLogBuf.append("FALSE|Invalid MACID");
		}
			
		outputxml.append("<MACID_EXISTS>" + macidExists + "</MACID_EXISTS>");		
		String[] macidArr = ((String)macidInfo.get("MACIDLIST")).split(",");
		
		if(macidArr!=null && macidArr.length >0)
		{
		Map macidInfoForBoxId = daoLookup.getDMDMacidAttributes(macidArr[0]);
		outputxml.append("<SKU>" + macidInfoForBoxId.get("SKU") + "</SKU>");
		outputxml.append("<NETWORK>" + macidInfoForBoxId.get("NETWORK_TYPE") + "</NETWORK>");
		outputxml.append("<DEVICE_TYPE>" + macidInfoForBoxId.get("DEVICE_TYPE") + "</DEVICE_TYPE>");
		outputxml.append("<MANUFACTURER>" + macidInfoForBoxId.get("MANUFACTURER") + "</MANUFACTURER>");
		}
		
		
		outputxml.append("<MACIDLIST>");
		for(String macid:macidArr)
		{
			//outputxml.append("<MACIDINFO>");
		outputxml.append("<MACID>" + macid + "</MACID>");	
		}
		outputxml.append("</MACIDLIST>");		
		
		outputxml.append("</DMD>");
		
		System.out.println("DMDMacidLookup.main() outputxml--"+outputxml);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}*/
	

}
