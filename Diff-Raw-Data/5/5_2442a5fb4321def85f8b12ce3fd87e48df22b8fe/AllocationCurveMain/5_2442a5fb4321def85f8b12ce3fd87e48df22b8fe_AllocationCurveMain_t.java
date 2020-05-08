 /*
 	Copyright: Marcus Cobden (2011)
 	This file is part of AllocationCurve.
 
 	AllocationCurve is free software: you can redistribute it and/or modify
 	it under the terms of version 3 of the GNU Lesser General Public License
 	as published by the Free Software Foundation.
 
 	AllocationCurve is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 	GNU Lesser General Public License for more details.
 
 	You should have received a copy of the GNU Lesser General Public License
 	along with AllocationCurve. If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.co.marcuscobden.allocationcurve;
 
 import java.awt.Dimension;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import javax.swing.WindowConstants;
 
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.yaml.snakeyaml.Yaml;
 
 import uk.co.marcuscobden.allocationcurve.exception.AllocationDeclarationException;
 import uk.co.marcuscobden.allocationcurve.renderer.SVGAllocationRenderer;
 import uk.co.marcuscobden.allocationcurve.yaml.SubYAMLConstructor;
 
 public class AllocationCurveMain
 {
 
 	public static void main(final String[] args)
 	{
 		AllocationCurveOptions opts = new AllocationCurveOptions();
 
 		if (args.length == 0)
 		{
 			startGUI(opts);
 		}
 		else
 		{
 
 			CmdLineParser parser = new CmdLineParser(opts);
 			try
 			{
 				parser.parseArgument(args);
 			} catch (CmdLineException e)
 			{
 				System.err.println(e.getMessage());
 				parser.printUsage(System.err);
 				return;
 			}
 
 			if (opts.showGUI)
 			{
 				startGUI(opts);
 			}
 			else
 			{
 				InputStream input = null;
 				File workingDir;
 
 				if (opts.input == null)
 				{
 					input = System.in;
 					workingDir = new File(System.getProperty("user.dir"));
 				}
 				else
 				{
 					try
 					{
 						input = new FileInputStream(opts.input);
 					} catch (FileNotFoundException e)
 					{
 						System.err.println(e.getMessage());
 						System.exit(1);
 					}
 					workingDir = opts.input.getParentFile();
 				}
 
 				OutputStream output = null;
 				if (opts.output == null)
 					output = System.out;
 				else
 				{
 					try
 					{
 						output = new FileOutputStream(opts.output);
 					} catch (FileNotFoundException e)
 					{
 						System.err.println(e.getMessage());
 						System.exit(1);
 					}
 				}
 
 				AllocationRecord root = loadConfig(input, workingDir,
 						opts.depthLimit);
 
 				try
 				{
 					root.validate(opts.depthLimit);
 				} catch (AllocationDeclarationException e)
 				{
 					System.err
 							.println("Cannot render, declaration failed validation.");
 					System.err.println(e.getMessage());
 					System.exit(1);
 				}
 
 				render(output, root, opts.depthLimit);
 
 				try
 				{
 					output.close();
 				} catch (IOException e)
 				{
 					System.err.println("Error closing output stream");
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	protected static void startGUI(final AllocationCurveOptions opts)
 	{
 		AllocationCurveGUI gui = new AllocationCurveGUI(opts);
 
 		gui.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		gui.setVisible(true);
 	}
 
 	public static AllocationRecord loadConfig(final InputStream input,
			final File workingDir, int depthLimit)
 	{
 		Yaml yamlParser = new Yaml(new SubYAMLConstructor<AllocationRecord>(
 				AllocationRecord.class, workingDir, depthLimit));
 
 		return (AllocationRecord) yamlParser.load(input);
 	}
 
 	public static void render(final OutputStream output,
 			final AllocationRecord root, final int depthLimit)
 	{
 		new SVGAllocationRenderer(new Dimension(500, 500)).render(output, root,
				depthLimit);
 	}
 }
