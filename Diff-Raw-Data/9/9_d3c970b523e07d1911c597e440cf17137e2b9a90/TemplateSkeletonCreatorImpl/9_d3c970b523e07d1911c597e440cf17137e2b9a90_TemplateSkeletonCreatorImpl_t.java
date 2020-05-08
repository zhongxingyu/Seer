 package edu.northwestern.bioinformatics.studycalendar.service;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.PlannedCalendarDelta;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
 
 import java.util.Date;
 
 public class TemplateSkeletonCreatorImpl {
    public static Study createBase(String name) {
        Study study = new Study();
        study.setName(name);
        study.setPlannedCalendar(new PlannedCalendar());
        Amendment start = new Amendment(Amendment.INITIAL_TEMPLATE_AMENDMENT_NAME);
        start.setDate(new Date()); // TODO: might want to use NowFactory
        start.addDelta(new PlannedCalendarDelta(study.getPlannedCalendar()));
        study.setDevelopmentAmendment(start);
        return study;
    }
 
    public static void addEpoch(Study study, Integer index, Epoch epoch) {
        Delta<?> delta = study.getDevelopmentAmendment().getDeltas().get(0);
        Add add = new Add();
        add.setChild(epoch);
        add.setIndex(index);
        delta.addChange(add);
    }
 
    static class Blank implements TemplateSkeletonCreator {
        public Study create() {
            String name = "[Unnamed blank study]";
            Study study = createBase(name);
            addEpoch(study, 0, Epoch.create("[Unnamed epoch]"));
            return study;
        }
    }
 
    static class Basic implements TemplateSkeletonCreator {
        public Study create() {
           Study study = createBase("[ABC 1234]");
            addEpoch(study, 0, Epoch.create("Screening"));
            addEpoch(study, 1, Epoch.create("Treatment", "A", "B", "C"));
            addEpoch(study, 2, Epoch.create("Follow up"));
            return study;
        }
    }
 }
