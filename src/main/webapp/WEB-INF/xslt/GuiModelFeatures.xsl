<?xml version="1.0" encoding="UTF-8"?>
<?xmlspysamplexml C:\bhamu\dmd\newXML\test.xml?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output omit-xml-declaration="yes"/>
	<xsl:template match="/equipment">
		<xsl:variable name="search_status" select="search_status/search_result_status"/>
		<xsl:if test="$search_status = 'true'">
			<td valign="top" width="600">
				<table cellpadding="2" cellspacing="2" width="80%">
					<tr>
						<td align="center" class="verdanaBlue16" bgcolor="#CCCC99">
							<xsl:value-of select="prod_name"/>
						</td>
						<td/>
					</tr>
					<tr>
						<td valign="top" class="verdanaBlack9NB">
							<xsl:value-of select="misc/desc" disable-output-escaping="yes" />
						</td>
						<td/>
					</tr>
					<tr>
						<td width="520">
							<span class="verdanaBlack10"><center>Features</center></span><br/>
							<img>
								<xsl:attribute name="src"><xsl:value-of select="uswin_image_url" disable-output-escaping="yes" /></xsl:attribute>
								<xsl:attribute name="width">520</xsl:attribute>
							</img>				<br/>		
							<table cellpadding="0" cellspacing="0" border="0" bordercolor="#333366" width="100%">
								<tr>
									<td>
										<table cellpadding="0" cellspacing="1" border="0" width="100%">
											<tr>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">EXP NWK</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">IS95</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Mobile Web</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Mobile Messaging</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Mobile IP</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Bluetooth</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">OTA</td>
											</tr>
											<tr>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="supported_technologies/item[@type='EXPRESS_NETWORK']"/>
												</td>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="supported_technologies/item[@type='IS95']"/>
												</td>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="general_features/item[@type='MOBILE_WEB']"/>
												</td>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="general_features/item[@type='MOBILE_MESSAGING']"/>
												</td>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="general_features/item[@type='MOBILE_IP']"/>
												</td>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="general_features/item[@type='BLUETOOTH']"/>
												</td>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="general_features/item[@type='OTA']"/>
												</td>
											</tr>
											<tr>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">E911</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Battery Charger Time</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Picture Caller id</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Voice Activation</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Voice Memo</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Streaming Video</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">OTAPA</td>
											</tr>
											<tr>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="gf_flag"/>
												</td>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="general_features/item[@type='BATTERY_CHARGE_TIME']"/>
												</td>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="general_features/item[@type='PICTURE_CALLER_ID']"/>
												</td>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="general_features/item[@type='VOICE_ACTIVATION']"/>
												</td>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="general_features/item[@type='VOICE_MEMO']"/>
												</td>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="general_features/item[@type='STREAMING_VIDEO']"/>
												</td>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="general_features/item[@type='OTAPA']"/>
												</td>
											</tr>
											<tr>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Push to Talk</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Eqp Mode</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Prod Technology</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Get It Now</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">J2ME</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Spanish</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Global Phone</td>
											</tr>
											<tr>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="general_features/item[@type='PUSH_TO_TALK']"/>
												</td>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="equipment_mode/mode"/>
												</td>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="prod_technology"/>
												</td>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="general_features/item[@type='BREW']"/>
												</td>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="general_features/item[@type='J2ME']"/>
												</td>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="general_features/item[@type='SPANISH']"/>
												</td>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="general_features/item[@type='GLOBAL_PHONE']"/>
												</td>
											</tr>
											<tr>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">VCast</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Music On Demand</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Vision ID</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Camera</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">EMS</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">MMS</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">LBS TRACK</td>
											</tr>
											<tr>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="general_features/item[@type='VCAST']" />
												</td>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="general_features/item[@type='MOD']" />
												</td>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="launch_pkg/vision_prod_id"/>
												</td>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="physical_attributes/item[@type='CAMERA']"/>
												</td>
												<xsl:variable name="fMask" select="featureMaskId"/>
												<xsl:if test="$fMask = ''">
													<td valign="top" align="center" class="verdanaRed9NB">N</td>
													<td valign="top" align="center" class="verdanaRed9NB">N</td>
												</xsl:if>
												<xsl:if test="$fMask = '0'">
													<td valign="top" align="center" class="verdanaRed9NB">N</td>
													<td valign="top" align="center" class="verdanaRed9NB">N</td>
												</xsl:if>
												<xsl:if test="$fMask = '1'">
													<td valign="top" align="center" class="verdanaRed9NB">Y</td>
													<td valign="top" align="center" class="verdanaRed9NB">N</td>
												</xsl:if>
												<xsl:if test="$fMask = '2'">
													<td valign="top" align="center" class="verdanaRed9NB">N</td>
													<td valign="top" align="center" class="verdanaRed9NB">Y</td>
												</xsl:if>
												<xsl:if test="$fMask = '3'">
													<td valign="top" align="center" class="verdanaRed9NB">Y</td>
													<td valign="top" align="center" class="verdanaRed9NB">Y</td>
												</xsl:if>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="general_features/item[@type='LBS_TRACK']"/>
												</td>
											</tr>
											<tr>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">MNAI</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">FOTA</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">FLASHCAST</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">SDM</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">V CAST Store</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">ICS</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Trusted Mode</td>											
											</tr>
											<tr>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="general_features/item[@type='MNAI']"/>
												</td>
												<td valign="top" align="center" class="verdanaRed9NB">
													   <xsl:value-of select="general_features/item[@type='FOTA']"/>
													</td>
												<td valign="top" align="center" class="verdanaRed9NB">
												   <xsl:value-of select="general_features/item[@type='FLASHCAST']"/>
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
											</tr>											
											<tr>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Device Type</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">SMS EvDO</td>
												<!-- <td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Adv Soc Ntwk</td>-->
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">VRD</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Non Geo Device</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">M2M Allow</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">WS FOTA</td>
												<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Preferred SIM</td>
											</tr>
											<tr>
												<td valign="top" align="center" class="verdanaRed9NB">
													<xsl:value-of select="device_type" />
												</td>	
											<!-- 	<td valign="top" align="center" class="verdanaRed9NB"> 
											 	<xsl:value-of select="adv_soc_ntwk" /> 
												</td> -->
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
												<td valign="top" align="center" class="verdanaRed9NB">
															<xsl:value-of select="preferred_sim" />
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
								<xsl:attribute name="src"><xsl:value-of select="uswin_image_url" disable-output-escaping="yes" /></xsl:attribute>
								<xsl:attribute name="width">520</xsl:attribute>
							</img>						
						</td>
						<td valign="center" >
							<img>
								<xsl:attribute name="src"><xsl:value-of select="misc/image[1]"/></xsl:attribute>
								<xsl:attribute name="alt">Device picture not available</xsl:attribute>
								<xsl:attribute name="height">250</xsl:attribute>
								<xsl:attribute name="border">0</xsl:attribute>
							</img>
						</td>
					</tr>
					<tr>
						<td colspan="2"/>
					</tr>
					<xsl:if test="count(ems_features/item) > 0">
						<tr>
							<td width="753">
								<span class="verdanaBlack10">
									<center>EMS Features</center>
								</span>
								<table cellpadding="0" cellspacing="0" border="2" bordercolor="#333366" width="100%">
									<tr>
										<td>
											<table cellpadding="0" cellspacing="1" border="0" width="100%">
												<tr>
													<xsl:for-each select="ems_features/item">
														<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">
															<xsl:value-of select="./@type"/>
														</td>
													</xsl:for-each>
												</tr>
												<tr>
													<xsl:for-each select="ems_features/item">
														<td valign="top" align="center" class="verdanaRed9NB">
															<xsl:value-of select="."/>
														</td>
													</xsl:for-each>
												</tr>
											</table>
										</td>
									</tr>
								</table>
							</td>
							<td/>
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
															<xsl:value-of select="./@type"/>
														</td>
													</xsl:for-each>
												</tr>
												<tr>
													<xsl:for-each select="mms_features/item">
														<td valign="top" align="center" class="verdanaRed9NB">
															<xsl:value-of select="./text()"/>
														</td>
													</xsl:for-each>
												</tr>
											</table>
										</td>
									</tr>
								</table>
							</td>
							<td/>
						</tr>
					</xsl:if>
					<tr>
						<td valign="top" style="height: 30px"/>
					</tr>
					<tr>
						<td> </td>
					</tr>
					<xsl:variable name="idloc" select="search_status/id_location"/>
					<xsl:if test="$idloc = 'ESN_REF'">
						<tr>
							<td colspan="1" align="center"><font color="red"><blink>*This search result came from ESN Range Reference Table</blink></font></td>
						</tr>
					</xsl:if>
					<!-->
					<tr>
						<td colspan="1" align="center">
							<a>
								<xsl:attribute name="href"><xsl:value-of select="misc/link[@type='micc']"/></xsl:attribute>
								<span class="verdanaBlack10">Click here to link to MICC OLR phone page</span>
							</a>
						</td>
					</tr>
					<-->
					<tr>
						<td colspan="1" align="center">
							<a>
								<xsl:attribute name="href"><xsl:value-of select="misc/link[@type='marketing']"/></xsl:attribute>
								<span class="verdanaBlack10">Click here to link to Marketing's equipment site</span>
							</a>
						</td>
					</tr>
				</table>
			</td>
		</xsl:if>
		<xsl:if test="$search_status = 'false'">
			<td valign="top" width="753" height="1000">
				<table cellpadding="0" cellspacing="0" border="0">
					<tr>
						<td>
							<bug>
								<big>
									<big>
										<xsl:value-of select="search_status/status/status_message"/>
									</big>
								</big>
							</bug>
						</td>
					</tr>
					<tr>
						<td valign="top" align="center">
							<img src="images/phone.gif" border="0"/>
						</td>
					</tr>
				</table>
			</td>
		</xsl:if>
		<xsl:if test="$search_status = ''">
		<td valign="top" width="753" height="1000">
		</td>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
