package com.boguenon.migration.discoverer.logic;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.boguenon.migration.discoverer.DiscoMigration;
import com.boguenon.migration.discoverer.business_model.Element;
import com.boguenon.migration.discoverer.business_model.ModelBase;

public class FormulaLexer 
{
	private int pos = 0;
	private int buflen = 0;
	private String buf = null;
	
	public static Map<Long, Element> element_map = new HashMap<Long, Element>();
	
	public void setInput(String value)
	{
		this.buf = value;
		this.pos = 0;
		this.buflen = value.length();
	}
	
	private Map<Long, ModelBase> refitems;
	
	public void setRefItems(Map<Long, ModelBase> refitems)
	{
		this.refitems = refitems;
	}
	
	public List<FormulaToken> parseInput(Connection con_eul)
		throws Exception
	{
		List<FormulaToken> input = new ArrayList<FormulaToken>();
		
		// [1,81]([6,120737],[6,121054])
		
		if (this.pos >= this.buflen) 
		{
			return null;
		}
		
		while (this.pos < this.buflen)
		{
			char c = this.buf.charAt(this.pos);
			
			if (c == '[')
			{
				FormulaToken t = this._processToken(con_eul);
				input.add(t);
			}
			else if (c == ',')
			{
				this.pos++;
				FormulaToken t = this._processToken(con_eul);
				input.add(t);
			}
			else
			{
				throw new Exception("token format need to start with [" + this.buf);
			}
		}
		
		return input;
	}
	
	public FormulaToken _processToken(Connection con_eul)
		throws Exception
	{
		int endpos = this.pos + 1;
		
		while (endpos < this.buflen) 
		{
			if (this.buf.charAt(endpos) == ']')
			{
				endpos++;
				break;
			}
			endpos++;
		}

		FormulaToken tok = new FormulaToken();
		String mvalue = this.buf.substring(this.pos + 1, endpos -1);
		
		int n = mvalue.indexOf(',');
		
		if (n > 0)
		{
			tok.token = Integer.parseInt(mvalue.substring(0, n));
			
			int n1 = mvalue.indexOf(',', n+1);
			
			if (n1 > -1)
			{
				tok.token_value = Integer.parseInt(mvalue.substring(n+1, n1).trim());
				tok.pvalue = mvalue.substring(n1+1);
			}
			else
			{
				tok.token_value = Long.parseLong(mvalue.substring(n+1).trim());
			}
		}
		else
		{
			throw new Exception("token value malformatted." + this.buf);
		}
		
		this.pos = endpos;
		
		if (this.buflen > endpos && this.buf.charAt(endpos) == '(')
		{
			// find parameters
			int ocnt = 1;
			this.pos++;
			endpos++;
			
			boolean b_quote = false;
			
			while (endpos < this.buflen)
			{
				if (b_quote == true)
				{
					if (this.buf.charAt(endpos) == '\'')
					{
						b_quote = false;
					}
				}
				else if (this.buf.charAt(endpos) == '\'')
				{
					b_quote = true;
				}
				else if (this.buf.charAt(endpos) == ')')
				{
					ocnt--;
					if (ocnt == 0)
					{
						endpos++;
						break;
					}
				}
				else if (this.buf.charAt(endpos) == '(')
				{
					ocnt++;
				}
				endpos++;
			}
			
			if (ocnt != 0)
			{
				throw new Exception("need to have closing bracket." + this.buf);
			}
			else
			{
				String params = this.buf.substring(this.pos, endpos-1);
				FormulaLexer l = new FormulaLexer();
				l.setInput(params);
				l.setRefItems(this.refitems);
				tok.params = l.parseInput(con_eul);
				this.pos = endpos;
			}
		}
		
		switch (tok.token)
		{
		case FormulaToken.P_FUNCTION:
			tok.pfunction = DiscoMigration.eul_functions.get(tok.token_value);
			break;
		case FormulaToken.P_CUSTOM_FUNCTION:
			break;
		case FormulaToken.P_EXPRESSION:
			Element e = null;
			
			if (refitems != null && refitems.containsKey(tok.token_value) == true)
			{
				e = (Element) refitems.get(tok.token_value);
			}
			else if (element_map.containsKey(tok.token_value) == true)
			{
				e = element_map.get(tok.token_value);
			}
			else
			{
				if (con_eul != null)
				{
					e = new Element();
					e.id = tok.token_value;
					e.updateInfo(con_eul, refitems);
					
					element_map.put(e.id, e);
				}
			}
			
			tok.pelement = e;
			break;
		case FormulaToken.P_PARAMETER:
			if (refitems != null && refitems.containsKey(tok.token_value) == true)
			{
				tok.pvalue = ((ModelBase) refitems.get(tok.token_value)).name;
			}
			break;
		}
		
		return tok;
	}
}
