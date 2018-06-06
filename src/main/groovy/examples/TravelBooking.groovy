package examples

import dera.core.Behavior
import dera.core.Event
import dera.core.Predicate
import dera.runtime.StandaloneExecutionDomain
import groovy.transform.TypeChecked
import org.slf4j.Logger

import static dera.DERA.*
import static org.slf4j.LoggerFactory.getLogger

@TypeChecked
class TravelBooking {

    private static final Logger log = getLogger(TravelBooking.class)

    static void main(String[] args) {

        def domain = StandaloneExecutionDomain.getInstance()
        domain.uri = "urn:htr3n:casestudy:TravelBooking"
        domain.start()

        newEventLogger(domain)

        def receive = newAction("ReceiveRequest", domain)
        receive.setBehavior(new Behavior() {
            @Override
            void run(Event e) {
                log.info "Receiving a request " + e
            }
        })

        def checkCard = newAction("CheckCreditCard", domain)
        checkCard.setBehavior(new Behavior() {
            @Override
            void run(Event e) {
                log.info "Checking credit cards " + e
            }
        })

        def bookHotel = newAction("BookHotel", domain)
        bookHotel.setBehavior(new Behavior() {
            @Override
            void run(Event e) {
                log.info "Booking a hotel " + e
            }
        })

        def bookFlight = newAction("BookFlight", domain)
        bookFlight.setBehavior(new Behavior() {
            @Override
            void run(Event e) {
                log.info "Booking a flight " + e
            }
        })

        def bookCar = newAction("BookCar", domain)
        bookCar.setBehavior(new Behavior() {
            @Override
            void run(Event e) {
                log.info "Booking a car " + e
            }
        })

        def chargeCard = newAction("ChargeCreditCard", domain)
        chargeCard.setBehavior(new Behavior() {
            @Override
            void run(Event e) {
                log.info "Charging credit cards " + e
            }
        })

        def finish = newAction("Finish", domain);

        def choice = newCondition("ChoiceOnCardChecking", domain)
        choice.predicate = new Predicate() {
            @Override
            boolean eval() {
                return true
            }
        }

        def barrier = newBarrier("BookingBarrier", domain)

        def startEvent = newEventType("start", domain)
        def requestReceived = newEventType("request-received", domain)
        def cardChecked = newEventType("card-checked", domain)
        def cardCheckedOK = newEventType("card-checked-ok", domain)
        def cardCheckedFailed = newEventType("card-checked-failed", domain)
        def hotelBooked = newEventType("hotel-booked", domain)
        def flightBooked = newEventType("flight-booked", domain)
        def carBooked = newEventType("car-booked", domain)
        def bookingFinished = newEventType("booking-finished", domain)
        def cardCharged = newEventType("card-charged", domain)

        receive.addInput startEvent
        receive.addOutput requestReceived

        checkCard.addInput requestReceived
        checkCard.addOutput cardChecked

        choice.addInput cardChecked
        choice.addTrueEvent cardCheckedOK
        choice.addFalseEvent cardCheckedFailed

        bookHotel.addInput cardCheckedOK
        bookHotel.addOutput hotelBooked

        bookFlight.addInput cardCheckedOK
        bookFlight.addOutput flightBooked

        bookCar.addInput cardCheckedOK
        bookCar.addOutput carBooked

        barrier.addInput carBooked
        barrier.addInput hotelBooked
        barrier.addInput flightBooked
        barrier.addOutput bookingFinished

        chargeCard.addInput bookingFinished
        chargeCard.addOutput cardCharged

        finish.addInput cardCharged
        finish.addInput cardCheckedFailed
        // firing
        domain.publish startEvent.newInstance()
    }

}
