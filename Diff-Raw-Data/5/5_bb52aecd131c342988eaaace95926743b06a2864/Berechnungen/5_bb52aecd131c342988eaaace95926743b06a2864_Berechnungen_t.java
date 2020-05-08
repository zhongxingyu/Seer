 package controller;
 
 public class Berechnungen {
 
 	public static double SummeEinkunft(double JahresBruttoLohn,
 			double WerbungsKosten) {
 		double SummeEinkunft = JahresBruttoLohn - WerbungsKosten;
 		return Math.round(SummeEinkunft * 100.00) / 100.00;
 	}
 
 	public static double WerbungsKosten(int ArbeitsTage, double EntfernungWA,
 			double ArbeitsMittelGezahlt, double TelefonKostenGezahlt) {
 
 		double ENTFERNUNGSPAUSCHALE = 0.3;
 		double WerbungsKostenAbzug = 0.0;
 		double ArbeitsMittelAbzug = 0.0;
 		double TelefonKostenAbzug = 0.0;
 		double WerbungsKostenGezahlt = 0.0;
 
 		if (ArbeitsMittelGezahlt <= 110.0) {
			ArbeitsMittelAbzug = 110.0;
 		} else {
 			ArbeitsMittelAbzug = Math.round(ArbeitsMittelGezahlt * 100.00) / 100.00;
 		}
 
 		if (TelefonKostenGezahlt <= 240.0) {
			TelefonKostenAbzug = 240.0;
 		} else {
 			TelefonKostenAbzug = Math.round(TelefonKostenGezahlt * 100.00) / 100.00;
 		}
 
 		WerbungsKostenGezahlt = Math.round(((ArbeitsTage * EntfernungWA * ENTFERNUNGSPAUSCHALE) + ArbeitsMittelGezahlt + TelefonKostenGezahlt) * 100.00) / 100.00;
 
 		if (WerbungsKostenGezahlt <= 1000.0) {
 			WerbungsKostenAbzug = 1000.0;
 		} else {
 			WerbungsKostenAbzug = Math.round(WerbungsKostenGezahlt * 100.00) / 100.00;
 		}
 		return Math.round(WerbungsKostenAbzug * 100.00) / 100.00;
 	}
 
 }
