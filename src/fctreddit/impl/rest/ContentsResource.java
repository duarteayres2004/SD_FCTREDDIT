package fctreddit.impl.rest;

import fctreddit.api.Post;
import fctreddit.api.java.Content;
import fctreddit.api.java.Result;
import fctreddit.api.rest.RestContent;
import fctreddit.impl.java.JavaContents;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.logging.Logger;


public class ContentsResource implements RestContent {

    private static final Logger Log = Logger.getLogger(UsersResource.class.getName());

    final Content impl;

    public ContentsResource() {
        impl = new JavaContents();
    }

    @Override
    public String createPost(Post post, String userPassword) {
        Log.info("createPost : " + post);

        Result<String> res = impl.createPost(post,userPassword);
        if (!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        return res.value();
    }

    @Override
    public List<String> getPosts(long timestamp, String sortOrder) {
        Log.info("getPosts");

        Result<List<String>> res = impl.getPosts(timestamp,sortOrder);
        if (!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        return res.value();
    }

    @Override
    public Post getPost(String postId) {
        Log.info("getPost: " + postId);

        Result<Post> res = impl.getPost(postId);
        if (!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        return res.value();
    }

    @Override
    public List<String> getPostAnswers(String postId, long timeout) {
        Log.info("getPostAnswers of: " + postId);

        Result<List<String>> res = impl.getPostAnswers(postId,timeout);
        if (!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        return res.value();
    }

    @Override
    public Post updatePost(String postId, String userPassword, Post post) {
        Log.info("updatePost: " + postId);

        Result<Post> res = impl.updatePost(postId,userPassword,post);
        if (!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        return res.value();
    }

    @Override
    public void deletePost(String postId, String userPassword) {
        Log.info("deletePost: " + postId);

        Result<Void> res = impl.deletePost(postId, userPassword);
        if (!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        res.value();
    }

    @Override
    public void upVotePost(String postId, String userId, String userPassword) {
        Log.info("upVotePost: " + postId);

        Result<Void> res = impl.upVotePost(postId, userId, userPassword);
        if (!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        res.value();
    }

    @Override
    public void removeUpVotePost(String postId, String userId, String userPassword) {
        Log.info("removeUpVotePost: " + postId);

        Result<Void> res = impl.removeUpVotePost(postId, userId, userPassword);
        if (!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        res.value();
    }

    @Override
    public void downVotePost(String postId, String userId, String userPassword) {
        Log.info("downVotePost: " + postId);

        Result<Void> res = impl.downVotePost(postId, userId, userPassword);
        if (!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        res.value();
    }

    @Override
    public void removeDownVotePost(String postId, String userId, String userPassword) {
        Log.info("removeDownVotePost: " + postId);

        Result<Void> res = impl.removeDownVotePost(postId, userId, userPassword);
        if (!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        res.value();
    }

    @Override
    public Integer getupVotes(String postId) {
        Log.info("getUpVotes: " + postId);

        Result<Integer> res = impl.getupVotes(postId);
        if (!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        return res.value();
    }

    @Override
    public Integer getDownVotes(String postId) {
        Log.info("getDownVotes: " + postId);

        Result<Integer> res = impl.getDownVotes(postId);
        if (!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        return res.value();
    }

    protected static Response.Status errorCodeToStatus(Result.ErrorCode error) {
        Response.Status status = switch (error) {
            case NOT_FOUND -> Response.Status.NOT_FOUND;
            case CONFLICT -> Response.Status.CONFLICT;
            case FORBIDDEN -> Response.Status.FORBIDDEN;
            case NOT_IMPLEMENTED -> Response.Status.NOT_IMPLEMENTED;
            case BAD_REQUEST -> Response.Status.BAD_REQUEST;
            default -> Response.Status.INTERNAL_SERVER_ERROR;
        };

        return status;
    }
}
