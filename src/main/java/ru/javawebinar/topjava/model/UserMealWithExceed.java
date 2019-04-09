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
    /*public UserMealWithExceed(UserMeal usermeal, boolean exceed) {
        this.dateTime = usermeal.getDateTime();
        this.description = usermeal.getDescription();
        this.calories = usermeal.getCalories();
        this.exceed = exceed;
    }*/
    public boolean isExceeded() {
        return this.exceed;
    }
    public LocalDateTime getDateTime() {
        return this.dateTime;
    }
    public String getDescription() {
        return description;
    }
    public int getCalories() {
        return calories;
    }
    @Override
    public String toString() {
        return
                dateTime.toString() + "\t" +
                        description + "\t" +
                        calories + "\t" +
                        exceed;
    }
    @Override
    public boolean equals(Object userMealWithExceed) {
        if (!(userMealWithExceed instanceof UserMealWithExceed)) {
            return false;
        }
        UserMealWithExceed other = (UserMealWithExceed) userMealWithExceed;
        if (this.dateTime.isEqual(other.dateTime) &&
            this.description.equals(other.description) &&
            this.calories == other.calories &&
            this.exceed == other.exceed
        ) {
            return true;
        }
        return false;
    }

}
