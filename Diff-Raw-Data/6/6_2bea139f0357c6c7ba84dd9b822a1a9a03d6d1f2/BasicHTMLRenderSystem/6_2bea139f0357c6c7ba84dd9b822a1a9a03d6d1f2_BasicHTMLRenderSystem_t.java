 /*
  * Created on Jul 27, 2005
  */
 package uk.org.ponder.rsf.renderer.html;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import uk.org.ponder.rsf.components.UIBasicListMember;
 import uk.org.ponder.rsf.components.UIComponent;
 import uk.org.ponder.rsf.renderer.ComponentRenderer;
 import uk.org.ponder.rsf.renderer.RenderSystem;
 import uk.org.ponder.rsf.renderer.RenderSystemContext;
 import uk.org.ponder.rsf.renderer.RenderUtil;
 import uk.org.ponder.rsf.renderer.TagRenderContext;
 import uk.org.ponder.rsf.renderer.decorator.DecoratorManager;
 import uk.org.ponder.rsf.renderer.scr.NullRewriteSCR;
 import uk.org.ponder.rsf.renderer.scr.StaticComponentRenderer;
 import uk.org.ponder.rsf.renderer.scr.StaticRendererCollection;
 import uk.org.ponder.rsf.request.FossilizedConverter;
 import uk.org.ponder.rsf.request.SubmittedValueEntry;
 import uk.org.ponder.rsf.template.XMLLump;
 import uk.org.ponder.rsf.template.XMLLumpList;
 import uk.org.ponder.util.Constants;
 import uk.org.ponder.util.Logger;
 import uk.org.ponder.util.UniversalRuntimeException;
 
 /**
  * The implementation of the standard XHTML rendering System. This class is due
  * for basic refactoring since it contains logic that belongs in a) a "base
  * System-independent" lookup bean, and b) in a number of individual
  * ComponentRenderer objects.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 
 public class BasicHTMLRenderSystem implements RenderSystem {
   private StaticRendererCollection scrc;
   private DecoratorManager decoratormanager;
   private ComponentRenderer componentRenderer;
 
   public void setComponentRenderer(ComponentRenderer componentRenderer) {
     this.componentRenderer = componentRenderer;
   }
 
   public void setStaticRenderers(StaticRendererCollection scrc) {
     this.scrc = scrc;
   }
 
   public void setDecoratorManager(DecoratorManager decoratormanager) {
     this.decoratormanager = decoratormanager;
   }
 
   // two methods for the RenderSystemDecoder interface
   public void normalizeRequestMap(Map requestparams) {
     String key = RenderUtil.findCommandParams(requestparams);
     if (key != null) {
       String params = key.substring(FossilizedConverter.COMMAND_LINK_PARAMETERS
           .length());
       RenderUtil.unpackCommandLink(params, requestparams);
       requestparams.remove(key);
     }
   }
 
   public void fixupUIType(SubmittedValueEntry sve) {
     if (sve.oldvalue instanceof Boolean) {
       if (sve.newvalue == null)
         sve.newvalue = Boolean.FALSE;
     }
     else if (sve.oldvalue instanceof String[]) {
       if (sve.newvalue == null)
         sve.newvalue = new String[] {};
     }
     else if (sve.oldvalue instanceof String) {
       if (sve.newvalue instanceof String
           && Constants.NULL_STRING.equals(sve.newvalue)
           || sve.newvalue instanceof String[]
           && Constants.NULL_STRING.equals(((String[]) sve.newvalue)[0])) {
         sve.newvalue = null;
       }
     }
   }
 
 
   public void renderDebugMessage(RenderSystemContext rsc, String string) {
     rsc.pos.print("<span style=\"background-color:#FF466B;color:white;padding:1px;\">");
     rsc.xmlw.write(string);
     rsc.pos.print("</span><br/>");
   }
   
   // This method is almost entirely dialect-invariant - awaiting final
   // factorisation of RenderSystem
   public int renderComponent(RenderSystemContext rsc, UIComponent torendero, XMLLump lump) {
     int lumpindex = lump.lumpindex;
     XMLLump[] lumps = lump.parent.lumps;
     int nextpos = -1;
     XMLLump outerendopen = lump.open_end;
     XMLLump outerclose = lump.close_tag;
 
     nextpos = outerclose.lumpindex + 1;
 
     XMLLumpList payloadlist = lump.downmap == null ? null
         : lump.downmap.headsForID(XMLLump.PAYLOAD_COMPONENT);
     XMLLump payload = payloadlist == null ? null
         : payloadlist.lumpAt(0);
 
     // if there is no peer component, it might still be a static resource holder
     // that needs URLs rewriting.
     // we assume there is no payload component here, since there is no producer
     // ID that might govern selection. So we use "outer" indices.
     if (torendero == null) {
       if (lump.rsfID.startsWith(XMLLump.SCR_PREFIX)) {
         String scrname = lump.rsfID.substring(XMLLump.SCR_PREFIX.length());
         StaticComponentRenderer scr = scrc.getSCR(scrname);
         if (scr == null) {
           Logger.log
               .info("Warning: unrecognised static component renderer reference with key "
                   + scrname + " at lump " + lump.toString());
           scr = NullRewriteSCR.instance;
         }
         int tagtype = RenderUtil.renderSCR(scr, lump, rsc.xmlw, rsc.collecteds);
         nextpos = tagtype == ComponentRenderer.LEAF_TAG ? outerclose.lumpindex + 1
             : outerendopen.lumpindex + 1;
       }
       else {
         if (rsc.debugrender) {
           renderDebugMessage(rsc, "Leaf component missing which was expected with template id " + 
               lump.rsfID + " at " + lump.toString());
         }
       }
     }
     else {
       // else there IS a component and we are going to render it. First make
       // sure we render any preamble.
       XMLLump endopen = outerendopen;
       XMLLump close = outerclose;
       XMLLump uselump = lump;
       if (payload != null) {
         endopen = payload.open_end;
         close = payload.close_tag;
         uselump = payload;
         RenderUtil.dumpTillLump(lumps, lumpindex, payload.lumpindex, rsc.pos);
         lumpindex = payload.lumpindex;
       }
 
       HashMap attrcopy = new HashMap();
       attrcopy.putAll(uselump.attributemap);
       rsc.IDassigner.adjustForID(attrcopy, torendero);
       decoratormanager.decorate(torendero.decorators, uselump.getTag(),
           attrcopy);
 
       TagRenderContext rendercontext = new TagRenderContext(attrcopy, uselump,
           endopen, close, rsc.pos, rsc.xmlw, nextpos);
       // ALWAYS dump the tag name, this can never be rewritten. (probably?!)
       rsc.pos.write(uselump.parent.buffer, uselump.start, uselump.length);
 
       if (torendero instanceof UIBasicListMember) {
         torendero = RenderUtil.resolveListMember(rsc.view,
             (UIBasicListMember) torendero);
       }
       try {
         componentRenderer.renderComponent(torendero, rsc.view, rendercontext);
       }
       catch (Exception e) {
         throw UniversalRuntimeException.accumulate(e,
             "Error rendering component " + torendero.getClass()
                 + " with full ID " + torendero.getFullID()
                 + " at template location " + rendercontext.uselump);
       }
       // if there is a payload, dump the postamble.
       if (payload != null) {
        // the default case is initialised to tag close
        if (rendercontext.nextpos == nextpos) {
          RenderUtil.dumpTillLump(lumps, close.lumpindex + 1,
             outerclose.lumpindex + 1, rsc.pos);
        }
       }
       nextpos = rendercontext.nextpos;
     }
 
     return nextpos;
   }
 
 
 }
