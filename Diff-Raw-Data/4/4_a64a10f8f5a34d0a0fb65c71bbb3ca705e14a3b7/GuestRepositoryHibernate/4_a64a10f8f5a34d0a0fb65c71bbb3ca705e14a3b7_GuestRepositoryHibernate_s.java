 package net.mklew.hotelms.persistance;
 
 import com.google.common.base.Optional;
 import net.mklew.hotelms.domain.booking.GuestRepository;
 import net.mklew.hotelms.domain.guests.Guest;
 import net.mklew.hotelms.domain.guests.Person;
 import net.mklew.hotelms.persistance.hibernate.configuration.HibernateSessionFactory;
 import org.hibernate.Session;
 import org.jcontainer.dna.Configuration;
 
 import java.util.Collection;
 
 /**
  * @author Marek Lewandowski <marek.m.lewandowski@gmail.com>
  * @since 11/30/12
  *        time 7:53 PM
  */
 public class GuestRepositoryHibernate extends HibernateRepository implements GuestRepository
 {
     private static final int GUEST_LOOK_UP_BY_NAME_LIMIT_DEFAULT = 5;
 
     private final int guestLookupByNameLimit;
 
     private final static String FIND_ALL_WHERE_NAME_LIKE = "select guest from Guest guest where firstName like " +
             ":firstName and surname like :surname";
 
     public GuestRepositoryHibernate(HibernateSessionFactory hibernateSessionFactory, Configuration config)
     {
         super(hibernateSessionFactory);
         guestLookupByNameLimit = config.getChild("guestLookUpByNameLimit").getValueAsInteger
                 (GUEST_LOOK_UP_BY_NAME_LIMIT_DEFAULT);
     }
 
     public GuestRepositoryHibernate(HibernateSessionFactory hibernateSessionFactory)
     {
         super(hibernateSessionFactory);
         guestLookupByNameLimit = GUEST_LOOK_UP_BY_NAME_LIMIT_DEFAULT;
     }
 
     @Override
     public Collection<Guest> findAllWhereNameLike(String firstName, String surname)
     {
         final Session session = getCurrentSession();
         String surnameLike = surname + "%";
         String firstNameLike = firstName + "%";
         @SuppressWarnings("unchecked") final Collection<Guest> guests = session.createQuery(FIND_ALL_WHERE_NAME_LIKE)
                 .setParameter("firstName",
                         firstNameLike).setParameter("surname", surnameLike).setMaxResults
                         (guestLookupByNameLimit).list();
         return guests;
 
     }
 
     @Override
     public Guest findGuestById(Long id)
     {
         final Session session = getCurrentSession();
         Guest guest = (Guest) session.byId(Guest.class).load(id);
         return guest;
     }
 
     @Override
     public void saveGuest(Guest guest)
     {
         final Session session = getCurrentSession();
         session.save(guest);
     }
 
     @Override
     public Collection<Guest> findAll()
     {
         final Session session = getCurrentSession();
         return (Collection<Guest>) session.createQuery("from Person where PERSON_TYPE = 'G'").list();
     }
 
     @Override
     public Collection<Guest> findAllInHouse()
     {
        String query = "select person from Reservation reservation join reservation.reservationOwner person where " +
                "person.PERSON_TYPE = 'G' and reservation.reservationStatus = 'INHOUSE'";
         final Session session = getCurrentSession();
         return (Collection<Guest>) session.createQuery(query).list();
     }
 
     @Override
     public Optional<Guest> lookup(long id)
     {
         final Session session = getCurrentSession();
         final Guest guest = (Guest) session.get(Person.class, id);
         if (guest != null)
         {
             return Optional.of(guest);
         }
         else
         {
             return Optional.absent();
         }
     }
 }
