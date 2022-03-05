//package com.udacity.webcrawler;
//
//import com.udacity.webcrawler.json.CrawlResult;
//import com.udacity.webcrawler.parser.PageParserFactory;
//
//import javax.inject.Inject;
//import javax.inject.Provider;
//import java.time.Clock;
//import java.time.Duration;
//import java.time.Instant;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentSkipListSet;
//import java.util.concurrent.ForkJoinPool;
//import java.util.logging.Logger;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
///**
// * A concrete implementation of {@link WebCrawler} that runs multiple threads on a
// * {@link ForkJoinPool} to fetch and process multiple web pages in parallel.
// */
//final class ParallelWebCrawler implements WebCrawler {
//  private final Clock clock;
//  private final Duration timeout;
//  private final int popularWordCount;
//  private int maxDepth;
//  private final ForkJoinPool pool;
//  PageParserFactory pageParserFactory;
//  private List<Pattern> ignoredUrls;
//
//  @Inject
//  ParallelWebCrawler(
//          Clock clock,
//          PageParserFactory pageParserFactory,
//          @MaxDepth int maxDepth,
//          @Timeout Duration timeout,
//          @PopularWordCount int popularWordCount,
//          @IgnoredUrls List<Pattern> ignoredUrls,
//          @TargetParallelism int threadCount) {
//    this.clock = clock;
//    this.pageParserFactory = pageParserFactory;
//    this.maxDepth = maxDepth;
//    this.timeout = timeout;
//    this.popularWordCount = popularWordCount;
//    this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
//    this.ignoredUrls = ignoredUrls;
//  }
//
//  @Override
//  public CrawlResult crawl(List<String> startingUrls) {
//
//
//    ConcurrentSkipListSet<String> visitedUrls = new ConcurrentSkipListSet<>();
//
//    ConcurrentHashMap<String, Integer> wordCountMap = new ConcurrentHashMap<>();
//
//    Logger.getLogger(this.getClass().getName()).info(String.valueOf(Runtime.getRuntime().availableProcessors()));
//
//    Instant crawlDeadline = clock.instant().plus(timeout);
//    while(!clock.instant().isAfter(crawlDeadline)){
//      for(String url: startingUrls){
//        pool.invoke(new WebCrawlerTask(clock, pageParserFactory, url, visitedUrls, ignoredUrls, maxDepth, crawlDeadline, wordCountMap));
//      }
//    }
//    return new CrawlResult.Builder()
//            .setWordCounts(WordCounts.sort(wordCountMap, popularWordCount))
//            .setUrlsVisited(visitedUrls.size())
//            .build();
//
////    return new CrawlResult.Builder().build();
//  }
//
//  @Override
//  public int getMaxParallelism() {
//
//    return Runtime.getRuntime().availableProcessors();
//  }
//}




package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParserFactory;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/**
 * A concrete implementation of {@link WebCrawler} that runs multiple threads on a
 * {@link ForkJoinPool} to fetch and process multiple web pages in parallel.
 */
final class ParallelWebCrawler implements WebCrawler {
  private final Clock clock;
  private final Duration timeout;
  private final int popularWordCount;
  private final ForkJoinPool pool;
  private final List<Pattern> ignoredUrls;
  private final int maxDepth;
  private final PageParserFactory parserFactory;

  @Inject
  ParallelWebCrawler(
          Clock clock,
          @Timeout Duration timeout,
          @PopularWordCount int popularWordCount,
          @TargetParallelism int threadCount,
          @IgnoredUrls List<Pattern> ignoredUrls,
          @MaxDepth int maxDepth,
          PageParserFactory parserFactory) {
    this.clock = clock;
    this.timeout = timeout;
    this.popularWordCount = popularWordCount;
    this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
    this.ignoredUrls = ignoredUrls;
    this.maxDepth = maxDepth;
    this.parserFactory = parserFactory;
  }

  @Override
  public CrawlResult crawl(List<String> startingUrls) {
    Instant deadline = clock.instant().plus(timeout);
    ConcurrentMap<String, Integer> counts = new ConcurrentHashMap<>();
    ConcurrentSkipListSet<String> visitedUrls = new ConcurrentSkipListSet<>();
    Iterator it = startingUrls.iterator();
    while (it.hasNext()) {
      pool.invoke(new WebCrawlerTask(clock, (String)it.next(), deadline, ignoredUrls, maxDepth, counts, visitedUrls, parserFactory));
    }



    while (counts.isEmpty()) {
      CrawlResult.Builder crawlerResult=new CrawlResult.Builder();
      return crawlerResult
              .setWordCounts(counts)
              .setUrlsVisited(visitedUrls.size())
              .build();
    }
    CrawlResult.Builder crawlResult=new CrawlResult.Builder();
    return crawlResult
            .setWordCounts(WordCounts.sort(counts, popularWordCount))
            .setUrlsVisited(visitedUrls.size())
            .build();
  }


  @Override
  public int getMaxParallelism() {
    return Runtime.getRuntime().availableProcessors();
  }

}