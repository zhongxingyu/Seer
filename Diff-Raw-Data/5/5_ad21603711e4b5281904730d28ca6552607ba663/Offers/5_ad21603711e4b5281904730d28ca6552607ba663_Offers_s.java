 package controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import models.Offer;
 import models.Tag;
 import models.User;
 import service.MatchService;
 import service.Utils;
 
 public class Offers extends BaseController
 {
     public static void create() {
     	Offer offerItem = new Offer();
     	offerItem.tags = new ArrayList<Tag>();
     	renderTemplate("Offers/form.html", offerItem);
     }
 
     public static void doCreate(String tags, Offer offerItem) {
     	User user = getConnectedUser();
 
     	boolean isCreate = offerItem.id == null;
 
     	if (!isCreate) {
     		// prevent tags to be appended to existing tags on edit
     		Tag.delete("offer.id", offerItem.id);
     	}
 		List<String> tagsListString = Utils.parseTags(tags);
 		List<Tag> tagsList = new ArrayList<Tag>();
 		for (String tagString : tagsListString) {
 		    Tag tag = new Tag(offerItem, tagString);
 		    tagsList.add(tag);
 		}
 		offerItem.tags = tagsList;
 
 		validation.valid(offerItem);
 		if (validation.hasErrors()) {
 		    renderTemplate("Offers/form.html", offerItem);
 		}
 	
 		offerItem.user = user;
 		offerItem.save();
 	
 		show(offerItem.id);
     }
     
     public static void save(Long offerId) {
     	Offer offerItem = Offer.findById(offerId);
 	offerItem.save();
 	show(offerItem.id);
     }
 
     public static void show(Long id) {
 	Offer offerItem = Offer.findById(id);
 	render(offerItem);
     }
 
     public static void showAfterEdit(Long id) {
 	Offer offerItem = Offer.findById(id);
 	Boolean isOldOffer = true;
 	render(offerItem, isOldOffer);
     }
 
     public static void showDetails(Long id) {
     	User user = getConnectedUser();
     	Offer offerItem = Offer.findById(id);
	Boolean someoneElsesOffer = isSomeoneElses(id);
    	render(user, offerItem, someoneElsesOffer);
     }
 
     public static void search(String phrase) {
     	User user = getConnectedUser();
 
     	List<Offer> allOffers = Offer.findAll();
     	List<Offer> foundOffers = MatchService.match(allOffers, phrase);
 
     	render(user, foundOffers, allOffers, phrase);
     }
 
     public static void edit(Long id) {
     	Offer offerItem = Offer.findById(id);
     	renderTemplate("Offers/form.html", offerItem);
     }
 
     public static boolean isSomeoneElses(Long offerId) {
 	User currentUser = getConnectedUser();
 	Offer currentOffer = Offer.findById(offerId);
 	User owner = currentOffer.user;
 	return !(currentUser.equals(owner));
     }
 
     public static void list() {
 	User user = getConnectedUser();
 	List<Offer> offers = Offer.find("user.id", user.id).fetch();
 	render(user, offers);
     }
 }
