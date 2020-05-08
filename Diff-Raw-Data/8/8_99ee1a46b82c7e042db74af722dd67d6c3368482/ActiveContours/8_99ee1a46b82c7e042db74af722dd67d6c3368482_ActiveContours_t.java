 package plugins.adufour.activecontours;
 
 import icy.image.IcyBufferedImage;
 import icy.main.Icy;
 import icy.painter.Painter;
 import icy.roi.ROI2D;
 import icy.roi.ROI2DArea;
 import icy.sequence.Sequence;
 import icy.swimmingPool.SwimmingObject;
 import icy.system.thread.ThreadUtil;
 import icy.type.DataType;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import javax.swing.Timer;
 import javax.vecmath.Point3d;
 import javax.vecmath.Point3i;
 
 import plugins.adufour.activecontours.SlidingWindow.Operation;
 import plugins.adufour.connectedcomponents.ConnectedComponent;
 import plugins.adufour.connectedcomponents.ConnectedComponents;
 import plugins.adufour.ezplug.EzException;
 import plugins.adufour.ezplug.EzGroup;
 import plugins.adufour.ezplug.EzPlug;
 import plugins.adufour.ezplug.EzStoppable;
 import plugins.adufour.ezplug.EzVar;
 import plugins.adufour.ezplug.EzVarBoolean;
 import plugins.adufour.ezplug.EzVarDouble;
 import plugins.adufour.ezplug.EzVarEnum;
 import plugins.adufour.ezplug.EzVarInteger;
 import plugins.adufour.ezplug.EzVarListener;
 import plugins.adufour.ezplug.EzVarSequence;
 import plugins.adufour.filtering.Convolution1D;
 import plugins.adufour.filtering.Kernels1D;
 import plugins.adufour.thresholder.Thresholder;
 import plugins.fab.trackmanager.TrackGroup;
 import plugins.fab.trackmanager.TrackPool;
 import plugins.fab.trackmanager.TrackSegment;
 import plugins.nchenouard.spot.Detection;
 
 public class ActiveContours extends EzPlug implements EzStoppable
 {
 	public enum Initialization
 	{
 		ROI, ISOVALUE
 	}
 	
 	public final EzVarSequence				input					= new EzVarSequence("Input");
 	public final EzVarEnum<Initialization>	init_type				= new EzVarEnum<Initialization>("Initialize from", Initialization.values());
 	public final EzVarDouble				init_isovalue			= new EzVarDouble("Isovalue", 1, 0, 1000000, 0.01);
 	
 	public final EzGroup					regul					= new EzGroup("Regularization");
 	public final EzVarBoolean				regul_flag				= new EzVarBoolean("Regularization", true);
 	public final EzVarDouble				regul_weight			= new EzVarDouble("weight", 0.1, 0, 1.0, 0.01);
 	
 	public final EzGroup					edge					= new EzGroup("Gradient-based evolution");
 	public final EzVarBoolean				edge_flag				= new EzVarBoolean("Use gradient", false);
 	public final EzVarSequence				edge_input				= new EzVarSequence("source data");
 	public final EzVarInteger				edge_c					= new EzVarInteger("Input channel", 0, 0, 1);
 	public final EzVarInteger				edge_z					= new EzVarInteger("Input Z slice", 0, 0, 1);
 	public final EzVarDouble				edge_weight				= new EzVarDouble("weight", 0, -1, 1, 0.1);
 	
 	public final EzGroup					region					= new EzGroup("Region-based evolution");
 	public final EzVarBoolean				region_flag				= new EzVarBoolean("Use region", true);
 	public final EzVarSequence				region_input			= new EzVarSequence("source data");
 	public final EzVarInteger				region_c				= new EzVarInteger("Input channel", 0, 0, 1);
 	public final EzVarInteger				region_z				= new EzVarInteger("Input Z slice", 0, 0, 1);
 	public final EzVarDouble				region_weight			= new EzVarDouble("weight", 1.0, 0.0, 1.0, 0.1);
 	public final EzVarDouble				region_sensitivity		= new EzVarDouble("sensitivity", 1.0, 0.1, 20.0, 0.1);
 	public final EzVarBoolean				coupling_flag			= new EzVarBoolean("Multi-contour coupling", true);
 	
 	public final EzGroup					axis					= new EzGroup("Long-axis constraint");
 	public final EzVarBoolean				axis_flag				= new EzVarBoolean("Use axis constraint", false);
 	public final EzVarDouble				axis_weight				= new EzVarDouble("weight", 0.5, 0.0, 1, 0.1);
 	
 	public final EzGroup					evolution				= new EzGroup("Evolution parameters");
 	public final EzVarDouble				contour_resolution		= new EzVarDouble("Contour resolution", 1.0, 0.1, 1000.0, 0.1);
 	public final EzVarInteger				contour_minArea			= new EzVarInteger("Contour min. area", 10, 1, 100000000, 1);
 	public final EzVarDouble				contour_timeStep		= new EzVarDouble("Evolution time step", 0.1, 0.1, 1, 0.01);
 	public final EzVarInteger				convergence_winSize		= new EzVarInteger("Convergence window size", 50, 10, 10000, 10);
 	public final EzVarEnum<Operation>		convergence_operation	= new EzVarEnum<SlidingWindow.Operation>("Convergence operation", Operation.values(), Operation.VAR_COEFF);
 	public final EzVarDouble				convergence_criterion	= new EzVarDouble("Convergence criterion", 0.001, 0, 0.1, 0.001);
 	
 	public final EzGroup					output					= new EzGroup("Output");
 	public final EzVarBoolean				output_rois				= new EzVarBoolean("Regions Of Interests", true);
 	public final EzVarBoolean				output_labels			= new EzVarBoolean("Labeled sequence", false);
 	
 	public final EzVarBoolean				tracking				= new EzVarBoolean("Tracking", false);
 	public final EzVarBoolean				updateMeans				= new EzVarBoolean("Update means", false);
 	
 	private IcyBufferedImage				edgeDataX, edgeDataY;
 	private IcyBufferedImage				region_data;
 	private IcyBufferedImage				region_local_mask;
 	private IcyBufferedImage				region_globl_mask;
 	private HashMap<ActiveContour, Double>	region_cin				= new HashMap<ActiveContour, Double>(0);
 	private double							region_cout;
 	private boolean							globalStop;
 	
 	private final TrackPool					trackPool				= new TrackPool();
 	private TrackGroup						trackGroup;
 	
 	private ActiveContoursPainter			painter;
 	
 	private ExecutorService					mainService				= Executors.newCachedThreadPool();
 	private ExecutorService					meanUpdaterService		= Executors.newCachedThreadPool();
 	
 	@Override
 	public void initialize()
 	{
 		addEzComponent(input);
 		
 		addEzComponent(init_type);
 		addEzComponent(init_isovalue);
 		init_type.addVisibilityTriggerTo(init_isovalue, Initialization.ISOVALUE);
 		
 		// regul
 		regul_flag.setToolTipText("Sets the contour(s) to remain smooth during its evolution");
 		addEzComponent(regul_flag);
 		regul.addEzComponent(regul_weight);
 		regul_weight.setToolTipText("Higher values result in a smoother contour, but may also slow its growth");
 		addEzComponent(regul);
 		regul_flag.addVisibilityTriggerTo(regul, true);
 		
 		// edge
 		edge_flag.setToolTipText("Sets the contour(s) to follow image intensity gradients");
 		addEzComponent(edge_flag);
 		edge_weight.setToolTipText("Negative (resp. positive) weight pushes contours toward decreasing (resp. increasing) intensities");
 		edge.addEzComponent(edge_input, edge_c, edge_z, edge_weight);
 		addEzComponent(edge);
 		edge_flag.addVisibilityTriggerTo(edge, true);
 		edge_input.addVarChangeListener(new EzVarListener<Sequence>()
 		{
 			@Override
 			public void variableChanged(EzVar<Sequence> source, Sequence newValue)
 			{
 				if (newValue != null)
 				{
 					int nC = newValue.getSizeC();
 					int nZ = newValue.getSizeZ();
 					
 					edge_c.setValue(0);
 					edge_c.setVisible(nC > 1);
 					edge_c.setMaxValue(nC - 1);
 					
 					edge_z.setValue(0);
 					edge_z.setVisible(nZ > 1);
 					edge_z.setMaxValue(nZ - 1);
 				}
 			}
 		});
 		
 		// region
 		region_flag.setToolTipText("Sets the contour(s) to isolate homogeneous intensity regions");
 		addEzComponent(region_flag);
 		region_sensitivity.setToolTipText("Higher values improve the sensitivity to dim objects");
 		region.addEzComponent(region_input, region_c, region_z, region_weight, region_sensitivity);
 		addEzComponent(region);
 		region_flag.addVisibilityTriggerTo(region, true);
 		region_input.addVarChangeListener(new EzVarListener<Sequence>()
 		{
 			@Override
 			public void variableChanged(EzVar<Sequence> source, Sequence newValue)
 			{
 				if (newValue != null)
 				{
 					int nC = newValue.getSizeC();
 					int nZ = newValue.getSizeZ();
 					
 					region_c.setValue(0);
 					region_c.setVisible(nC > 1);
 					region_c.setMaxValue(nC - 1);
 					
 					region_z.setValue(0);
 					region_z.setVisible(nZ > 1);
 					region_z.setMaxValue(nZ - 1);
 				}
 			}
 		});
 		
 		// axis contraint
 		axis_flag.setToolTipText("Sets the contour(s) to deform mostly along its principal axis");
 		addEzComponent(axis_flag);
 		axis_weight.setToolTipText("Higher values restrict the evolution along the principal axis");
 		axis.addEzComponent(axis_weight);
 		axis_flag.addVisibilityTriggerTo(axis, true);
 		addEzComponent(axis);
 		
 		// coupling
 		coupling_flag.setToolTipText("Prevents multiple contours from overlapping");
 		addEzComponent(coupling_flag);
 		
 		// contour
 		contour_resolution.setToolTipText("Sets the contour(s) precision as the distance (in pixels) between control points");
 		contour_minArea.setToolTipText("Contours with a surface (in pixels) below this value will be removed");
 		contour_timeStep.setToolTipText("Defines the evolution speed (warning: keep a low value to avoid vibration effects)");
 		convergence_winSize.setToolTipText("Defines over how many iterations the algorithm should check for convergence");
 		convergence_operation.setToolTipText("Defines the operation used to detect convergence");
 		convergence_criterion.setToolTipText("Defines the value of the criterion used to detect convergence");
 		evolution.addEzComponent(contour_resolution, contour_minArea, contour_timeStep, convergence_winSize, convergence_operation, convergence_criterion);
 		addEzComponent(evolution);
 		
 		// output
 		output_rois.setToolTipText("Clone the original sequence and with results overlayed as ROIs");
 		output_labels.setToolTipText("Creates a labeled sequence with all rasterized contours");
 		output.addEzComponent(output_rois, output_labels);
 		addEzComponent(output);
 		
 		tracking.setToolTipText("Track objects over time and export results to the track manager");
 		addEzComponent(tracking);
 	}
 	
 	@Override
 	public void execute()
 	{
 		globalStop = false;
 		
 		int startT = 0;
 		int endT = tracking.getValue() ? input.getValue().getSizeT() - 1 : startT;
 		
 		trackPool.clearTracks();
 		trackGroup = new TrackGroup(input.getValue());
 		trackPool.getTrackGroupList().add(trackGroup);
 		
 		if (tracking.getValue())
 		{
 			ThreadUtil.invokeLater(new Runnable()
 			{
 				public void run()
 				{
 					Icy.getMainInterface().getSwimmingPool().add(new SwimmingObject(trackGroup));
 				}
 			});
 		}
 		
 		if (getUI() != null)
 		{
 			// replace any ActiveContours Painter object on the sequence by ours
			for (Painter painter : input.getValue(true).getPainters())
 				if (painter instanceof ActiveContoursPainter) input.getValue().removePainter(painter);
 			
 			painter = new ActiveContoursPainter(trackGroup);
 			input.getValue().addPainter(painter);
 			
 			if (input.getValue().getFirstViewer() != null)
 			{
 				startT = input.getValue().getFirstViewer().getT();
 			}
 		}
 		
 		final Timer repaintTimer = new Timer(100, null);
 		repaintTimer.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent arg0)
 			{
 				if (input.getValue() != null)
 				{
 					input.getValue().painterChanged(painter);
 				}
 				else
 				{
 					repaintTimer.stop();
 				}
 			}
 		});
 		
 		Sequence outputSequence_rois = output_rois.getValue() ? input.getValue().getCopy() : null;
 		Sequence outputSequence_labels = output_labels.getValue() ? new Sequence() : null;
 		
 		for (int t = startT; t <= endT; t++)
 		{
 			if (globalStop) break;
 			if (getUI() != null && input.getValue().getFirstViewer() != null) input.getValue().getFirstViewer().setT(t);
 			initContours(t, t == startT);
 			repaintTimer.start();
 			evolveContours(t);
 			repaintTimer.stop();
 			
 			// store detections and results
 			storeResult(outputSequence_rois, outputSequence_labels, t);
 		}
 		
 		if (getUI() != null)
 		{
 			getUI().setProgressBarValue(0.0);
 			
 			if (output_rois.getValue())
 			{
 				outputSequence_rois.setName(input.getValue().getName() + " + active contours");
 				addSequence(outputSequence_rois);
 			}
 			if (output_labels.getValue())
 			{
 				outputSequence_labels.setName("labels found in " + input.getValue().getName());
 				addSequence(outputSequence_labels);
 			}
 			SwimmingObject object = new SwimmingObject(trackGroup);
 			trackPool.addResult(object);
 		}
 	}
 	
 	private void initContours(int t, boolean isFirstImage)
 	{
 		// Initialize the image data
 		
 		if (edge_flag.getValue())
 		{
			IcyBufferedImage inputImage = edge_input.getValue(true).getImage(t, region_z.getValue());
 			
 			if (inputImage.getSizeC() > 1) inputImage = inputImage.extractChannel(region_c.getValue());
 			
 			Sequence gradient = Kernels1D.GRADIENT.toSequence();
 			Sequence gaussian = Kernels1D.CUSTOM_GAUSSIAN.createGaussianKernel1D(1.0).toSequence();
 			
 			Sequence gradX = new Sequence(inputImage.convertToType(DataType.DOUBLE, true));
 			
 			// smooth the signal first
 			Convolution1D.convolve(gradX, gaussian, gaussian, null);
 			
 			// clone into gradY
 			Sequence gradY = gradX.getCopy();
 			
 			// compute the gradient in each direction
 			Convolution1D.convolve(gradX, gradient, null, null);
 			Convolution1D.convolve(gradY, null, gradient, null);
 			
 			edgeDataX = gradX.getFirstImage();
 			edgeDataY = gradY.getFirstImage();
 		}
 		
 		if (region_flag.getValue())
 		{
			IcyBufferedImage inputImage = region_input.getValue(true).getImage(t, region_z.getValue());
 			
 			if (inputImage.getSizeC() > 1) inputImage = inputImage.extractChannel(region_c.getValue());
 			
 			region_data = inputImage.convertToType(DataType.DOUBLE, true);
 			region_local_mask = new IcyBufferedImage(region_data.getWidth(), region_data.getHeight(), 1, DataType.UBYTE);
 			region_globl_mask = new IcyBufferedImage(region_data.getWidth(), region_data.getHeight(), 1, DataType.UBYTE);
 			region_cin.clear();
 		}
 		
 		// Initialize the contours
 		
 		if (isFirstImage)
 		{
 			Sequence inSeq = input.getValue();
 			
 			// // remove existing ActiveContourPainters and track segments if any
 			// for (Painter p : inSeq.getPainters())
 			// if (p instanceof ActiveContoursPainter)
 			// inSeq.removePainter(p);
 			
 			trackGroup.getTrackSegmentList().clear();
 			
 			switch (init_type.getValue())
 			{
 				case ROI:
 					
 					if (inSeq.getROI2Ds().isEmpty()) throw new EzException("Please draw or select a ROI", true);
 					
 					for (ROI2D roi : inSeq.getROI2Ds())
 					{
 						if (roi instanceof ROI2DArea)
 						{
 							// special case: check if the area has multiple components => split them
 							ROI2DArea area = (ROI2DArea) roi;
 							IcyBufferedImage binImg = new IcyBufferedImage(inSeq.getWidth(), inSeq.getHeight(), 1, DataType.UBYTE);
 							byte[] array = binImg.getDataXYAsByte(0);
 							boolean[] mask = area.getAsBooleanMask(0, 0, input.getValue().getWidth(), input.getValue().getHeight());
 							int off = 0;
 							for (int j = 0; j < inSeq.getSizeY(); j++)
 								for (int i = 0; i < inSeq.getSizeX(); i++, off++)
 									if (mask[off]) array[off] = (byte) 1;
 							initFromBinaryImage(binImg, t);
 						}
 						else
 						{
 							try
 							{
 								final ActiveContour contour = new ActiveContour(this, contour_resolution, contour_minArea, new SlidingWindow(convergence_winSize.getValue()), roi);
 								contour.setX(roi.getBounds2D().getCenterX());
 								contour.setY(roi.getBounds2D().getCenterY());
 								contour.setT(t);
 								TrackSegment segment = new TrackSegment();
 								segment.addDetection(contour);
 								trackGroup.getTrackSegmentList().add(segment);
 							}
 							catch (TopologyException topo)
 							{
 								String message = "Warning: a contour could not be triangulated. Possible reasons:\n";
 								message += " - binary mask is below the minimum contour area\n";
 								message += " - the binary mask contains a hole";
 								System.err.println(message);
 							}
 							catch (Exception e)
 							{
 								System.err.println("Unable to initialize the contour");
 								e.printStackTrace();
 							}
 						}
 					}
 				break;
 				
 				case ISOVALUE:
 					
 					IcyBufferedImage binImg = input.getValue().getImage(t, region_z.getValue()).getCopy();
 					
 					Thresholder.threshold(new Sequence(binImg), 0, new double[] { init_isovalue.getValue() }, true);
 					
 					initFromBinaryImage(binImg, t);
 				
 				break;
 				
 				default:
 				{
 					throw new EzException(init_type.getValue().name() + " initialization is currently not supported", true);
 				}
 			}
 		}
 		else
 		{
 			for (TrackSegment segment : trackGroup.getTrackSegmentList())
 			{
 				Detection previous = segment.getDetectionAtTime(t - 1);
 				
 				if (previous == null) continue;
 				
 				ActiveContour clone = new ActiveContour((ActiveContour) previous);
 				clone.setT(t);
 				segment.addDetection(clone);
 			}
 		}
 	}
 	
 	private void initFromBinaryImage(IcyBufferedImage binImg, int t)
 	{
 		for (ConnectedComponent cc : ConnectedComponents.extractConnectedComponents(new Sequence(binImg), null).get(0))
 		{
 			ROI2DArea roi = new ROI2DArea();
 			
 			for (Point3i pt : cc)
 				roi.addPoint(pt.x, pt.y);
 			
 			try
 			{
 				final ActiveContour contour = new ActiveContour(this, contour_resolution, contour_minArea, new SlidingWindow(convergence_winSize.getValue()), roi);
 				contour.setX(roi.getBounds2D().getCenterX());
 				contour.setY(roi.getBounds2D().getCenterY());
 				contour.setT(t);
 				TrackSegment segment = new TrackSegment();
 				segment.addDetection(contour);
 				trackGroup.getTrackSegmentList().add(segment);
 			}
 			catch (TopologyException e1)
 			{
 				String message = "Warning: contour could not be triangulated. Possible reasons:\n";
 				message += " - binary mask is below the minimum contour area\n";
 				System.out.println(message);
 			}
 			
 		}
 	}
 	
 	private void evolveContours(final int t)
 	{
 		// retrieve the contours on the current frame and store them in currentContours
 		final HashSet<ActiveContour> allContours = new HashSet<ActiveContour>(trackGroup.getTrackSegmentList().size());
 		
 		for (TrackSegment segment : trackGroup.getTrackSegmentList())
 		{
 			Detection det = segment.getDetectionAtTime(t);
 			if (det != null) allContours.add((ActiveContour) det);
 		}
 		
 		if (allContours.size() == 0) return;
 		
 		Rectangle field = new Rectangle(input.getValue().getWidth(), input.getValue().getHeight());
 		
 		int nbConvergedContours = 0;
 		
 		long iter = 0;
 		
 		if (region_flag.getValue()) updateRegionMeans(meanUpdaterService, allContours, t);
 		
 		final HashSet<ActiveContour> evolvingContours = new HashSet<ActiveContour>(allContours.size());
 		
 		while (!globalStop && nbConvergedContours < allContours.size())
 		{
 			iter++;
 			nbConvergedContours = 0;
 			
 			// update region information every 10 iterations
 			if (region_flag.getValue() && iter % 10 == 0) updateRegionMeans(meanUpdaterService, allContours, t);
 			
 			// take a snapshot of the current list of evolving (i.e. non-converged) contours
 			evolvingContours.clear();
 			for (ActiveContour contour : allContours)
 			{
 				Double criterion = contour.convergence.computeCriterion(convergence_operation.getValue());
 				
 				if (criterion != null && criterion < convergence_criterion.getValue())
 				{
 					nbConvergedContours++;
 					if (getUI() != null) getUI().setProgressBarValue((double) nbConvergedContours / allContours.size());
 					continue;
 				}
 				
 				// if the contour hasn't converged yet, store it for the main loop
 				evolvingContours.add(contour);
 			}
 			
 			// re-sample the contours to ensure homogeneous resolution
 			resampleContours(mainService, meanUpdaterService, evolvingContours, allContours, t);
 			
 			// if coupling is required, contours should all be deformed synchronously
 			if (coupling_flag.getValue())
 			{
 				// compute deformations issued from the energy minimization
 				computeDeformations(mainService, evolvingContours, allContours);
 				
 				// once they are all computed, apply all deformations at once
 				applyDeformations(evolvingContours, field);
 			}
 			else
 			{
 				// contours can be evolved independently from one another
 				deformContoursAsync(mainService, evolvingContours, field);
 			}
 		}
 	}
 	
 	private void deformContoursAsync(ExecutorService service, HashSet<ActiveContour> contours, final Rectangle field)
 	{
 		ArrayList<Future<ActiveContour>> tasks = new ArrayList<Future<ActiveContour>>(contours.size());
 		
 		for (final ActiveContour contour : contours)
 		{
 			tasks.add(service.submit(new Runnable()
 			{
 				public void run()
 				{
 					if (edge_flag.getValue()) contour.updateEdgeForces(edgeDataX, edgeDataY, edge_weight.getValue());
 					
 					if (regul_flag.getValue()) contour.updateInternalForces(regul_weight.getValue());
 					
 					if (region_flag.getValue()) contour.updateRegionForces(region_data, region_weight.getValue(), region_cin.get(contour), region_cout, region_sensitivity.getValue());
 					
 					if (axis_flag.getValue()) contour.updateAxisForces(axis_weight.getValue());
 					
 					contour.move(field, true, contour_timeStep.getValue());
 				}
 			}, contour));
 		}
 		
 		for (Future<?> future : tasks)
 			try
 			{
 				future.get();
 			}
 			catch (InterruptedException e1)
 			{
 				e1.printStackTrace();
 			}
 			catch (ExecutionException e1)
 			{
 				e1.printStackTrace();
 			}
 		
 	}
 	
 	private void updateRegionMeans(ExecutorService service, Collection<ActiveContour> contours, int t)
 	{
 		ArrayList<Future<?>> tasks = new ArrayList<Future<?>>(contours.size());
 		
 		final double[] _region_data = region_data.getDataXYAsDouble(0);
 		
 		byte[] _globlMask = region_globl_mask.getDataXYAsByte(0);
 		Arrays.fill(_globlMask, (byte) 0);
 		final Graphics2D g_globl_mask = region_globl_mask.createGraphics();
 		
 		final byte[] _localMask = region_local_mask.getDataXYAsByte(0);
 		final Graphics2D g_local_mask = region_local_mask.createGraphics();
 		
 		for (final ActiveContour contour : contours)
 		{
 			tasks.add(service.submit(new Runnable()
 			{
 				public void run()
 				{
 					double inSum = 0, inCpt = 0;
 					
 					// create a mask for each object for interior mean measuring
 					Arrays.fill(_localMask, (byte) 0);
 					g_local_mask.fill(contour.path);
 					
 					for (int i = 0; i < _localMask.length; i++)
 					{
 						if (_localMask[i] != 0)
 						{
 							inSum += _region_data[i];
 							inCpt++;
 						}
 					}
 					
 					region_cin.put(contour, inSum / inCpt);
 					
 					// add the contour to the global mask for background mean measuring
 					g_globl_mask.fill(contour.path);
 				}
 			}));
 			
 			for (Future<?> future : tasks)
 				try
 				{
 					future.get();
 				}
 				catch (InterruptedException e)
 				{
 				}
 				catch (ExecutionException e)
 				{
 				}
 		}
 		
 		double outSum = 0, outCpt = 0;
 		for (int i = 0; i < _globlMask.length; i++)
 		{
 			if (_globlMask[i] == 0)
 			{
 				outSum += _region_data[i];
 				outCpt++;
 			}
 		}
 		region_cout = outSum / outCpt;
 	}
 	
 	private void resampleContours(ExecutorService service, final ExecutorService meanUpdaterService, final HashSet<ActiveContour> evolvingContours, final HashSet<ActiveContour> allContours,
 			final int t)
 	{
 		ArrayList<Callable<ActiveContour>> tasks = new ArrayList<Callable<ActiveContour>>(evolvingContours.size());
 		
 		for (final ActiveContour contour : evolvingContours)
 		{
 			tasks.add(new Callable<ActiveContour>()
 			{
 				public ActiveContour call()
 				{
 					try
 					{
 						contour.reSample(0.7, 1.4);
 					}
 					catch (TopologyException e)
 					{
 						allContours.remove(contour);
 						evolvingContours.remove(contour);
 						
 						TrackSegment motherSegment = null;
 						
 						ArrayList<TrackSegment> segments = trackGroup.getTrackSegmentList();
 						for (int i = 0; i < segments.size(); i++)
 						{
 							TrackSegment segment = segments.get(i);
 							
 							if (segment.containsDetection(contour))
 							{
 								segment.removeDetection(contour);
 								
 								if (segment.getDetectionList().size() == 0)
 								{
 									segments.remove(i--);
 								}
 								else
 								{
 									motherSegment = segment;
 								}
 								break;
 							}
 						}
 						
 						for (ActiveContour child : e.children)
 						{
 							try
 							{
 								child.reSample(0.7, 1.4);
 								allContours.add(child);
 								evolvingContours.add(child);
 								TrackSegment childSegment = new TrackSegment();
 								childSegment.addDetection(child);
 								trackGroup.addTrackSegment(childSegment);
 								if (motherSegment != null && motherSegment.getDetectionList().size() > 0) trackPool.createLink(motherSegment, childSegment);
 							}
 							catch (TopologyException tpE)
 							{
 								// do nothing (the child will just not be added)
 							}
 						}
 						
 						if (region_flag.getValue()) updateRegionMeans(meanUpdaterService, allContours, t);
 					}
 					catch (NullPointerException npe)
 					{
 						allContours.remove(contour);
 						evolvingContours.remove(contour);
 						
 						ArrayList<TrackSegment> segments = trackGroup.getTrackSegmentList();
 						for (int i = 0; i < segments.size(); i++)
 						{
 							TrackSegment segment = segments.get(i);
 							
 							if (segment.containsDetection(contour))
 							{
 								segment.removeDetection(contour);
 								
 								if (segment.getDetectionList().size() == 0)
 								{
 									segments.remove(i--);
 								}
 								
 								break;
 							}
 						}
 						
 						if (region_flag.getValue()) updateRegionMeans(meanUpdaterService, allContours, t);
 					}
 					
 					return contour;
 				}
 			});
 		}
 		
 		try
 		{
 			service.invokeAll(tasks);
 		}
 		catch (InterruptedException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	private void computeDeformations(ExecutorService service, final HashSet<ActiveContour> evolvingContours, final HashSet<ActiveContour> allContours)
 	{
 		// ArrayList<Future<ActiveContour>> tasks = new
 		// ArrayList<Future<ActiveContour>>(evolvingContours.size());
 		//
 		// for (final ActiveContour contour : evolvingContours)
 		// {
 		// tasks.add(service.submit(new Runnable()
 		// {
 		// public void run()
 		// {
 		// if (edge_flag.getValue()) contour.updateEdgeForces(edgeDataX, edgeDataY,
 		// edge_weight.getValue());
 		//
 		// if (regul_flag.getValue()) contour.updateInternalForces(regul_weight.getValue());
 		//
 		// if (region_flag.getValue()) contour.updateRegionForces(region_data,
 		// region_weight.getValue(), region_cin.get(contour), region_cout,
 		// region_sensitivity.getValue());
 		//
 		// if (axis_flag.getValue()) contour.updateAxisForces(axis_weight.getValue());
 		//
 		// if (coupling_flag.getValue())
 		// {
 		// // warning: feedback must be computed against ALL contours
 		// // (including those which have already converged)
 		// for (ActiveContour otherContour : allContours)
 		// {
 		// if (otherContour == null || otherContour == contour) continue;
 		//
 		// contour.updateFeedbackForces(otherContour);
 		// }
 		// }
 		// }
 		// }, contour));
 		// }
 		//
 		// for (Future<?> future : tasks)
 		// try
 		// {
 		// future.get();
 		// }
 		// catch (InterruptedException e1)
 		// {
 		// e1.printStackTrace();
 		// }
 		// catch (ExecutionException e1)
 		// {
 		// e1.printStackTrace();
 		// }
 		
 		ArrayList<Callable<ActiveContour>> tasks = new ArrayList<Callable<ActiveContour>>(evolvingContours.size());
 		
 		for (final ActiveContour contour : evolvingContours)
 		{
 			tasks.add(new Callable<ActiveContour>()
 			{
 				public ActiveContour call()
 				{
 					if (edge_flag.getValue()) contour.updateEdgeForces(edgeDataX, edgeDataY, edge_weight.getValue());
 					
 					if (regul_flag.getValue()) contour.updateInternalForces(regul_weight.getValue());
 					
 					if (region_flag.getValue()) contour.updateRegionForces(region_data, region_weight.getValue(), region_cin.get(contour), region_cout, region_sensitivity.getValue());
 					
 					if (axis_flag.getValue()) contour.updateAxisForces(axis_weight.getValue());
 					
 					if (coupling_flag.getValue())
 					{
 						// warning: feedback must be computed against ALL contours
 						// (including those which have already converged)
 						for (ActiveContour otherContour : allContours)
 						{
 							if (otherContour == null || otherContour == contour) continue;
 							
 							contour.updateFeedbackForces(otherContour);
 						}
 					}
 					
 					return contour;
 				}
 			});
 		}
 		
 		try
 		{
 			service.invokeAll(tasks);
 		}
 		catch (InterruptedException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	private void applyDeformations(HashSet<ActiveContour> contours, Rectangle field)
 	{
 		for (ActiveContour contour : contours)
 			contour.move(field, true, contour_timeStep.getValue());
 	}
 	
 	private void storeResult(Sequence rois, Sequence labels, int t)
 	{
 		ArrayList<TrackSegment> segments = trackGroup.getTrackSegmentList();
 		
 		IcyBufferedImage labelsIMG = null;
 		if (output_labels.getValue())
 		{
 			labelsIMG = new IcyBufferedImage(input.getValue().getWidth(), input.getValue().getHeight(), 1, DataType.UBYTE);
 			labels.setImage(t, 0, labelsIMG);
 		}
 		
 		for (int i = 1; i <= segments.size(); i++)
 		{
 			TrackSegment segment = segments.get(i - 1);
 			
 			ActiveContour contour = (ActiveContour) segment.getDetectionAtTime(t);
 			if (contour == null) continue;
 			
 			// store detection parameters
 			Point3d center = new Point3d();
 			for (Point3d p : contour.points)
 				center.add(p);
 			center.scale(1.0 / contour.points.size());
 			contour.setX(center.x);
 			contour.setY(center.y);
 			
 			// output as ROIs
 			if (output_rois.getValue())
 			{
 				ROI2DArea area = new ROI2DArea();
 				area.addShape(contour.path);
 				area.setColor(contour.getColor());
 				area.setT(t);
 				area.setName("Object #" + i);
 				rois.addROI(area, false);
 			}
 			
 			// output as labels
 			if (output_labels.getValue())
 			{
 				Graphics2D g = labelsIMG.createGraphics();
 				g.setColor(new Color(i));
 				g.fill(contour.path);
 			}
 		}
 	}
 	
 	@Override
 	public void clean()
 	{
 		if (input.getValue() != null) input.getValue().removePainter(painter);
 		
 		// contoursMap.clear();
 		// contours.clear();
 		// trackGroup.clearTracks();
 		if (region_flag.getValue()) region_cin.clear();
 	}
 	
 	@Override
 	public void stopExecution()
 	{
 		globalStop = true;
 	}
 	
 }
