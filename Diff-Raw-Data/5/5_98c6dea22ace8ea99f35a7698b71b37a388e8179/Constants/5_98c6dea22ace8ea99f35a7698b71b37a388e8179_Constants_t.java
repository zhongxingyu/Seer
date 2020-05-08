 // Constants.java
 package org.eclipse.stem.core;
 
 /*******************************************************************************
  * Copyright (c) 2006, 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import org.eclipse.stem.core.common.DublinCore;
 import org.eclipse.stem.core.common.Identifiable;
 import org.eclipse.stem.core.experiment.Experiment;
 import org.eclipse.stem.core.graph.Graph;
 import org.eclipse.stem.core.model.Decorator;
 import org.eclipse.stem.core.model.Model;
 import org.eclipse.stem.core.scenario.Scenario;
 import org.eclipse.stem.core.sequencer.Sequencer;
 import org.eclipse.stem.core.trigger.Trigger;
 
 /**
  * This interface contains a number of significant constants used by the STEM
  * implementation.
  */
 public interface Constants {
 
 	/**
 	 * This is the string that is the root of all packages and plugin id's
 	 */
 	String ID_ROOT = "org.eclipse.stem";
 
 	/**
 	 * This is the id for the STEM project nature.
 	 */
 	String ID_STEM_PROJECT_NATURE = ID_ROOT + ".stemnature";
 
 	/**
 	 * This is the scheme used for STEM URI's that uniquely identify a
 	 * component.
 	 */
 	String STEM_SCHEME = "stem";
 
 	/**
 	 * This is the scheme used for STEM URI's that uniquely identify a STEM
 	 * "type".
 	 */
 	String STEM_TYPE_SCHEME = "stemtype";
 
 	/**
 	 * This is the "authority" of a URI that refers to STEM resources.
 	 */
 	String STEM_URI_AUTHORITY = ID_ROOT;
 
 	/**
 	 * This is the extension point id for the "graph" extension point. {@value}
 	 * 
 	 * @see Graph
 	 */
 	String ID_GRAPH_EXTENSION_POINT = ID_ROOT + ".core.graph";
 
 	/**
 	 * This is the extension point id for the "model" extension point. {@value}
 	 * 
 	 * @see Model
 	 */
 	String ID_MODEL_EXTENSION_POINT = ID_ROOT + ".core.model";
 
 	/**
 	 * This is the extension point id for the "scenario" extension point. * *
 	 * {@value}
 	 * 
 	 * @see Scenario
 	 */
 	String ID_SCENARIO_EXTENSION_POINT = ID_ROOT + ".core.scenario";
 
 	/**
 	 * This is the extension point id for the "experiment" extension point. * *
 	 * {@value}
 	 * 
 	 * @see Experiment
 	 */
 	String ID_EXPERIMENT_EXTENSION_POINT = ID_ROOT + ".core.experiment";
 
 	/**
 	 * This is the extension point id of the "Sequencers" extension point. * *
 	 * {@value}
 	 * 
 	 * @see Sequencer
 	 */
 	String ID_SEQUENCER_EXTENSION_POINT = ID_ROOT + ".core.sequencer";
 
 	/**
 	 * This is the extension point id of the "Decorators" extension point * *
 	 * {@value}
 	 * 
 	 * @see Decorator
 	 */
 	String ID_DECORATOR_EXTENSION_POINT = ID_ROOT + ".core.decorator";
 
 	/**
 	 * This is the extension point id of the "Triggers" extension point * *
 	 * {@value}
 	 * 
 	 * @see Trigger
 	 */
 	String ID_TRIGGER_EXTENSION_POINT = ID_ROOT + ".core.trigger";
 
 	/**
 	 * This is the extension point id of the "Predicate" extension point * *
 	 * {@value}
 	 * 
 	 * @see Predicate
 	 */
 	String ID_PREDICATE_EXTENSION_POINT = ID_ROOT + ".core.predicate";
 
 	/**
 	 * This is the extension point id for the "solver" extension point. A
 	 * {@link Solver} defines the underlying computational model.
 	 * {@value}
 	 */
 	String ID_SOLVER_EXTENSION_POINT = org.eclipse.stem.core.Constants.ID_ROOT
 	+ ".core.solver";
 
 	
 	/**
 	 * This is the identifier of the element in a
 	 * <code>ConfigurationElement</code> that specifies the name and
 	 * implementing class for STEM "solver".
 	 * 
 	 * @see org.eclipse.stem.core.Constants#EXECUTABLE_NAME_ATTRIBUTE
 	 */
 	String SOLVER_ELEMENT = "classdef";
 
 	/**
 	 * This is the identifier of the XML element that specifies the name of a
 	 * "category" for extension points that are extended by extensions that plug
 	 * in {@link Identifiable}s. {@value}
 	 * 
 	 * @see #CATEGORY_NAME_ATTRIBUTE
 	 * @see #PARENT_ID_ATTRIBUTE
 	 */
 	String CATEGORY_ELEMENT = "stem_category";
 
 	/**
 	 * This is the identifier of the XML attribute in a CATEGORY element that
 	 * specifies the name of the category. {@value}
 	 * 
 	 * @see #CATEGORY_ELEMENT
 	 * @see #PARENT_ID_ATTRIBUTE
 	 */
 	String CATEGORY_NAME_ATTRIBUTE = "name";
 
 	/**
 	 * This is the identifier of the XML attribute in a Category element that
 	 * specifies the unique identifier of the category. {@value}
 	 * 
 	 * @see #CATEGORY_NAME_ATTRIBUTE
 	 * @see #PARENT_ID_ATTRIBUTE
 	 */
 	String CATEGORY_ID_ATTRIBUTE = "id";
 
 	/**
 	 * This is the identifier of the XML attribute in a CATEGORY element that
 	 * specifies the unique identifier of the parent category of a child
 	 * category. {@value}
 	 * 
 	 * @see #CATEGORY_ELEMENT
 	 * @see #CATEGORY_NAME_ATTRIBUTE
 	 */
 	String PARENT_ID_ATTRIBUTE = "parent_id";
 
 	/**
 	 * This is the identifier of the XML element that specifies the Dublin Core
 	 * values for extension points that are extended by extensions that plug in
 	 * {@link Identifiable}s. {@value}
 	 * 
 	 * @see #CATEGORY_ELEMENT
 	 * @see DublinCore
 	 */
 	String DUBLIN_CORE_ELEMENT = "dublin_core";
 
 	/**
 	 * This is the identifier of the XML element that specifies the name and
 	 * implementing class for STEM "sequencers".
 	 * 
 	 * @see #EXECUTABLE_NAME_ATTRIBUTE
 	 */
 	String SEQUENCER_ELEMENT = "sequencer";
 
 	/**
 	 * This is the identifier of the XML element that specifies the name and
 	 * implementing class for STEM "decorators"
 	 * 
 	 * @see #EXECUTABLE_NAME_ATTRIBUTE
 	 */
 	String DECORATOR_ELEMENT = "decorator";
 
 	/**
 	 * This is the identifier of the XML attribute in an Observer element that
 	 * specifies the name of a STEM "executable".
 	 * 
 	 * @see #SEQUENCER_ELEMENT
 	 * @see #DECORATOR_ELEMENT
 	 */
 	String EXECUTABLE_NAME_ATTRIBUTE = "name";
 
 	/**
 	 * The name of the root category for all IdentifiablePluginViews.
 	 * 
 	 * @see #IDENTIFIABLE_ROOT_CATEGORY_ID
 	 */
 	String IDENTIFIABLE_ROOT_CATEGORY_NAME = "/";
 
 	/**
 	 * This is the unique identifier of the root category. {@value}
 	 * 
 	 * @see #IDENTIFIABLE_ROOT_CATEGORY_NAME
 	 */
 	String IDENTIFIABLE_ROOT_CATEGORY_ID = IDENTIFIABLE_ROOT_CATEGORY_NAME;
 

 
 } // Constants
