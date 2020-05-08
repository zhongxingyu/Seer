 package at.ac.tuwien.sepm.ui.entityViews;
 
 import at.ac.tuwien.sepm.entity.MetaLVA;
 import at.ac.tuwien.sepm.entity.Module;
 import at.ac.tuwien.sepm.service.*;
 import at.ac.tuwien.sepm.ui.SmallInfoPanel;
 import at.ac.tuwien.sepm.ui.StandardSimpleInsidePanel;
 import at.ac.tuwien.sepm.ui.UI;
 import at.ac.tuwien.sepm.ui.metaLva.MetaLVADisplayPanel;
 import at.ac.tuwien.sepm.ui.template.PanelTube;
 import at.ac.tuwien.sepm.ui.template.SelectItem;
 import at.ac.tuwien.sepm.ui.template.WideComboBox;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.swing.*;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Flo
  * Date: 04.06.13
  * Time: 13:57
  * To change this template use File | Settings | File Templates.
  */
 @UI
 public class ViewMetaLva extends StandardSimpleInsidePanel {
     private JLabel nrLabel;
     private JTextField nrInput;
 
     private JLabel nameLabel;
     private JTextField nameInput;
 
     private JLabel ectsLabel;
     private JSpinner ectsInput;
 
     private JLabel typeLabel;
     private JComboBox typeInput;
 
     private JLabel priorityLabel;
     private JSpinner priorityInput;
 
     private JLabel semestersOfferedLabel;
     private JComboBox semestersOfferedInput;
 
     private JLabel moduleLabel;
     private JComboBox moduleInput;
 
     private JLabel completedLabel;
     private JCheckBox completedInput;
 
     private JLabel precursorLabel;
     private MetaLVADisplayPanel precursorPanel;
 
     private JLabel addPrecursorLabel;
     private MetaLVADisplayPanel addPrecursorPanel;
     private JButton showAddPrecursorPanelButton;
     private JButton addPrecursorButton;
     
     private List<MetaLVA> precursor;
 
 
     private JButton save;
 
     private MetaLVAService metaLVAService;
     private ModuleService moduleService;
     private MetaLVA metaLVA;
 
     private Logger log = LogManager.getLogger(this.getClass().getSimpleName());
     private List<MetaLVA> allMetaLVAs;
 
     @Autowired
     public ViewMetaLva(MetaLVAService metaLVAService, ModuleService moduleService) {
         this.metaLVAService=metaLVAService;
         this.moduleService=moduleService;
         init();
         addImage();
         this.metaLVA = new MetaLVA();
         addTitle("Neue Lva");
         addReturnButton();
         addContent();
         addButtons();
         this.repaint();
         this.revalidate();
     }
 
     private void addButtons() {
         save = new JButton("Speichern");
         save.setFont(standardTextFont);
         save.setBounds((int)simpleWhiteSpace.getX()+(int)simpleWhiteSpace.getWidth()*1/3-170-120, (int)simpleWhiteSpace.getY() + (int)simpleWhiteSpace.getHeight()-60, 130,40);
         save.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 try {
                     metaLVA.setName(nameInput.getText());
                     metaLVA.setCompleted(completedInput.isSelected());
                     metaLVA.setECTS(((Number) ectsInput.getValue()).floatValue());
                     metaLVA.setModule(((ModuleSelectItem) moduleInput.getSelectedItem()).get().getId());
                     metaLVA.setNr(nrInput.getText());
                     metaLVA.setPriority(((Number) priorityInput.getValue()).floatValue());
                     metaLVA.setSemestersOffered((Semester) semestersOfferedInput.getSelectedItem());
                     metaLVA.setType((LvaType) typeInput.getSelectedItem());
                     metaLVA.setPrecursor(precursor);
 
                     if (metaLVA.getId() != null) {
                         if (metaLVAService.readById(metaLVA.getId()) != null)
                             metaLVAService.update(metaLVA);
                     } else {
                         metaLVAService.create(metaLVA);
                     }
                     PanelTube.backgroundPanel.viewSmallInfoText("Die LVA wurde gespeichert.", SmallInfoPanel.Success);
                     setVisible(false);
                     PanelTube.backgroundPanel.showLastComponent();
                 } catch (ServiceException e) {
                     log.error("MetaLvaEntity is invalid.");
                     PanelTube.backgroundPanel.viewSmallInfoText("Die Angaben sind ungültig.", SmallInfoPanel.Error);
                 }
             }
         });
         this.add(save);
     }
 
     private void addContent() {
         int smallSpace = 10;
         int bigSpace=20;
         
         int labelX = (int) (simpleWhiteSpace.getX()+bigSpace);
         int labelWidth = 180;
         
         int inputX = labelX+labelWidth+smallSpace;
         int inputWidth = 140;
         
         int oHeight = 25;
 
         int rightX = inputX+inputWidth+bigSpace*2;
         int rightWidth = (whiteSpace.x+whiteSpace.width-bigSpace) -rightX;
 
         int paneHeight = 150;
         int rightButtonWidth=200;
 
         nameLabel = new JLabel("Name:");
         nameLabel.setFont(standardTextFont);
         nameLabel.setBounds((int)simpleWhiteSpace.getX() + bigSpace,(int)simpleWhiteSpace.getY() + bigSpace,labelWidth,oHeight);
         this.add(nameLabel);
 
         nameInput = new JTextField();
         nameInput.setFont(standardTextFont);
         nameInput.setBounds(inputX, nameLabel.getY(), inputWidth,oHeight);
         nameInput.getDocument().addDocumentListener(new DocumentListener() {
             @Override
             public void insertUpdate(DocumentEvent e) {
                 changeTitle(nameInput.getText());
             }
 
             @Override
             public void removeUpdate(DocumentEvent e) {
                 changeTitle(nameInput.getText());
             }
 
             @Override
             public void changedUpdate(DocumentEvent e) {
                 changeTitle(nameInput.getText());
             }
         });
         this.add(nameInput);
         
         nrLabel = new JLabel("Lva Nummer:");
         nrLabel.setFont(standardTextFont);
         nrLabel.setBounds(labelX, nameLabel.getY() + nameLabel.getHeight() + smallSpace, labelWidth,oHeight);
         this.add(nrLabel);
 
         nrInput = new JTextField();
         nrInput.setFont(standardTextFont);
         nrInput.setBounds(inputX, nrLabel.getY(), inputWidth, oHeight);
         this.add(nrInput);
 
         ectsLabel = new JLabel("ECTs:");
         ectsLabel.setFont(standardTextFont);
         ectsLabel.setBounds(labelX, nrLabel.getY() + nrLabel.getHeight() + smallSpace, labelWidth,oHeight);
         this.add(ectsLabel);
 
         ectsInput = new JSpinner();
         ectsInput.setModel(new SpinnerNumberModel(0., 0., 30., 0.1));
         ectsInput.setEditor(new JSpinner.NumberEditor(ectsInput, "0.#"));
         ectsInput.setFont(standardTextFont);
         ectsInput.setBounds(inputX, ectsLabel.getY(), inputWidth, oHeight);
         this.add(ectsInput);
 
         typeLabel = new JLabel("Typ:");
         typeLabel.setFont(standardTextFont);
         typeLabel.setBounds(labelX, ectsLabel.getY() + ectsLabel.getHeight() + smallSpace, labelWidth,oHeight);
         this.add(typeLabel);
 
         typeInput = new WideComboBox();
         for (LvaType t : LvaType.values()) {
             typeInput.addItem(t);
         }
         typeInput.setFont(standardTextFont);
         typeInput.setBounds(inputX, typeLabel.getY(), inputWidth, oHeight);
         this.add(typeInput);
 
         semestersOfferedLabel = new JLabel("angebotene Semester:");
         semestersOfferedLabel.setFont(standardTextFont);
         semestersOfferedLabel.setBounds(labelX, typeLabel.getY() + typeLabel.getHeight() + smallSpace, labelWidth,oHeight);
         this.add(semestersOfferedLabel);
 
         semestersOfferedInput = new WideComboBox();
         for(Semester s : Semester.values())
             semestersOfferedInput.addItem(s);
         semestersOfferedInput.setFont(standardTextFont);
         semestersOfferedInput.setBounds(inputX, semestersOfferedLabel.getY(), inputWidth, oHeight);
         this.add(semestersOfferedInput);
 
         moduleLabel = new JLabel("Gehört zu Modul:");
         moduleLabel.setFont(standardTextFont);
         moduleLabel.setBounds(labelX, semestersOfferedLabel.getY() + semestersOfferedLabel.getHeight() + smallSpace, labelWidth,oHeight);
         this.add(moduleLabel);
 
         moduleInput = new WideComboBox();
         try {
             for (Module m :  moduleService.readAll())
                 moduleInput.addItem(new ModuleSelectItem(m));
         } catch (ServiceException e) {
             log.error("Exception: " +e.getMessage());
         }
         moduleInput.setFont(standardTextFont);
         moduleInput.setBounds(inputX, moduleLabel.getY(), inputWidth, oHeight);
         this.add(moduleInput);
 
         priorityLabel = new JLabel("Priorität:");
         priorityLabel.setFont(standardTextFont);
         priorityLabel.setBounds(labelX, moduleLabel.getY() + moduleLabel.getHeight() + smallSpace, labelWidth,oHeight);
         this.add(priorityLabel);
 
         priorityInput = new JSpinner();
         priorityInput.setModel(new SpinnerNumberModel(5., 0., 10., 0.5));
         priorityInput.setEditor(new JSpinner.NumberEditor(priorityInput, "0.#"));
         priorityInput.setFont(standardTextFont);
         priorityInput.setBounds(inputX, priorityLabel.getY(), inputWidth, oHeight);
         priorityInput.setValue(5);
         this.add(priorityInput);
 
         completedLabel = new JLabel("Abgeschlossen:");
         completedLabel.setFont(standardTextFont);
         completedLabel.setBounds(labelX, priorityLabel.getY() + priorityLabel.getHeight() + smallSpace, labelWidth,oHeight);
         this.add(completedLabel);
 
         completedInput = new JCheckBox();
         completedInput.addChangeListener(dONTFUCKINGBUGSWINGListener());
         completedInput.setBackground(new Color(0, 0, 0, 0));
         completedInput.setBounds(inputX, completedLabel.getY(), 20, 25);
         this.add(completedInput);
 
         precursorLabel = new JLabel("Vorgänger:");
         precursorLabel.setFont(standardTextFont);
         precursorLabel.setBounds(rightX, simpleWhiteSpace.y+bigSpace, rightWidth, oHeight);
         this.add(precursorLabel);
 
         precursorPanel = new MetaLVADisplayPanel(new ArrayList<MetaLVA>(0),rightWidth,paneHeight);
         precursorPanel.setBounds(rightX, precursorLabel.getY() + precursorLabel.getHeight() + smallSpace, rightWidth, paneHeight);
         this.add(precursorPanel);
 
         addPrecursorLabel = new JLabel("Vorgänger hinzufügen:");
         addPrecursorLabel.setFont(standardTextFont);
         addPrecursorLabel.setBounds(rightX, precursorPanel.getY()+precursorPanel.getHeight()+smallSpace, rightWidth, oHeight);
         this.add(addPrecursorLabel);
         
         showAddPrecursorPanelButton = new JButton("Vorgänger hinzufügen");
         showAddPrecursorPanelButton.setFont(standardButtonFont);
 
         showAddPrecursorPanelButton.setBounds(rightX, addPrecursorLabel.getY() + addPrecursorLabel.getHeight() + smallSpace, rightButtonWidth, oHeight);
         showAddPrecursorPanelButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 showAddPrecursorPanelButton.setVisible(false);
                 addPrecursorPanel.setVisible(true);
                 addPrecursorButton.setVisible(true);
             }
         });
         this.add(showAddPrecursorPanelButton);
 
 
         addPrecursorPanel = new MetaLVADisplayPanel(new ArrayList<MetaLVA>(0),rightWidth,paneHeight);
         addPrecursorPanel.setBounds(rightX, addPrecursorLabel.getY() + addPrecursorLabel.getHeight() + smallSpace, rightWidth, paneHeight);
         this.add(addPrecursorPanel);
         addPrecursorPanel.setVisible(false);
 
         addPrecursorButton = new JButton("hinzufügen");
         addPrecursorButton.setFont(standardButtonFont);
         addPrecursorButton.setBounds(rightX, addPrecursorPanel.getY() + addPrecursorPanel.getHeight() + smallSpace, rightButtonWidth, oHeight);
         addPrecursorButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 showAddPrecursorPanelButton.setVisible(true);
                 addPrecursorPanel.setVisible(false);
                 addPrecursorButton.setVisible(false);
                 MetaLVA toAdd = addPrecursorPanel.getSelectedMetaLVA();
                 if(toAdd!=null){
                     precursor.add(toAdd);
                     precursorPanel.refresh(precursor);
                     allMetaLVAs.remove(toAdd);
                     addPrecursorPanel.refresh(allMetaLVAs);
                 }else{
                     PanelTube.backgroundPanel.viewSmallInfoText("keine LVA ausgewählt.", SmallInfoPanel.Warning);
                 }
 
             }
         });
         this.add(addPrecursorButton);
         addPrecursorButton.setVisible(false);
 
     }
 
     public void setMetaLva(MetaLVA metaLVA) {
         allMetaLVAs=new ArrayList<MetaLVA>(0);
         try {
             allMetaLVAs = metaLVAService.readAll();
             Collections.sort(allMetaLVAs,MetaLVA.getAlphabeticalNameComparator());
         } catch (ServiceException e) {
             e.printStackTrace();
             log.error(e);
             PanelTube.backgroundPanel.viewSmallInfoText("Es ist ein Fehler beim Lesen der Datenbank aufgetreten", SmallInfoPanel.Error);
         }
         if (metaLVA == null) {
             this.metaLVA = new MetaLVA();
             changeTitle("Neue LVA");
             nameInput.setText("Neue LVA");
             nrInput.setText("");
             ectsInput.setValue(0);
             typeInput.setSelectedIndex(0);
             priorityInput.setValue(5);
             semestersOfferedInput.setSelectedIndex(0);
             moduleInput.setSelectedIndex(0);
             completedInput.setSelected(false);
             precursorPanel.refresh(new ArrayList<MetaLVA>(0));
             precursor=new ArrayList<MetaLVA>(0);
         } else {
             this.metaLVA=metaLVA;
             changeTitle(metaLVA.getName());
             nameInput.setText(metaLVA.getName());
             nrInput.setText(metaLVA.getNr());
             ectsInput.setValue(metaLVA.getECTS());
             typeInput.setSelectedItem(metaLVA.getType());
             priorityInput.setValue(metaLVA.getPriority());
             semestersOfferedInput.setSelectedItem(metaLVA.getSemestersOffered());
             for(int i = 0; i < moduleInput.getModel().getSize(); i++)
                 if (((ModuleSelectItem) moduleInput.getItemAt(i)).get().getId() == metaLVA.getModule()) {
                     moduleInput.setSelectedIndex(i);
                     break;
                 }
             completedInput.setSelected(metaLVA.isCompleted());
             precursorPanel.refresh(metaLVA.getPrecursor());
             precursor = metaLVA.getPrecursor();
         }
         ArrayList<MetaLVA> toRemove = new ArrayList<MetaLVA>();
         for(MetaLVA m1:allMetaLVAs){
             for(MetaLVA m2:precursor){
                 if(m1.getId()==m2.getId()){
                     toRemove.add(m1);
                 }
             }
         }
         allMetaLVAs.removeAll(toRemove);
         addPrecursorPanel.refresh(allMetaLVAs);
         precursorPanel.refresh(precursor);
     }
 
     private static class ModuleSelectItem extends SelectItem<Module> {
         ModuleSelectItem(Module item) {
             super(item);
         }
 
         @Override
         public String toString() {
             return item.getName();
         }
     }
 }
