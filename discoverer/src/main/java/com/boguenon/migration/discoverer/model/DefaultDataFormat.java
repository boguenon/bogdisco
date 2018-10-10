package com.boguenon.migration.discoverer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultDataFormat 
{
	@JsonProperty("DeveloperKey")
	public String DeveloperKey;
	
	@JsonProperty("BackgroundColor")
	public String BackgroundColor;
	
	@JsonProperty("ForegroundColor")
	public String ForegroundColor;
	
	@JsonProperty("NegativeColor")
	public String NegativeColor;
	
	@JsonProperty("ColumnWidth")
	public int ColumnWidth;
	
	@JsonProperty("DataType")
	public String DataType;
	
	@JsonProperty("DefaultMask")
	public String DefaultMask;
	
	@JsonProperty("FontCharset")
	public int FontCharset;
	
	@JsonProperty("FontItalic")
	public int FontItalic;
	
	@JsonProperty("FontOrientation")
	public int FontOrientation;
	
	@JsonProperty("FontStrikeout")
	public int FontStrikeout;
	
	@JsonProperty("FontUnderline")
	public int FontUnderline;
	
	@JsonProperty("FontWeight")
	public int FontWeight;
	
	@JsonProperty("FormatValid")
	public String FormatValid;
	
	@JsonProperty("HasBrackets")
	public boolean HasBrackets;
	
	@JsonProperty("HasCurrency")
	public boolean HasCurrency;
	
	@JsonProperty("HasNegativeColor")
	public boolean HasNegativeColor;
	
	@JsonProperty("HasSeparator")
	public boolean HasSeparator;
	
	@JsonProperty("HorizontalAlign")
	public String HorizontalAlign;
	
	@JsonProperty("IsGraphic")
	public boolean IsGraphic;
	
	@JsonProperty("Mask")
	public String Mask;
	
	@JsonProperty("MaskDeveloperKey")
	public String MaskDeveloperKey;
	
	@JsonProperty("MaskType")
	public String MaskType;
	
	@JsonProperty("Precision")
	public int Precision;
	
	@JsonProperty("Type")
	public int Type;
	
	@JsonProperty("VerticalAlign")
	public String VerticalAlign;
	
	@JsonProperty("FontEscapement")
	public String FontEscapement;
	
	@JsonProperty("FontFace")
	public String FontFace;
	
	@JsonProperty("AutoWidth")
	public int AutoWidth;
}
