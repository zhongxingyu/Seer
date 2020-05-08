 package com.centretown.tuba.client.cell;
 
 import com.centretown.tuba.client.youtube.request.YoutubeInfo;
 import com.google.gwt.cell.client.AbstractCell;
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.safehtml.client.SafeHtmlTemplates;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 
 public class InfoCell extends AbstractCell<YoutubeInfo> {
   interface Template extends SafeHtmlTemplates {
     @Template("<td><img src='{0}' width='80' height='45'></td><td>{1}</td>")
    SafeHtml render(String url, String title);
   }
 
   private static Template template = GWT.create(Template.class);
 
   @Override
   public void render(com.google.gwt.cell.client.Cell.Context context,
       YoutubeInfo value, SafeHtmlBuilder sb) {
    sb.append(template.render(value.getThumbnail(), value.getTitle()));
   }
 
 }
