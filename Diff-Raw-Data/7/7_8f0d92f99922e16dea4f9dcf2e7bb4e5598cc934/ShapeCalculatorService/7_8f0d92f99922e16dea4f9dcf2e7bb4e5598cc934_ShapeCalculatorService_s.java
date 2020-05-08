 package controllers;
 
 import static controllers.Shapes.CIRCLE;
 import static controllers.Shapes.RECTANGLE;
 import static controllers.Shapes.TRIANGLE;
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * @author Joe
  */
 public class ShapeCalculatorService {
     private static final double NULL_VALUE = -1;
     
     public static double calculateFromRequest(HttpServletRequest request){
         double result = -1;
         String sParam = request.getParameter("s");
         if (sParam != null) {
             Shapes shape = Shapes.toShape(Integer.parseInt(sParam));
             if (shape == null) {
                 //invalid s value
             }
             switch (shape) {
                 case RECTANGLE:
                     double width = getParameterDouble(request, "width");
                     double length = getParameterDouble(request, "length");
                     //double width = Double.parseDouble(request.getParameter("width"));
                     //double length = Double.parseDouble(request.getParameter("length"));
                     if (width != NULL_VALUE && length != NULL_VALUE) {
                         result = width * length;
                     } else {
                         //error?
                     }
 
                     break;
                 case CIRCLE:
                     double radius = getParameterDouble(request, "radius");
                     //double radius = Double.parseDouble(request.getParameter("radius"));
                     if (radius != NULL_VALUE) {
                         result = Math.PI * Math.pow(radius, 2);
                     } else {
                         //error
                     }
                     break;
                 case TRIANGLE:
                     double a = getParameterDouble(request, "a");
                     double b = getParameterDouble(request, "b");
                     double c = getParameterDouble(request, "c");
                     /*
                      String aStr = request.getParameter("a");
                      String bStr = request.getParameter("b");
                      String cStr = request.getParameter("c");
                      double a = Double.parseDouble(aStr);
                      double b = Double.parseDouble(bStr);
                      double c = Double.parseDouble(cStr);
                      */
                     if (c == NULL_VALUE) {
                         result = Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
                     } else {
                         if (a != NULL_VALUE) {
                            result = Math.sqrt(Math.pow(a, 2) - Math.pow(c, 2));
                         } else if (b != NULL_VALUE) {
                            result = Math.sqrt(Math.pow(b, 2) - Math.pow(c, 2));
                         } else {
                             //error
                         }
                     }
                     break;
             }
 
             
         }
         return result;
     }
     
     private static double getParameterDouble(HttpServletRequest request, String name) {
         String nameValue = request.getParameter(name).trim();
         return (nameValue != null && !nameValue.isEmpty()
                 ? Double.parseDouble(request.getParameter(name))
                 : NULL_VALUE);
     }
 }
