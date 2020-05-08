 package gui;
 
 import java.awt.GridLayout;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JPanel;
 
 import mcmc.MCMC;
 import mcmc.MCMCListener;
 
 import component.LikelihoodComponent;
 
 import parameter.AbstractParameter;
 
 /**
  * Panel that displays various output charts
  * @author brendano
  *
  */
 public class MainOutputFrame extends JPanel implements MCMCListener {
 	
 	//Maximum size we allow data series to get before they're thinned
 	public final int MAX_SERIES_SIZE = 1000;
 	
 	private int rows = 2;
 	private int cols = 2;
 	private MCMC mcmc = null;
 	private int frequency;
 	private boolean initialized = false;
 	
 	private List<MonitorPanel> figureList = new ArrayList<MonitorPanel>();
 	
 	public MainOutputFrame(MCMC chain, int frequency) {
 		this.mcmc = chain;
 		chain.addListener(this);
 		this.frequency = frequency;
 		initComponents();
 	}
 	
 	public void addChart(AbstractParameter<?> param, String logKey) {		
 		MonitorPanel figure;
 		if (logKey != null)
			 figure = new ParamMonitor(param, logKey);
 		else
			 figure = new ParamMonitor(param);
 		
 		figureList.add(figure);
 		this.add(figure);
 	}
 	
 	public void addChart(AbstractParameter<?> param) {
 		addChart(param, null);
 	}
 	
 	public void addChart(LikelihoodComponent comp) {
		MonitorPanel figure = new LikelihoodMonitor(comp);	
 		figureList.add(figure);
 		this.add(figure);
 	}
 	
 	
 	private void initComponents() {
 		//For reasons I don't understand at all, setting a layout here is required in order
 		//for figure legend element to be drawn, even though we set another
 		//layout later (in initializeMatrix). Your guess is as good as mine. 
 		GridLayout layout = new GridLayout(rows, cols, 2, 2);
 		this.setLayout(layout);
 		this.setBackground(ACGFrame.backgroundColor);
 	}
 
 	@Override
 	public void setMCMC(MCMC chain) {
 		this.mcmc = chain;
 	}
 
 	@Override
 	public void newState(int stateNumber) {
 		if (!initialized) {
 			initializeMatrix();
 		}
 		
 		if (stateNumber % frequency == 0) {
 			for(MonitorPanel fig : figureList) {
 				fig.update(stateNumber);
 			}
 			
 			//If any figures have more than MAX_SERIES_SIZE data point, remove half of the data points from all
 			//series and multiply frequency by two (so we collect less frequently)
 			boolean needsThinning = false;
 			for(MonitorPanel fig : figureList) {
 				needsThinning = fig.getSeriesSize() > MAX_SERIES_SIZE;
 				if (needsThinning)
 					break;
 			}
 			
 			if (needsThinning) {
 				System.out.println("Thinning all series.....");
 				for(MonitorPanel fig : figureList) { 
 					fig.thinSeries();
 				}
 				frequency *= 2;
 			}
 		}
 	}
 
 	private void initializeMatrix() {
 		initialized = true;
 		int numFigs = figureList.size();
 		if (numFigs < 4) {
 			rows = 1;
 			cols = numFigs;
 		}
 		if (numFigs == 4) {
 			rows = 2;
 			cols = 2;
 		}
 		if (numFigs > 4 && numFigs < 7) {
 			rows = 2;
 			cols = 3;
 		}
 		if (numFigs >=7 && numFigs < 10) {
 			rows = 3;
 			cols = 3;
 		}
 		
 		GridLayout layout = new GridLayout(rows, cols, 2, 2);
 		this.setLayout(layout);
 		for(MonitorPanel fig : figureList) {
 			this.add(fig);
 		}
 		
 		this.revalidate();
 		initialized = true;
 	}
 
 	@Override
 	public void chainIsFinished() {
 		// TODO Auto-generated method stub
 	}
 }
