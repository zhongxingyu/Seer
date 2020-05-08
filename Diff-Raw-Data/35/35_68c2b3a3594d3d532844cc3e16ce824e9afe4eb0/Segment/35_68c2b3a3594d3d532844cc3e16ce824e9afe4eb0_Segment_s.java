 /*
  * Quill and Parchment, a WYSIWYN document editor and rendering engine. 
  *
  * Copyright © 2009-2010 Operational Dynamics Consulting, Pty Ltd
  *
  * The code in this file, and the program it is a part of, is made available
  * to you by its authors as open source software: you can redistribute it
  * and/or modify it under the terms of the GNU General Public License version
  * 2 ("GPL") as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GPL for more details.
  *
  * You should have received a copy of the GPL along with this program. If not,
  * see http://www.gnu.org/licenses/. The authors of this program may be
  * contacted through http://research.operationaldynamics.com/projects/quill/.
  */
 package quill.textbase;
 
 /**
  * Segments are the block level grouping mechanism in textbase. Concrete
  * instances of this represent the entities we model as UI blocks each with
  * their own customized EditorWidget.
  * 
  * <p>
  * Note that some of these logical structures may correspond to more than one
  * underlying XML block level element; see {@link NormalSegment}.
  * 
  * <p>
  * Segment itself does not provide a chaining or sequencing mechanism; impose
  * that locally with a Segment[] (or a more complicated data structure as
  * necessary) locally, or if passing it further, wrapping it in a Series.
  * 
  * @author Andrew Cowie
  */
 public abstract class Segment
 {
     protected Segment(Extract entire) {
         this.entire = entire;
         this.extra = "";
         this.offset = 0;
         this.removed = 0;
         this.inserted = entire.getWidth();
     }
 
     protected Segment(Extract entire, int offset, int removed, int inserted) {
         this.entire = entire;
         this.extra = "";
         this.offset = offset;
         this.removed = removed;
         this.inserted = inserted;
     }
 
     protected Segment(Extract entire, String extra, int offset, int removed, int inserted) {
         this.entire = entire;
         this.extra = extra;
         this.offset = offset;
         this.removed = removed;
         this.inserted = inserted;
     }
 
     /**
      * The internal representation of the text body of this Segment.
      */
     private final Extract entire;
 
     public Extract getEntire() {
         return entire;
     }
 
     /*
      * This is a legacy of our having removed Change and replaced it with a
      * State based system; undo and redo became a nightmare. So, we cache
      * "where" a change happened that took us to this state, with a view to
      * being able to use this in EditorTextView's affect(Segment).
      */
 
     /**
      * At what offset into this Segment the last change was made.
      */
     private final int offset;
 
     /**
      * How wide, from offset, that the last change was.
      */
     private final int inserted;
 
     /**
      * How many characters, from offset, that were removed in the last change.
      */
     private final int removed;
 
     public int getOffset() {
         return offset;
     }
 
     public int getInserted() {
         return inserted;
     }
 
     public int getRemoved() {
         return removed;
     }
 
     public abstract Segment createSimilar(Extract entire, int offset, int removed, int inserted);
 
     public abstract Segment createSimilar(String extra);
 
     /**
      * A single item of metadata, originally the filename for an ImageSegment.
      */
     private final String extra;
 
     /**
      * Get the additional data (ie, the value of the Block's Attribute). This
      * is extra's src, endnot's name, heading's label, etc.
      */
     /*
      * FUTURE This will need to grow if we ever have more than one attribute
      * per block [ie hyperlinks will cause us to need getLink() as well], but
      * right now this covers the general metadata case.
      */
     public String getExtra() {
         return extra;
     }
 
     /**
      * Get a single String with the contents of the Span tree of text in this
      * Segment.
      */
     /*
      * This isn't exactly effecient given large amounts of text. Anything
      * doing something heavy with this result should iterate over the Spans
      * itself.
      */
     public String getText() {
         final StringBuilder str;
 
         str = new StringBuilder();
 
         entire.visit(new CharacterVisitor() {
             public boolean visit(int character, Markup markup) {
                 str.appendCodePoint(character);
                 return false;
             }
         });
 
         return str.toString();
     }
 
     /*
      * For debugging
      */
     public String toString() {
        return getClass().getSimpleName() + " \"" + getText() + "\"";
     }
 }
