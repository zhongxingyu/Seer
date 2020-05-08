 package kinect;
 
 
 import processing.core.PConstants;
 
 public class KinectConfig {
 	
 	public static final int MODE_Normal		 	= 0;
 	public static final int MODE_StdDevAdjust	= 1;
 	public static final int MODE_RangeAdjust	= 2;
 	public static final int MODE_Test			= 4;
 	
 	public int mode;
 	public boolean showBackground;
 	public RunningStat[] stats;
 	
 	private Kinect kinect;
 	
 	public KinectConfig(Kinect kinect) {
 		this.kinect = kinect;
 		reset();
 		
 	}
 	
 	public void reset() {
 		mode = 0;
		kinect.filter = false;
 		showBackground = false;
 	}
 
 	public void keyPressed(int key, int keyCode) {
 		
 		if(keyCode == PConstants.UP) {
 			
 			if(mode == MODE_StdDevAdjust) {
 				kinect.filterThreshold *= 1.5;
 				System.out.printf("std-dev=%f\n", kinect.filterThreshold);
 			}
 			if(mode == MODE_RangeAdjust) {
 				kinect.range += kinect.rangeSize/4;
 				System.out.printf("range=[%d,%d]\n", kinect.range, kinect.range+kinect.rangeSize);
 			}
 			
 		} else if(keyCode == PConstants.DOWN) {
 			
 			if(mode == MODE_StdDevAdjust) {
 				kinect.filterThreshold /= 1.5;
 				System.out.printf("std-dev=%f\n", kinect.filterThreshold);
 			}
 			if(mode == MODE_RangeAdjust) {
 				kinect.range -= kinect.rangeSize/4;
 				System.out.printf("range=[%d,%d]\n", kinect.range, kinect.range+kinect.rangeSize);
 			}
 			
 		} else if(keyCode == PConstants.RIGHT) {
 			mode = MODE_StdDevAdjust;
 			System.out.println("StdDev Adjust");
 		
 		} else if(keyCode == PConstants.LEFT) {
 			mode = MODE_RangeAdjust;
 			System.out.println("Range Adjust");
 			
 		} else if(keyCode == PConstants.ESC) {
 			reset();
 			System.out.println("Reset");
 		} else {
 			
 			switch(key) {
 			case 'h':
 			case 'H':
 				printHelp();
 				break;
 			case 'r':
 			case 'R':
 				reset();
 				System.out.println("Reset");
 				break;
 			case 'b':
 			case 'B':
 			case '5':
 				showBackground = !showBackground;
 				System.out.println(showBackground ? "rangeBg=ON" : "rangeBg=OFF");
 			case 'f':
 			case 'F':
 			case '4':
 				kinect.filter = !kinect.filter;
 				System.out.println( kinect.filter ? "Filter=ON" : "Filter=OFF" );
 				kinect.refreshGoodPixels(stats);
 				break;
 			case '6':
 			case 't':
 			case 'T':
 				mode = MODE_Test;
 				System.out.println("test mode");
 				break;
 			case 'n':
 			case 'N':
 			case '0':
 				mode = MODE_Normal;
 				System.out.println("Normal, blocking bad pixels");
 				break;
 			case '1':
 				mode = MODE_StdDevAdjust;
 				System.out.println("StdDev Adjust");
 				break;
 			case '2':
 				mode = MODE_RangeAdjust;
 				System.out.println("Range Adjust");
 				break;
 			case '+':
 			case '=':
 				if( mode == MODE_RangeAdjust ) {
 					kinect.rangeSize *= 1.5;
 					System.out.printf("range=[%d,%d]\n", kinect.range, kinect.range+kinect.rangeSize);
 				}
 				break;
 			case '-':
 			case '_':
 				if( mode == MODE_RangeAdjust ) {
 					kinect.rangeSize /= 1.5;
 					if( kinect.rangeSize < 5 ) kinect.rangeSize = 5;
 					System.out.printf("range=[%d,%d]\n", kinect.range, kinect.range+kinect.rangeSize);
 				}
 				break;
 			}
 		}
 
 
 	}
 
 	private void printHelp() {
 		System.out.println("" +
 				"--------------------------------------------\n" +
 				"KinectConfig  [ bernhard.todd@gmail.com ] \n" +
 				"0,N   -> Normal Mode\n" +
 				"1,>>  -> Adjust StdDev Threshold\n" +
 				"2,<<  -> Adjust Range\n" +
 				"3,A   -> Adjust Range alpha channel\n" +
 				"4,F   -> Toggles filter\n" +
 				"         (ignores pixels > StdDevThresh)\n" +
 				"5,B   -> Toggles rendering over backround in Range\n" +
 				"\n" +
 				"in <Adjust> modes use, Up / Down to change,\n" +
 				"in <Adjust Range> mode, use +/- to \"zoom\"\n" +
 				"\n" +
 				"R     -> Reset\n" +
 				"H,    -> this menu\n" +
 				"ESC -> EXIT the sim ");
 		
 	}
 }
