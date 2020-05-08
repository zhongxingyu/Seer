 package org.fhw.asta.kasse.shared.common;
 
 import java.io.Serializable;
 
 import com.google.gwt.i18n.client.NumberFormat;
 
 public class EuroAmount implements Serializable
 {
   private static final long serialVersionUID = 1L;
  private static final NumberFormat euroFormat = NumberFormat.getCurrencyFormat("EUR");
   private int cents;
 
   public EuroAmount()
   {
     this(0);
   };
 
   public EuroAmount(final int cents)
   {
     this.cents = cents;
   }
 
   public int getCentAmount()
   {
     return this.cents;
   }
 
   public EuroAmount plus(final EuroAmount summand)
   {
     return new EuroAmount(this.cents + summand.cents);
   }
 
   public EuroAmount minus(final EuroAmount subtrahend)
   {
     return new EuroAmount(this.cents - subtrahend.cents);
   }
 
   public EuroAmount times(final float factor)
   {
     return new EuroAmount((int) (this.cents * factor));
   }
 
   public EuroAmount times(final int factor)
   {
     return new EuroAmount(this.cents * factor);
   }
 
   @Override
   public String toString()
   {
    return euroFormat.format(this.cents / 100.0);
   }
 }
