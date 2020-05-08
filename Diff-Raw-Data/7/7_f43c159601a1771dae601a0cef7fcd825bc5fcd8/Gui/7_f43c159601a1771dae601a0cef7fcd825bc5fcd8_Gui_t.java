 package it.treviso.provincia.documentdecryptor;
 
 import it.treviso.provincia.utils.PGPProcessor;
 import java.awt.EventQueue;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JFileChooser;
 import javax.swing.filechooser.FileFilter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.security.NoSuchProviderException;
 import java.security.Security;
 import java.util.jar.Attributes;
 import java.util.jar.Manifest;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.ImageIcon;
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 import org.bouncycastle.openpgp.PGPException;
 
 public class Gui {
 
 	private JFrame frame;
 	private JTextField textFile;
 	private JTextField textKey;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					Gui window = new Gui();
 					window.frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public Gui() {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame();
 		frame.setBounds(100, 100, 450, 300);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().setLayout(null);
 		
 		final JPanel panel = new JPanel();
 		panel.setBounds(12, 26, 428, 263);
 		frame.getContentPane().add(panel);
 		panel.setLayout(null);
 		
 		JLabel lblFileDaCifrare = new JLabel("File da decifrare");
 		lblFileDaCifrare.setBounds(0, 32, 127, 15);
 		panel.add(lblFileDaCifrare);
 		
 		textFile = new JTextField();
 		textFile.setEditable(false);
 		textFile.setBounds(127, 30, 227, 25);
 		panel.add(textFile);
 		textFile.setColumns(10);
 		
 		JLabel lblPercorsoChiave = new JLabel("Percorso chiave");
 		lblPercorsoChiave.setBounds(0, 110, 112, 15);
 		panel.add(lblPercorsoChiave);
 		
 		textKey = new JTextField();
 		textKey.setEditable(false);
 		textKey.setColumns(10);
 		textKey.setBounds(127, 108, 227, 25);
 		panel.add(textKey);
 		
 		JButton chooseFile = new JButton("");
 		chooseFile.setToolTipText("Seleziona file da firmare");
 		chooseFile.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 			      JFileChooser chooser = new JFileChooser();
 			      chooser.setCurrentDirectory(new File("."));
 			      chooser.setFileFilter(new FileFilter() {
 				        public boolean accept(File f) {
 				          return f.getName().toLowerCase().endsWith(".enc") || f.isDirectory();
 				        }
 
 				        public String getDescription() {
 				          return "Encrypted files (*.enc)";
 				        }
 				      });
 			      int r = chooser.showOpenDialog(panel);
 			      if (r == JFileChooser.APPROVE_OPTION) {
 			        String filename = chooser.getSelectedFile().getPath();
 			        textFile.setText(filename);
 			      }
 			    }
 			}
 		);
 		chooseFile.setIcon(new ImageIcon(Gui.class.getResource("/com/sun/java/swing/plaf/windows/icons/NewFolder.gif")));
 		chooseFile.setBounds(374, 30, 42, 25);
 		panel.add(chooseFile);
 		
 		JButton chooseKey = new JButton("");
 		chooseKey.setToolTipText("Seleziona file key contenente la chiave privata");
 		chooseKey.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JFileChooser chooser = new JFileChooser();
 			      chooser.setCurrentDirectory(new File("."));
 			      chooser.setFileFilter(new FileFilter() {
 			        public boolean accept(File f) {
 			          return f.getName().toLowerCase().endsWith(".key") || f.getName().toLowerCase().endsWith(".asc")
 			              || f.isDirectory();
 			        }
 
 			        public String getDescription() {
 			          return "Key Files (*.key, *.asc)";
 			        }
 			      });
 			      int r = chooser.showOpenDialog(panel);
 			      if (r == JFileChooser.APPROVE_OPTION) {
 			        String keyname = chooser.getSelectedFile().getPath();
 			        textKey.setText(keyname);
 			      }
 			}
 		});
 		chooseKey.setIcon(new ImageIcon(Gui.class.getResource("/com/sun/java/swing/plaf/windows/icons/NewFolder.gif")));
 		chooseKey.setBounds(374, 108, 42, 25);
 		panel.add(chooseKey);
 		
		JButton btnCifraFile = new JButton("Decifra File");
 		btnCifraFile.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int result;
 				JPasswordField pwd = new JPasswordField(10); 
 				int ch = JOptionPane.showConfirmDialog(null, pwd,"Inserire password",JOptionPane.OK_CANCEL_OPTION);
 				if (ch < 0 || ch == 2)  result = -1;
 				else result = decryptFile(textFile.getText(),textKey.getText(),pwd.getPassword());
 				switch(result) {
 					case -1: JOptionPane.showMessageDialog(frame,"Decifratura annullata"); break;
 					case 0:	JOptionPane.showMessageDialog(frame, "File decifrato correttamente in "+textFile.getText().substring(0, textFile.getText().lastIndexOf("."))); break;
 					case 1: JOptionPane.showMessageDialog(frame, "File da cifrare non specificato","Specificare file", JOptionPane.ERROR_MESSAGE); break;
 					case 2: JOptionPane.showMessageDialog(frame, "Chiave di decifratura non specificata","Specificare chiave", JOptionPane.ERROR_MESSAGE); break;
 					case 3: JOptionPane.showMessageDialog(frame, "Chiave di decifratura non valida","Chiave Invalida", JOptionPane.ERROR_MESSAGE); break;
 					case 4: JOptionPane.showMessageDialog(frame, "Problemi in fase di decifratura","Errore", JOptionPane.ERROR_MESSAGE); break;
 					case 5: JOptionPane.showMessageDialog(frame, "Errore di I/O sul file, controllare di avere i permessi necessari sul file","Errore I/O", JOptionPane.ERROR_MESSAGE); break;
					case 6: JOptionPane.showMessageDialog(frame, "Errore nel salvare il file decifrato, controllare di avere i permessi di scrittura sul file","Specificare chiave", JOptionPane.ERROR_MESSAGE); break;
 					default: JOptionPane.showMessageDialog(frame, "Errore inatteso", "Errore", JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		});
 		btnCifraFile.setBounds(176, 170, 117, 25);
 		panel.add(btnCifraFile);
 		
 		JMenuBar menuBar = new JMenuBar();
 		frame.setJMenuBar(menuBar);
 		
 		JMenu mnAiuto = new JMenu("Aiuto");
 		menuBar.add(mnAiuto);
 		
 		JMenuItem mntmInformazioni = new JMenuItem("Informazioni");
 		mntmInformazioni.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				String versione = "0";
 				URLClassLoader cl = (URLClassLoader) getClass().getClassLoader();
 				try {
 				  URL url = cl.findResource("META-INF/MANIFEST.MF");
 				  Manifest manifest = new Manifest(url.openStream());
 				  Attributes attr = manifest.getMainAttributes();
 				  versione = attr.getValue("Versione");
 				} catch (IOException E) {
 				  E.printStackTrace();
 				}
 				JOptionPane.showMessageDialog(frame, "DocumentDecryptor versione "+versione+"\nProvincia di Treviso, Settore Sistemi Informatici");
 			}
 		});
 		mnAiuto.add(mntmInformazioni);
 	}
 
 	protected int decryptFile(String file, String key, char[] password) {
 			File f = new File(file);
 			if(!f.exists()) return 1;
 			f = new File(key);
 			if(!f.exists()) return 2;
 	        Security.addProvider(new BouncyCastleProvider());
 	        PGPProcessor p = new PGPProcessor();
 
 	        byte[] original;
 			try {
 				original = p.getBytesFromFile(new File(file));
 			} catch (IOException e) {
 				return 5;
 			}
 
 	        FileInputStream privKey;
 			try {
 				privKey = new FileInputStream(key);
 			} catch (FileNotFoundException e) {
 				return 3;
 			}
 	        byte[] decrypted;
 			try {
 				decrypted = p.decrypt(original, privKey, password);
 			} catch (NoSuchProviderException e) {
 				System.out.println("Error: NoSuchProviderException");
 				return 4;
 			} catch (IOException e) {
 				System.out.println("Error: IOException");
 				return 4;
 			} catch (PGPException e) {
 				System.out.println("Error: PGPException");
 				return 4;
 			}
 
 	        FileOutputStream dfis;
 			try {
 				dfis = new FileOutputStream(file.substring(0, file.lastIndexOf('.')));
 				dfis.write(decrypted);
 				dfis.close();
 			} catch (FileNotFoundException e) {
 				System.out.println("Error: FileNotFoundException");
 				return 6;
 			}
 	        catch (IOException e) {
 	        	System.out.println("Error: IOException");
 				return 6;
 			}
 				
 
 
 		return 0;
 	}
 	
 
 }
