package ru.javawebinar.topjava.model;

import java.time.LocalDateTime;

public class UserMealWithExceed {
    private final LocalDateTime dateTime;

    private final String description;

    private final int calories;

    private final boolean exceed;

    public UserMealWithExceed(LocalDateTime dateTime, String description, int calories, boolean exceed) {
        this.dateTime = dateTime;
        this.description = description;
        this.calories = calories;
        this.exceed = exceed;
    }
    public UserMealWithExceed(UserMeal usermeal, boolean exceed) {
        this.dateTime = usermeal.getDateTime();
        this.description = usermeal.getDescription();
        this.calories = usermeal.getCalories();
        this.exceed = exceed;
    }
    public boolean isExceeded() {
        return this.exceed;
    }
    public LocalDateTime getDateTime() {
        return dateTime;
    }
    @Override
    public String toString() {
        return
                dateTime.toString() + "\t" +
                        description + "\t" +
                        calories + "\t" +
                        exceed;
    }

}
