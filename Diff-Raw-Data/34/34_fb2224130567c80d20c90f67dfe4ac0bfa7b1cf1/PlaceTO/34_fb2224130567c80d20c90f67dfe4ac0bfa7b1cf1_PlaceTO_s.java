 package br.com.findplaces.model.to;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlRootElement;
 
 import br.com.findplaces.model.geographic.to.CityTO;
 import br.com.findplaces.model.geographic.to.NeighborhoodTO;
 import br.com.findplaces.model.geographic.to.StreetTO;
 import br.com.findplaces.model.spatial.to.PlaceSpatialTO;
 
 @XmlRootElement(name = "place")
 public class PlaceTO implements Serializable {
 
 	private static final long serialVersionUID = -4443449674515309396L;
 
 	private Long id;
 	
 	private List<Long> idImages;
 	
 	private List<ComentTO> coments;
 
 	private String address;
 
 	private CityTO city;
 
 	private NeighborhoodTO neighborhood;
 
 	private StreetTO street;
 
 	private PlaceTypeTO type;
 
 	private String description;
 
 	private SellerTO seller;
 
 	private Double lat;
 
 	private Double log;
 
 	private PlaceSpatialTO spatialTO;
 	
 	private String code;		
 
 	private Double price;	
 	
 	private Double complexPrice;	
 	
 	private Integer room;	
 	
 	private Integer bathroom;
 		
 	private Integer bedroom;	
 	
 	private Integer suite;	
 	
 	private Double m2;	
 	
 	private Integer garage;
 	
 	private List<Long> sellType;
 	
 	private FacilitiesTO facilities;
 	
 	private Double deposit;
 	
 	private Integer rentMonths;
 	
 	private Integer placeFloor;
 	
 	private Integer qtdPlaceFloor;
 	
 	private Double iptu;
 	
 	private Double condominiumPrice;
 	
 	private Double internet;
 	
 	private Double tv;
 	
 	private Double totalPrice;
 	
 	private String cellphone;
 	
 	private String cellphone2;
 	
 	private String cellphone3;
 
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getAddress() {
 		return address;
 	}
 
 	public void setAddress(String address) {
 		this.address = address;
 	}
 
 	public CityTO getCity() {
 		return city;
 	}
 
 	public void setCity(CityTO city) {
 		this.city = city;
 	}
 
 	public NeighborhoodTO getNeighborhood() {
 		return neighborhood;
 	}
 
 	public void setNeighborhood(NeighborhoodTO neighborhood) {
 		this.neighborhood = neighborhood;
 	}
 
 	public StreetTO getStreet() {
 		return street;
 	}
 
 	public void setStreet(StreetTO street) {
 		this.street = street;
 	}
 
 	public PlaceTypeTO getType() {
 		return type;
 	}
 
 	public void setType(PlaceTypeTO type) {
 		this.type = type;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public Double getLat() {
 		return lat;
 	}
 
 	public Double getLog() {
 		return log;
 	}
 
 	public SellerTO getSeller() {
 		return seller;
 	}
 
 	public void setSeller(SellerTO seller) {
 		this.seller = seller;
 	}
 
 	public void setLat(Double lat) {
 		this.lat = lat;
 	}
 
 	public void setLog(Double log) {
 		this.log = log;
 	}
 
 	public PlaceSpatialTO getSpatialTO() {
 		return spatialTO;
 	}
 
 	public void setSpatialTO(PlaceSpatialTO spatialTO) {
 		this.spatialTO = spatialTO;
 	}
 
 	public String getCode() {
 		return code;
 	}
 
 	public Double getPrice() {
 		return price;
 	}
 
 	public Double getComplexPrice() {
 		return complexPrice;
 	}
 
 	public Integer getRoom() {
 		return room;
 	}
 
 	public Integer getBathroom() {
 		return bathroom;
 	}
 
 	public Double getDeposit() {
 		return deposit;
 	}
 
 	public void setDeposit(Double deposit) {
 		this.deposit = deposit;
 	}
 
 	public Integer getRentMonths() {
 		return rentMonths;
 	}
 
 	public void setRentMonths(Integer rentMonths) {
 		this.rentMonths = rentMonths;
 	}
 
 	public Integer getPlaceFloor() {
 		return placeFloor;
 	}
 
 	public void setPlaceFloor(Integer placeFloor) {
 		this.placeFloor = placeFloor;
 	}
 
 	public Integer getQtdPlaceFloor() {
 		return qtdPlaceFloor;
 	}
 
 	public void setQtdPlaceFloor(Integer qtdPlaceFloor) {
 		this.qtdPlaceFloor = qtdPlaceFloor;
 	}
 
 	public Double getIptu() {
 		return iptu;
 	}
 
 	public void setIptu(Double iptu) {
 		this.iptu = iptu;
 	}
 
 	public Double getCondominiumPrice() {
 		return condominiumPrice;
 	}
 
 	public void setCondominiumPrice(Double condominiumPrice) {
 		this.condominiumPrice = condominiumPrice;
 	}
 
 	public Double getInternet() {
 		return internet;
 	}
 
 	public void setInternet(Double internet) {
 		this.internet = internet;
 	}
 
 	public Double getTv() {
 		return tv;
 	}
 
 	public void setTv(Double tv) {
 		this.tv = tv;
 	}
 
 	public Double getTotalPrice() {
 		return totalPrice;
 	}
 
 	public void setTotalPrice(Double totalPrice) {
 		this.totalPrice = totalPrice;
 	}
 
 	public String getCellphone() {
 		return cellphone;
 	}
 
 	public void setCellphone(String cellphone) {
 		this.cellphone = cellphone;
 	}
 
 	public String getCellphone2() {
 		return cellphone2;
 	}
 
 	public void setCellphone2(String cellphone2) {
 		this.cellphone2 = cellphone2;
 	}
 
 	public String getCellphone3() {
 		return cellphone3;
 	}
 
 	public void setCellphone3(String cellphone3) {
 		this.cellphone3 = cellphone3;
 	}
 
 	public Integer getBedroom() {
 		return bedroom;
 	}
 
 	public Integer getSuite() {
 		return suite;
 	}
 
 	public Double getM2() {
 		return m2;
 	}
 
 	public Integer getGarage() {
 		return garage;
 	}
 
 	public void setCode(String code) {
 		this.code = code;
 	}
 
 	public void setPrice(Double price) {
 		this.price = price;
 	}
 
 	public void setComplexPrice(Double complexPrice) {
 		this.complexPrice = complexPrice;
 	}
 
 	public void setRoom(Integer room) {
 		this.room = room;
 	}
 
 	public void setBathroom(Integer bathroom) {
 		this.bathroom = bathroom;
 	}
 
 	public void setBedroom(Integer bedroom) {
 		this.bedroom = bedroom;
 	}
 
 	public void setSuite(Integer suite) {
 		this.suite = suite;
 	}
 
 	public void setM2(Double m2) {
 		this.m2 = m2;
 	}
 
 	public void setGarage(Integer garage) {
 		this.garage = garage;
 	}
 
 	public FacilitiesTO getFacilities() {
 		return facilities;
 	}
 
 	public void setFacilities(FacilitiesTO facilities) {
 		this.facilities = facilities;
 	}
 
 	public List<Long> getSellType() {
 		return sellType;
 	}
 
 	public void setSellType(List<Long> sellType) {
 		this.sellType = sellType;
 	}
 
 	public List<ComentTO> getComents() {
 		return coments;
 	}
 
 	public void setComents(List<ComentTO> coments) {
 		this.coments = coments;
 	}
 
 	public List<Long> getIdImages() {
 		return idImages;
 	}
 
 	public void setIdImages(List<Long> idImages) {
 		this.idImages = idImages;
 	}
 
 }
