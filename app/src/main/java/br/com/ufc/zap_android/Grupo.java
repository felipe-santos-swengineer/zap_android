package br.com.ufc.zap_android;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Grupo implements Parcelable {
    private String id;
    private String name;
    private String fotoUrl;
    private ArrayList<User> users;
    private ArrayList<Message> messages;

    public Grupo(){}

    public Grupo(String id, String name, String fotoUrl, ArrayList<User> users, ArrayList<Message> messages) {
        this.id = id;
        this.name = name;
        this.fotoUrl = fotoUrl;
        this.users = users;
        this.messages = messages;
    }

    protected Grupo(Parcel in) {
        id = in.readString();
        name = in.readString();
        fotoUrl = in.readString();
        users = in.createTypedArrayList(User.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(fotoUrl);
        dest.writeTypedList(users);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Grupo> CREATOR = new Creator<Grupo>() {
        @Override
        public Grupo createFromParcel(Parcel in) {
            return new Grupo(in);
        }

        @Override
        public Grupo[] newArray(int size) {
            return new Grupo[size];
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

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }
}
