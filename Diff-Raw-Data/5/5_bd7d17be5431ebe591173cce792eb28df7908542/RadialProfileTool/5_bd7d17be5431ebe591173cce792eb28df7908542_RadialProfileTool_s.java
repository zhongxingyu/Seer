 package org.dawnsci.plotting.tools.profile;
 
 import java.util.Collection;
 
 import javax.vecmath.Vector3d;
 
 import org.dawb.common.services.ILoaderService;
 import org.dawb.common.ui.menu.MenuAction;
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.gda.extensions.loaders.H5Utils;
 import org.dawnsci.plotting.Activator;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.ui.PlatformUI;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
 import uk.ac.diamond.scisoft.analysis.diffraction.DetectorPropertyEvent;
 import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
 import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironmentEvent;
 import uk.ac.diamond.scisoft.analysis.diffraction.IDetectorPropertyListener;
 import uk.ac.diamond.scisoft.analysis.diffraction.IDiffractionCrystalEnvironmentListener;
 import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;
 import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
 import uk.ac.diamond.scisoft.analysis.roi.SectorROI;
 
 public class RadialProfileTool extends SectorProfileTool implements IDetectorPropertyListener, IDiffractionCrystalEnvironmentListener{
 	
 	private enum XAxis {
 		PIXEL, RESOLUTION, ANGLE, Q,
 	}
 	
 	private XAxis axis = XAxis.PIXEL;
     private MenuAction profileAxis;
     private Action metaLock;
 	
 	@Override
 	protected void configurePlottingSystem(AbstractPlottingSystem plotter) {
 		
 		profileAxis = new MenuAction("Select X Axis");
 		profileAxis.setToolTipText("Select x axis values");
 		
 		final Action pixelAxis = new Action("Px", IAction.AS_RADIO_BUTTON) {
 			@Override
 			public void run() {
 				axis = XAxis.PIXEL;
 				profileAxis.setSelectedAction(this);
 				profilePlottingSystem.clear();
 				update(null, null, false);
 			}
 		};
 		
 		pixelAxis.setId("org.dawb.workbench.plotting.tools.profile.pixelAxisAction");
 		
 		final Action resolutionAxis = new Action("d ", IAction.AS_RADIO_BUTTON) {
 			@Override
 			public void run() {
 				axis = XAxis.RESOLUTION;
 				profileAxis.setSelectedAction(this);
 				profilePlottingSystem.clear();
 				update(null, null, false);
 			}
 		};
 		
 		final Action angleAxis = new Action("2\u03b8", IAction.AS_RADIO_BUTTON) {
 			@Override
 			public void run() {
 				axis = XAxis.ANGLE;
 				profileAxis.setSelectedAction(this);
 				profilePlottingSystem.clear();
 				update(null, null, false);
 			}
 		};
 		
 		final Action qAxis = new Action("q ", IAction.AS_RADIO_BUTTON) {
 			@Override
 			public void run() {
 				axis = XAxis.Q;
 				profileAxis.setSelectedAction(this);
 				profilePlottingSystem.clear();
 				update(null, null, false);
 			}
 		};
 		
 		metaLock = new Action("Lock To Metadata", IAction.AS_CHECK_BOX) {
 			@Override
 			public void run() {
 				if (isChecked()) {
 					IMetaData meta = getMetaData();
 					profileAxis.setEnabled(true);
 					
 					if (meta != null && meta instanceof IDiffractionMetadata) {
 						updateSectorCenters(((IDiffractionMetadata)meta).getDetector2DProperties().getBeamCentreCoords());
 						registerMetadataListeners();
 					}
 					
 					if (getPlottingSystem()==null) return;
 					
 					final Collection<IRegion> regions = getPlottingSystem().getRegions();
 					if (regions!=null) for (final IRegion region : regions) {
 						if (isRegionTypeSupported(region.getRegionType())) {
 							region.setMobile(false);
 						}
 					}
 					
 					for (int i = 0; i < profileAxis.size(); ++i) {
 						IAction action = profileAxis.getAction(i);
 						if (action.isChecked()) {
 							profileAxis.setSelectedAction(i);
 							profileAxis.run();
 						}
 					}
 					
 					
 				} else {
 					
 					unregisterMetadataListeners();
 					IAction pixelAction = profileAxis.findAction("org.dawb.workbench.plotting.tools.profile.pixelAxisAction");
 					profileAxis.setEnabled(false);
 					pixelAction.run();
 					
 					final Collection<IRegion> regions = getPlottingSystem().getRegions();
 					if (regions!=null) for (final IRegion region : regions) {
 						if (isRegionTypeSupported(region.getRegionType())) {
 							region.setMobile(true);
 						}
 					}
 				}
 			}
 		};
 		
 		metaLock.setImageDescriptor(Activator.getImageDescriptor("icons/radial-tool-lock.png"));
 		
 		
 		getSite().getActionBars().getToolBarManager().add(profileAxis);
 		getSite().getActionBars().getToolBarManager().add(metaLock);
 		getSite().getActionBars().getToolBarManager().add(new Separator());
 		getSite().getActionBars().getMenuManager().add(profileAxis);
 		getSite().getActionBars().getMenuManager().add(metaLock);
 		getSite().getActionBars().getToolBarManager().add(new Separator());
 		
 		profileAxis.setSelectedAction(pixelAxis);
 		profileAxis.add(pixelAxis);
 		profileAxis.add(resolutionAxis);
 		profileAxis.add(angleAxis);
 		profileAxis.add(qAxis);
 		
 		setActionsEnabled(false);
 		profileAxis.setEnabled(false);
 		
 		//plotter.get
 		setActionsEnabled(isValidMetadata(getMetaData()));
 		
 		super.configurePlottingSystem(plotter);
 
 	}
 	
 	public void activate () {
 		super.activate();
 		
 		//setup the lock action to work for valid metadata
 		if (metaLock == null) return;
 		IMetaData meta = getMetaData();
 		if (meta==null) return;
 		
 		if (metaLock.isChecked()) {
 			if (isValidMetadata(meta)) {
 				updateSectorCenters(((IDiffractionMetadata)meta).getDetector2DProperties().getBeamCentreCoords());
 				registerMetadataListeners();
 			} else {
 				
 				metaLock.setChecked(false);
 				metaLock.run();
 				metaLock.setEnabled(false);
 				setError(true);
 			}
 		} else {
 			if (isValidMetadata(meta)) {
 				metaLock.setEnabled(true);
 			} else {
 				metaLock.setEnabled(false);
 				setError(true);
 			}
 		}
 	}
 	
 	public void deactivate() {
 		super.deactivate();
 		setError(false);
 		unregisterMetadataListeners();
 	}
 	
 	@Override
 	protected void updateSectors() {
 		
 		if(metaLock.isChecked()) {
 			metaLock.run();
 		}
 		
 		super.updateSectors();
 	}
 
 	@Override
 	protected AbstractDataset[] getXAxis(final SectorROI sroi, AbstractDataset[] integrals) {
 		final AbstractDataset xi = DatasetUtils.linSpace(sroi.getRadius(0), sroi.getRadius(1), integrals[0].getSize(), AbstractDataset.FLOAT64);
 		xi.setName("Radius (pixel)");
 		
 		IMetaData meta = getMetaData();
 		
 		if (!sroi.hasSeparateRegions())  {
 			
			if (meta!=null && isValidMetadata(meta)) {
 				setActionsEnabled(true);
 				return new AbstractDataset[]{pixelToValue(xi,(IDiffractionMetadata)meta)};
 			}
 			
 			return new AbstractDataset[]{xi};
 			
 		} else {
 
 			final AbstractDataset xii = DatasetUtils.linSpace(sroi.getRadius(0), sroi.getRadius(1), integrals[1].getSize(), AbstractDataset.FLOAT64);
 			xii.setName("Radius (pixel)");
 			
 			if (meta!=null && isValidMetadata(meta)) {
 				setActionsEnabled(true);
 				return new AbstractDataset[]{pixelToValue(xi,(IDiffractionMetadata)meta),pixelToValue(xii,(IDiffractionMetadata)meta)};
 			}
 
 			return new AbstractDataset[]{xi, xii};
 		}
 		
 	}
 	
 	private void setActionsEnabled(boolean enable) {
 		metaLock.setEnabled(enable);
 	}
 	
 	private boolean isValidMetadata(IMetaData meta) {
 		
 		if (meta!=null && (meta instanceof IDiffractionMetadata)) {
 
 			IDiffractionMetadata dm = (IDiffractionMetadata)meta;
 			double[] angles = dm.getDetector2DProperties().getNormalAnglesInDegrees();
 			
 			//non zero pitch roll and yaw currently not supported
 			if (angles[0] == 0 &&
 				angles[1] == 0 &&
 				angles[2] == 0) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	@Override
 	protected IMetaData getMetaData() {
 		
 		ILoaderService service = (ILoaderService)PlatformUI.getWorkbench().getService(ILoaderService.class);
 		
 		IDiffractionMetadata meta = service.getLockedDiffractionMetaData();
 		
 		if (meta!= null)
 			return meta;
 		else
 			return super.getMetaData();
 		
 	}
 	
 	private void setError(boolean isError) {
 		if (isError) {
 			getSite().getActionBars().getStatusLineManager().setErrorMessage("WARNING: Locking profile to meta data not supported for non-zero detector pitch/roll/yaw");
 		} else {
 			getSite().getActionBars().getStatusLineManager().setErrorMessage(null);
 		}
 		
 	}
 	
 	private void registerMetadataListeners() {
 		IMetaData meta = getMetaData();
 		if (meta!=null && (meta instanceof IDiffractionMetadata)) {
 			IDiffractionMetadata dm = (IDiffractionMetadata)meta;
 			dm.getDetector2DProperties().addDetectorPropertyListener(this);
 			dm.getDiffractionCrystalEnvironment().addDiffractionCrystalEnvironmentListener(this);
 		}
 	}
 	
 	private void unregisterMetadataListeners() {
 		IMetaData meta = getMetaData();
 		if (meta!=null && (meta instanceof IDiffractionMetadata)) {
 			IDiffractionMetadata dm = (IDiffractionMetadata)meta;
 			dm.getDetector2DProperties().removeDetectorPropertyListener(this);
 			dm.getDiffractionCrystalEnvironment().removeDiffractionCrystalEnvironmentListener(this);
 		}
 	}
 	
 	
 	private AbstractDataset pixelToValue(AbstractDataset dataset, IDiffractionMetadata metadata) {
 		
 		DetectorProperties detprops = metadata.getDetector2DProperties();
     	DiffractionCrystalEnvironment diffexp = metadata.getDiffractionCrystalEnvironment();
     	
     	if (detprops == null && diffexp == null) return dataset;
     		
     	double[] beamCen = detprops.getBeamCentreCoords();
     		
     	QSpace qSpace = new QSpace(detprops, diffexp);
     	
     	switch (axis) {
     	case PIXEL:
     		return dataset;
     	case RESOLUTION:
     		for (int i = 0; i < dataset.getSize(); ++i) {
         		double val = dataset.getDouble(i);
         		Vector3d vect= qSpace.qFromPixelPosition(beamCen[0] + val, beamCen[1]);
         		dataset.set((2*Math.PI)/vect.length(), i);
         	}
     		dataset.setName("d-spacing (\u00c5)");
     		return dataset;
     	case ANGLE:
     		for (int i = 0; i < dataset.getSize(); ++i) {
         		double val = dataset.getDouble(i);
         		Vector3d vect= qSpace.qFromPixelPosition(beamCen[0] + val, beamCen[1]);
         		dataset.set(Math.toDegrees(qSpace.scatteringAngle(vect)), i);
         	}
     		dataset.setName("2\u03b8 (\u00b0)");
     		return dataset;
     	case Q:
     		for (int i = 0; i < dataset.getSize(); ++i) {
         		double val = dataset.getDouble(i);
         		Vector3d vect= qSpace.qFromPixelPosition(beamCen[0] + val, beamCen[1]);
         		dataset.set(vect.length(), i);
         	}
     		dataset.setName("q (1/\u00c5)");
     		return dataset;
     	default:
     		return dataset;
 
     	}
     	
 	}
 	
 
 	@Override
 	protected AbstractDataset[] getIntegral(AbstractDataset data,
 			                              AbstractDataset mask, 
 			                              SectorROI       sroi, 
 			                              IRegion         region,
 			                              boolean         isDrag) {
 
 
 		AbstractDataset[] profile = ROIProfile.sector(data, mask, sroi, true, false, isDrag);
 		
         if (profile==null) return null;
 				
 		final AbstractDataset integral = profile[0];
 		integral.setName("Radial Profile "+region.getName());
 		
 		// If not symmetry profile[2] is null, otherwise plot it.
 	    if (profile.length>=3 && profile[2]!=null && sroi.hasSeparateRegions()) {
 	    	
 			final AbstractDataset reflection = profile[2];
 			reflection.setName("Symmetry "+region.getName());
 
 			return new AbstractDataset[]{integral, reflection};
 	    	
 	    } else {
 	    	return new AbstractDataset[]{integral};
 	    }
 	}
 
 	@Override
 	public DataReductionInfo export(DataReductionSlice slice) throws Exception {
 		
 		final IImageTrace   image   = getImageTrace();
 		final Collection<IRegion> regions = getPlottingSystem().getRegions();
 		
 		for (IRegion region : regions) {
 			if (!isRegionTypeSupported(region.getRegionType())) continue;
 			if (!region.isVisible())    continue;
 			if (!region.isUserRegion()) continue;
 			
 			final SectorROI sroi = (SectorROI)region.getROI();
 			AbstractDataset[] profile = ROIProfile.sector(slice.getData(), image.getMask(), sroi, true, false, false);
 		
 			AbstractDataset integral = profile[0];
 			integral.setName("radial_"+region.getName().replace(' ', '_'));     
 			H5Utils.appendDataset(slice.getFile(), slice.getParent(), integral);
 			
 		    if (profile.length>=3 && profile[2]!=null && sroi.hasSeparateRegions()) {
 				final AbstractDataset reflection = profile[2];
 				reflection.setName("radial_sym_"+region.getName().replace(' ', '_'));     
 				H5Utils.appendDataset(slice.getFile(), slice.getParent(), reflection);
 		    }
 		}
 		return new DataReductionInfo(Status.OK_STATUS);
 	}
 
 
 	@Override
 	public void diffractionCrystalEnvironmentChanged(
 			DiffractionCrystalEnvironmentEvent evt) {
 		update(null, null, false);
 		
 	}
 
 
 	@Override
 	public void detectorPropertiesChanged(DetectorPropertyEvent evt) {
 		
 		if (evt.getSource() instanceof DetectorProperties) {
 			if(evt.hasBeamCentreChanged()) {
 				updateSectorCenters(((DetectorProperties)evt.getSource()).getBeamCentreCoords());
 			} else {
 				update(null, null, false);
 			}
 		}
 	}
 	
 	private void updateSectorCenters(double[] point) {
 		
 		if (getPlottingSystem()==null) return;
 		
 		final Collection<IRegion> regions = getPlottingSystem().getRegions();
 		if (regions!=null) for (final IRegion region : regions) {
 			if (isRegionTypeSupported(region.getRegionType())) {
 				final SectorROI sroi = (SectorROI)region.getROI();
 				sroi.setPoint(point);
 				region.setROI(sroi);
 			}
 		}
 		
 		update(null, null, false);
 	}
 	
 }
