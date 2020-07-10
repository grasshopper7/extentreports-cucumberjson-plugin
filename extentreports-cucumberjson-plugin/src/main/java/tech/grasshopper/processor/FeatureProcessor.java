package tech.grasshopper.processor;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.inject.Singleton;

import tech.grasshopper.pojo.Feature;
import tech.grasshopper.pojo.Scenario;

@Singleton
public class FeatureProcessor {

	public void process(Feature feature) {
		updateScenarioWithBackgroundSteps(feature);
		updateStartAndEndTimes(feature);
	}

	protected void updateScenarioWithBackgroundSteps(Feature feature) {
		Scenario firstScenario = feature.getElements().get(0);
		if (firstScenario.getKeyword().equalsIgnoreCase("background")) {
			feature.getElements().remove(0);
			feature.getElements().forEach(s -> s.getSteps().addAll(0, firstScenario.getSteps()));
		}
	}
	
	protected void updateStartAndEndTimes(Feature feature) {
		List<ZonedDateTime> startTimes = new ArrayList<>();
		List<ZonedDateTime> endTimes = new ArrayList<>();
		
		Comparator<ZonedDateTime> zoneDateTimeComparator = ZonedDateTime::compareTo;
		Comparator<ZonedDateTime> zoneDateTimeComparatorReversed = zoneDateTimeComparator.reversed();
		
		feature.getElements().forEach(s -> {
			long scenarioHooksDuration = s.getBeforeAfterHooks().stream().mapToLong(h -> h.getResult().getDuration()).sum();
			long stepHooksDuration = s.getSteps().stream().flatMap(st -> st.getBeforeAfterHooks().stream()).mapToLong(h -> h.getResult().getDuration()).sum();
			long stepDurations = s.getSteps().stream().mapToLong(st -> st.getResult().getDuration()).sum();

			long duration = scenarioHooksDuration + stepHooksDuration + stepDurations;
			
			startTimes.add(DateConverter.parseToZonedDateTime(s.getStartTimestamp()));
			endTimes.add(DateConverter.parseToZonedDateTime(s.getStartTimestamp()).plusNanos(duration));
		});
		
		startTimes.sort(zoneDateTimeComparator);
		endTimes.sort(zoneDateTimeComparatorReversed);
		
		feature.setStartTime(Date.from(startTimes.get(0).toInstant()));
		feature.setEndTime(Date.from(endTimes.get(0).toInstant()));
	}
}
