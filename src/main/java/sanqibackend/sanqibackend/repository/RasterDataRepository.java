package sanqibackend.sanqibackend.repository;

import sanqibackend.sanqibackend.entity.RasterData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
@Repository
public interface RasterDataRepository extends JpaRepository<RasterData, Long> {
    // 可以添加自定义查询方法
} 