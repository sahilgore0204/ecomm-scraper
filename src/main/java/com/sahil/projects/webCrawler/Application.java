package com.sahil.projects.webCrawler;

import com.sahil.projects.webCrawler.service.CrawlSmartPhones;

public class Application {

	public static void main(String[] args) throws InterruptedException {
		CrawlSmartPhones crawlSmartPhones=new CrawlSmartPhones();
		crawlSmartPhones.startCrawling();
		System.exit(0);
	}

}
