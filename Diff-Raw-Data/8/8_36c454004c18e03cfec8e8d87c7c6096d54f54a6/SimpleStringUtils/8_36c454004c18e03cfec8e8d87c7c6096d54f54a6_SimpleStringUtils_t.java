 package com.redshape.utils;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 
 @SuppressWarnings("restriction")
 public class SimpleStringUtils {
     
     public static List<String> camelCaseDelimiters = Arrays.asList( "_", "-" );
 	private static final String RANDOM_STRING_SOURCE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
 	private static final String ESCAPE_SYMBOL = "\\";
 
 	public final static String[] chunks( String source, int chunkSize ) {
 		int leastLength = source.length();
 		float chunksCount = leastLength / chunkSize;
 
 		String[] chunks = new String[Math.round(chunksCount)
                 + ( leastLength - chunkSize * chunksCount > 0 ? 1 : 0 )];
 		int i = 0 ;
 		int offset = 0;
 		while ( leastLength >= chunkSize ) {
 			offset = i++ * chunkSize;
 			chunks[i-1] = source.substring( offset, offset + chunkSize );
 			leastLength -= chunkSize;
 		}
 
 		if ( leastLength > 0 ) {
 			chunks[i+1] = source.substring( offset, offset + leastLength );
 		}
 
 		return chunks;
 	}
 
     public final static String repeat(String source, int times) {
         String result = "";
 
         while (times > 0) {
             result = result.concat(source);
             times -= 1;
         }
 
         return result;
     }
 
     public final static String quote(String s) {
         int slashEIndex = s.indexOf("\\E");
         if (slashEIndex == -1)
             return "\\Q" + s + "\\E";
 
         StringBuilder sb = new StringBuilder(s.length() * 2);
         sb.append("\\Q");
         slashEIndex = 0;
         int current = 0;
         while ((slashEIndex = s.indexOf("\\E", current)) != -1) {
             sb.append(s.substring(current, slashEIndex));
             current = slashEIndex + 2;
             sb.append("\\E\\\\E\\Q");
         }
         sb.append(s.substring(current, s.length()));
         sb.append("\\E");
         return sb.toString();
     }
 
     public final static String reverseSentence( String sentence ) {
     	return reverseSentence( sentence, " ");
     }
     
     public final static String reverseSentence( String sentence, String delimiter ) {
     	StringBuilder builder = new StringBuilder();
         String[] parts = sentence.split( quote( delimiter ) );
         for ( int i = parts.length - 1; i >= 0; i--) {
             builder.append( parts[i] );
             
             if ( i != 0 ) {
             	builder.append( delimiter );
             }
         }
         
         return builder.toString();
     }
 
 	public final static String randomString(int length) {
 		if (length < 1) {
 			return "";
 		}
 
 		StringBuilder sb = new StringBuilder(length);
 		for (int i = 0; i < length; i++) {
 			sb.append(
 				RANDOM_STRING_SOURCE.charAt(
 					(int)(Math.random() * RANDOM_STRING_SOURCE.length())
 				)
 			);
 		}
 
 		return sb.toString();
 	}
 
     public final static String reverse( String input ) {
           return String.valueOf( reverse( input.toCharArray() ) );
     }
 
     public final static char[] reverse( char[] input ) {
         char[] result = new char[input.length];
 
         int lastOffset = input.length - 1;
         for( int i = lastOffset; i >= 0; i-- ) {
             result[lastOffset - i] = input[i];
         }
 
         return result;
     }
 
 	public final static String trim( String source ) {
 		return trim( source, " ");
 	}
 
 	public final static String trim( String source, String needle ) {
 		if ( source == null ) {
 			throw new IllegalArgumentException("<null>");
 		}
 
 		while ( source.startsWith(needle) ) {
 			source = source.substring(1);
 		}
 
 		while ( source.endsWith(needle) ) {
 			source = source.substring(0, source.length() - 1 );
 		}
 
 		return source;
 	}
 
     public final static String getFileExtension( String name ) {
     	int index = name.lastIndexOf(".");
     	if ( index != -1 ) {
     		return name.substring( index + 1, name.length() );
     	} else {
     		return "";
     	}
     }
 
     public final  static String toCamelCase( String name ) {
         return toCamelCase( name, true );
     }
 
     /**
 	* Camelize input string
 	* @param name Input string
 	* @param ucfirst Make first character uppercased
 	* @return String
 	*/
     public final static String toCamelCase( String name, boolean ucfirst ) {
         StringBuilder result = new StringBuilder();
 
         for (int i = 0; i < name.length(); i++) {
             String prevChar = name.substring(i > 0 ? i - 1 : 0, i > 0 ? i : 1);
             String currChar = name.substring(i, i + 1);
 
             if ( camelCaseDelimiters.contains( prevChar ) || ( ucfirst && i == 0 && !camelCaseDelimiters.contains( currChar ) ) ) {
                 result.append( currChar.toUpperCase() );
             } else if ( !camelCaseDelimiters.contains( currChar ) ) {
                 result.append(currChar);
             }
         }
 
         return result.toString();
     }
 
     public final static String fromCamelCase( String name, String delimiter ) {
         StringBuilder result = new StringBuilder();
 
         int last_delimiter_pos = 0;
         for ( int i = 0; i < name.length(); i++ ) {
             String currChar = name.substring(i, i + 1);
 
             if ( currChar.toUpperCase().equals( currChar ) && i != last_delimiter_pos - 1 ) {
                 if ( i > 0 ) {
                     result.append( delimiter );
                     last_delimiter_pos = i;
                 }
 
                 result.append( currChar.toLowerCase() );
             } else {
                 result.append( currChar.toLowerCase() );
             }
         }
 
         return result.toString();
     }
 
     public final static String join( Object[] join, String separator ) {
         return join( Arrays.asList(join), separator );
     }
     
     public final static String join( Collection<?> join, String separator ) {
 		return join( join.toArray(), separator, null );
     }
 
     public final static String join( Collection<?> join, String separator, ILambda<String> filter ) {
         return join( join.toArray( new Object[join.size()] ), separator, filter );
     }
 
     public final static String join( Object[] join, String separator, ILambda<String> filter ) {
         StringBuilder builder = new StringBuilder();
         int i = 0;
         for ( Object joinItem : join ) {
            String value;
             if ( filter != null ) {
                 try {
                     value = filter.invoke( joinItem );
                 } catch ( InvocationException e ) {
                     throw new IllegalArgumentException("Filtering exception", e );
                 }
            } else {
                value = String.valueOf(joinItem);
             }
 
            builder.append(value);
 
             if ( i++ != join.length - 1 ) {
                 builder.append( separator );
             }
         }
 
         return builder.toString();
     }
 
     public final static String formatDuration( int value ) {
         return formatDuration( value, false );
     }
 
     public final static String formatDuration( int value, boolean displayHours ) {
         StringBuilder result = new StringBuilder();
         if ( displayHours ) {
             if ( value > Constants.TIME_HOUR ) {
                 float hours = value / Constants.TIME_HOUR;
                 value -= hours * Constants.TIME_HOUR;
                 if ( hours > 9 ) {
                     result.append( Math.round(hours) );
                 } else {
                     result.append( "0" + Math.round(hours) );
                 }
             } else {
                 result.append("00");
             }
         }
         result.append(":");
 
         if ( value > Constants.TIME_MINUTE  ) {
             float minutes = value / Constants.TIME_MINUTE;
             value -= minutes * Constants.TIME_MINUTE;
             if ( minutes > 9 ) {
                 result.append( Math.round(minutes) );
             } else {
                 result.append("0")
                        .append( Math.round(minutes) );
             }
         } else {
             result.append("00");
         }
         result.append(":");
 
         if ( value >= Constants.TIME_SECOND ) {
             float seconds = value / Constants.TIME_SECOND;
             if ( seconds > 9 ) {
                 result.append(Math.round(seconds));
             } else {
                 result.append("0")
                       .append( Math.round(seconds) );
             }
         } else {
             result.append("00");
         }
 
         return result.toString();
     }
 
     public final static String ucfirst( String value ) {
 		if ( value.isEmpty() ) {
 			return value;
 		}
 
     	return value.substring(0, 1).toUpperCase().concat( value.substring(1) );
     }
 
     public final static String lcfirst( String value ) {
 		if ( value.isEmpty() ) {
 			return value;
 		}
 
     	return value.substring(0, 1).toLowerCase().concat( value.substring(1) );
     }
 
 
     public final static String IPToString( byte[] address ) {
         String[] result = new String[address.length];
         for ( int i = 0; i < address.length; i++ ) {
             result[i] = String.valueOf( address[i] & 0xff );
         }
 
         return SimpleStringUtils.join(result, ".");
     }
 
 	public final static String escape( String orig, String[] escapeSequences ) {
 		return escape(orig, escapeSequences, ESCAPE_SYMBOL );
 	}
 
 	public final static String escape( String orig, String[] escapeSequences, String escapeSymbol ) {
 		if ( orig == null ) {
 			throw new IllegalArgumentException("<null>");
 		}
 
 		String replacement = orig;
 		for ( String sequence : escapeSequences ) {
 			int lastIndex = replacement.indexOf(sequence);
 			while ( lastIndex > 0 && lastIndex !=
 											replacement.indexOf( escapeSymbol, lastIndex - 2) + 1 ) {
 				replacement = replacement.replace( sequence, escapeSymbol + sequence );
 				lastIndex = replacement.indexOf(sequence, lastIndex);
 			}
 		}
 
 		return replacement;
 	}
 
 	public final static String wordWrap( String sentence, int interval, String separator ) {
 		int count = ( sentence.length() / interval );
 		StringBuilder builder = new StringBuilder();
         int total = 0;
         for ( int i = 0; i < count; i++ ) {
             builder.append( sentence.substring(total, total + interval ) )
                    .append( separator );
 
             total += interval;
         }
 
         if ( total != sentence.length() ) {
             builder.append( sentence.substring(total, total + sentence.length() - total ) );
         }
 
 		return builder.toString();
 	}
 
     public final static String toHEX(String str) {
         StringBuilder builder = new StringBuilder();
         for (int i = 0; i < str.length(); i++) {
             byte c = (byte)str.charAt(i);
             StringBuilder hexBuffer = new StringBuilder();
             String tmp = Integer.toHexString(c).toUpperCase();
             for (int j = 0; j < 2 - tmp.length(); j++) {
                 hexBuffer.append("0");
             }
             
             builder.append( hexBuffer.toString() );
         }
 
         return builder.toString();
     }
     
 }
