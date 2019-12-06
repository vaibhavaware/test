<?xml version="1.0" encoding="UTF-8"?>
<?xmlspysamplexml C:\bhamu\dmd\newXML\esn_features_gui.xml?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output omit-xml-declaration="yes"/>
	<xsl:template match="/equipment">
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
	</xsl:template>
</xsl:stylesheet>
