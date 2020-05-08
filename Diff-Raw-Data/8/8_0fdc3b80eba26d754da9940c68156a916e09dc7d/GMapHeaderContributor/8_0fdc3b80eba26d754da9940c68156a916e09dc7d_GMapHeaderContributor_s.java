 package wicket.contrib.gmap3;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.WicketAjaxReference;
 import org.apache.wicket.behavior.Behavior;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.WicketEventReference;
 import org.apache.wicket.request.resource.JavaScriptResourceReference;
 import org.apache.wicket.request.resource.ResourceReference;
 
 /**
  * The Class GMapHeaderContributor.
  */
 public class GMapHeaderContributor extends Behavior {
     private static final long serialVersionUID = 1L;
 
     // URL for Google Maps' API endpoint.
    private static final String GMAP_API_URL = "://maps.google.com/maps/api/js?v=3&amp;sensor=false";
 
     private static final String HTTP = "http";
 
     // We have some custom Javascript.
     private static final ResourceReference WICKET_GMAP_JS = new JavaScriptResourceReference(GMap.class,
             "wicket-gmap.js");
 
     String _schema;
 
     /**
      * Instantiates a new g map header contributor.
      */
     public GMapHeaderContributor() {
         _schema = HTTP;
     }
 
     /* (non-Javadoc)
      * @see org.apache.wicket.behavior.Behavior#renderHead(org.apache.wicket.Component, org.apache.wicket.markup.html.IHeaderResponse)
      */
     @Override
     public void renderHead(final Component component, final IHeaderResponse response) {
         response.renderJavaScriptReference(_schema + GMAP_API_URL);
         response.renderJavaScriptReference(WicketEventReference.INSTANCE);
         response.renderJavaScriptReference(WicketAjaxReference.INSTANCE);
 
         response.renderJavaScriptReference(WICKET_GMAP_JS);
     }
 
 }
