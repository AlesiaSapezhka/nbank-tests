package api.models;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class CreateTransferRequest extends BaseModel{
    private int senderAccountId;
    private int receiverAccountId;
    public double amount;
}

