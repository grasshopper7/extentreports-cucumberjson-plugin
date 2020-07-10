package tech.grasshopper.reporters.aggregates;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.GherkinKeyword;
import com.aventstack.extentreports.gherkin.model.Asterisk;
import com.aventstack.extentreports.gherkin.model.ScenarioOutline;

import tech.grasshopper.DateConverter;
import tech.grasshopper.pojo.Feature;
import tech.grasshopper.pojo.Hook;
import tech.grasshopper.pojo.Scenario;
import tech.grasshopper.pojo.Step;
import tech.grasshopper.reporters.aggregates.DurationCalculator.ReporterDuration;

public class DurationCalculatorTest {

	private DurationCalculator durationCalculator;
	private List<Feature> features = new ArrayList<>();
	private List<com.aventstack.extentreports.model.Test> extentTestHeirarchy = new ArrayList<>();
	ExtentReports extent = new ExtentReports();

	@Test
	public void testReportDuration() {
		Feature feature1 = mock(Feature.class);
		when(feature1.getStartTime()).thenReturn(DateConverter.parseToDate("2020-01-01T09:00:00.500Z"));
		when(feature1.getEndTime()).thenReturn(DateConverter.parseToDate("2020-01-01T09:00:02.000Z"));
		Feature feature2 = mock(Feature.class);
		when(feature2.getStartTime()).thenReturn(DateConverter.parseToDate("2020-01-01T09:00:01.500Z"));
		when(feature2.getEndTime()).thenReturn(DateConverter.parseToDate("2020-01-01T09:00:03.500Z"));

		durationCalculator = new DurationCalculator(features, extentTestHeirarchy);
		features.addAll(Arrays.asList(feature1, feature2));
		ReporterDuration rd = durationCalculator.calculateReportDuration();

		assertEquals("Start time of report is wrong.", DateConverter.parseToDate("2020-01-01T09:00:00.500Z"), rd.getStartTime());
		assertEquals("End time of report is wrong.", DateConverter.parseToDate("2020-01-01T09:00:03.500Z"), rd.getEndTime());
	}
	
	private Step mockStep(int testid, long duration) {
		Step step = mock(Step.class, RETURNS_DEEP_STUBS);
		when(step.getResult().getDuration()).thenReturn(duration);
		when(step.getTestId()).thenReturn(testid);
		return step;
	}
	
	private Hook mockHook(int testid, long duration) {
		Hook hook = mock(Hook.class, RETURNS_DEEP_STUBS);
		when(hook.getResult().getDuration()).thenReturn(duration);
		when(hook.getTestId()).thenReturn(testid);
		return hook;
	}
	
	private Scenario mockScenario(int testid, String startTime, String endTime) {
		Scenario scenario = mock(Scenario.class);
		when(scenario.getTestId()).thenReturn(testid);
		when(scenario.getStartTime()).thenReturn(DateConverter.parseToDate(startTime));
		when(scenario.getEndTime()).thenReturn(DateConverter.parseToDate(endTime));
		return scenario;
	}
	
	private Feature mockFeature(List<Scenario> scenarios) {
		Feature feature = mock(Feature.class);
		when(feature.getElements()).thenReturn(scenarios);
		return feature;
	}

