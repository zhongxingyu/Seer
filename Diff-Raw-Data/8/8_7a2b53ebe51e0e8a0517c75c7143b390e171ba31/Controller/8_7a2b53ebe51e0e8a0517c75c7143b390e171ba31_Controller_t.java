 package gui;
 
 /* ******************************************************
  * * Copyright (C) 2013 xinouch** This file is part of L-Systems** L-Systems is free software: you can redistribute it and/or modify* it
  * under the terms of the GNU General Public License as published by* the Free Software Foundation, either version 3 of the License, or* (at
  * your option) any later version.** This program is distributed in the hope that it will be useful,* but WITHOUT ANY WARRANTY; without even
  * the implied warranty of* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the* GNU General Public License for more details.** You
  * should have received a copy of the GNU General Public License* along with this program. If not, see <http://www.gnu.org/licenses/>.
  * *****************************************************
  */
 /* ******************************************************
  * *
  * * Project: L-Systems* File: Controller.java** Created on: 8 mars 2013* Author: xinouch*
  * *****************************************************
  */
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.concurrent.Callable;
 
 import javax.swing.SwingUtilities;
 
 import parser.Generator;
 import parser.Grammar;
 import parser.IOmanager.Analyzer;
 import parser.IOmanager.BadFileException;
 import parser.IOmanager.ParseException;
 import ATuin.BadInterpretationException;
 import ATuin.Drawer;
 import ATuin.TubeTurtle;
 import ATuin.Turtle;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.math.ColorRGBA;
 import com.jme3.system.AppSettings;
 
 
 /**
  * Contains the controller of the application that make the link between the parser and the gui.
  * 
  * @author xinouch
  */
 public class Controller
 {
 	/** The list of grammar launched in the program */
 	private ArrayList<Grammar> grammars = new ArrayList<Grammar>();
 	/** The list of the interpretations launched in the program */
 	private ArrayList<Turtle> turtles = new ArrayList<Turtle>();
 	/** The generator for the current grammar */
 	private Generator generator = null;
 	/** index of the current grammar that we're working on */
 	private int indexOfCurrentGrammar = -1;
 	/** index of the current interpretation that we're working on */
 	private int indexOfCurrentTurtle = -1;
 	/** The main window */
 	private MainFrame mainFrame;
 	/** The panel containing the 3D application */
 	private Drawer application3d;
 	/** Size of the panel where the 3D appears (width) */
 	public int canvasJMEWidth = 1024;
 	/** Size of the panel where the 3D appears (height) */
 	public int canvasJMEHeight = 768;
 
 	/**
 	 * Constructor
 	 */
 	public Controller ()
 	{
 	}
 
 	/**
 	 * Create the window of the application and the 3D panel
 	 */
 	public void startLSystem ()
 	{
 		// gestion des objets 3D
 		AppSettings settings = new AppSettings(true);
 		settings.setWidth(canvasJMEWidth);
 		settings.setHeight(canvasJMEHeight);
 		application3d = new Drawer(settings);
 		final Panel3D pan = new Panel3D(application3d, settings);
 
 		final Controller me = this;
 		SwingUtilities.invokeLater(new Runnable()
 		{
 			public void run ()
 			{
 				try
 				{
 					mainFrame = new MainFrame(me, pan);
 					start3dApp();
 					mainFrame.setVisible(true);
 				}
 				catch (Exception e)
 				{
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Open and load a file containing grammars.
 	 * 
 	 * @param path the path to the file
 	 * @throws ParseException if the file is corrected written
 	 * @throws BadFileException if the file is corrected written
 	 */
 	public void loadFile (String path) throws ParseException, BadFileException
 	{
 		InputStream istrm = null;
 		try
 		{
 			istrm = new FileInputStream(path);
 			Analyzer analyzer = new Analyzer(istrm, "UTF-8");
 			try
 			{
 				analyzer.startValidation();
 
 				grammars.clear();
 				grammars = analyzer.getGrammars();
 				ArrayList<String> grs = new ArrayList<String>();
 				for (Grammar g : grammars)
 					grs.add(g.getName());
 				mainFrame.setListGrammars(grs);
 			}
 			catch (NumberFormatException e)
 			{
 				System.out.println(e.getMessage());
 				e.printStackTrace();
 			}
 		}
 		catch (FileNotFoundException e)
 		{
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Launch the grammar generator, and then call the turtle to draw.
 	 * 
 	 * @param nbIterations the number of iterations to make in the generetor
 	 * @throws BadInterpretationException
 	 */
 	public void launchTurtle (int nbIterations) throws BadInterpretationException
 	{
		generator = null;
 		// on génère les symboles
 		generator = new Generator(grammars.get(indexOfCurrentGrammar));
 		generator.setTotalIteration(nbIterations);
 		generator.generate();
 
 		System.out.println(generator.getGenerated());
 
 		// on donne la salade à la tortue
 		Turtle turtle = turtles.get(indexOfCurrentTurtle);
 		turtle.setSymbols(generator.getGenerated());
 		((TubeTurtle) turtle).setParameters(5, 10, 90, ColorRGBA.Green);
 		turtle.drawSymbols();
 	}
 
 	/**
 	 * @return the grammars
 	 */
 	public ArrayList<Grammar> getGrammars ()
 	{
 		return grammars;
 	}
 
 	/**
 	 * @param grammars the grammars to set
 	 */
 	public void setGrammars (ArrayList<Grammar> grammars)
 	{
 		this.grammars = grammars;
 	}
 
 	/**
 	 * @return the generator
 	 */
 	public Generator getGenerator ()
 	{
 		return generator;
 	}
 
 	/**
 	 * @param generator the generator to set
 	 */
 	public void setGenerator (Generator generator)
 	{
 		this.generator = generator;
 	}
 
 	/**
 	 * @return the indexOfCurrentGrammar
 	 */
 	public int getIndexOfCurrentGrammar ()
 	{
 		return indexOfCurrentGrammar;
 	}
 
 	/**
 	 * change the index and set the available grammars in the window.
 	 * 
 	 * @param indexOfCurrentGrammar the indexOfCurrentGrammar to set
 	 */
 	public void setIndexOfCurrentGrammar (int indexOfCurrentGrammar)
 	{
 		this.indexOfCurrentGrammar = indexOfCurrentGrammar;
 		chooseInterpretations();
 	}
 
 	/**
 	 * @return the current grammar
 	 */
 	public Grammar getCurrentGrammar ()
 	{
 		return grammars.get(indexOfCurrentGrammar);
 	}
 
 	/**
 	 * @return the indexOfCurrentTurtle
 	 */
 	public int getIndexOfCurrentTurtle ()
 	{
 		return indexOfCurrentTurtle;
 	}
 
 	/**
 	 * @param indexOfCurrentTurtle the indexOfCurrentTurtle to set
 	 */
 	public void setIndexOfCurrentTurtle (int indexOfCurrentTurtle)
 	{
 		this.indexOfCurrentTurtle = indexOfCurrentTurtle;
 	}
 
 	/**
 	 * start the 3D application in the canvas
 	 */
 	private void start3dApp ()
 	{
 		application3d.startCanvas();
 		application3d.enqueue(new Callable<Void>()
 		{
 			public Void call ()
 			{
 				if (application3d instanceof SimpleApplication)
 				{
 					SimpleApplication simpleApp = (SimpleApplication) application3d;
 					simpleApp.getFlyByCamera().setDragToRotate(true);
 				}
 				return null;
 			}
 		});
 	}
 
 	/**
 	 * Choose the interpretations compatible with the current grammar and put it in the window.
 	 */
 	private void chooseInterpretations ()
 	{
 		if (indexOfCurrentGrammar >= 0 && !grammars.isEmpty())
 		{
 			ArrayList<String> its = new ArrayList<String>();
 			Turtle turtle;
 			turtles.clear();
 			if (TubeTurtle.checkSymbols(grammars.get(indexOfCurrentGrammar).getUsableSymbolsWithoutNull()))
 			{
 				turtle = new TubeTurtle(application3d);
 				turtles.add(turtle);
 				its.add(turtle.getName());
 			}
 			mainFrame.setListInterpretations(its);
 		}
 	}
 }
