 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  *  Copyright (C) 2010 thorsten
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 /*
  * TabellenPanel.java
  *
  * Created on 24.11.2010, 20:42:25
  */
 package de.cismet.verdis.gui;
 
 import Sirius.navigator.connection.SessionManager;
 
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaObject;
 
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.LineString;
 
 import edu.umd.cs.piccolox.event.PNotification;
 
 import org.jdesktop.swingx.decorator.ColorHighlighter;
 import org.jdesktop.swingx.decorator.ComponentAdapter;
 import org.jdesktop.swingx.decorator.HighlightPredicate;
 import org.jdesktop.swingx.decorator.Highlighter;
 
 import java.awt.Color;
 import java.awt.Component;
 
 import java.sql.Date;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 
 import de.cismet.cids.custom.util.BindingValidationSupport;
 import de.cismet.cids.custom.util.CidsBeanSupport;
 
 import de.cismet.cids.dynamics.CidsBean;
 import de.cismet.cids.dynamics.CidsBeanStore;
 
 import de.cismet.cismap.commons.features.PureNewFeature;
 import de.cismet.cismap.commons.gui.piccolo.PFeature;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.AttachFeatureListener;
 
 import de.cismet.cismap.navigatorplugin.CidsFeature;
 
 import de.cismet.validation.Validator;
 
 import de.cismet.validation.validator.AggregatedValidator;
 
 import de.cismet.verdis.CidsAppBackend;
 
 import de.cismet.verdis.commons.constants.FrontinfoPropertyConstants;
 import de.cismet.verdis.commons.constants.KassenzeichenPropertyConstants;
 import de.cismet.verdis.commons.constants.VerdisMetaClassConstants;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
 public class WDSRTabellenPanel extends AbstractCidsBeanTable implements CidsBeanStore {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
             WDSRTabellenPanel.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private CidsBean cidsBean;
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JScrollPane jScrollPane1;
     private org.jdesktop.swingx.JXTable jxtOverview;
     private org.jdesktop.beansbinding.BindingGroup bindingGroup;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form TabellenPanel.
      */
     public WDSRTabellenPanel() {
         super(CidsAppBackend.Mode.ESW, new WDSRTableModel());
 
         initComponents();
         jxtOverview.setModel(getModel());
         final HighlightPredicate errorPredicate = new HighlightPredicate() {
 
                 @Override
                 public boolean isHighlighted(final Component renderer, final ComponentAdapter componentAdapter) {
                     final int displayedIndex = componentAdapter.row;
                     final int modelIndex = jxtOverview.getFilters().convertRowIndexToModel(displayedIndex);
                     final CidsBean cidsBean = getModel().getCidsBeanByIndex(modelIndex);
                     return getItemValidator(cidsBean).getState().isError();
                 }
             };
 
         final Highlighter errorHighlighter = new ColorHighlighter(errorPredicate, Color.RED, Color.WHITE);
 
         final HighlightPredicate changedPredicate = new HighlightPredicate() {
 
                 @Override
                 public boolean isHighlighted(final Component renderer, final ComponentAdapter componentAdapter) {
                     final int displayedIndex = componentAdapter.row;
                     final int modelIndex = jxtOverview.getFilters().convertRowIndexToModel(displayedIndex);
                     final CidsBean cidsBean = getModel().getCidsBeanByIndex(modelIndex);
                     if (cidsBean != null) {
                         return CidsAppBackend.getInstance().isEditable()
                                     && (cidsBean.getMetaObject().getStatus() == MetaObject.MODIFIED);
                     } else {
                         return false;
                     }
                 }
             };
 
         final Highlighter changedHighlighter = new ColorHighlighter(changedPredicate, null, Color.RED);
 
         final HighlightPredicate noGeometryPredicate = new HighlightPredicate() {
 
                 @Override
                 public boolean isHighlighted(final Component renderer, final ComponentAdapter componentAdapter) {
                     final int displayedIndex = componentAdapter.row;
                     final int modelIndex = jxtOverview.getFilters().convertRowIndexToModel(displayedIndex);
                     final CidsBean cidsBean = getModel().getCidsBeanByIndex(modelIndex);
                     return getGeometry(cidsBean) == null;
                 }
             };
 
         final Highlighter noGeometryHighlighter = new ColorHighlighter(noGeometryPredicate, Color.lightGray, null);
 
         jxtOverview.setHighlighters(changedHighlighter, noGeometryHighlighter, errorHighlighter);
 
         BindingValidationSupport.attachBindingValidationToAllTargets(bindingGroup);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public Validator getItemValidator(final CidsBean frontBean) {
         final AggregatedValidator aggVal = new AggregatedValidator();
         aggVal.add(WDSRDetailsPanel.getValidatorNummer(frontBean));
         aggVal.add(WDSRDetailsPanel.getValidatorLaengeGrafik(frontBean));
         aggVal.add(WDSRDetailsPanel.getValidatorLaengeKorrektur(frontBean));
         aggVal.add(WDSRDetailsPanel.getValidatorDatumErfassung(frontBean));
         aggVal.add(WDSRDetailsPanel.getValidatorVeranlagungWD(frontBean));
         aggVal.add(WDSRDetailsPanel.getValidatorVeranlagungSR(frontBean));
         return aggVal;
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         bindingGroup = new org.jdesktop.beansbinding.BindingGroup();
 
         jScrollPane1 = new javax.swing.JScrollPane();
         jxtOverview = getJXTable();
 
         setLayout(new java.awt.BorderLayout());
 
         jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
 
         jxtOverview.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
 
         final org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create(
                 "${cidsBean}");
         final org.jdesktop.swingbinding.JTableBinding jTableBinding = org.jdesktop.swingbinding.SwingBindings
                     .createJTableBinding(
                         org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                         this,
                         eLProperty,
                         jxtOverview);
         bindingGroup.addBinding(jTableBinding);
         jTableBinding.bind();
         final org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 this,
                 org.jdesktop.beansbinding.ELProperty.create("${selectedRow}"),
                 jxtOverview,
                 org.jdesktop.beansbinding.BeanProperty.create("selectedElement"));
         bindingGroup.addBinding(binding);
 
         jScrollPane1.setViewportView(jxtOverview);
 
         add(jScrollPane1, java.awt.BorderLayout.CENTER);
 
         bindingGroup.bind();
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  notification  DOCUMENT ME!
      */
     public void attachFeatureRequested(final PNotification notification) {
         final Object o = notification.getObject();
         if (o instanceof AttachFeatureListener) {
             final AttachFeatureListener afl = (AttachFeatureListener)o;
             final PFeature pf = afl.getFeatureToAttach();
             if ((pf.getFeature() instanceof PureNewFeature) && (pf.getFeature().getGeometry() instanceof LineString)) {
                 final List<CidsBean> selectedBeans = getSelectedBeans();
                 final CidsBean selectedBean = (!selectedBeans.isEmpty()) ? selectedBeans.get(0) : null;
                 if (selectedBean != null) {
                     final boolean hasGeometrie = getGeometry(selectedBean) != null;
                     final boolean isMarkedForDeletion = selectedBean.getMetaObject().getStatus()
                                 == MetaObject.TO_DELETE;
                     if (!hasGeometrie) {
                         if (isMarkedForDeletion) {
                             JOptionPane.showMessageDialog(
                                 Main.getMappingComponent(),
                                 "Dieser Fl\u00E4che kann im Moment keine Geometrie zugewiesen werden. Bitte zuerst speichern.");
                         } else {
                             try {
                                 final Geometry geom = pf.getFeature().getGeometry();
                                 final int laenge = (int)Math.abs(geom.getLength());
                                 Main.getMappingComponent().getFeatureCollection().removeFeature(pf.getFeature());
                                 setGeometry(geom, selectedBean);
                                 selectedBean.setProperty(FrontinfoPropertyConstants.PROP__LAENGE_GRAFIK, laenge);
                                 selectedBean.setProperty(FrontinfoPropertyConstants.PROP__LAENGE_KORREKTUR, laenge);
                                 final CidsFeature cidsFeature = createCidsFeature(selectedBean);
                                 final boolean editable = CidsAppBackend.getInstance().isEditable();
                                 cidsFeature.setEditable(editable);
                                 Main.getMappingComponent().getFeatureCollection().addFeature(cidsFeature);
                             } catch (Exception ex) {
                                 LOG.error("error while attaching feature", ex);
                             }
                         }
                     }
                 }
             } else if (pf.getFeature() instanceof CidsFeature) {
                 JOptionPane.showMessageDialog(
                     Main.getMappingComponent(),
                     "Es k\u00F6nnen nur nicht bereits zugeordnete Fl\u00E4chen zugeordnet werden.");
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public int getValidNummer() {
         int highestNummer = 0;
         for (final CidsBean flaecheBean : getAllBeans()) {
             final Integer nummer = (Integer)flaecheBean.getProperty(FrontinfoPropertyConstants.PROP__NUMMER);
             if (nummer == null) {
                 break;
             }
             try {
                 final int num = new Integer(nummer).intValue();
                 if (num > highestNummer) {
                     highestNummer = num;
                 }
             } catch (Exception ex) {
                 break;
             }
         }
         return highestNummer + 1;
     }
 
     @Override
     public CidsBean createNewBean() throws Exception {
         final MetaClass srMC = CidsAppBackend.getInstance()
                     .getVerdisMetaClass(VerdisMetaClassConstants.MC_STRASSENREINIGUNG);
         final MetaClass wdMC = CidsAppBackend.getInstance()
                     .getVerdisMetaClass(VerdisMetaClassConstants.MC_WINTERDIENST);
 
         final String srQuery = "SELECT " + srMC.getID() + ", " + srMC.getPrimaryKey() + " FROM " + srMC.getTableName()
                     + " WHERE schluessel = -100;";
         final String wdQuery = "SELECT " + wdMC.getID() + ", " + wdMC.getPrimaryKey() + " FROM " + wdMC.getTableName()
                     + " WHERE schluessel = -200;";
 
         final CidsBean frontinfoBean = CidsAppBackend.getInstance()
                     .getVerdisMetaClass(VerdisMetaClassConstants.MC_FRONTINFO)
                     .getEmptyInstance()
                     .getBean();
         final CidsBean geomBean = CidsAppBackend.getInstance()
                     .getVerdisMetaClass(VerdisMetaClassConstants.MC_GEOM)
                     .getEmptyInstance()
                     .getBean();
         final CidsBean strassenreinigungBean = SessionManager.getProxy().getMetaObjectByQuery(srQuery, 0)[0].getBean();
         final CidsBean winterdienstBean = SessionManager.getProxy().getMetaObjectByQuery(wdQuery, 0)[0].getBean();
 
         final int newId = getNextNewBeanId();
         frontinfoBean.setProperty(FrontinfoPropertyConstants.PROP__ID, newId);
         frontinfoBean.getMetaObject().setID(newId);
 
         // final CidsBean strasseBean = SessionManager.getProxy().getVerdisMetaObject(8, PROP__"strasse".getId(),
         // Main.DOMAIN).getBean();
 
         // cidsBean.setProperty(PROP__"strasse", strasseBean);
         frontinfoBean.setProperty(FrontinfoPropertyConstants.PROP__GEOMETRIE, geomBean);
         frontinfoBean.setProperty(FrontinfoPropertyConstants.PROP__SR_KLASSE_OR, strassenreinigungBean);
         frontinfoBean.setProperty(FrontinfoPropertyConstants.PROP__WD_PRIO_OR, winterdienstBean);
         frontinfoBean.setProperty(FrontinfoPropertyConstants.PROP__NUMMER, getValidNummer());
         frontinfoBean.setProperty(
             FrontinfoPropertyConstants.PROP__ERFASSUNGSDATUM,
             new Date(Calendar.getInstance().getTime().getTime()));
 
         final PFeature sole = Main.getMappingComponent().getSolePureNewFeature();
         if ((sole != null) && (sole.getFeature().getGeometry() instanceof LineString)) {
             final int answer = JOptionPane.showConfirmDialog(
                     Main.getCurrentInstance(),
                     "Soll die vorhandene, noch nicht zugeordnete Geometrie der neuen Front zugeordnet werden?",
                     "Geometrie verwenden?",
                     JOptionPane.YES_NO_OPTION,
                     JOptionPane.QUESTION_MESSAGE);
             if (answer == JOptionPane.YES_OPTION) {
                 try {
                     final Geometry geom = sole.getFeature().getGeometry();
                     // größe berechnen und zuweisen
                    final double abs_laenge = Math.abs(geom.getLength());
                    //round to second decimal place 
                    final int laenge = (int) Math.round(abs_laenge*100)/100;
                     frontinfoBean.setProperty(FrontinfoPropertyConstants.PROP__LAENGE_GRAFIK, laenge);
                     frontinfoBean.setProperty(FrontinfoPropertyConstants.PROP__LAENGE_KORREKTUR, laenge);
                     setGeometry(geom, frontinfoBean);
                     frontinfoBean.setProperty(FrontinfoPropertyConstants.PROP__NUMMER, getValidNummer());
 
                     // unzugeordnete Geometrie aus Karte entfernen
                     Main.getMappingComponent().getFeatureCollection().removeFeature(sole.getFeature());
                 } catch (Exception ex) {
                     LOG.error("error while assigning feature to new flaeche", ex);
                 }
             }
         }
         return frontinfoBean;
     }
 
     @Override
     public void removeBean(final CidsBean cidsBean) {
         if (cidsBean != null) {
             final CidsBean geomBean = (CidsBean)cidsBean.getProperty(FrontinfoPropertyConstants.PROP__GEOMETRIE);
             try {
                 if (geomBean != null) {
                     geomBean.delete();
                 }
                 cidsBean.delete();
             } catch (final Exception ex) {
                 LOG.error("error while removing frontbean", ex);
             }
         }
         super.removeBean(cidsBean);
     }
 
     @Override
     public CidsBean getCidsBean() {
         return cidsBean;
     }
 
     @Override
     public void setCidsBean(final CidsBean cidsBean) {
         this.cidsBean = cidsBean;
 
         final String prop = KassenzeichenPropertyConstants.PROP__FRONTEN;
         if ((cidsBean != null) && (cidsBean.getProperty(prop) instanceof List)) {
             setCidsBeans((List<CidsBean>)cidsBean.getProperty(prop));
         } else {
             setCidsBeans(new ArrayList<CidsBean>());
         }
     }
 
     @Override
     public void setGeometry(final Geometry geometry, final CidsBean cidsBean) throws Exception {
         WDSRDetailsPanel.setGeometry(geometry, cidsBean);
     }
 
     @Override
     public Geometry getGeometry(final CidsBean cidsBean) {
         return WDSRDetailsPanel.getGeometry(cidsBean);
     }
 }
