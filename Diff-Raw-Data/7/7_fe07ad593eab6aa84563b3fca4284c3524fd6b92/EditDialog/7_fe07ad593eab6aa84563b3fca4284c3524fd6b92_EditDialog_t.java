 package org.jboss.jbossesb.eclipse.plugin.view.dialog;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jface.dialogs.TitleAreaDialog;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Spinner;
 
 import org.eclipse.swt.widgets.Text;
 import org.jboss.jbossesb.eclipse.plugin.controller.PropertiesManipulator;
 import org.jboss.jbossesb.eclipse.plugin.model.XMLAttribute;
 import org.jboss.jbossesb.eclipse.plugin.model.XMLElement;
 
 /**
  * Dialog class used for data modification of ESB objects.
  * 
  * @author Tomas Sedmik, tomas.sedmik@gmail.com
  * @since 2013-04-08
  */
 public class EditDialog extends TitleAreaDialog {
 
 	private XMLElement data;
 	private QueueTuple root;
 
 	public EditDialog(Shell parentShell, XMLElement data) {
 		super(parentShell);
 		this.data = data;
 	}
 
 	@Override
 	public void create() { 
 		super.create();
 
 		setTitle(data.getName());
 		setDefaultMessage();
 	}
 
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		
 		// layout
 		//FIXME correct dialog layout
 		Composite composite = new Composite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
         composite.setLayout(new GridLayout());
 		
 		// add content
 		root = new QueueTuple(data.getAddress(), composite, null, null);
 		addAttributes(root, data); // root XMLElement attributes
 		addElements(root, data); // add child elements
 		addOperations(root); // add edit options
 	    	    
 		return parent;
 	}
 	
 
 	@Override
 	protected void createButtonsForButtonBar(Composite parent) {
 		
 		// layout
 		GridData gridData = new GridData();
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.horizontalSpan = 3;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.horizontalAlignment = SWT.CENTER;
 		parent.setLayoutData(gridData);
 		
 		// create OK button
 		createOkButton(parent, OK, "OK", true);
 
 		// create Cancel button
 		Button cancelButton = createButton(parent, CANCEL, "Cancel", false);
 		
 		// add a SelectionListener
 		cancelButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				setReturnCode(CANCEL);
 				close();
 			}
 		});
 	}
 	
 	@Override
 	protected boolean isResizable() {
 		return true;
 	}
 	
 	@Override
 	protected void okPressed() {
 		
 		saveInput();
 		super.okPressed();
 	}
 
 	/**
 	 * Clears error message and set standard information message
 	 */
 	private void setDefaultMessage() {
 		
 		setErrorMessage(null);
 		
 		// Set the hint
 		if (data.getHint() != null) {
 			setMessage(data.getHint(), IMessageProvider.INFORMATION);
 		} else {
 			setMessage("");
 		}
 	}
 	
 	/**
 	 * Creates a group object as a container for other objects
 	 * 
 	 * @param parent parent object
 	 * @param name text displays as name of the group
 	 * @return composite object (container) that is child of the group
 	 */
 	private Composite setGroupLayout(Composite parent, String name) {
 		
 		// layout (group)
 		Group group = new Group(parent, 0);
 		group.setText(name);
 		group.setLayout(new FillLayout());
 				
 		Composite composite = new Composite(group, 0);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		layout.horizontalSpacing = GridData.FILL;
 		composite.setLayout(layout);
 		
 		return composite;
 	}
 	
 	/**
 	 * Searches whether the dialog already contains object with defined addresss 
 	 *  
 	 * @param parent parental tree object
 	 * @param address address of searched object
 	 * @return true - object is already displayed, false - otherwise
 	 */
 	private boolean isObjectExists(QueueTuple parent, String address) {
 		
 		if (parent == null || address == null) {
 			return false;
 		}
 		
 		if (parent.getChildren() != null) {
 			for (QueueTuple tuple : parent.getChildren()) {
 				if (tuple.getAddress().equals(address)) {
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Creates an OK button with input validation.
 	 * 
 	 * @param parent Parent.
 	 * @param id Button id.
 	 * @param label Text on the button.
 	 * @param defaultButton Is button default?
 	 * @return The OK Button.
 	 */
 	private Button createOkButton(Composite parent, int id, String label, boolean defaultButton) {
 		
 		// increment the number of columns in the button bar
 		((GridLayout) parent.getLayout()).numColumns++;
 		Button button = new Button(parent, SWT.PUSH);
 		button.setText(label);
 		button.setFont(JFaceResources.getDialogFont());
 		button.setData(new Integer(id));
 		button.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				if (isValidInput()) {
 					okPressed();
 				}
 			}
 		});
 		if (defaultButton) {
 			Shell shell = parent.getShell();
 			if (shell != null) {
 				shell.setDefaultButton(button);
 			}
 		}
 		setButtonLayoutData(button);
 		return button;
 	}
 
 	/**
 	 * Input validation.
 	 * 
 	 * @return true - input is valid, false - otherwise
 	 */
 	private boolean isValidInput() {
 				
 		// go through the tree representation of the element
 		Stack<QueueTuple> stack = new Stack<QueueTuple>();
 		stack.push(root);
 		while (!stack.isEmpty()) {
 			
 			QueueTuple tuple = stack.pop();
 			
 			// Skip operations group 
 			if (tuple.getAddress().equals("o")) {
 				continue;
 			}
 			
 			if (tuple.getChildren() == null) {
 				
 				// validation (it's an attribute)
 				String value = extractAttributeValueFromControl(tuple.getControl());
 				if (!isValidValue(value, tuple.getAddress())) {
 					return false;
 				}
 			} else {
 				for (QueueTuple temp : tuple.getChildren()) {
 					stack.push(temp);
 				}
 			}
 		}
 		
 		return true;	
 	}
 	
 	/**
 	 * Extract String representation of value stored in the Control
 	 * 
 	 * @param control View object (Button, Combo, Text, Spinner)
 	 * @return value stored in the Control, null - in case that control doesn't contains any information
 	 */
 	private String extractAttributeValueFromControl(Control control) {
 		
 		if (control == null) {
 			return null;
 		}
 		
 		if (control instanceof Text) {
 			return ((Text) control).getText();
 		} else if (control instanceof Spinner) {
 			return ((Spinner) control).getText();
 		} else if (control instanceof Combo) {
 			return ((Combo) control).getText();
 		} else if (control instanceof Button) {
 			Button temp = (Button) control;
 			if (temp.getSelection()) {
 				return "true";
 			} else {
 				return "false";
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Check whether value is in the definition set of the attribute given by the XML address.
 	 * If value isn't valid, set the error message.
 	 * 
 	 * @param value value of the attribute
 	 * @param address XML address of processed attribute
 	 * @return true - value is valid, false - otherwise
 	 */
 	private boolean isValidValue(String value, String address) {
 		
 		// load configuration
 		PropertiesManipulator prop = new PropertiesManipulator(address);
 		
 		// Text validation (String255)
 		if (prop.getSomeAttributeValue(address, AttributeValues.TYPE).equals("String255") &&
 				value.length() > 255) {
 			String object = prop.getSomeAttributeValue(address, AttributeValues.NAME);
 			setErrorMessage(object + " can be only 255 characters long!");
 			return false;
 		}
 		
 		// Number validation
 		if (prop.getSomeAttributeValue(address, AttributeValues.TYPE).equals("int") ||
 				prop.getSomeAttributeValue(address, AttributeValues.TYPE).equals("Decimal")) {
 			
 			try {
 				if (Integer.parseInt(value) < 0) {
 					String object = prop.getSomeAttributeValue(address, AttributeValues.NAME);
 					setErrorMessage(object + " must be a positive number!");
 					return false;
 				}
 			} catch (NumberFormatException e) {
 				String object = prop.getSomeAttributeValue(address, AttributeValues.NAME);
 				setErrorMessage(object + " must be a number!");
 				return false;
 			}
 		}
 		
 		// Required value validation
 		if (prop.getSomeAttributeValue(address, AttributeValues.REQUIRED).startsWith("R") && 
 				value.length() == 0) {
 			String object = prop.getSomeAttributeValue(address, AttributeValues.NAME);
 			setErrorMessage(object + " is required (must be filled)!");
 			return false;
 		}
 		
 		return true;
 	}
 
 	/**
 	 * Saves the state of input objects of GUI to the XMLElement data model
 	 */
 	private void saveInput() {
 		
 		// TODO check if attribute value was changed (against model)
 		// TODO check if elements were changed (against model)
 		
 		// process root of the tree
 		List<XMLAttribute> attributes = new ArrayList<XMLAttribute>();
 		List<XMLElement> elements = new ArrayList<XMLElement>();
 		for (QueueTuple tuple : root.getChildren()) {
 			
 			// skip operations group
 			if (tuple.getAddress().equals("o")) {
 				continue;
 			}
 			
 			// attributes
 			PropertiesManipulator prop = new PropertiesManipulator(tuple.getAddress());
 			if (prop.getSomeAttributeValue(tuple.getAddress(), AttributeValues.ATTR_OR_ELEM).equals("A")) {
				
				// if value of attribute is null skip
				String value = extractAttributeValueFromControl(tuple.getControl());
				if (value == null || value.equals("")) {
					continue;
				}
				
 				XMLAttribute attr = createAttribute(tuple);
 				attributes.add(attr);
 			} 
 			
 			// add elements to the processing stack
 			else if (tuple.getChildren() != null) {
 				XMLElement elem = createElement(tuple);
 				elements.add(elem);
 			}
 		}
 		data.setAttributes(attributes);
 		
 		// hack for Globals setting
 		if (data.getAddress().equals("/jbossesb")) {
 			for (XMLElement elem : data.getChildren()) {
 				if (elem.getAddress().equals("/jbossesb/globals")) {
 					data.removeChild(elem);
 					break;
 				}
 			}
 			if (!elements.isEmpty()) {
 				data.addChildElement(elements.get(0));
 			}
 		} else {
 			data.setChildren(elements);
 		}
 		
 	}
 	
 	/**
 	 * Create a new XMLAttribute object based on QueueTuple (view object).
 	 * 
 	 * @param tuple view object (must contains Control with user data)
 	 * @return new XMLAttribute
 	 */
 	private XMLAttribute createAttribute(QueueTuple tuple) {
 		
 		PropertiesManipulator prop = new PropertiesManipulator(tuple.getAddress());
 		String type = prop.getSomeAttributeValue(tuple.getAddress(), AttributeValues.TYPE);
 		boolean required = prop.getSomeAttributeValue(tuple.getAddress(), AttributeValues.REQUIRED).startsWith("R") ? true : false;
 		
 		String allowedValues = prop.getSomeAttributeValue(tuple.getAddress(), AttributeValues.ALLOWED_VALUES);
 		List<String> allowedValues2 = new ArrayList<String>();
 		String[] temp = allowedValues.split(",");
 		for (int i = 0; i < temp.length; i++) {
 			allowedValues2.add(temp[i]);
 		}
 		
 		String name = prop.getSomeAttributeValue(tuple.getAddress(), AttributeValues.NAME);
 		String hint = prop.getSomeAttributeValue(tuple.getAddress(), AttributeValues.HINT);
 		
 		temp = tuple.getAddress().split("/");
 		String xmlName = temp[temp.length - 1];
 		
 		String value = extractAttributeValueFromControl(tuple.getControl());
 		
 		return new XMLAttribute(type, required, allowedValues2, value, name, hint, xmlName);
 	}
 	
 	/**
 	 * Create a new XMLElement and all children XMLElements and XMLAttributes (with recurse calls)
 	 * 
 	 * @param tuple identification of element
 	 * @return new XMLElement
 	 */
 	private XMLElement createElement(QueueTuple tuple) {
 		
 		// create new element
 		XMLElement newElement = new XMLElement();
 		newElement.setAddress(tuple.getAddress());
 		PropertiesManipulator prop = new PropertiesManipulator(tuple.getAddress());
 		newElement.setHint(prop.getSomeElementValue(tuple.getAddress(), ElementValues.HINT));
 		newElement.setName(prop.getSomeElementValue(tuple.getAddress(), ElementValues.NAME));
 		
 		// recurse - create children (elements and attributes)
 		List<XMLAttribute> attributes = new ArrayList<XMLAttribute>();
 		List<XMLElement> elements = new ArrayList<XMLElement>();
 		for (QueueTuple temp : tuple.getChildren()) {
 			
 			// skip operations group
 			if (temp.getAddress().equals("o")) {
 				continue;
 			}
 			
 			// attributes
 			if (prop.getSomeAttributeValue(temp.getAddress(), AttributeValues.ATTR_OR_ELEM).equals("A")) {
 				attributes.add(createAttribute(temp));
 			}
 			
 			// add elements to the processing stack
 			else if (temp.getChildren() != null) {
 				elements.add(createElement(temp));
 			}
 		}
 		newElement.setAttributes(attributes);
 		newElement.setChildren(elements);
 		
 		return newElement;
 	}
 
 	/**
 	 * Adds attributes of given element to the dialog
 	 * 
 	 * @param parent parental tree object
 	 * @param element element with data
 	 */
 	private void addAttributes(QueueTuple parent, XMLElement element) {
 		
 		// loading configuration
 		PropertiesManipulator propManipulator = new PropertiesManipulator(parent.getAddress());
 		Map<String, String> tempProperties = PropertiesManipulator.convertResourceBundleToMap(propManipulator.getResource());
 		Map<String, String> properties = PropertiesManipulator.alterProperties(tempProperties);
 		
 		// set layout
 		Composite composite = new Composite((Composite)parent.getControl(), 0);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		layout.horizontalSpacing = GridData.FILL;
 		composite.setLayout(layout);
 		
 		// get all attributes attached to the element
 		Map<String, String> attributes = PropertiesManipulator.getAttributesToElement(properties, parent.getAddress());
 		
 		// print attributes
 		for (String address : attributes.keySet()) {
 			
 			// set label (with char * if is required)
 			Label label = new Label(composite, SWT.NONE);
 			if (propManipulator.getSomeAttributeValue(address, AttributeValues.REQUIRED).equals("R")) {
 				label.setText(propManipulator.getSomeAttributeValue(address, AttributeValues.NAME) + "*");
 			} else {
 				label.setText(propManipulator.getSomeAttributeValue(address, AttributeValues.NAME));
 			}
 			
 			// create input fields
 			String type = propManipulator.getSomeAttributeValue(address, AttributeValues.TYPE);
 			String allowedValues = propManipulator.getSomeAttributeValue(address, AttributeValues.ALLOWED_VALUES);
 			Control input = null;
 			if (allowedValues.length() > 0) {
 				input = createComboInput(composite, address, element, allowedValues);
 			} else {
 				if (type.equals("boolean")) {
 					input = createCheckboxInput(composite, address, element);
 				} else if (type.equals("int") || type.equals("Decimal")) {
 					// TODO what difference is between int and Decimal?			
 					input = createSpinnerInput(composite, address, element);
 				} else {
 					input = createTextInput(composite, address, element);
 				}
 				
 			}
 			
 			// add to the tree representation of dialog
 			QueueTuple object = new QueueTuple(address, input, null, parent);
 			parent.addChildren(object);
 		}
 	}
 	
 	/**
 	 * Creates text input for method addAttributes.
 	 * 
 	 * @param parent parental view object
 	 * @param address XML address of added object
 	 * @param data XML data
 	 * @return created object
 	 */
 	private Control createTextInput(Composite parent, String address, XMLElement element) {
 		
 		Text text = new Text(parent, SWT.BORDER);
 		text.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
 		text.setText(extractAttibuteValueFromModel(address, element));
 		
 		return text;
 	}
 	
 	/**
 	 * Create combo box input for method addAttributes
 	 * 
 	 * @param parent parental view object
 	 * @param address XML address of added object
 	 * @param element XML data
 	 * @param allowedValues allowed values separate with ','
 	 * @return created object
 	 */
 	private Control createComboInput(Composite parent, String address, XMLElement element, String allowedValues) {
 		
 		Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
 		combo.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
 		String[] values = allowedValues.split(",");
 		for (int i = 0; i < values.length; i++) {
 			combo.add(values[i]);
 		}
 		
 		// set data (if exists)
 		String value = extractAttibuteValueFromModel(address, element);
 		if (!value.equals("")) {
 			combo.setText(value);
 		}
 		
 		return combo;
 	}
 	
 	/**
 	 * Create check box input for method addAttributes
 	 * 
 	 * @param parent parental view object
 	 * @param address XML address of added object
 	 * @param element XML data
 	 * @return created object
 	 */
 	private Control createCheckboxInput(Composite parent, String address, XMLElement element) {
 		Button button = new Button(parent, SWT.CHECK);
 		button.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
 		
 		// set data (if exists)
 		String value = extractAttibuteValueFromModel(address, element);
 		if (value.equals("true")) {
 			button.setSelection(true);
 		}
 		
 		return button;
 	}
 	
 	/**
 	 * Create spinner input for method addAttributes
 	 * 
 	 * @param parent parental view object
 	 * @param address XML address of added object
 	 * @param element XML data
 	 * @return created object
 	 */
 	private Control createSpinnerInput(Composite parent, String address, XMLElement element) {
 		
 		Spinner spinner = new Spinner(parent, 0);
 		spinner.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
 		
 		// set data (if exists)
 		String value = extractAttibuteValueFromModel(address, element);
 		if (!value.equals("")) {
 			spinner.setSelection(Integer.parseInt(value));
 		}
 		
 		return spinner;
 		
 	}
 	
 	/**
 	 * Method accesses attribute value of XML element.
 	 * 
 	 * @param address specify searched attribute with its XML address
 	 * @param element data that are searched.
 	 * @return attribute value, if exists. "" - otherwise
 	 */
 	private String extractAttibuteValueFromModel(String address, XMLElement element) {
 		
 		String value = "";
 		String[] temp = address.split("/");
 		String attributeXMLName = temp[temp.length - 1];
 		XMLAttribute attribute = element.getAttribute(attributeXMLName);
 		if (attribute != null && attribute.getValue() != null) {
 			value =  attribute.getValue();
 		} else {
 			// TODO if no data exists set a default value (save only if a user confirms it)
 		}
 		
 		return value;
 	}
 
 	
 	/**
 	 * Adds Elements to the dialog
 	 * 
 	 * @param parent parental tree object
 	 * @param element element with data
 	 */
 	private  void addElements(QueueTuple parent, XMLElement element) {
 		
 		// hack for "global settings" only
 		if (parent.getAddress().equals("/jbossesb")) {
 			
 			// add only element <globals>, if exists
 			for (XMLElement child : element.getChildren()) {
 				if (child.getAddress().equals("/jbossesb/globals")) {
 					addElement(parent, child);
 				}
 			}
 			return;
 		}
 		
 		// everything else
 		for (XMLElement child : element.getChildren()) {
 			addElement(parent, child);
 		}
 		
 		// add required elements (based on properties file)
 		PropertiesManipulator propManipulator = new PropertiesManipulator(parent.getAddress());
 		Map<String, String> temp = PropertiesManipulator.convertResourceBundleToMap(propManipulator.getResource());
 		Map<String, String> properties = PropertiesManipulator.alterProperties(temp);
 		Map<String, String> tempelements = PropertiesManipulator.getElementsFromMap(properties);
 		Map<String, String> elements = PropertiesManipulator.getElementsUnderElement(tempelements, parent.getAddress());
 		for (Map.Entry<String, String> entry : elements.entrySet()) {
 			String key = entry.getKey();
 			if (propManipulator.getSomeElementValue(key, ElementValues.REQUIRED).startsWith("R")) {
 				if (!isObjectExists(parent, key)) {
 					XMLElement temp2 = new XMLElement();
 					temp2.setAddress(key);
 					temp2.setName(propManipulator.getSomeElementValue(key, ElementValues.NAME));
 					addElement(parent, temp2);
 				}
 			}
 		}
 		
 	}
 	
 	/**
 	 * Adds single element to the dialog
 	 * 
 	 * @param parent parental tree object
 	 * @param element element with data
 	 */
 	private void addElement(QueueTuple parent, XMLElement element) {
 		
 		// layout
 		Composite composite = setGroupLayout((Composite)parent.getControl(), element.getName());
 		
 		// add element to the tree
 		QueueTuple tuple = new QueueTuple(element.getAddress(), composite, null, parent);
 		parent.addChildren(tuple);
 		
 		// add remove button
 		createRemoveButton(tuple);
 		
 		// add content
 		addAttributes(tuple, element);
 		addElements(tuple, element);
 		addOperations(tuple);
 	}
 	
 	/**
 	 * Adds operations with objects to the dialog
 	 * 
 	 * @param parent parental tree object
 	 */
 	private void addOperations(QueueTuple parent) {
 		
 		// layout (group)
 		Composite composite = null;
 		QueueTuple tuple = null;
 		
 		// loading configuration
 		PropertiesManipulator propManipulator;
 		// hack for "Global settings"
 		if (parent.getAddress().equals("/jbossesb")) {
 			propManipulator = new PropertiesManipulator("/jbossesb/globals");
 		} else {
 			propManipulator = new PropertiesManipulator(parent.getAddress());
 		}
 		Map<String, String> temp = PropertiesManipulator.convertResourceBundleToMap(propManipulator.getResource());
 		Map<String, String> properties = PropertiesManipulator.alterProperties(temp);
 		Map<String, String> tempelements = PropertiesManipulator.getElementsFromMap(properties);
 		Map<String, String> elements = PropertiesManipulator.getElementsUnderElement(tempelements, parent.getAddress());
 		
 		// compare with current model and add buttons
 		for (String key : elements.keySet()) {
 			
 			String required = propManipulator.getSomeElementValue(key, ElementValues.REQUIRED).substring(1);
 			String name = propManipulator.getSomeElementValue(key, ElementValues.NAME);
 			
 			// add "add buttons" if can
 			if (isObjectExists(parent, key)) {
 				if (required.equals("1N") || required.equals("0N")) {
 					if (composite == null) {
 						composite = setGroupLayout((Composite)parent.getControl(), "Operations");
 						tuple = new QueueTuple("o", composite, null, parent);
 						parent.addChildren(tuple);
 					}
 					createAddButton(tuple, key, name);
 				}
 			} else {
 				if (composite == null) {
 					composite = setGroupLayout((Composite)parent.getControl(), "Operations");
 					tuple = new QueueTuple("o", composite, null, parent);
 					parent.addChildren(tuple);
 				}
 				createAddButton(tuple, key, name);
 			}	
 		}
 	}
 	
 	/**
 	 * Call this method after every object manipulation (addition, remove). Renewss allowable operations.
 	 * 
 	 * @param parent parental tree object
 	 */
 	private void renewOperations(QueueTuple parent) {
 		
 		// dispose old group with operations
 		for (QueueTuple tuple : parent.getChildren()) {
 			if (tuple.getAddress().equals("o")) {
 				parent.removeChildren(tuple);
 				tuple.getControl().getParent().dispose();
 				break;
 			}
 		}
 		
 		// create new group with operations
 		addOperations(parent);
 		
 		// refresh view
 		parent.getControl().getShell().pack();
 		((Composite)parent.getControl()).layout();
 	}
 	
 	/**
 	 * Creates a button for adding objects to the form
 	 * 
 	 * @param parent parental tree object
 	 * @param address defined which object is added based on address in XML file
 	 * @param name button label
 	 */
 	private void createAddButton(final QueueTuple parent, final String address, String name) {
 		
 		// create button
 		Button button = new Button((Composite)parent.getControl(), SWT.PUSH);
 		button.setText("Add " + name);
 		button.setFont(JFaceResources.getDialogFont());
 		
 		// add reaction on click
 		button.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				setDefaultMessage();
 				createObject(parent.getParent(), address);
 				renewOperations(parent.getParent());
 			}
 		});
 	}
 	
 	/**
 	 * Creates object and puts it into the dialog
 	 * 
 	 * @param parent parental tree object
 	 * @param address defined which object is added based on address in XML file
 	 */
 	private void createObject(QueueTuple parent, String address) {
 		
 		// create mock object (only for store "address" and "name")
 		PropertiesManipulator prop = new PropertiesManipulator(address);
 		XMLElement temp = new XMLElement();
 		temp.setAddress(address);
 		temp.setName(prop.getSomeElementValue(address, ElementValues.NAME));
 		
 		// add objects to the view
 		addElement(parent, temp);
 		
 		// refresh view
 		parent.getControl().getShell().pack();
 		((Composite)parent.getControl()).layout();
 	}
 	
 	/**
 	 * Adds remove button into the dialog
 	 * 
 	 * @param target parental tree object
 	 */
 	private void createRemoveButton(final QueueTuple target) {
 		
 		// can be object removed?
 		PropertiesManipulator prop = new PropertiesManipulator(target.getAddress());
 		final String required = prop.getSomeElementValue(target.getAddress(), ElementValues.REQUIRED);
 		if (required.startsWith("R") && required.endsWith("1")) {
 			return;
 		}
 		
 		// create button
 		Button button = new Button((Composite)target.getControl(), SWT.PUSH);
 		button.setText("Remove");
 		button.setFont(JFaceResources.getDialogFont());
 				
 		// add reaction on click
 		button.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				
 				setDefaultMessage();
 				
 				// check if object can be removed
 				// if is required and is last one => can't be removed
 				if (required.startsWith("R")) {
 					int count = 0;
 					for (QueueTuple tuple : target.getParent().getChildren()) {
 						if (tuple.getAddress().equals(target.getAddress())) {
 							count++;
 						}
 					}
 					if (count == 1) {
 						// can't be removed
 						setErrorMessage("Element cannot be remove - at least one is required");
 						return;
 					}
 				}
 				
 				// delete from the tree
 				target.getParent().removeChildren(target);
 				
 				// delete from view
 				target.getControl().getParent().dispose();
 				
 				renewOperations(target.getParent());
 			}
 		});
 	}
 	
 
 	
 	
 }
