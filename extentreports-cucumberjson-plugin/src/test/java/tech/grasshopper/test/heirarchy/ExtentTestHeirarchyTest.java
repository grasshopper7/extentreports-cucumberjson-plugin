package tech.grasshopper.test.heirarchy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.model.Log;

import tech.grasshopper.DateConverter;
import tech.grasshopper.FeatureBuilder;
import tech.grasshopper.exception.ExceptionParser;
import tech.grasshopper.pojo.Feature;
import tech.grasshopper.pojo.Hook;
import tech.grasshopper.pojo.Hook.HookType;
import tech.grasshopper.pojo.Scenario;
import tech.grasshopper.pojo.Step;
import tech.grasshopper.processor.EmbeddedProcessor;
import tech.grasshopper.processor.FeatureProcessor;
import tech.grasshopper.processor.ScenarioProcessor;
import tech.grasshopper.processor.StepProcessor;
import tech.grasshopper.properties.ReportProperties;

public class ExtentTestHeirarchyTest {

	private ExtentTestHeirarchy extentTestHeirarchy;
	private FeatureProcessor featureProcessor;
	private ScenarioProcessor scenarioProcessor;
	private StepProcessor stepProcessor;
	private EmbeddedProcessor embeddedProcessor;
	private ExceptionParser exceptionParser;
	private ExtentReports extent;
	private ReportProperties reportProperties;

	@Before
	public void setup() {
		featureProcessor = mock(FeatureProcessor.class);
		scenarioProcessor = mock(ScenarioProcessor.class);
		stepProcessor = mock(StepProcessor.class);
		embeddedProcessor = mock(EmbeddedProcessor.class);
		exceptionParser = mock(ExceptionParser.class);
		extent = new ExtentReports();
		reportProperties = mock(ReportProperties.class);

		extentTestHeirarchy = new DefaultExtentTestHeirarchy(featureProcessor, scenarioProcessor, stepProcessor,
				embeddedProcessor, exceptionParser, extent, reportProperties);
	}

	@Test
	public void testNewFeatureCreation() {
		String uri = "classpath:features/urisample.feature";
		String name = "Feature Name";
		String description = "Feature Description";
		String tagName1 = "tag1";
		String tagName2 = "tag2";
		String startTime = "2020-06-20T12:30:15.654Z";
		String endTime = "2020-06-20T12:30:16.123Z";

		Feature feature = (new FeatureBuilder()).setUri(uri).setName(name).setDescription(description)
				.setStartTime(startTime).setEndTime(endTime).addFeatureTag(tagName1).addFeatureTag(tagName2).build();

		ExtentTest featureExtentTest = extentTestHeirarchy.createFeatureExtentTest(feature);
		com.aventstack.extentreports.model.Test model = featureExtentTest.getModel();

		assertEquals("Test name is not correct.", name, model.getName());
		assertEquals("Test description is not correct.", description, model.getDescription());
		assertEquals("Test categories are not correct.", Arrays.asList(tagName1, tagName2),
				model.getCategorySet().stream().map(t -> t.getName()).collect(Collectors.toList()));
		assertEquals("Test start time is not correct.", DateConverter.parseToDate(startTime), model.getStartTime());
		assertEquals("Test end time is not correct.", DateConverter.parseToDate(endTime), model.getEndTime());
	}

	@Test
	public void testNewFeatureWithNoTagsCreation() {
		String uri = "classpath:features/urisample.feature";
		String name = "Feature Name";
		String startTime = "2020-06-20T12:30:15.654Z";
		String endTime = "2020-06-20T12:30:16.123Z";

		Feature feature = (new FeatureBuilder()).setUri(uri).setName(name).setStartTime(startTime).setEndTime(endTime)
				.build();

		ExtentTest featureExtentTest = extentTestHeirarchy.createFeatureExtentTest(feature);
		com.aventstack.extentreports.model.Test model = featureExtentTest.getModel();

		assertEquals("Test name is not correct.", name, model.getName());
		assertTrue("Test categories are not empty.", model.getCategorySet().isEmpty());
		assertEquals("Test start time is not correct.", DateConverter.parseToDate(startTime), model.getStartTime());
		assertEquals("Test end time is not correct.", DateConverter.parseToDate(endTime), model.getEndTime());
	}

