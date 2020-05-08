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
 package fr.peralta.mycellar.domain.stock;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 
 import org.apache.commons.lang3.ObjectUtils;
 import org.hibernate.annotations.Type;
 import org.joda.time.LocalDate;
 
 import fr.peralta.mycellar.domain.shared.IdentifiedEntity;
 
 /**
  * @author speralta
  */
 @Entity
 @Table(name = "INPUT")
 @SequenceGenerator(name = "INPUT_ID_GENERATOR", allocationSize = 1)
 public class Input extends IdentifiedEntity<Input> {
 
     private static final long serialVersionUID = 201010311742L;
 
     @Column(name = "ARRIVAL")
     @Type(type = "org.joda.time.contrib.hibernate.PersistentLocalDate")
     private LocalDate arrival;
 
     @ManyToOne
    @JoinColumn(name = "BOTTLE", nullable = false)
     private Bottle bottle;
 
     @ManyToOne
     @JoinColumn(name = "CELLAR", nullable = false)
     private Cellar cellar;
 
     @Column(name = "CHARGES")
     private float charges;
 
     @Id
     @GeneratedValue(generator = "INPUT_ID_GENERATOR")
     @Column(name = "ID", nullable = false, unique = true)
     private Integer id;
 
     @Column(name = "NUMBER")
     private int number;
 
     @Column(name = "PRICE")
     private float price;
 
     @Column(name = "SOURCE")
     private String source;
 
     /**
      * @return the arrival
      */
     public LocalDate getArrival() {
         return arrival;
     }
 
     /**
      * @return the bottle
      */
     public Bottle getBottle() {
         return bottle;
     }
 
     /**
      * @return the cellar
      */
     public Cellar getCellar() {
         return cellar;
     }
 
     /**
      * @return the charges
      */
     public float getCharges() {
         return charges;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Integer getId() {
         return id;
     }
 
     /**
      * @return the number
      */
     public int getNumber() {
         return number;
     }
 
     /**
      * @return the price
      */
     public float getPrice() {
         return price;
     }
 
     /**
      * @return the source
      */
     public String getSource() {
         return source;
     }
 
     /**
      * @param arrival
      *            the arrival to set
      */
     public void setArrival(LocalDate arrival) {
         this.arrival = arrival;
     }
 
     /**
      * @param bottle
      *            the bottle to set
      */
     public void setBottle(Bottle bottle) {
         this.bottle = bottle;
     }
 
     /**
      * @param cellar
      *            the cellar to set
      */
     public void setCellar(Cellar cellar) {
         this.cellar = cellar;
     }
 
     /**
      * @param charges
      *            the charges to set
      */
     public void setCharges(float charges) {
         this.charges = charges;
     }
 
     /**
      * @param number
      *            the number to set
      */
     public void setNumber(int number) {
         this.number = number;
     }
 
     /**
      * @param price
      *            the price to set
      */
     public void setPrice(float price) {
         this.price = price;
     }
 
     /**
      * @param source
      *            the source to set
      */
     public void setSource(String source) {
         this.source = source;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected boolean dataEquals(Input other) {
         return ObjectUtils.equals(getArrival(), other.getArrival())
                 && ObjectUtils.equals(getCellar(), other.getCellar())
                 && ObjectUtils.equals(getCharges(), other.getCharges())
                 && ObjectUtils.equals(getNumber(), other.getNumber())
                 && ObjectUtils.equals(getPrice(), other.getPrice())
                 && ObjectUtils.equals(getSource(), other.getSource())
                 && ObjectUtils.equals(getBottle(), other.getBottle());
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected Object[] getHashCodeData() {
         return new Object[] { getArrival(), getCellar(), getSource() };
     }
 
 }
