package api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateDepositResponse extends BaseModel{
    private int id;
    private String accountNumber;
    private double balance;
    private List<GetTransactionsResponse> transactions;
}
