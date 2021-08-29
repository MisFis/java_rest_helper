package com.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import sam.misfis.core.criteria.query.ResponseDTOImpl;
import sam.misfis.core.dto.WrapperResponse;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class ResponseDTOImplTest {

    @SneakyThrows
    @Test
    public void testDeepValue() {
        TreeModel parent = new TreeModel("parent");
        parent.addChildren(new TreeModel("children 1"));
        parent.addChildren(new TreeModel("children 2"));

        TreeModel children3 = new TreeModel("children 3");
        children3.addChildren(new TreeModel("children 3 1"));

        TreeModelOptions optionsChild = new TreeModelOptions("VALID_CHILD", "ERROR_CHILD");
        optionsChild.addNewAttr("test", "test1");
        optionsChild.addNewAttr("test1", new TreeModel("option tree"));
        children3.setOptions(optionsChild);

        parent.addChildren(children3);

        TreeModelOptions treeModelOptions = new TreeModelOptions("VALID", "ERROR");
        treeModelOptions.addNewAttr("test", "test");
        treeModelOptions.addNewAttr("test1", new TreeModel("option tree"));

        parent.setOptions(treeModelOptions);

        ResponseDTOImpl<TreeModel> response = new ResponseDTOImpl();
        response.setInclude("default, children, options");
        //response.setInclude("options.setting");
        // response.setInclude("options");

        WrapperResponse<TreeModel> result = response.toDTO(parent);

        ObjectMapper mapper = new ObjectMapper();
        log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

        var rightResult = "{\"children\":[{\"name\":\"children 1\"},{\"name\":\"children 2\"},{\"children\":[{\"name\":\"children 3 1\"}],\"name\":\"children 3\",\"options\":{\"security\":\"ERROR_CHILD\",\"attr\":{\"test\":\"test1\",\"test1\":{\"name\":\"option tree\",\"parent\":null,\"children\":null,\"options\":null}},\"setting\":\"VALID_CHILD\"}}],\"name\":\"parent\",\"options\":{\"security\":\"ERROR\",\"attr\":{\"test\":\"test\",\"test1\":{\"name\":\"option tree\",\"parent\":null,\"children\":null,\"options\":null}},\"setting\":\"VALID\"}}";

        assertEquals(mapper.writeValueAsString(result), rightResult);
    }

    @Test
    public void testBigDecimal() throws JsonProcessingException {
        BigDecimalWrapper wrapper = new BigDecimalWrapper(UUID.fromString("bc65b452-1f4e-4d72-9cce-98a3ad3ba994"), new BigDecimal(100), false);

        ResponseDTOImpl<BigDecimalWrapper> response = new ResponseDTOImpl();
        response.setInclude("default, value, aBoolean");

        WrapperResponse<BigDecimalWrapper> result = response.toDTO(wrapper);

        ObjectMapper mapper = new ObjectMapper();
        log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

        var rightResult = "{\"aBoolean\":false,\"uuid\":\"bc65b452-1f4e-4d72-9cce-98a3ad3ba994\",\"value\":100}";

        assertEquals(mapper.writeValueAsString(result), rightResult);
    }

    @Test
    public void testEnum() throws JsonProcessingException {
        WrapperEnum wrapper = new WrapperEnum();
        wrapper.setTestEnum(TestEnum.VALUE1);
        ResponseDTOImpl<WrapperEnum> response = new ResponseDTOImpl();
        response.setInclude("testEnum");

        WrapperResponse<WrapperEnum> result = response.toDTO(wrapper);

        ObjectMapper mapper = new ObjectMapper();
        log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

        var rightResult = "{\"testEnum\":\"VALUE1\"}";

        assertEquals(mapper.writeValueAsString(result), rightResult);
    }


    @Test
    public void testObjectKeyNull() throws JsonProcessingException {
        TreeModel parent = new TreeModel("parent");

        ResponseDTOImpl<TreeModel> response = new ResponseDTOImpl();
        response.setInclude("default, children.uuid");

        WrapperResponse<TreeModel> result = response.toDTO(parent);

        ObjectMapper mapper = new ObjectMapper();
        log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

        var rightResult = "{\"name\":\"parent\"}";

        assertEquals(mapper.writeValueAsString(result), rightResult);
    }

    @Test
    public void testDeepInclude() throws JsonProcessingException {
        TreeModel parent = new TreeModel("parent");
        TreeModel children3 = new TreeModel("children 1");
        children3.addChildren(new TreeModel("children 1 1"));
        parent.addChildren(parent);

        ResponseDTOImpl<TreeModel> response = new ResponseDTOImpl();
        response.setInclude("children.children.name");

        WrapperResponse<TreeModel> result = response.toDTO(parent);

        ObjectMapper mapper = new ObjectMapper();
        log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

        var rightResult = "{\"name\":\"parent\"}";

        assertEquals(mapper.writeValueAsString(result), rightResult);
    }

    @Data
    public class WrapperEnum {
        private TestEnum testEnum;
    }

    public enum TestEnum {
        VALUE1,
        VALUE2
        ;
    }


    @Data
    @AllArgsConstructor
    public class BigDecimalWrapper {
        private UUID uuid;
        private BigDecimal value;
        private boolean aBoolean;
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    public class TreeModelOptions {
        private String setting;
        private String security;

        private Map<String, Object> attr = new HashMap<>();

        public TreeModelOptions(String setting, String security) {
            this.setting = setting;
            this.security = security;
        }

        public void addNewAttr(String key, Object value) {
            attr.put(key, value);
        }
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    public class TreeModel {
        private String name;
        private TreeModel parent;
        private List<TreeModel> children;

        private TreeModelOptions options;

        public TreeModel(String name, TreeModel parent, List<TreeModel> children) {
            this.name = name;
            this.parent = parent;
            this.children = children;
        }

        public TreeModel(String name, TreeModel parent) {
            this.name = name;
            this.parent = parent;
        }

        public TreeModel(String name, List<TreeModel> children) {
            this.name = name;
            this.children = children;
        }

        public TreeModel(String name) {
            this.name = name;
        }

        public void addChildren (TreeModel model) {
            if (children == null) children = new ArrayList<>();
            model.parent = this;
            children.add(model);
        }

        @Override
        public String toString() {
            return "TreeModel{" +
                    "name='" + name + '\'' +
                    ", parent=" + children +
                    '}';
        }
    }

}
