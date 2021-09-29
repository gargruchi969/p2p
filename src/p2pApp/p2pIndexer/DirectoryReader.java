package p2pApp.p2pIndexer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;

import p2pApp.p2pDownloader.DownloadEngine;
import tcpUtilities.CallbackRegister;
import utility.LuceneHandler;

public class DirectoryReader {

	private static String[] values = null;
	private static List<String[]> files= null;
	private static List<String[]> remFiles= null;
	private static HashMap<String, Object> fileList= null;
	private static DownloadEngine de= DownloadEngine.getInstance();
	private static DirectoryReader directoryReader;

	public static DirectoryReader getInstance(){
		if(directoryReader==null)
			directoryReader= new DirectoryReader();
		return directoryReader;
	}

	private static String parseInvalidNames(String n){
		if(TableHandler.tableType.equals("mysql")){
			return n.replace("'", "''").replace("\\", "/").replace("_", " ");
		}
		return n.replace("\\","/").replace( "_"," ");
	}
	
	private static String parseInvalidPaths(String n){
		if(TableHandler.tableType.equals("mysql")){
			return n.replace("'", "''").replace("\\", "/");
		}
		return n.replace("\\","/");
	}
	static long getFiles (File aFile, int depth) {	
		long sum=0;
		if(aFile.isFile()){
			if(!de.isPresentInPaused(aFile)){
				values = new String[]{String.valueOf(TableHandler.FileID++),parseInvalidNames(aFile.getName()), parseInvalidPaths(aFile.getPath()), "null", String.valueOf(aFile.length()), "1", (depth > -1)?"1":"2"};
				files.add(values);
			}
			return aFile.length();
		}
		else if (aFile.isDirectory()) {
			File[] listOfFiles = aFile.listFiles();
			if(listOfFiles!=null) {
				for (int i = 0; i < listOfFiles.length; i++)
					sum = sum+ getFiles(listOfFiles[i], depth-1);

				values = new String[]{String.valueOf(TableHandler.FileID++),parseInvalidNames(aFile.getName()), parseInvalidPaths(aFile.getPath()), "null", ""+sum, "2", (depth > -1)?"1":"2"};
				files.add(values);
			}
			return sum;
		} 
		else {
			System.out.println("Directory Read #3 - Access Denied");
		}

		return 0;
	}

	public static void DR_init(ArrayList<String> names){
		DR_init(names, true);
	}

	public static void DR_init(ArrayList<String> names, boolean force)
	{  
		try{
			File aFile;
			files= new ArrayList<String[]>();
			for(int i=0;i<names.size();i++)
			{
				try{

					int index= names.get(i).indexOf("::");
					int val= 128;
					if(index==-1)
						index= names.get(i).length();
					else
						val= Integer.parseInt(names.get(i).substring(index+2).trim());
					aFile= new File(names.get(i).substring(0, index));  
					getFiles(aFile, val);
				}
				catch(Exception e){

				}
			}

			getFilesOnStart();

			TableHandler.createTable(force);
			getFilesHashValues();
			TableHandler.fillTable(files);
			fillInMissingHashes();
			files.clear();

			//			preserveOldHashedFiles();
			TableHandler.fillTable(files);
		}
		catch(Exception e){
			System.out.println("Directory Reader #5 "+e.getMessage());
		}
		finally{
			fileList.clear();
			files.clear();
		}
	}

	private static void getFilesOnStart(){
		List<Map<String, Object>> l;
		try{
			l= TableHandler.getFilesFromTable();
			fileList= new HashMap<String, Object>();
			for(int i=0;i<l.size();i++){
				fileList.put(l.get(i).get("Path").toString(), l.get(i));	
			}
		}
		catch(Exception e){
			System.out.println("Directory Read #2 "+e.getMessage());  
		}
	}

	@SuppressWarnings("unchecked")
	private static void getFilesHashValues(){

		try{
			for(int i=0;i<files.size();i++){
				String []arr= files.get(i);
				String parsedPath= arr[2].replace("''", "'");

				if(fileList.containsKey(parsedPath)){
					HashMap<String, Object> o= (HashMap<String, Object>)fileList.get(parsedPath);
					String h= o.get("Hash").toString();
					String fs= o.get("FileSize").toString();

					if(h!=null && h.length()>20 && arr[4].equals(fs) && arr[5].equals("1")){
						files.get(i)[3]= h;
					}
					fileList.remove(parsedPath);
				}
			}
		}
		catch(Exception e){
			System.out.println("Directory Read #1 "+e.getMessage());
		}
	}

	private static void fillInMissingHashes(){

		for(int i=0;i<files.size();i++){
			if(files.get(i)[3].length()<20 && files.get(i)[5].equals("1")){
				HashCalculator.getInstance().addNewPath(files.get(i)[2].replace("''", "'"));
			}
		}
	}

	//	@SuppressWarnings("unchecked")
	//	private static void preserveOldHashedFiles(){
	//		try{
	//
	//			if(!fileList.isEmpty()){
	//				Set<String> paths= fileList.keySet();
	//				Iterator<String> itr= paths.iterator();
	//				while(itr.hasNext()){
	//					String s= itr.next();
	//					HashMap<String, Object> o= (HashMap<String, Object>)fileList.get(s);
	//					values = new String[]{String.valueOf(TableHandler.FileID++),o.get("FileName").toString().replace("'", "''"), o.get("Path").toString().replace("'", "''"), o.get("Hash").toString(), o.get("FileSize").toString(), o.get("Type").toString(), "0"};
	//					files.add(values);
	//				}
	//			}
	//
	//		}
	//		catch(Exception e){
	//			System.out.println("Directory Reader #4 "+e.getMessage());
	//		}
	//	}

