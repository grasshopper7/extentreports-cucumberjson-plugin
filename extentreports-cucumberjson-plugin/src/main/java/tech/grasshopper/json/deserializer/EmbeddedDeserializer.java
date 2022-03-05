package tech.grasshopper.json.deserializer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import tech.grasshopper.logging.ExtentReportsCucumberLogger;
import tech.grasshopper.pojo.Embedded;
import tech.grasshopper.properties.ReportProperties;

public class EmbeddedDeserializer implements JsonDeserializer<Embedded> {

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

	public EmbeddedDeserializer(ReportProperties reportProperties, ExtentReportsCucumberLogger logger) {
		this.reportProperties = reportProperties;
		this.logger = logger;
	}

	@Override
	public Embedded deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) {
		JsonObject jsonObject = json.getAsJsonObject();
		Embedded embedded = new Embedded();
		embedded.setData(jsonObject.get("data").getAsString());
		embedded.setMimeType(jsonObject.get("mime_type").getAsString());
		if (jsonObject.has("name"))
			embedded.setName(jsonObject.get("name").getAsString());
		else
			embedded.setName("");

		processEmbedding(embedded);
		return embedded;
	}

	private void processEmbedding(Embedded embedded) {
		String mimeType = embedded.getMimeType();
		String extension = MIME_TYPES_EXTENSIONS.get(mimeType);

		if (extension != null) {
			Path path = createEmbeddedFileStructure(extension);
			try {
				Files.write(path, Base64.getDecoder().decode(embedded.getData()));
			} catch (IOException e) {
				logger.warn("Skipping embedded file creation at location - " + path.toString()
						+ ", due to error in creating file.");
				return;
			} finally {
				// No need anymore
				embedded.setData("");
			}
			embedded.setFilePath(
					(Paths.get(reportProperties.getReportRelativeScreenshotLocation(), path.getFileName().toString()))
							.toString());
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
		return path;
	}
}
