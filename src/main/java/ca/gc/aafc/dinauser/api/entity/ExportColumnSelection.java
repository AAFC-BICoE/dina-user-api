package ca.gc.aafc.dinauser.api.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportColumnSelection {

  private String name;
  private String module;
  private List<String> columns;

}
