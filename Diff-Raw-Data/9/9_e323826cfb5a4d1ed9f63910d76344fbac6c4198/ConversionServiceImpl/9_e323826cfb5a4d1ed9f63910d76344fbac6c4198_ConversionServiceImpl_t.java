 package org.dawnsci.conversion;
 
 import org.dawb.common.services.conversion.IConversionContext;
 import org.dawb.common.services.conversion.IConversionService;
 import org.dawnsci.conversion.converters.AVIImageConverter;
 import org.dawnsci.conversion.converters.AbstractConversion;
 import org.dawnsci.conversion.converters.AsciiConvert1D;
 import org.dawnsci.conversion.converters.AsciiConvert2D;
 import org.dawnsci.conversion.converters.CompareConverter;
 import org.dawnsci.conversion.converters.Convert1DtoND;
 import org.dawnsci.conversion.converters.CustomNCDConverter;
 import org.dawnsci.conversion.converters.CustomTomoConverter;
 import org.dawnsci.conversion.converters.ImageConverter;
 import org.dawnsci.conversion.converters.ImagesToHDFConverter;
 import org.dawnsci.conversion.converters.VisitorConversion;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Display;
 
 public class ConversionServiceImpl implements IConversionService {
 	
 	static {
 		System.out.println("Starting conversion service.");
 	
 	}
 	public ConversionServiceImpl() {
 		// Important do nothing here, OSGI may start the service more than once.
 	}
 	
 	@Override
 	public IConversionContext open(String... paths) {
 		ConversionContext context = new ConversionContext();
 		try {
 			context.setFilePaths(paths);
 		} catch (Exception e) {
			MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Error opening conversion wizard", e.getMessage());
 			e.printStackTrace();
 		}
 		return context;
 	}
 
 	@Override
 	public void process(IConversionContext context) throws Exception {
 		AbstractConversion deligate=null;
 		try {
 			if (context.getConversionVisitor()!=null) {
 				deligate = new VisitorConversion(context);
 			}
 			if (deligate==null) {
 				switch(context.getConversionScheme()) {
 				case ASCII_FROM_2D:
 					deligate = new AsciiConvert2D(context);
 					break;
 				case ASCII_FROM_1D:
 					deligate = new AsciiConvert1D(context);
 					break;
 				case CUSTOM_NCD:
 					deligate = new CustomNCDConverter(context);
 					break;
 				case TIFF_FROM_3D:
 					deligate = new ImageConverter(context);
 					break;
 				case H5_FROM_IMAGEDIR:
 					deligate = new ImagesToHDFConverter(context);
 					break;
 				case AVI_FROM_3D:
 					deligate = new AVIImageConverter(context);
 					break;
 				case CUSTOM_TOMO:
 					deligate = new CustomTomoConverter(context);
 					break;
 				case H5_FROM_1D:
 					deligate = new Convert1DtoND(context);
 					break;
 				case COMPARE:
 					deligate = new CompareConverter(context);
 					break;
 				default:
 					throw new Exception("No conversion for "+context.getConversionScheme());
 				}
 			}
 			deligate.process(context);
 		} finally {
 			if (deligate!=null) deligate.close(context);
 		}
 	}
 
 }
