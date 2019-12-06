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
						<xsl:value-of select="misc/desc"  disable-output-escaping="yes" />
					</td>
					<td></td>
				</tr>
				<tr>
					<td width="520">
						<span class="verdanaBlack10"><center>Launch Packages</center></span><br/>
						<img>
							<xsl:attribute name="src"><xsl:value-of select="uswin_image_url" disable-output-escaping="yes" /></xsl:attribute>
							<xsl:attribute name="width">520</xsl:attribute>
						</img>				<br/>		
						<table cellpadding="0" cellspacing="0" border="0" bordercolor="#333366" width="100%">
							<tr>
								<td>
									<table cellpadding="0" cellspacing="1" border="0" width="100%">
										<tr>
											<td valign="middle" align="center"  bgcolor="#CCCCCC" class='verdanaBlue9'>Affected Area</td>
											<td valign="middle" align="center"  bgcolor="#CCCCCC" class='verdanaBlue9'>Network Type</td>
											<td valign="middle" align="center"  bgcolor="#CCCCCC" class='verdanaBlue9'>Positioning</td>
											<td valign="middle" align="center"  bgcolor="#CCCCCC" class='verdanaBlue9'>Warranty</td>
											<td valign="middle" align="center"  bgcolor="#CCCCCC" class='verdanaBlue9'>Vision Prod ID</td>
											<td valign="middle" align="center"  bgcolor="#CCCCCC" class='verdanaBlue9'>Literature Code</td>
										</tr>
										<tr>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="launch_pkg/affected_area"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="launch_pkg/network_type"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="launch_pkg/positioning"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="launch_pkg/warranty"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="launch_pkg/vision_prod_id"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="launch_pkg/literature_code"/>
											</td>
										</tr>
									</table>
								</td>
							</tr>
						</table>
						<img>
							<xsl:attribute name="src"><xsl:value-of select="uswin_image_url" disable-output-escaping="yes" /></xsl:attribute>
							<xsl:attribute name="width">520</xsl:attribute>
						</img>				<br/>		
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
				<tr><td valign='top' style="height: 20px"></td></tr>
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
					<td></td>
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
					<td></td>
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
