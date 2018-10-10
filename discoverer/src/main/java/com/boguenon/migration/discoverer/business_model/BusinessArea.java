package com.boguenon.migration.discoverer.business_model;

import java.util.ArrayList;
import java.util.List;

public class BusinessArea
	extends ModelBase
{
	public String ext_name;
	
	public List<BusinessObject> objs;
	
	public BusinessArea()
	{
		objs = new ArrayList<BusinessObject>();
	}
}
