 /*
  * movie-renamer-core
  * Copyright (C) 2012 Nicolas Magré
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
 package fr.free.movierenamer.scrapper;
 
 import fr.free.movierenamer.exception.InvalidUrlException;
 import java.lang.reflect.ParameterizedType;
 import java.util.List;
 import java.util.Locale;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import fr.free.movierenamer.info.CastingInfo;
 import fr.free.movierenamer.info.ImageInfo;
 import fr.free.movierenamer.info.MediaInfo;
 import fr.free.movierenamer.scrapper.impl.FanartTVImagesScrapper;
 import fr.free.movierenamer.searchinfo.Media;
import fr.free.movierenamer.searchinfo.Movie;
 import fr.free.movierenamer.settings.Settings;
 import fr.free.movierenamer.utils.CacheObject;
 import fr.free.movierenamer.utils.LocaleUtils.AvailableLanguages;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 /**
  * Class MediaScrapper
  *
  * @author Nicolas Magré
  * @author Simon QUÉMÉNEUR
  */
 public abstract class MediaScrapper<M extends Media, MI extends MediaInfo> extends SearchScrapper<M> {
 
   protected MediaScrapper(AvailableLanguages... supportedLanguages) {
     super(supportedLanguages);
   }
 
   @Override
   protected final List<M> search(String query, Locale language) throws Exception {
     Logger.getLogger(SearchScrapper.class.getName()).log(Level.INFO, String.format("Use '%s' to search media for '%s' in '%s'", getName(), query, language.getDisplayLanguage(Locale.ENGLISH)));
     CacheObject cache = getCache();
     @SuppressWarnings("unchecked")
     Class<M> genericClazz = (Class<M>) ((ParameterizedType) getClass().getSuperclass().getGenericSuperclass()).getActualTypeArguments()[0]; // TODO put it in Utils !
     List<M> results = (cache != null) ? cache.getList(query, language, genericClazz) : null;
     if (results != null) {
       return results;
     }
 
     // perform actual search
     try {
       URL url = new URL(query);
       if(!url.getHost().equals(getHost())) {
         throw new InvalidUrlException(query);
       }
       results = searchMedia(url, language);
     }catch(MalformedURLException ex) {
       results = searchMedia(query, language);
     }
     Settings.LOGGER.log(Level.INFO, String.format("'%s' returns %d media for '%s' in '%s'", getName(), results.size(), query, language.getDisplayLanguage(Locale.ENGLISH)));
 
     // cache results and return
     return (cache != null) ? cache.putList(query, language, genericClazz, results) : results;
   }
 
   protected abstract List<M> searchMedia(String query, Locale language) throws Exception;
   protected abstract List<M> searchMedia(URL searchUrl, Locale language) throws Exception;
 
   public final MI getInfo(M search) throws Exception {
     return getInfo(search, getLanguage());
   }
 
   protected final MI getInfo(M search, Locale language) throws Exception {
     Logger.getLogger(SearchScrapper.class.getName()).log(Level.INFO, String.format("Use '%s' to get media info for '%s' in '%s'", getName(), search, language.getDisplayLanguage(Locale.ENGLISH)));
     CacheObject cache = getCache();
     @SuppressWarnings("unchecked")
     Class<MI> genericClazz = (Class<MI>) ((ParameterizedType) getClass().getSuperclass().getGenericSuperclass()).getActualTypeArguments()[1]; // TODO put it in Utils !
     MI info = (cache != null) ? cache.getData(search, language, genericClazz) : null;
     if (info != null) {
       return info;
     }
 
     // perform actual search
     info = fetchMediaInfo(search, language);
     Settings.LOGGER.log(Level.INFO, String.format("'%s' returns '%s' as info for '%s' in '%s'", getName(), info, search, language.getDisplayLanguage(Locale.ENGLISH)));
 
     //let's fetch casting
     List<CastingInfo> casting;
     try {
       casting = getCasting(search, language);
     } catch (Exception ex) {
       casting = null;
     }
     info.setCasting(casting);
     // info.setImages(getImages(searchResult, language));
 
     // cache results and return
     return (cache != null) ? cache.putData(search, language, info) : info;
   }
 
   protected abstract MI fetchMediaInfo(M searchResult, Locale language) throws Exception;
 
   public final List<ImageInfo> getImages(M search) throws Exception {
     return getImages(search, getLanguage());
   }
 
   protected final List<ImageInfo> getImages(M search, Locale language) throws Exception {
 
     List<ImageInfo> imagesInfo = new ArrayList<ImageInfo>();
     FanartTVImagesScrapper fanartImagesSc = new FanartTVImagesScrapper();
    List<ImageInfo> tmpImagesInfo = fanartImagesSc.getImages((Movie)search, language);// FIXMe cast Movie
     if(tmpImagesInfo != null) {
       imagesInfo.addAll(tmpImagesInfo);
     }
     // TODO add tmbd
 
     if(imagesInfo.isEmpty()) {
       tmpImagesInfo = fetchImagesInfo(search, language);
       if(tmpImagesInfo != null) {
         imagesInfo.addAll(tmpImagesInfo);//FIXME use cache instead
       }
     }
 
     return imagesInfo;
   }
 
   protected List<ImageInfo> fetchImagesInfo(M media, Locale language) throws Exception {
     return null;
   }
 
   public final List<CastingInfo> getCasting(M search) throws Exception {
     return getCasting(search, getLanguage());
   }
 
   protected final List<CastingInfo> getCasting(M search, Locale language) throws Exception {
     Logger.getLogger(SearchScrapper.class.getName()).log(Level.INFO, String.format("Use '%s' to get casting info list for '%s' in '%s'", getName(), search, language.getDisplayLanguage(Locale.ENGLISH)));
     CacheObject cache = getCache();
     List<CastingInfo> personsInfo = (cache != null) ? cache.getList(search, language, CastingInfo.class) : null;
     if (personsInfo != null) {
       return personsInfo;
     }
 
     // perform actual search
     personsInfo = fetchCastingInfo(search, language);
     Settings.LOGGER.log(Level.INFO, String.format("'%s' returns %d casting info for '%s' in '%s'", getName(), personsInfo.size(), search, language.getDisplayLanguage(Locale.ENGLISH)));
 
     // cache results and return
     return (cache != null) ? cache.putList(search, language, CastingInfo.class, personsInfo) : personsInfo;
   }
 
   protected abstract List<CastingInfo> fetchCastingInfo(M search, Locale language) throws Exception;
 
 }
