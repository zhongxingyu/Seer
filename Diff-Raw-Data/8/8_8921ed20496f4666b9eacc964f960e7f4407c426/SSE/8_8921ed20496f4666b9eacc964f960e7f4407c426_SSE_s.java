 package li.rudin.rt.servlet.impl;
 
 import java.util.List;
 
 import javax.servlet.ServletOutputStream;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 
 import li.rudin.rt.core.container.ObjectContainer;
 import li.rudin.rt.core.handler.RTHandlerImpl;
 import li.rudin.rt.core.util.JSON;
 import li.rudin.rt.core.util.MessageQueue;
 import li.rudin.rt.servlet.impl.base.BasicEntry;
 
 public class SSE implements BasicEntry
 {
 
 	@Override
 	public void handle(ServletRequest req, ServletResponse resp, RTHandlerImpl handler, MessageQueue queue, int clientId) throws Exception
 	{
 		resp.setContentType("text/event-stream; charset=utf-8");
 		
 		ServletOutputStream output = resp.getOutputStream();
 		
 		//Send init event to prevent readystate from staying "CONNECTING"
 		sendEvent(new ObjectContainer("__init__", null), output);
 		
 		//Clear enqueued events
 		if(!queue.isEmpty())
 		{
 			List<ObjectContainer> data = queue.copyAndClear(handler.getFilter());
 			for (ObjectContainer o: data)
 				sendEvent(o, output);
 		}
 		
 
 		while (true)
 		{
 			synchronized(queue)
 			{
 				queue.wait(30000);
 				
 				//Refresh timeout
 				handler.get(clientId);
 
 				if (!queue.isEmpty())
 				{
 					List<ObjectContainer> data = queue.copyAndClear(handler.getFilter());
 					for (ObjectContainer o: data)
 						sendEvent(o, output);
 						
 				}
 				
 				//No events
 			}
 		}
 	}
 	
 
 	private void sendEvent(ObjectContainer o, ServletOutputStream output) throws Exception
 	{
 		
 		output.print("event: ");
 		output.print(o.type);
 		output.println();
 
 		String json = JSON.toJson(o.data);
 		
 		output.print("data: ");
 		output.print(json);
 		output.println();
 		output.println();
				
		output.flush();
 	}
 
 
 	@Override
 	public String getModeName()
 	{
 		return "sse";
 	}
 
 	@Override
 	public boolean queueNeeded()
 	{
 		return true;
 	}
 
 }
