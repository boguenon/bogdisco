package com.boguenon.migration.discoverer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UniqueIdent {
	@JsonProperty("ConstraintName")
	public String ConstraintName;
	
	@JsonProperty("DeveloperKey")
	public String DeveloperKey;
	
	@JsonProperty("Name")
	public String Name;
	
	@JsonProperty("ElementRef")
	public ElementRef elementRef;
}
