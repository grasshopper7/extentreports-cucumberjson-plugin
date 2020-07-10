package tech.grasshopper.properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import tech.grasshopper.logging.ExtentReportsCucumberLogger;

public class ReportPropertiesTest {

	private ReportProperties reportProperties;
	private ExtentReportsCucumberLogger logger;
	
	@Before
	public void setup() {
		logger = mock(ExtentReportsCucumberLogger.class);		
		reportProperties = new ReportProperties(logger);		
	}
	
	@Test
	public void testReadDefaultAndProjectPropertiesFiles() {
		Path projectProperty = Paths.get("src/test/resources/tech/grasshopper/properties");
		reportProperties.loadPropertyFiles(projectProperty.toAbsolutePath().toString(), "project-extent.properties");
		
		assertEquals("Value of 'class' for 'ReporterOne' is wrong.", "com.reporter.ReporterOne", reportProperties.getReportClassNameProperty("reporterone"));
		assertEquals("Value of 'start' for 'ReporterOne' is wrong.", "true", reportProperties.getReportStartProperty("reporterone"));
		assertEquals("Value of 'out' for 'ReporterOne' is wrong.", "test-output/ReporterOne", reportProperties.getReportOutProperty("reporterone"));
		assertEquals("Value of 'config' for 'ReporterOne' is wrong.", "src/test/resources/extent-config.xml", reportProperties.getReportConfigProperty("reporterone"));
		assertEquals("Value of screenshot directory is wrong.", "test-output/", reportProperties.getReportScreenshotLocation());
	}
	
	@Test
	public void testOverwriteDefaultPropertiesFileFromProjectPropertiesFile() {
		Path projectProperty = Paths.get("src/test/resources/tech/grasshopper/properties");
		reportProperties.loadPropertyFiles(projectProperty.toAbsolutePath().toString(), "project-extent-overwrite.properties");
		
		assertEquals("Value of 'start' for 'ReporterOne' should be overwritten with 'false' value.", "false", reportProperties.getReportStartProperty("reporterone"));
	}
	
	@Test
	public void testAbsentProjectPropertiesFile() {
		Path projectProperty = Paths.get("src/test/resources/tech/grasshopper/invalid");
		reportProperties.loadPropertyFiles(projectProperty.toAbsolutePath().toString(), "invalid.properties");
		
		verify(logger, times(1)).warn("Skipping reading project extent properties as file not found at location - " + projectProperty.toAbsolutePath().toString()  + "/" + "invalid.properties" + ".");
		assertEquals("Value of 'class' for 'ReporterOne' is wrong.", "com.reporter.ReporterOne", reportProperties.getReportClassNameProperty("reporterone"));
		assertEquals("Value of 'start' for 'ReporterOne' is wrong.", "true", reportProperties.getReportStartProperty("reporterone"));
		assertEquals("Value of 'out' for 'ReporterOne' is wrong.", "test-output/ReporterOne", reportProperties.getReportOutProperty("reporterone"));
		assertEquals("Value of 'config' for 'ReporterOne' is wrong.", "", reportProperties.getReportConfigProperty("reporterone"));
	}
	
	@Test
	public void testReporterClassMappings() {
		Properties props = new Properties();
		props.put("extent.reporter.reporterone.class", "com.reporter.ReporterOne");
		props.put("extent.reporter.reportertwo.class", "com.reporter.ReporterTwo");
		
		reportProperties = new ReportProperties(props);
		Map<String, String> mappings = reportProperties.retrieveReportIdToClassNameMappings();
		assertEquals("Count of reporter 'class' is wrong.", 2, mappings.size());
		assertEquals("Reporter keys are missing.",  true, mappings.containsKey("reporterone") && mappings.containsKey("reportertwo"));
		assertEquals("Value of reporter 'ReporterOne' is wrong.",  "com.reporter.ReporterOne", mappings.get("reporterone"));
		assertEquals("Value of reporter 'ReporterTwo' is wrong.",  "com.reporter.ReporterTwo", mappings.get("reportertwo"));
	}	
}
