 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JFrame;
 import javax.swing.JComponent;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SpringLayout;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.TextField;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.sql.Date;
 import java.util.Iterator;
 import java.util.Vector;
  
 public class AdminGUI extends JPanel implements ActionListener,KeyListener {
 	
 	
 	JButton searchButton = new JButton("Search");
 	JTextField textField = new JTextField("");
 	JFrame popup = new JFrame();
 	
 	JButton saveButton = new JButton("Save");
 	JButton deleteButton = new JButton("Delete");
 	
 	JList table = new JList();
 	db_helper db = new db_helper();
 	DefaultListModel listModel;
 	
 	Object popupData;
 	
 	Vector<Object> sideList = new Vector<Object>();
 	
 	//text fields for movie
 	JTextField movieTitle;
 	JTextField genre;
 	JTextField runtime;
 	JTextField rating;
 	JTextField productionYear;
 	JTextField totalGross;
 	JTextField openingWeekend;
 	JTextField release;
 	JTextField plotLabel;
 	
 	//text fields for person
 	JTextField name;
 	JTextField gender;
 	
 	//for characters
 	JTextField charName = new JTextField(" ");
 	
 	//text fields for company
 	JTextField compName;
 	JTextField loc;
 	
 	
 	
 	//Swing components for adding movies
 	JTextField actorName = new JTextField();
 	JTextField actorGender = new JTextField();
 	JTextField actorRole = new JTextField();
 	
 	//director
 	JTextField directorName = new JTextField();
 	JTextField directorGender = new JTextField();
 	
 	//producer
 	JTextField producerName = new JTextField();
 	JTextField producerGender = new JTextField();
 	
 	//writers
 	JTextField writerName = new JTextField();
 	JTextField writerGender = new JTextField();
 	
 	//gross
 	JTextField totalGrossField = new JTextField();
 	JTextField openingWeekendGrossField = new JTextField();
 	
 	//inputDate
 	JTextField inputDate = new JTextField();
 	
 	//production
 	JTextField inputProdCompName = new JTextField();
 	JTextField inputProdCompLoc = new JTextField();
 	
 	//movieData
 	JTextField inputTitle = new JTextField("TITLE");
 	JTextField inputGenre = new JTextField("GENRE");
 	JTextField inputRuntime = new JTextField("RUNTIME");
 	JTextField inputRating = new JTextField("RATING");
 	JTextField inputProductionYear = new JTextField("PRODUCTION YEAR");
 	JTextField inputTotalGross = new JTextField("TOTAL GROSS");
 	JTextField inputOpeningWeekend = new JTextField("OPENING WEEKEND");
 	JTextField inputRelease = new JTextField("RELEASE DATE");
 	JTextField inputPlot = new JTextField("PLOT");
 	
 	JButton addMovie = new JButton("Add Movie");
 	
     public AdminGUI() {
         super(new GridLayout(1, 1));
     	
         JTabbedPane tabbedPane = new JTabbedPane();
         ImageIcon icon = createImageIcon("images/middle.gif");
          
         //retrieve panel
         JComponent panel1 = makeListPanel();
         tabbedPane.addTab("Retrieve/Update", icon,panel1,"Search DB");
         tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
          
         //insert panel
         JComponent panel2 = makeGridPanel();
         tabbedPane.addTab("Insert Movie", icon, panel2,"Add movies to database");
         tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
         
          
         //Add the tabbed pane to this panel.
         add(tabbedPane);
          
         //The following line enables to use scrolling tabs.
         tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
         
 		MouseListener mouseListener = new MouseAdapter(){
 			public void mouseClicked(MouseEvent mouseEvent) {
 				mouseEvent.getSource();
 		        if (mouseEvent.getClickCount() == 2) {
 		          int index = table.locationToIndex(mouseEvent.getPoint());
 		          if (index >= 0) {
 		        	  System.out.println("index = " + index);
 		            Object o = table.getModel().getElementAt(index);
 		            Object p = listModel.getElementAt(index);
 		            Object z = sideList.elementAt(index);
 		            System.out.println(z.toString());
 		            popupData = z;
 		            createDialog(z);
 		            //System.out.println(p.toString());
 		            //System.out.println("Double-clicked on: " + o.toString());
 		          }
 		        }
 		      }
 		};
 		
 		table.addMouseListener(mouseListener);
 		textField.addKeyListener(this);
 		searchButton.addActionListener(this);
 		searchButton.addKeyListener(this);
 		
         textField.requestFocusInWindow();
         
         saveButton.addActionListener(this);
         deleteButton.addActionListener(this);
         
         addMovie.addActionListener(this);
         
         
     }
 	protected JComponent makeListPanel(){
 		JPanel panel = new JPanel(false);
 		panel.setLayout(new BorderLayout());
 		panel.add(textField,BorderLayout.PAGE_START);
 		panel.add(searchButton,BorderLayout.LINE_START);
 		panel.add(table,BorderLayout.CENTER);
 		panel.add(new JScrollPane(table));
 		return panel;
 	}
 	
 	protected JComponent makeGridPanel(){
 		JPanel panel = new JPanel(false);
 		panel.setLayout(new GridLayout(30,2));
 		
 		
 		//movieData
 		panel.add(new JLabel("Title: "));
 		panel.add(inputTitle);
 		
 		panel.add(new JLabel("Genre: "));
 		panel.add(inputGenre);
 		
 		panel.add(new JLabel("Runtime: "));
 		panel.add(inputRuntime);
 		
		panel.add(new JLabel("Rating:  (G/PG/PG-13/R/NC-17)"));
 		panel.add(inputRating);
 		
 		panel.add(new JLabel("Production Year: "));
 		panel.add(inputProductionYear);
 				
 		panel.add(new JLabel("Release Date: (YYYY-MM-DD) : "));
 		panel.add(inputRelease);
 		
 		panel.add(new JLabel("Plot: "));
 		panel.add(inputPlot);
 		
 		//actor
 		panel.add(new JLabel("Actor Name: "));
 		panel.add(actorName);
 		panel.add(new JLabel("Actor Gender: "));
 		panel.add(actorGender);
 		panel.add(new JLabel("Actor Role: "));
 		panel.add(actorRole);
 		
 		//director
 		panel.add(new JLabel("Director Name: "));
 		panel.add(directorName);
 		panel.add(new JLabel("Director Gender: "));
 		panel.add(directorGender);
 		
 		//producer
 		panel.add(new JLabel("Producer Name: "));
 		panel.add(producerName);
 		panel.add(new JLabel("Producer Gender: "));
 		panel.add(producerGender);
 		
 		//writers
 		panel.add(new JLabel("Writer Name: "));
 		panel.add(writerName);
 		panel.add(new JLabel("Writer Gender: "));
 		panel.add(writerGender);
 		
 		//gross
 		panel.add(new JLabel("Total Gross: "));
 		panel.add(totalGrossField);
 		panel.add(new JLabel("Opening Weekend Gross: "));
 		panel.add(openingWeekendGrossField);
 		
 		//inputDate
 //		panel.add(new JLabel("Release Day: "));
 //		panel.add(inputDate);
 		
 		//production
 		panel.add(new JLabel("Production Company Name: "));
 		panel.add(inputProdCompName);
 		panel.add(new JLabel("Produnction Company Location: "));
 		panel.add(inputProdCompLoc);
 		
 		panel.add(addMovie);
 		return panel;
 	}
 
      
     /** Returns an ImageIcon, or null if the path was invalid. */
     protected static ImageIcon createImageIcon(String path) {
         java.net.URL imgURL = TabbedPaneDemo.class.getResource(path);
         if (imgURL != null) {
             return new ImageIcon(imgURL);
         } else {
             System.err.println("Couldn't find file: " + path);
             return null;
         }
     }
     
     
     public void actionPerformed(ActionEvent e) {
 
 		if (e.getSource() instanceof JButton) {
 			JButton clickedButton = (JButton)e.getSource();
 			//adding info to database one huge chunk at a time
 			if (clickedButton.equals(addMovie)){
 				//i hope this works
 				Vector<String[]> actors = new Vector<String[]>();
 				actors.add(new String[]{actorName.getText(), actorGender.getText(),actorRole.getText()});
 				
 				Vector<String[]> directors = new Vector<String[]>();
 				directors.add(new String[]{directorName.getText(),directorGender.getText()});
 				
 				Vector<String[]> producers = new Vector<String[]>();
 				producers.add(new String[]{producerName.getText(),producerGender.getText()});
 				
 				Vector<String[]> writers = new Vector <String[]>();
 				writers.add(new String[]{writerName.getText(),writerGender.getText()});
 				
 				Vector<String[]> pc = new Vector<String[]>();
 				pc.add(new String[]{inputProdCompName.getText(),inputProdCompLoc.getText()});
 				
 				String totalGross = inputTotalGross.getText();
 				String opening_weekend_gross = inputOpeningWeekend.getText();
 				
 				String genre = inputGenre.getText();
 				String inputName = inputTitle.getText();
 				String rating = inputRating.getText();
 				String plot = inputPlot.getText();
 				int runtime = Integer.parseInt(inputRuntime.getText());
 				int production_year = Integer.parseInt(inputProductionYear.getText());
 				int year = Integer.parseInt(inputRelease.getText().substring(0, 3));
 				int month = Integer.parseInt(inputRelease.getText().substring(5, 6));
 				int day = Integer.parseInt(inputRelease.getText().substring(8,9));
				Date opening_day = new Date(year,month,day);
 				
 				//adds movie to DB
 				db.addMovie(inputName, actors, directors, producers, writers, totalGross, opening_weekend_gross, (java.sql.Date) opening_day, pc, genre, rating, plot, runtime, production_year);
 				
 				
 				
 				
 			}
 			if (clickedButton.equals(saveButton)){
 				System.out.println("lets save data for " + popupData.toString());
 				saveData();
 			}
 			if(clickedButton.equals(deleteButton)){
 				System.out.println("lets delete data for " + popupData.toString());
 				popup.setVisible(false);
 				
 				String data = popupData.toString();
 				String id = data.substring(0,data.length()-1);
 				char type = data.charAt(data.length() -1 );
 				
 			
 				if(type == 'm'){
 					db.deleteData("movie",id);
 				}
 				if(type == 'p'){
 					db.deleteData("person", id);
 				}if(type == 'c'){
 					db.deleteData("characters", id);
 				}if(type == 's'){
 					db.deleteData("production_companies",id);
 				}
 			}
 			if (clickedButton.equals(searchButton)) {
 				
 				try{
 					
 					String query = textField.getText();
 					String[] tmp_array;
 					
 					
 					listModel = new DefaultListModel<String>();
 					sideList = new Vector<Object>();
 					
 					Vector<movie> movieResults = new Vector<movie>(); 
 					movieResults = db.getMovieData(query);
 				
 					Vector<person> peopleResults = new Vector<person>();
 					peopleResults = db.getPeopleData(query);
 				
 					Vector<character> charResults = new Vector<character>();
 					charResults = db.getCharacterData(query);
 					
 					Vector<production_company> prodResults = new Vector<production_company>();
 					prodResults = db.getProductionCompaniesbyName(query);
 				
 					
 					//convert movie objects to just titles
 					Iterator itr = movieResults.iterator();
 					listModel.addElement("MOVIES");
 					sideList.addElement("MOVIES");
 					while (itr.hasNext() ) {
 						movie tempMovie = new movie();
 						tempMovie = (movie)itr.next();
 						
 						listModel.addElement(" " + tempMovie.getTitle());
 						sideList.addElement(tempMovie);
 						
 					}
 					listModel.addElement(" ");
 					sideList.addElement(" ");
 					listModel.addElement("PEOPLE");
 					sideList.addElement("PEOPLE");
 					itr = peopleResults.iterator();
 					while (itr.hasNext() ) {
 						person tempPerson = new person();
 						tempPerson = (person)itr.next();
 						
 						listModel.addElement(" " + tempPerson.getName());
 						
 						sideList.addElement(tempPerson);
 					}
 					
 					listModel.addElement(" ");
 					sideList.addElement(" ");
 					listModel.addElement("CHARACTER");
 					sideList.addElement("Element");
 					itr = charResults.iterator();
 					while (itr.hasNext() ) {
 						character tempPerson = new character();
 						tempPerson = (character)itr.next();
 						
 						listModel.addElement(" " + tempPerson.getName());
 						sideList.addElement(tempPerson);
 						
 						
 					}
 					
 					listModel.addElement(" ");
 					sideList.addElement(" ");
 					listModel.addElement("COMPANIES");
 					sideList.addElement("COMPANIES");
 					itr = prodResults.iterator();
 					while (itr.hasNext() ) {
 						production_company tmpComp = new production_company();
 						tmpComp = (production_company)itr.next();
 						
 						listModel.addElement(" " + tmpComp.getName());
 						sideList.addElement(tmpComp);
 					}
 
 					
 					table.setModel(listModel);
 					
 										
 					
 				} catch (ArrayIndexOutOfBoundsException ex) {
 					System.err.println("ArrayIndexOutOfBoundsException thrown");
 				}
 			}
 		}
 	}
     
     
     
     /**
      * Create the GUI and show it.  For thread safety,
      * this method should be invoked from
      * the event dispatch thread.
      */
     private static void createAndShowGUI() {
         //Create and set up the window.
         JFrame frame = new JFrame("TabbedPaneDemo");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          
         //Add content to the window.
         frame.add(new AdminGUI(), BorderLayout.CENTER);
          
         //Display the window.
         frame.pack();
         frame.setVisible(true);
 
         frame.setSize(1280, 800);
     }
      
     public static void main(String[] args) {
         //Schedule a job for the event dispatch thread:
         //creating and showing this application's GUI.
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 //Turn off metal's use of bold fonts
         UIManager.put("swing.boldMetal", Boolean.FALSE);
         createAndShowGUI();
             }
         });
     }
     
 	@Override
 	public void keyPressed(KeyEvent e) {
 		// TODO Auto-generated method stub
 		if(e.getKeyCode() == KeyEvent.VK_ENTER){
 			
 			System.out.println("ENTER BEING PRESSED");
 			searchButton.doClick();		
 
 			
 			//PopupPanel newDialog = new PopupPanel();
 		
 		}
 		
 		
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 	
 	public void createDialog(Object z){
 		String data = z.toString();
 		String id = data.substring(0,data.length()-1);
 		char type = data.charAt(data.length() -1 );
 		popup = new JFrame();
 		popup.setVisible(true);
 		
 		System.out.println("ID: "+id);
 		
 		
 		//popup.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	
 		JPanel p1 = new JPanel();
 		JPanel p2 = new JPanel();
 			
 
 		System.out.println("type = " + type);
 		if (type == 'm') {
 			
 			popup.setLayout(new BorderLayout());
 
 			p1.setLayout(new GridLayout(12, 1));
 			p2.setLayout(new GridLayout(1,2));
 
 			//get box_office data
 			box_office tbox = db.getBoxOfficeData(id);
 			//get movie data
 			movie tmov = db.getMovieDatabyID(id);
 			//set title of popup
 			popup.setTitle(tmov.getTitle());
 			
 			movieTitle = new JTextField(tmov.getTitle());
 			genre = new JTextField(tmov.getGenre());
 			rating = new JTextField(tmov.getRating());
 			runtime = new JTextField(new Integer(tmov.getRuntime()).toString() );
 			productionYear = new JTextField(new Integer(tmov.getProduction_year()).toString());
 			totalGross = new JTextField(tbox.getTotal_gross() );
 			openingWeekend = new JTextField(tbox.getOpening_weekend() );
 			release = new JTextField(tmov.getRelease_date());
 			
 			p1.add(new JLabel("Movie ID:" + tmov.getMid()));
 			p1.add(new JLabel(""));
 			p1.add(new JLabel("Title: "));
 			p1.add(movieTitle);
 			p1.add(new JLabel("Genre:"));
 			p1.add(genre);
 			p1.add(new JLabel("Rating:"));
 			p1.add(rating);
 			p1.add(new JLabel("Runtime:") );
 			p1.add(runtime);
 			p1.add(new JLabel("Production Year:" ) );
 			p1.add(productionYear);
 			p1.add(new JLabel("Release Date: (YYYY-MM-DD) " ) );
 			p1.add(release);			
 			p1.add(new JLabel("Opening Weekend Gross($): " ) );
 			p1.add(openingWeekend);
 			p1.add(new JLabel("Total Gross($): " ) );
 			p1.add(totalGross);
 			p1.add(new JLabel("Plot:"));
 			
 			String plot = tmov.getPlot();
 			if(plot == null){
 				plotLabel = new JTextField("Plot not avaiable");
 								p1.add(plotLabel);
 			}else{
 				plotLabel = new JTextField(tmov.getPlot());
 				
 				p1.add(plotLabel);
 			}
 			
 			popup.add(p1, BorderLayout.CENTER);
 			p2.add(saveButton);
 			p2.add(deleteButton);
 			popup.add(p2, BorderLayout.SOUTH);
 			
 			popup.setPreferredSize(new Dimension(900, 400));
 			popup.pack();
 			
 			popup.setVisible(true);
 			
 		} else if (type == 'p') {
 			popup.setLayout(new BorderLayout());
 
 			p1.setLayout(new GridLayout(12, 1));
 			p2.setLayout(new GridLayout(1,2));
 			
 			
 			//get people data
 			person tmpPerson = new person();
 			tmpPerson = db.getPeopleDataByID(id);
 			
 			
 			name = new JTextField(tmpPerson.getName());
 			gender = new JTextField(tmpPerson.getGender());
 			
 			popup.setTitle(tmpPerson.getName() );
 			
 			p1.add(new JLabel("Person ID: " + tmpPerson.getId()));
 			p1.add(new JLabel(""));
 			p1.add(new JLabel("Name: "));
 			p1.add(name);
 			p1.add(new JLabel("Gender: " ) );
 			p1.add(gender);
 
 			popup.add(p1, BorderLayout.CENTER);
 			p2.add(saveButton);
 			p2.add(deleteButton);
 			popup.add(p2, BorderLayout.SOUTH);
 			
 			popup.setPreferredSize(new Dimension(900, 400));
 			popup.pack();
 			
 			popup.setVisible(true);
 			
 		} else if (type == 'c') {
 			popup.setLayout(new BorderLayout());
 
 			p1.setLayout(new GridLayout(12, 1));
 			p2.setLayout(new GridLayout(1,2));
 			
 			
 			//get people data
 			character tmpPerson = new character();
 			tmpPerson = db.getCharacterDataByID(id);
 			
 			
 			charName = new JTextField(tmpPerson.getName());
 			
 			System.out.println("set charName to " + charName.getText());
 			
 			popup.setTitle(tmpPerson.getName() );
 			
 			p1.add(new JLabel("Character ID: " + tmpPerson.getId()));
 			p1.add(new JLabel(""));
 			p1.add(new JLabel("Name: "));
 			p1.add(charName);
 			popup.add(p1, BorderLayout.CENTER);
 			
 			p2.add(saveButton);
 			p2.add(deleteButton);
 			popup.add(p2, BorderLayout.SOUTH);
 			
 			popup.setPreferredSize(new Dimension(900, 400));
 			popup.pack();
 			
 			popup.setVisible(true);
 		} else if (type == 's') {
 			popup.setLayout(new BorderLayout());
 
 			p1.setLayout(new GridLayout(12, 1));
 			p2.setLayout(new GridLayout(1,2));
 			
 			
 			//get people data
 			production_company tmpComp = new production_company();
 			tmpComp = db.getProductionCompaniesbyID(id);
 			
 			
 			compName = new JTextField(tmpComp.getName());
 			loc = new JTextField(tmpComp.getLocation());
 			
 			popup.setTitle(tmpComp.getName() );
 			
 			p1.add(new JLabel("Company ID: " + tmpComp.getId()));
 			p1.add(new JLabel(""));
 			p1.add(new JLabel("Name: "));
 			p1.add(compName);
 			p1.add(new JLabel("Location: "));
 			p1.add(loc);
 			popup.add(p1, BorderLayout.CENTER);
 			
 			p2.add(saveButton);
 			p2.add(deleteButton);
 			popup.add(p2, BorderLayout.SOUTH);
 			
 			popup.setPreferredSize(new Dimension(900, 400));
 			popup.pack();
 			
 			popup.setVisible(true);			
 		}
 		
 		popup.setVisible(true);
 		popup.pack();
 		
 	}
 	
 	public void saveData(){
 		String data = popupData.toString();
 		String id = data.substring(0,data.length()-1);
 		char type = data.charAt(data.length() -1 );
 		
 		
 		//updating movie
 		if(type == 'm'){
 			//update title
 			db.updateData("movie", "mid", "title", movieTitle.getText(), id,0);
 			//update genre
 			db.updateData("movie", "mid", "genre", genre.getText(), id,0);
 			
 			if(!(runtime.getText() == "")){
 				//update runtime
 				db.updateData("movie", "mid", "runtime", runtime.getText(), id,1);
 			}
 			
 			
 			//update rating
 			db.updateData("movie", "mid", "rating", rating.getText(), id,0);
 			//update plot
 			db.updateData("movie", "mid", "plot", plotLabel.getText(), id,0);
 			//update production_year
 			if(!(productionYear.getText() == "")){
 				db.updateData("movie", "mid", "production_year", productionYear.getText(), id,1);
 			}
 			//update release_date
 			db.updateData("movie", "mid", "release_date", release.getText(), id,0);
 		}
 		if(type == 'p'){
 			//update name
 			db.updateData("person", "pid", "name", name.getText(), id,0);
 			//update gender
 			db.updateData("person", "pid", "gender", gender.getText(), id,0);
 			
 		}
 		if(type == 'c'){
 			
 			db.updateData("characters", "rid", "name", charName.getText(), id,0);
 		}
 		if(type == 's'){
 			//update name
 			db.updateData("production_companies", "id", "name", compName.getText(), id,0);
 			//update location
 			db.updateData("production_companies", "id", "location", loc.getText(), id,0);
 		}
 		popup.setVisible(false);
 	}
 }
