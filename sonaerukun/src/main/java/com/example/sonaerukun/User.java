package com.example.sonaerukun;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users") 
public class User {
    @Id
    private String username;
    private String password;
    
    private String hostName;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // ★ getHostName() メソッドを定義
    public String getHostName() {
        return hostName;
    }

    // ★ setHostName() メソッドを定義
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}