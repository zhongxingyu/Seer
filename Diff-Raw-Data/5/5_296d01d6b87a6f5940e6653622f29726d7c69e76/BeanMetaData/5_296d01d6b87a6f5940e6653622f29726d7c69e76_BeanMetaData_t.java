 /*---
    Copyright 2006-2007 Visual Systems Corporation.
    http://www.vscorp.com
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
         http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 ---*/
 package wicket.contrib.webbeans.model;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyDescriptor;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.apache.commons.beanutils.MethodUtils;
 import org.apache.commons.beanutils.PropertyUtils;
 
 import wicket.Component;
 import wicket.ajax.AjaxRequestTarget;
 import wicket.contrib.webbeans.actions.BeanSubmitButton;
 import wicket.contrib.webbeans.annotations.Action;
 import wicket.contrib.webbeans.annotations.Bean;
 import wicket.contrib.webbeans.annotations.Beans;
 import wicket.contrib.webbeans.annotations.Property;
 import wicket.contrib.webbeans.annotations.Tab;
 import wicket.contrib.webbeans.containers.BeanForm;
 import wicket.contrib.webbeans.containers.BeanGridPanel;
 import wicket.contrib.webbeans.fields.EmptyField;
 import wicket.contrib.webbeans.fields.Field;
 import wicket.contrib.webbeans.model.api.JBeans;
 import wicket.markup.html.form.Form;
 import wicket.markup.html.panel.Panel;
 import wicket.model.IModel;
 
 /**
  * Represents the metadata for a bean properties and actions. Metadata for beans is derived automatically by convention and optionally 
  * a number of different explicit sources. See the documentation for more information.
  * <p/>
  *  
  * @author Dan Syrstad
  */
 public class BeanMetaData extends MetaData implements Serializable
 {
     private static final long serialVersionUID = -4705317346444856939L;
 
     private static Logger logger = Logger.getLogger(BeanMetaData.class.getName());
 
     private static final Class<?>[] PROP_CHANGE_LISTENER_ARG = new Class<?>[] { PropertyChangeListener.class };
     /** Cache of beanprops files, already parsed. Key is the beanprops name, value is a List of Beans. */
     private static final Map<String, CachedBeanProps> cachedBeanProps = new HashMap<String, CachedBeanProps>();
     private static final String DEFAULT_RESOURCE_KEY = "STUB"; 
 
     public static final String PARAM_VIEW_ONLY = "viewOnly";
     public static final String PARAM_DISPLAYED = "displayed";
     public static final String PARAM_TABS = "tabs";
     public static final String PARAM_PROPS = "props";
     public static final String PARAM_ACTIONS = "actions";
     public static final String PARAM_LABEL = "label";
     public static final String PARAM_CONTAINER = "container";
 
     public static final String ACTION_PROPERTY_PREFIX = "action.";
     public static final String DEFAULT_TAB_ID = "DEFAULT_TAB";
     
     private Class<?> beanClass;
     private Class<?> metaDataClass;
     private Beans beansMetaData;
     private List<Bean> collectedBeans = new ArrayList<Bean>();
     private String context;
     private Component component;
     private ComponentRegistry componentRegistry;
     private boolean isChildBean;
 
     // List of all properties.
     private List<ElementMetaData> elements = new ArrayList<ElementMetaData>();
     private List<TabMetaData> tabs = new ArrayList<TabMetaData>();
 
     private boolean hasAddPropertyChangeListenerMethod;
     private boolean hasRemovePropertyChangeListenerMethod;
 
     /**
      * Construct a BeanMetaData. 
      *
      * @param beanClass the bean's class.
      * @param context specifies a context to use when looking up beans in beanprops. May be null to not
      *  use a context.
      * @param component the component used to get the Localizer.
      * @param componentRegistry the ComponentRegistry used to determine visual components. May be null.
      * @param viewOnly if true, specifies that the entire bean is view-only. This can be overridden by the
      *  Localizer configuration.
      */
     public BeanMetaData(Class<?> beanClass, String context, Component component, ComponentRegistry componentRegistry,
                     boolean viewOnly)
     {
         this(beanClass, context, null, null, component, componentRegistry, viewOnly, false);
     }
 
     /**
      * Construct a BeanMetaData. 
      *
      * @param beanClass the bean's class.
      * @param context specifies a context to use when looking up beans in beanprops. May be null to not
      *  use a context.
      * @param metaDataClass an optional arbitrary class that has WWB {@link Beans} and/or {@link Bean} annotations.
      *  May be null. This allows bean metadata to be separate from the component and the bean, hence reusable.
      * @param component the component used to get the Localizer.
      * @param componentRegistry the ComponentRegistry used to determine visual components. May be null.
      * @param viewOnly if true, specifies that the entire bean is view-only. This can be overridden by the
      *  Localizer configuration.
      */
     public BeanMetaData(Class<?> beanClass, String context, Class<?> metaDataClass, Component component, ComponentRegistry componentRegistry,
                     boolean viewOnly)
     {
         this(beanClass, context, null, metaDataClass, component, componentRegistry, viewOnly, false);
     }
 
     /**
      * Construct a BeanMetaData. 
      *
      * @param beanClass the bean's class.
      * @param context specifies a context to use when looking up beans in beanprops. May be null to not
      *  use a context.
      * @param beans an implementation using the Beans annotation to provide meta data. May be null. 
      * @param component the component used to get the Localizer.
      * @param componentRegistry the ComponentRegistry used to determine visual components. May be null.
      * @param viewOnly if true, specifies that the entire bean is view-only. This can be overridden by the
      *  Localizer configuration.
      */
     public BeanMetaData(Class<?> beanClass, String context, Beans beans, Component component, ComponentRegistry componentRegistry,
                     boolean viewOnly)
     {
         this(beanClass, context, beans, null, component, componentRegistry, viewOnly, false);
     }
 
     /**
      * Construct a BeanMetaData. 
      *
      * @param beanClass the bean's class.
      * @param context specifies a context to use when looking up beans in beanprops. May be null to not
      *  use a context.
      * @param bean an implementation using the Bean annotation to provide meta data. May be null. 
      * @param component the component used to get the Localizer.
      * @param componentRegistry the ComponentRegistry used to determine visual components. May be null.
      * @param viewOnly if true, specifies that the entire bean is view-only. This can be overridden by the
      *  Localizer configuration.
      */
     public BeanMetaData(Class<?> beanClass, String context, Bean bean, Component component, ComponentRegistry componentRegistry,
                     boolean viewOnly)
     {
         this(beanClass, context, bean == null ? null : new JBeans(bean), null, component, componentRegistry, viewOnly, false);
     }
 
     /**
      * Construct a BeanMetaData. 
      *
      * @param beanClass the bean's class.
      * @param context specifies a context to use when looking up beans in beanprops. May be null to not
      *  use a context.
      * @param beans an implementation using the Beans annotation to provide meta data.  May be null. 
      * @param metaDataClass an optional arbitrary class that has WWB {@link Beans} and/or {@link Bean} annotations.
      *  May be null. This allows bean metadata to be separate from the component and the bean, hence reusable.
      * @param component the component used to get the Localizer.
      * @param componentRegistry the ComponentRegistry used to determine visual components. May be null.
      * @param viewOnly if true, specifies that the entire bean is view-only. This can be overridden by the
      *  Localizer configuration.
      * @param isChildBean true if this bean is a child of another bean.
      */
     public BeanMetaData(Class<?> beanClass, String context, Beans beans, Class<?> metaDataClass, Component component, ComponentRegistry componentRegistry,
                     boolean viewOnly, boolean isChildBean)
     {
         super(component);
         
         this.beanClass = beanClass;
         this.context = context;
         this.beansMetaData = beans;
         this.metaDataClass = metaDataClass;
         this.component = component;
         if (componentRegistry == null) {
             this.componentRegistry = new ComponentRegistry();
         }
         else {
             this.componentRegistry = componentRegistry;
         }
 
         this.isChildBean = isChildBean;
 
         setParameter(PARAM_VIEW_ONLY, String.valueOf(viewOnly));
         setParameter(PARAM_DISPLAYED, "true");
         setParameter(PARAM_LABEL, createLabel(beanClass.getSimpleName()));
 
         init();
 
         consumeParameter(PARAM_LABEL);
         consumeParameter(PARAM_ACTIONS);
         consumeParameter(PARAM_PROPS);
         consumeParameter(PARAM_TABS);
         consumeParameter(PARAM_DISPLAYED);
         consumeParameter(PARAM_VIEW_ONLY);
     }
 
     /**
      * Determines if all parameters specified have been consumed for a specific tab, or all tabs.
      * 
      * @param unconsumedMsgs messages that report the parameter keys that were specified but not consumed.
      * @param tabMetaData the tab to be checked. If null, all elements and tabs are checked.
      * 
      * @return true if all parameters specified have been consumed.
      */
     public boolean areAllParametersConsumed(Set<String> unconsumedMsgs, TabMetaData tabMetaData)
     {
         if (!super.areAllParametersConsumed("Bean " + beanClass.getName(), unconsumedMsgs)) {
             return false;
         }
 
         // Make sure all elements and tabs have their parameters consumed.
         for (ElementMetaData element : tabMetaData == null ? getDisplayedElements() : getTabElements(tabMetaData)) {
             if (!element.areAllParametersConsumed("Property " + element.getPropertyName(), unconsumedMsgs)) {
                 return false;
             }
         }
 
         for (TabMetaData tab : tabMetaData == null ? tabs : Collections.singletonList(tabMetaData)) {
             if (!tab.areAllParametersConsumed("Tab " + tab.getId(), unconsumedMsgs)) {
                 return false;
             }
         }
 
         return true;
     }
 
     /**
      * Logs a warning if any parameter specified have not been consumed for a specific tab, or all tabs.
      * 
      * @param tabMetaData the tab to be checked. If null, all elements and tabs are checked.
      */
     public void warnIfAnyParameterNotConsumed(TabMetaData tabMetaData)
     {
         Set<String> msgs = new HashSet<String>();
         if (!areAllParametersConsumed(msgs, tabMetaData)) {
             for (String msg : msgs) {
                 logger.warning(msg);
             }
         }
     }
 
     private Method getAddPropertyChangeListenerMethod()
     {
         try {
             return beanClass.getMethod("addPropertyChangeListener", PROP_CHANGE_LISTENER_ARG);
         }
         catch (Exception e) {
             // Assume we don't have it.
             return null;
         }
     }
 
     private Method getRemovePropertyChangeListenerMethod()
     {
         try {
             return beanClass.getMethod("removePropertyChangeListener", PROP_CHANGE_LISTENER_ARG);
         }
         catch (Exception e) {
             // Assume we don't have it.
             return null;
         }
     }
 
     private void init()
     {
         // Check if bean supports PropertyChangeListeners.
         hasAddPropertyChangeListenerMethod = getAddPropertyChangeListenerMethod() != null;
         hasRemovePropertyChangeListenerMethod = getRemovePropertyChangeListenerMethod() != null;
         
         String baseBeanClassName = getBaseClassName(beanClass);
 
         // Deduce actions from the component.
         List<Method> actionMethods = getActionMethods(component.getClass());
         for (Method method : actionMethods) {
             String name = method.getName();
             String prefixedName = ACTION_PROPERTY_PREFIX + name;
             String label = getLabelFromLocalizer(baseBeanClassName, prefixedName);
             if (label == null) {
                 label = createLabel(name);
             }
             
             ElementMetaData actionMeta = new ElementMetaData(this, prefixedName, label, null);
             actionMeta.setAction(true);
             elements.add(actionMeta);
         }
         
         // Create defaults based on the bean itself.
         PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(beanClass);
         for (PropertyDescriptor descriptor : descriptors) {
             String name = descriptor.getName();
             
             // Skip getClass() and methods that are not readable or hidden.
             if (name.equals("class") || descriptor.getReadMethod() == null || descriptor.isHidden()) {
                 continue;
             }
             
             String label = getLabelFromLocalizer(baseBeanClassName, name);
             if (label == null) {
                 label = descriptor.getDisplayName();
             }
             
             if (label.equals(name)) {
                 label = createLabel(name);
             }
 
             ElementMetaData propertyMeta = new ElementMetaData(this, name, label, descriptor.getPropertyType());
             propertyMeta.setViewOnly( isViewOnly() );
             elements.add(propertyMeta);
 
             if (descriptor.getWriteMethod() == null) {
                 propertyMeta.setViewOnly(true);
             }
             
             deriveElementFromAnnotations(descriptor, propertyMeta);
         }
 
         // Collect various sources of metadata for the bean we're interested in. 
         collectAnnotations();
         collectFromBeanProps();
         collectBeansAnnotation(beansMetaData, false);
         
         // Process action annotations on component.
         for (Method method : getActionMethods(component.getClass())) {
             Action action = method.getAnnotation(Action.class);
             processActionAnnotation(action, method.getName());
         }
 
         // Determine the hierarchy of Bean contexts. I.e., the default Bean is always processed first, followed by those that
         // extend it, etc. This acts as a stack.
         List<Bean> beansHier = buildContextStack();
         
         // Apply beans in order from highest to lowest. The default context will always be first.
         boolean foundSpecifiedContext = false;
         for (Bean bean : beansHier) {
             if (context != null && context.equals(bean.context())) {
                 foundSpecifiedContext = true;
             }
             
             processBeanAnnotation(bean);
         }
 
         // Ensure that if a context was specified, that we found one in the metadata. Otherwise it might have been a typo.
         if (context != null && !foundSpecifiedContext) {
             throw new RuntimeException("Could not find specified context '" + context + "' in metadata.");
         }
         
         // Post-process Bean-level parameters
         if (!getBooleanParameter(PARAM_DISPLAYED)) {
             elements.clear();
             tabs.clear();
         }
         
         // Configure tabs
         if (tabs.isEmpty()) {
             // Create single default tab.
             tabs.add( new TabMetaData(this, DEFAULT_TAB_ID, getParameter(PARAM_LABEL) ) );
         }
         
         String defaultTabId = tabs.get(0).getId();
         
         // Post-process each property based on bean parameters
         for (ElementMetaData elementMeta : elements) {
             // If element is not on a tab, add it to the first. If it's an action, it must have been assigned an order to
             // appear on a tab. Otherwise it is a global action.
             if (elementMeta.getTabId() ==  null &&
                 (!elementMeta.isAction() || 
                  (elementMeta.isAction() && elementMeta.isActionSpecifiedInProps()))) {
                 elementMeta.setTabId(defaultTabId);
             }
         }
 
         Collections.sort(elements, new Comparator<ElementMetaData>() {
             public int compare(ElementMetaData o1, ElementMetaData o2)
             {
                 return (o1.getOrder() > o2.getOrder() ? 1 : (o1.getOrder() < o2.getOrder() ? -1 : 0));
             }
         });
     }
 
     /**
      * Determine the hierarchy of Bean contexts. I.e., the default Bean is always processed first, followed by those that
      * extend it, etc. This acts as a stack.
      *
      * @return see above.
      */
     private List<Bean> buildContextStack()
     {
         List<Bean> beansHier = new ArrayList<Bean>();
         String currContext = context;
         
         // Note: Limit accidental cyclical specs (e.g., A extends B, B extends A). This also limits the maximum hierarchy depth to the same 
         // amount, which should be plenty.
         for (int limit = 0; limit < 20; ++limit) {
             String extendsContext = null;
             for (Bean collectedBean : collectedBeans) {
                 String beanContext = collectedBean.context();
                if (beanContext != null && beanContext.length() == 0) {
                     beanContext = null;
                 }
                 
                 if (beanContext == currContext || (beanContext != null && beanContext.equals(currContext))) {
                     // Push it on stack
                     beansHier.add(0, collectedBean);
                     
                     if (extendsContext == null) {
                         extendsContext = collectedBean.extendsContext();
                        if (extendsContext != null && extendsContext.length() == 0) {
                             extendsContext = null;
                         }
                     }
                     else if (!extendsContext.equals(collectedBean.extendsContext())) {
                         throw new RuntimeException("Inconsistent extends context " + collectedBean.extendsContext() + 
                                         " is not consistent with first one encountered " + extendsContext);
                     }
                 }
             }
 
             if (currContext == null || currContext.length() == 0) {
                 // Just processed the default context, so stop.
                 break;
             }
 
             currContext = extendsContext;
         }
         return beansHier;
     }
 
     /**
      * Attempts to get the label for the given action or property name from the Localizer.
      *
      * @param baseBeanClassName
      * @param name
      * 
      * @return the label, or null if not defined.
      */
     private String getLabelFromLocalizer(String baseBeanClassName, String name)
     {
         // Try to retrieve label from properties file in the form of "Bean.{name}.label" or
         // simply {name}.label.
         String propLabelKey = name + ".label";
         String label = component.getLocalizer().getString(baseBeanClassName + '.' + propLabelKey, component, DEFAULT_RESOURCE_KEY);
         if (label == DEFAULT_RESOURCE_KEY) {
             label = component.getLocalizer().getString(propLabelKey, component, DEFAULT_RESOURCE_KEY);
         }
         
         if (label == DEFAULT_RESOURCE_KEY) {
             label = null;
         }
         
         return label;
     }
     
     /**
      * Collect any WWB Beans or Bean annotations that may exist on the component, bean, or meta-data class that
      * apply to the specified bean.
      * Order of processing is: Bean, Metadata class, then Component. Hence, Component annotations
      * augment or override those of the Metadata class and the Bean.  
      */
     private void collectAnnotations()
     {
         // Bean itself
         collectBeansAnnotation( beanClass.getAnnotation(Beans.class), true);
         collectBeanAnnotation( beanClass.getAnnotation(Bean.class), true);
 
         // Metadata class
         if (metaDataClass != null) {
             collectBeansAnnotation( metaDataClass.getAnnotation(Beans.class), false);
             collectBeanAnnotation( metaDataClass.getAnnotation(Bean.class), false);
         }
         
         // Component
         Class<? extends Component> componentClass = component.getClass(); 
         collectBeansAnnotation( componentClass.getAnnotation(Beans.class), false);
         collectBeanAnnotation( componentClass.getAnnotation(Bean.class), false);
     }
     
     private void collectBeansAnnotation(Beans beans, boolean isBeanAnnotation)
     {
         if (beans != null) {
             for (Bean bean : beans.value()) {
                 collectBeanAnnotation(bean, isBeanAnnotation);
             }
         }
     }
     
     private void collectBeanAnnotation(Bean bean, boolean isBeanAnnotation)
     {
         if (bean == null) {
             return;
         }
         
         Class<?> beanType = bean.type();
         if (beanType == Object.class) {
             if (!isBeanAnnotation) {
                 throw new RuntimeException("@Bean must include the type attribute when used on non-bean components. Occurred while processing annotations for bean " 
                                 + beanClass.getName());
             }
             
             beanType = beanClass;
         }
         
         if (beanType != beanClass) {
             return; // Doesn't match what we're interested in.
         }
         
         collectedBeans.add(bean);
     }
 
     private void processBeanAnnotation(Bean bean)
     {
         setParameter(BeanGridPanel.PARAM_COLS, String.valueOf(bean.columns()));
         setParameter(PARAM_DISPLAYED, String.valueOf(bean.displayed()));
         setParameterIfNotEmpty(PARAM_LABEL, bean.label());
         if (bean.container() != Panel.class) {
             setParameter(PARAM_CONTAINER, bean.container().getName());
         }
         
         setParameter(BeanForm.PARAM_ROWS, String.valueOf(bean.rows()));
         
         if (bean.viewOnly().length > 0) {
             // Only set if explicitly set.
             boolean viewOnly = bean.viewOnly()[0];
             setParameter(PARAM_VIEW_ONLY, String.valueOf(viewOnly));
             updateElementsViewOnlyState(viewOnly);
         }
 
         setParameterIfNotEmpty(bean.paramName(), bean.paramValue());
         for (wicket.contrib.webbeans.annotations.Parameter param : bean.params()) {
             setParameterIfNotEmpty(param.name(), param.value());
             if (param.name().equals(PARAM_VIEW_ONLY)) {
                 updateElementsViewOnlyState( getBooleanParameter(PARAM_VIEW_ONLY) );
             }
         }
         
         // Process actionNames after actions because actionNames is typically used to define order.
         int order = 1;
         for (String actionName : bean.actionNames()) {
             if (!handleElementRemove(actionName, true)) {
                 ElementMetaData element = findElementAddPseudos(ACTION_PROPERTY_PREFIX + actionName);
                 if (!element.isActionSpecifiedInProps() && element.getOrder() == ElementMetaData.DEFAULT_ORDER) {
                     element.setOrder(order++);
                 }
             }
         }
         
         order = 1;
         for (Action action : bean.actions()) {
             if (!handleElementRemove(action.name(), false)) {
                 ElementMetaData element = processActionAnnotation(action, null);
                 if (!element.isActionSpecifiedInProps() && element.getOrder() == ElementMetaData.DEFAULT_ORDER) {
                     element.setOrder(order++);
                 }
             }
         }
 
         // Process propertyNames before properties because propertyNames is typically used to define order.
         order = 1;
         for (String propName : bean.propertyNames()) {
             if (!handleElementRemove(propName, false)) {
                 ElementMetaData element = findElementAddPseudos(propName);
                 if (element.isAction()) {
                     element.setActionSpecifiedInProps(true);
                 }
                 
                 if (element.getOrder() == ElementMetaData.DEFAULT_ORDER) {
                     element.setOrder(order++);
                 }
             }
         }
         
         order = 1;
         for (Property property : bean.properties()) {
             if (!handleElementRemove(property.name(), false)) {
                 ElementMetaData element = processPropertyAnnotation(property, null);
                 if (element.isAction()) {
                     element.setActionSpecifiedInProps(true);
                 }
                 
                 if (element.getOrder() == ElementMetaData.DEFAULT_ORDER) {
                     element.setOrder(order++);
                 }
             }
         }
 
         for (Tab tab : bean.tabs()) {
             String tabName = tab.name();
             boolean removeTab = false;
             if (tabName.startsWith("-") && tabName.length() > 1) {
                 tabName = tabName.substring(1);
                 removeTab = true;
             }
 
             TabMetaData foundTab = findTab(tabName);
 
             if (removeTab) {
                 if (foundTab == null) {
                     throw new RuntimeException("Tab " + tabName + " does not exist in exposed list of tabs.");
                 }
 
                 tabs.remove(foundTab);
             }
             else {
                 processTabAnnotation(tab, foundTab);
             }
         }
     }
 
     /**
      * Set all elements to same viewOnly state. Note that this happens before individual elements are processed so 
      * that they can override the bean setting if necessary.
      *
      * @param viewOnly
      */
     private void updateElementsViewOnlyState(boolean viewOnly)
     {
         for (ElementMetaData element : elements) {
             element.setViewOnly(viewOnly);
         }
     }
     
     /**
      * Handle element removal if element name starts with a '-'. 
      *
      * @param elementName the element name, possibly starting with '-'.
      * 
      * @return true if element was removed, else false.
      */
     private boolean handleElementRemove(String elementName, boolean prependActionPrefix)
     {
         if (elementName.startsWith("-") && elementName.length() > 1) {
             elementName = elementName.substring(1);
             if (prependActionPrefix) {
                 elementName = ACTION_PROPERTY_PREFIX + elementName;
             }
             
             elements.remove( findElementAddPseudos(elementName) );
             return true;
         }
         
         return false;
     }
     
     private ElementMetaData processPropertyAnnotation(Property property, ElementMetaData element)
     {
         if (property == null) {
             return null;
         }
         
         if (element == null && property.name().length() == 0) {
             throw new RuntimeException("@Property annotation of @Bean " + beanClass.getName() + " did not set the name attribute.");
         }
         
         if (element == null || property.name().length() > 0) {
             element = findElementAddPseudos(property.name());
         }
         
         if (property.colspan() > 1) {
             element.setParameter(BeanGridPanel.PARAM_COLSPAN, String.valueOf(property.colspan()));
         }
         
         if (property.rows() > 0) {
             element.setParameter(ElementMetaData.PARAM_ROWS, String.valueOf(property.rows()));
         }
         
         if (property.columns() > 0) {
             element.setParameter(ElementMetaData.PARAM_COLUMNS, String.valueOf(property.columns()));
         }
         
         element.setParameterIfNotEmpty(ElementMetaData.PARAM_DEFAULT_VALUE, property.defaultValue());
         if (property.elementType() != Object.class) {
             element.setParameter(ElementMetaData.PARAM_ELEMENT_TYPE, property.elementType().getName());
         }
         
         if (property.fieldType() != Field.class) {
             element.setParameter(ElementMetaData.PARAM_FIELD_TYPE, property.fieldType().getName());
         }
         
         element.setParameterIfNotEmpty(ElementMetaData.PARAM_LABEL, property.label());
         element.setParameterIfNotEmpty(ElementMetaData.PARAM_LABEL_IMAGE, property.labelImage());
         if (property.maxLength() > 0) {
             element.setMaxLength(property.maxLength());
         }
         
         element.setRequired(property.required());
         if (!element.isAction()) {
             // Only set viewOnly if explicitly set.
             if (property.viewOnly().length > 0) {
                 element.setViewOnly(property.viewOnly()[0]);
             }
         }
         
         element.setParameterIfNotEmpty(property.paramName(), property.paramValue());
         for (wicket.contrib.webbeans.annotations.Parameter param : property.params()) {
             element.setParameterIfNotEmpty(param.name(), param.value());
         }
         
         return element;
     }
     
     private ElementMetaData processActionAnnotation(Action action, String methodName)
     {
         if (action == null) {
             return null;
         }
         
         if (methodName == null && action.name().length() == 0) {
             throw new RuntimeException("@Action annotation of @Bean " + beanClass.getName() + " did not set the name attribute.");
         }
         
         if (action.name().length() > 0) {
             methodName = action.name();
         }
         
         ElementMetaData element = findElementAddPseudos(ACTION_PROPERTY_PREFIX + methodName);
 
         if (action.colspan() > 1) {
             element.setParameter(BeanGridPanel.PARAM_COLSPAN, String.valueOf(action.colspan()));
         }
         
         element.setParameterIfNotEmpty(ElementMetaData.PARAM_LABEL, action.label());
         element.setParameterIfNotEmpty(ElementMetaData.PARAM_LABEL_IMAGE, action.labelImage());
         
         element.setParameterIfNotEmpty(BeanSubmitButton.PARAM_CONFIRM, action.confirm());
         element.setParameter(BeanSubmitButton.PARAM_AJAX, String.valueOf(action.ajax()));
         element.setParameterIfNotEmpty(BeanSubmitButton.PARAM_DEFAULT, String.valueOf(action.isDefault()));
         
         element.setParameterIfNotEmpty(action.paramName(), action.paramValue());
         for (wicket.contrib.webbeans.annotations.Parameter param : action.params()) {
             element.setParameterIfNotEmpty(param.name(), param.value());
         }
         
         return element;
     }
     
     /**
      * Process a Tab annotation.
      *
      * @param tab the annotation.
      * @param tabMetaData the tab metadata, if it already exists.
      */
     private void processTabAnnotation(Tab tab, TabMetaData tabMetaData)
     {
         if (tab == null) {
             return;
         }
         
         String tabName = tab.name();
         if (tabMetaData == null) {
             tabMetaData = new TabMetaData(this, tabName, createLabel(tabName));
             tabs.add(tabMetaData);
         }
         
         tabMetaData.setParameterIfNotEmpty(PARAM_LABEL, tab.label());
         
         int order = 1;
         for (Property property : tab.properties()) {
             if (!handleElementRemove(property.name(), false)) {
                 ElementMetaData element = processPropertyAnnotation(property, null);
                 element.setTabId( tabMetaData.getId() );
                 element.setOrder(order++);
                 if (element.isAction()) {
                     element.setActionSpecifiedInProps(true);
                 }
             }
         }
 
         // Process propertyNames after properties because propertyNames is typically used to define order.
         order = 1;
         for (String propName : tab.propertyNames()) {
             if (!handleElementRemove(propName, false)) {
                 ElementMetaData element = findElementAddPseudos(propName);
                 element.setTabId( tabMetaData.getId() );
                 element.setOrder(order++);
                 if (element.isAction()) {
                     element.setActionSpecifiedInProps(true);
                 }
             }
         }
         
         tabMetaData.setParameterIfNotEmpty(tab.paramName(), tab.paramValue());
         for (wicket.contrib.webbeans.annotations.Parameter param : tab.params()) {
             tabMetaData.setParameterIfNotEmpty(param.name(), param.value());
         }
     }
     
     /**
      * Collection Beans from the beanprops file, if any.
      */
     private void collectFromBeanProps()
     {
         String propFileName = getBaseClassName(component.getClass()) + ".beanprops";
         URL propFileURL = component.getClass().getResource(propFileName);
         long timestamp = 0;
         if (propFileURL != null && propFileURL.getProtocol().equals("file")) {
             try {
                 timestamp = new File(propFileURL.toURI()).lastModified();
             }
             catch (URISyntaxException e) { /* Ignore - treat as zero */ }
         }
         
         String cacheKey = beanClass.getName() + ':' + propFileName;
         CachedBeanProps beanprops = cachedBeanProps.get(cacheKey);
         if (beanprops == null || beanprops.getModTimestamp() != timestamp) {
             if (beanprops != null) {
                 logger.info("File changed: " + propFileName + " re-reading.");
             }
             
             // It's OK not to have a beanprops file. We can deduce the parameters by convention. 
             InputStream propsStream = component.getClass().getResourceAsStream(propFileName);
             if (propsStream != null) {
                 try {
                     JBeans beans = new BeanPropsParser(propFileName, propsStream).parseToJBeans(this);
                     beanprops = new CachedBeanProps(beans, timestamp);
                     cachedBeanProps.put(cacheKey, beanprops);
                 }
                 finally {
                     try { propsStream.close(); } catch (IOException e) { /* Ignore */ }
                 }
             }
         }
         
         if (beanprops != null) {
             collectBeansAnnotation(beanprops.getBeans(), false);
         }
     }
     
     /**
      * Derive metadata from standard annotations such as JPA and FindBugs.
      *
      * @param descriptor
      * @param elementMetaData
      */
     private void deriveElementFromAnnotations(PropertyDescriptor descriptor, ElementMetaData elementMetaData)
     {
         // NOTE: !!! The annotation classes must be present at runtime, otherwise getAnnotations() doesn't 
         // return the annotation.
         Method readMethod = descriptor.getReadMethod();
         if (readMethod != null) {
             processElementAnnotations(elementMetaData, readMethod.getAnnotations());
         }
 
         Method writeMethod = descriptor.getWriteMethod();
         if (writeMethod != null) {
             processElementAnnotations(elementMetaData, writeMethod.getAnnotations());
         }
     }
     
     /**
      * Process annotations for {@link #deriveElementFromAnnotations(PropertyDescriptor, ElementMetaData)}.
      *
      * @param elementMetaData
      * @param annotations
      */
     private void processElementAnnotations(ElementMetaData elementMetaData, Annotation[] annotations)
     {
         if (annotations == null) {
             return;
         }
         
         // Note: We only reference the annotations using their string name, not the class.
         // If we referenced the class, we'd have a dependency on those classes.
         // We also have to access the values by reflection so we don't depend on the class.
         for (Annotation annotation : annotations) {
             Class<?> annotationType = annotation.annotationType();
             String name = annotationType.getName();
             
             if (name.equals("javax.persistence.Column")) {
                 elementMetaData.setMaxLength( (Integer)invokeAnnotationMethod(annotation, "length") );
                 elementMetaData.setRequired( !(Boolean)invokeAnnotationMethod(annotation, "nullable") );
             }
             else if (name.equals("javax.jdo.annotations.Column")) {
                 elementMetaData.setMaxLength( (Integer)invokeAnnotationMethod(annotation, "length") );
                 elementMetaData.setRequired( "false".equals((String)invokeAnnotationMethod(annotation, "allowsNull")) );
                 elementMetaData.setDefaultValue( (String)invokeAnnotationMethod(annotation, "defaultValue") );
             }
             else if (annotationType == Property.class) {
                 processPropertyAnnotation((Property)annotation, elementMetaData);
             }
         }
     }
     
     /**
      * Invokes an annotation method to get a value, possibly returning null if no value or if the method doesn't exist.
      */
     private Object invokeAnnotationMethod(Annotation annotation, String methodName)
     {
         try {
             return MethodUtils.invokeExactMethod(annotation, methodName, null);
         }
         catch (Exception e) {
             // Ignore.
             return null;
         }
     }
 
     /**
      * Find action methods for a class. 
      *
      * @param aClass the class.
      * 
      * @return an List of sorted action methods, possibly empty.
      */
     private List<Method> getActionMethods(Class<? extends Component> aClass)
     {
         List<Method> result = new ArrayList<Method>();
         for (Method method : aClass.getMethods()) {
             Class<?>[] params = method.getParameterTypes();
             Class<?> returnType = method.getReturnType();
             if (returnType.equals(Void.TYPE) && params.length == 3 &&
                 params[0] == AjaxRequestTarget.class &&
                 params[1] == Form.class &&
                 (params[2] == beanClass || params[2] == Object.class)) {
                 result.add(method);
             }
         }
         
         Collections.sort(result, new Comparator<Method>() {
             public int compare(Method o1, Method o2)
             {
                 return o1.getName().compareTo(o2.getName());
             }
             
         });
         return result;
     }
 
     /**
      * Gets the base class name of a Class.
      * 
      * @param aClass the class.
      *
      * @return the base class name (the name without the package name).
      */
     static String getBaseClassName(Class<?> aClass)
     {
         String baseClassName = aClass.getName();
         int idx = baseClassName.lastIndexOf('.');
         if (idx >= 0) {
             baseClassName = baseClassName.substring(idx + 1);
         }
 
         return baseClassName;
     }
 
     /**
      * Finds a tab.
      *
      * @param tabName the tab name
      * @return the TabMetaData, or null if not found.
      */
     private TabMetaData findTab(String tabName)
     {
         TabMetaData foundTab = null;
         for (TabMetaData tab : tabs) {
             if (tab.getId().equals(tabName)) {
                 foundTab = tab;
                 break;
             }
         }
         return foundTab;
     }
 
     /**
      * Finds the specified element in the list of all elements. Handles special
      * Pseudo property names (e.g., "EMPTY") by adding a new one to the list.
      * 
      * @param propertyName
      * 
      * @return the ElementMetaData.
      * 
      * @throws RuntimeException if property is not found.
      */
     private ElementMetaData findElementAddPseudos(String propertyName)
     {
         ElementMetaData prop;
         if (propertyName.equals("EMPTY")) {
             prop = new ElementMetaData(this, "EMPTY:" + elements.size(), "", Object.class);
             prop.setFieldType(EmptyField.class.getName());
             prop.setViewOnly(true);
             elements.add(prop);
         }
         else {
             prop = findElement(propertyName);
             if (prop == null) {
                 throw new RuntimeException("Property: " + propertyName
                                 + " does not exist in exposed list of properties.");
             }
         }
 
         return prop;
     }
 
     /**
      * Finds the specified element in the list of all elements.
      * 
      * @param propertyName
      * 
      * @return the ElementMetaData or null if not found.
      */
     public ElementMetaData findElement(String propertyName)
     {
         for (ElementMetaData prop : elements) {
             if (prop.getPropertyName().equals(propertyName)) {
                 return prop;
             }
         }
 
         return null;
     }
 
     /**
      * Creates a human readable label from a Java identifier.
      * 
      * @param identifier the Java identifier.
      * 
      * @return the label.
      */
     private static String createLabel(String identifier)
     {
         // Check for a complex property.
         int idx = identifier.lastIndexOf('.');
         if (idx < 0) {
             idx = identifier.lastIndexOf('$'); // Java nested classes.
         }
 
         if (idx >= 0 && identifier.length() > 1) {
             identifier = identifier.substring(idx + 1);
         }
 
         if (identifier.length() == 0) {
             return "";
         }
 
         char[] chars = identifier.toCharArray();
         StringBuffer buf = new StringBuffer(chars.length + 10);
 
         // Capitalize the first letter.
         buf.append(Character.toUpperCase(chars[0]));
         boolean lastLower = false;
         for (int i = 1; i < chars.length; ++i) {
             if (!Character.isLowerCase(chars[i])) {
                 // Lower to upper case transition -- add space before it
                 if (lastLower) {
                     buf.append(' ');
                 }
             }
 
             buf.append(chars[i]);
             lastLower = Character.isLowerCase(chars[i]) || Character.isDigit(chars[i]);
         }
 
         return buf.toString();
     }
 
     public String getLabel()
     {
         return getParameter(PARAM_LABEL);
     }
     
     public Class<? extends Panel> getContainerClass()
     {
         String container = getParameter(PARAM_CONTAINER);
         if (container == null) {
             return null;
         }
 
         try {
             return (Class<? extends Panel>)Class.forName(container);
         }
         catch (Exception e) {
             throw new RuntimeException("Cannot load container class " + container);
         }
     }
 
     /**
      * @return the tabs defined for this bean. There will always be at least one tab.
      */
     public List<TabMetaData> getTabs()
     {
         return tabs;
     }
 
     /**
      * @return a list of all displayed elements for a tab.
      */
     public List<ElementMetaData> getTabElements(TabMetaData tab)
     {
         List<ElementMetaData> elems = new ArrayList<ElementMetaData>();
         for (ElementMetaData elem : elements) {
             if (elem.getTabId() != null && elem.getTabId().equals(tab.getId())) {
                 elems.add(elem);
             }
         }
 
         return elems;
     }
 
     /**
      * @return a list of all displayed elements for a bean.
      */
     public List<ElementMetaData> getDisplayedElements()
     {
         return elements;
     }
 
     /**
      * Gets a list of actions that are not assigned to any particular placement within the bean.
      *
      * @return the list of global actions.
      */
     public List<ElementMetaData> getGlobalActions()
     {
         List<ElementMetaData> elems = new ArrayList<ElementMetaData>();
         for (ElementMetaData elem : elements) {
             if (elem.isAction() && !elem.isActionSpecifiedInProps()) {
                 elems.add(elem);
             }
         }
 
         return elems;
     }
 
     /**
      * @return the bean class.
      */
     public Class<?> getBeanClass()
     {
         return beanClass;
     }
 
     /**
      * Gets the external metadata Class supplied to the constructor.
      *
      * @return a Class<?>, or null if not defined.
      */
     public Class<?> getMetaDataClass()
     {
         return metaDataClass;
     }
 
     /**
      * Gets the beansMetaData.
      *
      * @return a Beans.
      */
     public Beans getBeansMetaData()
     {
         return beansMetaData;
     }
 
     /**
      * @return the component.
      */
     public Component getComponent()
     {
         return component;
     }
 
     /**
      * @return the componentRegistry.
      */
     public ComponentRegistry getComponentRegistry()
     {
         return componentRegistry;
     }
 
     /**
      * @return the context.
      */
     public String getContext()
     {
         return context;
     }
 
     /**
      * @return the viewOnly flag.
      */
     public boolean isViewOnly()
     {
         return getBooleanParameter(PARAM_VIEW_ONLY);
     }
 
     /**
      * @return the displayed flag.
      */
     public boolean isDisplayed()
     {
         return getBooleanParameter(PARAM_DISPLAYED);
     }
 
     /**
      * Adds a property change listener to the bean if it supports it. If it doesn't support
      * addition property change listeners, nothing happens.
      *
      * @param beanModel the bean's IModel.
      * @param listener the {@link PropertyChangeListener}.
      */
     public void addPropertyChangeListener(BeanPropertyModel beanModel, PropertyChangeListener listener)
     {
         if (!hasAddPropertyChangeListenerMethod) {
             return;
         }
 
         Object bean = beanModel.getBean();
         if (bean != null) {
             try {
                 getAddPropertyChangeListenerMethod().invoke(bean, new Object[] { listener });
             }
             catch (Exception e) {
                 throw new RuntimeException("Error adding PropertyChangeListener: ", e);
             }
         }
     }
 
     /**
      * Removes a property change listener to the bean if it supports it. If it doesn't support
      * removal of property change listeners, nothing happens.
      *
      * @param beanModel the bean's IModel.
      * @param listener the {@link PropertyChangeListener}.
      */
     public void removePropertyChangeListener(IModel beanModel, PropertyChangeListener listener)
     {
         if (!hasRemovePropertyChangeListenerMethod) {
             return;
         }
 
         Object bean = beanModel.getObject(null);
         if (bean != null) {
             try {
                 getRemovePropertyChangeListenerMethod().invoke(bean, new Object[] { listener });
             }
             catch (Exception e) {
                 throw new RuntimeException("Error removing PropertyChangeListener: ", e);
             }
         }
     }
     
     /**
      * A Cached Beanprops file.
      */
     private static final class CachedBeanProps implements Serializable
     {
         private JBeans beans;
         private long modTimestamp;
         
         CachedBeanProps(JBeans beans, long modTimestamp)
         {
             this.beans = beans;
             this.modTimestamp = modTimestamp;
         }
         
         JBeans getBeans()
         {
             return beans;
         }
         
         long getModTimestamp()
         {
             return modTimestamp;
         }
     }
 }
