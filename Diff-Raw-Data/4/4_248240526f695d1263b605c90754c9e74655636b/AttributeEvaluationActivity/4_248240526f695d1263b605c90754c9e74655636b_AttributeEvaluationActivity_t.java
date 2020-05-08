 /*
  * Copyright (C) 2010 by Andreas Truszkowski <ATruszkowski@gmx.de>
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  * All we ask is that proper credit is given for our work, which includes
  * - but is not limited to - adding the above copyright notice to the beginning
  * of your source code files, and to any copyright notice that you may distribute
  * with programs based on this work.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
  */
 package org.openscience.cdk.applications.taverna.weka.learning;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
 import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
 import org.openscience.cdk.applications.taverna.basicutilities.ProgressLogger;
 import org.openscience.cdk.applications.taverna.weka.utilities.AttributeEvaluationWorker;
 import org.openscience.cdk.applications.taverna.weka.utilities.GAAttributeEvaluationWorker;
 import org.openscience.cdk.applications.taverna.weka.utilities.WekaTools;
 
 import weka.classifiers.Classifier;
 import weka.classifiers.Evaluation;
 import weka.core.Instances;
 import weka.filters.Filter;
 
 /**
  * Class which represents the dataset attribut selection activity.
  * 
  * @author Andreas Truzskowski
  * 
  */
 public class AttributeEvaluationActivity extends AbstractCDKActivity {
 
 	public static final String DATASET_ATTRIBUT_SELECTION_ACTIVITY = "Dataset Attribut Selection";
 
 	private AttributeEvaluationWorker[] workers = null;
 
 	private Class<?> classifierClass = null;
 	private String classifierOptions = null;
 	private Instances currentSet = null;
 	private double[] rmses = null;
 	private int currentIndex;
 
 	/**
 	 * Creates a new instance.
 	 */
 	public AttributeEvaluationActivity() {
 		this.INPUT_PORTS = new String[] { "Weka Learning Dataset" };
 		this.OUTPUT_PORTS = new String[] { "Weka Learning Datasets", "Attribut Info" };
 	}
 
 	@Override
 	protected void addInputPorts() {
 		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
 	}
 
 	@Override
 	protected void addOutputPorts() {
 		addOutput(this.OUTPUT_PORTS[0], 1);
 		addOutput(this.OUTPUT_PORTS[1], 1);
 	}
 
 	@Override
 	public void work() throws Exception {
 		WekaTools tools = new WekaTools();
 		// Get input
 		Instances dataset = this.getInputAsList(this.INPUT_PORTS[0], Instances.class).get(0);
 		String options = (String) this.getConfiguration().getAdditionalProperty(
 				CDKTavernaConstants.PROPERTY_ATTRIBUTE_SELECTION_OPTIONS);
 		String[] optionArray = options.split(";");
 		this.classifierClass = Class.forName(optionArray[0]);
 		this.classifierOptions = optionArray[1];
 		int threads = (Integer) this.getConfiguration().getAdditionalProperty(
 				CDKTavernaConstants.PROPERTY_NUMBER_OF_USED_THREADS);
 		// Do work
 		ArrayList<Instances> newDatasets = new ArrayList<Instances>();
 		LinkedList<Integer> removedAttributes = new LinkedList<Integer>();
 		this.currentSet = dataset;
 		ArrayList<String> attrInfo = new ArrayList<String>();
		ProgressLogger.getInstance().writeProgress(this.getActivityName(), "Starting work");
 		for (int i = 0; i < dataset.numAttributes() - 3; i++) {
 			// remove ID
 			this.currentSet = Filter.useFilter(this.currentSet, tools.getIDRemover(this.currentSet));
 			this.rmses = new double[currentSet.numAttributes() - 1];
 			// Calculate RMSEs
 			this.currentIndex = 0;
			
 			this.workers = new AttributeEvaluationWorker[threads];
 			for(int j = 0; j < this.workers.length; j++) {
 				this.workers[j] = new AttributeEvaluationWorker(this);
 				this.workers[j].start();
 			}
 			synchronized (this) {
 				this.wait();
 			}
 			// Choose worst attribute (best RMSE describes worst attribute)
 			double best = this.rmses[0];
 			int idx = 0;
 			for (int j = 1; j < this.rmses.length; j++) {
 				if (best > this.rmses[j]) {
 					best = this.rmses[j];
 					idx = j;
 				}
 			}
 			String name = this.currentSet.attribute(idx).name();
 			idx = dataset.attribute(name).index();
 			ProgressLogger.getInstance().writeProgress(this.getActivityName(),
 					"Step: " + (i + 1) + " - Attribute removed: " + name);
 			// range is 1..n
 			removedAttributes.add(idx + 1);
 			this.currentSet = Filter.useFilter(dataset, tools.getAttributRemover(dataset, removedAttributes));
 		}
 		// Create datasets
 		Instances set;
 		attrInfo.add("Index;RemovedAttribute;");
 		for (int i = 0; i < removedAttributes.size(); i++) {
 			String info = (i + 1) + ";" + dataset.attribute(removedAttributes.get(i)).name() + ";";
 			attrInfo.add(info);
 			set = Filter.useFilter(dataset, tools.getAttributRemover(dataset, removedAttributes.subList(0, i)));
 			newDatasets.add(set);
 		}
 		// Set output
 		this.setOutputAsObjectList(newDatasets, this.OUTPUT_PORTS[0]);
 		this.setOutputAsStringList(attrInfo, this.OUTPUT_PORTS[1]);
 	}
 
 	public Instances getCurrentSet() {
 		return this.currentSet;
 	}
 
 	public synchronized Classifier getClassifier() throws Exception {
 		Classifier classifier = (Classifier) this.classifierClass.newInstance();
 		classifier.setOptions(this.classifierOptions.split(" "));
 		return classifier;
 	}
 
 	public synchronized Integer getWork() {
 		if (this.currentIndex < this.currentSet.numAttributes() - 1) {
 			return this.currentIndex++;
 		} else {
 			return null;
 		}
 	}
 
 	public synchronized void publishResult(double result, int idx) {
 		this.rmses[idx] = result;
 	}
 
 	public synchronized void workerDone() {
 		boolean allDone = true;
 		for (AttributeEvaluationWorker worker : this.workers) {
 			if (!worker.isDone()) {
 				allDone = false;
 				break;
 			}
 		}
 		if (allDone) {
 			synchronized (this) {
 				this.notify();
 			}
 		}
 	}
 
 	@Override
 	public String getActivityName() {
 		return AttributeEvaluationActivity.DATASET_ATTRIBUT_SELECTION_ACTIVITY;
 	}
 
 	@Override
 	public HashMap<String, Object> getAdditionalProperties() {
 		HashMap<String, Object> properties = new HashMap<String, Object>();
 		properties.put(CDKTavernaConstants.PROPERTY_NUMBER_OF_USED_THREADS, Runtime.getRuntime().availableProcessors());
 		return properties;
 	}
 
 	@Override
 	public String getDescription() {
 		return "Description: " + AttributeEvaluationActivity.DATASET_ATTRIBUT_SELECTION_ACTIVITY;
 	}
 
 	@Override
 	public String getFolderName() {
 		return CDKTavernaConstants.WEKA_LEARNING_FOLDER_NAME;
 	}
 
 }
