package tech.grasshopper.pojo;

import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Result {

	private long duration;
	private String status;
	@SerializedName("error_message")
	private String errorMessage;
}
