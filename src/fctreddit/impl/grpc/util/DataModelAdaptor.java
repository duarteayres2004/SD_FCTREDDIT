package fctreddit.impl.grpc.util;

import fctreddit.api.Post;
import fctreddit.api.User;

import fctreddit.impl.grpc.generated_java.ContentProtoBuf;
import fctreddit.impl.grpc.generated_java.UsersProtoBuf.GrpcUser;
import fctreddit.impl.grpc.generated_java.UsersProtoBuf.GrpcUser.Builder;

public class DataModelAdaptor {

	//Notice that optional values in a Message might not have an
	//assigned value (although for Strings default value is "") so
	//before assigning we check if the field has a value, if not
	//we assign null.
	public static User GrpcUser_to_User( GrpcUser from )  {
		return new User(
				from.hasUserId() ? from.getUserId() : null,
				from.hasFullName() ? from.getFullName() : null,
				from.hasEmail() ? from.getEmail() : null,
				from.hasPassword() ? from.getPassword() : null,
				from.hasAvatarUrl() ? from.getAvatarUrl() : null);
	}

	//Notice that optional values might not have a value, and
	//you should never assign null to a field in a Message
	public static GrpcUser User_to_GrpcUser( User from )  {
		Builder b = GrpcUser.newBuilder();

		if(from.getUserId() != null)
			b.setUserId( from.getUserId());

		if(from.getPassword() != null)
			b.setPassword( from.getPassword());

		if(from.getEmail() != null)
			b.setEmail( from.getEmail());

		if(from.getFullName() != null)
			b.setFullName( from.getFullName());

		if(from.getAvatarUrl() != null)
			b.setAvatarUrl( from.getAvatarUrl());

		return b.build();
	}

	public static Post GrpcPost_to_Post(ContentProtoBuf.GrpcPost from) {
		return new Post(
				from.hasPostId() ? from.getPostId() : null,
				from.hasAuthorId() ? from.getAuthorId() : null,
				from.hasCreationTimestamp() ? from.getCreationTimestamp() : 0,
				from.hasContent() ? from.getContent() : null,
				from.hasMediaUrl() ? from.getMediaUrl() : null,
				from.hasParentUrl() ? from.getParentUrl() : null,
				from.hasUpVote() ? from.getUpVote() : 0,
				from.hasDownVote() ? from.getDownVote() : 0);
	}

	public static ContentProtoBuf.GrpcPost Post_to_GrpcPost(Post from) {
		ContentProtoBuf.GrpcPost.Builder b = ContentProtoBuf.GrpcPost.newBuilder();

		if (from.getPostId() != null)
			b.setPostId(from.getPostId());

		if (from.getAuthorId() != null)
			b.setAuthorId(from.getAuthorId());

		b.setCreationTimestamp(from.getCreationTimestamp());

		if (from.getContent() != null)
			b.setContent(from.getContent());

		if (from.getMediaUrl() != null)
			b.setMediaUrl(from.getMediaUrl());

		if (from.getParentUrl() != null)
			b.setParentUrl(from.getParentUrl());

		b.setUpVote(from.getUpVote());
		b.setDownVote(from.getDownVote());

		return b.build();
	}


}
