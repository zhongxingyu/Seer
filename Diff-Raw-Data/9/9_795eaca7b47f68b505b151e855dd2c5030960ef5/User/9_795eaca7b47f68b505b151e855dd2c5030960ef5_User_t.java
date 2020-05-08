 package com.brotherlogic.booser.atom;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Represents a user within the system
  * 
  * @author simon
  * 
  */
 public class User extends Atom
 {
    long creationDate;
    List<Drink> drinks;
    int numberOfCheckins;
    String user_name = "";
 
    public User(String id)
    {
       super(id);
       user_name = id;
       drinks = new LinkedList<Drink>();
    }
 
    public void addDrink(Drink d)
    {
      if (d != null)
         drinks.add(d);
    }
 
    @Override
    public boolean equals(Object obj)
    {
       if (!(obj instanceof User))
          return false;
 
       User other = (User) obj;
 
       if (!user_name.equals(other.user_name))
          return false;
 
       return super.equals(obj);
    }
 
    public List<Drink> getDrinks()
    {
       if (drinks == null)
          drinks = new LinkedList<Drink>();
       return drinks;
    }
 
    @Override
    public String getId()
    {
       // Override the user ud
       return user_name;
    }
 
    public int getNumberOfCheckins()
    {
       return numberOfCheckins;
    }
 
    @Override
    public boolean hasDecayed()
    {
       // Users decay after 1 minute
       return (System.currentTimeMillis() - creationDate) > 1000 * 60;
    }
 
    @Override
    public int hashCode()
    {
       return user_name.hashCode();
    }
 
    public void setDrinks(List<Drink> drinks)
    {
      this.drinks = new LinkedList<Drink>();
      for (Drink d : drinks)
         if (d != null)
            this.drinks.add(d);
    }
 
    @Override
    public void setId(String id)
    {
       super.setId(id);
       user_name = id;
    }
 
    public void setNumberOfCheckins(int numberOfCheckins)
    {
       this.numberOfCheckins = numberOfCheckins;
    }
 }
