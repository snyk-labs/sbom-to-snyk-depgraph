package com.mfvanek.hibernate;

import com.mfvanek.hibernate.entities.TestEvent;
import com.mfvanek.hibernate.entities.TestEventInfo;
import com.mfvanek.hibernate.enums.TestEventType;
import com.mfvanek.hibernate.utils.RowsCountValidator;
import com.mfvanek.hibernate.utils.SessionFactoryUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.time.Duration;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DemoInsertApp {

    private static final int LOOP_COUNT = 1_000;

    public static void main(final String[] args) {
        try (SessionFactory sessionFactory = SessionFactoryUtil.build()) {
            final RowsCountValidator validator = new RowsCountValidator(sessionFactory);

            saveFromCurrentThread(sessionFactory);
            saveFromNewSingleThread(sessionFactory);
            saveUsingThreadPool(sessionFactory);

            final long expectedEventsCount = 3 * 2 * LOOP_COUNT;
            validator.validate(expectedEventsCount, TestEvent.class);
            validator.validate(3 * expectedEventsCount, TestEventInfo.class);
            SessionFactoryUtil.validateQueriesCount(12 * LOOP_COUNT + 4);
        }
    }

    private static void saveItem(final SessionFactory sessionFactory) {
        try (Session session = sessionFactory.openSession()) {
            final Transaction trn = session.beginTransaction();
            try {
                final TestEvent firstEvent = new TestEvent("Our very first event!", new Date());
                addTestEventInfo(firstEvent, 11);
                session.persist(firstEvent);

                final TestEvent secondEvent = new TestEvent("A follow up event", new Date());
                addTestEventInfo(secondEvent, 22);
                session.persist(secondEvent);
                trn.commit();
            } catch (Throwable e) {
                log.error("Error occurred", e);
                if (trn.isActive()) {
                    trn.markRollbackOnly();
                }
            }
        }
    }

    private static void addTestEventInfo(final TestEvent event, final int number) {
        final Set<TestEventInfo> info = Set.of(
                new TestEventInfo(TestEventType.MAIN, String.format("%d, first, main", number)),
                new TestEventInfo(TestEventType.ADDITIONAL, String.format("%d, second, additional", number)),
                new TestEventInfo(TestEventType.EXTENDED, String.format("%d, third, ext", number)));
        event.addEventInfo(info);
    }

    private static void saveFromCurrentThread(final SessionFactory sessionFactory) {
        new DataSaver("current thread", sessionFactory).run();
    }

    @SneakyThrows
    private static void saveFromNewSingleThread(final SessionFactory sessionFactory) {
        final Thread thread = new Thread(new DataSaver("new single thread", sessionFactory), "pg_single");
        thread.start();
        thread.join(Duration.ofSeconds(100L));
    }

    @SneakyThrows
    private static void saveUsingThreadPool(final SessionFactory sessionFactory) {
        final String message = "Saving items using thread pool";
        log.trace(message);
        final long timeStart = System.currentTimeMillis();
        final int threadsCount = Runtime.getRuntime().availableProcessors() + 1;
        try (ExecutorService threadPool = Executors.newFixedThreadPool(threadsCount)) {
            for (int i = 0; i < LOOP_COUNT; ++i) {
                threadPool.submit(() -> DemoInsertApp.saveItem(sessionFactory));
            }
            threadPool.shutdown();
            threadPool.awaitTermination(10L, TimeUnit.SECONDS);
        }
        log.trace("{}. Completed. Time elapsed = {} (ms)", message, System.currentTimeMillis() - timeStart);
    }

    private static class DataSaver implements Runnable {

        private final String message;
        private final SessionFactory sessionFactory;

        DataSaver(final String message, final SessionFactory sessionFactory) {
            this.message = message;
            this.sessionFactory = sessionFactory;
        }

        @Override
        public void run() {
            log.trace("Saving items from {}", this.message);
            final long timeStart = System.currentTimeMillis();
            for (int i = 0; i < LOOP_COUNT; ++i) {
                saveItem(sessionFactory);
            }
            log.trace("{}. Completed. Time elapsed = {} (ms)", this.message, System.currentTimeMillis() - timeStart);
        }
    }
}
