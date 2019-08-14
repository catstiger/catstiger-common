package com.github.catstiger.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.ReflectionUtils;

public final class ReflectUtil {
  /**
   * Naming prefix for CGLIB-renamed methods.
   * 
   * @see #isCglibRenamedMethod
   */
  private static final String CGLIB_RENAMED_METHOD_PREFIX = "CGLIB$";

  private static final Method[] NO_METHODS = {};

  private static final Field[] NO_FIELDS = {};

  /**
   * Cache for {@link Class#getDeclaredMethods()} plus equivalent default methods from Java 8 based interfaces, allowing for fast iteration.
   */
  private static final Map<Class<?>, Method[]> declaredMethodsCache = new ConcurrentHashMap<Class<?>, Method[]>(256);

  /**
   * Cache for {@link Class#getDeclaredFields()}, allowing for fast iteration.
   */
  private static final Map<Class<?>, Field[]> declaredFieldsCache = new ConcurrentHashMap<Class<?>, Field[]>(256);

  private static final Map<Class<?>, Field[]> fieldsCache = new ConcurrentHashMap<Class<?>, Field[]>(256);

  /**
   * Convenience method to instantiate a class using its no-arg constructor. As this method doesn't try to load classes by name, it should avoid class-loading
   * issues.
   * 
   * @param clazz class to instantiate
   * @return the new instance
   * @throws RuntimeException if the bean cannot be instantiated
   */
  public static <T> T instantiate(Class<T> clazz) {
    if (clazz == null) {
      throw new RuntimeException("Class must not be null");
    }

    if (clazz.isInterface()) {
      throw new RuntimeException("Specified class is an interface [" + clazz.getName() + "]");
    }
    try {
      return clazz.newInstance();
    } catch (InstantiationException ex) {
      throw new RuntimeException("Specified class is an abstract class? [" + clazz.getName() + "]", ex);
    } catch (IllegalAccessException ex) {
      throw new RuntimeException("Is the constructor accessible? [" + clazz.getName() + "]", ex);
    }
  }

  /**
   * Attempt to find a {@link Field field} on the supplied {@link Class} with the supplied {@code name}. Searches all superclasses up to {@link Object}.
   * 
   * @param clazz the class to introspect
   * @param name the name of the field
   * @return the corresponding Field object, or {@code null} if not found
   */
  public static Field findField(Class<?> clazz, String name) {
    return findField(clazz, name, null);
  }
  
  /**
   * Attempt to find a {@link Field field} on the supplied {@link Class} with the supplied {@code name} and a given type of parameter.
   * Searches all superclasses up to {@link Object}.
   * 
   * @param clazz the class to introspect
   * @param name the name of the field
   * @return the corresponding Field object, or {@code null} if not found
   */
  public static Field findField(Class<?> clazz, String name, Class<?> type) {
    if (clazz == null) {
      throw new RuntimeException("Class must not be null");
    }
    if (name == null && type == null) {
      throw new RuntimeException("Either name or type of the field must be specified");
    }

    Class<?> searchType = clazz;
    while (Object.class != searchType && searchType != null) {
      Field[] fields = getDeclaredFields(searchType);
      for (Field field : fields) {
        if ((name == null || name.equals(field.getName())) && (type == null || type.equals(field.getType()))) {
          return field;
        }
      }
      searchType = searchType.getSuperclass();
    }
    return null;
  }

  /**
   * Returns an array of {@code Field} objects reflecting all the fields declared by the class or interface represented by this {@code Class} object. This
   * includes public, protected, default (package) access, and private fields, and includes inherited fields.
   * 
   * @param clazz the supplied {@link Class}
   * @return the LinkedList of {@code Field} objects representing all the declared fields of this class
   */
  public static Field[] getFields(Class<?> clazz) {
    if (clazz == null) {
      throw new RuntimeException("Class must not be null");
    }

    if (fieldsCache.containsKey(clazz)) {
      return fieldsCache.get(clazz);
    }

    List<Field> fieldList = fields(clazz);
    Field[] fields = fieldList.toArray(new Field[] {});

    fieldsCache.put(clazz, fields);
    return fields;
  }

