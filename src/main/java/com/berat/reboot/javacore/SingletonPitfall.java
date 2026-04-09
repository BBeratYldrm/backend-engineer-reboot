package com.berat.reboot.javacore;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

// WRONG — Singleton storing mutable state
// All threads share this same list → race condition
@Service
public class SingletonPitfall {

    private List<String> reports = new ArrayList<>();

    public void addReport(String report) {
        reports.add(report);
    }

    public List<String> getReports() {
        return reports;
    }
}