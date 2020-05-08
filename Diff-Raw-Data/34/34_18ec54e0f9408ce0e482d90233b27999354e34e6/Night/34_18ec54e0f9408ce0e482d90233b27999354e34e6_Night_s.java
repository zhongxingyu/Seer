 package net.mklew.hotelms.domain.booking.reservation;
 
 import net.mklew.hotelms.domain.booking.reservation.rates.Rate;
 import net.mklew.hotelms.domain.room.Room;
 import org.joda.money.Money;
 import org.joda.time.DateTime;
 
 /**
  * @author Marek Lewandowski <marek.m.lewandowski@gmail.com>
  * @since 9/27/12
  *        Time: 11:59 AM
  */
 public class Night implements Chargeable
 {
     private Reservation reservation;
     private DateTime date;
     private Money price;
     private NightStatus nightStatus;
     private Rate rate;
 
     public Night(Reservation reservation, DateTime date, NightStatus nightStatus, Money price, Rate rate)
     {
         this.reservation = reservation;
         this.date = date;
         this.price = price;
         this.nightStatus = nightStatus;
         this.rate = rate;
     }
 
     Room getRoom()
     {
         return rate.getRoom();
     }
 
     @Override
     public Money charge()
     {
         return price;
     }
 
     public Rate getRate()
     {
         return rate;
     }
 
     public DateTime getDate()
     {
         return date;
     }
 
     public Reservation getReservation()
     {
         return reservation;
     }
 
     public Money getPrice()
     {
         return price;
     }
 
     public NightStatus getNightStatus()
     {
         return nightStatus;
     }
 
     private void setReservation(Reservation reservation)
     {
         this.reservation = reservation;
     }
 
     private void setDate(DateTime date)
     {
         this.date = date;
     }
 
     public void setPrice(Money price)
     {
         this.price = price;
     }
 
     public void setNightStatus(NightStatus nightStatus)
     {
         this.nightStatus = nightStatus;
     }
 
     private void setRate(Rate rate)
     {
         this.rate = rate;
     }
 
     Night()
     {
     }
 }
