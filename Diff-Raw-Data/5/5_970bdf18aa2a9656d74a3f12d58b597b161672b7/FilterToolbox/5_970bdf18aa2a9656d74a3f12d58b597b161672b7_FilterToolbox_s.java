 package plugins.adufour.filtering;
 
 import icy.image.IcyBufferedImage;
 import icy.sequence.Sequence;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import plugins.adufour.ezplug.EzGroup;
 import plugins.adufour.ezplug.EzPlug;
 import plugins.adufour.ezplug.EzStoppable;
 import plugins.adufour.ezplug.EzVar;
 import plugins.adufour.ezplug.EzVarBoolean;
 import plugins.adufour.ezplug.EzVarDouble;
 import plugins.adufour.ezplug.EzVarEnum;
 import plugins.adufour.ezplug.EzVarFloatArray;
 import plugins.adufour.ezplug.EzVarInteger;
 import plugins.adufour.ezplug.EzVarListener;
 import plugins.adufour.ezplug.EzVarSequence;
 
 import com.nativelibs4java.opencl.CLBuildException;
 import com.nativelibs4java.opencl.CLContext;
 import com.nativelibs4java.opencl.CLException;
 import com.nativelibs4java.opencl.CLProgram;
 import com.nativelibs4java.opencl.CLQueue;
 import com.nativelibs4java.opencl.JavaCL;
 import com.ochafik.io.ReadText;
 
 public class FilterToolbox extends EzPlug implements EzStoppable
 {
 	private final static int	MAX_KERNEL_SIZE	= 13;
 	
 	public enum Axis
 	{
 		X, Y, Z
 	}
 	
 	public enum FilterType
 	{
 		CLASSIC, SEPARABLE
 	}
 	
 	public EzVarSequence				input				= new EzVarSequence("input");
 	public EzVarEnum<FilterType>		filterType			= new EzVarEnum<FilterToolbox.FilterType>("Filter type", FilterType.values());
 	
 	public EzVarEnum<Kernels1D>			kernel1D			= new EzVarEnum<Kernels1D>("1D Kernels", Kernels1D.values());
 	public EzVarBoolean					linearX				= new EzVarBoolean("Along X", true);
 	public EzVarBoolean					linearY				= new EzVarBoolean("Along Y", true);
 	public EzVarBoolean					linearZ				= new EzVarBoolean("Along Z", true);
 	
 	public EzVarDouble					gaussianX			= new EzVarDouble("Sigma X", 1, 0, 100, 0.1);
 	public EzVarDouble					gaussianY			= new EzVarDouble("Sigma Y", 1, 0, 100, 0.1);
 	public EzVarDouble					gaussianZ			= new EzVarDouble("Sigma Z", 1, 0, 100, 0.1);
 	
 	public EzVarEnum<Kernels2D>			kernel2D			= new EzVarEnum<Kernels2D>("2D Kernels", Kernels2D.values());
 	
 	public EzVarDouble					gaborSigma			= new EzVarDouble("Sigma", 0, 100, 0.1);
 	public EzVarDouble					gaborKx				= new EzVarDouble("Kx", 0, 100, 0.1);
 	public EzVarDouble					gaborKy				= new EzVarDouble("Ky", 0, 100, 0.1);
 	public EzVarBoolean					gaborSymmetric		= new EzVarBoolean("Symmertric", true);
 	
 	public EzVarBoolean					zeroEdge			= new EzVarBoolean("Zero on edge", false);
 	
 	public EzVarInteger					userKernelWidth		= new EzVarInteger("kernel width", 3, MAX_KERNEL_SIZE, 2);
 	public EzVarInteger					userKernelHeight	= new EzVarInteger("kernel height", 1, MAX_KERNEL_SIZE, 2);
 	public ArrayList<EzVarFloatArray>	kernelLines			= new ArrayList<EzVarFloatArray>(1);
 	public EzVarInteger					iterations			= new EzVarInteger("nb. iterations", 1, 1, 10000, 1);
 	
 	private ConvolutionCL				convolutionCL;
 	private CLContext					context;
 	private CLQueue						queue;
 	private CLProgram					program;
 	
 	public EzVarBoolean					useOpenCL			= new EzVarBoolean("Use OpenCL", false);
 	
 	private EzVarBoolean				stopFlag			= new EzVarBoolean("stop", false);
 	
 	@Override
 	public void initialize()
 	{
 		input.addVarChangeListener(new EzVarListener<Sequence>()
 		{
 			@Override
 			public void variableChanged(EzVar<Sequence> source, Sequence newValue)
 			{
 				if (newValue == null)
 					return;
 				
 				boolean is3D = newValue.getSizeZ() > 1;
 				
 				linearZ.setValue(is3D);
 				linearZ.setVisible(is3D);
 				gaussianZ.setVisible(is3D);
 			}
 		});
 		
 		addEzComponent(input);
 		
 		addEzComponent(iterations);
 		
 		try
 		{
 			useOpenCL.addVarChangeListener(new EzVarListener<Boolean>()
 			{
 				@Override
 				public void variableChanged(EzVar<Boolean> source, Boolean newValue)
 				{
 					linearZ.setValue(!newValue);
 					linearZ.setVisible(!newValue);
 					gaussianZ.setVisible(!newValue);
 				}
 			});
 			
 			addEzComponent(useOpenCL);
 			useOpenCL.setVisible(false);
 			context = JavaCL.createBestContext();
 			queue = context.createDefaultQueue();
 			String programFile = ReadText.readText(ConvolutionCL.class.getResourceAsStream("Convolution.cl"));
 			program = context.createProgram(programFile).build();
 			convolutionCL = new ConvolutionCL(context, program, queue);
 			useOpenCL.setValue(true);
 			useOpenCL.setVisible(true);
 		}
 		catch (IOException e)
 		{
 			System.out.println("Warning (FilterToolbox): unable to load the OpenCL code. Continuing in pure Java mode.");
 			e.printStackTrace();
 		}
 		catch (CLException e)
 		{
 			System.out.println("Warning (FilterToolbox): unable to create the OpenCL context. Continuing in pure Java mode.");
 			e.printStackTrace();
 		}
 		catch (CLBuildException e)
 		{
 			System.out.println("Warning (FilterToolbox): unable to create the OpenCL context. Continuing in pure Java mode.");
 			e.printStackTrace();
 		}
 		catch(NoClassDefFoundError e)
 		{
 			System.out.println("Warning (FilterToolbox): unable to create the OpenCL context. Continuing in pure Java mode.");
 			e.printStackTrace();
 		}
 		catch (UnsatisfiedLinkError linkError)
 		{
 			// throw new EzException("Unable to load OpenCL drivers on this system", true);
 			System.out.println("Warning (FilterToolbox): OpenCL drivers not found. Using basic Java implementation.");
 		}
 		
 		addEzComponent(filterType);
 		
 		addEzComponent(kernel1D);
 		filterType.addVisibilityTriggerTo(kernel1D, FilterType.SEPARABLE);
 		
 		EzGroup groupLinear = new EzGroup("Directions", linearX, linearY, linearZ);
 		addEzComponent(groupLinear);
 		filterType.addVisibilityTriggerTo(groupLinear, FilterType.SEPARABLE);
 		
 		EzGroup groupGaussian = new EzGroup("Gaussian filter", gaussianX, gaussianY, gaussianZ);
 		addEzComponent(groupGaussian);
 		kernel1D.addVisibilityTriggerTo(groupGaussian, Kernels1D.CUSTOM_GAUSSIAN);
 		
 		addEzComponent(kernel2D);
 		filterType.addVisibilityTriggerTo(kernel2D, FilterType.CLASSIC);
 		
 		EzGroup gaborGroup = new EzGroup("Gabor 2D", gaborSigma, gaborKx, gaborKy, gaborSymmetric);
 		addEzComponent(gaborGroup);
 		kernel2D.addVisibilityTriggerTo(gaborGroup, Kernels2D.CUSTOM_GABOR);
 		
 		addEzComponent(zeroEdge);
 		
 		addEzComponent(userKernelWidth);
 		kernel1D.addVisibilityTriggerTo(userKernelWidth, Kernels1D.CUSTOM);
 		kernel2D.addVisibilityTriggerTo(userKernelWidth, Kernels2D.CUSTOM);
 		addEzComponent(userKernelHeight);
 		kernel2D.addVisibilityTriggerTo(userKernelHeight, Kernels2D.CUSTOM);
 		
 		// first line of the kernel must always exist
 		EzVarFloatArray firstKernelLine = new EzVarFloatArray("Line 1", new Float[][] { new Float[] { 1f, 1f, 1f } }, 0, true);
 		kernelLines.add(firstKernelLine);
 		addEzComponent(firstKernelLine);
 		kernel1D.addVisibilityTriggerTo(firstKernelLine, Kernels1D.CUSTOM);
 		kernel2D.addVisibilityTriggerTo(firstKernelLine, Kernels2D.CUSTOM);
 		
 		// other lines
 		userKernelHeight.addVarChangeListener(new EzVarListener<Integer>()
 		{
 			@Override
 			public void variableChanged(EzVar<Integer> source, Integer newValue)
 			{
 				if (kernelLines.size() < newValue)
 				{
 					int nbLines = kernelLines.size();
 					int nbNewLines = newValue - nbLines;
 					for (int i = 1; i <= nbNewLines; i++)
 					{
 						int lineNumber = i + nbLines;
 						EzVarFloatArray newLine = new EzVarFloatArray("Line " + lineNumber, new Float[][] { new Float[] { 1f, 1f, 1f } }, 0, true);
 						kernelLines.add(newLine);
 						kernel2D.addVisibilityTriggerTo(newLine, Kernels2D.CUSTOM);
 						addEzComponent(newLine);
 					}
 				}
 				else
 				{
 					if (userKernelHeight.getVisible())
 						for (int i = 0; i < kernelLines.size(); i++)
 							kernelLines.get(i).setVisible(i < newValue);
 				}
 				if (getUI() != null)
 					getUI().repack(true);
 			}
 		});
 		
 		setTimeDisplay(true);
 	}
 	
 	@Override
 	public void execute()
 	{
 		stopFlag.setValue(false);
 		
 		Sequence inSeq = input.getValue();
 		
 		switch (filterType.getValue())
 		{
 			case SEPARABLE:
 			{
 				executeSeparable(inSeq);
 				break;
 			}
 			case CLASSIC:
 			{
 				executeClassic(inSeq);
 				break;
 			}
 		}
 	}
 	
 	private void executeClassic(Sequence inSeq)
 	{
 		Kernels2D k2d = kernel2D.getValue();
 		
 		switch (kernel2D.getValue())
 		{
 			case CUSTOM_GABOR:
 				k2d.createGaborKernel2D(gaborSigma.getValue(), gaborKx.getValue(), gaborKy.getValue(), gaborSymmetric.getValue());
 			break;
 			
 			case CUSTOM:
 			{
 				ArrayList<Float> kernel1D = new ArrayList<Float>();
 				for (int i = 0; i < userKernelHeight.getValue(); i++)
 					for (Float f : kernelLines.get(i).getValue())
 						kernel1D.add(f);
 				
 				double[] values = new double[kernel1D.size()];
 				for (int i = 0; i < kernel1D.size(); i++)
 					values[i] = kernel1D.get(i);
 				
 				k2d.createCustomKernel2D(values, userKernelWidth.getValue(), userKernelHeight.getValue(), false);
 			}
 			break;
 		}
 		
 		Sequence kernel = k2d.toSequence();
 		Sequence output = inSeq.getCopy();
 		
 		if (useOpenCL.getValue())
 		{
 			convolutionCL.convolve(output, kernel, zeroEdge.getValue(), iterations.getValue(), stopFlag);
 		}
 		else
 		{
 			Convolution.convolve(output, kernel, zeroEdge.getValue(), iterations.getValue(), stopFlag);
 		}
 		
 		output.setName(inSeq.getName() + " * " + kernel.getName());
 		output.updateComponentsBounds(true);
 		addSequence(output);
 	}
 	
 	private void executeSeparable(Sequence inSeq)
 	{
 		Kernels1D k1d = kernel1D.getValue();
 		
 		Sequence kernelX, kernelY, kernelZ;
 		
 		switch (kernel1D.getValue())
 		{
 			case CUSTOM_GAUSSIAN:
 				kernelX = k1d.createGaussianKernel1D(gaussianX.getValue()).toSequence();
 				kernelY = k1d.createGaussianKernel1D(gaussianY.getValue()).toSequence();
 				kernelZ = k1d.createGaussianKernel1D(gaussianZ.getValue()).toSequence();
 			break;
 			
 			case CUSTOM:
 			{
 				Float[] kernel1D = kernelLines.get(0).getValue();
 				double[] values = new double[kernel1D.length];
 				for (int i = 0; i < kernel1D.length; i++)
 					values[i] = kernel1D[i];
 				
 				kernelX = kernelY = kernelZ = k1d.createCustomKernel1D(values, false).toSequence();
 			}
 			break;
 			
 			default:
 				kernelX = kernelY = kernelZ = k1d.toSequence();
 		}
 		
 		Sequence output = inSeq.getCopy();
 		
 		String directions = " along ";
 		if (linearX.getValue())
 			directions += "X";
 		if (linearY.getValue())
 			directions += "Y";
 		if (linearZ.getValue())
 			directions += "Z";
 		
 		if (useOpenCL.getValue())
 		{
 			// the kernel along X is ready
 			if (linearX.getValue())
 				convolutionCL.convolve(output, kernelX, zeroEdge.getValue(), iterations.getValue(), stopFlag);
 			
 			if (linearY.getValue())
 			{
 				// the kernel along Y must be rotated from X
 				Sequence kernelY_vertical = new Sequence(new IcyBufferedImage(1, kernelY.getSizeX(), 1, kernelY.getDataType_()));
 				System.arraycopy(kernelY.getDataXY(0, 0, 0), 0, kernelY_vertical.getDataXY(0, 0, 0), 0, kernelY.getSizeX());
 				convolutionCL.convolve(output, kernelY_vertical, zeroEdge.getValue(), iterations.getValue(), stopFlag);
 			}
 			
 			// no convolution along Z yet.
 		}
 		else
 		{
			Convolution1D.convolve(output, linearX.getValue() ? kernelX : null, linearY.getValue() ? kernelY : null, linearZ.getValue() ? kernelZ : null, iterations.getValue(), stopFlag);
 		}
 		
 		output.setName(inSeq.getName() + " * " + kernelX.getName() + directions);
 		output.updateComponentsBounds(true);
 		addSequence(output);
 	}
 	
 	@Override
 	public void clean()
 	{
 		if (useOpenCL.getValue())
 		{
 			queue.release();
 			context.release();
 		}
 	}
 	
 	@Override
 	public void stopExecution()
 	{
 		stopFlag.setValue(true);
 	}
 }
