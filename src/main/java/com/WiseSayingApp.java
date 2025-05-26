package com;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class WiseSayingApp {
    static ArrayList<WiseSaying> wiseSayingList = new ArrayList<>();
    static String appRoot = "/Users/jiseopshin/DEV/dev_course_WiseSaying/src/main/resources";
    static Integer lastId = null;

    public static void start() {
        init();
        Scanner sc = new Scanner(System.in);

        System.out.println("== 명언 앱 ==");

        while (true) {
            System.out.print("명령) ");
            String cmd;
            cmd = sc.nextLine().trim();
            if (cmd.equals("종료")) {
                break;
            }

            switch (cmd) {
                case "종료":
                    return;
                case "등록":
                    System.out.print("명언 : ");
                    String wiseSayingContent = sc.nextLine().trim();
                    System.out.print("작가 : ");
                    String wiseSayingAuthor = sc.nextLine().trim();
                    if (wiseSayingList.isEmpty()) {
                        wiseSayingList.add(new WiseSaying(1, wiseSayingContent, wiseSayingAuthor));
                        lastId = 1;
                    } else {
                        wiseSayingList.add(new WiseSaying(wiseSayingList.getLast().id + 1, wiseSayingContent, wiseSayingAuthor));
                        lastId = wiseSayingList.getLast().id;
                    }
                    System.out.printf("%d번 명언이 등록되었습니다.\n", wiseSayingList.getLast().id);
                    break;
                case "목록":
                    System.out.println("번호 / 작가 / 명언");
                    System.out.println("-----------------------------");
                    wiseSayingList.forEach(System.out::println);
                    break;
                case "삭제":
                    System.out.print("?id = ");
                    int willingDeleteId = sc.nextInt();
                    sc.nextLine();
                    boolean isRemoved = wiseSayingList.removeIf(wiseSaying ->   // removeIf() 각 컬랙션에 대해 함수형 인터페이스를 사용해 true를 반환하는 요소를 제거하며 제거한 요소가 있다면 true, 그렇지 않다면 false를 반환한다.
                            wiseSaying.id.equals(willingDeleteId));
                    if (!isRemoved) {
                        System.out.printf("%d번 명언은 존재하지 않습니다.\n", willingDeleteId);
                    } else {
                        if (wiseSayingList.isEmpty()) {
                            lastId = 0;
                        } else {
                            lastId = wiseSayingList.getLast().id;
                        }
                        deleteFile(appRoot + "/db/wiseSaying", willingDeleteId);
                        System.out.printf("%d번 명언이 삭제되었습니다.\n", willingDeleteId);
                    }
                    break;
                case "수정":
                    System.out.print("?id = ");
                    int willingEditId = sc.nextInt();
                    sc.nextLine();
                    WiseSaying result = wiseSayingList.stream()
                            .filter(wiseSaying -> wiseSaying.id.equals(willingEditId))
                            .findFirst()
                            .orElse(null);
                    if (result == null) {
                        System.out.printf("%d번 명언은 존재하지 않습니다.\n", willingEditId);
                    } else {
                        System.out.println("명언(기존) : " + result.content);
                        System.out.print("명언 : ");
                        result.content = sc.nextLine().trim();
                        System.out.println("작가(기존) : " + result.author);
                        System.out.print("작가 : ");
                        result.author = sc.nextLine().trim();
                    }
                    break;
                case "빌드":
                    build();
                    System.out.println("data.json 파일의 내용이 갱신되었습니다.");
                    break;
            }
        }
        close();
        sc.close();
    }

    private static Integer getLastId() {
        try {
            return Integer.parseInt(Files.readString(Path.of(appRoot + "/db/wiseSaying/lastId.txt")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void init() {
        String path = appRoot + "/db/wiseSaying";

        if (!Files.exists(Paths.get(path + "/lastId.txt"))) {
            saveFile(path, "lastId", ".txt", "0");
            lastId = 0;
        } else {
            lastId = getLastId();
            try {
                Path dirPath = Paths.get(appRoot + "/db/wiseSaying");
                if (Files.exists(dirPath)) {
                    Files.list(dirPath)
                            .filter(file -> file.toString().endsWith(".json"))
                            .forEach(file -> {
                                WiseSaying wiseSaying = WiseSaying.fromJson(file);
                                wiseSayingList.add(wiseSaying);
                            });
                }
            } catch (IOException e) {
                System.out.println("Json 로딩 중 오류가 발생: " + e.getMessage());
            }
        }
    }

    public static void close() {
        wiseSayingList.forEach(w -> saveFile(appRoot + "/db/wiseSaying", w));
        saveFile(appRoot + "/db/wiseSaying", "lastId", ".txt", String.valueOf(lastId));
    }

    public static void saveFile(String path, String fileName, String fileExtension, String fileContent) {
        try {
            Path filePath = Paths.get(path, fileName + fileExtension);
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, fileContent);
        } catch (java.io.IOException e) {
            System.out.println("파일 저장 중 오류가 발생: " + e.getMessage());
        }
    }

    public static void saveFile(String path, WiseSaying wiseSaying) {
        try {
            Path filePath = Paths.get(path, wiseSaying.id + ".json");
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, wiseSaying.toJson());
        } catch (java.io.IOException e) {
            System.out.println("파일 저장 중 오류가 발생: " + e.getMessage());
        }
    }

    public static void deleteFile(String path, Integer id) {
        try {
            Path filePath = Paths.get(path, id + ".json");
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.out.println("파일 삭제 중 오류가 발생: " + e.getMessage());
        }
    }

    public static void build() {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n\t");
        wiseSayingList.forEach(w -> sb.append(w.toJson() + ",\n\t"));
        sb.delete(sb.length() - 3, sb.length());
        sb.append("\n]");
        String data = sb.toString();
        saveFile(appRoot + "/db/wiseSaying", "data", ".json", data);
    }
}
