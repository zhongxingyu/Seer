 package chalmers.dax021308.ecosystem.view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.media.opengl.GL;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import sun.misc.Cleaner;
 
 import chalmers.dax021308.ecosystem.model.agent.IAgent;
 import chalmers.dax021308.ecosystem.model.environment.EcoWorld;
 import chalmers.dax021308.ecosystem.model.environment.IModel;
 import chalmers.dax021308.ecosystem.model.environment.IObstacle;
 import chalmers.dax021308.ecosystem.model.population.IPopulation;
 import chalmers.dax021308.ecosystem.model.util.Log;
 import chalmers.dax021308.ecosystem.model.util.Position;
 import chalmers.dax021308.ecosystem.model.util.Vector;
 
 /**
  * Simulation JPanel showing graphical representation of the model.
  * <p>
  * Uses the build-in Java AWT for rendering.
  * 
  * @author Erik Ramqvist
  *
  */
 public class AWTSimulationView extends JPanel implements IView {
 	
 	private static final long serialVersionUID = 1585638837620985591L;
 	private List<IPopulation> newPops;
 	private List<IObstacle> newObs;
 	private Random ran = new Random();
 	private Dimension gridDimension;
 	private Timer fpsTimer;
 	private int updates;
 	private int lastFps;
 	private boolean showFPS;
 	private int newFps;
 	private Object fpsSync = new Object();
 	/**
 	 * Create the panel.
 	 */
 	public AWTSimulationView(IModel model, Dimension size, boolean showFPS) {
 		model.addObserver(this);
 		this.setBackground(Color.white);
 		gridDimension = size;
 		this.showFPS = showFPS;
 		if(showFPS) {
 			fpsTimer = new Timer();
 			fpsTimer.schedule(new TimerTask() {
 				@Override
 				public void run() {
 					int fps = getUpdate();
 					if(fps + lastFps != 0) {
 						fps = ( fps + lastFps ) / 2;
 					} 
 					setNewFps(fps);
 					lastFps = fps;
 					setUpdateValue(0);
 				}
 			}, 1000, 1000);
 		}
 	}
 	
 	private int getUpdate() {
 		synchronized (AWTSimulationView.class) {
 			return updates;
 		}
 	}
 
 	private void setUpdateValue(int newValue) {
 		synchronized (AWTSimulationView.class) {
 			updates = newValue;
 		}
 	}
 	
 
 	private int getNewFps() {
 		synchronized (fpsSync) {
 			return newFps;
 		}
 	}
 
 	private void setNewFps(int newValue) {
 		synchronized (fpsSync) {
 			newFps = newValue;
 		}
 	}
 	
 	private void increaseUpdateValue() {
 		synchronized (AWTSimulationView.class) {
 			updates++;
 		}
 	}
 
 
 	@Override
 	public void propertyChange(PropertyChangeEvent event) {
 		String eventName = event.getPropertyName();
 		if(eventName == EcoWorld.EVENT_STOP) {
 			//Model has stopped. Maybe hide view?
 			//frame.setVisible(false);
 		} else if(eventName == EcoWorld.EVENT_TICK) {
 			//Tick notification recived from model. Do something with the data.
 			if(event.getNewValue() instanceof List<?>) {
 				this.newPops = clonePopulationList((List<IPopulation>) event.getNewValue());
 			}
 			if(event.getOldValue() instanceof List<?>) {
 				this.newObs = (List<IObstacle>) event.getOldValue();
 			}
 			repaint();
 		} else if(eventName == EcoWorld.EVENT_DIMENSIONCHANGED) {
 			Object o = event.getNewValue();
 			if(o instanceof Dimension) {
				Dimension d = (Dimension) o;
 			}
			//Handle dimension change here.
 		}
 	}
 	
 	@Override
     public void paintComponent(Graphics g) {
 		super.paintComponent(g);
     	long start = System.currentTimeMillis();
 		Graphics2D g2 = (Graphics2D)g;
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		
 		int fps = getNewFps();
 		if(showFPS) {
 			increaseUpdateValue();
 			char[] fpsChar;
 			if(fps > 1000) {
 				fpsChar = new char[8];
 				fpsChar[0] = '9';
 				fpsChar[1] = '9';
 				fpsChar[2] = '9';
 				fpsChar[3] = ' ';
 				fpsChar[4] = 'f';
 				fpsChar[5] = 'p';
 				fpsChar[6] = 's';
 			} else if(fps > 100) {
 				fpsChar = new char[7];
 				fpsChar[1] = Character.forDigit( (fps / 100) , 10);
 				fpsChar[1] = Character.forDigit( (fps % 100) / 10, 10);
 				fpsChar[2] = Character.forDigit(  fps % 10   , 10);
 				fpsChar[3] = ' ';
 				fpsChar[4] = 'f';
 				fpsChar[5] = 'p';
 				fpsChar[6] = 's';
 			} else if(fps > 10) {
 				fpsChar = new char[6];
 				fpsChar[0] = Character.forDigit(fps / 10, 10);
 				fpsChar[1] = Character.forDigit(fps % 10, 10);
 				fpsChar[2] = ' ';
 				fpsChar[3] = 'f';
 				fpsChar[4] = 'p';
 				fpsChar[5] = 's';		
 			} else {
 				fpsChar = new char[5];
 				fpsChar[0] = Character.forDigit(fps, 10);
 				fpsChar[1] = ' ';
 				fpsChar[2] = 'f';
 				fpsChar[3] = 'p';
 				fpsChar[4] = 's';
 			}
 			g2.drawChars(fpsChar, 0, fpsChar.length, 15, 30);
 		}
 		
 		for(IPopulation pop : newPops) {
 			for(IAgent a : pop.getAgents()) {
 				Position p = a.getPosition();
 				g2.setColor(a.getColor());
 		       // g2.fillOval((int)(p.getX()), (int) (frame.getSize().getHeight() - p.getY()), a.getHeight(), a.getWidth());
 		        double height = (double)a.getHeight();
                 double width = (double)a.getWidth();
                 Color c = a.getColor();
 
                 Vector v = new Vector(a.getVelocity());
           		Vector bodyCenter = new Vector(p,new Position(0,0));
           		v.multiply(2.0*height/(3.0*v.getNorm()));
           		Vector nose = v.add(bodyCenter);
           		
           		v = new Vector(a.getVelocity());
           		v.multiply(-1.0*height/(3.0*v.getNorm()));
           		Vector bottom = v.add(bodyCenter);
           		
           		v = new Vector(a.getVelocity());
           		Vector legLengthVector = new Vector(-v.getY()/v.getX(),1);
           		legLengthVector = legLengthVector.multiply(width/(2*legLengthVector.getNorm()));
           		Vector rightLeg = legLengthVector.add(bottom);
           		
           		v = new Vector(a.getVelocity());
           	    legLengthVector = new Vector(v.getY()/v.getX(),-1);
           	    legLengthVector = legLengthVector.multiply(width/(2*legLengthVector.getNorm()));
           		Vector leftLeg = legLengthVector.add(bottom);
           		
           		int[] xPoints = new int[3];
       			int[] yPoints = new int[3];
       			xPoints[0] = (int) nose.getX();
       			xPoints[1] = (int) rightLeg.getX();
       			xPoints[2] = (int) leftLeg.getX();
       			
       			yPoints[0] = (int) (getHeight() - nose.getY());
       			yPoints[1] = (int) (getHeight() - rightLeg.getY());
       			yPoints[2] = (int) (getHeight() - leftLeg.getY());
       			
       			
           		g2.fillPolygon(xPoints, yPoints, 3);
 			}
 		}
 		
 		g2.setColor(Color.black);
 		int xLeft = 0;
 		int xRight = (int)gridDimension.getWidth();
 		int yBot = (int)(getHeight());
 		int yTop = (int)(getHeight())-(int)gridDimension.getHeight();
 		g2.drawLine(xLeft, yBot, 
 				   xLeft, yTop);
 		g2.drawLine(xLeft, yTop, 
 				   xRight, yTop);
 		g2.drawLine(xRight, yTop,
 				   xRight, yBot);
 		g2.drawLine(xRight, yBot,
 				   xLeft, yBot);
 		
 
 		Long totalTime = System.currentTimeMillis() - start;
 		StringBuffer sb = new StringBuffer("AWT Redraw! Fps: ");
 		sb.append(getNewFps());
 		sb.append(" Rendertime in ms: ");
 		sb.append(totalTime);
     	System.out.println(sb.toString());	
 		
     }
 	
 	/**
 	 * Clones the given list with {@link IPopulation#clonePopulation()} method.
 	 */
 	private List<IPopulation> clonePopulationList(List<IPopulation> popList) {
 		List<IPopulation> list = new ArrayList<IPopulation>(popList.size());
 		for(IPopulation p : popList) {
 			list.add(p.clonePopulation());
 		}
 		return list;
 	}
 	
 	/**
 	 * Sets the FPS counter visible or not visible
 	 * 
 	 * @param visible
 	 */
 	public void setFPSCounterVisible(boolean visible) {
 		if(showFPS && !visible) {
 				fpsTimer.cancel();
 				showFPS = visible;
 		} else if(!showFPS && visible) {
 			fpsTimer = new Timer();
 			fpsTimer.schedule(new TimerTask() {
 				@Override
 				public void run() {
 					newFps = getUpdate();
 					int temp = newFps;
 					if(newFps + lastFps != 0) {
 						newFps = ( newFps + lastFps ) / 2;
 					} 
 					lastFps = temp;
 					setUpdateValue(0);
 				}
 			}, 1000, 1000);
 			showFPS = true;
 		}
 	}
 
 	@Override
 	public void init() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void addController(ActionListener controller) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onTick() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void release() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
