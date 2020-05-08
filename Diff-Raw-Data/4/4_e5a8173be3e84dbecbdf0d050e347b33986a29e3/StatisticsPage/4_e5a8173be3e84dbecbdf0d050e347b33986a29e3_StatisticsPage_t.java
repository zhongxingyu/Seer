 package eu.margiel.pages.admin.registration;
 
 import static ch.lambdaj.Lambda.*;
 import static com.google.common.collect.Lists.*;
 import static eu.margiel.utils.Components.*;
 import static org.hamcrest.Matchers.*;
 
 import java.util.List;
 
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.wicketstuff.annotation.mount.MountPath;
 
 import ch.lambdaj.group.Group;
 import eu.margiel.domain.Participant;
 import eu.margiel.domain.RegistrationType;
 import eu.margiel.pages.admin.AdminBasePage;
 import eu.margiel.repositories.ParticipantRepository;
 
 @SuppressWarnings("serial")
 @MountPath(path = "/admin/statistics")
 public class StatisticsPage extends AdminBasePage {
 
 	@SpringBean
 	private ParticipantRepository repository;
 	private List<Participant> allParticipants;
 
 	public StatisticsPage() {
 		this.allParticipants = repository.readAll();
 		fillStatisticsRow(this, allParticipants);
 		add(new StatusStatistics(allParticipants));
 		add(new CityStatistics(groupByCities()));
 	}
 
 	private Group<Participant> groupByCities() {
 		return group(allParticipants, by(on(Participant.class).getCity()));
 	}
 
 	private void fillStatisticsRow(MarkupContainer component, List<Participant> participants) {
 		int all = participants.size();
 		component.add(createLabelsFor("sum", all, allParticipants.size()));
 		component.add(createLabelsFor("dinerNo", withDinerFrom(participants, false), all));
 		component.add(createLabelsFor("dinerYes", withDinerFrom(participants, true), all));
 		component.add(createLabelsFor("women", withSexFrom(participants, "K"), all));
 		component.add(createLabelsFor("men", withSexFrom(participants, "M"), all));
 	}
 
 	private Label[] createLabelsFor(String id, int count, int all) {
 		return new Label[] { label(id + "Val", count + ""), label(id + "Per", getPercent(count, all) + "%") };
 	}
 
	private int getPercent(double count, double all) {
		return (int) (all == 0 ? 0 : (count / all) * 100);
 	}
 
 	private int withSexFrom(List<Participant> participants, String sex) {
 		return select(participants, having(on(Participant.class).getSex(), is(sex))).size();
 	}
 
 	private int withDinerFrom(List<Participant> participants, boolean withDiner) {
 		return select(participants, having(on(Participant.class).isLunch(), is(withDiner))).size();
 	}
 
 	private final class CityStatistics extends ListView<String> {
 		private final Group<Participant> group;
 
 		private CityStatistics(Group<Participant> group) {
 			super("city", newArrayList(group.keySet()));
 			this.group = group;
 		}
 
 		@Override
 		protected void populateItem(ListItem<String> item) {
 			String city = item.getModelObject();
 			item.add(label("name", city));
 			item.add(createLabelsFor("people", group.find(city).size(), allParticipants.size()));
 		}
 	}
 
 	private final class StatusStatistics extends ListView<RegistrationType> {
 		private final List<Participant> participants;
 
 		private StatusStatistics(List<Participant> participants) {
 			super("statusInfo", newArrayList(RegistrationType.values()));
 			this.participants = participants;
 		}
 
 		@Override
 		protected void populateItem(ListItem<RegistrationType> item) {
 			RegistrationType type = item.getModelObject();
 			item.add(label("status", type.getName()));
 			fillStatisticsRow(item, getParticipantsByType(participants, type));
 		}
 
 		private List<Participant> getParticipantsByType(final List<Participant> participants, RegistrationType type) {
 			return select(participants, having(on(Participant.class).getRegistrationType(), is(type)));
 		}
 	}
 }
