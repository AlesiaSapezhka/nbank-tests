package models;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class CreateTransferResponse {
    private int senderAccountId;
    private int receiverAccountId;
    private double amount;
    private String message;
}
