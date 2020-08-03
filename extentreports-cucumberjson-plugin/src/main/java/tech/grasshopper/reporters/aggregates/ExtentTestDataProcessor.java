package tech.grasshopper.reporters.aggregates;

import static java.util.stream.Collectors.toList;

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
import tech.grasshopper.pojo.Scenario;
import tech.grasshopper.pojo.Step;

public class ExtentTestDataProcessor {

	private List<Feature> features;
	private List<Test> extentTestHeirarchy;
	private List<Scenario> scenarios;

	public ExtentTestDataProcessor(List<Feature> features, List<Test> extentTestHeirarchy) {
		this.features = features;
		this.extentTestHeirarchy = extentTestHeirarchy;
		this.scenarios = features.stream().flatMap(f -> f.getElements().stream()).collect(toList());
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

	public void updateExtentTestData() {
		List<Test> featureChildrenTests = extentTestHeirarchy.stream()
				.flatMap(e -> e.getNodeContext().getAll().stream()).collect(toList());
		List<Test> scenarioTests = new ArrayList<>();

		for (Test test : featureChildrenTests) {
			if (test.getBehaviorDrivenTypeName().equalsIgnoreCase("Scenario Outline")) {
				scenarioTests.addAll(test.getNodeContext().getAll());
				updateScenarioOutlineExtentTestData(test);
			} else
				scenarioTests.add(test);
		}
		updateScenarioExtentTestData(scenarioTests);
	}

	protected void updateScenarioOutlineExtentTestData(Test scenarioOutlineExtentTest) {
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

	protected void updateScenarioExtentTestData(List<Test> scenarioExtentTests) {
		Map<Integer, Scenario> idToScenarioMap = scenarios.stream()
				.collect(Collectors.toMap(Scenario::getTestId, Function.identity()));

		scenarioExtentTests.forEach(s -> {
			Scenario matched = idToScenarioMap.get(s.getId());
			s.setStartTime(matched.getStartTime());
			s.setEndTime(matched.getEndTime());
		});

		List<Test> stepAndHooksExtentTests = scenarioExtentTests.stream()
				.flatMap(e -> e.getNodeContext().getAll().stream()).collect(toList());
		updateStepAndHookExtentTestAndLogData(stepAndHooksExtentTests);
	}

	private void updateStepAndHookExtentTestAndLogData(List<Test> stepAndHooksExtentTests) {
		Map<Integer, Test> idToTestMap = stepAndHooksExtentTests.stream()
				.collect(Collectors.toMap(Test::getId, Function.identity()));

		scenarios.forEach(s -> {
			updateHookExtentTestAndLogData(s.getBefore(), idToTestMap);
			for (Step step : s.getSteps()) {
				updateHookExtentTestAndLogData(step.getBefore(), idToTestMap);
				updateStepExtentTestAndLogData(step, idToTestMap);
				updateHookExtentTestAndLogData(step.getAfter(), idToTestMap);
			}
			updateHookExtentTestAndLogData(s.getAfter(), idToTestMap);
		});
	}

	private void updateHookExtentTestAndLogData(List<Hook> hooks, Map<Integer, Test> idToTestMap) {
		for (Hook hook : hooks) {
			Test hookTest = idToTestMap.get(hook.getTestId());
			updateTestStartEndTimesAndLogTimestamp(hookTest, hook.getStartTime(), hook.getEndTime());
		}
	}

	private void updateStepExtentTestAndLogData(Step step, Map<Integer, Test> idToTestMap) {
		Test stepTest = idToTestMap.get(step.getTestId());
		updateTestStartEndTimesAndLogTimestamp(stepTest, step.getStartTime(), step.getEndTime());
	}

	private void updateTestStartEndTimesAndLogTimestamp(Test test, Date startTime, Date endTime) {
		test.setStartTime(startTime);
		List<Log> stepLogs = test.getLogContext().getAll();
		stepLogs.forEach(l -> l.setTimestamp(startTime));
		test.setEndTime(endTime);
	}
}