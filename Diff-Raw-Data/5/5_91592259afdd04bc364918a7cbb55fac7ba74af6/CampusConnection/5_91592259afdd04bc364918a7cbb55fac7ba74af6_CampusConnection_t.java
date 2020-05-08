 /*
 	Copyright 2011-2012 Fundació per a la Universitat Oberta de Catalunya
 
 	This file is part of PeLP (Programming eLearning Plaform).
 
     PeLP is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     PeLP is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package edu.uoc.pelp.engine.campus.UOC;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.opentrends.remoteinterface.auth.Auth;
 import net.opentrends.remoteinterface.auth.SessionContext;
 
 import org.apache.log4j.Logger;
 
 import edu.uoc.pelp.engine.campus.Classroom;
 import edu.uoc.pelp.engine.campus.ICampusConnection;
 import edu.uoc.pelp.engine.campus.IClassroomID;
 import edu.uoc.pelp.engine.campus.ISubjectID;
 import edu.uoc.pelp.engine.campus.ITimePeriod;
 import edu.uoc.pelp.engine.campus.IUserID;
 import edu.uoc.pelp.engine.campus.Person;
 import edu.uoc.pelp.engine.campus.Subject;
 import edu.uoc.pelp.engine.campus.UserRoles;
 import edu.uoc.pelp.engine.campus.UOC.ws.WsLibBO;
 import edu.uoc.pelp.exception.AuthPelpException;
 import edu.uoc.pelp.model.dao.admin.AdministrationDAO;
 import edu.uoc.pelp.model.vo.admin.PelpMainLabSubjects;
 import edu.uoc.serveis.gat.dadesacademiques.model.AnyAcademicVO;
 import edu.uoc.serveis.gat.dadesacademiques.model.AssignaturaReduidaVO;
 import edu.uoc.serveis.gat.dadesacademiques.model.AssignaturaRelacionadaVO;
 import edu.uoc.serveis.gat.dadesacademiques.model.AssignaturaVO;
 import edu.uoc.serveis.gat.dadesacademiques.service.DadesAcademiquesService;
 import edu.uoc.serveis.gat.expedient.model.ExpedientVO;
 import edu.uoc.serveis.gat.expedient.service.ExpedientService;
 import edu.uoc.serveis.gat.matricula.model.AssignaturaMatriculadaDocenciaVO;
 import edu.uoc.serveis.gat.matricula.service.MatriculaService;
 import edu.uoc.serveis.gat.rac.model.AulaVO;
 import edu.uoc.serveis.gat.rac.model.ConsultorAulaVO;
 import edu.uoc.serveis.gat.rac.model.EstudiantAulaVO;
 import edu.uoc.serveis.gat.rac.service.RacService;
 import edu.uoc.serveis.tercers.tercer.model.TercerVO;
 import edu.uoc.serveis.tercers.tercer.service.TercerService;
 
 /**
  * Implements the campus access for the Universitat Oberta de Catalunya (UOC).
  * @author Xavier Baró
  */
 public class CampusConnection implements ICampusConnection{
 
 	private String sesion;
 	private UserID userID;
 	private String username;
 
 	private String aplicacioTren;
 	private String appIdTREN;
 	private String appId;
 
 	private ArrayList<AssignaturaMatriculadaDocenciaVO> asignaturasMatriculadas;
 	private ArrayList<AulaVO> asignaturasConsultor;
 	private ArrayList<AssignaturaReduidaVO> asignaturasPRA;
 
 	private static final Logger log = Logger.getLogger(CampusConnection.class);
 
 	public CampusConnection(String sesion) {
 		super();
 		this.sesion = sesion;
 	}
 
 	@Override
 	public boolean isUserAuthenticated() throws AuthPelpException {
 		boolean authenticated = false;
 		try {
 			Auth authService = WsLibBO.getAuthServiceInstance();
 			authenticated = authService.isUserAuthenticated( sesion );
 		} catch ( Exception e){
 			throw new AuthPelpException("Authentication process failed");
 		}        
 		return authenticated;
 	}
 
 	@Override
 	public IUserID getUserID() throws AuthPelpException {
 
 		if( userID == null ) {
 
 			try {
 				Auth authService = WsLibBO.getAuthServiceInstance();
 				final SessionContext sessionContext = authService.getContextBySessionId(sesion);
 				if ( sessionContext == null ) {
 					log.error("Error al obtener la SessionContext de la sesion: " + sesion);
 					throw new Exception("Error al obtener la SessionContext de la sesion: " + sesion);
 				}
 				userID = new UserID( String.valueOf(sessionContext.getIdp()) );
 				username = sessionContext.getUserLogin();
 				
 				appId = UserUtils.getAppId(sessionContext);
 				aplicacioTren = UserUtils.getAplicacioTren(appId);
 				appIdTREN = UserUtils.getAplicacioTren(appId);
 
 			} catch ( Exception e){
 				throw new AuthPelpException("Authentication process failed");
 			}
 		}
 		return userID;
 	}
 
 
 	public ISubjectID[] getUserSubjects(ITimePeriod timePeriod) throws AuthPelpException {
 
 		return getUserSubjects(null, timePeriod); 
 	}
 
 	@Override
 	public ISubjectID[] getUserSubjects(UserRoles userRole, ITimePeriod timePeriod) throws AuthPelpException {
 		ArrayList<SubjectID> subjects = new ArrayList<SubjectID>();
 
 		if( userRole == null || userRole.compareTo(UserRoles.Student) == 0 ){
 			if( asignaturasMatriculadas == null ){
				asignaturasMatriculadas = getListaAsignaturasMatriculadas( timePeriod, null );
 			}
 		} 
 		if(  userRole == null || userRole.compareTo(UserRoles.Teacher) == 0 ){
 			if( asignaturasConsultor == null ){
				asignaturasConsultor = getListaAsignaturasConsultor( timePeriod, null );
 			}
 		} 
 		if(  userRole == null || userRole.compareTo(UserRoles.MainTeacher) == 0 ){
 			if( asignaturasPRA == null ){
 				asignaturasPRA = getListaAsignaturasPRA( timePeriod, null );
 			}
 		}
 
 		ITimePeriod[] semestres;
 		if( timePeriod == null ){
 			semestres = getActivePeriods();
 		} else {
 			semestres = new ITimePeriod[1];
 			semestres[0] = timePeriod;
 		}
 
 		for (ITimePeriod iTimePeriod : semestres) {
 			Semester semester = (Semester) iTimePeriod;
 			// asignaturas matriculadas estudiante
 			for (AssignaturaMatriculadaDocenciaVO assignaturaMatriculadaDocencia : asignaturasMatriculadas) {
 				AssignaturaReduidaVO asignatura;
 				asignatura = (AssignaturaReduidaVO) assignaturaMatriculadaDocencia.getAssignatura();
 				if( assignaturaMatriculadaDocencia.getAnyAcademic().equalsIgnoreCase(semester.getID()) ){
 					SubjectID subID = new SubjectID(asignatura.getCodAssignatura(), semester);
 					subjects.add(subID);
 				}
 			}
 			// asignaturas consultores
 			for (AulaVO aula : asignaturasConsultor) {
 				if( aula.getAnyAcademic().equalsIgnoreCase(semester.getID()) ){
 					SubjectID subID = new SubjectID(aula.getAssignatura().getCodAssignatura(), semester);
 					subjects.add(subID);
 				}
 			}
 
 			// asignaturas PRA
 			for (AssignaturaReduidaVO asignatura : asignaturasPRA) {
 				SubjectID subID = new SubjectID(asignatura.getCodAssignatura(), semester);
 				subjects.add(subID);
 			}
 
 		}
 
 		SubjectID[] subs = new SubjectID[subjects.size()];
 		return subjects.toArray(subs); 
 	}
 
 
 
 	public IClassroomID[] getUserClassrooms(ISubjectID subjectID) throws AuthPelpException {
 
 		return getUserClassrooms(null, subjectID);
 	}
 
 	@Override
 	public IClassroomID[] getUserClassrooms(UserRoles userRole, ISubjectID subjectID) throws AuthPelpException {
 		return getUserClassrooms(userRole, subjectID, (UserID)getUserID());
 	}
 
 	public IClassroomID[] getUserClassrooms(UserRoles userRole, ISubjectID subjectID, UserID user) throws AuthPelpException {
 		ArrayList<ClassroomID> classrooms = new ArrayList<ClassroomID>();
 
 
 		if( userRole == null || userRole.compareTo(UserRoles.Student) == 0 ){
 			if( asignaturasMatriculadas == null ){
 				asignaturasMatriculadas = getListaAsignaturasMatriculadas( null, user );
 			}
 		} 
 		if(  userRole == null || userRole.compareTo(UserRoles.Teacher) == 0 ){
 			if( asignaturasConsultor == null ){
 				asignaturasConsultor = getListaAsignaturasConsultor( null, user );
 			}
 		} 
 		if(  userRole == null || userRole.compareTo(UserRoles.MainTeacher) == 0 ){
 			if( asignaturasPRA == null ){
 				asignaturasPRA = getListaAsignaturasPRA( null, user );
 			}
 		}
 		try {
 
 			ITimePeriod[] semestres = getActivePeriods();
 			boolean todasLasAsignaturas = (subjectID == null);
 			SubjectID subject = new SubjectID("", new Semester(""));
 			if( !todasLasAsignaturas ) {
 				subject = (SubjectID) subjectID;
 			} 
 			for (ITimePeriod iTimePeriod : semestres) {
 				Semester semester = (Semester) iTimePeriod;
 				ClassroomID classroom;
 
 				// asignaturas matriculadas estudiante
 				for (AssignaturaMatriculadaDocenciaVO assignaturaMatriculadaDocencia : asignaturasMatriculadas) {
 					if( todasLasAsignaturas || assignaturaMatriculadaDocencia.getAssignatura().getCodAssignatura().equalsIgnoreCase( subject.getCode() ) ){
 						classroom = new ClassroomID(subject, assignaturaMatriculadaDocencia.getNumAula() );
 						classrooms.add(classroom);
 					}
 				}
 
 				// asignaturas consultores
 				for (AulaVO aula : asignaturasConsultor) {					
 					if( todasLasAsignaturas || aula.getAssignatura().getCodAssignatura().equalsIgnoreCase( subject.getCode()  ) ){
 						SubjectID subTmp = new SubjectID(aula.getAssignatura().getCodAssignatura(),  semester);
 						classroom = new ClassroomID(subTmp, aula.getNumAula() );
 						classrooms.add(classroom);
 					}
 				}
 
 
 				// asignaturas PRA
 				for (AssignaturaReduidaVO asignatura : asignaturasPRA) {
 					if( todasLasAsignaturas || asignatura.getCodAssignatura().equalsIgnoreCase( subject.getCode() )){
 
 						RacService racService = WsLibBO.getRacServiceInstance();
 						AulaVO[] aulas = racService.getAulesByAssignaturaAny(asignatura.getCodAssignatura(), semester.getID());
 						SubjectID subTmp = new SubjectID(asignatura.getCodAssignatura(), semester);
 						for (AulaVO aula : aulas) {
 							classroom = new ClassroomID(subTmp, aula.getNumAula() );
 							classrooms.add(classroom);
 						}
 					}
 				}
 			}
 
 		} catch (Exception e) {
 			log.error("Error al obtener el listado de aulas");
 			e.printStackTrace();
 			throw new AuthPelpException("Error al obtener el listado de aulas.");   
 		}
 		ClassroomID[] classroomsArray = new ClassroomID[classrooms.size()];
 		return classrooms.toArray(classroomsArray); 
 	}
 
 	@Override
 	public IClassroomID[] getSubjectClassrooms(ISubjectID subject, UserRoles userRole) throws AuthPelpException {
 		return getUserClassrooms(userRole, subject);
 	}
 
 	@Override
 	public boolean isRole(UserRoles role, ISubjectID subject, IUserID user) throws AuthPelpException {
 		boolean isRole = false;
 		SubjectID subjectID = (SubjectID) subject;		
 		ITimePeriod periodo = subjectID.getSemester();
 		UserID userID = (UserID) user;
 
 		if( role.compareTo(UserRoles.Student) == 0 ){
 			ArrayList<AssignaturaMatriculadaDocenciaVO> asignaturasMatriculadas = getListaAsignaturasMatriculadas( periodo, userID );
 			for (AssignaturaMatriculadaDocenciaVO asignaturaMatriculada : asignaturasMatriculadas) {
 				if( asignaturaMatriculada.getAssignatura().getCodAssignatura() == subjectID.getCode() ){
 					isRole = true;
 				}
 			}
 
 		} else if( role.compareTo(UserRoles.Teacher) == 0 ){
 			ArrayList<AulaVO> asignaturasConsultor = getListaAsignaturasConsultor( periodo, userID );
 			for (AulaVO aulaVO : asignaturasConsultor) {
 				if( aulaVO.getAssignatura().getCodAssignatura() == subjectID.getCode() ){
 					isRole = true;
 				}
 			}
 		} else if( role.compareTo(UserRoles.MainTeacher) == 0 ){
 			ArrayList<AssignaturaReduidaVO> asignaturasPRA = getListaAsignaturasPRA( periodo, userID );
 			for (AssignaturaReduidaVO asignatura : asignaturasPRA) {
 				if( asignatura.getCodAssignatura() == subjectID.getCode()){
 					isRole = true;
 				}
 			}
 		}
 
 		return isRole;	
 	}
 
 	@Override
 	public boolean isRole(UserRoles role, ISubjectID subject) throws AuthPelpException {
 		return isRole(role, subject, getUserID());
 	}
 
 	@Override
 	public boolean isRole(UserRoles role, IClassroomID classroom, IUserID user) throws AuthPelpException {
 
 		boolean encontrado = false;
 		IClassroomID[] classrooms = getUserClassrooms(role, null, (UserID)user);
 		for (IClassroomID iclassroomID : classrooms) {
 			ClassroomID classroomID = (ClassroomID) iclassroomID;
 			if( classroomID.compareTo(classroom) == 0){
 				encontrado = true;
 				break;
 			}
 		}
 		return encontrado;
 	}
 
 	@Override
 	public boolean isRole(UserRoles role, IClassroomID classroom) throws AuthPelpException {
 		return isRole(role, classroom, getUserID());
 	}
 
 	@Override
 	public IUserID[] getRolePersons(UserRoles userRole, ISubjectID subject) throws AuthPelpException {
 
 		List<IUserID> personas = new ArrayList<IUserID>();
 		try {
 			SubjectID subjectID = (SubjectID) subject;
 			ITimePeriod periodo = subjectID.getSemester();
 			Semester semestre = (Semester) periodo;
 
 			if( userRole.compareTo(UserRoles.Student) == 0 ){
 				RacService rac = WsLibBO.getRacServiceInstance();
 				EstudiantAulaVO[]  estudiantes = rac.getEstudiantsByAnyAssignaturaMask(semestre.getID(), subjectID.getCode(), "");
 				for (EstudiantAulaVO estudiante : estudiantes) {
 					UserID userID = new UserID( String.valueOf( estudiante.getTercer().getIdp() ) );
 					personas.add(userID);
 				}
 			}
 
 			if( userRole.compareTo(UserRoles.Teacher) == 0 ){
 				RacService rac = WsLibBO.getRacServiceInstance();
 				AulaVO[] aulas = rac.getAulesByAssignaturaAny(semestre.getID(), subjectID.getCode() );
 				for (AulaVO aulaVO : aulas) {
 					ConsultorAulaVO[]  consultores = aulaVO.getConsultors();
 					for (ConsultorAulaVO consultorAulaVO : consultores) {
 						UserID userID = new UserID( String.valueOf( consultorAulaVO.getTercer().getIdp() ) );
 						personas.add(userID);
 					}
 				}
 			}
 
 			if( userRole.compareTo(UserRoles.MainTeacher) == 0 ){
 				// TODO: obtener el listado de PRAs de una asignatura
 				// Ni los servicios de GAT ni OKI permiten obtener estos datos sin tener el domainId del aula
 				throw new UnsupportedOperationException("Not supported yet.");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new AuthPelpException("No se han podido obtener los usuarios de la asignatura");
 		}
 
 		return personas.toArray( new IUserID[personas.size()] );
 	}
 
 	@Override
 	public IUserID[] getRolePersons(UserRoles role, IClassroomID classroomID) throws AuthPelpException {
 
 		List<IUserID> personas = new ArrayList<IUserID>();
 		try {
 			ClassroomID classroom = (ClassroomID) classroomID;
 			SubjectID subjectID = (SubjectID) classroom.getSubject();
 			ITimePeriod periodo = subjectID.getSemester();
 			Semester semestre = (Semester) periodo;
 
 			if( role.compareTo(UserRoles.Student) == 0 ){
 				RacService rac = WsLibBO.getRacServiceInstance();
 				EstudiantAulaVO[]  estudiantes = rac.getEstudiantsByAula(semestre.getID(), subjectID.getCode(), classroom.getClassIdx());
 				for (EstudiantAulaVO estudiante : estudiantes) {
 					UserID userID = new UserID( String.valueOf( estudiante.getTercer().getIdp() ) );
 					personas.add(userID);
 				}
 			}
 
 			if( role.compareTo(UserRoles.Teacher) == 0 ){
 				RacService rac = WsLibBO.getRacServiceInstance();
 				AulaVO[] aulas = rac.getAulesByAssignaturaAny(semestre.getID(), subjectID.getCode() );
 				for (AulaVO aulaVO : aulas) {
 					if(aulaVO.getNumAula() == classroom.getClassIdx() ){
 						ConsultorAulaVO[]  consultores = aulaVO.getConsultors();					
 						for (ConsultorAulaVO consultorAulaVO : consultores) {
 							UserID userID = new UserID( String.valueOf( consultorAulaVO.getTercer().getIdp() ) );
 							personas.add(userID);
 						}
 					}
 				}
 			}
 
 			if( role.compareTo(UserRoles.MainTeacher) == 0 ){
 				// TODO: obtener el listado de PRAs de una aula
 				// Ni los servicios de GAT ni OKI permiten obtener estos datos sin tener el domainId del aula
 				throw new UnsupportedOperationException("Not supported yet.");
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new AuthPelpException("No se han podido obtener los usuarios de la asignatura");
 		}
 
 		return personas.toArray( new IUserID[personas.size()] );
 	}
 
 	@Override
 	public boolean hasLabSubjects(ISubjectID subject) throws AuthPelpException {
 		return getLabSubjects(subject).length > 0;
 	}
 
 	@Override
 	public ISubjectID[] getLabSubjects(ISubjectID subject) throws AuthPelpException {
 		AdministrationDAO dao = new AdministrationDAO();
 		SubjectID subjectID = (SubjectID) subject;
 		List<PelpMainLabSubjects> lista = dao.getLabSubjectOfMain(subjectID.getCode());
 		ArrayList<SubjectID> subjects = new ArrayList<SubjectID>();
 		for (PelpMainLabSubjects pelpMainLabSubject : lista) {
 			String code = pelpMainLabSubject.getPelpMainLabSubjectsPK().getLabSubjectCode();
 			subjects.add( new SubjectID(code, subjectID.getSemester()) );			
 		}		
 		SubjectID[] subs = new SubjectID[subjects.size()];
 		return subs;
 	}
 
 	@Override
 	public boolean hasEquivalentSubjects(ISubjectID subject) throws AuthPelpException {
 		return getEquivalentSubjects(subject).length > 0;
 	}
 
 	@Override
 	public ISubjectID[] getEquivalentSubjects(ISubjectID subject) throws AuthPelpException {
 		List<SubjectID> lista = new ArrayList<SubjectID>();
 		String TIPO_EQUIVALENTE = "E";
 		String TIPO_COMPARTIDA = "C";
 		try {
 			DadesAcademiquesService dades = WsLibBO.getDadesAcademiquesServiceInstance();
 			SubjectID subjectID = (SubjectID) subject;
 			AssignaturaRelacionadaVO[] asignaturasRelacionadas = dades.getAssignaturesRelacionades(0, subjectID.getCode(), subjectID.getSemester().getID(), UserUtils.getCampusLanguage(appId) );
 			for(int i = 0; i < asignaturasRelacionadas.length; i++){
 				AssignaturaRelacionadaVO asignatura = asignaturasRelacionadas[i];
 				String tipo = asignatura.getTipusRelacio();
 				if(tipo == null) throw new AuthPelpException("AsignaturaRelacionada con codigo de tipo de relacion nulo; datos: " + subjectID.getCode() + " " + subjectID.getSemester().getID() + " " + UserUtils.getCampusLanguage(appId));
 				if( tipo.equals(TIPO_EQUIVALENTE) || tipo.equals( TIPO_COMPARTIDA	) ){
 					SubjectID subjectAux = new SubjectID(asignatura.getCodi(), subjectID.getSemester());
 					lista.add(subjectAux);
 				}
 			}
 		} catch (Exception e) {			
 			e.printStackTrace();
 			throw new AuthPelpException("Error to recover equivalent subjects");
 		}
 
 		SubjectID[] SubjectArray = new SubjectID[lista.size()];
 		return lista.toArray(SubjectArray);
 
 	}
 
 	@Override
 	public boolean isCampusConnection() {
 		return true;
 	}
 
 	@Override
 	public Subject getSubjectData(ISubjectID isubjectID) throws AuthPelpException {
 		Subject subject;
 		try {
 			DadesAcademiquesService dades = WsLibBO.getDadesAcademiquesServiceInstance();
 			SubjectID subjectID = (SubjectID) isubjectID;
 			AssignaturaVO asignatura = dades.getAssignaturaByCodi( subjectID.getCode() );
 			subject = new Subject( subjectID );
 
 			// Descripciones
 			getUserID();
 			String idioma = UserUtils.getCampusLanguage(appIdTREN);
 			subject.setDescription( Utils.getLanguageTitle(asignatura.getDescLlarga(), idioma) );
 			subject.setShortName( Utils.getLanguageTitle(asignatura.getDescLlarga(), idioma) );
 
 			RacService rac = WsLibBO.getRacServiceInstance();
 
 			AulaVO[] aulas = rac.getAulesByAssignaturaAny(subjectID.getCode(), subjectID.getSemester().getID());
 			// Aulas
 			for (AulaVO aula : aulas) {
 				// Se añaden las aulas
 				Classroom classroom = new Classroom(new ClassroomID(subjectID, aula.getNumAula().intValue()));
 				subject.addClassroom(classroom);
 				// Se añaden los consultores de todas las aulas
 				ConsultorAulaVO[] consultores = aula.getConsultors();
 				// Consultores
 				for (ConsultorAulaVO consultorAulaVO : consultores) {
 					Person teacher = new Person(new UserID( String.valueOf( consultorAulaVO.getTercer().getIdp())));
 					subject.addMainTeacher(teacher);	
 				}
 			}
 
 
 			// Asignaturas equivalentes
 			ISubjectID[] equivalentSubjects = getEquivalentSubjects(subjectID);
 			for (ISubjectID iSubjectID2 : equivalentSubjects) {
 				subject.addEquivalentSubject( iSubjectID2 );	
 			}
 
 		
 			
 			// Laboratorio
 			boolean isLab = false;
 
 			AdministrationDAO dao = new AdministrationDAO();
 			List<PelpMainLabSubjects> lista = dao.getMainSubjectOfLab(subjectID.getCode());
 			for (PelpMainLabSubjects pelpMainLabSubject : lista) {
 				isLab = true;
 				String code = pelpMainLabSubject.getPelpMainLabSubjectsPK().getLabSubjectCode();
 				subject.setParent( new SubjectID(code, subjectID.getSemester()) );
 			}		
 
 			subject.setLabFlag(isLab);
 
 
 			ISubjectID[] labSubjects = getLabSubjects(subjectID);
 			for (ISubjectID iSubjectID2 : labSubjects) {
 				subject.addLaboratory(iSubjectID2);
 			}
 
 
 
 		} catch (Exception e) {			
 			e.printStackTrace();
 			throw new AuthPelpException("Error to recover subject data");
 		}
 		return subject;
 	}
 
 	@Override
 	public Classroom getClassroomData(IClassroomID iclassroomID) throws AuthPelpException {
 
 		Classroom classroom;
 		try {
 			ClassroomID classroomID = (ClassroomID) iclassroomID;
 			classroom = new Classroom(classroomID);
 
 			RacService rac = WsLibBO.getRacServiceInstance();		
 			AulaVO[] aulas = rac.getAulesByAssignaturaAny(classroomID.getSubject().getCode(), classroomID.getSubject().getSemester().getID());
 			// Aulas
 			for (AulaVO aula : aulas) {
 				if(aula.getNumAula() == classroomID.getClassIdx() ){
 					ConsultorAulaVO[] consultores = aula.getConsultors();
 					// Consultores
 					for (ConsultorAulaVO consultorAulaVO : consultores) {
 						Person teacher = new Person(new UserID( String.valueOf( consultorAulaVO.getTercer().getIdp())));
 						classroom.addTeacher(teacher);
 					}
 				}
 			}
 
 			Subject subject = new Subject(classroomID.getSubject());
 			classroom.setSubjectRef(subject);		
 
 			EstudiantAulaVO[] estudiantes = rac.getEstudiantsByAula(classroomID.getSubject().getSemester().getID(), classroomID.getSubject().getCode(), classroomID.getClassIdx());
 			for (EstudiantAulaVO estudiante : estudiantes) {
 				Person student = new Person(new UserID( String.valueOf( estudiante.getTercer().getIdp())));
 				classroom.addStudent(student);
 			}
 
 		}  catch (Exception e) {
 			e.printStackTrace();
 			throw new AuthPelpException("Error to recover classroom data");
 		}
 
 		return classroom;
 	}
 
 
 	@Override
 	public Person getUserData(IUserID userID) throws AuthPelpException {
 		if( userID == null ) {
 			userID = getUserID();
 		}
 		Person person;
 		try {
 			person = new Person(userID);
 
 			UserID user = (UserID) userID;
 			TercerService tercerService = WsLibBO.getTercerServiceInstance();
 			TercerVO tercer = tercerService.getTercer(Integer.valueOf(user.idp));
 			person.setName( tercer.getNombre() );
 			String fullname = tercer.getNombre();
 			if( tercer.getPrimerApellido() != null ) fullname += " " + tercer.getPrimerApellido();
 			if( tercer.getSegundoApellido() != null ) fullname += " " + tercer.getSegundoApellido();
 			person.setFullName( fullname );
 			person.setLanguage( UserUtils.getCampusLanguage(appIdTREN) );			
 			person.setUserPhoto( "http://cv.uoc.edu/UOC/mc-icons/fotos/" + username + ".jpg" );
 			person.setUsername( username );
 			person.seteMail( username + "@uoc.edu");
 			
 		}  catch (Exception e) {
 			e.printStackTrace();
 			throw new AuthPelpException("No se ha podido recuperar la informacion del usuario");
 		}
 
 		return person;
 	}
 
 	@Override
 	public Person getUserData() throws AuthPelpException {
 		return getUserData( null ); 
 	}
 
 
 
 	@Override
 	public ITimePeriod[] getPeriods() {
 		String MODUL = "NOTESAVAL0";
 		AnyAcademicVO[] anysAcademics;
 		ArrayList<Semester> semestres =  new ArrayList<Semester>();
 		try {
 			DadesAcademiquesService dades = WsLibBO.getDadesAcademiquesServiceInstance();
 			anysAcademics = dades.getAnysAcademicsCalendari(appIdTREN, aplicacioTren, MODUL);
 			for (AnyAcademicVO anyAcademic : anysAcademics) {
 				semestres.add( new Semester(anyAcademic.getAnyAcademic(), anyAcademic.getDataInici(), anyAcademic.getDataFinal()) );			
 			}
 		}  catch (Exception e) {
 			log.error("Error al obtener la lista de calendarios abiertos");
 			e.printStackTrace();			
 		}
 
 		Semester[] sems = new Semester[ semestres.size() ];
 		semestres.toArray(sems);
 		return sems;
 	}
 
 	@Override
 	public ITimePeriod[] getActivePeriods() {
 		return getPeriods();
 	}
 
 	private ArrayList<AssignaturaMatriculadaDocenciaVO> getListaAsignaturasMatriculadas(ITimePeriod timePeriod, UserID userID) throws AuthPelpException {
 
 		asignaturasMatriculadas = new ArrayList<AssignaturaMatriculadaDocenciaVO>();
 
 		try {
 			if( userID == null ) {
 				userID = (UserID) getUserID();
 			}
 			int idp = Integer.valueOf( userID.idp );
 
 			ExpedientService expedientService = WsLibBO.getExpedientServiceInstance();
 			ExpedientVO[] expedientes = expedientService.getExpedientsByEstudiant( idp );
 			MatriculaService matriculaService = WsLibBO.getMatriculaServiceInstance();
 
 			ITimePeriod[] semestres;
 			if( timePeriod == null ){
 				semestres = getActivePeriods();
 			} else {
 				semestres = new ITimePeriod[1];
 				semestres[0] = timePeriod;
 			}
 
 			for (ITimePeriod iTimePeriod : semestres) {
 				Semester semester = (Semester) iTimePeriod;
 				String semesterId = semester.getID();
 				for (ExpedientVO expedient : expedientes) {
 					AssignaturaMatriculadaDocenciaVO[] asignaturas = matriculaService.getAssignaturesDocenciaMatriculadesEstudiant(expedient.getNumExpedient(), semesterId);
 					for (AssignaturaMatriculadaDocenciaVO assignaturaMatriculadaDocencia : asignaturas) {
 
 						asignaturasMatriculadas.add( assignaturaMatriculadaDocencia );
 
 					}
 				}
 			}
 
 		} catch (Exception e) {
 			log.error("Error al obtener el listado de asignaturas del estudiante");
 			e.printStackTrace();
 			throw new AuthPelpException("Error al obtener el listado de asignaturas del estudiante.");            
 		}
 		return asignaturasMatriculadas;
 	}
 
 	private ArrayList<AulaVO> getListaAsignaturasConsultor(ITimePeriod timePeriod, UserID userID) throws AuthPelpException {
 
 		asignaturasConsultor = new ArrayList<AulaVO>();
 		try {
 			if( userID == null ) {
 				userID = (UserID) getUserID();
 			}
 			int idp = Integer.valueOf( userID.idp );
 			RacService rac = WsLibBO.getRacServiceInstance();
 
 			ITimePeriod[] semestres;
 			if( timePeriod == null ){
 				semestres = getActivePeriods();
 			} else {
 				semestres = new ITimePeriod[1];
 				semestres[0] = timePeriod;
 			}
 
 			for (ITimePeriod iTimePeriod : semestres) {
 				Semester semester = (Semester) iTimePeriod;
 				String semesterId = semester.getID();
 				AulaVO[] asignaturas = rac.getAulesByConsultorAny(idp, semesterId);
 				for (AulaVO aula : asignaturas) {
 					asignaturasConsultor.add( aula );
 				}
 			}
 		} catch (Exception e) {
 			log.error("Error al obtener el listado de asignaturas del consultor.");
 			e.printStackTrace();
 			throw new AuthPelpException("Error al obtener el listado de asignaturas del consultor.");   
 		}
 
 		return asignaturasConsultor;
 	}
 
 	private ArrayList<AssignaturaReduidaVO> getListaAsignaturasPRA(ITimePeriod timePeriod, UserID userID) throws AuthPelpException {
 
 		asignaturasPRA = new ArrayList<AssignaturaReduidaVO>();
 		try {
 			if( userID == null ) {
 				userID = (UserID) getUserID();
 			}
 			int idp = Integer.valueOf( userID.idp );
 			DadesAcademiquesService dades = WsLibBO.getDadesAcademiquesServiceInstance();
 
 			ITimePeriod[] semestres;
 			if( timePeriod == null ){
 				semestres = getActivePeriods();
 			} else {
 				semestres = new ITimePeriod[1];
 				semestres[0] = timePeriod;
 			}
 
 			for (ITimePeriod iTimePeriod : semestres) {
 				Semester semester = (Semester) iTimePeriod;
 				String semesterId = semester.getID();
 
 				AssignaturaReduidaVO[] asignaturas = dades.getAssignaturesByResponsableAny(idp, semesterId);
 				for (AssignaturaReduidaVO aula : asignaturas) {
 					asignaturasPRA.add( aula );
 				}
 			}
 		} catch (Exception e) {
 			log.error("Error al obtener el listado de asignaturas del PRA.");
 			e.printStackTrace();
 			throw new AuthPelpException("Error al obtener el listado de asignaturas del PRA.");   
 		}
 
 		return asignaturasPRA;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 }
