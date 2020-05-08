 package com.pa165.bookingmanager.dao.impl;
 
 import com.pa165.bookingmanager.dao.RoomDao;
 import com.pa165.bookingmanager.entity.HotelEntity;
 import com.pa165.bookingmanager.entity.RoomEntity;
 import org.hibernate.Query;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.dao.DataAccessException;
 import org.springframework.stereotype.Repository;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 /**
  * @author Jana Polakova, Josef Stribny, Jakub Polák
  */
 @Repository("roomDao")
public class RoomDaoImpl extends GenericDaoImpl<RoomEntity, Long> implements RoomDao
 {
     /**
      * Constructor
      */
     public RoomDaoImpl(){
         super(RoomEntity.class);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings("unchecked")
     public List<RoomEntity> findAvailable(Long hotelId, Date from, Date to) throws DataAccessException {
     	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
     	StringBuilder query = new StringBuilder("SELECT room FROM RoomEntity room WHERE room.hotelByHotelId="+hotelId+" ");
 
     	// Restrict results to available hotels in given dates
     	//
     	// If from or to is in the middle of reservation dates for this room, room is unavailable
     	// If from is before reservationFrom and to is after reservationTo, room is unavailable
     	System.out.println(from.toString());
     	System.out.println(to.toString());
     	query.append(" and room.id NOT IN(SELECT reservation.roomByRoomId FROM ReservationEntity reservation WHERE ");
     	query.append(" (reservation.reservationFrom <= '" + sdf.format(from) + "' and reservation.reservationTo >= '" + sdf.format(from) + "')");
     	query.append(" OR ");
     	query.append(" (reservation.reservationFrom <= '" + sdf.format(to) + "' and reservation.reservationTo >= '" + sdf.format(to) + "')");
     	query.append(" OR ");
     	query.append(" (reservation.reservationFrom >= '" + sdf.format(from) + "' and reservation.reservationTo <= '" + sdf.format(to) + "')");
     	query.append(" )");
     	
     	Query result = getCurrentSession().createQuery(query.toString());
         return result.list();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings("unchecked")
     public List<RoomEntity> findByHotel(HotelEntity hotelEntity) {
         return getCurrentSession()
             .createCriteria(RoomEntity.class)
             .add(Restrictions
             .eq("hotelByHotelId", hotelEntity))
             .list()
         ;
     }
 }
