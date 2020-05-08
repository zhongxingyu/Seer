 package ar.com.stomalab.souyaban.model;
 
 import java.util.ArrayList;
 
 public class EscenarioLoader {
 	public EscenarioLoader(){}
 	
 	public Escenario cargarEscenario(ArrayList<String> lineas){
 		Escenario escenario = new Escenario();
 		int fila = 0;
 		
 		for (String linea : lineas){
			for (int columna = 0; columna < linea.length(); columna++){
			    char c = linea.charAt(columna);        
 			    switch (c){
 			    case '#':
 			    {
 			    	Pared pared = new Pared();
 			    	pared.setPosicion(columna, fila);
 			    	escenario.agregarPared(pared);
 			    	break;
 			    }
 			    case '.':
 			    {
 			    	Destino destino = new Destino();
 		            destino.setPosicion(columna, fila);
 		            escenario.agregarDestino(destino);
 		            break;
 			    }
 			    case '$':
 			    {
 			    	Caja caja = new Caja();
 			    	caja.setPosicion(columna, fila);
 			    	escenario.agregarCaja(caja);
 			    	break;
 			    }
 			    case '@':
 			    {
 			    	Persona persona = new Persona();
 			    	persona.setPosicion(columna, fila);
 			    	escenario.agregarPersona(persona);
 			    	break;
 			    }
 			    case '*':
 			    {
 			    	Destino destino = new Destino();
 		            destino.setPosicion(columna, fila);
 		            escenario.agregarDestino(destino);
 		            
 		            Caja caja = new Caja();
 			    	caja.setPosicion(columna, fila);
 			    	escenario.agregarCaja(caja);
 			    	break;
 			    }
 			    case '+':
 			    {
 			    	Destino destino = new Destino();
 		            destino.setPosicion(columna, fila);
 		            escenario.agregarDestino(destino);
 		            
 		            Persona persona = new Persona();
 			    	persona.setPosicion(columna, fila);
 			    	escenario.agregarPersona(persona);
 		            break;
 			    }
 			    	
 			    }
 			    
 			}
			fila++;
 		}
 		return escenario;
 	}
 	/*
 	def cargar_escenario lineas
     escenario = Escenario.new
     fila = 0
     min_fila = 1000000
     min_col = 1000000
     lineas.each do |linea|
       linea = strip_comentarios linea
       if !linea_en_blanco?(linea)
         fila = fila + 1
         
         (1..linea.length).each do |columna|
           # puts linea[columna - 1]
           case linea[columna - 1]
           when '#'  
             pared = Pared.new
             pared.set_posicion(columna, fila)
             escenario.agregar_pared(pared)
 
             min_col = columna if columna < min_col
             min_fila = fila if fila < min_fila
           when '.'
             target = Destino.new
             target.set_posicion(columna, fila)
             escenario.agregar_destino(target)
 
             min_col = columna if columna < min_col
             min_fila = fila if fila < min_fila
           when '$'
             caja = Caja.new
             caja.set_posicion(columna, fila)
             caja.set_escenario(escenario)
             escenario.agregar_caja(caja)
 
             min_col = columna if columna < min_col
             min_fila = fila if fila < min_fila
           when '@'
             persona = Persona.new
             persona.set_posicion(columna, fila)
             persona.set_escenario(escenario)
             escenario.agregar_persona(persona)
 
             min_col = columna if columna < min_col
             min_fila = fila if fila < min_fila
           when '*'
             target = Destino.new
             target.set_posicion(columna, fila)
             escenario.agregar_destino(target)
 
             caja = Caja.new
             caja.set_posicion(columna, fila)
             caja.set_escenario(escenario)
             escenario.agregar_caja(caja)
 
             min_col = columna if columna < min_col
             min_fila = fila if fila < min_fila
           when '+'
             target = Destino.new
             target.set_posicion(columna, fila)
             escenario.agregar_destino(target)
             
             persona = Persona.new
             persona.set_posicion(columna, fila)
             persona.set_escenario(escenario)
             escenario.agregar_persona(persona)
 
             min_col = columna if columna < min_col
             min_fila = fila if fila < min_fila
           end
         end
       end
     end
 
     shift_escenario escenario, min_fila, min_col
     escenario
   end
 
   private
   def linea_en_blanco? linea
     (linea.strip).empty?
   end
 
   def strip_comentarios linea
     linea = linea.split(';')[0]
     linea
   end
   */
 
 }
