package com.boguenon.migration.discoverer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Formula {
	@JsonProperty("ExpressionString")
	@JacksonXmlProperty(isAttribute=true)
	public String ExpressionString;
	
	@JsonProperty("ElementRef")
	public List<ElementRef> elementRef;
}
