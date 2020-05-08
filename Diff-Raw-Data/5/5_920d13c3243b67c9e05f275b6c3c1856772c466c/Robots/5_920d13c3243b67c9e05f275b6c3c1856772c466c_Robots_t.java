 /*
  * @(#)Robots.java
  *
  * The author or authors of this code dedicate any and all copyright interest in this code to the public domain.
  * We make this dedication for the benefit of the public at large and to the detriment of our heirs and successors.
  * We intend this dedication to be an overt act of relinquishment in perpetuity of all present and future rights
  * this code under copyright law.
  *
  * $Id$
  *
  */
 package net.thauvin.erik;
 
 /**
  * The <code>Robots</code> class contains a method used to identify a user-agent against a list of known search engines
 * crawlers, spiders and robots.
  *
  * @author  <a href="mailto:erik@thauvin.net">Erik C. Thauvin</a>
  * @version $Revision$, $Date$
  * @created Feb 14, 2006
  * @since   1.0
  */
 public class Robots
 {
 	/**
 	 * The known robots. Taken from <a href="http://awstats.sourceforge.net/">AWStats</a>.
 	 */
 	public static final String[] ROBOTS =
 		{
 			".*appie.*", ".*architext.*", ".*jeeves.*", ".*bjaaland.*", ".*ferret.*", ".*googlebot.*", ".*gulliver.*",
 			".*harvest.*", ".*htdig.*", ".*linkwalker.*", ".*lycos_.*", ".*moget.*", ".*muscatferret.*", ".*myweb.*",
 			".*nomad.*", ".*scooter.*", ".*slurp.*", "^voyager\\/.*", ".*weblayers.*", ".*antibot.*", ".*digout4u.*",
 			".*echo.*", ".*fast\\-webcrawler.*", ".*ia_archiver\\-web\\.archive\\.org.*", ".*ia_archiver.*",
 			".*jennybot.*", ".*mercator.*", ".*netcraft.*", ".*msnbot.*", ".*petersnews.*", ".*unlost_web_crawler.*",
 			".*voila.*", ".*webbase.*", ".*zyborg.*", ".*wisenutbot.*", ".*[^a]fish.*", ".*abcdatos.*",
 			".*acme\\.spider.*", ".*ahoythehomepagefinder.*", ".*alkaline.*", ".*anthill.*", ".*arachnophilia.*",
 			".*arale.*", ".*araneo.*", ".*aretha.*", ".*ariadne.*", ".*powermarks.*", ".*arks.*", ".*aspider.*",
 			".*atn\\.txt.*", ".*atomz.*", ".*auresys.*", ".*backrub.*", ".*bbot.*", ".*bigbrother.*", ".*blackwidow.*",
 			".*blindekuh.*", ".*bloodhound.*", ".*borg\\-bot.*", ".*brightnet.*", ".*bspider.*",
 			".*cactvschemistryspider.*", ".*calif[^r].*", ".*cassandra.*", ".*cgireader.*", ".*checkbot.*",
 			".*christcrawler.*", ".*churl.*", ".*cienciaficcion.*", ".*collective.*", ".*combine.*", ".*conceptbot.*",
 			".*coolbot.*", ".*core.*", ".*cosmos.*", ".*cruiser.*", ".*cusco.*", ".*cyberspyder.*", ".*desertrealm.*",
 			".*deweb.*", ".*dienstspider.*", ".*digger.*", ".*diibot.*", ".*direct_hit.*", ".*dnabot.*",
 			".*download_express.*", ".*dragonbot.*", ".*dwcp.*", ".*e\\-collector.*", ".*ebiness.*", ".*elfinbot.*",
 			".*emacs.*", ".*emcspider.*", ".*esther.*", ".*evliyacelebi.*", ".*fastcrawler.*", ".*fdse.*", ".*felix.*",
 			".*fetchrover.*", ".*fido.*", ".*finnish.*", ".*fireball.*", ".*fouineur.*", ".*francoroute.*",
 			".*freecrawl.*", ".*funnelweb.*", ".*gama.*", ".*gazz.*", ".*gcreep.*", ".*getbot.*", ".*geturl.*",
 			".*golem.*", ".*grapnel.*", ".*griffon.*", ".*gromit.*", ".*gulperbot.*", ".*hambot.*", ".*havindex.*",
 			".*hometown.*", ".*htmlgobble.*", ".*hyperdecontextualizer.*", ".*iajabot.*", ".*iconoclast.*", ".*ilse.*",
 			".*imagelock.*", ".*incywincy.*", ".*informant.*", ".*infoseek.*", ".*infoseeksidewinder.*",
 			".*infospider.*", ".*inspectorwww.*", ".*intelliagent.*", ".*irobot.*", ".*iron33.*", ".*israelisearch.*",
 			".*javabee.*", ".*jbot.*", ".*jcrawler.*", ".*jobo.*", ".*jobot.*", ".*joebot.*", ".*jubii.*",
 			".*jumpstation.*", ".*kapsi.*", ".*katipo.*", ".*kilroy.*", ".*ko_yappo_robot.*", ".*labelgrabber\\.txt.*",
 			".*larbin.*", ".*legs.*", ".*linkidator.*", ".*linkscan.*", ".*lockon.*", ".*logo_gif.*", ".*macworm.*",
 			".*magpie.*", ".*marvin.*", ".*mattie.*", ".*mediafox.*", ".*merzscope.*", ".*meshexplorer.*",
 			".*mindcrawler.*", ".*mnogosearch.*", ".*momspider.*", ".*monster.*", ".*motor.*", ".*muncher.*",
 			".*mwdsearch.*", ".*ndspider.*", ".*nederland\\.zoek.*", ".*netcarta.*", ".*netmechanic.*", ".*netscoop.*",
 			".*newscan\\-online.*", ".*nhse.*", ".*northstar.*", ".*nzexplorer.*", ".*objectssearch.*", ".*occam.*",
 			".*octopus.*", ".*openfind.*", ".*orb_search.*", ".*packrat.*", ".*pageboy.*", ".*parasite.*", ".*patric.*",
 			".*pegasus.*", ".*perignator.*", ".*perlcrawler.*", ".*phantom.*", ".*phpdig.*", ".*piltdownman.*",
 			".*pimptrain.*", ".*pioneer.*", ".*pitkow.*", ".*pjspider.*", ".*plumtreewebaccessor.*", ".*poppi.*",
 			".*portalb.*", ".*psbot.*", ".*python.*", ".*raven.*", ".*rbse.*", ".*resumerobot.*", ".*rhcs.*",
 			".*road_runner.*", ".*robbie.*", ".*robi.*", ".*robocrawl.*", ".*robofox.*", ".*robozilla.*",
 			".*roverbot.*", ".*rules.*", ".*safetynetrobot.*", ".*search\\-info.*", ".*search_au.*",
 			".*searchprocess.*", ".*senrigan.*", ".*sgscout.*", ".*shaggy.*", ".*shaihulud.*", ".*sift.*", ".*simbot.*",
 			".*site\\-valet.*", ".*sitetech.*", ".*skymob.*", ".*slcrawler.*", ".*smartspider.*", ".*snooper.*",
 			".*solbot.*", ".*speedy.*", ".*spider_monkey.*", ".*spiderbot.*", ".*spiderline.*", ".*spiderman.*",
 			".*spiderview.*", ".*spry.*", ".*sqworm.*", ".*ssearcher.*", ".*suke.*", ".*suntek.*", ".*sven.*",
 			".*tach_bw.*", ".*tarantula.*", ".*tarspider.*", ".*techbot.*", ".*templeton.*", ".*titan.*", ".*titin.*",
 			".*tkwww.*", ".*tlspider.*", ".*ucsd.*", ".*udmsearch.*", ".*urlck.*", ".*valkyrie.*", ".*verticrawl.*",
 			".*victoria.*", ".*visionsearch.*", ".*voidbot.*", ".*vwbot.*", ".*w3index.*", ".*w3m2.*", ".*wallpaper.*",
 			".*wanderer.*", ".*wapspider.*", ".*webbandit.*", ".*webcatcher.*", ".*webcopy.*", ".*webfetcher.*",
 			".*webfoot.*", ".*webinator.*", ".*weblinker.*", ".*webmirror.*", ".*webmoose.*", ".*webquest.*",
 			".*webreader.*", ".*webreaper.*", ".*websnarf.*", ".*webspider.*", ".*webvac.*", ".*webwalk.*",
 			".*webwalker.*", ".*webwatch.*", ".*whatuseek.*", ".*whowhere.*", ".*wired\\-digital.*", ".*wmir.*",
 			".*wolp.*", ".*wombat.*", ".*worm.*", ".*wwwc.*", ".*wz101.*", ".*xget.*", ".*almaden.*", ".*aport.*",
 			".*argus.*", ".*asterias.*", ".*awbot.*", ".*baiduspider.*", ".*becomebot.*", ".*bender.*", ".*bloglines.*",
 			".*blogpulse.*", ".*blogshares.*", ".*blogslive.*", ".*blogssay.*", ".*bobby.*", ".*boris.*",
 			".*bumblebee.*", ".*converacrawler.*", ".*cscrawler.*", ".*daviesbot.*", ".*daypopbot.*",
 			".*dipsie\\.bot.*", ".*domainsdb\\.net.*", ".*exactseek.*", ".*everbeecrawler.*", ".*ezresult.*",
 			".*enteprise.*", ".*feedburner.*", ".*feedfetcher\\-google.*", ".*feedster.*", ".*findlinks.*",
 			".*gaisbot.*", ".*geniebot.*", ".*gigabot.*", ".*girafabot.*", ".*gnodspider.*", ".*grub.*",
 			".*henrythemiragorobot.*", ".*holmes.*", ".*infomine.*", ".*internetseer.*", ".*justview.*", ".*keyoshid.*",
 			".*kinjabot.*", ".*kinja\\-imagebot.*", ".*linkbot.*", ".*metager\\-linkchecker.*", ".*linkchecker.*",
 			".*livejournal\\.com.*", ".*lmspider.*", ".*magpierss.*", ".*mediapartners\\-google.*",
 			".*microsoft_url_control.*", ".*mj12bot.*", ".*msiecrawler.*", ".*nagios.*", ".*newsgatoronline.*",
 			".*noxtrumbot.*", ".*nutch.*", ".*opentaggerbot.*", ".*outfoxbot.*", ".*perman.*", ".*pluckfeedcrawler.*",
 			".*pompos.*", ".*popdexter.*", ".*rambler.*", ".*redalert.*", ".*rojo.*", ".*rssimagesbot.*", ".*ruffle.*",
 			".*rufusbot.*", ".*sandcrawler.*", ".*sbider.*", ".*seekbot.*", ".*seznambot.*", ".*shoutcast.*",
 			".*slysearch.*", ".*sohu-search.*", ".*surveybot.*", ".*syndic8.*", ".*technoratibot.*",
 			".*t\\-h\\-u\\-n\\-d\\-e\\-r\\-s\\-t\\-o\\-n\\-e.*", ".*topicblogs.*", ".*turnitinbot.*",
 			".*turtlescanner.*", ".*turtle.*", ".*ultraseek.*", ".*w3c\\-checklink.*", ".*w3c_css_validator_jfouffa.*",
 			".*w3c_validator.*", ".*webclipping\\.com.*", ".*webcompass.*", ".*webvulncrawl.*", ".*wonderer.*",
 			".*y!j.*", ".*yacy.*", ".*yahoo\\-blogs.*", ".*yahoo\\-verticalcrawler.*", ".*yahoofeedseeker.*",
 			".*yahooseeker\\-testing.*", ".*yahooseeker.*", ".*yahoo\\-mmcrawler.*", ".*yandex.*", ".*zealbot.*",
 			".*robot.*", ".*crawl.*", ".*spider.*", ".*wbot[\\/\\-].*"
 		};
 
 	/**
 	 * Disables the default constructor.
 	 *
 	 * @throws UnsupportedOperationException if the constructor is called.
 	 */
 	private Robots()
 			throws UnsupportedOperationException
 	{
 		throw new UnsupportedOperationException("Illegal constructor call.");
 	}
 
 	/**
 	 * Returns true if the specified user-agent is a known robot.
 	 *
 	 * @param  userAgent The userAgent.
 	 *
	 * @return <code>true</code> if the user-agent is a robot, <code>false</code> otherwise.
 	 */
 	public static boolean isRobot(String userAgent)
 	{
 		final String agent = userAgent.toLowerCase();
 
 		for (String robot : ROBOTS)
 		{
 			if (agent.matches(robot))
 			{
 				return true;
 			}
 		}
 
 		return false;
 	}
 }