	@Test
	public void testRepeatFeatureCreation() {
		String name = "Feature Name";
		Feature feature = (new FeatureBuilder()).setUri("classpath:features/urisample.feature").setName(name)
				.setStartTime("2020-06-20T12:30:15.654Z").setEndTime("2020-06-20T12:30:16.123Z").build();

		ExtentTest featureExtentTest = extentTestHeirarchy.createFeatureExtentTest(feature);

		Feature featureRepeat = (new FeatureBuilder()).setUri("classpath:features/urisample.feature").setName(name)
				.build();

		ExtentTest featureExtentTestRepeat = extentTestHeirarchy.createFeatureExtentTest(featureRepeat);
		com.aventstack.extentreports.model.Test modelRepeat = featureExtentTest.getModel();

		assertEquals("New test creation with existing uri is wrong.", featureExtentTest, featureExtentTestRepeat);
		assertEquals("Test name is not correct.", name, modelRepeat.getName());
	}

	@Test
	public void testNewScenarioCreation() {
		ExtentTest parentFeatureExtentTest = extent.createTest(com.aventstack.extentreports.gherkin.model.Feature.class,
				"Parent Feature Test", "");

		String name = "Scenario Name";
		String description = "Scenario Description";
		String tagName1 = "tag1";
		String tagName2 = "tag2";

		Feature feature = (new FeatureBuilder()).setUri("classpath:features/urisample.feature").setName("Feature Name")
				.addScenario().setScenarioKeyword("scenario").setScenarioName(name).setScenarioDescription(description)
				.addScenarioTag(tagName1).addScenarioTag(tagName2).build();

		ExtentTest scenarioExtentTest = extentTestHeirarchy.createScenarioExtentNode(parentFeatureExtentTest,
				feature.getElements().get(0));
		com.aventstack.extentreports.model.Test model = scenarioExtentTest.getModel();

		assertEquals("Test name is not correct.", name, model.getName());
		assertEquals("Test description is not correct.", description, model.getDescription());
		assertEquals("Test categories are not correct.", Arrays.asList(tagName1, tagName2),
				model.getCategorySet().stream().map(t -> t.getName()).collect(Collectors.toList()));

		List<com.aventstack.extentreports.model.Test> featureTestChilds = parentFeatureExtentTest.getModel()
				.getChildren();
		assertEquals("Number of child nodes of Feature Extent Test should be 1", 1, featureTestChilds.size());
		assertEquals("Scenario Extent Test should be child of Feature Extent Test.", model, featureTestChilds.get(0));
	}

	@Test
	public void testMultipleNewScenarioCreation() {
		ExtentTest parentFeatureExtentTest = extent.createTest(com.aventstack.extentreports.gherkin.model.Feature.class,
				"Parent Feature Test", "");

		Feature feature = (new FeatureBuilder()).setUri("classpath:features/urisample.feature").setName("Feature Name")
				.addScenario().setScenarioKeyword("scenario").setScenarioName("Scenario Name").addScenario()
				.setScenarioKeyword("scenario").setScenarioName("Scenario Name Repeat").build();

		extentTestHeirarchy.createScenarioExtentNode(parentFeatureExtentTest, feature.getElements().get(0));
		extentTestHeirarchy.createScenarioExtentNode(parentFeatureExtentTest, feature.getElements().get(1));

		List<com.aventstack.extentreports.model.Test> featureTestChilds = parentFeatureExtentTest.getModel()
				.getChildren();
		assertEquals("Number of child nodes of Feature Extent Test should be 2", 2, featureTestChilds.size());
	}

	@Test
	public void testNewScenarioOutlineCreation() {
		ExtentTest parentFeatureExtentTest = extent.createTest(com.aventstack.extentreports.gherkin.model.Feature.class,
				"Parent Feature Test", "");

		String name = "Scenario Name";
		String description = "Scenario Description";

		Feature feature = (new FeatureBuilder()).setUri("classpath:features/urisample.feature").setName("Feature Name")
				.addScenario().setScenarioKeyword("scenario outline").setScenarioName(name)
				.setScenarioDescription(description).setScenarioId("Feature-Name;Scenario-Outline-Name;Examples-Name;2")
				.setScenarioUriStepLines("classpath:features/urisample.feature" + ":5:6:7").build();

		ExtentTest scenarioExtentTest = extentTestHeirarchy.createScenarioExtentNode(parentFeatureExtentTest,
				feature.getElements().get(0));
		com.aventstack.extentreports.model.Test model = scenarioExtentTest.getModel();

		assertEquals("Test name is not correct.", name, model.getName());
		assertEquals("Test description is not correct.", description, model.getDescription());
		assertTrue("Test categories are not empty.", model.getCategorySet().isEmpty());

		List<com.aventstack.extentreports.model.Test> scenarioOutlineTest = parentFeatureExtentTest.getModel()
				.getChildren();
		assertEquals("Number of child nodes of Feature Extent Test should be 1", 1, scenarioOutlineTest.size());
		assertEquals("Scenario Outline Test name is not correct.", "Scenario Outline Name",
				scenarioOutlineTest.get(0).getName());
		assertEquals("Scenario Outline Test description is not correct.", description,
				scenarioOutlineTest.get(0).getDescription());

		List<com.aventstack.extentreports.model.Test> scenarioTest = scenarioOutlineTest.get(0).getChildren();
		assertEquals("Number of child nodes of Scenario Outline Test should be 1", 1, scenarioTest.size());
		assertEquals("Scenario Outline Extent Test should be child of Scenario Extent Test.", model,
				scenarioTest.get(0));
	}

