 package org.dawnsci.plotting.tools.region;
 
 import java.text.DecimalFormat;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 
 import org.dawnsci.common.widgets.celleditor.FloatSpinnerCellEditor;
 import org.dawnsci.plotting.api.axis.ICoordinateSystem;
 import org.dawnsci.plotting.api.region.IRegion;
 import org.dawnsci.plotting.api.region.IRegion.RegionType;
 import org.dawnsci.plotting.tools.Activator;
 import org.dawnsci.plotting.tools.preference.RegionEditorConstants;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.CheckboxCellEditor;
 import org.eclipse.jface.viewers.ColumnViewer;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Composite;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 
 
 class RegionEditingSupport extends EditingSupport {
 	
 	
 	private final static Collection<RegionType> EDITABLE_REGIONS;
 	static {
 		Collection<RegionType> tmp = new HashSet<IRegion.RegionType>();
 		tmp.add(RegionType.BOX);
 		tmp.add(RegionType.LINE);
 		tmp.add(RegionType.GRID);
 		tmp.add(RegionType.XAXIS);
 		tmp.add(RegionType.XAXIS_LINE);
 		tmp.add(RegionType.YAXIS);
 		tmp.add(RegionType.YAXIS_LINE);
 		tmp.add(RegionType.PERIMETERBOX);
 		EDITABLE_REGIONS = Collections.unmodifiableCollection(tmp);
 	}
 
 	private static final Logger logger = LoggerFactory.getLogger(RegionEditingSupport.class);
 
 		private int column;
 
 		private RegionEditTool tool;
 
 		public RegionEditingSupport(RegionEditTool tool, ColumnViewer viewer, int col) {
 			super(viewer);
 			this.tool   = tool;
 			this.column = col;
 		}
 		@Override
 		protected CellEditor getCellEditor(final Object element) {
 			CellEditor ed = null;
 			
 			if(column == 0){
 				return new CheckboxCellEditor(((TableViewer)getViewer()).getTable(), SWT.RIGHT);
 			} else if(column > 1 && column < 8){
 				
 				IPreferenceStore store = Activator.getPlottingPreferenceStore();
 				DecimalFormat pointFormat = new DecimalFormat(store.getString(RegionEditorConstants.POINT_FORMAT));
 				
 				final FloatSpinnerCellEditor fse = new FloatSpinnerCellEditor((Composite)getViewer().getControl(), SWT.RIGHT);
 				fse.setFormat(pointFormat.getMaximumIntegerDigits(), pointFormat.getMaximumFractionDigits());
 				fse.setMaximum(Double.MAX_VALUE);
 				fse.setMinimum(-Double.MAX_VALUE);
 				fse.addSelectionListener(new SelectionAdapter() {
 					@Override
 					public void widgetSelected(SelectionEvent e) {
 						try {
 							setValue(element, fse.getValue(), false);
 						} catch (Exception e1) {
 							logger.debug("Error while setting table value");
 							e1.printStackTrace();
 						}
 					}
 				});
 				return fse;
 			} else if(column == 1){
 				ed = new TextCellEditor(((TableViewer)getViewer()).getTable(), SWT.RIGHT);
 				return ed;
 			} else if(column == 8){
 				ed = new CheckboxCellEditor(((TableViewer)getViewer()).getTable(), SWT.RIGHT);
 				return ed;
 			}
 			return null;
 		}
 
 		@Override
 		protected boolean canEdit(Object element) {
 			
 			if (!(element instanceof IRegion)) return false;
 			final IRegion region = (IRegion)element;
 			if (!EDITABLE_REGIONS.contains(region.getRegionType())) return false;
 			if (column == 6 || column == 7) return false;
 			else return true;
 		}
 
 		@Override
 		protected Object getValue(Object element) {
 			final IRegion region = (IRegion)element;
 			IROI roi = region.getROI();
 			ICoordinateSystem coords = region.getCoordinateSystem();
 			double[] startPoint = tool.getAxisPoint(coords, roi.getPoint());
 			double[] endPoint = {0, 0};
 			if(roi instanceof RectangularROI){
 				endPoint = tool.getAxisPoint(coords, ((RectangularROI)roi).getEndPoint());
 			} else if (roi instanceof LinearROI){
 				endPoint = tool.getAxisPoint(coords, ((LinearROI)roi).getEndPoint());
 			}
 			switch (column){
 			case 0:
 				return region.isVisible();
 			case 1:
 				return region.getLabel();
 			case 2:
 				return startPoint[0];
 			case 3:
 				return startPoint[1];
 			case 4:
 				return endPoint[0];
 			case 5:
 				return endPoint[1];
 			case 6: //max intensity
 				return null;
 			case 7: //sum
 				return null;
 			case 8: //isPlot
 				return region.isActive();
 //				return roi.isPlot();
 			default:
 				return null;
 			}
 			
 		}
 
 		@Override
 		protected void setValue(Object element, Object value) {
 			try {
 				this.setValue(element, value, true);
 			} catch (Exception e) {
 				logger.debug("Error while setting table value");
 				e.printStackTrace();
 			}
 		}
 		
 		private void setValue(Object element, Object value, boolean tableRefresh) throws Exception {
 
 			final IRegion region = (IRegion) element;
 			IROI myRoi = region.getROI();
 			ICoordinateSystem coords = region.getCoordinateSystem();
 			double[] roiStartPoint = tool.getAxisPoint(coords, myRoi.getPoint());
 			double[] roiEndPoint = {0, 0};
 			if(myRoi instanceof RectangularROI){
 				roiEndPoint = tool.getAxisPoint(coords, ((RectangularROI)myRoi).getEndPoint());
 			} else if (myRoi instanceof LinearROI){
 				roiEndPoint = tool.getAxisPoint(coords, ((LinearROI)myRoi).getEndPoint());
 			}
 			switch (column){
 			case 0:
 				region.setVisible((Boolean)value);
 				break;
 			case 1:
 				// takes care of renaming the region (label and key value in hash table)
 				if(!region.getName().equals((String)value)) {
 					tool.getPlottingSystem().renameRegion(region, (String)value);
 					// Rename the roi
 					region.getROI().setName((String)value);
 				}
 				break;
 			case 2:
 				if(myRoi instanceof LinearROI){
 					LinearROI lroi = (LinearROI)myRoi;
 					double[] endPoint = lroi.getEndPoint();
 					double[] startPoint = tool.getImagePoint(coords, new double[]{(Double)value, roiStartPoint[1]});
 					lroi.setPoint(startPoint);
 					lroi.setPoint((Double)value, lroi.getPointY());
 					lroi.setEndPoint(endPoint);
 					myRoi = lroi;
 				} else if(myRoi instanceof RectangularROI){
 					RectangularROI rroi = (RectangularROI)myRoi;
 					double[] endPoint = rroi.getEndPoint();
 					double[] startPoint = tool.getImagePoint(coords, new double[]{(Double)value, roiStartPoint[1]});
 					rroi.setPoint(startPoint);
 					rroi.setLengths(endPoint[0] - startPoint[0], rroi.getLengths()[1]); //set new endpoint
 					myRoi = rroi;
 				}
 				break;
 			case 3:
 				if(myRoi instanceof LinearROI){
 					LinearROI lroi = (LinearROI)myRoi;
 					double[] endPoint = lroi.getEndPoint();
 					double[] startPoint = tool.getImagePoint(coords, new double[]{roiStartPoint[0], (Double)value});
 					lroi.setPoint(startPoint);
 					lroi.setEndPoint(endPoint);
 					myRoi = lroi;
 				} else if(myRoi instanceof RectangularROI){
 					RectangularROI rroi = (RectangularROI)myRoi;
 					double[] endPoint = rroi.getEndPoint();
 					double[] startPoint = tool.getImagePoint(coords, new double[]{roiStartPoint[0], (Double)value});
 					rroi.setPoint(startPoint);
 					rroi.setLengths(rroi.getLengths()[0], endPoint[1] - startPoint[1]); //set new endpoint
 					myRoi = rroi;
 				}
 				break;
 			case 4:
 				if(myRoi instanceof LinearROI){
 					double[] endPoint = tool.getImagePoint(coords, 
 							new double[]{(Double)value, roiEndPoint[1]});
 					((LinearROI)myRoi).setEndPoint(endPoint);
 				}
 				else if (myRoi instanceof RectangularROI){
 					double[] endPoint = tool.getImagePoint(coords, 
 							new double[]{(Double)value, roiEndPoint[1]});
 					((RectangularROI)myRoi).setEndPoint(endPoint);
 				}
 				break;
 			case 5:
 				if(myRoi instanceof LinearROI){
 					double[] endPoint = tool.getImagePoint(coords, 
 							new double[]{roiEndPoint[0], (Double)value});
 					((LinearROI)myRoi).setEndPoint(endPoint);
 				}
 				else if (myRoi instanceof RectangularROI){
 					double[] endPoint = tool.getImagePoint(coords, 
 							new double[]{roiEndPoint[0], (Double)value});
 					((RectangularROI)myRoi).setEndPoint(endPoint);
 				}
 				break;
 			case 6: //intensity
 				break;
 			case 7: //sum
 				break;
 			case 8: //isPlot
 				myRoi.setPlot((Boolean)value);
 				region.setActive((Boolean)value);
 				break;
 			default:
 				break;
 			}
 
 			if (tableRefresh) {
 				getViewer().refresh();
 			}
 
 			tool.setRoi(myRoi);
			IRegion myregion = tool.getPlottingSystem().getRegion(region.getName());
 			if(myregion!= null)
 				myregion.setROI(myRoi);
 		}
 	}
