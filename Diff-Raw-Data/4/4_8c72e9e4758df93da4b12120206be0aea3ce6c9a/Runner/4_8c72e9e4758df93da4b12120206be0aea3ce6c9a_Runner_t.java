 package Tools.Runner;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import javax.swing.JPanel;
 import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 import javax.swing.JButton;
 import javax.swing.ButtonGroup;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.JFileChooser;
 
 import javax.swing.border.TitledBorder;
 
 import java.awt.FlowLayout;
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.Dimension;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 import java.io.IOException;
 import java.io.BufferedInputStream;
 
 import Tools.Data.MotionDataConverter;
 
     public class Runner extends JPanel implements ActionListener, Runnable{
 	private int WIDTH, HEIGHT;
 	
 	private MotionDataConverter mdc;
 	
 	    private JFileChooser fc = new JFileChooser();
 
         private JTextField path = new JTextField("./main.exe"); 
 	private JButton runButton = new JButton("Run");
 	private JButton selectButton = new JButton("Select File");
 	//	private JEditorPane out = new JEditorPane();
 	private boolean end = true;
 	//	    private JScrollPane scroll = new JScrollPane(out);
 
 	public Runner(MotionDataConverter mdc){
 		this.mdc = mdc;
 		init();
 	}
 	public void init(){
 	    setLayout(new BorderLayout());
 		JPanel pathPanel = new JPanel();
 		    //		scroll.setPreferredSize(new Dimension(160,160));
 		pathPanel.setLayout(new FlowLayout());
 		    pathPanel.add(new JLabel("Kimoiss Path : "));
 		    pathPanel.add(path);
 		    pathPanel.add(selectButton);
 		    selectButton.addActionListener(this);
 		    JPanel runPanel = new JPanel();
 		    runPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
 		    runPanel.add(runButton);
 		runButton.addActionListener(this);
 		add(runPanel,BorderLayout.EAST);
 		    //		add(scroll,BorderLayout.CENTER);
                 add(pathPanel,BorderLayout.WEST);
 	}
 	    public void update(){
 		revalidate();
 		repaint();
 	    }
 	
 	
 	public void run(){
 	    if(!end)return;
 	    end = false;
 	    /*
 		ArrayList<String> command = new ArrayList<String>();
 		command.add(Option.kPath);
 		command.add("-f "+Option.fps);
 		switch(Option.mode){
 		case Option.MODEL_MODE:
 			command.add("-m m");
 			command.add("-sm "+Option.saveModel);
 			break;
 		case Option.USER_MODE:
 			command.add("-m u");
 			command.add("-mo "+Option.modelPath);
 			command.add("-su "+Option.saveUser);
 			command.add("-sc "+Option.saveConvert);
 			break;
 		}
 	    */
 		    try{
 			Runtime rt = Runtime.getRuntime();
 			//			Process process = rt.exec(command.toArray(new String[0]));
			String[] str = new String[1];
			str[0] = path.getText();
			Process process = rt.exec(str);
 
        			String tmp = "";
 			byte[] output = new byte[1];
 			BufferedInputStream in = new BufferedInputStream(process.getInputStream());
 			int charactor = 0;
 			String[] line;
 			while((charactor = in.read(output)) >= 0){
 			    if(charactor > 0){
 				tmp += new String(output);
 				if(tmp.indexOf("\r\n")>0){
 				    line = tmp.split("\r\n");
 				}else if(tmp.indexOf("\r")>0){
 				    line = tmp.split("\r");
 				}else if(tmp.indexOf("\n")>0){
 				    line = tmp.split("\n");
 				}else{
 				    line = new String[1];
 				    line[0] = tmp;
 				}
 				for(int i = 0; i < line.length; i++){
 				    String[] idxFile = line[i].split(":");
 				    if(idxFile.length == 2){
 					mdc.readFile(idxFile);
 				    }
 				}
 				//				out.setText(tmp);
 			    }
 			}
 			System.out.println(tmp);
 		    }catch(IOException e){
 			e.printStackTrace();
 		    }
 		    end = true;
 	}
 	
 	public void actionPerformed(ActionEvent e){
 		String str = e.getActionCommand();
 		if(str.equals("Run")){
 		    new Thread(this).start();
 		}
 		if(str.equals("Select File")){
 		    if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){
 			path.setText(fc.getSelectedFile().getAbsolutePath());
 		    }
 		}
 	}
 	
 }
