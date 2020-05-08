 package ws.wiklund.guides.model;
 
 import java.util.Date;
 
 import android.content.ContentValues;
 
 @TableName(name = "beverage")
 public class Beverage extends BaseModel {
 	private static final long serialVersionUID = 8130320547884807505L;
 	
 	private int no = -1;
 	private BeverageType beverageType;
 	private String thumb;
 	private Country country;
 	private int year = -1;
 	private Producer producer;
 	private double strength = -1;
 	private double price = -1;
 	private String usage;
 	private String taste;
 	private Provider provider;
 	private float rating = -1;
 	private Date added;
 	private String comment;
 	private Category category;
 	private int bottlesInCellar;
 
 	private String image;
 
 	public Beverage() {
 		this(null);
 	}
 
 	public Beverage(String name) {
 		super(name);
 	}
 
 	public Beverage(int id, String name, int no, BeverageType beverageType, String thumb,
 			String image, Country country, int year, Producer producer, double strength, double price,
 			String usage, String taste, Provider provider, float rating, String comment, 
 			Category category, Date added, int bottlesInCellar) {
 		super(id, name);
 		
 		this.no = no;
 		
 		if(beverageType.isOther()) {
 			this.beverageType = BeverageType.OTHER;
 		} else {
 			this.beverageType = beverageType;
 		}
 		
 		this.thumb = thumb;
 		this.image = image;
 		this.country = country;
 		this.year = year;
 		this.producer = producer;
 		this.strength = strength;
 		this.price = price;
 		this.usage = usage;
 		this.taste = taste;
 		this.provider = provider;
 		this.rating = rating;
 		this.comment = comment;
 		this.category = category;
 		this.added = added;
 		this.bottlesInCellar = bottlesInCellar;
 	}
 
 	public int getNo() {
 		return no;
 	}
 
 	public void setNo(int no) {
 		this.no = no;
 	}
 	
 	public BeverageType getBeverageType() {
 		return beverageType;
 	}
 
 	public void setBeverageType(BeverageType beverageType) {
 		this.beverageType = beverageType;
 	}
 
 	public String getThumb() {
 		return thumb;
 	}
 	
 	public boolean isCustomThumb() {
		return thumb != null && !thumb.startsWith("/");
 	}
 
 	public void setThumb(String thumb) {
 		this.thumb = thumb;
 	}
 	
 	public String getImage() {
 		return image;
 	}
 
 	public void setImage(String image) {
 		this.image = image;
 	}
 	
 	public boolean hasImage() {
 		return image != null;
 	}
 
 	public boolean isFullSizeImageAvailable() {
		return thumb != null && !thumb.contains("bild_saknas");
 	}
 
 	public Country getCountry() {
 		return country;
 	}
 
 	public void setCountry(Country country) {
 		this.country = country;
 	}
 
 	public int getYear() {
 		return year;
 	}
 
 	public void setYear(int year) {
 		this.year = year;
 	}
 
 	public Producer getProducer() {
 		return producer;
 	}
 
 	public void setProducer(Producer producer) {
 		this.producer = producer;
 	}
 
 	public double getStrength() {
 		return strength;
 	}
 
 	public void setStrength(double strength) {
 		this.strength = strength;
 	}
 
 	public double getPrice() {
 		return price;
 	}
 
 	public void setPrice(double price) {
 		this.price = price;
 	}
 
 	public String getUsage() {
 		return usage;
 	}
 
 	public void setUsage(String usage) {
 		this.usage = usage;
 	}
 
 	public String getTaste() {
 		return taste;
 	}
 
 	public void setTaste(String taste) {
 		this.taste = taste;
 	}
 
 	public Provider getProvider() {
 		return provider;
 	}
 
 	public void setProvider(Provider provider) {
 		this.provider = provider;
 	}
 
 	public float getRating() {
 		return rating;
 	}
 
 	public void setRating(float rating) {
 		this.rating = rating;
 	}
 
 	public String getComment() {
 		return comment;
 	}
 	
 	public void setComment(String comment) {
 		this.comment = comment;
 	}
 
 	public Date getAdded() {
 		return added;
 	}
 
 	public void setAdded(Date added) {
 		this.added = added;
 	}
 
 	public Category getCategory() {
 		return category;
 	}
 	
 	public void setCategory(Category category) {
 		this.category = category;
 	}
 	
 	public boolean hasPrice() {
 		return price > 0;
 	}
 	
 	public int getBottlesInCellar() {
 		return bottlesInCellar;
 	}
 
 	public void setBottlesInCellar(int bottlesInCellar) {
 		this.bottlesInCellar = bottlesInCellar;
 	}
 
 	public boolean hasBottlesInCellar() {
 		return bottlesInCellar > 0;
 	}
 	
 	public ContentValues getAsContentValues() {
 		ContentValues values = new ContentValues();
 
 		if(country != null && !country.isNew()) {
 			values.put("country_id", country.getId());  
 		}
 		
 		if(producer != null && !producer.isNew()) {
 			values.put("producer_id", producer.getId());  
 		}
 
 		if(provider != null && !provider.isNew()) {
 			values.put("provider_id", provider.getId());  
 		}
 		
 		if(category != null && !category.isNew()) {
 			values.put("category_id", category.getId());  
 		}
 
 		values.put("name", getName());  
 		values.put("no", no);  
 		values.put("thumb", thumb);
 		values.put("image", image);
 		values.put("year", year);  
 		values.put("beverage_type_id", beverageType.getId());  
 		values.put("strength", strength);  
 		values.put("price", price);  
 		values.put("usage", usage);  
 		values.put("taste", taste);  
 		values.put("rating", rating); 
 		values.put("comment", comment); 
 		
 		return values;
 	}
 	
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (!super.equals(obj))
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Beverage other = (Beverage) obj;
 		if (added == null) {
 			if (other.added != null)
 				return false;
 		} else if (!added.equals(other.added))
 			return false;
 		if (beverageType == null) {
 			if (other.beverageType != null)
 				return false;
 		} else if (!beverageType.equals(other.beverageType))
 			return false;
 		if (bottlesInCellar != other.bottlesInCellar)
 			return false;
 		if (category == null) {
 			if (other.category != null)
 				return false;
 		} else if (!category.equals(other.category))
 			return false;
 		if (comment == null) {
 			if (other.comment != null)
 				return false;
 		} else if (!comment.equals(other.comment))
 			return false;
 		if (country == null) {
 			if (other.country != null)
 				return false;
 		} else if (!country.equals(other.country))
 			return false;
 		if (no != other.no)
 			return false;
 		if (Double.doubleToLongBits(price) != Double
 				.doubleToLongBits(other.price))
 			return false;
 		if (producer == null) {
 			if (other.producer != null)
 				return false;
 		} else if (!producer.equals(other.producer))
 			return false;
 		if (provider == null) {
 			if (other.provider != null)
 				return false;
 		} else if (!provider.equals(other.provider))
 			return false;
 		if (Float.floatToIntBits(rating) != Float.floatToIntBits(other.rating))
 			return false;
 		if (Double.doubleToLongBits(strength) != Double
 				.doubleToLongBits(other.strength))
 			return false;
 		if (taste == null) {
 			if (other.taste != null)
 				return false;
 		} else if (!taste.equals(other.taste))
 			return false;
 		if (thumb == null) {
 			if (other.thumb != null)
 				return false;
 		} else if (!thumb.equals(other.thumb))
 			return false;
 		if (image == null) {
 			if (other.image != null)
 				return false;
 		} else if (!image.equals(other.image))
 			return false;
 		if (usage == null) {
 			if (other.usage != null)
 				return false;
 		} else if (!usage.equals(other.usage))
 			return false;
 		if (year != other.year)
 			return false;
 		return true;
 	}
 	
 	@Override
 	public String toString() {
 		return "Beverage [getId()=" + getId() + ", getName()=" + getName()
 				+ ", no=" + no + ", beverageType=" + beverageType
 				+ ", thumb=" + thumb + ", iamge=" + image
 				+ ", country=" + country + ", year="
 				+ year + ", producer=" + producer + ", strength=" + strength
 				+ ", price=" + price + ", usage=" + usage + ", taste=" + taste
 				+ ", provider=" + provider + ", rating=" + rating + ", added="
 				+ added + ", comment=" + comment + ", category=" + category
 				+ ", bottlesInCellar=" + bottlesInCellar + "]";
 	}
 
 }
