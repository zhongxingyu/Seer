 /*
  * Copyright 2013, Sebastian Kreisel. All rights reserved.
  * If you intend to use, modify or redistribute this file contact kreisel.sebastian@gmail.com
  */
 
 package com.elfeck.ephemeral.glContext;
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.opengl.GL13.*;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.PixelFormat;
 
 import com.elfeck.ephemeral.EPHemeral;
 
 
 public class EPHRenderContext {
 
 	private static boolean glInitialized = false;
 	private static boolean created = false;
 	private static int[] windowDimensions = new int[4];
 	protected static final Object glInitMonitor = new Object();
 
 	private boolean resizable, resizableTriggered, resizedTriggered;
 	private int fpsCap;
 	private String shaderParentPath, title;
 	private EPHemeral main;
 	private EPHInput input;
 
 	private EPHRenderContext(EPHemeral main, EPHInput input, int fpsCap, String shaderParentPath, String title) {
 		created = true;
 		this.main = main;
 		this.input = input;
 		this.fpsCap = fpsCap;
 		this.shaderParentPath = shaderParentPath;
 		this.title = title;
 		resizable = false;
 		resizableTriggered = false;
 		resizedTriggered = false;
 	}
 
 	private void glInitContext() {
 		DisplayMode displayMode = new DisplayMode(main.getWidth(), main.getHeight());
 		windowDimensions = new int[] { 0, 0, main.getWidth(), main.getHeight() };
 		PixelFormat pixelFormat = new PixelFormat().withSamples(8);
 		try {
 			Display.setDisplayMode(displayMode);
 			Display.setResizable(resizable);
			Display.setTitle(title + " using EPHemeral v. " + EPHemeral.VERSION);
			Display.create(pixelFormat);
 		} catch (LWJGLException e) {
 			e.printStackTrace();
 		}
 		glEnable(GL_DEPTH_TEST);
 		glDepthFunc(GL_LEQUAL);
 		glDepthMask(true);
 		glDepthRange(1.0f, 0.0f);
 		glEnable(GL_BLEND);
 		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 		glEnable(GL_MULTISAMPLE);
 		glEnable(GL_SCISSOR_TEST);
 		EPHVertexArrayObject.glInitShaderProgramPool(shaderParentPath);
 		glInitialized = true;
 		synchronized (glInitMonitor) {
 			glInitMonitor.notifyAll();
 		}
 	}
 
 	private boolean glHandleCloseRequest() {
 		if (Display.isCloseRequested()) {
 			main.destroy();
 			return true;
 		}
 		return false;
 	}
 
 	private void glHandleInput() {
 		input.mx = Mouse.getX();
 		input.my = Mouse.getY();
 		input.mdx = Mouse.getDX();
 		input.mdy = Mouse.getDY();
 
 		input.setMleftPressed(Mouse.isButtonDown(0));
 		input.setMrightPressed(Mouse.isButtonDown(1));
 	}
 
 	private void glClearDisplay() {
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 	}
 
 	private void glDraw() {
 		for (int i = 0; i < main.getVaos().size(); i++) {
 			EPHVertexArrayObject currentVao = main.getVaos().get(i);
 			if (currentVao.isDead()) {
 				main.getVaos().remove(i--);
 				continue;
 			}
 			currentVao.glRender();
 		}
 	}
 
 	private void glCheckResized() {
 		if (Display.wasResized()) {
 			windowDimensions[2] = main.getWidth();
 			windowDimensions[3] = main.getHeight();
 			glViewport(0, 0, main.getWidth(), main.getHeight());
 			glScissor(0, 0, main.getWidth(), main.getHeight());
 		}
 		if (resizableTriggered) {
 			Display.setResizable(resizable);
 			resizableTriggered = false;
 		}
 		if (resizedTriggered) {
 			try {
 				Display.setDisplayMode(new DisplayMode(main.getWidth(), main.getHeight()));
 			} catch (LWJGLException e) {
 				e.printStackTrace();
 			}
 			Display.update();
 			resizedTriggered = false;
 			windowDimensions[2] = main.getWidth();
 			windowDimensions[3] = main.getHeight();
 			glViewport(0, 0, main.getWidth(), main.getHeight());
 			glScissor(0, 0, main.getWidth(), main.getHeight());
 		}
 	}
 	public void glRender() {
 		if (!glInitialized) glInitContext();
 		if (glHandleCloseRequest()) return;
 		glCheckResized();
 		glClearDisplay();
 		if (main.getSurface() != null) glDraw();
 		Display.update();
 		glHandleInput();
 		Display.sync(fpsCap);
 		main.updateVaos();
 	}
 
 	public void glDestroy() {
 		if (!glInitialized) return;
 		main.glDestroyVaos();
 		Display.destroy();
 	}
 
 	public void setResizable(boolean resizable) {
 		this.resizable = resizable;
 		resizableTriggered = true;
 	}
 
 	public void resize() {
 		resizedTriggered = true;
 	}
 
 	public boolean wasResized() {
 		return Display.wasResized();
 	}
 
 	public int getWidth() {
 		return Display.getWidth();
 	}
 
 	public int getHeight() {
 		return Display.getHeight();
 	}
 
 	protected static int[] getWindowDimensions() {
 		synchronized (glInitMonitor) {
 			if (!glInitialized) {
 				try {
 					glInitMonitor.wait();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return windowDimensions;
 	}
 
 	protected static boolean isInitialized() {
 		return glInitialized;
 	}
 
 	public static EPHRenderContext createRenderContext(EPHemeral main, EPHInput input, int fpsCap, String shaderParentPath, String title) {
 		if (!created) return new EPHRenderContext(main, input, fpsCap, shaderParentPath, title);
 		System.err.println("RenderContext was already created");
 		return null;
 	}
 
 }
