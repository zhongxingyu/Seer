 package edacc.experiment;
 
 import edacc.EDACCApp;
 import edacc.EDACCExperimentMode;
 import edacc.EDACCSolverConfigEntry;
 import edacc.EDACCSolverConfigPanel;
 import edacc.model.ComputationMethodDoesNotExistException;
 import edacc.model.DatabaseConnector;
 import edacc.model.ExpResultHasSolvPropertyNotInDBException;
 import edacc.model.Experiment;
 import edacc.model.ExperimentDAO;
 import edacc.model.ExperimentHasGridQueue;
 import edacc.model.ExperimentHasGridQueueDAO;
 import edacc.model.ExperimentHasInstance;
 import edacc.model.ExperimentHasInstanceDAO;
 import edacc.model.ExperimentResult;
 import edacc.model.ExperimentResultDAO;
 import edacc.model.ExperimentResultHasProperty;
 import edacc.model.ExperimentResultNotInDBException;
 import edacc.model.ExperimentResultStatus;
 import edacc.model.GridQueue;
 import edacc.model.GridQueueDAO;
 import edacc.model.Instance;
 import edacc.model.InstanceClass;
 import edacc.model.InstanceClassDAO;
 import edacc.model.InstanceClassMustBeSourceException;
 import edacc.model.InstanceDAO;
 import edacc.model.InstanceHasProperty;
 import edacc.model.NoConnectionToDBException;
 import edacc.model.Property;
 import edacc.model.PropertyDAO;
 import edacc.model.Solver;
 import edacc.model.SolverConfiguration;
 import edacc.model.SolverConfigurationDAO;
 import edacc.model.SolverDAO;
 import edacc.model.PropertyNotInDBException;
 import edacc.model.TaskCancelledException;
 import edacc.model.Tasks;
 import edacc.properties.PropertyTypeNotExistException;
 import edacc.satinstances.ConvertException;
 import edacc.satinstances.PropertyValueType;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.sql.Date;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Random;
 import java.util.Vector;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 import javax.swing.SwingUtilities;
 
 /**
  * Experiment design more controller class, handles requests by the GUI
  * for creating, removing, loading, etc. experiments.
  * Provides caching for experiment results. Use the higher level methods to get
  * the experiment results. Those methods are synchronized and therefore can be
  * used in threads.
  * updateExperimentResults() will update the local cache for the current experiment
  * and should be called first.
  * @author daniel, simon
  */
 public class ExperimentController {
 
     EDACCExperimentMode main;
     EDACCSolverConfigPanel solverConfigPanel;
     private Experiment activeExperiment;
     private ArrayList<Experiment> experiments;
     private static RandomNumberGenerator rnd = new JavaRandom();
     // caching experiments
     // use of resultMap must be synchronized
     private HashMap<ResultIdentifier, ExperimentResult> resultMap;
     private Timestamp lastUpdated;
     private Experiment experiment;
     // end of caching experiment results
     public static Property PROP_CPUTIME;
 
     /**
      * Creates a new experiment Controller
      * @param experimentMode
      * @param solverConfigPanel
      */
     public ExperimentController(EDACCExperimentMode experimentMode, EDACCSolverConfigPanel solverConfigPanel) {
         this.main = experimentMode;
         this.solverConfigPanel = solverConfigPanel;
         PROP_CPUTIME = new Property();
         PROP_CPUTIME.setName("CPU-Time (s)");
 
     }
 
     /**
      * Initializes the experiment controller. Loads the experiments and the instances classes.
      * @throws SQLException
      */
     public void initialize() throws SQLException, InstanceClassMustBeSourceException, IOException, NoConnectionToDBException, PropertyNotInDBException, PropertyTypeNotExistException, ComputationMethodDoesNotExistException {
         ArrayList<Experiment> v = new ArrayList<Experiment>();
         v.addAll(ExperimentDAO.getAll());
         experiments = v;
         main.expTableModel.setExperiments(experiments);
         Vector<InstanceClass> vic = new Vector<InstanceClass>();
         try {
             vic.addAll(InstanceClassDAO.getAll());
 
             main.instanceClassModel.setClasses(vic);
             ArrayList<Instance> instances = new ArrayList<Instance>();
             instances.addAll(InstanceDAO.getAll());
             main.insTableModel.setInstances(instances);
         } catch (Exception e) {
             e.printStackTrace();
         }
 
     }
 
     /**
      * Loads an experiment, the solvers and the solver configurations.
      * @param exp
      * @param task
      * @throws SQLException
      */
     public void loadExperiment(Experiment exp, Tasks task) throws SQLException {
         activeExperiment = exp;
         main.solverConfigPanel.beginUpdate();
         solverConfigPanel.removeAll();
         SolverConfigurationDAO.clearCache();
         ArrayList<Solver> vs = new ArrayList<Solver>();
         ArrayList<SolverConfiguration> vss = new ArrayList<SolverConfiguration>();
         Vector<ExperimentHasInstance> ehi = new Vector<ExperimentHasInstance>();
         task.setStatus("Loading solvers..");
         vs.addAll(SolverDAO.getAll());
         task.setTaskProgress(.33f);
         task.setStatus("Loading solver configurations..");
         vss.addAll(SolverConfigurationDAO.getSolverConfigurationByExperimentId(exp.getId()));
         task.setTaskProgress(.66f);
         task.setStatus("Loading instances..");
         // select instances for the experiment
         ehi.addAll(ExperimentHasInstanceDAO.getExperimentHasInstanceByExperimentId(activeExperiment.getId()));
         main.solTableModel.setSolvers(vs);
         for (int i = 0; i < vss.size(); i++) {
             main.solverConfigPanel.addSolverConfiguration(vss.get(i));
         }
         main.insTableModel.setExperimentHasInstances(ehi);
         main.solverConfigPanel.endUpdate();
         task.setStatus("Finalizing");
         main.afterExperimentLoaded();
     }
 
     /**
      * Removes an experiment form the db.
      * @param id
      * @throws SQLException
      */
     public void removeExperiment(int id) throws SQLException, InstanceClassMustBeSourceException, IOException, NoConnectionToDBException, PropertyNotInDBException, PropertyTypeNotExistException, ComputationMethodDoesNotExistException {
         Experiment e = ExperimentDAO.getById(id);
         if (e.equals(activeExperiment)) {
             unloadExperiment();
         }
         ExperimentDAO.removeExperiment(e);
         initialize();
     }
 
     /**
      * returns a reference to the currently loaded experiment or null, if none
      * @return active experiment reference
      */
     public Experiment getActiveExperiment() {
         return activeExperiment;
     }
 
     /**
      * unloads the currently loaded experiment, i.e. sets activeExperiment to null
      * and calls UI functions to disable the experiment design tabs
      */
     public void unloadExperiment() {
         activeExperiment = null;
         main.afterExperimentUnloaded();
     }
 
     /**
      * invoked by the UI to create a new experiment, also calls initialize to load
      * instances and solvers
      * @param name
      * @param description
      * @throws SQLException
      * @throws InstanceClassMustBeSourceException
      * @throws IOException
      * @throws NoConnectionToDBException
      * @throws PropertyNotInDBException
      * @throws PropertyTypeNotExistException
      * @throws ComputationMethodDoesNotExistException
      */
     public void createExperiment(String name, String description) throws SQLException, InstanceClassMustBeSourceException, IOException, NoConnectionToDBException, PropertyNotInDBException, PropertyTypeNotExistException, ComputationMethodDoesNotExistException {
         java.util.Date d = new java.util.Date();
         ExperimentDAO.createExperiment(name, new Date(d.getTime()), description);
         initialize();
     }
 
     /**
      * This will save the settings for the given experiment.
      * @param exp the experiment
      * @throws SQLException
      */
     public void saveExperiment(Experiment exp) throws SQLException {
         ExperimentDAO.save(exp);
     }
 
     /**
      * Saves all solver configurations with parameter instances in the solver
      * config panel.
      * @throws SQLException
      */
     public void saveSolverConfigurations(Tasks task) throws SQLException, InterruptedException, InvocationTargetException, PropertyNotInDBException, PropertyTypeNotExistException, IOException, NoConnectionToDBException, ComputationMethodDoesNotExistException, ExpResultHasSolvPropertyNotInDBException, ExperimentResultNotInDBException {
 
         task.setStatus("Checking jobs..");
         ArrayList<SolverConfiguration> deletedSolverConfigurations = SolverConfigurationDAO.getAllDeleted();
         final ArrayList<ExperimentResult> deletedJobs = new ArrayList<ExperimentResult>();
         for (SolverConfiguration sc : deletedSolverConfigurations) {
             deletedJobs.addAll(ExperimentResultDAO.getAllBySolverConfiguration(sc));
         }
 
         if (deletedJobs.size() > 0) {
             int notDeletableJobsCount = 0;
             for (ExperimentResult job : deletedJobs) {
                 if (job.getStatus() != ExperimentResultStatus.NOTSTARTED) {
                     notDeletableJobsCount++;
                 }
             }
             String msg = "";
             if (notDeletableJobsCount > 0) {
                 msg = "There are " + notDeletableJobsCount + " started jobs and " + (deletedJobs.size() - notDeletableJobsCount) + " jobs waiting in the database which would be deleted. Do you want to continue?";
             } else {
                 msg = "There are " + deletedJobs.size() + " jobs waiting in the database which would be deleted. Do you want to continue?";
             }
             int userInput = javax.swing.JOptionPane.showConfirmDialog(Tasks.getTaskView(), msg, "Jobs would be deleted", javax.swing.JOptionPane.YES_NO_OPTION);
             if (userInput == 1) {
                 return;
             } else {
                 task.setStatus("Deleting jobs..");
                 ExperimentResultDAO.deleteExperimentResults(deletedJobs);
             }
         }
         task.setStatus("Saving solver configurations..");
         boolean invalidSeedGroup = false;
         for (int i = 0; i < solverConfigPanel.getComponentCount(); i++) {
             EDACCSolverConfigEntry entry = (EDACCSolverConfigEntry) solverConfigPanel.getComponent(i);
             int seed_group = 0;
             try {
                 seed_group = Integer.valueOf(entry.getSeedGroup().getText());
             } catch (NumberFormatException e) {
                 seed_group = 0;
                 entry.getSeedGroup().setText("0");
                 invalidSeedGroup = true;
             }
             if (entry.getSolverConfiguration() == null) {
                 entry.setSolverConfiguration(SolverConfigurationDAO.createSolverConfiguration(entry.getSolverId(), activeExperiment.getId(), seed_group));
                 solverConfigPanel.setSolverConfigurationName(entry);
             } else {
                 entry.getSolverConfiguration().setSeed_group(seed_group);
                 entry.getSolverConfiguration().setModified();
             }
             entry.saveParameterInstances();
         }
         SolverConfigurationDAO.saveAll();
         if (invalidSeedGroup) {
             SwingUtilities.invokeAndWait(new Runnable() {
 
                 @Override
                 public void run() {
                     javax.swing.JOptionPane.showMessageDialog(Tasks.getTaskView(), "Seed groups have to be integers, defaulted to 0", "Expected integer for seed groups", javax.swing.JOptionPane.ERROR_MESSAGE);
                 }
             });
         }
     }
 
     /**
      * This will clean up all changed data for the solver configurations.
      * @param task
      * @throws SQLException
      */
     public void undoSolverConfigurations(Tasks task) throws SQLException {
         main.solverConfigPanel.beginUpdate();
         main.solverConfigPanel.removeAll();
         ArrayList<SolverConfiguration> solverConfigurations = SolverConfigurationDAO.getAllCached();
         Collections.sort(solverConfigurations, new Comparator<SolverConfiguration>() {
 
             @Override
             public int compare(SolverConfiguration o1, SolverConfiguration o2) {
                 return o1.getId() - o2.getId();
             }
         });
         for (SolverConfiguration sc : solverConfigurations) {
             main.solverConfigPanel.addSolverConfiguration(sc);
             sc.setSaved();
         }
         main.solverConfigPanel.endUpdate();
         main.setTitles();
     }
 
     /**
      * saves the instances selection of the currently loaded experiment
      * @throws SQLException
      */
     public void saveExperimentHasInstances(Tasks task) throws SQLException, InterruptedException, InvocationTargetException, PropertyNotInDBException, PropertyTypeNotExistException, IOException, NoConnectionToDBException, ComputationMethodDoesNotExistException, ExpResultHasSolvPropertyNotInDBException, ExperimentResultNotInDBException {
         task.setStatus("Checking jobs..");
         ArrayList<ExperimentHasInstance> deletedInstances = main.insTableModel.getDeletedExperimentHasInstances();
         if (deletedInstances.size() > 0) {
             ArrayList<ExperimentResult> deletedJobs = new ArrayList<ExperimentResult>();
             for (ExperimentHasInstance ehi : deletedInstances) {
                 deletedJobs.addAll(ExperimentResultDAO.getAllByExperimentHasInstance(ehi));
             }
             int notDeletableJobsCount = 0;
             for (ExperimentResult job : deletedJobs) {
                 if (job.getStatus() != ExperimentResultStatus.NOTSTARTED) {
                     notDeletableJobsCount++;
                 }
             }
             String msg = "";
             if (notDeletableJobsCount > 0) {
                 msg = "There are " + notDeletableJobsCount + " started jobs and " + (deletedJobs.size() - notDeletableJobsCount) + " jobs waiting in the database which would be deleted. Do you want to continue?";
             } else {
                 msg = "There are " + deletedJobs.size() + " jobs waiting in the database which would be deleted. Do you want to continue?";
             }
             int userInput = javax.swing.JOptionPane.showConfirmDialog(Tasks.getTaskView(), msg, "Jobs would be deleted", javax.swing.JOptionPane.YES_NO_OPTION);
             if (userInput == 1) {
                 return;
             } else {
                 task.setStatus("Deleting jobs..");
                 ExperimentResultDAO.deleteExperimentResults(deletedJobs);
             }
         }
         task.setStatus("Saving instances..");
 
 
         // First: add all new ExperimentHasInstance objects
         for (Integer instanceId : main.insTableModel.getNewInstanceIds()) {
             ExperimentHasInstanceDAO.createExperimentHasInstance(activeExperiment.getId(), instanceId);
         }
 
         // Then: remove all removed ExperimentHasInstance objects
         for (ExperimentHasInstance ehi : main.insTableModel.getDeletedExperimentHasInstances()) {
             ExperimentHasInstanceDAO.removeExperimentHasInstance(ehi);
         }
         main.insTableModel.setExperimentHasInstances(ExperimentHasInstanceDAO.getExperimentHasInstanceByExperimentId(activeExperiment.getId()));
     }
 
     /**
      * method used for auto seed generation, uses the random number generator
      * referenced by this.rnd
      * @return integer between 0 and max inclusively
      */
     private int generateSeed(int max) {
         return rnd.nextInt(max + 1);
     }
 
     /**
      * generates the ExperimentResults (jobs) in the database for the currently active experiment
      * This is the cartesian product of the set of solver configs and the set of the selected instances
      * Doesn't overwrite existing jobs
      * @throws SQLException
      * @param numRuns
      * @return number of jobs added to the experiment results table
      * @throws SQLException
      */
     public synchronized int generateJobs(int numRuns, final Tasks task) throws SQLException, TaskCancelledException, IOException, PropertyTypeNotExistException, PropertyNotInDBException, NoConnectionToDBException, ComputationMethodDoesNotExistException, ExpResultHasSolvPropertyNotInDBException, ExperimentResultNotInDBException {
         PropertyChangeListener cancelExperimentResultDAOStatementListener = new PropertyChangeListener() {
 
             @Override
             public void propertyChange(PropertyChangeEvent evt) {
                 if ("state".equals(evt.getPropertyName()) && task.isCancelled()) {
                     try {
                         ExperimentResultDAO.cancelStatement();
                     } catch (SQLException ex) {
                     }
                 }
             }
         };
 
         task.setOperationName("Generating jobs for experiment " + activeExperiment.getName());
         task.setStatus("Loading data from database");
         updateExperimentResults();
         // get instances of this experiment
         LinkedList<Instance> listInstances = InstanceDAO.getAllByExperimentId(activeExperiment.getId());
 
         // get solver configurations of this experiment
         ArrayList<SolverConfiguration> vsc = SolverConfigurationDAO.getSolverConfigurationByExperimentId(activeExperiment.getId());
 
         int experiments_added = 0;
         HashMap<SeedGroup, Integer> linked_seeds = new HashMap<SeedGroup, Integer>();
         ArrayList<ExperimentResult> experiment_results = new ArrayList<ExperimentResult>();
 
         int elements = listInstances.size() * vsc.size() * numRuns;
         ExperimentDAO.updateNumRuns(activeExperiment);
         if (numRuns < activeExperiment.getNumRuns()) {
             // We have to delete jobs
             int runsToDelete = activeExperiment.getNumRuns() - numRuns;
             task.setStatus("Preparing..");
             ArrayList<Integer> runs = getAllRuns();
             ArrayList<Integer> deleteRuns = new ArrayList<Integer>();
             Random random = new Random();
             for (int i = 0; i < runsToDelete; i++) {
                 if (runs.isEmpty()) {
                     break;
                 }
                 int index = random.nextInt(runs.size());
                 deleteRuns.add(runs.get(index));
                 runs.remove(index);
             }
             ArrayList<ExperimentResult> deletedJobs = new ArrayList<ExperimentResult>();
             deletedJobs.addAll(getAllByRun(deleteRuns));
             String msg = "The number of runs specified is less than the number of runs in this experiment. There are " + deletedJobs.size() + " jobs which would be deleted. Do you want to continue?";
             int userInput = javax.swing.JOptionPane.showConfirmDialog(Tasks.getTaskView(), msg, "Jobs would be deleted", javax.swing.JOptionPane.YES_NO_OPTION);
             Tasks.getTaskView().setCancelable(true);
             task.setTaskProgress(0.f);
             if (userInput == 1) {
                 return 0;
             } else {
                 task.setStatus("Deleting jobs..");
                 task.addPropertyChangeListener(cancelExperimentResultDAOStatementListener);
                 try {
                     ExperimentResultDAO.setAutoCommit(false);
                     ExperimentResultDAO.deleteExperimentResults(deletedJobs);
                     task.setStatus("Updating existing jobs..");
                     ArrayList<ExperimentResult> updateJobs = new ArrayList<ExperimentResult>();
                     for (int i = 0; i < runs.size(); i++) {
                         if (task.isCancelled()) {
                             throw new TaskCancelledException();
                         }
 
                         ArrayList<ExperimentResult> tmp = this.getAllByRun(runs.get(i));
                         for (ExperimentResult er : tmp) {
                             er.setRun(i);
                         }
                         updateJobs.addAll(tmp);
                         task.setTaskProgress((float) (i + 1) / (runs.size()));
                     }
                     task.setTaskProgress(0.f);
 
 
                     ExperimentResultDAO.batchUpdateRun(updateJobs);
                 } catch (Exception ex) {
                     System.out.println("ROLLBACK!");
                     DatabaseConnector.getInstance().getConn().rollback();
                     if (ex.getMessage().contains("cancelled")) {
                         throw new TaskCancelledException();
                     }
                     if (ex instanceof SQLException) {
                         throw (SQLException) ex;
                     } else if (ex instanceof TaskCancelledException) {
                         throw (TaskCancelledException) ex;
                     }
                 } finally {
                     ExperimentResultDAO.setAutoCommit(true);
                 }
                 task.removePropertyChangeListener(cancelExperimentResultDAOStatementListener);
                 ExperimentDAO.updateNumRuns(activeExperiment);
             }
             Tasks.getTaskView().setCancelable(true);
         }
 
         if (activeExperiment.isAutoGeneratedSeeds() && activeExperiment.isLinkSeeds()) {
             // first pass over already existing jobs to accumulate existing linked seeds
             for (Instance i : listInstances) {
                 for (SolverConfiguration c : vsc) {
                     for (int run = 0; run < numRuns; ++run) {
                         task.setStatus("Preparing job generation");
                         if (resultMap.containsKey(new ResultIdentifier(c.getId(), i.getId(), run))) {
                             // use the already existing jobs to populate the seed group hash table so jobs of newly added solver configs use
                             // the same seeds as already existing jobs
                             int seed = getResult(c.getId(), i.getId(), run).getSeed();
                             SeedGroup sg = new SeedGroup(c.getSeed_group(), i.getId(), run);
                             if (!linked_seeds.containsKey(sg)) {
                                 linked_seeds.put(sg, new Integer(seed));
                             }
                         }
                     }
                 }
             }
         }
         if (task.isCancelled()) {
             throw new TaskCancelledException();
         }
 
 
         int done = 1;
         // cartesian product
         for (Instance i : listInstances) {
             for (SolverConfiguration c : vsc) {
                 for (int run = 0; run < numRuns; ++run) {
                     task.setTaskProgress((float) done / (float) elements);
                     if (task.isCancelled()) {
                         throw new TaskCancelledException();
                     }
                     task.setStatus("Adding job " + done + " of " + elements);
                     // check if job already exists
                     if (!resultMap.containsKey(new ResultIdentifier(c.getId(), i.getId(), run))) {
                         if (activeExperiment.isAutoGeneratedSeeds() && activeExperiment.isLinkSeeds()) {
                             Integer seed = linked_seeds.get(new SeedGroup(c.getSeed_group(), i.getId(), run));
                             if (seed != null) {
                                 experiment_results.add(ExperimentResultDAO.createExperimentResult(run, -1, seed.intValue(), 0, c.getId(), activeExperiment.getId(), i.getId()));
                             } else {
                                 Integer new_seed = new Integer(generateSeed(activeExperiment.getMaxSeed()));
                                 linked_seeds.put(new SeedGroup(c.getSeed_group(), i.getId(), run), new_seed);
                                 experiment_results.add(ExperimentResultDAO.createExperimentResult(run, -1, new_seed.intValue(), 0, c.getId(), activeExperiment.getId(), i.getId()));
                             }
                         } else if (activeExperiment.isAutoGeneratedSeeds() && !activeExperiment.isLinkSeeds()) {
                             experiment_results.add(ExperimentResultDAO.createExperimentResult(run, -1, generateSeed(activeExperiment.getMaxSeed()), 0, c.getId(), activeExperiment.getId(), i.getId()));
                         } else {
                             experiment_results.add(ExperimentResultDAO.createExperimentResult(run, -1, 0, 0, c.getId(), activeExperiment.getId(), i.getId()));
                         }
                         experiments_added++;
                     }
                     done++;
                 }
 
             }
         }
         task.setTaskProgress(0.f);
         task.addPropertyChangeListener(cancelExperimentResultDAOStatementListener);
         task.setStatus("Saving changes to database..");
         try {
             ExperimentResultDAO.batchSave(experiment_results);
         } catch (SQLException ex) {
             if (ex.getMessage().contains("cancelled")) {
                 throw new TaskCancelledException();
             }
             throw ex;
         }
         task.removePropertyChangeListener(cancelExperimentResultDAOStatementListener);
         ExperimentDAO.updateNumRuns(activeExperiment);
         return experiments_added;
     }
 
     /**
      * Updates the given parameters in the current experiment and saves the changes to the db.
      * @param CPUTimeLimit
      * @param wallClockTimeLimit
      * @param memoryLimit
      * @param stackSizeLimit
      * @param outputSizeLimit
      * @param maxSeed
      * @param generateSeeds
      * @param linkSeeds
      * @throws SQLException
      * @throws InstanceClassMustBeSourceException
      * @throws IOException
      * @throws NoConnectionToDBException
      * @throws PropertyNotInDBException
      * @throws PropertyTypeNotExistException
      * @throws ComputationMethodDoesNotExistException
      */
     public void saveExperimentParameters(Integer CPUTimeLimit, Integer wallClockTimeLimit, Integer memoryLimit, Integer stackSizeLimit, Integer outputSizeLimit, Integer maxSeed, boolean generateSeeds, boolean linkSeeds) throws SQLException, InstanceClassMustBeSourceException, IOException, NoConnectionToDBException, PropertyNotInDBException, PropertyTypeNotExistException, ComputationMethodDoesNotExistException {
         activeExperiment.setCPUTimeLimit(CPUTimeLimit);
         activeExperiment.setWallClockTimeLimit(wallClockTimeLimit);
         activeExperiment.setMemoryLimit(memoryLimit);
         activeExperiment.setStackSizeLimit(stackSizeLimit);
         activeExperiment.setOutputSizeLimit(outputSizeLimit);
         activeExperiment.setAutoGeneratedSeeds(generateSeeds);
         activeExperiment.setMaxSeed(maxSeed);
         activeExperiment.setLinkSeeds(linkSeeds);
         ExperimentDAO.save(activeExperiment);
         initialize();
     }
 
     /**
      * returns the number of jobs in the database for the given experiment
      * @return the number of jobs in the db
      */
     public int getNumJobs() {
         try {
             return ExperimentResultDAO.getCountByExperimentId(activeExperiment.getId());
         } catch (Exception e) {
             return 0;
         }
     }
 
     /**
      * Updates the job browser table
      */
     public synchronized void loadJobs() {
         try {
             updateExperimentResults();
             final ExperimentResultsBrowserTableModel sync = main.jobsTableModel;
             synchronized (sync) {
                 ArrayList<ExperimentResult> results = main.jobsTableModel.getJobs();
                 if (results != null) {
                     if (results.size() != resultMap.size()) {
                         results = null;
                     } else {
                         for (int i = 0; i < results.size(); i++) {
                             ExperimentResult er = results.get(i);
                             ExperimentResult tmp = resultMap.get(new ResultIdentifier(er.getSolverConfigId(), er.getInstanceId(), er.getRun()));
                             if (tmp == null) {
                                 results = null;
                                 break;
                             } else if (!er.getDatemodified().equals(tmp.getDatemodified())) {
                                 results.set(i, tmp);
                                 main.jobsTableModel.fireTableRowsUpdated(i, i);
                             }
                         }
                         if (results != null && results.size() != resultMap.size()) {
                             results = null;
                         }
                     }
                 }
                 if (results == null) {
                     results = new ArrayList<ExperimentResult>();
                     results.addAll(resultMap.values());
                     main.jobsTableModel.setJobs(results);
                     main.resultBrowserRowFilter.updateFilterTypes();
                     main.jobsTableModel.fireTableDataChanged();
                 } else {
                     SwingUtilities.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                            main.tableJobs.updateUI();
                         }
                     });
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
             // TODO: shouldn't happen but show message if it does
         }
 
     }
 
     /**
      * Deletes all illegal characters of filename and returns the result.
      * @param filename
      * @return
      */
     private String getFilename(String filename) {
         final char[] ILLEGAL_CHARACTERS = {'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':', ' '};
         String res = "";
         for (char c : filename.toCharArray()) {
             boolean illegal = false;
             for (char i : ILLEGAL_CHARACTERS) {
                 if (c == i) {
                     illegal = true;
                 }
             }
             if (illegal) {
                 res += "_";
             } else {
                 res += c;
             }
         }
         return res;
     }
 
     /**
      * Generates a ZIP archive with the necessary files for the grid.
      */
     public void generatePackage(String location, boolean exportInstances, boolean exportSolvers, Tasks task) throws FileNotFoundException, IOException, NoConnectionToDBException, SQLException, ClientBinaryNotFoundException {
         boolean foundSolverWithSameName = false;
         File tmpDir = new File("tmp");
         tmpDir.mkdir();
         Tasks.getTaskView().setCancelable(true);
         Calendar cal = Calendar.getInstance();
         String dateStr = cal.get(Calendar.YEAR) + "" + (cal.get(Calendar.MONTH) < 9 ? "0" + (cal.get(Calendar.MONTH) + 1) : (cal.get(Calendar.MONTH) + 1)) + "" + (cal.get(Calendar.DATE) < 10 ? "0" + cal.get(Calendar.DATE) : cal.get(Calendar.DATE));
         ArrayList<ExperimentHasGridQueue> eqs = ExperimentHasGridQueueDAO.getExperimentHasGridQueueByExperiment(activeExperiment);
         int count = 0;
         for (ExperimentHasGridQueue eq : eqs) {
             GridQueue queue = GridQueueDAO.getById(eq.getIdGridQueue());
 
             File zipFile = new File(location + getFilename(activeExperiment.getName() + "_" + queue.getName() + "_" + dateStr + ".zip"));
             if (zipFile.exists()) {
                 zipFile.delete();
             }
             ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
             ZipEntry entry;
 
 
             task.setOperationName("Generating Package " + (++count) + " of " + eqs.size());
             ArrayList<Solver> solvers;
             if (exportSolvers) {
                 solvers = ExperimentDAO.getSolversInExperiment(activeExperiment);
             } else {
                 solvers = new ArrayList<Solver>();
             }
 
             LinkedList<Instance> instances;
             if (exportInstances) {
                 instances = InstanceDAO.getAllByExperimentId(activeExperiment.getId());
             } else {
                 instances = new LinkedList<Instance>();
             }
 
             int total = solvers.size() + instances.size();
             int done = 0;
 
 
             HashSet<String> tmp = new HashSet<String>();
             HashSet<String> solvernameMap = new HashSet<String>();
             for (Solver s : solvers) {
                 if (tmp.contains(s.getBinaryName() + "_" + s.getVersion())) {
                     solvernameMap.add(s.getBinaryName() + "_" + s.getVersion());
                     foundSolverWithSameName = true;
                 } else {
                     tmp.add(s.getBinaryName() + "_" + s.getVersion());
                 }
             }
 
             if (!task.isCancelled() && exportSolvers) {
                 // add solvers to zip file
                 for (Solver s : solvers) {
                     done++;
                     task.setTaskProgress((float) done / (float) total);
                     if (task.isCancelled()) {
                         task.setStatus("Cancelled");
                         break;
                     }
                     task.setStatus("Writing solver " + done + " of " + solvers.size());
                     File bin = SolverDAO.getBinaryFileOfSolver(s);
                     String filename;
                     if (solvernameMap.contains(s.getBinaryName())) {
                         filename = s.getBinaryName() + "_" + s.getVersion() + "_" + s.getMd5().substring(0, 3);
                     } else {
                         filename = s.getBinaryName() + "_" + s.getVersion();
                     }
                     entry = new ZipEntry("solvers" + System.getProperty("file.separator") + filename);
                     addFileToZIP(bin, entry, zos);
                 }
             }
 
             if (!task.isCancelled() && exportInstances) {
                 // add instances to zip file
                 for (Instance i : instances) {
                     done++;
                     task.setTaskProgress((float) done / (float) total);
                     if (task.isCancelled()) {
                         task.setStatus("Cancelled");
                         break;
                     }
                     task.setStatus("Writing instance " + (done - solvers.size()) + " of " + instances.size());
                     File f = InstanceDAO.getBinaryFileOfInstance(i);
                     entry = new ZipEntry("instances" + System.getProperty("file.separator") + i.getName());
                     addFileToZIP(f, entry, zos);
                 }
             }
 
             if (!task.isCancelled()) {
                 task.setStatus("Writing client");
 
                 // add PBS script
                 File f = GridQueueDAO.getPBS(queue);
                 entry = new ZipEntry("start_client.pbs");
                 addFileToZIP(f, entry, zos);
 
                 // add configuration File
                 addConfigurationFile(zos, activeExperiment, queue);
 
                 // add run script
                 addRunScript(zos, exportInstances, exportSolvers, queue);
 
                 // add client binary
                 addClient(zos);
 
                 // add empty result library
                 entry = new ZipEntry("results" + System.getProperty("file.separator") + "~");
                 zos.putNextEntry(entry);
             }
             zos.close();
 
             // delete tmp directory
             deleteDirectory(new File("tmp"));
         }
         if (foundSolverWithSameName) {
             javax.swing.JOptionPane.showMessageDialog(null, "The resulting package file contains solvers with same names.", "Information", javax.swing.JOptionPane.INFORMATION_MESSAGE);
         }
 
     }
 
     private boolean deleteDirectory(File dir) {
         if (dir.exists()) {
             File[] files = dir.listFiles();
             for (int i = 0; i < files.length; i++) {
                 if (files[i].isDirectory()) {
                     deleteDirectory(files[i]);
                 } else {
                     files[i].delete();
                 }
             }
         }
         return (dir.delete());
     }
 
     /**
      * Adds a file to an open zip file.
      * @param f the location of the file to be added.
      * @param entry the zip entry to be created.
      * @param zos the open ZIPOutputStream of the zip file.
      */
     private void addFileToZIP(File f, ZipEntry entry, ZipOutputStream zos) throws FileNotFoundException, IOException {
         FileInputStream in = new FileInputStream(f);
         zos.putNextEntry(entry);
 
         byte[] buf = new byte[256 * 1024];
         int len;
         while ((len = in.read(buf)) > -1) {
             zos.write(buf, 0, len);
         }
         zos.closeEntry();
         in.close();
     }
 
     /**
      * Assigns all gridQueues to the active experiment.
      * This means: It creates a new ExperimentHasGridQueue object for each queue
      * and deletes all ExperimentHasGridQueue objects which are not in queues vektor
      * and persists it in the db
      * @param queues
      * @throws SQLException
      */
     public void assignQueuesToExperiment(ArrayList<GridQueue> queues) throws SQLException {
         boolean autoCommit = DatabaseConnector.getInstance().getConn().getAutoCommit();
         try {
             DatabaseConnector.getInstance().getConn().setAutoCommit(false);
             // create all ExperimentHasGridQueue objects which do not exist
             for (GridQueue q : queues) {
                 // check if assignment already exists
                 if (ExperimentHasGridQueueDAO.getByExpAndQueue(activeExperiment, q) != null) {
                     continue;
                 }
                 ExperimentHasGridQueueDAO.createExperimentHasGridQueue(activeExperiment, q);
             }
 
             // remove all ExperimentHasGridQueue objects which are not in the queues vektor
             ArrayList<ExperimentHasGridQueue> ehgqs = ExperimentHasGridQueueDAO.getExperimentHasGridQueueByExperiment(activeExperiment);
             for (ExperimentHasGridQueue egq : ehgqs) {
                 boolean found = false;
                 for (GridQueue q : queues) {
                     if (egq.getIdGridQueue() == q.getId()) {
                         found = true;
                         break;
                     }
                 }
                 if (!found) {
                     ExperimentHasGridQueueDAO.removeExperimentHasGridQueue(egq);
                 }
             }
         } catch (SQLException e) {
             DatabaseConnector.getInstance().getConn().rollback();
             throw e;
         } finally {
             DatabaseConnector.getInstance().getConn().setAutoCommit(autoCommit);
         }
     }
 
     public void selectAllInstanceClasses() {
         main.instanceClassModel.beginUpdate();
         for (int i = 0; i < main.instanceClassModel.getRowCount(); i++) {
             main.instanceClassModel.setInstanceClassSelected(i);
         }
         main.instanceClassModel.endUpdate();
     }
 
     public void deselectAllInstanceClasses() {
         main.instanceClassModel.beginUpdate();
         for (int i = 0; i < main.instanceClassModel.getRowCount(); i++) {
             main.instanceClassModel.setInstanceClassDeselected(i);
         }
         main.instanceClassModel.endUpdate();
     }
 
     private void addConfigurationFile(ZipOutputStream zos, Experiment activeExperiment, GridQueue activeQueue) throws IOException {
         // generate content of config file
         String sConf = "host = $host\n" + "username = $user\n" + "password = $pwd\n" + "database = $db\n" + "experiment = $exp\n" + "gridqueue = $q\n";
         DatabaseConnector con = DatabaseConnector.getInstance();
         sConf = sConf.replace("$host", con.getHostname());
         sConf = sConf.replace("$user", con.getUsername());
         sConf = sConf.replace("$pwd", con.getPassword());
         sConf = sConf.replace("$db", con.getDatabase());
         sConf = sConf.replace("$exp", String.valueOf(activeExperiment.getId()));
         sConf = sConf.replace("$q", String.valueOf(activeQueue.getId()));
 
         // write file into zip archive
         ZipEntry entry = new ZipEntry("config");
         zos.putNextEntry(entry);
         zos.write(sConf.getBytes());
         zos.closeEntry();
     }
 
     private void addRunScript(ZipOutputStream zos, boolean hasInstances, boolean hasSolvers, GridQueue q) throws IOException {
         String sRun = "#!/bin/bash\n"
                 + "chmod a-rwx client\n"
                 + "chmod u+rwx client\n"
                 + "chmod a-rwx config\n"
                 + "chmod u+rw config\n"
                 + (hasSolvers ? "chmod a-rwx solvers/*\n" : "")
                 + (hasSolvers ? "chmod u+rwx solvers/*\n" : "")
                 + (hasInstances ? "chmod a-rwx instances/*\n" : "")
                 + (hasInstances ? "chmod u+rw instances/*\n" : "")
                 + "for (( i = 0; i < " + q.getNumNodes() + "; i++ ))\n"
                 + "do\n"
                 + "    qsub start_client.pbs\n"
                 + "done\n";
 
         // write file into zip archive
         ZipEntry entry = new ZipEntry("run.sh");
         zos.putNextEntry(entry);
         zos.write(sRun.getBytes());
         zos.closeEntry();
     }
 
     private void addClient(ZipOutputStream zos) throws IOException, ClientBinaryNotFoundException {
         InputStream in = EDACCApp.class.getClassLoader().getResourceAsStream("edacc/resources/client");
         if (in == null) {
             throw new ClientBinaryNotFoundException();
         }
         ZipEntry entry = new ZipEntry("client");
         zos.putNextEntry(entry);
 
         int data;
 
         while ((data = in.read()) > -1) {
             zos.write(data);
         }
         zos.closeEntry();
         in.close();
     }
 
     /**
      * Exports all jobs and all columns currently visible to a CSV file.
      * @param file
      * @throws IOException
      */
     public void exportCSV(File file, Tasks task) throws IOException {
         Tasks.getTaskView().setCancelable(true);
         task.setOperationName("Exporting jobs to CSV file");
 
         if (file.exists()) {
             file.delete();
         }
 
         PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
         for (int i = 0; i < main.jobsTableModel.getColumnCount(); i++) {
             int vis_col = main.tableJobs.convertColumnIndexToModel(i);
             out.write("\"" + main.jobsTableModel.getColumnName(vis_col) + "\"");
             if (i < main.jobsTableModel.getColumnCount() - 1) {
                 out.write(",");
             }
         }
         out.write('\n');
 
         int total = main.getTableJobs().getRowCount();
         int done = 0;
         for (int i = 0; i < main.tableJobs.getRowCount(); i++) {
             int vis = main.getTableJobs().convertRowIndexToModel(i);
             done++;
             task.setTaskProgress((float) done / (float) total);
             if (task.isCancelled()) {
                 task.setStatus("Cancelled");
                 break;
             }
             task.setStatus("Exporting row " + done + " of " + total);
             for (int col = 0; col < main.jobsTableModel.getColumnCount(); col++) {
                 int vis_col = main.tableJobs.convertColumnIndexToModel(col);
                 if (main.jobsTableModel.getValueAt(vis, vis_col) == null) {
                     out.write("\"-\"");
                 } else {
                     out.write("\"" + main.jobsTableModel.getValueAt(vis, vis_col).toString() + "\"");
                 }
                 if (col < main.jobsTableModel.getColumnCount() - 1) {
                     out.write(",");
                 }
             }
             out.write('\n');
         }
 
         out.flush();
         out.close();
     }
 
     /**
      * Exports all jobs and all columns currently visible to a TeX file.
      * @param file
      * @throws IOException
      */
     public void exportTeX(File file, Tasks task) throws IOException {
         Tasks.getTaskView().setCancelable(true);
         task.setOperationName("Exporting jobs to TeX file");
 
         if (file.exists()) {
             file.delete();
         }
 
         PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
 
         String format = "|";
         String columns = "";
         for (int i = 0; i < main.jobsTableModel.getColumnCount(); i++) {
             int vis_col = main.tableJobs.convertColumnIndexToModel(i);
             columns += main.jobsTableModel.getColumnName(vis_col);
             if (main.jobsTableModel.getColumnClass(vis_col) == Integer.class
                     || main.jobsTableModel.getColumnClass(vis_col) == Double.class
                     || main.jobsTableModel.getColumnClass(vis_col) == Float.class
                     || main.jobsTableModel.getColumnClass(vis_col) == int.class
                     || main.jobsTableModel.getColumnClass(vis_col) == double.class
                     || main.jobsTableModel.getColumnClass(vis_col) == float.class) {
                 format += "r|";
             } else {
                 format += "l|";
             }
             if (i < main.jobsTableModel.getColumnCount() - 1) {
                 columns += "&";
             }
         }
         out.write("\\documentclass[a4paper]{report}\n");
         out.write("\\title{Results of " + activeExperiment.getName() + "}\n");
         out.write("\\begin{document}\n");
         out.write("\\begin{tabular}[h]{" + format + "}\n");
         out.write("\\hline\n");
         out.write(columns + " \\\\\n");
         out.write("\\hline\n");
         int total = main.getTableJobs().getRowCount();
         int done = 0;
         for (int i = 0; i < main.tableJobs.getRowCount(); i++) {
             int vis = main.getTableJobs().convertRowIndexToModel(i);
             done++;
             task.setTaskProgress((float) done / (float) total);
             if (task.isCancelled()) {
                 task.setStatus("Cancelled");
                 break;
             }
             task.setStatus("Exporting row " + done + " of " + total);
             for (int col = 0; col < main.jobsTableModel.getColumnCount(); col++) {
                 int vis_col = main.tableJobs.convertColumnIndexToModel(col);
                 if (main.jobsTableModel.getValueAt(vis, vis_col) == null) {
                     out.write("-");
                 } else {
                     out.write(main.jobsTableModel.getValueAt(vis, vis_col).toString());
                 }
                 if (col < main.jobsTableModel.getColumnCount() - 1) {
                     out.write(" & ");
                 }
             }
             out.write("\\\\\n");
         }
         out.write("\\hline\n");
         out.write("\\end{tabular}\n");
         out.write("\\end{document}\n");
         out.flush();
         out.close();
     }
 
     /**
      * Sets the priority of the currently visible jobs in the job browser to <code>priority</code> and
      * updates the local cached experiment results.
      * @param priority the new priority for the jobs
      */
     public void setPriority(int priority) throws SQLException, IOException, PropertyTypeNotExistException, PropertyNotInDBException, NoConnectionToDBException, ComputationMethodDoesNotExistException, ExpResultHasSolvPropertyNotInDBException, ExperimentResultNotInDBException {
         ArrayList<ExperimentResult> jobs = main.jobsTableModel.getJobs();
         ArrayList<ExperimentResult> updatedJobs = new ArrayList<ExperimentResult>();
         for (int i = 0; i < main.tableJobs.getRowCount(); i++) {
             int vis = main.getTableJobs().convertRowIndexToModel(i);
             if (jobs.get(vis).getPriority() != priority) {
                 jobs.get(vis).setPriority(priority);
                 updatedJobs.add(jobs.get(vis));
             }
         }
         ExperimentResultDAO.batchUpdatePriority(updatedJobs);
         this.updateExperimentResults();
     }
 
     /**
      * Returns the experiment with the given name.
      * @param name
      * @return the experiment
      * @throws SQLException
      */
     public Experiment getExperiment(String name) throws SQLException {
         return ExperimentDAO.getExperimentByName(name);
     }
 
     public String getExperimentResultOutput(int type, ExperimentResult er) throws SQLException, NoConnectionToDBException, IOException {
         return ExperimentResultDAO.getOutputText(type, er);
     }
 
     /**
      * Checks if data in the experiment design tabs is modified.
      * @param numRuns
      * @return true, iff some data is modified
      */
     public boolean experimentResultsIsModified(int numRuns) {
         try {
             if (numRuns != activeExperiment.getNumRuns()) {
                 return true;
             }
             ArrayList<Integer> solverConfigIds = ExperimentResultDAO.getAllSolverConfigIdsByExperimentId(activeExperiment.getId());
             if (solverConfigIds.isEmpty()) {
                 return false;
             }
             ArrayList<Integer> instanceIds = ExperimentResultDAO.getAllInstanceIdsByExperimentId(activeExperiment.getId());
             if (!SolverConfigurationDAO.getAllSolverConfigIdsByExperimentId(activeExperiment.getId()).equals(solverConfigIds)) {
                 return true;
             }
             if (!ExperimentHasInstanceDAO.getAllInstanceIdsByExperimentId(activeExperiment.getId()).equals(instanceIds)) {
                 return true;
             }
         } catch (SQLException _) {
         }
         return false;
     }
 
     /**
      * Returns all experiment results with any run in the given list for the active experiment.
      * updateExperimentResults() should be called first.
      * @param runs
      * @return arraylist of experiment results
      */
     public synchronized ArrayList<ExperimentResult> getAllByRun(ArrayList<Integer> runs) {
         HashSet<Integer> set = new HashSet<Integer>();
         set.addAll(runs);
         ArrayList<ExperimentResult> res = new ArrayList<ExperimentResult>();
         for (ExperimentResult er : resultMap.values()) {
             if (set.contains(er.getRun())) {
                 res.add(er);
             }
         }
         return res;
     }
 
     /**
      * Returns all experiment results with the specified run for the active experiment.
      * updateExperimentResults() should be called first.
      * @param runs
      * @return arraylist of experiment results
      */
     public synchronized ArrayList<ExperimentResult> getAllByRun(Integer run) {
         ArrayList<ExperimentResult> res = new ArrayList<ExperimentResult>();
         for (ExperimentResult er : resultMap.values()) {
             if (er.getRun() == run) {
                 res.add(er);
             }
         }
         return res;
     }
 
     /**
      * Returns all disjunct runs in an array list.
      * This array list should contain all integers between 0 and numRuns-1 inclusive.
      * updateExperimentResults() should be called first.
      * @return an array list of all runs
      * @throws SQLException
      * @throws IOException
      * @throws PropertyTypeNotExistException
      * @throws NoConnectionToDBException
      * @throws PropertyNotInDBException
      * @throws ComputationMethodDoesNotExistException
      */
     public synchronized ArrayList<Integer> getAllRuns() throws SQLException, IOException, PropertyTypeNotExistException, NoConnectionToDBException, PropertyNotInDBException, ComputationMethodDoesNotExistException {
         // then look for all disjunct runs and return them in an array list
         HashSet<Integer> runs = new HashSet<Integer>();
         for (ExperimentResult er : resultMap.values()) {
             runs.add(er.getRun());
         }
         ArrayList<Integer> res = new ArrayList<Integer>();
         res.addAll(runs);
         return res;
     }
 
     /**
      * Updates the experiment result cache. The resultMap is then synchronized with the database
      * @throws SQLException
      * @throws IOException
      * @throws PropertyTypeNotExistException
      * @throws PropertyNotInDBException
      * @throws NoConnectionToDBException
      * @throws ComputationMethodDoesNotExistException
      */
     public synchronized void updateExperimentResults() throws SQLException, IOException, PropertyTypeNotExistException, PropertyNotInDBException, NoConnectionToDBException, ComputationMethodDoesNotExistException, ExpResultHasSolvPropertyNotInDBException, ExperimentResultNotInDBException {
         if (activeExperiment == null) {
             experiment = null;
             resultMap = null;
             return;
         }
         if (experiment != activeExperiment) {
             resultMap = null;
             lastUpdated = new Timestamp(0);
         }
         Timestamp ts = ExperimentResultDAO.getLastModifiedByExperimentId(getActiveExperiment().getId());
         ArrayList<ExperimentResult> modified = ExperimentResultDAO.getAllModifiedByExperimentId(activeExperiment.getId(), lastUpdated);
         if (resultMap == null) {
             resultMap = new HashMap<ResultIdentifier, ExperimentResult>();
         }
         for (ExperimentResult result : modified) {
             ResultIdentifier key = new ResultIdentifier(result.getSolverConfigId(), result.getInstanceId(), result.getRun());
             if (resultMap.containsKey(key)) {
                 resultMap.remove(key);
             }
             resultMap.put(key, result);
         }
         int count = ExperimentResultDAO.getCountByExperimentId(getActiveExperiment().getId());
         if (count != resultMap.size()) {
             // full update
             resultMap.clear();
             ArrayList<ExperimentResult> experimentResults = ExperimentResultDAO.getAllByExperimentId(getActiveExperiment().getId());
             for (ExperimentResult result : experimentResults) {
                 resultMap.put(new ResultIdentifier(result.getSolverConfigId(), result.getInstanceId(), result.getRun()), result);
             }
         }
         lastUpdated = ts;
         experiment = activeExperiment;
     }
 
     /**
      * Returns a Vector of all ExperimentResults in the current experiment with the solverConfig id and instance id specified
      * @param solverConfigId the solverConfig id of the ExperimentResults
      * @param instanceId the instance id of the ExperimentResults
      * @return returns an empty vector if there are no such ExperimentResults
      */
     public ArrayList<ExperimentResult> getResults(int solverConfigId, int instanceId) {
         return getResults(solverConfigId, instanceId, null);
     }
 
     /**
      * Returns a Vector of all ExperimentResults in the current experiment with the solverConfig id and instance id specified
      * @param solverConfigId the solverConfig id of the ExperimentResults
      * @param instanceId the instance id of the ExperimentResults
      * @return returns an empty vector if there are no such ExperimentResults
      */
     public ArrayList<ExperimentResult> getResults(int solverConfigId, int instanceId, ExperimentResultStatus[] status) {
         ArrayList<ExperimentResult> res = new ArrayList<ExperimentResult>();
         for (int i = 0; i < experiment.getNumRuns(); i++) {
             ExperimentResult result = getResult(solverConfigId, instanceId, i, status);
             if (result != null) {
                 res.add(result);
             }
         }
         return res;
     }
 
     /**
      * Returns an ExperimentResult identified by solverConfig id, instance id and run for the current experiment.
      * @param solverConfigId the solverConfig id for the ExperimentResult
      * @param instanceId the instance id for the ExperimentResult
      * @param run the run
      * @return returns null if there is no such ExperimentResult
      */
     public ExperimentResult getResult(int solverConfigId, int instanceId, int run) {
         return getResult(solverConfigId, instanceId, run, null);
     }
 
     /**
      * Returns an ExperimentResult identified by solverConfig id, instance id and run for the current experiment.
      * @param solverConfigId the solverConfig id for the ExperimentResult
      * @param instanceId the instance id for the ExperimentResult
      * @param run the run
      * @return returns null if there is no such ExperimentResult
      */
     public synchronized ExperimentResult getResult(int solverConfigId, int instanceId, int run, ExperimentResultStatus[] status) {
         ExperimentResult res = resultMap.get(new ResultIdentifier(solverConfigId, instanceId, run));
         if (status != null) {
             boolean found = false;
             for (ExperimentResultStatus s : status) {
                 if (res.getStatus().equals(s)) {
                     found = true;
                     break;
                 }
             }
             if (!found) {
                 return null;
             }
         }
         return res;
     }
 
     /**
      * Returns a double value for the given property value type and the string value
      * @param type
      * @param value
      * @return null, iff the property value type doesn't represent Integer, Float or Double or an error occurred.
      */
     private Double transformPropertyValueTypeToDouble(PropertyValueType type, String value) {
         Double res = null;
         try {
             if (type.getJavaType() == Integer.class) {
                 res = new Double((Integer) type.getJavaTypeRepresentation(value));
             } else if (type.getJavaType() == Float.class) {
                 res = new Double((Float) type.getJavaTypeRepresentation(value));
             } else if (type.getJavaType() == Double.class) {
                 res = (Double) type.getJavaTypeRepresentation(value);
             }
         } catch (ConvertException ex) {
             return null;
         }
         return res;
     }
 
     /**
      * Returns the value for the given property and the given experiment result.
      * @param result
      * @param property
      * @return null, iff the experiment result`s status is RUNNING, NOTSTARTED, LAUNCHERCRASH, VERIFIERCRASH, WATCHERCRASH or the property isn't calculated or the result isn't successfully verified. In case of the cpu-time property CPUTimeLmit can be returned.
      */
     public Double getValue(ExperimentResult result, Property property) {
 
         if (result.getStatus() == ExperimentResultStatus.RUNNING
                 || result.getStatus() == ExperimentResultStatus.NOTSTARTED
                 || result.getStatus() == ExperimentResultStatus.LAUNCHERCRASH
                 || result.getStatus() == ExperimentResultStatus.VERIFIERCRASH
                 || result.getStatus() == ExperimentResultStatus.WATCHERCRASH) {
             return null;
         }
         if (property == PROP_CPUTIME) {
             if (!String.valueOf(result.getResultCode().getValue()).startsWith("1")) {
                 return new Double(experiment.getCPUTimeLimit());
             }
             return Double.valueOf(result.getResultTime());
         } else {
             if (!String.valueOf(result.getResultCode().getValue()).startsWith("1")) {
                 return null;
             }
             ExperimentResultHasProperty erhsp = result.getPropertyValues().get(property.getId());
             if (erhsp == null || erhsp.getValue().isEmpty()) {
                 return null;
             }
             return transformPropertyValueTypeToDouble(property.getPropertyValueType(), erhsp.getValue().get(0));
         }
     }
 
     /**
      * Returns the value of the property for this instance
      * @param instance the instance
      * @param property the property
      * @return null, iff the property isn't calculated or some error occurred.
      */
     public Double getValue(Instance instance, Property property) {
         InstanceHasProperty ihip = instance.getPropertyValues().get(property.getId());
         if (ihip == null) {
             return null;
         }
         return transformPropertyValueTypeToDouble(property.getPropertyValueType(), ihip.getValue());
     }
 
     /**
      * Returns all instances.
      * @return arraylist of the instances
      * @throws SQLException
      * @throws InstanceClassMustBeSourceException
      * @throws IOException
      * @throws NoConnectionToDBException
      * @throws PropertyNotInDBException
      * @throws PropertyTypeNotExistException
      * @throws ComputationMethodDoesNotExistException
      */
     public ArrayList<Instance> getInstances() throws SQLException, InstanceClassMustBeSourceException, IOException, NoConnectionToDBException, PropertyNotInDBException, PropertyTypeNotExistException, ComputationMethodDoesNotExistException {
         if (activeExperiment == null) {
             return null;
         }
         ArrayList<Instance> res = new ArrayList<Instance>();
         res.addAll(InstanceDAO.getAllByExperimentId(activeExperiment.getId()));
         return res;
     }
 
     /**
      * Returns all solver configurations.
      * @return arraylist of the solver configurations
      * @throws SQLException
      */
     public ArrayList<SolverConfiguration> getSolverConfigurations() throws SQLException {
         if (activeExperiment == null) {
             return null;
         }
         return SolverConfigurationDAO.getSolverConfigurationByExperimentId(activeExperiment.getId());
     }
 
     /**
      * Returns all result properties; adds CPUTime as result property.
      * @return arraylist of the result properties
      * @throws Exception
      */
     public ArrayList<Property> getResultProperties() throws Exception {
         ArrayList<Property> res = new ArrayList<Property>();
         res.add(ExperimentController.PROP_CPUTIME);
         res.addAll(PropertyDAO.getAllResultProperties());
         return res;
     }
 
     /**
      * Returns all instance properties.
      * @return arraylist of the instance properties
      * @throws Exception
      */
     public ArrayList<Property> getInstanceProperties() throws Exception {
         ArrayList<Property> res = new ArrayList<Property>();
         res.addAll(PropertyDAO.getAllInstanceProperties());
         return res;
     }
 
     /**
      * Used to identify an experiment result in the local cache.
      */
     class ResultIdentifier {
 
         int solverConfigId;
         int instanceId;
         int run;
 
         public ResultIdentifier(int solverConfigId, int instanceId, int run) {
             this.solverConfigId = solverConfigId;
             this.instanceId = instanceId;
             this.run = run;
         }
 
         @Override
         public boolean equals(Object obj) {
             if (obj == null) {
                 return false;
             }
             if (getClass() != obj.getClass()) {
                 return false;
             }
             final ResultIdentifier other = (ResultIdentifier) obj;
             if (this.solverConfigId != other.solverConfigId) {
                 return false;
             }
             if (this.instanceId != other.instanceId) {
                 return false;
             }
             if (this.run != other.run) {
                 return false;
             }
             return true;
         }
 
         @Override
         public int hashCode() {
             int hash = 3;
             hash = 53 * hash + this.solverConfigId;
             hash = 53 * hash + this.instanceId;
             hash = 53 * hash + this.run;
             return hash;
         }
     }
 }
