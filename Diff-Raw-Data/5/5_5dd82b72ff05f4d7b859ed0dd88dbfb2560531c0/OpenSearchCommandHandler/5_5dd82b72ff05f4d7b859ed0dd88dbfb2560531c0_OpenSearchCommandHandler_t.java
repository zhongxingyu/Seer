 
 package edu.ysu.onionuml.ui.handler;
 
 import edu.ysu.onionuml.ui.SearchDialog;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.IHandler;
 import org.eclipse.core.commands.IHandlerListener;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 
 public class OpenSearchCommandHandler implements IHandler {
 	@Override
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		Shell shell = new Shell(Display.getDefault(), SWT.ON_TOP ); 
		/*TODO: bug; the shell is on top of all application
		 * replace Shell(Display.getDefault(), SWT.ON_TOP ) by Shell(parent, SWT.ON_TOP )
		 * My guess is "parent" display is probably in OpenClassModelCommandHandler 
		 */
 		(new SearchDialog(shell)).open();
 
 		return null;
 	}
 
 	@Override
 	public boolean isEnabled() {
 		return true;
 	}
 
 	@Override
 	public boolean isHandled() {
 		return true;
 	}
 
 	@Override
 	public void addHandlerListener(IHandlerListener handlerListener) {
 	}
 
 	@Override
 	public void removeHandlerListener(IHandlerListener handlerListener) {
 	}
 
 	@Override
 	public void dispose() {
 	}
 }
