 package ar.noxit.ehockey.web.pages.planilla;
 
 import ar.noxit.ehockey.exception.JugadorSinTarjetasException;
 import ar.noxit.ehockey.model.Jugador;
 import ar.noxit.ehockey.model.Planilla;
 import ar.noxit.ehockey.model.TarjetasPartido;
 import ar.noxit.ehockey.web.pages.models.JugadorLocalModelItem;
 import ar.noxit.ehockey.web.pages.models.JugadorVisitanteModelItem;
 import ar.noxit.web.wicket.model.AbstractLocalDateTimeFormatModel;
 import java.util.List;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.list.Loop;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.joda.time.LocalDateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 public class PlanillaPanel extends Panel {
 
     private String FEDERACION = "Federación de Hockey - FIUBA - 75.47";
 
     public PlanillaPanel(String id, IModel<? extends Planilla> planillaModel) {
         super(id);
 
         add(new Label("federacion", this.FEDERACION));
         add(new Label("torneo", new PropertyModel<String>(planillaModel, "partido.torneo.nombre")));
         add(new Label("rueda", new PropertyModel<Integer>(planillaModel, "partido.rueda")));
         add(new Label("fecha", new PropertyModel<Integer>(planillaModel, "partido.fechaDelTorneo")));
         add(new Label("partido", new PropertyModel<Integer>(planillaModel, "partido.partido")));
        add(new Label("sector", new PropertyModel<String>(planillaModel, "partido.local.sector.sector")));
         add(new Label("categoria", "Campeonato"));
        add(new Label("division", new PropertyModel<String>(planillaModel, "partido.local.division")));
         add(new Label("zona", ""));
 
         add(new Label("goles_local", new PropertyModel<Integer>(planillaModel, "datosLocal.goles")));
         add(new Label("goles_visitante", new PropertyModel<Integer>(planillaModel, "datosVisitante.goles")));
 
         IModel<LocalDateTime> modelTime = new PropertyModel<LocalDateTime>(planillaModel, "partido.inicio");
         add(new Label("dia", new PatternLocalDateTimeModel(modelTime, "dd")));
         add(new Label("mes", new PatternLocalDateTimeModel(modelTime, "MM")));
         add(new Label("año", new PatternLocalDateTimeModel(modelTime, "YYYY")));
         add(new Label("lugar", "Paseo Colón"));
         add(new Label("nombreLocal", new PropertyModel<String>(planillaModel, "local.nombre")));
         add(new Label("nombreVisitante", new PropertyModel<String>(planillaModel, "visitante.nombre")));
 
         IModel<List<Jugador>> modelLocal = new JugadorLocalModelItem(planillaModel);
         IModel<List<Jugador>> modelVisitante = new JugadorVisitanteModelItem(planillaModel);
 
         add(new MyLoop("filasLocales", Model.of(18), modelLocal, planillaModel));
         add(new MyLoop("filasVisitantes", Model.of(18), modelVisitante, planillaModel));
 
         add(new Label("goleadores_local", new PropertyModel<String>(planillaModel, "datosLocal.goleadores")));
         add(new Label("goleadores_visitante", new PropertyModel<String>(planillaModel, "datosVisitante.goleadores")));
         add(new Label("dt_local", new PropertyModel<String>(planillaModel, "datosLocal.dT")));
         add(new Label("dt_visitante", new PropertyModel<String>(planillaModel, "datosVisitante.dT")));
         add(new Label("capitan_local", new PropertyModel<String>(planillaModel, "datosLocal.capitan")));
         add(new Label("capitan_visitante", new PropertyModel<String>(planillaModel, "datosVisitante.capitan")));
         add(new Label("pfisico_local", new PropertyModel<String>(planillaModel, "datosLocal.pFisico")));
         add(new Label("pfisico_visitante", new PropertyModel<String>(planillaModel, "datosVisitante.pFisico")));
         add(new Label("medico_local", new PropertyModel<String>(planillaModel, "datosLocal.medico")));
         add(new Label("medico_visitante", new PropertyModel<String>(planillaModel, "datosVisitante.medico")));
         add(new Label("juez_local", new PropertyModel<String>(planillaModel, "datosLocal.juezDeMesa")));
         add(new Label("juez_visitante", new PropertyModel<String>(planillaModel, "datosVisitante.juezDeMesa")));
         add(new Label("arbitro_local", new PropertyModel<String>(planillaModel, "datosLocal.arbitro")));
         add(new Label("arbitro_visitante", new PropertyModel<String>(planillaModel, "datosVisitante.arbitro")));
 
         add(new Label("observaciones", new PropertyModel<String>(planillaModel, "observaciones")));
     }
 
     private class PatternLocalDateTimeModel extends AbstractLocalDateTimeFormatModel {
 
         private String pattern;
 
         public PatternLocalDateTimeModel(IModel<LocalDateTime> delegate, String pattern) {
             super(delegate);
             this.pattern = pattern;
         }
 
         @Override
         protected DateTimeFormatter getFormatter() {
             return DateTimeFormat.forPattern(pattern);
         }
     }
 
     private class MyLoop extends Loop {
 
         private IModel<List<Jugador>> jugadores;
         private IModel<? extends Planilla> planillaModel;
 
         public MyLoop(String id, IModel<Integer> model, IModel<List<Jugador>> jugadores,
                 IModel<? extends Planilla> planillaModel) {
             super(id, model);
             this.jugadores = jugadores;
             this.planillaModel = planillaModel;
         }
 
         @Override
         protected void populateItem(LoopItem item) {
             final Integer iteration = item.getIteration();
             final IModel<Jugador> jugadorModel = new JugadorListModel(iteration);
             item.add(new Label("fichas", new PropertyModel<Integer>(jugadorModel, "ficha")));
             item.add(new Label("nombres", new AbstractReadOnlyModel<String>() {
 
                 @Override
                 public String getObject() {
                     Jugador object = jugadorModel.getObject();
                     if (object == null)
                         return null;
                     return String.format("%S, %s", object.getApellido(), object.getNombre());
                 }
             }));
             item.add(new Label("numeros", new PropertyModel<String>(jugadorModel, "letraJugador")));
             IModel<TarjetasPartido> tarjetasModel = new TarjetasPartidosModel(planillaModel, jugadorModel);
             item.add(new Label("rojas", new PropertyModel<Integer>(tarjetasModel, "rojas")));
             item.add(new Label("amarillas", new PropertyModel<Integer>(tarjetasModel, "amarillas")));
             item.add(new Label("verdes", new PropertyModel<Integer>(tarjetasModel, "verdes")));
         }
 
         private final class TarjetasPartidosModel extends AbstractReadOnlyModel<TarjetasPartido> {
 
             private IModel<? extends Planilla> planilla;
             private IModel<Jugador> jugador;
 
             public TarjetasPartidosModel(IModel<? extends Planilla> planilla, IModel<Jugador> jugador) {
                 this.planilla = planilla;
                 this.jugador = jugador;
             }
 
             @Override
             public TarjetasPartido getObject() {
                 try {
                     Jugador object = jugador.getObject();
                     if (object == null)
                         return null;
                     return planilla.getObject().getTarjetasDe(object);
                 } catch (JugadorSinTarjetasException e) {
                     return TarjetasPartido.zeroed();
                 }
             }
 
         }
 
         private final class JugadorListModel extends AbstractReadOnlyModel<Jugador> {
 
             private final Integer iteration;
 
             private JugadorListModel(Integer iteration) {
                 this.iteration = iteration;
             }
 
             @Override
             public Jugador getObject() {
                 try {
                     List<Jugador> object = jugadores.getObject();
                     return object.get(iteration);
                 } catch (IndexOutOfBoundsException e) {
                     return null;
                 }
             }
         }
     }
 }
