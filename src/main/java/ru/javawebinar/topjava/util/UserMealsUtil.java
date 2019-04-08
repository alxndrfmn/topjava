package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExceed;

import java.time.*;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;
import static ru.javawebinar.topjava.util.TimeUtil.isBetween;

public class UserMealsUtil {
    public static void main(String[] args) {
        List<UserMeal> mealList = Arrays.asList(
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30,10,0), "Завтрак", 400),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30,13,0), "Обед", 1100),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30,20,0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 31,10,0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 31,13,0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 31,20,0), "Ужин1", 510)
        );
        //mealList = generateUserMeals(LocalDate.of(1975, 1, 1), LocalDate.now());
        final LocalTime startTime = LocalTime.of(7, 0);
        final LocalTime endTime = LocalTime.of(12,0);
        final int caloriesPerDay = 2_000;

        List<UserMealWithExceed> listUserMealWithExceed =
                getFilteredWithExceeded(mealList, startTime, endTime, caloriesPerDay);

        List<UserMealWithExceed> listUserMealWithExceedLoop =
                getFilteredWithExceededLoop(mealList, startTime, endTime, caloriesPerDay);
        System.out.println("checkSolutions: " + (checkSolutions(listUserMealWithExceed, listUserMealWithExceedLoop) == true ? "solutions is the same" : "wrong solutions"));

        List<UserMealWithExceed> listUserMealWithExceedCycle =
                getFilteredWithExceededCycle(mealList, startTime, endTime, caloriesPerDay);
        System.out.println("checkSolutions: " + (checkSolutions(listUserMealWithExceed, listUserMealWithExceedCycle) == true ? "solutions is the same" : "wrong solutions"));
    }

    public static List<UserMealWithExceed>  getFilteredWithExceeded(List<UserMeal> mealList, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> caloriesPerDayMap =  mealList.stream()
                .collect(Collectors.groupingBy(um -> um.getDateTime().toLocalDate(),Collectors.summingInt(um -> um.getCalories())));
        return mealList.stream()
                .filter(userMeal -> isBetween(userMeal.getDateTime().toLocalTime(),startTime, endTime))
                .map(um -> new UserMealWithExceed(um,caloriesPerDayMap.get(um.getDateTime().toLocalDate()) > caloriesPerDay))
                .collect(Collectors.toList());
    }
    public static List<UserMealWithExceed>  getFilteredWithExceededCycle(List<UserMeal> mealList, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> caloriesPerDayMap = new TreeMap<>();
        mealList.forEach(userMeal -> {
            caloriesPerDayMap.put(userMeal.getDateTime().toLocalDate(),caloriesPerDayMap.getOrDefault(userMeal.getDateTime().toLocalDate(), 0) + userMeal.getCalories());
            return;
        });
        List<UserMealWithExceed> userMealWithExceedList = new ArrayList<>();
        for (UserMeal userMeal : mealList) {
            if (isBetween(userMeal.getDateTime().toLocalTime(), startTime, endTime)) {
                userMealWithExceedList.add(new UserMealWithExceed(userMeal, caloriesPerDayMap.get(userMeal.getDateTime().toLocalDate()) > caloriesPerDay));
            }
        }
        return userMealWithExceedList;
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
        if (listUserMealWithExceed.size() != listUserMealWithExceedLoop.size()) {
            System.out.println("Size of lists is not the same: " + listUserMealWithExceed.size() + " <> " + listUserMealWithExceedLoop.size());
            System.out.println(listUserMealWithExceed);
            System.out.println(listUserMealWithExceedLoop);
            return false;
        }
        listUserMealWithExceed.sort((um1, um2) -> um1.getDateTime().compareTo(um2.getDateTime()));
        listUserMealWithExceedLoop.sort((um1, um2) -> um1.getDateTime().compareTo(um2.getDateTime()));
        for (int i = 0; i < listUserMealWithExceed.size(); i++){
            if (!listUserMealWithExceed.get(i).toString().equals(listUserMealWithExceedLoop.get(i).toString()))
                return false;
        }
        return true;
    }
    private static List<UserMeal> generateUserMeals(LocalDate fromDate, LocalDate toDate) {
        final String[] meals = {"Breakfast", "Dinner", "Supper"};
        final LocalTime[] timeOfMeals = {LocalTime.of(8, 0), LocalTime.of(13, 0), LocalTime.of(19, 0)};
        LocalDate ld = fromDate;
        Random random = new Random();
        random.nextInt(1000);
        List<UserMeal> mealList = new ArrayList<>();
        while (ld.isBefore(toDate.plusDays(1L))) {
            for (int i = 0; i < timeOfMeals.length; i++) {
                LocalTime timeOfMeal = timeOfMeals[i];
                LocalDateTime ldt = LocalDateTime.of(ld.getYear(), ld.getMonth(), ld.getDayOfMonth(), timeOfMeal.getHour(), timeOfMeal.getMinute());
                mealList.add( new UserMeal(ldt, meals[i], 500 + random.nextInt(1001)));
            }
            ld = ld.plusDays(1);
        }
        return mealList;
    }
}
