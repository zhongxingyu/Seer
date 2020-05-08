 package main;
 
 import java.nio.FloatBuffer;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.PointerBuffer;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opencl.CL10;
 import org.lwjgl.opencl.CLCommandQueue;
 import org.lwjgl.opencl.CLContext;
 import org.lwjgl.opencl.CLKernel;
 import org.lwjgl.opencl.CLMem;
 import org.lwjgl.opencl.CLProgram;
 
 import pa.cl.CLUtil;
 import pa.cl.OpenCL;
 import pa.cl.CLUtil.PlatformDeviceFilter;
 import pa.cl.CLUtil.PlatformDevicePair;
 import pa.util.IOUtil;
 import pa.util.SizeOf;
 
 import opengl.GL;
 import static opengl.GL.*;
 import opengl.util.Camera;
 import opengl.util.ShaderProgram;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.util.vector.Matrix4f;
 
 import particle.ParticleFactory;
 import opengl.util.FrameBuffer;
 import opengl.util.Texture;
 import opengl.util.Geometry;
 import opengl.util.GeometryFactory;
 
 public class MainProgram {
 	private boolean running = true;
 	
 	////// SHARED BLOCK
 	private int bufferObjectPositions = -1;
 	private int bufferObjectLifetimes = -1;
 	private int elements         = 1<<8; // 2^n = 1<<n 
    private int spawnElements    = 2;
     private long respawnInterval = 1000; // milliseconds
 
 	////// OPENCL BLOCK
 	private CLContext context    = null;
 	private CLCommandQueue queue = null;
 	private CLProgram program    = null;
 	private CLKernel kernelMove  = null;
 	private CLKernel kernelSpawn = null;
 	private CLKernel kernelSort  = null;
 	private CLMem memPositions   = null;
 	private CLMem memLifetime    = null;
 	private CLMem memNewPositions = null;
 
 	////// OPENGL BLOCK + DEFERRED SHADING
 	private Matrix4f modelMat = new Matrix4f();
 	private Camera   cam      = new Camera();
 	private int vertexArrayID = -1;
 	
 	private Geometry screenQuad = null;
 	private ShaderProgram screenQuadSP = null;
 
 	private int textureUnit = 0;
 	private ShaderProgram depthSP = null;
 	private FrameBuffer depthFB   = null;
 	private Texture depthTex      = null;
 
 	////// other
 	private long lastTimestamp  = System.currentTimeMillis();
 	private long sumDeltaTime   = 0;
 	private int  numberOfFrames = 0;
 	private long respawnTimer   = 0;
 	
 	private boolean animating = true;
 	
 	public MainProgram() {
 	    initGL();
 		initCL();
 	    initParticleBuffers();
 	}
 	
 	private void initParticleBuffers() {
 	    // vertex array for particles (the screen quad uses a different one)
 	    // TODO: both in one vertexarray?
 	    vertexArrayID = glGenVertexArrays();
         glBindVertexArray(vertexArrayID);
 
 		// positions
 		FloatBuffer particlePositions = ParticleFactory.createZeroFloatBuffer(elements * 3);
 		bufferObjectPositions = glGenBuffers();
 		glBindBuffer(GL_ARRAY_BUFFER, bufferObjectPositions);
 		glBufferData(GL_ARRAY_BUFFER, particlePositions, GL_STATIC_DRAW);
 		
         glEnableVertexAttribArray(ShaderProgram.ATTR_POS);
         glVertexAttribPointer(ShaderProgram.ATTR_POS, 3, GL_FLOAT, false, 3 * SizeOf.FLOAT, 0);
         
         // lifetimes
         FloatBuffer particleLifetimes = ParticleFactory.createZeroFloatBuffer(elements * 2);
         bufferObjectLifetimes = glGenBuffers();
         glBindBuffer(GL_ARRAY_BUFFER, bufferObjectLifetimes);
         glBufferData(GL_ARRAY_BUFFER, particleLifetimes, GL_STATIC_DRAW);
         
         glEnableVertexAttribArray(ShaderProgram.ATTR_NORMAL);
         glVertexAttribPointer(ShaderProgram.ATTR_NORMAL, 2, GL_FLOAT, false, 2 * SizeOf.FLOAT, 0);
         
 	}
 
 	public void run() {
 		System.out.println("Running with " + elements + " Particles.");
 		
 		// push OpenGL Buffer to OpenCL TODO
 		memPositions = OpenCL.clCreateFromGLBuffer(context, OpenCL.CL_MEM_READ_WRITE, bufferObjectPositions);
 		memLifetime  = OpenCL.clCreateFromGLBuffer(context, OpenCL.CL_MEM_READ_WRITE, bufferObjectLifetimes);
 		
 		// static kernel arguments
         OpenCL.clSetKernelArg(kernelMove, 0, memPositions);
         OpenCL.clSetKernelArg(kernelMove, 1, memLifetime);
         
         OpenCL.clSetKernelArg(kernelSpawn, 0, memPositions);
         OpenCL.clSetKernelArg(kernelSpawn, 1, memLifetime);
         OpenCL.clSetKernelArg(kernelSpawn, 3, spawnElements);
         
         // calculate global work size
 		PointerBuffer gws = new PointerBuffer(elements);
         gws.put(0, elements);
         
         spawnElements = Math.min(spawnElements, elements);
         FloatBuffer bufferNewParticleData = BufferUtils.createFloatBuffer(spawnElements * 4);
         System.out.println("Respawning: " + spawnElements + " elements per " + respawnInterval + " ms.");
         
         while(running) {
 			long deltaTime = System.currentTimeMillis() - lastTimestamp;
 			lastTimestamp += deltaTime;
 			respawnTimer  += deltaTime;
 			calculateFramesPerSecond(deltaTime);
 			
 			handleInput(deltaTime);
 
 			// TODO
 			if(animating) {
 			    
     			OpenCL.clEnqueueAcquireGLObjects(queue, memPositions, null, null);
     			OpenCL.clEnqueueAcquireGLObjects(queue, memLifetime, null, null);
     
     			OpenCL.clSetKernelArg(kernelMove, 2, (int)deltaTime);
     			OpenCL.clEnqueueNDRangeKernel(queue, kernelMove, 1, null, gws, null, null, null);
     	        
     	        if(respawnTimer >= respawnInterval) {
     	            respawnTimer = 0;
     	            
         	        for(int i = 0; i < spawnElements * 4; i += 4) {
                         float[] pos = ParticleFactory.generateCoordinates();
                         bufferNewParticleData.put(i+0, pos[0]);
                         bufferNewParticleData.put(i+1, pos[1]);
                         bufferNewParticleData.put(i+2, pos[2]);
                         bufferNewParticleData.put(i+3, ParticleFactory.generateLifetime());
                     }
                     bufferNewParticleData.rewind();
                     
                     System.out.println("Respawn:");
                     for(int i = 0; i < bufferNewParticleData.capacity(); i++) {
                         System.out.print(bufferNewParticleData.get() + ", ");
                     }
                     System.out.println("\n");
                     bufferNewParticleData.rewind();
                     if(memNewPositions != null) {
                         OpenCL.clReleaseMemObject(memNewPositions);
                         memNewPositions = null;
                     }
                     memNewPositions = OpenCL.clCreateBuffer(context, OpenCL.CL_MEM_COPY_HOST_PTR | OpenCL.CL_MEM_READ_ONLY, bufferNewParticleData);
                     
                     OpenCL.clSetKernelArg(kernelSpawn, 2, memNewPositions);
                     OpenCL.clEnqueueNDRangeKernel(queue, kernelSpawn, 1, null, gws, null, null, null);
     	        }
     	        
     	        OpenCL.clEnqueueReleaseGLObjects(queue, memLifetime, null, null);
                 OpenCL.clEnqueueReleaseGLObjects(queue, memPositions, null, null);
                 
 			}  // if animating
 	        
 //	        debugCL(memPositions, 3, 1);
 //	        debugCL(memLifetime, 2, 5);
 		
 			drawScene();
             
             // if close is requested: close
 			if(Display.isCloseRequested() || Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
 				stop();
 			}
 		}
 		System.out.println("Program shut down properly.");
 	}
 	
 	public void drawScene() {
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 		
 		// post effects etc
 		depthSP.use();
 		depthSP.setUniform("model", modelMat);
 		depthSP.setUniform("viewProj", opengl.util.Util.mul(null, cam.getProjection(), cam.getView()));
         depthSP.setUniform("camPos", cam.getCamPos());
 
         depthFB.bind();
         depthFB.clearColor();
         
         glBindVertexArray(vertexArrayID);
         opengl.GL.glDrawArrays(opengl.GL.GL_POINTS, 0, elements);
 		
 		
 		
 		// draw texture on screenquad
         glBindFramebuffer(GL_FRAMEBUFFER, 0);
 		screenQuadSP.use();        
 		screenQuadSP.setUniform("image", depthTex);
 		screenQuad.draw();
 		
         // present screen
         Display.update();
 //        Display.sync(60);
 	}
 	
 	private void handleInput(long deltaTime) {
         float speed = 5e-6f * deltaTime;
         
         if(Keyboard.next() && Keyboard.isKeyDown(Keyboard.getEventKey())) {
             switch(Keyboard.getEventKey()) {
                 case Keyboard.KEY_S: animating = !animating; 
                     break; 
             }
         }
         
         while(Mouse.next()) {
             if(Mouse.isButtonDown(0)) {
                 cam.rotate(-speed*Mouse.getEventDX(), -speed*Mouse.getEventDY());
             }
         }
     }
 	
 	public void initGL() {
         try {
             GL.init();
         } catch (LWJGLException e) {
             e.printStackTrace();
         }
         
         // screenQuad
         screenQuad   = GeometryFactory.createScreenQuad();
         screenQuadSP = new ShaderProgram("shader/ScreenQuad_VS.glsl", "shader/CopyTexture_FS.glsl");
         
         // first renderpath: "depth"
         depthSP = new ShaderProgram("./shader/DefaultVS.glsl", "./shader/Default1FS.glsl");
         depthSP.use();
         
         depthFB = new FrameBuffer();
         depthFB.init(true, WIDTH, HEIGHT);
 
         depthTex = new Texture(GL_TEXTURE_2D, textureUnit++);
         depthFB.addTexture(depthTex, GL_RGBA16F, GL_RGBA);
         glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
         glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
         glBindFragDataLocation(depthSP.getId(), 0, "depth");
         
         glClearColor(0.1f, 0.0f, 0.4f, 1.0f);
     }
 	
 	public void initCL() {
         CLUtil.createCL();
         
         PlatformDevicePair pair = null;
         try {
             PlatformDeviceFilter filter = new PlatformDeviceFilter();
             
             // set spec here
             filter.addPlatformSpec(CL10.CL_PLATFORM_VENDOR, "NVIDIA");
             filter.setDesiredDeviceType(CL10.CL_DEVICE_TYPE_GPU);
                 
             // query platform and device
             pair = CLUtil.choosePlatformAndDevice(filter);
         }catch(Exception e) {
             pair = CLUtil.choosePlatformAndDevice();
         }
         
         context = OpenCL.clCreateContext(pair.platform, pair.device, null, Display.getDrawable());
         queue   = OpenCL.clCreateCommandQueue(context, pair.device, OpenCL.CL_QUEUE_PROFILING_ENABLE);
         // for out of order queue: OpenCL.CL_QUEUE_OUT_OF_ORDER_EXEC_MODE_ENABLE
         
         program = OpenCL.clCreateProgramWithSource(context, IOUtil.readFileContent("kernel/kernel.cl"));
         OpenCL.clBuildProgram(program, pair.device, "", null);
         
         kernelMove = OpenCL.clCreateKernel(program, "move");
         kernelSpawn = OpenCL.clCreateKernel(program, "respawn");
         // TODO other kernels
     }
 	
 	public void stop() {
 	    // TODO: Nullchecks
         running = false;
         
         screenQuadSP.delete();
         depthSP.delete();
         // TODO cleanup (possible) additional sps
         
         if(!Display.isCloseRequested())  {
             Display.destroy();
         }
         
         OpenCL.clReleaseMemObject(memLifetime);
         OpenCL.clReleaseMemObject(memPositions);
         
         if(memNewPositions != null)
             OpenCL.clReleaseMemObject(memNewPositions);
         
         OpenCL.clReleaseKernel(kernelSpawn);
         OpenCL.clReleaseKernel(kernelMove);
 
         OpenCL.clReleaseProgram(program);
         OpenCL.clReleaseCommandQueue(queue);
         OpenCL.clReleaseContext(context);
         
         CLUtil.destroyCL();
         
         GL.destroy();
     }
 	
 	private void calculateFramesPerSecond(long deltaTime) {
 		numberOfFrames++;
 		sumDeltaTime += deltaTime;
         if(sumDeltaTime > 1000) {
         	float fps = numberOfFrames / (float)(sumDeltaTime / 1000);
         	numberOfFrames = 0;
         	sumDeltaTime   = 0;
         	Display.setTitle("FPS: " + fps);
         }
 	}
 	
 	public void debugCL(CLMem memObject, int numberOfValues) {
 		debugCL(memObject, numberOfValues, 3);
 	}
 	
 	public void debugCL(CLMem memObject, int numberOfValues, int maxParticles) {
         FloatBuffer fb = BufferUtils.createFloatBuffer(elements * numberOfValues);
 		OpenCL.clEnqueueAcquireGLObjects(queue, memObject, null, null);
         OpenCL.clEnqueueReadBuffer(queue, memObject, 0, 0, fb, null, null);
         OpenCL.clEnqueueReleaseGLObjects(queue, memObject, null, null);
         fb.rewind();
 
         for(int i = 0; i < Math.min(fb.capacity(), maxParticles * numberOfValues); i++) {
         	if(i%numberOfValues == 0)
         		System.out.print("Particle " + i/numberOfValues + ": ");
         	System.out.print(fb.get(i) + ((i%numberOfValues == numberOfValues-1)?"\n":", "));
         }
         System.out.println();
 	}
 	
 	
 }
