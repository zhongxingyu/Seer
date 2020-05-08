 package cz.stoupa.showtimes.imports;
 
import javax.annotation.Nullable;

 import org.joda.time.LocalDate;
 import org.joda.time.LocalDateTime;
 
 import com.google.common.base.Objects;
 import com.google.common.base.Optional;
 
 import cz.stoupa.showtimes.domain.Translation;
 
 public class ShowingImport {
 	
 	private final LocalDateTime showingDateTime;
 	private final String czechTitle;
 	private final Optional<String> originalTitle;
 	private final Optional<Integer> year;
 	private final Optional<Translation> translation;
 	
 	protected ShowingImport( LocalDateTime showingDateTime, 
 			String czechTitle,
 			@Nullable String originalTitle,
 			@Nullable Integer year, 
 			@Nullable Translation translation ) {
 		this.showingDateTime = showingDateTime;
 		this.czechTitle = czechTitle;
 		this.originalTitle = Optional.fromNullable( originalTitle );
 		this.year = Optional.fromNullable( year );
 		this.translation = Optional.fromNullable( translation );
 	}
 
 	public LocalDateTime showingDateTime() {
 		return showingDateTime;
 	}
 	
 	public LocalDate showingDate() {
 		return showingDateTime.toLocalDate();
 	}
 
 	public String czechTitle() {
 		return czechTitle;
 	}
 
 	public Optional<String> originalTitle() {
 		return originalTitle;
 	}
 
 	public Optional<Integer> year() {
 		return year;
 	}
 
 	public Optional<Translation> translation() {
 		return translation;
 	}
 
 	@Override
 	public int hashCode() {
 		return Objects.hashCode(
 				showingDateTime,
 				czechTitle,
 				originalTitle,
 				year,
 				translation );
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 	    if ( obj == this) return true;
 	    if ( obj == null) return false;
 	    if ( !( obj instanceof ShowingImport ) ) return false;
 	    final ShowingImport other = (ShowingImport) obj;
 	    return Objects.equal( this.showingDateTime, other.showingDateTime ) &&
 	    		Objects.equal( czechTitle, other.czechTitle ) &&
 	    		Objects.equal( originalTitle, other.originalTitle ) &&
 	    		Objects.equal( year, other.year ) &&
 	    		Objects.equal( translation, other.translation );
 	}
 
 	@Override
 	public String toString() {
 		return Objects.toStringHelper( this )
 				.add( "showingDateTime", showingDateTime )
 				.add( "czechTitle", czechTitle )
 				.add( "originalTitle", originalTitle )
 				.add( "year", year )
 				.add( "translation", translation )
 				.toString();
 	}
 	
 	public static class Builder {
 
 		private LocalDateTime showingDateTime;
 		private String czechTitle;
 		private String originalTitle;
 		private Integer year;
 		private Translation translation;
 		
 		public Builder( LocalDateTime showingDateTime, String czechTitle ) {
 			this.showingDateTime = showingDateTime;
 			this.czechTitle = czechTitle;
 		}
 		
 		public Builder originalTitle( String originalTitle ) {
 			this.originalTitle = originalTitle;
 			return this;
 		}
 		
 		public Builder year( Integer year ) {
 			this.year = year;
 			return this;
 		}
 		
 		public Builder translation( Translation translation ) {
 			this.translation = translation;
 			return this;
 		}
 		
 		public ShowingImport build() {
 			return new ShowingImport( showingDateTime, czechTitle, 
 					originalTitle, year, translation );
 		}
 	}
 
 }
