 package org.put.hd.dmarf.algorithms.apriori.binary;
 
 import static org.jocl.CL.*;
 
 import org.jocl.*;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 import java.util.Set;
 import java.util.SortedMap;
 
 import org.put.hd.dmarf.data.DataRepresentationBase;
 import org.put.hd.dmarf.stopwatches.StopWatch;
 
 /**
  * JOCLs implementation of the {@link ISetsEngine} mostly for data mining.
  * 
  * @author Miki & Piotr
  * 
  */
 public class JOCLSetsEngine implements ISetsEngine {
 
 	// OpenCL stuff
 	private cl_context context;
 	private cl_command_queue commandQueue;
 	private cl_kernel kernel1;
 	private cl_kernel kernel2;
 	private cl_program program;
 
 	// transactions Map
 	private cl_mem transCharMapMem;
 	private Pointer transCharMapPointer;
 	private char[] transCharMap;
 
 	// ~support counting vector
 	private Pointer outSuppCharArrayPointer;
 	private cl_mem outSuppCharArrayMem;
 	private char[] outSuppCharArray;
 
 	// candidateSet Array
 	private cl_mem candSetMem;
 	private Pointer candSetPointer;
 	private char[] candSet;
 
 	// tmp Map
 	private Pointer outMapPointer;
 	private cl_mem outMapMem;
 	private char[] outTestCharMap;
 
 	private long global_work_size[];
 	private long local_work_size[];
 
 	private DataRepresentationBase data;
 	
 	private StopWatch sw;
 
 	public Set<BinaryItemSet> getCandidateSets(
 			Set<BinaryItemSet> frequentSupportMap, int i) {
 		// TODO Auto-generated method stub
 		// TEN KOD TE MOE TRAFI NA JOCLA - LATER
 		return null;
 	}
 
	public Set<BinaryItemSet> verifyCandidatesInData(
 			DataRepresentationBase data, Set<BinaryItemSet> candidates) {
 		// TODO Auto-generated method stub
 
 		// TUTAJ PISZESZ KOD PRZESZUKUJCY JOCLa
 
 		return null;
 	}
 
 	public SortedMap<BinaryItemSet, Integer> getSingleCandidateSets(
 			DataRepresentationBase data, Integer support) {
 
 		// TODO: write
 		// TUTAJ MONA WRZUCI ELEGANCKO WRZUCANIE DANYCH DO JOCLA
 
 		return null;
 	}
 
 	/**
 	 * Initialize OpenCL: Create the context, the command queue and the kernel.
 	 */
 	public void initCL(DataRepresentationBase data) {
 
 		long numBytes[] = new long[1];
 
 		// Create input- and output data
 		this.data = data;
 		System.out.println("Getting the charMap");
 		transCharMap = data.getTransactionsCharMap();
 		transCharMapPointer = Pointer.to(transCharMap);
 
 		// Obtain the number of platforms
 		int numPlatforms[] = new int[1];
 		clGetPlatformIDs(0, null, numPlatforms);
 
 		// Obtain the platform IDs and initialize the context properties
 		System.out.println("Number of platforms: " + numPlatforms[0]);
 
 		System.out.println("Obtaining platform...");
 		cl_platform_id platforms[] = new cl_platform_id[numPlatforms[0]];
 		clGetPlatformIDs(platforms.length, platforms, null);
 		cl_context_properties contextProperties = new cl_context_properties();
 		contextProperties.addProperty(CL_CONTEXT_PLATFORM, platforms[0]);
 
 		// Create an OpenCL context on a GPU device
 		context = clCreateContextFromType(contextProperties,
 				CL_DEVICE_TYPE_GPU, null, null, null);
 		if (context == null) {
 			// If no context for a GPU device could be created,
 			// try to create one for a CPU device.
 			context = clCreateContextFromType(contextProperties,
 					CL_DEVICE_TYPE_CPU, null, null, null);
 
 			if (context == null) {
 				System.out.println("Unable to create a context");
 				return;
 			}
 		}
 
 		// Enable exceptions and subsequently omit error checks in this sample
 		CL.setExceptionsEnabled(true);
 
 		// Get the list of GPU devices associated with the context
 		clGetContextInfo(context, CL_CONTEXT_DEVICES, 0, null, numBytes);
 
 		// Obtain the cl_device_id for the first device
 		int numDevices = (int) numBytes[0] / Sizeof.cl_device_id;
 		cl_device_id devices[] = new cl_device_id[numDevices];
 		clGetContextInfo(context, CL_CONTEXT_DEVICES, numBytes[0],
 				Pointer.to(devices), null);
 
 		// Create a command-queue
 		commandQueue = clCreateCommandQueue(context, devices[0], 0, null);
 
 		// Allocate the memory objects for the input- and output data
 		transCharMapMem = clCreateBuffer(context, CL_MEM_READ_ONLY
 				| CL_MEM_COPY_HOST_PTR, Sizeof.cl_short * transCharMap.length,
 				transCharMapPointer, null);
 
 		/**
 		 * The source code of the OpenCL support verifying program to execute
 		 */
 		String supportKernelSource = "__kernel void "
 				+ "supportKernel1(__global const ushort *inMap,"
 				+ "             __global const ushort *inSet,"
 				+ "             __global ushort *outMap)" + "{"
 				+ "    int gid = get_global_id(0);"
 				+ "    int grid = get_group_id(0);"
 				+ "    int glid = get_local_id(0);"
 				+ "	   outMap[gid] = (inSet[glid] & inMap[gid]) ^ inSet[glid];"
 				+ "}" + "__kernel void "
 				+ "supportKernel2(__global const ushort *inTmpMap,"
 				+ "             __global ushort *outSupp)" + "{"
 				+ "    int gid = get_global_id(0);"
 				+ "    int grid = get_group_id(0);"
 				+ "    int glid = get_local_id(0);"
 				+ "    for (uint pos = 0; pos <"
 				+ data.getNumberOfAttributesClusters() + "; pos++) {"
 				+ "	       outSupp[gid] = outSupp[gid] | inTmpMap[gid * "
 				+ data.getNumberOfAttributesClusters() + "+ pos];" + "    };"
 				+ "}";
 
 		// Create the program from the source code
 		program = clCreateProgramWithSource(context, 1,
 				new String[] { supportKernelSource }, null, null);
 
 		// Build the program
 		clBuildProgram(program, 0, null, null, null, null);
 
 		// Create the kernel
 		kernel1 = clCreateKernel(program, "supportKernel1", null);
 		kernel2 = clCreateKernel(program, "supportKernel2", null);
 
 		// Set the arguments for the kernel
 		clSetKernelArg(kernel1, 0, Sizeof.cl_mem, Pointer.to(transCharMapMem));
 
 		global_work_size = new long[] { transCharMap.length };
 		local_work_size = new long[] { data.getNumberOfAttributesClusters() };
 	}
 
 	/**
 	 * Finds number of transactions supporting a given set.
 	 * 
 	 * @param candidateSet
 	 *            set to be tested
 	 * @return number of transactions supporting given set.
 	 */
 	public int getSupport(char[] candidateSet) {
 
 		// preparing candidateSet argument for kernel
 		candSet = candidateSet;
 		candSetPointer = Pointer.to(candSet);
 
 		candSetMem = clCreateBuffer(context, CL_MEM_READ_ONLY
 				| CL_MEM_COPY_HOST_PTR,
 				Sizeof.cl_short * data.getNumberOfAttributesClusters(),
 				candSetPointer, null);
 
 		// preparing tmpMap argument for kernels
 		outTestCharMap = new char[transCharMap.length];
 		outMapPointer = Pointer.to(outTestCharMap);
 
 		outMapMem = clCreateBuffer(context, CL_MEM_READ_WRITE, Sizeof.cl_short
 				* transCharMap.length, null, null);
 
 		clSetKernelArg(kernel1, 1, Sizeof.cl_mem, Pointer.to(candSetMem));
 		clSetKernelArg(kernel1, 2, Sizeof.cl_mem, Pointer.to(outMapMem));
 
 		// Execute kernel1
 		// We got a worker per each comparison
 		global_work_size = new long[] { transCharMap.length };
 		local_work_size = new long[] { data.getNumberOfAttributesClusters() };
 
 		clEnqueueNDRangeKernel(commandQueue, kernel1, 1, null,
 				global_work_size, local_work_size, 0, null, null);
 
 		// preparing data for reduction kernel2
 		outSuppCharArray = new char[data.getNumberOfTransactions()];
 		outSuppCharArrayPointer = Pointer.to(outSuppCharArray);
 
 		outSuppCharArrayMem = clCreateBuffer(context, CL_MEM_WRITE_ONLY,
 				Sizeof.cl_short * data.getNumberOfTransactions(), null, null);
 
 		clSetKernelArg(kernel2, 0, Sizeof.cl_mem, Pointer.to(outMapMem));
 		clSetKernelArg(kernel2, 1, Sizeof.cl_mem,
 				Pointer.to(outSuppCharArrayMem));
 
 		// Execute the reduction kernel with 1 worker per transaction
 		global_work_size = new long[] { data.getNumberOfTransactions() };
 		local_work_size = new long[] { 1 };
 
 		clEnqueueNDRangeKernel(commandQueue, kernel2, 1, null,
 				global_work_size, local_work_size, 0, null, null);
 
 		// Read the output data
 		clEnqueueReadBuffer(commandQueue, outSuppCharArrayMem, CL_TRUE, 0,
 				data.getNumberOfTransactions() * Sizeof.cl_short,
 				outSuppCharArrayPointer, 0, null, null);
 
 		int supp = 0;
 		for (int i = 0; i < outSuppCharArray.length; i++) {
 			//System.out.println((int) outSuppCharArray[i] + " ");
 			if (outSuppCharArray[i] == 0)
 				supp++;
 		}
 
 		// cleaning gpuMem
 		clReleaseMemObject(candSetMem);
 		clReleaseMemObject(outMapMem);
 		clReleaseMemObject(outSuppCharArrayMem);
 		return supp;
 	}
 
 	public void cleanupCL() {
 
 		// Release kernel, program, and memory objects
 		System.out.println("Releasing objects.");
 		clReleaseMemObject(transCharMapMem);
 		clReleaseKernel(kernel1);
 		clReleaseKernel(kernel2);
 		clReleaseProgram(program);
 		clReleaseCommandQueue(commandQueue);
 		clReleaseContext(context);
 	}
 
 }
