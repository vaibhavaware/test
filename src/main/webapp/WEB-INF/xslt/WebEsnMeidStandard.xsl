<?xml version="1.0" encoding="UTF-8"?>
<?xmlspysamplexml C:\bhamu\dmd\test.xml?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:template match="/equipment">
		<xsl:variable name="search_status" select="search_status/search_result_status"/>
		<xsl:variable name="empty_dacc" select="'!-!-!'"/>
		<xsl:if test="$search_status = 'true'">
		<xsl:variable name="esn" select="id/esn/dec"/>
		<xsl:variable name="meid" select="id/meid/hex"/>
		<DMD>
			<STATUS>
				<STR>
					<xsl:value-of select="search_status/status/status_str"/>
				</STR>
				<MSG>
					<xsl:value-of select="search_status/status/status_message"/>
				</MSG>
			</STATUS>
			<xsl:if test="$esn != ''">
			<EQUIPMENT_ESN>
				<ESN>
					<xsl:value-of select="id/esn/dec"/>
				</ESN>
				<LOCK_STATUS>
					<xsl:value-of select="esn_lock_status"/>
				</LOCK_STATUS>
			</EQUIPMENT_ESN>
			</xsl:if>
			<xsl:if test="$meid != ''">
			<EQUIPMENT_MEID>
				<MEID>
					<xsl:value-of select="id/meid/hex"/>
				</MEID>
				<LOCK_STATUS>
					<xsl:value-of select="meid_lock_status"/>
				</LOCK_STATUS>
			</EQUIPMENT_MEID>
			</xsl:if>
			<EQUIPMENT_MODEL>
				<MFG_CODE>
					<xsl:value-of select="manufacturer/code"/>
				</MFG_CODE>
				<MFG_NAME>
					<xsl:value-of select="manufacturer/name"/>
				</MFG_NAME>
				<PROD_TYPE>
					<xsl:value-of select="prod_type"/>
				</PROD_TYPE>
				<PROD_NAME>
					<xsl:value-of select="prod_name"/>
				</PROD_NAME>
				<EFFECTIVE_DATE>
					<xsl:value-of select="effective_date"/>
				</EFFECTIVE_DATE>
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
				<MOBILE_MESSAGING>
					<xsl:value-of select="general_features/item[@type='MOBILE_MESSAGING']"/>
				</MOBILE_MESSAGING>
				<GET_IT_NOW>
					<xsl:value-of select="general_features/item[@type='BREW']"/>
				</GET_IT_NOW>
				<NATIONAL_ACCESS>
					<xsl:value-of select="supported_technologies/item[@type='EXPRESS_NETWORK']"/>
				</NATIONAL_ACCESS>
				<IMAGE>
					<xsl:value-of select="misc/image"/>
				</IMAGE> 
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
		<xsl:if test="$search_status = 'false'">
		<DMD>
			<STATUS>
				<STR>
					<xsl:value-of select="search_status/status/status_str"/>
				</STR>
				<MSG>
					<xsl:value-of select="search_status/status/status_message"/>
				</MSG>
			</STATUS>
		</DMD>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
