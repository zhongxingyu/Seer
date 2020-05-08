 
 package ualberta.g12.adventurecreator;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Models a story that would be created by an Author. Contains a list of
  * Fragments as well as an author and a title.
  */
 public class Story extends SModel{
     
     private static final int NEW_STORY_ID = -1;
     
     private String storyTitle;
     private String author;
     private int id = 1; // TODO: should be unique 
     private List<Fragment> fragments; // first index in pages is always the
                                         // start page
 
     public Story() {
         // No hardcoded strings
         // TODO: Move these to res/values/strings.xml
         /*
          * TODO: Possibly move to a View
          * I feel like these should be in the view for creating a new story as
          * the default text/hint. It doesn't really make sense for the Model to
          * have this
          */
         this("Add a title.", "Your pen name here");
     }
 
     public Story(String title, String author) {
         setStoryTitle(title);
         setAuthor(author);
         this.fragments = new LinkedList<Fragment>();
     }
 
     public String getStoryTitle() {
         return storyTitle;
     }
 
     public void setStoryTitle(String storyTitle) {
         this.storyTitle = storyTitle;
     }
 
     public String getAuthor() {
         return author;
     }
 
     public void setAuthor(String author) {
         this.author = author;
     }
 
     public List<Fragment> getFragments() {
         return fragments;
     }
 
     public void addFragment(Fragment newFragment) {
         this.fragments.add(newFragment);
     }
 
     public boolean removeFragment(Fragment oldFragment) {
         return this.fragments.remove(oldFragment);
     }
 
     public void setFragments(List<Fragment> f){
         this.fragments = f;
     }
     
     public void addLinkToNewPage() {
 
     }
 
     public void addLinkToPageInStory(Fragment currentPage, String choiceText, Fragment linkedToPage) {
 
     }
 
     // For merging stories (should happen when a choice is set to a page in
     // another story)
     public void afterPageLinkedToAnotherStory(Story newStory, Fragment pageLinkedTo,
             Choice choiceToSet) {
         // adds all pages of newStory to the current story
         for (int i = 0; i < newStory.getFragments().size(); i++) {
             this.fragments.add(newStory.getFragments().get(i));
         }
         choiceToSet.setLinkedToPage(pageLinkedTo);
     }
 
     // finds isolated pages and sets their isLinkedTo flag to false
     // should be run before each time a list of pages in the story is displayed
     // or only before the pages are displayed and change flag is true.
     // would need to create a change flag
     public void findAndMarkIsolatedPages() {
         LinkedList<Fragment> copyOfPages = new LinkedList<Fragment>();
         // copies pages list
         for (int i = 0; i < this.fragments.size(); i++) {
             copyOfPages.add(fragments.get(i));
         }
         // removes all pages from copyOfPages that are referenced
         for (int i = 0; i < this.fragments.size(); i++) {
             for (int j = 0; j < fragments.get(i).getChoices().size(); j++) {
                 Fragment tempPage = fragments.get(i).getChoices().get(j).getLinkedToPage();
                 copyOfPages.remove(tempPage);
             }
         }
         // only unreferenced pages remain
         for (int i = 0; i < copyOfPages.size(); i++) {
             copyOfPages.get(i).setLinkedTo(false);
         }
     }
     
     /**
      * will get id of the story (should be unique)
      * @return
      */
     public int getId(){
     	return this.id;
     }
     
     /**
      * will set id of the story (should be unique)
      * @param newId
      */
     public void setId(int newId){
     	this.id = newId;
     }
 
 }
