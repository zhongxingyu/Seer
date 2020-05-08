 import java.io.*;
 import java.awt.*;
 import javax.imageio.ImageIO;
 import java.awt.image.BufferedImage;
 import java.util.Map;
 import java.util.HashMap;
 
 
 public class Img2Html
 {
 
     public static String performPixel(String hexcolor, String tag)
     {
         /**
         *   Return colored pixel in html
         */
 
         return String.format("<%s style='background-color:#%s'></%s>",
                               tag, hexcolor, tag);
     }
 
 
     public static String performHeader(String tag, String size)
     {
         /**
         *   Return html header
         */
 
         return String.format(   "<html><head><style>" +
                                 "%s{width:%spx;height:%spx;float:left;margin:0}" +
                                 "br{display:block;margin:%spx}" +
                                 "</style></head><body>",
                                 tag, size, size, size);
     }
 
 
     public static String performBottom()
     {
         return "</body></html>";
     }
 
     public static Map<String, String> parseOpts(String[] args)
     {
         /**
         *   Get and return options from command line
         */
 
         Map<String, String> opts = new HashMap<String, String>();
 
         // Set default values
         opts.put("format", "html");
         opts.put("tag", "p");
         opts.put("size", "1");
 
         //Parse arguments
         for(int i = 0; i < args.length; ++i) {
             switch(args[i]) {
                 case "--tag":
                     if(i + 1 < args.length)
                         opts.put("tag", args[i + 1]);
                     break;
 
                 case "--size":
                     if(i + 1 < args.length)
                         opts.put("size", args[i + 1]);
                     break;
 
                 case "--help":
                     output( "--tag (pixel tag, default: <p>),\n" +
                                        "--size (pixel size, default: 1),\n" +
                                        "...");
             }
         }
 
         return opts;
     }
 
 
     public static void output(String str)
     {
         /**
         *   Just output argument string in some file or stdout
         *
         *   TODO: output to file
         */
 
         System.out.println(str);
     }
 
 
     public static void main(String[] args) {
         // Get command line options
         Map<String, String> options = parseOpts(args);
 
         // Open image file
         try {
 
             // args[0] must be
             if(args.length == 0) {
                 System.err.println("No input file");
                 System.exit(1);
             }
 
             File file = new File(args[0]);
             BufferedImage image = ImageIO.read(file);
 
             int pixel;
             String hexcolor;
 
             performHeader(options.get("tag"), options.get("size"));
             output(performHeader(options.get("tag"), options.get("size")));
 
             for(int y = 0; y < image.getHeight(); ++y) {
                 for(int x = 0; x < image.getWidth(); ++x) {
                     // Get pixel info
                     pixel = image.getRGB(x, y);
                     // Get pixel color in hex
                     hexcolor = Integer.toHexString(pixel & 0x00FFFFFF);
 
                     // Perform html element
                     output(performPixel(hexcolor, options.get("tag")));
 
                     // TODO: alpha channel
                 }
                 output("<br />");
 
             }
 
             output(performBottom());
 
 
         }
 
         catch(IOException e) {
             System.err.println("Something is wrong with your file");
         }
 
     }
 }
