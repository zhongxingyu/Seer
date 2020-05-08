 package com.vnexit.fingerprint.datamodel;
 
 import java.awt.image.BufferedImage;
 import java.awt.image.Raster;
 import java.awt.image.WritableRaster;
 
 public class ImageProcess {
 	private  BufferedImage mBimg;
 	
 	public ImageProcess(BufferedImage bmg){
 		this.mBimg=bmg;
 	}
 	
     public Pixel[][] getData() {
         Raster r = mBimg.getRaster();
         Pixel[][] data = new Pixel[r.getWidth()][r.getHeight()];
         int[] v = new int[3];
         v[0] = v[1] = v[2] = 0;
 
         for (int i = 0; i < r.getWidth(); i++) {
             for (int j = 0; j < r.getHeight(); j++) {
                 v = r.getPixel(i, j, v);
                 Pixel newPixel = new Pixel(v[0], v[1], v[2]);
                 data[i][j] = newPixel;
             }
         }
         return data;
     }
     
     public void setData(WritableRaster wr, Pixel[][] data) {
         int[] p = new int[3];
         for (int i = 0; i < wr.getWidth(); i++) {
             for (int j = 0; j < wr.getHeight(); j++) {
                 p[0] = data[i][j].mRed;
                 p[1] = data[i][j].mGreen;
                 p[2] = data[i][j].mBlue;
                 wr.setPixel(i, j, p);
             }
         }
     }
     
    public Pixel[][] grayExchange() {
    	Pixel[][] pi= getData();
         int s = 0;
         for (int i = 0; i < mBimg.getWidth(); i++) {
             for (int j = 0; j < mBimg.getHeight(); j++) {
                 s = Math.max(pi[i][j].mRed, Math.max(pi[i][j].mGreen, pi[i][j].mBlue));
                 Pixel p = new Pixel(s, s, s);
                 pi[i][j] = p;
             }
         }
         return pi;
     }

 }