	@Test
	public void testScenarioOutlineExtentTestStartEndTimes() {
		ExtentTest soExtentTest = extent.createTest(ScenarioOutline.class, "scenario outline", "");
		ExtentTest scenarioOneExtentTest = soExtentTest.createNode(com.aventstack.extentreports.gherkin.model.Scenario.class, "scenario one", "");
		ExtentTest scenarioTwoExtentTest = soExtentTest.createNode(com.aventstack.extentreports.gherkin.model.Scenario.class, "scenario two", "");

		Scenario scenarioOne = mockScenario(scenarioOneExtentTest.getModel().getId(), "2020-01-01T09:00:01.000Z", "2020-01-01T09:00:02.000Z");
		Scenario scenarioTwo = mockScenario(scenarioTwoExtentTest.getModel().getId(), "2020-01-01T09:00:01.500Z", "2020-01-01T09:00:03.500Z");
		Scenario scenarioOther = mockScenario(20, "2020-01-01T09:00:00.000Z", "2020-01-01T09:00:05.000Z");
			
		Feature feature = mockFeature(Arrays.asList(scenarioOne, scenarioTwo, scenarioOther));
		features.add(feature);

		durationCalculator = new DurationCalculator(features, extentTestHeirarchy);
		durationCalculator.updateScenarioOutlineExtentTestStartEndTimes(soExtentTest.getModel());
		
		assertEquals("Start time of report is wrong.", DateConverter.parseToDate("2020-01-01T09:00:01.000Z"), soExtentTest.getModel().getStartTime());
		assertEquals("End time of report is wrong.", DateConverter.parseToDate("2020-01-01T09:00:03.500Z"), soExtentTest.getModel().getEndTime());
	}
	
	@Test
	public void testScenarioExtentTestStartEndTimes() {
		ExtentTest scenarioOneExtentTest = extent.createTest(com.aventstack.extentreports.gherkin.model.Scenario.class, "scenario one", "");
		ExtentTest scenarioTwoExtentTest = extent.createTest(com.aventstack.extentreports.gherkin.model.Scenario.class, "scenario two", "");
		
		Scenario scenarioOne = mockScenario(scenarioOneExtentTest.getModel().getId(), "2020-01-01T09:00:01.000Z", "2020-01-01T09:00:02.000Z");
		Scenario scenarioTwo = mockScenario(scenarioTwoExtentTest.getModel().getId(), "2020-01-01T09:00:01.500Z", "2020-01-01T09:00:03.500Z");
		
		Feature feature = mockFeature(Arrays.asList(scenarioOne, scenarioTwo));
		features.add(feature);
		
		durationCalculator = new DurationCalculator(features, extentTestHeirarchy);
		durationCalculator.updateScenarioExtentTestStartEndTimes(Arrays.asList(scenarioOneExtentTest.getModel(), scenarioTwoExtentTest.getModel()));
		
		assertEquals("Start time of Scenario One Extent Test is wrong.", DateConverter.parseToDate("2020-01-01T09:00:01.000Z"), scenarioOneExtentTest.getModel().getStartTime());
		assertEquals("End time of Scenario One Extent Test is wrong.", DateConverter.parseToDate("2020-01-01T09:00:02.000Z"), scenarioOneExtentTest.getModel().getEndTime());
		assertEquals("Start time of Scenario Two Extent Test is wrong.", DateConverter.parseToDate("2020-01-01T09:00:01.500Z"), scenarioTwoExtentTest.getModel().getStartTime());
		assertEquals("End time of Scenario Two Extent Test is wrong.", DateConverter.parseToDate("2020-01-01T09:00:03.500Z"), scenarioTwoExtentTest.getModel().getEndTime());
	}

