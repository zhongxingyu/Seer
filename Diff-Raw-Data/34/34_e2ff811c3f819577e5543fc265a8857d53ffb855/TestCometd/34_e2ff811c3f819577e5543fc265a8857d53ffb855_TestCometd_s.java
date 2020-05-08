 package org.wicketstuff.push.examples.pages;
 
 import java.util.Map;
 
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.model.Model;
 import org.wicketstuff.push.ChannelEvent;
 import org.wicketstuff.push.IChannelListener;
import org.wicketstuff.push.IChannelService;
 import org.wicketstuff.push.IChannelTarget;
 
 public class TestCometd extends ExamplePage {
 
	private final TextField field;
 	private String val;
 
	public TestCometd(final PageParameters parameters)
 	{
		final IChannelService channelService = getCometdService();

 		final AjaxLink link = new AjaxLink("link"){
 
 			@Override
       public void onClick(final AjaxRequestTarget target) {
				channelService.publish(new ChannelEvent("myChannel"));
 			}
 
 		};
 		add(link);
 
 		field = new TextField("text", new Model(val));
 		field.setOutputMarkupId(true);
		channelService.addChannelListener(this, "myChannel", new IChannelListener() {
			public void onEvent(final String channel, final Map datas, final IChannelTarget target) {
				field.setModel(new Model("updated"));
				target.addComponent(field);
			}
		});
 		add(field);
 	}
 }
