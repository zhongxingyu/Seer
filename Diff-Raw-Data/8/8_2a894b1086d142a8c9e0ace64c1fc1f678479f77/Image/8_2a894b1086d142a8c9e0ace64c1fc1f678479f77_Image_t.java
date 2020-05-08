 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import java.io.*;
 import java.util.Arrays;
 
 /**
  *
  * @author Paul
  */
 public class Image {
   byte[] data;
   public int maxval;
   public int width;
   public int height;
 
   public Image(String filename) throws Exception {
     FileReader fr = new FileReader(filename);
     BufferedReader br = new BufferedReader(fr);
 
     if(!br.readLine().equals("P3"))
       throw new Exception("Unsupported format");
     String[] dims = br.readLine().split(" ");
         
     width = Integer.parseInt(dims[0]);
     height = Integer.parseInt(dims[1]);
     maxval = Integer.parseInt(br.readLine());
 
     data = new byte[height*width*3];
 
     int read = 0;
     while(read < data.length) {
       String[] values = br.readLine().trim().split("\\s+");
       for (String b : values)
         data[read++] = (byte)(Integer.parseInt(b) - 128);
     }
     br.close();
     fr.close();
   }
 
   @Override
   public boolean equals(Object object) {
     if (object == null) return false;
     if (object == this) return true;
     if (!(object instanceof Image)) return false;
     Image other = (Image) object;
 
     if (this.maxval != other.maxval
             || this.height != other.height
             || this.width != other.width)
       return false;
 
     for (int i = 0; i < this.data.length; ++i)
       if (this.data[i] != other.data[i])
         return false;
 
     return true;
   }
 
   @Override
   public int hashCode() {
     int hash = 5;
     hash = 37 * hash + Arrays.hashCode(this.data);
     hash = 37 * hash + this.maxval;
     hash = 37 * hash + this.width;
     hash = 37 * hash + this.height;
     return hash;
   }
 
   private int index(int pixel_x, int pixel_y) {
     return (width * pixel_y + pixel_x) * 3;
   }
 
   public Image(int width, int height, int maxval) {
     this.width = width; this.height = height; this.maxval = maxval;
     data = new byte[height*width*3];
   }
 
   @Override
   public String toString() {
     String s = "";
     s += "Width: " + width + "\n";
     s += "Height: " + height + "\n";
     s += "Maxval: " + maxval + "\n";
     return s;
   }
 
   public void printImage() {
     for (int r = 0; r < this.height; ++r) {
       for (int c = 0; c < this.width; ++c) {
         int i = this.index(c, r);
         System.out.print((128+this.data[i]) + " ");
         System.out.print((128+this.data[i+1]) + " ");
         System.out.print((128+this.data[i+2]) + " ");
       }
       System.out.println();
     }
 
   }
 
   public void Save(String filename) throws Exception {
     File f = new File(filename);
     FileWriter fw = new FileWriter(f);
     BufferedWriter bw = new BufferedWriter(fw);
 
     bw.append("P3\n");
     bw.append(width + " " +height +"\n");
     bw.append(maxval+"\n");
     
     for (int y = 0; y < height; y++) {
       for (int x = 0; x < width*3; x++)
         bw.write((data[y*width*3+x]+128)+" ");
       bw.write("\n");
     }
     bw.close();
     fw.close();
   }
 
   public static Image Smoothen(Image helper, Image im, int row_start, int row_end) {
     int r_sum = 0, b_sum = 0, g_sum = 0, index;
     final int col = 3;
     final int row = im.width*3;
     // <editor-fold defaultstate="collapsed" desc="Special case: first row of image">
     if (row_start == 0) {
       row_start++;
       // <editor-fold defaultstate="collapsed" desc="column 0">
       // ___
       // __X
       // ___
       r_sum += im.data[im.index(1,0)];
       g_sum += im.data[im.index(1,0)+1];
       b_sum += im.data[im.index(1,0)+2];
       // ___
       // ___
       // __X
       r_sum += im.data[im.index(1,1)];
       g_sum += im.data[im.index(1,1)+1];
       b_sum += im.data[im.index(1,1)+2];
       // ___
       // ___
       // _X_
       r_sum += im.data[im.index(0,1)];
       g_sum += im.data[im.index(0,1)+1];
       b_sum += im.data[im.index(0,1)+2];
 
       index = helper.index(0, 0);
       helper.data[index] = (byte)((im.data[index] + r_sum/3) / 2);
       helper.data[index+1] = (byte)((im.data[index+1] + g_sum/3) / 2);
       helper.data[index+2] = (byte)((im.data[index+2] + b_sum/3) / 2);
    
       // </editor-fold>
 
      // <editor-fold defaultstate="collapsed" desc="columns 1 .. width-2">
       for (int x = 1; x < im.width-1; x++) {
         r_sum = 0; b_sum = 0; g_sum = 0;
         // ___
         // __X
         // ___
         r_sum += im.data[im.index(x+1,0)];
         g_sum += im.data[im.index(x+1,0)+1];
         b_sum += im.data[im.index(x+1,0)+2];
         // ___
         // X__
         // ___
         r_sum += im.data[im.index(x-1,0)];
         g_sum += im.data[im.index(x-1,0)+1];
         b_sum += im.data[im.index(x-1,0)+2];
         // ___
         // ___
         // __X
         r_sum += im.data[im.index(x+1,1)];
         g_sum += im.data[im.index(x+1,1)+1];
         b_sum += im.data[im.index(x+1,1)+2];
         // ___
         // ___
         // X__
         r_sum += im.data[im.index(x-1,1)];
         g_sum += im.data[im.index(x-1,1)+1];
         b_sum += im.data[im.index(x-1,1)+2];
         // ___
         // ___
         // _X_
         r_sum += im.data[im.index(x,1)];
         g_sum += im.data[im.index(x,1)+1];
         b_sum += im.data[im.index(x,1)+2];
 
         index = helper.index(x, 0);
         helper.data[index] = (byte)((im.data[index] + r_sum/5) / 2);
         helper.data[index+1] = (byte)((im.data[index+1] + g_sum/5) / 2);
         helper.data[index+2] = (byte)((im.data[index+2] + b_sum/5) / 2);
       }
       // </editor-fold>
 
      // <editor-fold defaultstate="collapsed" desc="column width-1">
       r_sum = 0; b_sum = 0; g_sum = 0;
       // ___
       // X__
       // ___
       r_sum += im.data[im.index(im.width-2,0)];
       g_sum += im.data[im.index(im.width-2,0)+1];
       b_sum += im.data[im.index(im.width-2,0)+2];
       // ___
       // ___
       // X__
       r_sum += im.data[im.index(im.width-2,1)];
       g_sum += im.data[im.index(im.width-2,1)+1];
       b_sum += im.data[im.index(im.width-2,1)+2];
       // ___
       // ___
       // _X_
       r_sum += im.data[im.index(im.width-1,1)];
       g_sum += im.data[im.index(im.width-1,1)+1];
       b_sum += im.data[im.index(im.width-1,1)+2];
 
       index = helper.index(im.width-1, 0);
       helper.data[index] = (byte)((im.data[index] + r_sum/3) / 2);
       helper.data[index+1] = (byte)((im.data[index+1] + g_sum/3) / 2);
       helper.data[index+2] = (byte)((im.data[index+2] + b_sum/3) / 2);
     }
     // </editor-fold>
     // </editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="Special case: last row of image">
     if (row_end == im.height) {
       row_end--;
       r_sum = 0; b_sum = 0; g_sum = 0;
       // <editor-fold defaultstate="collapsed" desc="column 0">
       // ___
       // __X
       // ___
       r_sum += im.data[im.index(1,im.height-1)];
       g_sum += im.data[im.index(1,im.height-1)+1];
       b_sum += im.data[im.index(1,im.height-1)+2];
       // __X
       // ___
       // ___
       r_sum += im.data[im.index(1,im.height-2)];
       g_sum += im.data[im.index(1,im.height-2)+1];
       b_sum += im.data[im.index(1,im.height-2)+2];
       // _X_
       // ___
       // ___
       r_sum += im.data[im.index(0,im.height-2)];
       g_sum += im.data[im.index(0,im.height-2)+1];
       b_sum += im.data[im.index(0,im.height-2)+2];
 
       index = helper.index(0,im.height-1);
       helper.data[index] = (byte)((im.data[index] + r_sum/3) / 2);
       helper.data[index+1] = (byte)((im.data[index+1] + g_sum/3) / 2);
       helper.data[index+2] = (byte)((im.data[index+2] + b_sum/3) / 2);
 
      // </editor-fold>
 
       // <editor-fold defaultstate="collapsed" desc="column 1 .. height-2">
       for (int x = 1; x < im.width-1; x++) {
         r_sum = 0; b_sum = 0; g_sum = 0;
         // ___
         // __X
         // ___
         r_sum += im.data[im.index(x+1,im.height-1)];
         g_sum += im.data[im.index(x+1,im.height-1)+1];
         b_sum += im.data[im.index(x+1,im.height-1)+2];
         // ___
         // X__
         // ___
         r_sum += im.data[im.index(x-1,im.height-1)];
         g_sum += im.data[im.index(x-1,im.height-1)+1];
         b_sum += im.data[im.index(x-1,im.height-1)+2];
         // __X
         // ___
         // ___
         r_sum += im.data[im.index(x+1,im.height-2)];
         g_sum += im.data[im.index(x+1,im.height-2)+1];
         b_sum += im.data[im.index(x+1,im.height-2)+2];
         // X__
         // ___
         // ___
         r_sum += im.data[im.index(x-1,im.height-2)];
         g_sum += im.data[im.index(x-1,im.height-2)+1];
         b_sum += im.data[im.index(x-1,im.height-2)+2];
         // _X_
         // ___
         // ___
         r_sum += im.data[im.index(x,im.height-2)];
         g_sum += im.data[im.index(x,im.height-2)+1];
         b_sum += im.data[im.index(x,im.height-2)+2];
 
         index = helper.index(x, im.height-1);
         helper.data[index] = (byte)((im.data[index] + r_sum/5) / 2);
         helper.data[index+1] = (byte)((im.data[index+1] + g_sum/5) / 2);
         helper.data[index+2] = (byte)((im.data[index+2] + b_sum/5) / 2);
       }
 
       // </editor-fold>
 
       // <editor-fold defaultstate="collapsed" desc="column height-1">
       r_sum = 0; b_sum = 0; g_sum = 0;
       // ___
       // X__
       // ___
       r_sum += im.data[im.index(im.width-2,im.height-1)];
       g_sum += im.data[im.index(im.width-2,im.height-1)+1];
       b_sum += im.data[im.index(im.width-2,im.height-1)+2];
       // X__
       // ___
       // ___
       r_sum += im.data[im.index(im.width-2,im.height-2)];
       g_sum += im.data[im.index(im.width-2,im.height-2)+1];
       b_sum += im.data[im.index(im.width-2,im.height-2)+2];
       // _X_
       // ___
       // ___
       r_sum += im.data[im.index(im.width-1,im.height-2)];
       g_sum += im.data[im.index(im.width-1,im.height-2)+1];
       b_sum += im.data[im.index(im.width-1,im.height-2)+2];
 
      index = helper.index(im.width-1, im.height-1);
       helper.data[index] = (byte)((im.data[index] + r_sum/3) / 2);
       helper.data[index+1] = (byte)((im.data[index+1] + g_sum/3) / 2);
       helper.data[index+2] = (byte)((im.data[index+2] + b_sum/3) / 2);
     }
 
     // </editor-fold>
     // </editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="Normal case: rows 1 .. height-2">
     for (int y = row_start; y < row_end; y++) {
       // <editor-fold defaultstate="collapsed" desc="column 0">
       r_sum = 0; b_sum = 0; g_sum = 0;
       // _X_
       // ___
       // ___
       r_sum += im.data[im.index(0,y-1)];
       g_sum += im.data[im.index(0,y-1)+1];
       b_sum += im.data[im.index(0,y-1)+2];
       // __X
       // ___
       // ___
       r_sum += im.data[im.index(1,y-1)];
       g_sum += im.data[im.index(1,y-1)+1];
       b_sum += im.data[im.index(1,y-1)+2];
       // ___
       // __X
       // ___
       r_sum += im.data[im.index(1,y)];
       g_sum += im.data[im.index(1,y)+1];
       b_sum += im.data[im.index(1,y)+2];
       // ___
       // ___
       // __X
       r_sum += im.data[im.index(1,y+1)];
       g_sum += im.data[im.index(1,y+1)+1];
       b_sum += im.data[im.index(1,y+1)+2];
       // ___
       // ___
       // _X_
       r_sum += im.data[im.index(0,y+1)];
       g_sum += im.data[im.index(0,y+1)+1];
       b_sum += im.data[im.index(0,y+1)+2];
 
       index = helper.index(0, y);
       helper.data[index] = (byte)((im.data[index] + r_sum/5) / 2);
       helper.data[index+1] = (byte)((im.data[index+1] + g_sum/5) / 2);
       helper.data[index+2] = (byte)((im.data[index+2] + b_sum/5) / 2);
       // </editor-fold>
       // <editor-fold defaultstate="collapsed" desc="columns 1 .. height-2">
       for (int x = 1; x < im.width-1; x++) {
         index = helper.index(x, y);
           
         r_sum = 0; g_sum = 0; b_sum = 0;
         // ___
         // __X
         // ___
         r_sum += im.data[index+col];
         g_sum += im.data[index+col+1];
         b_sum += im.data[index+col+2];
         // ___
         // X__
         // ___
         r_sum += im.data[index-col];
         g_sum += im.data[index-col+1];
         b_sum += im.data[index-col+2];
         // __X
         // ___
         // ___
         r_sum += im.data[index+col-row];
         g_sum += im.data[index+col-row+1];
         b_sum += im.data[index+col-row+2];
         // X__
         // ___
         // ___
         r_sum += im.data[index-col-row];
         g_sum += im.data[index-col-row+1];
         b_sum += im.data[index-col-row+2];
         // _X_
         // ___
         // ___
         r_sum += im.data[index-row];
         g_sum += im.data[index-row+1];
         b_sum += im.data[index-row+2];
         // ___
         // ___
         // __X
         r_sum += im.data[index+col+row];
         g_sum += im.data[index+col+row+1];
         b_sum += im.data[index+col+row+2];
         // ___
         // ___
         // X__
         r_sum += im.data[index-col+row];
         g_sum += im.data[index-col+row+1];
         b_sum += im.data[index-col+row+2];
         // ___
         // ___
         // _X_
         r_sum += im.data[index+row];
         g_sum += im.data[index+row+1];
         b_sum += im.data[index+row+2];
         
         
         helper.data[index] = (byte)((im.data[index] + r_sum/8) / 2);
         helper.data[index+1] = (byte)((im.data[index+1] + g_sum/8) / 2);
         helper.data[index+2] = (byte)((im.data[index+2] + b_sum/8) / 2);
       }
       // </editor-fold>
       // <editor-fold defaultstate="collapsed" desc="column height-1">
       r_sum = 0; b_sum = 0; g_sum = 0;
       // _X_
       // ___
       // ___
       r_sum += im.data[im.index(im.width-1,y-1)];
       g_sum += im.data[im.index(im.width-1,y-1)+1];
       b_sum += im.data[im.index(im.width-1,y-1)+2];
       // X__
       // ___
       // ___
       r_sum += im.data[im.index(im.width-2,y-1)];
       g_sum += im.data[im.index(im.width-2,y-1)+1];
       b_sum += im.data[im.index(im.width-2,y-1)+2];
       // ___
       // X__
       // ___
       r_sum += im.data[im.index(im.width-2,y)];
       g_sum += im.data[im.index(im.width-2,y)+1];
       b_sum += im.data[im.index(im.width-2,y)+2];
       // ___
       // ___
       // X__
       r_sum += im.data[im.index(im.width-2,y+1)];
       g_sum += im.data[im.index(im.width-2,y+1)+1];
       b_sum += im.data[im.index(im.width-2,y+1)+2];
       // ___
       // ___
       // _X_
       r_sum += im.data[im.index(im.width-1,y+1)];
       g_sum += im.data[im.index(im.width-1,y+1)+1];
       b_sum += im.data[im.index(im.width-1,y+1)+2];
 
       index = helper.index(im.width-1, y);
       helper.data[index] = (byte)((im.data[index] + r_sum/5) / 2);
       helper.data[index+1] = (byte)((im.data[index+1] + g_sum/5) / 2);
       helper.data[index+2] = (byte)((im.data[index+2] + b_sum/5) / 2);
     }
     // </editor-fold>
     // </editor-fold>
 
     return helper;
   }
 
   public static Image Sharpen(Image helper, Image im, int row_start, int row_end) {
     int r_sum = 0, b_sum = 0, g_sum = 0, index;
     final int col = 3;
     final int row = im.width*3;
 
     // <editor-fold defaultstate="collapsed" desc="Special case: first row of image">
     if (row_start == 0) {
       row_start++;
       // <editor-fold defaultstate="collapsed" desc="column 0">
       // ___
       // __X
       // ___
       r_sum += im.data[im.index(1,0)];
       g_sum += im.data[im.index(1,0)+1];
       b_sum += im.data[im.index(1,0)+2];
       // ___
       // ___
       // __X
       r_sum += im.data[im.index(1,1)];
       g_sum += im.data[im.index(1,1)+1];
       b_sum += im.data[im.index(1,1)+2];
       // ___
       // ___
       // _X_
       r_sum += im.data[im.index(0,1)];
       g_sum += im.data[im.index(0,1)+1];
       b_sum += im.data[im.index(0,1)+2];
 
       index = helper.index(0, 0);
       helper.data[index] = byteClamp(2*(int)im.data[index] - r_sum/3);
       helper.data[index+1] = byteClamp(2*(int)im.data[index+1] - g_sum/3);
       helper.data[index+2] = byteClamp(2*(int)im.data[index+2] - b_sum/3);
       // </editor-fold>
       // <editor-fold defaultstate="collapsed" desc="columns 1 .. width-2">
       for (int x = 1; x < im.width-1; x++) {
         r_sum = 0; b_sum = 0; g_sum = 0;
         // ___
         // __X
         // ___
         r_sum += im.data[im.index(x+1,0)];
         g_sum += im.data[im.index(x+1,0)+1];
         b_sum += im.data[im.index(x+1,0)+2];
         // ___
         // X__
         // ___
         r_sum += im.data[im.index(x-1,0)];
         g_sum += im.data[im.index(x-1,0)+1];
         b_sum += im.data[im.index(x-1,0)+2];
         // ___
         // ___
         // __X
         r_sum += im.data[im.index(x+1,1)];
         g_sum += im.data[im.index(x+1,1)+1];
         b_sum += im.data[im.index(x+1,1)+2];
         // ___
         // ___
         // X__
         r_sum += im.data[im.index(x-1,1)];
         g_sum += im.data[im.index(x-1,1)+1];
         b_sum += im.data[im.index(x-1,1)+2];
         // ___
         // ___
         // _X_
         r_sum += im.data[im.index(x,1)];
         g_sum += im.data[im.index(x,1)+1];
         b_sum += im.data[im.index(x,1)+2];
         
         index = helper.index(x, 0);
         helper.data[index] = byteClamp(2*(int)im.data[index] - r_sum/5);
         helper.data[index+1] = byteClamp(2*(int)im.data[index+1] - g_sum/5);
         helper.data[index+2] = byteClamp(2*(int)im.data[index+2] - b_sum/5);
       }
       // </editor-fold>
       // <editor-fold defaultstate="collapsed" desc="column width-1">
       r_sum = 0; b_sum = 0; g_sum = 0;
       // ___
       // X__
       // ___
       r_sum += im.data[im.index(im.width-2,0)];
       g_sum += im.data[im.index(im.width-2,0)+1];
       b_sum += im.data[im.index(im.width-2,0)+2];
       // ___
       // ___
       // X__
       r_sum += im.data[im.index(im.width-2,1)];
       g_sum += im.data[im.index(im.width-2,1)+1];
       b_sum += im.data[im.index(im.width-2,1)+2];
       // ___
       // ___
       // _X_
       r_sum += im.data[im.index(im.width-1,1)];
       g_sum += im.data[im.index(im.width-1,1)+1];
       b_sum += im.data[im.index(im.width-1,1)+2];
 
       index = helper.index(im.width-1, 0);
       helper.data[index] = byteClamp(2*(int)im.data[index] - r_sum/3);
       helper.data[index+1] = byteClamp(2*(int)im.data[index+1] - g_sum/3);
       helper.data[index+2] = byteClamp(2*(int)im.data[index+2] - b_sum/3);
     }
     // </editor-fold>
     // </editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="Special case: last row of image">
     if (row_end == im.height) {
       row_end--;
       r_sum = 0; b_sum = 0; g_sum = 0;
       // <editor-fold defaultstate="collapsed" desc="column 0">
       // ___
       // __X
       // ___
       r_sum += im.data[im.index(1,im.height-1)];
       g_sum += im.data[im.index(1,im.height-1)+1];
       b_sum += im.data[im.index(1,im.height-1)+2];
       // __X
       // ___
       // ___
       r_sum += im.data[im.index(1,im.height-2)];
       g_sum += im.data[im.index(1,im.height-2)+1];
       b_sum += im.data[im.index(1,im.height-2)+2];
       // _X_
       // ___
       // ___
       r_sum += im.data[im.index(0,im.height-2)];
       g_sum += im.data[im.index(0,im.height-2)+1];
       b_sum += im.data[im.index(0,im.height-2)+2];
 
       index = helper.index(0,im.height-1);
       helper.data[index] = byteClamp(2*(int)im.data[index] - r_sum/3);
       helper.data[index+1] = byteClamp(2*(int)im.data[index+1] - g_sum/3);
       helper.data[index+2] = byteClamp(2*(int)im.data[index+2] - b_sum/3);
       // </editor-fold>
       // <editor-fold defaultstate="collapsed" desc="columns 1 .. width-2">
       for (int x = 1; x < im.width-1; x++) {
         r_sum = 0; b_sum = 0; g_sum = 0;
         // ___
         // __X
         // ___
         r_sum += im.data[im.index(x+1,im.height-1)];
         g_sum += im.data[im.index(x+1,im.height-1)+1];
         b_sum += im.data[im.index(x+1,im.height-1)+2];
         // ___
         // X__
         // ___
         r_sum += im.data[im.index(x-1,im.height-1)];
         g_sum += im.data[im.index(x-1,im.height-1)+1];
         b_sum += im.data[im.index(x-1,im.height-1)+2];
         // __X
         // ___
         // ___
         r_sum += im.data[im.index(x+1,im.height-2)];
         g_sum += im.data[im.index(x+1,im.height-2)+1];
         b_sum += im.data[im.index(x+1,im.height-2)+2];
         // X__
         // ___
         // ___
         r_sum += im.data[im.index(x-1,im.height-2)];
         g_sum += im.data[im.index(x-1,im.height-2)+1];
         b_sum += im.data[im.index(x-1,im.height-2)+2];
         // _X_
         // ___
         // ___
         r_sum += im.data[im.index(x,im.height-2)];
         g_sum += im.data[im.index(x,im.height-2)+1];
         b_sum += im.data[im.index(x,im.height-2)+2];
 
         index = helper.index(x, im.height-1);
         helper.data[index] = byteClamp(2*(int)im.data[index] - r_sum/5);
         helper.data[index+1] = byteClamp(2*(int)im.data[index+1] - g_sum/5);
         helper.data[index+2] = byteClamp(2*(int)im.data[index+2] - b_sum/5);
       }
       // </editor-fold>
       // <editor-fold defaultstate="collapsed" desc="column width-1">
       r_sum = 0; b_sum = 0; g_sum = 0;
       // ___
       // X__
       // ___
       r_sum += im.data[im.index(im.width-2,im.height-1)];
       g_sum += im.data[im.index(im.width-2,im.height-1)+1];
       b_sum += im.data[im.index(im.width-2,im.height-1)+2];
       // X__
       // ___
       // ___
       r_sum += im.data[im.index(im.width-2,im.height-2)];
       g_sum += im.data[im.index(im.width-2,im.height-2)+1];
       b_sum += im.data[im.index(im.width-2,im.height-2)+2];
       // _X_
       // ___
       // ___
       r_sum += im.data[im.index(im.width-1,im.height-2)];
       g_sum += im.data[im.index(im.width-1,im.height-2)+1];
       b_sum += im.data[im.index(im.width-1,im.height-2)+2];
 
       index = helper.index(im.width-1, im.height-1);
       helper.data[index] = byteClamp(2*(int)im.data[index] - r_sum/3);
       helper.data[index+1] = byteClamp(2*(int)im.data[index+1] - g_sum/3);
       helper.data[index+2] = byteClamp(2*(int)im.data[index+2] - b_sum/3);
     }
     // </editor-fold>
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="Normal case: rows 1 .. height-2">
     for (int y = row_start; y < row_end; y++) {
       // <editor-fold defaultstate="collapsed" desc="column 0">
       r_sum = 0; b_sum = 0; g_sum = 0;
       // _X_
       // ___
       // ___
       r_sum += im.data[im.index(0,y-1)];
       g_sum += im.data[im.index(0,y-1)+1];
       b_sum += im.data[im.index(0,y-1)+2];
       // __X
       // ___
       // ___
       r_sum += im.data[im.index(1,y-1)];
       g_sum += im.data[im.index(1,y-1)+1];
       b_sum += im.data[im.index(1,y-1)+2];
       // ___
       // __X
       // ___
       r_sum += im.data[im.index(1,y)];
       g_sum += im.data[im.index(1,y)+1];
       b_sum += im.data[im.index(1,y)+2];
       // ___
       // ___
       // __X
       r_sum += im.data[im.index(1,y+1)];
       g_sum += im.data[im.index(1,y+1)+1];
       b_sum += im.data[im.index(1,y+1)+2];
       // ___
       // ___
       // _X_
       r_sum += im.data[im.index(0,y+1)];
       g_sum += im.data[im.index(0,y+1)+1];
       b_sum += im.data[im.index(0,y+1)+2];
 
       index = helper.index(0, y);
       helper.data[index] = byteClamp(2*(int)im.data[index] - r_sum/5);
       helper.data[index+1] = byteClamp(2*(int)im.data[index+1] - g_sum/5);
       helper.data[index+2] = byteClamp(2*(int)im.data[index+2] - b_sum/5);
       // </editor-fold>
       
       // <editor-fold defaultstate="collapsed" desc="colums 1 .. width-2">
       for (int x = 1; x < im.width-1; x++) {
         r_sum = 0; g_sum = 0; b_sum = 0;
         index = helper.index(x, y);
         
         // ___
         // __X
         // ___
         r_sum += im.data[index+col];
         g_sum += im.data[index+col+1];
         b_sum += im.data[index+col+2];
         // ___
         // X__
         // ___
         r_sum += im.data[index-col];
         g_sum += im.data[index-col+1];
         b_sum += im.data[index-col+2];
         // __X
         // ___
         // ___
         r_sum += im.data[index+col-row];
         g_sum += im.data[index+col-row+1];
         b_sum += im.data[index+col-row+2];
         // X__
         // ___
         // ___
         r_sum += im.data[index-col-row];
         g_sum += im.data[index-col-row+1];
         b_sum += im.data[index-col-row+2];
         // _X_
         // ___
         // ___
         r_sum += im.data[index-row];
         g_sum += im.data[index-row+1];
         b_sum += im.data[index-row+2];
         // ___
         // ___
         // __X
         r_sum += im.data[index+col+row];
         g_sum += im.data[index+col+row+1];
         b_sum += im.data[index+col+row+2];
         // ___
         // ___
         // X__
         r_sum += im.data[index-col+row];
         g_sum += im.data[index-col+row+1];
         b_sum += im.data[index-col+row+2];
         // ___
         // ___
         // _X_
         r_sum += im.data[index+row];
         g_sum += im.data[index+row+1];
         b_sum += im.data[index+row+2];
 
         helper.data[index] = byteClamp(2*(int)im.data[index] - r_sum/8);
         helper.data[index+1] = byteClamp(2*(int)im.data[index+1] - g_sum/8);
         helper.data[index+2] = byteClamp(2*(int)im.data[index+2] - b_sum/8);
       }
       // </editor-fold>
       
       // <editor-fold defaultstate="collapsed" desc="column width-1">
       r_sum = 0; b_sum = 0; g_sum = 0;
       // _X_
       // ___
       // ___
       r_sum += im.data[im.index(im.width-1,y-1)];
       g_sum += im.data[im.index(im.width-1,y-1)+1];
       b_sum += im.data[im.index(im.width-1,y-1)+2];
       // X__
       // ___
       // ___
       r_sum += im.data[im.index(im.width-2,y-1)];
       g_sum += im.data[im.index(im.width-2,y-1)+1];
       b_sum += im.data[im.index(im.width-2,y-1)+2];
       // ___
       // X__
       // ___
       r_sum += im.data[im.index(im.width-2,y)];
       g_sum += im.data[im.index(im.width-2,y)+1];
       b_sum += im.data[im.index(im.width-2,y)+2];
       // ___
       // ___
       // X__
       r_sum += im.data[im.index(im.width-2,y+1)];
       g_sum += im.data[im.index(im.width-2,y+1)+1];
       b_sum += im.data[im.index(im.width-2,y+1)+2];
       // ___
       // ___
       // _X_
       r_sum += im.data[im.index(im.width-1,y+1)];
       g_sum += im.data[im.index(im.width-1,y+1)+1];
       b_sum += im.data[im.index(im.width-1,y+1)+2];
 
       index = helper.index(im.width-1, y);
       helper.data[index] = byteClamp(2*(int)im.data[index] - r_sum/5);
       helper.data[index+1] = byteClamp(2*(int)im.data[index+1] - g_sum/5);
       helper.data[index+2] = byteClamp(2*(int)im.data[index+2] - b_sum/5);
       // </editor-fold>
     }   
     // </editor-fold>
     
     return helper;
   }
 
   static byte byteClamp(int i) {
     return (i < -128 ? -128 : (i > 127 ? 127 : (byte)i));
   }
 }
