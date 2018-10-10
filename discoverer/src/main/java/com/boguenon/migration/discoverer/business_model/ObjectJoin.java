package com.boguenon.migration.discoverer.business_model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ObjectJoin 
	extends ModelBase
{
	public String type;
	public long key_obj_id;
	public String key_obj_developer_key;
	public String key_obj_name;
	
	public List<BusinessArea> key_ba = new ArrayList<BusinessArea>();
	
	public long key_obj_id_remote;
	public String key_obj_remote_developer_key;
	public String key_obj_remote_name;
	
	public List<BusinessArea> key_ba_remote = new ArrayList<BusinessArea>();
	
	public int fk_one_to_one;
	public int fk_mstr_no_detail;
	public int fk_dtl_no_master;
	public int fk_mandatory;
	
	public Element join_element = null;
	
	public void updateInfo(Connection con_eul)
		throws Exception
	{
		if (key_obj_developer_key != null)
			return;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		String sql = "SELECT obj_id, obj_developer_key, obj_name,"
				+ " ba_id, ba_developer_key, ba_name "
				+ " FROM eul5_objs n10"
				+ " INNER JOIN eul5_ba_obj_links n11 on n10.obj_id=n11.bol_obj_id"
				+ " INNER JOIN eul5_bas n12 on n11.bol_ba_id=n12.ba_id"
				+ " WHERE obj_id IN (?, ?)";
		
		pstmt = con_eul.prepareStatement(sql);
		pstmt.setLong(1, key_obj_id);
		pstmt.setLong(2, key_obj_id_remote);
		
		rs = pstmt.executeQuery();
		
		while (rs.next())
		{
			long obj_id = rs.getLong("obj_id");
			
			if (obj_id == key_obj_id)
			{
				key_obj_developer_key = rs.getString("obj_developer_key");
				key_obj_name = rs.getString("obj_name");
				
				BusinessArea ba = new BusinessArea();
				ba.id = rs.getLong("ba_id");
				ba.developer_key = rs.getString("ba_developer_key");
				ba.name = rs.getString("ba_name");
				
				this.key_ba.add(ba);
			}
			else if (obj_id == key_obj_id_remote)
			{
				key_obj_remote_developer_key = rs.getString("obj_developer_key");
				key_obj_remote_name = rs.getString("obj_name");
				
				BusinessArea ba = new BusinessArea();
				ba.id = rs.getLong("ba_id");
				ba.developer_key = rs.getString("ba_developer_key");
				ba.name = rs.getString("ba_name");
				
				this.key_ba_remote.add(ba);
			}
		}
		
		rs.close();
		rs = null;
		
		pstmt.close();
		pstmt = null;
		
		sql = "SELECT exp_id, exp_developer_key, exp_type, exp_name, exp_description "
			+ "FROM eul5_expressions WHERE jp_key_id=?";
		
		pstmt = con_eul.prepareStatement(sql);
		pstmt.setLong(1, this.id);
		
		rs = pstmt.executeQuery();
		
		if (rs.next())
		{
			Element ele = new Element();
			ele.id = rs.getLong("exp_id");
			ele.developer_key = rs.getString("exp_developer_key");
			ele.type = rs.getString("exp_type");
			ele.name = rs.getString("exp_name");
			ele.description = rs.getString("exp_description");
			
			this.join_element = ele;
			
			ele.updateInfo(con_eul, null);
		}
		
		rs.close();
		rs = null;
		
		pstmt.close();
		pstmt = null;
	}
}
