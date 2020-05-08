 package org.lite.js;
 import com.yahoo.platform.yui.compressor.*;
 
 import jargs.gnu.CmdLineParser;
 
 import java.io.*;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 
 public class LiteCompressor {
 	private static String charset;
 	private static String iext;
 	private static String oext;
 	
 	public static void main(String args[]){
 		CmdLineParser parser = new CmdLineParser();
 		CmdLineParser.Option typeOpt = parser.addStringOption('t',"type");
 		CmdLineParser.Option codeBaseOpt = parser.addStringOption('b',"codebase");
 		CmdLineParser.Option outputOpt = parser.addStringOption('o', "output");
 		CmdLineParser.Option inputOpt = parser.addStringOption('i', "input");
 		CmdLineParser.Option charsetOpt = parser.addStringOption("charset");
 		CmdLineParser.Option iextOpt = parser.addStringOption("iext");
 		CmdLineParser.Option oextOpt = parser.addStringOption("oext");
 		CmdLineParser.Option nomungeOpt = parser.addBooleanOption("nomunge");
 		CmdLineParser.Option preserveSemiOpt = parser.addBooleanOption("preserve-semi");
 		CmdLineParser.Option disableOptimizationsOpt = parser.addBooleanOption("disable-optimizations");
 		CmdLineParser.Option linebreakOpt = parser.addIntegerOption("line-break");
 		
 		CmdLineParser.Option helpOpt = parser.addBooleanOption('h', "help");
 		// parse value from commandLine
 		try{
 			parser.parse(args);
 		}catch(Exception e){
 			System.out.println("[ERROR] something wrong with params!");
 			help();
 			System.exit(1);
 		}
 		/**
 		 * type js | css
 		 */
 		String _type 					= (String)parser.getOptionValue(typeOpt);
 		String _codebase				= (String)parser.getOptionValue(codeBaseOpt);
 		String _in 						= (String)parser.getOptionValue(inputOpt);
 		String _out 					= (String)parser.getOptionValue(outputOpt);
 		String _charset					= (String)parser.getOptionValue(charsetOpt);
 		String _iext					= (String)parser.getOptionValue(iextOpt);
 		String _oext					= (String)parser.getOptionValue(oextOpt);
 		boolean _nomunge				= parser.getOptionValue(nomungeOpt) == null;
 		boolean _preserveAllSemiColons	= parser.getOptionValue(preserveSemiOpt) != null;
 		boolean _disableOptimizations	= parser.getOptionValue(disableOptimizationsOpt) != null;
 		boolean _help					= parser.getOptionValue(helpOpt) != null;
 		String _lb						= (String)parser.getOptionValue(linebreakOpt);
 		boolean _verbose = false;
 		
 		int _linebreak = -1;
 		String regEx="\\.\\w+$";
 		
 		
 		// check params
 		if( _type == null ){
 			_type = "js";
 		}
 		if( _in == null || _codebase == null || _help){
 			help();
 			System.exit(1);
 		}
 		if(!_codebase.matches("\\\\|/$")){
 			_codebase += "/";
 		}
 		if(_iext == null){
 			if(_type == "js")
 				_iext = "js";
 			else
 				_iext = "css";
 		}
 		if(_oext == null){
 			if(_type == "js")
 				_oext = "min.js";
 			else
 				_oext = "min.css";
 		}
 		
 		if(_out == null){
 			_out = _in.replaceFirst(regEx, "."+_oext);
 		}
 		if(_charset == null){
 			_charset = "utf-8";
 		}else{
 			if(!Charset.isSupported(_charset)){
 				System.out.println("[ERROR] unsupported charset:"+_charset);
 				System.exit(1);
 			}
 		}
 		if(_lb != null){
 			try{
 				_linebreak = Integer.parseInt(_lb, 10);
 			}catch(NumberFormatException e){
 				help();
 				System.exit(1);
 			}
 		}
 				
     	charset = _charset;
     	iext = _iext;
     	oext = _oext;
 		
     	MergerCompressor.config(_codebase,_charset,_linebreak, _nomunge, _verbose, _preserveAllSemiColons, _disableOptimizations);
     	 
     	//System.out.println(input);
     	File in_dir = new File(_in);
 		File out_dir = new File(_out);
 		
 		_in = in_dir.getAbsolutePath();
 		_out = out_dir.getAbsolutePath();
 		
 		if(in_dir.isDirectory()){			// dir file
 			compressDir(_in, _out,_type);
 		}else{							// single file
 			System.out.println( "[Compress File]" + _in.substring(_codebase.length()));
 			compressFile(_in,_out,_type);
 			System.out.println("[Done] ok!!!");
 		}
 	}
 	public static void compressFile(String in,String out,String type){
 		try{
 			Reader inputFile = new InputStreamReader(new FileInputStream(in), charset);
 			Writer outputFile = new OutputStreamWriter(new FileOutputStream(out), charset);
 			if(type.equalsIgnoreCase("js")){
 				MergerCompressor compressor = new MergerCompressor(true);
 				
 				compressor.loadScript(inputFile,new LiteReporter());
 				compressor.compress(outputFile);
 			}else{
 				CssCompressor compressor = new CssCompressor(inputFile);
 				compressor.compress(outputFile,-1);
 			}
 			inputFile.close();
 			outputFile.close();
 		}catch(Exception e){
 			System.out.println("compressor error!!!");
 		}
 	}
 	public static void compressDir(String input,String output,String type){
 		File out_dir = new File(output);
 		ArrayList<String> files = new ArrayList<String>();
 		getFileListByDir(input,files);
 		
 		// check output dir
 		if(!out_dir.exists()){
 			if(!out_dir.mkdirs()){
 				System.out.println("make output folder fail, please check pomission!");
 				return;
 			}
 		}
 		int i = 0;
 		int in_len = input.length();
 		
 		for(String fname : files){
 			i ++;
 			String relative_file = fname.substring(in_len);
 			System.out.println( i+"\t[Compress File]" + fname.substring(in_len));
 			String _output = output + relative_file.replaceFirst(iext+"$", "") + "." + oext ;
 			File check_file = new File(new File(_output).getParent());
 			if(!check_file.exists()){
 				if(!check_file.mkdirs()){
 					System.out.println("make output folder fail, please check pomission!");
 					return;
 				}
 			}
 			System.err.println("-->"+fname+"<--");
 			compressFile(fname,_output,type);
 			System.err.println("--EOF--");
			if(LiteReporter.errorCount > 0){
				System.exit(1);
				break;
			}
 		}	
 	}
 	private static  void getFileListByDir(String tar,ArrayList<String> files){
 		File dir = new File(tar);
 		File[] ffs = dir.listFiles();
 		String n;
 		int flag;
 		for(File file:ffs){
 			n = file.getName();
 			if(file.isDirectory()){
 				if(n.indexOf('.') == 0) continue;
 				getFileListByDir(file.getAbsolutePath() , files);
 			}else{
 				flag = n.lastIndexOf(iext);
 				if(flag != -1 && (flag == (n.length() - iext.length())))
 					files.add(file.getPath());
 			}
 		}
 	}
 	public static void help(){
 		 System.out.println(
 	                "\nUsage: java -jar LiteCompressor.jar [options] [input file]\n\n"
 				 
 	                        + "Global Options\n"
 	                        + "  -b, --codebase            !		codebase path\n"
 	                        + "  -i, --input               !		input file path\n"
 	                        + "  -o, --output              [in+oext]output file path\n"
	                        + "  --iext, --input_sfx       [js|css]	input file suffix\n"
	                        + "  --oext, --output_sfx      [min.js|min.css]	output file suffix\n"
 	                        + "  -t ,--type <js|css>       [js]		Specifies the type of the input file\n"
 	                        + "  --charset <charset>       [utf-8]	Read the input file using <charset>\n"
 	                        + "  --line-break <column>     [-1,nobreak]Insert a line break after the specified column number\n\n"
 
 	                        + "JavaScript Options\n"
 	                        + "  --nomunge                 Minify only, do not obfuscate\n"
 	                        + "  --preserve-semi           Preserve all semicolons\n"
 	                        + "  --disable-optimizations   Disable all micro optimizations\n\n"
 	                        
 	                        + "  -h, --help                Displays this information\n"
 			);
 	}
 }
