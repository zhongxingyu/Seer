 /*
  * Copyright 2011, MyCellar
  *
  * This file is part of MyCellar.
  *
  * MyCellar is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * MyCellar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with MyCellar. If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.peralta.mycellar.domain.booking.comparator;
 
 import java.io.Serializable;
 import java.util.Comparator;
 
 import fr.peralta.mycellar.domain.booking.BookingBottle;
 
 /**
  * @author speralta
  */
 public class BookingBottlePositionComparator implements Comparator<BookingBottle>, Serializable {
 
     private static final long serialVersionUID = 201205301634L;
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int compare(BookingBottle o1, BookingBottle o2) {
         if ((o1 == null) && (o2 == null)) {
             return 0;
         }
         if ((o1 != null) && (o2 == null)) {
             return -1;
         }
         if ((o1 == null) && (o2 != null)) {
             return 1;
         }
        if (o1 == o2) {
            return 0;
        }
        if (o1.getPosition() == null) {
            return 1;
        }
        if (o2.getPosition() == null) {
            return -1;
        }
         return o1.getPosition().compareTo(o2.getPosition());
     }
 
 }
