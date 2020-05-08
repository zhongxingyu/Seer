 package net.mklew.hotelms.domain.booking.reservation;
 
 import net.mklew.hotelms.domain.booking.reservation.rates.Rate;
 import net.mklew.hotelms.domain.room.Room;
 import net.mklew.hotelms.domain.room.RoomName;
 import net.mklew.hotelms.domain.room.RoomNotFoundException;
 import net.mklew.hotelms.domain.room.RoomRepository;
 import org.joda.time.DateTime;
 
 import java.util.Collection;
 import java.util.Set;
 
 /**
  * Implements booking business logic. What's below is very simple business logic with no overbooking taken into account
  *
  * @author Marek Lewandowski <marek.m.lewandowski@gmail.com>
  * @since 12/25/12
  *        time 4:04 PM
  */
 public class BookingService
 {
     private final RoomRepository roomRepository;
     private final ReservationRepository reservationRepository;
 
     public BookingService(RoomRepository roomRepository, ReservationRepository reservationRepository)
     {
         this.roomRepository = roomRepository;
         this.reservationRepository = reservationRepository;
     }
 
     public boolean isRoomAvailable(Reservation reservation)
     {
         return roomRepository.isRoomAvailableBetweenDates(reservation.getRoom(), reservation.getCheckIn(),
                reservation.getCheckOut());
     }
 
 
     public void bookReservation(Reservation reservation) throws RoomIsUnavailableException
     {
         if (isRoomAvailable(reservation))
         {
             reservation.reserve();
             reservationRepository.bookNewReservation(reservation);
         }
         else
         {
             throw new RoomIsUnavailableException(reservation.getRoom().fullRoomName(), reservation.getCheckIn(),
                     reservation.getCheckOut());
         }
     }
 
     /**
      * Changes room, using default rate (Rack rate) for given reservation
      *
      * @param reservation
      * @param room
      */
     private void changeRoom(Reservation reservation, Room room)
     {
         reservation.changeRoom(room, room.rackRate());
     }
 
     /**
      * Changes rate for given reservation. Rate must be available in given room
      *
      * @param reservation
      * @param chosenRate
      */
     public void changeRate(Reservation reservation, String chosenRate) throws RateNotFoundException
     {
         final Set<Rate> rates = reservation.getRoom().getRates();
         Rate rate = getChosenRate(chosenRate, rates);
        if (rates.contains(chosenRate))
         {
             reservation.changeRate(rate);
         }
         else
         {
             throw new RateNotFoundException(rate);
         }
     }
 
     private static Rate getChosenRate(String rateName, Collection<Rate> rates)
     {
         for (Rate rate : rates)
         {
             if (rate.getRateName().equals(rateName))
             {
                 return rate;
             }
         }
         throw new RuntimeException("Rate not found");
     }
 
     public void rebookReservationRoom(Reservation reservation, RoomName roomName, String newRate,
                                       DateTime newCheckIn, DateTime
             newCheckOut) throws RoomNotFoundException, RateNotFoundException, RoomIsUnavailableException
     {
         final Room room = roomRepository.getRoomByName(roomName);
         changeRoom(reservation, room);
         changeRate(reservation, newRate);
         changeCheckInAndCheckOut(reservation, newCheckIn, newCheckOut);
     }
 
     private void changeCheckInAndCheckOut(Reservation reservation, DateTime newCheckIn,
                                           DateTime newCheckOut) throws RoomIsUnavailableException
 
     {
         validateThatRoomIsAvailable(reservation, newCheckIn, newCheckOut);
         reservation.changeCheckInAndCheckOut(newCheckIn, newCheckOut);
     }
 
     private void validateThatRoomIsAvailable(Reservation reservation, DateTime newCheckIn,
                                              DateTime newCheckOut) throws RoomIsUnavailableException
     {
        if (!(roomRepository.isRoomAvailableBetweenDates(reservation.getRoom(), newCheckIn, newCheckOut)))
         {
             throw new RoomIsUnavailableException(reservation.getRoom().fullRoomName(), newCheckIn,
                     newCheckOut);
         }
     }
 
     public void rebookVisit(Reservation reservation, DateTime newCheckIn,
                             DateTime newCheckOut) throws RoomIsUnavailableException
     {
         validateThatRoomIsAvailable(reservation, newCheckIn, newCheckOut);
         reservation.changeCheckInAndCheckOut(newCheckIn, newCheckOut);
     }
 
 
 }
