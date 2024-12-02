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
	private Queue<String> seedUrls=Stream.of(CrawlerConstants.AMAZON_URL+"/s?k=laptops").collect(Collectors.toCollection(LinkedList::new));
	
	public void startCrawling() throws InterruptedException
	{
		MongoClient mongoClient=MongoClients.create(CrawlerConstants.MONGODB_URL);
		OkHttpClient client = new OkHttpClient.Builder()
				.connectTimeout(5, TimeUnit.SECONDS)
				.readTimeout(5, TimeUnit.SECONDS)   // Read timeout
                .writeTimeout(5, TimeUnit.SECONDS)
				.build();
		
		while(!seedUrls.isEmpty()) {
			String urlToCrawl=seedUrls.poll();
			if(urlToCrawl.equals(CrawlerConstants.AMAZON_URL))
				break;
			//System.out.println("parsing "+urlToCrawl);
			seedUrls.offer(storeProducts(urlToCrawl,mongoClient,client));
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
			Document doc=Jsoup.parse(response.body().string());
			Elements products=doc.select(".s-card-container");
			
			nextUrl=CrawlerConstants.AMAZON_URL + doc.select("span.s-pagination-strip li");
			System.out.println(nextUrl);
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
			Thread.sleep((int) (Math.random() * 10+1));
		} catch (Exception e) {
			// TODO define a well known logging mechanism
			System.out.println("error occured for "+pageUrl);
			e.printStackTrace();
			Thread.sleep(60000);
		}
		return nextUrl;
	}
}
