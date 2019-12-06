<?xml version="1.0" encoding="UTF-8"?>
<?xmlspysamplexml C:\bhamu\dmd\test.xml?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:template match="/equipment">
		<xsl:variable name="search_status" select="search_status/search_result_status"/>
		<xsl:variable name="empty_dacc" select="'!-!-!'"/>
		<xsl:variable name="trancnt" select="count(transaction_id)"/>
		<xsl:if test="$search_status = 'true'">
		<xsl:variable name="esn" select="id/esn/dec"/>
		<xsl:variable name="meid" select="id/meid/hex"/>
		<xsl:variable name="imei" select="id/imei/dec"/>
		<xsl:variable name="iccid" select="id/iccid/dec"/>
		
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
			<DEVICE_ID_INFO>
				<DEVICE_ID>
					<xsl:value-of select="id/esn/dec"/>
				</DEVICE_ID>
			</DEVICE_ID_INFO>
			</xsl:if>
			<xsl:if test="$meid != ''">
			<DEVICE_ID_INFO>
				<DEVICE_ID>
					<xsl:value-of select="id/meid/hex"/>
				</DEVICE_ID>
			</DEVICE_ID_INFO>
			</xsl:if>
			<xsl:if test="$imei != ''">
			<DEVICE_ID_INFO>
				<DEVICE_ID>
					<xsl:value-of select="id/imei/dec"/>
				</DEVICE_ID>
			</DEVICE_ID_INFO>
			</xsl:if>
			<xsl:if test="$iccid != ''">
			<DEVICE_ID_INFO>
				<DEVICE_ID>
					<xsl:value-of select="id/iccid/dec"/>
				</DEVICE_ID>
			</DEVICE_ID_INFO>
			</xsl:if>
			<EQUIPMENT_MODEL>
		        <MFG_CODE>
					<xsl:value-of select="manufacturer/code"/>
		        </MFG_CODE>
        		<PROD_NAME>
					<xsl:value-of select="prod_name"/>
		        </PROD_NAME>
				<PROD_TYPE>
					<xsl:value-of select="prod_type"/>
				</PROD_TYPE>
				<EQP_MODE_CODE>
					<xsl:value-of select="equipment_mode/code"/>
				</EQP_MODE_CODE>
				<xsl:variable name="fMask" select="featureMaskId"/>
				<xsl:if test="$fMask = ''">
					<EMS>N</EMS>
					<MMS>N</MMS>
				</xsl:if>
				<xsl:if test="$fMask = '0'">
					<EMS>N</EMS>
					<MMS>N</MMS>
				</xsl:if>
				<xsl:if test="$fMask = '1'">
					<EMS>Y</EMS>
					<MMS>N</MMS>
				</xsl:if>
				<xsl:if test="$fMask = '2'">
					<EMS>N</EMS>
					<MMS>Y</MMS>
				</xsl:if>
				<xsl:if test="$fMask = '3'">
					<EMS>Y</EMS>
					<MMS>Y</MMS>
				</xsl:if>
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
				<PTT>
					<xsl:value-of select="general_features/item	[@type='PUSH_TO_TALK']"/>
				</PTT>
				<GLOBAL_PHONE>
					<xsl:value-of select="general_features/item	[@type='GLOBAL_PHONE']"/>
				</GLOBAL_PHONE>
				<WAP>
					<xsl:value-of 	select="software/browser/version"/>
				</WAP>
				<xsl:variable name="cdma_1xrtt" 		select="supported_technologies/item[@type='CDMA_1XRTT']"/>
				<xsl:variable name="cdma_1xevdo" 		select="supported_technologies/item[@type='CDMA_1XEVDO']"/>
				<xsl:if test="$cdma_1xrtt = 'N'">
					<xsl:if test="$cdma_1xevdo = ''">
						<CDMA_1X>N</CDMA_1X>
					</xsl:if>
					<xsl:if test="$cdma_1xevdo = 'N'">
						<CDMA_1X>N</CDMA_1X>
					</xsl:if>
					<xsl:if test="$cdma_1xevdo = 'Y'">
						<CDMA_1X>Y</CDMA_1X>
					</xsl:if>
				</xsl:if>
				<xsl:if test="$cdma_1xrtt = ''">
					<xsl:if test="$cdma_1xevdo = ''">
						<CDMA_1X>N</CDMA_1X>
					</xsl:if>
					<xsl:if test="$cdma_1xevdo = 'N'">
						<CDMA_1X>N</CDMA_1X>
					</xsl:if>
					<xsl:if test="$cdma_1xevdo = 'Y'">
						<CDMA_1X>Y</CDMA_1X>
					</xsl:if>
				</xsl:if>
				<xsl:if test="$cdma_1xrtt = 'Y'">
					<CDMA_1X>Y</CDMA_1X>
				</xsl:if>
				<xsl:variable name="cdmartt" 	select="count(supported_technologies/item[@type='CDMA_1XRTT'])"/>
				<xsl:variable name="cdmaevdo" 	select="count(supported_technologies/item[@type='CDMA_1XEVDO'])"/>
				<xsl:if test="$cdmartt = 0">
					<xsl:if test="$cdmaevdo = 0">
						<CDMA_1X>N</CDMA_1X>
					</xsl:if>
				</xsl:if>
				<xsl:if test="$esn != ''">
				<ESN_LOCK>
					<xsl:value-of select="esn_lock_status"/>
				</ESN_LOCK>
				</xsl:if>
				<xsl:if test="$meid != ''">
				<MEID_LOCK>
					<xsl:value-of select="meid_lock_status"/>
				</MEID_LOCK>
				</xsl:if>

				<PIB_LOCK>
					<xsl:value-of select="pib_lock"/>
				</PIB_LOCK>
				
				<OUTLET_ID>
					<xsl:value-of select="outlet_id"/>
				</OUTLET_ID>
								
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
				<PREPAY_ELIGIBILITY>
					<xsl:value-of select="prepay_eligibility"/>
				</PREPAY_ELIGIBILITY>
				<ELIGIBILITY_DATE >
					<xsl:value-of select="eligibility_date"/>
				</ELIGIBILITY_DATE>
			</EQUIPMENT_MODEL>
		</DMD>
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
