package com.boguenon.migration.discoverer;

import java.util.ArrayList;
import java.util.List;

public class ExpressionParser 
{
	public List<ExpressionValue> parseExpression(String value)
	{
		List<ExpressionValue> blocks = new ArrayList<ExpressionValue>();
		
		int m = value.indexOf("[");
		
		while (m > -1)
		{
			int m2 = value.indexOf("]", m+1);
			
			if (m2 > m)
			{
				String b = value.substring(0, m);
				String e = value.substring(m+1, m2);
				ExpressionValue bl = new ExpressionValue(e);
				blocks.add(bl);
				
				m = m2 + 1;
				
				assert(b.length() == 0);
				
				String right = value.substring(m2+1);
				value = value.substring(m2+1);
				
				if (right.length() > 0 && right.charAt(0) == '(')
				{
					int m3 = getBlockText(right);
					
					m = m3 + 1;
					
					String ml = right.substring(1, m3);
					String mr = right.substring(m3 + 1);
					
					bl.params = parseExpression(ml);
					
					value = mr;
				}
			}
			m = value.indexOf("[");
		}
		
		return blocks;
	}
	
	private static int getBlockText(String block)
	{
		int r = -1;
		int open_bracket = 1;
		
		for (int i=1; i < block.length(); i++)
		{
			if (block.charAt(i) == ')')
			{
				open_bracket -= 1;
			}
			else if (block.charAt(i) == '(')
			{
				open_bracket += 1;
			}
			
			if (open_bracket == 0)
			{
				r = i;
				break;
			}
		}
		
		return r;
	}
}
