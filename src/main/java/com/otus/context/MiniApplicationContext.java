package com.otus.context;

import com.otus.annotation.Autowired;
import com.otus.annotation.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class MiniApplicationContext {

    private final Map<Class<?>, Object> beans = new HashMap<>();

    public MiniApplicationContext(Class<?>... componentClasses) {
        for (Class<?> clazz : componentClasses) {
            if (clazz.isAnnotationPresent(Component.class)) {
                try {
                    Object bean = clazz.getDeclaredConstructor().newInstance();
                    beans.put(clazz, bean);
                    inject(bean);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public <T> T getBeans(Class<T> type) {
        return type.cast(findBean(type));
    }

    private Object findBean(Class<?> type) {
        return beans.values().stream()
                .filter(type::isInstance)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No beans found for type: " + type.getName()));
    }

    private void inject(Object bean) {
        for (Field field : bean.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                Object dependency = findBean(field.getType());
                field.setAccessible(true);
                try {
                    field.set(bean, dependency);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
