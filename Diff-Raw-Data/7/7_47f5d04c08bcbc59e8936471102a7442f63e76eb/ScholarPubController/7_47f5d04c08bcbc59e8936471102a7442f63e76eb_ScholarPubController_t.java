 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.swing.JOptionPane;
 
 
 
 
 public class ScholarPubController {
 	
 	private ScholarshipModel model;
 	private SelectionView view;
 	
 	/**
 	 * Creates new controller with model and view
 	 */
 	public ScholarPubController(ScholarshipModel model, SelectionView view){
 		this.model = model;
 		this.view = view;
 		
 		this.view.getJbAddScholar().addActionListener(new AddScholarListener());
 		this.view.getJbDeleteSelectedScholar().addActionListener(new DeleteScholarListener());
 		this.view.getJbDeleteAllScholar().addActionListener(new DeleteAllScholarsListener());
 		
 		this.view.getJbAddSerial().addActionListener(new AddSerialListener());
 		this.view.getJbDeleteSelectedSerial().addActionListener(new DeleteSerialListener());
 		this.view.getJbDeleteAllSerial().addActionListener(new DeleteAllSerialsListener());
 		this.view.getJbAddPaper().addActionListener(new AddPaperListener());
 		this.view.getJbDeleteSelectedPaper().addActionListener(new DeletePaperListener());
 		this.view.getJbDeleteAllPaper().addActionListener(new DeleteAllPapersListener());
 		this.view.getJmiLoad().addActionListener(new LoadListener());
 		this.view.getJmiSave().addActionListener(new SaveListener());
 		this.view.getJmiExit().addActionListener(new ExitListener());
 		
 		this.view.getJmiTP().addActionListener(new TPListener());
 		
 		this.view.getJmiSNe().addActionListener(new scholarNeighborhoodListener());
 		this.view.getJmiPubNe().addActionListener(new pubNeighborhoodListener());
 	}
 	
 	/**
 	 * AddScholarListener provides a method to create a new scholar.
 	 */
 	private class AddScholarListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			Frame frame = new Frame();
 			Object[] obj = null;
 			String name = (String)JOptionPane.showInputDialog(frame, "Add a Scholar:\n"+ "Enter the Scholar's name","Add Scholar", JOptionPane.QUESTION_MESSAGE,null, obj,"Dean Hougen");
 			String aff = (String)JOptionPane.showInputDialog(frame, "Add a Scholar:\n"+ "Enter " + name + "'s affiliation","Add Scholar", JOptionPane.QUESTION_MESSAGE,null, obj,"University of Oklahoma");
 			String research = (String)JOptionPane.showInputDialog(frame, "Add a Scholar:\n"+ "Enter " + name + "'s research areas","Add Scholar", JOptionPane.QUESTION_MESSAGE,null, obj,"Artificial Intelligence");
 			
 			model.addScholar(new Scholar(name, aff, research));
 			view.updateList(model);
 		}
 	}
 	
 	/**
 	 * DeleteScholarListener provides a method to delete a scholar.
 	 */
 	private class DeleteScholarListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			//TODO stub
 			
 			
 			
 			view.updateList(model);
 		}
 	}
 	
 	/**
 	 * DeleteAllScholarsListener provides a method to delete all scholars.
 	 */
 	private class DeleteAllScholarsListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			model.deleteAllScholars();
 			view.updateList(model);
 		}
 	}
 	
 	/**
 	 * AddSerialListener provides a method to create a new serial.
 	 */
 	private class AddSerialListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			Frame frame = new Frame();
 			String[] obj = {"Conference", "Journal"};
 			String type = (String)JOptionPane.showInputDialog(frame, "Add a Serial:\n"+ "What kind of serial?","Add Serial", JOptionPane.QUESTION_MESSAGE,null, obj,"Conference");
 			
 			Scholar[] auth = new Scholar[model.getScholarList().size() + 1];
 			int i = 0;
 			for(Scholar sch : model.getScholarList().values()){
 				auth[i]=sch;
 				i++;
 			}
 			
 			if(type.equals("Conference"))
 			{
 				ArrayList<Scholar> chairTemp = new ArrayList<Scholar>();
 				ArrayList<Scholar> committeeTemp = new ArrayList<Scholar>();
 				
 				String chairs = JOptionPane.showInputDialog(frame, "Add a Serial:\n"+ "Who is a program chair?","Add Serial", JOptionPane.QUESTION_MESSAGE,null, auth,"Journal").toString();			
 				chairTemp.add(new Scholar(chairs, "", ""));
 				
 				
 				for(int loop = 0; loop==0;){
 					int moreChairsYN = JOptionPane.showConfirmDialog(null, "Would you like to add another chair?", "Add a Link?", JOptionPane.YES_NO_OPTION);
 			        if (moreChairsYN == JOptionPane.YES_OPTION) {
 			        	String nextChair = JOptionPane.showInputDialog(frame, "Add a Serial:\n"+ "Who else is a program chair?","Add Serial", JOptionPane.QUESTION_MESSAGE,null, auth,"Journal").toString();			
 			        	chairTemp.add(new Scholar(nextChair, "", ""));
 			        	
 			        }
 			        else {
 			        	loop = 1;
 			        }
 				
 				}
 				
 				String committee = JOptionPane.showInputDialog(frame, "Add a Serial:\n"+ "Who is a committee member?","Add Serial", JOptionPane.QUESTION_MESSAGE,null, auth,"Journal").toString();			
 				committeeTemp.add(new Scholar(committee, "", ""));
 				
 				
 				for(int loop = 0; loop==0;){
 					int moreChairsYN = JOptionPane.showConfirmDialog(null, "Would you like to add another committee member?", "Add a Link?", JOptionPane.YES_NO_OPTION);
 			        if (moreChairsYN == JOptionPane.YES_OPTION) {
 			        	String nextComm = JOptionPane.showInputDialog(frame, "Add a Serial:\n"+ "Who else is a committee member?","Add Serial", JOptionPane.QUESTION_MESSAGE,null, auth,"Journal").toString();			
 			        	committeeTemp.add(new Scholar(nextComm, "", ""));
 			        	
 			        }
 			        else {
 			        	loop = 1;
 			        }	
 				
 				}
 				
 				model.addSerial(new Journal(chairs, committee));
 				//model.addSerial(new Conference(new Date(month, year), new Location(city, state, country), chairTemp, committeeTemp));
 
 			}
 			else{
 				String editor = JOptionPane.showInputDialog(frame, "Add a Serial:\n"+ "Who is the editor?","Add Serial", JOptionPane.QUESTION_MESSAGE,null, auth, null).toString();
 				String reviewer = JOptionPane.showInputDialog(frame, "Add a Serial:\n"+ "Who is the reviewer","Add Serial", JOptionPane.QUESTION_MESSAGE,null, auth,null).toString();
 				
 				model.addSerial(new Journal(editor, reviewer));
 				
 			}
 			
 			view.updateList(model);
 		}
 	}
 	
 	/**
 	 * DeleteSerialListener provides a method to delete a serial.
 	 */
 	private class DeleteSerialListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			//TODO
 			
 			
 			view.updateList(model);
 		}
 	}
 	
 	/**
 	 * DeleteAllSerialsListener provides a method to delete all serials.
 	 */
 	private class DeleteAllSerialsListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			model.deleteAllSerials();
 			view.updateList(model);
 		}
 	}
 	
 	/**
 	 * AddPaperListener provides a method to create a new paper.
 	 */
 	private class AddPaperListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			Frame frame = new Frame();
 			
 			Scholar[] auth = new Scholar[model.getScholarList().size() + 1];
 			int i = 0;
 			for(Scholar sch : model.getScholarList().values()){
 				auth[i]=sch;
 				i++;
 			}
 			
 			Serial[] serial = new Serial[model.getSerialMap().size() + 1];
 			i = 0;
 			for(Serial sch : model.getSerialMap()){
 				serial[i]=sch;
 				i++;
 			}
 			
 			
 			String authorName = JOptionPane.showInputDialog(frame, "Add a Paper:\n"+ "Who is the author?","Add Paper", JOptionPane.QUESTION_MESSAGE,null, model.getScholarList().values().toArray(), null).toString();
 			HashMap<String, Scholar> authorsMap = new HashMap<String, Scholar>();
 			authorsMap.put(authorName, model.getScholarList().get(authorName));
 			
 			Object[] options = {"None", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
 			int numAuthors = JOptionPane.showOptionDialog(null,
 					"How many more Authors are there?", 
 					"Add more Authors?",
 					JOptionPane.YES_NO_CANCEL_OPTION,
 				    JOptionPane.QUESTION_MESSAGE,
 				    null,
 				    options,
 				    options[1]);
 			
 			for(int index=numAuthors; index>0; index--)
 			{
 				String a = JOptionPane.showInputDialog(frame, "Add a Paper:\n"+ "Who is the next author?","Add Paper", JOptionPane.QUESTION_MESSAGE,null, model.getScholarList().values().toArray(), null).toString();
 				authorsMap.put(a, model.getScholarList().get(a));
 			}
 			
 			String title = JOptionPane.showInputDialog(frame, "Add a Paper:\n"+ "What is the Title?","Add Paper", JOptionPane.QUESTION_MESSAGE,null, null,null).toString();
 			
 			String serialTitle = JOptionPane.showInputDialog(frame, "Add a Serial:\n"+ "What is the Serial Title?","Add Serial", JOptionPane.QUESTION_MESSAGE,null, serial, null).toString();
 			
 			String pgStart = JOptionPane.showInputDialog(frame, "Add a Paper:\n"+ "What is starting page number?","Add Paper", JOptionPane.QUESTION_MESSAGE,null, null,null).toString();
 			
 			String pgEnd = JOptionPane.showInputDialog(frame, "Add a Paper:\n"+ "What is the last page number?","Add Paper", JOptionPane.QUESTION_MESSAGE,null, null,null).toString();
 			
 			String month = JOptionPane.showInputDialog(frame, "Add a Paper:\n"+ "What month was the paper written?","Add Paper", JOptionPane.QUESTION_MESSAGE,null, null,null).toString();
 			
 			String year = JOptionPane.showInputDialog(frame, "Add a Paper:\n"+ "What year was the paper written?","Add Paper", JOptionPane.QUESTION_MESSAGE,null, null,null).toString();
 			
 
 			int linkYN = JOptionPane.showConfirmDialog(null, "Would you like to add a link?", "Add a Link?", JOptionPane.YES_NO_OPTION);
 	        if (linkYN == JOptionPane.YES_OPTION) {
 	        	String link = JOptionPane.showInputDialog(frame, "Add a Paper:\n"+ "What is the link?","Add link", JOptionPane.QUESTION_MESSAGE,null, null,null).toString();
 	        	model.addPaper(new Publication(authorsMap, title, serialTitle, pgStart, pgEnd, month, year, link));
 	        }
 	        else {
 	        	model.addPaper(new Publication(authorsMap, title, serialTitle, pgStart, pgEnd, month, year));
 	        }		
 			view.updateList(model);
 		}
 	}
 	
 	/**
 	 * DeletePaperListener provides a method to delete a paper.
 	 */
 	private class DeletePaperListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			//TODO
 			
 			view.updateList(model);
 		}
 	}
 	
 	/**
 	 * DeleteAllPapersListener provides a method to delete all papers.
 	 */
 	private class DeleteAllPapersListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			model.deleteAllPapers();
 			view.updateList(model);
 		}
 	}
 	
 	/**
 	 * Loads scholarship from binary file
 	 * @param filename
 	 */
 	private class LoadListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {			
 			try {
 				loadScholarship(view.loadFile());
 			} catch (ClassNotFoundException e1) {
 				// Auto-generated catch block
 				e1.printStackTrace();
 			} catch (IOException e1) {
 				// Auto-generated catch block
 				e1.printStackTrace();
 			}
 			view.updateList(model);
 		}
 	}
 	
 	/**
 	 * Loads a scholarship
 	 * @param filename		file being loaded
 	 * @throws IOException
 	 * @throws ClassNotFoundException
 	 */
 	public void loadScholarship(String filename) throws IOException, ClassNotFoundException {
 		FileInputStream fileInputStream = new FileInputStream(filename);
 		ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
 		model = (ScholarshipModel) objectInputStream.readObject();
 		objectInputStream.close();
 		model.notifyAllListeners();
 	}
 	
 	/**
 	 * Saves the scholarship using a specified binary file
 	 * @param filename
 	 */
 	private class SaveListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			try {
 				model.saveScholarship(model);
 			} catch (IOException e1) {
 				// Auto-generated catch block
 				e1.printStackTrace();
 			}
 			view.updateList(model);
 		}
 	}
 	
 	/**
 	 * Exits the program
 	 */
 	private class ExitListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			System.exit(-1);
 		}
 	}
 	
 	private class TPListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			
 			if(model.getPubMap() != null && model.getPubMap().size() > 0)
 			{
 				view.openGraph("TP", model.getPubMap().get(0).getScholarsString(), model.getScholarList());
 			}
 		}
 	}
 	
 	private class PYListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			//TODO
 		}
 	}
 	
 	private class CPYtListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			//TODO
 		}
 	}
 	
 	private class JAYListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			//TODO
 		}
 	}
 	
 	private class CAtListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			//TODO
 		}
 	}
 	
 	private class scholarNeighborhoodGraphListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			//TODO
 		}
 	}
 	
 	private class pubNeighborhoodGraphListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			//TODO
 		}
 	}
 	
 	private class scholarNeighborhoodListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			Frame frame = new Frame();
 			
 			Scholar[] pub = new Scholar[model.getScholarList().size() + 1];
 			int i = 0;
 			for(Scholar sch : model.getScholarList().values()){
 				pub[i]=sch;
 				i++;
 			}
 			
 			String schName = JOptionPane.showInputDialog(frame, "Add a Paper:\n"+ "Who is the author?","Add Paper", JOptionPane.QUESTION_MESSAGE,null, model.getScholarList().values().toArray(), null).toString();
 			
			if(schName == null)
				return;
			
 			Object[] depthOptions = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
 			int numdepth = JOptionPane.showOptionDialog(null,
 					"How many more Authors are there?", 
 					"Depth?",
 					JOptionPane.YES_NO_CANCEL_OPTION,
 				    JOptionPane.QUESTION_MESSAGE,
 				    null,
 				    depthOptions,
 				    depthOptions[1]);
 			
 			Scholar foundSch = null;
 			
 			for(Scholar currentSch : model.getScholarList().values())
 			{
 				if(currentSch.toString().equals(schName))
 				{
 					foundSch = currentSch;
 				}
 			}
 			if(foundSch != null)
 			{
 				HashMap< Integer, ArrayList<Scholar> > out = foundSch.getNeighbors(0, numdepth, null, new HashMap< Integer, ArrayList<Scholar> >() );
 				
 				int size = out.get(out.size()-1).size();
 				
 				int linkYN = JOptionPane.showConfirmDialog(null, "Size = " + size, "Size = " + size, JOptionPane.YES_NO_OPTION);
 			}
 		}
 	}
 	
 	private class pubNeighborhoodListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			Frame frame = new Frame();
 			
 			Publication[] pub = new Publication[model.getPubMap().size() + 1];
 			int i = 0;
 			for(Publication sch : model.getPubMap()){
 				pub[i]=sch;
 				i++;
 			}
 			
 			String PubName = JOptionPane.showInputDialog(frame, "Add a Paper:\n"+ "Who is the author?","Add Paper", JOptionPane.QUESTION_MESSAGE,null, model.getPubMap().toArray(), null).toString();
 			
			if(PubName == null)
				return;
			
 			Object[] depthOptions = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
 			int numdepth = JOptionPane.showOptionDialog(null,
 					"How many more Authors are there?", 
 					"Depth?",
 					JOptionPane.YES_NO_CANCEL_OPTION,
 				    JOptionPane.QUESTION_MESSAGE,
 				    null,
 				    depthOptions,
 				    depthOptions[1]);
 			
 			Publication foundPub = null;
 			
 			ArrayList<Scholar> foundPubScholars = new ArrayList<Scholar>(); 
 			
 			for(Publication currentPub : model.getPubMap())
 			{
 				if(currentPub.toString().equals(PubName))
 				{
 					foundPub = currentPub;
 					
 					for(Scholar currentScholar :foundPub.getScholars().values())
 					{
 						foundPubScholars.add(currentScholar);
 					}
 				}
 			}
 			if(foundPub != null)
 			{
 				HashMap< Integer, ArrayList<Scholar> > out = foundPub.getNeighbors(0, numdepth, foundPubScholars, new HashMap< Integer, ArrayList<Scholar> >() );
 				
 				int size = out.get(out.size()-1).size();
 				
 				int linkYN = JOptionPane.showConfirmDialog(null, "Size = " + size, "Size = " + size, JOptionPane.YES_NO_OPTION);
 			}
 		}
 	}
 	
 	private class scholarDistListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			//TODO
 		}
 	}
 	
 	private class pubDistListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			//TODO
 		}
 	}
 }
