/*
 * Created on Jul 7, 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.vzw.dmd;

import java.io.IOException;


import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.vzw.dmd.exception.EsnNotFoundException;
import com.vzw.dmd.exception.InvalidInputException;
import com.vzw.dmd.util.DMDConstants;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.DMDUtils;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vzw.dmd.valueobject.DeviceInfo;
import com.vzw.dmd.valueobject.RMLookupResponseVO;
import com.vzw.dmd.dao.DeviceLookupDAO;
import com.vzw.dmd.dao.OracleDAOFactory;
import com.vzw.dmd.dao.OracleEsnLookupDAO;
import com.vzw.dmd.dao.RMLookupDAO;
import com.vzw.dmd.exception.DaoException;
import com.vzw.dmd.exception.EsnNotFoundException;
import com.vzw.dmd.util.DmdRMXmlCreator;



/**
 * @author c0gaddv
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DmdRM extends HttpServlet {
	private static Logger L = Logger.getLogger( DMDLogs.getLogName( DmdRM.class ) );
	
	
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
		throws ServletException, IOException 
	{
		StringBuffer statsLogBuf = new StringBuffer( "XML|" );

		res.setStatus( 200 );
		res.setContentType( "text/xml" );
		Date entryTime = new Date();
		ServletOutputStream out = res.getOutputStream();

		try
		{
			String esn = req.getParameter( "esn" );
			String meid = req.getParameter( "meid" );
			String app_type = req.getParameter( "app_type" );
			
			// 4G Change
			String device_id = req.getParameter( "deviceID" );
			//
			
			 if(meid != null){
			 	meid = meid.toUpperCase();
			 }
			 
			System.out.println( "esn: " + esn + ", meid: " + meid + ", app_type: " + app_type  + "device_id" + device_id);
			
			if ( app_type == null || app_type.trim().length() == 0 ) 
				app_type = "UNKNOWN";
			
			statsLogBuf.append(
					app_type.trim().toUpperCase() + "|"	+ DMDUtils.getClientIP(req)+ "|LOOKUP|");

			/*if ( esn == null && meid == null )
			{
				statsLogBuf.append( "UNKNOWN|UNKNOWN|Esn or Meid must be passed as a parameter" );
				throw new InvalidInputException( "Esn or Meid must be passed as a parameter" );
			}
			*/
		
			RMLookupResponseVO vo = new RMLookupResponseVO();
			DeviceInfo deviceInfo = new DeviceInfo();
			if(device_id != null && !device_id.trim().equals(DMDConstants.EMPTY_STRING)){
				deviceInfo.setDeviceIdType(DMDUtils.getDeviceIDType(device_id));
				if(DMDConstants.DEVICE_TYPE_ESN.equals(deviceInfo.getDeviceIdType())){
					deviceInfo.setDeviceId(DMDUtils.addLeadingZeros(device_id));
			}else{
					deviceInfo.setDeviceId(device_id);
				}
			}
			int deviceNotFound =DeviceLookupDAO.locateDevice(deviceInfo);
			L.debug("Device ID "+device_id + "Device Found " + deviceNotFound );
			if(deviceNotFound  == DMDConstants.DMD_DEVICE_NOT_FOUND && DMDConstants.DEVICE_TYPE_IMEI.equals(deviceInfo.getDeviceIdType()) && deviceInfo.getDeviceId().length()==14){
				deviceInfo.setDeviceIdType(DMDConstants.DEVICE_TYPE_MEID);
				deviceNotFound =DeviceLookupDAO.locateDevice(deviceInfo);
			}
			L.debug("Device ID "+device_id + "Device Found " + deviceNotFound );
			if ( DMDConstants.DEVICE_TYPE_ESN.equals(deviceInfo.getDeviceIdType()))
			{
				Pattern esnPat = Pattern.compile( "\\d{11}" );
				Matcher esnMatcher = esnPat.matcher( device_id );
				
				if ( !esnMatcher.matches() )
				{
					statsLogBuf.append( "FALSE|Invalid ESN " + device_id );
					throw new InvalidInputException( "The input meid is not a valid ESN format" );					
				}
				
				statsLogBuf.append( "ESN_INFO|" + esn + "|" );
						
				vo.setEsn( device_id );
			}
			else if(DMDConstants.DEVICE_TYPE_MEID.equals(deviceInfo.getDeviceIdType()))
			{
				Pattern meidHexPat = Pattern.compile("[A-Fa-f0-9]{14}");
				Matcher meidHexMatcher = meidHexPat.matcher( device_id.trim() );
				if ( !meidHexMatcher.matches() )
				{
					statsLogBuf.append( "FALSE|Invalid MEID " + device_id );
					throw new InvalidInputException( "The input meid is not a valid hex meid format" );
				}
				
				statsLogBuf.append( "MEID_INFO|" + device_id + "|" );
				
				vo.setMeid( device_id );
			}
			else if(DMDConstants.DEVICE_TYPE_IMEI.equals(deviceInfo.getDeviceIdType())){
				vo.setImei(device_id);
			}
			else if (DMDConstants.DEVICE_TYPE_ICCID.equals(deviceInfo.getDeviceIdType())){
				vo.setIccid(device_id);
			}
			
			RMLookupDAO rmLookupDAO = OracleDAOFactory.getInstance().getRmLookupDAO();
			
			vo = rmLookupDAO.lookup( vo ,deviceInfo);		
			
            OracleEsnLookupDAO daoLookup = (OracleEsnLookupDAO) OracleDAOFactory.getInstance()
                    .createEsnLookupDAO();
            
            if(null == vo.getDeviceType()) {
            	vo.setDeviceType("");
            }
                    
//            try{
//            	vo.setDeviceType(deviceInfo.getDeviceIdType());
//            }catch(Exception e){
//            	vo.setDeviceType("");
//            }
			out.println( DmdRMXmlCreator.getStringFromDocumentWithFormat( 
					DmdRMXmlCreator.getRMLookupDocument( vo ) ) );

			out.flush();
			out.close();
			
			statsLogBuf.append( "TRUE|" );
		}
		catch ( InvalidInputException ie )
		{
			out.println( constructMessageXml( "NOT FOUND", ie.getMessage() ) ); 
			out.flush();
			out.close();
			return;
		}
		catch ( DaoException daoe )
		{
			statsLogBuf.append( "FALSE|" + daoe.getMessage() );
			
			out.println( constructMessageXml( "NOT FOUND", daoe.getMessage() ) ); 
			out.flush();
			out.close();
			return;
		}
		catch ( EsnNotFoundException enfe )
		{
			out.println( constructMessageXml( "NOT FOUND", enfe.getMessage() ) ); 
			out.flush();
			out.close();
			return;
			
		}
		finally
		{
			Date exitTime = new Date();
			DMDLogs.getStatsLogger().info(statsLogBuf.toString());
			long prcTime = exitTime.getTime() - entryTime.getTime();
			DMDLogs.getEStatsLogger().info(
                    statsLogBuf.toString() + "|"
                            + DMDProps.ldf.format(entryTime) + "|"
                            + DMDProps.ldf.format(exitTime) + "|" + prcTime);			
		}
	}
	
	
	
	private String constructMessageXml( String statusString, String messageString )
	{
		return DmdRMXmlCreator.getStringFromDocumentWithFormat( 
					DmdRMXmlCreator.getMessageDocument( statusString, messageString ) );
	}
}