  /**
   * Set the field represented by the supplied {@link Field field object} on the specified {@link Object target object} to the specified {@code value}. In
   * accordance with {@link Field#set(Object, Object)} semantics, the new value is automatically unwrapped if the underlying field has a primitive type.
   * Thrown exceptions are handled via a call to {@link #handleReflectionException(Exception)}.
   * 
   * @param field the field to set
   * @param target the target object on which to set the field
   * @param value the value to set (may be {@code null})
   */
  public static void setField(Field field, Object target, Object value) {
    try {
      field.setAccessible(true);
      field.set(target, value);
    } catch (IllegalAccessException ex) {
      throw new IllegalStateException("Unexpected reflection exception - " + ex.getClass().getName() + ": " + ex.getMessage());
    }
  }

  /**
   * Get the field represented by the supplied {@link Field field object} on the specified {@link Object target object}. In accordance with
   * {@link Field#get(Object)} semantics, the returned value is automatically wrapped if the underlying field has a primitive type.

   * Thrown exceptions are handled via a call to {@link #handleReflectionException(Exception)}.
   * 
   * @param field the field to get
   * @param target the target object from which to get the field
   * @return the field's current value
   */
  public static Object getField(Field field, Object target) {
    try {
      ReflectionUtils.makeAccessible(field);
      return field.get(target);
    } catch (IllegalAccessException ex) {
      throw new IllegalStateException("Unexpected reflection exception - " + ex.getClass().getName() + ": " + ex.getMessage());
    }
  }

  /**
   * Attempt to find a {@link Method} on the supplied class with the supplied name and no parameters. Searches all superclasses up to {@code Object}.
   * Returns {@code null} if no {@link Method} can be found.
   * 
   * @param clazz the class to introspect
   * @param name the name of the method
   * @return the Method object, or {@code null} if none found
   */
  public static Method findMethod(Class<?> clazz, String name) {
    return findMethod(clazz, name, new Class<?>[0]);
  }

