package tools.apis;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import mujava.loader.Reloader;

public class ReloaderAPI {

	private static ReloaderAPI instance = null;
	private String workingDir;
	private List<String> paths;
	private Set<String> allowedPackagesToReload;
	private Reloader reloader;
	
	public static void startInstance(String workingDir) {
		startInstance(workingDir, null, null);
	}
	
	public static void startInstance(String workingDir, List<String> extraPaths) {
		startInstance(workingDir, extraPaths, null);
	}
	
	public static void startInstance(String workingDir, List<String> extraPaths, Set<String> allowedPackagesToReload) {
		ReloaderAPI.instance = new ReloaderAPI(workingDir, extraPaths, allowedPackagesToReload);
	}
	
	public void rescan() {
		this.reloader.markEveryClassInFolderAsReloadable(this.workingDir, this.allowedPackagesToReload);
	}
	
	public void rescan(String dir) {
		if (!this.paths.contains(dir)) {
			throw new IllegalArgumentException("tools.ReloaderAPI#rescan(String) : can't rescan a dir(" + dir +  ") that was not previously set as a dir in Reloader classpath");
		}
		this.reloader.markEveryClassInFolderAsReloadable(dir, this.allowedPackagesToReload);
	}
	
	public static ReloaderAPI getInstance() {
		if (ReloaderAPI.instance != null) {
			return ReloaderAPI.instance;
		} else {
			throw new IllegalStateException("tools.ReloaderAPI#getInstance() : requesting an instance without a previous call to a startInstance method");
		}
	}
	
	private ReloaderAPI (String workingDir) {
		this(workingDir, null, null);
	}
	
	private ReloaderAPI (String workingDir, List<String> extraPaths) {
		this(workingDir, extraPaths, null);
	}
	
	private ReloaderAPI (String workingDir, List<String> extraPaths, Set<String> allowedPackagesToReload) {
		this.workingDir = workingDir;
		this.paths = new LinkedList<String>();
		this.paths.add(this.workingDir);
		if (extraPaths != null) this.paths.addAll(extraPaths);
		this.allowedPackagesToReload = allowedPackagesToReload;
		this.reloader = new Reloader(this.paths, Thread.currentThread().getContextClassLoader());
		this.reloader.markEveryClassInFolderAsReloadable(this.workingDir, this.allowedPackagesToReload);
	}
	
	public void setSpecificClassPath(String className, String path) {
		this.reloader.setSpecificClassPath(className, path);
	}
	
	public Class<?> reload(String className) throws ClassNotFoundException {
		Class<?> ret = this.reloader.rloadClass(className, true); 
		this.reloader = this.reloader.getLastChild();
		return ret;
	}
	
	public Class<?> reloadFrom(String className, String path) throws ClassNotFoundException {
		this.reloader.setSpecificClassPath(className, path);
		return reload(className);
	}
	
	public Class<?> load(String className) throws ClassNotFoundException {
		return this.reloader.loadClass(className);
	}
	
	public void setReloaderAsThreadClassLoader(Thread t) {
		t.setContextClassLoader(this.reloader);
	}
	
}
