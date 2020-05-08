 package ro.finsiel.eunis.dataimport.parsers; 
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import ro.finsiel.eunis.utilities.SQLUtilities;
 import eionet.eunis.util.Constants;
 
 /** 
  * 
  */ 
 public class Natura2000ImportParser extends DefaultHandler { 
         
         private BufferedInputStream inputStream;
         
         private PreparedStatement preparedStatementNatObject;
         private PreparedStatement preparedStatementUpdateNatObject;
         private PreparedStatement preparedStatementSiteInsert;
         private PreparedStatement preparedStatementSite;
         private PreparedStatement preparedStatementBioRegion;
         private PreparedStatement preparedStatementNatObjectReportType;
         private PreparedStatement preparedStatementNatObjectGeoscope;
         private PreparedStatement preparedStatementReportAttribute;
         private PreparedStatement preparedStatementSiteSites;
         private PreparedStatement preparedStatementSiteAttribute;
         private PreparedStatement preparedStatementSiteRelatedDesignations;
         private PreparedStatement preparedStatementReportType;
         
         private String siteNatureObjectId;
         
         private String siteCode;
         private String siteName;
         private String siteType;
         private String dateCompilation;
         private String dateUpdate;
         private String dateSpa;
         private String respondent;
         private String description;
         private String longitude;
         private String lonEw;
         private String lonDeg;
         private String lonMin;
         private String lonSec;
         private String latitude;
         private String latNs;
         private String latDeg;
         private String latMin;
         private String latSec;
         private String area;
         private String altMin;
         private String altMax;
         private String altMean;
         private String geoscopeId;
         private String nutsCode;
         private String nutsCover;
         private boolean newSite = false;
         
         private int maxNoIdInt = 0;
         private int maxReportAttributeId = 0;
         private int maxReportTypeId = 0;
         
         private String habitatCode;
         private String habitatCover;
         private String habitatRepresentatity;
         private String habitatRelsurface;
         private String habitatConsStatus;
         private String habitatGlobalAssesment;
         
         private String speciesCode;
         private String speciesName;
         private String speciesResident;
         private String speciesPopulation;
         private String speciesConservation;
         private String speciesIsolation;
         private String speciesGlobal;
         private String speciesWinter;
         private String speciesStaging;
         private String speciesBreeding;
         
         private String otherSpeciesGroup;
         private String otherSpeciesSciName;
         private String otherSpeciesPopulation;
         private String otherSpeciesMotivation;
         private String otherSpeciesAnnexII;
         
         private String siteDescriptionHabClassDesc;
         private String siteDescriptionHabClassCover;
         
         private String siteDescriptionOtherCharacteristics;
         private String siteDescriptionQualityImportance;
         private String siteDescriptionVulnerability;
         private String siteDescriptionSiteDesignation;
         private String siteDescriptionDocumentation;
         
         private String siteProtectionDesigTypeCode;
         private String siteProtectionDesigTypeCover;
         
         private String siteImpactsCode;
         private String siteImpactsIntensity;
         private String siteImpactsPercent;
         private String siteImpactsInfluence;
         
         private String ecoInfo;
         
         private List<String> errors;
         
         List<String> bioRegions;
         
         private Connection con; 
         
         private StringBuffer buf; 
         private SQLUtilities sqlUtilities;
         
         public Natura2000ImportParser(SQLUtilities sqlUtilities) {
         	this.sqlUtilities = sqlUtilities;
         	this.con = sqlUtilities.getConnection();
         	buf = new StringBuffer();
         	errors = new ArrayList<String>();
         	
         	bioRegions = new ArrayList<String>();
         	bioRegions.add(Constants.ALPINE);
         	bioRegions.add(Constants.ANATOL);
         	bioRegions.add(Constants.ARCTIC);
         	bioRegions.add(Constants.ATLANTIC);
         	bioRegions.add(Constants.BOREAL);
         	bioRegions.add(Constants.CONTINENT);
         	bioRegions.add(Constants.MACARONES);
         	bioRegions.add(Constants.MEDITERRANIAN);
         	bioRegions.add(Constants.PANNONIC);
         	bioRegions.add(Constants.PONTIC);
         	bioRegions.add(Constants.STEPPIC);
         } 
         
         private void parseDocument() throws SAXException { 
                 
             //get a factory 
             SAXParserFactory spf = SAXParserFactory.newInstance(); 
             try { 
             	//get a new instance of parser 
             	SAXParser sp = spf.newSAXParser(); 
             	//parse the file and also register this class for call backs 
             	sp.parse(inputStream, this);
             	
             }catch(SAXException se) { 
                     errors.add(se.getMessage());
                     //throw new RuntimeException(se.getMessage(), se); 
             }catch(ParserConfigurationException pce) {
                     errors.add(pce.getMessage());
                     //throw new RuntimeException(pce.getMessage(), pce); 
             }catch (IOException ie) {
                     errors.add(ie.getMessage());
                     //throw new RuntimeException(ie.getMessage(), ie); 
             } 
         } 
         
         public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
         	buf = new StringBuffer();
         } 
 
         public void characters(char[] ch, int start, int length) throws SAXException { 
         	buf.append(ch,start,length); 
         } 
         
         public void endElement(String uri, String localName, String qName) throws SAXException { 
         	try{ 
         		if(qName.equalsIgnoreCase("SiteCode")) {
         			siteCode = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("SiteName")) {
         			siteName = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("SiteType")) {
         			
         			siteType = buf.toString().trim();
         			
         			getSiteNatObjectId();
         			if(!newSite)
         				deleteOldRecords();
         			setSitesTab(siteNatureObjectId, Constants.SITES_TAB_GENERAL);
         			
         		} else if(qName.equalsIgnoreCase("Date_Compilation")) {
         			dateCompilation = buf.toString().trim();
        				if(dateCompilation.contains("T"))
        					dateCompilation = parseDate(dateCompilation);
         		} else if(qName.equalsIgnoreCase("Date_Update")) {
         			dateUpdate = buf.toString().trim();
        				if(dateUpdate.contains("T"))
        					dateUpdate = parseDate(dateUpdate);
         		} else if(qName.equalsIgnoreCase("Date_Spa")) {
         			dateSpa = buf.toString().trim();
        				if(dateSpa.contains("T"))
        					dateSpa = parseDate(dateSpa);
         		} else if(qName.equalsIgnoreCase("Respondent")) {
         			respondent = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("Description")) {
         			description = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("Longitude")) {
         			longitude = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("Latitude")) {
         			latitude = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("AreaHA")) {
         			area = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("Altitude_Min")) {
         			altMin = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("Altitude_Max")) {
         			altMax = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("Altitude_Mean")) {
         			altMean = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("NutsCode")) {
         			nutsCode = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("NutsCover")) {
         			nutsCover = buf.toString().trim();
         		}
         		
         		if(qName.equalsIgnoreCase("OtherSiteCode")) {
         			String code = buf.toString().trim();
         			if(code != null && code.length() > 0){
         				preparedStatementSiteSites.setString(1, siteCode);
         				preparedStatementSiteSites.setString(2, code);
         				preparedStatementSiteSites.setString(3, "=");
         				preparedStatementSiteSites.executeUpdate();
         				
         				setSitesTab(siteNatureObjectId, Constants.SITES_TAB_SITES);
         			}
         		}
         		
         		if(qName.equalsIgnoreCase("AdministrativeRegion")) {
         			if(nutsCode != null && nutsCode.length() > 0 && nutsCover != null && nutsCover.length() > 0){
 	        			maxReportTypeId++;
 	        			maxReportAttributeId++;
 	        			
 	        			preparedStatementNatObjectReportType.setString(1, siteNatureObjectId);
 	    				preparedStatementNatObjectReportType.setInt(2, -1);
 	    				preparedStatementNatObjectReportType.setInt(3, -1);
 	    				preparedStatementNatObjectReportType.setInt(4, maxReportTypeId);
 	   					preparedStatementNatObjectReportType.setInt(5, maxReportAttributeId);
 	    				preparedStatementNatObjectReportType.setInt(6, -1);
 	    				preparedStatementNatObjectReportType.executeUpdate();
 	        			
 	    				preparedStatementReportType.setInt(1, maxReportTypeId);
 	    				preparedStatementReportType.setString(2, nutsCode);
 	    				preparedStatementReportType.setString(3, "REGION_CODE");
 	    				preparedStatementReportType.executeUpdate();	    				
 	    				
     					preparedStatementReportAttribute.setInt(1, maxReportAttributeId);
     					preparedStatementReportAttribute.setString(2, "COVER");
     					preparedStatementReportAttribute.setString(3, "NUMBER");
     					preparedStatementReportAttribute.setString(4, nutsCover);
     					preparedStatementReportAttribute.executeUpdate();
         			}				
         		}
         		
         		if(qName.equalsIgnoreCase("BioDescription")) {
         			String bioRegion = buf.toString().trim();
         			if(bioRegion != null && bioRegions.contains(bioRegion.toUpperCase())){
         				preparedStatementSiteAttribute.setString(1, siteCode);
         				preparedStatementSiteAttribute.setString(2, bioRegion.toUpperCase());
         				preparedStatementSiteAttribute.setString(3, "BOOLEAN");
        					preparedStatementSiteAttribute.setString(4, "TRUE");
        					preparedStatementSiteAttribute.setString(5, "sdfxml");
         				preparedStatementSiteAttribute.executeUpdate();
         				
         				bioRegions.remove(bioRegion.toUpperCase());
         			}
         		}
         		
         		if(qName.equalsIgnoreCase("SiteLocation")) {
         			
         			if(dateUpdate == null)
         				dateUpdate = "";
         			
         			if(dateCompilation == null)
         				dateCompilation = "";
         			
         			calculateLatitudeParams();
         			calculateLongitudeParams();
         			
         			if(!newSite){
         				preparedStatementUpdateNatObject.setString(1, "NATURA2000_SITES");
         				preparedStatementUpdateNatObject.setString(2, siteNatureObjectId);
         				preparedStatementUpdateNatObject.setString(3, siteCode);
         				preparedStatementUpdateNatObject.executeUpdate();
         				
         				preparedStatementSite.setString(1, siteName);
 	        			preparedStatementSite.setString(2, dateCompilation);
 	        			preparedStatementSite.setString(3, dateUpdate);
 	        			preparedStatementSite.setString(4, dateSpa);
 	        			preparedStatementSite.setString(5, respondent);
 	        			preparedStatementSite.setString(6, description);
 	        			preparedStatementSite.setString(7, latitude);
 	        			preparedStatementSite.setString(8, latNs);
 	        			preparedStatementSite.setString(9, latDeg);
 	        			preparedStatementSite.setString(10, latMin);
 	        			preparedStatementSite.setString(11, latSec);
 	        			preparedStatementSite.setString(12, longitude);
 	        			preparedStatementSite.setString(13, lonEw);
 	        			preparedStatementSite.setString(14, lonDeg);
 	        			preparedStatementSite.setString(15, lonMin);
 	        			preparedStatementSite.setString(16, lonSec);
 	        			preparedStatementSite.setString(17, area);
 	        			preparedStatementSite.setString(18, altMin);
 	        			preparedStatementSite.setString(19, altMax);
 	        			preparedStatementSite.setString(20, altMean);
 	        			preparedStatementSite.setString(21, siteCode);
 	        			preparedStatementSite.executeUpdate();
         			} else {
 	        			preparedStatementNatObject.setString(1, siteNatureObjectId);
 	        			preparedStatementNatObject.setString(2, siteCode);
 	        			preparedStatementNatObject.executeUpdate();
 	        			
 	    				String geoIdEu = getGeoscopeIdEu();
 	    				preparedStatementSiteInsert.setString(1, siteCode);
 	    				preparedStatementSiteInsert.setString(2, siteNatureObjectId);
 	    				preparedStatementSiteInsert.setString(3, siteName);
 	    				preparedStatementSiteInsert.setString(4, dateCompilation);
 	    				preparedStatementSiteInsert.setString(5, dateUpdate);
 	    				preparedStatementSiteInsert.setString(6, dateSpa);
 	    				preparedStatementSiteInsert.setString(7, respondent);
 	    				preparedStatementSiteInsert.setString(8, description);
 	    				preparedStatementSiteInsert.setString(9, latitude);
 	    				preparedStatementSiteInsert.setString(10, latNs);
 	    				preparedStatementSiteInsert.setString(11, latDeg);
 	    				preparedStatementSiteInsert.setString(12, latMin);
 	    				preparedStatementSiteInsert.setString(13, latSec);
 	    				preparedStatementSiteInsert.setString(14, longitude);
 	    				preparedStatementSiteInsert.setString(15, lonEw);
 	    				preparedStatementSiteInsert.setString(16, lonDeg);
 	        			preparedStatementSiteInsert.setString(17, lonMin);
 	        			preparedStatementSiteInsert.setString(18, lonSec);
 	        			preparedStatementSiteInsert.setString(19, area);
 	        			preparedStatementSiteInsert.setString(20, altMin);
 	        			preparedStatementSiteInsert.setString(21, altMax);
 	        			preparedStatementSiteInsert.setString(22, altMean);
 	        			preparedStatementSiteInsert.setString(23, "NATURA2000");
 	        			preparedStatementSiteInsert.setString(24, geoIdEu);
 	        			preparedStatementSiteInsert.executeUpdate();
         			}
         			
         			geoscopeId = getGeoscopeId();
         			preparedStatementNatObjectGeoscope.setString(1, siteNatureObjectId);
         			preparedStatementNatObjectGeoscope.setString(2, geoscopeId);
         			preparedStatementNatObjectGeoscope.executeUpdate();
         			
         			preparedStatementSiteAttribute.setString(1, siteCode);
         			preparedStatementSiteAttribute.setString(2, "TYPE");
         			preparedStatementSiteAttribute.setString(3, "TEXT");
         			preparedStatementSiteAttribute.setString(4, siteType);
         			preparedStatementSiteAttribute.setString(5, "sdfxml");
         			preparedStatementSiteAttribute.addBatch();
         			
        	        	for(String region : bioRegions){
         				preparedStatementSiteAttribute.setString(1, siteCode);
         				preparedStatementSiteAttribute.setString(2, region);
         				preparedStatementSiteAttribute.setString(3, "BOOLEAN");
        					preparedStatementSiteAttribute.setString(4, "FALSE");
        					preparedStatementSiteAttribute.setString(5, "sdfxml");
         				preparedStatementSiteAttribute.addBatch();
         			}
        	        	preparedStatementSiteAttribute.executeBatch(); 
        	        	preparedStatementSiteAttribute.clearParameters();
         		}
         		
         		if(qName.equalsIgnoreCase("HCode")) {
         			habitatCode = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("HCover")) {
         			habitatCover = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("HRepresentatity")) {
         			habitatRepresentatity = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("HRelsurface")) {
         			habitatRelsurface = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("HConsStatus")) {
         			habitatConsStatus = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("HGlobalAssesment")) {
         			habitatGlobalAssesment = buf.toString().trim();
         		}
         		
         		if(qName.equalsIgnoreCase("Habitat")) {
         			
         			if(habitatCode != null && habitatCode.length() > 0){
 						String habitatIdNatObject = getHabitatNatObjectId(habitatCode);
 						
 						if(habitatIdNatObject != null && habitatIdNatObject.length() > 0){
 							
 							maxReportAttributeId++;
 							
 							preparedStatementNatObjectReportType.setString(1, siteNatureObjectId);
 							preparedStatementNatObjectReportType.setString(2, habitatIdNatObject);
 							preparedStatementNatObjectReportType.setInt(3, -1);
 							preparedStatementNatObjectReportType.setInt(4, -1);
 							preparedStatementNatObjectReportType.setInt(5, maxReportAttributeId);
 							preparedStatementNatObjectReportType.setInt(6, -1);
 							
 							preparedStatementNatObjectReportType.executeUpdate();
 							
 							setSitesTab(siteNatureObjectId, Constants.SITES_TAB_HABITAT_TYPES);
 							setHabitatTabSites(habitatIdNatObject);
 							
 							preparedStatementReportAttribute.setInt(1, maxReportAttributeId);
 							preparedStatementReportAttribute.setString(2, Constants.REPORT_ATTRIBUTE_HABITAT_SOURCE_TABLE);
 							preparedStatementReportAttribute.setString(3, "TEXT");
 							preparedStatementReportAttribute.setString(4, "habit2");
 							preparedStatementReportAttribute.addBatch();
 							
 							if(habitatCover != null && habitatCover.length() > 0){
 								preparedStatementReportAttribute.setInt(1, maxReportAttributeId);
 								preparedStatementReportAttribute.setString(2, Constants.REPORT_ATTRIBUTE_HABITAT_COVER);
 								preparedStatementReportAttribute.setString(3, "TEXT");
 								preparedStatementReportAttribute.setString(4, habitatCover);
 								preparedStatementReportAttribute.addBatch();
 							}
 							if(habitatRepresentatity != null && habitatRepresentatity.length() > 0){
 								preparedStatementReportAttribute.setInt(1, maxReportAttributeId);
 								preparedStatementReportAttribute.setString(2, Constants.REPORT_ATTRIBUTE_HABITAT_REPRESENTATIVITY);
 								preparedStatementReportAttribute.setString(3, "TEXT");
 								preparedStatementReportAttribute.setString(4, habitatRepresentatity);
 								preparedStatementReportAttribute.addBatch();
 							}
 							if(habitatRelsurface != null && habitatRelsurface.length() > 0){
 								preparedStatementReportAttribute.setInt(1, maxReportAttributeId);
 								preparedStatementReportAttribute.setString(2, Constants.REPORT_ATTRIBUTE_HABITAT_RELATIVE_SURFACE);
 								preparedStatementReportAttribute.setString(3, "TEXT");
 								preparedStatementReportAttribute.setString(4, habitatRelsurface);
 								preparedStatementReportAttribute.addBatch();
 							}
 							if(habitatConsStatus != null && habitatConsStatus.length() > 0){
 								preparedStatementReportAttribute.setInt(1, maxReportAttributeId);
 								preparedStatementReportAttribute.setString(2, Constants.REPORT_ATTRIBUTE_HABITAT_CONSERVATION);
 								preparedStatementReportAttribute.setString(3, "TEXT");
 								preparedStatementReportAttribute.setString(4, habitatConsStatus);
 								preparedStatementReportAttribute.addBatch();
 							}
 							if(habitatGlobalAssesment != null && habitatGlobalAssesment.length() > 0){
 								preparedStatementReportAttribute.setInt(1, maxReportAttributeId);
 								preparedStatementReportAttribute.setString(2, Constants.REPORT_ATTRIBUTE_HABITAT_GLOBAL);
 								preparedStatementReportAttribute.setString(3, "TEXT");
 								preparedStatementReportAttribute.setString(4, habitatGlobalAssesment);
 								preparedStatementReportAttribute.addBatch();
 							}
 							
 							preparedStatementReportAttribute.executeBatch(); 
 							preparedStatementReportAttribute.clearParameters();
 	
 						} else {
 							preparedStatementSiteAttribute.setString(1, siteCode);
 							preparedStatementSiteAttribute.setString(2, "HABITAT_CODE_"+habitatCode);
 							preparedStatementSiteAttribute.setString(3, "TEXT");
 							preparedStatementSiteAttribute.setString(4, habitatCode);
 							preparedStatementSiteAttribute.setString(5, "habit2");
 							preparedStatementSiteAttribute.addBatch();
 							
 							preparedStatementSiteAttribute.setString(1, siteCode);
 							preparedStatementSiteAttribute.setString(2, "HABITAT_COVER_"+habitatCode);
 							preparedStatementSiteAttribute.setString(3, "NUMBER");
 							preparedStatementSiteAttribute.setString(4, habitatCover);
 							preparedStatementSiteAttribute.setString(5, "habit2");
 							preparedStatementSiteAttribute.addBatch();
 							
 							if(habitatRepresentatity != null && habitatRepresentatity.length() > 0){
 								preparedStatementSiteAttribute.setString(1, siteCode);
 								preparedStatementSiteAttribute.setString(2, "HABITAT_REPRESENTATIVITY_"+habitatCode);
 								preparedStatementSiteAttribute.setString(3, "NUMBER");
 								preparedStatementSiteAttribute.setString(4, habitatRepresentatity);
 								preparedStatementSiteAttribute.setString(5, "habit2");
 								preparedStatementSiteAttribute.addBatch();
 							}
 							
 							if(habitatRelsurface != null && habitatRelsurface.length() > 0){
 								preparedStatementSiteAttribute.setString(1, siteCode);
 								preparedStatementSiteAttribute.setString(2, "HABITAT_RELATIVE_SURFACE_"+habitatCode);
 								preparedStatementSiteAttribute.setString(3, "NUMBER");
 								preparedStatementSiteAttribute.setString(4, habitatRelsurface);
 								preparedStatementSiteAttribute.setString(5, "habit2");
 								preparedStatementSiteAttribute.addBatch();
 							}
 							
 							if(habitatConsStatus != null && habitatConsStatus.length() > 0){
 								preparedStatementSiteAttribute.setString(1, siteCode);
 								preparedStatementSiteAttribute.setString(2, "HABITAT_CONSERVATION_"+habitatCode);
 								preparedStatementSiteAttribute.setString(3, "TEXT");
 								preparedStatementSiteAttribute.setString(4, habitatConsStatus);
 								preparedStatementSiteAttribute.setString(5, "habit2");
 								preparedStatementSiteAttribute.addBatch();
 							}
 							
 							if(habitatGlobalAssesment != null && habitatGlobalAssesment.length() > 0){
 								preparedStatementSiteAttribute.setString(1, siteCode);
 								preparedStatementSiteAttribute.setString(2, "HABITAT_GLOBAL_"+habitatCode);
 								preparedStatementSiteAttribute.setString(3, "TEXT");
 								preparedStatementSiteAttribute.setString(4, habitatGlobalAssesment);
 								preparedStatementSiteAttribute.setString(5, "habit2");
 								preparedStatementSiteAttribute.addBatch();
 							}
 							
 							preparedStatementSiteAttribute.executeBatch(); 
 							preparedStatementSiteAttribute.clearParameters();
 						}
 						
 						habitatCover = null;
 						habitatRepresentatity = null;
 						habitatRelsurface = null;
 						habitatConsStatus = null;
 						habitatGlobalAssesment = null;
         			}
         		}
         		
         		if(qName.equalsIgnoreCase("SpeciesCode")) {
         			speciesCode = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("SpeciesName")) {
         			speciesName = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("Resident")) {
         			speciesResident = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("Population")) {
         			speciesPopulation = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("Conservation")) {
         			speciesConservation = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("IsolationFactor")) {
         			speciesIsolation = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("GlobalImportance")) {
         			speciesGlobal = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("Staging")) {
         			speciesStaging = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("Winter")) {
         			speciesWinter = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("Breeding")) {
         			speciesBreeding = buf.toString().trim();
         		}
         		
         		//values are one step ahead
         		if(qName.equalsIgnoreCase("EcologicalInformation32A")) {
         			ecoInfo = Constants.SPECIES_SOURCE_TABLE_BIRD;
         		} else if(qName.equalsIgnoreCase("EcologicalInformation32B")) {
         			ecoInfo = Constants.SPECIES_SOURCE_TABLE_MAMMAL;
         		} else if(qName.equalsIgnoreCase("EcologicalInformation32C")) {
         			ecoInfo = Constants.SPECIES_SOURCE_TABLE_AMPREP;
         		} else if(qName.equalsIgnoreCase("EcologicalInformation32D")) {
         			ecoInfo = Constants.SPECIES_SOURCE_TABLE_FISHES;
         		} else if(qName.equalsIgnoreCase("EcologicalInformation32E")) {
         			ecoInfo = Constants.SPECIES_SOURCE_TABLE_INVERT;
         		} else if(qName.equalsIgnoreCase("EcologicalInformation32F")) {
         			ecoInfo = Constants.SPECIES_SOURCE_TABLE_PLANT;
         		}
         		
         		if(qName.equalsIgnoreCase("Species")) {
         			if(speciesCode != null && speciesCode.length() > 0){
 						String speciesIdNatObject = getSpeciesNatObjectId(speciesCode);
 						
 						if(speciesIdNatObject != null && speciesIdNatObject.length() > 0){
 							
 							if(ecoInfo == null || ecoInfo.length() == 0)
 								ecoInfo = Constants.SPECIES_SOURCE_TABLE_BIRD;
 							
 							maxReportAttributeId++;
 							
 							preparedStatementNatObjectReportType.setString(1, siteNatureObjectId);
 							preparedStatementNatObjectReportType.setString(2, speciesIdNatObject);
 							preparedStatementNatObjectReportType.setInt(3, -1);
 							preparedStatementNatObjectReportType.setInt(4, -1);
 							preparedStatementNatObjectReportType.setInt(5, maxReportAttributeId);
 							preparedStatementNatObjectReportType.setInt(6, -1);
 							
 							preparedStatementNatObjectReportType.executeUpdate();
 							
 							setSitesTab(siteNatureObjectId, Constants.SITES_TAB_FAUNA_FLORA);
 							setSpeciesTabSites(speciesIdNatObject);
 
 							if(speciesResident != null && speciesResident.length() > 0){
 								preparedStatementReportAttribute.setInt(1, maxReportAttributeId);
 								preparedStatementReportAttribute.setString(2, Constants.REPORT_ATTRIBUTE_SPECIES_RESIDENT);
 								preparedStatementReportAttribute.setString(3, "TEXT");
 								preparedStatementReportAttribute.setString(4, speciesResident);
 								preparedStatementReportAttribute.addBatch();
 							}
 							if(speciesPopulation != null && speciesPopulation.length() > 0){
 								preparedStatementReportAttribute.setInt(1, maxReportAttributeId);
 								preparedStatementReportAttribute.setString(2, Constants.REPORT_ATTRIBUTE_SPECIES_POPULATION);
 								preparedStatementReportAttribute.setString(3, "TEXT");
 								preparedStatementReportAttribute.setString(4, speciesPopulation);
 								preparedStatementReportAttribute.addBatch();
 							}
 							if(speciesConservation != null && speciesConservation.length() > 0){
 								preparedStatementReportAttribute.setInt(1, maxReportAttributeId);
 								preparedStatementReportAttribute.setString(2, Constants.REPORT_ATTRIBUTE_SPECIES_CONSERVATION);
 								preparedStatementReportAttribute.setString(3, "TEXT");
 								preparedStatementReportAttribute.setString(4, speciesConservation);
 								preparedStatementReportAttribute.addBatch();
 							}
 							if(speciesIsolation != null && speciesIsolation.length() > 0){
 								preparedStatementReportAttribute.setInt(1, maxReportAttributeId);
 								preparedStatementReportAttribute.setString(2, Constants.REPORT_ATTRIBUTE_SPECIES_ISOLATION);
 								preparedStatementReportAttribute.setString(3, "TEXT");
 								preparedStatementReportAttribute.setString(4, speciesIsolation);
 								preparedStatementReportAttribute.addBatch();
 							}
 							if(speciesGlobal != null && speciesGlobal.length() > 0){
 								preparedStatementReportAttribute.setInt(1, maxReportAttributeId);
 								preparedStatementReportAttribute.setString(2, Constants.REPORT_ATTRIBUTE_SPECIES_GLOBAL);
 								preparedStatementReportAttribute.setString(3, "TEXT");
 								preparedStatementReportAttribute.setString(4, speciesGlobal);
 								preparedStatementReportAttribute.addBatch();
 							}
 							if(speciesStaging != null && speciesStaging.length() > 0){
 								preparedStatementReportAttribute.setInt(1, maxReportAttributeId);
 								preparedStatementReportAttribute.setString(2, Constants.REPORT_ATTRIBUTE_SPECIES_STAGING);
 								preparedStatementReportAttribute.setString(3, "TEXT");
 								preparedStatementReportAttribute.setString(4, speciesStaging);
 								preparedStatementReportAttribute.addBatch();
 							}
 							if(speciesWinter != null && speciesWinter.length() > 0){
 								preparedStatementReportAttribute.setInt(1, maxReportAttributeId);
 								preparedStatementReportAttribute.setString(2, Constants.REPORT_ATTRIBUTE_SPECIES_WINTER);
 								preparedStatementReportAttribute.setString(3, "TEXT");
 								preparedStatementReportAttribute.setString(4, speciesWinter);
 								preparedStatementReportAttribute.addBatch();
 							}
 							if(speciesBreeding != null && speciesBreeding.length() > 0){
 								preparedStatementReportAttribute.setInt(1, maxReportAttributeId);
 								preparedStatementReportAttribute.setString(2, Constants.REPORT_ATTRIBUTE_SPECIES_BREEDING);
 								preparedStatementReportAttribute.setString(3, "TEXT");
 								preparedStatementReportAttribute.setString(4, speciesBreeding);
 								preparedStatementReportAttribute.addBatch();
 							}
 							
 							preparedStatementReportAttribute.setInt(1, maxReportAttributeId);
 							preparedStatementReportAttribute.setString(2, Constants.REPORT_ATTRIBUTE_SPECIES_OTHER_SPECIES);
 							preparedStatementReportAttribute.setString(3, "TEXT");
 							preparedStatementReportAttribute.setString(4, "False");
 							preparedStatementReportAttribute.addBatch();
 							
 							preparedStatementReportAttribute.setInt(1, maxReportAttributeId);
 							preparedStatementReportAttribute.setString(2, Constants.REPORT_ATTRIBUTE_SPECIES_SOURCE_TABLE);
 							preparedStatementReportAttribute.setString(3, "TEXT");
 							preparedStatementReportAttribute.setString(4, ecoInfo);
 							preparedStatementReportAttribute.addBatch();
 							
 							preparedStatementReportAttribute.executeBatch(); 
 							preparedStatementReportAttribute.clearParameters();
 						} else {
 							if(speciesName != null && speciesName.length() > 0){
 								preparedStatementSiteAttribute.setString(1, siteCode);
 								preparedStatementSiteAttribute.setString(2, "OTHER_SPECIES_"+speciesName);
 								preparedStatementSiteAttribute.setString(3, "TEXT");
 								preparedStatementSiteAttribute.setString(4, speciesName);
 								preparedStatementSiteAttribute.setString(5, "spec");
 								preparedStatementSiteAttribute.addBatch();
 							}
 							
 							if(speciesResident != null && speciesResident.length() > 0){
 								preparedStatementSiteAttribute.setString(1, siteCode);
 								preparedStatementSiteAttribute.setString(2, "OTHER_SPECIES_RESIDENT_"+speciesName);
 								preparedStatementSiteAttribute.setString(3, "TEXT");
 								preparedStatementSiteAttribute.setString(4, speciesResident);
 								preparedStatementSiteAttribute.setString(5, "spec");
 								preparedStatementSiteAttribute.addBatch();
 							}
 							
 							if(speciesPopulation != null && speciesPopulation.length() > 0){
 								preparedStatementSiteAttribute.setString(1, siteCode);
 								preparedStatementSiteAttribute.setString(2, "OTHER_SPECIES_POPULATION_"+speciesName);
 								preparedStatementSiteAttribute.setString(3, "TEXT");
 								preparedStatementSiteAttribute.setString(4, speciesPopulation);
 								preparedStatementSiteAttribute.setString(5, "spec");
 								preparedStatementSiteAttribute.addBatch();
 							}
 							
 							if(speciesBreeding != null && speciesBreeding.length() > 0){
 								preparedStatementSiteAttribute.setString(1, siteCode);
 								preparedStatementSiteAttribute.setString(2, "OTHER_SPECIES_BREEDING_"+speciesName);
 								preparedStatementSiteAttribute.setString(3, "TEXT");
 								preparedStatementSiteAttribute.setString(4, speciesBreeding);
 								preparedStatementSiteAttribute.setString(5, "spec");
 								preparedStatementSiteAttribute.addBatch();
 							}
 							
 							if(speciesWinter != null && speciesWinter.length() > 0){
 								preparedStatementSiteAttribute.setString(1, siteCode);
 								preparedStatementSiteAttribute.setString(2, "OTHER_SPECIES_WINTERING_"+speciesName);
 								preparedStatementSiteAttribute.setString(3, "TEXT");
 								preparedStatementSiteAttribute.setString(4, speciesWinter);
 								preparedStatementSiteAttribute.setString(5, "spec");
 								preparedStatementSiteAttribute.addBatch();
 							}
 							
 							if(speciesStaging != null && speciesStaging.length() > 0){
 								preparedStatementSiteAttribute.setString(1, siteCode);
 								preparedStatementSiteAttribute.setString(2, "OTHER_SPECIES_STAGING_"+speciesName);
 								preparedStatementSiteAttribute.setString(3, "TEXT");
 								preparedStatementSiteAttribute.setString(4, speciesStaging);
 								preparedStatementSiteAttribute.setString(5, "spec");
 								preparedStatementSiteAttribute.addBatch();
 							}
 							
 							if(speciesConservation != null && speciesConservation.length() > 0){
 								preparedStatementSiteAttribute.setString(1, siteCode);
 								preparedStatementSiteAttribute.setString(2, "OTHER_SPECIES_CONSERVATION_"+speciesName);
 								preparedStatementSiteAttribute.setString(3, "TEXT");
 								preparedStatementSiteAttribute.setString(4, speciesConservation);
 								preparedStatementSiteAttribute.setString(5, "spec");
 								preparedStatementSiteAttribute.addBatch();
 							}
 							
 							if(speciesIsolation != null && speciesIsolation.length() > 0){
 								preparedStatementSiteAttribute.setString(1, siteCode);
 								preparedStatementSiteAttribute.setString(2, "OTHER_SPECIES_ISOLATION_"+speciesName);
 								preparedStatementSiteAttribute.setString(3, "TEXT");
 								preparedStatementSiteAttribute.setString(4, speciesIsolation);
 								preparedStatementSiteAttribute.setString(5, "spec");
 								preparedStatementSiteAttribute.addBatch();
 							}
 							
 							if(speciesGlobal != null && speciesGlobal.length() > 0){
 								preparedStatementSiteAttribute.setString(1, siteCode);
 								preparedStatementSiteAttribute.setString(2, "OTHER_SPECIES_GLOBAL_"+speciesName);
 								preparedStatementSiteAttribute.setString(3, "TEXT");
 								preparedStatementSiteAttribute.setString(4, speciesGlobal);
 								preparedStatementSiteAttribute.setString(5, "spec");
 								preparedStatementSiteAttribute.addBatch();
 							}
 							
 						}
 						speciesCode = null;
 						speciesName = null;
 						speciesResident = null;
 						speciesPopulation = null;
 						speciesConservation = null;
 						speciesIsolation = null;
 						speciesGlobal = null;
 						speciesStaging = null;
 						speciesWinter = null;
 						speciesBreeding = null;
         			}
         		}
         		
         		if(qName.equalsIgnoreCase("Group")) {
         			otherSpeciesGroup = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("SciName")) {
         			otherSpeciesSciName = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("Population")) {
         			otherSpeciesPopulation = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("Motivation")) {
         			otherSpeciesMotivation = buf.toString().trim();
         		}
         		
         		if(qName.equalsIgnoreCase("OtherSpecies")) {
     				
 					if(otherSpeciesSciName != null && otherSpeciesSciName.length() > 0){
 						
 						String speciesIdNatObject = getSpeciesNatObjectIdByName(otherSpeciesSciName);
 						if(speciesIdNatObject != null && speciesIdNatObject.length() > 0){
 							
 							maxReportAttributeId++;
 							
 							preparedStatementNatObjectReportType.setString(1, siteNatureObjectId);
 							preparedStatementNatObjectReportType.setString(2, speciesIdNatObject);
 							preparedStatementNatObjectReportType.setInt(3, -1);
 							preparedStatementNatObjectReportType.setInt(4, -1);
 							preparedStatementNatObjectReportType.setInt(5, maxReportAttributeId);
 							preparedStatementNatObjectReportType.setInt(6, -1);
 							
 							preparedStatementNatObjectReportType.executeUpdate();
 							
 							setSitesTab(siteNatureObjectId, Constants.SITES_TAB_FAUNA_FLORA);
 							setSpeciesTabSites(speciesIdNatObject);
 							
 							preparedStatementReportAttribute.setInt(1, maxReportAttributeId);
 							preparedStatementReportAttribute.setString(2, Constants.REPORT_ATTRIBUTE_SPECIES_SOURCE_TABLE);
 							preparedStatementReportAttribute.setString(3, "TEXT");
 							preparedStatementReportAttribute.setString(4, "spec");
 							preparedStatementReportAttribute.addBatch();
 
 							if(otherSpeciesMotivation != null && otherSpeciesMotivation.length() > 0){
 								preparedStatementReportAttribute.setInt(1, maxReportAttributeId);
 								preparedStatementReportAttribute.setString(2, Constants.REPORT_ATTRIBUTE_OTHER_SPECIES_MOTIVATION);
 								preparedStatementReportAttribute.setString(3, "TEXT");
 								preparedStatementReportAttribute.setString(4, otherSpeciesMotivation);
 								preparedStatementReportAttribute.addBatch();
 							}
 							if(otherSpeciesPopulation != null && otherSpeciesPopulation.length() > 0){
 								preparedStatementReportAttribute.setInt(1, maxReportAttributeId);
 								preparedStatementReportAttribute.setString(2, Constants.REPORT_ATTRIBUTE_OTHER_SPECIES_POPULATION);
 								preparedStatementReportAttribute.setString(3, "TEXT");
 								preparedStatementReportAttribute.setString(4, otherSpeciesPopulation);
 								preparedStatementReportAttribute.addBatch();
 							}
 							
 							preparedStatementReportAttribute.executeBatch(); 
 							preparedStatementReportAttribute.clearParameters();
 							
 						} else {
 							preparedStatementSiteAttribute.setString(1, siteCode);
 							preparedStatementSiteAttribute.setString(2, "OTHER_SPECIES_"+otherSpeciesSciName);
 							preparedStatementSiteAttribute.setString(3, "TEXT");
 							preparedStatementSiteAttribute.setString(4, "spec");
 							preparedStatementSiteAttribute.setString(5, "spec");
 							preparedStatementSiteAttribute.addBatch();
 							
 							setSitesTab(siteNatureObjectId, Constants.SITES_TAB_FAUNA_FLORA);
 						
 							if(otherSpeciesGroup != null && otherSpeciesGroup.length() > 0){
 								preparedStatementSiteAttribute.setString(1, siteCode);
 								preparedStatementSiteAttribute.setString(2, "OTHER_SPECIES_TAXGROUP_"+otherSpeciesSciName);
 								preparedStatementSiteAttribute.setString(3, "TEXT");
 								preparedStatementSiteAttribute.setString(4, otherSpeciesGroup);
 								preparedStatementSiteAttribute.setString(5, "spec");
 								preparedStatementSiteAttribute.addBatch();
 							}
 							if(otherSpeciesPopulation != null && otherSpeciesPopulation.length() > 0){
 								preparedStatementSiteAttribute.setString(1, siteCode);
 								preparedStatementSiteAttribute.setString(2, "OTHER_SPECIES_POPULATION_"+otherSpeciesSciName);
 								preparedStatementSiteAttribute.setString(3, "TEXT");
 								preparedStatementSiteAttribute.setString(4, otherSpeciesPopulation);
 								preparedStatementSiteAttribute.setString(5, "spec");
 								preparedStatementSiteAttribute.addBatch();
 							}
 							if(otherSpeciesMotivation != null && otherSpeciesMotivation.length() > 0){
 								preparedStatementSiteAttribute.setString(1, siteCode);
 								preparedStatementSiteAttribute.setString(2, "OTHER_SPECIES_MOTIVATION_"+otherSpeciesSciName);
 								preparedStatementSiteAttribute.setString(3, "TEXT");
 								preparedStatementSiteAttribute.setString(4, otherSpeciesMotivation);
 								preparedStatementSiteAttribute.setString(5, "spec");
 								preparedStatementSiteAttribute.addBatch();
 							}
 							
 							preparedStatementSiteAttribute.executeBatch(); 
 							preparedStatementSiteAttribute.clearParameters();
 						}
 						
 						otherSpeciesGroup = null;
 						otherSpeciesSciName = null;
 						otherSpeciesPopulation = null;
 						otherSpeciesMotivation = null;
 					}
         		}
         		
         		if(qName.equalsIgnoreCase("Description")) {
         			siteDescriptionHabClassDesc = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("Cover")) {
         			siteDescriptionHabClassCover = buf.toString().trim();
         		}
         		
         		if(qName.equalsIgnoreCase("HabitatClasses")) {
     				if(siteDescriptionHabClassDesc != null && siteDescriptionHabClassCover != null)
     					updateSiteDescriptionHabitatClasses(siteDescriptionHabClassDesc, siteCode, siteDescriptionHabClassCover);
     				
     				siteDescriptionHabClassDesc = null;
     				siteDescriptionHabClassCover = null;
         		}
         		
         		if(qName.equalsIgnoreCase("OtherCharacteristics")) {
         			siteDescriptionOtherCharacteristics = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("QualityImportance")) {
         			siteDescriptionQualityImportance = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("Vulnerability")) {
         			siteDescriptionVulnerability = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("SiteDesignation")) {
         			siteDescriptionSiteDesignation = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("Documentation")) {
         			siteDescriptionDocumentation = buf.toString().trim();
         		}
         		
         		if(qName.equalsIgnoreCase("SiteDescription")) {
        				//TODO insert new values 
         		}
         		
         		if(qName.equalsIgnoreCase("Code")) {
         			siteProtectionDesigTypeCode = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("Cover")) {
         			siteProtectionDesigTypeCover = buf.toString().trim();
         		}
         		
         		if(qName.equalsIgnoreCase("DesignationTypes")) {
         			if(siteProtectionDesigTypeCode != null && siteProtectionDesigTypeCover != null){
         				preparedStatementSiteRelatedDesignations.setString(1, siteCode);
         				preparedStatementSiteRelatedDesignations.setString(2, siteProtectionDesigTypeCode);
         				preparedStatementSiteRelatedDesignations.setString(3, geoscopeId);
         				preparedStatementSiteRelatedDesignations.setString(4, siteProtectionDesigTypeCover);
         				
         				preparedStatementSiteRelatedDesignations.executeUpdate();
         				
         				setSitesTab(siteNatureObjectId, Constants.SITES_TAB_DESIGNATION);
         			}
         		}
         		
         		if(qName.equalsIgnoreCase("Code")) {
         			siteImpactsCode = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("Intensity")) {
         			siteImpactsIntensity = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("Influence")) {
         			siteImpactsInfluence = buf.toString().trim();
         		} else if(qName.equalsIgnoreCase("Percent")) {
         			siteImpactsPercent = buf.toString().trim();
         		}
         		
         		if(qName.equalsIgnoreCase("WithinSite") || qName.equalsIgnoreCase("AroundSite")) {
         			if(siteImpactsCode != null && siteImpactsCode.length() > 0){
         				
         				maxReportTypeId++;
         				preparedStatementReportType.setInt(1, maxReportTypeId);
         				preparedStatementReportType.setString(2, siteImpactsCode);
         				preparedStatementReportType.setString(3, "HUMAN_ACTIVITY");
         				preparedStatementReportType.executeUpdate();
         				
         				maxReportAttributeId++;
         				
         				preparedStatementReportAttribute.setInt(1, maxReportAttributeId);
     					preparedStatementReportAttribute.setString(2, "IN_OUT");
     					preparedStatementReportAttribute.setString(3, "TEXT");
     					if(qName.equalsIgnoreCase("WithinSite"))
     						preparedStatementReportAttribute.setString(4, "I");
     					else
     						preparedStatementReportAttribute.setString(4, "O");
     					preparedStatementReportAttribute.addBatch();
     					
         				if(siteImpactsIntensity != null && siteImpactsIntensity.length() > 0){
         					preparedStatementReportAttribute.setInt(1, maxReportAttributeId);
         					preparedStatementReportAttribute.setString(2, "INTENSITY");
         					preparedStatementReportAttribute.setString(3, "TEXT");
         					preparedStatementReportAttribute.setString(4, siteImpactsIntensity);
         					preparedStatementReportAttribute.addBatch();
         				}
         				if(siteImpactsInfluence != null && siteImpactsInfluence.length() > 0){
         					preparedStatementReportAttribute.setInt(1, maxReportAttributeId);
         					preparedStatementReportAttribute.setString(2, "INFLUENCE");
         					preparedStatementReportAttribute.setString(3, "TEXT");
         					preparedStatementReportAttribute.setString(4, siteImpactsInfluence);
         					preparedStatementReportAttribute.addBatch();
         				}
         				if(siteImpactsPercent != null && siteImpactsPercent.length() > 0){
         					preparedStatementReportAttribute.setInt(1, maxReportAttributeId);
         					preparedStatementReportAttribute.setString(2, "COVER");
         					preparedStatementReportAttribute.setString(3, "NUMBER");
         					preparedStatementReportAttribute.setString(4, siteImpactsPercent);
         					preparedStatementReportAttribute.addBatch();
         				}
         				
         				preparedStatementReportAttribute.executeBatch();
         				preparedStatementReportAttribute.clearParameters();
         				
         				preparedStatementNatObjectReportType.setString(1, siteNatureObjectId);
         				preparedStatementNatObjectReportType.setInt(2, -1);
         				preparedStatementNatObjectReportType.setInt(3, -1);
         				preparedStatementNatObjectReportType.setInt(4, maxReportTypeId);
        					preparedStatementNatObjectReportType.setInt(5, maxReportAttributeId);
         				preparedStatementNatObjectReportType.setInt(6, -1);
         				
         				preparedStatementNatObjectReportType.executeUpdate();
         				
         				setSitesTab(siteNatureObjectId, Constants.SITES_TAB_OTHER_INFO);
         				
         				siteImpactsCode = null;
         				siteImpactsIntensity = null;
         				siteImpactsInfluence = null;
         				siteImpactsPercent = null;
         				
         			}
         		}
         		
         	} 
         	catch (Exception e){ 
         		if(siteCode != null)
         			errors.add("Error! Site ID: "+siteCode+" Error Message: "+e.getMessage());
         		else
         			errors.add("Error Message: "+e.getMessage());
         		//throw new RuntimeException(e.toString(), e); 
         	} 
         } 
         
         public List<String> execute(BufferedInputStream inputStream) throws Exception {
                 
             this.inputStream = inputStream;
             
             try {
             	maxNoIdInt = getMaxId("SELECT MAX(ID_NATURE_OBJECT) FROM CHM62EDT_NATURE_OBJECT");
             	maxReportAttributeId = getMaxId("SELECT MAX(ID_REPORT_ATTRIBUTES) FROM CHM62EDT_REPORT_ATTRIBUTES");
             	maxReportTypeId = getMaxId("SELECT MAX(ID_REPORT_TYPE) FROM CHM62EDT_REPORT_TYPE");
             	
             	String queryNatObject = "INSERT INTO chm62edt_nature_object (ID_NATURE_OBJECT, ORIGINAL_CODE, ID_DC, TYPE) VALUES (?,?, -1, 'NATURA2000_SITES')";
             	this.preparedStatementNatObject = con.prepareStatement(queryNatObject);
             	
             	String updateNatObject = "UPDATE chm62edt_nature_object SET TYPE = ? WHERE ID_NATURE_OBJECT = ? AND ORIGINAL_CODE = ?";
             	this.preparedStatementUpdateNatObject = con.prepareStatement(updateNatObject);
             	
             	String querySiteInsert = "INSERT INTO chm62edt_sites (ID_SITE, ID_NATURE_OBJECT, NAME, COMPILATION_DATE, " +
             	"COMPLEX_NAME, DISTRICT_NAME, " +
     			"UPDATE_DATE, SPA_DATE, RESPONDENT, DESCRIPTION, LATITUDE, LAT_NS, LAT_DEG, LAT_MIN, LAT_SEC, " +
     			"LONGITUDE, LONG_EW, LONG_DEG, LONG_MIN, LONG_SEC, AREA, ALT_MIN, ALT_MAX, ALT_MEAN, SOURCE_DB, ID_GEOSCOPE) VALUES " +
     			"(?,?,?,?,'','',?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
             	this.preparedStatementSiteInsert = con.prepareStatement(querySiteInsert);
             	
             	String querySites = "UPDATE chm62edt_sites SET NAME=?, COMPILATION_DATE=?, UPDATE_DATE=?, " +
             			"SPA_DATE=?, RESPONDENT=?, DESCRIPTION=?, LATITUDE=?, LAT_NS=?, LAT_DEG=?, LAT_MIN=?, " +
             			"LAT_SEC=?, LONGITUDE=?, LONG_EW=?, LONG_DEG=?, LONG_MIN=?, LONG_SEC=?, AREA=?, " +
             			"ALT_MIN=?, ALT_MAX=?, ALT_MEAN=? WHERE ID_SITE = ?";
             	this.preparedStatementSite = con.prepareStatement(querySites);
             	
             	String queryBioRegion = "UPDATE chm62edt_site_attributes SET VALUE=? WHERE ID_SITE=? AND NAME=?";
             	this.preparedStatementBioRegion = con.prepareStatement(queryBioRegion);
             	
             	String insertNatObjectReportType = "INSERT INTO chm62edt_nature_object_report_type " +
     					"(ID_NATURE_OBJECT, ID_NATURE_OBJECT_LINK, ID_GEOSCOPE, ID_REPORT_TYPE, ID_REPORT_ATTRIBUTES, ID_DC) " +
     					"VALUES (?,?,?,?,?,?)";
             	this.preparedStatementNatObjectReportType = con.prepareStatement(insertNatObjectReportType);
             	
             	String insertNatObjectGeoscope = "INSERT INTO CHM62EDT_NATURE_OBJECT_GEOSCOPE " +
 						"(ID_NATURE_OBJECT, ID_NATURE_OBJECT_LINK, ID_DC, ID_GEOSCOPE, ID_REPORT_ATTRIBUTES) " +
 						"VALUES (?,-1,-1,?,-1)";
             	this.preparedStatementNatObjectGeoscope = con.prepareStatement(insertNatObjectGeoscope);
             	
             	String insertReportAttribute = "INSERT INTO chm62edt_report_attributes " +
             			"(ID_REPORT_ATTRIBUTES, NAME, TYPE, VALUE) VALUES (?,?,?,?)";
             	this.preparedStatementReportAttribute = con.prepareStatement(insertReportAttribute);
             	
             	String insertSiteSites = "INSERT INTO CHM62EDT_SITES_SITES " +
 						"(ID_SITE, ID_SITE_LINK, SEQUENCE, RELATION_TYPE, WITHIN_PROJECT, SOURCE_TABLE) " +
 						"VALUES (?,?,-1,?,1,'sitrel')";
             	this.preparedStatementSiteSites = con.prepareStatement(insertSiteSites);
             	
             	String insertSiteAttribute = "INSERT INTO chm62edt_site_attributes " +
     					"(ID_SITE, NAME, TYPE, VALUE, SOURCE_DB, SOURCE_TABLE) VALUES (?,?,?,?,'NATURA2000',?)";
             	this.preparedStatementSiteAttribute = con.prepareStatement(insertSiteAttribute);
             	
             	String insertSiteRelatedDesignations = "INSERT INTO chm62edt_sites_related_designations " +
 						"(ID_SITE, ID_DESIGNATION, ID_GEOSCOPE, SEQUENCE, OVERLAP_TYPE, OVERLAP, SOURCE_DB, SOURCE_TABLE) " +
 						"VALUES (?,?,?,-1,-1,?,'NATURA2000','desigc')";
             	this.preparedStatementSiteRelatedDesignations = con.prepareStatement(insertSiteRelatedDesignations);
             	
             	String insertReportType = "INSERT INTO chm62edt_report_type " +
     					"(ID_REPORT_TYPE, ID_LOOKUP, LOOKUP_TYPE) VALUES (?,?,?)";
             	this.preparedStatementReportType = con.prepareStatement(insertReportType);
             	
             	con.setAutoCommit(false); 
                 parseDocument();
                 con.commit(); 
             } 
             catch ( Exception e ) 
             { 
                 con.rollback(); 
                 con.commit(); 
 
                 if(siteCode != null)
         			errors.add("Error! Site ID: "+siteCode+" Error Message: "+e.getMessage());
         		else
         			errors.add("Error Message: "+e.getMessage());
                 //throw new IllegalArgumentException(e.getMessage(), e); 
             } 
             finally 
             { 
             	if(preparedStatementNatObject != null) 
             		preparedStatementNatObject.close();
             	
             	if(preparedStatementUpdateNatObject != null) 
             		preparedStatementUpdateNatObject.close();
             	
             	if(preparedStatementSiteInsert != null) 
             		preparedStatementSiteInsert.close();
             	
                 if(preparedStatementSite != null) 
                 	preparedStatementSite.close();
                 
                 if(preparedStatementBioRegion != null) 
                 	preparedStatementBioRegion.close();
                 
                 if(preparedStatementNatObjectReportType != null) 
                 	preparedStatementNatObjectReportType.close();
                 
                 if(preparedStatementNatObjectGeoscope != null) 
                 	preparedStatementNatObjectGeoscope.close();
                 
                 if(preparedStatementReportAttribute != null) 
                 	preparedStatementReportAttribute.close();
                 
                 if(preparedStatementSiteSites != null) 
                 	preparedStatementSiteSites.close();
                 
                 if(preparedStatementSiteAttribute != null) 
                 	preparedStatementSiteAttribute.close();
                 
                 if(preparedStatementSiteRelatedDesignations != null) 
                 	preparedStatementSiteRelatedDesignations.close();
                 
                 if(preparedStatementReportType != null) 
                 	preparedStatementReportType.close();
                 
                 if(con != null) 
                 	con.close();
             }
             
             return errors;
         
         }
         
         private String getHabitatNatObjectId(String habCode) {
         	String query = "SELECT ID_NATURE_OBJECT FROM chm62edt_habitat WHERE CODE_2000='"+habCode+"'";
         	String noId = sqlUtilities.ExecuteSQL(query);
         	
         	return noId;
         }
         
         private void getSiteNatObjectId() {
         	String query = "SELECT ID_NATURE_OBJECT FROM chm62edt_sites WHERE ID_SITE='"+siteCode+"'";
         	siteNatureObjectId = sqlUtilities.ExecuteSQL(query);
         	if(siteNatureObjectId == null || siteNatureObjectId.length() == 0){
         		newSite = true;
         		maxNoIdInt++;
         		siteNatureObjectId = new Integer(maxNoIdInt).toString();
         	}
         }
                 
         private void deleteOldRecords() throws Exception {
         	
         	PreparedStatement ps = null;
         	try{
         		
         		String query = "DELETE FROM CHM62EDT_SITE_ATTRIBUTES WHERE ID_SITE = ?";
 	    		ps = con.prepareStatement(query);
 	    		ps.setString(1, siteCode);
 	    		ps.executeUpdate();
 	    		
 	    		query = "DELETE FROM CHM62EDT_SITES_SITES WHERE ID_SITE = ?";
 	    		ps = con.prepareStatement(query);
 	    		ps.setString(1, siteCode);
 	    		ps.executeUpdate();
 	    		
 	    		query = "DELETE FROM CHM62EDT_SITES_RELATED_DESIGNATIONS WHERE ID_SITE = ?";
 	    		ps = con.prepareStatement(query);
 	    		ps.setString(1, siteCode);
 	    		ps.executeUpdate();
 	    		
 	    		query = "DELETE FROM CHM62EDT_NATURE_OBJECT_REPORT_TYPE WHERE ID_NATURE_OBJECT = ?";
 	    		ps = con.prepareStatement(query);
 	    		ps.setString(1, siteNatureObjectId);
 	    		ps.executeUpdate();
 	    		
 	    		query = "DELETE FROM CHM62EDT_NATURE_OBJECT_GEOSCOPE WHERE ID_NATURE_OBJECT = ?";
 	    		ps = con.prepareStatement(query);
 	    		ps.setString(1, siteNatureObjectId);
 	    		ps.executeUpdate();
 	    		
 	    		query = "DELETE FROM CHM62EDT_REPORTS WHERE ID_NATURE_OBJECT = ?";
 	    		ps = con.prepareStatement(query);
 	    		ps.setString(1, siteNatureObjectId);
 	    		ps.executeUpdate();
 	    		
         	} catch(Exception e) { 
                 throw new IllegalArgumentException(e.getMessage(), e); 
             } finally { 
             	if(ps != null) 
             		ps.close();
             }
         }
         
         private String getGeoscopeIdEu() throws Exception {
         	String ret = sqlUtilities.ExecuteSQL("SELECT ID_GEOSCOPE FROM CHM62EDT_COUNTRY WHERE EUNIS_AREA_CODE = 'EU' ORDER BY ID_GEOSCOPE");
         	if(ret == null || ret.length() == 0)
         		ret = "-1";
         	return ret;
         }
         
         private String getGeoscopeId() {
         	String query = "SELECT ID_GEOSCOPE FROM CHM62EDT_COUNTRY WHERE ISO_2L = '"+siteCode.substring(0,2)+"' ORDER BY ID_GEOSCOPE";
         	String geoId = sqlUtilities.ExecuteSQL(query);
         	
         	return geoId;
         }
         
         private int getMaxId(String query) throws ParseException {
         	String maxId = sqlUtilities.ExecuteSQL(query);
         	int maxIdInt = 0;
         	if(maxId != null && maxId.length()>0)
         		maxIdInt = new Integer(maxId).intValue();
         	
         	return maxIdInt;
         }
         
         private String getSpeciesNatObjectId(String speciesCode) {
         	String query = "SELECT ID_NATURE_OBJECT FROM chm62edt_nature_object_attributes WHERE NAME = '_natura2000Code' AND OBJECT = '"+speciesCode+"'";
         	String noId = sqlUtilities.ExecuteSQL(query);
         	
         	return noId;
         }
         
         private String getSpeciesNatObjectIdByName(String sciName) {
         	String query = "SELECT ID_NATURE_OBJECT FROM CHM62EDT_SPECIES WHERE SCIENTIFIC_NAME='"+sciName+"'";
         	String noId = sqlUtilities.ExecuteSQL(query);
         	
         	return noId;
         }
         
         private void updateSiteDescriptionHabitatClasses(String value, String siteId, String cover) throws Exception {
         	String code_query = "SELECT SUBSTRING(name,length(name) - instr(reverse(name),'_') + 2) AS CODE " +
         			"FROM chm62edt_site_attributes WHERE ID_SITE = '"+siteId+"' AND VALUE = '"+value+"'";
         	String code = sqlUtilities.ExecuteSQL(code_query);
         	
         	PreparedStatement ps = null;
         	try{
 	        	if(code != null && code.length() > 0){
 	        		String updateCover_query = "UPDATE chm62edt_site_attributes SET VALUE = ? WHERE ID_SITE = ? AND NAME = ?";
 	        		ps = con.prepareStatement(updateCover_query);
 	        		ps.setString(1, cover);
 	        		ps.setString(2, siteId);
 	        		ps.setString(3, "HABITAT_COVER_"+code);
 	        		
 	        		ps.executeUpdate();
 	        	}
         	} catch(Exception e) { 
                 throw new IllegalArgumentException(e.getMessage(), e); 
             } finally { 
             	if(ps != null) 
             		ps.close();
             }
         }
                 
         private void setSitesTab(String idNatureObject, String tabName) throws Exception {
         	PreparedStatement ps = null;
         	try{
         		if(tabName.equals(Constants.SITES_TAB_GENERAL)){
         			String query = "INSERT IGNORE INTO chm62edt_tab_page_sites(ID_NATURE_OBJECT,GENERAL_INFORMATION) VALUES(?,'Y')";
         			ps = con.prepareStatement(query);
             	} else {
             		String query = "UPDATE chm62edt_tab_page_sites SET "+tabName+"='Y' WHERE ID_NATURE_OBJECT=?";
         			ps = con.prepareStatement(query);
             	}
         		ps.setString(1, idNatureObject);
         		ps.executeUpdate();
         		        		
         	} catch(Exception e) { 
                 throw new IllegalArgumentException(e.getMessage(), e); 
             } finally { 
             	if(ps != null) 
             		ps.close();
             }
         	
         }
         
         private void setHabitatTabSites(String habitatIdNatureObject) throws Exception {
         	PreparedStatement ps = null;
         	try{
            		String query = "UPDATE chm62edt_tab_page_habitats SET SITES='Y' WHERE ID_NATURE_OBJECT = ?";
        			ps = con.prepareStatement(query);
         		ps.setString(1, habitatIdNatureObject);
         		ps.executeUpdate();
         		        		
         	} catch(Exception e) { 
                 throw new IllegalArgumentException(e.getMessage(), e); 
             } finally { 
             	if(ps != null) 
             		ps.close();
             }
         }
         
         private void setSpeciesTabSites(String speciesIdNatureObject) throws Exception {
         	PreparedStatement ps = null;
         	try{
            		String query = "UPDATE chm62edt_tab_page_species SET SITES='Y' WHERE ID_NATURE_OBJECT = ?";
        			ps = con.prepareStatement(query);
         		ps.setString(1, speciesIdNatureObject);
         		ps.executeUpdate();
         		        		
         	} catch(Exception e) { 
                 throw new IllegalArgumentException(e.getMessage(), e); 
             } finally { 
             	if(ps != null) 
             		ps.close();
             }
         }
         
         private void calculateLatitudeParams(){
         	latNs = "S";
 			latDeg = "";
 			latMin = "";
 			latSec = "";
 			
 			if(latitude != null && latitude.length() > 0){
 				//calculate LAT_NS
 				double lat_num = new Double(latitude).doubleValue();
 				if(lat_num > 0)
 					latNs = "N";
 				
 				//calculate LAT_DEG
 				int index = latitude.indexOf(".");
     			if(index != -1){
     				latDeg = latitude.substring(0, index);
     				String rest = latitude.substring(index+1, latitude.length());
     				
     				//calculate LAT_MIN
     				if(rest != null && rest.length() > 0){
     					rest = "0."+rest;
     					double d = new Double(rest).doubleValue();
     					double min = d * 60;
     					String minString = new Double(min).toString();
     					int indexMin = minString.indexOf(".");
     					if(indexMin != -1){
             				latMin = minString.substring(0, indexMin);
             				String restMin = minString.substring(indexMin+1, minString.length());
             				
             				//calculate LAT_SEC
             				if(restMin != null && restMin.length() > 0){
             					restMin = "0."+restMin;
             					double dsec = new Double(restMin).doubleValue();
             					double sec = dsec * 60;
             					String secString = new Double(sec).toString();
             					int indexSec = secString.indexOf(".");
             					if(indexSec != -1){
                     				latSec = secString.substring(0, indexSec);
             					} else {
             						latSec = secString;
             					}
             				}
     					} else {
     						latMin = minString;
     					}
     				}
     			} else {
     				latDeg = latitude;
     			}
 			}
         }
         
         private void calculateLongitudeParams(){
         	lonEw = "W";
 			lonDeg = "";
 			lonMin = "";
 			lonSec = "";
 			
 			if(longitude != null && longitude.length() > 0){
 				
 				//calculate LON_EW
 				double lon_num = new Double(longitude).doubleValue();
 				if(lon_num > 0)
 					lonEw = "E";
 				
 				//calculate LON_DEG
 				int index = longitude.indexOf(".");
     			if(index != -1){
     				lonDeg = longitude.substring(0, index);
     				String rest = longitude.substring(index+1, longitude.length());
     				
     				//calculate LON_MIN
     				if(rest != null && rest.length() > 0){
     					rest = "0."+rest;
     					double d = new Double(rest).doubleValue();
     					double min = d * 60;
     					String minString = new Double(min).toString();
     					int indexMin = minString.indexOf(".");
     					if(indexMin != -1){
             				lonMin = minString.substring(0, indexMin);
             				String restMin = minString.substring(indexMin+1, minString.length());
             				
             				//calculate LON_SEC
             				if(restMin != null && restMin.length() > 0){
             					restMin = "0."+restMin;
             					double dsec = new Double(restMin).doubleValue();
             					double sec = dsec * 60;
             					String secString = new Double(sec).toString();
             					int indexSec = secString.indexOf(".");
             					if(indexSec != -1){
                     				lonSec = secString.substring(0, indexSec);
             					} else {
             						lonSec = secString;
             					}
             				}
     					} else {
     						lonMin = minString;
     					}
     				}
     			} else {
     				lonDeg = longitude;
     			}
 			}
         }
         
         private String parseDate(String input) {
         	String ret = "";
         	if(input != null && input.length() > 0){
         		if(input.contains("T00:00:00")){
         			int end = input.indexOf("T");
         			String date = input.substring(0, end);
         			ret = date.replace("-","");
         		}
         	}
         	return ret;
         }
 } 
