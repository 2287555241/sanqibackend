package sanqibackend.sanqibackend.event;

public class RasterDataImportedEvent {
    private final Long rasterDataId;

    public RasterDataImportedEvent(Long rasterDataId) {
        this.rasterDataId = rasterDataId;
    }

    public Long getRasterDataId() {
        return rasterDataId;
    }
} 