 package SEURAT.editors;
 
 import java.util.Enumeration;
 import java.util.Vector;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.*;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Text;
 
 import SEURAT.events.RationaleUpdateEvent;
 
 import edu.wpi.cs.jburge.SEURAT.editors.ConsistencyChecker;
 import edu.wpi.cs.jburge.SEURAT.editors.DisplayUtilities;
 import edu.wpi.cs.jburge.SEURAT.editors.SelectItem;
 import edu.wpi.cs.jburge.SEURAT.rationaleData.Alternative;
 import edu.wpi.cs.jburge.SEURAT.rationaleData.ArgCategory;
 import edu.wpi.cs.jburge.SEURAT.rationaleData.ArgType;
 import edu.wpi.cs.jburge.SEURAT.rationaleData.Argument;
 import edu.wpi.cs.jburge.SEURAT.rationaleData.Assumption;
 import edu.wpi.cs.jburge.SEURAT.rationaleData.Claim;
 import edu.wpi.cs.jburge.SEURAT.rationaleData.Designer;
 import edu.wpi.cs.jburge.SEURAT.rationaleData.Importance;
 import edu.wpi.cs.jburge.SEURAT.rationaleData.Plausibility;
 import edu.wpi.cs.jburge.SEURAT.rationaleData.RationaleDB;
 import edu.wpi.cs.jburge.SEURAT.rationaleData.RationaleElement;
 import edu.wpi.cs.jburge.SEURAT.rationaleData.RationaleElementType;
 import edu.wpi.cs.jburge.SEURAT.rationaleData.Requirement;
 import edu.wpi.cs.jburge.SEURAT.views.RationaleExplorer;
 import edu.wpi.cs.jburge.SEURAT.views.TreeParent;
 
 /**
  * This class provides the editor for arguments.
  */
 /**
  * @author Administrator
  *
  */
 public class ArgumentEditor extends RationaleEditorBase {
 	public static RationaleEditorInput createInput(RationaleExplorer explorer, TreeParent tree,
 			RationaleElement parent, RationaleElement target, boolean new1) {
 		return new ArgumentEditor.Input(explorer, tree, parent, target, new1);
 	}
 
 	/**
 	 * This class provides caching features used when updating
 	 * properties of an argument remotely.
 	 */
 	private class DataCache
 	{
 		/**
 		 * last known good name for the argument 
 		 */
 		public String name;
 		/**
 		 * last known good description for the argument
 		 */
 		public String description;
 		/**
 		 * last known good designer for the argument
 		 */
 		public int designer;
 		/**
 		 * last known good argument stance (from typeChoice comboBox)
 		 */
 		int argumentStance;
 		/**
 		 * last known good plausibilty
 		 */
 		int plausibility;
 		/**
 		 * last known good amount
 		 */
 		int amount;
 		/**
 		 * last known good importance
 		 */
 		int importance;
 		/**
 		 * last known good argumentType (from typeBox combobox)
 		 */
 		int argumentType;
 
 		/**
 		 * last known good claim argument child 
 		 */
 		Claim selectedClaim;
 		/**
 		 * last known good requirement argument child 
 		 */
 		Requirement selectedRequirement;
 		/**
 		 * last known good assumption argument child 
 		 */
 		Assumption selectedAssumption;
 		/**
 		 * last known good alternative argument child 
 		 */
 		Alternative selectedAlternative;
 
 		/**
 		 * currently selected claim argument child 
 		 */
 		Claim curClaim = null;
 		/**
 		 * currently selected requirement argument child 
 		 */
 		Requirement curRequirement = null;
 		/**
 		 * currently selected assumption argument child 
 		 */
 		Assumption curAssumption = null;
 		/**
 		 * currently selected alternative argument child 
 		 */
 		Alternative curAlternative = null;
 		
 		/**
 		 * clear all argument child data, including the last known good
 		 * and current.
 		 */
 		public void resetArgumentData()
 		{
 			resetCachedArgumentData();
 			resetCurrentArgumentData();
 		}
 
 		/**
 		 * clear last known good argument child data
 		 */
 		public void resetCachedArgumentData()
 		{
 			selectedClaim = null;
 			selectedRequirement = null;
 			selectedAssumption = null;
 			selectedAlternative = null;
 		}
 		/**
 		 * clear current argument child data
 		 */
 		public void resetCurrentArgumentData()
 		{
 			curClaim = null;
 			curRequirement = null;
 			curAlternative = null;
 			curAssumption = null;
 		}
 	}
 	
 
 	/**
 	 * textbox for argument name
 	 */
 	private Text nameField;
 	/**
 	 * textbox for argument description
 	 */
 	private Text descArea;
 	/**
 	 * textbox for argument child
 	 */
 	private Text argArea;
 	/**
 	 * button for changing / selecting an argument child
 	 */
 	private Button selArgButton;
 	
 	/**
 	 * combobox for initial selection of designer
 	 */
 	private Combo designerBox;
 	/**
 	 * label for displaying designer
 	 */
 	private Label designerLabel;	
 	/**
 	 * composite used for switching between designerBox and designerLabel
 	 * after first save of new argument
 	 */
 	private Composite designerComposite;
 	
 	/**
 	 * combobox for argument child type
 	 */
 	private Combo typeBox;
 	/**
 	 *  combobox for argument plausibility
 	 */
 	private Combo plausibilityBox;
 	/**
 	 * combobox for argument importance
 	 */
 	private Combo importanceBox;
 	/**
 	 * combobox for argument amount
 	 */
 	private Combo amountBox;
 	/**
 	 * combobox for argument child stance
 	 */
 	private Combo typeChoice;
 	
 	/**
 	 * true if the argument child has been set
 	 */
 	private boolean argSet;
 	
 	/**
 	 * the SWT display parented to this
 	 * form.
 	 */
 	private Display ourDisplay;
 	
 	/**
 	 * member variable storing all last known good values
 	 * of the argument
 	 */
 	DataCache dataCache = new DataCache();
 	
 	/* (non-Javadoc)
 	 * @see SEURAT.editors.RationaleEditorBase#updateFormCache()
 	 */
 	@Override
 	protected void updateFormCache() {
 		if( nameField != null )
 			dataCache.name = nameField.getText();
 		
 		if( descArea != null )
 			dataCache.description = descArea.getText();
 		
 		if( designerBox != null )
 			dataCache.designer = designerBox.getSelectionIndex();
 		else
 			dataCache.designer = 0;
 		
 		if( typeBox != null )
 			dataCache.argumentType = typeBox.getSelectionIndex();
 		
 		if( typeChoice != null )
 			dataCache.argumentStance = typeChoice.getSelectionIndex();
 		
 		if( importanceBox != null )
 			dataCache.importance = importanceBox.getSelectionIndex();
 		
 		if( amountBox != null )
 			dataCache.amount = amountBox.getSelectionIndex();
 		
 		if( plausibilityBox != null )
 			dataCache.plausibility = plausibilityBox.getSelectionIndex();
 		
 		dataCache.resetArgumentData();
 		
 		if( getArgument().getClaim() != null ) 
 		{
 			dataCache.curClaim = dataCache.selectedClaim = getArgument().getClaim();
 		}
 		else if( getArgument().getRequirement() != null )
 		{
 			dataCache.curRequirement = dataCache.selectedRequirement = getArgument().getRequirement();
 		}
 		else if( getArgument().getAlternative() != null )
 		{
 			dataCache.curAlternative = dataCache.selectedAlternative = getArgument().getAlternative();
 		}
 		else if( getArgument().getAssumption() != null )
 		{
 			dataCache.curAssumption = dataCache.selectedAssumption = getArgument().getAssumption();
 		}
 	}
 
 	/**
 	 * Respond to remote changes to the argument.
 	 * 
 	 * @param pElement the argument which generated the event
 	 * @param pEvent description of what changes were made to
 	 * 			the argument
 	 */
 	public void onUpdate(Argument pElement, RationaleUpdateEvent pEvent)
 	{
 		if( pEvent.getElement().equals(getArgument()) )
 		{
 			if( pEvent.getModified() )
 				refreshForm(pEvent);
 			else
 			if( pEvent.getDestroyed() )
 				closeEditor();
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see SEURAT.editors.RationaleEditorBase#editorType()
 	 */
 	public Class editorType() {
 		return Argument.class;
 	}
 	
 	/* (non-Javadoc)
 	 * @see SEURAT.editors.RationaleEditorBase#getRationaleElement()
 	 */
 	public RationaleElement getRationaleElement() {
 		return getArgument();
 	}
 
 	/**
 	 * @return the argument being edited by this form
 	 */
 	public Argument getArgument() {
 		return (Argument)getEditorData().getAdapter(Argument.class);
 	}
 	
 	/* (non-Javadoc)
 	 * @see SEURAT.editors.RationaleEditorBase#onRefreshForm(SEURAT.events.RationaleUpdateEvent)
 	 */
 	public void onRefreshForm(RationaleUpdateEvent pEvent)
 	{
 		boolean l_dirty = isDirty();
 		
 		if( pEvent != null )
 			getArgument().fromDatabase(pEvent.getElement().getName());
 		
 		if( nameField.getText().equals(dataCache.name))
 		{
 			nameField.setText(getArgument().getName());
 			dataCache.name = nameField.getText();
 		}
 		else
 			l_dirty = true;
 		
 		unloadDesignerControls();
 		loadDesignerLabel();
 				
 		if( descArea.getText().equals(dataCache.description) )
 		{
 			descArea.setText(getArgument().getDescription());
 			dataCache.description = descArea.getText();
 		}
 		else
 			l_dirty = true;
 		
 		if( plausibilityBox.getSelectionIndex() == dataCache.plausibility )
 		{
 			Enumeration plausEnum = Plausibility.elements();
 			int j=0;
 			Plausibility stype;
 			while (plausEnum.hasMoreElements())
 			{
 				stype = (Plausibility) plausEnum.nextElement();
 				if (stype.toString().compareTo(getArgument().getPlausibility().toString()) == 0)
 				{
 					plausibilityBox.select(j);
 					dataCache.plausibility = plausibilityBox.getSelectionIndex();
 				}
 				j++;
 			}
 		}
 		else
 			l_dirty = true;
 		
 		if( amountBox.getSelectionIndex() == dataCache.amount )
 		{
 			for (int k = 1;k < 11; k++)
 			{
 				if (k == getArgument().getAmount())
 				{
 					amountBox.select(k-1);
 					dataCache.amount = amountBox.getSelectionIndex();
 				}
 			}
 		}
 		else
 			l_dirty = true;
 		
 		if( importanceBox.getSelectionIndex() == dataCache.importance )
 		{
 			Enumeration impEnum = Importance.elements();
 			int l=0;
 			Importance itype;
 			while (impEnum.hasMoreElements())
 			{
 				itype = (Importance) impEnum.nextElement();
 				
 				if (itype.toString().compareTo(getArgument().getImportance().toString()) == 0)
 				{
 					importanceBox.select(l);
 					dataCache.importance = importanceBox.getSelectionIndex();
 				}
 				l++;
 			}
 		}
 		else
 			l_dirty = true;
 		
 		if( typeBox.getSelectionIndex() == dataCache.argumentType )
 		{
 			if( !selectedAlternativeChanged() &&
 				!selectedRequirementChanged() &&
 				!selectedClaimChanged() &&
 				!selectedAssumptionChanged() )
 			{
 				// Only Update The Argument Stance If The Actual Argument
 				// That The Stance Relates To Hasn't Changed Either
 				if( typeChoice.getSelectionIndex() == dataCache.argumentStance )
 				{
 					refreshChoices();
					boolean hasSelected = false;
 					for (int h=0; h < typeChoice.getItemCount(); h++)
 					{
 						ArgType choiceArg = ArgType.fromString(typeChoice.getItem(h));
 						
 						if (choiceArg == getArgument().getType())
 						{
 							typeChoice.select(h);
 							dataCache.argumentStance = typeChoice.getSelectionIndex();
							hasSelected = true;
 						}
 					}
					if (!hasSelected){
						typeChoice.select(0); //To prevent "Index out of bound error"
					}
 				}
 				else
 					l_dirty = true;
 				
 				// Only Update The Argument Text Area If
 				// The Selected Argument Hasn't Changed
 				if (getArgument().getAlternative() != null)
 				{
 					argArea.setText(getArgument().getAlternative().toString());
 				}
 				else if (getArgument().getAssumption() != null)
 				{
 					argArea.setText(getArgument().getAssumption().toString());
 				}
 				else if (getArgument().getClaim() != null)
 				{
 					argArea.setText(getArgument().getClaim().toString());
 				}
 				else if (getArgument().getRequirement() != null)
 				{
 					argArea.setText(getArgument().getRequirement().toString());
 				}
 				else
 				{
 					argArea.setText("Undefined");
 				}
 			}
 			else
 				l_dirty = true;
 
 			dataCache.resetArgumentData();
 			
 			// Only Update The Argument Type If The Type
 			// Box Hasn't Changed
 			if (getArgument().getAlternative() != null)
 			{
 				typeBox.select(0);
 				dataCache.selectedAlternative = dataCache.curAlternative = getArgument().getAlternative();
 			}
 			else if (getArgument().getAssumption() != null)
 			{
 				typeBox.select(1);
 				dataCache.selectedAssumption = dataCache.curAssumption = getArgument().getAssumption();
 			}
 			else if (getArgument().getClaim() != null)
 			{
 				typeBox.select(2);
 				dataCache.selectedClaim = dataCache.curClaim = getArgument().getClaim();
 			}
 			else if (getArgument().getRequirement() != null)
 			{
 				typeBox.select(3);
 				dataCache.selectedRequirement = dataCache.curRequirement = getArgument().getRequirement();
 			}
 			else
 			{
 				typeBox.select(0);
 			}
 		}
 		else
 			l_dirty = true;		
 		
 		setDirty(l_dirty);
 	}
 	
 	/**
 	 * @return true if the currently selected alternative child
 	 * 		is different from the last known good alternative child
 	 */
 	public boolean selectedAlternativeChanged()
 	{
 		return !((dataCache.curAlternative != null &&
 				dataCache.curAlternative.equals(dataCache.selectedAlternative)) ||
 				(dataCache.curAlternative == null &&
 				dataCache.selectedAlternative == null));
 	}
 	
 	/**
 	 * @return true if the currently selected alternative child
 	 * 		is different from the last known good alternative child
 	 */
 	public boolean selectedClaimChanged()
 	{
 		return !((dataCache.curClaim != null &&
 				dataCache.curClaim.equals(dataCache.selectedClaim)) ||
 				(dataCache.curClaim == null &&
 				dataCache.selectedClaim == null));
 		
 	}
 	
 	/**
 	 * @return true if the currently selected assumption child
 	 * 		is different from the last known good assumption  child
 	 */
 	public boolean selectedAssumptionChanged()
 	{
 		return !((dataCache.curAssumption != null &&
 				dataCache.curAssumption.equals(dataCache.selectedAssumption)) ||
 				(dataCache.curAssumption == null &&
 				dataCache.selectedAssumption == null));
 	}
 	
 	/**
 	 * @return true if the currently selected requirement child
 	 * 		is different from the last known good requirement child
 	 */
 	public boolean selectedRequirementChanged()
 	{
 		return !((dataCache.curRequirement != null &&
 				dataCache.curRequirement.equals(dataCache.selectedRequirement)) ||
 				(dataCache.curRequirement == null &&
 				dataCache.selectedRequirement == null));		
 	}
 	
 	/**
 	 * remove any control being used to represent the
 	 * arguments designer in the editoir
 	 */
 	public void unloadDesignerControls()
 	{
 		if( designerLabel != null )
 		{
 			designerLabel.dispose();
 			designerLabel = null;
 		}
 		if( designerBox != null )
 		{
 			designerBox.dispose();
 			designerBox = null;
 		}		
 	}
 	
 	/**
 	 * load the designer combobox into the editor
 	 */
 	public void loadDesignerComboBox()
 	{
 		designerBox = new Combo(designerComposite, SWT.NONE);
 		designerBox.select(0);	
 		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		DisplayUtilities.setComboDimensions(designerBox, gridData, 50);
 		gridData.horizontalSpan = 5;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		designerBox.setLayoutData(gridData);
 		designerBox.addModifyListener(getNeedsSaveListener());
 		
 		RationaleDB db = RationaleDB.getHandle();
 		Vector ourDesigners = db.getNameList(RationaleElementType.DESIGNER);
 		if (ourDesigners != null)
 		{
 			Enumeration desEnum = ourDesigners.elements();
 			while (desEnum.hasMoreElements())
 			{
 				String des = (String) desEnum.nextElement();
 				if (des.compareTo("Designer-Profiles") != 0)
 				{
 					designerBox.add( des );					
 				}
 			}
 		}
 	}
 	
 	/**
 	 * load a label describing the designer into the editor
 	 */
 	public void loadDesignerLabel()
 	{
 		designerLabel = new Label(designerComposite, SWT.NONE);
 		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
 		gridData.horizontalSpan = 5;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		designerLabel.setLayoutData(gridData);
 		
 		if (getArgument().getDesigner() != null)
 		{
 			designerLabel.setText(getArgument().getDesigner().getName());
 		}
 		else
 		{
 			designerLabel.setText("No designer is associated with this Argument");		
 		}
 		
 		designerComposite.layout();
 	}
 	
 	/* (non-Javadoc)
 	 * @see SEURAT.editors.RationaleEditorBase#setupForm(org.eclipse.swt.widgets.Composite)
 	 */
 	public void setupForm(Composite parent) {
 		ourDisplay = parent.getDisplay();
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 6;
 		gridLayout.marginHeight = 5;
 		gridLayout.makeColumnsEqualWidth = true;
 		parent.setLayout(gridLayout);
 		
 		if (isCreating() && getArgument().getPtype() != RationaleElementType.ARGUMENT)
 		{
 			argSet = false;
 			getArgument().setImportance(Importance.DEFAULT);
 			getArgument().setPlausibility(Plausibility.HIGH);
 			getArgument().setAmount(10);
 		}
 		else
 		{
 			argSet = true;
 		}
 		
 		new Label(parent, SWT.NONE).setText("Name:");
 		
 		nameField =  new Text(parent, SWT.SINGLE | SWT.BORDER);
 		nameField.setText(getArgument().getName());
 		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
 		DisplayUtilities.setTextDimensions(nameField, gridData, 50);
 		gridData.horizontalSpan = 5;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		
 		nameField.addModifyListener(getNeedsSaveListener());
 		nameField.setLayoutData(gridData);
 				
 		
 		designerComposite = new Composite(parent, SWT.NONE);
 		gridLayout = new GridLayout();
 		gridLayout.numColumns = 6;
 		gridLayout.marginHeight = 0;
 		gridLayout.marginWidth = 0;
 		gridLayout.makeColumnsEqualWidth = true;
 		designerComposite.setLayout(gridLayout);
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		gridData.horizontalSpan = 6;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		designerComposite.setLayoutData(gridData);
 
 		new Label(designerComposite, SWT.NONE).setText("Designer:");
 		
 		if (isCreating())
 		{
 			loadDesignerComboBox();
 		}
 		else
 		{		
 			loadDesignerLabel();
 		}
 		
 		new Label(parent, SWT.NONE).setText("Description:");
 		
 		descArea = new Text(parent, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
 		descArea.setText(getArgument().getDescription());
 		descArea.addModifyListener(getNeedsSaveListener());
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		DisplayUtilities.setTextDimensions(descArea, gridData, 50, 5);
 		gridData.horizontalSpan = 5;
 		gridData.heightHint = descArea.getLineHeight() * 3;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.horizontalAlignment = GridData.FILL;
 		descArea.setLayoutData(gridData);
 		
 		new Label(parent, SWT.NONE).setText("Type:");
 		
 		typeChoice = new Combo(parent, SWT.NONE);
 		refreshChoices();
 		if ((isCreating()) && (getArgument().getPtype() != RationaleElementType.ARGUMENT))
 		{
 			typeChoice.select(0);
 		}
 		else
 		{
 			for (int h=0; h < typeChoice.getItemCount(); h++)
 			{
 				ArgType choiceArg = ArgType.fromString(typeChoice.getItem(h));
 				
 				if (choiceArg == getArgument().getType())
 				{
 					typeChoice.select(h);
 				}
 			}
 		}
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		typeChoice.setLayoutData(gridData);
 		typeChoice.addModifyListener(getNeedsSaveListener());
 		
 		new Label(parent, SWT.NONE).setText("Plausibility:");
 		
 		plausibilityBox = new Combo(parent, SWT.NONE);
 		Enumeration plausEnum = Plausibility.elements();
 		int j=0;
 		Plausibility stype;
 		while (plausEnum.hasMoreElements())
 		{
 			stype = (Plausibility) plausEnum.nextElement();
 			plausibilityBox.add( stype.toString() );
 			if (stype.toString().compareTo(getArgument().getPlausibility().toString()) == 0)
 			{
 				plausibilityBox.select(j);
 //				System.out.println(j);
 			}
 			j++;
 		}
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		plausibilityBox.setLayoutData(gridData);
 		plausibilityBox.addModifyListener(getNeedsSaveListener());
 		
 		new Label(parent, SWT.NONE).setText("Amount:");
 		
 		amountBox = new Combo(parent, SWT.NONE);
 		int k;
 		for (k = 1;k < 11; k++)
 		{
 			amountBox.add(  new Integer(k).toString());
 			if (k == getArgument().getAmount())
 			{
 				amountBox.select(k-1);
 			}
 		}
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		amountBox.setLayoutData(gridData);
 		amountBox.addModifyListener(getNeedsSaveListener());
 		
 		new Label(parent, SWT.NONE).setText("Importance:");
 		
 		importanceBox = new Combo(parent, SWT.NONE);
 		Enumeration impEnum = Importance.elements();
 		int l=0;
 		Importance itype;
 		while (impEnum.hasMoreElements())
 		{
 			itype = (Importance) impEnum.nextElement();
 			importanceBox.add( itype.toString() );
 			if (itype.toString().compareTo(getArgument().getImportance().toString()) == 0)
 			{
 				importanceBox.select(l);
 			}
 			l++;
 		}
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		importanceBox.setLayoutData(gridData);
 		importanceBox.addModifyListener(getNeedsSaveListener());
 		
 		int argT;
 		// Determine the type of argument currently being edited
 		if ((!isCreating()) || (getArgument().getPtype() == RationaleElementType.ARGUMENT))
 		{			
 			if (getArgument().getAlternative() != null)
 			{
 				argT = 0;
 			}
 			else if (getArgument().getAssumption() != null)
 			{
 				argT = 1;
 			}
 			else if (getArgument().getClaim() != null)
 			{
 				argT = 2;
 			}
 			else if (getArgument().getRequirement() != null)
 			{
 				argT = 3;
 			}
 			else
 			{
 				argT = 4;
 			}
 		}
 		else
 		{
 			argT = 4;
 		}
 		
 		//	another type...
 		new Label(parent, SWT.NONE).setText("Argument Type:");
 		
 		typeBox = new Combo(parent, SWT.NONE);
 		typeBox.add("Alternative");
 		typeBox.add("Assumption");
 		typeBox.add("Claim");
 		typeBox.add("Requirement");
 		typeBox.add("None Selected");
 		typeBox.select(argT);
 		
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		typeBox.setLayoutData(gridData);
 		typeBox.addModifyListener(getNeedsSaveListener());
 		
 		
 		selArgButton = new Button(parent, SWT.PUSH); 
 		selArgButton.setText("Select");
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
 		selArgButton.setLayoutData(gridData);
 		selArgButton.addSelectionListener(new SelectionAdapter() {
 			
 			public void widgetSelected(SelectionEvent event) 
 			{
 				RationaleElementType selType = RationaleElementType.fromString(typeBox.getItem(typeBox.getSelectionIndex()));
 				if (selType == RationaleElementType.REQUIREMENT)
 				{
 					Requirement newReq = null;
 					SelectItem sr = new SelectItem(ourDisplay, RationaleElementType.REQUIREMENT);
 					newReq = (Requirement) sr.getNewItem();
 					if (newReq != null)
 					{
 						getArgument().setRequirement(newReq);
 						dataCache.resetCurrentArgumentData();
 						dataCache.curRequirement = newReq;
 						argArea.setText(newReq.getName());
 						argSet = true;
 					}
 				}
 				else if (selType == RationaleElementType.ASSUMPTION)
 				{
 					Assumption newAssump = null;
 					SelectItem ar = new SelectItem(ourDisplay, RationaleElementType.ASSUMPTION);
 					newAssump = (Assumption) ar.getNewItem();
 					if (newAssump != null)
 					{
 						getArgument().setAssumption(newAssump);
 						dataCache.resetCurrentArgumentData();
 						dataCache.curAssumption = newAssump;
 						argArea.setText(newAssump.getName());
 						if (importanceBox.getItem(importanceBox.getSelectionIndex()).compareTo(Importance.DEFAULT.toString()) == 0)
 						{
 							int n = importanceBox.getItemCount();
 							boolean found = false;
 							int i = 0;
 							while ((!found) && (i < n))
 							{
 								if (importanceBox.getItem(i).compareTo(Importance.MODERATE.toString()) == 0)
 								{
 									found = true;
 									importanceBox.select(i);
 								}
 								i++;
 								
 							}
 							dataCache.importance = importanceBox.getSelectionIndex();
 						}
 						argSet = true;
 					}
 				}
 				else if (selType == RationaleElementType.CLAIM)
 				{
 					Claim newClaim = null;
 					SelectItem ar = new SelectItem(ourDisplay, RationaleElementType.CLAIM);
 					newClaim = (Claim) ar.getNewItem();
 					if (newClaim != null)
 					{
 						getArgument().setClaim(newClaim);
 						dataCache.resetCurrentArgumentData();
 						dataCache.curClaim = newClaim;
 						argArea.setText(newClaim.getName());
 						argSet = true;
 					}
 				}
 				else if (selType == RationaleElementType.ALTERNATIVE)
 				{
 					Alternative newAlt = null;
 					SelectItem aa = new SelectItem(ourDisplay, RationaleElementType.ALTERNATIVE);
 					newAlt = (Alternative) aa.getNewItem();
 					if (newAlt != null)
 					{
 						getArgument().setAlternative(newAlt);
 						dataCache.resetCurrentArgumentData();
 						dataCache.curAlternative = newAlt;
 						argArea.setText(newAlt.getName());
 						argSet = true;
 					}
 				}
 				if (argSet)
 					refreshChoices();
 			}
 		});
 		
 		new Label(parent, SWT.NONE).setText("");
 
 		new Label(parent, SWT.NONE).setText("Argues:");
 		
 		argArea = new Text(parent, SWT.READ_ONLY | SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
 		
 		if ((!isCreating()) || (getArgument().getPtype() == RationaleElementType.ARGUMENT))
 		{
 			if (getArgument().getAlternative() != null)
 			{
 				argArea.setText(getArgument().getAlternative().toString());
 			}
 			else if (getArgument().getAssumption() != null)
 			{
 				argArea.setText(getArgument().getAssumption().toString());
 			}
 			else if (getArgument().getClaim() != null)
 			{
 				argArea.setText(getArgument().getClaim().toString());
 			}
 			else if (getArgument().getRequirement() != null)
 			{
 				argArea.setText(getArgument().getRequirement().toString());
 			}
 			else
 			{
 				argArea.setText("Undefined");
 			}
 		}
 		else
 		{
 			argArea.setText("Undefined");
 		}
 		
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		DisplayUtilities.setTextDimensions(argArea, gridData, 50);
 		gridData.horizontalSpan = 5;
 		gridData.heightHint = descArea.getLineHeight() * 3;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.horizontalAlignment = GridData.FILL;
 		argArea.setLayoutData(gridData);
 		argArea.addModifyListener(getNeedsSaveListener());
 		
 		new Label(parent, SWT.NONE).setText(" ");
 		new Label(parent, SWT.NONE).setText(" ");
 		new Label(parent, SWT.NONE).setText(" ");
 		new Label(parent, SWT.NONE).setText(" ");
 		
 		try
 		{
 			RationaleDB.getHandle().Notifier().Subscribe(getArgument(), this, "onUpdate");
 		}
 		catch( Exception eError )
 		{
 			System.out.println("Argument Editor: Updates Unavailable");
 		}
 		
 		updateFormCache();
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see SEURAT.editors.RationaleEditorBase#saveData()
 	 */
 	public boolean saveData() {
 		ConsistencyChecker checker = new ConsistencyChecker(getArgument().getID(), nameField.getText(), "Arguments");
 		if (isCreating()) {
 			if (!argSet)
 			{
 				MessageBox mbox = new MessageBox(getSite().getShell(), SWT.ICON_ERROR);
 				mbox.setMessage("Need to select an alternative, claim, requirement, or assumption!");
 				mbox.open();
 			}
 			else if (!nameField.getText().trim().equals("") && 
 					getArgument().getName() == nameField.getText() || checker.check(false))
 			{
 				// Set the argument's parent data correctly
 				getArgument().setParent(getParentElement());
 				if (notCircular(getArgument()))
 				{
 					if ((designerBox.getItemCount() <= 0) || designerBox.getSelectionIndex() >= 0)
 					{
 						getArgument().setName(nameField.getText());
 						getArgument().setDescription(descArea.getText());
 						getArgument().setType(ArgType.fromString(typeChoice.getItem(typeChoice.getSelectionIndex())));
 						getArgument().setPlausibility( Plausibility.fromString(plausibilityBox.getItem(plausibilityBox.getSelectionIndex())));
 						getArgument().setImportance( Importance.fromString(importanceBox.getItem(importanceBox.getSelectionIndex())));
 						getArgument().setAmount( Integer.parseInt(amountBox.getItem(amountBox.getSelectionIndex())));
 						if (designerBox.getItemCount() > 0)
 						{
 							String designerName = designerBox.getItem(designerBox.getSelectionIndex());
 							Designer ourDes = new Designer();
 							ourDes.fromDatabase(designerName);
 							getArgument().setDesigner(ourDes);
 						}
 						
 						//			System.out.println("type " + getArgument().getType().toString());
 						
 //						comment before this made no sense...
 //						System.out.println("saving argument from edit");
 /*						
 						if (getArgument().getClaim() != null) {
 							// molerjc - this is a temporary hack - get the claim from the db and update it before we save the argument
 							Claim clm = new Claim();
 							clm.fromDatabase(getArgument().getClaim().getID());
 							getArgument().setClaim(clm);
 						}
 */
 						getArgument().setID(getArgument().toDatabase(getArgument().getParent(), getArgument().getPtype()));
 						return true;
 					}
 					else {
 						MessageBox mbox = new MessageBox(getSite().getShell(), SWT.ICON_ERROR);
 						mbox.setMessage("Need to provide the Designer name");
 						mbox.open();
 					}
 				}
 				else
 				{
 					MessageBox mbox = new MessageBox(getSite().getShell(), SWT.ICON_ERROR);
 					mbox.setMessage("Can not pre-suppose or oppose your self!");
 					mbox.open();
 				}
 			}
 			else
 			{
 				String l_message = "";
 				l_message += "The argument name you have specified is either already"
 					+ " in use or empty. Please make sure that you have specified"
 					+ " an argument name and the argument name does not already exist"
 					+ " in the database.";
 				MessageBox mbox = new MessageBox(getSite().getShell(), SWT.ICON_ERROR);
 				mbox.setMessage(l_message);
 				mbox.setText("Argument Name Is Invalid");
 				mbox.open();
 			}
 		} else {
 			if (notCircular(getArgument()))
 			{
 				if(getArgument().getName() == nameField.getText() || checker.check())
 				{
 					getArgument().setName(nameField.getText());
 					getArgument().setDescription(descArea.getText());
 					getArgument().setType(ArgType.fromString(typeChoice.getItem(typeChoice.getSelectionIndex())));
 					getArgument().setPlausibility( Plausibility.fromString(plausibilityBox.getItem(plausibilityBox.getSelectionIndex())));
 					getArgument().setImportance( Importance.fromString(importanceBox.getItem(importanceBox.getSelectionIndex())));
 					getArgument().setAmount( Integer.parseInt(amountBox.getItem(amountBox.getSelectionIndex())));
 					//since this is a save, not an add, the type and parent are ignored
 					//			System.out.println("saving argument from edit, ptype = " + getArgument().getPtype());
 					
 					if (getArgument().getClaim() != null) {
 						// molerjc - this is a temporary hack - get the claim from the db and update it before we save the argument
 						Claim clm = new Claim();
 						clm.fromDatabase(getArgument().getClaim().getID());
 						getArgument().setClaim(clm);
 					}
 					getArgument().setID(getArgument().toDatabase(getArgument().getParent(), getArgument().getPtype()));
 					return true;
 				}
 			}
 			else
 			{
 					MessageBox mbox = new MessageBox(getSite().getShell(), SWT.ICON_ERROR);
 					mbox.setMessage("Can not pre-suppose or oppose yourself!");
 					mbox.open();
 			}				
 		}
 		return false;
 	}
 	
 	/* 
 	 * Overloaded from base editor to update the designer
 	 * control when the element is saved for the first
 	 * time.
 	 * 
 	 * @see SEURAT.editors.RationaleEditorBase#saveNew()
 	 */
 	@Override
 	public TreeParent saveNew() {
 		unloadDesignerControls();
 		loadDesignerLabel();
 		
 		return super.saveNew();
 	}
 
 	/**
 	 * Check to make sure we aren't creating an argument for an alternative that opposes
 	 * or pre-supposes itself
 	 * @param ourArg the new argument
 	 * @return true if we are not circular
 	 */
 	private boolean notCircular(Argument ourArg)
 	{
 		boolean notcircular = true;
 		if (ourArg.getAlternative() != null)
 		{
 			//We need to read in our parent alternative to compare the names
 			Alternative parent = new Alternative();
 			parent.fromDatabase(ourArg.getParent());
 			String ourAlternativeName = ourArg.getAlternative().getName();
 			String parentName = parent.getName();
 			if (ourAlternativeName.compareTo(parentName) == 0)
 			{
 				notcircular = false;
 			}
 		}
 		return notcircular;
 	}
 	
 	/**
 	 * Sets the possible direction choices base on the type of argument we are making
 	 *
 	 */
 	private void refreshChoices()
 	{
 		if (getArgument() != null)
 		{
 			typeChoice.removeAll();
 			
 			if (getArgument().getClaim() != null)
 			{
 				typeChoice.add(ArgType.SUPPORTS.toString());
 				typeChoice.add(ArgType.DENIES.toString());
 			}
 			else if (getArgument().getAssumption() != null)
 			{
 				typeChoice.add(ArgType.SUPPORTS.toString());
 				typeChoice.add(ArgType.DENIES.toString());
 			}
 			else if (getArgument().getRequirement() != null)
 			{
 				typeChoice.add(ArgType.SATISFIES.toString());
 				typeChoice.add(ArgType.VIOLATES.toString());
 				typeChoice.add(ArgType.ADDRESSES.toString());
 			}
 			else if (getArgument().getAlternative() != null)
 			{
 				typeChoice.add(ArgType.PRESUPPOSES.toString());
 //				typeChoice.addItem(ArgType.PRESUPPOSEDBY);
 				typeChoice.add(ArgType.OPPOSES.toString());
 //				typeChoice.addItem(ArgType.OPPOSEDBY);
 			}
 			else if (getArgument().getCategory() == ArgCategory.NONE)
 			{
 				typeChoice.add(getArgument().getType().toString());
 			}
 			else
 			{
 				typeChoice.add("Select Argument");
 			}
 			
 			typeChoice.select(0);
 		}
 	}
 	
 	/**
 	 * Wraps an argument into a logical file.
 	 */
 	public static class Input extends RationaleEditorInput {
 		/**
 		 * @param explorer RationaleExplorer
 		 * @param tree the element in the RationaleExplorer tree for the argument
 		 * @param parent the parent of the argument in the RationaleExplorer tree
 		 * @param target the argument to associate with the logical file
 		 * @param new1 true if the argument is being created, false if it already exists
 		 */
 		public Input(RationaleExplorer explorer, TreeParent tree,
 				RationaleElement parent, RationaleElement target, boolean new1) {
 			super(explorer, tree, parent, target, new1);
 		}
 
 		/**
 		 * @return the argument wrapped in this logical file
 		 */
 		public Argument getData() { return (Argument)getAdapter(Argument.class); }
 		
 		/* (non-Javadoc)
 		 * @see SEURAT.editors.RationaleEditorInput#getName()
 		 */
 		@Override
 		public String getName() {
 			return isCreating() ? "New Argument Editor" :
 				"Argument: " + getData().getName();
 		}
 
 		/* (non-Javadoc)
 		 * @see SEURAT.editors.RationaleEditorInput#targetType(java.lang.Class)
 		 */
 		@Override
 		public boolean targetType(Class type) {
 			return type == Argument.class;
 		}		
 	}
 }
