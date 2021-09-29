package p2pApp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;

import p2pApp.p2pIndexer.TableHandler;
import p2pApp.p2pQueries.SearchQuery;
import utility.LuceneHandler;
import utility.MySqlHandler;

public class SearchDatabase {

	SearchQuery searchQuery;
	int size=0;

	public SearchDatabase(SearchQuery sq){
		this.searchQuery= sq;
	}

	public SearchQuery getResults(){

		switch(searchQuery.type){

		case "keyword" :
			searchByKeyword();
			break;
		case "fileid" :
			break;
		}
		return searchQuery;
	}

	private void searchByKeyword(){

		if(TableHandler.tableType.equals("mysql")){
			List<Map<String, Object>> l;
			String key= searchQuery.data.replace("'", "''");	
			String TblName = "DirReader";
			l=MySqlHandler.getInstance().fetchQuery("SELECT FileID, FileName, Hash, FileSize, Type, match("+utility.Utilities.searchCol+") against ('"+key+"' in natural language mode) as relevance from "+ TblName+" WHERE Valid= '1' && match("+utility.Utilities.searchCol+") against ('"+key+"' in natural language mode)");

			ArrayList<SearchResults> al= new ArrayList<SearchResults>();
			for(int i=0;i<l.size();i++){
				al.add(new SearchResults("","",l.get(i).get("FileId").toString(), l.get(i).get("FileName").toString(), l.get(i).get("Hash").toString(), l.get(i).get("FileSize").toString(), l.get(i).get("Type").toString(), StreamServer.getFileType(l.get(i).get("FileName").toString()) ));
			}
			size= al.size();
			searchQuery=new SearchQuery(searchQuery.searchId, "results", "", al); 
			return;
		}

		if(TableHandler.tableType.equals("lucene")){
			try {
				IndexSearcher searcher = LuceneHandler.getSearcher(TableHandler.INDEX_DIRECTORY);
				Analyzer analyzer = new StandardAnalyzer();
				QueryParser mqp = new QueryParser(TableHandler.columns[2], analyzer);
				Query query = mqp.parse(searchQuery.data);//search the given keyword
//				Query view = 
//				PhraseQuery.Builder builder = new PhraseQuery.Builder();
//				 builder.add(new Term(TableHandler.columns[1], "mp4"), 4);
//				 builder.add(new Term(TableHandler.columns[1], "mkv"), 5);
//				 PhraseQuery pq = builder.build();
				 
//				 BooleanQuery bq = new BooleanQuery.Builder()
//						 .add(new TermQuery(new Term(TableHandler.columns[1], "mp4")), BooleanClause.Occur.SHOULD)
//						 .add(new TermQuery(new Term(TableHandler.columns[1], "mkv")), BooleanClause.Occur.SHOULD)
//						 .build();
						    
				BooleanQuery booleanQuery = new BooleanQuery.Builder()
					    .add(query, BooleanClause.Occur.MUST)
					    .add(new TermQuery(new Term(TableHandler.columns[6], "1")), BooleanClause.Occur.MUST)
					    .build();
				
				ScoreDoc[] hits = searcher.search(booleanQuery, 100).scoreDocs; // run the query
				ArrayList<SearchResults> al= new ArrayList<SearchResults>();
				for (int i = 0; i < hits.length && i<100; i++) {
					Document doc = searcher.doc(hits[i].doc);//get the next  document
					al.add(new SearchResults("", "", doc.get(TableHandler.columns[0]),
							doc.get(TableHandler.columns[1]), doc.get(TableHandler.columns[3]),
							doc.get(TableHandler.columns[4]), doc.get(TableHandler.columns[5]), 
							StreamServer.getFileType(doc.get(TableHandler.columns[1]))));
				}
				size= al.size();
				searchQuery=new SearchQuery(searchQuery.searchId, "results", "", al); 
				return;

			} catch (Exception e) {
				searchQuery= new SearchQuery(searchQuery.searchId, "results", "", null);
				e.printStackTrace();
			}
		}
	}

	public int getSize(){
		return size;
	}
}
