 package cz.muni.fi.xtovarn.heimdall.localserver;
 
 import com.google.inject.Inject;
 import cz.muni.fi.xtovarn.heimdall.guice.Startable;
 import cz.muni.fi.xtovarn.heimdall.pipeline.PipelineFactory;
 
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 public class Resender implements Startable {
 
	private final ExecutorService childExecutor = Executors.newFixedThreadPool(2);
 	private final PipelineFactory pipelineFactory;
 
 	@Inject
 	public Resender(PipelineFactory pipelineFactory) {
 		this.pipelineFactory = pipelineFactory;
 	}
 
 	@Override
 	public void start() {
 
 	}
 
 	public void pipeline(String json) {
 		childExecutor.submit(pipelineFactory.getPipeline(json));
 	}
 
 	public void stop() {
 		System.out.println("Closing " + this.getClass() + "...");
 
 		childExecutor.shutdown();
 		try {
 			childExecutor.awaitTermination(5, TimeUnit.SECONDS);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 }
