package cloud.simlytics.devssfstore;

import devs.PDEVSModel;
import devs.Port;
import devs.msg.Bag;
import devs.msg.PortValue;
import devs.msg.time.DoubleSimTime;

public class StoreObserver extends PDEVSModel<DoubleSimTime, Void> {

  public static String modelIdentifier = "customerObserver";
  public static Port<Customer> observerInputPort = new Port<>("INPUT");

  public StoreObserver(Void modelState) {
    super(modelState, modelIdentifier);
  }

  @Override
  public void internalStateTransitionFunction(DoubleSimTime doubleSimTime) {

  }

  @Override
  public void externalStateTransitionFunction(DoubleSimTime doubleSimTime, Bag bag) {
    for (PortValue<?> pv: bag.getPortValueList()) {
      Customer customer = observerInputPort.getValue(pv);
      System.out.println("Customer leaving at " + doubleSimTime.getT() +
          " after a wait of " + customer.getTwait());
    }
  }

  @Override
  public void confluentStateTransitionFunction(DoubleSimTime doubleSimTime, Bag bag) {
    externalStateTransitionFunction(doubleSimTime, bag);
  }

  @Override
  public DoubleSimTime timeAdvanceFunction(DoubleSimTime doubleSimTime) {
    return DoubleSimTime.builder().t(Double.MAX_VALUE).build();
  }

  @Override
  public Bag outputFunction() {
    return Bag.builder().build();
  }
}
