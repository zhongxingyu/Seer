 package de.flower.rmt.ui.page.event.manager.invitations;
 
 import de.flower.common.ui.ajax.event.AjaxEventListener;
 import de.flower.common.ui.ajax.markup.html.AjaxLink;
 import de.flower.common.ui.markup.html.list.EntityListView;
 import de.flower.common.ui.modal.ModalDialogWindow;
 import de.flower.common.ui.panel.BasePanel;
 import de.flower.common.util.Check;
 import de.flower.rmt.model.db.entity.Invitation;
 import de.flower.rmt.model.db.entity.Invitation_;
 import de.flower.rmt.model.db.entity.event.Event;
 import de.flower.rmt.model.db.type.RSVPStatus;
 import de.flower.rmt.service.IInvitationManager;
 import de.flower.rmt.ui.app.Links;
 import de.flower.rmt.ui.page.event.CommentsPanel;
 import de.flower.rmt.ui.panel.DropDownMenuPanel;
 import de.flower.rmt.util.Dates;
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.datetime.markup.html.basic.DateLabel;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.AbstractLink;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.Fragment;
 import org.apache.wicket.model.*;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import javax.mail.internet.InternetAddress;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author flowerrrr
  */
 public class InvitationListPanel extends BasePanel {
 
     @SpringBean
     private IInvitationManager invitationManager;
 
     public InvitationListPanel(IModel<Event> model) {
         this(null, model);
     }
 
     public InvitationListPanel(String id, IModel<Event> model) {
         super(id);
         Check.notNull(model);
         addList(RSVPStatus.ACCEPTED, model);
         addList(RSVPStatus.UNSURE, model);
         addList(RSVPStatus.DECLINED, model);
         addList(RSVPStatus.NORESPONSE, model);
         add(new AjaxEventListener(Invitation.class));
     }
 
     private void addList(RSVPStatus status, IModel<Event> model) {
         final IModel<List<Invitation>> listModel = getInvitationList(model, status);
        add(createHead(status.name().toLowerCase() + "ListHead", RSVPStatus.ACCEPTED, listModel));
        add(createListView(status.name().toLowerCase() + "List", RSVPStatus.ACCEPTED, listModel));
     }
 
     private Component createHead(String id, RSVPStatus status, final IModel<List<Invitation>> listModel) {
         WebMarkupContainer head = new WebMarkupContainer(id);
         Fragment frag = new Fragment("tHead", "fragmentHead", this);
         frag.add(new Label("heading", new ResourceModel("invitation." + status.name().toLowerCase())));
         frag.add(new Label("num", new AbstractReadOnlyModel<String>() {
             @Override
             public String getObject() {
                 return "(" + listModel.getObject().size() + ")";
             }
         }));
         final IModel<List<InternetAddress>> emailAddressesModel = new AbstractReadOnlyModel<List<InternetAddress>>() {
             @Override
             public List<InternetAddress> getObject() {
                 final List<InternetAddress> emailAddresses = new ArrayList<>();
                 for (Invitation invitation : listModel.getObject()) {
                     if (invitation.hasEmail()) {
                         emailAddresses.add(invitation.getInternetAddress());
                     }
                 }
                 return emailAddresses;
             }
         };
         frag.add(Links.mailLink("emailLink", emailAddressesModel));
         head.add(frag);
         return head;
     }
 
     private Component createListView(String id, RSVPStatus status, IModel<List<Invitation>> listModel) {
         ListView list = new EntityListView<Invitation>(id, listModel) {
             @Override
             protected void populateItem(ListItem<Invitation> item) {
                 item.add(createInvitationFragement(item));
             }
         };
         return list;
     }
 
     private Component createInvitationFragement(ListItem<Invitation> item) {
         final Invitation invitation = item.getModelObject();
         Fragment frag = new Fragment("itemPanel", "fragmentRow", this);
         frag.add(new Label("name", invitation.getName()));
         frag.add(DateLabel.forDatePattern("date", Model.of(invitation.getDate()), Dates.DATE_TIME_SHORT));
         frag.add(new CommentsPanel(item.getModel()));
         // now the dropdown menu
         DropDownMenuPanel menuPanel = new DropDownMenuPanel();
         menuPanel.addLink(createEditLink("link", item), "button.edit");
         if (invitation.hasEmail()) {
             menuPanel.addLink(Links.mailLink("link", invitation.getEmail(), null), "button.email");
         }
         frag.add(menuPanel);
         return frag;
     }
 
     private AbstractLink createEditLink(String id, ListItem<Invitation> item) {
         return new AjaxLink<Invitation>(id, item.getModel()) {
 
             @Override
             public void onClick(final AjaxRequestTarget target) {
                 InvitationEditPanel content = new InvitationEditPanel(getModel());
                 ModalDialogWindow.showContent(this, content, 5);
             }
         };
     }
 
     private IModel<List<Invitation>> getInvitationList(final IModel<Event> model, final RSVPStatus rsvpStatus) {
         return new LoadableDetachableModel<List<Invitation>>() {
             @Override
             protected List<Invitation> load() {
                 return invitationManager.findAllByEventAndStatus(model.getObject(), rsvpStatus, Invitation_.user, Invitation_.comments);
             }
         };
     }
 }
