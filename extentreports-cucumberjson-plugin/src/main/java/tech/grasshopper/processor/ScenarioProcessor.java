package tech.grasshopper.processor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import tech.grasshopper.pojo.Feature;
import tech.grasshopper.pojo.Hook;
import tech.grasshopper.pojo.Hook.HookType;
import tech.grasshopper.pojo.Scenario;
import tech.grasshopper.pojo.Step;

@Singleton
public class ScenarioProcessor {

	public void process(Scenario scenario, Feature feature) {
		updateUri(scenario, feature.getUri());
		collectStepLineNumbers(scenario);
		updateScenarioStepHookType(scenario);
		updateScenarioStepHookStartAndEndTimes(scenario);
	}

	protected void updateUri(Scenario scenario, String uri) {
		scenario.setUri(uri);
	}

	protected void collectStepLineNumbers(Scenario scenario) {
		scenario.setStepLines(scenario.getSteps().stream().map(s -> s.getLine()).collect(Collectors.toList()));
	}

	protected void updateScenarioStepHookType(Scenario scenario) {
		scenario.getBefore().forEach(h -> h.setHookType(HookType.BEFORE));
		
		scenario.getSteps().stream().flatMap(s -> s.getBefore().stream()).collect(Collectors.toList())
				.forEach(h -> h.setHookType(HookType.BEFORE_STEP));
		
		scenario.getSteps().stream().flatMap(s -> s.getAfter().stream()).collect(Collectors.toList())
				.forEach(h -> h.setHookType(HookType.AFTER_STEP));
		
		scenario.getAfter().forEach(h -> h.setHookType(HookType.AFTER));
	}

	protected void updateScenarioStepHookStartAndEndTimes(Scenario scenario) {
		ZonedDateTime zoned = DateConverter.parseToZonedDateTime(scenario.getStartTimestamp());
		scenario.setStartTime(DateConverter.parseToDate(scenario.getStartTimestamp()));

		zoned = updateHookStartEndTimes(zoned, scenario.getBefore());
		for (Step step : scenario.getSteps()) {
			zoned = updateHookStartEndTimes(zoned, step.getBefore());

			step.setStartTime(DateConverter.parseToDate(zoned));
			zoned = zoned.plusNanos(step.getResult().getDuration());
			step.setEndTime(DateConverter.parseToDate(zoned));

			zoned = updateHookStartEndTimes(zoned, step.getAfter());
		}
		zoned = updateHookStartEndTimes(zoned, scenario.getAfter());
		scenario.setEndTime(DateConverter.parseToDate(zoned));
	}

	private ZonedDateTime updateHookStartEndTimes(ZonedDateTime zoned, List<Hook> hooks) {
		for (Hook hook : hooks) {
			hook.setStartTime(DateConverter.parseToDate(zoned));
			zoned = zoned.plusNanos(hook.getResult().getDuration());
			hook.setEndTime(DateConverter.parseToDate(zoned));
		}
		return zoned;
	}
}
