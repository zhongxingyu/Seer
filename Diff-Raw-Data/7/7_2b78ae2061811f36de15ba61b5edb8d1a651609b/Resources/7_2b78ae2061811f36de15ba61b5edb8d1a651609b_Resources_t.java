 public class Resources
 {
	public static final String VERSION_NUMBER			= "v1.7.7";
	public static final String VERSION_CODENAME			= "Chang Wang";
 	
 	public static final int QUEUES 						= 0;
 	public static final int SERVICE_DEMAND				= 1;
 	public static final int SYSTEMS						= 2;
 	public static final int POISSON						= 3;
 	public static final int DISK_ACCESS_TIME			= 4;
 	public static final int CLUSTERING					= 5;
 	
 	public static final int INFINITE_QUEUES				= 0;
 	public static final int FINITE_QUEUES				= 1;
 	
 	public static final int CLOSED_SYSTEM				= 0;
 	public static final int GENERAL_SYSTEM				= 1;
 	
 	public static final String HELP_SERVICE_DEMAND		= "SD";
 	public static final String HELP_CLUSTERING			= "C";
 	public static final String HELP_DISK_ACCESS_TIME 	= "DAT";
 	public static final String HELP_POISSON 			= "e^(-\u03BB*t)*(\u03BB*t)^k";
 	public static final String HELP_INFINITE_QUEUES 	= "Infinite Queues\n\nEquations\nP0 = 1 - (\u03BB/\u03BC)\nU = 1 - P0\nPk = P0 * (\u03BB/\u03BC)^k\n\u00D1 = (u)/(1-u)\nX = \u03BB\nR = N/X  --OR--  R = (1)/(\u03BC(1-u))\n";
	public static final String HELP_FINITE_QUEUES 		= "Finite Queues\nP0 = ((1-\u03BB/\u03BC)/(1-((\u03BB/\u03BC)^w+1))\nP(k) = P0*((\u03BB/\u03BC)^k)\nU = 1 - P0\nN = SUM(k*P0*((\u03BB/\u03BC)^k) k =0...w\nX = U*\u03BC\nR = N/X";
 	public static final String HELP_CLOSED_SYSTEM 		= "Closed systems can be used to calculate a queue size\n\nProcess Time(s) is the same as Service rate (\u03BC), it\'s in seconds and is the rate at\nwhich people/jobs are leaving the system.\nJob Rate (z) is the same as arival rate (\u03BB), it\'sin seconds and is simply the\namount of people/jobs are comming into the system per second.\n\n\nPLEASE HELP WITH THIS HELP PAGE. ANYTHING YOU CAN THINK OF THAT MAY\nHELP WOULD BE APPRICIATED";
 	public static final String HELP_GENERAL_SYSTEM 		= "General systems can be used to calculate a queue size on an open system.\n\nNumber of Terms (k) can be thought as the number of processors or how many\npeople can be doing something at once arival rate (\u03BB) is in seconds and is\nsimply the amount of people/jobs are comming into the system per second.\n\nService rate (\u03BC) is also in seconds and is the rate at which people/jobs are leaving\nthe system. it can be variable, if the processing speed is 10 people per second and\nk=4, then \u03BC changes. if only 1 person is in the system then \u03BC is 10. if there are 2\npeople in the system (for k=2) then \u03BC=2*10, for 3 people \u03BC=3*10 and so on.\n\nExample input:\nk: 5\n\u03BB: 10\n\u03BC: 10, 20, 30, 40, 50\n\nP0=1/{1+sum[Prod(\u03BB(i-1)/�i, i=1..i=k)]}\nPk=P(k-1) * \u03BB(k-1)/�k\nUtilization (u)=1-P0\nN=SUM[i*Pi, i=1..i=k]\nX=SUM[�i*Pi, i=1..i=k]\nR=N/X";
 	public static final String NC_1						= "+      o     +              o    \n     +             o     +       +\n o          +                     \n     o  +           +        +    \n +        o     o       +        o\n -_-_-_-_-_-_-_,---------,           \n _-_-_-_-_-_-_-|      /\\_/\\          \n -_-_-_-_-_-_-~|__( ^ .^)         \n _-_-_-_-_-_-_-\\\"\\\"  \\\"\\\"         \n +      o         o   +       o   \n     +         +                  \n o        o         o      o     +\n     o           +                \n +      +     o        o      +   \n";
 	public static final String NC_2						= "     o  +           +        +    \n o          +                    o\n     o                 +          \n +      o     +              o    \n  o     +        o               +\n _-_-_-_-_-_-_-,---------,           \n -_-_-_-_-_-_-_|      /\\_/\\          \n -_-_-_-_-_-_-~|__( ^ .^)         \n -_-_-_-_-_-_-_\\\"\\\"  \\\"\\\"       \n +      +     o        o      +   \n o        +                o     +\n +      o         +     +       o \n     +         +                  \n        +           o        +    \n";
 	
 }
