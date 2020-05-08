 package us.exultant.ahs.terminal.exe;
 
 import us.exultant.ahs.util.*;
 import us.exultant.ahs.terminal.*;
 import us.exultant.ahs.terminal.Terminal.Color;
import us.exultant.ahs.terminal.gui.*;
 import java.io.*;
 
 public class Test {
 	private static final Terminal T = StandardTerminal.get();
 	public static void main(String... args) {
		try {
			main();
		} catch (Throwable $t) {
			Stty.releaseControl();
			$t.printStackTrace();
		}
	}
	
	public static void main() {
 		final int $w = T.getWidth();
 		final int $h = T.getHeight();
		final Window $window = new Window($w,$h);
		final Palette $p_clear = new Palette(Color.CLEAR);
		final Palette $p_error = new Palette(Color.WHITE, Color.RED, true, true);
 		
 		// make a giant freakin' box
		$window.print(0, 0, BoxChars.NSDN+Strings.repeat(BoxChars.NSNS, $w-2)+BoxChars.NNDS, $p_clear);
		for (int $i = 1; $i < $h-1; $i++) {
			$window.print(0, $i, BoxChars.DNDN+Strings.repeat(' ', $w-2)+BoxChars.DNDN, $p_clear);
 		}
		$window.print(0, $h-1, BoxChars.DSNN+Strings.repeat(BoxChars.NSNS, $w-2)+BoxChars.DNNS, $p_clear);
		$window.render(T);
		
		
		
 		T.cursor().place(0, 3);
 		final int $inset = 3;
 		
 		// print top line, listing the background colors
 		T.cursor().shiftRight($inset);
 		T.print(Strings.padLeftToWidth("",' ',13));
 		for (Color $bg : Color.values()) {
 			T.setPalette(new Palette(Color.CLEAR, $bg, true, true));
 			T.print(Strings.padRightToWidth("  bg: "+$bg+" ",14));
 		}
 		
 		T.cursor().lineNext(1);
 		T.cursor().shiftRight($inset);
 		for (Color $fg : Color.values()) {
 			// print front of line, listing the forground color
 			T.setPalette(new Palette($fg, Color.CLEAR, true, null));
 			T.print(Strings.padLeftToWidth(" fg: "+$fg+" ",13));
 			
 			// print the table body
 			for (Color $bg : Color.values()) {
 				T.setPalette(new Palette($fg, $bg));
 				T.print("     word     ");
 			}
 			T.cursor().lineNext(1);
 			T.cursor().shiftRight($inset);
 		}
 		
 		T.setPalette(new Palette(Color.CLEAR));
 		
 		
 		
 		Reader $r = System.console().reader();
 		char[] $cbuf = new char[1];
 		try {
 			while (true) {
 				if ($r.ready()) {
 					$r.read($cbuf);
 					switch ($cbuf[0]) {
 						// a bunch of these are just too weird for us to ever think about echoing
 						case 0x00:	// NUL
 						case 0x01:	// SOH
 						case 0x02:	// STX
 						case 0x03:	// ETX
 						case 0x04:	// EOT
 						case 0x05:	// ENQ
 						case 0x06:	// ACK
 						case 0x07:	// BEL
 						case 0x08:	// BS
 						case 0x09:	// HT
 						case 0x0B:	// VT
 						case 0x0C:	// FF
 						case 0x0E:	// SO
 						case 0x0F:	// SI
 						case 0x10:	// DLE
 						case 0x11:	// DC1
 						case 0x12:	// DC2
 						case 0x13:	// DC3
 						case 0x14:	// DC4
 						case 0x15:	// NAK
 						case 0x16:	// SYN
 						case 0x17:	// ETB
 						case 0x18:	// CAN
 						case 0x19:	// EM
 						case 0x1A:	// SUB
 						case 0x1C:	// FS
 						case 0x1D:	// GS
 						case 0x1E:	// RS
 						case 0x1F:	// US
 						
 						// these we might usually just echo, but will certainly have to watch carefully:
 						case 0x1B:	// ESC
 							while (true) {
 								if (!$r.ready()) continue;
 								switch ($r.read()) {
 									case 'A':
 										T.cursor().shiftUp(1);
 								}
 								break;
 							}
 						case 0x0D:	// CR
 						case 0x0A:	// LF
 							T.print("*");
 							break;
 						case 0x7F:	// DEL
 						case 0x20:	// SPACE
 						default:
 							T.print("\r");
 							T.cursor().shiftRight(2);
 							T.print(new String($cbuf));
 						// in general, we'd do well to know what printing a character is going to do to our position on the screen, and as long as we have that we'd be fine.  a little easier said than done, though -- i think i'm going to ignore it for the forseeable future, and as long as i design the text boxes in the terminal.gui subpackage correctly, i should be able to implement it transparently later without breaking anything legacy.
 					}
 					// hectic bits:
 					//   - you can get more than one char of input at a time if someone copy-pastes in, and that seems to cause trouble.
 					//   - erm, LOTS of stuff starts with an escape code.  like arrow keys.  which show up funny if you don't do the single-char reporting switch quite right, but cause exit with what i've written right now.
 				}
 			}
 		} catch (IOException $e) {
 			$e.printStackTrace();
 		}
 		
 		StandardTerminal.get().clear();
 	}
 }
