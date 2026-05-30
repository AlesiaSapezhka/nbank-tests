package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class CreateTransferRequest extends BaseModel{
    private int senderAccountId;
    private int receiverAccountId;
    public double amount;
}

