 package splitcuber.image;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.imageio.ImageIO;
 
 import splitcuber.error.DictionaryError;
 import splitcuber.error.ImageError;
 import splitcuber.error.WebFetchError;
 import splitcuber.web.ContentType;
 import splitcuber.web.FileFetcher;
 
 public class MagicCardsInfoSingleCardImage extends SingleCardImage {
 
     private static final String HOST = "magiccards.info";
     private static final String PATH = "/query";
     private static final String QUERY = "v=card&s=cname&q=!";
 
     public MagicCardsInfoSingleCardImage(String name, File imagefile, boolean alreadDone) throws ImageError {
         super(name, imagefile);
         if (!alreadDone) {
             crop(6, 9, 299, 426);
             resize(200, 285);
             rotateSideways();
         }
     }
 
     public static MagicCardsInfoSingleCardImage fetchByName(String name, boolean forceReload) throws ImageError,
             DictionaryError, MalformedURLException, WebFetchError, IOException {
 
         MagicCardsInfoSingleCardImage magicCardsInfoImage;
         File path = new File(IMAGE_PATH);
         if (!path.exists()) {
             path.mkdir();
         }
         File image = new File(IMAGE_PATH + name + ".jpg");
         if (image.exists() && !forceReload) {
             // aus 'cache'
             magicCardsInfoImage = new MagicCardsInfoSingleCardImage(name, image, true);
         } else {
             URI uri;
             try {
                uri = new URI("http", HOST, PATH, QUERY + name, null); 
                //lim-dul fix
                String txtURL = uri.toString();
                txtURL = txtURL.replaceAll("\u00FB", "%C3%BB");
                uri = new URI(txtURL);
             } catch (URISyntaxException e) {
                 throw new MalformedURLException("URI Syntax: " + e.getMessage());
             }
            
             String textHTML = FileFetcher.fetchText(uri.toURL());
             // String regex = "(scan)";
             String regex = "(http:\\/\\/magiccards\\.info\\/scans\\/[\\w\\/]+\\.jpg)";
             Pattern pattern = Pattern.compile(regex);
             Matcher matcher = pattern.matcher(textHTML);
 
             String imageURLtxt = null;
             if (matcher.find()) {
                 imageURLtxt = matcher.group(1);
             } else {
                 throw new WebFetchError("Could not match image URL");
             }
             URL imageURL = new URL(imageURLtxt);
             FileFetcher.fetchFile(imageURL, ContentType.JPEG, image);
             magicCardsInfoImage = new MagicCardsInfoSingleCardImage(name, image, false);
             ImageIO.write(magicCardsInfoImage.image, "JPG", image);
         }
         return magicCardsInfoImage;
     }
 }
