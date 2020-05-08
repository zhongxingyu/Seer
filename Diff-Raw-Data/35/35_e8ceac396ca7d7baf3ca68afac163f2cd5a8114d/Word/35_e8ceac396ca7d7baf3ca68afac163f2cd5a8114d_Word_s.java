 package com.hersan.bablrr;
 
 import java.lang.Math;
 import processing.core.PApplet;
 import processing.core.PFont;
 import processing.core.PGraphics;
 import processing.core.PImage;
 
 
 /* from Processing :
  * 		PImage : uImage, dImage
  * 			createImage();
  *		PImage : tp0, tp1
  *			createImage()
  *			filter()
  *		PImage : src, dst
  *			width + height + pixels
  *			loadPixels()
  *			resize()
  *			copy()
  *		PGraphics : pg
  *			beginDraw() + text()
  *			get()
  *		PFont : theFont
  *
  */
 
 // Some tips taken from here : http://developer.android.com/guide/practices/design/performance.html
 
 public class Word {
 
 	final private static String WHICHFONT = "courier.ttf"; //"Monospaced"; //"Courier";
 	final private static int FONTSIZE = 48;
 
 	private String theWord;
 	protected PApplet myProcessing;
 	protected PImage uImage, dImage;
 
 	////////////////////
 	// constructors
 	///////////////////
 	public Word(String w, PApplet pa) {
 		theWord = w;
 		myProcessing = pa;
 		this.createUImage();
 		this.createDImage();
 	}
 	public Word(){
 		this(WHICHFONT,null);
 	}
 
 	public PImage getUImage() {
 		return uImage;
 	}
 
 	public PImage getDImage() {
 		return dImage;
 	}
 
 	//////////////////
 	// static helper functions from PApplet
 	/////////////////////
 	private static int red(int c){
 		return (c >> 16)&0xff;
 	}
 	private static int green(int c){
 		return (c >> 8)&0xff;
 	}
 	private static int blue(int c){
 		return (c >> 0)&0xff;
 	}
 	private static float random(float howsmall, float howbig) {
 		float diff = Math.abs(howbig-howsmall);
 		return (float)Math.random()*diff + howsmall;
 	}
 	private static float constrain(float amt, float low, float high) {
 		return (amt < low) ? low : ((amt > high) ? high : amt);
 	}
 
 
 	//////////////////////
 	// init/create functions
 	////////////////////////
 
 	// create the undistorted image.
 	//   get word, write it to an graphic, measure word, add line, crop.
 	private void createUImage() {
		PGraphics pg = myProcessing.createGraphics(theWord.length()*FONTSIZE, 90, PApplet.JAVA2D);
 		PFont theFont = myProcessing.createFont(WHICHFONT, FONTSIZE);
 
 		// write word on graphic
 		pg.beginDraw();
 		pg.background(0xffffffff);
 		pg.smooth();
 		pg.fill(0x00000000);
 		pg.textFont(theFont);
 		pg.text(theWord, 0, 0, pg.width, pg.height);
 		pg.endDraw();
 
 		// get image from graphic
 		// temporary sizing
 		uImage = myProcessing.createImage(1, 1, PApplet.ARGB);
 		// crop+copy
 		Word.cropImage(pg.get(), uImage);
 
 		// clear old drawing,
 		// draw some random lines through the cropped image
 		pg.beginDraw();
 		pg.background(0xffffffff);
 		pg.image(uImage,0,0);
 		pg.stroke(0x00000000);
 		pg.strokeWeight(3);
 		pg.noFill();
 		pg.line(Word.random(5f, uImage.width/5f), Word.random(5f, uImage.height-5f), Word.random(4f*uImage.width/5f, uImage.width-5f), Word.random(5f, uImage.height-5f));
 		pg.endDraw();
 
 		// copy final version with line
 		Word.cropImage(pg.get(), uImage);
 
 		// clean up
 		pg.dispose();
 		pg.delete();
 	}
 
 	// given src and dst images,
 	//    find the boundaries on src,
 	//    resize dst,
 	//    copy the right pixels from src to dst
 	protected static void cropImage(PImage src, PImage dst) {
 
 		// find boundaries
 		int minX = 1024;
 		int maxX = 0;
 		int minY = 1024;
 		int maxY = 0;
 		
 		// From : http://developer.android.com/guide/practices/design/performance.html
 		//        i < mArray.length is slowest, because the JIT can't yet optimize away 
 		//        the cost of getting the array length once for every iteration through the loop.
 		int srcW = src.width;
 		int srcH = src.height;
 
 		src.loadPixels();
 		// get pixel boundaries
 		for (int i=0; i<srcH; i++) {
 			for (int j=0; j<srcW; j++) {
 				int c = src.pixels[i*srcW+j];
 				// find non-white pixels
 				if ((Word.red(c) < 250) && (Word.green(c) < 250) && (Word.blue(c) < 250)) {
 					if (i<minY) minY = i;
 					if (i>maxY) maxY = i;
 					if (j<minX) minX = j;
 					if (j>maxX) maxX = j;
 				}
 			}
 		}
 
 		// copy it onto dst
 		dst.resize(maxX-minX+10, maxY-minY+10);
 		dst.copy(src, minX-5, minY-5, maxX-minX+10, maxY-minY+10, 0, 0, dst.width, dst.height);
 	}
 
 	// create the distorted image
 	protected void createDImage() {
 		// temp image to help with the distortions
 		//  both tpi and dImage are sized according to uImage
 		PImage tp0 = myProcessing.createImage(uImage.width, uImage.height, PApplet.ARGB);
 		PImage tp1 = myProcessing.createImage(uImage.width, uImage.height, PApplet.ARGB);
 		// temporary size
 		dImage = myProcessing.createImage(uImage.width, uImage.height, PApplet.ARGB);
 
 		//tp0.copy(uImage, 0, 0, uImage.width, uImage.height, 0, 0, uImage.width, uImage.height);
 
 		// barrel eye
 		//this.fisheye(uImage, tp, uImage.width/3, uImage.height/2, 0.001);
 
 		// horizontal shear
 		Word.horizontalShear(uImage, tp0);
 
 		// keystone
 		Word.keyTransform(tp0, tp1);
 
 		// wave shear
 		Word.waveShear(tp1, tp0);
 
 		// final blur
 		tp0.filter(PApplet.BLUR, 1.76f);
 
 		// crop and copy
 		Word.cropImage(tp0, dImage);
 
 		// clean up
 		tp0.delete();
 		tp1.delete();
 	}
 
 	/////////////////////////
 	// transforms
 	////////////////////////
 	
 	///////////////
 	// perspective transform (keystone)
 	//   from : http://xenia.media.mit.edu/~cwren/interpolator/
 	//   and  : http://www.bluebit.gr/matrix-calculator/linear_equations.aspx
 	//
 	private static void keyTransform(PImage src, PImage dst) {
 		// transform parameters
 		// how much to keystone
 		float w0 = Word.random(5f, 20f);
 		float w1 = 100.0f-w0;
 
 		// squeeze top or bottom
 		int squeezeTop = (int)(Word.random(0f,2f));
 
 		// pick transform parameters based on amount and location of squeeze
 		float a =  (squeezeTop == 1)?(100.0f/(w1-w0)):(100.0f/(w1+w0));
 		float b =  (squeezeTop == 1)?(0+w0/(w1-w0)):(0-w0/(w1+w0));
 		float c =  (squeezeTop == 1)?(-b):(0);
 		float d =  0.0f;
 		float e =  (squeezeTop == 1)?((w1+w0)/(w1-w0)):((w1-w0)/(w1+w0));
 		float f =  0.0f;
 		float g =  0.0f;
 		float h =  2*b;
 
 		// image shouldn't be bigger or smaller than src
 		dst.resize(src.width+10, src.height+20);
 
 		// From : http://developer.android.com/guide/practices/design/performance.html
 		//        i < mArray.length is slowest, because the JIT can't yet optimize away 
 		//        the cost of getting the array length once for every iteration through the loop.
 		int srcW = src.width;
 		int srcH = src.height;
 		int dstW = dst.width;
 		int dstH = dst.height;
 
 		src.loadPixels();
 		dst.loadPixels();
 
 		// iterate on dst pixels..
 		for (int i=0; i<dstH; i++) {
 			for (int j=0; j<dstW; j++) {
 				// pixels in percent
 				float xdf = (float)(j)/(float)(dstW);
 				float ydf = (float)(i)/(float)(dstH);
 
 				// percent points as well
 				float xuf = (a*xdf + b*ydf + c)/(g*xdf + h*ydf + 1);
 				float yuf = (d*xdf + e*ydf + f)/(g*xdf + h*ydf + 1);
 
 				// turn back to pixels
 				int xu = (int)(xuf*srcW);
 				int yu = (int)(yuf*srcH);
 
 				if ((xu>0) && (xu<srcW) && (yu>0) && (yu<srcH)) {
 					dst.pixels[i*dstW+j] = src.pixels[yu*srcW+xu];
 				}
 				else {
 					dst.pixels[i*dstW+j] = 0xffffffff;
 				}
 			}
 		}
 	}
 
 
 	//////////////
 	// affine transform (shear)
 	//   from : http://homepages.inf.ed.ac.uk/rbf/HIPR2/affine.htm
 	//
 	private static void horizontalShear(PImage src, PImage dst) {

 		// shear amount
 		float w = Word.random(0.3f, 0.7f);
 		w *= (Word.random(-1f, 1f) > 0)?1:-1;
 		
 		// resize according to src image (which could be bigger/smaller) and shear amount
 		dst.resize((int)(src.width+src.height*Math.abs(w)), src.height+10);
 
 		// From : http://developer.android.com/guide/practices/design/performance.html
 		//        i < mArray.length is slowest, because the JIT can't yet optimize away 
 		//        the cost of getting the array length once for every iteration through the loop.
 		int srcW = src.width;
 		int srcH = src.height;
 		int dstW = dst.width;
 		int dstH = dst.height;
 
 		src.loadPixels();
 		dst.loadPixels();
 
 		// iterate on dst pixels..
 		for (int i=0; i<dstH; i++) {
 			for (int j=0; j<dstW; j++) {
 				// linear shearing
 				int xu = (int)(j+i*w);
 				int yu = i;
 
 				// center word horizontally
 				if (w>0) {
 					xu -= (int)(dstH*Math.abs(w));
 				}
 
 				// check boundaries
 				if ((xu>=0) && (xu<srcW) && (yu>=0) && (yu<srcH)) {
 					dst.pixels[i*dstW+j] = src.pixels[yu*srcW+xu];
 				}
 				else {
 					dst.pixels[i*dstW+j] = 0xffffffff;
 				}
 			}
 		}
 	}
 
 	// wave shear
 	//   simple, efficient and elegant way of vertically shifting pixels
 	//
 	private static void waveShear(PImage src, PImage dst) {

 		// number of waves/half-cycles in the shearing [1,4]
 		float nn = Word.random(1f, 4f);
 		// max amplitude of shearing
 		float ampY = Word.random(4f, 10f);
 		// pick a kind...
 		//  k<1 = constant frequency
 		//  k<2 = increasing frequency
 		//  k<3 = decreasing frequency
 		int k = (int)(Word.random(0f, 3f));
 
 		// resize according to src image (which could be bigger/smaller) and shear amount
		dst.resize(src.width, (int)(src.height+2*ampY+20));
 
 		// From : http://developer.android.com/guide/practices/design/performance.html
 		//        i < mArray.length is slowest, because the JIT can't yet optimize away 
 		//        the cost of getting the array length once for every iteration through the loop.
 		int srcW = src.width;
 		int srcH = src.height;
 		int dstW = dst.width;
 		int dstH = dst.height;
 
 		src.loadPixels();
 		dst.loadPixels();
 
 		// iterate on dst pixels..
 		for (int j=0; j<dstW; j++) {
 			for (int i=0; i<dstH; i++) {

 				// sinusoidal shearing
				float yuf = 0;
 				if (k<1) {
 					// constant frequency
					yuf = i-ampY*(float)(Math.sin(Math.PI*j*nn/dstW));
 				}
 				else if (k<2) {
 					// increasing frequency
					yuf = i-ampY*(float)(Math.sin(Math.PI*(Word.constrain(nn-1, 1, nn)*0.01f*(float)(j+dstW/2))*(j)/dstW));
 				}
 				else if (k<3) {
 					//decreasing frequency
					yuf = i-ampY*(float)(Math.sin(Math.PI*(Word.constrain(nn-2, 1, nn)*0.01f*(float)((dstW-j)+dstW/2))*(dstW-j)/dstW));
 				}
 
				// center vertically 
				int yu = (int)(yuf-ampY/2);
 				int xu = j;
 
 				// check boundaries
 				if ((yu>=0) && (yu<srcH)) {
 					dst.pixels[i*dstW+j] = src.pixels[yu*srcW+xu];
 				}
 				else {
 					dst.pixels[i*dstW+j] = 0xffffffff;
 				}
 			}
 		}
 	}
 
 	/////////////////////
 	// swirl ditort??
 	//    from : http://stackoverflow.com/questions/225548/resources-for-image-distortion-algorithms
 	//
 	//    a = amount of rotation
 	//    b = size of effect
 	//
 	//    angle = a*exp(-(x*x+y*y)/(b*b))
 	//    u = cos(angle)*x + sin(angle)*y
 	//    v = -sin(angle)*x + cos(angle)*y
 	//
 	private static void swirl(PImage src, PImage dst, int xc, int yc) {
 		float a = 0.01f; // w
 		float b = 66;
 
 		// From : http://developer.android.com/guide/practices/design/performance.html
 		//        i < mArray.length is slowest, because the JIT can't yet optimize away 
 		//        the cost of getting the array length once for every iteration through the loop.
 		int srcW = src.width;
 		int dstW = dst.width;
 		int dstH = dst.height;
 
 		src.loadPixels();
 		dst.loadPixels();
 
 		// iterate on dst pixels..
 		for (int i=0; i<dstH; i++) {      
 			for (int j=0; j<dstW; j++) {
 
 				float rsq = (j-xc)*(j-xc) + (i-yc)*(i-yc);
 
 				if (rsq < 200) {
 					float angle = a*rsq/b;
 					float xuf = (float)(Math.cos(angle)*(j-xc) + Math.sin(angle)*(i-yc));
 					float yuf = (float)(-Math.sin(angle)*(j-xc) + Math.cos(angle)*(i-yc));
 
 					int xu = (int)(xc+xuf);
 					int yu = (int)(yc+yuf);
 
 					dst.pixels[i*dstW+j] = src.pixels[yu*srcW+xu];
 				}
 				else {
 					dst.pixels[i*dstW+j] = src.pixels[i*srcW+j];
 				}
 			}
 		}
 	}
 
 
 	/////////////////////
 	// fisheye distortion
 	//  from : http://www.jasonwaltman.com/thesis/filter-fisheye.html
 	//  w is some magic number [0.001, 0.1]
 	//
 	private static void fisheye(PImage src, PImage dst, int xc, int yc, float w) {
 
 		// resize according to src image (which could be bigger/smaller) and shear amount
 		// !!!! ToBeImplemented
 
 		// From : http://developer.android.com/guide/practices/design/performance.html
 		//        i < mArray.length is slowest, because the JIT can't yet optimize away 
 		//        the cost of getting the array length once for every iteration through the loop.
 		int srcW = src.width;
 		int dstW = dst.width;
 		int dstH = dst.height;
 
 		src.loadPixels();
 		dst.loadPixels();
 
 		// iterate on dst pixels..
 		for (int i=0; i<dstH; i++) {      
 			for (int j=0; j<dstW; j++) {
 
 				float rsq = (j-xc)*(j-xc) + (i-yc)*(i-yc);
 
 				// do transform
 				if (rsq < 256) {
 					float R = Math.min(dstH, dstW)/2;
 					float s = (float)(R/Math.log(w*R+1));
 
 					float r = (float)(Math.sqrt(rsq));
 					float a = (float)(Math.atan2((i-yc), (j-xc)));
 
 					//r = s * log(1+w*r);
 					r = (float)(Math.exp(r/s)-1)/w;
 
 					float xuf = (float)(r*Math.cos(a));
 					float yuf = (float)(r*Math.sin(a));
 
 					int xu = (int)(xc+xuf);
 					int yu = (int)(yc+yuf);
 
 					dst.pixels[i*dstW+j] = src.pixels[yu*srcW+xu];
 				}
 				else {
 					dst.pixels[i*dstW+j] = 0x00000000;
 					dst.pixels[i*dstW+j] = src.pixels[i*srcW+j];
 				}
 			}
 		}
 
 		src.updatePixels();
 		dst.updatePixels();
 	}
 }
 
