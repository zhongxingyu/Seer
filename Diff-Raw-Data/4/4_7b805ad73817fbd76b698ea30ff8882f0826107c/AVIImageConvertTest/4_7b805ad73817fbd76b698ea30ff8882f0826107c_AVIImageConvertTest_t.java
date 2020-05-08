 /*
  * Copyright (c) 2012 Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.dawnsci.conversion;
 
 import java.awt.Dimension;
 import java.io.File;
 
 import org.dawb.common.services.ServiceManager;
 import org.dawb.common.services.conversion.IConversionContext;
 import org.dawb.common.services.conversion.IConversionContext.ConversionScheme;
 import org.dawb.common.services.conversion.IConversionService;
 import org.dawb.common.ui.image.PaletteFactory;
 import org.dawnsci.conversion.converters.AVIImageConverter;
 import org.dawnsci.conversion.converters.ConversionInfoBean;
 import org.dawnsci.plotting.histogram.service.PaletteService;
 import org.dawnsci.plotting.services.ImageService;
 import org.dawnsci.plotting.services.PlotImageService;
 import org.eclipse.dawnsci.plotting.api.histogram.HistogramBound;
 import org.eclipse.dawnsci.plotting.api.histogram.IImageService;
 import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
 import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
 import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean.HistoType;
 import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean.ImageOrigin;
 import org.eclipse.dawnsci.plotting.api.image.IPlotImageService;
 import org.junit.Test;
 import org.monte.media.avi.AVIReader;
 
 import uk.ac.diamond.scisoft.analysis.dataset.function.DownsampleMode;
 
 public class AVIImageConvertTest {
 
 	
 	@Test
 	public void testAVISimple() throws Exception {
 		
 		IConversionService service = new ConversionServiceImpl();
 		
 		// Not sure of this will work...
		ServiceManager.setService(IPaletteService.class,     new PaletteService());
		ServiceManager.setService(IImageService.class,       new ImageService());
 		AVIImageConverter.setImageService(new ImageService());
 		AVIImageConverter.setPlotImageService(new PlotImageService());
 		
 		// Determine path to test file
 		final String path = getTestFilePath("export.h5");
 		
 		final IConversionContext context = service.open(path);
 		final File avi = File.createTempFile("test_video", ".avi");
 		avi.deleteOnExit();
         context.setOutputPath(avi.getAbsolutePath());
         context.setConversionScheme(ConversionScheme.AVI_FROM_3D);
         context.setDatasetName("/entry/edf/data");
         context.addSliceDimension(0, "all");
         
         final ConversionInfoBean info = new ConversionInfoBean();
         info.setImageServiceBean(createTestingBean());
         info.setDownsampleBin(2);
         info.setDownsampleMode(DownsampleMode.MAXIMUM);
         context.setUserObject(info);
         
         service.process(context);
         
         // Check avi file
         final AVIReader reader = new AVIReader(avi);
         int trackCount = reader.getTrackCount();
         if (trackCount!=1) throw new Exception("Incorrect number of tracks!");
         Dimension d = reader.getVideoDimension();
         if (d.width!=1024) throw new Exception("Incorrect downsampling applied!");
         if (d.height!=1024) throw new Exception("Incorrect downsampling applied!");
         
         // Done
         System.out.println("Test passed, avi file written!");
    	}
 	
 	private ImageServiceBean createTestingBean() {
 		ImageServiceBean imageServiceBean = new ImageServiceBean();
 		imageServiceBean.setPalette(PaletteFactory.makeBluesPalette());
 		imageServiceBean.setOrigin(ImageOrigin.TOP_LEFT);
 		imageServiceBean.setHistogramType(HistoType.MEAN);
 		imageServiceBean.setMinimumCutBound(HistogramBound.DEFAULT_MINIMUM);
 		imageServiceBean.setMaximumCutBound(HistogramBound.DEFAULT_MAXIMUM);
 		imageServiceBean.setNanBound(HistogramBound.DEFAULT_NAN);
 		imageServiceBean.setLo(0);
 		imageServiceBean.setHi(300);		
 		
 		return imageServiceBean;
 	}
 
 	private String getTestFilePath(String fileName) {
 		
 		final File test = new File("testfiles/"+fileName);
 		return test.getAbsolutePath();
 	
 	}
 
 }
