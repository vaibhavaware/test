package com.vzw.dmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.vzw.dmd.util.DMDConstants;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.DMDUtils;
import com.vzw.dmd.util.InnoPathProcessor;
import com.vzw.dmd.valueobject.UniversalVO;

/**
 *
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DMDInnoPathService extends HttpServlet {
    private static Logger L =
        Logger.getLogger(DMDLogs.getLogName(DMDInnoPathService.class));
    /**
    * @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
    	System.out.println("DMDInnoPathService.doGet() starts --");
    	res.addHeader("Access-Control-Allow-Origin", "*");
        res.addHeader("Access-Control-Allow-Methods", "GET,POST");
        res.addHeader("Access-Control-Allow-Headers", "Content-Type");
    	 defaultAction(req, res);
    	 res.addHeader("Access-Control-Allow-Origin", "*");
         res.addHeader("Access-Control-Allow-Methods", "GET,POST");
         res.addHeader("Access-Control-Allow-Headers", "Content-Type");
         System.out.println("DMDInnoPathService.enclosing_method() fininshed--");
    }

    /**
    * @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
    public void doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
    	System.out.println("DMDInnoPathService.doPost()");
    	res.addHeader("Access-Control-Allow-Origin", "*");
        res.addHeader("Access-Control-Allow-Methods", "GET,POST");
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
    	
    	System.out.println("DMDInnoPathService.defaultAction() start --");
    	System.out.println("DMDInnoPathService.defaultAction() res ---");
    	
    	res.addHeader("Access-Control-Allow-Origin", "*");
        res.addHeader("Access-Control-Allow-Methods", "GET,POST");
        res.addHeader("Access-Control-Allow-Headers", "Content-Type");
        
    	System.out.println("DMDInnoPathService.defaultAction() done ---");
    	
        StringBuffer statsLogBuf = new StringBuffer(DMDConstants.XML_NEW);
        statsLogBuf.append(DMDConstants.DMD_PIPE);
        Date entryTime = new Date();
        
       // UniversalVO universalVO = new UniversalVO(); 
       // universalVO.setFromMq(false);
        ServletOutputStream out = res.getOutputStream();
        //out.
        
        try{
        	InnoPathProcessor processor = new InnoPathProcessor();
            String xmlReq = null;
            
            /*xmlReq = req.getParameter("xmlReq");
            
            if(xmlReq==null || xmlReq.trim().length()==0){
                xmlReq=req.getParameter("xmlreqdoc");
            }*/
            
            StringBuffer reqBuffer = new StringBuffer();
            String line = null;
            
            BufferedReader reader = req.getReader();
            while ((line = reader.readLine()) != null){
            	reqBuffer.append(line);
            }
            xmlReq = reqBuffer.toString();
            L.info(""+"============");
            L.info("requestXML :"+xmlReq);           
            L.info(""+"============");         
          
            
            
            String statusCode= processor.processMsg(xmlReq);
            
            if(statusCode.equals(DMDConstants.STATUS_CODE_ERROR)){//error
                statsLogBuf .append(DMDConstants.DMD_PIPE)
                            .append(DMDUtils.getClientIP(req))
                            .append(DMDConstants.DMD_PIPE)
                            .append(DMDConstants.LOOKUP)
                            .append(DMDConstants.DMD_PIPE)
                            .append(DMDConstants.DMD_INNOPATH_WEB)
                            .append(DMDConstants.DMD_PIPE)
                            .append(DMDConstants.DMD_INNOPATH)
                            .append(DMDConstants.DMD_PIPE)
                            .append(DMDConstants.DMD_NONE)
                            .append(DMDConstants.DMD_PIPE)
                            .append(DMDConstants.DMD_FALSE)
                            .append(DMDConstants.DMD_PIPE);
                			//.append(universalVO.getRequestInfo().getAppType())
                            //.append(universalVO.getStatusMessage());
                
                out.println(getInvalidStatusErrorResp());
                out.flush();
                out.close();
            }else if(statusCode.equals(DMDConstants.STATUS_CODE__INVALID_INPUT)){//error
                statsLogBuf.append(DMDConstants.DMD_PIPE)
                .append(DMDUtils.getClientIP(req))
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.LOOKUP)
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.DMD_INNOPATH_WEB)
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.DMD_INNOPATH)
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.DMD_NONE)
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.DMD_FALSE)
                .append(DMDConstants.DMD_PIPE);
               // .append(universalVO.getRequestInfo().getAppType())
                //.append(universalVO.getStatusMessage());
                out.println(getInvalidStatusErrorResp());
                out.flush();
                out.close();
            }else{
                statsLogBuf.append(DMDConstants.DMD_PIPE)
                .append(DMDUtils.getClientIP(req))
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.LOOKUP)
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.DMD_INNOPATH_WEB)
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.DMD_INNOPATH)
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.DMD_NONE)
                .append(DMDConstants.DMD_PIPE)
                .append(DMDConstants.DMD_TRUE)
                .append(DMDConstants.DMD_PIPE);
               // .append(universalVO.getRequestInfo().getAppType())
               // .append(universalVO.getStatusMessage());
                out.println(getValidStatusResp());
                out.flush();
                out.close();
            }
        }catch (Exception e){
            statsLogBuf.append(DMDConstants.STATUS_MESSAGE_ERROR);
            e.printStackTrace();
        }finally{
            Date exitTime = new Date();
            DMDLogs.getStatsLogger().info(statsLogBuf.toString());
            long prcTime = exitTime.getTime() - entryTime.getTime();
            DMDLogs.getEStatsLogger().info(
                    statsLogBuf.toString() + DMDConstants.DMD_PIPE  //+ universalVO.getRequestInfo().getRequestId() 
                        + DMDConstants.DMD_PIPE + DMDProps.ldf.format(entryTime) + DMDConstants.DMD_PIPE
                        + DMDProps.ldf.format(exitTime) + DMDConstants.DMD_PIPE + prcTime);
            if(prcTime > 5000) {
                L.info("DMDInnoPathService: SLOWNESS ALERT: " + prcTime);
            }
        }
    }
    
    private String getInvalidStatusErrorResp() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><dmd><responseHeader><statusCode>-1</statusCode><message>DMD application error.</message></responseHeader><responseBody></responseBody></dmd>";
    }
    private String getValidStatusResp() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><dmd><responseHeader><statusCode>0</statusCode><message>Update Success</message></responseHeader><responseBody></responseBody></dmd>";
    }
}
