package edu.fsuj.csb.tools.newtork.pagefetcher;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * 
 * Contains methods to simply read remote (an local) files
 * @author Stephan Richter
 * 
 */
public class PageFetcher {

	private final static int divisor = 1024 * 1024; // if file is downloaded, after every <divisor> bytes, a progress message will be displayed
	//private static TreeSet<URL> fetchedPages;
	private static String cacheDir="cache";
	
	public static String cachedFile(URL url){
		return cacheDir+"/"+url.toString().replace(":", "/").replace("//", "/").replace("?", ""); // strip slashes from filename
	}

	/**
	 * tries to open the page given by URL, download it and stream it into a StringBuffer
	 * 
	 * @param url the page url
	 * @return the page source code
	 * @throws IOException
	 */
	public static StringBuffer fetch(URL url) throws IOException {
		boolean isLocal = url.toString().startsWith("file:");
		boolean rewrite = !isLocal;
		String cachedFile = cachedFile(url);
		StringBuffer result = new StringBuffer(); // create result string buffer
		BufferedReader br=null;
		if (!isLocal && (new File(cachedFile)).exists()) { // if data has been downloaded before: read existing file
			//Tools.indent("Reading " + url + "...");
			br = new BufferedReader(new FileReader(cachedFile));
			rewrite = false;
		} else {
			if (!isLocal) System.out.print("Downloading " + url + "...");
			int retry=23;
			int sleep=1;
			while (retry>0){
				try {				
					br = new BufferedReader(new InputStreamReader(url.openStream())); // if data was not downloaded before: start download
					break;
				} catch (IOException ce){
					retry--;					
					if (retry==0) throw ce;
					try {
						System.err.println("Could not read from "+url+". Will retry "+retry+" more times. Next trial in "+(sleep/1000.0)+" seconds.");
	          Thread.sleep(sleep);
	          sleep*=2;
          } catch (InterruptedException e) {}
				}
			}
		}
		int s = 0;
		s=0;
		String line;
		while (null!=(line=br.readLine())) { // read line by line
			result.append(line+"\n");
			if (!isLocal && rewrite) {
				int l = result.length() / divisor;
				if (l > s) {
					if (s > 0) System.out.print(spaces(((l - 1) + "MB").length()));
					System.out.println((result.length() / divisor) + "MB");
					s = l;
				}
			}
		}
		br.close();
		if (rewrite) { // if content is newly downloaded: write to file, so it may be reused next time
			createDirectory(cachedFile.substring(0,cachedFile.lastIndexOf("/")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(cachedFile));
			bw.write(result.toString());
			bw.close();
			int l = result.length() / divisor;
			if (!isLocal) {
				if (s > 0) {
					System.out.print(spaces(((l - 1) + "MB").length()));
				}
				System.out.println("done.");
			}
		}
		return result;
	}

	/**
	 * tries to convert the string into an url and then open and read the corrosponding file
	 * @param urlString
	 * @return a string buffer containing the file's content
	 * @throws IOException if file can't be read
	 */
	public static StringBuffer fetch(String urlString) throws IOException {
		return fetch(new URL(urlString));
	}

	/**
	 * tries to fetch the file specified by filename, and read it into an array
	 * 
	 * @param filename the file to be read. may be a local filename or url of a remote file
	 * @return the contents of the file, read into an array (every array element contains one line of the file)
	 * @throws IOException
	 */
	public static String[] fetchLines(String filename) throws IOException {
		if (!filename.contains("/")) filename=(new File(".")).getCanonicalPath()+"/"+filename;
		if (!filename.contains(":")) filename="file://"+filename;
		return fetchLines(new URL(filename));
	}

	/**
	 * tries to fetch the file specified by url, and read it into an array
	 * @param u the url of the file
	 * @return the contents of the file, read into an array (everey array element containing one line of the file)
	 * @throws IOException
	 */
	public static String[] fetchLines(URL u) throws IOException {
		return fetch(u).toString().split("\n");
	}
	
	/**
	 * creates a directory or structure of directories according to the given path (recursively creating super-dirs)
	 * @param fname the name of the directory to be created
	 */
	public static void createDirectory(String fname) {
		File f=new File(fname);
		if (!f.exists()) f.mkdirs();	  
  }
	
	/**
	 * creates a string consistin of a number of spaces
	 * @param i the number of spaces to be inserted
	 * @return the string containing only spaces
	 */
	public static String spaces(int i) {
		StringBuffer sb=new StringBuffer();
		for (;i>0;i--) sb.append(' ');
	  return sb.toString();
  }

	/**
	 * sets the cache direcotry variable. To this directory, the cache wil be written
	 * @param dir
	 */
	public static void setCache(String dir) {
		System.out.println("Setting cache directory to "+dir);
		cacheDir=dir;
  }
}
