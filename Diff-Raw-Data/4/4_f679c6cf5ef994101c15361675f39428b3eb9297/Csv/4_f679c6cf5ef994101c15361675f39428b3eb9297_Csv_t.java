 package dados;
 
 import gui.*;
 
 import java.awt.*;
 import java.io.*;
 import java.util.*;
 
 import javax.accessibility.*;
 import javax.swing.*;
 import javax.swing.filechooser.FileFilter;
 
 import jogosolimpicos.*;
 import listaligada.*;
 
 /**
  * 
  * Main class for the CSV imports and exports
  */
 @SuppressWarnings("serial")
 public class Csv extends JComponent implements Accessible {
 	/**
 	 * Method to add various csv files to the program. The application will
 	 * evaluate the content of the csv and will choose the csv file type.
 	 * 
 	 * * @param files if wanna choose the file in this method, send null.
 	 * 
 	 * @param janela
 	 *            the parent component of the dialog
 	 * @param paises
 	 *            linked list with the countries
 	 * @param disciplinas
 	 *            linked list with the competitions
 	 * @param modalidades
 	 *            linked list with the sports
 	 * @param jogos
 	 *            linked list of events
 	 * @param provas
 	 *            linked list with the competitions with event
 	 * @param equipas
 	 *            linked list with the teams
 	 * @param atletas
 	 *            linked list with the athletes
 	 * @see Pais country details
 	 * @see Disciplina competition details
 	 * @see Modalidade sport details
 	 * @see JogosOlimpicos event details
 	 * @see Prova competition with event details
 	 * @see Equipa team details
 	 * @see Atleta athlete details
 	 * 
 	 */
 	public void intelImport(File[] files, Component janela, ListaLigada<Pais> paises, ListaLigada<Disciplina> disciplinas, ListaLigada<Modalidade> modalidades, ListaLigada<JogosOlimpicos> jogos, ListaLigada<Prova> provas, ListaLigada<Equipa> equipas, ListaLigada<Atleta> atletas) {
 
 		if (files == null) {
 			JFileChooser fc = new JFileChooser();
 			fc.setFileFilter(new CsvFilter());
 			fc.setMultiSelectionEnabled(true);
 			fc.setDialogTitle("intelImport Files");
 			int returnVal = fc.showOpenDialog(janela);
 			if (returnVal != JFileChooser.APPROVE_OPTION)
 				return;
 			files = fc.getSelectedFiles();
 		}
 
 		ListaLigada<File> ficheirosPais = new ListaLigada<File>();
 		ListaLigada<File> ficheirosDisc = new ListaLigada<File>();
 		ListaLigada<File> ficheirosProva = new ListaLigada<File>();
 		ListaLigada<File> ficheirosResul = new ListaLigada<File>();
 		ListaLigada<File> ficheirosTemp = new ListaLigada<File>();
 		ListaLigada<Boolean> testes = new ListaLigada<Boolean>();
 		for (int i = 0; i < files.length; i++) {
 			if (files[i].getName().endsWith(".csv"))
 				ficheirosTemp.add(files[i]);
 		}
 
 		for (int i = 0; i < ficheirosTemp.size(); i++) {
 			if (testPais(ficheirosTemp.get(i))) {
 				ficheirosPais.add(ficheirosTemp.get(i));
 			} else if (testDisc(ficheirosTemp.get(i))) {
 				ficheirosDisc.add(ficheirosTemp.get(i));
 			} else if (testProva(ficheirosTemp.get(i))) {
 				ficheirosProva.add(ficheirosTemp.get(i));
 			} else if (testResul(ficheirosTemp.get(i))) {
 				ficheirosResul.add(ficheirosTemp.get(i));
 			}
 		}
 
 		for (int i = 0; i < ficheirosPais.size(); i++) {
 			testes.add(importPais(ficheirosPais.get(i), janela, paises));
 		}
 		for (int i = 0; i < ficheirosDisc.size(); i++) {
 			testes.add(importDisc(ficheirosDisc.get(i), janela, disciplinas, modalidades));
 		}
 		for (int i = 0; i < ficheirosProva.size(); i++) {
 			testes.add(importProvas(ficheirosProva.get(i), janela, jogos, provas, disciplinas, modalidades));
 		}
 		for (int i = 0; i < ficheirosResul.size(); i++) {
 			testes.add(importResultados(ficheirosResul.get(i), janela, atletas, modalidades, paises, provas, equipas, jogos));
 		}
 
 		int cont = 0;
 		for (int i = 0; i < testes.size(); i++) {
 			if (testes.get(i))
 				cont++;
 		}
 
 		if (cont == ficheirosTemp.size() && ficheirosTemp.size() != 0) {
 			JOptionPane.showMessageDialog(janela, "Files imported sucessfully!", "intelImport File", JOptionPane.INFORMATION_MESSAGE);
 		} else if (cont == 0) {
 			JOptionPane.showMessageDialog(janela, "File imported failed!", "intelImport File", JOptionPane.ERROR_MESSAGE);
 		} else {
 			JOptionPane.showMessageDialog(janela, "Files imported with errors!", "intelImport File", JOptionPane.INFORMATION_MESSAGE);
 		}
 
 	}
 
 	/**
 	 * Check if the file passed by parameter is a results csv file type
 	 * 
 	 * @param file
 	 *            file to be checked
 	 * @return true if is a result csv file type.
 	 */
 
 	private boolean testResul(File file) {
 		try {
 			Scanner in = new Scanner(file);
 			String cabind = "Individual ;;Value";
 			String cabcol = "Team ;;Value";
			String temp = in.nextLine();
 
			if (temp.replaceAll(" ", "").equalsIgnoreCase(cabind.replaceAll(" ", "")) || temp.replaceAll(" ", "").equalsIgnoreCase(cabcol.replaceAll(" ", ""))) {
 				in.close();
 				return true;
 			}
 
 		} catch (FileNotFoundException e) {
 		}
 
 		return false;
 	}
 
 	/**
 	 * Check if the file passed by parameter is a competition with edition csv
 	 * file type
 	 * 
 	 * @param file
 	 *            file to be checked
 	 * @return true if is a competition with edition csv file type.
 	 */
 
 	private boolean testProva(File file) {
 		try {
 			Scanner in = new Scanner(file);
 			String cabecalho = "Sport;Discipline;Men;Women";
 
 			if (in.nextLine().replaceAll(" ", "").equalsIgnoreCase(cabecalho.replaceAll(" ", ""))) {
 				in.close();
 				return true;
 			}
 
 		} catch (FileNotFoundException e) {
 		}
 
 		return false;
 	}
 
 	/**
 	 * Check if the file passed by parameter is a competition csv file type
 	 * 
 	 * @param file
 	 *            file to be checked
 	 * @return true if is a competition csv file type.
 	 */
 	private boolean testDisc(File file) {
 		try {
 			Scanner in = new Scanner(file);
 			String cabecalho = "Sport;Discipline;Type;Men;Women;Mixed;Type;Order";
 
 			if (in.nextLine().replaceAll(" ", "").equalsIgnoreCase(cabecalho.replaceAll(" ", ""))) {
 				in.close();
 				return true;
 			}
 
 		} catch (FileNotFoundException e) {
 		}
 
 		return false;
 	}
 
 	/**
 	 * Check if the file passed by parameter is a country csv file type
 	 * 
 	 * @param file
 	 *            file to be checked
 	 * @return true if is a country csv file type.
 	 */
 	private boolean testPais(File file) {
 		try {
 			Scanner in = new Scanner(file);
 			String cabecalho = "Code ;Nation (NOC) ;Other codes used";
 
 			if (in.nextLine().replaceAll(" ", "").equalsIgnoreCase(cabecalho.replaceAll(" ", ""))) {
 				in.close();
 				return true;
 			}
 
 		} catch (FileNotFoundException e) {
 		}
 
 		return false;
 	}
 
 	/**
 	 * Method to import the country csv file type.
 	 * 
 	 * @param ficheiro
 	 *            if wanna choose the file in this method, send null.
 	 * @param janela
 	 *            the parent component of the dialog
 	 * @param paises
 	 *            linked list with the countries
 	 * @return true if file import was sucessful
 	 * @see Pais country details
 	 * 
 	 */
 	public boolean importPais(File ficheiro, Component janela, ListaLigada<Pais> paises) {
 		boolean veri = false;
 
 		try {
 
 			if (ficheiro == null) {
 				JFileChooser fc = new JFileChooser();
 				fc.setFileFilter(new CsvFilter());
 				fc.setDialogTitle("Import Country");
 				int returnVal = fc.showOpenDialog(janela);
 				if (returnVal != JFileChooser.APPROVE_OPTION)
 					return false;
 
 				ficheiro = fc.getSelectedFile();
 				veri = true;
 
 			}
 
 			Scanner in = new Scanner(ficheiro);
 
 			if (!in.hasNextLine()) {
 				JOptionPane.showMessageDialog(janela, "Empty File!", "Import File", JOptionPane.ERROR_MESSAGE);
 				return false;
 			}
 			in.nextLine();
 			while (in.hasNextLine()) {
 				String temp[] = in.nextLine().split(";");
 				temp[0] = temp[0].replaceAll("  ", " ");
 				if (temp[0].endsWith(" "))
 					temp[0] = temp[0].substring(0, temp[0].length() - 1);
 				if (temp[0].startsWith(" "))
 					temp[0] = temp[0].substring(1);
 				temp[1] = temp[1].replaceAll("  ", " ");
 				if (temp[1].endsWith(" "))
 					temp[1] = temp[1].substring(0, temp[1].length() - 1);
 				if (temp[1].startsWith(" "))
 					temp[1] = temp[1].substring(1);
 
 				int itPais = 0;
 				for (; itPais < paises.size(); itPais++) {
 					if (paises.get(itPais).getNomePais().equalsIgnoreCase(temp[1]) && paises.get(itPais).getCodigoPais(0).equalsIgnoreCase(temp[0])) {
 						break;
 					}
 				}
 
 				if (itPais == paises.size()) {
 					paises.add(new Pais(temp[0], temp[1]));
 
 					if (temp.length == 3) {
 						int index = paises.size() - 1;
 						temp[2] = temp[2].replaceAll(" ", "");
 						String[] codes = temp[2].split("\\)");
 
 						for (int i = 0; i < codes.length; i++) {
 							String[] code = codes[i].split("\\(");
 							String[] anos = code[1].split(",");
 							for (int j = 0; j < anos.length; j++) {
 
 								if (anos[j].matches("^[0-9]{4}$")) {
 									int ano = Integer.parseInt(anos[j]);
 
 									paises.get(index).getCodigos().add(new CodigosPais(code[0], ano));
 								} else if (anos[j].matches("^[0-9]{4}(S){1}$")) {
 									anos[j] = anos[j].replaceAll("S", "");
 									int ano = Integer.parseInt(anos[j]);
 									paises.get(index).getCodigos().add(new CodigosPais(code[0], ano));
 								} else if (anos[j].matches("^[0-9]{4}(_){1}[0-9]{4}$")) {
 									String[] anosTemp = anos[j].split("_");
 									int anoInicio = Integer.parseInt(anosTemp[0]);
 									int anoFim = Integer.parseInt(anosTemp[1]);
 									paises.get(index).getCodigos().add(new CodigosPais(code[0], anoInicio, anoFim));
 								} else if (anos[j].matches("^[0-9]{4}(S|W){0,1}_[0-9]{4}(S|W){0,1}$")) {
 									String[] anosTemp = anos[j].split("_");
 									int anoInicio = 0;
 									int anoFim = 0;
 									if (anosTemp[0].matches("^[0-9]{4}(W){1}")) {
 										anosTemp[0] = anosTemp[0].replaceAll("W", "");
 										anoInicio = Integer.parseInt(anosTemp[0]);
 										anoInicio++;
 									} else if (anosTemp[0].matches("^[0-9]{4}(S){1}")) {
 										anosTemp[1] = anosTemp[0].replaceAll("S", "");
 										anoInicio = Integer.parseInt(anosTemp[0]);
 									} else if (anosTemp[0].matches("^[0-9]{4}")) {
 										anoInicio = Integer.parseInt(anosTemp[0]);
 									}
 									if (anosTemp[1].matches("^[0-9]{4}(W){1}")) {
 										anosTemp[1] = anosTemp[1].replaceAll("W", "");
 										anoFim = Integer.parseInt(anosTemp[1]);
 										anoFim++;
 									} else if (anosTemp[1].matches("^[0-9]{4}(S){1}")) {
 										anosTemp[1] = anosTemp[1].replaceAll("S", "");
 										anoFim = Integer.parseInt(anosTemp[1]);
 									} else if (anosTemp[1].matches("^[0-9]{4}")) {
 										anoFim = Integer.parseInt(anosTemp[1]);
 									}
 									paises.get(index).getCodigos().add(new CodigosPais(code[0], anoInicio, anoFim));
 								} else if (anos[j].matches("^[0-9]{4}(W){1}$")) {
 								}
 
 							}
 						}
 					}
 				}
 
 			}
 			in.close();
 			if (veri)
 				JOptionPane.showMessageDialog(janela, "File imported sucessfully!", "Import File", JOptionPane.INFORMATION_MESSAGE);
 
 		} catch (FileNotFoundException exc) {
 			if (veri)
 				JOptionPane.showMessageDialog(janela, "File not found!", "Import File", JOptionPane.ERROR_MESSAGE);
 			return false;
 		} catch (ArrayIndexOutOfBoundsException | NumberFormatException exc) {
 			if (veri)
 				JOptionPane.showMessageDialog(janela, "Corrupted File!", "Import File", JOptionPane.ERROR_MESSAGE);
 			return false;
 		}
 		return true;
 
 	}
 
 	/**
 	 * Method to import languages through .csv files.
 	 * 
 	 * @param janela
 	 *            the parent component of the dialog
 	 * @param linguas
 	 *            language details
 	 */
 	public void importLingua(Component janela, ListaLigada<Linguas> linguas) {
 
 		try {
 			JFileChooser fc = new JFileChooser();
 			fc.setFileFilter(new CsvFilter());
 			int returnVal = fc.showOpenDialog(janela);
 			if (returnVal != JFileChooser.APPROVE_OPTION)
 				return;
 
 			File ficheiro = fc.getSelectedFile();
 			Scanner in = new Scanner(ficheiro);
 			if (!in.hasNextLine()) {
 				JOptionPane.showMessageDialog(janela, "Empty File!", "Import File", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			int i = 0, j = 1, k = 0, l = 1;
 			String[] temp = new String[30];
 			while (in.hasNextLine()) {
 				String tempz[] = in.nextLine().split(";");
 				temp[k] = tempz[i];
 				temp[l] = tempz[j];
 				k += 2;
 				l += 2;
 			}
 			Linguas eng = new Linguas(temp[1], temp[3], temp[5], temp[7], temp[9], temp[11], temp[13], temp[15], temp[17], temp[19], temp[21], temp[23], temp[25], temp[27]);
 			linguas.add(eng);
 			in.close();
 			JOptionPane.showMessageDialog(janela, "File imported sucessfully!", "Import File", JOptionPane.INFORMATION_MESSAGE);
 		} catch (FileNotFoundException | ArrayIndexOutOfBoundsException f) {
 			JOptionPane.showMessageDialog(janela, "Error exporting the document!", "Export File", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 	/**
 	 * Method to export the data of the countries
 	 * 
 	 * @param janela
 	 *            the parent component of the dialog
 	 * @param paises
 	 *            linked list with the countries
 	 * @see Pais country details
 	 * 
 	 */
 	public void exportPais(Component janela, ListaLigada<Pais> paises) {
 
 		if (paises.isEmpty()) {
 			JOptionPane.showMessageDialog(janela, "File is empty!", "Export File", JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 
 		try {
 			JFileChooser fc = new JFileChooser();
 			fc.setFileFilter(new CsvFilter());
 			fc.setDialogTitle("Export Country");
 			int returnVal = fc.showSaveDialog(janela);
 			if (returnVal != JFileChooser.APPROVE_OPTION)
 				return;
 			File ficheiro = fc.getSelectedFile();
 			Formatter out = new Formatter(ficheiro + ".csv");
 			out.format("Code ;Nation (NOC) ;\n");
 			for (int i = 0; i < paises.size(); i++) {
 				out.format("%s ;%s ;", paises.get(i).getCodigoPais(0), paises.get(i).getNomePais());
 				for (int j = 0; j < paises.get(i).getCodigos().size(); j++) {
 					if (paises.get(i).getCodigos().get(j).getAnoInicio() == paises.get(i).getCodigos().get(j).getAnoFim()) {
 						out.format("%s (%d), ", paises.get(i).getCodigos().get(j).getCodigo(), paises.get(i).getCodigos().get(j).getAnoInicio());
 					} else {
 						out.format("%s (%d_%d), ", paises.get(i).getCodigos().get(j).getCodigo(), paises.get(i).getCodigos().get(j).getAnoInicio(), paises.get(i).getCodigos().get(j).getAnoFim());
 
 					}
 				}
 				out.format("\n");
 			}
 			out.close();
 			JOptionPane.showMessageDialog(janela, "File exported sucessfully!", "Export File", JOptionPane.INFORMATION_MESSAGE);
 
 		} catch (FileNotFoundException exc) {
 			JOptionPane.showMessageDialog(janela, "Error exporting the document!", "Export File", JOptionPane.ERROR_MESSAGE);
 		}
 
 	}
 
 	/**
 	 * Method to import the competition csv file type
 	 * 
 	 * @param janela
 	 *            the parent component of the dialog
 	 * @param disciplina
 	 *            linked list with the competitions
 	 * @param modalidades
 	 *            linked list with the sports
 	 * @param ficheiro
 	 *            if wanna choose the file in this method, send null.
 	 * @see Disciplina competition details
 	 * @see Modalidade sport details
 	 * @return true if file import was sucessful
 	 * 
 	 */
 	public boolean importDisc(File ficheiro, Component janela, ListaLigada<Disciplina> disciplina, ListaLigada<Modalidade> modalidades) {
 
 		boolean veri = false;
 		try {
 
 			if (ficheiro == null) {
 				JFileChooser fc = new JFileChooser();
 				fc.setDialogTitle("Competition Import");
 				fc.setFileFilter(new CsvFilter());
 				int returnVal = fc.showOpenDialog(janela);
 				if (returnVal != JFileChooser.APPROVE_OPTION)
 					return false;
 
 				ficheiro = fc.getSelectedFile();
 				veri = true;
 			}
 
 			Scanner in = new Scanner(ficheiro);
 
 			if (!in.hasNextLine()) {
 				JOptionPane.showMessageDialog(janela, "Empty File!", "Import File", JOptionPane.ERROR_MESSAGE);
 				return false;
 			}
 
 			if (!in.nextLine().equals("Sport;Discipline;Type;Men;Women;Mixed;Type;Order")) {
 				JOptionPane.showMessageDialog(janela, "Corrupted File!", "Import File", JOptionPane.ERROR_MESSAGE);
 				return false;
 			}
 			while (in.hasNextLine()) {
 				String tempModal[] = in.nextLine().split(";");
 				boolean temModal = false;
 
 				for (int i = 0; i < modalidades.size(); i++) {
 					if (tempModal[0].equals(modalidades.get(i).getNome()))
 						temModal = true;
 				}
 
 				if (!temModal) {
 					tempModal[0] = tempModal[0].replaceAll("  ", " ");
 					if (tempModal[0].endsWith(" "))
 						tempModal[0] = tempModal[0].substring(0, tempModal[0].length() - 1);
 					if (tempModal[0].startsWith(" "))
 						tempModal[0] = tempModal[0].substring(1);
 					modalidades.add(new Modalidade(tempModal[0]));
 				}
 			}
 			in.close();
 
 			Scanner in2 = new Scanner(ficheiro);
 			in2.nextLine();
 			while (in2.hasNextLine()) {
 				String temp[] = in2.nextLine().split(";");
 				temp[1] = temp[1].replaceAll("  ", " ");
 				if (temp[1].endsWith(" "))
 					temp[1] = temp[1].substring(0, temp[1].length() - 1);
 				if (temp[1].startsWith(" "))
 					temp[1] = temp[1].substring(1);
 
 				Disciplina tempDisc = new Disciplina("temp");
 				Disciplina tempDisc2 = new Disciplina("temp");
 				int i = 0;
 
 				for (; i < modalidades.size(); i++) {
 					if (temp[0].equals(modalidades.get(i).getNome()))
 						break;
 				}
 
 				if (temp[2].equalsIgnoreCase("Individual")) {
 					if (temp[3].equalsIgnoreCase("X")) {
 
 						if (temp[7].equalsIgnoreCase("H")) {
 							if (temp[6].equalsIgnoreCase("m, ft")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 0, true, 0);
 							} else if (temp[6].equalsIgnoreCase("time")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 0, true, 1);
 							} else if (temp[6].equalsIgnoreCase("points")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 0, true, 2);
 							} else if (temp[6].equalsIgnoreCase("rank")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 0, true, 3);
 							}
 
 						} else {
 							if (temp[6].equalsIgnoreCase("m, ft")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 0, false, 0);
 							} else if (temp[6].equalsIgnoreCase("time")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 0, false, 1);
 							} else if (temp[6].equalsIgnoreCase("points")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 0, false, 2);
 							} else if (temp[6].equalsIgnoreCase("rank")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 0, false, 3);
 							}
 
 						}
 
 					}
 					if (temp[4].equalsIgnoreCase("X")) {
 
 						if (temp[7].equalsIgnoreCase("H")) {
 							if (temp[6].equalsIgnoreCase("m, ft")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 1, true, 0);
 							} else if (temp[6].equalsIgnoreCase("time")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 1, true, 1);
 							} else if (temp[6].equalsIgnoreCase("points")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 1, true, 2);
 							} else if (temp[6].equalsIgnoreCase("rank")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 1, true, 3);
 							}
 
 						} else {
 							if (temp[6].equalsIgnoreCase("m, ft")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 1, false, 0);
 							} else if (temp[6].equalsIgnoreCase("time")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 1, false, 1);
 							} else if (temp[6].equalsIgnoreCase("points")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 1, false, 2);
 							} else if (temp[6].equalsIgnoreCase("rank")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 1, false, 3);
 							}
 
 						}
 					}
 					if (temp[5].equalsIgnoreCase("X")) {
 						if (temp[7].equalsIgnoreCase("H")) {
 							if (temp[6].equalsIgnoreCase("m, ft")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 3, true, 0);
 							} else if (temp[6].equalsIgnoreCase("time")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 3, true, 1);
 							} else if (temp[6].equalsIgnoreCase("points")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 3, true, 2);
 							} else if (temp[6].equalsIgnoreCase("rank")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 3, true, 3);
 							}
 
 						} else {
 							if (temp[6].equalsIgnoreCase("m, ft")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 3, false, 0);
 							} else if (temp[6].equalsIgnoreCase("time")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 3, false, 1);
 							} else if (temp[6].equalsIgnoreCase("points")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 3, false, 2);
 							} else if (temp[6].equalsIgnoreCase("rank")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 3, false, 3);
 							}
 
 						}
 					}
 					if (temp[3].equalsIgnoreCase("X") && temp[4].equalsIgnoreCase("X")) {
 
 						if (temp[7].equalsIgnoreCase("H")) {
 							if (temp[6].equalsIgnoreCase("m, ft")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 0, true, 0);
 								tempDisc2 = new Disciplina(temp[1], modalidades.get(i), false, 1, true, 0);
 							} else if (temp[6].equalsIgnoreCase("time")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 0, true, 1);
 								tempDisc2 = new Disciplina(temp[1], modalidades.get(i), false, 1, true, 1);
 							} else if (temp[6].equalsIgnoreCase("points")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 0, true, 2);
 								tempDisc2 = new Disciplina(temp[1], modalidades.get(i), false, 1, true, 2);
 							} else if (temp[6].equalsIgnoreCase("rank")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 0, true, 3);
 								tempDisc2 = new Disciplina(temp[1], modalidades.get(i), false, 1, true, 3);
 							}
 
 						} else {
 							if (temp[6].equalsIgnoreCase("m, ft")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 0, false, 0);
 								tempDisc2 = new Disciplina(temp[1], modalidades.get(i), false, 1, false, 0);
 							} else if (temp[6].equalsIgnoreCase("time")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 0, false, 1);
 								tempDisc2 = new Disciplina(temp[1], modalidades.get(i), false, 1, false, 1);
 							} else if (temp[6].equalsIgnoreCase("points")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 0, false, 2);
 								tempDisc2 = new Disciplina(temp[1], modalidades.get(i), false, 1, false, 2);
 							} else if (temp[6].equalsIgnoreCase("rank")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), false, 0, false, 3);
 								tempDisc2 = new Disciplina(temp[1], modalidades.get(i), false, 1, false, 3);
 							}
 
 						}
 
 					}
 				} else if (temp[2].equalsIgnoreCase("Team")) {
 					if (temp[3].equalsIgnoreCase("X")) {
 
 						if (temp[7].equalsIgnoreCase("H")) {
 							if (temp[6].equalsIgnoreCase("m, ft")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 0, true, 0);
 							} else if (temp[6].equalsIgnoreCase("time")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 0, true, 1);
 							} else if (temp[6].equalsIgnoreCase("points")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 0, true, 2);
 							} else if (temp[6].equalsIgnoreCase("rank")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 0, true, 3);
 							}
 
 						} else {
 							if (temp[6].equalsIgnoreCase("m, ft")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 0, false, 0);
 							} else if (temp[6].equalsIgnoreCase("time")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 0, false, 1);
 							} else if (temp[6].equalsIgnoreCase("points")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 0, false, 2);
 							} else if (temp[6].equalsIgnoreCase("rank")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 0, false, 3);
 							}
 
 						}
 
 					}
 					if (temp[4].equalsIgnoreCase("X")) {
 
 						if (temp[7].equalsIgnoreCase("H")) {
 							if (temp[6].equalsIgnoreCase("m, ft")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 1, true, 0);
 							} else if (temp[6].equalsIgnoreCase("time")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 1, true, 1);
 							} else if (temp[6].equalsIgnoreCase("points")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 1, true, 2);
 							} else if (temp[6].equalsIgnoreCase("rank")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 1, true, 3);
 							}
 
 						} else {
 							if (temp[6].equalsIgnoreCase("m, ft")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 1, false, 0);
 							} else if (temp[6].equalsIgnoreCase("time")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 1, false, 1);
 							} else if (temp[6].equalsIgnoreCase("points")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 1, false, 2);
 							} else if (temp[6].equalsIgnoreCase("rank")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 1, false, 3);
 							}
 
 						}
 					}
 					if (temp[5].equalsIgnoreCase("X")) {
 						if (temp[7].equalsIgnoreCase("H")) {
 							if (temp[6].equalsIgnoreCase("m, ft")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 4, true, 0);
 							} else if (temp[6].equalsIgnoreCase("time")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 4, true, 1);
 							} else if (temp[6].equalsIgnoreCase("points")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 4, true, 2);
 							} else if (temp[6].equalsIgnoreCase("rank")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 4, true, 3);
 							}
 
 						} else {
 							if (temp[6].equalsIgnoreCase("m, ft")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 4, false, 0);
 							} else if (temp[6].equalsIgnoreCase("time")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 4, false, 1);
 							} else if (temp[6].equalsIgnoreCase("points")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 4, false, 2);
 							} else if (temp[6].equalsIgnoreCase("rank")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 4, false, 3);
 							}
 
 						}
 					}
 					if (temp[3].equalsIgnoreCase("X") && temp[4].equalsIgnoreCase("X")) {
 
 						if (temp[7].equalsIgnoreCase("H")) {
 							if (temp[6].equalsIgnoreCase("m, ft")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 0, true, 0);
 								tempDisc2 = new Disciplina(temp[1], modalidades.get(i), true, 1, true, 0);
 							} else if (temp[6].equalsIgnoreCase("time")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 0, true, 1);
 								tempDisc2 = new Disciplina(temp[1], modalidades.get(i), true, 1, true, 1);
 							} else if (temp[6].equalsIgnoreCase("points")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 0, true, 2);
 								tempDisc2 = new Disciplina(temp[1], modalidades.get(i), true, 1, true, 2);
 							} else if (temp[6].equalsIgnoreCase("rank")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 0, true, 3);
 								tempDisc2 = new Disciplina(temp[1], modalidades.get(i), true, 1, true, 3);
 							}
 
 						} else {
 							if (temp[6].equalsIgnoreCase("m, ft")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 0, false, 0);
 								tempDisc2 = new Disciplina(temp[1], modalidades.get(i), true, 1, false, 0);
 							} else if (temp[6].equalsIgnoreCase("time")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 0, false, 1);
 								tempDisc2 = new Disciplina(temp[1], modalidades.get(i), true, 1, false, 1);
 							} else if (temp[6].equalsIgnoreCase("points")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 0, false, 2);
 								tempDisc2 = new Disciplina(temp[1], modalidades.get(i), true, 1, false, 2);
 							} else if (temp[6].equalsIgnoreCase("rank")) {
 								tempDisc = new Disciplina(temp[1], modalidades.get(i), true, 0, false, 3);
 								tempDisc2 = new Disciplina(temp[1], modalidades.get(i), true, 1, false, 3);
 							}
 
 						}
 
 					}
 				}
 
 				int j = 0;
 				for (; j < disciplina.size(); j++) {
 					if (disciplina.get(j).equals(tempDisc))
 						break;
 				}
 				if (disciplina.size() == j)
 					disciplina.add(tempDisc);
 
 				if (!tempDisc2.getNome().equals("temp")) {
 					for (; j < disciplina.size(); j++) {
 						if (disciplina.equals(tempDisc2))
 							break;
 					}
 
 					if (disciplina.size() == j)
 						disciplina.add(tempDisc2);
 				}
 			}
 			in2.close();
 			if (veri)
 				JOptionPane.showMessageDialog(janela, "File imported sucessfully!", "Import File", JOptionPane.INFORMATION_MESSAGE);
 
 		} catch (FileNotFoundException exc) {
 			if (veri)
 				JOptionPane.showMessageDialog(janela, "File not found!", "Import File", JOptionPane.ERROR_MESSAGE);
 			return false;
 		} catch (ArrayIndexOutOfBoundsException | NumberFormatException exc) {
 			if (veri)
 				JOptionPane.showMessageDialog(janela, "Corrupted File!", "Import File", JOptionPane.ERROR_MESSAGE);
 			return false;
 		}
 		return true;
 
 	}
 
 	/**
 	 * Method to export the data of the competitions
 	 * 
 	 * @param janela
 	 *            the parent component of the dialog
 	 * @param modalidades
 	 *            linked list with the sports
 	 * @see Modalidade sport details
 	 * 
 	 */
 	public void exportDisciplina(Component janela, ListaLigada<Modalidade> modalidades) {
 
 		if (modalidades.isEmpty()) {
 			JOptionPane.showMessageDialog(janela, "File is empty!", "Export File", JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 
 		try {
 			JFileChooser fc = new JFileChooser();
 			fc.setFileFilter(new CsvFilter());
 			fc.setDialogTitle("Export Competition");
 			int returnVal = fc.showSaveDialog(janela);
 			if (returnVal != JFileChooser.APPROVE_OPTION)
 				return;
 			File ficheiro = fc.getSelectedFile();
 			Formatter out = new Formatter(ficheiro + ".csv");
 			out.format("Sport;Discipline;Type;Men;Women;Mixed;Type;Order\n");
 			for (int i = 0; i < modalidades.size(); i++) {
 				String nomeModal = modalidades.get(i).getNome();
 				String nomeDisc = "";
 				String tipo = "";
 				String typeClass = "";
 				String order = "";
 
 				ListaLigada<Disciplina> discTemp = new ListaLigada<Disciplina>();
 				for (int j = 0; j < modalidades.get(i).getDisc().size(); j++) {
 					discTemp.add(modalidades.get(i).getDisc().get(j));
 				}
 
 				for (int j = 0; j < discTemp.size(); j++) {
 					nomeDisc = discTemp.get(j).getNome();
 					String men = "";
 					String women = "";
 					String mixed = "";
 					if (discTemp.get(j).getTipoMod())
 						tipo = "Team";
 					else
 						tipo = "Individual";
 					if (discTemp.get(j).getOrdenacao())
 						order = "H";
 					else
 						order = "L";
 					if (discTemp.get(j).getTipoClass() == 0)
 						typeClass = "m, ft";
 					else if (discTemp.get(j).getTipoClass() == 1)
 						typeClass = "time";
 					else if (discTemp.get(j).getTipoClass() == 2)
 						typeClass = "points";
 					else if (discTemp.get(j).getTipoClass() == 3)
 						typeClass = "rank";
 					if (discTemp.get(j).getGenero() == 0)
 						men = "X";
 					else if (discTemp.get(j).getGenero() == 1)
 						women = "X";
 					else if (discTemp.get(j).getGenero() == 2)
 						mixed = "X";
 
 					for (int k = j + 1; k < discTemp.size(); k++) {
 						if (discTemp.get(j).equals(discTemp.get(k))) {
 							if (discTemp.get(k).getGenero() == 0)
 								men = "X";
 							else if (discTemp.get(k).getGenero() == 1)
 								women = "X";
 							else if (discTemp.get(k).getGenero() == 2)
 								mixed = "X";
 							discTemp.remove(k);
 							k--;
 						}
 					}
 					out.format("%s;%s;%s;%s;%s;%s;%s;%s\n", nomeModal, nomeDisc, tipo, men, women, mixed, typeClass, order);
 				}
 
 			}
 			out.close();
 			JOptionPane.showMessageDialog(janela, "File exported sucessfully!", "Export File", JOptionPane.INFORMATION_MESSAGE);
 
 		} catch (FileNotFoundException exc) {
 			JOptionPane.showMessageDialog(janela, "Error exporting the document!", "Export File", JOptionPane.ERROR_MESSAGE);
 		}
 
 	}
 
 	/**
 	 * Method to import the results csv file type.
 	 * 
 	 * @param janela
 	 *            the parent component of the dialog
 	 * @param paises
 	 *            linked list with the countries
 	 * @param modalidades
 	 *            linked list with the sports
 	 * @param jogos
 	 *            linked list of events
 	 * @param provas
 	 *            linked list with the competitions with event
 	 * @param equipas
 	 *            linked list with the teams
 	 * @param atletas
 	 *            linked list with the athletes
 	 * @param ficheiro
 	 *            if wanna choose the file in this method, send null.
 	 * @see Pais country details
 	 * @see Disciplina competition details
 	 * @see Modalidade sport details
 	 * @see JogosOlimpicos event details
 	 * @see Prova competition with event details
 	 * @see Equipa team details
 	 * @see Atleta athlete details
 	 * @return true if file import was sucessful
 	 * 
 	 */
 	@SuppressWarnings("unused")
 	public boolean importResultados(File ficheiro, Component janela, ListaLigada<Atleta> atletas, ListaLigada<Modalidade> modalidades, ListaLigada<Pais> paises, ListaLigada<Prova> provas, ListaLigada<Equipa> equipas, ListaLigada<JogosOlimpicos> jogos) {
 		boolean veri = false;
 		try {
 			if (ficheiro == null) {
 				JFileChooser fc = new JFileChooser();
 				fc.setFileFilter(new CsvFilter());
 				fc.setDialogTitle("Import Results");
 				int returnVal = fc.showOpenDialog(janela);
 				if (returnVal != JFileChooser.APPROVE_OPTION)
 					return false;
 				ficheiro = fc.getSelectedFile();
 				veri = true;
 			}
 
 			int ponto = ficheiro.getName().lastIndexOf(".");
 			String[] tempPrin = ficheiro.getName().substring(0, ponto).split("_");
 			int ano = Integer.parseInt(tempPrin[0]);
 			int itJogos = 0;
 			for (; itJogos < jogos.size(); itJogos++) {
 				if (jogos.get(itJogos).getAno() == ano) {
 					break;
 				}
 			}
 
 			if (itJogos == jogos.size()) {
 				JOptionPane.showMessageDialog(janela, "Year not found!\nPlease import: " + ano + "!", "Import File", JOptionPane.ERROR_MESSAGE);
 				return false;
 			}
 
 			String modalidade = tempPrin[1];
 			String genero = tempPrin[2];
 			int codGenero = -1;
 			boolean tipoDisc = false;
 			int tipoClass = -1;
 			String nomeDisc;
 			int itDisc = 0;
 			int itProva = 0;
 
 			if (genero.equalsIgnoreCase("Men"))
 				codGenero = 0;
 			else if (genero.equalsIgnoreCase("Women"))
 				codGenero = 1;
 			else if (genero.equalsIgnoreCase("Mixed"))
 				codGenero = 2;
 
 			int itModal = 0;
 
 			for (; itModal < modalidades.size(); itModal++) {
 				if (modalidade.equals(modalidades.get(itModal).getNome())) {
 					break;
 				}
 			}
 
 			if (itModal == modalidades.size()) {
 				JOptionPane.showMessageDialog(janela, "Sport not found!\nPlease import: " + modalidade + "!", "Import File", JOptionPane.ERROR_MESSAGE);
 				return false;
 			}
 
 			Scanner inTest = new Scanner(ficheiro);
 
 			if (!inTest.hasNextLine()) {
 				JOptionPane.showMessageDialog(janela, "Empty File!", "Import File", JOptionPane.ERROR_MESSAGE);
 				return false;
 			}
 
 			while (inTest.hasNextLine()) {
 				String test[] = inTest.nextLine().split(";");
 				test[0] = test[0].replaceAll("  ", " ");
 				if (test[0].endsWith(" "))
 					test[0] = test[0].substring(0, test[0].length() - 1);
 				if (test[0].startsWith(" "))
 					test[0] = test[0].substring(1);
 				if (test[0].equalsIgnoreCase("Individual") || test[0].equalsIgnoreCase("Team")) {
 				} else if (test[0].equals("")) {
 				} else {
 					boolean testeDisc = false;
 					for (int j = 0; j < modalidades.get(itModal).getDisc().size(); j++) {
 						if (test[0].replaceAll(" ", "").equalsIgnoreCase(modalidades.get(itModal).getDisc().get(j).getNome().replaceAll(" ", "")))
 							testeDisc = true;
 					}
 					if (!testeDisc) {
 						JOptionPane.showMessageDialog(janela, "Competition not found!\nPlease import: " + test[0] + "!", "Import File", JOptionPane.ERROR_MESSAGE);
 						return false;
 					}
 
 				}
 
 			}
 
 			Scanner in = new Scanner(ficheiro);
 			while (in.hasNextLine()) {
 
 				String temp[] = in.nextLine().split(";");
 				if (temp[0].equals("")) {
 					if (!tipoDisc) {
 
 						String[] atl = temp[1].split(", ");
 						atl[0] = atl[0].replaceAll("  ", " ");
 						if (atl[0].endsWith(" "))
 							atl[0] = atl[0].substring(0, atl[0].length() - 1);
 						if (atl[0].startsWith(" "))
 							atl[0] = atl[0].substring(1);
 
 						atl[1] = atl[1].replaceAll("  ", " ");
 						if (atl[1].endsWith(" "))
 							atl[1] = atl[1].substring(0, atl[1].length() - 1);
 						if (atl[1].startsWith(" "))
 							atl[1] = atl[1].substring(1);
 
 						int itAtleta = 0;
 						for (; itAtleta < atletas.size(); itAtleta++) {
 							if (atletas.get(itAtleta).getNome().equalsIgnoreCase(atl[0]) && atletas.get(itAtleta).getPais().getCodigoPais(ano).equalsIgnoreCase(atl[1])) {
 
 								break;
 							}
 
 						}
 
 						if (itAtleta == atletas.size()) {
 							int itPais = 0;
 							for (; itPais < paises.size(); itPais++) {
 								if (atl[1].equalsIgnoreCase(paises.get(itPais).getCodigoPais(ano))) {
 									break;
 								}
 							}
 
 							if (itPais == paises.size()) {
 								JOptionPane.showMessageDialog(janela, "Country not found!\nPlease import: " + atl[1] + "!", "Import File", JOptionPane.ERROR_MESSAGE);
 								return false;
 							}
 
 							atletas.add(new Atleta(atl[0], paises.get(itPais)));
 
 						}
 						int i = 0;
 						for (; i < ((ProvaInd) provas.get(itProva)).getResultados().size(); i++) {
 							if (((ProvaInd) provas.get(itProva)).getResultados().get(i).equals(new ResultadosInd(atletas.get(itAtleta), temp[2], tipoClass)))
 								break;
 						}
 						if (i == ((ProvaInd) provas.get(itProva)).getResultados().size())
 							((ProvaInd) provas.get(itProva)).getResultados().add(new ResultadosInd(atletas.get(itAtleta), temp[2], tipoClass));
 
 					} else {
 						String[] team = temp[1].split("\\(");
 
 						boolean existeAtletas = false;
 
 						team[0] = team[0].replaceAll("  ", " ");
 						if (team[0].endsWith(" "))
 							team[0] = team[0].substring(0, team[0].length() - 1);
 						if (team[0].startsWith(" "))
 							team[0] = team[0].substring(1);
 
 						int itPais = 0;
 						for (; itPais < paises.size(); itPais++) {
 							if (team[0].equalsIgnoreCase(paises.get(itPais).getCodigoPais(ano))) {
 								break;
 							}
 						}
 
 						if (itPais == paises.size()) {
 							JOptionPane.showMessageDialog(janela, "Country not found!\nPlease import: " + team[0] + "!", "Import File", JOptionPane.ERROR_MESSAGE);
 							return false;
 						}
 						ListaLigada<Atleta> atletasEqu = new ListaLigada<Atleta>();
 						String[] atletasTemp = team[1].split(", ");
 						atletasTemp[atletasTemp.length - 1] = atletasTemp[atletasTemp.length - 1].replaceAll("\\)", "");
 						for (int i = 0; i < atletasTemp.length; i++) {
 							boolean existeAtleta = false;
 							int itAtleta = 0;
 							for (; itAtleta < atletas.size(); itAtleta++) {
 								if (atletas.get(itAtleta).getNome().equalsIgnoreCase(atletasTemp[i]) && atletas.get(itAtleta).getPais().getCodigoPais(ano).equalsIgnoreCase(team[0])) {
 									existeAtleta = true;
 									break;
 								}
 							}
 							if (!existeAtleta) {
 								atletas.add(new Atleta(atletasTemp[i], paises.get(itPais)));
 							}
 
 							atletasEqu.add(atletas.get(itAtleta));
 						}
 						Equipa equiTemp = new Equipa(paises.get(itPais));
 						equiTemp.setAtletas(atletasEqu);
 						int itEquipa = 0;
 						for (; itEquipa < equipas.size(); itEquipa++) {
 							if (equiTemp.equals(equipas.get(itEquipa))) {
 								break;
 							}
 						}
 						if (itEquipa == equipas.size())
 							equipas.add(new Equipa(paises.get(itPais)));
 						int i = 0;
 						for (; i < ((ProvaCol) provas.get(itProva)).getResultados().size(); i++) {
 							if (((ProvaCol) provas.get(itProva)).getResultados().get(i).equals(new ResultadosCol(equipas.get(itEquipa), temp[2], tipoClass)))
 								break;
 						}
 						if (i == ((ProvaCol) provas.get(itProva)).getResultados().size())
 							((ProvaCol) provas.get(itProva)).getResultados().add(new ResultadosCol(equipas.get(itEquipa), temp[2], tipoClass));
 
 					}
 
 				} else if (temp[0].replaceAll(" ", "").equalsIgnoreCase("Individual")) {
 					tipoDisc = false;
 
 				} else if (temp[0].replaceAll(" ", "").equalsIgnoreCase("Team")) {
 					tipoDisc = true;
 				} else {
 					nomeDisc = temp[0];
 					nomeDisc = nomeDisc.replaceAll("  ", " ");
 					if (nomeDisc.endsWith(" "))
 						nomeDisc = nomeDisc.substring(0, nomeDisc.length() - 1);
 					if (nomeDisc.startsWith(" "))
 						nomeDisc = nomeDisc.substring(1);
 
 					itDisc = 0;
 					for (; itDisc < modalidades.get(itModal).getDisc().size(); itDisc++) {
 						if (nomeDisc.replaceAll(" ", "").equalsIgnoreCase(modalidades.get(itModal).getDisc().get(itDisc).getNome().replaceAll(" ", "")) && codGenero == modalidades.get(itModal).getDisc().get(itDisc).getGenero())
 							break;
 					}
 					tipoClass = modalidades.get(itModal).getDisc().get(itDisc).getTipoClass();
 
 					if (!tipoDisc) {
 						boolean existeAtleta = false;
 						String[] atl = temp[1].split(", ");
 
 						atl[0] = atl[0].replaceAll("  ", " ");
 						if (atl[0].endsWith(" "))
 							atl[0] = atl[0].substring(0, atl[0].length() - 1);
 						if (atl[0].startsWith(" "))
 							atl[0] = atl[0].substring(1);
 
 						atl[1] = atl[1].replaceAll("  ", " ");
 						if (atl[1].endsWith(" "))
 							atl[1] = atl[1].substring(0, atl[1].length() - 1);
 						if (atl[1].startsWith(" "))
 							atl[1] = atl[1].substring(1);
 
 						int itAtleta = 0;
 						for (; itAtleta < atletas.size(); itAtleta++) {
 							if (atletas.get(itAtleta).getNome().equalsIgnoreCase(atl[0]) && atletas.get(itAtleta).getPais().getCodigoPais(ano).equalsIgnoreCase(atl[1])) {
 								existeAtleta = true;
 								break;
 							}
 
 						}
 
 						if (!existeAtleta) {
 							int itPais = 0;
 							for (; itPais < paises.size(); itPais++) {
 								if (atl[1].equalsIgnoreCase(paises.get(itPais).getCodigoPais(ano))) {
 									break;
 								}
 							}
 
 							if (itPais == paises.size()) {
 								JOptionPane.showMessageDialog(janela, "Country not found!\nPlease import: " + atl[1] + "!", "Import File", JOptionPane.ERROR_MESSAGE);
 								return false;
 							}
 
 							atletas.add(new Atleta(atl[0], paises.get(itPais)));
 
 						}
 
 						itProva = 0;
 
 						for (; itProva < provas.size(); itProva++) {
 							if (provas.get(itProva).getJogosOlimpicos().getAno() == ano && provas.get(itProva).getDisciplina().getNome().replaceAll(" ", "").equalsIgnoreCase(nomeDisc.replaceAll(" ", "")) && provas.get(itProva).getDisciplina().getTipoMod() == tipoDisc && provas.get(itProva).getDisciplina().getGenero() == codGenero) {
 								break;
 							}
 						}
 
 						if (itProva == provas.size()) {
 							JOptionPane.showMessageDialog(janela, "Competition not found!\nPlease import: " + nomeDisc + "of year" + ano + "!", "Import File", JOptionPane.ERROR_MESSAGE);
 							return false;
 						}
 						int i = 0;
 						for (; i < ((ProvaInd) provas.get(itProva)).getResultados().size(); i++) {
 							if (((ProvaInd) provas.get(itProva)).getResultados().get(i).equals(new ResultadosInd(atletas.get(itAtleta), temp[2], tipoClass)))
 								break;
 						}
 						if (i == ((ProvaInd) provas.get(itProva)).getResultados().size())
 							((ProvaInd) provas.get(itProva)).getResultados().add(new ResultadosInd(atletas.get(itAtleta), temp[2], tipoClass));
 
 					} else {
 						itProva = 0;
 
 						for (; itProva < provas.size(); itProva++) {
 							if (provas.get(itProva).getJogosOlimpicos().getAno() == ano && provas.get(itProva).getDisciplina().getNome().replaceAll(" ", "").equalsIgnoreCase(nomeDisc.replaceAll(" ", "")) && provas.get(itProva).getDisciplina().getTipoMod() == tipoDisc && provas.get(itProva).getDisciplina().getGenero() == codGenero) {
 								break;
 							}
 						}
 
 						if (itProva == provas.size()) {
 							JOptionPane.showMessageDialog(janela, "Competition not found!\nPlease import: " + nomeDisc + "of year" + ano + "!", "Import File", JOptionPane.ERROR_MESSAGE);
 							return false;
 						}
 						String[] team = temp[1].split("\\(");
 
 						boolean existeAtletas = false;
 
 						team[0] = team[0].replaceAll("  ", " ");
 						if (team[0].endsWith(" "))
 							team[0] = team[0].substring(0, team[0].length() - 1);
 						if (team[0].startsWith(" "))
 							team[0] = team[0].substring(1);
 
 						int itPais = 0;
 						for (; itPais < paises.size(); itPais++) {
 							if (team[0].equalsIgnoreCase(paises.get(itPais).getCodigoPais(ano))) {
 								break;
 							}
 						}
 
 						if (itPais == paises.size()) {
 							JOptionPane.showMessageDialog(janela, "Country not found!\nPlease import: " + team[0] + "!", "Import File", JOptionPane.ERROR_MESSAGE);
 							return false;
 						}
 
 						String[] atletasTemp = team[1].split(", ");
 						atletasTemp[atletasTemp.length - 1] = atletasTemp[atletasTemp.length - 1].replaceAll("\\)", "");
 						ListaLigada<Atleta> atletasEqu = new ListaLigada<Atleta>();
 
 						for (int i = 0; i < atletasTemp.length; i++) {
 							int itAtleta = 0;
 							boolean existeAtleta = false;
 							for (; itAtleta < atletas.size(); itAtleta++) {
 								if (atletas.get(itAtleta).getNome().equalsIgnoreCase(atletasTemp[i]) && atletas.get(itAtleta).getPais().getCodigoPais(ano).equalsIgnoreCase(team[0])) {
 									existeAtleta = true;
 									break;
 								}
 
 							}
 							if (!existeAtleta) {
 
 								atletas.add(new Atleta(atletasTemp[i], paises.get(itPais)));
 
 							}
 							atletasEqu.add(atletas.get(itAtleta));
 						}
 						Equipa equiTemp = new Equipa(paises.get(itPais));
 						equiTemp.setAtletas(atletasEqu);
 						int itEquipa = 0;
 						for (; itEquipa < equipas.size(); itEquipa++) {
 							if (equiTemp.equals(equipas.get(itEquipa))) {
 								break;
 							}
 						}
 						if (itEquipa == equipas.size())
 							equipas.add(new Equipa(paises.get(itPais)));
 						int i = 0;
 						for (; i < ((ProvaCol) provas.get(itProva)).getResultados().size(); i++) {
 							if (((ProvaCol) provas.get(itProva)).getResultados().get(i).equals(new ResultadosCol(equipas.get(itEquipa), temp[2], tipoClass)))
 								break;
 						}
 						if (i == ((ProvaCol) provas.get(itProva)).getResultados().size())
 							((ProvaCol) provas.get(itProva)).getResultados().add(new ResultadosCol(equipas.get(itEquipa), temp[2], tipoClass));
 
 					}
 
 				}
 
 			}
 			in.close();
 			if (veri)
 				JOptionPane.showMessageDialog(janela, "File imported sucessfully!", "Import File", JOptionPane.INFORMATION_MESSAGE);
 
 		} catch (FileNotFoundException exc) {
 			if (veri)
 				JOptionPane.showMessageDialog(janela, "File not found!", "Import File", JOptionPane.ERROR_MESSAGE);
 			return false;
 		} catch (ArrayIndexOutOfBoundsException | NumberFormatException exc) {
 			if (veri)
 				JOptionPane.showMessageDialog(janela, "Corrupted File!", "Import File", JOptionPane.ERROR_MESSAGE);
 			return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * Method to export the results to csv file.
 	 * 
 	 * @param janela
 	 *            the parent component of the dialog
 	 * @param modalidades
 	 *            linked list with the sports
 	 * @param provas
 	 *            linked list with the competitions with event
 	 * @param modalidade
 	 *            sport to export
 	 * @param genero
 	 *            genre to export. Male = 0, Female = 1, Mixed = 2
 	 * @param ano
 	 *            year to export
 	 * 
 	 * @see Modalidade sport details
 	 * @see Prova competition with event details
 	 */
 	public void exportResultados(Component janela, ListaLigada<Modalidade> modalidades, ListaLigada<Prova> provas, String modalidade, int genero, int ano) {
 
 		if (modalidades.isEmpty() || provas.isEmpty()) {
 			JOptionPane.showMessageDialog(janela, "File is empty!", "Export File", JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 
 		try {
 			JFileChooser fc = new JFileChooser();
 			fc.setFileFilter(new CsvFilter());
 			fc.setDialogTitle("Export Results");
 			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 			int returnVal = fc.showSaveDialog(janela);
 			if (returnVal != JFileChooser.APPROVE_OPTION)
 				return;
 			File ficheiro = fc.getSelectedFile();
 			Formatter out;
 			if (genero == 0)
 				out = new Formatter(ficheiro + "\\" + ano + "_" + modalidade + "_Men.csv");
 			else {
 				out = new Formatter(ficheiro + "\\" + ano + "_" + modalidade + "_Women.csv");
 			}
 			out.format("Individual ;;Value\n");
 			int imod = 0;
 			for (; imod < modalidades.size(); imod++) {
 				if (modalidade.equalsIgnoreCase(modalidades.get(imod).getNome()))
 					break;
 			}
 
 			for (int i = 0; i < modalidades.get(imod).getDisc().size(); i++) {
 
 			}
 
 			int itModal = 0;
 
 			for (; itModal < modalidades.size(); itModal++) {
 				if (modalidades.get(itModal).getNome().equals(modalidade))
 					break;
 			}
 
 			ListaLigada<Prova> provasTemp = new ListaLigada<Prova>();
 			for (int i = 0; i < provas.size(); i++) {
 				provasTemp.add(provas.get(i));
 			}
 
 			for (int i = 0; i < modalidades.get(itModal).getDisc().size(); i++) {
 				if (!modalidades.get(itModal).getDisc().get(i).getTipoMod())
 					for (int j = 0; j < provasTemp.size(); j++) {
 
 						if (modalidades.get(itModal).getDisc().get(i).getNome().equals(provasTemp.get(j).getDisciplina().getNome()) && modalidades.get(itModal).getNome().equals(provasTemp.get(j).getDisciplina().getModalidade().getNome())) {
 
 							if (provasTemp.get(j) instanceof ProvaInd) {
 
 								out.format(modalidades.get(itModal).getDisc().get(i).getNome());
 								for (int k = 0; k < ((ProvaInd) provasTemp.get(j)).getResultados().size(); k++) {
 									out.format(";" + ((ProvaInd) provasTemp.get(j)).getResultados().get(k).getAtleta().getNome() + ", ");
 									out.format(((ProvaInd) provasTemp.get(j)).getResultados().get(k).getAtleta().getPais().getCodigoPais(ano) + ";");
 									out.format(((ProvaInd) provasTemp.get(j)).getResultados().get(k).getResulTemp() + "\n");
 								}
 								provasTemp.remove(j);
 								j--;
 							}
 
 						}
 					}
 			}
 			out.format("Team ;;Value\n");
 			for (int i = 0; i < modalidades.get(itModal).getDisc().size(); i++) {
 				if (modalidades.get(itModal).getDisc().get(i).getTipoMod())
 					for (int j = 0; j < Main.getProvas().size(); j++) {
 
 						if (modalidades.get(itModal).getDisc().get(i).getNome().equals(provasTemp.get(j).getDisciplina().getNome()) && modalidades.get(itModal).getNome().equals(provasTemp.get(j).getDisciplina().getModalidade().getNome())) {
 
 							if (provasTemp.get(j) instanceof ProvaCol) {
 								out.format(modalidades.get(itModal).getDisc().get(i).getNome());
 
 								for (int k = 0; k < ((ProvaCol) provasTemp.get(j)).getResultados().size(); k++) {
 									out.format(";" + ((ProvaCol) provasTemp.get(j)).getResultados().get(k).getEquipa().getPais().getCodigoPais(ano) + "(");
 									for (int l = 0; l < ((ProvaCol) provasTemp.get(j)).getResultados().get(k).getEquipa().getAtleta().size() - 1; l++) {
 										out.format(((ProvaCol) provasTemp.get(j)).getResultados().get(k).getEquipa().getAtleta().get(l).getNome() + ", ");
 									}
 									int index = ((ProvaCol) provasTemp.get(j)).getResultados().get(k).getEquipa().getAtleta().size() - 1;
 									out.format(((ProvaCol) provasTemp.get(j)).getResultados().get(k).getEquipa().getAtleta().get(index).getNome() + ");");
 									out.format(((ProvaCol) provasTemp.get(j)).getResultados().get(k).getResulTemp() + "\n");
 								}
 								provasTemp.remove(j);
 								j--;
 							}
 
 						}
 					}
 			}
 
 			out.close();
 			JOptionPane.showMessageDialog(janela, "File exported sucessfully!", "Export File", JOptionPane.INFORMATION_MESSAGE);
 
 		} catch (FileNotFoundException exc) {
 			JOptionPane.showMessageDialog(janela, "Error exporting the document!", "Export File", JOptionPane.ERROR_MESSAGE);
 		}
 
 	}
 
 	/**
 	 * Method to import the competitions with event csv file.
 	 * 
 	 * @param janela
 	 *            the parent component of the dialog
 	 * @param modalidades
 	 *            linked list with the sports
 	 * @param provas
 	 *            linked list with the competitions with event
 	 * @param ficheiro
 	 *            if wanna choose the file in this method, send null.
 	 * @param disciplinas
 	 *            linked list with the competitions
 	 * @param jogos
 	 *            linked list with the events
 	 * @see Modalidade sport details
 	 * @see Prova competition with event details
 	 * @see Disciplina competition details
 	 * @see JogosOlimpicos event details
 	 * @return true if file import was sucessful
 	 */
 	public boolean importProvas(File ficheiro, Component janela, ListaLigada<JogosOlimpicos> jogos, ListaLigada<Prova> provas, ListaLigada<Disciplina> disciplinas, ListaLigada<Modalidade> modalidades) {
 
 		boolean veri = false;
 		try {
 			if (ficheiro == null) {
 				JFileChooser fc = new JFileChooser();
 				fc.setFileFilter(new CsvFilter());
 				fc.setDialogTitle("Competition with Event Import");
 				int returnVal = fc.showOpenDialog(janela);
 				if (returnVal != JFileChooser.APPROVE_OPTION)
 					return false;
 
 				ficheiro = fc.getSelectedFile();
 				veri = true;
 			}
 
 			String file[] = ficheiro.getName().split("_");
 			file[3] = file[3].replaceAll(".csv", "");
 			int ano = Integer.parseInt(file[3]);
 
 			int itAno = 0;
 			for (; itAno < jogos.size(); itAno++) {
 				if (jogos.get(itAno).getAno() == ano)
 					break;
 			}
 			if (itAno == jogos.size())
 				jogos.add(new JogosOlimpicos(ano));
 			else
 				return true;
 
 			for (int i = 0; i < jogos.size() - 1; i++) {
 				for (int j = i + 1; j < jogos.size(); j++) {
 					if (jogos.get(i).compareTo(jogos.get(j)) > 0) {
 						JogosOlimpicos temp = jogos.get(j);
 						jogos.set(j, jogos.get(i));
 						jogos.set(i, temp);
 					}
 				}
 			}
 
 			Scanner inTest = new Scanner(ficheiro);
 
 			if (!inTest.hasNextLine()) {
 				JOptionPane.showMessageDialog(janela, "Empty File!", "Import File", JOptionPane.ERROR_MESSAGE);
 				return false;
 			}
 
 			inTest.nextLine();
 			while (inTest.hasNextLine()) {
 				String test[] = inTest.nextLine().split(";");
 				test[0] = test[0].replaceAll("  ", " ");
 				if (test[0].endsWith(" "))
 					test[0] = test[0].substring(0, test[0].length() - 1);
 				if (test[0].startsWith(" "))
 					test[0] = test[0].substring(1);
 				test[1] = test[1].replaceAll("  ", " ");
 				if (test[1].endsWith(" "))
 					test[1] = test[1].substring(0, test[1].length() - 1);
 				if (test[1].startsWith(" "))
 					test[1] = test[1].substring(1);
 
 				int iTest = 0;
 				for (; iTest < modalidades.size(); iTest++) {
 					if (modalidades.get(iTest).getNome().equalsIgnoreCase(test[0]))
 						break;
 				}
 
 				if (iTest == modalidades.size()) {
 					JOptionPane.showMessageDialog(janela, "Sport not found!\nPlease import: " + test[0] + "!", "Import File", JOptionPane.ERROR_MESSAGE);
 					return false;
 				}
 				iTest = 0;
 				for (; iTest < disciplinas.size(); iTest++) {
 					if (disciplinas.get(iTest).getNome().equalsIgnoreCase(test[1]))
 						break;
 				}
 
 				if (iTest == disciplinas.size()) {
 					JOptionPane.showMessageDialog(janela, "Competition not found!\nPlease import: " + test[1] + "!", "Import File", JOptionPane.ERROR_MESSAGE);
 					return false;
 				}
 			}
 			inTest.close();
 
 			Scanner in = new Scanner(ficheiro);
 			in.nextLine();
 			while (in.hasNextLine()) {
 				String temp[] = in.nextLine().split(";");
 				temp[0] = temp[0].replaceAll("  ", " ");
 				if (temp[0].endsWith(" "))
 					temp[0] = temp[0].substring(0, temp[0].length() - 1);
 				if (temp[0].startsWith(" "))
 					temp[0] = temp[0].substring(1);
 
 				temp[1] = temp[1].replaceAll("  ", " ");
 				if (temp[1].endsWith(" "))
 					temp[1] = temp[1].substring(0, temp[1].length() - 1);
 				if (temp[1].startsWith(" "))
 					temp[1] = temp[1].substring(1);
 
 				boolean tipoProva = false;
 
 				for (int i = 0; i < disciplinas.size(); i++) {
 					if (disciplinas.get(i).getNome().equalsIgnoreCase(temp[1])) {
 						tipoProva = disciplinas.get(i).getTipoMod();
 						break;
 					}
 				}
 
 				int itModal = 0;
 				for (; itModal < modalidades.size(); itModal++) {
 					if (temp[0].equals(modalidades.get(itModal).getNome())) {
 						break;
 					}
 				}
 
 				if (temp[2].equalsIgnoreCase("x")) {
 					if (!tipoProva) {
 						int itDisc = 0;
 						for (; itDisc < disciplinas.size(); itDisc++) {
 							if (temp[1].equalsIgnoreCase(disciplinas.get(itDisc).getNome()) && 0 == disciplinas.get(itDisc).getGenero() && temp[0].equalsIgnoreCase(disciplinas.get(itDisc).getModalidade().getNome()))
 								break;
 						}
 
 						provas.add(new ProvaInd(disciplinas.get(itDisc), jogos.get(jogos.size() - 1)));
 
 					} else {
 						int itDisc = 0;
 						for (; itDisc < disciplinas.size(); itDisc++) {
 							if (temp[1].equalsIgnoreCase(disciplinas.get(itDisc).getNome()) && 0 == disciplinas.get(itDisc).getGenero() && temp[0].equalsIgnoreCase(disciplinas.get(itDisc).getModalidade().getNome()))
 								break;
 						}
 
 						provas.add(new ProvaCol(disciplinas.get(itDisc), jogos.get(jogos.size() - 1)));
 
 					}
 
 				}
 				if (temp.length == 4 && temp[3].equalsIgnoreCase("x")) {
 					if (!tipoProva) {
 						int itDisc = 0;
 						for (; itDisc < disciplinas.size(); itDisc++) {
 							if (temp[1].equalsIgnoreCase(disciplinas.get(itDisc).getNome()) && 1 == disciplinas.get(itDisc).getGenero() && temp[0].equalsIgnoreCase(disciplinas.get(itDisc).getModalidade().getNome()))
 								break;
 						}
 
 						provas.add(new ProvaInd(disciplinas.get(itDisc), jogos.get(jogos.size() - 1)));
 
 					} else {
 						int itDisc = 0;
 						for (; itDisc < disciplinas.size(); itDisc++) {
 							if (temp[1].equalsIgnoreCase(disciplinas.get(itDisc).getNome()) && 1 == disciplinas.get(itDisc).getGenero() && temp[0].equalsIgnoreCase(disciplinas.get(itDisc).getModalidade().getNome()))
 								break;
 						}
 						provas.add(new ProvaCol(disciplinas.get(itDisc), jogos.get(jogos.size() - 1)));
 
 					}
 				}
 
 			}
 			in.close();
 			if (veri)
 				JOptionPane.showMessageDialog(janela, "File imported sucessfully!", "Import File", JOptionPane.INFORMATION_MESSAGE);
 
 		} catch (FileNotFoundException exc) {
 			if (veri)
 				JOptionPane.showMessageDialog(janela, "File not found!", "Import File", JOptionPane.ERROR_MESSAGE);
 			return false;
 		} catch (ArrayIndexOutOfBoundsException | NumberFormatException exc) {
 			if (veri)
 				JOptionPane.showMessageDialog(janela, "Corrupted File!", "Import File", JOptionPane.ERROR_MESSAGE);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Method to export the competitions with event to csv.
 	 * 
 	 * @param janela
 	 *            the parent component of the dialog
 	 * @param provas
 	 *            linked list with the competitions with event
 	 * @param ano
 	 *            year to export
 	 * @see Prova competition with event details
 	 */
 	public void exportProvas(Component janela, ListaLigada<Prova> provas, int ano) {
 
 		if (provas.isEmpty()) {
 			JOptionPane.showMessageDialog(janela, "File is empty!", "Export File", JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 
 		try {
 			JFileChooser fc = new JFileChooser();
 			fc.setFileFilter(new CsvFilter());
 			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 			fc.setDialogTitle("Export Competitions with Events");
 			int returnVal = fc.showSaveDialog(janela);
 			if (returnVal != JFileChooser.APPROVE_OPTION)
 				return;
 			File ficheiro = fc.getSelectedFile();
 			Formatter out = new Formatter(ficheiro + "\\IOC_Sports_OG_" + ano + ".csv");
 			ListaLigada<Prova> provaTemp = provas;
 			out.format("Sport;Discipline;Men;Women\n");
 			for (int i = 0; i < provaTemp.size(); i++) {
 				if (provaTemp.get(i).getJogosOlimpicos().getAno() != ano) {
 					provaTemp.remove(i);
 				}
 			}
 
 			for (int j = 0; j < provaTemp.size(); j++) {
 				String nomeDisc = provaTemp.get(j).getDisciplina().getNome();
 				String modalidade = provaTemp.get(j).getDisciplina().getModalidade().getNome();
 				String men = "";
 				String women = "";
 
 				if (provaTemp.get(j).getDisciplina().getGenero() == 0)
 					men = "X";
 				else if (provaTemp.get(j).getDisciplina().getGenero() == 1)
 					women = "X";
 
 				for (int k = j + 1; k < provaTemp.size(); k++) {
 					if (provaTemp.get(j).equals(provaTemp.get(k))) {
 						if (provaTemp.get(k).getDisciplina().getGenero() == 0)
 							men = "X";
 						else if (provaTemp.get(k).getDisciplina().getGenero() == 1)
 							women = "X";
 						provaTemp.remove(k);
 						k--;
 					}
 				}
 				out.format("%s;%s;%s;%s\n", modalidade, nomeDisc, men, women);
 			}
 			out.close();
 			JOptionPane.showMessageDialog(janela, "File exported sucessfully!", "Export File", JOptionPane.INFORMATION_MESSAGE);
 
 		} catch (FileNotFoundException exc) {
 			JOptionPane.showMessageDialog(janela, "Error exporting the document!", "Export File", JOptionPane.ERROR_MESSAGE);
 		}
 
 	}
 
 	/**
 	 * Private class to filter the files in the JChooseFile by CSV
 	 * 
 	 */
 	private class CsvFilter extends FileFilter {
 
 		/**
 		 * Choose if the file in File chooser is acceptable
 		 * 
 		 * @param f
 		 *            file to be evaluated
 		 * @return true if is acceptable
 		 */
 
 		@Override
 		public boolean accept(File f) {
 
 			if (f.isDirectory()) {
 				return true;
 			}
 			int ponto = f.getName().lastIndexOf(".");
 			String extensao = f.getName().substring(ponto + 1);
 
 			if (extensao != null) {
 				if (extensao.equals("csv"))
 					return true;
 			} else
 				return false;
 
 			return false;
 
 		}
 
 		/**
 		 * The description of this filter. Eg: "CSV File"
 		 * 
 		 */
 		@Override
 		public String getDescription() {
 
 			return String.format("CSV File");
 		}
 	}
 }
