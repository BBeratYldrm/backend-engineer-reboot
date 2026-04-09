package com.berat.reboot.javacore;

import org.springframework.stereotype.Service;
import java.util.List;

// CORRECT — Singleton is stateless
// State is passed as parameter, not stored in the bean
@Service
public class ReportService {

    // No instance variables that change — stateless
    public void addReport(List<String> reports, String report) {
        reports.add(report); // list comes from outside
    }

    public List<String> getReports(List<String> reports) {
        return reports; // list comes from outside
    }
}