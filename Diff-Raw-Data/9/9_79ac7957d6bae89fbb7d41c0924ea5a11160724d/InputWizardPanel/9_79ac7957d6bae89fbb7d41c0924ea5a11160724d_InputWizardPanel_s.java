 package edu.cudenver.bios.powercalculator.client.panels;
 
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DeckPanel;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Hidden;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.xml.client.Document;
 
 import edu.cudenver.bios.powercalculator.client.PowerCalculatorGUI;
 import edu.cudenver.bios.powercalculator.client.listener.InputWizardStepListener;
 import edu.cudenver.bios.powercalculator.client.listener.ModelSelectListener;
 import edu.cudenver.bios.powercalculator.client.listener.NavigationListener;
 import edu.cudenver.bios.powercalculator.client.listener.OptionsListener;
 import edu.cudenver.bios.powercalculator.client.listener.StudyUploadListener;
 
 public class InputWizardPanel extends Composite 
 implements NavigationListener, OptionsListener, 
 StudyUploadListener, ModelSelectListener, InputWizardStepListener
 {
 	private static final String UPLOAD_PARAM = "u";
 	
     private static final int PANEL_STACK_START = 0;
     private static final int PANEL_STACK_STUDY_DESIGN = 1;
     private static final int PANEL_STACK_OPTIONS = 2;
     private static final int PANEL_STACK_RESULTS = 3;
 
 	private static final int STATUS_CODE_OK = 200;
 	private static final int STATUS_CODE_CREATED = 201;
 	private static final String POWER_URL = "/restcall/power/power";
     private static final String SAMPLE_SIZE_URL = "/restcall/power/samplesize";
 	private static final String POWER_CURVE_URL = "/restcall/power/curve";
 	
 	protected static final String STYLE = "inputPanel";
 	protected static final String DECK_STYLE = "inputPanelDeck";
 	// top steps left panel
 	protected StepsLeftPanel stepsLeftPanel = new StepsLeftPanel();
 	// bottom navigation buttons
 	protected NavigationPanel navPanel = new NavigationPanel();
 	// stack of panels for the wizard
 	protected DeckPanel panelStack = new DeckPanel();
 	// start panel - allows user to create a new study or upload an existing one
 	protected StartPanel startPanel = new StartPanel(this, PANEL_STACK_START);	
 	// study design panel
 	protected StudyDesignPanel studyDesignPanel = new StudyDesignPanel(this, PANEL_STACK_STUDY_DESIGN);
     // options panel (always in deck after input panel)
 	protected OptionsPanel optionsPanel = new OptionsPanel(this, PANEL_STACK_OPTIONS);
     // results panel (always in deck after options panel)
 	protected ResultsPanel resultsPanel = new ResultsPanel();
 
     // potential input panels.  Added to the deck depending on the input type
     // selected on the start panel
 	
 	// wait dialog
 	protected DialogBox waitDialog;
 	
 	// currently selected model
 	protected boolean newStudy = true;
 	protected String modelName = PowerCalculatorGUI.constants.modelGLMM();
 	protected boolean showCurve = false;
 	protected CurveOptions curveOpts = null;
 	protected boolean solveForPower = true;
 	
 	// indicators if study design and options are currently valid
 	protected boolean studyDesignValid = false;
 	protected boolean optionsValid = true;
 	
 	// form used to submit an image request to fill a hidden Iframe on the 
 	// results panel.  IE versions 5-7 do not support inline images, so this needs
 	// to be a separate request.  Lame-o.
 	protected FormPanel curveForm = new FormPanel(ResultsPanel.POWER_CURVE_FRAME);
 	protected Hidden curveRequestHidden = new Hidden("curveRequest");
 	
     public InputWizardPanel() 
     {       
         VerticalPanel container = new VerticalPanel();
 
         // set up the image request submission form
         curveForm.setAction(POWER_CURVE_URL);
         curveForm.setMethod(FormPanel.METHOD_POST);
         VerticalPanel formContainer = new VerticalPanel();
         formContainer.add(curveRequestHidden);
         curveForm.add(formContainer);
         
         // create a wait dialog
         waitDialog = createWaitDialog();
         
         
         // add the widgets to the stack of wizard panels
         // NOTE: order matters here - must match stack index constants
 //        String uploadStudy = Window.Location.getParameter(UPLOAD_PARAM);
 //        if (uploadStudy != null && uploadStudy.equals("1"))
 //        {
 //        	existingStudyPanel = new UploadPanel();
 //            panelStack.add(existingStudyPanel);
 //            
 //            // upload panel notifies study design panel when study is uploaded
 //            existingStudyPanel.addStudyUploadListener(this);
 //            existingStudyPanel.addStudyUploadListener(studyDesignPanel);
 //            existingStudyPanel.addStudyUploadListener(optionsPanel);
 //        }
 //        else
 //        {
 //        	newStudyPanel = new CreateNewStudyPanel();
 //            panelStack.add(newStudyPanel);
 //            
 //            // listen for model name changes from the create study panel
 //            newStudyPanel.addModelSelectListener(this);
 //            newStudyPanel.addModelSelectListener(studyDesignPanel);
 //            newStudyPanel.addModelSelectListener(optionsPanel);
 //        }
         panelStack.add(startPanel);
         panelStack.add(studyDesignPanel);
         panelStack.add(optionsPanel);
         panelStack.add(resultsPanel);
         // add the wizard panels and navigation buttons
         container.add(stepsLeftPanel);
         container.add(panelStack);
         container.add(navPanel);
         container.add(curveForm);
         // set up the navigation callbacks
         navPanel.addNavigationListener(this);
 
         // intialize the wizard
         updateStep(PANEL_STACK_START, false, true, true);
     	
         // listeners for study upload or model select events
         startPanel.addModelSelectListener(this);
         startPanel.addModelSelectListener(studyDesignPanel);
         startPanel.addModelSelectListener(optionsPanel);
         startPanel.addStudyUploadListener(this);
         startPanel.addStudyUploadListener(studyDesignPanel);
         startPanel.addStudyUploadListener(optionsPanel);
         
     	// listener for resize events on the essence matrix to allow 
     	// updating power/sample size options 
     	studyDesignPanel.addEssenceMatrixResizeListener(optionsPanel);
     	studyDesignPanel.addEssenceMatrixMetaDataListener(optionsPanel);
     	
     	// listener for options panel events to determine if we are solving for sample size or power
     	optionsPanel.addListener(this);
     	optionsPanel.addListener(resultsPanel);
         // add style to container and panel stack
         panelStack.setStyleName(DECK_STYLE);
         container.setStyleName(STYLE);
         
         // initialize the composite widget
         initWidget(container);
     }
     
     public void onPrevious()
     {
         int visibleIndex = panelStack.getVisibleWidget();
         switch(visibleIndex)
         {
         case PANEL_STACK_STUDY_DESIGN:
             updateStep(PANEL_STACK_START, false, true, true);
             break;
         case PANEL_STACK_OPTIONS:
             updateStep(PANEL_STACK_STUDY_DESIGN, true, studyDesignValid, true);
             break;
         case PANEL_STACK_RESULTS:
             updateStep(PANEL_STACK_OPTIONS, true, optionsValid, true);
             break;
         default:
             break;
         };
         stepsLeftPanel.onPrevious();
     }
     
     private void updateStep(int index, boolean allowPrevious, boolean allowNext, boolean allowCancel)
     {
         panelStack.showWidget(index);
         navPanel.setPrevious(allowPrevious);
         navPanel.setNext(allowNext);
         navPanel.setCancel(allowCancel);
     }
     
     public void onNext()
     {    	
         navPanel.setPrevious(true);
         int visibleIndex = panelStack.getVisibleWidget();
         switch(visibleIndex)
         {
         case PANEL_STACK_START:
             updateStep(PANEL_STACK_STUDY_DESIGN, true, studyDesignValid, true);
             break;
         case PANEL_STACK_STUDY_DESIGN:
             updateStep(PANEL_STACK_OPTIONS, true, optionsValid, true);
             break;
         case PANEL_STACK_OPTIONS:
             retrieveResults();
             updateStep(PANEL_STACK_RESULTS, true, false, true);
             break;
         default:
             break;
         };
         
         stepsLeftPanel.onNext();
     }
     
     public void onCancel()
     {
         updateStep(PANEL_STACK_START, false, true, true);
         stepsLeftPanel.onCancel();
     }
     
     public void addNavigationListener(NavigationListener listener)
     {
         navPanel.addNavigationListener(listener);
     }
     
     private void retrieveResults()
     {
         retrievePowerSampleSizeResults();
        curveForm.setAction(POWER_CURVE_URL);
        curveRequestHidden.setValue(buildPowerCurveRequestXML());
        curveForm.submit();
     }
     
     private void retrievePowerSampleSizeResults()
     {    	
         waitDialog.center();
     	RequestBuilder builder = null;
     	if (solveForPower)
     	    builder = new RequestBuilder(RequestBuilder.POST, POWER_URL);
     	else
             builder = new RequestBuilder(RequestBuilder.POST, SAMPLE_SIZE_URL);
 
     	try 
     	{
     		builder.setHeader("Content-Type", "text/xml");
     		builder.sendRequest((solveForPower ? buildPowerRequestXML() : buildSampleSizeRequestXML()),
     		        new RequestCallback() {
 
     			public void onError(Request request, Throwable exception) 
     			{
     				waitDialog.hide();
     				resultsPanel.setErrorResults("Calculation failed: " + exception.getMessage());	
     			}
 
     			public void onResponseReceived(Request request, Response response) 
     			{
     				waitDialog.hide();
     				if (STATUS_CODE_OK == response.getStatusCode() ||
     						STATUS_CODE_CREATED == response.getStatusCode()) 
     				{
     				    if (solveForPower)
     				        resultsPanel.setPowerResults(response.getText());
     				    else
     				        resultsPanel.setSampleSizeResults(response.getText());
     				} 
     				else 
     				{
     					resultsPanel.setErrorResults("Calculation failed: [HTTP STATUS " + 
     					        response.getStatusCode() + "] " + response.getText());
     				}
     			}
     		});
     	} 
     	catch (Exception e) 
     	{
 			waitDialog.hide();
 			resultsPanel.setErrorResults("Failed to send the request: " + e.getMessage());
     	}
     }
 
     private String buildPowerCurveRequestXML()
     {
         StringBuffer buffer = new StringBuffer();
         buffer.append("<power modelName='" + modelName + "' >");
         buffer.append(optionsPanel.getGraphicsOptions());
         buffer.append("<params " + studyDesignPanel.getStudyAttributes() + " " +
                 optionsPanel.getPowerAttributes() + ">");
         buffer.append(studyDesignPanel.getStudyXML(optionsPanel.getRowMetaDataXML()));
         buffer.append("</params></power>");
         return buffer.toString();
     }
     
     private String buildPowerRequestXML()
     {      
     	StringBuffer buffer = new StringBuffer();
     	buffer.append("<power modelName='" + modelName + "' >");
 		buffer.append("<params " + studyDesignPanel.getStudyAttributes() + " " +
 		        optionsPanel.getPowerAttributes() + ">");
 		buffer.append(studyDesignPanel.getStudyXML(optionsPanel.getRowMetaDataXML()));
 		buffer.append("</params></power>");
     	return buffer.toString();
     }
     
     private String buildSampleSizeRequestXML()
     {
         StringBuffer buffer = new StringBuffer();
         buffer.append("<sampleSize modelName='" + modelName + "' >");
         buffer.append("<params " + studyDesignPanel.getStudyAttributes() + " " +
                 optionsPanel.getSampleSizeAttributes() + ">");
         buffer.append(studyDesignPanel.getStudyXML(optionsPanel.getRowMetaDataXML()));
         buffer.append("</params></sampleSize>");
         return buffer.toString();
     }
     
     private DialogBox createWaitDialog()
     {
         DialogBox dialogBox = new DialogBox();
         HTML text = new HTML("Processing, Please Wait...");
         text.setStyleName("waitDialogText");
         dialogBox.setStyleName("waitDialog");
         dialogBox.setWidget(text);
         return dialogBox;
     }
     
     public void onShowCurve(boolean show, CurveOptions opts)
     {
         showCurve = show;
         curveOpts = opts;
     }
     
     public void onSolveFor(boolean power)
     {
         solveForPower = power;
     }
    
 
     
     public void onStudyUpload(Document doc, String modelName)
     {
         onModelSelect(modelName);
         this.onNext();
     }
     
     public void onModelSelect(String modelName)
     {
         this.modelName = modelName;
     }
     
     public void onStepInProgress(int stepIndex)
     {  
         if (stepIndex == PANEL_STACK_STUDY_DESIGN)
             studyDesignValid = false;
         else if (stepIndex == PANEL_STACK_OPTIONS)
             optionsValid = false;
         navPanel.setNext(false);
     }
     
     public void onStepComplete(int stepIndex)
     {
         if (stepIndex == PANEL_STACK_STUDY_DESIGN)
             studyDesignValid = true;
         else if (stepIndex == PANEL_STACK_OPTIONS)
             optionsValid = true;
         navPanel.setNext(true);
     }
 }
