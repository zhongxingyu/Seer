 /*
  * Copyright 2010, 2011 mapsforge.org
  *
  * This program is free software: you can redistribute it and/or modify it under the
  * terms of the GNU Lesser General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.mapsforge.applications.debug.osmosis;
 
 import java.util.HashMap;
 
 import org.mapsforge.poi.PoiCategory;
 
 /**
  * The category resolver delivers a {@link PoiCategory} object for a given osm tag.
  * 
  * @author Karsten Groll
  * 
  */
 public class CategoryResolver {
 
 	private static final HashMap<String, PoiCategory> categoryMap = new HashMap<String, PoiCategory>();
 
 	// See: http://wiki.openstreetmap.org/w/index.php?title=Key:amenity&oldid=608028
 
 	static class Categories {
 		// Root category
 		static final PoiCategory ROOT = new DoubleLinkedPoiCategory("All categories", null);
 		// Amenity
 		static final PoiCategory AMENITY_ROOT = new DoubleLinkedPoiCategory("Amenity root category", ROOT);
 		// Amenity : food
 		static final PoiCategory AMENITY_SUSTENANCE = new DoubleLinkedPoiCategory("Food amenities", AMENITY_ROOT);
 		static final PoiCategory AMENITY_SUSTENANCE_RESTAURANT = new DoubleLinkedPoiCategory("Restaurant", AMENITY_SUSTENANCE);
 		static final PoiCategory AMENITY_SUSTENANCE_FOOD_COURT = new DoubleLinkedPoiCategory(
 				"Area with several different food counters",
 				AMENITY_SUSTENANCE);
 		static final PoiCategory AMENITY_SUSTENANCE_FAST_FOOD = new DoubleLinkedPoiCategory("Fast food restaurant",
 				AMENITY_SUSTENANCE);
 		static final PoiCategory AMENITY_SUSTENANCE_DRINKING_WATER = new DoubleLinkedPoiCategory("A source of drinking water",
 				AMENITY_SUSTENANCE);
 		static final PoiCategory AMENITY_SUSTENANCE_BBQ = new DoubleLinkedPoiCategory(
 				"A public grill for cooking meat or vegetables",
 				AMENITY_SUSTENANCE);
 		static final PoiCategory AMENITY_SUSTENANCE_PUB = new DoubleLinkedPoiCategory(
 				"A place selling alcoholic beverages and sometimes even food",
 				AMENITY_SUSTENANCE);
 		static final PoiCategory AMENITY_SUSTENANCE_BAR = new DoubleLinkedPoiCategory("A place for getting drunk at",
 				AMENITY_SUSTENANCE);
 		static final PoiCategory AMENITY_SUSTENANCE_CAFE = new DoubleLinkedPoiCategory("A cafe", AMENITY_SUSTENANCE);
 		static final PoiCategory AMENITY_SUSTENANCE_BEERGARDEN = new DoubleLinkedPoiCategory("A beer garden.", AMENITY_SUSTENANCE);
 		static final PoiCategory AMENITY_SUSTENANCE_ICE_CREAM = new DoubleLinkedPoiCategory("A shop that sells ice cream.",
 				AMENITY_SUSTENANCE);
 
 		// Amenity : education
 		static final PoiCategory AMENITY_EDUCATION = new DoubleLinkedPoiCategory("Education institutes", AMENITY_ROOT);
 		static final PoiCategory AMENITY_EDUCATION_KINDERGARTEN = new DoubleLinkedPoiCategory("Kindergarten", AMENITY_EDUCATION);
 		static final PoiCategory AMENITY_EDUCATION_SCHOOL = new DoubleLinkedPoiCategory("School grounds", AMENITY_EDUCATION);
 		static final PoiCategory AMENITY_EDUCATION_COLLEGE = new DoubleLinkedPoiCategory("College campus or buildings",
 				AMENITY_EDUCATION);
 		static final PoiCategory AMENITY_EDUCATION_LIBRARY = new DoubleLinkedPoiCategory("Public libraries", AMENITY_EDUCATION);
 		static final PoiCategory AMENITY_EDUCATION_UNIVERSITY = new DoubleLinkedPoiCategory("University campus or buildings",
 				AMENITY_EDUCATION);
 
 		// Amenity : transportation
 		static final PoiCategory AMENITY_TRANSPORTATION = new DoubleLinkedPoiCategory("Transportation", AMENITY_ROOT);
 		static final PoiCategory AMENITY_TRANSPORTATION_FERRY_TERMINAL = new DoubleLinkedPoiCategory(
 				"Places where you can enter leave a ferry", AMENITY_TRANSPORTATION);
 		static final PoiCategory AMENITY_TRANSPORTATION_BICYCLE_PARKING = new DoubleLinkedPoiCategory(
 				"Places to park bicycles on", AMENITY_TRANSPORTATION);
 		static final PoiCategory AMENITY_TRANSPORTATION_BICYCLE_RENTAL = new DoubleLinkedPoiCategory("Bicycle rental stations",
 				AMENITY_TRANSPORTATION);
 		static final PoiCategory AMENITY_TRANSPORTATION_BUS_STATION = new DoubleLinkedPoiCategory("Bus stations",
 				AMENITY_TRANSPORTATION);
 		static final PoiCategory AMENITY_TRANSPORTATION_CAR_RENTAL = new DoubleLinkedPoiCategory("Car rental stations",
 				AMENITY_TRANSPORTATION);
 		static final PoiCategory AMENITY_TRANSPORTATION_CAR_SHARING = new DoubleLinkedPoiCategory("Car sharing stations",
 				AMENITY_TRANSPORTATION);
 		static final PoiCategory AMENITY_TRANSPORTATION_CAR_WASH = new DoubleLinkedPoiCategory("Car washing stations",
 				AMENITY_TRANSPORTATION);
 		static final PoiCategory AMENITY_TRANSPORTATION_FUEL = new DoubleLinkedPoiCategory("Fuel stations",
 				AMENITY_TRANSPORTATION);
 		static final PoiCategory AMENITY_TRANSPORTATION_GRIT_BIN = new DoubleLinkedPoiCategory(
 				"Containers holding a mixture of salt and grit", AMENITY_TRANSPORTATION);
 		static final PoiCategory AMENITY_TRANSPORTATION_PARKING = new DoubleLinkedPoiCategory("Car parks", AMENITY_TRANSPORTATION);
 		static final PoiCategory AMENITY_TRANSPORTATION_PARKING_SPACE = new DoubleLinkedPoiCategory("A single parking space",
 				AMENITY_TRANSPORTATION);
 		static final PoiCategory AMENITY_TRANSPORTATION_PARKING_ENTRANCE = new DoubleLinkedPoiCategory(
 				"Entrances / Exits to underground parking facilities", AMENITY_TRANSPORTATION);
 		static final PoiCategory AMENITY_TRANSPORTATION_TAXI = new DoubleLinkedPoiCategory(
 				"Places where taxis wait for passengers", AMENITY_TRANSPORTATION);
 		static final PoiCategory AMENITY_TRANSPORTATION_EV_CHARGING = new DoubleLinkedPoiCategory(
 				"Electric vehicle charging facilities", AMENITY_TRANSPORTATION);
 
 		// Amenity : financial
 		static final PoiCategory AMENITY_FINANCIAL = new DoubleLinkedPoiCategory("Financial institutes", AMENITY_ROOT);
 		static final PoiCategory AMENITY_FINANCIAL_ATM = new DoubleLinkedPoiCategory("ATMs and cash points", AMENITY_FINANCIAL);
 		static final PoiCategory AMENITY_FINANCIAL_BANK = new DoubleLinkedPoiCategory("Banks", AMENITY_FINANCIAL);
 		static final PoiCategory AMENITY_FINANCIAL_BUREAU_DE_CHANGE = new DoubleLinkedPoiCategory("Currency exchange places",
 				AMENITY_FINANCIAL);
 
 		// Amenity : healthcare
 		static final PoiCategory AMENITY_HEALTHCARE = new DoubleLinkedPoiCategory("Healthcare", AMENITY_ROOT);
 		// TODO dispensing = yes / no
 		static final PoiCategory AMENITY_HEALTHCARE_PHARMACY = new DoubleLinkedPoiCategory("Pharmacies", AMENITY_HEALTHCARE);
 		// TODO emergency = yes / no
 		static final PoiCategory AMENITY_HEALTHCARE_HOSPITAL = new DoubleLinkedPoiCategory("Hospitals", AMENITY_HEALTHCARE);
 		static final PoiCategory AMENITY_HEALTHCARE_BABY_HATCH = new DoubleLinkedPoiCategory("Places to drop babies for remorse",
 				AMENITY_HEALTHCARE);
		static final PoiCategory AMENITY_HEALTHCARE_DENTIST = new DoubleLinkedPoiCategory("Dentists", AMENITY_HEALTHCARE);
 		static final PoiCategory AMENITY_HEALTHCARE_DOCTORS = new DoubleLinkedPoiCategory("Doctors's practises",
 				AMENITY_HEALTHCARE);
 		static final PoiCategory AMENITY_HEALTHCARE_NURSING_HOME = new DoubleLinkedPoiCategory("Nursing homes",
 				AMENITY_HEALTHCARE);
 		static final PoiCategory AMENITY_HEALTHCARE_SOCIAL_FACILITY = new DoubleLinkedPoiCategory("Social Facilities",
 				AMENITY_HEALTHCARE);
 		static final PoiCategory AMENITY_HEALTHCARE_VETERINARY = new DoubleLinkedPoiCategory("Veterinary clinics",
 				AMENITY_HEALTHCARE);
 
 		// Amenity: entertainment (arts & culture)
 		static final PoiCategory AMENITY_ENTERTAINMENT = new DoubleLinkedPoiCategory("Entertainment, arts and culture",
 				AMENITY_ROOT);
 		static final PoiCategory AMENITY_ENTERTAINMENT_ARTS_CENTRE = new DoubleLinkedPoiCategory("Arts centers",
 				AMENITY_ENTERTAINMENT);
 		static final PoiCategory AMENITY_ENTERTAINMENT_CINEMA = new DoubleLinkedPoiCategory("Cinemas", AMENITY_ENTERTAINMENT);
 		static final PoiCategory AMENITY_ENTERTAINMENT_COMMUNITY_CENTRE = new DoubleLinkedPoiCategory("Community centers",
 				AMENITY_ENTERTAINMENT);
 		static final PoiCategory AMENITY_ENTERTAINMENT_SOCIAL_CENTER = new DoubleLinkedPoiCategory("Social centers",
 				AMENITY_ENTERTAINMENT);
 		static final PoiCategory AMENITY_ENTERTAINMENT_FOUNTAIN = new DoubleLinkedPoiCategory("Fountains", AMENITY_ENTERTAINMENT);
 		static final PoiCategory AMENITY_ENTERTAINMENT_NIGHTCLUB = new DoubleLinkedPoiCategory("Nightclubs (Dancing)",
 				AMENITY_ENTERTAINMENT);
 		static final PoiCategory AMENITY_ENTERTAINMENT_STRIPCLUB = new DoubleLinkedPoiCategory("Strip clubs",
 				AMENITY_ENTERTAINMENT);
 		static final PoiCategory AMENITY_ENTERTAINMENT_STUDIO = new DoubleLinkedPoiCategory("TV, radio or recording studios",
 				AMENITY_ENTERTAINMENT);
 		static final PoiCategory AMENITY_ENTERTAINMENT_THEATRE = new DoubleLinkedPoiCategory("Theatres and operas",
 				AMENITY_ENTERTAINMENT);
 
 		// Amenity: others
 		static final PoiCategory AMENITY_OTHER = new DoubleLinkedPoiCategory("Other amenities", AMENITY_ROOT);
 		static final PoiCategory AMENITY_OTHER_BENCH = new DoubleLinkedPoiCategory("Public benches", AMENITY_OTHER);
 		static final PoiCategory AMENITY_OTHER_BROTHEL = new DoubleLinkedPoiCategory("Brothels", AMENITY_OTHER);
 		static final PoiCategory AMENITY_OTHER_CLOCK = new DoubleLinkedPoiCategory("Public visible clocks", AMENITY_OTHER);
 		static final PoiCategory AMENITY_OTHER_COURTHOUSE = new DoubleLinkedPoiCategory("Court houses", AMENITY_OTHER);
 		static final PoiCategory AMENITY_OTHER_CREMATORIUM = new DoubleLinkedPoiCategory("Crematoriums", AMENITY_OTHER);
 		static final PoiCategory AMENITY_OTHER_EMBASSY = new DoubleLinkedPoiCategory("Embassies", AMENITY_OTHER);
 		static final PoiCategory AMENITY_OTHER_FIRE_STATIONS = new DoubleLinkedPoiCategory("Fire stations", AMENITY_OTHER);
 		static final PoiCategory AMENITY_OTHER_GRAVE_YARD = new DoubleLinkedPoiCategory("Grave yards", AMENITY_OTHER);
 		static final PoiCategory AMENITY_OTHER_HUNTING_STAND = new DoubleLinkedPoiCategory("Hunting stands", AMENITY_OTHER);
 		static final PoiCategory AMENITY_OTHER_MARKETPLACE = new DoubleLinkedPoiCategory("Marketplaces", AMENITY_OTHER);
 		static final PoiCategory AMENITY_OTHER_PLACE_OF_WORSHIP = new DoubleLinkedPoiCategory("Churches, mosques, temples",
 				AMENITY_OTHER);
 		static final PoiCategory AMENITY_OTHER_POLICE = new DoubleLinkedPoiCategory("Police stations", AMENITY_OTHER);
 		static final PoiCategory AMENITY_OTHER_POST_BOX = new DoubleLinkedPoiCategory("Post boxes", AMENITY_OTHER);
 		static final PoiCategory AMENITY_OTHER_POST_OFFICE = new DoubleLinkedPoiCategory("Post offices", AMENITY_OTHER);
 		static final PoiCategory AMENITY_OTHER_PRISON = new DoubleLinkedPoiCategory("Prisons (no schools)", AMENITY_OTHER);
 		static final PoiCategory AMENITY_OTHER_PUBLIC_BUILDING = new DoubleLinkedPoiCategory("Public buildings", AMENITY_OTHER);
 		static final PoiCategory AMENITY_OTHER_RECYCLING = new DoubleLinkedPoiCategory("Recycling facilities", AMENITY_OTHER);
 		static final PoiCategory AMENITY_OTHER_SAUNA = new DoubleLinkedPoiCategory("Saunas", AMENITY_OTHER);
 		static final PoiCategory AMENITY_OTHER_SHELTER = new DoubleLinkedPoiCategory("Bad weather shelters", AMENITY_OTHER);
 		static final PoiCategory AMENITY_OTHER_TELEPHONE = new DoubleLinkedPoiCategory("Public telephones", AMENITY_OTHER);
 		static final PoiCategory AMENITY_OTHER_TOILETS = new DoubleLinkedPoiCategory("Public toilets", AMENITY_OTHER);
 		static final PoiCategory AMENITY_OTHER_TOWNHALL = new DoubleLinkedPoiCategory("Town hall buildings", AMENITY_OTHER);
 		static final PoiCategory AMENITY_OTHER_VENDING_MACHINE = new DoubleLinkedPoiCategory("Vending machines", AMENITY_OTHER);
 		static final PoiCategory AMENITY_OTHER_WASTE_BASKET = new DoubleLinkedPoiCategory("Garbage cans", AMENITY_OTHER);
 		static final PoiCategory AMENITY_OTHER_WASTE_DISPOSAL = new DoubleLinkedPoiCategory("Garbage containers", AMENITY_OTHER);
 		static final PoiCategory AMENITY_OTHER_WATERING_PLACE = new DoubleLinkedPoiCategory("Water places for animals",
 				AMENITY_OTHER);
 
 		// TODO non-amenity categories
 	}
 
 	static {
 
 		//
 		// Mappings
 		//
 
 		// Root category
 		categoryMap.put("*", Categories.ROOT);
 
 		// Amenity
 		categoryMap.put("amenity=*", Categories.AMENITY_ROOT);
 		categoryMap.put("amenity=", Categories.AMENITY_ROOT);
 
 		// Amenity : food
 		categoryMap.put("amenity=[food]", Categories.AMENITY_SUSTENANCE);
 		categoryMap.put("amenity=[sustenance]", Categories.AMENITY_SUSTENANCE);
 
 		categoryMap.put("amenity=restaurant", Categories.AMENITY_SUSTENANCE_RESTAURANT);
 		categoryMap.put("amenity=food_court", Categories.AMENITY_SUSTENANCE_FOOD_COURT);
 		categoryMap.put("amenity=fast_food", Categories.AMENITY_SUSTENANCE_FAST_FOOD);
 		categoryMap.put("amenity=drinking_water", Categories.AMENITY_SUSTENANCE_DRINKING_WATER);
 		categoryMap.put("amenity=bbq", Categories.AMENITY_SUSTENANCE_BBQ);
 		categoryMap.put("amenity=pub", Categories.AMENITY_SUSTENANCE_PUB);
 		categoryMap.put("amenity=bar", Categories.AMENITY_SUSTENANCE_BAR);
 		categoryMap.put("amenity=cafe", Categories.AMENITY_SUSTENANCE_CAFE);
 		categoryMap.put("amenity=biergarten", Categories.AMENITY_SUSTENANCE_BEERGARDEN);
 		categoryMap.put("amenity=ice_cream", Categories.AMENITY_SUSTENANCE_ICE_CREAM);
 		categoryMap.put("shop=ice_cream", Categories.AMENITY_SUSTENANCE_ICE_CREAM);
 
 		// Amenity: education
 		categoryMap.put("amenity=[education]", Categories.AMENITY_EDUCATION);
 
 		categoryMap.put("amenity=kindergarten", Categories.AMENITY_EDUCATION_KINDERGARTEN);
 		categoryMap.put("amenity=school", Categories.AMENITY_EDUCATION_SCHOOL);
 		categoryMap.put("amenity=college", Categories.AMENITY_EDUCATION_COLLEGE);
 		categoryMap.put("amenity=library", Categories.AMENITY_EDUCATION_LIBRARY);
 		categoryMap.put("amenity=university", Categories.AMENITY_EDUCATION_UNIVERSITY);
 
 		// Amenity: transportation
 		categoryMap.put("amenity=[transportation]", Categories.AMENITY_TRANSPORTATION);
 
 		categoryMap.put("amenity=ferry_terminal", Categories.AMENITY_TRANSPORTATION_FERRY_TERMINAL);
 		categoryMap.put("amenity=bicycle_parking", Categories.AMENITY_TRANSPORTATION_BICYCLE_PARKING);
 		categoryMap.put("amenity=bicycle_rental", Categories.AMENITY_TRANSPORTATION_BICYCLE_RENTAL);
 		categoryMap.put("amenity=bus_station", Categories.AMENITY_TRANSPORTATION_BUS_STATION);
 		categoryMap.put("amenity=car_rental", Categories.AMENITY_TRANSPORTATION_CAR_RENTAL);
 		categoryMap.put("amenity=car_sharing", Categories.AMENITY_TRANSPORTATION_CAR_SHARING);
 		categoryMap.put("amenity=car_wash", Categories.AMENITY_TRANSPORTATION_CAR_WASH);
 		categoryMap.put("amenity=fuel", Categories.AMENITY_TRANSPORTATION_FUEL);
 		categoryMap.put("amenity=grit_bin", Categories.AMENITY_TRANSPORTATION_GRIT_BIN);
 		categoryMap.put("amenity=parking", Categories.AMENITY_TRANSPORTATION_PARKING);
 		categoryMap.put("amenity=parking_space", Categories.AMENITY_TRANSPORTATION_PARKING_SPACE);
 		categoryMap.put("amenity=parking_entrance", Categories.AMENITY_TRANSPORTATION_PARKING_ENTRANCE);
 		categoryMap.put("amenity=taxi", Categories.AMENITY_TRANSPORTATION_TAXI);
 		categoryMap.put("amenity=ev_charging", Categories.AMENITY_TRANSPORTATION_EV_CHARGING);
 
 		// Amenity: financial
 		categoryMap.put("amenity=[financial]", Categories.AMENITY_FINANCIAL);
 		categoryMap.put("amenity=atm", Categories.AMENITY_FINANCIAL_ATM);
 		categoryMap.put("amenity=bank", Categories.AMENITY_FINANCIAL_BANK);
 		categoryMap.put("amenity=bureau_de_change", Categories.AMENITY_FINANCIAL_BUREAU_DE_CHANGE);
 
 		// Amenity: healthcare
 		categoryMap.put("amenity=[healthcare]", Categories.AMENITY_HEALTHCARE);
 		categoryMap.put("amenity=pharmacy", Categories.AMENITY_HEALTHCARE_PHARMACY);
 		categoryMap.put("amenity=hospital", Categories.AMENITY_HEALTHCARE_HOSPITAL);
 		categoryMap.put("amenity=baby_hatch", Categories.AMENITY_HEALTHCARE_BABY_HATCH);
 		categoryMap.put("amenity=dentist", Categories.AMENITY_HEALTHCARE_DENTIST);
 		categoryMap.put("amenity=doctors", Categories.AMENITY_HEALTHCARE_DOCTORS);
 		categoryMap.put("amenity=nursing_home", Categories.AMENITY_HEALTHCARE_NURSING_HOME);
 		categoryMap.put("amenity=social_facility", Categories.AMENITY_HEALTHCARE_SOCIAL_FACILITY);
 		categoryMap.put("amenity=veterinary", Categories.AMENITY_HEALTHCARE_VETERINARY);
 
 		// Amenity: entertainment (arts & culture)
 		categoryMap.put("amenity=[entertainment]", Categories.AMENITY_ENTERTAINMENT);
 		categoryMap.put("amenity=[arts]", Categories.AMENITY_ENTERTAINMENT);
 		categoryMap.put("amenity=[culture]", Categories.AMENITY_ENTERTAINMENT);
 
 		categoryMap.put("amenity=arts_centre", Categories.AMENITY_ENTERTAINMENT_ARTS_CENTRE);
 		categoryMap.put("amenity=cinema", Categories.AMENITY_ENTERTAINMENT_CINEMA);
 		categoryMap.put("amenity=community_centre", Categories.AMENITY_ENTERTAINMENT_COMMUNITY_CENTRE);
 		categoryMap.put("amenity=social_centre", Categories.AMENITY_ENTERTAINMENT_SOCIAL_CENTER);
 		categoryMap.put("amenity=fountain", Categories.AMENITY_ENTERTAINMENT_FOUNTAIN);
 		categoryMap.put("amenity=nightclub", Categories.AMENITY_ENTERTAINMENT_NIGHTCLUB);
 		categoryMap.put("amenity=stripclub", Categories.AMENITY_ENTERTAINMENT_STRIPCLUB);
 		categoryMap.put("amenity=studio", Categories.AMENITY_ENTERTAINMENT_STUDIO);
 		categoryMap.put("amenity=theatre", Categories.AMENITY_ENTERTAINMENT_THEATRE);
 
 		// Amenity: others
 		categoryMap.put("amenity=[others]", Categories.AMENITY_OTHER);
 		categoryMap.put("amenity=[other]", Categories.AMENITY_OTHER);
 
 		categoryMap.put("amenity=bench", Categories.AMENITY_OTHER_BENCH);
 		categoryMap.put("amenity=brothel", Categories.AMENITY_OTHER_BROTHEL);
 		categoryMap.put("amenity=clock", Categories.AMENITY_OTHER_CLOCK);
 		categoryMap.put("amenity=courthouse", Categories.AMENITY_OTHER_COURTHOUSE);
 		categoryMap.put("amenity=crematorium", Categories.AMENITY_OTHER_CREMATORIUM);
 		categoryMap.put("amenity=embassy", Categories.AMENITY_OTHER_EMBASSY);
 		categoryMap.put("amenity=fire_station", Categories.AMENITY_OTHER_FIRE_STATIONS);
 		categoryMap.put("amenity=grave_yard", Categories.AMENITY_OTHER_GRAVE_YARD);
 		categoryMap.put("amenity=hunting_stand", Categories.AMENITY_OTHER_HUNTING_STAND);
 		categoryMap.put("amenity=market_place", Categories.AMENITY_OTHER_MARKETPLACE);
 		categoryMap.put("amenity=place_of_worship", Categories.AMENITY_OTHER_PLACE_OF_WORSHIP);
 		categoryMap.put("amenity=police", Categories.AMENITY_OTHER_POLICE);
 		categoryMap.put("amenity=post_box", Categories.AMENITY_OTHER_POST_BOX);
 		categoryMap.put("amenity=post_office", Categories.AMENITY_OTHER_POST_OFFICE);
 		categoryMap.put("amenity=prison", Categories.AMENITY_OTHER_PRISON);
 		categoryMap.put("amenity=public_building", Categories.AMENITY_OTHER_PUBLIC_BUILDING);
 		categoryMap.put("amenity=recycling", Categories.AMENITY_OTHER_RECYCLING);
 		categoryMap.put("amenity=sauna", Categories.AMENITY_OTHER_SAUNA);
 		categoryMap.put("amenity=shelter", Categories.AMENITY_OTHER_SHELTER);
 		categoryMap.put("amenity=telephone", Categories.AMENITY_OTHER_TELEPHONE);
 		categoryMap.put("amenity=toilets", Categories.AMENITY_OTHER_TOILETS);
 		categoryMap.put("amenity=townhall", Categories.AMENITY_OTHER_TOWNHALL);
 		categoryMap.put("amenity=vending_machine", Categories.AMENITY_OTHER_VENDING_MACHINE);
 		categoryMap.put("amenity=waste_basket", Categories.AMENITY_OTHER_WASTE_BASKET);
 		categoryMap.put("amenity=waste_disposal", Categories.AMENITY_OTHER_WASTE_DISPOSAL);
 		categoryMap.put("amenity=watering_place", Categories.AMENITY_OTHER_WATERING_PLACE);
 
 		// Calculate IDs
 		DoubleLinkedPoiCategory.calculateCategoryIDs((DoubleLinkedPoiCategory) Categories.ROOT, 0);
 
 		// Create image for category graph for debugging purposes
 		System.out.println(DoubleLinkedPoiCategory.getGraphVizString((DoubleLinkedPoiCategory) Categories.ROOT));
 	}
 
 	/**
 	 * 
 	 * @param key
 	 *            The tags key (e.g. 'amenity')
 	 * @param value
 	 *            The tags value (e.g. 'fuel');
 	 * @return The submost category for a given tag.
 	 * @throws UnknownCategoryException
 	 *             when there is no matching category.
 	 */
 	static PoiCategory getPoiCategoryByTag(String key, String value) throws UnknownCategoryException {
 		if (!categoryMap.containsKey(key + "=" + value)) {
 			throw new UnknownCategoryException();
 		}
 
 		return categoryMap.get(key + "=" + value);
 	}
 
 	static PoiCategory getPoiCategoryByTag(String tag) throws UnknownCategoryException {
 		String key = tag.split("=")[0];
 		String value = tag.split("=")[1];
 		return getPoiCategoryByTag(key, value);
 	}
 }