	@Test
	public void testMultipleScenarioForSingleScenarioOutlineCreation() {
		ExtentTest parentFeatureExtentTest = extent.createTest(com.aventstack.extentreports.gherkin.model.Feature.class,
				"Parent Feature Test", "");

		Feature feature = (new FeatureBuilder()).setUri("classpath:features/urisample.feature").setName("Feature Name")
				.addScenario().setScenarioKeyword("scenario outline").setScenarioName("Scenario Outline Name")
				.setScenarioId("Feature-Name;Scenario-Outline-Name;Examples-Name;2")
				.setScenarioUriStepLines("classpath:features/urisample.feature" + ":5:6:7").addScenario()
				.setScenarioKeyword("scenario outline").setScenarioName("Scenario Outline Name Repeat")
				.setScenarioId("Feature-Name;Scenario-Outline-Name;Examples-Name;3")
				.setScenarioUriStepLines("classpath:features/urisample.feature" + ":5:6:7").build();

		extentTestHeirarchy.createScenarioExtentNode(parentFeatureExtentTest, feature.getElements().get(0));
		extentTestHeirarchy.createScenarioExtentNode(parentFeatureExtentTest, feature.getElements().get(1));

		List<com.aventstack.extentreports.model.Test> scenarioOutlineTest = parentFeatureExtentTest.getModel()
				.getChildren();
		assertEquals("Number of child nodes of Feature Extent Test should be 1", 1, scenarioOutlineTest.size());
		List<com.aventstack.extentreports.model.Test> scenarioTest = scenarioOutlineTest.get(0).getChildren();
		assertEquals("Number of child nodes of Scenario Outline Test should be 2", 2, scenarioTest.size());
	}

	@Test
	public void testMultipleScenarioOutlineCreation() {
		ExtentTest parentFeatureExtentTest = extent.createTest(com.aventstack.extentreports.gherkin.model.Feature.class,
				"Parent Feature Test", "");

		Feature feature = (new FeatureBuilder()).setUri("classpath:features/urisample.feature").setName("Feature Name")
				.addScenario().setScenarioKeyword("scenario outline").setScenarioName("Scenario Outline Name First")
				.setScenarioId("Feature-Name;Scenario-Outline-Name;Examples-Name;10")
				.setScenarioUriStepLines("classpath:features/urisample.feature" + ":15:16:17").addScenario()
				.setScenarioKeyword("scenario outline").setScenarioName("Scenario Outline Name Second")
				.setScenarioId("Feature-Name;Scenario-Outline-Name;Examples-Name;20")
				.setScenarioUriStepLines("classpath:features/urisample.feature" + ":25:26:27").build();

		extentTestHeirarchy.createScenarioExtentNode(parentFeatureExtentTest, feature.getElements().get(0));
		extentTestHeirarchy.createScenarioExtentNode(parentFeatureExtentTest, feature.getElements().get(1));

		List<com.aventstack.extentreports.model.Test> scenarioOutlineTest = parentFeatureExtentTest.getModel()
				.getChildren();
		assertEquals("Number of child nodes of Feature Extent Test should be 2", 2, scenarioOutlineTest.size());

		List<com.aventstack.extentreports.model.Test> scenarioFirstTest = scenarioOutlineTest.get(0).getChildren();
		assertEquals("Number of child nodes of First Scenario Outline Test should be 1", 1, scenarioFirstTest.size());

		List<com.aventstack.extentreports.model.Test> scenarioSecondTest = scenarioOutlineTest.get(1).getChildren();
		assertEquals("Number of child nodes of Second Scenario Outline Test should be 1", 1, scenarioSecondTest.size());
	}

