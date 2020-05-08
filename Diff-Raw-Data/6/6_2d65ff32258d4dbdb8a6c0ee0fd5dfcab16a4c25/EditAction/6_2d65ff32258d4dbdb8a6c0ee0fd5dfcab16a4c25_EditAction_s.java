 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.crisma;
 
 import Sirius.navigator.ui.ComponentRegistry;
 
 import org.openide.util.lookup.ServiceProvider;
 
 import java.awt.event.ActionEvent;
 
 import javax.swing.AbstractAction;
 
 import de.cismet.cids.custom.crisma.objectrenderer.WorldstatesRenderer;
 
 import de.cismet.cids.navigator.utils.CidsClientToolbarItem;
 
 import de.cismet.cids.tools.metaobjectrenderer.CidsBeanRenderer;
 
 /**
  * DOCUMENT ME!
  *
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 @ServiceProvider(service = CidsClientToolbarItem.class)
 public final class EditAction extends AbstractAction implements CidsClientToolbarItem {
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new EditAction object.
      */
     public EditAction() {
        super("Toggle Edit");
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void actionPerformed(final ActionEvent e) {
         final CidsBeanRenderer r = ComponentRegistry.getRegistry().getDescriptionPane().currentRenderer();
         if (r instanceof WorldstatesRenderer) {
             final WorldstatesRenderer wr = (WorldstatesRenderer)r;
             wr.setEditing(!wr.isEditing());
         }
     }
 
     @Override
     public String getSorterString() {
         return "-1";
     }
 
     @Override
     public boolean isVisible() {
         return true;
     }
 }
