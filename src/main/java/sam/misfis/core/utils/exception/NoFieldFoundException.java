package sam.misfis.core.utils.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.Collection;

@Getter
@JsonIgnoreProperties(value = { "stackTrace", "cause", "suppressed", "localizedMessage" })
public class NoFieldFoundException extends RuntimeException {

    private final String message;

    private final Collection<String> supportFields;


    public NoFieldFoundException(String message, Collection<String> supportFields) {
        super(message);
        this.message = message;
        this.supportFields = supportFields;
    }
}
