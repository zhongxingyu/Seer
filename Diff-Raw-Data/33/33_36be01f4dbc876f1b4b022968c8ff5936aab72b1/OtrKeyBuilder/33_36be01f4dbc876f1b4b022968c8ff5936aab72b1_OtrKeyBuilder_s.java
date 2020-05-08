 package net.java.otrkeybuilder;
 import java.security.spec.*;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.*;
 
 import javax.swing.*;
 import javax.swing.border.BevelBorder;
 import javax.swing.text.Position;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.MutableTreeNode;
 import javax.swing.tree.TreePath;
 
 import net.java.otr4j.crypto.OtrCryptoEngineImpl;
 import net.java.otr4j.crypto.OtrCryptoException;
 /**
  * The main class for OteKeyBuilder that extends a {@link JFrame}.
  * 
  * @author Mohamed Akram Tabka
  */
 @SuppressWarnings("serial")
 public class OtrKeyBuilder extends JFrame {
 	
 		//JFrame app
 		static OtrKeyBuilder app;
 	    
 		// Generator and Importer Dialogs 
 		static KeyGeneratorDialog genDialog ; 
 		static KeyImporterDialog imp;
 		
 		//JTree and nodes
 		static JTree tree;
 		static DefaultMutableTreeNode keysNode = new DefaultMutableTreeNode("Keys");
 		public static DefaultMutableTreeNode nNode;
 	    public static MutableTreeNode node;
 	    public static DefaultTreeModel model;
 	    public static TreePath path;
 	    final static Map <String,DefaultMutableTreeNode> fingerPrint =  new HashMap<String,DefaultMutableTreeNode>();
 		final static Map <String,DefaultMutableTreeNode> googleTalk =  new HashMap<String,DefaultMutableTreeNode>();
 		final static Map <String,DefaultMutableTreeNode> facebook =  new HashMap<String,DefaultMutableTreeNode>();
 		final static Map <String,DefaultMutableTreeNode> icq =  new HashMap<String,DefaultMutableTreeNode>();
 		final static Map <String,DefaultMutableTreeNode> yahoo =  new HashMap<String,DefaultMutableTreeNode>();
 	
 		
 		//panels
 		static JScrollPane treePnl = new JScrollPane(tree);
 		static JPanel genImpPnl = new JPanel();
 		static JPanel buildPnl = new JPanel();
 		static JPanel mainPnl = new JPanel();
     	
 		static JButton generateBtn =new JButton("Generate");
 		static JButton importBtn =new JButton("Import");
 	    static JButton buildBtn =new JButton("Build");
 	  
 	    private static JButton browseSrcBtn = new JButton("Browse...");
 	    private static JButton browseDesBtn = new JButton("Browse...");
 	    static JTextField linuxUserFld = new JTextField("amnesia");
 	    
 	    static String isoPath ="./linuxDist/tails-i386-0.18.iso";
 	    static String desPath ="./linuxDist";
 	    
 	    static private JLabel srcPathLbl = new JLabel("<html><pre>"+isoPath+"</pre></html>");
 	    static private JLabel desPathLbl = new JLabel("<html><pre>"+desPath+"</pre></html>");
 	    static JLabel statusLabel = new JLabel("Ready");
 	    //this is an instance of the basic class OtrKey in which we will get the generated or imported Key.
 		static OtrKey key ;
 /*
  * Basic constructor of JFrame application
  */
 public OtrKeyBuilder()
 {
 super();
 setTitle("OTR key Builder");
 setSize(600, 400);
 setResizable(false);
 setLocationRelativeTo(null);
 setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 }		
 /*
  * private methods that display JFrame components
  */
 private static void setGenImpPnl(){
 	genImpPnl.setLayout(new BoxLayout(genImpPnl, BoxLayout.PAGE_AXIS));
 	genImpPnl.setBorder(BorderFactory.createRaisedBevelBorder());
 
 	generateBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
 	genImpPnl.add(generateBtn);
 	genImpPnl.add(Box.createRigidArea(new Dimension(0,5)));
 	importBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
 	genImpPnl.add(importBtn);
 }
 private static void setBuildPnl()
 {
 	buildPnl.setLayout(new GridBagLayout());
     GridBagConstraints c = new GridBagConstraints();
     c.fill = GridBagConstraints.HORIZONTAL;
     c.ipady = 0;
     c.gridx = 0;
     c.gridy = 0;
     buildPnl.add(new JLabel("Source iso:"),c);
     c.gridx = 1;
     buildPnl.add(srcPathLbl,c);
     c.gridx = 2;
     c.gridy = 0;
     buildPnl.add(browseSrcBtn,c);
     
     c.gridx = 0;
     c.gridy = 1;
     buildPnl.add(new JLabel("Dest path:"),c);
     c.gridx = 1;
     buildPnl.add(desPathLbl,c);     
     c.gridx = 2;
     buildPnl.add(browseDesBtn,c);
 
     c.fill = GridBagConstraints.HORIZONTAL;
     c.gridx= 1;
     c.gridy= 2;
     c.gridwidth = 1;
     buildPnl.add(linuxUserFld,c);
     c.gridwidth= 2 ;
     c.gridx= 2;
     buildPnl.add(buildBtn,c);
     
    c.fill = GridBagConstraints.HORIZONTAL;
 	app.add(buildPnl,BorderLayout.SOUTH);
 	buildPnl.setEnabled(false);
 	buildPnl.setBorder(BorderFactory.createTitledBorder(BorderFactory.createTitledBorder("Build keys")));
 	
 }
 private static void setStatusPnl(){
 	JPanel statusPanel = new JPanel();
 	statusPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
 
 	app.add(statusPanel, BorderLayout.PAGE_END);
 	statusPanel.setPreferredSize(new Dimension(app.getWidth(), 20));
 	statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
 	statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
 	statusPanel.add(statusLabel);
 }
 private static void setMainPnl(){
 	mainPnl.setLayout(new GridBagLayout());
     GridBagConstraints c1 = new GridBagConstraints();
     
     c1.ipady = 0;
     c1.gridx = 0;
     c1.gridy = 0;
     mainPnl.add(genImpPnl,c1);
     c1.fill = GridBagConstraints.HORIZONTAL;
     c1.gridx = 1;
       c1.gridy = 0;
       c1.gridheight=5;
       c1.ipady = 200 ;
       mainPnl.add(treePnl,c1);
     c1.fill = GridBagConstraints.HORIZONTAL;
     c1.gridx = 0;
     c1.gridy = 6;
     c1.ipady = 0 ;
     c1.weightx = 2;
     c1.gridwidth=2;
     
     mainPnl.add(buildPnl,c1);
     app.add(mainPnl,BorderLayout.CENTER);
 }
 	
 
 
 	/**
 	 * OtrKeyBuilder main function
 	 */
     public static void main(final String[] args) throws InvalidKeySpecException, IOException 
     {
     	System.out.println("Starting OTR Key Builder ...");
     	app = new OtrKeyBuilder();
     	generate();
     	importKey();
     	browse();
     	build();
     	createKeysTree();
     	
     	setGenImpPnl();
     	setBuildPnl();
     	setStatusPnl();
     	
 		setMainPnl();	
 		
     	app.setVisible(true);
     	
     	Runtime.getRuntime().addShutdownHook(new Thread()
     	{
     	    @Override
     	    public void run()
     	    {
     	    	try{
     	        Runtime r1 = Runtime.getRuntime();
     	              System.out.println("revoming key cache...");
     	              statusLabel.setText("removing key cache");
     	              
     	              System.out.print(desPath);
     	              String cmd = "rm -r keys" ;
     	              
     	              System.out.println("-----------------------\n"+cmd+"\n-----------------------\n");
     	          Process p = r1.exec(cmd);
 
     	          BufferedReader stdOutput = new BufferedReader(new InputStreamReader(p.getInputStream()));
     	          BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
     	          
     	          
     	          while(stdOutput.readLine()!= null||stdError.readLine()!= null)
     	          {
     	              System.out.println(stdOutput.readLine());
     	          	System.out.println(stdError.readLine());
     	          }
     	    }
     	    catch (Exception e)
     	    {
     	    	System.out.println(e.getMessage());
     	    }
     	    }
     	});
     }
     /**
      * This is the generate event
      */
     public static void generate()
     {
     	    generateBtn.addActionListener(new ActionListener()
     	    {
     	        public void actionPerformed(ActionEvent arg0)
     	        { 
     	            genDialog =new KeyGeneratorDialog();
     	            genDialog.start();
     	            genDialog.generateKeyFormats(); 
     	         
     	            
     	     genDialog.addWindowListener(new WindowAdapter() {            
     	      public void windowClosed(WindowEvent e) {
     	                System.out.println("Generator dialog closed");
                      try {
   	                     key = genDialog.getKey();
   	                     
   	                     String fingerprint = new OtrCryptoEngineImpl().getFingerprint(key.getKeyPair().getPublic());
     	                 updateKeysTree(fingerprint);
     	     	         key.getAccountArray();
     	     	         
     	      	       	model = (DefaultTreeModel)tree.getModel();
  		
     	       		  Set<Entry<String, String>> cles = key.getAccountArray().entrySet();
     	       		  		for(Entry<String, String> entry : cles) {
     	       		  					String cle = entry.getKey();
     	       		  					String valeur = entry.getValue();
     	       		  					
     	       		  					insertAccountTree(cle, valeur, fingerprint);   	    		       
     	       		  					statusLabel.setText("Key generated");
     	       		  		}
     	       		  		for (int i = 0; i < tree.getRowCount(); i++) {
     	       		  			tree.expandRow(i);
     	       		  		}
     	       		  		genDialog.removeWindowListener(this); //to avoid occurrence
     	        	      } catch (OtrCryptoException e1) {
     	        	      		// TODO Auto-generated catch block
     	        	      		e1.printStackTrace();
     	                  }                          
     	                }
     	                
     	            });      
     	 
     	        }
     	    });    
     }
     public static void importKey()
     {
     	 importBtn.addActionListener(new ActionListener()
  	    {
  	        public void actionPerformed(ActionEvent arg0)
  	        {
  	        	imp = new KeyImporterDialog();
  	       if(imp.getImportedFile()!="")
  	       {
  	    	   		imp.constructKeyPairs();
  	    	   		imp.start();
  	    	   		imp.generateKeyFormats();
  	    	 
  	    	   		imp.addWindowListener(new WindowAdapter() {            
  	    	   			public void windowClosed(WindowEvent e) {
  	    	   			System.out.println("Generator dialog closed");
  	    	   			key = imp.getKey();
  	    	   				String fingerprint;
  	    	   				try {
  	    	   					fingerprint = new OtrCryptoEngineImpl().getFingerprint(key.getKeyPair().getPublic());
  	    	   					updateKeysTree(fingerprint);
  	    	   					Set<Entry<String, String>> cles2 = key.getAccountArray().entrySet();
  	    	   					for(Entry<String, String> entry : cles2) {
  	    	   						String cle = entry.getKey();
  	    	   						String valeur = entry.getValue();
  	    	   						
  	    	   						insertAccountTree(cle,valeur,fingerprint);
  	    	   						statusLabel.setText("Key imported");
  	    	   						
  	    	   					}
  	    	   					for (int i = 0; i < tree.getRowCount(); i++) {
  	    	   						tree.expandRow(i);
  	    	   					}
  	    	   					imp.removeWindowListener(this);
  	    	   				} catch (OtrCryptoException e1) {
  	    	   					// TODO Auto-generated catch block
  	    	   					e1.printStackTrace();
  	    	   				}
  	    	   			}});
  	       }else{
  	    	   System.out.print("No key file imported\n");
  	       }
 	
  	      }
  	    });
     }
     private static void browse(){
     	browseSrcBtn.addActionListener(new ActionListener()
 	    {
 	        public void actionPerformed(ActionEvent arg0)
 	        { 
     	JFileChooser chooser = new JFileChooser();
         chooser.setCurrentDirectory(new java.io.File("."));
         chooser.setDialogTitle("Select Linux iso Distribution");
         chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
         chooser.setAcceptAllFileFilterUsed(false);
 
         if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
           System.out.println("getCurrentDirectory(): " + chooser.getCurrentDirectory());
           System.out.println("getSelectedFile() : " + chooser.getSelectedFile());
           isoPath = chooser.getSelectedFile().toString();
           srcPathLbl.setText(isoPath);
          
         } else {
           System.out.println("No Selection ");
         }
 	        }
 	    });
     	browseDesBtn.addActionListener(new ActionListener()
 	    {
 	        public void actionPerformed(ActionEvent arg0)
 	        { 
     	JFileChooser chooser = new JFileChooser();
         chooser.setCurrentDirectory(new java.io.File("."));
         chooser.setDialogTitle("Select output path");
         chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         chooser.setAcceptAllFileFilterUsed(false);
 
         if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
           System.out.println("getCurrentDirectory(): " + chooser.getCurrentDirectory());
           System.out.println("getSelectedFile() : " + chooser.getSelectedFile());
           desPath = chooser.getSelectedFile().toString();
          
           desPathLbl.setText(desPath);
          
         } else {
           System.out.println("No Selection ");
         }
 	        }
 	    });
     }
 
     public static DefaultMutableTreeNode updateKeysTree(String fingerprint)//, String jitsi,String pidgin)
     {
     	model = (DefaultTreeModel)tree.getModel();
     	if(!fingerPrint.containsKey(fingerprint))
     	{
     	fingerPrint.put(fingerprint, new DefaultMutableTreeNode(fingerprint));
         nNode = fingerPrint.get(fingerprint);
         path = tree.getNextMatch("M", 0, Position.Bias.Forward);
         
         node = keysNode;
         model.insertNodeInto(nNode, node, node.getChildCount());
     	}
     	else{
     		System.out.println("Key already exists");
     	}
        // accountFld.setText("");
         return nNode;
     }
     
  public static void createKeysTree()
  {
 	 tree = new JTree(keysNode);
      tree.setBorder(BorderFactory.createTitledBorder(BorderFactory.createTitledBorder("keys Tree :")));
 
       treePnl = new JScrollPane(tree);
  }
  private static void insertAccountTree(String cle,String valeur,String fingerprint)
  {
 	 nNode = new DefaultMutableTreeNode(cle);
 	 path = tree.getNextMatch("M", 0, Position.Bias.Forward);
 	 MutableTreeNode netNode = new DefaultMutableTreeNode();
 	 if (valeur=="Google_Talk"){
 	 	if(googleTalk.containsKey(fingerprint)){
 	 		netNode = googleTalk.get(fingerprint);
 	 	}
 	 	else{
 	 		model = (DefaultTreeModel)tree.getModel();
 	         googleTalk.put(fingerprint, new DefaultMutableTreeNode("Google_talk"));
 	         netNode = googleTalk.get(fingerprint);
 	         model.insertNodeInto(netNode, fingerPrint.get(fingerprint), fingerPrint.get(fingerprint).getChildCount());
 	 	}
 	 }
 	 else if (valeur=="Facebook"){
 	 	if(facebook.containsKey(fingerprint)){
 	 		netNode = facebook.get(fingerprint);
 	 	}
 	 	else{
 	 		model = (DefaultTreeModel)tree.getModel();
 	         facebook.put(fingerprint, new DefaultMutableTreeNode("Facebook"));
 	         netNode = facebook.get(fingerprint);
 	         model.insertNodeInto(netNode, fingerPrint.get(fingerprint), fingerPrint.get(fingerprint).getChildCount());
 	 	}
 	 }
 	 else if (valeur=="ICQ"){
 	 	if(icq.containsKey(fingerprint)){
 	 	netNode = icq.get(fingerprint);
 	 	}
 	 	else{
 	 		model = (DefaultTreeModel)tree.getModel();
 	         icq.put(fingerprint, new DefaultMutableTreeNode("ICQ"));
 	         netNode = icq.get(fingerprint);
 	         model.insertNodeInto(netNode, fingerPrint.get(fingerprint), fingerPrint.get(fingerprint).getChildCount());
 	 	}
 	 }
 	 else if (valeur=="Yahoo"){
 	 	if(yahoo.containsKey(fingerprint)){
 	 	netNode = yahoo.get(fingerprint);
 	 	}
 	 	else{
 	 		model = (DefaultTreeModel)tree.getModel();
 	         yahoo.put(fingerprint, new DefaultMutableTreeNode("Yahoo"));
 	         netNode = yahoo.get(fingerprint);
 	         model.insertNodeInto(netNode, fingerPrint.get(fingerprint), fingerPrint.get(fingerprint).getChildCount());
 	 	}
 	 }
 
 	 model.insertNodeInto(nNode,netNode, netNode.getChildCount());
  }
  private static void build()
  {
 	 buildBtn.addActionListener(new ActionListener()
 	    {
 	        public void actionPerformed(ActionEvent arg0)
 	        { 
 	        	 Runtime r = Runtime.getRuntime();
 
 	       /*
 	        * Creating builder.bash to be called 
 	        * when we need to build keys ....
 	        */
 	        	 File file = new File("./script/builder.bash");
 		       		file.getParentFile().mkdirs();
 	      	  try {
 	   			if (!file.exists()) {
 	       			file.createNewFile();
 	       		}
 	   			
 		       	FileWriter fw = new FileWriter(file.getAbsoluteFile());
 		   		BufferedWriter bw = new BufferedWriter(fw);
 		      	
 	 			bw.write("#!/bin/bash \nif (($#==5))\nthen\n  pidginPath=\"$1\"\n  jitsiPath=\"$2\"\n  isoPath=\"$3\"\n  linuxUser=\"$4\"\n  desPath=\"$5\"\nelse\necho \"error arguments\" 1>&2\n# exit\nfi\n");
                 bw.write("\nmkdir -p BUILD \nmkdir BUILD/mnt\nmount -o loop $isoPath BUILD/mnt/\nmkdir BUILD/extract-cd\nrsync --exclude=/live/filesystem.squashfs -a BUILD/mnt/ BUILD/extract-cd");
 	       	    bw.write("\nmkdir BUILD/squashfs\nmount -t squashfs -o loop BUILD/mnt/live/filesystem.squashfs BUILD/squashfs\nmkdir BUILD/edit\ncp -a BUILD/squashfs/* BUILD/edit/ 2> BUILD/hello");
 		       		  
        	 	    bw.write("\n\nmkdir ./BUILD/edit/home/$linuxUser\nmkdir ./BUILD/edit/home/$linuxUser/.purple ./BUILD/edit/home/$linuxUser/.jitsi");
 		        bw.write("\ncp $pidginPath/* ./BUILD/edit/home/$linuxUser/.purple/\ncp $jitsiPath/* ./BUILD/edit/home/$linuxUser/.jitsi/");
 		       		  
 	 		    bw.write("\nmksquashfs ./BUILD/edit ./BUILD/extract-cd/live/filesystem.squashfs\ncd ./BUILD/extract-cd");
 	 		    if (desPath.startsWith("/")){
 	 		    	bw.write("\ngenisoimage -o $desPath/monimage.iso -r -J -no-emul-boot -boot-load-size 4 -boot-info-table -b isolinux/isolinux.bin -c isolinux/boot.cat ./");
 	 		    }
 	 		    else{
 	 		    	bw.write("\ngenisoimage -o ../../$desPath/monimage.iso -r -J -no-emul-boot -boot-load-size 4 -boot-info-table -b isolinux/isolinux.bin -c isolinux/boot.cat ./");	
 	 		    }		        
 		       		
 		        bw.close();
 		        file.setExecutable(true);
 		        
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
 					System.out.print("Error while generating script files ");
 					e1.printStackTrace();
 				}
 	       		  	  
 	      	  /*
 	      	   * running builder process by calling the generated builder.bash script
 	      	   */
 	      	  try
 	      	  {  
 	      		  System.out.println("building...");
 	      		  statusLabel.setText("creating bash script...");
      
 	      		  statusLabel.setText("building...(please wait)");
          
 	      		  System.out.print(desPath);
 	      		  String cmd = "./script/builder.bash ./keys/purple ./keys/jitsi "+ isoPath +" "+ linuxUserFld.getText()+" "+desPath ;    
 	      		  Process p = r.exec(cmd);
 	      		  System.out.println("-----------------------\n"+cmd+"\n-----------------------\n");
      
 	      		  BufferedReader stdOutput = new BufferedReader(new InputStreamReader(p.getInputStream()));
 	      		  BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
 	      		  	while(stdOutput.readLine()!= null||stdError.readLine()!= null)
 	      		  	{
 	      		  		System.out.println(stdOutput.readLine());
 	      		  		System.out.println(stdError.readLine());
 	      		  	}
 	      	  }
 	      	  catch (Exception e)
 	      	  {
 	      		  System.out.println(e.getMessage());
 	      	  }
 	      	  
 	      	  /*
 	      	   * creating cleaner.bash script to 
 	      	   * be called when the build process finished injecting keys
 	      	   */
 	          File cleanerScript = new File("./script/cleaner.bash");
 	     		cleanerScript.getParentFile().mkdirs();
 
 	          try {
 	     			if (!cleanerScript.exists()) {
 	     				cleanerScript.createNewFile();
 	         		}
 
 	         		FileWriter fw = new FileWriter(cleanerScript.getAbsoluteFile());
 	         		BufferedWriter bw = new BufferedWriter(fw);
 	         	    bw.write("#!/bin/bash \ncd BUILD\numount -l squashfs\numount -l mnt\ncd ..\nrm -r BUILD");
 	         	    bw.close();
 	         	
 	                cleanerScript.setExecutable(true);
 	     		} catch (IOException e1) {
 	     			// TODO Auto-generated catch block
 	     			System.out.print("Error while generating cleaner script files ");
 	     			e1.printStackTrace();
 	     		}
 	          
 	          /*
 	           * Cleaning BUILD folders 
 	           * this process will unmount squashfs & mnt folders 
 	           * then delete the whole BUILD folder 
 	           */
 	          statusLabel.setText("Cleaning Build folder");
 	          String cmd1 = "./script/cleaner.bash" ;
 	          Process p1;
 			try {
 				p1 = r.exec(cmd1);
 		          //p1.waitFor();
 		          BufferedReader stdOutput = new BufferedReader(new InputStreamReader(p1.getInputStream()));
 	      		  BufferedReader stdError = new BufferedReader(new InputStreamReader(p1.getErrorStream()));
 		          stdOutput = new BufferedReader(new InputStreamReader(p1.getInputStream()));
 		          stdError = new BufferedReader(new InputStreamReader(p1.getErrorStream()));
 		          while(stdOutput.readLine()!= null||stdError.readLine()!= null)
 		          {
 		            System.out.println(stdOutput.readLine());
 		          	System.out.println(stdError.readLine());
 		          }
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	                 
 		    }
 	    });
  }
 
  }
