package fctreddit.clients.rest;

import java.net.URI;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

import fctreddit.api.Post;
import fctreddit.api.User;
import fctreddit.api.rest.RestContent;
import fctreddit.clients.java.ContentClient;
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
import fctreddit.api.rest.RestUsers;
import fctreddit.api.java.Result.ErrorCode;

public class RestContentClient extends ContentClient {
    private static final Logger Log = Logger.getLogger(RestContentClient.class.getName());

    protected static final int READ_TIMEOUT = 5000;
    protected static final int CONNECT_TIMEOUT = 5000;

    protected static final int MAX_RETRIES = 10;
    protected static final int RETRY_SLEEP = 5000;

    final URI serverURI;
    final Client client;
    final ClientConfig config;

    final WebTarget target;

    public RestContentClient(URI serverURI) {
        this.serverURI = serverURI;

        this.config = new ClientConfig();

        config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

        this.client = ClientBuilder.newClient(config);

        target = client.target(serverURI).path(RestUsers.PATH);
    }

    public Result<String> createPost(Post post, String password) {
        return repeated(() -> {
            Response r = target.queryParam(RestContent.PASSWORD, password).request().accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(post, MediaType.APPLICATION_JSON));

            int status = r.getStatus();
            if (status != Status.OK.getStatusCode())
                return Result.error(getErrorCodeFrom(status));
            else
                return Result.ok(r.readEntity(String.class));
        });
    }

    public Result<List<String>> getPosts(long timestamp, String sortOrder) {
        return repeated( () -> {
           Response r = target.queryParam(RestContent.TIMESTAMP, timestamp).queryParam(RestContent.SORTBY, sortOrder).request().accept(MediaType.APPLICATION_JSON).get();

            if( r.getStatus() == Status.OK.getStatusCode() && r.hasEntity() ) {
                List<String> posts = r.readEntity(new GenericType<List<String>>() {});
                System.out.println("Success: (" + posts.size() + " posts)");
                posts.stream().forEach( u -> System.out.println( u));
                return Result.ok(posts);
            } else {
                System.out.println("Error, HTTP error status: " + r.getStatus() );
                return Result.error(getErrorCodeFrom(r.getStatus()));
            }

        });
    }

    public  Result<Post> getPost(String postId) {
        return repeated(() -> {
            Response r = target.path(postId).request().accept(MediaType.APPLICATION_JSON).get();

            int status = r.getStatus();
            if (status != Status.OK.getStatusCode())
                return Result.error(getErrorCodeFrom(status));
            else
                return Result.ok(r.readEntity(Post.class));
        });
    }

    public  Result<List<String>> getPostAnswers(String postId, long maxTimeout) {
        return repeated( () -> {

                Response r = target.path(postId).path(RestContent.REPLIES)
                        .queryParam(RestContent.TIMEOUT, maxTimeout)
                        .request().accept(MediaType.APPLICATION_JSON).get();

                if( r.getStatus() == Status.OK.getStatusCode() && r.hasEntity() ) {
                    List<String> posts = r.readEntity(new GenericType<List<String>>() {});
                    System.out.println("Success: (" + posts.size() + " posts)");
                    posts.stream().forEach( u -> System.out.println( u));
                    return Result.ok(posts);
                } else {
                    System.out.println("Error, HTTP error status: " + r.getStatus() );
                    return Result.error(getErrorCodeFrom(r.getStatus()));
                }

        });
    }

    public Result<Post> updatePost(String postId, String userPassword, Post post) {
        return repeated(() -> {
            Response r = target.path(postId).queryParam(RestContent.PASSWORD, userPassword)
                    .request().accept(MediaType.APPLICATION_JSON)
                    .put(Entity.entity(post, MediaType.APPLICATION_JSON));

            int status = r.getStatus();
            if (status != Status.OK.getStatusCode())
                return Result.error(getErrorCodeFrom(status));
            else
                return Result.ok(r.readEntity(Post.class));
        });
    }

    public Result<Void> deletePost(String postId, String userPassword) {
        return repeated(() -> {
            Response r = target.path(postId).queryParam(RestContent.PASSWORD, userPassword)
                    .request().delete();

            int status = r.getStatus();
            if (status != Status.OK.getStatusCode())
                return Result.error(getErrorCodeFrom(status));
            else
                return Result.ok(r.readEntity(Void.class));
        });
    }

    public Result<Void> upVotePost(String postId, String userId, String userPassword) {
        return repeated(() -> {
            Response r = target.path(postId).path(RestContent.UPVOTE).path(userId)
                    .queryParam(RestContent.PASSWORD, userPassword)
                    .request().post(null);

            int status = r.getStatus();
            if (status != Status.OK.getStatusCode())
                return Result.error(getErrorCodeFrom(status));
            else
                return Result.ok(r.readEntity(Void.class));
        });
    }

    @Override
    public Result<Void> removeUpVotePost(String postId, String userId, String userPassword) {
        return repeated(() -> {
            Response r = target.path(postId).path(RestContent.UPVOTE).path(userId)
                    .queryParam(RestContent.PASSWORD, userPassword)
                    .request().delete();

            int status = r.getStatus();
            if (status != Status.OK.getStatusCode())
                return Result.error(getErrorCodeFrom(status));
            else
                return Result.ok(r.readEntity(Void.class));
        });
    }

    @Override
    public Result<Void> downVotePost(String postId, String userId, String userPassword) {
        return repeated(() -> {
            Response r = target.path(postId).path(RestContent.DOWNVOTE).path(userId)
                    .queryParam(RestContent.PASSWORD, userPassword)
                    .request().post(null);

            int status = r.getStatus();
            if (status != Status.OK.getStatusCode())
                return Result.error(getErrorCodeFrom(status));
            else
                return Result.ok(r.readEntity(Void.class));
        });
    }

    @Override
    public Result<Void> removeDownVotePost(String postId, String userId, String userPassword) {
        return repeated(() -> {
            Response r = target.path(postId).path(RestContent.DOWNVOTE).path(userId)
                    .queryParam(RestContent.PASSWORD, userPassword)
                    .request().delete();

            int status = r.getStatus();
            if (status != Status.OK.getStatusCode())
                return Result.error(getErrorCodeFrom(status));
            else
                return Result.ok(r.readEntity(Void.class));
        });
    }

    @Override
    public Result<Integer> getupVotes(String postId) {
        return repeated(() -> {
            Response r = target.path(postId).path(RestContent.UPVOTE)
                    .request().get();

            int status = r.getStatus();
            if (status != Status.OK.getStatusCode())
                return Result.error(getErrorCodeFrom(status));
            else
                return Result.ok(r.readEntity(Integer.class));
        });
    }

    @Override
    public Result<Integer> getDownVotes(String postId) {
        return repeated(() -> {
            Response r = target.path(postId).path(RestContent.DOWNVOTE)
                    .request().get();

            int status = r.getStatus();
            if (status != Status.OK.getStatusCode())
                return Result.error(getErrorCodeFrom(status));
            else
                return Result.ok(r.readEntity(Integer.class));
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
