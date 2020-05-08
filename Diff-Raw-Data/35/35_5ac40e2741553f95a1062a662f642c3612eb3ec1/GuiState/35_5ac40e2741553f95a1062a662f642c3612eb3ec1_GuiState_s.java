 package nl.tue.fingerpaint.client.gui;
 
 import nl.tue.fingerpaint.client.resources.FingerpaintConstants;
 
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.ToggleButton;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * A class that contains references to all the GUI elements used in the
  * Fingerpaint application.
  * 
  * @author Group Fingerpaint
  */
 public class GuiState {
 	// --- LOADING APPLICATION WIDGETS ----------------------------------------
 	/**
 	 * Panel that covers the entire application and blocks the user from //
 	 * accessing other features.
 	 */
 	public static FlowPanel loadingPanel = new FlowPanel();
 
 	/**
 	 * The message to be shown during the loading animation of the loading
 	 * panel.
 	 */
 	public static Label loadingPanelMessage = new Label();
 
 	// --- MENU WIDGETS -------------------------------------------------------
 	/** Vertical panel to contain all menu items. */
 	public static VerticalPanel menuPanel = new VerticalPanel();
 
 	/** Button to toggle whether the menu is visible. */
 	public static MenuToggleButton menuToggleButton;
 
 	// --- DRAWING TOOL WIDGETS -----------------------------------------------
 	/**
 	 * Pop-up panel which contains options for selecting a different drawing
 	 * tool.
 	 */
 	public static PopupPanel toolSelector;
 
 	/**
 	 * Panel in the pop-up panel to separate the tool selection and size
 	 * selection options for the drawing tool.
 	 */
 	public static HorizontalPanel popupPanelPanel;
 
 	/** Panel in the pop-up panel that contains the different drawing tools. */
 	public static VerticalPanel popupPanelMenu;
 
 	/** Numberspinner to change the size of the drawing tool. */
 	public static NumberSpinner cursorSizeSpinner;
 
 	/** Button to toggle between black and white drawing colour. */
 	public static ToggleColourButton toggleColor;
 
 	/** Button to change the shape of the selected drawing tool. */
 	// TODO: Change this to a button on which the current tool is drawn
 	public static Button toolSelectButton;
 
 	/** Button to select the square-shaped drawing tool. */
 	// TODO: Change this to a button on which a square is drawn
 	public static ToggleButton squareDrawingTool;
 
 	/** Button to select the circle-shaped drawing tool. */
 	// TODO: Change this to a button on which a circle is drawn
 	public static ToggleButton circleDrawingTool;
 
 	// --- INITIAL DISTRIBUTION WIDGETS ---------------------------------------
 	/** Button to save an initial concentration distribution. */
 	public static Button saveDistributionButton;
 
 	/** Button to load an initial concentration distribution. */
 	public static Button loadInitDistButton;
 
 	/** Button to reset the current distribution to completely white. */
 	public static Button resetDistButton;
 
 	// -- SAVE MIXING RESULTS WIDGETS -----------------------------------------
 	/** Button to save the current mixing result. */
 	public static Button saveResultsButton;
 
 	// --- REMOVE RESULTS WIDGETS ---------------------------------------------
 	/** Pop-up panel to handle the removing of results. */
 	public static PopupPanel removeResultsPanel;
 
 	/** Vertical panel to hold the flextable and close button. */
 	public static VerticalPanel removeResultsVerticalPanel;
 
 	/** Flextable to hold all the result entries. */
 	public static FlexTable resultsFlexTable;
 
 	/** Button to remove previously saved mixing results. */
 	public static Button removeSavedResultsButton;
 
 	/** Button to close the remove results pop-up panel. */
 	public static Button closeResultsButton;
 
 	// --- MIXING PROTOCOL WIDGETS --------------------------------------------
 	/** Container to hold all the widgets related to a mixing protocol. */
 	public static PopupPanel protocolPanelContainer;
 
 	/**
 	 * The numberspinner to define how many times the mixing protocol is
 	 * executed.
 	 */
 	public static NumberSpinner nrStepsSpinner;
 
 	/** Button to save the current mixing protocol. */
 	public static Button saveProtocolButton;
 
 	/** Button to load a mixing protocol. */
 	public static Button loadProtocolButton;
 
 	/** Button that resets the current mixing protocol when pressed. */
 	public static Button resetProtocolButton;
 
 	/**
 	 * Button that executes the current mixing run (initial distribution and
 	 * mixing protocol) when pressed.
 	 */
 	public static Button mixNowButton;
 
 	/**
 	 * Label to be displayed above the {@link #nrStepsSpinner}, to explain its
 	 * purpose.
 	 */
 	// TODO: The text '#steps' should be translated later on
 	public static Label nrStepsLabel = new Label(FingerpaintConstants.INSTANCE.nrSteps());
 
 	/**
 	 * Label that shows the textual representation of the current mixing
 	 * protocol.
 	 */
 	public static Label labelProtocolRepresentation = new Label();
 
 	/**
 	 * Label to be displayed above the protocol-related buttons, to explain
 	 * their purpose.
 	 */
 	public static Label labelProtocolLabel = new Label("Protocol:");
 
 	/**
 	 * Checkbox that needs to be checked to define a protocol. If it isn't
 	 * checked, steps (wall movements) are executed directly.
 	 */
 	public static CheckBox defineProtocolCheckBox;
 
 	// --- SAVE POP-UP MENU WIDGETS -------------------------------------------
 	/** Pop-up panel to handle the saving of the current results. */
 	public static PopupPanel saveItemPanel;
 
 	/**
 	 * Horizontal panel to hold the Save and Cancel buttons in the pop-up panel.
 	 */
 	public static HorizontalPanel saveButtonsPanel;
 
 	/**
 	 * Vertical panel to hold the textbox and the save button in the save pop-up
 	 * panel.
 	 */
 	public static VerticalPanel saveItemVerticalPanel;
 
 	/**
 	 * Button to save the item under the specified name in the
 	 * {@link #saveNameTextBox}.
 	 */
 	public static Button saveItemPanelButton;
 
 	/** Cancel button inside the save pop-up menu. */
 	public static Button cancelSaveResultsButton;
 
 	/** Ok / Cancel button to close the save results or overwrite pop-up panel. */
 	public static Button closeSaveButton;
 
 	/** Textbox to input the name in to name the file. */
 	public static TextBox saveNameTextBox;
 
 	/** Label to indicate that the chosen name is already in use. */
 	public static Label saveMessageLabel = new Label();
 
 	// --- OVERWRITE POP-UP MENU WIDGETS --------------------------------------
 	/**
 	 * Pop-up Panel that appears after the Save button in the save pop-up panel
 	 * has been pressed.
 	 */
 	public static PopupPanel overwriteSavePanel;
 
 	/**
 	 * Horizontal panel to hold the OK or Overwrite/Cancel button(s) in the
 	 * confirm save pop-up panel.
 	 */
 	public static HorizontalPanel overwriteButtonsPanel;
 
 	/**
 	 * Vertical panel to hold the save message and the OK/Overwrite button in
 	 * the confirm save pop-up panel.
 	 */
 	public static VerticalPanel overwriteSaveVerticalPanel;
 
 	/** Button to overwrite the item that is currently being saved. */
 	public static Button overwriteSaveButton;
 
 	// --- LOAD POP-UP MENU WIDGETS -------------------------------------------
 	/**
 	 * Vertical panel to hold the textbox and the cancel button in the load
 	 * pop-up panel.
 	 */
 	public static VerticalPanel loadVerticalPanel;
 
 	/** Pop-up panel to handle the loading of previously saved items. */
 	public static PopupPanel loadPanel = new PopupPanel();
 
 	/** Button to close the load pop-up menu. */
 	public static Button closeLoadButton;
 
 	// --- STEP SIZE WIDGETS --------------------------------------------------
 	/**
 	 * The numberspinner to define the step size of a single wall movement.
 	 */
 	public static NumberSpinner sizeSpinner;
 
 	// TODO: The text 'Step size' should be translated later on
 	/**
 	 * Label to be displayed above the {@link #sizeSpinner}, to explain its
 	 * purpose.
 	 */
 	public static Label sizeLabel = new Label("Step size");
 
 	// --- VIEW SINGLE GRAPH WIDGETS ------------------------------------------
 	/**
 	 * Pop-up menu to display the performance of a single graph. It is opened
 	 * when {@link #viewSingleGraph} is clicked. It contains a vertical panel.
 	 */
 	public static PopupPanel viewSingleGraphPopupPanel;
 
 	/**
 	 * Horizontal panel to contain the Close and Export buttons.
 	 */
 	public static HorizontalPanel viewSingleGraphHorizontalPanel;
 
 	/**
 	 * Vertical panel to contain the horizontal panel and simple panel of the
 	 * single graph pop-up.
 	 */
 	public static VerticalPanel viewSingleGraphVerticalPanel;
 
 	/**
 	 * Simple panel to display the graph of the previously executed mixing run.
 	 */
 	public static SimplePanel viewSingleGraphGraphPanel;
 
 	/** Button to view the performance of the previously executed mixing run. */
 	public static Button viewSingleGraph;
 
 	/** Button to close the performance pop-up. */
 	public static Button closeSingleGraphViewButton;
 
 	/** Button to export the image of the current mixing performance. */
 	public static Button exportSingleGraphButton;
 
 	// --- COMPARE PERFORMANCE WIDGETS ----------------------------------------
 	/**
 	 * Pop-up panel to display all the previously stored mixing runs with
 	 * performance. It also contains the Compare and Close buttons.
 	 */
 	public static PopupPanel compareSelectPopupPanel;
 
 	/**
 	 * Pop-up panel that displays the simple panel with the performance graph
 	 * and New Comparison and Close buttons.
 	 */
 	public static PopupPanel comparePopupPanel;
 
 	/**
 	 * Simple panel that displays a graph with the mixing performance of the
 	 * selected mixing runs.
 	 */
 	public static SimplePanel compareGraphPanel;
 
 	/**
 	 * Button to compare the performance of previously saved mixing runs. When
 	 * clicked, it opens the {@link #compareSelectPopupPanel} pop-up.
 	 */
 	public static Button comparePerformanceButton;
 
 	/**
 	 * Button to compare the performance of the selected mixing runs.
 	 */
 	public static Button compareButton;
 
 	/** Cancel button inside the compare performance pop-up. */
 	public static Button cancelCompareButton;
 
 	/** Close button inside the compare performance pop-up. */
 	public static Button closeCompareButton;
 
 	/**
 	 * Button inside the compare performance pop-up to start a new comparison.
 	 * When clicked, it closes the {@link #comparePopupPanel} pop-up and opens
 	 * the {@link #compareSelectPopupPanel}pop-up
 	 */
 	public static Button newCompareButton;
 
 }
