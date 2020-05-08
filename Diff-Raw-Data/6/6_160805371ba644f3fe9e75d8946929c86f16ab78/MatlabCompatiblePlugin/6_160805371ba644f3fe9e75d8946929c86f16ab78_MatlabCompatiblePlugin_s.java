 package ch.zhaw.simulation.plugin.matlab;
 
 import java.io.IOException;
 
 import javax.swing.JPanel;
 
 import butti.javalibs.config.Settings;
 import butti.javalibs.dirwatcher.DirectoryWatcher;
 import ch.zhaw.simulation.math.exception.SimulationModelException;
 import ch.zhaw.simulation.model.SimulationDocument;
 import ch.zhaw.simulation.model.SimulationType;
 import ch.zhaw.simulation.model.simulation.SimulationConfiguration;
 import ch.zhaw.simulation.plugin.ExecutionListener.FinishState;
 import ch.zhaw.simulation.plugin.PluginDataProvider;
 import ch.zhaw.simulation.plugin.SimulationPlugin;
 import ch.zhaw.simulation.plugin.data.SimulationCollection;
 import ch.zhaw.simulation.plugin.matlab.codegen.AbstractCodeGenerator;
 import ch.zhaw.simulation.plugin.matlab.gui.SettingsGui;
 import ch.zhaw.simulation.plugin.matlab.optimizer.FlowModelOptimizer;
 import ch.zhaw.simulation.plugin.matlab.optimizer.ModelOptimizer;
 import ch.zhaw.simulation.plugin.matlab.optimizer.XYModelOptimizer;
 import ch.zhaw.simulation.plugin.matlab.sidebar.MatlabConfigurationSidebar;
 import ch.zhaw.simulation.plugin.sidebar.DefaultConfigurationSidebar;
 
 public class MatlabCompatiblePlugin implements SimulationPlugin {
 	private Settings settings;
 	private MatlabConfigurationSidebar sidebar;
 	private ModelOptimizer optimizer;
 	private SimulationConfiguration config;
 
 	protected PluginDataProvider provider;
 	protected DirectoryWatcher watcher;
 	protected MatlabFinishListener finishListener;
 
 	public MatlabCompatiblePlugin() {
 	}
 
 	@Override
 	public DefaultConfigurationSidebar getConfigurationSidebar() {
 		return sidebar;
 	}
 
 	@Override
 	public void init(Settings settings, SimulationConfiguration config, PluginDataProvider provider) {
 		this.settings = settings;
 		this.config = config;
 		this.provider = provider;
 		this.sidebar = new MatlabConfigurationSidebar(config, provider.getSimulationType());
 		this.watcher = new DirectoryWatcher(1000);
 		this.finishListener = new MatlabFinishListener(this);
 		this.watcher.addResourceListener(this.finishListener);
 	}
 
 	@Override
 	public boolean load() throws Exception {
 		this.sidebar.load();
 		return true;
 	}
 
 	@Override
 	public void unload() {
 		this.sidebar.unload();
 	}
 
 	@Override
 	public JPanel getSettingsPanel() {
 		return new SettingsGui(this.settings, provider.getParent());
 	}
 
 	@Override
 	public void checkDocument(SimulationDocument doc) throws SimulationModelException {
 		if (doc.getType() == SimulationType.FLOW_SIMULATION) {
 			optimizer = new FlowModelOptimizer(doc.getFlowModel());
 		} else if (doc.getType() == SimulationType.XY_MODEL) {
 			optimizer = new XYModelOptimizer(doc.getXyModel());
 		}
 		optimizer.optimize();
 	}
 
 	@Override
 	public void executeFlowSimulation(SimulationDocument doc) throws Exception {
 		String workpath = settings.getSetting(MatlabParameter.WORKPATH, MatlabParameter.DEFAULT_WORKPATH);
 		String filename = null;
 
 		 AbstractCodeGenerator codeGenerator = sidebar.getSelectedNumericMethod().getCodeGenerator();
 		
 		try {
 			finishListener.updateWorkpath(workpath);
 			watcher.start(workpath);
 			provider.getExecutionListener().executionStarted("Simulation läuft...");
 
 			codeGenerator.setWorkingFolder(workpath);
 			codeGenerator.generateSimulation(doc);
 			filename = codeGenerator.getGeneratedFile();
 			startApplication(workpath, filename);
 		} catch (IOException e) {
 			provider.getExecutionListener().executionFinished(e.getMessage(), FinishState.ERROR);
 			throw e;
 		} catch (IllegalArgumentException e) {
 			provider.getExecutionListener().executionFinished(e.getMessage(), FinishState.ERROR);
 			throw e;
		} finally {
			watcher.stop();
 		}
 	}
 
 	@Override
 	public SimulationCollection getSimulationResults(SimulationDocument doc) {
 		return new SimulationResultParser(doc, config).parse(settings.getSetting(MatlabParameter.WORKPATH, MatlabParameter.DEFAULT_WORKPATH));
 	}
 
 	protected void startApplication(String dir, String filename) throws IllegalArgumentException, IOException {
 		String tool = settings.getSetting(MatlabParameter.TOOL, MatlabParameter.DEFAULT_TOOL);
 		MatlabTool t = MatlabTool.fromString(tool);
 		String executable = null;
 		String arguments = null;
 
 		if (t == MatlabTool.MATLAB) {
 			executable = settings.getSetting(MatlabParameter.EXEC_MATLAB_PATH, MatlabParameter.DEFAULT_EXEC_MATLAB_PATH);
 			arguments = "-nosplash -nodesktop -minimize -wait -sd " + dir + " -r " + filename;
 		} else if (t == MatlabTool.OCTAVE) {
 			executable = settings.getSetting(MatlabParameter.EXEC_OCTAVE_PATH, MatlabParameter.DEFAULT_EXEC_OCTAVE_PATH);
 			arguments = "--exec-path " + dir + " " + filename + ".m";
 		} else if (t == MatlabTool.SCILAB) {
 			executable = settings.getSetting(MatlabParameter.EXEC_SCILAB_PATH, MatlabParameter.DEFAULT_EXEC_SCILAB_PATH);
 			arguments = "";
 		} else {
 			throw new IllegalArgumentException();
 		}
 
 		System.out.println(executable + " " + arguments);
 		Process p = Runtime.getRuntime().exec(executable + " " + arguments);
 		//BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
 		//BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
 		//String line;
 		//while ((line = stdout.readLine ()) != null) {
 		//	System.out.println ("stdout: " + line);
 		//}
 		//while ((line = stderr.readLine ()) != null) {
 		//	System.out.println ("stderr: " + line);
 		//}
 	}
 
 	@Override
 	public void cancelSimulation() {
 		provider.getExecutionListener().setExecutionMessage("Abbrechen nicht möglich");
 		// TODO Auto-generated method stub
 		
 	}
 }
