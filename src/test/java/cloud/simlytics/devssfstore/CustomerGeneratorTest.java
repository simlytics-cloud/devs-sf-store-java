package cloud.simlytics.devssfstore;

import devs.msg.Bag;
import devs.msg.time.DoubleSimTime;
import devs.msg.time.LongSimTime;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test Customer Generator")
class CustomerGeneratorTest {

  private final TreeMap<Double, List<Customer>> customerSchedule = new TreeMap<>();

  CustomerGeneratorTest() {
    customerSchedule.put(1.0, Collections.singletonList(
        Customer.builder().twait(1.0).tenter(1.0).tleave(0.0).build()));
    customerSchedule.put(2.0, Collections.singletonList(
        Customer.builder().twait(4.0).tenter(2.0).tleave(0.0).build()));
  }



  @Test
  @DisplayName("Test generation of customers from a table")
  void testCustomerGeneration() {
    CustomerGenerator customerGenerator = new CustomerGenerator(customerSchedule);
    DoubleSimTime t0 = DoubleSimTime.builder().t(0.0).build();

    // First customer out should be at t = 1;
    DoubleSimTime t1 = customerGenerator.timeAdvanceFunction(t0);
    assert t1.getT() == 1;

    // Get first customer
    Bag output1 = customerGenerator.outputFunction();
    Customer customer1 = CustomerGenerator.generatorOutputPort
        .getValue(output1.getPortValueList().get(0));
    assertEquals(1.0, customer1.getTenter(), 0.01);
    assertEquals(1.0, customer1.getTwait(), 0.01);

    // Execute internal transition at t = 1, removing customer 1 from the list
    customerGenerator.internalStateTransitionFunction(t1);

    // Next customer should exit at t = 2
    DoubleSimTime t2 = customerGenerator.timeAdvanceFunction(t1);
    assert t2.getT() == 2;

    // Get first customer
    Bag output2 = customerGenerator.outputFunction();
    Customer customer2 = CustomerGenerator.generatorOutputPort
        .getValue(output2.getPortValueList().get(0));
    assertEquals(2.0, customer2.getTenter(), 0.01);
    assertEquals(4.0, customer2.getTwait(), 0.01);
  }

}