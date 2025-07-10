package com.rollingstone.model;

import jakarta.persistence.*;

@Entity
@Table(name="APP_USER")
public class AppUser {
    @Id
    private String username;
    private String password;
    private String roles; // comma-separated (e.g., "USER,ADMIN")

    public AppUser() {
    }

    public AppUser(String username, String password, String roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "AppUser{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", roles='" + roles + '\'' +
                '}';
    }
}

