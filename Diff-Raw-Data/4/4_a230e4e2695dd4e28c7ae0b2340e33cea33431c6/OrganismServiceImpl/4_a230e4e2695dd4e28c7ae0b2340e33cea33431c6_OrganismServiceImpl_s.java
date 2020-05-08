 package org.concord.geniverse.server;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.imageio.ImageIO;
 import javax.servlet.ServletContext;
 
 import org.concord.biologica.engine.Characteristic;
 import org.concord.biologica.engine.Organism;
 import org.concord.biologica.engine.Species;
 import org.concord.biologica.engine.SpeciesImage;
 import org.concord.biologica.engine.World;
 import org.concord.biologica.ui.StaticOrganismView;
 import org.concord.geniverse.client.GOrganism;
 import org.concord.geniverse.client.OrganismService;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 public class OrganismServiceImpl extends RemoteServiceServlet implements OrganismService {
 	private static final Logger logger = Logger.getLogger(OrganismServiceImpl.class.getName());
 	private static final long serialVersionUID = 1L;
 	private World world = new World("org/concord/biologica/worlds/dragon.xml");
 	private Species species = world.getCurrentSpecies();
 	private static int currentDragonNumber = 0;
 
 	private void cleanupWorld(Organism org) {
 		world.deleteOrganism(org);
 	}
 
 	private GOrganism createGOrg(Organism org) {
 		GOrganism gOrg = new GOrganism();
 		gOrg.setName(org.getName());
 		gOrg.setSex(org.getSex());
 		gOrg.setAlleles(org.getAlleleString());
 		gOrg.setImageURL(getOrganismImageURL(org, SpeciesImage.XLARGE_IMAGE_SIZE));
 		gOrg.setCharacteristics(getOrganismPhenotypes(org));
 		return gOrg;
 	}
 
 	private Organism createOrg(GOrganism gOrg) {
 		Organism org = new Organism(world, gOrg.getName(), species, gOrg.getSex(), gOrg.getAlleles());
 		return org;
 	}
 
 	public GOrganism getOrganism(int sex) {
 		System.out.println("getOrganism(int sex) called:");
 		Organism dragon = new Organism(world, sex, "Organism " + (++currentDragonNumber), world.getCurrentSpecies());
 		GOrganism gOrg = createGOrg(dragon);
 		cleanupWorld(dragon);
 		return gOrg;
 	}
 
 	public GOrganism getOrganism(int sex, String alleles) {
 		Organism dragon = new Organism(world, "Organism " + (++currentDragonNumber), world.getCurrentSpecies(), sex, alleles) ;
 		GOrganism gOrg = createGOrg(dragon);
 		cleanupWorld(dragon);
 		return gOrg;
 	}
 
 	public GOrganism getOrganism() {
 		System.out.println("getOrganism(int sex) called:");
 		Organism dragon = new Organism(world, Organism.RANDOM_SEX, "Organism " + (++currentDragonNumber), world.getCurrentSpecies());
 		GOrganism gOrg = createGOrg(dragon);
 		cleanupWorld(dragon);
 		return gOrg;
 	}
 
 	public ArrayList<String> getOrganismPhenotypes(GOrganism gOrg) {
 		Organism org = createOrg(gOrg);
 		ArrayList<String> phenotypes = getOrganismPhenotypes(org);
 		cleanupWorld(org);
 		return phenotypes;
 	}
 
 	private ArrayList<String> getOrganismPhenotypes(Organism org) {
 		ArrayList<String> phenotypes = new ArrayList<String>();
 
 		Enumeration<Characteristic> chars = org.getCharacteristics();
 		while (chars.hasMoreElements()) {
 			Characteristic c = chars.nextElement();
 			phenotypes.add(c.getName());
 		}
 
 		return phenotypes;
 	}
 
 	public String getOrganismImageURL() {
 		// FIXME cleanup
 		try {
 			return getOrganismImageURL(getOrganism(), SpeciesImage.XLARGE_IMAGE_SIZE);
 		} catch(Exception e) {
 			return e.toString();
 		}
 	}
 
 	public String getOrganismImageURL(GOrganism organism, int imageSize) {
 		Organism dragon = createOrg(organism);
 		String imageUrl = getOrganismImageURL(dragon, imageSize);
 		cleanupWorld(dragon);
 		return imageUrl;
 	}
 
 	private String getOrganismImageURL(Organism dragon, int imageSize) {
 		String filename = generateFilename(dragon, imageSize);
 		ServletContext context = getServletContext();
		String realpath = context.getRealPath("/cache/" + imageSize + "/" + filename);
 
 		if (realpath == null) {
 			String url = context.getContextPath() + "/cache/unknown.png";
 			return url;
 		}
 
 		File outputfile = new File(realpath);
 		if (! outputfile.exists()) {
 			try {
 				StaticOrganismView view = new StaticOrganismView();
 				view.setOrganism(dragon);
 				try {
 					BufferedImage image = view.getOrganismImage(dragon, imageSize);
 					ImageIO.write(image, "png", outputfile);
 				} catch (java.lang.ExceptionInInitializerError e) {
 					logger.log(Level.SEVERE, "Couldn't generate a buffered image!", e);
 				}
 
 
 			} catch (IOException e) {
 				logger.log(Level.SEVERE, "Couldn't write the image for the organism! " + realpath, e);
 			}
 		}
 
 		return context.getContextPath() + "/cache/" + filename;
 	}
 
 	private String generateFilename(Organism org, int imageSize) {
 		return "" + imageSize + "/" + org.getAlleleString(true) + ".png";
 	}
 
 	public GOrganism breedOrganism(GOrganism gorg1, GOrganism gorg2) {
 		System.out.println("breedOrganism(GOrganism gorg1, GOrganism gorg2) called:");
 		Organism org1 = createOrg(gorg1);
 		Organism org2 = createOrg(gorg2);
 		try {
 			Organism child = new Organism(org1, org2, "child");
 			GOrganism gOrg = createGOrg(child);
 			cleanupWorld(child);
 			cleanupWorld(org1);
 			cleanupWorld(org2);
 			return gOrg;
 		} catch (IllegalArgumentException e){
 			logger.log(Level.SEVERE, "Could not breed these organisms!", e);
 
 			cleanupWorld(org1);
 			cleanupWorld(org2);
 			return null;
 		}
 	}
 
     public GOrganism[] breedOrganisms(int number, GOrganism gorg1, GOrganism gorg2) {
         GOrganism[] orgs = new GOrganism[number];
         Organism org1 = createOrg(gorg1);
         Organism org2 = createOrg(gorg2);
         
         for (int i = 0; i > number; i++) {
             Organism child = new Organism(org1, org2, "child " + i);
             logger.warning("Bred " + child.getSexAsString() + " child");
             GOrganism gChild = createGOrg(child);
             
             orgs[i] = gChild;
             cleanupWorld(child);
         }
         
         cleanupWorld(org1);
         cleanupWorld(org2);
         
         return orgs;
     }
 }
