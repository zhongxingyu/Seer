 import java.awt.Dimension;
 import java.awt.EventQueue;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import Lexical.ErrorHandler;
 import Lexical.SymbolTable;
 import Sintactic.Parser;
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.IOException;
 
 import assembler.CodeGenerator;
 import utils.PathContainer;
 import utils.Printer;
 
 
 public class mainWindow {
 
 	private JFrame frame;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					mainWindow window = new mainWindow();
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
 	public mainWindow() {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	protected JLabel lblNotSelected;
 	protected JTextArea textArea;
 	protected JTextArea textArea_1;
 	private void initialize() {
 		frame = new JFrame();
 		frame.setBounds(100, 100, 673, 491);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().setLayout(null);
 		
 		JButton btnChooseSource = new JButton("Choose Source");
 		btnChooseSource.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				openDialog();
 			}
 		});
 		btnChooseSource.setMnemonic('c');
 		btnChooseSource.setBounds(22, 22, 137, 29);
 		frame.getContentPane().add(btnChooseSource);
 		
 		JButton btnRun = new JButton("Run");
 		btnRun.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				try {
 					runCompiler();
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 		});
 		btnRun.setMnemonic('r');
 		btnRun.setBounds(171, 22, 117, 29);
 		frame.getContentPane().add(btnRun);
 		
 		JLabel lblFilePath = new JLabel("File path:");
 		lblFilePath.setBounds(22, 63, 61, 16);
 		frame.getContentPane().add(lblFilePath);
 		
 		lblNotSelected = new JLabel("Not selected");
 		lblNotSelected.setBounds(95, 63, 296, 16);
 		frame.getContentPane().add(lblNotSelected);
 		
 		JLabel lblErrors = new JLabel("Errors");
 		lblErrors.setBounds(22, 116, 61, 16);
 		frame.getContentPane().add(lblErrors);
 		
 		textArea = new JTextArea();
 		textArea.setEditable(false);
 		textArea.setBounds(22, 144, 280, 304);
 		JScrollPane scroll = new JScrollPane (textArea, 
 				   JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 		scroll.setLocation(22, 144);
 		scroll.setSize(310, 297);
 		frame.getContentPane().add(scroll);
 		
 		textArea_1 = new JTextArea();
 		textArea_1.setEditable(false);
 		textArea_1.setBounds(330, 144, 280, 304);
 		JScrollPane scroll1 = new JScrollPane (textArea_1, 
 				   JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 		scroll1.setLocation(374, 144);
 		scroll1.setSize(293, 297);
 		scroll1.setPreferredSize(new Dimension(450, 110));
 		frame.getContentPane().add(scroll1);
 		
 		JLabel lblSymbolTable = new JLabel("Symbol Table");
 		lblSymbolTable.setBounds(374, 116, 117, 16);
 		frame.getContentPane().add(lblSymbolTable);
 	}
 	public void openDialog(){
 		JFileChooser fileChooser = new JFileChooser();
 		FileNameExtensionFilter filter = new FileNameExtensionFilter("Compiler source code *.txt","txt");
 		fileChooser.setFileFilter(filter);
 		frame.getContentPane().add(fileChooser);
 		int returnVal = fileChooser.showOpenDialog(null);
 	    if (returnVal == JFileChooser.APPROVE_OPTION) {
 	        File file = fileChooser.getSelectedFile();
 	        // What to do with the file, e.g. display it in a TextArea
 	        //textarea.read( new FileReader( file.getAbsolutePath() ), null );
 	        String path = file.getAbsolutePath();
 	        lblNotSelected.setText(path);
 	    } else {
 	        //System.out.println("File access cancelled by user.");
 	    }
 	}
 	
 	public void runCompiler() throws IOException{
 		if (lblNotSelected.getText().equals("Not selected")){
 			//showAlert("Please choose a file");
 			JOptionPane.showMessageDialog(null,"Please select a file first.");
 		}else{
 			PathContainer.setPath(lblNotSelected.getText());
 			Parser parser = new Parser();
 			parser.run();
 			textArea.setText(ErrorHandler.getInstance().toString());
 			textArea_1.setText(SymbolTable.getInstance().toString());
 			
			if(!ErrorHandler.getInstance().isInErrorState()){
				CodeGenerator generator = new CodeGenerator();
				Printer.printPolish(parser.parserUtils.intermediateCode);
				generator.generate(parser.parserUtils.intermediateCode);
			} else {
				textArea.append("Error founds during compilation. Aborting");
			}
			
			
 		}
 		
 	}
 }
