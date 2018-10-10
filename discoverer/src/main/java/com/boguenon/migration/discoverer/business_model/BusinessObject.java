package com.boguenon.migration.discoverer.business_model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusinessObject
	extends ModelBase
{
	private static final Logger logger = LoggerFactory.getLogger(BusinessObject.class);
			
	public String type;
	public boolean hidden;
	public boolean distinct_flag;
	
	public List<Element> elements;
	
	public String cbo_hint;
	public String ext_object;
	public String ext_owner;
	public String ext_db_link;
	
	private String object_sql;
	
	public String getAliasName()
	{
		return name;
	}
	
	public String getObjectName()
	{
		if (this.type.equals("SOBJ"))
		{
			return this.ext_table;
		}
		
		return name;
	}
	
	public BusinessArea businessArea;
	
	public String getObject_sql() {
		return object_sql;
	}

	public void setObject_sql(String object_sql) {
		if (object_sql != null && object_sql.length() > 0)
		{
			object_sql = object_sql.replaceAll("\\\\r", "");
			object_sql = object_sql.replaceAll("\\\\n", "\n");
			
			if (object_sql.startsWith("( SELECT ") == true)
			{
				logger.debug(object_sql);
				object_sql = object_sql.substring(object_sql.indexOf(" FROM (") + " FROM (".length());
				object_sql = object_sql.substring(0, object_sql.lastIndexOf(")"));
				
				int n = 0;
				
				for (int i=0; i < 1000; i++)
				{
					if (object_sql.charAt(i) != ' ')
					{
						n = i;
						break;
					}
				}
				
				if (n > 0)
				{
					object_sql = object_sql.substring(n);
				}
			}
		}
		this.object_sql = object_sql;
	}

	public String ext_table;
	
	public BusinessObject()
	{
		elements = new ArrayList<Element>();
	}
}
