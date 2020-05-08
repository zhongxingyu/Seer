 /*
  * Master-Thesis work: see https://sites.google.com/site/sifthesis/
  */
 package restful.device;
 
 import com.google.gson.JsonObject;
 import commons.ApiException;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import javax.imageio.stream.ImageInputStream;
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import managers.*;
 import managers.SubscriptionManager.AlreadyExistingSocket;
 import restful.streaming.AbstractStreamingThread;
 import restful.utils.ConditionalAccessResource;
 import restful.utils.UnauthorizedAccessException;
 import tangible.devices.TangibleDevice;
 import utils.ColorHelper;
 
 /**
  *
  * @author leo
  */
 @Path("/tangibleapi/{appuuid}/device_methods/{device_ID}")
 public class RestSpecificDevice extends ConditionalAccessResource {
 //public class RestSpecificDevice extends JSONRestResource {
 
     private static final Object synchronizer = new Object();
     private DeviceFinder _finder = DeviceFinderAccess.getInstance();
     private ReservationManager _mgr = ReservationManagerAccess.getInstance();
     private SubscriptionManager _subs = SubscriptionManagerAccess.getInstance();
 
     public RestSpecificDevice(@PathParam("appuuid") String uuid,
             @PathParam("device_ID") String devID) {
         super(uuid, new Condition[]{Condition.APP_REGISTERED});
         //System.out.println("the received uuid is : "+query_uuid);
         if (!_mgr.isAReservation(devID, _appuuid)) {
             throw new UnauthorizedAccessException("the device " + devID
                     + " is not reseved by the application" + uuid);
         }
     }
 
     @OPTIONS
     @Path("/text_message")
     public Response showTextOptions(
             @HeaderParam("Access-Control-Request-Headers") String requestH,
             @HeaderParam("Origin") String origin) {
         return makeCORS(requestH, origin);
     }
 
     @PUT
     @Path("/text_message")
     public Response showText(
             @PathParam("device_ID") String devID,
             @FormParam("msg") String msg,
             @FormParam("color") @DefaultValue("000000") String color,
             @HeaderParam("Origin") String origin) {
         if (!_mgr.isAReservation(devID, _appuuid)) {
             return this.createErrorMsg(origin, "the device is not reserved by "
                     + "the specified application!", "device: " + devID + " / app: " + _appuuid);
         }
         Integer color_value = null;
         try {
             color_value = Integer.parseInt(color, 16);
         } catch (NumberFormatException ex) {
             return this.createMissingCompulsoryParamMsg(origin, "a color must be specified "
                     + "using the parameters r, g & b or color");
         }
 
         TangibleDevice dev = _finder.getDevice(devID);
         System.out.println("text_message on #" + devID);
         dev.getTalk().showText(msg, color_value);
         return this.createOKCtrlMsg(origin);
     }
 
     @OPTIONS
     @Path("/show_color")
     public Response showColorOptions(
             @HeaderParam("Access-Control-Request-Headers") String requestH,
             @HeaderParam("Origin") String origin) {
         return makeCORS(requestH, origin);
     }
 
     @PUT
     @Path("/show_color")
     public Response showColor(
             @PathParam("device_ID") String devID,
             //@PathParam("appuuid") String appUUID,
             @FormParam("r") Integer r_value,
             @FormParam("g") Integer g_value,
             @FormParam("b") Integer b_value,
             @FormParam("color") String color,
             @HeaderParam("Origin") String origin) {
         Integer color_value;
         System.out.println("show_color on #" + devID);
 //    System.out.println("trying to change the cubes'color!");
 //    System.out.println("here are the variables: \n"
 //        + "\t device_ID " + devID + "\n"
 //        + "\t color " + color + "\n"
 //        + "\t appuuid " + _appuuid);
 //    if(appUUID == null){
 //      return this.createMissingCompulsoryParamMsg("appUUID");
 //    }
         //let's check that the device is reserved by the application
         if (!_mgr.isAReservation(devID, _appuuid)) {
             return this.createErrorMsg(origin, "the device is not reserved by "
                     + "the specified application!", "device: " + devID + " / app: " + _appuuid);
         }
         try {
             color_value = Integer.parseInt(color, 16);
         } catch (NumberFormatException ex) {
             color_value = null;
         }
         //now we know that the request is legal.
         //let's check if the other params are correct too
         if (r_value == null || g_value == null || b_value == null) {
             //one of the component is null ... let's try to use color instead
             if (color_value == null) {
                 return this.createMissingCompulsoryParamMsg(origin, "a color must be specified "
                         + "using the parameters r, g & b or color");
             } else {
                 if (ColorHelper.isValidColor(color_value)) {
                     return this.showColor(origin, color_value, devID);
                 } else {
                     return this.createErrorMsg(origin, "the specified color is not"
                             + " a valid representation of the color", color_value.toString());
                 }
             }
         } else {
             //let's use the three components to print the color on the cubes!
             if (ColorHelper.isValidColor(r_value, g_value, b_value)) {
                 return this.showColor(origin, r_value, g_value, b_value, devID);
             } else {
                 return this.createErrorMsg(origin, "the specified color is not"
                         + " a valid representation of the color",
                         "r:" + r_value + " g:" + g_value + " b:" + b_value);
             }
         }
     }
 
     private Response showColor(
             String origin, int rgb, String devID) {
         int[] rgb_array = ColorHelper.decompose(rgb);
         return showColor(origin, rgb_array[0], rgb_array[1], rgb_array[2], devID);
 
     }
 
     private Response showColor(
             String origin, int r, int g, int b, String devID) {
         TangibleDevice dev = _finder.getDevice(devID);
         dev.getTalk().showColor(r, g, b);
         return this.createOKCtrlMsg(origin);
     }
 
     @OPTIONS
     @Path("/show_picture")
     public Response showPictureOptions(
             @HeaderParam("Access-Control-Request-Headers") String requestH,
             @HeaderParam("Origin") String origin) {
         return makeCORS(requestH, origin);
     }
 
     @PUT
     @Path("/show_picture")
     @Consumes(MediaType.APPLICATION_OCTET_STREAM)
     public Response showPicture(@PathParam("device_ID") String devId, InputStream input,
             @HeaderParam("Origin") String origin) {
         System.out.println("show_picture on #" + devId);
         try {
 //      Date startTime = new Date();
 //      DateFormat format = DateFormat.getInstance();
 //      Logger.getLogger(RestSpecificDevice.class.getName()).log(Level.INFO, "Starting to process the showPicture command : time is -> {0}", format.format(startTime));
             ImageInputStream imgInput = ImageIO.createImageInputStream(input);
             BufferedImage image = ImageIO.read(imgInput);
             TangibleDevice dev = _finder.getDevice(devId);
             dev.getTalk().showPicture(image);
 //      Date endTime = new Date();
 //      Logger.getLogger(RestSpecificDevice.class.getName()).log(Level.INFO, "Ending to process the showPicture command : time is -> {0}", format.format(endTime));
             return this.createOKCtrlMsg(origin);
         } catch (IOException ex) {
             Logger.getLogger(RestSpecificDevice.class.getName()).log(Level.SEVERE, null, ex);
             return this.createErrorMsg(origin, "Could not procceed the picture", "something didn't work with the given picture");
         }
     }
 
     @PUT
     @Path("/show_picture")
     public Response showPictureFromURL(@PathParam("device_ID") String devId,
             @FormParam("url") String pic_url,
             @HeaderParam("Origin") String origin) {
         try {
             URL url = new URL(pic_url);
             BufferedImage img = ImageIO.read(url);
             TangibleDevice dev = _finder.getDevice(devId);
             dev.getTalk().showPicture(img);
            return null;
         } catch (MalformedURLException ex) {
             return this.createErrorMsg(origin, new ApiException(
                     Response.Status.BAD_REQUEST, "the given URL is not malformed"));
         } catch (IOException ex) {
             return this.createErrorMsg(origin, new ApiException(
                     Response.Status.BAD_REQUEST, "the given url couldn't be loaded"));
         }
     }
 
     @OPTIONS
     @Path("/fade_color")
     public Response fadeColorOptions(
             @HeaderParam("Access-Control-Request-Headers") String requestH,
             @HeaderParam("Origin") String origin) {
         return makeCORS(requestH, origin);
     }
 
     @PUT
     @Path("/fade_color")
     public Response fadeColor(
             @PathParam("device_ID") String devId,
             @FormParam("color") String color,
             @HeaderParam("Origin") String origin) {
         Integer color_value;
         try {
             color_value = Integer.parseInt(color, 16);
         } catch (NumberFormatException ex) {
             return this.createMissingCompulsoryParamMsg(origin, "a color must be specified "
                     + "using the parameters r, g & b or color");
         }
         TangibleDevice dev = _finder.getDevice(devId);
         dev.getTalk().fadeColor(color_value);
         return this.createOKCtrlMsg(origin);
     }
 
     @OPTIONS
     @Path("/subscribe")
     public Response subscribeOptions(
             @HeaderParam("Access-Control-Request-Headers") String requestH,
             @HeaderParam("Origin") String origin) {
         return makeCORS(requestH, origin);
     }
 
     @PUT
     @Path("/subscribe")
     public Response addSubscription(
             @PathParam("device_ID") String devId //TODO_LATER add a filter here to register only some events
             , @FormParam("sock_type") String sockType,
             @HeaderParam("Origin") String origin) {
         System.out.println("subscription required for #" + devId);
         //check if there is already a streaming socket for this appuuid
         synchronized (synchronizer) {
             AbstractStreamingThread sTh;
             SubscriptionManager.StreamingThreadType type;
             if (sockType != null && sockType.equals("ws")) {
                 type = SubscriptionManager.StreamingThreadType.WEB_SOCKET;
             } else {
                 type = SubscriptionManager.StreamingThreadType.TCP_SOCKET;
             }
             try {
                 if (_subs.existsStreaming(_appuuid)) {
                     sTh = _subs.getStreamingSocket(_appuuid);
                 } else {
                     try {
                         sTh = _subs.createStreamingSocket(_appuuid, type);
                     } catch (AlreadyExistingSocket ex) {
                         return new RestApiException(origin, ex, true).getResponse();
                     } catch (IOException ex) {
                         JsonObject msg = new JsonObject();
                         msg.addProperty("error", "streaming socket creation failed");
                         return createJsonCtrlResponseMsg(origin, msg, Response.Status.INTERNAL_SERVER_ERROR);
                     }
                 }
                 //setup the subscription
                 _subs.addEventsSubscription(_appuuid, devId);
                 //send back the port
                 JsonObject obj = new JsonObject();
                 int port = (type.equals(SubscriptionManager.StreamingThreadType.WEB_SOCKET))
                         ? _subs.getWsPort() : _subs.getTcpPort();
                 obj.addProperty("port", port);
                 return createJsonCtrlResponseMsg(origin, obj, Response.Status.OK);
             } catch (ApiException ex) {
                 Logger.getLogger(RestSpecificDevice.class.getName()).log(Level.INFO, "", ex);
                 return new RestApiException(origin, ex, true).getResponse();
             }
         }
     }
 }
