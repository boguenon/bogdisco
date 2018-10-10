package com.boguenon.migration.discoverer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

public class ElementRef 
{
	@JsonProperty("Name")
	public String Name;
	
	@JsonProperty("Type")
	public String Type;
	
	@JacksonXmlElementWrapper(useWrapping = false)
	@JsonProperty("UniqueIdent")
	public List<UniqueIdent> uniqueIdent;
}
