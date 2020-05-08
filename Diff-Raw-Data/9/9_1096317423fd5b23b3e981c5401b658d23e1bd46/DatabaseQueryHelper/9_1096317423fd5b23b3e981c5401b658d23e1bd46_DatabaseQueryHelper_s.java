 package me.mattsutter.conditionred.util;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DatabaseQueryHelper extends SQLiteOpenHelper {
 	
 	private static final String NAME = "radar.db";
 	private static final int DUAL_POL_SUPPORT_ADDED = 2;
	private static final int DATABASE_VERSION = DUAL_POL_SUPPORT_ADDED;
 //	private static final int SITES_DB_LEN = 158;
 //	private static final int PRODUCTS_DB_LEN = 65;
 
 	public static final String PRODUCT_TABLE = "products";
 	public static final String SITE_TABLE = "sites";
 	
 	public static final String PROD_ENABLED = "menu_enabled";
 	public static final String PROD_RES = "resolution";
 	public static final String PROD_DATA_L = "data_level";
 	public static final String PROD_NTR = "ntr";
 	public static final String PROD_RANGE = "range";
 	public static final String PROD_ANGLE = "angle";
 	public static final String PROD_DESCRIPT = "full_description";
 	public static final String PROD_NAME = "name";
 	public static final String PROD_URL = "url";
 	public static final String PROD_TYPE = "code";
 	
 	public static final String SITE_URL = "url";
 	public static final String SITE_STATE = "state";
 	public static final String SITE_CITY = "city";
 	public static final String SITE_ID = "site_id";
 	public static final String SITE_LAT = "lat";
 	public static final String SITE_LONG = "long";
 
 	// If you make changes to this, make sure you update the length values above.
 	// TODO: Add Dual Pol boolean
 	private static final String CREATE_PROD_TABLE = "CREATE TABLE products (_id INTEGER PRIMARY KEY, menu_enabled BOOLEAN, resolution TEXT, data_level NUMERIC, ntr NUMERIC, range NUMERIC, angle NUMERIC, full_description TEXT, name TEXT, code NUMERIC, url TEXT)";
 	// TODO: Add Dual Pol products
 	private static final String[] PRODUCT_TAB = {
 		"INSERT INTO products VALUES( 1,0,'.54 Nmi x 1 Deg',16,1,124,5,'Base Reflectivity - 124 nmi Range (angle = 0.5°)','Base Refl Legacy 1',19,'DS.p19r0/')",
 		"INSERT INTO products VALUES( 2,0,'.54 Nmi x 1 Deg',16,1,124,13,'Base Reflectivity - 124 nmi Range (angle = 1.3°)','Base Reflectivity 2',19,'DS.p19r1/')",
 		"INSERT INTO products VALUES( 3,0,'.54 Nmi x 1 Deg',16,1,124,15,'Base Reflectivity - 124 nmi Range (angle = 1.5°)','Base Reflectivity 2',19,'DS.p19r1/')",
 		"INSERT INTO products VALUES( 4,0,'.54 Nmi x 1 Deg',16,1,124,24,'Base Reflectivity - 124 nmi Range (angle = 2.4°)','Base Reflectivity 3',19,'DS.p19r2/')",
 		"INSERT INTO products VALUES( 5,0,'.54 Nmi x 1 Deg',16,1,124,25,'Base Reflectivity - 124 nmi Range (angle = 2.5°)','Base Reflectivity 3',19,'DS.p19r2/')",
 		"INSERT INTO products VALUES( 6,0,'.54 Nmi x 1 Deg',16,1,124,31,'Base Reflectivity - 124 nmi Range (angle = 3.1°)','Base Reflectivity 4',19,'DS.p19r3/')",
 		"INSERT INTO products VALUES( 7,0,'.54 Nmi x 1 Deg',16,1,124,34,'Base Reflectivity - 124 nmi Range (angle = 3.4°)','Base Reflectivity 4',19,'DS.p19r3/')",
 		"INSERT INTO products VALUES( 8,0,'.54 Nmi x 1 Deg',16,1,124,35,'Base Reflectivity - 124 nmi Range (angle = 3.5°)','Base Reflectivity 4',19,'DS.p19r3/')",
 		"INSERT INTO products VALUES( 9,0,'1.1 Nmi x 1 Deg',16,1,248,5,'Base Reflectivity - 248 nmi Range (angle = 0.5°)','Base Reflectivity LR',20,'DS.p20-r/')",
 		"INSERT INTO products VALUES(10,0,'.54 Nmi x 1 Deg',256,33,124,0,'Digital Hybrid Scan Reflectivity','DHS Reflectivity',32,'DS.32.dhr/')",
 		"INSERT INTO products VALUES(11,0,'1 km x 1 Deg',4,33,124,0,'Clutter Filter Control (Segment 1)','Clutter Filter Control 1',34,'DS.34cf1/')",
 		"INSERT INTO products VALUES(12,0,'1 km x 1 Deg',4,33,124,0,'Clutter Filter Control (Segment 2)','Clutter Filter Control 2',34,'DS.34cf2/')",
 		"INSERT INTO products VALUES(13,0,'1 km x 1 Deg',4,33,124,0,'Clutter Filter Control (Segment 3)','Clutter Filter Control 3',34,'DS.34cf3/')",
 		"INSERT INTO products VALUES(14,0,'1 km x 1 Deg',4,33,124,0,'Clutter Filter Control (Segment 4)','Clutter Filter Control 4',34,'DS.34cf4/')",
 		"INSERT INTO products VALUES(15,0,'.54 Nmi x .54 Nmi',16,6,124,0,'Composite Reflectivity - 124 nmi range','Composite Reflectivity',37,'DS.p37cr/')",
 		"INSERT INTO products VALUES(16,0,'2.2 Nmi x 2.2 Nmi',16,6,248,0,'Composite Reflectivity - 248 nmi range','Composite Reflectivity LR',38,'DS.p38cr/')",
 		"INSERT INTO products VALUES(17,0,'2.2 Nmi x 2.2 Nmi',16,6,0,0,'Echo Tops','Echo Tops',41,'DS.p41et/')",
 		"INSERT INTO products VALUES(18,0,'5 Knots',5,12,0,0,'Velocity Azimuth Display (VAD) Wind Profile','Wind Profile',48,'DS.48vwp/')",
 		"INSERT INTO products VALUES(19,1,'.54 Nmi x 1 Deg',16,16,124,5,'Storm Relative Mean Radial Velocity (angle = 0.5°)','Storm Relative Velocity 1',56,'DS.56rm0/')",
 		"INSERT INTO products VALUES(20,1,'.54 Nmi x 1 Deg',16,16,124,13,'Storm Relative Mean Radial Velocity (angle = 1.3°)','Storm Relative Velocity 2',56,'DS.56rm1/')",
 		"INSERT INTO products VALUES(21,1,'.54 Nmi x 1 Deg',16,16,124,15,'Storm Relative Mean Radial Velocity (angle = 1.5°)','Storm Relative Velocity 2',56,'DS.56rm1/')",
 		"INSERT INTO products VALUES(22,1,'.54 Nmi x 1 Deg',16,16,124,24,'Storm Relative Mean Radial Velocity (angle = 2.4°)','Storm Relative Velocity 3',56,'DS.56rm2/')",
 		"INSERT INTO products VALUES(23,1,'.54 Nmi x 1 Deg',16,16,124,25,'Storm Relative Mean Radial Velocity (angle = 2.5°)','Storm Relative Velocity 3',56,'DS.56rm2/')",
 		"INSERT INTO products VALUES(24,1,'.54 Nmi x 1 Deg',16,16,124,31,'Storm Relative Mean Radial Velocity (angle = 3.1°)','Storm Relative Velocity 4',56,'DS.56rm3/')",
 		"INSERT INTO products VALUES(25,1,'.54 Nmi x 1 Deg',16,16,124,34,'Storm Relative Mean Radial Velocity (angle = 3.4°)','Storm Relative Velocity 4',56,'DS.56rm3/')",
 		"INSERT INTO products VALUES(26,1,'.54 Nmi x 1 Deg',16,16,124,35,'Storm Relative Mean Radial Velocity (angle = 3.5°)','Storm Relative Velocity 4',56,'DS.56rm3/')",
 		"INSERT INTO products VALUES(27,0,'2.2 Nmi x 2.2 Nmi',16,17,124,0,'Vertical Integrated Liquid','VIL',57,'DS.57vil/')",
 		"INSERT INTO products VALUES(28,0,'N/A',0,18,248,0,'Storm Tracking Information','Storm Tracks',58,'DS.58sti/')",
 		"INSERT INTO products VALUES(29,0,'N/A',0,19,124,0,'Hail Index','Hail',59,'DS.p59hi/')",
 		"INSERT INTO products VALUES(30,0,'N/A',0,21,124,0,'Tornadic Vortex Signature','TVS',61,'DS.61tvs/')",
 		"INSERT INTO products VALUES(31,0,'N/A',0,22,248,0,'Storm Structure','Storm Structure',62,'DS.p62ss/')",
 		"INSERT INTO products VALUES(32,0,'2.2 Nmi x 2.2 Nmi',8,23,124,0,'Layer Composite Reflectivity Maximum (low level)','LCR Max 1',65,'DS.65lrm/')",
 		"INSERT INTO products VALUES(33,0,'2.2 Nmi x 2.2 Nmi',8,23,124,0,'Layer Composite Reflectivity Maximum (middle level)','LCR Max 2',66,'DS.66lrm/')",
 		"INSERT INTO products VALUES(34,0,'2.2 Nmi x 2.2 Nmi',8,23,124,0,'Layer Composite Reflectivity Maximum (high level)','LCR Max 3',67,'DS.90lrm/')",
 		"INSERT INTO products VALUES(35,0,'1/16 LFM',9,26,248,0,'Radar Coded Message','RCM',74,'DS.74rcm/')",
 		"INSERT INTO products VALUES(36,0,'N/A',0,27,0,0,'Free Text Message','FTM',75,'DS.75ftm/')",
 		"INSERT INTO products VALUES(37,1,'1.1 Nmi x 1 Deg',16,28,124,0,'Surface rainfall accumulation - one hour total','Rainfall - 1hr',78,'DS.78ohp/')",
 		"INSERT INTO products VALUES(38,1,'1.1 Nmi x 1 Deg',16,28,124,0,'Surface rainfall accumulation - three hour total','Rainfall - 3hr',79,'DS.79thp/')",
 		"INSERT INTO products VALUES(39,0,'1.1 Nmi x 1 Deg',16,28,124,0,'Surface rainfall accumulation - storm total','Rainfall - Storm Total',80,'DS.80stp/')",
 		"INSERT INTO products VALUES(40,1,'.54 Nmi x 1 Deg',256,1,248,5,'Base Reflectivity - 248 nmi Range (angle = 0.5°)','Base Reflectivity 1',94,'DS.p94r0/')",
 		"INSERT INTO products VALUES(41,1,'.54 Nmi x 1 Deg',256,1,248,9,'Base Reflectivity - 248 nmi Range (angle = 0.9°)','Base Reflectivity 2',94,'DS.p94ra/')",
 		"INSERT INTO products VALUES(42,1,'.54 Nmi x 1 Deg',256,1,248,13,'Base Reflectivity - 248 nmi Range (angle = 1.3°)','Base Reflectivity 3',94,'DS.p94r1/')",
 		"INSERT INTO products VALUES(43,1,'.54 Nmi x 1 Deg',256,1,248,15,'Base Reflectivity - 248 nmi Range (angle = 1.5°)','Base Reflectivity 3',94,'DS.p94r1/')",
 		"INSERT INTO products VALUES(44,1,'.54 Nmi x 1 Deg',256,1,248,18,'Base Reflectivity - 248 nmi Range (angle = 1.8°)','Base Reflectivity 4',94,'DS.p94rb/')",
 		"INSERT INTO products VALUES(45,1,'.54 Nmi x 1 Deg',256,1,248,24,'Base Reflectivity - 248 nmi Range (angle = 2.4°)','Base Reflectivity 5',94,'DS.p94r2/')",
 		"INSERT INTO products VALUES(46,1,'.54 Nmi x 1 Deg',256,1,248,25,'Base Reflectivity - 248 nmi Range (angle = 2.5°)','Base Reflectivity 5',94,'DS.p94r2/')",
 		"INSERT INTO products VALUES(47,1,'.54 Nmi x 1 Deg',256,1,248,31,'Base Reflectivity - 248 nmi Range (angle = 3.1°)','Base Reflectivity 6',94,'DS.p94r3/')",
 		"INSERT INTO products VALUES(48,1,'.54 Nmi x 1 Deg',256,1,248,34,'Base Reflectivity - 248 nmi Range (angle = 3.4°)','Base Reflectivity 6',94,'DS.p94r3/')",
 		"INSERT INTO products VALUES(49,1,'.54 Nmi x 1 Deg',256,1,248,35,'Base Reflectivity - 248 nmi Range (angle = 3.5°)','Base Reflectivity 6',94,'DS.p94r3/')",
 		"INSERT INTO products VALUES(50,1,'.13 Nmi x 1 Deg',256,2,124,5,'Base Radial Velocity - 124 nmi Range (angle = 0.5°)','Base Velocity 1',99,'DS.p99v0/')",
 		"INSERT INTO products VALUES(51,1,'.13 Nmi x 1 Deg',256,2,124,9,'Base Radial Velocity - 124 nmi Range (angle = 0.9°)','Base Velocity 2',99,'DS.p99va/')",
 		"INSERT INTO products VALUES(52,1,'.13 Nmi x 1 Deg',256,2,124,13,'Base Radial Velocity - 124 nmi Range (angle = 1.3°)','Base Velocity 3',99,'DS.p99v1/')",
 		"INSERT INTO products VALUES(53,1,'.13 Nmi x 1 Deg',256,2,124,15,'Base Radial Velocity - 124 nmi Range (angle = 1.5°)','Base Velocity 3',99,'DS.p99v1/')",
 		"INSERT INTO products VALUES(54,1,'.13 Nmi x 1 Deg',256,2,124,18,'Base Radial Velocity - 124 nmi Range (angle = 1.8°)','Base Velocity 4',99,'DS.p99vb/')",
 		"INSERT INTO products VALUES(55,1,'.13 Nmi x 1 Deg',256,2,124,24,'Base Radial Velocity - 124 nmi Range (angle = 2.4°)','Base Velocity 5',99,'DS.p99v2/')",
 		"INSERT INTO products VALUES(56,1,'.13 Nmi x 1 Deg',256,2,124,25,'Base Radial Velocity - 124 nmi Range (angle = 2.5°)','Base Velocity 5',99,'DS.p99v2/')",
 		"INSERT INTO products VALUES(57,1,'.13 Nmi x 1 Deg',256,2,124,31,'Base Radial Velocity - 124 nmi Range (angle = 3.1°)','Base Velocity 6',99,'DS.p99v3/')",
 		"INSERT INTO products VALUES(58,1,'.13 Nmi x 1 Deg',256,2,124,34,'Base Radial Velocity - 124 nmi Range (angle = 3.4°)','Base Velocity 6',99,'DS.p99v3/')",
 		"INSERT INTO products VALUES(59,1,'.13 Nmi x 1 Deg',256,2,124,35,'Base Radial Velocity - 124 nmi Range (angle = 3.5°)','Base Velocity 6',99,'DS.p99v3/')",
 		"INSERT INTO products VALUES(60,1,'.54 Nmi x 1 Deg',256,39,248,0,'Digital Vertical Integrated Liquid','VIL',134,'DS.134il/')",
 		"INSERT INTO products VALUES(61,1,'.54 Nmi x 1 Deg',199,41,186,0,'Enhanced Echo Tops','Echo Tops',135,'DS.135et/')",
 		"INSERT INTO products VALUES(62,1,'1.1 Nmi x 1 Deg',256,29,124,0,'Digital Storm Total Precipitation','Rainfall - Storm Total',138,'DS.138dp/')",
 		"INSERT INTO products VALUES(63,0,'N/A',0,20,124,0,'Mesocyclone','Meso',141,'DS.141md/')",
 		"INSERT INTO products VALUES(64,0,'.13 Nmi x 1 Deg',8,3,32,0,'Base Spectrum Width - 32 Nmi Range','Spectrum Width',28,'DS.p28sw/')",
 		"INSERT INTO products VALUES(65,0,'.54 Nmi x 1 Deg',8,3,124,0,'Base Spectrum Width - 124 Nmi Range','Spectrum Width LR',30,'DS.p30sw/')",
 	};
 
 	private static final String CREATE_SITE_TABLE = "CREATE TABLE sites (_id INTEGER PRIMARY KEY, has_dp BOOLEAN, url TEXT, state TEXT, city TEXT, site_id TEXT, lat NUMERIC, long NUMERIC)";
 	private static final String[] SITE_TAB = {
 		"INSERT INTO sites VALUES(1,1,'SI.kbmx/','Alabama','Birmingham','KBMX',33172,-86770)",
 		"INSERT INTO sites VALUES(2,0,'SI.kmob/','Alabama','Mobile','KMOB',30679,-88240)",
 		"INSERT INTO sites VALUES(3,0,'SI.keox/','Alabama','Fort Rucker','KEOX',31460,-85459)",
 		"INSERT INTO sites VALUES(4,0,'SI.kmxx/','Alabama','East Alabama \n(Montgomery)','KMXX',32537,-85790)",
 		"INSERT INTO sites VALUES(5,1,'SI.khtx/','Alabama','Huntsville','KHTX',34931,-86084)",
 		"INSERT INTO sites VALUES(6,0,'SI.pacg/','Alaska','Biorka Island','PACG',56853,-135528)",
 		"INSERT INTO sites VALUES(7,0,'SI.paih/','Alaska','Middleton Island','PAIH',59462,-146301)",
 		"INSERT INTO sites VALUES(8,0,'SI.pakc/','Alaska','King Salmon','PAKC',58680,-156627)",
 		"INSERT INTO sites VALUES(9,1,'SI.pabc/','Alaska','Bethel','PABC',60792,-161876)",
 		"INSERT INTO sites VALUES(10,0,'SI.paec/','Alaska','Nome','PAEC',64512,-165293)",
 		"INSERT INTO sites VALUES(11,0,'SI.papd/','Alaska','Pedro Dome\n(Fairbanks)','PAPD',65035,-147501)",
		"INSERT INTO sites VALUES(12,0,'SI.pahg/','Alaska','Kenai\n(Nikiski)','PAHG',60.726,-151.351)",
 		"INSERT INTO sites VALUES(13,1,'SI.kfsx/','Arizona','Flagstaff\n(Coconino)','KFSX',34574,-111198)",
 		"INSERT INTO sites VALUES(14,1,'SI.kiwa/','Arizona','Phoenix','KIWA',33289,-111670)",
 		"INSERT INTO sites VALUES(15,1,'SI.kyux/','Arizona','Yuma','KYUX',32495,-114656)",
 		"INSERT INTO sites VALUES(16,1,'SI.kemx/','Arizona','Tucson','KEMX',31894,-110630)",
 		"INSERT INTO sites VALUES(17,1,'SI.ksrx/','Arkansas','Western Arkansas/Ft. Smith','KSRX',35290,-94362)",
 		"INSERT INTO sites VALUES(18,1,'SI.klzk/','Arkansas','Little Rock','KLZK',34836,-92262)",
 		"INSERT INTO sites VALUES(19,1,'SI.kbhx/','California','Eureka','KBHX',40499,-124292)",
 		"INSERT INTO sites VALUES(20,1,'SI.khnx/','California','San Joaquin Valley\n(Hanford)','KHNX',36314,-119632)",
 		"INSERT INTO sites VALUES(21,1,'SI.kbbx/','California','Beale Air Force Base','KBBX',39496,-121632)",
 		"INSERT INTO sites VALUES(22,1,'SI.keyx/','California','Edwards Air Force Base','KEYX',35098,-117561)",
 		"INSERT INTO sites VALUES(23,1,'SI.kvbx/','California','Vandenberg Air Force Base','KVBX',34839,-120398)",
 		"INSERT INTO sites VALUES(24,1,'SI.kvtx/','California','Los Angeles\n(Oxnard)','KVTX',34412,-119179)",
 		"INSERT INTO sites VALUES(25,1,'SI.kmux/','California','San Francisco Bay Area\n(Monterey)','KMUX',37155,-121898)",
 		"INSERT INTO sites VALUES(26,1,'SI.ksox/','California','Santa Ana Mountain\n(Orange City)','KSOX',33818,-117636)",
 		"INSERT INTO sites VALUES(27,1,'SI.knkx/','California','San Diego','KNKX',32919,-117041)",
 		"INSERT INTO sites VALUES(28,1,'SI.kdax/','California','Sacremento','KDAX',38501,-121678)",
 		"INSERT INTO sites VALUES(29,1,'SI.kftg/','Colorado','Denver/Boulder','KFTG',39786,-104546)",
 		"INSERT INTO sites VALUES(30,1,'SI.kgjx/','Colorado','Grand Junction\n(Mesa)','KGJX',39062,-108214)",
 		"INSERT INTO sites VALUES(31,0,'SI.kpux/','Colorado','Pueblo','KPUX',38460,-104181)",
 		"INSERT INTO sites VALUES(32,1,'SI.kdox/','Delaware','Dover Air Force Base','KDOX',38826,-75440)",
 		"INSERT INTO sites VALUES(33,1,'SI.kjax/','Florida','Jacksonville','KJAX',30485,-81702)",
 		"INSERT INTO sites VALUES(34,0,'SI.kevx/','Florida','Northwest Florida\n(Eglin AFB)','KEVX',30565,-85922)",
 		"INSERT INTO sites VALUES(35,1,'SI.kbyx/','Florida','Key West','KBYX',24597,-81703)",
 		"INSERT INTO sites VALUES(36,1,'SI.kmlb/','Florida','Melbourne','KMLB',28113,-80654)",
 		"INSERT INTO sites VALUES(37,1,'SI.kamx/','Florida','Miami','KAMX',25611,-80413)",
 		"INSERT INTO sites VALUES(38,0,'SI.ktlh/','Florida','Tallahassee','KTLH',30398,-84329)",
 		"INSERT INTO sites VALUES(39,1,'SI.ktbw/','Florida','Tampa Bay','KTBW',27705,-82402)",
 		"INSERT INTO sites VALUES(40,1,'SI.kffc/','Georgia','Atlanta','KFFC',33363,-84566)",
 		"INSERT INTO sites VALUES(41,1,'SI.kjgx/','Georgia','Robins Air Force Base','KJGX',32675,-83351)",
 		"INSERT INTO sites VALUES(42,0,'SI.kvax/','Georgia','Moody Air Force Base','KVAX',30890,-83002)",
 		"INSERT INTO sites VALUES(43,1,'SI.phkm/','Hawaii','Kohala','PHKM',20125,-155778)",
 		"INSERT INTO sites VALUES(44,0,'SI.phwa/','Hawaii','South Shore Hawaii','PHWA',19095,-155569)",
 		"INSERT INTO sites VALUES(45,0,'SI.phmo/','Hawaii','Molokai','PHMO',21133,-157180)",
 		"INSERT INTO sites VALUES(46,1,'SI.phki/','Hawaii','Kauai','PHKI',21894,-159552)",
 		"INSERT INTO sites VALUES(47,1,'SI.kcbx/','Idaho','Boise','KCBX',43490,-116236)",
 		"INSERT INTO sites VALUES(48,0,'SI.ksfx/','Idaho','Pocatello/Idaho Falls','KSFX',43106,-112686)",
 		"INSERT INTO sites VALUES(49,1,'SI.kilx/','Illinois','Central Illinois\n(Lincoln)','KILX',40150,-89337)",
 		"INSERT INTO sites VALUES(50,1,'SI.klot/','Illinois','Chicago\n(Romeoville)','KLOT',41604,-88085)",
 		"INSERT INTO sites VALUES(51,0,'SI.kind/','Indiana','Indianapolis','KIND',39708,-86280)",
 		"INSERT INTO sites VALUES(52,0,'SI.kiwx/','Indiana','Northern Indiana\n(North Webster)','KIWX',41359,-85700)",
 		"INSERT INTO sites VALUES(53,0,'SI.kvwx/','Indiana','Evansville','KVWX',38260,-87724)",
 		"INSERT INTO sites VALUES(54,1,'SI.kdvn/','Iowa','Quad Cities \n(Davenport)','KDVN',41612,-90581)",
 		"INSERT INTO sites VALUES(55,1,'SI.kdmx/','Iowa','Des Moines\n(Johnston)','KDMX',41731,-93723)",
 		"INSERT INTO sites VALUES(56,1,'SI.kddc/','Kansas','Dodge City','KDDC',37761,-99969)",
 		"INSERT INTO sites VALUES(57,1,'SI.kgld/','Kansas','Goodland','KGLD',39367,-101700)",
 		"INSERT INTO sites VALUES(58,1,'SI.ktwx/','Kansas','Topeka','KTWX',38997,-96232)",
 		"INSERT INTO sites VALUES(59,1,'SI.kict/','Kansas','Wichita','KICT',37654,-97443)",
 		"INSERT INTO sites VALUES(60,0,'SI.kjkl/','Kentucky','Jackson','KJKL',37591,-83313)",
 		"INSERT INTO sites VALUES(61,0,'SI.khpx/','Kentucky','Fort Campbell\n(Hopkinsville)','KHPX',36737,-87285)",
 		"INSERT INTO sites VALUES(62,0,'SI.klvx/','Kentucky','Louisville','KLVX',37975,-85944)",
 		"INSERT INTO sites VALUES(63,0,'SI.kpah/','Kentucky','Paducah','KPAH',37068,-88772)",
 		"INSERT INTO sites VALUES(64,0,'SI.klch/','Louisiana','Lake Charles','KLCH',30125,-93216)",
 		"INSERT INTO sites VALUES(65,0,'SI.kpoe/','Louisiana','Fort Polk','KPOE',31155,-92976)",
 		"INSERT INTO sites VALUES(66,0,'SI.kshv/','Louisiana','Shreveport','KSHV',32451,-93841)",
 		"INSERT INTO sites VALUES(67,0,'SI.klix/','Louisiana','New Orleans/Baton Rouge\n(Sidell)','KLIX',30337,-89825)",
 		"INSERT INTO sites VALUES(68,1,'SI.kcbw/','Maine','Caribou\n(Houlton)','KCBW',46039,-67806)",
 		"INSERT INTO sites VALUES(69,1,'SI.kgyx/','Maine','Portland\n(Gray)','KGYX',43891,-70256)",
 		"INSERT INTO sites VALUES(70,1,'SI.klwx/','Maryland','Baltimore, MD/Washinton, DC\n(Sterling VA)','KLWX',38976,-77487)",
 		"INSERT INTO sites VALUES(71,1,'SI.kbox/','Massachusetts','Boston\n(Taunton)','KBOX',41956,-71137)",
 		"INSERT INTO sites VALUES(72,1,'SI.kapx/','Michigan','Gaylord','KAPX',44906,-84720)",
 		"INSERT INTO sites VALUES(73,1,'SI.kgrr/','Michigan','Grand Rapids/Muskegon','KGRR',42894,-85545)",
 		"INSERT INTO sites VALUES(74,1,'SI.kmqt/','Michigan','Marquette','KMQT',46531,-87548)",
 		"INSERT INTO sites VALUES(75,0,'SI.kdtx/','Michigan','Detoit/Pontiac\n(White Lake)','KDTX',42700,-83472)",
 		"INSERT INTO sites VALUES(76,1,'SI.kmpx/','Minnesota','Twin Cities\n(Chanhassen)','KMPX',44849,-93565)",
 		"INSERT INTO sites VALUES(77,1,'SI.kdlh/','Minnesota','Duluth','KDLH',46837,-92210)",
 		"INSERT INTO sites VALUES(78,1,'SI.kdgx/','Mississippi','Brandon/Jackson','KDGX',32280,-89984)",
 		"INSERT INTO sites VALUES(79,0,'SI.kgwx/','Mississippi','Columbus Air Force Base','KGWX',33897,-88329)",
 		"INSERT INTO sites VALUES(80,1,'SI.keax/','Missouri','Kansas City\n(Pleasant Hill)','KEAX',38810,-94264)",
 		"INSERT INTO sites VALUES(81,1,'SI.ksgf/','Missouri','Springfield','KSGF',37235,-93400)",
 		"INSERT INTO sites VALUES(82,1,'SI.klsx/','Missouri','St. Louis','KLSX',38699,-90683)",
 		"INSERT INTO sites VALUES(83,1,'SI.kblx/','Montana','Billings','KBLX',45854,-108607)",
 		"INSERT INTO sites VALUES(84,1,'SI.kggw/','Montana','Glasgow','KGGW',48206,-106625)",
 		"INSERT INTO sites VALUES(85,1,'SI.ktfx/','Montana','Great Falls','KTFX',47460,-111385)",
 		"INSERT INTO sites VALUES(86,1,'SI.kmsx/','Montana','Missoula','KMSX',47041,-113986)",
 		"INSERT INTO sites VALUES(87,0,'SI.kuex/','Nebraska','Grand Island\n(Hastings)','KUEX',40321,-98442)",
 		"INSERT INTO sites VALUES(88,0,'SI.klnx/','Nebraska','North Platte\n(Thedford)','KLNX',41958,-100576)",
 		"INSERT INTO sites VALUES(89,0,'SI.koax/','Nebraska','Omaha\n(Valley)','KOAX',41320,-96367)",
 		"INSERT INTO sites VALUES(90,1,'SI.klrx/','Nevada','Elko','KLRX',40740,-116803)",
 		"INSERT INTO sites VALUES(91,1,'SI.kesx/','Nevada','Las Vegas','KESX',35701,-114891)",
 		"INSERT INTO sites VALUES(92,1,'SI.krgx/','Nevada','Reno','KRGX',39754,-119462)",
 		"INSERT INTO sites VALUES(93,1,'SI.kdix/','New Jersey','Philadelphia, PA\n(Mount Holly, NJ)','KDIX',39947,-74411)",
 		"INSERT INTO sites VALUES(94,1,'SI.kabx/','New Mexico','Albuquerque','KABX',35150,-106824)",
 		"INSERT INTO sites VALUES(95,1,'SI.kfdx/','New Mexico','Cannon Air Force Base','KFDX',34634,-103619)",
 		"INSERT INTO sites VALUES(96,1,'SI.khdx/','New Mexico','Holloman Air Force Base\n(White Sands)','KHDX',33077,-106120)",
 		"INSERT INTO sites VALUES(97,1,'SI.kepz/','New Mexico','El Paso, TX\n(Santa Teresa, NM)','KEPZ',31873,-106698)",
 		"INSERT INTO sites VALUES(98,1,'SI.kenx/','New York','Albany','KENX',42586,-74064)",
 		"INSERT INTO sites VALUES(99,1,'SI.ktyx/','New York','Montague','KTYX',43756,-75680)",
 		"INSERT INTO sites VALUES(100,1,'SI.kbgm/','New York','Binghamton','KBGM',42200,-75985)",
 		"INSERT INTO sites VALUES(101,1,'SI.kbuf/','New York','Buffalo','KBUF',42949,-78737)",
 		"INSERT INTO sites VALUES(102,1,'SI.kokx/','New York','New York City\n(Upton)','KOKX',40865,-72864)",
 		"INSERT INTO sites VALUES(103,1,'SI.kmhx/','North Carolina','Newport/Morehead','KMHX',34776,-76876)",
 		"INSERT INTO sites VALUES(104,0,'SI.krax/','North Carolina','Raleigh/Durham\n(Clayton)','KRAX',35665,-78490)",
 		"INSERT INTO sites VALUES(105,1,'SI.kltx/','North Carolina','Wilmington','KLTX',33989,-78429)",
 		"INSERT INTO sites VALUES(106,1,'SI.kmbx/','North Dakota','Minot Air Force Base','KMBX',48393,-100864)",
 		"INSERT INTO sites VALUES(107,1,'SI.kbis/','North Dakota','Bismarck','KBIS',46771,-100760)",
 		"INSERT INTO sites VALUES(108,1,'SI.kmvx/','North Dakota','Grand Forks\n(Mayville)','KMVX',47528,-97325)",
 		"INSERT INTO sites VALUES(109,1,'SI.kcle/','Ohio','Cleveland','KCLE',41413,-81860)",
 		"INSERT INTO sites VALUES(110,1,'SI.kiln/','Ohio','Wilmington','KILN',39420,-83822)",
 		"INSERT INTO sites VALUES(111,1,'SI.kvnx/','Oklahoma','Vance Air Force Base','KVNX',36741,-98128)",
 		"INSERT INTO sites VALUES(112,1,'SI.ktlx/','Oklahoma','Oklahoma City\n(Norman)','KTLX',35333,-97278)",
 		"INSERT INTO sites VALUES(113,1,'SI.kinx/','Oklahoma','Tulsa','KINX',36175,-95564)",
 		"INSERT INTO sites VALUES(114,1,'SI.kfdr/','Oklahoma','Frederick','KFDR',34362,-98977)",
 		"INSERT INTO sites VALUES(115,1,'SI.kmax/','Oregon','Medford','KMAX',42081,-122717)",
 		"INSERT INTO sites VALUES(116,1,'SI.kpdt/','Oregon','Pendleton','KPDT',45691,-118853)",
 		"INSERT INTO sites VALUES(117,1,'SI.krtx/','Oregon','Portland','KRTX',45715,-122965)",
 		"INSERT INTO sites VALUES(118,1,'SI.kpbz/','Pennsylvania','Pittsburgh','KPBZ',40532,-80218)",
 		"INSERT INTO sites VALUES(119,1,'SI.kdix/','Pennsylvania','Philadelphia, PA\n(Mount Holly, NJ)','KDIX',39947,-74411)",
 		"INSERT INTO sites VALUES(120,1,'SI.kccx/','Pennsylvania','State College','KCCX',40923,-78004)",
 		"INSERT INTO sites VALUES(121,0,'SI.kclx/','South Carolina','Charleston\n(Grays)','KCLX',32655,-81042)",
 		"INSERT INTO sites VALUES(122,1,'SI.kcae/','South Carolina','Columbia','KCAE',33949,-81119)",
 		"INSERT INTO sites VALUES(123,1,'SI.kgsp/','South Carolina','Greer','KGSP',34883,-82220)",
 		"INSERT INTO sites VALUES(124,1,'SI.kabr/','South Dakota','Aberdeen','KABR',45456,-98413)",
 		"INSERT INTO sites VALUES(125,1,'SI.kudx/','South Dakota','Rapid City','KUDX',44125,-102830)",
 		"INSERT INTO sites VALUES(126,1,'SI.kfsd/','South Dakota','Sioux Falls','KFSD',43588,-96729)",
 		"INSERT INTO sites VALUES(127,1,'SI.knqa/','Tennessee','Memphis','KNQA',35345,-89873)",
 		"INSERT INTO sites VALUES(128,1,'SI.kmrx/','Tennessee','Knoxville/Tri Cities\n(Morristown)','KMRX',36168,-83402)",
 		"INSERT INTO sites VALUES(129,1,'SI.kohx/','Tennessee','Nashville','KOHX',36247,-86563)",
 		"INSERT INTO sites VALUES(130,1,'SI.kama/','Texas','Amarillo','KAMA',35233,-101709)",
 		"INSERT INTO sites VALUES(131,0,'SI.kdfx/','Texas','Laughlin Air Force Base','KDFX',29273,-100280)",
 		"INSERT INTO sites VALUES(132,0,'SI.kdyx/','Texas','Dyess Air Force Base','KDYX',32538,-99254)",
 		"INSERT INTO sites VALUES(133,0,'SI.kgrk/','Texas','Central Texas\n(Killeen Robert Gray AAF)','KGRK',30722,-97383)",
 		"INSERT INTO sites VALUES(134,0,'SI.kbro/','Texas','Brownsville','KBRO',25916,-97419)",
 		"INSERT INTO sites VALUES(135,0,'SI.kcrp/','Texas','Corpus Christi','KCRP',27784,-97511)",
 		"INSERT INTO sites VALUES(136,1,'SI.kepz/','Texas','El Paso, Texas\n(Santa Teresa, NM)','KEPZ',31873,-106698)",
 		"INSERT INTO sites VALUES(137,0,'SI.kfws/','Texas','Dallas/Fort Worth','KFWS',32573,-97303)",
 		"INSERT INTO sites VALUES(138,0,'SI.khgx/','Texas','Houston/Galveston\n(League City)','KHGX',29472,-95079)",
 		"INSERT INTO sites VALUES(139,0,'SI.klbb/','Texas','Lubbock','KLBB',33654,-101814)",
 		"INSERT INTO sites VALUES(140,0,'SI.kmaf/','Texas','Midland/Odessa','KMAF',31943,-102189)",
 		"INSERT INTO sites VALUES(141,1,'SI.kewx/','Texas','Austin/San Antonio\n(New Braunfels)','KEWX',29704,-98029)",
 		"INSERT INTO sites VALUES(142,1,'SI.ksjt/','Texas','San Angelo','KSJT',31371,-100492)",
 		"INSERT INTO sites VALUES(143,1,'SI.kmtx/','Utah','Salt Lake City','KMTX',41263,-112448)",
 		"INSERT INTO sites VALUES(144,1,'SI.kicx/','Utah','Cedar City','KICX',37591,-112862)",
 		"INSERT INTO sites VALUES(145,1,'SI.kcxx/','Vermont','Burlington\n(Colchester)','KCXX',44511,-73166)",
 		"INSERT INTO sites VALUES(146,1,'SI.kfcx/','Virginia','Blacksburg/Roanoke','KFCX',37024,-80274)",
 		"INSERT INTO sites VALUES(147,1,'SI.klwx/','Virginia','Baltimore, MD/Washington, DC\n(Sterling, VA)','KLWX',38976,-77487)",
 		"INSERT INTO sites VALUES(148,1,'SI.kakq/','Virginia','Wakefield','KAKQ',36984,-77008)",
 		"INSERT INTO sites VALUES(149,1,'SI.katx/','Washington','Seattle/Tacoma\n(Camano Island)','KATX',48195,-122496)",
 		"INSERT INTO sites VALUES(150,1,'SI.kotx/','Washington','Spokane','KOTX',47681,-117626)",
 		"INSERT INTO sites VALUES(151,1,'SI.krlx/','West Virginia','Charleston','KRLX',38311,-81723)",
 		"INSERT INTO sites VALUES(152,1,'SI.kgrb/','Wisconsin','Green Bay','KGRB',44499,-88111)",
 		"INSERT INTO sites VALUES(153,1,'SI.karx/','Wisconsin','La Crosse','KARX',43823,-91191)",
 		"INSERT INTO sites VALUES(154,1,'SI.kmkx/','Wisconsin','Milwaukee\n(Sullivan)','KMKX',42968,-88551)",
 		"INSERT INTO sites VALUES(155,1,'SI.kcys/','Wyoming','Cheyenne','KCYS',41152,-104806)",
 		"INSERT INTO sites VALUES(156,1,'SI.kriw/','Wyoming','Riverton','KRIW',43066,-108477)",
 		"INSERT INTO sites VALUES(157,0,'SI.pgua/','Guam','Andersen Air Force Base','PGUA',13450,144811)",
 		"INSERT INTO sites VALUES(158,0,'SI.tjua/','Puerto Rico','San Juan','TJUA',18116,-66078)"
 	};
 	
 	private static final String DROP_TABLE = "DROP TABLE ";
 	
 	public DatabaseQueryHelper(Context context) {
 		super(context, NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {	
 		createProductTable(db);
 		createSiteTable(db);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < DUAL_POL_SUPPORT_ADDED){
 			db.execSQL(DROP_TABLE + SITE_TABLE);
 			createSiteTable(db);
 		}
 	}
 
 	private void createSiteTable(SQLiteDatabase db){
 		db.execSQL(CREATE_SITE_TABLE);
 		for (int i = 0; i < SITE_TAB.length; i++)
 			db.execSQL(SITE_TAB[i]);
 	}
 	
 	private void createProductTable(SQLiteDatabase db){
 		db.execSQL(CREATE_PROD_TABLE);
 		for (int i = 0; i < PRODUCT_TAB.length; i++)
 			db.execSQL(PRODUCT_TAB[i]);
 	}
 }
