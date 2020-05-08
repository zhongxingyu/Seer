 /*
  * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
  * This cross-platform GIS is developed at French IRSTV institute and is able to
  * manipulate and create vector and raster spatial information. 
  * 
  * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier SIG"
  * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
  * 
  * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
  * 
  * This file is part of OrbisGIS.
  * 
  * OrbisGIS is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
  * 
  * For more information, please consult: <http://www.orbisgis.org/>
  * or contact directly:
  * info_at_ orbisgis.org
  */
 package org.orbisgis.groovy;
 
 import groovy.lang.GroovyShell;
 import java.awt.BorderLayout;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.beans.EventHandler;
 import java.io.IOException;
 import java.io.PrintStream;
 import javax.swing.JComponent;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.KeyStroke;
 import javax.swing.event.CaretListener;
 import javax.swing.event.DocumentListener;
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.codehaus.groovy.control.CompilationFailedException;
 import org.fife.rsta.ac.LanguageSupportFactory;
 import org.fife.rsta.ac.groovy.GroovyLanguageSupport;
 import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
 import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
 import org.fife.ui.rtextarea.RTextScrollPane;
 import org.orbisgis.core.layerModel.MapContext;
 import org.orbisgis.sif.UIFactory;
 import org.orbisgis.sif.components.OpenFilePanel;
 import org.orbisgis.sif.components.SaveFilePanel;
 import org.orbisgis.view.beanshell.ext.BeanShellAction;
 import org.orbisgis.view.components.Log4JOutputStream;
 import org.orbisgis.view.components.actions.ActionCommands;
 import org.orbisgis.view.components.actions.DefaultAction;
 import org.orbisgis.view.components.findReplace.FindReplaceDialog;
 import org.orbisgis.view.docking.DockingPanel;
 import org.orbisgis.view.docking.DockingPanelParameters;
 import org.orbisgis.view.icons.OrbisGISIcon;
 import org.orbisgis.view.map.MapElement;
 import org.orbisgis.view.util.CommentUtil;
 import org.xnap.commons.i18n.I18n;
 import org.xnap.commons.i18n.I18nFactory;
 
 /**
  * Create the groovy console panel
  *
  * @author Erwan Bocher
  */
 public class GroovyConsolePanel extends JPanel implements DockingPanel {
 
     public static final String EDITOR_NAME = "Groovy";
     private static final I18n I18N = I18nFactory.getI18n(GroovyConsolePanel.class);
     private static final Logger LOGGER = Logger.getLogger("gui." + GroovyConsolePanel.class);
     private static final Logger LOGGER_POPUP = Logger.getLogger("gui.popup" + GroovyConsolePanel.class);
     private final Log4JOutputStream infoLogger = new Log4JOutputStream(LOGGER, Level.INFO);
     private DockingPanelParameters parameters = new DockingPanelParameters();
     private ActionCommands actions = new ActionCommands();
     private GroovyLanguageSupport gls;
     private RTextScrollPane centerPanel;
     private static GroovyShell groovyShell = new GroovyShell();
     private RSyntaxTextArea scriptPanel;
     private DefaultAction executeAction;
     private DefaultAction clearAction;
     private DefaultAction saveAction;
     private DefaultAction findAction;
     private DefaultAction commentAction;
     private DefaultAction blockCommentAction;
     private FindReplaceDialog findReplaceDialog;
     private int line = 0;
     private int character = 0;
     private MapElement mapElement;
 
     /**
      * Create the groovy console panel
      */
     public GroovyConsolePanel() {
         setLayout(new BorderLayout());
         add(getCenterPanel(), BorderLayout.CENTER);
         init();
     }
 
     /**
      * Init the groovy panel with all docking parameters and set the necessary
      * properties to the console shell
      */
     private void init() {
         groovyShell.setProperty("out", new PrintStream(infoLogger));
         parameters.setName(EDITOR_NAME);
         parameters.setTitle(I18N.tr("Groovy"));
         parameters.setTitleIcon(OrbisGISIcon.getIcon("page_white_cup"));
         parameters.setDockActions(getActions().getActions());
         // If a map is already loaded fetch it in the EditorManager
         try {
             mapElement = MapElement.fetchFirstMapElement();
         } catch (Exception ex) {
             LOGGER.error(ex.getLocalizedMessage(), ex);
         }
         if (mapElement != null) {
             if (mapElement instanceof MapElement) {
                 mapElement = (MapElement) mapElement;
                 // Update the interpreter object
                 setMapContext(mapElement.getMapContext());
             }
         }
     }
 
     /**
      * Get the action manager.
      *
      * @return ActionCommands instance.
      */
     public ActionCommands getActions() {
         return actions;
     }
 
     /**
      * The main panel to write and execute a groovy script.
      *
      * @return
      */
     private RTextScrollPane getCenterPanel() {
         if (centerPanel == null) {
             initActions();
             LanguageSupportFactory lsf = LanguageSupportFactory.get();
             gls = (GroovyLanguageSupport) lsf.getSupportFor(SyntaxConstants.SYNTAX_STYLE_GROOVY);
             scriptPanel = new RSyntaxTextArea();
             scriptPanel.setLineWrap(true);
             lsf.register(scriptPanel);
             scriptPanel.setSyntaxEditingStyle(RSyntaxTextArea.SYNTAX_STYLE_JAVA);
             scriptPanel.addCaretListener(EventHandler.create(CaretListener.class, this, "onScriptPanelCaretUpdate"));
             scriptPanel.getDocument().addDocumentListener(EventHandler.create(DocumentListener.class, this, "onUserSelectionChange"));
             scriptPanel.clearParsers();
             actions.setAccelerators(scriptPanel);
             // Actions will be set on the scriptPanel PopupMenu
             scriptPanel.getPopupMenu().addSeparator();
             actions.registerContainer(scriptPanel.getPopupMenu());
             centerPanel = new RTextScrollPane(scriptPanel);
             onUserSelectionChange();
 
         }
         return centerPanel;
     }
 
     /**
      * Create actions instances
      *
      * Each action is put in the Popup menu and the tool bar Their shortcuts are
      * registered also in the editor
      */
     private void initActions() {
         //Execute action
         executeAction = new DefaultAction(BeanShellAction.A_EXECUTE, I18N.tr("Execute"),
                 I18N.tr("Execute the groovy script"),
                 OrbisGISIcon.getIcon("execute"),
                 EventHandler.create(ActionListener.class, this, "onExecute"),
                 KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK));
         actions.addAction(executeAction);
 
         //Clear action
         clearAction = new DefaultAction(BeanShellAction.A_CLEAR,
                 I18N.tr("Clear"),
                 I18N.tr("Erase the content of the editor"),
                 OrbisGISIcon.getIcon("erase"),
                 EventHandler.create(ActionListener.class, this, "onClear"),
                 null);
         actions.addAction(clearAction);
 
         //Open action
         actions.addAction(new DefaultAction(BeanShellAction.A_OPEN,
                 I18N.tr("Open"),
                 I18N.tr("Load a file in this editor"),
                 OrbisGISIcon.getIcon("open"),
                 EventHandler.create(ActionListener.class, this, "onOpenFile"),
                 KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK)));
         //Save
         saveAction = new DefaultAction(BeanShellAction.A_SAVE,
                 I18N.tr("Save"),
                 I18N.tr("Save the editor content into a file"),
                 OrbisGISIcon.getIcon("save"),
                 EventHandler.create(ActionListener.class, this, "onSaveFile"),
                 KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
         actions.addAction(saveAction);
         //Find action
         findAction = new DefaultAction(BeanShellAction.A_SEARCH,
                 I18N.tr("Search.."),
                 I18N.tr("Search text in the document"),
                 OrbisGISIcon.getIcon("find"),
                 EventHandler.create(ActionListener.class, this, "openFindReplaceDialog"),
                 KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK)).addStroke(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK));
         actions.addAction(findAction);
 
         // Comment/Uncomment
         commentAction = new DefaultAction(BeanShellAction.A_COMMENT,
                 I18N.tr("(Un)comment"),
                 I18N.tr("(Un)comment the selected text"),
                 null,
                 EventHandler.create(ActionListener.class, this, "onComment"),
                 KeyStroke.getKeyStroke("alt C")).setLogicalGroup("format");
         actions.addAction(commentAction);
 
         // Block Comment/Uncomment
         blockCommentAction = new DefaultAction(BeanShellAction.A_BLOCKCOMMENT,
                 I18N.tr("Block (un)comment"),
                 I18N.tr("Block (un)comment the selected text."),
                 null,
                 EventHandler.create(ActionListener.class, this, "onBlockComment"),
                 KeyStroke.getKeyStroke("alt shift C")).setLogicalGroup("format");
         actions.addAction(blockCommentAction);
     }
 
     /**
      * Clear the content of the console
      */
     public void onClear() {
         if (scriptPanel.getDocument().getLength() != 0) {
             int answer = JOptionPane.showConfirmDialog(this,
                     I18N.tr("Do you want to clear the contents of the console?"),
                     I18N.tr("Clear script"), JOptionPane.YES_NO_OPTION);
             if (answer == JOptionPane.YES_OPTION) {
                 scriptPanel.setText("");
             }
         }
     }
 
     /**
      * Open a dialog that let the user select a file and save the content of the
      * sql editor into this file.
      */
     public void onSaveFile() {
         final SaveFilePanel outfilePanel = new SaveFilePanel(
                 "groovyConsoleOutFile", I18N.tr("Save script"));
         outfilePanel.addFilter("groovy", I18N.tr("Groovy script (*.groovy)"));
         outfilePanel.loadState();
         if (UIFactory.showDialog(outfilePanel)) {
             try {
                 FileUtils.write(outfilePanel.getSelectedFile(), scriptPanel.getText());
             } catch (IOException e1) {
                 LOGGER.error(I18N.tr("Cannot write the script."), e1);
             }
             LOGGER_POPUP.info(I18N.tr("The file has been saved."));
         }
     }
 
     /**
      * Open a dialog that let the user select a file and add or replace the
      * content of the sql editor.
      */
     public void onOpenFile() {
         final OpenFilePanel inFilePanel = new OpenFilePanel("groovyConsoleInFile",
                 I18N.tr("Open script"));
         inFilePanel.addFilter("groovy", I18N.tr("Groovy Script (*.groovy)"));
         inFilePanel.loadState();
         if (UIFactory.showDialog(inFilePanel)) {
             int answer = JOptionPane.NO_OPTION;
             if (scriptPanel.getDocument().getLength() > 0) {
                 answer = JOptionPane.showConfirmDialog(
                         this,
                         I18N.tr("Do you want to clear all before loading the file ?"),
                         I18N.tr("Open file"),
                         JOptionPane.YES_NO_CANCEL_OPTION);
             }
             String text;
             try {
                 text = FileUtils.readFileToString(inFilePanel.getSelectedFile());
             } catch (IOException e1) {
                 LOGGER.error(I18N.tr("Cannot write the script."), e1);
                 return;
             }
 
             if (answer == JOptionPane.YES_OPTION) {
                 scriptPanel.setText(text);
             } else if (answer == JOptionPane.NO_OPTION) {
                 scriptPanel.append(text);
             }
         }
     }
 
     /**
      * Update the row:column label
      */
     public void onScriptPanelCaretUpdate() {
         line = scriptPanel.getCaretLineNumber() + 1;
         character = scriptPanel.getCaretOffsetFromLineStart();
     }
 
     /**
      * Change the status of the button when the console is empty or not.
      */
     public void onUserSelectionChange() {
         String text = scriptPanel.getText().trim();
         if (text.isEmpty()) {
             executeAction.setEnabled(false);
             clearAction.setEnabled(false);
             saveAction.setEnabled(false);
             findAction.setEnabled(false);
            commentAction.setEnabled(false);
            blockCommentAction.setEnabled(false);
         } else {
             executeAction.setEnabled(true);
             clearAction.setEnabled(true);
             saveAction.setEnabled(true);
             findAction.setEnabled(true);
            commentAction.setEnabled(true);
            blockCommentAction.setEnabled(true);
         }
     }
 
     /**
      * User click on execute script button
      */
     public void onExecute() {
         String text = scriptPanel.getText().trim();
         try {
             groovyShell.evaluate(text);
             infoLogger.flush();
         } catch (CompilationFailedException e) {
             LOGGER_POPUP.error(I18N.tr("Cannot execute the script"), e);
         } catch (IOException e) {
             LOGGER_POPUP.error(I18N.tr("Cannot display the output of the console"), e);
         }
     }
 
     /**
      * Returns the GroovyShell interpreter
      *
      * @return
      */
     public GroovyShell getGroovyShell() {
         return groovyShell;
     }
 
     /**
      * Expose the map context in the groovy interpreter
      *
      * @param mc MapContext instance
      */
     public void setMapContext(MapContext mc) {
         try {
             groovyShell.setVariable("mc", mc);
         } catch (Error ex) {
             LOGGER.error(ex.getLocalizedMessage(), ex);
         }
     }
 
     /**
      * Dispose
      */
     public void freeResources() {
         if (gls != null) {
             gls.uninstall(scriptPanel);
         }
     }
 
     @Override
     public DockingPanelParameters getDockingParameters() {
         return parameters;
     }
 
     @Override
     public JComponent getComponent() {
         return this;
     }
 
     /**
      * (Un)comment the selected text.
      */
     public void onComment() {
         CommentUtil.commentOrUncommentJava(scriptPanel);
     }
 
     /**
      * Block (un)comment the selected text.
      */
     public void onBlockComment() {
         CommentUtil.blockCommentOrUncomment(scriptPanel);
     }
 
     /**
      * Open one instanceof the find replace dialog
      */
     public void openFindReplaceDialog() {
         if (findReplaceDialog == null) {
             findReplaceDialog = new FindReplaceDialog(scriptPanel, UIFactory.getMainFrame());
         }
         findReplaceDialog.setAlwaysOnTop(true);
         findReplaceDialog.setVisible(true);
     }
 }
