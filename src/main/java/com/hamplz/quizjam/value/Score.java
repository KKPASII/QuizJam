package com.hamplz.quizjam.value;

import com.hamplz.quizjam.exception.BadRequestException;
import com.hamplz.quizjam.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Score {
    @Column(name = "total_score", nullable = false)
    private int value = 0;

    protected Score() {}

    private Score(int score) {
        this.value = score;
    }

    public static Score zero() {
        return new Score(0);
    }

    public int getTotalScore() {
        return this.value;
    }

    public void add(int score) {
        if (score  < 0) {
            throw new BadRequestException(ErrorCode.SCORE_NOT_POSITIVE);
        }
        this.value += score;
    }
}
