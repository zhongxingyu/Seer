 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawb.gda.extensions.util;
 
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.awt.image.DirectColorModel;
 import java.awt.image.IndexColorModel;
 import java.awt.image.WritableRaster;
 import java.io.File;
 import java.util.Arrays;
 
 import javax.swing.ImageIcon;
 import javax.swing.filechooser.FileSystemView;
 
 import org.dawb.common.services.IFileIconService;
 import org.dawb.common.services.IImageService;
 import org.dawb.common.services.ILoaderService;
 import org.dawb.common.services.IPaletteService;
 import org.dawb.common.services.IThumbnailService;
 import org.dawb.common.services.ImageServiceBean;
 import org.dawb.common.services.ImageServiceBean.ImageOrigin;
 import org.dawb.common.services.ServiceManager;
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.PlotType;
 import org.dawb.common.ui.plot.PlottingFactory;
 import org.dawb.common.util.io.FileUtils;
 import org.dawb.gda.extensions.Activator;
 import org.dawb.gda.extensions.loaders.H5Loader;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.PaletteData;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.program.Program;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorDescriptor;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.preferences.ScopedPreferenceStore;
 import org.eclipse.ui.services.AbstractServiceFactory;
 import org.eclipse.ui.services.IServiceLocator;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.RGBDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Stats;
 import uk.ac.diamond.scisoft.analysis.dataset.function.Downsample;
 import uk.ac.diamond.scisoft.analysis.dataset.function.DownsampleMode;
 import uk.ac.diamond.scisoft.analysis.histogram.mapfunctions.AbstractMapFunction;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.utils.GlobalColourMaps;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.utils.SWTImageUtils;
 import uk.ac.gda.util.OSUtils;
 
 public class ImageThumbnailCreator extends AbstractServiceFactory implements IThumbnailService {
 	
 	public ImageThumbnailCreator() {
 		
 	}
 	static {
 		// We just use file extensions
 		LoaderFactory.setLoaderSearching(false); 
 		// This now applies for the whole workbench
 	}
 	
 	private static float minimumThreshold = 0.98f;
 	private static int colourMapChoice    = 1;
     private static ImageRegistry imageRegistry;
     
 	public Image createImage(final File f, final int size) {
 		
 		if (f.isDirectory()) {
 			final Image image = Activator.getImageDescriptor("icons/folder.gif").createImage();
 			final Image blank = new Image(Display.getDefault(), size, size);
 			GC gc = new GC(blank);
 	        gc.drawImage(image, (size/2)-image.getImageData().width/2, size/2-image.getImageData().height/2);
 	        gc.dispose();
 	        
 	        return blank;
 		}
 		
 		try {
 			final AbstractDataset thumb = getThumbnail(f, size);
 		    return createImageSWT(thumb);
 		    
 		} catch (Throwable ne) {
 			
 			if (imageRegistry == null) imageRegistry = new ImageRegistry(Display.getDefault());
 			final String extension = FileUtils.getFileExtension(f);
 			Image image = imageRegistry.get(extension);
 			if (image != null) return image;
 
 			Program program = Program.findProgram(extension);
 			ImageData imageData = (program == null ? null : program.getImageData());
 			if (imageData != null) {
 				image = new Image(Display.getDefault(), imageData);
 				imageRegistry.put(extension, image);
 			    return image;
 			}
 
 		}
 		
 		final Image image = PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(f.getAbsolutePath()).createImage();
 		final Image blank = new Image(Display.getDefault(), size, size);
 		GC gc = new GC(blank);
         gc.drawImage(image, (size/2)-image.getImageData().width/2, size/2-image.getImageData().height/2);
         gc.dispose();
         
         return blank;
 	}
 	
 	private AbstractDataset getThumbnail(final File f, final int size) throws Throwable {
 		
 	    if (H5Loader.isH5(f.getAbsolutePath())) return null; // Cannot risk loading large datasets!
 		final ILoaderService loader = (ILoaderService)ServiceManager.getService(ILoaderService.class);
 		final AbstractDataset set   = loader.getDataset(f);
 		final AbstractDataset thumb = getThumbnail(set, size);
 		return thumb;
 	}
 
 	public AbstractDataset getThumbnail(final AbstractDataset ds, int size) {
 
 		if (ds!=null && ds.getRank() == 2) { // 2D datasets only!!!
 			int width = ds.getShape()[1];
 			int height = ds.getShape()[0];
 
 			int[] stepping = new int[2];
 			stepping[1] = Math.max(1, width / size);
 			stepping[0] = Math.max(1, height / size);
 			Downsample down = new Downsample(DownsampleMode.POINT, stepping);
 			AbstractDataset ds_downsampled = down.value(ds).get(0);
 			ds_downsampled.setName(ds.getName());
 			return ds_downsampled;
 		}
 
 		return null;
 		
 	}
 
 	/**
 	 * Modified from fable
 	 * @param thumbnail
 	 * @return
 	 * @throws Exception 
 	 */
 	public Image createImageSWT(final AbstractDataset thumbnail) throws Exception {
         
		final ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.plotting");
 		final ImageServiceBean bean = new ImageServiceBean();
 
 		final IPaletteService pservice = (IPaletteService)PlatformUI.getWorkbench().getService(IPaletteService.class);
 		bean.setPalette(pservice.getPaletteData(store.getString("org.dawb.plotting.system.colourSchemeName")));	
 		bean.setOrigin(ImageOrigin.forLabel(store.getString("org.dawb.plotting.system.originChoice")));
 		bean.setImage(thumbnail);
 		
 		final IImageService service = (IImageService)PlatformUI.getWorkbench().getService(IImageService.class);
 		return  service.getImage(bean);
 	}
 	/**
 	 * Modified from GDA
 	 * @param thumbnail
 	 * @return
 	 */
 	public Image createImageDiamond(final AbstractDataset thumbail) {
 		
 		GlobalColourMaps.InitializeColourMaps();
 		
 		final int[] shape = thumbail.getShape();
 		if (shape.length == 2) {
 			double max;
 			if (thumbail instanceof RGBDataset) {
 				double temp;
 				max = Stats.quantile(((RGBDataset) thumbail).createRedDataset(AbstractDataset.INT16),
 						minimumThreshold);
 				temp = Stats.quantile(((RGBDataset) thumbail).createGreenDataset(AbstractDataset.INT16),
 						minimumThreshold);
 				if (max < temp)
 					max = temp;
 				temp = Stats.quantile(((RGBDataset) thumbail).createBlueDataset(AbstractDataset.INT16),
 						minimumThreshold);
 				if (max < temp)
 					max = temp;
 			} else {
 				max = Stats.quantile(thumbail, minimumThreshold);
 			}
 			int redSelect = GlobalColourMaps.colourSelectList.get(colourMapChoice * 4);
 			int greenSelect = GlobalColourMaps.colourSelectList.get(colourMapChoice * 4 + 1);
 			int blueSelect = GlobalColourMaps.colourSelectList.get(colourMapChoice * 4 + 2);
 			AbstractMapFunction redFunc = GlobalColourMaps.mappingFunctions.get(Math.abs(redSelect));
 			AbstractMapFunction greenFunc = GlobalColourMaps.mappingFunctions.get(Math.abs(greenSelect));
 			AbstractMapFunction blueFunc = GlobalColourMaps.mappingFunctions.get(Math.abs(blueSelect));
 			ImageData imgD = SWTImageUtils.createImageData(thumbail, max, redFunc, greenFunc, blueFunc,
 					(redSelect < 0), (greenSelect < 0), (blueSelect < 0));
 			
 			return new Image(Display.getDefault(), imgD);
 		}
 		
 		return null;
 	}
 
 	@Override
 	public Object create(Class serviceInterface, IServiceLocator parentLocator,
 			IServiceLocator locator) {
 		
 		if (serviceInterface==IThumbnailService.class) {
 			return new ImageThumbnailCreator();
 		} else if (serviceInterface==IFileIconService.class) {
 			return new ImageThumbnailCreator();
 		}
 		return null;
 	}
 
 	@Override
 	public Image createImage(AbstractDataset thumb) throws Exception {
 		return createImageSWT(thumb);
 	}
 
 	@Override
 	public Image getThumbnailImage(final AbstractDataset set, final int size) throws Exception {
 		
 		if (set.getShape().length==2) {
 			final AbstractDataset thumb = getThumbnail(set, size);
 			if (thumb==null) return null;
 			return createImage(thumb);
 			
 		} else if (set.getShape().length==1) {
 
 			// We plot to an offscreen plotting system, then take a screen shot of this.
 			final AbstractPlottingSystem system = PlottingFactory.getLightWeightPlottingSystem();
 			
 			final Image[] scaled = new Image[1];
 			
 			final Display display = Display.getDefault();
 			display.syncExec(new Runnable() {
 				public void run() {
 					final Shell   shell   = new Shell(display);
 					shell.setSize(600, 600);
 					final Composite plotter = new Composite(shell, SWT.NONE);
 					system.createPlotPart(plotter, "Thumbnail", null, PlotType.XY, null);
 					
 					// TODO set no title?
 					
 					system.createPlot1D(null, Arrays.asList(set), new NullProgressMonitor());
 					
 		            final Image unscaled = system.getImage(new Rectangle(0, 0, 300, 300));
 		            scaled[0]   = new Image(display, unscaled.getImageData().scaledTo(size, size));
 				}
 			});
             return scaled[0];
 		}
 		return null;
 	}
 	    
     public Image getIconForFile(File file) {
     	
     	if (file.isDirectory()) {
     		return getFolderImage(file);
     	}
 
     	final String ext = FileUtils.getFileExtension(file);
     	if (imageRegistry == null) imageRegistry = new ImageRegistry();
     	
     	Image returnImage = imageRegistry.get(ext);
     	if (returnImage != null) return returnImage;   	
     	    	
     	// Eclipse icon
     	ECLISPE_BLOCK: if (returnImage==null) {
     		final IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getAbsolutePath());
     		if (desc==null) break ECLISPE_BLOCK;
     		final ImageDescriptor imageDescriptor = desc.getImageDescriptor();
     		if (imageDescriptor==null) break ECLISPE_BLOCK;
 	    	returnImage = imageDescriptor.createImage();
     	}
 
     	
     	// Program icon from system
     	if (returnImage==null) {
 	    	final Program program = Program.findProgram(ext);
 	    	
 	    	if (program!=null) {
 		    	ImageData iconData=Program.findProgram(ext).getImageData();
 		    	returnImage = new Image(Display.getCurrent(), iconData);
 	    	}
     	}
     	    	
     	if (returnImage==null)	returnImage = getImageSWT(file);
     	
     	imageRegistry.put(ext, returnImage);
     	
     	return returnImage;
     }
     
     static ImageData convertToSWT(BufferedImage bufferedImage) {
         if (bufferedImage.getColorModel() instanceof DirectColorModel) {
             DirectColorModel colorModel = (DirectColorModel)bufferedImage.getColorModel();
             PaletteData palette = new PaletteData(colorModel.getRedMask(), colorModel.getGreenMask(), colorModel.getBlueMask());
             ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
             for (int y = 0; y < data.height; y++) {
                     for (int x = 0; x < data.width; x++) {
                             int rgb = bufferedImage.getRGB(x, y);
                             int pixel = palette.getPixel(new RGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF)); 
                             data.setPixel(x, y, pixel);
                             if (colorModel.hasAlpha()) {
                                     data.setAlpha(x, y, (rgb >> 24) & 0xFF);
                             }
                     }
             }
             return data;            
         } else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
             IndexColorModel colorModel = (IndexColorModel)bufferedImage.getColorModel();
             int size = colorModel.getMapSize();
             byte[] reds = new byte[size];
             byte[] greens = new byte[size];
             byte[] blues = new byte[size];
             colorModel.getReds(reds);
             colorModel.getGreens(greens);
             colorModel.getBlues(blues);
             RGB[] rgbs = new RGB[size];
             for (int i = 0; i < rgbs.length; i++) {
                     rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF);
             }
             PaletteData palette = new PaletteData(rgbs);
             ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
             data.transparentPixel = colorModel.getTransparentPixel();
             WritableRaster raster = bufferedImage.getRaster();
             int[] pixelArray = new int[1];
             for (int y = 0; y < data.height; y++) {
                     for (int x = 0; x < data.width; x++) {
                             raster.getPixel(x, y, pixelArray);
                             data.setPixel(x, y, pixelArray[0]);
                     }
             }
             return data;
         }
         return null;
     }
     
     static Image getImageSWT(File file) {
         ImageIcon systemIcon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(file);
         java.awt.Image image = systemIcon.getImage();
         if (image instanceof BufferedImage) {
             return new Image(Display.getDefault(), convertToSWT((BufferedImage)image));
         }
         int width = image.getWidth(null);
         int height = image.getHeight(null);
         BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g2d = bufferedImage.createGraphics();
         g2d.drawImage(image, 0, 0, null);
         g2d.dispose();
         return new Image(Display.getDefault(), convertToSWT(bufferedImage));
     }
     
     private static Image folderImage;
     
 	private Image getFolderImage(File file) {
 		
 		if (folderImage==null) {
 			
 			if (file==null) file = OSUtils.isWindowsOS() ? new File("C:/Windows/") : new File("/");
 			/**
 			 * On windows, use windows icon for folder,
 			 * on unix folder icon can be not very nice looking, use folder.png
 			 */
 	        if (OSUtils.isWindowsOS()) {
 	        	folderImage = getImageSWT(file);
 	        } else {
 	        	folderImage = Activator.getImageDescriptor("icons/folder.gif").createImage();
 
 	        }
  			
 		}
 		return folderImage;
 	}
 
 
 }
