 import java.io.Serializable;
 import java.util.*;
 
 public class Token implements UserToken, Serializable{
 	private String issuer, subject;
 	private List<String> groups;
 
 	public Token(String issuer_, String subject_, List<String> groups_){
 		issuer = issuer_;
 		subject = subject_;
 		groups = groups_;
 	}
 
 	 public String getIssuer(){
 	 	return issuer;
 	 }
 
 
     /**
      * This method should return a string indicating the name of the
      * subject of the token.  For instance, if "Alice" requests a
      * token from the group server "Server1", this method will return
      * the string "Alice".
      *
      * @return The subject of this token
      *
      */
     public String getSubject(){
 
     	return subject;
     }
 
 
     /**
      * This method extracts the list of groups that the owner of this
      * token has access to.  If "Alice" is a member of the groups "G1"
      * and "G2" defined at the group server "Server1", this method
      * will return ["G1", "G2"].
      *
      * @return The list of group memberships encoded in this token
      *
      */
     public List<String> getGroups(){
 
     	return groups;
     }
 }
