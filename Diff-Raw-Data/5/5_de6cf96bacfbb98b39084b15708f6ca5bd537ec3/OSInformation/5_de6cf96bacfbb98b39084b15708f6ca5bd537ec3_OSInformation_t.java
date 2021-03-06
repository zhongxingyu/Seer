 package aQute.bnd.osgi;
 
 import java.util.regex.*;
 
 import aQute.bnd.version.*;
 
 /**
  * OS specific information, used by the native_capability macro for
  * osgi.native.* bundle properties.
  */
 public class OSInformation {
 	String					osnames							= null;
 	Version					osversion						= null;
 
 	static private String	regexQualifierNotAllowedChars	= "[^\\p{Alnum}-_]";
 	static private Pattern	digitPattern					= Pattern.compile("(\\d+).*");
 
 	/**
 	 * <p>
 	 * Convert a generic Unix kernel version to an OSGi version.
 	 * </p>
 	 * <p>
 	 * As long as we have digits separated by dots, convert the digits into the
 	 * respective version segments. Anything left after that conversion is the
 	 * qualifier. Illegal characters in that qualifier are converted into
 	 * underscores to ensure that the final qualifier is valid.
 	 * </p>
 	 * 
 	 * @param sysPropOsVersion
 	 *            the system property "os.version"
 	 */
 	void convertUnixKernelVersion(String sysPropOsVersion) {
 		osversion = new Version(0, 0, 0);
 		String s = sysPropOsVersion.trim();
 		int index = 0;
 		do {
 			Matcher matcher = digitPattern.matcher(s);
 			if (matcher.matches()) {
 				String matchedDigit = matcher.group(1);
 				int matchedDigitNumber;
 				try {
 					matchedDigitNumber = Integer.parseInt(matchedDigit);
 				}
 				catch (NumberFormatException e) {
 					assert (false);
 					break;
 				}
 
 				switch (index) {
 					case 0 :
 						osversion = new Version(matchedDigitNumber, osversion.getMinor(), osversion.getMicro());
 						break;
 
 					case 1 :
 						osversion = new Version(osversion.getMajor(), matchedDigitNumber, osversion.getMicro());
 						break;
 
 					case 2 :
 						osversion = new Version(osversion.getMajor(), osversion.getMinor(), matchedDigitNumber);
 						break;
 
 					default :
 						assert (false);
 						break;
 				}
 
 				s = s.substring(matchedDigit.length());
 
 				if (s.length() == 0 || s.charAt(0) != '.') {
 					break;
 				}
 
 				s = s.substring(1);
 
 				index++;
 			}
 		} while (index < 3);
 
 		if (s.length() != 0) {
 			String qualifier = s.replaceAll(regexQualifierNotAllowedChars, "_");
 			osversion = new Version(osversion.getMajor(), osversion.getMinor(),osversion.getMicro(),  qualifier);
 		}
 	}
 
 	/**
 	 * Construct OS specific information
 	 * 
 	 * @throws IllegalArgumentException
 	 */
 	public OSInformation() throws IllegalArgumentException {
 		String sysPropOsName = System.getProperty("os.name");
 		String sysPropOsVersion = System.getProperty("os.version");
 
 		if (sysPropOsName.startsWith("Windows")) {
 			if (sysPropOsVersion.startsWith("6.2")) {
 				osversion = new Version(6, 2, 0);
 				osnames = "Windows8,Windows 8,Win32";
 			} else if (sysPropOsVersion.startsWith("6.1")) {
 				osversion = new Version(6, 1, 0);
 				osnames = "Windows7,Windows 7,Win32";
			} else if (sysPropOsVersion.startsWith("6.0")) {
 				osversion = new Version(6, 0, 0);
 				osnames = "WindowsVista,WinVista,Windows Vista,Win32";
			} else if (sysPropOsVersion.startsWith("5.1")) {
 				osversion = new Version(5, 1, 0);
 				osnames = "WindowsXP,WinXP,Windows XP,Win32";
 			} else {
 				throw new IllegalArgumentException(
 						String.format(
 								"Unrecognised or unsupported Windows version while processing ${native_capability} macro: %s version %s. Supported: XP, Vista, Win7, Win8.",
 								sysPropOsName, sysPropOsVersion));
 			}
 
 			return;
 		}
 
 		if (sysPropOsName.startsWith("Mac OS X")) {
 			convertUnixKernelVersion(sysPropOsVersion);
 			osnames = "MacOSX,Mac OS X";
 			return;
 		}
 
 		if (sysPropOsName.toLowerCase().startsWith("linux")) {
 			convertUnixKernelVersion(sysPropOsVersion);
 			osnames = "Linux";
 			return;
 		}
 
 		if (sysPropOsName.startsWith("Solaris")) {
 			convertUnixKernelVersion(sysPropOsVersion);
 			osnames = "Solaris";
 			return;
 		}
 
 		if (sysPropOsName.startsWith("AIX")) {
 			convertUnixKernelVersion(sysPropOsVersion);
 			osnames = "AIX";
 			return;
 		}
 
 		if (sysPropOsName.startsWith("HP-UX")) {
 			convertUnixKernelVersion(sysPropOsVersion);
 			osnames = "HPUX,hp-ux";
 			return;
 		}
 
 		throw new IllegalArgumentException(
 				String.format(
 						"Unrecognised or unsupported OS while processing ${native_capability} macro: %s version %s. Supported: Windows, Mac OS X, Linux, Solaris, AIX, HP-UX.",
 						sysPropOsName, sysPropOsVersion));
 	}
 }
