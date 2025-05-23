package sanqibackend.sanqibackend.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import sanqibackend.sanqibackend.entity.SanqiRegion;
import java.util.List;

/**
 * 三七种植区域数据访问接口
 */
@Mapper
public interface SanqiRegionMapper{
  /**
   * 查询所有区域信息
   * 
   * SQL处理步骤:
   * 1. ST_SetSRID: 设置几何数据的空间参考标识符为4326(WGS84)
   * 2. ST_Transform: 确保坐标系统为WGS84
   * 3. ST_Difference: 处理区域间的重叠部分，移除与其他区域相交的部分
   * 4. ST_MakeValid: 修复任何无效的几何图形
   * 5. ST_CollectionExtract: 提取所有多边形要素(type=3表示多边形)
   * 6. ST_Multi: 确保返回MultiPolygon类型
   * 7. ST_AsGeoJSON: 将几何数据转换为GeoJSON格式
   * - 参数15: 坐标精度(小数位数)
   * - 参数2: 包含边界框信息
   *
   * @return 所有区域的列表
   */
  @Select("SELECT gid, name,  " +
      "ST_AsGeoJSON(" +
      "  ST_Multi(" + // 确保返回MultiPolygon类型
      "    ST_CollectionExtract(" + // 提取多边形要素
      "      ST_MakeValid(" + // 修复无效几何图形
      "        ST_Difference(" + // 处理重叠区域
      "          ST_Transform(" + // 转换坐标系统
      "            ST_SetSRID(geom, 4326), " + // 设置空间参考
      "            4326" +
      "          )," +
      "          COALESCE(" + // 处理NULL值情况
      "            (SELECT ST_Union(ST_Transform(ST_SetSRID(geom, 4326), 4326)) " +
      "             FROM public.sanqi s2 " +
      "             WHERE s2.gid != sanqi.gid " + // 排除自身
      "             AND ST_Intersects(s2.geom, sanqi.geom)" + // 找出相交区域
      "            ), " +
      "            ST_GeomFromText('POLYGON EMPTY')" + // 如果没有相交则返回空多边形
      "          )" +
      "        )" +
      "      ), " +
      "      3" + // 3表示多边形类型
      "    )" +
      "  )" +
      ", 15, 2)::json as geom " + // 转换为JSON格式
      "FROM public.sanqi")
  List<SanqiRegion> findAll();

  /**
   * 根据ID查询特定区域信息
   * 
   * SQL处理过程与findAll相同，但只返回指定ID的记录
   * 处理步骤详见findAll方法的注释
   *
   * @param id 区域ID
   * @return 指定ID的区域信息
   */
  @Select("SELECT gid, name, " +
      "ST_AsGeoJSON(" +
      "  ST_Multi(" +
      "    ST_CollectionExtract(" +
      "      ST_MakeValid(" +
      "        ST_Difference(" +
      "          ST_Transform(" +
      "            ST_SetSRID(geom, 4326), " +
      "            4326" +
      "          )," +
      "          COALESCE(" +
      "            (SELECT ST_Union(ST_Transform(ST_SetSRID(geom, 4326), 4326)) " +
      "             FROM public.sanqi s2 " +
      "             WHERE s2.gid != #{id} " +
      "             AND ST_Intersects(s2.geom, sanqi.geom)" +
      "            ), " +
      "            ST_GeomFromText('POLYGON EMPTY')" +
      "          )" +
      "        )" +
      "      ), " +
      "      3" +
      "    )" +
      "  )" +
      ", 15, 2)::json as geom " +
      "FROM public.sanqi " +
      "WHERE gid = #{id}")
  SanqiRegion findById(Long id);
}