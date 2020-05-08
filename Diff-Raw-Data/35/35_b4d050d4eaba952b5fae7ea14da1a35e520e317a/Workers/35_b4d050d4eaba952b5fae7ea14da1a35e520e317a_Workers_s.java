 package nz.ac.vuw.ecs.rprofs.server;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 import javax.inject.Singleton;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import nz.ac.vuw.ecs.rprofs.Context;
 import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
 import nz.ac.vuw.ecs.rprofs.server.data.RequestManager;
 import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
 import nz.ac.vuw.ecs.rprofs.server.domain.id.RequestId;
 import org.eclipse.jetty.continuation.Continuation;
 import org.eclipse.jetty.continuation.ContinuationSupport;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Author: Stephen Nelson <stephen@sfnelson.org>
  * Date: 28/11/11
  */
 @Singleton
 public class Workers extends HttpServlet {
 
 	private final Logger log = LoggerFactory.getLogger(Workers.class);
 
 	private BlockingQueue<Continuation> workers = new ArrayBlockingQueue<Continuation>(40);
 
 	private DatasetManager datasets;
 	private RequestManager requests;
 
 	@Inject
 	Workers(DatasetManager datasets, RequestManager requests) {
 		this.datasets = datasets;
 		this.requests = requests;
 	}
 
 	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		final Continuation continuation = ContinuationSupport.getContinuation(req);
 		byte[] data = (byte[]) req.getAttribute("Data");
 		RequestId request = (RequestId) req.getAttribute("RequestId");
 		String dataset = (String) req.getAttribute("Dataset");
 
 		if (data != null) {
 			// good to go
 			resp.addHeader("Dataset", dataset);
 			resp.addHeader("RequestId", String.valueOf(request.getValue()));
 			resp.setContentType("application/rprof");
 			resp.setContentLength(data.length);
 			resp.setStatus(HttpServletResponse.SC_OK);
 			resp.getOutputStream().write(data, 0, data.length);
 			resp.getOutputStream().close();
 			return;
 		}
 
		if (req.getAttribute("init") == null) {
 			// first time through
 			boolean hasCache = req.getHeader("HasCache") != null;
 			String requestId = req.getHeader("RequestId");
 			dataset = req.getHeader("Dataset");
 
 			if (requestId != null) {
 				Dataset ds = datasets.findDataset(dataset);
 				Context.setDataset(ds);
 
 				request = new RequestId(Long.valueOf(requestId));
 				// first time here
 				if (hasCache) {
 					// keep the request id
 					req.setAttribute("RequestId", request);
 					req.setAttribute("Dataset", dataset);
 				} else {
 					// no cache, so release the request
 					requests.releaseRequest(request);
 					request = null;
 				}
 
 				Context.clear();
 			}

			req.setAttribute("init", true);
 		}
 
 		if (continuation.isExpired()) {
			returnNoContent(resp);
 			workers.remove(continuation);
			if (request != null) {
				// If the worker gets a bad response it flushes data
				log.warn("worker disconnected, abandoning request {}", request.getValue());
				Dataset ds = datasets.findDataset((String) req.getAttribute("Dataset"));
				Context.setDataset(ds);
				requests.releaseRequest(request);
				Context.clear();
			}
 			return;
 		}
 
 		if (request != null) {
 			Dataset ds = datasets.findDataset(dataset);
 			Context.setDataset(ds);
 
 			// check still running
 			if (ds.getStopped() != null) {
 				resp.addHeader("Dataset", dataset);
 				resp.addHeader("RequestId", String.valueOf(request.getValue()));
 				resp.addHeader("Flush", "true");
 				returnNoContent(resp);
				workers.remove(continuation);
 				return;
 			}
 
 			Context.clear();
 		}
 
 		if (!workers.contains(continuation)) {
 			workers.offer(continuation);
 		}

		continuation.suspend();
 	}
 
 	protected Continuation getWorker() throws InterruptedException {
 		return workers.take();
 	}
 
 	protected void flush() {
 		List<Continuation> toFlush = Lists.newArrayList();
 		workers.drainTo(toFlush);
 		for (Continuation c : toFlush) {
 			c.resume();
 		}
 	}
 
 	private void returnNoContent(HttpServletResponse resp) throws IOException {
 		resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
 		resp.setContentLength(0);
 		resp.getOutputStream().close();
 	}
 }
