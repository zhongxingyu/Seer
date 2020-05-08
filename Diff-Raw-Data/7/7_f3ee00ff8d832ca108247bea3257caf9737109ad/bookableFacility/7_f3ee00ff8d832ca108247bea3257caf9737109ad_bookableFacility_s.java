
 public class bookableFacility {
 		protected String name;
 		protected double price;
 		protected String location;
 		private Buchungsmap bookings = new Buchungsmap();
 		
 
 		bookableFacility (String name, double price, String location ){		
 			this.setName(name);
 			this.setPrice(price);
 			this.setLocation(location);
 			
 		}
 		
 		bookableFacility(){
 			this.setName("leer");
 			this.setPrice(0);
 			this.setLocation("leer");
 		}
 		
 		public void setName(String name){ 
 			if( !name.isEmpty()){
 				this.name = name;
 			}
 		}
 		public String getName(){
 			return name; 
 		}
 		
 		public void setPrice(double price){ 
 			if(price > -1){
 				this.price = price;
 			}
 		}
 		public double getPrice(){
 			return this.price; 
 		}
 		
 	
 		public void setLocation(String location){
 			if(!location.isEmpty()){
 				this.location =location;
 			}
 		}
 		
 		public String getLocation(){
 			return location; 
 		}
 		
 		private double calcPrice(int tage){
 			return 0.0;
 			//has to be implemented
 		}
 		
 		
 		public final double buchen(Datum Heute, Datum BuchungsDatum, int tage, String name){		
 			if(tage < 1 ||
 					Heute.getYear() < BuchungsDatum.getYear() || 
 					Heute.getYear() == BuchungsDatum.getYear() && Heute.getMonth() == BuchungsDatum.getMonth() && Heute.getDay() == BuchungsDatum.getDay()){
 				return 0;
 			} else {
				if(this.isVerfgbar(BuchungsDatum, tage, name)){
 					return this.calcPrice(tage);
 				} else {
 					return 0;
 				}
 				
 			}		
 		}
 		
 		public final void stornieren(int nr){
 			try{
 				this.bookings.storniere(nr);
 			} catch(Exception e){
 				System.out.println("Buchung nicht stornierbar");
 			}
 			
 		}
 		
 		
		private boolean isVerfgbar(Datum BuchungsDatum, int naechte, String name){	
 			try{
 				return this.bookings.buche(BuchungsDatum, BuchungsDatum.addDays(naechte), name);
 			} catch (Exception e){
 				return false;
 			}
 		}
 				
 		
 		public String toString(){
 			return "bookableFacility: {  \n	" +
 					"name: " + name  + "\n	" +
 					"location: " + location + "\n	" +
 					"price: " + price + "\n	" +
 					"buchungen: " + bookings + "\n	" +	
 				"}";
 		}	
 }
