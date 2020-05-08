 package com.google.wave.extensions.emaily.email;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.google.inject.Singleton;
 
 @Singleton
 public class EmailAddressUtil {
   // TODO(dlux): Make the email format parts injectable.
   // TODO(dlux): Use the something@emaily-wave.appspot.com email format if wave
   //   and appengine will support them.
 
   /**
    * Returns an email address from a wave participant ID.
    * 
    * @param waveParticipantId The wave participant Id.
    * @return The email address of the wave participant. It returns null if it is
    *         a non-email participant.
    */
   public String waveParticipantIdToEmailAddress(String waveParticipantId) {
     Matcher m = waveParticipantIdToEmailPattern.matcher(waveParticipantId);
     if (!m.matches()) {
       return null;
     }
     return m.group(1) + "@" + m.group(2);
   }
   
   /**
    * Returns a wave participant Id for an email recipient.
    * 
    * @param emailAddress
    *          The email address. It requires to be a full email address with a
    *          domain, and must not contain any more information, besides the
    *          pure address.
    * @return A wave participant Id.
    */
   public String emailAddressToWaveParticipantId(String emailAddress) {
     int at = emailAddress.lastIndexOf('@');
     if (at < 0) {
       throw new RuntimeException("Invalid email address: " + emailAddress);
     }
     return "emaily-wave+" + emailAddress.substring(0, at) + "+"
         + emailAddress.substring(at + 1) + "@appspot.com";
   }

  private final Pattern waveParticipantIdToEmailPattern = Pattern
      .compile("emaily-wave\\+(.*)\\+(.*)\\@appspot.com");
 }
