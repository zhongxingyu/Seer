 package edacc.experiment;
 
 import com.mysql.jdbc.exceptions.MySQLStatementCancelledException;
 import edacc.model.SolverConfigCache;
 import edacc.EDACCExperimentMode;
 import edacc.api.costfunctions.*;
 import edacc.experiment.tabs.solver.SolverConfigurationEntry;
 import edacc.experiment.tabs.solver.SolverConfigurationEntryModel;
 import edacc.model.ClientDAO;
 import edacc.model.ComputationMethodDoesNotExistException;
 import edacc.model.ConfigurationScenario;
 import edacc.model.ConfigurationScenarioDAO;
 import edacc.model.ConfigurationScenarioParameter;
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
 import edacc.model.ExperimentResultDAO.IdValue;
 import edacc.model.ExperimentResultHasProperty;
 import edacc.model.ExperimentResultNotInDBException;
 import edacc.model.ResultCode;
 import edacc.model.ResultCodeNotInDBException;
 import edacc.model.StatusCode;
 import edacc.model.StatusCodeDAO;
 import edacc.model.GridQueue;
 import edacc.model.GridQueueDAO;
 import edacc.model.Instance;
 import edacc.model.InstanceClassDAO;
 import edacc.model.InstanceClassMustBeSourceException;
 import edacc.model.InstanceDAO;
 import edacc.model.InstanceHasProperty;
 import edacc.model.InstanceNotInDBException;
 import edacc.model.NoConnectionToDBException;
 import edacc.model.Parameter;
 import edacc.model.ParameterDAO;
 import edacc.model.ParameterInstance;
 import edacc.model.ParameterInstanceDAO;
 import edacc.model.Property;
 import edacc.model.PropertyDAO;
 import edacc.model.Solver;
 import edacc.model.SolverConfiguration;
 import edacc.model.SolverDAO;
 import edacc.model.PropertyNotInDBException;
 import edacc.model.ResultCodeDAO;
 import edacc.model.SolverBinaries;
 import edacc.model.SolverBinariesDAO;
 import edacc.model.SolverConfigurationDAO;
 import edacc.model.StatusCodeNotInDBException;
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
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Random;
 import java.util.Vector;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 import java.util.zip.ZipOutputStream;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import javax.swing.tree.DefaultMutableTreeNode;
 
 /**
  * Experiment design more controller class, handles requests by the GUI
  * for creating, removing, loading, etc. experiments.
  * @author daniel, simon
  */
 public class ExperimentController {
 
     EDACCExperimentMode main;
     private Experiment activeExperiment;
     private static RandomNumberGenerator rnd = new JavaRandom();
     // caching experiments
     private ExperimentResultCache experimentResultCache;
     /** caching solver configs */
     public SolverConfigCache solverConfigCache;
     /** the cpu time property. Will be created when creating an experiment controller. */
     public static Property PROP_CPUTIME;
     private ConfigurationScenario configScenario;
     private ConfigurationScenario savedScenario;
     private SolverConfigurationEntryModel solverConfigurationEntryModel;
     /**
      * Creates a new experiment Controller
      * @param experimentMode the experiment mode to be used
      * @param solverConfigPanel the solver config panel to be used
      */
     public ExperimentController(EDACCExperimentMode experimentMode) {
         this.main = experimentMode;
         solverConfigurationEntryModel = new SolverConfigurationEntryModel();
         PROP_CPUTIME = new Property();
         PROP_CPUTIME.setName("CPU-Time (s)");
 
     }
 
     /**
      * Initializes the experiment controller. Loads the experiments and the instances classes.
      * @throws SQLException
      * @throws InstanceClassMustBeSourceException
      * @throws IOException
      * @throws NoConnectionToDBException
      * @throws PropertyNotInDBException
      * @throws PropertyTypeNotExistException
      * @throws ComputationMethodDoesNotExistException 
      */
     public void initialize() throws SQLException, InstanceClassMustBeSourceException, IOException, NoConnectionToDBException, PropertyNotInDBException, PropertyTypeNotExistException, ComputationMethodDoesNotExistException {
         InstanceDAO.clearCache();
         StatusCodeDAO.initialize();
         ResultCodeDAO.initialize();
         ClientDAO.clearCache();
 
         SolverBinariesDAO.clearCache();
         SolverDAO.clearCache();
         ParameterDAO.clearCache();
         ExperimentDAO.clearCache();
         SolverConfigurationDAO.clearCache();
         ParameterInstanceDAO.clearCache();
 
         ArrayList<Experiment> experiments = new ArrayList<Experiment>();
         experiments.addAll(ExperimentDAO.getAll());
         main.expTableModel.setExperiments(experiments);
 
         DefaultMutableTreeNode root = (DefaultMutableTreeNode) InstanceClassDAO.getAllAsTreeFast();
         main.instanceClassTreeModel.setRoot(root);
         ArrayList<Instance> instances = new ArrayList<Instance>();
         instances.addAll(InstanceDAO.getAll());
         main.insTableModel.setInstances(instances, true, true);
 
         final boolean isCompetitionDB = DatabaseConnector.getInstance().isCompetitionDB();
         SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 if (!isCompetitionDB) {
                     main.tableInstances.removeColumn(main.tableInstances.getColumnModel().getColumn(InstanceTableModel.COL_BENCHTYPE));
                 }
             }
         });
     }
 
     /**
      * Loads an experiment, the solvers and the solver configurations.
      * @param exp
      * @param task
      * @throws SQLException
      * @throws Exception 
      */
     public void loadExperiment(Experiment exp, final Tasks task) throws SQLException, Exception {
         unloadExperiment();
         main.reinitializeSolvers();
         activeExperiment = exp;
         ArrayList<Solver> vs = new ArrayList<Solver>();
         Vector<ExperimentHasInstance> ehi = new Vector<ExperimentHasInstance>();
         task.setStatus("Loading solvers..");
         vs.addAll(SolverDAO.getAll());
         main.solTableModel.setSolvers(vs);
         task.setTaskProgress(.25f);
         task.setStatus("Loading solver configurations..");
 
         // now load solver configs of the current experiment
         if (solverConfigCache != null) {
             solverConfigCache.changeExperiment(activeExperiment);
         } else {
             solverConfigCache = new SolverConfigCache(activeExperiment);
             solverConfigCache.reload();
         }
         solverConfigurationEntryModel.clear();
         for (SolverConfiguration solverConfig : solverConfigCache.getAll()) {
             solverConfigurationEntryModel.add(new SolverConfigurationEntry(solverConfig, activeExperiment));
         }
         solverConfigurationEntryModel.fireDataChanged();
         task.setTaskProgress(.5f);
         task.setStatus("Loading instances..");
         // select instances for the experiment
         ehi.addAll(ExperimentHasInstanceDAO.getExperimentHasInstanceByExperimentId(activeExperiment.getId()));
         main.insTableModel.setExperimentHasInstances(ehi);
 
         task.setTaskProgress(.75f);
         task.setStatus("Loading experiment results..");
         experimentResultCache = new ExperimentResultCache(activeExperiment);
 
         PropertyChangeListener cancelExperimentResultDAOStatementListener = null;
         if (Tasks.getTaskView() != null) {
             task.setCancelable(true);
             cancelExperimentResultDAOStatementListener = new PropertyChangeListener() {
 
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
             task.addPropertyChangeListener(cancelExperimentResultDAOStatementListener);
         }
         try {
             experimentResultCache.updateExperimentResults();
         } catch (MySQLStatementCancelledException ex) {
             throw new TaskCancelledException();
         } finally {
             task.setCancelable(false);
             if (cancelExperimentResultDAOStatementListener != null) {
                 task.removePropertyChangeListener(cancelExperimentResultDAOStatementListener);
             }
         }
         main.generateJobsTableModel.updateNumRuns();
 
         SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 Util.updateTableColumnWidth(main.tblGenerateJobs);
             }
         });
         if (activeExperiment.isConfigurationExp()) {
             configScenario = ConfigurationScenarioDAO.getConfigurationScenarioByExperimentId(activeExperiment.getId());
             // we need a new instance for this
             savedScenario = ConfigurationScenarioDAO.getConfigurationScenarioByExperimentId(activeExperiment.getId());
         }
         main.afterExperimentLoaded();
     }
 
     /**
      * Removes an experiment form the db.
      * @param id
      * @throws SQLException
      * @throws InstanceClassMustBeSourceException
      * @throws IOException
      * @throws NoConnectionToDBException
      * @throws PropertyNotInDBException
      * @throws PropertyTypeNotExistException
      * @throws ComputationMethodDoesNotExistException 
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
      * Returns the experiment result cache
      * @return the experiment result cache
      */
     public ExperimentResultCache getExperimentResults() {
         return experimentResultCache;
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
         experimentResultCache = null;
         main.afterExperimentUnloaded();
     }
 
     /**
      * invoked by the UI to create a new experiment, also calls initialize to load
      * instances and solvers
      * @param name
      * @param description
      * @param configurationExp
      * @return the newly created experiment
      * @throws SQLException
      * @throws InstanceClassMustBeSourceException
      * @throws IOException
      * @throws NoConnectionToDBException
      * @throws PropertyNotInDBException
      * @throws PropertyTypeNotExistException
      * @throws ComputationMethodDoesNotExistException 
      */
     public Experiment createExperiment(String name, String description, boolean configurationExp) throws SQLException, InstanceClassMustBeSourceException, IOException, NoConnectionToDBException, PropertyNotInDBException, PropertyTypeNotExistException, ComputationMethodDoesNotExistException {
         java.util.Date d = new java.util.Date();
         Experiment res = ExperimentDAO.createExperiment(name, new Date(d.getTime()), description, configurationExp);
         initialize();
         return res;
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
      * @param task
      * @throws SQLException
      * @throws InterruptedException
      * @throws InvocationTargetException
      * @throws PropertyNotInDBException
      * @throws PropertyTypeNotExistException
      * @throws IOException
      * @throws NoConnectionToDBException
      * @throws ComputationMethodDoesNotExistException
      * @throws ExpResultHasSolvPropertyNotInDBException
      * @throws ExperimentResultNotInDBException
      * @throws StatusCodeNotInDBException
      * @throws ResultCodeNotInDBException
      * @throws Exception 
      */
     public void saveSolverConfigurations(Tasks task) throws SQLException, InterruptedException, InvocationTargetException, PropertyNotInDBException, PropertyTypeNotExistException, IOException, NoConnectionToDBException, ComputationMethodDoesNotExistException, ExpResultHasSolvPropertyNotInDBException, ExperimentResultNotInDBException, StatusCodeNotInDBException, ResultCodeNotInDBException, Exception {
         // TODO: there are some points where this task should never be canceled (by an application crash or lost db connection)... fix them!
         
         task.setStatus("Checking jobs..");
 
         // check for solver configurations with no value for parameters which must have values
         boolean yta = false;
         for (Solver solver : solverConfigurationEntryModel.getSolvers()) {
             for (SolverConfigurationEntry entry : solverConfigurationEntryModel.getEntries(solver)) {
                 if (entry.isModified() && entry.hasEmptyValues()) {
                     String[] options = {"Yes", "Yes to all", "No"};
                     int userinput = JOptionPane.showOptionDialog(Tasks.getTaskView(),
                             "The solver configuration " + entry.getName() + " has no value for a parameter which must have a value.\nDo you want to continue?",
                             "Warning",
                             JOptionPane.DEFAULT_OPTION,
                             JOptionPane.WARNING_MESSAGE,
                             null, options, options[0]);
                     if (userinput == 1) {
                         yta = true;
                         break;
                     } else if (userinput == 2) {
                         return;
                     }
                 }
             }
             if (yta) {
                 break;
             }
         }
 
         // check for deleted solver configurations (jobs have to be deleted)
         experimentResultCache.updateExperimentResults();
 
         ArrayList<SolverConfiguration> deletedSolverConfigurations = solverConfigCache.getAllDeleted();
         final ArrayList<ExperimentResult> deletedJobs = new ArrayList<ExperimentResult>();
         final HashSet<Integer> scIds = new HashSet<Integer>();
         for (SolverConfiguration sc : deletedSolverConfigurations) {
             scIds.add(sc.getId());
         }
         for (ExperimentResult job : experimentResultCache.values()) {
             if (scIds.contains(job.getSolverConfigId())) {
                 deletedJobs.add(job);
             }
         }
 
         // check for modified solver configurations (jobs have to be deleted)
         // this will append (to deletedJobs) all modified solver configurations, i.e. the solver configurations are not marked as deleted and
         //  * the seed group of the solver configurations has been changed or
         //  * some parameter instances have been modified/deleted or added
         ArrayList<SolverConfiguration> modifiedSolverConfigurations = solverConfigurationEntryModel.getModifiedSolverConfigurations();
         yta = false;
         boolean nta = false;
         for (SolverConfiguration sc : modifiedSolverConfigurations) {
             int userinput = -1;
             if (!yta && !nta) {
                 String[] options = {"Yes", "Yes to all", "No", "No to all"};
                 userinput = JOptionPane.showOptionDialog(Tasks.getTaskView(),
                         "The parameter values of the solver configuration " + sc.getName() + " have been changed.\nDo you want to delete the affected jobs?",
                         "Warning",
                         JOptionPane.DEFAULT_OPTION,
                         JOptionPane.WARNING_MESSAGE,
                         null, options, options[0]);
                 if (userinput == 1) {
                     yta = true;
                 } else if (userinput == 3) {
                     nta = true;
                 }
             }
             if (!nta && (yta || userinput == 0)) {
                 deletedJobs.addAll(ExperimentResultDAO.getAllBySolverConfiguration(sc));
             }
         }
         if (deletedJobs.size() > 0) {
             int notDeletableJobsCount = 0;
             for (ExperimentResult job : deletedJobs) {
                 if (job.getStatus() != StatusCode.NOT_STARTED) {
                     notDeletableJobsCount++;
                 }
             }
             String msg = "";
             if (notDeletableJobsCount > 0) {
                 msg = "There are " + notDeletableJobsCount + " started jobs and " + (deletedJobs.size() - notDeletableJobsCount) + " jobs waiting in the database which would be deleted. ";
             } else {
                 msg = "There are " + deletedJobs.size() + " jobs waiting in the database which would be deleted. ";
             }
             msg += "Do you want to continue?";
             int userInput = javax.swing.JOptionPane.showConfirmDialog(Tasks.getTaskView(), msg, "Jobs would be changed", javax.swing.JOptionPane.YES_NO_OPTION);
             if (userInput == 1) {
                 return;
             } else {
                 task.setStatus("Deleting jobs..");
                 ExperimentResultDAO.deleteExperimentResults(deletedJobs);
             }
         }
         task.setStatus("Saving solver configurations..");
 
         for (Solver s : solverConfigurationEntryModel.getSolvers()) {
             // iterate over solvers
             for (SolverConfigurationEntry entry : solverConfigurationEntryModel.getEntries(s)) {
                 // iterate over solver configs
                 if (entry.getSolverConfig() == null) {
                     entry.setSolverConfig(solverConfigCache.createSolverConfiguration(entry.getSolverBinary(), activeExperiment.getId(), entry.getSeedGroup(), entry.getName(), entry.getHint()));
                 } else {
                     entry.getSolverConfig().setSolverBinary(entry.getSolverBinary());
                     entry.getSolverConfig().setName(entry.getName());
                     entry.getSolverConfig().setSeed_group(entry.getSeedGroup());
                     entry.getSolverConfig().setHint(entry.getHint());
                 }
                 entry.saveParameterInstances();
             }
         }
         solverConfigCache.saveAll();
         getExperimentResults().updateExperimentResults();
         main.generateJobsTableModel.updateNumRuns();
         SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 Util.updateTableColumnWidth(main.tblGenerateJobs);
             }
         });
     }
 
     /**
      * This will clean up all changed data for the solver configurations.
      * @param task
      * @throws SQLException
      */
     public void undoSolverConfigurations(Tasks task) throws SQLException {
         solverConfigCache.reload();
         solverConfigurationEntryModel.clear();
         for (SolverConfiguration solverConfig : solverConfigCache.getAll()) {
             solverConfigurationEntryModel.add(new SolverConfigurationEntry(solverConfig, activeExperiment));
         }
         solverConfigurationEntryModel.fireDataChanged();
         main.setTitles();
     }
 
     /**
      * saves the instances selection of the currently loaded experiment
      * @param task
      * @throws SQLException
      * @throws InterruptedException
      * @throws InvocationTargetException
      * @throws PropertyNotInDBException
      * @throws PropertyTypeNotExistException
      * @throws IOException
      * @throws NoConnectionToDBException
      * @throws ComputationMethodDoesNotExistException
      * @throws ExpResultHasSolvPropertyNotInDBException
      * @throws ExperimentResultNotInDBException
      * @throws StatusCodeNotInDBException
      * @throws ResultCodeNotInDBException 
      */
     public void saveExperimentHasInstances(Tasks task) throws SQLException, InterruptedException, InvocationTargetException, PropertyNotInDBException, PropertyTypeNotExistException, IOException, NoConnectionToDBException, ComputationMethodDoesNotExistException, ExpResultHasSolvPropertyNotInDBException, ExperimentResultNotInDBException, StatusCodeNotInDBException, ResultCodeNotInDBException {
         task.setStatus("Checking jobs..");
         ArrayList<ExperimentHasInstance> deletedInstances = main.insTableModel.getDeletedExperimentHasInstances();
         if (deletedInstances.size() > 0) {
             ArrayList<ExperimentResult> deletedJobs = new ArrayList<ExperimentResult>();
             for (ExperimentHasInstance ehi : deletedInstances) {
                 deletedJobs.addAll(ExperimentResultDAO.getAllByExperimentHasInstance(ehi));
             }
             int notDeletableJobsCount = 0;
             for (ExperimentResult job : deletedJobs) {
                 if (job.getStatus() != StatusCode.NOT_STARTED) {
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
         main.generateJobsTableModel.updateNumRuns();
         SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 Util.updateTableColumnWidth(main.tblGenerateJobs);
             }
         });
     }
 
     /**
      * method used for auto seed generation, uses the random number generator
      * referenced by this.rnd
      * @return integer between 0 and max inclusively
      */
     public int generateSeed(int max) {
         return rnd.nextInt(max + 1);
     }
 
     /**
      * generates the ExperimentResults (jobs) in the database for the currently active experiment
      * Doesn't overwrite existing jobs
      * @param task
      * @param cpuTimeLimit
      * @param memoryLimit
      * @param wallClockTimeLimit
      * @param stackSizeLimit
      * @param outputSizeLimitFirst
      * @param outputSizeLimitLast
      * @param maxSeed
      * @return number of jobs added to the experiment results table
      * @throws SQLException
      * @throws TaskCancelledException
      * @throws IOException
      * @throws PropertyTypeNotExistException
      * @throws PropertyNotInDBException
      * @throws NoConnectionToDBException
      * @throws ComputationMethodDoesNotExistException
      * @throws ExpResultHasSolvPropertyNotInDBException
      * @throws ExperimentResultNotInDBException
      * @throws StatusCodeNotInDBException
      * @throws ResultCodeNotInDBException 
      */
     public synchronized int generateJobs(final Tasks task, int cpuTimeLimit, int memoryLimit, int wallClockTimeLimit, int stackSizeLimit, int outputSizeLimitFirst, int outputSizeLimitLast, int maxSeed) throws SQLException, TaskCancelledException, IOException, PropertyTypeNotExistException, PropertyNotInDBException, NoConnectionToDBException, ComputationMethodDoesNotExistException, ExpResultHasSolvPropertyNotInDBException, ExperimentResultNotInDBException, StatusCodeNotInDBException, ResultCodeNotInDBException {
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
         experimentResultCache.updateExperimentResults();
         // get instances of this experiment
         LinkedList<Instance> listInstances = InstanceDAO.getAllByExperimentId(activeExperiment.getId());
 
         // get solver configurations of this experiment
         ArrayList<SolverConfiguration> vsc = solverConfigCache.getAll();
 
         int experiments_added = 0;
         HashMap<SeedGroup, Integer> linked_seeds = new HashMap<SeedGroup, Integer>();
         ArrayList<ExperimentResult> experiment_results = new ArrayList<ExperimentResult>();
 
         ArrayList<ExperimentResult> deleteJobs = new ArrayList<ExperimentResult>();
         ArrayList<IdValue<Integer>> updateJobs = new ArrayList<IdValue<Integer>>();
         task.setStatus("Preparing..");
         int elements = 0; // # jobs when finished
         for (Instance i : listInstances) {
             for (SolverConfiguration sc : vsc) {
                 int numRuns = main.generateJobsTableModel.getNumRuns(i, sc);
                 elements += numRuns;
                 int currentNumRuns = experimentResultCache.getResults(sc.getId(), i.getId()).size();
                 if (currentNumRuns > numRuns) {
                     // we have to delete jobs
                     ArrayList<Integer> runs = new ArrayList<Integer>();
                     for (int run = 0; run < currentNumRuns; run++) {
                         runs.add(run);
                     }
                     int runsToDelete = currentNumRuns - numRuns;
                     Random random = new Random();
                     for (int k = 0; k < runsToDelete; k++) {
                         if (runs.isEmpty()) {
                             break;
                         }
                         int index = random.nextInt(runs.size());
                         deleteJobs.add(experimentResultCache.getResult(sc.getId(), i.getId(), runs.get(index)));
                         runs.remove(index);
                     }
 
                     for (int k = 0; k < runs.size(); k++) {
                         updateJobs.add(new IdValue<Integer>(experimentResultCache.getResult(sc.getId(), i.getId(), runs.get(k)).getId(), k));
                     }
                 }
             }
         }
 
         if (!deleteJobs.isEmpty()) {
             // We have to delete jobs
             String msg = "The number of runs specified is less than the number of runs in this experiment. There are " + deleteJobs.size() + " jobs which would be deleted. Do you want to continue?";
             int userInput = javax.swing.JOptionPane.showConfirmDialog(Tasks.getTaskView(), msg, "Jobs would be deleted", javax.swing.JOptionPane.YES_NO_OPTION);
             task.setCancelable(true);
             task.setTaskProgress(0.f);
             if (userInput == 1) {
                 return 0;
             } else {
                 task.setStatus("Deleting jobs..");
                 task.addPropertyChangeListener(cancelExperimentResultDAOStatementListener);
                 try {
                     ExperimentResultDAO.setAutoCommit(false);
                     ExperimentResultDAO.deleteExperimentResults(deleteJobs);
                     task.setStatus("Updating existing jobs..");
                     ExperimentResultDAO.batchUpdateRun(updateJobs);
                 } catch (Exception ex) {
                     DatabaseConnector.getInstance().getConn().rollback();
                     if (ex instanceof MySQLStatementCancelledException) {
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
             }
         }
         task.setCancelable(false);
         task.setStatus("Updating local cache..");
         experimentResultCache.updateExperimentResults();
         task.setCancelable(true);
 
         task.setStatus("Preparing job generation");
 
         HashMap<Integer, Boolean> solverConfigHasSeed = new HashMap<Integer, Boolean>();
 
         // first pass over already existing jobs to accumulate existing linked seeds
         for (SolverConfiguration c : vsc) {
             Vector<Parameter> params = ParameterDAO.getParameterFromSolverId(c.getSolverBinary().getIdSolver());
             boolean hasSeed = false;
             for (Parameter p : params) {
                 if ("seed".equals(p.getName())) {
                     hasSeed = true;
                     break;
                 }
             }
             solverConfigHasSeed.put(c.getId(), hasSeed);
             if (!hasSeed) {
                 // don't generate seed groups for solver configurations which have no seed
                 continue;
             }
 
             for (Instance i : listInstances) {
                 ArrayList<ExperimentResult> jobs = experimentResultCache.getResults(c.getId(), i.getId());
                 for (ExperimentResult job : jobs) {
                     // use the already existing jobs to populate the seed group hash table so jobs of newly added solver configs use
                     // the same seeds as already existing jobs
                     int seed = job.getSeed();
                     SeedGroup sg = new SeedGroup(c.getSeed_group(), i.getId(), job.getRun());
                     if (!linked_seeds.containsKey(sg)) {
                         linked_seeds.put(sg, new Integer(seed));
                     }
                 }
             }
         }
         if (task.isCancelled()) {
             throw new TaskCancelledException();
         }
 
         int done = 1;
 
         for (Instance i : listInstances) {
             for (SolverConfiguration c : vsc) {
                 for (int run = 0; run < main.generateJobsTableModel.getNumRuns(i, c); ++run) {
                     task.setTaskProgress((float) done / (float) elements);
                     if (task.isCancelled()) {
                         throw new TaskCancelledException();
                     }
                     //  task.setStatus("Adding job " + done + " of " + elements);
                     // check if job already exists
                     if (!experimentResultCache.contains(c.getId(), i.getId(), run)) {
                         if (solverConfigHasSeed.get(c.getId())) {
                             Integer seed = linked_seeds.get(new SeedGroup(c.getSeed_group(), i.getId(), run));
                             if (seed != null) {
                                 experiment_results.add(ExperimentResultDAO.createExperimentResult(run, 0, 0, StatusCode.NOT_STARTED, seed.intValue(), ResultCode.UNKNOWN, 0, c.getId(), activeExperiment.getId(), i.getId(), null, cpuTimeLimit, memoryLimit, wallClockTimeLimit, stackSizeLimit, outputSizeLimitFirst, outputSizeLimitLast));
                             } else {
                                 Integer new_seed = new Integer(generateSeed(maxSeed));
                                 linked_seeds.put(new SeedGroup(c.getSeed_group(), i.getId(), run), new_seed);
                                 experiment_results.add(ExperimentResultDAO.createExperimentResult(run, 0, 0, StatusCode.NOT_STARTED, new_seed.intValue(), ResultCode.UNKNOWN, 0, c.getId(), activeExperiment.getId(), i.getId(), null, cpuTimeLimit, memoryLimit, wallClockTimeLimit, stackSizeLimit, outputSizeLimitFirst, outputSizeLimitLast));
                             }
                         } else {
                             experiment_results.add(ExperimentResultDAO.createExperimentResult(run, 0, 0, StatusCode.NOT_STARTED, 0, ResultCode.UNKNOWN, 0, c.getId(), activeExperiment.getId(), i.getId(), null, cpuTimeLimit, memoryLimit, wallClockTimeLimit, stackSizeLimit, outputSizeLimitFirst, outputSizeLimitLast));
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
         experimentResultCache.updateExperimentResults();
         main.generateJobsTableModel.updateNumRuns();
         SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 Util.updateTableColumnWidth(main.tblGenerateJobs);
             }
         });
         return experiments_added;
 
     }
 
     /**
      * returns the number maximum run of the jobs in the database for the given experiment
      * @return the number of jobs in the db
      */
     public int getMaxRun() {
         try {
             return ExperimentResultDAO.getMaximumRun(activeExperiment);
         } catch (Exception e) {
             return 0;
         }
     }
 
     /**
      * Updates the job browser table
      */
     public synchronized void loadJobs() {
         try {
             boolean autocommit = DatabaseConnector.getInstance().getConn().getAutoCommit();
             DatabaseConnector.getInstance().getConn().setAutoCommit(false);
             try {
                 experimentResultCache.updateExperimentResults();
                 // TODO: also update solver config cache if needed and cache parameter instances of solver configs
                 //       maybe this should be done in experimentResultCache?
                
                // temporary: simple synchronize, but may not be needed..
                solverConfigCache.synchronize();
             } finally {
                 DatabaseConnector.getInstance().getConn().setAutoCommit(autocommit);
             }
             final ExperimentResultsBrowserTableModel sync = main.jobsTableModel;
             synchronized (sync) {
 
                 ArrayList<ExperimentResult> results = main.jobsTableModel.getJobs();
                 final HashSet<Integer> changedRows = new HashSet<Integer>();
                 if (results != null) {
                     if (results.size() != experimentResultCache.size()) {
                         results = null;
                     } else {
                         for (int i = 0; i < results.size(); i++) {
                             ExperimentResult er = results.get(i);
                             ExperimentResult tmp = experimentResultCache.getResult(er.getSolverConfigId(), er.getInstanceId(), er.getRun());
                             if (tmp == null) {
                                 results = null;
                                 break;
                             } else if (!er.getDatemodified().equals(tmp.getDatemodified())) {
                                 results.set(i, tmp);
                                 changedRows.add(i);
                             }
                         }
                         if (results != null && results.size() != experimentResultCache.size()) {
                             results = null;
                         }
                     }
                 }
                 if (results == null) {
                     results = new ArrayList<ExperimentResult>();
                     results.addAll(experimentResultCache.values());
                     main.jobsTableModel.setJobs(results);
                     main.resultBrowserRowFilter.updateFilterTypes();
                     main.jobsTableModel.fireTableDataChanged();
                 } else {
                     SwingUtilities.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             if (changedRows.size() > 1000) {
                                 main.jobsTableModel.fireTableDataChanged();
                             } else {
                                 for (Integer i : changedRows) {
                                     main.jobsTableModel.fireTableRowsUpdated(i, i);
                                 }
                             }
                         }
                     });
 
 
                     SwingUtilities.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             main.tableJobs.invalidate();
                             main.tableJobs.revalidate();
                             main.tableJobs.repaint();
                         }
                     });
                 }
                 SwingUtilities.invokeLater(new Runnable() {
 
                     @Override
                     public void run() {
                         main.updateJobsFilterStatus();
                     }
                 });
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
 
     }
 
     /**
      * Generates a ZIP archive with the necessary files for the grid.
      * @param location
      * @param exportInstances
      * @param exportSolvers
      * @param exportClient
      * @param exportRunsolver
      * @param exportConfig
      * @param clientBinary
      * @param task
      * @throws FileNotFoundException
      * @throws IOException
      * @throws NoConnectionToDBException
      * @throws SQLException
      * @throws ClientBinaryNotFoundException
      * @throws InstanceNotInDBException
      * @throws TaskCancelledException 
      */
     public void generatePackage(String location, boolean exportInstances, boolean exportSolvers, boolean exportClient, boolean exportRunsolver, boolean exportConfig, boolean exportVerifier, File clientBinary, File verifierBinary, Tasks task) throws FileNotFoundException, IOException, NoConnectionToDBException, SQLException, ClientBinaryNotFoundException, InstanceNotInDBException, TaskCancelledException {
         File tmpDir = new File("tmp");
         tmpDir.mkdir();
         task.setCancelable(true);
         Calendar cal = Calendar.getInstance();
         String dateStr = cal.get(Calendar.YEAR) + "" + (cal.get(Calendar.MONTH) < 9 ? "0" + (cal.get(Calendar.MONTH) + 1) : (cal.get(Calendar.MONTH) + 1)) + "" + (cal.get(Calendar.DATE) < 10 ? "0" + cal.get(Calendar.DATE) : cal.get(Calendar.DATE));
         ArrayList<ExperimentHasGridQueue> eqs = ExperimentHasGridQueueDAO.getExperimentHasGridQueueByExperiment(activeExperiment);
         int count = 0;
         for (ExperimentHasGridQueue eq : eqs) {
             GridQueue queue = GridQueueDAO.getById(eq.getIdGridQueue());
 
             File zipFile = new File(location + Util.getFilename(activeExperiment.getName() + "_" + queue.getName() + "_" + dateStr + ".zip"));
             if (zipFile.exists()) {
                 zipFile.delete();
             }
             ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
             ZipEntry entry;
 
 
             task.setOperationName("Generating Package " + (++count) + " of " + eqs.size());
             ArrayList<SolverBinaries> solverBinaries;
             if (exportSolvers) {
                 solverBinaries = SolverBinariesDAO.getSolverBinariesInExperiment(activeExperiment);
             } else {
                 solverBinaries = new ArrayList<SolverBinaries>();
             }
 
             LinkedList<Instance> instances;
             if (exportInstances) {
                 instances = InstanceDAO.getAllByExperimentId(activeExperiment.getId());
             } else {
                 instances = new LinkedList<Instance>();
             }
 
             int total = solverBinaries.size() + instances.size();
             int done = 0;
 
             if (!task.isCancelled() && exportSolvers) {
                 // add solvers to zip file
                 for (SolverBinaries binary : solverBinaries) {
                     done++;
                     task.setTaskProgress((float) done / (float) total);
                     if (task.isCancelled()) {
                         task.setStatus("Cancelled");
                         break;
                     }
                     task.setStatus("Writing solver " + done + " of " + solverBinaries.size());
                     ZipInputStream zis = new ZipInputStream(SolverBinariesDAO.getZippedBinaryFile(binary));
                     ZipEntry entryIn;
                     byte[] buffer = new byte[4 * 1024];
                     while ((entryIn = zis.getNextEntry()) != null) {
                         if (entryIn.isDirectory()) {
                             continue;
                         }
                         entry = new ZipEntry("solvers" + System.getProperty("file.separator") + binary.getMd5() + System.getProperty("file.separator") + entryIn.getName());
                         zos.putNextEntry(entry);
 
                         int read;
                         while ((read = zis.read(buffer, 0, buffer.length)) != -1) {
                             zos.write(buffer, 0, read);
                         }
 
                         zos.closeEntry();
                     }
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
                     task.setStatus("Writing instance " + (done - solverBinaries.size()) + " of " + instances.size());
                     File f = InstanceDAO.getBinaryFileOfInstance(i);
                     entry = new ZipEntry("instances" + System.getProperty("file.separator") + i.getMd5() + "_" + i.getName());
                     addFileToZIP(f, entry, zos);
                 }
             }
 
             if (!task.isCancelled()) {
                 task.setStatus("Writing client");
 
                 // add configuration File
                 if (exportConfig) {
                     String verifierFilename = verifierBinary == null ? null : "verifiers/" + verifierBinary.getName();
                     addConfigurationFile(zos, queue, verifierFilename);
                 }
 
                 // add run script
                 // addRunScript(zos, exportInstances, exportSolvers, queue);
 
                 // add client binary
                 if (exportClient) {
                     addClient(zos, clientBinary);
                 }
 
                 // add runsolver
                 if (exportRunsolver) {
                     addRunsolver(zos);
                 }
 
                 // add verifier
                 if (exportVerifier) {
                     addVerifier(zos, verifierBinary);
                 }
             }
             zos.close();
 
             // delete tmp directory
             deleteDirectory(new File("tmp"));
 
             if (task.isCancelled()) {
                 throw new TaskCancelledException("Cancelled");
             }
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
 
     private void addConfigurationFile(ZipOutputStream zos, GridQueue activeQueue, String verifierFilename) throws IOException {
         // generate content of config file
         String sConf =
                 "host = $host\n"
                 + "username = $user\n"
                 + "password = $pwd\n"
                 + "database = $db\n"
                 + "gridqueue = $q\n";
         DatabaseConnector con = DatabaseConnector.getInstance();
         sConf = sConf.replace("$host", con.getHostname());
         sConf = sConf.replace("$user", con.getUsername());
         sConf = sConf.replace("$pwd", con.getPassword());
         sConf = sConf.replace("$db", con.getDatabase());
         sConf = sConf.replace("$q", String.valueOf(activeQueue.getId()));
 
         if (verifierFilename != null) {
             sConf += "verifier = " + verifierFilename + "\n";
         }
 
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
                 + "for (( i = 0; i < " + q.getNumCPUs() + "; i++ ))\n"
                 + "do\n"
                 + "    qsub start_client.pbs\n"
                 + "done\n";
 
         // write file into zip archive
         ZipEntry entry = new ZipEntry("run.sh");
         zos.putNextEntry(entry);
         zos.write(sRun.getBytes());
         zos.closeEntry();
     }
 
     private void addClient(ZipOutputStream zos, File clientBinary) throws IOException, ClientBinaryNotFoundException {
 
         String[] files = new String[]{"AUTHORS", clientBinary.getName(), "LICENSE", "README"};
         for (String filename : files) {
             File f = new File(clientBinary.getParentFile() + System.getProperty("file.separator") + filename);
             if (!f.exists() || f.isDirectory()) {
                 continue;
             }
             InputStream in = new FileInputStream(f);
             if (in == null) {
                 throw new ClientBinaryNotFoundException();
             }
             ZipEntry entry = new ZipEntry(filename);
             zos.putNextEntry(entry);
 
             byte[] buf = new byte[1024];
             int data;
 
             while ((data = in.read(buf)) > -1) {
                 zos.write(buf, 0, data);
             }
             zos.closeEntry();
             in.close();
         }
     }
 
     private void addVerifier(ZipOutputStream zos, File verifierBinary) throws IOException, ClientBinaryNotFoundException {
         InputStream in = new FileInputStream(verifierBinary);
         if (in == null) {
             throw new ClientBinaryNotFoundException();
         }
         ZipEntry entry = new ZipEntry("verifiers/" + verifierBinary.getName());
         zos.putNextEntry(entry);
 
         byte[] buf = new byte[1024];
         int data;
 
         while ((data = in.read(buf)) > -1) {
             zos.write(buf, 0, data);
         }
         zos.closeEntry();
         in.close();
     }
 
     private void addRunsolver(ZipOutputStream zos) throws IOException, ClientBinaryNotFoundException {
         String[] files = new String[]{"runsolver", "runsolver_copyright.txt"};
         for (String filename : files) {
             InputStream in = new FileInputStream(new File(Util.getPath() + System.getProperty("file.separator") + "bin" + System.getProperty("file.separator") + filename));
             if (in == null) {
                 throw new ClientBinaryNotFoundException();
             }
             ZipEntry entry = new ZipEntry(filename);
             zos.putNextEntry(entry);
 
             byte[] buf = new byte[1024];
             int data;
 
             while ((data = in.read(buf)) > -1) {
                 zos.write(buf, 0, data);
             }
             zos.closeEntry();
             in.close();
         }
     }
 
     /**
      * Exports all jobs and all columns currently visible to a CSV file.
      * @param file
      * @param task
      * @throws IOException 
      */
     public void exportCSV(File file, Tasks task) throws IOException {
         task.setCancelable(true);
         task.setOperationName("Exporting jobs to CSV file");
 
         if (file.exists()) {
             file.delete();
         }
 
         PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
         for (int i = 0; i < main.tableJobs.getColumnCount(); i++) {
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
             for (int col = 0; col < main.tableJobs.getColumnCount(); col++) {
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
      * @param task
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
         for (int i = 0; i < main.tableJobs.getColumnCount(); i++) {
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
             for (int col = 0; col < main.tableJobs.getColumnCount(); col++) {
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
      * Sets the priority of the currently visible jobs in the job browser to <code>priority</code>,
      * updates the local cached experiment results and updates the gui.
      * @param priority the new priority for the jobs
      * @throws SQLException
      * @throws IOException
      * @throws PropertyTypeNotExistException
      * @throws PropertyNotInDBException
      * @throws NoConnectionToDBException
      * @throws ComputationMethodDoesNotExistException
      * @throws ExpResultHasSolvPropertyNotInDBException
      * @throws ExperimentResultNotInDBException
      */
     public void setPriority(int priority) throws SQLException, IOException, PropertyTypeNotExistException, PropertyNotInDBException, NoConnectionToDBException, ComputationMethodDoesNotExistException, ExpResultHasSolvPropertyNotInDBException, ExperimentResultNotInDBException {
         ArrayList<ExperimentResult> jobs = main.jobsTableModel.getJobs();
         ArrayList<IdValue<Integer>> values = new ArrayList<IdValue<Integer>>();
         for (int i = 0; i < main.tableJobs.getRowCount(); i++) {
             int vis = main.getTableJobs().convertRowIndexToModel(i);
             if (jobs.get(vis).getPriority() != priority) {
                 values.add(new IdValue<Integer>(jobs.get(vis).getId(), priority));
             }
         }
         ExperimentResultDAO.batchUpdatePriority(values);
         this.loadJobs();
     }
 
     /**
      * Sets the status of all currently visible jobs in the job browser to <code>status</code>,
      * updates the local cached experiment results and updates the gui.
      * @param status the new status for the jobs
      * @throws SQLException
      */
     public void setStatus(StatusCode status) throws SQLException {
         ArrayList<ExperimentResult> jobs = main.jobsTableModel.getJobs();
         ArrayList<ExperimentResult> updatedJobs = new ArrayList<ExperimentResult>();
         for (int i = 0; i < main.tableJobs.getRowCount(); i++) {
             int vis = main.getTableJobs().convertRowIndexToModel(i);
             if (!jobs.get(vis).getStatus().equals(status)) {
                 updatedJobs.add(jobs.get(vis));
             }
         }
         ExperimentResultDAO.batchUpdateStatus(updatedJobs, status);
         this.loadJobs();
     }
 
     /**
      * Returns an <code>ArrayList</code> of all experiments in the experiments table
      * @return <code>ArrayList</code> of experiments
      */
     public ArrayList<Experiment> getExperiments() {
         ArrayList<Experiment> experiments = new ArrayList<Experiment>();
         for (int row = 0; row < main.expTableModel.getRowCount(); row++) {
             experiments.add(main.expTableModel.getExperimentAt(row));
         }
         return experiments;
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
 
     /**
      * Returns the output of a experiment result as a string
      * @param type for possible types see edacc.model.ExperimentResult
      * @param er
      * @return
      * @throws SQLException
      * @throws NoConnectionToDBException
      * @throws IOException 
      * @see edacc.model.ExperimentResult
      */
     public String getExperimentResultOutput(int type, ExperimentResult er) throws SQLException, NoConnectionToDBException, IOException {
         return ExperimentResultDAO.getOutputText(type, er);
     }
 
     /**
      * returns a hashmap containing the maximum limits of the experiment results for the currently loaded experiment.<br/>
      * <br/>
      * possible keys are: cpuTimeLimit, memoryLimit, wallClockTimeLimit, stackSizeLimit, outputSizeLimitFirst, outputSizeLimitLast
      * @return
      * @throws SQLException
      * @throws Exception 
      */
     public HashMap<String, Integer> getMaxLimits() throws SQLException, Exception {
         HashMap<String, Integer> res = new HashMap<String, Integer>();
         int cpuTimeLimit = -1;
         int memoryLimit = -1;
         int wallClockTimeLimit = -1;
         int stackSizeLimit = -1;
         int outputSizeLimitLast = -1;
         int outputSizeLimitFirst = -1;
 
         experimentResultCache.updateExperimentResults();
         for (ExperimentResult er : experimentResultCache.values()) {
             if (er.getCPUTimeLimit() > cpuTimeLimit) {
                 cpuTimeLimit = er.getCPUTimeLimit();
             }
             if (er.getMemoryLimit() > memoryLimit) {
                 memoryLimit = er.getMemoryLimit();
             }
             if (er.getWallClockTimeLimit() > wallClockTimeLimit) {
                 wallClockTimeLimit = er.getWallClockTimeLimit();
             }
             if (er.getStackSizeLimit() > stackSizeLimit) {
                 stackSizeLimit = er.getStackSizeLimit();
             }
             if (er.getOutputSizeLimitFirst() > outputSizeLimitFirst) {
                 outputSizeLimitFirst = er.getOutputSizeLimitFirst();
             }
             if (er.getOutputSizeLimitLast() > outputSizeLimitLast) {
                 outputSizeLimitLast = er.getOutputSizeLimitLast();
             }
         }
 
         res.put("cpuTimeLimit", cpuTimeLimit);
         res.put("memoryLimit", memoryLimit);
         res.put("wallClockTimeLimit", wallClockTimeLimit);
         res.put("stackSizeLimit", stackSizeLimit);
         res.put("outputSizeLimitFirst", outputSizeLimitFirst);
         res.put("outputSizeLimitLast", outputSizeLimitLast);
         return res;
     }
 
     /**
      * Imports data from the specified solver configurations to the currently loaded experiment. <br/>
      * <br/>
      * <br/>
      * Let e_i be the experiments to import data from and e the currently loaded experiment then 
      * the following statements are valid for the merged experiment: <br/>
      *  * instances(e) = UNION{instances(e_i)}
      *  * sc(e) = UNION{sc(e_i)}, sc_1 == sc_2 iff they are equal in their semantics (same solver, same parameters)
      *  * ... tbd
      * @param task
      * @param solverConfigIds
      * @param duplicate
      * @throws SQLException
      * @throws Exception
      */
     public void importDataFromSolverConfigurations(Tasks task, ArrayList<Integer> solverConfigIds, ArrayList<Boolean> duplicate) throws SQLException, Exception {
         /*  if (solverConfigIds.size() != duplicate.size()) {
         throw new IllegalArgumentException("solverConfigIds.size() != duplicate.size()");
         }
         final boolean autocommit = DatabaseConnector.getInstance().getConn().getAutoCommit();
         DatabaseConnector.getInstance().getConn().setAutoCommit(false);
         try {
         HashSet<Integer> experimentIds = new HashSet<Integer>();
         for (int scId : solverConfigIds) {
         experimentIds.add(SolverConfigurationDAO.getSolverConfigurationById(scId).getExperiment_id());
         }
         boolean first = true;
         boolean useNotFinishedJobs = true;
         for (int expId : experimentIds) {
         Experiment exp = ExperimentDAO.getById(expId);
         if (first) {
         first = false;
         activeExperiment.setAutoGeneratedSeeds(exp.isAutoGeneratedSeeds());
         activeExperiment.setCPUTimeLimit(exp.getCPUTimeLimit());
         activeExperiment.setLinkSeeds(exp.isLinkSeeds());
         activeExperiment.setMaxSeed(exp.getMaxSeed());
         activeExperiment.setMemoryLimit(exp.getMemoryLimit());
         activeExperiment.setOutputSizeLimit(exp.getOutputSizeLimit());
         activeExperiment.setStackSizeLimit(exp.getStackSizeLimit());
         activeExperiment.setWallClockTimeLimit(exp.getWallClockTimeLimit());
         } else {
         activeExperiment.setAutoGeneratedSeeds(activeExperiment.isAutoGeneratedSeeds() | exp.isAutoGeneratedSeeds());
         activeExperiment.setLinkSeeds(activeExperiment.isLinkSeeds() | exp.isLinkSeeds());
         if (activeExperiment.getMaxSeed() < exp.getMaxSeed()) {
         activeExperiment.setMaxSeed(exp.getMaxSeed());
         }
         if (activeExperiment.getCPUTimeLimit() != exp.getCPUTimeLimit()) {
         useNotFinishedJobs = false;
         if (activeExperiment.getCPUTimeLimit() < exp.getCPUTimeLimit() && activeExperiment.getCPUTimeLimit() != -1) {
         activeExperiment.setCPUTimeLimit(exp.getCPUTimeLimit());
         }
         }
         if (activeExperiment.getMemoryLimit() != exp.getMemoryLimit()) {
         useNotFinishedJobs = false;
         if (activeExperiment.getMemoryLimit() < exp.getMemoryLimit() && activeExperiment.getMemoryLimit() != -1) {
         activeExperiment.setMemoryLimit(exp.getMemoryLimit());
         }
         }
         if (activeExperiment.getOutputSizeLimit() != exp.getOutputSizeLimit()) {
         useNotFinishedJobs = false;
         if (activeExperiment.getOutputSizeLimit() < exp.getOutputSizeLimit() && activeExperiment.getOutputSizeLimit() != -1) {
         activeExperiment.setOutputSizeLimit(exp.getOutputSizeLimit());
         }
         }
         if (activeExperiment.getStackSizeLimit() != exp.getStackSizeLimit()) {
         useNotFinishedJobs = false;
         if (activeExperiment.getStackSizeLimit() < exp.getStackSizeLimit() && activeExperiment.getStackSizeLimit() != -1) {
         activeExperiment.setStackSizeLimit(exp.getStackSizeLimit());
         }
         }
         if (activeExperiment.getWallClockTimeLimit() != exp.getWallClockTimeLimit()) {
         useNotFinishedJobs = false;
         if (activeExperiment.getWallClockTimeLimit() < exp.getWallClockTimeLimit() && activeExperiment.getWallClockTimeLimit() != -1) {
         activeExperiment.setWallClockTimeLimit(exp.getWallClockTimeLimit());
         }
         }
         }
         }
         
         boolean haveToDuplicate = false;
         //activeExperiment.setAutoGeneratedSeeds(true);
         int numRuns = 0;
         ArrayList<Integer> newIds = new ArrayList<Integer>();
         for (int i = 0; i < solverConfigIds.size(); i++) {
         haveToDuplicate |= duplicate.get(i);
         int scId = solverConfigIds.get(i);
         SolverConfiguration sc = SolverConfigurationDAO.getSolverConfigurationById(scId);
         task.setOperationName("Processing data from solver configuration " + sc.getName() + " (" + (i + 1) + " / " + solverConfigIds.size() + "):");
         task.setTaskProgress((float) (i + 1) / solverConfigIds.size());
         RunCountSCId tmp = this.importDataFromSolverConfiguration(task, sc, useNotFinishedJobs);
         if (tmp.runcount > numRuns) {
         numRuns = tmp.runcount;
         }
         newIds.add(tmp.scid);
         }
         if (haveToDuplicate) {
         task.setStatus("Duplicating results..");
         LinkedList<Instance> instances = InstanceDAO.getAllByExperimentId(activeExperiment.getId());
         updateExperimentResults();
         Random random = new Random();
         ArrayList<ExperimentResult> newResults = new ArrayList<ExperimentResult>();
         ArrayList<ExperimentResult> oldResults = new ArrayList<ExperimentResult>();
         for (int k = 0; k < solverConfigIds.size(); k++) {
         for (Instance instance : instances) {
         if (task.isCancelled()) {
         throw new TaskCancelledException();
         }
         if (duplicate.get(k)) {
         int runCount = this.getResults(newIds.get(k), instance.getId()).size();
         // int runCount = ExperimentDAO.getRunCountInExperimentForSolverConfigurationAndInstance(activeExperiment, newIds.get(k), instance.getId());
         ArrayList<ExperimentResult> results = getResults(newIds.get(k), instance.getId());
         if (results.size() > 0) {
         for (int t = runCount; t < numRuns; t++) {
         ExperimentResult er = results.get(random.nextInt(results.size()));
         newResults.add(ExperimentResultDAO.createExperimentResult(t, er.getPriority(), er.getComputeQueue(), er.getStatus().getDefaultValue(), er.getSeed(), er.getResultCode(), er.getResultTime(), er.getSolverConfigId(), activeExperiment.getId(), er.getInstanceId(), er.getStartTime()));
         oldResults.add(er);
         }
         }
         }
         }
         }
         if (newResults.size() > 0) {
         task.setStatus("Saving results..");
         ExperimentResultDAO.batchSave(newResults);
         if (task.isCancelled()) {
         throw new TaskCancelledException();
         }
         task.setStatus("Saving outputs..");
         ExperimentResultDAO.batchCopyOutputs(oldResults, newResults);
         }
         if (task.isCancelled()) {
         throw new TaskCancelledException();
         }
         }
         generateJobs(numRuns, task);
         task.setStatus("Finalizing..");
         ExperimentDAO.save(activeExperiment);
         updateExperimentResults();
         } catch (Exception e) {
         DatabaseConnector.getInstance().getConn().rollback();
         throw e;
         } finally {
         DatabaseConnector.getInstance().getConn().setAutoCommit(autocommit);
         }*/
     }
 
     /**
      * Imports data from the specified solver configs, instances and runs (if <code>statusCodes</code> is not empty) to the active experiment.
      * @param task
      * @param selectedSolverConfigs the solver configs to import
      * @param selectedInstances the instances to import
      * @param statusCodes also import runs with status code in this list
      * @throws SQLException
      * @throws Exception 
      */
     public void importData(Tasks task, ArrayList<SolverConfiguration> selectedSolverConfigs, ArrayList<Instance> selectedInstances, ArrayList<StatusCode> statusCodes) throws SQLException, Exception {
         if (main.hasUnsavedChanges()) {
             throw new IllegalArgumentException("Assertion failure: Has unsaved changes.");
         }
         // assertion: no unsaved data.
 
         DatabaseConnector.getInstance().getConn().setAutoCommit(false);
         try {
             // get selected instance ids for active experiment
             HashSet<Integer> instanceIds = new HashSet<Integer>();
             for (Instance i : InstanceDAO.getAllByExperimentId(activeExperiment.getId())) {
                 instanceIds.add(i.getId());
             }
 
             // get highest seed group which doesn't exist
             int seed_group = 0;
             for (SolverConfiguration sc : solverConfigCache.getAll()) {
                 if (sc.getSeed_group() >= seed_group) {
                     seed_group = sc.getSeed_group() + 1;
                 }
             }
 
             // check for existing solver configs with same semantics to the imported ones
             HashMap<Integer, Integer> mapHisScToMySc = new HashMap<Integer, Integer>();
             for (SolverConfiguration sc2 : selectedSolverConfigs) {
                 for (SolverConfiguration sc : solverConfigCache.getAll()) {
                     if (sc.hasEqualSemantics(sc2)) {
                         mapHisScToMySc.put(sc2.getId(), sc.getId());
                     }
                 }
             }
 
             // save solver configurations which doesn't exist
             for (SolverConfiguration sc : selectedSolverConfigs) {
                 if (!mapHisScToMySc.containsKey(sc.getId())) {
                     SolverConfiguration sc2 = solverConfigCache.createSolverConfiguration(sc.getSolverBinary(), activeExperiment.getId(), seed_group++, sc.getName(), sc.getHint());
                     for (ParameterInstance pi : ParameterInstanceDAO.getBySolverConfig(sc)) {
                         ParameterInstanceDAO.createParameterInstance(pi.getParameter_id(), sc2, pi.getValue());
                     }
 
                     // update map
                     for (SolverConfiguration tmp : selectedSolverConfigs) {
                         if (tmp.hasEqualSemantics(sc2)) {
                             mapHisScToMySc.put(tmp.getId(), sc2.getId());
                         }
                     }
                 }
             }
 
             // save instances
             for (Instance i : selectedInstances) {
                 if (!instanceIds.contains(i.getId())) {
                     ExperimentHasInstanceDAO.createExperimentHasInstance(activeExperiment.getId(), i.getId());
                 }
             }
 
             // import jobs
             if (!statusCodes.isEmpty()) {
 
 
                 ArrayList<ExperimentResult> resultsToImport = new ArrayList<ExperimentResult>();
                 ArrayList<ExperimentResult> importedResults = new ArrayList<ExperimentResult>();
                 HashMap<Integer, HashMap<Integer, Integer>> instanceSeedGroupFirstRun = new HashMap<Integer, HashMap<Integer, Integer>>();
                 for (Instance i : selectedInstances) {
                     HashMap<Integer, Integer> seedGroupFirstRun = instanceSeedGroupFirstRun.get(i.getId());
                     if (seedGroupFirstRun == null) {
                         seedGroupFirstRun = new HashMap<Integer, Integer>();
                         instanceSeedGroupFirstRun.put(i.getId(), seedGroupFirstRun);
                     }
                     for (SolverConfiguration sc : selectedSolverConfigs) {
                         ArrayList<ExperimentResult> tmp = ExperimentResultDAO.getBySolverConfigurationAndInstance(sc, i);
                         SolverConfiguration solverConfig = SolverConfigurationDAO.getSolverConfigurationById(mapHisScToMySc.get(sc.getId()));
                         Integer firstRun = seedGroupFirstRun.get(solverConfig.getSeed_group());
                         if (firstRun == null) {
                             firstRun = ExperimentResultDAO.getMaxRunForSeedGroupByExperimentIdAndInstanceId(solverConfig.getSeed_group(), activeExperiment.getId(), i.getId()) + 1;
                         }
                         for (ExperimentResult er : tmp) {
                             boolean contains = false;
                             for (StatusCode stat : statusCodes) {
                                 if (er.getStatus().equals(stat)) {
                                     contains = true;
                                     break;
                                 }
                             }
                             if (contains) {
                                 resultsToImport.add(er);
                                 importedResults.add(ExperimentResultDAO.createExperimentResult(firstRun++, er.getPriority(), er.getComputeQueue(), er.getStatus(), er.getSeed(), er.getResultCode(), er.getResultTime(), mapHisScToMySc.get(sc.getId()), activeExperiment.getId(), er.getInstanceId(), er.getStartTime(), er.getCPUTimeLimit(), er.getMemoryLimit(), er.getWallClockTimeLimit(), er.getStackSizeLimit(), er.getOutputSizeLimitFirst(), er.getOutputSizeLimitLast()));
                             }
                         }
                         seedGroupFirstRun.put(solverConfig.getSeed_group(), firstRun);
                     }
                 }
                 ExperimentResultDAO.batchSave(importedResults);
                 ExperimentResultDAO.batchCopyOutputs(resultsToImport, importedResults);
             }
 
             solverConfigCache.reload();
 
             if (!statusCodes.isEmpty()) {
                 experimentResultCache.updateExperimentResults();
                 main.generateJobsTableModel.updateNumRuns();
                 // no use of seed here because seeds are given by seed groups if jobs have to be generated
                 generateJobs(task, -1, -1, -1, -1, -1, -1, 0);
             }
             DatabaseConnector.getInstance().getConn().setAutoCommit(true);
         } catch (Exception ex) {
             DatabaseConnector.getInstance().getConn().rollback();
             DatabaseConnector.getInstance().getConn().setAutoCommit(true);
             throw ex;
         }
 
     }
 
     /**
      * Checks if a grid queue is assigned to this experiment
      * @return true, iff a grid queue is assigned
      */
     public boolean hasGridQueuesAssigned() {
         try {
             if (activeExperiment == null) {
                 return false;
             }
             return !ExperimentHasGridQueueDAO.getExperimentHasGridQueueByExperiment(activeExperiment).isEmpty();
         } catch (SQLException ex) {
             return false;
         }
     }
 
     /**
      * Checks if data in the experiment design tabs is modified.
      * @return true, iff some data is modified
      */
     public boolean experimentResultsIsModified() {
         boolean res = false;
         try {
             experimentResultCache.updateExperimentResults();
             LinkedList<Instance> instances = InstanceDAO.getAllByExperimentId(activeExperiment.getId());
             for (SolverConfiguration sc : solverConfigCache.getAll()) {
                 for (Instance i : instances) {
                     int savedNumRuns = experimentResultCache.getNumRuns(sc.getId(), i.getId());
                     main.generateJobsTableModel.setSavedNumRuns(i, sc, savedNumRuns);
                     if (main.generateJobsTableModel.getNumRuns(i, sc) != savedNumRuns) {
                         res = true;
                     }
                 }
             }
         } catch (Exception _) {
         }
         return res;
     }
 
     public boolean configurationScenarioIsModified() {
         if (activeExperiment == null || !activeExperiment.isConfigurationExp()) {
             return false;
         }
         if (savedScenario == null) {
             return true;
         }
         if (main.configScenarioTableModel.getSolverBinary() == null || savedScenario.getIdSolverBinary() != main.configScenarioTableModel.getSolverBinary().getId()) {
             return true;
         }
         HashMap<Integer, ConfigurationScenarioParameter> configParameters = main.configScenarioTableModel.getConfigScenarioParameters();
         if (configParameters == null) {
             return true;
         }
         if (savedScenario.getParameters().size() != configParameters.size()) {
             return true;
         }
         for (ConfigurationScenarioParameter param : configParameters.values()) {
             if (!ConfigurationScenarioDAO.configurationScenarioParameterIsSaved(param)) {
                 return true;
             }
         }
         return false;
     }
     
     public boolean solverConfigsIsModified() {
         return (solverConfigCache != null && solverConfigCache.isModified()) || (solverConfigurationEntryModel == null ? false : solverConfigurationEntryModel.isModified());
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
         return getValue(result, property, true);
     }
 
     /**
      * Returns the value for the given property and the given experiment result.
      * @param result
      * @param property
      * @param useTimeOutForCPUProp
      * @return null, iff the experiment result`s status is RUNNING, NOTSTARTED, LAUNCHERCRASH, VERIFIERCRASH, WATCHERCRASH or the property isn't calculated or the result isn't successfully verified. In case of the cpu-time property CPUTimeLmit can be returned.
      */
     public Double getValue(ExperimentResult result, Property property, boolean useTimeOutForCPUProp) {
 
         if (result.getStatus() == StatusCode.RUNNING
                 || result.getStatus() == StatusCode.NOT_STARTED
                 || result.getStatus() == StatusCode.LAUNCHERCRASH
                 || result.getStatus() == StatusCode.VERIFIERCRASH
                 || result.getStatus() == StatusCode.WATCHERCRASH) {
             return null;
         }
         if (property == PROP_CPUTIME) {
             if (!String.valueOf(result.getResultCode().getResultCode()).startsWith("1")) {
                 if (useTimeOutForCPUProp) {
                     return new Double(result.getCPUTimeLimit());
                 } else {
                     return null;
                 }
             }
             return Double.valueOf(result.getResultTime());
         } else {
             if (!String.valueOf(result.getResultCode().getResultCode()).startsWith("1")) {
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
         return solverConfigCache.getAll();
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
 
     public void updateConfigScenarioTable(SolverBinaries solverBinary) throws SQLException {
         main.configScenarioTableModel.setConfigurationScenario(solverBinary, configScenario);
     }
 
     public void saveConfigurationScenario() throws SQLException {
         SolverBinaries solverBinary = main.configScenarioTableModel.getSolverBinary();
         if (configScenario == null) {
             configScenario = new ConfigurationScenario();
             configScenario.setIdExperiment(activeExperiment.getId());
             configScenario.setNew();
         }
         configScenario.setIdSolverBinary(solverBinary.getId());
         configScenario.getParameters().clear();
         configScenario.getParameters().addAll(main.configScenarioTableModel.getConfigScenarioParameters().values());
         ConfigurationScenarioDAO.save(configScenario);
         // we need a new instance for this
         savedScenario = ConfigurationScenarioDAO.getConfigurationScenarioByExperimentId(activeExperiment.getId());
     }
     
     public ConfigurationScenario getConfigurationScenarioForExperiment(Experiment exp) throws SQLException {
         ConfigurationScenario scenario = ConfigurationScenarioDAO.getConfigurationScenarioByExperimentId(exp.getId());
         return scenario;
     }
 
     public ConfigurationScenario getConfigScenario() {
         return configScenario;
     }
 
     public void reloadConfigurationScenario() throws SQLException {
         configScenario = ConfigurationScenarioDAO.getConfigurationScenarioByExperimentId(activeExperiment.getId());
     }
     
     public SolverConfigurationEntryModel getSolverConfigurationEntryModel() {
         return solverConfigurationEntryModel;
     }
 
     /**
      * Returns an instance of a cost function from the given database representation,
      * or null, if no such cost function exists.
      * @param databaseRepresentation
      * @return
      */
     public CostFunction costFunctionByName(String databaseRepresentation) {
         if ("average".equals(databaseRepresentation)) {
             return new Average();
         } else if ("median".equals(databaseRepresentation)) {
             return new Median();
         } else if (databaseRepresentation != null && databaseRepresentation.startsWith("par")) {
             try {
                 int penaltyFactor = Integer.valueOf(databaseRepresentation.substring(3));
                 return new PARX(penaltyFactor);
             } catch (Exception e) {
                 return null;
             }
         }
         return null;
     }
 
     /**
      * Calculates the costs for the specified solver configurations. Persists all changes to db.<br/>
      * @param costFunction
      * @param solverConfigs
      * @throws Exception 
      */
     public void calculateCosts(Tasks task, String costFunction, ArrayList<SolverConfiguration> solverConfigs) throws Exception {
         task.setOperationName("Calculating cost for " + costFunction);
         if (costFunction == null) {
             throw new IllegalArgumentException("cost function cannot be null");
         } else if (solverConfigs == null || solverConfigs.isEmpty()) {
             throw new IllegalArgumentException("no solver configurations for cost calculation selected");
         } else if (activeExperiment == null) {
             throw new IllegalArgumentException("no experiment loaded");
         }
         for (SolverConfiguration solverConfig : solverConfigs) {
             if (solverConfig.getExperiment_id() != activeExperiment.getId()) {
                 throw new IllegalArgumentException("calculating costs for other experiments than the loaded experiment is currently not supported");
             }
         }
         CostFunction cFunction = costFunctionByName(costFunction);
         if (cFunction == null) {
             throw new Exception("invalid cost function");
         }
         task.setStatus("Updating jobs..");
         experimentResultCache.updateExperimentResults();
         task.setStatus("Calculating..");
         for (SolverConfiguration solverConfig : solverConfigs) {
             Float cost = cFunction.calculateCost(experimentResultCache.getResults(solverConfig.getId()));
             solverConfig.setCost(cost);
             solverConfig.setCost_function(cFunction.databaseRepresentation());
         }
     }
 }
