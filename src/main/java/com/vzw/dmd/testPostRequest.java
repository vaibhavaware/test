/*
 * Created on Jun 26, 2008
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.vzw.dmd;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

import com.vzw.dmd.util.DMDLogs;

/**
 * @author damodra
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class testPostRequest {

	public static void main(String[] args) {
		
		 try {
	        // Construct data
		 	Logger L = Logger.getLogger(DMDLogs.getLogName(testPostRequest.class));
	        String data = URLEncoder.encode("app_type", "UTF-8") + "=" + URLEncoder.encode("ACSS", "UTF-8");
	        data += "&" + URLEncoder.encode("pESN", "UTF-8") + "=" + URLEncoder.encode("0013E09D560F", "UTF-8");
	    
	        
	        //String data = "app_type" + "=" + "ACSS";
	        //data += "&" + "MACID" + "=" + "0013E09D9A7A" ;
	        //data += "&" + "pESN" + "=" + "04700000005" ;

	        // Send data
	        //URL url = new URL("http://localhost:9080/dmd/DMDHUBXml?");
	        URL url = new URL("http://dmdtest.vzwcorp.com/dmd/DMDHUBXml?");
	        
	        URLConnection conn = url.openConnection();
	        conn.setDoOutput(true);
	        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	        wr.write(data);
	        wr.flush();
	    
	        // Get the response
	        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        String line;
	        while ((line = rd.readLine()) != null) {
	            // Process line...
	        	L.debug("xmlReqString : "+line);
	        }
	        wr.close();
	        rd.close();
	    } catch (Exception e) {
	    }

	}
}
