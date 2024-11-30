package com.sahil.projects.webCrawler;

import com.sahil.projects.webCrawler.service.CrawlSmartPhones;

public class Application {

	public static void main(String[] args) throws InterruptedException {
		
		CrawlSmartPhones crawlSmartPhones=new CrawlSmartPhones();
		
		crawlSmartPhones.startCrawling();
		
		/*
		 * String amazonUrl = "https://www.amazon.in/s?k=smartphones&page=3"; try {
		 * Document doc = Jsoup.connect(amazonUrl).userAgent("Mozilla/5.0").get();
		 * Elements products = doc.select(".s-card-container");
		 * 
		 * for (Element product : products) { String title =
		 * product.select(".a-size-medium").text(); String price =
		 * product.select(".a-price-whole").text(); String rating =
		 * product.select(".a-icon-alt").text(); String productUrl =
		 * "https://www.amazon.in" + product.select(".a-link-normal").attr("href");
		 * 
		 * System.out.println("Title: " + title); System.out.println("Price: ₹" +
		 * price); System.out.println("Rating: " + rating); System.out.println("URL: " +
		 * productUrl); System.out.println("------"); } } catch (Exception e) {
		 * e.printStackTrace(); }
		 */
	}

}
