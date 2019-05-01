/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rgprotect;
import java.io.File;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author kerne
 */
public class db {
	public static Connection conn;
	public static Statement statmt;
	public static ResultSet resSet;
        String JarPath = db.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	String dirPath = new File(JarPath).getParent();
        //ПЕРЕСТРОИТЬ ПУТЬ НА СЕРВЕРЕ ПОД LINUX
        static String dbpath = new File(".").getAbsolutePath().replace(".", "")+"plugins\\WorldGuard\\cache\\profiles.sqlite";
	
	
	public static void Conn() throws ClassNotFoundException, SQLException 
	   {
		   conn = null;
		   Class.forName("org.sqlite.JDBC");
		   conn = DriverManager.getConnection("jdbc:sqlite:"+dbpath);
		   
		   System.out.println("[RGPROTECT] WG database connected!");
	   }
	
	
	public static void CreateDB() throws ClassNotFoundException, SQLException
	   {
		statmt = conn.createStatement();
		statmt.execute("CREATE TABLE if not exists 'damagers' "
                        + "('nickname' CHAR(32) PRIMARY KEY NOT NULL,"
                        + "'date' text,"
                        + "'damaged_uuid' CHAR(36),"
                        + " FOREIGN KEY (damaged_uuid) REFERENCES uuid_cache(uuid))");
		
		System.out.println("[RGPROTECT] WG new table created!");
	   }
	
	
	public static void WriteDB(String nickname, String uuid) throws SQLException
	{
		   statmt.execute("INSERT INTO 'damagers' ('nickname','date', 'damaged_uuid') "
                           + "VALUES ('" +nickname+ "'," +"datetime('now')," + "'" + uuid + "'); ");
		  
		   System.out.println("[RGPROTECT] New record added!");
	}
	
	
	public static ArrayList ReadDB(String uuid) throws ClassNotFoundException, SQLException
	   {
               ArrayList<String> damagers = new ArrayList();
		resSet = statmt.executeQuery("Select nickname "
                        +"from uuid_cache "
                        +"inner join damagers on damagers.damaged_uuid = uuid_cache.uuid "
                        +"where uuid_cache.uuid = '"+uuid+"';");
		
		while(resSet.next())
		{
			String nickname = resSet.getString("nickname");
                        //System.out.print("------------"+nickname);
                        damagers.add(nickname);
	         //System.out.println( "[RGPROTECT] nickname = " + nickname );

	         //System.out.println();
		}	
		
		//System.out.println("[RGPROTECT] TEST is ENDED");
                return damagers;
	    }
	public static ArrayList ReadDBT(String uuid) throws ClassNotFoundException, SQLException
	   {
               ArrayList<String> damagerst = new ArrayList<>();
		resSet = statmt.executeQuery("Select date "
                        +"from damagers "
                        +"inner join uuid_cache on uuid_cache.uuid = damagers.damaged_uuid "
                        +"where uuid_cache.uuid = '"+uuid+"';");
		
		while(resSet.next())
		{
			String date = resSet.getString("date");
                        //System.out.print("------------"+date);
                        damagerst.add(date);
		}	
		
                return damagerst;
	    }
		
            public static void CloseDB() throws ClassNotFoundException, SQLException
               {
                    conn.close();
                    statmt.close();
                    resSet.close();

                    System.out.println("[RGPROTECT] WG database disconnected!");
               }
        public static boolean ClearOldRecords(String days) throws SQLException
        {
            resSet = statmt.executeQuery("delete "
                    + "from damagers "
                    + "where date(damagers.date) < date('now','-"+days+" day')");
            
            return true;
        }
}