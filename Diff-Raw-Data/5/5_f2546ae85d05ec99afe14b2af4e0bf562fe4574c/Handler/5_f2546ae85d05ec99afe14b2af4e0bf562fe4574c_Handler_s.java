 package com.laboki.eclipse.plugin.fastopen;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 
 import com.laboki.eclipse.plugin.fastopen.events.ShowFastOpenDialogEvent;
 import com.laboki.eclipse.plugin.fastopen.main.EventBus;
import com.laboki.eclipse.plugin.fastopen.task.Task;
 
 public final class Handler extends AbstractHandler {
 
 	public Handler() {}
 
 	@Override
 	public Object
 	execute(final ExecutionEvent arg0) throws ExecutionException {
		new Task() {
 
 			@Override
 			public void
 			execute() {
 				EventBus.post(new ShowFastOpenDialogEvent());
 			}
 		}.start();
 		return null;
 	}
 }
