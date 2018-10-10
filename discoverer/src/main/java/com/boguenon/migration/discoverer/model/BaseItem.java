package com.boguenon.migration.discoverer.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class BaseItem 
{
	@JsonProperty("Id")
	public String Id;
	
	@JsonProperty("Name")
	public String Name;
	
	@JsonProperty("Description")
	@JacksonXmlProperty(isAttribute=true)
	public String Description;
	
	@JsonProperty("DeveloperKey")
	@JacksonXmlProperty(isAttribute=true)
	public String DeveloperKey;
	
	public String getName()
	{
		return Name;
	}
	
	public List<String> getObjectKey()
	{
		return null;
	}
	
	private List<String> objkey = null; 
	
	public List<String> getObjectKey(ElementRef elementRef)
	{
		if (this.objkey != null)
			return this.objkey;
		
		this.objkey = new ArrayList<String>();
		
		for (int i=0; i < elementRef.uniqueIdent.size(); i++)
		{
			if (elementRef.uniqueIdent.get(i).ConstraintName.equals("ITE2"))
			{
				for (int j=0; j < elementRef.uniqueIdent.get(i).elementRef.uniqueIdent.size(); j++)
				{
					if (elementRef.uniqueIdent.get(i).elementRef.uniqueIdent.get(j).ConstraintName.equals("OBJ1"))
					{
						String dkey = elementRef.uniqueIdent.get(i).elementRef.uniqueIdent.get(j).DeveloperKey;
						
						if (dkey != null)
						{
							this.objkey.add(dkey);
						}
					}
				}
				break;
			}
		}
		
		return objkey;
	}
}
