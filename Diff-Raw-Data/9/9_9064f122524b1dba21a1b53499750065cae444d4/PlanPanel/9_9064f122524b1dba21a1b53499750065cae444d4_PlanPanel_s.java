 package at.ac.tuwien.sepm.ui.studyProgress;
 
 import at.ac.tuwien.sepm.dao.DateDao;
 import at.ac.tuwien.sepm.dao.LvaDao;
 import at.ac.tuwien.sepm.dao.MetaLvaDao;
 import at.ac.tuwien.sepm.entity.LVA;
 import at.ac.tuwien.sepm.entity.MetaLVA;
 import at.ac.tuwien.sepm.service.DateService;
 import at.ac.tuwien.sepm.service.EscapeException;
 import at.ac.tuwien.sepm.service.IntelligentSemesterPlaner;
 import at.ac.tuwien.sepm.service.Semester;
 import at.ac.tuwien.sepm.service.impl.IntelligentSemesterPlanerImpl;
 import at.ac.tuwien.sepm.service.impl.LVAUtil;
 import at.ac.tuwien.sepm.ui.SmallInfoPanel;
 import at.ac.tuwien.sepm.ui.StandardInsidePanel;
 import at.ac.tuwien.sepm.ui.UI;
 import at.ac.tuwien.sepm.ui.metaLva.MetaLVADisplayPanel;
 import at.ac.tuwien.sepm.ui.template.PanelTube;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Flo, Georg
  * Date: 01.06.13
  * Time: 16:19
  * To change this template use File | Settings | File Templates.
  */
 @UI
 public class PlanPanel extends StandardInsidePanel {
     MetaLvaDao metaLVADAO;
     LvaDao lvaDAO;
     DateDao dateDAO;
 
     /*@Autowired
     LVAService lvaService;
 */
     DateService dateService;
 
     Logger logger = LogManager.getLogger(this.getClass().getSimpleName());
 
     private JProgressBar progressBar = new JProgressBar();
     private Rectangle outputPlane = new Rectangle(483,12,521,496);
     private MetaLVADisplayPanel pane;
     private List<MetaLVA> plannedMetaLVAs = new ArrayList<MetaLVA>(0);
     private int plannedYear =-1;
     private Semester plannedSemester =null;
 
     private JButton plan;
     private JButton take;
 
     // < basic settings >
     private JLabel desiredECTSTextLabel;
     private JTextField desiredECTSText;
     private JLabel yearTextLabel;
     private JTextField yearText;
     private JLabel semesterDropLabel;
     private JComboBox  semesterDrop;
     // </ basic settings >
 
     // < advanced settings >
     private JButton showAdvancedOptions;
     private JLabel intersectVOCheckLabel;
     private JCheckBox intersectVOCheck;
     private JLabel intersectUECheckLabel;
     private JCheckBox intersectUECheck;
     private JLabel intersectExamCheckLabel;
     private JCheckBox intersectExamCheck;
     private JLabel intersectCustomCheckLabel;
     private JCheckBox intersectCustomCheck;
     private JLabel considerStudyProgressCheckLabel;
     private JCheckBox considerStudyProgressCheck;
     private JLabel toleranceLabel;
     private JLabel toleranceText;
     private JLabel timeBetweenLabel;
     private JComboBox timeBetweenDropdown;
     private String[] timeBetweenTextLabelStrings;
     private JLabel timeBetweenTextLabel;
     private JLabel timeBetweenIntersectText;
     private JLabel timeBetweenBufferText;
     private JSpinner tolerance;
     private JSpinner timeIntersect;
     private JSpinner timeBuffer;
     // </ advanced settings >
 
     private boolean advancedShown = true;
     private boolean showTimeBetweenText = false;
     private boolean planningInProgress;
 
     @Autowired
     public PlanPanel(MetaLvaDao metaLVADAO,DateDao dateDao,DateService dateService,LvaDao lvaDAO) {
         this.lvaDAO=lvaDAO;
         this.dateService=dateService;
         this.metaLVADAO= metaLVADAO;
         this.dateDAO = dateDao;
 
 
         this.setLayout(null);
         this.setOpaque(false);
         loadFonts();
         setBounds((int) startCoordinateOfWhiteSpace.getX(), (int) startCoordinateOfWhiteSpace.getY(),(int) whiteSpace.getWidth(),(int) whiteSpace.getHeight());
         initTextAndLabels();
         toggleAdvanced();
         initButtons();
         initLVAPane();
     }
     private void refreshMetaLVAs(List<MetaLVA> metaLVAs){
         plannedMetaLVAs =metaLVAs;
         remove(pane);
         pane = new MetaLVADisplayPanel(plannedMetaLVAs, (int)outputPlane.getWidth(), (int)outputPlane.getHeight());
         pane.setBounds(outputPlane);
         add(pane);
         repaint();
         revalidate();
     }
 
      /*-----------------------RIGHT SIDE  PLAN ANZEIGEN-------------------*/
 
     private void initLVAPane() {
         //plannedMetaLVAs = metaLVADAO.readByYearSemesterStudyProgress(dateService.getCurrentYear(), dateService.getCurrentSemester(), true);
 
         plannedMetaLVAs = new ArrayList<MetaLVA>(); //metaLVADAO.readUncompletedByYearSemesterStudyProgress(2013,Semester.S, true);
         pane = new MetaLVADisplayPanel(plannedMetaLVAs, (int)outputPlane.getWidth(), (int)outputPlane.getHeight()); //todo plannedMetaLVAs anzeigen die schon geplant sind
         pane.setBounds(outputPlane);
         this.add(pane);
     }
 
 
     /*-----------------------LEFT SIDE  PLANEN-------------------*/
     private void initButtons() {
         take = new JButton("Übernehmen");
         take.setFont(standardButtonFont);
         take.setBounds((int) outputPlane.getX() - 150, (int) outputPlane.getY() + (int) outputPlane.getHeight() - 40, 130, 40);
         take.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 //todo warning alert: all data from year x, sem y will be overriden
                 List<LVA> toRemove = lvaDAO.readUncompletedByYearSemesterStudyProgress(plannedYear,plannedSemester,true);
                 logger.debug("deleting from studyProgress:\n"+LVAUtil.formatShortLVA(toRemove,1));
                 try {
                     for(LVA lva:toRemove){
                         //logger.debug("deleting from studyProgress: "+lva);
                         lva.setInStudyProgress(false);
                         lvaDAO.update(lva);
                     }
                     out:
                     for(MetaLVA m:plannedMetaLVAs){
                         LVA temp = m.getLVA(plannedYear,plannedSemester);
                         temp.setInStudyProgress(true);
                         //logger.debug("adding to studyProgress: "+temp);
                         try {
                             lvaDAO.update(temp);
                             PanelTube.backgroundPanel.viewInfoText("Daten erfolgreich Übernommen", SmallInfoPanel.Success);
                             refreshMetaLVAs(new ArrayList<MetaLVA>(0));
                         } catch (IOException e1) {
                             logger.error(e1);
                             PanelTube.backgroundPanel.viewInfoText("Beim speichern ist ein Problem aufgetreten.", SmallInfoPanel.Error);
                             for(MetaLVA m2:plannedMetaLVAs){    //rollback
                                 LVA temp2 = m.getLVA(plannedYear,plannedSemester);
                                 if(m2==m){
                                     break out;
                                 }
                                 temp2.setInStudyProgress(false);
                                 try {
                                     lvaDAO.update(temp2);
                                 } catch (IOException e2) {
                                     logger.error(e2);
                                 }
                             }
                         }
                     }
                 } catch (IOException e1) {
                     PanelTube.backgroundPanel.viewInfoText("Beim speichern ist ein Problem aufgetreten.", SmallInfoPanel.Error);
                 }
 
                 logger.debug("adding to studyProgress: \n"+LVAUtil.formatShortDetailedMetaLVA(plannedMetaLVAs, 1));
 
 
             }
         });
         take.setEnabled(false);
         this.add(take);
 
         plan = new JButton("Planen");
         plan.setFont(standardButtonFont);
         plan.setBounds((int) outputPlane.getX() - 130 - take.getWidth(), (int) outputPlane.getY() + (int) outputPlane.getHeight() - 40, 90, 40);
         progressBar.setIndeterminate(true);
         progressBar.setVisible(false);
         progressBar.setBounds(plan.getX()-(progressBar.getPreferredSize().width+20),plan.getY(),progressBar.getPreferredSize().width,progressBar.getPreferredSize().height);
         add(progressBar);
         plan.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if(planningInProgress){
                     //return error
                 }else{
                     new Thread(){
                         private IntelligentSemesterPlaner planer = new IntelligentSemesterPlanerImpl();
                         public void start(){
                             super.start();
                         }
                         @Override
                         public void run(){
                             planningInProgress(true);
                             try{
                                 float goalECTS = Float.parseFloat(desiredECTSText.getText());
                                 //boolean vointersect =  intersectVOCheck.isSelected();
                                 plannedYear = Integer.parseInt(yearText.getText());
                                 plannedSemester = Semester.S;
                                 if (semesterDrop.getSelectedIndex() == 0) {
                                     plannedSemester = Semester.W;
                                 }
                                 List<MetaLVA> forced;
                                 List<MetaLVA> pool= metaLVADAO.readUncompletedByYearSemesterStudyProgress(plannedYear,plannedSemester,false);
                                 if (considerStudyProgressCheck.isSelected()){
                                     forced = metaLVADAO.readUncompletedByYearSemesterStudyProgress(plannedYear, plannedSemester, true);
                                 }else{
                                     forced = new ArrayList<>();
                                     pool.addAll(metaLVADAO.readUncompletedByYearSemesterStudyProgress(plannedYear, plannedSemester, true));
                                 }
                                 if(pool.isEmpty()){
                                     throw new EscapeException("Es wurden keine LVAs im gewünschten Semester gefunden");
                                 }
                                 MetaLVA customMetaLVA = new MetaLVA();
 
                                 if(intersectCustomCheck.isSelected()){
                                     customMetaLVA.setLVA(dateDAO.readNotToIntersectByYearSemester(plannedYear,plannedSemester));
                                     //logger.debug(customMetaLVA.getLVA(plannedYear,plannedSemester));
                                     customMetaLVA.setName("custom dates");
                                     customMetaLVA.setNr("UniqueNr!!!");
                                     forced.add(customMetaLVA);
                                     pool.add(customMetaLVA); //todo ohne diese zeile bug?!
                                 }
                                 List<Integer> typesToIntersect = new ArrayList<Integer>();
                                 if(intersectVOCheck.isSelected()){
                                     typesToIntersect.add(LVAUtil.LECTURE_TIMES);
                                 }
                                 if(intersectUECheck.isSelected()){
                                     typesToIntersect.add(LVAUtil.EXERCISES_TIMES);
                                 }
                                 if(intersectExamCheck.isSelected()){
                                     typesToIntersect.add(LVAUtil.EXAM_TIMES);
                                 }
                                 planer.setLVAs(forced, pool);
                                 planer.setTypesToIntersect(typesToIntersect);
 
                                 float tempTolerance = 0;
                                 tempTolerance = ((Number)tolerance.getValue()).floatValue();
                                 tempTolerance = tempTolerance/100;
                                // tempTolerance=Float.parseFloat(toleranceText.getText().replace("%","").trim())/100;
 
                                 int tempTimeBetween = 0;
                                 if(timeBetweenDropdown.getSelectedIndex()==1){
                                     tempTimeBetween = (int)timeIntersect.getValue();
                                    tempTimeBetween=tempTimeBetween/60;
                                    //tempTimeBetween = -Integer.parseInt(timeBetweenIntersectText.getText().replace("min","").trim())/60;
                                 }else if(timeBetweenDropdown.getSelectedIndex()==2){
                                     tempTimeBetween = (int)timeBuffer.getValue();
                                    tempTimeBetween = tempTimeBetween/60;
                                    //tempTimeBetween = Integer.parseInt(timeBetweenBufferText.getText().replace("min","").trim())/60;
                                 }
                                 planer.setIntersectingTolerance(tempTolerance);
                                 planer.setAllowedTimeBetween(tempTimeBetween);
 
                                 ArrayList<MetaLVA> solution = planer.planSemester(goalECTS, plannedYear, plannedSemester);
                                 if(intersectCustomCheck.isSelected()){
                                     solution.remove(customMetaLVA);
                                     solution.remove(customMetaLVA);
                                 }
                                 logger.info("solution provided by planner:\n" + LVAUtil.formatShortMetaLVA(solution, 1));
 
                                 refreshMetaLVAs(solution);
                                 planningInProgress(false);
                                 take.setEnabled(true);
                             }catch(EscapeException e){
                                 PanelTube.backgroundPanel.viewInfoText(e.getMessage(), SmallInfoPanel.Warning);
 
                                 planningInProgress(false);
                             }
                         }
                     }.start();
                 }
             }
         });
         this.add(plan);
     }
     private void planningInProgress(boolean inProgress){
         planningInProgress = inProgress;
         plan.setEnabled(!inProgress);
         progressBar.setVisible(inProgress);
         take.setEnabled(!inProgress);
     }
     private void toggleAdvanced(){
         advancedShown=!advancedShown;
         refreshAdvanced();
     }
     private void refreshAdvanced() {
         intersectVOCheckLabel.setVisible(!advancedShown);
         intersectVOCheck.setVisible(!advancedShown);
 
         intersectUECheckLabel.setVisible(!advancedShown);
         intersectUECheck.setVisible(!advancedShown);
 
         intersectExamCheckLabel.setVisible(!advancedShown);
         intersectExamCheck.setVisible(!advancedShown);
 
         intersectCustomCheckLabel.setVisible(!advancedShown);
         intersectCustomCheck.setVisible(!advancedShown);
 
         considerStudyProgressCheck.setVisible(!advancedShown);
         considerStudyProgressCheckLabel.setVisible(!advancedShown);
 
         tolerance.setVisible(!advancedShown);
         toleranceLabel.setVisible(!advancedShown);
         toleranceText.setVisible(!advancedShown);
 
         timeBetweenLabel.setVisible(!advancedShown);
         timeBetweenDropdown.setVisible(!advancedShown);
 
         timeBetweenTextLabel.setVisible(!advancedShown);
         switch(timeBetweenDropdown.getSelectedIndex()){
             case 1:
                 timeBetweenIntersectText.setVisible(!advancedShown);
                 timeIntersect.setVisible(!advancedShown);
                 timeBetweenBufferText.setVisible(false);
                 timeBuffer.setVisible(false);
                 break;
             case 2:
                 timeBetweenIntersectText.setVisible(false);
                 timeIntersect.setVisible(false);
                 timeBetweenBufferText.setVisible(!advancedShown);
                 timeBuffer.setVisible(!advancedShown);
                 break;
             default:
                 timeBetweenIntersectText.setVisible(false);
                 timeIntersect.setVisible(false);
                 timeBetweenBufferText.setVisible(false);
                 timeBuffer.setVisible(false);
         }
         repaint();
         //advancedShown=!advancedShown;
     }
 
     private void initTextAndLabels() {
         int verticalSpace = 10;
         int textHeight = 25;
         int textWidth = 150;
 
 
         // < basic settings >
         desiredECTSTextLabel = new JLabel("Gewünschte ECTS:");
         desiredECTSTextLabel.setFont(standardTextFont);
         desiredECTSTextLabel.setBounds(10, 10, textWidth, textHeight);
         this.add(desiredECTSTextLabel);
 
         desiredECTSText = new JTextField("30");
         desiredECTSText.setBounds(desiredECTSTextLabel.getX()+desiredECTSTextLabel.getWidth()+5,desiredECTSTextLabel.getY(),25,textHeight);
         desiredECTSText.setFont(standardTextFont);
         this.add(desiredECTSText);
 
         yearTextLabel = new JLabel("Jahr:");
         yearTextLabel.setBounds(desiredECTSTextLabel.getX(),desiredECTSTextLabel.getY()+desiredECTSTextLabel.getHeight()+verticalSpace, textWidth, textHeight);
         yearTextLabel.setFont(standardTextFont);
         this.add(yearTextLabel);
 
         yearText = new JTextField(""+dateService.getCurrentYearOfSemester());
         yearText.setBounds(yearTextLabel.getX() + yearTextLabel.getWidth() +5, yearTextLabel.getY(), 50, textHeight);
         yearText.setFont(standardTextFont);
         this.add(yearText);
         plannedYear=Integer.parseInt(yearText.getText());
         yearText.addKeyListener(new KeyListener() {
             @Override
             public void keyTyped(KeyEvent e) {}
             @Override
             public void keyPressed(KeyEvent e) {}
             @Override
             public void keyReleased(KeyEvent e) {
                 refreshMetaLVAs(new ArrayList<MetaLVA>(0));
             }
         });
 
         semesterDropLabel = new JLabel("Semester:");
         semesterDropLabel.setFont(standardTextFont);
         semesterDropLabel.setBounds(yearTextLabel.getX(),yearTextLabel.getY()+yearTextLabel.getHeight()+verticalSpace, textWidth, textHeight);
         this.add(semesterDropLabel);
 
 
         semesterDrop = new JComboBox (new String[]{"Winter","Sommer"});
         semesterDrop.setFont(standardButtonFont);
         semesterDrop.setBounds(semesterDropLabel.getX()+semesterDropLabel.getWidth()+5, semesterDropLabel.getY(), 100, textHeight);
         this.add(semesterDrop);
         if(dateService.getCurrentSemester().equals(Semester.W)){
             plannedSemester=Semester.W;
         }else{
             plannedSemester = Semester.S;
             semesterDrop.setSelectedIndex(1);
         }
         semesterDrop.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent e) {
                 if (e.getStateChange() == ItemEvent.SELECTED) {
                     refreshMetaLVAs(new ArrayList<MetaLVA>(0));
                 }
             }
         });
         // </ basic settings >
 
         // < advanced settings >
         showAdvancedOptions = new JButton("Erweiterte Optionen");
         showAdvancedOptions.setFont(standardButtonFont);
         showAdvancedOptions.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 toggleAdvanced();
                 PlanPanel.this.repaint();
             }
         });
         showAdvancedOptions.setBounds(semesterDropLabel.getX()+50, semesterDropLabel.getY()+semesterDropLabel.getHeight()+verticalSpace, textWidth+20, textHeight);
         this.add(showAdvancedOptions);
 
         textWidth=410;
 
         intersectVOCheckLabel = new JLabel("Überprüfe Vorlesungstermine auf Überschneidungen:");
         intersectVOCheckLabel.setFont(standardTextFont);
         intersectVOCheckLabel.setBounds(semesterDropLabel.getX(), showAdvancedOptions.getY()+showAdvancedOptions.getHeight()+verticalSpace, textWidth,textHeight);
         this.add(intersectVOCheckLabel);
 
         intersectVOCheck = new JCheckBox();
         intersectVOCheck.addChangeListener(dONTFUCKINGBUGSWINGListener());
         intersectVOCheck.setBackground(new Color(0, 0, 0, 0));
         intersectVOCheck.setBounds(intersectVOCheckLabel.getX() + intersectVOCheckLabel.getWidth() + 5, intersectVOCheckLabel.getY() + 5, 20, 20);
         this.add(intersectVOCheck);
         intersectVOCheck.setSelected(true);
 
         intersectUECheckLabel = new JLabel("Überprüfe Übungstermine auf Überschneidungen:");
         intersectUECheckLabel.setFont(standardTextFont);
         intersectUECheckLabel.setBounds(intersectVOCheckLabel.getX(), intersectVOCheckLabel.getY() + intersectVOCheckLabel.getHeight() + verticalSpace, textWidth, textHeight);
         this.add(intersectUECheckLabel);
 
         intersectUECheck = new JCheckBox();
         intersectUECheck.addChangeListener(dONTFUCKINGBUGSWINGListener());
         intersectUECheck.setBackground(new Color(0, 0, 0, 0));
         intersectUECheck.setBounds(intersectUECheckLabel.getX() + intersectUECheckLabel.getWidth() + 5, intersectUECheckLabel.getY() + 5, 20, 20);
         this.add(intersectUECheck);
         intersectUECheck.setSelected(true);
 
         intersectExamCheckLabel = new JLabel("Überprüfe Prüfungstermine auf Überschneidungen:");
         intersectExamCheckLabel.setFont(standardTextFont);
         intersectExamCheckLabel.setBounds(intersectUECheckLabel.getX(), intersectUECheckLabel.getY()+intersectUECheckLabel.getHeight()+verticalSpace, textWidth,textHeight);
         this.add(intersectExamCheckLabel);
 
         intersectExamCheck = new JCheckBox();
         intersectExamCheck.addChangeListener(dONTFUCKINGBUGSWINGListener());
         intersectExamCheck.setBackground(new Color(0, 0, 0, 0));
         intersectExamCheck.setBounds(intersectExamCheckLabel.getX()+intersectExamCheckLabel.getWidth()+5, intersectExamCheckLabel.getY()+5, 20, 20);
         this.add(intersectExamCheck);
         intersectExamCheck.setSelected(true);
 
         intersectCustomCheckLabel = new JLabel("Überprüfe private Termine auf Überschneidungen:");
         intersectCustomCheckLabel.setFont(standardTextFont);
         intersectCustomCheckLabel.setBounds(intersectExamCheckLabel.getX(), intersectExamCheckLabel.getY() + intersectExamCheckLabel.getHeight() + verticalSpace, textWidth, textHeight);
         this.add(intersectCustomCheckLabel);
 
         intersectCustomCheck = new JCheckBox();
         intersectCustomCheck.addChangeListener(dONTFUCKINGBUGSWINGListener());
         intersectCustomCheck.setBackground(new Color(0, 0, 0, 0));
         intersectCustomCheck.setBounds(intersectCustomCheckLabel.getX()+intersectCustomCheckLabel.getWidth()+5, intersectCustomCheckLabel.getY()+5, 20, 20);
         this.add(intersectCustomCheck);
 
 
         considerStudyProgressCheckLabel = new JLabel("Inkludiere bereits verplante LVAs in dem Semester:");
         considerStudyProgressCheckLabel.setFont(standardTextFont);
         considerStudyProgressCheckLabel.setBounds(intersectCustomCheckLabel.getX(), intersectCustomCheckLabel.getY()+intersectCustomCheckLabel.getHeight()+verticalSpace, textWidth,textHeight);
         this.add(considerStudyProgressCheckLabel);
 
         considerStudyProgressCheck = new JCheckBox();
         considerStudyProgressCheck.addChangeListener(dONTFUCKINGBUGSWINGListener());
         considerStudyProgressCheck.setBackground(new Color(0,0,0,0));
         considerStudyProgressCheck.setBounds(considerStudyProgressCheckLabel.getX()+considerStudyProgressCheckLabel.getWidth()+5, considerStudyProgressCheckLabel.getY()+5, 20, 20);
         this.add(considerStudyProgressCheck);
         considerStudyProgressCheck.setSelected(true);
 
         toleranceLabel = new JLabel("Prozent der Termine, die sich schneiden dürfen:");
         toleranceLabel.setFont(standardTextFont);
         toleranceLabel.setBounds(considerStudyProgressCheckLabel.getX(), considerStudyProgressCheckLabel.getY()+considerStudyProgressCheckLabel.getHeight()+verticalSpace, textWidth,textHeight);
         this.add(toleranceLabel);
 
         tolerance = new JSpinner();
         tolerance.setModel(new SpinnerNumberModel(10, 0, 100, 5));
         tolerance.setBounds(toleranceLabel.getX() + toleranceLabel.getWidth() + 5 -20, toleranceLabel.getY() + 5, 42, 20);
         this.add(tolerance);
 
         toleranceText = new JLabel("%");
         toleranceText.setBackground(new Color(0, 0, 0, 0));
         toleranceText.setBounds(tolerance.getX() + tolerance.getWidth() + 1, tolerance.getY(), 20, 20);
         this.add(toleranceText);
 
 
         timeBetweenLabel = new JLabel("Zeit zwischen Terminen:");
         timeBetweenLabel.setFont(standardTextFont);
         timeBetweenLabel.setBounds(toleranceLabel.getX(), toleranceLabel.getY()+toleranceLabel.getHeight()+verticalSpace, textWidth,textHeight);
         this.add(timeBetweenLabel);
 
         timeBetweenDropdown = new JComboBox(new String[]{"exakte Zeiten verwenden","Termine dürfen sich überschneiden","Zwischen Terminen Zeit erzwingen"});
         timeBetweenDropdown.setFont(standardButtonFont);
         timeBetweenDropdown.setBounds(timeBetweenLabel.getX() + timeBetweenLabel.getWidth() - 197, timeBetweenLabel.getY(), 220, textHeight);
         timeBetweenDropdown.setSelectedIndex(0);
         this.add(timeBetweenDropdown);
 
         timeBetweenTextLabelStrings= new String[]{"","Zeit um die sich Termine schneiden dürfen:","Zeit die zwischen zwei Terminen liegen muss:"};
 
         timeBetweenTextLabel = new JLabel(timeBetweenTextLabelStrings[0]);
         timeBetweenTextLabel.setFont(standardTextFont);
         timeBetweenTextLabel.setBounds(timeBetweenLabel.getX(), timeBetweenLabel.getY() + timeBetweenLabel.getHeight() + verticalSpace, textWidth, textHeight);
         //timeBetweenTextLabel.setVisible(false);
         this.add(timeBetweenTextLabel);
 
         timeIntersect = new JSpinner();
         timeIntersect.setModel(new SpinnerNumberModel(0,0,Integer.MAX_VALUE,5));
         timeIntersect.setBounds(timeBetweenTextLabel.getX() + timeBetweenTextLabel.getWidth() + 5 -20, timeBetweenTextLabel.getY() + 5, 43, 20);
         this.add(timeIntersect);
 
         timeBetweenIntersectText = new JLabel("min");
         timeBetweenIntersectText.setBackground(new Color(0, 0, 0, 0));
         timeBetweenIntersectText.setBounds(timeIntersect.getX() + timeIntersect.getWidth() + 1, timeIntersect.getY(), 30, 20);
         this.add(timeBetweenIntersectText);
 
 
         timeBuffer = new JSpinner();
         timeBuffer.setModel(new SpinnerNumberModel(0,0,Integer.MAX_VALUE,5));
         timeBuffer.setBounds(timeBetweenTextLabel.getX() + timeBetweenTextLabel.getWidth() + 5 -20, timeBetweenTextLabel.getY() + 5, 43, 20);
         this.add(timeBuffer);
 
         timeBetweenBufferText = new JLabel("min");
         timeBetweenBufferText.setBackground(new Color(0, 0, 0, 0));
         timeBetweenBufferText.setBounds(timeBuffer.getX() + timeBuffer.getWidth() + 1, timeBuffer.getY(), 30, 20);
         this.add(timeBetweenBufferText);
         toggleAdvanced();
 
         timeBetweenDropdown.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 refreshAdvanced();
                 timeBetweenTextLabel.setText(timeBetweenTextLabelStrings[timeBetweenDropdown.getSelectedIndex()]);
             }
         });
         // </ advanced settings >
 
         this.repaint();
     }
 }
