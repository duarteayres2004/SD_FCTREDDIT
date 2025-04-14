package fctreddit.impl.server.java;

import fctreddit.api.Post;
import fctreddit.api.User;
import fctreddit.api.java.Content;
import fctreddit.api.java.Result;
import fctreddit.impl.server.persistance.Hibernate;
import fctreddit.server.discovery.Discovery;
import java.net.URI;

import java.util.List;
import java.util.logging.Logger;

public class JavaContents implements Content {

    private static final Logger Log = Logger.getLogger(JavaUsers.class.getName());

    private final Hibernate hibernate;

    private final Discovery discovery;


    public URI tryDiscovery(String serviceName){
        URI[] Uris = discovery.knownUrisOf(serviceName,1);
        URI Uri = Uris[0];
        return Uri;
    }

    public JavaContents() {
        hibernate = Hibernate.getInstance();
        try {
            discovery = new Discovery(Discovery.DISCOVERY_ADDR);
            discovery.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Discovery", e);
        }
    }



    @Override
    public Result<String> createPost(Post post, String userPassword) {
        Log.info("createPost : " + post);


        if(userPassword == null) {
            Log.info("User password invalid.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
        if(post.getAuthorId() == null || post.getContent() == null) {
            Log.info("Post object invalid.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        try {
            URI usersUri = this.tryDiscovery("Users");
            if (usersUri == null) {
                Log.info("URI invalid");
                return Result.error(Result.ErrorCode.NOT_FOUND);
            }

            //Fazer qq coisa com o user

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
        try {
            hibernate.persist(post);
        } catch (Exception e) {
            e.printStackTrace(); // Most likely the exception is due to the post already existing...
            Log.info("pPost already exists.");
            return Result.error(Result.ErrorCode.CONFLICT);
        }

        return Result.ok(post.getPostId());
    }

    @Override
    public Result<List<String>> getPosts(long timestamp, String sortOrder) {
        Log.info("getPosts");
        List<String> list;
        try {
            String baseQuery = "SELECT p FROM Post p WHERE p.parentUrl IS NULL";
            if(timestamp > 0) {
                baseQuery += " AND p.creationTimestamp >= " + timestamp;
            }
            if ("MOST_UP_VOTES".equals(sortOrder)) {
                baseQuery += " ORDER BY p.upVote DESC";
            } else if ("MOST_REPLIES".equals(sortOrder)) {
                baseQuery += " ORDER BY size(p.replies) DESC";
            } else {
                baseQuery += " ORDER BY p.creationTimestamp ASC";
            }

            list = hibernate.jpql(baseQuery, String.class);
            return Result.ok(list);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Post> getPost(String postId) {
        return null;
    }

    @Override
    public Result<List<String>> getPostAnswers(String postId, long maxTimeout) {
        return null;
    }

    @Override
    public Result<Post> updatePost(String postId, String userPassword, Post post) {
        return null;
    }

    @Override
    public Result<Void> deletePost(String postId, String userPassword) {
        return null;
    }

    @Override
    public Result<Void> upVotePost(String postId, String userId, String userPassword) {
        return null;
    }

    @Override
    public Result<Void> removeUpVotePost(String postId, String userId, String userPassword) {
        return null;
    }

    @Override
    public Result<Void> downVotePost(String postId, String userId, String userPassword) {
        return null;
    }

    @Override
    public Result<Void> removeDownVotePost(String postId, String userId, String userPassword) {
        return null;
    }

    @Override
    public Result<Integer> getupVotes(String postId) {
        return null;
    }

    @Override
    public Result<Integer> getDownVotes(String postId) {
        return null;
    }
}
