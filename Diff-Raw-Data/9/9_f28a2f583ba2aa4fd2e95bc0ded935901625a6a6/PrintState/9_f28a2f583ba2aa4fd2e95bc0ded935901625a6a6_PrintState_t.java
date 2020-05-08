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
 package ditl.cli;
 
 import java.io.IOException;
 
 import org.apache.commons.cli.*;
 
 import ditl.*;
 import ditl.Store.NoSuchTraceException;
 
public class PrintState extends ReadOnlyApp {
 	
 	private double d_time;
 	private String trace_name;
 	
 	public final static String PKG_NAME = null;
 	public final static String CMD_NAME = "ps";
 	public final static String CMD_ALIAS = null;
 	
 	@Override
 	protected void parseArgs(CommandLine cli, String[] args) throws ParseException, HelpException {
 		super.parseArgs(cli, args);
 		trace_name = args[1];
 		d_time = Double.parseDouble(args[2]);
 	}
 
 	
 	@Override
 	protected void run() throws IOException, NoSuchTraceException {
 		Trace<?> trace = _store.getTrace(trace_name);
 		if ( ! trace.isStateful() )
 			System.out.println("Trace '"+trace_name+"' is not a stateful trace.");
 		StatefulTrace<?,?> strace = (StatefulTrace<?,?>)trace;
 		long time = (long)(d_time * strace.ticsPerSecond());
 		StatefulReader<?,?> reader = strace.getReader();
 		reader.seek(time);
 		for ( Object state : reader.referenceState() )
 			System.out.println(state);
 		reader.close();
 	}
 
 	@Override
 	protected String getUsageString(){
 		return "[OPTIONS] STORE TRACE_NAME TIME";
 	}
 	
 }
