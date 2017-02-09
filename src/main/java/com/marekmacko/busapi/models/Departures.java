package com.marekmacko.busapi.models;

import java.util.List;

public class Departures {

    private List<Table> tables;

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public static class Table {
        private String description;
        private List<Hour> hours;

        public List<Hour> getHours() {
            return hours;
        }

        public void setHours(List<Hour> hours) {
            this.hours = hours;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class Hour {
        private String hour;
        private List<String> minutes;

        public String getHour() {
            return hour;
        }

        public void setHour(String hour) {
            this.hour = hour;
        }

        public List<String> getMinutes() {
            return minutes;
        }

        public void setMinutes(List<String> minutes) {
            this.minutes = minutes;
        }
    }
}
