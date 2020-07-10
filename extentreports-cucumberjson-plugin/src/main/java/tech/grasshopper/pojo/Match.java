package tech.grasshopper.pojo;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Match {

	private List<Argument> arguments = new ArrayList<>();
	private String location;
}
