 package de.unihamburg.zbh.fishoracle.server.data;
 
 import java.sql.*;
 import java.text.ParseException;
 import java.util.List;
 
 import org.ensembl.datamodel.CoordinateSystem;
 import org.ensembl.datamodel.Gene;
 import org.ensembl.datamodel.KaryotypeBand;
 import org.ensembl.datamodel.Location;
 
 import org.ensembl.driver.AdaptorException;
 import org.ensembl.driver.CoreDriver;
 import org.ensembl.driver.CoreDriverFactory;
 import org.ensembl.driver.KaryotypeBandAdaptor;
 
 import de.unihamburg.zbh.fishoracle.client.data.Amplicon;
 import de.unihamburg.zbh.fishoracle.client.data.Gen;
 
 public class DBQuery {
 
 	public DBQuery() {
 	}
 	
 	
 	/**
 	 * 
 	 * looks location information (chromosome, start, end) for an amplicon stable id up.
 	 * 
 	 * @param ampliconStableId The stable id of an amplicon.
 	 * @return		a ensembl API location object storing chromosome, start and end of an amplicon. 
 	 * 
 	 * */
 	public Location getLocationForAmpliconStableId(double ampliconStableId){
 	
 		Connection conn = null;
 		Location loc = null;
 		try{
 			
 			int ampStart = 0;
 			int ampEnd = 0;
 			int ampChr = 0;
 			
 			conn = FishOracleConnection.connect();
 			System.out.println("Connection established");
 			
 			Statement s = conn.createStatement();
 				
 				s.executeQuery("SELECT * from amplicon WHERE amplicon_stable_id = " + ampliconStableId);
 				ResultSet ampRs = s.getResultSet();
 				
 				while(ampRs.next()){
 				ampStart = ampRs.getInt(4);
 				ampEnd = ampRs.getInt(5);
 				ampChr = ampRs.getInt(3);
 				
 				System.out.println(ampChr + ":" + ampStart + "-" + ampEnd);
 				
 				String locStr = "chromosome:" + ampChr + ":" + ampStart + "-" + ampEnd;
 				
 				loc = new Location(locStr);
 				}
 				ampRs.close();
 				
 		} catch (Exception e){
 			FishOracleConnection.printErrorMessage(e);
 			System.exit(1);
 		} finally {
 			if(conn != null){
 				try{
 					conn.close();
 					System.out.println("Connection closed ...");
 				} catch(Exception e) {
 					String err = FishOracleConnection.getErrorMessage(e);
 					System.out.println(err);
 				}
 			}
 		}
 			
 		return loc;
 	}
 	
 	/**
 	 * 
 	 * looks location information (chromosome, start, end) for a gene symbol up.
 	 * 
 	 * @param symbol The gene symbol.
 	 * @return		a ensembl API location object storing chromosome, start and end of a gene. 
 	 * 
 	 * */
 	public Location getLocationForGene(String symbol){
 		Gene gene = null;
 		CoreDriver coreDriver;
 		try {
 			coreDriver = CoreDriverFactory.createCoreDriver("localhost",
 			3306, "homo_sapiens_core_54_36p", "fouser", "fish4me");
 			coreDriver.getConnection();
 		
 			gene = (Gene) coreDriver.getGeneAdaptor().fetchBySynonym(symbol).get(0);
 			
 			coreDriver.closeAllConnections();
 		
 		} catch (AdaptorException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}	
 		
 		return gene.getLocation();
 	}
 	
 	/**
 	 * 
 	 * looks location information (chromosome, start, end) for an karyoband up.
 	 * 
 	 * @param chr The chromosome number
 	 * @param band The karyoband
 	 * @return		a ensembl API location object storing chromosome, start and end of a chromosome and chromosome band. 
 	 * 
 	 * */
 	public Location getLocationForKaryoband(String chr, String band){
 		CoordinateSystem coordSys = null;
 		KaryotypeBand k = null;
 		CoreDriver coreDriver;
 		try {
 			coreDriver = CoreDriverFactory.createCoreDriver("localhost",
 			3306, "homo_sapiens_core_54_36p", "fouser", "fish4me");
 			coreDriver.getConnection();
 			
 			KaryotypeBandAdaptor kband = coreDriver.getKaryotypeBandAdaptor();
 			
 			coordSys = coreDriver.getCoordinateSystemAdaptor().fetch("chromosome", null);
 			
 			k = (KaryotypeBand) kband.fetch(coordSys, chr, band).get(0);
 			
 			coreDriver.closeAllConnections();
 		
 		} catch (AdaptorException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}	
 		
 		return k.getLocation();
 	}
 	
 	
 	/**
 	 * 
 	 * finds all amplicons that overlap with a given range on a chromosome and returns the 
 	 * maximum range over all overlapping amplicons as an ensembl location object.
 	 * 
 	 * @param chr Chromosome
 	 * @param start starting position
 	 * @param end ending postion
 	 * @return 		a ensembl API location object storing chromosome, start and end
 	 * 
 	 * */
 	public Location getMaxAmpliconRange(int chr, int start, int end){
 		Location loc = null;
 		Connection conn = null;
 		int ampChr = chr;
 		int ampStart = start;
 		int ampEnd = end;
 		try{
 			
 			conn = FishOracleConnection.connect();
 			System.out.println("Connection established");
 			
 			Statement s = conn.createStatement();
 			
 			s.executeQuery("SELECT MIN(start) as minstart, MAX(end) as maxend FROM amplicon WHERE chromosome = " + ampChr + " AND ((start <= " + ampStart + " AND end >= " + ampEnd + ") OR" +
 				       " (start >= " + ampStart + " AND end <= " + ampEnd + ") OR" +
 				       " (start >= " + ampStart + " AND start <= " + ampEnd + ") OR" +
 				       " (end >= " + ampStart + " AND end <= " + ampEnd + "))");
 			
 			ResultSet rangeRs = s.getResultSet();
 			rangeRs.next();
 			int qstart = rangeRs.getInt(1);
 			int qend = rangeRs.getInt(2);
 			
 			String locStr = "chromosome:" + Integer.toString(ampChr) + ":" + qstart + "-" + qend;
 			
 			loc = new Location(locStr);
 			
 			rangeRs.close();
 			s.close();
 			
 		} catch (Exception e){
 			FishOracleConnection.printErrorMessage(e);
 			System.exit(1);
 		} finally {
 			if(conn != null){
 				try{
 					conn.close();
 					System.out.println("Connection closed ...");
 				} catch(Exception e) {
 					String err = FishOracleConnection.getErrorMessage(e);
 					System.out.println(err);
 				}
 			}
 		}
 		
 		return loc;
 	}
 	
 	/**
 	 * 
 	 * For a range on a chromosome an array with all opverlapping amplicons is returned.
 	 * 
 	 * @param chr Chromosome
 	 * @param start starting position
 	 * @param end ending postion
 	 * @return		Array containing amplicon objects
 	 * 
 	 * */
 	public Amplicon[] getAmpliconData(String chr, int start, int end){
 		
 		Connection conn = null;
 		Amplicon[] amps = null;
 		try{
 			
 			conn = FishOracleConnection.connect();
 			System.out.println("Connection established");
 			
 			Statement s = conn.createStatement();
 				
 			int ampStart = start;
 			int ampEnd = end;
 			String ampChr = chr;
 			
 			s.executeQuery("SELECT count(*) from amplicon WHERE chromosome = \"" + ampChr + "\" AND ((start <= " + ampStart + " AND end >= " + ampEnd + ") OR" +
 				       " (start >= " + ampStart + " AND end <= " + ampEnd + ") OR" +
 				       " (start >= " + ampStart + " AND start <= " + ampEnd + ") OR" +
 				       " (end >= " + ampStart + " AND end <= " + ampEnd + "))");
 			
 			ResultSet countRegRs = s.getResultSet();
 			countRegRs.next();
 			int ampCount = countRegRs.getInt(1);
 			
 			System.out.println("Anzah: "  + ampCount);
 			
 			countRegRs.close();
 			
 			s.executeQuery("SELECT * from amplicon WHERE chromosome = \"" + ampChr + "\" AND ((start <= " + ampStart + " AND end >= " + ampEnd + ") OR" +
 					       " (start >= " + ampStart + " AND end <= " + ampEnd + ") OR" +
 					       " (start >= " + ampStart + " AND start <= " + ampEnd + ") OR" +
 					       " (end >= " + ampStart + " AND end <= " + ampEnd + "))");
 			
 			ResultSet regRs = s.getResultSet();
 			
 			int count = 0;
 
 			amps = new Amplicon[ampCount];
 			
 			while(regRs.next()){
 				int ampliconId = regRs.getInt(1);
 				double newAmpliconStableId = regRs.getDouble(2);
 				String newChr = regRs.getString(3);
 				int newStart = regRs.getInt(4);
 				int newEnd = regRs.getInt(5);
 				String caseName = regRs.getString(6);
 				String tumorType = regRs.getString(7);
 				int contin = regRs.getInt(8);
 				int amplevel = regRs.getInt(9);
 				
 				amps[count] = new Amplicon(newAmpliconStableId, newChr, newStart, newEnd);
 				
 				System.out.println("AmpId: " + ampliconId + ", stableId: " + newAmpliconStableId + ", Chromosome: " + newChr
 						            + ", start: " + newStart + ", end: " + newEnd + ", case: " + caseName
 						            + ", tumor_type: " + tumorType + ", continuous: " + contin + ", amplevel: " + amplevel + "\n");
 				count++;
 			}
 			
 			regRs.close();
 			s.close();
 			System.out.println(count + " rows were obtained");
 			
 		} catch (Exception e){
 			FishOracleConnection.printErrorMessage(e);
 			System.exit(1);
 		} finally {
 			if(conn != null){
 				try{
 					conn.close();
 					System.out.println("Connection closed ...");
 				} catch(Exception e) {
 					String err = FishOracleConnection.getErrorMessage(e);
 					System.out.println(err);
 				}
 			}
 		}
 		return amps;
 	}
 	
 	public Amplicon getAmpliconInfos(String query){
 		
 		Connection conn = null;
 		Amplicon amp = null;
 		try{
 		
 			conn = FishOracleConnection.connect();
 			System.out.println("Connection established");
 			
 			Statement s = conn.createStatement();
 			
 			s.executeQuery("select * FROM amplicon WHERE amplicon_stable_id = " + query);
 			
 			ResultSet ampRs = s.getResultSet();
 			
 			while(ampRs.next()){
 				
 				double ampliconStableId = ampRs.getDouble(2);
 				String chr = ampRs.getString(3);
 				int start = ampRs.getInt(4);
 				int end = ampRs.getInt(5);
 				String caseName = ampRs.getString(6);
 				String tumorType = ampRs.getString(7);
 				int contin = ampRs.getInt(8);
 				int amplevel = ampRs.getInt(9);
 				
 				amp = new Amplicon(ampliconStableId, chr, start, end, caseName, tumorType, contin, amplevel);
 				
 			}
 			
 		} catch (Exception e){
 			FishOracleConnection.printErrorMessage(e);
 			System.exit(1);
 		} finally {
 			if(conn != null){
 				try{
 					conn.close();
 					System.out.println("Connection closed ...");
 				} catch(Exception e) {
 					String err = FishOracleConnection.getErrorMessage(e);
 					System.out.println(err);
 				}
 			}
 		}
 		
 		return amp;
 	}
 	
 	public Gen getGeneInfos(String query) {
 		
 		Gen gene = null;
 		
 		CoreDriver coreDriver;
 		try {
 			coreDriver = CoreDriverFactory.createCoreDriver("localhost",
 			3306, "homo_sapiens_core_54_36p", "fouser", "fish4me");
 		
 			coreDriver.getConnection();
 			
 			List<Gene> ensGene =  coreDriver.getGeneAdaptor().fetchBySynonym(query);
 
 			gene = new Gen();
 			for (int j = 0; j < ensGene.size(); j++) {
 				
 				gene.setGenName(ensGene.get(j).getDisplayName());
 				gene.setChr(ensGene.get(j).getLocation().getSeqRegionName());
 				gene.setStart(ensGene.get(j).getLocation().getStart());
 				gene.setEnd(ensGene.get(j).getLocation().getEnd());
				gene.setStrand(Integer.toBinaryString(ensGene.get(j).getLocation().getStrand()));
 				gene.setAccessionID(ensGene.get(j).getAccessionID());
 				gene.setBioType(ensGene.get(j).getBioType());
 				gene.setDescription(ensGene.get(j).getDescription());
 				gene.setLength(ensGene.get(j).getLocation().getLength());
 				
 			}
 		} catch (AdaptorException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		
 		return gene;
 	}
 	
 	/**
 	 * For a range on a chromosome an array with all opverlapping genes is returned.
 	 * 
 	 * @param chr Chromosome
 	 * @param start starting position
 	 * @param end ending postion
 	 * @return 		Array containing gen objects
 	 * 
 	 * */
 	public Gen[] getEnsembleGenes(String chr, int start, int end){
 		
 		Gen[] genes = null;
 		
 		String loc = "chromosome:" + chr + ":" + Integer.toString(start) + "-" + Integer.toString(end);
 		
 		System.out.println(loc);
 		
 		try {
 			//Registry registry = Registry.createDefaultRegistry();
 			//CoreDriver coreDriver = registry.getGroup("human").getCoreDriver();
 			
 			CoreDriver coreDriver =
 				CoreDriverFactory.createCoreDriver("localhost",
 				3306, "homo_sapiens_core_54_36p", "fouser", "fish4me");
 
 			coreDriver.getConnection();
 			
 			List<?> ensGenes;
 			try {
 				ensGenes = coreDriver.getGeneAdaptor().fetch(new Location(loc));
 				
 				genes = new Gen[ensGenes.size()];
 				for (int j = 0; j < ensGenes.size(); j++) {
 					Gene g = (Gene) ensGenes.get(j);
 					
 					genes[j] = new Gen(g.getDisplayName(), g.getLocation().getSeqRegionName(), g.getLocation().getStart(), g.getLocation().getEnd(), Integer.toString(g.getLocation().getStrand()));
 					
 					
 					System.out.println(g.getAccessionID() + " " + g.getDisplayName() + " " + g.getLocation().getStart() + " " + g.getLocation().getEnd() + " " +
 							g.getLocation().getSeqRegionName() + " " + g.getLocation().getStrand() + " " +
 							" " + g.getBioType() + " " + g.getAnalysis() );
 				}
 			} catch (ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			coreDriver.closeAllConnections();
 
 		} catch (AdaptorException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return genes;
 	}
 	
 	/**
 	 * For a range on a chromosome an array with all opverlapping karyobands is returned.
 	 * 
 	 * @param chr Chromosome
 	 * @param start starting position
 	 * @param end ending postion
 	 * @return 		Array containing karyoband objects
 	 * 
 	 * */
 	public Karyoband[] getEnsemblKaryotypes(String chr, int start, int end){
 
 	        Karyoband[] karyoband = null;
 		
 			CoreDriver coreDriver;
 			try {
 				coreDriver = CoreDriverFactory.createCoreDriver("localhost",
 				3306, "homo_sapiens_core_54_36p", "fouser", "fish4me");
 
 			coreDriver.getConnection();
 			
 			String loc = "chromosome:" + chr + ":" + Long.toString(start) + "-" + Long.toString(end);
 			
 			KaryotypeBandAdaptor ktba = coreDriver.getKaryotypeBandAdaptor();
 			
 			List<?> ensChrs; 
 				
 			ensChrs = ktba.fetch(loc);
 			
 			karyoband = new Karyoband[ensChrs.size()];
 			for (int i = 0; i < ensChrs.size(); i++) {
 				
 				KaryotypeBand k = (KaryotypeBand) ensChrs.get(i);
 				
 				System.out.println(k.getLocation().getSeqRegionName() + " " + k.getBand() + " " + k.getLocation().getStart() + " " + k.getLocation().getEnd());
 				
 				karyoband[i] = new Karyoband(k.getLocation().getSeqRegionName(), k.getBand(), k.getLocation().getStart(), k.getLocation().getEnd());
 			}
 				
 			coreDriver.closeAllConnections();
 			
 			} catch (AdaptorException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}	
 			
 			return karyoband;
 	}
 
 }
