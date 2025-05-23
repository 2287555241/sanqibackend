package sanqibackend.sanqibackend.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import sanqibackend.sanqibackend.entity.GeoJsonFeature;
import sanqibackend.sanqibackend.entity.GeoJsonResponse;
import sanqibackend.sanqibackend.entity.SanqiRegion;
import sanqibackend.sanqibackend.mapper.SanqiRegionMapper;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@Service
public class SanqiRegionService {
  private static final Logger logger = LoggerFactory.getLogger(SanqiRegionService.class);

  @Autowired
  private SanqiRegionMapper sanqiMapper;

  private ObjectMapper objectMapper = new ObjectMapper();

  public GeoJsonResponse getAllSanqis() {
    List<SanqiRegion> sanqis = sanqiMapper.findAll();
    GeoJsonResponse response = new GeoJsonResponse();
    response.setFeatures(sanqis.stream()
        .map(this::convertToGeoJsonFeature)
        .collect(Collectors.toList()));
    return response;
  }

  public GeoJsonFeature getSanqiById(Long id) {
    SanqiRegion sanqi = sanqiMapper.findById(id);
    return convertToGeoJsonFeature(sanqi);
  }

  private GeoJsonFeature convertToGeoJsonFeature(SanqiRegion sanqi) {
    GeoJsonFeature feature = new GeoJsonFeature();

    try {
      // 设置属性
      Map<String, Object> properties = new HashMap<>();
      properties.put("gid", sanqi.getGid().longValue());
      properties.put("name", sanqi.getName());

      feature.setProperties(properties);

      // 解析几何数据
      if (sanqi.getGeom() != null) {
        @SuppressWarnings("unchecked")
        Map<String, Object> geometry = objectMapper.readValue(sanqi.getGeom(), Map.class);
        if (geometry == null || !geometry.containsKey("coordinates")) {
          logger.error("Invalid geometry data for region: {}", sanqi.getName());
        }
        feature.setGeometry(geometry);
      } else {
        logger.error("Null geometry data for region: {}", sanqi.getName());
      }
    } catch (Exception e) {
      logger.error("Error converting region data: {} - {}", sanqi.getName(), e.getMessage());
      e.printStackTrace();
    }

    return feature;
  }
}