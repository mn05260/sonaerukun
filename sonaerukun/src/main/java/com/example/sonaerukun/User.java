package com.example.sonaerukun;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
@Entity //自動的にDBにテーブルを作成する
@Table(name = "users") //テーブル名を指定
public class User {
@Id//重複を防止する
private String username;
private String password;

public String getUsername(){
    return username;
}
public void setUsername(String username){
    this.username = username;
}
public String getPassword(){
    return password;
}
public void setPassword(String password){
    this.password = password;
}
}
