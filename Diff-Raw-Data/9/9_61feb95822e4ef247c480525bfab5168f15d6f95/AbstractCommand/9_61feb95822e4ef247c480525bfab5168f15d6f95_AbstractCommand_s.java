 package com.cee.news.server.content;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springbyexample.bean.scope.thread.ThreadScopeRunnable;
 
 public abstract class AbstractCommand implements Command {
 
 	private static final Logger LOG = LoggerFactory.getLogger(AbstractCommand.class);
 	
 	private List<CommandCallback> callbacks = new ArrayList<CommandCallback>();
 	
 	protected abstract void runInternal();
 	
 	@Override
 	public void run() {
 		try {
 			new ThreadScopeRunnable(new Runnable() {
 				@Override
 				public void run() {
 					runInternal();
 				}
 			}).run();
 			LOG.debug("Cleared all thread scoped resources for thread {}", Thread.currentThread().getName());
 			
 		} catch (Exception ex) {
 			fireError(ex);
 		} finally {
 			fireFinished();
 		}
 	}
 	
 	@Override
 	public void addCommandCallback(CommandCallback callback) {
 		callbacks.add(callback);
 	}
 
 	protected void fireFinished() {
 		for (CommandCallback callback : callbacks) {
 			callback.notifyFinished();
 		}
 	}
 	
 	protected void fireError(Exception ex) {
 		for (CommandCallback callback : callbacks) {
 			callback.notifyError(ex);
 		}
 	}
 }
