 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jtoodle.api.json.mixin;
 
import com.fasterxml.jackson.annotation.JsonProperty;

 /**
  *
  * @author justo
  */
 public interface TokenMixIn extends AbstractJToodleBeanMixIn {
 
	@JsonProperty( "userid" )
	abstract String getUserId();
	abstract void setUserId( String userId );
 }
