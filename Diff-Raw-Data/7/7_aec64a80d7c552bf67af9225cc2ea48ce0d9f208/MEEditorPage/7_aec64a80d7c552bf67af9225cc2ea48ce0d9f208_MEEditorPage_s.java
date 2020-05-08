 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.ecp.editor;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.Diagnostician;
 import org.eclipse.emf.ecp.common.commands.DeleteModelElementCommand;
 import org.eclipse.emf.ecp.common.model.ECPModelelementContext;
 import org.eclipse.emf.ecp.common.utilities.ShortLabelProvider;
 import org.eclipse.emf.ecp.editor.mecontrols.AbstractMEControl;
 import org.eclipse.emf.ecp.editor.mecontrols.FeatureHintTooltipSupport;
 import org.eclipse.emf.ecp.editor.mecontrols.IValidatableControl;
 import org.eclipse.emf.ecp.editor.mecontrols.METextControl;
 import org.eclipse.emf.edit.provider.AdapterFactoryItemDelegator;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ContributionManager;
 import org.eclipse.jface.fieldassist.ControlDecoration;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.SWTException;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.AbstractSourceProvider;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.ISourceProvider;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.editor.FormPage;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.menus.IMenuService;
 import org.eclipse.ui.services.IEvaluationService;
 
 /**
  * The editor page for the {@link MEEditor}.
  * 
  * @author helming
  * @author shterev
  * @author naughton
  */
 public class MEEditorPage extends FormPage {
 
 	private EObject modelElement;
 	private FormToolkit toolkit;
 	private Map<EStructuralFeature, AbstractMEControl> meControls = new HashMap<EStructuralFeature, AbstractMEControl>();
 
 	private Map<AbstractMEControl, Diagnostic> valdiatedControls = new HashMap<AbstractMEControl, Diagnostic>();
 	private static String activeModelelement = "activeModelelement";
 	private ScrolledForm form;
 	private List<IItemPropertyDescriptor> leftColumnAttributes = new ArrayList<IItemPropertyDescriptor>();
 	private List<IItemPropertyDescriptor> rightColumnAttributes = new ArrayList<IItemPropertyDescriptor>();
 	private List<IItemPropertyDescriptor> bottomAttributes = new ArrayList<IItemPropertyDescriptor>();
 	private Composite leftColumnComposite;
 	private Composite rightColumnComposite;
 	private Composite bottomComposite;
 	private EStructuralFeature problemFeature;
 	private final ECPModelelementContext modelElementContext;
 	private final ComposedAdapterFactory adapterFactory;
 	
 	/**
 	 * Default constructor.
 	 * 
 	 * @param editor
 	 *            the {@link MEEditor}
 	 * @param id
 	 *            the {@link FormPage#id}
 	 * @param title
 	 *            the title
 	 * @param modelElement
 	 *            the modelElement
 	 * @param modelElementContext
 	 *            the {@link ModelElementContext}
 	 */
 	public MEEditorPage(MEEditor editor, String id, String title, ECPModelelementContext modelElementContext,
 		EObject modelElement) {
 		super(editor, id, title);
 		this.modelElementContext = modelElementContext;
 		this.modelElement = modelElement;
 		this.adapterFactory = new ComposedAdapterFactory(
 				ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 
 	}
 
 	/**
 	 * Default constructor.
 	 * 
 	 * @param editor
 	 *            the {@link MEEditor}
 	 * @param id
 	 *            the {@link FormPage#id}
 	 * @param title
 	 *            the title
 	 * @param modelElement
 	 *            the modelElement
 	 * @param problemFeature
 	 *            the problemFeature
 	 * @param modelElementContext
 	 *            the {@link ModelElementContext}
 	 */
 	public MEEditorPage(MEEditor editor, String id, String title, ECPModelelementContext modelElementContext,
 		EObject modelElement, EStructuralFeature problemFeature) {
 		this(editor, id, title, modelElementContext, modelElement);
 		this.problemFeature = problemFeature;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void createFormContent(IManagedForm managedForm) {
 		super.createFormContent(managedForm);
 
 		toolkit = this.getEditor().getToolkit();
 		form = managedForm.getForm();
 		toolkit.decorateFormHeading(form.getForm());
 		Composite body = form.getBody();
 		body.setLayout(new GridLayout());
 		Composite topComposite = toolkit.createComposite(body);
 		topComposite.setLayout(new GridLayout());
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(topComposite);
 
 		sortAndOrderAttributes();
 		if (!rightColumnAttributes.isEmpty()) {
 			SashForm topSash = new SashForm(topComposite, SWT.HORIZONTAL);
 			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(topSash);
 			toolkit.adapt(topSash, true, true);
 			topSash.setSashWidth(4);
 			leftColumnComposite = toolkit.createComposite(topSash, SWT.NONE);
 			rightColumnComposite = toolkit.createComposite(topSash, SWT.NONE);
 			GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false).extendedMargins(5, 2, 5, 5)
 				.applyTo(rightColumnComposite);
 			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(rightColumnComposite);
 			int[] topWeights = { 50, 50 };
 			topSash.setWeights(topWeights);
 		} else {
 			leftColumnComposite = toolkit.createComposite(topComposite, SWT.NONE);
 		}
 
 		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false).extendedMargins(2, 5, 5, 5)
 			.applyTo(leftColumnComposite);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(leftColumnComposite);
 
 		bottomComposite = toolkit.createComposite(topComposite);
 		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false).extendedMargins(0, 0, 0, 0)
 			.applyTo(bottomComposite);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(bottomComposite);
 		// updateSectionTitle();
 		form.setImage(new AdapterFactoryLabelProvider(adapterFactory).getImage(modelElement));
 		// Sort and order attributes
 		// Create attributes
 		createAttributes(leftColumnComposite, leftColumnAttributes);
 		if (!rightColumnAttributes.isEmpty()) {
 			createAttributes(rightColumnComposite, rightColumnAttributes);
 		}
 		createAttributes(bottomComposite, bottomAttributes);
 		createToolbar();
 		form.pack();
 		updateSectionTitle();
 		updateLiveValidation();
 	}
 
 	/**
 	 * Updates the name of the section.
 	 */
 	public void updateSectionTitle() {
 		// Layout form
 		ShortLabelProvider shortLabelProvider = new ShortLabelProvider();
 		String name = shortLabelProvider.getText(modelElement);
 
 		name += " [" + modelElement.eClass().getName() + "]";
 		try {
 			form.setText(name);
 		} catch (SWTException e) {
 			// Catch in case editor is closed directly after change
 		}
 	}
 
 	private void createToolbar() {
 		IMenuService menuService = (IMenuService) PlatformUI.getWorkbench().getService(IMenuService.class);
 		ISourceProvider sourceProvider = new AbstractSourceProvider() {
 			public void dispose() {
 			}
 
 			@SuppressWarnings("rawtypes")
 			public Map getCurrentState() {
 				HashMap<Object, Object> map = new HashMap<Object, Object>();
 				map.put(activeModelelement, modelElement);
 				return map;
 			}
 
 			public String[] getProvidedSourceNames() {
 				String[] namens = new String[1];
 				namens[0] = activeModelelement;
 				return namens;
 			}
 
 		};
 
 		IEvaluationService service = (IEvaluationService) PlatformUI.getWorkbench()
 			.getService(IEvaluationService.class);
 		service.addSourceProvider(sourceProvider);
 		form.getToolBarManager().add(new Action("", Activator.getImageDescriptor("icons/delete.gif")) {
 
 			@Override
 			public void run() {
 				new DeleteModelElementCommand(modelElement, modelElementContext).run();
 			}
 		});
 		menuService.populateContributionManager((ContributionManager) form.getToolBarManager(),
 			"toolbar:org.eclipse.emf.ecp.editor.MEEditorPage");
 		form.getToolBarManager().update(true);
 	}
 	
 	/**
 	 * Filters attributes marked with "hidden=true" annotation
 	 * 
 	 * @param propertyDescriptors property descriptors to filter
 	 */
 	private void filterHiddenAttributes(Collection<IItemPropertyDescriptor> propertyDescriptors) {
 		Iterator<IItemPropertyDescriptor> iterator = propertyDescriptors.iterator();
 		
 		AnnotationHiddenDescriptor visibilityDescriptor = new AnnotationHiddenDescriptor();
 		
 		while (iterator.hasNext()) {
 			IItemPropertyDescriptor descriptor = iterator.next();
 			
 			if (visibilityDescriptor.getValue(descriptor, modelElement)) {
 				iterator.remove();
 			}
 		}
 	}
 
 	private void sortAndOrderAttributes() {
 
 		AdapterFactoryItemDelegator adapterFactoryItemDelegator = new AdapterFactoryItemDelegator(
 			adapterFactory);
 
 		Collection<IItemPropertyDescriptor> propertyDescriptors = adapterFactoryItemDelegator
 			.getPropertyDescriptors(modelElement);
 		
 		filterHiddenAttributes(propertyDescriptors);
 		
 		if (propertyDescriptors != null) {
 			AnnotationPositionDescriptor positionDescriptor = new AnnotationPositionDescriptor();
 			for (IItemPropertyDescriptor itemPropertyDescriptor : propertyDescriptors) {
 				String value = positionDescriptor.getValue(itemPropertyDescriptor, modelElement);
 				if (value.equals("left")) {
 					leftColumnAttributes.add(itemPropertyDescriptor);
 				} else if (value.equals("right")) {
 					rightColumnAttributes.add(itemPropertyDescriptor);
 				} else if (value.equals("bottom")) {
 					bottomAttributes.add(itemPropertyDescriptor);
 				} else {
 					leftColumnAttributes.add(itemPropertyDescriptor);
 				}
 			}
 
 			final HashMap<IItemPropertyDescriptor, Double> priorityMap = new HashMap<IItemPropertyDescriptor, Double>();
 			AnnotationPriorityDescriptor priorityDescriptor = new AnnotationPriorityDescriptor();
 			for (IItemPropertyDescriptor itemPropertyDescriptor : propertyDescriptors) {
 				priorityMap.put(itemPropertyDescriptor,
 					priorityDescriptor.getValue(itemPropertyDescriptor, modelElement));
 			}
 
 			Comparator<IItemPropertyDescriptor> comparator = new Comparator<IItemPropertyDescriptor>() {
 				public int compare(IItemPropertyDescriptor o1, IItemPropertyDescriptor o2) {
 					return Double.compare(priorityMap.get(o1), priorityMap.get(o2));
 				}
 			};
 			Collections.sort(leftColumnAttributes, comparator);
 			Collections.sort(rightColumnAttributes, comparator);
 			Collections.sort(bottomAttributes, comparator);
 
 		}
 
 	}
 
 	private void createAttributes(Composite column, List<IItemPropertyDescriptor> attributes) {
 		Composite attributeComposite = toolkit.createComposite(column);
 		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(attributeComposite);
 		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.BEGINNING).indent(10, 0)
 			.applyTo(attributeComposite);
 
 		ControlFactory controlFactory = new ControlFactory();
 
 		for (IItemPropertyDescriptor itemPropertyDescriptor : attributes) {
 			AbstractMEControl meControl = controlFactory.createControl(itemPropertyDescriptor, modelElement,
 				modelElementContext);
 			if (meControl == null) {
 				continue;
 			}
 			meControls.put((EStructuralFeature) itemPropertyDescriptor.getFeature(modelElement), meControl);
 			Control control;
 			if (meControl.getShowLabel()) {
 				Label label = toolkit.createLabel(attributeComposite,
 					itemPropertyDescriptor.getDisplayName(modelElement));
 				label.setData(modelElement);
 				FeatureHintTooltipSupport.enableFor(label, itemPropertyDescriptor);
 				control = meControl.createControl(attributeComposite, SWT.WRAP, itemPropertyDescriptor, modelElement,
 					modelElementContext, toolkit);
 				GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(label);
 				GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.BEGINNING).indent(10, 0)
 					.applyTo(control);
 				meControl.applyCustomLayoutData();
 			} else {
 				control = meControl.createControl(attributeComposite, SWT.WRAP, itemPropertyDescriptor, modelElement,
 					modelElementContext, toolkit);
 				control.setData(modelElement);
 				FeatureHintTooltipSupport.enableFor(control, itemPropertyDescriptor);
 				GridDataFactory.fillDefaults().span(2, 1).grab(true, true).align(SWT.FILL, SWT.BEGINNING).indent(10, 0)
 					.applyTo(control);
 			}
 			if (itemPropertyDescriptor.getFeature(modelElement) == problemFeature) {
 				ControlDecoration dec = new ControlDecoration(control, SWT.TOP | SWT.LEFT);
 				dec.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 				dec.setDescriptionText("Problem detected.");
 			}
 
 		}
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void dispose() {
 		for (AbstractMEControl control : meControls.values()) {
 			control.dispose();
 		}
 		if (adapterFactory!=null) {
 			adapterFactory.dispose();
 		}
 		super.dispose();
 	}
 
 	/**
 	 * {@inheritDoc} This method is added to solve the focus bug of navigator.
 	 * Every time that a ME is opened in editor, navigator has to lose focus so
 	 * that its action contributions are set correctly for next time.
 	 */
 	@Override
 	public void setFocus() {
 		super.setFocus();
 		// set keyboard focus on the first Text control
 		for (AbstractMEControl meControl : this.meControls.values()) {
 			if (meControl instanceof METextControl) {
 				((METextControl) meControl).setFocus();
 				return;
 			}
 		}
 		leftColumnComposite.setFocus();
 	}
 
 	/**
 	 * Triggers live validation of the model attributes.
 	 * **/
 	public void updateLiveValidation() {	
 		Diagnostic diagnostic = Diagnostician.INSTANCE.validate(modelElement);
 		List<AbstractMEControl> affectedControls = new ArrayList<AbstractMEControl>();
 		
 		for (Iterator<Diagnostic> i= diagnostic.getChildren().iterator(); i.hasNext();) {
 			Diagnostic childDiagnostic = i.next();
 			AbstractMEControl meControl = this.meControls.get(childDiagnostic.getData().get(1));
 			affectedControls.add(meControl);
 			if (meControl instanceof IValidatableControl) { 
 				if (this.valdiatedControls.containsKey(meControl)) {
 					if (childDiagnostic.getSeverity() != this.valdiatedControls.get(meControl).getSeverity()) {
 						((IValidatableControl)meControl).handleValidation(childDiagnostic);
 						this.valdiatedControls.put(meControl, childDiagnostic);
 					} 
 				} else {
 					((IValidatableControl)meControl).handleValidation(childDiagnostic);
 					this.valdiatedControls.put(meControl, childDiagnostic);
 				}
 			}
 		}
 		
 		Map<AbstractMEControl, Diagnostic> temp = new HashMap<AbstractMEControl, Diagnostic>();
 		temp.putAll(this.valdiatedControls);
 		for (Map.Entry<AbstractMEControl, Diagnostic> entry : temp.entrySet()) {
 			AbstractMEControl meControl = entry.getKey();
 			if (!affectedControls.contains(meControl)) {
 				this.valdiatedControls.remove(meControl);
 				((IValidatableControl)meControl).resetValidation();
 			}
 		}
 	}
 
 }
