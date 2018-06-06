package examples

import dera.core.Behavior
import dera.core.Event
import dera.runtime.StandaloneExecutionDomain
import groovy.transform.TypeChecked
import org.slf4j.Logger

import static dera.DERA.*
import static org.slf4j.LoggerFactory.getLogger

@TypeChecked
class HelloWorld {

    private static final Logger log = getLogger(HelloWorld.class)

    static void main(String[] args) {

        def domain = StandaloneExecutionDomain.getInstance()
        domain.uri = "urn:htr3n:casestudy:HelloWorld"
        domain.start()

        newEventLogger(domain)

        def startEvent = newEventType("start", domain)
        def requestReceived = newEventType("request-received", domain)
        def greetingCreated = newEventType("greeting-created", domain)
        def greetingReplied = newEventType("greeting-replied", domain)

        def a0 = newAction("A0", domain)
        a0.addInput startEvent
        a0.addOutput requestReceived
        a0.behavior = new Behavior() {
            @Override
            void run(Event e) {
                log.info "I'm receiving a request " + e
            }
        }

        def a1 = newAction("A1", domain)
        a1.addInput requestReceived
        a1.addOutput greetingCreated
        a1.behavior = new Behavior() {
            @Override
            void run(Event e) {
                log.info "I'm creating a new greeting " + e
            }
        }
        def a2 = newAction("A2", domain)
        a2.addInput greetingCreated
        a2.addOutput greetingReplied
        a2.behavior = new Behavior() {
            @Override
            void run(Event e) {
                log.info "I'm replying to the invoker " + e
            }
        }
        def app1 = newApplication("HelloWorld-1")
        app1.addStart requestReceived
        app1.addEnd greetingReplied

        def app2 = newApplication("HelloWorld-2")
        app2.addStart startEvent
        app2.addEnd greetingCreated

        domain.addApplication app1
        domain.addApplication app2

        domain.publish startEvent.newInstance()
        domain.publish startEvent.newInstance()
    }
}