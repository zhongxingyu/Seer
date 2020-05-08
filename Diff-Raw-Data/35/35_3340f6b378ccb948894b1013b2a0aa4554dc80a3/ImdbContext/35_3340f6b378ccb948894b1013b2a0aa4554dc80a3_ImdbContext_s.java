 package de.bitkings;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.python.core.PyObject;
 import org.python.core.PyString;
 import org.python.core.PySystemState;
 import org.python.util.PythonInterpreter;
 
 import de.bitkings.model.ImdbMovieInfo;
 
 public class ImdbContext {
 	
 	private PythonInterpreter interpreter;
 
 	/**
 	 * @param id
 	 * @return
 	 */
 	public String getMovieById(String id) {
 		interpreter = getPythonInterpreter();
 		interpreter.exec("import sys");
 		interpreter.exec("sys.argv = [sys.argv[0], "+PyString.encode_UnicodeEscape(id, true)+"]");
 		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("get_movie.py")) {
 			interpreter.execfile(is);
 			PyObject movieobj = interpreter.get("movie");
			return movieobj.__getattr__("summary").__call__().toString();
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * @param moviename
 	 * @return
 	 */
 	public List<ImdbMovieInfo> searchMovie(String moviename) {
 		interpreter = getPythonInterpreter();
 		
 		List<ImdbMovieInfo> movlist = new ArrayList<ImdbMovieInfo>();
 		
 		interpreter.exec("import sys");
 		interpreter.exec("sys.argv = [sys.argv[0], "+PyString.encode_UnicodeEscape(moviename, true)+"]");
 		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("search_movie.py")) {
 			interpreter.execfile(is);
 
 			// after running, extract information from 'results' variable
 			PyObject pyobj = interpreter.get("results");
 			int len = pyobj.__len__();
 			if (len == 0) {
 //				txt_ResultSearchMovie.append("Nothing found or error happend :-/" + NL + NL);
 //				txt_ResultSearchMovie.append("Output log file " + outf + NL);
 //				txt_ResultSearchMovie.append("Error log file " + errf + NL);
 			} else {
 //				txt_ResultSearchMovie.setText("");
 				for (int i=0; i<len; i++) {
 					PyObject movieobj = pyobj.__getitem__(i);
 					interpreter.set("movie", movieobj);
 					interpreter.exec("imdbid = i.get_imdbID(movie)");
 					PyObject imdbid = interpreter.get("imdbid");
 					PyObject movie_data = movieobj.__getattr__("data");
 					String movtitle = movie_data.__finditem__("title").toString();
 					String movyear = movie_data.__finditem__("year").toString();
 					movlist.add(new ImdbMovieInfo(imdbid.toString(), movtitle, movyear));
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 		return movlist;
 	}
 
 	private PythonInterpreter getPythonInterpreter() {
 		if (interpreter == null) {
 			System.setProperty(PySystemState.PYTHON_CACHEDIR, new File(System.getProperty("java.io.tmpdir"), "jythoncachedir").toString());
 			this.interpreter = new PythonInterpreter();
 			File outf, errf;
 			try {
 				outf = File.createTempFile("search_imdb_py_LOG_OUT", ".txt");
 				errf = File.createTempFile("search_imdb_py_LOG_ERR", ".txt");
 				interpreter.setOut(new BufferedOutputStream(new FileOutputStream(outf)));
 				interpreter.setErr(new BufferedOutputStream(new FileOutputStream(errf)));
 			} catch (Exception exc) {
 				throw new RuntimeException("Error configuring PythonInterpreter.", exc);
 			}
 		}
 		return interpreter;
 	}
 
 }
