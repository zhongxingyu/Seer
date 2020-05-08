 package controllers;
 
 import java.util.*;
 
 import models.City;
 import models.County;
 import models.User;
 import models.Offer;
 import models.Request;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.With;
 import play.data.validation.*;
 
 public class Application extends Controller
 {   
     @Before
 	static void setConnectedUser() {
 	boolean isConnected = Security.isConnected();
 	renderArgs.put("isLoggedIn", isConnected);
 	if(isConnected) {
 	    User user = User.find("byEmail", Security.connected()).first();
 	    renderArgs.put("user", user);
 	}
     }
     public static void index()
     {
 	render();
     }
 
     public static void register() {
 	render();
     }
 
     public static void createOffer() {
 	render();
     }
 
     public static void finalizeOffer(Offer offerItem) {
 	render(offerItem);
     }
 
     public static void createRequest() {
 	render();
     }
 
     public static void doCreateOfferItem(@Valid Offer offerItem) {
 	if (validation.hasErrors()) {
 	    params.flash();
 	    validation.keep();
 	    createOffer();
 	}
 	finalizeOffer(offerItem);
     }
 
     public static void doCreateRequestItem(@Valid Request requestItem) {
 	if (validation.hasErrors()) {
 	    params.flash();
 	    validation.keep();
 	    createRequest();
 	}
 	finalizeRequest(requestItem);
     }
 
     public static void finalizeRequest(Request requestItem) {
 	render(requestItem);
     }
 
     public static void saveOffer(Offer offerItem) {
 	offerItem.save();
 	showOffer(offerItem.id);
     }
 
     public static void saveRequest(Request requestItem) {
 	requestItem.save();
 	showRequest(requestItem.id);
     }
 
     public static void showOffer(Long id) {
 	Offer offerItem = Offer.findById(id);
 	render(offerItem);
     }
 
     public static void showOfferDetails(Long id) {
 	Offer offerItem = Offer.findById(id);
 	render(offerItem);
     }
 
     public static void showRequestDetails(Long id) {
 	Request requestItem = Request.findById(id);
 	render(requestItem);
     }
     
     public static void showRequest(Long id) {
 	Request requestItem = Request.findById(id);
 	render(requestItem);
     }
 
     public static void listUserOffers(String email) {
 	List<Offer> offers = Offer.find("byUserEmail", email).fetch();
 	render(offers);
     }
 
     public static void listUserRequests(String email) {
 	List<Request> requests = Request.find("byUserEmail", email).fetch();
 	render(requests);
     }
 
     public static void searchOffers() {
 	List<Offer> offers = Offer.all().fetch();
 	render(offers);
     }
 
     public static void searchRequests() {
 	List<Request> requests = Request.all().fetch();
 	render(requests);
     }
 
 }
