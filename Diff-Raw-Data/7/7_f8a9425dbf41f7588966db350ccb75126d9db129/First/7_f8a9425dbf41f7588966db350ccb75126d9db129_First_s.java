 package at.ac.tuwien.sepm.ui.startUp;
 
 import at.ac.tuwien.sepm.entity.Curriculum;
 import at.ac.tuwien.sepm.entity.Module;
 import at.ac.tuwien.sepm.service.EscapeException;
 import at.ac.tuwien.sepm.service.ServiceException;
 import at.ac.tuwien.sepm.ui.SmallInfoPanel;
 import at.ac.tuwien.sepm.ui.template.PanelTube;
 import at.ac.tuwien.sepm.ui.template.WideComboBox;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Author: Georg Plaz
  */
 public class First extends StartRowPanel {
     private static Logger logger = LogManager.getLogger(First.class);
 
     private JTextField tissUsername = new JTextField();
     private JPasswordField tissPassword = new JPasswordField();
     private WideComboBox studyDrop;
     private JButton progressFurther;
     private JProgressBar progressBar = new JProgressBar();
 
     private boolean confirmed = false;
 
     //academicPrograms.setMinimumSize(new Dimension((int)this.getBounds().getWidth()-145, 20));
 
 
 
     public First(double width, double height,ViewStartUp parent) {
         super(width, height,parent);
 
         subInit();
     }
     public void subInit(){
        tissUsername.setText(getStartUp().propertyService.getProperty("user.user",""));
        tissPassword.setText(getStartUp().propertyService.getProperty("user.password",""));
         studyDrop = new WideComboBox();
         studyDrop.addItem(new CurriculumContainer());
         try {
             for(Curriculum c : getStartUp().lvaFetcherService.getAcademicPrograms()){
                 if (((c.getName().startsWith("Bachelor")) || (c.getName().startsWith("Master"))) && ((c.getName().contains("nformatik") || c.getName().contains("Software")) && !c.getName().contains("Geod"))){
                     studyDrop.addItem(new CurriculumContainer(c));
                 }
             }
         } catch (ServiceException e) {
             logger.error(e);
             PanelTube.backgroundPanel.viewInfoText("Es ist ein Fehler beim Lesen der Daten aufgetreten.",SmallInfoPanel.Error);
         }
         studyDrop.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent e) {
                 if(studyDrop.getSelectedIndex()==0){
                     progressFurther.setText("Weiter");
                 }else{
                     progressFurther.setText("Importieren");
                 }
             }
         });
         progressFurther = new JButton("weiter");
         progressFurther.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 try{
                     if(studyDrop.getSelectedIndex()== 0 && !confirmed){
                         PanelTube.backgroundPanel.viewInfoText("Wollen sie wirklich fortfahren, ohne ein Studium zu importieren?", SmallInfoPanel.Warning);
                         confirmed=true;
                         throw new EscapeException();
                     }
                     int un = tissUsername.getText().length();
                     int pw = tissPassword.getPassword().length;
                     if(un>0 && pw>0){
                         getStartUp().propertyService.setProperty("tiss.user",tissUsername.getText());
                         getStartUp().propertyService.setProperty("tiss.password",new String(tissPassword.getPassword()));
                     }else if((un==0)!=(pw==0)){
                         PanelTube.backgroundPanel.viewInfoText("Die TISS-Daten sind ungültig!", SmallInfoPanel.Warning);
                         throw new EscapeException();
                     }
 
                     if(studyDrop.getSelectedIndex()==0){
                         getStartUp().next();
                     }else{
                         setWaiting(true);
                         PanelTube.backgroundPanel.viewInfoText("Studium wird geladen. Bitte um etwas Geduld.", SmallInfoPanel.Info);
                         FetcherTask task = new FetcherTask();
                         task.execute();
                     }
 
                 }catch(EscapeException ignore){   }
             }
         });
         allLabels = new LinkedList<JComponent>();
 
        addText("Hallo!\n\nHerzlich willkommen im sTUdiumsmanager!.\nDies ist ein kurzer Startup-Wizard, " +
                 "der dir die wichtigsten Dinge im Programm kurz erklärt. Damit das Programm richtig funktioniert," +
                 "brauchen wir ein paar Informationen, die du auch gleich hier eingeben kannst!",true);
         addText("Alle Daten werden nur lokal gespeichert und eventuell zum Anmelden bei " +
                 "dem jeweiligen Dienst genutzt.",true);
         addText("Wenn du dich automatisch für Prüfungen anmelden lassen willst, musst du hier deine " +
                 "Tiss-Zugangsdaten eingeben. Du kannst sie aber auch jederzeit in den Einstellungen " +
                 "setzen oder ändern.\n",true);
         addRow(new JTextArea("TISS-Username"), tissUsername,true);
         addRow(new JTextArea("TISS-Passwort"), tissPassword,true);
 
         addText("\n\nUm dir LVAs für dein aktuelles Semester einzutragen (was du für alle Funktionen in dieser App brauchst), " +
                 "musst du vorher ein Studium importieren. Wähle hier dein Studium aus. Die Daten werden aus dem TISS ausgelesen." +
                 "Du kannst auch diesen Schritt auch erst später unter Lehrangebot -> Importieren " +
                 "ausführen\n.",false);
         addRow(new JTextArea("Studium"), studyDrop, false);
         addText("", false);
         addRow(null, progressFurther, false);
         progressBar.setIndeterminate(true);
         progressBar.setVisible(false);
         addText("",false);
         addRow(null, progressBar, false);
 
         logger.info("finished initializing");
     }
     private void setWaiting(boolean b){
         progressFurther.setEnabled(!b);
         progressBar.setVisible(b);
         tissPassword.setEnabled(!b);
         tissUsername.setEnabled(!b);
         studyDrop.setEnabled(!b);
     }
     private class CurriculumContainer{
         private Curriculum curriculum;
         private CurriculumContainer(Curriculum curriculum){
             this.curriculum = curriculum;
         }
         private CurriculumContainer(){
 
         }
         public String toString(){
             if(curriculum!=null){
                 return curriculum.getName();
             }
             return "Kein Studium augewählt";
         }
         public Curriculum get(){
             return curriculum;
         }
     }
     private class FetcherTask extends SwingWorker<Void, Void> {
         @Override
         protected Void doInBackground() throws Exception {
             try {
                 Curriculum curriculum = ((CurriculumContainer) studyDrop.getSelectedItem()).get();
                 //DefaultMutableTreeNode top = new DefaultMutableTreeNode(new CurriculumSelectItem(curriculum));
 
                 List<Module> currentModules = getStartUp().lvaFetcherService.getModules(curriculum.getStudyNumber(), true);
 
                 for(Module m : currentModules) {
                     getStartUp().moduleService.create(m);
                 }
                 PanelTube.backgroundPanel.viewInfoText("Gratuliere, das Studium wurde fertig geladen und importiert!", SmallInfoPanel.Success);
                 getStartUp().next();
             } catch (ServiceException e) {
                 logger.info("couldn't load LVAs", e);
                 PanelTube.backgroundPanel.viewInfoText("Die LVAs konnten leider nicht geladen werden.", SmallInfoPanel.Error);
 
             }
             setWaiting(false);
             //setWaiting(false);
             return null;
         }
     }
 
 }
