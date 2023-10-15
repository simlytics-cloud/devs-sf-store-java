package cloud.simlytics.devssfstore;


import devs.OutputCouplingHandler;
import devs.msg.PortValue;
import example.generator.GeneratorModel;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StoreCouplingHandler extends OutputCouplingHandler {


  public StoreCouplingHandler() {
    super(Optional.empty(), Optional.empty(), Optional.empty());
  }

  @Override
  public void handlePortValue(String sender, PortValue<?> portValue, Map<String, List<PortValue<?>>> receiverMap,
      List<PortValue<?>> outputList) {
    if (sender.startsWith("clerk")) {
      Customer customer = ClerkModel.clerkOutputPort.getValue(portValue);
      PortValue<Customer> inputPortValue = StoreObserver.observerInputPort.createPortValue(customer);
      addInputPortValue(inputPortValue, StoreObserver.modelIdentifier, receiverMap);
    } else if (sender.equals(CustomerGenerator.modelIdentifier)) {
      Customer customer = CustomerGenerator.generatorOutputPort.getValue(portValue);
      PortValue<Customer> inputPortValue = ClerkModel.clerkInputPort.createPortValue(customer);
      addInputPortValue(inputPortValue, "clerk1", receiverMap);
    }
  }
}
