package com.boguenon.migration.discoverer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DataFormat 
{
	public String DeveloperKey;
	
	public String StopLights;
	public int StopLightsSize;
	public int StopLights_0;
	
	@JsonProperty("DefaultDataFormat")
	public DefaultDataFormat defaultDataFormat;
	
	@JsonProperty("RangeFormats")
	public List<RangeFormats> rangeFormat;
}
