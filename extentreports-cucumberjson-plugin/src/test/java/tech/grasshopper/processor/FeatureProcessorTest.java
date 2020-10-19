package tech.grasshopper.processor;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import tech.grasshopper.DateConverter;
import tech.grasshopper.FeatureBuilder;
import tech.grasshopper.pojo.Feature;

public class FeatureProcessorTest {
	private FeatureProcessor featureProcessor;

	@Before
	public void setup() {
		featureProcessor = new FeatureProcessor();
	}

	@Test
	public void testBackgroundStepProcessing() {
		Feature feature = (new FeatureBuilder())
				.addScenario().setScenarioKeyword("background").addStep()
				.addScenario().setScenarioKeyword("scenario").addStep()
				.build();
		
		featureProcessor.updateScenarioWithBackgroundSteps(feature);
		assertEquals("Number of scenarios in feature is wrong.", 1, feature.getElements().size());
		assertEquals("Number of steps in scenario is wrong.", 2, feature.getElements().get(0).getSteps().size());
	}

	@Test
	public void testNoBackgroundStepsProcessing() {
		Feature feature = (new FeatureBuilder())
				.addScenario().setScenarioKeyword("scenario").addStep()
				.addScenario().addStep()
				.build();
		
		featureProcessor.updateScenarioWithBackgroundSteps(feature);
		assertEquals("Number of scenarios in feature is wrong.", 2, feature.getElements().size());
		assertEquals("Number of steps in scenario is wrong.", 1, feature.getElements().get(0).getSteps().size());
		assertEquals("Number of steps in scenario is wrong.", 1, feature.getElements().get(1).getSteps().size());
	}

	@Test
	public void testMultipleScenarioWithAllHooks() {
		Feature feature = (new FeatureBuilder())
				.addScenario().setScenarioStartTimestamp("2020-01-01T09:00:00.000Z")
				.addScenarioBeforeHook().setHookDuration(500000000L)
				.addScenarioAfterHook().setHookDuration(700000000L)
				.addStep().setStepDuration(500000000L)
				.addStepBeforeHook().setHookDuration(600000000L)
				.addStepAfterHook().setHookDuration(900000000L)
				.addStep().setStepDuration(400000000L)
				.addScenario().setScenarioStartTimestamp("2020-01-01T09:00:10.000Z")
				.addScenarioBeforeHook().setHookDuration(100000000L)
				.addScenarioAfterHook().setHookDuration(200000000L)
				.addStep().setStepDuration(700000000L)
				.addStepBeforeHook().setHookDuration(100000000L)
				.addStepAfterHook().setHookDuration(100000000L)
				.build();
		
		featureProcessor.updateStartAndEndTimes(feature);
		
		assertEquals("Feature start time is not correct.", DateConverter.parseToDate("2020-01-01T09:00:00.000Z"), feature.getStartTime());
		assertEquals("Feature end time is not correct.", DateConverter.parseToDate("2020-01-01T09:00:11.200Z"), feature.getEndTime());
	}
	
	@Test
	public void testScenarioWithNoHooks() {
		Feature feature = (new FeatureBuilder())
				.addScenario().setScenarioStartTimestamp("2020-01-01T09:00:00.000Z")
				.addStep().setStepDuration(500000000L)
				.addStep().setStepDuration(400000000L)
				.build();
		
		featureProcessor.updateStartAndEndTimes(feature);
		
		assertEquals("Feature start time is not correct.", DateConverter.parseToDate("2020-01-01T09:00:00.000Z"), feature.getStartTime());
		assertEquals("Feature end time is not correct.", DateConverter.parseToDate("2020-01-01T09:00:00.900Z"), feature.getEndTime());
	}
	
	@Test
	public void testScenarioWithScenarioHooks() {
		Feature feature = (new FeatureBuilder())
				.addScenario().setScenarioStartTimestamp("2020-01-01T09:00:00.000Z")
				.addScenarioBeforeHook().setHookDuration(50000000L)
				.addScenarioAfterHook().setHookDuration(70000000L)
				.addStep().setStepDuration(500000000L)
				.build();
		
		featureProcessor.updateStartAndEndTimes(feature);
		
		assertEquals("Feature start time is not correct.", DateConverter.parseToDate("2020-01-01T09:00:00.000Z"), feature.getStartTime());
		assertEquals("Feature end time is not correct.", DateConverter.parseToDate("2020-01-01T09:00:00.620Z"), feature.getEndTime());
	}
	
	@Test
	public void testScenarioWithStepHooks() {
		Feature feature = (new FeatureBuilder())
				.addScenario().setScenarioStartTimestamp("2020-01-01T09:00:00.000Z")
				.addStep().setStepDuration(500000000L)
				.addStepBeforeHook().setHookDuration(10000000L)
				.addStepAfterHook().setHookDuration(20000000L)
				.build();
		
		featureProcessor.updateStartAndEndTimes(feature);
		
		assertEquals("Feature start time is not correct.", DateConverter.parseToDate("2020-01-01T09:00:00.000Z"), feature.getStartTime());
		assertEquals("Feature end time is not correct.", DateConverter.parseToDate("2020-01-01T09:00:00.530Z"), feature.getEndTime());
	}
}
