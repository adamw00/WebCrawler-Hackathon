package com.eulerity.hackathon.imagefinder;

import java.io.IOException;
import java.util.ArrayList;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler{

    private ArrayList<String> imageList = new ArrayList<String>();
    private String startURL;
    // how many sub-pages deep (recursive limit)
    // 0: only crawls the startURL, 
    // 1: crawls startURL and crawls all of its sub-pages
    // 2: crawls start, sub, and sub of sub etc.
    private static final int MAX_DEPTH=3;

    public Crawler(String url) {
        startURL = url;
    }

    // given a url, returns arraylist of all image src on given url
    public ArrayList<String> getImages(String url) {
        ArrayList<String> iList = new ArrayList<String>();
        try {
            Document doc = Jsoup.connect(url).get();
            Elements images = doc.select("img[src~=(?i)\\.(png|jpe?g|gif)]");

            for (Element image : images) {
                //stops repeat images
                if (imageList.contains(image.absUrl("src"))) continue;
                iList.add(image.absUrl("src"));
            }
            return iList;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return iList;
    }

    // given a url, will recurse through all sub-pages 
    // images on these sub-pages will be added to imageList
    ArrayList<String> eList = new ArrayList<String>(); 
    public void recurseURL(String url, ArrayList<String> urls, int depth) throws URISyntaxException {
        // stops repeat sub-pages
        if (urls.contains(url)) return;
        if (depth > MAX_DEPTH) return;

        urls.add(url);
        // adds images of current url
        imageList.addAll(getImages(url));
        
        if (depth == MAX_DEPTH) return;

        // calls recurseURL for sub-pages
        try {
            Document doc = Jsoup.connect(url).get();
            Elements elements = doc.select("a[href]");

            for(Element element : elements){
                // stops re-crawling pages
                if (eList.contains(element.absUrl("href"))) continue;
                eList.add(element.absUrl("href"));

                // skips empty urls
                if (element.absUrl("href").equals("")) continue;

                // makes sure doesn't leave website
                URI uri = new URI(startURL);
                if (!element.absUrl("href").contains(uri.getHost())) continue;

                // https:// instead of https:/
                if (!element.absUrl("href").substring(4,9).contains("//")) continue;
                recurseURL(element.absUrl("href"), urls, depth+1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // returns final imageList
    public ArrayList<String> getImageList() {
        return imageList;
    }
}