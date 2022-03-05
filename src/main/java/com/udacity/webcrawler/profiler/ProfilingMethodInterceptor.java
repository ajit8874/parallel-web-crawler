package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor<T> implements InvocationHandler {

  private final Clock clock;
  private ProfilingState state;
  private T delegate;
  private static final Logger logger = Logger.getLogger(ProfilingMethodInterceptor.class.getName());

  ProfilingMethodInterceptor(Clock clock, ProfilingState state,T delegate) {
    this.clock = Objects.requireNonNull(clock);
    this.state = state;
    this.delegate = delegate;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // If the method contains @Profiled annotation record the invoked method call duration using ProfileState

    Instant initialTime = clock.instant();
    Object methodInvoking = new Object();
    if (!method.isAnnotationPresent(Profiled.class)){
      return method.invoke(delegate, args);

    }
    else{

      try {
        methodInvoking = method.invoke(delegate, args);
      }
      catch (InvocationTargetException targetException){
        throw targetException.getTargetException();
      }
      catch(UndeclaredThrowableException e){
        e.getUndeclaredThrowable();
//        Logger.getLogger(this.getClass().getName()).severe(e.getClass().getSimpleName() + " occurred while invoking method: " + method.getName());
      } finally {
        Duration elapsedTime = Duration.between(initialTime, clock.instant());
        state.record(delegate.getClass(), method, elapsedTime);
      }
      return methodInvoking;

    }



  }

}