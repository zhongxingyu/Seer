 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.tools.objects.event;
 
 import java.util.HashMap;
 import java.util.Map;
 
 
 /**
  * This class represents an event emanated from a defined list of errors which
  * may occur during a test step execution. The supported errors are identified
  * by string constants in the inner class <code>ID</code>.
  * 
  * @author BREDEX GmbH
  * @created 04.04.2005
  */
 public class TestErrorEvent {
     /**
      * unsupported keyboard layout.
      */
     public static final String UNSUPPORTED_KEYBOARD_LAYOUT = 
         "TestErrorEvent.UnsupportedKeyboardLayout"; //$NON-NLS-1$
     
     /**
      * click point outside component
      */
     public static final String CLICKPOINT_INVALID =
         "TestErrorEvent.ClickPointInvalid"; //$NON-NLS-1$
 
     /**
      * click point outside of screen bounds
      */
     public static final String CLICKPOINT_OFFSCREEN =
         "TestErrorEvent.ClickPointOffscreen"; //$NON-NLS-1$
     
     /**
      * tree node not found
      */
     public static final String TREE_NODE_NOT_FOUND =
         "TestErrorEvent.TreeNodeNotFound"; //$NON-NLS-1$
     /**
      * unknown operator
      */
     public static final String UNKNOWN_OPERATOR =
         "TestErrorEvent.UnknownOperator"; //$NON-NLS-1$
     /**
      * malformed regular expression
      */
     public static final String MALFORMED_REGEXP =
         "TestErrorEvent.MalformedRegexp"; //$NON-NLS-1$
     /**
      * execution error
      */
     public static final String EXECUTION_ERROR =
         "TestErrorEvent.ExecutionError"; //$NON-NLS-1$
     /**
      * unsupported UI
      */
     public static final String UNSUPPORTED_UI = "TestErrorEvent.UnsupportedUI"; //$NON-NLS-1$
     /**
      * confirmation timeout
      */
     public static final String CONFIRMATION_TIMEOUT =
         "TestErrorEvent.ConfirmationTimeout"; //$NON-NLS-1$
     /**
      * invalid index
      */
     public static final String INVALID_INDEX = "TestErrorEvent.InvalidIndex"; //$NON-NLS-1$
     /**
      * invalid index or header string
      */
     public static final String INVALID_INDEX_OR_HEADER = "TestErrorEvent.InvalidIndexOrHeader"; //$NON-NLS-1$
     /**
      * input failed
      */
     public static final String INPUT_FAILED = "TestErrorEvent.InputFailed"; //$NON-NLS-1$
     /**
      * not editable
      */
     public static final String NOT_EDITABLE = "TestErrorEvent.NotEditable"; //$NON-NLS-1$
     /**
      * not found
      */
     public static final String NOT_FOUND = "TestErrorEvent.NotFound"; //$NON-NLS-1$
     /**
      * invalid input
      */
     public static final String INVALID_INPUT = "TestErrorEvent.InvalidInput"; //$NON-NLS-1$
     /**
      * invalid parameter value
      */
     public static final String INVALID_PARAM_VALUE = "TestErrorEvent.InvalidParameterValue"; //$NON-NLS-1$
     /**
      * table header not visible or not existing
      */
     public static final String NO_HEADER = "TestErrorEvent.NoHeader"; //$NON-NLS-1$
     /**
      * unsupported table header action
      */
     public static final String UNSUPPORTED_HEADER_ACTION = "TestErrorEvent.UnsupportedHeaderAction"; //$NON-NLS-1$
     /**
      * no active window
      */
     public static final String NO_ACTIVE_WINDOW = 
         "TestErrorEvent.NoActiveWindow"; //$NON-NLS-1$
     /**
      * no menu bar
      */
     public static final String NO_MENU_BAR = 
         "TestErrorEvent.NoMenuBar"; //$NON-NLS-1$
     /**
      * no selection
      */
     public static final String NO_SELECTION = "TestErrorEvent.NoSelection"; //$NON-NLS-1$
     /**
      * property not accessible
      */
     public static final String PROPERTY_NOT_ACCESSABLE =
         "TestErrorEvent.PropertyNotAccessable"; //$NON-NLS-1$
     /**
      * Renderer not supported
      */
     public static final String RENDERER_NOT_SUPPORTED =
         "TestErrorEvent.RendererNotSupported"; //$NON-NLS-1$
     /**
      * Component not found
      */
     public static final String COMP_NOT_FOUND = "TestErrorEvent.CompNotFound"; //$NON-NLS-1$
     /**
      * Popup menu not found
      */
     public static final String POPUP_NOT_FOUND = "TestErrorEvent.PopupNotFound"; //$NON-NLS-1$
     /**
      * Dropdown list not found
      */
     public static final String DROPDOWN_LIST_NOT_FOUND = "TestErrorEvent.DropdownListNotFound"; //$NON-NLS-1$
     /**
      * Dropdown menu not found
      */
     public static final String DROPDOWN_NOT_FOUND = "TestErrorEvent.DropdownNotFound"; //$NON-NLS-1$
     /**
      * not visible
      */
     public static final String NOT_VISIBLE = "TestErrorEvent.NotVisible"; //$NON-NLS-1$
     
     /**
      * timer not found
      */
     public static final String TIMER_NOT_FOUND = "TestErrorEvent.TimerNotFound"; //$NON-NLS-1$
     
     /**
      * timeout expired, e.g. for a wait for window action
      */
     public static final String TIMEOUT_EXPIRED = "TestErrorEvent.TimeoutExpired"; //$NON-NLS-1$
     
     /**
      * menu item not enabled
      */
     public static final String MENU_ITEM_NOT_ENABLED =
         "TestErrorEvent.MenuItemNotEnabled"; //$NON-NLS-1$
     /**
      * File I/O error
      */
     public static final String FILE_IO_ERROR =
         "TestErrorEvent.FileIOError"; //$NON-NLS-1$
     /**
      * Operation unsupported on test operating system
      */
     public static final String UNSUPPORTED_OPERATION_ERROR = "TestErrorEvent.UnsupportedOperation";  //$NON-NLS-1$
 
     /** 
      * Required file was not found
      */
     public static final String FILE_NOT_FOUND = "TestErrorEvent.FileNotFound"; //$NON-NLS-1$
     
     /** 
      * No such command exists
      */
     public static final String NO_SUCH_COMMAND = "TestErrorEvent.NoSuchCommand"; //$NON-NLS-1$
 
     /**
      * Operation unsupported in this toolkit
      */
     public static final String UNSUPPORTED_OPERATION_IN_TOOLKIT_ERROR = "TestErrorEvent.UnsupportedOperationInToolkit"; //$NON-NLS-1$
 
     /**
      * Operation unsupported in this toolkit
      */
     public static final String SECURITY_PROBLEM_IN_TOOLKIT_ERROR = "TestErrorEvent.SecurityProblemInToolkit"; //$NON-NLS-1$
 
     /**
      * Window activation failed
      */
     public static final String WINDOW_ACTIVATION_FAILED = "TestErrorEvent.WindowActivationFailed"; //$NON-NLS-1$
 
     /**
      * AUT could not be ended in good time.
      */
     public static final String AUT_NOT_ENDED = "TestErrorEvent.AutCouldNotEnd"; //$NON-NLS-1$
 
     /**
      * Clipboard not available.
      */
     public static final String CLIPBOARD_NOT_AVAILABLE = "TestErrorEvent.ClipboardNotAvailable"; //$NON-NLS-1$
 
     /**
      * Property keys
      */
     public static final class Property {
         /**
          * description key
          */
         public static final String DESCRIPTION_KEY = "guidancerErrorDescription"; //$NON-NLS-1$
 
         /**
          * key for description parameters
          */
         public static final String PARAMETER_KEY = "guidancerErrorParameter"; //$NON-NLS-1$
 
         /**
          * <code>OPERATOR_KEY</code>
          */
         public static final String OPERATOR_KEY = "guidancerOperator"; //$NON-NLS-1$
 
         /**
          * <code>PATTERN_KEY</code>
          */
         public static final String PATTERN_KEY = "guidancerPattern"; //$NON-NLS-1$
 
         /**
          * <code>ACTUAL_VALUE_KEY</code>
          */
         public static final String ACTUAL_VALUE_KEY = "guidancerActualValue"; //$NON-NLS-1$
         
         /** to prevent instantiation */
         private Property() {
             // do nothing
         }
     }
     /**
      * This class defines the supported errors.
      */
     public static final class ID {
         /**
          * If the verification of an implementation class fails. Value:
          * <code>VerifyFailed</code>.
          */
         public static final String VERIFY_FAILED = "TestErrorEvent.VerifyFailed";  //$NON-NLS-1$
         /**
          * If the generic graphics component name cannot be mapped to the real
          * graphics component (e.g. invalid object mapping or non-existing
          * component). Value: <code>ComponentNotFoundError</code>.
          */
         public static final String COMPONENT_NOT_FOUND_ERROR = "TestErrorEvent.CompNotFound";  //$NON-NLS-1$
         /**
          * Any kind of AUT server configuration error, for example:
          * <ul>
          * <li>Unsupported graphics component (no corresponding implementation
          * class found)</li>
          * <li>Relfection errors when invoking the implementation class method
          * </li>
          * </ul>
          * 
          * Value: <code>ConfigurationError</code>.
          */
         public static final String CONFIGURATION_ERROR = "TestErrorEvent.Config";  //$NON-NLS-1$
         /**
          * Any kind of error when the implementation class action method is
          * executed, for example:
          * <ul>
          * <li>Invalid method parameters</li>
          * <li>Illegal graphics component state</li>
          * <li>Robot errors (failed moving or clicking)</li>
          * </ul>
          * 
          * Value: <code>ImplClassActionError</code>.
          */
         public static final String IMPL_CLASS_ACTION_ERROR = "TestErrorEvent.Action";  //$NON-NLS-1$
         
         /** to prevent instantiation */
         private ID() {
             // do nothing
         }
     }
 
     /** The event ID. */
     private String m_id;
     
     /** Additional event properties. */
     private Map m_properties = new HashMap();
     
     /** Default constructor (required by Betwixt). */
     public TestErrorEvent() {
         // Nothing to be done.
     }
     /**
      * Creates a new event.
      * 
      * @param id The event ID.
      */
     public TestErrorEvent(String id) {
         m_id = id;
     }
     /**
      * @return The event properties. Both keys and values are strings.
      */
     public Map getProps() {
         return m_properties;
     }
     /**
      * Adds a property to the event. They store additional information which may
      * be evaluated by the event receiver.
      * 
      * @param key
      *            The property key.
      * @param property
      *            The property value.
      */
     public void addProp(String key, Object property) {
         m_properties.put(key, property);
     }
     /**
      * @return The event ID.
      */
     public String getId() {
         return m_id;
     }
     /**
      * @param id The event ID (required by Betwixt).
      */
     public void setId(String id) {
         m_id = id;
     }
 }
