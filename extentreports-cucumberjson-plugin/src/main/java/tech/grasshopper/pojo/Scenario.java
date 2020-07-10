package tech.grasshopper.pojo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Scenario {

	@SerializedName("start_timestamp")
    private String startTimestamp;
	private int line;
	private String name;
    private String description;
    private String id;
    private String type;
    private String keyword;
    private List<Hook> before = new ArrayList<>();
    private List<Step> steps = new ArrayList<>();
    private List<Hook> after = new ArrayList<>();
    private List<Tag> tags = new ArrayList<>();
    
    private int testId;
    private String uri;
	private List<Integer> stepLines;
    private Date startTime;
    private Date endTime;
    
    public String getUriStepLines() {
    	return uri+":"+stepLines.stream().map(String::valueOf).collect(Collectors.joining(":"));
    }
    
    public List<Hook> getBeforeAfterHooks() {
    	List<Hook> hooks = new ArrayList<>();
    	hooks.addAll(before);
    	hooks.addAll(after);
    	return hooks;
    }
}
