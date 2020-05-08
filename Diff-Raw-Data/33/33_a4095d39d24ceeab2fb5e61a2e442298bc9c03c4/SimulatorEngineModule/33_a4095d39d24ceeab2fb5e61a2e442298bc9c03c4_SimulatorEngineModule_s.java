 package eu.scape_project.pw.simulator.engine.module;
 
 import com.google.inject.AbstractModule;
 
import eu.scape_project.pw.simulator.engine.container.EventContainer;
import eu.scape_project.pw.simulator.engine.container.EventObserverContainer;
import eu.scape_project.pw.simulator.engine.container.IEventContainer;
import eu.scape_project.pw.simulator.engine.container.IEventObserverContainer;
 import eu.scape_project.pw.simulator.engine.recorder.IRecorder;
 import eu.scape_project.pw.simulator.engine.recorder.Recorder;
 
 public class SimulatorEngineModule extends AbstractModule{
 
 	@Override
 	protected void configure() {
 		bind(IRecorder.class).to(Recorder.class);
 		
 	}
 
 }
