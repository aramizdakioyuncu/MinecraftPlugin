package com.armoyu.utils;

import java.util.EnumSet;
import java.util.Set;

public enum PlayerRole {
    GUEST(EnumSet.noneOf(PlayerPermission.class)), // hiçbir izin yok
    REGISTERED(EnumSet.of(
            PlayerPermission.MOVE,
            PlayerPermission.INTERACT,
            PlayerPermission.BREAK,
            PlayerPermission.PLACE,
            PlayerPermission.SPRINT)),
    ADMIN(EnumSet.allOf(PlayerPermission.class)); // tüm izinler

    private final Set<PlayerPermission> permissions;

    PlayerRole(Set<PlayerPermission> permissions) {
        this.permissions = permissions;
    }

    public Set<PlayerPermission> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(PlayerPermission permission) {
        return permissions.contains(permission);
    }
}
