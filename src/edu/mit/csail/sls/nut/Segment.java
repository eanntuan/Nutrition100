package edu.mit.csail.sls.nut;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Segment {
	@JsonProperty
	public
	String label;
	@JsonProperty
	public
	int start;
	@JsonProperty
	public
	int end;
}
