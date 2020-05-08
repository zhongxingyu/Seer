 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.skyebook.cl;
 
 import com.jme3.math.FastMath;
 import com.jme3.math.Vector3f;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.FloatBuffer;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.lwjgl.BufferUtils;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.PointerBuffer;
 import org.lwjgl.opencl.*;
 
 /**
  *
  * @author Skye Book
  */
 public class DistanceTest {
 
     public static void main(String[] args) {
 
 	//LittleJMEApp jMEApp = new LittleJMEApp();
 	//jMEApp.start();
 	
 	int size = 50;
 
 
 	Vector3f[] findDistanceFromBuffers = new Vector3f[size];
 	for (int i = 0; i < size; i++) {
 	    Vector3f vector = new Vector3f(FastMath.rand.nextFloat(),
 		    FastMath.rand.nextFloat(), FastMath.rand.nextFloat());
 	    findDistanceFromBuffers[i] = vector;
 	}
 	
 	
 	Vector3f[] vectors = new Vector3f[size];
 	float[] nativeDistances = new float[size];
 	float[] clDistances = new float[size];
 
 	// populate the array with random vectors
 	for (int i = 0; i < size; i++) {
 	    Vector3f vector = new Vector3f(FastMath.rand.nextFloat(),
 		    FastMath.rand.nextFloat(), FastMath.rand.nextFloat());
 	    //System.out.println(vector);
 	    vectors[i] = vector;
 	}
 
 	FloatBuffer[] xyz1Buffers = VectorBufferConverter.toFloatBuffers(findDistanceFromBuffers);
 	FloatBuffer x1 = xyz1Buffers[0];
 	FloatBuffer y1 = xyz1Buffers[1];
 	FloatBuffer z1 = xyz1Buffers[2];
 	//System.out.println(x1.get(3));
 	FloatBuffer[] xyzBuffers = VectorBufferConverter.toFloatBuffers(vectors);
 	FloatBuffer x2 = xyzBuffers[0];
 	FloatBuffer y2 = xyzBuffers[1];
 	FloatBuffer z2 = xyzBuffers[2];
        
 	FloatBuffer distances = BufferUtils.createFloatBuffer(vectors.length);
        for(int i=0; i<distances.capacity(); i++){
            distances.put(i*2);
        }
        distances.rewind();
 
 	// do CL work
 	try {
 	    CL.create();
 	    CLPlatform platform = CLPlatform.getPlatforms().get(0);
 	    List<CLDevice> devices = platform.getDevices(CL10.CL_DEVICE_TYPE_GPU);
 	    CLContext context = CLContext.create(platform, devices, null, null, null);
 
 	    CLCommandQueue queue = CL10.clCreateCommandQueue(context, devices.get(0), CL10.CL_QUEUE_PROFILING_ENABLE, null);
 
 
 	    CLMem x1Mem = CL10.clCreateBuffer(context, CL10.CL_MEM_READ_ONLY | CL10.CL_MEM_COPY_HOST_PTR, x1, null);
 	    CL10.clEnqueueWriteBuffer(queue, x1Mem, 1, 0, x1, null, null);
 
 	    CLMem y1Mem = CL10.clCreateBuffer(context, CL10.CL_MEM_READ_ONLY | CL10.CL_MEM_COPY_HOST_PTR, y1, null);
 	    CL10.clEnqueueWriteBuffer(queue, y1Mem, 1, 0, y1, null, null);
 
 	    CLMem z1Mem = CL10.clCreateBuffer(context, CL10.CL_MEM_READ_ONLY | CL10.CL_MEM_COPY_HOST_PTR, z1, null);
 	    CL10.clEnqueueWriteBuffer(queue, z1Mem, 1, 0, z1, null, null);
 	    
 	    CLMem x2Mem = CL10.clCreateBuffer(context, CL10.CL_MEM_READ_ONLY | CL10.CL_MEM_COPY_HOST_PTR, x2, null);
 	    CL10.clEnqueueWriteBuffer(queue, x2Mem, 1, 0, x2, null, null);
 
 	    CLMem y2Mem = CL10.clCreateBuffer(context, CL10.CL_MEM_READ_ONLY | CL10.CL_MEM_COPY_HOST_PTR, y2, null);
 	    CL10.clEnqueueWriteBuffer(queue, y2Mem, 1, 0, y2, null, null);
 
 	    CLMem z2Mem = CL10.clCreateBuffer(context, CL10.CL_MEM_READ_ONLY | CL10.CL_MEM_COPY_HOST_PTR, z2, null);
 	    CL10.clEnqueueWriteBuffer(queue, z2Mem, 1, 0, z2, null, null);
 
	    CLMem distancesMem = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR, distances, null);
 	    CL10.clEnqueueWriteBuffer(queue, distancesMem, 1, 0, distances, null, null);
 
 	    CL10.clFinish(queue);
 
 	    String source = KernelUtils.loadSource("kernels/Vector3fFunctions_1.c");
 	    //String source = KernelUtils.loadSource("kernels/Test.c");
 	    System.out.println(source);
 	    // program/kernel creation
 	    CLProgram program = CL10.clCreateProgramWithSource(context, source, null);
 	    Util.checkCLError(CL10.clBuildProgram(program, devices.get(0), "", null));
 
 	    // give the name of the method you want to call in the OpenCL source
 	    CLKernel kernel = CL10.clCreateKernel(program, "vector3_distance", null);
 	    
 	    // execution
 	    PointerBuffer kernel1DGlobalWorkSize = BufferUtils.createPointerBuffer(1);
 	    kernel1DGlobalWorkSize.put(0, vectors.length);
 	    kernel.setArg(0, x1Mem);
 	    kernel.setArg(1, y1Mem);
 	    kernel.setArg(2, z1Mem);
 	    kernel.setArg(3, x2Mem);
 	    kernel.setArg(4, y2Mem);
 	    kernel.setArg(5, z2Mem);
 	    kernel.setArg(6, distancesMem);
 	    CL10.clEnqueueNDRangeKernel(queue, kernel, 1, null, kernel1DGlobalWorkSize, null, null, null);
 
 	    // read the results back
 	    CL10.clEnqueueReadBuffer(queue, distancesMem, 1, 0, distances, null, null);
 	    CL10.clFinish(queue);
 
 	    print(distances);
 
 	    // teardown
 	    CL10.clReleaseKernel(kernel);
 	    CL10.clReleaseProgram(program);
 	    CL10.clReleaseCommandQueue(queue);
 	    CL10.clReleaseContext(context);
 	    CL.destroy();
 
 
 	} catch (FileNotFoundException ex) {
 	    Logger.getLogger(DistanceTest.class.getName()).log(Level.SEVERE, null, ex);
 	} catch (IOException ex) {
 	    Logger.getLogger(DistanceTest.class.getName()).log(Level.SEVERE, null, ex);
 	} catch (LWJGLException ex) {
 	    Logger.getLogger(DistanceTest.class.getName()).log(Level.SEVERE, null, ex);
 	}
     }
 
     public static void print(FloatBuffer buffer) {
 	for (int i = 0; i < buffer.capacity(); i++) {
 	    System.out.println(buffer.get(i));
 	}
     }
 }
