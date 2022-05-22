package account.admin;

import account.auditor.Event;
import account.auditor.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.Logger;

@Service
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final EventService eventService;

    @Autowired
    public CustomAccessDeniedHandler(EventService eventService) {
        this.eventService = eventService;
    }

    public static final Logger LOG
            = Logger.getLogger(String.valueOf(CustomAccessDeniedHandler.class));

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {

        Authentication auth
                = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            LOG.warning("User: " + auth.getName()
                    + " attempted to access the protected URL: "
                    + request.getRequestURI());
        }
        response.sendError(403, "Access Denied!");

        assert auth != null;
        eventService.saveEvent(new Event(LocalDateTime.now(), "ACCESS_DENIED", auth.getName(),
                request.getRequestURI(), request.getRequestURI()));
    }
}
