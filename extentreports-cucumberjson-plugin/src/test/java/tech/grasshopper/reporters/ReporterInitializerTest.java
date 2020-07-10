package tech.grasshopper.reporters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.aventstack.extentreports.reporter.ConfigurableReporter;

import tech.grasshopper.exception.ExtentReportsCucumberPluginException;
import tech.grasshopper.logging.ExtentReportsCucumberLogger;
import tech.grasshopper.properties.ReportProperties;

public class ReporterInitializerTest {

	private ReporterInitializer reportInitializer;
	private ReportProperties reportProperties;
	private ExtentReportsCucumberLogger logger;

	@Before
	public void setup() {
		logger = mock(ExtentReportsCucumberLogger.class);
		reportProperties = mock(ReportProperties.class);
		reportInitializer = new ReporterInitializer(reportProperties, logger);
	}

	@Test
	public void testReportClassMappingNotAvailable() {
		when(reportProperties.retrieveReportIdToClassNameMappings()).thenReturn(new HashMap<>());
		Exception exception = assertThrows(
				"The exception thrown should be an instance of ExtentReportsCucumberPluginException.",
				ExtentReportsCucumberPluginException.class, () -> {
					reportInitializer.instantiate();
				});
		assertEquals("Skipping reports generation as no report 'start' value set to 'true' in extent properties file.",
				exception.getMessage());
	}

	@Test
	public void testReportStartValueNotEnabled() {
		Map<String, String> mapping = new HashMap<>();
		mapping.put("reporter", "tech.grasshopper.reporter.invalid");
		when(reportProperties.retrieveReportIdToClassNameMappings()).thenReturn(mapping);
		when(reportProperties.checkReportRequired("reporter")).thenReturn(false);
		Exception exception = assertThrows(
				"The exception thrown should be an instance of ExtentReportsCucumberPluginException.",
				ExtentReportsCucumberPluginException.class, () -> {
					reportInitializer.instantiate();
				});
		assertEquals("Skipping reports generation as no report 'start' value set to 'true' in extent properties file.",
				exception.getMessage());
	}

	@Test
	public void testReportClassNotAvailable() {
		Map<String, String> mapping = new HashMap<>();
		mapping.put("reporter", "tech.grasshopper.reporter.invalid");
		when(reportProperties.retrieveReportIdToClassNameMappings()).thenReturn(mapping);
		when(reportProperties.checkReportRequired("reporter")).thenReturn(true);
		assertThrows("The exception thrown should be an instance of ExtentReportsCucumberPluginException.",
				ExtentReportsCucumberPluginException.class, () -> {
					reportInitializer.instantiate();
				});
		verify(logger, times(1)).warn(
				"Skipping report of class - tech.grasshopper.reporter.invalid, as unable to instantiate reporter class. Check the value in extent properties.");
	}

	@Test
	public void testReportClassNotValid() {
		Map<String, String> mapping = new HashMap<>();
		mapping.put("reporter", "java.lang.String");
		when(reportProperties.retrieveReportIdToClassNameMappings()).thenReturn(mapping);
		when(reportProperties.checkReportRequired("reporter")).thenReturn(true);
		when(reportProperties.getReportOutProperty("reporter")).thenReturn("test-output/reporter");
		assertThrows("The exception thrown should be an instance of ExtentReportsCucumberPluginException.",
				ExtentReportsCucumberPluginException.class, () -> {
					reportInitializer.instantiate();
				});
		verify(logger, times(1)).warn(
				"Skipping report of class - java.lang.String, as unable to cast to 'com.aventstack.extentreports.reporter.ConfigurableReporter' class.");
	}
	
	@Test
	public void testInstantiateReporter() {
		Map<String, String> mapping = new HashMap<>();
		mapping.put("spark", "com.aventstack.extentreports.reporter.ExtentSparkReporter");
		when(reportProperties.retrieveReportIdToClassNameMappings()).thenReturn(mapping);
		when(reportProperties.checkReportRequired("spark")).thenReturn(true);
		when(reportProperties.getReportOutProperty("spark")).thenReturn("test-output/Spark");
		when(reportProperties.getReportConfigProperty("spark")).thenReturn("");
		
		reportInitializer.instantiate();
		ConfigurableReporter reporter = reportInitializer.getReportKeyToInstance().get("spark");
		assertTrue("Reporter class instance is not correct.",reporter.getClass().isAssignableFrom(com.aventstack.extentreports.reporter.ExtentSparkReporter.class));
	}
	
	@Test
	public void testLoadReporterUISettings() {
		Map<String, String> mapping = new HashMap<>();
		mapping.put("spark", "com.aventstack.extentreports.reporter.ExtentSparkReporter");
		when(reportProperties.retrieveReportIdToClassNameMappings()).thenReturn(mapping);
		when(reportProperties.checkReportRequired("spark")).thenReturn(true);
		when(reportProperties.getReportOutProperty("spark")).thenReturn("test-output/Spark");
		when(reportProperties.getReportConfigProperty("spark")).thenReturn("src/test/resources/tech/grasshopper/reporters/extent-config.xml");
		
		reportInitializer.instantiate();
		Map<String, Object> store = reportInitializer.getReportKeyToInstance().get("spark").getConfigurationStore().getStore();
		assertEquals("Report name is wrong.", "Grasshopper Report", store.get("reportName"));
		assertEquals("Report theme is wrong.", "dark", store.get("theme"));
	}
	
	@Test
	public void testDefaultReporterUISettings() {
		Map<String, String> mapping = new HashMap<>();
		mapping.put("spark", "com.aventstack.extentreports.reporter.ExtentSparkReporter");
		when(reportProperties.retrieveReportIdToClassNameMappings()).thenReturn(mapping);
		when(reportProperties.checkReportRequired("spark")).thenReturn(true);
		when(reportProperties.getReportOutProperty("spark")).thenReturn("test-output/Spark");
		when(reportProperties.getReportConfigProperty("spark")).thenReturn("");
		
		reportInitializer.instantiate();
		Map<String, Object> store = reportInitializer.getReportKeyToInstance().get("spark").getConfigurationStore().getStore();
		assertEquals("Report name is wrong.", "ExtentReports", store.get("reportName"));
		assertEquals("Report theme is wrong.", "standard", store.get("theme"));
	}
}
