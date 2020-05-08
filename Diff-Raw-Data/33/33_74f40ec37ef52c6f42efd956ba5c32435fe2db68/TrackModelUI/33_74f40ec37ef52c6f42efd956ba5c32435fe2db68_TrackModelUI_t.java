 package TLTTC;
 
 
 import java.awt.*;
 import javax.swing.*;
 import java.util.*;
 
 
 public class TrackModelUI
 {
 	private TrackRenderComponent renderer;
 	public TrackModelUI()
 	{
 		JFrame frame = new JFrame();
		frame.setSize(400,600);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setVisible(true);
 	
 		/*TrackRenderComponent*/ renderer = new TrackRenderComponent();
 		frame.getContentPane().add(renderer);
 
 		/* --example for reference
 		//basic tests to see how linear blocks render
 		ConnectorNode cn1 = new ConnectorNode(0,0,0);
 		ConnectorNode cn2 = new ConnectorNode(100,0,0);
 		ConnectorNode cn3 = new ConnectorNode(100,100,0);
 		ConnectorNode cn4 = new ConnectorNode(300,300,0);
 
 		LinearBlock lb1 = new LinearBlock(cn1, cn2, 0, 0);
 		LinearBlock lb2 = new LinearBlock(cn2, cn3, 0, 0);
 		LinearBlock lb3 = new LinearBlock(cn3, cn4, 0, 0);
 
 		lb2.setOccupation(true);
 
 		renderer.addBlock(lb1);
 		renderer.addBlock(lb2);
 		renderer.addBlock(lb3);
 		renderer.repaint();
 		*/
 	}
 	
 	public void addBlockToRender( LinearBlock block )
 	{
 		renderer.addBlock(block);
 	}
 
 	public void addBlockToRender( ArcBlock block )
 	{
 		renderer.addBlock(block);
 	}
 
 	public void refresh()
 	{
 		renderer.repaint();
 	}
 
 }
 
 class TrackRenderComponent extends JComponent
 {
 	ArrayList<LinearBlock> linearBlocks = new ArrayList<LinearBlock>();
 	ArrayList<ArcBlock>    arcBlocks = new ArrayList<ArcBlock>();
 	
 	//blocks that should show up are added and then drawn on repaint()	
 	public void addBlock( LinearBlock block )
 	{
 		linearBlocks.add(block);
 	}
 	public void addBlock( ArcBlock block )
 	{
 		arcBlocks.add(block);
 	}
 	
 	private int metersToPixels(double meters)
 	{
 		//for now no scaling
		return (int)meters/8;
 	}
 
 	@Override
 	protected void paintComponent( Graphics g )
 	{
 
 		//g.fillOval(100,100,10,10);
 		for(LinearBlock block : linearBlocks)
 		{
 			Node start = block.getStartNode();
 			Node stop  = block.getStopNode();
 			
 			int x0 = metersToPixels(start.getX());
 			int y0 = metersToPixels(start.getY());
 
 			int x1 = metersToPixels(stop.getX());
 			int y1 = metersToPixels(stop.getY());
 
 			//if a block is occupied make it red, otherwise make sure it is black
 			g.setColor(Color.BLACK);
 			if(block.isOccupied())
 				g.setColor(Color.RED);
 			if(block.getMaintenance())
 				g.setColor(Color.ORANGE);
 
 			g.drawLine(x0,y0,x1,y1);
 		}
 
 		for(ArcBlock block : arcBlocks)
 		{
 			//for now just draw a line from end to end
 			Node start = block.getStartNode();
 			Node stop  = block.getStopNode();
 			
 			int x0 = metersToPixels(start.getX());
 			int y0 = metersToPixels(start.getY());
 
 			int x1 = metersToPixels(stop.getX());
 			int y1 = metersToPixels(stop.getY());
 
 			//if a block is occupied make it red, otherwise make sure it is black
 			g.setColor(Color.BLACK);
 			if(block.isOccupied())
 				g.setColor(Color.RED);
 			if(block.getMaintenance())
 				g.setColor(Color.ORANGE);
 			g.drawLine(x0,y0,x1,y1);
 		}
 	}
 
 }
