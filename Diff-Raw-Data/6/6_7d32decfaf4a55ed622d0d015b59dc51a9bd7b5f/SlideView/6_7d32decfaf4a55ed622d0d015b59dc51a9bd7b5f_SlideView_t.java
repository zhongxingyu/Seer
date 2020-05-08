 /*******************************************************************************
  * Copyright (c) 2009 The Eclipse Foundation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    The Eclipse Foundation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.examples.slideshow.ui.views;
 
 import java.io.FileNotFoundException;
 
 import org.eclipse.examples.slideshow.core.Slide;
 import org.eclipse.examples.slideshow.core.SlideDeck;
 import org.eclipse.examples.slideshow.resources.ITemplate;
 import org.eclipse.examples.slideshow.resources.ResourceManager;
 import org.eclipse.examples.slideshow.runner.PDFRunner;
 import org.eclipse.examples.slideshow.runner.ScreenRunner;
 import org.eclipse.examples.slideshow.ui.Activator;
 import org.eclipse.examples.slideshow.ui.ISlideshowTemplateProvider;
 import org.eclipse.examples.slideshow.viewer.SlideViewer;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.viewers.ComboViewer;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.part.ViewPart;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.util.tracker.ServiceTracker;
 
 public class SlideView extends ViewPart {
 
 	private SlideViewer viewer;
 	private ServiceRegistration slideshowUIHandlerRegistration;
 	private Action runAction;
 	private SlideDeck deck;
 	private ITemplate template;
 	private ResourceManager resourceManager;
 	private ServiceTracker templateServiceTracker;
 	private ComboViewer templateList;
 	private Action printAction;
 	private Action refreshAction;
 	private Action pdfAction;
 
 	public void createPartControl(final Composite parent) {
 		parent.setLayout(new GridLayout(2, false));
 		
 		createTemplateListLabel(parent);
 		createTemplateList(parent);
 		createSlideViewer(parent);
 		
 		resourceManager = new ResourceManager(parent.getDisplay());
 		viewer.setResourceManager(resourceManager);
 
 		startSlideshowTemplateServiceTracker();		
 		registerSlideshowUIHandler();
 		
 		makeActions();
 		contributeToActionBars();
 	}
 
 	private void createTemplateListLabel(final Composite parent) {
 		Label label = new Label(parent, SWT.NONE);
 		label.setText("Template:");
 		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
 	}
 
 	private void createTemplateList(final Composite parent) {
 		templateList = new ComboViewer(parent);
 		templateList.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
 				ISlideshowTemplateProvider templateProvider = (ISlideshowTemplateProvider)selection.getFirstElement();
 				createTemplate(templateProvider);
 			}		
 		});
 		templateList.setLabelProvider(new LabelProvider() {
 			@Override
 			public String getText(Object element) {
 				return ((ISlideshowTemplateProvider)element).getName();
 			}
 		});
 		templateList.setSorter(new ViewerSorter() {
 			@Override
 			public int compare(Viewer viewer, Object e1, Object e2) {
 				String name1 = ((ISlideshowTemplateProvider)e1).getName();
 				String name2 = ((ISlideshowTemplateProvider)e2).getName();
 				
 				return name1.compareTo(name2);
 			}
 		});
 		templateList.getControl().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
 	}
 
 	private void createSlideViewer(final Composite parent) {
 		viewer = new SlideViewer(parent, SWT.NONE);
 		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
 		viewer.setInset(5);
 	}
 
 	void startSlideshowTemplateServiceTracker() {
 		templateServiceTracker = new ServiceTracker(Activator.getDefault().getContext(), ISlideshowTemplateProvider.class.getName(), null) {
 			@Override
 			public Object addingService(ServiceReference reference) {
 				final ISlideshowTemplateProvider provider = (ISlideshowTemplateProvider) super.addingService(reference);
 				getDisplay().syncExec(new Runnable() {
 					public void run() {
 						templateList.add(provider);
 						if (templateList.getSelection().isEmpty()) {
 							templateList.setSelection(new StructuredSelection(provider), true);
 						}
 					}					
 				});				
 				
 				return provider;
 			}
 			
 			@Override
 			public void removedService(ServiceReference reference, final Object service) {
 				if (templateList.getControl().isDisposed()) return;
 				getDisplay().syncExec(new Runnable() {
 					public void run() {
 						templateList.remove(service);
 
 						if (templateList.getSelection().isEmpty()) {
 							/*
 							 * It should be valid to assume that there is at least one
 							 * element in the list. This bundle registers on instance of
 							 * ISlideshowTemplateProvider, so that instance should be available. 
 							 */
 							templateList.setSelection(new StructuredSelection(templateList.getElementAt(0)), true);
 						}
 					}
 				});
 			}
 		};
 		templateServiceTracker.open();
 	}
 
 	private void createTemplate(ISlideshowTemplateProvider templateProvider) {
 		setTemplate(templateProvider == null ? null : templateProvider.createTemplate(getDisplay()));
 	}	
 
 	private Display getDisplay() {
 		return getSite().getWorkbenchWindow().getWorkbench().getDisplay();
 	}
 
 	protected void setTemplate(ITemplate template) {
 		if (this.template != null) template.dispose();
 		this.template = template;
 		viewer.setTemplate(template);
 	}
 
 	void registerSlideshowUIHandler() {
 		ISlideshowUIHandler handler = new ISlideshowUIHandler() {
 			public void slideDeckChanged(SlideDeck deck) {
 				setSlidedeck(deck);
 			}
 
 			public void slideChanged(Slide slide) {
 				setSlide(slide);
 			}
 		};
 		slideshowUIHandlerRegistration = getBundleContext().registerService(ISlideshowUIHandler.class.getName(), handler, null);
 	}
 
 	protected void setSlidedeck(SlideDeck deck) {
 		this.deck = deck;		
 	}
 
 
 	private BundleContext getBundleContext() {
 		return Activator.getDefault().getContext();
 	}
 	
 	@Override
 	public void dispose() {
 		templateServiceTracker.close();
 		slideshowUIHandlerRegistration.unregister();
 
 		if (template != null) template.dispose();
 		resourceManager.dispose();
 		super.dispose();
 	}
 
 	void setSlide(Slide slide) {
 		viewer.setSlide(slide);
 	}
 	
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	public void setFocus() {
 		viewer.getControl().setFocus();
 	}
 	
 	private void makeActions() {
 		createRunAction();
 		createPdfAction();
 		createPrintAction();
 		createRefreshAction();
 	}
 
 	private void createRunAction() {
 		runAction = new Action() {
 			public void run() {
 				runSlideShow();
 			}
 		};
 		runAction.setText("Run Slideshow");
 		runAction.setToolTipText("Run the Slideshow full screen");
 		runAction.setImageDescriptor(Activator.getImageDescriptor("icons/console_view.gif"));
 	}
 
 	private void createPrintAction() {
 		printAction = new Action() {
 			public void run() {
 				
 			}
 		};
 		printAction.setText("Print Slideshow");
 		printAction.setToolTipText("Prints the current Slideshow");
 		printAction.setImageDescriptor(Activator.getImageDescriptor("icons/print_edit.gif"));
 	}
 	
 	private void createPdfAction() {
 		pdfAction = new Action() {
 			public void run() {
 				exportToPdf();
 			}
 		};
 		pdfAction.setText("Generate PDF");
 		pdfAction.setToolTipText("Renders the current Slideshow into a PDF document");
 		pdfAction.setImageDescriptor(Activator.getImageDescriptor("icons/pdf.gif"));
 	}
 
 	private void createRefreshAction() {
 		refreshAction = new Action() {
 			public void run() {
 				viewer.refresh();
 			}
 		};
 		refreshAction.setText("Refresh");
 		refreshAction.setToolTipText("Redraws the current slide");
 		refreshAction.setImageDescriptor(Activator.getImageDescriptor("icons/refresh.gif"));
 	}
 	
 	private void contributeToActionBars() {
 		IActionBars bars = getViewSite().getActionBars();
 		bars.getToolBarManager().add(runAction);
 		bars.getToolBarManager().add(pdfAction);
 		bars.getToolBarManager().add(printAction);
 		bars.getToolBarManager().add(refreshAction);
 	}
 	
 
 	protected void runSlideShow() {
 		ScreenRunner runner = new ScreenRunner(viewer.getControl().getDisplay(), SWT.NONE);
 		runner.setTemplate(template);
 		runner.setResourceManager(resourceManager);
 		runner.setSlideshow(deck);
 		
 		runner.open();
 	}
 	
 	protected void exportToPdf() {
 		FileDialog dialog = new FileDialog(getSite().getShell(), SWT.SAVE | SWT.SINGLE);
 		dialog.setText("Export to PDF");
 		// TODO provide a default file name
		String pdfFile = dialog.open() ;
		if (pdfFile == null) return;
 		PDFRunner runner = new PDFRunner(getSite().getShell().getDisplay(), resourceManager, template);
 		try {
			runner.print(deck, pdfFile);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
