 package de.hswt.hrm.place.model;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Strings.isNullOrEmpty;
 
 public class Place {
     private int id;
     private String placeName;
     private String postCode;
     private String city;
     private String street;
     private String streetNo;
     private String location;
     private String area;
 
     private String isMandatory = "Field is a mandatory.";
 
     public Place(final String placeName, final String postCode, final String city,
             final String street, final String streetNo, final String location, final String area) {
 
         this(-1, placeName, postCode, city, street, streetNo, location, area);
     }
 
     public Place(int id, final String placeName, final String postCode, final String city,
             final String street, final String streetNo, final String location, final String area) {
 
         this.id = id;
 
         setPlaceName(placeName);
         setPostCode(postCode);
         setCity(city);
         setStreet(street);
         setStreetNo(streetNo);
         setLocation(location);
         setArea(area);
     }
 
     public int getId() {
         return id;
     }
 
     public String getPlaceName() {
         return placeName;
     }
 
     public void setPlaceName(String placeName) {
         checkArgument(!isNullOrEmpty(placeName), isMandatory);
         this.placeName = placeName;
     }
 
     public String getPostCode() {
         return postCode;
     }
 
     public void setPostCode(String postCode) {
         checkArgument(!isNullOrEmpty(postCode), isMandatory);
         this.postCode = postCode;
     }
 
     public String getCity() {
         return city;
     }
 
     public void setCity(String city) {
         checkArgument(!isNullOrEmpty(city), isMandatory);
         this.city = city;
     }
 
     public String getStreet() {
         return street;
     }
 
     public void setStreet(String street) {
         checkArgument(!isNullOrEmpty(street), isMandatory);
         this.street = street;
     }
 
     public String getStreetNo() {
         return streetNo;
     }
 
     public void setStreetNo(String streetNo) {
         checkArgument(!isNullOrEmpty(streetNo), isMandatory);
         this.streetNo = streetNo;
     }
 
     public String getLocation() {
         return location;
     }
 
     public void setLocation(String location) {
         checkArgument(!isNullOrEmpty(location), isMandatory);
         this.location = location;
     }
 
     public String getArea() {
         return area;
     }
 
     public void setArea(String area) {
         checkArgument(!isNullOrEmpty(area), isMandatory);
         this.area = area;
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((area == null) ? 0 : area.hashCode());
         result = prime * result + ((city == null) ? 0 : city.hashCode());
         result = prime * result + id;
         result = prime * result + ((location == null) ? 0 : location.hashCode());
         result = prime * result + ((placeName == null) ? 0 : placeName.hashCode());
         result = prime * result + ((postCode == null) ? 0 : postCode.hashCode());
         result = prime * result + ((street == null) ? 0 : street.hashCode());
         result = prime * result + ((streetNo == null) ? 0 : streetNo.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         Place other = (Place) obj;
         if (area == null) {
             if (other.area != null) {
                 return false;
             }
         }
        else if (!area.equals(other.area)) {
             return false;
        }
         if (city == null) {
            if (other.city != null) {
                 return false;
            }
         }
         else if (!city.equals(other.city)) {
             return false;
         }
         if (id != other.id) {
             return false;
         }
         if (location == null) {
             if (other.location != null) {
                 return false;
             }
         }
         else if (!location.equals(other.location)) {
             return false;
         }
         if (placeName == null) {
             if (other.placeName != null) {
                 return false;
             }
         }
         else if (!placeName.equals(other.placeName)) {
             return false;
         }
         if (postCode == null) {
             if (other.postCode != null) {
                 return false;
             }
         }
         else if (!postCode.equals(other.postCode)) {
             return false;
         }
         if (street == null) {
             if (other.street != null) {
                 return false;
             }
         }
         else if (!street.equals(other.street)) {
             return false;
         }
         if (streetNo == null) {
             if (other.streetNo != null) {
                 return false;
             }
         }
         else if (!streetNo.equals(other.streetNo)) {
             return false;
         }
         return true;
     }
 
 }
