package de.litexo.model.external;

import lombok.Data;

@Data
public class ServiceError {
    ServiceErrorType type;
    String message;
    String stackTrace;
}
