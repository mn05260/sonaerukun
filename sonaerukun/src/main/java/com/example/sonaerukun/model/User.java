package com.example.sonaerukun.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users") 
public class User {
    @Id
    private String username;
    @jakarta.persistence.Column(length = 100)
    private String password;
    
    private String hostName;
    private String joinCode;
    private boolean enabled = true;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // ★ getHostName() メソッドを定義
    public String getHostName() {
        return hostName;
    }
    public String getJoinCode(){
        return joinCode;
    }
    public void setJoinCode(String joinCode){
        this.joinCode = joinCode;
    }

    // ★ setHostName() メソッドを定義
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}