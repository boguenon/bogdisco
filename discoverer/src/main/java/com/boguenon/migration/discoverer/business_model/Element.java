package com.boguenon.migration.discoverer.business_model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boguenon.migration.discoverer.logic.FormulaLexer;
import com.boguenon.migration.discoverer.logic.FormulaToken;

public class Element 
	extends ModelBase
{
	private static final Logger logger = LoggerFactory.getLogger(Element.class);
	
	public String type;
	private String formula;
	
	public String getFormula()
	{
		return this.formula;
	}
	
	public void setFormula(String value)
	{
		this.formula = value;
	}
	
	public int data_type;
	public int sequence;
	
	public BusinessObject obj;
	
	public String format_mask;
	public int max_data_width;
	public int max_disp_width;
	
	public int alignment;
	public boolean wordwrap;
	
	public String disp_null_val;
	public long function_id;
	
	public String heading;
	
	public String getObjectName()
	{
		 String r = null;
		 
		 if (this.type.equals("CO"))
		 {
			 r = this.ext_column != null && this.ext_column.length() > 0 ? this.ext_column : this.name;
		 }
		 else if (this.type.equals("CI"))
		 {
			 r = this.name;
		 }
		 else // FIL, JP
		 {
			 r = this.name;
		 }
		 
		 return r; //ele.name != null && ele.name.length() > 0 ? ele.name : ele.heading;
	}
	
	public String getAlias()
	{
		String r = null;
		
		if (this.heading != null && this.heading.length() > 0)
		{
			r = this.heading;
		}
		else
		{
			r = this.name;
		}
		
		return r;
	}
	
	public boolean hidden;
	public int placement;
	
	public String user_def_format;
	
	public String case_storage;
	public String case_display;
	
	public String ext_column;
	public String descriptor_id;
	
	public String it_id;
	public int it_dom_id;
	public String runtime_item;
	public String par_multiple_vals;
	
	public String nullable;
	public String case_sensitive;
	
	public String parameter_optional;
	public String parameter_lov_desc_id;
	
	public String getDataType()
	{
		String r = "VARCHAR";
		
		switch (this.data_type)
		{
		case 1:
		case 3:
			r = "VARCHAR";
			break;
		case 2:
			r = "NUMBER";
			break;
		case 4:
			r = "DATE";
			break;
		case 5:
			// BINARY
		case 6:
			// TEXT
			break;
		case 7:
			// ROW ID
			r = "VARCHAR";
			break;
		case 8:
			// ID
			r = "VARCHAR";
			break;
		case 10:
			// join predicates
			break;
		default:
			System.out.println(".. unknown data type : " + this.data_type);
			break;
		}
		
		return r;
	}
	
	public void updateInfo(Connection con_eul, Map<Long, ModelBase> refitems)
		throws Exception
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		String sql = "SELECT " +
				"exp_id, " + 
				"exp_type, " + 
				"exp_name, " + 
				"exp_developer_key, " + 
				"exp_description, " + 
				"exp_formula1, " + 
				"exp_data_type, " + 
				"exp_sequence, " +
				"n11.obj_id, " + 
				"n11.obj_developer_key, " + 
				"n11.obj_name, " +
				"n11.obj_description " + 
				" FROM eul5_expressions n10 " +
				" INNER JOIN eul5_objs n11 ON n10.it_obj_id=n11.obj_id " +
				" WHERE exp_id=?";
		
		pstmt = con_eul.prepareStatement(sql);
		pstmt.setLong(1, this.id);
		
		rs = pstmt.executeQuery();
		
		if (rs.next())
		{
			this.obj = new BusinessObject();
			this.obj.id = rs.getLong("obj_id");
			this.obj.name = rs.getString("obj_name");
			this.obj.developer_key = rs.getString("obj_developer_key");
			
			this.id = rs.getLong("exp_id");
			this.name = rs.getString("exp_name");
			this.type = rs.getString("exp_type");
			this.developer_key = rs.getString("exp_developer_key");
			this.description = rs.getString("exp_description");
			this.setFormula(rs.getString("exp_formula1"));
			this.data_type = rs.getInt("exp_data_type");
			this.sequence = rs.getInt("exp_sequence");
			
			this.parseFormula(con_eul, refitems);
		}
		rs.close();
		rs = null;
		
		pstmt.close();
		pstmt = null;
		
		if (this.obj == null)
		{
			logger.error("item not found");
		}
	}
	
	public List<FormulaToken> formula_values;
	
	public void parseFormula(Connection con_eul, Map<Long, ModelBase> refitems)
		throws Exception
	{
		if (this.formula != null && this.formula.length() > 0)
		{
			FormulaLexer lexer = new FormulaLexer();
			lexer.setInput(this.formula);
			lexer.setRefItems(refitems);
			
			List<FormulaToken> t = lexer.parseInput(null);
			
			if (t.size() > 0 && t.get(0).token == FormulaToken.P_EXPRESSION && t.get(0).token_value == this.id)
			{
				// no need to parse
			}
			else
			{
				lexer.setInput(this.formula);
				
				this.formula_values = lexer.parseInput(con_eul);
			}
		}
	}
}
