 package directi.androidteam.training.TagStore;
 
 public class Presence extends Tag {
     public Presence() {
         super("presence", null, null, null);
     }
 
     public Presence(Tag tag) {
         super("presence",tag.attributes,tag.childTags,tag.content);
         this.setRecipientAccount(tag.getRecipientAccount());
     }
 
    public Presence(String id, String from, String status, String show) {
         this.tagname = "presence";
         this.addAttribute("id", id);
         this.addAttribute("from", from);
         this.addChildTag(new Status(status));
        this.addChildTag(new Show(show));
     }
 
     public String getShow() {
         Tag show = this.getChildTag("show");
         if (show != null) {
             return show.getContent();
         } else {
             return null;
         }
     }
 
     public void setShow(String show) {
         this.addChildTag(new Show(show));
     }
 
     public String getStatus() {
         Tag status = this.getChildTag("status");
         if (status != null) {
             return status.getContent();
         } else {
             return null;
         }
     }
 
     public void setStatus(String status) {
         this.addChildTag(new Status(status));
     }
 
     public String getType() {
         return this.getAttribute("type");
     }
 
     public String getFrom() {
         return this.getAttribute("from");
     }
 
     public void setTo(String receiver) {
         this.addAttribute("to", receiver);
     }
 
     public void setType(String type) {
         this.addAttribute("type", type);
     }
 
     public String getAvatarShaOne() {
         Tag x = this.getChildTag("x");
         if (x == null) {return null;}
         Tag photo = x.getChildTag("photo");
         if (photo == null) {return null;}
         return photo.getContent();
     }
 }
