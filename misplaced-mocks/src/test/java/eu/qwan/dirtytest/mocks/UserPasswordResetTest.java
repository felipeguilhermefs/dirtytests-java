package eu.qwan.dirtytest.mocks;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;

public class UserPasswordResetTest {

    EmailFactory emailFactory = mock(EmailFactory.class);
    InMemoryUserRepository userRepository = spy(new InMemoryUserRepository());
    Email email = mock(Email.class);
    Mailer mailer = mock(Mailer.class);
    User user = mock(User.class);

    @BeforeEach
    void setup() {
        when(user.getId()).thenReturn("user-id");
        userRepository.add(user);
    }

    @Test
    public void sendNotification() {
        when(user.getEmailAddress()).thenReturn("user@company.com");
        when(emailFactory.create(eq("user@company.com"), eq("PASSWORD_RESET"), any())).thenReturn(email);
        when(email.send(mailer)).thenReturn(true);
        PasswordResetController ctrl = new PasswordResetController(emailFactory, userRepository, mailer);
        ctrl.resetPassword("user-id");

        verify(email).send(any());
        verify(emailFactory).create(any(), any(), any());
    }

    @Test
    public void testNotificationFails() {
        when(user.getEmailAddress()).thenReturn("user@company.com");
        when(emailFactory.create(eq("user@company.com"), eq("PASSWORD_RESET"), any())).thenReturn(email);
        when(email.send(mailer)).thenReturn(false);
        Email email2 = mock(Email.class);
        when(email2.send(mailer)).thenReturn(true);
        when(emailFactory.create("servicedesk@qwan.eu", "SD", null)).thenReturn(email2);
        PasswordResetController ctrl = new PasswordResetController(emailFactory, userRepository, mailer);
        ctrl.resetPassword("user-id");

        verify(email).send(any());
        verify(email2).send(any());
        verify(emailFactory, times(2)).create(any(), any(), any());
        verify(userRepository).byId("user-id");
    }

    @Test
    public void userNotFound() {
        when(email.send(mailer)).thenReturn(false);
        when(emailFactory.create("servicedesk@qwan.eu", "SD", null)).thenReturn(email);
        PasswordResetController ctrl = new PasswordResetController(emailFactory, userRepository, mailer);
        ctrl.resetPassword("user-id2");

        verify(email).send(any());
        verify(emailFactory).create(any(), any(), any());
        verify(userRepository).byId("user-id2");
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
