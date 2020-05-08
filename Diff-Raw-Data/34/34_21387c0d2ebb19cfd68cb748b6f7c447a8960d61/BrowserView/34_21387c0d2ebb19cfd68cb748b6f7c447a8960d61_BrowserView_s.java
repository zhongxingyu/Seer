 package com.lyndir.lhunath.snaplog.webapp.components;
 
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
 import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.inject.Inject;
 import com.lyndir.lhunath.lib.system.logging.Logger;
 import com.lyndir.lhunath.snaplog.data.Media;
 import com.lyndir.lhunath.snaplog.data.Media.Quality;
 import com.lyndir.lhunath.snaplog.model.AlbumService;
 import com.lyndir.lhunath.snaplog.util.SnaplogConstants;
 
 
 /**
  * <h2>{@link BrowserView}<br>
  * <sub>Component that allows users to browse through media chronologically.</sub></h2>
  * 
  * <p>
  * <i>Jan 6, 2010</i>
  * </p>
  * 
  * @author lhunath
  */
 public final class BrowserView extends Panel {
 
     static final Logger        logger              = Logger.get( BrowserView.class );
 
     protected static final int BROWSER_SIDE_IMAGES = 4;
 
     @Inject
     AlbumService               albumService;
 
     protected Media            currentFile;
 
 
     /**
      * Create a new {@link BrowserView} instance.
      * 
      * @param id
      *            The wicket ID to put this component in the HTML.
      * @param currentTimeModel
      *            The model contains the {@link Date} upon which the browser should focus. The first image on or past
      *            this date will be the focussed image.
      */
     public BrowserView(String id, final IModel<Date> currentTimeModel) {
 
         super( id, currentTimeModel );
         setOutputMarkupId( true );
 
         add( new BrowserListView( "photos", currentTimeModel ) );
     }
 
 
     /**
      * <h2>{@link BrowserListView}<br>
      * <sub>A {@link ListView} which enumerates {@link Media}s.</sub></h2>
      * 
      * <p>
      * <i>Jan 6, 2010</i>
      * </p>
      * 
      * @author lhunath
      */
     private final class BrowserListView extends ListView<Media> {
 
         final IModel<Date> currentTimeModel;
 
 
         /**
          * Create a new {@link BrowserListView} instance.
          */
         BrowserListView(String id, IModel<Date> currentTimeModel) {
 
             super( id, new BrowserFilesModel( currentTimeModel ) );
 
             this.currentTimeModel = currentTimeModel;
         }
 
         @Override
         protected void populateItem(ListItem<Media> item) {
 
            Media file = item.getModelObject();
 
            Quality imageQuality = Quality.THUMBNAIL;
            if (file.equals( currentFile ))
                imageQuality = Quality.SIZED;
             item.add( new AttributeAppender( "class", new Model<String>( imageQuality.getName() ), " " ) );
 
             final long shotTime = file.shotTime();
             AjaxFallbackLink<String> link = new AjaxFallbackLink<String>( "link" ) {
 
                 @Override
                 public void onClick(AjaxRequestTarget target) {
 
                     currentTimeModel.setObject( new Date( shotTime ) );
                     target.addComponent( BrowserView.this );
                 }
             };
             item.add( link );
 
             link.add( new Label( "caption", file.getDateString() ) );
            WebMarkupContainer photo = new WebMarkupContainer( "photo" );
            photo.add( new AttributeModifier( "src", new Model<String>(
                    albumService.getResourceURI( file, imageQuality ).toASCIIString() ) ) );
            link.add( photo );
         }
     }
 
 
     /**
      * <h2>{@link BrowserFilesModel}<br>
      * <sub>A {@link Model} that enumerates all files the browser should display when centered on a certain point in
      * time.</sub></h2>
      * 
      * <p>
      * <i>Jan 6, 2010</i>
      * </p>
      * 
      * @author lhunath
      */
     private final class BrowserFilesModel extends AbstractReadOnlyModel<List<Media>> {
 
         private final IModel<Date> currentTimeModel;
 
 
         /**
          * Create a new {@link BrowserFilesModel} instance.
          */
         BrowserFilesModel(IModel<Date> currentTimeModel) {
 
             this.currentTimeModel = currentTimeModel;
         }
 
         @Override
         public List<Media> getObject() {
 
             List<? extends Media> allFiles = albumService.getFiles( SnaplogConstants.DEFAULT_ALBUM );
             LinkedHashMap<Media, ?> files = new LinkedHashMap<Media, Object>( BROWSER_SIDE_IMAGES * 2 + 1 ) {
 
                 @Override
                 protected boolean removeEldestEntry(Map.Entry<Media, Object> eldest) {
 
                     return size() > BROWSER_SIDE_IMAGES * 2 + 1;
                 }
             };
 
             Iterator<? extends Media> it = Iterables.reverse( allFiles ).iterator();
             if (!it.hasNext())
                 return null;
 
             // Find the current file.
             while (it.hasNext()) {
                 Media nextFile = it.next();
 
                 if (nextFile.shotTime() < currentTimeModel.getObject().getTime())
                     break;
 
                 currentFile = nextFile;
                files.put( currentFile, null );
             }
 
             // Add the side images past the current file (we already have the ones before it).
             for (int i = 0; i < BROWSER_SIDE_IMAGES; ++i)
                 files.put( it.next(), null );
 
             return ImmutableList.copyOf( files.keySet() );
         }
     }
 }
