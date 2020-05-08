 /*
  * #%L
  * SCIFIO support for the OME data model (OME-XML and OME-TIFF).
  * %%
  * Copyright (C) 2013 - 2014 Open Microscopy Environment:
  *   - Massachusetts Institute of Technology
  *   - National Institutes of Health
  *   - University of Dundee
  *   - Board of Regents of the University of Wisconsin-Madison
  *   - Glencoe Software, Inc.
  * %%
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  * #L%
  */
 
 package io.scif.ome.commands;
 
 import io.scif.FormatException;
 import io.scif.Metadata;
 import io.scif.SCIFIO;
 import io.scif.config.SCIFIOConfig;
 import io.scif.filters.PlaneSeparator;
 import io.scif.filters.ReaderFilter;
 import io.scif.ome.OMEMetadata;
 import io.scif.services.InitializeService;
 import io.scif.tools.AbstractSCIFIOToolCommand;
 import io.scif.tools.SCIFIOToolCommand;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import loci.common.xml.XMLTools;
 import net.imglib2.meta.Axes;
 import net.imglib2.meta.AxisType;
 import net.imglib2.meta.CalibratedAxis;
 
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineException;
 import org.scijava.plugin.Parameter;
 import org.scijava.plugin.Plugin;
 
 /**
  * {@link SCIFIOToolCommand} plugin for printing OME-XML.
  * 
  * @author Mark Hiner
  */
 @Plugin(type = SCIFIOToolCommand.class)
 public class OMEXMLToolCommand extends AbstractSCIFIOToolCommand {
 
 	// -- Fields --
 
 	@Parameter
 	private InitializeService initializeService;
 
 	// -- Arguments --
 
 	@Argument(metaVar = "file", index = 0, usage = "image dataset to parse")
 	private String file;
 
 	@Argument(metaVar = "indent", index = 1, required = false,
 		usage = "indentation for xml nesting")
 	private final Integer xmlIndent = 3;
 
 	@Argument(index = 2, multiValued = true)
 	private final List<String> arguments = new ArrayList<String>();
 
 	// -- AbstractSCIFIOToolCommand API --
 
 	@Override
 	protected void run() throws CmdLineException {
 		try {
 			// OMEXML uses a fixed-5D XY[ZCT] data model. Thus we need to adjust the
 			// axis order to best match the OME model.
 			final ReaderFilter reader =
 				initializeService.initializeReader(file, new SCIFIOConfig()
 					.checkerSetOpen(true));
 			reader.enable(PlaneSeparator.class).separate(axesToSplit(reader));
 			final Metadata meta = reader.getMetadata();
 
 			// Print the metadata
 			printOMEXML(meta);
 		}
 		catch (final FormatException e) {
 			throw new CmdLineException(null, e.getMessage());
 		}
 		catch (final IOException e) {
 			throw new CmdLineException(null, e.getMessage());
 		}
 	}
 
 	@Override
 	protected String description() {
 		return "command line tool for printing OME-XML from a dataset";
 	}
 
 	@Override
 	protected String getName() {
 		return "omexml";
 	}
 
 	@Override
 	protected List<String> getExtraArguments() {
 		return arguments;
 	}
 
 	@Override
 	protected void validateParams() throws CmdLineException {
 		if (file == null) {
 			throw new CmdLineException(null, "Argument \"file\" is required");
 		}
 	}
 
 	// -- Helper methods --
 
 	/**
 	 * Translates the given metadata to {@link OMEMetadata} and logs the XML.
 	 */
 	private void printOMEXML(final Metadata meta) {
 		final SCIFIO scifio = new SCIFIO(meta.getContext());
 		final OMEMetadata omexml = new OMEMetadata(scifio.getContext());
 		scifio.translator().translate(meta, omexml, false);
 		log().info(XMLTools.indentXML(omexml.getRoot().dumpXML(), xmlIndent, true));
 	}
 
 	/**
	 * Generates a list of axes using the given Reader, such that {@link Axes.X}
	 * and {@link Axes.Y} are first.
 	 */
 	private AxisType[] axesToSplit(final ReaderFilter r) {
 		final Set<AxisType> axes = new HashSet<AxisType>();
 		final Metadata meta = r.getTail().getMetadata();
 		// Split any non-X,Y axis
 		for (final CalibratedAxis t : meta.get(0).getAxesPlanar()) {
 			final AxisType type = t.type();
 			if (!(type == Axes.X || type == Axes.Y)) {
 				axes.add(type);
 			}
 		}
 		return axes.toArray(new AxisType[axes.size()]);
 	}
 }
