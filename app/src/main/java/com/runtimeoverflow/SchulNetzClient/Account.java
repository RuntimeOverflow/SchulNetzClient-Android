package com.runtimeoverflow.SchulNetzClient;

import android.content.res.Resources;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

public class Account {
	public String host;
	public String username;
	public String password;

    private String id = "";
    private String currentTransId = "";
    private ArrayList<String> cookiesList = new ArrayList<>();
    private String currentCookies = "";

    private SessionManager manager = new SessionManager(this);

    public Account(String host, String username, String password){
        this.host = host;
        this.username = username;
        this.password = password;
    }

	public Object signIn(){
		try {
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
			if(navBar == null) return false;
			else if(navBar.childNodeSize() <= 0 || !navBar.child(0).hasAttr("href")) return null;
			String href = navBar.child(0).attr("href");
			if(!href.contains("&id=") || !href.contains("&transid=")) return null;
			id = href.substring(href.indexOf("&id=") + "&id=".length(), href.lastIndexOf("&transid="));
			currentTransId = getTransId(doc);
			currentCookies = getCookies(con.getHeaderFields());
			con.disconnect();

			manager.start();
			return true;
		} catch(Exception e){
			e.printStackTrace();
			return e;
		}
	}

	public Object signOut(){
    	try {
    		if(id.length() <= 0 || currentTransId.length() <= 0 || currentCookies.length() <= 0) return true;

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

			manager.stop();
			return true;
		} catch(Exception e){
    		e.printStackTrace();
    		return e;
		}
	}

	public Object resetTimeout(){
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

	public Object loadPage(String pageId){
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
}
