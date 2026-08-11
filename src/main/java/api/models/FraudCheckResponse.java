package api.models;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FraudCheckResponse extends BaseModel {
    private String transactionId;
    private String status;
    private String message;
    private String fraudCheckStatus;
    private String details;
}