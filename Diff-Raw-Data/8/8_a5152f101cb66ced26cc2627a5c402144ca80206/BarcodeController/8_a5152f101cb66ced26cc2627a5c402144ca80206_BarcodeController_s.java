 package controllers;
 
 import com.google.zxing.BinaryBitmap;
 import com.google.zxing.FormatException;
 import com.google.zxing.LuminanceSource;
 import com.google.zxing.NotFoundException;
 import com.google.zxing.common.HybridBinarizer;
 import com.google.zxing.oned.EAN13Reader;
 import helpers.BufferedImageLuminanceSource;
 import jsannotation.JSRoute;
 import org.apache.commons.lang3.StringUtils;
 import play.Logger;
 import play.libs.F;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.Security;
 import play.mvc.WebSocket;
 import views.html.barcode.barcodescanner;
 
 import javax.imageio.IIOImage;
 import javax.imageio.ImageIO;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  * This {@link Controller} is handling barcode scanning with a webcam
  * User: tuxburner
  * Date: 2/11/13
  * Time: 8:19 PM
  */
 @Security.Authenticated(Secured.class)
 public class BarcodeController extends Controller {
 
   /**
    * This initializes the barcodeScanner view
    * @return
    */
   @JSRoute
   public static Result displayBarcodeScaner() {
     return ok(barcodescanner.render());
   }
 
   public static WebSocket<String> scanBarcode() {
     return new WebSocket<String>() {
 
       // Called when the Websocket Handshake is done.
       public void onReady(WebSocket.In<String> in, final WebSocket.Out<String> out) {
 
         // For each event received on the socket,
         String code = null;
         in.onMessage(new F.Callback<String>() {
           public void invoke(String event) {
 
 
             if(StringUtils.startsWith(event,"data:image/jpeg;base64,")) {
                Logger.debug("Got an image. Trying to parse it.");
 
               try {
                 byte decoded[] = new sun.misc.BASE64Decoder().decodeBuffer(StringUtils.removeStart(event, "data:image/jpeg;base64,"));
                 InputStream in = new ByteArrayInputStream(decoded);
                 BufferedImage bufferedImage = ImageIO.read(in);
                 LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
                 BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                 EAN13Reader reader = new EAN13Reader();
                 com.google.zxing.Result decode = reader.decode(bitmap);
                 String decodeText = decode.getText();
                 if(Logger.isDebugEnabled()) {
                   Logger.debug("Got EAN code: "+decodeText+" from image.");
                 }
 
                 out.write(decodeText);
 
               } catch (IOException e) {
                 if(Logger.isErrorEnabled() == true) {
                   Logger.error("An error happend while reading the image.",e);
                 }
                 out.write("error");
               } catch (NotFoundException e) {
                 if(Logger.isErrorEnabled() == true) {
                   Logger.error("Could not extract barcode from image.", e);
                   out.write("error");
                 }
               } catch (FormatException e) {
                 Logger.error("An error happend while reading the image.", e);
                 out.write("error");
               }
             }
           }
         });
 
 
 
         // When the socket is closed.
         in.onClose(new F.Callback0() {
           public void invoke() {
 
 
 
           }
         });
 
       }
 
     };
   }
 
   /*public static Result scanBarcode(final String type, final String data) {
 
     DynamicForm requestData = form().bindFromRequest();
 
     return ok();
   } */
 }
