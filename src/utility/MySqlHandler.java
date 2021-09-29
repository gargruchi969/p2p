package utility;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.skife.jdbi.v2.Batch;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;

import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import p2pApp.p2pIndexer.TableHandler;

public class MySqlHandler {

	private static MySqlHandler mysqlHandler;
	private String dbName="p2p_app";
	private MysqlDataSource dataSource;
	private DBI dbi;
	
	private MySqlHandler(){
		try{
			loadDatabase(dbName);
		}
		catch(Exception e){
			System.out.println("Database Exception #1:"+ e.getMessage());
		}
	}
	
	public static MySqlHandler getInstance(){
		if(mysqlHandler==null)
			mysqlHandler= new MySqlHandler();
		return mysqlHandler;
	}
		
	public MysqlDataSource getProperties(String dbName) throws Exception{
		Properties props = new Properties();
        FileInputStream fis = null;
        MysqlDataSource ds = null;

        fis = new FileInputStream("data/db-ms.properties");
        props.load(fis);
        
        ds = new MysqlConnectionPoolDataSource();

        String pass=props.getProperty("mysql.password");
        if(pass==null)
        	pass="";

        String url= props.getProperty("mysql.url")+":"+props.getProperty("mysql.port")+"/";
        
        createDatabase(url, 
        		props.getProperty("mysql.username"), pass,
        		dbName);
        
        ds.setURL(url+dbName+"?"+props.getProperty("mysql.unicode"));
        ds.setUser(props.getProperty("mysql.username"));
        ds.setPassword(pass);

        return ds;
	}
	
	public String getDatabase(){
		return dbName;
	}
	
	public void loadDatabase(String dbName) throws Exception{
		
		dataSource= getProperties(dbName);
		Class.forName("com.mysql.jdbc.Driver");
    	dbi = new DBI(dataSource);
	}
	
	private void createDatabase(String url, String user, String pass, String dbName){
		Connection connection = null;
		Statement statement = null;
	    try {
	        Class.forName("com.mysql.jdbc.Driver");
	        connection = DriverManager.getConnection(url,
	                user, pass);
	        statement = connection.createStatement();
	        String sql = "CREATE DATABASE "+dbName;
	        statement.executeUpdate(sql);
	        connection.close();
	        
	    } catch (SQLException sqlException) {
	        if (sqlException.getErrorCode() == 1007) {
	            
	        } 
	        else if(sqlException.getErrorCode()== 1044 || sqlException.getErrorCode()== 1045){
	        	System.out.println("Database Exception #4: Check for database permissions. "+sqlException.getMessage());
	        }
	        else {
	            System.out.println("Database Exception #4: Check for database connectivity. "+sqlException.getMessage());
	        }
	    } catch (ClassNotFoundException e) {
	        
	    }
	}
	public List<Map<String, Object>> fetchQuery(String sql){
		
		Handle handle = null;
        
        List<Map<String, Object>> l=new ArrayList<Map<String, Object>>();
        
        try {
            handle = dbi.open();
            Query<Map<String, Object>> q = handle.createQuery(sql);
            l = q.list();
        } 
        catch(Exception e){
        	System.out.println("Database Exception #2: "+e.getMessage());
        }
        finally {
            if (handle != null) {
                handle.close();
            }
        }
        return l;
	}
	
	public void insertSingle(String tblName, String[] columns, String[] values){
		Handle handle= null;
		
		try {
            handle = dbi.open();
            
            String cms= "";
			for(int i=0; i<columns.length;i++){
				if(i==0) 
					cms= columns[i];
				else
					cms= cms+", "+columns[i];
			}
			
			String vs= "";
			for(int j=0; j<values.length;j++){
				if(j==0) 
					vs= "'"+values[j]+"'";
				else
					vs= vs+", "+"'"+values[j]+"'";
			}
			
            handle.execute("INSERT into "+tblName+" ( "+ cms+")" +"VALUES " +"( "+vs+")");
        } 
		catch(UnableToExecuteStatementException e){
			if(e.getCause() instanceof MySQLIntegrityConstraintViolationException){
				updateTable(TableHandler.TblName, "Hash", values[3], "Path", values[2].replace("\\", "/").replace("'", "''"));
			}
		}
        catch(Exception e){
				System.out.println("Database Exception #4: "+e.getMessage());
        }
        finally {
            if (handle != null) {
                handle.close();
            }
        }
		
	}
	
	public void insertMultiple(String tblName, String[] columns, List<String[]> values){
		
		Handle handle = null;
		try{
			handle= dbi.open();
			Batch batch= handle.createBatch();
			String cms= "";
			for(int i=0; i<columns.length;i++){
				if(i==0) 
					cms= columns[i];
				else
					cms= cms+", "+columns[i];
			}
			
			for(int i=0;i<values.size();i++){
				
				String vs= "";
				String temp[]= values.get(i);
				for(int j=0; j<temp.length;j++){
					if(j==0) 
						vs= "'"+temp[j]+"'";
					else
						vs= vs+", "+"'"+temp[j]+"'";
				}
				
				batch.add("INSERT into "+tblName+" ("+cms+")" + "VALUES "+ "("+ vs+")");
			}
			batch.execute();
		}
		catch(Exception e){
			if(e instanceof MySQLIntegrityConstraintViolationException){
			}
			else
				System.out.println("Database Exception #4: "+e.getMessage());
		}
		finally {
            if (handle != null) {
                handle.close();
            }
        }
	}
	
	public void createTable(String tblName, String[] data, String engine, boolean force){
		
		if(engine==null)
			engine= "MyISAM";
		
		Handle handle= null;
		try {
            handle = dbi.open();
            String cms= "";
			for(int i=0; i<data.length;i++){
				if(i==0) 
					cms= data[i];
				else
					cms= cms+", "+data[i];
			}
			if(force)
				handle.execute("DROP TABLE IF EXISTS "+tblName);
            handle.execute("CREATE table "+tblName+" ( "+ cms+") ENGINE="+engine);
        } 
		
        catch(Exception e){
        	System.out.println("Database Exception #5: "+e.getMessage());
        }
        finally {
            if (handle != null) {
                handle.close();
            }
        }
	}
	
	public void updateTable(String tblName, String setVariable, String setValue, String whereVar, String whereVal){
		
		Handle handle= null;
		try {
            handle = dbi.open();
            handle.execute("UPDATE "+tblName+" SET "+setVariable+" = "+ "'"+ setValue+ "'" +" WHERE "+whereVar+ "= "+ "'"+ whereVal +"'");
        } 
        catch(Exception e){
        	if(e instanceof MySQLIntegrityConstraintViolationException){
			}
			else
				System.out.println("Database Exception #4: "+e.getMessage());
        }
        finally {
            if (handle != null) {
                handle.close();
            }
        }
	}
	
	public void TestDatabase() throws Exception{
		Handle handle= null;
		
			handle= dbi.open();
			handle.execute("USE "+dbName);
		handle.close();
	}

	
}
