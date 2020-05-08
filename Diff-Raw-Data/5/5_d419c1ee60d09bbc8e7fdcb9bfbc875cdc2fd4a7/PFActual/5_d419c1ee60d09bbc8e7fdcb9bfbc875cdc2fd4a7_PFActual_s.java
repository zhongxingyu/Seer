 /*
  *  Copyright (C) 2012 The Animo Project
  *  http://animotron.org
  *
  *  This file is part of Animi.
  *
  *  Animotron is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as
  *  published by the Free Software Foundation, either version 3 of
  *  the License, or (at your option) any later version.
  *
  *  Animotron is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of
  *  the GNU Affero General Public License along with Animotron.
  *  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.animotron.animi.gui;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.event.InternalFrameEvent;
 import javax.swing.event.InternalFrameListener;
 
 import org.animotron.animi.*;
 import org.animotron.animi.cortex.*;
 
 /**
  * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
  *
  */
 public class PFActual implements Imageable, InternalFrameListener {
 
 	static List<Field> cnFds = new ArrayList<Field>();
 	static List<Field> snFds = new ArrayList<Field>();
 
 	static {
 		Field[] fields = NeuronComplex.class.getFields();
 		for (int i = 0; i < fields.length; i++) {
 			Field field = fields[i];
 			if (field.isAnnotationPresent(RuntimeParam.class))
 				cnFds.add(field);
 		}
 
 		fields = NeuronSimple.class.getFields();
 		for (int i = 0; i < fields.length; i++) {
 			Field field = fields[i];
 			if (field.isAnnotationPresent(RuntimeParam.class))
 				snFds.add(field);
 		}
 	}
 	
 	CortexZoneComplex zone;
 	Point point;
 	NeuronComplex cn;
 	
 	public PFActual(Object[] objs) {
 		zone = (CortexZoneComplex) objs[0];
 		point = (Point) objs[1];
 		cn = zone.col[point.x][point.y];
 	}
 
 	@Override
 	public String getImageName() {
 		return "running params";
 	}
 
 	@Override
 	public BufferedImage getImage() {
 		BufferedImage image = new BufferedImage(400, 600, BufferedImage.TYPE_INT_RGB);
         Graphics g = image.getGraphics();
         g.setColor(Color.WHITE);
 
 		int textY = g.getFontMetrics(g.getFont()).getHeight();
 		int x = 0, y = 0;
 		
 		for (Field f : cnFds) {
 			y += textY;
 	        g.drawString(getName(f), x, y);		
 
 	        y += textY;
 	        g.drawString(getValue(f, cn), x, y);		
 		}
 		
 		for (Field f : snFds) {
 			y += textY;
 	        x = 0;
 			for (int z = 0; z < zone.deep; z++) {
 				final NeuronSimple sn = zone.s[point.x][point.y][z];
 				
 				String str = getValue(f, sn);
 				if (str.length() > 3)
 					str = str.substring(0, 3);
 				
 		        g.drawString(str, x, y);		
 				x += 35;
 			}
 	        g.drawString(getName(f), x, y);		
 		}
 		x = 0;
 		y += textY;
 		BufferedImage img = drawRF();
		g.drawRect(x, y, x+2+(img.getWidth()*10), y+2+(img.getHeight()*10));
 		g.drawImage(
 				img.getScaledInstance(img.getWidth()*10, img.getHeight()*10, Image.SCALE_AREA_AVERAGING),
 				x+1, y+1, null);
 
 		x = 0;
 		y += 2+img.getHeight()*10;
 
 		img = drawTotalRF();
		g.drawRect(x, y, x+2+(img.getWidth()*10), y+2+(img.getHeight()*10));
 		g.drawImage(
 				img.getScaledInstance(img.getWidth()*10, img.getHeight()*10, Image.SCALE_AREA_AVERAGING),
 				x+1, y+1, null);
 
 		return image;
 	}
 
 	private BufferedImage drawRF() {
 		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
 		int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
 
         for (Link cl : cn.s_links) {
         	final NeuronSimple sn = (NeuronSimple) cl.dendrite;
         	if (sn.occupy) {
                 for (Link sl : sn.s_links) {
                     	minX = Math.min(minX, sl.dendrite.x);
                     	minY = Math.min(minY, sl.dendrite.y);
 
                     	maxX = Math.max(maxX, sl.dendrite.x);
                     	maxY = Math.max(maxY, sl.dendrite.y);
                 }
             }
         }
         int boxSize = Math.max(maxX - minX, maxY - minY);
         BufferedImage image = new BufferedImage(boxSize, boxSize, BufferedImage.TYPE_INT_ARGB);
 
         int pX, pY;
         for (Link cl : cn.s_links) {
         	final NeuronSimple sn = (NeuronSimple) cl.dendrite;
         	if (sn.occupy) {
                 for (Link sl : sn.s_links) {
 //                    if (sl.w > 0) {
 						pX = (boxSize / 2) + (sl.dendrite.x - cl.axon.x);
 						pY = (boxSize / 2) + (sl.dendrite.y - cl.axon.y);
 		            	if (pX >= 0 && pX < boxSize 
 		            			&& pY >= 0 && pY < boxSize) {
 		                	
 		                	int c = Utils.calcGrey(image, pX, pY);
 							c += 50;//255 * sl.w;
 							image.setRGB(pX, pY, Utils.create_rgb(255, c, c, c));
 //		                }
                     }
                 }
         	}
         }
         return image;
 	}
 	
 	private BufferedImage drawTotalRF() {
 		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
 		int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
 
         for (Link cl : cn.s_links) {
         	final NeuronSimple sn = (NeuronSimple) cl.dendrite;
 //        	if (sn.occupy) {
                 for (Link sl : sn.s_links) {
                 	minX = Math.min(minX, sl.dendrite.x);
                 	minY = Math.min(minY, sl.dendrite.y);
 
                 	maxX = Math.max(maxX, sl.dendrite.x);
                 	maxY = Math.max(maxY, sl.dendrite.y);
                 }
 //            }
         }
         int boxSize = Math.max(maxX - minX, maxY - minY);
         BufferedImage image = new BufferedImage(boxSize, boxSize, BufferedImage.TYPE_INT_ARGB);
 
         int pX, pY;
         for (Link cl : cn.s_links) {
         	final NeuronSimple sn = (NeuronSimple) cl.dendrite;
 //        	if (sn.occupy) {
                 for (Link sl : sn.s_links) {
 //                    if (sl.w > 0) {
                     	pX = (boxSize / 2) + (sl.dendrite.x - cl.axon.x);
 						pY = (boxSize / 2) + (sl.dendrite.y - cl.axon.y);
 		            	if (pX >= 0 && pX < boxSize 
 		            			&& pY >= 0 && pY < boxSize) {
 		                	
 		                	int c = Utils.calcGrey(image, pX, pY);
 							c += 50;//255 * sl.w;
 							image.setRGB(pX, pY, Utils.create_rgb(255, c, c, c));
 //		                }
                     }
                 }
 //        	}
         }
         return image;
 	}
 
 //	private BufferedImage drawRF() {
 //		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
 //		int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
 //
 //        for (int i = 0; i < zone.nsc_links; i++) {
 //        	final Link3d cl = cn.s_links[i];
 //            if (zone.s[cl.x][cl.y][cl.z].occupy) {
 //            	final NeuronSimple sn = zone.s[cl.x][cl.y][cl.z];
 //            	if (sn.occupy) {
 //                    for (int j = 0; j < zone.ns_links; j++) {
 //                        final Link2dZone sl = sn.s_links[j];
 //                        if (sl.cond) {
 //                        	minX = Math.min(minX, sl.x);
 //                        	minY = Math.min(minY, sl.y);
 //
 //                        	maxX = Math.max(maxX, sl.x);
 //                        	maxY = Math.max(maxY, sl.y);
 //                        }
 //                    }
 //            	}
 //            }
 //        }
 //        BufferedImage image = new BufferedImage(maxX - minX + 2, maxY - minY + 2, BufferedImage.TYPE_INT_ARGB);
 //
 //        int pX, pY;
 //        for (int i = 0; i < zone.nsc_links; i++) {
 //        	final Link3d cl = cn.s_links[i];
 //            if (zone.s[cl.x][cl.y][cl.z].occupy) {
 //            	
 //            	final NeuronSimple sn = zone.s[cl.x][cl.y][cl.z];
 //            	if (sn.occupy) {
 //                    for (int j = 0; j < zone.ns_links; j++) {
 //                        final Link2dZone sl = sn.s_links[j];
 //                        if (sl.cond) {
 //							pX = (sl.x - minX);
 //							pY = (sl.y - minY);
 //	                    	
 //	                    	int c = Utils.calcGrey(image, pX, pY);
 //							c += 50;
 //							image.setRGB(pX, pY, Utils.create_rgb(255, c, c, c));
 //                        }
 //                    }
 //            	}
 //            }
 //        }
 //        return image;//.getSubimage(0, 0, maxX, maxY);
 //	}
 	
 	private String getName(Field f) {
 		return f.getName();
 //		return f.getAnnotation(RuntimeParam.class).name();
 	}
 
 	private String getValue(Field f, Object obj) {
 		try {
 			return f.get(obj).toString();
 		} catch (Exception e) {
 		}
 		return "???";
 	}
 
 	@Override
 	public Object whatAt(Point point) {
 		return null;
 	}
 
 	@Override
 	public void focusGained(Point point) {
 	}
 
 	@Override
 	public void focusLost(Point point) {
 	}
 
 	@Override
 	public void closed(Point point) {
 	}
 
 	@Override
 	public void internalFrameOpened(InternalFrameEvent e) {
 		zone.getCRF().focusGained(point);
 	}
 
 	@Override
 	public void internalFrameClosing(InternalFrameEvent e) {
 		zone.getCRF().focusLost(point);
 	}
 
 	@Override
 	public void internalFrameClosed(InternalFrameEvent e) {
 		zone.getCRF().closed(point);
 	}
 
 	@Override
 	public void internalFrameIconified(InternalFrameEvent e) {
 	}
 
 	@Override
 	public void internalFrameDeiconified(InternalFrameEvent e) {
 	}
 
 	@Override
 	public void internalFrameActivated(InternalFrameEvent e) {
 		zone.getCRF().focusGained(point);
 	}
 
 	@Override
 	public void internalFrameDeactivated(InternalFrameEvent e) {
 		zone.getCRF().focusLost(point);
 	}
 }
