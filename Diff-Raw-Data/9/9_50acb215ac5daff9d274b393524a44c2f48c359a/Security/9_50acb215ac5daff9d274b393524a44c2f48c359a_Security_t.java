 package com.thomasbarker.bullionprompt.model.enums;
 
 import javax.xml.bind.annotation.XmlEnum;
 
 @XmlEnum
 public enum Security {
 
     AUXZU( "AUXZU", "Zurich Gold", SecurityClass.GOLD ),
     AUXLN( "AUXLN", "London Gold", SecurityClass.GOLD ),
     AUXNY( "AUXNY", "New York Gold", SecurityClass.GOLD ),
    AUXTR( "AUXTR", "Toronto Gold", SecurityClass.GOLD ),
    AUXSG( "AUXSG", "Singapore Gold", SecurityClass.GOLD ),
    AGXZU( "AGXZU", "Zurich Silver", SecurityClass.SILVER ),
     AGXLN( "AGXLN", "London Silver", SecurityClass.SILVER ),
    AGXSG( "AGXSG", "Singapore Silver", SecurityClass.SILVER ),
     EUR( "EUR", "Euro", SecurityClass.CURRENCY ),
     USD( "USD", "US Dollar", SecurityClass.CURRENCY ),
     GBP( "GBP", "Sterling", SecurityClass.CURRENCY );
 
     private final String code;
     private final String description;
     private final SecurityClass securityClass;
 
     Security( String code, String desc, SecurityClass securityClass ) {
         this.code = code;
         this.description = desc;
         this.securityClass = securityClass;
     }
 
     public String getCode() {
         return this.code;
     }
 
     public String getDescription() {
         return this.description;
     }
 
     public SecurityClass getSecurityClass() {
     	return this.securityClass;
     }
 
     @XmlEnum
     public enum SecurityClass {
 
     	GOLD( "AUX", "Gold bullion "),
     	SILVER( "AGX", "Silver bullion" ),
     	CURRENCY( "CSH", "Cash currency" );
     	
         private final String code;
         private final String description;
 
         SecurityClass( String code, String desc ) {
             this.code = code;
             this.description = desc;
         }
 
         public String getCode() {
             return this.code;
         }
 
         public String getDescription() {
             return this.description;
         }
     }
 }
