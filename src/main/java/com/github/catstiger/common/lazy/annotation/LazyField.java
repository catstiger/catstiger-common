package com.github.catstiger.common.lazy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在Entity Bean（BaseEntity的子类）的getters方法上，可以自动加载关联实体中的数据。
 * 分为如下情况：
 * <ul>
 *     <li>
 *     直接加载关联的Entity Bean
 *         <pre>
 *         class Employee extends BaseEntity {
 *             public Department getDepartment() {
 *                 return this.department;
 *             }
 *         }
 *         </pre>
 *         以上department属性中，如果id不为{@code null}, 则调用DepartmentService#get(id)方法，加载完整的对象。
 *         如果DepartmentService不存在，或者get方法不存在，则根据Department类的JPA配置，调用JdbcTemplate中的相关
 *         方法加载完整的Department对象。
 *         这种方式，是最常用的方式，没有任何侵入性。
 *         如果不需要加载，则使用<code>@LazyField(ignore = true)</code>, 或者JPA的<code>@Transient</code>标注
 *     </li>
 *     
 *     <li>
 *     根据指定的Bean和方法加载：
 *         <pre>
 *         @Entity
 *         @Table(name = "employees")
 *         class Employee extends BaseEntity {
 *         
 *             @LazyField(service = CustomerService.class, method = "byEmployeeId", paramFields = {"id"})
 *             @Transient
 *             public List<Customer> getCustomers() {
 *                 return this.customers;
 *             }
 *             
 *             @LazyField(service = CorpService.class, method = "getNameById", paramFields = {"corpId"})
 *             @Transient
 *             public String getCorpName() {
 *                 return this.corpName;
 *             }
 *         }
 *         </pre>
 *         上述代码中
 *         <ol>
 *             <li>
 *                 getCustomers()上的标注指出，调用<code>CustomerService#byEmplayeeId(Long)</code>加载customers；
 *             </li>
 *             <li>
 *                  getCorpName()标注指出，调用<code>CorpService#getNameById(Long)</code>加载corpName;
 *             </li>
 *             
 *          </ol>
 *     </li>
 *     
 *     
 * </ul>
 * @author samlee
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LazyField {
  /**
   * 指出调用的Bean的class
   */
  Class<?> service() default Object.class;
  
  /**
   * 指出哪些字段值传入到调用的方法中
   */
  String[] paramFields() default {};
  
  /**
   * 指出调用的方法名
   */
  String method() default "";
  
  /**
   * 缓存过期时间，单位为秒，如果为0，则不缓存，如果为-1，则缓存永不过期。
   */
  int cacheSeconds() default 0;
  
  /**
   * 抓取深度
   */
  int fetchSize() default 1;
  
  /**
   * 是否忽略
   */
  boolean ignore() default false;
}
