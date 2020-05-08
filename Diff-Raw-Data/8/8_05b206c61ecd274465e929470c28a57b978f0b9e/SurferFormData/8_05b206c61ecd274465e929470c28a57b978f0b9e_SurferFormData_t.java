 package views.formdata;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import models.Surfer;
 import models.SurferDB;
 import play.data.validation.ValidationError;
 
 /**
  * Holds the data when a New Contact form is filled in.
  */
 public class SurferFormData {
   
   /** Surfer name.*/
   public String name = "";
   
   /** Surfer hometown. */
   public String hometown = "";
   
   /** Surfer awards. */
   public String awards = "";
   
   /** URL for carousel image. */
   public String carouselURL;
   
   /** URL for bio image. */
   public String bioURL;
   
   /** Biography. */
   public String bio;
   
   /** Surfer slug. */
   public String slug;
   
   /** Surfer type. */
   public String type;
   public int index;
   public int slugIndex;
   /** Surfing style of Surfer. */
   public String footstyle;
   
   /**
    * No argument constructor for this class.
    */
   public SurferFormData() { 
   }
   
   /**
    * Constructor that takes a contact.
    * @param surfer The Surfer to add.
    */
   public SurferFormData(Surfer surfer) {
     this.name = surfer.getName();
     this.hometown = surfer.getHometown();
     this.awards = surfer.getAwards();
     this.carouselURL = surfer.getCarouselURL();
     this.bioURL = surfer.getBioURL();
     this.bio = surfer.getBio();
     this.slug = surfer.getSlug();
     this.type = surfer.getType();
     this.index = surfer.getIndex();
     this.slugIndex = surfer.getSlugIndex();
     this.footstyle = surfer.getFootstyle();
   }
   
   /**
    * Checks to make sure that all form fields are filled.
    * @return null on success.
    *         List<Validation Error> of errors on failure.
    */
   public List<ValidationError> validate() {
     List<ValidationError> errors = new ArrayList<>();
     
     if (name == null || name.length() == 0) {
       errors.add(new ValidationError("name", "Name is required."));
     }
     
     if (hometown == null || hometown.length() == 0) {
       errors.add(new ValidationError("hometown", "Hometown is required."));
     }
     
     if (carouselURL == null || carouselURL.length() == 0) {
       errors.add(new ValidationError("carouselURL", "Carousel URL is required."));
     }
     
     if (bioURL == null || bioURL.length() == 0) {
       errors.add(new ValidationError("bioURL", "Bio image URL is required."));
     }
     
     if (bio == null || bio.length() == 0) {
       errors.add(new ValidationError("bio", "A one paragraph bio is required."));
     }
    if (slugIndex == 0 && SurferDB.getSurfer(slug) != null) {
      errors.add(new ValidationError("slug", "The slug \"" + slug + "\" is already taken."));      
    }
    if (slug == null || slug.length() == 0) {
      errors.add(new ValidationError("slug", "Slug is required."));
     }
     
     if (type == null || type.length() == 0 || !SurferTypes.isTypes(type)) {
       errors.add(new ValidationError("type", "Type is required."));
     }
     
     return errors.isEmpty() ? null : errors;
   }
 }
