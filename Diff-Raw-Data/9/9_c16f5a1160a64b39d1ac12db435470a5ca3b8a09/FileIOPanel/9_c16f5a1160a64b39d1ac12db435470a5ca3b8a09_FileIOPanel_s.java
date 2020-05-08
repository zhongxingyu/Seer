 package fileio;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 
 import main.God;
 import main.ShapeList;
 import data.Shape;
 import data.ShapeData;
 import data.Vertex;
 
 public class FileIOPanel extends JPanel
 {
 
 	private static final long serialVersionUID = 1L;
 	JButton newButton, saveButton, loadButton;
 	JTextArea log;
 	JFileChooser fc;
 
 	public FileIOPanel()
 	{
 		this.setPreferredSize(new Dimension(200,200));
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 
 		newButton = new JButton("New Image");
 		newButton.addActionListener(newImage());
 		add(newButton);
 
 		saveButton = new JButton("Save Session");	
 		saveButton.addActionListener(saveSession());
 		add(saveButton);
 
 		loadButton = new JButton("Load Session");
 		loadButton.addActionListener(loadSession());
 		add(loadButton);
 	}
 
 	/***
 	 * ACTIONLISTENER DEFINITIONS FOR BUTTONS
 	 */
 
 	/***
 	 * Method definition for loading a new image into the system.
 	 * @return ActionListener for newButton button
 	 */
 	ActionListener newImage()
 	{
 		return new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent arg0) 
 			{
 				if (fc == null) 
 				{
 					fc = new JFileChooser();
 
 					//Add a custom file filter
 					fc.addChoosableFileFilter(new TypeFilter(1));
 					fc.addChoosableFileFilter(new TypeFilter(0));
 					fc.setAcceptAllFileFilterUsed(true);
 
 					//Add custom icons for file types.
 					fc.setFileView(new ViewFile());
 
 					//Add the preview pane.
 					fc.setAccessory(new ImagePreview(fc));
 				}
 
 				// Show it
 				int returnVal = fc.showDialog(FileIOPanel.this, "Opening New Image");
 
 				// Process the results
 				if (returnVal == JFileChooser.APPROVE_OPTION) 
 				{
 					String file = fc.getSelectedFile().getPath();
 
 					try 
 					{
 						// TODO SAVE BEFORE NEW PROJECT
 						ShapeData shapeData = new ShapeData();
 						God.shapeData = shapeData;
 						God.imagePanel.newImage(file);
 					} 
 					catch (IOException e) 
 					{
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				//Reset the file chooser for the next time it's shown.
 				fc.setSelectedFile(null);
 			}};
 	}
 
 	/***
 	 *  Method definition for saving an image and polygon labels.
 	 * @return ActionListener for saveButton button
 	 */
 	ActionListener saveSession()
 	{
 		return new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent arg0) 
 			{
 				if (fc == null) 
 				{
 					fc = new JFileChooser();
 
 					//Add a custom file filter
 					fc.addChoosableFileFilter(new TypeFilter(1));
 					fc.addChoosableFileFilter(new TypeFilter(0));
 					fc.setAcceptAllFileFilterUsed(true);
 
 					//Add custom icons for file types.
 					fc.setFileView(new ViewFile());
 
 					//Add the preview pane.
 					fc.setAccessory(new ImagePreview(fc));
 				}
 
 				int returnVal = fc.showDialog(FileIOPanel.this, "Save Session");
 
 				if (returnVal == JFileChooser.APPROVE_OPTION) 
 				{
 					FileWriter fw = null;
 					BufferedWriter bw = null;
 					try {
 						String filePath = fc.getSelectedFile().getPath();
 						String[] fileArray = filePath.split("\\."); // want to make sure we only take name, not extension
 						if (fileArray.length > 0) {
							filePath = fileArray[0];
 						}
 						File file = new File(filePath + ".csv");
 						if (!file.exists()) {
 							file.createNewFile();
 						}
 
 						fw = new FileWriter(file.getAbsoluteFile());
 						bw = new BufferedWriter(fw);
 						for (Shape shape : God.shapeData.getShapes()) {
 							bw.write(shape.getLabel());
 							bw.write(',' + String.valueOf(shape.getColor().getRed()));
 							bw.write(',' + String.valueOf(shape.getColor().getGreen()));
 							bw.write(',' + String.valueOf(shape.getColor().getBlue()));
 							for (Vertex v : shape.getVertices()) {
 								bw.write(',' + String.valueOf(v.getX()));
 								bw.write(',' + String.valueOf(v.getY()));
 							}
 							bw.newLine();
 
 						}
 						bw.close();
 						System.out.println("Done");
 
 					} catch (IOException e) {
 						System.out.println("Error when writing to file");
 						e.printStackTrace();
 						System.out.println(e.getMessage());
 					} 
 				}
 				//Reset the file chooser for the next time it's shown.
 				fc.setSelectedFile(null);
 			}};
 	}
 
 	/***
 	 * Method definition for loading an image and polygon labels.
 	 * @return ActionListener for loadButton button
 	 */
 	ActionListener loadSession()
 	{
 		// LOAD SESSION
 		return new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent arg0) 
 			{
 				if (fc == null) {
 					fc = new JFileChooser();
 
 					//Add a custom file filter
 					fc.addChoosableFileFilter(new TypeFilter(1));
 					fc.addChoosableFileFilter(new TypeFilter(0));
 					fc.setAcceptAllFileFilterUsed(false);
 
 					//Add custom icons for file types.
 					fc.setFileView(new ViewFile());
 
 					//Add the preview pane.
 					fc.setAccessory(new ImagePreview(fc));
 				}
 
 				//Show it.
 				int returnVal = fc.showDialog(FileIOPanel.this,
 						"Loading Session");
 
 				//Process the results.
 				if (returnVal == JFileChooser.APPROVE_OPTION) 
 				{
 					try {
 						
 						// TODO FUNCTIONALITY
 						File file = fc.getSelectedFile();
 						FileInputStream fis = new FileInputStream(file);
 						DataInputStream in = new DataInputStream(fis);
 						BufferedReader br = new BufferedReader(new InputStreamReader(in));
 						ArrayList<String> shapeInfo = new ArrayList<String>();
 						String strLine;
 						while ((strLine = br.readLine()) != null) {
 							shapeInfo.add(strLine);
 						}
 						//remove old shape data
 						for (Shape s : God.shapeData.getShapes()) {
 							ShapeList.removeShape(0);
 						}
 						ShapeData shapeData = new ShapeData();
 						for (String info : shapeInfo) {
 							if (!info.contains(",")) {
 								//PROBLEM, continue for now
 								continue;
 							}
 							String[] infoArray = info.split(",");
 							//create shape object from data
 							Shape shape = new Shape();
 							shape.setLabel(infoArray[0]);
 							Color color = new Color(Integer.parseInt(infoArray[1]),
 													Integer.parseInt(infoArray[2]),
 													Integer.parseInt(infoArray[3])
 							);
 							shape.setColor(color);
 							for (int i = 4; i < infoArray.length; i+=2) {
 								Vertex v = new Vertex(	Integer.parseInt(infoArray[i]),
 														Integer.parseInt(infoArray[i+1])
 								);
 								shape.add(v);
 							}
 							shapeData.addShape(shape);
 						}
 						God.shapeData = shapeData;
 					} catch(FileNotFoundException e) {
 						
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				} 
 				else 
 				{
 				}
 				//Reset the file chooser for the next time it's shown.
 				fc.setSelectedFile(null);
 			}};
 	}
 
 
 }
