package tech.grasshopper.reporters.aggregates;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.aventstack.extentreports.model.Log;
import com.aventstack.extentreports.model.Test;

import lombok.Data;
import tech.grasshopper.pojo.Feature;
import tech.grasshopper.pojo.Hook;
import tech.grasshopper.pojo.Result;
import tech.grasshopper.pojo.Scenario;
import tech.grasshopper.pojo.Step;

import static java.util.stream.Collectors.toList;

public class DurationCalculator {

	private List<Feature> features;
	private List<Test> extentTestHeirarchy;
	private List<Scenario> scenarios;
	private Date updatedDate;

	public DurationCalculator(List<Feature> features, List<Test> extentTestHeirarchy) {
		this.features = features;
		this.extentTestHeirarchy = extentTestHeirarchy;
		scenarios = features.stream().flatMap(f -> f.getElements().stream()).collect(toList());
		updatedDate = new Date();
	}

	public ReporterDuration calculateReportDuration() {
		ReporterDuration durationData = new ReporterDuration();
		Comparator<Date> dateComparator = Date::compareTo;

		durationData.setStartTime(
				features.stream().map(Feature::getStartTime).sorted(dateComparator).collect(toList()).get(0));
		durationData.setEndTime(
				features.stream().map(Feature::getEndTime).sorted(dateComparator.reversed()).collect(toList()).get(0));
		return durationData;
	}

	@Data
	public static class ReporterDuration {
		private Date startTime;
		private Date endTime;
	}

	public void updateExtentTestTimeData() {
		List<Test> featureChildrenTests = extentTestHeirarchy.stream()
				.flatMap(e -> e.getNodeContext().getAll().stream()).collect(toList());
		List<Test> scenarioTests = new ArrayList<>();

		for (Test test : featureChildrenTests) {
			if (test.getBehaviorDrivenTypeName().equalsIgnoreCase("Scenario Outline")) {
				scenarioTests.addAll(test.getNodeContext().getAll());
				updateScenarioOutlineExtentTestStartEndTimes(test);
			} else
				scenarioTests.add(test);
		}
		updateScenarioExtentTestStartEndTimes(scenarioTests);
	}

	protected void updateScenarioOutlineExtentTestStartEndTimes(Test scenarioOutlineExtentTest) {
		List<Integer> childTestIds = scenarioOutlineExtentTest.getNodeContext().getAll().stream().map(t -> t.getId())
				.collect(toList());
		List<Scenario> childScenarios = scenarios.stream().filter(s -> childTestIds.contains(s.getTestId()))
				.collect(toList());

		List<Date> startTimes = childScenarios.stream().map(s -> s.getStartTime()).collect(toList());
		List<Date> endTimes = childScenarios.stream().map(s -> s.getEndTime()).collect(toList());

		Comparator<Date> dateComparator = Date::compareTo;
		Comparator<Date> dateComparatorReversed = dateComparator.reversed();

		startTimes.sort(dateComparator);
		endTimes.sort(dateComparatorReversed);

		scenarioOutlineExtentTest.setStartTime(startTimes.get(0));
		scenarioOutlineExtentTest.setEndTime(endTimes.get(0));
	}

	protected void updateScenarioExtentTestStartEndTimes(List<Test> scenarioExtentTests) {
		Map<Integer, Scenario> idToScenarioMap = scenarios.stream()
				.collect(Collectors.toMap(Scenario::getTestId, Function.identity()));

		scenarioExtentTests.forEach(s -> {
			Scenario matched = idToScenarioMap.get(s.getId());
			s.setStartTime(matched.getStartTime());
			s.setEndTime(matched.getEndTime());
		});

		List<Test> stepAndHooksExtentTests = scenarioExtentTests.stream()
				.flatMap(e -> e.getNodeContext().getAll().stream()).collect(toList());
		updateStepAndHookExtentTestStartEndTimesAndLogTimestamp(stepAndHooksExtentTests);
	}

	private void updateStepAndHookExtentTestStartEndTimesAndLogTimestamp(List<Test> stepAndHooksExtentTests) {
		Map<Integer, Test> idToTestMap = stepAndHooksExtentTests.stream()
				.collect(Collectors.toMap(Test::getId, Function.identity()));

		scenarios.forEach(s -> {
			updatedDate = s.getStartTime();
			updateHookExtentTestStartEndTimesAndLogTimestamp(s.getBefore(), idToTestMap);
			for (Step step : s.getSteps()) {
				updateHookExtentTestStartEndTimesAndLogTimestamp(step.getBefore(), idToTestMap);
				updateStepExtentTestStartEndTimesAndLogTimestamp(step, idToTestMap);
				updateHookExtentTestStartEndTimesAndLogTimestamp(step.getAfter(), idToTestMap);
			}
			updateHookExtentTestStartEndTimesAndLogTimestamp(s.getAfter(), idToTestMap);
		});
	}

	private void updateHookExtentTestStartEndTimesAndLogTimestamp(List<Hook> hooks, Map<Integer, Test> idToTestMap) {
		for (Hook hook : hooks) {
			Test hookTest = idToTestMap.get(hook.getTestId());
			updateTestStartEndTimesAndLogTimestamp(hookTest, hook.getResult());
		}
	}

	private void updateStepExtentTestStartEndTimesAndLogTimestamp(Step step, Map<Integer, Test> idToTestMap) {
		Test stepTest = idToTestMap.get(step.getTestId());
		updateTestStartEndTimesAndLogTimestamp(stepTest, step.getResult());
	}

	private void updateTestStartEndTimesAndLogTimestamp(Test test, Result result) {
		test.setStartTime(updatedDate);
		Date logTimeStamp = updatedDate;

		List<Log> stepLogs = test.getLogContext().getAll();
		stepLogs.forEach(l -> l.setTimestamp(logTimeStamp));

		updatedDate = Date.from(updatedDate.toInstant().plusNanos(result.getDuration()));
		test.setEndTime(updatedDate);
	}
}