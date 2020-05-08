 
 package org.paxle.desktop.impl.dialogues;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.nio.charset.Charset;
 import java.util.Hashtable;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 import javax.swing.JToggleButton;
 import javax.swing.SwingUtilities;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventConstants;
 import org.osgi.service.event.EventHandler;
 
 import org.paxle.core.IMWComponent;
 import org.paxle.core.MWComponentEvent;
 import org.paxle.core.queue.CommandEvent;
 import org.paxle.core.queue.ICommand;
 import org.paxle.core.queue.ICommandTracker;
 import org.paxle.desktop.impl.DesktopServices;
 import org.paxle.desktop.impl.Utilities;
 
 public class CrawlingConsole extends DIServicePanel implements EventHandler, ActionListener {
 	
 	private static final long serialVersionUID = 1L;
 	
 	private static class IntRingBuffer {
 		
 		private final int[] buf;
 		private final int size;
 		private int head;			// points to next free index
 		private int tail;			// points to last full index
 		private int num = 0;
 		
 		public IntRingBuffer(final int size) {
 			this.size = size;
 			buf = new int[size];
 			tail = size - 1;
 		}
 		
 		public IntRingBuffer copyToNew(final int size) {
 			final IntRingBuffer rb = new IntRingBuffer(size);
 			System.arraycopy(buf, 0, rb.buf, 0, Math.min(this.size, size));
 			rb.head = head;
 			rb.tail = tail;
 			rb.num = num;
 			return rb;
 		}
 		
 		public int push(final int x) {
 			final int r = (head == tail) ? pop() : -1;
 			buf[head++] = x;
 			if (head == size)
 				head = 0;
 			num++;
 			return r;
 		}
 		
 		public int pop() {
 			final int r = buf[tail++];
 			if (tail == size)
 				tail = 0;
 			num--;
 			return r;
 		}
 		
 		public int top() {
 			return buf[tail];
 		}
 		
 		public void clear() {
 			head = tail + 1;
 			if (head == size)
 				head = 0;
 			num = 0;
 		}
 		
 		public boolean isEmpty() {
 			return num == 0;
 		}
 	}
 	
 	private class SaveActionRunnable implements Runnable {
 		public void run() {
 			final File file = Utilities.chooseSingleFile(CrawlingConsole.this, "Save As", false, null, true);
 			if (file == null)
 				return;
 			FileWriter fw = null;
 			try {
 				fw = new FileWriter(file);
 				synchronized (sync) {
 					text.write(fw);
 				}
 			} catch (IOException e) {
 				logger.error("I/O-exception storing text contents", e);
 				Utilities.showExceptionBox("I/O-exception storing text contents", e);
 			} finally { if (fw != null) try { fw.close(); } catch (IOException e) { /* ignore */ } }
 		}
 	}
 	
 	private static final Dimension DIM_CCONSOLE = new Dimension(700, 400);
 	
 	private static final String AC_CLEAR = new String();
 	private static final String AC_SAVE = new String();
 	private static final String AC_CRAWL = new String();
 	private static final String AC_SELECT = new String();
 	
 	private static final String LBL_CRAWLER_PAUSE = "Pause crawler";
 	private static final String LBL_CRAWLER_RESUME = "Resume crawler";
 	
 	private static final DesktopServices.MWComponents DEFAULT = DesktopServices.MWComponents.CRAWLER;
 	
 	private static enum Events {
 		QUEUE, MWCOMP_STATE
 	}
 	
 	private final Log           logger = LogFactory.getLog(CrawlingConsole.class);
 	private final JScrollPane   scroll = new JScrollPane();
 	private final JTextPane     text   = new JTextPane();
 	private final JButton       clear  = Utilities.createButton("Clear", this, AC_CLEAR, null);
 	private final JButton       save   = Utilities.createButton("Save ...", this, AC_SAVE, null);
 	private final JRadioButton  enc    = new JRadioButton("URL-encoded");
 	private final JRadioButton  normal = new JRadioButton("Original URLs");
 	private final Object        sync   = new Object();
 	private final IntRingBuffer buf    = new IntRingBuffer(100);
 	private final JToggleButton cpb    = new JToggleButton();
 	private final JComboBox     cbox;
 	private final ICommandTracker tracker;
 	
 	// TODO: save enc/normal, default
 	
 	public CrawlingConsole(final DesktopServices services) {
 		super(services);
 		cbox = new JComboBox(DesktopServices.MWComponents.humanReadableNames());
 		cbox.addActionListener(this);
 		this.tracker = services.getServiceManager().getService(ICommandTracker.class);
 		init();
 	}
 	
 	private void changeListenersTo(final DesktopServices.MWComponents comp) {
 		super.unregisterService(Events.QUEUE);
 		super.unregisterService(Events.MWCOMP_STATE);
 		registerListeners(comp);
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void registerListeners(final DesktopServices.MWComponents comp) {
 		final String id = comp.getID();
 		
 		final Hashtable<String,Object> propCrawler = new Hashtable<String,Object>();
 		propCrawler.put(EventConstants.EVENT_TOPIC, new String[] { CommandEvent.TOPIC_DEQUEUED });
 		propCrawler.put(EventConstants.EVENT_FILTER, String.format("(%s=%s.in)", CommandEvent.PROP_COMPONENT_ID, id));
 		super.registerService(Events.QUEUE, this, propCrawler, EventHandler.class);
 		
 		final Hashtable<String,Object> propMW = new Hashtable<String,Object>();
 		propMW.put(EventConstants.EVENT_TOPIC, new String[] { MWComponentEvent.TOPIC_ALL });
 		propMW.put(EventConstants.EVENT_FILTER, String.format("(%s=%s)", MWComponentEvent.PROP_COMPONENT_ID, id));
 		super.registerService(Events.MWCOMP_STATE, this, propMW, EventHandler.class);
 	}
 	
 	public Container getContainer() {
 		return this;
 	}
 	
 	@Override
 	public String getTitle() {
 		return "Crawling Console";
 	}
 	
 	@Override
 	public Dimension getWindowSize() {
 		return DIM_CCONSOLE;
 	}
 	
 	private void updateCpb(final boolean paused, final boolean getState, final boolean setState) {
 		if (getState && setState)
 			throw new IllegalArgumentException("cannot set and get state at the same time");
 		final IMWComponent<?> crawler = services.getMWComponent(DesktopServices.MWComponents.CRAWLER);
 		final boolean state = (getState && crawler != null) ? crawler.isPaused() : paused;
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				cpb.setVisible(crawler != null);
 				cpb.setSelected(state);
 				cpb.setText((state) ? LBL_CRAWLER_RESUME : LBL_CRAWLER_PAUSE);
 			}
 		});
 		if (setState && crawler != null) {
 			if (paused) {
 				crawler.pause();
 			} else {
 				crawler.resume();
 			}
 		}
 	}
 	
 	public void actionPerformed(ActionEvent e) {
 		final String ac = e.getActionCommand();
 		if (ac == AC_CLEAR) {
 		} else if (ac == AC_SAVE) {
 			new Thread(new SaveActionRunnable()).start();
 		} else if (ac == AC_CRAWL) {
 			updateCpb(cpb.isSelected(), false, true);
 		} else if (ac == AC_SELECT) {
 			clear();
 			changeListenersTo(DesktopServices.MWComponents.valueOf(((String)((JComboBox)e.getSource()).getSelectedItem()).toUpperCase()));
 		}
 	}
 	
 	private void clear() {
 		updateText(false, "");
 		clear.setEnabled(false);
 		save.setEnabled(false);
 	}
 	
 	public void updateText(final boolean appendLine, final String val) {
 		int j = 0;
 		int i = -1;
 		int top = 0;
 		synchronized (sync) {
 			if (!appendLine)
 				buf.clear();
 			while ((i = val.indexOf('\n', j)) != -1) {
 				top += buf.push(i - j + 1);
 				j = i + 1;
 			}
 			
 			try {
 				final Document doc = text.getDocument();
 				
 				if (appendLine) {
 					top += buf.push(val.length() - j + 1);
 					text.getEditorKit().read(new StringReader(val + "\n"), doc, doc.getLength());
 				} else {
 					text.setText(val);
 				}
 				if (top > 0)
 					doc.remove(0, top);
 				text.setCaretPosition(doc.getLength());
 			} catch (BadLocationException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		if (!clear.isEnabled()) {
 			clear.setEnabled(true);
 			save.setEnabled(true);
 		}
 	}
 	
 	private void init() {
 		text.setEditable(false);
 		final JPanel textPanel = new JPanel(new BorderLayout());
 		textPanel.add(text, BorderLayout.CENTER);
 		scroll.getViewport().setView(textPanel);
 		
 		updateCpb(false, true, false);
 		cpb.setActionCommand(AC_CRAWL);
 		cpb.addActionListener(this);
 		
 		enc.setSelected(true);
 		final ButtonGroup bg = new ButtonGroup();
 		bg.add(enc);
 		bg.add(normal);
 		final JPanel bottomLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		bottomLeft.add(normal);
 		bottomLeft.add(enc);
 		
 		cbox.setActionCommand(AC_SELECT);
 		cbox.setSelectedIndex(DEFAULT.ordinal());
 		final JPanel bottomRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
 		bottomRight.add(cbox);
 		bottomRight.add(cpb);
 		bottomRight.add(save);
 		bottomRight.add(clear);
 		
 		final JPanel bottom = new JPanel(new BorderLayout());
 		bottom.add(bottomLeft, BorderLayout.WEST);
 		bottom.add(bottomRight, BorderLayout.EAST);
 		
 		super.setLayout(new BorderLayout());
 		super.add(scroll, BorderLayout.CENTER);
 		super.add(bottom, BorderLayout.SOUTH);
 	}
 	
 	public void handleEvent(Event event) {
 		final String topic = event.getTopic();
 		if (topic == CommandEvent.TOPIC_DEQUEUED) {
 			final Long id = (Long)event.getProperty(CommandEvent.PROP_COMMAND_ID);
 			final ICommand cmd = tracker.getCommandByID(id);
 			logger.debug("received event for command: " + cmd + " from component (ID): " + event.getProperty(CommandEvent.PROP_COMPONENT_ID));
 			if (cmd != null) try {
 				final String uri = (enc.isSelected())
 						? URLDecoder.decode(cmd.getLocation().toString(), Charset.defaultCharset().name())
 						: cmd.getLocation().toASCIIString();
 				SwingUtilities.invokeLater(new Runnable() {
 					public void run() {
 						updateText(true, uri);
 					}
 				});
 			} catch (UnsupportedEncodingException e) { /* cannot happen as we use the default charset here */ }
 			
 		} else if (topic.startsWith(MWComponentEvent.TOPIC_)) {
 			updateCpb(topic == MWComponentEvent.TOPIC_PAUSED, false, false);
 		}
 	}
 }
