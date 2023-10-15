package cloud.simlytics.devssfstore;

import static org.junit.jupiter.api.Assertions.*;

import devs.msg.Bag;
import devs.msg.PortValue;
import devs.msg.time.DoubleSimTime;
import devs.msg.time.LongSimTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClerkModelTest {

  @Test
  @DisplayName("Test clerk model")
  void testClerkModel() {

    ClerkModel clerkModel = new ClerkModel("clerkModel");
    DoubleSimTime t1 = DoubleSimTime.builder().t(1.0).build();

    // next time should be max value
    assert clerkModel.timeAdvanceFunction(t1).getT() == Double.MAX_VALUE;

    // Send first customer
    Customer customer1 = Customer.builder().twait(1.0).tenter(1.0).tleave(0.0).build();
    PortValue<Customer> pv = ClerkModel.clerkInputPort.createPortValue(customer1);
    Bag bag1 = Bag.builder().addPortValueList(pv).build();
    clerkModel.externalSateTransitionFunction(t1, bag1);

    // Next time should be 2
    DoubleSimTime t2 = clerkModel.timeAdvanceFunction(t1);
    assertEquals(2.0, t2.getT(), 0.01);

    // Output first customer and do confluent state transition at t2
    Bag outBag2 = clerkModel.outputFunction();
    Customer outCustomer2 = ClerkModel.clerkOutputPort.getValue(outBag2.getPortValueList().get(0));
    assertEquals(2.0, outCustomer2.getTleave(), 0.01);

    Customer inCustomer2 = Customer.builder().twait(4.0).tenter(2.0).tleave(0.0).build();
    Bag inBag2 = Bag.builder().addPortValueList(
        ClerkModel.clerkInputPort.createPortValue(inCustomer2)).build();
    clerkModel.confluentStateTransitionFunction(t2, inBag2);

    // Next transition should be at t = 6
    DoubleSimTime t6 = clerkModel.timeAdvanceFunction(t2);
    assertEquals(6.0, t6.getT(), 0.01);

    Bag outBag6 = clerkModel.outputFunction();
    Customer outCustomer6 = ClerkModel.clerkOutputPort.getValue(outBag6.getPortValueList().get(0));
    assertEquals(6.0, outCustomer6.getTleave(), 0.01);
  }

}