 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.editors;
 
 import Sirius.navigator.tools.MetaObjectCache;
 
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaClassStore;
 import Sirius.server.middleware.types.MetaObject;
 
 import org.jdesktop.beansbinding.Converter;
 import org.jdesktop.beansbinding.Validator;
 
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.swing.ButtonGroup;
 import javax.swing.Icon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.SwingWorker;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.tools.CismetThreadPool;
 
 /**
  * DOCUMENT ME!
  *
  * @author   therter
  * @version  $Revision$, $Date$
  */
 public class DefaultBindableRadioButtonField extends JPanel implements Bindable, MetaClassStore, ActionListener {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
             DefaultBindableRadioButtonField.class);
     private static final String BINDING_PROPERTY = "selectedElements";
 
     //~ Instance fields --------------------------------------------------------
 
     private boolean enableLabels = true;
     private HashMap<Object, Icon> icons = null;
     private String iconProperty = null;
     private ButtonGroup bg = new ButtonGroup();
     private CidsBean selectedElements = null;
     private MetaClass mc = null;
     private Map<JRadioButton, MetaObject> boxToObjectMapping = new HashMap<JRadioButton, MetaObject>();
     private PropertyChangeSupport support = new PropertyChangeSupport(this);
     private volatile boolean threadRunning = false;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new CustomReferencedCheckboxField object.
      */
     public DefaultBindableRadioButtonField() {
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void addPropertyChangeListener(final PropertyChangeListener listener) {
         if (this.support != null) {
             this.support.addPropertyChangeListener(listener);
         }
     }
 
     @Override
     public void removePropertyChangeListener(final PropertyChangeListener listener) {
         if (this.support != null) {
             this.support.removePropertyChangeListener(listener);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  element  DOCUMENT ME!
      */
     public void setSelectedElements(final Object element) {
         if (element instanceof CidsBean) {
             this.selectedElements = (CidsBean)element;
         } else {
            if (element != null) {
                LOG.error("Selected element is not a CidsBean.");
            }
         }
 
         activateElement();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Object getSelectedElements() {
         return selectedElements;
     }
 
     @Override
     public String getBindingProperty() {
         return BINDING_PROPERTY;
     }
 
     @Override
     public Validator getValidator() {
         return null;
     }
 
     @Override
     public Converter getConverter() {
         return null;
     }
 
     @Override
     public Object getNullSourceValue() {
         return null;
     }
 
     @Override
     public Object getErrorSourceValue() {
         return null;
     }
 
     @Override
     public MetaClass getMetaClass() {
         return this.mc;
     }
 
     @Override
     public void setMetaClass(final MetaClass metaClass) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("set meta class " + ((metaClass != null) ? metaClass.getName() : "null"));
         }
 
         CismetThreadPool.execute(new SwingWorker<MetaObject[], Void>() {
 
                 @Override
                 protected MetaObject[] doInBackground() throws Exception {
                     while (!setThreadRunning()) {
                         try {
                             Thread.sleep(50);
                         } catch (final InterruptedException e) {
                             // nothing to do
                         }
                     }
 
                     selectedElements = null;
                     bg = new ButtonGroup();
                     boxToObjectMapping = new HashMap<JRadioButton, MetaObject>();
 
                     mc = metaClass;
 
                     if (mc != null) {
                         final String query = "select " + mc.getID() + ", " + mc.getPrimaryKey() + " from "
                                     + mc.getTableName();
 
                         return MetaObjectCache.getInstance().getMetaObjectByQuery(query);
                     } else {
                         LOG.error("Meta class is null.", new Throwable());
                     }
 
                     return null;
                 }
 
                 @Override
                 protected void done() {
                     try {
                         DefaultBindableRadioButtonField.this.removeAll();
 
                         JRadioButton button = null;
                         final MetaObject[] metaObjects = get();
 
                         if (icons == null) {
                             DefaultBindableRadioButtonField.this.setLayout(new GridLayout(metaObjects.length, 1));
                         } else {
                             DefaultBindableRadioButtonField.this.setLayout(new GridLayout(metaObjects.length, 2));
                         }
                         if (LOG.isDebugEnabled()) {
                             LOG.debug(metaObjects.length + " objects found.");
                         }
 
                         for (final MetaObject tmpMc : metaObjects) {
                             button = new JRadioButton();
                             button.addActionListener(DefaultBindableRadioButtonField.this);
                             button.setOpaque(false);
                             button.setContentAreaFilled(false);
                             if (enableLabels) {
                                 button.setText(tmpMc.getBean().toString());
                             }
                             bg.add(button);
                             if (icons != null) {
                                 final Icon i = icons.get(tmpMc.getBean().getProperty(iconProperty));
                                 final JLabel l = new JLabel();
                                 if (i != null) {
                                     l.setIcon(i);
                                 }
                                 l.setSize(i.getIconWidth(), i.getIconHeight());
                                 add(l);
                             }
                             add(button);
                             boxToObjectMapping.put(button, tmpMc);
 
                             if (Thread.currentThread().isInterrupted()) {
                                 return;
                             }
                         }
 
                         activateElement();
                     } catch (final Exception e) {
                         LOG.error("Exception while filling a radio button field.", e);
                     } finally {
                         threadRunning = false;
                     }
                 }
             });
     }
 
     /**
      * DOCUMENT ME!
      */
     private synchronized void activateElement() {
         if (selectedElements != null) {
             final Iterator<JRadioButton> it = boxToObjectMapping.keySet().iterator();
 
             while (it.hasNext()) {
                 final JRadioButton tmp = it.next();
                 final MetaObject mo = boxToObjectMapping.get(tmp);
                 if ((mo != null) && selectedElements.equals(mo.getBean())) {
                     bg.setSelected(tmp.getModel(), true);
                 }
             }
         } else {
             this.bg.clearSelection();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  decider                 DOCUMENT ME!
      * @param  removeSelectedElements  DOCUMENT ME!
      */
     public void refreshCheckboxState(final FieldStateDecider decider, final boolean removeSelectedElements) {
         CismetThreadPool.execute(new SwingWorker<Void, Void>() {
 
                 @Override
                 protected Void doInBackground() throws Exception {
                     while (!setThreadRunning()) {
                         try {
                             Thread.sleep(50);
                         } catch (final InterruptedException e) {
                             // nothing to do
                         }
                     }
 
                     return null;
                 }
 
                 @Override
                 protected void done() {
                     try {
                         if (LOG.isDebugEnabled()) {
                             LOG.debug("refresh CheckboxState", new Exception());
                         }
 
                         final Iterator<JRadioButton> it = boxToObjectMapping.keySet().iterator();
                         if (removeSelectedElements) {
                             selectedElements = null;
                         }
 
                         while (it.hasNext()) {
                             final JRadioButton button = it.next();
                             button.setEnabled(
                                 decider.isCheckboxForClassActive(boxToObjectMapping.get(button)));
                             button.setSelected(false);
                             if (Thread.currentThread().isInterrupted()) {
                                 return;
                             }
                         }
                         activateElement();
                     } finally {
                         threadRunning = false;
                     }
                 }
             });
     }
 
     /**
      * DOCUMENT ME!
      */
     public void dispose() {
     }
 
     @Override
     public void actionPerformed(final ActionEvent ae) {
         final MetaObject mo = boxToObjectMapping.get((JRadioButton)ae.getSource());
 
         final Object old = selectedElements;
         selectedElements = mo.getBean();
         support.firePropertyChange(BINDING_PROPERTY, old, mo.getBean());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  the icons
      */
     public HashMap<Object, Icon> getIcons() {
         return icons;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  icons         the icons to set
      * @param  iconProperty  DOCUMENT ME!
      */
     public void setIcons(final HashMap<Object, Icon> icons, final String iconProperty) {
         this.icons = icons;
         this.iconProperty = iconProperty;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  the enableLabels
      */
     public boolean isEnableLabels() {
         return enableLabels;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  enableLabels  the enableLabels to set
      */
     public void setEnableLabels(final boolean enableLabels) {
         this.enableLabels = enableLabels;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private synchronized boolean setThreadRunning() {
         if (threadRunning) {
             return false;
         } else {
             threadRunning = true;
             return true;
         }
     }
 }
