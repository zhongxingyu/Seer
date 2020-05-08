 package org.wingx.beans.beaneditor;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.beans.*;
 import java.lang.reflect.*;
 import java.util.*;
 import javax.swing.*;
 import javax.swing.table.*;
 
 import org.wings.*;
 import org.wingx.beans.*;
 import org.wingx.beans.editors.*;
 
 /**
  * Bean Editor <p>
  *
  * Usage:
  * <ul>
  *  <li>
  *   Via introspection:
  *   <code>
  *    BeanEditor editor = new BeanEditor(); <br>
  *    editor.setBean(bean); <br>
  *   </code>
  *  </li>
  *  <li>
  *   by providing a custom set of PropertyDescriptors:
  *   <code>
  *    BeanEditor editor = new BeanEditor(); <br>
  *    editor.setPropertyDescriptors(PropertyDescriptor[]); <br>
  *    editor.setBean(bean); <br>
  *   </code>
  *  </li>
  * </ul>
  *
  * @author <a href="mailto:hengels@mercatis.de">Holger Engels</a>,
  *         <a href="mailto:mreinsch@to.com">Michael Reinsch</a>
  * @version $Revision$
  */
 
 public class BeanEditor
     extends SPanel
     implements BeanEditorConstants, PropertyChangeListener
 {
     private static final PropertyDescriptor[] noPropertyDescriptors = new PropertyDescriptor[0];
 
     protected Class beanClass = null;
     protected Object bean = null;
     protected PropertyDescriptor[] propertyDescriptors = noPropertyDescriptors;
     protected boolean customPropertyDescriptors = false;
 
     protected Map properties = new HashMap();
     protected int size = 0;
 
     protected final SPanel editorPanel = new SPanel(new SGridLayout(2));
 
     public BeanEditor() {
 	super(new SFlowDownLayout());
         add(editorPanel);
     }
 
     public void setPropertyDescriptors(PropertyDescriptor[] descriptors) {
         propertyDescriptors = descriptors;
         customPropertyDescriptors = true;
 
         if (propertyDescriptors == null)
             propertyDescriptors = noPropertyDescriptors;
         else
             Arrays.sort(propertyDescriptors, new PropertyPriorityComparator());
 
         initComponents();
     }
     public PropertyDescriptor[] getPropertyDescriptors() { return propertyDescriptors; }
 
     public void introspect() {
         PropertyDescriptor[] descriptors = null;
         if (getBeanClass() != null) {
             try {
                 BeanInfo info = Introspector.getBeanInfo(getBeanClass());
                 descriptors = info.getPropertyDescriptors();
             }
             catch (IntrospectionException e) {
                 System.err.println(e.getMessage());
                 e.printStackTrace(System.err);
             }
         }
 
         setPropertyDescriptors(descriptors);
     }
 
     protected EditorAdapter getEditorAdapter(SPropertyEditor propertyEditor) {
         EditorAdapter editor = null;
 
 	if (propertyEditor.isWriteable() && propertyEditor.supportsCustomEditor()) {
 	    editor = new CustomEditorAdapter();
 	}
 	else if (propertyEditor.getTags() != null) {
 	    editor = new SComboBoxEditorAdapter();
 	}
 	else if (propertyEditor.getAsText() != null) {
 	    editor = new STextFieldEditorAdapter();
 	}
 	else {
 	    System.err.println("Warning: Property has non-displayabale editor. Using label.");
             editor = new SLabelEditorAdapter();
 	}
 	return editor;
     }
 
     protected SPropertyEditor getPropertyEditor(PropertyDescriptor descriptor) {
 	SPropertyEditor editor = null;
 	Object[] enumerationValues = (Object[])descriptor.getValue("enumerationValues");
 	Class pec = descriptor.getPropertyEditorClass();
 	if (pec == null)
 	    if (enumerationValues != null)
 		pec = TagsEditor.class;
 
 	if (pec != null) {
 	    try {
 		editor = (SPropertyEditor)pec.newInstance();
 	    } catch (Exception ex) {
 		// Drop through.
 	    }
 	}
 
 	if (editor == null)
 	    editor = SPropertyEditorManager.findEditor(descriptor.getPropertyType());
 	
 	if (editor instanceof TagsEditor)
 	    ((TagsEditor)editor).setEnumerationValues(enumerationValues);
 
 	return editor;
     }
 
     public void setBeanClass(Class cls) {
         if (beanClass != cls) {
             beanClass = cls;
             introspect();
         }
     }
     public Class getBeanClass() { return beanClass; }
 
 
     public void setBean(Object bean) {
         this.bean = bean;
         if (!customPropertyDescriptors)
             setBeanClass(bean.getClass());
 
         if (bean != null)
             readBean();
     }
     public Object getBean() { return bean; }
 
 
     protected void initComponents() {
         editorPanel.removeAll();
 
         size = 0;
         PropertyDescriptor[] descriptors = getPropertyDescriptors();
         for (int i = 0; i < descriptors.length; i++) {
             PropertyDescriptor descriptor = descriptors[i];
 	    String name = descriptor.getName();
 	    Class type = descriptor.getPropertyType();
 	    Method getter = descriptor.getReadMethod();
 	    Method setter = descriptor.getWriteMethod();
 
 	    if (getter == null)
 		continue;
 
             SPropertyEditor propertyEditor = getPropertyEditor(descriptor);
             if (propertyEditor == null) {
                 System.err.println("no propertyEditor available for: " + name);
                 continue;
             }
 	    propertyEditor.addPropertyChangeListener(this);
 
 	    EditorAdapter editorAdapter = (EditorAdapter)descriptor.getValue(EDITOR_ADAPTER);
 	    if (editorAdapter == null) {
 		if (setter == null)
 		    editorAdapter = new SLabelEditorAdapter();
 		else
 		    editorAdapter = getEditorAdapter(propertyEditor);
 	    }
 
 	    editorAdapter.setEditor(propertyEditor);
             editorAdapter.init(descriptor);
 
             SComponent nameLabel = new SLabel(descriptor.getDisplayName());
             Boolean mayBeNull = (Boolean)descriptor.getValue(MAYBENULL);
             if (mayBeNull != null && !mayBeNull.booleanValue()) {
                 SPanel inner = new SPanel();
                 SLabel mark = new SLabel(" *");
                 mark.setForeground(Color.red);
                 inner.add(nameLabel);
                 inner.add(mark);
                 editorPanel.add(inner);
             }
             else
                 editorPanel.add(nameLabel);
 
             editorPanel.add(editorAdapter.getComponent());
 
 	    properties.put(name, new Property(descriptor, propertyEditor, editorAdapter));
             
             if (setter != null)
                 size++;
         }
     }
 
     public int size() { return size; }
 
     protected void readBean() {
         PropertyDescriptor[] descriptors = getPropertyDescriptors();
         try {
             for (int i = 0; i < descriptors.length; i++) {
                 PropertyDescriptor descriptor = descriptors[i];
                 Method getter = descriptor.getReadMethod();
                 if (getter != null) {
 		    Property property = (Property)properties.get(descriptor.getName());
 		    if (property == null)
 			continue;
                     EditorAdapter editor = property.editorAdapter;
 
                     Object value = getter.invoke(getBean(), null);
 		    if (value == property.value || (value != null && value.equals(property.value))) {
 			// The property is equal to its old value.
 			continue;
 		    }
 
 		    if (value != null) {
                         // check for equality of primitive type arrays
 			Class comp = value.getClass().getComponentType();
 			if (comp != null && comp.isPrimitive()) {
 			    /*
 			      if (Arrays.equals(o, values[i]))
 			      continue;
 			    */
 			    Object o2 = property.value;
 			    boolean equal = true;
 			    int length = Array.getLength(value);
 			    if (length == Array.getLength(o2)) {
 				for (int a=0; a < length; a++) {
 				    if (!Array.get(value, a).equals(Array.get(o2, a))) {
 					equal = false;
 					break;
 				    }
 				}
 				if (equal)
 				    continue;
 			    }
 			}
 		    }
 
                     if (editor != null) {
 			property.value = value;
                         editor.setValue(value);
                     }
                     else
                       System.out.println("no editor for " + descriptor.getName());
                 }
                 else
                   System.out.println("no getter for " + descriptor.getName());
             }
         }
         catch (InvocationTargetException e) {
             Throwable t = e.getTargetException();
            System.err.println("InvocationTargetException: " + t.getMessage());
             t.printStackTrace(System.err);
         }
         catch (IllegalAccessException e) {
             System.err.println(e.getMessage());
             e.printStackTrace(System.err);
 	}
     }
 
 
     protected void writeBean() {
         try {
             PropertyDescriptor[] descriptors = getPropertyDescriptors();
             for (int i = 0; i < descriptors.length; i++) {
                 PropertyDescriptor descriptor = descriptors[i];
                 Method setter = descriptor.getWriteMethod();
                 if (setter != null) {
 		    Property property = (Property)properties.get(descriptor.getName());
 		    if (property == null)
 			continue;
                     EditorAdapter editor = property.editorAdapter;
 
                     if (editor != null) {
                         Object value = editor.getValue();
                         setter.invoke(getBean(), new Object[] { value });
                     }
                 }
             }
 	}
 	catch (InvocationTargetException e) {
 	    Throwable t = e.getTargetException();
             System.err.println(t.getMessage());
             t.printStackTrace(System.err);
 	}
 	catch (IllegalAccessException e) {
             System.err.println(e.getMessage());
             e.printStackTrace(System.err);
         }
         catch (Exception e) {
             System.err.println(e.getMessage());
             e.printStackTrace(System.err);
         }
     }
 
     public void propertyChange(PropertyChangeEvent evt) {
 	if (evt.getSource() instanceof SPropertyEditor) {
 	    SPropertyEditor editor = (SPropertyEditor)evt.getSource();
 
 	    Iterator iterator = properties.values().iterator();
 	    while (iterator.hasNext()) {
 		Property property = (Property)iterator.next();
 		if (editor == property.propertyEditor) {
 		    Object value = editor.getValue();
 
 		    if (value == property.value || (value != null && value.equals(property.value))) {
 			// The property is equal to its old value.
 			continue;
 		    }
                     property.value = value;
 
 		    Method setter = property.propertyDescriptor.getWriteMethod();
 		    try {
 			if (setter != null)
 			    setter.invoke(bean, new Object[] { value });
 
 		    }
 		    catch (InvocationTargetException ex) {
 			if (ex.getTargetException() instanceof PropertyVetoException) {
 			    System.err.println("WARNING: Vetoed; reason is: " 
 					       + ex.getTargetException().getMessage());
 			}
 			else {
 			    System.err.println("ERROR: " + ex.getTargetException().getMessage());
 			}
 			ex.printStackTrace(System.err);
 		    }
 		    catch (Exception ex) {
 		        System.err.println("Unexpected exception while updating " 
 			      + property.propertyDescriptor.getName() + ": " + ex);
 			ex.printStackTrace(System.err);
 	            }
 		    break;
 		}
 	    }
 	}
 	
 	readBean();
     }
 
     /** note: this comparator uses the position field from the bean info */
     public class PropertyPriorityComparator
         implements Comparator
     {
         public int compare(Object obj1, Object obj2) {
             int prio1 = 0;
             int prio2 = 0;
 
             Integer prioInteger1 = (Integer)((PropertyDescriptor)obj1).
                 getValue(BeanEditorConstants.PRIORITY);
             if (prioInteger1 != null)
                 prio1 = prioInteger1.intValue();
 
             Integer prioInteger2 = (Integer)((PropertyDescriptor)obj2).
                 getValue(BeanEditorConstants.PRIORITY);
             if (prioInteger2 != null)
                 prio2 = prioInteger2.intValue();
 
             if (prio1 < prio2)
                 return -1;
             else if (prio1 == prio2)
                 return 0;
             else
                 return 1;
         }
     }
 
     public class Property
     {
 	public PropertyDescriptor propertyDescriptor;
 	public SPropertyEditor propertyEditor;
 	public EditorAdapter editorAdapter;
 	public Object value;
 
 	public Property(PropertyDescriptor propertyDescriptor,
 			SPropertyEditor propertyEditor,
 			EditorAdapter editorAdapter,
 			Object value) {
 	    this.propertyDescriptor = propertyDescriptor;
 	    this.propertyEditor = propertyEditor;
 	    this.editorAdapter = editorAdapter;
 	    this.value = value;
 	}
 
 	public Property(PropertyDescriptor propertyDescriptor,
 			SPropertyEditor propertyEditor,
 			EditorAdapter editorAdapter) {
 	    this.propertyDescriptor = propertyDescriptor;
 	    this.propertyEditor = propertyEditor;
 	    this.editorAdapter = editorAdapter;
 	    value = null;
 	}
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * End:
  */
