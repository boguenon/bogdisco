package com.boguenon.migration.discoverer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Sheet {
	@JsonProperty("Name")
	public String Name;
	
	@JsonProperty("DeveloperKey")
	public String DeveloperKey;
	
	@JsonProperty("Title")
	public String Title;
	
	@JsonProperty("TitleHtml")
	public String TitleHtml;
	
	@JsonProperty("UniqueName")
	public String UniqueName;
	
	@JsonProperty("View")
	public View view;
	
	@JsonProperty("Showpageaxis")
	public boolean Showpageaxis;
	
	@JsonProperty("Options")
	public Options options;	
}
