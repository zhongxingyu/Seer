 package gov.va.med.iss.meditor.utils;
 
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 
 import gov.va.med.iss.connection.utilities.ConnectionUtilities;
 import gov.va.med.iss.meditor.MEditorPlugin;
 import gov.va.med.iss.meditor.preferences.MEditorPrefs;
 import gov.va.med.iss.meditor.utils.RoutineCompare;
 import gov.va.med.iss.connection.actions.VistaConnection;
 import gov.va.med.iss.connection.utilities.MPiece;
 
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Dialog;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Control;
 
 public class RoutineChangedDialog extends Dialog {
 
 	private Label lblQuestion;
 	private Button btnOK;
 	private Button btnCancel;
 	private Button btnView;
 	private Button chkSaveCopy;
 	private RoutineChangedDialogData result;
 	private String textString;
 	private String routineName;
 	private boolean isSaveValue;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 
 	}
 	public RoutineChangedDialog(Shell parent, int style) {
 		super(parent, style);
 	}
 	
 	public RoutineChangedDialog(Shell parent) {
 		this(parent, 0);
 	}
 	
 	String file1;
 	String file2;
 	
 	public RoutineChangedDialogData open(String rouName, String infile1, String infile2, boolean isSave, boolean isMult) {
 		result = new RoutineChangedDialogData();
 		routineName = rouName;
 		isSaveValue = isSave;
 		
 		final Shell shell = new Shell(getParent(), SWT.DIALOG_TRIM |
				SWT.APPLICATION_MODAL);
 		String strVal = MPiece.getPiece(VistaConnection.getCurrentServer(),";");
 		strVal = "Routine "+routineName+" Changed on Server '"+strVal+"'";
 		String strProject = MPiece.getPiece(VistaConnection.getCurrentServer(),";",4);
 		if (! (strProject.compareTo("") == 0)) {
 			strVal = strVal + " (Project "+strProject+")";
 		}
 		//shell.setText("Routine "+routineName+" Changed on Server "+MPiece.getPiece(VistaConnection.getCurrentServer(),";"));
 		shell.setText(strVal);
 		shell.setSize(400,200);
 		
 		file1 = infile1;
 		file2 = infile2;
 		
 		lblQuestion = new Label(shell, SWT.NONE);
 		lblQuestion.setLocation(20,10);
 		lblQuestion.setSize(350,20);
 		lblQuestion.setText("The version on the server has changed.  Do you still want to save");
 		if (! isSave) {
 			lblQuestion.setText("The version on the server has changed.  Do you still want to load");
 		}
 
 		Label lblQ2 = new Label(shell, SWT.NONE);
 		lblQ2.setLocation(20,30);
 		lblQ2.setSize(350,20);
		lblQ2.setText("this version on the server? (note that the changes HAVE");
 		
 		String projectName = VistaConnection.getCurrentProject();
 		if (projectName.compareTo("") == 0) {
 			projectName = MEditorPrefs.getPrefs(MEditorPlugin.P_PROJECT_NAME);
 		}
 		
 		Label lblQ4 = new Label(shell, SWT.NONE);
 		lblQ4.setLocation(20,50);
 		lblQ4.setSize(350,20);
 		lblQ4.setText("been saved in the "+projectName+" directory and you may open that");
 		if (! isSave) {
 			lblQ2.setText("this version from the server?");
 			lblQ4.setText("");
 		}
 		if (isMult) {
 			lblQ2.setText("this version on the Server?");
 			lblQ4.setText("");
 		}
 				
 		Label lblQ5 = new Label(shell, SWT.NONE);
 		lblQ5.setLocation(20,70);
 		lblQ5.setSize(350,20);
 		lblQ5.setText("file later (not loading from the server), edit it and then save to ");
 		
 		Label lblQ7 = new Label(shell, SWT.NONE);
 		lblQ7.setLocation(20,90);
 		lblQ7.setSize(350,20);
 		lblQ7.setText("the server.)");
 		if ((!isSave)|| isMult) {
 			lblQ5.setText("");
 			lblQ7.setText("");
 		}
 		
 		
 		chkSaveCopy = new Button(shell, SWT.CHECK);
 		chkSaveCopy.setText("Check to Save a Copy");
 		chkSaveCopy.setLocation(20,110);
 		chkSaveCopy.setSize(300,20);
 		if (!isSave) {  // don't show check box if loading from server
 			chkSaveCopy.setVisible(false);
 			chkSaveCopy.setSelection(false);
 		}
 		
 		
 		btnOK = new Button(shell, SWT.PUSH);
 		btnOK.setText("&OK");
 		btnOK.setLocation(20,130);
 		btnOK.setSize(55,25);
 		shell.setDefaultButton(btnOK);
 		
 		btnCancel = new Button(shell, SWT.PUSH);
 		btnCancel.setText("&Cancel");
 		btnCancel.setLocation(320,130);
 		btnCancel.setSize(55,25);
 		
 		btnView = new Button(shell, SWT.PUSH);
 		btnView.setText("&View");
 		btnView.setLocation(168,130);
 		btnView.setSize(55,25);
 		if (isMult && isSave) {
 			textString = "the version being saved to the server";
 		}
 		else {
 			textString = "the PREVIOUS version loaded from the server";
 		}
 		
 		
 		Listener listener = new Listener() {
 			public void handleEvent(Event event) {
 				if (event.widget == btnView) {
 					try {
 						RoutineCompare.compareRoutines(file1,file2,textString,routineName,isSaveValue);
 					} catch (Exception e) {
 						MessageDialog.openInformation(
 								MEditorUtilities.getIWorkbenchWindow().getShell(),
 								"M-Editor Plug-in",
 								"Error encountered while comparing versions on server "+e.getMessage());
 					}
 				}
 				if (event.widget == btnOK) {
 					result.setReturnValue(true);
 					result.setSaveServerRoutine(chkSaveCopy.getSelection());
 					shell.setVisible(false);
 					shell.close();
 				}
 				if (event.widget == btnCancel) {
 					result.setReturnValue(false);
 					result.setSaveServerRoutine(false);
 					shell.setVisible(false);
 					shell.close();
 				}
 			}
 		};
 		
 		btnOK.addListener(SWT.Selection, listener);
 		btnCancel.addListener(SWT.Selection, listener);
 		btnView.addListener(SWT.Selection, listener);
 		
 		shell.open();
 		Display display = getParent().getDisplay();
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) display.sleep();
 		}
 		
 		return result;
 	}
 
 
 }
