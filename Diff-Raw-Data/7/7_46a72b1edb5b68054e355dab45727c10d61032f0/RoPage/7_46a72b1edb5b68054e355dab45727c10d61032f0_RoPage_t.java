 package pl.psnc.dl.wf4ever.portal.pages.ro;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
import java.util.Collections;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.concurrent.Callable;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.Component;
 import org.apache.wicket.RestartResponseException;
 import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
 import org.apache.wicket.markup.head.IHeaderResponse;
 import org.apache.wicket.markup.head.StringHeaderItem;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.purl.wf4ever.rosrs.client.ResearchObject;
 import org.purl.wf4ever.rosrs.client.Utils;
 import org.purl.wf4ever.rosrs.client.notifications.Notification;
 import org.purl.wf4ever.rosrs.client.notifications.NotificationService;
 
 import pl.psnc.dl.wf4ever.portal.MySession;
 import pl.psnc.dl.wf4ever.portal.components.LoadingCircle;
 import pl.psnc.dl.wf4ever.portal.pages.BasePage;
 import pl.psnc.dl.wf4ever.portal.pages.Error404Page;
 
 import com.google.common.collect.Multimap;
 import com.sun.jersey.api.client.ClientResponse;
 
 /**
  * The Research Object page.
  * 
  * @author piotrekhol
  * 
  */
 public class RoPage extends BasePage {
 
     /** id. */
     private static final long serialVersionUID = 1L;
 
     /** Logger. */
     static final Logger LOG = Logger.getLogger(RoPage.class);
 
     /** Template for HTML Link Headers. */
     private static final String HTML_LINK_TEMPLATE = "<link rel=\"%s\" href=\"%s\"/>";
 
 
     /**
      * Constructor.
      * 
      * @param parameters
      *            page parameters
      * @throws URISyntaxException
      *             if URIs returned by the RODL are incorrect
      */
     public RoPage(final PageParameters parameters)
             throws URISyntaxException {
         super(parameters);
         if (parameters.get("ro").isEmpty()) {
             throw new RestartResponseException(Error404Page.class, new PageParameters().add("message",
                 "The RO URI is missing."));
         }
         URI roURI = new URI(parameters.get("ro").toString());
         Model<ResearchObject> researchObjectModel = new Model<ResearchObject>(new ResearchObject(roURI, MySession.get()
                 .getRosrs()));
         this.setDefaultModel(researchObjectModel);
         add(new AjaxLazyLoadPanel("lazy", researchObjectModel) {
 
             /** id. */
             private static final long serialVersionUID = 8101533630015610845L;
 
 
             @SuppressWarnings("unchecked")
             @Override
             public Component getLazyLoadComponent(String id) {
                 return new RoPanel(id, (IModel<ResearchObject>) getDefaultModel());
             }
 
 
             @Override
             public Component getLoadingComponent(String markupId) {
                 return new LoadingCircle(markupId, "Loading research object metadata...");
             }
         });
     }
 
 
     /**
      * Create a new task of loading the notifications that can be scheduled for later.
      * 
      * @param notificationService
      *            notification service
      * @param model
      *            RO model
      * @return a new {@link Callable}
      */
     static Callable<ArrayList<Notification>> createNotificationsCallable(final NotificationService notificationService,
             final IModel<ResearchObject> model) {
         return new Callable<ArrayList<Notification>>() {
 
             @Override
             public ArrayList<Notification> call()
                     throws Exception {
                ArrayList<Notification> notifications = notificationService.getNotifications(
                    model.getObject().getUri(), null, null);
                Collections.reverse(notifications);
                return notifications;
             }
         };
     }
 
 
     @Override
     public void renderHead(IHeaderResponse response) {
         super.renderHead(response);
         ResearchObject researchObject = (ResearchObject) getDefaultModelObject();
         try {
             ClientResponse head = MySession.get().getRosrs().getResourceHead(researchObject.getUri());
             List<String> headers = head.getHeaders().get("Link");
             if (headers != null && !headers.isEmpty()) {
                 Multimap<String, URI> links = Utils.getLinkHeaders(headers);
                 for (Entry<String, URI> link : links.entries()) {
                     response.render(StringHeaderItem.forString(String.format(HTML_LINK_TEMPLATE, link.getKey(),
                         link.getValue())));
                 }
             }
             head.close();
         } catch (Exception e) {
             LOG.error("Unexpected response when getting RO head", e);
             throw new RestartResponseException(Error404Page.class, new PageParameters().add("message", "The RO "
                     + researchObject.getUri() + " appears to be incorrect."));
         }
     }
 
 }
