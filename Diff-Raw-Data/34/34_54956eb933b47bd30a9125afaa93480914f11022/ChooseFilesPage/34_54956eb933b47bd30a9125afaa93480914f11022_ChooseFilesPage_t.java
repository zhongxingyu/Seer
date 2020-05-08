 package se.gustavkarlsson.snap.gui.pages.send.choosefiles;
 
 import net.miginfocom.swt.MigLayout;
 
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.FileTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 
 import se.gustavkarlsson.snap.domain.FolderNode;
 import se.gustavkarlsson.snap.resources.PropertyManager;
 
 public class ChooseFilesPage extends WizardPage {
 
 	private final FolderNode fileTreeRoot = new FolderNode("root");
 
 	private Button enableAdvancedOptionsButton;
 	private TreeViewer fileTreeViewer;
 
 	/**
 	 * Create the wizard.
 	 */
 	public ChooseFilesPage() {
 		super(ChooseFilesPage.class.getName());
 		setTitle("Choose Files");
 		setDescription("Use the \"Browse\" button or drag and drop from your OS.");
 	}
 
 	/**
 	 * Create contents of the wizard.
 	 * 
 	 * @param parent
 	 */
 	@Override
 	public void createControl(Composite parent) {
 		Composite container = new Composite(parent, SWT.NONE);
 		container.setLayout(new MigLayout("", "[grow]", "[grow][]"));
 		setControl(container);
 
 		Group filesGroup = new Group(container, SWT.NONE);
 		filesGroup.setLayoutData("grow, wrap");
 		filesGroup.setText("Files");
 		filesGroup.setLayout(new MigLayout("", "[grow]", "[grow]"));
 
 		fileTreeViewer = new TreeViewer(filesGroup, SWT.BORDER | SWT.MULTI);
 		fileTreeViewer.getTree().setLayoutData("grow");
 		fileTreeViewer.setLabelProvider(new FileTreeLabelProvider());
 		fileTreeViewer.setContentProvider(new FileTreeContentProvider());
 		fileTreeViewer.setComparator(new FileTreeViewerComparator(
 				new FileNameComparator()));
 		fileTreeViewer.setInput(fileTreeRoot);
 		fileTreeViewer.addDragSupport(DND.DROP_MOVE | DND.DROP_COPY,
 				new Transfer[] { InternalFileTransfer.getInstance() },
 				new InternalFileDragListener(fileTreeViewer));
 		fileTreeViewer.addDropSupport(DND.DROP_MOVE | DND.DROP_COPY,
 				new Transfer[] { FileTransfer.getInstance(), InternalFileTransfer.getInstance() },
				new FileTreeDropListener(fileTreeViewer, fileTreeRoot));
 
 		enableAdvancedOptionsButton = new Button(container, SWT.CHECK);
 		enableAdvancedOptionsButton.setText("Enable Advanced Options");
 		enableAdvancedOptionsButton.setSelection(PropertyManager
 				.isUsingAdvancedOptions());
 		enableAdvancedOptionsButton
 				.addSelectionListener(new EnableAdvancedOptionsAdapter());
 		
 		// TODO scroll Tree
 	}
 
 	@Override
 	public boolean isPageComplete() {
 		return fileTreeRoot.hasChildren() && getNextPage() != null;
 	}
 
 	@Override
 	public IWizardPage getNextPage() {
 		if (!enableAdvancedOptionsButton.getSelection()) {
 			return null; // TODO return page
 		}
 		return super.getNextPage();
 	}
 
 	private class EnableAdvancedOptionsAdapter extends SelectionAdapter {
 		@Override
 		public void widgetSelected(SelectionEvent event) {
 			PropertyManager.setUsingAdvancedOptions(enableAdvancedOptionsButton
 					.getSelection());
 			getWizard().getContainer().updateButtons();
 		}
 	}
 }
