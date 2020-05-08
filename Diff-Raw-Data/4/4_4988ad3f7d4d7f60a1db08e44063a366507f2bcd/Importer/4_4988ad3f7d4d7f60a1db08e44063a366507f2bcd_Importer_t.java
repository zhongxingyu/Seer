 /*******************************************************************************
  * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
  * as represented by the Administrator of the National Aeronautics and Space 
  * Administration. All rights reserved.
  *
  * The MCT platform is licensed under the Apache License, Version 2.0 (the 
  * "License"); you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  * http://www.apache.org/licenses/LICENSE-2.0.
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
  * License for the specific language governing permissions and limitations under 
  * the License.
  *
  * MCT includes source code licensed under additional open source licenses. See 
  * the MCT Open Source Licenses file included with this distribution or the About 
  * MCT Licenses dialog available at runtime from the MCT Help menu for additional 
  * information. 
  *******************************************************************************/
 package gov.nasa.arc.mct.importExport.provider;
 
 import gov.nasa.arc.mct.api.persistence.PersistenceService;
 import gov.nasa.arc.mct.components.AbstractComponent;
 import gov.nasa.arc.mct.components.ExtendedProperties;
 import gov.nasa.arc.mct.components.ModelStatePersistence;
 import gov.nasa.arc.mct.gui.MCTViewManifestationInfo;
 import gov.nasa.arc.mct.gui.OptionBox;
 import gov.nasa.arc.mct.importExport.access.ComponentRegistryAccess;
 import gov.nasa.arc.mct.importExport.provider.generated.AssociatedComponentType;
 import gov.nasa.arc.mct.importExport.provider.generated.ComponentListType;
 import gov.nasa.arc.mct.importExport.provider.generated.ComponentRefType;
 import gov.nasa.arc.mct.importExport.provider.generated.ComponentType;
 import gov.nasa.arc.mct.importExport.provider.generated.NameValueType;
 import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
 import gov.nasa.arc.mct.platform.spi.PlatformAccess;
 import gov.nasa.arc.mct.services.component.ComponentRegistry;
 import gov.nasa.arc.mct.services.internal.component.ComponentInitializer;
 import gov.nasa.jsc.mct.importExport.utilities.ValidationException;
 import gov.nasa.jsc.mct.importExport.utilities.XMLPersistence;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.Set;
 import java.util.TimeZone;
 
 import javax.swing.JDialog;
 import javax.swing.JProgressBar;
 import javax.swing.SwingWorker;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The <code>Importer</code> class processes XML files for import.
  * 
  */
 public class Importer extends SwingWorker<Void, Void> {
 
 	private static ResourceBundle bundle = ResourceBundle
 			.getBundle("ImportExportProvider");
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(Importer.class);
 	private static final String PREFIX = bundle.getString("metaPrefix");
 	private String missingRefMsg = "Some imported references refer to objects that do " +
 							       "not exist on this destination MCT system.";
 	private String unimportableCompMsg = "Some components can not be imported.";
 	private DialogMgr dialogMgr = new DialogMgr(null);
 	private List<Component> allComponents;
 	private List<String> badReferences;
 	private List<String> unimportableComps;
 	private int totalSize = 0;
 	private Integer currentCount = 0;
 	private PersistenceService persistenceService;
 	/** The MCT component registry **/
 	private ComponentRegistry registry;
 	/** A list of XML file objects to be parsed into MCT components **/
 	private List<File> files;
 	/** The owner of the components **/
 	private String owner;
 	/** Associated with the currently selected manifestation (component from which the
 	 *  import menu item was selected) **/
 	private AbstractComponent selectedComponent;
 	private JProgressBar progressBar;
 	private JDialog jd;
 
 	/**
 	 * Import constructor
 	 * @param files Files to import
 	 * @param owner Owner to set owner field to
 	 * @param selectedComponent Component to add imported components to
 	 * @param progressBar Progress bar
 	 * @param jd JDialog containing progress bar
 	 */
 	public Importer(List<File> files, String owner,	AbstractComponent selectedComponent, 
 			        JProgressBar progressBar, JDialog jd) {
 		
 		this.files = files;
 		this.owner = owner;
 		this.selectedComponent = selectedComponent;
 		this.progressBar = progressBar;
 		this.jd = jd;
 		
 		registry = (new ComponentRegistryAccess()).getComponentRegistry();
 		persistenceService = PlatformAccess.getPlatform().getPersistenceProvider();
 	}
 
 	/**
 	 * Constructor with no progress bar
 	 * @param files Files to import
 	 * @param owner Owner to set owner field to
 	 * @param selectedComponent Component to add imported components to
 	 */
 	public Importer(List<File> files, String owner, AbstractComponent selectedComponent) {
 		this(files, owner, selectedComponent, null, null);
 	}
 
 	/**
 	 * Parse list of Files and make components to be added to the currently selected 
 	 * manifestation under a parent named "import + datestamp".
 	 */
 	private void importComponents() {
 		try {
 			// Create component which will be named "Imported on <date>"
 			AbstractComponent importParentComponent = registry.newInstance(
 					ImportExportComponent.class, selectedComponent);
 			setOwner(importParentComponent);
 
 			Integer fileCount = 0;
 			importParentComponent.setDisplayName(getDatedName());
 
 			for (File file : files) {
 				AbstractComponent fileComp = processFile(file, importParentComponent);
 				if (fileComp != null) {
 					fileCount++;
 					importParentComponent.addDelegateComponent(fileComp);
 					importParentComponent.save();
 				}
 			}
 
 			if (fileCount > 0) {
 				selectedComponent.addDelegateComponent(importParentComponent);
 				selectedComponent.save();
 
 				String msg = fileCount + "";
 				if (fileCount == 1) {
 					msg = msg + " file successfully imported.";
 				} else {
 					msg = msg + " files successfully imported.";
 				}
 				LOGGER.info(msg);
 				dialogMgr.showMessageDialog(msg, "Import Successful", 
 						OptionBox.INFORMATION_MESSAGE);
 			}
 
 		} catch (Throwable t) {
 			// Errors shouldn't make it this far, but just in case...
 			LOGGER.error(t.toString());
             dialogMgr.showMessageDialog("Unexpected Error: " + t.toString(), 
             		"Import Failed", OptionBox.ERROR_MESSAGE);
 		}
 	}
 	
 	/**
 	 * Read and process data from an XML file
 	 * @param file XML file
 	 * @param importParentComponent Parent of file component
 	 * @return Component named with the name of the XML file
 	 */
 	private AbstractComponent processFile(File file, 
 			                              AbstractComponent importParentComponent) {
 		try {
 			// Unmarshal data from file
 			ComponentListType compList = XMLPersistence.unmarshal(file);
 			
 			allComponents = new ArrayList<Component>();
 			badReferences = new ArrayList<String>();
 			unimportableComps = new ArrayList<String>();
 
 			if (compList != null && compList.getComponent().size() > 0) {
 				// Create component with XML file name
 				AbstractComponent fileComp = makeFileComponent(file, 
 						                                       importParentComponent);
 				// Create components of data from file
 				List<AbstractComponent> comps = convertComponents(compList);
 				fileComp.addDelegateComponents(comps);
 
 				if (badReferences.size() > 0) {
 					showUnimportableMsg(file, badReferences, missingRefMsg);
 				}
 				
 				if (unimportableComps.size() > 0) {
 					showUnimportableMsg(file, unimportableComps, unimportableCompMsg);
 				}
 
 				// Loop through and save all of the components from the XML file
 				for (Component comp : allComponents) {
 					comp.getMctComp().save();
 				}
 				fileComp.save();
 
 				return fileComp;
 			}
 		} catch(IOException ex) {
 			LOGGER.error(ex.getMessage());
 			dialogMgr.showMessageDialog("Import Failed\n" + file
 					+ " not imported\nSee the log file for details.",
 					bundle.getString("import_fail_message_title"), 
 					OptionBox.ERROR_MESSAGE);
 		} catch(IllegalArgumentException ex) {
 			LOGGER.error(ex.getMessage());
 			dialogMgr.showMessageDialog("Import Failed\n" + file
 					+ " not imported\nSee the log file for details.",
 					bundle.getString("import_fail_message_title"), 
 					OptionBox.ERROR_MESSAGE);
 		} catch (ValidationException ex) {
 			LOGGER.error(ex.getMessage());
 			dialogMgr.showMessageDialog("Import Failed\n" + 
 					"The selected XML file " + file + 
 					"\ndid not validate against the schema." +
 					"\nSee the log file for details.",
 					bundle.getString("import_fail_message_title"), 
 					OptionBox.ERROR_MESSAGE);
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Converts JAXB components of type ComponentListType into an ArrayList of
 	 * AbstractComponents.  All components are at the top level in the XML file.  The
 	 * ids of their children are in each component's associatedComponents list.  Process
 	 * all elements and convert them to AbstractComponents, leaving their child lists
 	 * empty.  On the second pass through, attach the children to their parents.
 	 * 
 	 * @param componentListType The object to be converted into components
 	 * @return a list of AbstractComponents which were converted from the XML
 	 *         ComponentListType
 	 */
 	private List<AbstractComponent> convertComponents(ComponentListType componentListType) {
 		
 		List<ParentComponent> parentComps = new ArrayList<ParentComponent>();
 		List<AbstractComponent> topLevelComps = new ArrayList<AbstractComponent>();
 		
 		// Get list of top level components in XML file
 		List<ComponentType> xmlComponents = componentListType.getComponent();
 
 		totalSize = estimateTotalSize(xmlComponents);
 		
 		// Loop through all components and convert and add to list (allComponents).
 		// They are not added to their parents at this point.
 		for (ComponentType xmlComp : xmlComponents) {
 			// For every top-level component
 			AbstractComponent mctComp = convertComponent(xmlComp);
 			
 			if (mctComp != null) {
 				// Save the converted component along with the id it had in the XML file
 				// since the id in the converted component is different and we'll need
 				// it later.
 				Component comp = new Component(mctComp, xmlComp.getComponentId());
 				allComponents.add(comp);
 
 				// If this is a top level component in the structure tree, save it to 
 				// the list so we know at the end which component(s) to add to the file
 				// component (fileComp)
 				if (xmlComp.isToplevel()) {
 					topLevelComps.add(mctComp);
 				}
 
 				if (xmlComp.getAssociatedComponents() != null && 
 						xmlComp.getAssociatedComponents().size() > 0) {
 					// This component has children, so add them to the parents list
 					// for later processing.  Also, grab this component's list of 
 					// associated components (children) from the XML file. NOTE: Both 
 					// parentComps and allComponents point to the same objects in memory.
 					ParentComponent parent = new ParentComponent(mctComp,
 							xmlComp.getAssociatedComponents());
 					parentComps.add(parent);
 				}
 			}
 
 			updateProgressBar();
 		}
 
 		// Loop through the list of MCT components with children and attach children 
 		// to parents and update IDs in view state.
 		for (ParentComponent parentComp : parentComps) {
 			boolean goodParent = true;
 			// Loop through this component's children and add them to component
 			for (AssociatedComponentType childXmlComp : parentComp.getXmlChildren()) {
 				if (goodParent) {
 					String childId = childXmlComp.getId();
 					boolean childFound = false;
 					// Loop through all converted MCT components to find child
 					for (Component comp : allComponents) {
 						if (comp.getIdFromXMLFile().equals(childId)) {
 							// Add child to parent
 							childFound = true;
 							try {
 								parentComp.getMctComp()
 								          .addDelegateComponent(comp.getMctComp());
 							} catch (UnsupportedOperationException e) {
 								// This will happen if we try to add children to a 
 								// component that had an unknown class type in the XML
 								// file. It's now a BrokenComponent.
 								unimportableComps.add("Name: " + 
 								                parentComp.getMctComp().getDisplayName());
 								LOGGER.error(unimportableCompMsg + ": " + 
 								     parentComp.getMctComp().getDisplayName());
 								goodParent = false;
 							}
 							break;
 						}
 					}
 					if (!childFound) {
 						LOGGER.error("Import error: Child component not found in " +
 								"XML file: " + childXmlComp.getId());
 						unimportableComps.add("ID: " + childXmlComp.getId());
 					}
 
 					updateProgressBar();
 				}
 			}
 			
 			// Update child component IDs in the view state data since the child IDs
 			// have been changed.
 			ComponentInitializer initializer = parentComp.getMctComp()
 					                           .getCapability(ComponentInitializer.class);
 			if (initializer.getAllViewRoleProperties().size() > 0) {
 				ExtendedProperties properties = initializer
 						.getViewRoleProperties("gov.nasa.arc.mct.canvas.view.CanvasView");
 				updateCompIdInView(properties);
 			}
 		}
 		
 		return topLevelComps;
 	}
 	
 	/**
 	 * Upon import the component ids are changed. This method updates the ids in the
 	 * view state data to match the new ids.
 	 * @param properties ExtendedProperties object
 	 */
 	private void updateCompIdInView(ExtendedProperties properties) {
 		if (properties != null) {
 			Set<Object> props = properties.getProperty("CANVAS CONTENT PROPERTY");
 			if (props != null) {
 				for (Object prop : props) {
 					if (MCTViewManifestationInfo.class
 							               .isAssignableFrom(prop.getClass())) {
 						MCTViewManifestationInfo info = (MCTViewManifestationInfo) prop;
 						String oldID = info.getComponentId();
 						// Loop through all components (except references), looking for
 						// the one which used to have this ID. When it's found, take its
 						// new ID and put it in the view data in place of the old one.
 						for (Component comp : allComponents) {
 							if (comp.getIdFromXMLFile().equals(oldID)) {
 								info.setComponentId(comp.getMctComp().getComponentId());
 								break;
 							}
 						}
 
 						// Recursively update ids in the ownedProperties list
 						if (info.getOwnedProperties() != null) {
 							for (ExtendedProperties ownedProp : 
 								                   info.getOwnedProperties()) {
 								updateCompIdInView(ownedProp);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method is called for every component in the XML tree. It converts the
 	 * component to an AbstractComponent. We are ignoring children for now, except
 	 * for ones that are references only.
 	 * @param xmlComp component from the XML file
 	 * @return component as an AbstractComponent
 	 * @throws Exception
 	 */
 	private AbstractComponent convertComponent(ComponentType xmlComp) {
 		AbstractComponent comp = registry.newInstance(xmlComp.getComponentType());
 		
 		// TelemetryElementComponents should only be references in the XML file.
 		if (comp == null ||
		        "gov.nasa.arc.mct.components.telemetry".equals(comp.getClass().getName())) {
 			// Add type, ID, and external key to list of bad components
 			String refInfo = "Type: " + getSimpleClassName(xmlComp.getComponentType()) + 
 					         ", ID: " + xmlComp.getComponentId();
 			if (xmlComp.getExternalKey() != null) {
 				refInfo = refInfo + " " + xmlComp.getExternalKey();
 			}
 			unimportableComps.add(refInfo);
 			LOGGER.error(unimportableCompMsg + ": " + refInfo);
 			return null;
 		}
 
 		// Note: Date and ID should be set by the persistence layer. Creator is set by
 		// the registry.
 		comp.setDisplayName(xmlComp.getName());
 		comp.setExternalKey(xmlComp.getExternalKey());
 		ComponentInitializer initializer = comp.getCapability(ComponentInitializer.class);
 		initializer.setOwner(owner);
 
 		if (xmlComp.getComponentRefs() != null && 
 				xmlComp.getComponentRefs().size() != 0) {
 			// Import a referenced component
 			
 			for (ComponentRefType xmlCompRef : xmlComp.getComponentRefs()) {
 				AbstractComponent refComp = persistenceService.getComponent(
 						xmlCompRef.getExternalKey(), xmlCompRef.getClassType());
 
 				if (refComp == null) {
 					// Add type, ID, and external key to list of bad references
 					String refInfo = "Type: " + 
 					        getSimpleClassName(xmlCompRef.getClassType()) + 
 					        ", ID: " + xmlCompRef.getComponentId() + 
 					        ", Key: " + xmlCompRef.getExternalKey();
 					badReferences.add(refInfo);
 					LOGGER.error(missingRefMsg + ": " + refInfo);
 				} else {
 					comp.addDelegateComponent(refComp);
 				}
 			}
 		}
 
 		unmarshalModelState(comp, xmlComp);
 		unmarshalViewState(initializer, xmlComp);
 		
 		comp.save();
 
 		return comp;
 	}
 	
 	private String getSimpleClassName(String className) {
 		int classNamePos = className.lastIndexOf(".") + 1;
 		return className.substring(classNamePos);
 	}
 	
 	/**
 	 * Set name of new containing component to be:  "Imported on #date#"
 	 * @return name of containing component
 	 */
 	private String getDatedName() {
 		DateFormat dfm = new SimpleDateFormat("MMM d HH:mm:ss z yyyy");
 		dfm.setTimeZone(TimeZone.getDefault());
 		Date now = new Date();
 		String myDateString = dfm.format(now);
 		return "Imported on " + myDateString;
 	}
 	
 	/**
 	 * Set owner of new AbstractComponents
 	 * @param comp AbstractComponent being created
 	 */
 	private void setOwner(AbstractComponent comp) {
 		ComponentInitializer initializer = comp
 				              .getCapability(ComponentInitializer.class);
 		initializer.setOwner(owner);
 	}
 
 	/**
 	 * Unmarshal the model state
 	 * @param comp
 	 * @param xmlComp
 	 */
 	private void unmarshalModelState(AbstractComponent comp, ComponentType xmlComp) {
 		ModelStatePersistence persister = comp.getCapability(ModelStatePersistence.class);
 		if (persister != null && xmlComp.getModelState() != null) {
             persister.setModelState(xmlComp.getModelState());
 		}
 	}
 	
 	/**
 	 * Unmarshal the view state
 	 * @param initializer
 	 * @param xmlComp
 	 */
 	private void unmarshalViewState(ComponentInitializer initializer, 
 			                      ComponentType xmlComp) {
 		for (NameValueType entry : xmlComp.getViewStates()) {
 			initializer.setViewRoleProperty(entry.getKey(), 
 					                        XMLPersistence.unmarshal(entry.getValue()));
 		}
 	}
 
 	/**
 	 * Creates a component representing the XML file that was parsed by the
 	 * loader. It will have the same name as the XML file.
 	 * 
 	 * @param file File that is represented by a component
 	 * @param parent Parent of file component
 	 * @return
 	 */
 	private AbstractComponent makeFileComponent(File file, AbstractComponent parent) {
 
 		ImportExportComponent fileComponent = registry.newInstance(
 				ImportExportComponent.class, parent);
 		String fullname = file.getName();
 		fileComponent.setDisplayName(fullname);
 		setOwner(fileComponent);
 
 		return fileComponent;
 	}
 	
 	/**
 	 * Estimate number of components to be processed, both converting to AbstactComponents
 	 * and adding their children to them.  This is for the progress bar.
 	 * @param xmlComps
 	 * @return Estimate of number of components
 	 */
 	private int estimateTotalSize(List<ComponentType> xmlComps) {
 		// Get number of components to be converted
 		int rv = xmlComps.size();
 		// Get number of child components to be added to parents
 		for (ComponentType xmlComp : xmlComps) {
 			if (xmlComp.getAssociatedComponents() != null) {
 				rv += xmlComp.getAssociatedComponents().size();
 			}
 		}
 
 		return rv;
 	}
 	
 	private void updateProgressBar() {
 		if (progressBar != null) {
 			float percentCount = (++currentCount / (float) totalSize) * 
 					               progressBar.getMaximum();
 			setProgress(Math.min((int)percentCount, progressBar.getMaximum()));
 		}
 	}
 	
 	private void showUnimportableMsg(File file, List<String> unloadedComps,
 			                         String baseMsg) {
 		StringBuilder msg = new StringBuilder();
 		msg.append("Import of ");
 		msg.append(file);
 		msg.append(":\n" + baseMsg + "\nSee the log file for details.\n\n");
 
 		if (unloadedComps.size() < 11) {
 			msg.append("Unimportable objects:\n");
 		} else {
 			msg.append("First 10 unimportable objects:\n");
 		}
 		
 		// Only show first 10 bad components in dialog
 		int count = 1;
 		for (String badComp : unloadedComps) {
 			msg.append(badComp);
 			msg.append("\n");
 			count++;
 			if (count > 10) {
 				break;
 			}
 		}
 		dialogMgr.showMessageDialog(msg.toString(), 
 				bundle.getString("import_warning_message_title"), 
 				OptionBox.WARNING_MESSAGE);
 	}
 
 	/**
 	 * Gets the prefix for importExport meta data. This is used to delineate
 	 * this subset of metadata, for example, for views.
 	 * 
 	 * @return the importExport prefix
 	 */
 	@SuppressWarnings("unused")
 	private static String getPrefix() {
 		assert PREFIX != null;
 		return PREFIX + ".";
 	}
 
 	/**
 	 * Unit test enabler
 	 * 
 	 * @param dialogMgr
 	 */
 	void setDialogMgr(DialogMgr dialogMgr) {
 		this.dialogMgr = dialogMgr;
 	}
 
 	// Entry point for swing worker
 	@Override
 	public Void doInBackground() {
 		setProgress(0);
 		
 		// Ensure unit of work is done in a single database transaction
 		PersistenceProvider provider = 
 				            PlatformAccess.getPlatform().getPersistenceProvider();
 		provider.startRelatedOperations();
 		boolean completed = false;
 		try {
 	    	importComponents();
 	    	completed = true;
 		} finally {
 			provider.completeRelatedOperations(completed);
 		}
 		
 		return null;
 	}
 
 	@Override
 	public void done() {
 		if (progressBar != null) {
 		  progressBar.setValue(0);
 		}
 		if (jd != null) {
 		  jd.setVisible(false);
 		  jd.dispose();
 		}
 	}
 
 	/**
 	 * Class to hold, for each parent component, its converted MCT component, and its
 	 * list of child ids from the XML file.
 	 */
 	private class ParentComponent {
 		private AbstractComponent mctComp;
 		private List<AssociatedComponentType> xmlChildren;
 		
 		ParentComponent(AbstractComponent mctComp, 
 				        List<AssociatedComponentType> xmlChildren) {
 			this.mctComp = mctComp;
 			this.xmlChildren = xmlChildren;
 		}
 		
 		AbstractComponent getMctComp() {
 			return this.mctComp;
 		}
 		
 		List<AssociatedComponentType> getXmlChildren() {
 			return this.xmlChildren;
 		}
 	}
 		
 	/**
 	 * Class to hold, for each component (that's not a reference), its converted MCT 
 	 * component, and its id from the XML file.
 	 */
 	private class Component {
 		private AbstractComponent mctComp;
 		private String idFromXMLFile;
 		
 		Component(AbstractComponent mctComp, String idFromXMLFile) {
 	        this.mctComp = mctComp;
 	        this.idFromXMLFile = idFromXMLFile;
         }
 		
 		AbstractComponent getMctComp() {
 			return this.mctComp;
 		}
 		
 		String getIdFromXMLFile() {
 			return this.idFromXMLFile;
 		}
 	}
 	
 }