	@Test
	public void testStepExtentTestStartEndTimes() {
		ExtentTest scenarioExtentTest = extent.createTest(com.aventstack.extentreports.gherkin.model.Scenario.class, "scenario one", "");
		ExtentTest stepExtentTestOne = null;
		ExtentTest stepExtentTestTwo = null;
		try {
			stepExtentTestOne = scenarioExtentTest.createNode(new GherkinKeyword("Given"), "Step One");
			stepExtentTestTwo = scenarioExtentTest.createNode(new GherkinKeyword("When"), "Step Two");
		} catch (ClassNotFoundException e) { }
		
		Scenario scenario = mockScenario(scenarioExtentTest.getModel().getId(), "2020-01-01T09:00:05.000Z", "2020-01-01T09:00:08.000Z");
		
		Step stepOne = mockStep(stepExtentTestOne.getModel().getId(), 2000000000L);
		Step stepTwo = mockStep(stepExtentTestTwo.getModel().getId(), 1000000000L);
		
		when(scenario.getSteps()).thenReturn(Arrays.asList(stepOne, stepTwo));
		
		Feature feature = mockFeature(Arrays.asList(scenario));
		features.add(feature);
		
		durationCalculator = new DurationCalculator(features, extentTestHeirarchy);
		durationCalculator.updateScenarioExtentTestStartEndTimes(Arrays.asList(scenarioExtentTest.getModel()));
		
		assertEquals("Start time of Step One Extent Test is wrong.", DateConverter.parseToDate("2020-01-01T09:00:05.000Z"), stepExtentTestOne.getModel().getStartTime());
		assertEquals("End time of Step One Extent Test is wrong.", DateConverter.parseToDate("2020-01-01T09:00:07.000Z"), stepExtentTestOne.getModel().getEndTime());	

		assertEquals("Start time of Step Two Extent Test is wrong.", DateConverter.parseToDate("2020-01-01T09:00:07.000Z"), stepExtentTestTwo.getModel().getStartTime());
		assertEquals("End time of Step Two Extent Test is wrong.", DateConverter.parseToDate("2020-01-01T09:00:08.000Z"), stepExtentTestTwo.getModel().getEndTime());	
	}

	@Test
	public void testStepWithHooksExtentTestStartEndTimes() {
		ExtentTest scenarioExtentTest = extent.createTest(com.aventstack.extentreports.gherkin.model.Scenario.class, "scenario one", "");
		ExtentTest stepExtentTest = null;
		ExtentTest beforeScenarioHookExtentTest = null;
		ExtentTest afterScenarioHookExtentTest = null;
		ExtentTest beforeStepHookExtentTest = null;
		ExtentTest afterStepHookExtentTest = null;

		try {
			beforeScenarioHookExtentTest = scenarioExtentTest.createNode(Asterisk.class, "scenario before hook");
			beforeStepHookExtentTest = scenarioExtentTest.createNode(Asterisk.class, "step before hook");
			stepExtentTest = scenarioExtentTest.createNode(new GherkinKeyword("Given"), "Step");
			afterStepHookExtentTest = scenarioExtentTest.createNode(Asterisk.class, "step after hook");
			afterScenarioHookExtentTest = scenarioExtentTest.createNode(Asterisk.class, "scenario before hook");
		} catch (ClassNotFoundException e) { }
		
		Scenario scenario = mockScenario(scenarioExtentTest.getModel().getId(), "2020-01-01T09:00:05.000Z", "2020-01-01T09:00:15.000Z");
		
		Hook beforeScenarioHook = mockHook(beforeScenarioHookExtentTest.getModel().getId(), 2000000000L);
		Hook beforeStepHook = mockHook(beforeStepHookExtentTest.getModel().getId(), 2000000000L);
		Step step = mockStep(stepExtentTest.getModel().getId(), 2000000000L);
		Hook afterStepHook = mockHook(afterStepHookExtentTest.getModel().getId(), 2000000000L);
		Hook afterScenarioHook = mockHook(afterScenarioHookExtentTest.getModel().getId(), 2000000000L);
		
		when(step.getBefore()).thenReturn(Arrays.asList(beforeStepHook));
		when(step.getAfter()).thenReturn(Arrays.asList(afterStepHook));
		
		when(scenario.getSteps()).thenReturn(Arrays.asList(step));
		when(scenario.getBefore()).thenReturn(Arrays.asList(beforeScenarioHook));
		when(scenario.getAfter()).thenReturn(Arrays.asList(afterScenarioHook));
		
		Feature feature = mockFeature(Arrays.asList(scenario));
		features.add(feature);
		
		durationCalculator = new DurationCalculator(features, extentTestHeirarchy);
		durationCalculator.updateScenarioExtentTestStartEndTimes(Arrays.asList(scenarioExtentTest.getModel()));
		
		assertEquals("Start time of Scenario Before Hook Extent Test is wrong.", DateConverter.parseToDate("2020-01-01T09:00:05.000Z"), beforeScenarioHookExtentTest.getModel().getStartTime());
		assertEquals("End time of Scenario Before Hook Extent Test is wrong.", DateConverter.parseToDate("2020-01-01T09:00:07.000Z"), beforeScenarioHookExtentTest.getModel().getEndTime());	
		
		assertEquals("Start time of Step Before Hook Extent Test is wrong.", DateConverter.parseToDate("2020-01-01T09:00:07.000Z"), beforeStepHookExtentTest.getModel().getStartTime());
		assertEquals("End time of Step Before Hook Extent Test is wrong.", DateConverter.parseToDate("2020-01-01T09:00:09.000Z"), beforeStepHookExtentTest.getModel().getEndTime());	
		
		assertEquals("Start time of Step One Extent Test is wrong.", DateConverter.parseToDate("2020-01-01T09:00:09.000Z"), stepExtentTest.getModel().getStartTime());
		assertEquals("End time of Step One Extent Test is wrong.", DateConverter.parseToDate("2020-01-01T09:00:11.000Z"), stepExtentTest.getModel().getEndTime());	

		assertEquals("Start time of Scenario After Hook Extent Test is wrong.", DateConverter.parseToDate("2020-01-01T09:00:11.000Z"), afterStepHookExtentTest.getModel().getStartTime());
		assertEquals("End time of Scenario After Hook Extent Test is wrong.", DateConverter.parseToDate("2020-01-01T09:00:13.000Z"), afterStepHookExtentTest.getModel().getEndTime());	

		assertEquals("Start time of Scenario After Hook Extent Test is wrong.", DateConverter.parseToDate("2020-01-01T09:00:13.000Z"), afterScenarioHookExtentTest.getModel().getStartTime());
		assertEquals("End time of Scenario After Hook Extent Test is wrong.", DateConverter.parseToDate("2020-01-01T09:00:15.000Z"), afterScenarioHookExtentTest.getModel().getEndTime());	
	}
	
