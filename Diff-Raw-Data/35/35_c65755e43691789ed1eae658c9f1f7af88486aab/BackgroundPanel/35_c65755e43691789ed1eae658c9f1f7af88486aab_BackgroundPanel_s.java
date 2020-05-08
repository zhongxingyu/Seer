 package at.ac.tuwien.sepm.ui;
 
 import at.ac.tuwien.sepm.entity.*;
 import at.ac.tuwien.sepm.service.PropertyService;
 import at.ac.tuwien.sepm.service.RoomFinderService;
 import at.ac.tuwien.sepm.service.ServiceException;
 import at.ac.tuwien.sepm.ui.entityViews.*;
 import at.ac.tuwien.sepm.ui.startUp.ViewStartUp;
 import at.ac.tuwien.sepm.ui.template.PanelTube;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 @UI
 public class BackgroundPanel extends JPanel {
     private JButton properties;
     private JButton tab1;
     private JButton tab2;
     private JButton tab3;
     //private JButton viewStartupButton;
 
     private StandardInsidePanel calPanel;
     private StandardInsidePanel studPanel;
     private StandardInsidePanel propsPanel;
     private StandardInsidePanel lehrPanel;
     private ViewMerge viewMerge;
     private ViewDate viewDate;
     private ViewDeadline viewDeadline;
     private ViewLvaDate viewLVAdate;
     private ViewTODO viewTodo;
     private ViewLva viewLva;
     private ViewMetaLva viewMetaLva;
     private ViewModule viewModule;
     private ViewStartUp viewStartup;
     private Image image;
 
     private StandardInsidePanel lastComponent;
     private StandardSimpleInsidePanel lastSimpleComponent;
 
     private int lastImage;
     private SmallInfoPanel smallInfoPanel;
     private BiggerInfoPanel biggerInfoPanel;
     private ArrayList<JButton> tabs;
 
     private Timer hideInfoTimer;
 
     @Autowired
     RoomFinderService roomFinderService;
 
     private Logger log = LogManager.getLogger(this.getClass().getSimpleName());
 
     @Autowired
     public BackgroundPanel(CalendarPanel calPanel, StudiesPanel studPanel, LehrangebotPanel lehrPanel, SettingsPanel propsPanel, ViewDate viewDate,
                            ViewLvaDate viewLVAdate, ViewTODO viewTodo, ViewDeadline viewDeadline, ViewLva viewLva, ViewMetaLva viewMetaLva,
                            ViewModule viewModule, SmallInfoPanel smallInfoPanel, BiggerInfoPanel biggerInfoPanel, ViewMerge viewMerge,ViewStartUp viewStartUp,
                            PropertyService propertyService) {
         this.setLayout(null);
         PanelTube.backgroundPanel=this;
         this.viewMerge = viewMerge;
         this.viewMetaLva=viewMetaLva;
         this.viewModule = viewModule;
         this.smallInfoPanel=smallInfoPanel;
         this.viewLva=viewLva;
         this.lehrPanel=lehrPanel;
         this.calPanel = calPanel;
         this.studPanel = studPanel;
         this.propsPanel = propsPanel;
         this.viewDate = viewDate;
         this.viewLVAdate=viewLVAdate;
         this.viewTodo=viewTodo;
         this.viewDeadline=viewDeadline;
         this.biggerInfoPanel=biggerInfoPanel;
         this.viewStartup = viewStartUp;
         changeImage(1);
         createPropertiesButton();
         createTabButtons();
 
         if(Boolean.parseBoolean(propertyService.getProperty(PropertyService.FIRST_RUN,"true"))){
             //things, which happen on first startup
             viewStartup(true);
         }
        viewSmallInfoText("test1-blablablablablablablablablablablablablaaaaaaaaaaaaaaaaablablablabla\n" +
                "test3-blablablablablablablablablablablablablablablablabla\n" +
                "test4-blablablablablablablablablablablablablablablablabla\n" +
                "test5-blablablablablablablablablablablablablablablablabla\n" +
                "test6-blablablablablablablablablablablablablablablablabla\n" +
                "test7-blablablablablablablablablablablablablablablablabla\n" +
                "test8-blablablablablablablablablablablablablablablablabla\n", 1);
        log.info("Background Panel initialized.");
     }
 
     public void setControlsEnabled(boolean b){
         for(JButton button:tabs){
             button.setEnabled(b);
             if(b){
                 button.setCursor(new Cursor(Cursor.HAND_CURSOR));
             }else{
                 button.setCursor(Cursor.getDefaultCursor());
             }
         }
         properties.setEnabled(b);
     }
 
     //wenn neue entity erzeugt werden soll ->  viewDate(null)
     public void viewDate(DateEntity dateEntity, DateTime dateTime) {
         removeAddedPanels();
         viewDate.setDateEntity(dateEntity, dateTime);
         viewDate.setVisible(true);
         addSimplePanel(viewDate);
         this.revalidate();
         this.repaint();
     }
 
     public void viewMerge(List<MetaLVA> oldMetaLVAs, List<MetaLVA> newMetaLVAs) {
         removeAddedPanels();
         viewMerge.setIntersectingMetaLVAs(oldMetaLVAs, newMetaLVAs);
         viewMerge.setVisible(true);
         addSimplePanel(viewMerge);
         this.revalidate();
         this.repaint();
     }
 
     public void viewLvaDate(LvaDate lvaDate, DateTime dateTime) {
         removeAddedPanels();
         viewLVAdate.setLVADateEntity(lvaDate, dateTime);
         viewLVAdate.setVisible(true);
         addSimplePanel(viewLVAdate);
         this.revalidate();
         this.repaint();
     }
 
     public void viewTodo(Todo todo) {
         removeAddedPanels();
         viewTodo.setTodo(todo);
         viewTodo.setVisible(true);
         addSimplePanel(viewTodo);
         this.revalidate();
         this.repaint();
     }
 
     public void viewDeadline(LvaDate deadline) {
         removeAddedPanels();
         viewDeadline.setDeadline(deadline);
         viewDeadline.setVisible(true);
         addSimplePanel(viewDeadline);
         this.revalidate();
         this.repaint();
     }
 
     public void viewLva(LVA lva) {
         removeAddedPanels();
         viewLva.setLva(lva);
         viewLva.setVisible(true);
         addSimplePanel(viewLva);
         this.revalidate();
         this.repaint();
     }
 
     public void viewMetaLva(MetaLVA metalva) {
         removeAddedPanels();
         viewMetaLva.setMetaLva(metalva);
         viewMetaLva.setVisible(true);
         addSimplePanel(viewMetaLva);
         this.revalidate();
         this.repaint();
     }
     public void viewModule(Module module) {
         removeAddedPanels();
         viewModule.setModule(module);
         viewModule.setVisible(true);
         addSimplePanel(viewModule);
         this.revalidate();
         this.repaint();
     }
     public void viewStartup(boolean lock) {
         setControlsEnabled(!lock);
         removeAddedPanels();
         viewStartup.setVisible(true);
         viewStartup.jump(0);
         if(!lock){
             viewStartup.addReturnButton();
             changeImage(5);
         }else{
             viewStartup.removeReturnButton();
             changeImage(6);
         }
         this.add(viewStartup);
         this.revalidate();
         this.repaint();
     }
 
     /*
     *   String s = text im infopanel (max. 75 chars)
     *   int nmb = icon
     *   (1 = SmallInfoPanel.Error,
     *   2 = SmallInfoPanel.Info,
     *   3 = SmallInfoPanel.Warning
     *   4 = SmallInfoPanel.Success)
     */
     public void viewSmallInfoText(String s, int nmb) {
         smallInfoPanel.setVisible(true);
         smallInfoPanel.setInfoText(s, nmb);
         this.add(smallInfoPanel);
         this.revalidate();
         this.repaint();
         hideInfoText();
     }
 
     public void viewBiggerInfoText(String message) {
         biggerInfoPanel.setVisible(true);
         biggerInfoPanel.setInfoText(message);
         this.add(biggerInfoPanel);
 
         removeAddedPanels();
         if (lastSimpleComponent == null) {
             this.add(lastComponent);
         } else {
             this.add(lastSimpleComponent);
         }
 
         this.revalidate();
         this.repaint();
     }
 
     private void hideInfoText() {
         stopInfoTextTimer();
         hideInfoTimer = new Timer(5000, new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 smallInfoPanel.setVisible(false);
                 BackgroundPanel.this.remove(smallInfoPanel);
                 if (biggerInfoPanel.isVisible()) {
                     hideBiggerInfoText();
                 }
             }
         });
         hideInfoTimer.setRepeats(false);
         startInfoTextTimer();
     }
 
     public void hideBiggerInfoText() {
         biggerInfoPanel.setVisible(false);
         BackgroundPanel.this.remove(biggerInfoPanel);
     }
 
     public void stopInfoTextTimer() {
         if(hideInfoTimer!=null && hideInfoTimer.isRunning()){
             hideInfoTimer.stop();
         }
     }
 
     public void startInfoTextTimer() {
         if (!hideInfoTimer.isRunning()) {
             hideInfoTimer.start();
         }
     }
 
     @Override
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
         if (image != null) {
             g.drawImage(image, 0, 0, null);
         }
     }
 
     private void removeAddedPanels() {
         this.remove(propsPanel);
         this.remove(calPanel);
         this.remove(studPanel);
         this.remove(viewDate);
         this.remove(viewLVAdate);
         this.remove(viewTodo);
         this.remove(viewLva);
         this.remove(viewMetaLva);
         this.remove(viewModule);
         this.remove(viewMerge);
         this.remove(viewDeadline);
         this.remove(lehrPanel);
         this.remove(viewStartup);
     }
 
     private void addSimplePanel(StandardSimpleInsidePanel c) {
         this.add(c);
         lastSimpleComponent=c;
     }
 
     private void addPanel(StandardInsidePanel c) {
         this.add(c);
         lastComponent = c;
         lastSimpleComponent = null;
     }
 
     public void showLastComponent() {
         this.add(lastComponent);
         changeImage(lastImage);
         lastComponent.refresh();
     }
 
     private void changeImage(int nmb) {
         try{
             removeAddedPanels();
             switch(nmb) {
                 case 1:
                     image = ImageIO.read(ClassLoader.getSystemResource("img/cal.jpg"));
                     lastImage = 1;
                     calPanel.refresh();
                     this.addPanel(calPanel);
                     break;
                 case 2:
                     image = ImageIO.read(ClassLoader.getSystemResource("img/stud.jpg"));
                     lastImage = 2;
                     studPanel.refresh();
                     this.addPanel(studPanel);
                     break;
                 case 3:
                     image = ImageIO.read(ClassLoader.getSystemResource("img/lehr.jpg"));
                     lastImage = 3;
                     lehrPanel.refresh();
                     this.addPanel(lehrPanel);
                     break;
                 case 4:
                     image = ImageIO.read(ClassLoader.getSystemResource("img/settings.jpg"));
                     propsPanel.refresh();
                     break;
                 case 5:
                     image = ImageIO.read(ClassLoader.getSystemResource("img/settings.jpg"));//todo grafik für startup-wizard in der kein tab selektiert ist (die buttons sind noch intakt).
                     break;
                 case 6:
                     image = ImageIO.read(ClassLoader.getSystemResource("img/settings.jpg"));//todo grafik für startup-wizard in der alle tabs deaktiviert sind.
                     break;
                 default:
                     break;
             }
             this.repaint();
         } catch (IOException e) {
             log.error("Error: " + e.getMessage());
         }
     }
 
     private void createPropertiesButton() {
         properties = new JButton();
         properties.setBounds(1138,13,38,38);
         properties.setOpaque(false);
         properties.setContentAreaFilled(false);
         properties.setBorderPainted(false);
         properties.setCursor(new Cursor(Cursor.HAND_CURSOR));
         properties.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 removeAddedPanels();
                 changeImage(4);
                 propsPanel.setVisible(true);
                 BackgroundPanel.this.add(propsPanel);
                 BackgroundPanel.this.repaint();
                 BackgroundPanel.this.revalidate();
             }
         });
         this.add(properties);
     }
 
     private void createTabButtons() {
         tab1 = new JButton();
         tab2 = new JButton();
         tab3 = new JButton();
         //viewStartupButton = new JButton("S");
 
         tabs = new ArrayList<JButton>();
         tabs.add(tab1);
         tabs.add(tab2);
         tabs.add(tab3);
         //tabs.add(viewStartupButton);
 
         tab1.setBounds(12, 50, 55, 159);
         tab1.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 changeImage(1);
                 repaint();
                 revalidate();
             }
         });
 
         tab2.setBounds(12, 209, 55, 159);
         tab2.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 changeImage(2);
                 repaint();
                 revalidate();
             }
         });
 
         tab3.setBounds(12, 369, 55, 159);
         tab3.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 changeImage(3);
                 repaint();
                 revalidate();
             }
         });
 
 
         for (int i = 0; i < 3; i++) {
             tabs.get(i).setCursor(new Cursor(Cursor.HAND_CURSOR));
             tabs.get(i).setOpaque(false);
             tabs.get(i).setContentAreaFilled(false);
             tabs.get(i).setBorderPainted(false);
             this.add(tabs.get(i));
         }
     }
 
     public boolean openRoomBrowser(String room) {
         try {
             UIHelper.openURL(roomFinderService.getRoomURL(room));
         } catch (ServiceException e) {
             log.error("could not find room");
             return false;
         }
         return true;
     }
 
     public void refresh(){
     }
 }
