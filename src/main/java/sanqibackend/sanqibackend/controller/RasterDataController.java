package sanqibackend.sanqibackend.controller;

import sanqibackend.sanqibackend.entity.RasterData;
import sanqibackend.sanqibackend.service.RasterDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/raster")
public class RasterDataController {
    private static final Logger log = LoggerFactory.getLogger(RasterDataController.class);

    @Autowired
    private RasterDataService rasterDataService;

    @PostMapping("/import")
    public ResponseEntity<?> importRaster(
            @RequestParam("files") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "directory", required = false) String directory) {
        try {
            log.info("开始导入栅格数据: {}", file.getOriginalFilename());
            RasterData rasterData = rasterDataService.importRaster(file, description, directory);
            log.info("栅格数据导入成功, ID: {}", rasterData.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "栅格数据导入成功");
            response.put("id", rasterData.getId());
            response.put("name", rasterData.getName());
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            log.error("栅格数据导入失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "栅格数据导入失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/export/{id}")
    public ResponseEntity<?> exportRaster(
            @PathVariable Long id,
            @RequestParam("outputPath") String outputPath) {
        try {
            log.info("开始导出栅格数据, ID: {}, 输出路径: {}", id, outputPath);
            RasterData rasterData = rasterDataService.getRasterDataById(id);
            String actualPath = rasterDataService.exportRaster(id, outputPath);
            log.info("栅格数据导出成功, 实际路径: {}", actualPath);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "栅格数据导出成功");
            response.put("path", actualPath);
            response.put("dataName", rasterData.getName());
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            log.error("栅格数据导出失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "栅格数据导出失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/exportDirectories")
    public ResponseEntity<?> getExportDirectories() {
        try {
            log.info("获取可用的导出目录列表");
            List<String> directories = List.of(
                "F:/export",
                    "/temp/raster_data/export",
                "D:/export",
                "C:/Users/Public/Documents"
            );
            for (String dir : directories) {
                try {
                    Path path = Paths.get(dir);
                    if (!Files.exists(path)) {
                        Files.createDirectories(path);
                        log.info("创建导出目录: {}", path);
                    }
                } catch (Exception e) {
                    log.warn("创建目录失败: {}, 错误: {}", dir, e.getMessage());
                }
            }
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("directories", directories);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            log.error("获取导出目录列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "获取导出目录列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/list")
    public ResponseEntity<?> listRasterData() {
        try {
            log.info("获取栅格数据列表");
            List<RasterData> rasterDataList = (List<RasterData>) rasterDataService.getAllRasterData();
            log.info("成功获取栅格数据列表, 共 {} 条记录", rasterDataList.size());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", rasterDataList);
            response.put("count", rasterDataList.size());
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            log.error("获取栅格数据列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "获取栅格数据列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getRasterData(@PathVariable Long id) {
        try {
            log.info("获取栅格数据, ID: {}", id);
            RasterData rasterData = rasterDataService.getRasterDataById(id);
            log.info("成功获取栅格数据");
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", rasterData);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            log.error("获取栅格数据失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "获取栅格数据失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRasterData(@PathVariable Long id) {
        try {
            log.info("删除栅格数据, ID: {}", id);
            rasterDataService.deleteRasterData(id);
            log.info("栅格数据删除成功");
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "栅格数据删除成功");
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            log.error("栅格数据删除失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "栅格数据删除失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 

