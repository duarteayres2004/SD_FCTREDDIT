package fctreddit.impl.server.java;

import fctreddit.api.Post;
import fctreddit.api.User;
import fctreddit.api.java.Content;
import fctreddit.api.java.Result;
import fctreddit.api.java.Users;
import fctreddit.clients.grpc.GrpcUsersClient;
import fctreddit.clients.java.UsersClient;
import fctreddit.clients.rest.RestUsersClient;
import fctreddit.impl.server.persistance.Hibernate;
import fctreddit.server.discovery.Discovery;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;

import java.util.*;
import java.util.logging.Logger;

public class JavaContents implements Content {

    private static final Logger Log = Logger.getLogger(JavaUsers.class.getName());

    private final Hibernate hibernate;

    private final Discovery discovery;

    private UsersClient uClient;





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

            URI usersUri = this.tryDiscovery("Users");
            if (usersUri == null) {
                Log.info("URI invalid");
                //return Result.error(Result.ErrorCode.NOT_FOUND);
            }

            this.uClient = usersUri.toString().endsWith("rest") ? new RestUsersClient(usersUri) : new GrpcUsersClient();
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

            Result<User> u = this.getUser(post.getAuthorId(), userPassword);
            if (!u.isOK()) {
                return Result.error(u.error());
            }

            post.setPostId(UUID.randomUUID().toString());
            post.setCreationTimestamp(System.currentTimeMillis());


            hibernate.persist(post);
        } catch (Exception e) {
            e.printStackTrace();
            Log.info("Error creating post");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        return Result.ok(post.getPostId());
    }

    //Professor disse que é so replies diretas e não da árvore
    @Override
    public Result<List<String>> getPosts(long timestamp, String sortOrder) {
        Log.info("getPosts");
        List<String> list = new ArrayList<>();
        try {
            String baseQuery = "SELECT p FROM Post p WHERE p.parentUrl IS NULL";
            if(timestamp > 0) {
                baseQuery += " AND p.creationTimestamp >= " + timestamp;
            }
            if ("MOST_REPLIES".equals(sortOrder)) {
                baseQuery += " ORDER BY (SELECT COUNT(r) FROM Post r WHERE r.parentUrl = p.postId ) DESC";
            } if ("MOST_UP_VOTES".equals(sortOrder)) {
                baseQuery += " ORDER BY p.upVote";
            } else {
                baseQuery += " ORDER BY p.creationTimestamp ASC";

            }
            List<Post> postList = hibernate.jpql(baseQuery, Post.class);
            for (Post post : postList) {
                list.add(post.getPostId());
            }

            return Result.ok(list);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Post> getPost(String postId) {

        Log.info("Get Post:" + postId);

        try {
            Post post = hibernate.get(Post.class, postId);
            if (post == null) {
                return Result.error(Result.ErrorCode.NOT_FOUND);
            } else {
                return Result.ok(post);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<List<String>> getPostAnswers(String postId, long maxTimeout) {
        Log.info("Get Answers to post: " + postId);
        //TODO
        return null;
    }

    @Override
    public Result<Post> updatePost(String postId, String userPassword, Post post) {
        Log.info("Update post: " + postId);

        try {
            Result<User> u = this.getUser(post.getAuthorId(), userPassword);

            if (!u.isOK()) {
                return Result.error(u.error());
            }

            Post resultPost = hibernate.get(Post.class, postId);
            if (resultPost == null) {
                return Result.error(Result.ErrorCode.NOT_FOUND);
            }

            resultPost.setContent(post.getContent());
            resultPost.setMediaUrl(post.getMediaUrl());

            hibernate.update(resultPost);

            return Result.ok(resultPost);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
    }

    @Override
    public Result<Void> deletePost(String postId, String userPassword) {
        Log.info("deletePost : " + postId);

        try {
            Post post = hibernate.get(Post.class, postId);
            if (post == null) {
                return Result.error(Result.ErrorCode.NOT_FOUND);
            }

            Result<User> u = this.getUser(post.getAuthorId(), userPassword);
            if (!u.isOK()) {
                return Result.error(u.error());
            }

            //TODO : Descrição contraditoria
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
    }

    @Override
    public Result<Void> upVotePost(String postId, String userId, String userPassword) {
        Log.info("upVotePost : " + postId);

        try {
            Post post = hibernate.get(Post.class, postId);
            if (post == null) {
                return Result.error(Result.ErrorCode.NOT_FOUND);
            }

            Result<User> u = this.getUser(userId, userPassword);
            if (!u.isOK()) {
                return Result.error(u.error());
            }

            //TODO : Como saber se o user ja deu upvote ou downvote?
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
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
        Log.info("getUpVotes : " + postId);

        try {
            Post post = hibernate.get(Post.class, postId);
            if (post == null) {
                return Result.error(Result.ErrorCode.NOT_FOUND);
            }

            return Result.ok(post.getUpVote());

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
    }

    @Override
    public Result<Integer> getDownVotes(String postId) {
        Log.info("getDownVotes : " + postId);

        try {
            Post post = hibernate.get(Post.class, postId);
            if (post == null) {
                return Result.error(Result.ErrorCode.NOT_FOUND);
            }

            return Result.ok(post.getDownVote());

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
    }

    private Result<User> getUser(String userId, String pwd) {
        Result<User> u = uClient.getUser(userId, pwd);

        if (u.error().equals(Result.ErrorCode.NOT_FOUND)) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        } else if (u.error().equals(Result.ErrorCode.FORBIDDEN)) {
            return Result.error(Result.ErrorCode.FORBIDDEN);
        }
        return u;
    }


}
