 /*
  * Copyright (C) 2006-2009 Institute for Computational Biomedicine,
  *                         Weill Medical College of Cornell University
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package edu.cornell.med.icb.geo.tools;
 
 import it.unimi.dsi.lang.MutableString;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 /**
  * @author Fabien Campagne Date: Mar 1, 2006 Time: 10:51:53 AM
  */
 public class ClassificationTask {
     private String experimentDataFilename;
     private static final Log LOG = LogFactory.getLog(ClassificationTask.class);
     private final ConditionDescriptor[] conditionDescriptors;
 
     public ClassificationTask() {
         this(2);
     }
 
     public ClassificationTask(final int numberOfClasses) {
         super();
         conditionDescriptors = new ConditionDescriptor[numberOfClasses];
         for (int classIndex = 0; classIndex < numberOfClasses; classIndex++) {
             conditionDescriptors[classIndex] = new ConditionDescriptor();
         }
     }
 
     public int getNumberOfConditions() {
         return conditionDescriptors.length;
     }
 
     public ConditionIdentifiers getConditionsIdentifiers() {
         return conditionsIdentifiers;
     }
 
     public void setConditionsIdentifiers(final ConditionIdentifiers conditionsIdentifiers) {
         this.conditionsIdentifiers = conditionsIdentifiers;
         if (!conditionsIdentifiers.conditionExists(getFirstConditionName())) {
             throw new IllegalArgumentException(
                    "condition name " + getFirstConditionName() + "must be defined.");
         }
        if (!conditionsIdentifiers.conditionExists(getFirstConditionName())) {
             throw new IllegalArgumentException(
                    "condition name " + getSecondConditionName() + "must be defined.");
         }
 
     }
 
     public static ClassificationTask[] parseTaskAndConditions(final Reader taskListReader,
                                                               final Reader conditionIdsReader) {
         final ConditionIdentifiers conditionIdentifiers = readConditions(conditionIdsReader);
         if (conditionIdentifiers == null) {
             throw new IllegalStateException("conditionIndentifier must not be null");
         }
         String line;
 
         // read tasks:
         final Vector<ClassificationTask> tasks = new Vector<ClassificationTask>();
         final BufferedReader taskListBufferedReader = new BufferedReader(taskListReader);
         try {
             while ((line = taskListBufferedReader.readLine()) != null) {
                 if (line.startsWith("#")) {
                     continue;
                 }
                 final String[] tokens = line.split("[\t]");
                 final ClassificationTask task;
                 final int experimentNameIndex;
                 final int numberOfClasses;
                 if ("one-class".equals(tokens[0])) {
                     experimentNameIndex = 1;
                     numberOfClasses = 1;
                     task = parseNewTaskFormat(tokens, experimentNameIndex, numberOfClasses);
                 } else if ("two-class".equals(tokens[0])) {
                     experimentNameIndex = 1;
                     numberOfClasses = 2;
                     task = parseNewTaskFormat(tokens, experimentNameIndex, numberOfClasses);
                 } else if ("multi-class".equals(tokens[0])) {
                     experimentNameIndex = 1;
                     numberOfClasses = (tokens.length - 2) / 2;
                     task = parseNewTaskFormat(tokens, experimentNameIndex, numberOfClasses);
                 } else if ("regression".equals(tokens[0])) {
                     //   System.err.println("Error parsing task file: Keyword regression is reserved for future use, but not yet supported.");
                     throw new UnsupportedOperationException("Error parsing task file: Keyword regression is reserved for future use, but not yet supported.");
                 } else {
                     // parse legacy format:
                     experimentNameIndex = 0;
                     numberOfClasses = 2;
                     if (tokens.length != 5) {
                         System.err.println("Error parsing task. Task line must have 5 fields separated by tab. Line was :" + line);
                         return null;
                     }
                     task = new ClassificationTask(2);
                     task.setExperimentDataFilename(tokens[0]);
                     task.setConditionName(0, tokens[1]);
                     task.setConditionName(1, tokens[2]);
                     task.setConditionInstanceNumber(0, Integer.parseInt(tokens[3]));
                     task.setConditionInstanceNumber(1, Integer.parseInt(tokens[4]));
 
                 }
 
                 task.setConditionsIdentifiers(conditionIdentifiers);
                 tasks.add(task);
 
             }
         } catch (IOException e) {
             LOG.error(e);
             return null;
         }
         return tasks.toArray(new ClassificationTask[tasks.size()]);
     }
 
     private static ClassificationTask parseNewTaskFormat(final String[] tokens,
                                                          final int experimentNameIndex,
                                                          final int numberOfClasses) {
         final ClassificationTask task = new ClassificationTask(numberOfClasses);
         task.setExperimentDataFilename(tokens[experimentNameIndex]);
         for (int classIndex = 0; classIndex < numberOfClasses; classIndex++) {
             task.setConditionName(classIndex, tokens[experimentNameIndex + classIndex * 2 + 1]);
             task.setConditionInstanceNumber(classIndex,
                     Integer.parseInt(tokens[experimentNameIndex + classIndex * 2 + 2]));
 
         }
         return task;
     }
 
     private static ConditionIdentifiers readConditions(final Reader conditionIdsReader) {
         // read conditions to id mapping:
         final ConditionIdentifiers conditionIdentifiers = new ConditionIdentifiers();
         final BufferedReader conditionReader = new BufferedReader(conditionIdsReader);
         String line;
         try {
             while ((line = conditionReader.readLine()) != null) {
                 if (line.startsWith("#")) {
                     continue;
                 }
                 final String[] tokens = line.split("[\t]");
                 if (tokens.length != 2) {
                     LOG.fatal("condition-id line must have 2 fields separated by tabs. "
                             + "Line was :" + line);
                     System.exit(1);
                 }
                 //   System.out.println("counting: "+tokens[0]+" "+ tokens[1]);
                 conditionIdentifiers.addIdentifier(tokens[0], tokens[1]);
             }
         } catch (IOException e) {
             LOG.error(e);
             return null;
         }
 
         return conditionIdentifiers;
     }
 
     /**
      * The condition/class to predict.
      */
     protected class ConditionDescriptor {
         /**
          * The name of the condition/class.
          */
         public String conditionName;
         /**
          * The number of instances in this class/condition.
          */
         public int numberOfInstances;
     }
 
     private ConditionIdentifiers conditionsIdentifiers;
 
     public String getExperimentDataFilename() {
         return experimentDataFilename;
     }
 
     public ConditionDescriptor getClassDescriptor(final int classIndex) {
         return getConditionDescriptor(classIndex);
     }
 
     private ConditionDescriptor getConditionDescriptor(final int classIndex) {
         return conditionDescriptors[classIndex];
     }
 
     public void setExperimentDataFilename(final String experimentDataFilename) {
         this.experimentDataFilename = experimentDataFilename;
     }
 
     public String getFirstConditionName() {
         final int conditionIndex = 0;
         return getConditionName(conditionIndex);
     }
 
     /**
      * Return an array of all condition names (classes).
      * @return the condition names.
      */
     public String[] getConditionNames() {
         final int length = getNumberOfConditions();
         final String[] names = new String[length];
         for (int i = 0; i < length; i++) {
             names[i] = getConditionName(i);
         }
         return names;
     }
 
     public String getConditionName(final int conditionIndex) {
         return conditionDescriptors[conditionIndex].conditionName;
     }
 
     public void setFirstConditionName(final String firstConditionName) {
 
         setConditionName(0, firstConditionName);
     }
 
     public int getNumberSamplesFirstCondition() {
         return getNumberSamples(0);
     }
 
     public int getNumberSamples(final int conditionIndex) {
         return conditionDescriptors[conditionIndex].numberOfInstances;
     }
 
     public void setNumberSamplesFirstCondition(
             final int numberSamplesFirstCondition) {
         setConditionInstanceNumber(0, numberSamplesFirstCondition);
     }
 
     public int getNumberSamplesSecondCondition() {
         return getNumberSamples(1);
     }
 
     public void setNumberSamplesSecondCondition(
             final int numberSamplesSecondCondition) {
 
         setConditionInstanceNumber(1, numberSamplesSecondCondition);
     }
 
     private void setConditionInstanceNumber(final int conditionIndex,
                                             final int numberSamplesSecondCondition) {
         conditionDescriptors[conditionIndex].numberOfInstances = numberSamplesSecondCondition;
     }
 
     public String getSecondConditionName() {
         return getConditionName(1);
     }
 
     public void setSecondConditionName(final String secondConditionName) {
         setConditionName(1, secondConditionName);
     }
 
     public void setConditionName(final int conditionIndex, final String name) {
         conditionDescriptors[conditionIndex].conditionName = name.intern();
     }
 
     @Override
     public String toString() {
         final StringBuilder text = new StringBuilder();
         text.append(experimentDataFilename);
         for (int classIndex = 0; classIndex < getNumberOfConditions(); classIndex++) {
             text.append('_');
             text.append(getConditionName(classIndex));
         }
 
         return text.toString();
     }
 
     public MutableString getHeaders(final char delimiter) {
         final MutableString headers = new MutableString();
         headers.append("experimentDataFilename");
         for (int classIndex = 0; classIndex < getNumberOfConditions(); classIndex++) {
             headers.append(delimiter);
             headers.append(getConditionName(classIndex));
         }
 
         return headers;
     }
 
     /**
      * Read a cids/ conditions file.
      *
      * @param conditionIdsFilename
      * @return
      * @throws IOException
      */
     public static ConditionIdentifiers readConditions(final String conditionIdsFilename) throws IOException {
         return readConditions(new FileReader(conditionIdsFilename));
     }
 
     public static ClassificationTask[] parseTaskAndConditions(final String taskListFilename,
                                                               final String conditionIdsFilename) throws IOException {
         return parseTaskAndConditions(new FileReader(taskListFilename), new FileReader(conditionIdsFilename));
     }
 
     /**
      * Return text with the different attributes of this task, in the order of
      * getHeaders().
      *
      * @param delimiter The character to delimit task attributes.
      * @return character delimited content of this string.
      */
     public MutableString getDataAsText(final char delimiter) {
         final MutableString headers = new MutableString();
         headers.append(getExperimentDataFilename());
         headers.append(delimiter);
         headers.append(getFirstConditionName());
         headers.append(delimiter);
         headers.append(getSecondConditionName());
 
         return headers;
     }
 
     public List<Set<String>> calculateLabelValueGroups() {
         final ClassificationTask classificationTask = this;
         final List<Set<String>> labelValueGroups;
         labelValueGroups = new ArrayList<Set<String>>();
 
         final ConditionIdentifiers conditionsIdentifiers = classificationTask.getConditionsIdentifiers();
         for (int classIndex = 0; classIndex < getNumberOfConditions(); classIndex++) {
             final Set<String> labelGroup =
                     conditionsIdentifiers.getLabelGroup(classificationTask.getConditionName(classIndex));
             if (labelGroup == null) {
                 LOG.fatal("IDs were not found for condition name: "
                         + classificationTask.getConditionName(classIndex));
                 System.exit(1);
             }
 
             if (labelGroup.size() != classificationTask.getNumberSamples(classIndex)) {
                 LOG.fatal(" Number of samples must match in task list and conditions files "
                         + "(condition: " + classificationTask.getConditionName(classIndex) + ")");
                 System.exit(2);
             }
             labelValueGroups.add(labelGroup);
         }
         return labelValueGroups;
     }
 }
