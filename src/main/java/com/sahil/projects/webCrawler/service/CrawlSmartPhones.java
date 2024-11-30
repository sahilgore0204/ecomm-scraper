package com.sahil.projects.webCrawler.service;

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

	public void startCrawling() throws InterruptedException
	{
		String url=CrawlerConstants.AMAZON_URL;
		
		for(int i=3001;i<=4000;i++) {
			StringBuilder pageUrl=new StringBuilder();
			pageUrl.append(url+"/s?k=smartphones&page=").append(i);
			storeProducts(pageUrl);
			System.out.println("stored "+String.valueOf(i)+" products");
			Thread.sleep((int) (Math.random() * 10+1));
		}
		
	}

	private void storeProducts(StringBuilder pageUrl) {
		try {
			//only crawling smart phones
			Faker faker=new Faker();
			OkHttpClient client = new OkHttpClient(); // Connection pooling by default

	        Request request = new Request.Builder()
	                .url(pageUrl.toString())
	                .header("User-Agent", faker.internet().userAgentAny())
	                .build();
	        Response response = client.newCall(request).execute();
	        
			Document doc=Jsoup.parse(response.body().string());
			Elements products=doc.select(".s-card-container");
			for(Element product:products) {
				String productUrl=CrawlerConstants.AMAZON_URL+product.select("a.s-no-outline").attr("href");
				String imgUrl=product.select("a.s-no-outline").select("img.s-image").attr("src");
				String description=product.select("a.s-underline-text").select("span").text();
				String rating=product.select("span.a-icon-alt").text();
				String noOfReviews=product.select("a>span.a-size-base").text();
				String price=product.select("span.a-price-whole").text();
				
				//stroring the product details in mongodb database
				
				  MongoClient mongoClient=MongoClients.create(CrawlerConstants.MONGODB_URL);
				  MongoDatabase database = mongoClient.getDatabase("scraped_data");
				  MongoCollection<org.bson.Document> collection =
				  database.getCollection("products");
				  org.bson.Document productToSave = new org.bson.Document("product_description",description)
						  .append("price", price)
						  .append("product_url", productUrl) 
						  .append("image_url", imgUrl)
						  .append("rating", rating) .append("noOfReviews", noOfReviews);
					  
				 // Insert the document into the collection
			     collection.insertOne(productToSave);
					 
				
			}
		} catch (Exception e) {
			// TODO define a well known logging mechanism
			e.printStackTrace();
		}
		
	}
}
