package com;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WiseSaying {
    Integer id;
    String content;
    String author;

    public WiseSaying(Integer id, String content, String author) {
        this.id = id;
        this.content = content;
        this.author = author;
    }

    public static WiseSaying fromJson(Path file){
        Integer id = 0;
        String wiseSayingContent = "";
        String author = "";
        try {
            String content = Files.readString(file);
            int idStart = content.indexOf("\"id\":") + 5;
            int idEnd = content.indexOf(",", idStart);
            id = Integer.parseInt(content.substring(idStart, idEnd).trim());

            int contentStart = content.indexOf("\"content\":\"") + 11;
            int contentEnd = content.indexOf("\"", contentStart);
            wiseSayingContent = content.substring(contentStart, contentEnd);

            int authorStart = content.indexOf("\"author\":\"") + 10;
            int authorEnd = content.indexOf("\"", authorStart);
            author = content.substring(authorStart, authorEnd);

        } catch (IOException | StringIndexOutOfBoundsException | NumberFormatException e) {
            System.out.println("파일 처리 중 오류가 발생: " + e.getMessage());
        }
        return new WiseSaying(id, wiseSayingContent, author);
    }

    public String toJson() {
        return "{\"id\":" + this.id + ",\"content\":\"" + this.content + "\",\"author\":\"" + this.author + "\"}";
    }
    @Override
    public String toString() {
        return id + " / " + author + " / " + content;
    }
}
