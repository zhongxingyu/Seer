 package br.ufmg.dcc.vod.spiderpig.master.walker;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import br.ufmg.dcc.vod.spiderpig.common.config.AbstractConfigurable;
 import br.ufmg.dcc.vod.spiderpig.master.processor.ProcessorActor;
 import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.StopCondition;
 import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
 
 public abstract class AbstractWalker extends AbstractConfigurable<Void> 
 		implements ConfigurableWalker {
 
 	private final StopCondition stopCondition;
 	private final ArrayList<CrawlID> seeds;
 	private ProcessorActor processorActor;
 
 	public AbstractWalker() {
 		this.stopCondition = createStopCondition();
 		this.seeds = new ArrayList<>();
 	}
 	
 	private void dispatch(CrawlID crawlID) {
 		this.processorActor.dispatch(crawlID);
 	}
 	
 	@Override
 	public final void setProcessorActor(ProcessorActor processorActor) {
 		this.processorActor = processorActor;
 	}
 	
 	@Override
 	public final void dispatchNext(CrawlID crawled, List<CrawlID> links) {
 		this.stopCondition.resultReceived();
 		List<CrawlID> toWalk = getToWalkImpl(crawled, links);
 		for (CrawlID id : toWalk) {
 			this.stopCondition.dispatched();
 			dispatch(id);
 		}
 	}
 
 	@Override
 	public final StopCondition getStopCondition() {
 		return this.stopCondition;
 	}
 	
 	@Override
 	public final void errorReceived(CrawlID idWithError) {
 		this.stopCondition.errorReceived();
 	}
 
 	@Override
 	public void workerFailedWithID(CrawlID id) {
 		dispatch(id);
 	}
 	
 	@Override
 	public final void addSeedID(CrawlID seed) {
 		this.seeds.add(seed);
 	}
 	
 	@Override
 	public final void dispatchSeeds() {
 		List<CrawlID> seedDispatch = filterSeeds(this.seeds);
 		for (CrawlID id : seedDispatch) {
 			this.stopCondition.dispatched();
 			dispatch(id);
 		}
 	}
 	
 	protected abstract List<CrawlID> filterSeeds(List<CrawlID> seeds);
 	
 	protected abstract List<CrawlID> getToWalkImpl(CrawlID crawled, 
 			List<CrawlID> links);
 	
 	protected abstract void errorReceivedImpl(CrawlID crawled);
 	
 	protected abstract StopCondition createStopCondition();
 	
 }
