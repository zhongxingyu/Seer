 /*
 This file is part of leafdigital browserstats.
 
 browserstats is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 browserstats is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with browserstats.  If not, see <http://www.gnu.org/licenses/>.
 
 Copyright 2010 Samuel Marshall.
 */
 package com.leafdigital.browserstats.summary;
 
 import java.io.*;
 import java.util.*;
 import java.util.regex.*;
 
 import javax.xml.parsers.*;
 
 import org.w3c.dom.*;
 import org.xml.sax.SAXException;
 
 import com.leafdigital.browserstats.shared.*;
 import com.leafdigital.util.xml.XML;
 
 /**
  * Summarises a .knownagents file into desired categories in order to produce
  * nice numbers in CSV format which could be used for a pie chart or similar.
  */
 public class Summarise extends CommandLineTool
 {
 	private boolean overwrite, stdout, preventOther, showPercentages,
 		showExcluded=true, showHeaders=true, showTotal = true, autoGroup = true,
 		explicitAutoGroup;
 	private LinkedList<Conditions> parameters = new LinkedList<Conditions>();
 	private LinkedList<AutoVersion> autoVersions = new LinkedList<AutoVersion>();
 	private String onlyCategory, suffix;
 	private File folder;
 
 	private EnumSet<KnownAgent.Field> autoGroupFields =
 		EnumSet.of(KnownAgent.Field.AGENT);
 
 	private boolean csv=false, xml=false;
 
 	private TestType test = TestType.NONE;
 	private String[] testParams;
 
 	/**
 	 * @param args Command-line arguments
 	 */
 	public static void main(String[] args)
 	{
 		(new Summarise()).run(args);
 	}
 
 	/** Special test constants to check options. */
 	private enum TestType
 	{
 		/** No test */
 		NONE(-1),
 
 		/** Run the group member self-test */
 		SHOWGROUPS(0);
 
 		private int params;
 		TestType(int params)
 		{
 			this.params = params;
 		}
 
 		/** @return Number of parameters required by the option */
 		int getParams()
 		{
 			return params;
 		}
 	}
 
 	@Override
 	protected int processArg(String[] args, int i)
 		throws IllegalArgumentException
 	{
 		if(args[i].equals("-overwrite"))
 		{
 			overwrite = true;
 			return 1;
 		}
 		if(args[i].equals("-folder"))
 		{
 			checkArgs(args, i, 1);
 			folder = new File(args[i+1]);
 			if(!folder.exists() || !folder.isDirectory())
 			{
 				throw new IllegalArgumentException("Folder does not exist: " + folder);
 			}
 			return 2;
 		}
 		if(args[i].equals("-stdout"))
 		{
 			stdout = true;
 			return 1;
 		}
 		if(args[i].equals("-preventother"))
 		{
 			preventOther = true;
 			return 1;
 		}
 		if(args[i].equals("-include"))
 		{
 			Conditions parameter = new Include(args, i+1);
 			parameters.add(parameter);
 			return parameter.getArgsUsed() + 1;
 		}
 		if(args[i].equals("-exclude"))
 		{
 			Conditions parameter = new Exclude(args, i+1);
 			parameters.add(parameter);
 			return parameter.getArgsUsed() + 1;
 		}
 		if(args[i].equals("-format"))
 		{
 			checkArgs(args, i, 1);
 			String format = args[i+1];
 			if(format.equals("csv"))
 			{
 				csv = true;
 			}
 			else if(format.equals("xml"))
 			{
 				xml = true;
 			}
 			else
 			{
 				throw new IllegalArgumentException("-format unknown: " + format);
 			}
 			return 2;
 		}
 		if(args[i].equals("-group"))
 		{
 			checkArgs(args, i, 1);
 			if(explicitAutoGroup)
 			{
 				throw new IllegalArgumentException("-group must be before -autogroup");
 			}
 			autoGroup = false;
 			String group = args[i+1];
 			if(group.equals(SpecialNames.GROUP_EXCLUDED) || group.equals(SpecialNames.GROUP_OTHER))
 			{
 				throw new IllegalArgumentException("-group name is reserved: " + group);
 			}
 			if(group.indexOf(",") != -1)
 			{
 				throw new IllegalArgumentException("-group name may not contain comma: "
 					+ group);
 			}
 			Conditions parameter = new Group(group, args, i+2);
 			parameters.add(parameter);
 			return parameter.getArgsUsed() + 2;
 		}
 		if(args[i].equals("-autogroup"))
 		{
 			checkArgs(args, i, 1);
 			autoGroup = true;
 			explicitAutoGroup = true;
 
 			autoGroupFields = EnumSet.noneOf(KnownAgent.Field.class);
 			int used = 0;
 			for(int index = i+1; index<args.length; index++)
 			{
 				String field = args[index];
 				if(field.startsWith("-"))
 				{
 					break;
 				}
 				try
 				{
 					autoGroupFields.add(KnownAgent.Field.valueOf(field.toUpperCase()));
 				}
 				catch(IllegalArgumentException e)
 				{
 					throw new IllegalArgumentException("-autogroup unknown field: "
 						+ field);
 				}
 				used++;
 			}
 			if(used == 0)
 			{
 				// Default to using agent
 				autoGroupFields.add(KnownAgent.Field.AGENT);
 			}
 			return used + 1;
 		}
 		if(args[i].equals("-autoversion"))
 		{
 			checkArgs(args, i, 2);
 			AutoVersionType type = AutoVersionType.get(args[i+1]);
 			if(type == null)
 			{
 				throw new IllegalArgumentException("-autoversion type not known: "
 					+ args[i+1]);
 			}
 			try
 			{
 				autoVersions.add(new AutoVersion(Pattern.compile(args[i+2]), type));
 			}
 			catch(PatternSyntaxException e)
 			{
 				throw new IllegalArgumentException("-autoversion group name not valid "
 					+ "regular expression: " + args[i+2]);
 			}
 			return 3;
 		}
 		if(args[i].equals("-test"))
 		{
 			checkArgs(args, i, 1);
 			try
 			{
 				test = TestType.valueOf(args[i+1].toUpperCase());
 			}
 			catch(IllegalArgumentException e)
 			{
 				throw new IllegalArgumentException(
 					"Unrecognised test type: " + args[i+1]);
 			}
 			checkArgs(args, i, 1+test.getParams());
 			testParams = new String[test.getParams()];
 			for(int j=0; j<test.getParams(); j++)
 			{
 				testParams[j] = args[i+2+j];
 			}
 
 			return 2+test.getParams();
 		}
 		if(args[i].equals("-category"))
 		{
 			checkArgs(args, i, 1);
 			onlyCategory = args[i+1];
 			return 2;
 		}
 		if(args[i].equals("-showpercentages"))
 		{
 			showPercentages = true;
 			return 1;
 		}
 		if(args[i].equals("-noexcluded"))
 		{
 			showExcluded = false;
 			return 1;
 		}
 		if(args[i].equals("-nototal"))
 		{
 			showTotal = false;
 			return 1;
 		}
 		if(args[i].equals("-noheaders"))
 		{
 			showHeaders = false;
 			return 1;
 		}
 		if(args[i].equals("-suffix"))
 		{
 			checkArgs(args, i, 1);
 			suffix = args[i+1];
 			return 2;
 		}
 		return 0;
 	}
 
 	@Override
 	protected void validateArgs() throws IllegalArgumentException
 	{
 		// Default to xml format
 		if(!(csv || xml))
 		{
 			xml = true;
 		}
 		// Check they're not trying to do stdout with both formats
 		if(csv && xml && stdout)
 		{
 			throw new IllegalArgumentException(
 				"Cannot use -stdout together with -format both");
 		}
 		// Check the auto versioning matches a group
 		autoLoop: for(AutoVersion auto : autoVersions)
 		{
 			// Try all groups
 			for(Conditions parameter : parameters)
 			{
 				if(parameter instanceof Group)
 				{
 					if(auto.getGroup().matcher(((Group)parameter).getName()).find() &&
 						parameter.getVersion()==null && parameter.getVersionOperator()==null)
 					{
 
 						continue autoLoop;
 					}
 				}
 			}
 
 			// Ooops, didn't match
 			throw new IllegalArgumentException("-autoversion pattern does not match "
 				+ "any group: " + auto.getGroup().pattern());
 		}
 	}
 
 	@Override
 	protected void go()
 	{
 		File current = null;
 		try
 		{
 			// Add 'other' group to parameters
 			Other other = new Other();
 			parameters.add(other);
 
 			File[] input = getInputFiles();
 
 			// Read stdin data if required
 			Document stdinDoc = null;
 			if(input == null)
 			{
 				stdinDoc = loadXml(null);
 			}
 
 			// Do autogroup
 			if(autoGroup)
 			{
 				Set<KnownAgent> include = new HashSet<KnownAgent>();
 				if(input == null)
 				{
 					buildAutoGroupAgents(stdinDoc, other, include);
 				}
 				else
 				{
 					for(File f : input)
 					{
 						current = f;
 						Document d = loadXml(f);
 						buildAutoGroupAgents(d, other, include);
 					}
 				}
 
 				// Now that we have the list of agents, add the actual groups
 				addAutoGroups(include);
 
 				// Put 'other' back at the end
 				parameters.remove(other);
 				parameters.add(other);
 			}
 
 			if(input == null)
 			{
 				doFile(null, stdinDoc);
 			}
 			else
 			{
 				for(File f : input)
 				{
 					current = f;
 					Document d = loadXml(f);
 					doFile(f, d);
 				}
 			}
 		}
 		catch(IOException e)
 		{
 			String name = current == null ? "stdin" : current.toString();
 			System.err.println(name + ": " + e.getMessage());
 		}
 	}
 
 	private void addAutoGroups(Set<KnownAgent> agents)
 	{
 		// Check which parts of the agent vary; we will use this in group names
 		boolean varyType = false, varyOs = false,
 			varyEngine = false, varyAgent = false;
 		String singleType = null, singleOs = null,
 			singleEngine = null, singleAgent = null;
 		for(KnownAgent agent : agents)
 		{
 			if(singleType == null)
 			{
 				singleType = agent.getType();
 			}
 			else
 			{
 				if(!agent.getType().equals(singleType))
 				{
 					varyType = true;
 				}
 			}
 
 			if(singleOs == null)
 			{
 				singleOs = agent.getOs();
 			}
 			else
 			{
 				if(!agent.getOs().equals(singleOs))
 				{
 					varyOs = true;
 				}
 			}
 
 			if(singleEngine == null)
 			{
 				singleEngine = agent.getEngine();
 			}
 			else
 			{
 				if(!agent.getEngine().equals(singleEngine))
 				{
 					varyEngine = true;
 				}
 			}
 
 			if(singleAgent == null)
 			{
 				singleAgent = agent.getAgent();
 			}
 			else
 			{
 				if(!agent.getAgent().equals(singleAgent))
 				{
 					varyAgent = true;
 				}
 			}
 		}
 
 		// If they're all always the same, just include the agent name
 		if(!(varyType || varyOs || varyEngine || varyAgent))
 		{
 			varyAgent = true;
 		}
 
 		// Add a group for each agent
 		for(KnownAgent agent : agents)
 		{
 			// Work out group name
 			String name = agent.toStringWith(autoGroupFields).replace(":", " / ");
 
 			// Specify group to match agent
 			String[] args = new String[]
 	    {
 				"type",
 				Pattern.quote(agent.getType()),
 				"os",
 				Pattern.quote(agent.getOs()),
 				"engine",
 				Pattern.quote(agent.getEngine()),
 				"agent",
 				Pattern.quote(agent.getAgent())
 	    };
 
 			Conditions parameter = new Group(name, args, 0);
 			parameters.add(parameter);
 		}
 	}
 
 	private void buildAutoGroupAgents(Document d, Other other, Set<KnownAgent> include) throws IOException
 	{
 		// Get file contents
 		XnownAgentFileContents contents = new XnownAgentFileContents(d);
 		String[] categories = contents.getCategories();
 		KnownAgent[] knownAgents = contents.getKnownAgents();
 
 		// Check category
 		int onlyCategoryIndex = getCategoryIndex(categories);
 
 		// Count values; this will handle -include and -exclude lines, assigning
 		// all remaining agents to the 'other' group
 		countParameters(parameters, categories, knownAgents);
 		KnownAgent[] found = other.getKnownAgents();
 
 		// Organise found agents by their identifier minus version
 		Map<String, List<KnownAgent>> uniqueAgents =
 			new HashMap<String, List<KnownAgent>>();
 		for(KnownAgent agent : found)
 		{
 			String key = agent.toStringWith(autoGroupFields);
 			List<KnownAgent> list = uniqueAgents.get(key);
 			if(list == null)
 			{
 				list = new LinkedList<KnownAgent>();
 				uniqueAgents.put(key, list);
 			}
 			list.add(agent);
 		}
 
 		// Group versions together
 		found = new KnownAgent[uniqueAgents.size()];
 		Iterator<List<KnownAgent>> iterator = uniqueAgents.values().iterator();
 		for(int i=0; i<found.length; i++)
 		{
 			List<KnownAgent> list = iterator.next();
 
 			if(list.size() == 1)
 			{
 				found[i] = list.get(0);
 			}
 			else
 			{
 				found[i] = KnownAgent.combineCountsWithSameFields(
 					autoGroupFields, list);
 			}
 		}
 
 		// Count up totals
 		int overallTotal = 0;
 		int[] categoryTotals = new int[categories.length];
 		// If the preventOther flag is enabled, we add an entry for all agents
 		// so the total counts are zero (making the thresholds zero); don't bother
 		// counting
 		if(!preventOther)
 		{
 			for(KnownAgent agent : found)
 			{
 				// Get counts
 				int count = agent.getCount();
 				int[] categoryCounts = agent.getCategoryCounts();
 
 				// Build up totals
 				overallTotal += count;
 				for(int category=0; category<categories.length; category++)
 				{
 					categoryTotals[category] += categoryCounts[category];
 				}
 			}
 		}
 
 		// Work out thresholds for each category
 		int overallThreshold = overallTotal / 200;
 		int[] categoryThresholds = new int[categories.length];
 		for(int category=0; category<categories.length; category++)
 		{
 			categoryThresholds[category] = categoryTotals[category] / 200;
 		}
 
 		// Now loop through and add everything over threshold in any of the
 		// categories we're including, or in total (unless we are doing 'only
 		// category' which excludes the total value)
 		for(KnownAgent agent : found)
 		{
 			boolean overThreshold = preventOther;
 			if(!overThreshold && onlyCategoryIndex == -1
 				&& agent.getCount() > overallThreshold)
 			{
 				overThreshold = true;
 			}
 			if(!overThreshold)
 			{
 				int[] categoryCounts = agent.getCategoryCounts();
 				for(int category=0; category<categories.length; category++)
 				{
 					if(onlyCategoryIndex != -1 && onlyCategoryIndex != category)
 					{
 						continue;
 					}
 					if(categoryCounts[category] > categoryThresholds[category])
 					{
 						overThreshold = true;
 						break;
 					}
 				}
 			}
 
 			if(overThreshold)
 			{
 				include.add(agent.cloneWithoutCountData());
 			}
 		}
 	}
 
 	private static class XnownAgentFileContents
 	{
 		private String[] categories;
 		private KnownAgent[] knownAgents;
 
 		XnownAgentFileContents(Document d) throws IOException
 		{
 			// Parse XML
 			Element root = d.getDocumentElement();
 			if(!root.getTagName().equals("knownagents"))
 			{
 				throw new IOException("XML root tag <knownagents> not found");
 			}
 			categories = new String[0];
 			if(root.hasAttribute("categories")
 				&& root.getAttribute("categories").length() > 0)
 			{
 				categories = root.getAttribute("categories").split(",");
 			}
 			LinkedList<KnownAgent> knownAgentList = new LinkedList<KnownAgent>();
 			for(Node n = root.getFirstChild(); n!=null;
 				n = n.getNextSibling())
 			{
 				if(n instanceof Element && ((Element)n).getTagName().equals("agent"))
 				{
 					knownAgentList.add(new KnownAgent((Element)n, categories));
 				}
 			}
 			knownAgents = knownAgentList.toArray(new KnownAgent[knownAgentList.size()]);
 		}
 
 		/**
 		 * @return Categories from this file
 		 */
 		public String[] getCategories()
 		{
 			return categories;
 		}
 
 		/**
 		 * @return Array of known agents from this file
 		 */
 		public KnownAgent[] getKnownAgents()
 		{
 			return knownAgents;
 		}
 	}
 
 	private void doFile(File file, Document d) throws IOException
 	{
 		// Get file contents
 		XnownAgentFileContents contents = new XnownAgentFileContents(d);
 		String[] categories = contents.getCategories();
 		KnownAgent[] knownAgents = contents.getKnownAgents();
 
 		// Check category
 		int onlyCategoryIndex = getCategoryIndex(categories);
 
 		// Count values
 		LinkedList<Conditions> localParameters = parameters;
 		countParameters(localParameters, categories, knownAgents);
 
 		// Is auto versioning in use?
 		if(!autoVersions.isEmpty())
 		{
 			for(AutoVersion auto : autoVersions)
 			{
 				localParameters = auto.apply(localParameters);
 			}
 			countParameters(localParameters, categories, knownAgents);
 		}
 
 		// Build list of groups with the same name (these result in only a single
 		// line), while retaining the current ordering
 		LinkedList<Equivalents> equivalentList = new LinkedList<Equivalents>();
 		for(Conditions parameter : localParameters)
 		{
 			boolean done = false;
 			for(Equivalents equivalents : equivalentList)
 			{
 				if(equivalents.matches(parameter))
 				{
 					equivalents.add(parameter);
 					done = true;
 					break;
 				}
 			}
 
 			if(!done)
 			{
 				equivalentList.add(new Equivalents(parameter));
 			}
 		}
 
 		// Move 'Other' and 'Excluded' to end (in that order)
 		for(int i=0; i<equivalentList.size(); i++)
 		{
 			if(equivalentList.get(i).isOther())
 			{
 				equivalentList.addLast(equivalentList.remove(i));
 				break;
 			}
 		}
 		for(int i=0; i<equivalentList.size(); i++)
 		{
 			if(equivalentList.get(i).isExcluded())
 			{
 				equivalentList.addLast(equivalentList.remove(i));
 				break;
 			}
 		}
 
 		// Show groups
 		if(test == TestType.SHOWGROUPS)
 		{
 			for(Equivalents equivalents : equivalentList)
 			{
 				equivalents.displayAgents();
 			}
 			return;
 		}
 
 		// Count other and excluded totals
 		int otherCount = 0, excludedCount = 0, totalCount = 0;
 		int[] otherCategoryCounts = new int[categories.length],
 			excludedCategoryCounts = new int[categories.length],
 			totalCategoryCounts = new int[categories.length];
 		for(Equivalents equivalents : equivalentList)
 		{
 			if(equivalents.isOther())
 			{
 				totalCount += equivalents.getCount();
 				otherCount += equivalents.getCount();
 				for(int i=0; i<categories.length; i++)
 				{
 					otherCategoryCounts[i] += equivalents.getCategoryCounts()[i];
 				}
 			}
 			else if(equivalents.isExcluded())
 			{
 				excludedCount += equivalents.getCount();
 				for(int i=0; i<categories.length; i++)
 				{
 					excludedCategoryCounts[i] += equivalents.getCategoryCounts()[i];
 				}
 			}
 			else
 			{
 				totalCount += equivalents.getCount();
 				for(int i=0; i<categories.length; i++)
 				{
 					totalCategoryCounts[i] += equivalents.getCategoryCounts()[i];
 				}
 			}
 		}
 
 		// Check 'prevent other' status
 		if(preventOther && otherCount != 0)
 		{
 			throw new IOException("-preventother: Some values are not covered by "
 				+ "specified groups (use -test showgroups)");
 		}
 
 		// Get target file for result
 		File targetCsv, targetXml;
 		if(file==null || stdout)
 		{
 			targetCsv = null;
 			targetXml = null;
 		}
 		else
 		{
 			String targetName = file.getName();
 			if(targetName.endsWith(".knownagents"))
 			{
 				targetName = targetName.substring(
 					0, targetName.length() - ".knownagents".length());
 			}
 			if(suffix != null)
 			{
 				targetName += "." + suffix;
 			}
 			String
 				targetCsvName = targetName + ".csv",
 				targetXmlName = targetName + ".summary";
 			if(folder != null)
 			{
 				targetCsv = new File(folder, targetCsvName);
 				targetXml = new File(folder, targetXmlName);
 			}
 			else
 			{
 				targetCsv = new File(file.getParentFile(), targetCsvName);
 				targetXml = new File(file.getParentFile(), targetXmlName);
 			}
 
 			if(!overwrite && csv && targetCsv.exists())
 			{
 				throw new IOException("Target file already exists: " + targetCsv);
 			}
 			if(!overwrite && xml && targetXml.exists())
 			{
 				throw new IOException("Target file already exists: " + targetXml);
 			}
 		}
 
 		if(csv)
 		{
 			// Open a writer on target file
 			Writer out;
 			if(targetCsv == null)
 			{
 				out = new BufferedWriter(new OutputStreamWriter(System.out));
 			}
 			else
 			{
 				out = new BufferedWriter(new OutputStreamWriter(
 					new FileOutputStream(targetCsv), "UTF-8"));
 			}
 
 			// Write headers
 			if(showHeaders)
 			{
 				// There's an empty cell here
 				if(onlyCategory == null)
 				{
 					out.write(",Requests");
 					if(showPercentages)
 					{
 						out.write(",%");
 					}
 				}
 				for(String category : categories)
 				{
 					if(onlyCategory != null && !onlyCategory.equals(category))
 					{
 						continue;
 					}
 					out.write("," + category);
 					if(showPercentages)
 					{
 						out.write(",%");
 					}
 				}
 				out.write("\n");
 			}
 
 			// Write all groups
 			for(Equivalents equivalents : equivalentList)
 			{
 				// Skip other/excluded
 				if(equivalents.isOther() || equivalents.isExcluded())
 				{
 					continue;
 				}
 				out.write(equivalents.getName());
 				if(onlyCategory == null)
 				{
 					out.write("," + equivalents.getCount());
 					if(showPercentages)
 					{
 						out.write("," + percentage(equivalents.getCount(), totalCount));
 					}
 				}
 				int[] categoryCounts = equivalents.getCategoryCounts();
 				for(int i=0; i<categoryCounts.length; i++)
 				{
 					if(onlyCategoryIndex >= 0 && onlyCategoryIndex != i)
 					{
 						continue;
 					}
 					out.write("," + categoryCounts[i]);
 					if(showPercentages)
 					{
 						out.write("," +
 							percentage(categoryCounts[i], totalCategoryCounts[i]));
 					}
 				}
 				out.write("\n");
 			}
 
 			// Write 'other'
 			if(otherCount > 0)
 			{
 				out.write(SpecialNames.GROUP_OTHER);
 				if(onlyCategory == null)
 				{
 					out.write("," + otherCount);
 					if(showPercentages)
 					{
 						out.write("," + percentage(otherCount, totalCount));
 					}
 				}
 				for(int i=0; i<otherCategoryCounts.length; i++)
 				{
 					if(onlyCategoryIndex >= 0 && onlyCategoryIndex != i)
 					{
 						continue;
 					}
 					out.write("," + otherCategoryCounts[i] );
 					if(showPercentages)
 					{
 						out.write("," +
 							percentage(otherCategoryCounts[i], totalCategoryCounts[i]));
 					}
 				}
 				out.write("\n");
 			}
 
 			// Write total
 			if(showTotal)
 			{
 				out.write("Total");
 				if(onlyCategory == null)
 				{
 					out.write("," + totalCount);
 					if(showPercentages)
 					{
 						out.write(",100.0%");
 					}
 				}
 				for(int i=0; i<totalCategoryCounts.length; i++)
 				{
 					if(onlyCategoryIndex >= 0 && onlyCategoryIndex != i)
 					{
 						continue;
 					}
 					out.write("," + totalCategoryCounts[i]);
 					if(showPercentages)
 					{
 						out.write(",100.0%");
 					}
 				}
 				out.write("\n");
 			}
 
 			// Write excluded
 			if(showExcluded)
 			{
 				out.write(SpecialNames.GROUP_EXCLUDED);
 				if(onlyCategory == null)
 				{
 					out.write("," + excludedCount);
 					if(showPercentages)
 					{
 						out.write(",");
 					}
 				}
 				for(int i=0; i<excludedCategoryCounts.length; i++)
 				{
 					if(onlyCategoryIndex >= 0 && onlyCategoryIndex != i)
 					{
 						continue;
 					}
 					out.write("," + excludedCategoryCounts[i]);
 					if(showPercentages)
 					{
 						out.write(",");
 					}
 				}
 				out.write("\n");
 			}
 
 			out.close();
 		}
 		if(xml)
 		{
 			// Open a writer on target file
 			Writer out;
 			if(targetXml == null)
 			{
 				out = new BufferedWriter(new OutputStreamWriter(System.out));
 			}
 			else
 			{
 				out = new BufferedWriter(new OutputStreamWriter(
 					new FileOutputStream(targetXml), "UTF-8"));
 			}
 
 			// Do open tag and category list
 			out.write("<summary count='" + totalCount + "'");
 			if(categories.length > 0)
 			{
 				out.write(" categories='");
 				boolean first = true;
 				for(String category : categories)
 				{
 					if(first)
 					{
 						first = false;
 					}
 					else
 					{
 						out.write(",");
 					}
 					out.write(category);
 				}
 				out.write("'");
 				for(int i=0; i<categories.length; i++)
 				{
 					out.write(" " + categories[i] + "='" + totalCategoryCounts[i] + "'");
 				}
 			}
 			out.write(">\n");
 
 			// Write all groups
 			for(Equivalents equivalents : equivalentList)
 			{
 				// All groups including other, excluded
 				out.write("<group name='" + XML.esc(equivalents.getName()) + "' count='"
 					+ equivalents.getCount() + "'");
 				int[] categoryCounts = equivalents.getCategoryCounts();
 				for(int i=0; i<categoryCounts.length; i++)
 				{
 					out.write(" " + categories[i] + "='" + categoryCounts[i] + "'");
 				}
 				out.write("/>\n");
 			}
 
 			out.write("</summary>\n");
 			out.close();
 		}
 	}
 
 	/**
 	 * Gets the unique category index to use.
 	 * @param categories Array of categories
 	 * @return Category index Category index or -1 for all categories
 	 * @throws IllegalArgumentException
 	 */
 	private int getCategoryIndex(String[] categories)
 		throws IllegalArgumentException
 	{
 		int onlyCategoryIndex = -1;
 		if(onlyCategory != null)
 		{
 			for(int i=0; i<categories.length; i++)
 			{
 				if(categories[i].equals(onlyCategory))
 				{
 					onlyCategoryIndex = i;
 					break;
 				}
 			}
 			if(onlyCategoryIndex == -1)
 			{
 				throw new IllegalArgumentException("Invalid -category: " + onlyCategory);
 			}
 		}
 		return onlyCategoryIndex;
 	}
 
 	/**
	 * @param file
	 * @return
	 * @throws IOException
 	 */
 	private Document loadXml(File file) throws IOException
 	{
 		// Load input XML
 		Document d;
 		try
 		{
 			DocumentBuilder builder =
 				DocumentBuilderFactory.newInstance().newDocumentBuilder();
 			if(file != null)
 			{
 				d = builder.parse(file);
 			}
 			else
 			{
 				d = builder.parse(System.in, "stdin");
 			}
 		}
 		catch(ParserConfigurationException e)
 		{
 			throw new IOException("Misconfigured Java XML support: "
 				+ e.getMessage());
 		}
 		catch(SAXException e)
 		{
 			throw new IOException("Invalid XML input: " + e.getMessage());
 		}
 		return d;
 	}
 
 	/**
 	 * Converts a number into a percentage string, ending with "%", with one
 	 * decimal place.
 	 * @param num Number
 	 * @param total Total
 	 * @return Percentage (or empty string if total is zero)
 	 */
 	private static String percentage(int num, int total)
 	{
 		if(total == 0)
 		{
 			return "";
 		}
 		return String.format("%.1f", (double)num * 100.0 / (double) total) + "%";
 	}
 
 	private static class Equivalents
 	{
 		private String name;
 		private LinkedList<Conditions> equivalents = new LinkedList<Conditions>();
 
 		Equivalents(Conditions first)
 		{
 			name = first.getName();
 			equivalents.add(first);
 		}
 
 		boolean matches(Conditions other)
 		{
 			return other.getName().equals(name);
 		}
 
 		void add(Conditions other)
 		{
 			equivalents.add(other);
 		}
 
 		/**
 		 * @return Name of these groups
 		 */
 		public String getName()
 		{
 			return name;
 		}
 
 		/**
 		 * @return True if this collection represents other data.
 		 */
 		public boolean isOther()
 		{
 			return name.equals(SpecialNames.GROUP_OTHER);
 		}
 
 		/**
 		 * @return True if this collection represents excluded data.
 		 */
 		public boolean isExcluded()
 		{
 			return name.equals(SpecialNames.GROUP_EXCLUDED);
 		}
 
 		/**
 		 * @return Total count of all included groups
 		 */
 		public int getCount()
 		{
 			int total = 0;
 			for(Conditions parameter : equivalents)
 			{
 				total += parameter.getCount();
 			}
 			return total;
 		}
 
 		/**
 		 * @return Category count of all included groups (totalled)
 		 */
 		public int[] getCategoryCounts()
 		{
 			int[] categoryCounts = null;
 			for(Conditions parameter : equivalents)
 			{
 				if(categoryCounts == null)
 				{
 					categoryCounts = parameter.getCategoryCounts();
 				}
 				else
 				{
 					for(int i=0; i<categoryCounts.length; i++)
 					{
 						categoryCounts[i] += parameter.getCategoryCounts()[i];
 					}
 				}
 			}
 			return categoryCounts;
 		}
 
 		/**
 		 * Displays list of known agents (for test mode).
 		 */
 		public void displayAgents()
 		{
 			System.out.println("* " + getName());
 			HashSet<String> done = new HashSet<String>();
 			for(Conditions parameter : equivalents)
 			{
 				for(KnownAgent agent : parameter.getKnownAgents())
 				{
 					String value = agent.toString();
 					if(!done.contains(value))
 					{
 						done.add(value);
 						System.out.println("  " + agent);
 					}
 				}
 			}
 			System.out.println();
 		}
 	}
 
 	private static void countParameters(
 		Collection<Conditions> parameters,
 		String[] categories, KnownAgent[] knownAgents)
 	{
 		for(Conditions parameter : parameters)
 		{
 			parameter.prepare(categories);
 		}
 		for(KnownAgent knownAgent : knownAgents)
 		{
 			for(Conditions parameter : parameters)
 			{
 				if(parameter.match(knownAgent))
 				{
 					parameter.add(knownAgent);
 					break;
 				}
 			}
 		}
 	}
 }
