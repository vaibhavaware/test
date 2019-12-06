<?xml version="1.0" encoding="UTF-8"?>
<?xmlspysamplexml C:\bhamu\dmd\test.xml?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:template match="/equipment">
		<xsl:variable name="search_status" select="search_status/search_result_status"/>
		<xsl:variable name="modelloc" select="search_status/model_location"/>
		<xsl:variable name="esn" select="id/esn/dec"/>
		<xsl:variable name="meid" select="id/meid/hex"/>
		<xsl:variable name="trancnt" select="count(transaction_id)"/>
		<xsl:if test="$search_status = 'true'">
		<xsl:if test="$modelloc != 'MODEL_PRODUCT'">
			<xsl:if test="$modelloc = 'MODEL_REF'">
				<DMD xmlns="http://www.vzw.com/namespace/scm" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
				   <STATUS>
				      <STATUS_STR>Normal</STATUS_STR>
				      <MESSAGE></MESSAGE>
				   </STATUS>
        			<xsl:if test="$trancnt > 0">
                			<TRANSACTION_ID><xsl:value-of select="transaction_id"/></TRANSACTION_ID>
		        	</xsl:if>
				   <EQUIPMENT_ESN>
				      <ESN><xsl:value-of select="$esn"/></ESN>
				   </EQUIPMENT_ESN>
				   <EQUIPMENT_MODEL>
				      <PROD_NAME>Unknown</PROD_NAME>
				      <MFG_CODE>UNK</MFG_CODE>
				      <EFFECTIVE_DATE>2004/01/01</EFFECTIVE_DATE>
				   </EQUIPMENT_MODEL>
				</DMD>		
			</xsl:if>
			
			<xsl:if test="$modelloc = 'MEID_MODEL_REF'">
				<DMD xmlns="http://www.vzw.com/namespace/scm" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
				   <STATUS>
				      <STATUS_STR>Normal</STATUS_STR>
				      <MESSAGE></MESSAGE>
				   </STATUS>
				<xsl:if test="$trancnt > 0">
        				<TRANSACTION_ID><xsl:value-of select="transaction_id"/></TRANSACTION_ID>
				</xsl:if>
				   <EQUIPMENT_MEID>
				      <MEID><xsl:value-of select="$meid"/></MEID>
				   </EQUIPMENT_MEID>
				   <EQUIPMENT_MODEL>
				      <PROD_NAME>Unknown</PROD_NAME>
				      <MFG_CODE>UNK</MFG_CODE>
				      <EFFECTIVE_DATE>2004/01/01</EFFECTIVE_DATE>
				   </EQUIPMENT_MODEL>
				</DMD>		
			</xsl:if>
			<xsl:if test="$modelloc = 'ESN_EXCEPTION'">
				<DMD xmlns="http://www.vzw.com/namespace/scm" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
				   <STATUS>
				      <STATUS_STR>Normal</STATUS_STR>
				      <MESSAGE></MESSAGE>
				   </STATUS>
				<xsl:if test="$trancnt > 0">
        				<TRANSACTION_ID><xsl:value-of select="transaction_id"/></TRANSACTION_ID>
				</xsl:if>
				   <EQUIPMENT_ESN>
				      <ESN><xsl:value-of select="$esn"/></ESN>
				   </EQUIPMENT_ESN>
				   <EQUIPMENT_MODEL>
				      <PROD_NAME>Unknown</PROD_NAME>
				      <MFG_CODE>UNK</MFG_CODE>
				      <EFFECTIVE_DATE>2004/01/01</EFFECTIVE_DATE>
				   </EQUIPMENT_MODEL>
				</DMD>		
			</xsl:if>
			<xsl:if test="$modelloc = 'MEID_EXCEPTION'">
				<DMD xmlns="http://www.vzw.com/namespace/scm" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
				   <STATUS>
				      <STATUS_STR>Normal</STATUS_STR>
				      <MESSAGE></MESSAGE>
				   </STATUS>
				<xsl:if test="$trancnt > 0">
        				<TRANSACTION_ID><xsl:value-of select="transaction_id"/></TRANSACTION_ID>
				</xsl:if>
				   <EQUIPMENT_MEID>
				      <MEID><xsl:value-of select="$meid"/></MEID>
				   </EQUIPMENT_MEID>
				   <EQUIPMENT_MODEL>
				      <PROD_NAME>Unknown</PROD_NAME>
				      <MFG_CODE>UNK</MFG_CODE>
				      <EFFECTIVE_DATE>2004/01/01</EFFECTIVE_DATE>
				   </EQUIPMENT_MODEL>
				</DMD>		
			</xsl:if>
			<xsl:if test="$modelloc = 'NOT_FOUND'">
				<DMD xmlns="http://www.vzw.com/namespace/scm" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
				   <STATUS>
				      <STATUS_STR>Normal</STATUS_STR>
				      <MESSAGE></MESSAGE>
				   </STATUS>
				<xsl:if test="$trancnt > 0">
        				<TRANSACTION_ID><xsl:value-of select="transaction_id"/></TRANSACTION_ID>
				</xsl:if>
				   <EQUIPMENT_ESN>
				      <ESN><xsl:value-of select="$esn"/></ESN>
				   </EQUIPMENT_ESN>
				   <EQUIPMENT_MODEL>
				      <PROD_NAME>Unknown</PROD_NAME>
				      <MFG_CODE>UNK</MFG_CODE>
				      <EFFECTIVE_DATE>2004/01/01</EFFECTIVE_DATE>
				   </EQUIPMENT_MODEL>
				</DMD>		
			</xsl:if>
			<xsl:if test="$modelloc = ''">
			<DMD xmlns="http://www.vzw.com/namespace/scm" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
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
				<xsl:if test="$esn != ''">
				<EQUIPMENT_ESN>
					<ESN>
						<xsl:value-of select="id/esn/dec"/>
					</ESN>
				</EQUIPMENT_ESN>
				</xsl:if>
				<xsl:if test="$meid != ''">
				<EQUIPMENT_MEID>
					<MEID>
						<xsl:value-of select="id/meid/hex"/>
					</MEID>
				</EQUIPMENT_MEID>
				</xsl:if>
				<EQUIPMENT_MODEL>
					<PROD_NAME>
						<xsl:value-of select="prod_name"/>
					</PROD_NAME>
					<MFG_CODE>
						<xsl:value-of select="manufacturer/code"/>
					</MFG_CODE>
					<EFFECTIVE_DATE>
						<xsl:value-of select="effective_date"/>
					</EFFECTIVE_DATE>
				</EQUIPMENT_MODEL>
			</DMD>
			</xsl:if>
		</xsl:if>
		<xsl:if test="$modelloc = 'MODEL_PRODUCT'">
		<DMD xmlns="http://www.vzw.com/namespace/scm" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
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
			<xsl:if test="$esn != ''">
			<EQUIPMENT_ESN>
				<ESN>
					<xsl:value-of select="id/esn/dec"/>
				</ESN>
			</EQUIPMENT_ESN>
			</xsl:if>
			<xsl:if test="$meid != ''">
			<EQUIPMENT_MEID>
				<MEID>
					<xsl:value-of select="id/meid/hex"/>
				</MEID>
			</EQUIPMENT_MEID>
			</xsl:if>
			<EQUIPMENT_MODEL>
				<PROD_NAME>
					<xsl:value-of select="prod_name"/>
				</PROD_NAME>
				<MFG_CODE>
					<xsl:value-of select="manufacturer/code"/>
				</MFG_CODE>
				<EFFECTIVE_DATE>
					<xsl:value-of select="effective_date"/>
				</EFFECTIVE_DATE>
			</EQUIPMENT_MODEL>
		</DMD>
		</xsl:if>
		</xsl:if>
		<xsl:if test="$search_status = 'false'">
			<DMD xmlns="http://www.vzw.com/namespace/scm" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
			   <STATUS>
			      <STATUS_STR>Normal</STATUS_STR>
			      <MESSAGE></MESSAGE>
			   </STATUS>
				<xsl:if test="$trancnt > 0">
        				<TRANSACTION_ID><xsl:value-of select="transaction_id"/></TRANSACTION_ID>
				</xsl:if>
			   <EQUIPMENT_ESN>
			      <ESN><xsl:value-of select="$esn"/></ESN>
			   </EQUIPMENT_ESN>
			   <EQUIPMENT_MODEL>
			      <PROD_NAME>Unknown</PROD_NAME>
			      <MFG_CODE>UNK</MFG_CODE>
			      <EFFECTIVE_DATE>2004/01/01</EFFECTIVE_DATE>
			   </EQUIPMENT_MODEL>
			</DMD>		
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
