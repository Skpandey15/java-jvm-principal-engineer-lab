package lab.order.incident;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Switches for incident drills: start the service with --lab.incident=inc-00N to activate one scenario.
 * With no value (the default) the service runs its healthy code paths.
 */
@Component
public class Incidents {

    private final String active;

    public Incidents(@Value("${lab.incident:none}") String active) {
        this.active = active.toLowerCase();
    }

    public boolean isActive(String incidentId) {
        return active.equals(incidentId);
    }
}
