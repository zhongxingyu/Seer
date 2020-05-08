 /**
  * 
  */
 package org.eclipse.jubula.examples.aut.dvdtool.control;
 
 import java.awt.event.ActionEvent;
 import java.io.InputStream;
 
 import javax.swing.AbstractAction;
 
 import org.eclipse.jubula.examples.aut.dvdtool.persistence.DvdInvalidContentException;
 import org.eclipse.jubula.examples.aut.dvdtool.persistence.DvdPersistenceException;
 
 /**
  * @author al
  *
  */
 public class DvdLoadAction extends AbstractAction {
 
     /** the controller of the main frame */
     private transient DvdMainFrameController m_controller;
 
     /**
      * public constructor
      * @param name the text to display
      * @param controller the controller of the main frame
      */
     public DvdLoadAction(String name, DvdMainFrameController controller) {
         super(name);
         m_controller = controller;
     }
 
     /**
      * {@inheritDoc}
      */
     public void actionPerformed(ActionEvent ev) {
         try {
             final InputStream is = 
                     getClass().getClassLoader().getResourceAsStream(
                            "resources/default.dvd");
             if (is != null) {
                 DvdManager.singleton().open(m_controller, is);
                m_controller.opened("default");
             }
         } catch (DvdInvalidContentException e) {
             //  Auto-generated catch block
             e.printStackTrace();
         } catch (DvdPersistenceException e) {
             //  Auto-generated catch block
             e.printStackTrace();
         }
     }
 
 }
