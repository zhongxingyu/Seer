 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.cismet.cids.editors;
 
 import Sirius.navigator.tools.MetaObjectCache;
 
 import Sirius.server.localserver.attribute.ClassAttribute;
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaClassStore;
 import Sirius.server.middleware.types.MetaObject;
 
 import org.jdesktop.beansbinding.Converter;
 import org.jdesktop.beansbinding.Validator;
 
 import java.awt.Component;
 
 import java.io.Serializable;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.ListCellRenderer;
 import javax.swing.SwingWorker;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.tools.CismetThreadPool;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
 public class DefaultBindableReferenceCombo extends JComboBox implements Bindable, MetaClassStore, Serializable {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
             DefaultBindableReferenceCombo.class);
    private static final Comparator<CidsBean> beanToStringComparator;
 
     static {
         beanToStringComparator = new Comparator<CidsBean>() {
 
                 @Override
                 public final int compare(final CidsBean o1, final CidsBean o2) {
                     final String s1 = (o1 == null) ? "" : o1.toString();
                     final String s2 = (o2 == null) ? "" : o2.toString();
                     return (s1).compareToIgnoreCase(s2); // NOI18N
                 }
             };
     }
 
     //~ Instance fields --------------------------------------------------------
 
    private CidsBean cidsBean = null;
     private MetaClass metaClass = null;
 //    private String fieldname = null;
     private boolean fakeModel = false;
     private boolean nullable = false;
     private boolean onlyUsed = false;
     private Comparator<CidsBean> comparator = beanToStringComparator;
     private String nullValueRepresentation = " ";
 
     //~ Instance initializers --------------------------------------------------
 
     {
         setRenderer(new DefaultListCellRenderer() {
 
                 @Override
                 public Component getListCellRendererComponent(final JList list,
                         final Object value,
                         final int index,
                         final boolean isSelected,
                         final boolean cellHasFocus) {
                     final Component ret = super.getListCellRendererComponent(
                             list,
                             value,
                             index,
                             isSelected,
                             cellHasFocus);
                     if (value == null) {
                         ((JLabel)ret).setText(getNullValueRepresentation());
                     }
                     return ret;
                 }
             });
     }
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new DefaultBindableReferenceCombo object.
      */
     public DefaultBindableReferenceCombo() {
         final String[] s = new String[] { null };
 
         setModel(new DefaultComboBoxModel(s));
     }
 
     /**
      * Creates a new DefaultBindableReferenceCombo object.
      *
      * @param  nullable  DOCUMENT ME!
      */
     public DefaultBindableReferenceCombo(final boolean nullable) {
         this();
         this.nullable = nullable;
     }
 
     /**
      * Creates a new DefaultBindableReferenceCombo object.
      *
      * @param  comparator  DOCUMENT ME!
      */
     public DefaultBindableReferenceCombo(final Comparator<CidsBean> comparator) {
         this();
         this.comparator = comparator;
     }
 
     /**
      * Creates a new DefaultBindableReferenceCombo object.
      *
      * @param  mc  DOCUMENT ME!
      */
     public DefaultBindableReferenceCombo(final MetaClass mc) {
         this();
         init(mc);
     }
 
     /**
      * Creates a new DefaultBindableReferenceCombo object.
      *
      * @param  mc        DOCUMENT ME!
      * @param  nullable  DOCUMENT ME!
      * @param  onlyUsed  DOCUMENT ME!
      */
     public DefaultBindableReferenceCombo(final MetaClass mc, final boolean nullable, final boolean onlyUsed) {
         this();
         this.nullable = nullable;
         this.onlyUsed = onlyUsed;
         init(mc);
     }
 
     /**
      * Creates a new DefaultBindableReferenceCombo object.
      *
      * @param  mc          DOCUMENT ME!
      * @param  nullable    DOCUMENT ME!
      * @param  onlyUsed    DOCUMENT ME!
      * @param  comparator  DOCUMENT ME!
      */
     public DefaultBindableReferenceCombo(final MetaClass mc,
             final boolean nullable,
             final boolean onlyUsed,
             final Comparator<CidsBean> comparator) {
         this();
         this.nullable = nullable;
         this.onlyUsed = onlyUsed;
         this.comparator = comparator;
         init(mc);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param  mc  DOCUMENT ME!
      */
     private void init(final MetaClass mc) {
         if (!isFakeModel()) {
             CismetThreadPool.execute(new SwingWorker<DefaultComboBoxModel, Void>() {
 
                     @Override
                     protected DefaultComboBoxModel doInBackground() throws Exception {
                         return getModelByMetaClass(mc, nullable, onlyUsed, comparator);
                     }
 
                     @Override
                     protected void done() {
                         try {
                             setModel(get());
                             setSelectedItem(cidsBean);
                         } catch (InterruptedException interruptedException) {
                         } catch (ExecutionException executionException) {
                             log.error("Error while initializing the model of a referenceCombo", executionException); // NOI18N
                         }
                     }
                 });
         } else {
         }
     }
 
     @Override
     public String getBindingProperty() {
         return "selectedItem"; // NOI18N
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
     public void setSelectedItem(final Object anObject) {
         if (isFakeModel()) {
             setModel(new DefaultComboBoxModel(new Object[] { anObject }));
         }
         super.setSelectedItem(anObject);
         cidsBean = (CidsBean)anObject;
     }
 
     @Override
     public MetaClass getMetaClass() {
         return metaClass;
     }
 
     @Override
     public void setMetaClass(final MetaClass metaClass) {
         this.metaClass = metaClass;
         init(metaClass);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isFakeModel() {
         return fakeModel;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  fakeModel  DOCUMENT ME!
      */
     public void setFakeModel(final boolean fakeModel) {
         this.fakeModel = fakeModel;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getNullValueRepresentation() {
         return nullValueRepresentation;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  nullValueRepresentation  DOCUMENT ME!
      */
     public void setNullValueRepresentation(final String nullValueRepresentation) {
         this.nullValueRepresentation = nullValueRepresentation;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isNullable() {
         return nullable;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  onlyUsed  DOCUMENT ME!
      */
     public void setOnlyUsed(final boolean onlyUsed) {
         this.onlyUsed = onlyUsed;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isOnlyUsed() {
         return onlyUsed;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  nullable  DOCUMENT ME!
      */
     public void setNullable(final boolean nullable) {
         this.nullable = nullable;
     }
 
     @Override
     public Object getNullSourceValue() {
         return null;
     }
 
     @Override
     public Object getErrorSourceValue() {
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   mc          DOCUMENT ME!
      * @param   nullable    DOCUMENT ME!
      * @param   onlyUsed    DOCUMENT ME!
      * @param   comparator  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static DefaultComboBoxModel getModelByMetaClass(final MetaClass mc,
             final boolean nullable,
             final boolean onlyUsed,
             final Comparator<CidsBean> comparator) throws Exception {
         final ClassAttribute ca = mc.getClassAttribute("sortingColumn");                                 // NOI18N
         String orderBy = "";                                                                             // NOI18N
         if (ca != null) {
             final String value = ca.getValue().toString();
             orderBy = " order by " + value;                                                              // NOI18N
         }
         String query = "select " + mc.getID() + "," + mc.getPrimaryKey() + " from " + mc.getTableName(); // NOI18N
         if (onlyUsed) {
             query += " where used is true";                                                              // NOI18N
         }
         query += orderBy;
         final MetaObject[] MetaObjects = MetaObjectCache.getInstance().getMetaObjectByQuery(query);
         final List<CidsBean> cbv = new ArrayList<CidsBean>(MetaObjects.length);
         if (nullable) {
             cbv.add(null);
         }
         for (final MetaObject mo : MetaObjects) {
             cbv.add(mo.getBean());
         }
         if (ca == null) {
             // Sorts the model using String comparison on the bean's toString()
             Collections.sort(cbv, comparator);
         }
         return new DefaultComboBoxModel(cbv.toArray());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   mc        DOCUMENT ME!
      * @param   nullable  DOCUMENT ME!
      * @param   onlyUsed  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static DefaultComboBoxModel getModelByMetaClass(final MetaClass mc,
             final boolean nullable,
             final boolean onlyUsed) throws Exception {
         return getModelByMetaClass(mc, nullable, onlyUsed, beanToStringComparator);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   mc        DOCUMENT ME!
      * @param   nullable  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static DefaultComboBoxModel getModelByMetaClass(final MetaClass mc, final boolean nullable)
             throws Exception {
         return getModelByMetaClass(mc, nullable, false);
     }
 }
