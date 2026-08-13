package api.dao;

import api.models.TransactionsTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionsDao {
    private int id;
    private double amount;
    private TransactionsTypes type;
    private int relatedAccountId;
}