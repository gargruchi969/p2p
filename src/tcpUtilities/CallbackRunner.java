package tcpUtilities;

import java.lang.reflect.Method;

public class CallbackRunner extends Thread {

	String className;
	String methodName;
	Object queryObject;
	Object baseObject;
	String action;
	
	CallbackRunner(String action, String className, String methodName, Object qobj, Object bobj){
		this.action= action;
		this.className= className;
		this.methodName= methodName;
		this.queryObject= qobj;
		this.baseObject= bobj;
		
		this.start();
	}
	
	public void run(){
		try{
			Method method= Class.forName(className).getMethod(methodName, String.class, Object.class);
			method.invoke(baseObject, action, queryObject);
		}
		catch(Exception e){
			System.out.println("Callback: "+e.getMessage());
		}
	}
}
