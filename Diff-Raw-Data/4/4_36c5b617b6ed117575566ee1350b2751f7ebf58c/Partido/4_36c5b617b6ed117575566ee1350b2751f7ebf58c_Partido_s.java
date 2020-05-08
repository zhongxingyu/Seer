 package simumatch.match;
 
 import java.util.List;
 
 import simumatch.team.Equipo;
 import simumatch.common.*;
 
 public class Partido {
 	public Equipo local, visitante;
 	int aforoL, aforoV;
 	int animoL, animoV;
 	int tacL, tacV;
 	int marL=0; int marV=0;
 	int equilibrio;
 	Arbitro arbitro = new Arbitro();
 	int duracion = 10;//el numero de turnos que va a durar el partido
 	public Turno turno[] = new Turno[duracion+1];
 	int turnoActual = 0;
 	double[] lastAbanico = {0,0,0,0,0,0,0,0,0,0,0,0,0};
 	private Memento mementer= new Memento(this);
 	//List<Effect> activas=vacia();//Hay que llevar un contador con los turnos que les quedan, y ejecutarlas como AccTurno cada turno
 	
 	
 	/**
 	 * Metodos Publicos (solo os interesan estos 2)
 	 * */
 	public Partido(Equipo loc, Equipo vis){
 		local=loc;
 		visitante=vis;
 		arbitro = new Arbitro();
 		
 		aforoL = loc.aforoBase();
 		aforoV = vis.aforoBase();
 		animoL = loc.orgullo();
 		animoV = vis.orgullo();
 		
 		equilibrio = estadoEstable(loc,vis);
 		
 		if(equilibrio>=0){tacL=1;tacV=2;}
 		 else			 {tacL=2;tacV=1;}
 		
 		ejecuta(loc.getPreparatorias(), true);
 		ejecuta(vis.getPreparatorias(), false);
 		
 		aforoL = Math.min(loc.estadio.getAforoL(), aforoL);
 		aforoV = Math.min(loc.estadio.getAforoV(), aforoV);
 
 		loc.resetPreparatorias();
 		vis.resetPreparatorias();
 		
 		turno[0]= new Turno(equilibrio, this);
 		//el turno 0 nunca se muestra, solo se usa como base para en 1
 		
 		lastAbanico[estToPunt(equilibrio)]=1;
 		
 		recalculaTacticas();
 		recalculaAnimo();
 	}
 	public Turno turno(List<Effect> accLoc, List<Effect> accVis){
 		
 		if(turnoActual++>duracion){
 			System.out.println("El partido ya ha acabado");
 			return null;
 		}
 		
 		ejecuta(accLoc, true);
 		ejecuta(accVis, false);
 		//TODO ejecutaActivas(); 
 		
 		recalculaTacticas();
 		recalculaAnimo();
 
 		turno[turnoActual] = new Turno(generaTurno(calculaAbanico()), this);
 	
 		mementer.restaura();
 		
 		return turno[turnoActual];
 	}
 	
 	
 	/** 
 	 * Privados. ADVETENCIA: Su lectura puede producir daños neurologicos permanentes.
 	 * */
 	public static int estadoEstable(Equipo loc, Equipo vis) {
 		return(int)Math.round(Math.log(loc.nivel())-Math.log(vis.nivel()));
 	}
 	Turno actual(){
 		return turno[turnoActual];
 	}
 	private void ejecuta(List<Effect> acc, boolean loc) {
 		for(Effect a: acc) ejecutaEffect(a, loc, a.isPermanent());
 	}
 	private void ejecutaEffect(Effect a, boolean loc, boolean perm) {
 			if(a.getTarget()!=Target.OPPONENT)
 				if(perm)bonifPer(a.getScope(), a.getBonus(), a.getOperator(), loc);
 				else bonifTem(a.getScope(), a.getBonus(), a.getOperator(), loc);
 			if(a.getTarget()!=Target.SELF)
 				if(perm)bonifPer(a.getScope(), a.getBonus(), a.getOperator(), !loc);
 				else bonifTem(a.getScope(), a.getBonus(), a.getOperator(), !loc);
 	}
 	private void bonifPer(Scope scope, double bonus, Operator op, boolean loc) {
 		switch(scope){
 		case PEOPLE:
 			if(loc)aforoL= op.apply(aforoL, bonus);
 			else   aforoV= op.apply(aforoV, bonus);
 		break;
 		case TEAM_LEVEL:
 			equilibrio= op.apply(equilibrio, (loc?1:(-1))*bonus);
 		break;
 		case ATMOSPHERE:
 			if(loc)animoL= op.apply(animoL, bonus);
 			else   animoV= op.apply(animoV, bonus);
 		break;
 		default:
 			System.out.println("No implementadas acciones permanentes de tipo "+scope);
 		}
 		
 	}
 	private void bonifTem(Scope scope, double bonus, Operator op, boolean loc) {
 		this.mementer.bonifTemp(scope, bonus, op, loc);
 	}
 	int goles(){
 		return marL-marV;
 	}
 	private void recalculaAnimo() {
 		animoL+=goles();
 		animoV-=goles();
 		int estado = actual().estado;
 		if(estado>0){
 			animoL+=estado*local.indiceOptimismo();
 			animoV-=estado/visitante.indiceFrialdad();
 		}else
 		if(estado<0){
 			animoV+=estado*visitante.indiceOptimismo();
 			animoL-=estado/local.indiceFrialdad();
 		}
 		animoL=(2*animoL+aforoL)/3;
 		animoV=(2*animoV+aforoV)/3;
 	}
 	private void recalculaTacticas() {
 		int estado = actual().estado;
 		if(estado<0){
 			if(tacL==1 && Math.random()<local.versatilidad())tacL=2;
 			if(tacV==2 && Math.random()<visitante.versatilidad())tacV=1;
 		}
 		if(estado>0){
 			if(tacL==2 && Math.random()<local.versatilidad())tacL=1;
 			if(tacV==1 && Math.random()<visitante.versatilidad())tacV=2;
 		}
 	}
 	private int generaTurno(double[] abanico){
 		if(turnoActual==duracion/2)
 			return equilibrio;
 		int estado, i=0;
 		for (double tirada= Math.random(); tirada>0; i++)
 			if((tirada-=abanico[i]) <=0) break;
 		estado = Partido.puntToEst(i);
 		if(estado> 5)marL++;
 		if(estado<-5)marV++;
 		return estado;
 	}
 	private double[] calculaAbanico() {
 		double abanico[] = new double [13];
 		int eAnt, pAnt;
 		eAnt = (turnoActual>0)?turno[turnoActual-1].estado:equilibrio;
 		pAnt = puntToEst(eAnt);
 		double l = abanico.length;
 		for(int i=0; i<l; i++)abanico[i]=100;
 		
 		double indi_est[] = {6, 4.5, 3, 2.5, 1.5};
 		mul_adyacen(indi_est, pAnt, abanico);
 
 		if(eAnt>0){
 			if(tacL==1)bonif(pAnt, 13, local.indiceOfensivo(),abanico);
 			if(tacV==2)bonif(0, pAnt, 1/visitante.indiceDefensivo(),abanico);
 		}else
 		if(eAnt<0){
 			if(tacL==2)bonif(0, pAnt, 1/local.indiceDefensivo(),abanico);
 			if(tacV==1)bonif(pAnt, 13, visitante.indiceOfensivo(),abanico);
 		}
 		
 		double indi_equi[] = {4.5, 3, 2.5, 1.5};
 		mul_adyacen(indi_equi, estToPunt(equilibrio), abanico);
 	
 		
 		bonif(pAnt+1, 12, Math.max(0, animoL/animoV), abanico);
 		bonif( 0, pAnt-1, Math.max(0, animoV/animoL), abanico);
 		
 		
 		return lastAbanico=normalizar(abanico);
 	}
 	private void bonif(int origen, int destino, double mult, double aba[]){
 		if(origen<0)origen=0;
 		if(destino>12)destino=12;
 		for(int i=origen; i<=destino;i++ )
 			aba[i]*=mult;
 	}
 	private static double[] normalizar(double[] input) {
 		int len = input.length;
 		double output[] = new double[len];
 		double sum = 0;
 		for(int i=0; i<len; i++)
 			if(input[i]<0)System.out.println("Warning! franja negativa en el abanico");
 			else sum+=input[i++];
 		for(int i=0; i<len; i++)
 			output[i]=Math.max(input[i]/sum, 0);
 		return output;
 	}
 	private static void mul_adyacen(double indices[], int p, double abanico[]){
 		for(int i=0; i<indices.length; i++)
 			mul_adyacen(indices[i], p, i, abanico);
 	}
 	private static void mul_adyacen(double indice, int p, int g, double abanico[]) {
 		int ps[]= adyacentes(p,g);
 		for(int i=0; i<ps.length; i++)abanico[i]*=indice;
 	}
 	private static int[] adyacentes(int punto, int grado) {
 		int g = Math.abs(grado);
 		if(g>12)return null;
 		int[] r = new int[2];
 		int s=1;
		boolean b;
		if(b= punto-g < 0)r =new int[s--];
 		if(punto+g < 13)  r[s]=punto+g;
 		else r = new int[1];
 		if(!b) r[0]=punto-g;
 		return r;
 		//debuelve un vector con los DOS PUNTOS separados "grado" de "punto"
 		//no funciona bien para puntos de origen que no existen
 	}
 	static int puntToEst(int p){return p-6;}
 	static int estToPunt(int e){return e+6;}
 	private class Memento{
 		Partido p;
 		private double indOfL, indOfV ;
 		private double indDeL, indDeV;
 		int equilibrio;
 		int animoL, animoV;
 		boolean modificated = false;
 		boolean inited = false;
 	
 		void bonifTemp(Scope atrib, double bono, Operator op, boolean loc) {
 			switch (atrib){
 			case PEOPLE:
 				System.out.println("El partido ya esta en marcha, no se puede modificar el aforo");
 				break;
 			case TEAM_LEVEL:
 				init();
 				modificated=true;
 				p.equilibrio= op.apply(p.equilibrio, bono);
 				break;
 			case ATMOSPHERE:
 				init();
 				modificated=true;
 				if(loc)p.animoL =op.apply(p.animoL, bono);
 				else   p.animoV =op.apply(p.animoV, bono);
 				break;
 			case OFFENSIVE_SPIRIT:
 				init();
 				modificated=true;
 				if(loc)p.local.setIOf(op.apply(p.local.indiceOfensivo(), bono));
 				else p.visitante.setIOf(op.apply(p.visitante.indiceOfensivo(), bono));
 				break;
 			case DEFENSIVE_SPIRIT:
 				init();
 				modificated=true;
 				if(loc)p.local.setIDf(op.apply(p.local.indiceDefensivo(), bono));
 				else p.visitante.setIDf(op.apply(p.visitante.indiceDefensivo(), bono));
 				break;
 			default:
 				System.out.println("No implementadas acciones permanentes de tipo "+atrib);
 			}
 			
 		}
 		
 		Memento(Partido par){this.p= par;}
 	
 		void restaura(){
 			if(!modificated)return;
 			if(!inited)System.out.println("No se ha iniciado el turno antes de los cambios, se han perdido los datos originales");
 			p.animoL= animoL;
 			p.animoV= animoV;
 			p.equilibrio= equilibrio;
 			p.local.setIOf(indOfL);
 			p.local.setIDf(indDeL);
 			p.visitante.setIOf(indOfV);
 			p.visitante.setIDf(indDeV);
 			inited=false;
 			modificated=false;	
 		}
 		void init(){
 			if(inited)return;
 			if(modificated){
 				System.out.println("Se ha reiniciado el turno antes de restaurarlo, restauracion automatica");
 				restaura();
 			}	
 			animoL= p.animoL;
 			animoV= p.animoV;
 			equilibrio= p.equilibrio;
 			indOfL= local.indiceOfensivo();
 			indDeL= local.indiceDefensivo();
 			indOfV= visitante.indiceOfensivo();
 			indDeV= visitante.indiceDefensivo();
 			inited=true;
 			modificated=false;
 		}	
 	}//end of Memento
 	
 
 
 }//end of Partido
