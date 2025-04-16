package fctreddit.impl.java;

import fctreddit.api.User;
import fctreddit.api.java.Image;
import fctreddit.api.java.Result;
import fctreddit.api.java.Users;
import fctreddit.clients.grpc.GrpcUsersClient;
import fctreddit.clients.java.UsersClient;
import fctreddit.clients.rest.RestUsersClient;

import fctreddit.impl.java.JavaMethods;
import fctreddit.server.discovery.Discovery;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Client;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;

public class JavaImages extends JavaMethods implements Image {

    private static final Logger Log = Logger.getLogger(JavaImages.class.getName());
    private final Discovery discovery;

    private UsersClient usersClient;
    private String imgPath;


    public JavaImages(String URI) {
        this.imgPath = URI;

        try {
            discovery = new Discovery(Discovery.DISCOVERY_ADDR);
            discovery.start();
            URI usersUri = tryDiscovery("Users", discovery);
            if (usersUri == null) {
                Log.info("URI invalid");
                //return Result.error(Result.ErrorCode.NOT_FOUND);
            }

            this.usersClient = usersUri.toString().endsWith("rest") ? new RestUsersClient(usersUri) : new GrpcUsersClient();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Discovery", e);
        }
    }

    @Override
    public Result<String> createImage(String userId, byte[] imageContents, String password) {
        Log.info("create Image for user: " + userId);
        String imageID = UUID.randomUUID().toString();
        if (imageContents == null || imageContents.length == 0) {
            Log.info("Empty Image");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
        try {
            Result<User> r = getUserError(userId, password, usersClient.getUser(userId, password));
            if (!r.isOK()) {
                return Result.error(r.error());
            }

        } catch (Exception e) {
            Log.info("surprise!");
            e.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
        Path path = Path.of(imgPath,userId, imageID+ ".png");
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, imageContents);
        } catch (IOException e) {
            Log.severe("Internal error writing image to: " + path + " - " + e.getMessage());
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
        Log.info("Image successfully written to: " + path.toAbsolutePath());
        return Result.ok(imgPath + "/" + userId + "/" + imageID + ".png");

    }

    @Override
    public Result<byte[]> getImage(String userId, String imageId) {
        Log.info("Fetching image for " + userId);

        Path path = Path.of(imgPath, userId, imageId +".png");
        File file = new File(String.valueOf(path));
        if (!file.exists()) {
            Log.info("No image found.");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }
        try {
            byte[] image = Files.readAllBytes(file.toPath());
            Log.info("Image found.");
            return Result.ok(image);
        } catch (IOException e) {
            Log.info("Error reading file.");
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Void> deleteImage(String userId, String imageId, String password) {
        Log.info("Deleting image for " + userId);

        try {
            Result<User> r = getUserError(userId, password, usersClient.getUser(userId, password));
            if (!r.isOK()) {
                return Result.error(r.error());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
        Path path = Path.of(userId, imageId + ".png");
        File file = new File(String.valueOf(path));
        if (!file.exists()) {
            Log.info("No image found.");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }
        if (!file.delete()) {
            Log.info("delete failed.");
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
        Log.info("Image deleted.");
        return Result.ok();
    }

}
