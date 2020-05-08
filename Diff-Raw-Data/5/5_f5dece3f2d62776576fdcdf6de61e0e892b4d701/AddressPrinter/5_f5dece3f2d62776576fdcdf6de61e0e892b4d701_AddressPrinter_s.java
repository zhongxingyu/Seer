 /**
  * 
  */
 package module.geography.util;
 
 import module.geography.domain.Country;
 
 /**
  * Interface that has the methods to be implemented by the several
  * AddressPrinters per country that should exist and be assigned to the
  * modules.geography.domain.Country field
  * 
  * TODO
  * 
  * @author João André Pereira Antunes (joao.antunes@tagus.ist.utl.pt)
  * 
  */
 public class AddressPrinter {
 
     public AddressPrinter() {
 
     }
 
     public AddressPrinter(Country country) {
 	this();
 
     }
 
    public static String getFormatedAddress(String complementarAddress, Country country) {
 	if (country.equals(Country.getPortugal())) {
 	    return complementarAddress;
 	} else
 	    return complementarAddress;
     }
 
 }
