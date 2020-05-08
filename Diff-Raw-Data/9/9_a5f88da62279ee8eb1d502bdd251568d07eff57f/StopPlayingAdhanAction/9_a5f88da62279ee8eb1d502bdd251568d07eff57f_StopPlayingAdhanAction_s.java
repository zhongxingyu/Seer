 package net.paissad.waqtsalat.ui.actions;
 
 import java.util.Collection;
 
 import net.paissad.eclipse.logger.ILogger;
 import net.paissad.waqtsalat.core.api.Pray;
 import net.paissad.waqtsalat.ui.WaqtSalatUIPlugin;
 import net.paissad.waqtsalat.ui.audio.api.ISoundPlayer;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.resource.ImageDescriptor;
 
 public class StopPlayingAdhanAction extends Action {
 
     private static final ILogger logger = WaqtSalatUIPlugin.getPlugin().getLogger();
 
     private Collection<Pray>     prays;
 
     public StopPlayingAdhanAction(final Collection<Pray> prayTimes, String text, int style, ImageDescriptor image) {
         super(text, style);
         this.prays = prayTimes;
         setImageDescriptor(image);
         Thread t = new Thread(new Runnable() {
 
             @Override
             public void run() {
                 while (true) {
                     try {
                         if (prays != null) {
                            boolean playing = false;
                             for (Pray pray : prays) {
                                 if (pray == null) continue;
                                 if (pray.isPlayingAdhan()) {
                                     playing = true;
                                     break;
                                 }
                             }
                            setChecked(!playing);
                            setEnabled(playing);
                         }
                         Thread.sleep(200L);
                     } catch (InterruptedException e) {
                     }
                 }
             }
         });
         t.setDaemon(true);
         t.setName("Stop Playing Adhan Thread"); //$NON-NLS-1$
         t.start();
     }
 
     @Override
     public void run() {
         if (prays != null) {
             for (Pray pray : this.prays) {
                 if (pray == null) continue;
                 if (pray.isPlayingAdhan()) {
                     Object adhanPlayer = pray.getAdhanPlayer();
                     if (adhanPlayer instanceof ISoundPlayer) {
                         try {
                             ((ISoundPlayer) adhanPlayer).stop();
                         } catch (Exception e) {
                             logger.error(
                                     "Error while stopping adhan player for '" + pray.getName() + "' : " + e.getMessage(), e); //$NON-NLS-1$ //$NON-NLS-2$
                         }
                     } else {
                         logger.warn("Unable to stop adhan player for '" + pray.getName() + "' : unknown player type."); //$NON-NLS-1$ //$NON-NLS-2$
                     }
                 }
             }
         }
     }
 
     public void setPrays(Collection<Pray> prays) {
         this.prays = prays;
     }
 
 }
