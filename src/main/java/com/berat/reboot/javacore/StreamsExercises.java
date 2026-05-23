package com.berat.reboot.javacore;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class StreamsExercises {

    record Employee(String name, String department, int salary) {
    }

    public static void main(String[] args) {
        List<Employee> employees = List.of(
                new Employee("Ali", "Engineering", 5000),
                new Employee("Berat", "Marketing", 7000),
                new Employee("Veli", "Engineering", 6000),
                new Employee("Ayse", "Marketing", 4000),
                new Employee("Fatma", "Engineering", 8000)
        );

        // 1. Sort by salary ascending
        employees.stream()
                .sorted(Comparator.comparingInt(Employee::salary).reversed())
                .forEach(System.out::println);

        // 2. Find top 3 highest salaries
        employees.stream()
                .sorted(Comparator.comparingInt(Employee::salary).reversed())
                .limit(3)
                .forEach(System.out::println);

        // 3. Group by department
        employees.stream()
                .collect(Collectors.groupingBy(Employee::department))
                .forEach((department, empList) -> System.out.println(department + ": " + empList));

        // 4. Convert to Map<name, salary>
        employees.stream()
                .collect(Collectors.toMap(Employee::name, Employee::salary))
                .forEach((k, v) -> System.out.println(k + " " + v));

    }
}