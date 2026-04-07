// == checks reference equality (same object in memory)
// == kontrol eder: aynı nesne mi bellekte?

// equals() checks value/logical equality (same content)
// equals() kontrol eder: içerik aynı mı?

// Example:
// String a = new String("berat");
// String b = new String("berat");
// a == b       → false (different objects)
// a.equals(b)  → true  (same content)

package com.berat.reboot.javacore;

import java.util.HashSet;
import java.util.Set;

public class UserTest {

    public static void main(String[] args) {
        User u1 = new User("Berat");
        User u2 = new User("Berat");

        Set<User> set = new HashSet<>();

        set.add(u1);
        set.add(u2);
        System.out.println(set.size());
    }

    // Why override hashCode?
    // HashMap uses hashCode first, then equals.
}
