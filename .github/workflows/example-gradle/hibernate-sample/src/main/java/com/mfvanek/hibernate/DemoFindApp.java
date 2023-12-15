package com.mfvanek.hibernate;

import com.mfvanek.hibernate.entities.TestEvent;
import com.mfvanek.hibernate.utils.SessionFactoryUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

@Slf4j
public class DemoFindApp {

    public static void main(final String[] args) {
        try (SessionFactory sessionFactory = SessionFactoryUtil.build()) {
            final TestEvent first = findById(sessionFactory, 11L);
            log.info("Inside main: {}", first);
            SessionFactoryUtil.validateQueriesCount(1);
        }
    }

    private static TestEvent findById(final SessionFactory sessionFactory, final long id) {
        TestEvent result = new TestEvent();
        try (Session session = sessionFactory.openSession()) {
            final Transaction trn = session.beginTransaction();
            try {
                result = session.get(TestEvent.class, id);
                log.trace("Inside Session: {}", result);
                // In order to avoid LazyInitializationException
                Hibernate.initialize(result);
                trn.commit();
            } catch (Throwable e) {
                log.error("Error occurred", e);
                if (trn.isActive()) {
                    trn.markRollbackOnly();
                }
            }
        }
        log.info("Inside findById: {}", result);
        return result;
    }
}
