package sanqibackend.sanqibackend.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import sanqibackend.sanqibackend.repository.RasterDataRepository;
import sanqibackend.sanqibackend.entity.RasterData;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class RasterThumbnailService {

    @Value("${python.script.path}")
    private String pythonScriptPath;

    @Value("${python.executable}")
    private String pythonExecutable;

    @Autowired
    private RasterDataRepository rasterDataRepository;

    @Autowired
    private RasterDataService rasterDataService;

    public boolean generateThumbnail(String inputPath, String outputPath) {
        try {
            // 构建Python命令
            List<String> command = new ArrayList<>();
            command.add(pythonExecutable);
            command.add(pythonScriptPath);
            command.add(inputPath);
            command.add(outputPath);

            // 创建进程构建器
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            // 启动进程
            Process process = processBuilder.start();

            // 读取进程输出
            StringBuilder output = new StringBuilder();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // 等待进程完成
            int exitCode = process.waitFor();

            // 检查输出目录是否存在
            File outputFile = new File(outputPath);
            if (exitCode != 0 || !outputFile.exists()) {
                System.err.println("Python script execution failed:");
                System.err.println("Command: " + String.join(" ", command));
                System.err.println("Exit code: " + exitCode);
                System.err.println("Output: " + output.toString());
                return false;
            }

            return true;

        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing Python script: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 新增方法：根据id生成缩略图并保存到数据库
    public void generateAndSaveThumbnail(Long id) throws Exception {
        RasterData rasterData = rasterDataRepository.findById(id)
            .orElseThrow(() -> new Exception("未找到对应的影像数据"));

        // 检查原始数据是否存在
        if (rasterData.getLoOid() == null) {
            throw new Exception("影像数据不完整，缺少原始文件");
        }

        String tempDir = System.getProperty("java.io.tmpdir");
        String inputPath = tempDir + File.separator + "raster_" + id + ".tif";
        String outputPath = inputPath + ".thumbnail.jpg";

        try {
            // 导出原始tif文件到临时路径
            System.out.println("正在导出原始文件到: " + inputPath);
            rasterDataService.exportRaster(id, inputPath);

            // 检查导出的文件
            File inputFile = new File(inputPath);
            if (!inputFile.exists()) {
                throw new Exception("导出原始文件失败：文件不存在");
            }
            if (inputFile.length() == 0) {
                throw new Exception("导出原始文件失败：文件为空");
            }

            System.out.println("开始生成缩略图...");
            boolean success = generateThumbnail(inputPath, outputPath);
            if (!success) {
                throw new Exception("Python脚本生成缩略图失败，请检查日志获取详细信息");
            }

            // 检查生成的缩略图
            File outputFile = new File(outputPath);
            if (!outputFile.exists() || outputFile.length() == 0) {
                throw new Exception("缩略图生成失败：输出文件无效");
            }

            // 读取生成的缩略图文件为字节数组
            System.out.println("正在保存缩略图到数据库...");
            byte[] thumbnailBytes = Files.readAllBytes(Paths.get(outputPath));
            rasterData.setThumbnail(thumbnailBytes);
            rasterDataRepository.save(rasterData);
            System.out.println("缩略图保存成功");

        } catch (Exception e) {
            System.err.println("生成缩略图过程中发生错误: " + e.getMessage());
            throw e;
        } finally {
            // 清理临时文件
            try {
                new File(inputPath).delete();
                new File(outputPath).delete();
            } catch (Exception e) {
                System.err.println("清理临时文件时发生错误: " + e.getMessage());
            }
        }
    }
} 
