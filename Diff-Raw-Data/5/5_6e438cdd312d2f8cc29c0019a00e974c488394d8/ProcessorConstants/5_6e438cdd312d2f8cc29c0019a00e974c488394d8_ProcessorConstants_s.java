 /*
  * Created on Oct 13, 2006
  * @author
  *
  */
 package edu.common.dynamicextensions.processor;
 
 import edu.wustl.common.util.global.Constants;
 
 /**
  * @author preeti_munot
  *
  */
 public class ProcessorConstants
 {
 	//Types of controls
 	public static final String TEXT_CONTROL  = "TextControl";
 	public static final String MULTILINE_CONTROL  = "MultilineControl";
 	public static final String COMBOBOX_CONTROL  = "ComboboxControl";
 	public static final String LISTBOX_CONTROL  = "ListBoxControl";
 	public static final String CHECKBOX_CONTROL  = "CheckboxControl";
 	public static final String RADIOBUTTON_CONTROL  = "RadioButtonControl";
 	public static final String DATEPICKER_CONTROL  = "DateControl";
 	public static final String FILEUPLOAD_CONTROL = "FileUploadControl";
 	public static final String ADD_SUBFORM_CONTROL = "AddSubFormControl";
 	public static final String ADD_SUBFORM_TYPE = "Sub Form";
 
 	//Datattype constants 
 	public static final String DATATYPE_STRING = "Text";
 	public static final String DATATYPE_DATE = "Date";
 	public static final String DATATYPE_BOOLEAN = "Yes/No";
 	public static final String DATATYPE_NUMBER = "Number";
 	public static final String DATATYPE_BYTEARRAY = "ByteArray";
 	public static final String DATATYPE_FILE = "File";
 
 	//Max number of digits/decimals for various datatypes
 	public static final int MAX_NO_OF_DIGITS_SHORT = 5;
 	public static final int MAX_NO_OF_DIGITS_INT = 10;
 	public static final int MAX_NO_OF_DIGITS_LONG = 15;
 	public static final int MAX_NO_OF_DECIMALS_FLOAT = 5;
 	public static final int MAX_NO_OF_DECIMALS_DOUBLE = 10;
     
 	//Operations on UI.
     public static final String OPERATION_ADD = "Add";
     public static final String OPERATION_EDIT = "Edit";
     
     //Date Formats
     public static final String DATE_ONLY_FORMAT = Constants.DATE_PATTERN_MM_DD_YYYY;
     //public static final String DATE_TIME_FORMAT = Constants.TIMESTAMP_PATTERN;
     public static final String DATE_TIME_FORMAT = "MM-dd-yyyy HH:mm";
     public static final String MONTH_YEAR_FORMAT = "MM-yyyy";
     public static final String YEAR_ONLY_FORMAT = "yyyy";
     public static final String SQL_DATE_ONLY_FORMAT = "MM-dd-yyyy";
     public static final String SQL_DATE_TIME_FORMAT = "MM-dd-yyyy HH24:MI";
     public static final String DATE_SEPARATOR = Constants.DATE_SEPARATOR;
     
     //Types of permisible value sources for Combobox
     public static final String DISPLAY_CHOICE_USER_DEFINED = "UserDefined";
     public static final String DISPLAY_CHOICE_CDE = "CDE";
     public static final String DISPLAY_CHOICE_LOOKUP = "Lookup";
     
     //Measurement unit OTHER for numeric fields
     public static final String MEASUREMENT_UNIT_OTHER = "other";
     
     //Single line / multiline line types
     public static final String LINE_TYPE_MULTILINE = "MultiLine";
     public static final String LINE_TYPE_SINGLELINE = "SingleLine";
     
     //Single and multi list box
     public static final String LIST_TYPE_MULTI_SELECT = "MultiSelect";
     public static final String LIST_TYPE_SINGLE_SELECT = "SingleSelect";
     
     //Form creation
     public static final String CREATE_AS_NEW = "NewForm";
     public static final String CREATE_FROM_EXISTING = "ExistingForm";
     
     //Form view
     public static final String VIEW_AS_FORM = "Form";
     public static final String VIEW_AS_SPREADSHEET = "SpreadSheet";
     
     
     //Date formats
     public static final String DATE_FORMAT_OPTION_DATEONLY = "DateOnly";
     public static final String DATE_FORMAT_OPTION_DATEANDTIME = "DateAndTime";
     public static final String DATE_FORMAT_OPTION_MONTHANDYEAR = "MonthAndYear";
     public static final String DATE_FORMAT_OPTION_YEARONLY = "YearOnly";
     
     //Date Values
     public static final String DATE_VALUE_NONE = "None";
     public static final String 	DATE_VALUE_TODAY = "Today";
     public static final String DATE_VALUE_SELECT = "Select";
     
     //Lookup form types
     public static final String LOOKUP_SYSTEM_FORMS = "SystemForms";
     public static final String LOOKUP_USER_FORMS = "UserForms";
     
     
     //Group Constants
     public static final String GROUP_CREATEAS_NEW ="NewGroup";
     public static final String GROUP_CREATEFROM_EXISTING ="ExistingGroup";
     	//Group operations
     public static final String SAVE_GROUP = "savegroup";
     public static final String SHOW_NEXT_PAGE = "showNextPage";
     
     //separator for specifying file formats
     public static final String FILE_FORMATS_SEPARATOR = ",";
     public static final String CONTROLS_SEQUENCE_NUMBER_SEPARATOR = ",";
     
     
     /*DEFAULT VALUE CONSTANTS*/
     //Default value for create entity as new/from existing
     public static final String DEFAULT_FORM_CREATEAS = CREATE_AS_NEW;
     
     public static final String DEFAULT_FORM_VIEWAS = VIEW_AS_FORM;
     
     //default type of list : single line : combobox
     public static final String DEFAULT_LIST_TYPE = LIST_TYPE_SINGLE_SELECT;
     
     //default selected control : text control
     public static final String DEFAULT_SELECTED_CONTROL = TEXT_CONTROL;
     
     //default specification of list values : User defined 
     public static final String DEFAULT_DISPLAY_CHOICE_TYPE = DISPLAY_CHOICE_USER_DEFINED;
     
     //default line type for text : single line
     public static final String DEFAULT_LINE_TYPE = LINE_TYPE_SINGLELINE;
     
     //default data type : text/string
     public static final String DEFAULT_DATA_TYPE = DATATYPE_STRING;
     
     //default date format
     public static final String DEFAULT_DATE_FORMAT = DATE_FORMAT_OPTION_DATEONLY;
     
     //Default date value  : Select date
     public static final String DEFAULT_DATE_VALUE = DATE_VALUE_NONE;
     
     //Checkbox default value
     public static final String DEFAULT_CHECKBOX_VALUE = "checked";
     //Default value for group create as
     public static final String DEFAULT_GROUP_CREATEAS=  GROUP_CREATEAS_NEW ;
     //Default lookup type 
     public static final String DEFAULT_LOOKUP_TYPE = LOOKUP_USER_FORMS;
     
     //Max length for fields
     public static final int MAX_LENGTH_NAME = 40;
     public static final int MAX_LENGTH_DESCRIPTION = 1000;
     public static final int MAX_LENGTH_DISPLAY_WIDTH = 3;//i.e max value 999
     public static final int MAX_LENGTH_MAX_CHARACTERS = 3; //i.e max value 999
     
     
 }
