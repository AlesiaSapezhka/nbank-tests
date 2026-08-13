package api.models;

import api.generators.GeneratingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateDepositRequest extends BaseModel {
    @GeneratingRule(regex = "^[A-Za-z0-9]{3,15}$")
    private int accountId;
    @GeneratingRule(regex = "^[A-Za-z0-9]{3,15}$")
    private double amount;
}
