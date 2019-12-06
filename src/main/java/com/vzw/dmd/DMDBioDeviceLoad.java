package com.vzw.dmd;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.vzw.dmd.dao.BioDeviceLoadDAO;
import com.vzw.dmd.exception.DaoException;
import com.vzw.dmd.util.DMDConstants;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.ILteXmlCreator;
import com.vzw.dmd.util.XXEDisabler;
import com.vzw.dmd.valueobject.BioDeviceBean;



@SuppressWarnings("serial")
public class DMDBioDeviceLoad  extends HttpServlet implements ILteXmlCreator{


	int insertRec;
	private static Logger L = Logger.getLogger(DMDLogs.getLogName(DMDBioDeviceLoad.class));
	/**
	 * @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		System.out.println("Inside Get");
		defaultAction(req, res);
	}

	/**
	 * @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		System.out.println("Inside Get");
		defaultAction(req, res);
	}

	/**
	 * @see javax.servlet.GenericServlet#void ()
	 */
	/*public void init() throws ServletException {
			super.init();
		}*/

	/**
	 * @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void defaultAction(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {

		StringBuffer statsLogBuf = new StringBuffer("DMDBioDeviceLoad").append(DMDConstants.DMD_PIPE);
		Date entryTime = new Date();

		ServletOutputStream out = res.getOutputStream();
		String returnXML = null;
		BioDeviceBean bean=null;
		try{					 					
			String xmlReq = req.getParameter("xmlReq");					
			System.out.println("The request parameter is"+req.getParameter("xmlReq"));
			System.out.println("The request parameter is"+xmlReq);
			if(xmlReq==null || xmlReq.trim().length()==0){
				xmlReq = req.getParameter("xmlreqdoc");
			}
			if(xmlReq != null) {
				xmlReq = xmlReq.trim();
			}
			L.debug("The request xml  is :"+xmlReq);
			//Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xmlReq)));
			//Fortify Fix - XML External Entity Injection
			DocumentBuilderFactory docBuilderFactory = new XXEDisabler().disableDBF(DocumentBuilderFactory.newInstance());
			docBuilderFactory.setNamespaceAware(false);
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse (new InputSource(new StringReader(xmlReq)));
			// normalize text representation
			//doc.getDocumentElement ().normalize ();
			System.out.println ("Root element of the doc is " + 
					doc.getDocumentElement().getNodeName());
			/*String fileId = doc.getDocumentElement().getAttribute("fileID");
	            String fileDate = doc.getDocumentElement().getAttribute("fileDate");
	            String vendorId = doc.getDocumentElement().getAttribute("vendorID");*/

			NodeList listOfContentRec = doc.getElementsByTagName("ContentRec");
			int totalRecords = listOfContentRec.getLength();	           
			System.out.println("Total no of Content records are:"+ totalRecords);


