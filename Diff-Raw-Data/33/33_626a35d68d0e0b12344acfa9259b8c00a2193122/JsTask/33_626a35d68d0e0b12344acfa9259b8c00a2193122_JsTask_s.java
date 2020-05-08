 package com.theminequest.common.quest.js;
 
 import static com.theminequest.common.util.I18NMessage._;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.logging.Level;
 
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.ContinuationPending;
 import org.mozilla.javascript.EcmaError;
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 
 import com.theminequest.api.CompleteStatus;
 import com.theminequest.api.Managers;
 import com.theminequest.api.platform.entity.MQPlayer;
 import com.theminequest.api.quest.Quest;
 import com.theminequest.api.quest.QuestTask;
 import com.theminequest.api.quest.event.QuestEvent;
 import com.theminequest.common.Common;
 
 // kudos to
 // http://www.joshforde.com/blog/index.php/2011/12/tutorial-continuations-in-mozilla-rhino-a-javascript-interpreter-for-java/
 public class JsTask implements QuestTask {
 	
 	private static volatile ScriptableObject STD_OBJ = null;
 	private static final Object SYNCLOCK = new Object();
 	
 	private JsQuest quest;
 	private Object jsThreadLock;
 	private Thread jsThread;
 	private JsObserver observer;
 	
 	private Scriptable global;
 	private ContinuationPending continuation;
 	
 	private Object statusLock;
 	private CompleteStatus status;
 	private String taskDescription;
 	
 	protected JsTask(JsQuest quest) {
 		this.quest = quest;
 		this.jsThreadLock = new Object();
 		this.jsThread = null;
 		this.observer = new JsObserver();
 		
 		this.global = null;
 		this.continuation = null;

 		this.statusLock = new Object();
 		this.status = null;
 		this.taskDescription = _("No description given - ask the quest maker to use setTaskDescription()!");
 	}
 	
 	@Override
 	public void start() {
 		synchronized (statusLock) {
 			if (status != null)
 				return;
 		}
 		
 		synchronized (jsThreadLock) {
 			if (jsThread != null && jsThread.isAlive())
 				return;
 			
 			jsThread = new Thread(new Runnable() {
 				
 				@Override
 				public void run() {
 					Context cx = Context.enter();
 					
 					try {
 						synchronized (SYNCLOCK) {
 							if (STD_OBJ == null) {
 								STD_OBJ = cx.initStandardObjects(null, true);
 								STD_OBJ.sealObject();
 							}
 						}
 						
 						cx.setDebugger(observer, new Integer(0));
 						cx.setGeneratingDebug(true);
 						cx.setOptimizationLevel(-1);
 						
 						if (global == null) {
 							global = cx.newObject(STD_OBJ);
 							global.setPrototype(STD_OBJ);
 							global.setParentScope(null);
 							
 							ScriptableObject.putConstProperty(global, "details", Context.toObject(quest.getDetails(), global));
 							ScriptableObject.putConstProperty(global, "color", Context.toObject(Managers.getPlatform().chatColor(), global));
 							
 							// bind all the functions in the
 							// ScriptFunctions.java
 							// class into JavaScript as global functions
 							global.put("scriptfunctions", global, new JsQuestGlobalFunctions(JsTask.this, global));
 							cx.evaluateString(global, " for(var fn in scriptfunctions) { if(typeof scriptfunctions[fn] === 'function') {this[fn] = (function() {var method = scriptfunctions[fn];return function() {return method.apply(scriptfunctions,arguments);};})();}};", "function transferrer", 1, null);
 							
 							// pull in scripts we don't have
 							for (String str : Common.getCommon().getJavascriptResources()) {
 								try {
 									cx.evaluateReader(global, new InputStreamReader(Common.class.getResourceAsStream("/" + str)), str, 1, null);
 								} catch (IOException e) {
 									Managers.logf(Level.SEVERE, "[JsResources] IO exception loading %s: %s", str, e.getMessage());
 								} catch (EcmaError err) {
 									Managers.logf(Level.SEVERE, "[JsResources] ECMA error loading %s: %s", str, err.getMessage());
 								}
 							}
 							
 							cx.evaluateString(global, (String) quest.getDetails().getProperty(JsQuestDetails.JS_SOURCE), quest.getDetails().getName(), (int) quest.getDetails().getProperty(JsQuestDetails.JS_LINESTART), null);
 						}
 						
 						// evaluate
 						Function f = (Function) (global.get("main", global));
 						Double result = null;
 						try {
 							if (continuation == null)
 								result = (Double) cx.callFunctionWithContinuations(f, global, new Object[1]);
 							else
 								result = (Double) cx.resumeContinuation(continuation.getContinuation(), global, (Integer) continuation.getApplicationState());
 						} catch (ContinuationPending pending) {
 							// script paused
 							continuation = pending;
 							return;
 						} catch (EcmaError err) {
 							synchronized (statusLock) {
 								status = CompleteStatus.ERROR;
 							}
 							
 							Managers.logf(Level.SEVERE, "[ECMA] In evaluating %s/%s: %s", quest.getQuestOwner(), quest.getDetails().getName(), err.toString());
 							for (MQPlayer player : Managers.getGroupManager().get(getQuest()).getMembers())
 								player.sendMessage(Managers.getPlatform().chatColor().RED() + "Error with the quest: " + err.toString());
 							
 							Managers.getPlatform().scheduleSyncTask(new Runnable() {
 								
 								@Override
 								public void run() {
 									completed();
 								}
 								
 							});
 							return;
 						}
 						
 						synchronized (statusLock) {
 							if (status != null)
 								return;
 						}
 						
 						if (observer.isDisconnected()) {
 							synchronized (statusLock) {
 								status = CompleteStatus.CANCELED;
 							}
 							Managers.getPlatform().scheduleSyncTask(new Runnable() {
 								
 								@Override
 								public void run() {
 									completed();
 								}
 								
 							});
 							return;
 						}
 						
 						synchronized (statusLock) {
 							status = CompleteStatus.ERROR;
 							
							if (result == null || result.equals(0))
 								status = CompleteStatus.SUCCESS;
							else if (result.equals(1))
								status = CompleteStatus.FAIL;
							else if (result.equals(2))
								status = CompleteStatus.WARNING;
							else if (result.equals(-2))
								status = CompleteStatus.IGNORE;
							else if (result.equals(-1))
								status = CompleteStatus.CANCELED;
 						}
 						
 						Managers.getPlatform().scheduleSyncTask(new Runnable() {
 							
 							@Override
 							public void run() {
 								completed();
 							}
 							
 						});
 						
 					} finally {
 						Context.exit();
 					}
 				}
 				
 			});
 			
 			jsThread.start();
 		}
 	}
 	
 	public void unpause() {
 		start();
 	}
 	
 	private void completed() {
 		observer.setDisconnected(true); // just as a precaution, though is it
 										// really needed?
 		synchronized (statusLock) {
 			quest.completeTask(this, status, -1);
 		}
 	}
 	
 	@Override
 	public CompleteStatus isComplete() {
 		synchronized (statusLock) {
 			return status;
 		}
 	}
 	
 	@Override
 	public Quest getQuest() {
 		return quest;
 	}
 	
 	@Override
 	public int getTaskID() {
 		return 0;
 	}
 	
 	@Override
 	public String getTaskDescription() {
 		return taskDescription;
 	}
 	
 	protected void setTaskDescription(String taskDescription) {
 		this.taskDescription = taskDescription;
 	}
 	
 	@Override
 	public Collection<QuestEvent> getEvents() {
 		return new LinkedList<QuestEvent>();
 	}
 	
 	@Override
 	public void checkTasks() {
 		// does nothing
 	}
 	
 	@Override
 	public void cancelTask() {
 		// this will toggle the Js runtime to stop
 		// and will call (as above) completed(CANCELED)
 		observer.setDisconnected(true);
 		synchronized (statusLock) {
 			status = CompleteStatus.CANCELED;
 		}
 		Managers.getPlatform().scheduleSyncTask(new Runnable() {
 			
 			@Override
 			public void run() {
 				completed();
 			}
 			
 		});
 	}
 	
 	@Override
 	public void completeEvent(QuestEvent event, CompleteStatus status) {
 		// will do stuff (resumes task)
 		start();
 	}
 	
 	@Override
 	public void completeEvent(QuestEvent event, CompleteStatus status, int nextTask) {
 		// will do stuff (resumes task)
 		start();
 	}
 	
 }
