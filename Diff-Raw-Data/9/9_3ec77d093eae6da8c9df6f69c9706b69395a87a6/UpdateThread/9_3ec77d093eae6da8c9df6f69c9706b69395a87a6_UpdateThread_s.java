 package info.pishen.gameoflife;
 
 import java.util.logging.Logger;
 
 public class UpdateThread extends Thread{
 	//@SuppressWarnings("unused")
 	private static Logger log = Logger.getLogger(UpdateThread.class.getName());
 	
 	private boolean[][] oldGrid, newGrid;
 	private CellGrid cellGrid;
 	private int parallelLevel = 1;
 	private boolean toStop = false;
 	private int evalIter = 0;
 	
 	public UpdateThread(CellGrid cellGrid){
 		this.cellGrid = cellGrid;
 	}
 	
 	public UpdateThread(CellGrid cellGrid, int evalIter){
 		this.cellGrid = cellGrid;
 		this.evalIter = evalIter;
 	}
 	
 	@Override
 	public void run(){
 		int count = 0;
 		double accuTime = 0.0;
 		while(toStop == false){
 			oldGrid = cellGrid.getGrid();
 			newGrid = new boolean[oldGrid.length][oldGrid[0].length];
 			
 			//parallel update//\\//\\//\\\//\\//\\//\\//
 			long startTime = System.currentTimeMillis();
 			
 			SubUpdateThread[] subUpdaters = new SubUpdateThread[parallelLevel];
			int blockSize = oldGrid.length / subUpdaters.length;
			for(int i = 0; i < subUpdaters.length; i++){
				subUpdaters[i] = new SubUpdateThread(i * blockSize, Math.min((i+1) * blockSize, oldGrid.length));
 				subUpdaters[i].start();
 			}
 			
 			for(int i = 0; i < subUpdaters.length; i++){
 				try {
 					subUpdaters[i].join();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 			
 			long endTime = System.currentTimeMillis();
 			//\\//\\//\\\//\\//\\//\\//\\//\\\//\\//\\//
 			
 			if(count < evalIter){
 				count++;
 				double updateTime = (endTime - startTime) / 1000.0;
 				log.info("Iter: " + count + " Time: " + updateTime);
 				accuTime += updateTime;
 				if(count == evalIter){
 					log.info("Parallel: " + parallelLevel + " Avg: " + (accuTime / (double)evalIter));
 					MainFrame.instance.evalNext(parallelLevel + 1);
 				}
 			}
 			
 			synchronized(this){
 				if(toStop == false){
 					cellGrid.replaceGrid(newGrid);
 				}
 			}
 			
 			MainFrame.instance.showUpdateTime((endTime - startTime) / 1000.0);
 			MainFrame.instance.repaintGrid();
 		}
 	}
 	
 	public synchronized void lagStop(){
 		toStop = true;
 	}
 	
 	public void setParallelLevel(int value){
 		parallelLevel = value;
 	}
 	
 	private class SubUpdateThread extends Thread{
 		private int iStart, iEnd;
 		
 		public SubUpdateThread(int iStart, int iEnd){
 			this.iStart = iStart;
 			this.iEnd = iEnd;
 		}
 		
 		@Override
 		public void run() {
 			//iEnd is excluded
 			for(int i = iStart; i < iEnd; i++){
 				for(int j = 0; j < oldGrid[0].length; j++){
 					int count = 0;
 					for(int iSub = Math.max(0, i - 1); iSub < Math.min(i + 2, oldGrid.length); iSub++){
 						for(int jSub = Math.max(0, j - 1); jSub < Math.min(j + 2, oldGrid[i].length); jSub++){
 							if(iSub == i && jSub == j){
 								continue;
 							}
 							if(oldGrid[iSub][jSub]){
 								count++;
 							}
 						}
 					}
 					if(count < 2 || count > 3){
 						newGrid[i][j] = false;
 					}else if(count == 3){
 						newGrid[i][j] = true;
 					}else{
 						newGrid[i][j] = oldGrid[i][j];
 					}
 				}
 			}
 		}
 	}
 }
