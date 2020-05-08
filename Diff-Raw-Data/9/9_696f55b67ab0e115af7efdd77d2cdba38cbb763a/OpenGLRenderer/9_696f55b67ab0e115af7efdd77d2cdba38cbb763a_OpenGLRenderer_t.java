 /**
  * Copyright (c) 2011-2012 Henning Funke.
  * 
  * This file is part of Battlepath.
  *
  * Battlepath is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * Battlepath is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package interaction;
 
 import editor.EditorSession;
 import engine.Tile;
 import entities.Entity;
 import entities.HealthEntity;
 import entities.Projectile;
 import entities.Tower;
 import entities.Unit;
 import fx.FxEntity;
 import fx.Particle;
 import fx.Shockwave;
 import game.Core;
 import game.GameMode;
 import game.GameSession;
 import game.HUDButton;
 import game.Session;
 import game.Team;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.IntBuffer;
 import java.util.ArrayList;
 import javax.media.opengl.*;
 import javax.media.opengl.glu.*;
 
 import util.Line2D;
 import util.Rectangle2D;
 import util.SafeList;
 import util.Vector2D;
 
 public class OpenGLRenderer implements GLEventListener {
 
 	private Session session;
 	private GameSession game=null;
 	private EditorSession editor=null;
 	private int tileSize;
 	private double scaleFactor;
 	private Vector2D offset;
 
 	
 	//shockwave shader
 	int radiusUniform;
 	int posUniform;
 	int numberUniform;		
 	
 	//only valid while display is running
 	private GL2 gl;
 	
 	public OpenGLRenderer(Session g, int tS, BFrame frame) {
 		session = g;
 		tileSize = tS;
 		
 		frame.canvas.addGLEventListener(this);
 	}
 	
 	@Override
 	public void display(GLAutoDrawable drawable) {
 		session = Core.session;
 		if(session instanceof GameSession) {
 			game = (GameSession)session;
 			editor = null;
 		}
 		if(session instanceof EditorSession) {
 			editor = (EditorSession)session;
 			game = null;
 		}
 		
 		//Draw
 		scaleFactor = tileSize * Core.view.zoom;
 		offset = Core.view.offset;
 		gl = drawable.getGL().getGL2();
 		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
 		
 		
 		drawField();
 		drawEntities();
 		gl.glEnable(GL2.GL_BLEND);
 		drawParticles();
 		drawEffects();		
 		
 		//Collision Data selection
 		gl.glColor4d(0.3,0.0,0.0, 0.5);
 		if(Core.entitySystem.selected().size() > 0) {
 			ArrayList<Line2D> cls = Core.collisionSystem.relevantData(
 					Core.entitySystem.selected().get(0).getMove());
 			for(Line2D l : cls) {
 				line(l,1);
 			}
 		}
 
 		drawHUD();
 		drawConsole();
 		gl.glDisable(GL2.GL_BLEND);
 	}
 	
 	private void drawConsole() {
 
 
 	}
 	
 	private void setupShaders(GL2 gl) {
 		int f = gl.glCreateShader(GL2.GL_FRAGMENT_SHADER);
 		
 		BufferedReader brf = null;
 		try {
 			brf = new BufferedReader(new FileReader("src/shader/shockwave.glsl"));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return;
 		}
 		String[] fsrc = {""};
 		String line;
 		try {
 			while ((line=brf.readLine()) != null) {
 			  fsrc[0] += line + "\n";
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 			return;
 		}
 		
 		int len[] = new int[1];
 		len[0] = fsrc[0].length();
 		
 		gl.glShaderSource(f, 1, fsrc, len, 0);
 		gl.glCompileShader(f);
 		
 		checkLogInfo(gl, f);
 		
 		int shaderprogram = gl.glCreateProgram();
 		gl.glAttachShader(shaderprogram, f);
 		gl.glLinkProgram(shaderprogram);
 		gl.glValidateProgram(shaderprogram);
 
 		gl.glUseProgram(shaderprogram);
 		
 		radiusUniform = gl.glGetUniformLocation(shaderprogram, "radius");
 		posUniform = gl.glGetUniformLocation(shaderprogram, "pos");
 		numberUniform = gl.glGetUniformLocation(shaderprogram, "number");		
 	}
 	
 	private void checkLogInfo(GL2 gl, int programObject) {
         IntBuffer intValue = IntBuffer.allocate(1);
         gl.glGetObjectParameterivARB(programObject, GL2.GL_OBJECT_INFO_LOG_LENGTH_ARB, intValue);
         
         int lengthWithNull = intValue.get();
 
         if (lengthWithNull <= 1) {
             return;
         }
 
         ByteBuffer infoLog = ByteBuffer.allocate(lengthWithNull);
 
         intValue.flip();
         gl.glGetInfoLogARB(programObject, lengthWithNull, intValue, infoLog);
 
         int actualLength = intValue.get();
 
         byte[] infoBytes = new byte[actualLength];
         infoLog.get(infoBytes);
         System.out.println("GLSL Validation >> " + new String(infoBytes));
     }
 	
 	private void drawParticles() {
 		ArrayList<Particle> particles = Core.particleSystem.particles;
 		
 		gl.glPointSize((float)(0.8*scaleFactor));
 		gl.glBegin(GL.GL_POINTS);
 		
 		for(FxEntity fe : particles) {
 			if(!Core.view.getScreenRect().inside(fe.pos, 5))
 				continue;
 			
 			Particle p = (Particle)fe;
 			gl.glColor4d(0.2, Math.random()*0.2, 0.01, 0.5);
 			particle(p.pos);
 		}
 		
 		gl.glEnd();
 	}
 	
 	private void drawEffects() {
 		SafeList<FxEntity> fxs = Core.particleSystem.fxEntities;
 		
 		float swPositions[] = new float[20];
 		float swRadiuses[] = new float[10];
 		int shockwaves = 0;
 		
 		for(FxEntity e : fxs) {
 			if(shockwaves >= 10) break;
 			if(e instanceof Shockwave) {
 				Shockwave sw = (Shockwave)e;
 				
 				Point screenPos = Core.view.worldToViewGL(sw.pos);
 
 				swPositions[shockwaves*2] = screenPos.x;
 				swPositions[shockwaves*2+1] = screenPos.y;
 
 				swRadiuses[shockwaves] = (float) (sw.radius*scaleFactor);
 				shockwaves++;
 			}
 		}	
 		
 		gl.glUniform1i(numberUniform, shockwaves);
 		gl.glUniform1fv(radiusUniform, swRadiuses.length, swRadiuses, 0);
 		gl.glUniform2fv(posUniform, swPositions.length, swPositions, 0);
 	}
 	
 	public void teamColor(Team team) {
 		if(team == null) {
 			gl.glColor3d(1,0,0);
 			return;
 		}
 		int i = team.color;
 		if(i==0) gl.glColor3d(1,1,1);
 		if(i==1) gl.glColor3d(1,1,0);
 		if(i==2) gl.glColor3d(0,0,1);
 	}
 	
 	private void drawEntities() {
 		for(Entity e : Core.entitySystem.entities) {
         	
 			if(!Core.view.getScreenRect().inside(e.pos, e.getRadius()))
 				continue;
 			
 			if(e instanceof Tower) {
         		Tower tower = (Tower)e;
 
         		teamColor(e.team);
         		
         		rhombus(tower.pos, 1);
         		line(tower.pos, tower.pos.add(tower.aim.scalar(1.4)), 0.05f);
         	}
         	else if(e instanceof Projectile) {
         		Projectile proj = (Projectile)e;
         		
         		double length;
         		if(proj.pos.distance(proj.origin) < proj.length) 
         			length = proj.pos.distance(proj.origin);
         		else length = proj.length;
         		gl.glColor3d(0.5,0.5,1);
         		line(proj.pos, proj.pos.subtract(proj.direction.scalar(length)), 0.1f);
         	}
         	else if(e instanceof Unit) {
         		Unit u = (Unit)e;
         		
            		teamColor(e.team);
         		circle(u.pos, u.getRadius());
         		
         		if(game == null) continue;
         		
         		if(u.swarm != null && game.mode == GameMode.ACTION) {
         			gl.glEnable(GL2.GL_BLEND);
         			gl.glColor3d(0.3,0,0);
         			circle(u.pos, u.getRadius()+0.2);
         			gl.glDisable(GL2.GL_BLEND);
         		}
         		else if(u.isSelected && game.mode == GameMode.STRATEGY) {
         			gl.glColor3d(1, 0, 0);
         			line(u.pos, u.pos.add(new Vector2D(u.getRadius(), u.getRadius())), 0.1f);
         			line(u.pos, u.pos.add(new Vector2D(-u.getRadius(), u.getRadius())), 0.1f);
         			line(u.pos, u.pos.add(new Vector2D(u.getRadius(), -u.getRadius())), 0.1f);
         			line(u.pos, u.pos.add(new Vector2D(-u.getRadius(), -u.getRadius())), 0.1f);
         		}
         		if(u.path != null && u.path.size() > 3) {
         			gl.glEnable(GL2.GL_BLEND);
         			gl.glColor3d(0,.2,0);
         			line(u.pos, u.path.get(u.path.size()-1), 10f);
         			gl.glDisable(GL2.GL_BLEND);
         		}
         			
  
         	}
 
         }
 	}
 
 	private void drawField() {
 		gl.glBegin(GL2.GL_QUADS);
 		
 		Rectangle2D screen = Core.view.getScreenRect();
 		Point topleft = Core.field.tileIndexAt(screen.topleft);
 		Point bottomright = Core.field.tileIndexAt(screen.bottomright);
 		Tile[][] tiles = Core.field.getTiles();
 		
 		
 		Core.field.clamp(topleft);
 		Core.field.clamp(bottomright);
 		
 		for(int x=topleft.x; x <= bottomright.x; x++) {
 			for(int y=bottomright.y; y <= topleft.y; y++) {
 				int tileValue = tiles[x][y].getValue();
				gl.glColor3d((double)x/Core.field.getTilesX(), (double)x/Core.field.getTilesX(), (double)y/Core.field.getTilesY());
 				if(tileValue == 1) {
 					tile(new Vector2D(x+0.5,y+0.5));
 				} else if(tileValue > 1 && tileValue < 6)
 					this.polygonLines(tiles[x][y].getCollisionModel(), true);
 			}
 		}
 
 		gl.glEnd();
 	}
 	
 	private void drawHUD() {
 		//Cursor
 		Point cursor = Core.input.viewCursorPos;
 		gl.glLineWidth(2);
 		gl.glBegin(GL2.GL_LINES);
 		gl.glColor3d(0,1,0);
 		gl.glVertex2d(cursor.x, Core.view.windowSize.height-cursor.y-10);
 		gl.glVertex2d(cursor.x, Core.view.windowSize.height-cursor.y+10);
 		gl.glVertex2d(cursor.x-10, Core.view.windowSize.height-cursor.y);
 		gl.glVertex2d(cursor.x+10, Core.view.windowSize.height-cursor.y);
 		gl.glEnd();
 		
 		if(editor != null) {
 			circle(Core.input.getCursorPos(),editor.getBrush().size,false);
 			polygonLines(Core.field.getBoundingFrame(), false);
 			
 			ArrayList<Tile> edits = editor.getBrush().getPaint(Core.field, Core.input.getCursorPos());
 			
 			for(Tile t : edits) {
 				ArrayList<Line2D> draw = t.getCollisionModel();
 				if(draw.size() == 0)
 					draw = Core.field.tileAt(t.getIndex()).getCollisionModel();
 				
 				if(draw.size() > 1)
 					polygonLines(draw,false);
 			}
 			
 			
 		}
 		
 		
 		
 		//Selection Rectangle
 		if(Core.selectionRect != null) {
 			Rectangle2D sel = Core.selectionRect;
 			gl.glColor3d(0.1,0.1,0.2);
 			rectangle(sel.topleft, sel.bottomright);
 		}
 		
 		gl.glDisable(GL2.GL_BLEND);
 		
 		//HealthEntity menus
 		gl.glColor3d(1,1,1);
 		for(HealthEntity e : Core.entitySystem.visibleMenuEntities) {
 			for(HUDButton b : e.menu.buttons) {
 				if(b.mouseOver) gl.glColor3d(1,0,0);
 				circle(b.position, b.getRadius(), false);
 				if(b.mouseOver) gl.glColor3d(1,1,1);
 			}
 		}
 	}
 	
 	private void circle(Point pos, double radius, boolean filled) {
 		double angle;
 		if(filled) gl.glBegin(GL2.GL_POLYGON);
 		else gl.glBegin(GL2.GL_LINE_STRIP);
 	    for(int i = 20; i >= 0; i--) {
 	        angle = i * 2 * Math.PI / 20;
         	gl.glVertex2d(pos.x + (Math.cos(angle) * radius), 
         			(+ pos.y + (Math.sin(angle) * radius)));
 	    }
 	    gl.glEnd();
 	}
 	
 	private void circle(Vector2D pos, double radius, boolean filled) {
 		double angle;
 		if(filled) gl.glBegin(GL2.GL_POLYGON);
 		else gl.glBegin(GL2.GL_LINE_STRIP);
 	    for(int i = 20; i >= 0; i--) {
 	        angle = i * 2 * Math.PI / 20;
         	gl.glVertex2d((offset.x + pos.x + (Math.cos(angle) * radius)) * scaleFactor, 
         			(offset.y + pos.y + (Math.sin(angle) * radius)) * scaleFactor);
 	    }
 	    gl.glEnd();
 	}
 	
 	private void circle(Vector2D pos, double radius) {
 		circle(pos,radius,true);
 	}
 	
 	private void particle(Vector2D pos) {
 		gl.glVertex2d(scaleFactor * (offset.x + pos.x), scaleFactor * (pos.y + offset.y));
 	}
 	
 	private void line(Vector2D a, Vector2D b, float width) {
 		gl.glLineWidth(width*(float)scaleFactor);
 		gl.glBegin(GL2.GL_LINES);
 		gl.glVertex2d((a.x()+offset.x)*scaleFactor, (a.y()+offset.y)*scaleFactor);
 		gl.glVertex2d((b.x()+offset.x)*scaleFactor, ((b.y()+offset.y)*scaleFactor));
 		gl.glEnd();
 	}
 	
 	private void line(Line2D line, float width) {
 		line(line.a,line.b,width);
 	}
 	
 	private void rhombus(Vector2D pos, double edgeLength) {
 		edgeLength /= 2;
 		gl.glBegin(GL2.GL_QUADS);
 		gl.glVertex2d((pos.x-edgeLength+offset.x)*scaleFactor, (pos.y+offset.y)*scaleFactor);
 		gl.glVertex2d((pos.x+offset.x)*scaleFactor, (pos.y-edgeLength+offset.y)*scaleFactor);
 		gl.glVertex2d((pos.x+edgeLength+offset.x)*scaleFactor, (pos.y+offset.y)*scaleFactor);
 		gl.glVertex2d((pos.x+offset.x)*scaleFactor, (pos.y+edgeLength+offset.y)*scaleFactor);
 		gl.glEnd();
 	}
 	
 	private void square(Vector2D pos, double edgeLength) {
 		edgeLength /= 2;
 		gl.glBegin(GL2.GL_QUADS);
 		gl.glVertex2d((pos.x-edgeLength+offset.x)*scaleFactor, (pos.y+edgeLength+offset.y)*scaleFactor);
 		gl.glVertex2d((pos.x+edgeLength+offset.x)*scaleFactor, (pos.y+edgeLength+offset.y)*scaleFactor);
 		gl.glVertex2d((pos.x+edgeLength+offset.x)*scaleFactor, (pos.y-edgeLength+offset.y)*scaleFactor);
 		gl.glVertex2d((pos.x-edgeLength+offset.x)*scaleFactor, (pos.y-edgeLength+offset.y)*scaleFactor);
 		gl.glEnd();
 	}
 	
 	private void rectangle(Vector2D topleft, Vector2D bottomright) {
 		gl.glBegin(GL2.GL_QUADS);
 		gl.glVertex2d((topleft.x+offset.x)*scaleFactor, (topleft.y+offset.y)*scaleFactor);
 		gl.glVertex2d((topleft.x+offset.x)*scaleFactor, (bottomright.y+offset.y)*scaleFactor);
 		gl.glVertex2d((bottomright.x+offset.x)*scaleFactor, (bottomright.y+offset.y)*scaleFactor);
 		gl.glVertex2d((bottomright.x+offset.x)*scaleFactor, (topleft.y+offset.y)*scaleFactor);
 		gl.glEnd();
 	}
 	
 	private void tile(Vector2D pos) {
 		gl.glBegin(GL2.GL_QUADS);
 		gl.glVertex2d((pos.x-0.5+offset.x)*scaleFactor, (pos.y+0.5+offset.y)*scaleFactor);
 		gl.glVertex2d((pos.x+0.5+offset.x)*scaleFactor, (pos.y+0.5+offset.y)*scaleFactor);
 		gl.glVertex2d((pos.x+0.5+offset.x)*scaleFactor, (pos.y-0.5+offset.y)*scaleFactor);
 		gl.glVertex2d((pos.x-0.5+offset.x)*scaleFactor, (pos.y-0.5+offset.y)*scaleFactor);
 		gl.glEnd();
 	}
 	
 	private void polygonLines(ArrayList<Line2D> lines, boolean filled) {
 		if(filled) gl.glBegin(GL2.GL_POLYGON);
 		else gl.glBegin(GL2.GL_LINE_STRIP);
 		for(Line2D line : lines) {
 			gl.glVertex2d((line.a.x+offset.x)*scaleFactor, (line.a.y+offset.y)*scaleFactor);
 		}
 		gl.glVertex2d((lines.get(lines.size()-1).b.x+offset.x)*scaleFactor, 
 				(lines.get(lines.size()-1).b.y+offset.y)*scaleFactor);
 		gl.glEnd();
 	}
 
 	private void polygonVectors(ArrayList<Vector2D> vectors, boolean filled) {
 		if(filled) gl.glBegin(GL2.GL_POLYGON);
 		else gl.glBegin(GL2.GL_LINE_STRIP);
 		for(Vector2D vec : vectors) {
 			gl.glVertex2d((vec.x+offset.x)*scaleFactor, (vec.y+offset.y)*scaleFactor);
 		}
 		gl.glEnd();
 	}
 	
 	@Override
 	public void dispose(GLAutoDrawable drawable) {
 		//Clean up
 
 	}
 
 	@Override
 	public void init(GLAutoDrawable drawable) {
 		//Initialize
 		GL2 gl = drawable.getGL().getGL2();
 		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
 		gl.glMatrixMode(GL2.GL_PROJECTION);
 		gl.glLoadIdentity();
 		
 		gl.glBlendFunc(GL2.GL_ONE,GL2.GL_ONE);
 		
 		setupShaders(gl);
 		initTextRender();
 	}
 	
 	public void initTextRender() {
 
 	
 	}
 
 	@Override
 	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
 		//Resize window
 		GL2 gl = drawable.getGL().getGL2();
 		gl.glLoadIdentity();
 		GLU glu = new GLU();
 		gl.glViewport(0, 0, width, height);
 		glu.gluOrtho2D(0, width, 0, height);
 		Core.view.windowSize = new Dimension(width, height);
 	}
 
 
 }
