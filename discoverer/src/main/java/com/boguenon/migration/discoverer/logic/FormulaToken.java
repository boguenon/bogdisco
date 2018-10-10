package com.boguenon.migration.discoverer.logic;

import java.util.ArrayList;
import java.util.List;

import com.boguenon.migration.discoverer.business_model.Element;
import com.boguenon.migration.discoverer.business_model.Function;

public class FormulaToken 
{
	public static final int P_FUNCTION = 1;
	public static final int P_CUSTOM_FUNCTION = 2;
	public static final int P_STRING = 5;
	public static final int P_EXPRESSION = 6;
	public static final int P_PARAMETER = 8;
	
	public int token;
	public long token_value;
	
	public Element pelement;
	public Function pfunction;
	
	public String pvalue;
	
	public List<FormulaToken> params;
	
	public FormulaToken()
	{
		this.params = new ArrayList<FormulaToken>();
	}
	
	public boolean is_aggregation()
	{
		boolean r = false;
		
		if (this.token == FormulaToken.P_FUNCTION && this.pfunction != null)
		{
			if (this.pfunction.is_aggregation())
			{
				r = true;
			}
		}
		else if (params.size() > 0)
		{
			for (int i=0; i < params.size(); i++)
			{
				boolean b = params.get(i).is_aggregation();
				
				if (b == true)
				{
					r = true;
					break;
				}
			}
		}
		
		return r;
	}
	
	public String getSQL()
	{
		String r = "";
		
		switch (this.token)
		{
		case FormulaToken.P_FUNCTION:
			if (this.pfunction.function_type == 1 || this.pfunction.function_type == 2 || this.pfunction.function_type == 3)
			{
				for (int i=0; i < this.params.size(); i++)
				{
					r += (i > 0 ? " " + this.pfunction.ext_name + " " : "") + this.params.get(i).getSQL();
				}
			}
			else if (this.pfunction.function_type == 2)
			{
				r += this.pfunction.ext_name;
			}
			else
			{
				r = this.pfunction.ext_name + "(";
				
				if (this.params != null)
				{
					for (int i=0; i < this.params.size(); i++)
					{
						r += (i > 0 ? ", " : "") + this.params.get(i).getSQL();
					}
				}
				
				r += ")";
			}
			break;
		case FormulaToken.P_CUSTOM_FUNCTION:
			break;
		case FormulaToken.P_EXPRESSION:
			r = "[" + this.pelement.getAlias() + "]";
			break;
		case FormulaToken.P_STRING:
			String mvalue = this.pvalue.trim();
			
			if (mvalue.length() > 1 && mvalue.charAt(0) == '\"' && mvalue.charAt(mvalue.length() - 1) == '\"')
			{
				mvalue = mvalue.substring(1, mvalue.length() - 1);
			}
			
			r = "'" + mvalue + "'";
			break;
		case FormulaToken.P_PARAMETER:
			r = "${" + this.pvalue + "}";
			break;
		}
		
		return r;
	}
}
