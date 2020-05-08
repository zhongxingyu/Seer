 package info.micdm.munin_client;
 
 import info.micdm.munin_client.events.Event;
 import info.micdm.munin_client.events.EventDispatcher;
 import info.micdm.munin_client.events.EventListener;
 import info.micdm.munin_client.graph.GraphView;
 import info.micdm.munin_client.models.Node;
 import info.micdm.munin_client.models.Server;
 import info.micdm.munin_client.models.ServerList;
 import info.micdm.munin_client.reports.Report;
 import info.micdm.munin_client.reports.ReportLoader;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.View;
 
 /**
  * Экран с информацией о ноде.
  * @author Mic, 2011
  *
  */
 public class NodeActivity extends Activity {
 
     /**
      * Отображаемый сервер.
      */
     protected Server _server;
     
     /**
      * Отображаемая нода.
      */
     protected Node _node;
     
     /**
      * Отображаемый тип отчетов.
      */
     protected int _reportTypeIndex = 0;
     
     /**
      * Отображаемый период.
      */
     protected Report.Period _reportPeriod = Report.Period.HOUR;
     
     /**
      * Запоминает сервер и ноду для отображения.
      */
     protected void _setServerAndNode(Bundle bundle) {
         String serverName = bundle.getString("server");
         String nodeName = bundle.getString("node");
         _server = ServerList.INSTANCE.get(serverName);
         _node = _server.getNode(nodeName);
     }
     
     /**
      * Загружает отчет.
      */
     protected void _loadReport() {
         Report.Type type = _node.getReportTypes().get(_reportTypeIndex);
         ReportLoader.INSTANCE.load(_server, _node, type, _reportPeriod);
     }
     
     /**
      * Загружает предыдущий доступный отчет.
      */
     protected void _loadPreviousByType() {
         if (_reportTypeIndex == 0) {
             _reportTypeIndex = _node.getReportTypes().size() - 1;
         } else {
             _reportTypeIndex -= 1;
         }
         _loadReport();
     }
     
     /**
      * Загружает следующий доступный отчет.
      */
     protected void _loadNextByType() {
         if (_reportTypeIndex >= _node.getReportTypes().size() - 1) {
             _reportTypeIndex = 0;
         } else {
             _reportTypeIndex += 1;
         }
         _loadReport();
     }
     
     /**
      * Загружает предыдущий доступный отчет.
      */
     protected void _loadPreviousByPeriod() {
         _reportPeriod = (_reportPeriod == Report.Period.HOUR) ? Report.Period.DAY : Report.Period.HOUR;
         _loadReport();
     }
     
     /**
      * Загружает следующий доступный отчет.
      */
     protected void _loadNextByPeriod() {
         _reportPeriod = (_reportPeriod == Report.Period.HOUR) ? Report.Period.DAY : Report.Period.HOUR;
         _loadReport();
     }
     
     /**
      * Выполняется при событии прокрутки.
      */
     protected boolean _onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (Math.abs(e1.getY() - e2.getY()) < 200) {
             if (velocityX < 0) {
                 _loadNextByType();
                 return true;
             }
             if (velocityX > 0) {
                 _loadPreviousByType();
                 return true;
             }
        }
        if (Math.abs(e1.getX() - e2.getX()) < 200) {
             if (velocityY < 0) {
                 _loadNextByPeriod();
                 return true;
             }
             if (velocityY > 0) {
                 _loadPreviousByPeriod();
                 return true;
             }
         }
         return false;
     }
     
     /**
      * Слушает событие прокрутки, чтобы менять графики.
      */
     protected void _listenToFling() {
         final GestureDetector detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
             @Override
             public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                 return _onFling(e1, e2, velocityX, velocityY);
             }
         });
     
         findViewById(R.id.graph).setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 detector.onTouchEvent(event);
                 return true;
             }
         });
     }
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.node);
         _setServerAndNode(getIntent().getExtras());
         _listenToFling();
     }
     
     /**
      * Добавляет слушатели событий.
      */
     protected void _addListeners() {
         EventDispatcher.addListener(Event.Type.REPORT_AVAILABLE, new EventListener(this) {
             @Override
             public void notify(Event event) {
                 Object[] extra = event.getExtra();
                 Report report = (Report)extra[0];
                 GraphView view = (GraphView)findViewById(R.id.graph);
                 view.setReport(report);
             }
         });
     }
     
     @Override
     public void onResume() {
         super.onResume();
         _addListeners();
         _loadReport();
     }
     
     /**
      * Удаляет все слушатели событий.
      */
     protected void _removeListeners() {
         EventDispatcher.removeAllListeners(this);
     }
     
     @Override
     public void onPause() {
         super.onPause();
         _removeListeners();
     }
 }
