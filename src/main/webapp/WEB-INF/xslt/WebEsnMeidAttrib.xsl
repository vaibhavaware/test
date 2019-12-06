<?xml version="1.0" encoding="UTF-8"?>
<?xmlspysamplexml C:\bhamu\dmd\test.xml?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:template match="/equipment">
		<xsl:variable name="search_status" select="search_status/search_result_status"/>
		<xsl:variable name="empty_dacc" select="'!-!-!'"/>
		<xsl:variable name="modelloc" select="search_status/model_location"/>
		<xsl:variable name="esn" select="id/esn/dec"/>
		<xsl:variable name="meid" select="id/meid/hex"/>
		<xsl:variable name="trancnt" select="count(transaction_id)"/>
		<xsl:if test="$search_status = 'true'">
		<xsl:if test="$modelloc != 'MODEL_PRODUCT'">
			<xsl:if test="$modelloc = 'MODEL_REF'">
			<DMD>
				<STATUS>
					<STATUS_STR>Not Found</STATUS_STR>
					<MESSAGE>No Results Found for <xsl:value-of select="$esn"/></MESSAGE>
				</STATUS>
				<xsl:if test="$trancnt > 0">
        				<TRANSACTION_ID><xsl:value-of select="transaction_id"/></TRANSACTION_ID>
				</xsl:if>
			</DMD>
			</xsl:if>
			<xsl:if test="$modelloc = 'MEID_MODEL_REF'">
			<DMD>
				<STATUS>
					<STATUS_STR>Not Found</STATUS_STR>
					<MESSAGE>No Results Found for <xsl:value-of select="$meid"/></MESSAGE>
				</STATUS>
				<xsl:if test="$trancnt > 0">
	        			<TRANSACTION_ID><xsl:value-of select="transaction_id"/></TRANSACTION_ID>
				</xsl:if>
			</DMD>
			</xsl:if>
			<xsl:if test="$modelloc = 'ESN_EXCEPTION'">
			<DMD>
				<STATUS>
					<STATUS_STR>Not Found</STATUS_STR>
					<MESSAGE>No Results Found for <xsl:value-of select="$esn"/></MESSAGE>
				</STATUS>
				<xsl:if test="$trancnt > 0">
		        		<TRANSACTION_ID><xsl:value-of select="transaction_id"/></TRANSACTION_ID>
				</xsl:if>
			</DMD>
			</xsl:if>
			<xsl:if test="$modelloc = 'MEID_EXCEPTIONF'">
			<DMD>
				<STATUS>
					<STATUS_STR>Not Found</STATUS_STR>
					<MESSAGE>No Results Found for <xsl:value-of select="$meid"/></MESSAGE>
				</STATUS>
				<xsl:if test="$trancnt > 0">
			        	<TRANSACTION_ID><xsl:value-of select="transaction_id"/></TRANSACTION_ID>
				</xsl:if>
			</DMD>
			</xsl:if>
		</xsl:if>
		<xsl:if test="$modelloc = 'MODEL_PRODUCT'">
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
				<MFG_NAME>
					<xsl:value-of select="manufacturer/name"/>
				</MFG_NAME>
				<EQP_MODE>
					<xsl:value-of select="equipment_mode/mode"/>
				</EQP_MODE>
				<EQP_MODE_CODE>
					<xsl:value-of select="equipment_mode/code"/>
				</EQP_MODE_CODE>
				<IMAGE>
					<xsl:value-of select="misc/image"/>
				</IMAGE>
				<PUSH_TO_TALK>
					<xsl:value-of select="general_features/item[@type='PUSH_TO_TALK']"/>
				</PUSH_TO_TALK>
				<MOBILE_MESSAGING>
					<xsl:value-of select="general_features/item[@type='MOBILE_MESSAGING']"/>
				</MOBILE_MESSAGING>
				<GET_IT_NOW>
					<xsl:value-of select="general_features/item[@type='BREW']"/>
				</GET_IT_NOW>
				<xsl:variable name="fMask" select="featureMaskId"/>
				<xsl:if test="$fMask = ''">
					<MMS>N</MMS>
				</xsl:if>
				<xsl:if test="$fMask = '0'">
					<MMS>N</MMS>
				</xsl:if>
				<xsl:if test="$fMask = '1'">
					<MMS>N</MMS>
				</xsl:if>
				<xsl:if test="$fMask = '2'">
					<MMS>Y</MMS>
				</xsl:if>
				<xsl:if test="$fMask = '3'">
					<MMS>Y</MMS>
				</xsl:if>
				<BLUETOOTH>
					<xsl:value-of select="bluetooth"/>
				</BLUETOOTH>				
				<xsl:variable name="gfcnt" select="count(gf_flag)"/>
				<xsl:variable name="e911" select="general_features/item[@type='E911']"/>
				<xsl:if test="$gfcnt > 0">
					<xsl:variable name="gfflag" select="gf_flag"/>
					<xsl:if test="$gfflag = ''">
						<xsl:if test="$e911 != ''">
							<GPS><xsl:value-of select="$e911"/></GPS>
						</xsl:if>
						<xsl:if test="$e911 = ''">
							<GPS>N</GPS>
						</xsl:if>
					</xsl:if>
					<xsl:if test="$gfflag != ''">
						<GPS><xsl:value-of select="gf_flag"/></GPS>
					</xsl:if>
				</xsl:if>
				<xsl:if test="$gfcnt = 0">
					<xsl:if test="$e911 != ''">
						<GPS><xsl:value-of select="$e911"/></GPS>
					</xsl:if>
					<xsl:if test="$e911 = ''">
						<GPS>N</GPS>
					</xsl:if>
				</xsl:if>
				<EXPRESS_NETWORK>
					<xsl:value-of select="supported_technologies/item[@type='EXPRESS_NETWORK']"/>
				</EXPRESS_NETWORK>
				<USER_MANUAL_MIM_LINK>
					<xsl:value-of select="misc/link[@type='usermanual'][@format='mim']"/>
				</USER_MANUAL_MIM_LINK>
				<USER_MANUAL_PDF_LINK>
					<xsl:value-of select="misc/link[@type='usermanual'][@format='pdf']"/>
				</USER_MANUAL_PDF_LINK>
				<xsl:variable name="dacc" select="dacc"/>
				<xsl:variable name="dacc_cnt" select="count(dacc)"/>
				<xsl:if test="$dacc_cnt > 0">
					<xsl:if test="$dacc = ''">
						<DACC>
							<xsl:value-of select="$empty_dacc"/>
						</DACC>
					</xsl:if>
					<xsl:if test="$dacc != ''">
						<DACC>
							<xsl:value-of select="dacc"/>
						</DACC>
					</xsl:if>
				</xsl:if>
				<xsl:if test="$dacc_cnt = 0">
					<DACC>
						<xsl:value-of select="$empty_dacc"/>
					</DACC>
				</xsl:if>
				<DEVICE_TYPE>
					<xsl:value-of select="device_type"/>
				</DEVICE_TYPE>
			</EQUIPMENT_MODEL>
		</DMD>
		</xsl:if>
		</xsl:if>
		<xsl:if test="$search_status = 'false'">
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
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
