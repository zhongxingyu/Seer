 package org.fecoteme.core.service;
 
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.nio.file.Files;
 import java.nio.file.StandardCopyOption;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 
 import jxl.Cell;
 import jxl.CellType;
 import jxl.DateCell;
 import jxl.FormulaCell;
 import jxl.NumberCell;
 import jxl.Sheet;
 import jxl.Workbook;
 import jxl.biff.formula.FormulaException;
 import jxl.format.CellFormat;
 import jxl.read.biff.BiffException;
 import jxl.write.DateFormat;
 import jxl.write.DateTime;
 import jxl.write.Formula;
 import jxl.write.Label;
 import jxl.write.Number;
 import jxl.write.WritableCell;
 import jxl.write.WritableCellFormat;
 import jxl.write.WritableFont;
 import jxl.write.WritableImage;
 import jxl.write.WritableSheet;
 import jxl.write.WritableWorkbook;
 import jxl.write.WriteException;
 import jxl.write.biff.RowsExceededException;
 import org.apache.log4j.Logger;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.fecoteme.core.common.RankingConstants;
 import org.fecoteme.core.model.Afiliado;
 import org.fecoteme.core.model.Bonificacion;
 import org.fecoteme.core.model.Evento;
 import org.fecoteme.core.model.Grupo;
 import org.fecoteme.core.model.Ingreso;
 import org.fecoteme.core.model.Partida;
 
 public class ExcelService {
 	/** the SearchService log message writer*/
 	private Logger l = Logger.getLogger(SearchService.class);
 	private EventoService _eventoService;
 	private InscripcionService _inscripcionService;
 	private IngresoService _ingresoService;
 	private AfiliadoService _afiliadoService;
 	private PartidaService _partidaService;
 	private BonificacionService _bonificacionService;
 		
 	/**
 	 * Constructor for SearchService class
 	 */
 	public ExcelService() {
 		l.debug("ExcelLoadService constructor");
 		_eventoService = new EventoService();
 		_inscripcionService = new InscripcionService();
 		_ingresoService = new IngresoService();
 		_afiliadoService = new AfiliadoService();
 		_partidaService = new PartidaService();
 		_bonificacionService = new BonificacionService();
 	}
 	
 	/**
 	 * This method loads an excel file into the database
 	 * @param aFileName the path of the excel file to be loaded
 	 * */
 	public void loadExcelFile(String aFileName, Shell aShell) {
 		l.debug("ExcelLoadService.loadExcelFile");
 		try {
 			//AfiliadoDAO afiliadoDAO = new AfiliadoDAO();
 			//afiliadoDAO.truncateTable();
 
 			Workbook workbook = Workbook.getWorkbook(new File(aFileName));
 
 			Sheet sheet = workbook.getSheet(0);
 
 			// now we read the data
 			
 			int rows = sheet.getRows();
 			
 			for (int i = 2; i < rows; i++) {
 				Cell rankCell = sheet.getCell(0, i);
 				if (rankCell.getContents().length() > 0) {
 					Cell apellidoCell = sheet.getCell(1, i);
 					Cell nombreCell = sheet.getCell(2, i);
 					Cell sexoCell = sheet.getCell(3, i);
 					Cell clubCell = sheet.getCell(4, i);
 					Cell puntosCell = sheet.getCell(5, i);
 					Cell nacimientoCell = sheet.getCell(6, i);
 				
 					NumberCell puntosNumberCell = (NumberCell) puntosCell;
 					DateCell dateCell = (DateCell) nacimientoCell;
 					Date birthDate = dateCell.getDate();
 					// for some reason, the date loaded is 1 day behind, so we have to add 1 day
 					birthDate.setTime(birthDate.getTime() + 1000*60*60*24);
 					Afiliado afiliado = new Afiliado();
 					afiliado.setRank(Long.parseLong(rankCell.getContents()));
 					afiliado.setLastName(apellidoCell.getContents());
 					afiliado.setFirstName(nombreCell.getContents());
 					afiliado.setGender(sexoCell.getContents());
 					afiliado.setClub(clubCell.getContents());
 					afiliado.setPoints(new Long((long)puntosNumberCell.getValue()));
 					afiliado.setBirth(birthDate);
 					afiliado.setRankFem(new Long(0));
 					afiliado.setRankLm(new Long(0));
 					afiliado.setFixedPoints(new Long(0));
 					afiliado.setLmPoints(Long.parseLong(RankingConstants.properties.getProperty("org.fecoteme.puntaje.menor")));
 					afiliado.setLmFixedPoints(new Long(0));
 					afiliado.setFemPoints(Long.parseLong(RankingConstants.properties.getProperty("org.fecoteme.puntaje.fem")));
 					afiliado.setFemFixedPoints(new Long(0));
 					
 					_afiliadoService.createAfiliado(afiliado, true);	
 				}								
 			}
 			workbook.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			l.error(e);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			l.error(e);
 		} catch (BiffException e) {
 			// TODO Auto-generated catch block
 			l.error(e);
 		} catch (ClassCastException cce) {
 			l.error(cce);
 		} catch (Exception e) {
 			l.error(e);
 		}
 		
 	}
 	
 	/**
 	 * Generates an inscription report for the event and category specified
 	 * @param aEvento the event to generate the report
 	 * @param aCategoria the categoria to filter the report
 	 * @param aDialogShell the shell used to display the save dialog
 	 * 
 	 */
 	public void generateInscriptionReport(
 			Evento aEvento, int aCategoria, Shell aDialogShell) {
 		l.debug("ExcelService.generateInscriptionReport");
 
 		FileDialog fd = new FileDialog(aDialogShell, SWT.SAVE);
         fd.setText("Guardar Reporte de Inscripcion");
         fd.setFilterPath("C:/");
         String[] filterExt = { "*.xls", "*.*" };
         fd.setFilterExtensions(filterExt);
         String fileName = fd.open();
 		
 		try {
 			WritableWorkbook wb = Workbook.createWorkbook(new File(fileName));
 			// create the inscription report sheet
 			WritableSheet inscripcion = wb.createSheet("Inscripcion", 0);
 			this._llenarHojaDeInscripcion(inscripcion, aEvento, aCategoria);
 
 			// WRITE TO THE FILE
 			wb.write();
 			wb.close();
 		} catch (FileNotFoundException e) {
 			l.error("ERROR GENERATING REPORT: " + e.getMessage());
 			e.printStackTrace();
 		} catch (IOException e) {
 			l.error("ERROR GENERATING REPORT: " + e.getMessage());
 			e.printStackTrace();
 		} catch (WriteException e) {
 			l.error("ERROR GENERATING REPORT: " + e.getMessage());
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Generates an economic report for the event specified
 	 * @param aEventName the name of the event to generate the report
 	 * @param aDialogShell the shell used to display the save dialog
 	 * 
 	 */
 	public void generateEconomicReport(
 			String aEventName, Shell aDialogShell) {
 		l.debug("ExcelService.generateInscriptionReport");
 
 		Evento evento = this._eventoService.getEventoByName(aEventName);
 		
 		List <Ingreso> ingresos = this._ingresoService.getIngresosByEvento(evento);
 		
 		FileDialog fd = new FileDialog(aDialogShell, SWT.SAVE);
         fd.setText("Guardar Reporte Economico");
         fd.setFilterPath("C:/");
         String[] filterExt = { "*.xls", "*.*" };
         fd.setFilterExtensions(filterExt);
         String fileName = fd.open();
 		
 		try {
 			WritableWorkbook wb = Workbook.createWorkbook(new File(fileName));
 			// create the inscription report sheet
 			WritableSheet inscripcion = wb.createSheet("Reporte Economico", 0);
 			this._llenarHojaDeIngresos(inscripcion, evento, ingresos);
 
 			// WRITE TO THE FILE
 			wb.write();
 			wb.close();
 		} catch (FileNotFoundException e) {
 			l.error("ERROR GENERATING REPORT: " + e.getMessage());
 			e.printStackTrace();
 		} catch (IOException e) {
 			l.error("ERROR GENERATING REPORT: " + e.getMessage());
 			e.printStackTrace();
 		} catch (WriteException e) {
 			l.error("ERROR GENERATING REPORT: " + e.getMessage());
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Generates the game sheets with the generated groups
 	 * @param aEvento the evento to generate the game sheets for
 	 * @param aGroups the list of generated groups to create the game sheets
 	 * @param aCategoria the categoria for which the game sheets will be 
 	 * generated
 	 * @param aPreferenciaTresDeCinco whether to prefer to play the best of 5 sets or best of 3
 	 * @param aDialogShell the shell used to display the save dialog
 	 */
 	public void generarHojasDeJuego(
 			Evento aEvento, List<Grupo> grupos, int aCategoria, 
 			boolean aPreferenciaTresDeCinco, Shell aDialogShell) {
 		l.debug("ExcelService.generarHojasDeJuego");
 
 		// get the file where the game sheets will be stored
 		FileDialog fd = new FileDialog(aDialogShell, SWT.SAVE);
         fd.setText("Guardar Hojas de Juego");
         fd.setFilterPath("C:/");
         String[] filterExt = { "*.xls", "*.*" };
         fd.setFilterExtensions(filterExt);
         String fileName = fd.open();
         String categoria;
         String plantilla = 
         	RankingConstants.getProperty("org.fecoteme.plantilla.hoja.juego");
         switch (aCategoria) {
         	case 0:
         		categoria = "ELITE";
                 plantilla = 
                 	RankingConstants.getProperty("org.fecoteme.plantilla.hoja.elite");
         		break;	
         	case 1:
 				categoria = "PRIMERA";
 				break;
 			case 2:
 				categoria = "SEGUNDA";
 				break;
 			case 3:
 				categoria = "TERCERA";
 				break;
 			case 4:
 				categoria = "CUARTA";
 				break;
 			case 5:
 				categoria = "QUINTA";
 				break;
 			case 6: 
 				categoria = "SEXTA";
 				break;
 			case 7: 
 				categoria = "SUB18";
 				break;
 			case 8: 
 				categoria = "SUB15";
 				break;
 			case 9: 
 				categoria = "SUB13";
 				break;
 			case 10: 
 				categoria = "SUB11";
 				break;
 			case 11: 
 				categoria = "OPEN";
 				break;
 			default:
 				categoria = "SEXTA";
 				break;
 		}
 		try {
 			if (aCategoria == 0) {
 				File from = new File(plantilla);
 				File to = new File(fileName);
 				Files.copy( from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
 				Workbook wb = 
 					Workbook.getWorkbook(to);
 				WritableWorkbook writablePlantilla = 
 					Workbook.createWorkbook(to, wb);
 				WritableSheet inscripcionSheet = writablePlantilla.getSheet("Inscripcion"); 
 				this._llenarHojaDeInscripcion(inscripcionSheet, aEvento, aCategoria);
 				writablePlantilla.write(); 
 				writablePlantilla.close();
 				wb.close();
 			} else {
 				WritableWorkbook wb = Workbook.createWorkbook(new File(fileName));
 				// create the inscription report sheet
 				WritableSheet inscripcion = wb.createSheet("Inscripcion", 0);
 				this._llenarHojaDeInscripcion(inscripcion, aEvento, aCategoria);
 				
 				Workbook plantillaWorkbook = 
 					Workbook.getWorkbook(new File(plantilla));
 				WritableWorkbook writablePlantilla = 
 					Workbook.createWorkbook(new File("temp.xls"), plantillaWorkbook);
 				WritableSheet plantillaSheet;			
 				
 				Iterator <Grupo> iter = grupos.iterator();
 				Grupo grupo;
 				Afiliado[] jugadores;
 
 				int i = 1;
 				while(iter.hasNext()) {
 					grupo = iter.next();
 					jugadores = grupo.getPlayers();
 					Character letra = new Character((char) (i + 64));
 					WritableSheet sheet = 
 						wb.createSheet("Grupo " + (i) + " (" + letra.toString() + ")", i);
 					if (jugadores.length == 3) {
 						// only the first and second play 3 sets of 5
 						if (aPreferenciaTresDeCinco) {
 							plantillaSheet = writablePlantilla.getSheet(1);
 							sheet = this._copySheet(sheet, plantillaSheet, true);
 						} else {
 							plantillaSheet = writablePlantilla.getSheet(3);
 							sheet = this._copySheet(sheet, plantillaSheet, true);
 						}
 					} else {
 						// only the first and second play 3 sets of 5
 						if (aPreferenciaTresDeCinco) {
 							plantillaSheet = writablePlantilla.getSheet(0);
 							sheet = this._copySheet(sheet, plantillaSheet, true);
 						} else {
 							plantillaSheet = writablePlantilla.getSheet(2);
 							sheet = this._copySheet(sheet, plantillaSheet, true);
 						}					
 					}
 					
 					// populates the sheet with the players info
 					this._llenarHoja(sheet, jugadores, categoria, i);
 					
 					i++;
 				}
 				// NOW ADD FINAL ROUND DRAW SHEET
 				WritableSheet finalRoundDrawSheet = wb.createSheet("Rifa", i);
 				plantillaSheet = writablePlantilla.getSheet(9);
 				finalRoundDrawSheet = this._copySheet(finalRoundDrawSheet, plantillaSheet, false);
 				
 				// GENERATE THE DRAW
 				this._generateDraw(grupos.size());
 				
 				// NOW READ THE GENERATED DRAW AND FILL THE DRAW SHEET WITH IT
 				this._fillDrawSheet(finalRoundDrawSheet);
 				
 				// NOW ADD THE FINAL ROUND SHEET
 				WritableSheet finalRoundSheet = wb.createSheet("Llave", i+1);
 
 				if (grupos.size() <= 4) { // llave de 8
 					plantillaSheet = writablePlantilla.getSheet(4);
 				} else if (grupos.size() <= 8) { // llave de 16
 					plantillaSheet = writablePlantilla.getSheet(5);
 				} else if (grupos.size() <= 16) { // llave de 32
 					plantillaSheet = writablePlantilla.getSheet(6);
 				} else { // llave de 64
 					plantillaSheet = writablePlantilla.getSheet(7);
 				}
 				finalRoundSheet = this._copySheet(finalRoundSheet, plantillaSheet, false);
 				
 				// WRITE TO THE FILE
 				wb.write();
 				wb.close();				
 			}
 		} catch (FileNotFoundException fnfe) {
 			// TODO Auto-generated catch block
 			fnfe.printStackTrace();
 		} catch (IOException ioe) {
 			// TODO Auto-generated catch block
 			ioe.printStackTrace();
 		} catch (WriteException we) {
 			// TODO Auto-generated catch block
 			we.printStackTrace();
 		} catch (BiffException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 	}
 	
 	/**
 	 * Populates the inscription sheet with the players of the event
 	 * @param aSheet the target sheet
 	 * @param aEvento the evento to generate the inscription sheet
 	 * @param aCategoria the categoria for the inscription sheet
 	 */
 	private void _llenarHojaDeInscripcion(WritableSheet aSheet, Evento aEvento, int aCategoria) {
 		l.debug("ExcelService._llenarHojaDeInscripcion");
 				
 		try {
 			// EVENTO ROW
 			Label evento = new Label(0, 0, aEvento.getName());
 			aSheet.addCell(evento);
 	        String categoria;
 	        switch (aCategoria) {
 				case 0:
 					categoria = "ELITE";
 					break;
 	        	case 1:
 					categoria = "PRIMERA";
 					break;
 				case 2:
 					categoria = "SEGUNDA";
 					break;
 				case 3:
 					categoria = "TERCERA";
 					break;
 				case 4:
 					categoria = "CUARTA";
 					break;
 				case 5:
 					categoria = "QUINTA";
 					break;
 				case 6: 
 					categoria = "SEXTA";
 					break;
 				case 7: 
 					categoria = "SUB18";
 					break;
 				case 8: 
 					categoria = "SUB15";
 					break;
 				case 9: 
 					categoria = "SUB13";
 					break;
 				case 10: 
 					categoria = "SUB11";
 					break;
 				case 11: 
 					categoria = "OPEN";
 					break;
 				default:
 					categoria = "SEXTA";
 					break;
 			}
 			// CATEGORIA ROW
 			String catString = "REPORTE DE INSCRIPCION PARA " + categoria;
 			Label category = new Label(0, 1, catString);
 			aSheet.addCell(category);
 			// COLUMN NAME ROW
 			Label carneTitle = new Label(0, 2, "CARNE");
 			aSheet.addCell(carneTitle);
 			Label nameTitle = new Label(1, 2, "NOMBRE");
 			aSheet.addCell(nameTitle);
 			Label clubTitle = new Label(2, 2, "CLUB");
 			aSheet.addCell(clubTitle);
 			Label rankTitle = new Label(3, 2, "RANKING");
 			aSheet.addCell(rankTitle);
 			Label pointsTitle = new Label(4, 2, "PUNTOS");
 			aSheet.addCell(pointsTitle);
 
 			// get the afiliados incribed in the evento for the category specified
 			List <Afiliado> inscritos = 
 				this._inscripcionService.getAfiliadosInscritosAEventoByCategoria(
 						aEvento, aCategoria);
 			Iterator <Afiliado> iter = inscritos.iterator();
 			Afiliado afiliado;
 			short i = 3;
 			Number carne, points;
 			Label name, club;
 			while (iter.hasNext()) {
 				afiliado = iter.next();
 				
 				// CARNE CELL
 				carne = new Number(0, i, afiliado.getId());
 				aSheet.addCell(carne);
 				// NOMBRE + APELLIDO CELL
 				name = new Label(1, i, afiliado.getFirstName() + 
 						" " + afiliado.getLastName());
 				aSheet.addCell(name);
 				// CLUB CELL
 				club = new Label(2, i, afiliado.getClub());
 				aSheet.addCell(club);
 				// RANKING CELL
 				if (afiliado.getRank() == 0) {
 					Label rankLabel = new Label(3, i, "NUEVO AFILIADO");
 					aSheet.addCell(rankLabel);
 				} else {
 					Number rankNumber = new Number(3, i, afiliado.getRank());
 					switch (aEvento.getType()) {
 					case Evento.RANKING_MAYOR: // mayor
 						rankNumber = new Number(3, i, afiliado.getRank());
 						break;
 					case Evento.RANKING_MENOR: // menor
 						if (aEvento.getGenderRestricted() == 1 && aEvento.getGender().equals("F")) {
 							rankNumber = new Number(3, i, afiliado.getRankLmFem());
 						} else {
 							rankNumber = new Number(3, i, afiliado.getRankLm());	
 						}
 						break;
 					case Evento.OPEN: // open
 						if (aEvento.getGenderRestricted() == 1) {
 							if (aEvento.getGender().equals("F")) {
 								rankNumber = new Number(3, i, afiliado.getRankFem());
 							} else {
 								rankNumber = new Number(3, i, afiliado.getRank());		
 							}
 						} else {
 							rankNumber = new Number(3, i, afiliado.getRank());
 						}
 						break;
 					}
 					aSheet.addCell(rankNumber);
 				}
 				points = new Number(4, i, afiliado.getPoints());
 				// PUNTOS CELL
 				switch (aEvento.getType()) {
 				case Evento.RANKING_MAYOR: // mayor
 					points = new Number(4, i, afiliado.getPoints());
 					break;
 				case Evento.RANKING_MENOR: // menor
 					if (aEvento.getGenderRestricted() == 1 && aEvento.getGender().equals("F")) {
 						points = new Number(4, i, afiliado.getLmFemPoints());	
 					} else {
 						points = new Number(4, i, afiliado.getLmPoints());
 					}
 					break;
 				case Evento.OPEN: // open
 					if (aEvento.getGenderRestricted() == 1) {
 						if (aEvento.getGender().equals("F")) {
 							points = new Number(4, i, afiliado.getFemPoints());
 						} else {
 							points = new Number(4, i, afiliado.getPoints());	
 						}
 					} else {
 						points = new Number(4, i, afiliado.getPoints());
 					}
 					break;
 				}
 				
 				aSheet.addCell(points);
 
 				i++;
 			}
 
 			
 		} catch (RowsExceededException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (WriteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 	}
 
 	/**
 	 * Populates the ingresos sheet with the players of the event
 	 * @param aSheet the target sheet
 	 * @param aEvento the evento to generate the inscription sheet
 	 * @param ingresos all the ingresos made for the event
 	 */
 	private void _llenarHojaDeIngresos(WritableSheet aSheet, Evento aEvento, List<Ingreso> ingresos) {
 		l.debug("ExcelService._llenarHojaDeIngresos");
 				
 		try {
 			// EVENTO ROW
 			Label evento = new Label(0, 0, aEvento.getName());
 			aSheet.addCell(evento);
 			// TITULO ROW
 			String catString = "REPORTE DE INGRESOS";
 			Label category = new Label(0, 1, catString);
 			aSheet.addCell(category);
 			// COLUMN NAME ROW
 			Label carneTitle = new Label(0, 2, "CARNE");
 			aSheet.addCell(carneTitle);
 			Label categoriaTitle = new Label(1, 2, "CATEGORIA");
 			aSheet.addCell(categoriaTitle);
 			Label tipoTitle = new Label(2, 2, "MONTO");
 			aSheet.addCell(tipoTitle);
 
 			Iterator <Ingreso> iter = ingresos.iterator();
 			Ingreso ingreso;
 			short i = 3;
 			Number carne, categoria, tipo;
 			while (iter.hasNext()) {
 				ingreso = iter.next();
 				
 				// CARNE CELL
 				carne = new Number(0, i, ingreso.getAfiliado().getId());
 				aSheet.addCell(carne);
 				// CATEGORIA CELL
 				categoria = new Number(1, i, ingreso.getCategoria());
 				aSheet.addCell(categoria);
 				// TIPO CELL
 				int type = ingreso.getTipo();
 				switch(type) {
 				case 1:
 					tipo = new Number(2, i, 4000);
 					break;
 				case 2:
 					tipo = new Number(2, i, 2000);
 					break;
 				default:
 					tipo = new Number(2, i, 4000);
 					break;
 				}
 				
 				aSheet.addCell(tipo);
 
 				i++;
 			}
 
 			
 		} catch (RowsExceededException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (WriteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 	}
 	
 	/**
 	 * Copies the contents of a given template into a sheet and returns
 	 * the copied sheet
 	 * @param aSheet the target sheet
 	 * @param aTemplate the origin sheet
 	 * @param aIncludeLogo whether or not to include the logo
 	 */
 	private WritableSheet _copySheet(WritableSheet aSheet, WritableSheet aTemplate, boolean aIncludeLogo) {
 		l.debug("ExcelService._copySheet");
 		
 		int numRows = aTemplate.getRows();
 		int numCols = aTemplate.getColumns();
 		WritableCell readCell;
 		WritableCell newCell;
 		FormulaCell formulaCell;
 		CellFormat readFormat;
 		WritableCellFormat newFormat;
 		String logo = 
 			RankingConstants.getProperty("org.fecoteme.plantilla.hoja.juego.logo");
 		try {
 			// copy all sheet contents
 			for (int i = 0 ; i < numRows ; i++) {
 				for (int j = 0 ; j < numCols ; j++) {
 			      readCell = aTemplate.getWritableCell(j, i);
 			      if (readCell.getType().equals(CellType.BOOLEAN_FORMULA) || 
 			    		  readCell.getType().equals(CellType.NUMBER_FORMULA) || 
 			    		  readCell.getType().equals(CellType.STRING_FORMULA) ||
 			    		  readCell.getType().equals(CellType.FORMULA_ERROR)) {
 			    	  formulaCell = (FormulaCell) readCell;
 			    	  
 			    	  String formula = formulaCell.getFormula();
 			    	  formula = formula.replace(".0", "").replace("false", "0");
 			    	  if (formula.indexOf("V8:X200")!= -1) {
 			    		  formula = formula.replace("V8:X200", "$V$8:$X$200");
 			    	  }
 			    	  if (formula.indexOf("Inscripcion!A1:E200")!= -1) {
 			    		  formula = formula.replace("Inscripcion!A1:E200", "Inscripcion!$A$1:$E$200");
 			    	  }
 			    	  if (formula.indexOf("Rifa!A1:C100")!= -1) {
 			    		  formula = formula.replace("Rifa!A1:C100", "Rifa!$A$1:$C$100");
 			    	  }
 			    	  //l.debug(formula);
 			    	  newCell = new Formula(j, i, formula);
 			    	  
 			      } else {
 				      newCell = readCell.copyTo(j, i);		    	  
 			      }
 			      readFormat = readCell.getCellFormat();
 			      if (readFormat != null) {
 				      newFormat = new WritableCellFormat(readFormat);
 				      newCell.setCellFormat(newFormat);    	  
 			      }
 			      aSheet.addCell(newCell);
 				}
 			}
 			if (aIncludeLogo) {
 				// adds the picture
 				final double CELL_DEFAULT_HEIGHT = 17;
 				final double CELL_DEFAULT_WIDTH = 64;
 				File imageFile = new File(logo);
 				BufferedImage input = ImageIO.read(imageFile);
 				ByteArrayOutputStream baos = new ByteArrayOutputStream();
 				ImageIO.write(input, "PNG", baos);
 				aSheet.addImage(new WritableImage(2,2,input.getWidth() / CELL_DEFAULT_WIDTH,
 					input.getHeight() / CELL_DEFAULT_HEIGHT,baos.toByteArray()));
 				/*WritableImage wi = new WritableImage(2, 2, 7, 3, new File(logo));
 	            aSheet.addImage(wi);*/				
 			}
 		} catch (WriteException we) {
 			we.printStackTrace();
 		} catch (FormulaException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return aSheet;
 	}
 	/**
 	 * Populates the given sheet with the information of the players specified
 	 * @param aSheet the sheet to be filled
 	 * @param aJugadores the players to fill the sheet
 	 * @param aCategoria the categoria of the group
 	 * @param aGroupNumber the number of the group
 	 */
 	private void _llenarHoja(WritableSheet aSheet, Afiliado[] aJugadores, 
 			String aCategoria, int aGroupNumber) {
 		l.debug("ExcelService._llenarHoja");
 
 		try {
 			// set the date
 			WritableCell cell = aSheet.getWritableCell("H7");
 			Date now = Calendar.getInstance().getTime();
 			DateFormat customDateFormat = new DateFormat("dd/mm/yyyy");
 			WritableFont font = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD, true);
 			WritableCellFormat dateFormat =	new WritableCellFormat(font, customDateFormat);
 			DateTime dateCell = new DateTime(7, 6, now, dateFormat);
 			aSheet.addCell(dateCell); 
 			// set the category
 			cell = aSheet.getWritableCell("D9");
 			Label category = new Label(3, 8, aCategoria, cell.getCellFormat());
 			aSheet.addCell(category);
 			// set the group number
 			cell = aSheet.getWritableCell("F9");
 			Character letra = new Character((char) (aGroupNumber + 64));
 			String groupNumber = aGroupNumber + " (" + letra.toString() + ")";
 			Label number = 
 				new Label(5, 8, groupNumber, cell.getCellFormat());
 			aSheet.addCell(number);
 			// set the player information
 			int baseLocation = 12;
 			Afiliado afiliado;
 			for (int i = 0; i < aJugadores.length; i++) {
 				afiliado = aJugadores[i];
 				int rowNum = baseLocation + i;
 				// set the id
 				cell = aSheet.getWritableCell("C" + rowNum);
 				Number id = new Number(2, rowNum - 1, afiliado.getId(), cell.getCellFormat());
 				aSheet.addCell(id);
 				// set the name
 				/*cell = aSheet.getWritableCell("D" + rowNum);
 				Label name = new Label(
 					3, rowNum - 1, 
 					afiliado.getFirstName() + " " + afiliado.getLastName(),
 					cell.getCellFormat());
 				aSheet.addCell(name);
 				// set the place
 				cell = aSheet.getWritableCell("E" + rowNum);
 				Label club = new Label(4, rowNum - 1, afiliado.getClub(), cell.getCellFormat());
 				aSheet.addCell(club);
 				// set the ranking
 				cell = aSheet.getWritableCell("F" + rowNum);
 				Number rank = new Number(5, rowNum - 1, afiliado.getRank(), cell.getCellFormat()); 
 				aSheet.addCell(rank);
 				// set the points
 				cell = aSheet.getWritableCell("G" + rowNum);
 				Number points = new Number(6, rowNum - 1, afiliado.getPoints(), cell.getCellFormat());
 				aSheet.addCell(points);*/
 			}
 		} catch (RowsExceededException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (WriteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Generates an excel spreadsheet with all the afiliados, ordered by 
 	 * their points and ranking
 	 * @param aDialogShell the shell used to display the save dialog
 	 * @param aCategoria the categoria to generate the spreadsheet for
 	 * 
 	 */
 	public void generateHojaPosiciones(Shell aDialogShell, int aCategoria) {
 		l.debug("ExcelService.generateHojaPosiciones");
 		
 		FileDialog fd = new FileDialog(aDialogShell, SWT.SAVE);
         fd.setText("Guardar Hoja de Posiciones");
         fd.setFilterPath("C:/");
         String[] filterExt = { "*.xls", "*.*" };
         fd.setFilterExtensions(filterExt);
         String fileName = fd.open();
 		
 		try {
 			WritableWorkbook wb = Workbook.createWorkbook(new File(fileName));
 			// create the inscription report sheet
 			WritableSheet ranking = wb.createSheet("Ranking", 0);
 			this._llenarHojaDePosiciones(ranking, aCategoria);
 
 			// WRITE TO THE FILE
 			wb.write();
 			wb.close();
 		} catch (FileNotFoundException e) {
 			l.error("ERROR GENERATING REPORT: " + e.getMessage());
 			e.printStackTrace();
 		} catch (IOException e) {
 			l.error("ERROR GENERATING REPORT: " + e.getMessage());
 			e.printStackTrace();
 		} catch (WriteException e) {
 			l.error("ERROR GENERATING REPORT: " + e.getMessage());
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Populates the ranking sheet with all the afiliados
 	 * @param aSheet the target sheet
 	 * @param aCategoria the categoria to generate the spreadsheet for
 	 */
 	private void _llenarHojaDePosiciones(WritableSheet aSheet, int aCategoria) {
 		l.debug("ExcelService._llenarHojaDePosiciones");
 				
 		try {
 			// TITLE ROW
 			String catString = "HOJA DE POSICIONES";
 			Label category = new Label(0, 1, catString);
 			aSheet.addCell(category);
 			// COLUMN NAME ROW
 			Label carneTitle = new Label(0, 2, "CARNE");
 			aSheet.addCell(carneTitle);
 			Label nameTitle = new Label(1, 2, "NOMBRE");
 			aSheet.addCell(nameTitle);
 			Label clubTitle = new Label(2, 2, "CLUB");
 			aSheet.addCell(clubTitle);
 			Label rankTitle = new Label(3, 2, "RANKING");
 			aSheet.addCell(rankTitle);
 			Label pointsTitle = new Label(4, 2, "PUNTOS");
 			aSheet.addCell(pointsTitle);
 
 			// get all the afiliados
 			List <Afiliado> afiliados = 
 				this._afiliadoService.getAllAfiliadosByPoints(aCategoria);
 			Iterator <Afiliado> iter = afiliados.iterator();
 			Afiliado afiliado;
 			short i = 3;
 			Number carne, points;
 			Label name, club;
 			while (iter.hasNext()) {
 				afiliado = iter.next();
 				
 				// CARNE CELL
 				carne = new Number(0, i, afiliado.getId());
 				aSheet.addCell(carne);
 				// NOMBRE + APELLIDO CELL
 				name = new Label(1, i, afiliado.getFirstName() + 
 						" " + afiliado.getLastName());
 				aSheet.addCell(name);
 				// CLUB CELL
 				club = new Label(2, i, afiliado.getClub());
 				aSheet.addCell(club);
 				// RANKING CELL
 				if (afiliado.getRank() == 0) {
 					Label rankLabel = new Label(3, i, "NUEVO AFILIADO");
 					aSheet.addCell(rankLabel);
 				} else {
 					Number rankNumber;
 					if (aCategoria == 0) {
 						rankNumber = new Number(3, i, afiliado.getRank());
 					} else if (aCategoria == 5) {
 						rankNumber = new Number(3, i, afiliado.getRankFem());
 					} else if (aCategoria > 0 && aCategoria < 5) { // liga menor
 						rankNumber = new Number(3, i, afiliado.getRankLm());
 					} else { // liga menor fem
 						rankNumber = new Number(3, i, afiliado.getRankLmFem());
 					}
 					aSheet.addCell(rankNumber);
 				}
 				// PUNTOS CELL
 				if (aCategoria == 0) {
 					points = new Number(4, i, afiliado.getPoints());
 				} else if (aCategoria == 5) {
 					points = new Number(4, i, afiliado.getFemPoints());
 				} else if (aCategoria > 0 && aCategoria < 5) { // liga menor
 					points = new Number(4, i, afiliado.getLmPoints());
 				} else { // liga menor fem
 					points = new Number(4, i, afiliado.getLmFemPoints());
 				}
 				
 				aSheet.addCell(points);
 
 				i++;
 			}
 
 			
 		} catch (RowsExceededException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (WriteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 	}
 
 	/**
 	 * Calls the external application to generate the draw
 	 * @param aNumeroGrupos the number of grupos to generate the draw for 
 	 */
 	private void _generateDraw(int aNumeroGrupos) {
 		l.debug("ExcelService._generateDraw: " + new Integer(aNumeroGrupos).toString());
 		try {
 			// delete the output file (if present)
 			this.delete("llaves.txt");
             // Se lanza el ejecutable.
 			String[] llaves = new String[] {"\"" + RankingConstants.getProperty("org.fecoteme.generador.llaves") + "\""};
 
 			Process p = 
 				Runtime.getRuntime().exec(llaves); 
 	        OutputStream os = p.getOutputStream();
 	        InputStream is = p.getInputStream();
 	        // spawn two threads to handle I/O with child while we wait for it to complete.
 	        new Thread( new Receiver( is ) ).start();
 	        new Thread( new Sender( os, aNumeroGrupos ) ).start();
 	        try
 	            {
 	            p.waitFor();
 	            }
 	        catch ( InterruptedException e )
 	            {
 	            Thread.currentThread().interrupt();
 	            }
 	        System.out.println( "Child done" );
 	        // at this point the child is complete.  All of its output may or may not have been processed however.
 	        // The Receiver thread will continue until it has finished processing it.
 	        // You must close the streams even if you never use them!  In this case the threads close is and os.
 	        p.getErrorStream().close();
 
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 	}
 
 	/**
 	 * Fills the draw sheet with the generated draw
 	 * @param aDrawSheet the draw sheet to be filled
 	 */
 	private WritableSheet _fillDrawSheet(WritableSheet aDrawSheet) {
 		l.debug("ExcelService._fillDrawSheet");
 		try {
 		    BufferedReader in = new BufferedReader(new FileReader("llaves.txt"));
 		    String str;
 		    int i = 1;
 		    while ((str = in.readLine()) != null) {
 		    	Label jugador = new Label(0, i, str);
 		    	try {
 					aDrawSheet.addCell(jugador);
 				} catch (RowsExceededException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (WriteException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
             	i++;
 		    }
 		    in.close();
 		} catch (IOException e) {
 		}
 
 		
 		return aDrawSheet;
 	}
 
 	/**
 	 * Deletes the file passed as parameter from the file system
 	 * @param fileName the name (path) of the file to be deleted 
 	 */
 	public void delete(String fileName) {
 		try {
 			// Construct a File object for the file to be deleted.
 			File target = new File(fileName);
 			
 			if (!target.exists()) {
 				l.debug("File " + fileName + " not present to begin with!");
 				return;
 			}
 
 			// Quick, now, delete it immediately:
 			if (target.delete())
 				l.debug("** Deleted " + fileName + " **");
 			else
 		        l.error("Failed to delete " + fileName);
 		} catch (SecurityException e) {
 			l.error("Unable to delete " + fileName + "(" + e.getMessage() + ")");
 		}
 	}	
 	
 	/**
 	 * Processes a spreadsheet with event results
 	 * @param aFileName the path to the file to process
 	 * @param aEvent the evento to assign the results to
 	 */
 	public ArrayList<Boolean> processResultsSpreadsheet(String aFileName, Evento aEvento) {
 		ArrayList<Boolean> results = new ArrayList<Boolean>();
 		try {
 			Workbook workbook = Workbook.getWorkbook(new File(aFileName));
 			int sheetsNumber = workbook.getNumberOfSheets();
 			
 			// the last sheet is the final draw
 			// the one before the last one contains the generated draw
 			// the first is the list of players
 			for (int i = 1; i < sheetsNumber - 2; i++) {
 				l.debug("Processing group: " + i);
 				boolean groupSuccess = 
 					this._processGroup(workbook.getSheet(i), aEvento);
 				results.add(new Boolean(groupSuccess));
 			}
 			
 			double drawSize = this._getDrawSize(sheetsNumber);
 			boolean drawSuccess = 
 				this._processFinalDraw(workbook.getSheet(sheetsNumber - 1), drawSize, aEvento);
 			results.add(new Boolean(drawSuccess));
 			
 		} catch(Exception e) {
 			l.error("Unable to process the spreadsheet " + e.getMessage());
 			e.printStackTrace();
 			results.add(new Boolean(false));
 		}
 		return results;
 	}
 	
 	/**
 	 * Processes a spreadsheet with event results
 	 * @param aFileName the path to the file to process
 	 * @param aEvent the evento to assign the results to
 	 */
 	public boolean processEliteSpreadsheet(String aFileName, Evento aEvento) {
 		boolean result = true;
 		try {
 			Workbook workbook = Workbook.getWorkbook(new File(aFileName));
 			
 			Sheet partidasSheet = workbook.getSheet(1);
 			
 			// the last sheet is the final draw
 			// the one before the last one contains the generated draw
 			// the first is the list of players
 			char resultsColumn = 'L';
 			char playersColumn = 'C';
 			int padding = 22;
 			for (int i = 0; i < 28; i++) {
 				int resultRow = padding + (2 * i);
 				StringBuffer resultCellString = 
 					new StringBuffer(String.valueOf(resultsColumn)).append(String.valueOf(resultRow));
 				Cell resultCell = partidasSheet.getCell(resultCellString.toString());
 				l.error("resultCell: " + resultCell.getContents());
 				
 				if (resultCell.getContents().length() > 0 &&
 					!resultCell.getContents().equals("0")) {
 					StringBuffer player1Cell = 
 						new StringBuffer(String.valueOf(playersColumn)).append(String.valueOf(resultRow));
 					Cell player1ID = partidasSheet.getCell(player1Cell.toString()); 
 					l.error("player1ID: " + player1ID.getContents());
 					
 					StringBuffer player2Cell = 
 						new StringBuffer(String.valueOf(playersColumn)).append(String.valueOf(resultRow + 1));
 					Cell player2ID = partidasSheet.getCell(player2Cell.toString());
 					l.error("player2ID: " + player2ID.getContents());
 					if (resultCell.getContents().equals(player1ID.getContents())) {
 						Afiliado winner = 
 							this._afiliadoService.getAfiliadoById(
 									Long.parseLong(player1ID.getContents()));
 						Afiliado loser = 
 							this._afiliadoService.getAfiliadoById(
 									Long.parseLong(player2ID.getContents()));
 						Partida p = new Partida(aEvento, winner, loser);
 						//this._partidaService.createPartida(p);						
 					} else {
 						Afiliado winner = 
 							this._afiliadoService.getAfiliadoById(
 									Long.parseLong(player2ID.getContents()));
 						Afiliado loser = 
 							this._afiliadoService.getAfiliadoById(
 									Long.parseLong(player1ID.getContents()));
 						Partida p = new Partida(aEvento, winner, loser);
 						//this._partidaService.createPartida(p);						
 					}
 				} else if (resultCell.getContents().length() == 0) {
 					// result is missing!!!!
 					return false;
 				}
 			}
 			
 			// now add bonificaciones
 			
 		} catch(Exception e) {
 			l.error("Unable to process the spreadsheet " + e.getMessage());
 			result = false;
 		}
 		return result;
 	}
 	
 	/**
 	 * 
 	 */
 	public double _getDrawSize(int sheetsNumber) {
 		double drawSize = 8; // 8 is the minimum
 		sheetsNumber = sheetsNumber - 3;
 		int players = sheetsNumber * 2;
 		int i = 3;
 		while (drawSize < players) {
 			i++;
 			drawSize = Math.pow(2, i);
 		}
 		return drawSize;
 	}
 	
 	/**
 	 * Processes a group sheet
 	 * @param aGroupSheet the group sheet to be processed
 	 * @param aEvento the evento to add the results to
 	 */
 	private boolean _processGroup(Sheet aGroupSheet, Evento aEvento) {
 		// in the groups sheet:
 		// column C(12-15) contains the ids of the players 
 		// (12-14 in groups of 3)
 		// (12-15 in groups of 4)
 		// J17 is the winner column title in best of 5
 		// H17 is the winner column title in best of 3
 		
 		// start by getting the player ids
 		Cell player1ID = aGroupSheet.getCell("C12");
 		Cell player2ID = aGroupSheet.getCell("C13");
 		Cell player3ID = aGroupSheet.getCell("C14");
 		Cell player4ID = aGroupSheet.getCell("C15");
 		boolean grupos3 = false;
 		if (player4ID.getContents().length() == 0) {
 			grupos3 = true;
 		}
 		String winnerColumn = aGroupSheet.getCell("J17").getContents();
 		boolean bestOf5 = winnerColumn.indexOf("Ganador") != -1;
 		if (grupos3) {
 			// 3 matches played (1-3/1-2/2-3)
 			Cell match1WinnerCell = bestOf5 ? aGroupSheet.getCell("J18") : aGroupSheet.getCell("H18");
 			if (match1WinnerCell.getContents().length() == 0) {
 				return false;
 			}
 			if (match1WinnerCell.getContents().equals("1")) {
 				Afiliado winner = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player1ID.getContents()));
 				Afiliado loser = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player3ID.getContents()));
 				Partida p = new Partida(aEvento, winner, loser);
 				this._partidaService.createPartida(p);
 			} else {
 				Afiliado winner = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player3ID.getContents()));
 				Afiliado loser = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player1ID.getContents()));
 				Partida p = new Partida(aEvento, winner, loser);
 				this._partidaService.createPartida(p);
 			}
 			
 			Cell match2WinnerCell = bestOf5 ? aGroupSheet.getCell("J20") : aGroupSheet.getCell("H20");
 			if (match2WinnerCell.getContents().length() == 0) {
 				return false;
 			}
 			if (match2WinnerCell.getContents().equals("1")) {
 				Afiliado winner = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player1ID.getContents()));
 				Afiliado loser = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player2ID.getContents()));
 				Partida p = new Partida(aEvento, winner, loser);
 				this._partidaService.createPartida(p);
 			} else {
 				Afiliado winner = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player2ID.getContents()));
 				Afiliado loser = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player1ID.getContents()));
 				Partida p = new Partida(aEvento, winner, loser);
 				this._partidaService.createPartida(p);
 			}
 			
 			Cell match3WinnerCell = bestOf5 ? aGroupSheet.getCell("J22") : aGroupSheet.getCell("H22");
 			if (match3WinnerCell.getContents().length() == 0) {
 				return false;
 			}
 			if (match3WinnerCell.getContents().equals("2")) {
 				Afiliado winner = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player2ID.getContents()));
 				Afiliado loser = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player3ID.getContents()));
 				Partida p = new Partida(aEvento, winner, loser);
 				this._partidaService.createPartida(p);
 			} else {
 				Afiliado winner = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player3ID.getContents()));
 				Afiliado loser = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player2ID.getContents()));
 				Partida p = new Partida(aEvento, winner, loser);
 				this._partidaService.createPartida(p);
 			}
 		} else {
 			// 6 matches played (1-3/2-4/1-2/3-4/1-4/2-3)
 			Cell match1WinnerCell = bestOf5 ? aGroupSheet.getCell("J18") : aGroupSheet.getCell("H18");
 			if (match1WinnerCell.getContents().length() == 0) {
 				return false;
 			}
 			if (match1WinnerCell.getContents().equals("1")) {
 				Afiliado winner = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player1ID.getContents()));
 				Afiliado loser = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player3ID.getContents()));
 				Partida p = new Partida(aEvento, winner, loser);
 				this._partidaService.createPartida(p);
 			} else {
 				Afiliado winner = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player3ID.getContents()));
 				Afiliado loser = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player1ID.getContents()));
 				Partida p = new Partida(aEvento, winner, loser);
 				this._partidaService.createPartida(p);
 			}
 			
 			Cell match2WinnerCell = bestOf5 ? aGroupSheet.getCell("J20") : aGroupSheet.getCell("H20");
 			if (match2WinnerCell.getContents().length() == 0) {
 				return false;
 			}
 			if (match2WinnerCell.getContents().equals("2")) {
 				Afiliado winner = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player2ID.getContents()));
 				Afiliado loser = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player4ID.getContents()));
 				Partida p = new Partida(aEvento, winner, loser);
 				this._partidaService.createPartida(p);
 			} else {
 				Afiliado winner = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player4ID.getContents()));
 				Afiliado loser = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player2ID.getContents()));
 				Partida p = new Partida(aEvento, winner, loser);
 				this._partidaService.createPartida(p);
 			}
 			
 			Cell match3WinnerCell = bestOf5 ? aGroupSheet.getCell("J22") : aGroupSheet.getCell("H22");
 			if (match3WinnerCell.getContents().length() == 0) {
 				return false;
 			}
 			if (match3WinnerCell.getContents().equals("1")) {
 				Afiliado winner = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player1ID.getContents()));
 				Afiliado loser = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player2ID.getContents()));
 				Partida p = new Partida(aEvento, winner, loser);
 				this._partidaService.createPartida(p);
 			} else {
 				Afiliado winner = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player2ID.getContents()));
 				Afiliado loser = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player1ID.getContents()));
 				Partida p = new Partida(aEvento, winner, loser);
 				this._partidaService.createPartida(p);
 			}
 			
 			Cell match4WinnerCell = bestOf5 ? aGroupSheet.getCell("J24") : aGroupSheet.getCell("H24");
 			if (match4WinnerCell.getContents().length() == 0) {
 				return false;
 			}
			if (match4WinnerCell.getContents().equals("3")) {
 				Afiliado winner = 
 					this._afiliadoService.getAfiliadoById(
							Long.parseLong(player3ID.getContents()));
 				Afiliado loser = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player4ID.getContents()));
 				Partida p = new Partida(aEvento, winner, loser);
 				this._partidaService.createPartida(p);
 			} else {
 				Afiliado winner = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player4ID.getContents()));
 				Afiliado loser = 
 					this._afiliadoService.getAfiliadoById(
							Long.parseLong(player3ID.getContents()));
 				Partida p = new Partida(aEvento, winner, loser);
 				this._partidaService.createPartida(p);
 			}
 			
 			Cell match5WinnerCell = bestOf5 ? aGroupSheet.getCell("J26") : aGroupSheet.getCell("H26");
 			if (match5WinnerCell.getContents().length() == 0) {
 				return false;
 			}
 			if (match5WinnerCell.getContents().equals("1")) {
 				Afiliado winner = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player1ID.getContents()));
 				Afiliado loser = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player4ID.getContents()));
 				Partida p = new Partida(aEvento, winner, loser);
 				this._partidaService.createPartida(p);
 			} else {
 				Afiliado winner = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player4ID.getContents()));
 				Afiliado loser = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player1ID.getContents()));
 				Partida p = new Partida(aEvento, winner, loser);
 				this._partidaService.createPartida(p);
 			}
 			
 			Cell match6WinnerCell = bestOf5 ? aGroupSheet.getCell("J28") : aGroupSheet.getCell("H28");
 			if (match6WinnerCell.getContents().length() == 0) {
 				return false;
 			}
 			if (match6WinnerCell.getContents().equals("2")) {
 				Afiliado winner = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player2ID.getContents()));
 				Afiliado loser = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player3ID.getContents()));
 				Partida p = new Partida(aEvento, winner, loser);
 				this._partidaService.createPartida(p);
 			} else {
 				Afiliado winner = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player3ID.getContents()));
 				Afiliado loser = 
 					this._afiliadoService.getAfiliadoById(
 							Long.parseLong(player2ID.getContents()));
 				Partida p = new Partida(aEvento, winner, loser);
 				this._partidaService.createPartida(p);
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * Processes the final draw sheet
 	 * @param aFinalDrawSheet the final draw sheet to be processed
 	 * @param aDrawSize the draw size
 	 * @param aEvento the evento to add the results to
 	 */
 	private boolean _processFinalDraw(Sheet aFinalDrawSheet, double aDrawSize, Evento aEvento) {
 		// in the final draw sheet:
 		// player ids are in column D (starting in D8)
 		// if in the id column we have a "-", the other player has to be awarded with a BYE
 		// final draw size can be calculated based on the number of groups (#groups * 2)
 		
 		// rounds
 		int rounds = (int)(Math.log(aDrawSize)/Math.log(2));
 		
 		char startingColumn = 'G';
 		int drawPadding = 7; 
 		String [] first8 = new String[8];
 		int pos = 0;
 		
 		for (int i = rounds; i > 0; i--) {
 			int roundPad = (int)Math.pow(2, i - 1);
 			for (int j = roundPad + drawPadding; j < aDrawSize + drawPadding;) {
 				//l.error("j: " + j);
 				char column = (char)((int)startingColumn + (i - 1));
 				
 				StringBuffer result = new StringBuffer(String.valueOf(column)).append(String.valueOf(j));
 				Cell resultCell = aFinalDrawSheet.getCell(result.toString());
 				
 				// add winner to the first8 array
 				if (pos < 8) {
 					if (Arrays.asList(first8).indexOf(resultCell.getContents()) == -1) {
 						first8[pos] = resultCell.getContents();
 						pos++;
 					}
 				}
 				//l.error("Result: " + resultCell.getContents());
 				
 				// players are 1 column before the result
 				char playersColumn = (char)((int) column - 1);
 				// if we are processing the first round, the players ids are 2 columns to the left 
 				if (i == 1) {
 					playersColumn = (char)((int) playersColumn - 2);
 				}
 				int player1Row = j - (roundPad / 2);
 				//l.error("player1Row: " + player1Row);
 				int player2Row = player1Row + roundPad;
 				//l.error("player2Row: " + player2Row);
 				StringBuffer player1Cell = new StringBuffer(String.valueOf(playersColumn)).append(String.valueOf(player1Row));
 				Cell player1ID = aFinalDrawSheet.getCell(player1Cell.toString()); 
 				l.error("player1ID: '" + player1ID.getContents() + "'");
 				
 				StringBuffer player2Cell = new StringBuffer(String.valueOf(playersColumn)).append(String.valueOf(player2Row));
 				Cell player2ID = aFinalDrawSheet.getCell(player2Cell.toString());
 				l.error("player2ID: '" + player2ID.getContents() + "'");
 				
 				if (resultCell.getContents().length() == 0 || 
 					player1ID.getContents().length() == 0 ||
 					player2ID.getContents().length() == 0) {
 					// result or player ids are empty, something is not right
 					return false;
 				}
 				
 				// if we are in a draw of 8 people, the players should all be added
 				// to the top 8 list
 				if (aDrawSize == 8) {
 					if (pos < 8) {
 						if (Arrays.asList(first8).indexOf(player1ID.getContents()) == -1) {
 							first8[pos] = player1ID.getContents();
 							pos++;
 						}
 					}
 					if (pos < 8) {
 						if (Arrays.asList(first8).indexOf(player2ID.getContents()) == -1) {
 							first8[pos] = player2ID.getContents();
 							pos++;
 						}
 					}
 				}
 				
 				if (resultCell.getContents().equals(player1ID.getContents())) {
 					Afiliado winner = 
 						this._afiliadoService.getAfiliadoById(
 								Long.parseLong(player1ID.getContents()));
 					// player 1 has BYE
 					if (player2ID.getContents().equals("-")) {
 						Bonificacion bonificacion = 
 							new Bonificacion(
 									winner, 
 									aEvento, 
 									Long.parseLong(
 										RankingConstants.getProperty("org.fecoteme.bye")));
 						this._bonificacionService.createBonificacion(bonificacion);
 					} else {
 						Afiliado loser = 
 							this._afiliadoService.getAfiliadoById(
 									Long.parseLong(player2ID.getContents()));
 						Partida p = new Partida(aEvento, winner, loser);
 						this._partidaService.createPartida(p);						
 					}
 				} else {
 					Afiliado winner = 
 						this._afiliadoService.getAfiliadoById(
 								Long.parseLong(player2ID.getContents()));
 					// player 2 has BYE
 					if (player1ID.getContents().equals("-")) {
 						Bonificacion bonificacion = 
 							new Bonificacion(
 									winner, 
 									aEvento, 
 									Long.parseLong(
 										RankingConstants.getProperty("org.fecoteme.bye")));
 						this._bonificacionService.createBonificacion(bonificacion);
 					} else {
 						Afiliado loser = 
 							this._afiliadoService.getAfiliadoById(
 									Long.parseLong(player1ID.getContents()));
 						Partida p = new Partida(aEvento, winner, loser);
 						this._partidaService.createPartida(p);						
 					}
 				}
 				
 				j = j + (2 *roundPad);
 			}	
 		}
 		
 		// now process the first8 array
 		for (int t = 0; t < first8.length; t++) {
 			long bonificacion = 0;
 			switch(t) {
 			case 0:
 				bonificacion = 
 					Long.parseLong(
 						RankingConstants.getProperty("org.fecoteme.bonificacion.1erLugar"));
 				break;
 			case 1:
 				bonificacion = 
 					Long.parseLong(
 						RankingConstants.getProperty("org.fecoteme.bonificacion.2doLugar"));
 				break;
 			case 2:
 				bonificacion = 
 					Long.parseLong(
 						RankingConstants.getProperty("org.fecoteme.bonificacion.3erLugar"));
 				break;
 			case 3:
 				bonificacion = 
 					Long.parseLong(
 						RankingConstants.getProperty("org.fecoteme.bonificacion.4toLugar"));
 				break;
 			case 4:
 				bonificacion = 
 					Long.parseLong(
 						RankingConstants.getProperty("org.fecoteme.bonificacion.5toLugar"));
 				break;
 			case 5:
 				bonificacion = 
 					Long.parseLong(
 						RankingConstants.getProperty("org.fecoteme.bonificacion.6toLugar"));
 				break;
 			case 6:
 				bonificacion = 
 					Long.parseLong(
 						RankingConstants.getProperty("org.fecoteme.bonificacion.7moLugar"));
 				break;
 			case 7:
 				bonificacion = 
 					Long.parseLong(
 						RankingConstants.getProperty("org.fecoteme.bonificacion.8voLugar"));
 				break;
 			}
 			
 			Afiliado player = 
 				this._afiliadoService.getAfiliadoById(Long.parseLong(first8[t])); 
 				
 			Bonificacion bono = 
 				new Bonificacion(
 						player, 
 						aEvento, 
 						bonificacion);
 			this._bonificacionService.createBonificacion(bono);
 		}
 	
 		return true;
 	}
 }
 
 /**
  * thread to send output to the child.
  */
 final class Sender implements Runnable
     {
     // ------------------------------ CONSTANTS ------------------------------
 
     /**
      * e.g. \n \r\n or \r, whatever system uses to separate lines in a text file. Only used inside multiline fields. The
      * file itself should use Windows format \r \n, though \n by itself will alsolineSeparator work.
      */
     private static final String lineSeparator = System.getProperty( "line.separator" );
 
     /**
      * stream to send output to child on
      */
     private final OutputStream os;
     /**
      * 
      */
     private final int numberGroups;
 
     /**
      * method invoked when Sender thread started.  Feeds dummy data to child.
      */
     public void run() {
         try {
             final BufferedWriter bw = new BufferedWriter( new OutputStreamWriter( os ), 100);
             //bw.write(this.numberGroups);
             bw.write(new Integer(this.numberGroups).toString() + lineSeparator);
             bw.flush();
             bw.close();
         } catch ( IOException e ) {
         	throw new IllegalArgumentException( "IOException sending data to child process." );
         }
     }
 
     /**
      * constructor
      *
      * @param os stream to use to send data to child.
      * @param numberGroups the number of groups to be sent to the
      * llaves generator
      */
     Sender( OutputStream os, int numberGroups) {
     	this.os = os;
     	this.numberGroups = numberGroups;
     }
 }
 
 /**
  * thread to read output from child
  */
 class Receiver implements Runnable {
     /**
      * stream to receive data from child
      */
     private final InputStream is;
 
     /**
      * method invoked when Receiver thread started.  Reads data from child and displays in on System.out.
      */
     public void run() {
         try {
         	final BufferedReader br = new BufferedReader( new InputStreamReader( is ), 100);
             String line;
             while ( ( line = br.readLine() ) != null ) {
             	System.out.println( line );
             }
             br.close();
         } catch ( IOException e ) {
             throw new IllegalArgumentException( "IOException receiving data from child process." );
         }
     }
 
     /**
      * contructor
      *
      * @param is stream to receive data from child
      */
     Receiver( InputStream is ) {
         this.is = is;
     }
 }
