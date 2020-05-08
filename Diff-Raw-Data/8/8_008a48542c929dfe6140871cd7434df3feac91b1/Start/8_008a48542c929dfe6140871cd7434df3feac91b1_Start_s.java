 package de.ethalon.sudoku.impl;
 
 import java.awt.Point;
 import java.util.Scanner;
 
 import de.ethalon.sudoku.bean.PlayingField;
 
 /**
  * TODO tmy (23.01.2012): Insert javadoc for class de.ethalon.sudoku.impl.Start.java.
  * <p>
  * </p>
  * 
  * 
  * <br>
  * &nbsp;<br>
  * Licensed Materials - Property of <b>ETHALON GmbH</b> <br>
  * (c) Copyright ETHALON GmbH 2012. All rights reserved. <br>
  * Class Start.java created on 23.01.2012 14:20:27
  * 
  * <br>
  * &nbsp;<br>
  * <b>History:</b><br>
  * 
  * <br>
  * &nbsp;
  * 
  * @author ETHALON: tmy
  * 
  */
 
 public class Start {
 
 	public static void main(final String[] args) {
 
 		// 17 ist minimum fuer eindeutiges Sudoku-Feld
 		Scanner scanner = new Scanner(System.in);
 		System.out.println("Wie viele Zahlen sollen vorgegeben werden? (minimal 17 Zahlen!)");
 		System.out.print("Anzahl: ");
 		final int shownNumbersInBeginning = scanner.nextInt();
 
 		final PlayingLogicImpl impl = new PlayingLogicImpl();
 		PlayingField playingField = new PlayingField(shownNumbersInBeginning);
 
 		// make a copy of the array
 		final Integer[][] _rowsAndColumns = new Integer[playingField.getRowsAndColumns().length][playingField
 				.getRowsAndColumns().length];
 
 		while (!impl.initPlayingField(playingField)) {
 			playingField = new PlayingField(shownNumbersInBeginning);
 		}
 
 		for (int row = 0; row < playingField.getRowsAndColumns().length; row++) {
 			for (int col = 0; col < playingField.getRowsAndColumns().length; col++) {
 				_rowsAndColumns[row][col] = playingField.getRowsAndColumns()[row][col];
 			}
 		}
 		impl.solveSudoku(_rowsAndColumns);
 
 		Start.printPlayingField(playingField.getRowsAndColumns());
 
 		// Interaktionen mit dem Benutzer
 		System.out
 				.println("Mögliche Kommandos sind: \nclue (deckt eine Zahl auf)\ncheck (prüft die aktuelle Belegung)\nsolve (löst das Sudoku vollständig)\nexit (beendet das Programm)");
 		String input = "";
 
 		while (!input.equals("solve") && !input.equals("exit")) {
 			System.out.print("\nWelches Feld willst du füllen? (\"Zeile/Spalte\")");
 			System.out.print("\nKoordinaten: ");
 			scanner = new Scanner(System.in);
 			input = scanner.nextLine();
 
 			if (input.matches("^\\d/\\d$")) {
 				final Integer row = Integer.parseInt(input.substring(0, 1));
 				final Integer col = Integer.parseInt(input.substring(2, 3));
 
 				System.out.print("\nWelcher Wert soll an Punkt (" + row + "/" + col + ") eingetragen werden? (1-9)");
 				System.out.print("\nWert: ");
 				scanner = new Scanner(System.in);
 				input = scanner.nextLine();
 				final Integer value = Integer.parseInt(input);
 
 				if ((value >= 1) && (value <= 9)) {
 					playingField.getRowsAndColumns()[row][col] = value;
 
 				} else {
 					System.out.println("Fehler! Wert hat falsches Format! Erlaubt sind die Zahlen von 1 bis 9");
 				}
 
 				Start.printPlayingField(playingField.getRowsAndColumns());
 
 			} else if (input.equals("check")) {
 				if (impl.checkUserSolution(_rowsAndColumns, playingField.getRowsAndColumns())) {
 					System.out.println("Aktuelle Belegung ist korrekt!");
 				} else {
 					System.out.println("Aktuelle Belegung ist NICHT korrekt!");
 				}
 
 			} else if (input.equals("clue")) {
 				final Point givenPoint = impl.giveUserHelpWithOneNumber(_rowsAndColumns,
 						playingField.getRowsAndColumns());
 				Start.printPlayingField(playingField.getRowsAndColumns());
 				System.out.println("An der Koordinate (" + givenPoint.x + "/" + givenPoint.y
 						+ ") wurde eine Zahl aufgedeckt!");
 
 			} else if (input.equals("solve")) {
 				impl.solveSudoku(playingField.getRowsAndColumns());
 				Start.printPlayingField(playingField.getRowsAndColumns());
 
 			} else if (input.equals("exit")) {
 				// dont print the error message below, when user want to exit
 			} else {
 				System.out.println("Fehler! Wert hat falsches Format! Bsp: 5/2");
 			}
 
 		}
 
 		// Start.printPlayingField(_rowsAndColumns);
 
 		// System.out.println("\nErstellt nach -" + (System.currentTimeMillis() - timer) + "- ms");
 	}
 
 	/**
 	 * 
 	 * <p>
 	 * Prints the playing field.
 	 * </p>
 	 * 
 	 * @author ETHALON: tmy
 	 * 
 	 * @param rowsAndColumns
 	 */
 	public static void printPlayingField(final Integer[][] rowsAndColumns) {
 
 		System.out.print("       0   1   2    3   4   5    6   7   8  ");
 		for (int row = 0; row < rowsAndColumns.length; row++) {
 
 			if ((row == 0) || (row == 3) || (row == 6)) {
				System.out.print("\n    ========================================\n");
 			} else {
				System.out.print("\n    ----------------------------------------\n");
 			}
 			System.out.print(" " + row + " ");
 			for (int col = 0; col < rowsAndColumns.length; col++) {
 				if ((col == 0) || (col == 3) || (col == 6)) {
 					System.out.print(" || " + (rowsAndColumns[row][col] == null ? " " : rowsAndColumns[row][col]));
 				} else {
 					System.out.print(" | " + (rowsAndColumns[row][col] == null ? " " : rowsAndColumns[row][col]));
 				}
 
 			}
 			System.out.print(" || ");
 		}
		System.out.print("\n    ========================================\n");
 	}
 
 }
