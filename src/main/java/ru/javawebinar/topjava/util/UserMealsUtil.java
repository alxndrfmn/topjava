package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExceed;

import java.sql.Time;
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
        final LocalTime startTime = LocalTime.of(Integer.valueOf(args[0]), Integer.valueOf(args[1]));
        final LocalTime endTime = LocalTime.of(Integer.valueOf(args[2]),Integer.valueOf(args[3]));
        final int caloriesPerDay = Integer.valueOf(args[4]);
        List<UserMeal> mealList = Arrays.asList(
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30,10,0), "Завтрак", 400),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30,13,0), "Обед", 1100),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 30,20,0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 31,10,0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 31,13,0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2015, Month.MAY, 31,20,0), "Ужин1", 510)
        );
        mealList = generateUserMeals(LocalDate.of(Integer.valueOf(args[5]), 1, 1), LocalDate.now());
        if ((args[6].equals("shuffle")))
            Collections.shuffle(mealList, new Random());

        LocalTime beginTime = LocalTime.now();
        List<UserMealWithExceed> listUserMealWithExceed =
                getFilteredWithExceeded(mealList, startTime, endTime, caloriesPerDay);
        displayElapsedTime(beginTime, "getFilteredWithExceeded");

        beginTime = LocalTime.now();
        List<UserMealWithExceed> listUserMealWithExceedCycle =
                getFilteredWithExceededCycle(mealList, startTime, endTime, caloriesPerDay);
        displayElapsedTime(beginTime, "getFilteredWithExceededCycle");
        System.out.println("checkSolutions: " + (checkSolutions(listUserMealWithExceed, listUserMealWithExceedCycle) == true ? "solutions is the same" : "wrong solutions"));

        beginTime = LocalTime.now();
        List<UserMealWithExceed> listUserMealWithExceedOneLoop =
                getFilteredWithExceededON(mealList, startTime, endTime, caloriesPerDay);
        displayElapsedTime(beginTime, "listUserMealWithExceedOneLoop");
        System.out.println("checkSolutions: " + (checkSolutions(listUserMealWithExceed, listUserMealWithExceedOneLoop) == true ? "solutions is the same" : "wrong solutions"));
    }

    public static List<UserMealWithExceed>  getFilteredWithExceeded(List<UserMeal> mealList, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> caloriesPerDayMap =  mealList.stream()
                .collect(Collectors.groupingBy(um -> um.getDateTime().toLocalDate(),Collectors.summingInt(UserMeal::getCalories)));
        return mealList.stream()
                .filter(userMeal -> isBetween(userMeal.getDateTime().toLocalTime(),startTime, endTime))
                .map(userMeal -> new UserMealWithExceed(userMeal.getDateTime(), userMeal.getDescription(), userMeal.getCalories(),
                        caloriesPerDayMap.get(userMeal.getDateTime().toLocalDate()) > caloriesPerDay))
                .collect(Collectors.toList());
    }
    public static List<UserMealWithExceed>  getFilteredWithExceededCycle(List<UserMeal> mealList, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> caloriesPerDayMap = new HashMap<>();
        for (UserMeal meal : mealList) {
            caloriesPerDayMap.put(meal.getDateTime().toLocalDate(), caloriesPerDayMap.getOrDefault(meal.getDateTime().toLocalDate(), 0) + meal.getCalories());
        }
        List<UserMealWithExceed> userMealWithExceedList = new ArrayList<>();
        for (UserMeal userMeal : mealList) {
            if (isBetween(userMeal.getDateTime().toLocalTime(), startTime, endTime)) {
                userMealWithExceedList.add(new UserMealWithExceed(userMeal.getDateTime(), userMeal.getDescription(), userMeal.getCalories(),
                                                            caloriesPerDayMap.get(userMeal.getDateTime().toLocalDate()) > caloriesPerDay));
            }
        }
        return userMealWithExceedList;
    }
    public static List<UserMealWithExceed>  getFilteredWithExceededLoop(List<UserMeal> mealList, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> caloriesPerDayMap = new HashMap<>();
        Map<LocalDate, List<Integer>> indexxMealListMap = new HashMap<>();
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
                    if (isBetween(umwe.getDateTime().toLocalTime(), startTime, endTime)) {
                        UserMealWithExceed userMealWithExceed = new UserMealWithExceed(umwe.getDateTime(), umwe.getDescription(), umwe.getCalories(), false);
                        if (mealListWithExceed.indexOf(userMealWithExceed) > -1) {      // change previous/initial insertion with exceed=false into userMealWithExceed
                            mealListWithExceed.get(mealListWithExceed.indexOf(userMealWithExceed));
                            mealListWithExceed.set(mealListWithExceed.indexOf(userMealWithExceed), new UserMealWithExceed(umwe.getDateTime(), umwe.getDescription(), umwe.getCalories(), true));
                        } else {
                            mealListWithExceed.add(new UserMealWithExceed(umwe.getDateTime(), umwe.getDescription(), umwe.getCalories(), true));
                        }
                    }
                }
                listUserMealsByDate.clear();                                            // avoid repeating call to the list and duplicates
            } else {                                                                    // while total calories less than caloriesPerDay - add new UserMealWithExceed(userMeal, false)
                if (isBetween(userMeal.getDateTime().toLocalTime(), startTime, endTime)) {
                    mealListWithExceed.add(new UserMealWithExceed(userMeal.getDateTime(), userMeal.getDescription(), userMeal.getCalories(), false));
                }
            }
        }
        return mealListWithExceed;
    }
    public static List<UserMealWithExceed>  getFilteredWithExceededON(List<UserMeal> mealList, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> caloriesSumPerDayMap = new HashMap<>();
        Map<LocalDate, List<Integer>> mealListIndexxForDate = new HashMap<>();
        List<Integer> indexUserMealInMealList = new ArrayList<>();
        List<UserMealWithExceed> mealListWithExceed = new ArrayList<>();
        int indexInMealList = 0;
        for (UserMeal userMeal : mealList) {
            LocalDateTime ldt = userMeal.getDateTime();
            LocalDate ld = ldt.toLocalDate();
            mealListWithExceed.add(new UserMealWithExceed(ldt, userMeal.getDescription(), userMeal.getCalories(), false));
            List<Integer> listFrom = mealListIndexxForDate.getOrDefault(ld, new ArrayList<Integer>());
            listFrom.add(indexInMealList);
            mealListIndexxForDate.put(ld, listFrom);
            caloriesSumPerDayMap.put(ld, caloriesSumPerDayMap.getOrDefault(ld, 0) + userMeal.getCalories());
            if (caloriesSumPerDayMap.get(ld) > caloriesPerDay) {
                for (Integer indexInExceedList : listFrom) {
                    UserMealWithExceed umwe = mealListWithExceed.get(indexInExceedList);
                    mealListWithExceed.set(indexInExceedList, new UserMealWithExceed(umwe.getDateTime(), umwe.getDescription(), umwe.getCalories(), true));
                }
            }
            indexInMealList++;
        }
        mealListWithExceed.removeIf(um -> !isBetween(um.getDateTime().toLocalTime(), startTime, endTime));
        return mealListWithExceed;
    }
    public static boolean checkSolutions(List<UserMealWithExceed> listUserMealWithExceed, List<UserMealWithExceed> listUserMealWithExceedLoop) {
        //Collections.sort(listUserMealWithExceed, Comparator.comparing(userMealWithExceed -> userMealWithExceed.getDateTime()));
        //Collections.sort(listUserMealWithExceedLoop, Comparator.comparing(userMealWithExceed -> userMealWithExceed.getDateTime()));
        listUserMealWithExceed.sort((um1, um2) -> um1.getDateTime().compareTo(um2.getDateTime()));
        listUserMealWithExceedLoop.sort((um1, um2) -> um1.getDateTime().compareTo(um2.getDateTime()));
        return listUserMealWithExceed.equals(listUserMealWithExceedLoop);
    }
    private static List<UserMeal> generateUserMeals(LocalDate fromDate, LocalDate toDate) {
        final String[] meals = {"Breakfast", "Dinner", "Supper"};
        final LocalTime[] timeOfMeals = {LocalTime.of(8, 0), LocalTime.of(13, 0), LocalTime.of(19, 0)};
        LocalDate ld = fromDate;
        Random random = new Random();
        random.nextInt(1001);
        List<UserMeal> mealList = new ArrayList<>();
        while (ld.isBefore(toDate.plusDays(1L))) {
            for (int i = 0; i < timeOfMeals.length; i++) {
                LocalTime timeOfMeal = timeOfMeals[i];
                LocalDateTime ldt = LocalDateTime.of(ld.getYear(), ld.getMonth(), ld.getDayOfMonth(), timeOfMeal.getHour(), timeOfMeal.getMinute());
                mealList.add( new UserMeal(ldt, meals[i], random.nextInt(1001)));
            }
            ld = ld.plusDays(1);
        }
        return mealList;
    }
    private static void displayElapsedTime(LocalTime beginTime, String methodName) {
        System.out.println(Duration.between(beginTime, LocalTime.now()).toMillis() + " elapsed time: " + methodName);
    }
}
