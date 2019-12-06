package com.vzw.dmd;

import java.util.ArrayList;

import com.vzw.dmd.util.DMDConstants;
import com.vzw.dmd.util.ILteXmlCreator;
import com.vzw.dmd.valueobject.DeviceSimInfo;
import com.vzw.dmd.valueobject.MultiDeviceVO;

public class Test implements ILteXmlCreator {
public static void main(String[] args) {
	MultiDeviceVO vo=null;
	StringBuffer sbReturnXML=new StringBuffer(XML_RESPONSE_START);
	//add header. status and message
	sbReturnXML.append("200")
				.append(STATUS_CODE_END)
				.append(MESSAGE_START)
				.append("SUCCESS")
				.append(MESSAGE_END)
				.append(RESPONSE_HEADER_END);
	//add body					
	sbReturnXML.append(RESPONSE_BODY_START);		
	ArrayList<DeviceSimInfo> list= vo.getDeviceSimInfoList();
	for(int i=0; i<list.size();i++){
		DeviceSimInfo deviceSimInfo = list.get(i);
		sbReturnXML.append(DEVICE_SIM_INFO_START)
					.append(DEVICE_FOUND_START)
					.append(deviceSimInfo.isDeviceFound()?DMDConstants.DMD_Y:DMDConstants.DMD_N)
					.append(DEVICE_FOUND_END)
					.append(SIM_FOUND_START)
					.append(deviceSimInfo.isSimFound()?DMDConstants.DMD_Y:DMDConstants.DMD_N)
					.append(SIM_FOUND_END)
					.append(DEVICE_INFO_START);
		if(deviceSimInfo.getDeviceInfo()!=null){
			if(deviceSimInfo.isDeviceFound()){
				sbReturnXML.append(DEVICE_ID_START)
							.append(deviceSimInfo.getDeviceInfo().getDeviceId())
							.append(DEVICE_ID_END)
							.append(MFG_CODE_START)
							.append(deviceSimInfo.getDeviceInfo().getMfgCode())
							.append(MFG_CODE_END)
							.append(PROD_NAME_START)
							.append(deviceSimInfo.getDeviceInfo().getProdName())
							.append(PROD_NAME_END)
							.append(DACC_START)
							.append(deviceSimInfo.getDeviceInfo().getDacc())
							.append(DACC_END)
							.append(DEVICE_TYPE_START)
							.append(deviceSimInfo.getDeviceInfo().getDeviceType())
							.append(DEVICE_TYPE_END)
							.append(GLOBAL_PHONE_START)
							.append(deviceSimInfo.getDeviceInfo().getGlobalPhone())
							.append(GLOBAL_PHONE_END)
							.append(PREFERRED_SIM_START)
							.append(deviceSimInfo.getDeviceInfo().getPreferredSim())	
							.append(PREFERRED_SIM_END);
							
							/*.append(ICCID_STATUS_START)
							.append(deviceSimInfo.getDeviceInfo().getIccidStatus())	
							.append(ICCID_STATUS_END);*/
											
			}else{
				sbReturnXML.append(DEVICE_ID_START)
							.append(deviceSimInfo.getDeviceInfo().getDeviceId())
							.append(DEVICE_ID_END)
							.append(MFG_CODE_START)				
							.append(MFG_CODE_END)
							.append(PROD_NAME_START)				
							.append(PROD_NAME_END)
							.append(DACC_START)				
							.append(DACC_END)
							.append(DEVICE_TYPE_START)				
							.append(DEVICE_TYPE_END)
							.append(GLOBAL_PHONE_START)
							.append(GLOBAL_PHONE_END);
							/*.append(ICCID_STATUS_START)
							.append(ICCID_STATUS_END);*/
							
			}
		}
		sbReturnXML.append(DEVICE_INFO_END)
					.append(SIM_INFO_START);
		if(deviceSimInfo.getSimInfo()!=null){
			if(deviceSimInfo.isSimFound()){
				sbReturnXML.append(SIM_ID_START)
							.append(deviceSimInfo.getSimInfo().getDeviceId())
							.append(SIM_ID_END)
							.append(MFG_CODE_START)
							.append(deviceSimInfo.getSimInfo().getMfgCode())
							.append(MFG_CODE_END)
							.append(PROD_NAME_START)
							.append(deviceSimInfo.getSimInfo().getProdName())
							.append(PROD_NAME_END)
							.append(SACC_START)
							.append(deviceSimInfo.getSimInfo().getDacc())
							.append(SACC_END)
							.append(SIM_TYPE_START)
							.append(deviceSimInfo.getSimInfo().getDeviceType())
							.append(SIM_TYPE_END)
							.append(ICCID_STATUS_START)
							.append(deviceSimInfo.getSimInfo().getIccidStatus())	
							.append(ICCID_STATUS_END)
							.append(SIMGROUP_START)
							.append(deviceSimInfo.getSimInfo().getSimGroup())	
							.append(SIMGROUP_END);
							
							
			}else{
				sbReturnXML.append(SIM_ID_START)
							.append(deviceSimInfo.getSimInfo().getDeviceId())
							.append(SIM_ID_END)
							.append(MFG_CODE_START)
							.append(MFG_CODE_END)
							.append(PROD_NAME_START)
							.append(PROD_NAME_END)
							.append(SACC_START)
							.append(SACC_END)
							.append(SIM_TYPE_START)
							.append(SIM_TYPE_END)
							.append(ICCID_STATUS_START)
							.append(ICCID_STATUS_END);
						
			}
		}
		sbReturnXML.append(SIM_INFO_END);
		//Compatibility
		if(deviceSimInfo.isDeviceFound() && deviceSimInfo.isSimFound()){
			sbReturnXML.append(DEVICE_SIM_COMPATIBLE_START)
						.append(deviceSimInfo.isDeviceSimCompatible()?DMDConstants.DMD_N:DMDConstants.DMD_Y)
						.append(DEVICE_SIM_COMPATIBLE_END);
		}else{
			sbReturnXML.append(DEVICE_SIM_COMPATIBLE_START)
						.append(DEVICE_SIM_COMPATIBLE_END);
		}
		sbReturnXML.append(DEVICE_SIM_INFO_END);
	}
	sbReturnXML.append(RESPONSE_BODY_END)
				.append(DMD_END);
	System.out.println(sbReturnXML.toString());
	//return sbReturnXML.toString();
 
}
}
