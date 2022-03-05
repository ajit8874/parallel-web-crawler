package com.udacity.webcrawler.profiler;

import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {

  private final Clock clock;
  private final ProfilingState state = new ProfilingState();
  private final ZonedDateTime startTime;

  @Inject
  ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    this.startTime = ZonedDateTime.now(clock);
  }

  @Override
  public <T> T wrap(Class<T> klass, T delegate) {
    Objects.requireNonNull(klass);

    // TODO: Use a dynamic proxy (java.lang.reflect.Proxy) to "wrap" the delegate in a
    //       ProfilingMethodInterceptor and return a dynamic proxy from this method.
    //       See https://docs.oracle.com/javase/10/docs/api/java/lang/reflect/Proxy.html.
//    if (Arrays.stream(klass.getMethods()).noneMatch(method -> method.isAnnotationPresent(Profiled.class))) {
//      throw new IllegalArgumentException("Class does not contain any @Profiled Annotated methods");
//    }

    if (!Arrays.stream(klass.getMethods()).noneMatch(method -> method.isAnnotationPresent(Profiled.class))){
      ProfilingMethodInterceptor profilingMethodInterceptor=new ProfilingMethodInterceptor<>(clock,state,delegate);
      @SuppressWarnings("unchecked")
      T methodProfilerProxy = (T) Proxy.newProxyInstance(
              klass.getClassLoader(),
              new Class<?>[] { klass },
              profilingMethodInterceptor
      );

      return methodProfilerProxy;

    }
    else {
      throw new IllegalArgumentException("there is not any @Profiled Annotated methods");

    }




//    @SuppressWarnings("unchecked")
//    T methodProfilerProxy = (T) Proxy.newProxyInstance(
//            klass.getClassLoader(),
//            new Class<?>[] { klass },
//            new ProfilingMethodInterceptor(clock, state, delegate)
//    );
//
//    return methodProfilerProxy;

//    return delegate;

  }

  @Override
  public void writeData(Path path) throws IOException {
    // TODO: Write the ProfilingState data to the given file path. If a file already exists at that
    //       path, the new data should be appended to the existing file.
    Boolean add = false;
    File addFile = new File(path.toString());

    if (addFile.exists()==true){
      add=true;
    }
    String x = path.toString();
    FileWriter fileWriter = new FileWriter(x, add);
    try (fileWriter){
      state.write(fileWriter);
    } catch (IOException ex) {
      ex.printStackTrace();
    }

  }

  @Override
  public void writeData(Writer writer) throws IOException {
    writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
    writer.write(System.lineSeparator());
    state.write(writer);
    writer.write(System.lineSeparator());
  }
}