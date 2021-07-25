package com.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import sam.misfis.core.criteria.query.ResponseDTOImpl;
import sam.misfis.core.dto.WrapperResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class ResponseDTOImplTest {

    @SneakyThrows
    @Test
    public void t1() {
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

        // WrapperResponseUtils.toMap(new String[]{"name", "children", "options"}, parent);

        WrapperResponse<TreeModel> result = response.toDTO(parent);

        ObjectMapper mapper = new ObjectMapper();
        log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

        var rightResult = "{\"children\":[{\"name\":\"children 1\"},{\"name\":\"children 2\"},{\"children\":[{\"name\":\"children 3 1\"}],\"name\":\"children 3\",\"options\":{\"security\":\"ERROR_CHILD\",\"attr\":{\"test\":\"test1\",\"test1\":{\"name\":\"option tree\",\"parent\":null,\"children\":null,\"options\":null}},\"setting\":\"VALID_CHILD\"}}],\"name\":\"parent\",\"options\":{\"security\":\"ERROR\",\"attr\":{\"test\":\"test\",\"test1\":{\"name\":\"option tree\",\"parent\":null,\"children\":null,\"options\":null}},\"setting\":\"VALID\"}}";

        log.info(mapper.writeValueAsString(result));

        assertEquals(mapper.writeValueAsString(result), rightResult);
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
                    ", parent=" + parent +
                    '}';
        }
    }

}
