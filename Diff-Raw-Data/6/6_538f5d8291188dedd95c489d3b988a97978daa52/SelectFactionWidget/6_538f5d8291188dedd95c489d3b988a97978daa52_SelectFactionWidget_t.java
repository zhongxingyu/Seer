 package org.timadorus.webapp.client.character.ui.selectfraction;
 
 import java.util.List;
 
 import org.timadorus.webapp.beans.Character;
 import org.timadorus.webapp.client.character.ui.DefaultActionHandler;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 //Panel for selecting characters Faction
 public class SelectFactionWidget extends FormPanel implements SelectFactionDialog.Display {
 
   private Button nextButton;
 
   private Button prevButton;
   
   private Label genderLabel;
   
   private Label classLabel;
 
   private VerticalPanel panel;
 
   // grid for handling selections
   private FlexTable selectFactionGrid;
 
   // grid for next/prev buttons
   private FlexTable buttonGrid;
 
   // listbox for available faction
   private ListBox factionListBox;
 
   public SelectFactionWidget() {
     super();
 
     nextButton = new Button("weiter");
     prevButton = new Button("zur&uuml;ck");
     panel = new VerticalPanel();
     selectFactionGrid = new FlexTable();
     buttonGrid = new FlexTable();
     factionListBox = new ListBox();
 
     genderLabel = new Label("Geschlecht:  | Rasse: ");
     classLabel = new Label("Klasse: ");
     
     // headline
     HTML headline = new HTML("<h1>Fraktion w&auml;hlen</h1>");
     // progress bar picture
     Image progressBar = new Image("media/images/progressbar_3.png");
 
     // setting properties for selectfactiongrid
     selectFactionGrid.setBorderWidth(0);
     selectFactionGrid.setStylePrimaryName("selectGrid");
 
     Label factionLabel = new Label("Fraktion w&auml;hlen: ");
 
     selectFactionGrid.setWidget(0, 0, factionLabel);
     selectFactionGrid.setWidget(0, 1, factionListBox);
 
     factionListBox.setVisibleItemCount(factionListBox.getItemCount());
 
     // set properties of buttongrid
     buttonGrid.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);
     buttonGrid.setWidth("350px");
     buttonGrid.setWidget(0, 0, prevButton);
     buttonGrid.setWidget(0, 1, nextButton);
 
     // setting properties of the main panel
     panel.setStyleName("panel");
     panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
     panel.setWidth("100%");
 
     // adding widgets to the main panel
     panel.add(progressBar);
     panel.add(new Label("Schritt 3 von 9"));
     panel.add(genderLabel); 
     panel.add(classLabel); 
 
     panel.add(headline);
     panel.add(selectFactionGrid);
     panel.add(buttonGrid);
 
     // clearing "information" #div and adding actual informations for this panel
     RootPanel.get("information").clear();
     RootPanel.get("information").add(getInformation());
 
     // clearing "content" #div and adding this mainpanel to this #div
     RootPanel.get("content").clear();
     RootPanel.get("content").add(panel);
 
   }
   
   @Override
   public void setFactions(List<String> factions) {
     for (String faction : factions) {
      factionListBox.addItem(faction);
     }
   }  
   
   // returns and hols current panel information
   private static final HTML getInformation() {
    HTML information = new HTML("<h1>Fraktionen w&auml;hlen</h1>"
        + "<p>W&auml;hlen sie hier die Fraktionen ihres Charakteres. "
         + "Beachten sie, dass bestimmte Fraktionen nur von bestimmten Rassen sowie Klassen "
        + "gew&auml;hlt werden k&ouml;nnen.</p>");
     return information;
   }
 
   @Override
   public FormPanel getFormPanel() {
     return this;
   }
 
   @Override
   public String getSelectedFaction() {
     return factionListBox.getValue(factionListBox.getSelectedIndex());
 
   }
 
   @Override
   public void addPrevButtonHandler(final DefaultActionHandler handler) {
 
     prevButton.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent event) {
         handler.onAction();
 
       }
     });
   }
 
   @Override
   public void addNextButtonHandler(final DefaultActionHandler handler) {
     nextButton.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent event) {
         handler.onAction();
 
       }
     });
   }
 
   @Override
   public void addSelectFactionGridHandler(final DefaultActionHandler handler) {
 
     selectFactionGrid.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent event) {
         handler.onAction();
 
       }
     });
   }
 
   @Override
   public void setInformation(String msg) {
     RootPanel.get("information").clear();
     RootPanel.get("information").add(new HTML(msg));
   }
 
   @Override
   public void setCharacter(Character c) {
     genderLabel.setText("Geschlecht: " + c.getGender() + " | Rasse: " + c.getRace().getName());
     classLabel.setText("Klasse: " + c.getCharClass().getName());    
   }
 
   public void setNextButtonEnable(boolean enabled) {
     nextButton.setEnabled(enabled);
   }
 }
