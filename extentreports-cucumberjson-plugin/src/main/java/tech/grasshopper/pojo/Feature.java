package tech.grasshopper.pojo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Feature {

	private int line;
    private List<Scenario> elements = new ArrayList<>();
    private String name;
    private String description;
    private String id;
    private String keyword;
    private String uri;
    private List<Tag> tags = new ArrayList<>();
    
    private int testId;
    private Date startTime;
    private Date endTime;
}
