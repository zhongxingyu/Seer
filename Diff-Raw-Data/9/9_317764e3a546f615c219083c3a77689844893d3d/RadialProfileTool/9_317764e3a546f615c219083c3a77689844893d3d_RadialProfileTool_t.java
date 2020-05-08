 package org.dawb.workbench.plotting.tools;
 
 import org.dawb.common.ui.plot.region.IRegion;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
 import uk.ac.diamond.scisoft.analysis.roi.SectorROI;
 
 public class RadialProfileTool extends SectorProfileTool {
 
 
 	@Override
 	protected AbstractDataset getXAxis(final SectorROI sroi, AbstractDataset integral) {
 		
 		final AbstractDataset xi = DatasetUtils.linSpace(sroi.getRadius(0), sroi.getRadius(1), integral.getSize(), AbstractDataset.FLOAT32);
 		xi.setName("Radius (pixel)");
 		
 		return xi;
 	}
 
 	@Override
 	protected AbstractDataset getIntegral(AbstractDataset data,
 			                              AbstractDataset mask, 
 			                              SectorROI       sroi, 
 			                              IRegion         region,
 			                              boolean         isDrag) {
 
 
 		AbstractDataset[] profile = ROIProfile.sector(data, mask, sroi, true, false, isDrag);
 		
         if (profile==null) return null;
 				
 		final AbstractDataset integral = profile[0];
		integral.setName("Radial Profile "+region.getName());
 	
 		return integral;
 	}
 }
