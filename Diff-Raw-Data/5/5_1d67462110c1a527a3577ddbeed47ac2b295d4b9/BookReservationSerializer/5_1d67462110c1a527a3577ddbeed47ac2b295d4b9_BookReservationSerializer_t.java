 package pe.edu.pucp.resource;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.restlet.data.Form;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import pe.edu.pucp.dao.LibraryServiceDAO;
 import pe.edu.pucp.model.BaseSerializer;
 import pe.edu.pucp.model.Book;
 import pe.edu.pucp.model.BookReservation;
 import pe.edu.pucp.model.Student;
 
 import com.googlecode.objectify.Key;
 
 /**
  * @author cgavidia
  * 
  */
 public class BookReservationSerializer extends BaseSerializer<BookReservation> {
 
 	public static final Logger LOG = Logger
 			.getLogger(BookReservationSerializer.class.getName());
 
 	public static final String CODE_ELEMENT = "codigo";
 	private static final String LIST_ROOT_ELEMENT = "prestamos";
 	private static final String STUDENT_ID_ELEMENT = "codigoEstudiante";
 	private static final String BOOK_ID_ELEMENT = "codigoLibro";
 	private static final String DATE_PATTERN = "dd-MM-yyyy hh:mm";
 	private static final String DATE_ELEMENT = "fechaHora";
 
 	private static final String RESERVATION_ELEMENT = "prestamo";
 	private BookReservation entity;
 
 	private SimpleDateFormat formatter = new SimpleDateFormat(DATE_PATTERN);
 
 	@Override
 	public BookReservation getEntity() {
 		return entity;
 	}
 
 	@Override
 	public String getRootListElement() {
 		return LIST_ROOT_ELEMENT;
 	}
 
 	@Override
 	public void intializeProperties(Form form) {
 		if (form.getFirstValue(CODE_ELEMENT) != null) {
 			entity.setId(Long.parseLong(form.getFirstValue(CODE_ELEMENT)));
 		}
 		entity.setStudent(new Key<Student>(Student.class, Long.parseLong(form
 				.getFirstValue(STUDENT_ID_ELEMENT))));
 		entity.setBook(new Key<Book>(Book.class, Long.parseLong(form
 				.getFirstValue(BOOK_ID_ELEMENT))));
 		try {
 			entity.setDate(formatter.parse(form.getFirstValue(DATE_ELEMENT)));
 		} catch (ParseException e) {
 			LOG.log(Level.SEVERE, "Error parsing", e);
 			entity.setDate(null);
 		}
 	}
 
 	@Override
 	public Element toXml(Document document) {
 		Element reservationElement = document
 				.createElement(RESERVATION_ELEMENT);
 
 		Element codeElement = document.createElement(CODE_ELEMENT);
 		codeElement.appendChild(document.createTextNode(entity.getId()
 				.toString()));
 		reservationElement.appendChild(codeElement);
 
 		Student student = new LibraryServiceDAO<Student>(Student.class)
 				.get(entity.getStudent());
 		Book book = new LibraryServiceDAO<Book>(Book.class).get(entity
 				.getBook());
 		reservationElement.appendChild(new StudentSerializer(student)
 				.toXml(document));
 		reservationElement
 				.appendChild(new BookSerializer(book).toXml(document));
 
 		Element dateElement = document.createElement(DATE_ELEMENT);
		dateElement.appendChild(document
				.createTextNode(entity.getDate() != null ? formatter
						.format(entity.getDate()) : ""));
 		reservationElement.appendChild(dateElement);
 
 		return reservationElement;
 	}
 
 	@Override
 	public void setEntity(BookReservation entity) {
 		this.entity = entity;
 	}
 
 	@Override
 	public void setId(Long id) {
 		entity.setId(id);
 	}
 
 	@Override
 	public String getCodeElement() {
 		return CODE_ELEMENT;
 	}
 
 }
