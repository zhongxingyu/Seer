 package pg13.presentation;
 
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.SWT;
 import org.eclipse.wb.swt.SWTResourceManager;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.events.VerifyListener;
 import org.eclipse.swt.events.VerifyEvent;
 import pg13.models.Cryptogram;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.ModifyEvent;
 
 public class CryptogramLetterWidget extends Composite {
 	private Text txtPlaintextChar;
 	private char ciphertextChar;
 	private Cryptogram parentCryptogram;
 	private Composite parent;
 
 	/**
 	 * Creates and populates the letter widget.
 	 * @author Eric
 	 * @date May 29 2013
 	 * @param parent
 	 * @param style
 	 */
 	public CryptogramLetterWidget(Composite parent, int style, Cryptogram parentCryptogram, char ciphertextChar) {
 		super(parent, SWT.NONE);
 		setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		setLayout(new FormLayout());
 		this.ciphertextChar = ciphertextChar;
 		this.parentCryptogram = parentCryptogram;
 		this.parent = parent;
 		
 		// ciphertext character
 		Label lblCiphertextChar = new Label(this, SWT.NONE);
 		lblCiphertextChar.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		FormData fd_lblCiphertextChar = new FormData();
 		fd_lblCiphertextChar.bottom = new FormAttachment(100);
 		fd_lblCiphertextChar.right = new FormAttachment(100);
 		fd_lblCiphertextChar.top = new FormAttachment(50);
 		fd_lblCiphertextChar.left = new FormAttachment(0);
 		lblCiphertextChar.setLayoutData(fd_lblCiphertextChar);
 		lblCiphertextChar.setAlignment(SWT.CENTER);
 		lblCiphertextChar.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.NORMAL));
 		lblCiphertextChar.setText("" + ciphertextChar);
 		
 		// area for the plaintext character entered by user
 		txtPlaintextChar = new Text(this, SWT.BORDER | SWT.CENTER);
 		txtPlaintextChar.addModifyListener(new ModifyListener() 
 		{
 			public void modifyText(ModifyEvent event) 
 			{
 				updateCryptogram();
 			}
 		});
 		txtPlaintextChar.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.NORMAL));
 		//txtPlaintextChar.set
 		FormData fd_txtPlaintextChar = new FormData();
 		fd_txtPlaintextChar.top = new FormAttachment(0);
 		fd_txtPlaintextChar.left = new FormAttachment(0);
 		fd_txtPlaintextChar.right = new FormAttachment(100);
 		fd_txtPlaintextChar.bottom = new FormAttachment(50);
 		txtPlaintextChar.setLayoutData(fd_txtPlaintextChar);
 		
 		// if the ciphertext character is not a letter, then the plaintext character is given
 		if ("ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(ciphertextChar) < 0)
 		{
 			txtPlaintextChar.setEditable(false);
 			txtPlaintextChar.setText("" + ciphertextChar);
 		}
 		else
 		{
 			// limit characters that can be entered
 			txtPlaintextChar.setTextLimit(1);
 			txtPlaintextChar.addVerifyListener(new VerifyListener() 
 			{
 				public void verifyText(VerifyEvent event) 
 				{
 					char enteredChar;				// text that was entered
 					boolean textIsValid = false;	// text that was entered is OK
 					
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
 					
 					// an empty text is allowed
 					if (event.text.length() == 0)
 					{
 						textIsValid = true;
 					}
 					
 					if (!textIsValid)
 					{
 						// set to lowercase
 						event.text = event.text.toLowerCase();
 						
 						enteredChar = event.text.charAt(0);
 						if ("abcdefghijklmnopqrstuvwxyz".indexOf(enteredChar) >= 0)
 							textIsValid = true;
 					}
 					
 					// block the event if text is not valid
 					if (!textIsValid)
 					{
 						event.doit = false;
 					}
 				}
 			});
 		}
 
 	}
 
 	@Override
 	protected void checkSubclass() {
 		// Disable the check that prevents subclassing of SWT components
 	}
 	
 	/*
 	 * Indicates whether or not this widget contains a space
 	 * @author Eric
 	 * @date May 29 2013
 	 * @return true if the widget contains a space, false otherwise
 	 */
 	public boolean isSpace()
 	{
 		return ciphertextChar == ' ';
 	}
 
 	/*
 	 * Updates the values in the cryptogram according to the value stored in this widget
 	 * @author Eric
 	 * @date May 29 2013
 	 */
 	private void updateCryptogram()
 	{
 		char plaintextChar;
 		
		if (txtPlaintextChar.getEditable() == true)
 		{
 			if (txtPlaintextChar.getText().length() > 0)
 			{
 				plaintextChar = txtPlaintextChar.getText().charAt(0);
 			}
 			else
 			{
 				plaintextChar = '\0';
 			}
 			
			// TODO make it possible to set a plaintext character to '\o' or remove a plaintext character from the map?
			if (plaintextChar != '\0')
			{
				this.parentCryptogram.setUserPlaintextForCiphertext(plaintextChar, this.ciphertextChar);
			}
 			
 			if (this.parent instanceof CryptogramSolveWidget)
 			{
 				((CryptogramSolveWidget) this.parent).updateLetterWidgetContents();
 			}
 		}
 	}
 	
 	/*
 	 * Updates the contents of the widget according to the mapping stored in the cryptogram
 	 * @author Eric
 	 * @date May 29 2013
 	 */
 	public void updateContents()
 	{
 		char plaintextChar;
 		
 		if (txtPlaintextChar.getEditable() == true)
 		{
 			plaintextChar = this.parentCryptogram.getUserPlaintextFromCiphertext(ciphertextChar);
 			
			// TODO find a way to make this not recursively call the listenner...
 			if (plaintextChar == '\0')
 			{
				//this.txtPlaintextChar.setText("");
 			}
 			else
 			{
				//this.txtPlaintextChar.setText("" + plaintextChar);
 			}
 		}
 	}
 }
