 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controladors;
 
 
 import elementos.*;
 import java.util.*;
 
 /**
  *
  * @author ivanmartinez y ArclorenSarth
  */
 public class CtrlDomElementos {
     
     private int OID;
     private HashMap<Integer,ArrayList<Elemento>> mapTipoElem0;
     private HashMap<Integer,ArrayList<Elemento>> mapTipoElem1;
     private HashMap<Integer,ArrayList<Elemento>> mapTipoElem2;
     private HashMap<Integer,ArrayList<Elemento>> mapTipoElem3;
     private TreeMap<String,Elemento> mapElem0;
     private TreeMap<String,Elemento> mapElem1;
     private TreeMap<String,Elemento> mapElem2;
     private TreeMap<String,Elemento> mapElem3;
     private TreeMap<Integer,String> TradOIDtoName;
     private CtrlDomRestricciones RestDOM;
     private stubbedElementosGDP GDPElem;
     private static CtrlDomElementos INSTANCE = null;
     private static boolean init=false;
 
 
 
     /**
      * Creadora de la clase Controlador de elementos - DOMINIO
      * @return -
      */
     private CtrlDomElementos(){
         GDPElem = stubbedElementosGDP.getInstance();
         mapElem0 = new TreeMap<>();
         mapTipoElem0 = new HashMap<>();
         mapElem1 = new TreeMap<>();
         mapTipoElem1 = new HashMap<>();
         mapElem2 = new TreeMap<>();
         mapTipoElem2 = new HashMap<>();
         mapElem3 = new TreeMap<>();
         mapTipoElem3 = new HashMap<>();
         TradOIDtoName = new TreeMap<>();
             for(int j=1;j<4;++j){
                 ArrayList<Elemento> aux1 = new ArrayList<Elemento>();
                 ArrayList<Elemento> aux2 = new ArrayList<Elemento>();
                 ArrayList<Elemento> aux3 = new ArrayList<Elemento>();
                 ArrayList<Elemento> aux4 = new ArrayList<Elemento>();
                 mapTipoElem0.put(j, aux1);
                 mapTipoElem1.put(j, aux2);
                 mapTipoElem2.put(j, aux3);
                 mapTipoElem3.put(j, aux4);
 
             }
         ArrayList<Elemento> elems = new ArrayList();
         ArrayList<Integer> tipos = new ArrayList();
         ArrayList<Integer> tbarrios = new ArrayList();
         GDPElem.leerElementos(elems,tipos,tbarrios);
         for(int i=0; i<elems.size(); ++i){
             anadir_a_estructuras(elems.get(i),(int)tipos.get(i),(int)tbarrios.get(i));
         }
         if(TradOIDtoName.isEmpty()) OID = 1;
         else OID=TradOIDtoName.lastKey() + 1;
 
     }
 
     private static void creaInstancia() {
 
         if (INSTANCE == null) {
             INSTANCE = new CtrlDomElementos();
         }
     }
 
     public static CtrlDomElementos getInstance() {
 
         if (INSTANCE == null) {
             creaInstancia();
         }
         return INSTANCE;
     }
 
     public void inicializar(){
         if(!init){
             init = true;
             RestDOM = CtrlDomRestricciones.getInstance();
             RestDOM.inicializar();
         }
     }
 
 
     /**
      * Encargado de mantener la consistencia de las estructuras de datos
      * @param e Elemento que se añade a las esructuras pertinentes
      * @param tipo Nos indica el tipo de elemento que es: 1-Vivienda 
      * 2-Publico 3-Comercio
      * @param TB Nos indica el tipo de barrio al que pertenece:
      * 0-Cualquier barrio, 0-Culquier barrio 1-Barrio bajo, 2-Barrio medio,
      * 3-Barrio alto 
      * @return Devuelve true si todo se ha realizado correctamente
      */
 
     private boolean anadir_a_estructuras(Elemento e,int tipo, int TB){
         String nombre = e.getNom();
         if(!mapElem0.containsKey(nombre) && !mapElem1.containsKey(nombre) &&
            !mapElem2.containsKey(nombre) && !mapElem3.containsKey(nombre)){
 
             TradOIDtoName.put(e.getId(), nombre);
             ArrayList<Elemento> aux;
             //añadimos elemento a la estructura que contiene los elementos y
             //añadimos el elemento a la estructura que contiene los elementos 
             //clasificados por el tipo
             switch(TB){
                 case 0: mapElem0.put(nombre, e);
                         aux = mapTipoElem0.get((Integer)tipo);
                         aux.add(e);
                         mapTipoElem0.put((Integer) tipo, aux);
                         break;
                 case 1: mapElem1.put(nombre, e);
                         aux = mapTipoElem1.get((Integer)tipo);
                         aux.add(e);
                         mapTipoElem1.put((Integer) tipo, aux);
                         break;
                 case 2: mapElem2.put(nombre, e);
                         aux = mapTipoElem2.get((Integer)tipo);
                         aux.add(e);
                         mapTipoElem2.put((Integer) tipo, aux);
                         break;
                 case 3: mapElem3.put(nombre, e);
                         aux = mapTipoElem3.get((Integer)tipo);
                         aux.add(e);
                         mapTipoElem3.put((Integer) tipo, aux);
                         break;
             }
             ++OID;
             return true;         
         }
         else return false;
     }
 
 
     /**
      * Encargada de crear un elemento y añadirlo a las esucturas de datos
      * @param Nombre Nombre del elemento.
      * @param Des Descripcion del elemento.
      * @param tipo Tipo de elemento: Vivienda,publico,comercio
      * @param TB Nos indica el tipo de barrio al que pertenece:
      * 0-Cualquier barrio, 0-Culquier barrio 1-Barrio bajo, 2-Barrio medio,
      * 3-Barrio alto.
      * @param tamX Tamano X del Elemento.
      * @param tamY Tamano Y del Elemento.
      * @param prec Precio del Elemento creado.
      * @param aux1 Capacidad de servicio/comercio/vivienda.
      * @param aux2 Tipo de servicio(solo utilizada en creacion de publico.
      * @return Devuelve true en caso de que todo se realize correctamente 
      */
     public boolean CrearElemento(String Nombre,String Des, int tipo, int TB,
                                  int tamX,int tamY,int prec,int aux1,int aux2) throws Exception{
 
         //System.out.println("Entra");
             boolean ret=true;
             if(mapElem0.containsKey(Nombre) || mapElem1.containsKey(Nombre) ||
             mapElem2.containsKey(Nombre) || mapElem3.containsKey(Nombre)){
                 throw new Exception("\nYa existe un elemento con el ese nombre\n");
             }
             if(tamX<0 || tamY<0){
                  throw new Exception("\nEl tamanoX y tamanoY del" + 
                  "edificio han de ser mayores que 0\n");
             }
             if(prec<0){
                  throw new Exception("\nEl precio ha de ser mayor que 0\n");
             }
             if(TB<0 || TB>3){
                 throw new Exception("\nTipo de Barrio Incorrecto\n");
             }
             if(aux1<0){
                 throw new Exception("\nCapacidad ha de ser mayor a 0\n");
             }
             
             switch(tipo){
                 case 1 : Vivienda v = new Vivienda(OID,aux1,tamX,tamY,prec,
                                                    TB);
                          v.setNom(Nombre);
                          v.setDescrpcio(Des);
                          ret = anadir_a_estructuras(v,tipo,TB);
                          if(ret) GDPElem.escribirElemento(v);
                          break;
                 case 2 : Publico p = new Publico(OID,aux2,aux1,tamX,tamY,
                                                  prec,TB);
                          
                          if(aux2<1 || aux2>5){
                              throw new Exception("\nTipo de servicio publico" +
                                      "Incorrecto\n");
                          }
                          p.setNom(Nombre);
                          p.setDescrpcio(Des);
                          ret = anadir_a_estructuras(p,tipo,TB);
                          if(ret) GDPElem.escribirElemento(p);
                          break;
                 case 3 : Comercio c = new Comercio(OID,aux1,tamX,tamY,prec, 
                                                    TB);
                          c.setNom(Nombre);
                          c.setDescrpcio(Des);
                          ret = anadir_a_estructuras(c,tipo,TB);
                          if(ret) GDPElem.escribirElemento(c);
                          break;
                 default: throw new Exception("\nTipo de Edificio Incorrecto\n");
             }
 
             return ret;
 
     }
 
 
     /**
      * Funcion privada que nos indica que tipo de Elemento es el Elemento e.
      * @param e Elemento del cual queremos conocer el tipo.
      * @return Retorna el tipo de Elemento que es e: 1-Vivienda, 2-Publico,
      * 3-Comercio.
      */
     private int TipoElemento(Elemento e){
         if(e instanceof Vivienda)
             return 1;
         else if(e instanceof Publico)
             return 2;
         else 
             return 3;
     }
 
 
     /**
      * Funcion privada que se encarga de eliminar el Elemento con nombre
      * Elem de las estructuras de Elementos.
      * @param Elem Nombre del Elemento que queremos eliminar.
      * @param TBar Tipo de Barrio al que puede ir el Elemento.
      */
     private void eliminar_de_estructuras(String Elem,int TBar){
         int tipo;
         Elemento e;
         ArrayList<Elemento> aux;
         switch(TBar){
             case 0:
                 e = mapElem0.get(Elem);
                 TradOIDtoName.remove(e.getId());
                 tipo = TipoElemento(e);
                 aux = mapTipoElem0.get((Integer)tipo);
                 aux.remove(e);
                 mapElem0.remove(Elem);
                 break;
             case 1:
                 e = mapElem1.get(Elem);
                 TradOIDtoName.remove(e.getId());
                 tipo = TipoElemento(e);
                 aux = mapTipoElem1.get((Integer)tipo);
                 aux.remove(e);
                 mapElem1.remove(Elem);
                 break;
            case 2:
                 e = mapElem2.get(Elem);
                 TradOIDtoName.remove(e.getId());
                 tipo = TipoElemento(e);
                 aux = mapTipoElem2.get((Integer)tipo);
                 aux.remove(e);
                 mapElem2.remove(Elem);
                 break;
            case 3:
                 e = mapElem3.get(Elem);
                 TradOIDtoName.remove(e.getId());
                 tipo = TipoElemento(e);
                 aux = mapTipoElem3.get((Integer)tipo);
                 aux.remove(e);
                 mapElem3.remove(Elem);
                 break;                    
         }
     }
 
 
     /**
      * Funcion que se encarga de eliminar un Elemento del sistema. La 
      * funcion primero ha de comprobar si tal elemento existe dentro del 
      * sistema, y en caso de que exista, comprueba que ese elemento no tenga
      * ninguna Restriccion asociada a su identificador ni que ese Elemento
      * sea utilizado en algun Barrio. Si el Elemento si que tiene alguna
      * Restriccion asociada a su identificador o se utiliza en algun Barrio,
      * entonces no se podra eliminar ese Elemento hasta que no se eliminen
      * las Restricciones que se aplican sobre el y se elimine de todos los 
      * Barrios en los que se utiliza.
      * @param Elem Nombre del Elemento que queremos eliminar.
      */
     public void eliminarElemento(String Elem) throws Exception{
         boolean ret = false;
         Elemento e;
         if(mapElem0.containsKey(Elem)){
             e = mapElem0.get(Elem);
             if(e.getId()<=5){
                 throw new Exception("\nEste elemento pertenece al sistema.\n"
                     + "Los elementos del sistema no se pueden eliminar\n");
             }
             if(!GDPElem.existeElemEnBarrios(e.getId()) && 
                !RestDOM.existeRestElem(e.getId())){
                 eliminar_de_estructuras(Elem,0);
                 GDPElem.eliminarElemDisco(Elem);
                 ret = true;
             }
         }
         else if(mapElem1.containsKey(Elem)){
             e = mapElem1.get(Elem);
             if(e.getId()<=5){
                 throw new Exception("\nEste elemento pertenece al sistema.\n"
                     + "Los elementos del sistema no se pueden eliminar\n");
             }
             if(!GDPElem.existeElemEnBarrios(e.getId()) && 
                !RestDOM.existeRestElem(e.getId())){
                 eliminar_de_estructuras(Elem,1);
                 GDPElem.eliminarElemDisco(Elem);
                 ret = true;
             }                
         }
         else if(mapElem2.containsKey(Elem)){
             e = mapElem2.get(Elem);
             if(e.getId()<=5){
                 throw new Exception("\nEste elemento pertenece al sistema.\n"
                     + "Los elementos del sistema no se pueden eliminar\n");
             }
             if(!GDPElem.existeElemEnBarrios(e.getId()) && 
                !RestDOM.existeRestElem(e.getId())){
                 eliminar_de_estructuras(Elem,2);
                 GDPElem.eliminarElemDisco(Elem);
                 ret = true;
             }                
         }
         else if(mapElem3.containsKey(Elem)){
             e = mapElem3.get(Elem);
             if(e.getId()<=5){
                 throw new Exception("\nEste elemento pertenece al sistema.\n"
                     + "Los elementos del sistema no se pueden eliminar\n");
             }
             if(!GDPElem.existeElemEnBarrios(e.getId()) && 
                !RestDOM.existeRestElem(e.getId())){
                 eliminar_de_estructuras(Elem,3);
                 GDPElem.eliminarElemDisco(Elem);
                 ret = true;
             }                
         }
         else
             throw new Exception("El elemento no existe");
         
     }
 
 
     /**
      * Funcion que busca el Elemento con el Nombre Elem y devuelve su 
      * instancia o null si ese elemento no existe.
      * @param Elem Nombre del Elemento buscado.
      * @return Devuelve la instancia del Elemento con nombre Elem o null si 
      * tal Elemento no existe.
      */
     public Elemento getElemento(String Elem){
         Elemento e;
         if((e=mapElem0.get(Elem))!=null){}
         else if((e=mapElem1.get(Elem))!=null){}
         else if((e=mapElem2.get(Elem))!=null){}
         else if((e=mapElem3.get(Elem))!=null){}
         else e=null;
         return e;
     }
 
 
     /**
      * Funcion que devuelve el identificador numerico del Elemento con nombre 
      * Elem.
      * @param Elem Nombre del Elemento del cual queremos obtener el 
      * identificador
      * @return Retorna el identificador numerico del Elemento o null en caso de
      * que tal Elemento no exista.
      */
     public Integer getOID(String Elem){
         Integer oid = null;
         Elemento e;
         if((e=mapElem0.get(Elem))!=null){}
         else if((e=mapElem1.get(Elem))!=null){}
         else if((e=mapElem2.get(Elem))!=null){}
         else if((e=mapElem3.get(Elem))!=null){}
         else e=null;
         if(e!=null)
             oid =(Integer) e.getId();
         return oid;
     }
 
 
     /**
      * Fucnion que obtiene el nombre de un Elemento con identificador
      * OID.
      * @param OID Identificador del elemento del cual queremos obtener el 
      * nombre. 
      * @return Retorna el nombre del Elemento con el identificador OID.
      */
     public String NombreElemento(Integer OID){
         return TradOIDtoName.get(OID);
     }
 
 
     /**
      * Consulta en que tipo de Barrio puede ir el elemento e 
      * @param e Elemente del cual queremos consultar el tipo de barrio
      * @return TBarrio del elemento
      */
     public int getTBElemento(Elemento e){
         int tipoel;
         if(e instanceof Vivienda){
             Vivienda e2 = (Vivienda) e;
             tipoel = e2.getTBarrio();
         }
         else if(e instanceof Publico){
             Publico e2 = (Publico) e;
             tipoel = e2.getTBarrio();
         }
         else {
             Comercio e2 = (Comercio) e;
             tipoel = e2.getTBarrio();
         }
         return tipoel;
     }
 
 
     /**
      * Array de los elementos del sistema
      * @return Array de todos los elementos del sistema
      */
      private ArrayList<Elemento> ArrayElem(){
 
         ArrayList<Elemento> aux = new ArrayList();
         aux.addAll(mapElem0.values());
         aux.addAll(mapElem1.values());
         aux.addAll(mapElem2.values());
         aux.addAll(mapElem3.values());
         return aux;
 
      }
 
 
      /**
      * Array de un tipo de elementos del sistema de un tipo de Barrio.
      * @param TB Tipo de Barrio del que se queire listar.
      * @param tipo Tipo de Elemento que queremos listar: 1-Viienda, 
      * 2-Publico y 3-Comercio.
      * @return Array de todos los elementos del sistema de un determinado 
      * tipo
      */
      private ArrayList<Elemento> arrayElemTipo(int TB, int tipo){
         if(TB<0 || TB>3 || tipo < 1 || tipo>3) return null; 
         ArrayList<Elemento> aux = new ArrayList();
         switch(TB){
             case 0:
                 aux.addAll(mapTipoElem0.get((Integer)tipo));
                 break;
             case 1:
                 aux.addAll(mapTipoElem1.get((Integer)tipo));
                 break;
             case 2:
                 aux.addAll(mapTipoElem2.get((Integer)tipo));
                 break;
             case 3:
                 aux.addAll(mapTipoElem3.get((Integer)tipo));
                 break;
         }
         return aux;
      }
 
      
      /**
       * Funcion que lista un tipo de Elementos de un determinado tipo de Barrio.
       * @param TB Tipo de Barrio del que se quiere listar los Elementos.
       * @param tipo Tipo de Elemento que se quieren listar.
       * @return Retorna una matriz de Strings con los parametros de los 
       * Elementos deseados.
       */
      public String[][] listarElemTipo(int TB,int tipo){
          ArrayList<Elemento> elem = arrayElemTipo(TB,tipo);
          String[][] mat = null;
          if(!elem.isEmpty()) mat = new String[elem.size()][7];
          Vivienda v;
          Publico p;
          Comercio c;
          
          for(int i=0; i<elem.size(); ++i){
              switch(tipo){        
                  case 1:    
                      v = (Vivienda) elem.get(i);
                      mat[i][0] = v.getNom();
                      mat[i][1] = String.valueOf(v.getTBarrio());
                      mat[i][2] = String.valueOf(v.getPrecio());
                      mat[i][3] = String.valueOf(v.Getcap_max());
                      mat[i][4] = String.valueOf(v.getTamanoX());
                      mat[i][5] = String.valueOf(v.getTamanoY());
                      mat[i][6] = "-1";
                      break;
 
                  case 2:
                      p = (Publico) elem.get(i);
                      mat[i][0] = p.getNom();
                      mat[i][1] = String.valueOf(p.getTBarrio());
                      mat[i][2] = String.valueOf(p.getPrecio());
                      mat[i][3] = String.valueOf(p.Gettipo());
                      mat[i][4] = String.valueOf(p.Getcapacidad_serv());
                      mat[i][5] = String.valueOf(p.getTamanoX());
                      mat[i][6] = String.valueOf(p.getTamanoY());
                      break;
 
                  case 3:
                      c = (Comercio) elem.get(i);
                      mat[i][0] = c.getNom();
                      mat[i][1] = String.valueOf(c.getTBarrio());
                      mat[i][2] = String.valueOf(c.getPrecio());
                      mat[i][3] = String.valueOf(c.getCapacidad());
                      mat[i][4] = String.valueOf(c.getTamanoX());
                      mat[i][5] = String.valueOf(c.getTamanoY());
                      mat[i][6] = "-1";
                      break;
             }
          }
          return mat;
      }
 
 
      /**
       * Listado de nombres de los elementos del sistema
       * @return Devuelve un set con los nombres de los elementos del sistema 
       */
      public Set<String> ListaNombreElementos(){
 
         Set<String> aux = new HashSet();
         if(!mapElem0.isEmpty())
             aux.addAll(mapElem0.keySet());
         if(!mapElem1.isEmpty())
             aux.addAll(mapElem1.keySet());
         if(!mapElem2.isEmpty())
             aux.addAll(mapElem2.keySet());
         if(!mapElem3.isEmpty())
             aux.addAll(mapElem3.keySet());
 
         return aux;
      }
      
      
      /**
       * Funcion que ayuda a la interfaz grafica obtener los parametros para cada
       * elemento.
       * @param Elem Elemento del cual queremos obtener lo parametros.
       * @return Retorna un Array de Strings con los parametros del Elemento.
       */
      public String[] elemParms(String Elem){
          Elemento e;
          String[] par = new String[4];
          if(mapElem0.containsKey(Elem))
              e = mapElem0.get(Elem);
          else if(mapElem1.containsKey(Elem))
              e = mapElem1.get(Elem);
          else if(mapElem2.containsKey(Elem))
             e = mapElem1.get(Elem);
          else
              e = mapElem3.get(Elem);
         
          if(e instanceof Vivienda){
              Vivienda v = (Vivienda) e;
              par[0] = "v";
              par[1] = String.valueOf(v.getPrecio());
              par[2] = String.valueOf(v.Getcap_max());
              par[3] = "none";
          }
          else if(e instanceof Comercio){
              Comercio c = (Comercio) e;
              par[0] = "c";
              par[1] = String.valueOf(c.getPrecio());
              par[2] = String.valueOf(c.getCapacidad());
              par[3] = "none";
          }
          else {
              Publico p = (Publico) e;
              par[0] = "p";
              par[1] = String.valueOf(p.getPrecio());
              par[2] = String.valueOf(p.Getcapacidad_serv());
              String t = new String();
              switch(p.Gettipo()){
                  case 1: t="Sanidad";
                          break;
                  case 2: t="Educacion";
                          break;
                  case 3: t="Seguridad";
                          break;
                  case 4: t="Comunicacion";
                          break;
                  case 5: t="Ocio";
                          break;
              }
              par[3] = t;
          }
          return par;
          
      }
 
       /**
       * Listado de nombres de los elementos del sistema de un tipo de 
       * elemento y de un tipo de barrio.
       * @param TB Tipo de Barrio del que se quiere listar los elementos.
       * @param tipo Tipo de Elemento del que se quiere listar los elementos.
       * @return Devuelve un set con los nombres de los elementos del sistema
       * de cierto tipo de barrio y cierto tipo de elemento.
       */
      public Set<String> ListaNombreElementosTipo(int TB, int tipo){
         if(TB<0 || TB>3 || tipo < 1 || tipo>3) return null; 
         Set<String> ret = new HashSet();
         ArrayList<Elemento> aux=null;
         Elemento e;
         switch(TB){
             case 0:
                 aux = mapTipoElem0.get((Integer)tipo);
                 break;
             case 1:
                 aux = mapTipoElem1.get((Integer)tipo);
                 break;
             case 2:
                 aux = mapTipoElem2.get((Integer)tipo);
                 break;
             case 3:
                 aux = mapTipoElem3.get((Integer)tipo);
                 break;
         }
         for(int i=0; i<aux.size(); ++i){
             e = aux.get(i);
             ret.add(e.getNom());
         }
         return ret;
      }
 
 }
