 package Jeff;
 import Daniel.*;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.swing.Box;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerListModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 /**
  * Project #3
  * CS 2334, Section 011
  * 10/9/2013
  * This class shows the scholar data graphs
  * @version 2.0
  */
 public class DisplayView extends JPanel implements ActionListener{
 	/* Instance variables */
 	
 	/** the serialVersionID */
 	private static final long serialVersionUID = 1L;
 	/** the colors to be used for the graph*/
 	private static final Color[] BAR_COLORS = {Color.CYAN, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.YELLOW, Color.RED};
 	/** the location for the publication type label */
 	public static final int PUBLICATION_TYPE = 0;
 	/** the location for the publications per year label */
 	public static final int PUBLICATIONS_PER_YEAR = 1;
 	/** the location for the conference papers per year label */
 	public static final int CONFERENCE_PAPERS_PER_YEAR = 2;
 	/** the location for the journal articles per year label */
 	public static final int JOURNAL_ARTICLES_PER_YEAR = 3;
 	/** the location for the number of coauthorships label */
 	public static final int NUMBER_OF_COAUTHORS = 4;
 	
 	/** width of the panel */
 	private int width;
 	/** height of the panel */
 	private int height;
 	/** the int representation of the graph type */
 	private int graphType;
 	/** the Scholar being looked at */
 	private Scholar scholar;
 	/** the list of Scholars */
 	private ArrayList<String> scholarNameList = new ArrayList<String>();
 	/** the scholar spinner model */
 	private SpinnerListModel scholarSpinnerModel;
 	/** the Scholar selector */
 	private JSpinner scholarSpinner;
 	/** the type selector */
 	private JComboBox<String> typeSelector;
 	/** the model */
 	private ScholarshipModel model;
 
 
 	/**
      * Initializes all of the variables specific to a publication date grapher excepting the Scholar
     * @param             model					the model that will be displayed
      */
 	public DisplayView(ScholarshipModel model){
 		this.model = model;
 		this.model.addListener(this);
 		graphType = -1;
 		addComponentListener(new ComponentAdapter(){
 			public void componentResized(ComponentEvent e){
 				width = getWidth();
 				height = getHeight();
 			}
 		});
 
 		if(this.model != null && this.model.getScholarMap() != null){
 			scholarNameList = new ArrayList<String>(this.model.getScholarMap().keySet());
 		}
 		if(scholarNameList.size() == 0){
 			scholarNameList.add("none");
 		}
 		scholarSpinnerModel = new SpinnerListModel(scholarNameList);
 		scholarSpinner = new JSpinner(scholarSpinnerModel);
 		scholarSpinner.addChangeListener(new ChangeListener(){
 			public void stateChanged(ChangeEvent e){
 				setScholar(getSelectedScholar());
 				repaint();
 			}
 		});
 		Box controlBox = Box.createHorizontalBox();
 		controlBox.add(Box.createGlue());
 		controlBox.add(new JLabel("Scholar"));
 		controlBox.add(Box.createHorizontalStrut(10));
 		controlBox.add(scholarSpinner);
 		String[] types = {"Select Graph Parameter", "Publication Type", "Publications Per Year", "Conference Papers Per Year", "Journal Articles Per Year", "Coauthors"};
 		typeSelector = new JComboBox<String>(types);
 		typeSelector.addItemListener(new ItemListener(){
 			public void itemStateChanged(ItemEvent e) {
 				graphType = typeSelector.getSelectedIndex() - 1;
 				repaint();
 			}
 		});
 		controlBox.add(Box.createHorizontalStrut(20));
 		controlBox.add(typeSelector);
 		controlBox.add(Box.createGlue());
 		
 		setLayout(new BorderLayout());
 		add(controlBox, BorderLayout.NORTH);
 	}
 	
 	/**
 	 * sets the background and draws the graph and decor
 	 */
 	public void paintComponent(Graphics g){
 		super.paintComponent(g);
 		setBackground(new Color(218, 223, 245));
 
 		drawBarGraph(g);
 		drawDecor(g);
 	}
 	
 	/**
 	 * draws the graph
 	 * @param g			the Graphics to use
 	 */
 	private void drawBarGraph(Graphics g){
 		if(scholar == null){
 			return;
 		}
 		
 		if(graphType == PUBLICATION_TYPE){
 			int[] values = {scholar.getConferencePapers().size(), scholar.getJournalArticles().size()};
 			String[] labels = {"Conference Papers", "Journal Articles"};
 			drawBars(values, labels, g);
 		}
 		else if(graphType == PUBLICATIONS_PER_YEAR || graphType == CONFERENCE_PAPERS_PER_YEAR || graphType == JOURNAL_ARTICLES_PER_YEAR){
 			HashMap<String, Integer> dateMap = new HashMap<String, Integer>();
 			ArrayList<ConferencePaper> conPaps = scholar.getConferencePapers();
 			ArrayList<JournalArticle> jourArts = scholar.getJournalArticles();
 			int tempVal = 0;
 			String nextYear = "";
 			if(graphType != JOURNAL_ARTICLES_PER_YEAR){
 				for(int i = 0; i < conPaps.size(); i++){
 					nextYear = conPaps.get(i).getYear();
 					if(nextYear != null && nextYear.split(" ").length > 1){
 						nextYear = nextYear.split(" ")[1];
 					}
 					if(!dateMap.containsKey(nextYear)){
 						dateMap.put(nextYear, 1);
 					}
 					else{
 						tempVal = dateMap.get(nextYear);
 						dateMap.put(nextYear, tempVal + 1);
 					}
 				}
 			}
 			if(graphType != CONFERENCE_PAPERS_PER_YEAR){
 				for(int i = 0; i < jourArts.size(); i++){
 					nextYear = jourArts.get(i).getYear();
 					if(nextYear != null && nextYear.split(" ").length > 1){
 						nextYear = nextYear.split(" ")[1];
 					}
 					if(!dateMap.containsKey(nextYear)){
 						dateMap.put(nextYear, 1);
 					}
 					else{
 						tempVal = dateMap.get(nextYear);
 						dateMap.put(nextYear, tempVal + 1);
 					}
 				}
 			}
 			ArrayList<String> keys = new ArrayList<String>(dateMap.keySet());
 			String minDate = "9999";
 			String maxDate = "0000";
 			if(keys.size() == 0){
 				return;
 			}
 			for(int i = 0; i < keys.size(); i++){
 				if(keys.get(i).compareTo(minDate) < 0){
 					minDate = keys.get(i);
 				}
 				if(keys.get(i).compareTo(maxDate) > 0){
 					maxDate = keys.get(i);
 				}
 			}
 			int[] values;
 			String[] labels;
 			int min = 0;
 			int max = 0;
 			try{
 				min = Integer.parseInt(minDate);
 				max = Integer.parseInt(maxDate);
 			}
 			catch(NumberFormatException e){
 				return;
 			}
 			if(min > max){
 				int temp = min;
 				min = max;
 				max = temp;
 			}
 			values = new int[max - min + 1];
 			labels = new String[max - min + 1];
 			for(int i = min; i <= max; i++){
 				if(dateMap.get(""+i) == null){
 					values[i - min] = 0;
 				}
 				else{
 					values[i - min] = dateMap.get(""+i);
 				}
 				labels[i - min] = ""+i;
 			}
 			drawBars(values, labels, g);
 		}
 		else if(graphType == NUMBER_OF_COAUTHORS){
 			HashMap<Integer, Integer> coscholarMap = new HashMap<Integer, Integer>();
 			ArrayList<ConferencePaper> conPaps = scholar.getConferencePapers();
 			ArrayList<JournalArticle> jourArts = scholar.getJournalArticles();
 			int tempVal = 0;
 			if(graphType != JOURNAL_ARTICLES_PER_YEAR){
 				for(int i = 0; i < conPaps.size(); i++){
 					if(!coscholarMap.containsKey(conPaps.get(i).getAuthors().size() - 1)){
 						coscholarMap.put(conPaps.get(i).getAuthors().size() - 1, 1);
 					}
 					else{
 						tempVal = coscholarMap.get(conPaps.get(i).getAuthors().size() - 1);
 						coscholarMap.put(conPaps.get(i).getAuthors().size() - 1, tempVal + 1);
 					}
 				}
 			}
 			if(graphType != CONFERENCE_PAPERS_PER_YEAR){
 				for(int i = 0; i < jourArts.size(); i++){
 					if(!coscholarMap.containsKey(jourArts.get(i).getAuthors().size() - 1)){
 						coscholarMap.put(jourArts.get(i).getAuthors().size() - 1, 1);
 					}
 					else{
 						tempVal = coscholarMap.get(jourArts.get(i).getAuthors().size() - 1);
 						coscholarMap.put(jourArts.get(i).getAuthors().size() - 1, tempVal + 1);
 					}
 				}
 			}
 			ArrayList<Integer> keys = new ArrayList<Integer>(coscholarMap.keySet());
 			if(keys.size() != 0){
 				int minCoscholars = 9999;
 				int maxCoscholars = -9999;
 				for(int i = 0; i < keys.size(); i++){
 					if(keys.get(i) < minCoscholars){
 						minCoscholars = keys.get(i);
 					}
 					if(keys.get(i) > maxCoscholars){
 						maxCoscholars = keys.get(i);
 					}
 				}
 				int[] values = new int[maxCoscholars - minCoscholars + 1];
 				String[] labels = new String[maxCoscholars - minCoscholars + 1];
 				for(int i = minCoscholars; i <= maxCoscholars; i++){
 					if(coscholarMap.get(i) == null){
 						values[i - minCoscholars] = 0;
 					}
 					else{
 						values[i - minCoscholars] = coscholarMap.get(i);
 					}
 					labels[i - minCoscholars] = ""+i;
 				}
 				drawBars(values, labels, g);
 			}
 		}
 		drawDecor(g);
 	}
 	
 	/**
 	 * draws the decor
 	 * @param g			the Graphics to use
 	 */
 	private void drawDecor(Graphics g){
 		int yTopOffset = 80;  //Should be the same as below
 		int yBottomOffset = 28;  //Should be the same as below
 		int xMargin = 28;  //Should be the same as below
 		Font titleFont = new Font(Font.SANS_SERIF, Font.PLAIN, 24);
 		FontMetrics titleStick = g.getFontMetrics(titleFont);
 		g.setColor(Color.DARK_GRAY);
 		g.setFont(titleFont);
 
 		g.drawLine(xMargin, height - yBottomOffset, width - xMargin, height - yBottomOffset);
 		g.drawString(typeSelector.getSelectedItem().toString(), (width - titleStick.stringWidth(typeSelector.getSelectedItem().toString()))/2, yTopOffset - 10);
 	}
 
 	/**
 	 * draws the bars
 	 * @param values	the values to graph
 	 * @param labels	the labels to give the bars
 	 * @param g			the Graphics to use
 	 */
 	private void drawBars(int[] values, String[] labels, Graphics g){
 		drawBars(values, labels, BAR_COLORS, g);
 	}
 
 	/**
 	 * draws the bars
 	 * @param values	the values to graph
 	 * @param labels	the labels to give the bars
 	 * @param colors	the colors for the bars
 	 * @param g			the Graphics to use
 	 */
 	private void drawBars(int[] values, String[] labels, Color[] colors, Graphics g){
 		if(values.length == 0){
 			return;
 		}
 		Font labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
 		FontMetrics labelStick = g.getFontMetrics(labelFont);
 		g.setFont(labelFont);
 		int yTopOffset = 80;  //Should be the same as above
 		int yBottomOffset = 28;  //Should be the same as above
 		int xMargin = 28;  //Should be the same as above
 		int xPadding = 5 + (int)(400/Math.pow(values.length + 1, 2));
 		int columnWidth = (width - ((values.length + 1) * xPadding) - xMargin * 2) / values.length;
 		int maxValue = 1;
 		for(int i = 0; i < values.length; i++){
 			if(values[i] > maxValue){
 				maxValue = values[i];
 			}
 		}
 		int singleValueHeight = (height - yTopOffset - yBottomOffset - 10) / maxValue; // the (-10) is for padding within the graph
 		for(int i = 0; i < values.length; i++){
 			g.setColor(colors[i % colors.length]);
 			g.fillRect(xMargin + (i + 1) * xPadding + i * columnWidth, height - yBottomOffset - values[i]*singleValueHeight, columnWidth, values[i]*singleValueHeight);
 			g.setColor(Color.BLACK);
 			g.drawString(labels[i], xMargin + (i + 1) * xPadding + i * columnWidth + (columnWidth - labelStick.stringWidth(labels[i]))/2, height - yBottomOffset + labelStick.getHeight());
 			g.drawString(""+values[i], xMargin + (i + 1) * xPadding + i * columnWidth + (columnWidth - labelStick.stringWidth(""+values[i]))/2, height - yBottomOffset - values[i]*singleValueHeight - 4);
 		}
 	}
 	
 	/**
 	 * Listens for changes in the scholar map of the model
 	 */
 	public void actionPerformed(ActionEvent ev){
 		DataChangeEvent e;
 		if(ev instanceof DataChangeEvent){
 			e = (DataChangeEvent)ev;
 		}
 		else{
 			return;
 		}
 		if(e.getActionCommand() == DataChangeEvent.SCHOLAR_ADDED){
 			ArrayList<String> newScholarNameList = new ArrayList<String>(scholarNameList);
 			newScholarNameList.remove("none");
 			for(int i = 0; i < e.getObjectsChanged().length; i++){
 				newScholarNameList.add(e.getObjectsChanged()[i].toString());
 			}
 			scholarSpinnerModel.setList(newScholarNameList);
 		}
 		if(e.getActionCommand() == DataChangeEvent.SCHOLAR_REMOVED){
 			ArrayList<String> newScholarNameList = new ArrayList<String>(scholarNameList);
 			for(int i = 0; i < e.getObjectsChanged().length; i++){
 				newScholarNameList.remove(e.getObjectsChanged()[i].toString());
 			}
 			if(newScholarNameList.size() == 0){
 				newScholarNameList.add("none");
 			}
 			scholarSpinnerModel.setList(newScholarNameList);
 		}
 		if(e.getActionCommand() == DataChangeEvent.PAPER_ADDED){
 			ArrayList<String> newScholarNameList = new ArrayList<String>(scholarNameList);
 			ArrayList<String> scholars = new ArrayList<String>(model.getScholarMap().keySet());
 			for(int i = 0; i < scholars.size(); i++){
 				newScholarNameList.add(scholars.get(i));
 				System.out.println(scholars.get(i));
 			}
 			scholarSpinnerModel.setList(newScholarNameList);
 		}
 		if(e.getActionCommand() == DataChangeEvent.PAPER_REMOVED){
 			ArrayList<String> newScholarNameList = new ArrayList<String>(scholarNameList);
 			ArrayList<Scholar> scholars = new ArrayList<Scholar>(model.getScholarMap().values());
 			newScholarNameList.clear();
 			for(int i = 0; i < scholars.size(); i++){
 				newScholarNameList.add(scholars.get(i).toString());
 			}
 			scholarSpinnerModel.setList(newScholarNameList);
 		}
 	}
 	
 	/*
 	 * Mutator Methods
 	 */
 
 	/**
 	 * sets the graphType
 	 * @param graphType		the graph type
 	 */
 	public void setGraphType(int graphType){
 		this.graphType = graphType;
 		typeSelector.setSelectedIndex(graphType + 1);
 	}
 	
 	/**
 	 * sets the scholar being examined
 	 * @param scholar	the scholar to look at
 	 */
 	public void setScholar(Scholar scholar){
 		this.scholar = scholar;
 	}
 	
 	public Scholar getSelectedScholar(){
 		return model.getScholarMap().get(scholarSpinner.getValue().toString());
 	}
 }
