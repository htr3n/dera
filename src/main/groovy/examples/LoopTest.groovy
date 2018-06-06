package examples

import dera.core.Behavior
import dera.core.Event
import dera.core.Predicate
import dera.runtime.StandaloneExecutionDomain
import groovy.transform.TypeChecked
import org.slf4j.Logger

import java.util.concurrent.atomic.AtomicInteger

import static dera.DERA.*
import static org.slf4j.LoggerFactory.getLogger

@TypeChecked
class LoopTest {

    static final Logger log = getLogger(LoopTest.class);
    static final AtomicInteger count1 = new AtomicInteger();
    static final AtomicInteger count2 = new AtomicInteger();
    static final AtomicInteger count3 = new AtomicInteger();
    static final AtomicInteger condition = new AtomicInteger(10);

    static void main(String[] args) {

        def domain = StandaloneExecutionDomain.getInstance()

        new Thread({
            domain.start()
        } as Runnable, "dera-INSTANCE").start()

        while (!domain.isStarted()) {
            try {
                Thread.sleep 500
            } catch (Exception e) {
            }
        }

        def startEvent = newEventType("start", domain)

        def a1 = newAction("A1", domain)
        def a2 = newAction("A2", domain)
        def c = newCondition("ConditionC", domain)
        c.predicate = new Predicate() {
            @Override
            boolean eval() {
                int v = condition.decrementAndGet()
                log.info "Loop condition counter " + v
                return v >= 0
            }
        };
        def a3 = newAction("A3", domain)

        def a1Finished = newEventType("a1Finished", domain)
        def a2Finished = newEventType("a2Finished", domain)
        def cFinishedTrue = newEventType("cFinishedTrue", domain)
        def cFinishedFalse = newEventType("cFinishedFalse", domain)

        a1.addInput startEvent
        a1.addOutput a1Finished

        a1.behavior = new Behavior() {
            @Override
            void run(Event e) {
                log.info "count1 = " + count1.incrementAndGet()
            }
        }


        a2.addInput a1Finished
        a2.addInput cFinishedTrue
        a2.addOutput a2Finished
        a2.behavior = new Behavior() {
            @Override
            void run(Event e) {
                log.info "count2 = " + count2.incrementAndGet()
            }
        }

        c.addInput a2Finished
        c.addTrueEvent cFinishedTrue
        c.addFalseEvent cFinishedFalse

        a3.addInput cFinishedFalse;
        a3.behavior = new Behavior() {
            @Override
            void run(Event e) {
                log.info "count3 = " + count3.incrementAndGet()
            }
        }
        // firing
        domain.publish startEvent.newInstance()
    }
}
