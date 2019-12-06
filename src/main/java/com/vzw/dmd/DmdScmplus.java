/*
 * Created on Jul 7, 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.vzw.dmd;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.vzw.dmd.dao.DeviceLookupDAO;
import com.vzw.dmd.dao.OracleDAOFactory;
import com.vzw.dmd.dao.OracleEsnLookupDAO;
import com.vzw.dmd.exception.EsnNotFoundException;
import com.vzw.dmd.exception.InvalidInputException;
import com.vzw.dmd.util.DBUtils;
import com.vzw.dmd.util.DMDConstants;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.DMDUtils;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.LTEProcessor;
import com.vzw.dmd.util.XSSEncoder;
import com.vzw.dmd.valueobject.EsnLookupRequestVO;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.vzw.dmd.valueobject.DeviceInfo;

/**
 * @author c0gaddv
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DmdScmplus extends HttpServlet {
	private static Logger L =
		Logger.getLogger(DMDLogs.getLogName(DmdScmplus.class));
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
		int errorCode = 0;
		Date entryTime = new Date();
		Connection conn = null;
		CallableStatement stmt = null;
		EsnLookupRequestVO lookupResultVO = new EsnLookupRequestVO();
		StringBuffer statsLogBuf = new StringBuffer("XML|"); 
		String appType = req.getParameter("appType");
		try {
			String meid = null;
			String deviceKey = "";
			String pid = "";
			String retMsg = "";
			String esn = req.getParameter("esn");
			String device_id= req.getParameter("deviceid");
			meid = req.getParameter("meid");
			if(esn != null && !("").equals(esn)){
				device_id= esn;
			}
			else if(meid != null && !("").equals(meid)){
				device_id= meid;
			}
			
			DeviceInfo deviceInfo = new DeviceInfo();
			// check UNiversal
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
			// Check universal
			/*if (esn == null) {
				meid = req.getParameter("meid");
			}
			
			String app_type = req.getParameter("app_type");
			if (app_type == null || app_type.trim().equals("")) {
				app_type = "UNKNOWN";
			}
			statsLogBuf.append(
				app_type.trim().toUpperCase()
					+ "|"
					+ DMDUtils.getClientIP(req)
					+ "|LOOKUP|");

			if (esn == null && meid == null) {
				errorCode = 1;
				statsLogBuf.append("UNKNOWN|UNKNOWN|");
				throw new InvalidInputException("3||Esn or Meid must be passed as a parameter||");
			}
			*/	
			if(DMDConstants.DEVICE_TYPE_ESN.equals(deviceInfo.getDeviceIdType())) {
				lookupResultVO.setId(device_id);
				lookupResultVO.setIdType("ESN");
				// lookup esn
				esn = DMDUtils.addLeadingZeros(device_id.trim());
				/*OracleDAOFactory oracleDAOFactory = OracleDAOFactory.getInstance();
	            OracleEsnLookupDAO daoLookup = (OracleEsnLookupDAO) oracleDAOFactory
	                    .createEsnLookupDAO();
	            String deviceType=daoLookup.getDeviceMask();
	            */
				statsLogBuf.append("ESN_MODELINFO|" + device_id.trim() + "|");
				statsLogBuf.append(getLoggerData(appType, "", req));
				Pattern esnPat = Pattern.compile("\\d{11}");
				Matcher esnMatcher = esnPat.matcher(device_id);
				if (!esnMatcher.matches())
				{
					throw new Exception ("Invalid ESN");
				}
				
				conn = DBUtils.getDbConnection();
				if (conn == null) {
					L.error(": DmdScmplus: NO_DB_CONNECTION");
					throw new Exception("Database Connection could not be established");
				}
				stmt =
					conn.prepareCall(
						//"{call PK_DMD_SCMPLUS.sp_get_device_code_for_esn(?,?,?,?) }");
						"{call PK_DMD_SCMPLUS.sp_get_device_code_for_esn_pid(?,?,?,?,?) }");							
				stmt.registerOutParameter(1, java.sql.Types.INTEGER);
				// err_code           OUT
				stmt.registerOutParameter(2, java.sql.Types.VARCHAR);
				// err_msg            OUT
				stmt.setString(3, device_id); // esn           IN
				stmt.registerOutParameter(4, java.sql.Types.VARCHAR);
				// device_code            OUT
				stmt.registerOutParameter(5, java.sql.Types.VARCHAR);
				// pid
				stmt.execute();
				String errMsg = stmt.getString(2);
				if (stmt.getInt(1) == 1) {
					L.error(": DmdScmplus: ESN Not Found");
					throw new EsnNotFoundException("1||ESN Not found||");

				} else {
					if (stmt.getInt(1) != 0) {
						L.error(": DmdScmplus() : SQL_ERROR" + errMsg);
						throw new Exception(errMsg);
					}
					deviceKey = stmt.getString(4);
					
					// get PID
					pid = stmt.getString( 5 );
					//deviceKey="TEMP";
					if (null == deviceKey || "".equals(deviceKey)) {
						L.error(": DmdScmplus: Device Key does not exist for the given esn");
						throw new Exception("Device Key does not exist for the given esn");
					} else {
						statsLogBuf.append("TRUE|DeviceKey=" + deviceKey);
						statsLogBuf.append(getLoggerData(appType, "", req));
						retMsg = "0||Success||" + deviceKey + "||";
						
						if ( pid != null && pid.length() > 0 )
						{
							statsLogBuf.append( "|PID=" + pid );
							retMsg += pid;
						}
						
						LTEProcessor processor = new LTEProcessor();
						processor.getUniversalDeviceInfo(true, true, false, false, false, deviceInfo);
					}
				}
			} else if(deviceInfo.getDeviceIdType().equals(DMDConstants.DEVICE_TYPE_MEID)){
				lookupResultVO.setId(""+Long.parseLong(device_id,16));
				lookupResultVO.setIdType("MEID");
				// lookup meid
				device_id = device_id.toUpperCase();
				if(deviceNotFound == DMDConstants.DMD_DEVICE_NOT_FOUND){
					throw new EsnNotFoundException("1||MEID Not found||");
				}
				statsLogBuf.append("MEID_MODELINFO|" + device_id.trim() + "|");
				statsLogBuf.append(getLoggerData(appType, "", req));
				conn = DBUtils.getDbConnection();
				if (conn == null) {
					L.error(": DmdScmplus: NO_DB_CONNECTION");
					throw new Exception("Database Connection could not be established");
				}
				stmt =
					conn.prepareCall(
						//"{call PK_DMD_SCMPLUS.sp_get_device_code_for_meid(?,?,?,?)}");
						"{call PK_DMD_SCMPLUS.sp_get_device_code_for_meid_pd(?,?,?,?,?)}");
				stmt.registerOutParameter(1, java.sql.Types.INTEGER);
				// err_code           OUT
				stmt.registerOutParameter(2, java.sql.Types.VARCHAR);
				// err_msg            OUT
				stmt.setString(3, device_id); // meid        IN
				stmt.registerOutParameter(4, java.sql.Types.VARCHAR);
				// device_code            OUT
				stmt.registerOutParameter(5, java.sql.Types.VARCHAR);
				// pid
				stmt.execute();
				String errMsg = stmt.getString(2);
				if (stmt.getInt(1) == 1) {
					throw new EsnNotFoundException("1||MEID Not found||");
				} else {
					if (stmt.getInt(1) != 0) {
						L.error(": DmdScmplus() : SQL_ERROR :" + errMsg);
						throw new Exception(errMsg);
					}
					deviceKey = stmt.getString(4);
				
					// get PID
					pid = stmt.getString( 5 );
					
					if (null == deviceKey || "".equals(deviceKey)) {
						L.error(": DmdScmplus: Device Key does not exist for the given meid");
						throw new Exception("Device Key does not exist for the given meid");
					} else {
						statsLogBuf.append("TRUE|DeviceKey=" + deviceKey);
						statsLogBuf.append(getLoggerData(appType, "", req));
						retMsg = "0||Success||" + deviceKey + "||";
						
						if ( pid != null && pid.length() > 0 )
						{
							statsLogBuf.append("|PID=" + pid );
							retMsg += pid;
						}
						
						LTEProcessor processor = new LTEProcessor();
						processor.getUniversalDeviceInfo(true, true, false, false, false, deviceInfo);
					}

				}
			}
			else if(deviceInfo.getDeviceIdType().equals(DMDConstants.DEVICE_TYPE_IMEI)){
				lookupResultVO.setId(""+device_id.substring(0,14));
				lookupResultVO.setIdType("IMEI");
				// lookup meid
				device_id = device_id.toUpperCase();
				statsLogBuf.append("IMEI_MODELINFO|" + device_id.trim() + "|");
				statsLogBuf.append(getLoggerData(appType, "", req));
				conn = DBUtils.getDbConnection();
				if (conn == null) {
					L.error(": DmdScmplus: NO_DB_CONNECTION");
					throw new Exception("Database Connection could not be established");
				}
				stmt =
					conn.prepareCall(
						//"{call PK_DMD_SCMPLUS.sp_get_device_code_for_meid(?,?,?,?)}");
						"{call PK_DMD_SCMPLUS.sp_get_device_code_imei_pid(?,?,?,?,?)}");
				stmt.registerOutParameter(1, java.sql.Types.INTEGER);
				// err_code           OUT
				stmt.registerOutParameter(2, java.sql.Types.VARCHAR);
				// err_msg            OUT
				stmt.setString(3, device_id.substring(0,14)); // IMEI         IN
				stmt.registerOutParameter(4, java.sql.Types.VARCHAR);
				// device_code            OUT
				stmt.registerOutParameter(5, java.sql.Types.VARCHAR);
				// pid
				stmt.execute();
				String errMsg = stmt.getString(2);
				if (stmt.getInt(1) == 1) {
					throw new EsnNotFoundException("1||IMEI Not found||");
				} else {
					if (stmt.getInt(1) != 0) {
						L.error(": DmdScmplus() : SQL_ERROR :" + errMsg);
						throw new Exception(errMsg);
					}
					deviceKey = stmt.getString(4);
				
					// get PID
					pid = stmt.getString( 5 );
					
					if (null == deviceKey || "".equals(deviceKey)) {
						L.error(": DmdScmplus: Device Key does not exist for the given imei");
						throw new Exception("Device Key does not exist for the given imei");
					} else {
						statsLogBuf.append("TRUE|DeviceKey=" + deviceKey);
						statsLogBuf.append(getLoggerData(appType, "", req));
						retMsg = "0||Success||" + deviceKey + "||";
						
						if ( pid != null && pid.length() > 0 )
						{
							statsLogBuf.append("|PID=" + pid );
							retMsg += pid;
						}
						
						LTEProcessor processor = new LTEProcessor();
						processor.getUniversalDeviceInfo(true, true, false, false, false, deviceInfo);
						String deviceType = deviceInfo.getDeviceType();
						if(deviceType!=null && !deviceType.equals("")){
			            	retMsg = retMsg+"||"+deviceType;
			            }else{
			            	retMsg = retMsg+"||";
			            }
					}

				}

			}
			else{
				statsLogBuf.append("FALSE|ID Not Found");
				statsLogBuf.append(getLoggerData(appType, "", req));
			}
			

            //First check if ESN/MEID exist in our DataBase.
            if(!deviceInfo.getDeviceIdType().equals(DMDConstants.DEVICE_TYPE_IMEI) && 
            		!deviceInfo.getDeviceIdType().equals(DMDConstants.DEVICE_TYPE_ICCID)){
            	OracleDAOFactory oracleDAOFactory = OracleDAOFactory.getInstance();
                OracleEsnLookupDAO daoLookup = (OracleEsnLookupDAO) oracleDAOFactory
                        .createEsnLookupDAO();
            lookupResultVO = daoLookup.locateEsnMeid(lookupResultVO);
            if(!lookupResultVO.isSearchResultStatus()){
            	throw new EsnNotFoundException("1||Device ID Not found||");
            }
            String deviceType=daoLookup.getDeviceMask(lookupResultVO.getProdName(), lookupResultVO.getMfgCode(), lookupResultVO.getEffDate());
            L.debug(": DMDSCMPLUS: deviceType: " + deviceType);
            if(deviceType!=null){
            	retMsg = retMsg+"||"+deviceType;
            }else{
            	retMsg = retMsg+"||";
            }
            }
            
            if(retMsg != null && retMsg.startsWith("0")) {
            	retMsg += "||" + deviceInfo.getDacc();
            }
            //Fortify Fix - Cross-site scripting
            res.getWriter().print(new XSSEncoder().encodeHTML(retMsg));
		} 
		catch (EsnNotFoundException ex) {
			res.getWriter().print(ex.getMessage());
			statsLogBuf.append("FALSE|ID Not Found");
			statsLogBuf.append(getLoggerData(appType, "", req));
		} 
		catch (InvalidInputException ex) {
			res.getWriter().print(ex.getMessage());
			statsLogBuf.append("FALSE|Invalid Input. ESN or MEID must be passed as parameter.");
			statsLogBuf.append(getLoggerData(appType, "", req));
		}
		catch (SQLException ex) {
			res.getWriter().print("2||Database Error Occured||");
			statsLogBuf.append("FALSE|Database Error Occured");
			statsLogBuf.append(getLoggerData(appType, "", req));
		} catch (Exception e) {
			e.printStackTrace();

			if (errorCode == 1) {
				statsLogBuf.append(
					"FALSE|esn or meid must be passed as parameter");
				res.getWriter().print(e.getMessage());
				statsLogBuf.append(getLoggerData(appType, "", req));
			} else {
				//statsLogBuf.append("FALSE|Internal Server Error");
				statsLogBuf.append("FALSE|" + e.getMessage() );
				statsLogBuf.append(getLoggerData(appType, "", req));
				res.getWriter().print("2||" + e.getMessage() + "||");
			}
		} finally {
			closeCallableStatement(stmt);
			closeConnection(conn);
			Date exitTime = new Date();
			DMDLogs.getStatsLogger().info(statsLogBuf.toString());
			long prcTime = exitTime.getTime() - entryTime.getTime();
			DMDLogs.getEStatsLogger().info(
                    statsLogBuf.toString() + "||"
                            + DMDProps.ldf.format(entryTime) + "|"
                            + DMDProps.ldf.format(exitTime) + "|" + prcTime);
		}
	}

	public void closeCallableStatement(CallableStatement stmt) {
		if (stmt != null) {
			try {
				//stmt.clearParameters();
				stmt.close();
				stmt = null;
			} catch (SQLException e) {
				L.error("Error during closing Callable Statement!" + e);
				e.printStackTrace();
			}
		}
	}

	public void closeConnection(Connection conn) {
		String dDB = System.getProperty("DIRECT_DB");
		try {
			if (dDB != null && dDB.trim().equalsIgnoreCase("yes")) {
				// Don't close the db connection
			} else {
				if (conn != null) {
					conn.close();
				}
			}
		} catch (SQLException err) {
			L.error("Error during closing CONNECTION!" + err);
			err.printStackTrace();
		} finally {
			if (conn != null
				&& (dDB == null || !dDB.trim().equalsIgnoreCase("yes")))
				try {
					conn.close();
				} catch (SQLException se) {
					L.error("Error during closing CONNECTION!" + se);
					se.printStackTrace();
				}
		}
	}
	
	private StringBuffer getLoggerData(String appType,String statusMessage,HttpServletRequest req){
		
		StringBuffer statsLogBuf = new StringBuffer();
		
		 statsLogBuf.append(appType)
		.append(DMDConstants.DMD_PIPE)
		.append(DMDUtils.getClientIP(req))
		.append(DMDConstants.DMD_PIPE)
		.append(DMDConstants.LOOKUP)
		.append(DMDConstants.DMD_PIPE)
		.append("HTTP_DMD_SCM_PLUS_API")
		.append(DMDConstants.DMD_PIPE)	 
		.append(DMDConstants.DMD_TRUE)
		.append(DMDConstants.DMD_PIPE)
		.append(statusMessage);	
		 
		 return statsLogBuf;
	}

}
