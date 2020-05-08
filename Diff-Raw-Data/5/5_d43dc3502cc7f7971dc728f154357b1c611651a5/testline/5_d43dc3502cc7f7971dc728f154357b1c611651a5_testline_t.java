 import java.io.OutputStream;
 import java.io.FileOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 import java.io.IOException;
 import java.lang.IllegalArgumentException;
 import scijava.roi.shape.Line;
 import scijava.roi.types.Vertex3D;
 import scijava.roi.types.LinePoints3D;
 
 class testline
 {
     public static void main(String[] args)
     {
         System.out.println("Running model tests");
 
         {
             Line l1 = new Line();
             double l1len = l1.length();
 
             System.out.println("L1: " + l1 + " len=" + l1len);
         }
 
         {
             Vertex3D p1 = new Vertex3D(2,2,2);
             Vertex3D p2 = new Vertex3D(4,4,4);
             Line l2 = new Line(p1, p2);
             double l2len = l2.length();
 
             System.out.println("L2: " + l2 + " len=" + l2len);
         }
 
         {
             Vertex3D p1 = new Vertex3D(2,2,0);
             Vertex3D p2 = new Vertex3D(4,4,0);
             Vertex3D pa[] = {p1, p2};
             LinePoints3D lp = new LinePoints3D(pa);
             Line l3 = new Line(lp);
             double l3len = l3.length();
 
             System.out.println("L3: " + l3 + " len=" + l3len);
         }
 
         try
             {
                 Vertex3D p1 = new Vertex3D(2,2,5);
                 Vertex3D p2 = new Vertex3D(4,4,4);
                 Vertex3D p3 = new Vertex3D(4,2,5);
                 Vertex3D pa[] = {p1, p2, p3};
                 LinePoints3D lp = new LinePoints3D(pa);
                 Line l4 = new Line(lp);
                 double l4len = l4.length();
 
                 System.out.println("L4 (FAIL): " + l4 + " len=" + l4len);
             }
         catch (java.lang.IllegalArgumentException e)
             {
                 System.out.println("L4: Construction failed (expected): " + e);
             }
 
         try
             {
                 Vertex3D p1 = new Vertex3D(2,2,5);
                 Vertex3D pa[] = {p1};
                 LinePoints3D lp = new LinePoints3D(pa);
                 Line l5 = new Line(lp);
                 double l5len = l5.length();
 
                 System.out.println("L5 (FAIL): " + l5 + " len=" + l5len);
             }
         catch (java.lang.IllegalArgumentException e)
             {
                 System.out.println("L5: Construction failed (expected): " + e);
             }
 
         // Output a ROI.
         {
             Vertex3D p1 = new Vertex3D(204.5653,187.6969,0);
             Vertex3D p2 = new Vertex3D(53.9834,19.7242,4);
             Line l6 = new Line(p1, p2);
 
             output(l6, "lineroi1");
         }
     }
 
     static void output(Line line, String filename)
     {
         LinePoints3D pts = line.getPoints();
 
         // Icy XML format.
         try
             {
                 FileOutputStream ficy;
                 ficy = new FileOutputStream(filename + ".xml");
                new PrintStream(ficy).printf("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<root>\n<roi>\n<classname>icy.roi.ROI2DLine</classname>\n<id>1</id>\n<name>Line2D</name>\n<color>-7703041</color>\n<selected_color>-3014684</selected_color>\n<stroke>2</stroke>\n<selected>true</selected>\n<z>-1</z>\n<t>-1</t>\n<c>-1</c>\n<pt1>\n<color>-3014684</color>\n<selected_color>-1</selected_color>\n<pos_x>%s</pos_x>\n<pos_y>%s</pos_y>\n<ray>6</ray>\n<visible>true</visible>\n</pt1>\n<pt2>\n<color>-3014684</color>\n<selected_color>-1</selected_color>\n<pos_x>%s</pos_x>\n<pos_y>%s</pos_y>\n<ray>6</ray>\n<visible>true</visible>\n</pt2>\n</roi>\n</root>\n",
                     pts.points[0].vertex[0],
                     pts.points[0].vertex[1],
                     pts.points[1].vertex[0],
                     pts.points[1].vertex[1]);
                 ficy.close();
             }
         catch (IOException e)
             {
                 System.err.println ("Unable to write Icy roi.xml");
             }
 
         // Simple text format.
         try
             {
                 FileOutputStream ftxt;
                 ftxt = new FileOutputStream(filename + ".txt");
                 new PrintStream(ftxt).printf("LINE %s,%s,%s,%s\n",
                     pts.points[0].vertex[0],
                     pts.points[0].vertex[1],
                     pts.points[1].vertex[0],
                     pts.points[1].vertex[1]);
                 ftxt.close();
             }
         catch (IOException e)
             {
                 System.err.println ("Unable to write Icy roi.xml");
             }
 
         // ImageJ binary format.
         try
             {
                 String name = "SJROIMODEL";
                 byte data[] = new byte[128+(name.length()*2)];
 
                 double x1 = pts.points[0].vertex[0];
                 double y1 = pts.points[0].vertex[1];
                 double x2 = pts.points[1].vertex[0];
                 double y2 = pts.points[1].vertex[1];
 
                 data[0]=73; data[1]=111; data[2]=117; data[3]=116;
                 putShort(data, 4, 223); // VERSION
                 data[6] = (byte)3; // LINE
                 putShort(data, 8,  (short) Math.floor(Math.min(y1,y2))); // TOP
                 putShort(data, 10, (short) Math.floor(Math.min(x1,x2))); // LEFT
                 putShort(data, 12, (short) Math.ceil(Math.max(y1,y2))); // BOTTOM
                 putShort(data, 14, (short) Math.ceil(Math.max(x1,x2))); // RIGHT
 
                 putShort(data, 16, 2); // NCOORD
                 putInt(data, 56, 0); // POSITION
 
                 putFloat(data, 18, (float)x1); // X1
                 putFloat(data, 22, (float)y1); // Y1
                 putFloat(data, 26, (float)x2); // X2
                 putFloat(data, 30, (float)y2); // Y2
 
                putShort(data, 34, 2); // LINEWID
                 putInt(data, 40, 0xff0000ff); // LINECOL
                 putInt(data, 44, 0); // FILLCOL
 
                 for (int i=0; i<name.length(); i++)
                     putShort(data, 128+i*2, name.charAt(i));
 
                 OutputStream fbin;
                 fbin = new FileOutputStream(filename+".roi");
                 fbin.write(data);
                 fbin.close();
             }
         catch (IOException e)
             {
                 System.err.println ("Unable to write Icy roi.xml");
             }
     }
 
     static void putShort(byte data[], int base, int v) {
 		data[base] = (byte)(v>>>8);
 		data[base+1] = (byte)v;
     }
 
 	static void putFloat(byte data[], int base, float v) {
 		int tmp = Float.floatToIntBits(v);
 		data[base]   = (byte)(tmp>>24);
 		data[base+1] = (byte)(tmp>>16);
 		data[base+2] = (byte)(tmp>>8);
 		data[base+3] = (byte)tmp;
 	}
 
 	static void putInt(byte data[], int base, int i) {
 		data[base]   = (byte)(i>>24);
 		data[base+1] = (byte)(i>>16);
 		data[base+2] = (byte)(i>>8);
 		data[base+3] = (byte)i;
 	}
 
 }
