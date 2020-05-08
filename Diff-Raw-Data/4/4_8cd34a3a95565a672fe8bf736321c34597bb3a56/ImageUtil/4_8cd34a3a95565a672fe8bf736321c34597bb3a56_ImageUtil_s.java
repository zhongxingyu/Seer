 import java.awt.Rectangle;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Iterator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.imageio.ImageIO;
 import javax.imageio.ImageReader;
 import javax.imageio.stream.ImageInputStream;
 
 import org.apache.batik.css.parser.ParseException;
 import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
 import org.apache.batik.dom.svg.SVGDOMImplementation;
 import org.apache.batik.dom.util.XLinkSupport;
 import org.apache.batik.util.XMLResourceDescriptor;
 import org.w3c.dom.Attr;
 import org.w3c.dom.DOMImplementation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.events.EventTarget;
 
 import com.googlecode.javacpp.BytePointer;
 import com.googlecode.javacpp.Loader;
 import com.googlecode.javacv.cpp.opencv_core.IplImage;
 import com.googlecode.javacv.cpp.opencv_objdetect;
 
 import static com.googlecode.javacv.cpp.opencv_core.*;
 import static com.googlecode.javacv.cpp.opencv_imgproc.*;
 import static com.googlecode.javacv.cpp.opencv_highgui.*;
 
 public class ImageUtil {
 	private static final String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
 	
 	static {
 		Loader.load(opencv_objdetect.class);
 	}
 
 	public static File convertToBitmap(File imageFile, Rectangle imageSize) {
 		String path = getTmpDirectory();
 		String tmpFilename = path + "tmp" + getExtension(imageFile);
 		File tmpFile = new File(tmpFilename);
 		tmpFile.deleteOnExit();
 		File tmpOutFile = new File(path + "tmpout.bmp");
 		tmpOutFile.deleteOnExit();
 		try {
 			copyFile(imageFile, tmpFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 		
 		String outFilename = path + imageFile.getName();
 		outFilename = outFilename.replaceAll("\\.[^.]*$", ".bmp");
 		
 		IplImage image_source = cvLoadImage(tmpFile.getPath());
 		CvSize size_source = image_source.cvSize();
 		
 		// Color detection
 		IplImage image_hsv = cvCreateImage( size_source, IPL_DEPTH_8U, 3);
 		cvCvtColor(image_source, image_hsv, CV_RGB2HSV);
 		CvScalar mean = cvScalarAll(0);
 		CvScalar std_dev= cvScalarAll(0);
 
 		cvAvgSdv(image_hsv, mean, std_dev, null);
 		boolean isColorImage = std_dev.val(1) > 3.0f;
 		boolean containsIllust = std_dev.val(2) > 40.0f;
 		boolean isComplicatedIllust = std_dev.val(2) > 85.0f;
 		cvReleaseImage(image_hsv);
 		
 		// Binalize
 		double scale = 2;
 		CvSize size_target = new CvSize((int)(image_source.width()*scale), (int)(image_source.height()*scale));
 		if (imageSize != null) {
 			imageSize.width = image_source.width();
 			imageSize.height = image_source.height();
 		}
 
 		IplImage image_grey = cvCreateImage( size_source, IPL_DEPTH_8U, 1);
 		cvCvtColor( image_source, image_grey, CV_BGR2GRAY );
 		cvReleaseImage(image_source);
 				
 		IplImage image_target = cvCreateImage( size_target, IPL_DEPTH_8U, 1 );
 		IplImage image_edge   = cvCreateImage( size_target, IPL_DEPTH_8U, 1 );
 		
 		cvResize(image_grey, image_target, CV_INTER_LANCZOS4);
 
 		try {
 			
 			if (!isColorImage) {
 				if (!containsIllust) {
 					int blockSize = 31;
 					cvAdaptiveThreshold( image_target , image_edge, 255,
 							 CV_ADAPTIVE_THRESH_MEAN_C,
 							 CV_THRESH_BINARY_INV, blockSize, 5 );
 					cvNot(image_edge, image_target);
 				} else if (isComplicatedIllust) {
 					if (std_dev.val(2) > 85.0f) {
 						return null;
 					}
 					IplImage image_simple = cvCreateImage( size_source, IPL_DEPTH_8U, 1);
 					cvSmooth (image_grey, image_simple, CV_BILATERAL, 10, 10, 60, 40);
 					cvResize(image_simple, image_target, CV_INTER_LINEAR);
 					cvThreshold(image_target, image_target, 0, 255,	CV_THRESH_BINARY | CV_THRESH_OTSU);
 					cvReleaseImage(image_simple);
 					/*
 					int blockSize = 41;
 					IplImage image_smooth = cvCreateImage( size_target, IPL_DEPTH_8U, 1);
 					cvSmooth (image_target, image_smooth, CV_BILATERAL, 14, 14, 60, 40);
 					cvSaveImage(tmpOutFile.getPath() + ".smooth.bmp", image_smooth);
 					cvResize(image_smooth, image_target, CV_INTER_LINEAR);
 					cvReleaseImage(image_smooth);
 	
 					cvAdaptiveThreshold( image_target , image_edge, 255,
 							 CV_ADAPTIVE_THRESH_MEAN_C,
 							 CV_THRESH_BINARY_INV, blockSize, 9 );
 					
 					IplImage image_beta = cvCreateImage( size_target, IPL_DEPTH_8U, 1 );
 					cvThreshold(image_target, image_beta, 0, 255,	CV_THRESH_BINARY_INV | CV_THRESH_OTSU);
 					
 					cvOr(image_edge, image_beta, image_target, null);
 					cvNot(image_target, image_target);
 					cvReleaseImage(image_beta);
 		*/
 				} else {
 					int blockSize = 41;
 					cvAdaptiveThreshold( image_target , image_edge, 255,
 							 CV_ADAPTIVE_THRESH_MEAN_C,
 							 CV_THRESH_BINARY_INV, blockSize, 9 );
 					
 					IplImage image_beta = cvCreateImage( size_target, IPL_DEPTH_8U, 1 );
 					cvThreshold(image_target, image_beta, 0, 255,	CV_THRESH_BINARY_INV | CV_THRESH_OTSU);
 					
 					cvOr(image_edge, image_beta, image_target, null);
 					cvNot(image_target, image_target);
 					cvReleaseImage(image_beta);
 				}
 			} else {
 				// color
 				if (true) {
 					return null;
 				} else { // tone
 					IplImage image_tone = cvCreateImage( size_source, IPL_DEPTH_8U, 1 );
 					floydSteinberg(image_grey, image_tone);
 					cvReleaseImage(image_grey);
 					
 					cvNot(image_tone, image_tone);
 					
 					IplImage image_tone2 = cvCreateImage( size_target, IPL_DEPTH_8U, 1 );
 					cvResize(image_tone, image_tone2, CV_INTER_NN);
 					cvReleaseImage(image_tone);
 	
 					IplImage mask = cvCreateImage( size_target, IPL_DEPTH_8U, 1 );
 					cvThreshold(image_target, mask, 0, 255,	CV_THRESH_BINARY_INV | CV_THRESH_OTSU);
 	
 					cvCopy(image_edge, image_target);
 					cvOr(image_edge, image_tone2, image_target, mask);
 					cvNot(image_target, image_target);
 					
 					cvReleaseImage(image_tone2);
 					cvReleaseImage(mask);
 				}
 			}
 /*
 			cvNot(mask, mask);
 			cvNot(image_edge, image_edge);
 			cvNot(image_tone2, image_tone2);
 			
 			cvSaveImage(tmpOutFile.getPath() + ".mask.bmp", mask);
 			cvSaveImage(tmpOutFile.getPath() + ".edge.bmp", image_edge);
 			cvSaveImage(tmpOutFile.getPath() + ".tone2.bmp", image_tone2);
 */
 			cvSaveImage(tmpOutFile.getPath(), image_target);
 		} finally {
 			cvReleaseImage(image_target);
 			cvReleaseImage(image_edge);
 			cvReleaseImage(image_grey);
 		}
 		File outFile = new File(outFilename);
 		outFile.deleteOnExit();
 		try {
 			copyFile(tmpOutFile, outFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 
 		
 		return outFile;
 	}
 	
 	static void floydSteinberg(IplImage in, IplImage out) {
 		int[][] pattern = new int[][] {
 				{ 0,  1, 7},
 				{ 1, -1, 3},
 				{ 1,  0, 5},
 				{ 1,  1, 1}				
 		};
 
 		byte[] outBytes = new byte[in.widthStep() * in.height()];
 		in.imageData().get(outBytes);
 		
 		int index;
 		double e;
 		for (int j=0; j<out.height()-1; j++) {
 			for (int i=1; i<out.widthStep()-1; i++) {
 				index = j * out.widthStep() + i;
 				int c = (int)(outBytes[index] & 0xFF);
 				if (c > 127) {
 					e = (double)(c - 255);
 					outBytes[index] = (byte)-1;
 
 				} else {
 					e = (double)c;
 					outBytes[index] = (byte)0;
 				}
 				
 				for (int[] pat : pattern) {
 					index = (j + pat[0]) * out.widthStep() + (i + pat[1]);
 					c = (int) (outBytes[index] & 0xff) + (int)(e * pat[2] / 16.0f);
 					c = (c > 127) ? c - 256 : c;
 					outBytes[index] = (byte)c;
 				}
 			}
 		}
 		
 		out.imageData().put(outBytes);
 	}
 	
 	private static String getExtension(File file) {
 		int index = file.getName().lastIndexOf(".");
 		if (index > 0) {
 			return file.getName().substring(index);
 		}
 		return null;
 	}
 
 	static private String getTmpDirectory() {
 		String path = System.getProperty("java.io.tmpdir") + "/svgEpub/";
 		File tmp = new File(path);
 		if (!tmp.exists()) {
 			tmp.mkdirs();
 		}
 		return path;
 	}
 	
 	public static void copyFile(File source, File dest) throws IOException {
 		if (!dest.exists()) {
 			dest.createNewFile();
 		}
 		InputStream in = null;
 		OutputStream out = null;
 		try {
 			in = new FileInputStream(source);
 			out = new FileOutputStream(dest);
 
 			// Transfer bytes from in to out
 			byte[] buf = new byte[1024];
 			int len;
 			while ((len = in.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 		} finally {
 			if (in != null) {
 				in.close();
 			}
 			if (out != null) {
 				out.close();
 			}
 		}
 	}
 
 	public static Rectangle getImageSize(File file) {
     	String extension = PathUtil.getExtension(file.getName());
     	
     	Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix(extension);
         ImageReader imageReader = (ImageReader) readers.next();
     	Rectangle rect = new Rectangle();
     	FileInputStream stream = null;
 		try {
 			stream = new FileInputStream(file);
 	        ImageInputStream imageInputStream = ImageIO.createImageInputStream(stream);
 	        imageReader.setInput(imageInputStream, false);
 	        rect.width = imageReader.getWidth(0);
 	        rect.height = imageReader.getHeight(0);
 			stream.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return rect;
 	}
 
 	public static Rectangle getSvgSize(String newURI) {
 		Rectangle rect = new Rectangle(0, 0, 584, 754);
 		String parser = XMLResourceDescriptor.getXMLParserClassName();
 		SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
 		try {
 			Document doc = f.createDocument(newURI);
 			Element svgRoot = doc.getDocumentElement();
 			
 			Attr width = svgRoot.getAttributeNodeNS(null, "width");		
 			if (width != null) {
 				rect.width = parse(width.getValue());
 			}
 	
 			Attr height = svgRoot.getAttributeNodeNS(null, "height");
 			if (height != null) {
 				rect.height = parse(height.getValue());
 			}
 		} catch (Exception e) {
 			
 		}
 		
 		return rect;
 	}
 
 	private static int parse(String value) {
 		Pattern p = Pattern.compile("(\\d+)(.*)");
 		Matcher m = p.matcher(value);
 		if (m.find()) {
 			return Integer.parseInt(m.group(1));
 		}
 		throw new ParseException(null);
 	}
 	
 	
 	public static Document createSvgDocument(Rectangle clipRect, Rectangle imageRect, String imageURI, boolean isPreview) {
 		DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
		
 		Rectangle rootRect = isPreview ? clipRect : imageRect;
 		Document doc = impl.createDocument(svgNS, "svg", null);		
 		Element svgRootOuter = doc.getDocumentElement();
 		svgRootOuter.setAttributeNS(null , "width", "100%");
 		svgRootOuter.setAttributeNS(null , "height", "100%");
 		svgRootOuter.setAttributeNS(null, "viewBox", 
 				String.format("0 0 %d %d",  rootRect.width, rootRect.height));
 		svgRootOuter.setAttributeNS(null, "preserveAspectRatio", "xMidYMid meet");
 
 		Element image = doc.createElementNS(svgNS, "image");
 		image.setAttributeNS(null, "width", Integer.toString(imageRect.width));
 		image.setAttributeNS(null, "height", Integer.toString(imageRect.height));
 		XLinkSupport.setXLinkHref(image,  imageURI);
 //		image.setAttributeNS(null, "xlink:href", imageURI);
 
 		if (isPreview) {
 			Element svgRootInner = (Element) doc.createElementNS(svgNS, "svg");
 			svgRootInner.setAttribute("id", "root_inner");
 			svgRootInner.setAttributeNS(null , "width", Integer.toString(clipRect.width));
 			svgRootInner.setAttributeNS(null , "height", Integer.toString(clipRect.height));
 			svgRootInner.setAttributeNS(null, "viewBox", 
 					String.format("%d %d %d %d",  clipRect.x, clipRect.y, clipRect.width, clipRect.height));
 			svgRootInner.setAttributeNS(null, "preserveAspectRatio", "xMidYMid slice");
 			
 			svgRootOuter.appendChild(svgRootInner);
 			svgRootInner.appendChild(image);
 		} else {
 			svgRootOuter.appendChild(image);
 		}
 		return doc;
 	}
 }
