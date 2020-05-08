 package org.barteks2x.freemine;
 
 import java.awt.Color;
 import java.io.IOException;
 import java.nio.FloatBuffer;
 import java.text.DecimalFormat;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.barteks2x.freemine.Timer;
 import org.barteks2x.freemine.block.Block;
 import org.barteks2x.freemine.generator.ChunkGenerator;
 import org.lwjgl.BufferUtils;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.*;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.font.effects.ColorEffect;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 import org.newdawn.slick.util.ResourceLoader;
 
 import static org.lwjgl.util.glu.GLU.gluPerspective;
 
 import static org.lwjgl.opengl.GL11.*;
 
 public class FREEMine {
 
 	//OpenGL
 	private final String title;
 	private int fov;
 	private float aspectRatio;
 	private float zNear, zFar;
 	private int width, height;
 	private byte maxFPS = 60;
 	private boolean isRunning = true;
 	//Generator
 	private FloatBuffer perspectiveProjMatrix = BufferUtils.createFloatBuffer(16);
 	private FloatBuffer orthographicProjMatrix = BufferUtils.createFloatBuffer(16);
 	private ChunkGenerator chunkGenerator;
 	private Chunk chunkArray[];
 	private Map<ChunkPosition, Integer> chunkDisplayLists;
 	private int selectionDisplayList;
 	private long seed = 64646;
 	//movement
 	private Timer timer;
 	private float forwardMove = 0, sideMove = 0, upMove = 0, rX = 0, rY = 0;
 	private float playerSpeed = 0.01F;//units per milisecond
 	private float mouseSensitivity;
 	private boolean grabMouse;
 	private Player player;
 	//Textures and fonts
 	private UnicodeFont font;
 	private DecimalFormat formatter = new DecimalFormat("#.###");
 	private Texture tex;
 	//world constants
 	private int minWorldChunkX = -5;
 	private int maxWorldChunkX = 5;
 	private int minWorldChunkZ = -5;
 	private int maxWorldChunkZ = 5;
 	private int minWorldChunkY = -5;
 	private int maxWorldChunkY = 5;
 
 	public static void main(String args[]) {
 		FREEMine fm = new FREEMine();
 		fm.start(800, 600);
 	}
 
 	public FREEMine() {
 		this.title = FREEMine.class.getSimpleName() + " " + Version.getVersion();
 		int chunks = (maxWorldChunkX - minWorldChunkX) * (maxWorldChunkY - minWorldChunkY) *
 				(maxWorldChunkZ - minWorldChunkZ);
 		this.chunkArray = new Chunk[chunks];
 		chunkGenerator = new ChunkGenerator(seed);
 		timer = new Timer();
 		player = new Player();
 		mouseSensitivity = 0.6F;
 		this.chunkDisplayLists = new HashMap<ChunkPosition, Integer>(chunks);
 	}
 
 	private void start(int width, int height) {
 		this.width = width;
 		this.height = height;
 		this.fov = 60;
 		this.aspectRatio = (float)width / (float)height;
 		zNear = 0.1F;
 		zFar = 200F;
 		initDisplay();
 		initGL();
 		loadFonts();
 		loadTextures();
 		generateChunks(seed);
 		initDisplayLists();
 		while (isRunning) {
 			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 
 			timer.nextFrame();
 			input(timer.getDelta());
 
 			tex.bind();
 			glLoadIdentity();
 
 			renderChunks();
 
 			glDisable(GL_DEPTH_TEST);
 			renderSelection();
 			renderText();
 			glEnable(GL_DEPTH_TEST);
 
 			//Display.sync(maxFPS);
 			Display.update();
 			errorCheck();
 			if (Display.isCloseRequested()) {
 				isRunning = false;
 			}
 		}
 		onClose(0);
 	}
 
 	private void renderChunks() {
 
 		glRotated(player.getRy(), 1, 0, 0);
 		glRotated(player.getRx(), 0, 1, 0);
 		glTranslatef(-player.getX(), -player.getY(), -player.getZ());
 
 		for (int x = minWorldChunkX; x < maxWorldChunkX; ++x) {
 			for (int y = minWorldChunkY; y < maxWorldChunkY; ++y) {
 				for (int z = minWorldChunkZ; z < maxWorldChunkZ; ++z) {
 					glPushMatrix();
 					glTranslatef(x << 4, y << 4, z << 4);
 					glCallList(chunkDisplayLists.get(new ChunkPosition(x, y, z)));
 					glPopMatrix();
 				}
 			}
 		}
 	}
 
 	private void renderSelection() {
 		glPushMatrix();
 		glDisable(GL_BLEND);
 		BlockPosition pos = player.getSelectedBlock();
 		glTranslatef(pos.x, pos.y, pos.z);
 		glCallList(selectionDisplayList);
 		glEnable(GL_BLEND);
 		glPopMatrix();
 	}
 
 	private void renderText() {
 		glPushMatrix();
 		glMatrixMode(GL_PROJECTION);
 		glLoadMatrix(orthographicProjMatrix);
 		glMatrixMode(GL_MODELVIEW);
 
 		glLoadIdentity();
 		String x = formatter.format(player.getX());
 		String y = formatter.format(player.getY());
 		String z = formatter.format(player.getZ());
 		font.drawString(1F, 1F, new StringBuilder("FPS: ").append(timer.getFPS()).append("\n").
 				append("X: ").append(x).append("\nY: ").append(y).append("\nZ: ").append(z).
 				toString());
 
 		glMatrixMode(GL_PROJECTION);
 		glLoadMatrix(perspectiveProjMatrix);
 		glMatrixMode(GL_MODELVIEW);
 		glPopMatrix();
 	}
 
 	private void initDisplay() {
 		try {
 			Display.setTitle(title);
 			Display.setDisplayMode(new DisplayMode(width, height));
 			Display.create();
 		} catch (LWJGLException ex) {
 			Logger.getLogger(FREEMine.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 
 	private void initGL() {
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		gluPerspective(fov, aspectRatio, zNear, zFar);
 		glViewport(0, 0, width, height);
 		glMatrixMode(GL_MODELVIEW);
 
 		glGetFloat(GL_PROJECTION_MATRIX, perspectiveProjMatrix);
 
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		glOrtho(0, width, height, 0, -1, 1);
 		glGetFloat(GL_PROJECTION_MATRIX, orthographicProjMatrix);
 		glLoadMatrix(perspectiveProjMatrix);
 		glMatrixMode(GL_MODELVIEW);
 
 		glEnable(GL_TEXTURE_2D);
 		glEnable(GL_CULL_FACE);
 		glCullFace(GL_BACK);
 		glEnable(GL_DEPTH_TEST);
 
 		glEnable(GL_BLEND);
 		glEnable(GL_ALPHA_TEST);
 		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 
 		glClearColor(0.3F, 0.3F, 1F, 1F);
 
 		Mouse.setGrabbed(true);
 	}
 
 	private void generateChunks(long seed) {
 		int i = 0;
 		for (int x = minWorldChunkX; x < maxWorldChunkX; ++x) {
 			for (int y = minWorldChunkY; y < maxWorldChunkY; ++y) {
 				for (int z = minWorldChunkZ; z < maxWorldChunkZ; ++z) {
 					chunkArray[i++] = chunkGenerator.generateChunk(x, y, z);
 				}
 			}
 		}
 	}
 
 	private void initDisplayLists() {
 		timer.nextDelta();
 		for (Chunk chunk : chunkArray) {
 			int displayList;
 			if (!chunkDisplayLists.containsKey(chunk.getPosition())) {
 				displayList = glGenLists(1);
 				chunkDisplayLists.put(chunk.getPosition(), displayList);
 			} else {
 				displayList = chunkDisplayLists.get(chunk.getPosition());
 			}
 			tex.bind();
 			glNewList(displayList, GL_COMPILE);
 			glBegin(GL_QUADS);
 			for (int x = 0; x < Chunk.CHUNK_X; ++x) {
 				for (int y = 0; y < Chunk.CHUNK_Y; ++y) {
 					for (int z = 0; z < Chunk.CHUNK_Z; ++z) {
 						if (chunk.getBlockAt(x, y, z) != 0) {
 							boolean xp = false, xm = false, yp = false, ym = false, zp = false, zm =
 									false;
 							if (x == Chunk.CHUNK_X - 1 || chunk.getBlockAt(x + 1, y, z) == 0) {
 								xp = true;
 							}
 							if (x == 0 || chunk.getBlockAt(x - 1, y, z) == 0) {
 								xm = true;
 							}
 							if (y == Chunk.CHUNK_Y - 1 || chunk.getBlockAt(x, y + 1, z) == 0) {
 								yp = true;
 							}
 							if (y == 0 || chunk.getBlockAt(x, y - 1, z) == 0) {
 								ym = true;
 							}
 							if (z == Chunk.CHUNK_Z - 1 || chunk.getBlockAt(x, y, z + 1) == 0) {
 								zp = true;
 							}
 							if (z == 0 || chunk.getBlockAt(x, y, z - 1) == 0) {
 								zm = true;
 							}
 							Block b = Block.blocks.get(chunk.getBlockAt(x, y, z));
 							if (xp) {
 								int texid = b.getTextureForSide(0);
 								float tx = (texid & 0xf) / 16f;
 								float ty = (texid >> 4) / 16f;
 								glColor3f(0.7F, 0.7F, 0.7F);
 								glTexCoord2f(tx + 0.0625F, ty);
 								glVertex3f(x + 1, y + 1, z + 1);
 
 								glTexCoord2f(tx + 0.0625F, ty + 0.0625F);
 								glVertex3f(x + 1, y, z + 1);
 
 								glTexCoord2f(tx, ty + 0.0625F);
 								glVertex3f(x + 1, y, z);
 
 								glTexCoord2f(tx, ty);
 								glVertex3f(x + 1, y + 1, z);
 							}
 							if (xm) {
 								int texid = b.getTextureForSide(1);
 								float tx = (texid & 0xf) / 16f;
 								float ty = (texid >> 4) / 16f;
 								glColor3f(0.7F, 0.7F, 0.7F);
 								glTexCoord2f(tx, ty);
 								glVertex3f(x, y + 1, z);
 
 								glTexCoord2f(tx, ty + 0.0625F);
 								glVertex3f(x, y, z);
 
 								glTexCoord2f(tx + 0.0625F, ty + 0.0625F);
 								glVertex3f(x, y, z + 1);
 
 								glTexCoord2f(tx + 0.0625F, ty);
 								glVertex3f(x, y + 1, z + 1);
 							}
 							if (yp) {
 								int texid = b.getTextureForSide(2);
 								float tx = (texid & 0xf) / 16f;
 								float ty = (texid >> 4) / 16f;
 								glColor3f(1.1F, 1.1F, 1.1F);
 								glTexCoord2f(tx + 0.0625F, ty);
 								glVertex3f(x, y + 1, z + 1);
 
 								glTexCoord2f(tx, ty);
 								glVertex3f(x + 1, y + 1, z + 1);
 
 								glTexCoord2f(tx, ty + 0.0625F);
 								glVertex3f(x + 1, y + 1, z);
 
 								glTexCoord2f(tx + 0.0625F, ty + 0.0625F);
 								glVertex3f(x, y + 1, z);
 							}
 							if (ym) {
 								int texid = b.getTextureForSide(3);
 								float tx = (texid & 0xf) / 16f;
 								float ty = (texid >> 4) / 16f;
 								glColor3f(0.6F, 0.6F, 0.6F);
 								glTexCoord2f(tx + 0.0625F, ty + 0.0625F);
 								glVertex3f(x, y, z);
 
 								glTexCoord2f(tx, ty + 0.0625F);
 								glVertex3f(x + 1, y, z);
 
 								glTexCoord2f(tx, ty);
 								glVertex3f(x + 1, y, z + 1);
 
 								glTexCoord2f(tx + 0.0625F, ty);
 								glVertex3f(x, y, z + 1);
 							}
 							if (zp) {
 								int texid = b.getTextureForSide(4);
 								float tx = (texid & 0xf) / 16f;
 								float ty = (texid >> 4) / 16f;
 								glColor3f(0.85F, 0.85F, 0.85F);
 								glTexCoord2f(tx + 0.0625F, ty + 0.0625F);
 								glVertex3f(x, y, z + 1);
 
 								glTexCoord2f(tx, ty + 0.0625F);
 								glVertex3f(x + 1, y, z + 1);
 
 								glTexCoord2f(tx, ty);
 								glVertex3f(x + 1, y + 1, z + 1);
 
 								glTexCoord2f(tx + 0.0625F, ty);
 								glVertex3f(x, y + 1, z + 1);
 							}
 							if (zm) {
 								int texid = b.getTextureForSide(5);
 								float tx = (texid & 0xf) / 16f;
 								float ty = (texid >> 4) / 16f;
 								glColor3f(0.85F, 0.85F, 0.85F);
 								glTexCoord2f(tx + 0.0625F, ty);
 								glVertex3f(x, y + 1, z);
 
 								glTexCoord2f(tx, ty);
 								glVertex3f(x + 1, y + 1, z);
 
 								glTexCoord2f(tx, ty + 0.0625F);
 								glVertex3f(x + 1, y, z);
 
 								glTexCoord2f(tx + 0.0625F, ty + 0.0625F);
 								glVertex3f(x, y, z);
 							}
 						}
 					}
 				}
 			}
 			glEnd();
 			glEndList();
 		}
 		System.out.println(timer.nextDelta());
 		selectionDisplayList = glGenLists(1);
 		glNewList(selectionDisplayList, GL_COMPILE);
 		glBegin(GL_LINES);
 		glColor3f(0, 0, 0);
 		glLineWidth(3);
 
 		glVertex3f(1, 1, 1);
 		glVertex3f(1, 0, 1);
 
 		glVertex3f(1, 0, 1);
 		glVertex3f(1, 0, 0);
 
 		glVertex3f(1, 0, 0);
 		glVertex3f(1, 1, 0);
 
 		glVertex3f(1, 1, 0);
 		glVertex3f(1, 1, 1);
 
 
 
 		glVertex3f(0, 1, 0);
 		glVertex3f(0, 0, 0);
 
 		glVertex3f(0, 0, 0);
 		glVertex3f(0, 0, 1);
 
 		glVertex3f(0, 0, 1);
 		glVertex3f(0, 1, 1);
 
 		glVertex3f(0, 1, 1);
 		glVertex3f(0, 1, 0);
 
 
 		glVertex3f(0, 1, 0);
 		glVertex3f(1, 1, 0);
 
 		glVertex3f(0, 0, 0);
 		glVertex3f(1, 0, 0);
 
 		glVertex3f(0, 0, 1);
 		glVertex3f(1, 0, 1);
 
 		glVertex3f(0, 1, 1);
 		glVertex3f(1, 1, 1);
 		glEnd();
 		glEndList();
 	}
 
 	private boolean updateChunkDisplayList(ChunkPosition pos) {
 		return true;
 	}
 
 	private void onClose(int i) {
 		Collection<Integer> lists = chunkDisplayLists.values();
 		for (Integer x : lists) {
 			glDeleteLists(x, 1);
 		}
 		System.exit(i);
 	}
 	private int itime = 0;
 	private float selectionDistance = 0;
 
 	private void input(int dt) {
 		if (Mouse.isGrabbed() != grabMouse) {
 			Mouse.setGrabbed(grabMouse);
 		}
 		itime += dt;
 		while (Mouse.next()) {
 			if (Mouse.isGrabbed()) {
 				selectionDistance -= Mouse.getDWheel();
 				rX += Mouse.getDX() * mouseSensitivity;
 				rX %= 360;
 				rY = Math.max(-90, Math.min(90, rY - Mouse.getDY() * mouseSensitivity));
 			}
 		}
 		while (Keyboard.next()) {
 			boolean state = Keyboard.getEventKeyState();
 			if (Keyboard.getEventKey() == Keyboard.KEY_W) {
 				this.forwardMove = state ? playerSpeed : 0;
 			}
 			if (Keyboard.getEventKey() == Keyboard.KEY_S) {
 				this.forwardMove = state ? -playerSpeed : 0;
 			}
 			if (Keyboard.getEventKey() == Keyboard.KEY_A) {
 				this.sideMove = state ? playerSpeed : 0;
 			}
 			if (Keyboard.getEventKey() == Keyboard.KEY_D) {
 				this.sideMove = state ? -playerSpeed : 0;
 			}
 			if (Keyboard.getEventKey() == Keyboard.KEY_LSHIFT) {
 				this.upMove = state ? playerSpeed : 0;
 			}
 			if (Keyboard.getEventKey() == Keyboard.KEY_SPACE) {
 				this.upMove = state ? -playerSpeed : 0;
 			}
 			if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
 				if (itime > 200) {
 					this.grabMouse = !this.grabMouse;
 					itime = 0;
 				}
 			}
 		}
 
 		double sinRX = Math.sin(Math.toRadians(rX));
 		double cosRX = Math.cos(Math.toRadians(rX));
 		double cosRY = Math.cos(Math.toRadians(rY));
 		double sinRY = Math.sin(Math.toRadians(rY));
 		float x = player.getX(), y = player.getY(), z = player.getZ();
 
 		player.setZ((float)(z - sideMove * dt * sinRX - forwardMove * dt * cosRX * cosRY));
 		player.setX((float)(x - sideMove * dt * cosRX + forwardMove * dt * sinRX * cosRY));
 		player.setY((float)(y - upMove * dt - forwardMove * dt * sinRY));
 		player.setRx(rX);
 		player.setRy(rY);
 		float pz = player.getZ() + (float)(selectionDistance * cosRX * cosRY * 0.01F);
 		float py = player.getY() + (float)(selectionDistance * sinRY * 0.01F);
 		float px = player.getX() + (float)(-selectionDistance * sinRX * cosRY * 0.01F);
 		//System.out.println(px + ", " + py + ", " + pz);
 		player.setSelectedBlock((int)px - (px < 0 ? 1 : 0), (int)py - (py < 0 ? 1 : 0),
 				(int)pz - (pz < 0 ? 1 : 0));
 	}
 
 	private void loadTextures() {
 		try {
 			tex = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("texture.png"));
 		} catch (IOException ex) {
 			Logger.getLogger(FREEMine.class.getName()).log(Level.SEVERE, null, ex);
 			onClose(-1);
 		}
 		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
 		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
 	}
 
 	public FloatBuffer asFloatBuffer(float[] data) {
 		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
 		return (FloatBuffer)buffer.put(data).flip();
 	}
 
 	@SuppressWarnings("unchecked")
 	private void loadFonts() {
 		java.awt.Font awtFont = new java.awt.Font("Times New Roman", java.awt.Font.BOLD, 15);
 		font = new UnicodeFont(awtFont);
 		font.getEffects().add(new ColorEffect(Color.white));
 		font.addAsciiGlyphs();
 		try {
 			font.loadGlyphs();
 		} catch (SlickException ex) {
 			Logger.getLogger(FREEMine.class.getName()).log(Level.SEVERE, null, ex);
 			onClose(-1);
 		}
 	}
 
 	private void errorCheck() {
 		int e = glGetError();
 		if (e != GL_NO_ERROR) {
			System.out.println("OpenGL Error!\n"+e);
 		}
 	}
 }
