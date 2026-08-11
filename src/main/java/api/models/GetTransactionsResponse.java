package api.models;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetTransactionsResponse extends BaseModel {
    private int id;
    private double amount;
    private TransactionsTypes type;
    private int relatedAccountId;
}
