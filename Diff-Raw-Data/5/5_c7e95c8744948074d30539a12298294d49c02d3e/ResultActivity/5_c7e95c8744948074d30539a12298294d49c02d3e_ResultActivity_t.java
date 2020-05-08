 package tcc.tcc1;
 
 import tcc.tcc1.ResultContract.ResultReaderDbHelper;
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.TextView;
 /**
  * Tela de resultados do aplicativo
  * @author Fabricio e Manoel
  *
  */
 public class ResultActivity extends Activity{
 	
 	private TextView resultTextView, result_descTextView;
 	
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.resultado_activity);
//		ResultReaderDbHelper mDbHelper = new ResultReaderDbHelper();
 
         carregaVariaveis();    
     }
 	  
 	public void carregaVariaveis (){
 		resultTextView = (TextView)findViewById(R.id.resultado_textView);
 		result_descTextView = (TextView)findViewById(R.id.resultado_descricao_textView);
 		
 		Log.i(ManagerTest.APP_NAME, " Resultado % " + ManagerTest.getResultPercent());
 		
 		int percent = ManagerTest.getResultPercent();
 		
 		
 		if (percent >= 0 && percent <= 29 ) { 
 			
 			String strMeatFormat = getResources().getString(R.string.result_0);
 			String strMeatMsg = String.format(strMeatFormat, percent);
 			
 			resultTextView.setText(strMeatMsg);
 			result_descTextView.setText(R.string.result_0_desc);
 			
 			
 		} else if (percent > 30 && percent <= 69) {
 			resultTextView.setText(R.string.result_1);
 			result_descTextView.setText(R.string.result_1_desc);
 			
 			
 		} else if (percent > 70 && percent <= 100) {
 			
 			resultTextView.setText(R.string.result_2);
 			result_descTextView.setText(R.string.result_2_desc);
 			
 		} 
 
 	}
 }
