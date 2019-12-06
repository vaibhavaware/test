<?xml version="1.0" encoding="UTF-8"?>
<?xmlspysamplexml C:\bhamu\dmd\newXML\esn_features_gui.xml?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output omit-xml-declaration="yes"/>
	<xsl:template match="/equipment_list">
			<table cellpadding="0" cellspacing="0" border="0"  width="100%">
				<tr><td>
					<table cellpadding="0" cellspacing="0" border="0" bordercolor="#333366"  align="left">
						<tr><td>
							<img>
								<xsl:attribute name="src"><xsl:value-of select="uswin_image_url" disable-output-escaping="yes" /></xsl:attribute>
								<xsl:attribute name="width">620</xsl:attribute>
							</img>				<br/>		
							<table cellpadding="2" cellspacing="2" border="0" width="620">
								<tr bgcolor="black">
									<td valign="top" align="center" width="5%" bgcolor="#CCCCCC" class="verdanaBlue9">No.</td>
									<td valign="top" align="center" width="5%" bgcolor="#CCCCCC" class="verdanaBlue9">MFG</td>
									<td valign="top" align="center" width="5%" bgcolor="#CCCCCC" class="verdanaBlue9">Model</td>
									<td valign="top" align="center" width="5%" bgcolor="#CCCCCC" class="verdanaBlue9">Date</td>
									<td valign="top" align="center" width="5%" bgcolor="#CCCCCC" class="verdanaBlue9">EQP Mode</td>
									<td valign="top" align="center" width="5%" bgcolor="#CCCCCC" class="verdanaBlue9">Express Network</td>
									<td valign="top" align="center" width="5%" bgcolor="#CCCCCC" class="verdanaBlue9">Firmware</td>
								</tr>
								<xsl:for-each select="equipment_summary">
								<tr>
									<td valign="top" width="5%" align="center">
										<a>
											<xsl:attribute name="href">
												DMDGui?modelname=<xsl:value-of select="prod_name"/>
											</xsl:attribute>
											<font color="black"><xsl:value-of select="position()"/></font>
										</a>
									</td>
									<td valign="top" width="5%" align="center">
										<span class="verdanaRed9NB"><xsl:value-of select="manufacturer"/></span>
									</td>
									<td valign="top" width="5%" align="center">
										<span class="verdanaRed9NB"><xsl:value-of select="prod_name"/></span>
									</td>
									<td valign="top" width="5%" align="center">
										<span class="verdanaRed9NB"><xsl:value-of select="effective_date"/></span>
									</td>
									<td valign="top" width="5%" align="center">
										<span class="verdanaRed9NB"><xsl:value-of select="equipment_mode"/></span>
									</td>
									<td valign="top" width="5%" align="center">
										<span class="verdanaRed9NB"><xsl:value-of select="express_network"/></span>
									</td>
									<td valign="top" width="5%" align="center">
										<span class="verdanaRed9NB"><xsl:value-of select="firmware_version"/></span>
									</td>
								</tr>
								</xsl:for-each>
							</table>
							<img>
								<xsl:attribute name="src"><xsl:value-of select="uswin_image_url" disable-output-escaping="yes" /></xsl:attribute>
								<xsl:attribute name="width">620</xsl:attribute>
							</img>				<br/>		
						</td></tr>
					</table>
					<br/>
				</td></tr>
			</table>
	</xsl:template>
</xsl:stylesheet>
