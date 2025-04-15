package fctreddit.impl.java;

import java.util.List;
import java.util.logging.Logger;

import fctreddit.api.User;
import fctreddit.api.java.Result;
import fctreddit.api.java.Users;
import fctreddit.api.java.Result.ErrorCode;
import fctreddit.impl.persistance.Hibernate;
import jakarta.transaction.Transaction;


public class JavaUsers implements Users {

	private static final Logger Log = Logger.getLogger(JavaUsers.class.getName());

	private final Hibernate hibernate;

	public JavaUsers() {
		hibernate = Hibernate.getInstance();
	}

	@Override
	public Result<String> createUser(User user) {
		Log.info("createUser : " + user);

		// Check if user data is valid
		if (!this.isValidField(user.getUserId()) || !this.isValidField(user.getPassword()) || !this.isValidField(user.getFullName())
				|| !this.isValidField(user.getEmail())) {
			Log.info("User object invalid.");
			return Result.error(ErrorCode.BAD_REQUEST);
		}

		//Iniciar uma transação para poder verificar os restantes erros
		try {

			hibernate.persist(user);
		} catch (Exception e) {
			e.printStackTrace(); // Most likely the exception is due to the user already existing...
			Log.info("User already exists.");
			return Result.error(ErrorCode.CONFLICT);
		}

		return Result.ok(user.getUserId());
	}

	@Override
	public Result<User> getUser(String userId, String password) {
		Log.info("getUser : user = " + userId + "; pwd = " + password);

		User user = null;
		try {
			user = hibernate.get(User.class, userId);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(ErrorCode.INTERNAL_ERROR);
		}

		// Check if user exists
		if (user == null) {
			Log.info("User does not exist.");
			return Result.error(ErrorCode.NOT_FOUND);
		}

		// Check if the password is correct
		if (!user.getPassword().equals(password)) {
			Log.info("Password is incorrect");
			return Result.error(ErrorCode.FORBIDDEN);
		}

		return Result.ok(user);

	}

	@Override
	public Result<User> updateUser(String userId, String password, User user) {
		Log.info("updateUser : user = " + userId + "; pwd = " + password + "; user = " + user);

		Result<User> res = this.getUser(userId, password);
		if (!res.isOK()) return Result.error(res.error());

		User existingUser = res.value();

		try {
			if (user.getFullName() != null) {

				existingUser.setFullName(user.getFullName());
			}
			if (user.getEmail() != null) {
				existingUser.setEmail(user.getEmail());
			}
			if (user.getPassword() != null) {
				existingUser.setPassword(user.getPassword());
			}

			if (user.getAvatarUrl() != null) {
				existingUser.setAvatarUrl(user.getAvatarUrl());
			}

			hibernate.update(existingUser);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(ErrorCode.INTERNAL_ERROR);
		}

		return Result.ok(existingUser);
	}

	//Quando user é deleted é preciso fazer delete do seu avatar e dos contents
	@Override
	public Result<User> deleteUser(String userId, String password) {
		Log.info("deleteUser : user = " + userId + "; pwd = " + password);

		Result<User> res = this.getUser(userId, password);
		if (!res.isOK()) return Result.error(res.error());

		User existingUser = res.value();

		try {
			hibernate.delete(existingUser);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(ErrorCode.INTERNAL_ERROR);
		}
		return Result.ok(existingUser);
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
		Log.info("searchUsers : pattern = " + pattern);

		try {
			List<User> list = hibernate.jpql("SELECT u FROM User u WHERE u.userId LIKE '%" + pattern + "%'",
					User.class);
			return Result.ok(list);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(ErrorCode.INTERNAL_ERROR);
		}
	}

	private boolean isValidField(String field) {
		return field != null && !field.isBlank();
	}

}