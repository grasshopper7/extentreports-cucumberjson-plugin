package tech.grasshopper.processor;

import java.util.stream.Collectors;

import javax.inject.Singleton;

import tech.grasshopper.pojo.Feature;
import tech.grasshopper.pojo.Scenario;

@Singleton
public class ScenarioProcessor {

	public void process(Scenario scenario, Feature feature) {
		updateUri(scenario, feature.getUri());
		collectStepLineNumbers(scenario);
		updateStartAndEndTimes(scenario);
	}

	protected void updateUri(Scenario scenario, String uri) {
		scenario.setUri(uri);
	}

	protected void collectStepLineNumbers(Scenario scenario) {
		scenario.setStepLines(scenario.getSteps().stream().map(s -> s.getLine()).collect(Collectors.toList()));
	}

	protected void updateStartAndEndTimes(Scenario scenario) {

		long stepHooksDuration = scenario.getSteps().stream().flatMap(st -> st.getBeforeAfterHooks().stream())
				.mapToLong(h -> h.getResult().getDuration()).sum();
		long stepDurations = scenario.getSteps().stream().mapToLong(st -> st.getResult().getDuration()).sum();

		long duration = stepHooksDuration + stepDurations;

		scenario.setStartTime(DateConverter.parseToDate(scenario.getStartTimestamp()));
		scenario.setEndTime(DateConverter.parseToDate(DateConverter.parseToZonedDateTime(scenario.getStartTimestamp()).plusNanos(duration)));
	}
}
