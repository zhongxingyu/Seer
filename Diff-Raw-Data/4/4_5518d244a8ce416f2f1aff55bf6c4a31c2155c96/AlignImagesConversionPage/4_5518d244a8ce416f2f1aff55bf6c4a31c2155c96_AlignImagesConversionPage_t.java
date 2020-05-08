 /*-
  * Copyright (c) 2015 Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.dawnsci.conversion.ui.pages;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.dawb.common.services.conversion.IConversionContext;
 import org.dawb.common.ui.alignment.AlignProgressJob;
 import org.dawb.common.ui.wizard.ResourceChoosePage;
 import org.dawb.common.util.io.FileUtils;
 import org.dawnsci.conversion.converters.AlignImagesConverter.ConversionAlignBean;
 import org.dawnsci.conversion.ui.IConversionWizardPage;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.IJobChangeEvent;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.core.runtime.jobs.JobChangeAdapter;
 import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
 import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
 import org.eclipse.dawnsci.analysis.dataset.roi.PerimeterBoxROI;
 import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
 import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
 import org.eclipse.dawnsci.plotting.api.PlotType;
 import org.eclipse.dawnsci.plotting.api.PlottingFactory;
 import org.eclipse.dawnsci.plotting.api.region.IRegion;
 import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Scale;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.image.AlignMethod;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 
 /**
  * 
  * 
  * @author wqk87977
  *
  */
 public class AlignImagesConversionPage extends ResourceChoosePage
 		implements IConversionWizardPage {
 
 	private static final Logger logger = LoggerFactory.getLogger(AlignImagesConversionPage.class);
 
 	private IConversionContext context;
 	private IPlottingSystem plotSystem;
 	private Combo alignMethodCombo;
 
 	private IDataset firstImage;
 	private List<IDataset> data;
 	private List<IDataset> aligned;
 
 	private AlignMethod alignState = AlignMethod.WITH_ROI;
 	private static final String ROI_NAME = "align ROI";
 
 	private AlignProgressJob alignProgressJob;
 	private Button align;
 
 	private List<Button> radioButtons;
 	private int mode = 4; // Should be 2 or four
 
 	private Scale scaleProgress;
 	private int currentPosition;
 	private List<Button> sliderButtons;
 	private boolean showCorrected = false;
 
 	private Job loadDataJob;
 
 	public AlignImagesConversionPage() {
 		super("Convert image directory", null, null);
 		setDirectory(true);
 		setFileLabel("Aligned images output folder");
 		setNewFile(true);
 		setOverwriteVisible(true);
 		setPathEditable(true);
 		setDescription("Returns an aligned stack of images given a stack of images");
 	}
 
 	@Override
 	public IConversionContext getContext() {
 		if (context == null)
 			return null;
 		context.setOutputPath(getAbsoluteFilePath());
 		final File dir = new File(getSourcePath(context)).getParentFile();
 		context.setWorkSize(dir.list().length);
 		
 		final ConversionAlignBean bean = new ConversionAlignBean();
 		
 		String[] filePaths = getSelectedPaths();
 		if (data == null)
 			data = loadData(filePaths);
 		String[] datasetNames = new String[aligned.size()];
 		for (int i = 0; i < aligned.size(); i++) {
 			datasetNames[i] = aligned.get(i).getName();
 		}
 		context.setDatasetNames(datasetNames);
 
 		bean.setAligned(aligned);
 		context.setUserObject(bean);
 
 		return context;
 	}
 
 	private List<IDataset> loadData(final String[] filePaths) {
 		final List<IDataset> data = new ArrayList<IDataset>();
 		if (loadDataJob == null) {
 			loadDataJob = new Job("Loading image stack") {
 				@Override
 				protected IStatus run(IProgressMonitor monitor) {
 					for (int i = 0; i < filePaths.length; i++) {
 						IDataHolder holder = null;
 						try {
 							holder = LoaderFactory.getData(filePaths[i]);
							File file = new File(filePaths[i]);
							String filename = file.getName();
 							IDataset dataset = holder.getDataset(0);
 							if (dataset.getName() == null || dataset.getName().equals("")) {
 								dataset.setName(filename);
 							}
 							data.add(dataset);
 						} catch (Exception e) {
 							logger.error("Failed to load dataset:" + e);
 							return Status.CANCEL_STATUS;
 						}
 					}
 					return Status.OK_STATUS;
 				}
 			};
 			loadDataJob.addJobChangeListener(new JobChangeAdapter() {
 				@Override
 				public void done(IJobChangeEvent event) {
 					Display.getDefault().syncExec(new Runnable() {
 						@Override
 						public void run() {
 							if (scaleProgress != null) {
 								scaleProgress.setMaximum(data.size());
 								scaleProgress.redraw();
 							}
 						}
 					});
 				}
 			});
 		}
 		loadDataJob.schedule();
 		return data;
 	}
 
 	@Override
 	protected void createContentAfterFileChoose(Composite container) {
 		Composite controlComp = new Composite(container, SWT.NONE);
 		controlComp.setLayout(new GridLayout(2, false));
 		controlComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 4, 1));
 
 		// Check type of data and load first image
 		if (getSelectedPaths() == null)
 			return;
 		String filePath = getSelectedPaths()[0];
 		try {
 			IDataHolder holder = LoaderFactory.getData(filePath);
 			firstImage = holder.getDataset(0);
 		} catch (Exception e) {
 			logger.error("Error loading file:" + e.getMessage());
 		}
 		Label methodLabel = new Label(controlComp, SWT.NONE);
 		methodLabel.setText("Align method:");
 		alignMethodCombo = new Combo(controlComp, SWT.READ_ONLY);
 		alignMethodCombo.setItems(new String[] {"With ROI", "Affine transform"});
 		alignMethodCombo.setToolTipText("Choose the method of alignement: with a region of interest or without using an affine transformation");
 		alignMethodCombo.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent event) {
 				alignState = AlignMethod.getAlignMethod(alignMethodCombo.getSelectionIndex());
 				IRegion region = getRegion(ROI_NAME);
 				if (region != null) {
 					boolean withROI = alignState == AlignMethod.WITH_ROI;
 					region.setVisible(withROI);
 					radioButtons.get(0).setEnabled(withROI);
 					radioButtons.get(1).setEnabled(withROI);
 				}
 			}
 		});
 		// set default selection
 		alignMethodCombo.select(AlignMethod.WITH_ROI.getIdx());
 
 		Label modeLabel = new Label(controlComp, SWT.NONE);
 		modeLabel.setText("Mode:");
 		final Composite modeComp = new Composite(controlComp, SWT.NONE);
 		modeComp.setLayout(new RowLayout());
 		modeComp.setToolTipText("Number of columns used for image alignment with ROI");
 		Button b;
 		radioButtons = new ArrayList<Button>();
 		b = new Button(modeComp, SWT.RADIO);
 		b.setText("2");
 		b.setToolTipText("Use 2 columns to implement image alignment with ROI");
 		b.addSelectionListener(radioListener);
 		radioButtons.add(b);
 		b = new Button(modeComp, SWT.RADIO);
 		b.setText("4");
 		b.setToolTipText("Use 4 columns to implement image alignment with ROI");
 		b.addSelectionListener(radioListener);
 		radioButtons.add(b);
 		b.setSelection(true);
 
 		align = new Button(controlComp, SWT.NONE);
 		align.setText("Align");
 		align.setToolTipText("Run the alignment calculation with the corresponding alignment type chosen");
 		GridData gd = new GridData(SWT.CENTER, SWT.CENTER, true, false, 2, 1);
 		align.setLayoutData(gd);
 		align.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (alignState == AlignMethod.WITH_ROI) {
 					// if there is a ROI on the plotting system we can perform an align calculation
 					IRegion region = getRegion(ROI_NAME);
 					if (region != null && region.getROI() instanceof RectangularROI) {
 						align((RectangularROI)region.getROI());
 					} else {
 						String[] dialogButtonLabel = { "OK" };
 						Image warning = new Image(Display.getCurrent(), getClass().getResourceAsStream(
 								"/icons/warning_small.gif"));
 						MessageDialog messageDialog = new MessageDialog(Display.getCurrent().getActiveShell(),
 								"Error running image alignment", warning, "", MessageDialog.WARNING, dialogButtonLabel, 0);
 						messageDialog.open();
 					}
 				} else if (alignState == AlignMethod.AFFINE_TRANSFORM) {
 					align(null);
 				}
 			}
 		});
 
 		Group sliderGroup = new Group(container, SWT.NONE);
 		sliderGroup.setText("Image stack slicing");
 		sliderGroup.setToolTipText("Use the slider to browse through the original " +
 				"loaded images or through the corrected images");
 		sliderGroup.setLayout(new GridLayout(1, false));
 		sliderGroup.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 4, 1));
 
 		sliderButtons = new ArrayList<Button>();
 		b = new Button(sliderGroup, SWT.RADIO);
 		b.setText("Original Data");
 		b.setToolTipText("Show original stack of images");
 		b.addSelectionListener(sliderListener);
 		sliderButtons.add(b);
 		b.setSelection(true);
 		b = new Button(sliderGroup, SWT.RADIO);
 		b.setText("Aligned Data");
 		b.setToolTipText("Show aligned stack of images");
 		b.addSelectionListener(sliderListener);
 		sliderButtons.add(b);
 		sliderButtons.get(1).setEnabled(false);
 		
 		scaleProgress = new Scale(sliderGroup, SWT.HORIZONTAL);
 		scaleProgress.setPageIncrement(1);
 		scaleProgress.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				int p = scaleProgress.getSelection();
 				if (p != currentPosition) {
 					currentPosition = p;
 					if (showCorrected) {
 						if (aligned != null && aligned.size() > 0 && p < aligned.size()) {
 							plotSystem.updatePlot2D(aligned.get(p), null, null);
 						}
 					} else {
 						if (data != null && data.size() > 0 && p < data.size())
 							plotSystem.updatePlot2D(data.get(p), null, null);
 					}
 				}
 			}
 		});
 		scaleProgress.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
 		if (data != null) {
 			scaleProgress.setMaximum(data.size());
 			scaleProgress.redraw();
 		}
 
 		Composite plotComp = new Composite(container, SWT.NONE);
 		plotComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
 		plotComp.setLayout(new GridLayout(1, false));
 
 		Composite subComp = new Composite(plotComp, SWT.NONE);
 		subComp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
 		subComp.setLayout(new GridLayout(2, false));
 		
 		Label description = new Label(subComp, SWT.WRAP);
 		description.setText("Press 'Align' to register the stack of images loaded then "
 				+ "press 'Finish' to save all aligned images in the output folder of your choice.");
 		description.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		try {
 			plotSystem = PlottingFactory.createPlottingSystem();
 			plotSystem.createPlotPart(plotComp, "Preprocess", null, PlotType.IMAGE, null);
 			plotSystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 			
 			plotSystem.createPlot2D(firstImage, null, null);
 			plotSystem.setKeepAspect(true);
 			createRegion(firstImage, ROI_NAME);
 		} catch (Exception e) {
 			logger.error("Error creating the plotting system:" + e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	private SelectionListener radioListener = new SelectionAdapter() {
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			Button btn = (Button) e.widget;
 			if (!btn.getSelection()) {
 				btn.setSelection(false);
 			} else {
 				mode = radioButtons.indexOf(btn) == 0 ? 2 : 4;
 			}
 		}
 	};
 
 	private SelectionListener sliderListener = new SelectionAdapter() {
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			Button btn = (Button) e.widget;
 			if (!btn.getSelection()) {
 				btn.setSelection(false);
 			} else {
 				boolean showOriginal = sliderButtons.indexOf(btn) == 0;
 				if (showOriginal && data != null && data.size() > 0) {
 					plotSystem.updatePlot2D(data.get(currentPosition), null, null);
 					showCorrected = false;
 				} else if (!showOriginal && aligned != null && aligned.size() > 0){
 					plotSystem.updatePlot2D(aligned.get(currentPosition), null, null);
 					showCorrected = true;
 				} else if (!showOriginal && aligned == null) {
 					showCorrected = true;
 				}
 			}
 		}
 	};
 
 	private void align(final RectangularROI roi) {
 		if (alignProgressJob == null) {
 			alignProgressJob = new AlignProgressJob();
 		}
 		alignProgressJob.setRectangularROI(roi);
 		alignProgressJob.setMode(mode);
 		alignProgressJob.setData(data);
 		alignProgressJob.setAlignMethod(alignState);
 		ProgressMonitorDialog alignProgress = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
 		alignProgress.setCancelable(true);
 		try {
 			alignProgress.run(true, true, alignProgressJob);
 		} catch (InvocationTargetException e1) {
 			MessageDialog
 					.openError(
 							Display.getCurrent().getActiveShell(),
 							"Alignment Error",
 							"An error occured during alignment: "
 									+ e1.getTargetException()
 											.getLocalizedMessage());
 		} catch (InterruptedException e1) {
 			e1.printStackTrace();
 		}
 		aligned = alignProgressJob.getShiftedImages();
 		sliderButtons.get(1).setEnabled(aligned != null);
 		sliderButtons.get(0).setSelection(false);
 		sliderButtons.get(1).setSelection(true);
 		showCorrected = true;
 	}
 
 	private IRegion getRegion(String regionName) {
 		Collection<IRegion> regions = plotSystem.getRegions();
 		for (IRegion region : regions) {
 			if (region.getName().equals(regionName))
 				return region;
 		}
 		return null;
 	}
 
 	private void createRegion(IDataset data, String regionName) {
 		try {
 			Collection<IRegion> regions = plotSystem.getRegions();
 			for (IRegion region : regions) {
 				if (region.getName().equals(regionName))
 					return;
 			}
 			IRegion region = plotSystem.createRegion(regionName, RegionType.BOX);
 			double width = plotSystem.getAxes().get(0).getUpper() / 2;
 			double height = plotSystem.getAxes().get(1).getLower() / 2; // images by default have their ordinate up side down
 			RectangularROI rroi = new PerimeterBoxROI(0, 0, width, height, 0);
 			rroi.setName(regionName);
 			region.setROI(rroi);
 			plotSystem.addRegion(region);
 		} catch (Exception e) {
 			logger.error("Error creating Region Of Interest:", e);
 		}
 	}
 
 	@Override
 	public void setContext(IConversionContext context) {
 		if (context != null && context.equals(this.context))
 			return;
 
 		this.context = context;
 		setErrorMessage(null);
 		if (context == null) { // new context being prepared.
 			setPageComplete(false);
 			return;
 		}
 
 		final File dir = new File(getSourcePath(context)).getParentFile();
 		String uniqueDir = FileUtils.getUnique(dir, "Aligned_Images", null).getAbsolutePath();
 		setPath(uniqueDir);
 
 	}
 
 	@Override
 	public boolean isOpen() {
 		return true;
 	}
 
 	public void pathChanged() {
 		final String p = getAbsoluteFilePath();
 		if (p == null || p.length() == 0) {
 			setErrorMessage("Please select a folder to export to.");
 			return;
 		}
 		final File path = new File(p);
 		if (path.exists()) {
 			if (overwrite != null && !overwrite.getSelection()) {
 				setErrorMessage("The folder " + path.getName()
 						+ " already exists.");
 				return;
 			}
 
 			if (!path.canWrite()) {
 				setErrorMessage("Please choose another location to export to; this one is read only.");
 				return;
 			}
 		}
 		setErrorMessage(null);
 	}
 }
