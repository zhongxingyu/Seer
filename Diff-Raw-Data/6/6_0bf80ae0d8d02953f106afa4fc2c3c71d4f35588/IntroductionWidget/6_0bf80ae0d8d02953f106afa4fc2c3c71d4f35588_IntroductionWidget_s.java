 /*
  * Quill and Parchment, a WYSIWYN document editor and rendering engine. 
  *
  * Copyright © 2010 Operational Dynamics Consulting, Pty Ltd
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
 
 import java.io.FileNotFoundException;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import org.freedesktop.cairo.Context;
 import org.freedesktop.cairo.Extend;
 import org.freedesktop.cairo.LinearPattern;
 import org.freedesktop.cairo.Pattern;
 import org.gnome.gdk.Pixbuf;
 import org.gnome.gtk.Alignment;
 import org.gnome.gtk.DrawingArea;
 import org.gnome.gtk.Image;
 import org.gnome.gtk.Justification;
 import org.gnome.gtk.Label;
 import org.gnome.gtk.LinkButton;
 import org.gnome.gtk.MenuBar;
 import org.gnome.gtk.MenuItem;
 import org.gnome.gtk.PolicyType;
 import org.gnome.gtk.ScrolledWindow;
 import org.gnome.gtk.VBox;
 import org.gnome.gtk.Widget;
 
 import static org.freedesktop.bindings.Internationalization._;
 
 /**
  * Introduction widget. Instead of a splash screen, this starts out being
  * displayed by the PrimaryWindow. Mostly this is here to tell you that F1 is
  * what you need to press for help.
  * 
  * @author Andrew Cowie
  */
 class IntroductionWidget extends ScrolledWindow
 {
     private final VBox top;
 
     private final MenuBar spacer;
 
     IntroductionWidget() {
         super();
         final ScrolledWindow scroll;
         final MenuItem blank;
         final Label title, description, help;
         final Pixbuf pixbuf;
         final Image image;
         Widget bar;
         Label warning;
         Alignment align;
         final LinkButton site;
 
         try {
             scroll = this;
             scroll.setPolicy(PolicyType.NEVER, PolicyType.AUTOMATIC);
             top = new VBox(false, 0);
             scroll.addWithViewport(top);
 
             /*
              * We need to put a MenuBar at the top of the IntroductionWidget
              * pane so that when the application's real MenuBar is shown we
              * can hide this one for the effect that the content of the pane
              * stays put. This is unfortunately necessary because toggling the
              * optional menu is something you can expect from this pane, and
              * we want the view to stay still.
              */
 
             spacer = new MenuBar();
             top.packStart(spacer, false, false, 0);
             blank = new MenuItem("Quill and Parchment");
             spacer.prepend(blank);
             spacer.connect(new Widget.Draw() {
                 public boolean onDraw(Widget source, Context cr) {
                     return true;
                 }
             });
 
             /*
              * Now on with the actual display.
              */
 
             pixbuf = new Pixbuf("share/quill/images/feather.png", 128, 128, true);
             image = new Image(pixbuf);
             image.setAlignment(Alignment.CENTER, Alignment.BOTTOM);
             top.packStart(image, false, false, 10);
 
             title = new Label("<span font='DejaVu Serif, 34' color='darkgreen'>Quill </span>"
                     + "<span font='Inconsolata, 36' color='darkblue'>and</span>"
                     + "<span font='Linux Libertine O, 40'> Parchment</span>");
             title.setUseMarkup(true);
             title.setJustify(Justification.CENTER);
             title.setAlignment(Alignment.CENTER, Alignment.TOP);
             top.packStart(title, false, false, 0);
 
             align = new Alignment(0.5f, 0.5f, 0.0f, 0.0f);
             site = new LinkButton(new URI("http://research.operationaldynamics.com/projects/quill/"));
             align.add(site);
             top.packStart(align, false, false, 0);
 
             description = new Label(
                     "This is <b>Quill</b>, a <u>W</u>hat <u>Y</u>ou <u>S</u>ee <u>I</u>s <u>W</u>hat <u>Y</u>ou <u>N</u>eed document editor, "
                             + "attempting to give you a clean display of your content and subtle indications of semantic markup."
                             + "\n\n"
                             + "<b>Parchment</b> is a simple but powerful rendering "
                             + "back-end which takes these documents and outputs them as PDF files. "
                             + "There are rendering engines (ie, stylesheets) customized for various different document types.");
             description.setLineWrap(true);
             description.setWidthChars(50);
             description.setUseMarkup(true);
             description.setPadding(15, 0);
             description.setJustify(Justification.CENTER);
             top.packStart(description, false, false, 20);
 
             help = new Label(_("Press F12 to show the menu"));
             help.setLineWrap(false);
             help.setUseMarkup(true);
 
             top.packStart(help, true, false, 0);
 
             bar = new UnderConstruction();
             top.packStart(bar, false, false, 0);
 
             warning = new Label(
                     "<b>"
                             + _("Under Construction")
                             + "</b>\n\n"
                             + _("This is experimental software and still under heavy development. "
                                     + "It is <i>not</i> suitable for general use.")
                             + " "
                             + _("Quill and Parchment <i>will</i> crash, "
                                     + "and it <i>will</i> eat your documents, "
                                     + "though if possible we attempt to save what you "
                                     + "were doing in \"recovery\" files with a")
                             + " <tt>.RESCUED</tt> "
                             + _("extension.")
                             + " "
                             + _("The on-disk format of manuscripts and chapters is text based, "
                                     + "so we encourage you to store your documents in Bazaar or another version control system.")
                             + "\n");
             warning.setLineWrap(true);
             warning.setUseMarkup(true);
             warning.setWidthChars(50);
             warning.setAlignment(Alignment.CENTER, Alignment.TOP);
             warning.setPadding(15, 10);
             warning.setJustify(Justification.CENTER);
             top.packStart(warning, false, false, 0);
 
         } catch (URISyntaxException e) {
             e.printStackTrace();
             throw new AssertionError();
         } catch (FileNotFoundException e) {
             e.printStackTrace();
             throw new AssertionError();
         }
     }
 
     void showSpacer() {
         spacer.show();
     }
 
     void hideSpacer() {
         spacer.hide();
     }
 }
 
 class UnderConstruction extends Alignment
 {
     private final Alignment align;
 
     UnderConstruction() {
         super(Alignment.CENTER, Alignment.CENTER, 1.0f, 0.0f);
         final DrawingArea drawing;
         align = this;
 
         drawing = new DrawingArea();
         drawing.setSizeRequest(-1, 30);
 
         drawing.connect(new Widget.Draw() {
             public boolean onDraw(Widget source, Context cr) {
                 final Pattern pattern;
 
                 pattern = new LinearPattern(0.0, 0.0, 20.0, 20.0);
                 pattern.addColorStopRGB(0.0, 0.0, 0.0, 0.0);
                pattern.addColorStopRGB(0.5, 0.0, 0.0, 0.0);
                pattern.addColorStopRGB(0.5, 255.0 / 255.0, 196.0 / 255.0, 0.0 / 255.0);
                pattern.addColorStopRGB(1.0, 255.0 / 255.0, 196.0 / 255.0, 0.0 / 255.0);
                 pattern.addColorStopRGB(1.0, 0.0, 0.0, 0.0);
                 pattern.setExtend(Extend.REPEAT);
 
                 cr.setSource(pattern);
 
                 cr.paint();
 
                 return false;
             }
         });
 
         align.add(drawing);
     }
 }
