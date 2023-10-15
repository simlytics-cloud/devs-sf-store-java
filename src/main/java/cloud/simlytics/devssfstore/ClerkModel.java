package cloud.simlytics.devssfstore;

import devs.PDEVSModel;
import devs.Port;
import devs.msg.Bag;
import devs.msg.PortValue;
import devs.msg.time.DoubleSimTime;
import devs.msg.time.LongSimTime;
import java.util.ArrayList;
import java.util.List;

public class ClerkModel extends PDEVSModel<DoubleSimTime, List<Customer>> {

  public static Port<Customer> clerkInputPort = new Port<>("arrive");

  public static Port<Customer> clerkOutputPort = new Port<>("depart");

  public ClerkModel(String modelIdentifier) {
    super(new ArrayList<>(), modelIdentifier);
  }

  @Override
  protected void internalStateTransitionFunction(DoubleSimTime doubleSimTime) {
    modelState.remove(0);  // remove customer that exits at this time
    if (!modelState.isEmpty()) {
      serveNextCustomer(doubleSimTime);
    }
  }

  private void serveNextCustomer(DoubleSimTime doubleSimTime) {
    // Start serving next customer
    Customer nextCustomer = modelState.remove(0);
    nextCustomer = nextCustomer.withTleave(doubleSimTime.getT() + nextCustomer.getTwait());
    modelState.add(0, nextCustomer);
  }

  @Override
  protected void externalSateTransitionFunction(DoubleSimTime doubleSimTime, Bag bag) {
    for (PortValue<?> pv: bag.getPortValueList()) {
      Customer customer = clerkInputPort.getValue(pv);
      modelState.add(customer);
      if (modelState.size() == 1) { // If this is the first customer, start serving
        serveNextCustomer(doubleSimTime);
      }
    }
  }

  @Override
  protected void confluentStateTransitionFunction(DoubleSimTime doubleSimTime, Bag bag) {
    internalStateTransitionFunction(doubleSimTime);
    externalSateTransitionFunction(doubleSimTime, bag);
  }

  @Override
  protected DoubleSimTime timeAdvanceFunction(DoubleSimTime doubleSimTime) {
    if (modelState.isEmpty()) {
      return DoubleSimTime.builder().t(Double.MAX_VALUE).build();
    } else {
      double tLeave = modelState.get(0).getTleave();
      return DoubleSimTime.builder().t(tLeave).build();
    }
  }

  @Override
  protected Bag outputFunction() {
    Customer exitingCustomer = modelState.get(0);
    return Bag.builder().addPortValueList(clerkOutputPort.createPortValue(exitingCustomer)).build();
  }
}
