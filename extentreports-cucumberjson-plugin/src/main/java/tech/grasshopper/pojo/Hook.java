package tech.grasshopper.pojo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Hook {

	private List<String> output = new ArrayList<>();
	private List<Embedded> embeddings = new ArrayList<>();
	private Result result;
    private Match match;
    
    private int testId;
    private Date startTime;
    private Date endTime;
    
    private HookType hookType;
    
    public static enum HookType {
    	BEFORE, AFTER, BEFORE_STEP, AFTER_STEP;
    }
}
