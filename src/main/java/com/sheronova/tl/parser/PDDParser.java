package com.sheronova.tl.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public class PDDParser {
    public static void main(String[] args) throws IOException, SQLException {
        String url = "http://xn----7sbnackuskv0m.xn--p1ai";
        Document doc = Jsoup.connect("http://xn----7sbnackuskv0m.xn--p1ai/?marafon800").get();
        //Element elements = doc.select("script").get(0).childNode(0);


        String data = doc.select("script").get(0).childNode(0).attributes().get("data");
        String[] arr = data.split("; ");
        Map<Integer, Rool> rools = new HashMap<>();
        data = data.replaceAll("<s.*?/>", "");
        Pattern pattern = Pattern.compile("(\\d+)");
        Pattern pattern1 = Pattern.compile("\\s[a-zA-Z_-]+(\\[\\d+\\])[\\d\\[\\]]*=\".*?(\";)");
        Matcher matcher1 = pattern1.matcher(data);
        int ind = 0;
        while (matcher1.find()) {
            String str = matcher1.group();
            ind++;
            if (str.matches(".*a_answers(\\[\\d+\\])(\\[\\d+\\]).*")) {
                Matcher matcher = pattern.matcher(str);
                if (matcher.find()) {
                    Integer id = Integer.valueOf(matcher.group());
                    if (id.equals(0)) {
                        System.out.println("");
                    }
                    int ansId = 0;
                    if (matcher.find()) {
                        ansId = Integer.valueOf(matcher.group());
                    }
                    String value = str.split("=")[1];
                    value = value.substring(value.indexOf("\"") + 1, value.lastIndexOf("\""));
                    Rool rool = rools.get(id);
                    if (rool == null) {
                        rool = new Rool();
                    }
                    Map<Integer, String> answers = rool.getAnswers();
                    if (answers == null) {
                        answers = new HashMap<>();
                    }
                    answers.put(ansId, value);
                    rool.setAnswers(answers);
                    rools.put(id, rool);
                }
            }
            if (str.matches(".*quest_quest(\\[\\d+\\]).*")) {
                Matcher matcher = pattern.matcher(str);
                if (matcher.find()) {
                    Integer id = Integer.valueOf(matcher.group(0));
                    String value = str.split("=")[1];
                    value = value.substring(value.indexOf("\"") + 1, value.lastIndexOf("\""));
                    Rool rool = rools.get(id);
                    if (rool == null) {
                        rool = new Rool();
                    }
                    rool.setQuest(value);
                    rools.put(id, rool);
                }
            }
            if (str.matches(".*a_t(\\[\\d+\\]).*")) {
                Matcher matcher = pattern.matcher(str);
                if (matcher.find()) {
                    Integer id = Integer.valueOf(matcher.group(0));
                    String value = str.split("=")[1];
                    Rool rool = rools.get(id);
                    if (rool == null) {
                        rool = new Rool();
                    }
                    rool.setCorrect(Integer.valueOf(value.substring(value.indexOf("\"") + 1, value.lastIndexOf("\""))));
                    rools.put(id, rool);
                }
            }
            if (str.matches(".*quest_help(\\[\\d+\\]).*")) {
                Matcher matcher = pattern.matcher(str);
                if (matcher.find()) {
                    Integer id = Integer.valueOf(matcher.group(0));
                    String value = str.split("=")[1];
                    value = value.replace("\"", "");
                    if (value.contains("<")) {
                        value = value.substring(0, value.indexOf("<"));
                    }
                    Rool rool = rools.get(id);
                    if (rool == null) {
                        rool = new Rool();
                    }
                    rool.setHelp(value);
                    rools.put(id, rool);
                }
            }
            if (str.matches(".*quest_image_name(\\[\\d+\\]).*")) {
                Matcher matcher = pattern.matcher(str);
                if (matcher.find()) {
                    Integer id = Integer.valueOf(matcher.group(0));
                    String value = str.split("=")[1];
                    if (value.length() > 4) {
                        URL url1 = new URL(url + "/" + value.substring(value.indexOf("\"") + 1, value.lastIndexOf("\"")));
                        InputStream in = new BufferedInputStream(url1.openStream());
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        int n = 0;
                        while (-1 != (n = in.read(buf))) {
                            out.write(buf, 0, n);
                        }
                        out.close();
                        in.close();
                        byte[] response = out.toByteArray();
                        String filePath = "/home/anna/home-projects/telegram-bots/traffic_laws/src/main/resources/images/" + id + ".jpg";
                        FileOutputStream fos = new FileOutputStream(filePath);
                        fos.write(response);
                        fos.close();
                        Rool rool = rools.get(id);
                        if (rool == null) {
                            rool = new Rool();
                        }
                        rool.setImageFilePath(filePath);
                        rools.put(id, rool);
                    }
                }
            }
        }
        List<Rool> roolList = rools.values().stream().filter(value -> value.getAnswers() == null
                || value.getCorrect() == null || value.getQuest() == null
                || value.getHelp() == null).collect(Collectors.toList());
        if (roolList.size() > 0) {
            System.out.println("");
        }
        writeToDb(rools);
    }


    private static void writeToDb(Map<Integer, Rool> values) throws SQLException, JsonProcessingException {
        String url = "jdbc:postgresql://localhost:5432/traffic_laws";
        String user = "postgres";
        String password = "postgres";
        ObjectMapper mapper = new ObjectMapper();
        try (Connection con = DriverManager.getConnection(url, user, password)) {
            PreparedStatement st = con.prepareStatement("INSERT INTO tl_quests(id, quest, help, answers, image_file, correct) " +
                    "VALUES (?, ?, ?, ?, ?, ?)");
            for (Map.Entry<Integer, Rool> value : values.entrySet()) {
                st.setInt(1, value.getKey());
                st.setString(2, value.getValue().getQuest());
                st.setString(3, value.getValue().getHelp());
                st.setObject(4, mapper.writeValueAsString(value.getValue().getAnswers()), Types.OTHER);
                st.setString(5, value.getValue().getImageFilePath());
                st.setInt(6, value.getValue().getCorrect());
                st.executeUpdate();
            }

        }
    }
}


@Getter
@Setter
class Rool {
    String quest;
    Map<Integer, String> answers;
    String help;
    Integer correct;
    String imageFilePath;
}
