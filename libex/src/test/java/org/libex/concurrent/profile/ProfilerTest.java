package org.libex.concurrent.profile;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.joda.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.libex.concurrent.profile.Profiling.Callback;
import org.libex.concurrent.profile.Profiling.ProfileResult;
import org.libex.test.TestBaseLocal;
import org.libex.test.mockito.answer.DelayedAnswer;
import org.libex.test.mockito.answer.InstanceAnswer;
import org.libex.test.mockito.answer.ThrowingAnswer;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ProfilerTest extends TestBaseLocal {

    private static final String RETURN_VALUE = "default";

    @Mock
    public Callable<String> callable;

    @Mock
    public Callback callback;

    @Captor
    public ArgumentCaptor<ProfileResult> captor;

    private Profiler profiler;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(callable.call()).then(
                DelayedAnswer.create(
                        InstanceAnswer.wrap(RETURN_VALUE),
                        Duration.millis(200)));

        profiler = new Profiler();
        profiler.setObserver(callback);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testNulls() {
        nullPointerTester.testAllPublicConstructors(Profiler.class);
        nullPointerTester.testAllPublicInstanceMethods(profiler);
    }

    @Test
    public void testCall_noObserver() throws Exception {
        // setup
        profiler = new Profiler();

        // test
        String result = profiler.call(callable);

        // verify
        assertThat(result, equalTo(RETURN_VALUE));
        verifyNoMoreInteractions(callback);
        verify(callable).call();
    }

    @Test
    public void testCall_withObserver() throws Exception {
        // test
        String result = profiler.call(callable);

        // verify
        assertThat(result, equalTo(RETURN_VALUE));
        verifyCallableAndCallbackCalled();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCall_exceptionFromCallable() throws Exception {
        // setup
        Exception e = new Exception();
        reset(callable);
        when(callable.call()).then(
                DelayedAnswer.create(
                        ThrowingAnswer.wrap(e),
                        Duration.millis(200)));

        // expect
        expectedException.expect(sameInstance(e));

        try {
            // test
            profiler.call(callable);
        } finally {
            // verify
            verifyCallableAndCallbackCalled();
        }
    }

    @Test
    public void testCall_exceptionFromCallback() throws Throwable {
        // setup
        RuntimeException e = new RuntimeException("Some test runtime exception - testCall_exceptionFromCallback");
        reset(callback);
        doThrow(e).when(callback).processProfileEvent(Mockito.any(ProfileResult.class));

        // expect
        expectedException.expect(sameInstance(e));

        try {
            // test
            profiler.call(callable);
        } finally {
            // verify
            verifyCallableAndCallbackCalled();
        }
    }

    @Test
    public void testCall_exceptionFromCallableAndCallback() throws Throwable {
        // setup
        Exception callableEx = new Exception();
        RuntimeException callbackEx =
                new RuntimeException("Some test runtime exception - testCall_exceptionFromCallableAndCallback");
        reset(callable, callback);
        when(callable.call()).then(
                DelayedAnswer.create(
                        ThrowingAnswer.wrap(callableEx),
                        Duration.millis(200)));
        doThrow(callbackEx).when(callback).processProfileEvent(Mockito.any(ProfileResult.class));

        // expect
        expectedException.expect(sameInstance(callableEx));

        try {
            // test
            profiler.call(callable);
        } finally {
            // verify
            verifyCallableAndCallbackCalled();
        }
    }

    private void verifyCallableAndCallbackCalled() throws Exception {
        verify(callable).call();
        verify(callback).processProfileEvent(captor.capture());
        ProfileResult profileResult = captor.getValue();
        assertThat(profileResult.getCallable(), sameInstance((Object) callable));
        assertThat(profileResult.getTimeSpan().getDurationIn(TimeUnit.MILLISECONDS),
                allOf(greaterThanOrEqualTo(180L),
                        lessThan(300L)));
    }
}
