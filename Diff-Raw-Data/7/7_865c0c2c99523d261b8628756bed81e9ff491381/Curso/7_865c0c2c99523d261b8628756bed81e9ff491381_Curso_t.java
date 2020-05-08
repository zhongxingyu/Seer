 //Probando cambios con git-bash
 
 /**
 ******************************************************
 * @file Profesor.java
 * @author AirZs
 * @date mayo 2010
 * @version 0.1
 * @brief En este archivo se especifica la clase del tipo Curso.
 *****************************************************/
 
 package jramos.tiposDatos;
 import java.util.ArrayList;
 
 public class Curso {
     static int idCursoActual = 0;
 
     private String nomCurso;
     private String descrip = "0";
     private int codCurso;
     private String seccion = "0";
     private ArrayList<Carrera> enCarrera;
     private ArrayList<Integer> codigosCarrera;
     private Profesor profeAsig;
     private int idProfeAsig;
     private ArrayList<Integer> listSalas;
     private ArrayList<Hora> horario;
     private int idCurso;
 
     //////////////////////////////////////////////////////////////////////
     // Contructor
 
     /**
      * 1º Constructor para Curso:
      * Este constructor debe ser utilizado para crear un objeto Curso la primera vez. Se le asigna un
      * id de forma automatica.
      *
      * @param nombreRamo Nombre del Ramo
      * @param codigoCurso Codigo del Curso
      */
     public Curso(String nombreRamo, int codigoCurso){
 
         this.nomCurso = nombreRamo;
         this.codCurso = codigoCurso;
         this.enCarrera = new ArrayList<Carrera>();
         this.codigosCarrera = new ArrayList<Integer>();
         this.listSalas = new ArrayList<Integer>();
         this.horario = new ArrayList<Hora>();
         this.idCurso = ++idCursoActual;
 
 
     }
 
     /**
      * 2º Constructor para Curso:
      * Este constructor debe ser utilizado para crear un objeto Profesor a partir de un registro o archivo.
      *
      * @param nombreRamo Nombre del Ramo
      * @param codigoCurso Codigo del Curso
      * @param id int con el id del Curso
      */
     public Curso(String nombreRamo, int codigoCurso, int id){
         this.nomCurso = nombreRamo;
         this.codCurso = codigoCurso;
         this.enCarrera = new ArrayList<Carrera>();
         this.codigosCarrera = new ArrayList<Integer>();
         this.listSalas = new ArrayList<Integer>();
         this.horario = new ArrayList<Hora>();
         this.idCurso = id;
 
     }
 
     //////////////////////////////////////////////////////////////////////
     // Metodos de Obtener Variables
 
    /**
     * Metodo para obetener el nombre del curso
     *
     * @return String con el nombre del curso
     */
    public String getNombreCurso(){
        return this.nomCurso;
     }
 
    /**
     * Metodo para obtener la descripcion del curso
     *
     * @return String con la descripcion del curso
     */
    public String getDescripcion(){
         if (!this.descrip.contentEquals("0")){
             return this.descrip;
         }
         else{
             return "No Hay Descripción Disponible...";
         }
         
     }
 
    /**
     * Metodo para obtener el codigo del curso
     *
     * @return int con el codigo del curso
     */
    public int getCodigoCurso(){
         return this.codCurso;
     }
 
    /**
     * Metodo para obtener la seccion del curso
     *
     * @return String con la seccion del curso
     */
    public String getSeccion(){
         return this.seccion;
     }
 
    /**
     * Metodo para obtener las carreras que contienen este curso
     *
     * @return String con los nombres de las carreras separados con |
     */
    public String getEnCarreras(){
         StringBuilder text = new StringBuilder();
         for (Carrera carrera: this.enCarrera){
             text.append(carrera.getNombreCarrera()).append("|");
         }
         text.deleteCharAt(text.length()-1);
         return text.toString();
     }
 
    /**
     * Metodo para obtener los codigos de las carreras que contienen este curso
     *
     * @return String con los codigos de la carrera separados con |
     */
    public String getEnCarreras_Codigo(){
         StringBuilder text = new StringBuilder();
        if (this.codigosCarrera.size() != 0 )
         {   for (Integer codigo: this.codigosCarrera){
                 text.append(String.valueOf(codigo)).append("|");
             }
            text.deleteCharAt(text.length()-1);
            return text.toString();
         }
         else
             return "";
     }
 
    /**
     * Metodo para obtener el nombre del profesor que esta asignado este curso
     *
     * @return String con el nombre del profesor
     */
    public String getNombreProfesor(){
        return this.profeAsig.getNombreProfesor();
    }
 
    /**
     * Metodo para obtener el id del profesor que esta asignado a este curso
     *
     * @return int con el id del profesor
     */
    public int getIdProfeAsig(){
        return this.idProfeAsig;
    }
 
    /**
     * Metodo para obtener las salas donde se imparte este curso
     *
     * @return String con los numeros de las salas separados con |
     */
    public String getSalas(){
       StringBuilder text = new StringBuilder();
       for (Integer numSala: this.listSalas){
           text.append(String.valueOf(numSala)).append("|");
       }
       try //esto es en caso que no tenga salas
       {      text.deleteCharAt(text.length()-1);
       }
       catch (Exception e){}
       return text.toString();
    }
 
    /**
     * Metodo para obtener los horarios de este curso
     *
     * @return String con los horarios con formato Str separados con |
     */
    public String getHorario(){
       StringBuilder text = new StringBuilder();
       if (this.horario.size() != 0 )
       {     for (Hora hora: this.horario){
                 try
                 {     text.append(hora.getHoraStr()).append("|");
                 }
                 catch (Exception e)
                 {     System.out.println("Error al pedir datos de hora");
                 }
             }
             text.deleteCharAt(text.length()-1);
       return text.toString();
       }
       else
           return "";
    }
 
    /**
     * Metodo para obtener el id del curso
     *
     * @return int con el id del curso
     */
    public int getIdCurso(){
        return this.idCurso;
    }
 
 
    //////////////////////////////////////////////////////////////////////
    // Metodos de Modificar Variables
 
    /**
     * Metodo para agregar la descripcion al curso
     *
     * @param desc String con la descripcion del curso
     */
    public void setDescripcion(String desc){
        this.descrip = desc;
    }
 
    /**
     * Metodo para agregar el codigo del curso
     *
     * @param codigo int con el codigo del curso
     */
    public void setCodigoCurso(int codigo){
        this.codCurso = codigo;
    }
 
    /**
     * Metodo para agregar la seccion del curso
     *
     * @param codSec Codigo con el formato BNF <codSec>:= <letra>-<num><num>
     */
    public void setSeccion(String codSec){
        this.seccion = codSec;
    }
 
    /**
     * Metodo para hacer la conexion al profesor que esta asignado a este curso.
     * Actualiza automaticamente el rut del profesor
     *
     * @param profesorToAsign Objeto Profesor
     */
    public void setProfesor(Profesor profesorToAsig){
        this.profeAsig = profesorToAsig;
        this.idProfeAsig = profesorToAsig.getIdProfesor();
    }
 
    /**
     * Metodo para agregar el id del profesor que esta asignado a este curso (Usese como referencia)
     *
     * @param id int con el id del profesor
     */
    public void setIdProfeAsig(int id){
        this.idProfeAsig = id;
    }
 
    /**
     * Metodo para agregar o quitar un codigo de las carrera que contienen este curso(Usese como referencia)
     *
     * @param codigo int con el codigo de la carrera
     * @param selector 1: Agregar  -1: Quitar
     */
    public void modCodigosCarrera(int codigo, int selector){
        if (selector == 1){
            if (!this.codigosCarrera.contains(codigo)){
                this.codigosCarrera.add(new Integer(codigo));
            }
            else{
                System.out.println("Ya existe el codigo de la carrera...");
            }
        }
        else if (selector == -1){
            if (this.codigosCarrera.contains(codigo)){
                this.codigosCarrera.remove(new Integer (codigo));
            }
            else{
                System.out.println("No existe ese codigo de la carrera...");
            }
        }
        else{
            System.out.println("Error en la seleccion...");
        }
 
    }
 
    /**
     * Metodo para agregar o quitar una de las carrera que contienen este curso
     *
     * @param carrera Objeto carrera que se desea agregar o quitar
     * @param selector 1: Agregar  -1: Quitar
     */
    public void modCarrera(Carrera carreraToAsig, int selector){
       if (selector == 1){
           if (!this.enCarrera.contains(carreraToAsig)){
               this.enCarrera.add(carreraToAsig);
           }
           else{
               System.out.println("Ya existe la carrera...");
           }
           
       }
       else if (selector == -1){
           if (this.enCarrera.contains(carreraToAsig)){
               this.enCarrera.remove(carreraToAsig);
           }
           else{
               System.out.println("Ya existe la carrera...");
           }
           
       }
       else{
           System.out.println("Error en la seleccion.");
       }
    }
 
    /**
     * Metodo para agregar o quitar una de las salas en donde se imparte este curso
     *
     * @param sala int con el numero de la sala
     * @param selector 1: Agregar  -1: Quitar
     */
    public void modListaSalas (int sala, int selector){
        if (selector == 1){
            if (!this.listSalas.contains(sala)){
                this.listSalas.add(new Integer(sala));
            }
            else{
                System.out.println("Ya existe la sala...");
            }
        }
        else if (selector == -1){
            if (this.listSalas.contains(sala)){
                this.listSalas.remove(new Integer(sala));
            }
            else{
                System.out.println("No existe la sala...");
            }
        }
        else{
            System.out.println("Error en la seleccion...");
        }
    }
 
    /**
     * Metodo para agregar o quitar una bloque horario donde se imparte este curso
     *
     * @param horaToAsign Objeto Hora que se desea agregar o quitar
     * @param selector 1: Agregar  -1: Quitar
     */
    public void modHorario(Hora horaToAsig, int selector){
        if (selector == 1){
            if (!this.horario.contains(horaToAsig)){
                this.horario.add(horaToAsig);
            }
            else{
                System.out.println("Ya existe esa hora...");
            }
        }
        else if (selector == -1){
            if (this.horario.contains(horaToAsig)){
                this.horario.remove(horaToAsig);
            }
            else{
                System.out.println("No existe esa hora...");
            }
        }
        else{
            System.out.println("Error en la seleccion...");
        }
    }
 
    /**
     * Metodo para setear el Id del curso actual(variable static). Se debe obtener desde un archivo.
     * Se utiliza este metodo siempre que se cargue el sistema, para no volver a utilizar los mismos
     * id.
     * 
     * @param id int con el id static para ser seteado
     */
    public void setIdCursos(int id){
        Curso.idCursoActual = id;
    }
 }
