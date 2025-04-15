package fctreddit.clients.grpc;

import fctreddit.api.java.Result;
import fctreddit.clients.java.ImageClient;

public class GrpcImageClient extends ImageClient {
    @Override
    public Result<String> createImage(String userId, byte[] imageContents, String password) {
        return null;
    }

    @Override
    public Result<byte[]> getImage(String userId, String imageId) {
        return null;
    }

    @Override
    public Result<Void> deleteImage(String userId, String imageId, String password) {
        return null;
    }
}
