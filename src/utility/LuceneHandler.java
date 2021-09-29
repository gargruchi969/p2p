package utility;

import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import p2pApp.SearchResults;
import p2pApp.StreamServer;
import p2pApp.p2pIndexer.TableHandler;

public class LuceneHandler {

	private static IndexWriter writer= null;
	private static IndexReader reader= null;
	private static IndexSearcher searcher= null;
	
	public static IndexWriter createNewIndex(String path) throws Exception{
		if(writer==null || writer.getConfig().getOpenMode()==OpenMode.CREATE_OR_APPEND){
			closeWriter();

			Directory dir = FSDirectory.open(Paths.get(path));
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setOpenMode(OpenMode.CREATE);
			writer = new IndexWriter(dir, iwc);
		}
		return writer;
	}

	public static IndexWriter createIndex(String path) throws Exception{
		if(writer==null){
			Directory dir = FSDirectory.open(Paths.get(path));
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			writer = new IndexWriter(dir, iwc);
		}
		return writer;
	}

	public static void closeWriter(){
		try{
			if(writer!=null && writer.isOpen()){
				writer.close();
				writer= null;
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static IndexReader getReader(String path) throws Exception{
		if(reader==null)
			reader= DirectoryReader.open(FSDirectory.open(Paths.get(path)));
		return reader;
	}
	
	public static IndexSearcher getSearcher(String path) throws Exception{
		if(searcher==null)
			searcher= new IndexSearcher(getReader(path));
		return searcher;
	}
	
	public static String getFieldValue(String path, String sfield, String rfield, String id){
		try{
			searcher = getSearcher(path);
			TopDocs hits= searcher.search(IntPoint.newExactQuery(sfield, Integer.parseInt(id)), 5);
			if(hits.totalHits>0){
				return searcher.doc(hits.scoreDocs[0].doc).get(rfield);
			}
		}
		catch(Exception e){

		}
		return null;
	}

	public static ArrayList<SearchResults> getDirValues(String path, String idfield, String pfield, String id, String name){
		try {
			
			String filepath= getFieldValue(path, idfield, pfield, id);
			IndexSearcher searcher= new IndexSearcher(getReader(path));
			WildcardQuery pq= new WildcardQuery(new Term("pathstring", filepath+"/*"));
			ScoreDoc[] hits = searcher.search(pq, 100000).scoreDocs; // run the query
			ArrayList<SearchResults> al= new ArrayList<SearchResults>();
			for (int i = 0; i < hits.length; i++) {
				Document doc = searcher.doc(hits[i].doc);//get the next  document
				al.add(new SearchResults("", "", doc.get(TableHandler.columns[0]),
						doc.get(TableHandler.columns[2]).replaceFirst("(.*)"+name+"/", name+"/"), doc.get(TableHandler.columns[3]),
						doc.get(TableHandler.columns[4]), doc.get(TableHandler.columns[5]), StreamServer.getFileType(TableHandler.columns[1])));
			}
			return al;
		}
		catch(Exception e){

		}
		return null;
	}
}
