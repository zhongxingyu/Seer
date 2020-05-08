 package br.com.findplaces.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import br.com.findplaces.jpa.entity.Coment;
 import br.com.findplaces.jpa.entity.Facilities;
 import br.com.findplaces.jpa.entity.Image;
 import br.com.findplaces.jpa.entity.Place;
 import br.com.findplaces.jpa.entity.PlaceType;
 import br.com.findplaces.jpa.entity.SellType;
 import br.com.findplaces.jpa.entity.Seller;
 import br.com.findplaces.jpa.entity.User;
 import br.com.findplaces.jpa.entity.UserType;
 import br.com.findplaces.jpa.entity.geographic.City;
 import br.com.findplaces.jpa.entity.geographic.Country;
 import br.com.findplaces.jpa.entity.geographic.Neighborhood;
 import br.com.findplaces.jpa.entity.geographic.Region;
 import br.com.findplaces.jpa.entity.geographic.Street;
 import br.com.findplaces.jpa.entity.spatial.PlaceSpatial;
 import br.com.findplaces.model.geographic.to.CityTO;
 import br.com.findplaces.model.geographic.to.CountryTO;
 import br.com.findplaces.model.geographic.to.NeighborhoodTO;
 import br.com.findplaces.model.geographic.to.RegionTO;
 import br.com.findplaces.model.geographic.to.StreetTO;
 import br.com.findplaces.model.spatial.to.PlaceSpatialTO;
 import br.com.findplaces.model.to.ComentTO;
 import br.com.findplaces.model.to.FacilitiesTO;
 import br.com.findplaces.model.to.PhotoTO;
 import br.com.findplaces.model.to.PlaceRequest;
 import br.com.findplaces.model.to.PlaceTO;
 import br.com.findplaces.model.to.PlaceTypeTO;
 import br.com.findplaces.model.to.SellerTO;
 import br.com.findplaces.model.to.UserTO;
 import br.com.findplaces.model.to.UserTypeTO;
 
 import com.restfb.types.Photo;
 
 public class ConverterTO {
 
 	private ConverterTO() {
 	}
 
 	public static UserTO converter(User user) {
 		UserTO userTO = new UserTO();
 		userTO.setId(user.getId());
 		userTO.setName(user.getName());
 		userTO.setEmail(user.getEmail());
 		userTO.setPassword(user.getPassword());
 		userTO.setSocialID(user.getSocialID());
 		userTO.setType(converter(user.getType()));
 		userTO.setRelationship(user.getRelationship());
 		userTO.setAge(user.getAge());
 		userTO.setStudyAt(user.getStudyAt());
 		return userTO;
 	}
 
 	public static UserTypeTO converter(UserType userType) {
 		UserTypeTO userTypeTO = new UserTypeTO();
 		userTypeTO.setId(userType.getId());
 		userTypeTO.setName(userType.getName());
 		return userTypeTO;
 	}
 
 	public static User converter(UserTO userTO) {
 		User user = new User();
 		user.setName(userTO.getName());
 		user.setId(userTO.getId());
 		user.setEmail(userTO.getEmail());
 		user.setPassword(userTO.getPassword());
 		user.setSocialID(userTO.getSocialID());
 		user.setType(converter(userTO.getType()));
 		user.setRelationship(userTO.getRelationship());
 		user.setAge(userTO.getAge());
 		user.setStudyAt(userTO.getStudyAt());
 		return user;
 	}
 
 	public static UserType converter(UserTypeTO userTypeTO) {
 		UserType userType = new UserType();
 		userType.setId(userTypeTO.getId());
 		userType.setName(userTypeTO.getName());
 		return userType;
 	}
 
 	public static Seller converter(SellerTO sellerTO) {
 		Seller seller = new Seller();
 		seller.setId(sellerTO.getId());
 
 		if (sellerTO.getCity() != null) {
 			seller.setCity(sellerTO.getCity());
 		}
 
 		if (sellerTO.getCountry() != null) {
 			seller.setCountry(sellerTO.getCountry());
 		}
 
 		if (sellerTO.getLatitude() != null) {
 			seller.setLatitude(sellerTO.getLatitude());
 		}
 
 		if (sellerTO.getLatitude() != null) {
 			seller.setLatitude(sellerTO.getLatitude());
 		}
 
 		if (sellerTO.getLongitude() != null) {
 			seller.setLongitude(sellerTO.getLongitude());
 		}
 
 		if (sellerTO.getName() != null) {
 			seller.setName(sellerTO.getName());
 		}
 
 		if (sellerTO.getWebsite() != null) {
 			seller.setState(sellerTO.getWebsite());
 		}
 
 		if (sellerTO.getUserTO() != null) {
 			seller.setUser(ConverterTO.converter(sellerTO.getUserTO()));
 		}
 
 		if (sellerTO.getWebsite() != null) {
 			seller.setWebsite(sellerTO.getWebsite());
 		}
 
 		return seller;
 	}
 
 	public static SellerTO converter(Seller seller) {
 		SellerTO sellerTO = new SellerTO();
 		sellerTO.setCity(seller.getCity());
 		sellerTO.setCountry(seller.getCountry());
 		sellerTO.setId(seller.getId());
 		sellerTO.setLatitude(seller.getLatitude());
 		sellerTO.setLongitude(seller.getLongitude());
 		sellerTO.setName(seller.getName());
 		sellerTO.setState(seller.getWebsite());
 		if (seller.getUser() != null) {
 			sellerTO.setUserTO(ConverterTO.converter(seller.getUser()));
 		}
 		sellerTO.setWebsite(seller.getWebsite());
 		return sellerTO;
 	}
 
 	public static Place converter(PlaceTO place) {
 		Place entity = new Place();
 		entity.setId(place.getId());
 		entity.setAddress(place.getAddress());
 		entity.setBathroom(place.getBathroom());
 		entity.setBedroom(place.getBedroom());
 		// entity.setCity(converter(place.getCity()));
 		entity.setCode(place.getCode());
 		entity.setDescription(place.getDescription());
 		entity.setGarage(place.getGarage());
 		entity.setM2(place.getM2());
 		// entity.setNeighborhood(converter(place.getNeighborhood()));
 		entity.setPrice(place.getPrice());
 		entity.setRoom(place.getRoom());
 		entity.setSeller(converter(place.getSeller()));
 		// entity.setStreet(converter(place.getStreet()));
 		entity.setSuite(place.getSuite());
 		entity.setDeposit(place.getDeposit());
 		entity.setCellphone(place.getCellphone());
 		entity.setCellphone2(place.getCellphone2());
 		// entity.setCellphone3(place.getCellphone3());
 		// entity.setInternet(place.getInternet());
 		entity.setCondominiumPrice(place.getCondominiumPrice());
 		entity.setQtdPlaceFloor(place.getQtdPlaceFloor());
 		entity.setRentMonths(place.getRentMonths());
 		// entity.setTv(place.getTv());
 		entity.setTotalPrice(place.getTotalPrice());
 		
 		entity.setRent(place.getRent());
 		entity.setContract_time(place.getContract_time());
 		entity.setTv(place.getTv());
 		entity.setInternet(place.getInternet());
 		entity.setCondominiumPrice(place.getCondominiumPrice());
 		
 		if (place.getSpatialTO() != null) {
 			entity.setSpatial(converter(place.getSpatialTO()));
 		}
 		
 		if(place.getFacilities() != null){
 			entity.setFacilities(converter(place.getFacilities()));
 		}
 		entity.setType(converter(place.getType()));
 		List<Coment> coments = new ArrayList<Coment>();
 		if (place.getComents() != null) {
 			for (ComentTO coment : place.getComents()) {
 				coments.add(converter(coment));
 			}
 			entity.setComents(coments);
 		}
 		List<Image> photos = new ArrayList<Image>();
 		if (place.getIdImages() != null) {
 			for (Long photoID : place.getIdImages()) {
 				Image photo = new Image();
 				photo.setId(photoID);
 			}
 			entity.setPhotos(photos);
 		}
 
 		return entity;
 	}
 
 	public static PlaceTO converter(Place place) {
 		PlaceTO to = new PlaceTO();
 		to.setId(place.getId());
 		to.setAddress(place.getAddress());
 		to.setBathroom(place.getBathroom());
 		to.setBedroom(place.getBedroom());
 		// to.setCity(converter(place.getCity()));
 		to.setCode(place.getCode());
 		to.setDescription(place.getDescription());
 		to.setGarage(place.getGarage());
 		to.setM2(place.getM2());
 		// to.setNeighborhood(converter(place.getNeighborhood()));
 		to.setPrice(place.getPrice());
 		if (place.getSpatial() != null) {
 			to.setLat(place.getSpatial().getGeom().getCoordinate().x);
 			to.setLng(place.getSpatial().getGeom().getCoordinate().y);
 		}
 		to.setRoom(place.getRoom());
 		to.setSeller(converter(place.getSeller()));
 		// to.setStreet(converter(place.getStreet()));
 		to.setSuite(place.getSuite());
 		to.setDeposit(place.getDeposit());
 		to.setCellphone(place.getCellphone());
 		to.setCellphone2(place.getCellphone2());
 		// to.setCellphone3(place.getCellphone3());
 		// to.setInternet(place.getInternet());
 		to.setCondominiumPrice(place.getCondominiumPrice());
 		to.setQtdPlaceFloor(place.getQtdPlaceFloor());
 		to.setRentMonths(place.getRentMonths());
 		// to.setTv(place.getTv());
 		to.setTotalPrice(place.getTotalPrice());
 		to.setType(converter(place.getType()));
 		ArrayList<Long> sellType = new ArrayList<Long>();
 //		if (place.getSellType() != null) {
 //			for (SellType sell : place.getSellType()) {
 //				sellType.add(sell.getId());
 //			}
 //			to.setSellType(sellType);
 //		} // fixme
 
 		List<ComentTO> coments = new ArrayList<ComentTO>();
 		if (place.getComents() != null) {
 			for (Coment coment : place.getComents()) {
 				coments.add(converter(coment));
 			}
 			to.setComents(coments);
 		}
 		//Geometry cant be convert to json, and anottations are not working
 		//for know, let`s keep this out from the PlaceTO
 		if (place.getSpatial() != null) {
 //			to.setSpatialTO(converter(place.getSpatial()));
 		}
 		// SPATIAL
 		return to;
 	}
 
 	private static PlaceSpatialTO converter(PlaceSpatial spatial) {
 		PlaceSpatialTO to = new PlaceSpatialTO();
 		to.setId(spatial.getId());
		to.setGeom(null);
 		return to;
 	}
 
 	public static ComentTO converter(Coment coment) {
 		ComentTO to = new ComentTO();
 		to.setId(coment.getId());
 		to.setAnswer(converter(coment.getAnswer()));
 		to.setUser(converter(coment.getUser()));
 		to.setText(coment.getText());
 		to.setStatus(coment.getStatus());
 		return to;
 	}
 
 	public static Coment converter(ComentTO to) {
 		Coment coment = new Coment();
 		coment.setAnswer(converter(to.getAnswer()));
 		coment.setId(to.getId());
 		coment.setUser(converter(to.getUser()));
 		coment.setStatus(to.getStatus());
 		return coment;
 	}
 
 	public static PlaceSpatial converter(PlaceSpatialTO to) {
 		PlaceSpatial entity = new PlaceSpatial();
 		entity.setId(to.getId());
 		// entity.setPlace(converter(to.getPlace()));
		entity.setGeom(to.getGeom());
 		return entity;
 	}
 
 	public static PlaceType converter(PlaceTypeTO type) {
 		PlaceType entity = new PlaceType();
 		entity.setId(type.getId());
 		entity.setName(type.getName());
 		return entity;
 	}
 
 	public static Country converter(CountryTO country) {
 		Country entity = new Country();
 		entity.setId(country.getId());
 		entity.setName(country.getName());
 		return entity;
 	}
 
 	public static Region converter(RegionTO region) {
 		Region entity = new Region();
 		entity.setId(region.getId());
 		entity.setName(region.getName());
 		entity.setAlias(region.getAlias());
 		entity.setCountry(converter(region.getCountry()));
 		return entity;
 	}
 
 	public static City converter(CityTO city) {
 		City entity = new City();
 		entity.setId(city.getId());
 		entity.setName(city.getName());
 		entity.setRegion(converter(city.getRegion()));
 		return entity;
 	}
 
 	public static Neighborhood converter(NeighborhoodTO neighborhood) {
 		Neighborhood entity = new Neighborhood();
 		entity.setId(neighborhood.getId());
 		entity.setHoodName(neighborhood.getName());
 		entity.setCity(converter(neighborhood.getCity()));
 		return entity;
 	}
 
 	public static Street converter(StreetTO street) {
 		Street entity = new Street();
 		entity.setId(street.getId());
 		entity.setStreetName(street.getStreetName());
 		entity.setHood(converter(street.getHood()));
 		return entity;
 	}
 
 	private static PlaceTypeTO converter(PlaceType type) {
 		PlaceTypeTO to = new PlaceTypeTO();
 		to.setId(type.getId());
 		to.setName(type.getName());
 		return to;
 	}
 
 	public static CountryTO converter(Country country) {
 		CountryTO to = new CountryTO();
 		to.setId(country.getId());
 		to.setName(country.getName());
 		return to;
 	}
 
 	public static RegionTO converter(Region region) {
 		RegionTO to = new RegionTO();
 		to.setId(region.getId());
 		to.setName(region.getName());
 		to.setAlias(region.getAlias());
 		to.setCountry(converter(region.getCountry()));
 		return to;
 	}
 
 	public static CityTO converter(City city) {
 		CityTO to = new CityTO();
 		to.setId(city.getId());
 		to.setName(city.getName());
 		to.setRegion(converter(city.getRegion()));
 		return to;
 	}
 
 	public static NeighborhoodTO converter(Neighborhood neigh) {
 		NeighborhoodTO to = new NeighborhoodTO();
 		to.setId(neigh.getId());
 		to.setName(neigh.getHoodName());
 		to.setCity(converter(neigh.getCity()));
 		return to;
 	}
 
 	public static StreetTO converter(Street street) {
 		StreetTO to = new StreetTO();
 		to.setId(street.getId());
 		to.setStreetName(street.getStreetName());
 		to.setHood(converter(street.getHood()));
 		return to;
 	}
 
 	public static PhotoTO converter(Image image) {
 
 		PhotoTO photoTO = new PhotoTO();
 
 		photoTO.setId(image.getId());
 		photoTO.setUrl(image.getPath());
 
 		return photoTO;
 
 	}
 
 	public static Image converter(PhotoTO photoTO) {
 
 		Image image = new Image();
 
 		image.setId(photoTO.getId());
 		image.setPath(photoTO.getUrl());
 
 		return image;
 	}
 
 	public static Facilities converter(FacilitiesTO to) {
 		Facilities entity = new Facilities();
 		entity.setAir(to.isAir());
 		entity.setAutomaticDoor(to.isAutomaticDoor());
 		entity.setBarbecue(to.isBarbecue());
 		entity.setBathBoxGlass(to.isBathBoxGlass());
 		entity.setBathroomCloset(to.isBathroomCloset());
 		entity.setBedroomCloset(to.isBedroomCloset());
 		entity.setGasShower(to.isGasShower());	
 		entity.setGasTubing(to.isGasTubing());
 		entity.setGatekeeper(to.isGatekeeper());
 		entity.setGym(to.isGym());
 		entity.setHidromassage(to.isHidromassage());
 		entity.setInternet(to.isInternet());
 		entity.setKitchenCabinet(to.isKitchenCabinet());
 		entity.setLaundry(to.isLaundry());
 		entity.setName(to.getName());
 		entity.setPartyRoom(to.isPartyRoom());
 		entity.setPlayground(to.isPlayground());
 		entity.setPool(to.isPool());
 		entity.setPrice(to.getPrice());
 		entity.setRoofing(to.isRoofing());
 		entity.setSauna(to.isSauna());
 		entity.setSportArea(to.isSportArea());
 		entity.setTerrace(to.isTerrace());
 		entity.setTownBarbecue(to.isTownBarbecue());		
 		entity.setTownPool(to.isTownPool());
 		return entity;
 	}
 
 	public static PlaceTO converter(PlaceRequest re) {
 		PlaceTO to = new PlaceTO();
 
 		if (re.getCity() != null) {
 			to.setCity(re.getCity());
 		}
 
 		if (re.getNeighborhood() != null) {
 			to.setNeighborhood(re.getNeighborhood());
 		}
 
 		if (re.getStreet() != null) {
 			to.setStreet(re.getStreet());
 		}
 
 		to.setSeller(re.getSeller());
 		to.setType(re.getPlacetype());
 		to.setLat(re.getLat());
 		to.setLng(re.getLog());
 		to.setAddress(re.getAddress());
 		to.setBathroom(re.getBathroom());
 		to.setBedroom(re.getBedroom());
 		to.setCode(re.getCode());
 		to.setComplexPrice(re.getComplexPrice());
 		to.setDescription(re.getDescription());
 		to.setGarage(re.getGarage());
 		to.setM2(re.getM2());
 		to.setPrice(re.getPrice());
 		to.setRoom(re.getRoom());
 		to.setDeposit(re.getDeposit());
 		to.setCellphone(re.getCellphone());
 		to.setCellphone2(re.getCellphone2());
 		to.setSpatialTO(re.getSpatial());
 		to.setComplexPrice(re.getComplexPrice());
 		to.setQtdPlaceFloor(re.getQtdPlaceFloor());
 		to.setRentMonths(re.getRentMonths());	
 		to.setTotalPrice(re.getTotalPrice());
 		to.setSuite(re.getSuite());
 		to.setFacilities(re.getFacilities());
 		
 		to.setRent(re.getRent());
 		to.setContract_time(re.getContract_time());
 		to.setInternet(re.getInternet());
 		to.setTv(re.getTv());
 		to.setCondominiumPrice(re.getCondominiumPrice());
 
 		ArrayList<Long> sellTypes = new ArrayList<Long>();
 
 		if (re.getSellType() != null) {
 			for (Long sellType : re.getSellType()) {
 				sellTypes.add(sellType);
 			}
 
 			to.setSellType(sellTypes);
 		}
 
 		return to;
 	}
 
 }
