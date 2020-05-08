 package net.cscott.sdr.calls;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import net.cscott.jutil.*;
 import net.cscott.sdr.calls.TaggedFormation.Tag;
 import net.cscott.sdr.util.Fraction;
 
 public abstract class Tagger {
     private Tagger() { /* no instances */ }
     
    // XXX if we find 4-dancer formations, we should look inside those for
    //     2-person formations (ie, not allow 2-person matches which span
    //     multiple 4-person formations, like the very centers in
    //     point-to-point diamonds.
     public static void addAutomatic(Formation f, MultiMap<Dancer, Tag> tags) {
         switch(f.dancers().size()) {
         case 8:
             // XXX: add center 2, outer 6
         case 4:
             // XXX: add centers & ends? only if we find waves
             //      exactly (center wave in a twin diamond does not
             //      have centers & ends unless it is selected exactly)
         case 2:
             tag2(f, tags);
         default: // skip
             break;
         }
     }
 
     /** Find all instance of the tagged {@code template} formation in the given
      * given formation {@code f}, and add the tags from the template to the
      * set of tags on {@code f} given in {@code tags}.
      */
     public static void addFrom(TaggedFormation template, Formation f, MultiMap<Dancer, Tag> tags) {
         assert false : "unimplemented";
     }
 
     // XXX: in some formations, doMatch will find multiple possible matches
     //      and complain about ambiguity.  How to solve?
     //   >v  <- all are trailing beaus
     //   ^<
     public static void tag2(Formation f, MultiMap<Dancer,Tag> tags) {
         // match against our beau/belle pattern.
         for (int i=0; i<2; i++) {
             try {
                 FormationMatch fm = GeneralFormationMatcher.doMatch
                 (f, (i==0) ? fBeauBelle : fLeaderTrailer, true, false);
                 // for each match, label as beau/belle/leader/trailer
                 for (TaggedFormation tf : fm.matches.values()) {
                     assert tf.dancers().size()==1 || tf.dancers().size()==2;
                     if (tf.dancers().size()!=2) continue; //an unmatched dancer
                     for (Dancer d : tf.dancers()) {
                         Position p = tf.location(d);
                         assert p.y.equals(Fraction.ZERO);
                         assert p.x.abs().equals(Fraction.ONE);
                         boolean left = (p.x.compareTo(Fraction.ZERO)<0);
                         if (p.facing.equals(ExactRotation.NORTH))
                             tags.add(d, left ? Tag.BEAU : Tag.BELLE);
                         else if (p.facing.equals(ExactRotation.SOUTH))
                             tags.add(d, left ? Tag.BELLE : Tag.BEAU);
                         else if (p.facing.equals(ExactRotation.EAST))
                             tags.add(d, left ? Tag.TRAILER : Tag.LEADER);
                         else if (p.facing.equals(ExactRotation.WEST))
                             tags.add(d, left ? Tag.LEADER : Tag.TRAILER);
                     }
                 }
             } catch (NoMatchException nme) {
                 System.err.println(f.toStringDiagram()+" "+nme);
                 // no leaders/trailers/beaus/belles to be found here.
                 continue;
             }
         }
         // ok, finally: there are two "pinwheel" formations that will cause
         // doMatch to report an ambiguous match, yet these do actually have
         // beaus/belles/leaders/trailers.  Try these.
        // XXX DO ME.
     }
     /** Our private stash of phantom dancers. */
     private static final PhantomDancer[] dancers = new PhantomDancer[] {
         new PhantomDancer(), new PhantomDancer(),
     };
     /** A method to build formations with these dancers. */
     private static TaggedFormation buildFormation(Position... positions) {
         Map<Dancer,Position> m =
             new LinkedHashMap<Dancer,Position>(positions.length);
         for (int i=0; i<positions.length; i++)
             m.put(dancers[i], positions[i]);
         return new TaggedFormation(new Formation(m),
                 Default.<Dancer,Tag>EMPTY_MULTIMAP());
     }
     /** Helper method to make Positions more concise. */
     private static Position p(int x, int y, String dir) {
         return new Position(x,y,Rotation.fromAbsoluteString(dir));
     }
     // formations that will be useful to us.
     private static final TaggedFormation fBeauBelle =
         buildFormation(p(-1,0,"|"), p(+1,0,"o"));
     private static final TaggedFormation fLeaderTrailer =
         buildFormation(p(-1,0,"-"), p(+1,0,"o"));
 }
