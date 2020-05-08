 package br.ufmg.dcc.vod.spiderpig.distributed.worker;
 
 import java.util.List;
 
 import br.ufmg.dcc.vod.spiderpig.distributed.nio.service.RemoteMessageSender;
 import br.ufmg.dcc.vod.spiderpig.jobs.WorkerInterested;
 import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
 import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;
 import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.BaseResult;
 import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.BaseResult.Builder;
 
 public class InterestedProxy implements WorkerInterested {
 
 	private final RemoteMessageSender sender;
 	private final ServiceID callBackID;
 
 	public InterestedProxy(ServiceID callBackID, RemoteMessageSender sender) {
 		this.callBackID = callBackID;
 		this.sender = sender;
 	}
 
 	@Override
 	public void crawlDone(CrawlID id, List<CrawlID> toQueue) {
 		Builder builder = BaseResult.newBuilder();
 		builder.setIsError(false);
 		builder.setId(id);
		builder.addAllToQueue(toQueue);
 		
 		sender.send(callBackID, builder.build());
 	}
 	
 	@Override
	public void crawlError(CrawlID id, String cause, boolean workerSuspected) {
 		Builder builder = BaseResult.newBuilder();
 		builder.setIsError(true);
 		builder.setErrorMessage(cause);
 		builder.setId(id);
 		
 		sender.send(callBackID, builder.build());
 	}
 }
