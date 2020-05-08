 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import java.io.*;
 import javax.imageio.*;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.image.*;
 import java.util.*;
 
 /**
  *
  * @author geza
  */
 public class Main {
 
     /**
      * @param args the command line arguments
      */
 
 	public static void convolve(WritableRaster r1, WritableRaster r2, int[][] m, int w) {
 		int[] rgbf = new int[r1.getNumBands()];
 		int[] rgbft = new int[r1.getNumBands()];
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 				for (int i = 0; i < rgbf.length; ++i)
 					rgbf[i] = 0;
 				for (int my = 0; my < m.length; ++my) {
 					if (y+my-m.length/2 < 0 || y+my-m.length/2 >= r1.getHeight()) continue;
 					for (int mx = 0; mx < m[my].length; ++mx) {
 						if (x+mx-m[my].length/2 < 0 || x+mx-m[my].length/2 >= r1.getWidth()) continue;
 						r1.getPixel(x+mx-m[my].length/2, y+my-m.length/2, rgbft);
 						//System.out.println("("+rgbft[0]+","+rgbft[1]+","+rgbft[2]+")");
 						for (int i = 0; i < rgbf.length; ++i)
 							rgbf[i] += rgbft[i]*m[my][mx];
 					}
 				}
 				for (int i = 0; i < rgbf.length; ++i)
 					rgbf[i] /= w;
 				//System.out.println("("+rgbf[0]+","+rgbf[1]+","+rgbf[2]+")");
 				r2.setPixel(x, y, rgbf);
 			}
 		}
 	}
 
 	public static void convolveBW(WritableRaster r1, WritableRaster r2, int[][] m, int w) {
 		int[] rgbft = new int[r1.getNumBands()];
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 				int tot = 0;
 				for (int my = 0; my < m.length; ++my) {
 					if (y+my-m.length/2 < 0 || y+my-m.length/2 >= r1.getHeight()) continue;
 					for (int mx = 0; mx < m[my].length; ++mx) {
 						if (x+mx-m[my].length/2 < 0 || x+mx-m[my].length/2 >= r1.getWidth()) continue;
 						r1.getPixel(x+mx-m[my].length/2, y+my-m.length/2, rgbft);
 						//System.out.println("("+rgbft[0]+","+rgbft[1]+","+rgbft[2]+")");
 						for (int i = 0; i < rgbft.length; ++i)
 							tot += rgbft[i]*m[my][mx];
 					}
 				}
 				//for (int i = 0; i < rgbf.length; ++i)
 				//	rgbf[i] /= w;
 				//System.out.println("("+rgbf[0]+","+rgbf[1]+","+rgbf[2]+")");
 				//r2.setSample(x, y, 0, bound(tot/(w*3), 255, 0));
 				r2.setSample(x, y, 0, tot/(w*3));
 			}
 		}
 	}
 
 
 	public static void copyChannels(WritableRaster r1, WritableRaster r2, int ch, int nch) {
 		int[] buf1 = new int[r1.getNumBands()];
 		int[] buf2 = new int[nch];
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 				r1.getPixel(x, y, buf1);
 				for (int i = 0; i < buf2.length; ++i) {
 					buf2[i] = buf1[ch+i];
 				}
 				for (int i = 0; i < buf2.length; ++i) {
 					r2.setSample(x, y, i, buf2[i]);
 				}
 				//r2.setPixel(x, y, buf2);
 			}
 		}
 	}
 
 	public static void mergeChannels(WritableRaster r1, WritableRaster r2, WritableRaster r3) {
 		int[] buf1 = new int[r1.getNumBands()];
 		int[] buf2 = new int[r2.getNumBands()];
 		int[] buf3 = new int[r1.getNumBands()+r2.getNumBands()];
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r2.getHeight(); ++y) {
 				r1.getPixel(x, y, buf1);
 				r2.getPixel(x, y, buf2);
 				for (int i = 0; i < buf1.length; ++i) {
 					buf3[i] = buf1[i];
 				} for (int i = 0; i < buf2.length; ++i) {
 					buf3[i+buf1.length] = buf2[i];
 				}
 				r3.setPixel(x, y, buf3);
 			}
 		}
 	}
 
 	public static void sqrtImage(WritableRaster r1, WritableRaster r2, WritableRaster r3) {
 		int[] buf1 = new int[3];
 		int[] buf2 = new int[3];
 		int[] buf3 = new int[3];
 		for (int x = 0; x < r3.getWidth(); ++x) {
 			for (int y = 0; y < r3.getHeight(); ++y) {
 				r1.getPixel(x, y, buf1);
 				r2.getPixel(x, y, buf2);
 				for (int i = 0; i < buf3.length; ++i)
 					buf3[i] = buf1[i]+buf2[i];
 				r3.setPixel(x, y, buf3);
 			}
 		}
 	}
 
 	public static int square(int x) {
 		return x*x;
 	}
 
 	public static void sqrtImageBW(WritableRaster r1, WritableRaster r2, WritableRaster r3) {
 		for (int x = 0; x < r3.getWidth(); ++x) {
 			for (int y = 0; y < r3.getHeight(); ++y) {
 				r3.setSample(x, y, 0, (int)Math.sqrt(square(r1.getSample(x, y, 0))+square(r2.getSample(x, y, 0))));
 			}
 		}
 	}
 
 	public static int bound(int v, int max, int min) {
 		if (v > max) return max;
 		else if (v < min) return min;
 		else return v;
 	}
 
 	public static void normImage(WritableRaster r1, WritableRaster r2) {
 		//int[] buf = new int[3];
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 				//r1.getPixel(x, y, buf);
 				//int tot = buf[0]+buf[1]+buf[2];
 				//buf[0] *= (400.0f/tot);
 				//buf[1] *= (400.0f/tot);
 				//buf[2] *= (400.0f/tot);
 				//r2.setPixel(x, y, buf);
 				int r = r1.getSample(x, y, 0);
 				int g = r1.getSample(x, y, 1);
 				int b = r1.getSample(x, y, 2);
 				//int tot = bound((int)Math.sqrt(r*r+g*g+b*b), 765, -765);
 				int tot = r+g+b;
 				if (tot == 0) tot = 1;
 				//int tot = r+g+b;
 				r2.setSample(x, y, 0, bound(r*255/tot, 255, -255));
 				r2.setSample(x, y, 1, bound(g*255/tot, 255, -255));
 				r2.setSample(x, y, 2, bound(b*255/tot, 255, -255));
 				r2.setSample(x, y, 3, bound(255-tot/3, 255, -255));
 			}
 		}
 	}
 
 	public static void mergeAlpha(WritableRaster r1, WritableRaster r2) {
 		//int[] buf = new int[3];
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 				//r1.getPixel(x, y, buf);
 				//int tot = buf[0]+buf[1]+buf[2];
 				//buf[0] *= (400.0f/tot);
 				//buf[1] *= (400.0f/tot);
 				//buf[2] *= (400.0f/tot);
 				//r2.setPixel(x, y, buf);
 				int r = r1.getSample(x, y, 0);
 				int g = r1.getSample(x, y, 1);
 				int b = r1.getSample(x, y, 2);
 				int tot = 3*(255-r1.getSample(x, y, 3));
 				r2.setSample(x, y, 0, r*tot/255);
 				r2.setSample(x, y, 1, g*tot/255);
 				r2.setSample(x, y, 2, b*tot/255);
 			}
 		}
 	}
 
 	public static void rmlastchannel(WritableRaster r1, WritableRaster r2) {
 		int[] buf = new int[r1.getNumBands()];
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 				r1.getPixel(x, y, buf);
 				buf[buf.length-1] = 255;
 				r2.setPixel(x, y, buf);
 			}
 		}
 	}
 
 	public static void rgb2hsv(WritableRaster r1, WritableRaster r2) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 				int r = r1.getSample(x, y, 0);
 				int g = r1.getSample(x, y, 1);
 				int b = r1.getSample(x, y, 2);
 				int max = Math.max(Math.max(r, g), b);
 				int min = Math.min(Math.min(r, g), b);
 				int h = 0;
 				if (max == min) {
 					
 				} else if (max == r) {
 					h = (g-b)*85/(2*(max-min));
 					if (h < 0)
 						h += 255;
 				} else if (max == g) {
 					h = 85 + (b-r)*85/(2*(max-min));
 				} else { // max == b
 					h = 170 + (r-g)*85/(2*(max-min));
 				}
 				r2.setSample(x, y, 0, h); // h
 				r2.setSample(x, y, 1, (max == 0) ? 0 : 255*(max-min)/max); // s
 				r2.setSample(x, y, 2, max); // v
 			}
 		}
 	}
 
 	public static void colorProp(WritableRaster r1, WritableRaster r2) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 				
 			}
 		}
 	}
 
 	public static void findRed(WritableRaster r1, WritableRaster r2) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 				int r = r1.getSample(x, y, 0);
 				if (r > 110)
 					r2.setSample(x, y, 0, 255);
 			}
 		}
 	}
 
 	public static void findBlue(WritableRaster r1, WritableRaster r2) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 				int b = r1.getSample(x, y, 2);
 				if (b > 110)
 					r2.setSample(x, y, 2, 255);
 			}
 		}
 	}
 
 	public static void findGate(WritableRaster r1, WritableRaster r2) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 				int r = r1.getSample(x, y, 0);
 				int g = r1.getSample(x, y, 1);
 				int b = r1.getSample(x, y, 2);
 				if (r > 90 && g > 70 && b < 50) {
 					r2.setSample(x, y, 0, 100);
 					r2.setSample(x, y, 1, 100);
 					r2.setSample(x, y, 2, 100);
 				}
 			}
 		}
 	}
 
 	public static boolean isRed(WritableRaster r1, int x, int y) {
 		if (x >= 0 && y >= 0 && x < r1.getWidth() && y < r1.getHeight()) {
 			int r = r1.getSample(x, y, 0);
 			//int g = r1.getSample(x, y, 1);
 			//int b = r1.getSample(x, y, 2);
 			if (r > 110) return true;
 		} return false;
 	}
 
 	public static void safeDraw(WritableRaster r1, int x, int y) {
 		if (x >= 0 && y >= 0 && x < r1.getWidth() && y < r1.getHeight())
 			r1.setSample(x, y, 0, 255);
 	}
 
 	public static void rasterCircle(WritableRaster r1, int x0, int y0, int radius)
 	{
 		int f = 1 - radius;
 		int ddF_x = 1;
 		int ddF_y = -2 * radius;
 		int x = 0;
 		int y = radius;
 		safeDraw(r1,x0, y0 + radius);
 		safeDraw(r1, x0, y0 - radius);
 		safeDraw(r1, x0 + radius, y0);
 		safeDraw(r1, x0 - radius, y0);
 
 		while(x < y)
 		{
 			// ddF_x == 2 * x + 1;
 			// ddF_y == -2 * y;
 			// f == x*x + y*y - radius*radius + 2*x - y + 1;
 			if(f >= 0)
 			{
 				y--;
 				ddF_y += 2;
 				f += ddF_y;
 			}
 			x++;
 			ddF_x += 2;
 			f += ddF_x;
 			safeDraw(r1, x0 + x, y0 + y);
 			safeDraw(r1, x0 - x, y0 + y);
 			safeDraw(r1, x0 + x, y0 - y);
 			safeDraw(r1, x0 - x, y0 - y);
 			safeDraw(r1, x0 + y, y0 + x);
 			safeDraw(r1, x0 - y, y0 + x);
 			safeDraw(r1, x0 + y, y0 - x);
 			safeDraw(r1, x0 - y, y0 - x);
 		}
 	}
 
 	public static void filledCircle(WritableRaster r1, int x0, int y0, int r) {
 		int xe = Math.min(r1.getWidth(), x0+r+1);
 		int ye = Math.min(r1.getHeight(), y0+r+1);
 		for (int x = Math.max(0, x0-r); x < xe; ++x) {
 			int xq = (x0-x)*(x0-x);
 			for (int y = Math.max(0, y0-r); y < ye; ++y) {
 				if (xq+(y0-y)*(y0-y) <= r*r)
 					r1.setSample(x, y, 0, 255);
 			}
 		}
 	}
 
 	public static float median(float[] arr) {
 		if (arr.length == 0) return 0.0f;
 		if (arr.length % 2 == 0) {
 			return (arr[arr.length/2-1] + arr[arr.length/2])/2;
 		} else {
 			return arr[arr.length/2];
 		}
 	}
 
 	public static void printList(int[] c) {
 		if (c.length == 0) return;
 		System.out.print("[ ");
 		for (int x = 0; x < c.length-1; ++x) {
 			System.out.print(c[x]+", ");
 		}
 		System.out.println(c[c.length-1]+" ]");
 	}
 
 	public static void circleDetectTop(WritableRaster r1, WritableRaster r2, int startx, int starty) {
 		int nstartx = startx;
 		while (isRed(r1,nstartx,starty)) ++nstartx;
 		nstartx = (startx+nstartx)/2;
 		int diam = 0;
 		while (isRed(r1,nstartx,starty+diam)) ++diam;
 		if (diam/2 == 0) return;
 		int[] uy = new int[diam];
 		int[] ly = new int[diam];
 		for (int y = starty; y < starty+diam; ++y) {
 			for (int x = nstartx; x < r1.getWidth(); ++x) {
 				if (!isRed(r1,x,y)) {
 					uy[y-starty] = x;
 					break;
 				}
 			} for (int x = nstartx-1; x >= 0; --x) {
 				if (!isRed(r1,x,y)) {
 					ly[y-starty] = x;
 					break;
 				}
 			}
 		}
 		float[] rvals = new float[diam/2-1];
 		for (int x = 0; x < rvals.length; ++x) {
 			int c = uy[x+1]-ly[x+1];
 			int h = x+2;
 			rvals[x] = (c*c+4.0f*h*h)/(8.0f*h);
 		}
 		Arrays.sort(rvals);
 		float lr = median(rvals);
 		for (int x = 0; x < rvals.length; ++x)
 			rvals[x] = Math.abs(lr-rvals[x]);
 		Arrays.sort(rvals);
 		float ldev = median(rvals);
 		System.out.println("lr is "+lr+" ldev is "+ldev);
 		for (int x = 0; x < rvals.length; ++x) {
 			int c = uy[diam-x-1]-ly[diam-x-1];
 			//System.out.println(c);
 			int h = x+2;
 			rvals[x] = (c*c+4.0f*h*h)/(8.0f*h);
 		}
 		Arrays.sort(rvals);
 		float rr = median(rvals);
 		for (int x = 0; x < rvals.length; ++x)
 			rvals[x] = Math.abs(rr-rvals[x]);
 		Arrays.sort(rvals);
 		float rdev = median(rvals);
 		rvals = null;
 		System.out.println("rr is "+rr+" rdev is "+rdev);
 		if (ldev < 1.0f && ldev < rdev /*&& ldev < udev && ldev < bdev*/) {
 			filledCircle(r2,nstartx,(int)(starty+Math.ceil(lr)),(int)(Math.ceil(lr)));
 			r2.setSample(nstartx, (int)(Math.ceil(starty+lr)), 2, 255);
 		} else if (rdev < 1.0f && rdev < ldev /*&& rdev < udev && rdev < bdev*/) {
 			filledCircle(r2,nstartx,(int)(starty+diam-Math.ceil(rr)),(int)(Math.ceil(rr)));
 			r2.setSample(nstartx, (int)(Math.ceil(starty+diam-rr)), 2, 255);
 		} /*else if (udev < 2.0f && udev < ldev && udev < rdev && udev < bdev) {
 			filledCircle(r2,(int)(Math.ceil(startx+uc)),(int)(nstarty+umaxv-Math.ceil(ur)),(int)(Math.ceil(ur)));
 			r2.setSample((int)(Math.ceil(startx+uc)), (int)(nstarty+umaxv-Math.ceil(ur)), 2, 255);
 			//filledCircle(r2,(int)(Math.ceil(startx+bc)),(int)(nstarty-bmaxv+Math.ceil(br)),(int)(Math.ceil(br)));
 			//r2.setSample((int)(Math.ceil(startx+bc)), (int)(nstarty-bmaxv+Math.ceil(br)), 2, 255);
 		} else if  (bdev < 2.0f && bdev < ldev && bdev < rdev && bdev < udev) {
 			//filledCircle(r2,(int)(Math.ceil(startx+uc)),(int)(nstarty+umaxv-Math.ceil(ur)),(int)(Math.ceil(ur)));
 			//r2.setSample((int)(Math.ceil(startx+uc)), (int)(nstarty+umaxv-Math.ceil(ur)), 2, 255);
 			filledCircle(r2,(int)(Math.ceil(startx+bc)),(int)(nstarty-bmaxv+Math.ceil(br)),(int)(Math.ceil(br)));
 			r2.setSample((int)(Math.ceil(startx+bc)), (int)(nstarty-bmaxv+Math.ceil(br)), 2, 255);
 		}*/ else {
 			System.out.println("circledetect failed");
 		}
 	}
 
 	public static void circleDetect(WritableRaster r1, WritableRaster r2, int startx, int starty) {
 		int nstarty = starty;
 		while (isRed(r1,startx,nstarty)) ++nstarty;
 		nstarty = (starty+nstarty)/2;
 		int diam = 0;
 		while (isRed(r1,startx+diam,nstarty)) ++diam;
 		if (diam/2 == 0) return;
 		int[] uy = new int[diam];
 		int[] ly = new int[diam];
 		for (int x = startx; x < startx+diam; ++x) {
 			for (int y = nstarty; y < r1.getHeight(); ++y) {
 				if (!isRed(r1,x,y)) {
 					uy[x-startx] = y;
 					break;
 				}
 			} for (int y = nstarty-1; y >= 0; --y) {
 				if (!isRed(r1,x,y)) {
 					ly[x-startx] = y;
 					break;
 				}
 			}
 		}
 		//System.out.println("ly length "+ly.length+" uy length "+uy.length);
 		//int r = Math.min(uy.size()/2, uy.get(uy.size()/2)-ly.get(uy.size()/2));
 		//int r = (uy.get(uy.size()/2)-ly.get(uy.size()/2))/2;
 		//int r = uy.size()/2;
 		//int centery = 0;
 		//for (x = 0; x < uy.size(); ++x) {
 		//	centery += uy.get(x)+ly.get(x);
 		//}
 		//centery /= (2*uy.size());
 		//System.out.println(centery);
 		//int lmean = 0;
 		//int lvar = 0;
 		float[] rvals = new float[diam/2-1];
 		//rvals[0] = uy.size()/2.0f;
 		//rvals[uy.size()-1] = (uy.get(uy.size()/2)-ly.get(uy.size()/2))/2.0f;
 		for (int x = 0; x < rvals.length; ++x) {
 			int c = uy[x+1]-ly[x+1];
 			int h = x+2;
 			rvals[x] = (c*c+4.0f*h*h)/(8.0f*h);
 		}
 		Arrays.sort(rvals);
 		float lr = median(rvals);
 		for (int x = 0; x < rvals.length; ++x)
 			rvals[x] = Math.abs(lr-rvals[x]);
 		Arrays.sort(rvals);
 		float ldev = median(rvals);
 		System.out.println("lr is "+lr+" ldev is "+ldev);
 		for (int x = 0; x < rvals.length; ++x) {
 			int c = uy[diam-x-1]-ly[diam-x-1];
 			//System.out.println(c);
 			int h = x+2;
 			rvals[x] = (c*c+4.0f*h*h)/(8.0f*h);
 		}
 		Arrays.sort(rvals);
 		float rr = median(rvals);
 		for (int x = 0; x < rvals.length; ++x)
 			rvals[x] = Math.abs(rr-rvals[x]);
 		Arrays.sort(rvals);
 		float rdev = median(rvals);
 		rvals = null;
 		System.out.println("rr is "+rr+" rdev is "+rdev);
 		int uc = 0;
 		int umaxv = 0;
 		for (int x = 0; x < diam; ++x) {
 			if (uy[x]-nstarty > umaxv) {
 				umaxv = uy[x]-nstarty;
 				uc = x;
 			}
 		}
 		{
 			int nuc = uc;
			while (nuc < diam && nstarty-ly[nuc] == umaxv) ++nuc;
 			uc = (uc+nuc)/2;
 		}
 		rvals = new float[umaxv];
 		for (int y = umaxv+nstarty; y > nstarty; --y) {
 			int c = 0;
 			for (int x = uc; x < diam; ++x) {
 				if (uy[x] >= y) ++c;
 				else break;
 			} for (int x = uc-1; x >= 0; --x) {
 				if (uy[x] >= y) ++c;
 				else break;
 			}
 			int h = umaxv-y+nstarty+1;
 			rvals[umaxv-y+nstarty] = (c*c+4.0f*h*h)/(8.0f*h);
 		}
 		Arrays.sort(rvals);
 		float ur = median(rvals);
 		for (int x = 0; x < umaxv; ++x)
 			rvals[x] = Math.abs(ur-rvals[x]);
 		Arrays.sort(rvals);
 		float udev = median(rvals);
 		System.out.println("ur is "+ur+" udev is "+udev);
 		rvals = null;
 		int bc = 0;
 		int bmaxv = 0;
 		for (int x = 0; x < diam; ++x) {
 			if (nstarty-ly[x] > bmaxv) {
 				bmaxv = nstarty-ly[x];
 				bc = x;
 			}
 		}
 		{
 			int nbc = bc;
 			while (nbc < diam && nstarty-ly[nbc] == bmaxv) ++nbc;
 			bc = (bc+nbc)/2;
 		}
 		System.out.println("bmaxv is "+bmaxv+" bc is "+bc);
 		rvals = new float[bmaxv];
 		for (int y = nstarty-bmaxv; y < nstarty; ++y) {
 			int c = 0;
 			for (int x = bc; x < diam; ++x) {
 				if (ly[x] <= y) ++c;
 				else break;
 			} for (int x = bc-1; x >= 0; --x) {
 				if (ly[x] <= y) ++c;
 				else break;
 			}
 			int h = bmaxv+y-nstarty+1;
 			rvals[bmaxv+y-nstarty] = (c*c+4.0f*h*h)/(8.0f*h);
 		}
 		Arrays.sort(rvals);
 		float br = median(rvals);
 		for (int x = 0; x < bmaxv; ++x)
 			rvals[x] = Math.abs(br-rvals[x]);
 		Arrays.sort(rvals);
 		float bdev = median(rvals);
 		System.out.println("br is "+br+" bdev is "+bdev);
 		/*
 		if (ldev < 1.0f && ldev < rdev && ldev < udev && ldev < bdev && lr > 1.0f) {
 			filledCircle(r2,(int)(startx+Math.ceil(lr)),nstarty,(int)(Math.ceil(lr)));
 			r2.setSample((int)(Math.ceil(startx+lr)), nstarty, 2, 255);
 		} else if (rdev < 1.0f && rdev < ldev && rdev < udev && rdev < bdev && rr > 1.0f) {
 			filledCircle(r2,(int)(startx+diam-Math.ceil(rr)),nstarty,(int)(Math.ceil(rr)));
 			r2.setSample((int)(Math.ceil(startx+diam-rr)), nstarty, 2, 255);
 		} else*/ if (udev < 2.0f /*&& udev < ldev && udev < rdev*/ && udev < bdev && ur > 1.0f) {
 			filledCircle(r2,(int)(Math.ceil(startx+uc)),(int)(nstarty+umaxv-Math.ceil(ur)),(int)(Math.ceil(ur)));
 			r2.setSample((int)(Math.ceil(startx+uc)), (int)(nstarty+umaxv-Math.ceil(ur)), 2, 255);
 			//filledCircle(r2,(int)(Math.ceil(startx+bc)),(int)(nstarty-bmaxv+Math.ceil(br)),(int)(Math.ceil(br)));
 			//r2.setSample((int)(Math.ceil(startx+bc)), (int)(nstarty-bmaxv+Math.ceil(br)), 2, 255);
 		} else if  (bdev < 2.0f /*&& bdev < ldev && bdev < rdev*/ && bdev < udev && br > 1.0f) {
 			//filledCircle(r2,(int)(Math.ceil(startx+uc)),(int)(nstarty+umaxv-Math.ceil(ur)),(int)(Math.ceil(ur)));
 			//r2.setSample((int)(Math.ceil(startx+uc)), (int)(nstarty+umaxv-Math.ceil(ur)), 2, 255);
 			filledCircle(r2,(int)(Math.ceil(startx+bc)),(int)(nstarty-bmaxv+Math.ceil(br)),(int)(Math.ceil(br)));
 			r2.setSample((int)(Math.ceil(startx+bc)), (int)(nstarty-bmaxv+Math.ceil(br)), 2, 255);
 		} else {
 			System.out.println("circledetect failed");
 		}
 	}
 
 	public static void seekStart(WritableRaster r1, WritableRaster r2) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 		//for (int y = 0; y < r1.getHeight(); ++y) {
 		//	for (int x = 0; x < r1.getWidth(); ++x) {
 				if (isRed(r1,x,y) &&
 					!isRed(r2,x,y) && !isRed(r2,x-1,y) && !isRed(r2,x,y-1) &&
 					!isRed(r2,x-1,y-1) && !isRed(r2,x+1,y-1) && !isRed(r2,x+1,y) &&
 					!isRed(r2,x-1,y+1) && !isRed(r2,x,y+1) && !isRed(r2,x+1,y+1)) {
 					circleDetect(r1,r2,x,y);
 					//circleDetectTop(r1,r2,x,y);
 					System.out.println("circledetect at "+x+" , "+y);
 					//return;
 				}
 			}
 		}
 	}
 
 	public static void findEdge(WritableRaster r1, WritableRaster r2) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			int state = 0;
 			int y = 0;
 			for (; y < r1.getHeight(); ++y) { // seeking blue line
 				int v = r2.getSample(x, y, 2);
 				if (v == 255) {
 					if (state++ >= 2) break;
 				} else state = 0;
 			}
 			//System.out.println("ch1");
 			state = 0;
 			for (; y < r1.getHeight(); ++y) { // seeking end of blue
 				int v = r2.getSample(x, y, 2);
 				if (v != 255) {
 					if (state++ >= 2) break;
 				} else state = 0;
 			}
 			state = 0;
 			int[][] m = {{3,10,3},{0,0,0},{-3,-10,-3}};
 			for (; y < r1.getHeight(); ++y) { // seeking end-of-wall edge
 				int v = 0;
 				if (r1.getSample(x, y, 0)+r1.getSample(x, y, 1)+r1.getSample(x, y, 2) < 490) {
 					if (state++ >= 5) break;
 				} else state = 0;
 				for (int my = 0; my < m.length; ++my) {
 					if (y+my-m.length/2 < 0 || y+my-m.length/2 >= r1.getHeight()) continue;
 					for (int mx = 0; mx < m[my].length; ++mx) {
 						if (x+mx-m[my].length/2 < 0 || x+mx-m[my].length/2 >= r1.getWidth()) continue;
 						//r1.getPixel(x+mx-m[my].length/2, y+my-m.length/2, rgbft);
 						//System.out.println("("+rgbft[0]+","+rgbft[1]+","+rgbft[2]+")");
 						//for (int i = 0; i < rgbf.length; ++i)
 							//rgbf[i] += rgbft[i]*m[my][mx];
 						//v += r2.getSample(x+mx-m[my].length/2, y+my-m.length/2, 0)*m[my][mx]+r2.getSample(x+mx-m[my].length/2, y+my-m.length/2, 1)*m[my][mx]+r2.getSample(x+mx-m[my].length/2, y+my-m.length/2, 2)*m[my][mx];
 						v += (r1.getSample(x+mx-m[my].length/2, y+my-m.length/2, 0)+r1.getSample(x+mx-m[my].length/2, y+my-m.length/2, 1)+r1.getSample(x+mx-m[my].length/2, y+my-m.length/2, 2))*m[my][mx];
 					}
 				}
 				v /= 32;
 				//System.out.println("value of v"+v);
 				if (v > 8) {
 					break;
 				} else {
 					r2.setSample(x, y, 1, 255);
 				}
 			}
 			/*
 			//System.out.println("ch2");
 			state = 0;
 			for (; y < r1.getHeight(); ++y) { // seeking start of white
 				int v = r2.getSample(x,y,0)+r2.getSample(x,y,1)+r2.getSample(x,y,2);
 				if (v > 230) {
 					if (state++ >= 2) break;
 				} else if (state != 0) state = 0;
 			}
 			//System.out.println("ch3");
 			state = 0;
 			for (; y < r1.getHeight(); ++y) { // seeking end of white
 				int v = r2.getSample(x,y,0)+r2.getSample(x,y,1)+r2.getSample(x,y,2);
 				if (v < 230) {
 					if (state++ >= 2) break;
 				} else if (state != 0) state = 0;
 				r1.setSample(x, y, 1, 255);
 			}
 			*/
 		}
 	}
 
 	public static void main(String[] args) {
 		//for (String x : args)
 		//	System.out.println(x);
 		if (args.length < 1)
 		{
 			System.out.println("not enough args");
 			return;
 		}
 		File f = new File(args[0]);
 		if (!f.exists())
 		{
 			System.out.println("file "+args[0]+"does not exist");
 			return;
 		}
 		BufferedImage im = null;//new BufferedImage();
 		try {
 			//ImageIO.read
 			im = ImageIO.read(f);
 		} catch (IOException e) {
 			System.out.println("could not load image");
 			return;
 		}
 		WritableRaster r = im.getRaster();
 		BufferedImage im2 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r2 = im2.getRaster();
 		//float[][] m = { {01.f, 0.2f, 0.1f}, {0.2f, 0.4f, 0.2f}, {0.1f, 0.2f, 0.1f} };
 		int[][] m = {{1,2,1},{2,4,2},{1,2,1}};
 		//int[][] m = {{2,4,5,4,2},{4,9,12,9,4},{5,12,15,12,5},{4,9,12,9,4},{2,4,5,4,2}};
 		convolve(r, r2, m, 16);
 		BufferedImage im3 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r3 = im3.getRaster();
 		int[][] m3 = {{3,10,3},{0,0,0},{-3,-10,-3}};
 		convolve(r2, r3, m3, 32);
 		BufferedImage im4 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r4 = im4.getRaster();
 		int[][] m4 = {{3,0,-3},{10,0,-10},{3,0,-3}};
 		convolve(r2, r4, m4, 32);
 		BufferedImage im5 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r5 = im5.getRaster();
 		sqrtImageBW(r3, r4, r5);
 		BufferedImage im6 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_ARGB);
 		WritableRaster r6 = im6.getRaster();
 		normImage(r2, r6);
 		//BufferedImage im7 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_ARGB);
 		//WritableRaster r7 = im7.getRaster();
 		//convolve(r6, r7, m, 159);
 		BufferedImage im8 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r8 = im8.getRaster();
 		copyChannels(r6, r8, 0, 3);
 		BufferedImage im9 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r9 = im9.getRaster();
 		copyChannels(r6, r9, 3, 1);
 		/*
 		BufferedImage im10 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
 		WritableRaster r10 = im10.getRaster();
 		convolve(r9, r10, m, 159);
 		BufferedImage im11 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
 		WritableRaster r11 = im11.getRaster();
 		convolve(r10, r11, m, 159);
 		BufferedImage im12 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_ARGB);
 		WritableRaster r12 = im12.getRaster();
 		mergeChannels(r8, r11, r12);
 		BufferedImage im12n = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r12n = im12n.getRaster();
 		mergeAlpha(r12, r12n);
 		BufferedImage im13 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
 		WritableRaster r13 = im13.getRaster();
 		convolve(r12n, r13, m3, 16);
 		BufferedImage im14 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
 		WritableRaster r14 = im14.getRaster();
 		convolve(r12n, r14, m4, 16);
 		BufferedImage im15 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
 		WritableRaster r15 = im15.getRaster();
 		sqrtImageBW(r13, r14, r15);
 		BufferedImage im16 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r16 = im16.getRaster();
 		rgb2hsv(r, r16);
 		*/
 		BufferedImage im10 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r10 = im10.getRaster();
 		convolve(r8, r10, m3, 32);
 		BufferedImage im11 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r11 = im11.getRaster();
 		convolve(r8, r11, m4, 32);
 		BufferedImage im12 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r12 = im12.getRaster();
 		sqrtImageBW(r10, r11, r12);
 		BufferedImage im9b = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r9b = im9b.getRaster();
 		convolve(r9, r9b, m, 159);
 		BufferedImage im13 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r13 = im13.getRaster();
 		convolve(r9b, r13, m3, 32);
 		BufferedImage im14 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r14 = im14.getRaster();
 		convolve(r9b, r14, m4, 32);
 		BufferedImage im15 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r15 = im15.getRaster();
 		sqrtImageBW(r13, r14, r15);
 		BufferedImage im16 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r16 = im16.getRaster();
 		sqrtImageBW(r12, r15, r16);
 		BufferedImage im17 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r17 = im17.getRaster();
 		seekStart(r8, r17);
 		//findRed(r8 , r17);
 		BufferedImage im18 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r18 = im18.getRaster();
 		findBlue(r8 , r17);
 		BufferedImage im19 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
 		WritableRaster r19 = im19.getRaster();
 		findEdge(r2, r17);
 		//findGate(r8, r17);
 		
 		//convolve(r6, r8, m, 159);
 		//for (int x = 0; x < 50; ++x) {
 		//	for (int y = 0; y < 50; ++y)
 		//		im.getR
 		//		im.setRGB(x, y, 255*255*255);
 		//}
 		JFrame jf = new JFrame();
 		ImageIcon ic = new ImageIcon();
 		JLabel jl = new JLabel();
 		ic.setImage(im);
 		jl.setIcon(ic);
 		ImageIcon ic2 = new ImageIcon();
 		JLabel jl2 = new JLabel();
 		ic2.setImage(im2);
 		jl2.setIcon(ic2);
 		ImageIcon ic3 = new ImageIcon();
 		JLabel jl3 = new JLabel();
 		ic3.setImage(im5);
 		jl3.setIcon(ic3);
 		ImageIcon ic4 = new ImageIcon();
 		JLabel jl4 = new JLabel();
 		ic4.setImage(im6);
 		jl4.setIcon(ic4);
 		ImageIcon ic5 = new ImageIcon();
 		//JLabel jl5 = new JLabel();
 		//ic5.setImage(im7);
 		//jl5.setIcon(ic5);
 		ImageIcon ic6 = new ImageIcon();
 		JLabel jl6 = new JLabel();
 		ic6.setImage(im8);
 		jl6.setIcon(ic6);
 		ImageIcon ic7 = new ImageIcon();
 		JLabel jl7 = new JLabel();
 		ic7.setImage(im9);
 		jl7.setIcon(ic7);
 		ImageIcon ic8 = new ImageIcon();
 		JLabel jl8 = new JLabel();
 		ic8.setImage(im12);
 		jl8.setIcon(ic8);
 		ImageIcon ic9 = new ImageIcon();
 		JLabel jl9 = new JLabel();
 		ic9.setImage(im15);
 		jl9.setIcon(ic9);
 		ImageIcon ic10 = new ImageIcon();
 		JLabel jl10 = new JLabel();
 		ic10.setImage(im16);
 		jl10.setIcon(ic10);
 		ImageIcon ic11 = new ImageIcon();
 		JLabel jl11 = new JLabel();
 		ic11.setImage(im17);
 		jl11.setIcon(ic11);
 		ImageIcon ic12 = new ImageIcon();
 		JLabel jl12 = new JLabel();
 		ic12.setImage(im18);
 		jl12.setIcon(ic12);
 		ImageIcon ic13 = new ImageIcon();
 		JLabel jl13 = new JLabel();
 		ic13.setImage(im19);
 		jl13.setIcon(ic13);
 		JPanel cp = new JPanel(new GridLayout(3,4));
 		cp.add(jl);
 		cp.add(jl2);
 		cp.add(jl3);
 		cp.add(jl4);
 		//cp.add(jl5);
 		cp.add(jl6);
 		cp.add(jl7);
 		cp.add(jl8);
 		cp.add(jl9);
 		cp.add(jl10);
 		cp.add(jl11);
 		cp.add(jl12);
 		cp.add(jl13);
 		//jf.getContentPane().add(jl);
 		//jf.getContentPane().add(jl2);
 		//jf.setSize(im.getWidth()*2, im.getHeight());
 		//jf.setVisible(true);
 		jf.setContentPane(cp);
 		jf.setSize(im.getWidth()*4, im.getHeight()*3);
 		jf.setVisible(true);
 		//jf2.setSize(im2.getWidth(), im2.getHeight());
 		//jf2.setVisible(true);
 
 	//System.out.println(args);
         // TODO code application logic here
     }
 
 }
