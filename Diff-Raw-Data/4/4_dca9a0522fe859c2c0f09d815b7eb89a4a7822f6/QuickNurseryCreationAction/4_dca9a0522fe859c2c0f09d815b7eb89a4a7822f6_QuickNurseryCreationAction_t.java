 
 package ibfb.nursery.actions;
 
 import ibfb.domain.core.SelectedStudy;
 import ibfb.domain.core.Study;
 import ibfb.nursery.core.NurseryEditorTopComponent;
 import ibfb.nursery.quickcreation.JDQuickCreation;
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 import java.util.ArrayList;
 import java.util.ResourceBundle;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import org.openide.awt.ActionRegistration;
 import org.openide.awt.ActionReference;
 import org.openide.awt.ActionReferences;
 import org.openide.awt.ActionID;
 import org.openide.util.HelpCtx;
 import org.openide.util.Mutex;
 import org.openide.util.NbBundle;
 import org.openide.util.NbBundle.Messages;
 import org.openide.util.actions.SystemAction;
 import org.openide.windows.TopComponent;
 import org.openide.windows.WindowManager;
 
 @ActionID(category = "File",
 id = "ibfb.nursery.actions.QuickCreationAction")
 @ActionRegistration(iconBase = "ibfb/nursery/images/quickNursery16.png",
 displayName = "#CTL_QuickCreationAction")
 @ActionReferences({
     @ActionReference(path = "Menu/Study", position = 1243),
     @ActionReference(path = "Toolbars/File", position = -312)
 })
 
 public final class QuickNurseryCreationAction extends SystemAction implements ActionListener {
 
     private final Study context;
     private JDQuickCreation quick;
         private ResourceBundle bundle = NbBundle.getBundle(QuickNurseryCreationAction.class);
 
 
     
         public QuickNurseryCreationAction() {
         putValue(NAME, bundle.getString("QuickNurseryCreationAction.name"));
         setEnabled(Boolean.TRUE);
         this.context = null;
     }
     
     public QuickNurseryCreationAction(Study context) {
         this.context = context;
         setEnabled(Boolean.TRUE);
     }
 
     @Override
     public void actionPerformed(ActionEvent ev) {
          changeCursorWaitStatus(true);
         quick=new JDQuickCreation(null, true);
         
         TopComponent background = WindowManager.getDefault().findTopComponent("BackgroundWindowTopComponent");
         if (background.isOpened()) {
             background.close();
         }
         if (existeNursery("Nursery - "+SelectedStudy.selected.getStudy())) {
             int opcion = JOptionPane.showConfirmDialog(null, bundle.getString("QuickNurseryCreationAction.alreadyExists"), bundle.getString("QuickNurseryCreationAction.caution"), JOptionPane.YES_NO_OPTION);
             if (opcion == 0) {
                 NurseryEditorTopComponent nurseryEditor = null;
                 ArrayList<TopComponent> opened = new ArrayList<TopComponent>(WindowManager.getDefault().getRegistry().getOpened());
                 for (TopComponent t : opened) {
                    String studyname =  t.getName();
                    String selectedStudyName = "Nursery - "+SelectedStudy.selected.getStudy();
                    if (studyname.equals(selectedStudyName)) {
                         nurseryEditor = (NurseryEditorTopComponent) t;
                         nurseryEditor.close();
                     }
                 }
                 nurseryEditor.setStudy(SelectedStudy.selected);
                 quick.setNurseryWindow(nurseryEditor);                              
                 quick.cleanFields();
                 quick.setLocationRelativeTo(null);
                 quick.setVisible(true);
             }
 
         } else {
             NurseryEditorTopComponent nurseryEditor = new NurseryEditorTopComponent();
             nurseryEditor.setStudy(SelectedStudy.selected);
             nurseryEditor.setName("Nursery - "+SelectedStudy.selected.getStudy());           
             quick.setNurseryWindow(nurseryEditor);
             quick.cleanFields();
             quick.setLocationRelativeTo(null);
             quick.setVisible(true);
         }
     
       changeCursorWaitStatus(false);   
         
     }
     
     
     
     public boolean existeNursery(String nursery) {
         boolean existe = false;
         try {
             ArrayList<TopComponent> opened = new ArrayList<TopComponent>(WindowManager.getDefault().getRegistry().getOpened());
             for (TopComponent t : opened) {
                 if (t.getName().equals(nursery)) {
                     existe = true;
                 }
             }
         } catch (NullPointerException ex) {
             existe = false;
         }
         return existe;
     }
     
      
     
     private static void changeCursorWaitStatus(final boolean isWaiting) {
         Mutex.EVENT.writeAccess(new Runnable() {
 
             @Override
             public void run() {
                 try {
                     JFrame mainFrame =
                             (JFrame) WindowManager.getDefault().getMainWindow();
                     Component glassPane = mainFrame.getGlassPane();
                     if (isWaiting) {
                         glassPane.setVisible(true);
 
                         glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                     } else {
                         glassPane.setVisible(false);
 
                         glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                     }
                 } catch (Exception e) {
                 }
             }
         });
     }
 
     @Override
     public String getName() {
          return "Quick Nursery Creation";
     }
 
     @Override
     public HelpCtx getHelpCtx() {
        return null;
     }
 }
