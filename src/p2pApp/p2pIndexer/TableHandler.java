package p2pApp.p2pIndexer;

import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

import utility.LuceneHandler;
import utility.MySqlHandler;

public class TableHandler {

	public static long FileID =10000;
	public static String TblName = "DirReader";
	public final static String[] columns = new String[]{"FileID","FileName","Path","Hash","FileSize", "Type", "Valid"};
	private static String[] data = new String[]{"FileID int not null","FileName varchar(255) not null","Path varchar(255) CHARACTER SET utf8 COLLATE utf8_spanish_ci not null","Hash char(40) null","FileSize char(20)", "Type char(1) not null default '1'", "Valid char(1) not null default '1'", "primary key(FileID)", "FULLTEXT("+utility.Utilities.searchCol+")", "Unique key (Path)", "key(valid)", "key(hash)"};
	public static final String INDEX_DIRECTORY = "data/.searchIndex";
	public static String tableType= "lucene";

	public static void createTable(boolean force){
		MySqlHandler.getInstance().createTable(TblName, data, null, force);
	}


	public static void fillTable(List<String[]> files){
		MySqlHandler.getInstance().insertMultiple(TblName, columns, files);
	}

	public static String getFilePath(String FileID)
	{
		if(tableType.equals("mysql")){
			String query = "SELECT Path from "+ TblName + " WHERE FileID = "+ FileID;
			return (String)(MySqlHandler.getInstance().fetchQuery(query)).get(0).get("Path");
		}
		else{
			return LuceneHandler.getFieldValue(INDEX_DIRECTORY, columns[0], "pathstring", FileID);
		}
	}

	public static List<Map<String, Object>> getFilesFromTable(){
		return MySqlHandler.getInstance().fetchQuery("SELECT * from "+TblName);
	}

	public static int getNextId(){
		if(tableType.equals("mysql")){
			List<Map<String, Object>> l= MySqlHandler.getInstance().fetchQuery("SELECT max(FileId) as max from "+TblName);
			return (Integer.parseInt(l.get(0).get("max").toString())+1);
		}
		else
			return (int)TableHandler.FileID;
	}

	public static void fillTable(String[] values){
		MySqlHandler.getInstance().insertSingle(TblName, columns, values);
	}

	public static List<Map<String, Object>> getFilesFromDir(String fileId){
		String query = "SELECT Path from "+ TblName + " WHERE FileID = "+ fileId;
		String path = (String)(MySqlHandler.getInstance().fetchQuery(query)).get(0).get("Path");
		return MySqlHandler.getInstance().fetchQuery(
				"SELECT * from "+TblName + " WHERE Path like '"+path+"/%'");
	}

	//Lucene Functions
	public static void createIndex(List<String[]> files, boolean newIndex) throws Exception{
		if(files.size()==0)
			return;
		int fileId = 0;
		IndexWriter writer = null;
		if(newIndex)
			writer= LuceneHandler.createNewIndex(INDEX_DIRECTORY);
		else
			writer= LuceneHandler.createIndex(INDEX_DIRECTORY);

		for(int i=0;i<files.size();i++){
			try {
				Document doc= new Document();
				String arr[]= files.get(i);

				if(newIndex)
					fileId= Integer.parseInt(arr[0]);
				else
					fileId= (int)FileID;

				Field path= new TextField(columns[2], arr[2], Field.Store.YES);
				doc.add(path);
				Field pathString= new StringField("pathstring", arr[2], Field.Store.YES);
				doc.add(pathString);
				Field filename = new TextField(columns[1], arr[1], Field.Store.YES);
				doc.add(filename);
				if(arr[3]==null)
					arr[3]="null";

				Field hash= new StringField(columns[3], arr[3], Field.Store.YES);
				doc.add(hash);
				Field size= new StringField(columns[4], arr[4], Field.Store.YES);
				doc.add(size);
				Field type= new StringField(columns[5], arr[5], Field.Store.YES);
				doc.add(type);
				Field valid= new StringField(columns[6], arr[6], Field.Store.YES);
				doc.add(valid);
				Field fileid= new IntPoint(columns[0], fileId);
				doc.add(fileid);
				doc.add(new StoredField(columns[0], fileId));
				FileID++;
				writer.addDocument(doc);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		LuceneHandler.closeWriter();
		//writer.close();
	}
}
