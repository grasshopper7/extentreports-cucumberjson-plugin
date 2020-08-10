package tech.grasshopper.processor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;

import net.iharder.Base64;
import tech.grasshopper.logging.ExtentReportsCucumberLogger;
import tech.grasshopper.pojo.Embedded;
import tech.grasshopper.properties.ReportProperties;

@Singleton
public class EmbeddedProcessor {

	private static final AtomicInteger EMBEDDED_INT = new AtomicInteger(0);

	@SuppressWarnings("serial")
	private static final Map<String, String> MIME_TYPES_EXTENSIONS = new HashMap<String, String>() {
		{
			put("image/bmp", "bmp");
			put("image/gif", "gif");
			put("image/jpeg", "jpg");
			put("image/png", "png");
			put("image/svg+xml", "svg");
			put("video/ogg", "ogg");
		}
	};

	private ReportProperties reportProperties;
	private ExtentReportsCucumberLogger logger;

	@Inject
	public EmbeddedProcessor(ReportProperties reportProperties, ExtentReportsCucumberLogger logger) {
		this.reportProperties = reportProperties;
		this.logger = logger;
	}

	public void updateExtentTestWithEmbedding(ExtentTest test, List<Embedded> embeddings) {
		for (Embedded embed : embeddings) {
			String name = embed.getName() == null ? "" : embed.getName();
			String filePath = embed.getFilePath();

			if (filePath == null || filePath.isEmpty()) {
				logger.warn("Skipping adding embedded file as filepath is empty for step - '"
						+ test.getModel().getName() + "'.");
				return;
			}
			try {
				test.info(name, MediaEntityBuilder.createScreenCaptureFromPath(filePath).build());
				// Embedding workaround for html report.
				test.addScreenCaptureFromPath(filePath);
			} catch (Exception e) {
				logger.warn("Skipping adding embedded file for step - '" + test.getModel().getName()
						+ "' as error in processing.");
				return;
			}
		}
	}

	public void processEmbedding(Embedded embedded) {
		String mimeType = embedded.getMimeType();
		String extension = MIME_TYPES_EXTENSIONS.get(mimeType);

		if (extension != null) {
			Path path = createEmbeddedFileStructure(extension);
			try {
				Files.write(path, Base64.decode(embedded.getData()));
			} catch (IOException e) {
				logger.warn("Skipping embedded file creation at location - " + path.toString()
						+ ", due to error in creating file.");
				return;
			} finally {
				// No need anymore
				embedded.setData("");
			}
			embedded.setFilePath((Paths.get(reportProperties.getReportRelativeScreenshotLocation(), path.getFileName().toString())).toString());
		} else {
			logger.warn("Mime type '" + mimeType + "' not supported.");
		}
	}

	private Path createEmbeddedFileStructure(String extension) {
		StringBuilder fileName = new StringBuilder("embedded").append(EMBEDDED_INT.incrementAndGet()).append(".")
				.append(extension);
		String embedDirPath = reportProperties.getReportScreenshotLocation();

		File dir = new File(embedDirPath);
		// Create directory if not existing
		if (!dir.exists())
			dir.mkdirs();
		
		Path path = Paths.get(embedDirPath, fileName.toString());
		//return path.toAbsolutePath();
		return path;
	}
}
