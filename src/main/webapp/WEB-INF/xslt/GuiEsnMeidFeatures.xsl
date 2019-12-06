<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output omit-xml-declaration="yes" />
	
	
	<xsl:template match="/equipment">
		<script language="JavaScript1.2">
			   function showFirmwareHistory(firmwares)
				{
					myWin = window.open("","nCt", "left=0,width=290,height=100,status=no,toolbar=no,menubar=no,scrollbars=no");
					myWin.document.write ("<pre><table><tr><td><b>Firmware Version</b></td><td><b>Timestamp</b></td></tr></table></pre><br></br>");
					myWin.document.write ("" + firmwares);
					myWin.document.write ("");
					myWin.document.close();
				}
			
				function closeIt(){
					//if (!myWin.closed)
					//myWin.self.close()
				}
        </script>
        <style> 
          .trigger {
			  position:relative;
			  cursor:pointer;
			  body {font: verdanaBlue9;}
		  }
        </style>
        

		<xsl:variable name="search_status" select="search_status/search_result_status" />
		<xsl:if test="$search_status = 'true'">
			<xsl:variable name="desc_cnt" select="count(misc/desc)" />
			<xsl:if test="$desc_cnt > 0">
				<td valign="top" width="753" height="1000">
					<table cellpadding="2" cellspacing="2" width="80%">
						<tr>
							<td align="center" class="verdanaBlue16" colspan="1" bgcolor="#CCCC99">
								<xsl:value-of select="prod_name" />
							</td>
						</tr>
						<tr>
							<td valign="top" width="520" class="verdanaBlack9NB">
								<xsl:value-of select="misc/desc" disable-output-escaping="yes" />
							</td>
							<td></td>
						</tr>
						<tr>
							<td width="538">
								<span class="verdanaBlack10"><center>Features</center></span><br/>
								<img>
								</img><br/>		
								<table cellpadding="0" cellspacing="0" border="0" bordercolor="#333366" width="538">
									<tr>
										<td>
											<table cellpadding="0" cellspacing="1" border="0" width="538">
												<tr>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">EXP NWK</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">IS95</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Mobile Web</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Mobile Messaging</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Mobile IP</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Bluetooth</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">OTA</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">E911</td>
												</tr>
												<tr>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="supported_technologies/item[@type='EXPRESS_NETWORK']" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="supported_technologies/item[@type='IS95']" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="general_features/item[@type='MOBILE_WEB']" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="general_features/item[@type='MOBILE_MESSAGING']" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="general_features/item[@type='MOBILE_IP']" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="general_features/item[@type='BLUETOOTH']" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="general_features/item[@type='OTA']" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="gf_flag" />
													</td>
												</tr>
												<tr>
													
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Camera</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Picture Caller id</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Voice Activation</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Voice Memo</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Streaming Video</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">OTAPA</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Push to Talk</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Eqp Mode</td>
												</tr>
												<tr>
													
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="physical_attributes/item[@type='CAMERA']" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="general_features/item[@type='PICTURE_CALLER_ID']" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="general_features/item[@type='VOICE_ACTIVATION']" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="general_features/item[@type='VOICE_MEMO']" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="general_features/item[@type='STREAMING_VIDEO']" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="general_features/item[@type='OTAPA']" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="general_features/item[@type='PUSH_TO_TALK']" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="equipment_mode/mode" />
													</td>
												</tr>
												<tr>
													
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Prod Technology</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Get It Now</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">J2ME</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Spanish</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Global Phone</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">VCast</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Music On Demand</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">EMS</td>
												</tr>
												<tr>
													
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="prod_technology" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="general_features/item[@type='BREW']" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="general_features/item[@type='J2ME']" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="general_features/item[@type='SPANISH']" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="general_features/item[@type='GLOBAL_PHONE']" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="general_features/item[@type='VCAST']" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="general_features/item[@type='MOD']" />
													</td>
													<xsl:variable name="fMask" select="featureMaskId" />
													<xsl:if test="$fMask = ''">
														<td valign="top" align="center" class="verdanaRed9NB">N</td>
													</xsl:if>
													<xsl:if test="$fMask = '0'">
														<td valign="top" align="center" class="verdanaRed9NB">N</td>
														
													</xsl:if>
													<xsl:if test="$fMask = '1'">
														<td valign="top" align="center" class="verdanaRed9NB">Y</td>
														
													</xsl:if>
													<xsl:if test="$fMask = '2'">
														<td valign="top" align="center" class="verdanaRed9NB">N</td>
														
													</xsl:if>
													<xsl:if test="$fMask = '3'">
														<td valign="top" align="center" class="verdanaRed9NB">Y</td>
														
													</xsl:if>
												</tr>
												<tr>
													
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">MMS</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">LBS TRACK</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">MNAI</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">FOTA</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">FLASHCAST</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">VISION ID</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">PRL Index</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">PRL Name</td>
													</tr>
													
												<tr>
													<xsl:variable name="fMask" select="featureMaskId" />
													<xsl:if test="$fMask = ''">
													<td valign="top" align="center" class="verdanaRed9NB">N</td>
													</xsl:if>
													<xsl:if test="$fMask = '0'">
														
														<td valign="top" align="center" class="verdanaRed9NB">N</td>
													</xsl:if>
													<xsl:if test="$fMask = '1'">
														
														<td valign="top" align="center" class="verdanaRed9NB">N</td>
													</xsl:if>
													<xsl:if test="$fMask = '2'">
														
														<td valign="top" align="center" class="verdanaRed9NB">Y</td>
													</xsl:if>
													<xsl:if test="$fMask = '3'">
														
														<td valign="top" align="center" class="verdanaRed9NB">Y</td>
													</xsl:if>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="general_features/item[@type='LBS_TRACK']" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="general_features/item[@type='MNAI']" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
													   <xsl:value-of select="general_features/item[@type='FOTA']"/>
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
													   <xsl:value-of select="general_features/item[@type='FLASHCAST']"/>
													</td>
												  	<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="launch_pkg/vision_prod_id" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="prl/index" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="prl/name" />
													</td>
													
													
													
													</tr>
														
													<tr>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Billing System</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Last Updated</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">SDM</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">V CAST Store</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">ICS</td>													
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Trusted Mode</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Device Type</td>
													<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Adv Soc Ntwk</td>
													</tr>
													<tr>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="prl/billing_system" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														
														<xsl:value-of select="prl/start_date" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="sdm" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="v_cast_store" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="ics" />
													</td>	
													<td valign="top" align="center" class="verdanaRed9NB">
														<xsl:value-of select="trusted_mode" />
													</td>										
													<td valign="top" align="center" class="verdanaRed9NB">
															<xsl:value-of select="device_type" />
													</td>																															
																										
													<td valign="top" align="center" class="verdanaRed9NB">
															<xsl:value-of select="adv_soc_ntwk" />
														</td>	
														
													</tr>
													<tr>
														<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Prepay Eligibility</td>
														<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Eligibility Date</td>
														<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">PIB Lock Status</td>
														<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">SMS EvDO</td>
														<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">VRD</td>
														<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Non Geo Device</td>
														<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">M2M Allow</td>
														<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">WS FOTA</td>
													</tr>
													<tr>
														<td valign="top" align="center" class="verdanaRed9NB">
															<xsl:value-of select="prepay_eligibility" />
														</td>
														<td valign="top" align="center" class="verdanaRed9NB">
															<xsl:value-of select="eligibility_date" />
														</td>
														<td valign="top" align="center" class="verdanaRed9NB">
															<xsl:value-of select="pib_lock" />
														</td>
														<td valign="top" align="center" class="verdanaRed9NB">
															<xsl:value-of select="sms_evdo" />
														</td>
														<td valign="top" align="center" class="verdanaRed9NB">
															<xsl:value-of select="vrd_capable" />
														</td>
														<td valign="top" align="center" class="verdanaRed9NB">
															<xsl:value-of select="non_geo_device_ind" />
														</td>
														<td valign="top" align="center" class="verdanaRed9NB">
															<xsl:value-of select="m2m_allow" />
														</td>
														<td valign="top" align="center" class="verdanaRed9NB">
															<xsl:value-of select="ws_fota" />
														</td>
													</tr>																																					
													<tr>
														<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Preferred SIM</td>
														<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Wi-Fi Calling</td>
														<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">CDMA_Less</td>
														<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">SIMPN</td>
														<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">IAS</td>
														<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Neg List</td>
														<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">FMD</td>
														<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">LWOMA</td>
													</tr>
													<tr>
														<td valign="top" align="center" class="verdanaRed9NB">
															<xsl:value-of select="preferred_sim" />
														</td>
														<td valign="top" align="center" class="verdanaRed9NB">
															<xsl:value-of select="Wi-Fi_Calling" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
															<xsl:value-of select="cdma_Less" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
															<xsl:value-of select="simpn" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
															<xsl:value-of select="ias" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
															<xsl:value-of select="neg_List" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
															<xsl:value-of select="fmd" />
													</td>
													<td valign="top" align="center" class="verdanaRed9NB">
															<xsl:value-of select="lWOma" />
													</td>
													</tr>
													<tr>
														<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Lte Category</td>														
													</tr>
													<tr>
														<td valign="top" align="center" class="verdanaRed9NB">
															<xsl:value-of select="lteCategory"/>
														</td>														
													</tr>
												</table>
										</td>
									</tr>
								</table>
								<img>
								<!--  	<xsl:attribute name="src"><xsl:value-of select="uswin_image_url" disable-output-escaping="yes" /></xsl:attribute>
									<xsl:attribute name="width">538</xsl:attribute> -->
								</img><br/>		
							</td>
							<td valign="top" rowspan="3">
								<xsl:variable name="img_cnt" select="count(misc/image)" />
								<xsl:if test="$img_cnt > 0">
									<img>
										<xsl:attribute name="src">
											<xsl:value-of select="misc/image[1]" />
										</xsl:attribute>
										<xsl:attribute name="alt">Device picture not available</xsl:attribute>
										<xsl:attribute name="height">250</xsl:attribute>
										<xsl:attribute name="border">0</xsl:attribute>
									</img>
								</xsl:if>
								<xsl:if test="$img_cnt = 0">
									<img src="images/phone.gif" border="0" />
								</xsl:if>
							</td>
						</tr>
						<tr>
							<td colspan="2"></td>
						</tr>

						
						<xsl:if test="count(firmware_history/firmware_record) > 0">
							<tr>
								<td width="538">
								    <br/>
									<span class="verdanaBlack10"><center>Firmware History</center></span>
									<img>
									<!-- 	<xsl:attribute name="src"><xsl:value-of select="uswin_image_url" disable-output-escaping="yes" /></xsl:attribute>
										<xsl:attribute name="width">538</xsl:attribute>   -->
									</img><br/>		
									<table cellpadding="1" cellspacing="0" border="0" bordercolor="#333366" width="100%">
										<tr>
											<td>
												<table rules="all" cellpadding="0" cellspacing="1" frame="border" width="100%">
													<tr>
												  <!--><td valign="middle" align="center" bgcolor="#CCCCCC" class='verdanaBlue9'><a href="javascript:void()" onMouseOver="showFirmwareHistory('1204,1203,1202,1201')" onMouseOut="closeIt()"  onClick="return false">Firmware Version</a></td><-->
												  <td valign="middle" align="center" bgcolor="#CCCCCC" class='verdanaBlue9'>Software Version</td>
												  <td valign="middle" align="center" bgcolor="#CCCCCC" class='verdanaBlue9'>Firmware Revision</td>
												  <td valign="middle" align="center" bgcolor="#CCCCCC" class='verdanaBlue9'>Update Source</td>
												  <td valign="middle" align="center" bgcolor="#CCCCCC" class='verdanaBlue9'>Update Date (EST)</td>
												  </tr>
												  
												  <xsl:for-each select="firmware_history/firmware_record">
													  <tr>
													  	<td valign="top" align="center" class="verdanaRed9NB">
													  		<xsl:for-each select="firmware_version">
																<xsl:value-of select="." /><br/>
															</xsl:for-each>
														</td>
														<td valign="middle" align="center" class="verdanaRed9NB">
															<xsl:value-of select="firmware_revision" />
														</td>
														<td valign="middle" align="center" class="verdanaRed9NB">
															<xsl:value-of select="firmware_source" />
														</td>
														<td valign="middle" align="center" class="verdanaRed9NB">
															<xsl:value-of select="update_date" />
														</td>
													</tr>
												  </xsl:for-each>
											      
													
												</table>
											</td>
										</tr>
									</table>
									<img>
									<!--  	<xsl:attribute name="src"><xsl:value-of select="uswin_image_url" disable-output-escaping="yes" /></xsl:attribute>
										<xsl:attribute name="width">538</xsl:attribute> -->
									</img><br/>		
								</td>
								<td></td>
							</tr>
						</xsl:if>
						<xsl:if test="count(ems_features/item) > 0">
							<tr>
								<td width="538">
								    <br/>
									<span class="verdanaBlack10"><center>EMS Features</center></span>
									<img>
									<!--  	<xsl:attribute name="src"><xsl:value-of select="uswin_image_url" disable-output-escaping="yes" /></xsl:attribute>
										<xsl:attribute name="width">538</xsl:attribute> -->
									</img><br/>		
									<table cellpadding="0" cellspacing="0" border="0" bordercolor="#333366" width="100%">
										<tr>
											<td>
												<table cellpadding="0" cellspacing="1" border="0" width="100%">
													<tr>
														<xsl:for-each select="ems_features/item">
															<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">
																<xsl:value-of select="./@type" />
															</td>
														</xsl:for-each>
													</tr>
													<tr>
														<xsl:for-each select="ems_features/item">
															<td valign="top" align="center" class="verdanaRed9NB">
																<xsl:value-of select="." />
															</td>
														</xsl:for-each>
													</tr>
												</table>
											</td>
										</tr>
									</table>
									<img>
								<!--  	<xsl:attribute name="src"><xsl:value-of select="uswin_image_url" disable-output-escaping="yes" /></xsl:attribute>
										<xsl:attribute name="width">538</xsl:attribute> -->	
									</img><br/>		
								</td>
								<td></td>
							</tr>
							</xsl:if>						
						<xsl:if test="count(mms_features/item) > 0">
							<tr>
								<td width="753">
									<span class="verdanaBlack10">
										<center>MMS Features</center>
									</span>
									<table cellpadding="0" cellspacing="0" border="2" bordercolor="#333366" width="100%">
										<tr>
											<td>
												<table cellpadding="0" cellspacing="1" border="0" width="100%">
													<tr>
														<xsl:for-each select="mms_features/item">
															<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">
																<xsl:value-of select="./@type" />
															</td>
														</xsl:for-each>
													</tr>
													<tr>
														<xsl:for-each select="mms_features/item">
															<td valign="top" align="center" class="verdanaRed9NB">
																<xsl:value-of select="." />
															</td>
														</xsl:for-each>
													</tr>
												</table>
											</td>
										</tr>
									</table>
								</td>
								<td></td>
							</tr>
						</xsl:if>
						<tr>
							<td valign='top' style="height: 30px"></td>
						</tr>
						<tr>
							<td></td>
						</tr>
						<xsl:variable name="idloc" select="search_status/id_location" />
						<xsl:if test="$idloc = 'ESN_REF'">
							<tr>
								<td colspan="1" align="center">
									<font color="red">
										<blink>*This search result came from ESN Range Reference Table</blink>
									</font>
								</td>
							</tr>
						</xsl:if>
						<xsl:if test="$idloc = 'MEID_REF'">
							<tr>
								<td colspan="1" align="center">
									<font color="red">
										<blink>*This search result came from MEID Range Reference Table</blink>
									</font>
								</td>
							</tr>
						</xsl:if>
						<xsl:if test="$idloc = 'ESN_EXCEPTION'">
							<tr>
								<td colspan="1" align="center">
									<font color="red">
										<blink>*This search result came from ESN Exception Table</blink>
									</font>
								</td>
							</tr>
						</xsl:if>
						<xsl:if test="$idloc = 'MEID_EXCEPTION'">
							<tr>
								<td colspan="1" align="center">
									<font color="red">
										<blink>*This search result came from ESN Exception Table</blink>
									</font>
								</td>
							</tr>
						</xsl:if>
						<!-->
						<tr>
							<td colspan='1' align='center'>
								<a>
									<xsl:attribute name="href">
										<xsl:value-of select="misc/link[@type='micc']" />
									</xsl:attribute>
									<span class='verdanaBlack10'>Click here to link to MICC OLR phone page</span>
								</a>
							</td>
							<td></td>
						</tr>
						<-->
						<tr>
							<td colspan='1' align='center'>
								<a>
									<xsl:attribute name="href">
										<xsl:value-of select="misc/link[@type='marketing']" />
									</xsl:attribute>
									<span class='verdanaBlack10'>Click here to link to Marketing's equipment site</span>
								</a>
							</td>
							<td></td>
						</tr>
						
					</table>
				</td>
			</xsl:if>
			<xsl:if test="$desc_cnt = 0">
				<td valign="top" align="center">
					<table cellpadding="0" cellspacing="0" border="0">
						<tr>
							<td>
								<table>
									<tr>
										<td>
											<font size="+2">ESN Range/Exception Search Result: (Detail information unavailable)</font>
										</td>
									</tr>
									<tr>
										<td>
											<font size="+2">
												Make:
												<xsl:value-of select="search_status/model/mfg_name" />
											</font>
										</td>
									</tr>
									<tr>
										<td>
											<font size="+2">
												Model:
												<xsl:value-of select="search_status/model/model_id" />
											</font>
										</td>
									</tr>
									<xsl:variable name="e911" select="gf_flag" />
									<xsl:if test="$e911 != ''">
										<tr>
											<td>
												<font size="+2">
													GPS:
													<xsl:value-of select="$e911" />
												</font>
											</td>
										</tr>
									</xsl:if>
								</table>
							</td>
						</tr>
						<tr>
							<td valign="top" align="center">
								<img src="images/phone.gif" border="0" />
							</td>
						</tr>
					</table>
				</td>
			</xsl:if>
		</xsl:if>
		<xsl:if test="$search_status = 'false'">
			<td valign="top" width="753" height="1000">
				<table cellpadding="0" cellspacing="0" border="0">
					<tr>
						<td>
							<bug>
								<big>
									<big>
										<xsl:value-of select="search_status/status/status_message" />
									</big>
								</big>
							</bug>
						</td>
					</tr>
					<tr>
						<td valign="top" align="center">
							<img src="images/phone.gif" border="0" />
						</td>
					</tr>
				</table>
			</td>
		</xsl:if>
		<xsl:if test="$search_status = ''">
			<td valign="top" width="753" height="1000"></td>
		</xsl:if>

	</xsl:template>
</xsl:stylesheet>

