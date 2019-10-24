package Utils;

import java.util.regex.Pattern;

public class Validators {
	
	// method to validate whether a username is valid
	public static boolean checkName(String username) {	
		Pattern p = Pattern.compile("^[a-zA-Z0-9._-]{3,}$"); 
		
		return p.matcher(username).matches();
	}
	
	// method to validate whether a pool/room number is valid	
	public static boolean checkSize(String size) {	
		Pattern p = Pattern.compile("^(0?[1-9]|[1-9][0-9])$"); 
		
		return p.matcher(size).matches();
	}
	
	// method to validate whether a port number is valid
	public static boolean checkPort(String port) {	
		Pattern p = Pattern.compile("^[0-9]{1,5}$"); 
		
		return p.matcher(port).matches();
	}
	
	// method to validate whether a host is valid
	public static boolean checkHost(String host) {	
		Pattern p = Pattern.compile("^"
                + "(((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}" // Domain name
                + "|"
                + "localhost" // localhost
                + "|"
                + "(([0-9]{1,3}\\.){3})[0-9]{1,3})$"); // Ip
		
		return p.matcher(host).matches();
	}
}
