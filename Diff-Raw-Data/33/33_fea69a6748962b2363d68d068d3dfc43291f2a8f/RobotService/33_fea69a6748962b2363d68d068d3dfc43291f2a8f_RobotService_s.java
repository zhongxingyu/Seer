 package ru.aemelin.awt.robot.remote.rest;
 
 import javax.servlet.ServletContext;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import java.awt.*;
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dolf
  * Date: 22.02.13
  */
 @Path("/")
 public class RobotService {
     private static final java.util.List<Color> COLORS = new ArrayList<Color>(){{
         add(new Color(0x00, 0xCC, 0x00));
         add(new Color(0xFF, 0x74, 0x00));
         add(new Color(0xCD, 0x00, 0x74));
     }};
    private static final int COLOR_ACCURACY = 40;
     private static Robot robot;
 
     static {
         try {
             robot = new Robot();
         } catch (AWTException e) {
             e.printStackTrace();
         }
     }
 
     @GET
     @Produces({MediaType.TEXT_PLAIN})
     @Path("/getRGBPixelColor/{x}/{y}/")
     public Response getRGBPixelColor(@Context ServletContext context,
                                      @PathParam("x") final int x, @PathParam("y") final int y) {
         if(x < 0 || y < 0){
             return Response.status(Response.Status.BAD_REQUEST).build();
         }
         try{
             int color = robot.getPixelColor(x, y).getRGB();
             return Response.ok(String.valueOf(color), MediaType.TEXT_PLAIN).build();
 
         }
         catch(Exception e){
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
         }
     }
 
     @GET
     @Produces({MediaType.TEXT_PLAIN})
     @Path("/getBrowserOffset/")
     public Response getBrowserOffset(@Context ServletContext context) {
         try{
             for(int x = 0; x < 30; x++){
                 for(int y = 0; y < 150; y += 3){
                     Color color = robot.getPixelColor(x, y);
                     int index = findColor(COLORS, color);
                     if(index > -1){
                         int dy = y - index;
                         if(dy > -1){
                             boolean contains = true;
                             for(Color standard : COLORS){
                                 contains &= eq(robot.getPixelColor(x, dy++), standard, COLOR_ACCURACY);
                             }
                             if(contains){
                                 String p = x + "," + (y - index);
                                 return Response.ok(p, MediaType.TEXT_PLAIN).build();
                             }
                         }
                     }
                 }
             }
             return Response.status(Response.Status.NOT_FOUND).build();
 
         }
         catch(Exception e){
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
         }
     }
 
     private int findColor(java.util.List<Color> colors, Color color){
         int index = -1;
         for(Color standard : colors){
             if(eq(standard, color, COLOR_ACCURACY)){
                 index = colors.indexOf(standard);
             }
         }
         return index;
     }
 
     private boolean eq(Color color1, Color color2, int accuracy){
         boolean eq = Math.abs(Math.abs(color1.getRed()) - Math.abs(color2.getRed())) < accuracy;
         eq &= Math.abs(Math.abs(color1.getGreen()) - Math.abs(color2.getGreen())) < accuracy;
         eq &= Math.abs(Math.abs(color1.getBlue()) - Math.abs(color2.getBlue())) < accuracy;
         return eq;
     }
 
     @POST
     @Produces({MediaType.TEXT_PLAIN})
     @Path("/keyPress/{x}/")
     public Response keyPress(@Context ServletContext context, @PathParam("x") final int x) {
         try{
             robot.keyPress(x);
             return Response.ok().build();
 
         }
         catch(Exception e){
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
         }
     }
 
     @POST
     @Produces({MediaType.TEXT_PLAIN})
     @Path("/keyRelease/{x}/")
     public Response keyRelease(@Context ServletContext context, @PathParam("x") final int x) {
         try{
             robot.keyRelease(x);
             return Response.ok().build();
 
         }
         catch(Exception e){
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
         }
     }
 
     @POST
     @Produces({MediaType.TEXT_PLAIN})
     @Path("/mouseMove/{x}/{y}/")
     public Response mouseMove(@Context ServletContext context,
                                      @PathParam("x") final int x, @PathParam("y") final int y) {
         if(x < 0 || y < 0){
             return Response.status(Response.Status.BAD_REQUEST).build();
         }
         try{
             robot.mouseMove(x, y);
             return Response.ok().build();
 
         }
         catch(Exception e){
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
         }
     }
 
     @POST
     @Produces({MediaType.TEXT_PLAIN})
     @Path("/mousePress/{x}/")
     public Response mousePress(@Context ServletContext context, @PathParam("x") final int x) {
         try{
             robot.mousePress(x);
             return Response.ok().build();
 
         }
         catch(Exception e){
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
         }
     }
 
     @POST
     @Produces({MediaType.TEXT_PLAIN})
     @Path("/mouseRelease/{x}/")
     public Response mouseRelease(@Context ServletContext context, @PathParam("x") final int x) {
         try{
             robot.mouseRelease(x);
             return Response.ok().build();
 
         }
         catch(Exception e){
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
         }
     }
 
     @POST
     @Produces({MediaType.TEXT_PLAIN})
     @Path("/mouseWheel/{x}/")
     public Response mouseWheel(@Context ServletContext context, @PathParam("x") final int x) {
         if(x < 0) return Response.status(Response.Status.BAD_REQUEST).build();
         try{
             robot.mouseWheel(x);
             return Response.ok().build();
 
         }
         catch(Exception e){
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
         }
     }
 }
