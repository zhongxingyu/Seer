 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package us.physion.ovation.ui.browser.insertion;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import static javax.swing.Action.NAME;
 import org.joda.time.DateTime;
 import org.openide.WizardDescriptor;
 import org.openide.util.lookup.ServiceProvider;
 import org.openide.util.lookup.ServiceProviders;
 import us.physion.ovation.DataContext;
 import us.physion.ovation.domain.Epoch;
 import us.physion.ovation.domain.EpochContainer;
 import us.physion.ovation.domain.Protocol;
 import us.physion.ovation.domain.Source;
 import us.physion.ovation.ui.interfaces.EpochGroupInsertable;
 import us.physion.ovation.ui.interfaces.ExperimentInsertable;
 import us.physion.ovation.ui.interfaces.IEntityWrapper;
 
 @ServiceProviders(value={
     @ServiceProvider(service=EpochGroupInsertable.class),
     @ServiceProvider(service=ExperimentInsertable.class)
 })
 /**
  *
  * @author jackie
  */
 public class InsertEpochAndMeasurement extends InsertEntity implements ExperimentInsertable, EpochGroupInsertable{
     public InsertEpochAndMeasurement() {
         putValue(NAME, "Insert Measurement...");
     }
 
     @Override
     public List<WizardDescriptor.Panel<WizardDescriptor>> getPanels(IEntityWrapper parent)
     {
         String explanation =
             "<html><p>Select any Sources that contributed to the generation of this Measurement.<br/>"
             + "These sources become the inputs to the Epoch that generates this Measurement.</p>"
             + "<br/><p>Input Sources are given names within the scope of their containing Epoch, to<br/>"
             + "distinguish one input from another. Choose names that reflect the Source's relationship<br/>"
             + "to the Epoch.</p><br/></html>";
         List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
         panels.add(new StartAndEndTimeController("epoch"));//start and end time
         panels.add(new NamedSourceController("epoch.inputs", null, explanation));
         panels.add(new SelectProtocolController("epoch"));//protocol
         panels.add(new KeyValueController("Add Protocol Parameters", "Add optional protocol parameters", "epoch.protocolParameters"));
         panels.add(new KeyValueController("Add Device Parameters", "Add optional device parameters", "epoch.deviceParameters"));
         
         panels.add(new DataElementController());
         panels.add(new SourceNameSelectionController("epoch.inputs"));
         panels.add(new DeviceNameSelectionController((EpochContainer)parent.getEntity()));
         return panels;
     }
 
     @Override
     public void wizardFinished(WizardDescriptor wiz, DataContext c, IEntityWrapper parent)
     {
         EpochContainer parentEntity = (EpochContainer)parent.getEntity();
         
         Map<String, Source> inputSources = (Map<String, Source>) wiz.getProperty("epoch.inputs");
         DateTime start = (DateTime) wiz.getProperty("epoch.start");
         DateTime end = (DateTime) wiz.getProperty("epoch.end");
         Protocol protocol = getProtocolFromProtocolSelector(parentEntity.getDataContext(),
                 (Map<String, String>) wiz.getProperty("epoch.newProtocols"),
                 (String) wiz.getProperty("epoch.protocolName"),
                 (Protocol) wiz.getProperty("epoch.protocol"));
 
         Map<String, Object> protocolParameters = (Map<String, Object>) wiz.getProperty("epoch.protocolParameters");
         Map<String, Object> deviceParameters = (Map<String, Object>) wiz.getProperty("epoch.deviceParameters");
             
         Epoch epoch = parentEntity.insertEpoch(inputSources, null, start, end, protocol, protocolParameters, deviceParameters);
        String name = (String)wiz.getProperty("measurement.name");
        String contentType = (String)wiz.getProperty("measurement.contentType");
        URL file = (URL)wiz.getProperty("measurement.file");
         Set<String> sourceNames = (Set<String>)wiz.getProperty("sourceNames");
         Set<String> deviceNames = (Set<String>)wiz.getProperty("deviceNames");
 
         epoch.insertMeasurement(name, sourceNames, deviceNames, file, contentType);
     }
 }
