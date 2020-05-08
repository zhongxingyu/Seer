 import java.util.ArrayList;
 
 public class Author implements Comparable<Author>{
 	
 	/**
 	 * first name of the author
 	 */
 	private String nameFirst;
 	
 	/**
 	 * last name of the author
 	 */
 	private String nameLast;
 	
 	/**
 	 * publications published by this author
 	 */
 	private ArrayList<Publication> publishedPapers = new ArrayList<Publication>();
 	
 	/**
 	 * default constructor
 	 * 
 	 * @param nameWhole name of author
 	 */
 	public Author(String nameWhole) {
 		setName(nameWhole);
 	}
 	
 	public String toString() {
 		return getName();
 	}
 	
 	@Override
 	public int compareTo(Author arg0) {
 		return this.getName().compareTo(arg0.getName());
 	}
 	
 	public ArrayList<Publication> getPublishedPapers()
 	{
 		return publishedPapers;
 	}
 	
 	public boolean addPublishedPaper(Publication paper)
 	{
 		publishedPapers.add(paper);
 		return true;
 	}
 	
 	public int[] valueCalculator(String desiredValue){
 		
 		int[] calculatedValues = new int[publishedPapers.size()];
 		ArrayList<String> conferencePapers = new ArrayList<String>();
 		ArrayList<String> journalPapers = new ArrayList<String>();
 		
 		if(desiredValue == "NC"){
 			//TODO calculate collaborations
 			/* needs to basically return the size of the ArrayList<String> of Authors
 			 * for every publication in publishedPapers and store in calculatedValues
 			 */
 
 		}
 		else if(desiredValue == "PY"){
 			//TODO get all year values from publishedPapers and store in calculateValues
 			//This should be able to be copied and pasted right into the last two todo statements
 			
 		}
 		else {
 			for(Publication p : publishedPapers){
 				/* TODO split publishedPapers into two lists. 
 				 * 1 of conference papers called conferencePapers (initialized above)
 				 * 1 of journal articles called journalPapers (initialized above)
 				*/
 				
 			}
 			if(desiredValue == "TP"){
 				calculatedValues = new int[2];
 				calculatedValues[0] = conferencePapers.size();
 				calculatedValues[1] = journalPapers.size();
 			}
 			
 			//for conference paper choice
 			else if(desiredValue == "CPY"){
 				calculatedValues = new int[conferencePapers.size()];
 				
 				//TODO copy section from PY if statement
 				
 			}
 			//for journal article choice
 			else if(desiredValue == "JAY"){
 				calculatedValues = new int[journalPapers.size()];
 				
 				//TODO copy section from PY if statement
 			}		
 		}
 		return calculatedValues;
 	}
 	
 	public String getName() {
 		return getNameFirst() + " " + getNameLast();
 	}
 
 	public void setName(String nameWhole) {
 		if(nameWhole.contains(" ") == true)
 		{
			String[] split = nameWhole.split("\\ ");
			setNameLast(split[0]);
			setNameFirst(split[split.length-1]);
 		}
 		else
 			setNameFirst(nameWhole);
 	}
 	
 	public String getNameFirst() {
 		return nameFirst;
 	}
 
 	public void setNameFirst(String nameFirst) {
 		this.nameFirst = nameFirst;
 	}
 
 	public String getNameLast() {
 		return nameLast;
 	}
 
 	public void setNameLast(String nameLast) {
 		this.nameLast = nameLast;
 	}
 	
 	public int getSizeOfPublishedPapers() {
 		return publishedPapers.size();
 	}
 }
