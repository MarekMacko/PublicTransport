package com.marekmacko.busapi;

import com.marekmacko.busapi.models.Departures;
import com.marekmacko.busapi.models.TransportCategory;
import com.marekmacko.busapi.models.Line;
import com.marekmacko.busapi.models.Stop;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HtmlParser {

    private static final Logger LOGGER = Logger.getLogger(HtmlParser.class.getName());
    private static final String ZDITM_BASE_URL = "http://www.zditm.szczecin.pl/";
    private static final String TIMETABLE_BY_LINES = "pasazer/rozklady-jazdy,wedlug-linii";

    public void parseCategories() throws IOException {
        List<TransportCategory> categories = new ArrayList<>();
        Document document = Jsoup.connect(ZDITM_BASE_URL + TIMETABLE_BY_LINES).get();
        Elements tables = document.select("ul.listalinii");
        tables.forEach(table -> {
            String description = parseDescription(table);

            TransportCategory transportCategory = new TransportCategory();
            transportCategory.setDescription(description);

            List<Line> lines = parseLines(table);
            transportCategory.setLines(lines);
            categories.add(transportCategory);
        });
        LOGGER.log(Level.INFO, "success");
    }

    private String parseDescription(Element table) {
        Element header = table.previousElementSibling();
        return header.text();
    }

    private List<Line> parseLines(Element table) {
        List<Line> lines = new ArrayList<>();
        Elements rows = table.getElementsByTag("li");
        rows.forEach(row -> {
            Line line = parseLine(row);
            lines.add(line);
        });
        return lines;
    }

    private Line parseLine(Element row) {
        Line line = new Line();
        String number = row.text();
        String stopsUrl = parseStopsUrl(row);
        line.setNumber(number);
        line.setStopsUrl(stopsUrl);
        return line;
    }

    private String parseStopsUrl(Element row) {
        Element link = row.select("a").first();
        String baseUri = row.baseUri();
        String timetableUrl = link.attr("href");
        return baseUri + timetableUrl;
    }

    public void parseLineStops(String lineUrl) throws IOException {
        Element body = Jsoup.connect(ZDITM_BASE_URL + lineUrl)
                .get()
                .body();

        Element routes = body.select("div.trasy").first();
        Elements lineDirections = routes.getElementsByTag("tbody");
        List<List<Stop>> directions = parseLineDirections(lineDirections);
        LOGGER.log(Level.INFO, "end");
    }

    private List<List<Stop>> parseLineDirections(Elements directions) {
        List<List<Stop>> lineDirections = new ArrayList<>(2);
        directions.forEach(direction -> {
            List<Stop> lineDirection = parseLineDirection(direction);
            lineDirections.add(lineDirection);
        });
        return lineDirections;
    }

    private List<Stop> parseLineDirection(Element direction) {
        List<Stop> stops = new ArrayList<>();
        Elements rows = direction.getElementsByTag("tr");
        rows.forEach(row -> {
            if (!row.hasClass("przystanekdod")) {
                Stop stop = parseStop(row);
                stops.add(stop);
            }
        });
        return stops;
    }

    private Stop parseStop(Element row) {
        Elements values = row.getElementsByTag("td");
        String name =  parseStopName(values);
        String time = parseStopTime(values);
        String timetableUrl = parseStopTimeTableUrl(values);
        Stop stop = new Stop();
        stop.setName(name);
        stop.setMin(time);
        stop.setTimetableUrl(timetableUrl);
        return stop;
    }

    private String parseStopName(Elements row) {
        for (Element e : row) {
            if (e.className().equals("przystanek")) {
                return e.text();
            }
        }
        return null;
    }

    private String parseStopTime(Elements row) {
        for (Element e : row) {
            if (e.className().equals("czas")) {
                return e.text();
            }
        }
        return null;
    }

    private String parseStopTimeTableUrl(Elements row) {
        for (Element e : row) {
            if (e.className().equals("przystanek")) {
                Element link = e.getElementsByTag("a").first();
                String baseUrl = link.baseUri();
                return baseUrl + link.attr("href");
            }
        }
        return null;
    }

    public void parseStopDepartures(String departuresUrl) throws IOException {
        Element body = Jsoup.connect(ZDITM_BASE_URL + departuresUrl)
                .get()
                .body();

        Element timetable = body.select("div.rozkladmaly").first();
        Elements tables = timetable.getElementsByTag("table");
        List<Departures.Table> departuresTables = parseTables(tables);
    }

    private List<Departures.Table> parseTables(Elements tables) {
        List<Departures.Table> departureTables = new ArrayList<>();
        tables.forEach(table -> {
            Departures.Table departuresTable = parseTable(table);
            departureTables.add(departuresTable);
        });
        return departureTables;
    }

    private Departures.Table parseTable(Element table) {
        String description = parseTableDescription(table);
        List<Departures.Hour> departureHours = parseDeparturesHours(table);
        Departures.Table departureTable = new Departures.Table();
        departureTable.setDescription(description);
        departureTable.setHours(departureHours);
        return departureTable;
    }

    private String parseTableDescription(Element table) {
        Element tableHeader = table.getElementsByTag("thead").first();
        tableHeader.getElementsByTag("span").forEach(Node::remove);
        return tableHeader.text();
    }

    private List<Departures.Hour> parseDeparturesHours(Element table) {
        Element tableBody = table.getElementsByTag("tbody").first();
        Elements rows = tableBody.getElementsByTag("tr");
        List<Departures.Hour> departureHours = new ArrayList<>();
        rows.forEach(row -> {
            Elements rowValues = row.getElementsByTag("td");
            Departures.Hour departureHour = parseDeparturesHour(rowValues);
            departureHours.add(departureHour);
        });
        return departureHours;
    }

    private Departures.Hour parseDeparturesHour(Elements rowValues) {
        Departures.Hour departureHour = new Departures.Hour();
        rowValues.forEach(value -> {
            if (value.className().equals("godzina")) {
                String hour = value.text();
                departureHour.setHour(hour);
            } else if (value.className().isEmpty()) {
                String minutes = value.text();
                List<String> minutesList = Arrays.asList(minutes.split(" "));
                departureHour.setMinutes(minutesList);
            }
        });
        return departureHour;
    }

}