	public static void updateTableOnDownload(String filepath){
		File f= new File(filepath);
		if(f.isFile()){
			values = new String[]{String.valueOf(TableHandler.getNextId()), f.getName().replace("'", "''").replace("_", " "), f.getPath().replace("\\", "/").replace("'", "''"), "null", String.valueOf(f.length()), "1", "2"};
			if(TableHandler.tableType.equals("mysql")){
				TableHandler.fillTable(values);
				HashCalculator.getInstance().addNewPath(filepath);
			}
			else{
				List<String[]> l= new ArrayList<String[]>();
				l.add(values);
				HashCalculator.getInstance().addMultiplePaths(l, 2);
			}
		}
	}

	public static void updateTableOnDownload(String filepath, String hash){
		
		if(hash== null || hash.length()<10)
			updateTableOnDownload(filepath);
		
		File f= new File(filepath);
		if(f.isFile()){
			values = new String[]{String.valueOf(TableHandler.getNextId()), f.getName().replace("'", "''").replace("_", " "), f.getPath().replace("\\", "/").replace("'", "''"), hash, String.valueOf(f.length()), "1", "2"};
			if(TableHandler.tableType.equals("mysql")){
				TableHandler.fillTable(values);
			}
			else{
				List<String[]> l= new ArrayList<String[]>();
				l.add(values);
				try {
					TableHandler.createIndex(l, false);
				} catch (Exception e) {
				}
			}
		}
	}

	public void indexDirectories(ArrayList<String> names){
		try{
			File aFile;
			files= new ArrayList<String[]>();
			for(int i=0;i<names.size();i++)
			{
				try{
					int index= names.get(i).indexOf("::");
					int val= 128;
					if(index==-1)
						index= names.get(i).length();
					else
						val= Integer.parseInt(names.get(i).substring(index+2).trim());
					aFile= new File(names.get(i).substring(0, index));  
					getFiles(aFile, val);
				}
				catch(Exception e){

				}
			}
			getAllIndexes(files);
		}
		catch(Exception e){
			System.out.println("Directory Reader #8 "+e.getMessage());
		}
	}

	public void getAllIndexes(List<String[]> files) throws Exception{
		HashMap<String, Integer> paths= new HashMap<String, Integer>();
		IndexSearcher searcher = null;
		ScoreDoc[] hits = null;
		try {
			searcher= new IndexSearcher(LuceneHandler.getReader(TableHandler.INDEX_DIRECTORY));
			Query query= new MatchAllDocsQuery();
			hits = searcher.search(query, 10000000).scoreDocs;

			for(int i=0;i<hits.length;i++){
				Document doc= searcher.doc(hits[i].doc);
				paths.put(doc.get("pathstring")+"::"+doc.get(TableHandler.columns[4]), i);
				if(Integer.parseInt(doc.get("FileID")) > TableHandler.FileID){
					TableHandler.FileID += 10;
				}
			}
		} 
		catch(IndexNotFoundException f){

		}catch (Exception e) {
			e.printStackTrace();
		}
		matchPaths(files, hits, paths, searcher);
	}

	public void matchPaths(List<String[]> files,
			ScoreDoc[] hits, HashMap<String, Integer> paths, IndexSearcher searcher) throws Exception{

		int rem[], found[];
		rem = new int[files.size()];
		found= new int[files.size()];
		int r=0, f=0;

		for(int i=0;i<files.size();i++){
			String []fl= files.get(i);
			if(paths.containsKey(fl[2]+"::"+fl[4])){
				found[f++]= paths.get(fl[2]+"::"+fl[4]);
			}
			else
				rem[r++]= i;
		}

		if(f>0){
			//			IndexWriter writer= LuceneHandler.createNewIndex(TableHandler.INDEX_DIRECTORY);
			List<String[]> list= new ArrayList<String[]>();
			for(int i=0;i<f;i++){
				Document doc= searcher.doc(hits[found[i]].doc);
				//				doc.add(new IntPoint(TableHandler.columns[0], Integer.parseInt(doc.get(TableHandler.columns[0]))));
				//				writer.addDocument(doc);
				list.add(new String[]{doc.get(TableHandler.columns[0]),
						doc.get(TableHandler.columns[1]),
						doc.get("pathstring"),
						doc.get(TableHandler.columns[3]),
						doc.get(TableHandler.columns[4]),
						doc.get(TableHandler.columns[5]),
						doc.get(TableHandler.columns[6])});
			}
			TableHandler.createIndex(list, true);
			list= null;
			//writer.close();
		}
		remFiles= new ArrayList<String[]>();
		for(int i=0;i<r;i++){
			remFiles.add(files.get(rem[i]));
		}

		paths.clear();
		paths= null;
		files.clear();
		files = null;

		CallbackRegister.getInstance().registerForCallback("p2p-app-hashing-done", "p2pApp.p2pIndexer.DirectoryReader", "addNewHashes", false, DirectoryReader.this);
		if(r>0)
			HashCalculator.getInstance().addMultiplePaths(remFiles, 2);

	}

	//callback functions 

	@SuppressWarnings("unchecked")
	public void addNewHashes(String action, Object obj){
		if(action.equals("p2p-app-hashing-done")){
			try {
				remFiles= (List<String[]>)obj;
				TableHandler.createIndex(remFiles, false);
				remFiles.clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
