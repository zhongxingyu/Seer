 package Configurator;
 
 import Button2.NetworkRequest;
 import Button2.Sender;
 import Login.PasswordEncoder;
 import Login.UI;
 import Util.FileIO;
 import Util.Messages;
 import Util.Path;
 import Util.Popup;
 import Util.Strings;
 import java.io.IOException;
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.CommandListener;
 import javax.microedition.lcdui.Display;
 import javax.microedition.lcdui.Displayable;
 import javax.microedition.lcdui.Form;
 import javax.microedition.lcdui.TextField;
 
 /*******************************************************************************
 * CLASS: Login
  *
  * FUNCTIONS: Login(String title, Configurator m)
  *            void updateLanguage()
  *
  * DATE: 2013-01-18
  *
  * DESIGNER: Team Cirno
  *
  * PROGRAMMER: Team Cirno
  *
  * NOTES: The screen display for the login submenu and updates login information
  *******************************************************************************/
 public class Login extends Form
 {
 	/**
 	 * The configurator this screen is part of.
 	 */
 	private Configurator m;
 	/**
 	 * The structure that holds the user's information.
 	 */
 	private UI userInfo;
 	/**
 	 * Command to login.
 	 */
 	private Command loginCommand = new Command("Login", Command.OK, 1);//"Save", Command.OK, 1);
 	/**
 	 * Command to go back a screen.
 	 */
 	private Command backCommand = new Command("Back", Command.BACK, 3);
 	/**
 	 * Command to go cancel logging in.
 	 */
 	private Command cancelCommand = new Command("Cancel", Command.CANCEL, 3);
 	/**
 	 * Command to read user information from a file.
 	 */
 	private Command readCommand = new Command("Read information", Command.OK, 2);
 	/**
 	 * Text field for inputting username.
 	 */
 	private TextField userName;
 	/**
 	 * Text field for inputting password.
 	 */
 	private TextField password;
 	/**
 	 * Text field for inputting companyID.
 	 */
 	private TextField companyID;
 	/**
 	 * Display class used for pop up.
 	 */
 	private Display dis;
 	/**
 	 * File IO object.
 	 */
 	private FileIO f = new FileIO();
 	/**
 	 * Pop up object used to displace messages to the user.
 	 */
 	private Popup pop;
 
 	/*************************************************************************** 
 	 * FUNCTION: Login
 	 * 
 	 * DATE: 2013-01-30
 	 * 
 	 * DESIGNER: Team Cirno
 	 * 
 	 * PROGRAMMER: Team Cirno
 	 * 
 	 * INTERFACE: Login(String title, Configurator m)
 	 *            String title - the title of the screen
 	 *            Configurator m - the Configurator this screen is part of
 	 * 
 	 * NOTES: Constructor for the Login class
 	 ***************************************************************************/
 	public Login(String title, Configurator m)
 	{
 		super(title);
 		this.m = m;
 		dis = Display.getDisplay(m);
 
 		String str[] = Strings.getAllMessages();
 		userName = new TextField(str[Messages.NAME], "", 100, TextField.ANY);
 		password = new TextField(str[Messages.USER_PASSWORD], "", 100, TextField.PASSWORD);
 		companyID = new TextField(str[Messages.USER_ID], "", 16, TextField.NUMERIC);
 
 		append(userName);
 		append(password);
 		append(companyID);
 
 		addCommand(loginCommand);
 		addCommand(readCommand);
 		addCommand(backCommand);
 		addCommand(cancelCommand);
 		f = new FileIO();
 		setCommandListener(new LoginListener());
 	}
 
 	/*************************************************************************** 
 	 * FUNCTION: updateLanguage
 	 * 
 	 * DATE: 2013-01-30
 	 * 
 	 * DESIGNER: Team Cirno
 	 * 
 	 * PROGRAMMER: Team Cirno
 	 * 
 	 * INTERFACE: void updateLanguage()
 	 * 
 	 * RETURN: void
 	 * 
 	 * NOTES: Updates the displayed language
 	 ***************************************************************************/
 	public void updateLanguage()
 	{
 		String str[] = Strings.getAllMessages();
 		setTitle(str[Messages.LOGIN]);
 		userName.setLabel(str[Messages.NAME]);
 		password.setLabel(str[Messages.USER_PASSWORD]);
 		companyID.setLabel(str[Messages.USER_ID]);
 	}
 
 	/**
 	 * Command listener
 	 */
 	private class LoginListener implements CommandListener
 	{
 		/**
 		 * Command action listener.
 		 *
 		 * @param c The command
 		 * @param d Displayable where the command originated
 		 */
 		public void commandAction(Command c, Displayable d)
 		{
 			//if save button is pressed.
 			if (c.equals(loginCommand))
 			{
 				String username = userName.getString();
 				String passw = password.getString();
 				int compID = Integer.parseInt(companyID.getString());
 
 				userInfo = new UI(username, passw, compID);
 				//write information to text file.
 				if(!f.writeTextFile(Path.LOGIN_FILENAME, userInfo.toString()))
 				{
 					//if failed popup a message
 					pop = new Popup(m, Strings.getMessage(Messages.SAVING_FAILED), true, m.getLoginScreen());
 					dis.setCurrent(pop);
 				} else
 				{
 					//if successfully saved, send message to saftyline servers.
 					String response = Sender.send(null, NetworkRequest.OK_MESSAGE);
 					//then popup message with result.
 					pop = new Popup(m, (response.equals("OK"))
 						? Strings.getMessage(Messages.RECEIVED)
 						: response, true, m.getLoginScreen());
 
 					dis.setCurrent(pop);
 				}
 			//if cancel button is clicked
 			} else if(c.equals(cancelCommand))
 			{
 				Configurator.moveto(m.getSettingScreen());
 			//if read info button is clicked
 			} else if(c.equals(readCommand))
 			{
 				try
 				{
 					//read information from path.
 					f.readFromAbsolutePath(Path.ROOT_PATH + Path.LOGIN_FILENAME);
 					userInfo = new UI(f.getLineAt(0), PasswordEncoder.decode(f.getLineAt(1)), Integer.parseInt(f.getLineAt(2)));
 
 					userName.setString(userInfo.getUserName());
 					password.setString(userInfo.getPassword());
 					companyID.setString(Integer.toString(userInfo.getCompanyId()));
 				} catch (IOException ex)
 				{
 					//if there is an exception, popup message containing the 
 					//detail of the exception.
 					dis.setCurrent(new Popup(m, ex.getMessage(), true, m.getLoginScreen()));
 				}
 			} else if (c.equals(backCommand)) //if back button is clicked
 			{
 				Configurator.moveto(m.getSettingScreen());
 			}
 		}
 	}
 }
