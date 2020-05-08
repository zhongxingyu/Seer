 package net.mklew.hotelms.inhouse.web.rest;
 
 import com.sun.jersey.spi.resource.Singleton;
 import net.mklew.hotelms.domain.booking.GuestRepository;
 import net.mklew.hotelms.domain.booking.reservation.*;
 import net.mklew.hotelms.domain.booking.reservation.rates.Rate;
 import net.mklew.hotelms.domain.booking.reservation.rates.RateRepository;
 import net.mklew.hotelms.domain.guests.Guest;
 import net.mklew.hotelms.domain.room.Room;
 import net.mklew.hotelms.domain.room.RoomName;
 import net.mklew.hotelms.domain.room.RoomNotFoundException;
 import net.mklew.hotelms.domain.room.RoomRepository;
 import net.mklew.hotelms.inhouse.web.dto.GuestDto;
 import net.mklew.hotelms.inhouse.web.dto.MissingGuestInformation;
 import net.mklew.hotelms.inhouse.web.dto.ReservationDto;
 import net.mklew.hotelms.persistance.hibernate.configuration.HibernateSessionFactory;
 import org.hibernate.Session;
 import org.jcontainer.dna.Logger;
 
 import javax.naming.OperationNotSupportedException;
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import java.util.Collection;
 
 /**
  * @author Marek Lewandowski <marek.m.lewandowski@gmail.com>
  * @since 12/24/12
  *        time 3:46 PM
  */
 @Singleton
 @Path("/reservations")
 public class ReservationResource
 {
     private final Logger logger;
     private final ReservationFactory reservationFactory;
     private final GuestRepository guestRepository;
     private final HibernateSessionFactory hibernateSessionFactory;
     private final RoomRepository roomRepository;
     private final RateRepository rateRepository;
     private final BookingService bookingService;
 
     public ReservationResource(Logger logger, ReservationFactory reservationFactory, GuestRepository guestRepository,
                                HibernateSessionFactory hibernateSessionFactory, RoomRepository roomRepository,
                                RateRepository rateRepository, BookingService bookingService)
     {
         this.logger = logger;
         this.reservationFactory = reservationFactory;
         this.guestRepository = guestRepository;
         this.hibernateSessionFactory = hibernateSessionFactory;
         this.roomRepository = roomRepository;
         this.rateRepository = rateRepository;
         this.bookingService = bookingService;
     }
 
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public Collection<ReservationDto> getAllReservations()
     {
         // todo
         return null;
     }
 
     @POST
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
     public ReservationDto createNewReservation(MultivaluedMap<String, String> formParams,
                                                @Context HttpServletResponse httpServletResponse)
     {
         Session session = hibernateSessionFactory.getCurrentSession();
         session.beginTransaction();
         logger.debug("Got new reservation with parameters: " + formParams.toString());
         try
         {
             GuestDto reservationOwner = GuestDto.fromReservationForm(formParams);
             ReservationDto reservationDto = ReservationDto.fromReservationForm(formParams);
 
            Guest owner;
             if (reservationOwner.exists())
             {
                 owner = guestRepository.findGuestById(Long.parseLong(reservationOwner.id));
             }
             else
             {
                 owner = new Guest(reservationOwner.socialTitle, reservationOwner.firstName,
                         reservationOwner.surname, reservationOwner.gender, reservationOwner.idType,
                         reservationOwner.idNumber, reservationOwner.phoneNumber);
                 owner.setPreferences(reservationOwner.preferences);
                 owner.setDateOfBirth(reservationOwner.dateOfBirthDate);
                 // owner.setEmailAddress(); // todo add field to form, dto, and set it here
                 // todo nationality, address and other
                 guestRepository.saveGuest(owner);
             }
 
             // get room
             RoomName roomName = RoomName.getNameWithoutPrefix(reservationDto.getRoomName());
             final Room room = roomRepository.getRoomByName(roomName);
             // find rate
             Collection<Rate> rates = rateRepository.getAllRatesForRoom(room);
             Rate rate = getChosenRate(reservationDto, rates);
 
             // create reservation using factory
            Reservation reservation;
             if (ReservationType.fromName(reservationDto.getReservationType()).equals(ReservationType.SINGLE))
             {
                 reservation = reservationFactory.createSingleReservation(owner, room, rate,
                         reservationDto.getCheckinDate(),
                         reservationDto.getCheckoutDate(), Integer.parseInt(reservationDto.getNumberOfAdults()),
                         Integer.parseInt(reservationDto.getNumberOfChildren()), Integer.parseInt(reservationDto
                         .getRoomExtraBed()));
             }
             else
             {
                 throw new OperationNotSupportedException("Other reservation types are not supported ");
             }
 
             // book reservation or fail on exception
             bookingService.bookReservation(reservation);
             ReservationDto bookedDto = ReservationDto.fromReservation(reservation);
 
 
             session.getTransaction().commit();
             return bookedDto;
         }
         catch (MissingGuestInformation missingGuestInformation)
         {
             logger.error("Reservation owner has no sufficient information", missingGuestInformation);
             httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
             return null;
         }
         catch (RoomNotFoundException e)
         {
             logger.error("Room not found exception", e);
             httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
             return null;
         }
         catch (OperationNotSupportedException e)
         {
             logger.error("Operation not supported", e);
             httpServletResponse.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
         }
         catch (RoomIsUnavailableException e)
         {
             httpServletResponse.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
         }
         return null;
     }
 
     private Rate getChosenRate(ReservationDto reservationDto, Collection<Rate> rates)
     {
         for (Rate rate : rates)
         {
             if (reservationDto.getRateType().equals(rate.getRateName()))
             {
                 return rate;
             }
         }
         throw new RuntimeException("Rate not found");
     }
 }
