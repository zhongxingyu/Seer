 /*
  * Copyright Author
  * (USE & RESTRICTIONS - Please read COPYRIGHT file)
 
  * Version : XX.XX
  * Date : 4/20/11 1:09 PM
  */
 
 // Default Package
 package DWLProject;
 
 import java.awt.Color;
 
 import model.modeling.message;
 import view.modeling.ViewableAtomic;
 import DWLProject.utils.DWLProperties;
 import DWLProject.utils.DWLUtils;
 import GenCol.entity;
 
 public class DataPartitioner_0_0 extends ViewableAtomic {
 	private FlatFile flatFile;
 	private CatFile aCatFile;
 	private int[] catNumRecords;
 	private static int MAX_DIMENSIONS = Integer.valueOf(DWLProperties.getInstance().getValue("MAX_DIMENSIONS"));
 	private static int MAX_SUMLEVELS = Integer.valueOf(DWLProperties.getInstance().getValue("MAX_SUMLEVELS"));
 	private static int YEARS = Integer.valueOf(DWLProperties.getInstance().getValue("NumberOfYears"));
 	private static int PROCESSING_FACTOR_CAT = Integer.valueOf(DWLProperties.getInstance().getValue("processingFactorCAT"));
 	
 	
 	private  int cats, recs, validRecs, errors, totalRecsAssigned; 
 	private static final String FF_IN = "FFin";
 	private static final String CAT_FILE_OUT = "CatFileOut";
 	private static final String ERROR_FILE = "errorFile";
 	private static final String DP_DONE = "DPDone";
 	private static final String nL = "\n";
 	private static String ttText = "";
 
 	// Phases
 	private static final String PASSIVE = "passive";
 	private static final String RECEIVE_FF = "ReceiveFF";
 	private static final String CREATE_CAT = "CreateCATs";
 	private static final String SEND_CAT = "SendCATs";
 	private static final String CHECK_ERRORS = "CheckErrors";
 	private static final String SEND_ERRORS = "SendErrorFile";
 	private static final String ENDING = "Ending";
 
 	private message doneMessage;
 	private message catFileMessage;
 	private message errorFileMessage;
 
 	// Add Default Constructor
 	public DataPartitioner_0_0() {
 		this("DataPartitioner_0_0");
 	}
 
 	// Add Parameterized Constructors
 	public DataPartitioner_0_0(String name) {
 		super(name);
 		// Structure information start
 		// Add input port names
 		addInport(FF_IN);
 
 		// Add output port names
 		addOutport(CAT_FILE_OUT);
 		addOutport(DP_DONE);
 		addOutport(ERROR_FILE);
 
 		// add test input ports:
 
 		// Structure information end
 		initialize();
 	}
 
 	// Add initialize function
 	public void initialize() {
 		super.initialize();
 		phase = "passive";
 		sigma = INFINITY;
 	}
 
 	// Add external transition function
 	public void deltext(double e, message x) {
 		Continue(e);
 		if (phaseIs(PASSIVE)) {
 			for (int i = 0; i < x.size(); i++) {
 				checkForFlatFile(x, i);
 			}
 		}
 	}
 
 	private void checkForFlatFile(message x, int i) {
 		if (messageOnPort(x, FF_IN, i)) {
 			entity value = x.getValOnPort(FF_IN, i);
 			if (value instanceof FlatFile) {
 				flatFile = (FlatFile) value;
 				if (flatFile.getNumberOfCategories() < 0) {
 					System.out.println("Invalid Flat File: # of Categories must be > 1");
 					holdIn(PASSIVE, INFINITY);
 					this.setBackgroundColor(Color.GRAY);
 				} else {
 					holdIn(RECEIVE_FF, flatFile.getTimeToRegister());
 					this.setBackgroundColor(Color.PINK);
 				}
 			} else {
 				System.out.println("Not a Flat File: " + value.getName());
 				holdIn(PASSIVE, INFINITY);
 				this.setBackgroundColor(Color.GRAY);
 			}
 		}
 	}
 
 	// Add internal transition function
 	public void deltint() {
 		if (phaseIs(RECEIVE_FF)) {
 			double processingTime = DWLUtils.DEFAULT_PROCESSING_TIME;
 			if (Double.compare(PROCESSING_FACTOR_CAT, 0D) > 0) {
				processingTime = Math.max(processingTime, flatFile.getNumberOfRecords()/PROCESSING_FACTOR_CAT);
 			} 
 			holdIn(CREATE_CAT, processingTime);
 			this.setBackgroundColor(Color.PINK);
 		} else if (phaseIs(CREATE_CAT)) {
 			recs = flatFile.getNumberOfRecords();
 			cats = flatFile.getNumberOfCategories();
 			errors = flatFile.getNumberOfErrors();
 			validRecs = recs - errors;
 			int remainingRecords = validRecs;
 			totalRecsAssigned = 0;
 
 			//The total number of valid records are split evenly between all the categories
 			if (cats >0){
 				catNumRecords = new int[cats];
 				for (int i = 0; i < cats; i++) {
 					if (remainingRecords > validRecs/cats)  
 						catNumRecords[i] = validRecs/cats;
 					else
 						catNumRecords[i] = remainingRecords;
 					totalRecsAssigned += catNumRecords[i];
 					remainingRecords -= catNumRecords[i];
 				}
 			}
 			// Create the CAT Files
 			catFileMessage = new message();
			
 			for (int i = 0; i < cats; i++) {
 				String catName = "CAT" + i;
 				double processingTime = DWLUtils.DEFAULT_PROCESSING_TIME;;
 				if (Double.compare(PROCESSING_FACTOR_CAT, 0D) > 0) {
 					for (int k = 1; k <= MAX_SUMLEVELS; k++) {
						processingTime = Math.max(processingTime, Double.valueOf(catNumRecords[i])/(k *PROCESSING_FACTOR_CAT));
 					}
 				}
 				
 				if (catNumRecords[i]>0){
 					aCatFile = new CatFile(catName, catNumRecords[i], DWLUtils.DEFAULT_REGISTRATION_TIME,
 							MAX_DIMENSIONS, MAX_SUMLEVELS, YEARS, processingTime);
 					catFileMessage.add(makeContent(CAT_FILE_OUT, aCatFile));
 					ttText = ttText + catName + " #recs: " + catNumRecords[i] + " #dims: "+ MAX_DIMENSIONS + 
 					" #SummLevels: " + MAX_SUMLEVELS+ " #years: " + YEARS+nL;
 				}
 			}
 			holdIn(SEND_CAT, 1);
 		} else if (phaseIs(SEND_CAT)) {
 			holdIn(CHECK_ERRORS, 1);
 			this.setBackgroundColor(Color.PINK);
 		} else if (phaseIs(CHECK_ERRORS)) {
 			if (errors != 0) {
 				ErrorFile ef = new ErrorFile(errors, DWLUtils.DEFAULT_REGISTRATION_TIME);
 				errorFileMessage = new message();
 				errorFileMessage.add(makeContent(ERROR_FILE, ef));
 				holdIn(SEND_ERRORS, 1);
 				this.setBackgroundColor(Color.PINK);
 			} else {
 				doneMessage = new message();
 				doneMessage.add(makeContent(DP_DONE, new entity(DP_DONE)));
 				holdIn(ENDING, 1);
 				this.setBackgroundColor(Color.PINK);
 			}
 		} else if (phaseIs(SEND_ERRORS)) {
 			doneMessage = new message();
 			doneMessage.add(makeContent(DP_DONE, new entity(DP_DONE)));
 			holdIn(ENDING, 1);
 			this.setBackgroundColor(Color.PINK);
 		} else {
 			passivate();
 			this.setBackgroundColor(Color.GRAY);
 		}
 	}
 
 	// Add confluent function
 	public void deltcon(double e, message x) {
 		deltint();
 		deltext(0, x);
 	}
 
 	// Add output function
 	public message out() {
 		if (phaseIs(SEND_CAT))
 			return catFileMessage;
 		if (phaseIs(SEND_ERRORS))
 			return errorFileMessage;
 		if (phaseIs(ENDING))
 			return doneMessage;
 		return new message();
 	}
 
 	// Add Show State function
 
 	public String getTooltipText() {
 		if (phaseIs(SEND_CAT)){
 		    return super.getTooltipText() + "\n" +
 		    ttText;
          }
 		else 
 			if (phaseIs(SEND_ERRORS))
 			{
 				ttText = "#Valid records: " + validRecs + " #Errors: " + errors;
 			    return super.getTooltipText() + "\n" +
 			    ttText;
 			}
 			return super.getTooltipText();
 	}
 }
