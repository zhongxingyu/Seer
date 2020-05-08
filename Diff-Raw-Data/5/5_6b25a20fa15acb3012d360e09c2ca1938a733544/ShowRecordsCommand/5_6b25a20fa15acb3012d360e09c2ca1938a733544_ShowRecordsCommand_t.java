 package com.codiform.as400shell.command;
 
 import java.beans.PropertyVetoException;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 import javanet.staxutils.IndentingXMLStreamWriter;
 
 import javax.xml.stream.FactoryConfigurationError;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import joptsimple.OptionParser;
 import joptsimple.OptionSet;
 import joptsimple.OptionSpec;
 
 import com.codiform.as400shell.model.FileType;
 import com.codiform.as400shell.model.Library;
 import com.codiform.as400shell.model.LibraryFile;
 import com.codiform.as400shell.model.LibraryFileMember;
 import com.codiform.as400shell.shell.ShellContext;
 import com.ibm.as400.access.AS400Exception;
 import com.ibm.as400.access.AS400File;
 import com.ibm.as400.access.AS400SecurityException;
 import com.ibm.as400.access.IFSFile;
 import com.ibm.as400.access.Record;
 import com.ibm.as400.access.RecordFormat;
 import com.ibm.as400.access.SequentialFile;
 
 public class ShowRecordsCommand extends ParsedArgumentCommand {
 
 	private OptionSpec<Boolean> logical, physical;
 	private OptionSpec<String> output;
 
 	public ShowRecordsCommand() {
 		setParser( new OptionParser() {
 			{
 				logical = accepts(
 						"logical",
 						"Include records from logical files." ).withOptionalArg().ofType(
 						Boolean.class ).describedAs( "include" ).defaultsTo(
 						true );
 				physical = accepts(
 						"physical",
 						"Include records from physical files." ).withOptionalArg().ofType(
 						Boolean.class ).describedAs( "include" ).defaultsTo(
 						true );
 				output = accepts( "output", "Save the output to a file." ).withRequiredArg().ofType(
 						String.class ).describedAs(
 						"filename" );
 			}
 		} );
 	}
 
 	@Override
 	public void displayHelp(ShellContext context) {
 		try {
 			context.out().println(
 					"SYNTAX: showSchema [options] <Library Name>\nDisplays the records in files in the specified library; by default shows only physical files.\n" );
 			parser.printHelpOn( context.out() );
 		} catch( IOException exception ) {
 			exception.printStackTrace( context.err() );
 		}
 	}
 
 	@Override
 	public void execute(ShellContext context, OptionSet options) {
 		if( options.nonOptionArguments().size() != 1 ) {
 			context.err().println( "No library name specified." );
 			displayHelp( context );
 			return;
 		}
 
 		try {
 			if( redirectOutput( context, options ) ) {
 				show( context, options );
 				context.reset();
 			}
 		} catch( Exception exception ) {
 			exception.printStackTrace( context.err() );
 		}
 	}
 
 	private boolean redirectOutput(ShellContext context, OptionSet options) {
 		if( !options.has( output ) ) {
 			return true;
 		}
 
 		File file = new File( options.valueOf( output ) );
 		if( file.exists() ) {
 			context.err().println( "File already exists: " + output );
 			return false;
 		}
 
 		try {
 			context.redirectTo( file );
 		} catch( FileNotFoundException e ) {
 			e.printStackTrace( context.err() );
 		}
 		return true;
 	}
 
 	public void show(ShellContext context, OptionSet options)
 			throws IOException,
 			AS400SecurityException, XMLStreamException,
 			FactoryConfigurationError, AS400Exception, InterruptedException,
 			PropertyVetoException {
 		String libraryName = options.nonOptionArguments().get( 0 );
 		IFSFile file = new IFSFile( context.getServer(), "/QSYS.LIB/"
 				+ libraryName + ".LIB" );
 		if( file.exists() ) {
 			show( context, options, libraryName, file );
 		} else {
 			context.err().printf( "Library %s cannot be found at: %s\n",
 					libraryName, file.getAbsolutePath() );
 		}
 	}
 
 	private void show(ShellContext context, OptionSet options,
 			String libraryName, IFSFile file)
 			throws IOException,
 			AS400SecurityException, XMLStreamException,
 			FactoryConfigurationError, AS400Exception, InterruptedException,
 			PropertyVetoException {
 		XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(
 				context.out() );
 		try {
 			showRecords( context, options, new Library( libraryName, file ),
 					new IndentingXMLStreamWriter( writer ) );
 		} finally {
 			writer.close();
 		}
 	}
 
 	private void showRecords(ShellContext content, OptionSet options,
 			Library library, IndentingXMLStreamWriter writer)
 			throws XMLStreamException, AS400Exception, IOException,
 			AS400SecurityException, InterruptedException, PropertyVetoException {
 
 		writer.writeStartDocument();
 		writer.writeStartElement( "library" );
 		writer.writeAttribute( "name", library.getName() );
 
 		for( LibraryFile file : library.getFiles() ) {
 			logLibraryFile( options, writer, file );
 		}
 
 		writer.writeEndElement();
 		writer.writeEndDocument();
 	}
 
 	private void logLibraryFile(OptionSet options,
 			IndentingXMLStreamWriter writer,
 			LibraryFile file) throws XMLStreamException, IOException,
 			AS400SecurityException,
 			AS400Exception, InterruptedException, PropertyVetoException {
 
 		if( file.getType() == FileType.LOGICAL
				&& !showFiles( options, logical, false ) )
 			return;
 		if( file.getType() == FileType.PHYSICAL
				&& !showFiles( options, physical, true ) )
 			return;
 
 		writer.writeStartElement( "file" );
 		writer.writeAttribute( "name", file.getName() );
 		writer.writeAttribute( "type", file.getType().toString() );
 		writer.writeAttribute( "members",
 				String.valueOf( file.getMembers().size() ) );
 
 		for( LibraryFileMember member : file.getMembers() ) {
 			logLibraryFileMember( writer, member );
 		}
 
 		writer.writeEndElement();
 	}
 
 	private boolean showFiles(OptionSet options, OptionSpec<Boolean> option, boolean defaultValue) {
 		if( options.has( option ) ) {
 			return options.valueOf( option );
 		} else {
 			return defaultValue;
 		}
 	}
 
 	private void logLibraryFileMember(IndentingXMLStreamWriter writer,
 			LibraryFileMember member)
 			throws XMLStreamException,
 			IOException, AS400SecurityException, AS400Exception,
 			InterruptedException, PropertyVetoException {
 		writer.writeStartElement( "member" );
 
 		writer.writeAttribute( "name", member.getName() );
 
 		RecordFormat[] formats = member.getRecordFormats();
 		if( formats.length > 0 )
 			logRecords( writer, formats[0], member.getSequentialFile() );
 
 		writer.writeEndElement();
 	}
 
 	private void logRecords(IndentingXMLStreamWriter writer,
 			RecordFormat recordFormat, SequentialFile sequentialFile)
 			throws PropertyVetoException, XMLStreamException, AS400Exception,
 			AS400SecurityException, InterruptedException, IOException {
 		Record item = null;
 		sequentialFile.setRecordFormat( recordFormat );
 		writer.writeStartElement( "records" );
 		sequentialFile.open( AS400File.READ_ONLY, 1,
 				AS400File.COMMIT_LOCK_LEVEL_DEFAULT );
 		try {
 			while( (item = sequentialFile.readNext()) != null ) {
 				logRecord( writer, sequentialFile, item, recordFormat );
 			}
 		} finally {
 			writer.writeEndElement();
 			sequentialFile.close();
 		}
 	}
 
 	private void logRecord(IndentingXMLStreamWriter writer,
 			SequentialFile sequentialFile, Record record, RecordFormat format)
 			throws XMLStreamException {
 		writer.writeStartElement( "record" );
 		writer.writeAttribute( "recordNumber",
 				String.valueOf( record.getRecordNumber() ) );
 		writer.writeAttribute( "recordName", record.getRecordName() );
 		writer.writeAttribute( "fileName", sequentialFile.getFileName() );
 		writer.writeAttribute( "memberName", sequentialFile.getMemberName() );
 		writer.writeAttribute( "fields",
 				String.valueOf( record.getNumberOfFields() ) );
 		for( String fieldName : format.getFieldNames() ) {
 			writer.writeAttribute( fieldName, getValue( record, fieldName ) );
 		}
 		writer.writeEndElement();
 	}
 
 	private String getValue(Record record, String fieldName) {
 		try {
 			return String.valueOf( record.getField( fieldName ) );
 		} catch( NumberFormatException exception ) {
 			return "nfe: " + exception.getMessage();
 		} catch( UnsupportedEncodingException exception ) {
 			return "unsupported-encoding: " + exception.getMessage();
 		}
 	}
 
 }
