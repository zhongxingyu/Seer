 
 
 /**
  * <p>Title: </p>
  * <p>Description: </p>
  * <p>Copyright: Copyright (c) 2004</p>
  * <p>Company: RedBugz Software</p>
  * @author Logan Allred
  * @version 1.0
  */
 import com.apple.cocoa.application.*;
 import com.apple.cocoa.foundation.*;
 import com.apple.cocoa.application.NSToolbarItem;
 
 public class MainToolbar extends NSToolbar implements NSToolbar.Delegate {
   public static final String ADD_FAMILY_IDENTIFIER = "AddFamily";
   public static final String ADD_INDIVIDUAL_IDENTIFIER = "AddIndividual";
   public static final String VIEW_PEDIGREE_IDENTIFIER = "ViewPedigree";
   public static final String FIND_INDIVIDUAL_IDENTIFIER = "FindIndividual";
   public static final String FIND_FAMILY_IDENTIFIER = "FindFamily";
   public static final String PRINT_REPORTS_IDENTIFIER = "PrintReports";
   public static final String IMPORT_GEDCOM_IDENTIFIER = "ImportGEDCOM";
   public static final String EDIT_NOTES_IDENTIFIER = "EditNotes";
   public static final String MULTIMEDIA_CENTER_IDENTIFIER = "MultimediaCenter";
   public static final String SOUCE_CENTER_IDENTIFIER = "SourceCenter";
   public static final String REPOSITORY_CENTER_IDENTIFIER = "RepositoryCenter";
   public static final String LOCATION_CENTER_IDENTIFIER = "LocationCenter";
   public static final String INDIVIDUAL_LIST_IDENTIFIER = "IndividualList";
   public static final String FAMILY_LIST_IDENTIFIER = "FamilyList";
 
   NSMutableDictionary toolbarItems = new NSMutableDictionary();
 
   public MainToolbar() {
 	super("main");
 	setDelegate(this);
 	setAllowsUserCustomization(true);
 	setAutosavesConfiguration(true);
 	toolbarItems.setObjectForKey(new NSToolbarItem(NSToolbarItem.CustomizeToolbarItemIdentifier), NSToolbarItem.CustomizeToolbarItemIdentifier);
 	toolbarItems.setObjectForKey(new NSToolbarItem(NSToolbarItem.FlexibleSpaceItemIdentifier), NSToolbarItem.FlexibleSpaceItemIdentifier);
	toolbarItems.setObjectForKey(createToolbarItem(ADD_FAMILY_IDENTIFIER, "Add Family", createBadgedImage("Family","Badge Add"), new NSSelector("editNotes", new Class[] {Object.class})), ADD_FAMILY_IDENTIFIER);
	toolbarItems.setObjectForKey(createToolbarItem(ADD_INDIVIDUAL_IDENTIFIER, "Add Individual", createBadgedImage("Individual","Badge Add"), new NSSelector("editNotes", new Class[] {Object.class})), ADD_INDIVIDUAL_IDENTIFIER);
	toolbarItems.setObjectForKey(createToolbarItem(VIEW_PEDIGREE_IDENTIFIER, "View Pedigree", "Pedigree", new NSSelector("editNotes", new Class[] {Object.class})), VIEW_PEDIGREE_IDENTIFIER);
 	toolbarItems.setObjectForKey(createToolbarItem(FIND_FAMILY_IDENTIFIER, "Find Family", "Magnifying Glass", new NSSelector("findFamily", new Class[] {Object.class})), FIND_FAMILY_IDENTIFIER);
 	toolbarItems.setObjectForKey(createToolbarItem(FIND_INDIVIDUAL_IDENTIFIER, "Find Individual", "Magnifying Glass", new NSSelector("findIndividual", new Class[] {Object.class})), FIND_INDIVIDUAL_IDENTIFIER);
 	toolbarItems.setObjectForKey(createToolbarItem(INDIVIDUAL_LIST_IDENTIFIER, "Individual List", "Metal Window", new NSSelector("showIndividualList", new Class[] {Object.class})), INDIVIDUAL_LIST_IDENTIFIER);
 	toolbarItems.setObjectForKey(createToolbarItem(FAMILY_LIST_IDENTIFIER, "Family List", "Metal Window", new NSSelector("showFamilyList", new Class[] {Object.class})), FAMILY_LIST_IDENTIFIER);
 	toolbarItems.setObjectForKey(createToolbarItem(EDIT_NOTES_IDENTIFIER, "Edit Notes", "notepad", new NSSelector("editNotes", new Class[] {Object.class})), EDIT_NOTES_IDENTIFIER);
   }
 
   /**
  * @return
  */
 private NSToolbarItem createToolbarItem(String identifier, String label, String imageName, NSSelector action) {
 	return createToolbarItem(identifier, label, NSImage.imageNamed(imageName), action);
 }
 
 /**
  * @return
  */
 private NSToolbarItem createToolbarItem(String identifier, String label, NSImage image, NSSelector action) {
 	NSToolbarItem toolbarItem = new NSToolbarItem(identifier);
 	toolbarItem.setLabel(label);
 	toolbarItem.setImage(image);
 	toolbarItem.setAction(action);
 	return toolbarItem;
 }
 
 private NSImage createBadgedImage(String imageName, String badgeName) {
 	 NSImage newImage = new NSImage(new NSSize(48,48));
 	 try {
 		NSImage badge = NSImage.imageNamed(badgeName);
 		 NSImage iconImage = NSImage.imageNamed(imageName);
 		 newImage.lockFocus();
 		 iconImage.compositeToPoint(new NSPoint(0,0), NSImage.CompositeSourceOver);
 		 badge.compositeToPoint(new NSPoint(0,16), NSImage.CompositeSourceOver);
 //	 NSAttributedString T = CocoaCommon.makeAttributedString("TEXT","small-bold-white",20f);
 //	 NSGraphics.drawAttributedString(T, new NSPoint(95,84));
 
 		 newImage.unlockFocus();
 	} catch (RuntimeException e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
 //	 NSApplication.sharedApplication().setApplicationIconImage(NewImage);
 	 return newImage;
 }
 
 /**
    * toolbarItemForItemIdentifier
    *
    * @param nSToolbar NSToolbar
    * @param string String
    * @param boolean2 boolean
    * @return NSToolbarItem
    */
   public NSToolbarItem toolbarItemForItemIdentifier(NSToolbar nSToolbar, String string, boolean boolean2) {
 	return (NSToolbarItem) toolbarItems.objectForKey(string);
   }
 
   /**
    * toolbarDefaultItemIdentifiers
    *
    * @param nSToolbar NSToolbar
    * @return NSArray
    */
   public NSArray toolbarDefaultItemIdentifiers(NSToolbar nSToolbar) {
 	return new NSArray(new String[] {
 	  ADD_INDIVIDUAL_IDENTIFIER,
 	  ADD_FAMILY_IDENTIFIER,
 	  EDIT_NOTES_IDENTIFIER,
 	  INDIVIDUAL_LIST_IDENTIFIER,
 	  FAMILY_LIST_IDENTIFIER,
 	  NSToolbarItem.FlexibleSpaceItemIdentifier,
 	  NSToolbarItem.PrintItemIdentifier,
 	  NSToolbarItem.CustomizeToolbarItemIdentifier
 	});
   }
 
   /**
    * toolbarAllowedItemIdentifiers
    *
    * @param nSToolbar NSToolbar
    * @return NSArray
    */
   public NSArray toolbarAllowedItemIdentifiers(NSToolbar nSToolbar) {
 	return toolbarItems.allKeys(); 
 //	new NSArray(new String[] {
 //	  ADD_INDIVIDUAL_IDENTIFIER,
 //	  ADD_FAMILY_IDENTIFIER,
 //	  NSToolbarItem.SeparatorItemIdentifier,
 //	  NSToolbarItem.PrintItemIdentifier,
 //	  NSToolbarItem.FlexibleSpaceItemIdentifier,
 //	  NSToolbarItem.CustomizeToolbarItemIdentifier
 //	});
   }
 
   /**
    * toolbarSelectableItemIdentifiers
    *
    * @param nSToolbar NSToolbar
    * @return NSArray
    */
   public NSArray toolbarSelectableItemIdentifiers(NSToolbar nSToolbar) {
 	return null;
   }
 
 }
