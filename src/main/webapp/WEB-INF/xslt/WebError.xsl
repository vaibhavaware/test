<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:template match="/equipment">
        	<xsl:variable name="trancnt" select="count(transaction_id)"/>
		<DMD>
			<STATUS>
				<STATUS_STR>
					<xsl:value-of select="search_status/status/status_str"/>
				</STATUS_STR>
				<MESSAGE>
					<xsl:value-of select="search_status/status/status_message"/>
				</MESSAGE>
			</STATUS>
			<xsl:if test="$trancnt > 0">
       				<TRANSACTION_ID><xsl:value-of select="transaction_id"/></TRANSACTION_ID>
			</xsl:if>
		</DMD>
	</xsl:template>
</xsl:stylesheet>