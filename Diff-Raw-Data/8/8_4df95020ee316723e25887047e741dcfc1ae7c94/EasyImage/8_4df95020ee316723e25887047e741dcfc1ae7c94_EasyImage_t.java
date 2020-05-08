 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.maxel.ati.tp;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.util.Collections;
 
 /**
  *
  * @author maximo
  */
 public class EasyImage {
 
     BufferedImage img;
     int[] channelR;
     int[] channelG;
     int[] channelB;
     int[] channelA;
     int[] fullImg;
 
     public EasyImage(BufferedImage img) {
         this.img = img;
 
         fullImg = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
         channelR = new int[fullImg.length];
         channelG = new int[fullImg.length];
         channelB = new int[fullImg.length];
         channelA = new int[fullImg.length];
         updateChannels();
     }
 
     public BufferedImage getImage() {
         updateImg();
         return img;
     }
 
     public void updateImg() {
         img.setRGB(0, 0, img.getWidth(), img.getHeight(), fullImg, 0, img.getWidth());
     }
 
     private void updateChannels() {
         for (int i = 0; i < fullImg.length; i++) {
             int rgb = fullImg[i];
             channelA[i] = (rgb >> 24) & 0x000000FF;
             channelR[i] = (rgb >> 16) & 0x000000FF;
             channelG[i] = (rgb >> 8) & 0x000000FF;
             channelB[i] = (rgb) & 0x000000FF;
         }
     }
 
     private void updateFullImg() {
         for (int i = 0; i < fullImg.length; i++) {
             fullImg[i] = channelA[i] << 24;
             fullImg[i] += channelR[i] << 16;
             fullImg[i] += channelG[i] << 8;
             fullImg[i] += channelB[i];
         }
     }
 
     public void applyNegative() {
         for (int i = 0; i < img.getWidth() * img.getHeight(); i++) {
             fullImg[i] = 0x0fff - fullImg[i];
         }
         //                    for (int x = 0; x < img.getWidth(); x++) {
 //                        for (int y = 0; y < img.getHeight(); y++) {
 //                            int aux = img.getRGB(x, y);
 //                            aux = 0x0fff - aux;
 //                            img.setRGB(x, y, aux);
 //                        }
 //                    }
     }
 
     private void normalizeChannel(int[] channel) {
         Integer max = channel[0];
         Integer min = channel[0];
         for (int x : channelA) {
             max = x > max ? x : max;
             min = x < min ? x : min;
         }
         for (int x : channelR) {
             max = x > max ? x : max;
             min = x < min ? x : min;
         }
         for (int x : channelG) {
             max = x > max ? x : max;
             min = x < min ? x : min;
         }
         for (int x : channelB) {
             max = x > max ? x : max;
             min = x < min ? x : min;
         }
         max = max - min;
         for (int i = 0; i < channel.length; i++) {
            channel[i] = ((channel[i] - min) * 255) / max;
         }
     }
 
     public void normalize() {
         normalizeChannel(channelA);
         normalizeChannel(channelR);
         normalizeChannel(channelG);
         normalizeChannel(channelB);
         updateFullImg();
     }
     
     public void substract(EasyImage img2){
         for(int i=0;i<fullImg.length;i++){
             fullImg[i]-=img2.fullImg[i];
         }
         normalize();
     }
     public void add(EasyImage img2){
         for(int i=0;i<fullImg.length;i++){
             fullImg[i]+=img2.fullImg[i];
         }
         updateChannels();
         normalize();
     }
 }
