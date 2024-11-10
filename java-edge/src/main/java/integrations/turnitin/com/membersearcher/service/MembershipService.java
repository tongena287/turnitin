package integrations.turnitin.com.membersearcher.service;

import java.util.concurrent.CompletableFuture;
import java.util.Map;
import java.util.stream.Collectors;

import integrations.turnitin.com.membersearcher.client.MembershipBackendClient;
import integrations.turnitin.com.membersearcher.model.MembershipList;
import integrations.turnitin.com.membersearcher.model.User;
import integrations.turnitin.com.membersearcher.model.UserList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MembershipService {
	@Autowired
	private MembershipBackendClient membershipBackendClient;

	/**
	 * Method to fetch all memberships with their associated user details included.
	 * This method calls out to the php-backend service and fetches all memberships,
	 * It now calls all users endpoint and merges the data within the method to
	 * reduce network round trips
	 *
	 * @return A CompletableFuture containing a fully populated MembershipList
	 *         object.
	 */
	public CompletableFuture<MembershipList> fetchAllMembershipsWithUsers() {
		return membershipBackendClient.fetchMemberships()
				.thenCombine(membershipBackendClient.fetchUsers(), (memberships, users) -> {
					Map<String, User> userMap = users.getUsers().stream()
							.collect(Collectors.toMap(User::getId, user -> user));

					memberships.getMemberships().forEach(member -> member.setUser(userMap.get(member.getUserId())));

					return memberships;
				});
	}
}
