 /**
  * (c) Copyright 2011 WESO, Computer Science Department,
  * Facultad de Ciencias, University of Oviedo, Oviedo, Asturias, Spain, 33007
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. The name of the author may not be used to endorse or promote products
  *    derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.weso.pscs.utils;
 
 import org.weso.moldeas.utils.URISchemeManager;
 
 
 public class PSCConstants {
 
 	private static final String ID_PREFIX = "resource";
 
	//public static final String HTTP_NUTS_PSI_ENAKTING_ORG_ID = URISchemeManager.getURIPrefix("nuts-id");
 	//FIXME: Change # by /
 	
 	public static final String HTTP_WWW_W3_ORG_2004_02_SKOS_CORE = URISchemeManager.getURIPrefix("skos");
 	public static final String SKOS_prefLabel = HTTP_WWW_W3_ORG_2004_02_SKOS_CORE+"prefLabel";
 	public static final String SKOS_example = HTTP_WWW_W3_ORG_2004_02_SKOS_CORE+"example";
 	public static final String SKOS_Concept = HTTP_WWW_W3_ORG_2004_02_SKOS_CORE+"Concept";
 	public static final String SKOS_Broader = HTTP_WWW_W3_ORG_2004_02_SKOS_CORE+"broader";
 	public static final String SKOS_Broader_Transitive = HTTP_WWW_W3_ORG_2004_02_SKOS_CORE+"broaderTransitive";
 	public static final String SKOS_IN_SCHEME = HTTP_WWW_W3_ORG_2004_02_SKOS_CORE+"inScheme";
 	public static final String SKOS_CLOSE_MATCH = HTTP_WWW_W3_ORG_2004_02_SKOS_CORE+"closeMatch";
 	public static final String SKOS_EXACT_MATCH= HTTP_WWW_W3_ORG_2004_02_SKOS_CORE+"exactMatch";
 
 
 	public static final String BASE_URI=URISchemeManager.getURIPrefix("moldeas");
 
 	public static final String HTTP_PURL_ORG_WESO_PSCS = URISchemeManager.getURIPrefix("pscs");
 	public static final String HTTP_PURL_ORG_WESO_PSCS_DEF_LEVEL = URISchemeManager.getURIPrefix("pscs-onto")+"level";
 	public static final String HTTP_PURL_ORG_WESO_PSCS_DEF_RELATED_MATCH = URISchemeManager.getURIPrefix("pscs-onto")+"relatedMatch";
 	public static final String HTTP_PURL_ORG_WESO_CPV_2008 = URISchemeManager.getURIPrefix("cpv-2008");
 	public static final String HTTP_PURL_ORG_WESO_CPV_DEF = URISchemeManager.getURIPrefix("cpv-onto");
 	public static final String HTTP_PURL_ORG_WESO_CPV_2003 =  URISchemeManager.getURIPrefix("cpv-2003");
 	public static final String CPV2003_codeIn = URISchemeManager.getURIPrefix("moldeas-onto")+"topic-2003";
 	public static final String CPV_codeIn = URISchemeManager.getURIPrefix("moldeas-onto")+"topic";
 	
 	public static final String HTTP_PURL_ORG_WESO_CPV_DEF_DIVISION = URISchemeManager.getURIPrefix("cpv-onto")+"Division";
 	public static final String HTTP_PURL_ORG_WESO_CPV_DEF_GROUP = URISchemeManager.getURIPrefix("cpv-onto")+"Group";
 	public static final String HTTP_PURL_ORG_WESO_CPV_DEF_CLASS = URISchemeManager.getURIPrefix("cpv-onto")+"Class";
 	public static final String HTTP_PURL_ORG_WESO_CPV_DEF_CATEGORY = URISchemeManager.getURIPrefix("cpv-onto")+"Category";
 	
 	public static final String CPV_CONCEPT = URISchemeManager.getURIPrefix("cpv-onto")+"cpvConcept";
 	
 	public static final String HTTP_PURL_ORG_WESO_PPN_DEF = URISchemeManager.getURIPrefix("moldeas-onto");
 	public static final String HTTP_PURL_ORG_WESO_PPN = URISchemeManager.getURIPrefix("moldeas-ppn");
 	//public static final String CPV2008_codeIn = PSCConstants.HTTP_PURL_ORG_WESO_CPV_DEF+"cpv-code";	
 	public static final String NUTS_CODE = URISchemeManager.getURIPrefix("moldeas-onto")+"located-in";
 
 	public static final String IN_PROCESS = URISchemeManager.getURIPrefix("moldeas-onto")+"in-process";
 	
 	
 	public static String formatNUTSTO(String id){	
		return URISchemeManager.getURIPrefix("moldeas-nuts") + id ;
 	}
 	 
 	//2   f(Id)=Skos-Uri
 	public static final String formatId2003(String id){
 		return HTTP_PURL_ORG_WESO_CPV_2003 +ID_PREFIX +URISchemeManager.URI_SEPARATOR+ id;
 	}
 	
 
 	//3   f(Id)=Skos-Uri
 	public static String formatId(String id){	
 		return HTTP_PURL_ORG_WESO_CPV_2008 +ID_PREFIX +URISchemeManager.URI_SEPARATOR+ id ;
 	}
 	
 
 	public static String formatURIId(String id, String date) {
 		return PSCConstants.HTTP_PURL_ORG_WESO_PPN+date+URISchemeManager.URI_SEPARATOR+ID_PREFIX+URISchemeManager.URI_SEPARATOR+id;
 	}
 
 }
