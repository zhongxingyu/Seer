 package com.brotherlogic.booser.atom;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 public class Drink extends Atom
 {
    Beer beer;
    Date created_at;
    String photo_url;
    double rating_score;
    Calendar time;
    User user;
    Venue v;
 
    public Drink(String id)
    {
       super(id);
    }
 
    public Drink(String id, Beer beer, User user, Venue venue, Date timestamp)
    {
       super(id);
       this.beer = beer;
       v = venue;
       this.user = user;
       this.created_at = timestamp;
    }
 
    public Beer getBeer()
    {
       // Pull the beer if we don't have it
       if (beer == null && getUnderlyingRep() != null && getUnderlyingRep().length() > 0)
       {
          JsonObject obj = new JsonParser().parse(super.getUnderlyingRep()).getAsJsonObject();
          beer = new Gson().fromJson(obj.get("beer"), Beer.class);
       }
 
       return beer;
    }
 
    public Date getCreated_at()
    {
       // Fix if the id is not yet set
       if (created_at == null)
       {
          JsonObject obj = new JsonParser().parse(super.getUnderlyingRep()).getAsJsonObject();
          created_at = new GsonBuilder().setDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").create()
                .fromJson(obj.get("created_at"), Date.class);
       }
 
       return created_at;
    }
 
    @Override
    public String getId()
    {
       // Fix if the id is not yet set
       if (super.getId() == null && getUnderlyingRep() != null)
       {
          JsonObject obj = new JsonParser().parse(getUnderlyingRep()).getAsJsonObject();
          super.setId(obj.get("checkin_id").getAsString());
       }
 
       return super.getId();
    }
 
    public Integer getIdNumber()
    {
      return Integer.parseInt(getId());
    }
 
    public String getPhoto_url()
    {
       // Pull the beer if we don't have it
       if (photo_url == null && getUnderlyingRep() != null && getUnderlyingRep().length() > 0)
       {
          JsonObject obj = new JsonParser().parse(getUnderlyingRep()).getAsJsonObject();
          JsonArray photo = obj.get("media").getAsJsonObject().get("items").getAsJsonArray();
          if (photo.size() > 0)
             photo_url = photo.get(0).getAsJsonObject().get("photo").getAsJsonObject()
                   .get("photo_img_md").getAsString();
       }
       return photo_url;
    }
 
    public double getRating_score()
    {
       return rating_score;
    }
 
    public Calendar getTime()
    {
       if (time == null)
       {
          time = Calendar.getInstance();
          time.setTime(created_at);
       }
 
       return time;
    }
 
    public Date getTimestamp()
    {
       return created_at;
    }
 
    public User getUser()
    {
       return user;
    }
 
    public Venue getVenue()
    {
       return v;
    }
 
    @Override
    public boolean hasDecayed()
    {
       // Drinks never decay
       return false;
    }
 
    public void setBeer(Beer beer)
    {
       this.beer = beer;
    }
 
    public void setCreated_at(Date created_at)
    {
       this.created_at = created_at;
    }
 
    public void setPhoto_url(String photo_url)
    {
       this.photo_url = photo_url;
    }
 
    public void setRating_score(double rating_score)
    {
       this.rating_score = rating_score;
    }
 
    @Override
    public void setUnderlyingRep(String rep)
    {
       super.setUnderlyingRep(rep);
 
       if (beer != null && beer.getUnderlyingRep() == null)
          beer.setUnderlyingRep(new JsonParser().parse(rep).getAsJsonObject().get("beer").toString());
    }
 
    public void setUser(User u)
    {
       this.user = u;
    }
 
    public void setVenue(Venue venue)
    {
       v = venue;
    }
 }
