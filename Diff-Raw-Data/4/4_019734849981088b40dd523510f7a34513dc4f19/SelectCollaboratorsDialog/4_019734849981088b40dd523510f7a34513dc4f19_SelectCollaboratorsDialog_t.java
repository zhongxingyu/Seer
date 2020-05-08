 package org.iplantc.de.client.views.dialogs;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.events.CollaboratorsAddedEvent;
 import org.iplantc.de.client.events.CollaboratorsAddedEventHandler;
 import org.iplantc.de.client.events.CollaboratorsRemovedEvent;
 import org.iplantc.de.client.events.CollaboratorsRemovedEventHandler;
 import org.iplantc.de.client.images.Resources;
 import org.iplantc.de.client.models.Collaborator;
 import org.iplantc.de.client.utils.CollaboratorsUtil;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.SelectionMode;
 import com.extjs.gxt.ui.client.Style.SortDir;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.Dialog;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.button.ToolButton;
 import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.grid.GridView;
 import com.extjs.gxt.ui.client.widget.grid.GridViewConfig;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.AbstractImagePrototype;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * A Dialog that displays a grid of the user's Collaborators, which can be selected in a Checkbox
  * selection grid. The final selection can be retrieved from the getSelectedCollaborators method called
  * in a SelectionListener added to the OK/Done button.
  * 
  * @author psarando
  * 
  */
 public class SelectCollaboratorsDialog extends Dialog {
     public class ManageCollaboratorsListener extends SelectionListener<ButtonEvent> {
 
         @Override
         public void componentSelected(ButtonEvent ce) {
             CollaboratorsDialog collabsDialog = new CollaboratorsDialog();
             collabsDialog.setModal(true);
             collabsDialog.show();
         }
 
     }
 
     private static final String ID_BTN_MANAGE_COLLABS = "idBtnManageCollabs"; //$NON-NLS-1$
     private Grid<Collaborator> grid;
     private List<HandlerRegistration> handlers;
 
     public SelectCollaboratorsDialog() {
         init();
     }
 
     private void init() {
         setSize(640, 320);
         setLayout(new FitLayout());
         setHeading(I18N.DISPLAY.selectCollabs());
         setHideOnButtonClick(true);
         setModal(true);
 
         // TODO temp. remove help button.
         // buildHelpToolTip();
 
         setButtons();
 
         buildCollaboratorsGrid();
 
         add(grid);
 
         initHandlers();
     }
 
     private void initHandlers() {
         handlers = new ArrayList<HandlerRegistration>();
 
         EventBus eventBus = EventBus.getInstance();
 
         handlers.add(eventBus.addHandler(CollaboratorsAddedEvent.TYPE,
                 new CollaboratorsAddedEventHandler() {
 
                     @Override
                     public void onAdd(CollaboratorsAddedEvent event) {
                         grid.getStore().add(event.getCollaborators());
                     }
                 }));
 
         handlers.add(eventBus.addHandler(CollaboratorsRemovedEvent.TYPE,
                 new CollaboratorsRemovedEventHandler() {
 
                     @Override
                     public void onRemove(CollaboratorsRemovedEvent event) {
                         for (Collaborator user : event.getCollaborators()) {
                             grid.getStore().remove(user);
                         }
                     }
                 }));
     }
 
     @Override
    protected void onDetach() {
        super.onDetach();
 
         for (HandlerRegistration handler : handlers) {
             handler.removeHandler();
         }
     }
 
     private void buildHelpToolTip() {
         ToolTipConfig ttc = getToolTipConfig();
         ttc.setTitle(I18N.DISPLAY.help());
         ttc.setText(I18N.HELP.shareCollaboratorsHelp());
 
         ToolButton helpBtn = new ToolButton("x-tool-help"); //$NON-NLS-1$
         helpBtn.setToolTip(ttc);
         getHeader().addTool(helpBtn);
     }
 
     private ToolTipConfig getToolTipConfig() {
         ToolTipConfig config = new ToolTipConfig();
         config.setMouseOffset(new int[] {0, 0});
         config.setAnchor("left"); //$NON-NLS-1$
         config.setCloseable(true);
         return config;
     }
 
     private Widget buildCollaboratorsGrid() {
         CheckBoxSelectionModel<Collaborator> sm = new CheckBoxSelectionModel<Collaborator>();
         grid = new Grid<Collaborator>(new ListStore<Collaborator>(), buildCollaboratorColumnModel(sm));
 
         sm.setSelectionMode(SelectionMode.MULTI);
         grid.setSelectionModel(sm);
         grid.addPlugin(sm);
 
         grid.setAutoExpandColumn(Collaborator.NAME);
         grid.setBorders(false);
         grid.getView().setEmptyText(I18N.DISPLAY.selectCollaboratorsEmptyText());
 
         GridView view = grid.getView();
         view.setViewConfig(buildGridViewConfig());
         view.setForceFit(true);
 
         return grid;
     }
 
     private GridViewConfig buildGridViewConfig() {
         GridViewConfig config = new GridViewConfig() {
 
             @Override
             public String getRowStyle(ModelData model, int rowIndex, ListStore<ModelData> ds) {
                 return "iplantc-select-grid"; //$NON-NLS-1$
             }
 
         };
 
         return config;
     }
 
     private ColumnModel buildCollaboratorColumnModel(CheckBoxSelectionModel<Collaborator> sm) {
         List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
 
         ColumnConfig name = new ColumnConfig(Collaborator.NAME, I18N.DISPLAY.name(), 150);
         ColumnConfig email = new ColumnConfig(Collaborator.EMAIL, I18N.DISPLAY.email(), 200);
 
         columns.addAll(Arrays.asList(sm.getColumn(), name, email));
 
         return new ColumnModel(columns);
     }
 
     private void setButtons() {
         ToolBar bottomBar = new ToolBar();
         bottomBar.add(buildManageCollabsButton());
 
         setBottomComponent(bottomBar);
 
         setButtons(Dialog.OKCANCEL);
         setButtonAlign(HorizontalAlignment.RIGHT);
 
         getDoneButton().setText(I18N.DISPLAY.done());
     }
 
     private Button buildManageCollabsButton() {
         Button addCollabsBtn = new Button(I18N.DISPLAY.addCollabs(),
                 AbstractImagePrototype.create(Resources.ICONS.viewCurrentCollabs()));
         addCollabsBtn.setId(ID_BTN_MANAGE_COLLABS);
         addCollabsBtn.addSelectionListener(new ManageCollaboratorsListener());
 
         return addCollabsBtn;
     }
 
     /**
      * @return The OK button, labeled "Done" by default.
      */
     public Button getDoneButton() {
         return getButtonById(Dialog.OK);
     }
 
     /**
      * Sets the grid with the given list of Collaborators.
      * 
      * @param collaborators
      */
     public void loadResults(List<Collaborator> collaborators) {
         // Clear results before adding.
         clear();
 
         ListStore<Collaborator> store = grid.getStore();
         store.add(collaborators);
 
         // Sort alphabetically.
         store.sort(Collaborator.NAME, SortDir.ASC);
     }
 
     public void clear() {
         grid.getStore().removeAll();
     }
 
     /**
      * Sets the grid with the user's current Collaborators.
      */
     public void showCurrentCollborators() {
         List<Collaborator> currentCollaborators = CollaboratorsUtil.getCurrentCollaborators();
         if (currentCollaborators != null) {
             loadResults(currentCollaborators);
         } else {
             grid.mask(I18N.DISPLAY.loadingMask());
 
             CollaboratorsUtil.getCollaborators(new AsyncCallback<Void>() {
                 @Override
                 public void onSuccess(Void result) {
                     grid.unmask();
                     loadResults(CollaboratorsUtil.getCurrentCollaborators());
                 }
 
                 @Override
                 public void onFailure(Throwable caught) {
                     // TODO Auto-generated method stub
                     grid.unmask();
                     ErrorHandler.post(caught);
                 }
             });
         }
     }
 
     /**
      * @return The list of Collaborators selected in the grid.
      */
     public List<Collaborator> getSelectedCollaborators() {
         return grid.getSelectionModel().getSelectedItems();
     }
 
 }
