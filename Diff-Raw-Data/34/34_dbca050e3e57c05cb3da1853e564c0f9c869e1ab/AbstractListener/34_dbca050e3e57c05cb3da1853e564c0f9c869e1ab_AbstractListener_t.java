 package com.laboki.eclipse.plugin.jcolon.listeners.abstraction;
 
 import com.laboki.eclipse.plugin.jcolon.inserter.EditorContext;
 import com.laboki.eclipse.plugin.jcolon.inserter.EventBus;
 import com.laboki.eclipse.plugin.jcolon.instance.AbstractEventBusInstance;
 import com.laboki.eclipse.plugin.jcolon.instance.Instance;
 import com.laboki.eclipse.plugin.jcolon.task.Task;
 
 public abstract class AbstractListener extends AbstractEventBusInstance implements IListener {
 
 	public AbstractListener(final EventBus eventbus) {
 		super(eventbus);
 	}
 
 	@Override
 	public Instance begin() {
 		this.tryToAdd();
 		return super.begin();
 	}
 
 	private void tryToAdd() {
 		try {
 			this.add();
 		} catch (final Exception e) {}
 	}
 
 	@Override
 	public void add() {}
 
 	@Override
 	public Instance end() {
 		this.tryToRemove();
 		return super.end();
 	}
 
 	private void tryToRemove() {
 		try {
 			this.remove();
 		} catch (final Exception e) {}
 	}
 
 	@Override
 	public void remove() {}
 
 	protected void scheduleErrorChecking() {
		new Task(EditorContext.LISTENER_TASK, 1000) {

			@Override
			public boolean shouldSchedule() {
				return EditorContext.shouldSchedule(EditorContext.LISTENER_TASK);
			}

			@Override
			public boolean shouldRun() {
				return EditorContext.shouldSchedule(EditorContext.ERROR_CHECKING_TASK);
			}
 
 			@Override
 			public void execute() {
 				EditorContext.scheduleErrorChecking(AbstractListener.this.eventBus);
 			}
 		}.begin();
 	}
 }
