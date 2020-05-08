 /**
  * 
  */
 package net.niconomicon.tile.source.app;
 
 import java.awt.BorderLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 
 /**
  * @author Nicolas Hoibian
  * 
  */
 public class SaveDialog extends JPanel {
 
 	protected JTextField outputFileName;
 	protected JTextField where;
 	protected JButton browseOutput;
 
 	JTextArea description;
 	JTextField author;
 	JTextField title;
 
 	public SaveDialog() {
 		super();
 		this.setLayout(new BorderLayout());
 		init();
 	}
 
 	public void init() {
 		GridBagConstraints c;
 		int x, y;
 		x = y = 0;
 		JPanel option = new JPanel(new GridBagLayout());
 
 		title = new JTextField("", 20);
 		description = new JTextArea("", 5, 30);
 		author = new JTextField(System.getProperty("user.name"), 20);
 
 		// could also load from the user's preferences
 
 		// load from file name
 		outputFileName = new JTextField("", 10);
 
 		where = new JTextField("", 20);
 		where.setEditable(false);
 
 		browseOutput = new JButton("Choose Directory");
 		browseOutput.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 			//	Thread t = new Thread(new RootDirSetter());
 			//	t.start();
 			}
 		});
 
 		
 		c = new GridBagConstraints();
 		c.gridy = y++;
 		c.gridx = x;
 		c.anchor = c.LINE_END;
 		option.add(new JLabel("Title :"), c);
 
 		// //////////////
 
 		c = new GridBagConstraints();
 		c.gridy = y++;
 		c.gridx = x;
 		c.anchor = c.LINE_END;
 		option.add(new JLabel("Save as :"), c);
 
 		c = new GridBagConstraints();
 		c.gridy = y++;
 		c.gridx = x;
 		c.anchor = c.LINE_END;
 		option.add(new JLabel("In directory :"), c);
 
		x=1;
		y=0;
 		c = new GridBagConstraints();
 		c.gridy = y++;
 		c.gridx = x;
 		c.gridwidth = 2;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		option.add(title, c);
 
 		c = new GridBagConstraints();
 		c.gridy = y++;
 		c.gridx = x;
 		c.gridwidth = 2;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.anchor = c.LINE_START;
 		option.add(outputFileName, c);
 
 		c = new GridBagConstraints();
 		c.gridy = y;
 		c.gridx = x;
 		c.anchor = c.LINE_START;
 		option.add(where, c);
 
 		c = new GridBagConstraints();
 		c.gridy = y++;
 		c.gridx = x + 1;
 		option.add(browseOutput, c);
 
 		this.add(option, BorderLayout.CENTER);
 		this.add(new JLabel("Save !"), BorderLayout.NORTH);
 	}
 
 	public static void main(String[] args) {
 		SaveDialog d = new SaveDialog();
 		JFrame f = new JFrame();
 		f.setContentPane(d);
 		f.pack();
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		f.setVisible(true);
 	}
 }
