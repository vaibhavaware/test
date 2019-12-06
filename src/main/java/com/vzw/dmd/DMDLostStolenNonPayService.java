/*
 * Created on Jul 7, 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.vzw.dmd;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.owasp.esapi.ESAPI;

import com.vzw.dmd.util.DBUtils;
import com.vzw.dmd.util.DMDConstants;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.DMDUtils;
import com.vzw.dmd.util.ESAPIValidationUtils;
import com.vzw.dmd.util.LostStolenNonPayProcessor;




/**
 * @author c0gaddv
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DMDLostStolenNonPayService extends HttpServlet {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger L =
        Logger.getLogger(DMDLogs.getLogName(DMDLostStolenNonPayService.class));
    /**
    * @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
    	
    	 /*defaultAction(req, res);
    	 res.addHeader("Access-Control-Allow-Origin", "*");
         res.addHeader("Access-Control-Allow-Methods", "GET,POST");
         res.addHeader("Access-Control-Allow-Headers", "Content-Type");*/
         
         L.info("HTTP GET: " + DMDUtils.getClientIP(req));
 		
 		// Set to expire far in the past.
 		res.setHeader("Expires", "Sat, 6 May 1995 12:00:00 GMT");
 		// Set standard HTTP/1.1 no-cache headers.
 		res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
 		// Set IE extended HTTP/1.1 no-cache headers (use addHeader).
 		res.addHeader("Cache-Control", "post-check=0, pre-check=0");
 		// Set standard HTTP/1.0 no-cache header.
 		res.setHeader("Pragma", "no-cache");		
 		
 		PrintWriter out = res.getWriter();
 		out.println("<html><body>");
 		out.println("<h3>Please use HTTP XML POST</h3>");
 		out.println(new java.util.Date());
 		out.println("</body><html>");
 		out.flush();
 		out.close();
         
    }

    /**
    * @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
    public void doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
    	
    	res.addHeader("Access-Control-Allow-Origin", "*");
        res.addHeader("Access-Control-Allow-Methods", "POST");
        res.addHeader("Access-Control-Allow-Headers", "Content-Type");
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
    	
    	res.addHeader("Access-Control-Allow-Origin", "*");
        res.addHeader("Access-Control-Allow-Methods", "GET,POST");
        res.addHeader("Access-Control-Allow-Headers", "Content-Type");
        
    	    	
        StringBuffer statsLogBuf = new StringBuffer(DMDConstants.XML_NEW);
        statsLogBuf.append(DMDConstants.DMD_PIPE);
        Date entryTime = new Date();
        
                
        try{
        	LostStolenNonPayProcessor processor = new LostStolenNonPayProcessor();
        	 ServletOutputStream out = res.getOutputStream();
        	 
           /* String xmlReq=req.getParameter("xmlReq");
            
            if(xmlReq==null || xmlReq.trim().length()==0){
                xmlReq=req.getParameter("xmlreqdoc");
            }*/
            
            //support only for post
            ServletInputStream xmlRequest = req.getInputStream();
            String xmlReq = DMDUtils.getStringFromInputStream(xmlRequest);
                 
            L.info("DMDLostStolenNonPayService.defaultAction() xmlReq--"+xmlReq);
            L.debug("before calling processMsg method");
            String statusCode = processor.processMsg(xmlReq);
            
            L.info("DMDLostStolenNonPayService.defaultAction() statusCode--"+statusCode);
            L.debug("DMDLostStolenNonPayService.defaultAction() statusCode--"+statusCode);
           
            L.debug(""+"============");
            L.debug("requestXML :"+xmlReq);           
            L.debug(""+"============");         
           
            		
            if(statusCode.equals(DMDConstants.STATUS_CODE_ERROR)){//error
               
                statsLogBuf .append(DMDConstants.DMD_PIPE)
                            .append(DMDUtils.getClientIP(req))
                            .append(DMDConstants.DMD_PIPE)
                            .append(DMDConstants.LOOKUP)                            
                            .append(DMDConstants.DMD_LOST_STOLEN)
                            .append(DMDConstants.DMD_PIPE)
                            .append(DMDConstants.DMD_NONE)
                            .append(DMDConstants.DMD_PIPE)
                            .append(DMDConstants.DMD_FALSE)
                            .append(DMDConstants.DMD_PIPE)
                            .append(getInvalidStatusErrorResp());
                
                out.println(getInvalidStatusErrorResp());
                out.flush();
                out.close();
            }else if(statusCode.equals(DMDConstants.STATUS_CODE__INVALID_INPUT)){//error
               
                statsLogBuf.append(DMDConstants.DMD_PIPE)
                .append(DMDUtils.getClientIP(req))
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.LOOKUP)                
                .append(DMDConstants.DMD_LOST_STOLEN)
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.DMD_NONE)
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.DMD_FALSE)
                .append(DMDConstants.DMD_PIPE)
                .append(getInvalidStatusErrorResp());
                out.println(getInvalidStatusErrorResp());
                out.flush();
                out.close();
            }else{
            	String safeStatusCode = ESAPIValidationUtils.getValidContent(statusCode);
            	statsLogBuf.append(DMDConstants.DMD_PIPE)
                .append(DMDUtils.getClientIP(req))
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.LOOKUP)    
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.DMD_LOST_STOLEN)
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.DMD_NONE)
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.DMD_TRUE)
                .append(DMDConstants.DMD_PIPE)
                .append(safeStatusCode);
                out.println(safeStatusCode);
                out.flush();
                out.close();
            }
            
            
          //feb 2019 BQVT-963
			//send input request to new dmd write queue for cassandra processing
			//need to be handled via on-off toggle
			String serviceEnabled = DBUtils.getDBPropertyValue("DMDLOSTSTOLENCASSSERVICE");
			L.debug("serviceEnabled-"+serviceEnabled);
			//add service name to the xml
			if("Y".equalsIgnoreCase(serviceEnabled))
			{
				String messageText =  "<xmlReq><apiName>DMDLostStolenNonPayService</apiName><dmdXmlReq><![CDATA["+xmlReq+"]]></dmdXmlReq></xmlReq>";
				L.debug("outgoing jms msg-"+messageText);
				DMDMqClient mqClient = new DMDMqClient();
				mqClient.sendMessageToQueue(messageText);			
			
			}
            
            
            
            
        }catch (Exception e){
            statsLogBuf.append(DMDConstants.STATUS_MESSAGE_ERROR);
            L.error("ERRRO-"+e);
            e.printStackTrace();
        }finally{
            Date exitTime = new Date();
            DMDLogs.getStatsLogger().info(statsLogBuf.toString());
            long prcTime = exitTime.getTime() - entryTime.getTime();
            DMDLogs.getEStatsLogger().info(
                    statsLogBuf.toString()  
                        + DMDConstants.DMD_PIPE + DMDProps.ldf.format(entryTime) + DMDConstants.DMD_PIPE
                        + DMDProps.ldf.format(exitTime) + DMDConstants.DMD_PIPE + prcTime);
            if(prcTime > 5000) {
                L.info("DMDLostStolenNonPayService: SLOWNESS ALERT: " + prcTime);
            }
        }
    }
    
    private String getInvalidStatusErrorResp() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><dmd><responseHeader><statusCode>-1</statusCode><message>Unable to process the request</message></responseHeader><responseBody></responseBody></dmd>";
    }   
    private String getValidStatusResp() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><dmd><responseHeader><statusCode>0</statusCode><message>Success</message></responseHeader><responseBody></responseBody></dmd>";
    }
}
