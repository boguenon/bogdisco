package com.boguenon.migration.discoverer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RangeFormats 
{
	public boolean AutoName;
	public String Description;
	public String DeveloperKey;
	public String DisplayName;
	public int ExceptionType;
	public boolean ForStopLight;
	public boolean FormatValidBackgroundColor;
	public boolean FormatValidFontFace;
	public boolean FormatValidFontSize;
	public boolean FormatValidFontStyle;
	public boolean FormatValidFormatMask;
	public boolean FormatValidHorizontalAlignment;
	public boolean FormatValidInlineWidth;
	public boolean FormatValidOutlineWidth;
	public boolean FormatValidVerticalAlignment;
	public boolean FormatValidWidth;
	public boolean IsActive;
	
	public boolean FormatValidForgroundColor;
	
	@JsonProperty("RangeItem")
	public RangeItem rangeItem;
	
	@JsonProperty("FormatItem")
	public DefaultDataFormat formatItem;
}
