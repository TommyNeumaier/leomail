package at.htlleonding.leomail.services;

import jakarta.inject.Inject;

public class KeycloakUserSynchronisation {

    @Inject
    KeycloakAdminService adminService;

    public void synchroniseUsers() {
        adminService.synchroniseUsers();
    }
}
