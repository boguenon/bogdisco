package com.boguenon.migration.discoverer;

import java.util.HashMap;
import java.util.Map;

public class App 
{
    public static void main(String[] args)
    {
    	// parsing command line arguments
    	Map<String, String> params = new HashMap<String, String>();
		
		for (int i=0; i < args.length; i++)
		{
			String arg = args[i]; 
			String value = null;
			
			if (arg.startsWith("--") == true)
			{
				arg = arg.substring(2);
				
				int n = arg.indexOf("=");
				if (n > -1)
				{
					value = arg.substring(n+1);
					arg = arg.substring(0, n);
				}
				
				if (value != null && value.equals("") == false)
				{
					params.put(arg, value);
				}
			}
		}
		
    	String option = params.get("option");
    	
    	if (option == null)
    	{
    		printUsage();
    	}
    	else
    	{
	    	try
	    	{
		        DiscoMigration migration = new DiscoMigration();
		        migration.execute(params);
	    	}
	    	catch (Exception ex)
	    	{
	    		ex.printStackTrace();
	    	}
    	}
    }
    
    private static void printUsage()
    {
    	System.out.println(">> usage");
    	System.out.println("--option=migrate_eex / migrate_bas / list_bas");
    }
}
