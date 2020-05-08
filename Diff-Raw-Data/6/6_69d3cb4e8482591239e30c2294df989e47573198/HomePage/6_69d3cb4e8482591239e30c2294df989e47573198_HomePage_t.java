 package com.solidstategroup.radar.web.pages;
 
 
import com.solidstategroup.radar.web.pages.content.DiseaseIndexPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

 public class HomePage extends BasePage {
     public HomePage() {
        add(new BookmarkablePageLink("diseaseIndexLink", DiseaseIndexPage.class));
     }
 
 }
