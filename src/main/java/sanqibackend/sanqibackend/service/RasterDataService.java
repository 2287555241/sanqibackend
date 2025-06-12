package sanqibackend.sanqibackend.service;

import sanqibackend.sanqibackend.entity.RasterData;
import sanqibackend.sanqibackend.repository.RasterDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.io.OutputStream;
import java.io.FileOutputStream;

@Service
public class RasterDataService {
    
    private static final Logger log = LoggerFactory.getLogger(RasterDataService.class);
    
    @Autowired
    private RasterDataRepository rasterDataRepository;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private DataSource dataSource;
    
    /**
     * 导入栅格数据到数据库
     */
    @Transactional
    public RasterData importRaster(MultipartFile file, String description) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        log.info("开始导入栅格数据: {}", originalFilename);
        Long oid = null;
        Connection conn = null;
        try {
            conn = DataSourceUtils.getConnection(dataSource);
            conn.setAutoCommit(false);
            LargeObjectManager lobj = conn.unwrap(org.postgresql.PGConnection.class).getLargeObjectAPI();
            oid = lobj.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
            LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);
            try (InputStream inputStream = file.getInputStream()) {
                byte[] buf = new byte[4096];
                int s;
                while ((s = inputStream.read(buf, 0, buf.length)) > 0) {
                    obj.write(buf, 0, s);
                }
            }
            obj.close();
            conn.commit();
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception ex) {
                    log.error("回滚失败", ex);
                }
            }
            log.error("导入大对象失败: {}", e.getMessage(), e);
            throw new IOException("导入大对象失败: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    log.error("关闭连接失败", e);
                }
            }
        }
        RasterData rasterData = new RasterData();
        rasterData.setName(originalFilename);
        rasterData.setDescription(description);
        rasterData.setLoOid(oid);
        rasterData.setFileSize(file.getSize());
        RasterData savedRasterData = rasterDataRepository.save(rasterData);
        log.info("栅格数据已成功导入到数据库，ID: {}", savedRasterData.getId());
        return savedRasterData;
    }

    /**
     * 兼容旧代码的重载方法，忽略directory参数
     * @param file 上传的文件
     * @param description 文件描述
     * @param directory 目录参数（已废弃，将被忽略）
     * @return 保存的栅格数据对象
     * @throws IOException 如果导入过程中发生IO错误
     */
    public RasterData importRaster(MultipartFile file, String description, String directory) throws IOException {
        log.info("使用三参数版本的importRaster方法，directory参数将被忽略");
        return importRaster(file, description);
    }
    
    /**
     * 导出栅格数据到指定路径
     */
    public String exportRaster(Long id, String outputPath) throws IOException {
        RasterData rasterData = rasterDataRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("未找到ID为" + id + "的栅格数据"));

        Long oid = rasterData.getLoOid();
        if (oid == null) {
            throw new IllegalStateException("数据库未保存大对象 OID，无法导出");
        }

        Connection conn = null;
        try {
            conn = DataSourceUtils.getConnection(dataSource);
            conn.setAutoCommit(false);
            LargeObjectManager lobj = conn.unwrap(org.postgresql.PGConnection.class).getLargeObjectAPI();
            LargeObject obj = lobj.open(oid, LargeObjectManager.READ);
            try (OutputStream out = new FileOutputStream(outputPath)) {
                byte[] buf = new byte[4096];
                int s;
                while ((s = obj.read(buf, 0, buf.length)) > 0) {
                    out.write(buf, 0, s);
                }
            }
            obj.close();
            conn.commit();
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ex) { /* 忽略 */ }
            }
            throw new IOException("导出大对象失败: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (Exception e) { /* 忽略 */ }
            }
        }
        return outputPath;
    }
    
    /**
     * 获取所有栅格数据
     */
    public Iterable<RasterData> getAllRasterData() {
        return rasterDataRepository.findAll();
    }
    
    /**
     * 根据ID获取栅格数据
     */
    public RasterData getRasterDataById(Long id) {
        return rasterDataRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("未找到ID为" + id + "的栅格数据"));
    }
    
    /**
     * 删除栅格数据
     */
    @Transactional
    public void deleteRasterData(Long id) {
        RasterData rasterData = getRasterDataById(id);
        Long oid = rasterData.getLoOid();
        
        if (oid != null) {
            Connection conn = null;
            try {
                conn = DataSourceUtils.getConnection(dataSource);
                conn.setAutoCommit(false);
                LargeObjectManager lobj = conn.unwrap(org.postgresql.PGConnection.class).getLargeObjectAPI();
                lobj.delete(oid);
                conn.commit();
            } catch (Exception e) {
                if (conn != null) {
                    try { conn.rollback(); } catch (Exception ex) { /* 忽略 */ }
                }
                log.error("删除大对象失败: {}", e.getMessage(), e);
            } finally {
                if (conn != null) {
                    try { conn.close(); } catch (Exception e) { /* 忽略 */ }
                }
            }
        }
        
        rasterDataRepository.deleteById(id);
    }
} 