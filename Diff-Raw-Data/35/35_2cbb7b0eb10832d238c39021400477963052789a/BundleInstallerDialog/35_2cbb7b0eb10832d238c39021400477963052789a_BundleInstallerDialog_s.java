 package kkckkc.jsourcepad.installer.bundle;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import kkckkc.jsourcepad.Dialog;
 import kkckkc.jsourcepad.model.Application;
 import kkckkc.jsourcepad.model.bundle.BundleManager;
 import kkckkc.jsourcepad.util.Config;
 import kkckkc.jsourcepad.util.io.ScriptExecutor;
 import kkckkc.utils.DomUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.InputSource;
 
 import javax.annotation.PostConstruct;
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.StringReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 public class BundleInstallerDialog implements Dialog<BundleInstallerDialogView> {
     private BundleInstallerDialogView view;
     private List<BundleTableModel.Entry> bundles;
 
     @PostConstruct
     public void init() {
         view.getInstallButton().addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 install();
             }
         });
 
         view.getCancelButton().addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 close();
             }
         });
 
         view.getLabel().setText("Getting list of bundles...");
 
 
 
     }
 
     public void show() {
         final BundleManager bundleManager = Application.get().getBundleManager();
         bundles = Lists.newArrayList();
 
         if (! verifyGit()) {
             JOptionPane.showMessageDialog(null,
                     "Git is not found. Please make sure git is installed (in cygwin if running on windows).",
                     "Git is not found",
                     JOptionPane.ERROR_MESSAGE);
             return;
         }
 
         try {
             new SwingWorker<Void, Object>() {
 
                 @Override
                 protected Void doInBackground() throws Exception {
                     Set<String> preSelectedBundles = Sets.newHashSet();
                     if (bundleManager.getBundles() == null || bundleManager.getBundles().isEmpty()) {
                         preSelectedBundles.addAll(Arrays.asList(
                                 "bundle-development", "c", "css", "html", "java", "javascript", "markdown",
                                 "php", "perl", "python", "ruby", "sql", "shellscript", "source", "text",
                                 "textmate", "xml"));
                     }
 
                     URL url = new URL("http://github.com/api/v2/xml/repos/show/textmate");
                     final URLConnection conn = url.openConnection();
                     conn.connect();
 
                     Document document = DomUtil.parse(new InputSource(conn.getInputStream()));
                     for (Element e : DomUtil.getChildren(document.getDocumentElement())) {
                         Element nameE = DomUtil.getChild(e, "name");
                         String name = DomUtil.getText(nameE);
 
                         if (! name.endsWith(".tmbundle")) continue;
 
                         bundles.add(new BundleTableModel.Entry(
                                 bundleManager.getBundleByDirName(name) != null ||
                                     preSelectedBundles.contains(name.substring(0, name.lastIndexOf("."))),
                                 bundleManager.getBundleByDirName(name) != null, name, DomUtil.getChildText(e, "url")));
                     }
 
                     return null;
                 }
 
                 @Override
                 protected void done() {
                     view.setModel(new BundleTableModel(bundles));
                     view.getLabel().setText("Available bundles:");
 
                     if (bundleManager.getBundles() == null || bundleManager.getBundles().isEmpty()) {
                         JOptionPane.showMessageDialog(view.getJDialog(),
                                 "As this is the first time you are installing bundles, a selection \n" +
                                 "of common bundles have been pre-selected for you");
                     }
                 }
 
             }.execute();
 
         } catch (Exception ioe) {
             throw new RuntimeException(ioe);
         }
 
         view.getJDialog().show();
     }
 
     private boolean verifyGit() {
         try {
 
 
             StatusCallback statusCallback = new StatusCallback();
 
             ScriptExecutor se = new ScriptExecutor("git --version", Application.get().getThreadPool());
             ScriptExecutor.Execution execution = se.execute(statusCallback, new StringReader(""), System.getenv());
             execution.waitForCompletion();
 
             if (! statusCallback.isSuccessful()) {
                 return false;
             }
         } catch (Exception ioe) {
             throw new RuntimeException(ioe);
         }
         
         return true;
     }
 
     @Override
     public void close() {
         view.getJDialog().dispose();
     }
 
     @Override
     @Autowired
     public void setView(BundleInstallerDialogView view) {
         this.view = view;
     }
 
 
     private void install() {
         final Collection<BundleTableModel.Entry> entriesToInstall = Collections2.filter(bundles, new Predicate<BundleTableModel.Entry>() {
             @Override
             public boolean apply(BundleTableModel.Entry entry) {
                 return ! entry.isDisabled() && entry.isSelected();
             }
         });
 
         final ProgressMonitor progressMonitor = new ProgressMonitor(view.getJDialog(),
                                       "Installing bundles",
                                       "", 0, entriesToInstall.size());
         progressMonitor.setMillisToPopup(100);
 
         new SwingWorker<Void, BundleTableModel.Entry>() {
             @Override
             protected Void doInBackground() throws Exception {
                 int i = 0;
 
                 for (BundleTableModel.Entry entry : entriesToInstall) {
                     setProgress(i++);
                     publish(entry);
 
                     installBundle(entry);
 
                     if (progressMonitor.isCanceled()) {
                         return null;
                     }
                 }
 
                 return null;
             }
 
             @Override
             protected void process(List<BundleTableModel.Entry> chunks) {
                 for (BundleTableModel.Entry s : chunks) {
                     progressMonitor.setProgress(getProgress());
                     progressMonitor.setNote(s.getName());
                 }
             }
 
             @Override
             protected void done() {
                 Application.get().getBundleManager().reload();
 
                 progressMonitor.close();
                 close();
             }
 
            private void installBundle(BundleTableModel.Entry entry) {
                 try {
                     // TODO: Show progress
                     StatusCallback statusCallback = new StatusCallback();
 
                    ScriptExecutor se = new ScriptExecutor("git clone " + entry.getUrl() + ".git", Application.get().getThreadPool());
                     se.setDirectory(Config.getBundlesFolder());
                    ScriptExecutor.Execution execution = se.execute(statusCallback, new StringReader(""), System.getenv());
                     execution.waitForCompletion();
                         
                     if (! statusCallback.isSuccessful()) {
                        JOptionPane.showMessageDialog(view.getJDialog(), "Cannot install bundle " + entry.getName(),
                                "Error installing bundle", JOptionPane.ERROR_MESSAGE);
                     }
                 } catch (Exception e) {
                     throw new RuntimeException(e);
                 }
             }
         }.execute();
 
     }
 
    static class StatusCallback extends ScriptExecutor.CallbackAdapter {
         boolean successful = false;
 
         @Override
         public void onFailure(ScriptExecutor.Execution execution) {
             successful = false;
         }
 
         @Override
         public void onSuccess(ScriptExecutor.Execution execution) {
             successful = true;
         }
 
         public boolean isSuccessful() {
             return successful;
         }
     };
 }
