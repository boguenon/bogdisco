package com.boguenon.migration.discoverer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "EndUserLayoutExport")
@JsonIgnoreProperties(ignoreUnknown = true)
public class EndUserLayerExport 
{
	@JsonProperty("Document")
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName="Document", isAttribute=false)
	public List<Document> documents;
}
