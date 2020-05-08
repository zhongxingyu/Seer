 package kc.UI;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 
 import javax.print.PrintException;
 
 import javafx.application.Platform;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.geometry.Insets;
 import javafx.geometry.Pos;
 import javafx.scene.Node;
 import javafx.scene.Scene;
 import javafx.scene.control.Button;
 import javafx.scene.control.CheckBox;
 import javafx.scene.control.ChoiceBox;
 import javafx.scene.control.Label;
 import javafx.scene.control.ScrollPane;
 import javafx.scene.image.Image;
 import javafx.scene.image.ImageView;
 import javafx.scene.input.MouseButton;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.layout.BorderPane;
 import javafx.scene.layout.HBox;
 import javafx.scene.layout.TilePane;
 import javafx.scene.layout.VBox;
 import javafx.stage.Stage;
 import kc.utils.CommonConstants;
 import kc.utils.PhotoliciousUtils;
 import kc.utils.SlideShow;
 import kc.vo.PrintServiceVO;
 import service.PrintImage;
 
 public class Home 
 {
 	Label number;
 	Label currentPrints;
 	Label newFile;
 	Label timeStamp;
 	BorderPane borderPane = null;
 	File outputFolder;
 	Stage stage;
 	List<String> list = new ArrayList<String>();
 	Map<String, Label> filePrints = new HashMap<String, Label>();
 	VBox imageViewBox = new VBox(20);
 	
 	PrintImage printImage;
 	SlideShow slideShow;
 	ExecutorService exec = null;
 	
 	SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
 
 	public Home()
 	{
 		slideShow = new SlideShow();
 		number = new Label("0");
 		currentPrints = new Label("0");
 		printImage = new PrintImage();
 		borderPane = new BorderPane();
 		newFile = new Label();
 		timeStamp = new Label();
 		this.outputFolder = new File(PhotoliciousUtils.readOutputFolder());
 	}
 	public Home(File outPutFolder)
 	{
 		borderPane = new BorderPane();
 		this.outputFolder = outPutFolder;
 		
 	}
 	public BorderPane showHome(Stage stage, ExecutorService exec)
 	{
 		this.stage = stage;
 		this.exec = exec;
 		borderPane.setLeft(leftPane());
 		borderPane.setCenter(viewGallery(stage));
 		return borderPane;
 	}
 	
 	public VBox leftPane()
 	{
 		VBox finalBox = null;
 		VBox detailsBox =null;
 		VBox printOptionsBox =null;
 		
 		try{
 			finalBox = new VBox(30);
 			finalBox.setMaxWidth(300);
 			finalBox.setId("finalBox");
 			
 			detailsBox = new VBox();
 			detailsBox.setAlignment(Pos.TOP_LEFT);
 			detailsBox.setPadding(new Insets(20, 30, 20, 30));
 			detailsBox.setSpacing(15);
 			
 			
 			
 			
 			printOptionsBox = new VBox(20);
 			printOptionsBox.setPadding(new Insets(20, 30, 20, 30));
 			printOptionsBox.setAlignment(Pos.CENTER);
 			printOptionsBox.setId("printOptionsBox");
 			
 			Button beginSlideshow = new Button("Begin Slideshow");
 			beginSlideshow.setId("buttonStMac");
 			beginSlideshow.setOnAction(new EventHandler<ActionEvent>() {
 				
 				@Override
 				public void handle(ActionEvent arg0) {
 					slideShow.start(outputFolder, stage);
 				}
 			});
 			
 			
 			Button printSelected = new Button("Print Selected");
 			printSelected.setId("buttonStMac");
 			printSelected.setOnAction(new EventHandler<ActionEvent>() {
 				
 				@Override
 				public void handle(ActionEvent event) {
 					
 					
 					Platform.runLater(new Runnable() {
 				        @Override
 				        public void run() {
 					
 								BorderPane borderPane = new BorderPane();
 								final Stage newStage = new Stage();
 				            	newStage.setWidth(300);
 				            	newStage.setHeight(200);
 				            	newStage.setTitle("Print");
 				            	Scene scene = new Scene(borderPane);
 				            	scene.getStylesheets().add(this.getClass().getClassLoader().getResource("kc/css/home.css").toString());
 				            	newStage.setScene(scene);
 				                newStage.show();
 								
 								
 								if(imageViewBox.getChildren().size()!=0)
 								{
 									
 									VBox vBox = new VBox(20);
 									vBox.setAlignment(Pos.CENTER);
 									HBox hBox = new HBox(20);
 									hBox.setAlignment(Pos.CENTER);
 					            	Label selectprinter = new Label("Printer");
 					            	final ChoiceBox<PrintServiceVO> printList = new ChoiceBox<PrintServiceVO>(printImage.printerList());
					            			            	
					            	
					            	//Choose default printer here
 					            	printList.getSelectionModel().selectFirst();
					            	printList.setMaxWidth(200);
 					            	hBox.getChildren().addAll(selectprinter, printList);
 					            	
 					            	final CheckBox checkBox = new CheckBox("Set as Default");
					            	//Implement save Default Printer here.
					            	
					            	
 					            	
 					            	Button finalPrint = new Button("Print");
 					            	finalPrint.setOnAction(new EventHandler<ActionEvent>() {
 										
 										@Override
 										public void handle(ActionEvent event1) {
 											
 											if(!checkBox.selectedProperty().getValue())
 											{
 												try {
 													printImage.print((((ImageView)imageViewBox.getChildren().get(0)).getImage().impl_getUrl()).substring(5), 
 															printList.getSelectionModel().getSelectedItem().getPrintService(), newStage);
 													currentPrints.setText(String.valueOf((Integer.parseInt(currentPrints.getText())+1)));	
 												} catch (FileNotFoundException e) {
 													// TODO Auto-generated catch block
 													e.printStackTrace();
 												} catch (InterruptedException e) {
 													// TODO Auto-generated catch block
 													e.printStackTrace();
 												} catch (PrintException e) {
 													// TODO Auto-generated catch block
 													e.printStackTrace();
 												}
 											}
 											
 										}
 									});
 					            	
 					            	
 					            	vBox.getChildren().addAll(hBox,checkBox, finalPrint);
 					            	
 					            	
 					            	
 					            	borderPane.setCenter(vBox);
 					            	
 								
 								
 								
 									String url = (((ImageView)imageViewBox.getChildren().get(0)).getImage().impl_getUrl()).substring(5);
 									File file = new File(url);
 									filePrints.get(file.getName()).setText(String.valueOf(Integer.parseInt(filePrints.get(file.getName()).getText())+1));
 											
 								}
 								else
 								{
 									VBox vBox = new VBox(20);
 									vBox.setAlignment(Pos.CENTER);
 									//vBox.setPadding(new Insets(20));
 							
 									Button button = new Button("OK");
 									
 									button.setOnAction(new EventHandler<ActionEvent>() {
 										
 										@Override
 										public void handle(ActionEvent event) {
 											
 											newStage.close();
 										}
 									});
 									
 									vBox.getChildren().addAll(new Label("Please Select An Image !"), button);
 									borderPane.setCenter(vBox);
 								}
 						}
 					});
 								
 				}
 			});
 			
 			
 			printOptionsBox.getChildren().addAll(printSelected, beginSlideshow);
 			
 			
 			imageViewBox.setId("imgViewBox");
 			imageViewBox.setAlignment(Pos.CENTER_RIGHT);
 			imageViewBox.setMaxWidth(230);
 			//imageViewBox.setMaxHeight(finalBox.getHeight() - (detailsBox.getHeight() + printOptionsBox.getHeight() + 15));
 			
 	
 			
 			outputFolder = new File(PhotoliciousUtils.readOutputFolder());
 			
 			
 			Label noOfPhotos = new Label(CommonConstants.noOfPics);
 			noOfPhotos.setId("label1");
 			
 			
 			Label noOfPrints = new Label(CommonConstants.noOfPrints);
 			noOfPrints.setId("label1");
 			/*
 			 * ..Include the prints..
 			 */
 			//Label currentPrints = new Label(String.valueOf(0));
 			
 			Label newestFileLabel = new Label(CommonConstants.newestPic);
 			newestFileLabel.setId("label1");
 			
 			
 			Label timeStampLabel = new Label(CommonConstants.photoTimestamp);
 			timeStampLabel.setId("label1");
 			
 			
 			detailsBox.getChildren().addAll(noOfPhotos, number, noOfPrints, currentPrints, newestFileLabel, newFile, timeStampLabel, timeStamp);
 			
 			finalBox.getChildren().addAll(detailsBox, printOptionsBox, imageViewBox);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		return finalBox;
 	}
 	
 	public ScrollPane viewGallery(final Stage stage)
 	{
 		ScrollPane root = null;
 		final TilePane tile = new TilePane();
 	
 		
 		try{
 			root = new ScrollPane();
 			root.setStyle("-fx-background-color: DAE6F3;");
 			tile.setPadding(new Insets(15, 15, 15, 15));
 			tile.setTileAlignment(Pos.CENTER);
 			tile.setAlignment(Pos.TOP_LEFT);
 
 			tile.setVgap(4);
 		    tile.setHgap(4);
 		    tile.setStyle("-fx-background-color: DAE6F3;");
 		    
 		    exec.execute(new Runnable() {
 
                 @Override
                 public void run() {
                 	outputFolder = new File(PhotoliciousUtils.readOutputFolder());
                 	while(!Thread.interrupted())
                 	{
                 	final File[] listOfFiles = PhotoliciousUtils.filterJPEGImagesFromFolder(outputFolder.listFiles());
         			
                     Platform.runLater(new Runnable() {
 
                         @SuppressWarnings("deprecation")
 						@Override
                         public void run() {
                         	number.setText(String.valueOf(listOfFiles.length));
                         	if(list.size()!=
                         			listOfFiles.length)
                         	{
                         		if(list.size()>listOfFiles.length)
                         		{
                         			list.clear();
                         			list = PhotoliciousUtils.nameOfFiles(listOfFiles);
                         			for(int i=0;i<tile.getChildren().size();i++)
                         			{
                         				VBox vBox = (VBox)tile.getChildren().get(i);
                         				if(!list.contains(new File(((ImageView)(vBox.getChildren().get(0))).getImage().impl_getUrl().substring(5)).getName()))
                         				{
                         					tile.getChildren().remove(i);
                         					break;
                         				}
                         			}
                         		}
                         		else
                         		{
                         		for (File file : listOfFiles) {
                         			if(!list.contains(file.getName())){
                         				newFile.setText(file.getName());
                         				timeStamp.setText(sdf.format(file.lastModified()));
 		                				System.out.println(file.getPath());
 		                				final Image image = new Image("file:"+file.getPath());
 		                				final VBox vBox = new VBox();
 		                				vBox.setAlignment(Pos.BOTTOM_CENTER);
 		                				vBox.setId("Images");
 		                				
 		                				filePrints.put(file.getName(), new Label("0"));
 		                				final ImageView iv2 = new ImageView();
 		                				iv2.setImage(image);
 		                				iv2.setFitWidth(150);
 		                				iv2.setPreserveRatio(true);
 		                				iv2.setSmooth(true);
 		                				iv2.setCache(true);
 		                				vBox.getChildren().addAll(iv2, 
 		                						new Label((file.getName().length()) > 20 ? file.getName().substring(0,10)+".." : file.getName()),
 		                						filePrints.get(file.getName()));
 		                				tile.getChildren().add(vBox);
 		                				list.add(file.getName());
 		                				
 		                				
 		                				
 		                				iv2.setOnMouseClicked(new EventHandler<MouseEvent>() {
 		                						
 											@Override
 											public void handle(MouseEvent mouseEvent) {
 												
 												if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
 										            
 													
 									            	
 													if(mouseEvent.getClickCount() == 1 ){
 														
 														ImageView imageView = new ImageView();
 										            	imageView.setImage(image);
 										            	
 														/*imageView.setFitHeight(imageViewBox.getMaxHeight() - 10);*/
 														imageView.setFitWidth(imageViewBox.getMaxWidth() - 10);
 														imageView.setPreserveRatio(true);
 														imageView.setSmooth(true);
 														imageView.setCache(true);
 														imageViewBox.getChildren().clear();
 														imageViewBox.getChildren().add(imageView);
 														
 														
 														for(Node node : tile.getChildren())
 														{
 															if(node.getId().equals("imgShow"))
 																node.setId("noCss");
 														}
 														vBox.setId("imgShow");
 														//currentPrints.setText(filePrints.get(new File((((ImageView)imageViewBox.getChildren().get(0)).getImage().impl_getUrl()).substring(5)).getName()).getText()); 
 														
 													
 													}
 													
 													else if(mouseEvent.getClickCount() == 2){
 
 										            	BorderPane borderPane = new BorderPane();
 										            	ImageView imageView = new ImageView();
 										            	imageView.setImage(image);
 										            	imageView.setFitHeight(stage.getHeight() - 10);
 														imageView.setPreserveRatio(true);
 														imageView.setSmooth(true);
 														imageView.setCache(true);
 										            	borderPane.setCenter(imageView);
 										            	Stage newStage = new Stage();
 										            	newStage.setWidth(stage.getWidth());
 										            	newStage.setHeight(stage.getHeight());
 										            	newStage.setTitle(new File((((ImageView)imageViewBox.getChildren().get(0)).getImage().impl_getUrl()).substring(5)).getName());
 										            	newStage.setScene(new Scene(borderPane));
 										                newStage.show();
 										                
 										            }
 										            
 										           
 												}
 											}
 										});
 		                				
 		                				
 		                				
 		                				
                         			}
                         		}
                         	}
                         	}
                         }
                     });
                     try {
 						Thread.sleep(5000);
 					} catch(InterruptedException ex)
 					{
 						Thread.currentThread().interrupt();
 						return;
 					}catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
                 }
                 	
                 }
             });
 		    
 		   
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 		}
         
 		root.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);    // Horizontal scroll bar
 		root.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);    // Vertical scroll bar
 		//	root.setFitToHeight(true);
 		root.setFitToWidth(true);
         root.setContent(tile);	
 		
         return root;
 	}
 	
 	public BorderPane showEmptyHome(Stage stage)
 	{
 		BorderPane borderPane = null;
 		try{
 			borderPane = new BorderPane();
 			Label label = new Label(CommonConstants.noOutPutFolder);
 			label.setId("noOutputFolderLabel");
 			borderPane.setCenter(label);
 			borderPane.setAlignment(label, Pos.CENTER);
 		}
 		catch (Exception e) {
 			// TODO: handle exception
 		}
 		return borderPane;
 	}	
 }
