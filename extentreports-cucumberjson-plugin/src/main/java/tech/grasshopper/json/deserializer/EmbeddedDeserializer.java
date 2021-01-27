package tech.grasshopper.json.deserializer;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import tech.grasshopper.pojo.Embedded;
import tech.grasshopper.processor.EmbeddedProcessor;

public class EmbeddedDeserializer implements JsonDeserializer<Embedded> {

	private EmbeddedProcessor embeddedProcessor;

	public EmbeddedDeserializer(EmbeddedProcessor embeddedProcessor) {
		this.embeddedProcessor = embeddedProcessor;
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

		embeddedProcessor.processEmbedding(embedded);
		return embedded;
	}
}
