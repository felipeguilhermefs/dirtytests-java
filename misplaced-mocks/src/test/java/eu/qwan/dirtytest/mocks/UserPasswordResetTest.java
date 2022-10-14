package eu.qwan.dirtytest.mocks;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;

public class UserPasswordResetTest {

    static final String EXISTING_USER = "existing-user";

    EmailFactory emailFactory = new EmailFactory();
    InMemoryUserRepository userRepository = new InMemoryUserRepository(
        new User(EXISTING_USER, "user@company.com")
    );
    Mailer mailer = mock(Mailer.class);
    PasswordResetController ctrl = new PasswordResetController(emailFactory, userRepository, mailer);

    @Test
    void sendsResetPasswordEmailWhenUserExists() throws Exception {
        ctrl.resetPassword(EXISTING_USER);

        verify(mailer).send(
            eq("user@company.com"),
            eq("info@qwan.eu"),
            eq("Password reset"),
            contains("Please click this link to reset your password: "));
    }

    @Test
    void sendsEmailToServiceDeskWhenResetPasswordEmailFails() throws Exception {
        doThrow(new SendEmailFailed(new RuntimeException()))
            .when(mailer).send(eq("user@company.com"), any(), any(), any());

        doNothing()
            .when(mailer).send(eq("servicedesk@qwan.eu"), any(), any(), any());

        ctrl.resetPassword(EXISTING_USER);

        verify(mailer, times(2)).send(any(), any(), any(), any());
    }

    @Test
    void sendsProblemNotificationEmailToServiceDeskWhenUserIsNotFound() throws Exception {
        ctrl.resetPassword("not-user");

        verify(mailer).send(
            eq("servicedesk@qwan.eu"),
            eq("info@qwan.eu"),
            eq("Problem notification"),
            eq("User servicedesk@qwan.eu wants to reset his/her password, but sending the email failed")
        );
    }

    static class InMemoryUserRepository implements UserRepository {

        Map<String, User> users = new HashMap<>();

        public InMemoryUserRepository(User user) {
            users.put(user.getId(), user);
        }

        @Override
        public Optional<User> byId(String id) {
            return Optional.ofNullable(users.get(id));
        }
    }
}
