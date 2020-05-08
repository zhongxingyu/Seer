 package net.cyklotron.cms.modules.views.popup;
 
 import org.objectledge.parameters.Parameters;
 import org.objectledge.parameters.RequestParameters;
 import org.objectledge.templating.Template;
 import org.objectledge.web.mvc.builders.BuildException;
 import org.objectledge.web.mvc.builders.DefaultBuilder;
 
 /**
  * Screen which automaticaly closes it's window.
  *
  * <p>It is useful when a popup has a form which when posted must close the popup,
  * but cannot change the state of the opening window.</p>
  *
  * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: ClosePopup.java,v 1.5 2005-03-09 10:00:40 pablo Exp $
  */
 public class ClosePopup extends DefaultBuilder
 {
     
     public ClosePopup(org.objectledge.context.Context context)
     {
         super(context);
     }
     
     public static String CLOSE_POPUP_SCREEN_CONTENT =
         "<script type=\"text/javascript\">\n"+
         "<!--\n"+
         "function closePopup()\n"+
         "{\n"+
         "    window.close();\n"+
         "}\n"+
         "closePopup();\n"+
         "// -->\n"+
         "</script>\n";
     
     public static String CLOSE_POPUP_RELOAD_OPENER_SCREEN_CONTENT =
         "<script type=\"text/javascript\">\n"+
         "<!--\n"+
         "function closePopup()\n"+
         "{\n"+
         "    window.opener.location.href = window.opener.location.href;\n"+
         "    window.close();\n"+
         "}\n"+
         "closePopup();\n"+
         "// -->\n"+
         "</script>\n";
     
     
    public String build(Template template, String embeddedBuildResults) 
        throws BuildException
     {
         Parameters parameters = RequestParameters.getRequestParameters(context);
         if(parameters.isDefined("close_popup_reload"))
         {
             return CLOSE_POPUP_RELOAD_OPENER_SCREEN_CONTENT;
         }
         else
         {
             return CLOSE_POPUP_SCREEN_CONTENT;
         }
     }
     
 }
