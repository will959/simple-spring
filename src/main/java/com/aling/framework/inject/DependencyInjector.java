package com.aling.framework.inject;

import com.aling.framework.core.BeanContainer;
import com.aling.framework.inject.annotation.Autowired;
import com.aling.util.ClassUtil;
import com.aling.util.UtilCollection;
import com.aling.util.UtilString;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * 实现依赖注入
 */
@Slf4j
public class DependencyInjector {
    /**
     * Bean容器
     */
    private BeanContainer beanContainer;

    public DependencyInjector() {
        beanContainer = BeanContainer.getInstance();
    }

    /**
     * 执行Ioc 依赖注入
     */
    public void doIoc() {
        if (UtilCollection.isEmpty(beanContainer.getClasses())) {
            log.warn("容器中的class对象为空");
            return;
        }
        //1.遍历Bean容器中所有的Class对象
        for (Class<?> clazz : beanContainer.getClasses()) {
            //2.遍历Class对象的所有成员变量
            Field[] fields = clazz.getDeclaredFields();
            if (UtilCollection.isEmpty(fields)) {
                continue;
            }
            for (Field field : fields) {
                //3.找出被Autowired标记的成员变量
                if (field.isAnnotationPresent(Autowired.class)) {
                    Autowired autowired = field.getAnnotation(Autowired.class);
                    String autowiredValue = autowired.value();
                    //4.获取这些成员变量的类型
                    Class<?> fieldClass = field.getType();
                    //5.获取这些成员变量的类型在容器里对应的实例
                    Object fieldValue = getFieldInstance(fieldClass, autowiredValue);
                    if (fieldValue == null) {
                        throw new RuntimeException("无法从容器中获得对应类型的类，注入失败，target fieldClass is:" + fieldClass.getName() + " autowiredValue is : " + autowiredValue);
                    } else {
                        //6.通过反射将对应的成员变量实例注入到成员变量所在类的实例里
                        Object targetBean = beanContainer.getBean(clazz);
                        ClassUtil.setField(field, targetBean, fieldValue, true);
                    }
                }
            }
        }


    }

    /**
     * 根据Class在beanContainer里获取其实例或者实现类
     */
    private Object getFieldInstance(Class<?> fieldClass, String autowiredValue) {
        Object fieldValue = beanContainer.getBean(fieldClass);
        //若直接找到的是spring管理的类就返回
        if (fieldValue != null) {
            return fieldValue;
        }
        //则找出其实现类，然后返回对应的bean
        Class<?> implementedClass = getImplementedClass(fieldClass, autowiredValue);
        if (implementedClass == null) {
            return null;
        }
        return beanContainer.getBean(implementedClass);
    }

    /**
     * 获取接口的实现类
     */
    private Class<?> getImplementedClass(Class<?> fieldClass, String autowiredValue) {
        Set<Class<?>> classSet = beanContainer.getClassesBySuper(fieldClass);
        if (UtilCollection.isEmpty(classSet)) {
            return null;
        }
        if (UtilString.isEmpty(autowiredValue)) {
            if (classSet.size() == 1) {
                return classSet.iterator().next();
            } else {
                //如果多于两个实现类且用户未指定其中一个实现类，则抛出异常
                throw new RuntimeException("根据成员变量的类型，得到多个实现类：" + fieldClass.getName() + " 请指定一个实例进行注入 ，在autowired时加入value");
            }
        }
        for (Class<?> clazz : classSet) {
            if (autowiredValue.equals(clazz.getSimpleName())) {
                return clazz;
            }
        }
        return null;
    }
}
