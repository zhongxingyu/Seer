 package org.iplantc.de.client.views.panels;
 
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.images.Resources;
 import org.iplantc.de.client.models.Collaborator;
 import org.iplantc.de.client.utils.CollaboratorsUtil;
 
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.FieldEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.HorizontalPanel;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.Status;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.AbstractImagePrototype;
 
 /**
  * 
  * 
  * A view class that enables the users to manage collaborators
  * 
  * @author sriram
  * 
  */
 
 public class ManageCollaboratorsPanel extends LayoutContainer {
 
     private TextField<String> searchTerm;
     private Button search;
     private Status status;
     private Button showList;
 
     private MODE mode;
 
     private CollaboratorsPanel panel;
 
     public static enum MODE {
         MANAGE, SEARCH
     };
 
     public ManageCollaboratorsPanel(MODE mode, int width, int height) {
         setWidth(width);
         this.mode = mode;
         init(width, height);
 
     }
 
     private void init(int width, int height) {
         initSearch();
         panel = new CollaboratorsPanel(I18N.DISPLAY.currentCollaborators(), mode, width, height);
         add(panel);
         loadCollaborators();
     }
 
     private void initSearch() {
         buildSearchField();
 
         buildSearchButton();
 
         buildSearchStatus();
 
         HorizontalPanel hp = new HorizontalPanel();
         hp.setBorders(false);
         hp.setWidth(440);
        hp.setSpacing(15);
         hp.add(searchTerm);
         hp.add(search);
         buildShowListButton();
         hp.add(showList);
         add(status);
         add(hp);
 
     }
 
     private void buildShowListButton() {
         showList = new Button();
         showList.setIcon(AbstractImagePrototype.create(Resources.ICONS.viewCurrentCollabs()));
         showList.addSelectionListener(new SelectionListener<ButtonEvent>() {
 
             @Override
             public void componentSelected(ButtonEvent ce) {
                 showList.setVisible(false);
                 updateCurrentMode(MODE.MANAGE);
                 panel.showCurrentCollborators();
                 panel.setHeading(I18N.DISPLAY.collaborators());
                 searchTerm.clear();
             }
         });
         showList.setVisible(false);
         showList.setTitle(I18N.DISPLAY.currentCollabList());
     }
 
     private void buildSearchButton() {
         search = new Button();
         search.setIcon(AbstractImagePrototype.create(Resources.ICONS.search()));
         search.addSelectionListener(new SelectionListener<ButtonEvent>() {
 
             @Override
             public void componentSelected(ButtonEvent ce) {
                 doSearch();
 
             }
         });
         search.setTitle(I18N.DISPLAY.search());
     }
 
     private void buildSearchField() {
         searchTerm = new TextField<String>() {
             @Override
             public void onKeyUp(FieldEvent fe) {
                 if (fe.getKeyCode() == 13) {
                     if (showList.isVisible()) {
                         showList.setVisible(false);
                     }
                     doSearch();
                 }
             }
         };
        searchTerm.setWidth(250);
         searchTerm.setEmptyText(I18N.DISPLAY.collabSearchPrompt());
         searchTerm.setMinLength(3);
     }
 
     private void buildSearchStatus() {
         status = new Status();
     }
 
     private void doSearch() {
         String search = searchTerm.getValue();
         if (search == null || search.isEmpty() || search.length() < 3) {
             searchTerm.markInvalid(I18N.DISPLAY.collabSearchPrompt());
             return;
         }
 
         panel.setHeading(I18N.DISPLAY.search() + ": " + search);
         status.setBusy("");
         panel.mask(I18N.DISPLAY.searching());
         searchTerm.clearInvalid();
         CollaboratorsUtil.search(search, new AsyncCallback<Void>() {
 
             @Override
             public void onFailure(Throwable caught) {
                 panel.unmask();
                 status.clearStatus("");
             }
 
             @Override
             public void onSuccess(Void result) {
                 updateCurrentMode(MODE.SEARCH);
                 panel.unmask();
                 status.clearStatus("");
                 panel.loadResults(CollaboratorsUtil.getSearchResutls());
                 showList.setVisible(true);
             }
         });
     }
 
     private void loadCollaborators() {
         panel.mask(I18N.DISPLAY.loadingMask());
         CollaboratorsUtil.getCollaborators(new AsyncCallback<Void>() {
 
             @Override
             public void onFailure(Throwable caught) {
                 status.clearStatus("");
                 panel.unmask();
             }
 
             @Override
             public void onSuccess(Void result) {
                 updateCurrentMode(MODE.MANAGE);
                 status.clearStatus("");
                 panel.unmask();
                 panel.loadResults(CollaboratorsUtil.getCurrentCollaborators());
                 showList.setVisible(false);
             }
 
         });
     }
 
     public Collaborator getSelectedCollaborator() {
         return panel.getSelectedCollaborator();
     }
 
     private void updateCurrentMode(MODE m) {
         mode = m;
         panel.setMode(mode);
     }
 
 }
