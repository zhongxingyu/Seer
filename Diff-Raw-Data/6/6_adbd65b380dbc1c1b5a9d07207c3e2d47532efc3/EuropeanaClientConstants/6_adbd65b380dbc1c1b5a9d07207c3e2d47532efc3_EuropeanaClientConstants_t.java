 /*
  * Copyright 2007 EDL FOUNDATION
  *
  * Licensed under the EUPL, Version 1.1 or - as soon they
  * will be approved by the European Commission - subsequent
  * versions of the EUPL (the "Licence");
  * you may not use this work except in compliance with the
  * Licence.
  * You may obtain a copy of the Licence at:
  *
  * http://ec.europa.eu/idabc/eupl
  *
  * Unless required by applicable law or agreed to in
  * writing, software distributed under the Licence is
  * distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied.
  * See the Licence for the specific language governing
  * permissions and limitations under the Licence.
  */
 package eu.europeana.uim.gui.cp.client.utils;
 
 
 
 /**
  * String constants for GUI labels.
  * 
  * @author Georgios Markakis
  */
 
 public class EuropeanaClientConstants {
 
 	public final static String PANELLABEL = "Import Resources" ;
 	public final static String PANELDESCRIPTION = "This view allows to import resources into UIM." ;
 	
 	public final static String ERRORIMAGELOC = "images/no.png" ;
 	public final static String SUCCESSIMAGELOC = "images/ok.png" ;
 	public final static String PROBLEMIMAGELOC = "images/iconQuestionMark.png" ;
 	public final static String QUERYIMAGELOC = "images/network.gif"; 
 	public final static String SEARCHDIALOGMSG = "Searching for SugarCRM entries"; 
 	
 	//Search related labels
 	public final static String DSNAMESEARCHLABEL = "DataSet Name:" ;
 	public final static String IDSEARCHLABEL = "Identifier:" ;
 	public final static String ORGANIZATIONSEARCHLABEL = "Organization:" ;	
 	public final static String ACRONYMSEARCHLABEL = "Acronym:" ;	
 	public final static String TYPESEARCHLABEL = "Type:" ;
 	public final static String STATUSSEARCHLABEL = "SugarCRM Status:" ;
 	public final static String ENABLEDSEARCHLABEL = "Enabled:" ;
 	public final static String INGESTDATESEARCHLABEL = "Expected Ingestion Date:" ;
 	public final static String AMOUNTSEARCHLABEL = "Amount of ingested items:" ;
 	public final static String COUNTRYSEARCHLABEL = "Country:" ;
 	public final static String USERSEARCHLABEL = "User:" ;
 	public final static String SEARCHBUTTONLABEL = "Search" ;
 	public final static String SEARCHBUTTONTITLE = "Search SugarCRM for Records" ;	
 	
 	//Import related labels
 	public final static String IMPORTMENULABEL = "Importing to UIM & Repox" ;
 	public final static String IMPORTBUTTONLABEL = "Import Selected" ;
 	public final static String IMPORTBUTTONTITLE = "Populate UIM and Repox with Data from SugarCrm" ;
 	
 	//Misc
 	public final static String UIMSTATELABEL = "State:" ;
 	public final static String LEGENDSUCCESSLABEL = "The current Collection has already been imported " +
 			"into UIM and all its expernal dependencies are functioning normally." ;
 	public final static String LEGENDNALABEL = "The current Collection has not been imported " +
 			"and it is not available from within UIM." ;
 	public final static String LEGENDFAILURELABEL = "The current Collection defined in SugarCRM " +
 			"has been imported, but some of its external dependencies (ie REPOX) have not been setup proprerly." +
 			"Try modifying the values contained in SugarCRM and importing these values again." ;
 	
 	//Import Controlled Vocabulary Labels
 	public final static String IMPORTVOCABULARY = "Import Vocabulary";
 	public final static String VOCABULARYNAME = "Vocabulary Name:";
 	public final static String VOCABULARYURI = "Vocabulary URI";
 	public final static String VOCABULARYSUFFIX = "Vocabulary Suffix";
 	public final static String VOCABULARYRULES = "Vocabulary Rules";
 	public final static String VOCSAVEANDUPLOAD = "Save and Upload";
 	public final static String VOCSELECTLOCALFILE = "Select File";
 	public final static String VOCORIGINALFIELDS = "Original Fields";
 	public final static String VOCMAPPABLEFIELDS = "Mappable EDM Fields";
 	public final static String VOCCREATEMAPPING = "Create Field Mapping";
 	public final static String VOCMAPPEDFIELDS = "Mapped Fields";
 	public final static String VOCSAVEMAPPING = "Save Mapping";
 	public final static String VOCDELETEMAPPING = "Delete Field Mapping";
 	public final static String VOCDELETEVOCABULARY = "Delete Vocabulary";
 	public final static String VOCEDITVOCABULARY = "Edit Vocabulary";
 	public final static String VOCREFRESHVOCABULARY="Refresh";
	public final static String UPLOAD_SERVLET_URL="http://sip-manager.isti.cnr.it:8181/gui/EuropeanaIngestionControlPanel/uploadservlet";
	public final static String REMOTE_UPLOAD_SERVLET_URL="http://sip-manager.isti.cnr.it:8181/gui/EuropeanaIngestionControlPanel/remoteuploadservlet";
	//public final static String UPLOAD_SERVLET_URL="http://localhost:8181/gui/EuropeanaIngestionControlPanel/uploadservlet";
	//public final static String REMOTE_UPLOAD_SERVLET_URL="http://localhost:8181/gui/EuropeanaIngestionControlPanel/remoteuploadservlet";
 }
