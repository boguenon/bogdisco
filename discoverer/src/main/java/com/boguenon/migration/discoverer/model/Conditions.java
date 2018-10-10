package com.boguenon.migration.discoverer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Conditions 
	extends BaseItem
{
	@JsonProperty("ElementRef")
	public ElementRef elementRef;
	
	// for workbook
	@JsonProperty("CaseSensitive")
	public boolean CaseSensitive;
	
	@JsonProperty("Hidden")
	public boolean Hidden;
	
	@JsonProperty("Sequence")
	public int Sequence;
	
	@JsonProperty("Formula")
	public Formula formula;
	
	@JsonProperty("ItemDependencies")
	public ItemDependencies itemDependencies;
}
