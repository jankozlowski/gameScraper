package com.binaryalchemist.GameScraper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSession;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.RawAPIResponse;
import facebook4j.conf.ConfigurationBuilder;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;

public class AppContextListener implements ServletContextListener {

	private ArrayList<String> ignoreList;
	private ArrayList<String> facebookIgnoreList;
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {

		System.out.println("Listener has been shutdown");

	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {

		TimerTask vodTimer = new VodTimerTask();

		Timer timer = new Timer();
		timer.schedule(vodTimer, 10000, 900000);

	}

	class VodTimerTask extends TimerTask {

		@Override
		public void run() {
			
			ArrayList<String> searchTitle=new ArrayList<>();
			searchTitle.add("zakazana");
			searchTitle.add("kraken");
			searchTitle.add("talisman");
			
			Float price = 60f;
			String[] facebookGroupID = {"393609927334644","680439345342603"};
			
			if(ignoreList == null){
				ignoreList = new ArrayList<>();
			}
			if(facebookIgnoreList == null){
				facebookIgnoreList = new ArrayList<>();
			}
		
			//check gry-planszowe.pl  
			Document doc;
			try {
				doc = Jsoup.connect("http://www.gry-planszowe.pl/forum/viewforum.php?f=53").get();
			
			Elements links = doc.getElementsByClass("row1");
			for (Element link : links) {
				  String linkHref = link.child(0).attr("href");
				  if(linkHref.length()>0){
					  String url = linkHref.substring(2, linkHref.length());
					  for(int a=0; a<searchTitle.size(); a++){
						  checkIfExists("http://www.gry-planszowe.pl/forum/"+url,searchTitle.get(a));
					  }
				  }
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			/*	//check gumtree
			doc = Jsoup.connect("https://www.gumtree.pl/s-gry-planszowe/v1c9516p1").get();
			links = doc.getElementsByClass("container");
			for (Element link : links) {
				 String linkHref = link.child(0).child(0).attr("href");
				  if(linkHref.length()>0){
					  String url = linkHref.substring(1, linkHref.length());
					  checkIfExists("https://www.gumtree.pl/"+url,searchTitle);
				  }
			}
			//check olx
			doc = Jsoup.connect("https://www.olx.pl/oferty/q-"+searchTitle).get();
			links = doc.getElementsByClass("link");
			for (Element link : links) {
				 String linkHref = link.attr("href");
				 String linkText = link.text();
				  if(linkHref.length()>0 &&linkText.toLowerCase().contains(searchTitle)){
					 checkIfExistsOlx(linkHref,searchTitle,price);
				  }
			}
			
			//check allegro
			doc = Jsoup.connect("http://allegro.pl/listing?string="+searchTitle+"&bmatch=base-relevance-floki-5-nga-cul-1-1-0222").get();
			links = doc.getElementsByClass("header__title__2RWO4");
			for (Element link : links) {
				 String linkHref = link.child(0).attr("href");
				 String linkText = link.text();
				 System.out.println(linkHref);
				 System.out.println(linkText);
				 
				  if(linkHref.length()>0 &&linkText.toLowerCase().contains(searchTitle)){
					  checkIfExistsAllegro(linkHref,searchTitle,price);
				  }
			}*/
			
			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true)
			  .setOAuthAppId("****")
			  .setOAuthAppSecret("****")
			  .setOAuthAccessToken("****")
			  .setOAuthPermissions("public_profile,user_friends,email,user_about_me,user_actions.books,user_actions.fitness,user_actions.music,user_actions.news,user_actions.video,user_birthday,user_education_history,user_events,user_games_activity,user_hometown,user_likes,user_location,user_managed_groups,user_photos,user_posts,user_relationships,user_relationship_details,user_religion_politics,user_tagged_places,user_videos,user_website,user_work_history,read_custom_friendlists,read_insights,read_audience_network_insights,read_page_mailboxes,manage_pages,publish_pages,publish_actions,rsvp_event,pages_show_list,pages_manage_cta,pages_manage_instant_articles,ads_read,ads_management,business_management,pages_messaging,pages_messaging_subscriptions,pages_messaging_payments,pages_messaging_phone_number");
			FacebookFactory ff = new FacebookFactory(cb.build());
			Facebook facebook = ff.getInstance();
			
			//Long live acess token generator
			// https://graph.facebook.com/oauth/access_token?client_id=APP_ID&client_secret=APP_SECRET&grant_type=fb_exchange_token&fb_exchange_token=CURRENT_ACCESS_TOKEN
			
			facebook.getAuthorization();
		
			for(int b=0; b<facebookGroupID.length;b++){
				RawAPIResponse res;
				try {
					res = facebook.callGetAPI(facebookGroupID[b]+"/feed?fields=from,message");
					JSONObject jsonObject = res.asJSONObject();
					
					JSONArray arr = jsonObject.getJSONArray("data");
					for(int a=0; a<arr.length();a++){
						JSONObject obj = arr.getJSONObject(a);
						String id = obj.getString("id");
						for(int c=0; c<searchTitle.size(); c++){
							if(obj.get("message").toString().toLowerCase().contains(searchTitle.get(c))&&checkIfNotInArrayFacebook(id,facebookIgnoreList)){
								SendMail mail = new SendMail();
								mail.sendEmail("melchiah@o2.pl", "Emperor of Mankind", "patdurman@wp.pl", "Searched game "+searchTitle.get(c)+ " found", searchTitle.get(c)+" found on: https://www.facebook.com/groups/"+facebookGroupID[b]);
								facebookIgnoreList.add(id);
							}
						}
					}
				} 
				catch (FacebookException e) {e.printStackTrace();} 
				catch (JSONException e) {e.printStackTrace();}
				
				
				try {
					URL myURL = new URL("https://aqueous-sea-14369.herokuapp.com/game");
					HttpURLConnection myURLConnection = (HttpURLConnection) myURL.openConnection();
					myURLConnection.setRequestMethod("GET");
					myURLConnection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
					
					myURLConnection.connect();
					BufferedReader in = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
					in.readLine();
					in.close();

					myURLConnection.disconnect();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				
				System.out.println("Done" + new Date().toString());
			}
		   
		}
	}
	
	public boolean checkIfExists(String url,String searchTitle) throws IOException{
		Document doc = Jsoup.connect(url).get();
		boolean contains = doc.toString().toLowerCase().contains(searchTitle);
		
		if(contains && checkIfNotInArray(url,ignoreList)){
			SendMail mail = new SendMail();
			mail.sendEmail("melchiah@o2.pl", "Emperor of Mankind", "patdurman@wp.pl", "Searched game "+searchTitle+ " found", searchTitle+" found on: "+ url);
			ignoreList.add(url);
		}
		return contains;
	}
	public boolean checkIfExistsOlx(String url,String searchTitle, Float price) throws IOException{
		Document doc = Jsoup.connect(url).get();
		boolean contains = doc.toString().toLowerCase().contains(searchTitle);
		Elements links = doc.getElementsByClass("price-label");
		String stringValue = links.get(0).text();
		stringValue= stringValue.replaceAll("\\s+","");
		stringValue= stringValue.replaceAll(",",".");
		Float value = Float.valueOf(stringValue.substring(0, stringValue.indexOf('z')));
		if(contains&& value<=price){
			SendMail mail = new SendMail();
			mail.sendEmail("melchiah@o2.pl", "Emperor of Mankind", "patdurman@wp.pl", "Searched game "+searchTitle+ " found", searchTitle+" found on: "+ url);
		}
		return contains;
	}
	public boolean checkIfExistsAllegro(String url,String searchTitle, Float price) throws IOException{
		Document doc = Jsoup.connect(url).get();
		boolean contains = doc.toString().toLowerCase().contains(searchTitle);
		Elements links = doc.select("div.price");
		String stringValue = links.get(0).text();

		
		stringValue= stringValue.replaceAll("\\s+","");
		stringValue= stringValue.replaceAll(",",".");
		Float value = Float.valueOf(stringValue.substring(0, stringValue.indexOf('z')));
		if(contains&& value<=price){
			SendMail mail = new SendMail();
			mail.sendEmail("melchiah@o2.pl", "Emperor of Mankind", "patdurman@wp.pl", "Searched game "+searchTitle+ " found", searchTitle+" found on: "+ url);
		}
		return contains;
	}
	public boolean checkIfNotInArray(String string, ArrayList<String> array){
		boolean in= true;
		string = string.substring(0, string.indexOf("&sid"));
		for(String ignoreString: array){
			ignoreString = ignoreString.substring(0, ignoreString.indexOf("&sid"));			
			if(ignoreString.equals(string)){
				in = false; 
			}
		}
		return in;
	}
	
	public boolean checkIfNotInArrayFacebook(String string, ArrayList<String> array){
		boolean in= true;
		for(String ignoreString: array){
			if(ignoreString.equals(string)){
				in = false; 
			}
		}
		return in;
	}

}