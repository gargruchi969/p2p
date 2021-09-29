package tcpUtilities;

import java.util.ArrayList;
import java.util.List;

public class CallbackRegister {

	private List<callbackFields> callbacks;
	private static CallbackRegister callbackRegis;
	private CallbackRegister(){
		callbacks= new ArrayList<callbackFields>();
	}
	
	public static CallbackRegister getInstance(){
		if(callbackRegis==null)
			callbackRegis= new CallbackRegister();
		return callbackRegis;
	}
	
	public void registerForCallback(String action, String className, String methodName, boolean singleRun, Object baseObj){
		addNew(new callbackFields(action, className, methodName, singleRun, baseObj));
	}
	
	public void notifyCallbacks(String action, Object obj){
		for(int i=0; i<callbacks.size();i++){
			if(callbacks.get(i).action.equals(action)){
				new CallbackRunner(action, callbacks.get(i).className, callbacks.get(i).methodName, obj, callbacks.get(i).baseObj);
				if(callbacks.get(i).singleRun){
					callbacks.remove(i--);
				}
			}
		}
	}
	
	public void unregisterForCallback(String action, String className, String methodName){
		for(int i=0; i<callbacks.size();i++){
			if(callbacks.get(i).action.equals("action") && callbacks.get(i).className.equals(className) && callbacks.get(i).methodName.equals(methodName)){
					callbacks.remove(i--);
			}
		}
	}
	
	private void addNew(callbackFields callback){
		for(int i=0;i<callbacks.size();i++){
			callbackFields c= callbacks.get(i);
			if(c.action.equals(callback.action)
					&& c.className.equals(callback.className)
					&& c.methodName.equals(callback.methodName)){
				return;
			}
		}
		callbacks.add(callback);
	}
	
	public void echoCallbacks(){
		for(int i=0;i<callbacks.size();i++){
			callbackFields cf= callbacks.get(i);
			System.out.println("a:"+cf.action+"\t"+"c:"+cf.className+"\t"+"f:"+cf.methodName+"\t"+"s:"+cf.singleRun);
		}
	}
}

class callbackFields{
	
	public String action;
	public String className;
	public String methodName;
	boolean singleRun;
	Object baseObj;
	callbackFields(String a, String c, String m, boolean s, Object o){
		action= a;
		className= c;
		methodName=m;
		singleRun= s;
		baseObj= o;
	}
}
