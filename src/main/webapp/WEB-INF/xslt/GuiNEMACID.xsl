<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output omit-xml-declaration="yes"/>
	<xsl:template match="NEMACID">
		<xsl:variable name="search_status" select="SearchStatus"/>
		<xsl:if test="$search_status = 'true'">
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
					<td width="753">
						<span class="verdanaBlack10">
							<center>Network Extender</center>
						</span>
						<table cellpadding="0" cellspacing="0" border="2" bordercolor="#333366" width="100%">
							<tr>
								<td>
									<table cellpadding="0" cellspacing="1" border="0" width="90%">
										<tr>
											<td valign="middle" align="center"  bgcolor="#CCCCCC" class='verdanaBlue9'>Model ID</td>
											<td valign="middle" align="center"  bgcolor="#CCCCCC" class='verdanaBlue9'>Manufacturer Code</td>
											<td valign="middle" align="center"  bgcolor="#CCCCCC" class='verdanaBlue9'>Ship Date</td>
										</tr>
										<tr>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="model_id"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="mfg_code"/>
											</td>
											<td valign="top" align="center" class="verdanaRed9NB">
												<xsl:value-of select="effective_date"/>
											</td>
										</tr>
									</table>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
		</td>
		</xsl:if>
		<xsl:if test="$search_status = 'false'">
		<td valign="top" width="753" height="1000">
			<table cellpadding="0" cellspacing="0" border="0">
				<tr>
					<td><bug><big><big>
						<xsl:value-of select="status_message"/>
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

	</xsl:template>
</xsl:stylesheet>
