 /*
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.rcp.views;
 
 import gda.observable.IObserver;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.math.complex.Complex;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IViewReference;
 import org.eclipse.ui.part.ViewPart;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Maths;
 import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
 import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
 import uk.ac.diamond.scisoft.analysis.plotserver.DataSetWithAxisInformation;
 import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
 import uk.ac.diamond.scisoft.analysis.plotserver.NexusDataBean;
 
 /**
  * Panel that observes a PlotView to show additional information
  * allow allows maths and moving of data
  */
 public class PlotViewStatsAndMaths extends ViewPart implements IObserver {
 	protected Composite parentComp;
 	protected DataBean dataBeanA, dataBeanB, currentBean;
 	protected PlotView plotView;
 	
 	private String doubleFormat = "%5.5g";
 	private Button btnPlotA;
 	private Button btnPlotAb;
 	private Button btnPlotB;
 	private Button btnPlotBa;
 	private Button btnPlotAnB;
 	private Button btnStoreCurrentAsA;
 	private Button btnStoreCurrentAsB;
 	private Composite composite;
 	private Combo spinner;
 	private Button btnNewButton;
 	private Label labelA;
 	private Label labelB;
 	private Text textMaxVal;
 	private Text textMaxPos;
 	private Text textMinVal;
 	private Text textMinPos;
 	private Text textSum;
 	private Text textMean;
 	private Map<String, PlotView> viewMap = new HashMap<String, PlotView>();
 	
 	private SelectionListener updatePlotList = new SelectionListener() {
 		
 		@Override
 		public void widgetSelected(SelectionEvent event) {
 			String selected = spinner.getText();
 			viewMap.clear();
 			IViewReference[] viewReferences = getSite().getPage().getViewReferences();
 			for (IViewReference viewref: viewReferences) {
 				IViewPart view = viewref.getView(false);
 				if (view != null && view instanceof PlotView) {
 					viewMap.put(view.getTitle(), (PlotView) view);
 				}
 			}
 			int i = 0, sel = 0;
 			spinner.setItems(new String[]{});
 			for (String label: viewMap.keySet()) {
 				spinner.add(label);
 				if (label.equals(selected)) sel = i;
 				i++;
 			}
 			spinner.select(sel);
 			updatePlotSelection.widgetSelected(null);
 		}
 		
 		@Override
 		public void widgetDefaultSelected(SelectionEvent e) {
 		}
 	};
 
 	private SelectionListener updatePlotSelection = new SelectionListener() {
 		
 		@Override
 		public void widgetSelected(SelectionEvent event) {
 			unobserve(plotView);
 			plotView = viewMap.get(spinner.getText());
 			if (plotView == null) // this can happen if bogus one is entered
 				return;
 			DataBean bean = plotView.getPlotWindow().getDataBean();
 			if (bean == null) {
 				btnStoreCurrentAsA.setEnabled(false);
 				btnStoreCurrentAsB.setEnabled(false);
 
 				textMaxVal.setText("");
 				textMaxPos.setText("");
 				textMinVal.setText("");
 				textMinPos.setText("");
 				textSum.setText("");
 				textMean.setText("");
 			} else {
 				update(plotView, bean);
 			}
 			observe(plotView);
 		}
 		
 		@Override
 		public void widgetDefaultSelected(SelectionEvent e) {
 		}
 	};
 
 	
 	private void observe(PlotView pv) {
 		if (pv != null) pv.addDataObserver(this);
 	}
 	private void unobserve(PlotView pv) {
 		if (pv != null) pv.deleteDataObserver(this);
 	}
 	
 	/**
 	 * Controls the server side display of live or near live data and displays statistics
 	 */
 	public PlotViewStatsAndMaths() {
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 
 
 		GridLayout layout = new GridLayout();
 
 		parent.setLayout(layout);
 		parentComp = parent;
 		
 		composite = new Composite(parent, SWT.NONE);
 		composite.setLayout(new RowLayout(SWT.HORIZONTAL));
 		
 		Label lblNewLabel = new Label(composite, SWT.NONE);
 		lblNewLabel.setText("Choose Plot View to operate on: ");
 		
 		spinner = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
 		spinner.setItems(new String[] {"                    "});
 		spinner.addSelectionListener(updatePlotSelection);
 		
 		btnNewButton = new Button(composite, SWT.NONE);
 		btnNewButton.setText("update choices");
 		btnNewButton.addSelectionListener(updatePlotList );
 
 		Composite grpMaths = new Composite(parent, SWT.NONE);
 		grpMaths.setLayout(new GridLayout(5, false));
 		grpMaths.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		btnStoreCurrentAsA = new Button(grpMaths, SWT.NONE);
 		btnStoreCurrentAsA.setText("Store current as A");
 		btnStoreCurrentAsA.setEnabled(false);
 		btnStoreCurrentAsA.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				dataBeanA = currentBean;
 				labelA.setText(getLabel(dataBeanA));
 				enableDisableMaths();
 			}
 		});
 
 		btnPlotA = new Button(grpMaths, SWT.NONE);
 		btnPlotA.setText("Plot A");
 		btnPlotA.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				pushToPlotView(dataBeanA);
 			}
 		});
 
 		btnPlotAb = new Button(grpMaths, SWT.NONE);
 		btnPlotAb.setText("Plot A-B");
 		btnPlotAb.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				pushToPlotView(dataBeanSubtract(dataBeanA, dataBeanB));
 			}
 		});
 
 		btnPlotAnB = new Button(grpMaths, SWT.NONE);
 		btnPlotAnB.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2));
 		btnPlotAnB.setText("Plot A + B");
 		btnPlotAnB.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				pushToPlotView(dataBeanAdd(dataBeanA, dataBeanB));
 			}
 		});
 		
 		labelA = new Label(grpMaths, SWT.NONE);
 		labelA.setText("                                                                                             ");
 
 		btnStoreCurrentAsB = new Button(grpMaths, SWT.NONE);
 		btnStoreCurrentAsB.setText("Store current as B");
 		btnStoreCurrentAsB.setEnabled(false);
 		btnStoreCurrentAsB.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				dataBeanB = currentBean;
 				labelB.setText(getLabel(dataBeanB));
 				enableDisableMaths();
 			}
 		});
 
 		btnPlotB = new Button(grpMaths, SWT.NONE);
 		btnPlotB.setText("Plot B");
 		btnPlotB.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				pushToPlotView(dataBeanB);
 			}
 		});
 
 		btnPlotBa = new Button(grpMaths, SWT.NONE);
 		btnPlotBa.setText("Plot B-A");
 		
 		labelB = new Label(grpMaths, SWT.NONE);
 		labelB.setText("                                                                                             ");
 		btnPlotBa.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				pushToPlotView(dataBeanSubtract(dataBeanB, dataBeanA));
 			}
 		});
 
 		Group grpStatistics = new Group(parent, SWT.NONE);
 		grpStatistics.setText("Statistics");
 		grpStatistics.setLayout(new GridLayout(3, false));
 		grpStatistics.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		
 		new Label(grpStatistics, SWT.NONE);
 
 		Label txtValue = new Label(grpStatistics, SWT.NONE);
 		txtValue.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
 		txtValue.setAlignment(SWT.CENTER);
 		txtValue.setText("value");
 
 		Label txtPosition = new Label(grpStatistics, SWT.NONE);
 		txtPosition.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
 		txtPosition.setAlignment(SWT.CENTER);
 		txtPosition.setText("position");
 
 		Label txtMax = new Label(grpStatistics, SWT.NONE);
 		txtMax.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
 		txtMax.setAlignment(SWT.RIGHT);
 		txtMax.setText("max");
 
 		textMaxVal = new Text(grpStatistics, SWT.RIGHT);
 		textMaxVal.setText("0");
 		GridData gd_textMaxVal = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
 		gd_textMaxVal.widthHint = 120;
 		textMaxVal.setLayoutData(gd_textMaxVal);
 		textMaxVal.setEditable(false);
 
 		textMaxPos = new Text(grpStatistics, SWT.RIGHT);
 		textMaxPos.setText("0");
 		GridData gd_textMaxPos = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
 		gd_textMaxPos.widthHint = 120;
 		textMaxPos.setLayoutData(gd_textMaxPos);
 		textMaxPos.setEditable(false);
 
 		Label txtMin = new Label(grpStatistics, SWT.NONE);
 		txtMin.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
 		txtMin.setAlignment(SWT.RIGHT);
 		txtMin.setText("min");
 
 		textMinVal = new Text(grpStatistics, SWT.RIGHT);
 		textMinVal.setText("0");
 		GridData gd_textMinVal = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
 		gd_textMinVal.widthHint = 120;
 		textMinVal.setLayoutData(gd_textMinVal);
 		textMinVal.setEditable(false);
 
 		textMinPos = new Text(grpStatistics, SWT.RIGHT);
 		textMinPos.setText("0");
 		GridData gd_textMinPos = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
 		gd_textMinPos.widthHint = 120;
 		textMinPos.setLayoutData(gd_textMinPos);
 		textMinPos.setEditable(false);
 
 		Label txtSum = new Label(grpStatistics, SWT.NONE);
 		txtSum.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
 		txtSum.setAlignment(SWT.RIGHT);
 		txtSum.setText("sum");
 
 		textSum = new Text(grpStatistics, SWT.RIGHT);
 		textSum.setText("0");
 		GridData gd_textSum = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
 		gd_textSum.widthHint = 120;
 		textSum.setLayoutData(gd_textSum);
 		textSum.setEditable(false);
 
 		new Label(grpStatistics, SWT.NONE);
 
 		Label txtMean = new Label(grpStatistics, SWT.NONE);
 		txtMean.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
 		txtMean.setAlignment(SWT.RIGHT);
 		txtMean.setText("mean");
 
 		textMean = new Text(grpStatistics, SWT.RIGHT);
 		textMean.setText("0");
 		GridData gd_textMean = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
 		gd_textMean.widthHint = 120;
 		textMean.setLayoutData(gd_textMean);
 		textMean.setEditable(false);
 		
 		new Label(grpStatistics, SWT.NONE);
 
 		enableDisableMaths();
 	}
 
 	private static String getLabel(DataBean db) {
 		AbstractDataset ds = db.getData().get(0).getData();
 		return ds.getName() + " ("+ formatIntArray(ds.getShape()) + ")";
 	}
 	
 	public static DataBean dataBeanSubtract(DataBean dataBean1, DataBean dataBean2) {
 		DataBean result = (dataBean1 instanceof NexusDataBean) ? new NexusDataBean() : new DataBean();
 
 		AbstractDataset dataSet1 = dataBean1.getData().get(0).getData();
 		AbstractDataset dataSet2 = dataBean2.getData().get(0).getData();
 
 		AbstractDataset dataSet = Maths.subtract(dataSet1, dataSet2);
 
 		AxisMapBean axismap = dataBean1.getData().get(0).getAxisMap();
 		List<DataSetWithAxisInformation> coll = new ArrayList<DataSetWithAxisInformation>();
 		DataSetWithAxisInformation dswai = new DataSetWithAxisInformation();
 		dswai.setAxisMap(axismap);
 		dswai.setData(dataSet);
 		coll.add(dswai);
 
 		result.setData(coll);
 		if (dataBean1 instanceof NexusDataBean)
 			((NexusDataBean) result).setNexusTrees(((NexusDataBean) dataBean1).getNexusTrees());
 		result.setAxisData(dataBean1.getAxisData());
 		return result;
 	}
 
 	public static DataBean dataBeanAdd(DataBean dataBean1, DataBean dataBean2) {
 		DataBean result = (dataBean1 instanceof NexusDataBean) ? new NexusDataBean() : new DataBean();
 
 		AbstractDataset dataSet1 = dataBean1.getData().get(0).getData();
 		AbstractDataset dataSet2 = dataBean2.getData().get(0).getData();
 		AbstractDataset dataSet = Maths.add(dataSet1, dataSet2);
 
 		AxisMapBean axismap = dataBean1.getData().get(0).getAxisMap();
 		List<DataSetWithAxisInformation> coll = new ArrayList<DataSetWithAxisInformation>();
 		DataSetWithAxisInformation dswai = new DataSetWithAxisInformation();
 		dswai.setAxisMap(axismap);
 		dswai.setData(dataSet);
 		coll.add(dswai);
 
 		result.setData(coll);
 		if (dataBean1 instanceof NexusDataBean)
 			((NexusDataBean) result).setNexusTrees(((NexusDataBean) dataBean1).getNexusTrees());
 		result.setAxisData(dataBean1.getAxisData());
 		return result;
 	}
 
 	protected void enableDisableMaths() {
 		btnPlotA.setEnabled(dataBeanA != null);
 		btnPlotB.setEnabled(dataBeanB != null);
 
 		boolean samerank = false;
 		if (dataBeanA != null && dataBeanB != null) {
 			int[] dimA = dataBeanA.getData().get(0).getData().getShape();
 			int[] dimB = dataBeanB.getData().get(0).getData().getShape();
 			samerank = Arrays.equals(dimA, dimB);
 		}
 		btnPlotAb.setEnabled(samerank);
 		btnPlotBa.setEnabled(samerank);
 		btnPlotAnB.setEnabled(samerank);
 	}
 
 	@Override
 	public void setFocus() {
 	}
 
 	@Override
 	public void update(Object theObserved, Object changeCode) {
 		if (parentComp.isDisposed()) {
 			try {
 				plotView.deleteIObserver(this);
 			} catch (Exception e) {
 				// ignored
 			}
 			return;
 		}
 
//		if (changeCode instanceof NexusDataBean) {
//			currentBean = (NexusDataBean) changeCode;
//			processData(currentBean);
//		}
		if (changeCode instanceof DataBean) {
			currentBean = (DataBean) changeCode;
 			processData(currentBean);
 		}
 	}
 
 	protected void processData(DataBean bean) {
 		// do stuff with new data
 		List<DataSetWithAxisInformation> dc = bean.getData();
 		final AbstractDataset d = dc.get(0).getData();
 		parentComp.getDisplay().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				btnStoreCurrentAsA.setEnabled(true);
 				btnStoreCurrentAsB.setEnabled(true);
 				try {
 					textMaxVal.setText(formatNumber(d.max()));
 					textMaxPos.setText(formatIntArray(d.maxPos()));
 				} catch (UnsupportedOperationException e) {
 					textMaxVal.setText("n/a");
 					textMaxPos.setText("");
 				}
 				try {
 					textMinVal.setText(formatNumber(d.min()));
 					textMinPos.setText(formatIntArray(d.minPos()));
 				} catch (UnsupportedOperationException e) {
 					textMinVal.setText("n/a");
 					textMinPos.setText("");
 				}
 				textSum.setText(formatObject(d.sum()));
 				textMean.setText(formatObject(d.mean()));
 			}
 		});
 	}
 
 	private String formatObject(Object o) {
 		if (o instanceof Number) {
 			return formatNumber((Number) o);
 		} else if (o instanceof double[]) {
 			return formatDoubleArray((double[]) o);
 		} else if (o instanceof Complex) {
 			Complex z = (Complex) o;
 			double i = z.getImaginary();
 			if (i < 0)
 				return String.format(doubleFormat + " - " + doubleFormat + "j", z.getReal(), i);
 			return String.format(doubleFormat + " + " + doubleFormat + "j", z.getReal(), i);
 		}
 		
 		return o.toString();
 	}
 
 	private String formatNumber(Number n) {
 		if (n instanceof Double || n instanceof Float) {
 			return String.format(doubleFormat, n.doubleValue());
 		}
 		return n.toString();
 	}
 
 	private String formatDoubleArray(double[] array) {
 		if (array == null || array.length == 0) {
 			return "";
 		}
 
 		StringBuilder sb = new StringBuilder(String.format(doubleFormat, array[0]));
 		for (int i = 1; i < array.length; i++) {
 			sb.append(", ");
 			sb.append(String.format(doubleFormat, array[array.length - i]));
 		}
 		return sb.toString();
 	}
 
 	private static String formatIntArray(int[] array) {
 		if (array == null || array.length == 0) {
 			return "";
 		}
 
 		StringBuilder sb = new StringBuilder(String.format("%d", array[0]));
 		for (int i = 1; i < array.length; i++) {
 			sb.append(String.format(", %d", array[array.length - i]));
 		}
 		return sb.toString();
 	}
 
 	protected void pushToPlotView(DataBean dBean) {
 		if (plotView == null) {
 			return;
 		}
 
 		List<DataSetWithAxisInformation> dc = dBean.getData();
 		AbstractDataset data = dc.get(0).getData();
 		if (data.getRank() == 1) {
 			plotView.updatePlotMode(GuiPlotMode.ONED);
 		} else if (data.getRank() == 2) {
 			plotView.updatePlotMode(GuiPlotMode.TWOD);
 		}
 
 		plotView.processPlotUpdate(dBean);
 		currentBean = dBean;
 	}
 }
