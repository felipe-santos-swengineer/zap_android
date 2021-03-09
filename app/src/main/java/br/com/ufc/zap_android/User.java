package br.com.ufc.zap_android;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private String id;
    private String name;
    private String fotoUrl;
    private String endereco;
    private String email;
    private String senha;

    public User(String id, String name, String fotoUrl, String endereco, String email, String senha) {
        this.id = id;
        this.name = name;
        this.fotoUrl = fotoUrl;
        this.endereco = endereco;
        this.email = email;
        this.senha = senha;
    }

    public User(){

    }

    protected User(Parcel in) {
        id = in.readString();
        name = in.readString();
        fotoUrl = in.readString();
        endereco = in.readString();
        email = in.readString();
        senha = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(fotoUrl);
        dest.writeString(endereco);
        dest.writeString(email);
        dest.writeString(senha);
    }
}
