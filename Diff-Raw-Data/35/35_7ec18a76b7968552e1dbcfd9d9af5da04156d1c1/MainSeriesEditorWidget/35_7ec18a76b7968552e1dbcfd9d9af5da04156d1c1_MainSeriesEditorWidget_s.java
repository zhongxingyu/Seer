 /*
  * Quill and Parchment, a WYSIWYN document editor and rendering engine. 
  *
  * Copyright © 2010-2011 Operational Dynamics Consulting, Pty Ltd
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
 package quill.ui;
 
 import java.util.List;
 
 import org.freedesktop.cairo.Context;
 import org.freedesktop.cairo.Surface;
 import org.gnome.gdk.Color;
 import org.gnome.gdk.EventExpose;
 import org.gnome.gdk.EventFocus;
 import org.gnome.gtk.PolicyType;
 import org.gnome.gtk.Scrollbar;
 import org.gnome.gtk.ScrolledWindow;
 import org.gnome.gtk.StateType;
 import org.gnome.gtk.Viewport;
 import org.gnome.gtk.Widget;
 
 import quill.textbase.AttributionSegment;
 import quill.textbase.ChapterSegment;
 import quill.textbase.Component;
 import quill.textbase.DivisionSegment;
 import quill.textbase.EndnoteSegment;
 import quill.textbase.HeadingSegment;
 import quill.textbase.ImageSegment;
 import quill.textbase.LeaderSegment;
 import quill.textbase.ListitemSegment;
 import quill.textbase.NormalSegment;
 import quill.textbase.PoeticSegment;
 import quill.textbase.PreformatSegment;
 import quill.textbase.QuoteSegment;
 import quill.textbase.ReferenceSegment;
 import quill.textbase.Segment;
 import quill.textbase.Series;
 import quill.textbase.SpecialSegment;
 
 /**
  * Left hand side of a PrimaryWindow for editing the main body of a chapter.
  * 
  * @author Andrew Cowie
  */
 class MainSeriesEditorWidget extends SeriesEditorWidget
 {
     private Component component;
 
     MainSeriesEditorWidget(PrimaryWindow primary) {
         super(primary);
         final Viewport port;
 
         /*
          * Set the background color of the entire EditorWidget to white in
          * order to hide the upper side of the horizontal Scrollbars in the
          * preformatted blocks. Finding that this was the right place was
          * traumatic, but it turns out that the Viewport has the underlying
          * [org.gnome.gdk] Window where the drawing happens. Annoyingly,
          * calling modifyBackground() on the ScolledWindow didn't work.
          */
 
         port = (Viewport) super.getChild();
         port.modifyBackground(StateType.NORMAL, Color.WHITE);
     }
 
     Component getComponent() {
         return component;
     }
 
     protected Widget createEditorForSegment(int index, Segment segment) {
         final Widget result;
         final EditorTextView editor;
         final HeadingBox heading;
         final ImageDisplayBox image;
         final NormalListitemBox listitem;
         final Scrollbar bar;
         final ScrolledWindow wide;
         final List<EditorTextView> editors;
 
         if (segment instanceof NormalSegment) {
             editor = new NormalEditorTextView(this, segment);
 
             result = editor;
         } else if (segment instanceof QuoteSegment) {
             editor = new QuoteEditorTextView(this, segment);
 
             result = editor;
         } else if (segment instanceof PoeticSegment) {
             editor = new PoeticEditorTextView(this, segment);
 
             result = editor;
         } else if (segment instanceof ListitemSegment) {
             listitem = new NormalListitemBox(this, segment);
 
             editor = listitem.getEditor();
             result = listitem;
         } else if (segment instanceof AttributionSegment) {
             editor = new AttributionEditorTextView(this, segment);
 
             result = editor;
         } else if (segment instanceof PreformatSegment) {
             editor = new PreformatEditorTextView(this, segment);
 
             wide = new ScrolledWindow();
             wide.setPolicy(PolicyType.ALWAYS, PolicyType.NEVER);
             wide.add(editor);
 
             /*
              * Having set up horizontal scrollbars for code blocks, we want to
              * make them a bit less obtrusive in normal use.
              */
 
             bar = wide.getHScrollbar();
             editor.connect(new Widget.FocusInEvent() {
                 public boolean onFocusInEvent(Widget source, EventFocus event) {
                     bar.setSensitive(true);
                     return false;
                 }
             });
             editor.connect(new Widget.FocusOutEvent() {
                 public boolean onFocusOutEvent(Widget source, EventFocus event) {
                     bar.setValue(0);
                     bar.setSensitive(false);
                     return false;
                 }
             });
             bar.setSensitive(false);
 
             bar.connect(new ExposeEvent() {
                 public boolean onExposeEvent(Widget source, EventExpose event) {
                     final Context cr;
                     final Surface surface;
 
                     if (bar.getSensitive()) {
                         return false;
                     }
 
                     cr = new Context(event);
 
                     cr.setSource(Color.WHITE);
                     cr.paint();
 
                     surface = cr.getTarget();
                     surface.flush();
 
                     return true;
                 }
             });
 
             result = wide;
         } else if (segment instanceof ImageSegment) {
             image = new ImageDisplayBox(this, segment);
 
             editor = image.getEditor();
             result = image;
         } else if (segment instanceof HeadingSegment) {
             heading = new SectionHeadingBox(this, segment);
 
             editor = heading.getEditor();
             result = heading;
         } else if (segment instanceof LeaderSegment) {
             editor = new LeaderEditorTextView(this, segment);
 
             result = editor;
         } else if (segment instanceof ChapterSegment) {
             heading = new ChapterHeadingBox(this, segment);
 
             editor = heading.getEditor();
             result = heading;
         } else if (segment instanceof DivisionSegment) {
             heading = new PartHeadingBox(this, segment);
 
             editor = heading.getEditor();
             result = heading;
         } else if (segment instanceof SpecialSegment) {
             // TODO placeholder; improve!
             editor = null;
             result = new SpecialHeadingBox(this, segment);
         } else {
             /*
              * Sanity check; don't really need this. FIXME In fact, returning
              * a Widget without placing an editor is probably harmful.
              */
 
             if ((segment instanceof EndnoteSegment) || (segment instanceof ReferenceSegment)) {
                 // skip
                 return null;
             }
             throw new IllegalStateException("Unknown Segment type");
         }
 
         editors = super.getEditors();
         editors.add(index, editor);
 
         return result;
     }
 
     /*
      * Convenience wrappers
      */
 
     void initialize(Component component) {
         final Series series;
 
         if (component == this.component) {
             return;
         }
 
         series = component.getSeriesMain();
         super.initializeSeries(series);
 
         this.component = component;
     }
 
     void advanceTo(Component replacement) {
         final Series series;
 
         if (replacement == this.component) {
             return;
         }
 
         series = replacement.getSeriesMain();
         super.advanceTo(series);
 
         this.component = replacement;
     }
 
    void reveseTo(Component replacement) {
         final Series series;
 
         if (replacement == this.component) {
             return;
         }
 
         series = replacement.getSeriesMain();
         super.reveseTo(series);
 
         this.component = replacement;
     }
 
     void propegateTextualChange(final PrimaryWindow primary, final Series former,
             final Series replacement) {
         final Component apres;
 
         apres = component.updateMain(replacement);
         primary.update(this, component, apres);
     }
 
     void propegateStructuralChange(final PrimaryWindow primary, final Series former,
             final Series replacement) {
         final Component apres;
 
         apres = component.updateMain(replacement);
         primary.update(this, component, apres);
     }
 }
