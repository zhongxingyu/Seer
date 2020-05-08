 /**
  * Copyright (c) 2009 University of Rochester
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the  
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/. 
  */
 
 package org.extensiblecatalog.ncip.v2.koha.util;
 
 import java.text.SimpleDateFormat;
 
 /**
  * This class defines several constants as public static final variables which may be used throughout the NCIP Toolkit
  */
 public class KohaConstants {
 	// Koha configuration
 
 	/**
 	 * Currency code used for fines for koha
 	 */
 
 	public static final String DEFAULT_SCHEME = "http://www.niso.org/schemas/ncip/v2_02/ncip_v2_02.xsd";
 
 	// Constants for parsing from toolkit.properties
 	public static final String CONF_OPAC_PORT = "OPACPort";
 	public static final String CONF_OPAC_SERVER = "OPACServer";
 	public static final String CONF_SVC_PORT = "SVCPort";
 	public static final String CONF_ILS_DI_SUFFIX = "ILSDISuffix";
 	public static final String CONF_SVC_SUFFIX = "SVCSuffix";
 	public static final String CONF_DEFAULT_AGENCY = "AgencyId";
 	public static final String CONF_AUTH_DATA_FORMAT_TYPE = "AuthDataFormatType";
 	public static final String CONF_USER_REGISTRATION_LINK = "UserRegistrationLink";
 	public static final String CONF_AGENCY_UNSTRUCTURED_ADDRESS = "AgencyUnstructuredAddress";
 	public static final String CONF_AGENCY_TRANSLATED_NAME = "AgencyTranslatedName";
 	public static final String CONF_NCIP_TOOLKIT_VERSION = "NCIPToolkitVersion";
 	public static final String CONF_MAX_ITEM_PREPARATION_TIME_DELAY = "MaxItemPreparationTimeDelay";
 	public static final String CONF_NEXT_ITEM_TOKEN_EXPIRATION_TIME = "NextItemTokenExpirationTime";
 	public static final String CONF_INCLUDE_PARTICULAR_PROBLEMS_TO_LUIS = "IncludeParticularProblemsInLookupItemSet";
	public static final String CONF_KOHA_CURRENCY_CODE = "KohaCurrencyCode";
 	public static final String CONF_MARC_ITEM_DESC_FIELD = "MarcItemDescriptionField";
 	public static final String CONF_ADMIN_NAME = "AdminName";
 	public static final String CONF_ADMIN_PASS = "AdminPass";
 
 	public static final String REQUEST_ID_DELIMITER = ";";
 
 	public static final SimpleDateFormat KOHA_DATE_FORMATTER = new SimpleDateFormat("yyy-MM-dd");
 
 	public static final String PARAM_SERVICE = "service";
 	public static final String PARAM_ID = "id";
 
 	public static final String ILS_DI_LOOKUP_USER = "GetPatronInfo";
 	public static final String ILS_DI_LOOKUP_ITEM = "GetRecords";
 
 	public static final String SVC_AUTHENTICATION = "authentication";
 	public static final String SVC_BARCODE = "barcode";
 	public static final String SVC_BIB = "bib";
 	public static final String SVC_BIB_PROFILE = "bib_profile";
 	public static final String SVC_CHECKING = "checkin";
 	public static final String SVC_CHECKOUTS = "checkouts";
 	public static final String SVC_HOLDS = "holds";
 	public static final String SVC_RENEW = "renew";
 	public static final String SVC_REPORT = "report";
 	public static final String SVC_MEMBERS_SEARCH = "members/search";
 
 	public static final String STATUS_OK = "ok";
 
 	// HTML ATTRS
 
 	public static final String ATTR_ITEMS = "items";
 
 	//
 	// XML PARSING SECTION
 	//
 
 	public static final String XML_AUTH_STATUS = "auth_status";
 	public static final String XML_STATUS = "status";
 	public static final String XML_CONTROLFIELD = "controlfield";
 	public static final String XML_DATAFIELD = "datafield";
 	public static final String XML_SUBFIELD = "subfield";
 	public static final String XML_LEADER = "leader";
 
 	// XML ATTRS
 
 	public static final String XML_ATTR_TAG = "tag";
 	public static final String XML_ATTR_CODE = "code";
 
 	// Possible XML responses
 
 	public static final String XML_VAL_STATUS_EXPIRED = "expired";
 	public static final String XML_VAL_STATUS_OK = "ok";
 
 	// Tags human readable
 	public static final String CONTROLFIELD_ITEM_ID_TAG = "001";
 
 	// datafields
 	public static final String DATAFIELD_AUTHOR_RELATED_TAG = "100";
 	public static final String DATAFIELD_ISBN_RELATED_TAG = "020";
 	public static final String DATAFIELD_TITLE_RELATED_TAG = "245";
 	public static final String DATAFIELD_PUBLICATION_RELATED_TAG = "260";
 	public static final String DATAFIELD_EDITION_TAG = "250";
 	public static final String DATAFIELD_PHYSICAL_DESCRIPTION_TAG = "300";
 	public static final String DATAFIELD_SIGLA_PARENT_TAG = "910"; // TODO: Implement SIGLA parsing
 
 	// subfields
 	public static final String SUBFIELD_AUTHOR_NAME_CODE = "a";
 	public static final String SUBFIELD_ISBN_CODE = SUBFIELD_AUTHOR_NAME_CODE;
 	public static final String SUBFIELD_TITLE_CODE = SUBFIELD_AUTHOR_NAME_CODE;
 	public static final String SUBFIELD_PUBLICATION_PLACE_CODE = SUBFIELD_AUTHOR_NAME_CODE;
 	public static final String SUBFIELD_EDITION_STATEMENT_CODE = SUBFIELD_AUTHOR_NAME_CODE;
 	public static final String SUBFIELD_PAGINATION_CODE = SUBFIELD_AUTHOR_NAME_CODE;
 	public static final String SUBFIELD_SIGLA_CODE = SUBFIELD_AUTHOR_NAME_CODE;
 
 	public static final String SUBFIELD_TITLE_OF_COMPONENT_CODE = "b";
 	public static final String SUBFIELD_PUBLISHER_NAME_CODE = SUBFIELD_TITLE_OF_COMPONENT_CODE;
 
 	public static final String SUBFIELD_PUBLICATION_DATE_CODE = "c";
 }
