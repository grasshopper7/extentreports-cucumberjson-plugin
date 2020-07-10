package tech.grasshopper;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import org.junit.Before;
import org.junit.Test;

import tech.grasshopper.exception.ExtentReportsCucumberPluginException;
import tech.grasshopper.json.JsonFileConverter;
import tech.grasshopper.json.JsonPathCollector;
import tech.grasshopper.logging.ExtentReportsCucumberLogger;
import tech.grasshopper.processor.EmbeddedProcessor;
import tech.grasshopper.properties.ReportProperties;
import tech.grasshopper.reporters.ReporterInitializer;
import tech.grasshopper.test.ExtentTestManager;

public class ExtentReportsCucumberPluginTest {

	private ExtentReportsCucumberPlugin extentReportsCucumberPlugin;
	private JsonPathCollector jsonPathCollector;
	private JsonFileConverter jsonFileConverter;
	private ReportProperties reportProperties;
	private ReporterInitializer reportInitializer;
	private ExtentTestManager extentTestManager;
	private EmbeddedProcessor embeddedProcessor;
	private ExtentReportsCucumberLogger logger;

	@Before
	public void setup() {

		jsonPathCollector = mock(JsonPathCollector.class);
		jsonFileConverter = mock(JsonFileConverter.class);
		reportProperties = mock(ReportProperties.class);
		reportInitializer = mock(ReporterInitializer.class);
		extentTestManager = mock(ExtentTestManager.class);
		embeddedProcessor = mock(EmbeddedProcessor.class);
		logger = mock(ExtentReportsCucumberLogger.class);

		extentReportsCucumberPlugin = new ExtentReportsCucumberPlugin(jsonPathCollector, jsonFileConverter,
				reportProperties, reportInitializer, extentTestManager, embeddedProcessor, logger);
	}
	
	@Test
	public void testBuildContinuesOnPluginFailure() {				
		when(jsonPathCollector.retrieveFilePaths(null)).thenThrow(new ExtentReportsCucumberPluginException("failure"));
		extentReportsCucumberPlugin.execute();		
		verify(logger, times(1)).error("STOPPING EXTENT REPORT GENERATION - failure");
		verify(logger, never()).info("FINISHED EXTENT REPORT GENERATION PLUGIN");
	}
	
	@Test
	public void testPluginExecution() {	
		extentReportsCucumberPlugin.execute();		
		verify(logger, never()).error(argThat(startsWith("STOPPING EXTENT REPORT GENERATION - ")));
		verify(logger, times(1)).info("FINISHED EXTENT REPORT GENERATION PLUGIN");
	}
}
