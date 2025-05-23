package sanqibackend.sanqibackend.entity;

import lombok.Data;

@Data
public class SanqiRegion {
  private Long gid;
  private String name;
  private String geom;
}