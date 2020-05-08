 /*
  * movie-renamer
  * Copyright (C) 2012 QUÉMÉNEUR Simon
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.free.movierenamer.worker;
 
 import fr.free.movierenamer.media.IMediaImage;
 import fr.free.movierenamer.media.IMediaInfo;
 import fr.free.movierenamer.media.MediaID;
 import fr.free.movierenamer.parser.xml.MrParser;
 import fr.free.movierenamer.parser.xml.XMLParser;
 import fr.free.movierenamer.utils.ActionNotValidException;
 import fr.free.movierenamer.utils.Settings;
 import fr.free.movierenamer.utils.YTdecodeUrl;
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import javax.swing.event.SwingPropertyChangeSupport;
 import javax.xml.parsers.ParserConfigurationException;
 import org.xml.sax.SAXException;
 
 /**
  * Class MediaInfoWorker
  * 
  * @param <T> 
  * @param <U> 
  * @author QUÉMÉNEUR Simon
  */
 public abstract class MediaInfoWorker<T extends IMediaInfo<U>, U extends IMediaImage> extends HttpWorker<T> {
 
   protected final MediaID id;
 
   /**
    * Constructor arguments
    * 
    * @param errorSupport Swing change support
    * @param id Media ID
    * @throws ActionNotValidException
    */
   public MediaInfoWorker(SwingPropertyChangeSupport errorSupport, MediaID id) throws ActionNotValidException {
     super(errorSupport);
     this.id = id;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see fr.free.movierenamer.worker.HttpWorker#proccessFile(java.io.File)
    */
   @Override
   protected final T proccessFile(File xmlFile) throws Exception {
     T mediaInfo = null;
     U mediaImage = null;
 
     try {
       // Parse XML
       XMLParser<T> xmp = new XMLParser<T>(xmlFile.getAbsolutePath());
       MrParser<T> infoParser = getInfoParser();
       infoParser.setOriginalFile(xmlFile);
       xmp.setParser(infoParser);
       mediaInfo = xmp.parseXml();
 
       XMLParser<U> xmmp = new XMLParser<U>(xmlFile.getAbsolutePath());
       MrParser<U> imageParser = getImageParser();
       imageParser.setOriginalFile(xmlFile);
       xmmp.setParser(imageParser);
       mediaImage = xmmp.parseXml();
 
     } catch (IOException ex) {
       Settings.LOGGER.log(Level.SEVERE, null, ex);
     } catch (InterruptedException ex) {
       Settings.LOGGER.log(Level.SEVERE, null, ex);
     } catch (ParserConfigurationException ex) {
       Settings.LOGGER.log(Level.SEVERE, null, ex);
     } catch (SAXException ex) {
       Settings.LOGGER.log(Level.SEVERE, null, ex);
     }
 
     if (mediaInfo == null) {
       firePropertyChange("closeLoadingDial", "scrapperInfoFailed");
       return null;
     }
 
     ((IMediaInfo<U>) mediaInfo).setImages(mediaImage);
    
    if (!((IMediaInfo<U>) mediaInfo).getTrailer().equals("")) {
      String trailer = YTdecodeUrl.getRealUrl(((IMediaInfo<U>) mediaInfo).getTrailer(), YTdecodeUrl.HD);
       if (trailer != null) {
        ((IMediaInfo<U>) mediaInfo).setTrailer(trailer);
       }
     }
 
     setProgress(100);
     return mediaInfo;
   }
 
   /**
    * @return The Parser for the info
    * @throws Exception
    */
   protected abstract MrParser<T> getInfoParser() throws Exception;
 
   /**
    * @return The Parser for the image
    * @throws Exception
    */
   protected abstract MrParser<U> getImageParser() throws Exception;
 }
