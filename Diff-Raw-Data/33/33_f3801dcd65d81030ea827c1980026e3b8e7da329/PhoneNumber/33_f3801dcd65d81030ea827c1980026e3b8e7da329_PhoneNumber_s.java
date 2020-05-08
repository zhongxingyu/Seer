 package acmetelecom;
 
 /**
  * Harro I am a phone number class.
  */
 
 public class PhoneNumber {
 private String number;
 private String countrycode;
 private String stem;
 
public PhoneNumber(String number, String countrycode, String stem) {
 	this.number = number;
	this.countrycode = countrycode;
	this.stem = stem;
 }
 
 public boolean isValid() {
	if (number.length() == 12 && countrycode.length() == 2 && stem.length() == 9) {
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
