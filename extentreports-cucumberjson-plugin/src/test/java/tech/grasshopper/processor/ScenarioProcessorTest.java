package tech.grasshopper.processor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import tech.grasshopper.pojo.Scenario;
import tech.grasshopper.pojo.Step;

public class ScenarioProcessorTest {

	private ScenarioProcessor scenarioProcessor;
	
	@Before
	public void setup() {
		scenarioProcessor = new ScenarioProcessor();	
	}
	
	@Test
	public void testStepLinesCollector() {
		
		Scenario scenario = spy(Scenario.class);
		
		Step stepOne = mock(Step.class);
		when(stepOne.getLine()).thenReturn(5);
		scenario.getSteps().add(stepOne);
		
		Step stepTwo = mock(Step.class);
		when(stepTwo.getLine()).thenReturn(20);
		scenario.getSteps().add(stepTwo);
		
		scenarioProcessor.collectStepLineNumbers(scenario);
		assertEquals("", Arrays.asList(5, 20), scenario.getStepLines());
	}
}
