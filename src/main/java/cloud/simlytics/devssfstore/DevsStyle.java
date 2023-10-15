package cloud.simlytics.devssfstore;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import org.immutables.value.Value;

@Target({ElementType.PACKAGE, ElementType.TYPE})
@Value.Style(typeAbstract = "Abstract*", typeImmutable = "*", stagedBuilder = true)
public @interface DevsStyle {

}


