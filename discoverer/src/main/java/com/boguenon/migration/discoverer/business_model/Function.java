package com.boguenon.migration.discoverer.business_model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Function
	extends ModelBase
{
	private static final Logger logger = LoggerFactory.getLogger(Function.class);
	
	public String description_s;
	public String description_mn;
	
	public int function_type;
	public boolean hidden;
	public int data_type;
	public boolean available;
	public int maximum_args;
	public int minimum_args;
	
	public boolean built_in;
	
	public String ext_name;
	public String ext_package;
	public String ext_owner;
	public String ext_db_link;
	
	public boolean is_aggregation()
	{
		boolean r = this.function_type == 4;
		
		return r;
	}
	
	public String getOperator()
	{
		String r = this.ext_name;
		
		if (this.ext_name.equals("="))
		{
			r = "EQ";
		}
		else if (this.ext_name.equals(">="))
		{
			r = "GTE";
		}
		else if (this.ext_name.equals(">"))
		{
			r = "GT";
		}
		else if (this.ext_name.equals("<="))
		{
			r = "LTE";
		}
		else if (this.ext_name.equals("<"))
		{
			r = "LT";
		}
		else if (this.ext_name.equals("BETWEEN"))
		{
			r = "BETWEEN";
		}
		else if (this.ext_name.equals("IN"))
		{
			r = "IN";
		}
		else if (this.ext_name.equals("<>"))
		{
			r = "EQ";
		}
		else if (this.ext_name.equals("LIKE"))
		{
			r = "LIKE";
		}
		else if (this.ext_name.equals("NOT IN"))
		{
			r = "IN";
		}
		else if (this.ext_name.equals("NOT LIKE"))
		{
			r = "LIKE";
		}
		else
		{
			logger.debug("undefined operation " + this.ext_name);
		}
		
		return r;
	}
	
	public boolean getIsNot()
	{
		boolean isnot = false;
		
		if (this.ext_name.equals("<>") || this.ext_name.equals("NOT IN") || this.ext_name.equals("NOT LIKE"))
		{
			isnot = true;
		}
		
		return isnot;
	}
}
