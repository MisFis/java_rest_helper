package com.test;

import lombok.Data;
import org.junit.jupiter.api.Test;
import sam.misfis.core.criteria.SearchSpecification;
import sam.misfis.core.criteria.query.SpecQueryImpl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpecQueryTest {
    @Test
    public void t1() {
        SpecQueryImpl<ModelTest1> specQuery = new SpecQueryImpl("publishDate>" +
                LocalDateTime.of(2020, 6, 15, 15, 0)
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME
                        ));
        SearchSpecification<ModelTest1> val = (SearchSpecification<ModelTest1>) specQuery.toSpec(ModelTest1.class);
        assertEquals("2020-06-15T15:00:00", val.getCriteria().getValue());
    }
}

@Data
class ModelTest1 {
    private LocalDateTime publishDate;
}
