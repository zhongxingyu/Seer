 /***************************************************
  *
  * cismet GmbH, Saarbruecken, Germany
  *
  *              ... and it just works.
  *
  ****************************************************/
 package de.cismet.cids.custom.deprecated;
 
 import de.cismet.cids.dynamics.Disposable;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.LayoutManager;
 import javax.swing.GroupLayout;
 
 import javax.swing.JPanel;
 import org.jdesktop.swingx.JXBusyLabel;
 import org.jdesktop.swingx.painter.BusyPainter;
 
 /**
  * Der JLoadSpinner erzeugt einen zeitlich unbestimmten Ladeanzeiger im Mac-Stil. Wird z.B. in Renderern beim Laden
  * eines Kartenausschnitts verwendet.
  *
  * @author   nh
  * @version  $Revision$, $Date$
  */
 public class JLoadDots extends JPanel implements Disposable {
 
     //~ Static fields/initializers ---------------------------------------------
     //~ Instance fields --------------------------------------------------------
     private JXBusyLabel busyLabel;
 
     //~ Constructors -----------------------------------------------------------
     /**
      * Erzeugt einen neuen JLoadSpinner.
      */
     public JLoadDots() {
 
         busyLabel = new JXBusyLabel(new Dimension(100, 100));
         this.add(busyLabel);
         BusyPainter painter = new BusyPainter(100);
         painter.setBaseColor(Color.lightGray.darker());
         painter.setPaintCentered(true);
 
         busyLabel.setBusyPainter(painter);
         busyLabel.setBusy(true);
 
     }
 
     @Override
     public void setLayout(LayoutManager mgr) {
         /*
          *Umgeheung des von Matisse automatisch erzeugten GroupLayouts
          */
        removeAll();
         if(mgr instanceof GroupLayout){
             super.setLayout(new BorderLayout());
             this.add(BorderLayout.CENTER,busyLabel);
         }
        else{
            super.setLayout(mgr);
            if(busyLabel != null)
                this.add(busyLabel);
        }
     }
 
     /**
      * Ueberschriebene setVisible-Methode des JPanels. Wird der JLoadSpinner versteckt so stoppt der Timer. Dieser wird
      * wieder gestartet, sobald der JLoadSpinner wieder angezeigt wird.
      *
      * @param  aFlag  boolean-Parameter ob der JLoadSpinner sichtbar gesetzt wird oder nicht
      */
     @Override
     public void setVisible(final boolean aFlag) {
         busyLabel.setBusy(aFlag);
         super.setVisible(aFlag);
     }
 
     @Override
     public void dispose() {
         busyLabel.setBusy(false);
     }
 
 
 }
