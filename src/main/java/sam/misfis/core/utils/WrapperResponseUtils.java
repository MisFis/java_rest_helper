package sam.misfis.core.utils;

import lombok.Data;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.proxy.HibernateProxy;
import sam.misfis.core.utils.exception.NoFieldFoundException;

import javax.persistence.Entity;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

        List<KeyValue> properties = createKeyValue(props);

        for (KeyValue prop : properties) {
            String root = prop.getRoot();
            if (root != null) {
                try {
                    Field field = fieldInTarget.get(root);
                    if (!Modifier.isPublic(field.getModifiers())) {
                        field.setAccessible(true);
                    }
                    Object obj = field.get(target);
                    if (obj == null) return map;
                    if (field.getType().getAnnotation(Entity.class) != null) {
                        obj = initializeAndUnproxy(obj);
                    }
                    if (map.containsKey(root)) {
                        Object o = map.get(root);
                        Map<String, Field> fieldsRoot = getFieldInTarget(o);
                        prop.getProperties().addAll(fieldsRoot.keySet());
                    }
                    map.put(root, toMap(prop.getProperties().toArray(new String[0]), obj));
                } catch (IllegalAccessException e) {
                    log.error(e.getMessage(), e);
                }
            } else {
                addPropertiesToMap(map, prop, fieldInTarget, target);
            }

        }
        return map;
    }

    private static void addPropertiesToMap(Map<String, Object> map, KeyValue prop, Map<String, Field> fieldInTarget, Object target) {
        for (String key : prop.getProperties()) {
            Field declaredField = fieldInTarget.get(key);
            if (declaredField == null)
                throw new NoFieldFoundException(String.format("field %s not found in object", key), fieldInTarget.keySet());
            if (!Modifier.isPublic(declaredField.getModifiers())) {
                declaredField.setAccessible(true);
            }
            try {
                Object obj = declaredField.get(target);
                if (target.getClass().getAnnotation(Entity.class) != null) {
                    obj = initializeAndUnproxy(obj);
                }
                map.put(key, obj);
            } catch (IllegalAccessException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private static List<KeyValue> createKeyValue(String[] props) {
        List<KeyValue> properties = new ArrayList<>();
        for (String prop : props) {
            if (prop != null && !prop.equals("")) {
                if (prop.contains(".")) {
                    String[] split = prop.split("\\.", 2);
                    Optional<KeyValue> find = properties.stream()
                            .filter(keyValue -> split[0].equals(keyValue.getRoot()))
                            .findFirst();
                    if (find.isPresent()) {
                        find.get().addProperty(split[1]);
                    } else {
                        KeyValue keyValue = new KeyValue();
                        keyValue.setRoot(split[0]);
                        keyValue.addProperty(split[1]);
                        properties.add(keyValue);
                    }
                } else {
                    KeyValue keyValue = new KeyValue();
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
                    Field field = fieldInTarget.get(s);
                    return !excludeField.contains(s) && primitiveType.contains(field.getType());
                }).collect(Collectors.toSet());
    }

    public static Set<String> getByKey(String  key, Class clazz) {
        Map<String, Field> fieldInTarget = getFieldInTarget(clazz);
        return fieldInTarget
                .keySet()
                .stream()
                .filter(s -> {
                    Field field = fieldInTarget.get(s);
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

    public static Map<String, Field> getFieldInTarget(Class clazz) {
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
            KeyValue keyValue = (KeyValue) o;
            return Objects.equals(root, keyValue.root);
        }

        @Override
        public int hashCode() {
            return Objects.hash(root);
        }
    }
}
