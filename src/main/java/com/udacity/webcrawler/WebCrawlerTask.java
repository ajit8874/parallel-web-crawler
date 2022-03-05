package com.udacity.webcrawler;//package com.udacity.webcrawler;
//
//import com.udacity.webcrawler.parser.PageParser;
//import com.udacity.webcrawler.parser.PageParserFactory;
//
//import java.time.Clock;
//import java.time.Instant;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//import java.util.concurrent.ConcurrentSkipListSet;
//import java.util.concurrent.RecursiveTask;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
//public class WebCrawlerTask extends RecursiveTask<Void> {
//    PageParserFactory pageParserFactory;
//    Clock clock;
//    String url;
//    ConcurrentSkipListSet<String> visitedUrls;
//    List<Pattern> ignoredUrls;
//    int maxDepth;
//    Instant crawlDeadline;
//    ConcurrentHashMap<String, Integer> wordCountMap;
//
//    public WebCrawlerTask(
//                          Clock clock,
//                          PageParserFactory pageParserFactory,
//                          String url,
//                          Set<String> visitedUrls,
//                          List<Pattern> ignoredUrls,
//                          int maxDepth,
//                          Instant crawlDeadline,
//                          Map<String, Integer> wordCountMap) {
//        this.pageParserFactory = pageParserFactory;
//        this.crawlDeadline = crawlDeadline;
//        this.clock = clock;
//        this.url = url;
//        this.visitedUrls = (ConcurrentSkipListSet<String>) visitedUrls;
//        this.ignoredUrls = ignoredUrls;
//        this.maxDepth = maxDepth;
//        this.wordCountMap = (ConcurrentHashMap<String, Integer>) wordCountMap;
//    }
//
//    @Override
//
//    protected Void compute() {
////        Boolean computeResult=false;
//        if(visitedUrls.contains(url) ||  maxDepth == 0 || clock.instant().isAfter(crawlDeadline)){
//
//            return null;
//        }
//        if (clock.instant().isBefore(crawlDeadline)==true){
//            // updating and adding url to the visited url set
//
//            visitedUrls.add(url);
//            // parse the page
//            PageParser.Result crawlResult = pageParserFactory.get(url).parse();
//            // updating the wordCountMap
//            crawlResult.getWordCounts().forEach((word, count) ->
//                    wordCountMap.compute(word, (w, c) -> Objects.isNull(c) ? count : c + count));
//
//            List<WebCrawlerTask> childWebCrawlerTasks = crawlResult.getLinks()
//                    .stream()
//                    .map(childURL -> new WebCrawlerTask(clock, pageParserFactory, childURL, visitedUrls, ignoredUrls, maxDepth - 1, crawlDeadline, wordCountMap))
//                    .collect(Collectors.toList());
//            invokeAll(childWebCrawlerTasks);
//
//
//        }
//        else{
//            return null;
//        }
//
//        return null;
//    }
//}

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

public class WebCrawlerTask extends RecursiveTask<Boolean> {
    private final Clock clock;
    private String url;
    private Instant deadline;
    private final List<Pattern> ignoredUrls;
    private int maxDepth;
    private ConcurrentMap<String, Integer> counts;
    private ConcurrentSkipListSet<String> visitedUrls;
    private final PageParserFactory pageParserFactory;

    public WebCrawlerTask(Clock clock, String url, Instant deadline, List<Pattern> ignoredUrls, int maxDepth, ConcurrentMap<String, Integer> counts, ConcurrentSkipListSet<String> visitedUrls, PageParserFactory pageParserFactory) {
        this.clock = clock;
        this.url = url;
        this.deadline = deadline;
        this.ignoredUrls = ignoredUrls;
        this.maxDepth = maxDepth;
        this.counts = counts;

        this.visitedUrls = visitedUrls;
        this.pageParserFactory = pageParserFactory;
    }

    @Override
    protected Boolean compute() {

        if (visitedUrls.contains(url)) {
            return false;
        }

        else if (maxDepth == 0 || clock.instant().isAfter(deadline) ||ignoredUrls.contains(Pattern.compile(url))) {
            return false;
        }
        else{

            for (Pattern pattern : ignoredUrls) {
                while (pattern.matcher(url).matches()) {
                    return false;
                }
            }

            visitedUrls.add(url);
            PageParser.Result result = pageParserFactory.get(url).parse();


            for (ConcurrentMap.Entry<String, Integer> value : result.getWordCounts().entrySet()) {
                counts.compute(value.getKey(), (k, v) -> (v == null) ? value.getValue() : value.getValue() + v);
            }


            List<WebCrawlerTask> tasks = new ArrayList<>();

            Iterator it =result.getLinks().iterator();

            while (it.hasNext()){
                tasks.add(new WebCrawlerTask(clock, (String)it.next(), deadline, ignoredUrls, maxDepth -1, counts, visitedUrls, pageParserFactory));

            }

            invokeAll(tasks);
            return true;


        }


    }
}
