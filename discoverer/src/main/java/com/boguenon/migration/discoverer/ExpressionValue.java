package com.boguenon.migration.discoverer;

import java.util.List;

public class ExpressionValue 
{
	public String orig_value;
	public int type;
	public String value1;
	public String value2;
	public List<ExpressionValue> params;
	
	public ExpressionValue(String e)
	{
		String[] me = e.split(",");
		type = Integer.parseInt(me[0]);
		value1 = me[1];
		value2 = me[2];
	}
}
