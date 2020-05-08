 /*
  *   Copyright 2009, Maarten Billemont
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package com.lyndir.lhunath.snaplog.data.object.media;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import com.google.common.base.Objects;
 import com.lyndir.lhunath.lib.system.logging.Logger;
 import com.lyndir.lhunath.lib.wayward.i18n.MessagesFactory;
 import com.lyndir.lhunath.snaplog.data.object.security.AbstractSecureObject;
 import com.lyndir.lhunath.snaplog.model.service.WebUtil;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 
 
 /**
  * <h2>{@link Media}<br> <sub>DO for .</sub></h2>
  *
  * <p> <i>Jul 25, 2009</i> </p>
  *
  * @author lhunath
  */
 public abstract class Media extends AbstractSecureObject<Album> implements Comparable<Media> {
 
     static final Logger logger = Logger.get( Media.class );
     static final Messages msgs = MessagesFactory.create( Messages.class );
 
     private static final transient DateTimeFormatter filenameFormat = ISODateTimeFormat.basicDateTimeNoMillis();
 
     private final String name;
 
     /**
      * @param name The unique name of this media in the album.
      */
     protected Media(final String name) {
 
         this.name = checkNotNull( name, "Given media name must not be null." );
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Album getParent() {
 
         return getAlbum();
     }
 
     /**
      * @return The name of this {@link Media}.
      */
     public String getName() {
 
         return name;
     }
 
     /**
      * @return The album of this {@link Media}.
      */
     public abstract Album getAlbum();
 
     /**
      * Obtain the time since the UNIX Epoch in milliseconds since the picture was taken.
      *
      * @return The amount of milliseconds, or 0 if it could not be determined.
      */
     public long shotTime() {
 
         StringBuffer shotTimeString = new StringBuffer( getName() );
 
         // Trim the extension off the filename.
        int extensionIndex = shotTimeString.lastIndexOf( "." );
        if (extensionIndex > 0)
            shotTimeString.delete( extensionIndex, shotTimeString.length() );
 
         // Trim the "hidden file prefix" off the filename.
         while (shotTimeString.charAt( 0 ) == '.')
             shotTimeString.deleteCharAt( 0 );
 
         // Trim "_extras" off the filename.
        int extraIndex = shotTimeString.lastIndexOf( "_" );
        if (extraIndex > 0)
            shotTimeString.delete( extraIndex, shotTimeString.length() );
 
         // No time zone == UTC.
         if (shotTimeString.indexOf( "+" ) < 0 && shotTimeString.indexOf( "-" ) < 0)
             shotTimeString.append( "+0000" );
 
         try {
             return filenameFormat.parseMillis( shotTimeString.toString() );
         }
 
         catch (IllegalArgumentException e) {
             logger.wrn( e, "Couldn't parse shot time: %s, for file: %s", shotTimeString, name );
 
             return 0;
         }
     }
 
     /**
      * Generate a string to express the time at which the shot was taken; formatted according to the active web session's locale.
      *
      * @return A date formatted according to the active locale.
      */
     public String getDateString() {
 
         return DateTimeFormat.mediumDateTime().withLocale( WebUtil.getLocale() ).print( shotTime() );
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int compareTo(final Media o) {
 
         if (shotTime() > o.shotTime())
             return 1;
         else if (shotTime() < o.shotTime())
             return -1;
 
         return 0;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean equals(final Object o) {
 
         if (o == this)
             return true;
         if (!getClass().isInstance( o ))
             return false;
 
         return Objects.equal( ((Media) o).getName(), getName() ) && Objects.equal( ((Media) o).getAlbum(), getAlbum() );
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int hashCode() {
 
         return Objects.hashCode( getName(), getAlbum() );
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String toString() {
 
         return String.format( "{media: name=%s}", name );
     }
 
     @Override
     public String typeDescription() {
 
         return msgs.type();
     }
 
     @Override
     public String objectDescription() {
 
         return msgs.description( name );
     }
 
     /**
      * <h2>{@link Quality}<br> <sub>The media resource is available at different {@link Quality} levels.</sub></h2>
      *
      * <p> <i>Jan 6, 2010</i> </p>
      *
      * @author lhunath
      */
     public enum Quality {
 
         /**
          * The full quality of the original media file.
          */
         METADATA( "metadata", 0, 0, 0 ),
 
         /**
          * The full quality of the original media file.
          */
         ORIGINAL( "original", -1, -1, 1 ),
 
         /**
          * Media quality fit for displaying the media such that it fills the screen.
          */
         FULLSCREEN( "fullscreen", 1024, 768, 0.9f ),
 
         /**
          * Media quality fit for previewing the media at a size where it is easy to tell what it depicts.
          */
         PREVIEW( "preview", 600, 450, 0.8f ),
 
         /**
          * Media quality fit for giving a hint on what the media is about.
          */
         THUMBNAIL( "thumbnail", 150, 100, 0.75f );
 
         private final String name;
         private final int maxWidth;
         private final int maxHeight;
         private final float compression;
 
         Quality(final String name, final int maxWidth, final int maxHeight, final float compression) {
 
             this.name = checkNotNull( name, "Given quality name must not be null." );
             this.maxWidth = maxWidth;
             this.maxHeight = maxHeight;
             this.compression = compression;
         }
 
         /**
          * @return The identifier used for this quality.
          */
         public String getName() {
 
             return name;
         }
 
         /**
          * @return The maximum width media at this quality should have, or <code>-1</code> if there should be no limit.
          */
         public int getMaxWidth() {
 
             return maxWidth;
         }
 
         /**
          * @return The maximum height media at this quality should have, or <code>-1</code> if there should be no limit.
          */
         public int getMaxHeight() {
 
             return maxHeight;
         }
 
         /**
          * @return The compression ratio media at this quality should use. A decimal number between <code>0</code> and <code>1</code>
          *         (inclusive) where <code>1</code> indicates maximum quality.
          */
         public float getCompression() {
 
             return compression;
         }
 
         /**
          * Find the {@link Quality} by the given name.
          *
          * @param qualityName The name of the quality (case insensitive) you're after.
          *
          * @return <code>null</code> if no quality exists for the given name.
          *
          * @see #getName()
          */
         public static Quality findQualityWithName(final String qualityName) {
 
             for (final Quality quality : Quality.values())
                 if (quality.getName().equalsIgnoreCase( qualityName ))
                     return quality;
 
             return null;
         }
     }
 
 
     interface Messages {
 
         /**
          * @return The name of this type.
          */
         String type();
 
         /**
          * @param name The name of the media.
          *
          * @return A description of a media.
          */
         String description(String name);
     }
 }
