 package net.foxycorndog.jfoxylib.components;
 
 import java.util.ArrayList;
 
 import net.foxycorndog.jfoxylib.Color;
 import net.foxycorndog.jfoxylib.opengl.GL;
 import net.foxycorndog.jfoxylib.opengl.bundle.Bundle;
 
 /**
  * Class that is used to contain other Components.
  * 
  * @author	Braden Steffaniak
  * @since	Mar 10, 2013 at 12:28:06 AM
  * @since	v0.1
  * @version Mar 10, 2013 at 12:28:06 AM
  * @version	v0.2
  */
 public class Panel extends Component
 {
 	private					boolean					independentSize;
 	
 	private					float					backgroundIndex;
 	
 	private					Color					backgroundColor;
 	
 	private					ArrayList<Component>	children;
 	
 	private	static	final	Bundle					backgroundBundle;
 	
 	/**
 	 * Initialize the backgroundBundle.
 	 */
 	static
 	{
 		backgroundBundle = new Bundle(3 * 2, 2, true, false);
 		
 		backgroundBundle.beginEditingVertices();
 		{
 			backgroundBundle.addVertices(GL.genRectVerts(0, 0, 1, 1));
 		}
 		backgroundBundle.endEditingVertices();
 		
 		backgroundBundle.beginEditingTextures();
 		{
 			backgroundBundle.addTextures(GL.WHITE.getImageOffsets());
 		}
 		backgroundBundle.endEditingTextures();
 	}
 	
 	/**
 	 * Construct a Panel that is used to hold Components with the
 	 * specified parent Panel.
 	 * 
 	 * @param parent The parent Panel of this Panel.
 	 */
 	public Panel(Panel parent)
 	{
 		super(parent);
 		
 		children = new ArrayList<Component>();
 	}
 	
 	/**
 	 * Get whether or not the size of the Panel is independent to
 	 * the scale that the OpenGL matrix has at the rendering time.
 	 * 
 	 * @return Whether or not the size of the Panel is independent to
 	 * 		scale at render time.
 	 */
 	public boolean isIndependentSize()
 	{
 		return independentSize;
 	}
 	
 	/**
 	 * Set whether or not the size of the Panel should be independent to
 	 * the scale that the OpenGL matrix has at the rendering time.
 	 * 
 	 * @param independentSize Whether or not to set the size of the Panel
 	 * 		to be independent to scale at render time.
 	 */
 	public void setIndependentSize(boolean independentSize)
 	{
 		this.independentSize = independentSize;
 	}
 	
 	/**
 	 * Get the z-index of the Background.
 	 * 
 	 * @return The z-index of the Background;
 	 */
 	public float getBackgroundIndex()
 	{
 		return backgroundIndex;
 	}
 	
 	/**
 	 * Set the z-index of the background.
 	 * 
 	 * @param backgroundIndex The z-index of the background.
 	 */
 	public void setBackgroundIndex(float backgroundIndex)
 	{
 		this.backgroundIndex = backgroundIndex;
 	}
 	
 	/**
 	 * Get the Color that is rendered as the background.
 	 * 
 	 * @return The Color instance that is rendered as the background.
 	 */
 	public Color getBackgroundColor()
 	{
 		return backgroundColor;
 	}
 	
 	/**
 	 * Set the Color that the background will render in.
 	 * 
 	 * @param color The Color to set as the background Color.
 	 */
 	public void setBackgroundColor(Color color)
 	{
 		this.backgroundColor = color;
 	}
 	
 	/**
 	 * Add the specified Component as a child to this Panel. This removes
 	 * any previous links to parents from the child.
 	 * 
 	 * @param child The child Component to add to this Panel.
 	 */
 	public boolean addChild(Component child)
 	{
 		child.setParent(this);
 		
 		if (children.contains(child))
 		{
 			return false;
 		}
 		
 		return children.add(child);
 	}
 	
 	/**
 	 * Dispose this Panel and all of its children from the Listeners.
 	 * 
 	 * @return Whether it was successfully disposed.
 	 */
 	public boolean dispose()
 	{
 		boolean disposed = super.dispose();
 		
 		for (int i = 0; i < children.size(); i++)
 		{
 			children.get(i).dispose();
 		}
 		
 		return disposed;
 	}
 	
 	/**
 	 * Render all of the Components within the Panel to the Display.
 	 */
 	public void renderComponents()
 	{
 		for (int i = children.size() - 1; i >= 0; i--)
 		{
 			Component child = children.get(i);
 			
 			if (child.isVisible())
 			{
 				child.render();
 			}
 		}
 	}
 	
 	/**
 	 * Renders the Component to the screen.
 	 */
 	public void render()
 	{
 		if (isVisible())
 		{
 			GL.pushMatrix();
 			{
 				if (independentSize)
 				{
 					GL.translateIgnoreScale(getX(), getY(), 0);
 					
 					GL.beginFrameClipping(getX(), getY(), getWidth(), getHeight());
 				}
 				else
 				{
 					GL.translate(getX(), getY(), 0);
 				
 					GL.beginClipping(0, 0, getWidth(), getHeight());
 				}
 				
 				if (backgroundColor != null)
 				{
 					GL.pushMatrix();
 					{
 						GL.translate(0, 0, backgroundIndex);
						
						if (independentSize)
						{
							GL.unscale();
						}
						
 						GL.scale(getWidth(), getHeight(), 1);
 						
 						Color current = GL.getColor();
 						
 						GL.setColor(backgroundColor);
 						backgroundBundle.render(GL.TRIANGLES, GL.WHITE);
 						
 						GL.setColor(current);
 					}
 					GL.popMatrix();
 				}
 				
 				renderComponents();
 				
 				GL.endClipping();
 			}
 			GL.popMatrix();
 		}
 	}
 }
