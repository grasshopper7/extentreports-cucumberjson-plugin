package tech.grasshopper.properties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import tech.grasshopper.ExtentReportsCucumberPlugin;
import tech.grasshopper.exception.ExtentReportsCucumberPluginException;
import tech.grasshopper.logging.ExtentReportsCucumberLogger;

@Singleton
public class ReportProperties {

	private String reportsPropertiesFolder;
	private String reportsPropertiesFileName;
	private boolean displayAllHooks;
	private boolean strictCucumber6Behavior;

	private ExtentReportsCucumberLogger logger;

	private static final Properties properties = new Properties();
	private static final String DEFAULT_REPORTS_PROPERTIES_FILE = "tech/grasshopper/properties/extent.properties";
	private static final String DEFAULT_PROJECT_REPORTS_PROPERTIES_FILE_NAME = "extent.properties";
	public static final String DEFAULT_REPORTS_PROPERTIES = "extent.reporter.";
	public static final String DEFAULT_REPORTS_PROPERTIES_CLASS = ".class";
	public static final String DEFAULT_REPORTS_PROPERTIES_START = ".start";
	public static final String DEFAULT_REPORTS_PROPERTIES_CONFIG = ".config";
	public static final String DEFAULT_REPORTS_PROPERTIES_OUT = ".out";
	public static final String DEFAULT_REPORTS_SCREENSHOTS_DIRECTORY = "screenshot.dir";
	public static final String DEFAULT_REPORTS_SCREENSHOTS_DIR_RELATIVE_PATH = "screenshot.rel.path";
	public static final String REPORTS_BASEFOLDER_NAME = "basefolder.name";
	public static final String REPORTS_BASEFOLDER_DATETIMEPATTERN = "basefolder.datetimepattern";
	private static final String DEFAULT_SCREENSHOTS_LOCATION = "test-output/";
	private static final String DEFAULT_SCREENSHOTS_DIR_PATH = "../";
	private static final LocalDateTime FOLDER_TIMESTAMP = LocalDateTime.now();
	private static final String SYS_INFO_MARKER = "systeminfo.";

	@Inject
	public ReportProperties(ExtentReportsCucumberLogger logger) {
		this.reportsPropertiesFileName = DEFAULT_PROJECT_REPORTS_PROPERTIES_FILE_NAME;
		this.logger = logger;
	}

	ReportProperties(Properties props) {
		properties.putAll(props);
	}

	void loadPropertyFiles(String folderName, String fileName) {
		if (folderName != null && !folderName.isEmpty())
			this.reportsPropertiesFolder = folderName;
		if (fileName != null && !fileName.isEmpty())
			this.reportsPropertiesFileName = fileName;

		loadDefaultPropertyFile();
		loadProjectPropertyFile();
	}

	public void loadPropertyFiles(String folderName) {
		loadPropertyFiles(folderName, "");
	}

	private void loadDefaultPropertyFile() {
		ClassLoader loader = ExtentReportsCucumberPlugin.class.getClassLoader();
		InputStream is = loader.getResourceAsStream(DEFAULT_REPORTS_PROPERTIES_FILE);
		try {
			properties.load(is);
		} catch (IOException e) {
			throw new ExtentReportsCucumberPluginException(
					"Stopping report generation as default extent report properties file not available at - "
							+ DEFAULT_REPORTS_PROPERTIES_FILE + ".");
		}
	}

	private void loadProjectPropertyFile() {
		if (reportsPropertiesFolder != null) {
			try {
				InputStream is = new FileInputStream(reportsPropertiesFolder + "/" + reportsPropertiesFileName);
				properties.load(is);
			} catch (FileNotFoundException e) {
				logger.warn("Skipping reading project extent properties as file not found at location - "
						+ reportsPropertiesFolder + "/" + reportsPropertiesFileName + ".");
			} catch (IOException e) {
				logger.warn("Skipping reading project extent properties due to parsing error.");
			}
		}
	}

