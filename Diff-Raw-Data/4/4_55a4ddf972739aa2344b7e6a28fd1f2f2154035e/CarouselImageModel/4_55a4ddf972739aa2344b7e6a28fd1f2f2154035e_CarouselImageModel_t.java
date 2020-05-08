 package ch9k.plugins.carousel;
 
 import ch9k.chat.Conversation;
 import ch9k.core.Model;
 import ch9k.plugins.ProvidedImage;
 
 /**
  * A model class describing the image selection.
  */
 public class CarouselImageModel extends Model {
     /**
      * Current image.
      */
     private ProvidedImage image; 
 
     /**
      * The conversation.
      */
     private Conversation conversation;
 
     /**
      * If this image has been recommended.
      */
     private boolean recommended;
 
     /**
      * Constructor.
      */
     public CarouselImageModel(Conversation conversation)
     {
         image = null;
         this.conversation = conversation;
         recommended = false;
     }
 
     /**
      * Set a provided image.
      * @param image New image to set as selection.
      */
     public void setProvidedImage(ProvidedImage image) {
        if(image != null && (this.image == null ||
                !this.image.equals(image))) {
             this.image = image;
             recommended = false;
             fireStateChanged();
         }
     }
 
     /**
      * Get the provided image.
      * @return The provided image.
      */
     public ProvidedImage getProvidedImage() {
         return image;
     }
 
     /**
      * Get the relevant conversation.
      * @return The relevant conversation.
      */
     public Conversation getConversation() {
         return conversation;
     }
 
     /**
      * Set that this image has been recommended.
      * @param recommended If this image has been recommended.
      */
     public void setRecommended(boolean recommended) {
         if(recommended != this.recommended) {
             this.recommended = recommended;
             fireStateChanged();
         }
     }
 
     /**
      * Check if this image has been recommended.
      * @return If this image has been recommended.
      */
     public boolean isRecommended() {
         return recommended;
     }
 }
