 package plugins.adufour.activecontours;
 
 import icy.gui.viewer.Viewer;
 import icy.image.IcyBufferedImage;
 import icy.image.IcyBufferedImageUtil;
 import icy.main.Icy;
 import icy.roi.BooleanMask2D;
 import icy.roi.ROI;
 import icy.roi.ROI2D;
 import icy.roi.ROI2DArea;
 import icy.roi.ROI2DRectangle;
 import icy.sequence.DimensionId;
 import icy.sequence.Sequence;
 import icy.sequence.SequenceUtil;
 import icy.swimmingPool.SwimmingObject;
 import icy.system.IcyHandledException;
 import icy.system.SystemUtil;
 import icy.system.thread.ThreadUtil;
 import icy.type.DataType;
 import icy.type.collection.array.Array1DUtil;
 import icy.util.ShapeUtil.ShapeOperation;
 import icy.util.StringUtil;
 
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import javax.vecmath.Point3d;
 
 import plugins.adufour.activecontours.SlidingWindow.Operation;
 import plugins.adufour.blocks.lang.Block;
 import plugins.adufour.blocks.util.VarList;
 import plugins.adufour.ezplug.EzException;
 import plugins.adufour.ezplug.EzGroup;
 import plugins.adufour.ezplug.EzPlug;
 import plugins.adufour.ezplug.EzStoppable;
 import plugins.adufour.ezplug.EzVar;
 import plugins.adufour.ezplug.EzVarBoolean;
 import plugins.adufour.ezplug.EzVarDimensionPicker;
 import plugins.adufour.ezplug.EzVarDouble;
 import plugins.adufour.ezplug.EzVarEnum;
 import plugins.adufour.ezplug.EzVarInteger;
 import plugins.adufour.ezplug.EzVarListener;
 import plugins.adufour.ezplug.EzVarSequence;
 import plugins.adufour.filtering.Convolution1D;
 import plugins.adufour.filtering.ConvolutionException;
 import plugins.adufour.filtering.Kernels1D;
 import plugins.adufour.vars.lang.VarBoolean;
 import plugins.adufour.vars.lang.VarROIArray;
 import plugins.fab.trackmanager.TrackGroup;
 import plugins.fab.trackmanager.TrackManager;
 import plugins.fab.trackmanager.TrackSegment;
 import plugins.nchenouard.spot.Detection;
 
 public class ActiveContours extends EzPlug implements EzStoppable, Block
 {
     private final double                   EPSILON                 = 0.0000001;
     
     private final EzVarBoolean             showAdvancedOptions     = new EzVarBoolean("Show advanced options", false);
     
     public final EzVarSequence             input                   = new EzVarSequence("Input");
     private Sequence                       inputData;
     
     public final EzVarDouble               init_isovalue           = new EzVarDouble("Isovalue", 1, 0, 1000000, 0.01);
     
     public final EzVarDouble               regul_weight            = new EzVarDouble("Contour smoothness", 0.05, 0, 1.0, 0.01);
     
     public final EzGroup                   edge                    = new EzGroup("Find bright/dark edges");
     public final EzVarDimensionPicker      edge_c                  = new EzVarDimensionPicker("Find edges in channel", DimensionId.C, input);
     public final EzVarDouble               edge_weight             = new EzVarDouble("Edge weight", 0, -1, 1, 0.1);
     
     public final EzGroup                   region                  = new EzGroup("Find homogeneous intensity areas");
     public final EzVarDimensionPicker      region_c                = new EzVarDimensionPicker("Find regions in channel", DimensionId.C, input);
     public final EzVarDouble               region_weight           = new EzVarDouble("Region weight", 1.0, 0.0, 1.0, 0.1);
     
     public final EzVarDouble               balloon_weight          = new EzVarDouble("Contour inflation", 0, -0.5, 0.5, 0.001);
     
     public final EzVarDouble               axis_weight             = new EzVarDouble("Axis constraint", 0, 0.0, 1, 0.1);
     
     public final EzVarBoolean              coupling_flag           = new EzVarBoolean("Multi-contour coupling", true);
     
     public final EzGroup                   evolution               = new EzGroup("Evolution parameters");
     public final EzVarSequence             evolution_bounds        = new EzVarSequence("Bound field to ROI of");
     public final EzVarDouble               contour_resolution      = new EzVarDouble("Contour resolution", 2, 0.1, 1000.0, 0.1);
     public final EzVarInteger              contour_minArea         = new EzVarInteger("Contour min. area", 10, 1, 100000000, 1);
     public final EzVarDouble               contour_timeStep        = new EzVarDouble("Evolution time step", 0.1, 0.1, 10, 0.01);
     public final EzVarInteger              convergence_winSize     = new EzVarInteger("Convergence window size", 50, 10, 10000, 10);
     public final EzVarEnum<Operation>      convergence_operation   = new EzVarEnum<SlidingWindow.Operation>("Convergence operation", Operation.values(), Operation.VAR_COEFF);
     public final EzVarDouble               convergence_criterion   = new EzVarDouble("Convergence criterion", 0.001, 0, 0.1, 0.001);
     
     public final EzVarBoolean              output_rois             = new EzVarBoolean("Regions of interest (ROI)", true);
     
     public final EzVarBoolean              tracking                = new EzVarBoolean("Track objects over time", false);
     
     private IcyBufferedImage               edgeDataX, edgeDataY;
     private IcyBufferedImage               region_data;
     private IcyBufferedImage               region_local_mask;
     private Graphics2D                     region_local_mask_graphics;
     byte[]                                 _region_globl_mask;
     
     private IcyBufferedImage               region_globl_mask;
     private Graphics2D                     region_globl_mask_graphics;
     private HashMap<ActiveContour, Double> region_cin              = new HashMap<ActiveContour, Double>(0);
     private HashMap<ActiveContour, Double> region_sin              = new HashMap<ActiveContour, Double>(0);
     private double                         region_cout;
     private double                         region_sout;
     
     private VarROIArray                    roiInput                = new VarROIArray("input ROI");
     private VarROIArray                    roiOutput               = new VarROIArray("Regions of interest");
     
     private boolean                        globalStop;
     
     private TrackGroup                     trackGroup;
     
     private ActiveContoursOverlay          painter;
     
     private ExecutorService                contourEvolutionService = Executors.newFixedThreadPool(SystemUtil.getAvailableProcessors());
     private ExecutorService                meanUpdateService       = Executors.newFixedThreadPool(SystemUtil.getAvailableProcessors());
     
     @Override
     public void initialize()
     {
         addEzComponent(showAdvancedOptions);
         
         addEzComponent(input);
         
         // regul
         regul_weight.setToolTipText("Higher values result in a smoother contour, but may also slow its growth");
         addEzComponent(regul_weight);
         
         // edge
         edge.setToolTipText("Sets the contour(s) to follow image intensity gradients");
         edge_weight.setToolTipText("Negative (resp. positive) weight pushes contours toward decreasing (resp. increasing) intensities");
         edge.addEzComponent(edge_c, edge_weight);
         addEzComponent(edge);
         
         // region
         region.setToolTipText("Sets the contour(s) to isolate homogeneous intensity regions");
         region_weight.setToolTipText("Set to 0 to deactivate this parameter");
         region.addEzComponent(region_c, region_weight);
         addEzComponent(region);
         
         // balloon force
         balloon_weight.setToolTipText("Positive (resp. negative) values will inflate (resp. deflate) the contour");
         addEzComponent(balloon_weight);
         
         // axis contraint
         axis_weight.setToolTipText("Higher values restrict the evolution along the principal axis");
         addEzComponent(axis_weight);
         
         // coupling
         coupling_flag.setToolTipText("Prevents multiple contours from overlapping");
         addEzComponent(coupling_flag);
         
         // contour
         contour_resolution.setToolTipText("Sets the contour(s) precision as the distance (in pixels) between control points");
         
         contour_minArea.setToolTipText("Contours with a surface (in pixels) below this value will be removed");
         showAdvancedOptions.addVisibilityTriggerTo(contour_minArea, true);
         
         contour_timeStep.setToolTipText("Defines the evolution speed (warning: keep a low value to avoid vibration effects)");
         
         convergence_winSize.setToolTipText("Defines over how many iterations the algorithm should check for convergence");
         showAdvancedOptions.addVisibilityTriggerTo(convergence_winSize, true);
         
         convergence_operation.setToolTipText("Defines the operation used to detect convergence");
         showAdvancedOptions.addVisibilityTriggerTo(convergence_operation, true);
         
         convergence_criterion.setToolTipText("Defines the value of the criterion used to detect convergence");
         
         evolution_bounds.setNoSequenceSelection();
         evolution_bounds.setToolTipText("Bounds the evolution of the contour to all ROI of the given sequence (select \"No sequence\" to deactivate)");
         showAdvancedOptions.addVisibilityTriggerTo(evolution_bounds, true);
         
         evolution.addEzComponent(evolution_bounds, contour_resolution, contour_minArea, contour_timeStep, convergence_winSize, convergence_operation, convergence_criterion);
         addEzComponent(evolution);
         
         contour_resolution.addVarChangeListener(new EzVarListener<Double>()
         {
             @Override
             public void variableChanged(EzVar<Double> source, Double newValue)
             {
                 convergence_winSize.setValue((int) (100.0 / newValue));
             }
         });
         
         // output
         output_rois.setToolTipText("Clone the original sequence and with results overlayed as ROIs");
         addEzComponent(output_rois);
         
         tracking.setToolTipText("Track objects over time and export results to the track manager");
         addEzComponent(tracking);
         
         setTimeDisplay(true);
     }
     
     @Override
     public void execute()
     {
         roiOutput.setValue(null);
         inputData = input.getValue(true);
         
         globalStop = false;
         
         int startT = inputData.getFirstViewer() == null ? 0 : inputData.getFirstViewer().getPositionT();
         int endT = tracking.getValue() ? inputData.getSizeT() - 1 : startT;
         
         ThreadUtil.invokeNow(new Runnable()
         {
             @Override
             public void run()
             {
                 trackGroup = new TrackGroup(inputData);
                 trackGroup.setDescription("Active contours (" + new Date().toString() + ")");
                 if (tracking.getValue())
                 {
                     SwimmingObject object = new SwimmingObject(trackGroup); 
                     Icy.getMainInterface().getSwimmingPool().add(object);
                 }
             }
         });
         
         // replace any ActiveContours Painter object on the sequence by ours
         for (icy.painter.Painter painter : inputData.getPainters())
             if (painter instanceof ActiveContoursOverlay) inputData.removePainter(painter);
         
         painter = new ActiveContoursOverlay(trackGroup);
         inputData.addPainter(painter);
         
         if (getUI() != null)
         {
             roiInput.setValue(new ROI[0]);
             
             if (inputData.getFirstViewer() != null)
             {
                 startT = inputData.getFirstViewer().getPositionT();
             }
         }
         
         Sequence outputSequence_rois = output_rois.getValue() ? SequenceUtil.getCopy(inputData) : null;
         
         for (int t = startT; t <= endT; t++)
         {
             if (inputData.getFirstViewer() != null) inputData.getFirstViewer().setPositionT(t);
             initContours(t, t == startT);
             evolveContours(t);
             ThreadUtil.sleep(200);
             
             // store detections and results
             storeResult(t);
             
             if (Thread.currentThread().isInterrupted()) globalStop = true;
             
             if (globalStop) break;
         }
         
         if (getUI() != null)
         {
             getUI().setProgressBarValue(0.0);
             
             if (output_rois.getValue() && outputSequence_rois != null)
             {
                 outputSequence_rois.setName(inputData.getName() + " + active contours");
                 for (ROI roi : roiOutput.getValue())
                     outputSequence_rois.addROI(roi, false);
                 addSequence(outputSequence_rois);
             }
             
             if (tracking.getValue())
             {
                 ThreadUtil.invokeLater(new Runnable()
                 {
                     public void run()
                     {
                         TrackManager tm = new TrackManager();
                         tm.reOrganize();
                         tm.setDisplaySequence(inputData);
                     }
                 });
             }
         }
         else
         {
             // possibly block mode, remove the painter after processing
             // if (inputData != null) inputData.removePainter(painter);
         }
     }
     
     private void initContours(final int t, boolean isFirstImage)
     {
         // 1) Initialize the edge data
         
         Viewer v = inputData.getFirstViewer();
         
         int z = v == null ? 0 : inputData.getFirstViewer().getPositionZ();
         
         IcyBufferedImage edgeInputData = inputData.getSizeC() > 1 ? inputData.getImage(t, z, edge_c.getValue()) : inputData.getImage(t, z);
         
         if (edgeInputData == null) throw new IcyHandledException("The edge input data is invalid. Make sure the selected channel is valid.");
         
         Sequence gradient = Kernels1D.GRADIENT.toSequence();
         Sequence gaussian = Kernels1D.CUSTOM_GAUSSIAN.createGaussianKernel1D(0.5).toSequence();
         
         Sequence gradX = new Sequence(IcyBufferedImageUtil.convertToType(edgeInputData, DataType.DOUBLE, true, true));
         
         // smooth the signal first
         try
         {
             Convolution1D.convolve(gradX, gaussian, gaussian, null);
         }
         catch (ConvolutionException e)
         {
             throw new EzException("Cannot smooth the signal: " + e.getMessage(), true);
         }
         
         // clone into gradY
         Sequence gradY = SequenceUtil.getCopy(gradX);
         
         // compute the gradient in each direction
         try
         {
             Convolution1D.convolve(gradX, gradient, null, null);
             Convolution1D.convolve(gradY, null, gradient, null);
         }
         catch (ConvolutionException e)
         {
             throw new EzException("Cannot compute the gradient information: " + e.getMessage(), true);
         }
         
         edgeDataX = gradX.getFirstImage();
         edgeDataY = gradY.getFirstImage();
         
         // 2) initialize the region data
         
         IcyBufferedImage regionInputData = inputData.getSizeC() > 1 ? inputData.getImage(t, z, region_c.getValue()) : inputData.getImage(t, z);
         
         if (regionInputData == null) throw new IcyHandledException("The region input data is invalid.  Make sure the selected channel is valid.");
         
         region_data = IcyBufferedImageUtil.convertToType(regionInputData, DataType.DOUBLE, true, true);
         
         if (isFirstImage)
         {
             region_local_mask = new IcyBufferedImage(region_data.getWidth(), region_data.getHeight(), 1, DataType.UBYTE);
             region_local_mask_graphics = region_local_mask.createGraphics();
             region_globl_mask = new IcyBufferedImage(region_data.getWidth(), region_data.getHeight(), 1, DataType.UBYTE);
             region_globl_mask_graphics = region_globl_mask.createGraphics();
         }
         
         // 1) Initialize the contours
         
         if (isFirstImage)
         {
             // // remove existing ActiveContourPainters and track segments if any
             // for (Painter p : inSeq.getPainters())
             // if (p instanceof ActiveContoursPainter)
             // inSeq.removePainter(p);
             
             if (roiInput.getValue().length == 0)
             {
                 ArrayList<ROI2D> roiFromSequence = inputData.getROI2Ds();
                 
                 if (roiFromSequence.isEmpty()) throw new EzException("Please draw or select a ROI", true);
                 
                 roiInput.setValue(roiFromSequence.toArray(new ROI2D[roiFromSequence.size()]));
             }
             
             ArrayList<Future<?>> tasks = new ArrayList<Future<?>>(roiInput.getValue().length);
             
             for (ROI roi : roiInput.getValue())
             {
                 if (!(roi instanceof ROI2D))
                 {
                     System.err.println("Warning: skipped non-2D ROI");
                     continue;
                 }
                 
                 final ROI2D roi2d = (ROI2D) roi;
                 
                 tasks.add(contourEvolutionService.submit(new Runnable()
                 {
                     public void run()
                     {
                         if (roi2d instanceof ROI2DArea)
                         {
                             // special case: check if the area has multiple components => split them
                             ROI2DArea area = (ROI2DArea) roi2d;
                             
                             BooleanMask2D[] components = area.getBooleanMask(true).getComponents();
                             
                             for (BooleanMask2D comp : components)
                             {
                                 ROI2DArea roi = new ROI2DArea(comp);
                                 final ActiveContour contour = new ActiveContour(ActiveContours.this, contour_resolution, contour_minArea, new SlidingWindow(convergence_winSize.getValue()), roi);
                                 contour.setX(roi.getBounds2D().getCenterX());
                                 contour.setY(roi.getBounds2D().getCenterY());
                                 contour.setT(t);
                                 TrackSegment segment = new TrackSegment();
                                 segment.addDetection(contour);
                                 trackGroup.addTrackSegment(segment);
                             }
                         }
                         else
                         {
                             final ActiveContour contour = new ActiveContour(ActiveContours.this, contour_resolution, contour_minArea, new SlidingWindow(convergence_winSize.getValue()), roi2d);
                             contour.setX(roi2d.getBounds2D().getCenterX());
                             contour.setY(roi2d.getBounds2D().getCenterY());
                             contour.setT(t);
                             TrackSegment segment = new TrackSegment();
                             segment.addDetection(contour);
                             trackGroup.addTrackSegment(segment);
                         }
                     }
                 }));
             }
             
             try
             {
                 for (Future<?> task : tasks)
                     task.get();
             }
             catch (InterruptedException e)
             {
                 for (Future<?> task : tasks)
                     task.cancel(true);
                 
                 Thread.currentThread().interrupt();
             }
             catch (Exception e)
             {
                 e.printStackTrace();
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
         
         // get the bounded field of evolution
         ROI2D field;
         
         Sequence boundSource = evolution_bounds.getValue();
         
         if (boundSource == null || !boundSource.getDimension().equals(inputData.getDimension()) || boundSource.getROI2Ds().size() == 0)
         {
             field = new ROI2DRectangle(0, 0, inputData.getWidth(), inputData.getHeight());
         }
         else
         {
             field = ROI2D.merge(boundSource.getROI2Ds().toArray(new ROI2D[0]), ShapeOperation.OR);
         }
         
         int nbConvergedContours = 0;
         
         long iter = 0;
         
         final HashSet<ActiveContour> evolvingContours = new HashSet<ActiveContour>(allContours.size());
         
         while (!globalStop && nbConvergedContours < allContours.size())
         {
             nbConvergedContours = 0;
             
             // update region information every 10 iterations
             if (region_weight.getValue() > EPSILON && iter % 10 == 0) updateRegionInformation(allContours, t);
             
             // take a snapshot of the current list of evolving (i.e. non-converged) contours
             evolvingContours.clear();
             for (ActiveContour contour : allContours)
             {
                 Double criterion = contour.convergence.computeCriterion(convergence_operation.getValue());
                 
                 if (criterion != null && criterion < convergence_criterion.getValue() / 10)
                 {
                     nbConvergedContours++;
                     if (getUI() != null) getUI().setProgressBarValue((double) nbConvergedContours / allContours.size());
                     continue;
                 }
                 
                 // if the contour hasn't converged yet, store it for the main loop
                 evolvingContours.add(contour);
             }
             
             // re-sample the contours to ensure homogeneous resolution
             resampleContours(evolvingContours, allContours, t);
             
             // compute deformations issued from the energy minimization
             deformContours(evolvingContours, allContours, field);
             
             // compute energy
             // computeEnergy(mainService, allContours);
             
             iter++;
             
             if (Thread.currentThread().isInterrupted()) globalStop = true;
         }
     }
     
     public double computeEnergy(ExecutorService service, HashSet<ActiveContour> contours)
     {
         double e = 0;
         
         if (regul_weight.getValue() != 0)
         {
             for (ActiveContour contour : contours)
             {
                 e += regul_weight.getValue() * contour.getDimension(1);
             }
         }
         
         if (region_weight.getValue() > EPSILON)
         {
             final Object _region_data = region_data.getDataXY(0);
             final double max = region_data.getChannelTypeMax(0);
             final DataType type = region_data.getDataType_();
             
             int w = region_data.getWidth();
             int h = region_data.getHeight();
             int off = 0;
             
             for (int j = 0; j < h; j++)
             {
                 for (int i = 0; i < w; i++, off++)
                 {
                     double val = Array1DUtil.getValue(_region_data, off, type) / max;
                     
                     if (_region_globl_mask[off] == 0)
                     {
                         val -= region_cout;
                         e += region_weight.getValue() * val * val;
                     }
                     else
                     {
                         for (ActiveContour contour : contours)
                         {
                             if (contour.path.contains(i, j))
                             {
                                 val -= region_cin.get(contour);
                                 e += region_weight.getValue() * val * val;
                                 break;
                             }
                         }
                     }
                 }
             }
         }
         
         return e;
     }
     
     /**
      * Deform contours together (coupling involved)
      * 
      * @param service
      * @param evolvingContours
      * @param allContours
      */
     private void deformContours(final HashSet<ActiveContour> evolvingContours, final HashSet<ActiveContour> allContours, final ROI2D field)
     {
         if (evolvingContours.size() == 1)
         {
             // no multi-threading needed
             
             ActiveContour contour = evolvingContours.iterator().next();
             
             if (Math.abs(edge_weight.getValue()) > EPSILON) contour.computeEdgeForces(edgeDataX, edgeDataY, edge_weight.getValue());
             
             if (regul_weight.getValue() > EPSILON) contour.computeInternalForces(regul_weight.getValue());
             
             if (region_weight.getValue() > EPSILON) contour.computeRegionForces(region_data, region_weight.getValue(), region_cin.get(contour), region_sin.get(contour), region_cout, region_sout);
             
             if (axis_weight.getValue() > EPSILON) contour.computeAxisForces(axis_weight.getValue());
             
             if (Math.abs(balloon_weight.getValue()) > EPSILON) contour.computeBalloonForces(balloon_weight.getValue());
             
             if (coupling_flag.getValue())
             {
                 // warning: feedback must be computed against ALL contours
                 // (including those which have already converged)
                 for (ActiveContour otherContour : allContours)
                 {
                     if (otherContour == null || otherContour == contour) continue;
                     
                     contour.computeFeedbackForces(otherContour);
                 }
             }
         }
         else
         {
             ArrayList<Future<?>> tasks = new ArrayList<Future<?>>(evolvingContours.size());
             
             for (final ActiveContour contour : evolvingContours)
             {
                 tasks.add(contourEvolutionService.submit(new Runnable()
                 {
                     public void run()
                     {
                         if (regul_weight.getValue() > EPSILON) contour.computeInternalForces(regul_weight.getValue());
                         
                         if (Math.abs(edge_weight.getValue()) > EPSILON) contour.computeEdgeForces(edgeDataX, edgeDataY, edge_weight.getValue());
                         
                         if (region_weight.getValue() > EPSILON)
                             contour.computeRegionForces(region_data, region_weight.getValue(), region_cin.get(contour), region_sin.get(contour), region_cout, region_sout);
                         
                         if (axis_weight.getValue() > EPSILON) contour.computeAxisForces(axis_weight.getValue());
                         
                         if (Math.abs(balloon_weight.getValue()) > EPSILON) contour.computeBalloonForces(balloon_weight.getValue());
                         
                         if (coupling_flag.getValue())
                         {
                             // warning: feedback must be computed against ALL contours
                             // (including those which have already converged)
                             for (ActiveContour otherContour : allContours)
                             {
                                 if (otherContour == null || otherContour == contour) continue;
                                 
                                 contour.computeFeedbackForces(otherContour);
                             }
                         }
                         else
                         {
                             // move contours asynchronously
                             contour.move(field, true, contour_timeStep.getValue());
                         }
                     }
                 }));
             }
             
             try
             {
                 for (Future<?> future : tasks)
                     future.get();
             }
             catch (InterruptedException e)
             {
                 for (Future<?> future : tasks)
                     future.cancel(true);
                 
                 Thread.currentThread().interrupt();
             }
             catch (Exception e1)
             {
                 e1.printStackTrace();
             }
         }
         
         if (coupling_flag.getValue())
         {
             // motion is synchronous, and can be done now
             for (ActiveContour contour : evolvingContours)
                 contour.move(field, true, contour_timeStep.getValue());
         }
         
         // refresh the display
         painter.painterChanged();
     }
     
     private void resampleContours(final HashSet<ActiveContour> evolvingContours, final HashSet<ActiveContour> allContours, final int t)
     {
         final VarBoolean loop = new VarBoolean("loop", true);
         
         while (loop.getValue())
         {
             loop.setValue(false);
             
             if (evolvingContours.size() == 1)
             {
                 // no multi-threading needed
                 
                 ActiveContour contour = evolvingContours.iterator().next();
                 boolean change = new ReSampler(trackGroup, contour, evolvingContours, allContours).call();
                 if (change) loop.setValue(true);
             }
             else
             {
                 ArrayList<ReSampler> tasks = new ArrayList<ReSampler>(evolvingContours.size());
                 
                 for (final ActiveContour contour : evolvingContours)
                     tasks.add(new ReSampler(trackGroup, contour, evolvingContours, allContours));
                 
                 List<Future<Boolean>> results = null;
                 
                 try
                 {
                     results = contourEvolutionService.invokeAll(tasks);
                     for (Future<Boolean> f : results)
                     {
                         if (f.get()) loop.setValue(true);
                     }
                 }
                 catch (InterruptedException e)
                 {
                     if (results != null) for (Future<?> f : results)
                         f.cancel(true);
                     
                     loop.setValue(false);
                     Thread.currentThread().interrupt();
                 }
                 catch (Exception e)
                 {
                     e.printStackTrace();
                 }
             }
         }
         
         if (region_weight.getValue() > EPSILON) updateRegionInformation(allContours, t);
     }
     
     private void updateRegionInformation(Collection<ActiveContour> contours, int t)
     {
         region_cin.clear();
         region_sin.clear();
         
         ArrayList<Future<?>> tasks = new ArrayList<Future<?>>(contours.size());
         
         final double[] _region_data = region_data.getDataXYAsDouble(0);
         _region_globl_mask = region_globl_mask.getDataXYAsByte(0);
         Arrays.fill(_region_globl_mask, (byte) 0);
         
         final byte[] _localMask = region_local_mask.getDataXYAsByte(0);
         
         for (final ActiveContour contour : contours)
         {
             tasks.add(meanUpdateService.submit(new Runnable()
             {
                 public void run()
                 {
                     double inSum = 0, inSumSq = 0, inCpt = 0;
                     
                     // create a mask for each object for interior mean measuring
                     Arrays.fill(_localMask, (byte) 0);
                     region_local_mask_graphics.fill(contour.path);
                     
                     for (int i = 0; i < _localMask.length; i++)
                     {
                         if (_localMask[i] != 0)
                         {
                             double value = _region_data[i];
                             inSum += value;
                             inSumSq += value * value;
                             inCpt++;
                         }
                     }
                     
                     region_cin.put(contour, inSum / inCpt);
                     region_sin.put(contour, Math.sqrt((inSumSq - (inSum * inSum) / inCpt) / inCpt));
                     
                     // add the contour to the global mask for background mean measuring
                     region_globl_mask_graphics.fill(contour.path);
                 }
             }));
             
             try
             {
                 for (Future<?> future : tasks)
                     future.get();
             }
             catch (InterruptedException e)
             {
             }
             catch (ExecutionException e)
             {
             }
         }
         
         double outSum = 0, outSumSq = 0, outCpt = 0;
         for (int i = 0; i < _region_globl_mask.length; i++)
         {
             if (_region_globl_mask[i] == 0)
             {
                 double value = _region_data[i];
                 outSum += value;
                 outSumSq += value * value;
                 outCpt++;
             }
         }
         region_cout = outSum / outCpt;
         region_sout = Math.sqrt((outSumSq - (outSum * outSum) / outCpt) / outCpt);
     }
     
     private void storeResult(int t)
     {
         ArrayList<TrackSegment> segments = trackGroup.getTrackSegmentList();
         
         ArrayList<ROI> rois = null;
         if (output_rois.getValue()) rois = new ArrayList<ROI>(Arrays.asList(roiOutput.getValue()));
         
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
                 area.setName("[T=" + StringUtil.toString(t, 1 + (int) Math.round(Math.log10(inputData.getSizeT()))) + "] Object #" + i);
                 rois.add(area);
             }
         }
         
         if (output_rois.getValue() && rois.size() > 0) roiOutput.setValue(rois.toArray(new ROI2D[rois.size()]));
     }
     
     @Override
     public void clean()
     {
         if (inputData != null) inputData.removePainter(painter);
         
         // contoursMap.clear();
         // contours.clear();
         // trackGroup.clearTracks();
         if (region_weight.getValue() > EPSILON) region_cin.clear();
         
         meanUpdateService.shutdownNow();
         contourEvolutionService.shutdownNow();
     }
     
     @Override
     public void stopExecution()
     {
         globalStop = true;
     }
     
     @Override
     public void declareInput(VarList inputMap)
     {
         inputMap.add("input sequence", input.getVariable());
         inputMap.add("Input ROI", roiInput);
         inputMap.add("regularization: weight", regul_weight.getVariable());
         inputMap.add("edge: weight", edge_weight.getVariable());
         edge_c.setActive(false);
         edge_c.setValues(0, 0, 16, 1);
         inputMap.add("edge: channel", edge_c.getVariable());
         inputMap.add("region: weight", region_weight.getVariable());
         region_c.setActive(false);
         region_c.setValues(0, 0, 16, 1);
         inputMap.add("region: channel", region_c.getVariable());
         
         inputMap.add("balloon: weight", balloon_weight.getVariable());
         
         coupling_flag.setValue(true);
         inputMap.add("contour resolution", contour_resolution.getVariable());
         contour_resolution.addVarChangeListener(new EzVarListener<Double>()
         {
             @Override
             public void variableChanged(EzVar<Double> source, Double newValue)
             {
                 convergence_winSize.setValue((int) (100.0 / newValue));
             }
         });
         
         // inputMap.add("minimum object size", contour_minArea.getVariable());
         inputMap.add("region bounds", evolution_bounds.getVariable());
         inputMap.add("time step", contour_timeStep.getVariable());
         // inputMap.add("convergence window size", convergence_winSize.getVariable());
         inputMap.add("convergence value", convergence_criterion.getVariable());
         output_rois.setValue(true);
         inputMap.add("tracking", tracking.getVariable());
     }
     
     @Override
     public void declareOutput(VarList outputMap)
     {
         outputMap.add(roiOutput);
     }
     
 }
