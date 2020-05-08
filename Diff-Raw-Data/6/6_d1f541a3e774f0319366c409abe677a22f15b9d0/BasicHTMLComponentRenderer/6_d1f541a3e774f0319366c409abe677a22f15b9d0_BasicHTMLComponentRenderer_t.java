 /*
  * Created on 11 Mar 2007
  */
 package uk.org.ponder.rsf.renderer.html;
 
 import java.io.InputStream;
 import java.io.Reader;
 import java.util.Map;
 
 import uk.org.ponder.rsf.components.UIAnchor;
 import uk.org.ponder.rsf.components.UIBound;
 import uk.org.ponder.rsf.components.UIBoundBoolean;
 import uk.org.ponder.rsf.components.UIBoundList;
 import uk.org.ponder.rsf.components.UIBoundString;
 import uk.org.ponder.rsf.components.UICommand;
 import uk.org.ponder.rsf.components.UIComponent;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UILink;
 import uk.org.ponder.rsf.components.UIOutputMultiline;
 import uk.org.ponder.rsf.components.UIParameter;
 import uk.org.ponder.rsf.components.UISelect;
 import uk.org.ponder.rsf.components.UISelectChoice;
 import uk.org.ponder.rsf.components.UIVerbatim;
 import uk.org.ponder.rsf.renderer.ComponentRenderer;
 import uk.org.ponder.rsf.renderer.RenderUtil;
 import uk.org.ponder.rsf.renderer.TagRenderContext;
 import uk.org.ponder.rsf.renderer.scr.StaticRendererCollection;
 import uk.org.ponder.rsf.request.EarlyRequestParser;
 import uk.org.ponder.rsf.request.FossilizedConverter;
 import uk.org.ponder.rsf.template.XMLLump;
 import uk.org.ponder.rsf.uitype.UITypes;
 import uk.org.ponder.rsf.view.View;
 import uk.org.ponder.streamutil.StreamCopyUtil;
 import uk.org.ponder.streamutil.write.PrintOutputStream;
 import uk.org.ponder.stringutil.StringList;
 import uk.org.ponder.util.Constants;
 import uk.org.ponder.xml.XMLUtil;
 import uk.org.ponder.xml.XMLWriter;
 
 /** The core HTML-dialect specific renderer for a primitive RSF component */
 
 public class BasicHTMLComponentRenderer implements ComponentRenderer {
 
   private StaticRendererCollection scrc;
 
   public void setStaticRenderers(StaticRendererCollection scrc) {
     this.scrc = scrc;
   }
 
   private void dumpBoundFields(UIBound torender, XMLWriter xmlw) {
     if (torender != null) {
       if (torender.fossilizedbinding != null) {
         RenderUtil.dumpHiddenField(torender.fossilizedbinding, xmlw);
       }
       if (torender.fossilizedshaper != null) {
         RenderUtil.dumpHiddenField(torender.fossilizedshaper, xmlw);
       }
     }
   }
 
   public void renderComponent(UIComponent torendero, View view,
       TagRenderContext trc) {
     Map attrcopy = trc.attrcopy;
     XMLWriter xmlw = trc.xmlw;
     XMLLump[] lumps = trc.uselump.parent.lumps;
     int lumpindex = trc.uselump.lumpindex;
     PrintOutputStream pos = trc.pos;
 
     // Make an early and general attempt to rewrite side-URLs
     URLRewriteSCR urlrewriter = (URLRewriteSCR) scrc.getSCR(URLRewriteSCR.NAME);
     urlrewriter.rewriteURLs(trc.uselump, attrcopy);
     
     if (torendero instanceof UIBound) {
       UIBound torender = (UIBound) torendero;
 
       if (torendero.getClass() == UIOutputMultiline.class) {
         StringList value = ((UIOutputMultiline) torendero).getValue();
         if (value == null) {
           RenderUtil.dumpTillLump(lumps, lumpindex + 1,
               trc.close.lumpindex + 1, trc.pos);
         }
         else {
           XMLUtil.dumpAttributes(attrcopy, trc.xmlw);
           trc.pos.print(">");
           for (int i = 0; i < value.size(); ++i) {
             if (i != 0) {
               trc.pos.print("<br/>");
             }
             trc.xmlw.write(value.stringAt(i));
           }
           trc.closeTag();
         }
       }
       else if (torender.getClass() == UIAnchor.class) {
         String value = ((UIAnchor) torendero).getValue();
         if (UITypes.isPlaceholder(value)) {
           trc.renderUnchanged();
         }
         else {
           attrcopy.put("name", value);
           trc.replaceAttributes();
         }
       }
       // factor out component-invariant processing of UIBound.
       else { // non-Anchor, non-Multiline
         if (torender.willinput) {
           attrcopy.put("name", torender.submittingname);
         }
         if (torendero instanceof UIBoundBoolean) {
           if (((UIBoundBoolean) torender).getValue()) {
             attrcopy.put("checked", "yes");
           }
           else {
             attrcopy.remove("checked");
           }
           attrcopy.put("value", "true");
           trc.rewriteLeaf(null);
         }
         else if (torendero instanceof UIBoundList) {
           // Cannot be rendered directly, must be fake
           trc.renderUnchanged();
         }
         else {
           String value = ((UIBoundString) torender).getValue();
           if (trc.uselump.textEquals("<textarea ")) {
             if (UITypes.isPlaceholder(value) && torender.willinput) {
               // FORCE a blank value for input components if nothing from
               // model, if input was intended.
               value = "";
             }
             trc.rewriteLeaf(value);
           }
           else if (trc.uselump.textEquals("<input ")) {
             if (torender.willinput || !UITypes.isPlaceholder(value)) {
               attrcopy.put("value", value);
             }
             trc.rewriteLeaf(null);
           }
           else {
             trc.rewriteLeafOpen(value);
           }
         }
         // unify hidden field processing? ANY parameter children found must
         // be dumped as hidden fields.
       }
       // dump any fossilized binding for this component.
       dumpBoundFields(torender, xmlw);
     } // end if UIBound
 
     else if (torendero instanceof UISelect) {
       UISelect select = (UISelect) torendero;
       if (attrcopy.get("id") != null) {
         // TODO: This is an irregularity, should probably remove for 0.8
         attrcopy.put("id", select.selection.getFullID());
       }
       boolean ishtmlselect = trc.uselump.textEquals("<select ");
       if (select.selection instanceof UIBoundList && ishtmlselect) {
         attrcopy.put("multiple", "true");
       }
       if (ishtmlselect) {
         // The HTML submitted value from a <select> actually corresponds
         // with the selection member, not the top-level component.
         if (select.selection.willinput) {
           attrcopy.put("name", select.selection.submittingname);
         }
       }
       XMLUtil.dumpAttributes(attrcopy, xmlw);
       if (ishtmlselect) {
         pos.print(">");
         String[] values = select.optionlist.getValue();
         String[] names = select.optionnames == null ? values
             : select.optionnames.getValue();
         for (int i = 0; i < names.length; ++i) {
           pos.print("<option value=\"");
           String value = values[i];
           if (value == null)
             value = Constants.NULL_STRING;
           xmlw.write(value);
           if (select.selected.contains(value)) {
             pos.print("\" selected=\"selected");
           }
           pos.print("\">");
           xmlw.write(names[i]);
           pos.print("</option>\n");
         }
         trc.closeTag();
       }
       else {
         trc.dumpTemplateBody();
       }
 
       dumpBoundFields(select.selection, xmlw);
       dumpBoundFields(select.optionlist, xmlw);
       dumpBoundFields(select.optionnames, xmlw);
     }
     else if (torendero instanceof UISelectChoice) {
       UISelectChoice torender = (UISelectChoice) torendero;
       UISelect parent = (UISelect) view.getComponent(torender.parentFullID);
       String value = parent.optionlist.getValue()[torender.choiceindex];
       // currently only peers with "input type="radio"".
       attrcopy.put("name", parent.selection.submittingname);
       attrcopy.put("value", value);
       attrcopy.remove("checked");
       if (parent.selected.contains(value)) {
         attrcopy.put("checked", "true");
       }
       trc.replaceAttributes();
     }
     else if (torendero instanceof UILink) {
       UILink torender = (UILink) torendero;
 
       String attrname = URLRewriteSCR.getLinkAttribute(trc.uselump);
       if (attrname != null) {
         String target = torender.target.getValue();
         if (UITypes.isPlaceholder(target)) {
           target = (String) attrcopy.get(attrname);
         }
         else {
           String resolved = urlrewriter.resolveURL(trc.uselump.parent, target);
           if (resolved != null) {
             target = resolved;
           }
         }
         attrcopy.put(attrname, target);
       }
       String value = torender.linktext == null ? null
           : torender.linktext.getValue();
       if (value == null) {
         trc.replaceAttributesOpen();
       }
       else {
         trc.rewriteLeaf(value);
       }
     }
 
     else if (torendero instanceof UICommand) {
       UICommand torender = (UICommand) torendero;
       String value = RenderUtil.makeURLAttributes(torender.parameters);
       // any desired "attributes" decoded for JUST THIS ACTION must be
       // secretly bundled as this special attribute.
       attrcopy.put("name", FossilizedConverter.COMMAND_LINK_PARAMETERS + value);
       String text = torender.commandtext == null ? null
           : torender.commandtext.getValue();
       boolean isbutton = trc.uselump.textEquals("<button ");
       if (text != null && !isbutton) {
         attrcopy.put("value", torender.commandtext.getValue());
         text = null;
       }
       trc.rewriteLeaf(text);
     }
     else if (torendero instanceof UIForm) {
       UIForm torender = (UIForm) torendero;
       attrcopy.put("method", 
           torender.type.equals(EarlyRequestParser.RENDER_REQUEST)?
           "get" : "post");
       // form fixer guarantees that this URL is attribute free.
       attrcopy.put("action", torender.targetURL);
 
       trc.replaceAttributesOpen();
       for (int i = 0; i < torender.parameters.size(); ++i) {
         UIParameter param = torender.parameters.parameterAt(i);
         RenderUtil.dumpHiddenField(param, xmlw);
       }
     }
     else if (torendero instanceof UIVerbatim) {
       UIVerbatim torender = (UIVerbatim) torendero;
       String rendered = null;
       // inefficient implementation for now, upgrade when we write bulk POS
       // utils.
       if (torender.markup instanceof InputStream) {
         rendered = StreamCopyUtil.streamToString((InputStream) torender.markup);
       }
       else if (torender.markup instanceof Reader) {
         rendered = StreamCopyUtil.readerToString((Reader) torender.markup);
       }
       else if (torender.markup instanceof UIBoundString) {
         rendered = ((UIBoundString)(torender.markup)).getValue();
       }
       else if (torender.markup != null) {
         rendered = torender.markup.toString();
       }
       if (rendered == null) {
         trc.renderUnchanged();
       }
       else {
        if (!trc.iselide) {
          XMLUtil.dumpAttributes(attrcopy, xmlw);
          pos.print(">");
        }
         pos.print(rendered);
         trc.closeTag();
       }
     }
 
   }
 }
