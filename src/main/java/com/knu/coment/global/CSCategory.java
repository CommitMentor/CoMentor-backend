package com.knu.coment.global;

public enum CSCategory {
    DATA_STRUCTURES_ALGORITHMS("Data Structures / Algorithms"),
    OPERATING_SYSTEMS("Operating Systems"),
    NETWORKING("Networking"),
    DATABASES("Databases"),
    SECURITY("Security"),
    LANGUAGE_AND_DEVELOPMENT_PRINCIPLES("Language and Development Principles"),
    ETC("Etc");


    private final String label;

    CSCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

