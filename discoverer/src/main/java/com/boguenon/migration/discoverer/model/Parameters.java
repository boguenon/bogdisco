package com.boguenon.migration.discoverer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Parameters 
	extends BaseItem
{
	@JsonProperty("DescriptorId")
	public String DescriptorId;
	
	@JsonProperty("AllowMultipleValues")
	public boolean AllowMultipleValues;
	
	@JsonProperty("DefaultValue")
	public String DefaultValue;
	
	@JsonProperty("Optional")
	public boolean Optional;
	
	@JsonProperty("Prompt")
	public String Prompt;
	
	@JsonProperty("Scope")
	public String Scope;
	
	@JsonProperty("ShowDescriptorId")
	public boolean ShowDescriptorId;
	
	@JsonProperty("ForItemId")
	public ForItemId forItemId;
	
	// for sheet
	@JsonProperty("RestrictedByLOVCondition")
	public boolean RestrictedByLOVCondition;
	
	@JsonProperty("ParameterReference")
	public ParameterReference parameterReference;
	
	@JsonProperty("Values")
	public String Values;
	
	@JsonProperty("DefaultValues")
	public String DefaultValues;
}
