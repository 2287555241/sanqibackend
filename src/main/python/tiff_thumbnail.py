import os
from osgeo import gdal
from PIL import Image
import numpy as np
from pathlib import Path
import sys
import warnings
import logging
import math

# 配置日志
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# 配置GDAL使用异常
gdal.UseExceptions()

def get_optimal_block_size(width, height, max_memory=1024*1024*1024):  # 1GB
    """计算最优的块大小"""
    total_pixels = width * height
    block_size = int(math.sqrt(max_memory / 4))  # 假设每个像素4字节
    return min(block_size, width, height)

def read_in_blocks(dataset, band, block_size):
    """分块读取栅格数据"""
    width = dataset.RasterXSize
    height = dataset.RasterYSize
    
    # 创建输出数组
    result = np.zeros((height, width), dtype=np.float32)
    
    # 计算块数
    x_blocks = math.ceil(width / block_size)
    y_blocks = math.ceil(height / block_size)
    
    logger.info(f"分块读取: {x_blocks}x{y_blocks} 块, 块大小: {block_size}x{block_size}")
    
    for y in range(y_blocks):
        for x in range(x_blocks):
            try:
                # 计算当前块的范围
                x_start = x * block_size
                y_start = y * block_size
                x_size = min(block_size, width - x_start)
                y_size = min(block_size, height - y_start)
                
                # 读取数据块
                block = band.ReadAsArray(x_start, y_start, x_size, y_size)
                if block is not None:
                    result[y_start:y_start+y_size, x_start:x_start+x_size] = block
                else:
                    logger.warning(f"块读取失败: x={x}, y={y}")
                    # 使用周围块的平均值填充
                    if x > 0 and y > 0:
                        result[y_start:y_start+y_size, x_start:x_start+x_size] = np.mean([
                            result[y_start-1:y_start, x_start:x_start+x_size],
                            result[y_start:y_start+y_size, x_start-1:x_start]
                        ])
            except Exception as e:
                logger.warning(f"处理块时出错 (x={x}, y={y}): {str(e)}")
                continue
    
    return result

def try_read_with_gdal(input_path, dataset=None):
    """尝试使用GDAL读取TIFF文件"""
    try:
        if dataset is None:
            # 设置GDAL配置
            gdal.SetConfigOption('GDAL_CACHEMAX', '1024')  # 1GB缓存
            gdal.SetConfigOption('GDAL_DISABLE_READDIR_ON_OPEN', 'TRUE')
            gdal.SetConfigOption('CPL_DEBUG', 'OFF')
            
            dataset = gdal.Open(input_path, gdal.GA_ReadOnly)
            if dataset is None:
                return None, None

        band = dataset.GetRasterBand(1)
        if band is None:
            return None, dataset

        # 获取栅格大小
        width = dataset.RasterXSize
        height = dataset.RasterYSize
        
        # 计算最优块大小
        block_size = get_optimal_block_size(width, height)
        logger.info(f"栅格大小: {width}x{height}, 使用块大小: {block_size}")
        
        # 分块读取数据
        data = read_in_blocks(dataset, band, block_size)
        
        if data is None or data.size == 0:
            return None, dataset
            
        return data, dataset
        
    except Exception as e:
        logger.error(f"GDAL读取失败: {str(e)}")
        return None, dataset

def generate_tiff_thumbnail(input_path, output_path, max_size=(800, 800)):
    """
    Generate a thumbnail from a TIFF file.
    
    Args:
        input_path (str): Path to the input TIFF file
        output_path (str): Path where the thumbnail will be saved
        max_size (tuple): Maximum size of the thumbnail (width, height)
    """
    dataset = None
    try:
        # 检查输入文件是否存在
        if not os.path.exists(input_path):
            raise Exception(f"Input file does not exist: {input_path}")

        # 检查文件大小
        file_size = os.path.getsize(input_path)
        if file_size == 0:
            raise Exception(f"Input file is empty: {input_path}")

        logger.info(f"开始处理文件: {input_path}")
        logger.info(f"文件大小: {file_size} bytes")

        # 设置GDAL配置选项
        gdal.SetConfigOption('GDAL_TIFF_INTERNAL_MASK', 'YES')
        gdal.SetConfigOption('GDAL_TIFF_OVR_BLOCKSIZE', '256')
        gdal.SetConfigOption('GDAL_TIFF_USE_OVR', 'NO')  # 禁用概览
        gdal.SetConfigOption('GDAL_DISABLE_READDIR_ON_OPEN', 'TRUE')
        gdal.SetConfigOption('CPL_DEBUG', 'ON')  # 启用调试输出

        # 尝试使用GDAL读取
        logger.info("使用GDAL读取文件")
        data, dataset = try_read_with_gdal(input_path)
        if data is None:
            raise Exception("无法读取TIFF文件数据")

        # 确保数据是二维的
        if len(data.shape) > 2:
            data = data[0] if data.shape[0] == 1 else data[:,:,0]

        # 处理无效值
        if np.issubdtype(data.dtype, np.floating):
            data = np.nan_to_num(data, nan=0.0, posinf=data.max(), neginf=data.min())

        # 归一化数据
        if data.dtype != np.uint8:
            data_min = np.min(data)
            data_max = np.max(data)
            if data_max > data_min:
                data = ((data - data_min) * (255.0 / (data_max - data_min))).astype(np.uint8)
            else:
                data = np.zeros_like(data, dtype=np.uint8)

        # 创建PIL图像
        img = Image.fromarray(data)
        
        # 计算新尺寸
        ratio = min(max_size[0] / img.width, max_size[1] / img.height)
        new_size = (int(img.width * ratio), int(img.height * ratio))
        
        # 调整图像大小
        img = img.resize(new_size, Image.Resampling.LANCZOS)
        
        # 创建输出目录
        os.makedirs(os.path.dirname(output_path), exist_ok=True)
        
        # 保存缩略图
        img.save(output_path, 'JPEG', quality=85)
        logger.info(f"缩略图已保存到: {output_path}")
        
        return True, "缩略图生成成功"
        
    except Exception as e:
        logger.error(f"生成缩略图时发生错误: {str(e)}")
        return False, f"Error generating thumbnail: {str(e)}"
        
    finally:
        # 清理资源
        if dataset is not None:
            dataset = None
        gdal.SetConfigOption('GDAL_TIFF_INTERNAL_MASK', None)
        gdal.SetConfigOption('GDAL_TIFF_OVR_BLOCKSIZE', None)
        gdal.SetConfigOption('GDAL_TIFF_USE_OVR', None)
        gdal.SetConfigOption('GDAL_DISABLE_READDIR_ON_OPEN', None)
        gdal.SetConfigOption('CPL_DEBUG', None)
        gdal.SetConfigOption('GDAL_CACHEMAX', None)

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python tiff_thumbnail.py <input_tiff_path> <output_jpg_path>")
        sys.exit(1)
        
    input_file = sys.argv[1]
    output_file = sys.argv[2]
    
    logger.info(f"处理文件: {input_file}")
    logger.info(f"输出将保存到: {output_file}")
    
    # 忽略GDAL的FutureWarning
    with warnings.catch_warnings():
        warnings.simplefilter("ignore", FutureWarning)
        success, message = generate_tiff_thumbnail(input_file, output_file)
    
    logger.info(message)
    sys.exit(0 if success else 1) 