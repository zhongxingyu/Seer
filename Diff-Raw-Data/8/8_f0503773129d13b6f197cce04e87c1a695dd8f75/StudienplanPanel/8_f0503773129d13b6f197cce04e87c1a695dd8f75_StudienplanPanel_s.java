 package at.ac.tuwien.sepm.ui.lehrangebot;
 
 import at.ac.tuwien.sepm.entity.Curriculum;
 import at.ac.tuwien.sepm.entity.Module;
 import at.ac.tuwien.sepm.service.*;
 import at.ac.tuwien.sepm.ui.SmallInfoPanel;
 import at.ac.tuwien.sepm.ui.StandardInsidePanel;
 import at.ac.tuwien.sepm.ui.UI;
 import at.ac.tuwien.sepm.ui.template.PanelTube;
 import at.ac.tuwien.sepm.ui.template.WideComboBox;
 import net.miginfocom.swing.MigLayout;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.activation.MimetypesFileTypeMap;
 import javax.annotation.PostConstruct;
 import javax.swing.*;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.table.DefaultTableModel;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.File;
 import java.util.*;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Flo
  * Date: 01.06.13
  * Time: 19:23
  * To change this template use File | Settings | File Templates.
  */
 
 @UI
 public class StudienplanPanel extends StandardInsidePanel {
     protected CreateCurriculumService curriculumService;
     protected ModuleService moduleService;
 
     private final int MAX_INFO_LENGTH = 18;
 
     private JPanel panel;
     private JButton baddcurr;
     private JButton bcreate;
     private JButton bimport;
     private JTable tmodule;
    private DefaultTableModel mmodule;
     private JScrollPane spane;
     private WideComboBox ccurr;
     private DefaultComboBoxModel<CurriculumComboBoxItem> mcurr;
     private JFileChooser jfc;
     private List<Module> optionalModules;
     private List<Module> requiredModules;
     private Map<String, Module> modules;
     private CurriculumDisplayPanel curriculumDisplayPanel;
 
     private static Logger log = LogManager.getLogger(StudienplanPanel.class.getSimpleName());
 
     @Autowired
     public StudienplanPanel(CreateCurriculumService curriculumService, ModuleService moduleService) {
         PanelTube.studienplanPanel=this;
         this.curriculumService = curriculumService;
         this.moduleService=moduleService;
         this.setLayout(new GridLayout());
         this.setOpaque(false);
         loadFonts();
         setBounds((int)startCoordinateOfWhiteSpace.getX(), (int) startCoordinateOfWhiteSpace.getY(),(int) whiteSpace.getWidth(),(int) whiteSpace.getHeight());
 
         initPanel();
         initFileChooser();
         initAddCurriculumButton();
         initCurriculumComboBox();
         initButtonImport();
         initButtonCreate();
         initCurriculumComboBoxActionListener();
         refreshModuleMap();
         initCurriculumDisplayPanel();
         setBackground(Color.RED);
         setVisible(true);
         revalidate();
         repaint();
     }
 
     @PostConstruct
     public void placeComponents() {
         panel.setLayout(new MigLayout("nogrid, fill"));
         panel.add(ccurr);
         panel.add(baddcurr);
         panel.add(bimport, "wrap");
         //panel.add(spane, "grow, span, wrap");
         panel.add(curriculumDisplayPanel, "wrap");
         panel.add(bcreate);
         this.add(panel);
         panel.setOpaque(false);
         panel.revalidate();
         panel.repaint();
     }
 
     private void initCurriculumDisplayPanel () {
         log.info("StudienplanPanel.initCurriculumDisplayPanel() called ... ");
         refreshModuleMap();
         refreshCurriculumComboBox();
         curriculumDisplayPanel = new CurriculumDisplayPanel(new ArrayList<>(modules.values()), (int)whiteSpace.getWidth(), (int)whiteSpace.getHeight()-baddcurr.getHeight()-bcreate.getHeight());
     }
 
     private void refreshModuleMap() {
         modules = new HashMap<>();
         List<Module> moduleList = new ArrayList<>();
         try {
             moduleList = moduleService.readAll();
         } catch (ServiceException e) {
             if(PanelTube.backgroundPanel != null) {
                 PanelTube.backgroundPanel.viewSmallInfoText(e.getMessage(), SmallInfoPanel.Error);
             }
         }
 
         for(Module m : moduleList) {
             m.setTempBooleanContained(false);
             modules.put(m.getName(), m);
         }
 
     }
 
     private void initFileChooser() {
         jfc = new JFileChooser();
         jfc.setFileFilter(new FileFilter() {
             @Override
             public boolean accept(File f) {
                 if (f == null) {
                     return false;
                 }
 
                 MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
                 mimetypesFileTypeMap.addMimeTypes("application/pdf pdf PDF");
                 String mimeType = mimetypesFileTypeMap.getContentType(f);
 
                 if (f.isDirectory()) {
                     return true;
                 } else if (mimeType.equals("application/pdf")) {
                     return true;
                 }
 
                 return false;
             }
 
             @Override
             public String getDescription() {
                 if (System.getProperty("user.language").equals("de")) {
                     return "pdf Dateien (*.pdf)";
                 }
                 return "pdf files (*.pdf)";
             }
         });
         jfc.setFont(standardTextFont);
     }
 
     private void initButtonImport() {
         bimport = new JButton();
         bimport.setText("Studienplan importieren");
         bimport.setFont(standardButtonFont);
         bimport.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if(jfc.showOpenDialog(StudienplanPanel.this) == JFileChooser.APPROVE_OPTION) {
                     PanelTube.backgroundPanel.viewSmallInfoText("Die Daten werden geladen ... ", SmallInfoPanel.Info);
                     File file = jfc.getSelectedFile();
                     try {
                         log.info("pdf path: " + file.getPath());
                         optionalModules = moduleService.getOptionalModules(file.getPath());
                         requiredModules = moduleService.getRequiredModules(file.getPath());
                     } catch (ServiceException e1) {
                         PanelTube.backgroundPanel.viewSmallInfoText(e1.getMessage(), SmallInfoPanel.Error);
                         return;
                     }
 
                     int modulesNotFound = 0;
                     String moduleNotFoundNames = "";
 
                     refreshModuleMap();
                     for(Module m : optionalModules) {
                         Module temp = modules.get("Modul " + m.getName());
                         if (temp != null) {
                             temp.setTempBooleanContained(true);
                             temp.setTempBooleanOptional(true);
                             temp.setCompleteall(calcCompleteAll(m.getName()));
                         } else {
                             moduleNotFoundNames = addToModuleNotFoundString(modulesNotFound, moduleNotFoundNames, m.getName());
                             modulesNotFound++;
                             log.info("optional module not found: " + m.getName());
                         }
                     }
 
                     for(Module m : requiredModules) {
                         Module temp = modules.get("Modul " + m.getName());
                         if (temp != null) {
                             temp.setTempBooleanContained(true);
                             temp.setTempBooleanOptional(false);
                             temp.setCompleteall(calcCompleteAll(m.getName()));
                         } else {
                             moduleNotFoundNames = addToModuleNotFoundString(modulesNotFound, moduleNotFoundNames, m.getName());
                             modulesNotFound++;
                             log.info("required module not found: " + "Modul " + m.getName());
                         }
                     }
 
                     curriculumDisplayPanel.refresh(new ArrayList<Module>(modules.values()));
                     curriculumDisplayPanel.changeStateOfContainedComboBox("selected");
                     if (modulesNotFound == 0) {
                         PanelTube.backgroundPanel.viewSmallInfoText("Die Daten sind geladen.", SmallInfoPanel.Success);
                     } else {
                         String moduleString = " Module konnten nicht gespeichert werden, da sie nicht exisitieren:\n";
                         if (modulesNotFound == 1) {
                             moduleString = " Modul konnte nicht gespeichert werden, da es nicht exisitiert:\n";
                         }
                         PanelTube.backgroundPanel.viewSmallInfoText(modulesNotFound + moduleString + moduleNotFoundNames + "   ", SmallInfoPanel.Error);
                         log.info(modulesNotFound + " modules not stored yet ... ");
                     }
                 } else {
                     return;
                 }
             }
         });
     }
 
     private boolean calcCompleteAll (String name) {
         if (name == null) {
             return true;
         }
         return !(name.contains("Bachelorarbeit") || name.contains("Freie Wahl"));
     }
 
     private String addToModuleNotFoundString (int modulesNotFound, String s, String name) {
         if (modulesNotFound==0) {
             s += " '" + name + "'";
         } else {
             s += "\n'" + name + "'";
         }
 
         return s;
     }
 
     public void initButtonCreate() {
         bcreate = new JButton();
         bcreate.setText("Speichern");
         bcreate.setFont(standardButtonFont);
         bcreate.addActionListener(new ActionListener() {
             @Override
             @Transactional
             public void actionPerformed(ActionEvent e) {
                 try {
                     if (ccurr.getSelectedItem() != null && ((CurriculumComboBoxItem) ccurr.getSelectedItem()).get() != null) {
                         int cid = ((CurriculumComboBoxItem) ccurr.getSelectedItem()).get().getId();
                         HashMap<Module, Boolean> mMap = curriculumService.readModuleByCurriculum(cid);
                         Set<Module> mSet = mMap.keySet();
                         HashMap<Integer, Module> mids = new HashMap<>();
 
                         for (Module m : mSet) {
                             mids.put(m.getId(), m);
                         }
 
                         List<Module> allModules = curriculumDisplayPanel.getAllModules();
                         for (Module m : allModules) {
                             int mid = m.getId();
                             if (m.getTempBooleanContained() == true) { // the module has been chosen
                                 if (mids.get(mid) == null) { // the module is new added to the curriculum
                                     curriculumService.addModuleToCurriculum(mid, cid, !m.getTempBooleanOptional());
                                 } else { // the module is already in the curriculum
                                     if (mMap.get(mids.get(mid)).booleanValue() != m.getTempBooleanOptional()) { // update is necessary
                                         curriculumService.deleteModuleFromCurriculum(mid, cid);
                                         curriculumService.addModuleToCurriculum(mid, cid, !m.getTempBooleanOptional());
                                     }
                                 }
                             } else { // the module has not been chosen
                                 if (mids.get(mid) != null) { // the module was in the curriculum and has to be deleted
                                     curriculumService.deleteModuleFromCurriculum(mid, cid);
                                 }
                             }
                             Module module = new Module();
                             module.setId(m.getId());
                             module.setCompleteall(m.isCompleteall());
                             moduleService.update(module);
                         }
                     }
                 } catch (ServiceException e1) {
                     log.error("Error: " + e1.getMessage());
                     return;
                 }
                 fillTable();
                 PanelTube.backgroundPanel.viewSmallInfoText("Das Studium ist erfolgreich gespeichert.", SmallInfoPanel.Success);
             }
         });
     }
 
     public void initCurriculumComboBox() {
         mcurr = new DefaultComboBoxModel<>();
         ccurr = new WideComboBox(mcurr);
         ccurr.setFont(standardButtonFont);
         mcurr.addElement(new CurriculumComboBoxItem(null));
         refreshCurriculumComboBox();
     }
 
     public void initCurriculumComboBoxActionListener() {
         ccurr.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent e) {
                 if (ccurr.getSelectedItem() != null && ccurr.getSelectedItem() instanceof CurriculumComboBoxItem && ((CurriculumComboBoxItem)ccurr.getSelectedItem()).get() != null) {
                     PanelTube.backgroundPanel.viewSmallInfoText("Das Studium wird geladen ... ", SmallInfoPanel.Info);
                     fillTable();
                     PanelTube.backgroundPanel.viewSmallInfoText("Das Studium wurde geladen.", SmallInfoPanel.Success);
                 } else if (curriculumDisplayPanel != null) {
                     curriculumDisplayPanel.changeStateOfContainedComboBox("disabled");
                 }
             }
         });
     }
 
     public void refreshCurriculumComboBox() {
         List<Curriculum> l;
 
         try {
             l = curriculumService.readAllCurriculum();
         } catch (ServiceException e) {
             log.error("Error: " + e.getMessage());
             return;
         }
 
         mcurr.removeAllElements();
         mcurr.addElement(new CurriculumComboBoxItem(null));
 
         for(Curriculum c : l) {
             mcurr.addElement(new CurriculumComboBoxItem(c));
         }
     }
 
     public void initPanel() {
         panel = new JPanel(new MigLayout("debug"));
         panel.setBounds(whiteSpace);
         panel.setBackground(Color.WHITE);
     }
 
     public void initAddCurriculumButton () {
         baddcurr = new JButton();
         baddcurr.setText("Studium anlegen");
         baddcurr.setFont(standardButtonFont);
         baddcurr.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 PanelTube.backgroundPanel.viewCurriculum();
             }
         });
     }
 
     public void fillTable() {
         log.info("StudienplanPanel.fillTable() called ... ");
         if(ccurr.getSelectedItem()!=null && ccurr.getSelectedItem() instanceof CurriculumComboBoxItem && ((CurriculumComboBoxItem)ccurr.getSelectedItem()).get() != null){
             List<Module> list;
             try {
                 list = curriculumService.readAllModules();
             } catch (ServiceException e) {
                 PanelTube.backgroundPanel.viewSmallInfoText(e.getMessage(), SmallInfoPanel.Error);
                 log.error("Error: " + e.getMessage());
                 return;
             }
 
             log.info("StudienplanPanel.fillTable(): " + list.size() + " modules loaded  ... ");
 
             int cid = ((CurriculumComboBoxItem)ccurr.getSelectedItem()).get().getId();
             HashMap<Module, Boolean> mMap;
 
             try {
                 mMap = curriculumService.readModuleByCurriculum(cid);
             } catch (ServiceException e) {
                 PanelTube.backgroundPanel.viewSmallInfoText(e.getMessage(), SmallInfoPanel.Error);
                 log.error("Error: " + e.getMessage());
                 return;
             }
 
             Set<Module> mSet = mMap.keySet();
             HashMap<Integer, Module> midsMap = new HashMap<>();
             Set<Integer> mids = new HashSet<>();
 
             for(Module m : mSet) {
                 mids.add(m.getId());
                 midsMap.put(m.getId(), m);
             }
 
            clearTable();
             modules = new HashMap<>(list.size());
             for(Module m : list) {
                 if(mids.contains(m.getId())) {
                     m.setTempBooleanContained(true);
                     m.setTempBooleanOptional(!mMap.get(midsMap.get(m.getId())));
                 } else {
                     m.setTempBooleanContained(false);
                     m.setTempBooleanOptional(false);
                 }
                 modules.put(m.getName(), m);
             }
             curriculumDisplayPanel.refresh(new ArrayList<Module>(modules.values()));
             if (mMap.size() > 0) {
                 curriculumDisplayPanel.changeStateOfContainedComboBox("selected");
             } else {
                 curriculumDisplayPanel.changeStateOfContainedComboBox("disabled");
             }
         } else if (ccurr.getSelectedItem()!=null && ccurr.getSelectedItem() instanceof CurriculumComboBoxItem && ((CurriculumComboBoxItem)ccurr.getSelectedItem()).get() == null) {
             refreshModuleMap();
             curriculumDisplayPanel.refresh(new ArrayList<>(modules.values()));
         }
     }
 
    private void clearTable() {
        mmodule.setRowCount(0);
    }

     private class CurriculumComboBoxItem {
         private Curriculum c;
 
         public CurriculumComboBoxItem(Curriculum c) {
             this.c = c;
         }
 
         public Curriculum get () {
             return c;
         }
         public String toString() {
             if (c == null)
                 return "Studium auswählen";
             return c.getStudyNumber();
         }
 
     }
 }
