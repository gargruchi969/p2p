package modules;

import java.io.DataOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.jar.JarFile;

import tcpUtilities.TaskRunner;
import utility.Query_v12;

public class ModuleLoader {

	private static ModuleLoader moduleLoader;
	private ArrayList<module> moduleList;

	private ModuleLoader(){
		moduleList= new ArrayList<module>();
		loadModules();
	}

	public static ModuleLoader getInstance(){
		if(moduleLoader==null)
			moduleLoader= new ModuleLoader();
		return moduleLoader;
	}
	
	private void loadModules(){
		initModules();
		loadJarModules();
		loadFileModules();
	}
	
	private void initModules(){
		moduleList.add(new module("p2p-app", "entry.FirstClass"));
	}

	private void loadJarModules(){

		for(module m: moduleList){

			try{

				String pathToJar= utility.Utilities.modulesBasePath+m.name+".jar";
				JarFile jarFile = new JarFile(pathToJar);
				URLClassLoader cl = URLClassLoader.newInstance( new URL[]
						{ new URL("jar:file:" + pathToJar+"!/") });
				m.classInstance= cl.loadClass(m.className);
				jarFile.close();

			}
			catch(Exception e){
				System.out.println("Module Loader #3: "+e.getMessage());
			}
		}
	}

	private void loadFileModules(){

		for(module m: moduleList){

			try{
				//use here m.name as a parameter to new URL();
				URLClassLoader loader = new URLClassLoader(new URL[] {
						new URL("file://" + "E:/Compilers/eclipse/projects/p2p-app/bin/")
				});

				m.classInstance=loader.loadClass(m.className);
				loader.close();
			}
			catch(Exception e){
				System.out.println("Module loader #4: "+e.getMessage());
			}
		}
	}

	public boolean moduleLoad(String src, Query_v12 query, DataOutputStream output){
		
		for(module m: moduleList){
			if(m.name.equals(query.getModule())){
				launchModule(src, query, output, m.classInstance);
				return true;
			}
		}
		return false;
	}

	private void launchModule(String src, Query_v12 query, DataOutputStream output, Class<?> loadedClass){
		try{
			new TaskRunner(src, query, output, loadedClass);
		}
		catch(Exception e){
			System.out.println("Module-Loader #1: "+e.getMessage());
		}
	}
}

class module{
	public String name;
	public String className;
	public Class<?> classInstance;
	module(String n, String cn){
		name= n;
		className= cn;
		classInstance= null;
	}

}
