 /*
  * Created on 18 Sep 2006
  */
 package uk.org.ponder.rsf.renderer.html;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import uk.org.ponder.rsf.renderer.ComponentRenderer;
 import uk.org.ponder.rsf.renderer.RenderUtil;
 import uk.org.ponder.rsf.renderer.TagRenderContext;
 import uk.org.ponder.rsf.renderer.scr.CollectingSCR;
 import uk.org.ponder.rsf.template.XMLLump;
 import uk.org.ponder.rsf.template.XMLLumpList;
 
 /**
  * A basic collector of &lt;head&gt; material for HTML pages. Will emit all
  * collected &lt;style&gt; and &lt;script&gt; tags, and leave the tag in an open
  * condition.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 
 public class HeadCollectingSCR implements CollectingSCR {
   public static final String NAME = "head-collect";
   private URLRewriteSCR urlRewriteSCR;
 
   public String getName() {
     return NAME;
   }
 
   public String[] getCollectingNames() {
     return new String[] { "style", "script" };
   }
 
   public void setURLRewriteSCR(URLRewriteSCR urlRewriteSCR) {
     this.urlRewriteSCR = urlRewriteSCR;
   }
 
   public int render(XMLLumpList collected, TagRenderContext trc) {
     if (!trc.iselide) {
       RenderUtil.dumpTillLump(trc.uselump.parent.lumps, trc.uselump.lumpindex,
         trc.uselump.open_end.lumpindex + 1, trc.pos);
     }
     Set used = new HashSet();
     for (int i = 0; i < collected.size(); ++i) {
       XMLLump collump = collected.lumpAt(i);
       String attr = URLRewriteSCR.getLinkAttribute(collump);
       if (attr != null) {
         String attrval = (String) collump.attributemap.get(attr);
         if (attrval != null) {
           String rewritten = urlRewriteSCR.resolveURL(collump.parent, attrval);
           if (rewritten == null) rewritten = attrval;
           int qpos = rewritten.indexOf('?');
           if (qpos != -1)
             rewritten = rewritten.substring(0, qpos);
           if (used.contains(rewritten))
             continue;
           else
             used.add(rewritten);
         }
       }
      TagRenderContext temptrc = 
        new TagRenderContext(trc.attrcopy, collump, collump.open_end, collump.close_tag, 
             trc.pos, trc.xmlw, collump.lumpindex, false);
       urlRewriteSCR.render(temptrc);
       RenderUtil.dumpTillLump(collump.parent.lumps,
           collump.open_end.lumpindex + 1, collump.close_tag.lumpindex + 1, trc.pos);
       trc.pos.println();
     }
     if (trc.iselide) {
       trc.dumpTemplateBody();
     }
     return trc.iselide? ComponentRenderer.LEAF_TAG : ComponentRenderer.NESTING_TAG;
   }
 
 }
