package io.github.skomarica.practice.user.web;

import io.github.skomarica.practice.support.RestResponsePage;
import io.github.skomarica.practice.user.domain.User;
import io.github.skomarica.practice.user.domain.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

/**
 * @author Sinisa Komarica
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Sql(scripts = "/fixture/sample_user_before.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "/fixture/sample_user_after.sql", executionPhase = AFTER_TEST_METHOD)
public class UserControllerIntegrationTest {

    private static final ParameterizedTypeReference<RestResponsePage<User>> responseTypeRef =
            new ParameterizedTypeReference<RestResponsePage<User>>() {
            };

    private static final Pattern URI_PATTERN_CREATED = Pattern.compile(".*/users/(\\d+)");


    @Autowired
    private UserService userService;

    @Autowired
    private TestRestTemplate restTemplate;

    private long count;

    @Before
    public void setUp() throws Exception {
        count = userService.countUsers();
    }

    @Test
    public void getUsersShouldReturnOkAndAllUsers() {
        ResponseEntity<RestResponsePage<User>> response = this.restTemplate.exchange(
                "/users", HttpMethod.GET, null, responseTypeRef);

        // validate status
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // validate body
        final List<User> users = response.getBody().getContent();

        assertThat(users).hasSize(5);
        assertThat(users.stream().map(User::getUsername).collect(toSet())).containsExactlyInAnyOrder("user1", "user2", "user3", "user4", "user5");

        // check database state
        assertThat(userService.countUsers()).isEqualTo(count);
    }

    @Test
    public void getUsersWhenPageSizeSortParametersShouldReturnOkAndSortedResults() {

        ResponseEntity<RestResponsePage<User>> response = this.restTemplate.exchange(
                "/users?page={page}&size={size}&sort={sort}", HttpMethod.GET, null, responseTypeRef, 1, 2, "username,DESC");

        // validate status
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // validate body
        final List<User> users = response.getBody().getContent();
        assertThat(users).hasSize(2);

        final User user = users.get(1); // sort by username descending, page size 2; page 0: {user5, user4}, page 1: {user3, user2}, page 2: {user1}
        assertThat(user.getUsername()).isEqualTo("user2");

        // check database state
        assertThat(userService.countUsers()).isEqualTo(count);
    }

    @Test
    public void getUserShouldReturnOkAndUser() throws Exception {
        final ResponseEntity<User> responseEntity = restTemplate.getForEntity(
                "/users/{id}", User.class, 1);

        // validate status
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        // validate body
        final User user = responseEntity.getBody();

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("user1");
        assertThat(user.getPassword()).isEqualTo("pass1");

        // check database state
        assertThat(userService.countUsers()).isEqualTo(count);
    }

    @Test
    public void getUserWhenIdDoesNotExistShouldReturnNotFound() throws Exception {
        final ResponseEntity<RestError> responseEntity = restTemplate.getForEntity(
                "/users/{id}", RestError.class, 6);

        // validate status
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // validate body
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("User with id 6 can not be found");

        // check database state
        assertThat(userService.countUsers()).isEqualTo(count);
    }

    @Test
    public void createUserShouldReturnCreatedAndUserIdAndLocation() {

        ResponseEntity<Long> responseEntity = this.restTemplate.postForEntity(
                "/users", new User(null, "user", "pass"), Long.class);

        //validate status
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // validate location header
        String location = responseEntity.getHeaders().getLocation().toString();
        Matcher matcher = URI_PATTERN_CREATED.matcher(location);
        assertThat(matcher.matches()).isTrue();
        Long locationHeaderId = Long.valueOf(matcher.group(1));

        // validate body
        final Long bodyId = responseEntity.getBody();
        assertThat(bodyId).isEqualTo(locationHeaderId);

        final User createdUser = userService.getUser(bodyId);
        assertThat(createdUser.getUsername()).isEqualTo("user");
        assertThat(createdUser.getPassword()).isEqualTo("pass");

        // check database state
        assertThat(userService.countUsers()).isEqualTo(count + 1);
    }

    @Test
    public void createUserWhenIdSetShouldIgnoreIdAndReturnCreatedAndUserIdAndLocation() {

        ResponseEntity<Long> responseEntity = this.restTemplate.postForEntity(
                "/users", new User(1L, "user", "pass"), Long.class);

        //validate status
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // validate location header
        String location = responseEntity.getHeaders().getLocation().toString();
        Matcher matcher = URI_PATTERN_CREATED.matcher(location);
        assertThat(matcher.matches()).isTrue();
        Long locationHeaderId = Long.valueOf(matcher.group(1));

        // validate body
        final Long bodyId = responseEntity.getBody();
        assertThat(bodyId).isEqualTo(locationHeaderId);

        // fetch and validate the created user
        final User createdUser = userService.getUser(bodyId);
        assertThat(createdUser.getUsername()).isEqualTo("user");
        assertThat(createdUser.getPassword()).isEqualTo("pass");

        // check database state
        assertThat(userService.countUsers()).isEqualTo(count + 1);
    }

    @Test
    public void createUserWhenMissingUsernameFieldShouldReturnBadRequest() {

        ResponseEntity<RestError> responseEntity = this.restTemplate.postForEntity(
                "/users", new User(null, null, "pass"), RestError.class);

        //validate status
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // check database state
        assertThat(userService.countUsers()).isEqualTo(count);
    }

    @Test
    public void updateUserShouldReturnNoContent() {

        HttpEntity<User> entity = new HttpEntity<>(new User(null, "user", "pass"), new HttpHeaders());

        ResponseEntity<Void> responseEntity = this.restTemplate.exchange(
                "/users/{id}", HttpMethod.PUT, entity, Void.class, 1);

        //validate status
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // fetch and validate the updated user
        final User updatedUser = userService.getUser(1L);
        assertThat(updatedUser.getUsername()).isEqualTo("user");
        assertThat(updatedUser.getPassword()).isEqualTo("pass");

        // check database state
        assertThat(userService.countUsers()).isEqualTo(count);
    }

    @Test
    public void updateUserWhenDifferentIdsInPathAndBodyShouldIgnoreOneFromBodyAndReturnNoContent() {

        HttpEntity<User> entity = new HttpEntity<>(new User(5L, "user", "pass"), new HttpHeaders());

        ResponseEntity<Void> responseEntity = this.restTemplate.exchange(
                "/users/{id}", HttpMethod.PUT, entity, Void.class, 1);

        //validate status
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // fetch and validate the updated and non-updated users
        final User updatedUser = userService.getUser(1L);
        assertThat(updatedUser.getUsername()).isEqualTo("user");
        assertThat(updatedUser.getPassword()).isEqualTo("pass");

        final User notUpdateUser = userService.getUser(5L);
        assertThat(notUpdateUser.getUsername()).isEqualTo("user5");
        assertThat(notUpdateUser.getPassword()).isEqualTo("pass5");

        // check database state
        assertThat(userService.countUsers()).isEqualTo(count);
    }

    @Test
    public void updateUserWhenMissingUsernameFieldShouldReturnBadRequest() {

        HttpEntity<User> entity = new HttpEntity<>(new User(null, null, "pass"), new HttpHeaders());

        ResponseEntity<Void> responseEntity = this.restTemplate.exchange(
                "/users/{id}", HttpMethod.PUT, entity, Void.class, 1);

        //validate status
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // check database state
        assertThat(userService.countUsers()).isEqualTo(count);
    }

    @Test
    public void deleteShouldReturnNoContent() {
        ResponseEntity<Void> response = this.restTemplate.exchange(
                "/users/{id}", HttpMethod.DELETE, null, Void.class, 5);

        // validate status
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // fetch and validate remaining users
        assertThat(userService.getUsers(null).getContent().stream().map(User::getUsername).collect(toSet()))
                .containsExactlyInAnyOrder("user1", "user2", "user3", "user4");

        // check database state
        assertThat(userService.countUsers()).isEqualTo(count - 1);
    }

    @Test
    public void deleteWhenIdDoesNotExistShouldReturnNotFound() {
        ResponseEntity<Void> response = this.restTemplate.exchange(
                "/users/{id}", HttpMethod.DELETE, null, Void.class, 6);

        // validate status
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // check database state
        assertThat(userService.countUsers()).isEqualTo(count);
    }

}