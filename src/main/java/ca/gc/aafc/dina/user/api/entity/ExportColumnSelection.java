package ca.gc.aafc.dina.user.api.entity;

import java.util.List;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
  private String component;

  @NotNull
  private List<String> columns;

  private List<String> columnAliases;

}
