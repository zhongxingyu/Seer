 package org.timadorus.webapp.client.character.ui.selectclass;
 
 import org.timadorus.webapp.beans.CClass;
 import org.timadorus.webapp.beans.Character;
 import org.timadorus.webapp.beans.Faction;
 import org.timadorus.webapp.beans.User;
 import org.timadorus.webapp.client.DefaultTimadorusWebApp;
 import org.timadorus.webapp.client.character.ui.DefaultActionHandler;
 import org.timadorus.webapp.client.character.ui.DefaultDialog;
 import org.timadorus.webapp.client.character.ui.DefaultDisplay;
 import org.timadorus.webapp.client.eventhandling.events.SelectRaceEvent;
 import org.timadorus.webapp.client.eventhandling.events.ShowSelectClassEvent;
 import org.timadorus.webapp.client.eventhandling.handler.ShowDialogHandler;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.RootPanel;
 
 public class SelectClassDialog extends DefaultDialog<SelectClassDialog.Display> implements ShowDialogHandler {
   public interface Display extends DefaultDisplay {
 
     /**
      * Gets the name of the selected class of the ui.
      * 
      * @return {@link String}
      */
     public String getSelectedClass();
 
     public void setPrevButtonHandler(DefaultActionHandler handler);
 
     public void setNextButtonHandler(DefaultActionHandler handler);
 
     public void setClassGridButtonHandler(DefaultActionHandler handler);
 
     public void loadSelectRaceDialog(DefaultTimadorusWebApp entry, Character character, User user);
 
     public void loadSelectFactionDialog(DefaultTimadorusWebApp entry, Character character, User user);
   }
 
   private Character character;
 
   private User user;
 
   public SelectClassDialog(Display display, DefaultTimadorusWebApp entry, Character character, User user) {
     super(display, entry);
     this.character = character;
     this.user = user;
     entry.addHandler(ShowSelectClassEvent.SHOWDIALOG, this);
 
   }
 
   private void initDisplay() {
     getDisplay().setClassGridButtonHandler(new DefaultActionHandler() {
 
       @Override
       public void onAction() {
         doClassGridClick();
       }
     });
     getDisplay().setNextButtonHandler(new DefaultActionHandler() {
 
       @Override
       public void onAction() {
         doNextButtonClick();
       }
     });
     getDisplay().setPrevButtonHandler(new DefaultActionHandler() {
 
       @Override
       public void onAction() {
         doPrevButtonClick();
       }
     });
   }
 
   private void doClassGridClick() {
     String className = getDisplay().getSelectedClass();
     RootPanel.get("information").clear();
     for (CClass newClass : getEntry().getTestValues().getClasses()) {
       if (newClass.getName().equals(className)) {
         RootPanel.get("information").add(new HTML("<h1>" + newClass.getName() + "</h1><p>" + newClass.getDescription()
                                              + "</p>"));
 
         // Show available Factions in "information" #div
         RootPanel.get("information").add(new HTML("<h2>Wählbare Fraktionen</h2>"));
         String availableFactions = new String("<ul>");
         String nextFaction = new String();
         for (Faction newFaction : newClass.getAvailableFactions()) {
           nextFaction = newFaction.getName();
           if (character.getRace().getAvailableFactions().contains(newFaction)) {
             availableFactions = availableFactions + "<li>" + nextFaction + "</li>";
           }
         }
         availableFactions = availableFactions + "</ul>";
         RootPanel.get("information").add(new HTML(availableFactions));
       }
     }
   }
 
   // clear "content" #div and add Class SelectRacePanel to it
   private void doPrevButtonClick() {
     getEntry().fireEvent(new SelectRaceEvent(user, character));
   }
 
   // clear "content" #div and add Class SelectFactionPanel to it
   private void doNextButtonClick() {
     character.setCharClass(getSelectedClass());
     getDisplay().loadSelectFactionDialog(getEntry(), character, user);
   }
 
   private CClass getSelectedClass() {
 
     for (CClass selectedClass : getEntry().getTestValues().getClasses()) {
       String className = selectedClass.getName();
       String selectedName = getDisplay().getSelectedClass();
       if (className.equals(selectedName)) { return selectedClass; }
     }
     return null;
   }
 
   /**
    * Creates the dialog with its widget.
    * 
    * @param entry
    *          {@link TimadorusWebApp} entry point.
    * @param character
    * @param user
    * @return {@link SelectClassDialog}
    */
   public static SelectClassDialog getSelecteddDialog(DefaultTimadorusWebApp entry, Character character, User user) {
//    SelectClassDialog.Display display = new SelectClassWidget(character, entry.getTestValues().getClasses());
    SelectClassDialog dialog = new SelectClassDialog(null, entry, character, user);
     return dialog;
   }
 
   @Override
   public void show(DefaultTimadorusWebApp entry, Character character, User user) {
     this.character = character;
     this.user = user;
     SelectClassDialog.Display display = new SelectClassWidget(character, entry.getTestValues().getClasses());
     setDisplay(display);
     initDisplay();
     RootPanel.get("content").clear();
     RootPanel.get("content").add(getFormPanel());
   }
 
 }
