 package io.github.axxiss.places.request;
 
 import io.github.axxiss.places.enums.Params;
 
 /**
  * The Places Photo service is a read-only API that allows you to easily add high quality
  * photographic content to your application. The Photo service gives you access to the millions of
  * photos stored in the Places and Google+ Local database. When you search for Places using either a
  * Place Search or Place Details request, photo references will be returned for relevant
  * photographic content. The Photo service lets you access the referenced photos, and resize the
  * image to the optimal size for your application.
  *
  * @author Axxiss
  */
 public class PlacePhotos {
     protected static final String PHOTO = "photo";
 
     PlaceParams params = new PlaceParams();
 
     /**
      * All requests to the Place Photo service must include a photoreference, returned in the
      * response to a Place Search or Place Details request. The response to either service will
      * contain a photos[] field if that Place has related photographic content. The number of photos
      * returned varies by request. A Place Search will return at most one photo element in the array
      * while the Details request will return up to ten photo elements.
      * <p/>
      * Photos returned by the Photo service are sourced from a variety of locations, including
      * business owners and Google+ users contributed photos. In most cases, these photos can be used
      * without attribution, or will have the required attribution included as a part of the image.
      * However, if the returned photo element includes a value in the html_attributions field, you
      * will have to include the additional attribution in your application wherever you display the
      * image.
      *
      * @param reference place's reference
      * @return
      */
     public PlacePhotos(final String reference, final int maxHeight, final int maxWidth) {
        params.put(Params.PĥotoReference, reference);
         params.put(Params.MaxHeight, maxHeight);
         params.put(Params.MaxWidth, maxWidth);
     }
 }
