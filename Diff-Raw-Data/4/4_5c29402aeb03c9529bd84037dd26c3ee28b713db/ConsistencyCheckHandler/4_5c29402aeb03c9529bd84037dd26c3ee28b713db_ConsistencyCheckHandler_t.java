 /**
  * 
  */
 package org.feature.multi.perspective.measure.performance;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.MatchResult;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.IJobChangeEvent;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.core.runtime.jobs.JobChangeAdapter;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.feature.model.utilities.FeatureMappingUtil;
 import org.feature.model.utilities.FeatureModelUtil;
 import org.feature.model.utilities.WorkbenchUtil;
 import org.feature.multi.perspective.mapping.viewmapping.MappingModel;
import org.feature.multi.perspective.mapping.viewmapping.ViewmappingPackage;
 import org.feature.multi.perspective.model.viewmodel.GroupModel;
 import org.feature.multi.perspective.model.viewmodel.ViewPointContainer;
 import org.feature.multi.perspective.model.editor.editors.View;
 import org.feature.multi.perspective.model.editor.editors.ViewCreator;
 import org.feature.multi.perspective.model.editor.editors.algorithms.BruteForceAlgorithm;
 import org.feature.multi.perspective.model.editor.editors.algorithms.IncrementalAlgorithm;
 import org.feature.multi.perspective.model.editor.editors.algorithms.ViewPointWrapper;
 import org.featuremapper.models.feature.FeatureModel;
 import org.featuremapper.models.featuremapping.FeatureMappingModel;
 import org.featuremapper.models.featuremapping.FeatureMappingPackage;
 import org.featuremapper.models.featuremapping.FeatureModelRef;
 
 /**
  * Handler that triggers both consistency check algorithm to compare their preformance.
  * 
  * @author <a href=mailto:info@juliaschroeter.de>Julia Schroeter</a>
  * 
  */
 public class ConsistencyCheckHandler extends AbstractHandler {
 
    private Job job;
 
    private static String generatedProject = "generatedProject";
    // private static String[] genProjects = new String[] {
    // "vp100nc_cnf5000", "vp100nc_cnf5000",
    // "vp100nc_cnf10000", "vp100nc_cnf10000", "vp100nc_cnf10000",
    // "vp100nc_cnf2000","vp100nc_cnf2000","vp100nc_cnf2000" };
 
   protected static String mappingFolder = ViewmappingPackage.eNS_PREFIX;
 
    private static Logger log = Logger.getLogger(ConsistencyCheckHandler.class);
 
    List<Long> bruteforceTimeList = new LinkedList<Long>();
    List<Long> heuristicTimeList = new LinkedList<Long>();
    List<Integer> numberFeatures = new LinkedList<Integer>();
    List<Integer> numberViews = new LinkedList<Integer>();
    List<Integer> numberViewPoints = new LinkedList<Integer>();
    List<Integer> numberConstraints = new LinkedList<Integer>();
    List<String> groupTreeHeight = new LinkedList<String>();
    List<String> groupMaxChildren = new LinkedList<String>();
    List<String> groupsPerVP = new LinkedList<String>();
    List<String> featuresPerGroup = new LinkedList<String>();
    List<Double> bruteForceConsistentVPRatio = new LinkedList<Double>();
    List<Double> heuristicConsistentVPRatio = new LinkedList<Double>();
 
    public List<Double> getBruteForceConsistentVPRatio() {
       return bruteForceConsistentVPRatio;
    }
 
    public void resetLists() {
       bruteforceTimeList = new LinkedList<Long>();
       heuristicTimeList = new LinkedList<Long>();
       numberFeatures = new LinkedList<Integer>();
       numberViews = new LinkedList<Integer>();
       numberViewPoints = new LinkedList<Integer>();
       numberConstraints = new LinkedList<Integer>();
       groupTreeHeight = new LinkedList<String>();
       groupMaxChildren = new LinkedList<String>();
       groupsPerVP = new LinkedList<String>();
       featuresPerGroup = new LinkedList<String>();
       bruteForceConsistentVPRatio = new LinkedList<Double>();
       heuristicConsistentVPRatio = new LinkedList<Double>();
    }
 
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
       createJob();
       return null;
    }
 
    private void measurePerformance() {
       // for (int i = 0; i < genProjects.length; i++) {
       log.debug("-------------------------------");
       // String projectPart = genProjects[i];
       // String projectName = generatedProject + "_" + projectPart;
       String projectName = generatedProject;
       checkProjectConsistency(projectName);
       // }
    }
 
    private void createJob() {
 
       final Job job = new Job("Measure Performance of Consistency Check") {
 
          protected IStatus run(IProgressMonitor monitor) {
             try {
                measurePerformance();
                if (monitor.isCanceled()) return Status.CANCEL_STATUS;
                return Status.OK_STATUS;
             }
             finally {
                // schedule(60000); // start again in an hour
             }
          }
       };
 
       job.addJobChangeListener(new JobChangeAdapter() {
 
          public void done(IJobChangeEvent event) {
             if (event.getResult().isOK())
                postMessage("Performance Measurement completed successfully");
             else
                postError("Performance Measurement did not complete successfully");
          }
 
          private void postError(String msg) {
             Shell shell = WorkbenchUtil.getShell();
             if (shell != null) {
                MessageBox msgBox = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
                msgBox.setMessage(msg);
                msgBox.open();
             }
          }
 
          private void postMessage(String msg) {
             Shell shell = WorkbenchUtil.getShell();
             if (shell != null) {
                MessageBox msgBox = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
                msgBox.setMessage(msg);
                msgBox.open();
             }
          }
       });
       job.setSystem(true);
       job.schedule(); // start as soon as possible
    }
 
    private void checkProjectConsistency(String projectName) {
       log.debug("check consistency of " + projectName);
       IWorkspace workspace = ResourcesPlugin.getWorkspace();
       IProject project = workspace.getRoot().getProject(projectName);
       if (project.exists()) {
          resetLists();
          IFolder projectFolder = project.getFolder(mappingFolder);
          if (projectFolder.exists()) {
             IResource[] members;
             try {
                members = projectFolder.members();
                for (IResource iResource : members) {
                   if (iResource instanceof IFile) {
                      IFile file = (IFile) iResource;
                      ResourceSet resourceSet = new ResourceSetImpl();
                       MappingModel mapping = FeatureMappingUtil.getFeatureMapping(file, resourceSet);
                      if (mapping != null) {
                         determineInfo(file);
                         checkConsistency(mapping);
                      }
                   }
                }
                if (log.isDebugEnabled()) {
                   printPerformanceMeasure();
                }
             } catch (CoreException e) {
                log.error("Could not determine children of folder " + projectFolder);
             }
          }
 
       }
    }
 
    private void determineInfo(IFile file) {
       determineInfo(groupTreeHeight, "_height", file);
       determineInfo(groupMaxChildren, "_countChildren", file);
       determineInfo(groupsPerVP, "_groupsVP", file);
       determineInfo(featuresPerGroup, "_FCount", file);
    }
 
    private void determineInfo(List<String> paramList, String param, IFile file) {
       String fileName = file.getName();
       // example:
       // documentmanagement.feature_balanced_height2_countChildren2_vpCount10_groupsVP1_2012-03-31-082230484.cluster_FCount_2012-03-31-082231424.featuremapping
       String regex = param + "([0-9])*";
       Pattern pattern = Pattern.compile(regex);
       Matcher matcher = pattern.matcher(fileName);
       while (matcher.find()) {
          MatchResult result = matcher.toMatchResult();
          String match = result.group();
          String number = match.substring(param.length());
          paramList.add(number);
       }
    }
 
    private void printPerformanceMeasure() {
       // TODO: avg. time algorithm
       printCollection("NumberFeatures    ", numberFeatures);
       printCollection("NumberConstraints ", numberConstraints);
       printCollection("NumberViewpoints  ", numberViewPoints);
       printCollection("NumberViews       ", numberViews);
       printCollection("Time Bruteforce   ", bruteforceTimeList);
       printCollection("Time Heuristic    ", heuristicTimeList);
       printStringCollection("Group TreeHeight  ", groupTreeHeight);
       printStringCollection("Group MaxChildren ", groupMaxChildren);
       printStringCollection("Groups per VP     ", groupsPerVP);
       printStringCollection("Features per Group", featuresPerGroup);
       printDoubleCollection("ConsistentRatio Bruteforce", bruteForceConsistentVPRatio);
       printDoubleCollection("ConsistentRatio Heuristic ", heuristicConsistentVPRatio);
    }
 
    private StringBuffer initStringBuffer(String description) {
       StringBuffer s = new StringBuffer();
       s.append(description);
       s.append("{");
       return s;
    }
 
    private void finalizeStringBuffer(StringBuffer s) {
       s.append("}");
       log.debug(s);
    }
 
    private void printDoubleCollection(String description, List<Double> bruteForceConsistentVPRatio2) {
       StringBuffer s = initStringBuffer(description);
       for (Double ratio : bruteForceConsistentVPRatio2) {
          // DecimalFormat df = new DecimalFormat("0.00");
          // String ratioStr = df.format(ratio);
          s.append(ratio);
          s.append(", ");
       }
       finalizeStringBuffer(s);
    }
 
    private void printStringCollection(String description, List<String> list) {
       StringBuffer s = initStringBuffer(description);
       for (String l : list) {
          s.append(l);
          s.append(", ");
       }
       finalizeStringBuffer(s);
    }
 
    private void printCollection(String description, Collection<Integer> list) {
       StringBuffer s = initStringBuffer(description);
       for (Integer l : list) {
          s.append(l);
          s.append(", ");
       }
       finalizeStringBuffer(s);
    }
 
    private void printCollection(String description, List<Long> list) {
       StringBuffer s = initStringBuffer(description);
       for (Long l : list) {
          s.append(l);
          s.append(", ");
       }
       finalizeStringBuffer(s);
    }
 
    private void printVPCollection(String description, List<ViewPointWrapper> list) {
       StringBuffer s = initStringBuffer(description);
       for (ViewPointWrapper l : list) {
          s.append(l);
          s.append(", ");
       }
       finalizeStringBuffer(s);
    }
 
    private double getRatio(List<ViewPointWrapper> list) {
       List<ViewPointWrapper> consistent = new ArrayList<ViewPointWrapper>(list.size());
       for (ViewPointWrapper viewPointWrapper : list) {
          if (viewPointWrapper.isConsistent()) {
             consistent.add(viewPointWrapper);
          }
       }
       double ratio = consistent.size() * 1.00 / list.size();
       return ratio;
    }
 
    private void checkConsistency(MappingModel featureMapping) {
       GroupModel groupModel = featureMapping.getViewModel();
       FeatureModel featureModel = featureMapping.getFeatureModel();
          ViewCreator viewCreator = new ViewCreator(featureMapping);
          List<View> views = viewCreator.getViews();
          log.debug("GroupModel " + groupModel.eResource().getURI());
 
          ViewPointContainer container = groupModel.getViewPointContainer();
          int viewpoints = 0;
          if (container != null) {
             viewpoints = container.getViewPoints().size();
          }
          numberViewPoints.add(viewpoints);
          numberFeatures.add(FeatureModelUtil.getAllFeatures(featureModel).size());
          numberConstraints.add(FeatureModelUtil.getConstraints(featureModel, FeatureModelUtil.csp_constraintLanguage).size());
          numberViews.add(views.size());
 
          long startB = System.currentTimeMillis();
          List<ViewPointWrapper> bfViewPoints = runBruteForce(views, groupModel, featureModel);
          long endB = System.currentTimeMillis();
          long timeB = endB - startB;
          bruteforceTimeList.add(timeB);
 
          printVPCollection("BruteForce VPs", bfViewPoints);
          double bRatio = getRatio(bfViewPoints);
          bruteForceConsistentVPRatio.add(bRatio);
 
          long startH = System.currentTimeMillis();
          List<ViewPointWrapper> hViewPoints = runHeuristic(featureMapping);
          long endH = System.currentTimeMillis();
          long timeH = endH - startH;
          heuristicTimeList.add(timeH);
          printVPCollection("Heuristic VPs ", hViewPoints);
          double hRatio = getRatio(hViewPoints);
          heuristicConsistentVPRatio.add(hRatio);
 
          log.debug("Bruteforce [" + bRatio + "] " + timeB + "ms , Heuristic [" + hRatio + "]" + timeH + "ms");
          log.debug("-------------------------------");
    }
 
    private List<ViewPointWrapper> runHeuristic(MappingModel featureMapping) {
       IncrementalAlgorithm algorithm = new IncrementalAlgorithm(featureMapping);
       List<ViewPointWrapper> viewpoints = algorithm.checkViewpoints();
       return viewpoints;
    }
 
    private List<ViewPointWrapper> runBruteForce(List<View> views, GroupModel groupModel, FeatureModel featureModel) {
       BruteForceAlgorithm algorithm = new BruteForceAlgorithm(groupModel, views, featureModel);
       List<ViewPointWrapper> viewPoints = algorithm.checkViewPoints();
       return viewPoints;
    }
 
    @Override
    public void dispose() {
       if (job != null) {
          job.cancel();
       }
       super.dispose();
    }
 
 }
