 
 package SeljeIRC;
 
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.text.BadLocationException;
 
 /**
  * Creates and takes care of the input-field where the user writes text/commands
  * @author Hallvard Westman
  * @since 0.2
  */
 public class InputField extends JPanel {
     private JLabel label = new JLabel();
     private JTextField inputField;
     private String channel;
     private int tabType;
     
     private static ConnectionHandler connection = SeljeIRC.connectionHandlerObj.getInstance();
     private Pattern inputCommandFinderPattern = Pattern.compile("^/\\w+");
 
     /**
     * Constructor for aa opprette felt for aa faa tekst fra brukeren. 
      * 
      * 
     * Feltet opprettes basert paa kanalnavn og typen tab den skal vaare i. Den vil derfra ta teksten som blir puttet inn og sende det til servern.  
      * @author Hallvard Westman
      * @param tabName
      * @param TabType
      */
     public InputField(String tabName, int TabType){
         super();											// Ingenting blir sendt til parent
         channel = tabName;
         this.tabType = TabType;								// Sets the tabtype to int provided from constructor
         BorderLayout layout = new BorderLayout();
         setLayout(layout);									// Set layout of field
 
         label = new JLabel(tabName);						// Name provided from statustabe
         add(label,BorderLayout.WEST);
         
         inputField = new JTextField();
                 
         add(inputField,BorderLayout.CENTER);
         JButton button = new JButton(I18N.get("inputfield.send"));
         add(button,BorderLayout.EAST);
         
         button.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent ae){
                 
                 /*
                  * sending input to approporiate screen
                  */
             	postTextToIRC(inputField);
             }
         });
         
         inputField.addActionListener(new ActionListener(){		// Hvis en bruker trykker knapp eller enter
 
             public void actionPerformed(ActionEvent ae){
                 
                 /*
                  * sending input to approporiate screen
                  */
             	postTextToIRC(inputField);						// Post to function	
             	
                 
                 
             }
         });
 
 
     }
     /**
      * Types the text to the server, and resets the text in the input field
      * 
      * Checks the type of the tab and if text contains /commands. Takes actions approrpiatly.
      * 
      * @author Christer Vaskinn
      * @since 0.1
      * @param txtInputField InputField with text to send
      */
     
     private void postTextToIRC(JTextField txtInputField){
     	int typeOfMessage = tabType;								// Type of field  or where to post message
     	String textToPost = txtInputField.getText();				// Get text from inputfield provided
     	
     	Matcher inputCommandFinder = inputCommandFinderPattern.matcher(textToPost);
     	
         if(inputCommandFinder.find())									// Checks if text contains /commands
         	typeOfMessage = SingleTab.STATUS;
     														
         if(this.connection == null)							// Checks if the object got initiated correctly
     		this.connection = SeljeIRC.connectionHandlerObj.getInstance();			// IT didn't
         
         try{
         	switch(typeOfMessage){
     			case SingleTab.PRIVATE: connection.sayToPrivate(textToPost, channel); break;	// Send it to private
     			case SingleTab.CHANNEL: connection.sayToChannel(textToPost, channel); break; 	// Send ut to private
     			default: 									
     				if(tabType == SingleTab.CHANNEL)											// Send text server
     					connection.sayToServer(textToPost,channel);
     				else
     					connection.sayToServer(textToPost,null);
     				break;
 
         	}
         }catch(BadLocationException e){												// Something awful
         	System.err.println(I18N.get("connection.systemerror") + e.getMessage());
         }catch(NullPointerException ex){
         	System.err.println(I18N.get("inputfield.majorfuckup") + ex.getMessage());
         }
         
     	txtInputField.setText("");
     
     	
     }
     public void setFocusOnField(){
         inputField.requestFocus();
     }
 }
