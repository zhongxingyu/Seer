 /**
  * 
  */
 package pl.psnc.dl.wf4ever.portal.pages.ro;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Random;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.behavior.AttributeAppender;
 import org.apache.wicket.behavior.Behavior;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.ExternalLink;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.PropertyModel;
 
 import pl.psnc.dl.wf4ever.portal.model.RoEvoNode;
 import pl.psnc.dl.wf4ever.portal.model.RoEvoNode.EvoClassModifier;
 import pl.psnc.dl.wf4ever.portal.services.RoEvoService;
 
 /**
  * A panel for displaying the RO evolution surroundings.
  * 
  * @author Piotr Hołubowicz
  * 
  */
 public class RoEvoBox extends Panel {
 
     /** id. */
     private static final long serialVersionUID = -3775797988389365540L;
 
 
     /**
      * Constructor.
      * 
      * @param id
      *            wicket id
      * @param sparqlEndpointURI
      *            RODL SPARQL endpoint URI
      * @param researchObjectURI
      *            RO URI
      * @throws IOException
      *             when there are problems with connecting to the SPARQL endpoint
      * @throws URISyntaxException
      *             when data from the SPARQL endpoint contain invalid URIs
      */
     @SuppressWarnings("serial")
     public RoEvoBox(String id, URI sparqlEndpointURI, final URI researchObjectURI)
             throws IOException, URISyntaxException {
         super(id);
 
         setOutputMarkupId(true);
         setOutputMarkupPlaceholderTag(true);
 
         WebMarkupContainer sources = new WebMarkupContainer("sources");
         add(sources);
         WebMarkupContainer live = new WebMarkupContainer("live");
         add(live);
         WebMarkupContainer snapshots = new WebMarkupContainer("snapshots");
         add(snapshots);
         WebMarkupContainer archived = new WebMarkupContainer("archived");
         add(archived);
         WebMarkupContainer forks = new WebMarkupContainer("forks");
         add(forks);
 
         Collection<RoEvoNode> nodes = RoEvoService.describeSnapshot(sparqlEndpointURI, researchObjectURI);
         List<RoEvoNode> preorder = new ArrayList<>();
         final List<RoEvoNode> postorder = new ArrayList<>();
         for (RoEvoNode node : nodes) {
             if (!preorder.contains(node)) {
                 visit(node, preorder, postorder);
             }
         }
         final int dx = 120, ox = 20;
         List<RoEvoNode> sourceNodes = new ArrayList<>();
         List<RoEvoNode> liveNodes = new ArrayList<>();
         List<RoEvoNode> snapshotNodes = new ArrayList<>();
         List<RoEvoNode> archivedNodes = new ArrayList<>();
         List<RoEvoNode> forkNodes = new ArrayList<>();
         for (RoEvoNode node : nodes) {
             if (node.getEvoClassModifier() == EvoClassModifier.SOURCE && node.getItsLiveROs().isEmpty()) {
                 sourceNodes.add(node);
                 continue;
             }
             if (node.getEvoClassModifier() == EvoClassModifier.FORK && !node.getUri().equals(researchObjectURI)) {
                 forkNodes.add(node);
                 continue;
             }
             switch (node.getEvoClass()) {
                 case LIVE:
                     liveNodes.add(node);
                     break;
                 case SNAPSHOT:
                     snapshotNodes.add(node);
                     break;
                 case ARCHIVED:
                     archivedNodes.add(node);
                     break;
                 default:
                     snapshotNodes.add(node);
                     break;
             }
         }
         if (!sourceNodes.isEmpty()) {
             sources.add(new ListView<RoEvoNode>("sourceNodes", sourceNodes) {
 
                 @Override
                 protected void populateItem(ListItem<RoEvoNode> item) {
                     populateRoEvoNode(item, researchObjectURI, ox + dx * postorder.indexOf(item.getModelObject()));
                 }
 
             });
         } else {
             sources.setVisible(false);
         }
         if (!liveNodes.isEmpty()) {
             live.add(new ListView<RoEvoNode>("liveNodes", liveNodes) {
 
                 @Override
                 protected void populateItem(ListItem<RoEvoNode> item) {
                     populateRoEvoNode(item, researchObjectURI, ox + dx * postorder.indexOf(item.getModelObject()));
                 }
 
             });
         } else {
             live.setVisible(false);
         }
         if (!snapshotNodes.isEmpty()) {
             snapshots.add(new ListView<RoEvoNode>("snapshotNodes", snapshotNodes) {
 
                 @Override
                 protected void populateItem(ListItem<RoEvoNode> item) {
                     populateRoEvoNode(item, researchObjectURI, ox + dx * postorder.indexOf(item.getModelObject()));
                 }
 
             });
         } else {
             snapshots.setVisible(false);
         }
         if (!archivedNodes.isEmpty()) {
             archived.add(new ListView<RoEvoNode>("archivedNodes", archivedNodes) {
 
                 @Override
                 protected void populateItem(ListItem<RoEvoNode> item) {
                     populateRoEvoNode(item, researchObjectURI, ox + dx * postorder.indexOf(item.getModelObject()));
                 }
 
             });
         } else {
             archived.setVisible(false);
         }
         if (!forkNodes.isEmpty()) {
             forks.add(new ListView<RoEvoNode>("forkNodes", forkNodes) {
 
                 @Override
                 protected void populateItem(ListItem<RoEvoNode> item) {
                     populateRoEvoNode(item, researchObjectURI, ox + dx * postorder.indexOf(item.getModelObject()));
                 }
 
             });
         } else {
             forks.setVisible(false);
         }
 
         add(new Behavior() {
 
             @Override
             public void renderHead(Component component, IHeaderResponse response) {
                 super.renderHead(component, response);
                 final StringBuilder sb = new StringBuilder();
                 sb.append("$('#evoroot').on('firstShow', function() {");
 
                 for (RoEvoNode source : postorder) {
                     for (RoEvoNode node : source.getItsLiveROs()) {
                         sb.append(createConnection(source, node, "Has live RO"));
                     }
                     for (RoEvoNode node : source.getPreviousSnapshots()) {
                         sb.append(createConnection(source, node, "Previous snapshot"));
                     }
                     for (RoEvoNode node : source.getDerivedResources()) {
                         sb.append(createConnection(source, node, "Derived from"));
                     }
 
                 }
 
                 sb.append("});");
 
                 response.renderOnLoadJavaScript(sb.toString());
             }
         });
     }
 
 
     /**
      * Used for topological sorting of ROs.
      * 
      * @param node
      *            visited node
      * @param preorder
      *            preorder list of nodes
      * @param postorder
      *            postorder list of nodes
      */
     private void visit(RoEvoNode node, List<RoEvoNode> preorder, List<RoEvoNode> postorder) {
         preorder.add(node);
         for (RoEvoNode n : node.getPreviousSnapshots()) {
             if (!preorder.contains(n)) {
                 visit(n, preorder, postorder);
             }
         }
         for (RoEvoNode n : node.getItsLiveROs()) {
             if (!preorder.contains(n)) {
                 visit(n, preorder, postorder);
             }
         }
         for (RoEvoNode n : node.getDerivedResources()) {
             if (!preorder.contains(n)) {
                 visit(n, preorder, postorder);
             }
         }
         postorder.add(node);
     }
 
 
     /**
      * Create the RO node for the visualization.
      * 
      * @param item
      *            list item containing the node
      * @param researchObjectURI
      *            RO URI
      * @param x
      *            the horizontal position of the node, in px
      */
     private void populateRoEvoNode(ListItem<RoEvoNode> item, final URI researchObjectURI, int x) {
         RoEvoNode node = item.getModelObject();
        item.add(new ExternalLink("labelOrIdentifier", new PropertyModel<String>(node, "uri.toString"),
                new PropertyModel<>(node, "labelOrIdentifier")));
         StringBuilder cssClasses = new StringBuilder();
         if (researchObjectURI.equals(item.getModelObject().getUri())) {
             cssClasses.append(" active");
         }
         item.add(new AttributeAppender("style", "left: " + x + "px;"));
         item.add(new AttributeAppender("class", cssClasses.toString()));
         item.add(new AttributeAppender("rel", "popover"));
         item.add(new AttributeAppender("data-content", node.getUri()));
         if (node.isResearchObject()) {
             item.add(new AttributeAppender("data-original-title", "Research Object"));
         } else {
             item.add(new AttributeAppender("data-original-title", "An external resource"));
         }
         item.add(new WebMarkupContainer("roMark").setVisible(node.isResearchObject()));
         node.setComponent(item);
         item.setOutputMarkupId(true);
     }
 
 
     /**
      * Create a JavaScript code that connects 2 nodes.
      * 
      * @param source
      *            source node
      * @param target
      *            target node
      * @param label
      *            connection label
      * @return JavaScript code
      */
     protected String createConnection(RoEvoNode source, RoEvoNode target, String label) {
 
         StringBuilder sb = new StringBuilder();
         String connId = "conn" + Math.abs(new Random().nextInt());
         sb.append("var " + connId + " = jsPlumb.connect({");
         sb.append("source: '" + source.getComponent().getMarkupId() + "',");
         sb.append("target: '" + target.getComponent().getMarkupId() + "',");
         sb.append("overlays:[ [ 'Label', { label:'" + label + "', id: 'label', cssClass : 'evolabel' } ] ]");
         sb.append("});");
         sb.append(connId + ".bind('mouseenter', function (c) { c.showOverlay('label'); });");
         sb.append(connId + ".bind('mouseexit', function (c) { c.hideOverlay('label'); });");
         sb.append(connId + ".hideOverlay('label');");
         return sb.toString();
     }
 
 }
