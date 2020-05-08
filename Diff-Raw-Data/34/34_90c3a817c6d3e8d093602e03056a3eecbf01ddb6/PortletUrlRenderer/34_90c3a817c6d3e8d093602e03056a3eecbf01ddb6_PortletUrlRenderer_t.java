 package org.apache.wicket.portlet;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.wicket.request.Request;
 import org.apache.wicket.request.Url;
 import org.apache.wicket.request.UrlRenderer;
 import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;
 
 public class PortletUrlRenderer extends UrlRenderer
 {
   Request r;
   public PortletUrlRenderer(Request request)
   {
     super(request);
    this.r = request;
   }
   
   public String renderRelativeUrl(final Url url)
   {
     Args.notNull(url, "url");
 
     List<String> baseUrlSegments = getBaseUrl().getSegments();
     List<String> urlSegments = new ArrayList<String>(url.getSegments());
 
     List<String> newSegments = new ArrayList<String>();
 
     int common = 0;
 
     String last = null;
 
     for (String s : baseUrlSegments)
     {
       if (!urlSegments.isEmpty() && s.equals(urlSegments.get(0)))
       {
         ++common;
         last = urlSegments.remove(0);
       }
       else
       {
         break;
       }
     }
 
     // we want the new URL to have at least one segment (other than possible ../)
     if ((last != null) && (urlSegments.isEmpty() || (baseUrlSegments.size() == common)))
     {
       --common;
       urlSegments.add(0, last);
     }
 
     int baseUrlSize = baseUrlSegments.size();
     if (common + 1 == baseUrlSize && urlSegments.isEmpty())
     {
       newSegments.add(".");
     }
     else
     {
       for (int i = common + 1; i < baseUrlSize; ++i)
       {
         newSegments.add("..");
       }
     }
     newSegments.addAll(urlSegments);
 
     String renderedUrl = new Url(newSegments, url.getQueryParameters()).toString();
     if (!renderedUrl.startsWith(".."))
     {
       // WICKET-4260
      if(!renderedUrl.startsWith("/"))
         renderedUrl = "./" + renderedUrl;
     }
     if (renderedUrl.endsWith(".."))
     {
       // WICKET-4401
       renderedUrl = renderedUrl + '/';
     }
     return renderedUrl;
   }
  
  
  @Override
  protected boolean shouldRenderAsFull(Url url)
  {
    Url clientUrl = r.getClientUrl();

    if (!Strings.isEmpty(url.getProtocol()) &&
      !url.getProtocol().equals(clientUrl.getProtocol()))
    {
      return true;
    }
    if (!Strings.isEmpty(url.getHost()) && !url.getHost().equals(clientUrl.getHost()))
    {
      return true;
    }
    if ((url.getPort() != null) && !url.getPort().equals(clientUrl.getPort()))
    {
      return true;
    }
    return false;
  }
 
 }
