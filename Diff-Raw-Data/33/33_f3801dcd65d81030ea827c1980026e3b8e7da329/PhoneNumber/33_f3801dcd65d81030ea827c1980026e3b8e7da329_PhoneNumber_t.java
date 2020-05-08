 package acmetelecom;
 
 /**
  * Harro I am a phone number class.
  */
 
 public class PhoneNumber {
 private String number;
 private String countrycode;
 private String stem;
 
public PhoneNumber(String number) {
 	this.number = number;
	this.countrycode = number.substring(0, 2);
	this.stem = number.substring(2);
 }
 
 public boolean isValid() {
	if (number.length() == 12 && countrycode.length() == 2 && stem.length() == 10) {
 		return true;
 	}
 	else return false;
 }
 
 public String getNumber() {
 	return number;
 }
 
 public String getCountrycode() {
 	return countrycode;
 }
 
 public String getStem() {
 	return stem;
 }
 
 public void setNumber(String number) {
 	this.number = number;
 }
 
 public void setCountrycode(String countrycode) {
 	this.countrycode = countrycode;
 }
 
 public void setStem(String stem) {
 	this.stem = stem;
 }
 
 }
