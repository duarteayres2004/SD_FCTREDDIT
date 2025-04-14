package fctreddit.clients.java;

import fctreddit.api.java.Result;
import jakarta.ws.rs.client.Client;


public abstract class ImageClient implements Client {


    abstract public Result<String> createImage(String userId, byte[] imageContents);

    abstract public Result<byte[]> getImage(String userId, String imageId);

    abstract public Result<Void> deleteImage(String userId, String imageId);

}
