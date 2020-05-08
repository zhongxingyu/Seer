 /**
  * 
  */
 package edu.brown.cs32.atian.crassus.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 
 import javax.swing.BorderFactory;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EtchedBorder;
 
 import edu.brown.cs32.atian.crassus.backend.Stock;
 import edu.brown.cs32.atian.crassus.backend.StockList;
 
 /**
  * @author Matthew
  *
  */
 public class CrassusGUI implements GUI {
 	
 	public class CompoundChangeStockListener implements CrassusChangeStockListener {
 		@Override public void changeToStock(Stock stock) {
 			if(stock==null && stockInfoStateNormal){
 				frame.getContentPane().remove(stockInfo);
 				frame.add(nullStockInfo,BorderLayout.CENTER);
 				stockInfoStateNormal = false;
 			}
 			else if(!stockInfoStateNormal){
 				frame.getContentPane().remove(nullStockInfo);
 				frame.add(stockInfo,BorderLayout.CENTER);
 				stockInfoStateNormal = true;
 			}
 			
 			plotPane.changeToStock(stock);
 			eventBox.changeToStock(stock);
 			
 			frame.getContentPane().revalidate();
 			//frame.repaint();
 		}
 	}
 
 	private JFrame frame;
 	
 	private CrassusPlotPane plotPane;
 	
 	private CrassusStockTablePane stockBox;
 	private CrassusEventTablePane eventBox;
 	
 	private JPanel stockInfo;
 	private JPanel nullStockInfo;
 	private boolean stockInfoStateNormal = false;
 
 	public CrassusGUI(StockList stocks) {
 		
 		frame = new JFrame("Crassus");
 		frame.getContentPane().setBackground(Color.WHITE);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		stockBox = new CrassusStockTablePane(frame,stocks);
 		stockBox.setChangeStockListener(new CompoundChangeStockListener());
 
 		eventBox = new CrassusEventTablePane(frame);
 		
 		//make plot pane
 		plotPane = new CrassusPlotPane();
 		
 		
 		stockInfo = new JPanel();
 		stockInfo.setBackground(Color.WHITE);
 		stockInfo.setBorder(BorderFactory.createCompoundBorder(
 				BorderFactory.createEmptyBorder(20,20,20,20),
 				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
 		stockInfo.setLayout(new BorderLayout());
 		stockInfo.add(eventBox, BorderLayout.EAST);
 		stockInfo.add(plotPane,BorderLayout.CENTER);
 
 		nullStockInfo = new JPanel();
 		nullStockInfo.setBackground(Color.WHITE);
 		nullStockInfo.setBorder(BorderFactory.createCompoundBorder(
 				BorderFactory.createEmptyBorder(20,20,20,20),
 				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
 		nullStockInfo.setLayout(new BorderLayout());
 		nullStockInfo.add(new JPanel(), BorderLayout.CENTER);
 		
 		frame.add(stockBox, BorderLayout.WEST);
 		frame.add(nullStockInfo,BorderLayout.CENTER);
		frame.setMinimumSize(new Dimension(700,400));
 	}
 	
 	@Override
 	public void launch() {
 		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
 		frame.setVisible(true);
 	}
 
 	@Override
 	public void update() {
 		stockBox.refresh();
 		plotPane.refresh();
 	}
 
 }
