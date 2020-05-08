 /*
  * This file is part of the aidGer project.
  *
  * Copyright (C) 2010-2011 The aidGer Team
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.aidger.view.forms;
 
 import static de.aidger.utils.Translation._;
 
 import java.awt.GridBagConstraints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.SpinnerDateModel;
 import javax.swing.SwingUtilities;
 
 import de.aidger.model.Runtime;
 import de.aidger.model.models.Assistant;
 import de.aidger.model.models.Contract;
 import de.aidger.model.models.CostUnit;
 import de.aidger.model.models.Course;
 import de.aidger.model.models.Employment;
 import de.aidger.utils.DataXMLManager;
 import de.aidger.view.UI;
 import de.aidger.view.forms.HourlyWageEditorForm.Qualification;
 import de.aidger.view.models.ComboBoxModel;
 import de.aidger.view.models.GenericListModel;
 import de.aidger.view.models.UIAssistant;
 import de.aidger.view.models.UIContract;
 import de.aidger.view.models.UICostUnit;
 import de.aidger.view.models.UICourse;
 import de.aidger.view.tabs.EditorTab;
 import de.aidger.view.tabs.Tab;
 import de.aidger.view.tabs.ViewerTab.DataType;
 import de.aidger.view.utils.AutoCompletion;
 import de.aidger.view.utils.InputPatternFilter;
 import de.aidger.view.utils.NumberFormat;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import de.unistuttgart.iste.se.adohive.model.IAssistant;
 import de.unistuttgart.iste.se.adohive.model.IContract;
 import de.unistuttgart.iste.se.adohive.model.ICourse;
 
 /**
  * A form used for editing / creating new employments.
  * 
  * @author aidGer Team
  */
 @SuppressWarnings("serial")
 public class EmploymentEditorForm extends JPanel {
 
     /**
      * A flag whether the form is in edit mode.
      */
     private boolean editMode = false;
 
     /**
      * The data XML manager.
      */
     private final DataXMLManager dataManager = Runtime.getInstance()
         .getDataXMLManager();
 
     /**
      * Constructs an employment editor form.
      * 
      * @param employment
      *            The employment that will be edited
      * @param listModels
      *            the list models of the parent editor tab
      */
     @SuppressWarnings("unchecked")
     public EmploymentEditorForm(Employment employment,
             List<GenericListModel> listModels) {
         if (employment != null) {
             editMode = true;
         }
 
         initComponents();
 
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 cmbAssistant.requestFocusInWindow();
             }
         });
 
         btnContractAdd.setIcon(new ImageIcon(getClass().getResource(
             "/de/aidger/res/icons/plus-small.png")));
         btnContractAdd.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 Tab current = UI.getInstance().getCurrentTab();
 
                 EditorTab next = new EditorTab(DataType.Contract);
 
                 if (cmbAssistant.getSelectedItem() != null) {
                     ((ContractEditorForm) next.getEditorForm())
                         .setAssistant(((Assistant) cmbAssistant
                             .getSelectedItem()));
                 }
 
                 current.markAsPredecessor();
                 next.markAsNoPredecessor();
 
                 UI.getInstance().replaceCurrentTab(next);
 
                 current.markAsNoPredecessor();
             }
         });
 
         AutoCompletion.enable(cmbAssistant);
         AutoCompletion.enable(cmbCourse);
         AutoCompletion.enable(cmbContract);
         AutoCompletion.enable(cmbCostUnit);
 
         cmbAssistant.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 refreshQualification((Assistant) cmbAssistant.getSelectedItem());
             }
         });
 
         try {
             List<IAssistant> assistants = (new Assistant()).getAll();
 
             ComboBoxModel cmbAssistantModel = new ComboBoxModel(
                 DataType.Assistant);
 
             for (IAssistant a : assistants) {
                 Assistant assistant = new UIAssistant(a);
 
                 cmbAssistantModel.addElement(assistant);
 
                 if (employment != null
                         && assistant.getId()
                             .equals(employment.getAssistantId())) {
                     cmbAssistantModel.setSelectedItem(assistant);
                 }
             }
 
             List<ICourse> courses = (new Course()).getAll();
 
             ComboBoxModel cmbCourseModel = new ComboBoxModel(DataType.Course);
 
             for (ICourse c : courses) {
                 Course course = new UICourse(c);
 
                 cmbCourseModel.addElement(course);
 
                 if (employment != null
                         && course.getId().equals(employment.getCourseId())) {
                     cmbCourseModel.setSelectedItem(course);
                 }
             }
 
             ComboBoxModel cmbFundsModel = new ComboBoxModel(
                 DataType.FinancialCategory);
 
             cmbAssistant.setModel(cmbAssistantModel);
             cmbCourse.setModel(cmbCourseModel);
 
             cmbCostUnit.setModel(cmbFundsModel);
 
             listModels.add(cmbAssistantModel);
             listModels.add(cmbCourseModel);
 
             if (cmbAssistant.getSelectedItem() != null) {
                 refreshQualification((Assistant) cmbAssistant.getSelectedItem());
             }
 
             addNewDate();
 
             if (editMode) {
                 CostUnit costUnit = dataManager.fromTokenDB(employment
                     .getFunds());
                 cmbFunds.setSelectedItem(costUnit == null ? employment
                     .getFunds() : costUnit);
 
                 cmbCostUnit.setSelectedItem(UICostUnit.valueOf(employment
                     .getCostUnit()));
                 cmbQualification.setSelectedItem(Qualification
                     .valueOf(employment.getQualification()));
                 txtRemark.setText(employment.getRemark());
 
                 DateLine dl = dateLines.get(0);
 
                 Calendar cal = Calendar.getInstance();
                 cal.set(Calendar.MONTH, employment.getMonth() - 1);
                 cal.set(Calendar.YEAR, employment.getYear());
                 dl.spDate.setValue(cal.getTime());
 
                 dl.txtHourCount.setText(NumberFormat.getInstance().format(
                     employment.getHourCount()));
             }
 
             List<Contract> contracts;
             if (editMode) {
                 contracts = (new Contract())
                     .getContracts((Assistant) cmbAssistant.getSelectedItem());
             } else {
                contracts = (new Contract()).getAll();
             }
 
             ComboBoxModel cmbContractModel = new ComboBoxModel(
                 DataType.Contract);
 
             if (!editMode) {
                 cmbContractModel.addElement(new UIContract());
             }
 
             for (IContract c : contracts) {
                 Contract contract = new UIContract(c);
 
                 cmbContractModel.addElement(contract);
 
                 if (employment != null
                         && contract.getId() == employment.getContractId()) {
                     cmbContractModel.setSelectedItem(contract);
                 }
             }
 
             cmbAssistant.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     try {
                         List<Contract> contracts = (new Contract())
                             .getContracts((Assistant) cmbAssistant
                                 .getSelectedItem());
 
                         ComboBoxModel cmbContractModel = (ComboBoxModel) cmbContract
                             .getModel();
 
                         cmbContractModel.removeAllElements();
 
                         cmbContractModel.addElement(new UIContract());
 
                         for (Contract c : contracts) {
                             Contract contract = new UIContract(c);
 
                             cmbContractModel.addElement(contract);
                         }
                     } catch (AdoHiveException e2) {
                     }
                 }
             });
 
             cmbContract.setModel(cmbContractModel);
             listModels.add(cmbContractModel);
         } catch (AdoHiveException e) {
         }
 
         cmbFunds.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 refreshCostUnit();
             }
         });
 
         refreshCostUnit();
     }
 
     /**
      * Refreshs the cost unit field.
      */
     private void refreshCostUnit() {
         CostUnit costUnit = (CostUnit) cmbFunds.getSelectedItem();
 
         cmbCostUnit.setSelectedItem(costUnit.getCostUnit());
     }
 
     /**
      * Refreshs the qualification model.
      * 
      * @param assistant
      *            the new assistant
      */
     private void refreshQualification(Assistant assistant) {
         if (assistant == null) {
             return;
         }
 
         cmbQualification.setSelectedItem(Qualification.valueOf(assistant
             .getQualification()));
     }
 
     /**
      * Get the id referencing the assistant.
      * 
      * @return The id of the assistant
      */
     public Assistant getAssistant() {
         return (Assistant) cmbAssistant.getSelectedItem();
     }
 
     /**
      * Get the id referencing the contract.
      * 
      * @return The id of the contract
      */
     public int getContractId() {
         if (cmbContract.getSelectedItem() != null) {
             return ((Contract) cmbContract.getSelectedItem()).getId();
         }
 
         return 0;
     }
 
     /**
      * Get the funds of the employment.
      * 
      * @return The funds of the employment
      */
     public String getFunds() {
         Object funds = cmbFunds.getSelectedItem();
 
         return funds instanceof CostUnit ? ((CostUnit) funds).getTokenDB()
                 : (String) funds;
     }
 
     /**
      * Get the id referencing the course.
      * 
      * @return The id of the course
      */
     public Course getCourse() {
         return (Course) cmbCourse.getSelectedItem();
     }
 
     /**
      * Get the funds of the employment.
      * 
      * @return The funds of the employment
      * @throws NumberFormatException
      */
     public Integer getCostUnit() throws NumberFormatException {
         return Integer.valueOf((String) cmbCostUnit.getSelectedItem());
     }
 
     /**
      * Get the qualification of the employment.
      * 
      * @return The qualification of the employment
      */
     public String getQualification() {
         return ((Qualification) cmbQualification.getSelectedItem()).name();
     }
 
     /**
      * Get the remarks regarding the employment.
      * 
      * @return The remarks regarding the employment
      */
     public String getRemark() {
         return txtRemark.getText();
     }
 
     /**
      * Get the dates of the employment.
      * 
      * @return The dates of the employment
      */
     public List<Date> getDates() {
         List<Date> dates = new ArrayList<Date>();
 
         for (DateLine dl : dateLines) {
             dates.add((Date) dl.spDate.getValue());
         }
 
         return dates;
     }
 
     /**
      * Get the hour counts worked during the employment.
      * 
      * @return The hour counts worked during the employment
      * @throws NumberFormatException
      */
     public List<Double> getHourCounts() throws ParseException {
         List<Double> hcs = new ArrayList<Double>();
 
         for (DateLine dl : dateLines) {
             hcs.add(NumberFormat.getInstance().parse(dl.txtHourCount.getText())
                 .doubleValue());
         }
 
         return hcs;
     }
 
     /**
      * Removes all date lines from editor.
      */
     private void clearDateLines() {
         for (DateLine dateLine : dateLines) {
             remove(dateLine.lblDate);
             remove(dateLine.spDate);
             remove(dateLine.lblHourCount);
             remove(dateLine.txtHourCount);
             remove(dateLine.btnPlusMinus);
         }
 
         dateLines.clear();
 
         repaint();
         revalidate();
     }
 
     /**
      * Generates the employments months in concordance to the contract.
      */
     private void generateEmploymentMonths() {
         Contract contract = (Contract) cmbContract.getSelectedItem();
 
         if (contract == null || contract.getStartDate() == null) {
             return;
         }
 
         Calendar start = Calendar.getInstance();
         start.setTime(contract.getStartDate());
         start.set(Calendar.DAY_OF_MONTH, 1);
 
         Calendar end = Calendar.getInstance();
         end.setTime(contract.getEndDate());
         end.set(Calendar.DAY_OF_MONTH, 2);
 
         clearDateLines();
 
         addNewDate();
         dateLines.get(0).spDate.setValue(start.getTime());
         start.add(Calendar.MONTH, 1);
 
         while (end.after(start)) {
             addNewDate();
 
             start.add(Calendar.MONTH, 1);
         }
     }
 
     /**
      * Adds a new date line to the form.
      */
     private void addNewDate() {
         GridBagConstraints gridBagConstraints;
 
         JLabel lblDate = new JLabel();
         lblDate.setText(_("Date"));
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(lblDate, gridBagConstraints);
 
         JSpinner spDate = new JSpinner();
         spDate.setModel(new SpinnerDateModel());
         spDate.setEditor(new JSpinner.DateEditor(spDate, "MM.yyyy"));
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(spDate, gridBagConstraints);
 
         JLabel lblHourCount = new JLabel();
         lblHourCount.setText(_("Hour count"));
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
         gridBagConstraints.anchor = GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 35, 10, 10);
         add(lblHourCount, gridBagConstraints);
 
         JTextField txtHourCount = new JTextField();
         txtHourCount.setMinimumSize(new java.awt.Dimension(200, 25));
         txtHourCount.setPreferredSize(new java.awt.Dimension(200, 25));
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(txtHourCount, gridBagConstraints);
 
         InputPatternFilter.addDoubleFilter(txtHourCount);
 
         if (dateLines.isEmpty() && !editMode) {
             JLabel lblAutoGuess = new JLabel();
             lblAutoGuess.setIcon(new javax.swing.ImageIcon(getClass()
                 .getResource("/de/aidger/res/icons/wand.png")));
             lblAutoGuess
                 .setToolTipText(_("Generate employment months in concordance to the contract."));
             gridBagConstraints = new GridBagConstraints();
             gridBagConstraints.gridx = 4;
             gridBagConstraints.gridy = 4;
             gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
             add(lblAutoGuess, gridBagConstraints);
 
             lblAutoGuess.addMouseListener(new java.awt.event.MouseAdapter() {
                 @Override
                 public void mouseClicked(java.awt.event.MouseEvent evt) {
                     generateEmploymentMonths();
                 }
             });
         }
 
         JButton btnPlusMinus = new JButton();
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 5;
 
         DateLine dl = new DateLine(lblDate, spDate, lblHourCount, txtHourCount,
             btnPlusMinus);
 
         if (dateLines.isEmpty()) {
             EditorTab.setTimeToNow(spDate);
 
             btnPlusMinus.setIcon(new ImageIcon(getClass().getResource(
                 "/de/aidger/res/icons/plus-small.png")));
 
             gridBagConstraints.gridy = 4;
 
             btnPlusMinus.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     addNewDate();
                 }
             });
 
             if (editMode) {
                 btnPlusMinus.setVisible(false);
             }
         } else {
             gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
 
             btnPlusMinus.setAction(new RemoveDateAction(dl));
 
             Calendar cal = Calendar.getInstance();
             cal.clear();
             cal.setTime((Date) dateLines.get(dateLines.size() - 1).spDate
                 .getValue());
             cal.add(Calendar.MONTH, 1);
 
             spDate.setValue(cal.getTime());
 
             txtHourCount
                 .setText(dateLines.get(dateLines.size() - 1).txtHourCount
                     .getText());
         }
 
         add(btnPlusMinus, gridBagConstraints);
 
         dateLines.add(dl);
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         lblAssistant = new javax.swing.JLabel();
         lblCourse = new javax.swing.JLabel();
         lblContract = new javax.swing.JLabel();
         lblCostUnit = new javax.swing.JLabel();
         lblFunds = new javax.swing.JLabel();
         lblQualification = new javax.swing.JLabel();
         lblRemark = new javax.swing.JLabel();
         cmbAssistant = new javax.swing.JComboBox();
         cmbCourse = new javax.swing.JComboBox();
         cmbContract = new javax.swing.JComboBox();
         btnContractAdd = new javax.swing.JButton();
         cmbCostUnit = new javax.swing.JComboBox();
         cmbFunds = new javax.swing.JComboBox();
         cmbQualification = new javax.swing.JComboBox();
         txtRemark = new javax.swing.JTextField();
         filler = new javax.swing.JLabel();
 
         setLayout(new java.awt.GridBagLayout());
 
         lblAssistant.setText(_("Assistant"));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(lblAssistant, gridBagConstraints);
         lblAssistant.getAccessibleContext().setAccessibleDescription(
             "assistantId");
 
         lblCourse.setText(_("Course"));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(lblCourse, gridBagConstraints);
         lblCourse.getAccessibleContext().setAccessibleDescription("courseId");
 
         lblContract.setText(_("Contract"));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 35, 10, 10);
         add(lblContract, gridBagConstraints);
         lblContract.getAccessibleContext().setAccessibleDescription(
             "contractId");
 
         lblCostUnit.setText(_("Cost unit"));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 35, 10, 10);
         add(lblCostUnit, gridBagConstraints);
         lblCostUnit.getAccessibleContext().setAccessibleDescription("costUnit");
 
         lblFunds.setText(_("Funds"));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(lblFunds, gridBagConstraints);
         lblFunds.getAccessibleContext().setAccessibleDescription("funds");
 
         lblQualification.setText(_("Qualification"));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 35, 10, 10);
         add(lblQualification, gridBagConstraints);
         lblQualification.getAccessibleContext().setAccessibleDescription(
             "qualification");
 
         lblRemark.setText(_("Remark"));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(lblRemark, gridBagConstraints);
         lblRemark.getAccessibleContext().setAccessibleDescription("remark");
 
         cmbAssistant.setMinimumSize(new java.awt.Dimension(300, 25));
         cmbAssistant.setPreferredSize(new java.awt.Dimension(300, 25));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(cmbAssistant, gridBagConstraints);
 
         cmbCourse.setMinimumSize(new java.awt.Dimension(300, 25));
         cmbCourse.setPreferredSize(new java.awt.Dimension(300, 25));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(cmbCourse, gridBagConstraints);
 
         cmbContract.setMinimumSize(new java.awt.Dimension(300, 25));
         cmbContract.setPreferredSize(new java.awt.Dimension(300, 25));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(cmbContract, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 5;
         gridBagConstraints.gridy = 1;
         add(btnContractAdd, gridBagConstraints);
 
         cmbCostUnit.setEnabled(false);
         cmbCostUnit.setMinimumSize(new java.awt.Dimension(300, 25));
         cmbCostUnit.setPreferredSize(new java.awt.Dimension(300, 25));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(cmbCostUnit, gridBagConstraints);
 
         cmbFunds.setModel(new javax.swing.DefaultComboBoxModel(dataManager
             .getCostUnitMap().toArray()));
         cmbFunds.setMinimumSize(new java.awt.Dimension(300, 25));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(cmbFunds, gridBagConstraints);
 
         cmbQualification.setModel(new javax.swing.DefaultComboBoxModel(
             Qualification.values()));
         cmbQualification.setEnabled(false);
         cmbQualification.setMinimumSize(new java.awt.Dimension(300, 25));
         cmbQualification.setPreferredSize(new java.awt.Dimension(300, 25));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(cmbQualification, gridBagConstraints);
 
         txtRemark.setMinimumSize(new java.awt.Dimension(200, 25));
         txtRemark.setPreferredSize(new java.awt.Dimension(200, 25));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(txtRemark, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.gridwidth = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         add(filler, gridBagConstraints);
     }// </editor-fold>//GEN-END:initComponents
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnContractAdd;
     private javax.swing.JComboBox cmbAssistant;
     private javax.swing.JComboBox cmbContract;
     private javax.swing.JComboBox cmbCostUnit;
     private javax.swing.JComboBox cmbCourse;
     private javax.swing.JComboBox cmbFunds;
     private javax.swing.JComboBox cmbQualification;
     private javax.swing.JLabel filler;
     private javax.swing.JLabel lblAssistant;
     private javax.swing.JLabel lblContract;
     private javax.swing.JLabel lblCostUnit;
     private javax.swing.JLabel lblCourse;
     private javax.swing.JLabel lblFunds;
     private javax.swing.JLabel lblQualification;
     private javax.swing.JLabel lblRemark;
     private javax.swing.JTextField txtRemark;
     // End of variables declaration//GEN-END:variables
 
     private final List<DateLine> dateLines = new ArrayList<DateLine>();
 
     /**
      * This class represents a date line in the form.
      * 
      * @author aidGer Team
      */
     private class DateLine {
         public JLabel lblDate;
         public JSpinner spDate;
         public JLabel lblHourCount;
         public JTextField txtHourCount;
         public JButton btnPlusMinus;
 
         /**
          * Initializes a date line.
          * 
          */
         public DateLine(JLabel lblDate, JSpinner spDate, JLabel lblHourCount,
                 JTextField txtHourCount, JButton btnPlusMinus) {
             this.lblDate = lblDate;
             this.spDate = spDate;
             this.lblHourCount = lblHourCount;
             this.txtHourCount = txtHourCount;
             this.btnPlusMinus = btnPlusMinus;
         }
     }
 
     /**
      * Removes a date line from the form by clicking on "-" button.
      * 
      * @author aidGer Team
      */
     private class RemoveDateAction extends AbstractAction {
         /**
          * The date line that will be removed.
          */
         private final DateLine dateLine;
 
         /**
          * Initializes the action.
          */
         public RemoveDateAction(DateLine dateLine) {
             putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource(
                 "/de/aidger/res/icons/minus-small.png")));
 
             this.dateLine = dateLine;
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see
          * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
          * )
          */
         @Override
         public void actionPerformed(ActionEvent e) {
             remove(dateLine.lblDate);
             remove(dateLine.spDate);
             remove(dateLine.lblHourCount);
             remove(dateLine.txtHourCount);
             remove(dateLine.btnPlusMinus);
 
             dateLines.remove(dateLine);
 
             repaint();
             revalidate();
         }
     }
 
 }
