package com.google.devrel.training.conference.spi;

import static com.google.devrel.training.conference.service.OfyService.ofy;

import java.util.List;

import static com.google.devrel.training.conference.service.OfyService.factory;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;
import com.google.devrel.training.conference.Constants;
import com.google.devrel.training.conference.domain.Conference;
import com.google.devrel.training.conference.domain.Profile;
import com.google.devrel.training.conference.form.ConferenceForm;
import com.google.devrel.training.conference.form.ConferenceQueryForm;
import com.google.devrel.training.conference.form.ProfileForm;
import com.google.devrel.training.conference.form.ProfileForm.TeeShirtSize;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;

/**
 * Defines conference APIs.
 */
@Api(name = "conference", version = "v1", scopes = { Constants.EMAIL_SCOPE }, clientIds = { Constants.WEB_CLIENT_ID,
		Constants.API_EXPLORER_CLIENT_ID }, description = "API for the Conference Central Backend application.")

public class ConferenceApi {
	/*
	 * Get the display name from the user's email. For example, if the email is
	 * lemoncake@example.com, then the display name becomes "lemoncake."
	 */
	private static String extractDefaultDisplayNameFromEmail(String email) {
		return email == null ? null : email.substring(0, email.indexOf("@"));
	}

	/**
	 * Creates or updates a Profile object associated with the given user object.
	 *
	 * @param user        A User object injected by the cloud endpoints.
	 * @param profileForm A ProfileForm object sent from the client form.
	 * @return Profile object just created.
	 * @throws UnauthorizedException when the User object is null.
	 */

	// Declare this method as a method available externally through Endpoints
	@ApiMethod(name = "saveProfile", path = "profile", httpMethod = HttpMethod.POST)

	// The request that invokes this method should provide data that
	// conforms to the fields defined in ProfileForm

	// TODO 1 Pass the ProfileForm parameter
	// TODO 2 Pass the User parameter
	public Profile saveProfile(final User user, ProfileForm profileForm) throws UnauthorizedException {

		String userId = user.getUserId();
		String mainEmail = user.getEmail();
		String displayName = profileForm.getDisplayName();
		TeeShirtSize teeShirtSize = profileForm.getTeeShirtSize();

		/*
		 * // TODO 2 // If the user is not logged in, throw an UnauthorizedException if
		 * (user == null) { throw new UnauthorizedException("Authorization required"); }
		 * 
		 * // TODO 1 // Set the teeShirtSize to the value sent by the ProfileForm, if
		 * sent // otherwise leave it as the default value if
		 * (profileForm.getTeeShirtSize() != null) { teeShirtSize =
		 * profileForm.getTeeShirtSize(); }
		 * 
		 * // TODO 1 // Set the displayName to the value sent by the ProfileForm, if
		 * sent // otherwise set it to null displayName = profileForm.getDisplayName();
		 * 
		 * // TODO 2 // Get the userId and mainEmail mainEmail = user.getEmail(); userId
		 * = user.getUserId();
		 * 
		 * // TODO 2 // If the displayName is null, set it to the default value based on
		 * the user's // email // by calling extractDefaultDisplayNameFromEmail(...) if
		 * (displayName == null) { displayName =
		 * extractDefaultDisplayNameFromEmail(user.getEmail()); }
		 * 
		 * // Create a new Profile entity from the // userId, displayName, mainEmail and
		 * teeShirtSize Profile profile = new Profile(userId, displayName, mainEmail,
		 * teeShirtSize);
		 * 
		 * // TODO 3 (In lesson 3) // Save the entity in the datastore
		 * ofy().save().entity(profile).now();
		 */

		Key key = Key.create(Profile.class, userId);
		Profile profile = (Profile) ofy().load().key(key).now();
		if (profile == null) {
			if (profileForm.getTeeShirtSize() != null) {
				teeShirtSize = profileForm.getTeeShirtSize();
			}
			if (displayName == null) {
				displayName = extractDefaultDisplayNameFromEmail(user.getEmail());
			}
			profile = new Profile(userId, displayName, mainEmail, teeShirtSize);

		} else {
			profile.Update(profileForm.getDisplayName(), profileForm.getTeeShirtSize());
		}
		ofy().save().entity(profile).now();

		// Return the profile
		return profile;
	}

