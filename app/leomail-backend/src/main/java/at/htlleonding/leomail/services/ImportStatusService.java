package at.htlleonding.leomail.services;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.atomic.AtomicBoolean;

@ApplicationScoped
public class ImportStatusService {

    private final AtomicBoolean importing = new AtomicBoolean(false);

    public void setImporting(boolean status) {
        importing.set(status);
    }

    public boolean isImporting() {
        return importing.get();
    }
}