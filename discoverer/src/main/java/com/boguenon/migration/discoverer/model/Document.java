package com.boguenon.migration.discoverer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Document {
	@JacksonXmlProperty(localName="DeveloperKey")
	public String DeveloperKey;
	
	@JacksonXmlProperty(localName="Id")
	public String id;
	
	@JacksonXmlProperty(localName="Name")
	public String Name;
	
	@JacksonXmlProperty(localName="Workbook", isAttribute=false)
	@JacksonXmlElementWrapper(localName="Workbook", useWrapping = false)
	public List<Workbook> workbooks;
}