	@Test
	public void testStepAndHooksWithLogsExtentTestStartEndTimes() {
		ExtentTest scenarioExtentTest = extent.createTest(com.aventstack.extentreports.gherkin.model.Scenario.class, "scenario one", "");
		ExtentTest stepExtentTest = null;
		ExtentTest beforeScenarioHookExtentTest = null;
		
		try {
			beforeScenarioHookExtentTest = scenarioExtentTest.createNode(Asterisk.class, "scenario before hook");
			stepExtentTest = scenarioExtentTest.createNode(new GherkinKeyword("Given"), "Step");
		} catch (ClassNotFoundException e) { }
		
		Scenario scenario = mockScenario(scenarioExtentTest.getModel().getId(), "2020-01-01T09:00:05.000Z", "2020-01-01T09:00:10.000Z");
		
		Step step = mockStep(stepExtentTest.getModel().getId(), 3000000000L);
		Hook beforeScenarioHook = mockHook(beforeScenarioHookExtentTest.getModel().getId(), 2000000000L);

		beforeScenarioHookExtentTest.info("Scenario Before Hook Log Info");
		stepExtentTest.info("Step Log Info");
		
		when(scenario.getSteps()).thenReturn(Arrays.asList(step));
		when(scenario.getBefore()).thenReturn(Arrays.asList(beforeScenarioHook));
		
		Feature feature = mockFeature(Arrays.asList(scenario));
		features.add(feature);
		
		durationCalculator = new DurationCalculator(features, extentTestHeirarchy);
		durationCalculator.updateScenarioExtentTestStartEndTimes(Arrays.asList(scenarioExtentTest.getModel()));
		
		assertEquals("Timestamp of Step Extent Test Log is wrong.", DateConverter.parseToDate("2020-01-01T09:00:05.000Z"),beforeScenarioHookExtentTest.getModel().getLogContext().get(0).getTimestamp());
		assertEquals("Timestamp of Before Scenario Extent Test Log is wrong.", DateConverter.parseToDate("2020-01-01T09:00:07.000Z"),stepExtentTest.getModel().getLogContext().get(0).getTimestamp());
	}
}
