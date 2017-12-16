package pso;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

import com.aditya.research.pso.parsers.io.DocumentWIthIdentifier;

import pso.DBCache.DocumentIterator;

public class DBCacheDao {
	String table;
	String baseURL;
	PreparedStatement read,write;
	Statement readAll;
	static String EXTENSION = ".html"; 

	public DBCacheDao(String type,String schema) {
		super();
		this.table = type;

		// create our mysql database connection
		try {
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/" + schema , "root", "root");
			//			conn.setAutoCommit(false);
			read = conn.prepareStatement("Select * from " + table + " where uri= ?");
			write = conn.prepareStatement("insert into " + table + " (uri,raw_html) VALUES (?,?)");
			readAll = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			readAll.setFetchSize(Integer.MIN_VALUE);


		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
	}

	public String get(String uri) throws IOException{
		try{
			read.setString(1, uri);
			ResultSet rs = read.executeQuery();
			if(rs.next()){
				return new String(rs.getBlob("raw_html").getBytes(1L, (int)rs.getBlob("raw_html").length()));
			}
			throw new NoSuchElementException();
		}
		catch(SQLException s){
			s.printStackTrace();
			throw new IOException("DB exception");
		}
	}

	public void put(String uri,String data) throws IOException{
		try{
			write.setString(1, uri);
			write.setBytes(2, data.getBytes());

			write.execute();
		}
		catch (SQLIntegrityConstraintViolationException e) {
			e.printStackTrace();
		}
		catch(SQLException s){
			s.printStackTrace();
			throw new IOException("DB exception");
		}
	}

}
