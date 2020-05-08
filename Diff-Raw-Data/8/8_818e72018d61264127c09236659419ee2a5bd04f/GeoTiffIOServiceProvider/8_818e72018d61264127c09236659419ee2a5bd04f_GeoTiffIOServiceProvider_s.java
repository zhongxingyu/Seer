 package ucar.nc2.iosp.geotiff;
 
 import ucar.nc2.iosp.geotiff.cs.CSHandler;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBuffer;
 import java.awt.image.Raster;
 import java.awt.image.renderable.ParameterBlock;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Formatter;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import javax.imageio.ImageIO;
 import javax.imageio.ImageReadParam;
 import javax.imageio.ImageReader;
 import javax.imageio.ImageTypeSpecifier;
 import javax.imageio.metadata.IIOMetadata;
 import javax.imageio.stream.ImageInputStream;
 import javax.media.jai.JAI;
 import javax.media.jai.PlanarImage;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import org.w3c.dom.Node;
 import ucar.ma2.Array;
 import ucar.ma2.DataType;
 import ucar.ma2.InvalidRangeException;
 import ucar.ma2.Range;
 import ucar.ma2.Section;
 import ucar.nc2.Attribute;
 import ucar.nc2.NetcdfFile;
 import ucar.nc2.Variable;
 import ucar.nc2.constants.FeatureType;
 import ucar.nc2.dataset.CoordinateAxis1D;
 import ucar.nc2.dt.GridCoordSystem;
 import ucar.nc2.dt.GridDataset;
 import ucar.nc2.dt.GridDatatype;
 import ucar.nc2.ft.FeatureDataset;
 import ucar.nc2.ft.FeatureDatasetFactoryManager;
 import ucar.nc2.iosp.AbstractIOServiceProvider;
 import ucar.nc2.util.CancelTask;
 import ucar.unidata.io.RandomAccessFile;
 
 
 /**
  *
  * @author tkunicki
  */
 public class GeoTiffIOServiceProvider extends AbstractIOServiceProvider {
 
     private final static String IMAGEIO_EXT_READER =
             "it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReader";
     private final static String IMAGEIO_JAI_READER =
             "com.sun.media.imageioimpl.plugins.tiff.TIFFImageReader";
 
     private final static String ATTRIBUTE_BAND = "band";
     private final static String ATTRIBUTE_IMAGE = "image";
 
     private String location;
 
 
     static {
         ImageIO.scanForPlugins();
     }
 
     @Override
     public boolean isValidFile(RandomAccessFile raf) throws IOException {
         return raf.getLocation().endsWith("tiff") ||
                 raf.getLocation().endsWith("tif");
 
     }
 
     @Override
     public void open(RandomAccessFile raf, NetcdfFile ncfile, CancelTask cancelTask) {
 
         location = raf.getLocation();
 
         ImageReader imageReader = null;
         ImageInputStream imageInputStream = null;
         try {
 
             imageReader = createImageReader();
             imageInputStream = ImageIO.createImageInputStream(new File(location));
             imageReader.setInput(imageInputStream);
 
             Map<GeoTIFFIIOCoordSys, GeoTIFFIIOCoordSys.Adapter> csAdapterMap =
                     new HashMap<GeoTIFFIIOCoordSys, GeoTIFFIIOCoordSys.Adapter>();
             Set<CSHandler> csHandlerSet = new HashSet<CSHandler>();
 
             int imageCount = imageReader.getNumImages(true);
             for (int imageIndex = 0; imageIndex < imageCount; ++imageIndex) {
 
                 int imageWidthPixels = imageReader.getWidth(imageIndex);
                 int imageHeightPixels = imageReader.getHeight(imageIndex);
                 IIOMetadata imageMetadata = imageReader.getImageMetadata(imageIndex);
 
                 GeoTIFFIIOCoordSys cs = new GeoTIFFIIOCoordSys(
                         imageMetadata, imageWidthPixels, imageHeightPixels);
 
                 if (cs.isSupported()) {
 
                     GeoTIFFIIOCoordSys.Adapter csAdapter = csAdapterMap.get(cs);
                     if (csAdapter == null) {
                         csAdapter = cs.new Adapter(ncfile);
                         csAdapter.generate(csAdapterMap.size());
                         csAdapterMap.put(cs, csAdapter);
                     }
 
                     CSHandler crsHandler = cs.getCSHandler();
                     if (!csHandlerSet.contains(crsHandler)) {
                         Variable crsVariable = crsHandler.generateCRSVariable(ncfile, csHandlerSet.size());
                         ncfile.addVariable(null, crsVariable);
                         csHandlerSet.add(crsHandler);
                     }
 
                     ImageTypeSpecifier imageTypeSpecifier = imageReader.getRawImageType(imageIndex);
                     DataType bandDataType = createDataType(imageTypeSpecifier);
                     String bandDimensions = csAdapter.getDimensionsAsString();
                     String bandCoordinates = csAdapter.getCoordinatesAsString();
                     int bandCount = imageTypeSpecifier.getNumBands();
                     for (int bandIndex = 0; bandIndex < bandCount; ++bandIndex) {
                         Variable dataVariable = new Variable(ncfile, null, null,
                                 "I" + imageIndex + "B" + bandIndex);
                         dataVariable.setDataType(bandDataType);
                         dataVariable.setDimensions(bandDimensions);
                        dataVariable.addAttribute(new Attribute(ATTRIBUTE_IMAGE, 0));
                         dataVariable.addAttribute(new Attribute(ATTRIBUTE_BAND, bandIndex));
                         dataVariable.addAttribute(new Attribute("grid_mapping", crsHandler.getName()));
                         dataVariable.addAttribute(new Attribute("coordinates", bandCoordinates));
                         ncfile.addVariable(null, dataVariable);
                     }
                 }
             }
            ncfile.addAttribute(null, new Attribute("Conventions", "CF-1.4"));
         } catch (IOException e) {
             throw new RuntimeException(e);
         } catch (Exception e) {
             System.out.println(e);
         } finally {
             if (imageReader != null) {
                 imageReader.dispose();
             }
             if (imageInputStream != null) {
                 try { imageInputStream.close(); } catch (IOException e) { }
             }
         }
     }
 
     @Override
     public void close() throws IOException {
         super.close();
     }
 
     @Override
     public Array readData(Variable variable, Section section) throws IOException, InvalidRangeException {
 
         int image = -1;
         int band = -1;
 
         for(Attribute attribute : variable.getAttributes()) {
             if (ATTRIBUTE_IMAGE.equals(attribute.getName())) {
                 image = attribute.getNumericValue().intValue();
             } else if (ATTRIBUTE_BAND.equals(attribute.getName())) {
                 band = attribute.getNumericValue().intValue();
             }
         }
 
         if (image < 0 || band < 0) {
             throw new IllegalArgumentException("ERROR:  Can't extract image "
                     + "or band number from variable.");
         }
 
         ImageReader imageReader = null;
         ImageInputStream imageInputStream = null;
         try {
 
             Range yRange = section.getRange(0);
             Range xRange = section.getRange(1);
 
             imageReader = createImageReader();
 
             if (section.getStride(1) > 1 &&
                 variable.getDataType().isFloatingPoint() &&
                 IMAGEIO_JAI_READER.equals(imageReader.getClass().getName())) {
                 throw new IllegalArgumentException(
                         "ERROR:  Data corruption bug in Java JAI ImageIO TIFF "
                         + " ImageReader with floating point data and X strides > "
                        + " 1.  To utilize this functionality please obatain another"
                         + "JAI TIFF implementation such as ImageIO-Ext and place"
                         + "in the application classpath");
             }
 
             imageInputStream = ImageIO.createImageInputStream(new File(location));
             imageReader.setInput(imageInputStream, true, true);
 
             ImageReadParam imageReadParam = imageReader.getDefaultReadParam();
             imageReadParam.setSourceRegion(new Rectangle(
                     xRange.first(),
                     yRange.first(),
                     xRange.last() - xRange.first() + 1,
                     yRange.last() - yRange.first() + 1));
             imageReadParam.setSourceSubsampling(
                     xRange.stride(),
                     yRange.stride(),
                     0,
                     0);
             imageReadParam.setSourceBands(new int[] { band } );
             imageReadParam.setDestinationBands(new int[] { 0 });
 
             BufferedImage bufferedImage = imageReader.read(image, imageReadParam);
 
             // *POTENTIAL MEMORY ISSUE* Wish we could do this on read.
             // It appears that even with source/destination band specified with
             // a single entry, image comes back with storage for all bands in
             // original image (interleaved, unused bands are set to 0).
             if (imageReader.getRawImageType(image).getNumBands() > 1) {
                 ParameterBlock parameterBlock = new ParameterBlock();
                 parameterBlock.addSource(bufferedImage);
                 parameterBlock.add(new int[] { 0 });
                 PlanarImage planarImage = JAI.create("BandSelect", parameterBlock);
                 bufferedImage = planarImage.getAsBufferedImage();
             }
 
             Raster raster = bufferedImage.getData();
 
             // *POTENTIAL MEMORY ISSUE*  I looked into directly referencing the
             // image data storage but it's not robust given all the SampleModel
             // implementations.
             Object arrayAsObject = createJavaArray(
                     variable.getDataType(), (int)section.computeSize());
 
             raster.getDataElements(
                     0, 0,
                     bufferedImage.getWidth(), bufferedImage.getHeight(),
                     arrayAsObject);
 
             Array array = Array.factory(
                     variable.getDataType(),
                     section.getShape(),
                     arrayAsObject);
 
             return array;
 
         } finally {
             if (imageReader != null) {
                 imageReader.dispose();
             }
             if (imageInputStream != null) {
                 try { imageInputStream.close(); } catch (IOException e) { }
             }
         }
     }
 
     @Override
     public String getFileTypeId() {
         return "GeoTIFF";
     }
 
     @Override
     public String getFileTypeDescription() {
         return "GeoTIFF";
     }
 
     private ImageReader createImageReader() {
         Iterator<ImageReader> imageReaders =
             ImageIO.getImageReadersBySuffix("tiff");
         List<ImageReader> otherReaderList = new ArrayList<ImageReader>();
         ImageReader extReader = null;
         ImageReader jaiReader = null;
         while (imageReaders.hasNext()) {
             ImageReader reader = imageReaders.next();
             String readerClassName = reader.getClass().getName();
             if (IMAGEIO_EXT_READER.equals(readerClassName)) {
                 extReader = reader;
             } else if (IMAGEIO_JAI_READER.equals(readerClassName)){
                 jaiReader = reader;
             } else {
                 otherReaderList.add(reader);
             }
         }
 
         if (extReader != null) {
             return extReader;
         } else if (jaiReader != null) {
             return jaiReader;
         } else if (otherReaderList.size() > 0) {
             return otherReaderList.get(0);
         } else {
             return null;
         }
     }
 
     private DataType createDataType(ImageTypeSpecifier imageTypeSpecifier) {
         switch (imageTypeSpecifier.getSampleModel().getDataType()) {
             case DataBuffer.TYPE_BYTE:
                 return DataType.BYTE;
             case DataBuffer.TYPE_USHORT:
                 return DataType.SHORT; // TODO ??
             case DataBuffer.TYPE_SHORT:
                 return DataType.SHORT;
             case DataBuffer.TYPE_INT:
                 return DataType.INT;
             case DataBuffer.TYPE_FLOAT:
                 return DataType.FLOAT;
             case DataBuffer.TYPE_DOUBLE:
                 return DataType.DOUBLE;
             case DataBuffer.TYPE_UNDEFINED:
             default:
                 return null; // TODO ??
         }
     }
 
     private Object createJavaArray(DataType dataType, int size) {
         switch (dataType) {
             case BYTE:
                 return new byte[size];
             case SHORT:
                 return new short[size];
             case INT:
                 return new int[size];
             case FLOAT:
                 return new float[size];
             case DOUBLE:
                 return new double[size];
             default:
                 return null;
         }
     }
 
     public static void dumpMetaDataXML(IIOMetadata metadata) {
         try {
             Node node = metadata.getAsTree(metadata.getNativeMetadataFormatName());
             Source source = new DOMSource(node);
             Result result = new StreamResult(System.out);
             Transformer transformer = TransformerFactory.newInstance().newTransformer();
             transformer.setOutputProperty(OutputKeys.INDENT, "yes");
             transformer.transform(source, result);
         } catch (Exception e) { }
     }
 
     public static void main(String[] args) throws Exception {
 
         NetcdfFile.registerIOProvider(GeoTiffIOServiceProvider.class);
 
         FeatureDataset fd = FeatureDatasetFactoryManager.open(FeatureType.GRID, "/Users/tkunicki/Downloads/nlcd-9.tiff", null, new Formatter(System.err));
 
         fd.getNetcdfFile().writeCDL(System.out, true);
 
         if (fd instanceof GridDataset) {
             GridDataset gd = (GridDataset)fd;
 //            for (GridDatatype gdt : gd.getGrids()) {
                 GridDatatype gdt = gd.findGridDatatype("I0B0"); {
                 System.out.println(gdt.getName());
                 GridCoordSystem gcs = gdt.getCoordinateSystem();
 
                 CoordinateAxis1D xAxis = (CoordinateAxis1D)gcs.getXHorizAxis();
                 CoordinateAxis1D yAxis = (CoordinateAxis1D)gcs.getYHorizAxis();
 
                 System.out.println(xAxis.getMinValue() + " : " + xAxis.getMaxValue());
                 System.out.println(yAxis.getMinValue() + " : " + yAxis.getMaxValue());
 
                 Array a = gdt.makeSubset(null, null, null, null, new Range(0, 1021, 1), new Range(0,3060,1)).readVolumeData(-1);
                 Double min = Double.MAX_VALUE;
                 Double max = -Double.MAX_VALUE;
                 while (a.hasNext()) {
                     double v = a.nextDouble();
                     if (v < min) min = v;
                     if (v > max) max = v;
                 }
                 System.out.println(min + " : " + max);
             }
         }
 
         fd.close();
 
     }
 
 }
