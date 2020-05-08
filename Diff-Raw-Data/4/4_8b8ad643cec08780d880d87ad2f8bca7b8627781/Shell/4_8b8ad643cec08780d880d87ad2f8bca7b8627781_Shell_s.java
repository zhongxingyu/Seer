 import java.awt.Container;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.io.File;
 
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 
 import com.jcraft.jsch.Channel;
 import com.jcraft.jsch.ChannelShell;
 import com.jcraft.jsch.JSch;
 import com.jcraft.jsch.Logger;
 import com.jcraft.jsch.Session;
 import com.jcraft.jsch.UIKeyboardInteractive;
 import com.jcraft.jsch.UserInfo;
 
 import name.khoobyar.joe.jsch.JSchSSPI;
 
 public class Shell {
 
 	public static void main(String[] arg) {
 
 		try {
 			if (arg.length < 1) {
 				System.err.println ("usage: Shell hostname");
 				System.exit (-1);
 			} 
 
 			String host = arg[0];
 			String user = System.getProperty ("user.name");
 			String home = System.getProperty ("user.home");
 			Logger logger = new Logger() {
 				public boolean isEnabled(int level) { return true; }
 				public void log(int level, String message) { System.out.println(message); }
 			};
 
 			JSch jsch = new JSchSSPI ();
 			jsch.setKnownHosts (home+File.separator+".ssh"+File.separator+"known_hosts");
 			JSch.setLogger (logger);
 			Session session = jsch.getSession (user, host, 22);
 
 			// session.setPassword("your password");
 
 			// username and password will be given via UserInfo interface.
 			//UserInfo ui = new MyUserInfo();
 			//session.setUserInfo(ui);
 			session.setConfig("PreferredAuthentications", "gssapi-with-mic");
 			//session.setConfig("GSSAPIAuthentication", "yes");
 			//session.setConfig("GSSAPIDelegateCredentials", "yes");
			// session.setConfig("gssapi-with-mic.krb5", "com.jcraft.jsch.sspi.GSSContextSSPI");
 			session.setConfig("StrictHostKeyChecking", "no");
 
 			// session.connect();
 			session.connect(30000); // making a connection with timeout.
 
 			Channel channel = session.openChannel("shell");
 
 			// Enable agent-forwarding.
 			// ((ChannelShell)channel).setAgentForwarding(true);
 
 			channel.setInputStream(System.in);
 			/*
 			 * // a hack for MS-DOS prompt on Windows.
 			 * channel.setInputStream(new FilterInputStream(System.in){ public
 			 * int read(byte[] b, int off, int len)throws IOException{ return
 			 * in.read(b, off, (len>1024?1024:len)); } });
 			 */
 
 			channel.setOutputStream(System.out);
 
 			// Choose the pty-type "vt102".
 			((ChannelShell)channel).setPtyType("vt102");
 
 			/*
 			 * // Set environment variable "LANG" as "ja_JP.eucJP".
 			 * ((ChannelShell)channel).setEnv("LANG", "ja_JP.eucJP");
 			 */
 
 			// channel.connect();
 			channel.connect(3 * 1000);
 		} catch (Exception e) {
			System.out.println(e);
 		}
 	}
 
 	public static class MyUserInfo implements UserInfo, UIKeyboardInteractive {
 		public String getPassword() {
 			return passwd;
 		}
 
 		public boolean promptYesNo(String str) {
 			Object[] options = { "yes", "no" };
 			int foo = JOptionPane.showOptionDialog(null, str, "Warning",
 					JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
 					null, options, options[0]);
 			return foo == 0;
 		}
 
 		String passwd;
 		JTextField passwordField = (JTextField) new JPasswordField(20);
 
 		public String getPassphrase() {
 			return null;
 		}
 
 		public boolean promptPassphrase(String message) {
 			return true;
 		}
 
 		public boolean promptPassword(String message) {
 			Object[] ob = { passwordField };
 			int result = JOptionPane.showConfirmDialog(null, ob, message,
 					JOptionPane.OK_CANCEL_OPTION);
 			if (result == JOptionPane.OK_OPTION) {
 				passwd = passwordField.getText();
 				return true;
 			} else {
 				return false;
 			}
 		}
 
 		public void showMessage(String message) {
 			JOptionPane.showMessageDialog(null, message);
 		}
 
 		final GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1, 1,
 				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
 				new Insets(0, 0, 0, 0), 0, 0);
 		private Container panel;
 
 		public String[] promptKeyboardInteractive(String destination,
 				String name, String instruction, String[] prompt, boolean[] echo) {
 			panel = new JPanel();
 			panel.setLayout(new GridBagLayout());
 
 			gbc.weightx = 1.0;
 			gbc.gridwidth = GridBagConstraints.REMAINDER;
 			gbc.gridx = 0;
 			panel.add(new JLabel(instruction), gbc);
 			gbc.gridy++;
 
 			gbc.gridwidth = GridBagConstraints.RELATIVE;
 
 			JTextField[] texts = new JTextField[prompt.length];
 			for (int i = 0; i < prompt.length; i++) {
 				gbc.fill = GridBagConstraints.NONE;
 				gbc.gridx = 0;
 				gbc.weightx = 1;
 				panel.add(new JLabel(prompt[i]), gbc);
 
 				gbc.gridx = 1;
 				gbc.fill = GridBagConstraints.HORIZONTAL;
 				gbc.weighty = 1;
 				if (echo[i]) {
 					texts[i] = new JTextField(20);
 				} else {
 					texts[i] = new JPasswordField(20);
 				}
 				panel.add(texts[i], gbc);
 				gbc.gridy++;
 			}
 
 			if (JOptionPane.showConfirmDialog(null, panel, destination + ": "
 					+ name, JOptionPane.OK_CANCEL_OPTION,
 					JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
 				String[] response = new String[prompt.length];
 				for (int i = 0; i < prompt.length; i++) {
 					response[i] = texts[i].getText();
 				}
 				return response;
 			} else {
 				return null; // cancel
 			}
 		}
 	}
 }
