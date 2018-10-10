package com.boguenon.migration.discoverer.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Workbook {
	@JsonProperty("ActiveSheet")
	public int ActiveSheet;

	@JacksonXmlElementWrapper(localName="Sheet", useWrapping = false)
	@JacksonXmlProperty(localName="Sheet")
	public List<Sheet> sheets;

	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName="Condition")
	public List<Conditions> conditions;
	
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName="DisplayedItem")
	public List<DisplayedItem> displayedItems;
	
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName="Parameter")
	public List<Parameters> parameters;
	
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName="Calculation")
	public List<Calculation> calculations;
	
	private Map<String, BaseItem> m_basemap;
	
	public BaseItem getByDeveloperKey(String key)
	{
		if (m_basemap == null)
		{
			m_basemap = new HashMap<String, BaseItem>();
			
			if (this.calculations != null)
			{
				for (int i=0; i < this.calculations.size(); i++)
				{
					m_basemap.put(this.calculations.get(i).DeveloperKey, this.calculations.get(i));
				}
			}
			
			if (this.displayedItems != null)
			{
				for (int i=0; i < this.displayedItems.size(); i++)
				{
					m_basemap.put(this.displayedItems.get(i).DeveloperKey, this.displayedItems.get(i));
				}
			}
			
			if (this.parameters != null)
			{
				for (int i=0; i < this.parameters.size(); i++)
				{
					m_basemap.put(this.parameters.get(i).DeveloperKey, this.parameters.get(i));
				}
			}
			
			if (this.conditions != null)
			{
				for (int i=0; i < this.conditions.size(); i++)
				{
					m_basemap.put(this.conditions.get(i).DeveloperKey, this.conditions.get(i));
				}
			}
		}
		
		return m_basemap.get(key);
	}
}
