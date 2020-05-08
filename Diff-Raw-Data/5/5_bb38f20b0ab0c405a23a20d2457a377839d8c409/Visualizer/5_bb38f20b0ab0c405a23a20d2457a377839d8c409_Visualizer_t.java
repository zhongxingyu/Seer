 package sph;
 
 import static pa.cl.OpenCL.CL_MEM_READ_WRITE;
 import static pa.cl.OpenCL.clCreateFromGLBuffer;
 import static pa.cl.OpenCL.clEnqueueAcquireGLObjects;
 import static pa.cl.OpenCL.clEnqueueReleaseGLObjects;
 import static pa.cl.OpenCL.clReleaseMemObject;
 import static pa.cl.OpenCL.clSetKernelArg;
 
 import java.nio.FloatBuffer;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opencl.CLCommandQueue;
 import org.lwjgl.opencl.CLContext;
 import org.lwjgl.opencl.CLKernel;
 import org.lwjgl.opencl.CLMem;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL15;
 import org.lwjgl.opengl.GL20;
 import org.lwjgl.util.vector.Matrix4f;
 import org.lwjgl.util.vector.Vector3f;
 
 import visualize.FrameWork;
 import visualize.gl.GeometryFactory;
 import visualize.gl.GeometryFactory.Geometry;
 import visualize.gl.Program;
 import visualize.util.Timer;
 
 public class Visualizer extends FrameWork 
 {    
     public static class Params
     {
         public float m_timeStep;
         public float m_clusterScale;
         public float m_velocityScale;
         public float m_softening;
         public float m_damping;
         public float m_pointSize;
         public float m_x, m_y, m_z;
         
         protected Params(float ts, float cs, float vs, float soft, float damp, float ps, float x, float y, float z)
         {
             m_timeStep = ts;
             m_clusterScale = cs;
             m_velocityScale = vs;
             m_softening = soft;
             m_damping = damp;
             m_x = x;
             m_y = y;
             m_z = z;
             m_pointSize = ps;
         }
     }
     
     protected Params m_demoParams[] = 
         {
             new Params(0.016f, 1.54f, 8.0f, 0.1f, 1.0f, 1.0f, 0, -2, -100.0f),
             new Params(0.016f, 0.68f, 20.0f, 0.1f, 1.0f, 0.8f, 0, -2, -30.0f),
             /*new Params(0.0006f, 0.16f, 1000.0f, 1.0f, 1.0f, 0.07f, 0, 0, -1.5f),
             new Params(0.0006f, 0.16f, 1000.0f, 1.0f, 1.0f, 0.07f, 0, 0, -1.5f),
             new Params(0.0019f, 0.32f, 276.0f, 1.0f, 1.0f, 0.07f, 0, 0, -5.0f), */
             new Params(0.00016f, 0.32f, 272.0f, 0.145f, 1.0f, 0.08f, 0, 0, -5.0f),
             new Params(0.016f, 6.04f, 0.0f, 1.0f, 1.0f, 0.76f, 0, 0, -50.0f)
         };
     
     protected Params m_currentParams = m_demoParams[2];
     
     protected CLKernel m_kernel;
     protected CLCommandQueue m_queue = null;
     
     protected int m_invCameraAdress;
     
     protected Geometry[] m_buffer = new Geometry[2];
     
     protected Program m_program;
     
     protected int m_toggle = 0;
     
     protected CLMem m_oglBuffer0;
     protected CLMem m_oglBuffer1;
     
     public Visualizer(int w, int h) 
     {
         super(w, h, true, true, "SPH Simulation", false, false);
     }
     
     public Params getCurrentParams()
     {
         return m_currentParams;
     }
     
     public Program getProgram()
     {
         return m_program;
     }
     
     public Timer getTimer()
     {
         return m_timer;
     }
 
     @Override
     public void init() 
     {
         m_program = new Program();
         m_program.create("shader/Particles_VS.glsl", "shader/Particles_FS.glsl");
         m_program.bindAttributeLocation("vs_in_pos", 0);
         m_program.bindAttributeLocation("vs_in_normal", 1);
         m_program.bindAttributeLocation("vs_in_tc", 2);
         m_program.bindAttributeLocation("vs_in_instance", 3);
         m_program.linkAndValidate();
         m_program.bindUniformBlock("Camera", FrameWork.UniformBufferSlots.CAMERA_BUFFER_SLOT);
         m_program.bindUniformBlock("Color", FrameWork.UniformBufferSlots.COLOR_BUFFER_SLOT);
         m_program.use();
 
         m_invCameraAdress = m_program.getUniformLocation("invCamera");
         
         Matrix4f m = new Matrix4f();
         m.setIdentity();
         m.store(MATRIX4X4_BUFFER);
         MATRIX4X4_BUFFER.flip();
   
         GL20.glUniformMatrix4(m_invCameraAdress, false, MATRIX4X4_BUFFER);
         
         FloatBuffer data = BufferUtils.createFloatBuffer(4);
         data.put(0.5f); data.put(1); data.put(0); data.put(1);
         data.flip();
         m_color.loadFloatData(data, GL15.GL_DYNAMIC_DRAW);
         
         m_camera.setSpeed(0.25f);
         
         m_camera.lookAt(new Vector3f(0,0, m_currentParams.m_z), new Vector3f());
         uploadCameraBuffer();
     }
 
     @Override
     public void close() 
     {
         clEnqueueReleaseGLObjects(m_queue, m_oglBuffer0, null, null);
         clEnqueueReleaseGLObjects(m_queue, m_oglBuffer1, null, null);
         if(m_oglBuffer0 != null)
         {
             clReleaseMemObject(m_oglBuffer0);
             m_oglBuffer0 = null;
         }
         if(m_oglBuffer1 != null)
         {
             clReleaseMemObject(m_oglBuffer1);
             m_oglBuffer1 = null;
         }
         
         m_program.delete();
         m_buffer[0].delete();
         m_buffer[1].delete();
         destroy();
     }
     
     public void setKernelAndQueue(CLKernel kernel, CLCommandQueue queue)
     {
         m_kernel = kernel;
         m_queue = queue;
         clSetKernelArg(m_kernel, 2, m_currentParams.m_timeStep);
         clSetKernelArg(m_kernel, 3, m_currentParams.m_softening);
     }
     
     public CLMem[] createPositions(float[] pos, CLContext context)
     {
         if(m_buffer[0] != null)
         {
             m_buffer[0].delete();
         }
         
        m_buffer[0] = GeometryFactory.createParticles(pos, m_currentParams.m_pointSize * 0.1f, 4);
         
         if(m_buffer[1] != null)
         {
             m_buffer[1].delete();
         }
         
        m_buffer[1] = GeometryFactory.createParticles(pos, m_currentParams.m_pointSize * 0.1f, 4);
         
         m_oglBuffer0 = clCreateFromGLBuffer(context, CL_MEM_READ_WRITE, m_buffer[0].getInstanceBuffer(0).getId());
         m_oglBuffer1 = clCreateFromGLBuffer(context, CL_MEM_READ_WRITE, m_buffer[1].getInstanceBuffer(0).getId());
         
         CLMem pair[] = new CLMem[2];
         pair[0] = m_oglBuffer0;
         pair[1] = m_oglBuffer1;
         
         clEnqueueAcquireGLObjects(m_queue, m_oglBuffer0, null, null);
         clEnqueueAcquireGLObjects(m_queue, m_oglBuffer1, null, null);
         
         return pair;
     }
     
     public void visualize()
     {
         render();
     }
 
     @Override
     public void render() 
     {
     	if(m_queue != null) {
     		clEnqueueReleaseGLObjects(m_queue, m_oglBuffer0, null, null);
         	clEnqueueReleaseGLObjects(m_queue, m_oglBuffer1, null, null);
     	}
         updateInput();
         GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);       
         
 //        m_toggle = (++m_toggle) % 2;
         m_toggle = 0;
         if(m_buffer[m_toggle]!=null){
         	m_buffer[m_toggle].draw();
         }
         Display.update();
         
         if(m_queue != null) {
         	clEnqueueAcquireGLObjects(m_queue, m_oglBuffer0, null, null);
         	clEnqueueAcquireGLObjects(m_queue, m_oglBuffer1, null, null);
         }
         m_timer.tick();
     }
     
     public boolean isDone()
     {
         return m_done || Display.isCloseRequested();
     }
     
     @Override
     public void processMouseMoved(int x, int y, int dx, int dy) 
     {
         super.processMouseMoved(x, y, dx, dy);
         if(m_enableCamera && Mouse.isGrabbed())
         {
             m_program.use();
             Matrix4f mat = new Matrix4f();
             mat.rotate(m_camera.getPhi(), new Vector3f(0, 1, 0));
             mat.rotate(m_camera.getTheta(), new Vector3f(1, 0, 0));
             
             mat.store(MATRIX4X4_BUFFER);
             MATRIX4X4_BUFFER.position(0);
             
             GL20.glUniformMatrix4(m_invCameraAdress, false, MATRIX4X4_BUFFER); 
         }
     }
 }
