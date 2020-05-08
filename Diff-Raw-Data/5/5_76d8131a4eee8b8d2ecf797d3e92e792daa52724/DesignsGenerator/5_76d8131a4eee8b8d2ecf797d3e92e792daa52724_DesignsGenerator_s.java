 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ibfb.studyeditor.designs;
 
 import ibfb.domain.core.Factor;
 import ibfb.domain.core.FactorLabel;
 import ibfb.domain.core.Workbook;
 import ibfb.studyeditor.core.model.GermplasmEntriesTableModel;
 import ibfb.studyeditor.core.model.ObservationsTableModel;
 import ibfb.studyeditor.core.model.OtherTreatmentFactorsTableModel;
 import ibfb.studyeditor.core.model.TreatmentLabelsTableModel;
 import java.text.NumberFormat;
 import java.util.*;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.table.TableColumnModel;
 
 /**
  *
  * @author TMSANCHEZ
  */
 public class DesignsGenerator {
 
     private JTable jTableEntries;
     private JTextField jTextFieldEntries;
     private Workbook workbook;
     private TreatmentLabelsTableModel treatmentLabelsTableModel;
 
     public DesignsGenerator(JTable jTableEntries, JTextField jTextFieldEntries, Workbook workbook, TreatmentLabelsTableModel treatmentLabelsTableModel) {
         this.jTableEntries = jTableEntries;
         this.jTextFieldEntries = jTextFieldEntries;
         this.workbook = workbook;
         this.treatmentLabelsTableModel = treatmentLabelsTableModel;
     }
 
     /**
      * Get number of combinations
      *
      * @return
      */
     public int getCombintationsTotal() {
         int combintationsTotal = 0;
 
         if (workbook.getOtherFactors() == null) {
             combintationsTotal = 1;
         } else if (workbook.getOtherFactors().isEmpty()) {
             combintationsTotal = 1;
         } else {
             for (Factor otherFactor : workbook.getOtherFactors()) {
                 if (otherFactor.getValue() != null && !otherFactor.getValue().toString().isEmpty()) {
                     Integer value = Integer.parseInt(otherFactor.getValue().toString());
                     combintationsTotal += value;
                 }
             }
         }
         return combintationsTotal;
     }
 
     /**
      * Generates an unreplicated design with randomization
      *
      * @param trial
      * @param model
      * @param otherFactors
      * @param factorsDesignCad
      * @param totalRep
      */
     public void generateUnreplicatedDesignWithRandomization(int trial, ObservationsTableModel model, int totalRep) {
         GermplasmEntriesTableModel entriesTableModel = (GermplasmEntriesTableModel) this.jTableEntries.getModel();
         int total = Integer.parseInt(this.jTextFieldEntries.getText());
         List<List<Object>> trialValues = new ArrayList<List<Object>>();
         int otherTreatmentPlot = 1;
         for (int i = 0; i < total; i++) {
             for (int j = 0; j < totalRep; j++) {
                 Object[] rowToAdd = new Object[model.getColumnCount()];
                 rowToAdd[model.getHeaderIndex(ObservationsTableModel.TRIAL)] = trial;
                 if (model.getHeaderIndex(ObservationsTableModel.REPLICATION) > 0) {
                     rowToAdd[model.getHeaderIndex(ObservationsTableModel.REPLICATION)] = 1;
                 }
                 if (model.getHeaderIndex(ObservationsTableModel.BLOCK) > 0) {
                     rowToAdd[model.getHeaderIndex(ObservationsTableModel.BLOCK)] = 1;
                 }
 
                 //A2*10^(TRUNC(LOG10(MAX(16,5,22)))+1)+B2                
 
                 int resInt = (int) java.lang.Math.floor(java.lang.Math.log10(total)) + 1;
                 int newPlot = (trial * ((int) (Math.pow(10, resInt)))) + (i + 1);
 
                 if (model.getHeaderIndex(ObservationsTableModel.PLOTNUMBER) > 0) {
                     rowToAdd[model.getHeaderIndex(ObservationsTableModel.PLOTNUMBER)] = i + 1;
                 } else {
 
                     rowToAdd[model.getHeaderIndex(ObservationsTableModel.PLOT)] = newPlot; //NESTEDNUMBER
                 }
                 // rowToAdd[model.getHeaderIndex(ObservationsTableModel.PLOT)] = i + 1;
                 //rowToAdd[model.getHeaderIndex(ObservationsTableModel.ENTRY)] = i + 1;
 
                 int entriesColIndex = 0;
                 for (Factor factor : entriesTableModel.getFactorHeaders()) {
                     String columnHeader = Workbook.getStringWithOutBlanks(factor.getProperty() + factor.getScale());
                     //rowToAdd[model.getHeaderIndex(columnHeader)] = entriesTableModel.getValueAt(vector[i], entriesColIndex);
                     rowToAdd[model.getHeaderIndex(columnHeader)] = entriesTableModel.getValueAt(i, entriesColIndex);
                     entriesColIndex++;
                 }
 
                 // tmsanchez
                 if (workbook.getOtherFactors() != null && !workbook.getOtherFactors().isEmpty()) {
                     for (Factor otherFactor : workbook.getOtherFactors()) {
                         String columnHeader = Workbook.getStringWithOutBlanks(otherFactor.getProperty() + otherFactor.getScale());
                         int headerIndex = model.getHeaderIndex(columnHeader);
                         int factorLevel = 1;
                         for (FactorLabel factorLabel : treatmentLabelsTableModel.getFactorLabels()) {
                             if (model.getHeaderIndex(ObservationsTableModel.PLOTNUMBER) > 0) {
                                 rowToAdd[model.getHeaderIndex(ObservationsTableModel.PLOTNUMBER)] = otherTreatmentPlot;
                             }
                             //rowToAdd[headerIndex] = factorLabel.getValue();
                             rowToAdd[headerIndex] = factorLevel;
                             for (Factor childFactor : workbook.getChildFactors(otherFactor.getFactorName())) {
                                 String columnHeaderChild = Workbook.getStringWithOutBlanks(childFactor.getProperty() + childFactor.getScale());
                                 int headerIndexChild = model.getHeaderIndex(columnHeaderChild);
                                 rowToAdd[headerIndexChild] = factorLabel.getValue();
                             }
 
                             //model.addRow(rowToAdd);
                             trialValues.add(new ArrayList<Object>(Arrays.asList(rowToAdd)));
                             otherTreatmentPlot++;
                             //increase factor level
                             factorLevel++;
                         }
 
                     }
 
                 } else {
                     //model.addRow(rowToAdd);
                     trialValues.add(new ArrayList<Object>(Arrays.asList(rowToAdd)));
                 }
             }
         }
 
         int vector[] = randomize(trialValues.size());
         List<List<Object>> finalList = new ArrayList<List<Object>>();
         for (int index = 0; index < vector.length; index++) {
             finalList.add(trialValues.get(vector[index]));
         }
         model.getValues().addAll(finalList);
 
     }
 
     /**
      * Generates an Unreplicated Design with out randomization
      *
      * @param trial
      * @param model
      * @param otherFactors
      * @param factorsDesignCad
      * @param totalRep
      */
     public void generateUnreplicatedDesignWithoutRandomization(int trial, ObservationsTableModel model, int totalRep) {
         TableColumnModel tcm = this.jTableEntries.getColumnModel();
         GermplasmEntriesTableModel entriesTableModel = (GermplasmEntriesTableModel) this.jTableEntries.getModel();
         int total = Integer.parseInt(this.jTextFieldEntries.getText());
         int otherTreatmentPlot = 1;
         NumberFormat numberFormat = NumberFormat.getInstance();
         numberFormat.setMaximumFractionDigits(0);
 
         for (int i = 0; i < total; i++) {
             for (int j = 0; j < totalRep; j++) {
                 Object[] rowToAdd = new Object[model.getColumnCount()];
                 rowToAdd[model.getHeaderIndex(ObservationsTableModel.TRIAL)] = trial;
 
                 if (model.getHeaderIndex(ObservationsTableModel.REPLICATION) > 0) {
                     rowToAdd[model.getHeaderIndex(ObservationsTableModel.REPLICATION)] = 1;
                 }
 
                 if (model.getHeaderIndex(ObservationsTableModel.BLOCK) > 0) {
                     rowToAdd[model.getHeaderIndex(ObservationsTableModel.BLOCK)] = 1;
                 }
 
                 //A2*10^(TRUNC(LOG10(MAX(16,5,22)))+1)+B2                
 
                 int resInt = (int) java.lang.Math.floor(java.lang.Math.log10(total)) + 1;
                 int newPlot = (trial * ((int) (Math.pow(10, resInt)))) + (i + 1);
 
                 if (model.getHeaderIndex(ObservationsTableModel.PLOTNUMBER) > 0) {
                     rowToAdd[model.getHeaderIndex(ObservationsTableModel.PLOTNUMBER)] = i + 1;
                 } else {
                     rowToAdd[model.getHeaderIndex(ObservationsTableModel.PLOT)] = newPlot; //NESTEDNUMBER
                 }
                 //  rowToAdd[model.getHeaderIndex(ObservationsTableModel.PLOT)] = i;
 
                 //rowToAdd[model.getHeaderIndex(ObservationsTableModel.ENTRY)] = i + 1;
                 int entriesColIndex = 0;
                 for (Factor factor : entriesTableModel.getFactorHeaders()) {
                     String columnHeader = Workbook.getStringWithOutBlanks(factor.getProperty() + factor.getScale());
                     rowToAdd[model.getHeaderIndex(columnHeader)] = entriesTableModel.getValueAt(i, entriesColIndex);
                     entriesColIndex++;
                 }
                 // tmsanchez
                 if (workbook.getOtherFactors() != null && !workbook.getOtherFactors().isEmpty()) {
                     for (Factor otherFactor : workbook.getOtherFactors()) {
                         String columnHeader = Workbook.getStringWithOutBlanks(otherFactor.getProperty() + otherFactor.getScale());
                         int headerIndex = model.getHeaderIndex(columnHeader);
                         int factorLevel = 1;
                         for (FactorLabel factorLabel : treatmentLabelsTableModel.getFactorLabels()) {
                             if (model.getHeaderIndex(ObservationsTableModel.PLOTNUMBER) > 0) {
                                 rowToAdd[model.getHeaderIndex(ObservationsTableModel.PLOTNUMBER)] = otherTreatmentPlot;
                             }
                             //rowToAdd[headerIndex] = factorLabel.getValue();
                             rowToAdd[headerIndex] = factorLevel;
 
                             for (Factor childFactor : workbook.getChildFactors(otherFactor.getFactorName())) {
                                 String columnHeaderChild = Workbook.getStringWithOutBlanks(childFactor.getProperty() + childFactor.getScale());
                                 int headerIndexChild = model.getHeaderIndex(columnHeaderChild);
                                 rowToAdd[headerIndexChild] = factorLabel.getValue();
                             }
 
                            model.addRow(rowToAdd);
                             otherTreatmentPlot++;
                             //increase factor level
                             factorLevel++;
                         }
 
                     }
 
                } else {
                    model.addRow(rowToAdd);
                 }
                 model.addRow(rowToAdd);
             }
         }
     }
 
     public void generateRandomizeCompleteBlock(int rep, int trial, ObservationsTableModel model, ArrayList<String> otherFactors, String[][] factorsDesignCad, int totalRep) {
         GermplasmEntriesTableModel entriesTableModel = (GermplasmEntriesTableModel) this.jTableEntries.getModel();
         int total = Integer.parseInt(this.jTextFieldEntries.getText());
         int plot = 0;
         int repet = 0;
         int otherTreatmentPlot = 1;
         List<List<Object>> trialValues = new ArrayList<List<Object>>();
         //A2*10^(TRUNC(LOG10(MAX(16,5,22)))+1)+B2                                
 
         for (int j = 0; j < rep; j++) {
             repet++;
             int vector[] = randomize(total);
 
             for (int i = 0; i < total; i++) {
 
 
                 int resInt = (int) java.lang.Math.floor(java.lang.Math.log10(total * rep)) + 1;
                 int newPlot = (trial * ((int) (Math.pow(10, resInt)))) + (plot + 1);
                 plot++;
 
                 for (int m = 0; m < totalRep; m++) {
                     Object[] rowToAdd = new Object[model.getColumnCount()];
                     rowToAdd[model.getHeaderIndex(ObservationsTableModel.TRIAL)] = trial;
                     if (model.getHeaderIndex(ObservationsTableModel.REPLICATION) > 0) {
                         rowToAdd[model.getHeaderIndex(ObservationsTableModel.REPLICATION)] = repet;
                     }
                     if (model.getHeaderIndex(ObservationsTableModel.BLOCK) > 0) {
                         rowToAdd[model.getHeaderIndex(ObservationsTableModel.BLOCK)] = 1;
                     }
 
 
                     if (model.getHeaderIndex(ObservationsTableModel.PLOTNUMBER) > 0) {
                         rowToAdd[model.getHeaderIndex(ObservationsTableModel.PLOTNUMBER)] = plot;
                     } else {
                         rowToAdd[model.getHeaderIndex(ObservationsTableModel.PLOT)] = newPlot;
                     }
 
                     int entriesColIndex = 0;
                     for (Factor factor : entriesTableModel.getFactorHeaders()) {
                         String columnHeader = Workbook.getStringWithOutBlanks(factor.getProperty() + factor.getScale());
                         rowToAdd[model.getHeaderIndex(columnHeader)] = entriesTableModel.getValueAt(vector[i], entriesColIndex);
                         entriesColIndex++;
                     }
 
 //                    // tmsanchez
 //                    if (otherFactors != null) {
 //                        for (int k = 0; k < otherFactors.size(); k++) {
 //                            //rowToAdd[findColumn(otherFactors.get(k), model)] = factorsDesignCad[k][j];
 //                        }
 //                    }
 //                    model.addRow(rowToAdd);
 // tmsanchez
                     if (workbook.getOtherFactors() != null && !workbook.getOtherFactors().isEmpty()) {
                         for (Factor otherFactor : workbook.getOtherFactors()) {
                             String columnHeader = Workbook.getStringWithOutBlanks(otherFactor.getProperty() + otherFactor.getScale());
                             int headerIndex = model.getHeaderIndex(columnHeader);
                             int factorLevel = 1;
                             for (FactorLabel factorLabel : treatmentLabelsTableModel.getFactorLabels()) {
                                 if (model.getHeaderIndex(ObservationsTableModel.PLOTNUMBER) > 0) {
                                     rowToAdd[model.getHeaderIndex(ObservationsTableModel.PLOTNUMBER)] = otherTreatmentPlot;
                                 }
                                 //rowToAdd[headerIndex] = factorLabel.getValue();
                                 //model.addRow(rowToAdd);
                                 rowToAdd[headerIndex] = factorLevel;
                                 for (Factor childFactor : workbook.getChildFactors(otherFactor.getFactorName())) {
                                     String columnHeaderChild = Workbook.getStringWithOutBlanks(childFactor.getProperty() + childFactor.getScale());
                                     int headerIndexChild = model.getHeaderIndex(columnHeaderChild);
                                     rowToAdd[headerIndexChild] = factorLabel.getValue();
                                 }
 
                                 trialValues.add(new ArrayList<Object>(Arrays.asList(rowToAdd)));
                                 otherTreatmentPlot++;
                                 factorLevel++;
                             }
 
                         }
 
                     } else {
                         // model.addRow(rowToAdd);
                         trialValues.add(new ArrayList<Object>(Arrays.asList(rowToAdd)));
                     }
                 }
             }
         }
         int vector[] = randomize(trialValues.size());
         List<List<Object>> finalList = new ArrayList<List<Object>>();
         for (int index = 0; index < vector.length; index++) {
             finalList.add(trialValues.get(vector[index]));
         }
         model.getValues().addAll(finalList);
 
     }
 
     private int[] randomize(int tam) {
 
         Random r;
         r = new Random();
         r.setSeed(new Date().getTime());
 
 
         int vector[] = new int[tam];
         int i = 0, j;
         vector[i] = (int) (Math.random() * tam);
 
         for (i = 1; i < tam; i++) {
             vector[i] = (int) (Math.random() * tam);
             for (j = 0; j < i; j++) {
                 if (vector[i] == vector[j]) {
                     i--;
                 }
             }
         }
         return vector;
     }
 
     public void randomizeEntries(ObservationsTableModel model) {
         int total = model.getRowCount();
         int vector[] = randomize(total);
         List<List<Object>> originalList = model.getValues();
         List<List<Object>> finalList = new ArrayList<List<Object>>();
         for (int index = 0; index < total; index++) {
             finalList.add(originalList.get(vector[index]));
         }
         model.setValues(finalList);
     }
 }
