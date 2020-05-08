 package de.flower.rmt.ui.manager.page.players;
 
 import de.flower.common.ui.ajax.AjaxLinkWithConfirmation;
 import de.flower.common.ui.ajax.MyAjaxLink;
 import de.flower.common.ui.ajax.updatebehavior.AjaxRespondListener;
 import de.flower.common.ui.ajax.updatebehavior.AjaxUpdateBehavior;
 import de.flower.common.ui.ajax.updatebehavior.events.Event;
 import de.flower.rmt.model.Users;
 import de.flower.rmt.model.Users_;
 import de.flower.rmt.service.IUserManager;
 import de.flower.rmt.ui.common.page.ModalDialogWindow;
 import de.flower.rmt.ui.manager.ManagerBasePage;
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.ResourceModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import java.util.List;
 
 /**
  * @author oblume
  */
 public class PlayersPage extends ManagerBasePage {
 
     @SpringBean
     private IUserManager playerManager;
 
     public PlayersPage() {
 
        final ModalDialogWindow modal = new ModalDialogWindow("playerDialog");
         final PlayerEditPanel playerEditPanel = new PlayerEditPanel(modal.getContentId());
         modal.setContent(playerEditPanel);
         add(modal);
 
         add(new MyAjaxLink("newButton") {
 
             @Override
             public void onClick(AjaxRequestTarget target) {
                // show modal dialog with edit form.
                 playerEditPanel.init(null);
                 modal.show(target);
             }
         });
 
         WebMarkupContainer playerListContainer = new WebMarkupContainer("playerListContainer");
         add(playerListContainer);
         playerListContainer.add(new ListView<Users>("playerList", getPlayerListModel()) {
 
             @Override
             protected void populateItem(final ListItem<Users> item) {
                 Users player = item.getModelObject();
                 item.add(new Label("fullname", player.getFullname()));
                 item.add(new Label("email", player.getEmail()));
                 Component manager;
                 item.add(manager = new WebMarkupContainer("manager"));
                 manager.setVisible(player.isManager());
                 item.add(new MyAjaxLink("editButton") {
 
                     @Override
                     public void onClick(AjaxRequestTarget target) {
                         playerEditPanel.init(item.getModel());
                         modal.show(target);
                     }
                 });
                 item.add(new AjaxLinkWithConfirmation("deleteButton", new ResourceModel("manager.players.delete.confirm")) {
 
                     @Override
                     public void onClick(AjaxRequestTarget target) {
                         playerManager.delete(item.getModelObject());
                         target.registerRespondListener(new AjaxRespondListener(Event.EntityDeleted(Users.class)));
                     }
                 });
             }
         });
         playerListContainer.add(new AjaxUpdateBehavior(Event.EntityAll(Users.class)));
     }
 
 
     private IModel<List<Users>> getPlayerListModel() {
         return new LoadableDetachableModel<List<Users>>() {
             @Override
             protected List<Users> load() {
                 return playerManager.findAll(Users_.authorities);
             }
         };
     }
 }
