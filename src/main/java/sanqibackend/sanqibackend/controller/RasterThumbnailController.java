package sanqibackend.sanqibackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sanqibackend.sanqibackend.entity.RasterData;
import sanqibackend.sanqibackend.repository.RasterDataRepository;
import sanqibackend.sanqibackend.service.RasterThumbnailService;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/raster/thumbnail")
public class RasterThumbnailController {
    @Autowired
    private RasterThumbnailService rasterThumbnailService;
    @Autowired
    private RasterDataRepository rasterDataRepository;

    // 为指定影像生成缩略图
    @PostMapping("/generate/{id}")
    public ResponseEntity<?> generateThumbnail(@PathVariable Long id) {
        try {
            rasterThumbnailService.generateAndSaveThumbnail(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "缩略图生成成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "缩略图生成失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 获取缩略图内容
    @GetMapping("/view/{id}")
    public ResponseEntity<byte[]> viewThumbnail(@PathVariable Long id) {
        RasterData data = rasterDataRepository.findById(id).orElse(null);
        if (data == null || data.getThumbnail() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header("Content-Type", "image/jpeg")
                .body(data.getThumbnail());
    }
}
