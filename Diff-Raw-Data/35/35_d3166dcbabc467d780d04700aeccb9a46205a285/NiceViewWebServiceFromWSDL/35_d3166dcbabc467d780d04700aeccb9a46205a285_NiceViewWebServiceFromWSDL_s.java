/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

 package ws.niceview;
 
 import dk.dtu.imm.fastmoney.types.CreditCardFaultType;
 import java.util.Random;
 import javax.jws.WebService;
 import ws.niceview.types.AddressType;
 import ws.niceview.types.BookHotelFaultType;
 import ws.niceview.types.CancelHotelFaultType;
 import ws.niceview.types.HotelListType;
 import ws.niceview.types.HotelType;
 
 /**
  *
  * @author mkucharek
  */
 @WebService(serviceName = "NiceViewWSDLService", portName = "NiceViewWSDLPort", endpointInterface = "ws.niceview.NiceViewWSDLPortType", targetNamespace = "http://niceview.ws", wsdlLocation = "WEB-INF/wsdl/NiceViewWebServiceFromWSDL/NiceViewWSDL.wsdl")
 public class NiceViewWebServiceFromWSDL {
 
     private int DAY_LIMIT = 14;

    private HotelType[] hotelArray = new HotelType[] {
         createHotel("Superb Hotel", "TestStreet", "12345", "Barcelona", "1", 50000.0, true),
         createHotel("Nice Hotel", "TestStreet", "12345", "Vienna", "2", 2000.0, true),
         createHotel("Passable Hotel", "TestStreet", "12345", "Vienna", "3", 500.0, true),
         createHotel("Shitty Hotel", "TestStreet", "12345", "Zgierz", "4", 10.0, false)
     };
 
     public ws.niceview.types.HotelListType getHotels(java.lang.String city, java.util.Calendar departureDate, java.util.Calendar arrivalDate) {
 
         HotelListType hotelList = new HotelListType();
         boolean randomize = false;
         Random rand = null;
 
         if (arrivalDate.get(java.util.Calendar.DAY_OF_YEAR) -
               departureDate.get(java.util.Calendar.DAY_OF_YEAR) > DAY_LIMIT) {
             randomize = true;
             rand = new Random(System.currentTimeMillis());
         }
 
         for (HotelType hotel : hotelArray) {
 
             if (hotel.getHotelAddress().getCity().equalsIgnoreCase(city)) {
                 if (randomize) {
                     if (rand.nextBoolean()) {
                         hotelList.getHotel().add(hotel);
                     }
 
                 } else {
                     hotelList.getHotel().add(hotel);
                    
                 }
             }
         }
 
         return hotelList;
 
     }
 
     public boolean bookHotel(java.lang.String bookingNumber, dk.dtu.imm.fastmoney.types.CreditCardInfoType creditCardInfo) throws BookHotelFault, BookHotelCreditCardFault {
        if ("".equals(bookingNumber))
             throw new BookHotelFault("Booking number cannot be empty", new BookHotelFaultType());
 
        if ("".equals(creditCardInfo.getNumber()))
             throw new BookHotelCreditCardFault("Number cannot be empty", new CreditCardFaultType());
         return true;
     }
 
     public boolean cancelHotel(java.lang.String bookingNumber) throws CancelHotelFault {
        if ("".equals(bookingNumber))
             throw new CancelHotelFault("Booking number cannot be empty", new CancelHotelFaultType());
         return true;
     }
 
     private HotelType createHotel(String hotelName, String hotelStreet, String hotelPostcode, String hotelCity, String bookingNumber, double stayPrice, boolean ifCreditCardGuaranteeRequired) {
 
         HotelType hotel = new HotelType();
 
         hotel.setHotelName(hotelName);
 
         AddressType hotelAddress = new AddressType();
         hotelAddress.setStreet(hotelStreet);
         hotelAddress.setPostcode(hotelPostcode);
         hotelAddress.setCity(hotelCity);
         hotel.setHotelAddress(hotelAddress);
        
         hotel.setBookingNumber(bookingNumber);
         hotel.setHotelStayPrice(stayPrice);
         hotel.setIfCreditCardRequired(ifCreditCardGuaranteeRequired);
 
         return hotel;
     }

 }
