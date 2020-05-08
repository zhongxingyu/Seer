 /*  Copyright (C) 2011  Nicholas Wright
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package gui;
 
 
 import java.awt.Color;
 import java.awt.Graphics;
import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.swing.JPanel;
 

 public class DataGraph extends JPanel {
 	/**
 	 * Shows data over time
 	 */
 	private static final long serialVersionUID = 1L;
 	private int updateInterval = 1;		// in Seconds
 	private int noOfColums = 20;
 	private int columWidth = 2;
 	private Timer updater = null;
 
 
 	LinkedList<Integer> graphData = new LinkedList<Integer>();
 	AtomicInteger currentCount = new AtomicInteger(0);
 
 	public DataGraph(int hight, int width, int cNum, int cWidth, int interval){

 		this.setSize(width , hight);
 		this.noOfColums = cNum;
 		this.columWidth = cWidth;
 		this.updateInterval = interval;
 
 		synchronized (graphData) {
 			for (int i=0; i<noOfColums; i++){
 				graphData.add(0);
 			}
 		}
 	}
 
	public void increment(){
		currentCount.incrementAndGet();
	}

 	public void add(int delta){
 		currentCount.addAndGet(delta);
 	}
 
 	public void start(){
 		if (updater == null){
			updater = new Timer("Updater");
 			updater.scheduleAtFixedRate(new Updater(), updateInterval*1000, updateInterval*1000);
 		}

 	}
 
 	@Override
 	public void paintComponent(Graphics g){
		super.paintComponents(g);
 		g.setColor(Color.BLUE);
 		synchronized(graphData){
			Iterator<Integer> ite = graphData.listIterator();
 
 			int i = 0;
			while(ite.hasNext()){
				int currval =ite.next();
 				g.fillRect(i*columWidth, this.getHeight()-currval, columWidth, currval);
 				i++;
 			}
 		}
 	}
 	class Updater extends TimerTask{
 
 		@Override
 		public void run(){
 			synchronized (graphData) {
 				graphData.removeLast();
 				graphData.addFirst(currentCount.getAndSet(0));
 			}
			paintComponent(getGraphics());
 		}
 	}
}
