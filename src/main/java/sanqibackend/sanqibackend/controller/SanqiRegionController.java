package sanqibackend.sanqibackend.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import sanqibackend.sanqibackend.entity.GeoJsonFeature;
import sanqibackend.sanqibackend.entity.GeoJsonResponse;
import sanqibackend.sanqibackend.service.SanqiRegionService;

@RestController
@RequestMapping("/api/SanqiRegion")
public class SanqiRegionController {
  @Autowired
  private SanqiRegionService sanqiService;

  @GetMapping
  public GeoJsonResponse getAllSanqis() {
    return sanqiService.getAllSanqis();
  }

  @GetMapping("/{id}")
  public GeoJsonFeature getSanqiById(@PathVariable Long id) {
    return sanqiService.getSanqiById(id);
  }
}