 package com.laboki.eclipse.plugin.jcolon.main;
 
 import com.google.common.eventbus.AllowConcurrentEvents;
 import com.google.common.eventbus.Subscribe;
 import com.laboki.eclipse.plugin.jcolon.events.AssistSessionEndedEvent;
 import com.laboki.eclipse.plugin.jcolon.events.AssistSessionStartedEvent;
 import com.laboki.eclipse.plugin.jcolon.events.CheckErrorEvent;
 import com.laboki.eclipse.plugin.jcolon.events.ScheduleCheckErrorEvent;
 import com.laboki.eclipse.plugin.jcolon.instance.EventBusInstance;
 import com.laboki.eclipse.plugin.jcolon.instance.Instance;
 import com.laboki.eclipse.plugin.jcolon.task.BaseTask;
 import com.laboki.eclipse.plugin.jcolon.task.Task;
 import com.laboki.eclipse.plugin.jcolon.task.TaskMutexRule;
 
 public final class Scheduler extends EventBusInstance {
 
 	static final String FAMILY = "JCOLON_SCHEDULER_FAMILY";
 	static final TaskMutexRule RULE = new TaskMutexRule();
 	protected boolean completionAssistantIsActive;
 
 	public Scheduler() {
 		super();
 	}
 
 	@Subscribe
 	@AllowConcurrentEvents
 	public void
 	eventHandler(final ScheduleCheckErrorEvent event) {
 		new Task() {
 
 			@Override
 			public boolean
 			shouldSchedule() {
 				if (Scheduler.this.completionAssistantIsActive) return false;
 				return BaseTask.noTaskFamilyExists(Scheduler.FAMILY);
 			}
 
 			@Override
 			public void
 			execute() {
 				EditorContext.cancelErrorCheckingJobs();
 				EventBus.post(new CheckErrorEvent());
 			}
		}.setRule(Scheduler.RULE)
 			.setFamily(Scheduler.FAMILY)
 			.setDelay(EditorContext.SHORT_DELAY)
 			.start();
 	}
 
 	@Subscribe
 	public void
 	eventHandler(final AssistSessionStartedEvent event) {
 		this.completionAssistantIsActive = true;
 		EditorContext.cancelAllJobs();
 	}
 
 	@Subscribe
 	public void
 	eventHandler(final AssistSessionEndedEvent event) {
 		this.completionAssistantIsActive = false;
 	}
 
 	@Override
 	public Instance
 	stop() {
 		EditorContext.cancelAllJobs();
 		return super.stop();
 	}
 }
