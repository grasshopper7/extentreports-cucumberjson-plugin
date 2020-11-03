package tech.grasshopper.test.heirarchy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.GherkinKeyword;
import com.aventstack.extentreports.model.Test;

import tech.grasshopper.pojo.Embedded;
import tech.grasshopper.pojo.Feature;
import tech.grasshopper.pojo.Hook;
import tech.grasshopper.pojo.Result;
import tech.grasshopper.pojo.Scenario;
import tech.grasshopper.pojo.Step;
import tech.grasshopper.processor.EmbeddedProcessor;
import tech.grasshopper.processor.ErrorMessageProcessor;
import tech.grasshopper.processor.FeatureProcessor;
import tech.grasshopper.processor.ScenarioProcessor;
import tech.grasshopper.processor.StepProcessor;
import tech.grasshopper.properties.ReportProperties;

public abstract class ExtentTestHeirarchy {

	protected List<Feature> features;
	protected ExtentReports extent;
	protected Map<String, ExtentTest> uriFeatureTestMap = new HashMap<>();
	protected Map<String, ExtentTest> uriLinesScenarioOutlineTestMap = new HashMap<>();

	protected FeatureProcessor featureProcessor;
	protected ScenarioProcessor scenarioProcessor;
	protected StepProcessor stepProcessor;
	protected ErrorMessageProcessor errorMessageProcessor;
	protected EmbeddedProcessor embeddedProcessor;
	private ReportProperties reportProperties;

	@Inject
	protected ExtentTestHeirarchy(FeatureProcessor featureProcessor, ScenarioProcessor scenarioProcessor,
			StepProcessor stepProcessor, ErrorMessageProcessor errorMessageProcessor,
			EmbeddedProcessor embeddedProcessor, ReportProperties reportProperties) {
		this.featureProcessor = featureProcessor;
		this.scenarioProcessor = scenarioProcessor;
		this.stepProcessor = stepProcessor;
		this.errorMessageProcessor = errorMessageProcessor;
		this.embeddedProcessor = embeddedProcessor;
		this.reportProperties = reportProperties;

	}

	protected ExtentTestHeirarchy(FeatureProcessor featureProcessor, ScenarioProcessor scenarioProcessor,
			StepProcessor stepProcessor, ErrorMessageProcessor errorMessageProcessor,
			EmbeddedProcessor embeddedProcessor, ExtentReports extent, ReportProperties reportProperties) {
		this(featureProcessor, scenarioProcessor, stepProcessor, errorMessageProcessor, embeddedProcessor,
				reportProperties);
		this.extent = extent;
	}

	public abstract void createTestHeirarchy(List<Feature> features, ExtentReports extent);

	public ExtentTest createFeatureExtentTest(Feature feature) {
		String uri = feature.getUri();
		if (uriFeatureTestMap.containsKey(uri))
			return uriFeatureTestMap.get(uri);

		ExtentTest featureExtentTest = extent.createTest(com.aventstack.extentreports.gherkin.model.Feature.class,
				feature.getName(), feature.getDescription());
		uriFeatureTestMap.put(uri, featureExtentTest);
		feature.getTags().forEach(t -> featureExtentTest.assignCategory(t.getName()));
		Test test = featureExtentTest.getModel();
		test.setStartTime(feature.getStartTime());
		test.setEndTime(feature.getEndTime());
		feature.setTestId(test.getId());
		return featureExtentTest;
	}

	public ExtentTest createScenarioExtentNode(ExtentTest parentExtentTest, Scenario scenario) {
		if (scenario.getKeyword().equalsIgnoreCase("Scenario Outline"))
			parentExtentTest = createScenarioOutlineExtentNode(parentExtentTest, scenario);

		ExtentTest scenarioExtentTest = parentExtentTest.createNode(
				com.aventstack.extentreports.gherkin.model.Scenario.class, scenario.getName(),
				scenario.getDescription());
		scenario.getTags().forEach(t -> scenarioExtentTest.assignCategory(t.getName()));
		Test test = scenarioExtentTest.getModel();
		test.setStartTime(scenario.getStartTime());
		test.setEndTime(scenario.getEndTime());
		scenario.setTestId(test.getId());
		return scenarioExtentTest;
	}

	public ExtentTest createScenarioOutlineExtentNode(ExtentTest parentExtentTest, Scenario scenarioOutline) {
		String uriStepLines = scenarioOutline.getUriStepLines();
		if (uriLinesScenarioOutlineTestMap.containsKey(scenarioOutline.getUriStepLines()))
			return uriLinesScenarioOutlineTestMap.get(uriStepLines);

		ExtentTest scenarioOutlineExtentTest = parentExtentTest.createNode(
				com.aventstack.extentreports.gherkin.model.ScenarioOutline.class,
				parseTestNameFromId(scenarioOutline.getId()), scenarioOutline.getDescription());
		uriLinesScenarioOutlineTestMap.put(uriStepLines, scenarioOutlineExtentTest);
		return scenarioOutlineExtentTest;
	}