  /**
   * Attempt to find a {@link Method} on the supplied class with the supplied name and parameter types. Searches all superclasses up to {@code Object}.
   * Returns {@code null} if no {@link Method} can be found.
   * 
   * @param clazz the class to introspect
   * @param name the name of the method
   * @param paramTypes the parameter types of the method (may be {@code null} to indicate any signature)
   * @return the Method object, or {@code null} if none found
   */
  public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
    if (clazz == null) {
      throw new RuntimeException("Class must not be null");
    }
    if (name == null) {
      throw new RuntimeException("Method name must not be null");
    }
    Class<?> searchType = clazz;
    while (searchType != null) {
      Method[] methods = (searchType.isInterface() ? searchType.getMethods() : getDeclaredMethods(searchType));
      for (Method method : methods) {
        if (name.equals(method.getName()) && (paramTypes == null || Arrays.equals(paramTypes, method.getParameterTypes()))) {
          return method;
        }
      }
      searchType = searchType.getSuperclass();
    }
    return null;
  }

  /**
   * Invoke the specified {@link Method} against the supplied target object with no arguments. The target object can be {@code null} when invoking a static
   * {@link Method}.
   * Thrown exceptions are handled via a call to {@link #handleReflectionException}.
   * 
   * @param method the method to invoke
   * @param target the target object to invoke the method on
   * @return the invocation result, if any
   * @see #invokeMethod(java.lang.reflect.Method, Object, Object[])
   */
  public static Object invokeMethod(Method method, Object target) {
    return invokeMethod(method, target, new Object[0]);
  }

  /**
   * Invoke the specified {@link Method} against the supplied target object with the supplied arguments. The target object can be {@code null} when invoking a
   * static {@link Method}.
   * Thrown exceptions are handled via a call to {@link #handleReflectionException}.
   * 
   * @param method the method to invoke
   * @param target the target object to invoke the method on
   * @param args the invocation arguments (may be {@code null})
   * @return the invocation result, if any
   */
  public static Object invokeMethod(Method method, Object target, Object... args) {
    try {
      return method.invoke(target, args);
    } catch (Exception ex) {
      throw Exceptions.unchecked(ex);
    }
  }

  /**
   * Determine whether the given method is a CGLIB 'renamed' method, following the pattern "CGLIB$methodName$0".
   * 
   * @param renamedMethod the method to check
   * @see org.springframework.cglib.proxy.Enhancer#rename
   */
  public static boolean isCglibRenamedMethod(Method renamedMethod) {
    String name = renamedMethod.getName();
    if (name.startsWith(CGLIB_RENAMED_METHOD_PREFIX)) {
      int i = name.length() - 1;
      while (i >= 0 && Character.isDigit(name.charAt(i))) {
        i--;
      }
      return ((i > CGLIB_RENAMED_METHOD_PREFIX.length()) && (i < name.length() - 1) && name.charAt(i) == '$');
    }
    return false;
  }

  /**
   * 将一个Bean转换为Map
   * 
   * @param bean Object to be mapping.
   * @param fieldnames 需要转换的字段
   * @return
   */
  public static Map<String, Object> toMap(Object bean, String... fieldnames) {
    if (bean == null) {
      return Collections.emptyMap();
    }
    Field[] fields = getFields(bean.getClass());
    if (fields == null || fields.length == 0) {
      return Collections.emptyMap();
    }
    Map<String, Object> map = new HashMap<>(fields.length);

    for (Field field : fields) {
      if (fieldnames != null) {
        if (ArrayUtils.contains(fieldnames, field.getName())) {
          Object value = getField(field, bean);
          map.put(field.getName(), value);
        }
      } else {
        Object value = getField(field, bean);
        map.put(field.getName(), value);
      }
    }

    return map;
  }

  private static LinkedList<Field> fields(Class<?> clazz) {
    if (clazz == null) {
      throw new RuntimeException("Class must not be null");
    }
    LinkedList<Field> fields = new LinkedList<Field>();
    Field[] fs = getDeclaredFields(clazz);
    if (fs != null) {
      for (Field f : fs) {
        fields.add(f);
      }
    }
    Class<?> searchType = clazz.getSuperclass();

    while (searchType != null && Object.class != searchType) {
      fields.addAll(fields(searchType));
      searchType = searchType.getSuperclass();
    }

    return fields;
  }

  private static Field[] getDeclaredFields(Class<?> clazz) {
    Field[] result = declaredFieldsCache.get(clazz);
    if (result == null) {
      result = clazz.getDeclaredFields();
      declaredFieldsCache.put(clazz, (result.length == 0 ? NO_FIELDS : result));
    }
    return result;
  }

  private static Method[] getDeclaredMethods(Class<?> clazz) {
    Method[] result = declaredMethodsCache.get(clazz);
    if (result == null) {
      Method[] declaredMethods = clazz.getDeclaredMethods();
      List<Method> defaultMethods = findConcreteMethodsOnInterfaces(clazz);
      if (defaultMethods != null) {
        result = new Method[declaredMethods.length + defaultMethods.size()];
        System.arraycopy(declaredMethods, 0, result, 0, declaredMethods.length);
        int index = declaredMethods.length;
        for (Method defaultMethod : defaultMethods) {
          result[index] = defaultMethod;
          index++;
        }
      } else {
        result = declaredMethods;
      }
      declaredMethodsCache.put(clazz, (result.length == 0 ? NO_METHODS : result));
    }
    return result;
  }

  private static List<Method> findConcreteMethodsOnInterfaces(Class<?> clazz) {
    List<Method> result = null;
    for (Class<?> ifc : clazz.getInterfaces()) {
      for (Method ifcMethod : ifc.getMethods()) {
        if (!Modifier.isAbstract(ifcMethod.getModifiers())) {
          if (result == null) {
            result = new LinkedList<Method>();
          }
          result.add(ifcMethod);
        }
      }
    }
    return result;
  }

  private ReflectUtil() {

  }
}
