package com.runtimeoverflow.SchulNetzClient;

import android.app.Application;
import android.content.res.Resources;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

public class Account {
	public String host;
	public String username;
	public String password;
	
	public boolean signedIn = false;
	
    private String id = "";
    private String currentTransId = "";
    private ArrayList<String> cookiesList = new ArrayList<>();
    private String currentCookies = "";

    private SessionManager manager;
	
	public Account(String host, String username, String password){
		this(host, username, password, true);
	}
    
    public Account(String host, String username, String password, boolean session){
        this.host = host;
        this.username = username;
        this.password = password;
        
        if(session){
			manager = new SessionManager(this);
			((Application)Variables.get().currentContext.getApplicationContext()).registerActivityLifecycleCallbacks(manager);
		}
    }
    
    private boolean signingIn = false;
    public Object signIn(){
		if(signingIn) return true;
		
		try {
			signingIn = true;
			
			HttpsURLConnection con = (HttpsURLConnection) new URL("https://" + host + "/").openConnection();
			con.setRequestProperty("User-Agent", "SchulNetz Client");
			InputStream in = con.getInputStream();

			currentCookies = getCookies(con.getHeaderFields());
			Document doc = Jsoup.parse(in, "UTF-8", "/");

			Elements es = doc.getElementsByAttributeValue("name", "loginhash");
			if(es == null || es.size() <= 0 || !es.get(0).hasAttr("value")) {
				Exception e = new Exception(){
					@Nullable
					@Override
					public String getLocalizedMessage() {
						return (String)Resources.getSystem().getText(android.R.string.httpErrorBadUrl);
					}
				};
				
				signingIn = false;
				return e;
			}
			String loginHash = es.get(0).attr("value");

			con.disconnect();

			String postData = "login=" + URLEncoder.encode(username, "UTF-8") + "&passwort=" + URLEncoder.encode(password, "UTF-8") + "&loginhash=" + URLEncoder.encode(loginHash, "UTF-8");

			con = (HttpsURLConnection)new URL("https://" + host + "/index.php?pageid=1").openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			con.setRequestProperty("Connection", "keep-alive");
			con.setRequestProperty("Cookie", currentCookies);
			con.setRequestProperty("User-Agent", "SchulNetz Client");
			DataOutputStream ds = new DataOutputStream(con.getOutputStream());
			ds.write(postData.getBytes("UTF-8"));
			ds.flush();

			in = con.getInputStream();
			doc = Jsoup.parse(in, "UTF-8", "/");
			Element navBar = doc.getElementById("nav-main-menu");
			if(navBar == null) {
				signingIn = false;
				return false;
			} else if(navBar.childNodeSize() <= 0 || !navBar.child(0).hasAttr("href")) {
				signingIn = false;
				return null;
			}
			
			String href = navBar.child(0).attr("href");
			if(!href.contains("&id=") || !href.contains("&transid=")) {
				signingIn = false;
				return null;
			}
			
			id = href.substring(href.indexOf("&id=") + "&id=".length(), href.lastIndexOf("&transid="));
			currentTransId = getTransId(doc);
			currentCookies = getCookies(con.getHeaderFields());
			con.disconnect();
			
			if(manager != null) manager.start();
			signedIn = true;
			
			signingIn = false;
			return true;
		} catch(Exception e){
			e.printStackTrace();
			signingIn = false;
			return e;
		}
	}
	
	private boolean signingOut = false;
	public Object signOut(){
		if(signingOut) return true;
		
    	try {
    		if(id.length() <= 0 || currentTransId.length() <= 0 || currentCookies.length() <= 0) return true;
			
    		signingOut = true;
    		
			HttpsURLConnection con = (HttpsURLConnection) new URL("https://" + host + "/index.php?pageid=9999&id=" + id + "&transid=" + currentTransId).openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			con.setRequestProperty("Connection", "keep-alive");
			con.setRequestProperty("Cookie", currentCookies);
		    con.setRequestProperty("User-Agent", "SchulNetz Client");
		    con.getInputStream();
			con.disconnect();

			id = currentTransId = currentCookies = "";
			cookiesList = new ArrayList<>();
			
			if(manager != null) manager.stop();
			signedIn = false;
			
			signingOut = false;
			return true;
		} catch(Exception e){
    		e.printStackTrace();
			signingOut = false;
    		return e;
		}
	}

