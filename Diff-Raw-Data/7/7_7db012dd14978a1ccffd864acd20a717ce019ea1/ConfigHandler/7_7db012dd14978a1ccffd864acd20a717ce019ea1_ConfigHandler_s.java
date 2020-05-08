 /**
  * 
  */
 package project.efg.util.pdf.bookmaker;
 
 
 import java.util.SortedSet;
 
 import project.efg.templates.taxonPageTemplates.CharacterValue;
 import project.efg.templates.taxonPageTemplates.GroupType;
 import project.efg.templates.taxonPageTemplates.GroupTypeItem;
 import project.efg.templates.taxonPageTemplates.GroupsType;
 import project.efg.templates.taxonPageTemplates.GroupsTypeItem;
 import project.efg.templates.taxonPageTemplates.XslPage;
 import project.efg.util.pdf.factory.EFGPDFSpringFactory;
 import project.efg.util.pdf.interfaces.EFGRankObject;
 import project.efg.util.pdf.interfaces.PDFGUIConstants;
 
 /**
  * Handles Configuration of Bookmaker.
  * TODO - Extend to handle all PDF configurations
  *
  */
 public class ConfigHandler {
 	private String title;
 	private SortedSet sortFields;
 	private int numberOFImagesPerColumn;
 	
 	
 	private SortedSet mediaResourceFields;
 	private String mainHeader;
 	private SortedSet subHeaders;
 	private SortedSet descriptionFields;
 	
 	//For front page
 	private SortedSet frontPageTitles;
 	private SortedSet frontPageSubTitles;
 	
 	private  SortedSet credits1;
 	private SortedSet credits2;
 	
 	private XslPage xslPage;
 	public ConfigHandler(XslPage xslPage){
 		this.mediaResourceFields = EFGPDFSpringFactory.getEFGPDFSortedTree();
 		this.sortFields = EFGPDFSpringFactory.getEFGPDFSortedTree();
 		this.subHeaders = EFGPDFSpringFactory.getEFGPDFSortedTree();
 		this.descriptionFields = EFGPDFSpringFactory.getEFGPDFSortedTree();
 		this.frontPageTitles = EFGPDFSpringFactory.getEFGPDFSortedTree();
 		this.frontPageSubTitles = EFGPDFSpringFactory.getEFGPDFSortedTree();
 		this.credits1 = EFGPDFSpringFactory.getEFGPDFSortedTree();
 		this.credits2 = EFGPDFSpringFactory.getEFGPDFSortedTree();
 		this.xslPage = xslPage;
 		this.processGroups();
 	}
 	private void processGroups() {
 		GroupsType groups = xslPage.getGroups();
 		GroupsTypeItem[] groupsArray = groups.getGroupsTypeItem();
 
 		//FIXME refactor
 		for (int i = 0; i < groupsArray.length; i++) {
 			GroupsTypeItem gpt = (GroupsTypeItem) groupsArray[i];
 			GroupType group = gpt.getGroup();
 			String label = group.getLabel();
 			if(label == null){
 				continue;
 			}
 			if ("titles".equalsIgnoreCase(label)){
 				handleTitle(group);
 			}
 			else if (PDFGUIConstants.FRONT_PAGE_TITLES.equalsIgnoreCase(label)) {
 				
 				handleFrontPageData(group,this.frontPageTitles);
 				if(this.frontPageTitles.size() > 0){//make this the title of
 					//all pages
					this.title = (String)this.frontPageTitles.first();
 				}
 			}
 			else if (PDFGUIConstants.FRONT_PAGE_SUBTITLES.equalsIgnoreCase(label)) {
 				handleFrontPageData(group,this.frontPageSubTitles);
 			}
 			else if (PDFGUIConstants.CREDITS1_TEXT.equalsIgnoreCase(label)) {
				System.out.println("adding credits1");
 				handleFrontPageData(group,this.credits1);
 			}
 			else if (PDFGUIConstants.CREDITS2_TEXT.equalsIgnoreCase(label)) {
				
				System.out.println("adding credits2");
 				handleFrontPageData(group,this.credits2);
 			}
 			else if ("taxonomy".equalsIgnoreCase(label)) {
 				handleTaxonomy(group);
 			} else if ("descriptions"
 					.equalsIgnoreCase(label)) {
 				handleDescriptions(group);
 			} else if (
 					"mediaresources".equalsIgnoreCase(label)) {
 				handleMediaResources(group);
 			} else if ("sort".equalsIgnoreCase(label)) {
 				handleSort(group);
 			} else if (
 					"numberofcolumns".equalsIgnoreCase(label)) {// we
 				handleColumns(group);
 			} 
 			else{//we don't know you
 			}
 		}
 	}
 	/**
 	 * TODO create a method that takes the Set and group as parameters
 	 * @param group
 	 */
 	private void handleFrontPageData(GroupType group, SortedSet set) {
 		for (java.util.Enumeration e = group.enumerateGroupTypeItem(); e
 		.hasMoreElements();) {
 		GroupTypeItem key = (GroupTypeItem) e.nextElement();
 			if (key != null) {
 				CharacterValue cv = key.getCharacterValue();
 
 				if (cv != null) {
 					String title = cv.getValue();
 					if (title != null && !title.trim().equals("")) {
 						EFGRankObject efgrank = EFGPDFSpringFactory.getEFGRankInstance();
 						efgrank.setRank(cv.getRank());
 						efgrank.setName(cv.getValue());
 						set.add(efgrank);
 					}
 				}
 			}
 		}	
 	}
 	/**
 	 * @param group
 	 */
 	private void handleTitle(GroupType group) {
 		for (java.util.Enumeration e = group.enumerateGroupTypeItem(); e
 		.hasMoreElements();) {
 		GroupTypeItem key = (GroupTypeItem) e.nextElement();
 	
 			if (key != null) {
 				
 				CharacterValue cv = key.getCharacterValue();
 				if (cv != null) {
 					
 					String title = cv.getValue();
 						if (title != null && !title.trim().equals("")) {
 							this.title = title;
 							break;
 						}
 					}
 				}
 			}
 	}
 
 	/**
 	 * @param group
 	 */
 	private void handleColumns(GroupType group) {
 		for (java.util.Enumeration e = group.enumerateGroupTypeItem(); e
 		.hasMoreElements();) {
 		GroupTypeItem key = (GroupTypeItem) e.nextElement();
 		if (key != null) {
 			
 			CharacterValue cv = key.getCharacterValue();
 			if (cv != null) {
 				String label = cv.getLabel();
 				String columns = cv.getValue();
 					if (columns != null && !columns.trim().equals("")) {
 						this.numberOFImagesPerColumn = Integer.parseInt(columns);
 						break;
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param group
 	 */
 	private void handleSort(GroupType group) {
 		for (java.util.Enumeration e = group.enumerateGroupTypeItem(); e
 		.hasMoreElements();) {
 		GroupTypeItem key = (GroupTypeItem) e.nextElement();
 		if (key != null) {
 			
 			CharacterValue cv = key.getCharacterValue();
 			if (cv != null) {
 				String label = cv.getLabel();
 				String sort = cv.getValue();
 					if (sort != null && !sort.trim().equals("")) {
 						EFGRankObject efgrank = EFGPDFSpringFactory.getEFGRankInstance();
 						efgrank.setRank(cv.getRank());
 						efgrank.setName(cv.getValue());
 						this.sortFields.add(efgrank);
 					}
 				}
 			}
 		}
 
 	}
 	/**
 	 * @param group
 	 */
 	private void handleDescriptions(GroupType group) {
 		for(int i = 0; i < group.getGroupTypeItemCount(); i++){
 			GroupTypeItem key =group.getGroupTypeItem(i);
 			CharacterValue cv = key.getCharacterValue();
 			if(cv.getValue() != null){
 				EFGRankObject efgrank = EFGPDFSpringFactory.getEFGRankInstance();
 				efgrank.setRank(cv.getRank());
 				efgrank.setName(cv.getValue());
 				this.descriptionFields.add(efgrank);
 			}
 		}
 	}
 
 	/**
 	 * @param group
 	 */
 	private void handleMediaResources(GroupType group) {
 		EFGRankObject efgrank = null;
 		for(int i = 0; i < group.getGroupTypeItemCount(); i++){
 			GroupTypeItem key =group.getGroupTypeItem(i);
 			CharacterValue cv = key.getCharacterValue();
 			
 			//TODO refactor constants
 			if("displayallimages".equalsIgnoreCase(cv.getLabel())){
 				if(efgrank != null){
 					if("yes".equalsIgnoreCase(cv.getValue())){
 						efgrank.setDisplay(true);
 					}
 				}
 			}
 			else{
 				if(cv.getValue() != null){
 					if(efgrank == null){
 						efgrank = EFGPDFSpringFactory.getEFGRankInstance();
 					}
 					efgrank.setRank(group.getRank());
 					efgrank.setName(cv.getValue());
 				}
 			}
 		}
 		if(efgrank != null){
 			this.mediaResourceFields.add(efgrank);
 		}
 	}
 	/**
 	 * @param group
 	 */
 	private void handleTaxonomy(GroupType group) {
 		for(int i = 0; i < group.getGroupTypeItemCount(); i++){
 			GroupTypeItem key =group.getGroupTypeItem(i);
 			CharacterValue cv = key.getCharacterValue();
 			if(cv.getValue() != null){
 				if(cv.getLabel().equals("mainHeader")){
 					this.mainHeader = cv.getValue();
 				}
 				else{
 					EFGRankObject efgrank =  EFGPDFSpringFactory.getEFGRankInstance();
 					String name=cv.getValue();
 					efgrank.setName(name);
 					efgrank.setRank(cv.getRank());
 				
 					this.subHeaders.add(efgrank);
 				}
 			}
 		}
 	}
 	/**
 	 * 
 	 * @return
 	 */
 	public SortedSet getDescriptionFields() {
 		return this.descriptionFields;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public String getMainHeader() {
 		return this.mainHeader;
 	}
 	/**
 	 * 
 	 * @return
 	 */
 	public SortedSet getMediaResourceFields() {
 		return this.mediaResourceFields;
 	}
 	/**
 	 * 
 	 * @return
 	 */
 	public int getNumberOFImagesPerColumn() {
 		return this.numberOFImagesPerColumn;
 	}
 	/**
 	 * 
 	 * @return
 	 */
 	public SortedSet getSortFields() {
 		if(this.sortFields.size() == 0){//sort by main header
 			this.sortFields.add(this.getMainHeader());
 		}
 		return this.sortFields;
 	}
 	/**
 	 * 
 	 * @return
 	 */
 	public SortedSet getSubHeaders() {
 		return this.subHeaders;
 	}
 	/**
 	 * 
 	 * @return
 	 */
 	public String getTitle() {
 		
 		return this.title;
 	}
 	/**
 	 * 
 	 * @return
 	 */
 	public SortedSet getFrontPageTitle() {
 		return this.frontPageTitles;
 	}
 	/**
 	 * 
 	 * @return
 	 */
 	public SortedSet getFrontSubtitle() {
 		return this.frontPageSubTitles;
 	}
 	/**
 	 * 
 	 * @return
 	 */
 	public SortedSet getCredits1() {
 		return this.credits1;
 	}
 	/**
 	 * 
 	 * @return
 	 */
 	public SortedSet getCredits2() {
 		return this.credits2;
 	}
 }
