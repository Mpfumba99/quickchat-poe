# QuickChat POE - PROG5121 (Programming 1A)

A console-based Java chat application built in three parts.

## Features
- **Part 1 - Registration and login:** username, password complexity and SA cell number checks (`Login`).
- **Part 2 - Sending messages:** 10-digit message IDs, recipient and length checks, message hash, send / disregard / store, JSON storage (`Message`, `QuickChat`).
- **Part 3 - Arrays and reports:** sent, stored and disregarded message arrays plus message hash and ID arrays, stored messages read from JSON, longest message, search by message ID or recipient, delete by message hash, and a full report (`MessageStore`).

## Running
Open the project in NetBeans (Maven) and run `Main.java`, or run `mvn test` to execute the JUnit tests.

## Automated tests
GitHub Actions runs all JUnit tests on every push (`.github/workflows/maven.yml`).

## References
- Leary, S. 2024. JSON in Java (org.json). [Online] Available at: https://github.com/stleary/JSON-java
