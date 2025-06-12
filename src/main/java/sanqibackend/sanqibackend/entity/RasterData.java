package sanqibackend.sanqibackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "raster_data")
@Data
public class RasterData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column
    private String description;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "raster_type")
    private String rasterType;
    
    @Column
    private Double resolution;
    
    @Column
    private Integer bands;
    
    @Column(name = "lo_oid")
    private Long loOid;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "thumbnail")
    private byte[] thumbnail;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 