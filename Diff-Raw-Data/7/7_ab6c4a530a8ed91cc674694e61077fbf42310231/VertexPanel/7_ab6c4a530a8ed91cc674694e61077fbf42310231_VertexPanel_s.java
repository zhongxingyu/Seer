 package main;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import data.MoveVertex;
 import data.Shape;
 import data.ShapeData;
 import data.Vertex;
 
 public class VertexPanel extends JPanel implements MouseListener, MouseMotionListener
 {
 	private static final long serialVersionUID = 1L;
 	BufferedImage image;
 	/** The maximum size of the panel */
 	Dimension size = new Dimension(800,600);
 
 	public VertexPanel()
 	{
 		addMouseListener(this);
 		addMouseMotionListener(this);
 		this.setVisible(true);
 		this.setOpaque(false);
 		this.setPreferredSize(size);
 		image = new BufferedImage(
 				(int)ImagePanel.size.getWidth(),
 				(int)ImagePanel.size.getHeight(),
 				BufferedImage.TYPE_INT_ARGB
 				);
 	}
 
 	public VertexPanel(ShapeData shapes) throws IOException 
 	{
 		this();
 	}
 
 	@Override
 	public void paintComponent(Graphics g) 
 	{
 		//		super.paintComponent(g);
 		ArrayList<Shape> shapes = God.shapeData.getShapes();
 		for (int i = 0; i < shapes.size(); i++) 
 		{
 			Shape shape = shapes.get(i);
 			drawShape(this.getGraphics(), shape);
 			if (God.shapeData.listSelection == i) {
 				System.out.println("polygonning");
 				Graphics2D g2 = (Graphics2D) this.getGraphics();
 				g2.setColor(new Color(shape.getColor().getRed(),
 						shape.getColor().getGreen(),
 						shape.getColor().getBlue(),
 						60));
 				//		g2.fillPolygon(shape.getPolygon()); // UNCOMMENT TO HIGHLIGHT TODO
 			}
 		}
 	}
 
 	/**
 	 * Draws all the vertices in the shape and draws lines between 
 	 * each adjacent vertex
 	 * @param shape
 	 */
 	private void drawShape(Graphics g, Shape shape) 
 	{
 		g.setColor(shape.getColor());
 		ArrayList<Vertex> vertices = shape.getVertices();
 		int size = vertices.size();
 
 		for (int i = 0; i < size; i++) 
 		{
 			Vertex current = vertices.get(i);
 			if (i != 0) 
 			{
 				Vertex previous = vertices.get(i - 1);
 				drawLine(current, previous, shape.getColor());
 			}
 			drawVertex(current, shape.getColor());
 		}
 	}
 
 	/**
 	 * Draws a line from the first vertex to the next
 	 * @param v1
 	 * @param v2
 	 */
 	public void drawLine(Vertex v1, Vertex v2, Color color) 
 	{
 		Graphics2D g = (Graphics2D) this.getGraphics();
 		g.setColor(color);
 		g.drawLine(v2.getX(), v2.getY(), v1.getX(), v1.getY());
 	}
 
 	/**
 	 * Draws a circle around the given vertex of the given color
 	 * @param v
 	 * @param color
 	 */
 	private void drawVertex(Vertex v, Color color) 
 	{
 		Graphics2D g = (Graphics2D) this.getGraphics();
 		g.setColor(color);
 		g.fillOval(v.getX() - v.getRadius(), v.getY() - v.getRadius(), v.getRadius()*2, v.getRadius()*2);
 	}
 
 	/***
 	 * Finds the Euclidean distance between two vertices
 	 * @param a
 	 * @param b
 	 * @return
 	 */
 	public double EuclideanDistance(Vertex a, Vertex b)
 	{
 		double x1 = a.getX();
 		double y1 = a.getY();
 		double x2 = b.getX();
 		double y2 = b.getY();
 		return Math.sqrt(( Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2))); 
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) 
 	{
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) 
 	{
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) 
 	{
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) 
 	{	
 		// Convert mouse click into a vertex
 		Vertex mouse = new Vertex(arg0.getX(), arg0.getY());
 
 		// Focus is now on current polygon
 		God.shapeData.listSelection = -1;
 
 		// Stores closest vertex and shape index (0 vertex, 1 shape)
 		int [] candidate_vertex = new int[2];
 		double distance = Double.MAX_VALUE;
 
 		// Check mouse is within image boundaries 
 		if (mouse.getX() < (God.imageDimension[0] - mouse.getRadius()) && mouse.getY() < (God.imageDimension[1] - mouse.getRadius())) 
 		{
 			// For each shape, fetch each vertex and compare to mouse click
 			ShapeData shapeData = God.shapeData;
 			for (int shapeIndex = 0; shapeIndex < shapeData.shapes.size(); shapeIndex++)
 			{
 				Shape shape = shapeData.getShape(shapeIndex);
 				for (int vertexIndex = 0; vertexIndex < shape.size(); vertexIndex++)
 				{
 					Vertex v = shape.get(vertexIndex);
 					// If distance between new vertex and mouse is shorter than current shortest distance, swap
 					if((EuclideanDistance(mouse, v) < distance) 
 							&& (EuclideanDistance(mouse, v) <= v.getRadius()))
 					{
 						candidate_vertex[0]= vertexIndex;
 						candidate_vertex[1]= shapeIndex;
 						distance = EuclideanDistance(mouse, v);
 					}
 				}
 			}
 
 
 			// If no vertex found, must be a new vertex for current polygon 
 			if(distance == Double.MAX_VALUE)
 			{
 				System.out.println("No candidate vertex found near mouse");	
 				ArrayList<Shape> shapes = God.shapeData.getShapes();
 
 				System.out.println("Point at : "+ mouse.getX() + " " + mouse.getY());
 				if(shapes.size() == 0)
 				{
 					shapeData.addShape(new Shape());
 				}
 				Shape lastShape = shapes.get(shapes.size() - 1);
 				drawVertex(mouse, lastShape.getColor());
 				if (lastShape.size() != 0) 
 				{
 					drawLine(mouse, lastShape.get(lastShape.size()-1), lastShape.getColor());
 				}
 				lastShape.add(mouse);
 				God.lastVertex = lastShape.size()-1;
 				System.out.println("God.lastVertex " + God.lastVertex);
 				God.dirtyFlag = true;
 				return;
 			}
 
 
 			if (shapeData.getShape(candidate_vertex[1]).complete())
 			{
 				System.out.println("I'm complete");
 				System.out.println(shapeData.getShape(candidate_vertex[1]).size() + " " + candidate_vertex[1]);
 				// Vertex is start (and end) vertex, force vertex to be head
 				// This is for moving the start and end vertices together (seemless to user)
 				if(shapeData.getShape(candidate_vertex[1]).get(candidate_vertex[0]).
 						equals(shapeData.getShape(candidate_vertex[1]).getHead()))
 				{
 					System.out.println("THEY ARE EQUAL");
 					candidate_vertex[0] = 0;
 				}
 			}
 			else
 			{
 				// Current shape is not complete, must complete it
 				Shape lastShape = shapeData.getShape(candidate_vertex[1]);
 
 				if(shapeData.getShape(candidate_vertex[1]).get(candidate_vertex[0]).
 						equals(lastShape.getHead()))
 				{
 					if(lastShape.size() > 2)
 					{
 						int temp =  God.lastVertex;
 						God.lastVertex = -1;
 						if(God.requestLabel())
 						{
 							lastShape = God.shapeData.endShape();
 
 							if (lastShape != null) 
 							{
 								BufferedImage screenshot = God.imagePanel.getScreenshot();
 								Rectangle r = lastShape.getBoundingBox();
 								lastShape.setThumbnail(screenshot.getSubimage(r.x,r.y,r.width,r.height));
 
 								God.vertexPanel.drawLine(lastShape.get(lastShape.size() - 2), lastShape.get(0), lastShape.getColor());
 								God.shapeData.addShape(new Shape());	
 							}
 						}
 						else
 						{
 							God.lastVertex = temp;
 							return;
 						}
 					}
 					else
 					{
 						JOptionPane.showMessageDialog(null, "Polygon requires at least 3 vertices!");
 					}
 				}
 			}
 
 			God.moveVertex = new MoveVertex(candidate_vertex);
 
 		}
 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) 
 	{
 		if (God.moveVertex != null)
 		{
 			Shape shape = God.shapeData.getShape(God.moveVertex.getShape());
 			BufferedImage screenshot = God.imagePanel.getScreenshot();
 			Rectangle r = shape.getBoundingBox();
 			shape.setThumbnail(screenshot.getSubimage(r.x,r.y,r.width,r.height));
 			God.layeredPanel.paint(God.layeredPanel.getGraphics());
 			God.lastVertex = -1;
 			God.shapeList.repaint();
 		}
 		God.moveVertex = null;
 		God.dirtyFlag = true;
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e) 
 	{
 		// Drag and move individual vertex
 		if(God.moveVertex != null)
 		{
 			Vertex mouse = new Vertex(e.getX(), e.getY());
 			if (mouse.getX() < (God.imageDimension[0] - mouse.getRadius()) && mouse.getY() < (God.imageDimension[1] - mouse.getRadius())) 
 			{
 
 				// If shape is complete, and if vertex is head, must move tail too 
 				if(God.moveVertex.getVertex() == 0 && God.shapeData.getShape(God.moveVertex.getShape()).complete())
 				{
 					// Remove vertex from shape and add new one with current mouse location
 					God.shapeData.shapes.get(God.moveVertex.getShape()).remove(God.moveVertex.getVertex());
 					God.shapeData.shapes.get(God.moveVertex.getShape()).addAt(God.moveVertex.getVertex(), mouse);
 					God.shapeData.shapes.get(God.moveVertex.getShape()).remove(God.shapeData.shapes.get(God.moveVertex.getShape()).size() - 1);
 					God.shapeData.shapes.get(God.moveVertex.getShape()).add(mouse);
 				}
 				else
 				{
 					// Remove vertex from shape and add new one with current mouse location
 					God.shapeData.shapes.get(God.moveVertex.getShape()).remove(God.moveVertex.getVertex());
 					God.shapeData.shapes.get(God.moveVertex.getShape()).addAt(God.moveVertex.getVertex(), mouse);
 				}
 				God.layeredPanel.paint(this.getGraphics());
 			}
 		}
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) 
 	{
 	}
 
 }
