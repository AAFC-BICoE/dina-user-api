package ca.gc.aafc.dinauser.api.entity;

import java.util.List;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportColumnSelection {

  @NotEmpty
  @Size(max = 150)
  private String name;

  @NotEmpty
  @Size(max = 50)
  private String module;

  @NotNull
  private List<String> columns;

}
