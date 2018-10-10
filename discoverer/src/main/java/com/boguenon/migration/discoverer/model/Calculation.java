package com.boguenon.migration.discoverer.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Calculation
	extends BaseItem
{
	@JsonProperty("DataType")
	@JacksonXmlProperty(isAttribute=true)
	public int Datatype;
		
	@JsonProperty("FormatMask")
	@JacksonXmlProperty(isAttribute=true)
	public String FormatMask;
	
	@JsonProperty("IsACalculation")
	@JacksonXmlProperty(isAttribute=true)
	public boolean IsACalculation;
	
	@JsonProperty("IsHidden")
	@JacksonXmlProperty(isAttribute=true)
	public boolean IsHidden;
		
	@JsonProperty("Placement")
	@JacksonXmlProperty(isAttribute=true)
	public int Placement;
	
	@JsonProperty("Formula")
	public Formula formula;
	
	@JsonProperty("ItemDependencies")
	public ItemDependencies itemDependencies;
	
	public List<String> getObjectKey()
	{
		List<String> objkey = new ArrayList<String>();
		
		if (this.formula != null && this.formula.elementRef != null)
		{
			for (int i=0; i < this.formula.elementRef.size(); i++)
			{
				List<String> mkey = this.getObjectKey(this.formula.elementRef.get(i));
				
				if (mkey != null && mkey.size() > 0)
				{
					objkey.addAll(mkey);
				}
			}
		}
		
		return objkey;
	}
}
