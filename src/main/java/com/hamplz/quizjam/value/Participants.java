package com.hamplz.quizjam.value;

import com.hamplz.quizjam.exception.ErrorCode;
import com.hamplz.quizjam.exception.quizRoom.RoomFullException;
import com.hamplz.quizjam.quizroom.entity.Participant;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

@Embeddable
public class Participants {

    private static final int MAX = 5;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Participant> values = new ArrayList<>();

    public Participants() {}

    public Participant createHost(Long userId, String nickname) {
        ensureCanJoin();
        return Participant.host(userId, nickname);
    }

    public Participant createAnonymous(String nickname) {
        ensureCanJoin();
        return Participant.anonymous(nickname);
    }

    public void add(Participant participant) {
        values.add(participant);
    }

    private void ensureCanJoin() {
        if (values.size() >= MAX) {
            throw new RoomFullException(ErrorCode.QUIZ_ROOM_FULL);
        }
    }

    public Participant getHost() {
        return values.stream().filter(Participant::isHost).findFirst().orElse(null);
    }

    public List<Participant> getValues() {
        return List.copyOf(values);
    }

    public int size() {
        return values.size();
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }
}