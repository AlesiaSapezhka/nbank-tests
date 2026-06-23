package api.models;
import api.generators.GeneratingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateProfileRequest extends BaseModel{
    @GeneratingRule(regex = "[A-Za-z]{3,10} [A-Za-z]{3,15}")
    private String name;
}
