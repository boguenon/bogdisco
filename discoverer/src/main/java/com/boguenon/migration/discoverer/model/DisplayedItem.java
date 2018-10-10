package com.boguenon.migration.discoverer.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DisplayedItem 
	extends BaseItem
{
	@JsonProperty("Type")
	public String Type;
	
	@JsonProperty("Sortonly")
	public boolean Sortonly;
	
	@JsonProperty("Lasttotalplace")
	public String Lasttotalplace;
	
	@JsonProperty("ElementRef")
	public ElementRef elementRef;
	
	@JsonProperty("DataFormat")
	public DataFormat dataFormat;
	
	@JsonProperty("HeadingFormat")
	public DataFormat headingFormat;
	
	@JsonProperty("Item")
	public Item item;
	
	public String getName()
	{
		String name = null;
		
		if (this.Name != null && this.Name.length() > 0)
		{
			name = this.Name;
		}
		else if (this.item != null && this.item.elementRef != null)
		{
			for (int i=0; i < this.item.elementRef.uniqueIdent.size(); i++)
			{
				if (this.item.elementRef.uniqueIdent.get(i).ConstraintName.equals("ITE2"))
				{
					name = this.item.elementRef.uniqueIdent.get(i).Name;
					break;
				}
			}
		}
		
		return name;
	}
	
	public List<String> getObjectKey()
	{
		List<String> objkey = new ArrayList<String>();
		
		if (this.item != null && this.item.elementRef != null)
		{
			objkey = this.getObjectKey(this.item.elementRef);
		}
		
		return objkey;
	}
}
