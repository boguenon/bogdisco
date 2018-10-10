package com.boguenon.migration.discoverer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Options 
{
	public boolean AutoSizeColumns;
	public String CellGridColor;
	public String DeveloperKey;
	public boolean ShowYCellGrid;
	public boolean Showcolumns;
	public boolean Showpageaxis;
	public boolean Showrows;
	public boolean Showtitle;
	public boolean Showxcellgrid;
	
	public int ColsPerPage;
	public int RowsPerPage;
	public boolean ShowAnnotations;
	public boolean ShowDrillIcon;
	public String ShowNaAs;
	public String ShowNullAs;
	
	public boolean ShowPageItems;
	public boolean ShowParameterFrame;
	public boolean ShowTableForCrossTab;
	
	public boolean Show_Graph;
	public String SlAcceptable;
	public String SlDesirable;
	public String SlUnacceptable;
	
	public String WorksheetDescription;
	
	public String DefaultTitle;
}
