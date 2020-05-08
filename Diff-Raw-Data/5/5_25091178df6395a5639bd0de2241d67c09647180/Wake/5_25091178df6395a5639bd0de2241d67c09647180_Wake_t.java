 package com.binarysprite.wake;
 
 import java.io.File;
 
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 
 /**
  * Wake（ウェイク）のメインクラスです。
  * 
  * @author Tabunoki
  * 
  */
 public class Wake {
 
 	/**
 	 * true の場合、デバックモードで動作します。
 	 */
 	@Option(name = "-d", usage = "debug options")
 	boolean isDebug;
 
 	/**
 	 * true の場合、一覧表示モードで動作します。
 	 */
 	@Option(name = "-l", usage = "dispray list")
 	boolean isList;
 
 	/**
 	 * インプットディレクトリです。
 	 */
	@Option(name="-i", usage="input directory", metaVar="INPUT", required=true)
     private File input = new File("./");
 	
 	/**
 	 * アウトプットディレクトリです。
 	 */
	@Option(name="-o", usage="output directory", metaVar="OUTPUT", required=true)
     private File output = new File("./");
 
 	/**
 	 * メインメソッドです。
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		new Wake().doMain(args);
 	}
 
 	/**
 	 * 
 	 */
 	public Wake() {
 		/*
 		 * do nothing.
 		 */
 	}
 
 	/**
 	 * 
 	 * @param args
 	 */
 	public void doMain(String[] args) {
 
 		/*
 		 * 引数解析
 		 */
 		CmdLineParser parser = new CmdLineParser(this);
 
 		parser.setUsageWidth(80);
 
 		try {
 			parser.parseArgument(args);
 
 			if (args.length == 0) {
 				System.out.println("No argument is given.");
 				parser.printUsage(System.out);
 				System.out.println();
 			}
 
 		} catch (CmdLineException e) {
 
 			System.err.println(e.getMessage());
 			System.err.println("java -jar wake.jar [options...] arguments...");
 			parser.printUsage(System.err);
 			System.err.println();
 			
 			return;
 		}
 		
 		/*
 		 * 実処理
 		 */
 		if (isList) {
 			WebBuilderMode.LIST.handle(WebBuilder.DIRECTORY, new WebBuilderParam(input, output));
 		} else {
 			WebBuilderMode.BUILD.handle(WebBuilder.DIRECTORY, new WebBuilderParam(input, output));
 		}
 	}
 }
