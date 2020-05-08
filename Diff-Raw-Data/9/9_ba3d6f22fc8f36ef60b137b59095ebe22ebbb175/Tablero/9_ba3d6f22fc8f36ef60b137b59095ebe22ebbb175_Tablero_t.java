 package tablero;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Hashtable;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import municiones.DisparoConvencional;
 import municiones.MinaSubmarinaPorContacto;
 import municiones.Municion;
 import naves.Buque;
 import naves.Destructor;
 import naves.Direccion;
 import naves.EstadoDeSalud;
 import naves.Lancha;
 import naves.Nave;
 import naves.PortaAviones;
 import naves.RompeHielos;
 import naves.SeccionDeNave;
 import naves.Sentido;
 import tablero.Casillero;
 import excepciones.ErrorIdCasilleroInvalido;
 
 public class Tablero implements  TableroComunicable {
 	
 	private Hashtable<String, Casillero> coleccionCasilleros;
 	private LinkedList<Casillero> casillerosConMunicion;
 	private LinkedList<Nave> naves;
 
 	public Tablero() {
 		
 		this.coleccionCasilleros = new Hashtable<String,Casillero>();
 		this.casillerosConMunicion = new LinkedList<Casillero>();
 		this.naves = new LinkedList<Nave>();
 		
 		this.crearCasilleros();
 	}
 	
 	private void crearCasilleros(){
 
 	for (int x = 0; x < 10; x++) {
 		for (int y = 0; y < 10; y++) {
 			int[] id = { x, y };
 			String id_string = this.estandarizarId(id);
 			this.coleccionCasilleros.put(id_string, new Casillero(id));
 		}
 	}
 
 }
 	public  String estandarizarId(int[] id){
 		//Transforma el id de array al que se usa como llave en la tabla de casilleros
 		String id_string = Integer.toString(id[0]) + Integer.toString(id[1]);
 		return id_string;
 	}
 
 	public void construirYPosicionarLasNavesAleatoriamente() {
 		
 		Direccion[] arrayDeDirecciones = new Direccion[7];
 		Nave[] arrayDeNaves = new Nave[7];
 		
 		for (int i = 0; i < 7; i++) {
 			arrayDeDirecciones[i] = new Direccion(null);
 			arrayDeDirecciones[i].random();
 		}
 		
 		arrayDeNaves[0] = new Lancha(arrayDeDirecciones[0]);
 		arrayDeNaves[1] = new Lancha(arrayDeDirecciones[1]);
 		arrayDeNaves[2] = new Destructor(arrayDeDirecciones[2]);
 		arrayDeNaves[3] = new Destructor(arrayDeDirecciones[3]);
 		arrayDeNaves[4] = new Buque(arrayDeDirecciones[4]);
 		arrayDeNaves[5] = new PortaAviones(arrayDeDirecciones[5]);
 		arrayDeNaves[6] = new RompeHielos(arrayDeDirecciones[6]);
 		
 		for (int i =0; i<7; i++){
 			//int [] posProa = this.buscarCasilleroParaProa();
 			//this.posicionarNaveEnTablero(arrayDeNaves[i], posProa);
 			posicionarNaveAleatoriamenteEnTablero(arrayDeNaves[i]);
 			
 			
 			
 		}
 	}
 	
 		public void posicionarNaveAleatoriamenteEnTablero(Nave nave) {
 			
 			this.naves.add(nave);
 			Casillero unCasillero;
 			int[] id = new int[2];
 		
 			do {
 				id[0] = (int) (Math.random() * 10);
 				id[1] = (int) (Math.random() * 10);
 	
 				unCasillero = this.obtenerCasillero(id);
 			} while (!this.naveEntraEn(nave, unCasillero));
 			
 			for(SeccionDeNave unaSeccionDeNave : nave){
 				id = this.proximoCasilleroAlPosicionar(unCasillero, nave.direccion());
 				unCasillero = this.obtenerCasillero(id);
 				unCasillero.ponerSeccionDeNave(unaSeccionDeNave);
 			}
 
 		
 		}
		
 
 		/* Sirve para obtener el casillero proximo en la direccion */
 		public int[] proximoCasillero(Casillero casillero, Sentido sentido) {
 			int[] nuevoId = new int[2]; 
 			
 			if (sentido == Sentido.NOROESTE) {
 				nuevoId[0] = casillero.id()[0] - 1;
 				nuevoId[1] = casillero.id()[1] + 1;
 			} else if (sentido == Sentido.NORTE) {
 				nuevoId[0] = casillero.id()[0];
 				nuevoId[1] = casillero.id()[1] + 1;
 			} else if (sentido == Sentido.NORESTE) {
 				nuevoId[0] = casillero.id()[0] + 1;
 				nuevoId[1] = casillero.id()[1] + 1;
 			} else if (sentido == Sentido.ESTE) {
 				nuevoId[0] = casillero.id()[0] + 1;
 				nuevoId[1] = casillero.id()[1];
 			} else if (sentido == Sentido.SUDESTE) {
 				nuevoId[0] = casillero.id()[0] + 1;
 				nuevoId[1] = casillero.id()[1] - 1;
 			} else if (sentido == Sentido.SUR) {
 				nuevoId[0] = casillero.id()[0];
 				nuevoId[1] = casillero.id()[1] - 1;
 			} else if (sentido == Sentido.SUDOESTE) {
 				nuevoId[0] = casillero.id()[0] - 1;
 				nuevoId[1] = casillero.id()[1] - 1;
 			} else if (sentido == Sentido.OESTE) {
 				nuevoId[0] = casillero.id()[0] - 1;
 				nuevoId[1] = casillero.id()[1];
 			}
 
 			return nuevoId;
 		}
 		
 		/* Sirve para posicionar la nave */
 		public int[] proximoCasilleroAlPosicionar(Casillero casillero, Sentido sentido) {
 			int[] nuevoId = new int[2]; 
 			
 			if (sentido == Sentido.NOROESTE) {
 				nuevoId[0] = casillero.id()[0] + 1;
 				nuevoId[1] = casillero.id()[1] - 1;
 			} else if (sentido == Sentido.NORTE) {
 				nuevoId[0] = casillero.id()[0];
 				nuevoId[1] = casillero.id()[1] - 1;
 			} else if (sentido == Sentido.NORESTE) {
 				nuevoId[0] = casillero.id()[0] - 1;
 				nuevoId[1] = casillero.id()[1] - 1;
 			} else if (sentido == Sentido.ESTE) {
 				nuevoId[0] = casillero.id()[0] - 1;
 				nuevoId[1] = casillero.id()[1];
 			} else if (sentido == Sentido.SUDESTE) {
 				nuevoId[0] = casillero.id()[0] - 1;
 				nuevoId[1] = casillero.id()[1] + 1;
 			} else if (sentido == Sentido.SUR) {
 				nuevoId[0] = casillero.id()[0];
 				nuevoId[1] = casillero.id()[1] + 1;
 			} else if (sentido == Sentido.SUDOESTE) {
 				nuevoId[0] = casillero.id()[0] + 1;
 				nuevoId[1] = casillero.id()[1] + 1;
 			} else if (sentido == Sentido.OESTE) {
 				nuevoId[0] = casillero.id()[0] + 1;
 				nuevoId[1] = casillero.id()[1];
 			}
 
 			return nuevoId;
 		}
 		
 		private boolean naveEntraEn(Nave unaNave, Casillero unCasillero) {
			//Verifica que la nave entre, y que tenga un casillero al frente 
			//y detras, para moverse, esto impide que la nave quede bloqueada
			int[] id = this.proximoCasillero(unCasillero, unaNave.direccion());
 
			for (int tmp = 0; tmp < (unaNave.largo()+1); tmp++) {
 				id = this.proximoCasilleroAlPosicionar(unCasillero, unaNave.direccion());
 				try {
 					unCasillero = this.obtenerCasillero(id);
 				} catch (ErrorIdCasilleroInvalido e) {
 					return false;
 				}
 			}
 
 			return true;
 		}	
 			
 			
 	public LinkedList<Nave> devolverNaves(){
 		return this.naves;
 	}
 	
 
 	public void posicionarNaveEnTablero(Nave nave, int[] posicionDeProa) {
 		
 		int [] posProa = posicionDeProa;
 		//invierto la direccion de la nave, para que se valla armando para atras
 		nave.invertirSentido();
 		int [] patronDeTrayectoria = this.patronDeSumaParaTrayectoriaDeNave(nave.direccion());
 		nave.invertirSentido();
 		int [] posSeccion = posProa;
 		Casillero casillero;
 		
 		this.naves.add(nave);
 		for (SeccionDeNave seccion : nave.secciones()){
 			casillero = coleccionCasilleros.get(this.estandarizarId(posSeccion));
 			casillero.ponerSeccionDeNave(seccion);
 			
 			//Calcula la poiscion de la siguiente seccion.
 			posSeccion = this.sumarPatronDeSumaEId(posSeccion, patronDeTrayectoria);
 		}
 
 	}
 
 	public Casillero obtenerCasillero(int[] id) throws ErrorIdCasilleroInvalido {
 		Casillero.validarId(id);
 		String idString = this.estandarizarId(id);
 		return this.coleccionCasilleros.get(idString);
 
 
 	}
 
 	public boolean tieneNaves(){
 		if (this.cantidadTotalDeNaves() != 0)
 			return true;
 		return false;
 	}
 	
 	public int cantidadTotalDeNaves() {
 		//Cantidad total de naves, sin distincion de estado
 		return this.naves.size();
 	}
 	
 	public int cantidadDeNavesActivas(){
 		//Devuelve la cantidad de naves sanas y dañadas
 		int totalNavesActivas =0;
 		for (Nave nave : this.naves){
 			if( nave.estado()== EstadoDeSalud.SANO || nave.estado()== EstadoDeSalud.DANADO)
 				totalNavesActivas +=1;
 		}
 		return totalNavesActivas;
 	}
 	
 	public int cantidadDeNavesDestruidas(){
 		int totalNaves = this.cantidadTotalDeNaves();
 		int totalNavesActivas = this.cantidadDeNavesActivas();
 		return (totalNaves - totalNavesActivas);
 	}
 
 
 	
 	public void agregarCasilleroConMunicion(Casillero casillero) {
 		this.casillerosConMunicion.add(casillero);
 	}
 	
 	public void actualizarTablero() {
 //		0-Estallan las minas que tengan retardo 0/  - hecho
 //		1-Se resta 1 al retardo de la municion  - hecho
 //		2-Se mueven las naves
 
 		
 		
 		for (Casillero casillero : coleccionCasilleros.values()){
 			if( casillero.tieneMuniciones() ){
 				List<Municion> municiones = casillero.devolverMuniciones();
 				
 				for( Municion municion : municiones){
 					
 					if (municion.retardo() == 0){
 						
 						if(casillero.tieneSeccionesDeNave()){
 							//Quita las minas por contacto tambien
 							municiones.remove(municion);							
 						}
 						else if (!(municion instanceof MinaSubmarinaPorContacto)){
 							//Quita las minas que esten con retardo 0 pero que no sean por contacto
 							municiones.remove(municion);
 						}//if
 						
 						for (SeccionDeNave seccion : casillero.devolverSeccionesDeNave()){
 							seccion.recibirImpacto(municion);
 						}//for
 						
 					}//if
 					else
 						municion.disminuirRetardo();
 					
 				}//for
 				
 			}//if
 		}//for
 		
 		this.moverTodasLasNaves();
 		
 
 
 	}
 	
 	public void moverTodasLasNaves(){
 		
 		this.invertirSentidoDeNavesEnElBorde();
 		this.avanzarNaves();
 	}
 	
 	public void avanzarNaves(){
 		int[] idCasilleroProximo;
 		List<SeccionDeNave> seccionesYaMovidas = new LinkedList<SeccionDeNave>();
 		for ( Casillero casillero : coleccionCasilleros.values()){
 			
 			
 			List<SeccionDeNave> secciones = new LinkedList<SeccionDeNave>();
 			for (SeccionDeNave seccion : casillero.devolverSeccionesDeNave()){
 				secciones.add(seccion);
 			}
 			for (SeccionDeNave seccion : secciones){
 				if(!seccionesYaMovidas.contains(seccion)){
 				casillero.quitarSeccion(seccion);
 				idCasilleroProximo = this.proximoCasillero(casillero, seccion.sentido());
 				Casillero casilleroProximo = this.obtenerCasillero(idCasilleroProximo);
 				
 				casilleroProximo.ponerSeccionDeNave(seccion);
 				seccionesYaMovidas.add(seccion);
 				}
 			}
 		}
 		seccionesYaMovidas.clear();
 		
 	}
 	
 	public void invertirSentidoDeNavesEnElBorde() {
 		// Las naves afectadas son aquellas que estan en el borde
 
 		for (int x = 0; x < 10; x++) {
 			for (int y = 0; y < 10; y++) {
 				int[] idDeBorde = { x, y };
 
 				if (this.esIdDeCasilleroDeBorde(x, y)) {// D
 					Casillero casilleroDelBorde = this
 							.obtenerCasillero(idDeBorde);
 					List<SeccionDeNave> seccionesDeNaveEnBorde = casilleroDelBorde
 							.devolverSeccionesDeNave();
 
 					for (SeccionDeNave seccion : seccionesDeNaveEnBorde) {
 						int[] siguienteId = this.proximoCasillero(casilleroDelBorde, seccion.sentido());
 						try{
 							this.obtenerCasillero(siguienteId);
 						} catch (ErrorIdCasilleroInvalido e){
 							//el siguiente casillero se fue del mapa
 							seccion.invertirSentido();
 						}
 					}
 				}
 
 			}
 		}
 
 	}
 	
 	public boolean esIdDeCasilleroDeBorde(int x, int y){
 		//Pequeño grafico para entender el if
 //		
 //		AAAAAAAAAB
 //		C		 B
 //		C		 B
 //		C		 B
 //		C		 B
 //		C		 B
 //		C		 B
 //		C		 B
 //		C		 B
 //		C		 B
 //		CDDDDDDDDD
 
 		if (((x == 0) && (y >= 1 && y <= 9)) // C
 				|| ((x == 9) && (y >= 0 && y <= 9)) // B
 				|| ((x >= 0 && x <= 8) && y == 0) // A
 				|| ((x >= 1 && x <= 9) && y == 9))/*D*/ {
 			
 			return true;
 		}
 		return false;
 
 	}
 	
 	public boolean esIdDeCasilleroDeBorde(int[] idAProbar){
 		return esIdDeCasilleroDeBorde(idAProbar[0], idAProbar[1]);
 	}
 
 	
 	private int[] sumarPatronDeSumaEId(int[] id, int[] patronDeSuma) {
 		int[] total = new int[2];
 		total[0] = id[0] + patronDeSuma[0];
 		total[1] = id[1] + patronDeSuma[1];
 
 		return total;
 	}
 
 	
 	private int[] patronDeSumaParaTrayectoriaDeNave(Sentido sentido) {
 		// Devuelve un patron para sumarle al idCelda y ubicar las secciones de
 		// nave
 		int[] patronSur = { 0, -1 };
 		int[] patronNorte = { 0, 1 };
 		int[] patronOeste = { -1, 0 };
 		int[] patronEste = { 1, 0 };
 		int[] patronSudEste = { 1, -1 };
 		int[] patronNorEste = { 1, 1 };
 		int[] patronSudOeste = { -1, -1 };
 		int[] patronNorOeste = { -1, 1 };
 
 		if (sentido == Sentido.SUR)
 			return patronSur;
 		else if (sentido == Sentido.NORTE)
 			return patronNorte;
 		else if (sentido == Sentido.ESTE)
 			return patronEste;
 		else if (sentido == Sentido.OESTE)
 			return patronOeste;
 		else if (sentido == Sentido.NORESTE)
 			return patronNorEste;
 		else if (sentido == Sentido.NOROESTE)
 			return patronNorOeste;
 		else if (sentido == Sentido.SUDESTE)
 			return patronSudEste;
 		else
 			// (sentido== Sentido.SUDOESTE)
 			return patronSudOeste;
 	}
 
 	public void imprimirTablero() {
 		
 		for (int y = 9; y >= 0; y--) {
 			for (int x = 0; x < 10; x++) {
 				int[] id = { x, y };
 				if(this.obtenerCasillero(id).devolverSeccionesDeNave().isEmpty()){
 					System.out.print(".");
 				}else{
 					System.out.print("0");
 				}
 			}
 			System.out.println(" ");
 		}
 		System.out.println(" ");
 	}
 	
 	
 }
