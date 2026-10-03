# Dependency Injection и IoC в Spring
**Курс:** Java Pro
## Live demo для вебинара
**Написание простой реализации Spring контейнера**
Без сканирования пакетов, AOP, прокси и lifecycle.
**Ограничения:**
- нет `@ComponentScan`
- классы передаем руками
- только field injection
- только singleton
Перед началом удалить все классы и почистить метод `main`
**Порядок проведения демо:**
### 1. Аннотации
Написать два файла
**Runtime здесь нужен, чтобы рефлексия могла прочитать аннотацию во время работы программы**
```
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {}
```
```
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Autowired {}
```
### 2. Контекст
Здесь beans это BeanFactory, где ключ - класс, а значение - объект
```
import java.lang.reflect.Field;
import java.util.*;

public class MiniApplicationContext {
    private final Map<Class<?>, Object> beans = new HashMap<>();

    public MiniApplicationContext(Class<?>... componentClasses) {
        // 1. создаём бины
        // 2. внедряем зависимости
    }

    public <T> T getBean(Class<T> type) {
        return type.cast(findBean(type));
    }

    private Object findBean(Class<?> type) {
        return beans.values().stream()
                .filter(type::isInstance)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No bean of type " + type));
    }
}
```
### 3. Создание бинов
Здесь сначала создаем все бины, затем внедряем зависимости
```
for (Class<?> clazz : componentClasses) {
    if (clazz.isAnnotationPresent(Component.class)) {
        try {
            Object bean = clazz.getDeclaredConstructor().newInstance();
            beans.put(clazz, bean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

for (Object bean : beans.values()) {
    inject(bean);
}
```
### 4. Внедрение зависимостей
Упрощенный аналог `AutowiredAnnotationBeanPostProcessor`. В спринге это происходит с прокси, `@Qualifier`, конструкторами и циклами.
```
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
```
Добавить вызов метода в цикл создания бинов
```
Object bean = clazz.getDeclaredConstructor().newInstance();
beans.put(clazz, bean);
inject(bean);
```
### 5. `getBean`
Если несколько бинов одного типа, мы вернем первый. В Spring здесь были бы `@Primary` и `@Qualifier`
```
public <T> T getBean(Class<T> type) {
    return type.cast(findBean(type));
}
```
### 6. Запуск
`true` - потому что контейнер вернул один и тот же объект, singleton.
```
interface GreetingService {
    String greet(String name);
}

@Component
class GreetingServiceImpl implements GreetingService {
    public String greet(String name) {
        return "Hello, " + name;
    }
}

@Component
class UserService {
    @Autowired
    private GreetingService greetingService;

    public void greet(String name) {
        System.out.println(greetingService.greet(name));
    }
}

public class Main {
    public static void main(String[] args) {
        var context = new MiniApplicationContext(
                GreetingServiceImpl.class,
                UserService.class
        );

        context.getBean(UserService.class).greet("Spring");

        System.out.println(
                context.getBean(GreetingServiceImpl.class)
                        == context.getBean(GreetingServiceImpl.class)
        );
    }
}
```
### Что можно добавить
- `ComponentScan` - для этого необходимо рекурсивно пройтись по пакету и найти все классы с аннотацией `@Component`