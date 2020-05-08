 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author geza
  */
 
 //import javax.imageio.*;
 import java.util.*;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.image.*;
 
 public class Vision extends java.lang.Thread {
 	public boolean running = true;
 	public double[] leftMotorAction = null;
 	public float[] leftMotorWeight = null;
 	public double[] rightMotorAction = null;
 	public float[] rightMotorWeight = null;
 	//public float[] rollerAction = null;
 	//public float[] rollerWeight = null;
 	public int idx = 0;
 	public int found = 0;
 	public int lifetime = 0;
 	public int distance = Integer.MAX_VALUE;
 	public int pxoffset = 0;
 	public int unknownpxoffset = 0;
 	public byte color = 0; // 0 is red, 1 is yellow, 2 is blue
 	public BufferedImage origI = null;
 	public WritableRaster origR = null;
 	public ImageIcon origC = null;
 	public JLabel origL = null;
 	public BufferedImage hsvI = null;
 	public WritableRaster hsvR = null;
 	public ImageIcon hsvC = null;
 	public JLabel hsvL = null;
 	public BufferedImage hI = null;
 	public WritableRaster hR = null;
 	public ImageIcon hC = null;
 	public JLabel hL = null;
 	public BufferedImage sI = null;
 	public WritableRaster sR = null;
 	public ImageIcon sC = null;
 	public JLabel sL = null;
 	public BufferedImage vI = null;
 	public WritableRaster vR = null;
 	public ImageIcon vC = null;
 	public JLabel vL = null;
 	public BufferedImage colorI = null;
 	public WritableRaster colorR = null;
 	public ImageIcon colorC = null;
 	public JLabel colorL = null;
 	public BufferedImage wallI = null;
 	public WritableRaster wallR = null;
 	public ImageIcon wallC = null;
 	public JLabel wallL = null;
 	public BufferedImage mapI = null;
 	public WritableRaster mapR = null;
 	public ImageIcon mapC = null;
 	public JLabel mapL = null;
 	public BufferedImage dispI = null;
 	public WritableRaster dispR = null;
 	public ImageIcon dispC = null;
 	public JLabel dispL = null;
 	public JFrame jf = null;
 	public JPanel cp = null;
 	public int[] wallbot = null;
 	public int[] wallbotm = null;
 	public int[] walltop = null;
 	public int[] walltopm = null;
 	public int[] queuex = null;
 	public int[] queuey = null;
 	//public Colors[] ctable = null;
 	public boolean circleseen = false;
 	public boolean gateseen = false;
 	public boolean unknownseen = false;
 	public boolean testmode = true;
 	public int circlecentery;
 	public int circleradius;
 	public int gatewidth;
 	public int gateheight;
 	public int unknownwidth;
 	public int gatetimer = 0;
 	public int gatepxoffset;
	public final float k = 0.005f;
 	public int state = 0;
 	public int capturecounter = 0;
 	public int[] timeouts = {80, 80, 15, 15, 80, 60, 99999, 4, 4, -1500, -1500, -1500};
 	public float[] weights = {0.3f, 0.965f, 0.4f, 0.4f, 0.965f, 2.00f, 0.4f, 0.965f, 0.965f, 3.975f, 3.975f, 3.975f};
 	public String[] names = {"rotate", "fetchball", "forward", "reverse", "gate", "shoot", "explore", "scanleft", "scanright", "turnright", "turnleft", "edgeforward"};
 	public int[] transitions = {-1, -1, -1, -1, 3, -1, 6, -1, -1, -1, -1, -1};
 	public int statetimeout = 0;
 	public long timeouttime = System.currentTimeMillis();
 	public boolean turningright = true;
 	public boolean goforward = false;
 	public int shoottimer = 0;
 	public int gapidx = 0;
 	public int gapminidx = 0;
 	public int gaplen = 0;
 	public int gapminlen = 0;
 	//public MouseController mc = null;
 	//public Orc o = null;
 	public Odometry odom = null;
 	public Gyroscope gyro = null;
 	public Arbiter arb = null;
 	public double desangle = 0;
 
 	public static boolean reverseb(boolean b) {
 		if (b) return false;
 		else return true;
 	}
 
 	public void setWeight(float newweight) {
 		leftMotorWeight[idx] = newweight;
 		rightMotorWeight[idx] = newweight;
 		//rollerWeight[idx] = newweight;
 	}
 
 	public void setState(int newstate) {
 		if (newstate == -1) {
 			turningright = reverseb(turningright);
 			newstate = 0;
 		} if (newstate == -2) {
 			if (goforward = reverseb(goforward)) {
 				newstate = 2;
 			} else {
 				newstate = 3;
 			}
 		} if (newstate == 9) { // turn 90 degrees
 			desangle = gyro.angled + 90.0 % 360.0;
 		} if (newstate == 10) { // turn 90 degrees
 			desangle = gyro.angled + 270.0 % 360.0;
 		} if (newstate == 11) { // go straight
 			desangle = gyro.angled;
 		} if (newstate == 5) { // gate shoot
 			desangle = gyro.angled;
 			arb.rollerAction = -1.0;
 		} else {
 			arb.rollerAction = 1.0;
 		}
 		System.err.println("transition to "+names[newstate]);
 		state = newstate;
 		statetimeout = 0;
 		if (timeouts[state] < 0) {
 			timeouttime = System.currentTimeMillis() - timeouts[state];
 		}
 		setWeight(weights[newstate]);
 		return;
 	}
 
 	public void run() {
 		try {
 		//byte[] inet = {(byte)192, (byte)168, (byte)237, (byte)7};
 		//Orc o = new orc.Orc(java.net.Inet4Address.getByAddress(inet));
 		System.out.println("1");
 		orc.camera.Camera c;
 		System.out.println("1a");
 		try {
 		c = new orc.camera.Camera("/dev/video0");
 		} catch (Exception e) {
 			e.printStackTrace();
 			c = new orc.camera.Camera("/dev/video1");
 		}
 		//c = orc.camera.Camera.makeCamera();
 		c.setBacklightCompensation(false);
 		c.setGain(0);
 		c.setNoiseReduction(3);
 		c.setWhiteBalanceMode(0);
 		System.out.println("2");
 		//orc.camera.Camera c = new orc.camera.Camera("/dev/video0");
 		origI = c.createImage();
 		c.capture(origI);
 		allocImages();
 		if (testmode) {
 		setupImagePanels();
 		}
 		leftMotorWeight[idx] = 0.5f;
 		rightMotorWeight[idx] = 0.5f;
 		setState(0);
 		// 0 = rotating
 		// 1 = going forward to get seen ball
 		// 2 = capturing previously seen ball
 		while (running) {
 			if (timeouts[state] >= 0) { // cycle time
 				if (statetimeout >= timeouts[state]) { // state timed out, make transition
 				setState(transitions[state]);
 			} else {
 				++statetimeout;
 			}
 			} else { // human time
 				if (System.currentTimeMillis() >= timeouttime) {
 					setState(transitions[state]);
 				}
 			}
 			System.out.println("state is "+state+" timeout is "+statetimeout);
 			/*
 			if (found > 0) --found;
 			if (lifetime > 0) --lifetime;
 			*/
 			c.capture(origI);
 			//boolean circleseen = false;
 			circleseen = false;
 			circleradius = 0;
 			pxoffset = 0;
 			gateseen = false;
 			gatewidth = 0;
 			gatepxoffset = 0;
 			unknownseen = false;
 			unknownpxoffset = 0;
 			unknownwidth = 0;
 			processImage();
 			++gatetimer;
 			System.out.println("gate timer is "+gatetimer);
 			
 			if (unknownseen && state != 4 && state != 5) {
 				if (unknownpxoffset > 0) {
 					setState(8);
 				} else {
 					setState(7);
 				}
 			}
 			if (circleseen && state != 4 && state != 5) {
 				setState(1);
 			}
 			
 			if (gateseen && state != 5) {
 				if (gatetimer > 500) {
 					System.out.println("approach gate");
 					setState(4);
 				} else if (gatewidth > 100 || gateheight > 100) {
 					System.out.println("backup from gate");
 					setState(3);
 				}
 			}
 			
 			//if (found > 0) { // moving towards ball
 			if (state == 0) { // idly searching, nothing interesting in sight, turn left
 				if (turningright) {
 					leftMotorAction[idx] = 0.6f;
 					rightMotorAction[idx] = -0.6f;
 				} else {
 					leftMotorAction[idx] = -0.6f;
 					rightMotorAction[idx] = 0.6f;
 				}
 			}
 			if (state == 1) { // getball
 				if (!circleseen) { // ball out of sight, let's capture it
 					if (circleradius > 5 || circlecentery > origR.getHeight()/2) { // capture the ball
 						setState(2);
 						setWeight(1.0f);
 						statetimeout = 20;
 					} else { // we misssed the ball, search further //go back // search further
 						//setState(-1);
 						if (pxoffset > 0) { // likely disappeared off the right, scan right
 							setState(8);
 						} else {
 							setState(7);
 						}
 					}
 				} else { // we see a ball, go to it
 				float basevel = 0.7f;
 				//float basevel = bound(1.0f-Math.abs(pxoffset)/0.1f, 1.0f, 0.7f);
 				float rspeed = -k*pxoffset; //+ 0.6f;
 				float lspeed = k*pxoffset; //+ 0.6f;
 				if (lspeed > rspeed) {
 					rspeed += basevel-Math.abs(lspeed);
 					lspeed = basevel;
 				} else {
 					lspeed += basevel-Math.abs(rspeed);
 					rspeed = basevel;
 				}
 				leftMotorAction[idx] = lspeed;
 				rightMotorAction[idx] = rspeed;
 				}
 			} if (state == 2) { // forwards
 				leftMotorAction[idx] = 0.7f;
 				rightMotorAction[idx] = 0.7f;
 			} if (state == 3) { // backwards
 				leftMotorAction[idx] = -0.7f;
 				rightMotorAction[idx] = -0.7f;
 			} if (state == 4) { // gate delivery approach
 				/*if (!gateseen) { // we missed the gate, back up
 					setState(3);
 				} else*/ if (gatewidth > 150) { // shoot those balls
 					setState(5);
 					gatetimer = 0;
 					shoottimer = 0;
 				} else { // approach the gate
 				float basevel = 0.7f;
 				//float basevel = bound(1.0f-Math.abs(gatepxoffset)/0.1f, 1.0f, 0.7f);
 				float rspeed = -k*gatepxoffset; //+ 0.6f;
 				float lspeed = k*gatepxoffset; //+ 0.6f;
 				if (lspeed > rspeed) {
 					rspeed += basevel-Math.abs(lspeed);
 					lspeed = basevel;
 				} else {
 					lspeed += basevel-Math.abs(rspeed);
 					rspeed = basevel;
 				}
 				leftMotorAction[idx] = lspeed;
 				rightMotorAction[idx] = rspeed;
 				}
 			} if (state == 5) { // gate delivery shoot
 				++shoottimer;
 				if (shoottimer < 8) { // go back
 					int cangle = gyro.anglei;
 					float basevel = -0.7f;
 					float nk = 0.02f;
 					leftMotorAction[idx] = basevel - (nk*(circsub(cangle, desangle)));
 					rightMotorAction[idx] = basevel + (nk*(circsub(cangle, desangle)));
 					/*
 					if (gateseen) {
 					float basevel = -0.7f;
 					//float basevel = bound(1.0f-Math.abs(gatepxoffset)/0.1f, 1.0f, 0.7f);
 					float rspeed = k*gatepxoffset; //+ 0.6f;
 					float lspeed = -k*gatepxoffset; //+ 0.6f;
 					if (lspeed > rspeed) {
 						rspeed += basevel-Math.abs(lspeed);
 						lspeed = basevel;
 					} else {
 						lspeed += basevel-Math.abs(rspeed);
 						rspeed = basevel;
 					}
 					leftMotorAction[idx] = lspeed;
 					rightMotorAction[idx] = rspeed;
 					} else {
 					rightMotorAction[idx] = -0.7f;
 					leftMotorAction[idx] = -0.7f;
 					}
 					 */
 				} else if (shoottimer < 10) { // stop
 					rightMotorAction[idx] = 0.0f;
 					leftMotorAction[idx] = 0.0f;
 				}else {
 					if (shoottimer == 17) {
 						shoottimer = 0;
 					} else { // forward
 					int cangle = gyro.anglei;
 					float basevel = 0.7f;
 					float nk = 0.02f;
 					leftMotorAction[idx] = basevel - (nk*(circsub(cangle, desangle)));
 					rightMotorAction[idx] = basevel + (nk*(circsub(cangle, desangle)));
 					/*
 					if (gateseen) {
 					float basevel = 0.7f;
 					//float basevel = bound(1.0f-Math.abs(gatepxoffset)/0.1f, 1.0f, 0.7f);
 					float rspeed = -k*gatepxoffset; //+ 0.6f;
 					float lspeed = k*gatepxoffset; //+ 0.6f;
 					if (lspeed > rspeed) {
 						rspeed += basevel-Math.abs(lspeed);
 						lspeed = basevel;
 					} else {
 						lspeed += basevel-Math.abs(rspeed);
 						rspeed = basevel;
 					}
 					leftMotorAction[idx] = lspeed;
 					rightMotorAction[idx] = rspeed;
 					} else {
 					rightMotorAction[idx] = 0.7f;
 					leftMotorAction[idx] = 0.7f;
 					}
 					*/
 					}
 				}
 			} if (state == 6) { // explore
 				/*
 				if (gaplen < 20 || gapminlen < 5) {
 					System.err.println("backing up gapminlen is "+gapminlen);
 					float basevel = bound(-1.0f+Math.abs(gapidx - origR.getWidth()/2)/0.1f, -1.0f, -0.7f);
 					float rspeed = -k*(gapidx - origR.getWidth()/2); //+ 0.6f;
 					float lspeed = k*(gapidx - origR.getWidth()/2); //+ 0.6f;
 					if (lspeed > rspeed) {
 						rspeed += basevel-Math.abs(lspeed);
 						lspeed = basevel;
 					} else {
 						lspeed += basevel-Math.abs(rspeed);
 						rspeed = basevel;
 					}
 					leftMotorAction[idx] = lspeed;
 					rightMotorAction[idx] = rspeed;
 				} else*/ {
 					float basevel = 0.7f;
 					//float basevel = bound(1.0f-Math.abs(gapidx - origR.getWidth()/2)/0.1f, 0.8f, 0.7f);
 					float rspeed = -k*(gapidx - origR.getWidth()/2) ;//+ basevel;
 					float lspeed = k*(gapidx - origR.getWidth()/2) ;//+ basevel;
 					
 					if (lspeed > rspeed) {
 						rspeed += basevel-Math.abs(lspeed);
 						lspeed = basevel;
 					} else {
 						lspeed += basevel-Math.abs(rspeed);
 						rspeed = basevel;
 					}
 					
 					leftMotorAction[idx] = lspeed;
 					rightMotorAction[idx] = rspeed;
 				}
 			} if (state == 7) { // rotate left
 				leftMotorAction[idx] = -0.1f;
 				rightMotorAction[idx] = 0.7f;
 			} if (state == 8) { // rotate right
 				leftMotorAction[idx] = 0.7f;
 				rightMotorAction[idx] = -0.1f;
 			} if (state == 9) { // turn right 90 degrees
 				double cangle = gyro.angled;
 				if (Math.abs(cangle-desangle) < 5) { // we're done
 					leftMotorAction[idx] = 0.0f;
 					rightMotorAction[idx] = 0.0f;
 					break;
 				} else {
 					leftMotorAction[idx] = 0.7f;
 					rightMotorAction[idx] = -0.7f;
 				}
 			} if (state == 10) { // turn left 90 degrees
 				double cangle = gyro.angled;
 				if (Math.abs(cangle-desangle) < 5) { // we're done
 					leftMotorAction[idx] = 0.0f;
 					rightMotorAction[idx] = 0.0f;
 					break;
 				} else {
 					leftMotorAction[idx] = -0.7f;
 					rightMotorAction[idx] = 0.7f;
 				}
 			} if (state == 11) { // edge forward
 				double cangle = gyro.angled;
 				float basevel = 0.7f;
 				double nk = 0.1;
 				leftMotorAction[idx] = basevel - (nk*(circsub(cangle, desangle)));
 				rightMotorAction[idx] = basevel + (nk*(circsub(cangle, desangle)));
 			}
			java.lang.Thread.sleep(10);
 		}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public int circdiff(int ang1, int ang2) {
 		if (ang1 > ang2) {
 			return Math.min(Math.abs(ang2+360-ang1), Math.abs(ang1-ang2));
 		} else {
 			return Math.min(Math.abs(ang1+360-ang2), Math.abs(ang1-ang2));
 		}
 	}
 
 	public double circsub(double ang1, double ang2) {
 		double retv = ang1-ang2;
 		if (retv > 180.0) {
 			return retv - 360.0;
 		} else if (retv < -180.0) {
 			return retv + 360.0;
 		} else {
 			return retv;
 		}
 	}
 
 	public int circsub(int ang1, int ang2) {
 		int retv = ang1-ang2;
 		if (retv > 180) {
 			return retv - 360;
 		} else if (retv < -180) {
 			return retv + 360;
 		} else {
 			return retv;
 		}
 	}
 
 	public void setup(Arbiter a, int ActionWeightIndex) {
 		arb = a;
 		idx = ActionWeightIndex;
 		leftMotorAction = a.leftMotorAction;
 		leftMotorWeight = a.leftMotorWeight;
 		rightMotorAction = a.rightMotorAction;
 		rightMotorWeight = a.rightMotorWeight;
 		CON.ctable = new byte[16777216];
 		for (int r = 0; r < 256; ++r) {
 			for (int g = 0; g < 256; ++g) {
 				for (int b = 0; b < 256; ++b) {
 					int max = Math.max(Math.max(r, g), b);
 					int min = Math.min(Math.min(r, g), b);
 					int h = 0;
 					int s = 0;
 					int delta = max-min;
 					if (delta == 0) delta = 1;
 					if (max != 0) {
 						s = 255*delta/max;
 						if (max == r) {
 							h = (g-b)*85/(2*delta);
 							if (h < 0)
 								h += 256;
 						} else if (max == g) {
 							h = 85 + (b-r)*85/(2*delta);
 						} else { // max == b
 							h = 170 + (r-g)*85/(2*delta);
 						}
 						CON.ctable[r*65536+g*256+b] = (byte)getColorHsv(h,s,max).ordinal();
 					}
 				}
 			}
 		}
 	}
 
 	public void bye() {
 		running = false;
 	}
 
 	public void setupImagePanels() {
 		jf = new JFrame();
 		origC = new ImageIcon();
 		origL = new JLabel();
 		origC.setImage(origI);
 		origL.setIcon(origC);
 		//hsvC = new ImageIcon();
 		//hsvL = new JLabel();
 		//hsvC.setImage(hsvI);
 		//hsvL.setIcon(hsvC);
 		//hC = new ImageIcon();
 		//hL = new JLabel();
 		//hC.setImage(hI);
 		//hL.setIcon(hC);
 		//sC = new ImageIcon();
 		//sL = new JLabel();
 		//sC.setImage(sI);
 		//sL.setIcon(sC);
 		//vC = new ImageIcon();
 		//vL = new JLabel();
 		//vC.setImage(vI);
 		//vL.setIcon(vC);
 		colorC = new ImageIcon();
 		colorL = new JLabel();
 		colorC.setImage(colorI);
 		colorL.setIcon(colorC);
 		//wallC = new ImageIcon();
 		//wallL = new JLabel();
 		//wallC.setImage(wallI);
 		//wallL.setIcon(wallC);
 		//mapC = new ImageIcon();
 		//mapL = new JLabel();
 		//mapC.setImage(mapI);
 		//mapL.setIcon(mapC);
 		dispC = new ImageIcon();
 		dispL = new JLabel();
 		dispC.setImage(dispI);
 		dispL.setIcon(dispC);
 		cp = new JPanel(new GridLayout(3,3));
 		cp.add(origL);
 		//cp.add(hsvL);
 		//cp.add(hL);
 		//cp.add(sL);
 		//cp.add(vL);
 		cp.add(colorL);
 		//cp.add(wallL);
 		//cp.add(mapL);
 		cp.add(dispL);
 		jf.setContentPane(cp);
 		jf.setSize(origI.getWidth()*3, origI.getHeight()*3);
 		jf.setVisible(true);
 	}
 
 	public void processImage() {
 		/*
 		findBlueLine(origR, walltop, wallbot);
 		meanpass(walltop);
 		meanpass(wallbot);
 		//medianfilter(walltop, walltopm);
 		//findWallBottom(origR, walltopm, wallbot);
 		//medianfilter(wallbot, wallbotm);
 		//findWalls(origR, walltop, wallbot);
 		blankTop(origR, walltop);
 		*/
 		/*
 		shadeWalls(wallR,walltop,wallbot);
 		
 		wallC.setImage(wallI);
 		wallL.setIcon(wallC);
 		wallL.repaint();
 		*/
 		if (testmode) {
 		origC.setImage(origI);
 		origL.setIcon(origC);
 		origL.repaint();
 		//rgb2hsv(origR, hsvR);
 		/*
 		breakcomponents(hsvR, hR, sR, vR);
 		hC.setImage(hI);
 		hL.setIcon(hC);
 		hL.repaint();
 		sC.setImage(sI);
 		sL.setIcon(sC);
 		sL.repaint();
 		vC.setImage(vI);
 		vL.setIcon(vC);
 		vL.repaint();
 		*/
 		//findWallBottom(hsvR, wallR);
 		//mostcarpet(hsvR, wallR);
 		//paintwalls(hsvR, wallR);
 		/*
 		paintwalls(hsvR, walltop, wallbot);
 		meanfilter2(wallbot,wallbotm);
 		meanfilter2(walltop,walltopm);
 		shadeWalls(wallR,walltopm,wallbotm);
 		findwallgap(walltopm,wallbotm);
 		blankTop(hsvR, walltopm);
 		wallC.setImage(wallI);
 		wallL.setIcon(wallC);
 		wallL.repaint();
 		*/
 		/*
 		hsvC.setImage(hsvI);
 		hsvL.setIcon(hsvC);
 		hsvL.repaint();
 		*/
 		/*
 		mapwalls(mapR,walltopm,wallbotm);
 		mapC.setImage(mapI);
 		mapL.setIcon(mapC);
 		mapL.repaint();
 		*/
 		//shadeColors(hsvR,colorR);
 		//shadeColors(origR,colorR);
 		//colorC.setImage(colorI);
 		//colorL.setIcon(colorC);
 		//colorL.repaint();
 		//seekStart2(r,r3);
 		/*
 		rgb2hsv(origR, wallR);
 		wallC.setImage(wallI);
 		wallL.setIcon(wallC);
 		wallL.repaint();
 		*/
 		}
 		blankimg(dispR);
 		findExtrema(origR, dispR);
 		if (testmode) {
 		dispC.setImage(dispI);
 		dispL.setIcon(dispC);
 		dispL.repaint();
 		}
 	}
 
 	public void allocImages() {
 		origR = origI.getRaster();
 		//hsvI = new BufferedImage(origI.getWidth(), origI.getHeight(), BufferedImage.TYPE_INT_RGB);
 		//hsvR = hsvI.getRaster();
 		//hI = new BufferedImage(origI.getWidth(), origI.getHeight(), BufferedImage.TYPE_INT_RGB);
 		//hR = hI.getRaster();
 		//sI = new BufferedImage(origI.getWidth(), origI.getHeight(), BufferedImage.TYPE_INT_RGB);
 		//sR = sI.getRaster();
 		//vI = new BufferedImage(origI.getWidth(), origI.getHeight(), BufferedImage.TYPE_INT_RGB);
 		//vR = vI.getRaster();
 		colorI = new BufferedImage(origI.getWidth(), origI.getHeight(), BufferedImage.TYPE_INT_RGB);
 		colorR = colorI.getRaster();
 		//wallI = new BufferedImage(origI.getWidth(), origI.getHeight(), BufferedImage.TYPE_INT_RGB);
 		//wallR = wallI.getRaster();
 		//shadeRed(origR,colorR);
 		//mapI = new BufferedImage(origI.getWidth(), origI.getHeight(), BufferedImage.TYPE_INT_RGB);
 		//mapR = mapI.getRaster();
 		dispI = new BufferedImage(origI.getWidth(), origI.getHeight(), BufferedImage.TYPE_INT_RGB);
 		dispR = dispI.getRaster();
 		wallbot = new int[origI.getWidth()];
 		Arrays.fill(wallbot, origI.getHeight()-1);
 		wallbotm = new int[origI.getWidth()];
 		Arrays.fill(wallbotm, origI.getHeight()-1);
 		walltop = new int[origI.getWidth()];
 		walltopm = new int[origI.getWidth()];
 		queuex = new int[origI.getWidth()*origI.getHeight()];
 		queuey = new int[origI.getWidth()*origI.getHeight()];
 	}
 
 	public static void findWallBottom(WritableRaster r1, WritableRaster r2) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			int lls = 0;
 			int llv = 0;
 			int lms = r1.getSample(x, r1.getHeight()-1, 1);
 			int lmv = r1.getSample(x, r1.getHeight()-1, 2);
 			int mms = r1.getSample(x, r1.getHeight()-2, 1);
 			int mmv = r1.getSample(x, r1.getHeight()-2, 2);
 			int tms = r1.getSample(x, r1.getHeight()-3, 1);
 			int tmv = r1.getSample(x, r1.getHeight()-3, 2);
 			int tts = r1.getSample(x, r1.getHeight()-4, 1);
 			int ttv = r1.getSample(x, r1.getHeight()-4, 2);
 			int maxidx = r1.getHeight()-1;
 			int maxval = Integer.MIN_VALUE;
 			for (int y = r1.getHeight()-5; y >= 0; --y) {
 				lls = lms;
 				lms = mms;
 				mms = tms;
 				tms = tts;
 				llv = lmv;
 				lmv = mmv;
 				mmv = tmv;
 				tmv = ttv;
 				tts = r1.getSample(x, y, 1);
 				ttv = r1.getSample(x, y, 2);
 				if (lls > 60) continue;
 				if (lms > 60) continue;
 				if (mms > 60) continue;
 				if (tms > 60) continue;
 				if (tts > 60) continue;
 				int changes = Math.abs(-3*lls-lms+tms+3*tts);
 				int changev = Math.abs(-3*llv-lmv+tmv+3*ttv);
 				int changet = changev/8; //(changes+changev)/16;
 				r2.setSample(x, y, 0, changet);
 				r2.setSample(x, y, 1, changet);
 				r2.setSample(x, y, 2, changet);
 				if (changet > maxval) {
 					maxidx = y;
 					maxval = changet;
 				}
 			}
 			r2.setSample(x, maxidx, 0, 255);
 			r2.setSample(x, maxidx, 1, 255);
 			r2.setSample(x, maxidx, 2, 255);
 		}
 	}
 
 	public static void shiftleft(int[] a, int v) {
 		int i = 0;
 		for (; i < a.length-1; ++i) {
 			a[i] = a[i+1];
 		}
 		a[i] = v;
 	}
 
 	public void findwallgap(int[] wtop, int[] wbot) {
 		int[] gvals = new int[15];
 		int minidx = 7;
 		int maxidx = 7;
 		gvals[0] = origR.getHeight()-wbot[0];
 		gvals[1] = origR.getHeight()-wbot[1];
 		gvals[2] = origR.getHeight()-wbot[2];
 		gvals[3] = origR.getHeight()-wbot[3];
 		gvals[4] = origR.getHeight()-wbot[4];
 		gvals[5] = origR.getHeight()-wbot[5];
 		gvals[6] = origR.getHeight()-wbot[6];
 		gvals[7] = origR.getHeight()-wbot[7];
 		gvals[8] = origR.getHeight()-wbot[8];
 		gvals[9] = origR.getHeight()-wbot[9];
 		gvals[10] = origR.getHeight()-wbot[10];
 		gvals[11] = origR.getHeight()-wbot[11];
 		gvals[12] = origR.getHeight()-wbot[12];
 		gvals[13] = origR.getHeight()-wbot[13];
 		gvals[14] = origR.getHeight()-wbot[14];
 		int tot = gvals[0]+gvals[1]+gvals[2]+gvals[3]+gvals[4]+gvals[5]+gvals[6]+gvals[7]+gvals[8]+gvals[9]+gvals[10]+gvals[11]+gvals[12]+gvals[13]+gvals[14];
 		int maxv = tot;
 		int minv = tot;
 		for (int x = 8; x < wbot.length-7; ++x) {
 			tot -= gvals[0];
 			shiftleft(gvals, origR.getHeight()-wbot[x+4]);
 			tot += gvals[14];
 			if (tot > maxv) {
 				maxv = tot;
 				maxidx = x;
 			} else if (tot < minv) {
 				minv = tot;
 				minidx = x;
 			}
 		}
 		gapidx = maxidx;
 		gapminidx = minidx;
 		gaplen = maxv/15;
 		gapminlen = minv/15;
 		wallR.setSample(maxidx, 5, 0, 255);
 	}
 
 	public static void mapwalls(WritableRaster r1, int[] wtop, int[] wbot) {
 		for (int x = 0; x < wtop.length; ++x) {
 			if (wbot[x]-wtop[x] == 0) {
 				for (int y = 0; y < r1.getHeight(); ++y) {
 					r1.setSample(x, y, 0, 0);
 					r1.setSample(x, y, 1, 0);
 					r1.setSample(x, y, 2, 0);
 				}
 				continue;
 			}
 			int distance = r1.getHeight()-wbot[x]+2000/(wbot[x]-wtop[x]);
 			if (distance >= r1.getHeight() || distance < 0) continue;
 			r1.setSample(x, r1.getHeight()-distance, 0, 255);
 			r1.setSample(x, r1.getHeight()-distance, 1, 255);
 			r1.setSample(x, r1.getHeight()-distance, 2, 255);
 			for (int y = 0; y < r1.getHeight(); ++y) {
 				if (y == r1.getHeight()-distance) continue;
 				r1.setSample(x, y, 0, 0);
 				r1.setSample(x, y, 1, 0);
 				r1.setSample(x, y, 2, 0);
 			}
 		}
 	}
 
 	public void paintwalls(WritableRaster r1, int[] wtop, int[] wbot) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			int numcarpet = 0;
 			int numwall = 0;
 			int endcarpet = 0;
 			for (int y = r1.getHeight()-1; y >= 0; --y) {
 				int h = r1.getSample(x, y, 0);
 				int s = r1.getSample(x, y, 1);
 				int v = r1.getSample(x, y, 2);
 				if (isBlue(h,s,v)) {
 					break;
 				}
 				if (isWhite(h,s,v)) {
 					endcarpet += 2;
 					if (++numwall > 8) break;
 				} else {
 					numwall = 0;
 					if (isCarpet(h,s,v)) {
 						++numcarpet;
 						endcarpet = 0;
 					} else if (++endcarpet < 15) {
 						++numcarpet;
 					}
 				}
 			}
 			//int walltop = endcarpet;
 			endcarpet = 0; // endcarpet used as endwall
 			for (int y = r1.getHeight()-1-(numcarpet); y >= 0; --y) {
 				int h = r1.getSample(x, y, 0);
 				int s = r1.getSample(x, y, 1);
 				int v = r1.getSample(x, y, 2);
 				if (isBlue(h,s,v)) {
 					//walltop = y;
 					numcarpet = r1.getHeight()-1-(y+numwall);
 					//System.out.println(numcarpet);
 					break;
 				}
 				if (isWhite(h,s,v)) {
 					++numwall;
 					endcarpet = 0;
 				} else {
 					Colors c = getColor(h,s,v);
 					if (c != Colors.Red && c != Colors.Yellow) {
 						if (++endcarpet > 2) break;
 					}
 				}
 			}
 			//if (false) {
 			if (numwall < 20) {
 				wbot[x] = 0;
 				wtop[x] = 0;
 			} else {
 				wbot[x] = r1.getHeight()-1-(numcarpet);
 				wtop[x] = r1.getHeight()-1-(numcarpet)-(numwall);
 				if (wtop[x] < 0) wtop[x] = 0;
 			}
 			/*
 			if (numwall < 20) continue;
 			//r2.setSample(x, r1.getHeight()-1-(numcarpet), 0, 255);
 			//r2.setSample(x, r1.getHeight()-1-(numcarpet), 1, 255);
 			//r2.setSample(x, r1.getHeight()-1-(numcarpet), 2, 255);
 			for (int y = r1.getHeight()-1-(numcarpet); y >= r1.getHeight()-1-(numcarpet)-(numwall) && y >= 0; --y) {
 				r2.setSample(x, y, 0, 255);
 				r2.setSample(x, y, 1, 255);
 				r2.setSample(x, y, 2, 255);
 			}
 			*/
 		}
 	}
 
 	public void mostcarpet(WritableRaster r1, WritableRaster r2) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			int numcarpet = 0;
 			int numwall = 0;
 			int endcarpet = 0;
 			for (int y = r1.getHeight()-1; y >= 0; --y) {
 				int h = r1.getSample(x, y, 0);
 				int s = r1.getSample(x, y, 1);
 				int v = r1.getSample(x, y, 2);
 				if (isBlue(h,s,v)) {
 					break;
 				}
 				if (isWhite(h,s,v)) {
 					endcarpet += 2;
 					if (++numwall > 8) break;
 				} else {
 					numwall = 0;
 					if (isCarpet(h,s,v)) {
 						++numcarpet;
 						endcarpet = 0;
 					} else if (++endcarpet < 15) {
 						++numcarpet;
 					}
 				}
 			}
 			r2.setSample(x, r1.getHeight()-1-(numcarpet), 0, 255);
 			r2.setSample(x, r1.getHeight()-1-(numcarpet), 1, 255);
 			r2.setSample(x, r1.getHeight()-1-(numcarpet), 2, 255);
 		}
 	}
 
 	public void walldist2(WritableRaster r1, WritableRaster r2) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			int numcarpet = 0;
 			int numwall = 0;
 			int endcarpet = 0;
 			int endwall = 0;
 			for (int y = r1.getHeight()-1; y >= 0; --y) {
 				int h = r1.getSample(x, y, 0);
 				int s = r1.getSample(x, y, 1);
 				int v = r1.getSample(x, y, 2);
 				if (isBlue(h,s,v)) {
 					numwall += 10;
 					break;
 				}
 				if (endcarpet < 5) { // seeking end of carpet
 					if (isWhite(h,s,v)) {
 						++endcarpet;
 						++numwall;
 					} else {
 						endcarpet = 0;
 						numwall = 0;
 						if (isCarpet(h,s,v)) {
 							++numcarpet;
 						} else {
 							++numcarpet;
 						}
 					}
 				} else { // seeking end of wall
 					if (isWhite(h,s,v)) {
 						++numwall;
 					} else {
 						if (++endwall >= 5) break;
 					}
 				}
 			}
 			for (int y = Math.min(r1.getHeight()-1, r1.getHeight()-1-(numcarpet)); y >= Math.max(0, r1.getHeight()-1-(numcarpet)-numwall); --y) {
 				r2.setSample(x, y, 0, 255);
 				r2.setSample(x, y, 1, 255);
 				r2.setSample(x, y, 2, 255);
 			}
 		}
 	}
 
 	public void walldist(WritableRaster r1, WritableRaster r2) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			int numcarpet = 0;
 			int numwall = 0;
 			int endcarpet = 0;
 			int endwall = 0;
 			for (int y = r1.getHeight()-1; y >= 0; --y) {
 				int h = r1.getSample(x, y, 0);
 				int s = r1.getSample(x, y, 1);
 				int v = r1.getSample(x, y, 2);
 				if (isBlue(h,s,v)) {
 					numwall += 10;
 					break;
 				}
 				if (endcarpet < 5) { // seeking end of carpet
 					if (isWhite(h,s,v)) {
 						++endcarpet;
 						++numwall;
 					} else {
 						endcarpet = 0;
 						numwall = 0;
 						if (isCarpet(h,s,v)) {
 							++numcarpet;
 						} else {
 							++numcarpet;
 						}
 					}
 				} else { // seeking end of wall
 					if (isWhite(h,s,v)) {
 						++numwall;
 					} else {
 						if (++endwall >= 5) break;
 					}
 				}
 			}
 			if (numwall == 0) continue;
 			for (int y = Math.min(r1.getHeight()-1, r1.getHeight()-1-(3000/numwall)); y >= Math.max(0, r1.getHeight()-1-(3000/numwall)-numwall/10); --y) {
 				r2.setSample(x, y, 0, 255);
 				r2.setSample(x, y, 1, 255);
 				r2.setSample(x, y, 2, 255);
 			}
 		}
 	}
 
 	public static void meanpass(int[] inp) {
 		int total = 0;
 		for (int x = 0; x < inp.length; ++x) {
 			total += inp[x];
 		}
 		total /= inp.length;
 		for (int x = 0; x < inp.length; ++x) {
 			if (4*inp[x] < 3*total)
 				inp[x] = total*3/4;
 			if (3*inp[x] > 4*total)
 				inp[x] = total*4/3;
 		}
 	}
 
 	public static void meanfilter2(int[] inp, int[] out) {
 		out[0] = inp[0];
 		out[1] = inp[1];
 		out[inp.length-1] = inp[inp.length-1];
 		out[inp.length-2] = inp[inp.length-2];
 		int tot = inp[0]+inp[1]+inp[2]+inp[3]+inp[4];
 		int numv = 0;
 		if (inp[0] != 0) ++numv;
 		if (inp[1] != 0) ++numv;
 		if (inp[2] != 0) ++numv;
 		if (inp[3] != 0) ++numv;
 		if (inp[4] != 0) ++numv;
 		if (numv == 0) out[2] = 0;
 		else out[2] = tot/numv;
 		for (int x = 3; x < inp.length-2; ++x) {
 			if (inp[x-3] != 0) {
 				tot -= inp[x-3];
 				--numv;
 			}
 			if (inp[x+2] != 0) {
 				tot += inp[x+2];
 				++numv;
 			}
 			if (numv == 0) out[x] = 0;
 			else out[x] = tot/numv;
 		}
 	}
 
 	public static void meanfilter(int[] inp, int[] out) {
 		out[0] = inp[0];
 		out[1] = inp[1];
 		out[inp.length-1] = inp[inp.length-1];
 		out[inp.length-2] = inp[inp.length-2];
 		for (int x = 2; x < inp.length-2; ++x) {
 			int numv = 0;
 			int tot = 0;
 			if (inp[x-2] != 0) {
 				tot += inp[x-2];
 				++numv;
 			}
 			if (inp[x-1] != 0) {
 				tot += inp[x-1];
 				++numv;
 			}
 			if (inp[x] != 0) {
 				tot += inp[x];
 				++numv;
 			}
 			if (inp[x+1] != 0) {
 				tot += inp[x+1];
 				++numv;
 			}
 			if (inp[x+2] != 0) {
 				tot += inp[x+2];
 				++numv;
 			}
 			if (numv == 0) out[x] = 0;
 			else out[x] = tot/numv;
 		}
 	}
 
 	public static void medianfilter(int[] inp, int[] out) {
 		out[0] = (inp[0]+inp[1])/2;
 		out[out.length-1] = (out[out.length-1]+out[out.length-2])/2;
 		for (int x = 1; x < out.length-2; ++x) {
 			int a = inp[x];
 			int b = inp[x-1];
 			int c = inp[x+1];
 			if (a < b) {
 				if (c < b) { // a,c < b
 					if (a < c) { // a < c < b
 						out[x] = c;
 					} else { // c < a < b
 						out[x] = a;
 					}
 				} else { // a < b < c
 					out[x] = b;
 				}
 			} else { // b < a
 				if ( c < a) { // b,c < a
 					if (b < c) { // b < c < a
 						out[x] = c;
 					} else { // c < b < a
 						out[x] = b;
 					}
 				} else { // b < a < c
 					out[x] = a;
 				}
 			}
 		}
 	}
 
 	public static void breakcomponents(WritableRaster rhsv, WritableRaster rh, WritableRaster rs, WritableRaster rv) {
 		for (int x = 0; x < rhsv.getWidth(); ++x) {
 			for (int y = 0; y < rhsv.getHeight(); ++y) {
 				int h = rhsv.getSample(x, y, 0);
 				int s = rhsv.getSample(x, y, 1);
 				int v = rhsv.getSample(x, y, 2);
 				rh.setSample(x, y, 0, h);
 				rh.setSample(x, y, 1, h);
 				rh.setSample(x, y, 2, h);
 				rs.setSample(x, y, 0, s);
 				rs.setSample(x, y, 1, s);
 				rs.setSample(x, y, 2, s);
 				rv.setSample(x, y, 0, v);
 				rv.setSample(x, y, 1, v);
 				rv.setSample(x, y, 2, v);
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
 				int s = 0;
 				int delta = max-min;
 				if (delta == 0) delta = 1;
 				if (max != 0) {
 					s = 255*delta/max;
 					if (max == r) {
 						h = (g-b)*85/(2*delta);
 						if (h < 0)
 							h += 256;
 					} else if (max == g) {
 						h = 85 + (b-r)*85/(2*delta);
 					} else { // max == b
 						h = 170 + (r-g)*85/(2*delta);
 					}
 				}
 				r2.setSample(x, y, 0, h); // h
 				//r2.setSample(x, y, 1, h);
 				//r2.setSample(x, y, 2, h);
 				r2.setSample(x, y, 1, s); // s
 				r2.setSample(x, y, 2, max); // v
 			}
 		}
 	}
 
 	public void blankTop(final WritableRaster r1, final int[] wtop) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			//int y = wtop[x]-1 < r1.getHeight()-1 ? wtop[x]-1 : r1.getHeight()-1; // ugly hack
 			int y = wtop[x]-1;
 			Colors c = Colors.None;
 			if (y >= 0 && (c = getColor(r1,x,y)) == Colors.Red || c == Colors.Yellow) {
 				--y;
 				while (y >= 0 && ((c = getColor(r1,x,y)) == Colors.Red || c == Colors.Yellow)) {
 					--y;
 				}
 				wtop[x] = y+1;
 			}
 			for (; y >= 0 && y < r1.getHeight(); --y) {
 				//System.err.println(y);
 				r1.setSample(x, y, 0, 0);
 				r1.setSample(x, y, 1, 0);
 				r1.setSample(x, y, 2, 0);
 			}
 		}
 	}
 
 	/*
 	public void setExtrema(final WritableRaster r1, final WritableRaster r2, final int x, final int y, final Extrema m) {
 		m.update(x, y);
 		r2.setSample(x, y, 0, 255);
 		if (isRed(r1,x+1,y) && r2.getSample(x+1, y, 0) != 255) {
 			setExtrema(r1,r2,x+1,y,m);
 		} if (isRed(r1,x-1,y) && r2.getSample(x-1, y, 0) != 255) {
 			setExtrema(r1,r2,x-1,y,m);
 		} if (isRed(r1,x,y+1) && r2.getSample(x, y+1, 0) != 255) {
 			setExtrema(r1,r2,x,y+1,m);
 		} if (isRed(r1,x,y-1) && r2.getSample(x, y-1, 0) != 255) {
 			setExtrema(r1,r2,x,y-1,m);
 		}
 	}
 	*/
 
 	public static boolean isBlank(WritableRaster r, int x, int y) {
 		return (r.getSample(x, y, 0) == 0 && r.getSample(x, y, 1) == 0 && r.getSample(x, y, 2) == 0);
 	}
 
 	public void setExtrema4(final WritableRaster r1, final WritableRaster r2, final int ox, final int oy, final Extrema m, final Colors c, final int[] qx, final int[] qy) {
 		int s = 0;
 		int e = 1;
 		qx[0] = ox;
 		qy[0] = oy;
 		m.update(ox, oy);
 		colorPix(r2,ox,oy,c);
 		if (c == Colors.Red) {
 			while (e != s) {
 				int x = qx[s];
 				int y = qy[s];
 				++s;
 				if (x+1 < r1.getWidth() && isBlank(r2,x+1,y) && isRed(r1,x+1,y)) {
 					m.update(x+1, y);
 					r2.setSample(x+1, y, 0, 255);
 					r2.setSample(x+1, y, 1, 0);
 					r2.setSample(x+1, y, 2, 0);
 					qx[e] = x+1;
 					qy[e] = y;
 					++e;
 				} if (x-1 >= 0 && isBlank(r2,x-1,y) && isRed(r1,x-1,y)) {
 					m.update(x-1, y);
 					r2.setSample(x-1, y, 0, 255);
 					r2.setSample(x-1, y, 1, 0);
 					r2.setSample(x-1, y, 2, 0);
 					qx[e] = x-1;
 					qy[e] = y;
 					++e;
 				} if (y+1 < r1.getHeight() && isBlank(r2,x,y+1) && isRed(r1,x,y+1)) {
 					m.update(x, y+1);
 					r2.setSample(x, y+1, 0, 255);
 					r2.setSample(x, y+1, 1, 0);
 					r2.setSample(x, y+1, 2, 0);
 					qx[e] = x;
 					qy[e] = y+1;
 					++e;
 				} if (y-1 >= 0 && isBlank(r2,x,y-1) && isRed(r1,x,y-1)) {
 					m.update(x, y-1);
 					r2.setSample(x, y-1, 0, 255);
 					r2.setSample(x, y-1, 1, 0);
 					r2.setSample(x, y-1, 2, 0);
 					qx[e] = x;
 					qy[e] = y-1;
 					++e;
 				}
 			}
 		} else {
 			while (e != s) {
 				int x = qx[s];
 				int y = qy[s];
 				++s;
 				if (x+1 < r1.getWidth() && isBlank(r2,x+1,y) && isYellow(r1,x+1,y)) {
 					m.update(x+1, y);
 					r2.setSample(x+1, y, 0, 255);
 					r2.setSample(x+1, y, 1, 255);
 					r2.setSample(x+1, y, 2, 0);
 					qx[e] = x+1;
 					qy[e] = y;
 					++e;
 				} if (x-1 >= 0 && isBlank(r2,x-1,y) && isYellow(r1,x-1,y)) {
 					m.update(x-1, y);
 					r2.setSample(x-1, y, 0, 255);
 					r2.setSample(x-1, y, 1, 255);
 					r2.setSample(x-1, y, 2, 0);
 					qx[e] = x-1;
 					qy[e] = y;
 					++e;
 				} if (y+1 < r1.getHeight() && isBlank(r2,x,y+1) && isYellow(r1,x,y+1)) {
 					m.update(x, y+1);
 					r2.setSample(x, y+1, 0, 255);
 					r2.setSample(x, y+1, 1, 255);
 					r2.setSample(x, y+1, 2, 0);
 					qx[e] = x;
 					qy[e] = y+1;
 					++e;
 				} if (y-1 >= 0 && isBlank(r2,x,y-1) && isYellow(r1,x,y-1)) {
 					m.update(x, y-1);
 					r2.setSample(x, y-1, 0, 255);
 					r2.setSample(x, y-1, 1, 255);
 					r2.setSample(x, y-1, 2, 0);
 					qx[e] = x;
 					qy[e] = y-1;
 					++e;
 				}
 			}
 		}
 	}
 
 	public void setExtrema3(final WritableRaster r1, final WritableRaster r2, final int ox, final int oy, final Extrema m, final Colors c) {
 		java.util.LinkedList<Integer> qx = new java.util.LinkedList<Integer>();
 		java.util.LinkedList<Integer> qy = new java.util.LinkedList<Integer>();
 		qx.add(ox);
 		qy.add(oy);
 		m.update(ox, oy);
 		colorPix(r2,ox,oy,c);
 		while (!qx.isEmpty()) {
 			int x = qx.pop();
 			int y = qy.pop();
 			if (getColor(r1,x+1,y) == c && isBlank(r2,x+1,y)) {
 				m.update(x+1, y);
 				colorPix(r2,x+1,y,c);
 				qx.add(x+1);
 				qy.add(y);
 			} if (getColor(r1,x-1,y) == c && isBlank(r2,x-1,y)) {
 				m.update(x-1, y);
 				colorPix(r2,x-1,y,c);
 				qx.add(x-1);
 				qy.add(y);
 			} if (getColor(r1,x,y+1) == c && isBlank(r2,x,y+1)) {
 				m.update(x, y+1);
 				colorPix(r2,x,y+1,c);
 				qx.add(x);
 				qy.add(y+1);
 			} if (getColor(r1,x,y-1) == c && isBlank(r2,x,y-1)) {
 				m.update(x, y-1);
 				colorPix(r2,x,y-1,c);
 				qx.add(x);
 				qy.add(y-1);
 			}
 		}
 	}
 
 	private void setExtrema2(final WritableRaster raster, final WritableRaster r2, final int x, final int y, final Extrema m, final Colors c) {
 			Rectangle bounds = raster.getBounds();
 			int fillL = x;
 			do {
 					m.update(fillL, y);
 					//r2.setSample(fillL, y, 0, 255);
 					colorPix(r2, fillL, y, c);
 					fillL--;
 			} while (fillL >= 0 && getColor(raster, fillL, y) == c && isBlank(r2, fillL, y));
 			fillL++;
 
 			// find the right right side, filling along the way
 			int fillR = x;
 			do {
 					m.update(fillR, y);
 					//r2.setSample(fillR, y, 0, 255);
 					colorPix(r2, fillL, y, c);
 					fillR++;
 			} while (fillR < bounds.width - 1 && getColor(raster, fillR, y) == c && isBlank(r2, fillR, y));
 			fillR--;
 
 			// checks if applicable up or down
 			for (int i = fillL; i <= fillR; i++) {
 					if (y > 0 && getColor(raster, i, y - 1) == c && isBlank(r2, i, y-1)) setExtrema2(raster, r2, i, y - 1, m, c);
 					if (y < bounds.height - 1 && getColor(raster, i, y + 1) == c && isBlank(r2, i, y+1)) setExtrema2(raster, r2, i, y + 1, m, c);
 			}
 	}
 
 	// Returns true if RGBA arrays are equivalent, false otherwise
 	// Could use Arrays.equals(int[], int[]), but this is probably a little faster...
 	private static boolean isEqualRgba(int[] pix1, int[] pix2) {
 			return pix1[0] == pix2[0] && pix1[1] == pix2[1] && pix1[2] == pix2[2] && pix1[3] == pix2[3];
 	}
 	
 
 	public static void drawline(final WritableRaster r, int x, int y, final int x2, final int y2) {
 		int w = x2 - x ;
 		int h = y2 - y ;
 		int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0 ;
 		if (w<0) dx1 = -1 ; else if (w>0) dx1 = 1 ;
 		if (h<0) dy1 = -1 ; else if (h>0) dy1 = 1 ;
 		if (w<0) dx2 = -1 ; else if (w>0) dx2 = 1 ;
 		int longest = Math.abs(w) ;
 		int shortest = Math.abs(h) ;
 		if (!(longest>shortest)) {
 			longest = Math.abs(h) ;
 			shortest = Math.abs(w) ;
 			if (h<0) dy2 = -1 ; else if (h>0) dy2 = 1 ;
 			dx2 = 0 ;
 		}
 		int numerator = longest >> 1 ;
 		for (int i=0;i<=longest;i++) {
 			r.setSample(x, y, 2, 255);
 			numerator += shortest ;
 		 if (!(numerator<longest)) {
 				numerator -= longest ;
 				x += dx1 ;
 			 y += dy1 ;
 			} else {
 				x += dx2 ;
 				y += dy2 ;
 			}
 		}
 	}
 
 	public static void drawline(final WritableRaster r, int x, int y, final int x2, final int y2, Colors c) {
 		int w = x2 - x ;
 		int h = y2 - y ;
 		int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0 ;
 		if (w<0) dx1 = -1 ; else if (w>0) dx1 = 1 ;
 		if (h<0) dy1 = -1 ; else if (h>0) dy1 = 1 ;
 		if (w<0) dx2 = -1 ; else if (w>0) dx2 = 1 ;
 		int longest = Math.abs(w) ;
 		int shortest = Math.abs(h) ;
 		if (!(longest>shortest)) {
 			longest = Math.abs(h) ;
 			shortest = Math.abs(w) ;
 			if (h<0) dy2 = -1 ; else if (h>0) dy2 = 1 ;
 			dx2 = 0 ;
 		}
 		int numerator = longest >> 1 ;
 		for (int i=0;i<=longest;i++) {
 			colorPix(r,x,y,c);
 			//r.setSample(x, y, 2, 255);
 			numerator += shortest ;
 		 if (!(numerator<longest)) {
 				numerator -= longest ;
 				x += dx1 ;
 			 y += dy1 ;
 			} else {
 				x += dx2 ;
 				y += dy2 ;
 			}
 		}
 	}
 
 	public static void countLine(final WritableRaster r, int x, int y, final int x2, final int y2, final int[] matchvnon, Colors c) {
 		int w = x2 - x ;
 		int h = y2 - y ;
 		int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0 ;
 		if (w<0) dx1 = -1 ; else if (w>0) dx1 = 1 ;
 		if (h<0) dy1 = -1 ; else if (h>0) dy1 = 1 ;
 		if (w<0) dx2 = -1 ; else if (w>0) dx2 = 1 ;
 		int longest = Math.abs(w) ;
 		int shortest = Math.abs(h) ;
 		if (!(longest>shortest)) {
 			longest = Math.abs(h) ;
 			shortest = Math.abs(w) ;
 			if (h<0) dy2 = -1 ; else if (h>0) dy2 = 1 ;
 			dx2 = 0 ;
 		}
 		int numerator = longest >> 1 ;
 		if (c == Colors.Red) {
 		for (int i=0;i<=longest;i++) {
 			//r.setSample(x, y, 2, 255);
 			if (r.getSample(x, y, 0) == 255 && r.getSample(x, y, 1) == 0 && r.getSample(x, y, 2) == 0) {
 				++matchvnon[0];
 			} else {
 				++matchvnon[1];
 			}
 			numerator += shortest ;
 		 if (!(numerator<longest)) {
 				numerator -= longest ;
 				x += dx1 ;
 			 y += dy1 ;
 			} else {
 				x += dx2 ;
 				y += dy2 ;
 			}
 		}
 		} else {
 		for (int i=0;i<=longest;i++) {
 			//r.setSample(x, y, 2, 255);
 			if (r.getSample(x, y, 0) == 255 && r.getSample(x, y, 1) == 255 && r.getSample(x, y, 2) == 0) {
 				++matchvnon[0];
 			} else {
 				++matchvnon[1];
 			}
 			numerator += shortest ;
 		 if (!(numerator<longest)) {
 				numerator -= longest ;
 				x += dx1 ;
 			 y += dy1 ;
 			} else {
 				x += dx2 ;
 				y += dy2 ;
 			}
 		}
 		}
 		/*
 		for (int i=0;i<=longest;i++) {
 			//r.setSample(x, y, 2, 255);
 			if (getColor(r,x,y) == c) {
 				++matchvnon[0];
 			} else {
 				++matchvnon[1];
 			}
 			numerator += shortest ;
 		 if (!(numerator<longest)) {
 				numerator -= longest ;
 				x += dx1 ;
 			 y += dy1 ;
 			} else {
 				x += dx2 ;
 				y += dy2 ;
 			}
 		}
 		*/
 	}
 
 	public static void shadeWalls(final WritableRaster r1, final int[] wtop, final int[] wbot) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			int y = r1.getHeight()-1;
 			for (; y >= wbot[x]; --y) {
 				colorPix(r1,x,y,Colors.None);
 			} for (; y >= wtop[x]; --y) {
 				colorPix(r1,x,y,Colors.White);
 			} for (; y >= 0; --y) {
 				colorPix(r1,x,y,Colors.None);
 			}
 		}
 	}
 
 	public void findBlueLine(final WritableRaster r1, final int[] wtop, final int[] wbot) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			int y = r1.getHeight()-1;
 			wtop[x] = y;
 			wbot[x] = y;
 			int maxblue = Integer.MIN_VALUE;
 			// first let's find some white (bottom of the wall)
 			for (; y >= 0; --y) {
 				if (getColor(r1,x,y) == Colors.White && getColor(r1,x,y-1) == Colors.White) {
 					wbot[x] = y;
 					break;
 				}
 			}
 			// now let's find the first blue
 			for (; y >= 0; --y) {
 				if (getColor(r1,x,y) == Colors.Blue) {
 					wtop[x] = y;
 					break;
 				}
 			}
 			// no blue found? return the lowest maximal blue
 			for (y = wbot[x]; y >= 0; --y) {
 				int blueness = (y/3)+3*r1.getSample(x, y, 2)-2*r1.getSample(x, y, 0)-2*r1.getSample(x, y, 1);
 				if (blueness > maxblue) {
 					maxblue = blueness;
 					wtop[x] = y;
 				}
 			}
 		}
 	}
 
 	public void findWalls(final WritableRaster r1, final int[] wtop, final int[] wbot) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			int y = r1.getHeight()-1;
 			wbot[x] = 0;
 			wtop[x] = 0;
 			int wallseg = 0;
 			int endseg = 0;
 			for (; y >= 0; --y) {
 				Colors c = getColor(r1,x,y);
 				if (wallseg < 4) { // haven't found wall yet
 					if (c == Colors.White) {
 						if (++wallseg == 4) {
 							wbot[x] = y+3;
 						}
 					} else {
 						wallseg = 0;
 					}
 				} else if (endseg < 2) { // searching for wall end
 					if (c == Colors.Blue || c == Colors.None) {
 						if (++endseg == 2) {
 							wtop[x] = y;
 							break;
 						}
 					} else {
 						endseg = 0;
 					}
 				}
 			}
 		}
 	}
 
 	public void findExtrema(final WritableRaster r1, final WritableRaster r2) {
 		Extrema m = new Extrema();
 		int[] matchvnon = new int[2];
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			int y = r1.getHeight()-1;
 			/*
 			int bld = 0;
 			for (; bld < 2 && y < r1.getWidth(); ++y) {
 				if (isBlue(r1,x,y)) {
 					++bld;
 					r2.setSample(x, y, 2, 255);
 				} else {
 					bld = 0;
 				}
 			}
 			*/
 			for (; y >= 0; --y) {
 				Colors c = getColor(r1,x,y);
 				if (c == Colors.Blue) break;
 				if (((c == Colors.Red) || (c == Colors.Yellow)) && isBlank(r2,x,y) &&
 					
 					(getColor(r1,x+1,y) == c) && isBlank(r2,x+1,y) &&
 					(getColor(r1,x-1,y) == c) && isBlank(r2,x-1,y) &&
 					(getColor(r1,x,y+1) == c) && isBlank(r2,x,y+1) &&
 					(getColor(r1,x,y-1) == c) && isBlank(r2,x,y-1) // &&
 					
 
 					// diagonals
 					/*
 					(getColor(r1,x+1,y+1) == c) && isBlank(r2,x+1,y+1) &&
 					(getColor(r1,x+1,y-1) == c) && isBlank(r2,x+1,y-1) &&
 					(getColor(r1,x-1,y+1) == c) && isBlank(r2,x-1,y+1) &&
 					(getColor(r1,x-1,y-1) == c) && isBlank(r2,x-1,y-1)
 					*/
 					) {
 					m.initval(x, y);
 					//r2.setSample(x, y, 2, 255);
 					//setExtrema3(r1, r2, x, y, m, c);
 					setExtrema4(r1, r2, x, y, m, c, queuex, queuey);
 					/*
 					r2.setSample(m.lbx, m.lby, 1, 255);
 					r2.setSample(m.rbx, m.rby, 1, 255);
 					r2.setSample(m.ltx, m.lty, 1, 255);
 					r2.setSample(m.rtx, m.rty, 1, 255);
 					*/
 					matchvnon[0] = matchvnon[1] = 0;
 					countLine(r2, m.lbx+(m.rbx-m.lbx)/4, m.lby+(m.rby-m.lby)/4, m.rbx-(m.rbx-m.lbx)/4, m.rby-(m.rby-m.lby)/4, matchvnon, c);
 					// drawline(r2, m.lbx+(m.rbx-m.lbx)/4, m.lby+(m.rby-m.lby)/4, m.rbx-(m.rbx-m.lbx)/4, m.rby-(m.rby-m.lby)/4);
 					//  drawline(r2, m.lbx, m.lby, m.rbx, m.rby);
 					countLine(r2, m.ltx+(m.rbx-m.ltx)/3, m.lty+(m.rby-m.lty)/3, m.rbx-(m.rbx-m.ltx)/3, m.rby-(m.rby-m.lty)/3, matchvnon, c);
 					// drawline(r2, m.ltx+(m.rbx-m.ltx)/3, m.lty+(m.rby-m.lty)/3, m.rbx-(m.rbx-m.ltx)/3, m.rby-(m.rby-m.lty)/3);
 					//  drawline(r2, m.ltx, m.lty, m.rbx, m.rby);
 					countLine(r2, m.rtx+(m.lbx-m.rtx)/3, m.rty+(m.lby-m.rty)/3, m.lbx-(m.lbx-m.rtx)/3, m.lby-(m.lby-m.rty)/3, matchvnon, c);
 					// drawline(r2, m.rtx+(m.lbx-m.rtx)/3, m.rty+(m.lby-m.rty)/3, m.lbx-(m.lbx-m.rtx)/3, m.lby-(m.lby-m.rty)/3);
 					//  drawline(r2, m.rtx, m.rty, m.lbx, m.lby);
 					if (matchvnon[0] > matchvnon[1] && m.ty != 0) {
 						int lbrb = (m.lbx-m.rbx)*(m.lbx-m.rbx)+(m.lby-m.rby)*(m.lby-m.rby); // bot-left to bot-right distance squared
 						int lbrt = (m.lbx-m.rtx)*(m.lbx-m.rtx)+(m.lby-m.rty)*(m.lby-m.rty); // bot-left to top-right distance squared
 						int ltrb = (m.ltx-m.rbx)*(m.ltx-m.rbx)+(m.lty-m.rby)*(m.lty-m.rby); // top-left to bot-right distance squared
 						int heufail = 0;
 						if (3*lbrb < lbrt || 3*lbrb < ltrb) { // likely actually a gate // doesn't seem to exactly work
 							System.out.println("fails diagonal-bottom heuristic at "+(+m.lx+m.rx)/2+","+(m.by+m.ty)/2);
 							++heufail;
 						} if (m.lbx > m.tx || m.tx > m.rbx) {
 							System.out.println("fails central-top heuristic at "+(+m.lx+m.rx)/2+","+(m.by+m.ty)/2);
 							++heufail;
 						} /*if (Math.abs(m.tx-m.lbx) > 3*(Math.abs(m.rbx-m.tx)) || 3*(Math.abs(m.tx-m.lbx)) < Math.abs(m.rbx-m.tx)) {
 							System.out.println("fails centered-center heuristic at "+(+m.lx+m.rx)/2+","+(m.by+m.ty)/2);
 							++heufail;
 						}*/ if (m.tx == 0 || m.tx == r2.getWidth()-1 || m.bx == 0 || m.bx == r2.getWidth()-1) {
 							System.out.println("fails top-bottom-xedge heuristic at "+(+m.lx+m.rx)/2+","+(m.by+m.ty)/2);
 							++heufail;
 						} if (m.lbx == 0 || m.ltx == 0 || m.rtx == r2.getWidth()-1 || m.rbx == r2.getWidth()-1) { // don't classify if the corner is off the page; likely gate
 							System.out.println("fails corner-edge heuristic at "+(+m.lx+m.rx)/2+","+(m.by+m.ty)/2);
 							++heufail;
 						} /*if (m.ty == 0) {
 							System.out.println("fails top heuristic at "+(+m.lx+m.rx)/2+","+(m.by+m.ty)/2);
 							++heufail;
 						}*/ if (heufail >= 1) {
 							System.out.println("unknown at "+(+m.lx+m.rx)/2+","+(m.by+m.ty)/2);
 							unknownFound(r2,m,c);
 						} else {
 							System.out.println("ball"+matchvnon[0]+" vs "+matchvnon[1]);
 							// TODO radius (intersection) of ball
 							
 							double radius = 0.0;
 							radius += Math.sqrt(((m.tx-m.bx)*(m.tx-m.bx))/4+((m.ty-m.by)*(m.ty-m.by))/4);
 							radius += Math.sqrt(((m.rx-m.lx)*(m.rx-m.lx))/4+((m.ry-m.ly)*(m.ry-m.ly))/4);
 							radius += Math.sqrt(((m.ltx-m.rbx)*(m.ltx-m.rbx))/4+((m.lty-m.rby)*(m.lty-m.rby))/4);
 							radius += Math.sqrt(((m.rtx-m.lbx)*(m.rtx-m.lbx))/4+((m.rty-m.lby)*(m.rty-m.lby))/4);
 							radius /= 4.0;
 							//double b2rb = Math.sqrt();
 							//circleFound(r2, m, c);
 							circleFound(r2, m, (m.rx+m.lx)/2, (m.ty+m.by)/2, (int)radius, c);
 							//System.out.println("circle found at "+ (m.rx+m.lx)/2+" "+(m.ty+m.by)/2);
 							// TODO confirm detection via standard deviation of 8-cardinals
 							/*
 							drawline(r2, m.lbx+(m.rbx-m.lbx)/4, m.lby+(m.rby-m.lby)/4, m.rbx-(m.rbx-m.lbx)/4, m.rby-(m.rby-m.lby)/4, Colors.Purple);
 							drawline(r2, m.ltx+(m.rbx-m.ltx)/3, m.lty+(m.rby-m.lty)/3, m.rbx-(m.rbx-m.ltx)/3, m.rby-(m.rby-m.lty)/3, Colors.Purple);
 							drawline(r2, m.rtx+(m.lbx-m.rtx)/3, m.rty+(m.lby-m.rty)/3, m.lbx-(m.lbx-m.rtx)/3, m.lby-(m.lby-m.rty)/3, Colors.Purple);
 							*/
 						}
 					} else {
 						System.out.println("gate"+matchvnon[0]+" vs "+matchvnon[1]);
 						/*
 						drawline(r2, m.lbx+(m.rbx-m.lbx)/4, m.lby+(m.rby-m.lby)/4, m.rbx-(m.rbx-m.lbx)/4, m.rby-(m.rby-m.lby)/4, Colors.Green);
 						drawline(r2, m.ltx+(m.rbx-m.ltx)/3, m.lty+(m.rby-m.lty)/3, m.rbx-(m.rbx-m.ltx)/3, m.rby-(m.rby-m.lty)/3, Colors.Green);
 						drawline(r2, m.rtx+(m.lbx-m.rtx)/3, m.rty+(m.lby-m.rty)/3, m.lbx-(m.lbx-m.rtx)/3, m.lby-(m.lby-m.rty)/3, Colors.Green);
 						*/
 						gateFound(r2, m, c);
 					}
 				}
 			}
 		}
 	}
 
 	public void unknownFound(WritableRaster r1, Extrema m, Colors c) {
 		if (c == Colors.Red)
 			filledRectange(r1, m.ty, m.by, m.lx, m.rx, Colors.Brown);
 		else
 			filledRectange(r1, m.ty, m.by, m.lx, m.rx, Colors.Teal);
 		if (m.rx-m.lx > unknownwidth) {
 		unknownwidth = m.rx-m.lx;
 		unknownseen = true;
 		unknownpxoffset = (m.rx+m.lx)/2-r1.getWidth()/2;
 		}
 		/*if ((m.rx+m.lx) > r1.getWidth()) { // towards the right half
 			setState(8);
 		} else {
 			setState(7);
 		}*/
 		//unknownpxoffset = (m.rx+m.lx)/2-r1.getWidth()/2;
 		/*
 		int r = (m.rx-m.lx)/2;
 		int ndistance = 600/r;
 		int npxoffset = (m.rx+m.lx)/2-r1.getWidth()/2;
 		System.out.println("distance is "+ndistance+"cm offcenter is "+npxoffset+"px");
 		found = 3;
 		if (distance > ndistance || lifetime == 0) {
 			lifetime = 1;
 			distance = ndistance;
 			pxoffset = npxoffset;
 		}
 		*/
 	}
 
 	public void gateFound(WritableRaster r, Extrema m, Colors c) {
 		/*
 		double ld = Math.sqrt((m.lbx-m.ltx)*(m.lbx-m.ltx)+(m.lby-m.lty)*(m.lby-m.lty)); // left distance
 		double rd = Math.sqrt((m.rbx-m.rtx)*(m.rbx-m.rtx)+(m.rby-m.rty)*(m.rby-m.rty)); // right distance
 		*/
 		//System.out.println("average dist is "+(ld+rd)/2.0);
 		if (c == Colors.Red)
 			filledRectange(r, m.ty, m.by, m.lx, m.rx, Colors.Purple);
 		else
 			filledRectange(r, m.ty, m.by, m.lx, m.rx, Colors.Green);
 		if (m.by-m.ty < 40 && m.rbx - m.lbx < 40) return;
 		gatepxoffset = (m.rbx + m.lbx)/2-r.getWidth()/2;
 		gatewidth = (m.rbx - m.lbx);
 		gateheight = (m.by-m.ty);
 		System.out.println("gate width is "+gatewidth);
 		gateseen = true;
 		/*
 		int top = (m.ltx < m.rtx) ? m.ltx : m.rtx;
 		int bottom = (m.lbx > m.rbx) ? m.lbx : m.rbx;
 		float slopel = (float)(m.lby-m.lty)/(float)(m.lbx-m.ltx);
 		float sloper = (float)(m.lby-m.lty)/(float)(m.lbx-m.ltx);
 		for (int y = top; y <= bottom; ++y) {
 			int startx;
 			
 			int stopx;
 
 		}
 		*/
 	}
 
 	public static int bound(int v, int max, int min) {
 		if (v > max) return max;
 		else if (v < min) return min;
 		else return v;
 	}
 
 	public static float bound(float v, float max, float min) {
 		if (v > max) return max;
 		else if (v < min) return min;
 		else return v;
 	}
 
 	public static void blankimg(WritableRaster r) {
 		for (int x = 0; x < r.getWidth(); ++x) {
 			for (int y = 0; y < r.getHeight(); ++y) {
 				r.setSample(x, y, 0, 0);
 				r.setSample(x, y, 1, 0);
 				r.setSample(x, y, 2, 0);
 			}
 		}
 	}
 
 	public static void safeColorPix(WritableRaster r1, int x, int y, Colors c) {
 		if (x < 0 || y < 0 || x >= r1.getWidth() || y >= r1.getHeight()) return;
 		colorPix(r1,x,y,c);
 	}
 
 	public static void colorPix(WritableRaster r1, int x, int y, Colors c) {
 		if (c == Colors.Carpet) {
 			r1.setSample(x, y, 0, 100);
 			r1.setSample(x, y, 1, 150);
 			r1.setSample(x, y, 2, 150);
 		} else if (c == Colors.Red) {
 			r1.setSample(x, y, 0, 255);
 			r1.setSample(x, y, 1, 0);
 			r1.setSample(x, y, 2, 0);
 		} else if (c == Colors.Blue) {
 			r1.setSample(x, y, 0, 0);
 			r1.setSample(x, y, 1, 0);
 			r1.setSample(x, y, 2, 255);
 		} else if (c == Colors.Yellow) {
 			r1.setSample(x, y, 0, 255);
 			r1.setSample(x, y, 1, 255);
 			r1.setSample(x, y, 2, 0);
 		} else if (c == Colors.Green) {
 			r1.setSample(x, y, 0, 0);
 			r1.setSample(x, y, 1, 255);
 			r1.setSample(x, y, 2, 0);
 		} else if (c == Colors.Purple) {
 			r1.setSample(x, y, 0, 128);
 			r1.setSample(x, y, 1, 0);
 			r1.setSample(x, y, 2, 128);
 		} else if (c == Colors.Teal) {
 			r1.setSample(x, y, 0, 130);
 			r1.setSample(x, y, 1, 240);
 			r1.setSample(x, y, 2, 240);
 		} else if (c == Colors.Brown) {
 			r1.setSample(x, y, 0, 160);
 			r1.setSample(x, y, 1, 100);
 			r1.setSample(x, y, 2, 0);
 		} else if (c == Colors.White) {
 			r1.setSample(x, y, 0, 255);
 			r1.setSample(x, y, 1, 255);
 			r1.setSample(x, y, 2, 255);
 		} else {
 			r1.setSample(x, y, 0, 0);
 			r1.setSample(x, y, 1, 0);
 			r1.setSample(x, y, 2, 0);
 		}
 	}
 
 	public void shadeColors(WritableRaster r1, WritableRaster r2) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			boolean foundblue = false;
 			for (int y = r1.getHeight()-1; y >= 0; --y) {
 				//if (foundblue) {
 					colorPix(r2,x,y,Colors.None);
 				//} else {
 					Colors curcolor = getColor(r1,x,y);
 					colorPix(r2,x,y,curcolor);
 					if (curcolor == Colors.Blue) foundblue = true;
 				//}
 			}
 		}
 	}
 
 	/*
 	public void shadeRed(WritableRaster r1, WritableRaster r2) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 				if (isRed(r1,x,y)) {
 					r2.setSample(x, y, 0, 255);
 					r2.setSample(x, y, 1, 255);
 					r2.setSample(x, y, 2, 255);
 				} else {
 					r2.setSample(x, y, 0, 0);
 					r2.setSample(x, y, 1, 0);
 					r2.setSample(x, y, 2, 0);
 				}
 			}
 		}
 	}
 	*/
 
 	/*
 	public void seekStart2(WritableRaster r1, WritableRaster r2) {
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			int y = 0;
 			int ey = 0;
 			while (ey < r2.getHeight()) {
 				if (isRed(r1,x,ey)) {
 					while (isRed(r1,x,ey) && ey < r2.getHeight()) ++ey;
 					y += ey;
 					y /= 2;
 					if (!isRed(r1,x-1,y)) {
 						if (!isRed(r2,x,y) && !isRed(r2,x+1,y) && !isRed(r2,x+2,y) && !isRed(r2,x+3,y))
 							circleDetectRightFull(r1,r2,x,y);
 					}
 					if (!isRed(r1,x+1,y)) {
 						if (!isRed(r2,x,y) && !isRed(r2,x-1,y) && !isRed(r2,x-2,y) && !isRed(r2,x-3,y))
 							circleDetectLeftFull(r1,r2,x,y);
 					}
 				}
 				y = ++ey;
 			}
 		}
 	}
 	 */
 
 	/*
 	public boolean isRed(WritableRaster r1, int x, int y) {
 		if (x >= 0 && y >= 0 && x < r1.getWidth() && y < r1.getHeight()) {
 			int r = r1.getSample(x, y, 0);
 			int g = r1.getSample(x, y, 1);
 			int b = r1.getSample(x, y, 2);
 			//if (b < 150 && r > 2*b && g > 2*b) return true; // yellow
 			//if (r > 90 && 2*(g+b) < 3*r) return true;
 			//if (r > 110 && 3*(g+b) < 4*r) return true; // 26-100
 			if (color == 0) { // red
 				if (r > 110 && 5*(g+b) < 6*r) return true;
 			} else if (color == 1) { // yellow
 				if (b < 150 && r > 2*b && g > 2*b) return true; // yellow
 			} else if (color == 2) { // blue
 				
 			}
 		} return false;
 	}
 	*/
 
 	public static void convolve(WritableRaster r1, WritableRaster r2, int[][] m, int w) {
 		//int[] rgbf = new int[r1.getNumBands()];
 		int rgbf = 0;
 		int[] rgbft = new int[r1.getNumBands()];
 		for (int x = 0; x < r1.getWidth(); ++x) {
 			for (int y = 0; y < r1.getHeight(); ++y) {
 				//for (int i = 0; i < rgbf.length; ++i)
 				//	rgbf[i] = 0;
 				rgbf = 0;
 				for (int my = 0; my < m.length; ++my) {
 					if (y+my-m.length/2 < 0 || y+my-m.length/2 >= r1.getHeight()) continue;
 					for (int mx = 0; mx < m[my].length; ++mx) {
 						if (x+mx-m[my].length/2 < 0 || x+mx-m[my].length/2 >= r1.getWidth()) continue;
 						r1.getPixel(x+mx-m[my].length/2, y+my-m.length/2, rgbft);
 						//System.out.println("("+rgbft[0]+","+rgbft[1]+","+rgbft[2]+")");
 						//for (int i = 0; i < rgbf.length; ++i)
 						//	rgbf[i] += rgbft[i]*m[my][mx];
 						rgbf += rgbft[0]*m[my][mx] + rgbft[1]*m[my][mx] + rgbft[2]*m[my][mx];
 					}
 				}
 				rgbf /= w;
 				//for (int i = 0; i < rgbf.length; ++i)
 				//	rgbf[i] /= w;
 				//System.out.println("("+rgbf[0]+","+rgbf[1]+","+rgbf[2]+")");
 				//r2.setPixel(x, y, rgbf);
 				r2.setSample(x, y, 0, rgbf);
 				r2.setSample(x, y, 1, rgbf);
 				r2.setSample(x, y, 2, rgbf);
 			}
 		}
 	}
 
 	public static Colors getColorHsv(int r, int g, int b) {
 		if (isRedHsv(r,g,b)) return Colors.Red;
 		else if (isYellowHsv(r,g,b)) return Colors.Yellow;
 		else if (isBlueHsv(r,g,b)) return Colors.Blue;
 		//else if (r > 190 && g > 190 && b > 170) return Colors.White;
 		else if (isWhiteHsv(r,g,b)) return Colors.White;
 		else if (isCarpetHsv(r,g,b)) return Colors.Carpet;
 		else return Colors.None;
 	}
 
 	public static Colors getColor(int r, int g, int b) {
 		return CON.cmap[CON.ctable[r*65536+g*256+b]];
 		/*
 		if (isRed(r,g,b)) return Colors.Red;
 		else if (isYellow(r,g,b)) return Colors.Yellow;
 		else if (isBlue(r,g,b)) return Colors.Blue;
 		//else if (r > 190 && g > 190 && b > 170) return Colors.White;
 		else if (isWhite(r,g,b)) return Colors.White;
 		else if (isCarpet(r,g,b)) return Colors.Carpet;
 		else return Colors.None;
 		*/
 	}
 
 	public static Colors getColor(WritableRaster r1, int x, int y) {
 		if (x >= 0 && y >= 0 && x < r1.getWidth() && y < r1.getHeight()) {
 			int r = r1.getSample(x, y, 0);
 			int g = r1.getSample(x, y, 1);
 			int b = r1.getSample(x, y, 2);
 			return CON.cmap[CON.ctable[r*65536+g*256+b]];
 			/*
 			//if (r > 110 && 5*(g+b) < 6*r) return Colors.Red;
 			if (isRed(r,g,b)) return Colors.Red;
 			else if (isYellow(r,g,b)) return Colors.Yellow;
 			else if (isBlue(r,g,b)) return Colors.Blue;
 			//else if (r > 190 && g > 190 && b > 170) return Colors.White;
 			else if (isWhite(r,g,b)) return Colors.White;
 			else if (isCarpet(r,g,b)) return Colors.Carpet;
 			*/
 		} return Colors.None;
 	}
 
 	public static boolean isCarpet(final WritableRaster r1, final int x, final int y) {
 		return isCarpet(r1.getSample(x, y, 0),r1.getSample(x, y, 1),r1.getSample(x, y, 2));
 	}
 
 	public static boolean isRed(final WritableRaster r1, final int x, final int y) {
 		return isRed(r1.getSample(x, y, 0),r1.getSample(x, y, 1),r1.getSample(x, y, 2));
 	}
 
 	public static boolean isYellow(final WritableRaster r1, final int x, final int y) {
 		return isYellow(r1.getSample(x, y, 0),r1.getSample(x, y, 1),r1.getSample(x, y, 2));
 	}
 
 	public static boolean isBlue(final WritableRaster r1, final int x, final int y) {
 		return isBlue(r1.getSample(x, y, 0),r1.getSample(x, y, 1),r1.getSample(x, y, 2));
 	}
 
 	public static boolean isWhite(final WritableRaster r1, final int x, final int y) {
 		return isWhite(r1.getSample(x, y, 0),r1.getSample(x, y, 1),r1.getSample(x, y, 2));
 	}
 
 	public static boolean isCarpet(final int r, final int g, final int b) {
 		return (CON.ctable[r*65536+g*256+b] == Colors.Carpet.ordinal());
 	}
 
 	public static boolean isRed(final int r, final int g, final int b) {
 		return (CON.ctable[r*65536+g*256+b] == Colors.Red.ordinal());
 	}
 
 	public static boolean isYellow(final int r, final int g, final int b) {
 		return (CON.ctable[r*65536+g*256+b] == Colors.Yellow.ordinal());
 	}
 
 	public static boolean isBlue(final int r, final int g, final int b) {
 		return (CON.ctable[r*65536+g*256+b] == Colors.Blue.ordinal());
 	}
 
 	public static boolean isWhite(final int r, final int g, final int b) {
 		return (CON.ctable[r*65536+g*256+b] == Colors.White.ordinal());
 	}
 
 	public static boolean isCarpetHsv(final int h, final int s, final int v) {
 		//return false;
 		return (50 <= h && h <= 200) && (s <= 50) && (v <= 220);
 	}
 
 
 	public static boolean isRedHsv(final int h, final int s, final int v) {
 		// 0
 		return (220 <= h || h <= 7) && (77 <= s && s <= 170) && (64 <= v && v <= 220);
 	}
 
 	public static boolean isYellowHsv(final int h, final int s, final int v) {
 		// 43
 		return (8 <= h && h <= 90) && (100 <= s) & (170 <= v);
 	}
 
 	public static boolean isBlueHsv(final int h, final int s, final int v) {
 		// 170
 		return (91 <= h && h <= 219) && (90 <= s) && (100 <= v);
 	}
 
 	public static boolean isWhiteHsv(final int h, final int s, final int v) {
 		return (h <= 60 || 240 <= h) && (s <= 80) && (140 <= v);
 	}
 
 	/*
 	public static boolean isCarpetHsv(final int h, final int s, final int v) {
 		//return false;
 		return (50 <= h && h <= 200) && (s <= 50) && (v <= 220);
 	}
 
 	
 	public static boolean isRedHsv(final int h, final int s, final int v) {
 		// 0
 		return (220 <= h || h <= 7) && (77 <= s && s <= 170) && (64 <= v && v <= 220);
 	}
 
 	public static boolean isYellowHsv(final int h, final int s, final int v) {
 		// 43
 		return (8 <= h && h <= 90) && (100 <= s) & (170 <= v);
 	}
 
 	public static boolean isBlueHsv(final int h, final int s, final int v) {
 		// 170
 		return (91 <= h && h <= 219) && (90 <= s) && (100 <= v);
 	}
 
 	public static boolean isWhiteHsv(final int h, final int s, final int v) {
 		return (h <= 60 || 240 <= h) && (s <= 80) && (140 <= v);
 	}
 	*/
 
 	/* RGB
 
 	public static boolean isRed(final int r, final int g, final int b) {
 		return (r > 110 && 2*r > 3*b && 2*r > 3*g);
 	}
 
 	public static boolean isYellow(final int r, final int g, final int b) {
 		//return (b < 150 && 2*r > 3*b && 2*g > 3*b);
 		return (b < 190 && 2*r > 3*b && 2*g > 3*b);
 	}
 
 	public static boolean isBlue(final int r, final int g, final int b) {
 		return (b > 80 && 5*b > 6*r && 5*b > 6*g);
 	}
 
 	public static boolean isWhite(final int r, final int g, final int b) {
 		//return (r+g+b > 570);
 		return (r+g+b > 650); // 26-100
 	}
 	*/
 
 	/*
 	public static boolean isBlue(WritableRaster r1, int x, int y) {
 		if (x >= 0 && y >= 0 && x < r1.getWidth() && y < r1.getHeight()) {
 			int r = r1.getSample(x, y, 0);
 			int g = r1.getSample(x, y, 1);
 			int b = r1.getSample(x, y, 2);
 			//if (b < 150 && r > 2*b && g > 2*b) return true;
 			if (b > 110 && 2*(r+g) < 3*b) return true;
 			//if (b > 110 && 3*(r+g) < 4*b) return true;
 		} return false;
 	}
 	*/
 
 	/*
 	public void circleDetectRightFull(WritableRaster r1, WritableRaster r2, int startx, int starty) {
 		int diam = 0;
 		while (isRed(r1,startx+diam,starty)) ++diam;
 		if (diam/2 == 0) return;
 		float[] rvals = new float[diam];
 		int h = 0;
 		for (int x = startx; x < startx+diam/2; ++x) {
 			for (int y = starty; y < r1.getHeight(); ++y) {
 				if (!isRed(r1,x,y)) {
 					int c = 2*(y-starty);
 					++h;
 					rvals[x-startx] = (c*c+4.0f*h*h)/(8.0f*h);
 					break;
 				}
 			}
 		}
 		h = (diam+1)/2+1;
 		for (int x = startx+diam/2; x < startx+diam; ++x) {
 			for (int y = starty; y < r1.getHeight(); ++y) {
 				if (!isRed(r1,x,y)) {
 					int c = 2*(y-starty);
 					--h;
 					rvals[x-startx] = (c*c+4.0f*h*h)/(8.0f*h);
 					break;
 				}
 			}
 		}
 		//printList(rvals);
 		Arrays.sort(rvals);
 		//printList(rvals);
 		float lrt = median(rvals);
 		for (int x = 0; x < diam; ++x)
 			rvals[x] = Math.abs(lrt-rvals[x]);
 		Arrays.sort(rvals);
 		float ldevt = median(rvals);
 		ldevt += 32.0f*Math.abs((float)lrt-(float)diam/2.0f)/(float)diam;
 		//System.out.println("lrt is "+lrt+" ldevt is "+ldevt);
 		h = 0;
 		for (int x = startx; x < startx+diam/2; ++x) {
 			for (int y = starty-1; y >= 0; --y) {
 				if (!isRed(r1,x,y)) {
 					int c = 2*(starty-y);
 					++h;
 					rvals[x-startx] = (c*c+4.0f*h*h)/(8.0f*h);
 					break;
 				}
 			}
 		}
 		h = (diam+1)/2+1;
 		for (int x = startx+diam/2; x < startx+diam; ++x) {
 			for (int y = starty-1; y >= 0; --y) {
 				if (!isRed(r1,x,y)) {
 					int c = 2*(starty-y);
 					--h;
 					rvals[x-startx] = (c*c+4.0f*h*h)/(8.0f*h);
 					break;
 				}
 			}
 		}
 		Arrays.sort(rvals);
 		float lrb = median(rvals);
 		for (int x = 0; x < diam; ++x)
 			rvals[x] = Math.abs(lrb-rvals[x]);
 		Arrays.sort(rvals);
 		float ldevb = median(rvals);
 		ldevb += 32.0f*Math.abs((float)lrb-(float)diam/2.0f)/(float)diam;
 		//if (stoptop < 4 || stoptop*2 < stopbot) ldevt = Float.MAX_VALUE;
 		//if (stopbot < 4 || stopbot*2 < stoptop) ldevb = Float.MAX_VALUE;
 		//System.out.println("lrb is "+lrb+" ldevb is "+ldevb);
 		if (ldevt < ldevb) {
 			if (ldevt < 0.15f*(float)diam && lrt > 3.0f) {
 				circleFound(r2,(int)(startx+Math.ceil(lrt)),starty,(int)(Math.ceil(lrt)));
 			}
 		} else {
 			if (ldevb < 0.15f*(float)diam && lrb > 3.0f) {
 				circleFound(r2,(int)(startx+Math.ceil(lrb)),starty,(int)(Math.ceil(lrb)));
 			}
 		}
 	}
 
 	public void circleDetectLeftFull(WritableRaster r1, WritableRaster r2, int startx, int starty) {
 		int diam = 0;
 		while (isRed(r1,startx-diam,starty)) ++diam;
 		if (diam/2 == 0) return;
 		float[] rvals = new float[diam];
 		int h = 0;
 		for (int x = startx; x > startx-diam/2; --x) {
 			for (int y = starty; y < r1.getHeight(); ++y) {
 				if (!isRed(r1,x,y)) {
 					int c = 2*(y-starty);
 					++h;
 					rvals[startx-x] = (c*c+4.0f*h*h)/(8.0f*h);
 					break;
 				}
 			}
 		}
 		h = (diam+1)/2+1;
 		for (int x = startx-diam/2; x > startx-diam; --x) {
 			for (int y = starty; y < r1.getHeight(); ++y) {
 				if (!isRed(r1,x,y)) {
 					int c = 2*(y-starty);
 					--h;
 					rvals[startx-x] = (c*c+4.0f*h*h)/(8.0f*h);
 					break;
 				}
 			}
 		}
 		//printList(rvals);
 		Arrays.sort(rvals);
 		//printList(rvals);
 		float lrt = median(rvals);
 		for (int x = 0; x < diam; ++x)
 			rvals[x] = Math.abs(lrt-rvals[x]);
 		Arrays.sort(rvals);
 		float ldevt = median(rvals);
 		ldevt += 32.0f*Math.abs((float)lrt-(float)diam/2.0f)/(float)diam;
 		//System.out.println("lrt is "+lrt+" ldevt is "+ldevt);
 		h = 0;
 		for (int x = startx; x > startx-diam/2; --x) {
 			for (int y = starty-1; y >= 0; --y) {
 				if (!isRed(r1,x,y)) {
 					int c = 2*(starty-y);
 					++h;
 					rvals[startx-x] = (c*c+4.0f*h*h)/(8.0f*h);
 					break;
 				}
 			}
 		}
 		h = (diam+1)/2+1;
 		for (int x = startx-diam/2; x > startx-diam; --x) {
 			for (int y = starty-1; y >= 0; --y) {
 				if (!isRed(r1,x,y)) {
 					int c = 2*(starty-y);
 					--h;
 					rvals[startx-x] = (c*c+4.0f*h*h)/(8.0f*h);
 					break;
 				}
 			}
 		}
 		Arrays.sort(rvals);
 		float lrb = median(rvals);
 		for (int x = 0; x < diam; ++x)
 			rvals[x] = Math.abs(lrb-rvals[x]);
 		Arrays.sort(rvals);
 		float ldevb = median(rvals);
 		ldevb += 32.0f*Math.abs((float)lrb-(float)diam/2.0f)/(float)diam;
 		//if (stoptop < 4 || stoptop*2 < stopbot) ldevt = Float.MAX_VALUE;
 		//if (stopbot < 4 || stopbot*2 < stoptop) ldevb = Float.MAX_VALUE;
 		//System.out.println("lrb is "+lrb+" ldevb is "+ldevb);
 		if (ldevt < ldevb) {
 			if (ldevt < 0.15f*(float)diam && lrt > 3.0f) {
 				circleFound(r2,(int)(startx-Math.ceil(lrt)),starty,(int)(Math.ceil(lrt)));
 			}
 		} else {
 			if (ldevb < 0.15f*(float)diam && lrb > 3.0f) {
 				circleFound(r2,(int)(startx-Math.ceil(lrb)),starty,(int)(Math.ceil(lrb)));
 			}
 		}
 	}
 	*/
 
 	public void circleFound(WritableRaster r1, Extrema m, Colors c) {
 		filledRectange(r1, m.ty, m.by, m.lx, m.rx, c);
 	}
 
 	public void circleFound(WritableRaster r1, Extrema m, int x, int y, int r, Colors c) {
 		if (r == 0) r = 1; // ugly hack
 		if (x >= 0 && x < r1.getWidth() && y >= 0 && y < r1.getHeight()) {
 			//filledCircle(r1,x,y,r, c);
 			//filledRectange(r1, y-r, y+r, x-r, x+r, c);
 			filledRectange(r1, m.ty, m.by, m.lx, m.rx, c);
 			//r1.setSample(x, y, 2, 255);
 			//System.out.println(r);
 			int ndistance = 600/r;
 			int npxoffset = x-r1.getWidth()/2;
 			System.out.println("distance is "+ndistance+"cm offcenter is "+npxoffset+"px");
 			found = 3;
 			//if (distance > ndistance || lifetime == 0) {
 			if (odom != null) {
 				odom.markPoint(ndistance*100.0, npxoffset*50.0, r/3, c);
 			}
 			if (r > circleradius) {
 				circleradius = r;
 				lifetime = 1;
 				distance = ndistance;
 				pxoffset = npxoffset;
 				circleseen = true;
 				circlecentery = y;
 			}
 		}
 	}
 
 	public static void filledCircle(WritableRaster r1, int x0, int y0, int r, Colors c) {
 		int xe = Math.min(r1.getWidth(), x0+r+1);
 		int ye = Math.min(r1.getHeight(), y0+r+1);
 		for (int x = Math.max(0, x0-r); x < xe; ++x) {
 			int xq = (x0-x)*(x0-x);
 			for (int y = Math.max(0, y0-r); y < ye; ++y) {
 				if (xq+(y0-y)*(y0-y) <= r*r)
 					colorPix(r1,x,y,c);
 					//r1.setSample(x, y, 0, 255);
 			}
 		}
 	}
 
 	public static void filledRectange(WritableRaster r1, int top, int bot, int left, int right, Colors c) {
 		
 		for (int x = left; x <= right; ++x) {
 			for (int y = top; y <= bot; ++y) {
 				colorPix(r1,x,y,c);
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
 
 	public static float median (float[] arr, int end) {
 		if (end == 0) return 0.0f;
 		if (end % 2 == 0) {
 			return (arr[end/2-1] + arr[end/2])/2;
 		} else {
 			return arr[end/2];
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
 
 	public static void printList(float[] c) {
 		if (c.length == 0) return;
 		System.out.print("[ ");
 		for (int x = 0; x < c.length-1; ++x) {
 			System.out.print(c[x]+", ");
 		}
 		System.out.println(c[c.length-1]+" ]");
 	}
 }
