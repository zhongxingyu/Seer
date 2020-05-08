 /*
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /*
  * Program for generating universes. This allows decent looking, arbitrarily
  * large worlds to be made automatically, and then manaully tuned.
  */
 package lib;
 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.Random;
 import lib.Parser.Term;
 import universe.Universe;
 
 /**
  *
  * @author nwiehoff
  */
 public class WorldMaker {
     //sample
 
     private final char[] generic = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
         'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2',
         '3', '4', '5', '6', '7', '8', '9', '0'};
     private String[] greek = {"Alpha", "Beta", "Gamma", "Delta", "Epsilon", "Zeta", "Eta", "Theta", "Iota",
         "Kappa", "Lambda", "Mu", "Nu", "Xi", "Omicron", "Pi", "Rho", "Sigma", "Tau", "Upsilon", "Phi", "Chi",
         "Psi", "Omega"};
     //rng
     Random rnd = new Random();
     //used names
     private ArrayList<String> usedSystemNames = new ArrayList<>();
 
     public WorldMaker() {
         //generate universe
         String out = generate(1, 10, 90, 150, 1000, 16000, 48000, 900, 2000, 0, 100);
         //save
         AstralIO tmp = new AstralIO();
         tmp.writeFile("/tmp/UNIVERSE.txt", out);
         System.out.println(out);
     }
 
     public static void main(String[] args) {
         new WorldMaker();
     }
 
     public final String generate(int minPlanetsPerSystem, int maxPlanetsPerSystem, int minSystems, int maxSystems,
             int worldSize, int minSystemSize, int maxSystemSize, int minPlanetSize,
             int maxPlanetSize, int minAsteroidsPerSystem, int maxAsteroidsPerSystem) {
         String ret = "";
         {
             //precache parsers
             Parser sky = new Parser("SKY.txt");
             ArrayList<Term> skyTypes = sky.getTermsOfType("Skybox");
             Parser stars = new Parser("PLANET.txt");
             ArrayList<Term> starTypes = stars.getTermsOfType("Star");
             /*Parser particle = new Parser("PARTICLE.txt");
              ArrayList<Term> nebTypes = particle.getTermsOfType("Nebula");*/
             Parser planets = new Parser("PLANET.txt");
             ArrayList<Term> planetTypes = planets.getTermsOfType("Planet");
             //determine the number of systems to make
             int numSystems = rnd.nextInt(maxSystems);
             if (numSystems < minSystems) {
                 numSystems = minSystems;
             }
             //generate syslings
             ArrayList<Sysling> syslings = makeSyslings(numSystems, rnd, worldSize);
             //sort syslings
             for (int a = 0; a < syslings.size(); a++) {
                 syslings.get(a).sortBuddy(syslings);
             }
             //drop sov in syslings
             dropSov(syslings);
             //generate each system
             for (int a = 0; a < syslings.size(); a++) {
                 System.out.println("Generating Universe - " + (a + 1) + "/" + numSystems);
                 ArrayList<Simpling> objects = new ArrayList<>();
                 String thisSystem = "";
                 {
                     //get sysling
                     Sysling sys = syslings.get(a);
                     double x = sys.getLoc().x;
                     double y = sys.getLoc().y;
                     String systemName = sys.getName();
                     //pick size
                     int dRS = maxSystemSize - minSystemSize;
                     int dRB = (int) (rnd.nextFloat() * dRS);
                     int size = minSystemSize + dRB;
                     //determine skybox
                     int pick = rnd.nextInt(skyTypes.size());
                     //add some randomization
                     x += 2.0 * rnd.nextDouble() - 1;
                     y += 2.0 * rnd.nextDouble() - 1;
                     //create the system entry
                     thisSystem +=
                             "[System]\n"
                             + "name=" + systemName + "\n"
                             + "owner=" + sys.getOwner() + "\n"
                             + "x=" + x + "\n"
                             + "y=" + y + "\n"
                             + "sky=" + skyTypes.get(pick).getValue("name") + "\n"
                             + "ambient=" + sys.getAmbientMusic() + "\n"
                             + "danger=" + sys.getDangerMusic() + "\n"
                             + "[/System]\n\n";
                     //get star types
                     pick = rnd.nextInt(starTypes.size());
                     //create a star in the relative center of the system
                     x = rnd.nextInt(size / 4) - size / 8;
                     y = rnd.nextInt(size / 4) - size / 8;
                     int dR = maxPlanetSize - minPlanetSize;
                     int dB = (int) (rnd.nextFloat() * dR);
                     int r = minPlanetSize + dB;
                     int seed = rnd.nextInt();
                     thisSystem +=
                             "[Star]\n"
                             + "name=" + systemName + "\n"
                             + "system=" + systemName + "\n"
                             + "texture=" + starTypes.get(pick).getValue("name") + "\n"
                             + "x=" + x + "\n"
                             + "y=" + y + "\n"
                             + "d=" + 2 * r + "\n"
                             + "seed=" + seed + "\n"
                             + "[/Star]\n\n";
                     //add a simpling for testing
                     objects.add(new Simpling(new Point2D.Float((float) x, (float) y), 4 * r));
                     /*
                      * CREATE JUMPHOLES
                      */
                     //calculate the number of connections to make
                    int density = rnd.nextInt(4) + 1;
                     for (int v = 0; v < density; v++) {
                         //get the sysling to connect to
                         Sysling in = sys;
                         Sysling out = sys.findBuddy(null, v + 1);
                         if (!in.connectedTo(out) && !out.connectedTo(in)) {
                             //name holes
                             String inName = out.getName() + " Jumphole";
                             String outName = sys.getName() + " Jumphole";
                             //build in gate
                             x = rnd.nextInt(size * 2) - size;
                             y = rnd.nextInt(size * 2) - size;
                             thisSystem +=
                                     "[Jumphole]\n"
                                     + "name=" + inName + "\n"
                                     + "x=" + x + "\n"
                                     + "y=" + y + "\n"
                                     + "system=" + in.getName() + "\n"
                                     + "out=" + out.getName() + "/" + outName + "\n"
                                     + "[/Jumphole]\n\n";
                             //build out gate
                             x = rnd.nextInt(size * 2) - size;
                             y = rnd.nextInt(size * 2) - size;
                             thisSystem +=
                                     "[Jumphole]\n"
                                     + "name=" + outName + "\n"
                                     + "x=" + x + "\n"
                                     + "y=" + y + "\n"
                                     + "system=" + out.getName() + "\n"
                                     + "out=" + in.getName() + "/" + inName + "\n"
                                     + "[/Jumphole]\n\n";
                             //inform simplings
                             sys.addConnection(out);
                             out.addConnection(sys);
                         }
                     }
                     /*
                      * CREATE PLANETS
                      */
                     //get list of planet assets
                     int numPlanets = rnd.nextInt(maxPlanetsPerSystem);
                     if (numPlanets < minPlanetsPerSystem) {
                         numPlanets = minPlanetsPerSystem;
                     }
                     for (int b = 0; b < numPlanets; b++) {
                         //pick texture
                         pick = rnd.nextInt(planetTypes.size());
                         Term type = planetTypes.get(pick);
                         String texture = type.getValue("name");
                         //pick name
                         String name = randomPlanetName();
                         //pick seed
                         seed = rnd.nextInt();
                         //generate position
                         x = rnd.nextInt(size * 2) - size;
                         y = rnd.nextInt(size * 2) - size;
                         //generate the radius
                         //pick size
                         dRS = maxPlanetSize - minPlanetSize;
                         dRB = (int) (rnd.nextFloat() * dRS);
                         r = minPlanetSize + dRB;
                         //create a simpling for testing
                         Simpling test = new Simpling(new Point2D.Float((float) x, (float) y), r);
                         boolean safe = true;
                         for (int c = 0; c < objects.size(); c++) {
                             if (objects.get(c).collideWith(test)) {
                                 safe = false;
                                 break;
                             }
                         }
                         //if it is safe add it
                         if (safe) {
                             thisSystem +=
                                     "[Planet]\n"
                                     + "name=" + name + "\n"
                                     + "system=" + systemName + "\n"
                                     + "texture=" + texture + "\n"
                                     + "x=" + x + "\n"
                                     + "y=" + y + "\n"
                                     + "d=" + 2 * r + "\n"
                                     + "seed=" + seed + "\n"
                                     + "[/Planet]\n\n";
                             objects.add(test);
                         }
                     }
                     /*
                      * CREATE ASTEROIDS (only in neutral space)
                      */
                     if (sys.getOwner().matches("Neutral")) {
                         int numAsteroids = rnd.nextInt(maxAsteroidsPerSystem);
                         if (numAsteroids < minAsteroidsPerSystem) {
                             numAsteroids = minAsteroidsPerSystem;
                         }
                         for (int b = 0; b < numAsteroids; b++) {
                             //pick name
                             String name = "Asteroid " + b;
                             //generate position
                             x = rnd.nextInt(size * 2) - size;
                             y = rnd.nextInt(size * 2) - size;
                             //generate rotation
                             double theta = rnd.nextFloat() * 2.0 * Math.PI;
                             //pick size
                             dRS = maxPlanetSize - minPlanetSize;
                             dRB = (int) (rnd.nextFloat() * dRS);
                             r = minPlanetSize + dRB;
                             //create a simpling for testing
                             Simpling test = new Simpling(new Point2D.Float((float) x, (float) y), r);
                             boolean safe = true;
                             for (int c = 0; c < objects.size(); c++) {
                                 if (objects.get(c).collideWith(test)) {
                                     safe = false;
                                     break;
                                 }
                             }
                             //if it is safe add it
                             if (safe) {
                                 thisSystem +=
                                         "[Asteroid]\n"
                                         + "name=" + name + "\n"
                                         + "system=" + systemName + "\n"
                                         + "x=" + x + "\n"
                                         + "y=" + y + "\n"
                                         + "t=" + theta + "\n"
                                         + "[/Asteroid]\n\n";
                                 objects.add(test);
                             }
                         }
                     }
                     /*
                      * Add Initial Stations
                      */
                     ArrayList<Statling> stations = sys.getStations();
                     for (int b = 0; b < stations.size(); b++) {
                         //pick a random planet
                         int pa = rnd.nextInt(objects.size());
                         //add owner stations near planets
                         Statling tmp = stations.get(b);
                         if (tmp.getOwner().matches(sys.getOwner())) {
                             //drop it near it
                             Simpling host = objects.get(pa);
                             //get root coordinates
                             x = host.getLoc().getX();
                             y = host.getLoc().getY();
                             //mutate
                             x += rnd.nextInt(10000) - 5000;
                             y += rnd.nextInt(10000) - 5000;
                             //drop
                             thisSystem += "[Station]\n"
                                     + "name=" + tmp.getName() + "\n"
                                     + "system=" + systemName + "\n"
                                     + "ship=" + tmp.getType() + "\n"
                                     + "x=" + x + "\n"
                                     + "y=" + y + "\n"
                                     + "faction=" + tmp.getOwner() + "\n"
                                     + "[/Station]\n\n";
                         } else {
                             //it is probably a pirate base drop it somewhere
                             x = rnd.nextInt(2 * size) - size;
                             y = rnd.nextInt(2 * size) - size;
                             //drop
                             thisSystem += "[Station]\n"
                                     + "name=" + tmp.getName() + "\n"
                                     + "system=" + systemName + "\n"
                                     + "ship=" + tmp.getType() + "\n"
                                     + "x=" + x + "\n"
                                     + "y=" + y + "\n"
                                     + "faction=" + tmp.getOwner() + "\n"
                                     + "[/Station]\n\n";
                         }
                     }
                 }
                 ret += thisSystem;
             }
         }
         return ret;
     }
 
     private void syslingTest() {
         ArrayList<Sysling> test = new ArrayList<>();
         Random rnd = new Random(5);
         //make some syslings
         for (int a = 0; a < 50; a++) {
             String name = "Sysling " + a;
             double sx = rnd.nextInt(1000) - 500;
             double sy = rnd.nextInt(1000) - 500;
             test.add(new Sysling(name, new Point2D.Double(sx, sy)));
         }
         //make them find their buddy
         for (int a = 0; a < test.size(); a++) {
             test.get(a).sortBuddy(test);
         }
         //get a print out
         Sysling t = test.get(0);
         System.out.println("Mapping from " + t.getName());
         for (int a = 0; a < test.size(); a++) {
             Sysling _0th = t.findBuddy(test, a);
             System.out.println(_0th.getName() + " " + _0th.distance(t));
         }
     }
 
     private ArrayList<Sysling> makeSyslings(int numSystems, Random rnd, int worldSize) {
         //generate syslings
         ArrayList<Sysling> syslings = new ArrayList<>();
         for (int a = 0; a < numSystems; a++) {
             //determine map location
             double x = rnd.nextInt(worldSize * 2) - worldSize;
             double y = rnd.nextInt(worldSize * 2) - worldSize;
             //pick name
             String name = null;
             while (name == null) {
                 name = randomSystemName(generic);
             }
             //make sysling
             Sysling test = new Sysling(name, new Point2D.Double(x, y));
             //check for collission
             boolean safe = true;
             for (int v = 0; v < syslings.size(); v++) {
                 if (syslings.get(v).collideWith(test)) {
                     safe = false;
                     break;
                 }
             }
             //add if safe
             if (safe) {
                 syslings.add(test);
             }
         }
         return syslings;
     }
 
     private String randomPlanetName() {
         /*
          * Generates a random name for a planet
          */
         ArrayList<Term> fg = Universe.getCache().getNameCache().getTermsOfType("First");
         ArrayList<Term> lg = Universe.getCache().getNameCache().getTermsOfType("Last");
         String first = "";
         String last = "";
         {
             for (int a = 0; a < fg.size(); a++) {
                 if (fg.get(a).getValue("name").matches("Generic")) {
                     Parser.Param pick = fg.get(a).getParams().get(rnd.nextInt(fg.get(a).getParams().size() - 1) + 1);
                     first = pick.getValue();
                     break;
                 }
             }
 
             for (int a = 0; a < lg.size(); a++) {
                 if (lg.get(a).getValue("name").matches("Generic")) {
                     Parser.Param pick = lg.get(a).getParams().get(rnd.nextInt(lg.get(a).getParams().size() - 1) + 1);
                     last = pick.getValue();
                     break;
                 }
             }
         }
         int num = rnd.nextInt(24) + 1;
         if (rnd.nextFloat() > 0.5) {
             return "'" + first + " " + num + "'";
         } else {
             return "'" + last + " " + num + "'";
         }
     }
 
     public String randomSystemName(char[] sample) {
         String ret = "";
         {
             //prefix
             String prefix = greek[rnd.nextInt(greek.length)];
             ret += prefix + " ";
             int l1 = rnd.nextInt(4) + 1;
             //pass 1
             for (int a = 0; a < l1; a++) {
                 char pick = sample[rnd.nextInt(sample.length)];
                 ret += pick;
             }
             //add tack
             ret += "-";
             int l2 = rnd.nextInt(4) + 1;
             //pass 2
             for (int a = 0; a < l2; a++) {
                 char pick = sample[rnd.nextInt(sample.length)];
                 ret += pick;
             }
             //safety check
             for (int v = 0; v < usedSystemNames.size(); v++) {
                 if (usedSystemNames.get(v).matches(ret)) {
                     return null;
                 }
             }
         }
         //add
         usedSystemNames.add(ret);
         //return
         return ret;
     }
 
     private void dropStations(ArrayList<Sysling> syslings, Faction faction) {
         ArrayList<Sysling> simp = new ArrayList<>();
         if (faction.isEmpire()) {
             //make a list of this faction's systems
             for (int a = 0; a < syslings.size(); a++) {
                 if (syslings.get(a).getOwner().matches(faction.getName())) {
                     simp.add(syslings.get(a));
                 }
             }
         } else {
             //figure out who hosts this faction
             ArrayList<String> hosts = faction.hosts;
             for (int a = 0; a < hosts.size(); a++) {
                 //make a list of the host's systems
                 for (int v = 0; v < syslings.size(); v++) {
                     if (syslings.get(v).getOwner().matches(hosts.get(a))) {
                         simp.add(syslings.get(v));
                     }
                 }
             }
         }
         //get a list of stations for this faction
         Parser sParse = new Parser("FACTIONS.txt");
         ArrayList<Term> terms = sParse.getTermsOfType("Stations");
         Term stat = null;
         for (int a = 0; a < terms.size(); a++) {
             if (terms.get(a).getValue("name").matches(faction.getName())) {
                 stat = terms.get(a);
             }
         }
         if (stat != null) {
             //get types of stations
             int a = 0;
             String type = "";
             while ((type = stat.getValue("station" + a)) != null) {
                 //get station info
                 String ty = type.split(",")[0];
                 double spread = Float.parseFloat(type.split(",")[1]);
                 //calculate the number of stations (guaranteeing at least 1!)
                 int count = (int) (spread * simp.size()) + 1;
                 //place them
                 for (int v = 0; v < count; v++) {
                     String name = ty + " " + (v + 1);
                     Statling tr = new Statling(name, ty, faction.getName());
                     //put it in a random system owned by this faction
                     int pick = rnd.nextInt(simp.size());
                     simp.get(pick).getStations().add(tr);
                 }
                 //iterate
                 a++;
             }
         } else {
             System.out.println(faction.getName() + " doesn't have any stations!");
         }
     }
 
     private void dropSov(ArrayList<Sysling> syslings) {
         /*
          * Seeds factions
          */
         //make a list of all factions
         ArrayList<SuperFaction> factions = new ArrayList<>();
         Parser fParse = new Parser("FACTIONS.txt");
         ArrayList<Term> terms = fParse.getTermsOfType("Faction");
         for (int a = 0; a < terms.size(); a++) {
             factions.add(new SuperFaction(null, terms.get(a).getValue("name")));
         }
         //for each sov holding faction pick a capital
         for (int a = 0; a < factions.size(); a++) {
             if (factions.get(a).isEmpire()) {
                 //pick a random system as the capital
                 Sysling pick = null;
                 while (pick == null) {
                     Sysling tmp = syslings.get(rnd.nextInt(syslings.size()));
                     if (tmp.getOwner().matches("Neutral")) {
                         pick = tmp;
                     }
                 }
                 //mark it
                 pick.setOwner(factions.get(a).getName());
                 //pick music
                 ArrayList<String> ambientMusic = factions.get(a).getAmbientMusic();
                 if (ambientMusic.size() > 0) {
                     pick.setAmbientMusic(ambientMusic.get(rnd.nextInt(ambientMusic.size())));
                 }
                 ArrayList<String> dangerMusic = factions.get(a).getDangerMusic();
                 if (dangerMusic.size() > 0) {
                     pick.setDangerMusic(dangerMusic.get(rnd.nextInt(dangerMusic.size())));
                 }
                 //determine system count
                 int numSystems = (int) (factions.get(a).getSpread() * syslings.size());
                 int offset = 0;
                 //pick unclaimed systems by proximity
                 for (int x = 0; x < numSystems + offset; x++) {
                     if ((x + offset) < syslings.size()) {
                         Sysling tmp = pick.findBuddy(syslings, x + offset);
                         if (tmp.getOwner().matches("Neutral")) {
                             tmp.setOwner(factions.get(a).getName());
                             //pick music
                             if (ambientMusic.size() > 0) {
                                 pick.setAmbientMusic(ambientMusic.get(rnd.nextInt(ambientMusic.size())));
                             }
                             if (dangerMusic.size() > 0) {
                                 pick.setDangerMusic(dangerMusic.get(rnd.nextInt(dangerMusic.size())));
                             }
                         } else {
                             offset += 1;
                         }
                     } else {
                         System.out.println(factions.get(a).getName() + " ran out of space to claim sov!");
                         //no room left
                     }
                 }
             } else {
                 //these will be handled in the next pass
             }
         }
         //build stations
         for (int a = 0; a < factions.size(); a++) {
             dropStations(syslings, factions.get(a));
         }
         //report
         for (int a = 0; a < syslings.size(); a++) {
             if (!syslings.get(a).getOwner().matches("Neutral")) {
                 System.out.println("Sysling " + a + " claimed by " + syslings.get(a).getOwner());
             }
         }
     }
 
     public class Statling {
         /*
          * Simple structure for storing a station template
          */
 
         private String name;
         private String type;
         private String owner;
 
         public Statling(String name, String type, String owner) {
             this.name = name;
             this.type = type;
             this.owner = owner;
         }
 
         public String getName() {
             return name;
         }
 
         public void setName(String name) {
             this.name = name;
         }
 
         public String getType() {
             return type;
         }
 
         public void setType(String type) {
             this.type = type;
         }
 
         public String getOwner() {
             return owner;
         }
 
         public void setOwner(String owner) {
             this.owner = owner;
         }
     }
 
     public class Sysling {
         /*
          * Solar system template used for jump hole mapping.
          */
 
         private Point2D.Double loc;
         private Sysling[] neighbors;
         private ArrayList<Sysling> connections = new ArrayList<>();
         private ArrayList<Statling> stations = new ArrayList<>();
         private String name;
         private String owner = "Neutral";
         private String ambientMusic = "audio/music/Undefined.wav";
         private String dangerMusic = "audio/music/Committing.wav";
         private SuperFaction neutralFaction = new SuperFaction(null, "Neutral");
 
         public Sysling(String name, Point2D.Double loc) {
             this.loc = loc;
             this.name = name;
             //pick neutral ambient music
             ArrayList<String> ambientMusic = neutralFaction.getAmbientMusic();
             if (ambientMusic.size() > 0) {
                 setAmbientMusic(ambientMusic.get(rnd.nextInt(ambientMusic.size())));
             }
         }
 
         public void addConnection(Sysling sys) {
             connections.add(sys);
         }
 
         public boolean connectedTo(Sysling test) {
             return connections.contains(test);
         }
 
         public void sortBuddy(ArrayList<Sysling> verse) {
             /*
              * Sorts the syslings by distance ascending.
              */
             Sysling[] arr = (Sysling[]) verse.toArray(new Sysling[0]);
             for (int a = 0; a < arr.length; a++) {
                 for (int b = 1; b < arr.length - a; b++) {
                     if (distance(arr[b - 1]) > distance(arr[b])) {
                         Sysling tmp = arr[b];
                         arr[b] = arr[b - 1];
                         arr[b - 1] = tmp;
                     }
                 }
             }
             neighbors = arr;
         }
 
         public Sysling findBuddy(ArrayList<Sysling> verse, int place) {
             /*
              * Finds Nth closest sysling to this one. Nth = place.
              */
             if (neighbors == null) {
                 sortBuddy(verse);
             }
             return neighbors[place];
         }
 
         public boolean collideWith(Sysling test) {
             if (test.getName().matches(name)) {
                 return true;
             } else {
                 return false;
             }
         }
 
         public double distance(Sysling comp) {
             return loc.distance(comp.getLoc());
         }
 
         public Point2D.Double getLoc() {
             return loc;
         }
 
         public String getName() {
             return name;
         }
 
         public String toString() {
             return name;
         }
 
         public String getOwner() {
             return owner;
         }
 
         public void setOwner(String owner) {
             this.owner = owner;
         }
 
         public ArrayList<Statling> getStations() {
             return stations;
         }
 
         public String getAmbientMusic() {
             return ambientMusic;
         }
 
         public void setAmbientMusic(String ambientMusic) {
             this.ambientMusic = ambientMusic;
         }
 
         public String getDangerMusic() {
             return dangerMusic;
         }
 
         public void setDangerMusic(String dangerMusic) {
             this.dangerMusic = dangerMusic;
         }
     }
 
     public class Simpling {
         /*
          * Class used for storing the location and radius of an object for
          * the sole purpose of avoiding collisions.
          */
 
         private Point2D.Float loc;
         private float rad;
 
         public Simpling(Point2D.Float loc, float rad) {
             this.loc = loc;
             this.rad = rad;
         }
 
         public boolean collideWith(Simpling test) {
             Point2D.Float testLoc = (Point2D.Float) test.getLoc().clone();
             float testRad = test.getRad();
             //add the radii to get the min separation
             float minRad = testRad + rad;
             //multiply this rad by 1.5 so they can't be kissing each other
             minRad *= 1.5f;
             //determine if they are colliding using distance
             float dist = (float) loc.distance(testLoc);
             if (dist < minRad) {
                 return true;
             }
             return false;
         }
 
         public Point2D getLoc() {
             return loc;
         }
 
         public float getRad() {
             return rad;
         }
     }
 }