	@Test
	public void testSinglePassedStepCreation() {
		ExtentTest featureExtentTest = extent.createTest(com.aventstack.extentreports.gherkin.model.Feature.class,
				"Feature Test", "");

		ExtentTest scenarioExtentTest = featureExtentTest
				.createNode(com.aventstack.extentreports.gherkin.model.Scenario.class, "Scenario Test", "");

		String stepText = "Step text to test";
		String keyword = "Given ";
		String location = "stepdefs.Stepdefs.step()";
		String status = "passed";

		Feature feature = (new FeatureBuilder()).setUri("classpath:features/urisample.feature").setName("Feature Name")
				.addScenario().setScenarioKeyword("scenario").setScenarioName("Scenario Name").addStep()
				.setStepKeyword(keyword).setStepText(stepText).setStepLocation(location).setStepResult(status).build();

		ExtentTest stepExtentTest = extentTestHeirarchy.createStepExtentNode(scenarioExtentTest,
				feature.getElements().get(0).getSteps().get(0));

		List<com.aventstack.extentreports.model.Test> scenarioChilds = scenarioExtentTest.getModel().getChildren();
		assertEquals("Number of child nodes of scenario should be 1.", 1, scenarioChilds.size());

		com.aventstack.extentreports.model.Test stepTest = stepExtentTest.getModel();
		assertEquals("Step keyword is not correct.",
				"class com.aventstack.extentreports.gherkin.model." + keyword.trim(), stepTest.getBddType().toString());
		assertEquals("Step text is not correct.", keyword + stepText, stepTest.getName());
		assertEquals("Step definition method location is not correct.", location, stepTest.getDescription());
		assertEquals("Step status is not correct.", "PASS", stepTest.getStatus().name());
	}

	@Test
	public void testMultipleSkippedAndFailedStepCreation() {
		ExtentTest featureExtentTest = extent.createTest(com.aventstack.extentreports.gherkin.model.Feature.class,
				"Feature Test", "");
		ExtentTest scenarioExtentTest = featureExtentTest
				.createNode(com.aventstack.extentreports.gherkin.model.Scenario.class, "Scenario Test", "");

		Feature feature = (new FeatureBuilder()).setUri("classpath:features/urisample.feature").setName("Feature Name")
				.addScenario().setScenarioKeyword("scenario").setScenarioName("Scenario Name").addStep()
				.setStepKeyword("When ").setStepText("Step text for \u0027when\\u0027 step")
				.setStepLocation("stepdefs.Stepdefs.stepWhen(java.lang.String)").setStepResult("failed")
				.setStepErrorMessage("Error Message").addStep().setStepKeyword("Then ")
				.setStepText("Step text for \u0027then\\u0027 step")
				.setStepLocation("stepdefs.Stepdefs.stepThen(java.lang.String)").setStepResult("skipped")
				.setStepErrorMessage("Skipped Message").build();

		ExtentTest stepWhenExtentTest = extentTestHeirarchy.createStepExtentNode(scenarioExtentTest,
				feature.getElements().get(0).getSteps().get(0));
		assertEquals("Step status is not correct.", "FAIL", stepWhenExtentTest.getStatus().name());

		ExtentTest stepThenExtentTest = extentTestHeirarchy.createStepExtentNode(scenarioExtentTest,
				feature.getElements().get(0).getSteps().get(1));
		assertEquals("Step status is not correct.", "SKIP", stepThenExtentTest.getStatus().name());

		List<com.aventstack.extentreports.model.Test> scenarioChilds = scenarioExtentTest.getModel().getChildren();
		assertEquals("Number of child nodes of scenario should be 2.", 2, scenarioChilds.size());
	}

	@Test
	public void testStepWithMessageCreation() {
		ExtentTest featureExtentTest = extent.createTest(com.aventstack.extentreports.gherkin.model.Feature.class,
				"Feature Test", "");
		ExtentTest scenarioExtentTest = featureExtentTest
				.createNode(com.aventstack.extentreports.gherkin.model.Scenario.class, "Scenario Test", "");

		Feature feature = (new FeatureBuilder()).setUri("classpath:features/urisample.feature").setName("Feature Name")
				.addScenario().setScenarioKeyword("scenario").setScenarioName("Scenario Name").addStep()
				.setStepKeyword("Given ").setStepText("Step text to test").setStepLocation("stepdefs.Stepdefs.step()")
				.setStepResult("passed").setStepOutput(Arrays.asList("Information One", "Information Two")).build();

		ExtentTest stepExtentTest = extentTestHeirarchy.createStepExtentNode(scenarioExtentTest,
				feature.getElements().get(0).getSteps().get(0));
		List<Log> logs = stepExtentTest.getModel().getLogs();
		assertTrue("Step message is not saved in logs.", logs.stream().map(Log::getDetails).collect(Collectors.toList())
				.containsAll(Arrays.asList("Information One", "Information Two")));
	}

