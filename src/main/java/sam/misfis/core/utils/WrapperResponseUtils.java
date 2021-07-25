package sam.misfis.core.utils;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.proxy.HibernateProxy;
import org.reflections.ReflectionUtils;
import sam.misfis.core.utils.exception.NoFieldFoundException;

import javax.persistence.Entity;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.reflections.ReflectionUtils.withModifier;
import static org.reflections.ReflectionUtils.withPrefix;

@Slf4j
@UtilityClass
public class WrapperResponseUtils {
    private static final Map<Class<?>, Map<String, Field>> cache = new ConcurrentHashMap<>();

    private static final Set<Class<?>> primitiveType = new HashSet<>();

    private static final Set<String> excludeField = new HashSet<>();

    static {
        primitiveType.add(Boolean.class);
        primitiveType.add(Character.class);
        primitiveType.add(Byte.class);
        primitiveType.add(Short.class);
        primitiveType.add(Integer.class);
        primitiveType.add(Long.class);
        primitiveType.add(Float.class);
        primitiveType.add(Double.class);
        primitiveType.add(Void.class);
        primitiveType.add(String.class);
        primitiveType.add(LocalDateTime.class);
        primitiveType.add(LocalDate.class);
        primitiveType.add(UUID.class);

        excludeField.add("createdWhen");
        excludeField.add("createdBy");
        excludeField.add("updatedBy");
        excludeField.add("updatedWhen");
    }

    public static Map<String, Object> toMap(String[] props, Object target) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Field> fieldInTarget = getFieldInTarget(target);

        var properties = createKeyValue(props);

