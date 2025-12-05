package com.hamplz.quizjam.value;

import com.hamplz.quizjam.quizroom.entity.Participant;
import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;

import java.util.ArrayList;
import java.util.List;

@Embeddable
public class Participants {

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Participant> values = new ArrayList<>();

    public Participants() {}

    public void add(Participant participant) {
        values.add(participant);
    }

    public int size() {
        return values.size();
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public List<Participant> getValues() {
        return List.copyOf(values);
    }

    public Participant getHost() {
        return values.stream().filter(Participant::isHost).findFirst().orElse(null);
    }
}