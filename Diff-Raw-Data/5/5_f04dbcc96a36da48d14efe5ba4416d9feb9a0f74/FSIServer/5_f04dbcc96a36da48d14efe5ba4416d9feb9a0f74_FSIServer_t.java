 package iiif;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * Unsupported: bitonal, rotation, does not distort aspect ratio
  */
 public class FSIServer implements ImageServer {
     private String baseurl;
     private Map<String, ImageInfo> image_info_cache;
     private static int MAX_IMAGE_INFO_CACHE_SIZE = 1000;
 
     public FSIServer(String baseurl) {
         this.baseurl = baseurl;
         this.image_info_cache = new ConcurrentHashMap<String, ImageInfo>();
     }
 
     // TODO always lookup image?
 
     public String constructURL(IIIFImageRequest req) throws IIIFException {
         String url = baseurl + "?type=image";
         url += "&" + param("source", req.getImage());
 
         ImageInfo info = lookupImage(req.getImage());
 
         if (info == null) {
             return null;
         }
 
         if (req.getFormat() == ImageFormat.PNG) {
             url += "&" + param("profile", "png");
         } else if (req.getFormat() == null
                 || req.getFormat() == ImageFormat.JPG) {
             url += "&" + param("profile", "jpeg");
         } else {
             throw new IIIFException("format unsupported", "format");
         }
 
         Region reg = req.getRegion();
 
         double left, top, right, bottom;
 
         if (reg.getType() == Region.Type.FULL) {
             left = 0.0;
             top = 0.0;
             right = 1.0;
             bottom = 1.0;
         } else if (reg.getType() == Region.Type.ABSOLUTE) {
             double width = (double) info.getWidth();
             double height = (double) info.getHeight();
 
             left = reg.getX() / width;
             top = reg.getY() / height;
             right = left + (reg.getWidth() / width);
             bottom = top + (reg.getHeight() / height);
 
         } else if (reg.getType() == Region.Type.PERCENTAGE) {
             left = reg.getPercentageX() / 100.0;
             top = reg.getPercentageY() / 100.0;
             right = left + (reg.getPercentageWidth() / 100.0);
             bottom = top + (reg.getPercentageHeight() / 100.0);
         } else {
             throw new IIIFException("region unsupported", "region");
         }
 
         // FSI docs say this should be left,top,right,bottom but it actually
         // needs left,top,width,height all as percentages
 
         url += "&"
                 + param("rect", left + "," + top + "," + (right - left) + ","
                         + (bottom - top));
 
         Size scale = req.getSize();
 
         int width = -1, height = -1;
 
         if (scale.getType() == Size.Type.BEST_FIT) {
             width = scale.getWidth();
             height = scale.getHeight();
         } else if (scale.getType() == Size.Type.EXACT) {
             width = scale.getWidth();
             height = scale.getHeight();
         } else if (scale.getType() == Size.Type.EXACT_HEIGHT) {
             width = -1;
             height = scale.getHeight();
         } else if (scale.getType() == Size.Type.EXACT_WIDTH) {
             width = scale.getWidth();
             height = -1;
         } else if (scale.getType() == Size.Type.FULL) {
         } else if (scale.getType() == Size.Type.PERCENTAGE) {
             width = (int) ((right - left) * info.getWidth() * (scale
                     .getPercentage() / 100));
             height = (int) ((bottom - top) * info.getHeight() * (scale
                     .getPercentage() / 100));
         } else {
             throw new IIIFException("scale unsupported", "scale");
         }
 
         if (width != -1) {
             url += "&" + param("width", "" + width);
         }
 
         if (height != -1) {
             url += "&" + param("height", "" + height);
         }
 
         String effects = null;
 
         if (req.getQuality() == Quality.NATIVE) {
 
         } else if (req.getQuality() == Quality.COLOR) {
 
         } else if (req.getQuality() == Quality.GREY) {
            effects = "desaturate(lightness),";
         } else {
             throw new IIIFException("quality unsupported", "quality");
         }
 
         if (effects != null) {
            url += "&" + param("effects", effects);
         }
 
         if (req.getRotation() != 0.0) {
             throw new IIIFException("rotation unsupported", "rotation");
         }
 
         return url;
     }
 
     private String param(String name, String value) {
         try {
             return name + "=" + URLEncoder.encode(value, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException(e);
         }
     }
 
     public ImageInfo lookupImage(String image) throws IIIFException {
         ImageInfo info = image_info_cache.get(image);
 
         if (info != null) {
             return info;
         }
 
         info = new ImageInfo();
         info.setId(image);
         info.setTileWidth(1000);
         info.setTileHeight(1000);
         info.setQualities(Quality.NATIVE, Quality.COLOR);
         info.setFormats(ImageFormat.JPG, ImageFormat.PNG);
 
         String url = baseurl + "?type=info&tpl=info";
         url += "&" + param("source", image);
 
         DocumentBuilderFactory docFactory = DocumentBuilderFactory
                 .newInstance();
 
         try {
             DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
             URLConnection con = new URL(url).openConnection();
             con.connect();
 
             if (con instanceof HttpURLConnection) {
                 if (((HttpURLConnection) con).getResponseCode() == 404) {
                     return null;
                 }
             }
 
             InputStream is = con.getInputStream();
             Document doc = docBuilder.parse(is);
             is.close();
 
             NodeList widths = doc.getElementsByTagName("Width");
             NodeList heights = doc.getElementsByTagName("Height");
 
             if (widths.getLength() > 0) {
                 String s = widths.item(0).getAttributes().getNamedItem("value")
                         .getTextContent();
                 info.setWidth(Integer.parseInt(s));
             }
 
             if (heights.getLength() > 0) {
                 String s = heights.item(0).getAttributes()
                         .getNamedItem("value").getTextContent();
                 info.setHeight(Integer.parseInt(s));
             }
         } catch (ParserConfigurationException e) {
             throw new IIIFException(e);
         } catch (SAXException e) {
             throw new IIIFException(e);
         } catch (NumberFormatException e) {
             throw new IIIFException(e);
         } catch (IOException e) {
             throw new IIIFException(e);
         }
 
         if (image_info_cache.size() > MAX_IMAGE_INFO_CACHE_SIZE) {
             image_info_cache.clear();
         }
 
         image_info_cache.put(image, info);
 
         return info;
     }
 
     public int compliance() {
         return 1;
     }
 }