	public Object resetTimeout(){
		if(!signedIn) return null;
		
		try{
			HttpsURLConnection con = (HttpsURLConnection)new URL("https://" + host + "/xajax_js.php?pageid=1&id=" + id + "&transid=" + currentTransId).openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			con.setRequestProperty("Referer", "https://" + host + "/index.php?pageid=");
			con.setRequestProperty("Origin", "https://" + host);
			con.setRequestProperty("Accept", "*/*");
			con.setRequestProperty("Sec-Fetch-Site", "same-origin");
			con.setRequestProperty("Sec-Fetch-Mode", "cors");
			con.setRequestProperty("Sec-Fetch-Dest", "empty");
			con.setRequestProperty("Connection", "keep-alive");
			con.setRequestProperty("Cookie", currentCookies);
			con.setRequestProperty("User-Agent", "SchulNetz Client");
			DataOutputStream ds = new DataOutputStream(con.getOutputStream());
			ds.write(("xajax=reset_timeout&xajaxr=" + Long.toString(System.currentTimeMillis())).getBytes("UTF-8"));
			ds.flush();

			con.getInputStream();
			con.disconnect();

			return true;
		} catch(Exception e){
			e.printStackTrace();
			return e;
		}
	}
	
	private ArrayList<String> queue = new ArrayList<>();
	public Object loadPage(String pageId){
		if(signingOut) return null;
		else if(!signedIn && !signingIn) signIn();
		
		if(queue.contains(pageId)) return false;
		
		queue.add(pageId);
		while(!queue.get(0).equals(pageId)) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		while(signingIn) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if(!signedIn || signingOut) {
			queue.remove(0);
			return null;
		}
		
		try {
			HttpsURLConnection con = (HttpsURLConnection) new URL("https://" + host + "/index.php?pageid=" + pageId + "&id=" + id + "&transid=" + currentTransId).openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			con.setRequestProperty("Connection", "keep-alive");
			con.setRequestProperty("Cookie", currentCookies);
			con.setRequestProperty("User-Agent", "SchulNetz Client");

			InputStream in = con.getInputStream();
			Document src = Jsoup.parse(in, "UTF-8", "/");
			
			currentTransId = getTransId(src);
			currentCookies = getCookies(con.getHeaderFields());
			
			con.disconnect();
			
			queue.remove(0);
			
			signedIn = src.getElementById("nav-main-menu") != null;
			if(!signedIn) return null;
			else return src;
		} catch (IOException e) {
			e.printStackTrace();
			queue.remove(0);
			return e;
		}
	}
	
	public Object loadSchedule(Calendar from, Calendar to){
		return  loadSchedule(from, to, "day");
	}
	
	public Object loadSchedule(Calendar from, Calendar to, String view){
		if((!signedIn && !signingIn) || signingOut) return null;
		
		while(signingIn || queue.size() > 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if(!signedIn) return null;
		
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			
			Calendar to2 = Calendar.getInstance();
			to2.setTimeInMillis(to.getTimeInMillis());
			to2.add(Calendar.DAY_OF_YEAR, 1);
			
			HttpsURLConnection con = (HttpsURLConnection) new URL("https://" + host + "/scheduler_processor.php?view=" + view + "&curr_date=2005-05-10&min_date=" + sdf.format(from.getTime()) + "&max_date=" + sdf.format(to2.getTime()) + "&ansicht=schueleransicht&id=" + id + "&transid=potato&pageid=22202").openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			con.setRequestProperty("Connection", "keep-alive");
			con.setRequestProperty("Cookie", currentCookies);
			con.setRequestProperty("User-Agent", "SchulNetz Client");
			
			InputStream in = con.getInputStream();
			Document src = Jsoup.parse(in, "UTF-8", "/", Parser.xmlParser());
			
			con.disconnect();
			return src;
		} catch (IOException e) {
			e.printStackTrace();
			return e;
		}
	}

	private String getTransId(Document src) {
		Element navBar = src.getElementById("nav-main-menu");
		if(navBar == null || navBar.childNodeSize() <= 0 || !navBar.child(0).hasAttr("href")) return "";
		String href = navBar.child(0).attr("href");
		if(!href.contains("transid=")) return "";
		return href.substring(href.indexOf("transid=") + "transid=".length());
	}

	private String getCookies(Map<String, List<String>> headers) {
		String cookies = "";

		for(String value : headers.get("Set-Cookie")) {
			boolean existing = false;

			for(String old : cookiesList) {
				if(Objects.equals(value.substring(0, value.indexOf('=')), old.substring(0, old.indexOf('=')))) {
					cookiesList.set(cookiesList.indexOf(old), value);
					existing = true;
					break;
				}
			}

			if(!existing) cookiesList.add(value);
		}

		for(String value : cookiesList) {
			if (cookies != "") cookies += ";";
			cookies += (value.indexOf(';') >= 0) ? value.substring(0, value.indexOf(';')) : value;
		}

		return cookies;
	}
	
	public void close(){
		if(manager != null) {
			((Application)Variables.get().currentContext.getApplicationContext()).unregisterActivityLifecycleCallbacks(manager);
			manager.stop();
			manager = null;
		}
		
		if(signedIn) signOut();
	}
}
