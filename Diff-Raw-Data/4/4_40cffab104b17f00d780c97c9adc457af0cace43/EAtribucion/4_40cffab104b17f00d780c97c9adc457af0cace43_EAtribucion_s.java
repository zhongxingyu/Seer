 package plg.proto;
 
 import java.util.Iterator;
 import java.util.Vector;
 
 import es.ucm.fdi.plg.evlib.Atribucion;
 import es.ucm.fdi.plg.evlib.Atributo;
 import es.ucm.fdi.plg.evlib.LAtributo;
 import es.ucm.fdi.plg.evlib.SemFun;
 import es.ucm.fdi.plg.evlib.TAtributos;
 
 import Procedimientos.CodigoProcs;
 import TablaSimbolos.TablaSimbolos;
 import Tipos.CompruebaTipos;
 import Tipos.Tipo;
 import Tipos.TipoArray;
 import Tipos.TipoBasico;
 import Tipos.TipoConstante;
 import Tipos.TipoElemTupla;
 import Tipos.TipoError;
 import Tipos.TipoParametro;
 import Tipos.TipoSubprograma;
 import Tipos.TipoTupla;
 
 // Definicin de las funciones semnticas
 
 @SuppressWarnings("unused")
 class Asignacion implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
         return args[0].valor();
     }
     
 }
 
 class AsignacionDependencias1 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String cod = (String) args[1].valor();
     	return args[0].valor();
     }
     
 }
 
 class DevuelveConst implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
         return new String("const");
     }
     
 }
 
 class DevuelveTipo implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
         return new String("tipo");
     }
     
 }
 
 class creaTipoBasicoCharacter implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	TipoBasico t = new TipoBasico("character");
     	return t;
     }
     
 }
 
 class creaTipoBasicoInt implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	TipoBasico t = new TipoBasico("integer");
     	return t;
     }
     
 }
 
 class creaTipoBasicoNat implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	TipoBasico t = new TipoBasico("natural");
     	return t;
     }
     
 }
 
 class creaTipoBasicoFloat implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	TipoBasico t = new TipoBasico("float");
     	return t;
     }
     
 }
 
 class creaTipoBasicoBoolean implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	TipoBasico t = new TipoBasico("boolean");
     	return t;
     }
     
 }
 
 class CreaTipoNoBasicoIden implements SemFun{
 
 	 @Override
 	    public Object eval(Atributo... args) {
 	        TablaSimbolos ts = (TablaSimbolos) args[0].valor();
 	        String id = (String) args[1].valor();
 	        
 	        if (!ts.existeId(id)) return new TipoError("terror");
 	        else if (!ts.esTipo(id)) return new TipoError("terror");
 	        else return ts.getTipo(id);
 	    }
     
 }
 
 class CreaTuplaVacia implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	TipoTupla t = new TipoTupla();
     	return t;
     }
     
 }
 
 class TsDameValorInt implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
         TablaSimbolos ts = (TablaSimbolos) args[0].valor();
     	return ts.dameValorCte((String)args[1].valor());
     }
     
 }
 
 class DevuelveNull implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
         return null;
     }
     
 }
 
 class Devuelve0 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
         return new String("0");
     }
     
 }
 
 class Devuelve6 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
         return new String("6");
     }
     
 }
 
 
 class DevuelveVar implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
         return new String("var");
     }
     
 }
 
 class Concatena implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String res = "";
     	for (int i = 0; i < args.length; i++){
     		if (args[i]!=null)
     			res = res + args[i].valor();
     	}
     	return res;
     }
     
 }
 
 class DevuelveVacio implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	return new String("");
     }
     
 }
 
 
 class CodInicio implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	TablaSimbolos ts = (TablaSimbolos) args[0].valor();
     	String cod1 = (String) args[1].valor();
     	
     	CodigoProcs c = new CodigoProcs();
     	String cod = "";
     	int tam = ts.dameTamDatosGlobales();
     	cod = c.codInicio(tam);
     	cod += cod1;
     	cod += "stop";
     
     	return cod;
     }
     
 }
 
 
 class CodProg implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String codConst = (String) args[0].valor();
     	String etqSub = (String) args[1].valor();
     	String codSub = (String) args[2].valor();
     	String codInstr= (String) args[3].valor();
 
     	String cod = codConst;
     	cod += "ir_a(" + etqSub + ")\n";
     	cod += codSub;
     	cod += codInstr;
     	
     	return cod;
     }
     
 }
 
 
 class CompruebaYanyadeIdConst implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	TablaSimbolos ts = (TablaSimbolos) args[0].valor();
         String iden = (String) args[1].valor();
         String err1 = (String) args[2].valor();
         String err2 = (String) args[3].valor();
         String clase = (String) args[4].valor();
         Tipo tipo = (Tipo) args[5].valor();
         @SuppressWarnings("unused")
 		String err = (String) args[6].valor();
         
         if (!ts.existeId(iden) && !ts.esPalabraReservada(iden) && err1.equals("") && err2.equals("")) ts.anyadeId(iden, clase, tipo);
         return ts;
     }
     
 }
 
 class CodConstantes0 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String ctes1Cod = (String) args[0].valor();
     	String cteCod = (String) args[1].valor();
     	String id = (String) args[2].valor();
     	TablaSimbolos ts = (TablaSimbolos) args[3].valor();
     	String codRes = ctes1Cod + cteCod + "desapila_dir(" + Integer.toString(ts.getDir(id)) + ")\n";
     	return codRes;
     }
     
 }
 
 class ErrConstantes0 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String ctes1Err = (String) args[0].valor();
     	String cteErr = (String) args[1].valor();
     	TablaSimbolos ts = (TablaSimbolos) args[2].valor();
     	String id = (String) args[3].valor();
     	
     	String errRes = ctes1Err + cteErr;
     	if (ts.esPalabraReservada(id)) errRes = errRes + "Las constantes no pueden ser palabras reservadas.\n";
     	else if (ts.existeId(id)) errRes = errRes + "Identificador declarado previamente.\n";
     	return errRes;
     }
     
 }
 
 
 class CompruebaPRCreaTsYAnyadeId implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String iden = (String) args[0].valor();
     	String err = (String) args[1].valor();
     	String clase = (String) args[2].valor();
     	Tipo tipo = (Tipo) args[3].valor();
     	TablaSimbolos ts = new TablaSimbolos();
     	if (!ts.esPalabraReservada(iden) && err.equals("")) ts.anyadeId(iden, clase, tipo);
     	return ts;
     }
     
 }
 
 class CodConstantes1 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String cteCod = (String) args[0].valor();
     	String id = (String) args[1].valor();
     	TablaSimbolos ts = (TablaSimbolos) args[2].valor();
     	String codRes = cteCod + "desapila_dir(" + Integer.toString(ts.getDir(id)) + ")\n";
     	return codRes;
     }
     
 }
 
 class ErrConstantes1 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String cteErr = (String) args[0].valor();
     	TablaSimbolos ts = (TablaSimbolos) args[1].valor();
     	String id = (String) args[2].valor();
    
     	String errRes = cteErr;
     	if (ts.esPalabraReservada(id)) errRes = errRes + "Las constantes no pueden ser palabras reservadas.\n";
     	return errRes;
     }
     
 }
 
 
 class CreaTipoConstante implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	TipoBasico tipo = (TipoBasico) args[0].valor();
     	String valor = (String) args[1].valor();
     	
     	TipoConstante tCons = new TipoConstante(tipo.getTipo(), valor);
     	
     	return tCons;
     }
     
 }
 
 class ErrConstante implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	TipoBasico tipo = (TipoBasico) args[0].valor();
     	String valor = (String) args[1].valor();
     	String id = (String) args[2].valor();
    
     	String errRes = "";
     	if (!tipo.esCompatibleAsign(valor)) 
     		errRes ="Error de tipos al asignar el valor " + valor +  " a la constante " + id + "\n";
 
     	return errRes;
     }
     
 }
 
 class CompruebaTsYasigna implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	TablaSimbolos ts = (TablaSimbolos) args[0].valor();
     	
     	if (ts == null) ts = new TablaSimbolos();
     	return ts;
     }
     
 }
 
 class CompruebaYanyadeIdTipo implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	TablaSimbolos ts = (TablaSimbolos) args[0].valor();
     	String iden = (String) args[1].valor();
     	String err1 = (String) args[2].valor();
     	String err2 = (String) args[3].valor();
     	String clase = (String) args[4].valor();
     	Tipo tipo = (Tipo) args[5].valor();
     	@SuppressWarnings("unused")
 		String errDependencias = (String) args[6].valor();
     	
     	if (!ts.existeId(iden) && !ts.esPalabraReservada(iden) && err1.equals("") && err2.equals("")) ts.anyadeId(iden, clase, tipo);
     	return ts;
     }
     
 }
 
 
 class ErrTipos0 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String errTipos1 = (String) args[0].valor();
     	String errTipo = (String) args[1].valor();
     	TablaSimbolos ts = (TablaSimbolos) args[2].valor();
     	String id = (String) args[3].valor();
     	
     	String errSalida = errTipos1 + errTipo;
     	if (ts.existeId(id)){
     		if (ts.esPalabraReservada(id)) errSalida = errSalida + "El identificador no puede ser una palabra reservada\n";
     		else errSalida = errSalida + "El identificador esta repetido\n";
     	}
     	return errSalida;
     }
     
 }
 
 
 
 class CompruebaCreaTsYanyadeTipo implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	TablaSimbolos ts = (TablaSimbolos) args[0].valor();
     	String iden = (String) args[1].valor();
     	String err = (String) args[2].valor();
     	String clase = (String) args[3].valor();
     	Tipo tipo = (Tipo) args[4].valor();
     	@SuppressWarnings("unused")
 		String errDependencias = (String) args[5].valor();
     	if (ts==null) ts = new TablaSimbolos();
     	if (!ts.existeId(iden) && !ts.esPalabraReservada(iden) && err.equals("")) ts.anyadeId(iden, clase, tipo);
     	return ts;
     }
     
 }
 
 class ErrTipos1 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String tErr = (String) args[0].valor();
     	String id = (String) args[1].valor();
     	TablaSimbolos ts = (TablaSimbolos) args[2].valor();
     	
     	String err = tErr;
     	if (ts==null) ts = new TablaSimbolos();
     	if (ts.esPalabraReservada(id)) err = err + "El tipo " + id + " es una palabra reservada\n";
     	else if (ts.existeId(id)) err = err + "El idenfiticador " + id + " esta repetido\n";
     	
     	return err;
     }
     
 }
 
 class ErrTipoNoBasico0 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	TablaSimbolos ts = (TablaSimbolos) args[0].valor();
     	String id = (String) args[1].valor();
     	
     	String err = "";
 
     	if (!ts.existeId(id)) err = err + "El tipo " + id + " no ha sido declarado\n";
     	else {
     		if (ts.esPalabraReservada(id)) err = err + "El tipo " + id + " es una palabra reservada\n";
     		if (!ts.esTipo(id)) err = err + "El idenficador " + id + " no corresponde a un tipo\n";
     	}
     	
     	return err;
     }
     
 }
 
 
 class CompruebaTuplasAnyadeCampo implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String errTuplas1 = (String) args[0].valor();
     	String errDeclTipo = (String) args[1].valor();
     	TipoTupla tipoTuplas;
     	try{
     		tipoTuplas = (TipoTupla) args[2].valor();
     	} catch (Exception e) {
     		return new TipoError("terror");
     	}
     	Tipo tipoDecl = (Tipo) args[3].valor();
     	if (errTuplas1.equals("") && errDeclTipo.equals("")) tipoTuplas.anyadeElemTupla(tipoDecl);
     	return tipoTuplas;
     }
     
 }
 
 class CompruebaYCreaTuplaConCampo implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String err = (String) args[0].valor();
     	Tipo tipoDecl = (Tipo) args[1].valor();
     	
     	if (err.equals("")) return new TipoTupla(tipoDecl);
     	else return new TipoError("terror");
     }
     
 }
 
 class CompruebaYcreaArray implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String errIndice1 = (String) args[0].valor();
     	String errDeclTipo = (String) args[1].valor();
     	String nElem = (String) args[2].valor();
     	int nElemInt;
     	try {
     		nElemInt = Integer.parseInt(nElem);
     	} catch (Exception e){
     		return new TipoError("terror");
     	}
     	Tipo tipoDecl = (Tipo) args[3].valor();
     	
     	if (errIndice1.equals("") && errDeclTipo.equals("")) return new TipoArray(nElemInt, tipoDecl);
     	else return new TipoError("terror");
     }
     
 }
 
 class ErrTipoNoBasico2 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String errDecl = (String) args[0].valor();
     	String errIndice = (String) args[1].valor();
     	
     	String err = errDecl + errIndice;
     	return err;
     }
     
 }
 
 class ErrTipoIndice0 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	TablaSimbolos ts = (TablaSimbolos) args[0].valor();
     	String id = (String) args[1].valor();
     	
     	String err = "";
     	if (!ts.existeId(id)) err = err + "El identificador " + id + " no esta declarado\n";
     	else {
     		if (ts.esPalabraReservada(id)) err = err + "El identificador " + id +" es una palabra reservada\n";
     		if (!ts.esConstante(id)) err = err + "El identificador " + id + " debe ser una constante\n";
     		else {
     			String val = ts.dameValorCte(id);
     			try {
 					int a = Integer.parseInt(val);
 					if (a < 0) err = err + "El valor del identificador " + id + " no es compatible con natural\n";
 				} catch (Exception e) {
 					err = err + "El valor del identificador " + id + " no es compatible con natural\n";
 				}
     		}
     	}
     	
     	return err;
     }
     
 }
 
 
 class CompruebaYanyadeIdVariables0 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	TablaSimbolos ts = (TablaSimbolos) args[0].valor();
     	String iden = (String) args[1].valor();
     	String err1 = (String) args[2].valor();
     	String err2 = (String) args[3].valor();
     	String clase = (String) args[4].valor();
     	Tipo tipo = (Tipo) args[5].valor();
     	@SuppressWarnings("unused")
 		String err = (String) args[6].valor();
     	if (!ts.existeId(iden) && !ts.esPalabraReservada(iden) && err1.equals("")) ts.anyadeId(iden, clase, tipo);
     	return ts;
     }
     
 }
 
 
 /*class CodVariables0 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String cod1 = (String) args[0].valor();
     	TablaSimbolos ts = (TablaSimbolos) args[1].valor();
     	String id = (String) args[2].valor();
     	
     	if (!ts.existeId(id)) return new String("");
     	String cod = cod1 + "reserva(" + ts.getTipo(id).getTam() + ")\n";
     	return cod;
     }
     
 }*/
 
 
 class ErrVariables0 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String err1 = (String) args[0].valor();
     	String err2 = (String) args[1].valor();
     	TablaSimbolos ts = (TablaSimbolos) args[2].valor();
     	String id = (String) args[3].valor();
     	
     	String err = err1 + err2;
     	if (ts.esPalabraReservada(id)) err = err + "El identificador " + id + " es una palabra reservada\n";
     	else if (ts.existeId(id)) {
     		err = err + "El identificador " + id + " ya esta declarado previamente\n";
     	}
     	return err;
     }
     
 }
 
 
 class CompruebaCreaTsYAnyadeIdVariable implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	TablaSimbolos ts = (TablaSimbolos) args[0].valor();
     	String iden = (String) args[1].valor();
     	String err = (String) args[2].valor();
     	String clase = (String) args[3].valor();
     	Tipo tipo = (Tipo) args[4].valor();
     	if (ts==null) ts = new TablaSimbolos();
     	if (!ts.existeId(iden) && !ts.esPalabraReservada(iden) && err.equals("")) ts.anyadeId(iden, clase, tipo);
     	return ts;
     }
     
 }
 
 /*class ReservaVariable implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String iden = (String) args[0].valor();
     	TablaSimbolos ts = (TablaSimbolos) args[1].valor();
     	String cod = "reserva(" + Integer.toString(ts.getTam(iden)) + ")\n";
     	return cod;
     }
     
 }*/
 
 
 class ActivaNivelLocal implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	TablaSimbolos ts = (TablaSimbolos) args[0].valor();
     	if (ts == null) ts = new TablaSimbolos();
     	ts.activaNivel2();
     	return ts;
     }
     
 }
 
 class DesactivaNivelLocal implements SemFun{
 
     @SuppressWarnings("unused")
 	@Override
     public Object eval(Atributo... args) {
     	TablaSimbolos ts = (TablaSimbolos) args[0].valor();
     	String cod = (String) args[1].valor();
     	String err = (String) args[2].valor();
     	String etq = (String) args[3].valor();
     	ts.desactivaNivel2();
     	return ts;
     }
     
 }
 
 class CompruebaYanyadeIdSubprogramas0 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	TablaSimbolos ts = (TablaSimbolos) args[0].valor();
     	String id = (String) args[1].valor();
     	String clase = (String) args[2].valor();
     	TipoSubprograma tipo = (TipoSubprograma) args[3].valor();
     	String err = (String) args[4].valor();
     	
     	/*if (err.equals(""))*/ ts.anyadeIdPrimerNivel(id, clase, tipo);
     	return ts;
     }
     
 }
 
 class ErrSubprogramas0 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String err1 = (String) args[0].valor();
     	String err2 = (String) args[1].valor();
     	TablaSimbolos ts = (TablaSimbolos) args[2].valor();
     	String id = (String) args[1].valor();
     	
     	String err = err1 + err2;
     	if (ts.existeIdPrimerNivel(id)) err += "El nombre de subprograma no puede ser un identificador repetido\n";
     	
     	return err;
     }
     
 }
 
 class CompruebaYanyadeIdSubprogramas1 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	TablaSimbolos ts = (TablaSimbolos) args[0].valor();
     	String id = (String) args[1].valor();
     	String clase = (String) args[2].valor();
     	TipoSubprograma tipo = (TipoSubprograma) args[3].valor();
     	String err = (String) args[4].valor();
     	if (ts == null) ts = new TablaSimbolos();
     	
     	/*if (err.equals(""))*/ ts.anyadeIdPrimerNivel(id, clase, tipo);
     	return ts;
     }
     
 }
 
 
 class DevuelveProc implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	return new String("proc");
     }
     
 }
 
 class ConstruyeTipoSubprograma implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	@SuppressWarnings("unchecked")
 		Vector<TipoParametro> v = (Vector<TipoParametro>) args[0].valor();
     	String etqh = (String) args[1].valor();
     	
     	TipoSubprograma t = new TipoSubprograma(v, Integer.parseInt(etqh));
     	
     	return t;
     }
     
 }
 
 class CompruebaYanyadeIdSubprograma implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	TablaSimbolos ts = (TablaSimbolos) args[0].valor();
     	String id = (String) args[1].valor();
     	String clase = (String) args[2].valor();
     	TipoSubprograma tipo = (TipoSubprograma) args[3].valor();
     	//String err = (String) args[4].valor();
     	
     	/*if (err.equals(""))*/ ts.anyadeId(id, clase, tipo);
     	return ts;
     }
     
 }
 
 class CodSubprograma implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String cod1 = (String) args[0].valor();
     	String desp = (String) args[1].valor();
     	
     	
     	CodigoProcs c = new CodigoProcs();
     	String cod = "";
     	
     	int tam = Integer.parseInt(desp);
     	cod = c.codigoPrologo(tam);
     	cod += cod1;
     	cod += c.codigoEpilogo(tam);
     	
     	return cod;
     }
     
 }
 
 
 class Suma15 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String val = (String)args[0].valor();
     	int valInt = Integer.parseInt(val);
     	valInt = valInt + 15;
     	return Integer.toString(valInt);
     }
     
 }
 
 class Suma16 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String val = (String)args[0].valor();
     	int valInt = Integer.parseInt(val);
     	valInt = valInt + 16;
     	return Integer.toString(valInt);
     }
     
 }
 
 
 class ErrSubprograma implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String err0 = (String)args[0].valor();
     	String err1 = (String)args[1].valor();
     	TablaSimbolos ts = (TablaSimbolos) args[2].valor();
     	String id = (String)args[3].valor();
     	
     	String err = "";
     	err += err0 + err1;
     	if (ts.existeIdPrimerNivel(id)) err += "El nombre de subprograma no puede ser un identificador repetido\n";
     	
     	return err;
     }
     
 }
 
 class DevuelveListaVacia implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	Vector<TipoParametro> v = new Vector<TipoParametro>();
     	return v;
     }
     
 }
 
 
 class CompruebaYanyadeIdParametros0 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	
     	TablaSimbolos ts = (TablaSimbolos) args[0].valor();
     	String id = (String) args[1].valor();
     	String clase = (String) args[2].valor();
     	Tipo tipo = (Tipo) args[3].valor();
     	String err = (String) args[4].valor();
     	@SuppressWarnings("unused")
 		String errDependencias = (String) args[5].valor();
     	
     	if (err.equals("") && (!ts.esParametroRepetido(id))) ts.anyadeId(id, clase, tipo);
     	return ts;
     }
     
 }
 
 
 class AnyadeParam implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	@SuppressWarnings("unchecked")
 		Vector<TipoParametro> v0 = (Vector<TipoParametro>) args[0].valor();
     	@SuppressWarnings("unchecked")
 		Vector<TipoParametro> v1 = (Vector<TipoParametro>) args[1].valor();
     	@SuppressWarnings("unused")
 		String err = (String) args[2].valor();
     	
     	v0.add(v1.firstElement());
     	
     	return v0;
     }
     
 }
 
 
 class ErrParametros0 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	
     	String err1 = (String) args[0].valor();
     	String err2 = (String) args[1].valor();
     	@SuppressWarnings("unchecked")
 		Vector<TipoParametro> v0 = (Vector<TipoParametro>) args[2].valor();
     	@SuppressWarnings("unchecked")
 		Vector<TipoParametro> v1 = (Vector<TipoParametro>) args[3].valor();
     	
     	String err = err1 + err2;
     	Iterator<TipoParametro> it = v0.iterator();
     	while (it.hasNext()){
     		TipoParametro p = it.next();
     		if (p.getId().equals(v1.lastElement().getId())) err += "El nombre de uno de los parametros del subprograma esta repetido\n";
     	}
     	return err;
     }
     
 }
 
 class CompruebaYanyadeIdParametros1 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {    	
     	String err = (String) args[0].valor();
     	TablaSimbolos ts = (TablaSimbolos) args[1].valor();
     	String id = (String) args[2].valor();
     	String clase = (String) args[3].valor();
     	Tipo tipo = (Tipo) args[4].valor();
     	
     	if (err.equals("")) ts.anyadeId(id, clase, tipo);
     	return ts;
     }
     
 }
 
 class CreaParamsYanyade implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	TipoParametro param = (TipoParametro) args[0].valor();
     	
     	Vector<TipoParametro> v = new Vector<TipoParametro>();
     	v.add(param);
 
     	return v;
     }
     
 }
 
 
 
 class ParamsParametro0 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String desp = (String) args[0].valor();
     	Tipo tipo = (Tipo) args[1].valor();
     	String id = (String) args[2].valor();
     	String err = (String) args[3].valor();
     	
     	TipoParametro param;
     	if (err.equals("")) {
     		param = new TipoParametro("var", Integer.parseInt(desp), tipo, id);
     	}
     	else param = new TipoParametro("var", Integer.parseInt(desp), new TipoError("terrorRec"), id);
     	Vector<TipoParametro> v = new Vector<TipoParametro>();
     	v.add(param);
     	
     	return v;
     }
     
 }
 
 class ParamsParametro1 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String desp = (String) args[0].valor();
     	Tipo tipo = (Tipo) args[1].valor();
     	String id = (String) args[2].valor();
     	
     	TipoParametro param = new TipoParametro("pvar", Integer.parseInt(desp), tipo, id);
     	Vector<TipoParametro> v = new Vector<TipoParametro>();
     	v.add(param);
     	
     	return v;
     }
     
 }
 
 
 class CalculaDesp implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String desp = (String) args[0].valor();
     	Tipo tipo = (Tipo) args[1].valor();
     	if (tipo == null && tipo.getTipo().equals("terror")) return "-1";
     	
     	int despInt = Integer.parseInt(desp);
     	despInt = despInt + tipo.getTam();
     	
     	return Integer.toString(despInt);
     }
     
 }
 
 
 class ErrParametro0 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String err1 = (String) args[0].valor();
     	TablaSimbolos ts = (TablaSimbolos) args[1].valor();
     	String id = (String) args[2].valor();
     	
     	String err = err1;
     	if (ts.esPalabraReservada(id)) err += "No se pueden usar palabras reservadas como parametros de un subprograma\n";
     	
     	return err;
     }
     
 }
 
 class DevuelvePvar implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	return new String("pvar");
     }
     
 }
 
 
 class CodAsign implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String cod1 = (String) args[0].valor();
     	String cod2 = (String) args[1].valor();
     	String esDesign = (String) args[2].valor();
     	Tipo tExp = (Tipo) args[3].valor();
     	if (tExp == null && tExp.getTipo().equals("terror")) return "errorCod5";
     	
     	String cod = cod1 + cod2;
     	if (esDesign.equals("false")) cod = cod + "desapila_ind\n";
     	else cod = cod + "mueve(" + tExp.getTam() + ")\n";
     	return cod;
     }
     
 }
 
 
 
 class Suma12Designador implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String etq = (String) args[0].valor();
     	String esDesign = (String) args[1].valor();
     	
     	int etqInt = Integer.parseInt(etq);
     	etqInt++;
     	if (esDesign.equals("true")) etqInt++;
     	return Integer.toString(etqInt);
     }
     
 }
 
 
 class Suma1siDesignador implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String etq = (String) args[0].valor();
     	String esDesign = (String) args[1].valor();
     	
     	int etqInt = Integer.parseInt(etq);
     	if (esDesign.equals("true")) etqInt++;
     	return Integer.toString(etqInt);
     }
     
 }
 
 class ErrAsign implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String desErr = (String) args[0].valor();
     	String exprErr = (String) args[1].valor();
     	Tipo desTipo = (Tipo) args[2].valor();
     	Tipo exprTipo = (Tipo) args[3].valor();
     	String esCte = (String) args[4].valor();
     	CompruebaTipos cTipos = new CompruebaTipos();
     	
     	String err = desErr + exprErr;
     	if (!err.equals("")) return err;
     	if (esCte.equals("true")) return err + "No se puede asignar una expresion a una constante\n";
     	if (!cTipos.compatiblesAsign(desTipo, exprTipo)) return err + "Error de tipos\n";
     	return err;
     }
     
 }
 
 
 class DevuelveSwap1 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	return new String("swap1\n");
     }
     
 }
 
 class DevuelveSwap2 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	return new String("swap2\n");
     }
     
 }
 
 class CodLectura implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String cod1 = (String) args[0].valor();
     	
     	String cod = cod1 + "in\n" + "desapila_ind_compruebaTipos\n";
     	return cod;
     }
     
 }
 
 class ErrLectura implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String err1 = (String) args[0].valor();
     	Tipo tipo = (Tipo) args[1].valor();
     	
     	if (tipo.getTipo().equals("integer") || tipo.getTipo().equals("natural") || tipo.getTipo().equals("character") || 
     			tipo.getTipo().equals("boolean") || tipo.getTipo().equals("float")) return err1;
     	else return err1 + "Solo se pueden leer tipos basicos\n";
     }
     
 }
 
 class CodEscritura implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String cod1 = (String) args[0].valor();
     	String esDesignador = (String) args[1].valor();
     	
     	String cod = cod1;
     	if (esDesignador.equals("true")) cod = cod + "apila_ind\n";
     	cod = cod + "out\n";
     	
     	return cod;
     }
     
 }
 
 
 class ErrEscritura implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String err1 = (String) args[0].valor();
     	Tipo tipo = (Tipo) args[1].valor();
     	Tipo t;
     	
     	if (tipo.getTipo().equals("elemTupla")) t = ((TipoElemTupla)tipo).getTipoElem();
     	else t = tipo;
     	if (t.getTipo().equals("integer") || t.getTipo().equals("natural") || t.getTipo().equals("character") || 
     			t.getTipo().equals("boolean") || t.getTipo().equals("float")) return err1;
     	else return err1 + "Solo se pueden escribir tipos basicos\n";
     }
     
 }
 
 class CodIfs0 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String expCod = (String) args[0].valor();
     	String esDesignador = (String) args[1].valor();
     	String instrsV0Etq = (String) args[2].valor();
     	String instrsV0Cod = (String) args[3].valor();
     	String instrsV1Etq = (String) args[4].valor();
     	String instrsV1Cod = (String) args[5].valor();
     	
     	//Expr.cod || if (Expr.esDesignador) apila_ind
     	String cod = expCod;
     	if (esDesignador.equals("true")) cod = cod + "apila_ind\n";
     	int etq;
     	
     	//ir_f(InstrsV0.etq+1)
     	cod = cod + "ir_f(";
     	etq = Integer.parseInt(instrsV0Etq);
     	etq++;
     	cod = cod + etq + ")\n";
     	
     	//InstrsV0.cod
     	cod = cod + instrsV0Cod;
     	
     	//ir_a(InstrsV1.etq)
     	cod = cod + "ir_a(" + instrsV1Etq + ")\n";
     	
     	//InstrsV1.cod
     	cod = cod + instrsV1Cod;
     	
     	return cod;  	
     }
     
 }
 
 
 class CodIfs1 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String expCod = (String) args[0].valor();
     	String esDesignador = (String) args[1].valor();
     	String instrsVEtq = (String) args[2].valor();
     	String instrsVCod = (String) args[3].valor();
     	
     	//Expr.cod || if (Expr.esDesignador) apila_ind
     	String cod = expCod;
     	if (esDesignador.equals("true")) cod = cod + "apila_ind\n";
     	
     	//ir_f(InstrsV.etq)
     	cod = cod + "ir_f(" + instrsVEtq + ")\n";
     	
     	//InstrsV.cod
     	cod = cod + instrsVCod;
     		
     	return cod;  	
     }
     
 }
 
 
 class CodWhile implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String expCod = (String) args[0].valor();
     	String esDesignador = (String) args[1].valor();
     	String instrsVEtq = (String) args[2].valor();
     	String instrsVCod = (String) args[3].valor();
     	String whilesEtqh = (String) args[4].valor();
     	    	
     	//Expr.cod || if (Expr.esDesignador) apila_ind
     	String cod = expCod;
     	if (esDesignador.equals("true")) cod = cod + "apila_ind\n";
     	int etq;
     	
     	//ir_f(InstrsV.etq+1)
     	cod = cod + "ir_f(";
     	etq = Integer.parseInt(instrsVEtq);
     	etq++;
     	cod = cod + etq + ")\n";
     	
     	//InstrsV.cod
     	cod = cod + instrsVCod;
     	
     	//ir_a(Whiles.etqh)
     	cod = cod + "ir_a(" + whilesEtqh + ")\n";
     		
     	return cod;  	
     }
     
 }
 
 class Suma67 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String val = (String)args[0].valor();
     	String cod = (String)args[1].valor();
     	int valInt = Integer.parseInt(val);
     	if (cod.equals("")) valInt = valInt + 6;
     	else valInt = valInt + 7;
     	return Integer.toString(valInt);
     }
     
 }
 
 
 class CodCalls implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String etq = (String) args[0].valor();
     	TablaSimbolos ts = (TablaSimbolos) args[1].valor();
     	String id = (String) args[2].valor();
     	String cod1 = (String) args[3].valor();
     	CodigoProcs c = new CodigoProcs();
     	
     	String cod = cod1;
     	if (!cod.equals("")){
     		cod += "desapila\n";
     		cod += c.guardaRetorno(Integer.parseInt(etq) + 7);
     		cod += "ir_a(" + ts.dameDirComienzoProc(id) + ")\n";
     	} else { //Call vacio
     		cod += c.guardaRetorno(Integer.parseInt(etq) + 6);
     		cod += "ir_a(" + ts.dameDirComienzoProc(id) + ")\n";
     	}
     	
     	return cod;
     	
     }
     
 }
 
 
 class ErrCalls implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String err1 = (String) args[0].valor();
     	TablaSimbolos ts = (TablaSimbolos) args[1].valor();
     	String id = (String) args[2].valor();
     	String numParams = (String) args[3].valor();
 
     	String err = err1;
     	if (!ts.esSubprograma(id)) err += "El idenficiador " + id + " no corresponde al nombre de un subprograma\n";
     	else if (ts.dameNumParametros(id) != Integer.parseInt(numParams)) err += "El numero de parametros de llamada al subprograma " + id + " es incorrecto";
     	
     	return err;    	
     }
     
 }
 
 
 class CreaListaParamsVacia implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	Vector<String> l = new Vector<String>();
     	return l;
     }
     
 }
 
 class Suma4 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String val = (String)args[0].valor();
     	int valInt = Integer.parseInt(val);
     	valInt = valInt + 4;
     	return Integer.toString(valInt);
     }
     
 }
 
 class CodArgumentos0 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String cod0 = (String) args[0].valor();
     	String cod1 = (String) args[1].valor();
      	
     	String cod = cod0;
     	cod += "copia\n";
     	cod += cod1;
     	return cod;
     }
     
 }
 
 class CodArgumentos1 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String cod1 = (String) args[0].valor();
     	
     	String cod = "";
     	cod += "apila_dir(0)\n";
     	cod += "apila(3)\n";
     	cod += "suma\n";
     	cod += "copia\n";
     	cod += cod1;
     	
     	return cod;
     }
     
 }
 
 
 class CodArgumento implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {    	
     	TablaSimbolos ts = (TablaSimbolos) args[0].valor();
     	String idProc = (String) args[1].valor();
     	String idVar = (String) args[2].valor();
     	String cod1 = (String) args[3].valor();
     	String esDesignador = (String) args[4].valor();
     	
     	String cod = "";
     	cod += "apila(" + ts.dameDespProc(idProc,idVar) + ")\n";
     	cod += "suma\n";
     	cod += cod1;
     	if (ts.esModoPvar(idProc, idVar) || esDesignador.equals("false")) cod += "desapila_ind\n";
     	else cod += "mueve(" + ts.dameTamParametro(idProc, idVar) + ")\n";
     	
     	return cod;
     }
     
 }
 
 
 class ErrArgumento implements SemFun{
 
     @Override 
     public Object eval(Atributo... args) {
     	String err1 = (String) args[0].valor();
     	@SuppressWarnings("unchecked")
 		Vector<String> l = (Vector<String>) args[1].valor();
     	String idVar = (String) args[2].valor();
     	String idProc = (String) args[3].valor();
     	Tipo tipo = (Tipo) args[4].valor();
     	TablaSimbolos ts = (TablaSimbolos) args[5].valor();
     	String esDesign = (String) args[6].valor();
     	CompruebaTipos c = new CompruebaTipos();
     	
     	String err = err1;
     	if (l.contains(idVar)) err += "El parametro " + idVar + " del subprograma " + idProc + " esta repetido\n";
     	if (!ts.existeParam(idProc, idVar)) err += "El identificador " + idVar + " no es un parametro del subprograma " + idProc + "\n";
     	else if (!c.compatiblesAsign(ts.dameTipoParam(idProc, idVar), tipo)) err += "Error de tipos en la asignacion del parametro " + idVar + " del subprograma " + idProc + "\n";
     	else if (!c.compatiblesAsign(tipo, ts.dameTipoParam(idProc, idVar)) && ts.esModoPvar(idProc, idVar)) err += "Error de tipos en la asignacion del parametro " + idVar + " del subprograma " + idProc + "\n";
     	else if (ts.esModoPvar(idProc, idVar) && esDesign.equals("false")) err += "El parametro " + idVar + " del subprograma " + idProc + " debe ser un designador";
     	
     	return err;    	
     }
     
 }
 
 
 class AnyadeParametro implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	@SuppressWarnings("unchecked")
 		Vector<String> l = (Vector<String>) args[0].valor();
     	String id = (String) args[1].valor();
     	
     	l.add(id);
     	return l;
     }
     
 }
 
 class CodExpr implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String cod0 = (String) args[0].valor();
     	String esDesignador0 = (String) args[1].valor();
     	String cod1 = (String) args[2].valor();
     	String esDesignador1 = (String) args[3].valor();
     	String op = (String) args[4].valor();
     	TablaSimbolos ts = new TablaSimbolos();
     	
     	String cod = cod0;
     	if (esDesignador0.equals("true")) cod = cod + "apila_ind\n";
     	cod = cod + cod1;
     	if (esDesignador1.equals("true")) cod = cod + "apila_ind\n";
     	cod = cod + ts.calculaCodigoPilaOP(op) + "\n";
     	
     	return cod;  	
     }
     
 }
 
 
 class DefineTipoNivel0 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	Tipo t10 = (Tipo) args[0].valor();
     	Tipo t11 = (Tipo) args[1].valor();
     	String op = (String) args[2].valor();
 
     	CompruebaTipos cTipos = new CompruebaTipos();
     	
     	return cTipos.defineTipo(t10, t11, op);
     }
     
 }
 
 
 class ErrNivelX implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String err0 = (String) args[0].valor();
     	String err1 = (String) args[1].valor();
     	Tipo tipo = (Tipo) args[2].valor();
 
     	String err = err0 + err1;
     	if (tipo.getTipo().equals("terror")) err = err + "Error de tipos\n";
     	return err;
     }
     
 }
 
 
 class CodNivel10 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String cod0 = (String) args[0].valor();
     	String esDesignador0 = (String) args[1].valor();
     	String cod1 = (String) args[2].valor();
     	String esDesignador1 = (String) args[3].valor();
     	String op = (String) args[4].valor();
     	TablaSimbolos ts = new TablaSimbolos();
     	
     	String cod = cod0;
     	if (esDesignador0.equals("true")) cod = cod + "apila_ind\n";
     	cod = cod + cod1;
     	if (esDesignador1.equals("true")) cod = cod + "apila_ind\n";
     	cod = cod + ts.calculaCodigoPilaOP(op) + "\n";
     	
     	return cod;  	
     }
     
 }
 
 class DefineTipoNivel10 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	Tipo t1 = (Tipo) args[0].valor();
     	Tipo t2 = (Tipo) args[1].valor();
     	String op = (String) args[2].valor();
 
     	CompruebaTipos cTipos = new CompruebaTipos();
     	
     	return cTipos.defineTipo(t1, t2, op);
     }
     
 }
 
 
 class DevuelveFalse implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {    	
     	return new String("false");  	
     }
     
 }
 
 class CodNivel11 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String cod0 = (String) args[0].valor();
     	String esDesignador0 = (String) args[1].valor();
     	String irvh = (String) args[2].valor();
     	String cod1 = (String) args[3].valor();
     	String esDesignador1 = (String) args[4].valor();
     	
     	String cod = cod0;
     	if (esDesignador0.equals("true")) cod = cod + "apila_ind\n";
     	cod = cod + "copia\n";
     	cod = cod + "ir_v(" + irvh + ")\n" + "desapila\n" + cod1;
     	if (esDesignador1.equals("true")) cod = cod + "apila_ind\n";
     	
     	return cod;  	
     }
     
 }
 
 
 class Suma23Designador implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String etq = (String) args[0].valor();
     	String esDesign = (String) args[1].valor();
     	
     	int etqInt = Integer.parseInt(etq);
     	etqInt = etqInt + 2;
     	if (esDesign.equals("true")) etqInt++;
     	return Integer.toString(etqInt);
     }
     
 }
 
 class DefineTipoBoolean implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	Tipo tipo1 = (Tipo) args[0].valor();
     	Tipo tipo2 = (Tipo) args[1].valor();
     	if (tipo1 == null || tipo2 == null) return new TipoError("terrorRec");
     	String t1 = tipo1.getTipo();
     	String t2 = tipo2.getTipo();
     	if (t1.equals("boolean") && t2.equals("boolean")) return new TipoBasico("boolean");
     	else if (t1.equals("terror") || t2.equals("terror") || t1.equals("terrorRec") || t2.equals("terrorRec")) 
     		return new TipoError("terrorRec");
     	else return new TipoError("terror");
     }
     
 }
 
 
 class CodNivel20 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String cod0 = (String) args[0].valor();
     	String esDesignador0 = (String) args[1].valor();
     	String cod1 = (String) args[2].valor();
     	String esDesignador1 = (String) args[3].valor();
     	String op = (String) args[4].valor();
     	TablaSimbolos ts = new TablaSimbolos();
     	
     	String cod = cod0;
     	if (esDesignador0.equals("true")) cod = cod + "apila_ind\n";
     	cod = cod + cod1;
     	if (esDesignador1.equals("true")) cod = cod + "apila_ind\n";
     	cod = cod + ts.calculaCodigoPilaOP(op) + "\n";
     	
     	return cod;  	
     }
     
 }
 
 class DefineTipoNivel20 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	Tipo t2 = (Tipo) args[0].valor();
     	Tipo t3 = (Tipo) args[1].valor();
     	String op = (String) args[2].valor();
 
     	CompruebaTipos cTipos = new CompruebaTipos();
     	
     	return cTipos.defineTipo(t2, t3, op);
     }
     
 }
 
 
 class CodNivel21 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String cod0 = (String) args[0].valor();
     	String esDesignador0 = (String) args[1].valor();
     	String irfh = (String) args[2].valor();
     	String cod1 = (String) args[3].valor();
     	String esDesignador1 = (String) args[4].valor();
     	
     	String cod = cod0;
     	if (esDesignador0.equals("true")) cod = cod + "apila_ind\n";
     	cod = cod + "copia\n";
     	cod = cod + "ir_f(" + irfh + ")\n" + "desapila\n" + cod1;
     	if (esDesignador1.equals("true")) cod = cod + "apila_ind\n";
     	
     	return cod;  	
     }
     
 }
 
 
 class CodNivel30 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String cod0 = (String) args[0].valor();
     	String esDesignador0 = (String) args[1].valor();
     	String cod1 = (String) args[2].valor();
     	String esDesignador1 = (String) args[3].valor();
     	String op = (String) args[4].valor();
     	TablaSimbolos ts = new TablaSimbolos();
     	
     	String cod = cod0;
     	if (esDesignador0.equals("true")) cod = cod + "apila_ind\n";
     	cod = cod + cod1;
     	if (esDesignador1.equals("true")) cod = cod + "apila_ind\n";
     	cod = cod + ts.calculaCodigoPilaOP(op) + "\n";
     	
     	return cod;  	
     }
     
 }
 
 
 class DefineTipoNivel30 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	Tipo t4 = (Tipo) args[0].valor();
     	Tipo t3 = (Tipo) args[1].valor();
     	String op = (String) args[2].valor();
 
     	CompruebaTipos cTipos = new CompruebaTipos();
     	
     	return cTipos.defineTipo(t4, t3, op);
     }
     
 }
 
 
 class CodNivel40 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String cod0 = (String) args[0].valor();
     	String esDesignador0 = (String) args[1].valor();
     	String op = (String) args[2].valor();
     	TablaSimbolos ts = new TablaSimbolos();
     	
     	String cod = cod0;
     	if (esDesignador0.equals("true")) cod = cod + "apila_ind\n";
 
     	cod = cod + ts.calculaCodigoPilaOP(op) + "\n";
     	
     	return cod;  	
     }
     
 }
 
 
 class DefineTipoNivel4 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	Tipo t = (Tipo) args[0].valor();
     	String op = (String) args[1].valor();
 
     	CompruebaTipos cTipos = new CompruebaTipos();
     	
     	return cTipos.defineTipoNivel4(t, op);
     }
     
 }
 
 class ErrNivel4 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String err0 = (String) args[0].valor();
     	Tipo tipo = (Tipo) args[1].valor();
 
     	String err = err0;
     	if (tipo.getTipo().equals("terror")) err = err + "Error de tipos\n";
     	return err;
     }
     
 }
 
 class DevuelveMenor implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	return new String("<");
     }
     
 }
 
 class DevuelveMayor implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	return new String(">");
     }
     
 }
 
 class DevuelveMenorIgual implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	return new String("<=");
     }
     
 }
 
 class DevuelveMayorIgual implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	return new String(">=");
     }
     
 }
 
 class DevuelveIgualIgual implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	return new String("==");
     }
     
 }
 
 class DevuelveDistinto implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	return new String("!=");
     }
     
 }
 
 class DevuelveMas implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	return new String("+");
     }
     
 }
 
 class DevuelveMenos implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	return new String("-");
     }
     
 }
 
 class DevuelveTrue implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	return new String("true");
     }
     
 }
 
 class DevuelvePor implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	return new String("*");
     }
     
 }
 
 class DevuelveDiv implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	return new String("/");
     }
     
 }
 
 class DevuelveMod implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	return new String("%");
     }
     
 }
 
 class DevuelvedespIzq implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	return new String("<<");
     }
     
 }
 
 class DevuelvedespDer implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	return new String(">>");
     }
     
 }
 
 class DevuelveCastFloat implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	return new String("(float)");
     }
     
 }
 
 class DevuelveCastInt implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	return new String("(int)");
     }
     
 }
 
 class DevuelveCastChar implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	return new String("(char)");
     }
     
 }
 
 class DevuelveCastNat implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	return new String("(nat)");
     }
     
 }
 
 class DevuelveNot implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	return new String("not");
     }
     
 }
 
 class CompruebaEsConstante implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String err = (String) args[0].valor();
     	TablaSimbolos ts = (TablaSimbolos) args[1].valor();
     	String id = (String) args[2].valor();
     	if (!err.equals("")) return "false";
         else if (ts.esConstante(id)) return "true";
         else return "false";
     }
     
 }
 
 class CompruebaDesignador0 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	TablaSimbolos ts = (TablaSimbolos) args[0].valor();
     	String id = (String) args[1].valor();
     	String err = "";
     	if (!ts.existeId(id)) err = "El identificador " + id + " no esta declarado\n";
     	else if (ts.esPalabraReservada(id)) err = "El idenficador " + id + " es una palabra reservada\n";
     	else if (ts.esTipo(id)) err = "El identificador de el designador " + id + " no puede ser un tipo\n";
     	return err;
     }
     
 }
 
 class DameTipo implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	TablaSimbolos ts = (TablaSimbolos) args[0].valor();
     	String id = (String) args[1].valor();
     	return ts.getTipo(id);
     }
     
 }
 
 class CodDesignador0 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	TablaSimbolos ts = (TablaSimbolos) args[0].valor();
     	String id = (String) args[1].valor();
     	
     	return ts.calculaCodigoAccesoVar(id);
     }
     
 }
 
 class sumaLongAccesoVar implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	TablaSimbolos ts = (TablaSimbolos) args[0].valor();
     	String val = (String)args[1].valor();
     	int valInt = Integer.parseInt(val);
     	String id = (String) args[2].valor();
     	   	   	
 		if (ts.nivel2Activado() && ts.existeIdSegundoNivel(id)){
 			valInt += 3;
 			if (ts.getClase(id).equals("pvar")) valInt += 1;
 		}
 		else if (ts.existeIdPrimerNivel(id)){
 			valInt += 1;
 		}
 		
     	return Integer.toString(valInt);
     }
     
 }
 
 
 class DameTipoBaseArray implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	Tipo t = (Tipo) args[1].valor();
         if (t instanceof TipoError) return new TipoError("terror");
         
         Tipo tipo = t;
         TipoArray t2;
     	if (tipo.getTipo().equals("elemTupla")) tipo = ((TipoElemTupla)tipo).getTipoElem(); 
         try {
         	t2 = (TipoArray) tipo;
         } catch (Exception e) {
         	return new TipoError("terror");
         }
     	return t2.getTipoBase();
     }
     
 }
 
 
 class ErrDesignador1 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String err1 = (String) args[0].valor();
     	Tipo des1Tipo = (Tipo) args[1].valor();
     	Tipo exprTipo = (Tipo) args[2].valor();
     	
     	Tipo t = des1Tipo;
     	if (t.getTipo().equals("elemTupla")) t = ((TipoElemTupla)t).getTipoElem(); 
     	String err = err1;
     	if (!t.getTipo().equals("array")) err = err + "El tipo del designador no es tipo array\n";
     	if (!exprTipo.getTipo().equals("natural")) err = err + "El tipo de la expresion debe ser natural\n";
     	return err;
     }
     
 }
 
 class CodDesignador1 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String codDesig = (String) args[0].valor();
         String codExpr = (String) args[1].valor();
         String esDesign = (String) args[2].valor();
         
         Tipo t = (Tipo) args[3].valor();
         if (t.getTipo().equals("elemTupla")) t = ((TipoElemTupla)t).getTipoElem();
         TipoArray tipo;
         if (t instanceof TipoError) return "errorCod";
         try {
         	tipo = (TipoArray) t;
         } catch (Exception e) {
         	return "codError1\n";
         }
     	
     	String cod = codDesig + codExpr;
     	if (esDesign.equals("true")) cod = cod + "apila_ind\n";
     	cod = cod + "apila(" + tipo.getTipoBase().getTam() + ")\n";
     	cod = cod + "multiplica\n";
     	cod = cod + "suma\n";
     	
     	return cod;
     }
     
 }
 
 
 class Suma34Designador implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String etq = (String) args[0].valor();
     	String esDesign = (String) args[1].valor();
     	
     	int etqInt = Integer.parseInt(etq);
     	etqInt = etqInt + 3;
     	if (esDesign.equals("true")) etqInt++;
     	return Integer.toString(etqInt);
     }
     
 }
 
 class DameTipoElemTupla implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	
     	String lex = (String) args[1].valor();
         Tipo t = (Tipo) args[2].valor();
         if (t instanceof TipoError) return new TipoError("terror");
         
         if (t.getTipo().equals("elemTupla")) t = ((TipoElemTupla)t).getTipoElem();
         TipoTupla tipo;
         try {
         	tipo = (TipoTupla)t;
         } catch (Exception e) {
         	return new TipoError("terror");
         }
         
     	int lexInt = Integer.parseInt(lex);
     	if (lexInt >= tipo.getNElem()) return new TipoError("terror");
     	else return tipo.getTipoElemTupla(lexInt);
     }
     
 }
 
 
 class ErrDesignador2 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String err1 = (String) args[0].valor();
     	Tipo tipo = (Tipo) args[1].valor(); 
     	int lexInt = Integer.parseInt((String) args[2].valor());
     	
     	String err = err1;
     	if (tipo.getTipo().equals("elemTupla")) tipo = ((TipoElemTupla)tipo).getTipoElem();
     	if (!tipo.getTipo().equals("tup")) err = err + "El tipo del designador no es tipo tupla\n";
     	else if (lexInt >= ((TipoTupla)tipo).getNElem()) return err = err + "El indice de la tupla se sale de rango\n";
     	return err;
     }
     
 }
 
 class CodDesignador2 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	
     	String codDesig = (String) args[0].valor();
         Tipo t = (Tipo) args[1].valor();
         if (t instanceof TipoError) return "codError2";
         
         if (t.getTipo().equals("elemTupla")) t = ((TipoElemTupla)t).getTipoElem();
         TipoTupla tipo;
         try {
         	tipo = (TipoTupla)t;
         } catch (Exception e){
         	return "codError3";
         }
         String lex = (String) args[2].valor();
         int lexInt = Integer.parseInt(lex);    
     	
     	if (lexInt >= tipo.getNElem()) return "codError4";
     	String cod = codDesig;
     	cod = cod + "apila(" + tipo.getTipoElemTupla(lexInt).getDespl() + ")\n";
     	cod = cod + "suma\n";
     	
     	
     	return cod;
     }
     
 }
 
 
 class Apila implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String cod = "apila("+((String)args[0].valor())+")\n";
     	return cod;
     }
     
 }
 
 
 class Suma1 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String val = (String)args[0].valor();
     	int valInt = Integer.parseInt(val);
     	valInt = valInt + 1;
     	return Integer.toString(valInt);
     }
     
 }
 
 class Suma2 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String val = (String)args[0].valor();
     	int valInt = Integer.parseInt(val);
     	valInt = valInt + 2;
     	return Integer.toString(valInt);
     }
     
 }
 
 class Suma3 implements SemFun{
 
     @Override
     public Object eval(Atributo... args) {
     	String val = (String)args[0].valor();
     	int valInt = Integer.parseInt(val);
     	valInt = valInt + 3;
     	return Integer.toString(valInt);
     }
     
 }
 
 
 
 public class EAtribucion extends Atribucion{
 	private static SemFun asignacion = new Asignacion();
 	private static SemFun asignacionDependencias1 = new AsignacionDependencias1();
     private static SemFun devuelveConst= new DevuelveConst();
     private static SemFun devuelveTipo= new DevuelveTipo();
     private static SemFun creaTipoBasicoCharacter= new creaTipoBasicoCharacter();
     private static SemFun creaTipoBasicoInt= new creaTipoBasicoInt();
     private static SemFun creaTipoBasicoNat= new creaTipoBasicoNat();
     private static SemFun creaTipoBasicoFloat= new creaTipoBasicoFloat();
     private static SemFun creaTipoBasicoBoolean= new creaTipoBasicoBoolean();
     
     private static SemFun creaTipoNoBasicoIden= new CreaTipoNoBasicoIden();
     private static SemFun creaTuplaVacia = new CreaTuplaVacia ();
     private static SemFun tsDameValorInt= new TsDameValorInt();
     private static SemFun devuelveNull = new DevuelveNull();
     private static SemFun devuelve0 = new Devuelve0();
     private static SemFun devuelve6 = new Devuelve6();
     private static SemFun devuelveVar = new DevuelveVar();
     private static SemFun concatena = new Concatena();
     private static SemFun devuelveVacio = new DevuelveVacio();
     private static SemFun codInicio = new CodInicio();
     private static SemFun codProg = new CodProg();
     private static SemFun compruebaYanyadeIdConst = new CompruebaYanyadeIdConst();
     private static SemFun codConstantes0 = new CodConstantes0();
     private static SemFun errConstantes0 = new ErrConstantes0();
     private static SemFun compruebaPRCreaTsYAnyadeId = new CompruebaPRCreaTsYAnyadeId();
     private static SemFun codConstantes1 = new CodConstantes1();
     private static SemFun errConstantes1 = new ErrConstantes1();
     private static SemFun creaTipoConstante = new CreaTipoConstante();
     private static SemFun errConstante = new ErrConstante();
     private static SemFun compruebaTsYasigna = new CompruebaTsYasigna();
     private static SemFun compruebaYanyadeIdTipo = new CompruebaYanyadeIdTipo(); 
     private static SemFun errTipos0 = new ErrTipos0();
     private static SemFun compruebaCreaTsYanyadeTipo = new CompruebaCreaTsYanyadeTipo();
     private static SemFun errTipos1 = new ErrTipos1();
     private static SemFun errTipoNoBasico0 = new ErrTipoNoBasico0();
     private static SemFun compruebaTuplasAnyadeCampo = new CompruebaTuplasAnyadeCampo();
     private static SemFun compruebaYCreaTuplaConCampo = new CompruebaYCreaTuplaConCampo();
     private static SemFun compruebaYcreaArray = new CompruebaYcreaArray();
     private static SemFun errTipoNoBasico2 = new ErrTipoNoBasico2();
     private static SemFun errTipoIndice0 = new ErrTipoIndice0();
     private static SemFun compruebaYanyadeIdVariables0 = new CompruebaYanyadeIdVariables0();
     //private static SemFun codVariables0 = new CodVariables0();
     private static SemFun errVariables0 = new ErrVariables0();
     private static SemFun CompruebaCreaTsYAnyadeIdVariable = new CompruebaCreaTsYAnyadeIdVariable();
     //private static SemFun reservaVariable = new ReservaVariable();
     
     
     private static SemFun activaNivelLocal = new ActivaNivelLocal ();
     private static SemFun desactivaNivelLocal = new DesactivaNivelLocal();
     private static SemFun compruebaYanyadeIdSubprogramas0 = new CompruebaYanyadeIdSubprogramas0 ();
     private static SemFun errSubprogramas0 = new ErrSubprogramas0 ();
     private static SemFun compruebaYanyadeIdSubprogramas1 = new CompruebaYanyadeIdSubprogramas1 ();
     //private static SemFun errSubprogramas1 = new ErrSubprogramas1 ();
     private static SemFun devuelveProc = new DevuelveProc ();
     private static SemFun construyeTipoSubprograma = new ConstruyeTipoSubprograma ();
     private static SemFun compruebaYanyadeIdSubprograma = new CompruebaYanyadeIdSubprograma ();
     private static SemFun codSubprograma = new CodSubprograma ();
     private static SemFun suma15 = new Suma15 ();
     private static SemFun suma16 = new Suma16 ();
     private static SemFun errSubprograma = new ErrSubprograma ();
     private static SemFun devuelveListaVacia = new DevuelveListaVacia ();
     private static SemFun compruebaYanyadeIdParametros0 = new CompruebaYanyadeIdParametros0 ();
     private static SemFun anyadeParam = new AnyadeParam ();
     private static SemFun errParametros0 = new ErrParametros0();
     private static SemFun compruebaYanyadeIdParametros1 = new CompruebaYanyadeIdParametros1 ();
     //private static SemFun creaParamsYanyade = new CreaParamsYanyade ();
     private static SemFun paramsParametro0 = new ParamsParametro0 ();
     private static SemFun paramsParametro1 = new ParamsParametro1 ();
     private static SemFun calculaDesp = new CalculaDesp ();
     private static SemFun errParametro0 = new ErrParametro0 ();
     private static SemFun devuelvePvar = new DevuelvePvar ();
     
     private static SemFun codAsign = new CodAsign();
     private static SemFun suma12Designador = new Suma12Designador();
     private static SemFun suma1siDesignador = new Suma1siDesignador();
     private static SemFun errAsign = new ErrAsign();
     private static SemFun devuelveSwap1 = new DevuelveSwap1();
     private static SemFun devuelveSwap2 = new DevuelveSwap2();
     private static SemFun codLectura = new CodLectura();
     private static SemFun errLectura = new ErrLectura();
     private static SemFun codEscritura = new CodEscritura();
     private static SemFun errEscritura = new ErrEscritura();
     private static SemFun codIfs0 = new CodIfs0();
     private static SemFun codIfs1 = new CodIfs1();
     private static SemFun codWhile = new CodWhile();
     private static SemFun suma67 = new Suma67();
     private static SemFun codCalls = new CodCalls();
     private static SemFun errCalls = new ErrCalls();
     private static SemFun creaListaParamsVacia = new CreaListaParamsVacia();
     private static SemFun suma4 = new Suma4();
     private static SemFun codArgumentos0 = new CodArgumentos0();
     private static SemFun codArgumentos1 = new CodArgumentos1();
     private static SemFun codArgumento = new CodArgumento();
     private static SemFun errArgumento  = new ErrArgumento();
     private static SemFun anyadeParametro = new AnyadeParametro();
     private static SemFun codExpr = new CodExpr();
     private static SemFun defineTipoNivel0 = new DefineTipoNivel0();
     private static SemFun errNivelX = new ErrNivelX();
     private static SemFun codNivel10 = new CodNivel10();
     private static SemFun defineTipoNivel10 = new DefineTipoNivel10 ();
     private static SemFun codNivel11 = new CodNivel11();
     private static SemFun suma23Designador = new Suma23Designador();
     private static SemFun defineTipoBoolean = new DefineTipoBoolean();
     private static SemFun codNivel20 = new CodNivel20();
     private static SemFun defineTipoNivel20 = new DefineTipoNivel20 ();
     private static SemFun codNivel21 = new CodNivel21();
     private static SemFun codNivel30 = new CodNivel30();
     private static SemFun defineTipoNivel30 = new DefineTipoNivel30 ();
     private static SemFun codNivel40 = new CodNivel40();
     private static SemFun defineTipoNivel4 = new DefineTipoNivel4();
     private static SemFun errNivel4 = new ErrNivel4();
     
   	private static SemFun devuelveMenor = new DevuelveMenor ();
   	private static SemFun devuelveMayor = new DevuelveMayor ();
   	private static SemFun devuelveMenorIgual = new DevuelveMenorIgual ();
   	private static SemFun devuelveMayorIgual = new DevuelveMayorIgual ();
   	private static SemFun devuelveIgualIgual = new DevuelveIgualIgual ();
   	private static SemFun devuelveDistinto = new DevuelveDistinto ();
   	private static SemFun devuelveMas = new DevuelveMas ();
   	private static SemFun devuelveMenos = new DevuelveMenos ();
   	private static SemFun devuelveTrue = new DevuelveTrue();
     private static SemFun devuelveFalse = new DevuelveFalse();
   	private static SemFun devuelvePor = new DevuelvePor ();
   	private static SemFun devuelveDiv = new DevuelveDiv ();
   	private static SemFun devuelveMod = new DevuelveMod ();
   	private static SemFun devuelvedespIzq = new DevuelvedespIzq ();
   	private static SemFun devuelvedespDer = new DevuelvedespDer ();
   	private static SemFun devuelveCastFloat = new DevuelveCastFloat ();
   	private static SemFun devuelveCastInt = new DevuelveCastInt ();
   	private static SemFun devuelveCastChar = new DevuelveCastChar ();
   	private static SemFun devuelveCastNat = new DevuelveCastNat ();
   	private static SemFun devuelveNot = new DevuelveNot ();
 
   	private static SemFun compruebaDesignador0 = new CompruebaDesignador0 ();
   	private static SemFun compruebaEsConstante = new CompruebaEsConstante ();
   	private static SemFun dameTipo = new DameTipo ();
   	private static SemFun codDesignador0 = new CodDesignador0();
   	private static SemFun sumaLongAccesoVar = new sumaLongAccesoVar();
     
   	private static SemFun dameTipoBaseArray = new DameTipoBaseArray();
   	private static SemFun errDesignador1 = new ErrDesignador1();
   	private static SemFun codDesignador1 = new CodDesignador1();
   	private static SemFun suma34Designador = new Suma34Designador();
     
   	private static SemFun dameTipoElemTupla = new DameTipoElemTupla();
   	private static SemFun errDesignador2 = new ErrDesignador2();
   	private static SemFun codDesignador2 = new CodDesignador2();
   	
     private static SemFun apila = new Apila();
     private static SemFun suma1 = new Suma1();
     private static SemFun suma2 = new Suma2();
     @SuppressWarnings("unused")
 	private static SemFun suma3 = new Suma3();
     
     
     public TAtributos rInicio(TAtributos prog) {
         regla("Inicio -> program : palabra { Prog }");
         TAtributos inicio = atributosPara("inicio", "cod", "err", "ts");
        
         dependencias(inicio.a("cod"), prog.a("ts"), prog.a("cod"));
         dependencias(inicio.a("err"), prog.a("err"));
         dependencias(inicio.a("ts"), prog.a("ts"));
 
         calculo(prog.a("tsh"), devuelveNull);
         calculo(prog.a("etqh"),devuelve6);
         calculo(inicio.a("cod"), codInicio);
         calculo(inicio.a("err"), asignacion);
         calculo(inicio.a("ts"), asignacion);
         
         return inicio;
      } 
      
  public TAtributos rProg(TAtributos decConstantes0,TAtributos decTipos0, TAtributos decVariables0, TAtributos decSubprogramas0, TAtributos decInstruc0) {
          regla("Prog ->  DecConstantes DecTipos DecVariables DecSubprogramas DecInstruc");
 
          TAtributos prog = atributosPara("prog", "ts","tsh", "etqh", "etq", "cod", "err"); 
        
          dependencias(decConstantes0.a("tsh"), prog.a("tsh"));
          dependencias(decTipos0.a("tsh"), decConstantes0.a("ts"));
          dependencias(decVariables0.a("tsh"), decTipos0.a("ts"));
          dependencias(decSubprogramas0.a("tsh"), decVariables0.a("ts"));
          dependencias(decInstruc0.a("tsh"), decSubprogramas0.a("ts"));
          dependencias(prog.a("ts"), decSubprogramas0.a("ts"));
          
         
          dependencias(decConstantes0.a("etqh"), prog.a("etqh"));
          dependencias(decSubprogramas0.a("etqh"), decConstantes0.a("etq")); 
          dependencias(decInstruc0.a("etqh"), decSubprogramas0.a("etq")); 
          dependencias(prog.a("etq"), decInstruc0.a("etq"));
           
          dependencias(prog.a("cod"), decConstantes0.a("cod"), decSubprogramas0.a("etq"), decSubprogramas0.a("cod"), decInstruc0.a("cod")); 
          dependencias(prog.a("err"), decConstantes0.a("err"), decTipos0.a("err"), decVariables0.a("err"), decSubprogramas0.a("err"), decInstruc0.a("err")); 
          
          
          
          calculo(decConstantes0.a("tsh"), asignacion);
          calculo(decTipos0.a("tsh"), asignacion);
          calculo(decVariables0.a("tsh"), asignacion);
          calculo(decSubprogramas0.a("tsh"), asignacion);
          calculo(decInstruc0.a("tsh"), asignacion);
          calculo(prog.a("ts"), asignacion);
     
          calculo(decConstantes0.a("etqh"), asignacion);
          calculo(decSubprogramas0.a("etqh"), suma1);
          calculo(decInstruc0.a("etqh"), asignacion);
          calculo(prog.a("etq"), asignacion);
         
          calculo(prog.a("cod"), codProg);
          calculo(prog.a("err"), concatena);
          
     
          return prog;
     }
 
  public TAtributos rDecConstantes0(TAtributos constantes) {
          regla("DecConstantes -> consts { Constantes }");
 
          TAtributos decConstantes = atributosPara("decConstantes", "ts", "etqh", "etq", "cod", "tsh", "err");
          
          dependencias(constantes.a("tsh"), decConstantes.a("tsh"));
          dependencias(decConstantes.a("ts"), constantes.a("ts"));
          dependencias(constantes.a("etqh"), decConstantes.a("etqh"));
          dependencias(decConstantes.a("etq"), constantes.a("etq"));
          dependencias(decConstantes.a("cod"), constantes.a("cod"));
          dependencias(decConstantes.a("err"), constantes.a("err"));
 
          calculo(constantes.a("tsh"), asignacion);
          calculo(decConstantes.a("ts"), asignacion);
          calculo(constantes.a("etqh"), asignacion);
          calculo(decConstantes.a("etq"), asignacion);
          calculo(decConstantes.a("cod"), asignacion);
          calculo(decConstantes.a("err"), asignacion);
          
          return decConstantes;
  }
  	
  public TAtributos rDecConstantes1() {
          regla("DecConstantes :: vacio");
          
          TAtributos decConstantes = atributosPara("decConstantes", "ts", "etq", "etqh", "cod", "tsh", "err");
 
          dependencias(decConstantes.a("etq"), decConstantes.a("etqh"));
          dependencias(decConstantes.a("ts"), decConstantes.a("tsh"));
          
          
          calculo(decConstantes.a("ts"), asignacion);
          calculo(decConstantes.a("etq"), asignacion);
          calculo(decConstantes.a("cod"), devuelveVacio);
          calculo(decConstantes.a("err"), devuelveVacio);
               
          return decConstantes;
  }
  		
  public TAtributos rConstantes0(TAtributos constantes1, TAtributos constante) {
          regla("Constantes -> Constantes Constante");
      
          TAtributos constantes0 = atributosPara("constantes0", "ts","tsh", "etq", "etqh", "cod", "err");
 
          dependencias(constantes1.a("tsh"), constantes0.a("tsh"));
          dependencias(constantes0.a("err"), constantes1.a("err"), constante.a("err"), constantes1.a("ts"), constante.a("lex"));
          dependencias(constantes0.a("ts"), constantes1.a("ts"), constante.a("lex"), constantes1.a("err"), constante.a("err"), constante.a("clase"), constante.a("tipo"), constantes0.a("err"));
 
          dependencias(constantes1.a("etqh"), constantes0.a("etqh"));
          dependencias(constante.a("etqh"), constantes1.a("etq"));
          dependencias(constantes0.a("etq"), constante.a("etq"));
 
 
          dependencias(constantes0.a("cod"), constantes1.a("cod"), constante.a("cod"), constante.a("lex"), constantes0.a("ts"));
          
          calculo(constantes1.a("tsh"), asignacion);
          calculo(constantes0.a("err"), errConstantes0);
          calculo(constantes0.a("ts"), compruebaYanyadeIdConst);	
 
          calculo(constantes1.a("etqh"), asignacion);	
          calculo(constante.a("etqh"), asignacion);	
          calculo(constantes0.a("etq"), suma1);
 
          calculo(constantes0.a("cod"), codConstantes0);
 
 
                
          return constantes0;
  } 
     	
 
 
  public TAtributos rConstantes1(TAtributos constante) {
          regla("Constantes -> Constante");
          TAtributos constantes = atributosPara("constantes", "ts", "etq", "etqh", "cod", "err", "tsh");
 
          dependencias(constantes.a("ts"), constante.a("lex"), constante.a("err"), constante.a("clase"), constante.a("tipo"));
          dependencias(constante.a("etqh"), constantes.a("etqh"));
          dependencias(constantes.a("cod"), constante.a("cod"), constante.a("lex"), constantes.a("ts"));
          dependencias(constantes.a("etq"), constante.a("etq"));
          dependencias(constantes.a("err"), constante.a("err"), constantes.a("ts"), constante.a("lex"));
            
          calculo(constantes.a("ts"), compruebaPRCreaTsYAnyadeId);
          calculo(constante.a("etqh"), asignacion);
          calculo(constantes.a("cod"), codConstantes1);
          calculo(constantes.a("etq"), suma1);
          calculo(constantes.a("err"), errConstantes1);
        
          return constantes;
  }
  		
  public TAtributos rConstante(TAtributos tipoBasico, String iden, TAtributos valorTipoBasico) {
          
          regla("Constante ->  const TipoBasico identificador = ValorTipoBasico ;");
  
          TAtributos constante = atributosPara("constante", "lex","clase", "tipo", "etq", "etqh", "cod", "err");
 
          Atributo astring= atributoLexicoPara("identificador","lex", iden);
 
          dependencias(constante.a("lex"), astring);
          dependencias(constante.a("tipo"), tipoBasico.a("tipo"), valorTipoBasico.a("valor"));
          dependencias(constante.a("cod"), valorTipoBasico.a("cod"));
          dependencias(valorTipoBasico.a("etqh"), constante.a("etqh"));
          dependencias(constante.a("etq"), valorTipoBasico.a("etq"));
          dependencias(constante.a("err"), tipoBasico.a("tipo"), valorTipoBasico.a("valor"), constante.a("lex"));
        
          calculo(constante.a("lex"), asignacion);
          calculo(constante.a("clase"), devuelveConst);		
          calculo(constante.a("tipo"), creaTipoConstante);
 
          calculo(constante.a("cod"), asignacion);
          calculo(valorTipoBasico.a("etqh"), asignacion);
          calculo(constante.a("etq"), asignacion);
          calculo(constante.a("err"), errConstante);
          
          return constante;
  }
  	
  public TAtributos rDecTipos0(TAtributos tipos) {
          regla("DecTipos:: tipos { Tipos }");
          
          TAtributos decTipos = atributosPara("decTipos", "ts", "tsh", "err");
 
          dependencias(tipos.a("tsh"), decTipos.a("tsh"));       
          dependencias(decTipos.a("ts"), tipos.a("ts"));  
          dependencias(decTipos.a("err"), tipos.a("err"));  
 
          calculo(tipos.a("tsh"), compruebaTsYasigna);       
          calculo(decTipos.a("ts"), asignacion);  
          calculo(decTipos.a("err"), asignacion);  
          
          return decTipos;
  } 
     
  public TAtributos rDecTipos1() {
          regla("DecTipos -> vacio");
          
          TAtributos decTipos = atributosPara("decTipos", "ts", "tsh", "err");
 
          dependencias(decTipos.a("ts"), decTipos.a("tsh"));       
          
          calculo(decTipos.a("ts"), asignacion);
          calculo(decTipos.a("err"), devuelveVacio);
          
         return decTipos;
  }
 
  public TAtributos rTipos0(TAtributos tipos1, TAtributos tipo) {
          regla("Tipos -> Tipos Tipo");
          
          TAtributos tipos0 = atributosPara("tipos0", "ts", "tsh", "err");
 
          dependencias(tipos1.a("tsh"), tipos0.a("tsh"));       
          dependencias(tipo.a("tsh"), tipos1.a("ts"));   
          dependencias(tipos0.a("ts"), tipos1.a("ts"), tipo.a("lex"),tipos1.a("err"), tipo.a("err"), tipo.a("clase"), tipo.a("tipo"), tipos0.a("err"));
          dependencias(tipos0.a("err"), tipos1.a("err"), tipo.a("err"), tipos1.a("ts"), tipo.a("lex")); 
 
          calculo(tipos1.a("tsh"), asignacion);       
          calculo(tipo.a("tsh"), asignacion);   
          calculo(tipos0.a("ts"), compruebaYanyadeIdTipo);
          calculo(tipos0.a("err"), errTipos0);
          
  		      
          return tipos0;
  }   
 
 
 
  public TAtributos rTipos1(TAtributos tipo) {
          regla("Tipos -> Tipo");
          
   
          TAtributos tipos= atributosPara("tipos", "ts","tsh", "err");
                 
          dependencias(tipo.a("tsh"), tipos.a("tsh"));
          dependencias(tipos.a("ts"), tipos.a("tsh"), tipo.a("lex"), tipo.a("err"), tipo.a("clase"), tipo.a("tipo"), tipos.a("err"));
          dependencias(tipos.a("err"), tipo.a("err"), tipo.a("lex"), tipos.a("tsh"));        
          calculo(tipo.a("tsh"), asignacion);
          calculo(tipos.a("ts"), compruebaCreaTsYanyadeTipo);  
          calculo(tipos.a("err"), errTipos1); 
 
          
          return tipos;
     }
 
  public TAtributos rTipo0(TAtributos declaracionTipo, String iden) {
          regla("Tipo:: tipo DeclaracionTipo identificador ;");
          
          TAtributos tipo = atributosPara("tipo", "tsh", "lex", "clase", "tipo", "err");
 
          Atributo astring= atributoLexicoPara("identificador","lex", iden);
  	
          dependencias(declaracionTipo.a("tsh"), tipo.a("tsh"));
          dependencias(tipo.a("lex"), astring);
          dependencias(tipo.a("tipo"), declaracionTipo.a("tipo"));
          dependencias(tipo.a("err"), declaracionTipo.a("err"));
 
          calculo(declaracionTipo.a("tsh"), asignacion);
          calculo(tipo.a("lex"), asignacion);
          calculo(tipo.a("clase"), devuelveTipo);
          calculo(tipo.a("tipo"), asignacion);
          calculo(tipo.a("err"), asignacion);
 
          
          return tipo;
  }
  	
 
 
  public TAtributos rDeclaracionTipo0(TAtributos tipoBasico) {
          regla("DeclaracionTipo :: TipoBasico");
          
          TAtributos declaracionTipo = atributosPara("declaracionTipo", "tipo", "err", "tsh");
          dependencias(declaracionTipo.a("tipo"), tipoBasico.a("tipo"));
 
          calculo(declaracionTipo.a("tipo"), asignacion);
          calculo(declaracionTipo.a("err"), devuelveVacio);
               
          return declaracionTipo;
  }
  		
 
 
  public TAtributos rDeclaracionTipo1(TAtributos tipoNoBasico) {
          regla("DeclaracionTipo :: TipoNoBasico");
          
          TAtributos declaracionTipo = atributosPara("declaracionTipo", "tsh", "tipo", "err");
     
 
          dependencias(tipoNoBasico.a("tsh"), declaracionTipo.a("tsh"));
          dependencias(declaracionTipo.a("tipo"), tipoNoBasico.a("tipo"));
          dependencias(declaracionTipo.a("err"), tipoNoBasico.a("err"));
          
          calculo(tipoNoBasico.a("tsh"), asignacion);
          calculo(declaracionTipo.a("tipo"), asignacion);	
          calculo(declaracionTipo.a("err"), asignacion);	
                
          return declaracionTipo;
  } 
     	
 
  public TAtributos rTipoBasico0(Object val) {
          regla("TipoBasico :: natural"); 
 
          TAtributos tipoBasico= atributosPara("tipoBasico", "tipo");
     
          calculo(tipoBasico.a("tipo"), creaTipoBasicoNat);
          
          return tipoBasico;
  }
  		
  public TAtributos rTipoBasico1(Object val) {
          regla("TipoBasico :: integer");
 
          TAtributos tipoBasico= atributosPara("tipoBasico", "tipo");
   
          calculo(tipoBasico.a("tipo"), creaTipoBasicoInt);
          
          return tipoBasico;
  }
 
 
  public TAtributos rTipoBasico2(Object val) {
          regla("TipoBasico :: float");
        
          TAtributos tipoBasico= atributosPara("tipoBasico", "tipo");
 
          calculo(tipoBasico.a("tipo"), creaTipoBasicoFloat);
          
          return tipoBasico;
  }
 
 
  public TAtributos rTipoBasico3(Object val) {
          regla("TipoBasico :: boolean");
 
          TAtributos tipoBasico= atributosPara("tipoBasico", "tipo");
 
          calculo(tipoBasico.a("tipo"), creaTipoBasicoBoolean);
          
          return tipoBasico;
  }
  public TAtributos rTipoBasico4(Object val) {
          regla("TipoBasico :: character");
          
          TAtributos tipoBasico= atributosPara("tipoBasico", "tipo");
 
          calculo(tipoBasico.a("tipo"), creaTipoBasicoCharacter);
          
          return tipoBasico;
  }
 
  public TAtributos rTipoNoBasico0(String val) {
          regla("TipoNoBasico :: identificador");
          
          TAtributos tipoNoBasico= atributosPara("tipoNoBasico", "tipo", "err", "tsh");
 
          Atributo astring= atributoLexicoPara("identificador","tipo", val);	
 
          dependencias(tipoNoBasico.a("tipo"), tipoNoBasico.a("tsh"), astring);
          dependencias(tipoNoBasico.a("err"), tipoNoBasico.a("tsh"), astring);
            
          calculo(tipoNoBasico.a("tipo"), creaTipoNoBasicoIden);
          calculo(tipoNoBasico.a("err"), errTipoNoBasico0);
          
          return tipoNoBasico;
  }
 
 
  public TAtributos rTipoNoBasico1(TAtributos decTuplas) {
          
          regla("TipoNoBasico:: ( DecTuplas )");
 
          TAtributos tipoNoBasico= atributosPara("tipoNoBasico", "tsh", "tipo", "err");
 
          dependencias(decTuplas.a("tsh"), tipoNoBasico.a("tsh"));
          dependencias(tipoNoBasico.a("tipo"), decTuplas.a("tipo"));
          dependencias(tipoNoBasico.a("err"), decTuplas.a("err"));
        
          calculo(decTuplas.a("tsh"), asignacion);	
          calculo(tipoNoBasico.a("tipo"), asignacion);
          calculo(tipoNoBasico.a("err"), asignacion);
          
          return tipoNoBasico;
  }
  	
  public TAtributos rDecTuplas0(TAtributos tuplas) {
          regla("DecTuplas :: Tuplas");
          
          TAtributos decTuplas = atributosPara("decTuplas", "tsh", "tipo", "err");
    
          dependencias(tuplas.a("tsh"), decTuplas.a("tsh"));       
          dependencias(decTuplas.a("tipo"), tuplas.a("tipo"));  
          dependencias(decTuplas.a("err"), tuplas.a("err"));  
 
          calculo(tuplas.a("tsh"), asignacion);       
          calculo(decTuplas.a("tipo"), asignacion);  
          calculo(decTuplas.a("err"), asignacion);  
          
          return tuplas;
  } 
     
  public TAtributos rDecTuplas1() {
          regla("DecTuplas -> vacio");
          
          TAtributos decTuplas = atributosPara("decTuplas", "tipo", "err", "tsh");
          
          calculo(decTuplas.a("tipo"), creaTuplaVacia);
          calculo(decTuplas.a("err"), devuelveVacio);
          
          return decTuplas;
  }
  	
  public TAtributos rTuplas0(TAtributos tuplas1, TAtributos declaracionTipo) {
          regla("Tuplas :: Tuplas , DeclaracionTipo");
          TAtributos tuplas0 = atributosPara("tuplas0", "tsh", "tipo", "err");
 
          dependencias(declaracionTipo.a("tsh"), tuplas0.a("tsh"));       
          dependencias(tuplas1.a("tsh"), tuplas0.a("tsh"));
          dependencias(tuplas0.a("tipo"), tuplas1.a("err"), declaracionTipo.a("err"), tuplas1.a("tipo"), declaracionTipo.a("tipo"));       
          dependencias(tuplas0.a("err"), tuplas1.a("err"), declaracionTipo.a("err"));  
          
          
          calculo(tuplas1.a("tsh"), asignacion);       
          calculo(declaracionTipo.a("tsh"), asignacion);       
          calculo(tuplas0.a("tipo"), compruebaTuplasAnyadeCampo);   
          calculo(tuplas0.a("err"), concatena);     
          
  		      
          return tuplas0;
  }   
  	
 
  public TAtributos rTuplas1(TAtributos declaracionTipo) {
          regla("Tuplas :: DeclaracionTipo");
          
          TAtributos tuplas = atributosPara("tuplas", "tsh", "tipo", "err");
          
          dependencias(declaracionTipo.a("tsh"), tuplas.a("tsh"));
          dependencias(tuplas.a("tipo"), declaracionTipo.a("err"), declaracionTipo.a("tipo"));
          dependencias(tuplas.a("err"), declaracionTipo.a("err"));
          
          calculo(declaracionTipo.a("tsh"), asignacion);
          calculo(tuplas.a("tipo"), compruebaYCreaTuplaConCampo);
          calculo(tuplas.a("err"), asignacion);
 
          
          return tuplas;
  }
 
  public TAtributos rTipoNoBasico2(TAtributos declaracionTipo, TAtributos tipoIndice) {
          regla("TipoNoBasico:: DeclaracionTipo [ TipoIndice ]");
          
          TAtributos tipoNoBasico = atributosPara("tipoNoBasico", "tsh", "tipo", "err");
          
          dependencias(declaracionTipo.a("tsh"), tipoNoBasico.a("tsh"));
          dependencias(tipoIndice.a("tsh"), tipoNoBasico.a("tsh"));
          dependencias(tipoNoBasico.a("tipo"), tipoIndice.a("err"), declaracionTipo.a("err"), tipoIndice.a("num"), declaracionTipo.a("tipo"));
          dependencias(tipoNoBasico.a("err"), declaracionTipo.a("err"), tipoIndice.a("err"));
 
          calculo(declaracionTipo.a("tsh"), asignacion);
          calculo(tipoIndice.a("tsh"), asignacion);
          calculo(tipoNoBasico.a("tipo"), compruebaYcreaArray);
          calculo(tipoNoBasico.a("err"), errTipoNoBasico2);
               
          return tipoNoBasico;
  }
 
 
  public TAtributos rTipoIndice0(String val) {
          regla("TipoIndice :: identificador");
          
          TAtributos tipoIndice = atributosPara("tipoIndice", "num", "tsh", "err");
          
          Atributo astring= atributoLexicoPara("identificador","lex", val);
 
          dependencias(tipoIndice.a("num"), tipoIndice.a("tsh"), astring);
          dependencias(tipoIndice.a("err"), tipoIndice.a("tsh"), astring);
          
          calculo(tipoIndice.a("num"), tsDameValorInt);   
          calculo(tipoIndice.a("err"), errTipoIndice0);  
               
          return tipoIndice;
  }
  	
  public TAtributos rTipoIndice1(Object val) {
          regla("TipoIndice :: naturales");
          
          TAtributos tipoIndice = atributosPara("tipoIndice", "num", "err", "tsh");
          
          Atributo astring= atributoLexicoPara("naturales","lex", val);
 
          dependencias(tipoIndice.a("num"), astring);
          
          calculo(tipoIndice.a("num"), asignacion);     
          calculo(tipoIndice.a("err"), devuelveVacio);     
               
          return tipoIndice;
  }
 
  public TAtributos rDecVariables0(TAtributos variables) {
          regla("DecVariables :: vars { Variables }");
          
          TAtributos decVariables = atributosPara("decVariables", "ts","tsh", /*"etqh", "etq", "cod",*/ "err");
 
          dependencias(variables.a("tsh"), decVariables.a("tsh"));
          dependencias(decVariables.a("ts"), variables.a("ts"));
      
          //dependencias(variables.a("etqh"), decVariables.a("etqh"));
          //dependencias(decVariables.a("etq"), variables.a("etq"));
 
          //dependencias(decVariables.a("cod"), variables.a("cod"));
          dependencias(decVariables.a("err"), variables.a("err"));
          
          calculo(variables.a("tsh"), asignacion);
          calculo(decVariables.a("ts"), asignacion);
          //calculo(variables.a("etqh"), asignacion);	
          //calculo(decVariables.a("etq"), asignacion);	
          //calculo(decVariables.a("cod"), asignacion);	
          calculo(decVariables.a("err"), asignacion);	
              
          return decVariables;
  } 
     	
 
 
  public TAtributos rDecVariables1(){
          regla("DecVariables :: vacio");
          
          TAtributos decVariables = atributosPara("decVariables", "ts", "tsh", /*"etqh", "etq", "cod",*/ "err");
 
          dependencias(decVariables.a("ts"), decVariables.a("tsh"));
          //dependencias(decVariables.a("etq"), decVariables.a("etqh"));
            
          calculo(decVariables.a("ts"), asignacion);
          //calculo(decVariables.a("etq"), asignacion);
          //calculo(decVariables.a("cod"), devuelveVacio);
          calculo(decVariables.a("err"), devuelveVacio);
          
          return decVariables;
  }
 
  public TAtributos rVariables0(TAtributos variables1,TAtributos variable) {
          
          regla("Variables :: Variables Variable");
                
 
          TAtributos variables0 = atributosPara("variables0", "ts", "tsh", /*"etq", "etqh", "cod",*/ "err");
 
          dependencias(variable.a("tsh"), variables1.a("tsh"));
          dependencias(variables1.a("tsh"), variables0.a("tsh"));
          dependencias(variables0.a("err"), variables1.a("err"), variable.a("err"), variables1.a("ts"), variable.a("lex"));
          dependencias(variables0.a("ts"), variables1.a("ts"), variable.a("lex"), variable.a("err"), variables1.a("err"), variable.a("clase"), variable.a("tipo"), variables0.a("err"));
          //dependencias(variables1.a("etqh"), variables0.a("etqh"));
          //dependencias(variables0.a("etq"), variables1.a("etq"));
          //dependencias(variables0.a("cod"), variables1.a("cod"), variables0.a("ts"), variable.a("lex"));
 
 
          calculo(variable.a("tsh"), asignacion);
          calculo(variables1.a("tsh"), asignacion);
          calculo(variables0.a("ts"), compruebaYanyadeIdVariables0);	
          //calculo(variables1.a("etqh"), asignacion);	
          //calculo(variables0.a("etq"),suma1);
          //calculo(variables0.a("cod"),codVariables0);
          calculo(variables0.a("err"), errVariables0);	
          
          return variables0;
  }
  	
  	
  public TAtributos rVariables1(TAtributos variable) {
          regla("Variables :: Variable");     
          
          
          TAtributos variables = atributosPara("variables", "ts", "tsh", /*"etq", "etqh", "cod",*/ "err");
 
          dependencias(variable.a("tsh"), variables.a("tsh"));
          dependencias(variables.a("ts"), variables.a("tsh"), variable.a("lex"), variable.a("err"), variable.a("clase"), variable.a("tipo"));     
          //dependencias(variables.a("etq"), variables.a("etqh"));   
          //dependencias(variables.a("cod"), variable.a("lex"), variables.a("ts"));   
          dependencias(variables.a("err"), variable.a("err"));    
      
          calculo(variable.a("tsh"), asignacion);
          calculo(variables.a("ts"), CompruebaCreaTsYAnyadeIdVariable);      
          //calculo(variables.a("etq"),suma1);
          //calculo(variables.a("cod"),reservaVariable);
          calculo(variables.a("err"),asignacion);
  		 
          return variables;
  }   
 
 
  public TAtributos rVariable0(TAtributos declaracionTipo, String iden) {
          regla("Variable ::  var DeclaracionTipo identificador ;");
          
          TAtributos variable= atributosPara("variable", "lex","clase", "tipo", "err", "etqh", "etq", "ts", "tsh");
 
          Atributo astring= atributoLexicoPara("identificador","lex", iden);
 
          dependencias(variable.a("lex"), astring);
          dependencias(declaracionTipo.a("tsh"), variable.a("tsh"));
          dependencias(variable.a("tipo"), declaracionTipo.a("tipo"));
          dependencias(variable.a("err"), declaracionTipo.a("err"));   
 
          calculo(declaracionTipo.a("tsh"), asignacion);
          calculo(variable.a("lex"), asignacion);		
          calculo(variable.a("clase"), devuelveVar);
          calculo(variable.a("tipo"), asignacion);
          calculo(variable.a("err"), asignacion);
          
          return variable;
  }   
 
  
  
  
  
  
  
  
  
  
  public TAtributos rDecSubprogramas0(TAtributos subprogramas) {
 		regla("DecSubprogramas :: subprograms { Subprogramas } ");
 
 		TAtributos decSubprogramas = atributosPara("decSubprogramas", "tsh", "ts", "cod", "etqh", "etq", "err");
 
 		dependencias(subprogramas.a("tsh"), decSubprogramas.a("tsh"));
 		dependencias(decSubprogramas.a("ts"), subprogramas.a("ts"), subprogramas.a("cod"), subprogramas.a("err"), subprogramas.a("etq"));
 
 		dependencias(decSubprogramas.a("cod"), subprogramas.a("cod"));
 		dependencias(subprogramas.a("etqh"), decSubprogramas.a("etqh"));
 		dependencias(decSubprogramas.a("etq"), subprogramas.a("etq"));
 
 		dependencias(decSubprogramas.a("err"), subprogramas.a("err"));
 
 		calculo(subprogramas.a("tsh"), asignacion); 
 		calculo(decSubprogramas.a("ts"), desactivaNivelLocal); 
 		calculo(decSubprogramas.a("cod"), asignacion); 
 		calculo(subprogramas.a("etqh"), asignacion);
 		calculo(decSubprogramas.a("etq"), asignacion);
 		calculo(decSubprogramas.a("err"), asignacion);
 
 		return decSubprogramas ;
 	}
 
 
 	public TAtributos rDecSubprogramas1() {
 		regla("DecSubprogramas :: vacio ");
 
 		TAtributos decSubprogramas = atributosPara("decSubprogramas", "tsh", "ts", "cod", "etqh", "etq", "err");
 
 
 		dependencias(decSubprogramas.a("ts"), decSubprogramas.a("tsh"));
 
 		dependencias(decSubprogramas.a("etq"), decSubprogramas.a("etqh"));
 
 
 		calculo(decSubprogramas.a("ts"), asignacion); 
 		calculo(decSubprogramas.a("cod"), devuelveVacio); 
 		calculo(decSubprogramas.a("etq"), asignacion);
 		calculo(decSubprogramas.a("err"), devuelveVacio);
 
 		return decSubprogramas ;
 	}
 
 	public TAtributos rSubprogramas0(TAtributos subprogramas1, TAtributos subprograma){
 		regla("Subprogramas :: Subprogramas Subprograma");
 	
 		TAtributos subprogramas0 = atributosPara("subprogramas0", "tsh", "etq", "etqh", "cod", "err", "ts");
 	
 		dependencias(subprogramas1.a("tsh"), subprogramas0.a("tsh"));
		dependencias(subprograma.a("tsh"), subprogramas1.a("ts"), subprogramas1.a("cod"));  
 		dependencias(subprogramas0.a("ts"), subprogramas1.a("ts"), subprograma.a("lex"), subprograma.a("clase"), subprograma.a("tipo"), subprogramas0.a("err")); 
 		dependencias(subprogramas0.a("cod"), subprogramas1.a("cod"), subprograma.a("cod"));  
 		dependencias(subprogramas1.a("etqh"), subprogramas0.a("etqh")); 
 		dependencias(subprograma.a("etqh"), subprogramas1.a("etq")); 
 		dependencias(subprogramas0.a("etq"), subprograma.a("etq")); 
 		dependencias(subprogramas0.a("err"), subprogramas1.a("err"), subprograma.a("err"), subprogramas1.a("ts"), subprograma.a("lex"));  
 	
 	
 		calculo(subprogramas1.a("tsh"), asignacion);
 		calculo(subprograma.a("tsh"), asignacionDependencias1);
 		calculo(subprogramas0.a("ts"), compruebaYanyadeIdSubprogramas0);  
 		calculo(subprogramas0.a("cod"), concatena);  
 		calculo(subprogramas1.a("etqh"), asignacion);  
 		calculo(subprograma.a("etqh"), asignacion); 
 		calculo(subprogramas0.a("etq"), asignacion); 
 		calculo(subprogramas0.a("err"), errSubprogramas0);  
 		
 		return subprogramas0;
 		
 	}
 	public TAtributos rSubprogramas1(TAtributos subprograma){
 		regla("Subprogramas :: Subprograma");
 	
 		TAtributos subprogramas = atributosPara("subprogramas", "tsh", "etq", "etqh", "cod", "err", "ts");
 	
 		dependencias(subprogramas.a("ts"), subprogramas.a("tsh"), subprograma.a("lex"), subprograma.a("clase"), subprograma.a("tipo"), subprograma.a("err"));
 		dependencias(subprograma.a("tsh"), subprogramas.a("tsh"));
 		dependencias(subprogramas.a("cod"), subprograma.a("cod"));
 		dependencias(subprograma.a("etqh"), subprogramas.a("etqh"));
 		dependencias(subprogramas.a("etq"), subprograma.a("etq"));
 		dependencias(subprogramas.a("err"), subprograma.a("err"));//, subprogramas.a("tsh"), subprograma.a("lex"));
 	
 		calculo(subprogramas.a("ts"), compruebaYanyadeIdSubprogramas1);
 		calculo(subprograma.a("tsh"), asignacion);
 		calculo(subprogramas.a("cod"), asignacion);
 		calculo(subprograma.a("etqh"), asignacion);
 		calculo(subprogramas.a("etq"), asignacion);
 		//calculo(subprogramas.a("err"), errSubprogramas1);
 		calculo(subprogramas.a("err"), asignacion);
 		
 	
 		return subprogramas;
 		
 	}
 
 
 	public TAtributos rSubprograma(String identificador, TAtributos decParams, TAtributos cuerpo){
 		regla("subprogram : identificador(DecParams){Cuerpo}");
 	
 		TAtributos subprograma = atributosPara("subprograma", "tsh", "etq", "etqh", "cod", "err", "tipo", "lex", "clase");
 	
 		Atributo astring= atributoLexicoPara("identificador","lex", identificador);
 	
 		dependencias(subprograma.a("lex"), astring);
 		dependencias(subprograma.a("tipo"), decParams.a("params"), subprograma.a("etqh"));
 		dependencias(decParams.a("tsh"), subprograma.a("tsh"));
 		dependencias(cuerpo.a("tsh"), decParams.a("ts"), subprograma.a("lex"), subprograma.a("clase"), subprograma.a("tipo"));
 		dependencias(subprograma.a("cod"), cuerpo.a("cod"), decParams.a("desp"));
 		dependencias(cuerpo.a("etqh"), subprograma.a("etqh"));
 		dependencias(subprograma.a("etq"), cuerpo.a("etq"));
 		dependencias(subprograma.a("err"), decParams.a("err"), cuerpo.a("err"), decParams.a("tsh"), subprograma.a("lex"));
 	
 		calculo(subprograma.a("clase"), devuelveProc);
 		calculo(subprograma.a("lex"), asignacion);
 		calculo(subprograma.a("tipo"), construyeTipoSubprograma);
 		calculo(decParams.a("tsh"), activaNivelLocal);
 		calculo(cuerpo.a("tsh"), compruebaYanyadeIdSubprograma);
 		calculo(subprograma.a("cod"), codSubprograma);
 		calculo(cuerpo.a("etqh"), suma15);
 		calculo(subprograma.a("etq"), suma16);
 		calculo(subprograma.a("err"), errSubprograma);
 	
 		return subprograma;
 
 
 	}
 
 
 
 	public TAtributos rDecParams0(){
 		regla("DecParams:: VACIO");
 
 		TAtributos decParams= atributosPara("decParams", "tsh", "err", "ts", "desp", "desph", "params");
 	
 		dependencias(decParams.a("ts"), decParams.a("tsh"));
 	
 		calculo(decParams.a("ts"), asignacion);
 		calculo(decParams.a("desp"), devuelve0);
 		calculo(decParams.a("params"), devuelveListaVacia);
 		calculo(decParams.a("err"), devuelveVacio);
 	
 		return decParams;
 	}
 
 	public TAtributos rDecParams1(TAtributos parametros){
 		regla("DecParams:: Parametros");
 
 		TAtributos decParams= atributosPara("decParams", "tsh", "err", "ts", "desp", "desph", "params");
 	
 		dependencias(parametros.a("tsh"), decParams.a("tsh"));
 		dependencias(decParams.a("ts"), parametros.a("ts"));
 		dependencias(decParams.a("params"), parametros.a("params"));
 		dependencias(decParams.a("desp"), parametros.a("desp"));
 		dependencias(decParams.a("err"), parametros.a("err"));
 	
 		calculo(parametros.a("tsh"), asignacion);
 		calculo(decParams.a("ts"), asignacion);
 		calculo(decParams.a("params"), asignacion);
 		calculo(parametros.a("desph"), devuelve0);
 		calculo(decParams.a("desp"), asignacion);
 		calculo(decParams.a("err"), asignacion);
 
 		return decParams;
 	}
 
 
 	public TAtributos rParametros0(TAtributos parametros1, TAtributos parametro){
 		regla("Parametros:: Parametros,Parametro");
 
 		TAtributos parametros0= atributosPara("parametros0", "tsh", "err", "ts", "desph", "desp", "params");
 	
 		dependencias(parametros0.a("ts"), parametros1.a("ts"), parametro.a("lex"), parametro.a("clase"), parametro.a("tipo"), parametro.a("err"), parametros0.a("err"));
 		dependencias(parametros1.a("tsh"), parametros0.a("tsh"));
 		dependencias(parametro.a("tsh"), parametros1.a("ts"));
 		dependencias(parametros0.a("params"), parametros1.a("params"), parametro.a("params"), parametros0.a("err"));
 		dependencias(parametros1.a("desph"), parametros0.a("desph"));
 		dependencias(parametro.a("desph"), parametros1.a("desp"));
 		dependencias(parametros0.a("desp"), parametro.a("desp"));
 		dependencias(parametros0.a("err"), parametros1.a("err"), parametro.a("err"), parametros1.a("params"), parametro.a("params"));
 	
 	
 		calculo(parametros0.a("ts"), compruebaYanyadeIdParametros0);
 		calculo(parametros1.a("tsh"), asignacion);
 		calculo(parametro.a("tsh"), asignacion);
 		calculo(parametros0.a("params"), anyadeParam);
 		calculo(parametros1.a("desph"), asignacion);
 		calculo(parametro.a("desph"), asignacion);
 		calculo(parametros0.a("desp"), asignacion);
 		calculo(parametros0.a("err"), errParametros0);
 	
 		return parametros0;
 
 
 	}
 
 
 	public TAtributos rParametros1(TAtributos parametro){
 		regla("Parametros:: Parametro");
 
 		TAtributos parametros= atributosPara("parametros", "tsh", "err", "ts", "desph", "desp", "params");
 	
 	
 		dependencias(parametro.a("tsh"), parametros.a("tsh"));
 		dependencias(parametros.a("ts"), parametro.a("err"), parametros.a("tsh"), parametro.a("lex"), parametro.a("clase"), parametro.a("tipo"));
 		dependencias(parametros.a("params"), parametro.a("params"));
 		dependencias(parametro.a("desph"), parametros.a("desph"));
 		dependencias(parametros.a("desp"), parametro.a("desp"));
 		dependencias(parametros.a("err"), parametro.a("err"));
 	
 	
 		calculo(parametro.a("tsh"), asignacion);
 		calculo(parametros.a("ts"), compruebaYanyadeIdParametros1);
 		calculo(parametros.a("params"), asignacion);
 		calculo(parametro.a("desph"), asignacion);
 		calculo(parametros.a("desp"), asignacion);
 		calculo(parametros.a("err"), asignacion);
 	
 	
 	
 		return parametros;
 
 
 	}
 	public TAtributos rParametro0(TAtributos declaracionTipo, String identificador){
 		regla("Parametro:: DeclaracionTipo identificador");
 
 		TAtributos parametro= atributosPara("parametro", "tsh", "err", "ts", "desph", "desp", "params", "tipo", "lex", "clase");
 	
 		Atributo astring= atributoLexicoPara("identificador","lex", identificador);
 	
 	
 		dependencias(declaracionTipo.a("tsh"), parametro.a("tsh"));
 		dependencias(parametro.a("params"), parametro.a("desph"), declaracionTipo.a("tipo"), astring, parametro.a("err"));
 		dependencias(parametro.a("tipo"), declaracionTipo.a("tipo"));
 		dependencias(parametro.a("lex"), astring);
 		dependencias(parametro.a("desp"), parametro.a("desph"), declaracionTipo.a("tipo"));
 		dependencias(parametro.a("err"), declaracionTipo.a("err"), parametro.a("tsh"), astring);
 	
 	
 		calculo(declaracionTipo.a("tsh"), asignacion);
 		calculo(parametro.a("params"), paramsParametro0);
 		calculo(parametro.a("tipo"), asignacion);
 		calculo(parametro.a("lex"), asignacion);
 		calculo(parametro.a("clase"), devuelveVar);
 		calculo(parametro.a("desp"), calculaDesp);
 		calculo(parametro.a("err"), errParametro0);
 	
 		return parametro;
 
 
 	}
 
 	public TAtributos rParametro1(TAtributos declaracionTipo, String identificador){
 		regla("Parametro:: DeclaracionTipo * identificador");
 
 		TAtributos parametro= atributosPara("parametro", "tsh", "err", "ts", "desph", "desp", "params", "tipo", "lex", "clase");
 	
 		Atributo astring= atributoLexicoPara("identificador","lex", identificador);
 	
 	
 		dependencias(declaracionTipo.a("tsh"), parametro.a("tsh"));
 		dependencias(parametro.a("params"), parametro.a("desph"), declaracionTipo.a("tipo"), astring);
 		dependencias(parametro.a("tipo"), declaracionTipo.a("tipo"));
 		dependencias(parametro.a("lex"), astring);
 		dependencias(parametro.a("desp"), parametro.a("desph"));
 		dependencias(parametro.a("err"), declaracionTipo.a("err"), parametro.a("tsh"), astring);
 	
 	
 		calculo(declaracionTipo.a("tsh"), asignacion);
 		calculo(parametro.a("params"), paramsParametro1);
 		calculo(parametro.a("tipo"), asignacion);
 		calculo(parametro.a("lex"), asignacion);
 		calculo(parametro.a("clase"), devuelvePvar);
 		calculo(parametro.a("desp"), suma1);
 		calculo(parametro.a("err"), errParametro0);
 	
 	
 	
 		return parametro;
 
 
 	}
 
 
 
 	public TAtributos rCuerpo(TAtributos decVariables, TAtributos decInstruc){
 		regla("Cuerpo :: DecVariables DecInstruc");
 
 		TAtributos cuerpo= atributosPara("cuerpo", "tsh", "cod", "etq", "etqh",  "err");
 	
 	
 		dependencias(decVariables.a("tsh"), cuerpo.a("tsh"));
 		dependencias(decInstruc.a("tsh"), decVariables.a("ts"));
 		dependencias(cuerpo.a("cod"), decInstruc.a("cod"));
 		dependencias(decInstruc.a("etqh"), cuerpo.a("etqh"));
 		dependencias(cuerpo.a("etq"), decInstruc.a("etq"));
 		dependencias(cuerpo.a("err"), decVariables.a("err"), decInstruc.a("err"));
 	
 	
 		calculo(decVariables.a("tsh"), asignacion);
 		calculo(decInstruc.a("tsh"), asignacion);
 		calculo(cuerpo.a("cod"), asignacion);
 		calculo(decInstruc.a("etqh"), asignacion);
 		calculo(cuerpo.a("etq"), asignacion);
 		calculo(cuerpo.a("err"), concatena);
 	
 	
 		return cuerpo;
 
 
 	}
 
  
  public TAtributos rDecInstruc(TAtributos instrs) {
          regla("DecInstruc:: instructions { Instrs }");
          
          TAtributos decInstruc = atributosPara("decInstruc", "tsh", "etq", "etqh", "cod", "err");
 
          dependencias(instrs.a("tsh"), decInstruc.a("tsh"));       
          dependencias(instrs.a("etqh"), decInstruc.a("etqh"));   
          dependencias(decInstruc.a("etq"), instrs.a("etq"));   
          dependencias(decInstruc.a("cod"), instrs.a("cod"));  
          dependencias(decInstruc.a("err"), instrs.a("err"));  
          
          calculo(instrs.a("tsh"), asignacion);       
          calculo(instrs.a("etqh"), asignacion);   
          calculo(decInstruc.a("etq"), asignacion);   
          calculo(decInstruc.a("cod"), asignacion);  
          calculo(decInstruc.a("err"), asignacion);  
          		      
          return decInstruc;
  }   
 
  public TAtributos rInstrs0(TAtributos instrs1, TAtributos instr) {
          regla("Instrs:: Instrs Instr");
          
          TAtributos instrs0= atributosPara("instrs0", "tsh", "etqh", "etq", "cod", "err");
 
          dependencias(instrs1.a("tsh"), instrs0.a("tsh"));       
          dependencias(instr.a("tsh"), instrs0.a("tsh"));   
          dependencias(instrs1.a("etqh"), instrs0.a("etqh"));   
          dependencias(instr.a("etqh"), instrs1.a("etq"));   
          dependencias(instrs0.a("etq"), instr.a("etq"));   
          dependencias(instrs0.a("cod"), instrs1.a("cod"), instr.a("cod"));   
          dependencias(instrs0.a("err"), instrs1.a("err"), instr.a("err"));   
          
          calculo(instrs1.a("tsh"), asignacion);       
          calculo(instr.a("tsh"), asignacion);   
 
          calculo(instrs1.a("etqh"), asignacion);   
          calculo(instr.a("etqh"), asignacion);   
          calculo(instrs0.a("etq"), asignacion);   
          calculo(instrs0.a("cod"),concatena);   
          calculo(instrs0.a("err"),concatena);   
  		   
          return instrs0;
  }   
 
 
  public TAtributos rInstrs1(TAtributos instr) {
          regla("Instrs:: Instr");
          
          TAtributos instrs= atributosPara("instrs", "tsh", "etq", "etqh", "cod", "err");
 
          dependencias(instr.a("tsh"), instrs.a("tsh"));   
          dependencias(instr.a("etqh"), instrs.a("etqh"));   
          dependencias(instrs.a("etq"), instr.a("etq"));   
          dependencias(instrs.a("cod"), instr.a("cod"));   
          dependencias(instrs.a("err"), instr.a("err"));   
          
          calculo(instr.a("tsh"), asignacion);   
          calculo(instr.a("etqh"), asignacion);   
          calculo(instrs.a("etq"),  asignacion);   
          calculo(instrs.a("cod"), asignacion);   
          calculo(instrs.a("err"), asignacion);   
  		   
          return instrs;
     }
 
  public TAtributos rInstr0(TAtributos asign) {
          regla("Instr:: Asign");
          
           TAtributos instr= atributosPara("instr", "tsh", "etq", "etqh", "cod", "err");
 
          dependencias(asign.a("tsh"), instr.a("tsh"));   
          dependencias(asign.a("etqh"), instr.a("etqh"));  
          dependencias(instr.a("etq"), asign.a("etq"));  
          dependencias(instr.a("cod"), asign.a("cod"));  
          dependencias(instr.a("err"), asign.a("err"));  
          
          calculo(asign.a("tsh"), asignacion);   
          calculo(asign.a("etqh"), asignacion);  
          calculo(instr.a("etq"), asignacion);  
          calculo(instr.a("cod"), asignacion);  
          calculo(instr.a("err"), asignacion);  
  		   
          return instr;
  }
 
  	
 
  public TAtributos rInstr1(TAtributos lectura) {
          regla("Instr:: Lectura");
          
           TAtributos instr= atributosPara("instr", "tsh", "err", "etq", "etqh", "cod");
 
          dependencias(lectura.a("tsh"), instr.a("tsh"));   
          dependencias(lectura.a("etqh"), instr.a("etqh"));  
          dependencias(instr.a("etq"), lectura.a("etq"));  
          dependencias(instr.a("cod"), lectura.a("cod")); 
          dependencias(instr.a("err"), lectura.a("err")); 
          
          calculo(lectura.a("tsh"), asignacion);   
          calculo(lectura.a("etqh"), asignacion);  
          calculo(instr.a("etq"), asignacion);  
          calculo(instr.a("cod"), asignacion);  
          calculo(instr.a("err"), asignacion);  
  		   
          return instr;
  }
  		
  	
 
  public TAtributos rInstr2(TAtributos escritura) {
          regla("Instr:: Escritura");
          
           TAtributos instr= atributosPara("instr", "tsh", "etq", "etqh", "cod", "err");
 
          dependencias(escritura.a("tsh"), instr.a("tsh"));   
          dependencias(escritura.a("etqh"), instr.a("etqh"));  
          dependencias(instr.a("etq"), escritura.a("etq"));  
          dependencias(instr.a("cod"), escritura.a("cod")); 
          dependencias(instr.a("err"), escritura.a("err")); 
          
          calculo(escritura.a("tsh"), asignacion);   
          calculo(escritura.a("etqh"), asignacion);  
          calculo(instr.a("etq"), asignacion);  
          calculo(instr.a("cod"), asignacion);  
          calculo(instr.a("err"), asignacion);  
  		   
          return instr;
  } 
     	
 
  public TAtributos rInstr3(TAtributos swap1) {
          regla("Instr:: Swap1");
          
           TAtributos instr= atributosPara("instr", "etq", "etqh", "cod", "err", "tsh");  
 
          dependencias(swap1.a("etqh"), instr.a("etqh"));  
          dependencias(instr.a("etq"), swap1.a("etq"));  
          dependencias(instr.a("cod"), swap1.a("cod")); 
 
          calculo(swap1.a("etqh"), asignacion);  
          calculo(instr.a("etq"), asignacion);  
          calculo(instr.a("cod"), asignacion);  
          calculo(instr.a("err"), devuelveVacio);   
  		   
          return instr;
  }
  		
  public TAtributos rInstr4(TAtributos swap2) {
          regla("Instr:: Swap2");
          
           TAtributos instr= atributosPara("instr", "etq", "etqh", "cod", "err", "tsh");        
 
          dependencias(swap2.a("etqh"), instr.a("etqh"));  
          dependencias(instr.a("etq"), swap2.a("etq"));  
          dependencias(instr.a("cod"), swap2.a("cod"));        
 
          calculo(swap2.a("etqh"), asignacion);  
          calculo(instr.a("etq"), asignacion);  
          calculo(instr.a("cod"), asignacion); 
          calculo(instr.a("err"), devuelveVacio);  
 
          return instr;
  }
 
  public TAtributos rInstr5(TAtributos ifs) {
          regla("Instr:: Ifs");
          
           TAtributos instr= atributosPara("instr", "tsh", "err", "etq", "etqh", "cod");
 
          dependencias(ifs.a("tsh"), instr.a("tsh"));   
          dependencias(ifs.a("etqh"), instr.a("etqh"));  
          dependencias(instr.a("etq"), ifs.a("etq"));  
          dependencias(instr.a("cod"), ifs.a("cod")); 
          dependencias(instr.a("err"), ifs.a("err")); 
          
          calculo(ifs.a("tsh"), asignacion);   
          calculo(ifs.a("etqh"), asignacion);  
          calculo(instr.a("etq"), asignacion);  
          calculo(instr.a("cod"), asignacion); 
          calculo(instr.a("err"), asignacion); 
  		   
          return instr;
  }
 
 
  public TAtributos rInstr6(TAtributos whiles) {
          regla("Instr:: Whiles");
          
           TAtributos instr= atributosPara("instr", "tsh", "err", "etq", "etqh", "cod");
 
          dependencias(whiles.a("tsh"), instr.a("tsh"));   
          dependencias(whiles.a("etqh"), instr.a("etqh"));  
          dependencias(instr.a("etq"), whiles.a("etq"));  
          dependencias(instr.a("cod"), whiles.a("cod")); 
          dependencias(instr.a("err"), whiles.a("err")); 
          
          calculo(whiles.a("tsh"), asignacion);   
          calculo(whiles.a("etqh"), asignacion);  
          calculo(instr.a("etq"), asignacion);  
          calculo(instr.a("cod"), asignacion); 
          calculo(instr.a("err"), asignacion); 
  		   
          return instr;
  }
 
  
  public TAtributos rInstr7(TAtributos calls){
 		regla("Instr:: Calls");
 
 	TAtributos instr = atributosPara("instr", "tsh", "cod", "etq", "etqh",  "err");
 
 
 	dependencias(calls.a("tsh"), instr.a("tsh"));
 	dependencias(instr.a("err"), calls.a("err"));
 	dependencias(instr.a("etq"), calls.a("etq"));
 	dependencias(calls.a("etqh"), instr.a("etqh"));
 	dependencias(instr.a("cod"), calls.a("cod"));
 
 
 	calculo(calls.a("tsh"), asignacion);
 	calculo(instr.a("err"), asignacion);
 	calculo(instr.a("etq"), asignacion);
 	calculo(calls.a("etqh"), asignacion);
 	calculo(instr.a("cod"), asignacion);
 
 
 	return instr;
 
 
 }
  
  
  
 
  public TAtributos rAsign(TAtributos designador, TAtributos expr) {
          regla("Asign:: Designador = Expr ;");
          
          TAtributos asign= atributosPara("asign", "tsh", "etq", "etqh", "cod", "err", "esDesignador");	
 
          dependencias(designador.a("tsh"), asign.a("tsh"));
          dependencias(expr.a("tsh"), asign.a("tsh"));     
          dependencias(designador.a("etqh"), asign.a("etqh"));   
          dependencias(expr.a("etqh"), designador.a("etq"));    
          dependencias(asign.a("etq"), expr.a("etq"));      
          dependencias(asign.a("cod"), designador.a("cod"), expr.a("cod"), expr.a("esDesignador"), expr.a("tipo"));      
          dependencias(asign.a("err"), designador.a("err"), expr.a("err"), designador.a("tipo"), expr.a("tipo"), designador.a("esConstante"));      
 
          calculo(designador.a("tsh"), asignacion);
          calculo(expr.a("tsh"), asignacion);
 
          calculo(designador.a("etqh"), asignacion);   
          calculo(expr.a("etqh"), asignacion);    
          calculo(asign.a("etq"), suma1);      
          calculo(asign.a("cod"), codAsign);     
          calculo(asign.a("err"), errAsign);      
          
          return asign;
  }
 
 
 
  public TAtributos rSwap1() {
          regla("Swap1 :: swap1 ( ) ;");
          
          TAtributos swap1= atributosPara("swap1", "etq", "etqh", "cod");
 
          dependencias(swap1.a("etq"), swap1.a("etqh"));      
 
          calculo(swap1.a("etq"), suma1);    
          calculo(swap1.a("cod"), devuelveSwap1);      
          
          return swap1;
  }
 
 
  public TAtributos rSwap2() {
          
          regla("Swap2 :: swap2 ( ) ;");
          
          TAtributos swap2= atributosPara("swap2", "etq", "etqh", "cod");
 
          dependencias(swap2.a("etq"), swap2.a("etqh"));  
          
          calculo(swap2.a("cod"), devuelveSwap2);     
          
          return swap2;
 
  }
 
  public TAtributos rLectura( TAtributos designador) {
          regla("Lectura:: in ( Designador ) ;");
          
          TAtributos lectura= atributosPara("lectura", "tsh", "etq", "etqh", "cod", "err");
 
          dependencias(designador.a("tsh"), lectura.a("tsh"));  
          dependencias(designador.a("etqh"), lectura.a("etqh")); 
          dependencias(lectura.a("etq"), designador.a("etq")); 
          dependencias(lectura.a("cod"), designador.a("cod")); 
          dependencias(lectura.a("err"), designador.a("err"), designador.a("tipo")); 
 
          calculo(designador.a("tsh"), asignacion);    
          calculo(designador.a("etqh"), asignacion); 
          calculo(lectura.a("etq"), suma2); 
          calculo(lectura.a("cod"), codLectura);     
          calculo(lectura.a("err"), errLectura);     
          
          return lectura;
  } 
 
  public TAtributos rEscritura(TAtributos  expr) {
          regla("Escritura:: out ( Expr ) ;");
          
          TAtributos escritura= atributosPara("escritura", "tsh", "etq", "etqh", "cod", "err");
          
          dependencias(expr.a("tsh"), escritura.a("tsh"));         
          dependencias(expr.a("etqh"), escritura.a("etqh"));  
          dependencias(escritura.a("etq"), expr.a("etq"), expr.a("esDesignador"));  
          dependencias(escritura.a("cod"), expr.a("cod"), expr.a("esDesignador"));  
          dependencias(escritura.a("err"), expr.a("err"), expr.a("tipo"));  
 
          calculo(expr.a("tsh"), asignacion);
          calculo(expr.a("etqh"), asignacion);  
          calculo(escritura.a("etq"), suma12Designador);  
          calculo(escritura.a("cod"), codEscritura);  
          calculo(escritura.a("err"), errEscritura);  
          
          return escritura;
  }
  	
  public TAtributos rIfs0(TAtributos expr, TAtributos instrsV0, TAtributos instrsV1) {
          regla("Ifs :: if Expr then InstrsV else InstrsV endif");
          
          TAtributos ifs= atributosPara("ifs", "tsh", "etq", "etqh", "cod", "err");
 
          dependencias(expr.a("tsh"), ifs.a("tsh"));
          dependencias(instrsV0.a("tsh"), ifs.a("tsh"));
          dependencias(instrsV1.a("tsh"), ifs.a("tsh"));
          dependencias(expr.a("etqh"), ifs.a("etqh"));
          dependencias(instrsV0.a("etqh"), expr.a("etq"), expr.a("esDesignador"));
          dependencias(instrsV1.a("etqh"), instrsV0.a("etq"));
          dependencias(ifs.a("etq"), instrsV1.a("etq"));
          dependencias(ifs.a("cod"), expr.a("cod"), expr.a("esDesignador"), instrsV0.a("etq"), instrsV0.a("cod"), instrsV1.a("etq"), instrsV1.a("cod"));
          dependencias(ifs.a("err"), expr.a("err"), instrsV0.a("err"), instrsV1.a("err"));
          
          calculo(expr.a("tsh"), asignacion);       
          calculo(instrsV0.a("tsh"), asignacion);   
          calculo(instrsV1.a("tsh"), asignacion);
 
          calculo(expr.a("etqh"), asignacion);
          calculo(instrsV0.a("etqh"), suma12Designador);
          calculo(instrsV1.a("etqh"), suma1);
          calculo(ifs.a("etq"), asignacion);
          calculo(ifs.a("cod"), codIfs0);
          calculo(ifs.a("err"), concatena);
  		      
          return ifs;
  }   
 
  public TAtributos rIfs1(TAtributos expr, TAtributos instrsV) {
          regla("Ifs :: if Expr then InstrsV endif");
          
          TAtributos ifs= atributosPara("ifs", "tsh", "etq", "etqh", "cod", "err");
 
          dependencias(expr.a("tsh"), ifs.a("tsh"));
          dependencias(instrsV.a("tsh"), ifs.a("tsh"));
          dependencias(expr.a("etqh"), ifs.a("etqh"));
          dependencias(instrsV.a("etqh"), expr.a("etq"), expr.a("esDesignador"));
          dependencias(ifs.a("etq"), instrsV.a("etq"));
          dependencias(ifs.a("cod"), expr.a("cod"), expr.a("esDesignador"), instrsV.a("etq"), instrsV.a("cod"));
          dependencias(ifs.a("err"), expr.a("err"), instrsV.a("er"));
 
 
          calculo(expr.a("tsh"), asignacion);
          calculo(instrsV.a("tsh"), asignacion);
          calculo(expr.a("etqh"), asignacion);
          calculo(instrsV.a("etqh"), suma12Designador);
          calculo(ifs.a("etq"),asignacion);
          calculo(ifs.a("cod"), codIfs1);
          calculo(ifs.a("err"),concatena);
          
          return ifs;
  }
 
  public TAtributos rInstrsV0(TAtributos instrs) {
          regla("InstrsV :: Instrs");
         
          TAtributos instrsV= atributosPara("instrsV", "tsh", "etq", "etqh", "cod", "err");	
 
          dependencias(instrs.a("tsh"), instrsV.a("tsh"));
          dependencias(instrs.a("etqh"), instrsV.a("etqh"));
          dependencias(instrsV.a("etq"), instrs.a("etq"));
          dependencias(instrsV.a("cod"), instrs.a("cod"));
          dependencias(instrsV.a("err"), instrs.a("err"));
            
          calculo(instrs.a("tsh"), asignacion);
          calculo(instrs.a("etqh"), asignacion);
          calculo(instrsV.a("etq"), asignacion);
          calculo(instrsV.a("cod"), asignacion);
          calculo(instrsV.a("err"), asignacion);
          
          return instrsV;
  }
 
 
  public TAtributos rInstrsV1() {
          
          regla("InstrsV :: vacio");
          
          TAtributos instrsV= atributosPara("instrsV", "etq", "etqh", "cod");
       
          dependencias(instrsV.a("etq"), instrsV.a("etqh"));	
 
          calculo(instrsV.a("etq"), asignacion);	
          calculo(instrsV.a("cod"), devuelveVacio);	
          calculo(instrsV.a("err"), devuelveVacio);	
          
          return instrsV;
  }
 
  public TAtributos rWhiles(TAtributos expr, TAtributos instrsV) {
          regla("Whiles :: while Expr do InstrsV endwhile ;");
          
          TAtributos whiles= atributosPara("whiles", "tsh", "etq", "etqh", "cod", "err");
 
          dependencias(expr.a("tsh"), whiles.a("tsh"));       
          dependencias(instrsV.a("tsh"), whiles.a("tsh"));  
          dependencias(expr.a("etqh"), whiles.a("etqh"));  
          dependencias(instrsV.a("etqh"), expr.a("etq"), expr.a("esDesignador"));  
          dependencias(whiles.a("etq"), instrsV.a("etq"));  
          dependencias(whiles.a("cod"), expr.a("cod"), expr.a("esDesignador"), instrsV.a("etq"), instrsV.a("cod"), whiles.a("etqh"));  
          dependencias(whiles.a("err"), expr.a("err"), instrsV.a("err"));  
 
          calculo(expr.a("tsh"), asignacion);       
          calculo(instrsV.a("tsh"), asignacion);  
          calculo(expr.a("etqh"), asignacion);  
          calculo(instrsV.a("etqh"), suma12Designador);  
          calculo(whiles.a("etq"), suma1);  
          calculo(whiles.a("cod"), codWhile);  
          calculo(whiles.a("err"), concatena);  
          
          return whiles;
  } 
 
  public TAtributos rCalls(String identificador, TAtributos decArgs){
 		regla("Calls :: call identificador ( DecArgs ) ;");
 
 		TAtributos calls= atributosPara("calls", "tsh", "cod", "etq", "etqh",  "err");
 	
 		Atributo astring= atributoLexicoPara("identificador","lex", identificador);
 	
 		dependencias(decArgs.a("tsh"), calls.a("tsh"));
 		dependencias(decArgs.a("etqh"), calls.a("etqh"));
 		dependencias(calls.a("etq"), decArgs.a("etq"), decArgs.a("cod"));
 		dependencias(calls.a("cod"), decArgs.a("etq"), calls.a("tsh"), astring, decArgs.a("cod"));
 		dependencias(decArgs.a("idProch"), astring);
 		dependencias(calls.a("err"), decArgs.a("err"), calls.a("tsh"), astring, decArgs.a("numParams"));
 	
 		calculo(decArgs.a("tsh"), asignacion);
 		calculo(decArgs.a("etqh"), asignacion);
 		calculo(calls.a("etq"), suma67);
 		calculo(calls.a("cod"), codCalls);
 		calculo(decArgs.a("idProch"), asignacion);
 		calculo(calls.a("err"), errCalls);
 	
 	
 	
 		return calls;
 
 
 	}
 
 	public TAtributos rDecArgs0(TAtributos argumentos){
 		regla("DecArgs:: Argumentos");
 
 		TAtributos decArgs= atributosPara("decArgs", "tsh", "etq", "etqh", "cod", "err", "idProch", "numParams");
 	
 	
 		dependencias(argumentos.a("tsh"), decArgs.a("tsh"));
 		dependencias(decArgs.a("etq"), argumentos.a("etq"));
 		dependencias(argumentos.a("etqh"), decArgs.a("etqh"));
 		dependencias(argumentos.a("idProch"), decArgs.a("idProch"));
 		dependencias(decArgs.a("numParams"), argumentos.a("numParams"));
 		dependencias(decArgs.a("err"), argumentos.a("err"));
 		dependencias(decArgs.a("cod"), argumentos.a("cod"));
 	
 	
 	
 		calculo(argumentos.a("tsh"), asignacion);
 		calculo(decArgs.a("etq"), asignacion);
 		calculo(argumentos.a("etqh"), asignacion);
 		calculo(argumentos.a("idProch"), asignacion);
 		calculo(decArgs.a("numParams"), asignacion);
 		calculo(argumentos.a("numParamsh"), devuelve0);
 		calculo(decArgs.a("err"), asignacion);
 		calculo(decArgs.a("cod"), asignacion);
 		calculo(argumentos.a("listaParamsh"), creaListaParamsVacia);
 	
 	
 	
 		return decArgs;
 
 
 	}
 
 	public TAtributos rDecArgs1(){
 		regla("DecArgs:: VACIO");
 
 		TAtributos decArgs= atributosPara("decArgs", "etq", "etqh", "cod", "err", "numParams", "tsh", "idProch");
 	
 	
 		dependencias(decArgs.a("etq"), decArgs.a("etqh"));
 	
 	
 		calculo(decArgs.a("etq"), asignacion);
 		calculo(decArgs.a("cod"), devuelveVacio);
 		calculo(decArgs.a("numParams"), devuelve0);
 		calculo(decArgs.a("err"), devuelveVacio);
 	
 	
 		return decArgs;
 
 
 	}
 
 
 	public TAtributos rArgumentos0(TAtributos argumentos1, TAtributos argumento){
 		regla("Argumentos :: Argumentos , Argumento");
 
 		TAtributos argumentos0= atributosPara("argumentos0", "tsh", "etq", "etqh", "idProch", "listaParams", "listaParamsh","cod", "err", "numParams", "numParamsh");
 	
 	
 		dependencias(argumentos1.a("tsh"), argumentos0.a("tsh"));
 		dependencias(argumento.a("tsh"), argumentos0.a("tsh"));
 		dependencias(argumentos0.a("cod"), argumentos1.a("cod"), argumento.a("cod"));
 		dependencias(argumentos1.a("etqh"), argumentos0.a("etqh"));
 		dependencias(argumento.a("etqh"), argumentos1.a("etq"));
 		dependencias(argumentos0.a("etq"), argumento.a("etq"));
 		dependencias(argumentos1.a("idProch"), argumentos0.a("idProch"));
 		dependencias(argumento.a("idProch"), argumentos0.a("idProch"));
 		dependencias(argumentos1.a("listaParamsh"), argumentos0.a("listaParamsh"));
 		dependencias(argumento.a("listaParamsh"), argumentos1.a("listaParams"));
 		dependencias(argumentos0.a("listaParams"), argumento.a("listaParams"));
 		dependencias(argumentos1.a("numParamsh"), argumentos0.a("numParamsh"));
 		dependencias(argumento.a("numParamsh"), argumentos1.a("numParams"));
 		dependencias(argumentos0.a("numParams"), argumento.a("numParams"));
 		dependencias(argumentos0.a("err"), argumentos1.a("err"), argumento.a("err"));
 	
 	
 		calculo(argumentos1.a("tsh"), asignacion);
 		calculo(argumento.a("tsh"), asignacion);
 		calculo(argumentos0.a("cod"), codArgumentos0);
 		calculo(argumentos1.a("etqh"), asignacion);
 		calculo(argumento.a("etqh"), suma1);
 		calculo(argumentos0.a("etq"), asignacion);
 		calculo(argumentos1.a("idProch"), asignacion);
 		calculo(argumento.a("idProch"), asignacion);
 		calculo(argumentos1.a("listaParamsh"), asignacion);
 		calculo(argumento.a("listaParamsh"), asignacion);
 		calculo(argumentos0.a("listaParams"), asignacion);
 		calculo(argumentos1.a("numParamsh"), asignacion);
 		calculo(argumento.a("numParamsh"), asignacion);
 		calculo(argumentos0.a("numParams"), asignacion);
 		calculo(argumentos0.a("err"), concatena);
 	
 	
 	
 		return argumentos0;
 
 
 	}
 
 
 	public TAtributos rArgumentos1(TAtributos argumento){
 		regla("Argumentos :: Argumento");
 
 		TAtributos argumentos= atributosPara("argumentos", "tsh", "etq", "etqh", "idProch", "listaParams", "listaParamsh", "cod", "err", "numParams", "numParamsh");
 	
 	
 		dependencias(argumento.a("tsh"), argumentos.a("tsh"));
 		dependencias(argumento.a("etqh"), argumentos.a("etqh"));
 		dependencias(argumentos.a("etq"), argumento.a("etq"));
 		dependencias(argumento.a("idProch"), argumentos.a("idProch"));
 		dependencias(argumentos.a("cod"), argumento.a("cod"));
 		dependencias(argumento.a("listaParamsh"), argumentos.a("listaParamsh"));
 		dependencias(argumento.a("numParamsh"), argumentos.a("numParamsh"));
 		dependencias(argumentos.a("listaParams"), argumento.a("listaParams"));
 		dependencias(argumentos.a("numParams"), argumento.a("numParams"));
 		dependencias(argumentos.a("err"), argumento.a("err"));
 	
 	
 		calculo(argumento.a("tsh"), asignacion);
 		calculo(argumento.a("etqh"), suma4);
 		calculo(argumentos.a("etq"), asignacion);
 		calculo(argumento.a("idProch"), asignacion);
 		calculo(argumentos.a("cod"), codArgumentos1);
 		calculo(argumento.a("listaParamsh"), asignacion);
 		calculo(argumento.a("numParamsh"), asignacion);
 		calculo(argumentos.a("listaParams"), asignacion);
 		calculo(argumentos.a("numParams"), asignacion);
 		calculo(argumentos.a("err"), asignacion);
 	
 	
 	
 		return argumentos;
 
 
 	}
 
 
 	public TAtributos rArgumento(String identificador, TAtributos expr){
 		regla("Argumento :: identificador = Expr");
 
 		TAtributos argumento= atributosPara("argumento", "tsh", "etq", "etqh", "idProch", "listaParams", "listaParamsh",  "cod", "err", "numParams", "numParamsh");
 	
 		Atributo astring= atributoLexicoPara("identificador","lex", identificador);
 	
 	
 		dependencias(expr.a("tsh"), argumento.a("tsh"));
 		dependencias(expr.a("etqh"), argumento.a("etqh"));
 		dependencias(argumento.a("etq"), expr.a("etq"));
 		dependencias(argumento.a("cod"), argumento.a("tsh"), argumento.a("idProch"), astring, expr.a("cod"), expr.a("esDesignador"));
 		dependencias(argumento.a("err"), expr.a("err"), argumento.a("listaParamsh"), astring, argumento.a("idProch"), expr.a("tipo"), argumento.a("tsh"), expr.a("esDesignador"));
 		dependencias(argumento.a("listaParams"), argumento.a("listaParamsh"), astring);
 		dependencias(argumento.a("numParams"), argumento.a("numParamsh"));
 	
 	
 	
 		calculo(expr.a("tsh"), asignacion);
 		calculo(expr.a("etqh"), suma2);
 		calculo(argumento.a("etq"), suma1);
 		calculo(argumento.a("cod"), codArgumento);
 		calculo(argumento.a("err"), errArgumento);
 		calculo(argumento.a("listaParams"), anyadeParametro);
 		calculo(argumento.a("numParams"), suma1);
 	
 	
 		return argumento;
 
 
 	}
 
  
  
  
  
  public TAtributos rExpr(TAtributos nivel0) {
          regla("Expr:: Nivel0");
          
          TAtributos expr= atributosPara("expr", "tsh", "etqh", "etq", "cod", "err", "tipo", "esDesignador", "irvh", "irfh");
 
          dependencias(nivel0.a("tsh"), expr.a("tsh"));   
          dependencias(nivel0.a("etqh"), expr.a("etqh")); 
          dependencias(expr.a("etq"), nivel0.a("etq")); 
          dependencias(expr.a("cod"), nivel0.a("cod")); 
          dependencias(nivel0.a("irvh"), nivel0.a("etq"));
          dependencias(nivel0.a("irfh"), nivel0.a("etq")); 
          dependencias(expr.a("tipo"), nivel0.a("tipo")); 
          dependencias(expr.a("err"), nivel0.a("err")); 
          dependencias(expr.a("esDesignador"), nivel0.a("esDesignador"));
          
          calculo(nivel0.a("tsh"), asignacion);
          calculo(nivel0.a("etqh"),asignacion); 
          calculo(expr.a("etq"), asignacion); 
          calculo(expr.a("cod"), asignacion); 
          calculo(nivel0.a("irvh"), asignacion);
          calculo(nivel0.a("irfh"), asignacion); 
          calculo(expr.a("err"), asignacion);
          calculo(expr.a("tipo"), asignacion); 
          calculo(expr.a("esDesignador"), asignacion); 
          
          return expr;
  }
     
  public TAtributos rNivel00(TAtributos nivel10, TAtributos opNivel0, TAtributos nivel11) {
          regla("Nivel0:: Nivel1 Opnivel0 Nivel1");
          
          TAtributos nivel0= atributosPara("nivel0", "tsh", "etq", "etqh", "cod", "tipo", "err", "esDesignador", "irvh", "irfh");
 
          dependencias(nivel10.a("tsh"), nivel0.a("tsh"));
          dependencias(nivel11.a("tsh"), nivel0.a("tsh"));
          dependencias(nivel10.a("etqh"), nivel0.a("etqh"));
          dependencias(nivel11.a("etqh"), nivel10.a("etq"), nivel10.a("esDesignador"));
          dependencias(nivel0.a("etq"), nivel11.a("etq"), nivel11.a("esDesignador"));
          dependencias(nivel0.a("cod"), nivel10.a("cod"), nivel10.a("esDesignador"), nivel11.a("cod"), nivel11.a("esDesignador"), opNivel0.a("lex"));
          dependencias(nivel10.a("irvh"), nivel10.a("etq"), nivel10.a("esDesignador"));
          dependencias(nivel10.a("irfh"), nivel10.a("etq"), nivel10.a("esDesignador"));
          dependencias(nivel11.a("irvh"), nivel11.a("etq"), nivel11.a("esDesignador"));
          dependencias(nivel11.a("irfh"), nivel11.a("etq"), nivel11.a("esDesignador"));
          dependencias(nivel0.a("tipo"), nivel10.a("tipo"), nivel11.a("tipo"), opNivel0.a("lex"));
          dependencias(nivel0.a("err"), nivel10.a("err"), nivel11.a("err"), nivel0.a("tipo"));
 
       
          calculo(nivel10.a("tsh"), asignacion);       
          calculo(nivel11.a("tsh"), asignacion);       
          calculo(nivel10.a("etqh"), asignacion);
          calculo(nivel11.a("etqh"), suma1siDesignador);
          calculo(nivel0.a("etq"), suma12Designador);
          calculo(nivel0.a("cod"), codExpr);
          calculo(nivel10.a("irvh"), suma1siDesignador);
          calculo(nivel10.a("irfh"), suma1siDesignador);
          calculo(nivel11.a("irvh"), suma1siDesignador);
          calculo(nivel11.a("irfh"), suma1siDesignador);
          calculo(nivel0.a("tipo"), defineTipoNivel0);
          calculo(nivel0.a("err"), errNivelX);
          calculo(nivel0.a("esDesignador"), devuelveFalse);
  		      
          return nivel0;
  }   
 
  public TAtributos rNivel01(TAtributos nivel1) {
          regla("Nivel0:: Nivel1");
           
          TAtributos nivel0= atributosPara("nivel0", "tsh", "cod", "etq", "etqh", "irvh", "irfh", "err", "tipo", "esDesignador");
 
          dependencias(nivel1.a("tsh"), nivel0.a("tsh"));
          dependencias(nivel1.a("etqh"), nivel0.a("etqh"));
          dependencias(nivel0.a("etq"), nivel1.a("etq"));
          dependencias(nivel0.a("cod"), nivel1.a("cod"));
          dependencias(nivel1.a("irvh"), nivel0.a("irvh"));
          dependencias(nivel1.a("irfh"), nivel0.a("irfh"));
          dependencias(nivel0.a("tipo"), nivel1.a("tipo"));
          dependencias(nivel0.a("err"), nivel1.a("err"));
          dependencias(nivel0.a("esDesignador"), nivel1.a("esDesignador"));
 
          calculo(nivel1.a("tsh"), asignacion);           
          calculo(nivel1.a("etqh"), asignacion);
          calculo(nivel0.a("etq"), asignacion);
          calculo(nivel0.a("cod"), asignacion);
          calculo(nivel1.a("irvh"), asignacion);
          calculo(nivel1.a("irfh"), asignacion);
          calculo(nivel0.a("tipo"), asignacion);
          calculo(nivel0.a("err"), asignacion);
          calculo(nivel0.a("esDesignador"), asignacion);
  		      
          return nivel0;
 
  }
   
  public TAtributos rNivel10(TAtributos nivel11, TAtributos opNivel1, TAtributos nivel2) {
          regla("Nivel1:: Nivel1 Opnivel1 Nivel2");
          
          TAtributos nivel10= atributosPara("nivel10", "tsh", "cod", "etq", "etqh", "err", "tipo", "esDesignador", "irvh", "irfh");
 
          dependencias(nivel11.a("tsh"), nivel10.a("tsh"));
          dependencias(nivel2.a("tsh"), nivel10.a("tsh"));
          dependencias(nivel11.a("etqh"), nivel10.a("etqh"));
          dependencias(nivel2.a("etqh"), nivel11.a("etq"), nivel11.a("esDesignador"));
          dependencias(nivel10.a("etq"), nivel2.a("etq"), nivel2.a("esDesignador"));
          dependencias(nivel10.a("cod"), nivel11.a("cod"), nivel11.a("esDesignador"), nivel2.a("cod"), nivel2.a("esDesignador"), opNivel1.a("lex"));
          dependencias(nivel11.a("irvh"), nivel11.a("etq"), nivel11.a("esDesignador"));
          dependencias(nivel11.a("irfh"), nivel11.a("etq"), nivel11.a("esDesignador"));
          dependencias(nivel2.a("irvh"), nivel2.a("etq"), nivel2.a("esDesignador"));
          dependencias(nivel2.a("irfh"), nivel2.a("etq"), nivel2.a("esDesignador"));
          dependencias(nivel10.a("tipo"), nivel11.a("tipo"), nivel2.a("tipo"), opNivel1.a("lex"));
          dependencias(nivel10.a("err"), nivel11.a("err"), nivel2.a("err"), nivel10.a("tipo"));
        
          calculo(nivel11.a("tsh"), asignacion); 
          calculo(nivel2.a("tsh"), asignacion); 
          calculo(nivel11.a("etqh"), asignacion);
          calculo(nivel2.a("etqh"), suma1siDesignador);
          calculo(nivel10.a("etq"), suma12Designador);
          calculo(nivel10.a("cod"), codNivel10);
          calculo(nivel11.a("irvh"), suma1siDesignador);
          calculo(nivel11.a("irfh"), suma1siDesignador);
          calculo(nivel2.a("irvh"), suma1siDesignador);
          calculo(nivel2.a("irfh"), suma1siDesignador);
          calculo(nivel10.a("tipo"), defineTipoNivel10);
          calculo(nivel10.a("err"), errNivelX);
          calculo(nivel10.a("esDesignador"), devuelveFalse);
          
          return nivel10;
  }
 
 
  public TAtributos rNivel11(TAtributos nivel11, TAtributos nivel2) {
          
          regla("Nivel1:: Nivel1 or Nivel2");
          
          TAtributos nivel10= atributosPara("nivel10", "tsh", "etq", "etqh", "cod", "irvh", "irfh", "tipo", "err", "esDesignador");
 
          dependencias(nivel11.a("tsh"), nivel10.a("tsh"));
          dependencias(nivel2.a("tsh"), nivel10.a("tsh"));
          dependencias(nivel11.a("etqh"), nivel10.a("etqh"));
          dependencias(nivel2.a("etqh"), nivel11.a("etq"), nivel11.a("esDesignador"));
          dependencias(nivel10.a("etq"), nivel2.a("etq"), nivel2.a("esDesignador"));
          dependencias(nivel10.a("cod"), nivel11.a("cod"), nivel11.a("esDesignador"), nivel10.a("irvh"), nivel2.a("cod"), nivel2.a("esDesignador"));
          dependencias(nivel11.a("irvh"), nivel10.a("irvh"), nivel11.a("esDesignador"));
          dependencias(nivel11.a("irfh"), nivel11.a("etq"),  nivel11.a("esDesignador"));
          dependencias(nivel2.a("irvh"), nivel10.a("irvh"),  nivel2.a("esDesignador"));
          dependencias(nivel2.a("irfh"), nivel10.a("irfh"),  nivel2.a("esDesignador"));
          dependencias(nivel10.a("tipo"), nivel11.a("tipo"), nivel2.a("tipo"));
          dependencias(nivel10.a("err"), nivel11.a("err"), nivel2.a("err"), nivel10.a("tipo"));
        
          calculo(nivel11.a("tsh"), asignacion); 
          calculo(nivel2.a("tsh"), asignacion); 
          calculo(nivel11.a("etqh"), asignacion);
          calculo(nivel2.a("etqh"), suma34Designador);
          calculo(nivel10.a("etq"), suma1siDesignador);
          calculo(nivel10.a("cod"), codNivel11);
          calculo(nivel11.a("irvh"), suma1siDesignador);
          calculo(nivel11.a("irfh"), suma23Designador);
          calculo(nivel2.a("irvh"), suma1siDesignador);
          calculo(nivel2.a("irfh"), suma1siDesignador);
          calculo(nivel10.a("tipo"), defineTipoBoolean);
          calculo(nivel10.a("err"), errNivelX);
          calculo(nivel10.a("esDesignador"), devuelveFalse);
          
          return nivel10;
  }
  	
  public TAtributos rNivel12(TAtributos nivel2) {
          regla("Nivel1:: Nivel2");
          
          TAtributos nivel1= atributosPara("nivel1", "tsh", "etq", "etqh", "cod", "irvh", "irfh", "err", "tipo", "esDesignador");
 
          dependencias(nivel2.a("tsh"), nivel1.a("tsh"));       
          dependencias(nivel2.a("etqh"), nivel1.a("etqh"));   
          dependencias(nivel1.a("etq"), nivel2.a("etq"));   
          dependencias(nivel1.a("cod"), nivel2.a("cod"));   
          dependencias(nivel2.a("irvh"), nivel1.a("irvh"));   
          dependencias(nivel2.a("irfh"), nivel1.a("irfh")); 
          dependencias(nivel1.a("tipo"), nivel2.a("tipo"));   
          dependencias(nivel1.a("err"), nivel2.a("err"));   
          dependencias(nivel1.a("esDesignador"), nivel2.a("esDesignador"));   
 
          calculo(nivel2.a("tsh"), asignacion);     
          calculo(nivel2.a("etqh"), asignacion);   
          calculo(nivel1.a("etq"), asignacion);   
          calculo(nivel1.a("cod"), asignacion);   
          calculo(nivel2.a("irvh"), asignacion);   
          calculo(nivel2.a("irfh"), asignacion);   
          calculo(nivel1.a("tipo"), asignacion);   
          calculo(nivel1.a("err"), asignacion);   
          calculo(nivel1.a("esDesignador"), asignacion);   
          
          return nivel1;
  } 
  	
  public TAtributos rNivel20(TAtributos nivel21, TAtributos opNivel2, TAtributos nivel3) {
          regla("Nivel2:: Nivel2 Opnivel2 Nivel3");
          
          TAtributos nivel20= atributosPara("nivel20", "tsh", "etqh", "etq", "cod", "tipo", "err", "esDesignador", "irvh", "irfh");
          
          dependencias(nivel21.a("tsh"), nivel20.a("tsh"));  
          dependencias(nivel3.a("tsh"), nivel20.a("tsh"));  
          dependencias(nivel21.a("etqh"), nivel20.a("etqh"));  
          dependencias(nivel3.a("etqh"), nivel21.a("etq"), nivel21.a("esDesignador"));  
          dependencias(nivel20.a("etq"), nivel3.a("etq"), nivel3.a("esDesignador"));  
          dependencias(nivel20.a("cod"), nivel21.a("cod"), nivel21.a("esDesignador"), nivel3.a("cod"), nivel3.a("esDesignador"), opNivel2.a("lex"));  
          dependencias(nivel21.a("irvh"), nivel21.a("etq"), nivel21.a("esDesignador"));  
          dependencias(nivel21.a("irfh"), nivel21.a("etq"),  nivel21.a("esDesignador")); 
          dependencias(nivel3.a("irvh"), nivel3.a("etq"),  nivel3.a("esDesignador"));   
          dependencias(nivel3.a("irfh"), nivel3.a("etq"),  nivel3.a("esDesignador"));  
          dependencias(nivel20.a("tipo"), nivel21.a("tipo"), nivel3.a("tipo"), opNivel2.a("lex"));  
          dependencias(nivel20.a("err"), nivel21.a("err"), nivel3.a("err"), nivel20.a("tipo"));  
 
          calculo(nivel21.a("tsh"), asignacion);  
          calculo(nivel3.a("tsh"), asignacion);  
          calculo(nivel21.a("etqh"), asignacion);  
          calculo(nivel3.a("etqh"), suma1siDesignador);  
          calculo(nivel20.a("etq"), suma12Designador);  
          calculo(nivel20.a("cod"), codNivel20);  
          calculo(nivel21.a("irvh"), suma1siDesignador);  
          calculo(nivel21.a("irfh"), suma1siDesignador); 
          calculo(nivel3.a("irvh"), suma1siDesignador);   
          calculo(nivel3.a("irfh"), suma1siDesignador);  
          calculo(nivel20.a("tipo"), defineTipoNivel20);  
          calculo(nivel20.a("err"), errNivelX);  
          calculo(nivel20.a("esDesignador"), devuelveFalse);  
    
          return nivel20;
  }
  	
  public TAtributos rNivel21(TAtributos nivel21, TAtributos nivel3) {
          regla("Nivel2:: Nivel2 and Nivel3");
          
          TAtributos nivel20 = atributosPara("nivel20", "tsh", "etq", "etqh", "cod", "irfh", "irvh", "err", "tipo", "esDesignador");
 
          dependencias(nivel21.a("tsh"), nivel20.a("tsh"));
          dependencias(nivel3.a("tsh"), nivel20.a("tsh"));     
          dependencias(nivel21.a("etqh"), nivel20.a("etqh"));  
          dependencias(nivel3.a("etqh"), nivel21.a("etq"), nivel21.a("esDesignador"));  
          dependencias(nivel20.a("etq"), nivel3.a("etq"), nivel3.a("esDesignador"));
          dependencias(nivel20.a("cod"), nivel21.a("cod"), nivel21.a("esDesignador"), nivel20.a("irfh"), nivel3.a("cod"), nivel3.a("esDesignador"));  
          dependencias(nivel21.a("irvh"), nivel21.a("etq"), nivel21.a("esDesignador"));    
          dependencias(nivel21.a("irfh"), nivel20.a("irfh"), nivel21.a("esDesignador"));    
          dependencias(nivel3.a("irvh"), nivel20.a("irvh"), nivel3.a("esDesignador"));    
          dependencias(nivel3.a("irfh"), nivel20.a("irfh"), nivel3.a("esDesignador"));    
          dependencias(nivel20.a("tipo"), nivel21.a("tipo"), nivel3.a("tipo"));
          dependencias(nivel20.a("err"), nivel21.a("err"), nivel3.a("err"), nivel20.a("tipo"));  
          
          calculo(nivel21.a("tsh"), asignacion);       
          calculo(nivel3.a("tsh"), asignacion);        
          calculo(nivel21.a("etqh"), asignacion);  
          calculo(nivel3.a("etqh"), suma34Designador);  
          calculo(nivel20.a("etq"), suma1siDesignador);
          calculo(nivel20.a("cod"), codNivel21);  
          calculo(nivel21.a("irvh"), suma1siDesignador);    
          calculo(nivel21.a("irfh"), suma23Designador);    
          calculo(nivel3.a("irvh"), suma1siDesignador);    
          calculo(nivel3.a("irfh"), suma1siDesignador);   
          calculo(nivel20.a("tipo"), defineTipoBoolean);
          calculo(nivel20.a("err"), errNivelX);     
          calculo(nivel20.a("esDesignador"), devuelveFalse);   
  		      
          return nivel20;
  }   
 
 
  public TAtributos rNivel22(TAtributos nivel3) {
          regla("Nivel2:: Nivel3");
          
          TAtributos nivel2= atributosPara("nivel2", "tsh", "etq", "etqh", "cod", "irvh", "irfh", "tipo", "err", "esDesignador");
 
          dependencias(nivel3.a("tsh"), nivel2.a("tsh"));       
          dependencias(nivel3.a("etqh"), nivel2.a("etqh"));  
          dependencias(nivel2.a("etq"), nivel3.a("etq"));  
          dependencias(nivel2.a("cod"), nivel3.a("cod"));  
          dependencias(nivel3.a("irvh"), nivel2.a("irvh"));  
          dependencias(nivel3.a("irfh"), nivel2.a("irfh"));  
          dependencias(nivel2.a("tipo"), nivel3.a("tipo"));  
          dependencias(nivel2.a("err"), nivel3.a("err"));  
          dependencias(nivel2.a("esDesignador"), nivel3.a("esDesignador"));  
 
          calculo(nivel3.a("tsh"), asignacion);       
          calculo(nivel3.a("etqh"), asignacion);  
          calculo(nivel2.a("etq"), asignacion);  
          calculo(nivel2.a("cod"), asignacion);  
          calculo(nivel3.a("irvh"), asignacion);  
          calculo(nivel3.a("irfh"), asignacion);  
          calculo(nivel2.a("tipo"), asignacion);  
          calculo(nivel2.a("err"), asignacion);  
          calculo(nivel2.a("esDesignador"), asignacion);  
          
          return nivel2;
  } 
 
  public TAtributos rNivel30(TAtributos nivel4, TAtributos opNivel3, TAtributos nivel31) {
          regla("Nivel3:: Nivel4 Opnivel3 Nivel3");
          
          TAtributos nivel30= atributosPara("nivel30", "tsh", "etq", "etqh", "cod", "esDesignador", "irvh", "irfh", "tipo", "err");
 
          dependencias(nivel4.a("tsh"), nivel30.a("tsh"));     
          dependencias(nivel31.a("tsh"), nivel30.a("tsh"));
          dependencias(nivel4.a("etqh"), nivel30.a("etqh"));
          dependencias(nivel31.a("etqh"), nivel4.a("etq"), nivel4.a("esDesignador"));
          dependencias(nivel30.a("etq"), nivel31.a("etq"), nivel31.a("esDesignador"));
          dependencias(nivel30.a("cod"), nivel4.a("cod"), nivel4.a("esDesignador"), nivel31.a("cod"), nivel31.a("esDesignador"), opNivel3.a("lex"));
          dependencias(nivel4.a("irvh"), nivel4.a("etq"), nivel4.a("esDesignador"));
          dependencias(nivel4.a("irfh"), nivel4.a("etq"), nivel4.a("esDesignador"));
          dependencias(nivel31.a("irvh"), nivel31.a("etq"), nivel31.a("esDesignador"));
          dependencias(nivel31.a("irfh"), nivel31.a("etq"), nivel31.a("esDesignador"));
          dependencias(nivel30.a("tipo"), nivel4.a("tipo"), nivel31.a("tipo"), opNivel3.a("lex"));
          dependencias(nivel30.a("err"), nivel31.a("err"), nivel4.a("err"), nivel30.a("tipo"));
 
          calculo(nivel4.a("tsh"), asignacion);    
          calculo(nivel31.a("tsh"), asignacion);  
          calculo(nivel31.a("etqh"), suma1siDesignador);
          calculo(nivel4.a("etqh"), asignacion);
          calculo(nivel30.a("etq"), suma12Designador);
          calculo(nivel30.a("cod"), codNivel30);
          calculo(nivel4.a("irvh"), suma1siDesignador);
          calculo(nivel4.a("irfh"), suma1siDesignador);
          calculo(nivel31.a("irvh"), suma1siDesignador);
          calculo(nivel31.a("irfh"), suma1siDesignador);
          calculo(nivel30.a("tipo"), defineTipoNivel30);
          calculo(nivel30.a("err"), errNivelX);
          calculo(nivel30.a("esDesignador"), devuelveFalse);
 
          return nivel30;
  }
  	
     	
  public TAtributos rNivel31(TAtributos nivel4) {
          regla("Nivel3:: Nivel4");
          
          TAtributos nivel3 = atributosPara("nivel3", "tsh", "etq", "etqh", "cod", "irvh", "irfh", "tipo", "err", "esDesignador");
 
          dependencias(nivel4.a("tsh"), nivel3.a("tsh"));   
          dependencias(nivel4.a("etqh"), nivel3.a("etqh")); 
          dependencias(nivel3.a("etq"), nivel4.a("etq"));     
          dependencias(nivel3.a("cod"), nivel4.a("cod"));   
          dependencias(nivel4.a("irvh"), nivel3.a("irvh"));      
          dependencias(nivel4.a("irfh"), nivel3.a("irfh"));   
          dependencias(nivel3.a("tipo"), nivel4.a("tipo"));     
          dependencias(nivel3.a("err"), nivel4.a("err"));      
          dependencias(nivel3.a("esDesignador"), nivel4.a("esDesignador"));      
         
          calculo(nivel4.a("tsh"), asignacion);  
          calculo(nivel4.a("etqh"), asignacion); 
          calculo(nivel3.a("etq"), asignacion);     
          calculo(nivel3.a("cod"), asignacion);   
          calculo(nivel4.a("irvh"), asignacion);      
          calculo(nivel4.a("irfh"), asignacion);          
          calculo(nivel3.a("tipo"), asignacion);     
          calculo(nivel3.a("err"), asignacion);     
          calculo(nivel3.a("esDesignador"), asignacion);    
  		      
          return nivel3 ;
  }   
 
  public TAtributos rNivel40(TAtributos opNivel4, TAtributos operando) {
          regla("Nivel4:: Opnivel4 Operando");
          
          TAtributos nivel4= atributosPara("nivel4", "tsh", "etq", "etqh", "cod", "tipo", "err", "esDesignador", "irvh", "irfh");
 
          dependencias(operando.a("tsh"), nivel4.a("tsh"));
          dependencias(operando.a("etqh"), nivel4.a("etqh"));
          dependencias(nivel4.a("etq"), operando.a("etq"), operando.a("esDesignador"));
          dependencias(nivel4.a("cod"), operando.a("cod"), operando.a("esDesignador"), opNivel4.a("lex"));
          dependencias(operando.a("irvh"), operando.a("etq"), operando.a("esDesignador"));
          dependencias(operando.a("irfh"), operando.a("etq"), operando.a("esDesignador"));
          dependencias(nivel4.a("tipo"), operando.a("tipo"), opNivel4.a("lex"));
          dependencias(nivel4.a("err"), operando.a("err"), nivel4.a("tipo"));
            
          calculo(operando.a("tsh"), asignacion);
          calculo(operando.a("etqh"), asignacion);
          calculo(nivel4.a("etq"), suma12Designador);
          calculo(nivel4.a("cod"), codNivel40);
          calculo(operando.a("irvh"), suma1siDesignador);
          calculo(operando.a("irfh"), suma1siDesignador);
          calculo(nivel4.a("tipo"), defineTipoNivel4);
          calculo(nivel4.a("err"), errNivel4);
          calculo(nivel4.a("esDesignador"), devuelveFalse);
          
          return nivel4;
  }
   
  public TAtributos rNivel41(TAtributos  operando) {
          regla("Nivel4:: Operando");
          
          TAtributos nivel4= atributosPara("nivel4", "tsh", "etq", "etqh", "cod", "irvh", "irfh", "tipo", "err", "esDesignador");
 
          dependencias(operando.a("tsh"), nivel4.a("tsh"));
          dependencias(operando.a("etqh"), nivel4.a("etqh"));
          dependencias(nivel4.a("etq"), operando.a("etq"));
          dependencias(nivel4.a("cod"), operando.a("cod"));
          dependencias(operando.a("irvh"), nivel4.a("irvh"));
          dependencias(operando.a("irfh"), nivel4.a("irfh"));
          dependencias(nivel4.a("tipo"), operando.a("tipo"));
          dependencias(nivel4.a("err"), operando.a("err"));
          dependencias(nivel4.a("esDesignador"), operando.a("esDesignador"));
 
          calculo(operando.a("tsh"), asignacion);
          calculo(operando.a("etqh"), asignacion);
          calculo(nivel4.a("etq"), asignacion);
          calculo(nivel4.a("cod"), asignacion);      
          calculo(operando.a("irvh"), asignacion);
          calculo(operando.a("irfh"), asignacion);
          calculo(nivel4.a("tipo"), asignacion);
          calculo(nivel4.a("err"), asignacion);
          calculo(nivel4.a("esDesignador"), asignacion);
          
          return nivel4;
  }
 
 
  public TAtributos rOperando0(TAtributos expr) {
          
          regla("Operando :: ( Expr )");
          
          TAtributos operando = atributosPara("operando", "tsh", "etq", "etqh", "cod", "irvh", "irfh", "tipo", "err", "esDesignador");
 
          dependencias(expr.a("tsh"), operando.a("tsh"));
          dependencias(expr.a("etqh"), operando.a("etqh"));
          dependencias(operando.a("etq"), expr.a("etq"));
          dependencias(operando.a("cod"), expr.a("cod"));
          dependencias(expr.a("irvh"), operando.a("irvh"));
          dependencias(expr.a("irfh"), operando.a("irfh"));
          dependencias(operando.a("tipo"), expr.a("tipo"));
          dependencias(operando.a("err"), expr.a("err"));
          dependencias(operando.a("esDesignador"), expr.a("esDesignador"));
        
          calculo(expr.a("tsh"), asignacion);	
          calculo(expr.a("etqh"), asignacion);
          calculo(operando.a("etq"), asignacion);
          calculo(operando.a("cod"), asignacion);
          calculo(expr.a("irvh"), asignacion);
          calculo(expr.a("irfh"), asignacion);
          calculo(operando.a("tipo"), asignacion);
          calculo(operando.a("err"), asignacion);
          calculo(operando.a("esDesignador"), asignacion);
          
          return operando;
  }
  	  
  public TAtributos rOperando1(TAtributos designador) {
          regla("Operando:: Designador");
          
          TAtributos operando = atributosPara("operando", "tsh", "cod", "tipo", "err", "esDesignador", "irvh", "irfh", "etq", "etqh");
 
          dependencias(designador.a("tsh"), operando.a("tsh"));    
          dependencias(operando.a("cod"), designador.a("cod"));      
          dependencias(operando.a("tipo"), designador.a("tipo"));      
          dependencias(operando.a("err"), designador.a("err"));   
          dependencias(operando.a("esDesignador"), designador.a("esDesignador"));   
          dependencias(designador.a("etqh"), operando.a("etqh"));
          dependencias(operando.a("etq"), designador.a("etq"));
 
          
          calculo(designador.a("tsh"), asignacion);  
          calculo(operando.a("cod"), asignacion);  
          calculo(operando.a("tipo"), asignacion);          
          calculo(operando.a("err"), asignacion);   
          calculo(operando.a("esDesignador"), asignacion);  
          calculo(operando.a("etq"), asignacion);    
          calculo(designador.a("etqh"), asignacion); 
 
                  
          return operando;
  } 
     
  public TAtributos rOperando2(TAtributos valorTipoBasico) {
          regla("Operando:: ValorTipoBasico");
          TAtributos operando = atributosPara("operando", "tsh", "tipo", "err", "esDesignador", "etqh", "etq", "irvh", "irfh", "cod");
 
          dependencias(valorTipoBasico.a("tsh"), operando.a("tsh"));    
          dependencias(operando.a("tipo"), valorTipoBasico.a("tipo"));    
          dependencias(operando.a("esDesignador"), valorTipoBasico.a("esDesignador"));  
          dependencias(operando.a("cod"), valorTipoBasico.a("cod"));
          dependencias(valorTipoBasico.a("etqh"), operando.a("etqh"));
          dependencias(operando.a("etq"), valorTipoBasico.a("etq"));
          
          calculo(valorTipoBasico.a("tsh"), asignacion);
          calculo(operando.a("tipo"), asignacion);
          calculo(operando.a("err"), devuelveVacio);
          calculo(operando.a("esDesignador"), asignacion);
          calculo(operando.a("cod"), asignacion);
          calculo(operando.a("etq"), asignacion);
          calculo(valorTipoBasico.a("etqh"), asignacion);
          
          return operando;
  }
 
  public TAtributos rValorTipoBasico0(Object caracter) {
          regla("ValorTipoBasico:: caracter");  
           
          TAtributos valorTipoBasico = atributosPara("valorTipoBasico ", "tipo", "etq", "etqh", "cod", "valor", "tipo", "esDesignador", "tsh", "irvh", "irfh");
          
          Atributo astring= atributoLexicoPara("caracter","lex", caracter);
 
          dependencias(valorTipoBasico.a("valor"), astring);
          dependencias(valorTipoBasico.a("etq"), valorTipoBasico.a("etqh"));
          dependencias(valorTipoBasico.a("cod"), astring);
          
          calculo(valorTipoBasico.a("valor"), asignacion);     
          calculo(valorTipoBasico.a("etq"), suma1);
          calculo(valorTipoBasico.a("cod"), apila);
          calculo(valorTipoBasico.a("tipo"), creaTipoBasicoCharacter);
          calculo(valorTipoBasico.a("esDesignador"), devuelveFalse);
               
          return valorTipoBasico ;
 
  }
 
  public TAtributos rValorTipoBasico1(Object entero) {
          regla("ValorTipoBasico:: entero");
          
          TAtributos valorTipoBasico = atributosPara("valorTipoBasico ", "tipo", "etq", "etqh", "cod", "valor", "tipo", "esDesignador", "tsh", "irvh", "irfh");
          
          Atributo astring= atributoLexicoPara("entero","lex", entero);
 
          dependencias(valorTipoBasico.a("valor"), astring);
          dependencias(valorTipoBasico.a("etq"), valorTipoBasico.a("etqh"));
          dependencias(valorTipoBasico.a("cod"), astring);
          
          calculo(valorTipoBasico.a("valor"), asignacion);     
          calculo(valorTipoBasico.a("etq"), suma1);
          calculo(valorTipoBasico.a("cod"), apila);
          calculo(valorTipoBasico.a("tipo"), creaTipoBasicoInt);
          calculo(valorTipoBasico.a("esDesignador"), devuelveFalse);
               
          return valorTipoBasico ;
  }
 
  public TAtributos rValorTipoBasico2(Object naturales) {
          regla("ValorTipoBasico:: naturales");
          
          TAtributos valorTipoBasico = atributosPara("valorTipoBasico ", "tipo", "etq", "etqh", "cod", "valor", "tipo", "esDesignador", "tsh", "irvh", "irfh");
          
          Atributo astring= atributoLexicoPara("naturales","lex", naturales);
 
          dependencias(valorTipoBasico.a("valor"), astring);
          dependencias(valorTipoBasico.a("etq"), valorTipoBasico.a("etqh"));
          dependencias(valorTipoBasico.a("cod"), astring);
          
          calculo(valorTipoBasico.a("valor"), asignacion);     
          calculo(valorTipoBasico.a("etq"), suma1);
          calculo(valorTipoBasico.a("cod"), apila);
          calculo(valorTipoBasico.a("tipo"), creaTipoBasicoNat);
          calculo(valorTipoBasico.a("esDesignador"), devuelveFalse);
               
          return valorTipoBasico ;
  }
 
  public TAtributos rValorTipoBasico3(Object real) {
          regla("ValorTipoBasico:: real");
          
          TAtributos valorTipoBasico = atributosPara("valorTipoBasico ", "tipo", "etq", "etqh", "cod", "valor", "tipo", "esDesignador", "tsh", "irvh", "irfh");
          
          Atributo astring= atributoLexicoPara("real","lex", real);
 
          dependencias(valorTipoBasico.a("valor"), astring);
          dependencias(valorTipoBasico.a("etq"), valorTipoBasico.a("etqh"));
          dependencias(valorTipoBasico.a("cod"), astring);
          
          calculo(valorTipoBasico.a("valor"), asignacion);     
          calculo(valorTipoBasico.a("etq"), suma1);
          calculo(valorTipoBasico.a("cod"), apila);
          calculo(valorTipoBasico.a("tipo"), creaTipoBasicoFloat);
          calculo(valorTipoBasico.a("esDesignador"), devuelveFalse);
               
          return valorTipoBasico ;
  }
 
  public TAtributos rValorTipoBasico4(Object booleano) {
          regla("ValorTipoBasico:: booleano");
          
          TAtributos valorTipoBasico = atributosPara("valorTipoBasico ", "tipo", "etq", "etqh", "cod", "valor", "tipo", "esDesignador", "tsh", "irvh", "irfh");
          
          Atributo astring= atributoLexicoPara("booleano","lex", booleano);
 
          dependencias(valorTipoBasico.a("valor"), astring);
          dependencias(valorTipoBasico.a("etq"), valorTipoBasico.a("etqh"));
          dependencias(valorTipoBasico.a("cod"), astring);
           
          calculo(valorTipoBasico.a("valor"), asignacion);     
          calculo(valorTipoBasico.a("etq"), suma1);
          calculo(valorTipoBasico.a("cod"), apila);
          calculo(valorTipoBasico.a("tipo"), creaTipoBasicoBoolean);
          calculo(valorTipoBasico.a("esDesignador"), devuelveFalse);
               
          return valorTipoBasico ;
  }
 
  public TAtributos rOpNivel00() {
          regla("Opnivel0:: <");
          
          TAtributos opNivel0 = atributosPara("opNivel0", "lex");
 
          calculo(opNivel0.a("lex"), devuelveMenor);
               
          return opNivel0 ;
  }
 
  public TAtributos rOpNivel01() {
          regla("Opnivel0:: >");
          
          TAtributos opNivel0 = atributosPara("opNivel0", "lex");
 
          calculo(opNivel0.a("lex"), devuelveMayor);
               
          return opNivel0 ;
  }
 
  public TAtributos rOpNivel02() {
          regla("Opnivel0:: <=");
          
          TAtributos opNivel0 = atributosPara("opNivel0", "lex");
 
          calculo(opNivel0.a("lex"), devuelveMenorIgual);
               
          return opNivel0 ;
  }	
 
  public TAtributos rOpNivel03() {
          regla("Opnivel0:: >=");
          
          TAtributos opNivel0 = atributosPara("opNivel0", "lex");
 
          calculo(opNivel0.a("lex"), devuelveMayorIgual);
               
          return opNivel0 ;
  }
 
  public TAtributos rOpNivel04() {
          regla("Opnivel0:: ==");
          
          TAtributos opNivel0 = atributosPara("opNivel0", "lex");
 
          calculo(opNivel0.a("lex"), devuelveIgualIgual);
               
          return opNivel0 ;
  }
 
  public TAtributos rOpNivel05() {
          regla("Opnivel0:: !=");
        
          TAtributos opNivel0 = atributosPara("opNivel0", "lex");
 
          calculo(opNivel0.a("lex"), devuelveDistinto);
               
          return opNivel0 ;
  }
 
 
  public TAtributos rOpNivel10() {
          regla("Opnivel1:: +");
          
          TAtributos opNivel1= atributosPara("opNivel1", "lex");
 
          calculo(opNivel1.a("lex"), devuelveMas);
               
          return opNivel1;
  }
 
  public TAtributos rOpNivel11() {
          regla("Opnivel1:: -");
          
          TAtributos opNivel1= atributosPara("opNivel1", "lex");
 
          calculo(opNivel1.a("lex"), devuelveMenos);
               
          return opNivel1;
  }
 
  public TAtributos rOpNivel20() {
          regla("Opnivel2:: *");
          
          TAtributos opNivel2= atributosPara("opNivel2", "lex");
 
          calculo(opNivel2.a("lex"), devuelvePor);
               
          return opNivel2;
  }
 
  public TAtributos rOpNivel21() {
          regla("Opnivel2:: / ");
          
          TAtributos opNivel2= atributosPara("opNivel2", "lex");
 
          calculo(opNivel2.a("lex"), devuelveDiv);
               
          return opNivel2;
  }
 
  public TAtributos rOpNivel22() {
          regla("Opnivel2:: % ");
          
          TAtributos opNivel2= atributosPara("opNivel2", "lex");
 
          calculo(opNivel2.a("lex"), devuelveMod);
               
          return opNivel2;
  }
 
  public TAtributos rOpNivel30() {
          regla("Opnivel3:: << ");
          
          TAtributos opNivel3= atributosPara("opNivel3", "lex");
 
          calculo(opNivel3.a("lex"), devuelvedespDer);
               
          return opNivel3;
  }
 
  public TAtributos rOpNivel31() {
          regla("Opnivel3:: >> ");
          
          TAtributos opNivel3= atributosPara("opNivel3", "lex");
 
          calculo(opNivel3.a("lex"), devuelvedespIzq);
               
          return opNivel3;
  }
 
  public TAtributos rOpNivel40() {
          regla("Opnivel4:: (float) ");
          
          TAtributos opNivel4= atributosPara("opNivel4", "lex");
 
          calculo(opNivel4.a("lex"), devuelveCastFloat);
               
          return opNivel4;
  }
 
  public TAtributos rOpNivel41() {
          regla("Opnivel4:: (int) ");
          
          TAtributos opNivel4= atributosPara("opNivel4", "lex");
 
          calculo(opNivel4.a("lex"), devuelveCastInt);
               
          return opNivel4;
  }
 
  public TAtributos rOpNivel42() {
          regla("Opnivel4:: (char) ");
          
          TAtributos opNivel4= atributosPara("opNivel4", "lex");
 
          calculo(opNivel4.a("lex"), devuelveCastChar);
               
          return opNivel4;
  }
 
  public TAtributos rOpNivel43() {
          regla("Opnivel4:: (nat) ");
          
          TAtributos opNivel4= atributosPara("opNivel4", "lex");
 
          calculo(opNivel4.a("lex"), devuelveCastNat);
               
          return opNivel4;
  }
 
  public TAtributos rOpNivel44() {
          regla("Opnivel4:: not ");
          
          TAtributos opNivel4= atributosPara("opNivel4", "lex");
 
          calculo(opNivel4.a("lex"), devuelveNot);
               
          return opNivel4;
  }
 
  public TAtributos rOpNivel45() {
          regla("Opnivel4:: - ");
          
          TAtributos opNivel4= atributosPara("opNivel4", "lex");
 
          calculo(opNivel4.a("lex"), devuelveMenos);
               
          return opNivel4;
  }
 
  public TAtributos rDesignador0(String identificador) {
          regla("Designador :: identificador ");
          
          TAtributos designador= atributosPara("designador", "err", "esConstante", "tipo", "esDesignador", "etq", "etqh", "cod", "tsh");
 
          Atributo astring= atributoLexicoPara("identificador","tipo", identificador);
 
          dependencias(designador.a("err"), designador.a("tsh"), astring);
          dependencias(designador.a("esConstante"), designador.a("err"), designador.a("tsh"), astring);
          dependencias(designador.a("tipo"), designador.a("tsh"), astring);
          dependencias(designador.a("etq"), designador.a("tsh"),designador.a("etqh"),astring);
          dependencias(designador.a("cod"), designador.a("tsh"), astring);
 
          calculo(designador.a("err"), compruebaDesignador0);
          calculo(designador.a("esConstante"), compruebaEsConstante);
          calculo(designador.a("tipo"), dameTipo);
          calculo(designador.a("esDesignador"), devuelveTrue);
          calculo(designador.a("cod"), codDesignador0);
          calculo(designador.a("etq"), sumaLongAccesoVar);
          
          return designador;
  }
 
  public TAtributos rDesignador1(TAtributos designador1,TAtributos expr) {
          regla("Designador :: Designador [ Expr ]  ");
          
          TAtributos designador0= atributosPara("designador0", "tsh", "esConstante", "tipo", "cod", "esDesignador", "etq", "etqh", "err");
 
          dependencias(designador1.a("tsh"), designador0.a("tsh"));
          dependencias(expr.a("tsh"), designador0.a("tsh"));
 
          dependencias(designador0.a("err"), designador1.a("err"), designador1.a("tipo"), expr.a("tipo"));
          dependencias(designador0.a("tipo"), designador0.a("tsh"), designador1.a("tipo"));
          dependencias(designador0.a("cod"), designador1.a("cod"), expr.a("cod"), expr.a("esDesignador"), designador1.a("tipo"));
          dependencias(designador0.a("etq"), expr.a("etq"), expr.a("esDesignador"));
          dependencias(expr.a("etqh"), designador1.a("etq"));
          dependencias(designador1.a("etqh"), designador0.a("etqh"));
          dependencias(designador0.a("esConstante"), designador1.a("esConstante"));
 
          calculo(designador1.a("tsh"), asignacion);
          calculo(expr.a("tsh"), asignacion);
          calculo(designador0.a("tipo"), dameTipoBaseArray);
          calculo(designador0.a("err"), errDesignador1);
          calculo(designador0.a("esDesignador"), devuelveTrue);
          calculo(designador0.a("cod"), codDesignador1);
          calculo(designador0.a("etq"), suma34Designador);
          calculo(expr.a("etqh"), asignacion);
          calculo(designador1.a("etqh"), asignacion);
          calculo(designador0.a("esConstante"), asignacion);
 
          
          return designador0;
  }
 
  public TAtributos rDesignador2(TAtributos designador1, Object naturales) {
          regla("Designador :: Designador _ naturales ");
 
          Atributo astring= atributoLexicoPara("naturales","tipo", naturales);	        
     
         TAtributos designador0 = atributosPara("designador0", "tsh", "err", "tipo", "cod", "etq", "etqh", "esDesignador", "esConstante");
 
          dependencias(designador1.a("tsh"), designador0.a("tsh"));
          dependencias(designador0.a("err"), designador1.a("err"));
          dependencias(designador0.a("tipo"), designador0.a("tsh"), astring, designador1.a("tipo"));
          dependencias(designador0.a("err"), designador1.a("err"), designador1.a("tipo"), astring);
          dependencias(designador0.a("cod"), designador1.a("cod"), designador1.a("tipo"), astring);
          dependencias(designador1.a("etqh"), designador0.a("etqh"));
          dependencias(designador0.a("etq"), designador1.a("etq"));
 
          
          calculo(designador1.a("tsh"), asignacion);
          calculo(designador0.a("esConstante"), devuelveFalse);
          calculo(designador0.a("tipo"), dameTipoElemTupla);
          calculo(designador0.a("err"), errDesignador2);
          calculo(designador0.a("cod"), codDesignador2);
          calculo(designador1.a("etqh"), asignacion);
          calculo(designador0.a("etq"), suma2);
          calculo(designador0.a("esDesignador"), devuelveTrue);
 
          return designador0;
  }
 
     
     
 }
