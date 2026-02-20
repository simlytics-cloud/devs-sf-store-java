package cloud.simlytics.devssfstore;

import devs.iso.time.DoubleSimTime;
import devs.msg.state.TimeState;
import java.util.ArrayList;
import java.util.List;

public class ClerkState extends TimeState<DoubleSimTime> {

  protected List<Customer> customerList = new ArrayList<>();

  public ClerkState(DoubleSimTime currentTime) {
    super(currentTime);
  }

  public List<Customer> getCustomerList() {
    return customerList;
  }

  public void setCustomerList(List<Customer> customerList) {
    this.customerList = customerList;
  }

}