        for (var prop : properties) {
            String root = prop.getRoot();
            if (root != null) {
                Object value = getValue(root, target);
                if (value.getClass().getAnnotation(Entity.class) != null) {
                    value = initializeAndUnproxy(value);
                }
                Map<String, Field> fieldInChildrenTarget = getFieldInTarget(value);
                Map<String, Object> result = new HashMap<>();
                addPropertiesToResult(result, prop, fieldInChildrenTarget, value);
                map.put(root, result);
            } else {
                addPropertiesToResult(map, prop, fieldInTarget, target);
            }
        }
        return map;
    }

    private static void addPropertiesToResult(Map<String, Object> map, WrapperResponseUtils.KeyValue prop, Map<String, Field> fieldInTarget, Object target) {
        for (String key : prop.getProperties()) {
            var declaredField = fieldInTarget.get(key);
            addProperty(map, key, declaredField, fieldInTarget, target);
        }
    }

    private static void addProperty(Map<String, Object> map, String key, Field declaredField, Map<String, Field> fieldInTarget, Object target) {
        if (declaredField == null)
            throw new NoFieldFoundException(String.format("field %s not found in object", key), fieldInTarget.keySet());

        Object value = getValue(key, target);
        if (value == null) return;
        if (primitiveType.contains(value.getClass())) {
            addPrimitiveValue(map, key, value);
            return;
        }

        if (value instanceof Collection) {
            map.put(key, addArrayValue(key, target));
            return;
        }

        if (value instanceof Map) {
            map.put(key, addMapValue(key, target));
            return;
        }

        var object = addObjectValue(key, target);
        if (object != null) {
            map.put(key, object);
        }
    }

    private static void addPrimitiveValue(Map<String, Object> map, String key, Object value) {
        map.put(key, value);
    }

    @SneakyThrows
    private static Map<String, Object> addObjectValue(String key, Object target) {
        Object value = getValue(key, target);
        if (value == null) return null;
        if (target.getClass().getAnnotation(Entity.class) != null) {
            value = initializeAndUnproxy(value);
        }

        Map<String, Field> fieldInChildrenTarget = getFieldInTarget(value);
        var objField = new HashMap<String, Object>();
        for (var prop : fieldInChildrenTarget.entrySet()) {
            Object propValue = getValue(prop.getKey(), value);
            if (propValue != null && !value.equals(propValue))
                addProperty(objField, prop.getKey(), fieldInChildrenTarget.get(prop.getKey()), fieldInChildrenTarget, value);
        }
        if (objField.isEmpty()) return null;
        return objField;
    }

    @SneakyThrows
    private static List<?> addArrayValue(String key, Object target) {
        Object value = getValue(key, target);
        if (value == null) return Collections.emptyList();
        var array = new ArrayList<>();
        ((Collection<?>) value)
                .forEach(o -> {
                    Map<String, Field> fieldInChildrenTarget = getFieldInTarget(o);
                    var objField = new HashMap<String, Object>();
                    for (var prop : fieldInChildrenTarget.entrySet()) {
                        String keyField = prop.getKey();
                        Object o1 = getValue(keyField, o);
                        if (!target.equals(o1))
                            addProperty(objField, keyField, fieldInChildrenTarget.get(keyField), fieldInChildrenTarget, o);
                    }
                    array.add(objField);
                });
        return array;
    }

    private static Map<String, Object> addMapValue(String key, Object target) {
        // TODO Дать возможность из мап выбирать определенные свойства
        Map<String, Object> map = (Map<String, Object>) getValue(key, target);
        return map;
    }

    @SneakyThrows
    private static Object getValue(String fieldName, Object target) {
        var get = ReflectionUtils.getAllMethods(target.getClass(),
                withModifier(Modifier.PUBLIC), withPrefix("get" + String.format("%s%s", String.valueOf(fieldName.charAt(0)).toUpperCase(Locale.ROOT), fieldName.substring(1))));
        if (get.isEmpty()) return null;
        return get.iterator().next().invoke(target);
    }

    private static List<WrapperResponseUtils.KeyValue> createKeyValue(String[] props) {
        List<WrapperResponseUtils.KeyValue> properties = new ArrayList<>();
        for (String prop : props) {
            if (prop != null && !prop.equals("")) {
                if (prop.contains(".")) {
                    String[] split = prop.split("\\.", 2);
                    Optional<WrapperResponseUtils.KeyValue> find = properties.stream()
                            .filter(keyValue -> split[0].equals(keyValue.getRoot()))
                            .findFirst();
                    if (find.isPresent()) {
                        find.get().addProperty(split[1]);
                    } else {
                        var keyValue = new WrapperResponseUtils.KeyValue();
                        keyValue.setRoot(split[0]);
                        keyValue.addProperty(split[1]);
                        properties.add(keyValue);
                    }
                } else {
                    var keyValue = new WrapperResponseUtils.KeyValue();
                    keyValue.setRoot(null);
                    keyValue.addProperty(prop);
                    properties.add(keyValue);
                }
            }
        }
        return properties;
    }


    public static Set<String> getAllPrimitive(Object target) {
        Map<String, Field> fieldInTarget = getFieldInTarget(target);
        return fieldInTarget
                .keySet()
                .stream()
                .filter(s -> {
                    var field = fieldInTarget.get(s);
                    return !excludeField.contains(s) && primitiveType.contains(field.getType());
                }).collect(Collectors.toSet());
    }

    public static Set<String> getByKey(Class<?> clazz) {
        var fieldInTarget = getFieldInTarget(clazz);
        return fieldInTarget
                .keySet()
                .stream()
                .filter(s -> {
                    var field = fieldInTarget.get(s);
                    return !excludeField.contains(s) && primitiveType.contains(field.getType());
                }).collect(Collectors.toSet());
    }

    public static Map<String, Field> getFieldInTarget(Object target) {
        if (cache.containsKey(target.getClass())) return cache.get(target.getClass());
        Map<String, Field> fields = new HashMap<>();
        for (Class<?> cls = target.getClass();
             cls != null;
             cls = cls.getSuperclass()) {
            for (Field field : cls.getDeclaredFields()) {
                fields.put(field.getName(), field);
            }
        }
        cache.put(target.getClass(), fields);
        return fields;
    }

    public static Map<String, Field> getFieldInTarget(Class<?> clazz) {
        if (cache.containsKey(clazz)) return cache.get(clazz);
        Map<String, Field> fields = new HashMap<>();
        for (Class<?> cls = clazz;
             cls != null;
             cls = cls.getSuperclass()) {
            for (Field field : cls.getDeclaredFields()) {
                fields.put(field.getName(), field);
            }
        }
        cache.put(clazz, fields);
        return fields;
    }


    private static <T> Object initializeAndUnproxy(T entity) {
        if (entity == null) {
            return null;
        }
        if (entity instanceof HibernateProxy) {
            return Hibernate.unproxy(entity);
        }
        if (entity instanceof PersistentBag) {
            Hibernate.initialize(entity);
        }
        return entity;
    }

    @Data
    private static class KeyValue {
        private String root;

        private Set<String> properties = new HashSet<>();

        public void addProperty(String property) {
            properties.add(property);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            var keyValue = (WrapperResponseUtils.KeyValue) o;
            return Objects.equals(root, keyValue.root);
        }

        @Override
        public int hashCode() {
            return Objects.hash(root);
        }
    }
}
