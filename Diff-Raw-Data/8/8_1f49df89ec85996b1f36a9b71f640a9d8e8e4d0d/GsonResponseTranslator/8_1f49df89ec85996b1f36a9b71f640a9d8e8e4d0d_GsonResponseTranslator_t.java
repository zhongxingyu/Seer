 /**
  * Copyright (c) 2010, Todd Ginsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  *    * Redistributions of source code must retain the above copyright
  *      notice, this list of conditions and the following disclaimer.
  *    * Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    * Neither the name of Todd Ginsberg, or Gowalla nor the
  *      names of any contributors may be used to endorse or promote products
  *      derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
  *  Also, please use this for Good and not Evil.  
  */
 package com.ginsberg.gowalla.request.translate;
 
 import java.lang.reflect.Type;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
import java.util.Calendar;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
import java.util.SimpleTimeZone;
 
 import com.ginsberg.gowalla.dto.Address;
 import com.ginsberg.gowalla.dto.Category;
 import com.ginsberg.gowalla.dto.FullCategory;
 import com.ginsberg.gowalla.dto.FullSpot;
 import com.ginsberg.gowalla.dto.FullUser;
 import com.ginsberg.gowalla.dto.Id;
 import com.ginsberg.gowalla.dto.Identity;
 import com.ginsberg.gowalla.dto.Item;
 import com.ginsberg.gowalla.dto.ItemEvent;
 import com.ginsberg.gowalla.dto.LocatedSpot;
 import com.ginsberg.gowalla.dto.Pin;
 import com.ginsberg.gowalla.dto.SimpleSpot;
 import com.ginsberg.gowalla.dto.SpotEvent;
 import com.ginsberg.gowalla.dto.SpotPhoto;
 import com.ginsberg.gowalla.dto.SpotVisitor;
 import com.ginsberg.gowalla.dto.Stamp;
 import com.ginsberg.gowalla.dto.Trip;
 import com.ginsberg.gowalla.dto.TripSummary;
 import com.ginsberg.gowalla.dto.User;
 import com.ginsberg.gowalla.dto.UserEvent;
 import com.ginsberg.gowalla.dto.UserPhoto;
 import com.ginsberg.gowalla.dto.VisitedSpot;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonDeserializationContext;
 import com.google.gson.JsonDeserializer;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonParseException;
 
 import static com.ginsberg.gowalla.util.Strings.toId;
 
 /**
  * Translates API Responses using the GSON library.  This library was 
  * used because it does a good job of mapping fields, and lets me easily
  * override the Date format, which the basic Java date formatter doesn't
  * natively support without a bit of help.
  * 
  * TODO: Set a FieldNamingStrategy on GsonBuilder, remove underscores in DTO
  * object fields in favor of standard Java naming convention?
  * 
  * TODO: Remove static inner classes and replace with TypeAdapters?
  * 
  * @author Todd Ginsberg
  */
 public class GsonResponseTranslator implements ResponseTranslator {
 
 	private Gson gson = null;
 	
 	/**
 	 * Construct an instance.  This implementation sets the GsonBuilder.
 	 */
 	public GsonResponseTranslator() {
 		super();
 		gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
 			// Damn you to heck, SimpleDateFormat. 
 			public Date deserialize(JsonElement arg0, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
 				String date = arg0.getAsString();
 				date = date.replaceAll("/", "-");
 				date = date.replaceFirst("\\s", "T");
 				date = date.replaceAll(" ", "");
 				date = date.replaceAll("\\+", "GMT\\+");
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
				Calendar cal = Calendar.getInstance(new SimpleTimeZone(0, "GMT"));
		        df.setCalendar(cal);
 				try {
 					return df.parse(date);
 				} catch (ParseException e) {
 					throw new JsonParseException("Cannot parse date: " + e.getMessage(), e);
 				}
 			}
 			
 		}).create();
 	}
 	
 
 	/**
 	 * @see com.ginsberg.gowalla.request.translate.ResponseTranslator#translateCategories(java.lang.String)
 	 */
 	@Override
 	public List<FullCategory> translateCategories(final String response) {
 		final FullCategory topCategory = gson.fromJson(response, FullCategory.class);
 		final List<FullCategory> categories = topCategory.getSubcategories();
 		
 		for(FullCategory category : categories) {
 			fixCategoryId(category);
 		}
 		return categories;
 	}
 	
 	private void fixCategoryId(final FullCategory category) {
 		fixId(category);
 		for(FullCategory c : category.getSubcategories()) {
 			fixCategoryId(c);
 		}
 	}
 
 	/**
 	 * @see com.ginsberg.gowalla.request.translate.ResponseTranslator#translateCategory(java.lang.String)
 	 */
 	@Override
 	public FullCategory translateCategory(final String response)  {
 		final FullCategory category = gson.fromJson(response, FullCategory.class);
 		fixId(category);
 		return category;
 	}
 	
 	/**
 	 * @see com.ginsberg.gowalla.request.translate.ResponseTranslator#translateSpot(java.lang.String, int)
 	 */
 	@Override
 	public FullSpot translateSpot(final String response, final int id) {
 		final FullSpot spot = gson.fromJson(response, FullSpot.class);
 		
 		// The things I do for you...
 		fixId(spot);
 		fixId(spot.getCreator());
 		for(User f : spot.getFounders()) {
 			fixId(f);
 		}		
 		for(SpotVisitor v : spot.getTop10()) {
 			fixId(v);
 		}
 		for(Category c : spot.getCategories()) {
 			fixId(c);
 		}
 		spot.setMerged(id != spot.getId());
 		return spot;
 	}
 
 	@Override
 	public List<Item> translateItems(final String response) {
 		final List<Item> items = gson.fromJson(response, ItemsContainer.class).items;
 		for(Item item : items) {
 			fixId(item);
 		}
 		return items;
 	}
 
 	@Override
 	public Item translateItem(final String response) {
 		final Item item = gson.fromJson(response, Item.class);
 		fixId(item);
 		return item;
 	}
 
 	@Override
 	public List<SimpleSpot> translateSimpleSpots(String response) {
 		final List<SimpleSpot> spots = gson.fromJson(response, SimpleSpotsContainer.class).spots;
 		for(SimpleSpot spot : spots) {
 			fixId(spot);
 		}
 		return spots;
 	}
 	
 	@Override
 	public Trip translateTrip(final String response) {
 		final Trip trip = gson.fromJson(response, Trip.class);
 		fixId(trip);
 		fixId(trip.getCreator());
 		for(LocatedSpot spot : trip.getSpots()) {
 			fixId(spot);
 		}
 		return trip;
 	}
 	
 	@Override
 	public List<TripSummary> translateUserCreatedTrips(final String response) {
 		final List<TripSummary> trips = gson.fromJson(response, TripSummaryContainer.class).trips;
 		for(TripSummary trip : trips) {
 			fixId(trip);
 		}
 		return trips;
 	}
 	
 	@Override
 	public List<Pin> translateUserPins(final String response) {
 		final List<Pin> pins = gson.fromJson(response, PinsContainer.class).pins;
 		for(Pin pin : pins) {
 			fixId(pin.getTrip());
 			fixId(pin);
 		}
 		return pins;
 	}
 
 	@Override
 	public FullUser translateUser(final String response) {
 		final FullUser user = gson.fromJson(response, FullUser.class);
 		fixId(user);
 		for(UserEvent e : user.getLastCheckins()) {
 			fixId(e.getSpot());
 		}
 		return user;
 	}
 	
 	public List<User> translateUsers(final String response) {
 		final List<User> users = gson.fromJson(response, UsersContainer.class).users;
 		for(User user : users) {
 			fixId(user);
 		}
 		return users;
 	}
 
 	@Override
 	public List<VisitedSpot> translateVisitedSpots(final String response) {
 		final List<VisitedSpot> spots = gson.fromJson(response, VisitedSpotsContainer.class).top_spots;
 		for(VisitedSpot s : spots) {
 			fixId(s);
 		}
 		return spots;
 	}
 
 	@Override
 	public List<SpotEvent> translateSpotEvents(String response) {
 		final List<SpotEvent> events = gson.fromJson(response, SpotEventsContainer.class).activity;
 		for(SpotEvent e : events) {
 			fixId(e.getUser());
 		}
 		return events;
 	}
 	
 	@Override
 	public List<ItemEvent> translateItemEvents(String response) {
 		final List<ItemEvent> events = gson.fromJson(response, ItemEventsContainer.class).events;
 		 for(ItemEvent e : events) {
 			 fixId(e.getSpot());
 			 fixId(e.getUser());
 		 }
 		return events;
 	}
 	
 	@Override
 	public List<SpotPhoto> translateSpotPhotos(String response) {
 		final List<SpotPhoto> events = gson.fromJson(response, SpotPhotosContainer.class).activity;
 		for(SpotPhoto photo : events) {
 			fixId(photo.getUser());
 		}
 		return events;
 	}
 	
 	@Override
 	public List<UserPhoto> translateUserPhotos(String response) {
 		final List<UserPhoto> events = gson.fromJson(response, UserPhotosContainer.class).activity;
 		for(UserPhoto photo : events) {
 			fixId(photo.getSpot());
 		}
 		return events;
 	}
 	
 	@Override
 	public List<TripSummary> translateTripSummaries(String response) {
 		final List<TripSummary> trips = gson.fromJson(response, TripSummaryContainer.class).trips;
 		for(TripSummary t : trips) {
 			fixId(t);
 			for(Identity<FullSpot> i : t.getSpots()) {
 				fixId(i);
 			}
 		}
 		return trips;
 	}
 
 	@Override
 	public List<Stamp> translateStamps(String response) {
 		final List<ContainedStamp> containedStamps = gson.fromJson(response, StampContainer.class).stamps;
 		final List<Stamp> stamps = new LinkedList<Stamp>();
 		// Sorry for this, but I want the Stamp object to be flatter
 		// than Gowalla is returning it.  Thus, the hoop jumping.
 		for(ContainedStamp cs : containedStamps) {
 			final Stamp s = new Stamp();
 			s.setCheckinsCount(cs.checkins_count);
 			s.setFirstCheckinAt(cs.first_checkin_at);
 			s.setLastCheckinAt(cs.last_checkin_at);
 			s.setImageUrl(cs.spot.image_url);
 			s.setName(cs.spot.name);
 			s.setUrl(cs.spot.url);
 			s.setAddress(cs.spot.address);
 			fixId(s);
 			stamps.add(s);
 		}
 		return stamps;
 	}
 
 	/**
 	 * For ID objects, fix the ids by picking them off the end of the url.
 	 */
 	private <T> void fixId(Id<T> object) {
 		if(object != null) {
 			object.setId(toId(object.getUrl()));
 		}
 	}
 	
 	/**
 	 * I only want the insides of this part.
 	 */
 	private static class SimpleSpotsContainer {
 		List<SimpleSpot> spots;
 		// Group ignored for now.
 	}	
 	
 	/**
 	 * I only want the insides of this part.
 	 */
 	private static class ItemsContainer {
 		List<Item> items;
 	}
 	
 	/**
 	 * I only want the insides of this part.
 	 */
 	private static class PinsContainer {
 		List<Pin> pins;
 	}
 	
 	/**
 	 * I only want the insides of this part.
 	 */
 	private static class UsersContainer {
 		List<User> users;
 	}
 	
 	/**
 	 * I only want the insides of this part.
 	 */
 	private static class TripSummaryContainer {
 		List<TripSummary> trips;
 	}
 	
 	/**
 	 * I only want the insides of this part.
 	 */
 	private static class SpotEventsContainer {
 		List<SpotEvent> activity;
 	}
 	
 	/**
 	 * I only want the insides of this part.
 	 */
 	private static class SpotPhotosContainer {
 		List<SpotPhoto> activity;
 	}
 	
 	/**
 	 * I only want the insides of this part.
 	 */
 	private static class UserPhotosContainer {
 		List<UserPhoto> activity;
 	}
 	
 	/**
 	 * I only want the insides of this part.
 	 */
 	private static class ItemEventsContainer {
 		List<ItemEvent> events;
 	}
 	
 	/**
 	 * I only want the insides of this part.
 	 */
 	private static class VisitedSpotsContainer {
 		List<VisitedSpot> top_spots;
 	}
 	
 	/**
 	 * I don't like the way Stamps are returned from the server, so I'm going 
 	 * to define this to give GSON something to dump it into directly.  Then
 	 * I can manipulate it the way I want to later.
 	 */
 	private static class ContainedStamp {
 		 int checkins_count;
 	     Date first_checkin_at;
 	     Date last_checkin_at;
 	     StampSpot spot;
 	     
 	}
 	
 	/**
 	 * Used in StampContainer
 	 */
 	private static class StampSpot {
 		String name;
 		Address address;
 		String url;
 		String image_url;
 	}
 	
 	private static class StampContainer {
 		List<ContainedStamp> stamps;
 	}
 	
 	
 }
