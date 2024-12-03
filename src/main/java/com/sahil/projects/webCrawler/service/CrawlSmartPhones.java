package com.sahil.projects.webCrawler.service;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.javafaker.Faker;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sahil.projects.webCrawler.constants.CrawlerConstants;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CrawlSmartPhones {

	//commit check 2
	private Queue<String> seedUrls=Stream.of(CrawlerConstants.AMAZON_URL+"/s?k=laptops",CrawlerConstants.AMAZON_URL+"/s?k=smartphones",CrawlerConstants.AMAZON_URL+"/s?k=gaming+console",CrawlerConstants.AMAZON_URL+"/s?k=television",CrawlerConstants.AMAZON_URL+"/s?k=chargers").collect(Collectors.toCollection(LinkedList::new));
	private Queue<String> testUrls=Stream.of(CrawlerConstants.AMAZON_URL+"/s?k=gaming+console").collect(Collectors.toCollection(LinkedList::new));
	public void startCrawling() throws InterruptedException
	{
		MongoClient mongoClient=MongoClients.create(CrawlerConstants.MONGODB_URL);
		OkHttpClient client = new OkHttpClient.Builder()
				.connectTimeout(5, TimeUnit.SECONDS)
				.readTimeout(5, TimeUnit.SECONDS)   // Read timeout
                .writeTimeout(5, TimeUnit.SECONDS)
				.build();
		
		int itr=0;
		int limit=seedUrls.size()*30;
		while(itr<limit) {
			String urlToCrawl=seedUrls.poll();
			String nextUrl=storeProducts(urlToCrawl,mongoClient,client);
			seedUrls.offer(nextUrl);
			itr++;
			Thread.sleep((int) (Math.random() * 10+1));
		}
		System.out.println("ended");
		/*for(int i=5001;i<=11000;i++) {
			StringBuilder pageUrl=new StringBuilder();
			pageUrl.append(url+"/s?k=smartphones&page=").append(i);
			storeProducts(pageUrl,i,mongoClient,client);
			
		}*/
		
	}

	private String storeProducts(String pageUrl,MongoClient mongoClient,OkHttpClient client) throws InterruptedException {
		String nextUrl="";
		try {
			//only crawling smart phones
			Faker faker=new Faker();
			 // Connection pooling by default

	        Request request = new Request.Builder()
	                .url(pageUrl)
	                .header("User-Agent", faker.internet().userAgentAny())
	                .build();
	        Response response = client.newCall(request).execute();
	        if(!response.isSuccessful()) {
	        	throw new Exception("connection timeout for "+pageUrl);
	        }
	        String htmlString=response.body().string();
			Document doc=Jsoup.parse(htmlString);
			Elements products=doc.select(".s-card-container");
			
			nextUrl=pageUrl;
			if(doc.select("a[aria-label*=next]").last()!=null)
			nextUrl=CrawlerConstants.AMAZON_URL + doc.select("a[aria-label*=next]").last().attr("href");
			for(Element product:products) {
				String productUrl=CrawlerConstants.AMAZON_URL+product.select("a.s-no-outline").attr("href");
				String imgUrl=product.select("a.s-no-outline").select("img.s-image").attr("src");
				String description=product.select("a.s-underline-text").select("span").text();
				String rating=product.select("span.a-icon-alt").text();
				String noOfReviews=product.select("a>span.a-size-base").text();
				String price=product.select("span.a-price-whole").text();
				//stroring the product details in mongodb database
				
				MongoDatabase database = mongoClient.getDatabase("scraped_data");
				MongoCollection<org.bson.Document> collection =database.getCollection("products");
				org.bson.Document productToSave = new org.bson.Document("product_description",description)
						  .append("price", price)
						  .append("product_url", productUrl) 
						  .append("image_url", imgUrl)
						  .append("rating", rating) 
						  .append("noOfReviews", noOfReviews);
					  
				 // Insert the document into the collection
			     collection.insertOne(productToSave);
			}
			
			System.out.println("crawling successfull for "+pageUrl);
		} catch (Exception e) {
			// TODO define a well known logging mechanism
			System.out.println("error occured for "+pageUrl);
			e.printStackTrace();
			Thread.sleep(5000);
		}
		return nextUrl;
	}
}
