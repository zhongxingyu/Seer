 package pg13.presentation;
 
 import java.util.Date;
 
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.wb.swt.SWTResourceManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.events.VerifyListener;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import pg13.models.Cryptogram;
 
 public class CryptogramEditWidget extends Composite 
 {
 	private Text txtPlaintext;					// plaintext used to generate cryptogram
 	private CryptogramSolveWidget cmpPreview;	// preview area for the cryptogram
 	private Cryptogram workingCryptogram;
 
 	/**
 	 * Creates and populates the cryptogram edit widget.
 	 * @author Eric
 	 * @param parent
 	 * @param style
 	 * @date May 29 2013
 	 */
 	public CryptogramEditWidget(Composite parent, int style, Cryptogram workingCryptogram) 
 	{
 		super(parent, style);
 		setLayout(new FormLayout());
 		this.workingCryptogram = workingCryptogram;
 		
 		// cryptogram preview widget
 		cmpPreview = new CryptogramSolveWidget(this, SWT.BORDER, workingCryptogram);
 		FormData fd_cmpPreview = new FormData();
 		fd_cmpPreview.bottom = new FormAttachment(100, -10);
 		fd_cmpPreview.right = new FormAttachment(100, -10);
 		fd_cmpPreview.top = new FormAttachment(0, 136);
 		fd_cmpPreview.left = new FormAttachment(0, 10);
 		cmpPreview.setLayoutData(fd_cmpPreview);
 		
 		Label lblPlaintext = new Label(this, SWT.NONE);
 		FormData fd_lblPlaintext = new FormData();
 		fd_lblPlaintext.top = new FormAttachment(0, 10);
 		fd_lblPlaintext.left = new FormAttachment(0, 10);
 		lblPlaintext.setLayoutData(fd_lblPlaintext);
 		lblPlaintext.setText("Cryptogram Plaintext");
 		
 		Label lblPreview = new Label(this, SWT.NONE);
 		FormData fd_lblPreview = new FormData();
 		fd_lblPreview.bottom = new FormAttachment(cmpPreview, -6);
 		fd_lblPreview.left = new FormAttachment(0, 10);
 		lblPreview.setLayoutData(fd_lblPreview);
 		lblPreview.setText("Preview");
 		
 		txtPlaintext = new Text(this, SWT.BORDER);
 		txtPlaintext.addVerifyListener(new VerifyListener() 
 		{
 			public void verifyText(VerifyEvent event) 
 			{
 				char[] enteredText;				// text that was entered
 				boolean textIsValid = false;	// text that was entered is OK
 				char curr;						// current character being validated
 				
 				// allow editing keys
 				switch (event.keyCode) 
 				{  
 	            	case SWT.BS:           // Backspace  
 	            	case SWT.DEL:          // Delete  
 	            	case SWT.HOME:         // Home  
 	            	case SWT.END:          // End  
 	            	case SWT.ARROW_LEFT:   // Left arrow  
 	            	case SWT.ARROW_RIGHT:  // Right arrow  
 	                textIsValid = true;  
 				}  
 				
 				if (!textIsValid)
 				{
 					// assume input is valid until proven otherwise
 					textIsValid = true;
 					
 					// restrict the text that can be entered to avoid strange characters
 					enteredText = event.text.toCharArray();
 					for (int i = 0; i < enteredText.length; i++)
 					{
 						curr = enteredText[i];
 						if ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890.,:;- !?()&#$%'\"".indexOf(curr) < 0)
 						{
 							textIsValid = false;
 						}
 					}
 				}
 				
 				// block the event if text is not valid
 				if (!textIsValid)
 				{
 					event.doit = false;
 				}
 			}
 		});
 		txtPlaintext.setText("The quick brown fox jumps over the lazy dog.");
 		FormData fd_txtPlaintext = new FormData();
 		fd_txtPlaintext.right = new FormAttachment(100, -10);
 		fd_txtPlaintext.bottom = new FormAttachment(lblPreview, -6);
 		fd_txtPlaintext.top = new FormAttachment(lblPlaintext, 6);
 		fd_txtPlaintext.left = new FormAttachment(cmpPreview, 0, SWT.LEFT);
 		txtPlaintext.setLayoutData(fd_txtPlaintext);
 		
 		Button btnPreview = new Button(this, SWT.NONE);
 		btnPreview.addSelectionListener(new SelectionAdapter() 
 		{
 			@Override
 			public void widgetSelected(SelectionEvent e) 
 			{
 				preview();
 			}
 		});
 		FormData fd_btnPreview = new FormData();
 		fd_btnPreview.top = new FormAttachment(lblPreview, -5, SWT.TOP);
 		fd_btnPreview.right = new FormAttachment(100, -10);
 		btnPreview.setLayoutData(fd_btnPreview);
 		btnPreview.setText("Generate Preview");
 
 	}
 
 	/*
 	 * Previews the cryptogram -- displays the cryptogram in the preview screen
 	 * @author Eric
 	 * @date May 29 2013
 	 */
 	private void preview()
 	{
		// TODO make this reset user mapping as well
		workingCryptogram.setPlaintext(txtPlaintext.getText());
 		cmpPreview.displayCryptogram(workingCryptogram.getCiphertext());
 	}
 	
 	@Override
 	protected void checkSubclass() 
 	{
 		// Disable the check that prevents subclassing of SWT components
 	}
 }
