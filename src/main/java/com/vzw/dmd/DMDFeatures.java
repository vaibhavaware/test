/*
 * Created on Jul 7, 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.vzw.dmd;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.vzw.dmd.util.DMDConstants;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.DMDUtils;
import com.vzw.dmd.util.LTEProcessor;
import com.vzw.dmd.util.LteXmlCreator;
import com.vzw.dmd.util.XSSEncoder;
import com.vzw.dmd.util.XXEDisabler;
import com.vzw.dmd.util.XmlUtils;
import com.vzw.dmd.valueobject.UniversalVO;

/**
 * @author c0gaddv
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DMDFeatures extends HttpServlet {
    private static Logger L =
        Logger.getLogger(DMDLogs.getLogName(DMDFeatures.class));
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
        
        UniversalVO universalVO = new UniversalVO(); 
        universalVO.setFromMq(true);
        ServletOutputStream out = res.getOutputStream();
        try{
            LTEProcessor processor = new LTEProcessor();
            String xmlReq=req.getParameter("xmlReq");
            if(xmlReq==null || xmlReq.trim().length()==0){
                xmlReq=req.getParameter("xmlreqdoc");
            }
            
            L.info("DMDFeatures.defaultAction() starts for valid xml check");
         
            //BQVT - 1254
            boolean isXmlValid = validateRequestXml(xmlReq);
            
            if(isXmlValid){
            
            processor.getUniversalData_New(xmlReq, universalVO, true);
            String returnXML = LteXmlCreator.createUniversalXML(universalVO, true);
           
            String statusCode=universalVO.getStatusCode();
            if(statusCode == null){
                returnXML = getInvalidStatusErrorResp();
                statusCode = "-1";
            }
            L.debug(""+"============");
            L.debug("requestXML :"+xmlReq);
            L.debug("returnXML :"+returnXML);
            L.debug(""+"============");         
            L.debug("logXML :"+returnXML.replaceAll("</>", "</>\n"));
          
            
            if(statusCode.equals(DMDConstants.STATUS_CODE_ERROR)){//error
                statsLogBuf.append(universalVO.getRequestInfo().getAppType())
                            .append(DMDConstants.DMD_PIPE)
                            .append(DMDUtils.getClientIP(req))
                            .append(DMDConstants.DMD_PIPE)
                            .append(DMDConstants.LOOKUP)
                            .append(DMDConstants.DMD_PIPE)
                            .append(DMDConstants.UNIVERSAL_WEB)
                            .append(DMDConstants.DMD_PIPE)
                            .append(DMDConstants.DMD_FEATURES)
                            .append(DMDConstants.DMD_PIPE)
                            .append(DMDConstants.DMD_NONE)
                            .append(DMDConstants.DMD_PIPE)
                            .append(DMDConstants.DMD_FALSE)
                            .append(DMDConstants.DMD_PIPE)
                            .append(universalVO.getStatusMessage());
                
                //Fortify Fix - Cross-site scripting
                out.println(encoder.encodeXML(returnXML));
                out.flush();
                out.close();
            }else if(statusCode.equals(DMDConstants.STATUS_CODE__INVALID_INPUT)){//error
                statsLogBuf.append(universalVO.getRequestInfo().getAppType())
                .append(DMDConstants.DMD_PIPE)
                .append(DMDUtils.getClientIP(req))
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.LOOKUP)
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.UNIVERSAL_WEB)
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.DMD_FEATURES)
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.DMD_NONE)
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.DMD_FALSE)
                .append(DMDConstants.DMD_PIPE)
                .append(universalVO.getStatusMessage());
                //Fortify Fix - Cross-site scripting
                out.println(encoder.encodeXML(returnXML));
                out.flush();
                out.close();
            }else{
                statsLogBuf.append(universalVO.getRequestInfo().getAppType())
                .append(DMDConstants.DMD_PIPE)
                .append(DMDUtils.getClientIP(req))
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.LOOKUP)
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.UNIVERSAL_WEB)
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.DMD_FEATURES)
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.DMD_NONE)
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.DMD_TRUE)
                .append(DMDConstants.DMD_PIPE)
                .append(universalVO.getStatusMessage());
                //Fortify Fix - Cross-site scripting
                out.println(encoder.encodeXML(returnXML));
                out.flush();
                out.close();
            }
            
            }//valid xml
            else{
            	
            	L.info("DMDFeatures.defaultAction() inside not valid xml--"+getInvalidXmlResp());
            	
            	 statsLogBuf.append(universalVO.getRequestInfo().getAppType())
                 .append(DMDConstants.DMD_PIPE)
                 .append(DMDUtils.getClientIP(req))
                 .append(DMDConstants.DMD_PIPE)
                 .append(DMDConstants.LOOKUP)
                 .append(DMDConstants.DMD_PIPE)
                 .append(DMDConstants.UNIVERSAL_WEB)
                 .append(DMDConstants.DMD_PIPE)
                 .append(DMDConstants.DMD_FEATURES)
                 .append(DMDConstants.DMD_PIPE)
                 .append(DMDConstants.DMD_NONE)
                 .append(DMDConstants.DMD_PIPE)
                 .append(DMDConstants.DMD_TRUE)
                 .append(DMDConstants.DMD_PIPE)
                 .append(universalVO.getStatusMessage());
                 //Fortify Fix - Cross-site scripting
                 out.println(encoder.encodeXML(getInvalidXmlResp()));
                 out.flush();
                 out.close();
            }
        }catch (Exception e){
            statsLogBuf.append(DMDConstants.STATUS_MESSAGE_ERROR);
        }finally{
            Date exitTime = new Date();
            DMDLogs.getStatsLogger().info(statsLogBuf.toString());
            long prcTime = exitTime.getTime() - entryTime.getTime();
            DMDLogs.getEStatsLogger().info(
                    statsLogBuf.toString() + DMDConstants.DMD_PIPE + universalVO.getRequestInfo().getRequestId() 
                        + DMDConstants.DMD_PIPE + DMDProps.ldf.format(entryTime) + DMDConstants.DMD_PIPE
                        + DMDProps.ldf.format(exitTime) + DMDConstants.DMD_PIPE + prcTime);
            if(prcTime > 5000) {
                L.info("DMDFeatures: SLOWNESS ALERT: " + prcTime);
            }
        }
    }
    
    private String getInvalidStatusErrorResp() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><dmd><responseHeader><statusCode>-1</statusCode><message>DMD application error.</message></responseHeader><responseBody><deviceInfo></deviceInfo><simInfo></simInfo><deviceSimCompatible></deviceSimCompatible><deviceSimM2MPair></deviceSimM2MPair></responseBody></dmd>";
    }
    
    private boolean validateRequestXml(String xmlReq){
    	
    	boolean isValid = true;
    	
    	L.info("DMDFeatures.validateRequestXml() starts");
    	
    	DocumentBuilderFactory docBuilderFactory;
		try {
			docBuilderFactory = new XXEDisabler().disableDBF(DocumentBuilderFactory.newInstance());
			Document document = docBuilderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(xmlReq)));
			
			String deviceId=XmlUtils.getValue(document, DMDConstants.UNIVERSAL_DEVICE_ID_QUERY);
			String deviceSku=XmlUtils.getValue(document, DMDConstants.UNIVERSAL_DEVICE_SKU_QUERY);
			String simId=XmlUtils.getValue(document, DMDConstants.UNIVERSAL_SIM_ID_QUERY);
			String simSku=XmlUtils.getValue(document, DMDConstants.UNIVERSAL_SIM_SKU_QUERY);
			String eId = XmlUtils.getValue(document, DMDConstants.UNIVERSAL_E_ID_QUERY);
			String eSku = XmlUtils.getValue(document, DMDConstants.UNIVERSAL_E_SKU_QUERY);
			
			if(isEmpty(deviceId) && isEmpty(deviceSku) && isEmpty(simId) && isEmpty(simSku) && isEmpty(eId) && isEmpty(eSku))
				isValid = false;
			
			L.info("DMDFeatures.validateRequestXml() isValid--"+isValid);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  	
    	
    	return isValid;   	
    	
    }
    
    private boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}	
    
    private String getInvalidXmlResp() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><dmd><responseHeader><statusCode>-1</statusCode><message>Invalid XML request.</message></responseHeader><responseBody><deviceInfo></deviceInfo><simInfo></simInfo><deviceSimCompatible></deviceSimCompatible><deviceSimM2MPair></deviceSimM2MPair></responseBody></dmd>";
    }
	
}
