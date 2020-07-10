package tech.grasshopper.pojo;

import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Embedded {

	private String data;
	@SerializedName("mime_type")
	private String mimeType;
	private String name;
	
	private String filePath;
}
