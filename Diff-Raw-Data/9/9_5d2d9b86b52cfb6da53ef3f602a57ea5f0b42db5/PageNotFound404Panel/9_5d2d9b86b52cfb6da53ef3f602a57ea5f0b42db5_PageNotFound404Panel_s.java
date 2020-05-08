 package de.flower.rmt.ui.common.page.error;
 
 import de.flower.rmt.ui.app.Links;
 import de.flower.rmt.ui.common.panel.BasePanel;
import org.apache.wicket.markup.html.link.Link;
 
 /**
  * @author flowerrrr
  */
 public class PageNotFound404Panel extends BasePanel {
 
     public PageNotFound404Panel() {
         add(Links.homePage("home"));
        add(new Link("link") {

            @Override
            public void onClick() {
                throw new UnsupportedOperationException("Feature not implemented!");
            }
        });
     }
 }
