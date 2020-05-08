 package org.pl;
 
 import org.pl.exception.CanNotInstantiateStaticClassException;
 
 public final class ErrorCode extends Object {
 	private ErrorCode () throws CanNotInstantiateStaticClassException {
 		throw new
       CanNotInstantiateStaticClassException ("ErrorCode is a static class.");
 	}
 
 	public static final int EC_OK = 0;
   
   public static final int EC_UNEXPECTED_EXCEPTION = 1;
 
 	public static final int EC_NOT_IMPLEMENTED = 2;
 	
   public static final int EC_INVALID_PARAMETER = 3;
   public static final int EC_NULL_PARAMETER = 4;
 
   public static final int EC_INVALID_OBJECT = 5;
   public static final int EC_NULL_OBJECT = 6;
 	
   public static final int EC_OUT_OF_RANGE = 7;
 	public static final int EC_OUT_OF_MEMORY = 8;
   
   public static final int EC_IMPOSSIBLE_CASE = 9;
 
   public static final int EC_CAN_NOT_INSTANTIATE_STATIC_CLASS_EXCEPTION = 10;
 
   public static final int EC_FAILED_CONVERTING = 11;
 
   public static final int EC_FAILED_READING_FILE = 12;
   public static final int EC_INVALID_FILE_FORMAT = 13;
   
  public static final int EC_DB_FAILED = 14;
 
  public static final int EC_FAILED_FINDING_WEB_ELEMENT = 15;
 
 	public static void main (String args []) {
 		try {
 			ErrorCode ec = new ErrorCode ();
 			System.out.println ("Error happened while instantiating ErrorCode.");
 		} catch (CanNotInstantiateStaticClassException e) {
 			System.out.println ("Prevented instantiation.");
 		} finally {
 		}
 		
 		System.out.println ("EC_OK: " + EC_OK);
     System.out.println ("EC_UNEXPECTED_EXCEPTION: " + EC_UNEXPECTED_EXCEPTION);
 		System.out.println ("EC_NOT_IMPLEMENTED: " + EC_NOT_IMPLEMENTED);
     System.out.println ("EC_NOT_IMPLEMENTED: " + EC_INVALID_PARAMETER);
 		System.out.println ("EC_NULL_PARAMETER: " + EC_NULL_PARAMETER);
 		System.out.println ("EC_OUT_OF_RANGE: " + EC_OUT_OF_RANGE);
 		System.out.println ("EC_OUT_OF_MEMORY: " + EC_OUT_OF_RANGE);
     System.out.println ("EC_IMPOSSIBLE_CASE: " + EC_IMPOSSIBLE_CASE);
     System.out.println ("EC_CAN_NOT_INSTANTIATE_STATIC_CLASS_EXCEPTION" +
       EC_CAN_NOT_INSTANTIATE_STATIC_CLASS_EXCEPTION);
     System.out.println ("EC_FAILED_CONVERTING: " + EC_FAILED_CONVERTING);
 
 		System.exit (EC_OK);
 	}
 }
