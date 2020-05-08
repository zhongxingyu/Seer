 package compilationunit;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.ListIterator;
 
 public class GenFinal {
     BufferedWriter bw;
     File archiEscri=null;
     String temporal;
     String operacion,op1,op2,op3;
     String etiquetasputs="";
     int num_param_actual = 0;
     int c_etiqueta;
 
 
 public GenFinal(LinkedList<tupla_Tercetos> colaTercetos, Tablas tabla, String fichero) {
     
     int desp_total;  //variable para el desplazamiento total de las tablas de simbolos
     archiEscri= new File(fichero);
     tupla_Tercetos tupla_actual;
     String terceto_actual;
     TablaSimbolos ambito_actual;
     //cola para ir metiendo los metodos a los que se llama
     LinkedList<String> colaMetodos = new LinkedList<String> (); 
     Simbolo simbolo;
     TablaSimbolos tabla_aux;
     c_etiqueta = 0;
     
     System.out.println("Comienza la fase de generacion de codigo objeto");
     //preparamos el fichero que contendra el codigo objeto
     try
     	{
         bw= new BufferedWriter(new FileWriter(fichero));
     	}
     catch (IOException e) 
     	{
          System.out.println("Error fichero de salida para Codigo Objeto.");
     	}
     
 
     //inicializamos el codigo objeto y lo dejamos todo preparado para leer los
     //tercetos del main
     try {
         bw.write("ORG 0\n");
         // Inicializamos la pila al maximo puesto que es decreciente
         // y la guardamos en el puntero de pila
         bw.write ("MOVE #65535, .SP\n");
         bw.write ("MOVE .SP, .IX\n");
         
         /* creamos el RA de la clase que contiene el metodo principal, dejando
          * hueco para todos sus atributos, despues guardamos el IX, que apuntará
          * al primer atributo de la clase que contiene el metodo main
          * para luego poder acceder cogiendo el desplazamiento de la tabla
          * de simbolos */
         tabla_aux = tabla.GetAmbitoGlobal();  //buscamos la tabla de la clase del metodo principal
         desp_total = tabla_aux.GetDesplazamiento(); //cogemos el desp de la tabla de simbolos global
         bw.write ("ADD #-" + desp_total + ", .SP\n"); //sumamos desp_total de la tabla de simbolos padre al SP
         System.out.println("guarrilla");
         bw.write("MOVE .A, .SP\n"); //actualizamos SP
         bw.write("PUSH .IX\n");  //guardamos el IX para saber donde empiezan los atributos de la tabla de simbolos padre
         bw.write ("MOVE .SP, .IX\n");  //actualizamos IX
         
         //Vamos a buscar el main para que el PC
         //Si el analisis semantico ha validado el codigo, dentro del ambito global deberia estar el objeto main
         simbolo = tabla_aux.GetSimbolo("main");
         String etiqueta_main;
         etiqueta_main = simbolo.GetEtiqueta();
         bw.write("CALL /" + etiqueta_main + " ; VAMOS AL MAIN\n");
         bw.write("POP .IX ; Recuperamos el marco de pila\n");
         bw.write("MOVE .IX, .SP\n");
         bw.write("HALT ;Cuando se vuelva del Main se terminara la ejecucion\n");
         
 //        ProcesarTercetos(colaTercetos, tabla);
         
         /*
          * Bucle para imprimir toda la cola de tercetos!
          */
         System.out.println("-----------------------------------");
         System.out.println("Elementos de la lista "+colaTercetos);
        System.out.println("Tamano de la lista:"+colaTercetos.size());
         //Iterator it2 = a.iterator();
         Iterator<tupla_Tercetos> it = colaTercetos.iterator();
         while (it.hasNext()) {
             //this.separar(it.next().GetTerceto());
        	tupla_actual = it.next();
            System.out.println("Terceto: "+tupla_actual.GetTerceto());
             System.out.println("Tabla:"+tabla.GetAmbitoGlobal().GetDesplazamiento());
            System.out.println("Desplazamiento de la tabla para temp:"+tupla_actual.GetAmbitoActual().GetDesplazamiento());
             //System.out.println("Ambito_actual: "+it.next().GetAmbitoActual());
         }
         System.out.println("-----------------------------------");
         
         
         // Importante! sino no se guarda nada en el fichero!
         bw.close();
     }
        
     catch (IOException e)
     	{
     	System.out.println("Tranquilo vaquero");
     	}
         
         
         
         
  
     }
 
 private void ProcesarTercetos(LinkedList<tupla_Tercetos> colaTercetos, Tablas tabla)
 	{
 	while (!colaTercetos.isEmpty()) 
 		{
 		String terceto_actual;
 		tupla_Tercetos tupla_actual;
 	    tupla_actual = colaTercetos.removeFirst();
 	    terceto_actual = tupla_actual.GetTerceto();
 	    TablaSimbolos ambitoterceto = tupla_actual.GetAmbitoActual();
 	    
         this.separar(terceto_actual); //Esto se para el terceto en sus operandos
         if (operacion.compareTo("ASIGNACION") == 0)
         	EjecutarAsignacion(op1, op2, ambitoterceto);
 //        this.traducir(tabla);
 		}
 	
 	
     try 
     	{
         /*bw.write("mens1:     DATA \"Introduzca el numero:\" \n");
         bw.write("eol:            DATA \"\\n\"\n"+etiquetasputs);
         bw.write("valor_falso: DATA \"FALSE\"\n");
         bw.write("valor_verdad: DATA \"TRUE\"\n");
         bw.write("cadena_get: RES 1\n");
         */
         bw.close();
     	} 
     catch (IOException e) 
     	{
             // TODO
     	}
 	}
 
 
 
 //***********************************************************************************************
 	private void EjecutarAsignacion(String op1, String op2, TablaSimbolos ambito_terceto)
 		{
 		
 	    try 
 	    	{
 	    	Simbolo simbolo_op1 = ambito_terceto.GetSimbolo(op1);
 	    	int op2ent = Integer.parseInt(op2);
 	    	bw.write("ADD .IX, " + simbolo_op1.GetDesplazamiento() + "\n");//Tenemos en A la direccion donde dejamos el resultado de la asignacion
 	    	bw.write("MOVE .A, R5\n"); 
 	    	bw.write("MOVE "+ op2 + ", [.A]\n");
 	        bw.close();
 	    	} 
     catch (IOException e) 
 	    	{
 	            // TODO
 	    	}
 		}
     
     private void separar(String linea)
     	{
         int u= linea.indexOf(",");
         this.operacion=linea.substring(0,u); //cogemos la operación
         linea=linea.substring(u+1);
         
         u= linea.indexOf(",");
         op1=linea.substring(0,u);
         linea=linea.substring(u+1);
 
         u= linea.indexOf(",");
         op2=linea.substring(0,u);
         linea=linea.substring(u+1);
 
         op3=linea.substring(0,linea.indexOf("\n"));
     	}
     
     
     public void traducir(Tablas tabla)
     	{
     	
     	}
 
 
 }
