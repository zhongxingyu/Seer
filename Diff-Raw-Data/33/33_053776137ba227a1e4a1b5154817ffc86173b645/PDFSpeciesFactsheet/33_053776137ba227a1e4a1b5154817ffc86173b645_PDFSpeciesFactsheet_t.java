 package ro.finsiel.eunis.factsheet;
 
 import com.lowagie.text.*;
 import com.lowagie.text.Font;
 import ro.finsiel.eunis.WebContentManagement;
 import ro.finsiel.eunis.factsheet.species.*;
 import ro.finsiel.eunis.jrfTables.SpeciesNatureObjectPersist;
 import ro.finsiel.eunis.jrfTables.species.factsheet.DistributionWrapper;
 import ro.finsiel.eunis.jrfTables.species.factsheet.ReportsDistributionStatusPersist;
 import ro.finsiel.eunis.jrfTables.species.factsheet.SitesByNatureObjectPersist;
 import ro.finsiel.eunis.jrfTables.species.taxonomy.Chm62edtTaxcodeDomain;
 import ro.finsiel.eunis.jrfTables.species.taxonomy.Chm62edtTaxcodePersist;
 import ro.finsiel.eunis.reports.pdfReport;
 import ro.finsiel.eunis.search.Utilities;
 import ro.finsiel.eunis.search.sites.SitesSearchUtility;
 import ro.finsiel.eunis.search.species.SpeciesSearchUtility;
 import ro.finsiel.eunis.search.species.VernacularNameWrapper;
 import ro.finsiel.eunis.search.species.factsheet.PublicationWrapper;
 
 import java.awt.*;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 /**
  * Specifes factsheet in PDF format.
  */
 public final class PDFSpeciesFactsheet
 {
   private static final float TABLE_WIDTH = 94f;
 
   private Integer idNatureObject = null;
   private String scientificName = null;
   private int idSpecies = 0;
 
   private WebContentManagement contentManagement = null;
   private pdfReport report = null;
   private SpeciesFactsheet factsheet = null;
 
   private final String SQL_DRV;
   private final String SQL_URL;
   private final String SQL_USR;
   private final String SQL_PWD;
 
   Font fontNormal = FontFactory.getFont( FontFactory.HELVETICA, 9, Font.NORMAL );
   Font fontNormalBold = FontFactory.getFont( FontFactory.HELVETICA, 9, Font.BOLD );
   Font fontTitle = FontFactory.getFont( FontFactory.HELVETICA, 12, Font.BOLD );
   Font fontSubtitle = FontFactory.getFont( FontFactory.HELVETICA, 10, Font.BOLD );
 
   /**
    * Constructor for PDFSpeciesFactsheet object.
    * @param contentManagement current content management
    * @param report Report to write to, already initialized
    * @param idSpecies ID_SPECIES
    * @param idSpeciesLink ID_SPECIES_LINK
    * @param SQL_DRV SQL Driver
    * @param SQL_URL SQL Driver URL
    * @param SQL_USR SQL Driver username
    * @param SQL_PWD SQL Driver password
    */
   public PDFSpeciesFactsheet( WebContentManagement contentManagement, pdfReport report,
                               Integer idSpecies, Integer idSpeciesLink,
                               String SQL_DRV, String SQL_URL, String SQL_USR, String SQL_PWD )
   {
     this.contentManagement = contentManagement;
     this.report = report;
     this.idSpecies = idSpecies;
     this.SQL_DRV = SQL_DRV;
     this.SQL_URL = SQL_URL;
     this.SQL_USR = SQL_USR;
     this.SQL_PWD = SQL_PWD;
 
     factsheet = new SpeciesFactsheet( this.idSpecies, idSpeciesLink );
     this.idNatureObject = factsheet.getSpeciesNatureObject().getIdNatureObject();
     this.idSpecies = factsheet.getIdSpecies();
     this.scientificName = factsheet.getSpeciesNatureObject().getScientificName();
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
 
       getVernacularNames();
       report.getDocument().newPage();
 
       getGeographicalDistribution();
       report.getDocument().newPage();
 
       getPopulation();
       report.getDocument().newPage();
 
       getTrends();
       report.getDocument().newPage();
 
       getReferences();
       report.getDocument().newPage();
 
       getThreatStatus();
       report.getDocument().newPage();
 
       getLegalInstruments();
       report.getDocument().newPage();
 
       getGridDistribution();
       report.getDocument().newPage();
 
       getRelatedHabitats();
       report.getDocument().newPage();
 
       getSites();
     }
     catch ( Exception e )
     {
       ret = false;
       e.printStackTrace();
     }
     return ret;
   }
 
   /**
    * species-factsheet-distribution.jsp
    *
    * @throws Exception
    */
   private void getGridDistribution() throws Exception
   {
     DistributionWrapper dist = new DistributionWrapper( idNatureObject );
     List d = dist.getDistribution();
     if ( d.size() > 0 )
     {
       Table table = new Table( 5 );
       table.setCellsFitPage( true );
       table.setWidth( TABLE_WIDTH );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setBorderWidth( 1 );
       table.setDefaultCellBorderWidth( 1 );
       table.setBorderColor( Color.BLACK );
       table.setCellspacing( 2 );
 
       float[] colWidths = { 15, 15, 15, 20, 35 };
       table.setWidths( colWidths );
 
       // Header
       Cell cell;
       cell = new Cell( new Phrase( "Grid distribution", fontTitle ) );
       cell.setBackgroundColor( new Color( 0xCC, 0xCC, 0xCC ) );
       cell.setColspan( 5 );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( contentManagement.cms( "code_cell" ), fontNormal ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "species_factsheet-distribution_04" ), fontNormal ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "species_factsheet-distribution_05" ), fontNormal ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "status" ), fontNormal ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "reference" ), fontNormal ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       // Content
       String GridName;
       String GridLongitude = " ";
       String GridLatitude = " ";
       String GridStatus;
       String GridIdDc;
       // Display results.
       for ( int i = 0; i < d.size(); i += 2 )
       {
         ReportsDistributionStatusPersist dis;
         dis = ( ReportsDistributionStatusPersist ) d.get( i );
         GridName = dis.getIdLookupGrid();
         GridStatus = dis.getDistributionStatus();
         GridIdDc = dis.getIdDc().toString();
         if ( i < d.size() - 1 )
         {
           dis = ( ReportsDistributionStatusPersist ) d.get( i + 1 );
           GridLongitude = Utilities.formatDecimal( dis.getLongitude().toString(), 2 );
           GridLatitude = Utilities.formatDecimal( dis.getLatitude().toString(), 2 );
         }
         table.addCell( new Cell( new Phrase( GridName, fontNormal ) ) );
         table.addCell( new Cell( new Phrase( GridLongitude, fontNormal ) ) );
         table.addCell( new Cell( new Phrase( GridLatitude, fontNormal ) ) );
         table.addCell( new Cell( new Phrase( GridStatus, fontNormal ) ) );
         table.addCell( new Cell( new Phrase( ( String ) Utilities.getAuthorAndUrlByIdDc( GridIdDc ).get( 0 ), fontNormal ) ) );
       }
       report.addTable( table );
     }
   }
 
   /**
    * species-factsheet-general.jsp
    *
    * @throws Exception
    */
   private void getGeneralInformation() throws Exception
   {
     Table table = new Table( 1 );
     table.setWidth( TABLE_WIDTH );
     table.setBorderColor( Color.BLACK );
     table.setAlignment( Table.ALIGN_LEFT );
     table.setBorderWidth( 1 );
     table.setDefaultCellBorderWidth( 1 );
     table.setCellspacing( 2 );
     table.setCellsFitPage( true );
 
     Cell cell;
     cell = new Cell( new Phrase( scientificName, fontTitle ) );
     cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
     cell.setHorizontalAlignment( Cell.ALIGN_CENTER );
     table.addCell( cell );
 
    cell = new Cell( new Phrase( contentManagement.cms( "scientific_name" ) + ": " + scientificName, fontNormal ) );
     cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
     table.addCell( cell );
 
    cell = new Cell( new Phrase( contentManagement.cms( "author" ) + ": " + Utilities.formatString( factsheet.getSpeciesNatureObject().getAuthor() ), fontNormal ) );
     cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
     table.addCell( cell );
 
     report.addTable( table );
 
     // Taxonomy information
     table = new Table( 3 );
     table.setWidth( TABLE_WIDTH );
     table.setBorderColor( Color.BLACK );
     table.setAlignment( Table.ALIGN_LEFT );
     table.setBorderWidth( 1 );
     table.setDefaultCellBorderWidth( 1 );
     table.setCellspacing( 2 );
     table.setCellsFitPage( true );
     float[] widths = { 10f, 70f, 20f };
     table.setWidths( widths );
 
     cell = new Cell( new Phrase( contentManagement.cms( "species_factsheet_taxonomicInformation" ), fontTitle ) );
     cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
     cell.setColspan( 2 );
     table.addCell( cell );
     cell = new Cell( new Phrase( contentManagement.cms( "reference" ), fontSubtitle ) );
     cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
     cell.setHorizontalAlignment( Cell.ALIGN_CENTER );
     table.addCell( cell );
 
     List list = new Vector();
     try
     {
       list = new Chm62edtTaxcodeDomain().findWhere( "ID_TAXONOMY = '" + factsheet.getSpeciesNatureObject().getIdTaxcode() + "'" );
     }
     catch ( Exception ex )
     {
       ex.printStackTrace();
     }
     if ( list != null && list.size() > 0 )
     {
       String authorDate = SpeciesFactsheet.getBookAuthorDate( factsheet.getTaxcodeObject().IdDcTaxcode() );
       Chm62edtTaxcodePersist t = ( Chm62edtTaxcodePersist ) list.get( 0 );
       String str = t.getTaxonomyTree();
       //System.out.println("str = " + str);
       StringTokenizer st = new StringTokenizer( str, "," );
       while ( st.hasMoreTokens() )
       {
         StringTokenizer sts = new StringTokenizer( st.nextToken(), "*" );
         String classification_id = sts.nextToken();
         String classification_level = sts.nextToken();
         String classification_name = sts.nextToken();
 
         cell = new Cell( new Phrase( classification_level, fontNormal ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( classification_name, fontNormal ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         if ( classification_level.equalsIgnoreCase( "kingdom" ) )
         {
           cell = new Cell( new Phrase( authorDate, fontNormal ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           cell.setHorizontalAlignment( Cell.ALIGN_CENTER );
           cell.setVerticalAlignment( Cell.ALIGN_MIDDLE );
           cell.setRowspan( st.countTokens() + 2 );
           table.addCell( cell );
         }
       }
     }
 
     cell = new Cell( new Phrase( "Genus", fontNormal ) );
     cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
     table.addCell( cell );
 
     cell = new Cell( new Phrase( Utilities.formatString( factsheet.getSpeciesNatureObject().getGenus() ), fontNormal ) );
     cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
     table.addCell( cell );
 
     report.addTable( table );
 
     // Source
     table = new Table( 2 );
     table.setWidth( TABLE_WIDTH );
     table.setAlignment( Table.ALIGN_LEFT );
     table.setCellspacing( 2 );
     float[] widthsSrc = { 20f, 80f };
     table.setWidths( widthsSrc );
     cell = new Cell( new Phrase( contentManagement.cms( "source" ), fontTitle ) );
     cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
     cell.setColspan( 2 );
     table.addCell( cell );
 
     PublicationWrapper book = factsheet.getSpeciesBook();
     cell = new Cell( new Phrase( contentManagement.cms( "title" ), fontNormalBold ) );
     table.addCell( cell );
     cell = new Cell( new Phrase( Utilities.formatString( book.getTitle() ), fontNormal ) );
     table.addCell( cell );
     cell = new Cell( new Phrase( contentManagement.cms( "author" ) ) );
     table.addCell( cell );
     cell = new Cell( new Phrase( Utilities.formatString( book.getAuthor() ), fontNormal ) );
     table.addCell( cell );
     cell = new Cell( new Phrase( contentManagement.cms( "publisher" ) ) );
     table.addCell( cell );
     cell = new Cell( new Phrase( Utilities.formatString( book.getPublisher() ), fontNormal ) );
     table.addCell( cell );
     cell = new Cell( new Phrase( contentManagement.cms( "publication_date" ) ) );
     table.addCell( cell );
     cell = new Cell( new Phrase( Utilities.formatString( book.getDate() ), fontNormal ) );
     table.addCell( cell );
     cell = new Cell( new Phrase( contentManagement.cms( "url" ) ) );
     table.addCell( cell );
     cell = new Cell( new Phrase( Utilities.formatString( book.getURL() ), fontNormal ) );
     table.addCell( cell );
     report.addTable( table );
 
 
     // Synonyms
     List synonyms = factsheet.getSynonymsIterator();
     if ( synonyms.size() > 0 )
     {
       table = new Table( 2 );
       table.setWidth( TABLE_WIDTH );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setCellspacing( 2 );
       float[] widthsSyn = { 60f, 40f };
       table.setWidths( widthsSyn );
 
       cell = new Cell( new Phrase( contentManagement.cms( "synonyms" ), fontTitle ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       cell.setColspan( 2 );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "scientific_name" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "author" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       for ( int i = 0; i < synonyms.size(); i++ )
       {
         SpeciesNatureObjectPersist synonym = ( SpeciesNatureObjectPersist ) synonyms.get( i );
 
         cell = new Cell( new Phrase( synonym.getScientificName(), fontNormal ) );
         if ( synonym.getIdSpecies().intValue() == Utilities.checkedStringToInt( factsheet.getIdSpecies().toString(), 0 ) )
         {
           cell.setBackgroundColor( new Color( 0xC3, 0x00, 0x00 ) );
         }
         table.addCell( cell );
         cell = new Cell( new Phrase( synonym.getAuthor(), fontNormal ) );
         table.addCell( cell );
       }
       report.addTable( table );
     }
 
     // Subspecies list.
     List subSpecies = factsheet.getSubspecies();
     if ( !subSpecies.isEmpty() )
     {
       table = new Table( 2 );
       table.setWidth( TABLE_WIDTH );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setCellspacing( 2 );
       float[] widthsSubsp = { 60f, 40f };
       table.setWidths( widthsSubsp );
 
       cell = new Cell( new Phrase( contentManagement.cms( "valid_subspecies_europe" ), fontTitle ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       cell.setColspan( 2 );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( contentManagement.cms( "scientific_name" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "source" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       for ( int i = 0; i < subSpecies.size(); i++ )
       {
         SpeciesNatureObjectPersist species = ( SpeciesNatureObjectPersist ) subSpecies.get( i );
 
         cell = new Cell( new Phrase( species.getScientificName() + "(" + species.getAuthor() + ")", fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( SpeciesFactsheet.getBookAuthorDate( species.getIdDublinCore() ), fontNormal ) );
         table.addCell( cell );
       }
       report.addTable( table );
     }
   }
 
   /**
    * species-factsheet-geo.jsp
    *
    * @throws Exception
    */
   private void getGeographicalDistribution() throws Exception
   {
     Vector v = SpeciesFactsheet.getBioRegionIterator( idNatureObject, idSpecies );
     if ( v.size() > 0 )
     {
       Table table = new Table( 4 );
       table.setWidth( TABLE_WIDTH );
       table.setBorderColor( Color.BLACK );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setBorderWidth( 1 );
       table.setDefaultCellBorderWidth( 1 );
       table.setCellspacing( 2 );
       table.setCellsFitPage( true );
       float[] widths = { 15f, 25f, 20f, 40f };
       table.setWidths( widths );
       table.setWidth( TABLE_WIDTH );
 
       Cell cell;
       cell = new Cell( new Phrase( "Geographical distribution", fontTitle ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       cell.setColspan( 4 );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( contentManagement.cms( "species_factsheet-geo_04" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "biogeographic_region" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "status" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "reference" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       for ( int i = 0; i < v.size(); i++ )
       {
         GeographicalStatusWrapper aRow = ( GeographicalStatusWrapper ) v.get( i );
         String country = ( null != aRow.getCountry() ) ? aRow.getCountry().getAreaNameEnglish() : " ";
         Vector authorURL = Utilities.getAuthorAndUrlByIdDc( aRow.getReference() );
 
         cell = new Cell( new Phrase( country, fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( aRow.getRegion(), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( aRow.getStatus(), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( ( String ) authorURL.get( 0 ), fontNormal ) );
         table.addCell( cell );
       }
       report.addTable( table );
     }
   }
 
   /**
    * species-factsheet-habitats.jsp
    *
    * @throws Exception
    */
   private void getRelatedHabitats() throws Exception
   {
     List habitats = factsheet.getHabitatsForSpecies();
     if ( habitats.size() > 0 )
     {
       Table table = new Table( 9 );
       table.setWidth( TABLE_WIDTH );
       table.setBorderColor( Color.BLACK );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setBorderWidth( 1 );
       table.setDefaultCellBorderWidth( 1 );
       table.setCellspacing( 2 );
       table.setCellsFitPage( true );
       float[] widths = { 10f, 10f, 30f, 10f, 5f, 5f, 5f, 5f, 20f };
       table.setWidths( widths );
       table.setWidth( TABLE_WIDTH );
 
       Cell cell;
       cell = new Cell( new Phrase( contentManagement.cms( "habitat_type_populated_by_species" ), fontTitle ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       cell.setColspan( 9 );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( contentManagement.cms( "eunis_code" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "annex_code" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "habitat_type_name" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "biogeographic_region" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "abundance" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "frequencies" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "faithfulness" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "species_status" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "comment" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
 
       for ( int i = 0; i < habitats.size(); i++ )
       {
         SpeciesHabitatWrapper habitat = ( SpeciesHabitatWrapper ) habitats.get( i );
 
         cell = new Cell( new Phrase( Utilities.formatString( habitat.getEunisHabitatcode() ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( Utilities.formatString( habitat.getAnnexICode() ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( Utilities.formatString( habitat.getHabitatName() ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( Utilities.formatString( habitat.getGeoscope() ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( Utilities.formatString( habitat.getAbundance() ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( Utilities.formatString( habitat.getFrequencies() ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( Utilities.formatString( habitat.getFaithfulness() ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( Utilities.formatString( habitat.getSpeciesStatus() ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( Utilities.formatString( habitat.getComment() ), fontNormal ) );
         table.addCell( cell );
       }
       report.addTable( table );
     }
   }
 
   /**
    * species-factsheet-legal.jsp
    *
    * @throws Exception
    */
   private void getLegalInstruments() throws Exception
   {
     Vector legals = factsheet.getLegalStatus();
     if ( legals.size() > 0 )
     {
       Table table = new Table( 5 );
       table.setWidth( TABLE_WIDTH );
       table.setBorderColor( Color.BLACK );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setBorderWidth( 1 );
       table.setDefaultCellBorderWidth( 1 );
       table.setCellspacing( 2 );
       table.setCellsFitPage( true );
       float[] widths = { 30f, 20f, 20f, 10f, 20f };
       table.setWidths( widths );
       table.setWidth( TABLE_WIDTH );
 
       Cell cell;
       cell = new Cell( new Phrase( contentManagement.cms( "legal_instruments" ), fontTitle ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       cell.setColspan( 5 );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( contentManagement.cms( "detailed_reference" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "legal_text" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( "Comments" ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "url" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "species_factsheet_legalInstrumentsGeoImplem" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       for ( int i = 0; i < legals.size(); i++ )
       {
         LegalStatusWrapper legal = ( LegalStatusWrapper ) legals.get( i );
 
         cell = new Cell( new Phrase( Utilities.formatString( legal.getDetailedReference() ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( Utilities.formatString( legal.getLegalText() ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( Utilities.formatString( legal.getComments() ), fontNormal ) );
         table.addCell( cell );
 
         String sFormattedURL = Utilities.formatString( legal.getUrl() ).replaceAll( "#", "" );
         if ( sFormattedURL.length() > 30 )
         {
           sFormattedURL = sFormattedURL.substring( 0, 30 ) + "...";
         }
         cell = new Cell( new Phrase( sFormattedURL, fontNormal ) );
         table.addCell( cell );
 
         String legalInfo = contentManagement.cms( "species_factsheet_notApplicable" );
         if ( !legal.getReference().equalsIgnoreCase( "10333" ) )
         {
           legalInfo = " ";
         }
         cell = new Cell( new Phrase( legalInfo, fontNormal ) );
         table.addCell( cell );
       }
       report.addTable( table );
     }
   }
 
   /**
    * species-factsheet-pop.jsp
    *
    * @throws Exception
    */
   private void getPopulation() throws Exception
   {
     Vector list = SpeciesFactsheet.getPopulation( idNatureObject.toString() );
     if ( list.size() > 0 )
     {
       Table table = new Table( 7 );
       table.setWidth( TABLE_WIDTH );
       table.setBorderColor( Color.BLACK );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setBorderWidth( 1 );
       table.setDefaultCellBorderWidth( 1 );
       table.setCellspacing( 2 );
       table.setCellsFitPage( true );
       float[] widths = { 15f, 20f, 10f, 10f, 12f, 23f, 10f };
       table.setWidths( widths );
       table.setWidth( TABLE_WIDTH );
 
       Cell cell;
       cell = new Cell( new Phrase( "Population", fontTitle ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       cell.setColspan( 7 );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( contentManagement.cms( "country" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "biogeographic_region" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "min_max" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "date" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "status" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "quality" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "reference" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
 
       Vector authorURL;
       for ( int i = 0; i < list.size(); i++ )
       {
         FactSheetPopulationWrapper aRow = ( FactSheetPopulationWrapper ) list.get( i );
         authorURL = Utilities.getAuthorAndUrlByIdDc( aRow.getReference() );
         cell = new Cell( new Phrase( Utilities.formatString( aRow.getCountry() ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( Utilities.formatString( aRow.getBioregion() ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( Utilities.formatString( aRow.getMin() + "/" + aRow.getMax() + "(" + aRow.getUnits() + ")" ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( Utilities.formatString( aRow.getDate() ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( Utilities.formatString( aRow.getStatus() ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( Utilities.formatString( aRow.getQuality() ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( Utilities.formatString( authorURL.get( 0 ) ), fontNormal ) );
         table.addCell( cell );
       }
       report.addTable( table );
     }
   }
 
   /**
    * species-factsheet-references.jsp
    *
    * @throws Exception
    */
   private void getReferences() throws Exception
   {
     // Set SQL string
     String sql = "";
     sql += "    SELECT";
     sql += "      `CHM62EDT_REPORT_TYPE`.`LOOKUP_TYPE` AS `TYPE`,";
     sql += "      `DC_SOURCE`.`SOURCE`,";
     sql += "      `DC_SOURCE`.`EDITOR`,";
     sql += "      `DC_DATE`.`CREATED`,";
     sql += "      `DC_TITLE`.`TITLE`,";
     sql += "      `DC_PUBLISHER`.`PUBLISHER`";
     sql += "    FROM";
     sql += "      `CHM62EDT_SPECIES`";
     sql += "      INNER JOIN `CHM62EDT_NATURE_OBJECT` ON (`CHM62EDT_SPECIES`.`ID_NATURE_OBJECT` = `CHM62EDT_NATURE_OBJECT`.`ID_NATURE_OBJECT`)";
     sql += "      INNER JOIN `CHM62EDT_REPORTS` ON (`CHM62EDT_SPECIES`.`ID_NATURE_OBJECT` = `CHM62EDT_REPORTS`.`ID_NATURE_OBJECT`)";
     sql += "      INNER JOIN `CHM62EDT_REPORT_TYPE` ON (`CHM62EDT_REPORTS`.`ID_REPORT_TYPE` = `CHM62EDT_REPORT_TYPE`.`ID_REPORT_TYPE`)";
     sql += "      INNER JOIN `DC_INDEX` ON (`CHM62EDT_REPORTS`.`ID_DC` = `DC_INDEX`.`ID_DC`)";
     sql += "      INNER JOIN `DC_PUBLISHER` ON (`DC_INDEX`.`ID_DC` = `DC_PUBLISHER`.`ID_DC`)";
     sql += "      INNER JOIN `DC_TITLE` ON (`DC_INDEX`.`ID_DC` = `DC_TITLE`.`ID_DC`)";
     sql += "      INNER JOIN `DC_SOURCE` ON (`DC_INDEX`.`ID_DC` = `DC_SOURCE`.`ID_DC`)";
     sql += "      INNER JOIN `DC_DATE` ON (`DC_INDEX`.`ID_DC` = `DC_DATE`.`ID_DC`)";
     sql += "    WHERE";
     sql += "      (`CHM62EDT_REPORT_TYPE`.`LOOKUP_TYPE` IN ('DISTRIBUTION_STATUS','LANGUAGE','CONSERVATION_STATUS','SPECIES_GEO','LEGAL_STATUS','SPECIES_STATUS','POPULATION_UNIT','TREND'))";
     sql += "    AND (`CHM62EDT_SPECIES`.`ID_SPECIES` = " + idSpecies + ")";
     sql += "    UNION";
     sql += "    SELECT";
     sql += "      'Synonyms' AS `TYPE`,";
     sql += "      `DC_SOURCE`.`SOURCE`,";
     sql += "      `DC_SOURCE`.`EDITOR`,";
     sql += "      `DC_DATE`.`CREATED`,";
     sql += "      `DC_TITLE`.`TITLE`,";
     sql += "      `DC_PUBLISHER`.`PUBLISHER`";
     sql += "    FROM";
     sql += "      `CHM62EDT_SPECIES`";
     sql += "      INNER JOIN `CHM62EDT_NATURE_OBJECT` ON (`CHM62EDT_SPECIES`.`ID_NATURE_OBJECT` = `CHM62EDT_NATURE_OBJECT`.`ID_NATURE_OBJECT`)";
     sql += "      INNER JOIN `DC_INDEX` ON (`CHM62EDT_NATURE_OBJECT`.`ID_DC` = `DC_INDEX`.`ID_DC`)";
     sql += "      INNER JOIN `DC_PUBLISHER` ON (`DC_INDEX`.`ID_DC` = `DC_PUBLISHER`.`ID_DC`)";
     sql += "      INNER JOIN `DC_TITLE` ON (`DC_INDEX`.`ID_DC` = `DC_TITLE`.`ID_DC`)";
     sql += "      INNER JOIN `DC_SOURCE` ON (`DC_INDEX`.`ID_DC` = `DC_SOURCE`.`ID_DC`)";
     sql += "      INNER JOIN `DC_DATE` ON (`DC_INDEX`.`ID_DC` = `DC_DATE`.`ID_DC`)";
     sql += "    WHERE `CHM62EDT_SPECIES`.`ID_SPECIES_LINK` = " + idSpecies;
     sql += "    AND `CHM62EDT_SPECIES`.`ID_SPECIES` <> " + idSpecies;
     sql += "    UNION";
     sql += "    SELECT";
     sql += "      'Species' AS `TYPE`,";
     sql += "      `DC_SOURCE`.`SOURCE`,";
     sql += "      `DC_SOURCE`.`EDITOR`,";
     sql += "      `DC_DATE`.`CREATED`,";
     sql += "      `DC_TITLE`.`TITLE`,";
     sql += "      `DC_PUBLISHER`.`PUBLISHER`";
     sql += "    FROM";
     sql += "      `CHM62EDT_SPECIES`";
     sql += "      INNER JOIN `CHM62EDT_NATURE_OBJECT` ON (`CHM62EDT_SPECIES`.`ID_NATURE_OBJECT` = `CHM62EDT_NATURE_OBJECT`.`ID_NATURE_OBJECT`)";
     sql += "      INNER JOIN `DC_INDEX` ON (`CHM62EDT_NATURE_OBJECT`.`ID_DC` = `DC_INDEX`.`ID_DC`)";
     sql += "      INNER JOIN `DC_PUBLISHER` ON (`DC_INDEX`.`ID_DC` = `DC_PUBLISHER`.`ID_DC`)";
     sql += "      INNER JOIN `DC_TITLE` ON (`DC_INDEX`.`ID_DC` = `DC_TITLE`.`ID_DC`)";
     sql += "      INNER JOIN `DC_SOURCE` ON (`DC_INDEX`.`ID_DC` = `DC_SOURCE`.`ID_DC`)";
     sql += "      INNER JOIN `DC_DATE` ON (`DC_INDEX`.`ID_DC` = `DC_DATE`.`ID_DC`)";
     sql += "    WHERE `CHM62EDT_SPECIES`.`ID_SPECIES` = " + idSpecies;
     sql += "    UNION";
     sql += "    SELECT";
     sql += "      'Taxonomy' AS `TYPE`,";
     sql += "      `DC_SOURCE`.`SOURCE`,";
     sql += "      `DC_SOURCE`.`EDITOR`,";
     sql += "      `DC_DATE`.`CREATED`,";
     sql += "      `DC_TITLE`.`TITLE`,";
     sql += "      `DC_PUBLISHER`.`PUBLISHER`";
     sql += "    FROM";
     sql += "      `CHM62EDT_SPECIES`";
     sql += "      INNER JOIN `CHM62EDT_NATURE_OBJECT` ON (`CHM62EDT_SPECIES`.`ID_NATURE_OBJECT` = `CHM62EDT_NATURE_OBJECT`.`ID_NATURE_OBJECT`)";
     sql += "      INNER JOIN `CHM62EDT_TAXONOMY` ON (`CHM62EDT_SPECIES`.`ID_TAXONOMY` = `CHM62EDT_TAXONOMY`.`ID_TAXONOMY`)";
     sql += "      INNER JOIN `DC_INDEX` ON (`CHM62EDT_TAXONOMY`.`ID_DC` = `DC_INDEX`.`ID_DC`)";
     sql += "      INNER JOIN `DC_PUBLISHER` ON (`DC_INDEX`.`ID_DC` = `DC_PUBLISHER`.`ID_DC`)";
     sql += "      INNER JOIN `DC_TITLE` ON (`DC_INDEX`.`ID_DC` = `DC_TITLE`.`ID_DC`)";
     sql += "      INNER JOIN `DC_SOURCE` ON (`DC_INDEX`.`ID_DC` = `DC_SOURCE`.`ID_DC`)";
     sql += "      INNER JOIN `DC_DATE` ON (`DC_INDEX`.`ID_DC` = `DC_DATE`.`ID_DC`)";
     sql += "    WHERE `CHM62EDT_SPECIES`.`ID_SPECIES` = " + idSpecies;
     sql += "    GROUP BY CHM62EDT_SPECIES.ID_NATURE_OBJECT,DC_INDEX.ID_DC,DC_SOURCE.SOURCE,DC_SOURCE.EDITOR,DC_TITLE.TITLE,DC_PUBLISHER.PUBLISHER,DC_DATE.CREATED";
 
     Class.forName( SQL_DRV );
     Connection con = DriverManager.getConnection( SQL_URL, SQL_USR, SQL_PWD );
     Statement ps = con.createStatement();
     ResultSet rs = ps.executeQuery( sql );
     if ( rs.next() )
     {
       rs.beforeFirst();
       Table table = new Table( 5 );
       table.setWidth( TABLE_WIDTH );
       table.setBorderColor( Color.BLACK );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setBorderWidth( 1 );
       table.setDefaultCellBorderWidth( 1 );
       table.setCellspacing( 2 );
       table.setCellsFitPage( true );
       float[] widths = { 20f, 20f, 25f, 10f, 25f };
       table.setWidths( widths );
       table.setWidth( TABLE_WIDTH );
 
       Cell cell;
       cell = new Cell( new Phrase( "References", fontTitle ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       cell.setColspan( 5 );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( contentManagement.cms( "title" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "author" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "editor" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "date" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "source" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       String source;
       String author;
       String editor;
       String date;
       String title;
       while ( rs.next() )
       {
         if ( rs.getString( 1 ) == null )
         {
           source = "&nbsp;";
         }
         else
         {
           source = ro.finsiel.eunis.search.Utilities.FormatDatabaseFieldName( rs.getString( 1 ) );
         }
         if ( rs.getString( 2 ) == null )
         {
           author = "&nbsp;";
         }
         else
         {
           author = rs.getString( 2 );
         }
         if ( rs.getString( 3 ) == null )
         {
           editor = "&nbsp;";
         }
         else
         {
           editor = rs.getString( 3 );
         }
         if ( rs.getString( 4 ) == null || rs.getString( 4 ).equals( "" ) )
         {
           date = "&nbsp;";
         }
         else
         {
           date = Utilities.formatReferencesDate( rs.getDate( 4 ) );
         }
         if ( rs.getString( 5 ) == null )
         {
           title = "&nbsp;";
         }
         else
         {
           title = rs.getString( 5 );
         }
 
         cell = new Cell( new Phrase( title, fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( author, fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( editor, fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( date, fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( source, fontNormal ) );
         table.addCell( cell );
       }
       report.addTable( table );
     }
   }
 
   /**
    * species-factsheet-sites.jsp
    *
    * @throws Exception
    */
   private void getSites() throws Exception
   {
     // Related sites
     List sites = factsheet.getSitesForSpecies();
     if ( sites.size() > 0 )
     {
       Table table = new Table( 4 );
       table.setWidth( TABLE_WIDTH );
       table.setBorderColor( Color.BLACK );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setBorderWidth( 1 );
       table.setDefaultCellBorderWidth( 1 );
       table.setCellspacing( 2 );
       table.setCellsFitPage( true );
       float[] widths = { 15f, 20f, 20f, 45f };
       table.setWidths( widths );
       table.setWidth( TABLE_WIDTH );
 
       Cell cell;
       cell = new Cell( new Phrase( "Related sites", fontTitle ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       cell.setColspan( 4 );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( contentManagement.cms( "site_code" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "source_data_set" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "country" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "site_name" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       for ( int i = 0; i < sites.size(); i++ )
       {
         SitesByNatureObjectPersist site = ( SitesByNatureObjectPersist ) sites.get( i );
         cell = new Cell( new Phrase( Utilities.formatString( site.getIDSite() ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( Utilities.formatString( SitesSearchUtility.translateSourceDB( site.getSourceDB() ) ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( Utilities.formatString( site.getAreaNameEn() ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( Utilities.formatString( site.getName() ), fontNormal ) );
         table.addCell( cell );
       }
       report.addTable( table );
 
       // Sites for subtaxa of this taxon
       List sites2 = factsheet.getSitesForSubpecies();
       if ( sites2.size() > 0 )
       {
         table = new Table( 3 );
         table.setWidth( TABLE_WIDTH );
         table.setBorderColor( Color.BLACK );
         table.setAlignment( Table.ALIGN_LEFT );
         table.setBorderWidth( 1 );
         table.setDefaultCellBorderWidth( 1 );
         table.setCellspacing( 2 );
         table.setCellsFitPage( true );
         float[] widths2 = { 20f, 20f, 60f };
         table.setWidths( widths2 );
         table.setWidth( TABLE_WIDTH );
 
         cell = new Cell( new Phrase( contentManagement.cms( "source_data_set" ), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( contentManagement.cms( "country" ), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( contentManagement.cms( "site_name" ), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         for ( int i = 0; i < sites2.size(); i++ )
         {
           SitesByNatureObjectPersist site = ( SitesByNatureObjectPersist ) sites2.get( i );
           cell = new Cell( new Phrase( site.getIDSite(), fontNormal ) );
           table.addCell( cell );
           cell = new Cell( new Phrase( SitesSearchUtility.translateSourceDB( site.getSourceDB() ), fontNormal ) );
           table.addCell( cell );
           cell = new Cell( new Phrase( Utilities.formatString( site.getAreaNameEn() ), fontNormal ) );
           table.addCell( cell );
         }
         report.addTable( table );
       }
     }
   }
 
 
   /**
    * species-factsheet-threat.jsp
    *
    * @throws Exception
    */
   private void getThreatStatus() throws Exception
   {
     // National threat status
     List nationalThreatStatus = factsheet.getNationalThreatStatus( factsheet.getSpeciesObject() );
     if ( nationalThreatStatus.size() > 0 )
     {
       Table table = new Table( 4 );
       table.setWidth( TABLE_WIDTH );
       table.setBorderColor( Color.BLACK );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setBorderWidth( 1 );
       table.setDefaultCellBorderWidth( 1 );
       table.setCellspacing( 2 );
       table.setCellsFitPage( true );
       float[] widths = { 20f, 20f, 30f, 30f };
       table.setWidths( widths );
       table.setWidth( TABLE_WIDTH );
 
       Cell cell;
       cell = new Cell( new Phrase( "National threat status", fontTitle ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       cell.setColspan( 4 );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( contentManagement.cms( "country" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "status" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "national_threat_code" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "reference" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       for ( int i = 0; i < nationalThreatStatus.size(); i++ )
       {
         NationalThreatWrapper threat = ( NationalThreatWrapper ) nationalThreatStatus.get( i );
 
         cell = new Cell( new Phrase( Utilities.formatString( threat.getCountry() ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( Utilities.formatString( threat.getStatus() ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( Utilities.formatString( threat.getThreatCode() ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( Utilities.formatString( threat.getReference() ), fontNormal ) );
         table.addCell( cell );
       }
       report.addTable( table );
     }
 
 
     // International threat status
     List consStatus = factsheet.getConservationStatus( factsheet.getSpeciesObject() );
     if ( consStatus.size() > 0 )
     {
       Table table = new Table( 4 );
       table.setWidth( TABLE_WIDTH );
       table.setBorderColor( Color.BLACK );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setBorderWidth( 1 );
       table.setDefaultCellBorderWidth( 1 );
       table.setCellspacing( 2 );
       table.setCellsFitPage( true );
       float[] widths = { 20f, 20f, 30f, 30f };
       table.setWidths( widths );
       table.setWidth( TABLE_WIDTH );
 
       Cell cell;
       cell = new Cell( new Phrase( "International threat status", fontTitle ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       cell.setColspan( 4 );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( contentManagement.cms( "area" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "status" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "species_factsheet-conservation_09" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "reference" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       // Display results.
       for ( int i = 0; i < consStatus.size(); i++ )
       {
         NationalThreatWrapper threat = ( NationalThreatWrapper ) consStatus.get( i );
 
         cell = new Cell( new Phrase( Utilities.formatString( threat.getCountry() ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( Utilities.formatString( threat.getStatus() ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( Utilities.formatString( threat.getThreatCode() ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( Utilities.formatString( threat.getReference() ), fontNormal ) );
         table.addCell( cell );
       }
       report.addTable( table );
     }
   }
 
   /**
    * species-factsheet-trends.jsp
    *
    * @throws Exception
    */
   private void getTrends() throws Exception
   {
     Vector list = SpeciesFactsheet.getTrends( idNatureObject, idSpecies );
     if ( list.size() > 0 )
     {
       Table table = new Table( 8 );
       table.setWidth( TABLE_WIDTH );
       table.setBorderColor( Color.BLACK );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setBorderWidth( 1 );
       table.setDefaultCellBorderWidth( 1 );
       table.setCellspacing( 2 );
       table.setCellsFitPage( true );
       float[] widths = { 20f, 15f, 10f, 10f, 10f, 10f, 10f, 15f };
       table.setWidths( widths );
       table.setWidth( TABLE_WIDTH );
 
       Cell cell;
       cell = new Cell( new Phrase( "Trends", fontTitle ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       cell.setColspan( 8 );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( contentManagement.cms( "country" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( contentManagement.cms( "biogeographic_region" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( contentManagement.cms( "start_period" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( contentManagement.cms( "end_period" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( contentManagement.cms( "status" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( contentManagement.cms( "species_factsheet-trends_07" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( contentManagement.cms( "quality" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( contentManagement.cms( "reference" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       for ( int i = 0; i < list.size(); i++ )
       {
         FactSheetTrendsWrapper aRow = ( FactSheetTrendsWrapper ) list.get( i );
 
         cell = new Cell( new Phrase( Utilities.formatString( aRow.getCountry() ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( Utilities.formatString( aRow.getBioregion() ), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( aRow.getStartPeriod(), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( aRow.getEndPeriod(), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( aRow.getStatus(), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( aRow.getTrends(), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( aRow.getQuality(), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( ( String ) Utilities.getAuthorAndUrlByIdDc( aRow.getReference() ).get( 0 ) ) );
         table.addCell( cell );
       }
       report.addTable( table );
     }
   }
 
   /**
    * species-factsheet-vern.jsp
    *
    * @throws Exception
    */
   private void getVernacularNames() throws Exception
   {
     List results = SpeciesSearchUtility.findVernacularNames( idNatureObject );
     Iterator it = results.iterator();
     if ( results.size() > 0 )
     {
       Table table = new Table( 3 );
       table.setWidth( TABLE_WIDTH );
       table.setBorderColor( Color.BLACK );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setBorderWidth( 1 );
       table.setDefaultCellBorderWidth( 1 );
       table.setCellspacing( 2 );
       table.setCellsFitPage( true );
       float[] widths = { 30f, 30f, 40f };
       table.setWidths( widths );
       table.setWidth( TABLE_WIDTH );
 
       Cell cell;
       cell = new Cell( new Phrase( "Vernacular names", fontTitle ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       cell.setColspan( 3 );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( contentManagement.cms( "vernacular_name" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "language" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
       cell = new Cell( new Phrase( contentManagement.cms( "reference" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       while ( it.hasNext() )
       {
         VernacularNameWrapper vName = ( ( VernacularNameWrapper ) it.next() );
         String reference = ( vName.getIdDc() == null ? "-1" : vName.getIdDc().toString() );
         Vector authorURL = Utilities.getAuthorAndUrlByIdDc( reference );
 
         cell = new Cell( new Phrase( vName.getName(), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( vName.getLanguage(), fontNormal ) );
         table.addCell( cell );
         cell = new Cell( new Phrase( ( String ) authorURL.get( 0 ), fontNormal ) );
         table.addCell( cell );
       }
       report.addTable( table );
     }
   }
 }
