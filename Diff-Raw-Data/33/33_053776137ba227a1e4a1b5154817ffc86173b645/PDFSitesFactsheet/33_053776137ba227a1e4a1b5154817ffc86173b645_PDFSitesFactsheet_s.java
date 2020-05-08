 package ro.finsiel.eunis.factsheet;
 
 import com.lowagie.text.*;
 import com.lowagie.text.Font;
 import ro.finsiel.eunis.WebContentManagement;
 import ro.finsiel.eunis.factsheet.sites.SiteFactsheet;
 import ro.finsiel.eunis.jrfTables.Chm62edtReportAttributesPersist;
 import ro.finsiel.eunis.jrfTables.Chm62edtSitesAttributesPersist;
 import ro.finsiel.eunis.jrfTables.DesignationsSitesRelatedDesignationsPersist;
 import ro.finsiel.eunis.jrfTables.sites.factsheet.*;
 import ro.finsiel.eunis.reports.pdfReport;
 import ro.finsiel.eunis.search.Utilities;
 import ro.finsiel.eunis.search.sites.SitesSearchUtility;
 import ro.finsiel.eunis.utilities.SQLUtilities;
 
 import java.awt.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 
 /**
  * Site factsheet in PDF format.
  */
 public class PDFSitesFactsheet
 {
   private static final float TABLE_WIDTH = 94f;
 
   private WebContentManagement contentManagement = null;
   private final String SQL_DRV;
   private final String SQL_URL;
   private final String SQL_USR;
   private final String SQL_PWD;
 
   private SiteFactsheet factsheet = null;
   private pdfReport report = null;
   int type = -1;
   private String designationDescr = null;
 
   Font fontNormal = FontFactory.getFont( FontFactory.HELVETICA, 9, Font.NORMAL );
   Font fontNormalBold = FontFactory.getFont( FontFactory.HELVETICA, 9, Font.BOLD );
   Font fontTitle = FontFactory.getFont( FontFactory.HELVETICA, 12, Font.BOLD );
   Font fontSubtitle = FontFactory.getFont( FontFactory.HELVETICA, 10, Font.BOLD );
 
   /**
    * Constructor for PDFSitesFactsheet object.
    * @param idSite ID_SITE
    * @param report Report to write to, already initialized
    * @param contentManagement current content management
    * @param SQL_DRV SQL Driver
    * @param SQL_URL SQL Driver URL
    * @param SQL_USR SQL Driver username
    * @param SQL_PWD SQL Driver password
    */
   public PDFSitesFactsheet( String idSite, pdfReport report, WebContentManagement contentManagement,
                             String SQL_DRV, String SQL_URL, String SQL_USR, String SQL_PWD )
   {
     this.contentManagement = contentManagement;
     this.report = report;
     this.SQL_DRV = SQL_DRV;
     this.SQL_URL = SQL_URL;
     this.SQL_USR = SQL_USR;
     this.SQL_PWD = SQL_PWD;
 
 
     factsheet = new SiteFactsheet( idSite );
     type = factsheet.getType();
     try
     {
       designationDescr = factsheet.getDesignation();
     }
     catch ( Exception ex )
     {
       ex.printStackTrace();
     }
   }
 
   /**
    * Generate entire factsheet.
    * @return operation status
    */
   public final boolean generateFactsheet()
   {
     boolean ret = true;
     try
     {
       getGeneralInformation();
       report.getDocument().newPage();
 
       getDesignations();
       report.getDocument().newPage();
 
       getFaunaFlora();
       report.getDocument().newPage();
 
       getHabitats();
       report.getDocument().newPage();
 
       getRelatedSites();
       report.getDocument().newPage();
 
       getOtherInfo();
     }
     catch ( Exception ex )
     {
       ret = false;
       ex.printStackTrace();
     }
     return ret;
   }
 
   private void getDesignations() throws Exception
   {
     if ( type == SiteFactsheet.TYPE_NATURA2000 || type == SiteFactsheet.TYPE_EMERALD || type == SiteFactsheet.TYPE_CORINE )
     {
       List sitesDesigc;
       if ( type == SiteFactsheet.TYPE_NATURA2000 || type == SiteFactsheet.TYPE_EMERALD )
       {
         sitesDesigc = factsheet.findSiteRelationsNatura2000Desigc();
       }
       else
       {
         //CORINE
         sitesDesigc = factsheet.findSiteRelationsCorine();
       }
       //2nd table should be called "National and/or International Designation of Natura 2000 site"
       // and should display information from desigc table decoded with desig-x table.
       //Columns should then be desigcode, descript with a link to Site module designation fact sheet (should always exist),
       // category and cover.
       if ( sitesDesigc.size() > 0 )
       {
         Table table = new Table( 4 );
         table.setCellsFitPage( true );
         table.setWidth( TABLE_WIDTH );
         table.setAlignment( Table.ALIGN_LEFT );
         table.setBorderWidth( 1 );
         table.setDefaultCellBorderWidth( 1 );
         table.setBorderColor( Color.BLACK );
         table.setCellspacing( 2 );
 
         float[] colWidths1 = { 20, 40, 30, 10 };
         table.setWidths( colWidths1 );
         Cell cell;
 
         cell = new Cell( new Phrase( "National and/or International Designation of Natura 2000 site", fontTitle ) );
         cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
         cell.setColspan( 4 );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( "Designation code", fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( "Designation name", fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( "Category", fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( "Cover(%)", fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         for ( int i = 0; i < sitesDesigc.size(); i++ )
         {
           DesignationsSitesRelatedDesignationsPersist desig = ( DesignationsSitesRelatedDesignationsPersist ) sitesDesigc.get( i );
 
           cell = new Cell( new Phrase( desig.getDescription(), fontNormal ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( desig.getDescriptionEn(), fontNormal ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( Utilities.formatString( desig.getNationalCategory() ), fontNormal ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( Utilities.formatDecimal( desig.getOverlap(), 2 ), fontNormal ) );
           table.addCell( cell );
         }
         report.addTable( table );
       }
 
       //Third table should be called "Relation with designated areas" and should display information
       // from desigr table decoded with desig-x table.
       //Columns should then be des_site with link to "http://eunis.eea.eu.int/sites-names.jsp" prefilled with site name
       // and data sets CDDA National, European Diploma, CDDA International and Biogenetic Reserve selected,
       // descript with a link to Site module designation fact sheet (should always exist),
       // category, overlap and overlap_p.
       List sitesDesigr = new Vector();
       if ( type == SiteFactsheet.TYPE_NATURA2000 || type == SiteFactsheet.TYPE_EMERALD )
       {
         sitesDesigr = factsheet.findSiteRelationsNatura2000Desigr();
       }
       if ( sitesDesigr.size() > 0 )
       {
         Table table = new Table( 5 );
         table.setCellsFitPage( true );
         table.setWidth( TABLE_WIDTH );
         table.setAlignment( Table.ALIGN_LEFT );
         table.setBorderWidth( 1 );
         table.setDefaultCellBorderWidth( 1 );
         table.setBorderColor( Color.BLACK );
         table.setCellspacing( 2 );
 
         float[] colWidths1 = { 20, 40, 20, 10, 10 };
         table.setWidths( colWidths1 );
         Cell cell;
 
         cell = new Cell( new Phrase( "Relation with designated areas", fontTitle ) );
         cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
         cell.setColspan( 5 );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( "Designated site", fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( "Designation name", fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( "Category", fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( "Overlap", fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( "Overlap P", fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         for ( int i = 0; i < sitesDesigr.size(); i++ )
         {
           DesignationsSitesRelatedDesignationsPersist desig = ( DesignationsSitesRelatedDesignationsPersist ) sitesDesigr.get( i );
 
           cell = new Cell( new Phrase( Utilities.formatString( desig.getDesignatedSite() ), fontNormal ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( Utilities.formatString( desig.getDescriptionEn(), " " ), fontNormal ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( Utilities.formatString( desig.getNationalCategory() ), fontNormal ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( Utilities.formatDecimal( desig.getOverlap(), 2 ), fontNormal ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( Utilities.formatString( desig.getOverlapType() ), fontNormal ) );
           table.addCell( cell );
         }
         report.addTable( table );
       }
     }
   }
 
   private void getFaunaFlora() throws Exception
   {
     SQLUtilities sqlc = new SQLUtilities();
     sqlc.Init( SQL_DRV, SQL_URL, SQL_USR, SQL_PWD );
 
     String respondent = factsheet.getSiteObject().getRespondent();
     String author = factsheet.getAuthor();
     String manager = factsheet.getSiteObject().getManager();
     String information = factsheet.getSiteObject().getRespondent();
     String officialContactInternational = factsheet.getOfficialContactInternational();
     String officialContactNational = factsheet.getOfficialContactNational();
     String officialContactRegional = factsheet.getOfficialContactRegional();
     String officialContactLocal = factsheet.getOfficialContactLocal();
     if ( ( !respondent.equalsIgnoreCase( "" ) || !author.equalsIgnoreCase( "" ) || !manager.equalsIgnoreCase( "" ) ||
         !information.equalsIgnoreCase( "" ) || !officialContactInternational.equalsIgnoreCase( "" ) ||
         !officialContactNational.equalsIgnoreCase( "" ) || !officialContactRegional.equalsIgnoreCase( "" ) ||
         !officialContactLocal.equalsIgnoreCase( "" )
     ) && (
         SiteFactsheet.TYPE_NATURA2000 == type || SiteFactsheet.TYPE_EMERALD == type ||
             SiteFactsheet.TYPE_CORINE == type || SiteFactsheet.TYPE_BIOGENETIC == type ||
             SiteFactsheet.TYPE_DIPLOMA == type ) )
     {
       //Site contact authorities
       Table table = new Table( 2 );
       table.setCellsFitPage( true );
       table.setWidth( TABLE_WIDTH );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setBorderWidth( 1 );
       table.setDefaultCellBorderWidth( 1 );
       table.setBorderColor( Color.BLACK );
       table.setCellspacing( 2 );
 
       float[] colWidths1 = { 30, 70 };
       table.setWidths( colWidths1 );
       Cell cell;
 
       cell = new Cell( new Phrase( contentManagement.cms( "site_contact_authorities"), fontSubtitle ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       cell.setColspan( 2 );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( contentManagement.cms( "respondent"), fontNormal ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( respondent, fontNormal ) );
       table.addCell( cell );
 
       if ( SiteFactsheet.TYPE_BIOGENETIC == type )
       {
         cell = new Cell( new Phrase( contentManagement.cms( "author"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( author, fontNormal ) );
         table.addCell( cell );
       }
 
       if ( SiteFactsheet.TYPE_NATURA2000 == type ||
           SiteFactsheet.TYPE_EMERALD == type ||
           SiteFactsheet.TYPE_DIPLOMA == type ||
           SiteFactsheet.TYPE_BIOGENETIC == type )
       {
         cell = new Cell( new Phrase( contentManagement.cms( "manager"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( manager, fontNormal ) );
         table.addCell( cell );
       }
 
       if ( SiteFactsheet.TYPE_DIPLOMA == type )
       {
         cell = new Cell( new Phrase( contentManagement.cms( "information"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( information, fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "official_contact_international"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( officialContactInternational, fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "official_contact_national"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( officialContactNational, fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "official_contact_regional"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( officialContactRegional, fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "official_contact_local"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( officialContactLocal, fontNormal ) );
         table.addCell( cell );
       }
 
       report.addTable( table );
     }
 
     String character = factsheet.getSiteObject().getCharacter();
     String quality = factsheet.getSiteObject().getQuality();
     String vulnerability = factsheet.getSiteObject().getVulnerability();
     String designation = ( null != designationDescr ) ? designationDescr : " ";
     String ownership = factsheet.getSiteObject().getOwnership();
     String documentation = factsheet.getSiteObject().getDocumentation();
     String characterization = factsheet.getHabitatCharacterization();
     String floraCharacterization = factsheet.getFloraCharacterization();
     String faunaCharacterization = factsheet.getFaunaCharacterization();
     String potentialVegetation = factsheet.getPotentialVegetation();
     String geomorphology = factsheet.getGeomorphology();
     String educationalInterest = factsheet.getEducationalInterest();
     String culturalHeritage = factsheet.getCulturalHeritage();
     String justification = factsheet.getJustification();
     String methodology = factsheet.getMethodology();
     String budget = factsheet.getBudget();
     String managementPlan = factsheet.getSiteObject().getManagementPlan();
     String urlOfficial = factsheet.getURLOfficial();
     String urlInteresting = factsheet.getURLInteresting();
     if ( !character.equalsIgnoreCase( "" ) || !quality.equalsIgnoreCase( "" ) ||
         !vulnerability.equalsIgnoreCase( "" ) ||
         (
             !designation.equalsIgnoreCase( "" ) &&
                 (
                     SiteFactsheet.TYPE_CDDA_NATIONAL != type &&
                         SiteFactsheet.TYPE_CDDA_INTERNATIONAL != type
                 )
         ) ||
         !ownership.equalsIgnoreCase( "" ) || !documentation.equalsIgnoreCase( "" ) ||
         !characterization.equalsIgnoreCase( "" ) || !floraCharacterization.equalsIgnoreCase( "" ) ||
         !faunaCharacterization.equalsIgnoreCase( "" ) || !potentialVegetation.equalsIgnoreCase( "" ) ||
         !geomorphology.equalsIgnoreCase( "" ) || !educationalInterest.equalsIgnoreCase( "" ) ||
         !culturalHeritage.equalsIgnoreCase( "" ) || !justification.equalsIgnoreCase( "" ) ||
         !methodology.equalsIgnoreCase( "" ) || !budget.equalsIgnoreCase( "" ) ||
         !managementPlan.equalsIgnoreCase( "" ) || !urlOfficial.equalsIgnoreCase( "" ) ||
         !urlInteresting.equalsIgnoreCase( "" ) )
     {
       Table table = new Table( 2 );
       table.setCellsFitPage( true );
       table.setWidth( TABLE_WIDTH );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setBorderWidth( 1 );
       table.setDefaultCellBorderWidth( 1 );
       table.setBorderColor( Color.BLACK );
       table.setCellspacing( 2 );
 
       float[] colWidths1 = { 30, 70 };
       table.setWidths( colWidths1 );
       Cell cell;
 
       cell = new Cell( new Phrase( "Description", fontTitle ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       cell.setColspan( 2 );
       table.addCell( cell );
 
       if ( SiteFactsheet.TYPE_NATURA2000 == type || SiteFactsheet.TYPE_EMERALD == type ||
           SiteFactsheet.TYPE_DIPLOMA == type || SiteFactsheet.TYPE_CORINE == type )
       {
         cell = new Cell( new Phrase( contentManagement.cms( "general_character_of_site"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( character, fontNormal ) );
         table.addCell( cell );
       }
       if ( SiteFactsheet.TYPE_CDDA_NATIONAL != type && SiteFactsheet.TYPE_CDDA_INTERNATIONAL != type )
       {
         cell = new Cell( new Phrase( contentManagement.cms( "quality"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( quality, fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "vulnerability"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( vulnerability, fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "designation"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( designation, fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "owner"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( ownership, fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "documentation"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( documentation, fontNormal ) );
         table.addCell( cell );
       }
       if ( SiteFactsheet.TYPE_BIOGENETIC == type || SiteFactsheet.TYPE_DIPLOMA == type )
       {
         cell = new Cell( new Phrase( contentManagement.cms( "habitat_types"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( characterization, fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "flora"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( floraCharacterization, fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "fauna"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( faunaCharacterization, fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "potential_vegetation"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( potentialVegetation, fontNormal ) );
         table.addCell( cell );
       }
       if ( SiteFactsheet.TYPE_DIPLOMA == type )
       {
         cell = new Cell( new Phrase( contentManagement.cms( "geomorphology"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( geomorphology, fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "educational_interest"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( educationalInterest, fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "cultural_heritage"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( culturalHeritage, fontNormal ) );
         table.addCell( cell );
       }
       if ( SiteFactsheet.TYPE_DIPLOMA == type || SiteFactsheet.TYPE_CORINE == type )
       {
         cell = new Cell( new Phrase( contentManagement.cms( "justification"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( justification, fontNormal ) );
         table.addCell( cell );
       }
       if ( SiteFactsheet.TYPE_DIPLOMA == type )
       {
         cell = new Cell( new Phrase( contentManagement.cms( "methodology"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( methodology, fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "budget"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( budget, fontNormal ) );
         table.addCell( cell );
       }
       if ( SiteFactsheet.TYPE_NATURA2000 == type ||
           SiteFactsheet.TYPE_EMERALD == type ||
           SiteFactsheet.TYPE_DIPLOMA == type ||
           SiteFactsheet.TYPE_BIOGENETIC == type )
       {
         cell = new Cell( new Phrase( contentManagement.cms( "management_plan"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( managementPlan, fontNormal ) );
         table.addCell( cell );
       }
       if ( SiteFactsheet.TYPE_DIPLOMA == type || SiteFactsheet.TYPE_CDDA_INTERNATIONAL == type )
       {
         cell = new Cell( new Phrase( contentManagement.cms( "url_official"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( urlOfficial, fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "url_interesting"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( urlInteresting, fontNormal ) );
         table.addCell( cell );
       }
       report.addTable( table );
     }
 
     //1. everything but Natura 2000
     if ( SiteFactsheet.TYPE_EMERALD == type ||
         SiteFactsheet.TYPE_DIPLOMA == type ||
         SiteFactsheet.TYPE_BIOGENETIC == type ||
         SiteFactsheet.TYPE_CORINE == type )
     {
       //list of species recognized in EUNIS
       List species = factsheet.findSitesSpeciesByIDNatureObject();
       //list of species not recognized in EUNIS
       List sitesSpecificspecies = factsheet.findSitesSpecificSpecies();
       if ( !species.isEmpty() || !sitesSpecificspecies.isEmpty() )
       {
         Chm62edtReportAttributesPersist attribute;
         if ( species.size() > 0 )
         {
           //Site contact authorities
           Table table = new Table( 13 );
           table.setCellsFitPage( true );
           table.setWidth( TABLE_WIDTH );
           table.setAlignment( Table.ALIGN_LEFT );
           table.setBorderWidth( 1 );
           table.setDefaultCellBorderWidth( 1 );
           table.setBorderColor( Color.BLACK );
           table.setCellspacing( 2 );
 
           float[] colWidths1 = { 20, 10, 10, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6 };
           table.setWidths( colWidths1 );
           Cell cell;
 
           cell = new Cell( new Phrase( contentManagement.cms( "ecological_information_fauna_flora"), fontTitle ) );
           cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
           cell.setColspan( 13 );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( contentManagement.cms( "species_scientific_name"), fontNormalBold ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( contentManagement.cms( "species_group"), fontNormalBold ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( contentManagement.cms( "resident"), fontNormalBold ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( contentManagement.cms( "breeding"), fontNormalBold ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_94"), fontNormalBold ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( contentManagement.cms( "staging"), fontNormalBold ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_96"), fontNormalBold ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( contentManagement.cms( "species"), fontNormalBold ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_98"), fontNormalBold ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_99"), fontNormalBold ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( contentManagement.cms( "isolation"), fontNormalBold ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_101"), fontNormalBold ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( contentManagement.cms( "species_status"), fontNormalBold ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           for ( int i = 0; i < species.size(); i++ )
           {
             SiteSpeciesPersist specie = ( SiteSpeciesPersist ) species.get( i );
 
             cell = new Cell( new Phrase( specie.getSpeciesScientificName(), fontNormal ) );
             table.addCell( cell );
 
             cell = new Cell( new Phrase( specie.getSpeciesCommonName(), fontNormal ) );
             table.addCell( cell );
 
             attribute = factsheet.findSiteAttributes( "BREEDING", specie.getIdReportAttributes() );
             cell = new Cell( new Phrase( ( null != attribute ) ? ( ( null != attribute.getValue() ) ? attribute.getValue() : " " ) : " ", fontNormal ) );
             table.addCell( cell );
 
             attribute = factsheet.findSiteAttributes( "RESIDENT", specie.getIdReportAttributes() );
             cell = new Cell( new Phrase( ( null != attribute ) ? ( ( null != attribute.getValue() ) ? attribute.getValue() : " " ) : " ", fontNormal ) );
             table.addCell( cell );
 
             attribute = factsheet.findSiteAttributes( "WINTERING", specie.getIdReportAttributes() );
             cell = new Cell( new Phrase( ( null != attribute ) ? ( ( null != attribute.getValue() ) ? attribute.getValue() : " " ) : " ", fontNormal ) );
             table.addCell( cell );
 
             attribute = factsheet.findSiteAttributes( "STAGING", specie.getIdReportAttributes() );
             cell = new Cell( new Phrase( ( null != attribute ) ? ( ( null != attribute.getValue() ) ? attribute.getValue() : " " ) : " ", fontNormal ) );
             table.addCell( cell );
 
             attribute = factsheet.findSiteAttributes( "POPULATION", specie.getIdReportAttributes() );
             cell = new Cell( new Phrase( ( null != attribute ) ? ( ( null != attribute.getValue() ) ? attribute.getValue() : " " ) : " ", fontNormal ) );
             table.addCell( cell );
 
             attribute = factsheet.findSiteAttributes( "MIGRATION", specie.getIdReportAttributes() );
             cell = new Cell( new Phrase( ( null != attribute ) ? ( ( null != attribute.getValue() ) ? attribute.getValue() : " " ) : " ", fontNormal ) );
             table.addCell( cell );
 
             attribute = factsheet.findSiteAttributes( "NESTING", specie.getIdReportAttributes() );
             cell = new Cell( new Phrase( ( null != attribute ) ? ( ( null != attribute.getValue() ) ? attribute.getValue() : " " ) : " ", fontNormal ) );
             table.addCell( cell );
 
             attribute = factsheet.findSiteAttributes( "CONSERVATION", specie.getIdReportAttributes() );
             cell = new Cell( new Phrase( ( null != attribute ) ? ( ( null != attribute.getValue() ) ? attribute.getValue() : " " ) : " ", fontNormal ) );
             table.addCell( cell );
 
             attribute = factsheet.findSiteAttributes( "ISOLATION", specie.getIdReportAttributes() );
             cell = new Cell( new Phrase( ( null != attribute ) ? ( ( null != attribute.getValue() ) ? attribute.getValue() : " " ) : " ", fontNormal ) );
             table.addCell( cell );
 
             attribute = factsheet.findSiteAttributes( "GLOBAL", specie.getIdReportAttributes() );
             cell = new Cell( new Phrase( ( null != attribute ) ? ( ( null != attribute.getValue() ) ? attribute.getValue() : " " ) : " ", fontNormal ) );
             table.addCell( cell );
 
             attribute = factsheet.findSiteAttributes( "SPECIES_STATUS", specie.getIdReportAttributes() );
             cell = new Cell( new Phrase( ( null != attribute ) ? ( ( null != attribute.getValue() ) ? attribute.getValue() : " " ) : " ", fontNormal ) );
             table.addCell( cell );
           }
           report.addTable( table );
         }
       }
       if ( sitesSpecificspecies.size() > 0 )
       {
         Table table = new Table( 1 );
         table.setCellsFitPage( true );
         table.setWidth( TABLE_WIDTH );
         table.setAlignment( Table.ALIGN_LEFT );
         table.setBorderWidth( 1 );
         table.setDefaultCellBorderWidth( 1 );
         table.setBorderColor( Color.BLACK );
         table.setCellspacing( 2 );
 
         Cell cell;
         cell = new Cell( new Phrase( contentManagement.cms( "other_species_mentioned_in_site"), fontTitle ) );
         cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "species_scientific_name"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         // Here I get the species which are only specific to site
         for ( int i = 0; i < sitesSpecificspecies.size(); i++ )
         {
           Chm62edtSitesAttributesPersist specie = ( Chm62edtSitesAttributesPersist ) sitesSpecificspecies.get( i );
 
           cell = new Cell( new Phrase( specie.getValue(), fontNormal ) );
           table.addCell( cell );
         }
         report.addTable( table );
       }
     }
 
     //2. only for Natura 2000
     if ( SiteFactsheet.TYPE_NATURA2000 == type )
     {
       List eunisSpeciesListedAnnexesDirectives = factsheet.findEunisSpeciesListedAnnexesDirectivesForSitesNatura2000();
       List eunisSpeciesOtherMentioned = factsheet.findEunisSpeciesOtherMentionedForSitesNatura2000();
       List notEunisSpeciesListedAnnexesDirectives = factsheet.findNotEunisSpeciesListedAnnexesDirectives();
       List notEunisSpeciesOtherMentioned = factsheet.findNotEunisSpeciesOtherMentioned();
 
       Chm62edtReportAttributesPersist attribute;
       Cell cell;
       if ( !eunisSpeciesListedAnnexesDirectives.isEmpty() || !notEunisSpeciesListedAnnexesDirectives.isEmpty() )
       {
         Table table = new Table( 10 );
         table.setCellsFitPage( true );
         table.setWidth( TABLE_WIDTH );
         table.setAlignment( Table.ALIGN_LEFT );
         table.setBorderWidth( 1 );
         table.setDefaultCellBorderWidth( 1 );
         table.setBorderColor( Color.BLACK );
         table.setCellspacing( 2 );
 
         float[] colWidths1 = { 20, 10, 10, 9, 9, 9, 9, 8, 8, 8 };
         table.setWidths( colWidths1 );
 
 
         cell = new Cell( new Phrase( contentManagement.cms( "species"), fontTitle ) );
         cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
         cell.setColspan( 10 );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "species_scientific_name"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "species_group"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "resident"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "breeding"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_94"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "staging"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_99"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_96"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "isolation"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_101"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         for ( int i = 0; i < eunisSpeciesListedAnnexesDirectives.size(); i++ )
         {
           SitesSpeciesReportAttributesPersist specie = ( SitesSpeciesReportAttributesPersist ) eunisSpeciesListedAnnexesDirectives.get( i );
 
           cell = new Cell( new Phrase( specie.getSpeciesScientificName(), fontNormal ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( specie.getSpeciesCommonName(), fontNormal ) );
           table.addCell( cell );
 
           attribute = factsheet.findSiteAttributes( "RESIDENT", specie.getIdReportAttributes() );
           cell = new Cell( new Phrase( ( null != attribute ) ? ( ( null != attribute.getValue() ) ? attribute.getValue() : " " ) : " ", fontNormal ) );
           table.addCell( cell );
 
           attribute = factsheet.findSiteAttributes( "BREEDING", specie.getIdReportAttributes() );
           cell = new Cell( new Phrase( ( null != attribute ) ? ( ( null != attribute.getValue() ) ? attribute.getValue() : " " ) : " ", fontNormal ) );
           table.addCell( cell );
 
           attribute = factsheet.findSiteAttributes( "WINTERING", specie.getIdReportAttributes() );
           cell = new Cell( new Phrase( ( null != attribute ) ? ( ( null != attribute.getValue() ) ? attribute.getValue() : " " ) : " ", fontNormal ) );
           table.addCell( cell );
 
           attribute = factsheet.findSiteAttributes( "STAGING", specie.getIdReportAttributes() );
           cell = new Cell( new Phrase( ( null != attribute ) ? ( ( null != attribute.getValue() ) ? attribute.getValue() : " " ) : " ", fontNormal ) );
           table.addCell( cell );
 
           attribute = factsheet.findSiteAttributes( "CONSERVATION", specie.getIdReportAttributes() );
           cell = new Cell( new Phrase( ( null != attribute ) ? ( ( null != attribute.getValue() ) ? attribute.getValue() : " " ) : " ", fontNormal ) );
           table.addCell( cell );
 
           attribute = factsheet.findSiteAttributes( "POPULATION", specie.getIdReportAttributes() );
           cell = new Cell( new Phrase( ( null != attribute ) ? ( ( null != attribute.getValue() ) ? attribute.getValue() : " " ) : " ", fontNormal ) );
           table.addCell( cell );
 
           attribute = factsheet.findSiteAttributes( "ISOLATION", specie.getIdReportAttributes() );
           cell = new Cell( new Phrase( ( null != attribute ) ? ( ( null != attribute.getValue() ) ? attribute.getValue() : " " ) : " ", fontNormal ) );
           table.addCell( cell );
 
           attribute = factsheet.findSiteAttributes( "GLOBAL", specie.getIdReportAttributes() );
           cell = new Cell( new Phrase( ( null != attribute ) ? ( ( null != attribute.getValue() ) ? attribute.getValue() : " " ) : " ", fontNormal ) );
           table.addCell( cell );
         }
 
         Chm62edtSitesAttributesPersist attribute2;
         for ( int i = 0; i < notEunisSpeciesListedAnnexesDirectives.size(); i++ )
         {
           Chm62edtSitesAttributesPersist specie = ( Chm62edtSitesAttributesPersist ) notEunisSpeciesListedAnnexesDirectives.get( i );
           String specName = specie.getName();
           specName = ( specName == null ? " " : specName.substring( specName.lastIndexOf( "_" ) + 1 ) );
           String groupName = specie.getSourceTable();
           groupName = ( groupName == null ? " " : ( groupName.equalsIgnoreCase( "amprep" ) ? "Amphibians"
               : ( groupName.equalsIgnoreCase( "bird" ) ? "Birds"
               : ( groupName.equalsIgnoreCase( "fishes" ) ? "Fishes"
               : ( groupName.equalsIgnoreCase( "invert" ) ? "Invertebrates"
               : ( groupName.equalsIgnoreCase( "mammal" ) ? "Mammals"
               : ( groupName.equalsIgnoreCase( "plant" ) ? "Flowering Plants" : " " ) ) ) ) ) ) );
 
           cell = new Cell( new Phrase( specName, fontNormal ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( groupName, fontNormal ) );
           table.addCell( cell );
 
           attribute2 = factsheet.findNotEunisSpeciesListedAnnexesDirectivesAttributes( "RESIDENT_" + specName );
           cell = new Cell( new Phrase( ( null != attribute2 ) ? ( ( null != attribute2.getValue() ) ? attribute2.getValue() : " " ) : " ", fontNormal ) );
           table.addCell( cell );
 
           attribute2 = factsheet.findNotEunisSpeciesListedAnnexesDirectivesAttributes( "BREEDING_" + specName );
           cell = new Cell( new Phrase( ( null != attribute2 ) ? ( ( null != attribute2.getValue() ) ? attribute2.getValue() : " " ) : " ", fontNormal ) );
           table.addCell( cell );
 
           attribute2 = factsheet.findNotEunisSpeciesListedAnnexesDirectivesAttributes( "WINTERING_" + specName );
           cell = new Cell( new Phrase( ( null != attribute2 ) ? ( ( null != attribute2.getValue() ) ? attribute2.getValue() : " " ) : " ", fontNormal ) );
           table.addCell( cell );
 
           attribute2 = factsheet.findNotEunisSpeciesListedAnnexesDirectivesAttributes( "STAGING_" + specName );
           cell = new Cell( new Phrase( ( null != attribute2 ) ? ( ( null != attribute2.getValue() ) ? attribute2.getValue() : " " ) : " ", fontNormal ) );
           table.addCell( cell );
 
           attribute2 = factsheet.findNotEunisSpeciesListedAnnexesDirectivesAttributes( "CONSERVATION_" + specName );
           cell = new Cell( new Phrase( ( null != attribute2 ) ? ( ( null != attribute2.getValue() ) ? attribute2.getValue() : " " ) : " ", fontNormal ) );
           table.addCell( cell );
 
           attribute2 = factsheet.findNotEunisSpeciesListedAnnexesDirectivesAttributes( "POPULATION_" + specName );
           cell = new Cell( new Phrase( ( null != attribute2 ) ? ( ( null != attribute2.getValue() ) ? attribute2.getValue() : " " ) : " ", fontNormal ) );
           table.addCell( cell );
 
           attribute2 = factsheet.findNotEunisSpeciesListedAnnexesDirectivesAttributes( "ISOLATION_" + specName );
           cell = new Cell( new Phrase( ( null != attribute2 ) ? ( ( null != attribute2.getValue() ) ? attribute2.getValue() : " " ) : " ", fontNormal ) );
           table.addCell( cell );
 
           attribute2 = factsheet.findNotEunisSpeciesListedAnnexesDirectivesAttributes( "GLOBAL_" + specName );
           cell = new Cell( new Phrase( ( null != attribute2 ) ? ( ( null != attribute2.getValue() ) ? attribute2.getValue() : " " ) : " ", fontNormal ) );
           table.addCell( cell );
 
         }
         report.addTable( table );
       }
 
       if ( !eunisSpeciesOtherMentioned.isEmpty() || !notEunisSpeciesOtherMentioned.isEmpty() )
       {
         Table table = new Table( 4 );
         table.setCellsFitPage( true );
         table.setWidth( TABLE_WIDTH );
         table.setAlignment( Table.ALIGN_LEFT );
         table.setBorderWidth( 1 );
         table.setDefaultCellBorderWidth( 1 );
         table.setBorderColor( Color.BLACK );
         table.setCellspacing( 2 );
 
         float[] colWidths1 = { 30, 20, 25, 25 };
         table.setWidths( colWidths1 );
 
         cell = new Cell( new Phrase( contentManagement.cms( "other_species_mentioned_in_site"), fontTitle ) );
         cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
         cell.setColspan( 4 );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "species_group"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "species_name"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "population_size_estimations"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "motivation_for_species_mention"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         for ( int i = 0; i < eunisSpeciesOtherMentioned.size(); i++ )
         {
           SitesSpeciesReportAttributesPersist specie = ( SitesSpeciesReportAttributesPersist ) eunisSpeciesOtherMentioned.get( i );
 
           cell = new Cell( new Phrase( specie.getSpeciesCommonName(), fontNormal ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( specie.getSpeciesScientificName(), fontNormal ) );
           table.addCell( cell );
 
           attribute = factsheet.findSiteAttributes( "OTHER_POPULATION", specie.getIdReportAttributes() );
           cell = new Cell( new Phrase( ( null != attribute ) ? ( ( null != attribute.getValue() ) ? attribute.getValue() : " " ) : " ", fontNormal ) );
           table.addCell( cell );
 
           attribute = factsheet.findSiteAttributes( "OTHER_MOTIVATION", specie.getIdReportAttributes() );
           cell = new Cell( new Phrase( ( null != attribute ) ? ( ( null != attribute.getValue() ) ? attribute.getValue() : " " ) : " ", fontNormal ) );
           table.addCell( cell );
         }
         Chm62edtSitesAttributesPersist attribute2;
         for ( int i = 0; i < notEunisSpeciesOtherMentioned.size(); i++ )
         {
           Chm62edtSitesAttributesPersist specie = ( Chm62edtSitesAttributesPersist ) notEunisSpeciesOtherMentioned.get( i );
           String specName = specie.getName();
           specName = ( specName == null ? " " : specName.substring( specName.lastIndexOf( "_" ) + 1 ) );
           attribute2 = factsheet.findNotEunisSpeciesOtherMentionedAttributes( "TAXGROUP_" + specName );
           String groupName = ( null != attribute2 ) ? ( ( null != attribute2.getValue() ) ? attribute2.getValue() : " " ) : " ";
           groupName = ( groupName == null ? " " : ( groupName.equalsIgnoreCase( "P" ) ? "Plants"
               : ( groupName.equalsIgnoreCase( "A" ) ? "Amphibians"
               : ( groupName.equalsIgnoreCase( "F" ) ? "Fishes"
               : ( groupName.equalsIgnoreCase( "I" ) ? "Invertebrates"
               : ( groupName.equalsIgnoreCase( "M" ) ? "Mammals"
               : ( groupName.equalsIgnoreCase( "B" ) ? "Birds"
               : ( groupName.equalsIgnoreCase( "F" ) ? "Flowering"
               : ( groupName.equalsIgnoreCase( "R" ) ? "Reptiles" : " " ) ) ) ) ) ) ) ) );
 
           cell = new Cell( new Phrase( groupName, fontNormal ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( specName, fontNormal ) );
           table.addCell( cell );
 
           attribute2 = factsheet.findNotEunisSpeciesOtherMentionedAttributes( "POPULATION_" + specName );
           cell = new Cell( new Phrase( ( null != attribute2 ) ? ( ( null != attribute2.getValue() ) ? attribute2.getValue() : " " ) : " ", fontNormal ) );
           table.addCell( cell );
 
           attribute2 = factsheet.findNotEunisSpeciesOtherMentionedAttributes( "MOTIVATION_" + specName );
           cell = new Cell( new Phrase( ( null != attribute2 ) ? ( ( null != attribute2.getValue() ) ? attribute2.getValue() : " " ) : " ", fontNormal ) );
           table.addCell( cell );
         }
         report.addTable( table );
       }
     }
   }
 
   private void getGeneralInformation() throws Exception
   {
 
     Table table = new Table( 1 );
     table.setCellsFitPage( true );
     table.setWidth( TABLE_WIDTH );
     table.setAlignment( Table.ALIGN_LEFT );
     table.setBorderWidth( 1 );
     table.setDefaultCellBorderWidth( 1 );
     table.setBorderColor( Color.BLACK );
     table.setCellspacing( 2 );
 
     String sdb = SitesSearchUtility.translateSourceDB(factsheet.getSiteObject().getSourceDB());
 
     Cell cell;
     cell = new Cell( new Phrase( factsheet.getSiteObject().getName(), fontTitle ) );
     cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
     cell.setHorizontalAlignment( Cell.ALIGN_CENTER );
     table.addCell( cell );
     cell = new Cell( new Phrase( contentManagement.cms("facthseet_filled_with_data_from") + " " + sdb + " " + contentManagement.cms("sites_factsheet_02"), fontNormalBold ) );
     cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
     cell.setHorizontalAlignment( Cell.ALIGN_CENTER );
     table.addCell( cell );
 
 
     report.addTable( table );
     report.writeln( " " );
 
     table = new Table( 2 );
     table.setCellsFitPage( true );
     table.setWidth( TABLE_WIDTH );
     table.setAlignment( Table.ALIGN_LEFT );
     table.setBorderWidth( 1 );
     table.setDefaultCellBorderWidth( 1 );
     table.setBorderColor( Color.BLACK );
     table.setCellspacing( 2 );
 
     float[] colWidths1 = { 50, 50 };
     table.setWidths( colWidths1 );
 
     cell = new Cell( new Phrase( contentManagement.cms("site_identification"), fontSubtitle ) );
     cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
     cell.setColspan( 2 );
     table.addCell( cell );
 
 
     cell = new Cell( new Phrase( SitesSearchUtility.translateSourceDB( factsheet.getSiteObject().getSourceDB() ) + " " + contentManagement.cms( "code_in_database"), fontNormal ) );
     table.addCell( cell );
 
     cell = new Cell( new Phrase( factsheet.getIDSite(), fontNormal ) );
     table.addCell( cell );
 
     cell = new Cell( new Phrase( contentManagement.cms( "surface_area_ha"), fontNormal ) );
     table.addCell( cell );
 
     cell = new Cell( new Phrase( Utilities.formatArea( factsheet.getSiteObject().getArea(), 0, 2, " ", null ), fontNormal ) );
     table.addCell( cell );
 
     if ( SiteFactsheet.TYPE_NATURA2000 == type || SiteFactsheet.TYPE_EMERALD == type || SiteFactsheet.TYPE_BIOGENETIC == type )
     {
       if ( factsheet.getSiteObject().getLength() != null )
       {
         cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_06"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( factsheet.getSiteObject().getLength(), fontNormal ) );
         table.addCell( cell );
       }
     }
     // Complex name
     cell = new Cell( new Phrase( contentManagement.cms( "complex_name"), fontNormal ) );
     table.addCell( cell );
 
     cell = new Cell( new Phrase( factsheet.getSiteObject().getComplexName(), fontNormal ) );
     table.addCell( cell );
 
     // District name
     cell = new Cell( new Phrase( contentManagement.cms( "district_name"), fontNormal ) );
     table.addCell( cell );
 
     cell = new Cell( new Phrase( factsheet.getSiteObject().getDistrictName(), fontNormal ) );
     table.addCell( cell );
 
     if ( SiteFactsheet.TYPE_CDDA_NATIONAL != type && SiteFactsheet.TYPE_CDDA_INTERNATIONAL != type )
     {
       String dateformatCompilationDate = " ";
       String dateformatCompilationDate2 = " ";
       if ( SiteFactsheet.TYPE_NATURA2000 != type || type == SiteFactsheet.TYPE_EMERALD )
       {
         if ( factsheet.getSiteObject().getCompilationDate().length() == 4 )
         {
           dateformatCompilationDate = "(yyyy)";
         }
         if ( factsheet.getSiteObject().getCompilationDate().length() == 6 )
         {
           dateformatCompilationDate = "(yyyyMM)";
         }
         if ( factsheet.getSiteObject().getCompilationDate().length() == 8 )
         {
           dateformatCompilationDate = "(yyyyMMdd)";
         }
       }
       else
       {
         if ( factsheet.getSiteObject().getCompilationDate().length() == 4 )
         {
           dateformatCompilationDate2 = "yyyy";
         }
         if ( factsheet.getSiteObject().getCompilationDate().length() == 6 )
         {
           dateformatCompilationDate2 = "yyyyMM";
         }
         if ( factsheet.getSiteObject().getCompilationDate().length() == 8 )
         {
           dateformatCompilationDate2 = "yyyyMMdd";
         }
       }
 
       String dateformatUpdateDate = " ";
       String dateformatUpdateDate2 = " ";
       if ( SiteFactsheet.TYPE_NATURA2000 != type || type == SiteFactsheet.TYPE_EMERALD )
       {
         if ( factsheet.getSiteObject().getUpdateDate().length() == 4 )
         {
           dateformatUpdateDate = "(yyyy)";
         }
         if ( factsheet.getSiteObject().getUpdateDate().length() == 6 )
         {
           dateformatUpdateDate = "(yyyyMM)";
         }
         if ( factsheet.getSiteObject().getUpdateDate().length() == 8 )
         {
           dateformatUpdateDate = "(yyyyMMdd)";
         }
       }
       else
       {
         if ( factsheet.getSiteObject().getUpdateDate().length() == 4 )
         {
           dateformatUpdateDate2 = "yyyy";
         }
         if ( factsheet.getSiteObject().getUpdateDate().length() == 6 )
         {
           dateformatUpdateDate2 = "yyyyMM";
         }
         if ( factsheet.getSiteObject().getUpdateDate().length() == 8 )
         {
           dateformatUpdateDate2 = "yyyyMMdd";
         }
       }
       // Date form compilation date
       cell = new Cell( new Phrase( contentManagement.cms( "date_form_compilation_date") + " " + dateformatCompilationDate, fontNormal ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase(
           SiteFactsheet.TYPE_NATURA2000 != type ?
               factsheet.getSiteObject().getCompilationDate() :
               Utilities.formatDate( Utilities.stringToTimeStamp(
                   factsheet.getSiteObject().getCompilationDate(), dateformatCompilationDate2 ), "MMM yyyy" ) +
                   (
                       factsheet.getSiteObject().getCompilationDate() == null || factsheet.getSiteObject().getCompilationDate().trim().length() <= 0
                           ? " " :
                           " (" + contentManagement.cms( "sites_factsheet_170") + " " + factsheet.getSiteObject().getCompilationDate() + ")" ), fontNormal ) );
       table.addCell( cell );
 
       // Date form update
       cell = new Cell( new Phrase( contentManagement.cms( "surface_area_ha") + " " + dateformatUpdateDate, fontNormal ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase(
           SiteFactsheet.TYPE_NATURA2000 != type ?
               factsheet.getSiteObject().getUpdateDate() :
               Utilities.formatDate( Utilities.stringToTimeStamp(
                   factsheet.getSiteObject().getUpdateDate(), dateformatUpdateDate2 ), "MMM yyyy" ) +
                   (
                       factsheet.getSiteObject().getUpdateDate() == null || factsheet.getSiteObject().getUpdateDate().trim().length() <= 0 ?
                           " " :
                           " (" + contentManagement.cms( "sites_factsheet_170") + " " + factsheet.getSiteObject().getUpdateDate() + ")" ), fontNormal ) );
       table.addCell( cell );
     }
     if ( SiteFactsheet.TYPE_CDDA_NATIONAL != type && SiteFactsheet.TYPE_CDDA_INTERNATIONAL != type && SiteFactsheet.TYPE_CORINE != type )
     {
       // Date proposed
       cell = new Cell( new Phrase( contentManagement.cms( "date_proposed"), fontNormal ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase(
           SiteFactsheet.TYPE_NATURA2000 != type ?
               factsheet.getSiteObject().getProposedDate() :
               Utilities.formatDate( Utilities.stringToTimeStamp( factsheet.getSiteObject().getProposedDate(), "yyyyMM" ), "MMM yyyy" ) +
                   (
                       factsheet.getSiteObject().getProposedDate() == null || factsheet.getSiteObject().getProposedDate().trim().length() <= 0 ?
                           " " :
                           " (" + contentManagement.cms( "sites_factsheet_170") + " " + factsheet.getSiteObject().getProposedDate() + ")" ), fontNormal ) );
       table.addCell( cell );
     }
     if ( SiteFactsheet.TYPE_NATURA2000 == type || SiteFactsheet.TYPE_DIPLOMA == type || type == SiteFactsheet.TYPE_EMERALD )
     {
       // Date confirmed
       cell = new Cell( new Phrase( contentManagement.cms( "date_proposed"), fontNormal ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase(
           SiteFactsheet.TYPE_NATURA2000 != type ?
               factsheet.getSiteObject().getConfirmedDate() :
               Utilities.formatDate( Utilities.stringToTimeStamp( factsheet.getSiteObject().getConfirmedDate(), "yyyyMM" ), "MMM yyyy" ) +
                   ( factsheet.getSiteObject().getConfirmedDate() == null || factsheet.getSiteObject().getConfirmedDate().trim().length() <= 0 ?
                       " " :
                       " (" + contentManagement.cms( "sites_factsheet_170") + " " + factsheet.getSiteObject().getConfirmedDate() + ")" ), fontNormal ) );
       table.addCell( cell );
     }
     if ( SiteFactsheet.TYPE_DIPLOMA == type )
     {
       // Date first designation
       cell = new Cell( new Phrase( contentManagement.cms( "date_first_designation"), fontNormal ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( factsheet.getDateFirstDesignation(), fontNormal ) );
       table.addCell( cell );
     }
     if ( SiteFactsheet.TYPE_CORINE != type )
     {
       // Date first designation
       cell = new Cell( new Phrase( contentManagement.cms( "site_designation_date"), fontNormal ) );
       table.addCell( cell );
 
       String str = " ";
       String spaDate = factsheet.getSiteObject().getSpaDate();
       String sacDate = factsheet.getSiteObject().getSacDate();
       String designationDate = factsheet.getSiteObject().getDesignationDate();
       if ( null != spaDate && spaDate.length() > 0 )
       {
         str += spaDate + ",";
       }
       if ( null != sacDate && sacDate.length() > 0 )
       {
         str += sacDate + "/";
       }
       if ( null != designationDate && designationDate.length() > 0 )
       {
         str += designationDate;
       }
 
       cell = new Cell( new Phrase( str, fontNormal ) );
       table.addCell( cell );
     }
     report.addTable( table );
 
     // Site designations
     List designations = SitesSearchUtility.findDesignationsForSitesFactsheet( factsheet.getSiteObject().getIdSite() );
     if ( designations != null && designations.size() > 0 )
     {
       table = new Table( SiteFactsheet.TYPE_CORINE != type ? 5 : 4 );
       table.setCellsFitPage( true );
       table.setWidth( TABLE_WIDTH );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setBorderWidth( 1 );
       table.setDefaultCellBorderWidth( 1 );
       table.setBorderColor( Color.BLACK );
       table.setCellspacing( 2 );
 
 
       float[] colWidths2 = { 30, 20, 30, 20 };
       float[] colWidths3 = { 30, 15, 20, 20, 20 };
       if ( SiteFactsheet.TYPE_CORINE != type )
       {
         table.setWidths( colWidths3 );
       }
       else
       {
         table.setWidths( colWidths2 );
       }
 
       cell = new Cell( new Phrase( contentManagement.cms( "designation_information"), fontTitle ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       cell.setColspan( SiteFactsheet.TYPE_CORINE != type ? 5 : 4 );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( contentManagement.cms( "source_data_set"), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( contentManagement.cms( "designation_code"), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( contentManagement.cms( "designation_name_original"), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( contentManagement.cms( "designation_name_english"), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       if ( SiteFactsheet.TYPE_CORINE != type )
       {
         cell = new Cell( new Phrase( contentManagement.cms( "designation_name_french"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
       }
 
       SitesDesignationsPersist designation = ( SitesDesignationsPersist ) designations.get( 0 );
       cell = new Cell( new Phrase( designation.getDataSource(), fontNormal ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( Utilities.formatString( designation.getIdDesignation(), " " ), fontNormal ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( designation.getDescription(), fontNormal ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( designation.getDescriptionEn(), fontNormal ) );
       table.addCell( cell );
 
       if ( SiteFactsheet.TYPE_CORINE != type )
       {
         cell = new Cell( new Phrase( designation.getDescriptionFr(), fontNormal ) );
         table.addCell( cell );
       }
       report.addTable( table );
     }
 
     // Project ID, title
     table = new Table( 1 );
     table.setCellsFitPage( true );
     table.setWidth( TABLE_WIDTH );
     table.setAlignment( Table.ALIGN_LEFT );
     table.setBorderWidth( 1 );
     table.setDefaultCellBorderWidth( 1 );
     table.setBorderColor( Color.BLACK );
     table.setCellspacing( 2 );
 
     cell = new Cell( new Phrase( contentManagement.cms( "project_id"), fontNormal ) );
     cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
     table.addCell( cell );
 
     cell = new Cell( new Phrase( contentManagement.cms( "project_title"), fontNormal ) );
     cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
     table.addCell( cell );
     report.addTable( table );
 
     // Monitoring activities
     table = new Table( 1 );
     table.setCellsFitPage( true );
     table.setWidth( TABLE_WIDTH );
     table.setAlignment( Table.ALIGN_LEFT );
     table.setBorderWidth( 1 );
     table.setDefaultCellBorderWidth( 1 );
     table.setBorderColor( Color.BLACK );
     table.setCellspacing( 2 );
 
     cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_24"), fontNormal ) );
     cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
     table.addCell( cell );
     report.addTable( table );
 
     // Location information
     String country = Utilities.formatString( factsheet.getCountry() ).trim();
     String parentCountry = Utilities.formatString( factsheet.getParentCountry() ).trim();
 
     table = new Table( 9 );
     table.setCellsFitPage( true );
     table.setWidth( TABLE_WIDTH );
     table.setAlignment( Table.ALIGN_LEFT );
     table.setBorderWidth( 1 );
     table.setDefaultCellBorderWidth( 1 );
     table.setBorderColor( Color.BLACK );
     table.setCellspacing( 2 );
 
     float[] colWidths = { 20, 10, 10, 10, 10, 10, 10, 10, 10 };
     table.setWidths( colWidths );
 
     cell = new Cell( new Phrase( contentManagement.cms( "location_information"), fontSubtitle ) );
     cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
     cell.setColspan( 9 );
     table.addCell( cell );
 
     cell = new Cell( new Phrase( contentManagement.cms( "country"), fontNormal ) );
     cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
     cell.setColspan( 2 );
     table.addCell( cell );
 
     cell = new Cell( new Phrase( Utilities.formatString( country, " " ), fontNormal ) );
     cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
     table.addCell( cell );
 
     if ( !country.equalsIgnoreCase( parentCountry ) )
     {
       cell = new Cell( new Phrase( contentManagement.cms( "parent_country"), fontNormal ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       cell.setColspan( 2 );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( parentCountry, fontNormal ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
     }
     else
     {
       cell = new Cell( new Phrase( " ", fontNormal ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       cell.setColspan( 2 );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( " ", fontNormal ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
     }
 
     if ( SiteFactsheet.TYPE_CDDA_INTERNATIONAL != type )
     {
       List regionCodes = factsheet.findAdministrativeRegionCodes();
 
       cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_30"), fontNormal ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       cell.setColspan( 2 );
       table.addCell( cell );
 
       String txt = "NUTS code ";
       if ( regionCodes.size() > 0 )
       {
         for ( int i = 0; i < regionCodes.size(); i++ )
         {
           RegionsCodesPersist region = ( RegionsCodesPersist ) regionCodes.get( i );
           txt += Utilities.formatString( region.getRegionCode() ) + ", " + Utilities.formatString( region.getRegionName() ) +
               ", cover: " + Utilities.formatString( region.getRegionCover() ) + "%";
           if ( regionCodes.size() > 1 && i < regionCodes.size() - 1 )
           {
             txt += " ";
           }
         }
         cell = new Cell( new Phrase( txt, fontNormal ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
       }
     }
     report.addTable( table );
 
     // Site biogeographical regions
     if ( SiteFactsheet.TYPE_NATURA2000 == type || type == SiteFactsheet.TYPE_EMERALD )
     {
       boolean alpine = Utilities.checkedStringToBoolean( factsheet.findSiteAttribute( "ALPINE" ), false );
       boolean anatol = Utilities.checkedStringToBoolean( factsheet.findSiteAttribute( "ANATOL" ), false );
       boolean arctic = Utilities.checkedStringToBoolean( factsheet.findSiteAttribute( "ARCTIC" ), false );
       boolean atlantic = Utilities.checkedStringToBoolean( factsheet.findSiteAttribute( "ATLANTIC" ), false );
       boolean boreal = Utilities.checkedStringToBoolean( factsheet.findSiteAttribute( "BOREAL" ), false );
       boolean continent = Utilities.checkedStringToBoolean( factsheet.findSiteAttribute( "CONTINENT" ), false );
       boolean macarones = Utilities.checkedStringToBoolean( factsheet.findSiteAttribute( "MACARONES" ), false );
       boolean mediterranean = Utilities.checkedStringToBoolean( factsheet.findSiteAttribute( "MEDITERRANIAN" ), false );
       boolean pannonic = Utilities.checkedStringToBoolean( factsheet.findSiteAttribute( "PANNONIC" ), false );
       boolean pontic = Utilities.checkedStringToBoolean( factsheet.findSiteAttribute( "PONTIC" ), false );
       boolean steppic = Utilities.checkedStringToBoolean( factsheet.findSiteAttribute( "STEPPIC" ), false );
       if ( alpine ||
           anatol ||
           arctic ||
           atlantic ||
           boreal ||
           continent ||
           macarones ||
           mediterranean ||
           pannonic ||
           pontic ||
           steppic )
       {
         table = new Table( 12 );
         table.setCellsFitPage( true );
         table.setWidth( TABLE_WIDTH );
         table.setAlignment( Table.ALIGN_LEFT );
         table.setBorderWidth( 1 );
         table.setDefaultCellBorderWidth( 1 );
         table.setBorderColor( Color.BLACK );
         table.setCellspacing( 2 );
 
         float[] colWidths3 = { 10, 10, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8 };
         table.setWidths( colWidths3 );
 
         cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_149"), fontTitle ) );
         cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
         cell.setColspan( 12 );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "biogeographic_region"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_152"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_153"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_154"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_155"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_156"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_157"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_158"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_159"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_160"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_161"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_162"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         // ROW
 
         cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_151"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( alpine ? "Yes" : " ", fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( anatol ? "Yes" : " ", fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( arctic ? "Yes" : " ", fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( atlantic ? "Yes" : " ", fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( boreal ? "Yes" : " ", fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( continent ? "Yes" : " ", fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( macarones ? "Yes" : " ", fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( mediterranean ? "Yes" : " ", fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( pannonic ? "Yes" : " ", fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( pontic ? "Yes" : " ", fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( steppic ? "Yes" : " ", fontNormal ) );
         table.addCell( cell );
 
         report.addTable( table );
       }
 
       // Altitude
 
       String altMin = factsheet.getSiteObject().getAltMin();
       String altMax = factsheet.getSiteObject().getAltMax();
       String altMean = factsheet.getSiteObject().getAltMean();
       if ( SiteFactsheet.TYPE_CORINE == type ) // For CORINE BIOTOPES, -99 means invalide altitude value
       {
         if ( altMin != null && altMin.equalsIgnoreCase( "-99" ) )
         {
           altMin = " ";
         }
         if ( altMax != null && altMax.equalsIgnoreCase( "-99" ) )
         {
           altMax = " ";
         }
         if ( altMean != null && altMean.equalsIgnoreCase( "-99" ) )
         {
           altMean = " ";
         }
       }
 
      report.writeln( contentManagement.cms( "minimum_altitude") + ":" + Utilities.formatString( altMin ), fontNormal );
      report.writeln( contentManagement.cms( "mean_altitude_m") + ":" + Utilities.formatString( altMean ), fontNormal );
      report.writeln( contentManagement.cms( "maximum_altitude_m") + ":" + Utilities.formatString( altMax ), fontNormal );
 
       // Coordinates
       String longitude;
       String latitude;
       if ( SiteFactsheet.TYPE_CORINE != type )
       {
         latitude = SitesSearchUtility.formatCoordinatesPDF( factsheet.getSiteObject().getLatNS(),
             factsheet.getSiteObject().getLatDeg(),
             factsheet.getSiteObject().getLatMin(),
             factsheet.getSiteObject().getLatSec() );
         longitude = SitesSearchUtility.formatCoordinatesPDF( factsheet.getSiteObject().getLongEW(),
             factsheet.getSiteObject().getLongDeg(),
             factsheet.getSiteObject().getLongMin(),
             factsheet.getSiteObject().getLongSec() );
       }
       else
       {
         latitude = SitesSearchUtility.formatCoordinatesPDF( "N", factsheet.getSiteObject().getLatDeg(),
             factsheet.getSiteObject().getLatMin(),
             factsheet.getSiteObject().getLatSec() );
         longitude = SitesSearchUtility.formatCoordinatesPDF( factsheet.getSiteObject().getLongEW(),
             factsheet.getSiteObject().getLongDeg(),
             factsheet.getSiteObject().getLongMin(),
             factsheet.getSiteObject().getLongSec() );
       }
 
      report.writeln( contentManagement.cms( "longitude") + ":" + longitude, fontNormal );
      report.writeln( contentManagement.cms( "latitude") + ":" + latitude, fontNormal );
       report.writeln( " ", fontNormal );
      report.writeln( contentManagement.cms( "sites_factsheet_36") + ":" + Utilities.formatAreaPDF( factsheet.getSiteObject().getLongitude(), 0, 6, null ), fontNormal );
      report.writeln( contentManagement.cms( "sites_factsheet_37") + ":" + Utilities.formatAreaPDF( factsheet.getSiteObject().getLatitude(), 0, 6, null ), fontNormal );
 
 
       if ( SiteFactsheet.TYPE_DIPLOMA == type || SiteFactsheet.TYPE_BIOGENETIC == type || SiteFactsheet.TYPE_CORINE == type )
       {
         List results = factsheet.getBiogeoregion();
         if ( results.size() > 0 )
         {
           table = new Table( 2 );
           table.setCellsFitPage( true );
           table.setWidth( TABLE_WIDTH );
           table.setAlignment( Table.ALIGN_LEFT );
           table.setBorderWidth( 1 );
           table.setDefaultCellBorderWidth( 1 );
           table.setBorderColor( Color.BLACK );
           table.setCellspacing( 2 );
 
           float[] colWidths4 = { 50, 50 };
           table.setWidths( colWidths4 );
 
           cell = new Cell( new Phrase( contentManagement.cms( "biogeographic_regions"), fontNormalBold ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           String txt = " ";
           for ( int i = 0; i < results.size(); i++ )
           {
             Chm62edtSitesAttributesPersist persist = ( Chm62edtSitesAttributesPersist ) results.get( i );
             txt += persist.getValue() + ",";
           }
           cell = new Cell( new Phrase( txt, fontNormal ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
           report.addTable( table );
         }
       }
     }
   }
 
   private void getHabitats() throws Exception
   {
     SQLUtilities sqlc = new SQLUtilities();
     sqlc.Init( SQL_DRV, SQL_URL, SQL_USR, SQL_PWD );
 
     List habit1Eunis = new ArrayList();
     List habit1NotEunis = new ArrayList();
     List habits2Eunis = new ArrayList();
     List habits2NotEunis = new ArrayList();
 
     List habitats = new ArrayList();
     List sitesSpecificHabitats = new ArrayList();
 
     if ( type == SiteFactsheet.TYPE_NATURA2000 || type == SiteFactsheet.TYPE_EMERALD )
     {
       habit1Eunis = factsheet.findHabit1Eunis();
       habit1NotEunis = factsheet.findHabit1NotEunis();
       habits2Eunis = factsheet.findHabit2Eunis();
       habits2NotEunis = factsheet.findHabit2NotEunis();
     }
     else
     {
       habitats = factsheet.findSitesHabitatsByIDNatureObject();
       sitesSpecificHabitats = factsheet.findSitesSpecificHabitats();
     }
 
     if ( ( SiteFactsheet.TYPE_NATURA2000 == type || type == SiteFactsheet.TYPE_EMERALD && ( !habit1Eunis.isEmpty() || !habit1NotEunis.isEmpty() || !habits2Eunis.isEmpty() || !habits2NotEunis.isEmpty() ) )
         || ( !habitats.isEmpty() || !sitesSpecificHabitats.isEmpty() ) )
     {
       // List of habitats related to site
       if ( SiteFactsheet.TYPE_NATURA2000 == type || type == SiteFactsheet.TYPE_EMERALD &&
           ( !habit1Eunis.isEmpty() || !habit1NotEunis.isEmpty() || !habits2Eunis.isEmpty() || !habits2NotEunis.isEmpty() ) )
       {
         Chm62edtReportAttributesPersist attribute;
 
         if ( !habit1Eunis.isEmpty() || !habit1NotEunis.isEmpty() )
         {
           Table table = new Table( 7 );
           table.setCellsFitPage( true );
           table.setWidth( TABLE_WIDTH );
           table.setAlignment( Table.ALIGN_LEFT );
           table.setBorderWidth( 1 );
           table.setDefaultCellBorderWidth( 1 );
           table.setBorderColor( Color.BLACK );
           table.setCellspacing( 2 );
 
           float[] colWidths1 = { 20, 20, 20, 10, 10, 10, 10 };
           table.setWidths( colWidths1 );
           Cell cell;
 
           cell = new Cell( new Phrase( "Ecological information: Habitats within site", fontTitle ) );
           cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
           cell.setColspan( 7 );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( "Code", fontNormal ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( contentManagement.cms( "english_name"), fontNormalBold ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( contentManagement.cms( "cover_percent"), fontNormalBold ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( contentManagement.cms( "representativity"), fontNormalBold ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_118"), fontNormalBold ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( contentManagement.cms( "conservation"), fontNormalBold ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( contentManagement.cms( "global"), fontNormalBold ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           if ( !habit1Eunis.isEmpty() )
           {
             //System.out.println("habit1Eunis = " + habit1Eunis.size());
             for ( int i = 0; i < habit1Eunis.size(); i++ )
             {
               SiteHabitatsPersist habitat = ( SiteHabitatsPersist ) habit1Eunis.get( i );
 
               cell = new Cell( new Phrase( habitat.getIdHabitat(), fontNormal ) );
               table.addCell( cell );
 
               cell = new Cell( new Phrase( habitat.getHabitatDescription(), fontNormal ) );
               table.addCell( cell );
 
               attribute = factsheet.findSiteAttributes( "COVER", habitat.getIdReportAttributes() );
               cell = new Cell( new Phrase( ( null != attribute ) ? Utilities.formatDecimal( attribute.getValue(), 2 ) : " ", fontNormal ) );
               table.addCell( cell );
 
               attribute = factsheet.findSiteAttributes( "REPRESENTATIVITY", habitat.getIdReportAttributes() );
               cell = new Cell( new Phrase( ( null != attribute ) ? Utilities.formatString( attribute.getValue() ) : " ", fontNormal ) );
               table.addCell( cell );
 
               attribute = factsheet.findSiteAttributes( "RELATIVE_SURFACE", habitat.getIdReportAttributes() );
               cell = new Cell( new Phrase( ( null != attribute ) ? Utilities.formatString( attribute.getValue() ) : " ", fontNormal ) );
               table.addCell( cell );
 
               attribute = factsheet.findSiteAttributes( "CONSERVATION", habitat.getIdReportAttributes() );
               cell = new Cell( new Phrase( ( null != attribute ) ? Utilities.formatString( attribute.getValue() ) : " ", fontNormal ) );
               table.addCell( cell );
 
               attribute = factsheet.findSiteAttributes( "GLOBAL", habitat.getIdReportAttributes() );
               cell = new Cell( new Phrase( ( null != attribute ) ? Utilities.formatString( attribute.getValue() ) : " ", fontNormal ) );
               table.addCell( cell );
             }
           }
           if ( !habit1NotEunis.isEmpty() )
           {
             Chm62edtSitesAttributesPersist attribute2;
             for ( int i = 0; i < habit1NotEunis.size(); i++ )
             {
               Chm62edtSitesAttributesPersist habitat = ( Chm62edtSitesAttributesPersist ) habit1NotEunis.get( i );
               String habCode = habitat.getName();
               habCode = ( habCode == null ? " " : habCode.substring( habCode.lastIndexOf( "_" ) + 1 ) );
 
               cell = new Cell( new Phrase( habCode, fontNormal ) );
               table.addCell( cell );
 
               attribute2 = factsheet.findHabit1NotEunisAttributes( "NAME_EN_" + habCode );
               cell = new Cell( new Phrase( ( null != attribute2 ) ? Utilities.formatString( attribute2.getValue() ) : " ", fontNormal ) );
               table.addCell( cell );
 
               attribute2 = factsheet.findHabit1NotEunisAttributes( "COVER_" + habCode );
               cell = new Cell( new Phrase( ( null != attribute2 ) ? Utilities.formatDecimal( attribute2.getValue(), 2 ) : " ", fontNormal ) );
               table.addCell( cell );
 
               attribute2 = factsheet.findHabit1NotEunisAttributes( "REPRESENTATIVITY_" + habCode );
               cell = new Cell( new Phrase( ( null != attribute2 ) ? Utilities.formatString( attribute2.getValue() ) : " ", fontNormal ) );
               table.addCell( cell );
 
               attribute2 = factsheet.findHabit1NotEunisAttributes( "RELATIVE_SURFACE_" + habCode );
               cell = new Cell( new Phrase( ( null != attribute2 ) ? Utilities.formatString( attribute2.getValue() ) : " ", fontNormal ) );
               table.addCell( cell );
 
               attribute2 = factsheet.findHabit1NotEunisAttributes( "CONSERVATION_" + habCode );
               cell = new Cell( new Phrase( ( null != attribute2 ) ? Utilities.formatString( attribute2.getValue() ) : " ", fontNormal ) );
               table.addCell( cell );
 
               attribute2 = factsheet.findHabit1NotEunisAttributes( "GLOBAL_" + habCode );
               cell = new Cell( new Phrase( ( null != attribute2 ) ? Utilities.formatString( attribute2.getValue() ) : " ", fontNormal ) );
               table.addCell( cell );
             }
           }
           report.addTable( table );
         }
         if ( !habits2Eunis.isEmpty() || !habits2NotEunis.isEmpty() )
         {
           Table table = new Table( 3 );
           table.setCellsFitPage( true );
           table.setWidth( TABLE_WIDTH );
           table.setAlignment( Table.ALIGN_LEFT );
           table.setBorderWidth( 1 );
           table.setDefaultCellBorderWidth( 1 );
           table.setBorderColor( Color.BLACK );
           table.setCellspacing( 2 );
 
           float[] colWidths1 = { 30, 50, 20 };
           table.setWidths( colWidths1 );
           Cell cell;
 
           cell = new Cell( new Phrase( "Ecological information: Habitats within site", fontTitle ) );
           cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
           cell.setColspan( 3 );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( contentManagement.cms( "habitat_type_code"), fontNormalBold ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( contentManagement.cms( "habitat_type_english_name"), fontNormalBold ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( contentManagement.cms( "cover_percent"), fontNormalBold ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           if ( !habits2Eunis.isEmpty() )
           {
             for ( int i = 0; i < habits2Eunis.size(); i++ )
             {
               SiteHabitatsPersist habitat = ( SiteHabitatsPersist ) habits2Eunis.get( i );
 
               cell = new Cell( new Phrase( habitat.getIdHabitat(), fontNormal ) );
               table.addCell( cell );
 
               cell = new Cell( new Phrase( Utilities.formatString( habitat.getHabitatDescription(), " " ), fontNormal ) );
               table.addCell( cell );
 
               attribute = factsheet.findSiteAttributes( "COVER", habitat.getIdReportAttributes() );
               cell = new Cell( new Phrase( ( null != attribute ) ? Utilities.formatDecimal( attribute.getValue(), 2 ) : " ", fontNormal ) );
               table.addCell( cell );
             }
           }
           if ( !habits2NotEunis.isEmpty() )
           {
             Chm62edtSitesAttributesPersist attribute2;
             for ( int i = 0; i < habits2NotEunis.size(); i++ )
             {
               Chm62edtSitesAttributesPersist habitat = ( Chm62edtSitesAttributesPersist ) habits2NotEunis.get( i );
               String habCode = habitat.getName();
               habCode = ( habCode == null ? " " : habCode.substring( habCode.lastIndexOf( "_" ) + 1 ) );
 
               cell = new Cell( new Phrase( habCode, fontNormal ) );
               table.addCell( cell );
 
               attribute2 = factsheet.findHabit2NotEunisAttributes( "NAME_EN_" + habCode );
               cell = new Cell( new Phrase( ( null != attribute2 ) ? Utilities.formatString( attribute2.getValue() ) : " ", fontNormal ) );
               table.addCell( cell );
 
               attribute2 = factsheet.findHabit2NotEunisAttributes( "COVER_" + habCode );
               cell = new Cell( new Phrase( ( null != attribute2 ) ? Utilities.formatString( attribute2.getValue() ) : " ", fontNormal ) );
               table.addCell( cell );
             }
           }
           report.addTable( table );
         }
       }
       else
       {
         // List of habitats related to site not Natura2000
         if ( !habitats.isEmpty() )
         {
           Table table = new Table( 6 );
           table.setCellsFitPage( true );
           table.setWidth( TABLE_WIDTH );
           table.setAlignment( Table.ALIGN_LEFT );
           table.setBorderWidth( 1 );
           table.setDefaultCellBorderWidth( 1 );
           table.setBorderColor( Color.BLACK );
           table.setCellspacing( 2 );
 
           float[] colWidths1 = { 30, 20, 20, 10, 10, 10 };
           table.setWidths( colWidths1 );
           Cell cell;
 
           cell = new Cell( new Phrase( "Ecological information: Habitats within site", fontTitle ) );
           cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
           cell.setColspan( 6 );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( "Habitat type english name", fontNormal ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( "Cover (%)", fontNormal ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( "Representativity", fontNormal ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( "Surface (ha)", fontNormal ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( "Conservation", fontNormal ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( "Global", fontNormal ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           Chm62edtReportAttributesPersist attribute;
           for ( int i = 0; i < habitats.size(); i++ )
           {
             SiteHabitatsPersist habitat = ( SiteHabitatsPersist ) habitats.get( i );
 
             cell = new Cell( new Phrase( habitat.getHabitatDescription(), fontNormal ) );
             table.addCell( cell );
 
             attribute = factsheet.findSiteAttributes( "COVER", habitat.getIdReportAttributes() );
             cell = new Cell( new Phrase( ( null != attribute ) ? Utilities.formatDecimal( attribute.getValue(), 2 ) : " ", fontNormal ) );
             table.addCell( cell );
 
             attribute = factsheet.findSiteAttributes( "REPRESENTATIVITY", habitat.getIdReportAttributes() );
             cell = new Cell( new Phrase( ( null != attribute ) ? Utilities.formatString( attribute.getValue() ) : " ", fontNormal ) );
             table.addCell( cell );
 
             attribute = factsheet.findSiteAttributes( "SURFACE", habitat.getIdReportAttributes() );
             cell = new Cell( new Phrase( ( null != attribute ) ? Utilities.formatString( attribute.getValue() ) : " ", fontNormal ) );
             table.addCell( cell );
 
             attribute = factsheet.findSiteAttributes( "CONSERVATION", habitat.getIdReportAttributes() );
             cell = new Cell( new Phrase( ( null != attribute ) ? Utilities.formatString( attribute.getValue() ) : " ", fontNormal ) );
             table.addCell( cell );
 
             attribute = factsheet.findSiteAttributes( "GLOBAL", habitat.getIdReportAttributes() );
             cell = new Cell( new Phrase( ( null != attribute ) ? Utilities.formatString( attribute.getValue() ) : " ", fontNormal ) );
             table.addCell( cell );
           }
           report.addTable( table );
         }
         if ( sitesSpecificHabitats.size() > 0 )
         {
           Table table = new Table( 1 );
           table.setCellsFitPage( true );
           table.setWidth( TABLE_WIDTH );
           table.setAlignment( Table.ALIGN_LEFT );
           table.setBorderWidth( 1 );
           table.setDefaultCellBorderWidth( 1 );
           table.setBorderColor( Color.BLACK );
           table.setCellspacing( 2 );
 
           Cell cell;
 
           cell = new Cell( new Phrase( "Habitat types not in EUNIS", fontTitle ) );
           cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( "Habitat type code", fontNormal ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           for ( int i = 0; i < sitesSpecificHabitats.size(); i++ )
           {
             Chm62edtSitesAttributesPersist habitat = ( Chm62edtSitesAttributesPersist ) sitesSpecificHabitats.get( i );
 
             cell = new Cell( new Phrase( habitat.getValue(), fontNormal ) );
             table.addCell( cell );
           }
           report.addTable( table );
         }
       }
     }
   }
 
   private void getOtherInfo() throws Exception
   {
     SQLUtilities sqlc = new SQLUtilities();
     sqlc.Init( SQL_DRV, SQL_URL, SQL_USR, SQL_PWD );
 
     // Human activity.
     if ( SiteFactsheet.TYPE_CORINE == type ||
         SiteFactsheet.TYPE_DIPLOMA == type || SiteFactsheet.TYPE_BIOGENETIC == type )
     {
       //not a NATURA2000 factsheet
       List activities = factsheet.findHumanActivity();
       //System.out.println( "activities.size() = " + activities.size() );
       if ( activities.size() > 0 )
       {
         Table table = new Table( 5 );
         table.setCellsFitPage( true );
         table.setWidth( TABLE_WIDTH );
         table.setAlignment( Table.ALIGN_LEFT );
         table.setBorderWidth( 1 );
         table.setDefaultCellBorderWidth( 1 );
         table.setBorderColor( Color.BLACK );
         table.setCellspacing( 2 );
 
         float[] colWidths1 = { 30, 20, 20, 10, 20 };
         table.setWidths( colWidths1 );
         Cell cell;
 
         cell = new Cell( new Phrase( contentManagement.cms( "human_activities"), fontTitle ) );
         cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
         cell.setColspan( 5 );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "activity"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "inside_outside"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "intensity"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "cover_percent"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "influence"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         for ( int i = 0; i < activities.size(); i++ )
         {
           HumanActivityPersist activity = ( HumanActivityPersist ) activities.get( i );
 
           cell = new Cell( new Phrase( Utilities.formatString( activity.getActivityName() ), fontNormal ) );
           table.addCell( cell );
 
           HumanActivityAttributesPersist humanActivityAttribute;
           humanActivityAttribute = factsheet.findHumanActivityAttribute( "IN_OUT", i );
 
           String ActivityLocation = humanActivityAttribute.getAttributeValue();
           if(ActivityLocation.equalsIgnoreCase("I")) ActivityLocation = "Inside";
           if(ActivityLocation.equalsIgnoreCase("O")) ActivityLocation = "Outside";
 
           cell = new Cell( new Phrase( ( null != humanActivityAttribute && SiteFactsheet.TYPE_CORINE != type ) ? ActivityLocation : " ", fontNormal ) );
           table.addCell( cell );
 
           humanActivityAttribute = factsheet.findHumanActivityAttribute( "INTENSITY", i );
           String ActivityIntensity = " ";
           if(null != humanActivityAttribute)
           {
             ActivityIntensity = humanActivityAttribute.getAttributeValue();
             //System.out.println("ActivityIntensity = " + ActivityIntensity);
             ActivityIntensity = sqlc.ExecuteSQL("SELECT NAME FROM CHM62EDT_ACTIVITY_INTENSITY WHERE ID_ACTIVITY_INTENSITY = '" + ActivityIntensity+"'");
             if( ActivityIntensity.length() == 0 ) ActivityIntensity = Utilities.formatString( humanActivityAttribute.getAttributeValue(), "&nbsp;" );
           }
           cell = new Cell( new Phrase( ( null != humanActivityAttribute && SiteFactsheet.TYPE_CORINE != type ) ? ActivityIntensity: " ", fontNormal ) );
           table.addCell( cell );
 
           humanActivityAttribute = factsheet.findHumanActivityAttribute( "COVER" );
           cell = new Cell( new Phrase( ( null != humanActivityAttribute ) ? humanActivityAttribute.getAttributeValue() : " ", fontNormal ) );
           table.addCell( cell );
 
           humanActivityAttribute = factsheet.findHumanActivityAttribute( "INFLUENCE", i );
           String ActivityInfluence = null;
           if(null != humanActivityAttribute)
           {
             ActivityInfluence = humanActivityAttribute.getAttributeValue();
             //System.out.println("ActivityInfluence = " + ActivityInfluence);
             ActivityInfluence = sqlc.ExecuteSQL("SELECT NAME FROM CHM62EDT_ACTIVITY_INFLUENCE WHERE ID_ACTIVITY_INFLUENCE = '" + ActivityInfluence+"'");
             if( ActivityInfluence.length() == 0 ) ActivityInfluence = Utilities.formatString( humanActivityAttribute.getAttributeValue(), "&nbsp;" );
           }
 
           cell = new Cell( new Phrase( ( null != humanActivityAttribute && SiteFactsheet.TYPE_CORINE != type ) ? ActivityInfluence : " ", fontNormal ) );
           table.addCell( cell );
         }
         report.addTable( table );
       }
 
     }
     // Human activity.
     if ( SiteFactsheet.TYPE_NATURA2000 == type || type == SiteFactsheet.TYPE_EMERALD )
     {
       //a NATURA2000 factsheet
       List activities = factsheet.findHumanActivity();
       if ( activities.size() > 0 )
       {
         Table table = new Table( 6 );
         table.setCellsFitPage( true );
         table.setWidth( TABLE_WIDTH );
         table.setAlignment( Table.ALIGN_LEFT );
         table.setBorderWidth( 1 );
         table.setDefaultCellBorderWidth( 1 );
         table.setBorderColor( Color.BLACK );
         table.setCellspacing( 2 );
 
         float[] colWidths1 = { 30, 20, 20, 10, 10, 10 };
         table.setWidths( colWidths1 );
         Cell cell;
 
         cell = new Cell( new Phrase( contentManagement.cms( "human_activities"), fontSubtitle ) );
         cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
         cell.setColspan( 6 );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( "Activity", fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( "Description", fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( "Location", fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( "Intensity", fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( "Cover(%)", fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( "Influence", fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         for ( int i = 0; i < activities.size(); i++ )
         {
           HumanActivityPersist activity = ( HumanActivityPersist ) activities.get( i );
 
           cell = new Cell( new Phrase( Utilities.formatString( activity.getActivityCode() ), fontNormal ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( Utilities.formatString( activity.getActivityName() ), fontNormal ) );
           table.addCell( cell );
 
           HumanActivityAttributesPersist humanActivityAttribute;
           humanActivityAttribute = factsheet.findHumanActivityAttribute( "IN_OUT", i  );
           String ActivityLocation = humanActivityAttribute.getAttributeValue();
           if ( ActivityLocation.equalsIgnoreCase( "I" ) )
           {
             ActivityLocation = "Inside";
           }
           if ( ActivityLocation.equalsIgnoreCase( "O" ) )
           {
             ActivityLocation = "Outside";
           }
           cell = new Cell( new Phrase( ( null != humanActivityAttribute && SiteFactsheet.TYPE_CORINE != type ) ? ActivityLocation : " ", fontNormal ) );
           table.addCell( cell );
 
           humanActivityAttribute = factsheet.findHumanActivityAttribute( "INTENSITY", i );
           String ActivityIntensity = null;
           if ( null != humanActivityAttribute )
           {
             ActivityIntensity = humanActivityAttribute.getAttributeValue();
             //System.out.println("ActivityIntensity = " + ActivityIntensity);
             ActivityIntensity = sqlc.ExecuteSQL( "SELECT NAME FROM CHM62EDT_ACTIVITY_INTENSITY WHERE ID_ACTIVITY_INTENSITY = '" + ActivityIntensity + "'" );
           }
           cell = new Cell( new Phrase( ( null != humanActivityAttribute && SiteFactsheet.TYPE_CORINE != type ) ? ActivityIntensity : " ", fontNormal ) );
           table.addCell( cell );
 
           humanActivityAttribute = factsheet.findHumanActivityAttribute( "COVER" );
           String ActivityCover = null;
           if ( null != humanActivityAttribute )
           {
             ActivityCover = humanActivityAttribute.getAttributeValue();
           }
           cell = new Cell( new Phrase( ( null != humanActivityAttribute ) ? Utilities.formatDecimal( ActivityCover, 5 ) : " ", fontNormal ) );
           table.addCell( cell );
 
           humanActivityAttribute = factsheet.findHumanActivityAttribute( "INFLUENCE", i );
           String ActivityInfluence = null;
           if ( null != humanActivityAttribute )
           {
             ActivityInfluence = humanActivityAttribute.getAttributeValue();
             //System.out.println("ActivityInfluence = " + ActivityInfluence);
             ActivityInfluence = sqlc.ExecuteSQL( "SELECT NAME FROM CHM62EDT_ACTIVITY_INFLUENCE WHERE ID_ACTIVITY_INFLUENCE = '" + ActivityInfluence + "'" );
           }
           cell = new Cell( new Phrase( ( null != humanActivityAttribute && SiteFactsheet.TYPE_CORINE != type ) ? ActivityInfluence : " ", fontNormal ) );
           table.addCell( cell );
         }
         report.addTable( table );
       }
     }
     // References to Maps
     if ( SiteFactsheet.TYPE_NATURA2000 == type || SiteFactsheet.TYPE_EMERALD == type ||
         SiteFactsheet.TYPE_DIPLOMA == type || SiteFactsheet.TYPE_BIOGENETIC == type )
     {
       String mapID = factsheet.getMapID();
       String mapScale = factsheet.getMapScale();
       String mapProjection = factsheet.getMapProjection();
       String mapDetails = factsheet.getMapDetails();
       // If none of the information is available for this site, we don't display the entire table at all.
       // Objects cannot be null because the persistent object returns "" in case of null.
       if ( !mapID.equalsIgnoreCase( "" ) &&
           !mapScale.equalsIgnoreCase( "" ) &&
           !mapProjection.equalsIgnoreCase( "" ) &&
           !mapDetails.equalsIgnoreCase( "" ) )
       {
         Table table = new Table( 4 );
         table.setCellsFitPage( true );
         table.setWidth( TABLE_WIDTH );
         table.setAlignment( Table.ALIGN_LEFT );
         table.setBorderWidth( 1 );
         table.setDefaultCellBorderWidth( 1 );
         table.setBorderColor( Color.BLACK );
         table.setCellspacing( 2 );
 
         float[] colWidths1 = { 30, 25, 25, 20 };
         table.setWidths( colWidths1 );
         Cell cell;
 
         cell = new Cell( new Phrase( contentManagement.cms( "reference_to_maps"), fontSubtitle ) );
         cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
         cell.setColspan( 4 );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "number"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "scale"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "projection"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "details"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( mapID, fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( mapScale, fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( mapProjection, fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( mapDetails, fontNormal ) );
         table.addCell( cell );
 
         report.addTable( table );
       }
     }
     // References to Photos
     if ( SiteFactsheet.TYPE_NATURA2000 == type ||
         SiteFactsheet.TYPE_EMERALD == type ||
         SiteFactsheet.TYPE_DIPLOMA == type ||
         SiteFactsheet.TYPE_BIOGENETIC == type )
     {
       String photoType = factsheet.getPhotoType();
       String photoNumber = factsheet.getPhotoNumber();
       String photoLocation = factsheet.getPhotoLocation();
       String photoDescription = factsheet.getPhotoDescription();
       String photoDate = factsheet.getPhotoDate();
       String photoAuthor = factsheet.getPhotoAuthor();
       // If none of the information is available for this site, we don't display the entire table at all.
       // Objects cannot be null because the persistent object returns "" in case of null.
       if ( !photoType.equalsIgnoreCase( "" ) &&
           !photoNumber.equalsIgnoreCase( "" ) &&
           !photoLocation.equalsIgnoreCase( "" ) &&
           !photoDescription.equalsIgnoreCase( "" ) &&
           !photoDescription.equalsIgnoreCase( "" ) &&
           !photoDate.equalsIgnoreCase( "" ) &&
           !photoAuthor.equalsIgnoreCase( "" ) )
       {
         Table table = new Table( 6 );
         table.setCellsFitPage( true );
         table.setWidth( TABLE_WIDTH );
         table.setAlignment( Table.ALIGN_LEFT );
         table.setBorderWidth( 1 );
         table.setDefaultCellBorderWidth( 1 );
         table.setBorderColor( Color.BLACK );
         table.setCellspacing( 2 );
 
         float[] colWidths1 = { 30, 20, 20, 10, 10, 10 };
         table.setWidths( colWidths1 );
         Cell cell;
 
         cell = new Cell( new Phrase( contentManagement.cms( "reference_to_photos"), fontSubtitle ) );
         cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
         cell.setColspan( 6 );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "type"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "number"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "location"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "description"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "date"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "author"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( photoType, fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( photoNumber, fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( photoLocation, fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( photoDescription, fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( photoDate, fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( photoAuthor, fontNormal ) );
         table.addCell( cell );
 
         report.addTable( table );
       }
     }
     // Other project specific fields
     String category = factsheet.getSiteObject().getIucnat();
     String typology = factsheet.getTypology();
     String referenceDocNumber = factsheet.getReferenceDocumentNumber();
     String referenceDocSource = factsheet.getReferenceDocumentSource();
     // If one of the attributes above are valid, we show the entire table
     // Objects cannot be null because the persistent object returns "" in case of null.
     if ( !category.equalsIgnoreCase( "" ) ||
         !typology.equalsIgnoreCase( "" ) ||
         !referenceDocNumber.equalsIgnoreCase( "" ) ||
         !referenceDocSource.equalsIgnoreCase( "" ) )
     {
       Table table = new Table( 2 );
       table.setCellsFitPage( true );
       table.setWidth( TABLE_WIDTH );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setBorderWidth( 1 );
       table.setDefaultCellBorderWidth( 1 );
       table.setBorderColor( Color.BLACK );
       table.setCellspacing( 2 );
 
       float[] colWidths1 = { 50, 50 };
       table.setWidths( colWidths1 );
       Cell cell;
 
       cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_144"), fontSubtitle ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       cell.setColspan( 2 );
       table.addCell( cell );
       if ( SiteFactsheet.TYPE_CDDA_NATIONAL == type )
       {
         cell = new Cell( new Phrase( contentManagement.cms( "iucn_management"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( Utilities.formatString( category ), fontNormal ) );
         table.addCell( cell );
       }
       if ( SiteFactsheet.TYPE_NATURA2000 == type || SiteFactsheet.TYPE_EMERALD == type )
       {
         cell = new Cell( new Phrase( contentManagement.cms( "site_typology"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( Utilities.formatString( typology ), fontNormal ) );
         table.addCell( cell );
       }
       if ( SiteFactsheet.TYPE_CDDA_INTERNATIONAL == type )
       {
         cell = new Cell( new Phrase( contentManagement.cms( "reference_document_number"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( Utilities.formatString( referenceDocNumber ), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "reference_document_source"), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( Utilities.formatString( referenceDocSource ), fontNormal ) );
         table.addCell( cell );
       }
       report.addTable( table );
     }
   }
 
   private void getRelatedSites() throws Exception
   {
     if ( type != SiteFactsheet.TYPE_NATURA2000 )
     {
       //not a NATURA2000 site - everything remains as it is
       List sites = factsheet.findSiteRelations();
       if ( sites.size() > 0 )
       {
         Table table = new Table( 5 );
         table.setCellsFitPage( true );
         table.setWidth( TABLE_WIDTH );
         table.setAlignment( Table.ALIGN_LEFT );
         table.setBorderWidth( 1 );
         table.setDefaultCellBorderWidth( 1 );
         table.setBorderColor( Color.BLACK );
         table.setCellspacing( 2 );
 
         float[] colWidths1 = { 30, 20, 20, 20, 10 };
         table.setWidths( colWidths1 );
         Cell cell;
 
         cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_123"), fontSubtitle ) );
         cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
         cell.setColspan( 5 );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "id_site"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "scientific_name"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "within_databases"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "type"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "overlap_percent"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         for ( int i = 0; i < sites.size(); i++ )
         {
           SiteRelationsPersist site = ( SiteRelationsPersist ) sites.get( i );
           boolean withinProject = site.getWithinProject() != null && site.getWithinProject().intValue() == 1;
 
           cell = new Cell( new Phrase( Utilities.formatString( site.getIdSiteLink(), " " ), fontNormal ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( Utilities.formatString( site.getSiteName() ), fontNormal ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( withinProject ? "Yes" : " ", fontNormal ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( Utilities.formatString( site.getRelationType(), " " ), fontNormal ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( Utilities.formatString( site.getOverlap(), " " ), fontNormal ) );
           table.addCell( cell );
         }
         report.addTable( table );
       }
     }
     else
     {
       //we have a Natura 2000 factsheet type
       //First render the relations between Natura 2000 sites - sitrel
       List sitesNatura200 = factsheet.findSiteRelationsNatura2000Natura2000();
       if ( sitesNatura200.size() > 0 )
       {
         Table table = new Table( 3 );
         table.setCellsFitPage( true );
         table.setWidth( TABLE_WIDTH );
         table.setAlignment( Table.ALIGN_LEFT );
         table.setBorderWidth( 1 );
         table.setDefaultCellBorderWidth( 1 );
         table.setBorderColor( Color.BLACK );
         table.setCellspacing( 2 );
 
         float[] colWidths1 = { 30, 20, 50 };
         table.setWidths( colWidths1 );
         Cell cell;
 
         cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_123"), fontTitle ) );
         cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
         cell.setColspan( 3 );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "Type of relation"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "Site code"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "Site name"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         for ( int i = 0; i < sitesNatura200.size(); i++ )
         {
           SiteRelationsPersist site = ( SiteRelationsPersist ) sitesNatura200.get( i );
 
           cell = new Cell( new Phrase( site.getRelationName(), fontNormal ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( site.getIdSiteLink(), fontNormal ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( site.getSiteName(), fontNormal ) );
           table.addCell( cell );
         }
         report.addTable( table );
       }
 
       List sitesCorine = factsheet.findSiteRelationsNatura2000Corine();
       if ( sitesCorine.size() > 0 )
       {
         Table table = new Table( 4 );
         table.setCellsFitPage( true );
         table.setWidth( TABLE_WIDTH );
         table.setAlignment( Table.ALIGN_LEFT );
         table.setBorderWidth( 1 );
         table.setDefaultCellBorderWidth( 1 );
         table.setBorderColor( Color.BLACK );
         table.setCellspacing( 2 );
 
         float[] colWidths1 = { 20, 40, 20, 20 };
         table.setWidths( colWidths1 );
         Cell cell;
 
         cell = new Cell( new Phrase( contentManagement.cms( "sites_factsheet_123"), fontSubtitle ) );
         cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
         cell.setColspan( 4 );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "Site code"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "Site name"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "Overlap"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( contentManagement.cms( "Overlap P"), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         for ( int i = 0; i < sitesCorine.size(); i++ )
         {
           SiteRelationsPersist site = ( SiteRelationsPersist ) sitesCorine.get( i );
 
           cell = new Cell( new Phrase( site.getIdSiteLink(), fontNormal ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( site.getSiteName(), fontNormal ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( site.getRelationType(), fontNormal ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( Utilities.formatDecimal( site.getOverlap(), 2 ), fontNormal ) );
           table.addCell( cell );
         }
         report.addTable( table );
       }
     }
   }
 }
