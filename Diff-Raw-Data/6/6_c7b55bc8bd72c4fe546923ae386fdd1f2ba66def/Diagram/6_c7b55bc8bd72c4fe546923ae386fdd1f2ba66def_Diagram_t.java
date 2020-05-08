 package annotationsketch;
 
 import java.util.AbstractList;
 
 import annotationsketch.Diagram.GT.TRACKSELECTOR;
 
 import com.sun.jna.Library;
 import com.sun.jna.Native;
 import com.sun.jna.Pointer;
 import com.sun.jna.Callback;
 
 import core.Array;
 import core.GTerror;
 import core.Range;
 import core.Str;
 import extended.FeatureNode;
 
 public class Diagram
 {
   protected Pointer diagram_ptr;
   // these references are kept to avoid garbage collection 
   // of the track selector funcs as long as this object exists
   @SuppressWarnings("unused")
 private TRACKSELECTOR tsf;
   @SuppressWarnings("unused")
   private TrackSelector ts;
 
   public interface GT extends Library
   {
     GT INSTANCE = (GT) Native.loadLibrary("genometools", GT.class);
 
    Pointer gt_diagram_new(Pointer feat_index, String seqid, Range gt_range,
         Pointer gt_style, Pointer gt_err);
     Pointer gt_diagram_new_from_array(Pointer gt_array, Range gt_range,
         Pointer gt_style);
     void gt_diagram_set_track_selector_func(Pointer gt_diagram,
         TRACKSELECTOR func);
     void gt_diagram_reset_track_selector_func(Pointer diagram_ptr);
     void gt_diagram_delete(Pointer gt_diagram);
 
     interface TRACKSELECTOR extends Callback
     {
       void callback(Pointer block_ptr, Pointer str_ptr, Pointer data_ptr) throws GTerror;
     }
   }
 
   public Diagram(AbstractList<FeatureNode> feats, Range rng, Style sty) throws GTerror
   {
     Pointer dia;
     if (rng.get_start() > rng.get_end()) {
       throw new GTerror("range.start > range.end");
     }
     Array gtarr = new Array(Pointer.SIZE);
     for (int i = 0; i < feats.size(); i++) {
       gtarr.add(feats.get(i).to_ptr());
     }
     synchronized (this) {
       dia = GT.INSTANCE.gt_diagram_new_from_array(gtarr.to_ptr(), rng,
           sty.to_ptr());
     }
     if (dia == null) {
       throw new GTerror("diagram pointer was NULL");
     } else {
       this.diagram_ptr = dia;
     }
   }
 
   public Diagram(FeatureIndex feat_index, String seqid, Range ran, Style style) throws GTerror
   {
     Pointer dia;
     if (ran.start.longValue() > ran.end.longValue()) {
       throw new GTerror("range.start > range.end");
     }
     if (style.equals(Style.class)) {
       throw new GTerror("style parameter has to be a style Object");
     }
     GTerror err = new GTerror();
     synchronized (this) {
      dia = GT.INSTANCE.gt_diagram_new(feat_index.to_ptr(), seqid, ran, style.to_ptr(), err.to_ptr());
     }
     if (dia == null) {
       throw new GTerror(err.get_err(),err.to_ptr());
     } else {
       this.diagram_ptr = dia;
     }
   }
 
   public void set_track_selector_func(final TrackSelector ts)
   {
     TRACKSELECTOR tsf = new TRACKSELECTOR()
     {
       public void callback(Pointer block_ptr, Pointer str_ptr, Pointer data_ptr) throws GTerror
       {
         Block b = new Block(block_ptr);
         String s = ts.getTrackId(b);
         Str str = new Str(str_ptr);
         if (s instanceof String) {
           str.append_str(s);
         } else {
           throw new GTerror("Track selector function must return a string");
         }
       }
     };
     this.tsf = tsf;
     this.ts = ts;
     GT.INSTANCE.gt_diagram_set_track_selector_func(diagram_ptr, tsf);
   }
   
   public void reset_track_selector_func()
   {
     GT.INSTANCE.gt_diagram_reset_track_selector_func(diagram_ptr);
   }
   
   protected synchronized void finalize() throws Throwable {
     try {
       GT.INSTANCE.gt_diagram_delete(diagram_ptr);
     } finally {
 	    super.finalize();
     }
   }
   
   public Pointer to_ptr()
   {
     return diagram_ptr;
   }
 }
