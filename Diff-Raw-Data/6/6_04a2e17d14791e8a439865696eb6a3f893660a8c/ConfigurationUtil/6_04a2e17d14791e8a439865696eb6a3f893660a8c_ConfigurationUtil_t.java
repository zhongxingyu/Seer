 /*******************************************************************************
  * Copyright (c) 2011 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.pror.reqif10.util;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.command.AddCommand;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.rmf.pror.reqif10.configuration.Column;
 import org.eclipse.rmf.pror.reqif10.configuration.ConfigFactory;
 import org.eclipse.rmf.pror.reqif10.configuration.ConfigPackage;
 import org.eclipse.rmf.pror.reqif10.configuration.LabelConfiguration;
 import org.eclipse.rmf.pror.reqif10.configuration.ProrGeneralConfiguration;
 import org.eclipse.rmf.pror.reqif10.configuration.ProrPresentationConfiguration;
 import org.eclipse.rmf.pror.reqif10.configuration.ProrPresentationConfigurations;
 import org.eclipse.rmf.pror.reqif10.configuration.ProrSpecViewConfiguration;
 import org.eclipse.rmf.pror.reqif10.configuration.ProrToolExtension;
 import org.eclipse.rmf.reqif10.AttributeDefinition;
 import org.eclipse.rmf.reqif10.AttributeValue;
 import org.eclipse.rmf.reqif10.DatatypeDefinition;
 import org.eclipse.rmf.reqif10.ReqIf;
 import org.eclipse.rmf.reqif10.ReqIfToolExtension;
 import org.eclipse.rmf.reqif10.Reqif10Package;
 import org.eclipse.rmf.reqif10.SpecElementWithAttributes;
 import org.eclipse.rmf.reqif10.SpecHierarchy;
 import org.eclipse.rmf.reqif10.SpecType;
 import org.eclipse.rmf.reqif10.Specification;
 import org.eclipse.rmf.reqif10.util.Reqif10Switch;
 import org.eclipse.rmf.reqif10.util.Reqif10Util;
 
 public class ConfigurationUtil {
 	
 	/**
 	 * @return The Configuration element for the given
 	 *         {@link DatatypeDefinition} or null if none is configured.
 	 */
 	public static ProrPresentationConfiguration getConfiguration(
 			DatatypeDefinition datatypeDefinition, EditingDomain domain) {
 		ReqIf reqif = Reqif10Util.getReqIf(datatypeDefinition);
 		if (reqif == null)
 			return null;
 		ProrPresentationConfigurations extensions = getProrToolExtension(reqif, domain)
 				.getPresentationConfigurations();
 		if (extensions == null)
 			return null;
 		for (ProrPresentationConfiguration config : extensions
 				.getPresentationConfigurations()) {
 			if (datatypeDefinition.equals(config.getDatatype()))
 				return config;
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns the {@link ProrToolExtension} associated with this
 	 * {@link ReqIF}.  If it doesn't exist yet, null is returned.
 	 * <p>
 	 */
 	private static ProrToolExtension getProrToolExtension(ReqIf reqif) {
 		EList<ReqIfToolExtension> extensions = reqif.getToolExtensions();
 		for (ReqIfToolExtension extension : extensions) {
 			if (extension instanceof ProrToolExtension) {
 				return (ProrToolExtension) extension;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the {@link ProrToolExtension} associated with this
 	 * {@link ReqIF}.  If it doesn't exist yet, it is created.
 	 * <p>
 	 */
 	public static ProrToolExtension getProrToolExtension(ReqIf reqif, EditingDomain domain) {
 		ProrToolExtension extension = getProrToolExtension(reqif);
 		if (extension != null) return extension;
 		
 		extension = ConfigFactory.eINSTANCE.createProrToolExtension();
 		domain.getCommandStack().execute(
 				AddCommand.create(domain, reqif,
 						Reqif10Package.Literals.REQ_IF__TOOL_EXTENSIONS,
 						extension));		
 		return extension;
 	}
 	
 	/**
 	 * Finds the best labels, according to what is set in the preferences.
 	 * 
 	 * @param specElement
 	 * @return
 	 */
 	public static String getSpecElementLabel(
 			SpecElementWithAttributes specElement) {
 		
 		List<String> labels = getDefaultLabels(Reqif10Util.getReqIf(specElement));
 
 		// Iterate over the list of labels requested
 		for (String label : labels) {
 			
 			for (AttributeValue value : specElement.getValues()) {
 				// TODO eventually should also work for non-simple attributes
 				AttributeDefinition ad = Reqif10Util.getAttributeDefinition(value);
 				if (ad == null)
 					continue;
 
 				if (label.equals(ad.getLongName())) {
 
 					String customLabel = getCustomLabel(value);
 					if (customLabel != null) {
 						return customLabel;
 					}
 
 					Object result = Reqif10Util.getTheValue(value);
 					if (result != null) {
 						return result.toString();
 					}
 				}
 			}
 		}
 		return specElement.getIdentifier();
 	}
 
 	/**
 	 * Returns the list of default Label names.
 	 * 
 	 * @return always a list, sometimes empty.
 	 */
 	static List<String> getDefaultLabels(
 			ReqIf reqif) {
 		ProrToolExtension extension = getProrToolExtension(reqif);
 		if (extension == null) {
 			return Collections.emptyList();
 		}
 		ProrGeneralConfiguration generalConfig = extension.getGeneralConfiguration();
 		if (generalConfig == null) {
 			return Collections.emptyList();
 		}
 
 		LabelConfiguration labelConfig = generalConfig.getLabelConfiguration();
 		if (labelConfig == null) {
 			return Collections.emptyList();
 		}
 		
 		return labelConfig.getDefaultLabel();
 	}
 
 	private static String getCustomLabel(AttributeValue value) {
 		// TODO request custom label from PresentationPluginManager
 		return null;
 		// See whether we have a custom label renderer
 //		DatatypeDefinition dd = Reqif10Util.getDatatypeDefinition(value);
 //		ProrPresentationConfiguration manager = PresentationManager
 //				.getConfiguration(dd);
 //		if (manager != null) {
 //			PresentationService service = PresentationPluginManager
 //					.getPresentationService(manager);
 //			if (service != null) {
 //				String customLabel = service.getLabel(value);
 //				if (customLabel != null) {
 //					return customLabel;
 //				}
 //			}
 //		}
 //		return null;
 	}
 
 	/**
 	 * Retrieves the {@link ProrSpecViewConfiguration} for the given
 	 * {@link Specification}. If none exists, it is built. The builder collects
 	 * all attribute names of all SpecObjects and creates corresponding columns.
 	 * <p>
 	 */
 	public static ProrSpecViewConfiguration getSpecViewConfiguration(
 			Specification specification, EditingDomain domain) {
 		ProrToolExtension extension = getProrToolExtension(Reqif10Util.getReqIf(specification), domain);
 	
 		EList<ProrSpecViewConfiguration> configs = extension
 				.getSpecViewConfigurations();
 		for (ProrSpecViewConfiguration config : configs) {
 			if (config.getSpecification().equals(specification)) {
 				return config;
 			}
 		}
 	
 		// None found, let's build a new one that includes all attribute names.
 		ProrSpecViewConfiguration specViewConfig = ConfigFactory.eINSTANCE
 				.createProrSpecViewConfiguration();
 		specViewConfig.setSpecification(specification);
 		
 		// Collect all Types
 		final Set<SpecType> types = new HashSet<SpecType>();
 		Reqif10Switch<SpecHierarchy> visitor = new Reqif10Switch<SpecHierarchy>() {
 			@Override
 			public SpecHierarchy caseSpecHierarchy(SpecHierarchy specHierarchy) {
 				if (specHierarchy.getObject() != null && specHierarchy.getObject().getType() != null) {
 					// Duplicates will disappear due to HashSet
 					types.add(specHierarchy.getObject().getType());
 				}
 				return specHierarchy;
 			}
 		};
 		for (Iterator<EObject> i = EcoreUtil.getAllContents(specification, true); i.hasNext();) {
 			visitor.doSwitch(i.next());
 		}
 		
 		// Collect all names from the types
 		final Set<String> colnames = new HashSet<String>();
 		for (SpecType type : types) {
 			for (AttributeDefinition ad : type.getSpecAttributes()) {
 				// Duplicates will disappear due to HashSet
 				colnames.add(ad.getLongName());
 			}
 		}
 		
 		// Build all Columns from the names
 		for (String colname : colnames) {
 			Column column = ConfigFactory.eINSTANCE.createColumn();
 			column.setWidth(100);
 			column.setLabel(colname);
 			specViewConfig.getColumns().add(column);
 		}
 		domain.getCommandStack()
 				.execute(
 						AddCommand
 								.create(domain,
 										extension,
										ConfigPackage.Literals.PROR_TOOL_EXTENSION__SPEC_VIEW_CONFIGURATIONS,
										specViewConfig));
 
 		return specViewConfig;
 	}
 
 	public static ProrPresentationConfiguration getPresentationConfig(AttributeValue value, EditingDomain domain) {
 		DatatypeDefinition dd = Reqif10Util.getDatatypeDefinition(value);
 		ProrPresentationConfiguration config = getConfiguration(dd, domain);
 		return config;
 	}
 
 }
