package tech.grasshopper.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import tech.grasshopper.exception.ExtentReportsCucumberPluginException;

public class JsonPathCollectorTest {

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	private JsonPathCollector jsonPathCollector;

	@Before
	public void setup() {
		jsonPathCollector = new JsonPathCollector();
	}

	@Test
	public void testInvalidDirectoryPath() {
		Exception exception = assertThrows(
				"The exception thrown should be an instance of ExtentReportsCucumberPluginException.",
				ExtentReportsCucumberPluginException.class, () -> {
					jsonPathCollector.retrieveFilePaths("invalid");
					;
				});
		assertEquals(
				"Unable to navigate Cucumber Json report folders. Stopping report creation. "
						+ "Check the 'extentreport.cucumberJsonReportDirectory' plugin configuration.",
				exception.getMessage());
	}

	@Test
	public void testValidDirectoryPathWithNoJsonReport() {
		Exception exception = assertThrows(
				"The exception thrown should be an instance of ExtentReportsCucumberPluginException.",
				ExtentReportsCucumberPluginException.class, () -> {
					jsonPathCollector.retrieveFilePaths(testFolder.getRoot().getAbsolutePath());
					;
				});
		assertEquals(
				"No Cucumber Json Report found. Stopping report creation. "
						+ "Check the 'extentreport.cucumberJsonReportDirectory' plugin configuration.",
				exception.getMessage());
	}

	@Test
	public void testDirectoryPathWithJsonFile()  {
		String[] jsonFiles = {"testReportOne.json", "testReportTwo.json"};
		try {
			testFolder.newFile(jsonFiles[0]);
			testFolder.newFile(jsonFiles[1]);
		} catch (IOException e) {
			fail("Error in creating temporary json report file");
		}
		List<Path> paths = jsonPathCollector.retrieveFilePaths(testFolder.getRoot().getAbsolutePath());
		assertEquals(2,paths.size());
		List<String> fileNames = paths.stream().map(p -> p.getFileName().toString()).collect(Collectors.toList());
		assertEquals(fileNames, Arrays.asList(jsonFiles));
	}
}
