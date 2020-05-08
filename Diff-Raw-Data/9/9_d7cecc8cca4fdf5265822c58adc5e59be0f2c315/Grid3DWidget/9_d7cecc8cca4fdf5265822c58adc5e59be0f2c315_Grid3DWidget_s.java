 package com.hexcore.cas.ui;
 
 import java.awt.event.KeyEvent;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.util.ArrayList;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 import javax.media.opengl.fixedfunc.GLMatrixFunc;
 import javax.media.opengl.glu.GLU;
 
 import com.hexcore.cas.math.Vector2f;
 import com.hexcore.cas.math.Vector2i;
 import com.hexcore.cas.math.Vector3f;
 import com.hexcore.cas.model.Cell;
 import com.hexcore.cas.model.ColourRuleSet;
 import com.hexcore.cas.model.Grid;
 import com.jogamp.common.nio.Buffers;
 
 public class Grid3DWidget<T extends Grid> extends GridWidget<T>
 {
 	public class Slice
 	{
 		public int		colourProperty;
 		public int		heightProperty;
 		public float	scale;
 		
 		public Slice(int heightProperty, float scale) 
 		{
 			this.colourProperty = -1;
 			this.heightProperty = heightProperty;
 			this.scale = scale;
 		}		
 		
 		public Slice(int colourProperty, int heightProperty, float scale) 
 		{
 			this.colourProperty = colourProperty;
 			this.heightProperty = heightProperty;
 			this.scale = scale;
 		}
 	}
 	
 	// Options
 	protected ArrayList<Slice>	slices;
 	
 	protected boolean	drawGrid = true;
 	
 	// Camera
 	protected float		yaw = 0.0f, pitch = 30.0f;
 	protected Vector3f	cameraPosition;
 	protected boolean	cameraMoving = false;
 	protected Vector2i	cameraMoveStart = new Vector2i();
 	
 	// Buffer for 3D drawing
 	protected IntBuffer		buffers = null;
 	protected int			numVertices = 0;
 	protected int			numLinePoints = 0;
 	protected FloatBuffer	vertexBufferData = null;
 	protected FloatBuffer	colourBufferData = null;
 	protected FloatBuffer	normalBufferData = null;
 	protected FloatBuffer	lineBufferData = null;
 	protected boolean		dirty = true;
 	
 	public Grid3DWidget(Vector2i size, T grid, int tileSize)
 	{
 		this(new Vector2i(), size, grid, tileSize);
 	}
 
 	public Grid3DWidget(Vector2i position, Vector2i size, T grid, int tileSize)
 	{
 		super(position, size, grid, tileSize);
 		cameraPosition = new Vector3f(grid.getWidth() * tileSize / 2.0f, grid.getHeight() * tileSize / 2.0f, 200);
 		slices = new ArrayList<Slice>();
 		
 		buffers = IntBuffer.allocate(4);
 	}
 	
 	@Override
 	public boolean canGetFocus() {return true;}
 	
 	@Override
 	public void setColourProperty(int propertyIndex)
 	{
 		colourProperty = propertyIndex;
 		dirty = true;
 	}
 
 	@Override
 	public void setGrid(T grid)
 	{
 		this.grid = grid;
 		dirty = true;
 	}
 
 	@Override
 	public void setColourRuleSet(ColourRuleSet ruleSet)
 	{
 		colourRules = ruleSet;
 		dirty = true;
 	}
 
 	public void setDrawGrid(boolean state)
 	{
 		drawGrid = state;
 		dirty = true;
 	}
 	
 	public void addSlice(int heightProperty, float scale)
 	{
 		slices.add(new Slice(heightProperty, scale));
 		dirty = true;
 	}	
 	
 	public void addSlice(int colourProperty, int heightProperty, float scale)
 	{
 		slices.add(new Slice(colourProperty, heightProperty, scale));
 		dirty = true;
 	}
 	
 	public void clearSlices()
 	{
 		slices.clear();
 		dirty = true;
 	}
 	
 	@Override
 	public void update(Vector2i position, float delta)
 	{
 		float speed = (float)Math.sqrt(cameraPosition.z) * 6.0f;
 		if (speed < 10.0f) speed = 10.0f;
 		
 		Vector2f posDelta = new Vector2f(0.0f, 0.0f);
 		float	 sinYaw = (float)Math.sin(yaw * Math.PI / 180.0f);
 		float	 cosYaw = (float)Math.cos(yaw * Math.PI / 180.0f);
 		
 		if (window.getKeyState(KeyEvent.VK_UP))
 		{
 			posDelta.x -= sinYaw;
 			posDelta.y -= cosYaw;
 		}
 		if (window.getKeyState(KeyEvent.VK_DOWN))
 		{
 			posDelta.x += sinYaw;
 			posDelta.y += cosYaw;
 		}
 		if (window.getKeyState(KeyEvent.VK_LEFT))
 		{
			posDelta.x -= sinYaw;
			posDelta.y += cosYaw;
 		}
 		if (window.getKeyState(KeyEvent.VK_RIGHT))
 		{
			posDelta.x += sinYaw;
			posDelta.y -= cosYaw;
 		}
 		
 		if ((posDelta.x != 0.0f) || (posDelta.y != 0.0f))
 		{
 			posDelta.normalise();
 			cameraPosition.inc(posDelta.x * speed * delta, posDelta.y * speed * delta, 0.0f);
 		}
 	}
 	
 	@Override
 	public void render(GL gl, Vector2i position)
 	{
 		Vector2i pos = this.position.add(position);
 		
 		Graphics.renderRectangle(gl, pos, size, backgroundColour);
 		window.setViewport(gl, pos, size);
 		
 		GL2 	gl2 = gl.getGL2();
 		GLU		glu = new GLU();
 		
         gl2.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
         gl2.glLoadIdentity();
         glu.gluPerspective(45.0f, (float)size.x / size.y, 16.0f, 10240.0f);
         gl2.glScaled(1.0f,-1.0f, 1.0f);
         gl2.glRotatef(pitch, 1.0f, 0.0f, 0.0f);
         gl2.glRotatef(yaw, 0.0f, 0.0f, 1.0f);
         gl2.glTranslatef(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
         gl2.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
         gl2.glLoadIdentity();
         
         gl2.glEnable(GL2.GL_LIGHTING);
         gl2.glEnable(GL2.GL_LIGHT0);
         gl2.glEnable(GL2.GL_COLOR_MATERIAL);
         gl2.glEnable(GL.GL_DEPTH_TEST);
         
         gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, (new Vector3f(0.25f, 0.5f, 1.0f)).toFloatBuffer(0.0f));
         gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, Colour.WHITE.toFloatBuffer());
 
 		if (dirty) loadGeometry(gl);
 		dirty = false;
 		
 		renderVertexBuffer(gl);
 		
 		window.resetView(gl2);
 		gl2.glDisable(GL2.GL_LIGHTING);
 		gl2.glDisable(GL.GL_DEPTH_TEST);
 	}
 	
 	public void loadGeometry(GL gl)
 	{
 	
 	}
 	
 	@Override
 	public boolean handleEvent(Event event, Vector2i position)
 	{
 		if ((event.type == Event.Type.MOUSE_CLICK) && event.pressed)
 		{
 			window.requestFocus(this);
 			cameraMoveStart.set(event.position);
 			cameraMoving = true;
 			return true;
 		}
 		else if ((event.type == Event.Type.LOST_FOCUS) || ((event.type == Event.Type.MOUSE_CLICK) && !event.pressed))
 		{
 			if (cameraMoving)
 			{
 				cameraMoving = false;
 				return true;
 			}
 		}
 		else if (cameraMoving && (event.type == Event.Type.MOUSE_MOTION))
 		{
 			yaw -= event.position.x - cameraMoveStart.x;
 			
 			pitch -= event.position.y - cameraMoveStart.y;
 			pitch = Math.max(Math.min(pitch, 180.0f), 0.0f);
 			
 			cameraMoveStart.set(event.position);
 			return true;
 		}
 		else if (event.type == Event.Type.MOUSE_SCROLL)
 		{
 			cameraPosition.z += event.amount;
 			if (cameraPosition.z < 0.0f) cameraPosition.z = 0.001f;
 		}
 		
 		return false;
 	}
 	
 	protected int calculateBufferRequirement(int sides)
 	{
 		long	vertices = (long)((sides - 2) * 3 + sides * 6) * slices.size() * grid.getHeight() * grid.getWidth() * 3;
 		System.out.println("Allocating space for " + vertices + " vertices");
 		if (vertices < Integer.MAX_VALUE) return (int)vertices;
 		return -1;
 	}
 	
 	protected void setupVertexBuffer(GL gl, int sides)
 	{
 		gl.glGenBuffers(4, buffers);
 		
 		int	bufferSize = calculateBufferRequirement(sides);
 		
 		if (bufferSize > 0)
 		{
 			try
 			{
 				vertexBufferData = Buffers.newDirectFloatBuffer(bufferSize);
 				colourBufferData = Buffers.newDirectFloatBuffer(bufferSize);
 				normalBufferData = Buffers.newDirectFloatBuffer(bufferSize);
 				lineBufferData = Buffers.newDirectFloatBuffer(bufferSize);
 				
 				resetVertexBuffer(gl, sides);
 			}
 			catch (OutOfMemoryError oome)
 			{
 				vertexBufferData = null;
 				colourBufferData = null;
 				normalBufferData = null;
 				lineBufferData = null;
 				
 				System.err.println("Out of memory: Too many vertices, try drawing less...");
 			}
 		}
 		else
 			System.err.println("More vertices than an Integer can hold, try drawing less...");
 	}
 	
 	protected void resetVertexBuffer(GL gl, int sides)
 	{
 		if (vertexBufferData == null || colourBufferData == null || normalBufferData == null)
 		{
 			setupVertexBuffer(gl, sides);
 		}
 		else
 		{
 			vertexBufferData.rewind();
 			colourBufferData.rewind();
 			normalBufferData.rewind();
 			lineBufferData.rewind();
 			numVertices = 0;
 			numLinePoints = 0;
 		}
 	}
 	
 	protected void loadVertexBuffer(GL gl)
 	{
 		if (vertexBufferData == null) return;
 		
 		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, buffers.get(0));
 		gl.glBufferData(GL.GL_ARRAY_BUFFER, numVertices * 3 * Buffers.SIZEOF_FLOAT, vertexBufferData.position(0), GL2.GL_STREAM_DRAW);
 		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
 		
 		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, buffers.get(1));
 		gl.glBufferData(GL.GL_ARRAY_BUFFER, numVertices * 3 * Buffers.SIZEOF_FLOAT, colourBufferData.position(0), GL2.GL_STREAM_DRAW);
 		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);	
 				
 		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, buffers.get(2));
 		gl.glBufferData(GL.GL_ARRAY_BUFFER, numVertices * 3 * Buffers.SIZEOF_FLOAT, normalBufferData.position(0), GL2.GL_STREAM_DRAW);
 		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);	
 		
 		if (drawGrid)
 		{
 			gl.glBindBuffer(GL.GL_ARRAY_BUFFER, buffers.get(3));
 			gl.glBufferData(GL.GL_ARRAY_BUFFER, numLinePoints * 3 * Buffers.SIZEOF_FLOAT, lineBufferData.position(0), GL2.GL_STREAM_DRAW);
 			gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);		
 		}
 	}
 	
 	protected void renderVertexBuffer(GL gl)
 	{		
 		if (vertexBufferData == null) return;
 		
 		GL2 gl2 = gl.getGL2();
 		
 		if (drawGrid)
 		{
 			gl2.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
 			gl2.glPolygonOffset(0.5f, 0.5f);
 		}
 				
 		gl2.glEnableClientState(GL2.GL_VERTEX_ARRAY);
 		gl2.glEnableClientState(GL2.GL_COLOR_ARRAY);
 		gl2.glEnableClientState(GL2.GL_NORMAL_ARRAY);
 								
 		gl2.glBindBuffer(GL.GL_ARRAY_BUFFER, buffers.get(0));
 		gl2.glVertexPointer(3, GL.GL_FLOAT, 0, 0);
 		gl2.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
 		
 		gl2.glBindBuffer(GL.GL_ARRAY_BUFFER, buffers.get(1));
 		gl2.glColorPointer(3, GL.GL_FLOAT, 0, 0);
 		gl2.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
 		
 		gl2.glBindBuffer(GL.GL_ARRAY_BUFFER, buffers.get(2));
 		gl2.glNormalPointer(GL.GL_FLOAT, 0, 0);
 		gl2.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);		
 		
 		gl2.glDrawArrays(GL.GL_TRIANGLES, 0, numVertices);
 		gl2.glDisableClientState(GL2.GL_COLOR_ARRAY);
 		gl2.glDisableClientState(GL2.GL_NORMAL_ARRAY);
 		
 		if (drawGrid)
 		{
 			gl2.glDisable(GL2.GL_POLYGON_OFFSET_FILL);
 			
 			Graphics.applyColour(gl2, Colour.BLACK);
 			
 			gl2.glBindBuffer(GL.GL_ARRAY_BUFFER, buffers.get(3));
 			gl2.glVertexPointer(3, GL.GL_FLOAT, 0, 0);
 			gl2.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
 			
 			gl2.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
 			gl2.glDrawArrays(GL.GL_LINES, 0, numLinePoints);
 			gl2.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
 		}
 		
 		gl2.glDisableClientState(GL2.GL_VERTEX_ARRAY);	
 	}
 		
 	protected void addVertex(float x, float y, float z, Vector3f normal, Colour colour)
 	{
 		if (vertexBufferData == null) return;
 		
 		vertexBufferData.put(x);
 		vertexBufferData.put(y);
 		vertexBufferData.put(z);
 		
 		normalBufferData.put(normal.x);
 		normalBufferData.put(normal.y);
 		normalBufferData.put(normal.z);			
 		
 		colourBufferData.put(colour.r);
 		colourBufferData.put(colour.g);
 		colourBufferData.put(colour.b);		
 		numVertices++;
 	}
 	
 	protected void addLinePoint(float x, float y, float z)
 	{
 		if (lineBufferData == null) return;
 		
 		lineBufferData.put(x);
 		lineBufferData.put(y);
 		lineBufferData.put(z);
 		numLinePoints++;
 	}
 		
 	protected void add3DPolygon(Vector2f pos, Vector2f[] polygon, float startHeight, float height, Colour colour)
 	{
 		if (polygon.length <= 0) return;
 
 		Vector2f	start = polygon[0];
 		Vector3f	up = new Vector3f(0.0f, 0.0f, 1.0f);
 		
 		for (int i = 1; i < polygon.length - 1; i++) 
 		{
 			Vector2f	v1 = polygon[i];
 			Vector2f	v2 = polygon[i+1];
 
 			addVertex(pos.x+start.x, pos.y+start.y, startHeight+height, up, colour);
 			addVertex(pos.x+v1.x, pos.y+v1.y, startHeight+height, up, colour);
 			addVertex(pos.x+v2.x, pos.y+v2.y, startHeight+height, up, colour);
 		}	
 
 		if (height > 0.0f)
 			for (int i = 0; i < polygon.length; i++)
 			{
 				Vector2f f = polygon[i];
 				Vector2f s = polygon[(i == polygon.length - 1) ? 0 : i + 1];
 				Vector3f normal = new Vector3f((f.subtract(s)).getPerpendicular().getNormalised(), 0.0f);
 				
 				addVertex(pos.x+f.x, pos.y+f.y, startHeight+height, normal, colour);
 				addVertex(pos.x+s.x, pos.y+s.y, startHeight, normal, colour);
 				addVertex(pos.x+s.x, pos.y+s.y, startHeight+height, normal, colour);
 				
 				addVertex(pos.x+f.x, pos.y+f.y, startHeight+height, normal, colour);
 				addVertex(pos.x+s.x, pos.y+s.y, startHeight, normal, colour);
 				addVertex(pos.x+f.x, pos.y+f.y, startHeight, normal, colour);
 			}
 		
 		if (drawGrid)
 		{
 			for (int i = 0; i < polygon.length; i++)	
 			{
 				Vector2f f = polygon[i];
 				Vector2f s = polygon[(i == polygon.length - 1) ? 0 : i + 1];
 				
 				addLinePoint(pos.x+f.x, pos.y+f.y, startHeight+height);
 				addLinePoint(pos.x+s.x, pos.y+s.y, startHeight+height);
 			}
 			
 			if (height > 0.0f)
 			{
 				for (Vector2f v : polygon)
 				{
 					addLinePoint(pos.x+v.x, pos.y+v.y, startHeight+height);
 					addLinePoint(pos.x+v.x, pos.y+v.y, startHeight);
 				}
 			}	
 		}
 	}
 
 	protected void addColumn(Vector2f position, Cell cell, Vector2f[] shape)
 	{
 		float		startHeight = 0.0f;
 		boolean		bottom = true;
 		
 		for (Slice slice : slices)
 		{
 			Colour		colour = Colour.DARK_GREY;
 			int			colProperty = (slice.colourProperty < 0) ? colourProperty : slice.colourProperty;
 			float		height = (float)cell.getValue(slice.heightProperty) * slice.scale;
 			
 			if ((height <= 0.0f) && !bottom) continue;
 			bottom = false;
 			
 			if (colourRules != null)
 				colour = colourRules.getColour(cell, colProperty);
 			else if (cell.getValue(colourProperty) > 0) 
 				colour = Colour.LIGHT_GREY;
 											
 			add3DPolygon(position, shape, startHeight, height, colour);
 			startHeight += height;
 		}
 	}
 	
 	protected void render3DPolygon(GL gl, Vector2f pos, Vector2f[] polygon, float startHeight, float height, Colour colour)
 	{
 		GL2 gl2 = gl.getGL2();
 		height += startHeight;
 		
 		Graphics.applyColour(gl2, colour);
 				
 		if (drawGrid)
 		{
 			gl2.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
 			gl2.glPolygonOffset(0.5f, 0.5f);
 		}
 		
 		gl2.glBegin(GL.GL_TRIANGLE_FAN);
 			gl2.glNormal3f(0.0f, 0.0f, 1.0f);
 			for (Vector2f v : polygon) gl2.glVertex3f(pos.x+v.x, pos.y+v.y, height);
 		gl2.glEnd();			
 		
 		for (int i = 0; i < polygon.length; i++)
 		{
 			Vector2f f = polygon[i];
 			Vector2f s = polygon[(i == polygon.length - 1) ? 0 : i + 1];
 			Vector2f normal = (f.subtract(s)).getPerpendicular().getNormalised();
 			gl2.glBegin(GL.GL_TRIANGLE_STRIP);
 				gl2.glNormal3f(normal.x, normal.y, 0.0f);
 				gl2.glVertex3f(pos.x+f.x, pos.y+f.y, startHeight);
 				gl2.glVertex3f(pos.x+f.x, pos.y+f.y, height);
 				gl2.glVertex3f(pos.x+s.x, pos.y+s.y, startHeight);
 				gl2.glVertex3f(pos.x+s.x, pos.y+s.y, height);
 			gl2.glEnd();
 		}
 		
 		if (drawGrid) 
 		{
 			gl2.glDisable(GL2.GL_POLYGON_OFFSET_LINE);
 
 			Graphics.applyColour(gl2, Colour.BLACK);
 						
 			gl2.glBegin(GL.GL_LINE_LOOP);
 			for (Vector2f v : polygon) gl2.glVertex3f(pos.x+v.x, pos.y+v.y, height);
 			gl2.glEnd();	
 			
 			if (height > startHeight)
 			{
 				gl2.glBegin(GL.GL_LINES);
 				for (Vector2f v : polygon)
 				{
 					gl2.glVertex3f(pos.x+v.x, pos.y+v.y, startHeight);
 					gl2.glVertex3f(pos.x+v.x, pos.y+v.y, height);	
 				}
 				gl2.glEnd();
 			}
 			
 			gl2.glDisable(GL2.GL_POLYGON_OFFSET_FILL);
 		}
 	}
 	
 	protected void renderColumn(GL gl, Vector2f position, Cell cell, Vector2f[] shape)
 	{
 		float		startHeight = 0.0f;
 		boolean		bottom = true;
 		
 		for (Slice slice : slices)
 		{
 			Colour		colour = Colour.DARK_GREY;
 			int			colProperty = (slice.colourProperty < 0) ? colourProperty : slice.colourProperty;
 			float		height = (float)cell.getValue(slice.heightProperty) * slice.scale;
 			
 			if ((height <= 0.0f) && !bottom) continue;
 			bottom = false;
 			
 			if (colourRules != null)
 				colour = colourRules.getColour(cell, colProperty);
 			else if (cell.getValue(colourProperty) > 0) 
 				colour = Colour.LIGHT_GREY;
 											
 			render3DPolygon(gl, position, shape, startHeight, height, colour);
 			startHeight += height;
 		}
 	}
 }
