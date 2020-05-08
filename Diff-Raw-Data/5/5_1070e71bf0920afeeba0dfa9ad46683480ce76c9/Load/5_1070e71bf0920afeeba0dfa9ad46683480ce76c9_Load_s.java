 package star.genetics.xls;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.zip.DataFormatException;
 
 import org.apache.poi.hssf.usermodel.HSSFCell;
 import org.apache.poi.hssf.usermodel.HSSFRow;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.poifs.filesystem.POIFSFileSystem;
 
 import star.annotations.Handles;
 import star.annotations.Properties;
 import star.annotations.Property;
 import star.annotations.SignalComponent;
 import star.genetics.Messages;
 import star.genetics.events.ErrorDialogRaiser;
 import star.genetics.events.LoadModelRaiser;
 import star.genetics.events.OpenModelRaiser;
 import star.genetics.genetic.impl.CreatureImpl;
 import star.genetics.genetic.impl.CreatureSetImpl;
 import star.genetics.genetic.impl.DiploidAllelesImpl;
 import star.genetics.genetic.impl.GeneticMakeupImpl;
 import star.genetics.genetic.impl.Utilities;
 import star.genetics.genetic.model.Allele;
 import star.genetics.genetic.model.Chromosome;
 import star.genetics.genetic.model.Creature;
 import star.genetics.genetic.model.Creature.Sex;
 import star.genetics.genetic.model.CreatureSet;
 import star.genetics.genetic.model.DiploidAlleles;
 import star.genetics.genetic.model.Gene;
 import star.genetics.genetic.model.GeneticMakeup;
 import star.genetics.genetic.model.Genome;
 import star.genetics.genetic.model.Model;
 import star.genetics.genetic.model.ModelWriter;
 import star.genetics.genetic.model.RuleSet;
 import utils.FileUtils;
 import utils.UIHelpers;
 
 @SignalComponent(raises = { ErrorDialogRaiser.class })
 @Properties(@Property(name = "errorMessage", type = Exception.class, getter = Property.PUBLIC, setter = Property.PUBLIC))
 public class Load extends Load_generated implements OpenModel
 {
 	public static transient PrivateIO priv;
 	private static final String ALLELES = "Alleles"; //$NON-NLS-1$
 	private static final String GENE_LOCATION = "Gene Location"; //$NON-NLS-1$
 	private static final String GENE = "Gene Name"; //$NON-NLS-1$
 	private static final String MALERECOMBINATIONRATE = "Male recombination"; //$NON-NLS-1$
 	private static final String FEMALERECOMBINATIONRATE = "Female recombination"; //$NON-NLS-1$
 	public static final String CHROMOSOME = "Chromosome"; //$NON-NLS-1$
 	private static final String VISUALIZER = "Visualizer"; //$NON-NLS-1$
 	private static final String SEX_TYPE = "sex type:"; //$NON-NLS-1$
 	private static final String NAME = "name:"; //$NON-NLS-1$
 	public static final String PHENOTYPE = "Phenotype"; //$NON-NLS-1$
 	public static final String GENETICS = "Genetics"; //$NON-NLS-1$
 	public static final String ORGANISM = "Organism"; //$NON-NLS-1$
 	private static final String GENOTYPE = "Genotype"; //$NON-NLS-1$
 	private static final String NOTE = "Note"; //$NON-NLS-1$
 	private static final String NOTES = "Notes"; //$NON-NLS-1$
 	private static final String PROGENIESCOUNT = "Progenies"; //$NON-NLS-1$
 	private static final String MATINGSCOUNT = "Matings"; //$NON-NLS-1$
 	private Model model;
 	private String modelName;
 
 	private void checkAllSheets(HSSFWorkbook wb) throws ParseException
 	{
 		boolean ret = wb.getSheet(ORGANISM) != null && wb.getSheet(GENETICS) != null && wb.getSheet(PHENOTYPE) != null;
 		if (!ret)
 		{
 			throw new ParseException(MessageFormat.format(Messages.getString("Load.17"), PHENOTYPE, GENETICS, ORGANISM)); //$NON-NLS-1$
 		}
 
 	}
 
 	private boolean isRequiredColumnsForGenetics(String columnName)
 	{
 		boolean ret = CHROMOSOME.equals(columnName) || GENE.equals(columnName) || GENE_LOCATION.equals(columnName) || ALLELES.equals(columnName);
 		return ret;
 	}
 
 	private void chechRequiredColumnsForGenetics(Set<String> keys) throws ParseException
 	{
 		boolean ret = keys.contains(CHROMOSOME) && keys.contains(GENE) && keys.contains(GENE_LOCATION) && keys.contains(ALLELES);
 		if (!ret)
 		{
 			throw new ParseException(MessageFormat.format(Messages.getString("Load.18"), GENETICS, CHROMOSOME, GENE, GENE_LOCATION, ALLELES)); //$NON-NLS-1$
 		}
 	}
 
 	private void chechRequiredColumnsForPhenotype(Set<String> keys) throws ParseException
 	{
 		boolean ret = keys.contains(GENOTYPE);
 		if (!ret)
 		{
 			throw new ParseException(MessageFormat.format(Messages.getString("Load.19"), GENETICS, CHROMOSOME, GENE, GENE_LOCATION, ALLELES)); //$NON-NLS-1$
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void parseOrganismSheet(HSSFSheet sheet, ModelWriter model, Genome genome)
 	{
 		Iterator<HSSFRow> rows = sheet.rowIterator();
 		while (rows.hasNext())
 		{
 			HSSFRow r = rows.next();
 			String key = String.valueOf(r.getCell(r.getFirstCellNum() + 0));
 			String value = String.valueOf(r.getCell((r.getFirstCellNum() + 1)));
 			if (key.toLowerCase().startsWith(NAME.toLowerCase()))
 			{
 				genome.setName(value);
 			}
 			if (key.toLowerCase().startsWith(SEX_TYPE.toLowerCase()))
 			{
 				genome.setSexType(value);
 			}
 			if (key.toLowerCase().startsWith(VISUALIZER.toLowerCase()))
 			{
 				model.setVisualizerClass(value);
 			}
 			if (key.toLowerCase().startsWith(MALERECOMBINATIONRATE.toLowerCase()))
 			{
 				float rate = Float.parseFloat(value);
 				model.setRecombinationRate(rate / 100f, Sex.MALE);
 			}
 			if (key.toLowerCase().startsWith(FEMALERECOMBINATIONRATE.toLowerCase()))
 			{
 				float rate = Float.parseFloat(value);
 				model.setRecombinationRate(rate / 100f, Sex.FEMALE);
 			}
 			if (key.toLowerCase().startsWith(PROGENIESCOUNT.toLowerCase()))
 			{
 				int progeniesCount = (int) Float.parseFloat(value);
 				model.setProgeniesCount(progeniesCount);
 			}
 			if (key.toLowerCase().startsWith(MATINGSCOUNT.toLowerCase()))
 			{
 				int matingsCount = (int) Float.parseFloat(value);
 				model.setMatingsCount(matingsCount);
 			}
 
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void parseGeneticsSheet(HSSFSheet sheet, ModelWriter model, Genome genome) throws ParseException
 	{
 		Iterator<HSSFRow> rows = sheet.rowIterator();
 		HashMap<Integer, String> headings = new HashMap<Integer, String>();
 		Set<String> set = new LinkedHashSet<String>();
 		parseHeading(rows, headings, set);
 		chechRequiredColumnsForGenetics(set);
 		Map<String, CreatureImpl> creatures = new LinkedHashMap<String, CreatureImpl>();
 		for (String name : set)
 		{
 			if (!isRequiredColumnsForGenetics(name) && name.length() != 0)
 			{
 				CreatureImpl c = new CreatureImpl(name, genome, null, new GeneticMakeupImpl(), model.getMatingsCount(), new HashMap<String, String>(), new CreatureSetImpl());
 				c.setReadOnly(true);
 				creatures.put(name, c);
 			}
 		}
 
 		while (rows.hasNext())
 		{
 			String chromosomeName = null;
 			String geneName = null;
 			float geneLocation = Float.NaN;
 			String[] alleles = null;
 
 			HashMap<String, String> organismMakeup = new HashMap<String, String>();
 			HSSFRow r = rows.next();
 			Iterator<HSSFCell> cells = r.cellIterator();
 			while (cells.hasNext())
 			{
 				HSSFCell c = cells.next();
 				String heading = headings.get(Integer.valueOf(c.getColumnIndex()));
 				if (heading == null || heading.length() == 0)
 				{
 					continue;
 				}
 				if (CHROMOSOME.equalsIgnoreCase(heading))
 				{
 					if (c.getCellType() == HSSFCell.CELL_TYPE_NUMERIC)
 					{
 						double value = c.getNumericCellValue();
 						if (value == Math.round(value))
 						{
 							chromosomeName = "" + (int) value; //$NON-NLS-1$
 						}
 					}
 					else
 					{
 						chromosomeName = c.toString();
 					}
 
 				}
 				else if (GENE.equalsIgnoreCase(heading))
 				{
 					geneName = c.toString();
 				}
 				else if (GENE_LOCATION.equalsIgnoreCase(heading))
 				{
 					geneLocation = (float) c.getNumericCellValue();
 				}
 				else if (ALLELES.equalsIgnoreCase(heading))
 				{
 					alleles = c.toString().split(","); //$NON-NLS-1$
 				}
 				else
 				{
 					organismMakeup.put(heading, c.toString());
 				}
 			}
 			if ((chromosomeName == null || chromosomeName.length() == 0) && (geneName == null || geneName.length() == 0))
 			{
 				continue;
 			}
 
 			if (chromosomeName == null || geneName == null || Double.isNaN(geneLocation) || alleles == null || alleles.length == 0)
 			{
 				throw new ParseException(MessageFormat.format(Messages.getString("Load.22"), r.getRowNum())); //$NON-NLS-1$
 
 			}
 			if (chromosomeName.length() == 0 && geneName.length() == 0)
 			{
 				continue;
 			}
 
 			Chromosome c = getChromosomeByName(genome, chromosomeName);
 			Gene g = getGeneByName(geneName, geneLocation, c, r, chromosomeName);
 			generateAlleles(alleles, g);
 			updateCreaturesGeneticMap(creatures, organismMakeup, r, g);
 		}
 		CreatureSet c = new star.genetics.genetic.impl.CreatureSetImpl();
 		for (java.util.Map.Entry<String, CreatureImpl> entry : creatures.entrySet())
 		{
 			CreatureImpl creature = entry.getValue();
 			creature.setSex(getSex(creature, genome));
 			isValidGenoType(creature, genome);
 			c.add(creature);
 			Utilities.printCreature(creature);
 		}
 		model.setCreatures(c);
 	}
 
 	private Creature.Sex getSex(CreatureImpl cr, Genome genome) throws ParseException
 	{
 		GeneticMakeup makeup = cr.getMakeup();
 		if (genome.getSexType() == Genome.SexType.XY)
 		{
 			Chromosome c = genome.getChromosomeByName("Y"); //$NON-NLS-1$
 			// this algorithm assumes that
 			// if there are any 0-s or 2-s on y chromosome than $ones will note be one
 			// if there are any 1-s on y chromosome that $zeros will not be zero
 			int ones = 1;
 			int zeros = 0;
 
 			for (Gene g : c.getGenes())
 			{
 				if (makeup.get(g) == null)
 				{
 					makeup.put(g, new DiploidAllelesImpl(null, null));
 				}
 				int count = makeup.get(g) != null ? makeup.get(g).getAlleleCount() : 0;
 				ones *= count;
 				zeros += count;
 			}
 			if (ones == 1)
 			{
 				// all y chromosome genes DiploidAlleles have one gene on them -- male
 				Chromosome c2 = genome.getChromosomeByName("X"); //$NON-NLS-1$
 				for (Gene g : c2.getGenes())
 				{
 					if (makeup.get(g).getAlleleCount() != 1)
 					{
 						throw new ParseException(MessageFormat.format(Messages.getString("Load.25"), cr.getName())); //$NON-NLS-1$
 					}
 				}
 				return Creature.Sex.MALE;
 			}
 			if (zeros == 0)
 			{
 				// all y chromosome genes DiploidAlleles have no gene on them -- female
 				Chromosome c2 = genome.getChromosomeByName("X"); //$NON-NLS-1$
 				for (Gene g : c2.getGenes())
 				{
 					if (makeup.get(g).getAlleleCount() != 2)
 					{
 						throw new ParseException(MessageFormat.format(Messages.getString("Load.27"), cr.getName())); //$NON-NLS-1$
 					}
 				}
 				return Creature.Sex.FEMALE;
 
 			}
 			throw new ParseException(MessageFormat.format(Messages.getString("Load.28"), cr.getName())); //$NON-NLS-1$
 		}
 		else
 		{
 			throw new ParseException(Messages.getString("Load.29")); //$NON-NLS-1$
 		}
 	}
 
 	private void isValidGenoType(CreatureImpl cr, Genome genome) throws ParseException
 	{
 		GeneticMakeup makeup = cr.getMakeup();
 
 		if (genome.getSexType() == Genome.SexType.XY)
 		{
 			Iterator<Chromosome> iter = genome.iterator();
 			while (iter.hasNext())
 			{
 				Chromosome c = iter.next();
 				for (Gene g : c.getGenes())
 				{
 					if (makeup.containsKey(g))
 					{
 						DiploidAlleles diploid = makeup.get(g);
 						if (!(g.getChromosome().getName().equalsIgnoreCase("X") || g.getChromosome().getName().equalsIgnoreCase("Y"))) //$NON-NLS-1$ //$NON-NLS-2$
 						{
 							if (diploid.getAlleleCount() != 2)
 							{
 								throw new ParseException(MessageFormat.format(Messages.getString("Load.32"), g.getId(), cr.getName())); //$NON-NLS-1$
 							}
 						}
 					}
 					else
 					{
 						throw new ParseException(MessageFormat.format(Messages.getString("Load.33"), g.getName(), cr.getName())); //$NON-NLS-1$
 					}
 				}
 			}
 		}
 		else
 		{
 			throw new ParseException(Messages.getString("Load.34")); //$NON-NLS-1$
 		}
 	}
 
 	private void updateCreaturesGeneticMap(Map<String, CreatureImpl> creatures, Map<String, String> organismMakeup, HSSFRow r, Gene g) throws ParseException
 	{
 		for (java.util.Map.Entry<String, String> makeup : organismMakeup.entrySet())
 		{
 			CreatureImpl creature = creatures.get(makeup.getKey());
 			String[] creatureAlleles = makeup.getValue().split(","); //$NON-NLS-1$
 			if (creatureAlleles.length == 1 || creatureAlleles.length == 2)
 			{
 				Allele a1 = g.getAlleleByName(creatureAlleles[0]);
 				Allele a2 = creatureAlleles.length == 2 ? g.getAlleleByName(creatureAlleles[1]) : null;
 				DiploidAlleles diploidMakeup = new star.genetics.genetic.impl.DiploidAllelesImpl(a1, a2);
 				creature.getMakeup().put(g, diploidMakeup);
 			}
 			else
 			{
 				throw new ParseException(MessageFormat.format(Messages.getString("Load.36"), r.getRowNum(), makeup.getKey(), makeup.getValue())); //$NON-NLS-1$
 			}
 		}
 	}
 
 	private void generateAlleles(String[] alleles, Gene g)
 	{
 		for (String name : alleles)
 		{
 			new star.genetics.genetic.impl.AlleleImpl(name, g);
 		}
 	}
 
 	private Gene getGeneByName(String geneName, float geneLocation, Chromosome c, HSSFRow r, String chromosomeName) throws ParseException
 	{
 		if (c.getGeneByName(geneName) != null)
 		{
 			throw new ParseException(MessageFormat.format(Messages.getString("Load.37"), geneName, r.getRowNum(), chromosomeName)); //$NON-NLS-1$
 		}
 
 		star.genetics.genetic.impl.GeneImpl g = new star.genetics.genetic.impl.GeneImpl(geneName, geneLocation, c);
 		return g;
 	}
 
 	private Chromosome getChromosomeByName(Genome genome, String chromosomeName)
 	{
 		Chromosome c = genome.getChromosomeByName(chromosomeName);
 
 		if (c == null)
 		{
 			star.genetics.genetic.impl.ChromosomeImpl c1 = new star.genetics.genetic.impl.ChromosomeImpl(chromosomeName, genome);
 			c = c1;
 		}
 		return c;
 	}
 
 	@SuppressWarnings("unchecked")
 	void parseHeading(Iterator<HSSFRow> rows, Map<Integer, String> headings, Set<String> set) throws ParseException
 	{
 		headings.clear();
 		set.clear();
 		if (rows.hasNext())
 		{
 			HSSFRow r = rows.next();
 			Iterator<HSSFCell> cells = r.cellIterator();
 			while (cells.hasNext())
 			{
 				HSSFCell c = cells.next();
 				headings.put(Integer.valueOf(c.getColumnIndex()), String.valueOf(c));
 				set.add(String.valueOf(c));
 			}
 		}
 		else
 		{
 			throw new ParseException(Messages.getString("Load.38")); //$NON-NLS-1$
 		}
 
 	}
 
 	@SuppressWarnings("unchecked")
 	void parsePhenotypeSheet(HSSFSheet sheet, ModelWriter model, Genome genome) throws ParseException
 	{
 		Iterator<HSSFRow> rows = sheet.rowIterator();
 		HashMap<Integer, String> headings = new HashMap<Integer, String>();
 		HashSet<String> set = new HashSet<String>();
 		parseHeading(rows, headings, set);
 		chechRequiredColumnsForPhenotype(set);
 
 		RuleSet rules = new star.genetics.genetic.impl.RuleSetImpl();
 		while (rows.hasNext())
 		{
 			HashMap<String, String> properties = new HashMap<String, String>();
 			String rule = null;
 
 			HSSFRow r = rows.next();
 			Iterator<HSSFCell> cells = r.cellIterator();
 			while (cells.hasNext())
 			{
 				HSSFCell c = cells.next();
 				String heading = headings.get(Integer.valueOf(c.getColumnIndex()));
 				if (GENOTYPE.equalsIgnoreCase(heading))
 				{
 					rule = c.toString();
 				}
 				else if (NOTE.equalsIgnoreCase(heading) || NOTES.equalsIgnoreCase(heading))
 				{
 
 				}
 				else
 				{
 					String value = c.toString();
 					if (value.length() != 0)
 					{
 						properties.put(heading, value);
 					}
 				}
 			}
 
 			if (rule == null && properties.size() != 0)
 			{
 				throw new ParseException(MessageFormat.format(Messages.getString("Load.39"), r.getRowNum())); //$NON-NLS-1$
 			}
 
 			if (properties.size() == 0)
 			{
 				continue;
 			}
 
 			rules.add(new star.genetics.genetic.impl.RuleImpl(rule, properties, genome));
 		}
 		model.setRules(rules);
 	}
 
 	public Model Load(InputStream is) throws ParseException, IOException, DataFormatException
 	{
 		return load(new ByteArrayInputStream(priv.load(FileUtils.getStreamToByteArray(is))));
 	}
 
 	public Model load(InputStream is) throws ParseException
 	{
 		try
 		{
 			ModelWriter model = new star.genetics.genetic.impl.ModelImpl();
 			Genome genome = new star.genetics.genetic.impl.GenomeImpl();
 			model.setGenome(genome);
 			POIFSFileSystem fs = new POIFSFileSystem(is);
 			HSSFWorkbook wb = new HSSFWorkbook(fs);
 
 			Model ret = null;
 			try
 			{
 				ret = (new Load2()).load(wb, model, genome);
 			}
 			catch (ParseException ex)
 			{
 
 				try
 				{
 					ret = oldLoad(wb, model, genome);
 					UIHelpers.track("OldLoad"); //$NON-NLS-1$
 				}
 				catch (ParseException ex2)
 				{
 					ex2.printStackTrace();
 					throw ex;
 				}
 
 			}
 			return ret;
 		}
 		catch (Throwable t)
 		{
 			System.err.println("Printed here..."); //$NON-NLS-1$
 			t.printStackTrace();
 			if (t.getCause() != null)
 			{
 				t.getCause().printStackTrace();
 			}
 			throw new ParseException(t.getLocalizedMessage(), t);
 		}
 	}
 
 	private Model oldLoad(HSSFWorkbook wb, ModelWriter model, Genome genome) throws ParseException
 	{
 		checkAllSheets(wb);
 		parseOrganismSheet(wb.getSheet(ORGANISM), model, genome);
 		parseGeneticsSheet(wb.getSheet(GENETICS), model, genome);
 		parsePhenotypeSheet(wb.getSheet(PHENOTYPE), model, genome);
 		updateCreatures(model, genome);
 		return model;
 	}
 
 	private void updateCreatures(ModelWriter model, Genome genome) throws ParseException
 	{
 
 		CreatureSet c = new star.genetics.genetic.impl.CreatureSetImpl();
 		RuleSet r = model.getRules();
 		for (Creature cr : model.getCreatures())
 		{
 			CreatureImpl creature = (CreatureImpl) cr;
 			creature.setSex(getSex(creature, genome));
 			Map<String, String> map = r.getProperties(creature.getMakeup(), creature.getSex());
 			creature.addProperties(map);
 			isValidGenoType(creature, genome);
 			c.add(creature);
 			Utilities.printCreature(creature);
 		}
 		model.setCreatures(c);
 	}
 
 	@Override
 	@Handles(raises = { LoadModelRaiser.class })
 	void openModel(final OpenModelRaiser r)
 	{
 		if (r.getModelURL().toExternalForm().endsWith(".xls")) //$NON-NLS-1$
 		{
 			try
 			{
 				model = load(r.getOpenModelStream());
 				modelName = r.getModelFileName();
 				raise_LoadModelEvent();
 			}
 			catch (Exception ex)
 			{
 				setErrorMessage(new RuntimeException(ex.getLocalizedMessage(), ex));
 				raise_ErrorDialogEvent();
 			}
 		}
		priv.openModel(r, this);
 		// needs to load PrivateIO
 	}
 
 	public Model getModel()
 	{
 		return model;
 	}
 
 	public String getModelName()
 	{
 		return modelName;
 	}
 
 	public byte[] save(byte[] s)
 	{
 		return priv.save(s);
 	}
 
 	@Override
 	public void setModel(Model model, String modelName)
 	{
 		this.model = model;
 		this.modelName = modelName;
 	}
 }
