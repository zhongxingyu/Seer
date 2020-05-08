 package ch.hsr.bieridee.utils;
 
 import java.io.File;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import org.apache.log4j.Logger;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.kernel.EmbeddedGraphDatabase;
 import org.neo4j.server.WrappingNeoServerBootstrapper;
 import org.neo4j.server.configuration.EmbeddedServerConfigurator;
 
 import ch.hsr.bieridee.config.NodeProperty;
 import ch.hsr.bieridee.config.RelType;
 
 /**
  * Utiltiy to create a neo4j database with some testdata.
  * 
  */
 public final class Testdb {
 
 	private static final Logger LOG = Logger.getLogger(Testdb.class);
 
 	private static WrappingNeoServerBootstrapper SRV;
 
 	private Testdb() {
 		// do not instantiate.
 	}
 
 	/**
 	 * Creates the Database at the given path with the given name. If a database with the same path/name exists, it will
 	 * be deleted first (mercyless).
 	 * 
 	 * @param path
 	 *            Path and name of the database to be created
 	 * @return The newly created database
 	 */
 	public static EmbeddedGraphDatabase createDB(String path) {
 		deleteDir(path);
 		final EmbeddedGraphDatabase graphDb = new EmbeddedGraphDatabase(path);
 		EmbeddedServerConfigurator config;
 		config = new EmbeddedServerConfigurator(graphDb);
 		config.configuration().setProperty("org.neo4j.server.webserver.address", "0.0.0.0");
 		SRV = new WrappingNeoServerBootstrapper(graphDb, config);
 
 		SRV.start();
 		registerShutdownHook(graphDb);
 		return graphDb;
 	}
 
 	/**
 	 * Deletes the given database.
 	 * 
 	 * @param db
 	 *            The database to be deleted
 	 */
 	public static void deleteDB(EmbeddedGraphDatabase db) {
 		Testdb.deleteDir(db.getStoreDir());
 	}
 
 	/**
 	 * Fills the given database with test data. There is at least four nodes of every type and all needed relationships.
 	 * 
 	 * @param db
 	 *            The database to be filled
 	 * @return The filled database
 	 */
 	// SUPPRESS CHECKSTYLE: only for test purposes.
 	public static EmbeddedGraphDatabase fillDB(EmbeddedGraphDatabase db) {
 		final Transaction transaction = db.beginTx();
 
 		try {
 			final Node rootNode = db.getReferenceNode();
 
 			final Node userIndex = db.createNode();
 			final Node beerIndex = db.createNode();
 			final Node beertypeIndex = db.createNode();
 			final Node tagIndex = db.createNode();
 			final Node breweryIndex = db.createNode();
 			final Node timelineIndex = db.createNode();
 
 			final Node timelineStart = db.createNode();
 			timelineStart.setProperty("type", "timelinestart");
 			final Node timeLineEnde = db.createNode();
 			timeLineEnde.setProperty("type", "timelineend");
 			timelineStart.createRelationshipTo(timeLineEnde, RelType.NEXT);
 
 			rootNode.createRelationshipTo(userIndex, RelType.INDEX_USER);
 			rootNode.createRelationshipTo(beerIndex, RelType.INDEX_BEER);
 			rootNode.createRelationshipTo(tagIndex, RelType.INDEX_TAG);
 			rootNode.createRelationshipTo(breweryIndex, RelType.INDEX_BREWERY);
 			rootNode.createRelationshipTo(timelineIndex, RelType.INDEX_TIMELINE);
 			rootNode.createRelationshipTo(beertypeIndex, RelType.INDEX_BEERTYPE);
 			rootNode.createRelationshipTo(timelineStart, RelType.INDEX_TIMELINESTART);
 
 			/* CREATE BEERTYPES */
 
 			// Gemäss http://de.wikipedia.org/wiki/Bier#Unterg.C3.A4rige_Biere
 			final Node ale = createBeertype(db, "Ale", "Ale ist ein althergebrachter Begriff für ein fermentiertes alkoholisches Getränk, das hauptsächlich aus gemälzter Gerste hergestellt wird. Ale ist in Grossbritannien beheimatet.");
 			final Node altbier = createBeertype(db, "Altbier", "Altbier (oft nur Alt genannt) ist eine dunkle obergärige Biersorte, die überwiegend am Niederrhein getrunken wird.");
 			final Node berlinerWeisse = createBeertype(
 					db,
 					"Berliner Weisse",
 					"Die Berliner Weisse ist ein obergäriges Schankbier aus Gersten- und Weizenmalz. Sie hat eine Stammwürze von 7-8% und einen Alkoholgehalt von ca. 2.8% vol. Ihre Farbe ist ein dunkles, leicht hefetrübes Gelb. Anders als bei den meisten anderen Biersorten folgt bei der Berliner Weissen auf die alkoholische Gärung eine zweite Milchsäuregärung bei etwas höheren Temperaturen. Das verlängert einerseits die Haltbarkeit erheblich, sorgt andererseits für vergleichsweise sauren Geschmack. Das Bier wird heute meist in Kombination mit süssem Fruchtsirup getrunken.");
 			final Node gose = createBeertype(
 					db,
 					"Gose",
 					"Die Gose ist eine Biersorte, die ursprünglich aus der alten Kaiserstadt Goslar am Harz stammt. Sie stellt einen eigenen, alten Biertyp dar, der eine gewisse Ähnlichkeit sowohl mit Berliner Weiße, als auch mit belgischen Lambicbieren bzw. deren Spezialform, der Geuze, aufweist.");
 			final Node haferbier = createBeertype(
 					db,
 					"Haferbier",
 					"Haferbier wird aus Hafermalz gebraut, oft unter Zusatz von Gerstenmalz, Hopfen, Hefe und Wasser. Es handelt sich dabei um ein leicht säuerlich schmeckendes, sehr erfrischendes Getränk. Der Geschmack ist in etwa zwischen Berliner Weiße und Hefeweizen angesiedelt.");
 			final Node koelsch = createBeertype(db, "Kölsch",
 					"Kölsch ist ein helles, blankes (gefiltertes), hopfenbetontes und obergäriges Vollbier mit einer durchschnittlichen Stammwürze von 11.3% und einem Alkoholgehalt von durchschnittlich 4.8%.");
 			final Node porter = createBeertype(
 					db,
 					"Porter",
 					"Porter ist ein dunkles, oft tiefschwarzes Bier mit einem malzigen oder sogar röstmalzbetontem Geschmack. Traditionell war Porter häufig stark gehopft und daher herb. Heute steht der Begriff für verschiedene Arten von Bier. Im englischen Sprachraum bezeichnet es ein meist (aber nicht immer) obergäriges Bier mit einem Alkoholgehalt von 5%.");
 			final Node roggenbier = createBeertype(
 					db,
 					"Roggenbier",
 					"Roggenbier ist ein Bier zu dessen Herstellung Roggen als Stärkelieferant für die Alkoholische Gärung eingesetzt wird. Es ist üblicherweise dunkel und obergärig, es wird in der Regel mit einer Weizenbierhefe vergoren und hat schon deshalb eine gewisse Ähnlichkeit mit dunklem Weizenbier. Wie jenes ist es in der Regel trüb (oft trüber als Weizenbier) und verfügt über deutliche Fruchtnoten.");
 			final Node stout = createBeertype(
 					db,
 					"Stout",
 					"Das Stout ist ein schwarzbraunes, röst- und/oder hopfenbitteres, obergäriges Bier mit einer ausgeprägten, cremefarbenen Schaumkrone. Es wird mit besonders stark geröstetem Gerstenmalz oder Röstgerste gebraut und ist vor allem in englischsprachigen Ländern beliebt. Bekannte Beispiele für Stouts sind Guinness und Murphy's.");
 			final Node dinkelbier = createBeertype(
 					db,
 					"Dinkelbier",
 					"Dinkelbier ist wie Weizenbier eine obergärige Biersorte. Der Dinkel gilt als Vorläufer des modernen Weizens und wird in der Brautechnik ähnlich behandelt. Geschmacklich liegt Dinkelbier in der Varianz der verschiedenen Weizenbiere. Für die Herstellung von Dinkelbier gilt das Reinheitsgebot von 1516.");
 			final Node weizenbier = createBeertype(db, "Weizenbier",
 					"Das Aroma der obergärigen Hefe, die Brimse, prägt den Charakter der Weissbiere. Die Resthefe im Flaschenboden trübt die Flüssigkeit und verleiht den Bieren meist einen leichten Bananen-Nelken Geschmack.");
 			final Node emmerbier = createBeertype(
 					db,
 					"Emmerbier",
 					"Emmerbier ist eine spezielle obergärige Biersorte, die aus Emmer (Triticum dicoccum) gebraut wird. Es wird hergestellt aus Malz, darunter über 50% Emmer, dazu Einkorn, Dinkel, Weizen und Gerste sowie Naturhopfen. Das Bier erhält ein bernsteinfarbenes, eher naturtrübes Aussehen und einen ausgeprägt malzaromatischen Geschmack.");
 			final Node exportbier = createBeertype(db, "Exportbier",
 					"Exportbier ist im deutschsprachigen Raum ein untergäriges Vollbier mit einer Stammwürze von 12% bis 14% und einem Alkoholgehalt von meistens etwas über 5% vol. Es kann hell oder dunkel sein.");
 			final Node helles = createBeertype(db, "Helles",
 					"Helles als Biersorte ist vor allem in Bayern ein untergäriges, gelbes Bier mit einer Stammwürze von 11–13% und einem Alkoholgehalt von 4.5–6% vol. Dabei gibt es keine scharfe Grenze zu den Biersorten Lager und Export.");
 			final Node lagerbier = createBeertype(db, "Lagerbier",
 					"Als Lagerbier werden in Deutschland heute Biere bezeichnet, die den Stammwürzegehalt eines Vollbieres aufweisen, im Gegensatz zum Pilsner Bier jedoch in der Regel nur schwach gehopft sind.");
 			final Node muenchnerDunkel = createBeertype(
 					db,
 					"Münchner Dunkel",
 					"Münchner Dunkel ist eine traditionelle untergärige Münchner Biersorte. Es variiert farblich je nach Herstellungsverfahren und Zutaten zwischen kräftig kupferrot bis dunkelbraun, ist nur mäßig gehopft und daher eher mild und hat meist eine deutlich malzig-süße Note.");
 			final Node maerzen = createBeertype(
 					db,
 					"Märzen",
 					"Das Märzenbier ist ein untergäriges Lagerbier. Der Begriff Märzenbier wird heutzutage vor allem in Süddeutschland und Österreich für etwas stärkere Lagerbiere verwendet, die eigentlich in die Kategorie Exportbier fallen. In Österreich ist Märzen überall verbreitet und sozusagen das \"Standardbier\" des Landes. Im englischen Sprachraum ist Märzen ein Synonym für Oktoberfestbier.");
 			final Node pils = createBeertype(
 					db,
 					"Pils",
 					"Pils(e)ner Bier, auch Pils genannt, ist ein nach der böhmischen Stadt Pilsen benanntes, untergäriges Lagerbier mit im Vergleich zu anderen Biersorten erhöhtem Hopfengehalt (und dementsprechend auch starkem Hopfenaroma) und höchstens 12.5% Stammwürzegehalt. Nach Pilsner Brauart hergestellte Biere bilden heute den Großteil der in Deutschland produzierten und verkauften Biere.");
 			final Node schwarzbier = createBeertype(
 					db,
 					"Schwarzbier",
 					"Schwarzbiere sind dunkle Vollbiere, die heute - anders als früher - meist untergärig hergestellt werden. Seine dunkle Farbe erhält Schwarzbier meist durch die Verwendung dunklen Braumalzes oder Röstmalzes, was ihm die typisch röstige Note gibt. Der Stammwürzegehalt beträgt mindestens 11%.");
 
 			beertypeIndex.createRelationshipTo(ale, RelType.INDEXES);
 			beertypeIndex.createRelationshipTo(altbier, RelType.INDEXES);
 			beertypeIndex.createRelationshipTo(berlinerWeisse, RelType.INDEXES);
 			beertypeIndex.createRelationshipTo(gose, RelType.INDEXES);
 			beertypeIndex.createRelationshipTo(haferbier, RelType.INDEXES);
 			beertypeIndex.createRelationshipTo(koelsch, RelType.INDEXES);
 			beertypeIndex.createRelationshipTo(porter, RelType.INDEXES);
 			beertypeIndex.createRelationshipTo(roggenbier, RelType.INDEXES);
 			beertypeIndex.createRelationshipTo(stout, RelType.INDEXES);
 			beertypeIndex.createRelationshipTo(dinkelbier, RelType.INDEXES);
 			beertypeIndex.createRelationshipTo(weizenbier, RelType.INDEXES);
 			beertypeIndex.createRelationshipTo(emmerbier, RelType.INDEXES);
 			beertypeIndex.createRelationshipTo(exportbier, RelType.INDEXES);
 			beertypeIndex.createRelationshipTo(helles, RelType.INDEXES);
 			beertypeIndex.createRelationshipTo(lagerbier, RelType.INDEXES);
 			beertypeIndex.createRelationshipTo(muenchnerDunkel, RelType.INDEXES);
 			beertypeIndex.createRelationshipTo(maerzen, RelType.INDEXES);
 			beertypeIndex.createRelationshipTo(pils, RelType.INDEXES);
 			beertypeIndex.createRelationshipTo(schwarzbier, RelType.INDEXES);
 
 			/* CREATE BEERS */
 
 			final Node feldschloesschen = createBeerNode(db, "Feldschlösschen", "Feldschlösschen Original", "");
 			final Node falken = createBeerNode(db, "Falken", "Falken First Cool", "");
 			final Node calanda = createBeerNode(db, "Calanda", "Calanda Meisterbräu", "");
 			final Node waedibraeu = createBeerNode(db, "Wädibräu", "Ur-Weizen Wädibräu", "");
 			final Node guinness = createBeerNode(db, "Guinness", "Guinness Draught", "");
 			final Node kilkenny = createBeerNode(db, "Kilkenny", "Kilkenny", "");
 			final Node quoellfrisch = createBeerNode(db, "Appenzeller Bier", "Quöllfrisch blond", "");
 			final Node vollmond = createBeerNode(db, "Appenzeller Bier", "Vollmond Bier blond", "");
 			final Node holzfass = createBeerNode(db, "Appenzeller Bier", "Holzfass Bier", "");
 
 			beerIndex.createRelationshipTo(feldschloesschen, RelType.INDEXES);
 			beerIndex.createRelationshipTo(falken, RelType.INDEXES);
 			beerIndex.createRelationshipTo(calanda, RelType.INDEXES);
 			beerIndex.createRelationshipTo(waedibraeu, RelType.INDEXES);
 			beerIndex.createRelationshipTo(guinness, RelType.INDEXES);
 			beerIndex.createRelationshipTo(kilkenny, RelType.INDEXES);
 			beerIndex.createRelationshipTo(quoellfrisch, RelType.INDEXES);
 			beerIndex.createRelationshipTo(vollmond, RelType.INDEXES);
 			beerIndex.createRelationshipTo(holzfass, RelType.INDEXES);
 
 			/* CREATE BEER TO BEERTYPE RELATIONS */
 
 			feldschloesschen.createRelationshipTo(exportbier, RelType.HAS_BEERTYPE);
 			falken.createRelationshipTo(helles, RelType.HAS_BEERTYPE);
 			calanda.createRelationshipTo(pils, RelType.HAS_BEERTYPE);
 			waedibraeu.createRelationshipTo(weizenbier, RelType.HAS_BEERTYPE);
 			guinness.createRelationshipTo(stout, RelType.HAS_BEERTYPE);
 			kilkenny.createRelationshipTo(stout, RelType.HAS_BEERTYPE);
 			quoellfrisch.createRelationshipTo(lagerbier, RelType.HAS_BEERTYPE);
 			vollmond.createRelationshipTo(pils, RelType.HAS_BEERTYPE);
 			holzfass.createRelationshipTo(pils, RelType.HAS_BEERTYPE);
 
 			/* CREATE TAGS */
 
 			final Node lecker = createTag(db, "lecker");
 			final Node wuerzig = createTag(db, "wuerzig");
 			final Node suess = createTag(db, "suess");
 			final Node leicht = createTag(db, "leicht");
 			final Node billig = createTag(db, "billig");
 			final Node gruusig = createTag(db, "gruusig");
 			final Node irisch = createTag(db, "irisch");
 			final Node deutsch = createTag(db, "deutsch");
 			final Node schweiz = createTag(db, "schweiz");
 			final Node dunkel = createTag(db, "dunkel");
 			final Node hell = createTag(db, "hell");
 			final Node rot = createTag(db, "rot");
 			final Node holzig = createTag(db, "holzig");
 
 			tagIndex.createRelationshipTo(lecker, RelType.INDEXES);
 			tagIndex.createRelationshipTo(wuerzig, RelType.INDEXES);
 			tagIndex.createRelationshipTo(suess, RelType.INDEXES);
 			tagIndex.createRelationshipTo(leicht, RelType.INDEXES);
 			tagIndex.createRelationshipTo(billig, RelType.INDEXES);
 			tagIndex.createRelationshipTo(gruusig, RelType.INDEXES);
 			tagIndex.createRelationshipTo(irisch, RelType.INDEXES);
 			tagIndex.createRelationshipTo(deutsch, RelType.INDEXES);
 			tagIndex.createRelationshipTo(schweiz, RelType.INDEXES);
 			tagIndex.createRelationshipTo(dunkel, RelType.INDEXES);
 			tagIndex.createRelationshipTo(hell, RelType.INDEXES);
 			tagIndex.createRelationshipTo(rot, RelType.INDEXES);
 			tagIndex.createRelationshipTo(holzig, RelType.INDEXES);
 
 			/* ADD TAGS TO BEERS */
 
 			feldschloesschen.createRelationshipTo(gruusig, RelType.HAS_TAG);
 			feldschloesschen.createRelationshipTo(hell, RelType.HAS_TAG);
 			falken.createRelationshipTo(billig, RelType.HAS_TAG);
 			falken.createRelationshipTo(leicht, RelType.HAS_TAG);
 			waedibraeu.createRelationshipTo(wuerzig, RelType.HAS_TAG);
 			waedibraeu.createRelationshipTo(hell, RelType.HAS_TAG);
 			calanda.createRelationshipTo(lecker, RelType.HAS_TAG);
 			calanda.createRelationshipTo(suess, RelType.HAS_TAG);
 			guinness.createRelationshipTo(irisch, RelType.HAS_TAG);
 			guinness.createRelationshipTo(wuerzig, RelType.HAS_TAG);
 			guinness.createRelationshipTo(dunkel, RelType.HAS_TAG);
 			kilkenny.createRelationshipTo(irisch, RelType.HAS_TAG);
 			kilkenny.createRelationshipTo(rot, RelType.HAS_TAG);
 			holzfass.createRelationshipTo(wuerzig, RelType.HAS_TAG);
 			holzfass.createRelationshipTo(holzig, RelType.HAS_TAG);
 
 			/* CREATE USERS */
 
			final Node danilo = createUser(db, "danilo", "bargen", "bargi@beer.ch", "danilo", "$2$10$357b306e9593a80fb8241uBaNyWsVkUkNokf0hnkoqhAsxNhJgK2i");
			final Node jonas = createUser(db, "jonas", "furrer", "jonas@beer.ch", "jonas", "$2$10$582c567e42c6ebf719891uLa7l5b3qWwl.Ma//pS3JypF2WJkDTMi");
			final Node chrigi = createUser(db, "chrigi", "fässler", "chrigi@beer.ch", "chrigi", "$2$10$2f8ed556165a7642f0a9eu3I4DaCM35Gzo043e6wd16TFangjzvE6");
			final Node urs = createUser(db, "urs", "baumann", "urs@beer.ch", "urs", getSHA1("creat user : user with password"));
 
 			userIndex.createRelationshipTo(danilo, RelType.INDEXES);
 			userIndex.createRelationshipTo(jonas, RelType.INDEXES);
 			userIndex.createRelationshipTo(chrigi, RelType.INDEXES);
 			userIndex.createRelationshipTo(urs, RelType.INDEXES);
 
 			/* CREATE RATINGS */
 
 			final Node rating1 = createRating(db, 1);
 			final Node rating2 = createRating(db, 2);
 			final Node rating3 = createRating(db, 3);
 			final Node rating4 = createRating(db, 5);
 			final Node rating5 = createRating(db, 4);
 			final Node rating6 = createRating(db, 3);
 
 			timelineIndex.createRelationshipTo(rating1, RelType.INDEXES);
 			timelineIndex.createRelationshipTo(rating2, RelType.INDEXES);
 			timelineIndex.createRelationshipTo(rating3, RelType.INDEXES);
 			timelineIndex.createRelationshipTo(rating4, RelType.INDEXES);
 			timelineIndex.createRelationshipTo(rating5, RelType.INDEXES);
 			timelineIndex.createRelationshipTo(rating6, RelType.INDEXES);
 
 			jonas.createRelationshipTo(rating1, RelType.DOES);
 			rating1.createRelationshipTo(feldschloesschen, RelType.CONTAINS);
 
 			jonas.createRelationshipTo(rating2, RelType.DOES);
 			rating2.createRelationshipTo(falken, RelType.CONTAINS);
 
 			jonas.createRelationshipTo(rating3, RelType.DOES);
 			rating3.createRelationshipTo(calanda, RelType.CONTAINS);
 
 			chrigi.createRelationshipTo(rating4, RelType.DOES);
 			rating4.createRelationshipTo(calanda, RelType.CONTAINS);
 
 			danilo.createRelationshipTo(rating5, RelType.DOES);
 			rating5.createRelationshipTo(feldschloesschen, RelType.CONTAINS);
 
 			danilo.createRelationshipTo(rating6, RelType.DOES);
 			rating6.createRelationshipTo(feldschloesschen, RelType.CONTAINS);
 
 			/* CREATE CONSUMPTIONS */
 
 			final Node c1 = createConsumption(db);
 			final Node c2 = createConsumption(db);
 			final Node c3 = createConsumption(db);
 			final Node c4 = createConsumption(db);
 			final Node c5 = createConsumption(db);
 
 			timelineIndex.createRelationshipTo(c1, RelType.INDEXES);
 			timelineIndex.createRelationshipTo(c2, RelType.INDEXES);
 			timelineIndex.createRelationshipTo(c3, RelType.INDEXES);
 			timelineIndex.createRelationshipTo(c4, RelType.INDEXES);
 			timelineIndex.createRelationshipTo(c5, RelType.INDEXES);
 
 			c1.createRelationshipTo(feldschloesschen, RelType.CONTAINS);
 			jonas.createRelationshipTo(c1, RelType.DOES);
 
 			c2.createRelationshipTo(feldschloesschen, RelType.CONTAINS);
 			chrigi.createRelationshipTo(c2, RelType.DOES);
 
 			c3.createRelationshipTo(calanda, RelType.CONTAINS);
 			urs.createRelationshipTo(c3, RelType.DOES);
 
 			c4.createRelationshipTo(falken, RelType.CONTAINS);
 			urs.createRelationshipTo(c4, RelType.DOES);
 
 			c5.createRelationshipTo(waedibraeu, RelType.CONTAINS);
 			danilo.createRelationshipTo(c5, RelType.DOES);
 
 			/* INSERT CONSUMPTION TO TIMELINE */
 
 			long now = System.currentTimeMillis();
 			final int diff = 3;
 			rating1.setProperty(NodeProperty.Action.TIMESTAMP, now += diff);
 			c1.setProperty(NodeProperty.Action.TIMESTAMP, now += diff);
 			c2.setProperty(NodeProperty.Action.TIMESTAMP, now += diff);
 			rating2.setProperty(NodeProperty.Action.TIMESTAMP, now += diff);
 			c3.setProperty(NodeProperty.Action.TIMESTAMP, now += diff);
 			rating3.setProperty(NodeProperty.Action.TIMESTAMP, now += diff);
 			rating4.setProperty(NodeProperty.Action.TIMESTAMP, now += diff);
 			c4.setProperty(NodeProperty.Action.TIMESTAMP, now += diff);
 			c5.setProperty(NodeProperty.Action.TIMESTAMP, now += diff);
 			rating5.setProperty(NodeProperty.Action.TIMESTAMP, now += diff);
 			rating6.setProperty(NodeProperty.Action.TIMESTAMP, now += diff);
 
 			addAction(db, rating1);
 			addAction(db, c1);
 			addAction(db, c2);
 			addAction(db, rating2);
 			addAction(db, c3);
 			addAction(db, rating3);
 			addAction(db, rating4);
 			addAction(db, c4);
 			addAction(db, c5);
 			addAction(db, rating5);
 			addAction(db, rating6);
 
 			/* CREATE BREWRIES */
 
 			final Node calandaAg = createBrewery(db, "Calanda", "national", "Calanda ist eine schweizer Traditions-Brauerei. Gegründet wurde sie im Jahre...", "");
 			final Node feldschloesschenAg = createBrewery(db, "Felschlösschen", "national", "Feldschlösschen ist eine gesichtslose und gänzlich uninspirierte Brauerei. Sie wurde im Jahre ...", "");
 			final Node falkenAg = createBrewery(db, "Falken Brauerei", "regional", "Falke, der. Ein majestätischer Jagdvogel, besonders beliebt bei Grafen und Baronen.", "");
 			final Node waedibraeuAg = createBrewery(db, "Wädibräu", "regional", "Eine kleine aber feine regional Brauerei. Wädibräu stellt Bier in rauen Mengen her und hat noch lange nicht genug.", "");
 			final Node guinnessBrewery = createBrewery(db, "Guinness Brewery", "national", "Wurde von Arthur Guinness im Jahr 1759 in Dublin gegründet.", "");
 			final Node stFrancisAbbeyBrewery = createBrewery(db, "St. Francis Abbey Brewery", "national", "Die älteste irische Brauerei, welche unter Anderem das Kilkenny Bier braut.", "");
 			final Node locherAg = createBrewery(db, "Brauerei Locher AG", "national", "Die Brauerei Locher ist ein traditionsreicher Familienbetrieb in Appenzell. Seit Mitte der 1990er Jahre entwickelte sie sich von einer nur lokal aktiven zu einer in der ganzen Schweiz (und darüber hinaus) bekannten Brauerei für innovative Spezialbiere.", "");
 
 			breweryIndex.createRelationshipTo(calandaAg, RelType.INDEXES);
 			breweryIndex.createRelationshipTo(feldschloesschenAg, RelType.INDEXES);
 			breweryIndex.createRelationshipTo(falkenAg, RelType.INDEXES);
 			breweryIndex.createRelationshipTo(waedibraeuAg, RelType.INDEXES);
 			breweryIndex.createRelationshipTo(guinnessBrewery, RelType.INDEXES);
 			breweryIndex.createRelationshipTo(stFrancisAbbeyBrewery, RelType.INDEXES);
 			breweryIndex.createRelationshipTo(locherAg, RelType.INDEXES);
 
 			calanda.createRelationshipTo(calandaAg, RelType.BREWN_BY);
 			falken.createRelationshipTo(falkenAg, RelType.BREWN_BY);
 			feldschloesschen.createRelationshipTo(feldschloesschenAg, RelType.BREWN_BY);
 			waedibraeu.createRelationshipTo(waedibraeuAg, RelType.BREWN_BY);
 			guinness.createRelationshipTo(guinnessBrewery, RelType.BREWN_BY);
 			kilkenny.createRelationshipTo(stFrancisAbbeyBrewery, RelType.BREWN_BY);
 			quoellfrisch.createRelationshipTo(locherAg, RelType.BREWN_BY);
 			vollmond.createRelationshipTo(locherAg, RelType.BREWN_BY);
 			holzfass.createRelationshipTo(locherAg, RelType.BREWN_BY);
 
 			final Node activeRatingIndex = db.createNode();
 			rootNode.createRelationshipTo(activeRatingIndex, RelType.INDEX_ACTIVERATINGINDEX);
 
 			activeRatingIndex.createRelationshipTo(rating6, RelType.INDEXES_ACTIVE);
 			activeRatingIndex.createRelationshipTo(rating1, RelType.INDEXES_ACTIVE);
 			activeRatingIndex.createRelationshipTo(rating2, RelType.INDEXES_ACTIVE);
 			activeRatingIndex.createRelationshipTo(rating3, RelType.INDEXES_ACTIVE);
 			activeRatingIndex.createRelationshipTo(rating4, RelType.INDEXES_ACTIVE);
 
 			/* CREATE TESTUSER */
 
 			final Node testuser = createUser(db, "Test", "User", "test@nusszipfel.com", "testuser", "$2$10$ae5deb822e0d719929004uD0KL0l5rHNCSFKcfBvoTzG5Og6O/Xxu");
 			userIndex.createRelationshipTo(testuser, RelType.INDEXES);
 			
 			/* CREATE MISTERIOUS UNKNOWN NODES */
 
 			final Node unknownIdex = db.createNode();
 			rootNode.createRelationshipTo(unknownIdex, RelType.INDEX_UNKNOWN);
 			
 			final Node unknownBrewery = createBrewery(db, "Unknown", "international", "Nothing is known about this misterious brewery. It seems to be totally unknown!", "");
 			unknownBrewery.setProperty("unknownnode", true);
 			unknownIdex.createRelationshipTo(unknownBrewery, RelType.INDEXES); // not connected to breweryindex, to hide in lists
 			
 			final Node unknownBeertype = createBeertype(db, "Unknown", "This is a very strange type of beer, nobody knows it. Maybe it has a hilariously strange taste.");
 			unknownBeertype.setProperty("unknownnode", true);
 			unknownIdex.createRelationshipTo(unknownBeertype, RelType.INDEXES); // not connected to beertypeindex, to hide in lists
 
 			/* CREATE BARCODE NODES */
 
 			final Node barcodeIndex = db.createNode();
 			rootNode.createRelationshipTo(barcodeIndex, RelType.INDEX_BARCODE);
 
 			final Node barcodeQuoellfrisch1 = createBarcode(db, "7611889110310", "EAN_13");
 			barcodeIndex.createRelationshipTo(barcodeQuoellfrisch1, RelType.INDEXES);
 			quoellfrisch.createRelationshipTo(barcodeQuoellfrisch1, RelType.HAS_BARCODE);
 
 			final Node barcodeQuoellfrisch2 = createBarcode(db, "7611889110662", "EAN_13");
 			barcodeIndex.createRelationshipTo(barcodeQuoellfrisch2, RelType.INDEXES);
 			quoellfrisch.createRelationshipTo(barcodeQuoellfrisch2, RelType.HAS_BARCODE);
 
 			final Node barcodeFeldschloesschen1 = createBarcode(db, "76129049");
 			barcodeIndex.createRelationshipTo(barcodeFeldschloesschen1, RelType.INDEXES);
 			feldschloesschen.createRelationshipTo(barcodeFeldschloesschen1, RelType.HAS_BARCODE);
 
 			final Node barcodeFeldschloesschen2 = createBarcode(db, "76129810");
 			barcodeIndex.createRelationshipTo(barcodeFeldschloesschen2, RelType.INDEXES);
 			feldschloesschen.createRelationshipTo(barcodeFeldschloesschen2, RelType.HAS_BARCODE);
 
 			final Node barcodeFeldschloesschen3 = createBarcode(db, "76129155");
 			barcodeIndex.createRelationshipTo(barcodeFeldschloesschen3, RelType.INDEXES);
 			feldschloesschen.createRelationshipTo(barcodeFeldschloesschen3, RelType.HAS_BARCODE);
 
 			final Node barcodeFeldschloesschen4 = createBarcode(db, "7612900024517", "EAN_13");
 			barcodeIndex.createRelationshipTo(barcodeFeldschloesschen4, RelType.INDEXES);
 			feldschloesschen.createRelationshipTo(barcodeFeldschloesschen4, RelType.HAS_BARCODE);
 
 			final Node barcodeCalanda1 = createBarcode(db, "7610055007508", "EAN_13");
 			barcodeIndex.createRelationshipTo(barcodeCalanda1, RelType.INDEXES);
 			calanda.createRelationshipTo(barcodeCalanda1, RelType.HAS_BARCODE);
 
 			final Node barcodeCalanda2 = createBarcode(db, "76400803");
 			barcodeIndex.createRelationshipTo(barcodeCalanda2, RelType.INDEXES);
 			calanda.createRelationshipTo(barcodeCalanda2, RelType.HAS_BARCODE);
 
 			final Node barcodeCalanda3 = createBarcode(db, "7610055007508", "EAN_13");
 			barcodeIndex.createRelationshipTo(barcodeCalanda3, RelType.INDEXES);
 			calanda.createRelationshipTo(barcodeCalanda3, RelType.HAS_BARCODE);
 
 			transaction.success();
 		} finally {
 			transaction.finish();
 		}
 
 		return db;
 	}
 
 	private static void deleteDir(String path) {
 		final File file = new File(path);
 		try {
 			for (File f : file.listFiles()) {
 				f.delete();
 			}
 			// SUPPRESS CHECKSTYLE: we want to catch any possible exception.
 		} catch (Exception e) {
 			LOG.error(e.getMessage(), e);
 		}
 	}
 
 	private static Node createTag(EmbeddedGraphDatabase db, String name) {
 		final Node tag = db.createNode();
 		tag.setProperty("type", "tag");
 		tag.setProperty("name", name);
 		return tag;
 	}
 
 	/**
 	 * Create a beertype node.
 	 * 
 	 * @param db
 	 *            Database
 	 * @param name
 	 *            Name
 	 * @param description
 	 *            Description
 	 * @return Beertype node
 	 */
 	private static Node createBeertype(EmbeddedGraphDatabase db, String name, String description) {
 		final Node beertype = db.createNode();
 		beertype.setProperty("type", "beertype");
 		beertype.setProperty("name", name);
 		beertype.setProperty("description", description);
 		return beertype;
 	}
 
 	/**
 	 * Create a beer node.
 	 * 
 	 * @param db
 	 *            Database
 	 * @param brand
 	 *            Brand
 	 * @param name
 	 *            Name
 	 * @param image
 	 *            Image
 	 * @return Beer node
 	 */
 	private static Node createBeerNode(EmbeddedGraphDatabase db, String brand, String name, String image) {
 		final Node beer = db.createNode();
 		beer.setProperty("type", "beer");
 		beer.setProperty("brand", brand);
 		beer.setProperty("name", name);
 		beer.setProperty("image", image);
 		return beer;
 	}
 
 	/**
 	 * Create a user node.
 	 * 
 	 * @param db
 	 *            Database
 	 * @param prename
 	 *            Prename
 	 * @param surname
 	 *            Surname
 	 * @param email
 	 *            Email
 	 * @param username
 	 *            Username
 	 * @param password
 	 *            Password (SHA-1)
 	 * @return User node
 	 */
 	private static Node createUser(EmbeddedGraphDatabase db, String prename, String surname, String email, String username, String password) {
 		final Node user = db.createNode();
 		user.setProperty("type", "user");
 		user.setProperty("prename", prename);
 		user.setProperty("surname", surname);
 		user.setProperty("email", email);
 		user.setProperty("username", username);
 		user.setProperty("password", password);
 		return user;
 	}
 
 	private static Node createConsumption(EmbeddedGraphDatabase db) {
 		final Node consumption = db.createNode();
 		consumption.setProperty("type", "consumption");
 		consumption.setProperty("timestamp", System.currentTimeMillis());
 		return consumption;
 	}
 
 	private static Node createRating(EmbeddedGraphDatabase db, int value) {
 		final Node rating = db.createNode();
 		rating.setProperty("type", "rating");
 		rating.setProperty("timestamp", System.currentTimeMillis());
 		rating.setProperty("rating", value);
 		return rating;
 	}
 
 	private static Node createBrewery(EmbeddedGraphDatabase db, String name, String size, String description, String picture) {
 		final Node brewery = db.createNode();
 		brewery.setProperty("type", "brewery");
 		brewery.setProperty("name", name);
 		brewery.setProperty("size", size);
 		brewery.setProperty("description", description);
 		brewery.setProperty("picture", picture);
 		return brewery;
 	}
 
 	private static Node createBarcode(EmbeddedGraphDatabase db, String code) {
 		return createBarcode(db, code, "");
 	}
 
 	private static Node createBarcode(EmbeddedGraphDatabase db, String code, String format) {
 		final Node barcodeNode = db.createNode();
 		barcodeNode.setProperty("type", "barcode");
 		barcodeNode.setProperty("code", code);
 		barcodeNode.setProperty("format", format);
 		return barcodeNode;
 	}
 
 	private static String getSHA1(String pw) {
 		MessageDigest md = null;
 		try {
 			md = MessageDigest.getInstance("SHA-1");
 		} catch (NoSuchAlgorithmException e) {
 			LOG.error(e.getMessage(), e);
 		}
 		return new String(md.digest(pw.getBytes()));
 	}
 
 	private static void addAction(EmbeddedGraphDatabase db, Node newAction) {
 		final Node home = db.getReferenceNode();
 		final Node timeLineStart = home.getSingleRelationship(RelType.INDEX_TIMELINESTART, Direction.OUTGOING).getEndNode();
 		final Relationship relationToNext = timeLineStart.getSingleRelationship(RelType.NEXT, Direction.OUTGOING);
 		final Node next = relationToNext.getEndNode();
 		relationToNext.delete();
 		timeLineStart.createRelationshipTo(newAction, RelType.NEXT);
 		newAction.createRelationshipTo(next, RelType.NEXT);
 	}
 
 	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 			@Override
 			public void run() {
 				graphDb.shutdown();
 				SRV.stop();
 			}
 		});
 	}
 
 }
