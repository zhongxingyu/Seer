 package de.objectcode.time4u.client.connection.ui.dialogs;
 
 import java.util.HashMap;
 
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.window.IShellProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.graphics.FontMetrics;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.swt.widgets.Text;
 
 import de.objectcode.time4u.server.api.data.ServerConnection;
 
 public class ConnectionDialog extends Dialog
 {
   Text m_nameText;
   Text m_urlText;
   Text m_userIdText;
   Text m_passwordText;
   Text m_passwordConfirmText;
   Spinner m_synchronizeItervalSpinner;
 
   ServerConnection m_serverConnection;
   boolean m_create;
 
   public ConnectionDialog(final IShellProvider shellProvider)
   {
     this(shellProvider, null);
   }
 
   public ConnectionDialog(final IShellProvider shellProvider, final ServerConnection serverConnection)
   {
     super(shellProvider);
 
     setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE | getDefaultOrientation());
 
     if (serverConnection == null) {
       m_serverConnection = new ServerConnection();
       m_serverConnection.setName("");
       m_serverConnection.setUrl("");
       m_serverConnection.setRootProjectId(null);
       m_serverConnection.setCredentials(new HashMap<String, String>());
       m_serverConnection.setSynchronizeInterval(5 * 60);
     } else {
       m_serverConnection = serverConnection;
       m_create = false;
     }
   }
 
   public ServerConnection getServerConnection()
   {
     return m_serverConnection;
   }
 
   @Override
   protected void configureShell(final Shell newShell)
   {
     super.configureShell(newShell);
 
     if (m_create) {
       newShell.setText("New Server Connection");
     } else {
       newShell.setText("Edit Server Connection");
     }
   }
 
   @Override
   protected Control createDialogArea(final Composite parent)
   {
     final GC gc = new GC(parent);
     final FontMetrics fm = gc.getFontMetrics();
     final int width = fm.getAverageCharWidth();
     gc.dispose();
 
     final Composite composite = (Composite) super.createDialogArea(parent);
     final Composite root = new Composite(composite, SWT.NONE);
     root.setLayout(new GridLayout(2, false));
     root.setLayoutData(new GridData(GridData.FILL_BOTH));
 
     final Label nameLabel = new Label(root, SWT.NONE);
     nameLabel.setText("Name");
     m_nameText = new Text(root, SWT.BORDER);
     m_nameText.setText(m_serverConnection.getName());
     m_nameText.setTextLimit(50);
     final GridData nameGridData = new GridData(GridData.FILL_HORIZONTAL);
     nameGridData.widthHint = 50 * width;
     m_nameText.setLayoutData(nameGridData);
 
     final Label urlLabel = new Label(root, SWT.NONE);
     urlLabel.setText("URL");
     m_urlText = new Text(root, SWT.BORDER);
     m_urlText.setText(m_serverConnection.getUrl());
     m_urlText.setTextLimit(200);
     final GridData urlGridData = new GridData(GridData.FILL_HORIZONTAL);
     urlGridData.widthHint = 50 * width;
     m_urlText.setLayoutData(urlGridData);
 
     m_urlText.addKeyListener(new KeyAdapter() {
       @Override
       public void keyReleased(final KeyEvent e)
       {
         enableOkButton();
       }
     });
 
     final Label userIdLabel = new Label(root, SWT.NONE);
     userIdLabel.setText("User");
     m_userIdText = new Text(root, SWT.BORDER);
     m_userIdText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
     m_userIdText.setTextLimit(30);
     m_userIdText.setText(m_serverConnection.getCredentials().get("userId") != null ? m_serverConnection
         .getCredentials().get("userId") : "");
     m_userIdText.addKeyListener(new KeyAdapter() {
       @Override
       public void keyReleased(final KeyEvent e)
       {
         enableOkButton();
       }
     });
 
     final Label passwordLabel = new Label(root, SWT.NONE);
     passwordLabel.setText("Password");
     m_passwordText = new Text(root, SWT.BORDER | SWT.PASSWORD);
     m_passwordText.setTextLimit(30);
     m_passwordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
     m_passwordText.addKeyListener(new KeyAdapter() {
       @Override
       public void keyReleased(final KeyEvent e)
       {
         enableOkButton();
       }
     });
 
     final Label passwordConfirmLabel = new Label(root, SWT.NONE);
     passwordConfirmLabel.setText("Password Confirm");
     m_passwordConfirmText = new Text(root, SWT.BORDER | SWT.PASSWORD);
     m_passwordConfirmText.setTextLimit(30);
     m_passwordConfirmText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
     m_passwordConfirmText.addKeyListener(new KeyAdapter() {
       @Override
       public void keyReleased(final KeyEvent e)
       {
         enableOkButton();
       }
     });
 
     final Label synchronizeEveryConfirmLabel = new Label(root, SWT.NONE);
     synchronizeEveryConfirmLabel.setText("Synchronize interval");
     m_synchronizeItervalSpinner = new Spinner(root, SWT.BORDER);
     m_synchronizeItervalSpinner.setIncrement(30);
     m_synchronizeItervalSpinner.setValues(m_serverConnection.getSynchronizeInterval() / 60, 0, 120, 0, 1, 5);
 
     return composite;
   }
 
   @Override
   protected void okPressed()
   {
     m_serverConnection.setName(m_nameText.getText());
     m_serverConnection.setUrl(m_urlText.getText());
     m_serverConnection.getCredentials().put("userId", m_userIdText.getText());
    if (m_passwordText.getText().length() > 0) {
      m_serverConnection.getCredentials().put("password", m_passwordText.getText());
    }
     m_serverConnection.setSynchronizeInterval(m_synchronizeItervalSpinner.getSelection() * 60);
 
     super.okPressed();
   }
 
   @Override
   protected Control createButtonBar(final Composite parent)
   {
     final Control control = super.createButtonBar(parent);
 
     enableOkButton();
 
     return control;
   }
 
   private void enableOkButton()
   {
     boolean enabled = true;
 
     enabled = enabled && m_urlText.getText() != null && m_urlText.getText().length() > 0;
     enabled = enabled && m_userIdText.getText() != null && m_userIdText.getText().length() > 0;
    enabled = enabled && m_passwordText.getText() != null;
     enabled = enabled && m_passwordText.getText().equals(m_passwordConfirmText.getText());
 
     final Button button = getButton(IDialogConstants.OK_ID);
 
     button.setEnabled(enabled);
   }
 
 }
