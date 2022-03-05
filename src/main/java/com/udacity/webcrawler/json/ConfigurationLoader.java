package com.udacity.webcrawler.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A static utility class that loads a JSON configuration file.
 */
public final class ConfigurationLoader {

  private final Path path;

  /**
   * Create a {@link ConfigurationLoader} that loads configuration from the given {@link Path}.
   */
  public ConfigurationLoader(Path path) {
    this.path = Objects.requireNonNull(path);
  }

  /**
   * Loads configuration from this {@link ConfigurationLoader}'s path
   *
   * @return the loaded {@link CrawlerConfiguration}.
   */
  public CrawlerConfiguration load() throws FileNotFoundException {
    // TODO: Fill in this method.
    CrawlerConfiguration.Builder builder=new CrawlerConfiguration.Builder();
    FileReader fileReader=new FileReader(this.path.toString());
    try(fileReader){
      return read(fileReader);
    }
    catch(IOException ex){

      ex.printStackTrace();
    }
    return builder.build();

//    return new CrawlerConfiguration.Builder().build();

//    return new CrawlerConfiguration.Builder().build();
  }

  /**
   * Loads crawler configuration from the given reader.
   *
   * @param reader a Reader pointing to a JSON string that contains crawler configuration.
   * @return a crawler configuration
   */
  public static CrawlerConfiguration read(Reader reader) {
    // This is here to get rid of the unused variable warning.
    Objects.requireNonNull(reader);
    // TODO: Fill in this method
//    Objects.requireNonNull(reader);

    ObjectMapper mapper = new ObjectMapper();
    mapper.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
    CrawlerConfiguration.Builder builder = new CrawlerConfiguration.Builder();
    try {

      builder = mapper.readValue(reader, CrawlerConfiguration.Builder.class);
    } catch (Exception ex) {

      ex.printStackTrace();
    }
    return builder.build();

//    return new CrawlerConfiguration.Builder().build();
  }
}
