 
 //Quick and dirty java implementation of phase one for CSE 408 project 1
 //Needs some cleanup, addition of other color space traversal methods, better error
 // handling, finish parsing command line options, and various other things (see TODO's)
 package mis.project1;
 
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import javax.imageio.ImageIO;
 
 public class Main {
 
     public static void main(String[] args) 
     {
         //Declarations and Initializations (To defaults if no command line args used to override)
         BufferedReader input1;
         BufferedReader input2;
         String f1, f2; //input files
         String outputfile = "output.bmp";
         double[][] data1 = new double[20][];
         double[][] data2 = new double[20][];
         double[][] diff = new double[20][];
         int B = 1; //Beta value
         int maxlength;
         int[] imagedata;
         int width;
         int padding = 20;
         int thickness = 10;
         int height = thickness*20 + 22*padding;
         int color1 = 0xffffff; //lower color
         int color2 = 0x000000; //upper color
         double c1a = 0, c1b = 0, c1c = 0; //lower color
        double c2a = 50, c2b = 500, c2c = -200; //upper color
         BufferedImage output;
         int background = 0xffffff;
         int colorspace = 0;
         
         
         //Parse command line arguments
         //TODO: Add man page type thing on -h
         for(int i = 0; i < args.length-2; i++)
         {
             switch(args[i])
             {
                 //Output file flag
                 case "-o":
                     outputfile = args[i+1];
                     i++;
                     break;
                 //Thickness flag
                 //TODO: check input validity
                 case "-t":
                     thickness = Integer.parseInt(args[i+1]);
                     i++;
                     break;
                 //Padding flag
                 //TODO: check input validity
                 case "-p":
                     padding = Integer.parseInt(args[i+1]);
                     i++;
                     break;
                 //Beta flag
                 case "-B":
                     B = Integer.parseInt(args[i+1]);
                     i++;
                     break;
                 //Background color toggle flag
                 case "-b":
                     background = 0x000000;
                     break;
                 //Colorspace flag
                 case "-c":
                     switch(args[i+1])
                     {
                         case "RGB":
                             colorspace = 0;
                             break;
                         case "YUV":
                             colorspace = 1;
                             break;
                         case "YIQ":
                             colorspace = 2;
                             break;
                         case "HSL":
                             colorspace = 3;
                             break;
                         case "XYZ":
                             colorspace = 4;
                             break;
                         case "Lab":
                             colorspace = 5;
                             break;
                         case "YCbCr":
                             colorspace = 6;
                             break;
                         default:
                             System.err.println("Color space not recognized");
                             return;
                     }
                     i++;
                     break;
                 //Lower color flag
                 case "-l":
                     //TODO: Needs to check for validity in input
                     if(colorspace == 0 || colorspace == 6)
                     {
                         color1 = Integer.decode(args[i+1]);
                         i++;
                     }
                     else
                     {
                         c1a = Float.parseFloat(args[i+1]);
                         c1b = Float.parseFloat(args[i+2]);
                         c1c = Float.parseFloat(args[i+3]);
 			i+=3;
                     }
                     break;
                 //Upper color flag
                 case "-u":
                     //TODO: Needs to check for validity in input
                     if(colorspace == 0 || colorspace == 6)
                     {
                         color2 = Integer.decode(args[i+1]);
                         i++;
                     }
                     else
                     {
                         c2a = Float.parseFloat(args[i+1]);
                         c2b = Float.parseFloat(args[i+2]);
                         c2c = Float.parseFloat(args[i+3]);
 			i+=3;
                     }
                     break;
                 default:
                     System.err.println("Unrecognized flag");
                     return;
             }
         }
         //Input files
         f1 = args[args.length-2];
         f2 = args[args.length-1];
         
         //Open the two file readers
         try
         {
             input1 = new BufferedReader(new FileReader(f1));
             input2 = new BufferedReader(new FileReader(f2));
         }
         catch(FileNotFoundException e)
         {
             System.err.println(e);
             return;
         }
         
         //Read and parse the data for files
         try
         {
             String line;
             //Executes 20 times (for the given data sets)
             for(int j = 0; j < 20; j++)
             {
                 //Read and parse data for file 1
                 line = input1.readLine();
                 String temp[] = line.split(",");
                 data1[j] = new double[temp.length];
                 for(int i = 0; i < temp.length; i++)
                 {
                     data1[j][i] = Double.parseDouble(temp[i]);
                 }
                 //Read and parse data for file 2
                 line = input2.readLine();
                 temp = line.split(",");
                 data2[j] = new double[temp.length];
                 for(int i = 0; i < temp.length; i++)
                 {
                     data2[j][i] = Double.parseDouble(temp[i]);
                 }
             }
         }
         catch(IOException e)
         {
             System.err.println(e);
         }
         
         //Figure out which data set is longer, and store maxlength
         if(data1[0].length > data1[0].length)
             maxlength = data1[0].length;
         else
             maxlength = data2[0].length;
         
         //Calculate the difference and perform scaling (outer loop runs 20 times, inner runs variable (maxlength) amount)
         for(int j = 0; j < 20; j++)
         {
             double max = 0;
             diff[j] = new double[maxlength];
             for(int i = 0; i < maxlength; i++)
             {
                 if((data1[j].length > i) && (data2[j].length > i))
                 {
                     diff[j][i] = Math.abs(data1[j][i]-data2[j][i]);
                 }
                 else if(data1[j].length > i)
                 {
                     diff[j][i] = Math.abs(data1[j][i]);
                 }
                 else
                 {
                     diff[j][i] = Math.abs(data2[j][i]);
                 }
                 if(diff[j][i] > max)
                     max = diff[j][i];
             }
             //scale by max in sensor set
             for(int i = 0; i < maxlength; i++)
             {
                 diff[j][i] = diff[j][i]/max;
                 diff[j][i] = Math.pow(diff[j][i], B);
             }
         }
         
         //Call appropriate color space traversal function
         int temp[] = new int[maxlength*20];
         for(int j = 0; j < 20; j++)
         {
             for(int i = 0; i < diff[j].length; i++)
             {
                 //TODO: add other colorspaces
                 switch(colorspace)
                 {
                     case 0:
                         temp[(maxlength*j)+i] = getRGBColor(color1, color2, diff[j][i]);
                         break;
                     case 1:
                         temp[(maxlength*j)+i] = getYUVColor(c1a, c1b, c1c, c2a, c2b, c2c, diff[j][i]);
                         break;
                     case 2:
                         temp[(maxlength*j)+i] = getYIQColor(c1a, c1b, c1c, c2a, c2b, c2c, diff[j][i]);
                         break;
                     case 3:
                         temp[(maxlength*j)+i] = getHSLColor(c1a, c1b, c1c, c2a, c2b, c2c, diff[j][i]);
                         break;
      		    case 4:
 			temp[(maxlength*j)+i] = getXYZColor(c1a, c1b, c1c, c2a, c2b, c2c, diff[j][i]);
 			break;
                     case 5:
 			temp[(maxlength*j)+i] = getLabColor(c1a, c1b, c1c, c2a, c2b, c2c, diff[j][i]);
 			break;
 		    case 6:
 			temp[(maxlength*j)+i] = getYCbCrColor(color1,color2,diff[j][i]);
 			break;
 
                     default:
                         System.err.println("Something broke when choosing color space");
                         return;
                 }
             }
         }
         
         //Set image width and instantiate imagedata/image
         width = maxlength*thickness + 2*padding;
         imagedata = new int[width*height];
         output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
         
         //Draw background then populate imagedata
         for(int i = 0; i < imagedata.length; i++)
             imagedata[i] = background;
         for(int j = 0; j < 20; j++)
         {
             int r,q;
             for(int i = 0; i < diff[j].length; i++)
             {
                 //Sets integers in appropriate width x width area
                 // to the specified color rgb value calculated from above
                 for(r = 0; r < thickness; r++)
                     for(q = 0; q < thickness; q++)
                         imagedata[(padding + j*(thickness+padding)+r)*width + (padding+i*thickness + q)] = temp[(maxlength*j)+i];
             }
         }
         
         //Create image from data and write to file
         output.setRGB(0, 0, width, height, imagedata, 0, width);
         try
         {
             ImageIO.write(output, "bmp", new File(outputfile));
             System.out.println("Image successfully generated");
         }
         catch(IOException e)
         {
             System.err.println(e);
         }
     }
     
     //Takes two packed integers (RGB, 8 bits each) and a double (the diff val) and returns
     // another packed integer in RGB (also 8 bits each).
     //Works by drawing a vector between the two points in 3d space and scales using the in value
     // where 0 is the first point and 1 is the second point.
     public static int getRGBColor(int c1, int c2, double in)
     {
         int c1r, c1b, c1g;
         int c2r, c2b, c2g;
         int c12r, c12b, c12g;
         int result;
         
         //unpack components in c1/c2
         c1b = c1&0x0000ff;
         c2b = c2&0x0000ff;
         c1g = (c1>>8)&0x0000ff;
         c2g = (c2 >> 8)&0x0000ff;
         c1r = (c1>>16)&0x0000ff;
         c2r = (c2 >> 16)&0x0000ff;
         
         //calculate direction vector
         c12r = c2r-c1r;
         c12g = c2g-c1g;
         c12b = c2b-c1b;
         
         //scale, repack, and return
         result = (int) (c1r+in*c12r);
         result = (result << 8) | (int) (c1g+in*c12g);
         result = (result << 8) | (int) (c1b+in*c12b); 
         
         return result;
     }
 
     public static int getYCbCrColor(int c1, int c2, double in)
     {
 	int c1y, c1cb, c1cr;
 	int c2y, c2cb, c2cr;
 	int c12y, c12cb, c12cr;
 	double y, cb, cr;
 	int r, g, b;
 	int result;
 	
 	c1cr = c1&0x0000ff;
 	c2cr = c2&0x0000ff;
 	c1cb = (c1>>8)&0x0000ff;
 	c2cb = (c2>>8)&0x0000ff;
 	c1y = (c1>>16)&0x0000ff;
 	c2y = (c2>>16)&0x0000ff;
 	
 	c12y = c2y-c1y;
 	c12cb = c2cb-c1cb;
 	c12cr = c2cr-c1cr;
 
 	//find point on vector
 	y = c1y+in*c12y;
 	cb = c1cb+in*c12cb;
 	cr = c1cr+in*c12cr;
 
 	r = (int) (y +                    1.402*(cr-128));
 	g = (int) (y - 0.34414*(cb-128) - 0.71414*(cb-128));
 	b = (int) (y + 1.772*(cb-128)                     );
 
 	result = r;
 	result = (result << 8) | g;
 	result = (result << 8) | b;
 
 	return result;
     }
     
     //Traverses the YUV color space and returns the clamped RGB values
     //Inputs are normalized floats in the range [0,1]
     public static int getYUVColor(double c1y, double c1u, double c1v, double c2y, double c2u, double c2v, double in)
     {
         //Constants and variables
         double umax = 0.436;
         double vmax = 0.615;
         double c12y, c12u, c12v;
         double y, u, v;
         int r, g, b;
         int result;
         
         //Unnormalize
         c1u = (c1u*2*umax)-umax;
         c2u = (c2u*2*umax)-umax;
         c1v = (c1v*2*vmax)-vmax;
         c2v = (c2v*2*vmax)-vmax;
         
 
         //calculate direction vector
         c12y = c2y-c1y;
         c12u = c2u-c1u;
         c12v = c2v-c1v;
         
         //scale and convert to RGB
         y = c1y+in*c12y;
         u = c1u+in*c12u;
         v = c1v+in*c12v;
         
         r = (int) ((y             + 1.13983*v)*255); //red
         g = (int) ((y - 0.39465*u - 0.58060*v)*255); //green
         b = (int) ((y + 2.03211*u            )*255); //blue
         
         //Clamp if necessary
         if(r > 255)
         {
             System.err.println("Out of range (clamping value to fit in RGB)");
             r = 255;
         }
         else if(r < 0)
         {
             System.err.println("Out of range (clamping value to fit in RGB)");
             r = 0;
         }
         if(g > 255)
         {
             System.err.println("Out of range (clamping value to fit in RGB)");
             g = 255;
         }
         else if(g < 0)
         {
             System.err.println("Out of range (clamping value to fit in RGB)");
             g = 0;
         }
         if(b > 255)
         {
             System.err.println("Out of range (clamping value to fit in RGB)");
             b = 255;
         }
         else if(b < 0)
         {
             System.err.println("Out of range (clamping value to fit in RGB)");
             b = 0;
         }
         
         //repack and return
         result = r;
         result = (result << 8) | g;
         result = (result << 8) | b;
         return result;
     }
 
     //Traverses the YIQ color space and returns the clamped RGB values
     public static int getYIQColor(double c1y, double c1i, double c1q, double c2y, double c2i, double c2q, double in)
     {
         //Constants and variables
         double imax = 0.5957;
         double qmax = 0.5226;
         double c12y, c12i, c12q;
         double y, i, q;
         int r, g, b;
         int result;
         
         //Unnormalize
         c1i = (c1i*2*imax)-imax;
         c2i = (c2i*2*imax)-imax;
         c1q = (c1q*2*qmax)-qmax;
         c2q = (c2q*2*qmax)-qmax;
 
         //calculate direction vector
         c12y = c2y-c1y;
         c12i = c2i-c1i;
         c12q = c2q-c1q;
         
         //scale and convert to RGB
         y = c1y+in*c12y;
         i = c1i+in*c12i;
         q = c1q+in*c12q;
         
         r = (int) ((y + 0.9563*i + 0.6210*q)*255); //red
         g = (int) ((y - 0.2721*i - 0.6474*q)*255); //green
         b = (int) ((y - 1.1070*i + 1.7046*q)*255); //blue
         
         //Clamp if necessary
         if(r > 255)
         {
             System.err.println("Out of range (clamping value to fit in RGB)");
             r = 255;
         }
         else if(r < 0)
         {
             System.err.println("Out of range (clamping value to fit in RGB)");
             r = 0;
         }
         if(g > 255)
         {
             System.err.println("Out of range (clamping value to fit in RGB)");
             g = 255;
         }
         else if(g < 0)
         {
             System.err.println("Out of range (clamping value to fit in RGB)");
             g = 0;
         }
         if(b > 255)
         {
             System.err.println("Out of range (clamping value to fit in RGB)");
             b = 255;
         }
         else if(b < 0)
         {
             System.err.println("Out of range (clamping value to fit in RGB)");
             b = 0;
         }
         
         //repack and return
         result = r;
         result = (result << 8) | g;
         result = (result << 8) | b;
         return result;
     }
 
     public static int getXYZColor(double c1x, double c1y, double c1z, double c2x, double c2y, double c2z, double in)
     {
 	//Constants/vars
 	double c12x, c12y, c12z;
 	double x, y, z;
 	int r, g, b;
 	int result;
 
         //Get direction vector between the two 3D XYZ color instance points
 	c12x = c2x - c1x;
 	c12y = c2y - c1y;
 	c12z = c2z - c1z;
 
 	//compute input point along vector between floor and ceiling
 	x = c1x+in*c12x;
 	y = c1y+in*c12y;
 	z = c1z+in*c12z;
 
 	//convert to RGB with inverted transform
 	r = (int) ((3.50645*x - 1.74019*y -0.543868*z)*255);
 	g = (int) ((-1.06926*x + 1.97786*y + 0.0350523*z)*255);
 	b = (int) ((0.0564385*x - 0.197016*y + 1.05014*z)*255);
 
         //Clamp if necessary                                                                                                                                
         if(r > 255)
 	    {
 		System.err.println("Out of range (clamping value to fit in RGB)");
 		r = 255;
 	    }
         else if(r < 0)
 	    {
 		System.err.println("Out of range (clamping value to fit in RGB)");
 		r = 0;
 	    }
         if(g > 255)
 	    {
 		System.err.println("Out of range (clamping value to fit in RGB)");
 		g = 255;
 	    }
         else if(g < 0)
 	    {
 		System.err.println("Out of range (clamping value to fit in RGB)");
 		g = 0;
 	    }
         if(b > 255)
 	    {
 		System.err.println("Out of range (clamping value to fit in RGB)");
 		b = 255;
 	    }
         else if(b < 0)
 	    {
 		System.err.println("Out of range (clamping value to fit in RGB)");
 		b = 0;
 	    }
                                                                              
         //repack and return                                                                                                                                 
         result = r;
         result = (result << 8) | g;
         result = (result << 8) | b;
         return result;
     }
     
     public static int getHSLColor(double c1h, double c1s, double c1l, double c2h, double c2s, double c2l, double in)
     {
         double r, g, b;
         double h, s, l;
         int result;
         double c, x, m;
         double c12h, c12s, c12l;
         double hmax = 360;
         
         //Unnormalize
         c1h = (c1h*hmax);
         c2h = (c2h*hmax);
 
         //calculate direction vector
         c12h = c2h-c1h;
         c12s = c2s-c1s;
         c12l = c2l-c1l;
         
         //scale and convert to RGB
         h = c1h+in*c12h;
         s = c1s+in*c12s;
         l = c1l+in*c12l;
         
         c = (1-Math.abs(2*l-1))*s;
         x = c*(1-Math.abs(((h/60)%2)-1));
         m = l - c/2;
         
         if(h < 60 && h >= 0)
         {
             r = c;
             g = x;
             b = 0;
         }
         else if(h < 120 && h >= 60)
         {
             r = x;
             g = c;
             b = 0;
         }
         else if(h < 180 && h >= 120)
         {
             r = 0;
             g = c;
             b = x;
         }
         else if(h < 240 && h >= 180)
         {
             r = 0;
             g = x;
             b = c;
         }
         else if(h < 300 && h >= 240)
         {
             r = x;
             g = 0;
             b = c;
         }
         else if(h <= 360 && h >= 300)
         {
             r = c;
             g = 0;
             b = x;
         }
         else
         {
             System.err.println("Error occurred with HSL input.");
             return -1;
         }
         
         r = (r + m)*255;
         g = (g + m)*255;
         b = (b + m)*255;
         
         
         //Clamp if necessary                                                                                                                                
         if(r > 255)
         {
             System.err.println("Out of range (clamping value to fit in RGB)");
             r = 255;
         }
         else if(r < 0)
         {
             System.err.println("Out of range (clamping value to fit in RGB)");
             r = 0;
         }
         if(g > 255)
         {
             System.err.println("Out of range (clamping value to fit in RGB)");
             g = 255;
         }
         else if(g < 0)
         {
             System.err.println("Out of range (clamping value to fit in RGB)");
             g = 0;
         }
         if(b > 255)
         {
             System.err.println("Out of range (clamping value to fit in RGB)");
             b = 255;
         }
         else if(b < 0)
         {
             System.err.println("Out of range (clamping value to fit in RGB)");
             b = 0;
         }
         
         //repack and return                                                                                                                                 
         result = (int) (r);
         result = (result << 8) | (int)(g);
         result = (result << 8) | (int)(b);
         return result;
     }
     
     public static int getLabColor(double c1l, double c1a, double c1b, double c2l, double c2a, double c2b, double in)
     {
 	//Constants/vars
 	double c12l, c12a, c12b;
 	double l, a, b2;
         double x, y, z;
 	int r, g, b;
 	int result;
         
         //Unnormalize
         c1l *= 100;
         c2l *= 100;
 
         
         //Get direction vector between the two color instance points
 	c12l = c2l - c1l;
 	c12a = c2a - c1a;
 	c12b = c2b - c1b;
 
 	//scale
 	l = c1l+in*c12l;
 	a = c1a+in*c12a;
 	b2 = c1b+in*c12b;
 
         //Convert to XYZ
         y = (l + 16)/116;
         x = y + a/500;
         z = y - b2/200;
         
         if(x > 0.2068965517241379)
             x = Math.pow(x,3);
         else
             x = (x - 16/116)/(7.787);
         if(y > 0.2068965517241379)
             y = Math.pow(y,3);
         else
             y = (y - 16/116)/(7.787);
         if(z > 0.2068965517241379)
             z = Math.pow(z,3);
         else
             z = (z - 16/116)/(7.787);
         
         
 	//convert to RGB from XYZ with inverted transform
 	r = (int) ((2.3706743*x - 0.9000405*y - 0.4706338*z)*255);
 	g = (int) ((-0.5138850*x + 1.4253036*y + 0.0885814*z)*255);
 	b = (int) ((0.0052982*x - 0.0146949*y + 1.0093968*z)*255);
         
         //Clamp if necessary                                                                                                                                
         if(r > 255)
 	    {
 		System.err.println("Out of range (clamping value to fit in RGB)");
 		r = 255;
 	    }
         else if(r < 0)
 	    {
 		System.err.println("Out of range (clamping value to fit in RGB)");
 		r = 0;
 	    }
         if(g > 255)
 	    {
 		System.err.println("Out of range (clamping value to fit in RGB)");
 		g = 255;
 	    }
         else if(g < 0)
 	    {
 		System.err.println("Out of range (clamping value to fit in RGB)");
 		g = 0;
 	    }
         if(b > 255)
 	    {
 		System.err.println("Out of range (clamping value to fit in RGB)");
 		b = 255;
 	    }
         else if(b < 0)
 	    {
 		System.err.println("Out of range (clamping value to fit in RGB)");
 		b = 0;
 	    }
                                                                              
         //repack and return                                                                                                                                 
         result = r;
         result = (result << 8) | g;
         result = (result << 8) | b;
         return result;
     }
 }
