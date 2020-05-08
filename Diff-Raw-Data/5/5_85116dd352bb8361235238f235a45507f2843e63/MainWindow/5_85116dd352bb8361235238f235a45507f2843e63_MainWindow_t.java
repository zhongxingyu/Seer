 package main;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.KeyEventDispatcher;
 import java.awt.KeyboardFocusManager;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.BoxLayout;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import data.Shape;
 import data.ShapeData;
 
 
 public class MainWindow extends JFrame
 {
 
 	private static final long serialVersionUID = 1L;
 	JPanel mainPanel;
 	ImagePanel imagePanel;
 	Toolbox toolbox;
 	Dimension minimumSize = new Dimension(1000,600);
 	//String imageName = "res/kirby.jpg";
 	String imageName = "res/a.jpg";
 	JFileChooser fc;
 
 	/**
 	 * Create the panel.
 	 */
 	public MainWindow() 
 	{
 		this.addWindowListener(new WindowAdapter() 
 		{
 			public void windowClosing(WindowEvent event) 
 			{
 				System.exit(0);
 			}
 
 		});
 
 		//TODO Keyboard!!!
 		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
 		manager.addKeyEventDispatcher(new MyDispatcher());
 
 		this.setMinimumSize(minimumSize);
 		mainPanel = new JPanel();
 		mainPanel.setFocusable(true);
 
 		this.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
 		this.setContentPane(mainPanel);
 
 		God.mainWindow = this;
 		God.shapeData = new ShapeData();
 
 		try 
 		{
 			imagePanel = new ImagePanel(imageName);
 		} 
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
 
 		imagePanel.setOpaque(true); //content panes must be opaque
 
 		toolbox = new Toolbox(color());
 
 		God.imagePanel = imagePanel;
 		God.toolBox = toolbox;
 
 		mainPanel.setLayout(new BorderLayout());
 		mainPanel.add(toolbox,BorderLayout.EAST);
 
 		VertexPanel vPanel = new VertexPanel();
 		God.vertexPanel = vPanel;
 
 		// Set up layered panel
 		MyLayeredPane layeredPanel = new MyLayeredPane(new BorderLayout());
 		layeredPanel.imagePanel = imagePanel;
 		layeredPanel.vertexPanel = vPanel;
 		layeredPanel.add(imagePanel,BorderLayout.CENTER);
 		layeredPanel.add(vPanel,BorderLayout.CENTER);
 		God.layeredPanel = layeredPanel;
 		mainPanel.add(layeredPanel,BorderLayout.CENTER);
 
 		layeredPanel.addMouseListener(new MouseListener()
 		{
 			@Override
 			public void mouseClicked(MouseEvent arg0) 
 			{
 				God.vertexPanel.mouseClicked(arg0);		
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent arg0) 
 			{
 				God.vertexPanel.mouseEntered(arg0);		
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) 
 			{
 				God.vertexPanel.mouseExited(arg0);		
 			}
 
 			@Override
 			public void mousePressed(MouseEvent arg0) 
 			{
 				God.vertexPanel.mousePressed(arg0);		
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent arg0) 
 			{
 				God.vertexPanel.mouseReleased(arg0);		
 			}});
 
 		layeredPanel.addMouseMotionListener(new MouseMotionListener()
 		{
 			@Override
 			public void mouseDragged(MouseEvent arg0)
 			{
 				God.vertexPanel.mouseDragged(arg0);
 			}
 
 			@Override
 			public void mouseMoved(MouseEvent arg0)
 			{
 				God.vertexPanel.mouseMoved(arg0);		
 			}
 		});
 
 		this.pack();
 		this.setVisible(true);
 	}
 
 	/**
 	 * Runs the program
 	 * @param argv path to an image
 	 */
 	public static void main(String argv[]) 
 	{
 		//creates a window and display the image
 		try 
 		{
 			new MainWindow();
 		} 
 		catch (Exception e) 
 		{
 			e.printStackTrace();
 		}
 	}
 
 	private class MyDispatcher implements KeyEventDispatcher 
 	{
 		@Override
 		public boolean dispatchKeyEvent(KeyEvent e) 
 		{
 			if (e.getID() == KeyEvent.KEY_TYPED) 
 			{
 				// Complete polygon
 				if (e.getKeyChar() == KeyEvent.VK_ENTER & God.mainWindow.isFocused() ) 
 				{			
 					if(God.shapeData.getShapes().size() != 0)
 					{
 						Shape lastShape = God.shapeData.getLastShape();
 						if(lastShape.size() > 2)
 						{
 							int temp =  God.lastVertex;
 							God.lastVertex = -1;
 							if(God.requestLabel())
 							{
 								lastShape = God.shapeData.endShape();
 
 								if (lastShape != null) 
 								{
 									BufferedImage screenshot = imagePanel.getScreenshot();
 									Rectangle r = lastShape.getBoundingBox();
 									lastShape.setThumbnail(screenshot.getSubimage(r.x,r.y,r.width,r.height));
 
 									God.vertexPanel.drawLine(lastShape.get(lastShape.size() - 2), lastShape.get(0), lastShape.getColor());
 									God.shapeData.addShape(new Shape());	
 								}
 							}
 							else
 							{
 								God.lastVertex = temp;
 								return false;
 							}
 						}
 						else
 						{
 
 							if(lastShape.size() > 0)
 							{
 								JOptionPane.showMessageDialog(null, "Polygon requires at least 3 vertices!");
 							}
 						}
 					}
 				}
 
 				// Delete vertex
 				if (e.getKeyChar() == KeyEvent.VK_DELETE || e.getKeyChar() == KeyEvent.VK_BACK_SPACE ) 
 				{
 					if(God.moveVertex != null)
 					{
 						if(God.shapeData.getShape(God.moveVertex.getShape()).size() > 0)
 						{
 							// If shape is complete
 							if(God.shapeData.getShape(God.moveVertex.getShape()).complete())
 							{
 								// Delete existing vertex of polygon
 								// Must account for head being twice
 								if(God.shapeData.getShape(God.moveVertex.getShape()).size() > 4)
 								{
 									// delete vertex
 									God.shapeData.shapes.get(God.moveVertex.getShape()).remove(God.moveVertex.getVertex());
 									// If vertex is head, must remove tail
 									if(God.moveVertex.getVertex() == 0)
 									{
 										God.shapeData.shapes.get(God.moveVertex.getShape()).
 										remove(God.shapeData.shapes.get(God.moveVertex.getShape()).size() - 1);
 										// Push new head to tail
 										God.shapeData.shapes.get(God.moveVertex.getShape()).
 										add(God.shapeData.shapes.get(God.moveVertex.getShape()).getHead());
 									}
 									God.moveVertex = null;
 									God.layeredPanel.paint(God.layeredPanel.getGraphics());
 								}
 								else
 								{
 									// Show error message
 									JOptionPane.showMessageDialog(null, "A label must be at least 3 vertices! TODO Delete entire label?");
 								}
 							}
 						}
 					}
 					else if (God.lastVertex >= 0)
 					{
 						ArrayList<Shape> shapes = God.shapeData.getShapes();
 						System.out.println("shapes.size(): " + shapes.size());
 						Shape lastShape = shapes.get(shapes.size() - 1);
 						lastShape.remove(God.lastVertex);
 						God.layeredPanel.paint(God.layeredPanel.getGraphics());
 						God.lastVertex = -1;
 					}
 				}
 			}
 			return false;
 		}
 	}
 
 	ActionListener color()
 	{
 		return new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent arg0) 
 			{
				if (ShapeList.list.getSelectedIndex() >= 0)
 				{
					God.shapeData.listSelection = ShapeList.list.getSelectedIndex();
 					God.shapeData.getShape(God.shapeData.listSelection).setColor(ColorEnum.getColor(arg0.getActionCommand()));
 					God.layeredPanel.paint(God.layeredPanel.getGraphics());
 				}
 				else
 				{
 					God.shapeData.setColor(ColorEnum.getColor(arg0.getActionCommand()));
 					God.color = ColorEnum.getColor(arg0.getActionCommand());
 					God.layeredPanel.paint(God.layeredPanel.getGraphics());
 				}
 			}};
 	}
 
 }
