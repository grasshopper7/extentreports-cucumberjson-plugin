package tech.grasshopper.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import tech.grasshopper.exception.ExtentReportsCucumberPluginException;
import tech.grasshopper.logging.ExtentReportsCucumberLogger;
import tech.grasshopper.pojo.Argument;
import tech.grasshopper.pojo.Feature;
import tech.grasshopper.pojo.Hook;
import tech.grasshopper.pojo.Scenario;
import tech.grasshopper.pojo.Step;
import tech.grasshopper.processor.EmbeddedProcessor;

public class JsonFileConverterTest {

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	private ExtentReportsCucumberLogger logger;
	private JsonFileConverter jsonFileConverter;
	private EmbeddedProcessor embeddedProcessor;

	@Before
	public void setup() {
		logger = mock(ExtentReportsCucumberLogger.class);
		embeddedProcessor = mock(EmbeddedProcessor.class);
		jsonFileConverter = new JsonFileConverter(embeddedProcessor, logger);
	}

	@Test
	public void testJsonReportWithNoFeature() {
		File[] jsonFiles = new File[2];
		InputStream is = JsonFileConverter.class.getResourceAsStream("data1.json");

		try {
			jsonFiles[0] = testFolder.newFile("testReportOne.json");
			jsonFiles[1] = testFolder.newFile("testReportTwo.json");
			Files.copy(is, jsonFiles[0].toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			fail("Error in creating temporary json report file");
		}
		List<Path> paths = new ArrayList<>();
		paths.add(Paths.get(jsonFiles[0].getAbsolutePath()));
		paths.add(Paths.get(jsonFiles[1].getAbsolutePath()));

		jsonFileConverter.retrieveFeaturesFromReport(paths);

		verify(logger, times(1)).warn("Skipping json report at '" + jsonFiles[1].getAbsolutePath()
				+ "', parsing json report file returned no Feature pojo.");
	}

	@Test
	public void testExecutionWithNoFeature() {
		File[] jsonFiles = new File[2];

		try {
			jsonFiles[0] = testFolder.newFile("testReportOne.json");
			jsonFiles[1] = testFolder.newFile("testReportTwo.json");
		} catch (IOException e) {
			fail("Error in creating temporary json report file");
		}
		List<Path> paths = new ArrayList<>();
		paths.add(Paths.get(jsonFiles[0].getAbsolutePath()));
		paths.add(Paths.get(jsonFiles[1].getAbsolutePath()));

		Exception exception = assertThrows(
				"The exception thrown should be an instance of ExtentReportsCucumberPluginException.",
				ExtentReportsCucumberPluginException.class, () -> {
					jsonFileConverter.retrieveFeaturesFromReport(paths);
				});

		assertEquals(
				"No Feature found in report. Stopping report creation. "
						+ "Check the 'extentreport.cucumberJsonReportDirectory' plugin configuration.",
				exception.getMessage());
	}

	@Test
	public void testParsedReportDetails() {
		File jsonFile = null;
		InputStream is = JsonFileConverter.class.getResourceAsStream("data1.json");

		try {
			jsonFile = testFolder.newFile("testReportOne.json");
			Files.copy(is, jsonFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			fail("Error in creating temporary json report file");
		}
		List<Path> paths = new ArrayList<>();
		paths.add(Paths.get(jsonFile.getAbsolutePath()));

		List<Feature> features = jsonFileConverter.retrieveFeaturesFromReport(paths);

		assertEquals("Number of features should be equal to '1'.", 1, features.size());
		Feature feature = features.get(0);
		assertEquals("Feature id should be 'scenarios-feature-file'.", "scenarios-feature-file", feature.getId());
		assertEquals("Feature name should be 'Scenarios feature file'.", "Scenarios feature file", feature.getName());
		assertEquals("Feature description should be 'Scenarios feature file' description.",
				"Scenarios feature file description", feature.getDescription());
		assertEquals("Feature keyword should be 'Feature'.", "Feature", feature.getKeyword());
		assertEquals("Feature uri should be 'classpath:stepdefs/scenarios.feature'.",
				"classpath:stepdefs/scenarios.feature", feature.getUri());
		assertEquals("Feature line location should be '3'.", 3, feature.getLine());

		assertEquals("Number of tags on feature should be '1'", 1, feature.getTags().size());
		assertEquals("Tag name on feature should be '@TagFeature'", "@TagFeature", feature.getTags().get(0).getName());

		assertEquals("Number of scenarios should be equal to '2'.", 2, feature.getElements().size());
		Scenario scenario = feature.getElements().get(0);
		assertEquals("Scenario id should be 'scenarios-feature-file;scenario-number-one'.",
				"scenarios-feature-file;scenario-number-one", scenario.getId());
		assertEquals("Scenario name should be 'Scenario Number One'.", "Scenario Number One", scenario.getName());
		assertEquals("Scenario description should be 'blank.", "", scenario.getDescription());
		assertEquals("Scenario keyword should be 'Scenario'.", "Scenario", scenario.getKeyword());
		assertEquals("Scenario type should be 'Scenario'.", "scenario", scenario.getType());
		assertEquals("Scenario line location should be '6'.", 6, scenario.getLine());
		assertEquals("Scenario start timestamp should be '2020-06-18T12:35:46.659Z'.", "2020-06-18T12:35:46.659Z",
				scenario.getStartTimestamp());

		assertEquals("Number of before hook should be equal to '1'.", 1, scenario.getBefore().size());
		Hook beforeHook = scenario.getBefore().get(0);
		assertEquals("Hook duration should be equal to '252000000'.", 252000000, beforeHook.getResult().getDuration());
		assertEquals("Hook result should be equal to 'passed'.", "passed", beforeHook.getResult().getStatus());
		assertEquals("Hook location should be equal to 'stepdefs.Stepdefs.before()'.", "stepdefs.Stepdefs.before()",
				beforeHook.getMatch().getLocation());

		assertEquals("Number of steps should be equal to '3'.", 3, scenario.getSteps().size());
		Step step = scenario.getSteps().get(0);
		assertEquals("Step name should be 'Write a 'given' step with precondition in 'Scenario One'.",
				"Write a 'given' step with precondition in 'Scenario One'", step.getName());
		assertEquals("Step line location should be '7'.", 7, step.getLine());
		assertEquals("Step keyword should be 'Given'.", "Given", step.getKeyword().trim());
		assertEquals("Step duration should be equal to '70000000'.", 70000000, step.getResult().getDuration());
		assertEquals("Step result should be equal to 'passed'.", "passed", step.getResult().getStatus());
		assertEquals("Step location should be equal to 'stepdefs.Stepdefs.step(java.lang.String,java.lang.String)'.",
				"stepdefs.Stepdefs.step(java.lang.String,java.lang.String)", step.getMatch().getLocation());

		assertEquals("Number of step arguments should be equal to '2'.", 2, step.getMatch().getArguments().size());
		Argument argument = step.getMatch().getArguments().get(0);
		assertEquals("Argument value be 'given'.", "'given'", argument.getVal());
		assertEquals("Argument offset should be '8'.", 8, argument.getOffset());
	}

	@Test
	public void testMultipleJsonReport() {
		File[] jsonFiles = new File[2];
		InputStream isone = JsonFileConverter.class.getResourceAsStream("data1.json");
		InputStream istwo = JsonFileConverter.class.getResourceAsStream("data2.json");

		try {
			jsonFiles[0] = testFolder.newFile("testReportOne.json");
			jsonFiles[1] = testFolder.newFile("testReportTwo.json");
			Files.copy(isone, jsonFiles[0].toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(istwo, jsonFiles[1].toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			fail("Error in creating temporary json report file");
		}
		List<Path> paths = new ArrayList<>();
		paths.add(Paths.get(jsonFiles[0].getAbsolutePath()));
		paths.add(Paths.get(jsonFiles[1].getAbsolutePath()));

		List<Feature> features = jsonFileConverter.retrieveFeaturesFromReport(paths);
		assertEquals("Number of features should be equal to '3'.", 3, features.size());		
		int scenarios = features.stream().mapToInt(f -> f.getElements().size()).sum();
		assertEquals("Number of scenarios should be equal to '5'.", 5, scenarios);
	}
}
