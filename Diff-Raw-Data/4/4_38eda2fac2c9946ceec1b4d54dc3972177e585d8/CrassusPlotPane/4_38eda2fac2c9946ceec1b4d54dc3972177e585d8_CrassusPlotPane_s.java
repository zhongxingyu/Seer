 package edu.brown.cs32.atian.crassus.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 import javax.swing.border.EtchedBorder;
 
 import edu.brown.cs32.atian.crassus.backend.Stock;
 
 public class CrassusPlotPane extends JPanel {
 
 	CrassusImageDisplayer imageDisplayer;
 	Stock stock;
 	
 	public CrassusPlotPane(){
 		this.setBackground(Color.WHITE);
 		this.setPreferredSize(new Dimension(300,300));
 		this.setLayout(new BorderLayout());
 		this.setBorder(BorderFactory.createCompoundBorder(
 				BorderFactory.createCompoundBorder(
 						BorderFactory.createEmptyBorder(20,20,20,20),
 						BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)),
 				BorderFactory.createEmptyBorder(5,5,5,5)));
 		
 		imageDisplayer = new CrassusImageDisplayer();
 		this.add(imageDisplayer, BorderLayout.CENTER);
 	}
 	
 	public void changeToStock(Stock stock){
 		this.stock = stock;
 		refresh();
 	}
 	
 	public void refresh(){
 		//check width of imageDisplayer because when pane is swapped out it will be zero, plot object flips out
 		if(stock!=null && imageDisplayer.getWidth()!=0){
			StockPlot plot = new PlotWrapper(stock.getCompanyName());
 			stock.addToPlot(plot);
 			BufferedImage image = plot.getPrimaryBufferedImage(imageDisplayer.getWidth(), imageDisplayer.getHeight());
 			imageDisplayer.setImage(image);
 		}
 		else{
 			imageDisplayer.setImage(null);
 		}
 		imageDisplayer.revalidate();
 		imageDisplayer.repaint();
 	}
 	
 }
