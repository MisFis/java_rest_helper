package com.test;

import lombok.Data;
import org.junit.jupiter.api.Test;
import sam.misfis.core.criteria.SearchSpecification;
import sam.misfis.core.criteria.query.SpecQueryImpl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpecQueryTest {
    @Test
    void t1() {
        SpecQueryImpl<ModelTest1> specQuery = new SpecQueryImpl("publishDate>" +
                LocalDateTime.of(2020, 6, 15, 15, 0)
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME
                        ));
        SearchSpecification<ModelTest1> val = (SearchSpecification<ModelTest1>) specQuery.toSpec(ModelTest1.class);
        assertEquals("2020-06-15T15:00:00", val.getCriteria().getValue());
    }

    @Test
    void t_() {
        SpecQueryImpl<DeepModel> specQuery = new SpecQueryImpl("children.parent.uuid=" + UUID.randomUUID());
        SearchSpecification<DeepModel> val = (SearchSpecification<DeepModel>) specQuery.toSpec(DeepModel.class);
        assertEquals("2020-06-15T15:00:00", val.getCriteria().getValue());
    }

    @Test
    void array () {
        SpecQueryImpl<ModelTest1> specQuery = new SpecQueryImpl("strings*1,2,3,4,5");
        SearchSpecification<ModelTest1> val = (SearchSpecification<ModelTest1>) specQuery.toSpec(ModelTest1.class);
        // assertEquals("1,2,3,4,5", val.toArray(val.getCriteria().getType()));
    }
}

@Data
class ModelTest1 {
    private LocalDateTime publishDate;
    private List<String> strings;
}

@Data
class DeepModel {
    String uuid;
    private DeepChildrenModel children;
}

@Data
class DeepChildrenModel {
    String uuid;
    private List<String> strings;
    private DeepChildrenWithParentModel parent;
}
@Data
class DeepChildrenWithParentModel {
    String uuid;
    private List<String> strings;
}
