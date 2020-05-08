 package nl.ctmm.trait.proteomics.qcviewer.utils;
 
 import java.awt.Dimension;
 import java.awt.Font;
 import java.text.SimpleDateFormat;
 
 /**
  * This interface contains the most important constants of the project.
  *
  * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
  * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
  */
 public interface Constants {
     /**
      * Application name.
      */
     String APPLICATION_NAME = "Proteomics QC Report Viewer";
 
     /**
      * Application version.
      */
     String APPLICATION_VERSION = "1.6.5";
 
     /**
      * Application title.
      */
     String APPLICATION_TITLE = APPLICATION_NAME + " " + APPLICATION_VERSION;
 
     /**
      * Name of application properties file.
      */
     String PROPERTIES_FILE_NAME = "appProperties";
 
     /**
      * Property name for the folder to retrieve QC reports from.
      */
     String PROPERTY_ROOT_FOLDER = "RootFolder";
 
     /**
      * Default property value for the folder to retrieve QC reports from.
      */
     String DEFAULT_ROOT_FOLDER = "QCReports";
 
     /**
      * Name of the progress log file to monitor for new QC reports.
      */
     String PROGRESS_LOG_FILE_NAME = "qc_status.log";
 
     /**
      * Property name for the initial metrics to show.
      */
     String PROPERTY_TOP_COLUMN_NAMESV2 = "TopColumnNamesV2";
 
 
     /**
      * Property name for the start date of the QC reports to show.
      */
     String PROPERTY_SHOW_REPORTS_FROM_DATE = "ShowReportsFromDate";
 
     /**
      * Property name for the end date of the QC reports to show.
      */
     String PROPERTY_SHOW_REPORTS_TILL_DATE = "ShowReportsTillDate";
 
 //    String DEFAULT_REPORTS_DISPLAY_PERIOD = "DefaultReportsDisplayPeriod";
 //    String DEFAULT_REPORTS_DISPLAY_PERIOD_VALUE = "14"; //show reports from last two weeks by default
 
     /**
      * Date format string used for parsing dates.
      *
      * TODO: use DATE_FORMAT below instead of this string? [Freek]
      * 
      * [Pravin] Following occurrences of SIMPLE_DATE_FORMAT_STRING can not be 
      * replaced by DATE_FORMAT, since JDateChooser does not support using
      * SimpleDateFormat. It supports setDateFormatString. 
      * DataEntryForm.java: 
      * fromDateChooser.setDateFormatString(Constants.SIMPLE_DATE_FORMAT_STRING);
      * tillDateChooser.setDateFormatString(Constants.SIMPLE_DATE_FORMAT_STRING);
      * 
      * Replaced one occurrence of SIMPLE_DATE_FORMAT_STRING as follows:
      * ReportReader.java:
      * //SimpleDateFormat sdf = new SimpleDateFormat(Constants.SIMPLE_DATE_FORMAT_STRING);
      * SimpleDateFormat sdf = Constants.DATE_FORMAT;
      */
     String SIMPLE_DATE_FORMAT_STRING = "dd/MM/yyyy";
 
     /**
      * Simple date format used for parsing dates.
      */
     SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(SIMPLE_DATE_FORMAT_STRING);
 
     /**
      * Name of the file with all QC metrics.
      */
     String METRICS_LISTING_FILE_NAME = "MetricsListing.txt";
 
     /**
      * Name of the CTMM TraIT logo file.
      */
     String CTMM_TRAIT_LOGO_FILE_NAME = "images\\traitctmmlogo.png";
 
     /**
      * Name of the NIST logo file.
      */
     String NIST_LOGO_FILE_NAME = "images\\nistlogo.jpg";
 
     /**
      * Name of the NBIC logo file.
      */
     String NBIC_LOGO_FILE_NAME = "images\\nbiclogo.png";
 
     /**
      * Name of the OPL logo file.
      */
     String OPL_LOGO_FILE_NAME = "images\\opllogo.jpg";
 
     /**
      * Name of the CTMM logo file.
      */
     String CTMM_LOGO_FILE_NAME = "images\\ctmmlogo.jpg";
 
     /**
      * The name of the font used in the GUI.
      */
     String FONT_NAME = "Garamond";
 
     /**
      * The default font.
      */
     Font DEFAULT_FONT = new Font(FONT_NAME, Font.BOLD, 11);
 
     /**
      * The font used for the report numbers.
      */
     Font REPORT_NUMBER_FONT = new Font(Constants.FONT_NAME, Font.BOLD, 22);
 
     /**
      * The font used for the titles in the charts.
      */
     Font CHART_TITLE_FONT = new Font(FONT_NAME, Font.BOLD, 13);
 
     /**
      * The font used for all the metrics in the details frame and the text areas in the about frame.
      */
     Font PLAIN_FONT = new Font(FONT_NAME, Font.PLAIN, 11);
 
     /**
      * The font used for the metrics headers in the details frame.
      */
     Font DETAILS_HEADER_FONT = new Font(FONT_NAME, Font.BOLD, 12);
 
     /**
      * The poll interval in milliseconds for checking the QC pipeline log file.
      */
     int POLL_INTERVAL_PIPELINE_LOG = 5000;
 
     /**
      * Separator used in metrics definitions.
      */
     String METRICS_SEPARATOR = ":";
 
     /**
      * The OK button text and action command.
      */
     String OK_BUTTON_TEXT = "OK";
 
     /**
      * The Cancel button text and action command.
      */
     String CANCEL_BUTTON_TEXT = "Cancel";
 
     /**
      * The Yes button text and action command.
      */
     String YES_BUTTON_TEXT = "Yes";
 
     /**
      * The No button text and action command.
      */
     String NO_BUTTON_TEXT = "No";
 
     /**
      * HTML opening tag.
      */
     String HTML_OPENING_TAG = "<html>";
 
     /**
      * HTML closing tag.
      */
     String HTML_CLOSING_TAG = "</html>";
     
     /**
      * Dimension object for filler areas of 5x0 pixels for GUI layout.
      */
     Dimension DIMENSION_5X0 = new Dimension(5, 0);
 
     /**
      * Dimension object for filler areas of 10x0 pixels for GUI layout.
      */
     Dimension DIMENSION_10X0 = new Dimension(10, 0);
 
     /**
      * Dimension object for filler areas of 25x0 pixels for GUI layout.
      */
     Dimension DIMENSION_25X0 = new Dimension(25, 0);
 
     /**
      * Dimension object for filler areas of 0x5 pixels for GUI layout.
      */
     Dimension DIMENSION_0X5 = new Dimension(0, 5);
 
     /**
      * Dimension object for filler areas of 0x10 pixels for GUI layout.
      */
     Dimension DIMENSION_0X10 = new Dimension(0, 10);
 
     /**
      * The number of seconds per minute.
      */
     int SECONDS_PER_MINUTE = 60;
 
     /**
      * The name of the JSON file generated by the QC pipeline with the metrics values.
      */
     String METRICS_JSON_FILE_NAME = "metrics.json";
 
     /**
      * The suffix of the TIC matrix file generated by the (Java addition to the) QC pipeline with the TIC matrix.
      */
     String TIC_MATRIX_FILE_NAME_SUFFIX = "_ticmatrix.csv";
 
     /**
     * Sort key string to sort according to report index 
      */
     String SORT_KEY_REPORT_INDEX = "index";
 
    
     /**
     * Sort key string to sort according to RAW file size 
      */
     String SORT_KEY_FILE_SIZE = "generic:f_size";
 
     /**
     * Sort key string to sort according to number of MS1 spectra 
      */
     String SORT_KEY_MS1_SPECTRA = "generic:ms1_spectra";
 
     /**
     * Sort key string to sort according to number of MS2 spectra 
      */
     String SORT_KEY_MS2_SPECTRA = "generic:ms2_spectra";
 
    
     /**
     * Sort key string to sort according to report generation date 
      */
     String SORT_KEY_DATE = "generic:date";
 
    
     /**
     * Sort key string to sort according to runtime 
      */
     String SORT_KEY_RUNTIME = "generic:runtime";
 
    
     /**
     * Sort key string to sort according to maxIntensity 
      */
     String SORT_KEY_MAX_INTENSITY = "maxIntensity";

    



 }
