package sanqibackend.sanqibackend.entity;

import lombok.Data;
import java.util.List;

@Data
public class GeoJsonResponse {
  private String type = "FeatureCollection";
  private List<GeoJsonFeature> features;
}