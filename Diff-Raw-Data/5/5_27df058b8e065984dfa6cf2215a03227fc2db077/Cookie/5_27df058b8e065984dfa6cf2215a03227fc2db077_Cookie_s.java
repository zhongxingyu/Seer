 package http.header;
 
 /**
  * An HTTP {@code Cookie} header.
  *
  * @author Karl Bennett
  */
public class Cookie extends Header<Cookie> {
 
     public static final String COOKIE = "Cookie";
 
     /**
      * Create a {@code Cookie} header with the supplied {@link http.Cookie}.
      *
      * @param cookie the cookie for this header.
      */
    public Cookie(Cookie cookie) {
         super(COOKIE, cookie);
     }
 }
