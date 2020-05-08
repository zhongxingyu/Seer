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
 
 import org.gnome.gtk.SizeGroup;
 import org.gnome.gtk.Widget;
 
 import quill.textbase.Component;
 import quill.textbase.EndnoteSegment;
 import quill.textbase.Segment;
 import quill.textbase.Series;
 
 import static org.gnome.gtk.SizeGroupMode.HORIZONTAL;
 
 /**
  * Series editor for editing notes or references.
  * 
  * @author Andrew Cowie
  */
 // cloned from ReferencesSeriesEditorWidget
 class EndnotesSeriesEditorWidget extends SeriesEditorWidget
 {
     private SizeGroup group;
 
     private Component component;
 
     EndnotesSeriesEditorWidget(PrimaryWindow primary) {
         super(primary, "Endnotes");
         setupLabels();
     }
 
     Component getComponent() {
         return component;
     }
 
     private void setupLabels() {
         group = new SizeGroup(HORIZONTAL);
     }
 
     Widget createEditorForSegment(int index, Segment segment) {
         final PrimaryWindow primary;
         final Widget result;
         final EditorTextView editor;
         final ReferenceListitemBox listitem;
         final List<EditorTextView> editors;
 
         primary = super.getPrimary();
 
         if (segment instanceof EndnoteSegment) {
             listitem = new ReferenceListitemBox(this, segment);
 
             editor = listitem.getEditor();
             result = listitem;
         } else {
             throw new AssertionError();
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
 
         series = component.getSeriesEndnotes();
         super.initializeSeries(series);
 
         this.component = component;
     }
 
     void advanceTo(Component replacement) {
         final Series series;
 
         if (replacement == this.component) {
             return;
         }
 
         series = replacement.getSeriesEndnotes();
         super.advanceTo(series);
 
         this.component = replacement;
     }
 
    void reverseTo(Component replacement) {
         final Series series;
 
         if (replacement == this.component) {
             return;
         }
 
         series = replacement.getSeriesEndnotes();
         super.reveseTo(series);
 
         this.component = replacement;
     }
 
     void propegateTextualChange(final PrimaryWindow primary, final Series former,
             final Series replacement) {
         final Component apres;
 
         apres = component.updateEndnotes(replacement);
         primary.update(this, component, apres);
     }
 
     void propegateStructuralChange(final PrimaryWindow primary, final Series former,
             final Series replacement) {
         final Component apres;
 
         apres = component.updateEndnotes(replacement);
         primary.update(this, component, apres);
     }
 }
