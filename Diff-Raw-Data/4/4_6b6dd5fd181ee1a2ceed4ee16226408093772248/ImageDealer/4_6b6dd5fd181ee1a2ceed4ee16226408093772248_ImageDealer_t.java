 package image;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.io.File;
 import java.io.IOException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JRadioButton;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.SpinnerListModel;
 import javax.swing.SpringLayout;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import json.JSONArray;
 import json.JSONException;
 import json.JSONObject;
 import main.Errors;
 import main.MultiDB;
 import main.ProgressBarWindow;
 import music.db.Disc;
 
 import org.htmlparser.Node;
 import org.htmlparser.Parser;
 import org.htmlparser.nodes.TagNode;
 import org.htmlparser.util.NodeIterator;
 import org.htmlparser.util.NodeList;
 
 import web.WebReader;
 
 
 
 public class ImageDealer {
 	
 	public static final int FRONT_COVER = 0;
 	public static final int BACK_COVER = 1;
 	public static final String FRONT_STRING = "front";
 	public static final String BACK_STRING = "back";
     public static final Dimension COVERS_DIM = new Dimension(400,400);
     public static final Dimension MAX_COVERS_DIM = new Dimension(1200,800);
     public static final int SEEK_NUMBER_LOW = 4;
     public static final int SEEK_NUMBER_MAX = 8;
     public static final String BING_SEARCH = "Bing";
     public static final String COVER_PARADIES_SEARCH = "Paradies";
     
     private static final String NORTH = SpringLayout.NORTH;
     private static final String SOUTH = SpringLayout.SOUTH;
     private static final String WEST = SpringLayout.WEST;
     private static final String EAST = SpringLayout.EAST;
     
 	private static final String bingUrl1=MultiDB.webBing1;
 	private static final String bingUrl2=MultiDB.webBing2;
 	private static final String coverParadiesURL="http://ecover.to/";
	private static final String coverParadiesURLSearch=coverParadiesURL+"Lookup.html";
 	
 
 	public static boolean frontCover=true;
 	public static boolean otherCover=false;
     private Disc currentDisc;
     private MultiDBImage multiIm;
     private JLabel currentLabel; 
     private File currentPath;
     
     protected JLabel selectCoversView;
     protected JFrame selectCoverFrame;
     protected JSpinner spinnerCovers;
     protected JButton saveCoverButton,deleteCoverButton;
     protected JRadioButton backRButton,frontRButton,otherRButton;
     protected JLabel nofPicLabel,picNLabel;
     protected JTextField newNameField;
     protected SpinnerListModel spinnerCoversM;
     protected ArrayList<MultiDBImage> imageList = new ArrayList<MultiDBImage>();
 	protected String bingUrl;
 	protected ViewCoverHandler viewHandler;   
 	
 	
     
     public ImageDealer() {
     	bingUrl = bingUrl1+SEEK_NUMBER_LOW+bingUrl2;
     	selectFrameInit();
 	}
     
     public ImageDealer(int seekNumber) {
     	if (seekNumber>SEEK_NUMBER_MAX) seekNumber=SEEK_NUMBER_MAX;
     	if (seekNumber<1) seekNumber=SEEK_NUMBER_LOW;
     	bingUrl = bingUrl1+seekNumber+bingUrl2;
     	selectFrameInit();
 	}
     
     public ImageDealer(Disc disc) {
     	currentDisc=disc;
     	selectFrameInit();
 	}
     
     public void setDisc(Disc disc){
     	currentDisc=disc;
     }
     
     public MultiDBImage getImage(){
     	return multiIm;
     }
 
 
 	public void selectFrameInit(){
         ///////////setting icons for pictures
 		frontCover=true;
         multiIm = new MultiDBImage();
         if (selectCoverFrame!=null) selectCoverFrame.dispose();
 	    selectCoverFrame = new JFrame("Select a picture");
 	    selectCoverFrame.setSize(500, 580);
 	    selectCoversView = new JLabel();
 	    selectCoversView.setMinimumSize(COVERS_DIM);	    
 	    
 	    spinnerCoversM = new SpinnerListModel();	
 	    try{
 	    	spinnerCoversM.setList(imageList);
 	    }catch(IllegalArgumentException ilex){
 	    	//do nothing
 	    	//this exception breaks when the list has 0 members, like the first time this function is invoked
 	    }
 	    spinnerCovers = new JSpinner(spinnerCoversM);
 	    JComponent field = ((JSpinner.DefaultEditor) spinnerCovers.getEditor());
 	    Dimension prefSize = field.getPreferredSize();
 	    prefSize = new Dimension(300, prefSize.height);
 	    field.setPreferredSize(prefSize);
 	    saveCoverButton = new JButton("Save current cover");
 	    deleteCoverButton = new JButton("Delete current cover");
 	    //handler to view covers on selectFrameCover
         viewHandler = new ViewCoverHandler();
         spinnerCovers.addChangeListener(viewHandler);
 	    SaveCurrentCoverHandler saveCurrentCoverHandler = new SaveCurrentCoverHandler();
 	    saveCoverButton.addActionListener(saveCurrentCoverHandler);
 	    DeleteCurrentCoverHandler deleteCurrentCoverHandler = new DeleteCurrentCoverHandler();
 	    deleteCoverButton.addActionListener(deleteCurrentCoverHandler);
 
 	    //Create the radio buttons.
 	    frontRButton = new JRadioButton("Front");
 	    frontRButton.setSelected(true);
 
 	    backRButton = new JRadioButton("Back");
 	    otherRButton = new JRadioButton("Other");
 
 	    //Group the radio buttons.
 	    ButtonGroup frontBackSelect = new ButtonGroup();
 	    frontBackSelect.add(frontRButton);
 	    frontBackSelect.add(backRButton);
 	    frontBackSelect.add(otherRButton);
 	    
 	    SelectTypeCoverHandler selectTypeCoverHandler = new SelectTypeCoverHandler();
 	    frontRButton.addActionListener(selectTypeCoverHandler);
 	    backRButton.addActionListener(selectTypeCoverHandler);
 	    otherRButton.addActionListener(selectTypeCoverHandler);
 	    
 	    nofPicLabel = new JLabel("Number of pics: ");
 	    //picNLabel = new JLabel("Pic number: ");
 	    newNameField = new JTextField(30);	    
 	    newNameField.setEnabled(false);
 	    
 	    //set layout
 	    SpringLayout layout = new SpringLayout();
 	    selectCoverFrame.getContentPane().setLayout(layout);
 	    
 	    
 	    
 	    selectCoverFrame.getContentPane().add(spinnerCovers);
 	    selectCoverFrame.getContentPane().add(frontRButton);
 	    selectCoverFrame.getContentPane().add(backRButton);
 	    selectCoverFrame.getContentPane().add(otherRButton);
 	    selectCoverFrame.getContentPane().add(nofPicLabel);
 	    //selectCoverFrame.getContentPane().add(picNLabel);
 	    selectCoverFrame.getContentPane().add(newNameField);
 	    selectCoverFrame.getContentPane().add(saveCoverButton);
 	    selectCoverFrame.getContentPane().add(deleteCoverButton);
 	    
 	    layout.putConstraint(NORTH, spinnerCovers,0, NORTH, selectCoverFrame.getContentPane());
 	    layout.putConstraint(WEST, spinnerCovers,0, WEST, selectCoverFrame.getContentPane());
 	    layout.putConstraint(NORTH, frontRButton,3, SOUTH, spinnerCovers);
 	    layout.putConstraint(NORTH, backRButton,3, SOUTH, frontRButton);
 	    layout.putConstraint(NORTH, otherRButton,3, SOUTH, backRButton);
 	    layout.putConstraint(NORTH, saveCoverButton,3, SOUTH, spinnerCovers);
 	    layout.putConstraint(WEST, saveCoverButton,50, EAST, frontRButton);
 	    layout.putConstraint(NORTH, deleteCoverButton,3, SOUTH, saveCoverButton);
 	    layout.putConstraint(WEST, deleteCoverButton,50, EAST, frontRButton);    
 	    layout.putConstraint(NORTH, nofPicLabel,3, SOUTH, spinnerCovers);
 	    layout.putConstraint(WEST, nofPicLabel,50, EAST, deleteCoverButton);
 	    //layout.putConstraint(NORTH, picNLabel,3, SOUTH, nofPicLabel);
 	    //layout.putConstraint(WEST, picNLabel,50, EAST, deleteCoverButton);
 	    layout.putConstraint(NORTH, newNameField,3, SOUTH, otherRButton);
 	    layout.putConstraint(SOUTH, selectCoversView,-3, SOUTH, selectCoverFrame.getContentPane());
 	    layout.putConstraint(WEST, selectCoversView,20, WEST, selectCoverFrame.getContentPane());
 	    
 	    FrameCloseHandler frameCloseHandler = new FrameCloseHandler();
 	    selectCoverFrame.addWindowListener(frameCloseHandler);
     }
     
 	private class FrameCloseHandler extends WindowAdapter{
 		@Override
 	    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
 			ImageDealer.this.showImage(ImageDealer.this.currentPath, ImageDealer.this.currentLabel, FRONT_COVER);
 		    selectCoverFrame.dispose();
 	    }
 	}
     
     public void searchImage(String name,String type){
     	selectFrameInit();
     	SearchImageInternet searchImageInternetThread = new SearchImageInternet(name);
     	searchImageInternetThread.type=type;
     	searchImageInternetThread.start();
 	}    
     
     public Dimension showCurrentImageInLabel(JLabel labelIn){
     	return multiIm.putImage(labelIn);		
     } 
     
     private ArrayList<MultiDBImage> getListOfImages(File pathDisc){
     	int numArchivos;    
     	String[] imageNamesList;
     	MultiDBImage tempIm;
     	ArrayList<MultiDBImage> imList = new ArrayList<MultiDBImage>();
     
     	imageNamesList = pathDisc.list();
 		if (imageNamesList!=null){
 			numArchivos = imageNamesList.length;
 			imList.clear();
 			
 			for (int i = 0; i < numArchivos; i++) {
 				String currentImageName = imageNamesList[i].toLowerCase();
 				if (((currentImageName.indexOf(".jpg") > -1) || (currentImageName.indexOf(".gif") > -1)|| (currentImageName.indexOf(".png")) > -1)) {
 					tempIm=new MultiDBImage();
 					tempIm.path = new File(pathDisc.getAbsolutePath() + File.separator + imageNamesList[i]);
 					tempIm.name = imageNamesList[i];
 					imList.add(tempIm);					
 				}
 			}
 		}
 		return imList;
     }
     
     public void showImages(File pathDisc){    	
     	if ((imageList=getListOfImages(pathDisc)).size()>0){
     		showListOfImages();
     	}else JOptionPane.showMessageDialog(selectCoverFrame,"Cannot find images");
     }
     
     private void showListOfImages(){
     	if (imageList.size()>0){
 			for (int i=0;i<imageList.size();i++){
 				imageList.get(i).setImageFromFile();
 			}
 			selectFrameInit();
 			nofPicLabel.setText("Number of pics: "+imageList.size());
 			spinnerCoversM.setList(imageList);
 			//System.out.println(imageListWeb.get(0).width);
 			multiIm.putImage(selectCoversView, imageList.get(0));
 			selectCoverFrame.getContentPane().add(selectCoversView);
 			selectCoverFrame.setVisible(true);
 		}
     }
     
     public boolean showImage(File pathDisc,JLabel labelIn,int type){
     	return showImage(pathDisc, labelIn, type, COVERS_DIM);
     }
     
     public boolean showImage(File pathDisc,JLabel labelIn,int type, Dimension dim){
     	int indexCover=0;
     	boolean found=false;
     	currentLabel = labelIn;
     	currentPath = pathDisc; 
     	String stringSearch="";
     	
 		imageList.clear();			
 		imageList=getListOfImages(pathDisc);
 		
 		if (imageList.size()<1) return false;
 		if (type == ImageDealer.FRONT_COVER) stringSearch=FRONT_STRING;
 		if (type == ImageDealer.BACK_COVER) stringSearch=BACK_STRING;		
 		
 		for (int i = 0; i < imageList.size(); i++) {
 			String currentImageName = imageList.get(i).name.toLowerCase();
 			if (currentImageName.indexOf(stringSearch) > -1) {
 				found = true;
 				indexCover = i;
 				break;
 			}
 		}
 			
 		if (found) {
 			if (type == FRONT_COVER) frontCover = true; else frontCover = false;
 			multiIm.putImage(labelIn, MultiDBImage.FILE_TYPE, imageList.get(indexCover).path.getAbsolutePath(), dim);
 		} else  showListOfImages();			
 		return true;
     }
     
     
     
     
   //THREAD FOR SEARCH IMAGE INTERNET////////////////////////////////////////////////////////////////////////  
     public class SearchImageInternet extends Thread {
     	   
     	private String HTMLText="";
     	private MultiDBImage tempIm;
     	private String name;
     	private String type;
     	private ProgressBarWindow pw = new ProgressBarWindow();
     	  
     	public SearchImageInternet(String name) {
     		super();
     		this.name=name;
     		this.type=COVER_PARADIES_SEARCH;
     	}
     	
     	public SearchImageInternet(String name,String type) {
     		super();
     		this.name=name;
     		this.type=type;
     	}
     	    
     	@Override
     	public void run() {    		
     	    pw.setFrameSize(pw.dimWebImageReader);
     	    pw.startProgBar(2);
 
     	    imageList.clear();
     	   
     	    if (type.compareTo(BING_SEARCH)==0) searchBing();
     	    else searchCoverParadies(0);	
     	    if (imageList.size()>0){
     	    	spinnerCoversM.setList(imageList);
 				tempIm=new MultiDBImage();
 				if (imageList.get(0).image!=null) tempIm.putImage(selectCoversView,imageList.get(0));
 				else if (imageList.get(0).thumbNail!=null) tempIm.putImage(selectCoversView,imageList.get(0).thumbNail);
 				else Errors.showWarning(Errors.IMAGE_NOT_FOUND);
 				nofPicLabel.setText("Number of pics: "+imageList.size());
 				selectCoverFrame.getContentPane().add(selectCoversView);
 				selectCoverFrame.setVisible(true);
     	    }else {
     	    	pw.setPer(2,"");
     	    	Errors.showWarning(Errors.IMAGE_NOT_FOUND);
     	    }
 			pw.setPer(2,"");			 				
     	}
     	
     	private void searchBing(){
     		 try{
     			JSONObject job;
 	    		String search=URLEncoder.encode("'"+name+"'","UTF-8");
 				String searchString = bingUrl+search;
 				//System.out.println(searchString);
 				pw.setPer(0, "Searching...");
 				HTMLText=WebReader.getHTMLfromURLHTTPS(searchString,MultiDB.webBingAccountKey);
 				pw.setPer(1, "Downloading results...");
 				if (HTMLText.compareTo("Error")==0)  Errors.showError(Errors.WEB_MALF_URL);
 				else{
 					try {
 						job = new JSONObject(HTMLText);
 						job=job.getJSONObject("d");
 							//System.out.println(job.toString());
 						JSONArray list = job.getJSONArray("results");				
 						for (int i=0;i<list.length();i++){
 							job=list.getJSONObject(i);
 							tempIm=new MultiDBImage();
 							tempIm.url=job.getString("MediaUrl");
 							tempIm.setImageFromUrl();
 							if (tempIm.image!=null){
 	    						tempIm.path=new File(ImageDealer.this.currentDisc.path+File.separator+i);
 	    						tempIm.width=job.getInt("Width");
 	    						tempIm.height=job.getInt("Height");
 	    						tempIm.fileSize=job.getInt("FileSize");
 	    						imageList.add(tempIm);
 	    					}
 						}
 						
 					} catch (JSONException e) {
 						// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			} catch (Exception e) {
 					// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
     	}
     	
     	
     	private void searchCoverParadies(Integer page){
     		Disc disc;
     		ArrayList<Disc> discList = new ArrayList<Disc>();
     		try{
  				pw.setPer(0, "Searching...");
  			    String data = URLEncoder.encode("Page", "UTF-8") + "=" + URLEncoder.encode(page.toString(), "UTF-8");
  			    data += "&" + URLEncoder.encode("SearchString", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8");
  			    //data += "&" + URLEncoder.encode("Sektion", "UTF-8") + "=" + URLEncoder.encode("", "UTF-8");
 			    System.out.println("Data to send to coverparadies "+data);
  				HTMLText=WebReader.postHTMLfromURL(coverParadiesURLSearch,data);
  				pw.setPer(1, "Downloading results...");
  				if (HTMLText.compareTo("Error")==0)  Errors.showError(Errors.WEB_MALF_URL);
  				else{
  					Parser parser;
  					parser = new Parser(HTMLText);		 				
 		 			NodeList nl = parser.parse(null); 
  					//System.out.println(HTMLText);
  					NodeList tableNodes=WebReader.getTagNodesOfType(nl,"table",true);
 		 			Node node;
 		 			NodeList links;
 		 			String classTd="";
 		 			for (NodeIterator i = tableNodes.elements (); i.hasMoreNodes ();){
 		 				node=i.nextNode();
 		 				if ((node) instanceof TagNode){
 			 				classTd=((TagNode)node).getAttribute("class");
 			 				if (classTd!=null)
 				 				if (classTd.contains("Table_SimpleSearchResult")) { 					
 				 					if (((TagNode)node).getChildren()!=null){
 				 						links=WebReader.getTagNodesOfType(((TagNode)node).getChildren(),"a",false);
 				 						if (links.size()>0){  		 			    		
 				 							for (NodeIterator itlinks = links.elements (); itlinks.hasMoreNodes (); ){
 				 								String href = ((TagNode)itlinks.nextNode()).getAttribute("href");
 				 								if (href!=null){
 				 									if (href.contains("Cover")){
 				 										//more than  1 result
 				 										String link = coverParadiesURL+href.substring(1);//get rid of /
 				 										boolean found = false;
 				 										for (int di=0;di<discList.size();di++){
 				 											if (discList.get(di).link.equalsIgnoreCase(link)){
 				 												found = true;
 				 												break;
 				 											}
 				 										}
 				 										if (!found){
 					 										disc = new Disc();
 					 										disc.setLink(link); 
 				 											discList.add(disc);			
 				 										}
 				 									}	
 				 								}	
 				 							}
 				 							break;
 				 						}	
 			 					}
 				 				
 			 				}
 		 				}
 		 			}
 
 		 			if (discList.size()>0){
 		 				for (int it=0;it<discList.size();it++){
 							disc = discList.get(it);
 							imageList.addAll(getImageFromLinkCoverParadies(WebReader.getHTMLfromURL(disc.getLink())));
 						}
 		 				if (discList.size()==12) searchCoverParadies(page+1);
 		 			}else { //web goes directly to the unique result
 		 				imageList.addAll(getImageFromLinkCoverParadies(HTMLText));
 		 			}
  			}
  			} catch (Exception e) {
  					// TODO Auto-generated catch block
  				e.printStackTrace();
  			}
     	}
     	
     	private ArrayList<MultiDBImage> getImageFromLinkCoverParadies(String HTMLText){
     		ArrayList<MultiDBImage> listTi = new ArrayList<MultiDBImage>();
     		MultiDBImage ti=null;
     		try{
  				if (HTMLText.compareTo("Error")==0)  Errors.showError(Errors.WEB_MALF_URL);
  				else{
  					Parser parser;
  					parser = new Parser(HTMLText);		 				
 		 			NodeList nl = parser.parse(null); 
  					//System.out.println(HTMLText);
  					NodeList tableNodes=WebReader.getTagNodesOfType(nl,"div",true);
 		 			Node node,nodeImg;
 		 			NodeList links,divChildren,itImg;
 		 			String classTd="";
 		 			boolean filter=false;
 		 			for (NodeIterator i = tableNodes.elements (); i.hasMoreNodes ();){
 		 				node=i.nextNode();
 		 				if (node instanceof TagNode){
 			 				classTd=((TagNode)node).getAttribute("class");
 			 				filter=false;
 			 				if (classTd!=null){
 				 				if (classTd.contains("ThumbDetails")) filter=true;
 				 				else filter=false;
 			 				}
 			 				if (filter){ 
 			 					divChildren=((TagNode)node).getChildren();
 			 					if (divChildren!=null){
 			 						links=WebReader.getTagNodesOfType(((TagNode)node).getChildren(),"a",false);
 			 						if (links.size()>0){  		 			    		
 			 							for (NodeIterator itlinks = links.elements (); itlinks.hasMoreNodes (); ){
 			 								String href = ((TagNode)itlinks.nextNode()).getAttribute("href");
 			 								if (href!=null){
 			 									ti=new MultiDBImage();
 			 									if (!href.contains("Type=Test.JPG")){
 				 									itImg=WebReader.getTagNodesOfType(((TagNode)node).getChildren(),"img",false);
 				 									for (NodeIterator iterImg = itImg.elements (); iterImg.hasMoreNodes (); ){
 				 										nodeImg=iterImg.nextNode();
 				 										if (nodeImg instanceof TagNode){
 				 											String img=((TagNode)nodeImg).getAttribute("src");
 				 											if (img!=null){
 				 												ti.setThumbNailFromUrl(coverParadiesURL+img.substring(1));
 				 												break;
 				 											}
 				 										}
 				 									}
 								 					//System.out.println(href);
 				 									href=coverParadiesURL+href.substring(1);
 				 									ti.url=href;
 				 									ti.name=href.substring(href.lastIndexOf("/")+1);
 				 									//System.out.println("URL =" + ti.url);
 				 									//System.out.println("URL =" + ti.name);
 				 									if (ti.thumbNail!=null){
 				 										ti.path=new File(ImageDealer.this.currentDisc.path+File.separator+ti.name);
 				 										listTi.add(ti);
 				 									}
 			 									}
 			 								}	
 			 								}	
 			 							}
 			 						}	
 			 					}	
 			 				}
 		 				}
 		 			}
 
  			} catch (Exception e) {
  				Errors.showWarning(Errors.GENERIC_ERROR, e.getMessage());
  				e.printStackTrace();
  			}
     		return listTi;
     	}
     }
     
     
 ///////////////////////////////////////////COVER HANDLERS///////////////////////////////
 ///////////////////////////////////////////COVER HANDLERS///////////////////////////////
 ///////////////////////////////////////////COVER HANDLERS///////////////////////////////
 
     
 private class SaveCurrentCoverHandler implements ActionListener {
 
 
     private MultiDBImage tempIm;
     	
     public void actionPerformed(ActionEvent evento) {
     	tempIm=(MultiDBImage) spinnerCovers.getValue();
     	SaveImageThread saveImageThread = new SaveImageThread(tempIm);
     	saveImageThread.setDaemon(true);
     	saveImageThread.start();
     	
     
 } //FIN HANDLER CHANGE NAME
     
 
 
 public class SaveImageThread extends Thread {
 	private MultiDBImage im;
     private String archivo,rutaArch,type,ext;
     private File file;
     private boolean success=false;
     private Disc currDisc;
     
     public SaveImageThread(MultiDBImage im) {
 		super();
 		this.im=im;
 	}
 	@Override
 	public void run() {
 		file = im.path;
 		currDisc= new Disc(currentDisc);
     	rutaArch = currDisc.path.getAbsolutePath();
     	archivo=file.getName();
     	int pos = archivo.lastIndexOf('.');
     	if (pos>0) ext = "."+archivo.substring(pos+1);
     	else ext=".jpg";
     	im.type=ext.substring(1);
     	try{	    	    	
 	    	if (!file.canWrite()) {
 	    		if (!file.createNewFile()) JOptionPane.showMessageDialog(selectCoverFrame, "Could not rename file");
 	    		else  {
 	    			im.writeImageToFile();
 	    			success=true;
 	    		}
 	    	} else success=true;
 	    
 	    	if (success){
 	    		File nfile;
 		    	if (frontCover) type="front"; 
 		    	else if (!otherCover) type="back";
 		    	else type="other";
 		    	String name;
 		    	if (type.compareTo("other")==0){
 		    		name=rutaArch + File.separator + im +ext;
 		    		if (newNameField.getText().compareTo("")!=0) name=rutaArch + File.separator + newNameField.getText() +ext;
 		    	}else name=rutaArch + File.separator + currDisc.group + " - " + currDisc.title + " - " + type+ext;
 		    	nfile= new File(name);
 	    		if (file.renameTo(nfile)) JOptionPane.showMessageDialog(selectCoverFrame, "File renamed succesfully to "+name);
 	    	    else JOptionPane.showMessageDialog(selectCoverFrame, "Could not rename file");	    	
 	    	}
     	}catch(IOException e){
     		Errors.showWarning(Errors.IMAGE_NOT_SAVED, e.getMessage());
 			//e.printStackTrace();
 		}
     }
 	}
 }
 
 
 private class DeleteCurrentCoverHandler implements ActionListener {
     private MultiDBImage tempIm,newIm;
     private File file;
     
 	public void actionPerformed(ActionEvent e) {
 		tempIm=(MultiDBImage) spinnerCovers.getValue();		
 		if (imageList.size()>1){
 			imageList.remove(tempIm);
 			selectFrameInit();
 			newIm=new MultiDBImage();
 			newIm.putImage(selectCoversView,imageList.get(0));
 			nofPicLabel.setText("Number of pics: "+imageList.size());
 			selectCoverFrame.getContentPane().add(selectCoversView);
 			selectCoverFrame.setVisible(true);
 		} else{
 			imageList.clear();
 			selectCoverFrame.dispose();
 		}
 		
     	file = tempIm.path;
     	
 		if(!file.delete()) {
 		    // Deletion failed
 			Errors.showWarning(Errors.FILE_DELETE_ERROR);
 		}
 	}
 } //FIN SELECT TYPE COVER
 
 
 private class SelectTypeCoverHandler implements ActionListener {
 
 	public void actionPerformed(ActionEvent e) {
 		
 		if (e.getActionCommand().compareTo("Back")==0) {
 			frontCover=false;
 			otherCover=false;
 			newNameField.setEnabled(false);
 		}
 		else if (e.getActionCommand().compareTo("Front")==0) {
 			frontCover=true;
 			otherCover=false;
 			newNameField.setEnabled(false);
 		}else{
 			frontCover=false;
 			otherCover=true;
 			newNameField.setEnabled(true);
 		}
 	}
 } //FIN SELECT TYPE COVER
 
 
    
 private class ViewCoverHandler implements ChangeListener {
 
     // manejar evento de cambio en lista
     public void stateChanged(ChangeEvent e) {
         JSpinner spinner = (JSpinner) e.getSource();
         //int lenght=0;
         try{
         	
         	multiIm.putImage(selectCoversView, ((MultiDBImage) spinner.getValue()));
         }catch(IndexOutOfBoundsException ex){
         	Errors.writeError(Errors.GENERIC_ERROR, ex.toString());
         }
     }
 } //FIN HANDLER VIEW COVERS
 
 }
