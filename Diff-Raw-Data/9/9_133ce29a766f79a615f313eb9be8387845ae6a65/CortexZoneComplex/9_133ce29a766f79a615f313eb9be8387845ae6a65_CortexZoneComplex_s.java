 /*
  *  Copyright (C) 2012 The Animo Project
  *  http://animotron.org
  *
  *  This file is part of Animotron.
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
 package org.animotron.animi.cortex;
 
 import org.animotron.animi.*;
 import org.animotron.animi.acts.*;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Complex cortex zone
  * 
  * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
  * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
  * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
  */
 public class CortexZoneComplex extends CortexZoneSimple {
 
 	@Params
 	public Mapping[] in_zones;
     
 	public SNActivation snActivation = new SNActivation();
 	public CNActivation cnActivation = new CNActivation();
 	public Inhibitory inhibitory = new Inhibitory();
 
 	public Subtraction subtraction = new Subtraction();
 
 	public Restructorization restructorization = new Restructorization();
 
 	@Params
 	public Remember remember = new Remember();
 	
 	@InitParam(name="disper")
 	public double disper = 0.5;
 
 	@InitParam(name="stoper_links")
 	public int stoper_links = 15;
 	
 	@InitParam(name="deep")
 	public int deep;
 	
 	/** Number of synaptic connections of the all simple neurons **/
 	public int ns_links;
 	/** Number of axonal connections of the all simple neurons **/
 	protected int nas_links = 9;
 	/** Number of synaptic connections of the complex neuron **/
 	public int nsc_links;
 	
 	/** Memory **/
 	public NeuronSimple[][][] s;
 	
     CortexZoneComplex() {
 		super();
     }
 
 	CortexZoneComplex(String name, MultiCortex mc, int deep, Mapping[] in_zones) {
 		super(name, mc);
 		this.in_zones = in_zones;
 		this.deep = deep;
     }
     
     public void init() {
     	super.init();
     	
 		s = new NeuronSimple[width()][height()][deep];
 
 		ns_links = 0;
         for (Mapping i : in_zones) {
             ns_links += i.ns_links;
 		}
 
 		nsc_links = nas_links * deep;
 
 	    // Инициализация синаптических связей простых нейронов
 		for (int x = 0; x < width(); x++) {
 			for (int y = 0; y < height(); y++) {
 				for (int z = 0; z < deep; z++) {
 					s[x][y][z] = new NeuronSimple(x,y,z);
 				}
 			}
 		}
 
 		// Создание синаптических связей симпл нейронов.
 		// Связи распределяются случайным образом.
 		// Плотность связей убывает экспоненциально с удалением от колонки.
 		double x_in_nerv, y_in_nerv;
         double X, Y, S;
         double _sigma, sigma;
 
 		for (Mapping m : in_zones) {
 
             boolean[][] nerv_links = new boolean[m.zone.width()][m.zone.height()];
 
 //            int sigmaX = (int) (m.disp * m.zone.width());
 //            int sigmaY = (int) (m.disp * m.zone.height());
 
             for (int x = 0; x < width(); x++) {
 				for (int y = 0; y < height(); y++) {
 //					System.out.println("x = "+x+" y = "+y);
 
 					// Определение координат текущего нейрона в масштабе
 					// проецируемой зоны
 					x_in_nerv = x * m.zone.width() / (double) width();
 					y_in_nerv = y * m.zone.height() / (double) height();
 //					System.out.println("x_in_nerv = "+x_in_nerv+" y_in_nerv = "+y_in_nerv);
 
                     _sigma = m.disp * ((m.zone.width() + m.zone.height()) / 2);
                     sigma = _sigma;
 
                     for (int z = 0; z < deep; z++) {
 						// Обнуление массива занятости связей
 						for (int n1 = 0; n1 < m.zone.width(); n1++) {
 							for (int n2 = 0; n2 < m.zone.height(); n2++) {
 								nerv_links[n1][n2] = false;
 							}
 						}
 
 						// преобразование Бокса — Мюллера для получения
 						// нормально распределенной величины
 						// DispLink - дисперсия связей
 						int count = 0;
 						for (int i = 0; i < m.ns_links; i++) {
                             int lx, ly;
                             do {
                                 do {
 	                                if (count > m.ns_links * 3) {
 	                                	if (Double.isInfinite(sigma)) {
 	                                		System.out.println("initialization failed @ x = "+x+" y = "+y);
 	                                		System.exit(1);
 	                                	}
 	                                	sigma += _sigma;
 //	        							System.out.println(""+i+" of "+m.ns_links+" ("+sigma+")");
 	                                	count = 0;
 	                                }
 	                                count++;
 	                                	
 	                                do {
 	                                    X = 2.0 * Math.random() - 1;
 	                                    Y = 2.0 * Math.random() - 1;
 	                                    S = X * X + Y * Y;
 	                                } while (S > 1 || S == 0);
 	                                S = Math.sqrt(-2 * Math.log(S) / S);
 	                                double dX = X * S * sigma;
 	                                double dY = Y * S * sigma;
 	                                lx = (int) Math.round(x_in_nerv + dX);
 	                                ly = (int) Math.round(y_in_nerv + dY);
 	
 	                                //определяем, что не вышли за границы поля колонок
 	                                //колонки по периметру не задействованы
                                 } while (!(lx >= 1 && ly >= 1 && lx < m.zone.width() - 1 && ly < m.zone.height() - 1));
 
 //                                System.out.println("lx = "+lx+" ly = "+ly);
 
                             // Проверка на повтор связи
 							} while (nerv_links[lx][ly]);
 
 							nerv_links[lx][ly] = true;
 
 							// Создаем синаптическую связь
 							new Link(m.zone.getCol(lx, ly), s[x][y][z], LinkType.NORMAL);
 						}
 					}
 				}
 			}
 		}
 
 		// Инициализация аксонных связей простых нейронов
 		// и, соответственно, синаптических сложных нейронов.
 		// В простейшем случае каждый простой нейрон сязан с девятью колонками,
 		// образующими квадрат с центров в этом нейроне.
 //		for (int x = 0; x < width(); x++) {
 //			for (int y = 0; y < height(); y++) {
 //				NeuronComplex sn = col[x][y];
 //
 //				sn.s_links = new Link[nsc_links];
 //				for (int i = 0; i < nsc_links; i++) {
 //					sn.s_links[i] = new Link();
 //				}
 //			}
 //		}
 
 		// колонки по периметру не задействованы
 		for (int x = 1; x < width() - 1; x++) {
 			for (int y = 1; y < height() - 1; y++) {
 
 				//XXX: disparse
 				for (int i = x - 1; i <= x + 1; i++) {
 					for (int j = y - 1; j <= y + 1; j++) {
 						for (int k = 0; k < deep; k++) {
 
 							new Link(s[i][j][k], col[x][y], LinkType.NORMAL);
 						}
 					}
 				}
 				col[x][y].init();
 			}
 		}
 		
         //разброс торозных связей
 		_sigma = disper;// * ((width() + height()) / 2);
         boolean[][] nerv_links = new boolean[width()][height()];
         
         int _sigma_ = 1;//(int) _sigma;
 
         for (int x = _sigma_; x < width() - _sigma_; x++) {
 			for (int y = _sigma_; y < height() - _sigma_; y++) {
 				System.out.println("x = "+x+" y = "+y);
 
 				x_in_nerv = x;
 				y_in_nerv = y;
 		        sigma = _sigma;
 
 				// Обнуление массива занятости связей
 				for (int n1 = 0; n1 < width(); n1++) {
 					for (int n2 = 0; n2 < height(); n2++) {
 						nerv_links[n1][n2] = false;
 					}
 				}
 
 				// преобразование Бокса — Мюллера для получения
 				// нормально распределенной величины
 				// DispLink - дисперсия связей
 				int count = 0;
 				for (int i = 0; i < stoper_links; i++) {
                     int lx, ly;
                     do {
                         do {
                             if (count > stoper_links * 5) {
                             	if (Double.isInfinite(sigma)) {
                             		System.out.println("initialization failed @ x = "+x+" y = "+y);
                             		System.exit(1);
                             	}
                             	sigma += _sigma * .1;
     							System.out.println(""+i+" of ("+sigma+")");
                             	count = 0;
                             }
                             count++;
                             	
                             do {
                                 X = 2.0 * Math.random() - 1;
                                 Y = 2.0 * Math.random() - 1;
                                 S = X * X + Y * Y;
                             } while (S > 1 || S == 0);
                             S = Math.sqrt(-2 * Math.log(S) / S);
                             double dX = X * S * sigma;
                             double dY = Y * S * sigma;
                             lx = (int) Math.round(x_in_nerv + dX);
                             ly = (int) Math.round(y_in_nerv + dY);
 
                             //определяем, что не вышли за границы поля колонок
                             //колонки по периметру не задействованы
                         } while (!(lx >= 1 && ly >= 1 && lx < width() - 1 && ly < height() - 1));
 
                         System.out.print("!");
 //                            System.out.println("lx = "+lx+" ly = "+ly);
 
                     // Проверка на повтор связи
 					} while (nerv_links[lx][ly]);
                     System.out.print(".");
 
 					nerv_links[lx][ly] = true;
 
 					// Создаем синаптическую связь
 					Link link = new Link(getCol(lx, ly), getCol(x, y), LinkType.STOPPER);
 					
 					//UNDERSTAND: is it ok to have sum ^2 ~ 1
 					link.w = 1 / ((double) (stoper_links / 2));//Math.sqrt(1 / (double)stoper_links);
 				}
 				System.out.println();
 			}
 		}
 	}
     
 	// Картинка активных нейронов по колонкам
 	public BufferedImage[] getSImage() {
 		BufferedImage[] a = new BufferedImage[deep];
 		for (int z = 0; z < deep; z++) {
 			BufferedImage image = new BufferedImage(width(), height(), BufferedImage.TYPE_INT_ARGB);
 			for (int x = 0; x < width(); x++) {
 				for (int y = 0; y < height(); y++) {
 					int c = s[x][y][z].activity > 0 ? Color.WHITE.getRGB() : Color.BLACK.getRGB();
 					image.setRGB(x, y, Utils.create_rgb(255, c, c, c));
 				}
 			}
 			a[z] = image;
 		}
 		return a;
 	}
 	
 	public void prepareForSerialization() {
 		CRF = null;
 		RRF = null;
 	}
 	
 	ColumnRF_Image CRF = null;
 	
 	public Imageable getCRF() {
 		if (CRF == null)
 			CRF = new ColumnRF_Image();
 		
 		return CRF;
 	}
 
 	class ColumnRF_Image implements Imageable {
 		
 	    private int boxSize;
 	    private int maxX;
 	    private int maxY;
 	    private BufferedImage image;
 	    
 	    private List<Point> watching = new ArrayList<Point>();
 	    private Point atFocus = null;
 
 		ColumnRF_Image() {
 	        boxSize = 1;
 	        for (Mapping m : in_zones) {
 	            boxSize = (int) Math.max(boxSize, ((m.zone.width() + m.zone.height()) / 2) * m.disp * 15);
 			}
 
 	        maxX = width() * boxSize;
 	        maxY = height() * boxSize;
 	        
 	        image = new BufferedImage(maxX, maxY, BufferedImage.TYPE_INT_RGB);
 		}
 	
 		public String getImageName() {
 			return "input from output";
 		}
 
 		public BufferedImage getImage() {
 			Graphics g = image.getGraphics();
 			g.setColor(Color.BLACK);
 			g.fillRect(0, 0, maxX, maxY);
 	
 			int pX, pY = 0;
 	
 			g.setColor(Color.YELLOW);
 			for (Point p : watching) {
 				if (atFocus == p) {
 					g.setColor(Color.RED);
 					g.draw3DRect(p.x*boxSize, p.y*boxSize, boxSize, boxSize, true);
 					g.setColor(Color.YELLOW);
 				} else
 					g.draw3DRect(p.x*boxSize, p.y*boxSize, boxSize, boxSize, true);
 			}
 	
 			for (int x = 1; x < width() - 1; x++) {
 				for (int y = 1; y < height() - 1; y++) {
 					
 //					g.drawLine(x*boxSize, 0, x*boxSize, maxY);
 //					g.drawLine(0, y*boxSize, maxX, y*boxSize);
 	
 					final NeuronComplex cn = col[x][y];
 		    		double Q2 = 0;
 		    		for (LinkQ link : cn.Qs.values()) {
 		    			Q2 += link.q * link.q;
 		    		}
 					for (LinkQ link : cn.Qs.values()) {
                     	pX = x*boxSize + (boxSize / 2) + (link.synapse.x - x);
 						pY = y*boxSize + (boxSize / 2) + (link.synapse.y - y);
                                 	
 						if (       pX > x*boxSize 
                     			&& pX < (x*boxSize+boxSize) 
                     			&& pY > y*boxSize 
                     			&& pY < (y*boxSize+boxSize)) {
 				                    	
 	                    	int c = Utils.calcGrey(image, pX, pY);
							c += 255 * link.q * 10;// * Q2;
 							if (c > 255) c = 255;
 							image.setRGB(pX, pY, Utils.create_rgb(255, c, c, c));
                     	}
                     }
 				}
 			}
 			return image;
 		}
 
 		@Override
 		public Object whatAt(Point point) {
 			try {
 				Point pos = new Point(
 					Math.round(point.x / boxSize), 
 					Math.round(point.y / boxSize)
 				);
 				
 				if (pos.x > 1 && pos.x < width && pos.y > 1 && pos.y < height) {
 					
 					watching.add(pos);
 					
 					System.out.println("x = "+pos.x+" y = "+pos.y);
 					
 					return new Object[] { CortexZoneComplex.this, pos };
 				}
 			} catch (Exception e) {
 			}
 			return null;
 		}
 
 		@Override
 		public void focusGained(Point point) {
 			atFocus = point;
 		}
 
 		@Override
 		public void focusLost(Point point) {
 			atFocus = null;
 		}
 
 		@Override
 		public void closed(Point point) {
 			watching.remove(point);
 		}
 	}
 
 	RRF_Image RRF = null;
 	
 	public Imageable getRRF() {
 		if (RRF == null)
 			RRF = new RRF_Image();
 		
 		return RRF;
 	}
 
 	class RRF_Image implements Imageable {
 		
 	    private BufferedImage image;
 	    
 	    RRF_Image() {
 	        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
 		}
 	
 		public String getImageName() {
 			return "restored input";
 		}
 
 		public BufferedImage getImage() {
 			Graphics g = image.getGraphics();
 			g.setColor(Color.BLACK);
 			g.fillRect(0, 0, width, height);
 	
 			CortexZoneSimple zone = in_zones[0].zone;
 			for (int x = 1; x < width() - 1; x++) {
 				for (int y = 1; y < height() - 1; y++) {
 					
 					final NeuronComplex cn = zone.col[x][y];
 
 					int value = image.getRGB(x, y);
 			        int c_r = Utils.get_red(value);
 			        int c_g = Utils.get_green(value);
 			        int c_b = Utils.get_blue(value);
 
                 	double minus = cn.posActivity;
                 	if (minus > 0) {
                 		c_r += 255 * minus;
                 		if (c_r > 255) c_r = 255;
                 	} else {
                 		c_g += 255 * -minus;
                 		if (c_g > 255) c_g = 255;
                 	}
 					image.setRGB(x, y, Utils.create_rgb(255, c_r, c_g, c_b));
 				}
 			}
 			return image;
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
 	}
 
 	// Картинка суммы занятых нейронов в колонке
 	public BufferedImage[] getOccupyImage() {
 		BufferedImage[] a = new BufferedImage[deep];
 		for (int z = 0; z < deep; z++) {
 			BufferedImage image = new BufferedImage(width(), height(), BufferedImage.TYPE_INT_ARGB);
 			for (int x = 0; x < width(); x++) {
 				for (int y = 0; y < height(); y++) {
 					int c = s[x][y][z].occupy ? Color.WHITE.getRGB() : Color.BLACK.getRGB();
 					image.setRGB(x, y, Utils.create_rgb(255, c, c, c));
 				}
 			}
 			a[z] = image;
 		}
 		return a;
 	}
     
     public void cycle (int x1, int y1, int x2, int y2, Act<CortexZoneComplex> task) {
         for (int x = x1; x < x2; x++) {
             for (int y = y1; y < y2; y++) {
                 task.process(this, x, y);
             }
         }
     }
 
     //Граничные нейроны не задействованы.
     //Такт 1. Активация колонок (узнавание)
     public void cycleActivation() {
         cycle(1, 1, width() - 1, height() - 1, snActivation);
         cycle(1, 1, width() - 1, height() - 1, cnActivation);
         for (int i = 0; i < 10; i++)
         	cycle(1, 1, width() - 1, height() - 1, inhibitory);
     }
 
     //Граничные нейроны не задействованы.
     //Такт 2. Запоминание  и переоценка параметров стабильности нейрона
     public void cycle2() {
         cycle(1, 1, width() - 1, height() - 1, restructorization);
 //        cycle(1, 1, width() - 1, height() - 1, subtraction);
 //        cycle(1, 1, width() - 1, height() - 1, remember);
     }
 }
