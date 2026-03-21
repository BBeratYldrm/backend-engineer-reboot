package com.berat.reboot.javacore;

import java.util.Objects;

public class User {
    private String name;

    public User(String name) {
        this.name = name;
    }

    // equals override
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(name, user.name);
    }

    // hashCode override
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}

