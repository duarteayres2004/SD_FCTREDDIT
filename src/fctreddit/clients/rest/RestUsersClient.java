package fctreddit.clients.rest;

import java.net.URI;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

import fctreddit.clients.java.ImageClient;
import fctreddit.clients.java.UsersClient;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import fctreddit.api.java.Result;
import fctreddit.api.User;
import fctreddit.api.rest.RestUsers;
import fctreddit.api.java.Result.ErrorCode;

public class RestUsersClient extends UsersClient {
    private static final Logger Log = Logger.getLogger(RestUsersClient.class.getName());

    protected static final int READ_TIMEOUT = 5000;
    protected static final int CONNECT_TIMEOUT = 5000;

    protected static final int MAX_RETRIES = 10;
    protected static final int RETRY_SLEEP = 5000;

    final URI serverURI;
    final Client client;
    final ClientConfig config;

    final WebTarget target;

    public RestUsersClient(URI serverURI) {
        this.serverURI = serverURI;

        this.config = new ClientConfig();

        config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

        this.client = ClientBuilder.newClient(config);

        target = client.target(serverURI).path(RestUsers.PATH);
    }

    public Result<String> createUser(User user) {
        return repeated(() -> {
            Response r = target.request().accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(user, MediaType.APPLICATION_JSON));

            int status = r.getStatus();
            if (status != Status.OK.getStatusCode())
                return Result.error(getErrorCodeFrom(status));
            else
                return Result.ok(r.readEntity(String.class));
        });
    }

    public Result<User> getUser(String userId, String pwd) {

        return repeated(() -> {
            Response r = target.path(userId).queryParam(RestUsers.PASSWORD, pwd).request()
                    .accept(MediaType.APPLICATION_JSON).get();

            int status = r.getStatus();
            if (status != Status.OK.getStatusCode())
                return Result.error(getErrorCodeFrom(status));
            else
                return Result.ok(r.readEntity(User.class));
        });
    }

    public Result<User> updateUser(String userId, String password, User user) {
        // throw new RuntimeException("Not Implemented...");

        return repeated(() -> {
            Response r = target.path(userId).queryParam(RestUsers.PASSWORD, password).request()
                    .accept(MediaType.APPLICATION_JSON).put(Entity.entity(user, MediaType.APPLICATION_JSON));

            int status = r.getStatus();
            if (status != Status.OK.getStatusCode())
                return Result.error(getErrorCodeFrom(status));
            else
                return Result.ok(r.readEntity(User.class));
        });
    }

    public Result<User> deleteUser(String userId, String password) {
        // throw new RuntimeException("Not Implemented...");
        return repeated(() -> {
            Response r = target.path(userId).queryParam(RestUsers.PASSWORD, password).request()
                    .accept(MediaType.APPLICATION_JSON).delete();

            int status = r.getStatus();
            if (status != Status.OK.getStatusCode())
                return Result.error(getErrorCodeFrom(status));
            else
                return Result.ok(r.readEntity(User.class));
        });
    }

    public Result<List<User>> searchUsers(String pattern) {
        //throw new RuntimeException("Not Implemented...");
        return repeated(() -> {
            Response r = target.path("/").queryParam( RestUsers.QUERY, pattern).request()
                    .accept(MediaType.APPLICATION_JSON)
                    .get();

            if( r.getStatus() == Status.OK.getStatusCode() && r.hasEntity() ) {
                List<User> users = r.readEntity(new GenericType<List<User>>() {});
                System.out.println("Success: (" + users.size() + " users)");
                users.stream().forEach( u -> System.out.println( u));
                return Result.ok(users);
            } else {
                System.out.println("Error, HTTP error status: " + r.getStatus() );
                return Result.error(getErrorCodeFrom(r.getStatus()));
            }

        });

    }

    private <T> Result<T> repeated(Supplier<Result<T>> f) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return f.get();
            } catch (ProcessingException x) {
                Log.info(x.getMessage());

                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException e) {
                    // Nothing to be done here.
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return Result.error(ErrorCode.TIMEOUT);

    }

    public static ErrorCode getErrorCodeFrom(int status) {
        return switch (status) {
            case 200, 209 -> ErrorCode.OK;
            case 409 -> ErrorCode.CONFLICT;
            case 403 -> ErrorCode.FORBIDDEN;
            case 404 -> ErrorCode.NOT_FOUND;
            case 400 -> ErrorCode.BAD_REQUEST;
            case 500 -> ErrorCode.INTERNAL_ERROR;
            case 501 -> ErrorCode.NOT_IMPLEMENTED;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }
}
