 package org.dawb.workbench.plotting.tools;
 
 import org.dawb.common.ui.plot.region.IRegion;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
 import uk.ac.diamond.scisoft.analysis.roi.SectorROI;
 
 public class AzimuthalProfileTool extends SectorProfileTool {
 
 	@Override
 	protected AbstractDataset getXAxis(final SectorROI sroi, AbstractDataset integral) {
 		
 		final AbstractDataset xi;
 		
 		if (sroi.getSymmetry() != SectorROI.FULL)
 			xi = DatasetUtils.linSpace(sroi.getAngleDegrees(0), sroi.getAngleDegrees(1),integral.getSize(), AbstractDataset.FLOAT64);
 		else
 			xi = DatasetUtils.linSpace(sroi.getAngleDegrees(0), sroi.getAngleDegrees(0) + 360., integral.getSize(), AbstractDataset.FLOAT64);
 		xi.setName("Angle (°)");
 		
 		return xi;
 	}
 
 	@Override
 	protected AbstractDataset getIntegral(AbstractDataset data,
 			                              AbstractDataset mask, 
 			                              SectorROI       sroi, 
 			                              IRegion         region,
 			                              boolean         isDrag) {
 
 
 		final AbstractDataset[] profile = ROIProfile.sector(data, mask, sroi, false, true, isDrag);
 		if (profile==null) return null;
 		
 		AbstractDataset integral = profile[1];
		integral.setName("Azimuthal Profile "+region.getName());
 	
 		return integral;
 	}
 
 
 }
