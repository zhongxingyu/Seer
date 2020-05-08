 package edu.mx.utvm.congreso.dao.impl;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
 import javax.sql.DataSource;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.stereotype.Repository;
 
 import edu.mx.utvm.congreso.dao.IActivitieDao;
 import edu.mx.utvm.congreso.dominio.Activitie;
 import edu.mx.utvm.congreso.dominio.Place;
 import edu.mx.utvm.congreso.dominio.PlaceSection;
 @Repository
 public class ActivitieDaoImpl extends JdbcTemplate implements IActivitieDao{
 
 	@Autowired
 	@Override
 	public void setDataSource(DataSource dataSource) {
 		super.setDataSource(dataSource);
 	}
 
 	@Override
 	public void create(Activitie newInstance) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public Activitie read(Integer id) {
 		String sql = "";
 		sql = sql + "select ";
 		sql = sql + 	"a.id, a.id_place_section, a.day, a.activitie, a.hour, a.h1 , a.h2, a.capacity_max, a.capacity_min,ps.id, a.is_visit, ";
 		sql = sql + 	"ps.id_place, ps.place_section,p.id, p.place ";
 		sql = sql + "from ";
 		sql = sql + 	"activities a ";
 		sql = sql + 	"left join place_section ps on ps.id = a.id_place_section ";
 		sql = sql + 	"left join place p on p.id = ps.id_place  ";
 		sql = sql + "where a.id = ?";	 
 		try {
 			Activitie resultado = this.queryForObject(sql,
 				new Object[] { id },
 				new RowMapper<Activitie>() {
 					@Override
 					public Activitie mapRow(ResultSet rs,
 							int rowNum) throws SQLException {
 
 						Activitie activitie = new Activitie();
 						activitie.setActivitie(rs.getString("a.activitie"));
 						activitie.setCapacityMax(rs.getInt("a.capacity_max"));
 						activitie.setCapacityMin(rs.getInt("a.capacity_min"));
 						activitie.setDay(rs.getString("a.day"));
 						activitie.setH1(rs.getInt("a.h1"));
 						activitie.setH2(rs.getInt("a.h2"));
 						activitie.setHour(rs.getString("a.hour"));
 						activitie.setId(rs.getInt("a.id"));
 						activitie.setVisit(rs.getString("a.is_visit"));
 						
 						PlaceSection placeSection = new PlaceSection();
 						placeSection.setId(rs.getInt("ps.id"));
 						placeSection.setPlaceSection(rs.getString("ps.place_section"));
 						
 						Place place = new Place();
 						place.setId(rs.getInt("p.id"));
 						place.setPlace(rs.getString("p.place"));
 						placeSection.setPlace(place);
 						
 						activitie.setPlaceSection(placeSection);
 						
 						return activitie;
 					}
 				});
 			return resultado;
 		} catch (EmptyResultDataAccessException accessException) {
 			return null;
 		}
 	}
 
 	@Override
 	public void update(Activitie transientObject) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void delete(Activitie persistentObject) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public List<Activitie> findAll() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	public List<Activitie> findAllByEmail(String email) {
 		
 		String sql = "";
 		sql = sql + "select ";
 		sql = sql + 	"a.id, a.id_place_section, a.day, a.activitie, a.hour, a.h1 , a.h2, a.capacity_max, a.capacity_min, a.is_visit, ";
 		sql = sql + 	"ps.id, ps.id_place, ps.place_section,";
 		sql = sql + 	"p.id, p.place,";
 		sql = sql + 	"coalesce(ap.id_activitie,0) as asignacion ";
 		sql = sql + "from ";
 		sql = sql + 	"activities a ";
 		sql = sql + "left join activitie_participant ap on ap.email = '" + email + "' and a.id = ap.id_activitie ";
 		sql = sql + "left join place_section ps on ps.id = a.id_place_section ";
 		sql = sql + "left join place p on p.id = ps.id_place order by a.day, a.hour";
 		
 		List<Activitie> resultados = this.query(sql, new RowMapper<Activitie>() {
 			@Override
 			public Activitie mapRow(ResultSet rs, int rowNum) throws SQLException {
 				Activitie activitie = new Activitie();
 				activitie.setActivitie(rs.getString("a.activitie"));
 				activitie.setAsignacion(rs.getInt("asignacion"));
 				activitie.setCapacityMax(rs.getInt("a.capacity_max"));
 				activitie.setCapacityMin(rs.getInt("a.capacity_min"));
 				activitie.setDay(rs.getString("a.day"));
 				activitie.setH1(rs.getInt("a.h1"));
 				activitie.setH2(rs.getInt("a.h2"));
 				activitie.setHour(rs.getString("a.hour"));
 				activitie.setId(rs.getInt("a.id"));
 				activitie.setVisit(rs.getString("a.is_visit"));
 				
 				PlaceSection placeSection = new PlaceSection();
 				placeSection.setId(rs.getInt("ps.id"));
 				placeSection.setPlaceSection(rs.getString("ps.place_section"));
 				
 				Place place = new Place();
 				place.setId(rs.getInt("p.id"));
 				place.setPlace(rs.getString("p.place"));
 				placeSection.setPlace(place);
 				
 				activitie.setPlaceSection(placeSection);
 				
 				return activitie;
 			}
 		});
 		return resultados;
 	}
 
 	@Override
 	public void asist(int idActivitie, String email) {
 		this.update("insert into activitie_participant (id_activitie, email) values (?,?)", 
 			new Object[] { 
 				idActivitie,
 				email,
 			}
 		);
 	}
 
 	@Override
 	public void deasist(int idActivitie, String email) {
 		this.update("delete from activitie_participant where id_activitie = ? and email = ?", 
 			new Object[] { 
 				idActivitie,
 				email,
 			}
 		);
 	}
 
 	@Override
 	public boolean canAsistAtActivitie(String email, int h1, int h2, String day) {
 		boolean result = false;
 		String sql = "";
 		sql = sql + "select ";
 		sql = sql + 	"coalesce(ap.id_activitie,0) ";
 		sql = sql + "from ";
 		sql = sql + 	"activities a, activitie_participant ap ";
 		sql = sql + "where ap.email = ? ";
 		sql = sql + 	"and a.id = ap.id_activitie ";
 		sql = sql + 	"and a.h1 >= ? ";
 		sql = sql + 	"and a.h2 <= ? ";
 		sql = sql + 	"and a.day = ? ";
 		try {			
 			this.queryForInt(sql,
 				new Object[] {
 					email,h1,h2, day
 				}
 			);
 			result = false;
 		} catch (EmptyResultDataAccessException accessException) {
 			result = true;
 		}
 		return result;
 	}
 	
 	@Override
 	public boolean haveAVisit(String email) {
 		boolean result = false;
 		String sql = "";
 		sql = sql + "select ";
 		sql = sql + 	"NULL ";
 		sql = sql + "from ";
 		sql = sql + 	"activitie_participant ap, ";
 		sql = sql + 	"activities a ";
 		sql = sql + "where ";
 		sql = sql + 	"ap.email = ? ";
 		sql = sql + 	"and a.id = ap.id_activitie and a.is_visit = 'SI'";
 		
 		try {			
 			this.queryForInt(sql,
 				new Object[] {
 					email
 				}
 			);
 			result = true;
 		} catch (EmptyResultDataAccessException accessException) {
 			result = false;
 		}
 		return result;
 	}
 
 	@Override
 	public int countActivitiesAsigned(int idActivitie) {
 		String sql = "select count(id_activitie) from activitie_participant where id_activitie = ?";
 		return this.queryForInt(sql,new Object[] {idActivitie});
 	}
 	
 	@Override
 	public boolean paySucced(String email) {
 		boolean result = false;
 		String sql = "";
 		sql = sql + "select ";
 		sql = sql + 	"NULL ";
 		sql = sql + "from ";
 		sql = sql + 	"preregister_information ";
 		sql = sql + "where ";
 		sql = sql + 	"email = ? ";
 		sql = sql + 	"and payment_status = 'PAGADO'";		
 		try {			
 			this.queryForInt(sql,
 				new Object[] {
 					email
 				}
 			);
 			result = true;
 		} catch (EmptyResultDataAccessException accessException) {
 			result = false;
 		}
 		return result;
 	}
 
 	@Override
 	public List<Activitie> findActivitiesSelectedByEmail(String email) {
 				
 		String sql = "";
 		sql = sql + "select ";
 		sql = sql + 	"a.id, a.id_place_section, a.day, a.activitie, a.hour, a.h1 , a.h2, a.capacity_max, a.capacity_min, a.is_visit,ps.id,  ";
 		sql = sql + 	"ps.id_place, ps.place_section, ";
 		sql = sql + 	"p.id, p.place ";
 		sql = sql + "from  ";
 		sql = sql + 	"activitie_participant ap, activities a, place_section ps, place p ";
 		sql = sql + "where ";
		sql = sql + 	"ap.email = 'mogugos_adony@hotmail.com' and ";
 		sql = sql + 	"a.id = ap.id_activitie and ";
 		sql = sql + 	"ps.id = a.id_place_section and ";
 		sql = sql + 	"p.id = ps.id_place ";
 		sql = sql + "order by ";
 		sql = sql + 	"a.day, a.hour";
 
 
 		
		List<Activitie> resultados = this.query(sql, new RowMapper<Activitie>() {
 			@Override
 			public Activitie mapRow(ResultSet rs, int rowNum) throws SQLException {
 				Activitie activitie = new Activitie();
 				activitie.setActivitie(rs.getString("a.activitie"));
 				activitie.setCapacityMax(rs.getInt("a.capacity_max"));
 				activitie.setCapacityMin(rs.getInt("a.capacity_min"));
 				activitie.setDay(rs.getString("a.day"));
 				activitie.setH1(rs.getInt("a.h1"));
 				activitie.setH2(rs.getInt("a.h2"));
 				activitie.setHour(rs.getString("a.hour"));
 				activitie.setId(rs.getInt("a.id"));
 				activitie.setVisit(rs.getString("a.is_visit"));
 				
 				PlaceSection placeSection = new PlaceSection();
 				placeSection.setId(rs.getInt("ps.id"));
 				placeSection.setPlaceSection(rs.getString("ps.place_section"));
 				
 				Place place = new Place();
 				place.setId(rs.getInt("p.id"));
 				place.setPlace(rs.getString("p.place"));
 				placeSection.setPlace(place);
 				
 				activitie.setPlaceSection(placeSection);
 				
 				return activitie;
 			}
 		});
 		return resultados;
 	}
 }
