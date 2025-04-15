package fctreddit.clients.java;

import fctreddit.api.java.Image;
import fctreddit.api.java.Result;
import jakarta.ws.rs.client.Client;


public abstract class ImageClient implements Image {


    abstract public Result<String> createImage(String userId, byte[] imageContents, String password);

    abstract public Result<byte[]> getImage(String userId, String imageId);

    abstract public Result<Void> deleteImage(String userId, String imageId, String password);

}
