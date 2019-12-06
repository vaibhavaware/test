<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output omit-xml-declaration="yes"/>
	<xsl:template match="/equipment">
		<xsl:variable name="search_status" select="search_status/search_result_status"/>
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
					<td valign="top" width="700" class="verdanaBlack9NB">
						<xsl:value-of select="misc/desc" disable-output-escaping="yes" />
					</td>
					<td></td>
				</tr>
				<tr>
					<td width="520">
						<span class="verdanaBlack10"><center>Physical Attributes</center></span><br/>
						<img>
							<xsl:attribute name="src"><xsl:value-of select="uswin_image_url" disable-output-escaping="yes" /></xsl:attribute>
							<xsl:attribute name="width">520</xsl:attribute>
						</img>				<br/>		
						<table cellpadding="0" cellspacing="0" border="0" bordercolor="#333366" width="100%">
							<tr>
								<td>
									<table cellpadding="0" cellspacing="1" border="0" width="100%">
										<tr>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Product Type</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Product Tier</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Display Size</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Network Speed</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Phone Book Entries</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Battery Type</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Talk Time(hour)</td>
										</tr>
										<tr>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="prod_type"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="prod_tier"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='DISPLAY_SIZE']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="network/speed"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='PHONE_BOOK_ENTRIES']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='BATTERY_TYPE']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='TALK_TIME']"/>
											</td>
										</tr>
										<tr>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Standby Time IS95(hour)</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Standby Time 1X(hour)</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Form Factor Style</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Dual LCD</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Antenna Type</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Weight</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Phone Size</td>
										</tr>
										<tr>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='STANDBY_TIME_IS95']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='STANDBY_TIME_1X']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='FORM_FACTOR_STYLE']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='DUAL_LCD']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='ANTENNA_TYPE']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='WEIGHT']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='PHONE_SIZE']"/>
											</td>
										</tr>
										<tr>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Disply Type</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Display Technology</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Num Display Color</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">SRAM Size</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Flash Size</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Speaker Type</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Vibra</td>
										</tr>
										<tr>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='DISPLAY_TYPE']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='DISPLAY_TECHNOLOGY']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='NUM_DISPLAY_COLOR']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='SRAM_SIZE']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='FLASH_SIZE']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='SPEAKER_TYPE']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='VIBRA']"/>
											</td>
										</tr>
										<tr>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Navigation Key</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Connector</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">R UIM</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Backlight</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Camera</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Section 255</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">MIL Standard</td>
										</tr>
										<tr>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='NAVIGATION_KEY']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='CONNECTER']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='R_UIM']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='BACKLIGHT']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='CAMERA']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='SEC255']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='MIL_STANDARD']"/>
											</td>
										</tr>
										<tr>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">TTY</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Preload Ringtones</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Total Ringtones</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Preload Games</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Total Games</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Display Resolution</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Display Dimension</td>
										</tr>
										<tr>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='TTY']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='PRELOAD_RINGTONES']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='TOTAL_RINGTONES']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='PRELOAD_GAMES']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='TOTAL_GAMES']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='DISPLAY_RESOLUTION']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='DISPLAY_DIMENSION']"/>
											</td>
										</tr>
										<tr>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">SD Card Slot</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Infrared Port</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">MP3 Player</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Data Speed</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Headset Jack</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">PC Card</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">FM Stereo Radio</td>
										</tr>
										<tr>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='SD_CARD_SLOT']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='INFRARED_PORT']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='MP3_PLAYER']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='DATA_SPEED']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='HEADSET_JACK']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='PC_CARD']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='FM_STEREO_RADIO']"/>
											</td>
										</tr>
										<tr>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Connectivity Kit</td>
											<td valign="middle" align="center" bgcolor="#CCCCCC" class="verdanaBlue9">Modem 1X</td>
										</tr>
										<tr>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='CONNECTIVITY_KIT']"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="physical_attributes/item[@type='MODEM_1X']"/>
											</td>
										</tr>
									</table>
								</td>
							</tr>
						</table>
						<img>
							<xsl:attribute name="src"><xsl:value-of select="uswin_image_url" disable-output-escaping="yes" /></xsl:attribute>
							<xsl:attribute name="width">520</xsl:attribute>
						</img><br/>		
					</td>
					<td valign="center" rowspan="3">
						<xsl:variable name="img_cnt" select="count(misc/image)"/>
						<xsl:if test="$img_cnt > 0">
						<img>
							<xsl:attribute name="src"><xsl:value-of select="misc/image[1]" /></xsl:attribute>
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
				<tr><td valign='top' style="height: 30px"></td></tr>
				<tr><td> </td></tr>
				<xsl:variable name="idloc" select="search_status/id_location"/>
				<xsl:if test="$idloc = 'ESN_REF'">
					<tr>
						<td colspan="1" align="center"><font color="red"><blink>*This search result came from ESN Range Reference Table</blink></font></td>
					</tr>
				</xsl:if>
				<xsl:if test="$idloc = 'MEID_REF'">
					<tr>
						<td colspan="1" align="center"><font color="red"><blink>*This search result came from MEID Range Reference Table</blink></font></td>
					</tr>
				</xsl:if>
				<xsl:if test="$idloc = 'ESN_EXCEPTION'">
					<tr>
						<td colspan="1" align="center"><font color="red"><blink>*This search result came from ESN Exception Table</blink></font></td>
					</tr>
				</xsl:if>
				<xsl:if test="$idloc = 'MEID_EXCEPTION'">
					<tr>
						<td colspan="1" align="center"><font color="red"><blink>*This search result came from ESN Exception Table</blink></font></td>
					</tr>
				</xsl:if>
				<!-->
				<tr>
					<td colspan='1' align='center'>
						<a>
							<xsl:attribute name="href">
								<xsl:value-of select="misc/link[@type='micc']"/>
							</xsl:attribute>
							<span class= 'verdanaBlack10'>Click here to link to MICC OLR phone page</span>
						</a>
					</td>
				</tr>
				<-->
				<tr>
					<td colspan='1' align='center'>
						<a>
							<xsl:attribute name="href">
								<xsl:value-of select="misc/link[@type='marketing']"/>
							</xsl:attribute>
							<span class= 'verdanaBlack10'>Click here to link to Marketing's equipment site</span>
						</a>
					</td>
				</tr>
			</table>
		</td>
		</xsl:if>
		<xsl:if test="$desc_cnt = 0">
		<td  valign="top" align="center">
			<table cellpadding="0" cellspacing="0" border="0">
				<tr><td>
					<table>
						<tr><td><font size="+2">ESN Range/Exception Search Result: (Detail information unavailable)</font></td></tr>
						<tr><td><font size="+2">Make: <xsl:value-of select="search_status/model/mfg_name"/></font></td></tr>
						<tr><td><font size="+2">Model: <xsl:value-of select="search_status/model/model_id"/></font></td></tr>
						<xsl:variable name="e911" select="gf_flag"/>
						<xsl:if test="$e911 != ''">
						<tr><td><font size="+2">GPS: <xsl:value-of select="$e911"/></font></td></tr>
						</xsl:if>
					</table>
				</td></tr>
				<tr><td valign="top" align="center"><img src="images/phone.gif" border="0" /></td></tr>
			</table>
		</td>
		</xsl:if>
		</xsl:if>
		<xsl:if test="$search_status = 'false'">
		<td valign="top" width="753" height="1000">
			<table cellpadding="0" cellspacing="0" border="0">
				<tr>
					<td><bug><big><big>
						<xsl:value-of select="search_status/status/status_message"/>
					</big></big></bug></td>
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