	private String parseTestNameFromId(String id) {
		String[] splits = id.split(";");
		if (splits.length != 4)
			return id;

		String[] name = splits[1].split("-");
		return Arrays.stream(name).map(t -> {
			if (t.length() == 1)
				return t.toUpperCase();
			return t.substring(0, 1).toUpperCase() + t.substring(1);
		}).collect(Collectors.joining(" "));
	}

	public ExtentTest createHookExtentNode(ExtentTest parentExtentTest, Hook hook) {
		ExtentTest hookExtentTest = parentExtentTest.createNode(
				com.aventstack.extentreports.gherkin.model.Asterisk.class, hook.getMatch().getLocation(),
				hook.getHookType().toString().toUpperCase());

		hook.setTestId(hookExtentTest.getModel().getId());
		hook.getOutput().forEach(o -> hookExtentTest.info(o));
		updateTestEmbeddings(hookExtentTest, hook.getEmbeddings());
		updateTestLogStatus(hookExtentTest, hook.getResult());
		Test test = hookExtentTest.getModel();
		test.setStartTime(hook.getStartTime());
		test.setEndTime(hook.getEndTime());
		return hookExtentTest;
	}

	public List<ExtentTest> createHookExtentNode(ExtentTest parentExtentTest, List<Hook> hooks) {
		List<ExtentTest> hookTests = new ArrayList<>();

		if (!reportProperties.isDisplayAllHooks())
			hooks.removeIf(h -> h.getEmbeddings().isEmpty() && h.getOutput().isEmpty());

		hooks.forEach(h -> hookTests.add(createHookExtentNode(parentExtentTest, h)));
		return hookTests;
	}

	public List<ExtentTest> createBeforeHookExtentNodes(ExtentTest parentExtentTest, Scenario scenario) {
		return createHookExtentNode(parentExtentTest, scenario.getBefore());
	}

	public List<ExtentTest> createAfterHookExtentNodes(ExtentTest parentExtentTest, Scenario scenario) {
		return createHookExtentNode(parentExtentTest, scenario.getAfter());
	}

	public List<ExtentTest> createBeforeStepHookExtentNodes(ExtentTest parentExtentTest, Step step) {
		return createHookExtentNode(parentExtentTest, step.getBefore());
	}

	public List<ExtentTest> createAfterStepHookExtentNodes(ExtentTest parentExtentTest, Step step) {
		return createHookExtentNode(parentExtentTest, step.getAfter());
	}

	public ExtentTest createStepExtentNode(ExtentTest parentExtentTest, Step step) {
		ExtentTest stepExtentTest = null;
		GherkinKeyword keyword = null;
		try {
			// Default set to And
			keyword = new GherkinKeyword("And");
			keyword = new GherkinKeyword(step.getKeyword().trim());
		} catch (ClassNotFoundException e) {
		}

		stepExtentTest = parentExtentTest.createNode(keyword, step.getKeyword() + step.getName(),
				step.getMatch().getLocation());
		step.setTestId(stepExtentTest.getModel().getId());

		Test test = stepExtentTest.getModel();
		test.setStartTime(step.getStartTime());
		test.setEndTime(step.getEndTime());

		if (step.getRows().size() > 0)
			stepExtentTest.pass(step.getDataTableMarkup());
		if (step.getDocStringMarkup() != null)
			stepExtentTest.pass(step.getDocStringMarkup());
		for (String msg : step.getOutput())
			stepExtentTest.info(msg);
		if (step.getEmbeddings().size() > 0)
			updateTestEmbeddings(stepExtentTest, step.getEmbeddings());

		updateTestLogStatus(stepExtentTest, step.getResult());
		return stepExtentTest;
	}

	public void updateTestEmbeddings(ExtentTest test, List<Embedded> embeddings) {
		embeddedProcessor.updateExtentTestWithEmbedding(test, embeddings);
	}

	public void updateTestLogStatus(ExtentTest test, Result result) {
		String stepStatus = result.getStatus();

		if (stepStatus.equalsIgnoreCase("failed")) {
			Throwable throwInstance = errorMessageProcessor.createThrowableObject(result.getErrorMessage());
			test.fail(throwInstance);
		} else if (stepStatus.equalsIgnoreCase("passed"))
			test.pass("");
		else if ((stepStatus.equalsIgnoreCase("undefined") || stepStatus.equalsIgnoreCase("pending"))
				&& reportProperties.isStrictCucumber6Behavior())
			test.fail("");
		else
			test.skip("");
	}
}
