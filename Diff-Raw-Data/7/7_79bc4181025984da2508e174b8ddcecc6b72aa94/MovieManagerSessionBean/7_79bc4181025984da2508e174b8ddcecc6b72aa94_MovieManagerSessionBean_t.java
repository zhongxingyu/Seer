 package br.com.fourlinux.videostore.ejb;
 
 import java.util.List;
 
 import javax.ejb.LocalBean;
 import javax.ejb.Stateless;
 import javax.inject.Named;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import br.com.fourlinux.videostore.domain.Actor;
 import br.com.fourlinux.videostore.domain.Movie;
 import br.com.fourlinux.videostore.domain.User;
 
 @Stateless
 @LocalBean
 @Named("movieService")
 public class MovieManagerSessionBean {
 	@PersistenceContext(unitName = "movie-persistence")
 	private EntityManager entityManager;
 
 	public void markAsFavorite(Long movieId, Long userId) {
 		if (!isFavoriteMovie(movieId, userId)) {
 			Movie movie = entityManager.find(Movie.class, movieId);
 			User user = entityManager.find(User.class, userId);
 	
 			if (user != null && movie != null) {
 				user.getFavoriteMovies().add(movie);
 				entityManager.merge(user);
 				System.out.println("Marcando como favorito");
 			}
 		}
 	}
 	
 	public boolean isFavoriteMovie(Long movieId, Long userId) {
 		User user = entityManager.find(User.class, userId);
 		if (user != null) {
 			for (Movie movie : user.getFavoriteMovies()) {
 				if (movieId.equals(movie.getId())) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
	
	@SuppressWarnings("unchecked")
	public List<Movie> getFavoriteMovies(Long userId) {
		Query query = entityManager.createQuery("SELECT movies FROM User u JOIN u.favoriteMovies movies WHERE u.id = :id");
		query.setParameter("id", userId);
		return query.getResultList();
	}
 
 	public void addMovie(Movie movie) {
 		entityManager.persist(movie);
 	}
 
 	public void updateMovie(Movie movie) {
 		entityManager.merge(movie);
 	}
 
 	public void removeMovie(Movie movie) {
 		movie = entityManager.find(Movie.class, movie.getId());
 		if (movie != null) {
 			entityManager.remove(movie);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Movie> getAllMovies() {
 		Query query = entityManager.createQuery("SELECT m FROM Movie m");
 		return query.getResultList();
 	}
 
 	public Movie getMovie(Long id) {
 		return entityManager.find(Movie.class, id);
 	}
 
 	public Actor getActor(Long id) {
 		return entityManager.find(Actor.class, id);
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Actor> getActorsByMovie(Long movieId) {
 		Query query = entityManager
 				.createQuery("SELECT m.stars FROM Movie m WHERE m.id = :id");
 		query.setParameter("id", movieId);
 		return query.getResultList();
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Actor> getAllActors() {
 		Query query = entityManager.createQuery("SELECT a FROM Actor a");
 		return query.getResultList();
 	}
 
 	public void addActor(Actor actor) {
 		entityManager.persist(actor);
 	}
 }
