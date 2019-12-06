<?xml version="1.0" encoding="UTF-8"?>
<?xmlspysamplexml C:\bhamu\dmd\test.xml?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:template match="/esn_lock">
		<xsl:variable name="trancnt" select="count(transaction_id)"/>
		<DMD>
			<STATUS>
				<STATUS_STR>
					<xsl:value-of select="status/status_str"/>
				</STATUS_STR>
				<MESSAGE>
					<xsl:value-of select="status/status_message"/>
				</MESSAGE>
        			<xsl:if test="$trancnt > 0">
                			<TRANSACTION_ID><xsl:value-of select="transaction_id"/></TRANSACTION_ID>
		        	</xsl:if>
			</STATUS>
		</DMD>
	</xsl:template>
</xsl:stylesheet>
