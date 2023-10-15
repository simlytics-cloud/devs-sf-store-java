package cloud.simlytics.devssfstore;

import devs.PDEVSModel;
import devs.Port;
import devs.msg.Bag;
import devs.msg.time.DoubleSimTime;
import devs.msg.time.LongSimTime;
import java.util.List;
import java.util.TreeMap;

public class CustomerGenerator extends PDEVSModel<DoubleSimTime, TreeMap<Double, List<Customer>>> {

  public static String modelIdentifier = "customerGenerator";
  public static Port<Customer> generatorOutputPort = new Port<>("OUTPUT");

  public CustomerGenerator(TreeMap<Double, List<Customer>> modelState) {
    super(modelState, modelIdentifier);
  }

  @Override
  protected void internalStateTransitionFunction(DoubleSimTime doubleSimTime) {
    // Remove the customers generated at this time.  They were sent as output during the call to
    // the output function
    modelState.remove(doubleSimTime.getT());
  }

  @Override
  protected void externalSateTransitionFunction(DoubleSimTime doubleSimTime, Bag bag) {

  }

  @Override
  protected void confluentStateTransitionFunction(DoubleSimTime doubleSimTime, Bag bag) {

  }

  @Override
  protected DoubleSimTime timeAdvanceFunction(DoubleSimTime doubleSimTime) {
    if (modelState.isEmpty()) {
      return DoubleSimTime.builder().t(Double.MAX_VALUE).build();
    } else {
      return DoubleSimTime.builder().t(modelState.firstKey()).build();
    }
  }

  @Override
  protected Bag outputFunction() {
    List<Customer> customers = modelState.firstEntry().getValue();
    Bag.Builder builder = Bag.builder();
    for (Customer customer: customers) {
      builder.addPortValueList(generatorOutputPort.createPortValue(customer));
    }
    return builder.build();
  }
}
