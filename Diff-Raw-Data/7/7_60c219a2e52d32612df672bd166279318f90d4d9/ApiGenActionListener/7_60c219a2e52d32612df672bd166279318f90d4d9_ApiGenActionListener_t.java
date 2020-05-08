 /*
  *  The MIT License
  * 
  *  Copyright 2010 Ondřej Brejla <ondrej@brejla.cz>.
  * 
  *  Permission is hereby granted, free of charge, to any person obtaining a copy
  *  of this software and associated documentation files (the "Software"), to deal
  *  in the Software without restriction, including without limitation the rights
  *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  *  copies of the Software, and to permit persons to whom the Software is
  *  furnished to do so, subject to the following conditions:
  * 
  *  The above copyright notice and this permission notice shall be included in
  *  all copies or substantial portions of the Software.
  * 
  *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  *  THE SOFTWARE.
  */
 
 package org.netbeans.modules.php.apigen.ui.actions;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.net.MalformedURLException;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import org.netbeans.api.extexecution.ExecutionDescriptor;
 import org.netbeans.api.extexecution.ExecutionService;
 import org.netbeans.api.extexecution.ExternalProcessBuilder;
 import org.netbeans.modules.php.api.phpmodule.PhpModule;
 import org.netbeans.modules.php.apigen.actions.GenerateApiAction;
 import org.netbeans.modules.php.apigen.options.ApiGenOptions;
 import org.netbeans.modules.php.apigen.ui.ApiGenPreferences;
 import org.netbeans.modules.php.project.ui.options.PhpOptions;
 import org.openide.DialogDescriptor;
 import org.openide.awt.HtmlBrowser;
 import org.openide.filesystems.FileUtil;
 import org.openide.util.Exceptions;
 import org.openide.util.NbBundle;
 
 /**
  *
  * @author Ondřej Brejla <ondrej@brejla.cz>
  */
 public class ApiGenActionListener implements ActionListener {
 
 	private String apiGenTab = NbBundle.getMessage(GenerateApiAction.class, "LBL_ApiGenTab");
 
 	private ApiGenActionPanel panel;
 
 	private PhpModule phpModule;
 
 	public ApiGenActionListener(ApiGenActionPanel panel, PhpModule phpModule) {
 		this.panel = panel;
 		this.phpModule = phpModule;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == DialogDescriptor.OK_OPTION) {
 			ApiGenPreferences.setApiGenTarget(phpModule, panel.getTargetDirectory());
 			ApiGenPreferences.setApiGenTitle(phpModule, panel.getDocumentationTitle());
			ApiGenPreferences.setApiGenConfig(phpModule, panel.getOutputCfgFile());
 
 			ExecutionDescriptor descriptor = new ExecutionDescriptor().frontWindow(true).controllable(true);
 
 			String path = PhpOptions.getInstance().getPhpInterpreter();
 
 			ExternalProcessBuilder processBuilder = new ExternalProcessBuilder(path)
 					.workingDirectory(FileUtil.toFile(phpModule.getSourceDirectory()))
 					.addArgument(ApiGenOptions.getPath())
 					.addArgument("-s")
 					.addArgument(panel.getSourceDirectory())
 					.addArgument("-d")
 					.addArgument(panel.getTargetDirectory()); // NOI18N
 
 			if (!panel.getOutputCfgFile().trim().isEmpty()) {
				processBuilder = processBuilder.addArgument("-c").addArgument(panel.getOutputCfgFile()); // NOI18N
 			}
 
 			if (!panel.getDocumentationTitle().trim().isEmpty()) {
				processBuilder = processBuilder.addArgument("-t").addArgument(panel.getDocumentationTitle()); // NOI18N
 			}
 
 			final ExecutionService service = ExecutionService.newService(processBuilder, descriptor, apiGenTab);
 
 			new Thread(new Runnable() {
 
 				@Override
 				public void run() {
 					Future<Integer> task = service.run();
 					try {
 						task.get();
 						FileUtil.refreshFor(new File(panel.getTargetDirectory()));
 						File index = new File(panel.getTargetDirectory(), "index.html"); // NOI18N
 						if (index.isFile()) {
 							try {
 								HtmlBrowser.URLDisplayer.getDefault().showURL(index.toURI().toURL());
 							} catch (MalformedURLException ex) {
 								Exceptions.printStackTrace(ex);
 							}
 						}
 					} catch (InterruptedException ex) {
 						Exceptions.printStackTrace(ex);
 					} catch (ExecutionException ex) {
 						Exceptions.printStackTrace(ex);
 					}
 				}
 
 			}).start();
 		}
 	}
 
 }
