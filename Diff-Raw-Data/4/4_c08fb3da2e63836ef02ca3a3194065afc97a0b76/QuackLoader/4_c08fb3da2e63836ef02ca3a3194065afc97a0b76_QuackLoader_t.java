 /*
  * Quill and Parchment, a WYSIWYN document editor and rendering engine. 
  *
  * Copyright © 2008-2010 Operational Dynamics Consulting, Pty Ltd
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
 package quill.quack;
 
 import java.util.ArrayList;
 
 import nu.xom.Document;
 import quill.textbase.AttributionSegment;
 import quill.textbase.ChapterSegment;
 import quill.textbase.Common;
 import quill.textbase.ComponentSegment;
 import quill.textbase.DivisionSegment;
 import quill.textbase.EndnoteSegment;
 import quill.textbase.Extract;
 import quill.textbase.HeadingSegment;
 import quill.textbase.ImageSegment;
 import quill.textbase.LeaderSegment;
 import quill.textbase.ListitemSegment;
 import quill.textbase.Markup;
 import quill.textbase.NormalSegment;
 import quill.textbase.PoeticSegment;
 import quill.textbase.PreformatSegment;
 import quill.textbase.QuoteSegment;
 import quill.textbase.ReferenceSegment;
 import quill.textbase.Segment;
 import quill.textbase.Series;
 import quill.textbase.Span;
 import quill.textbase.Special;
 import quill.textbase.SpecialSegment;
 import quill.textbase.TextChain;
 
 import static quill.textbase.Span.createSpan;
 
 /**
  * Take a XOM tree (built using QuackNodeFactory and so having our
  * QuackElements) and convert it into our internal in-memory textchain
  * representation.
  * 
  * @author Andrew Cowie
  */
 public class QuackLoader
 {
     private final ArrayList<Segment> list;
 
     /**
      * The (single) formatting applicable at this insertion point.
      */
     private Markup markup;
 
     /**
      * Did we just enter a block level element?
      */
     private boolean start;
 
     /**
      * The current block level element wrapper
      */
     private Segment segment;
 
     /**
      * The current text body we are building up.
      */
     private TextChain chain;
 
     /**
      * The current metadata we have captured
      */
     /*
      * This is horrid, only supporting one attribute.
      */
     private String attribute;
 
     /**
      * Are we in a block where line endings and other whitespace is preserved?
      */
     private boolean preserve;
 
     /**
      * Did we trim a whitespace character (newline, specifically) from the end
      * of the previous Text?
      */
     private boolean space;
 
     /**
      * If a whitespace was swollowed, then append this Span.
      */
     private Span pending;
 
     public QuackLoader() {
         list = new ArrayList<Segment>(5);
         chain = null;
     }
 
     /*
      * FIXME This assumes our documents have only one chapter in them. That's
      * not correct!
      */
     /*
      * This kinda assumes we only load one Document; if that's not the case,
      * and we don't force instantiating a new QuackLoader, then clear the
      * processor state here.
      */
     public Series process(Document doc) {
         final Root quack;
         int j;
         Block[] blocks;
 
         quack = (Root) doc.getRootElement();
         processComponent(quack);
 
         /*
          * Now iterate over the Blocks.
          */
 
         blocks = quack.getBlocks();
 
         for (j = 0; j < blocks.length; j++) {
             processBlock(blocks[j]);
         }
 
         /*
          * Finally, transpose the resultant collection of Segments into a
          * Series, and return it.
          */
 
         ensureChapterHasTitle();
 
         return new Series(list);
     }
 
     private void processComponent(Root quack) {
         if (quack instanceof RootElement) {
             start = true;
             preserve = false;
         } else {
             throw new UnsupportedOperationException();
         }
     }
 
     /**
      * Ensure that the first Segment in the Series is a ComponentSegment.
      */
     /*
      * This is a bit of a hack, but this means that at run time inside the app
      * we always have at least one Segment. And, just to make the user
      * experience a touch better, if there wasn't a second Segment, then add a
      * Normal one so that there is somewhere to type.
      */
     private void ensureChapterHasTitle() {
         final int num;
         Segment first, second;
         final Extract blank;
 
         num = list.size();
 
         if (num == 0) {
             blank = Extract.create();
             first = new ChapterSegment(blank);
             list.add(first);
             second = new NormalSegment(blank);
             list.add(second);
             return;
         }
 
         first = list.get(0);
 
         if (first instanceof ComponentSegment) {
             if (num > 1) {
                 /*
                  * This is the usual case; there's a title and some body.
                  * Excellent. We're done.
                  */
                 return;
             }
 
             blank = Extract.create();
             second = new NormalSegment(blank);
             list.add(second);
         } else {
             blank = Extract.create();
             first = new ChapterSegment(blank);
             list.add(0, first);
         }
     }
 
     /*
      * It's a bit ugly to be adding the Segment only to immediately remove it
      * again in the common case of a continuing run of normal text.
      */
     private void processBlock(Block block) {
         Extract entire;
         int i;
 
         start = true;
         chain = new TextChain();
         attribute = null;
 
         if (block instanceof TextElement) {
             preserve = false;
             if (segment instanceof NormalSegment) {
                 entire = segment.getEntire();
                 chain.setTree(entire);
                 chain.append(Span.createSpan('\n', null));
 
                 i = list.size() - 1;
                 list.remove(i);
             }
         } else if (block instanceof CodeElement) {
             preserve = true;
         } else if (block instanceof QuoteElement) {
             preserve = false;
             if (segment instanceof QuoteSegment) {
                 entire = segment.getEntire();
                 chain.setTree(entire);
                 chain.append(Span.createSpan('\n', null));
 
                 i = list.size() - 1;
                 list.remove(i);
             }
         } else if (block instanceof PoemElement) {
             preserve = true;
         } else if (block instanceof ListElement) {
             preserve = false;
             processData(block);
             if ((attribute == null) && (segment instanceof ListitemSegment)) {
                 entire = segment.getEntire();
                 chain.setTree(entire);
                 chain.append(Span.createSpan('\n', null));
 
                 i = list.size() - 1;
                 list.remove(i);
 
                 attribute = segment.getImage();
             }
         } else if (block instanceof CreditElement) {
             preserve = false;
             if (segment instanceof AttributionSegment) {
                 entire = segment.getEntire();
                 chain.setTree(entire);
                 chain.append(Span.createSpan('\n', null));
 
                 i = list.size() - 1;
                 list.remove(i);
             }
         } else if (block instanceof HeadingElement) {
             preserve = false;
             processData(block);
         } else if (block instanceof LeaderElement) {
             preserve = false;
         } else if (block instanceof ImageElement) {
             preserve = false;
             processData(block);
         } else if ((block instanceof ChapterElement) || (block instanceof DivisionElement)) {
             preserve = false;
             if (segment != null) {
                throw new IllegalStateException("\n" + "A " + block.toString()
                        + " must be the first block in a Quack file.");
             }
             processData(block);
         } else if (block instanceof EndnoteElement) {
             preserve = false;
             processData(block);
         } else if (block instanceof ReferenceElement) {
             preserve = false;
             processData(block);
         } else if (block instanceof SpecialElement) {
             preserve = false;
             processData(block);
         } else {
             throw new IllegalStateException("\n" + "What kind of Block is " + block);
         }
 
         /*
          * Now iterate over the text and Inlines of the Block, accumulating
          * into the TextChain.
          */
 
         processBody(block);
 
         /*
          * With that done, form the [immutable] Segment based on the
          * accumulated data.
          */
 
         entire = chain.extractAll();
 
         if (block instanceof TextElement) {
             segment = new NormalSegment(entire);
         } else if (block instanceof CodeElement) {
             segment = new PreformatSegment(entire);
         } else if (block instanceof QuoteElement) {
             segment = new QuoteSegment(entire);
         } else if (block instanceof PoemElement) {
             segment = new PoeticSegment(entire);
         } else if (block instanceof ListElement) {
             segment = new ListitemSegment(entire, attribute);
         } else if (block instanceof CreditElement) {
             segment = new AttributionSegment(entire);
         } else if (block instanceof HeadingElement) {
             segment = new HeadingSegment(entire, attribute);
         } else if (block instanceof ImageElement) {
             segment = new ImageSegment(entire, attribute);
         } else if (block instanceof ChapterElement) {
             segment = new ChapterSegment(entire, attribute);
         } else if (block instanceof DivisionElement) {
             segment = new DivisionSegment(entire, attribute);
         } else if (block instanceof EndnoteElement) {
             segment = new EndnoteSegment(entire, attribute);
         } else if (block instanceof ReferenceElement) {
             segment = new ReferenceSegment(entire, attribute);
         } else if (block instanceof LeaderElement) {
             segment = new LeaderSegment(entire);
         } else if (block instanceof SpecialElement) {
             segment = new SpecialSegment(attribute);
         } else {
             throw new IllegalStateException("\n" + "What kind of Block is " + block);
         }
 
         list.add(segment);
     }
 
     private void processData(final Block block) {
         final Meta[] data;
         int i;
 
         data = block.getData();
         for (i = 0; i < data.length; i++) {
             processMetadata(data[i]);
         }
     }
 
     private void processBody(final Block block) {
         Inline[] elements;
         int i;
 
         elements = block.getBody();
         for (i = 0; i < elements.length; i++) {
             processInline(elements[i]);
         }
     }
 
     private void processMetadata(final Meta meta) {
         final String str;
 
         if (meta instanceof SourceAttribute) {
             str = meta.getValue();
             attribute = str;
         } else if (meta instanceof NameAttribute) {
             str = meta.getValue();
             attribute = str;
         } else if (meta instanceof LabelAttribute) {
             str = meta.getValue();
             attribute = str;
         } else if (meta instanceof TypeAttribute) {
             str = meta.getValue();
             attribute = str;
         } else {
             throw new IllegalStateException("Unknown Meta type");
         }
     }
 
     private void processInline(Inline span) {
         final String str;
 
         str = span.getText();
 
         if (span instanceof Normal) {
             markup = null;
             processText(str);
         } else if (span instanceof InlineElement) {
             if (span instanceof FunctionElement) {
                 markup = Common.FUNCTION;
             } else if (span instanceof FilenameElement) {
                 markup = Common.FILENAME;
             } else if (span instanceof TypeElement) {
                 markup = Common.TYPE;
             } else if (span instanceof LiteralElement) {
                 markup = Common.LITERAL;
             } else if (span instanceof CommandElement) {
                 markup = Common.COMMAND;
             } else if (span instanceof HighlightElement) {
                 markup = Common.HIGHLIGHT;
             } else if (span instanceof TitleElement) {
                 markup = Common.TITLE;
             } else if (span instanceof KeyboardElement) {
                 markup = Common.KEYBOARD;
             } else if (span instanceof AcronymElement) {
                 markup = Common.ACRONYM;
             } else if (span instanceof ProjectElement) {
                 markup = Common.PROJECT;
             } else if (span instanceof ItalicsElement) {
                 markup = Common.ITALICS;
             } else if (span instanceof BoldElement) {
                 markup = Common.BOLD;
             } else {
                 throw new IllegalStateException("Unknown Inline type");
             }
 
             start = false;
             processText(str);
         } else if (span instanceof MarkerElement) {
             if (span instanceof NoteElement) {
                 markup = Special.NOTE;
             } else if (span instanceof CiteElement) {
                 markup = Special.CITE;
             }
             processMarker(str);
         }
     }
 
     /*
      * This will be the contiguous text body of the element until either a) an
      * nested (inline) element starts, or b) the end of the element is
      * reached. So we trim off the leading pretty-print whitespace then add a
      * single StringSpan with this content.
      */
     private void processText(String text) {
         final String trim, str;
         int len;
         char ch;
 
         len = text.length();
 
         /*
          * This case, and the first in the next block, are common for
          * structure tags
          */
 
         if (len == 0) {
             space = false;
             return; // empty
         }
 
         /*
          * Check for valid file format: no bare newlines. This is probably
          * expensive, but we have tests that expect this.
          */
 
         if ((!preserve) && (len > 1)) {
             if (text.contains("\n\n")) {
                 throw new IllegalStateException("Can't have bare newlines in normal Blocks");
             }
         }
 
         /*
          * Do we think we're starting a block? If so, trim off the leading
          * newline. If we're starting an inline and we swollowed a trailing
          * space from the previous run of text, pad the inline with one space.
          */
 
         if (start) {
             start = false;
             space = false;
 
             ch = text.charAt(0);
             if (ch == '\n') {
                 if (len == 1) {
                     return; // ignore it
                 }
                 text = text.substring(1);
                 len--;
             }
         } else if (space) {
             chain.append(pending);
             pending = null;
         }
 
         /*
          * If not preformatted text, turn any interior newlines into spaces,
          * then add.
          */
 
         if (preserve) {
             str = text;
         } else {
             str = text.replace('\n', ' ');
         }
 
         /*
          * Trim the trailing newline (if there is one) as it could be the
          * break before a close-element tag. We replace it with a space and
          * prepend it if we find it is just a linebreak separator between a
          * Text and an Inline when making the next Text node.
          */
 
         ch = text.charAt(len - 1);
         if (ch == '\n') {
             trim = str.substring(0, len - 1);
             space = true;
             pending = Span.createSpan(' ', markup);
 
             if (len == 1) {
                 return; // captured in pending
             }
             len--;
         } else {
             trim = str;
             space = false;
         }
 
         chain.append(createSpan(trim, markup));
     }
 
     private void processMarker(String str) {
         chain.append(Span.createMarker(str, markup));
     }
 }
