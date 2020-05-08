 package ee.alkohol.juks.sirvid.math;
 
 import java.util.HashMap;
 
 /**
  * Generic astronomical and calendar calculations
  * 
  * Most math is taken from:
  * Meeus, Jean. 
  * Astronomical algorithms. 
  * Willmann-Bell Inc., Richmond, 1991. 
 * ISBN 0943396352
  * 
  * Years here are astronomical, so 1 BC is here year 0, 2 BC is year -1 and so on. 
  * Note: Julian Day (Number) or JD(N) is continous count of days and their fractions from 
  * beginning of the year -4172. By tradition, JD begins at Greenwich mean noon, that is, 
  * at 12h Universal Time. 
  * Gregorian calendar reform: 1582-10-04 (in Julian calendar) is followed by 1582-10-15 (in Gregorian calendar).
  * 
  * @author juks@alkohol.ee 
  */
 
 public class Astronomy {
     
     public static boolean dynamicTime = true;
     public static final double[] MONTH_DAYS = {31,28.25,31,30,31,30,31,31,30,31,30,31};
     public static final double YEAR_DAYS = 365.25;
     public static final long UNIX_EPOCH_JD = 2440588;
     public static final int SECONDS_IN_DAY = 86400;
     public static final long JDN2000_01_01 = 2451545;
     public static final long FIRST_GREGORIAN_DATE = 15821015;
 
 
     public static final double[][] MF_PLANETARY = {
         {299.77, 0.107408, 0.325},
         {251.88, 0.016321, 0.165},
         {251.83, 26.651886, 0.164},
         {349.42, 36.412478, 0.126},
         {84.66, 18.206239, 0.11},
         {141.74, 53.303771, 0.062},
         {207.14, 2.453732, 0.06},
         {154.84, 7.30686, 0.056},
         {34.52, 27.261239, 0.047},
         {207.19, 0.121824, 0.042},
         {291.34, 1.844379, 0.04},
         {161.72, 24.198154, 0.037},
         {239.56, 25.513099, 0.035},
         {331.55, 3.592518, 0.023},
     };
 
     public static final double[][] MF_CORRECT_MUL = {
         {-0.4072, -0.40614, -0.62801},
         {0.17241, 0.17302, 0.17172},
         {0.01608, 0.01614, -0.01183},
         {0.01039, 0.01043, 0.00862},
         {0.00739, 0.00734, 0.00804},
         {-0.00514, -0.00515, 0.00454},
         {0.00208, 0.00209, 0.00204},
         {-0.00111, -0.00111, -0.0018},
         {-0.00057, -0.00057, -0.0007},
         {0.00056, 0.00056, -0.0004},
         {-0.00042, -0.00042, -0.00034},
         {0.00042, 0.00042, 0.00032},
         {0.00038, 0.00038, 0.00032},
         {-0.00024, -0.00024, -0.00028},
         {-0.00017, -0.00017, 0.00027},
         {-0.00007, -0.00007, -0.00017},
         {0.00004, 0.00004, -0.00005},
         {0.00004, 0.00004, 0.00004},
         {0.00003, 0.00003, -0.00004},
         {0.00003, 0.00003, 0.00004},
         {-0.00003, -0.00003, 0.00003},
         {0.00003, 0.00003, 0.00003},
         {-0.00002, -0.00002, 0.00002},
         {-0.00002, -0.00002, 0.00002},
         {0.00002, 0.00002, -0.00002}
     };
 
     public static final short[][] MF_CORRECT_POW = {
         {0, 0},
         {1, 1},
         {0, 1},
         {0, 0},
         {1, 0},
         {1, 1},
         {2, 2},
         {0, 0},
         {0, 0},
         {1, 0},
         {0, 1},
         {1, 1},
         {1, 1},
         {1, 2},
         {0, 1},
         {0, 0},
         {0, 0},
         {0, 0},
         {0, 0},
         {0, 0},
         {0, 0},
         {0, 0},
         {0, 0},
         {0, 0},
         {0, 0}
     };
     
     // Meeus pp. 166
     public static final double[][] SOLSTICE_J = {
         {1721139.29189, 365242.1374, 0.06134, 0.00111, -0.00071},
         {1721233.25401, 365241.72562, -0.05323, 0.00907, 0.00025},
         {1721325.70455, 365242.49558, -0.11677, -0.00297, 0.00074},
         {1721414.39987, 365242.88257, -0.00769, -0.00933, -0.00006},
         {2451623.80984, 365242.37404, 0.05169, -0.00411, -0.00057},
         {2451716.56767, 365241.62603, 0.00325, 0.00888, -0.0003},
         {2451810.21715, 365242.01767, -0.11575, 0.00337, 0.00078},
         {2451900.05952, 365242.74049, -0.06223, -0.00823, 0.00032}
     };
 
     // Meeus pp. 167
     public static final double[][] SOLSTICE_S = {
         {485, 324.96, 1934.136},
         {203, 337.23, 32964.467},
         {199, 342.08, 20.186},
         {182, 27.85, 445267.112},
         {156, 73.14, 45036.886},
         {136, 171.52, 22518.443},
         {77, 222.54, 65928.934},
         {74, 296.72, 3034.906},
         {70, 243.58, 9037.513},
         {58, 119.81, 33718.147},
         {52, 297.17, 150.678},
         {50, 21.02, 2281.226},
         {45, 247.54, 29929.562},
         {44, 325.15, 31555.956},
         {29, 60.93, 4443.417},
         {18, 155.12, 67555.328},
         {17, 288.79, 4562.452},
         {16, 198.04, 62894.029},
         {14, 199.76, 31436.921},
         {12, 95.39, 14577.848},
         {12, 287.11, 31931.756},
         {12, 320.81, 34777.259},
         {9, 227.73, 1222.114},
         {8, 15.45, 16859.074}
     };
     
     public static final class Keys {
         public static final String J_SET = "Jset";
         public static final String J_RISE = "Jrise";
     }
 
     /**
      * Gregorian Easter 
      * 
      * Meeus, pp. 67-68. 
      * Unlike Gauss formula, this method is valid for all years in Gregorian calendar, hence since 1583. 
      * Recurrence cycle is 5 700 000 years, most frequent Gregorian easter date is April 19. 
      * Extremes examples: min March 22 (as in 1818 and 2285), max April 25 (as in 1886, 1943 and 2038). 
      * 
      * @param year
      * @return Date of Gregorian Easter in given year as {month, day}
      */
     
     public static int[] gregorianEaster(int year) {
 
         int a = year % 19;
         int b = (int)Math.floor(year / 100);
         int c = year % 100;
         int d = (int)Math.floor(b / 4);
         int e = b % 4;
         int f = (int)Math.floor((b +8) / 25);
         int g = (int)Math.floor((b -f +1) / 3);
         int h = (19*a +b -d -g +15) % 30;
         int i = (int)Math.floor(c / 4);
         int k = c % 4;
         int l = (2*e + 2*i -h -k +32) % 7;
         int m = (int)Math.floor((a +11*h +22*l) / 451);
         int t = h +l -7*m +114;
 
         int[] y = {(int)Math.floor(t / 31), (t % 31)+1};
         return y;
     }
 
     /**
      * Julian Easter 
      * 
      * Meeus, pp. 69.
      * Recurrence cycle 532 years.  
      * 
      * @param year
      * @return Date of Julian Easter in given year as {month, day}
      */
     
     public static int[] julianEaster(int year) {
 
         int a = year % 4;
         int b = year % 7;
         int c = year % 19;
         int d = (19*c + 15) % 30;
         int e = (2*a +4*b -d +34) % 7;
         int t = d +e +114;
 
         int[] y = {(int)Math.floor(t / 31), (t % 31)+1};
         return y;
     }
 
     /**
      * Difference between Universal Time and Dynamic Time 
      * 
      * Meeus, pp. 71-75. 
      * Calculates time difference between Greenwich Civil Time (also: Universal Time, UT) and Dynamic Time (DT).
      * DT is more accurate because it takes into account Earth rotations slowdown and other irregularities.
      * 
      * @param year
      * @return UT vs. DT difference in given year
      */
     
     public static double JDE2UTdT(double year) {
         double y;
         double T = (year -2000) / 100;
         double Tp2 = T*T;
         if(year >1600) {
             y = 102.3 +123.5*T +32.5*Tp2;
         } else if (year >947) {
             y = 50.6 +67.5*T +22.5*Tp2;
         } else {
             y = 2715.6 +573.36*T +46.5*Tp2;
         }
         return y;
     }
     
     /**
      * Calculate solstices 
      * 
      * Meeus, pp. 165-170. 
      * Is aproximately true between years -1000 and +3000. 
      * 
      * @param year
      * @param month
      * @param dynamicTime
      * @return Julian Date of given solstice
      */
     
     public static double solstice(long year, short month, boolean dynamicTime) {
         
         short sol;
         double Y;
         
         if(year > 1000) {
             sol = 4;
             Y = (double)(year -2000) /1000;
         } else {
             sol = 0;
             Y = (double)year / 1000;
         }
         sol += Math.floor(month/3) -1;
         double Yp2 = Y*Y;
         
         double JDE0 = SOLSTICE_J[sol][0]
             +SOLSTICE_J[sol][1]*Y
             +SOLSTICE_J[sol][2]*Yp2
             +SOLSTICE_J[sol][3]*Yp2*Y
             +SOLSTICE_J[sol][4]*Yp2*Yp2;
         
         double T = (JDE0 -JDN2000_01_01) / 36525;
         double W = 35999.373*T -2.47;
         double dL = 1 +.0334*Math.cos(deg2rad(W)) +.0007*Math.cos(deg2rad(2*W));
         
         short cycleLen = (short)SOLSTICE_S.length;
         short i;
         double S = 0;
         for(i=0; i<cycleLen; i++) {
             S += SOLSTICE_S[i][0] *Math.cos(deg2rad(SOLSTICE_S[i][1] +SOLSTICE_S[i][2]*T));
         }
 
         double JDE = JDE0 + ((.00001*S) /dL);
         double dT = getDT(JDE, dynamicTime);
         return JDE -dT;
     }
     
     /**
      * Calculate lunation number
      * 
      * Meeus pp. 320. 
      * k is the lunation number. partial lunations correspond to partial phases. 
      * Algorithm is valid only for 1/4 parts of a lunation.
      * Approximate value for k is (year - 2000) * 12.3685 .
      * Find the lunation (new moon) closest to date X.
      * This is often not accurate enough!
      * 
      * @param year
      * @param month
      * @param phase 0 - new, 1 - first quarter, 2 - full, 3 - last quarter
      * @return k - lunation number ( k - int(k) : 0 - new moon; 0.25 - first quarter; 0.5 - full moon; 0.75 - last quarter)
      */
     public static double getLunationNumber(long year, short month, short phase) {
     	return Math.floor(((double)year +yearFrac(month, 0.5) -2000) *12.3685) + 0.25 * phase;
     }
     
     /**
      * Calculate Moon phases using lunation number 
      * 
      * Meeus, pp. 319-324. 
      * Use it through {@link #moonPhaseCorrected(long, short, short)}
      * 
      * @param lunation number from {@link #getLunationNumber(long, short, short)}
      * @param phase 0 - new, 1 - first quarter, 2 - full, 3 - last quarter
      * @param dynamicTime
      * @return nearest requested phase moment as Julian Date
      */
     public static double moonPhaseK(double k, short phase, boolean dynamicTime) {
         
         short cycleLen;
         short i;
         double temp;
         
         double T = k/1236.85; // time in Julian centuries since the epoch 2000
         double Tp2 = T*T;
         double Tp3 = Tp2*T;
         double Tp4 = Tp2*Tp2;
         double E = 1 -.002516*T -.0000074*Tp2; // Meeus, pp. 308
         double Ep2 = E*E;
         
         double JDE = 2451550.09765
             +29.530588853*k
             +.0001337*Tp2
             -.00000015*Tp3
             +.00000000073*Tp4;
         double M = deg2rad(2.5534 
             +29.10535669*k 
             -.0000218*Tp2 
             -.00000011*Tp3); // Suns mean anomaly
         double M1 = deg2rad(201.5643 
             +385.81693528*k 
             +.0107438*Tp2 
             -.00001239*Tp3 
             -.000000058*Tp4); // Moons mean anomaly
         double F = deg2rad(160.7108 
             +390.67050274*k 
             -.0016341*Tp2 
             -.00000227*Tp3 
             +.000000011*Tp4); // Moons argument of latitude
         double O = deg2rad(124.7746 
             -1.56375580*k 
             +.0020691*Tp2 
             +.00000215*Tp3);
         double W = 0;
 
         cycleLen = (short)MF_CORRECT_MUL.length;
         double correction = 0;
         
         if(phase == 0 || phase == 2) { // New or full moon
             short correctionCol = (short)(phase == 2 ? 1 : 0);
             double[] sr = {
                 M1,
                 M,
                 2*M1,
                 2*F,
                 M1-M,
                 M1+M,
                 2*M,
                 M1-2*F,
                 M1+2*F,
                 2*M1+M,
                 3*M1,
                 M+2*F,
                 M-2*F,
                 2*M1-M,
                 O,
                 M1+2*M,
                 2*M1-2*F,
                 3*M,
                 M1+M-2*F,
                 2*M1+2*F,
                 M1+M+2*F,
                 M1-M+2*F,
                 M1-M-2*F,
                 3*M1+M,
                 4*M1
             };
             for(i=0; i<cycleLen; i++) {
                 temp = MF_CORRECT_MUL[i][correctionCol] *Math.sin(sr[i]);
                 if(MF_CORRECT_POW[i][0] == 1) {
                     temp *= E;
                 } else if(MF_CORRECT_POW[i][0] == 2) {
                     temp *= Ep2;
                 }
                 correction += temp;
             }
         } else {
             double[] sr = {
             M1,
             M,
             M1+M,
             2*M1,
             2*F,
             M1-M,
             2*M,
             M1-2*F,
             M1+2*F,
             3*M1,
             2*M1-M,
             M+2*F,
             M-2*F,
             M1+2*M,
             2*M1+M,
             O,
             M1-M-2*F,
             2*M1+2*F,
             M1+M+2*F,
             M1-2*M,
             M1+M-2*F,
             3*M,
             2*M1-2*F,
             M1-M+2*F,
             3*M1+M
             };
             for(i=0; i<cycleLen; i++) {
                 temp = MF_CORRECT_MUL[i][2] *Math.sin(sr[i]);
                 if(MF_CORRECT_POW[i][1] == 1) {
                     temp *= E;
                 } else if(MF_CORRECT_POW[i][1] == 2) {
                     temp *= Ep2;
                 }
                 correction += temp;
             }
             W = 306
                 -38*E*Math.cos(M)
                 +26*Math.cos(M1)
                 -2*Math.cos(M1 -M)
                 +2*Math.cos(M1 +M)
                 +2*Math.cos(2*F);
             W /= 100000;
             if(phase == 3) { W = -W; }
         }
 
         cycleLen = (short)MF_PLANETARY.length;
         temp = -.009173 *Tp2;
         double correctionPlanetary = 0;
         for(i=0; i<cycleLen; i++) {
             temp += MF_PLANETARY[i][0] + MF_PLANETARY[i][1] * k;
             correctionPlanetary +=  MF_PLANETARY[i][2]*Math.sin(deg2rad(temp));
             temp = 0;
         }
         correctionPlanetary /= 1000;
 
         double dT = getDT(JDE +correction +correctionPlanetary +W, dynamicTime);
         return JDE +correction +correctionPlanetary +W -dT;
     }
     
     /**
      * Calculate Moon phases 
      * 
      * Meeus, pp. 319-324. 
      * Use it through {@link #moonPhaseCorrected(long, short, short)}
      * 
      * @param year
      * @param month
      * @param phase 0 - new, 1 - first quarter, 2 - full, 3 - last quarter
      * @param dynamicTime
      * @return nearest requested phase moment in Julian Date
      * @see <a href="http://www.webmasterworld.com/foo/3999789.htm">Moon Phases calculator in VBScript</a>
      */
     
     public static double moonPhase(long year, short month, short phase, boolean dynamicTime) {
         return moonPhaseK( getLunationNumber(year, month, phase), phase, dynamicTime);
     }
     
     /**
      * Recalculate moon phase, if you missed right month 
      * 
      * Sometimes {@link #moonPhase(long, short, short)} returns value which is one month earlier or later.
      * 
      * @param year
      * @param month
      * @param phase 0 - new, 1 - first quarter, 2 - full, 3 - last quarter
      * @param dynamicTime
      * @return Julian Date of given phase
      * @see {@link #moonPhase(long, short, short)}
      */
     
     public static double moonPhaseCorrected(long year, short month, short phase, boolean dynamicTime) {
         
         long desiredMonth = 12*year + month -1;
         long currentMonth = desiredMonth;
         short m;
         int ctl = 2; // cycles to live
         double moonF = 0;
         int[] moonFGregorian;
         long calculatedMonth;
         
         while(true) {
             if(ctl < 1) break;
             m = (short)(currentMonth%12);
             moonF = moonPhase((long)((currentMonth-m) / 12), (short)(m+1), phase, dynamicTime);
             moonFGregorian = Astronomy.JDN2gregorianDate(moonF);
             calculatedMonth = 12*moonFGregorian[0] + moonFGregorian[1] -1;
             if (desiredMonth == calculatedMonth) {
                 break;
             } else if (desiredMonth <  calculatedMonth) {
                 currentMonth--;
             } else {
                 currentMonth++;
             }
             ctl --;
         }
         
         return moonF;
     }    
     
     /**
      * Convert Julian Day to Gregorian Date 
      * 
      * NB! Method does not work on negative JD! 
      * Meeus, pp. 63. 
      * 
      * @param JD Julian Day
      * @return Gregorian date (and UTC time) as {year,month,day,hour,minute,second}
      */
     
     public static int[] JDN2gregorianDate(double JD) {
         int Z = (int)Math.floor(JD +.5);
         double F = JD +.5 -Z;
         int A;
         if(Z < 2299161) {
             A = Z;
         } else {
             int alfa = (int)Math.floor((Z -1867216.25)/36524.25);
             A = Z +1 +alfa -(int)Math.floor(alfa/4);
         }
 
         int B = A +1524;
         int C = (int)Math.floor((B -122.1)/YEAR_DAYS);
         int D = (int)Math.floor(YEAR_DAYS*C);
         int E = (int)Math.floor((B -D)/30.6001);
         int m = (E < 14) ? E -1 : E -13;
 
         int[] y = {
             m > 2 ? C -4716 : C -4715,
             m,
             B -D -(int)Math.floor(30.6001*E),
             0,
             0,
             0
         };
 
         short[] div = {24,60,60};
         for(int i = 0; i < 3; i++) {
             F *= div[i];
             y[3+i] = (int)Math.floor(F);
             F -= y[3+i];
         }
         y[5] += (int)Math.round(F);
         return y;
     }
     
     /**
      * Julian Day's dynamic time correction
      * 
      * @param JD Julian Day
      * @param dynamicTime
      * @return correction
      */
     
     public static double getDT(double JD, boolean dynamicTime) {
         if(dynamicTime) {
             int[] jTime = JDN2gregorianDate(JD);
             return JDE2UTdT((double)jTime[0] +yearFrac((short)jTime[1],jTime[2])) / SECONDS_IN_DAY;
         } else {
             return 0;
         }
     }
     
     /**
      * Find fractional part for year from date
      * 
      * @param month
      * @param monthDay day of month
      * @return year's fractional part
      */
     
     public static double yearFrac(short month, double monthDay) {
         double y = 0;
         short i;
         for(i=0; i<12; i++) {
             if(month == i+1) {
                 if(monthDay >= 0 && monthDay < 1) {
                     y += MONTH_DAYS[i] * monthDay;
                 } else { // hey, no babysitting here!
                     y += monthDay;
                 }
                 break;
             }
             y += MONTH_DAYS[i];
         }
         return y / YEAR_DAYS;
     }
     
     /**
      * Convert degrees to radians
      * 
      * @param deg degrees
      * @return radians
      */
     
     public static double deg2rad(double deg) {
         return (deg -360*Math.floor(deg/360)) *Math.PI /180;
     }
     
     /**
      * Convert Gregorian date to Julian Day Number 
      * 
      * @param year
      * @param month
      * @param day
      * @return Julian Day Number
      * @see <a href="http://en.wikipedia.org/wiki/Julian_Day#Converting_Gregorian_calendar_date_to_Julian_Day_Number">Converting Gregorian calendar date to Julian Day Number</a>
      */
     
     public static double gregorianDate2JDN(int year, int month, int day) {
         int a = (int)Math.floor((14 -month) / 12);
         int y = year + 4800 -a;
         int m = month + 12*a -3;
         return day + Math.floor((153*m +2)/5)  + 365*y + Math.floor(y/4)
                 + (year*10000 + month*100 + day >= FIRST_GREGORIAN_DATE ? -Math.floor(y/100) + Math.floor(y/400) -32045 : -32083);
     }
     
     /**
      * Convert Gregorian date to Julian Day Number 
      * 
      * @param year
      * @param month
      * @param day
      * @return Julian Day Number
      */
     public static double gregorianDate2JDN(int year, int month, double day) {
     	if(month < 3) {
     		year--;
     		month += 12;
     	}
     	int A = (int)Math.floor(year/100);
     	int B = year*10000 + month*100 + day >= FIRST_GREGORIAN_DATE ? B = 2 - A + (int)Math.floor(A/4) : 0;
     	return Math.floor(YEAR_DAYS * (year + 4716)) 
     		+ Math.floor(30.6001 * (month + 1))
     		+ day + B - 1524.5;
     }
     
     /**
      * Calculate sunrise and sunset time for given day and location
      * 
      * It doesn't use dynamicTime. 
      * 
      * @param Jdate Julian Day of day for which sunrise and sunset is calculated
      * @param lw longitude west (west is positive, east is negative) of the observer on the Earth
      * @param ln north latitude of the observer (north is positive, south is negative) on the Earth
      * @return hashmap consisting Julian Days of sunrise (Jrise) and sunset (Jset)
      * @see <a href="http://en.wikipedia.org/wiki/Sunrise_equation#Complete_calculation_on_Earth">Sunrise and Sunset calculations for Earth</a>
      * @see <a href="http://users.electromagnetic.net/bu/astro/sunrise-set.php">Calculate Sunrise/Sunset</a>
      */
     
     public static HashMap<String,Double> gregorianSunrise(double Jdate, double lw, double ln) {
         
         // Calculate current Julian Cycle
         double np = Jdate -JDN2000_01_01 - 0.0009 - (lw/360);
         double n = Math.round(np); // n is the Julian cycle since Jan 1st, 2000
         
         
         // Approximate Solar Noon
         double Jp = JDN2000_01_01 + 0.0009  + (lw/360) + n; // an approximation of solar noon at lw
         
         // Solar Mean Anomaly
         double M = 357.5291 + 0.98560028 * (Jp - JDN2000_01_01);
         M -= Math.floor(M/360)*360;
         
         // Equation of Center
         double C = 1.9148 * Math.sin(deg2rad(M)) + 0.02 * Math.sin(deg2rad(2*M)) + 0.0003 * Math.sin(deg2rad(3*M));
         
         // Ecliptic Longitude
         double lambda = M + 102.9372 + C + 180;
         lambda -= Math.floor(lambda/360)*360;
         
         // Solar Transit
         double Jtransit = Jp + (0.0053 * Math.sin(deg2rad(M))) - (0.0069 * Math.sin(deg2rad(2*lambda))); // hour angle for solar transit (or solar noon).
         
         // Declination of the Sun
         double delta =  Math.asin(Math.sin(deg2rad(lambda)) * Math.sin(deg2rad(23.45)));
         
         // Hour Angle
         double omega0 = Math.acos( ( Math.sin(deg2rad(-0.83)) - (Math.sin(deg2rad(ln)) * Math.sin(delta)) ) 
                 / ( Math.cos(deg2rad(ln)) * Math.cos(delta) ) );
         
         // Calculate Sunrise and Sunset
         double Jpp  = JDN2000_01_01 + 0.0009 + ( ( (180*omega0/Math.PI) + lw) /360) + n;
         double Jset = Jpp + (0.0053 *Math.sin(deg2rad(M))) - (0.0069 *Math.sin(deg2rad(2*lambda)));
         double Jrise = Jtransit - (Jset - Jtransit);
         
         HashMap<String,Double> result = new HashMap<String,Double>();
         /*
         result.put("n", n);
         result.put("Jp", Jp);
         result.put("M", M);
         result.put("lambda", lambda);
         result.put("C", C);
         result.put("delta", delta);
         result.put("omega0", omega0);
         result.put("Jtransit", Jtransit);
         */
         result.put(Keys.J_SET, Jset);
         result.put(Keys.J_RISE, Jrise);
         return result;
     }
     
 }
