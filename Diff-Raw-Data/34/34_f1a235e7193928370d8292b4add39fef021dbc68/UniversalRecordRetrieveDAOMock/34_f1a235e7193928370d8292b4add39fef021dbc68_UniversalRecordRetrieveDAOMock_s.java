 /*
  */
 package com.springeetravel.dataaccess.support;
 
 import com.springeetravel.dataaccess.UniversalRecordRetrieveDAO;
 import com.springeetravel.domain.*;
 import java.util.*;
 import javax.inject.Named;
 
 /**
  *
  * @author peter
  */
@Named
 public class UniversalRecordRetrieveDAOMock implements UniversalRecordRetrieveDAO {
 
     @Override
     public List<UniversalRecord> findByAirTicketedDate(Date fromDate, Date toDate) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public List<UniversalRecord> findByAgent(String agentId) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public List<UniversalRecord> findByAgency(String agencyId) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public UniversalRecord findByLocatorCode(String locatorCode) {
         // to be replaced with dao look up code...
         return buildMockUniversalRecord();
     }
 
     @Override
     public List<UniversalRecord> findByBookingPassengerName(String passengerName) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public UniversalRecord buildMockUniversalRecord() {
         UniversalRecord ur = new UniversalRecord();
         ur.setId("12000");
 
         Calendar cal = Calendar.getInstance();
         Date time = cal.getTime();
         String departureAirportCode = "JFK";
         Date departureTime = time;
         String arrivalAirportCode = "DEN";
         Date arrivalTime = time;
         String airline = "American Airlines";
         String airlineCode = "AA";
         String airlineFlightNumber = "AA4829";
         String classOfService = "Economy";
         double price = 220.00;
 
         FlightDetail departureFlight = new FlightDetail();
         departureFlight.setAirline(airline);
         departureFlight.setAirlineCode(airlineCode);
         departureFlight.setAirlineFlightNumber(airlineFlightNumber);
         departureFlight.setArrivalAirportCode(arrivalAirportCode);
         departureFlight.setArrivalTime(arrivalTime);
         departureFlight.setClassOfService(classOfService);
         departureFlight.setDepartureAirportCode(departureAirportCode);
         departureFlight.setDepartureTime(departureTime);
         departureFlight.setPrice(price);
 
         FlightDetail arrivalFlight = new FlightDetail();
         arrivalFlight.setAirline(airline);
         arrivalFlight.setAirlineCode(airlineCode);
         arrivalFlight.setAirlineFlightNumber(airlineFlightNumber);
         arrivalFlight.setArrivalAirportCode(departureAirportCode);
         arrivalFlight.setArrivalTime(arrivalTime);
         arrivalFlight.setClassOfService(classOfService);
         arrivalFlight.setDepartureAirportCode(arrivalAirportCode);
         arrivalFlight.setDepartureTime(departureTime);
         arrivalFlight.setPrice(price);
 
         String firstName = "David";
         String lastName = "Jones";
         String initials = "M. H.";
         String contactPhone = "303 764 2311";
         String contactEmail = "djones@home.com";
         String countryOfResidenceCode = "CAN";
         String countryOfNationality = "CAN";
         String passportId = "A1001229";
         Boolean valuedCustomer = Boolean.TRUE;
 
         PassengerDetail passengerDetail = new PassengerDetail();
         passengerDetail.setAirSegmentId(null);
         passengerDetail.setContactEmail(contactEmail);
         passengerDetail.setContactPhone(contactPhone);
         passengerDetail.setCountryOfNationalityCode(countryOfNationality);
         passengerDetail.setCountryOfResidenceCode(countryOfResidenceCode);
         passengerDetail.setFirstName(firstName);
         passengerDetail.setInitials(initials);
         passengerDetail.setLastName(lastName);
         passengerDetail.setPassportId(passportId);
         passengerDetail.setValuedCustomer(valuedCustomer);
         cal.roll(Calendar.YEAR, -30);
         Date dob = cal.getTime();
         passengerDetail.setDateOfBirth(dob);
 
         String formOfPaymentType = "CC";
         String creditCardNumber = "2921 1929 1281 3282";
         String creditCardName = "Visa";
         String creditExpiry = "02/13";
         String creditCcid = "320";
 
         FormOfPayment formOfPayment = new FormOfPayment();
         formOfPayment.setCreditCardName(creditCardName);
         formOfPayment.setCreditCardNumber(creditCardNumber);
         formOfPayment.setCreditCcid(creditCcid);
         formOfPayment.setCreditExpiry(creditExpiry);
         formOfPayment.setFormOfPaymentType(formOfPaymentType);
 
         List<AirSegment> airSegments = createAirSegments(departureFlight, arrivalFlight, passengerDetail, formOfPayment);
 
         ur.setAirSegmentList(airSegments);
         return ur;
     }
 
     public List<AirSegment> createAirSegments(FlightDetail departureFlight, FlightDetail arrivalFlight, PassengerDetail passengerDetail, FormOfPayment formOfPayment) {
         List<AirSegment> airSegments = new ArrayList<AirSegment>();
         Date time = GregorianCalendar.getInstance().getTime();
         //
         AirSegment departureAirSegment = new AirSegment();
         departureAirSegment.setCreateDate(time);
         departureAirSegment.setFlightDetail(departureFlight);
         PassengerDetail passengerDetail1 = new PassengerDetail(passengerDetail.getFirstName(), passengerDetail.getLastName(), passengerDetail.getInitials(), passengerDetail.getContactPhone(), passengerDetail.getContactEmail(), passengerDetail.getCountryOfResidenceCode(), passengerDetail.getCountryOfNationalityCode(), passengerDetail.getPassportId(), passengerDetail.getDateOfBirth(), passengerDetail.getValuedCustomer());
         FormOfPayment formOfPayment1 = new FormOfPayment(formOfPayment.getFormOfPaymentType(), formOfPayment.getCreditCardNumber(), formOfPayment.getCreditCardName(), formOfPayment.getCreditExpiry(), formOfPayment.getCreditCcid());
         departureAirSegment.setPassengerDetailList(Arrays.asList(new PassengerDetail[]{passengerDetail1}));
         departureAirSegment.setFormOfPaymentList(Arrays.asList(new FormOfPayment[]{formOfPayment1}));
         associateAirSegment(departureAirSegment, departureFlight, passengerDetail1, formOfPayment1);
         airSegments.add(departureAirSegment);
         //
         AirSegment arrivalAirSegment = new AirSegment();
         arrivalAirSegment.setCreateDate(time);
         arrivalAirSegment.setFlightDetail(arrivalFlight);
         PassengerDetail passengerDetail2 = new PassengerDetail(passengerDetail.getFirstName(), passengerDetail.getLastName(), passengerDetail.getInitials(), passengerDetail.getContactPhone(), passengerDetail.getContactEmail(), passengerDetail.getCountryOfResidenceCode(), passengerDetail.getCountryOfNationalityCode(), passengerDetail.getPassportId(), passengerDetail.getDateOfBirth(), passengerDetail.getValuedCustomer());
         FormOfPayment formOfPayment2 = new FormOfPayment(formOfPayment.getFormOfPaymentType(), formOfPayment.getCreditCardNumber(), formOfPayment.getCreditCardName(), formOfPayment.getCreditExpiry(), formOfPayment.getCreditCcid());
         arrivalAirSegment.setPassengerDetailList(Arrays.asList(new PassengerDetail[]{passengerDetail2}));
         arrivalAirSegment.setFormOfPaymentList(Arrays.asList(new FormOfPayment[]{formOfPayment2}));
         arrivalFlight.setAirSegmentId(arrivalAirSegment);
         passengerDetail2.setAirSegmentId(arrivalAirSegment);
         associateAirSegment(arrivalAirSegment, arrivalFlight, passengerDetail2, formOfPayment2);
         airSegments.add(arrivalAirSegment);
         //
         return airSegments;
     }
 
     protected void associateAirSegment(AirSegment airSegment, FlightDetail flightDetails, PassengerDetail passengerDetail, FormOfPayment formOfPayment) {
         flightDetails.setAirSegmentId(airSegment);
         passengerDetail.setAirSegmentId(airSegment);
         formOfPayment.setAirSegmentId(airSegment);
     }
 }