	public Map<String, String> retrieveReportIdToClassNameMappings() {
		Map<String, String> idToName = new HashMap<>();
		properties.forEach((k, v) -> {
			String key = String.valueOf(k);
			if (key.endsWith(DEFAULT_REPORTS_PROPERTIES_CLASS)) {
				Pattern pattern = Pattern
						.compile(DEFAULT_REPORTS_PROPERTIES + "([\\w]+)" + DEFAULT_REPORTS_PROPERTIES_CLASS);
				Matcher matcher = pattern.matcher(key);
				if (matcher.find())
					idToName.put(matcher.group(1), String.valueOf(v));
			}
		});
		if (idToName.size() == 0)
			throw new ExtentReportsCucumberPluginException(
					"Skipping reports generation as no report 'class' value available in extent properties file.");
		return new HashMap<>(idToName);
	}

	public String getProperty(String name) {
		return properties.getProperty(name, "").trim();
	}

	public String getReportClassNameProperty(String key) {
		return getProperty(DEFAULT_REPORTS_PROPERTIES + key + DEFAULT_REPORTS_PROPERTIES_CLASS);
	}

	public String getReportStartProperty(String key) {
		return getProperty(DEFAULT_REPORTS_PROPERTIES + key + DEFAULT_REPORTS_PROPERTIES_START);
	}

	public String getReportConfigProperty(String key) {
		return getProperty(DEFAULT_REPORTS_PROPERTIES + key + DEFAULT_REPORTS_PROPERTIES_CONFIG);
	}

	public String getReportOutProperty(String key) {
		return Paths.get(getBaseFolderName(),
				getProperty(DEFAULT_REPORTS_PROPERTIES + key + DEFAULT_REPORTS_PROPERTIES_OUT)).toString();
	}

	public String getReportScreenshotLocation() {
		String screenshotDirectory = DEFAULT_SCREENSHOTS_LOCATION;
		if (!getProperty(DEFAULT_REPORTS_SCREENSHOTS_DIRECTORY).isEmpty())
			screenshotDirectory = getProperty(DEFAULT_REPORTS_SCREENSHOTS_DIRECTORY);
		return Paths.get(getBaseFolderName(), screenshotDirectory).toString();
	}

	public String getReportRelativeScreenshotLocation() {
		String reportPathToScreenshotDir = DEFAULT_SCREENSHOTS_DIR_PATH;
		if (!getProperty(DEFAULT_REPORTS_SCREENSHOTS_DIR_RELATIVE_PATH).isEmpty())
			reportPathToScreenshotDir = getProperty(DEFAULT_REPORTS_SCREENSHOTS_DIR_RELATIVE_PATH);
		return reportPathToScreenshotDir;
	}

	private String getBaseFolderName() {
		String folderpattern = "";
		if (!getProperty(REPORTS_BASEFOLDER_NAME).isEmpty()
				&& !getProperty(REPORTS_BASEFOLDER_DATETIMEPATTERN).isEmpty()) {
			DateTimeFormatter folderSuffix = DateTimeFormatter
					.ofPattern(getProperty(REPORTS_BASEFOLDER_DATETIMEPATTERN));
			folderpattern = getProperty(REPORTS_BASEFOLDER_NAME) + " " + folderSuffix.format(FOLDER_TIMESTAMP);
		}
		return folderpattern;
	}

	public boolean checkReportRequired(String key) {
		return getProperty(DEFAULT_REPORTS_PROPERTIES + key + DEFAULT_REPORTS_PROPERTIES_START).equalsIgnoreCase("true")
				? true
				: false;
	}

	public boolean isDisplayAllHooks() {
		return displayAllHooks;
	}

	public void setDisplayAllHooks(String displayAllHooks) {
		this.displayAllHooks = Boolean.parseBoolean(displayAllHooks);
	}

	public boolean isStrictCucumber6Behavior() {
		return strictCucumber6Behavior;
	}

	public void setStrictCucumber6Behavior(String strictCucumber6Behavior) {
		this.strictCucumber6Behavior = Boolean.parseBoolean(strictCucumber6Behavior);
	}

	public Map<String, String> getSystemInfomation() {
		Map<String, String> systemInfoMap = new HashMap<>();

		properties.forEach((k, v) -> {
			String key = String.valueOf(k);
			if (key.startsWith(SYS_INFO_MARKER)) {
				key = key.substring(key.indexOf('.') + 1);
				systemInfoMap.put(key, String.valueOf(v));
			}
		});
		return systemInfoMap;
	}
}
