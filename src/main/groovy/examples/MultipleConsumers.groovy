package examples

import dera.runtime.StandaloneExecutionDomain
import groovy.transform.TypeChecked

import static dera.DERA.*

@TypeChecked
class MultipleConsumers {

    static void main(String[] args) {

        def domain = StandaloneExecutionDomain.getInstance()
        domain.uri = "urn:com:example:MultipleConsumers"
        domain.start()

        newEventLogger(domain)

        def startEvent = newEventType("start")
        def e1 = newEventType("e1")
        def e2 = newEventType("e2")
        def e3 = newEventType("e3")
        def e4 = newEventType("e4")
        def e5 = newEventType("e5")
        def e6 = newEventType("e6")

        def a0 = newAction("A0", domain)
        a0.addInput startEvent
        a0.addOutput e1

        def a1 = newAction("A1", domain)
        a1.addInput e1
        a1.addOutput e2

        def a2 = newAction("A2", domain)
        a2.addInput e1
        a2.addOutput e3

        def condition = newCondition("Condition", domain)
        condition.addInput e2
        condition.addTrueEvent e4
        condition.addFalseEvent e5

        def barrier = newBarrier("Barrier", domain)
        barrier.addInput e3
        barrier.addInput e4
        barrier.addOutput e6

        def finish = newAction('finish', domain)
        finish.addInput e5
        finish.addInput e6

        def app1 = newApplication("HelloWorld-1")
        app1.addStart e1
        app1.addEnd e3

        domain.addApplication app1
        // firing
        //INSTANCE.publish startEvent.newInstance()
    }
}