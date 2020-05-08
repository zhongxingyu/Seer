 package org.iplantc.de.client.views.panels;
 
 import java.util.Collection;
 
 import org.iplantc.core.client.widgets.Hyperlink;
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.tito.client.events.TemplateLoadEvent;
 import org.iplantc.core.tito.client.events.TemplateLoadEvent.MODE;
 import org.iplantc.core.uiapplications.client.events.AnalysisGroupCountUpdateEvent;
 import org.iplantc.core.uiapplications.client.events.AnalysisGroupCountUpdateEvent.AnalysisGroupType;
 import org.iplantc.core.uiapplications.client.models.Analysis;
 import org.iplantc.core.uiapplications.client.models.AnalysisFeedback;
 import org.iplantc.core.uiapplications.client.views.panels.BaseCatalogMainPanel;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.de.client.Constants;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.events.UserEvent;
 import org.iplantc.de.client.factories.EventJSONFactory;
 import org.iplantc.de.client.images.Resources;
 import org.iplantc.de.client.services.ConfluenceServiceFacade;
 import org.iplantc.de.client.services.TemplateServiceFacade;
 import org.iplantc.de.client.utils.MessageDispatcher;
 import org.iplantc.de.client.views.dialogs.AppCommentDialog;
 import org.iplantc.de.client.views.windows.DECatalogWindow;
 import org.iplantc.de.client.views.windows.TitoWindow;
 
 import com.extjs.gxt.ui.client.core.FastMap;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.IconButtonEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.MenuEvent;
 import com.extjs.gxt.ui.client.event.MessageBoxEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.Dialog;
 import com.extjs.gxt.ui.client.widget.HorizontalPanel;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.button.IconButton;
 import com.extjs.gxt.ui.client.widget.grid.ColumnData;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
 import com.extjs.gxt.ui.client.widget.grid.GridViewConfig;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.menu.Menu;
 import com.extjs.gxt.ui.client.widget.menu.MenuItem;
 import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
 import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.AbstractImagePrototype;
 
 /**
  * 
  * Displays a list of analyses in grid. Actions to edit, copy and run are supported
  * 
  * @author sriram
  * 
  */
 public class CatalogMainPanel extends BaseCatalogMainPanel {
 
     private static final String ACTION_ID_COPY = "idCopy"; //$NON-NLS-1$
     private static final String ACTION_ID_REMOVE_FAV = "idRemoveFav"; //$NON-NLS-1$
     private static final String ACTION_ID_MARK_FAV = "idMarkFav"; //$NON-NLS-1$
     private static final String ACTION_ID_ACTIONS = "idActions"; //$NON-NLS-1$
     private static final String ACTION_ID_MAKE_PUBLIC = "idMakePublic"; //$NON-NLS-1$
     private static final String ACTION_ID_DELETE = "idDelete"; //$NON-NLS-1$
     private static final String ACTION_ID_EDIT = "idEdit"; //$NON-NLS-1$
     private static final String ACTION_ID_RUN = "idRun"; //$NON-NLS-1$
 
     private final FastMap<Button> buttons;
     private final FastMap<MenuItem> menuItems;
     private Analysis selectedItem;
 
     /**
      * Creates a new CatalogMainPanel.
      */
     public CatalogMainPanel(String tag) {
         super(tag, new TemplateServiceFacade());
 
         buttons = new FastMap<Button>();
         menuItems = new FastMap<MenuItem>();
 
         initGridListeners();
         initGridViewConfig();
         addToolBarActions();
     }
 
     private TemplateServiceFacade getTemplateService() {
         return (TemplateServiceFacade)templateService;
     }
 
     private void initGridListeners() {
         if (analysisGrid != null) {
             analysisGrid.getSelectionModel().addListener(Events.SelectionChange,
                     new GridSelectionChangeListener());
         }
 
     }
 
     private void initGridViewConfig() {
         analysisGrid.getView().setViewConfig(new AppGridViewConfig());
     }
 
     /**
      * Overridden to render app names as hyperlinks to run the app, and to add the "unrate" icon
      */
     @Override
     protected ColumnModel buildColumnModel() {
         ColumnModel model = super.buildColumnModel();
 
         model.getColumnById(Analysis.NAME).setRenderer(new AppNameCellRenderer());
         model.getColumnById(Analysis.RATING).setRenderer(new VotingCellRenderer());
 
         return model;
     }
 
     private void addToolBarActions() {
         addToToolBar(new SeparatorToolItem());
         addToToolBar(buildNewButton());
         addToToolBar(new FillToolItem());
         addToToolBar(buildRunButton());
         addToToolBar(buildCopyButton());
         addToToolBar(buildMoreActionsButton());
     }
 
     private Button buildRunButton() {
         Button run = new Button(I18N.DISPLAY.run());
         run.setId(ACTION_ID_RUN);
         run.setEnabled(false);
         run.setIcon(AbstractImagePrototype.create(Resources.ICONS.run()));
         run.addSelectionListener(new RunButtonSelectionListener());
         buttons.put(ACTION_ID_RUN, run);
         return run;
     }
 
     private MenuItem buildEditMenuItem() {
         MenuItem edit = new MenuItem(I18N.DISPLAY.edit());
 
         edit.setId(ACTION_ID_EDIT);
         edit.setIcon(AbstractImagePrototype.create(Resources.ICONS.edit()));
         edit.setEnabled(false);
 
         edit.addSelectionListener(new SelectionListener<MenuEvent>() {
             @Override
             public void componentSelected(MenuEvent ce) {
                 final String id = selectedItem.getId();
 
                 // first check if this app can be exported to TITo
                 getTemplateService().analysisExportable(id, new AsyncCallback<String>() {
                     @Override
                     public void onSuccess(String result) {
                         JSONObject exportable = JsonUtil.getObject(result);
 
                         if (JsonUtil.getBoolean(exportable, "can-export", false)) { //$NON-NLS-1$
                             editAnalysis(id);
                         } else {
                             ErrorHandler.post(JsonUtil.getString(exportable, "cause")); //$NON-NLS-1$
                         }
                     }
 
                     @Override
                     public void onFailure(Throwable caught) {
                         ErrorHandler.post(caught);
                     }
                 });
             }
         });
 
         menuItems.put(ACTION_ID_EDIT, edit);
 
         return edit;
     }
 
     private void editAnalysis(final String id) {
         getTemplateService().editAnalysis(id, new AsyncCallback<String>() {
             @Override
             public void onSuccess(String result) {
                 openTitoForEdit(id);
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(caught);
             }
         });
     }
 
     private void openTitoForEdit(final String id) {
         EventBus.getInstance().fireEvent(new TemplateLoadEvent(id, MODE.EDIT));
     }
 
     private MenuItem buildDeleteMenuItem() {
         MenuItem delete = new MenuItem(I18N.DISPLAY.delete());
         delete.setId(ACTION_ID_DELETE);
         delete.setIcon(AbstractImagePrototype.create(Resources.ICONS.cancel()));
         delete.setEnabled(false);
         delete.addSelectionListener(new DeleteMenuItemSelectionListener());
         menuItems.put(ACTION_ID_DELETE, delete);
         return delete;
     }
 
     private MenuItem buildSubmitMenuItem() {
         MenuItem submit = new MenuItem(I18N.DISPLAY.makePublic());
         submit.setId(ACTION_ID_MAKE_PUBLIC);
         submit.setIcon(AbstractImagePrototype.create(Resources.ICONS.submitForPublic()));
         submit.setEnabled(false);
         submit.addSelectionListener(new SelectionListener<MenuEvent>() {
 
             @Override
             public void componentSelected(MenuEvent ce) {
                 showPublishToWorldDialog(analysisGrid.getSelectionModel().getSelectedItem());
             }
         });
         menuItems.put(ACTION_ID_MAKE_PUBLIC, submit);
         return submit;
     }
 
     private void showPublishToWorldDialog(final Analysis analysis) {
         final Window makePublicWin = new Window();
         makePublicWin.setModal(true);
 
         PublishToWorldPanel requestForm = new PublishToWorldPanel(analysis, new AsyncCallback<String>() {
             @Override
             public void onSuccess(String url) {
                 makePublicWin.hide();
 
                 MessageBox.info(I18N.DISPLAY.makePublicSuccessTitle(),
                         I18N.DISPLAY.makePublicSuccessMessage(url), null);
 
                 fireAnalysisGroupCountUpdateEvent(false, AnalysisGroupType.BETA);
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 makePublicWin.hide();
                 ErrorHandler.post(I18N.DISPLAY.makePublicFail(), caught);
             }
         });
 
         makePublicWin.setHeading(analysis.getName() + " " + I18N.DISPLAY.publicSubmissionForm()); //$NON-NLS-1$
         makePublicWin.setLayout(new FitLayout());
         makePublicWin.setSize(615, 480);
         makePublicWin.setResizable(false);
         makePublicWin.add(requestForm);
 
         makePublicWin.show();
     }
 
     private Button buildNewButton() {
         Button btn = new Button(I18N.DISPLAY.create());
 
         btn.setId("idCreate"); //$NON-NLS-1$
         btn.setIcon(AbstractImagePrototype.create(Resources.ICONS.add()));
         btn.setEnabled(true);
 
         Menu actionsMenu = new Menu();
         actionsMenu.add(buildNewAnalysis());
         actionsMenu.add(buildNewWorkflow());
         btn.setMenu(actionsMenu);
 
         return btn;
     }
 
     private MenuItem buildNewAnalysis() {
         MenuItem new_analysis = new MenuItem(I18N.DISPLAY.createNewAnalysis());
 
         new_analysis.setId("idNewAnalysis"); //$NON-NLS-1$
         new_analysis.setIcon(AbstractImagePrototype.create(Resources.ICONS.add()));
         new_analysis.setEnabled(true);
 
         new_analysis.addSelectionListener(new SelectionListener<MenuEvent>() {
             @Override
             public void componentSelected(MenuEvent ce) {
                TitoWindow.launch();
             }
         });
 
         return new_analysis;
     }
 
     private MenuItem buildNewWorkflow() {
         MenuItem new_analysis = new MenuItem(I18N.DISPLAY.createNewWorkflow());
 
         new_analysis.setId("idNewWorkflow"); //$NON-NLS-1$
         new_analysis.setIcon(AbstractImagePrototype.create(Resources.ICONS.add()));
         new_analysis.setEnabled(true);
 
         new_analysis.addSelectionListener(new SelectionListener<MenuEvent>() {
             @Override
             public void componentSelected(MenuEvent ce) {
                 JSONObject payload = new JSONObject();
                 payload.put("tag", new JSONString(Constants.CLIENT.pipelineEditorTag())); //$NON-NLS-1$
 
                 String json = EventJSONFactory.build(EventJSONFactory.ActionType.DISPLAY_WINDOW,
                         payload.toString());
 
                 MessageDispatcher dispatcher = MessageDispatcher.getInstance();
                 dispatcher.processMessage(json);
             }
         });
 
         return new_analysis;
     }
 
     private Button buildMoreActionsButton() {
         Button actions = new Button(I18N.DISPLAY.moreActions());
         actions.setId(ACTION_ID_ACTIONS);
         buttons.put(ACTION_ID_ACTIONS, actions);
         Menu actionsMenu = new Menu();
         actionsMenu.add(buildEditMenuItem());
         actionsMenu.add(buildDeleteMenuItem());
         actionsMenu.add(buildSubmitMenuItem());
         actions.setMenu(actionsMenu);
         return actions;
     }
 
     private MenuItem buildAddFavouriteMenuItem() {
         MenuItem addFav = new MenuItem(I18N.DISPLAY.markFav());
         addFav.setId(ACTION_ID_MARK_FAV);
         addFav.setIcon(AbstractImagePrototype.create(Resources.ICONS.addFav()));
         addFav.setEnabled(true);
         addFav.addSelectionListener(new SelectionListener<MenuEvent>() {
 
             @Override
             public void componentSelected(MenuEvent ce) {
                 markAsFav(analysisGrid.getSelectionModel().getSelectedItem().getId(), true);
             }
         });
         menuItems.put(ACTION_ID_MARK_FAV, addFav);
         return addFav;
     }
 
     private void markAsFav(String id, final boolean fav) {
         UserInfo info = UserInfo.getInstance();
         if (info != null) {
             getTemplateService().favoriteAnalysis(info.getWorkspaceId(), id, fav,
                     new AsyncCallback<String>() {
 
                         @Override
                         public void onFailure(Throwable caught) {
                             ErrorHandler.post(I18N.ERROR.favServiceFailure(), caught);
 
                         }
 
                         @Override
                         public void onSuccess(String result) {
                             updateFavPreference(fav);
                             checkAndBuildFavMenu(fav);
                             fireAnalysisGroupCountUpdateEvent(fav, AnalysisGroupType.FAVORITES);
                         }
                     });
         } else {
             ErrorHandler.post(I18N.ERROR.retrieveUserInfoFailed());
         }
 
     }
 
     private void updateFavPreference(boolean fav) {
         Analysis a = analysisGrid.getSelectionModel().getSelectedItem();
         a.setUser_favourite(fav);
         analysisGrid.getStore().update(a);
     }
 
     private void fireAnalysisGroupCountUpdateEvent(boolean inc, AnalysisGroupType eventGroupType) {
         AnalysisGroupCountUpdateEvent event = new AnalysisGroupCountUpdateEvent(inc, eventGroupType);
         EventBus.getInstance().fireEvent(event);
     }
 
     private MenuItem buildRemoveFavouriteMenuItem() {
         MenuItem removeFav = new MenuItem(I18N.DISPLAY.removeFav());
         removeFav.setId(ACTION_ID_REMOVE_FAV);
         removeFav.setIcon(AbstractImagePrototype.create(Resources.ICONS.removeFav()));
         removeFav.setEnabled(true);
         removeFav.addSelectionListener(new SelectionListener<MenuEvent>() {
 
             @Override
             public void componentSelected(MenuEvent ce) {
                 markAsFav(analysisGrid.getSelectionModel().getSelectedItem().getId(), false);
             }
         });
         menuItems.put(ACTION_ID_REMOVE_FAV, removeFav);
         return removeFav;
     }
 
     private Button buildCopyButton() {
         Button copy = new Button(I18N.DISPLAY.copy());
 
         copy.setId(ACTION_ID_COPY);
         copy.setIcon(AbstractImagePrototype.create(Resources.ICONS.copy()));
         copy.setEnabled(false);
 
         copy.addSelectionListener(new SelectionListener<ButtonEvent>() {
             @Override
             public void componentSelected(ButtonEvent ce) {
                 final String id = selectedItem.getId();
 
                 // first check if this app can be exported to TITo
                 getTemplateService().analysisExportable(id, new AsyncCallback<String>() {
                     @Override
                     public void onSuccess(String result) {
                         JSONObject exportable = JsonUtil.getObject(result);
 
                         if (JsonUtil.getBoolean(exportable, "can-export", false)) { //$NON-NLS-1$
                             copyAnalysis(id);
                         } else {
                             ErrorHandler.post(JsonUtil.getString(exportable, "cause")); //$NON-NLS-1$
                         }
                     }
 
                     @Override
                     public void onFailure(Throwable caught) {
                         ErrorHandler.post(caught);
                     }
                 });
             }
         });
 
         buttons.put(ACTION_ID_COPY, copy);
 
         return copy;
     }
 
     private void copyAnalysis(final String id) {
         getTemplateService().copyAnalysis(id, new AsyncCallback<String>() {
             @Override
             public void onSuccess(String result) {
                 String copiedAnalysisId = JsonUtil.getString(JsonUtil.getObject(result), "analysis_id"); //$NON-NLS-1$
 
                 if (!copiedAnalysisId.isEmpty()) {
                     openTitoForEdit(copiedAnalysisId);
                 }
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(caught);
             }
         });
     }
 
     private void checkAndBuildFavMenu(boolean isFav) {
         Menu m = buttons.get(ACTION_ID_ACTIONS).getMenu();
         cleanUpFavMenuItems(m);
 
         if (current_category.getName().equalsIgnoreCase(DECatalogWindow.WORKSPACE) && !isFav) {
             // do nothing...
             return;
         }
 
         if ((current_category.getName().equalsIgnoreCase(DECatalogWindow.FAVORITES) || current_category
                 .getName().equalsIgnoreCase(DECatalogWindow.WORKSPACE)) && isFav) {
             // only remove
             m.add(buildRemoveFavouriteMenuItem());
             return;
         }
 
         // TODO: Remove hard caoding. Adding to fav from this category must be disabled in services
         if (!current_category.getName().equalsIgnoreCase(DECatalogWindow.APPLICATIONS_UNDER_DEVLOPMENT)) {
             if (isFav) {
                 m.add(buildRemoveFavouriteMenuItem());
             } else {
                 m.add(buildAddFavouriteMenuItem());
             }
         }
     }
 
     private void cleanUpFavMenuItems(Menu m) {
         if (m.getItemByItemId(ACTION_ID_MARK_FAV) != null) {
             m.remove(m.getItemByItemId(ACTION_ID_MARK_FAV));
         }
 
         if (m.getItemByItemId(ACTION_ID_REMOVE_FAV) != null) {
             m.remove(m.getItemByItemId(ACTION_ID_REMOVE_FAV));
         }
     }
 
     private class RunButtonSelectionListener extends SelectionListener<ButtonEvent> {
         @Override
         public void componentSelected(ButtonEvent ce) {
             Analysis analysis = analysisGrid.getSelectionModel().getSelectedItem();
             EventBus bus = EventBus.getInstance();
             UserEvent e = new UserEvent(Constants.CLIENT.windowTag(), analysis.getId());
             bus.fireEvent(e);
         }
     }
 
     private class DeleteMenuItemSelectionListener extends SelectionListener<MenuEvent> {
         @Override
         public void componentSelected(MenuEvent ce) {
             MessageBox.confirm(I18N.DISPLAY.warning(), I18N.DISPLAY.appDeleteWarning(),
                     new DeleteMessageBoxListener());
 
         }
     }
 
     private class AppGridViewConfig extends GridViewConfig {
         @Override
         public String getRowStyle(ModelData md, int rowIndex, @SuppressWarnings("rawtypes") ListStore ds) {
             if (md != null) {
                 Analysis a = (Analysis)md;
                 if (a.isDisabled()) {
                     return "disabled_app_background";
                 }
             }
 
             return "";
         }
     }
 
     private final class DeleteMessageBoxListener implements Listener<MessageBoxEvent> {
         @Override
         public void handleEvent(MessageBoxEvent ce) {
             Button btn = ce.getButtonClicked();
             // did the user click yes?
             if (btn.getItemId().equals(Dialog.YES)) {
                 UserInfo info = UserInfo.getInstance();
                 if (info != null) {
                     getTemplateService().deleteAnalysisFromWorkspace(info.getFullUsername(),
                             selectedItem.getId(), new AsyncCallback<String>() {
 
                                 @Override
                                 public void onFailure(Throwable caught) {
                                     ErrorHandler.post(I18N.ERROR.appRemoveFailure(), caught);
                                 }
 
                                 @Override
                                 public void onSuccess(String result) {
                                     analysisGrid.getStore().remove(selectedItem);
                                     fireAnalysisGroupCountUpdateEvent(false, null);
                                 }
                             });
                 }
             }
         }
     }
 
     private final class GridSelectionChangeListener implements Listener<BaseEvent> {
         @Override
         public void handleEvent(BaseEvent be) {
             Collection<Button> items = buttons.values();
             Collection<MenuItem> mItems = menuItems.values();
             if (analysisGrid.getSelectionModel().getSelectedItems().size() > 0) {
                 selectedItem = analysisGrid.getSelectionModel().getSelectedItem();
                 for (Button b : items) {
                     b.enable();
                 }
 
                 if (selectedItem.isDisabled()) {
                     buttons.get(ACTION_ID_RUN).disable();
                 }
 
                 if (!current_category.isPublic()) {
                     for (MenuItem mi : mItems) {
                         mi.enable();
                     }
 
                     if (selectedItem.isPublic()) {
                         menuItems.get(ACTION_ID_MAKE_PUBLIC).disable();
                     }
                 }
                 checkAndBuildFavMenu(selectedItem.isUser_favourite());
 
             } else {
                 for (Button b : items) {
                     b.disable();
                 }
                 for (MenuItem mi : mItems) {
                     mi.disable();
                 }
             }
 
         }
     }
 
     /**
      * Displays app names as hyperlinks; clicking a link runs the app.
      */
     public class AppNameCellRenderer implements GridCellRenderer<Analysis> {
         @Override
         public Object render(final Analysis model, String property, ColumnData config, int rowIndex,
                 int colIndex, ListStore<Analysis> store, Grid<Analysis> grid) {
             String name = null;
             if (model.isUser_favourite()) {
                 name = "<img src='./images/fav.png'/>&nbsp;" + model.getName(); //$NON-NLS-1$
             } else {
                 name = model.getName();
             }
 
             if (!model.isDisabled()) {
                 Hyperlink link = new Hyperlink(name, "analysis_name"); //$NON-NLS-1$
                 link.addListener(Events.OnClick, new Listener<BaseEvent>() {
 
                     @Override
                     public void handleEvent(BaseEvent be) {
                         EventBus bus = EventBus.getInstance();
                         UserEvent e = new UserEvent(Constants.CLIENT.windowTag(), model.getId());
                         bus.fireEvent(e);
                     }
                 });
                 link.setWidth(model.getName().length());
                 return link;
             } else {
                 name = "<img title ='"
                         + org.iplantc.core.uiapplications.client.I18N.DISPLAY.appUnavailable()
                         + "' src='./images/exclamation.png'/>&nbsp;" + name;
                 return name;
             }
         }
     }
 
     private class VotingCellRenderer extends RenderVotingCell {
         private final int NUM_RATING_ICONS = RATING_CONSTANT.values().length;
 
         @Override
         protected HorizontalPanel buildPanel(Analysis model) {
             HorizontalPanel hp = super.buildPanel(model);
             hp.setSize(120, 20);
             if (model.getFeedback().getUser_score() > 0) {
                 addUnrateIcon(model, hp);
             }
             return hp;
         }
 
         /** adds a "remove rating" icon after the rating icons */
         private void addUnrateIcon(Analysis model, LayoutContainer parent) {
             parent.add(buildUnrateIcon(model, parent));
         }
 
         /** removes the "remove rating" icon if it is present */
         private void removeUnrateIcon(LayoutContainer parent) {
             if (parent.getItemCount() > NUM_RATING_ICONS) {
                 parent.remove(parent.getWidget(NUM_RATING_ICONS));
             }
         }
 
         private IconButton buildUnrateIcon(final Analysis model, final LayoutContainer hp) {
             final IconButton icon = new IconButton("apps_rating_unrate_button"); //$NON-NLS-1$
 
             icon.setToolTip(I18N.DISPLAY.unrate());
             icon.setId("unrate_button"); //$NON-NLS-1$
 
             icon.addSelectionListener(new SelectionListener<IconButtonEvent>() {
                 @Override
                 public void componentSelected(IconButtonEvent ce) {
                     removeUnrateIcon(hp);
                     hp.layout();
 
                     Long commentId = null;
                     try {
                         AnalysisFeedback feedback = model.getFeedback();
                         if (feedback != null) {
                             commentId = feedback.getComment_id();
                         }
                     } catch (NumberFormatException e) {
                         // comment id empty or not a number, leave it null and proceed
                     }
 
                     getTemplateService().deleteRating(model.getId(), model.getName(), commentId,
                             new AsyncCallback<String>() {
                                 @Override
                                 public void onSuccess(String result) {
                                     updateFeedback(model, result);
                                 }
 
                                 @Override
                                 public void onFailure(Throwable caught) {
                                     ErrorHandler.post(caught.getMessage(), caught);
                                 }
                             });
                 }
 
                 /**
                  * Updates the user rating and the average rating
                  * 
                  * @param model
                  * @param json a JSON string containing the new average rating
                  */
                 private void updateFeedback(Analysis model, String json) {
                     AnalysisFeedback userFeedback = model.getFeedback();
                     userFeedback.setUser_score(0);
                     userFeedback.setComment_id(null);
 
                     if (json == null || json.isEmpty()) {
                         userFeedback.setAverage_score(0);
                     } else {
                         JSONObject jsonObj = JsonUtil.getObject(json);
                         if (jsonObj != null) {
                             double newAverage = JsonUtil.getNumber(jsonObj, "avg").doubleValue(); //$NON-NLS-1$
                             userFeedback.setAverage_score(newAverage);
                         }
                     }
 
                     resetRatingStarColors(userFeedback, hp);
 
                     icon.setStyleName("apps_rating_unrate_button"); //$NON-NLS-1$
                 }
             });
 
             icon.addListener(Events.OnMouseOver, new Listener<BaseEvent>() {
                 @Override
                 public void handleEvent(BaseEvent be) {
                     if (model.getFeedback().getUser_score() > 0) {
                         icon.setStyleName("apps_rating_unrate_button_hover"); //$NON-NLS-1$
                     }
                 }
             });
             icon.addListener(Events.OnMouseOut, new Listener<BaseEvent>() {
                 @Override
                 public void handleEvent(BaseEvent be) {
                     icon.setStyleName("apps_rating_unrate_button"); //$NON-NLS-1$
                 }
             });
 
             return icon;
         }
 
         @Override
         protected IconButton buildStar(String style, int index, final Analysis model) {
             IconButton icon = super.buildStar(style, index, model);
 
             final AnalysisFeedback userFeedback = model.getFeedback();
             if (userFeedback != null) {
                 icon.addListener(Events.OnMouseOver, new Listener<BaseEvent>() {
                     @Override
                     public void handleEvent(BaseEvent be) {
                         IconButton icon = (IconButton)be.getSource();
                         Integer hoveredRating = icon.getData("index"); //$NON-NLS-1$
 
                         HorizontalPanel pnlParent = (HorizontalPanel)icon.getParent();
 
                         int i;
                         for (i = 0; i <= hoveredRating; i++) {
                             icon = getIcon(pnlParent, i);
                             icon.setStyleName("apps_rating_gold_button"); //$NON-NLS-1$
                         }
                         for (int j = i; j < ratings.size(); j++) {
                             icon = getIcon(pnlParent, j);
                             icon.setStyleName("apps_rating_white_button"); //$NON-NLS-1$
                         }
                     }
                 });
 
                 icon.addSelectionListener(new SelectionListener<IconButtonEvent>() {
                     @Override
                     public void componentSelected(IconButtonEvent ce) {
                         IconButton icon = (IconButton)ce.getSource();
                         final Integer score = (Integer)icon.getData("index") + 1; //$NON-NLS-1$
                         onRate(model, score, (LayoutContainer)icon.getParent());
                     }
                 });
             }
 
             return icon;
         }
 
         /** Called when a rating star is clicked. Shows the comment dialog. */
         protected void onRate(final Analysis model, final int score, final LayoutContainer parent) {
             if (model == null) {
                 return;
             }
 
             // populate dialog via an async call if previous comment ID exists, otherwise show blank dlg
             final AppCommentDialog dlg = new AppCommentDialog(model.getName());
             Long commentId = model.getFeedback().getComment_id();
             if (commentId == null) {
                 dlg.unmaskDialog();
             } else {
                 ConfluenceServiceFacade.getInstance().getComment(commentId, new AsyncCallback<String>() {
                     @Override
                     public void onSuccess(String comment) {
                         dlg.setText(comment);
                         dlg.unmaskDialog();
                     }
 
                     @Override
                     public void onFailure(Throwable e) {
                         ErrorHandler.post(e.getMessage(), e);
                         dlg.unmaskDialog();
                     }
                 });
             }
             Command onConfirm = new Command() {
                 @Override
                 public void execute() {
                     persistRating(model, score, dlg.getComment(), parent);
                 }
             };
             dlg.setCommand(onConfirm);
             dlg.show();
         }
 
         /** saves a rating to the database and the wiki page */
         private void persistRating(final Analysis model, final int score, String comment,
                 final LayoutContainer parent) {
 
             AsyncCallback<String> callback = new AsyncCallback<String>() {
                 @Override
                 public void onSuccess(String result) {
                     AnalysisFeedback userFeedback = model.getFeedback();
                     int userScoreBefore = userFeedback.getUser_score();
 
                     try {
                         userFeedback.setComment_id(Long.valueOf(result));
                     } catch (NumberFormatException e) {
                         // no comment id, do nothing
                     }
 
                     userFeedback.setUser_score(score);
 
                     resetRatingStarColors(userFeedback, parent);
                     if (parent != null) {
                         if (userScoreBefore <= 0) {
                             addUnrateIcon(model, parent);
                         }
                         parent.layout(); // so the unrate icon shows
                     }
                 }
 
                 @Override
                 public void onFailure(Throwable caught) {
                     ErrorHandler.post(caught.getMessage(), caught);
                 }
             };
 
             Long commentId = model.getFeedback().getComment_id();
             if (commentId == null) {
                 getTemplateService().rateAnalysis(model.getId(), score, model.getName(), comment,
                         model.getIntegratorsEmail(), callback);
             } else {
                 getTemplateService().updateRating(model.getId(), score, model.getName(), commentId,
                         comment, model.getIntegratorsEmail(), callback);
             }
         }
     }
 }
