 import java.util.ArrayList;
 import java.util.ListIterator;
 
 public class Buchungsmap{
 	Buchungsmap(){}
 	private int uid = 0;
 	private ArrayList<Buchung> buchungen = new ArrayList<Buchung>(); 
 	
 	public boolean buche(Datum start, Datum end, String gastname) throws overBooking{
 		
 		Buchung buchung = new Buchung(start, end, gastname);		
 		
		if( this.buchungM�glich(buchung) ){
 			buchung.uid = this.uid++;
 			this.buchungen.add(buchung);		
 			return true;
 		} else {
 			throw new overBooking(start, end);		
 		}
 	}
 	
	private boolean buchungM�glich(Buchung b1){
 		
 		ListIterator<Buchung> it = this.buchungen.listIterator( this.buchungen.size() );
 		Datum b1Start= b1.start,
 				b1End = b1.end;
 			
 		while( it.hasPrevious() ){
 			Buchung b2 = it.previous();
 			
 			Datum	b2Start = b2.start,
 					b2End = b2.end;
 			
 			if(b1Start.isInRangeOf(b2Start, b2End) || b1End.isInRangeOf(b2Start, b2End) || 
 					b2Start.isInRangeOf(b1Start, b1End) || b2End.isInRangeOf(b1Start, b1End)){
 				if(!b1Start.isEqual(b2End)){
 					return false;
 				}	
 			} 	
 		
 		}		    
 		return true;
 	}
 	
 	
 	public ArrayList<Buchung> getBuchungen(){
 		return this.buchungen;
 	}
 	
 	public void storniere(int nr) throws nonExistingBookingNumber{
 		ListIterator<Buchung> it = this.buchungen.listIterator( this.buchungen.size() );
 		
 		while( it.hasPrevious() ){
 			Buchung buchung = it.previous();
 			if(buchung.uid == nr){
 				this.buchungen.remove(buchung);
 				return;
 			}
 		}		
 		
 		throw new nonExistingBookingNumber();
 		
 	}
 	
 	private class nonExistingBookingNumber extends Exception{
 		nonExistingBookingNumber(){
 			super("Non Existing Booking Number was passed");
 		}
 		nonExistingBookingNumber(int nr){
 			super("The passed Booking Number: '" + nr + "' is not existing");
 		}		
 	}
 	
 	private class overBooking extends Exception{
 		overBooking(){
 			super("The Booking is not possible");
 		}
 		overBooking(Datum S, Datum E){
 			super("The Booking '" + S + "' and '"+ E +"' is not possible");
 		}		
 	}
 	
 	
 	
 }
 	
 	
