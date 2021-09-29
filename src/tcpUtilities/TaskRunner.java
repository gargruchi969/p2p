package tcpUtilities;

import java.io.DataOutputStream;

import utility.Query_v12;

public class TaskRunner implements Runnable{
	
	String src;
	Query_v12 query;
	DataOutputStream output;
	Class<?> loadedClass;
	
	public TaskRunner(String src, Query_v12 query, DataOutputStream output, Class<?> loadedClass){
		this.src= src;
		this.query= query;
		this.output= output;
		this.loadedClass= loadedClass;
		new Thread(this).start();
	}
	
	public void run(){
		try{
			loadedClass.getDeclaredConstructor(String.class, Query_v12.class, DataOutputStream.class).newInstance(src, query, output);
		}
		catch(Exception e){
			System.out.println("Task Runner #1:"+e.getMessage());
		}
	}
}
