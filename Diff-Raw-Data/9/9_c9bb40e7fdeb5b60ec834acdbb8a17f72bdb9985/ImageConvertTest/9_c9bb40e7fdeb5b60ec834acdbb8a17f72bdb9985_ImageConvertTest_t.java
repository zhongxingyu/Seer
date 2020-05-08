 package org.dawnsci.conversion;
 
 import java.io.File;
 
 import org.dawb.common.services.conversion.IConversionContext;
 import org.dawb.common.services.conversion.IConversionContext.ConversionScheme;
 import org.dawnsci.conversion.converters.ImageConverter;
 import org.junit.Test;
 
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.io.DataHolder;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 
 
 public class ImageConvertTest {
 
 	
 	@Test
 	public void testTiffSimple() throws Exception {
 		
 		ConversionServiceImpl service = new ConversionServiceImpl();
 		
 		// Determine path to test file
 		final String path = getTestFilePath("export.h5");
 		
 		final IConversionContext context = service.open(path);
 		final File tmp = File.createTempFile("whatever", ".unknown");
 		tmp.deleteOnExit();
		File dir = new File(tmp.getParent(), "tiff_export"+System.currentTimeMillis());
 		dir.mkdirs();
 		dir.deleteOnExit();
         context.setOutputPath(dir.getAbsolutePath());
         context.setConversionScheme(ConversionScheme.TIFF_FROM_3D);
         context.setDatasetName("/entry/edf/data");
         context.addSliceDimension(0, "all");
         
         service.process(context);
         
         double lastVal = 0;
        dir = new File(dir, "export");
         final File[] fa = dir.listFiles();
         for (File file : fa) {
         	file.deleteOnExit();
         	final DataHolder holder = LoaderFactory.getData(file.getAbsolutePath());
         	final IDataset   set    = holder.getDataset(0);
         	if (set.getShape()[0]!=2048 || set.getShape()[1]!=2048) {
         		throw new Exception("Incorrect shape of exported dataset!");
         	}
         	
         	double val = set.getDouble(820,1000);
         	if (val == lastVal) throw new Exception("Same data loaded each time");
         	lastVal = val;
         }
         
         // Check that 4 datasets were exported.
         if (fa.length!=4) {
         	String msg = "The directory: "+dir.getAbsolutePath()+" does not contain 4 images!";
         	System.out.println(msg);
         	throw new Exception(msg);
         }
    	}
 	
 	// TODO Test wih slicing other than "all"
 	
 	@Test
 	public void testPNGCustomConfig() throws Exception {
 		
 		ConversionServiceImpl service = new ConversionServiceImpl();
 		
 		// Determine path to test file
 		final String path = getTestFilePath("export.h5");
 		
 		final IConversionContext context = service.open(path);
 		final File tmp = File.createTempFile("whatever", ".unknown");
 		tmp.deleteOnExit();
		File dir = new File(tmp.getParent(), "tiff_export"+System.currentTimeMillis());
 		dir.mkdirs();
 		dir.deleteOnExit();
         context.setOutputPath(dir.getAbsolutePath());
         context.setConversionScheme(ConversionScheme.TIFF_FROM_3D);
         context.setDatasetName("/entry/edf/data");
         context.addSliceDimension(0, "all");
         
         ImageConverter.ConversionInfoBean bean = new ImageConverter.ConversionInfoBean();
         bean.setExtension("png");
         bean.setBits(16);
         bean.setAlternativeNamePrefix("Export");
         context.setUserObject(bean);
         
         service.process(context);
         
        dir = new File(dir, "export");
         final File[] fa = dir.listFiles();
         for (File file : fa) file.deleteOnExit();
         
         double lastVal = 0;
         
         for (File file : fa) {
         	
         	if (!file.getName().startsWith("Export")) throw new Exception("Alternative name did not work!");
         	
         	final DataHolder holder = LoaderFactory.getData(file.getAbsolutePath());
         	final IDataset   set    = holder.getDataset(0);
         	if (set.getShape()[0]!=2048 || set.getShape()[1]!=2048) {
         		throw new Exception("Incorrect shape of exported dataset!");
         	}
         	
         	double val = set.getDouble(820,1000);
         	if (val == lastVal) throw new Exception("Same data loaded each time");
         	lastVal = val;
         }
          
         // Check that 4 datasets were exported.
         if (fa.length!=4) {
         	String msg = "The directory: "+dir.getAbsolutePath()+" does not contain 4 images!";
         	System.out.println(msg);
         	throw new Exception(msg);
         }
  	}
 
 
 	private String getTestFilePath(String fileName) {
 		
 		final File test = new File("testfiles/"+fileName);
 		return test.getAbsolutePath();
 	
 	}
 
 }
