 package fonten;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.blobstore.BlobKey;
 //import com.google.appengine.api.blobstore.BlobstoreService;
 //import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 import com.google.appengine.api.files.FileService;
 import com.google.appengine.api.files.FileServiceFactory;
 import com.google.appengine.api.files.AppEngineFile;
 import com.google.appengine.api.files.FileReadChannel;
 import com.google.appengine.api.memcache.MemcacheService;
 import com.google.appengine.api.memcache.MemcacheServiceFactory;
 
 
 import com.google.typography.font.sfntly.Font;
 import com.google.typography.font.sfntly.FontFactory;
 import com.google.typography.font.sfntly.Tag;
 import com.google.typography.font.sfntly.data.WritableFontData;
 //import com.google.typography.font.sfntly.table.core.CMapTable;
 import com.google.typography.font.tools.conversion.eot.EOTWriter;
 import com.google.typography.font.tools.conversion.woff.WoffWriter;
 import com.google.typography.font.tools.fontinfo.FontUtils;
 import com.google.typography.font.tools.subsetter.HintStripper;
 import com.google.typography.font.tools.subsetter.Subsetter;
 import com.google.typography.font.tools.subsetter.RenumberingSubsetter;
 
 //import org.apache.commons.codec.binary.Base64;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.BufferedInputStream;
 import java.nio.channels.Channels;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Logger;
 //import java.util.HashSet;
 import java.util.List;
 import java.util.Date;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class FontSubsetter extends HttpServlet {
     private static final Logger LOGGER = Logger.getLogger(FontSubsetter.class.getName());
     private static final String OPT_EOT = "eot";
     private static final String OPT_WOFF = "woff";
     //private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
     private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
     private MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
 
     public enum FontFormat {
         Undef, Eot, Woff
     }
 
         
     @Override
     public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException {
         try {
             String fontID = req.getParameter("id").toLowerCase();
             String text = req.getParameter("text");
             String format = req.getParameter("format");
             String strip = req.getParameter("strip");
             String token = req.getParameter("token");
            
            text = getTextFromMemcacheOrDatastore(text, token);
             LOGGER.info(text);
             
             //unique values in string
             //Set<String> temp = new HashSet<String>(Arrays.asList(text));
             //text = temp.toArray().toString();
             //String text = sText;
             
             //decide font format
             FontFormat fontFormat = FontFormat.Undef;
             if( format == OPT_EOT){
             	fontFormat = FontFormat.Eot;
             }else if( format == OPT_WOFF){
             	fontFormat = FontFormat.Woff;
             }
             
             //decide strip hinting
             boolean hinting = true;
             if( strip == "0" || strip == "false"){
             	hinting = false;
             }
             
             BlobKey blobKey = getBlobKeyFromMemcacheOrDatastore(fontID);
             
             Font font = getFontFromBlobstore(blobKey);
 
         	subsetFont(font, text, fontFormat, hinting, res);
         	
         } catch (Exception ex) {
             throw new ServletException(ex);
         }
     }
     
     private String getTextFromMemcacheOrDatastore(String text, String token) throws ServletException{
     	try{
 	    	if( text == null && token != null){
 	        	String tokenText = (String) memcache.get(token);
 	        	if( tokenText != null){
 	        		text = tokenText;
 	        	}else{
 	        		Key rKey = KeyFactory.createKey("Reservation", token);
 	        		Entity reservation = datastore.get(rKey);
 	        		text = reservation.getProperty("text").toString();
 	        	}
 	        }
 	    	return text;
     	} catch (Exception ex){
     		throw new ServletException(ex);
     	}
     }
     
     private BlobKey getBlobKeyFromMemcacheOrDatastore(String fontID) throws ServletException{
     	try{
 	    	BlobKey blobKey;
 	    	String cachedBlobKey;
 	        cachedBlobKey = (String) memcache.get(fontID);
 	        if( cachedBlobKey != null){
 	        	blobKey = new BlobKey(cachedBlobKey.toString());
 	        }else{
 	        	//get font from id
 	        	Key fontKey = KeyFactory.createKey("Font", fontID);
 	        	Entity entityFont = datastore.get(fontKey);
 	        	LOGGER.info(entityFont.toString());
 	        	
 	        	blobKey = new BlobKey(entityFont.getProperty("blobkey").toString());
 	        	LOGGER.info(blobKey.getKeyString());
 	        	memcache.put(fontID, blobKey.getKeyString());
 	        }
 	        return blobKey;
     	} catch (Exception ex){
     		throw new ServletException(ex);
     	}
     }
     
     private Font getFontFromBlobstore(BlobKey blobKey) throws ServletException{
     	try{
     		FileService fileService = FileServiceFactory.getFileService();
             AppEngineFile fontFile = fileService.getBlobFile(blobKey);
             FileReadChannel readChannel = fileService.openReadChannel(fontFile, false);
             //BufferedReader reader = new BufferedReader(Channels.newReader(readChannel, "UTF8"));
             BufferedInputStream biStream = new BufferedInputStream(Channels.newInputStream(readChannel));
         	Font font = FontUtils.getFonts(biStream)[0];
         	return font;
     	} catch (IOException ex){
     		throw new ServletException(ex);
     	}
     }
     
     private void subsetFont(
             Font font, String text, FontFormat format, boolean hinting, HttpServletResponse res)
             throws IOException {
     	boolean mtx = false;
     	
     	Font newFont = font;
     	FontFactory fontFactory = FontFactory.getInstance();
     	if( text != null){
     	
 	    	//List<CMapTable.CMapId> cmapIds = new ArrayList<CMapTable.CMapId>();
 	        //cmapIds.add(CMapTable.CMapId.WINDOWS_BMP);
 	        
 	    	Subsetter subsetter = new RenumberingSubsetter(newFont, fontFactory);
 	    	//subsetter.setCMaps(cmapIds, 1);
 	    	List<Integer> glyphs = GlyphCoverage.getGlyphCoverage(font, text);
 	    	subsetter.setGlyphs(glyphs);
 	    	Set<Integer> removeTables = new HashSet<Integer>();
 	    	
 	    	//update tables
 	    	removeTables.add(Tag.GDEF);
 	        removeTables.add(Tag.GPOS);
 	        removeTables.add(Tag.GSUB);
 	        removeTables.add(Tag.kern);
 	        removeTables.add(Tag.hdmx);
 	        removeTables.add(Tag.vmtx);
 	        removeTables.add(Tag.VDMX);
 	        removeTables.add(Tag.LTSH);
 	        removeTables.add(Tag.DSIG);
 	        removeTables.add(Tag.intValue(new byte[]{'m', 'o', 'r', 't'}));
 	        removeTables.add(Tag.intValue(new byte[]{'m', 'o', 'r', 'x'}));
 	        subsetter.setRemoveTables(removeTables);
 	        newFont = subsetter.subset().build();
     	}
         
     	//strip hinting
     	if( hinting ){
     		Subsetter hintStripper = new HintStripper(newFont, fontFactory);
     		Set<Integer> removeTables = new HashSet<Integer>();
 	    	removeTables.add(Tag.fpgm);
 	        removeTables.add(Tag.prep);
 	        removeTables.add(Tag.cvt);
 	        removeTables.add(Tag.hdmx);
 	        removeTables.add(Tag.VDMX);
 	        removeTables.add(Tag.LTSH);
 	        removeTables.add(Tag.DSIG);
 	        hintStripper.setRemoveTables(removeTables);
 	        newFont = hintStripper.subset().build();
     	}
     	
         //set cache
     	res.setHeader("Last-Modified", new Date().toString());
     	res.setHeader("Cache-Control", "max-age=86400, public");
     	
     	//allow cross domain for firefox
     	res.setHeader("Access-Control-Allow-Origin","*");
     	
     	//output font file by format
         OutputStream os = res.getOutputStream();
         if (format == FontFormat.Eot) {
             res.setContentType("application/vnd.ms-fontobject");
             WritableFontData eotData = new EOTWriter(mtx).convert(newFont);
             eotData.copyTo(os);
         } else if (format == FontFormat.Woff) {
             res.setContentType("application/x-font-woff");
             WritableFontData woffData = new WoffWriter().convert(newFont);
             woffData.copyTo(os);
         } else {
         	//res.setContentType("application/x-font-ttf");
         	fontFactory.serializeFont(newFont, os);
         }
     }
 }
