package demo;

import demo.domain.MaxAndMinMap;
import demo.domain.UserVisitLog;
import demo.domain.VisitInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import static java.util.stream.Collectors.*;

public class ReadTask implements Callable<MaxAndMinMap> {
    private String filePath;
    private CountDownLatch countDownLatch;

    public ReadTask(String filePath, CountDownLatch countDownLatch) {
        this.filePath = filePath;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public MaxAndMinMap call() throws Exception {
        try (FileReader fileReader = new FileReader(filePath);
             BufferedReader br = new BufferedReader(fileReader)) {
            List<UserVisitLog> userVisitLogs = new ArrayList<>();
            while (true) {
                String s = br.readLine();
                if (s == null) {
                    break;
                }
                userVisitLogs.add(TrafficStatistics.parseLine(s));
            }
            if (userVisitLogs.isEmpty()) {
                return null;
            }
            Map<String, Map<String, Long>> urlIdCountMap = userVisitLogs.parallelStream().collect(
                    groupingBy(UserVisitLog::getUrl, groupingBy(UserVisitLog::getId, counting())));
            Map<String, Long> urlCountMap = userVisitLogs.parallelStream().collect(groupingBy(UserVisitLog::getUrl, counting()));
            List<Map.Entry<String, Long>> min20 = urlCountMap.entrySet().parallelStream()
                    .sorted(Map.Entry.comparingByValue())
                    .limit(20)
                    .collect(toList());
            List<Map.Entry<String, Long>> max20 = urlCountMap.entrySet().parallelStream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(20)
                    .collect(toList());
            List<VisitInfo> maxVisits = parse(urlIdCountMap, min20, true);
            List<VisitInfo> minVisits = parse(urlIdCountMap, min20, false);
            return new MaxAndMinMap(maxVisits, minVisits);
        } finally {
            countDownLatch.countDown();
        }

    }

    private List<VisitInfo> parse(Map<String, Map<String, Long>> urlIdCountMap, List<Map.Entry<String, Long>> entries, boolean withIds) {
        return entries.stream().map(e -> {
            VisitInfo visitInfo = new VisitInfo();
            visitInfo.setUrl(e.getKey());
            visitInfo.setCount(e.getValue());
            if (withIds) {
                visitInfo.setIds(urlIdCountMap.get(e.getKey()).entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit(20)
                        .map(Map.Entry::getKey)
                        .collect(joining(",")));
            }
            return visitInfo;
        }).collect(toList());
    }

}