			for(int s=0; s<listOfContentRec.getLength() ; s++){
				bean = new BioDeviceBean();
				String contentId="";
				try {	

					Node firstContentRecNode = listOfContentRec.item(s);
					if(firstContentRecNode.getNodeType() == Node.ELEMENT_NODE){


						Element firstContentRecElement = (Element)firstContentRecNode;
						String recordId = firstContentRecElement.getAttribute("recID");

						L.debug("Content record  id is:"+ recordId);


						//-------	                    	                    	                   
						NodeList contentVendorIdList = firstContentRecElement.getElementsByTagName("ContentVendorID");	                    
						Element contentVendorIdElement = (Element)contentVendorIdList.item(0);
						if (contentVendorIdElement != null){	                    	
							NodeList textcontentVendorIdList = contentVendorIdElement.getChildNodes();
							if(textcontentVendorIdList.item(0) != null){
								String contentVendorId = textcontentVendorIdList.item(0).getNodeValue().trim();
								bean.setContentVendorId(contentVendorId);
								L.debug("Content Vendor id is:"+ contentVendorId);
							}
							else{
								L.debug("Content Vendor id is: EMPTY");
							}

						}
						//-------
						NodeList contentIdList = firstContentRecElement.getElementsByTagName("ContentID");
						Element contentIdElement = (Element)contentIdList.item(0);
						if (contentIdElement != null){
							NodeList textcontentIdList = contentIdElement.getChildNodes();
							if(textcontentIdList.item(0) != null)
							{
								contentId = textcontentIdList.item(0).getNodeValue().trim();
								bean.setContentId(contentId);
								L.debug("Content id is:"+ contentId);
							}
							else{
								returnXML =  createConsolidatedXml("01", "FAILURE", "Failed-Invalid Input-ContentId is not present",bean.getContentId(),returnXML,"01");
								continue;
							}
						}
						else{
							returnXML =  createConsolidatedXml("01", "FAILURE", "Failed-Invalid Input-ContentId is not present",bean.getContentId(),returnXML,"01");
							continue;
						}
						//----
						NodeList contentVendorNameList = firstContentRecElement.getElementsByTagName("ContentVendorName");
						Element contentVendorNameElement = (Element)contentVendorNameList.item(0);
						if (contentVendorNameElement != null){
							NodeList textContentVendorNameList = contentVendorNameElement.getChildNodes();
							if(textContentVendorNameList.item(0) != null){
								String contentVendorName = textContentVendorNameList.item(0).getNodeValue().trim();
								bean.setContentVendorName(contentVendorName);
								L.debug("Content Vendor Name is:"+ contentVendorName);
							}
							else{
								L.debug("Content Vendor Name is: EMPTY");
							}
						}
						//----
						NodeList contentShortNameList = firstContentRecElement.getElementsByTagName("ShortName");
						Element contentShortNameElement = (Element)contentShortNameList.item(0);
						if (contentShortNameElement != null){
							NodeList textcontentShortNameList = contentShortNameElement.getChildNodes();
							if(textcontentShortNameList.item(0) != null){
								String contentShortName = textcontentShortNameList.item(0).getNodeValue().trim();
								bean.setContentShortName(contentShortName);
								L.debug("Content Short Name is:"+ contentShortName);
							}
							else{
								L.debug("Content Short Name is: EMPTY ");
							}
						}
						//----
						NodeList contentLongNameList = firstContentRecElement.getElementsByTagName("LongName");
						Element contentLongNameElement = (Element)contentLongNameList.item(0);
						if (contentLongNameElement != null){
							NodeList textcontentLongNameList = contentLongNameElement.getChildNodes();
							if(textcontentLongNameList.item(0)!=null)
							{
								String contentLongName = textcontentLongNameList.item(0).getNodeValue().trim();
								bean.setContentLongName(contentLongName);
								L.debug("Content Long Name is:"+ contentLongName);
							}
							else
							{
								L.debug("Content Long Name is: EMPTY " );
							}

						}
						//----
						NodeList contentShortDescList = firstContentRecElement.getElementsByTagName("ShortDescription");
						Element contentShortDescElement = (Element)contentShortDescList.item(0);
						if (contentShortDescElement != null){
							NodeList textcontentShortDescList = contentShortDescElement.getChildNodes();
							if(textcontentShortDescList.item(0) != null){
								String contentShortDesc = textcontentShortDescList.item(0).getNodeValue().trim();
								bean.setContentShortDesc(contentShortDesc);
								L.debug("Content Short Desc is:"+ contentShortDesc);
							}
							else{
								L.debug("Content Short Desc is: EMPTY");
							}
						}
						//----
						NodeList contentLongDescList = firstContentRecElement.getElementsByTagName("LongDescription");
						Element contentLongDescElement = (Element)contentLongDescList.item(0);
						if (contentLongDescElement != null){
							NodeList textcontentLongDescList = contentLongDescElement.getChildNodes();
							if(textcontentLongDescList.item(0) != null){
								String contentLongDesc = textcontentLongDescList.item(0).getNodeValue().trim();
								bean.setContentLongDesc(contentLongDesc);
								L.debug("Content Long Desc is:"+ contentLongDesc);
							}
							else{
								L.debug("Content Long Desc is: EMPTY");
							}
						}
						//----
						NodeList contentTypeList = firstContentRecElement.getElementsByTagName("ContentType");
						Element contentTypeElement = (Element)contentTypeList.item(0);
						if (contentTypeElement != null){
							NodeList textcontentTypeList = contentTypeElement.getChildNodes();
							if(textcontentTypeList.item(0) != null){
								String contentType = textcontentTypeList.item(0).getNodeValue().trim();
								bean.setContentType(contentType);
								L.debug("Content Type is:"+ contentType);
							}
							else{
								L.debug("Content Type is: EMPTY");
							}
						}
						//----
						NodeList langCodeList = firstContentRecElement.getElementsByTagName("LanguageCode");
						Element langCodeElement = (Element)langCodeList.item(0);
						if (langCodeElement != null){
							NodeList textlangCodeList = langCodeElement.getChildNodes();
							if(textlangCodeList.item(0) != null){
								String langCode = textlangCodeList.item(0).getNodeValue().trim();
								bean.setLangCode(langCode);
								L.debug("Language Code is:"+ langCode);
							}
							else{
								L.debug("Language Code is: EMPTY");
							}
						}
						//----
						NodeList statusList = firstContentRecElement.getElementsByTagName("Status");
						Element statusElement = (Element)statusList.item(0);
						if (statusElement != null){
							NodeList textstatusList = statusElement.getChildNodes();
							if(textstatusList.item(0) != null){
								String status = textstatusList.item(0).getNodeValue().trim();
								bean.setStatus(status);
								L.debug("Content Status is:"+ status);
							}
							else{
								L.debug("Content Status is:EMPTY ");
							}
						}
						//commented the else part as status is not a mandatory field 
						/*else {
	                    	returnXML =  createConsolidatedXml("01", "FAILURE", "Failed-Invalid Input-status is not present",bean.getContentId(),returnXML,"01");
	                    	continue;
	                    }*/
						//----
						NodeList isPurchasableList = firstContentRecElement.getElementsByTagName("IsPurchasable");
						Element isPurchasableElement = (Element)isPurchasableList.item(0);
						if (isPurchasableElement != null){
							NodeList textisPurchasableList = isPurchasableElement.getChildNodes();
						if(textisPurchasableList.item(0) != null){
							String isPurchasable = textisPurchasableList.item(0).getNodeValue().trim();
								if ( isPurchasable.equalsIgnoreCase("true")){
									isPurchasable = "Y";
								}
								else 
									isPurchasable = "N";
								bean.setIsPurchasable(isPurchasable);
								L.debug(" is Purchasable :"+ isPurchasable);
							}
							else{
								L.debug(" is Purchasable : EMPTY ");
							}


						}
						//----
						NodeList collectionTypeList = firstContentRecElement.getElementsByTagName("CollectionType");
						Element collectionTypeElement = (Element)collectionTypeList.item(0);
						if (collectionTypeElement != null){
							NodeList textcollectionTypeList = collectionTypeElement.getChildNodes();
							if(textcollectionTypeList.item(0) != null){
								String collectionType = textcollectionTypeList.item(0).getNodeValue().trim();
								bean.setCollectionType(collectionType);
								L.debug("Collection Type is:"+ collectionType);
							}
							else{
								L.debug("Collection Type is: EMPTY");
							}
						}
						//----
						NodeList isWirelessEnabledList = firstContentRecElement.getElementsByTagName("IsWirelessEnabled");
						Element isWirelessEnabledElement = (Element)isWirelessEnabledList.item(0);
						if (isWirelessEnabledElement != null){
							NodeList textisWirelessEnabledList = isWirelessEnabledElement.getChildNodes();
							if(textisWirelessEnabledList.item(0) != null){
								String isWirelessEnabled = textisWirelessEnabledList.item(0).getNodeValue().trim();
								bean.setIsWirelessEnabled(isWirelessEnabled);
								L.debug("Wireless Enabled is:"+ isWirelessEnabled);
							}
							else{
								L.debug("Wireless Enabled is: Empty");
							}
						}
						//commented the else part as IsWirelessEnabled is not a mandatory field
						/*else{
	                    	returnXML =  createConsolidatedXml("01", "FAILURE", "Failed-Invalid Input-isWirelessEnabled is not present",bean.getContentId(),returnXML,"01");
	                    	continue;
	                    }*/
						//----
						NodeList wirelessContentIdList = firstContentRecElement.getElementsByTagName("WirelessContentID");
						Element wirelessContentIdElement = (Element)wirelessContentIdList.item(0);
						if (wirelessContentIdElement != null){
							NodeList textwirelessContentIdList = wirelessContentIdElement.getChildNodes();
							if(textwirelessContentIdList.item(0) != null){
								String wirelessContentId = textwirelessContentIdList.item(0).getNodeValue().trim();
								bean.setWirelessContentId(wirelessContentId);
								L.debug(" Wireless device sku is:"+ wirelessContentId);
							}
							else{
								L.debug(" Wireless device sku is: EMPTY");
							}

						}
						//commented the else part as WirelessContentID is not a mandatory field
						/*else{
	                    	returnXML =  createConsolidatedXml("01", "FAILURE", "Failed-Invalid Input-wirelessContentId is not present",bean.getContentId(),returnXML,"01");
	                    	continue;
	                    }*/
						insertRec = BioDeviceLoadDAO.insertBioDeviceRec(bean,xmlReq);


						if (insertRec == 1){
							//if(s > 0){
							returnXML = createConsolidatedXml("00","SUCCESS","SUCCESS",contentId,returnXML,"00");
							//}
							//returnXML = createConsolidatedXml("00","SUCCESS","SUCCESS",contentId,null,"00");	                    		
						}	                    
						/*else 
	                    	returnXML = createConsolidatedXml("01","Failure","SQL_ERROR");*/	 
					}//end of if clause





				}catch (DaoException daoEx){
					L.error("Unable to process request.", daoEx);

					statsLogBuf.append("STATUS=Failure");
					statsLogBuf.append(DMDConstants.DMD_PIPE);
					returnXML =  createConsolidatedXml("00", "SUCCESS", "Failed-"+daoEx.getMessage(),bean.getContentId(),returnXML,"01");
				}


				catch (Exception e){
					L.error("Unable to process request.", e);

					statsLogBuf.append("STATUS=Failure");
					statsLogBuf.append(DMDConstants.DMD_PIPE);                              
					returnXML =  createConsolidatedXml("00", "SUCCESS", "Failed-Invalid Input",bean.getContentId(),returnXML,"01");
				}
			}
		}//end of for loop
		catch (Exception e){
			L.error("Unable to process request.", e);

			statsLogBuf.append("STATUS=Failure");
			statsLogBuf.append(DMDConstants.DMD_PIPE);
			returnXML =  createConsolidatedXml("01", "Failure",null,null,null,null);
		}finally{
			Date exitTime = new Date();
			DMDLogs.getStatsLogger().info(statsLogBuf.toString());
			long prcTime = exitTime.getTime() - entryTime.getTime();
			DMDLogs.getEStatsLogger().info(
					statsLogBuf.toString() + DMDConstants.DMD_PIPE                    
					+ DMDProps.ldf.format(entryTime) + DMDConstants.DMD_PIPE
					+ DMDProps.ldf.format(exitTime) + DMDConstants.DMD_PIPE + prcTime);
		}
		out.println(returnXML);
		out.flush();
		out.close();

	}

	private String createConsolidatedXml(String statusCd, String msg, String body,String cId,String responseXML,String cStatusCd) {
		if (responseXML != null && responseXML.indexOf("</ContentRec>") > 0){				
			int index = responseXML.indexOf("</ContentRec>");
			//if (index >0){
			String str = "<ContentRec><ContentID>"+cId+"</ContentID><statusCode>"+cStatusCd+"</statusCode><message>"+body+"</message></ContentRec>";
			StringBuffer sbReturnXML=new StringBuffer(responseXML);
			sbReturnXML.insert(index+13,str);
			return sbReturnXML.toString();
			//}								
		}
		StringBuffer sbReturnXML=new StringBuffer(XML_RESPONSE_START);
		//add header. status and message
		sbReturnXML.append(statusCd)
		.append(STATUS_CODE_END)
		.append(MESSAGE_START)
		.append(msg)
		.append(MESSAGE_END)
		.append(RESPONSE_HEADER_END);
		//add body					
		sbReturnXML.append(RESPONSE_BODY_START);
		if(body != null || cId != null) {
			sbReturnXML.append(CONTENT_REC_START)
			.append(CONTENT_ID_START)
			.append(cId)
			.append(CONTENT_ID_END)
			.append(STATUS_CODE_START)
			.append(cStatusCd)
			.append(STATUS_CODE_END)
			.append(MESSAGE_START)
			.append(body)
			.append(MESSAGE_END)
			.append(CONTENT_REC_END);
			//sbReturnXML.append(body);
		}
		sbReturnXML.append(RESPONSE_BODY_END).append(DMD_END);

		L.debug("The response is :"+sbReturnXML);

		return sbReturnXML.toString();

	}
}

