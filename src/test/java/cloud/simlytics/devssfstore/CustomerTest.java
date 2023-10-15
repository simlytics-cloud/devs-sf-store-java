package cloud.simlytics.devssfstore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import devs.msg.Bag;
import devs.msg.DevsMessage;
import devs.msg.ExecuteTransition;
import devs.msg.PortValue;
import devs.msg.time.LongSimTime;
import devs.utils.DevsObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CustomerTest {

  ObjectMapper objectMapper = DevsObjectMapper.buildObjectMapper();

  @Test
  @DisplayName("Serialize and deserialize customer port value")
  void serializeDeserializeCustomerPortValue() throws IOException {
    Customer customer1 = Customer.builder().twait(1.0).tenter(1.0).tleave(0.0).build();

    String customer1Json = objectMapper.writeValueAsString(customer1);
    System.out.println(customer1Json);

//    String executeTransitionJson = Files.readString(Path.of("data/customerTest.json"));
//    DevsMessage devsMessage = objectMapper.readValue(executeTransitionJson, DevsMessage.class);
//    assert devsMessage instanceof ExecuteTransition;
//    ExecuteTransition<LongSimTime> executeTransitionDes = (ExecuteTransition<LongSimTime>)
//        devsMessage;
//    PortValue<?> pvDes =executeTransitionDes.getModelInputsOption().get().getPortValueList().get(0);
//    assert pvDes.getValue() instanceof Customer;

    PortValue<Customer> pv = ClerkModel.clerkInputPort.createPortValue(customer1);
    Bag inputBag = Bag.builder().addPortValueList(pv).build();
    ExecuteTransition<?> executeTransition = ExecuteTransition.builder()
        .time(LongSimTime.builder().t(0L).build())
        .modelInputsOption(inputBag)
        .build();

    String executeTransitionJson2 = objectMapper.writeValueAsString(executeTransition);
    System.out.println(executeTransitionJson2);

    DevsMessage devsMessage = objectMapper.readValue(executeTransitionJson2, DevsMessage.class);
    assert devsMessage instanceof ExecuteTransition;
    ExecuteTransition<LongSimTime> executeTransitionDes = (ExecuteTransition<LongSimTime>)
        devsMessage;
    PortValue<?> pvDes =executeTransitionDes.getModelInputsOption().get().getPortValueList().get(0);
    assert pvDes.getValue() instanceof Customer;

  }

}
