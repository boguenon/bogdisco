package com.boguenon.migration.discoverer;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boguenon.migration.discoverer.business_model.BusinessArea;
import com.boguenon.migration.discoverer.business_model.BusinessObject;
import com.boguenon.migration.discoverer.business_model.CalculationElement;
import com.boguenon.migration.discoverer.business_model.Domain;
import com.boguenon.migration.discoverer.business_model.Element;
import com.boguenon.migration.discoverer.business_model.Function;
import com.boguenon.migration.discoverer.business_model.ModelBase;
import com.boguenon.migration.discoverer.business_model.ObjectJoin;
import com.boguenon.migration.discoverer.logic.FormulaLexer;
import com.boguenon.migration.discoverer.logic.FormulaToken;
import com.boguenon.migration.discoverer.model.BaseItem;
import com.boguenon.migration.discoverer.model.Calculation;
import com.boguenon.migration.discoverer.model.Conditions;
import com.boguenon.migration.discoverer.model.DisplayedItem;
import com.boguenon.migration.discoverer.model.Document;
import com.boguenon.migration.discoverer.model.ElementRef;
import com.boguenon.migration.discoverer.model.EndUserLayerExport;
import com.boguenon.migration.discoverer.model.Parameters;
import com.boguenon.migration.discoverer.model.Sheet;
import com.boguenon.migration.discoverer.model.Workbook;

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class DiscoMigration 
{
	private static final Logger logger = LoggerFactory.getLogger(DiscoMigration.class);
	
	public static Map<Long, Function> eul_functions = null;
	
	private Properties configuration;
	private Map<String, String> cmdparams;
	
	private String tempfolder;
	
	private List<CalculationElement> calculation_custom_dims;
	private List<CalculationElement> calculation_custom_measures;
	
	public void execute(Map<String, String> cmdparams)
		throws Exception
	{
		String config_file = "migration.properties";
		
		this.cmdparams = cmdparams;
		configuration = new Properties();
		
		InputStream input = null;
		
		try
		{
			input = DiscoMigration.class.getClassLoader().getResourceAsStream(config_file);
			configuration.load(input);
			
			if (input != null)
				input.close();
			input = null;
		}
		catch (Exception ex)
		{
			logger.error("Error on configuration file." + config_file, ex);
		}
		finally
		{
		}
		
		String option = cmdparams.get("option");
		
		this.tempfolder = this.configuration.getProperty("temp_dir");
		
		if (tempfolder.endsWith("/") == false)
		{
			tempfolder += "/";
		}
		
		Connection con_eul = this.getConnection();
		
		if (option.equals("migrate_eex"))
		{
			this.execute_migrate_eex(con_eul);
		}
		else if (option.equals("migrate_bas"))
		{
			this.execute_migrate_bas(con_eul);
		}
		else if (option.equals("list_bas"))
		{
			this.execute_list_bas(con_eul);
		}
		else if (option.equals("list_workbooks"))
		{
			this.execute_list_workbooks(con_eul);
		}
		
		this.freeConnection(con_eul);
		con_eul = null;
	}
	
	private void extract_eex_file(Map<String, String> item)
	{
		String extract_cmd = this.configuration.getProperty("extract_program");
		extract_cmd = extract_cmd.trim();
		
		if (extract_cmd.startsWith("\"") && extract_cmd.endsWith("\""))
		{
			extract_cmd = extract_cmd.substring(1, extract_cmd.length() - 1);
		}
		
		logger.debug("extracting file : " + item.get("name"));
		
		String filename = item.get("id") + ".xml";
		item.put("filename", filename);
		
		File eex_file = new File(tempfolder + filename);
		
		if (eex_file.exists() == false)
		{
			String db_user_passwd = this.configuration.getProperty("db_user_passwd");
			String db_user_id = this.configuration.getProperty("db_user_id");
			
			extract_cmd = extract_cmd.replaceAll("\\$\\{db_user_passwd\\}", db_user_passwd);
			extract_cmd = extract_cmd.replaceAll("\\$\\{db_user_id\\}", db_user_id);
			extract_cmd = extract_cmd.replaceAll("\\$\\{temp_dir\\}", tempfolder);
			extract_cmd = extract_cmd.replaceAll("\\$\\{eex_filename\\}", filename);
			extract_cmd = extract_cmd.replaceAll("\\$\\{workbook_name\\}", "\"" + item.get("name") + "\"");
			
			executeShellscript(extract_cmd);
		}
	}
	
	private void execute_migrate_eex(Connection con_eul)
		throws Exception
	{
		List<Map<String, String>> items = this.execute_list_workbooks(con_eul);
		
		Map<String, String> item = null;
		String mitem = this.cmdparams.get("workbook");
		
		for (int i=0; i < items.size(); i++)
		{
			if (items.get(i).get("developer_key").equals(mitem) || items.get(i).get("id").equals(mitem) || items.get(i).get("name").equals(mitem))
			{
				item = items.get(i);
				break;
			}
		}
		
		extract_eex_file(item);
		
		String uid = item.get("id");
		String filename = tempfolder + item.get("filename");
		
		logger.debug("processing file : " + filename);
		
		File eex_file = new File(filename);
		
		if (eex_file.exists() == true && eex_file.canRead() == true)
		{
			// loading file
			logger.debug("pojo create java class for eex_file");
			
			JacksonXmlModule module = new JacksonXmlModule();
			// to default to using "unwrapped" Lists:
			module.setDefaultUseWrapper(false);
			
			XmlMapper xmlMapper = new XmlMapper(module);
			EndUserLayerExport eul_layer = xmlMapper.readValue(eex_file, EndUserLayerExport.class);
			
			assert(eul_layer != null);
			
			for (int j=0; j < eul_layer.documents.size(); j++)
			{
				this.calculation_custom_dims = new ArrayList<CalculationElement>();
				this.calculation_custom_measures = new ArrayList<CalculationElement>();
				this.migrateDocument(con_eul, eul_layer.documents.get(j));
			}

			logger.debug("loading pojo instance completed");
		}
		else
		{
			logger.error("file does not exist to migrate");
			throw new Exception("file does not exist to migrate");
		}
	}
	
	private void execute_migrate_bas(Connection con_eul)
		throws Exception
	{
		String mitem = this.cmdparams.get("target");
		
		List<BusinessArea> items = getBusinessArea(con_eul);
		
		BusinessArea ba = null;
		
		for (int i=0; i < items.size(); i++)
		{
			if (items.get(i).name.equals(mitem) || items.get(i).developer_key.equals(mitem))
			{
				ba = items.get(i);
				break;
			}
		}
		
		this.migrateBusinessArea(con_eul, ba);
	}
		
	private List<BusinessArea> getBusinessArea(Connection con_eul)
		throws Exception
	{
		List<BusinessArea> bas_list = new ArrayList<BusinessArea>();
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try
		{
			String sql = "SELECT DISTINCT ba_id, ba_name, ba_developer_key, ba_description "
					+ " FROM eul5_bas "
					+ " WHERE EXISTS (SELECT 1 FROM eul5_ba_obj_links WHERE bol_ba_id=ba_id)"
					+ " ORDER BY ba_name";
			
			pstmt = con_eul.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while (rs.next())
			{
				BusinessArea ba = new BusinessArea();
				ba.id = rs.getInt("ba_id");
				ba.name = rs.getString("ba_name");
				ba.developer_key = rs.getString("ba_developer_key");
				ba.description = rs.getString("ba_description");
				
				bas_list.add(ba);
			}
			
			rs.close();
			rs = null;
			
			pstmt.close();
			pstmt = null;
		}
		catch (Exception ex)
		{
			throw ex;
		}
		finally
		{
		}
		
		return bas_list;
	}
	
	private void execute_list_bas(Connection con_eul)
		throws Exception
	{
		List<BusinessArea> bas = this.getBusinessArea(con_eul);
		
		for (int i=0; i < bas.size(); i++)
		{
			String bas_item = bas.get(i).id + "," + bas.get(i).developer_key + "," + bas.get(i).name;
			System.out.println(bas_item);
		}
	}
	
	private List<Map<String, String>> execute_list_workbooks(Connection con_eul)
		throws Exception
	{
		List<Map<String, String>> items = new ArrayList<Map<String, String>>();
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try
		{
			String sql = "SELECT DISTINCT doc_id, doc_name, doc_developer_key, doc_description "
					+ " FROM eul5_documents "
					+ " ORDER BY doc_id";
			
			pstmt = con_eul.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while (rs.next())
			{
				Map<String, String> item = new HashMap<String, String>();
				item.put("id", Integer.toString(rs.getInt("doc_id")));
				item.put("developer_key", rs.getString("doc_developer_key"));
				item.put("name", rs.getString("doc_name"));
				item.put("description", rs.getString("doc_description"));
				String docinfo = "" + item.get("id") + "," + item.get("developer_key") + "," + item.get("name") + "," + item.get("description");
				System.out.println(docinfo);
				
				items.add(item);
			}
			
			rs.close();
			rs = null;
			
			pstmt.close();
			pstmt = null;
		}
		catch (Exception ex)
		{
			throw ex;
		}
		finally
		{
		}
		
		return items;
	}
	
	private void loadFunctions(Connection con_eul)
		throws Exception
	{
		if (DiscoMigration.eul_functions == null)
		{
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			
			String sql = "SELECT "
				+ "fun_id, fun_name, fun_developer_key, fun_description_s, fun_description_mn, " 
				+ "fun_function_type, fun_hidden, fun_data_type, fun_available, fun_maximum_args, " 
				+ "fun_minimum_args, fun_built_in, fun_ext_name, fun_ext_package, fun_ext_owner, " 
				+ "fun_ext_db_link, fun_user_prop2, fun_user_prop1, fun_element_state, "
				+ "fun_created_by, fun_created_date, fun_updated_by, fun_updated_date"
				+ " FROM eul5_functions";
			
			pstmt = con_eul.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			DiscoMigration.eul_functions = new HashMap<Long, Function>();
			
			while (rs.next())
			{
				Function f = new Function();
				f.id = rs.getLong("fun_id");
				f.name = rs.getString("fun_name");
				f.developer_key = rs.getString("fun_developer_key");
				f.description_s = rs.getString("fun_description_s");
				f.data_type = rs.getInt("fun_data_type");
				f.function_type = rs.getInt("fun_function_type");
				f.hidden = rs.getInt("fun_hidden") == 1 ? true : false;
				f.ext_name = rs.getString("fun_ext_name");
				f.ext_package = rs.getString("fun_ext_package");
				f.ext_owner = rs.getString("fun_ext_owner");
				f.ext_db_link = rs.getString("fun_ext_db_link");
				
				DiscoMigration.eul_functions.put(f.id, f);
			}
			
			rs.close();
			rs = null;
			
			pstmt.close();
			pstmt = null;
		}
	}
	
	private Map<String, Object> migrateBusinessArea(Connection con_eul, BusinessArea ba)
		throws Exception
	{
		this.loadFunctions(con_eul);
		
		Map<String, Object> result = new HashMap<String, Object>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		String sql = "SELECT ba_id, ba_name, ba_developer_key, ba_description, "
				+ "obj_id, obj_type, obj_name, obj_developer_key, obj_description, "  
				+ " obj_ba_id, obj_hidden, obj_distinct_flag, obj_ndeterministic, obj_cbo_hint, " 
				+ " obj_ext_object, obj_ext_owner, obj_ext_db_link, " 
				+ " obj_object_sql1, obj_object_sql2, obj_object_sql3, " 
				+ " sobj_ext_table, obj_user_prop1, obj_user_prop2 "
				+ " FROM eul5_bas n10 "
				+ " INNER JOIN eul5_ba_obj_links n11 on n10.ba_id=n11.bol_ba_id "
				+ " INNER JOIN eul5_objs n12 on n11.bol_obj_id=n12.obj_id "
				+ " WHERE ba_id=? "
				+ " ORDER BY ba_name";
		
		pstmt = con_eul.prepareStatement(sql);
		pstmt.setLong(1, ba.id);
		rs = pstmt.executeQuery();
		
		int n = 0;
		
		while (rs.next())
		{
			if (n == 0)
			{
				ba.id = rs.getInt("ba_id");
				ba.name = rs.getString("ba_name");
				ba.developer_key = rs.getString("ba_developer_key");
				ba.description = rs.getString("ba_description");
				ba.objs = new ArrayList<BusinessObject>();
			}
			
			BusinessObject bo = new BusinessObject();
			bo.id = rs.getInt("obj_id");
			bo.developer_key = rs.getString("obj_developer_key");
			bo.name = rs.getString("obj_name");
			
			bo.type = rs.getString("obj_type"); 
			
			if (bo.type.equals("SOBJ") == true)
			{
				bo.ext_table = rs.getString("sobj_ext_table");
			}
			else
			{
				StringBuilder object_sql = new StringBuilder();
				
				for (int i=1; i < 4; i++)
				{
					String tsql = rs.getString("obj_object_sql" + i);
					
					if (tsql != null && tsql.length() > 0)
					{
						object_sql.append(tsql);
					}
				}
												
				bo.setObject_sql(object_sql.length() > 0 ? object_sql.toString() : null);
			}
			
			bo.ext_object = rs.getString("obj_ext_object");
			bo.ext_owner = rs.getString("obj_ext_owner");
			bo.businessArea = ba;
			
			ba.objs.add(bo);
			n++;
		}
		
		rs.close();
		rs = null;
		
		pstmt.close();
		pstmt = null;
		
		if (n == 0)
			throw new Exception("business area not defined");
		
		// get joins
		sql = "SELECT key_id, key_type, key_name, key_developer_key, key_description,"
			+ " key_obj_id, fk_obj_id_remote, fk_one_to_one, fk_mstr_no_detail, "
			+ " fk_dtl_no_master, fk_mandatory "
			+ " FROM eul5_key_cons n10 "
			// + " INNER JOIN eul5_expressions n11 on n10.key_id=n11.jp_key_id "
			+ " WHERE key_obj_id IN (";
		
		for (int i=0; i < ba.objs.size(); i++)
		{
			sql += (i > 0 ? "," : "") + "?";
		}
		
		sql += ") OR fk_obj_id_remote IN (";
		
		for (int i=0; i < ba.objs.size(); i++)
		{
			sql += (i > 0 ? "," : "") + "?";
		}
		
		sql += ")";
		
		n = 0;
		pstmt = con_eul.prepareStatement(sql);
		
		for (int i=0; i < ba.objs.size(); i++)
		{
			pstmt.setLong(++n, ba.objs.get(i).id);
		}
		
		for (int i=0; i < ba.objs.size(); i++)
		{
			pstmt.setLong(++n, ba.objs.get(i).id);
		}
		
		rs = pstmt.executeQuery();
		
		List<ObjectJoin> joins = new ArrayList<ObjectJoin>();
		
		while (rs.next())
		{
			ObjectJoin join = new ObjectJoin();
			join.id = rs.getLong("key_id");
			join.name = rs.getString("key_name");
			join.type = rs.getString("key_type");
			join.developer_key = rs.getString("key_developer_key");
			join.description = rs.getString("key_description");
			join.key_obj_id = rs.getLong("key_obj_id");
			join.key_obj_id_remote = rs.getLong("fk_obj_id_remote");
			join.fk_one_to_one = rs.getInt("fk_one_to_one");
			join.fk_mstr_no_detail = rs.getInt("fk_mstr_no_detail");
			join.fk_dtl_no_master = rs.getInt("fk_dtl_no_master");
			join.fk_mandatory = rs.getInt("fk_mandatory");
			
			joins.add(join);
		}
		
		rs.close();
		rs = null;
		
		pstmt.close();
		pstmt = null;
		
		// create cube
		
		Set<String> table_names = new HashSet<String>();
		
		for (int i=0; i < ba.objs.size(); i++)
		{
			this.updateBusinessObjectElements(con_eul, ba.objs.get(i));
			
			// register lookups
			this.registerLookups(con_eul, ba.objs.get(i));
			
			Map<String, Object> r = this.registerBusinessObject(con_eul, ba.objs.get(i), joins);
			
			List<ObjectJoin> bajoins = (List<ObjectJoin>) r.get("joins");			
		}
		
		this.updateCubeJoins(con_eul, ba, joins);
				
		// update roles on object mcu
		sql = "SELECT eu_id, eu_username "
			+ " FROM eul5_access_privs n10 "
			+ " INNER JOIN eul5_eul_users n11 ON n10.ap_eu_id=n11.eu_id AND n10.ap_type=?"
			+ " WHERE n10.gba_ba_id=?";
		
		pstmt = con_eul.prepareStatement(sql);
		pstmt.setString(1, "GBA");
		pstmt.setLong(2, ba.id);
		
		rs = pstmt.executeQuery();
		
		List<String> roles = new ArrayList<String>();
		
		while (rs.next())
		{
			roles.add(rs.getString("eu_username"));
		}
		
		rs.close();
		rs = null;
		
		pstmt.close();
		pstmt = null;
		
		this.updateRoles(con_eul, roles);
		
		return result;
	}
	
	private void updateRoles(Connection con_eul, List<String> roles)
		throws Exception
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		String sql = null;
		
		Map<String, String> responsibilities = new HashMap<String, String>();
		
		// get roles with orbit and map to ebs roles
		try
		{
			sql = "SELECT responsibility_id, responsibility_name FROM apps.fnd_responsibility_tl";
			pstmt = con_eul.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			logger.debug("----- responsibilities");
			
			while (rs.next())
			{
				String resp_id = Long.toString(rs.getLong("responsibility_id"));
				String resp_name = rs.getString("responsibility_name");
				
				responsibilities.put(resp_name, resp_id);
				
				logger.debug("\t" + resp_name);
			}
			
			logger.debug("----- end of responsiblities");
		}
		catch (Exception ex)
		{
			logger.error("error on ebs database connection -> role synchronization ignored", ex);
		}
		finally
		{
			try
			{
				if (rs != null)
					rs.close();
				rs = null;
				
				if (pstmt != null)
					pstmt.close();
				pstmt = null;
			}
			catch (Exception eresource)
			{
			}
		}
		
		for (int i=0; i < roles.size(); i++)
		{
			String role = roles.get(i);
			
			if (role.startsWith("#") == true)
			{
				String role_id = role.substring(1);
				
				if (role_id.indexOf("#") > -1)
				{
					role_id = role_id.substring(0, role_id.indexOf("#"));
				}
				
				if (responsibilities.containsKey(role_id) == true )
				{
					String rsid = responsibilities.get(role_id);
				}
			}
		}
	}
	
	private void updateCubeJoins(Connection con_eul, BusinessArea ba, List<ObjectJoin> joins)
		throws Exception
	{
		if (joins.size() > 0)
		{
			for (int i=0; i < joins.size(); i++)
			{
				ObjectJoin jo = joins.get(i);
				jo.updateInfo(con_eul);
				
				Element jelement = jo.join_element;
				if (jelement == null)
				{
					logger.debug("join element not found : " + jo.id);
					// throw new Exception("join element not found : " + jo.id);
					continue;
				}
				
				logger.debug(jelement.getFormula());
				FormulaToken ptoken = jelement.formula_values.get(0);
				
				if (ptoken.pfunction != null && ptoken.pfunction.ext_name.equals("AND"))
				{
					for (int j=0; j < ptoken.params.size(); j++)
					{
						if (ptoken.params.get(j).pfunction != null && ptoken.params.get(j).pfunction.ext_name.equals("="))
						{
							Element psrc = ptoken.params.get(j).params.get(0).pelement;
							Element ptgt = ptoken.params.get(j).params.get(1).pelement;
							
							this.processJoins(ba, jo, psrc, ptgt);
						}
					}
				}
				else if (ptoken.pfunction != null && ptoken.pfunction.ext_name.equals("="))
				{
					Element psrc = ptoken.params.get(0).pelement;
					Element ptgt = ptoken.params.get(1).pelement;
					
					this.processJoins(ba, jo, psrc, ptgt);
				}
			}
		}
	}
	
	private void processJoins(BusinessArea ba, ObjectJoin jo, Element psrc, Element ptgt)
	{
		logger.debug(" ***** joins");
		
		logger.debug(" - from " + jo.key_obj_developer_key + "." + psrc.name);
		logger.debug(" - to   " + jo.key_obj_remote_developer_key + "." + ptgt.name);
	}
	
	private void migrateDocument(Connection con_eul, Document doc)
		throws Exception
	{
		try
		{
			logger.debug("** processing " + doc.Name);
			
			for (int i=0; i < doc.workbooks.size(); i++)
			{
				Workbook wb = doc.workbooks.get(i);
				logger.debug(" ** workbook " + wb.calculations);
				
				PreparedStatement pstmt = null;
				ResultSet rs = null;
				
				// update roles on object mcu
				String sql = "SELECT eu_id, eu_username "
					+ " FROM eul5_access_privs n10 "
					+ " INNER JOIN eul5_eul_users n11 ON n10.ap_eu_id=n11.eu_id AND n10.ap_type=?"
					+ " WHERE n10.gd_doc_id=?";
				
				pstmt = con_eul.prepareStatement(sql);
				pstmt.setString(1, "GD");
				pstmt.setLong(2, Long.parseLong(doc.id));
				
				rs = pstmt.executeQuery();
				
				List<String> roles = new ArrayList<String>();
				
				while (rs.next())
				{
					roles.add(rs.getString("eu_username"));
				}
				
				rs.close();
				rs = null;
				
				pstmt.close();
				pstmt = null;
				
				this.updateRoles(con_eul, roles);
				// end role update
				
				for (int j=0; j < wb.sheets.size(); j++)
				{
					// create report with sheet name
					Sheet sheet = wb.sheets.get(j);
					String sheetname = sheet.Name;
					
					logger.debug("----- sheet : " + sheetname);
					
					// mapping fields
					
					List<BusinessArea> ba_items = new ArrayList<BusinessArea>();
					
					List<BaseItem> xdisp = getDisplayedItem(con_eul, wb, ba_items, sheet.view.xDisplayItems);
					List<BaseItem> ydisp = getDisplayedItem(con_eul, wb, ba_items, sheet.view.yDisplayItems);
					List<BaseItem> measures = getDisplayedItem(con_eul, wb, ba_items, sheet.view.measureItems);
					
					if (ba_items.size() > 0)
					{
						for (int k=0; k < ba_items.size(); k++)
						{
							BusinessArea bobj = ba_items.get(k);
							
							logger.debug(bobj.name);
							
							// register lookups
							// this.registerLookups(con_meta, con_eul, mobj_cube_folder, dbobject, mobj_lookup, bobj);
							Map<String, Object> r = this.migrateBusinessArea(con_eul, bobj);
						}
					}
					
					// create report
					
					if (ba_items.size() > 1)
					{
						logger.debug("* multiple business area joins");
					}
										
					logger.debug("* distinct " + sheet.view.Distinct);
					
					logger.debug("----- x display fields");
					
					for (int k=0; k < xdisp.size(); k++)
					{
						this.appendSheetField(con_eul, wb, xdisp.get(k));
					}
					
					logger.debug("----- y display fields");
					
					for (int k=0; k < ydisp.size(); k++)
					{
						this.appendSheetField(con_eul, wb, ydisp.get(k));
					}
					
					logger.debug("----- measure display fields");
					
					for (int k=0; k < measures.size(); k++)
					{
						this.appendSheetField(con_eul, wb, measures.get(k));
					}
					
					if (sheet.view.parameters != null)
					{
						logger.debug("----- parameters");
						
						for (int k=0; k < sheet.view.parameters.size(); k++)
						{
							Parameters p = sheet.view.parameters.get(k);
							
							String devkey = p.DeveloperKey;
							
							Parameters t = (Parameters) wb.getByDeveloperKey(devkey);
							
							if (t == null && p.parameterReference != null)
							{
								for (int l=0; l < p.parameterReference.elementRef.uniqueIdent.size(); l++)
								{
									if (p.parameterReference.elementRef.uniqueIdent.get(l).ConstraintName.equals("OBJ1"))
									{
										devkey = p.parameterReference.elementRef.uniqueIdent.get(l).DeveloperKey;
										break;
									}
								}
								
								t = (Parameters) wb.getByDeveloperKey(devkey);
							}
							
							assert(t != null);
							
							if (t != null)
							{
								logger.debug(" - name " + t.getName());
								logger.debug(" - title " + t.Prompt);
								logger.debug(" - optional " + t.Optional);
								logger.debug(" - description " + t.Description);
								logger.debug(" - defaultvalue " + (t.AllowMultipleValues && t.DefaultValues != null && t.DefaultValues.length() > 0 ? t.DefaultValues : t.DefaultValue));
							}
						}
					}
					
					this.updateReportConditions(con_eul, wb, sheet);
					
					logger.debug("----- end of sheet\n\n");
				}
			}
		}
		catch (Exception ex)
		{
			throw ex;
		}
		finally
		{
		}
	}
	
	private void updateReportConditions(Connection con_eul, Workbook wb, Sheet sheet)
		throws Exception
	{
		if (sheet.view.conditions != null)
		{
			List<FormulaToken> conditions = new ArrayList<FormulaToken>();
			
			for (int i=0; i < sheet.view.conditions.size(); i++)
			{
				Conditions c = sheet.view.conditions.get(i);
				
				String devkey = null;
				
				Conditions wc = null;
				
				if (c.elementRef != null)
				{
					for (int j=0; j < c.elementRef.uniqueIdent.size(); j++)
					{
						if (c.elementRef.uniqueIdent.get(j).ConstraintName.equals("OBJ1"))
						{
							devkey = c.elementRef.uniqueIdent.get(j).DeveloperKey;
							break;
						}
					}
				}
				
				assert(devkey != null);
				
				if (devkey != null)
				{
					wc = (Conditions) wb.getByDeveloperKey(devkey);
				}
				
				if (wc != null)
				{
					if (wc.formula != null)
					{
						String formula = wc.formula.ExpressionString;
						
						if (formula != null && formula.length() > 0)
						{
							Map<Long, ModelBase> refitems = new HashMap<Long, ModelBase>();
							
							this.getRefItems(con_eul, wb, refitems, wc.formula.elementRef);
							
							FormulaLexer lexer = new FormulaLexer();
							lexer.setInput(formula);
							lexer.setRefItems(refitems);
							
							List<FormulaToken> t = lexer.parseInput(con_eul);
							
							assert(t != null && t.size() > 0);
							
							conditions.add(t.get(0));
						}
					}
				}
				else
				{
					logger.error("condition not found");
				}
			}
			
			if (conditions.size() > 0)
			{
				boolean is_single = true;
				
				for (int j=0; j < conditions.size(); j++)
				{
					if (conditions.get(j).pfunction.function_type == 3)
					{
						is_single = false;
						break;
					}
				}
				
				if (is_single == true)
				{
					logger.debug("----- filter condition");
					logger.debug(" - type group");
					
					for (int j=0; j < conditions.size(); j++)
					{
						this.appendFilter(con_eul, conditions.get(j));
					}
				}
				else
				{
					// TBD : multiple filter conditions
				}
			}
		}
	}
	
	private void getRefItems(Connection con_eul, Workbook wb, Map<Long, ModelBase> refitems, List<ElementRef> elementRef)
		throws Exception
	{
		if (elementRef != null)
		{
			for (int j=0; j < elementRef.size(); j++)
			{
				ElementRef ele = elementRef.get(j);
				long ename = Long.parseLong(ele.Name);
				
				if (ele.Type.equals("EulItem"))
				{
					String ba_key = null;
					String expression_name = null;
					
					if (ele.uniqueIdent.get(0).ConstraintName.equals("ITE1"))
					{
						String etype = ele.uniqueIdent.get(0).elementRef.Type;
						
						if (etype.equals("SimpleObject"))
						{
							for (int k=0; k < ele.uniqueIdent.get(0).elementRef.uniqueIdent.size(); k++)
							{
								if (ele.uniqueIdent.get(0).elementRef.uniqueIdent.get(k).ConstraintName.equals("OBJ1"))
								{
									ba_key = ele.uniqueIdent.get(0).elementRef.uniqueIdent.get(k).DeveloperKey; 
								}
							}
							
							expression_name = ele.uniqueIdent.get(1).Name;
						}
					}
					
					Element e = this.getElement(con_eul, ba_key, expression_name);
					
					assert(e != null);
					
					e.updateInfo(con_eul, null);
					
					refitems.put(ename, e);
				}
				else if (ele.Type.equals("Calculation"))
				{
					String calc_developer_key = null;
					
					for (int k=0; k < ele.uniqueIdent.size(); k++)
					{
						if (ele.uniqueIdent.get(k).ConstraintName.equals("OBJ1"))
						{
							calc_developer_key = ele.uniqueIdent.get(k).DeveloperKey;
							break;
						}
					}
					
					Calculation calc = (Calculation) wb.getByDeveloperKey(calc_developer_key);
					
					assert(calc != null);
					
					Map<Long, ModelBase> calc_refitems = new HashMap<Long, ModelBase>();
					
					this.getRefItems(con_eul, wb, calc_refitems, calc.formula.elementRef);
					
					CalculationElement celem = new CalculationElement();
					celem.id = ename;
					celem.setFormula(calc.formula.ExpressionString);
					celem.parseFormula(con_eul, calc_refitems);
					refitems.put(ename, celem);
				}
				else if (ele.Type.equals("Parameter"))
				{
					Parameters pc = null;
					
					if (ele.uniqueIdent != null)
					{
						String param_key = null;
						
						for (int k=0; k < ele.uniqueIdent.size(); k++)
						{
							if (ele.uniqueIdent.get(k).ConstraintName.equals("OBJ1"))
							{
								param_key = ele.uniqueIdent.get(k).DeveloperKey;
								break;
							}
						}
						
						pc = (Parameters) wb.getByDeveloperKey(param_key);
					}
					
					assert(pc != null);
					
					ModelBase p = new ModelBase();
					p.name = pc.Name;
					refitems.put(ename, p);
				}
				else
				{
					assert(true);
				}
			}
		}
	}
	
	private void appendFilter(Connection con_eul, FormulaToken t)
		throws Exception
	{
		if (t.pfunction.function_type == 1)
		{
			// 2 parameters
			FormulaToken tleft = t.params.get(0);
			
			if (tleft.token == FormulaToken.P_EXPRESSION)
			{
				if (tleft.pelement instanceof CalculationElement)
				{
					//TBD : calculation element to filter
					logger.error("calculation element expression");
					CalculationElement celem = (CalculationElement) tleft.pelement;
					
					this.getCalculationMetrics(con_eul, celem.formula_values);
					
					logger.debug("----- filter");
					logger.debug(" - type condition");
					logger.debug(" - operator " + t.pfunction.getOperator());
					logger.debug(" - is not " + t.pfunction.getIsNot());
					logger.debug(" - left expression" + celem.formula_values.get(0).getSQL());
				}
				else
				{
					this.findMetricField(con_eul, tleft.pelement);
					
					logger.debug("----- filter");
					logger.debug(" - operator " + t.pfunction.getOperator());
					logger.debug(" - is not " + t.pfunction.getIsNot());
				}
			}
		}
	}
	
	private void getCalculationMetrics(Connection con_eul, List<FormulaToken> tokens)
		throws Exception
	{
		for (int i=0; i < tokens.size(); i++)
		{
			if (tokens.get(i).token == FormulaToken.P_FUNCTION)
			{
				if (tokens.get(i).params != null)
				{
					this.getCalculationMetrics(con_eul, tokens.get(i).params);
				}
			}
			else if (tokens.get(i).token == FormulaToken.P_EXPRESSION)
			{
				if (tokens.get(i).pelement != null && tokens.get(i).pelement.obj != null)
				{
					Element pelem = tokens.get(i).pelement;
					
					this.findMetricField(con_eul, pelem);
				}
			}
		}
	}
	
	private Element getElement(Connection con_eul, String ba_key, String expression_name)
		throws Exception
	{
		Element ele = null;
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		String sql = "SELECT n10.exp_id, n10.exp_name, n10.exp_description "
				+ " FROM eul5_expressions n10 "
				+ " INNER JOIN eul5_objs n11 on n10.it_obj_id=n11.obj_id "
				+ " WHERE n11.obj_developer_key=? AND n10.exp_name=?";
		
		pstmt = con_eul.prepareStatement(sql);
		pstmt.setString(1, ba_key);
		pstmt.setString(2, expression_name);
		
		rs = pstmt.executeQuery();
		
		if (rs.next())
		{
			ele = new Element();
			ele.id = rs.getLong("exp_id");
			ele.name = rs.getString("exp_name");
			ele.description = rs.getString("exp_description");
		}
		
		rs.close();
		rs = null;
		
		pstmt.close();
		pstmt = null;
		
		return ele;
	}
	
	private void registerLookups(Connection con_eul, BusinessObject bobj)
		throws Exception
	{
		List<Element> lookups = new ArrayList<Element>();
		
		for (int i=0; i < bobj.elements.size(); i++)
		{
			if (bobj.elements.get(i).it_dom_id > 0)
			{
				lookups.add(bobj.elements.get(i));
			}
		}
		
		if (lookups.size() > 0)
		{
			logger.debug("----- register lookup");
			Map<String, BusinessObject> business_item_set = new HashMap<String, BusinessObject>();
			
			String sql = "SELECT distinct obj_id, obj_type, obj_name, obj_developer_key, obj_description, "
					+ " obj_ba_id, obj_hidden, obj_distinct_flag, obj_ndeterministic, obj_cbo_hint, "
					+ " obj_ext_object, obj_ext_owner, obj_ext_db_link, "
					+ " obj_object_sql1, obj_object_sql2, obj_object_sql3, "
					+ " sobj_ext_table, obj_user_prop1, obj_user_prop2, "
					+ " n12.dom_id, n12.dom_name, n12.dom_developer_key, n12.dom_description, "
					+ " n12.dom_it_id_lov, n12.dom_it_id_rank "
					+ " FROM eul5_objs n10 "
					+ " INNER JOIN eul5_expressions n11 on n10.obj_id=n11.it_obj_id"
					+ " INNER JOIN eul5_domains n12 on n11.exp_id=n12.dom_it_id_lov OR n11.exp_id=n12.dom_it_id_rank "
					+ " WHERE n12.dom_id IN (";
			

			for (int i=0; i < lookups.size(); i++)
			{
				if (i > 0)
					sql += ",";
				sql += "?";
			}
			
			sql += ")";
			
			List<BusinessObject> bo_items = new ArrayList<BusinessObject>();
			
			if (lookups.size() > 0)
			{
				PreparedStatement pstmt = con_eul.prepareStatement(sql);
				
				int n = 1;
				
				for (int i=0; i < lookups.size(); i++)
				{
					pstmt.setInt(n++, lookups.get(i).it_dom_id);
				}
				
				ResultSet rs = pstmt.executeQuery();
				
				List<Domain> domains = new ArrayList<Domain>();
				
				while (rs.next())
				{
					BusinessObject bo = new BusinessObject();
					bo.id = rs.getInt("obj_id");
					bo.developer_key = rs.getString("obj_developer_key");
					bo.name = rs.getString("obj_name");
					
					int dom_id = rs.getInt("dom_id");
					
					Domain domain = new Domain();
					domain.id = dom_id;
					
					domain.name = rs.getString("dom_name");
					domain.description = rs.getString("dom_description");
					domain.developer_key = rs.getString("dom_developer_key");
					domain.it_id_lov = rs.getInt("dom_it_id_lov");
					domain.it_id_rank = rs.getInt("dom_it_id_rank");
					
					if (business_item_set.containsKey(bo.developer_key))
					{
						domain.ref_bo = business_item_set.get(bo.developer_key);
						domains.add(domain);
						continue;
					}
					
					domain.ref_bo = bo;
					domains.add(domain);
					
					bo.type = rs.getString("obj_type");
					
					if (bo.type.equals("SOBJ"))
					{
						bo.ext_table = rs.getString("sobj_ext_table");
					}
					else
					{
						StringBuilder object_sql = new StringBuilder();
						
						for (int i=1; i < 4; i++)
						{
							String tsql = rs.getString("obj_object_sql" + i);
							
							if (tsql != null && tsql.length() > 0)
							{
								object_sql.append(tsql);
							}
						}
														
						bo.setObject_sql(object_sql.length() > 0 ? object_sql.toString() : null);
					}
					 
					bo.ext_object = rs.getString("obj_ext_object");
					bo.ext_owner = rs.getString("obj_ext_owner");
					
					business_item_set.put(bo.developer_key, bo);
					bo_items.add(bo);
				}
				
				rs.close();
				rs = null;
				
				pstmt.close();
				pstmt = null;
				
				for (int i=0; i < bo_items.size(); i++)
				{
					this.updateBusinessObjectElements(con_eul, bo_items.get(i));
					
					Map<String, Object> result = this.registerBusinessObject(con_eul, bo_items.get(i), null);
					
					for (int j=0; j < domains.size(); j++)
					{
						if (domains.get(j).ref_bo.developer_key.equals(bo_items.get(i).developer_key))
						{
							this.registerDomain(con_eul, domains.get(j));
						}
					}
				}
			}
		}
	}
	
	private void registerDomain(Connection con_eul, Domain domain)
		throws Exception
	{
		logger.debug("----- register domain");
		logger.debug(" - name " + domain.name);
		logger.debug(" - description " + domain.description);
	}
	
	private void findMetricField(Connection con_eul, BaseItem item)
		throws Exception
	{
		List<String> object_key = item.getObjectKey();
		
		if (object_key != null && object_key.size() > 0)
		{
			String obj_key = object_key.get(0);
			String exp_name = item.getName();
			
			findMetricField(con_eul, obj_key, exp_name);
		}
	}
	
	private void findMetricField(Connection con_eul, Element item)
		throws Exception
	{
		String obj_key = item.obj.developer_key;
		String exp_name = item.getAlias();
		
		findMetricField(con_eul, obj_key, exp_name);
	}
	
	private void findMetricField(Connection con_eul, String obj_key, String exp_name)
		throws Exception
	{
		BusinessObject bobj = this.getBusinessObject(con_eul, obj_key);
		
		if (bobj == null)
		{
			logger.error(" not found object item");
		}
		else
		{
			logger.debug("***** field : " + bobj.getAliasName());
		}
	}
	
	private void appendSheetField(Connection con_eul, Workbook wb, BaseItem item)
		throws Exception
	{
		DisplayedItem ditem = (DisplayedItem) item;
		
		if (ditem.item != null && ditem.item.elementRef != null && ditem.item.elementRef.Type.equals("Calculation"))
		{
			String dkey = null;
			
			for (int i=0; i < ditem.item.elementRef.uniqueIdent.size(); i++)
			{
				if (ditem.item.elementRef.uniqueIdent.get(i).ConstraintName.equals("OBJ1"))
				{
					dkey = ditem.item.elementRef.uniqueIdent.get(i).DeveloperKey;
					break;
				}
			}
			
			Calculation calc = (Calculation) wb.getByDeveloperKey(dkey);
			CalculationElement dim = null;
			
			if (calc != null)
			{
				for (int i=0; i < this.calculation_custom_dims.size(); i++)
				{
					if (this.calculation_custom_dims.get(i).name.equals(calc.Name))
					{
						dim = calculation_custom_dims.get(i);
						break;
					}
				}
				
				if (dim == null)
				{
					for (int i=0; i < calculation_custom_measures.size(); i++)
					{
						if (calculation_custom_measures.get(i).name.equals(calc.Name))
						{
							dim = calculation_custom_measures.get(i);
							break;
						}
					}
				}
				
				if (dim == null)
				{
					Map<Long, ModelBase> calc_refitems = new HashMap<Long, ModelBase>();
					
					this.getRefItems(con_eul, wb, calc_refitems, calc.formula.elementRef);
					
					CalculationElement celem = new CalculationElement();
					celem.setFormula(calc.formula.ExpressionString);
					celem.parseFormula(con_eul, calc_refitems);
					
					boolean is_aggregation = celem.formula_values.get(0).is_aggregation();

					if (is_aggregation == true)
					{
						logger.debug(" expression " + celem.formula_values.get(0).getSQL());
						this.getCalculationMetrics(con_eul, celem.formula_values);
						
						calculation_custom_measures.add(dim);
					}
					else
					{
						logger.debug("----- custom field");
						logger.debug("expression " + celem.formula_values.get(0).getSQL());
						
						calculation_custom_dims.add(celem);
					}
					
					dim.name = calc.Name;
				}
			}
			else
			{
				logger.error("calculation not found");
			}
		}
	}
	
	private BusinessObject getBusinessObject(Connection con_eul, String obj_key)
		throws Exception
	{
		BusinessObject bobj = null;
		
		
		return bobj;
	}
	
	private Map<String, Object> registerBusinessObject(Connection con_eul, BusinessObject bo, List<ObjectJoin> joins)
		throws Exception
	{
		Map<String, Object> result = new HashMap<String, Object>();
		// create physical object
		// schema lists
		
		logger.debug(" ***** register business object");
		
		if (bo.ext_owner == null)
			bo.ext_owner = "APPS";
		
		logger.debug(" - schema name : " + bo.ext_owner);
		
		logger.debug(" - object type : " + (bo.ext_table != null && bo.ext_table.length() > 0 ? "table" : "inlineview"));
		
		logger.debug("***** writing table : " + bo.getObjectName() + "," + bo.getAliasName());
		
		// register columns
		for (int i=0; i < bo.elements.size(); i++)
		{
			Element ele = bo.elements.get(i);
			
			if (ele.type.equals("CO"))
			{
				logger.debug(" - column " + ele.getObjectName());
				logger.debug(" - field " + ele.ext_column != null ? ele.ext_column : ele.name);
				logger.debug(" - description " + ele.description);
				logger.debug(" - datatype " + ele.getDataType());
				logger.debug(" - alias "  + ele.getAlias());
			}
			else if (ele.type.equals("CI"))
			{
				ele.updateInfo(con_eul, null);
				
				if (ele.formula_values != null && ele.formula_values.size() > 0)
				{
					FormulaToken ftoken = ele.formula_values.get(0);
					boolean is_aggregation = ftoken.is_aggregation();
					
					if (is_aggregation == true)
					{
						logger.debug(" - aggregation true");
						logger.debug(" - expression " + ftoken.getSQL());
					}
					else
					{
						logger.debug(" - calculated field true");
						logger.debug(" - expression " +  ftoken.getSQL());
					}
					logger.debug("name " + ele.getObjectName());
				}
			}
		}

		List<ObjectJoin> ba_joins = new ArrayList<ObjectJoin>();
		
		if (joins != null)
		{
			for (int i=0; i < joins.size(); i++)
			{
				if (joins.get(i).key_obj_id == bo.id || joins.get(i).key_obj_id_remote == bo.id)
				{
					ba_joins.add(joins.get(i));
				}
			}
		}
		
		result.put("joins", ba_joins);
		
		return result;
	}
	
	private List<BaseItem> getDisplayedItem(Connection con_eul, Workbook wb, List<BusinessArea> ba_items, List<DisplayedItem> sheet_items)
		throws Exception
	{
		List<BaseItem> items = new ArrayList<BaseItem>();
		
		if (sheet_items != null)
		{
			for (int k=0; k < sheet_items.size(); k++)
			{
				DisplayedItem disp = sheet_items.get(k);
				String devkey = disp.DeveloperKey;
				
				if (devkey == null && disp.elementRef != null)
				{
					for (int l=0; l < disp.elementRef.uniqueIdent.size(); l++)
					{
						if (disp.elementRef.uniqueIdent.get(l).ConstraintName.equals("OBJ1"))
						{
							devkey = disp.elementRef.uniqueIdent.get(l).DeveloperKey;
							break;
						}
					}
				}
				
				BaseItem t = wb.getByDeveloperKey(devkey);
				
				if (t == null)
				{
					logger.debug("item not found");
				}
				else
				{
					logger.debug(t.getName());
					items.add(t);
					
					List<String> ekey = t.getObjectKey();
					
					if (ekey == null || (ekey != null && ekey.size() == 0))
					{
						logger.error("element reference node error");
					}
					else
					{
						String sql = "SELECT DISTINCT "
								+ " n12.ba_id, n12.ba_name, n12.ba_developer_key, n12.ba_description "
								+ " FROM eul5_objs n10 "
								+ " INNER JOIN eul5_ba_obj_links n11 on n10.obj_id=n11.bol_obj_id "
								+ " INNER JOIN eul5_bas n12 on n12.ba_id=n11.bol_ba_id "
								+ " WHERE obj_developer_key IN (";
						
						for (int i=0; i < ekey.size(); i++)
						{
							if (i > 0)
								sql += ",";
							sql += "?";
						}
						
						sql += ")";
						
						PreparedStatement pstmt = con_eul.prepareStatement(sql);
						
						for (int i=0; i < ekey.size(); i++)
						{
							pstmt.setString(i+1, ekey.get(i));
						}
						
						ResultSet rs = pstmt.executeQuery();
						
						while (rs.next())
						{
							BusinessArea ba = new BusinessArea();
							ba.id = rs.getInt("ba_id");
							ba.developer_key = rs.getString("ba_developer_key");
							ba.name = rs.getString("ba_name");
							ba.description = rs.getString("ba_description");
							
							ba_items.add(ba);
						}
						
						rs.close();
						rs = null;
						
						pstmt.close();
						pstmt = null;
					}
				}
			}
		}
		
		return items;
	}
	
	private void updateBusinessObjectElements(Connection con_eul, BusinessObject bo)
		throws Exception
	{
		String sql = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		if (bo.type.equals("CUO") || (bo.type.equals("COBJ") && bo.getObject_sql() == null))
		{
			// getting segment sql
			sql = "SELECT seg_id, seg_seg_type, seg_sequence, seg_obj_id, "
				+ " seg_chunk1, seg_chunk2, seg_chunk3, seg_chunk4"
				+ " FROM eul5_segments"
				+ " WHERE ";
			
			if (bo.type.equals("CUO"))
			{
				sql += " seg_obj_id=?";
			}
			else
			{
				sql += " seg_obj_id=?"; 
			}
			sql += " ORDER BY seg_sequence";
			
			pstmt = con_eul.prepareStatement(sql);
			
			pstmt.setLong(1, bo.id);
			
			rs = pstmt.executeQuery();
			
			StringBuilder object_sql = new StringBuilder();
			
			while (rs.next())
			{
				for (int j=1; j < 5; j++)
				{
					String tsql = rs.getString("seg_chunk" + j);
					
					if (tsql != null && tsql.length() > 0)
					{
						object_sql.append(tsql);
					}
				}
			}
			
			rs.close();
			rs = null;
			
			pstmt.close();
			pstmt = null;
			
			bo.setObject_sql(object_sql.length() > 0 ? object_sql.toString() : null);
		}
		
		// getting elements
		sql = "SELECT exp_id, exp_type, exp_name, exp_developer_key, exp_description,"
			+ " exp_formula1, exp_data_type, exp_sequence, it_format_mask, "
			+ " it_max_data_width, it_max_disp_width, it_alignment, it_word_wrap, it_disp_null_val, "
			+ " it_fun_id, it_heading, it_hidden, it_placement, it_case_storage, it_dom_id, "
			+ " it_case_display, it_ext_column "
			+ " FROM EUL5_EXPRESSIONS "
			+ " WHERE it_obj_id=? "
			+ " ORDER BY exp_sequence";
		
		pstmt = con_eul.prepareStatement(sql);
		
		pstmt.setLong(1, bo.id);
		
		rs = pstmt.executeQuery();
		
		List<Element> seg_elements = new ArrayList<Element>();
		
		while (rs.next())
		{
			Element exp = new Element();
			exp.id = rs.getLong("exp_id");
			exp.type = rs.getString("exp_type");
			exp.developer_key = rs.getString("exp_developer_key");
			exp.description = rs.getString("exp_description");
			String exp_formula1 = rs.getString("exp_formula1");
			if (exp_formula1 != null && exp_formula1.length() > 0)
			{
				exp.setFormula(exp_formula1);
			}
			else
			{
				seg_elements.add(exp);
			}
			exp.name = rs.getString("exp_name");
			exp.ext_column = rs.getString("it_ext_column"); 
			exp.heading = rs.getString("it_heading");
			exp.data_type = rs.getInt("exp_data_type");
			exp.it_dom_id = rs.getInt("it_dom_id");
			
			bo.elements.add(exp);
		}
		
		rs.close();
		rs = null;
		
		pstmt.close();
		pstmt = null;
		
		if (seg_elements.size() > 0)
		{
			sql = "SELECT seg_id, seg_seg_type, seg_sequence, seg_exp_id, "
				+ " seg_chunk1, seg_chunk2, seg_chunk3, seg_chunk4"
				+ " FROM eul5_segments"
				+ " WHERE "
				+ " seg_exp_id=?";
			sql += " ORDER BY seg_sequence";
			pstmt = con_eul.prepareStatement(sql);
			
			for (int i=0; i < seg_elements.size(); i++)
			{
				pstmt.setLong(1, seg_elements.get(i).id);
			
				rs = pstmt.executeQuery();
			
				StringBuilder object_sql = new StringBuilder();
			
				while (rs.next())
				{
					for (int j=1; j < 5; j++)
					{
						String tsql = rs.getString("seg_chunk" + j);
						
						if (tsql != null && tsql.length() > 0)
						{
							object_sql.append(tsql);
						}
					}
				}
				
				seg_elements.get(i).setFormula(object_sql.toString());
			
				rs.close();
				rs = null;
			}

			pstmt.close();
			pstmt = null;
		}
	}
	
	private Connection getConnection()
		throws Exception
	{
		Class.forName(configuration.getProperty("db_jdbc_driver"));
		Connection con_eul = DriverManager.getConnection(configuration.getProperty("db_jdbc_url"), configuration.getProperty("db_user_id"), configuration.getProperty("db_user_passwd"));
		
		return con_eul;
	}
	
	private void freeConnection(Connection con)
		throws Exception
	{
		if (con != null)
			con.close();
	}
	
	private void executeShellscript(String cmd)
	{
		Runtime r = null;
		Process p = null;
		
		logger.debug(cmd);
		
		try
		{
			r = Runtime.getRuntime();
			p = r.exec(cmd);
			
			BufferedReader reader=new BufferedReader(new InputStreamReader(p.getInputStream())); 
		    String line=reader.readLine(); 
		    while(line!=null) 
		    { 
			    logger.debug(line);
			    line=reader.readLine();
		    } 
		    
		    p.waitFor();
		    logger.debug(">> command execution completed <<");
		}
		catch (Exception ex)
		{
			logger.error("Exception while execute command.", ex);
		}
	}
	
	public static String getDateTime()
	{
		Date date = new Date();
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
		
		return formatter.format(date);
	}
}
