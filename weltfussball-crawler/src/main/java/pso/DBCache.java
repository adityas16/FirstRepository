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

import org.apache.http.HttpException;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.aditya.research.pso.parsers.io.DocumentWIthIdentifier;



public class DBCache {
	String table;
	String baseURL;
	PreparedStatement read,write;
	Statement readAll;
	String EXTENSION = ""; 

	private DBCache(String type,String schema, String baseURL) {
		super();
		this.table = type;
		this.baseURL = baseURL;

		// create our mysql database connection
		try {
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/" + schema, "root", "root");
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

	public Document get(String uri) throws IOException{
		try{
			read.setString(1, uri);
			ResultSet rs = read.executeQuery();
			if(rs.next()){
				return readDocument(rs).getDoc();
			}
			Document doc = null;
			try{
			doc = Jsoup.connect(baseURL + uri + EXTENSION)
						.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
		               .referrer("http://www.google.com") 
		               .timeout(10000)               
		               .get();
			}
			catch(HttpStatusException h){
				h.printStackTrace();
				throw new IOException("resource not found");
			}
			write.setString(1, uri);
			write.setBytes(2, doc.outerHtml().getBytes());

			try{
			write.execute();
			}
			catch (SQLIntegrityConstraintViolationException e) {
				e.printStackTrace();
			}
			return doc;
		}
		catch(SQLException s){
			s.printStackTrace();
			throw new IOException("DB exception");
		}
	}

	private DocumentWIthIdentifier readDocument(ResultSet rs) throws SQLException {
		Document doc = Jsoup.parse(new String(rs.getBlob("raw_html").getBytes(1L, (int)rs.getBlob("raw_html").length()))); 
		return new DocumentWIthIdentifier(doc, rs.getString("uri"));
	}

	public class DocumentIterator implements Iterator<DocumentWIthIdentifier>{
		ResultSet rs;
		DocumentWIthIdentifier next;
		boolean hasNext;
		public DocumentIterator(ResultSet rs) {
			this.rs = rs;
			readNext();
		}
		public boolean hasNext() {
			return hasNext;
		}
		public DocumentWIthIdentifier next() {
			DocumentWIthIdentifier current = next;
			readNext();
			return current;
		}
		private void readNext(){
			try{
				hasNext = rs.next();
				if(hasNext){
					next = readDocument(rs);
				}
			}
			catch(Exception e){
				e.printStackTrace();
				throw new RuntimeException("DB error");
			}
		}
	}

	public Iterator<DocumentWIthIdentifier> allDocuments() throws IOException{
		try {
			return new DocumentIterator(readAll.executeQuery("Select * from " + table));
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException("DB exception");
		}
	}
	
	public static DBCache weltPlayerCache(){
		return new DBCache("player","weltfussball", Constants.worldfootballURL + Constants.player_summary + "/");
	}
	public static DBCache weltpsoCache(){
		return new DBCache("pso", "weltfussball",Constants.worldfootballURL + Constants.report + "/");
	}
	public static DBCache weltgameCache(){
		return new DBCache("game", "weltfussball",Constants.worldfootballURL + Constants.report + "/");
	}
	public static DBCache weltseasonCache(){
		return new DBCache("season", "weltfussball",Constants.worldfootballURL + Constants.all_matches + "/");
	}
	public static DBCache transfermrktPlayerPenaltyStatsCache(){
		return new DBCache("player_penalty_stats", "transfermrkt",Constants.transfermrktURL);
	}
	public static DBCache transfermrktKeeperPenaltyStatsCache(){
		return new DBCache("keeper_penalty_stats", "transfermrkt",Constants.transfermrktURL);
	}
	
	
	public static DBCache ASATpsoCache(){
		return new DBCache("pso", "ASAT",Constants.ASATURL + "/data/spiele/");
	}
	public static DBCache ASATseasonCache(){
		return new DBCache("season", "ASAT",Constants.ASATURL + "/data/nat/");
	}
	
	public static DBCache ChampionatpsoCache(){
		return new DBCache("pso", "championat",Constants.championatURL);
	}
	public static DBCache ChampionatSeasonCache(){
		return new DBCache("season", "championat",Constants.championatURL);
	}
	public static DBCache hockeyrefGameCache(){
		DBCache dbCache = new DBCache("game", "hockeyref",Constants.hockeyrefURL + "boxscores/");
		dbCache.EXTENSION = ".html";
		return dbCache;
	}

}
