package eu.qwan.dirtytest.mocks;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;

public class UserPasswordResetTest {

    static final String EXISTING_USER = "existing-user";

    EmailFactory emailFactory = spy(new EmailFactory());
    InMemoryUserRepository userRepository = new InMemoryUserRepository();
    Email userEmail = new Email("", "user@company.com", "", "");
    Email sdEmail = new Email("", "servicedesk@qwan.eu", "", "");
    Mailer mailer = mock(Mailer.class);
    PasswordResetController ctrl = new PasswordResetController(emailFactory, userRepository, mailer);

    @BeforeEach
    void setup() {
        var user = new User(EXISTING_USER, "user@company.com");
        userRepository.add(user);
    }

    @Test
    public void sendNotification() throws Exception {
        ctrl.resetPassword(EXISTING_USER);

        verify(mailer).send(any(), any(), any(), any());
        verify(emailFactory).create(any(), any(), any());
    }

    @Test
    public void testNotificationFails() throws Exception {
        doThrow(new SendEmailFailed(new RuntimeException()))
            .doNothing()
            .when(mailer).send(any(), any(), any(), any());

        ctrl.resetPassword(EXISTING_USER);

        verify(mailer, times(2)).send(any(), any(), any(), any());
        verify(emailFactory, times(2)).create(any(), any(), any());
    }

    @Test
    public void userNotFound() throws Exception {
        ctrl.resetPassword("not-user");

        verify(mailer).send(any(), any(), any(), any());
        verify(emailFactory).create(any(), any(), any());
    }

    static class InMemoryUserRepository implements UserRepository {

        Map<String, User> users = new HashMap<>();

        @Override
        public Optional<User> byId(String id) {
            return Optional.ofNullable(users.get(id));
        }

        public void add(User user) {
            users.put(user.getId(), user);
        }
    }
}