	/**
	 * Returns a Profile object associated with the given user object. The cloud
	 * endpoints system automatically inject the User object.
	 *
	 * @param user A User object injected by the cloud endpoints.
	 * @return Profile object.
	 * @throws UnauthorizedException when the User object is null.
	 */
	@ApiMethod(name = "getProfile", path = "profile", httpMethod = HttpMethod.GET)
	public Profile getProfile(final User user) throws UnauthorizedException {
		if (user == null) {
			throw new UnauthorizedException("Authorization required");
		}

		// TODO
		// load the Profile Entity
		String userId = user.getUserId(); // TODO
		Key key = Key.create(Profile.class, userId); // TODO
		Profile profile = (Profile) ofy().load().key(key).now();
		; // TODO load the Profile entity
		return profile;
	}

	/**
	 * Gets the Profile entity for the current user or creates it if it doesn't
	 * exist
	 * 
	 * @param user
	 * @return user's Profile
	 */
	private static Profile getProfileFromUser(User user) {
		// First fetch the user's Profile from the datastore.
		Profile profile = ofy().load().key(Key.create(Profile.class, user.getUserId())).now();
		if (profile == null) {
			// Create a new Profile if it doesn't exist.
			// Use default displayName and teeShirtSize
			String email = user.getEmail();
			profile = new Profile(user.getUserId(), extractDefaultDisplayNameFromEmail(email), email,
					TeeShirtSize.NOT_SPECIFIED);
		}
		return profile;
	}

	/**
	 * Creates a new Conference object and stores it to the datastore.
	 *
	 * @param user           A user who invokes this method, null when the user is
	 *                       not signed in.
	 * @param conferenceForm A ConferenceForm object representing user's inputs.
	 * @return A newly created Conference Object.
	 * @throws UnauthorizedException when the user is not signed in.
	 */
	@ApiMethod(name = "createConference", path = "conference", httpMethod = HttpMethod.POST)
	public Conference createConference(final User user, final ConferenceForm conferenceForm)
			throws UnauthorizedException {
		if (user == null) {
			throw new UnauthorizedException("Authorization required");
		}

		// TODO (Lesson 4)
		// Get the userId of the logged in User
		String userId = user.getUserId();

		// TODO (Lesson 4)
		// Get the key for the User's Profile
		Key<Profile> profileKey = Key.create(Profile.class, userId);

		// TODO (Lesson 4)
		// Allocate a key for the conference -- let App Engine allocate the ID
		// Don't forget to include the parent Profile in the allocated ID
		final Key<Conference> conferenceKey = factory().allocateId(profileKey, Conference.class);

		// TODO (Lesson 4)
		// Get the Conference Id from the Key
		final long conferenceId = conferenceKey.getId();

		// TODO (Lesson 4)
		// Get the existing Profile entity for the current user if there is one
		// Otherwise create a new Profile entity with default values
		Profile profile = getProfileFromUser(user);

		// TODO (Lesson 4)
		// Create a new Conference Entity, specifying the user's Profile entity
		// as the parent of the conference
		Conference conference = new Conference(conferenceId, userId, conferenceForm);

		// TODO (Lesson 4)
		// Save Conference and Profile Entities
		ofy().save().entities(conference, profile).now();

		return conference;
	}

	@ApiMethod(name = "queryConferences", path = "queryConferences", httpMethod = HttpMethod.POST)
	public List<Conference> queryConferences(ConferenceQueryForm conferenceQueryForm) {
		Query<Conference> query = conferenceQueryForm.getQuery();
		return query.list();
	}

	@ApiMethod(name = "getConferencesCreated", path = "getConferencesCreated", httpMethod = HttpMethod.POST)
	public List<Conference> getConferencesCreated(final User user) {
		Key<Profile> profile = Key.create(Profile.class, user.getUserId());
		Query<Conference> query = ofy().load().type(Conference.class).ancestor(profile).order("name");
		return query.list();
	}

	public List<Conference> filterPlayground() {
		Query<Conference> query = ofy().load().type(Conference.class);
		// Filtrado de conferencia con city = LONDON
		//query = query.filter("city =", "LONDON");
		//query = query.filter("topics =", "Medical Innovation");
		//query = query.filter("month =",6);
		//query = query.filter("maxAttendees >", 10).order("maxAttendees");
		query = query.filter("city =", "Tokyo").filter("seatsAvailable <", 10).filter("seatsAvailable >", 0)
				.order("seatsAvailable")
				.order("name")
				.order("month");
		return query.list();
	}
}
