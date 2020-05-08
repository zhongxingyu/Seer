 package com.hexcore.cas.ui.toolkit;
 
 import javax.media.opengl.GL;
 
 import com.hexcore.cas.math.Vector2f;
 import com.hexcore.cas.math.Vector2i;
 import com.hexcore.cas.model.Cell;
 import com.hexcore.cas.model.HexagonGrid;
 
 public class HexagonGrid3DWidget extends Grid3DWidget
 {
 	public HexagonGrid3DWidget(Vector2i size, HexagonGrid grid, int cellSize)
 	{
 		super(size, grid, cellSize);
 	}
 
 	@Override
 	public void loadGeometry(GL gl)
 	{        
 		float	s = cellSize;
 		float	h = cellSize / 2;
 		float	r = cellSize * (float)Math.cos(30.0 * Math.PI / 180.0);
 			
 		Vector2f[]	hexagon = new Vector2f[6];
 		hexagon[0] = new Vector2f(0.0f,	r);
 		hexagon[1] = new Vector2f(h,	r+r);
 		hexagon[2] = new Vector2f(h+s,	r+r);
 		hexagon[3] = new Vector2f(h+s+h,r);
 		hexagon[4] = new Vector2f(h+s,	0.0f);
 		hexagon[5] = new Vector2f(h,	0.0f);
 		
 		resetVertexBuffer(gl, 6);
 		
 		for (int y = 0; y < grid.getHeight(); y++)
 			for (int x = 0; x < grid.getWidth(); x++)
 			{
 				Cell 		cell = grid.getCell(x, y);
 				Vector2f	p = new Vector2f(x*(s+h), y*r*2);
 				
				if ((x & 1) == 0) p.inc(0, r);
 				
 				addColumn(p, cell, hexagon);
 			}
 		
 		loadVertexBuffer(gl);
 	}
 }
