package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExceed;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

import static ru.javawebinar.topjava.util.TimeUtil.isBetween;

public class UserMealsUtil {
    public static void main(String[] args) {
        List<UserMeal> mealList = Arrays.asList(
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30,10,0), "Завтрак", 400),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30,13,0), "Обед", 1100),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30,20,0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 27, 9,0), "Завтрак", 400),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 27,13,0), "Обед", 1100),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 27,19,0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 31,10,0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 31,13,0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30,10,0), "Завтрак1", 500),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30,13,0), "Обед1", 1100),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30,20,0), "Ужин1", 500),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 31,10,0), "Завтрак1", 1000),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 31,13,0), "Обед1", 500),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30,10,0), "Завтрак", 400),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30,13,0), "Обед", 1100),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30,20,0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 28, 9,0), "Завтрак", 400),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 28,13,0), "Обед", 1100),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 28,19,0), "Ужин", 3500),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 31,10,0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 31,13,0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30,10,0), "Завтрак1", 500),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30,13,0), "Обед1", 1100),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30,20,0), "Ужин1", 500),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 31,10,0), "Завтрак1", 1000),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 31,13,0), "Обед1", 500),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 31,20,0), "Ужин1", 510)
        );
        List<UserMealWithExceed> listUserMealWithExceed =
                getFilteredWithExceeded(mealList, LocalTime.of(7, 0), LocalTime.of(20,0), 4000);
        listUserMealWithExceed.forEach(System.out::println);
        System.out.println("------------------------------------");

        List<UserMealWithExceed> listUserMealWithExceedLoop =
                getFilteredWithExceededLoop(mealList, LocalTime.of(7, 0), LocalTime.of(20,0), 4000);
        listUserMealWithExceedLoop.forEach(System.out::println);
        System.out.println("--------------------------------------------");

        System.out.println("checkSolutions: " + (checkSolutions(listUserMealWithExceed, listUserMealWithExceedLoop) == true ? "solutions is equal" : "wrong solutions"));

//        .toLocalDate();
//        .toLocalTime();
    }

    public static List<UserMealWithExceed>  getFilteredWithExceeded(List<UserMeal> mealList, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> caloriesPerDayMap =  mealList.stream()
                .collect(Collectors.groupingBy(um -> um.getDateTime().toLocalDate(),Collectors.summingInt(um -> um.getCalories())));
        return mealList.stream()
                .filter(userMeal -> userMeal.getDateTime().toLocalTime().isAfter(startTime) && userMeal.getDateTime().toLocalTime().isBefore(endTime))
                .map(um -> new UserMealWithExceed(um,caloriesPerDayMap.get(um.getDateTime().toLocalDate()) > caloriesPerDay))
                .collect(Collectors.toList());
    }

    public static List<UserMealWithExceed>  getFilteredWithExceededLoop(List<UserMeal> mealList, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> caloriesPerDayMap = new TreeMap<>();
        Map<LocalDate, List<Integer>> indexxMealListMap = new TreeMap<>();
        List<UserMealWithExceed> mealListWithExceed = new ArrayList<>();
        int indexUserMeal = 0;
        for (UserMeal userMeal : mealList) {
            LocalDate ld = userMeal.getDateTime().toLocalDate();
            caloriesPerDayMap.merge(ld, userMeal.getCalories(), (v1, v2) -> v1 + v2);   // accumulate total calories by date
            List<Integer> listUserMealsByDate = new ArrayList<>();                      // where date occurs in the mealList (index of UserMeal in the mealList with this date)
            if (!indexxMealListMap.containsKey(ld)) {                                   // add index of UserMeal in mealList into listUserMealsByDate
                listUserMealsByDate.add(indexUserMeal++);
                indexxMealListMap.put(ld, listUserMealsByDate);
            } else {
                listUserMealsByDate = (ArrayList<Integer>) indexxMealListMap.get(ld);
                listUserMealsByDate.add(indexUserMeal++);
                indexxMealListMap.put(ld, listUserMealsByDate);
            }
            if (caloriesPerDayMap.get(ld) > caloriesPerDay) {                           // total calories for this date becomes greater then threshold
                List<Integer> indexes = indexxMealListMap.get(ld);
                for (int exceed : indexes) {
                    UserMeal umwe = mealList.get(exceed);
                    if (umwe.getDateTime().toLocalTime().isAfter(startTime) && umwe.getDateTime().toLocalTime().isBefore(endTime)) {
                        UserMealWithExceed userMealWithExceed = new UserMealWithExceed(umwe, false);
                        if (mealListWithExceed.indexOf(userMealWithExceed) > -1) {      // change previous/initial insertion with exceed=false into userMealWithExceed
                            mealListWithExceed.get(mealListWithExceed.indexOf(userMealWithExceed));
                            mealListWithExceed.set(mealListWithExceed.indexOf(userMealWithExceed), new UserMealWithExceed(umwe, true));
                        } else {
                            mealListWithExceed.add(new UserMealWithExceed(umwe, true));
                        }
                    }
                }
                System.out.println("listUserMealsByDate: " + listUserMealsByDate);
                listUserMealsByDate.clear();                                            // avoid repeating call to the list and duplicates
            } else {                                                                    // while total calories less than caloriesPerDay - add new UserMealWithExceed(userMeal, false)
                if (userMeal.getDateTime().toLocalTime().isAfter(startTime) && userMeal.getDateTime().toLocalTime().isBefore(endTime)) {
                    mealListWithExceed.add(new UserMealWithExceed(userMeal, false));
                }
            }
        }
        return mealListWithExceed;
    }
    public static boolean checkSolutions(List<UserMealWithExceed> listUserMealWithExceed, List<UserMealWithExceed> listUserMealWithExceedLoop) {
        if (listUserMealWithExceed.size() != listUserMealWithExceedLoop.size())
            return false;
        listUserMealWithExceed.sort((um1, um2) -> um1.getDateTime().compareTo(um2.getDateTime()));
        listUserMealWithExceedLoop.sort((um1, um2) -> um1.getDateTime().compareTo(um2.getDateTime()));
        for (int i = 0; i < listUserMealWithExceed.size(); i++){
            if (!listUserMealWithExceed.get(i).toString().equals(listUserMealWithExceedLoop.get(i).toString()))
                return false;
        }
        return true;
    }
}
