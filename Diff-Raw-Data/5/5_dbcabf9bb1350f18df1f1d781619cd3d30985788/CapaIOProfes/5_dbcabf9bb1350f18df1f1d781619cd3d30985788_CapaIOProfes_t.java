 /**
 ******************************************************
 * @file CapaIOProfes.java
 * @author victor flores sanchez
 * @date mayo 2010
 * @version 0.1
 * @brief En este archivo se especifica la clase CapaIOCursos que se encarga de leer/escribir datos en el archivo de cursos.
 *****************************************************/
 
 
 package jramos.capaIO;
 
 import jramos.tiposDatos.Profesor;
 import jramos.tiposDatos.Hora;
 import jramos.tiposDatos.HourOutOfRangeException;
 import java.util.ArrayList;
 import java.io.File;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.PrintWriter;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 
 public class CapaIOProfes
 {	/* ATRIBUTOS */
 	private String nombreArchivoProfes;
 	private static final int capacidadInicialVector = 100;
 	private static final int capacidadInicialString = 200;
 
 	/* CONSTRUCTORES */
 	public CapaIOProfes() throws IOException
 	{	this.nombreArchivoProfes = (System.getProperty("user.home") + System.getProperty("file.separator") + "archProfes.txt");
 		File archPrueba = new File(this.nombreArchivoProfes);
 		/** Si no existe el archivo de cursos, este es creado.*/
 		try
 		{	if (archPrueba.createNewFile())
 				System.out.println("Se ha creado el archivo de profesores vacio\nEn: " + this.nombreArchivoProfes);
 		}
 		catch (IOException IOE)
 		{	System.out.println("No se puede crear un archivo de profesores en: \n" + this.nombreArchivoProfes + "\nError grave!!");
 			throw IOE;
 		}
 	}
 	
 	/* METODOS */
 	/** Metodo para leer todos los profesores.
 	* 
 	*/
 	public ArrayList<Profesor> leeProfes() throws FileNotFoundException, IOException, HourOutOfRangeException
 	{	ArrayList<Profesor> listaProfes= new ArrayList(CapaIOProfes.capacidadInicialVector);
 		BufferedReader lector;
 		StringBuilder lineaDatos = new StringBuilder(CapaIOProfes.capacidadInicialString);
 		int caracterLeido = 0;
 		long i, j;
 		
 		/** Intento abrir el archivo de cursos */
 		try
 		{	lector = new BufferedReader(new FileReader(this.nombreArchivoProfes));
 		}
 		catch (FileNotFoundException FNFE)
 		{	throw FNFE; //<Devuelvo la excepción haca quien llame el método leeCursos.
 		}
 		
 		/** Leo el archivo de profesores hasta el final */
 		for (i = 0; caracterLeido != -1; i++)
 		{	caracterLeido = lector.read();
 			/**Comienza a leer datos desde que encuentra un caracter '<' */
 			if (caracterLeido == '<')
 			{	for (j = 0; ((caracterLeido != -1) && (caracterLeido != '>')); j++) //ver que el -1 que se almacena si llego al final del archivo. en teoria no debe ocurrir se antes compruebo sintaxis.
 				{	lineaDatos.append(String.valueOf((char)caracterLeido));
 					caracterLeido = lector.read();
 				}
 				lineaDatos.append(String.valueOf((char)caracterLeido));//agrego el caracter '>' que no fue agregado en el bucle
 				i += j; //sumo los caracteres que ya se han leido a i, aun no se si esto pueda ser necesario a futuro.
 			}
 			/** Como se ha encontrado una linea con una especificacion de un objeto, ahora proceso esa linea y agrego el objeto que retorna el metodo analizaLinea */
 			Profesor ProfesorEncontrado = this.stringToProfesor(lineaDatos.toString());
 			if (ProfesorEncontrado != null)
 				listaProfes.add(ProfesorEncontrado);
 			else
 				System.out.println("Aviso: Lo que se ha encontrado en la linea analizada no es un curso");
                         lineaDatos = new StringBuilder(CapaIOProfes.capacidadInicialString);
                 }
 		/** Cierro el archivo*/
 		try
 		{	lector.close();
 		}
 		catch (IOException IOE)
 		{	System.out.println("Error: No se puede cerrar el archivo de cursos\nError grave!!");
 			throw IOE;
 		}
 		/**  Retorno la lista con los profesores leidos*/
 		return listaProfes;
 	}
 
         public void escribeProfes(ArrayList<Profesor> listaProfes) throws FileNotFoundException, SecurityException, IOException
         {       Integer idInicialProfesWrap = this.leeIDInicial("idProfes");
                 int idInicialProfes;
                 if (idInicialProfesWrap == null)
                         idInicialProfes = 1;
                 else
                         idInicialProfes = idInicialProfesWrap.intValue();
 
                 escribeProfes(listaProfes, idInicialProfes);
         }
 
 	public Integer leeIDInicial(String tipoId) throws FileNotFoundException, IOException
         {       int idInicial = 0;
                 BufferedReader lector;
 		StringBuilder lineaDatos = new StringBuilder(CapaIOProfes.capacidadInicialString);
 		int caracterLeido = 0;
 		long i, j;
 
 		/** Intento abrir el archivo de cursos */
 		try
 		{	lector = new BufferedReader(new FileReader(this.nombreArchivoProfes));
 		}
 		catch (FileNotFoundException FNFE)
 		{	throw FNFE; //<Devuelvo la excepción haca quien llame el método leeCursos.
 		}
 
                 for (i = 0; (caracterLeido != -1) && idInicial == 0; i++)
 		{	caracterLeido = lector.read();
 			/**Comienza a leer datos desde que encuentra un caracter '<' */
 			if (caracterLeido == '<')
 			{	for (j = 0; ((caracterLeido != -1) && (caracterLeido != '>')); j++) //ver que el -1 que se almacena si llego al final del archivo. en teoria no debe ocurrir se antes compruebo sintaxis.
 				{	lineaDatos.append(String.valueOf((char)caracterLeido));
                                         //lineaDatos.append(Character.forDigit(caracterLeido, 10));
 					caracterLeido = lector.read();
 				}
 				lineaDatos.append(String.valueOf((char)caracterLeido));//agrego el caracter '>' que no fue agregado en el bucle
 				i += j; //sumo los caracteres que ya se han leido a i, aun no se si esto pueda ser necesario a futuro.
 			}
 			/** Como se ha encontrado una linea con una especificacion de un objeto, ahora proceso esa linea y agrego el objeto que retorna el metodo analizaLinea */
 			idInicial = this.stringToIdInicial(new String(lineaDatos.toString()), tipoId);
 			if (idInicial == 0)
 				System.out.println("Aviso: Lo que se ha encontrado en la linea analizada no es un id");
                         lineaDatos = new StringBuilder(CapaIOProfes.capacidadInicialString);
                 }
 		/** Cierro el archivo*/
 		lector.close();
                return new Integer(idInicial);
 
         }
 
         private int stringToIdInicial(String linea, String tipoId)
         {       int comienzoDato, idInicial;
                 if (((linea.indexOf("<idProfesInicial") != -1)) && (tipoId.equals("idProfes")))
                 {       comienzoDato = linea.indexOf("idProfesInicial=") + "idProfesInicial=".length();
                         idInicial = Integer.valueOf(linea.substring(comienzoDato+1, linea.indexOf("\"", comienzoDato+1)));
                         return idInicial;
                 }
                 else
                         return 0;
         }
 
 	/** 
 	* Método que guarda todos los profesores en el archivo de profesores.
 	*/
 	public void escribeProfes(ArrayList<Profesor> listaProfes, int idInicialProfes) throws FileNotFoundException, SecurityException, IOException
 	{	PrintWriter escritor;
 
 		int i;
 		/** Intenta abrir el archivo de profesores para escribir en él. */
 		try
 		{	escritor = new PrintWriter(this.nombreArchivoProfes);
 		}
 		catch (FileNotFoundException FNFE)
 		{	System.out.println("ERROR: El archivo no existe"); //no deberia llegar a esta excepcion con el constructor que crea el archivo.
 			throw FNFE;
 			
 		}
 		catch (SecurityException SE)
 		{	System.out.println("ERROR: No tiene permisos de escritura sobre el archivo de cursos.");
 			throw SE;
 		}
 
                  //Escribo los idIniciales de los profesores
                 escritor.println("<idProfesInicial=\""+idInicialProfes+"\" >");
 
 		//Escribo los profesores del ArrayList<Profesor> en el archivo de profesores.
 		for(i = 0; i<listaProfes.size();i++)
 		{       escritor.println(this.profesorToString(listaProfes.get(i)));//Escribo en el archivo de profes.
 		}
 		
 		/** Cierro el archivo*/
 		escritor.close();
 	}
 
 	/** 
 	* Este método recibe un String que contiene especificado un objeto del tipo Curso, analiza este String y devuelve un objeto Curso.
 	*/
 	private Profesor stringToProfesor(String linea) throws HourOutOfRangeException
 	{	Profesor profesorLeido;
                 String idProfe; //id Interna del profesor
 		String nombProfe; //Nombre del cur
                 String rutProfe; //Rut del profesor
                 String idCursosAsig; //Id de los cursos que el profesor tiene asignados
                 String codCursosDisp; //Codigo de los cursos que puede dictar el profesor
 		String horasDisp; //Horas que el profesor tiene disponibles para hacer clases
 		String horasAsig; //Horas que al profesor se le han asignado.
 		int comienzoDato, codCurso, posicionBarra, i, idCurso;
 		Hora objHora;
                 
 		/* Si es un curso lo que está espeficado en la linea, creo un objeto "Curso" */
 		if ((linea.indexOf("<Profesor") != -1))
 		{	/* Busco errores de sintaxis en la linea analizada*/
 			if ((linea.indexOf("nombProfe=") == -1) || (linea.indexOf("idProfe=") == -1) || (linea.indexOf("rutProfe=") == -1) || (linea.indexOf("idCursosAsig=") == -1) || (linea.indexOf("horasDisp=") == -1) || (linea.indexOf("horasAsig=") == -1))
 			{	System.out.println("ERROR: La linea leida desde el archivo de cursos es incorrecta");
 			}
 
 			/* Busco el nombre del curso en la linea*/
 			comienzoDato = linea.indexOf("idProfe=") + "idProfe=".length();
 			idProfe = linea.substring(comienzoDato +1, linea.indexOf("\"", comienzoDato+1)); //confirmar que debo sumar 1 !!!
 
 			/* Busco la descripción del curso en la linea*/
 			comienzoDato = linea.indexOf("nombProfe=") + "nombProfe=".length();
 			nombProfe = linea.substring(comienzoDato+1, linea.indexOf("\"", comienzoDato+1)); //confirmar que debo sumar 1 !!!
 
 			/* Busco el código del curso en la linea*/
 			comienzoDato = linea.indexOf("rutProfe=") + "rutProfe=".length();
 			rutProfe = linea.substring(comienzoDato+1, linea.indexOf("\"", comienzoDato+1)); //confirmar que debo sumar 1 !!!
 
 			/* Busco las secciones del curso en la linea*/
 			comienzoDato = linea.indexOf("idCursosAsig=") + "idCursosAsig=".length();
 			idCursosAsig = linea.substring(comienzoDato+1, linea.indexOf("\"", comienzoDato+1)); //confirmar que debo sumar 1 !!!
 
                         comienzoDato = linea.indexOf("codCursosDisp=") + "codCursosDisp=".length();
 			codCursosDisp = linea.substring(comienzoDato+1, linea.indexOf("\"", comienzoDato+1));
 
 			/* Busco las carreras del curso en la linea*/
 			comienzoDato = linea.indexOf("horasDisp=") + "horasDisp=".length();
 			horasDisp = linea.substring(comienzoDato+1, linea.indexOf("\"", comienzoDato+1)); //confirmar que debo sumar 1 !!!
 
 			/* Busco el profesor del curso en la linea*/
 			comienzoDato = linea.indexOf("horasAsig=") + "horasAsig=".length();
 			horasAsig = linea.substring(comienzoDato+1, linea.indexOf("\"", comienzoDato+1)); //confirmar que debo sumar 1 !!!
 
                         ArrayList<Integer> listaCursosParaImpartir= new ArrayList();
                         if (codCursosDisp.length() != 0) //Seteo los codigos de curso que puede impartir
                         {       for (i = 0; codCursosDisp.indexOf("|") != -1;i++)
                                 {       System.out.println(codCursosDisp.substring(0, codCursosDisp.indexOf("|")));
                                         codCurso = Integer.valueOf(codCursosDisp.substring(0, codCursosDisp.indexOf("|")));
                                         posicionBarra = codCursosDisp.indexOf("|");
                                         codCursosDisp = codCursosDisp.substring(posicionBarra+1);
                                         listaCursosParaImpartir.add(new Integer(Integer.valueOf(codCurso)));
                                         System.out.println("En carrera: " + codCurso);
                                 }
                                 //Agrego el ultimo que no fue agregado en el bucle:
                                 listaCursosParaImpartir.add(new Integer(Integer.valueOf(codCursosDisp)));
                                 System.out.println("En carrera: " +codCursosDisp);
                         }
 
                         /* Construyo el objeto cursoLeido con los datos recopilados */
 			profesorLeido = new Profesor(nombProfe, listaCursosParaImpartir, Integer.valueOf(idProfe));
                         profesorLeido.setRutProfesor(Integer.valueOf(rutProfe));
                         //Seteo la lista de horarios disponibles del profesor.
                         if (horasDisp.length() != 0)
                         {       for (i = 0; horasDisp.indexOf("|") != -1;i++)
                                 {       System.out.println(horasDisp.substring(0, horasDisp.indexOf("|")));
                                         objHora = new Hora(horasDisp.substring(0, horasDisp.indexOf("|")));
                                         posicionBarra = horasDisp.indexOf("|");
                                         horasDisp = horasDisp.substring(posicionBarra+1);
                                         profesorLeido.modHorasDisponibles(objHora, 1);
                                         System.out.println("Hora disponible: " + objHora);
                                 }
                                 //Agrego el ultimo que no fue agregado en el bucle:
                                 profesorLeido.modHorasDisponibles(new Hora(horasDisp), 1);
                                 System.out.println("Hora disponible: " +horasDisp);
                         }
                         //Seteo la lista de horarios asignados del profesor
                         if (horasAsig.length() != 0)
                         {       for (i = 0; horasAsig.indexOf("|") != -1;i++)
                                 {       System.out.println(horasAsig.substring(0, horasAsig.indexOf("|")));
                                         objHora = new Hora(horasAsig.substring(0, horasAsig.indexOf("|")));
                                         posicionBarra = horasAsig.indexOf("|");
                                         horasAsig = horasAsig.substring(posicionBarra+1);
                                         profesorLeido.modHorasAsignadas(objHora, 1);
                                         System.out.println("Hora asignada: " + objHora);
                                 }
                                 //Agrego el ultimo que no fue agregado en el bucle:
                                 profesorLeido.modHorasAsignadas(new Hora(horasAsig), 1);
                                 System.out.println("Hora asignada: " +horasAsig);
                         }
                         //Seteo la lista de cursos asignaos al profesor
                         if (idCursosAsig.length() != 0) //Seteo los codigos de curso que puede impartir
                         {       for (i = 0; idCursosAsig.indexOf("|") != -1;i++)
                                 {       System.out.println(idCursosAsig.substring(0, idCursosAsig.indexOf("|")));
                                         idCurso = Integer.valueOf(idCursosAsig.substring(0, idCursosAsig.indexOf("|")));
                                         posicionBarra = idCursosAsig.indexOf("|");
                                         idCursosAsig = idCursosAsig.substring(posicionBarra+1);
                                         profesorLeido.modIdCursosAsignados(Integer.valueOf(idCurso), 1);
                                         System.out.println("id cursos asignados: " + idCurso);
                                 }
                                 //Agrego el ultimo que no fue agregado en el bucle:
                                 profesorLeido.modIdCursosAsignados(Integer.valueOf(idCursosAsig), 1);
                                 System.out.println("id cursos asignados: " +idCursosAsig);
                         }
 
                         //Falta hacer el siguiente código acá!!!
                         //Seteo idCursosAsig
 
 
 			/* Seteo los demas atributos del curso leido */
 			return profesorLeido;
 		}
 
 		else
 		{	return null;
 		}
 	}
 	/**
 	* Esté metodo recibe un objeto Curso y crea un string de como debe ser escrito en el archivo de cursos
 	*/
 	private String profesorToString(Profesor profesorAEscribir)
 	{	String cursoString;
 		String nomProfe = profesorAEscribir.getNombreProfesor(); //Nombre del profesor
 		String idProfe = Integer.toString(profesorAEscribir.getIdProfesor()); //id interna del profesor
 		String rutProfe = Integer.toString(profesorAEscribir.getRutProfesor()); //rut del profesor
 		String idCursosAsig = profesorAEscribir.getIdCursosAsignados(); //cursos que el profesor tiene asignados
                 String horasDisp = profesorAEscribir.getHorasDisponibles(); //obtiene las horas que el profesor tiene disponibles
                 String horasAsig = profesorAEscribir.getHorasAsignadas(); //obtiene las horas que se le han asignado al profesor
                 String codCursosDisp = profesorAEscribir.getCursosQueImparte(); //
 		cursoString = "<Profesor nombProfe=\""+nomProfe+"\" idProfe=\""+idProfe+"\" rutProfe=\""+rutProfe+"\" idCursosAsig=\""+idCursosAsig+"\" codCursosDisp=\""+codCursosDisp+"\" horasDisp=\""+horasDisp+"\" horasAsig=\""+horasAsig+"\" >";
 		return cursoString;
 	}
 }
 
