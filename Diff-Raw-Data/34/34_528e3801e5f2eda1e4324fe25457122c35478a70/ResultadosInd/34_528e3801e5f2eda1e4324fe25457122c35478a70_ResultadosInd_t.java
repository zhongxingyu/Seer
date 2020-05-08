 package jogosolimpicos;
 
 import java.io.*;
 
 /**
  * Main class for individual result.
  * 
  */
 @SuppressWarnings("serial")
 public class ResultadosInd implements Serializable {
 	/**
 	 * athlete with that result
 	 * 
 	 * @see Atleta athlete details
 	 */
 	private Atleta atleta;
 	/**
 	 * result in the string
 	 */
 	private String resulTemp;
 	/**
 	 * numeric result to compare
 	 */
 	private float resultado;
 
 	/**
 	 * Main constructor for the individual result. This constructor adds the
 	 * athlete, the result and converts the string result to numeric.
 	 * 
 	 * @param atleta
 	 *            athlete
 	 * @param resultado
 	 *            result in the string
 	 * @param tipoClass
 	 *            type of the classification. Distance = 0, time = 1, score = 2
 	 *            and ranked = 3.
 	 * @see Atleta athlete details
 	 * @see Disciplina type of the classification details
 	 */
 	public ResultadosInd(Atleta atleta, String resultado, int tipoClass) {
 		setResulTemp(resultado);
 		convertResul(resultado, tipoClass);
 		setAtleta(atleta);
 
 	}
 
 	/**
 	 * Get the athlete
 	 * 
 	 * @return the athlete
 	 * @see Atleta athlete details
 	 */
 	public Atleta getAtleta() {
 
 		return atleta;
 	}
 
 	/**
 	 * Set the athlete
 	 * 
 	 * @param atleta
 	 *            the athlete
 	 * @see Atleta athlete details
 	 */
 	public void setAtleta(Atleta atleta) {
 
 		this.atleta = atleta;
 	}
 
 	/**
 	 * Convert the string time into numeric.
 	 */
 	private void convertTempo() {
 
 		if (resulTemp.matches("^[0-9]{2}:[0-9]{2}:[0-9]{2}$")) {
 			String temp[] = resulTemp.split(":");
 			setResultado(Float.parseFloat(temp[0]) * 3600 + Float.parseFloat(temp[1]) * 60 + Float.parseFloat(temp[2]));
 		} else if (resulTemp.matches("^[0-9]{1,2}:[0-9]{2}(.[0-9]{1,})?$")) {
 			String temp[] = resulTemp.split(":");
 			setResultado(Float.parseFloat(temp[0]) * 60 + Float.parseFloat(temp[1]));
 
 		} else {
 			setResultado(Float.parseFloat(resulTemp));
 		}
 
 	}
 
 	/**
 	 * Convert the string result into numeric.
 	 * 
 	 * @param resultado
 	 *            string with the result.
 	 * @param tipoClass
 	 *            type of the classification. Distance = 0, time = 1, score = 2
 	 *            and ranked = 3.
 	 * @see Disciplina type of classification details
 	 */
 	private void convertResul(String resultado, int tipoClass) {
 		if (tipoClass == 0) {
 			convertDist();
 		} else if (tipoClass == 1) {
 			convertTempo();
 		} else if (tipoClass == 2) {
 			setResultado(Float.parseFloat(resulTemp));
 		} else {
 			setResultado(Float.parseFloat(resulTemp));
 		}
 
 	}
 
 	/**
 	 * Convert the string distance into numeric
 	 */
 	private void convertDist() {
 		String temp = resulTemp;
		temp = temp.replaceAll(" ", "");
		if (temp.matches("^[0-9]*(.{1}[0-9]{1,})?(m){1}$"))
			temp = temp.replaceAll("m", "");
		else if (temp.matches("^[0-9]*(.{1}[0-9]{1,})?(ft){1}$"))
			temp = temp.replaceAll("ft", "");
 		setResultado(Float.parseFloat(temp));
 	}
 
 	/**
 	 * Get the numeric result
 	 * 
 	 * @return numeric result
 	 */
 	public float getResultado() {
 		return resultado;
 	}
 
 	/**
 	 * Set the numeric result
 	 * 
 	 * @param resultado
 	 *            numeric result
 	 */
 	public void setResultado(float resultado) {
 		this.resultado = resultado;
 	}
 
 	/**
 	 * Set the string result
 	 * 
 	 * @param resulTemp
 	 *            string result
 	 */
 	public void setResulTemp(String resulTemp) {
 		this.resulTemp = resulTemp;
 	}
 
 	/**
 	 * @param o
 	 *            Object to be compared.
 	 * @return the difference between this result and that result.
 	 */
 	public int compareTo(Object o) {
 
 		return (int) (this.getResultado() * 10000 - ((ResultadosInd) o).getResultado() * 10000);
 	}
 
 	/**
 	 * Get the string result
 	 * 
 	 * @return string result
 	 */
 	public String getResulTemp() {
 		return resulTemp;
 	}
 
 }
