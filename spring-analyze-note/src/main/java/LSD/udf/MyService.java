package LSD.udf;

import java.lang.annotation.*;

/**
 * @author LSD
 * @date 2021/1/11 19:58
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface MyService {

	String tablesName() default "";
}
