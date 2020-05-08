 /*******************************************************************************
  * This file is part of DITL.                                                  *
  *                                                                             *
  * Copyright (C) 2011 John Whitbeck <john@whitbeck.fr>                         *
  *                                                                             *
  * DITL is free software: you can redistribute it and/or modify                *
  * it under the terms of the GNU General Public License as published by        *
  * the Free Software Foundation, either version 3 of the License, or           *
  * (at your option) any later version.                                         *
  *                                                                             *
  * DITL is distributed in the hope that it will be useful,                     *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               *
  * GNU General Public License for more details.                                *
  *                                                                             *
  * You should have received a copy of the GNU General Public License           *
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.       *
  *******************************************************************************/
 package ditl.graphs.cli;
 
 import java.io.IOException;
 
 import org.apache.commons.cli.*;
 
 import ditl.Store.NoSuchTraceException;
 import ditl.cli.ExportApp;
 import ditl.graphs.*;
 
 
 public class ExportMovement extends ExportApp {
 
 	private Long maxTime;
 	private Long dtps;
 	private ExternalFormat ext_fmt = new ExternalFormat(ExternalFormat.NS2, ExternalFormat.ONE);
 	private GraphOptions graph_options = new GraphOptions(GraphOptions.MOVEMENT);
	private long interval;
 	
 	public final static String PKG_NAME = "graphs";
 	public final static String CMD_NAME = "export-movement";
 	public final static String CMD_ALIAS = "xm";
 
 	@Override
 	protected void parseArgs(CommandLine cli, String[] args) throws ParseException, HelpException {
 		super.parseArgs(cli, args);
 		graph_options.parse(cli);
 		ext_fmt.parse(cli);
 		if ( cli.hasOption(maxTimeOption) )
 			maxTime = Long.parseLong(cli.getOptionValue(maxTimeOption));
 		dtps = getTicsPerSecond( cli.getOptionValue(destTimeUnitOption,"s"));
 		if ( dtps == null )
 			throw new HelpException();
		interval = (long)Double.parseDouble(cli.getOptionValue(intervalOption,"1"));
 	}
 	
 	@Override
 	protected void initOptions() {
 		super.initOptions();
 		graph_options.setOptions(options);
 		ext_fmt.setOptions(options);
 		options.addOption(null, maxTimeOption, true, "maximum movement time (for ONE only)");
 		options.addOption(null, destTimeUnitOption, true, "time unit of destination trace [s, ms, us, ns] (default: s)");
 		options.addOption(null, intervalOption, true, "interval (for ONE only)");
 	}
 	
 	@Override
 	protected void run() throws IOException, NoSuchTraceException {
 		MovementTrace movement = (MovementTrace) _store.getTrace(graph_options.get(GraphOptions.MOVEMENT));
 		long otps = movement.ticsPerSecond();
		interval *= otps;
 		if ( maxTime != null ) maxTime *= otps;
 		double timeMul = getTimeMul(otps,dtps);
 		if ( ext_fmt.is(ExternalFormat.NS2) )
 			NS2Movement.toNS2(movement, _out, timeMul);
 		else
 			ONEMovement.toONE(movement, _out, timeMul, interval, maxTime);
 	}
 }