	@Test
	public void testSingleHookCreation() {
		ExtentTest featureExtentTest = extent.createTest(com.aventstack.extentreports.gherkin.model.Feature.class,
				"Feature Test", "");
		ExtentTest scenarioExtentTest = featureExtentTest
				.createNode(com.aventstack.extentreports.gherkin.model.Scenario.class, "Scenario Test", "");

		String location = "stepdefs.Stepdefs.step()";

		when(reportProperties.isDisplayAllHooks()).thenReturn(true);
		Scenario scenario = mock(Scenario.class);
		Hook hook = mock(Hook.class, RETURNS_DEEP_STUBS);
		when(scenario.getBefore()).thenReturn(Arrays.asList(hook));
		when(hook.getResult().getStatus()).thenReturn("passed");
		when(hook.getMatch().getLocation()).thenReturn(location);
		when(hook.getHookType()).thenReturn(HookType.BEFORE);

		List<ExtentTest> hooksExtentTest = extentTestHeirarchy.createBeforeHookExtentNodes(scenarioExtentTest,
				scenario);
		List<com.aventstack.extentreports.model.Test> scenarioHooks = scenarioExtentTest.getModel().getChildren();
		assertEquals("Number of hooks of scenario should be 1.", 1, scenarioHooks.size());
		assertEquals("Hook step definition method is not correct.", location,
				hooksExtentTest.get(0).getModel().getName());
		assertEquals("Hook status is not correct.", "PASS", hooksExtentTest.get(0).getModel().getStatus().name());
	}

	@Test
	public void testMultipleHooksCreation() {
		ExtentTest featureExtentTest = extent.createTest(com.aventstack.extentreports.gherkin.model.Feature.class,
				"Feature Test", "");
		ExtentTest scenarioExtentTest = featureExtentTest
				.createNode(com.aventstack.extentreports.gherkin.model.Scenario.class, "Scenario Test", "");

		when(reportProperties.isDisplayAllHooks()).thenReturn(true);

		Scenario scenario = mock(Scenario.class);
		Hook beforeHook = mock(Hook.class, RETURNS_DEEP_STUBS);
		when(scenario.getBefore()).thenReturn(Arrays.asList(beforeHook));
		when(beforeHook.getResult().getStatus()).thenReturn("passed");
		when(beforeHook.getMatch().getLocation()).thenReturn("stepdefs.Stepdefs.step()");
		when(beforeHook.getHookType()).thenReturn(HookType.BEFORE);

		Hook afterHook = mock(Hook.class, RETURNS_DEEP_STUBS);
		when(scenario.getAfter()).thenReturn(Arrays.asList(afterHook));
		when(afterHook.getResult().getStatus()).thenReturn("passed");
		when(afterHook.getMatch().getLocation()).thenReturn("stepdefs.Stepdefs.step()");
		when(afterHook.getHookType()).thenReturn(HookType.AFTER);

		Step step = mock(Step.class);
		Hook beforeStepHook = mock(Hook.class, RETURNS_DEEP_STUBS);
		when(step.getBefore()).thenReturn(Arrays.asList(beforeStepHook));
		when(beforeStepHook.getResult().getStatus()).thenReturn("passed");
		when(beforeStepHook.getMatch().getLocation()).thenReturn("stepdefs.Stepdefs.step()");
		when(beforeStepHook.getHookType()).thenReturn(HookType.BEFORE_STEP);

		Hook afterStepHook = mock(Hook.class, RETURNS_DEEP_STUBS);
		when(step.getAfter()).thenReturn(Arrays.asList(afterStepHook));
		when(afterStepHook.getResult().getStatus()).thenReturn("passed");
		when(afterStepHook.getMatch().getLocation()).thenReturn("stepdefs.Stepdefs.step()");
		when(afterStepHook.getHookType()).thenReturn(HookType.AFTER_STEP);

		extentTestHeirarchy.createBeforeHookExtentNodes(scenarioExtentTest, scenario);
		extentTestHeirarchy.createBeforeStepHookExtentNodes(scenarioExtentTest, step);
		extentTestHeirarchy.createAfterStepHookExtentNodes(scenarioExtentTest, step);
		extentTestHeirarchy.createAfterHookExtentNodes(scenarioExtentTest, scenario);

		List<com.aventstack.extentreports.model.Test> scenarioHooks = scenarioExtentTest.getModel().getChildren();
		assertEquals("Number of hooks of scenario should be 4.", 4, scenarioHooks.size());
	}

}
