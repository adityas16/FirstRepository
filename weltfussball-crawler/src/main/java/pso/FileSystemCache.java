package pso;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class FileSystemCache {
	String folder;
	String baseURL;
	
	static String EXTENSION = ".html"; 
	
	public FileSystemCache(String type, String baseURL) {
		super();
		this.folder = type;
		this.baseURL = baseURL;
	}



	public Document get(String uri) throws IOException{
		throw new UnsupportedOperationException();
//		File f = new File(getFileName(uri));
//		if(f.exists()){
//			return Jsoup.parse(f, "UTF-8", "http://example.com/");
//		}
//		final Response response = Jsoup.connect(baseURL + uri).execute();
//        final Document doc = response.parse();
//
//        f = new File(getFileName(uri));
//        FileUtils.writeStringToFile(f, doc.outerHtml(), "UTF-8");
//        return doc;
	}



	private String getFileName(String uri) {
		return folder+uri  + EXTENSION;
	}
}
