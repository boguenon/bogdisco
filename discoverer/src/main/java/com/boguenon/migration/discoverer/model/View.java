package com.boguenon.migration.discoverer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

// @JsonIgnoreProperties(ignoreUnknown = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class View 
{
	public int AggregationBehavior;
	public String DeveloperKey;
	public boolean Distinct; 
	public String MeasureAxis;
	public int MeasureLevel;
	public String Type;
			
	@JacksonXmlElementWrapper(useWrapping = false)
	@JsonProperty("XDisplayedItems")
	public List<DisplayedItem> xDisplayItems;
	
	@JacksonXmlElementWrapper(useWrapping = false)
	@JsonProperty("YDisplayedItems")
	public List<DisplayedItem> yDisplayItems;
	
	@JacksonXmlElementWrapper(useWrapping = false)
	@JsonProperty("ZDisplayedItems")
	public List<DisplayedItem> zDisplayItems;
	
	@JacksonXmlElementWrapper(useWrapping = false)
	@JsonProperty("MeasureItems")
	public List<DisplayedItem> measureItems;
	
	@JacksonXmlElementWrapper(useWrapping = false)
	@JsonProperty("Parameters")
	public List<Parameters> parameters;
	
	@JacksonXmlElementWrapper(useWrapping = false)
	@JsonProperty("Conditions")
	public List<Conditions> conditions;
}
