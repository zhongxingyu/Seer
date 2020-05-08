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
 package org.eclipse.rmf.reqif10.pror.util;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.command.AddCommand;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.provider.ItemProviderAdapter;
 import org.eclipse.rmf.reqif10.AttributeDefinition;
 import org.eclipse.rmf.reqif10.AttributeValue;
 import org.eclipse.rmf.reqif10.AttributeValueEnumeration;
 import org.eclipse.rmf.reqif10.DatatypeDefinition;
 import org.eclipse.rmf.reqif10.EnumValue;
 import org.eclipse.rmf.reqif10.ReqIF;
 import org.eclipse.rmf.reqif10.SpecElementWithAttributes;
 import org.eclipse.rmf.reqif10.SpecHierarchy;
 import org.eclipse.rmf.reqif10.SpecType;
 import org.eclipse.rmf.reqif10.Specification;
 import org.eclipse.rmf.reqif10.common.util.ReqIF10Util;
 import org.eclipse.rmf.reqif10.common.util.ReqIFToolExtensionUtil;
 import org.eclipse.rmf.reqif10.pror.configuration.Column;
 import org.eclipse.rmf.reqif10.pror.configuration.ConfigurationFactory;
 import org.eclipse.rmf.reqif10.pror.configuration.ConfigurationPackage;
 import org.eclipse.rmf.reqif10.pror.configuration.LabelConfiguration;
 import org.eclipse.rmf.reqif10.pror.configuration.ProrGeneralConfiguration;
 import org.eclipse.rmf.reqif10.pror.configuration.ProrPresentationConfiguration;
 import org.eclipse.rmf.reqif10.pror.configuration.ProrPresentationConfigurations;
 import org.eclipse.rmf.reqif10.pror.configuration.ProrSpecViewConfiguration;
 import org.eclipse.rmf.reqif10.pror.configuration.ProrToolExtension;
 import org.eclipse.rmf.reqif10.util.ReqIF10Switch;
 
 public class ConfigurationUtil {
 
 	public static final String DEFAULT_LEFT_HEADER_COLUMN_NAME = "Lead Header Column";
 	public static final int DEFAULT_LEFT_HEADER_COLUMN_WIDTH = 30;
 
 	/**
 	 * @return The Configuration element for the given
 	 *         {@link DatatypeDefinition} or null if none is configured.
 	 */
 	public static ProrPresentationConfiguration getPresentationConfiguration(
 			DatatypeDefinition dd) {
 		ReqIF reqif = ReqIF10Util.getReqIF(dd);
 		if (reqif == null)
 			return null;
 		ProrToolExtension prorToolExtension = getProrToolExtension(reqif);
 		if (prorToolExtension == null)
 			return null;
 		ProrPresentationConfigurations extensions = prorToolExtension
 				.getPresentationConfigurations();
 		if (extensions == null)
 			return null;
 		for (ProrPresentationConfiguration config : extensions
 				.getPresentationConfigurations()) {
 			if (dd.equals(config.getDatatype()))
 				return config;
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the {@link ProrPresentationConfiguration} that is associated with
 	 * the {@link DatatypeDefinition} of the given {@link AttributeValue}. If
 	 * either intermediate element is null, null is returned.
 	 */
 	public static ProrPresentationConfiguration getPresentationConfiguration(
 			AttributeValue av) {
 		DatatypeDefinition dd = ReqIF10Util.getDatatypeDefinition(av);
 		if (av != null) {
 			return getPresentationConfiguration(dd);
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the {@link ProrToolExtension} associated with this {@link ReqIF}.
 	 * If it doesn't exist yet, null is returned.
 	 * <p>
 	 */
 	public static ProrToolExtension getProrToolExtension(ReqIF reqif) {
 		if (reqif != null) {
 			List<ProrToolExtension> extensions = ReqIFToolExtensionUtil
 					.getToolExtensionsByType(reqif,
 							ConfigurationPackage.eINSTANCE
 									.getProrToolExtension());
 			if (0 < extensions.size()) {
 				return extensions.get(0);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the {@link ProrToolExtension} associated with this {@link ReqIF}.
 	 * If it doesn't exist yet, it is created.
 	 * <p>
 	 */
 	public static ProrToolExtension createProrToolExtension(ReqIF reqif, EditingDomain domain) {
 		ProrToolExtension extension = getProrToolExtension(reqif);
 		if (extension != null)
 			return extension;
 		extension = ConfigurationFactory.eINSTANCE.createProrToolExtension();
 		domain.getCommandStack().execute(
 				ReqIFToolExtensionUtil.getAddToolExtensionCommand(reqif,
 						extension));
 		return extension;
 	}
 
 	/**
 	 * Returns the left header {@link Column} (which shows the hierarchy level)
 	 * associated with this {@link Specification}. If it doesn't exist yet, it
 	 * is created.
 	 */
 	public static Column getLeftHeaderColumn(Specification specification,
 			EditingDomain domain) {
 
 		ProrSpecViewConfiguration specViewConfiguration = createSpecViewConfiguration(
 				specification, domain);
 
 		Column leftHeaderColumn = specViewConfiguration.getLeftHeaderColumn();
 
 		if (leftHeaderColumn == null) {
 			leftHeaderColumn = ConfigurationFactory.eINSTANCE.createColumn();
 			leftHeaderColumn.setLabel(DEFAULT_LEFT_HEADER_COLUMN_NAME);
 			leftHeaderColumn.setWidth(DEFAULT_LEFT_HEADER_COLUMN_WIDTH);
 			specViewConfiguration.setLeftHeaderColumn(leftHeaderColumn);
 		}
 
 		return leftHeaderColumn;
 
 	}
 
 	/**
 	 * Retrieves all active {@link PresentationEditInterface} instances for the
 	 * given object (which assumes to be part of a ReqIF model). If none is
 	 * found, an empty set is returned.
 	 */
 	public static Set<PresentationEditInterface> getPresentationEditInterfaces(
 			Object obj, AdapterFactory adapterFactory) {
 		ReqIF reqif = ReqIF10Util.getReqIF(obj);
 		ProrPresentationConfigurations configs = getPresentationConfigurations(reqif);
 		if (configs != null) {
 			Set<PresentationEditInterface> result = new HashSet<PresentationEditInterface>();
 			for (ProrPresentationConfiguration config : configs
 					.getPresentationConfigurations()) {
 				ItemProviderAdapter ip = ProrUtil.getItemProvider(
 						adapterFactory, config);
 				if (ip instanceof PresentationEditInterface) {
 					result.add((PresentationEditInterface) ip);
 				}
 			}
 			return result;
 		}
 		return Collections.emptySet();
 	}
 
 	/**
 	 * Finds the best labels, according to what is set in the preferences.
 	 * 
 	 * @param specElement
 	 * @param adapterFactory
 	 * @param adapterFactory
 	 * @return
 	 */
 	public static String getSpecElementLabel(
 			SpecElementWithAttributes specElement, AdapterFactory adapterFactory) {
 
 		List<String> labels = getDefaultLabels(ReqIF10Util
 				.getReqIF(specElement));
 
 		// Iterate over the list of labels requested
 		for (String label : labels) {
 
 			for (AttributeValue value : specElement.getValues()) {
 				AttributeDefinition ad = ReqIF10Util
 						.getAttributeDefinition(value);
 				if (ad == null)
 					continue;
 
 				if (label.equals(ad.getLongName())) {
 					ProrPresentationConfiguration config = getPresentationConfig(value);
 
					ItemProviderAdapter ip = ProrUtil.getItemProvider(
							adapterFactory, config);
 					if (ip instanceof PresentationEditInterface) {
 						String customLabel = ((PresentationEditInterface) ip)
 								.getLabel(value);
 						if (customLabel != null)
 							return customLabel;
 					}
 
 					Object result = ReqIF10Util.getTheValue(value);
 					if (result != null) {
 
 						// If we have an enumeration attribute
 						if (value instanceof AttributeValueEnumeration
 								&& result instanceof EList) {
 							EList<?> list = (EList<?>) result;
 							if (!list.isEmpty())
 								return ((EnumValue) list.get(0)).getLongName();
 							else
 								return "";
 						}
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
 	public static List<String> getDefaultLabels(ReqIF reqif) {
 		ProrToolExtension extension = getProrToolExtension(reqif);
 		if (extension == null) {
 			return Collections.emptyList();
 		}
 		ProrGeneralConfiguration generalConfig = extension
 				.getGeneralConfiguration();
 		if (generalConfig == null) {
 			return Collections.emptyList();
 		}
 
 		LabelConfiguration labelConfig = generalConfig.getLabelConfiguration();
 		if (labelConfig == null) {
 			return Collections.emptyList();
 		}
 
 		return labelConfig.getDefaultLabel();
 	}
 
 	/**
 	 * Retrieves the {@link ProrSpecViewConfiguration} for the given
 	 * {@link Specification}. If none exists, it is built. The builder collects
 	 * all attribute names of all SpecObjects and creates corresponding columns.
 	 */
 	public static ProrSpecViewConfiguration createSpecViewConfiguration(
 			Specification specification, EditingDomain domain) {
 		ProrToolExtension extension = createProrToolExtension(ReqIF10Util.getReqIF(specification), domain);
 	
 		EList<ProrSpecViewConfiguration> configs = extension
 				.getSpecViewConfigurations();
 		for (ProrSpecViewConfiguration config : configs) {
 			if (config.getSpecification() != null
 					&& config.getSpecification().equals(specification)) {
 				return config;
 			}
 		}
 
 		// None found, let's build a new one that includes all attribute names.
 		ProrSpecViewConfiguration specViewConfig = ConfigurationFactory.eINSTANCE
 				.createProrSpecViewConfiguration();
 		specViewConfig.setSpecification(specification);
 
 		// Collect all Types
 		final Set<SpecType> types = new HashSet<SpecType>();
 		ReqIF10Switch<SpecHierarchy> visitor = new ReqIF10Switch<SpecHierarchy>() {
 			@Override
 			public SpecHierarchy caseSpecHierarchy(SpecHierarchy specHierarchy) {
 				if (specHierarchy.getObject() != null
 						&& specHierarchy.getObject().getType() != null) {
 					// Duplicates will disappear due to HashSet
 					types.add(specHierarchy.getObject().getType());
 				}
 				return specHierarchy;
 			}
 		};
 		for (Iterator<EObject> i = EcoreUtil
 				.getAllContents(specification, true); i.hasNext();) {
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
 			Column column = ConfigurationFactory.eINSTANCE.createColumn();
 			column.setWidth(100);
 			column.setLabel(colname);
 			specViewConfig.getColumns().add(column);
 		}
 		domain.getCommandStack()
 				.execute(
 						AddCommand
 								.create(domain,
 										extension,
 										ConfigurationPackage.Literals.PROR_TOOL_EXTENSION__SPEC_VIEW_CONFIGURATIONS,
 										specViewConfig));
 
 		return specViewConfig;
 
 	}
 
 	public static ProrPresentationConfiguration getPresentationConfig(
 			AttributeValue value) {
 		DatatypeDefinition dd = ReqIF10Util.getDatatypeDefinition(value);
 		ProrPresentationConfiguration config = getPresentationConfiguration(dd);
 		return config;
 	}
 
 	/**
 	 * @return the {@link ProrPresentationConfigurations} for the given
 	 *         {@link ReqIf} and {@link EditingDomain}.
 	 */
 	public static ProrPresentationConfigurations getPresentationConfigurations(
 			ReqIF reqif) {
 		ProrToolExtension uiExtension = ConfigurationUtil
 				.getProrToolExtension(reqif);
 		return uiExtension == null ? null : uiExtension
 				.getPresentationConfigurations();
 	}
 
 }
